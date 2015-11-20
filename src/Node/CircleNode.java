package Node;

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
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * The Node.CircleNode represents a node in a unstructured circular P2P network.
 */
public class CircleNode {

    private String hostIp, leftSideIp, rightSideIp;
    private SocketInfo ownSocket, leftSide, rightSide;

    private int ownPort, leftSidePort, rightSidePort;

    private Map<Integer, String> ownResources = new HashMap<>();
    private Map<Integer, String> referencedResources = new HashMap<>();
    private ServerSocket inputServerSocket;

    private Thread echo;
    private boolean underConstruction;

    public CircleNode (int port)
    {
        setUpServer(port);
        Runnable run = this::listenToServerSocket;
        Thread thread = new Thread(run);
        thread.start();
    }

    public CircleNode (int port, int _ortherPort, String _otherIP)
    {
        ownSocket = new SocketInfo("",-1);// Initially empty
        leftSide = new SocketInfo("",-1); // Initially empty
        rightSide = new SocketInfo(_otherIP,_ortherPort);
        setUpServer(port);

        //Set up connection to others.
        ConnectToExistingNode();


        Runnable listen = this::listenToServerSocket;
        Thread thread = new Thread(listen);
        thread.start();
    }

    /**
     * Creates a server socket with the node's own port.
     */
    public void setUpServer(int port)
    {
        try
        {
            ownSocket.setPort(port);
            inputServerSocket = new ServerSocket(ownPort);
            ownSocket.setIp(inputServerSocket.getInetAddress().getLocalHost().getHostAddress());
        }
        catch (IOException e){e.printStackTrace();}
    }

    /**
     * Sends a connect message to an existing. This is used by new nodes that wants to join the system.
     */
    public void ConnectToExistingNode()
    {
        try
        {
            Socket destinationNode = rightSide.getSocket();

            ConnectMessage connectMessage = new ConnectMessage("From",ownSocket.getIp(),ownSocket.getPort());

            sendMessage(destinationNode,connectMessage);
        }
        catch (IOException e){e.printStackTrace();}
    }

