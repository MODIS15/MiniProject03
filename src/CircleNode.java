import Messages.*;

import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
//import java.net.SocketException;
//import java.net.UnknownHostException;
import java.util.HashMap;

/**
 * The CircleNode represents a node in a unstructured circular P2P network.
 */
public class CircleNode {

    private String hostIp, leftSideIp, rightSideIp;
    private int ownPort, leftSidePort, rightSidePort;

    private HashMap<Integer, String> ownResources = new HashMap<>();
    private HashMap<Integer, String> referencedResources = new HashMap<>();
    private ServerSocket inputServerSocket;

    private Thread echo;
    private boolean underConstruction;

    public CircleNode (int port)
    {
        ownPort = port;
        setUpServer();
        Runnable run = this::listenToServerSocket;
        Thread thread = new Thread(run);
        thread.start();
    }

    public CircleNode (int port, int _ortherPort, String _otherIP)
    {
        ownPort = port;
        hostIp = ""; // Initially empty
        leftSideIp = ""; // Initially empty
        rightSideIp = _otherIP;
        rightSidePort = _ortherPort;
        setUpServer();

        //Set up connection to others.
        sendStartConnectMessage();


        Runnable listen = this::listenToServerSocket;
        Thread thread = new Thread(listen);
        thread.start();
    }

    /**
     * Creates a server socket with the node's own port.
     */
    public void setUpServer()
    {
        try
        {
            inputServerSocket = new ServerSocket(ownPort);
            hostIp = inputServerSocket.getInetAddress().getLocalHost().getHostAddress();
        }
        catch (IOException e){e.printStackTrace();}
    }

    /**
     *
     */
    public void sendStartConnectMessage()
    {
        try
        {
            Socket startSocket = new Socket(rightSideIp, rightSidePort);

            ConnectMessage connectMessage = new ConnectMessage("From",hostIp,ownPort);

            sendMessage(startSocket,connectMessage);
        }
        catch (IOException e){e.printStackTrace();}
    }

    private Message readMessageFromInputStream(Socket s) throws IOException, ClassNotFoundException {
        if(s==null) return null;
        ObjectInputStream inputStream = new ObjectInputStream(s.getInputStream());
        Object object = inputStream.readObject();

        if(object instanceof Message) return (Message)object;
        else return null;
    }