    /***
     * Gets/retrieve a message from an input stream. If the incoming object is a message.
     * @param s
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private Message readMessageFromInputStream(Socket s) throws IOException, ClassNotFoundException {
        if(s==null) return null;
        ObjectInputStream inputStream = new ObjectInputStream(s.getInputStream());
        Object object = inputStream.readObject();

        if(object instanceof Message) return (Message)object;
        else return null;
    }

    /**
     * Listen to currents node server socket for incoming connection (From other nodes).
     */
    public void listenToServerSocket() {
        try {
            while (true) {
                Socket clientSocket = inputServerSocket.accept();
                Message inputMessage = readMessageFromInputStream(clientSocket); //Get incoming messages

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

        System.out.println("leftside: " + leftSide.getPort());
        System.out.println("rightside: " + leftSide.getPort());
    }

    private void updateCurrentNodeIp() throws UnknownHostException {
        ownSocket.setIp(inputServerSocket.getInetAddress().getLocalHost().getHostAddress());
    }

    /**
     * Handles a From connect message used to join a network initially.
     * The new node is connected to the left side of the existing node.
     * @param newIp of new node
     * @param newPort of new node
     */
    private void handleConnectFromMessage(String newIp, int newPort)
    {
        try
        {
            if (rightSide.getIp().equals("") && leftSide.getIp().equals("")) // Only one node in network
            {
                System.out.println("INSIDE");
                System.out.println(newPort + " " + newIp);

                //Sets sender ip and port to right and left side
                rightSide = new SocketInfo(newIp,newPort);
                leftSide = new SocketInfo(newIp,newPort);

                System.out.println("Connecting to Rightside - info: "+ rightSide.toString());

                //Send back to new node that it should put left side to this node.
                Socket rightSocket = rightSide.getSocket();

                //Updating stored ip of current node if it has changed
                updateCurrentNodeIp();

                // Tell rightSide node that it should set left side to current node
                ConnectMessage connectMessage = new ConnectMessage("Closure",ownSocket.getIp(),ownSocket.getPort());
                sendMessage(rightSocket, connectMessage);

                if (!ownResources.isEmpty()) // Send all information to rightSide node (The new node)
                {
                    sendMessage(rightSide.getSocket(), new ResourceMessage(ownResources));
                }
            }
            else //When there is more than one node
            {
                SocketInfo newNode = new SocketInfo(newIp,newPort) ;
                if (!referencedResources.isEmpty()) // Sends all inherited references to new middle node
                {
                    sendMessage(newNode.getSocket(), new ResourceMessage(referencedResources));
                    referencedResources.clear();
                }

                // Tell left side to set right side to the new node
                ConnectMessage connectMessage = new ConnectMessage("To",newNode.getIp(),newNode.getPort());
                sendMessage(leftSide.getSocket(), connectMessage);

                // Current nodes left side set to new node
                leftSide = newNode;
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
        rightSide = new SocketInfo(ip,port);

        try
        {
            if (underConstruction) // Circle reconstructed
            {
                if (!ownResources.isEmpty()) // Send copy of own resources to new right node so it can store them as references.
                {
                    sendMessage(rightSide.getSocket(),new ResourceMessage(ownResources));
                }
                underConstruction = false;
            }

            updateCurrentNodeIp();

            // Inform new right node to set left side to this node
            ConnectMessage connectMessage = new ConnectMessage("Closure",ownSocket.getIp(),ownSocket.getPort());
            sendMessage(rightSide.getSocket(), connectMessage);
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
        leftSide = new SocketInfo(ip,port);
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
            SocketInfo lostNode = new SocketInfo( reconstructMessage.getLostSideIp(),reconstructMessage.getLostSidePort());
            SocketInfo discoverNode = new SocketInfo(reconstructMessage.getDiscoverIp(),reconstructMessage.getDiscoverPort());

            if (lostNode.getPort() == leftSide.getPort()) //If left side node equals missing/lost port try to reconnect
            {
                // Send all inherited references to right side
                if (!referencedResources.isEmpty())
                {
                    sendMessage(rightSide.getSocket(), new ResourceMessage(referencedResources));

                    //Set references resource to own resource
                    String message;
                    for (Integer key : referencedResources.keySet())
                    {
                        message = referencedResources.get(key);
                        ownResources.put(key, message);
                    }
                    referencedResources.clear(); //Purge references
                }

                updateCurrentNodeIp();

                // Inform new left side that it should connect right side with itself
                Socket newLeftSideSocket = discoverNode.getSocket();
                sendMessage(newLeftSideSocket, new ConnectMessage("To", ownSocket.getIp(), ownSocket.getPort()));
                System.out.println("Tried to reconnect with: "+ discoverNode.toString());
                underConstruction = true; // Responds differently when a resourceMessage arrives.
            }
            else //Pass ip and port information to right side
            {
                // Can be removed
                ReconstructMessage newReconstructMessage = new ReconstructMessage(
                        lostNode.getIp(),
                        lostNode.getPort(),
                        discoverNode.getIp(),
                        discoverNode.getPort());
                sendMessage(leftSide.getSocket(), newReconstructMessage);
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
            try {sendMessage(leftSide.getSocket(), new EchoMessage(true,ownPort));}
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
        System.out.println("ECHO-MESSAGE"); // Start echo heartbeat
        if (echo != null)
        {
            echo.interrupt();
            echo = null;
        }
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
            System.out.println("Sent Heartbeat.");
            sendMessage(rightSide.getSocket(), new EchoMessage(false, ownPort));
            Thread.sleep(5000);
        }
        catch (IOException e)
        {
            System.out.println("An IOException occurred : ALERT...");
            reconstruct();
        }
        catch (InterruptedException e) {
            System.out.println("Neighbour is alive");
        }
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

        if (putMessage.getSentFromPut()) // Resource is put inside ownResources if
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