    public void listenToServerSocket() {
        try {
            while (true) {
                Socket clientSocket = inputServerSocket.accept();

                Message inputMessage = readMessageFromInputStream(clientSocket);

                if (inputMessage == null) return;
                handleMessage(inputMessage);
            }
        }
        catch (IOException e){} catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to handle different message types appropriately.
     * @param message
     */
    public void handleMessage(Message message)
    {
        switch (message.getMessageType())
        {
            case ConnectMessage: //Join nodes
                handleConnectMessage((ConnectMessage)message);
                System.out.println("ECHO-MESSAGE"); // Start echo heartbeat
                initiateNewEcho();
                break;
            case ReconstructMessage: // Reconstruct circle network if broken
                handleReconstructMessage((ReconstructMessage)message);
                break;

            case EchoMessage: // Check if right side node is alive
                handleEchoMessage((EchoMessage) message);
                break;

            case ResourceMessage: // Send entire hash maps with resources to other nodes.
                handleResourceMessage((ResourceMessage) message);
                break;

            case PutMessage: // Place resource in a given node
                handlePutMessage((PutMessage)message);
                break;

            case GetMessage: // Get resource in existing node
                handleGetMessage((GetMessage)message);
                break;
        }
    }

    /**
     * Sends a serialized message to a given node.
     * @param socket endpoint of node to which message is sent
     * @param message message sent
     */
    private void sendMessage(Socket socket, Message message)
    {
        try
        {
            ObjectOutputStream clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
            clientOutputStream.writeObject(message);
        }
        catch (IOException e){e.printStackTrace();}
    }

    /**
     * Determines how nodes are placed in the network based on the state of the joining process.
     * @param connectMessage message added in network
     */
    private void handleConnectMessage(ConnectMessage connectMessage)
    {
        // Get values from sender
        String toFromOrClosure = connectMessage.getToAndFrom(); // Determine state of join process
        String ip = connectMessage.getIpAddress();
        int port = connectMessage.getPort();

        if(toFromOrClosure.equals("From"))// New node in network. Note that the receiver of message always sets left side to sender.
        {
            handleConnectFromMessage(ip,port);
        }
        else if (toFromOrClosure.equals("To")) //Sets right side to the ip and port and send a closure message to new right side.
        {
            handleConnectToMessage(ip,port);
        }
        else if (toFromOrClosure.equals("Closure")) //Sets left side ip and port.
        {
            handleConnectClosureMessage(ip,port);
        }

        System.out.println("leftside: " + leftSidePort);
        System.out.println("rightside: " + rightSidePort);
    }

    /**
     * Handles a From connect message used to join a network initially.
     * The new node is connected to the left side of the existing node.
     * @param ip of node
     * @param port of node
     */
    private void handleConnectFromMessage(String ip, int port)
    {
        try
        {
            if (rightSideIp.equals("") && leftSideIp.equals("")) // Only one node in network
            {
                System.out.println("INSIDE");
                System.out.println(port + " " + ip);

                //Sets sender ip and port to right and left side
                rightSideIp = ip;   rightSidePort = port;
                leftSideIp = ip;    leftSidePort = port;

                System.out.println(rightSidePort + " " + rightSideIp);

                //Send back to new node that it should put left side to this node.
                Socket rightSocket = new Socket(rightSideIp, rightSidePort);
                System.out.println("Create socket");

                String hostIP = inputServerSocket.getInetAddress().getLocalHost().toString();
                int index  = hostIP.indexOf("/");
                hostIP = hostIP.substring(index+1,hostIP.length());

                // Tell node it should set left side to this node
                ConnectMessage connectMessage = new ConnectMessage("Closure",hostIP,ownPort);

                sendMessage(rightSocket, connectMessage);

                if (!ownResources.isEmpty()) // Send all information to new node
                {
                    sendMessage(new Socket(rightSideIp,rightSidePort),
                                new ResourceMessage(ownResources));
                }
            }
            else //When there is more than one node
            {
                Socket leftSocket;
                if (!referencedResources.isEmpty()) // Sends all inherited references to new middle node
                {
                    leftSocket = new Socket(ip, port);
                    sendMessage(leftSocket, new ResourceMessage(referencedResources));
                    referencedResources.clear();
                }

                // Tell left side to set right side to new node
                leftSocket = new Socket(leftSideIp, leftSidePort);
                ConnectMessage connectMessage = new ConnectMessage("To",ip,port);
                sendMessage(leftSocket, connectMessage);

                // Left side set to sender
                leftSideIp = ip;
                leftSidePort = port;
            }
        }
        catch (IOException e){e.printStackTrace();}
    }

    /**
     * Handles a Connect To message by setting the ip and port to the current node's right side.
     * Checks if the network is being reconstructed, otherwise right node connects to left side of this node.
     * @param ip of node
     * @param port of node
     */
    private void handleConnectToMessage(String ip, int port)
    {
        rightSideIp = ip;
        rightSidePort = port;

        try
        {
            if (underConstruction) // Circle reconstructed
            {
                if (!ownResources.isEmpty()) // Send copy of own resources to new right node so it can store them as references.
                {
                    sendMessage(new Socket(rightSideIp, rightSidePort),
                                        new ResourceMessage(ownResources));
                }
                underConstruction = false;
            }

            String hostIP = inputServerSocket.getInetAddress().getLocalHost().toString();
            int index  = hostIP.indexOf("/");
            hostIP = hostIP.substring(index+1,hostIP.length());

            // Inform new right node to set left side to this node
            ConnectMessage connectMessage = new ConnectMessage("Closure",hostIP,ownPort);
            Socket rightSocket = new Socket(rightSideIp, rightSidePort);
            sendMessage(rightSocket, connectMessage);
        }
        catch (IOException e){e.printStackTrace();}
    }

    /**
     * Handles Closure message used to connect the last two remaining nodes in the network.
     * This is done by connecting the left side to the given ip and port.
     * @param ip
     * @param port
     */
    private void handleConnectClosureMessage(String ip, int port)
    {
        leftSideIp = ip;
        leftSidePort = port;
        if (underConstruction) {underConstruction = false; } // REMOVE?
    }

    /**
     * Handles a ReconstructMessage by reconnecting the remaining nodes from the left and rearranging their resources.
     * @param reconstructMessage used to reconstruct broken circular network
     */
    private void handleReconstructMessage(ReconstructMessage reconstructMessage)
    {
        try
        {
            String lostIP = reconstructMessage.getLostSideIp();
            int lostPort = reconstructMessage.getLostSidePort();
            String discoverIp = reconstructMessage.getDiscoverIp();
            int discoverPort = reconstructMessage.getDiscoverPort();

            if (lostPort == leftSidePort) //If left side node equals missing/lost port try to reconnect
            {
                // Send all inherited references to right side
                if (!referencedResources.isEmpty())
                {
                    sendMessage(new Socket(rightSideIp,rightSidePort),
                                new ResourceMessage(referencedResources)
                    );

                    String message = "";
                    for (Integer key : referencedResources.keySet()) // Remove all references now replaced by ownResources
                    {
                        message = referencedResources.get(key);
                        ownResources.put(key, message);
                    }
                    referencedResources.clear();
                }

                String localhost = inputServerSocket.getInetAddress().getLocalHost().toString();
                int index  = localhost.indexOf("/");
                localhost = localhost.substring(index+1,localhost.length());

                // Inform new left side that it should connect right side with itself
                Socket newLeftSideSocket = new Socket(discoverIp, discoverPort);
                sendMessage(newLeftSideSocket, new ConnectMessage("To", localhost, ownPort));
                System.out.println("Tried to reconnect with: " + discoverIp + " " + discoverPort);
                underConstruction = true; // Responds differently when a resourceMessage arrives.
            }
            else //Pass ip and port information to right side
            {
                // Can be removed
                ReconstructMessage newReconstructMessage = new ReconstructMessage(
                        lostIP = lostIP,
                        lostPort = lostPort,
                        discoverIp = discoverIp,
                        discoverPort = discoverPort
                );
                sendMessage(new Socket(leftSideIp, leftSidePort), newReconstructMessage);
            }
        }catch (IOException e){e.printStackTrace();}
    }

    /**
     * Handles an echo message based on whether the node is still alive.
     * If it is alive, the node should send back a message or otherwise retry contact.
     * @param echoMessage heartbeat to see if node is alive
     */
    private void handleEchoMessage(EchoMessage echoMessage )
    {
        boolean echoMessageContent = echoMessage.getStillAlive();
        if (echoMessageContent == false)
        {
            System.out.println("SEND ECHO RETURN");
            //Send echo-message return
            try {sendMessage(new Socket(leftSideIp,leftSidePort), new EchoMessage(true,ownPort));}
            catch (IOException e){e.printStackTrace();}
        }
        else
        {
            System.out.println("START NEW ECHO");
            //Receive echo-message - Stop echo
            echo.interrupt();
            echo = null;
            //Initiate new one echo-thread
            initiateNewEcho();
        }
    }


    /**
     * Repeatedly sends an echo heartbeat on a new thread by calling the sendEcho() method.
     */
    private void initiateNewEcho()
    {
        if (echo != null)
        {
            echo.interrupt();
            echo = null;
        }
        System.out.println("CALL!!!");
        Runnable echoSend = this::sendEcho;
        echo = new Thread(echoSend);
        echo.start();
    }

    /**
     * Sends out an echo heartbeat to the right side neighbor node asking if it is alive within a given time out.
     * If the echo has not terminated within time out something is wrong.
     * Note that is has not been implemented for left side nodes.
     */
    public void sendEcho()
    {
        try
        {
            Thread.sleep(3000); // Time out
            System.out.println("ECHO!!!");
            sendMessage(new Socket(rightSideIp, rightSidePort), new EchoMessage(false, ownPort));
            Thread.sleep(5000);
        }
        catch (IOException e)
        {
            System.out.println("An IOException occurred : ALERT...");
            reconstruct();
        }
        catch (InterruptedException e) {}
    }

    /**
     * Handles a ResourceMessage holding several resources by adding them to the node's referenced resources.
     * @param resourceMessage HashMap with several resources
     */
    public void handleResourceMessage(ResourceMessage resourceMessage)
    {
        HashMap<Integer,String> moreRefs = resourceMessage.getStoredResource();
        for (int key : moreRefs.keySet())
        {
            String message = moreRefs.get(key);
            referencedResources.put(key, message);
        }
        if (underConstruction) { underConstruction = false; }
    }

    /**
     * Handles a PutMessage by determining whether it is sent from a PutClient as an original message.
     * If the PutMessage is original it is stored in the node and otherwise added as a reference.
     * @param putMessage message with a resource
     */
    private void handlePutMessage(PutMessage putMessage)
    {
        Integer key = putMessage.getKey();
        String resource = putMessage.getResource();
        boolean original = putMessage.getOriginal(); // True if message is from PutClient

        if (putMessage.getOriginal()) // Resource is put inside ownResources
        {
            ownResources.put(key, resource);


            if (!rightSideIp.equals("")) // Send reference to right node socket if it exists
            {
                try
                {
                    PutMessage newPutMessage = new PutMessage(key,resource,false);
                    sendMessage(new Socket(rightSideIp,rightSidePort),newPutMessage);
                }
                catch (IOException e) {e.printStackTrace();}
            }
        }
        else // Add message to references if it is not original
        {
            referencedResources.put(key, resource);
        }
    }

    /**
     * Handles a get message request by checking if its key is already located in the node (ownResources) or its neighbors (referencedResources).
     * Else propagates the message to the right side "neighbor" node.
     * @param getMessage message used to retrieve resource
     */
    private void handleGetMessage(GetMessage getMessage)
    {
        try {
            int key = getMessage.getKey();
            int port = getMessage.getPort();
            String ip = getMessage.getIp();

            String message = "";
            Socket getClientSocket = new Socket(ip, port); // Connect to client

            if (ownResources.containsKey(key)) // Checks if ownResources has key
            {
                message = ownResources.get(key);
                sendMessage(getClientSocket, new PutMessage(key, message, false));
            }
            else if (referencedResources.containsKey(key)) // Checks if referenced resources has key
            {
                message = referencedResources.get(key);
                sendMessage(getClientSocket, new PutMessage(key, message, false));
            }
            else //Otherwise propagate message to the right side node.
            {
                sendMessage(new Socket(rightSideIp, rightSidePort), new GetMessage(key, ip, port));
            }
        }
        catch (IOException e) {e.printStackTrace();}
    }

    /**
     * Reconstructs the circular network in case of node failures (crash, disconnect etc.)
     * This is done by sending a message to the left side node that will eventually reach the right side node.
     */
    private void reconstruct()
    {
        underConstruction = true;
        System.out.println(rightSidePort + "=" + leftSidePort + " " + rightSideIp + "=" + leftSideIp);
        if (rightSidePort == leftSidePort)
        {
            System.out.println("STOP");
            echo.interrupt();
            echo = null;
            rightSideIp = "";
            leftSideIp = "";
            if (!referencedResources.isEmpty())
            {
                String message = "";
                for (int key : referencedResources.keySet())
                {
                    message = referencedResources.get(key);
                    ownResources.put(key,message);
                }
                referencedResources.clear();
            }

        }
        else
        {
            try {
                String lostHostIP = inputServerSocket.getInetAddress().getLocalHost().toString();
                int index = lostHostIP.indexOf("/");
                lostHostIP = lostHostIP.substring(index + 1, lostHostIP.length());

                ReconstructMessage reconstructMessage = new ReconstructMessage(
                        rightSideIp,
                        rightSidePort,
                        lostHostIP,
                        ownPort
                );

                sendMessage(new Socket(leftSideIp, leftSidePort), reconstructMessage);

            } catch (IOException e) {
            }
        }
    }


    public static void main(String[] args)
    {
        if (args.length == 1) //For the first port only
        {
            CircleNode circleNode = new CircleNode(Integer.parseInt(args[0]));
        }
        else //For all other node there after.
        {
            CircleNode circleNode = new CircleNode(Integer.parseInt(args[0]),Integer.parseInt(args[1]),args[2]);
        }
    }
}