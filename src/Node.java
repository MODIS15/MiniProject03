import Messages.*;
import NodeUtils.DeadException;
import NodeUtils.SocketInfo;
import NodeUtils.UserInput;

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
 * The Node represents a node in a unstructured circular P2P network.
 */
public class Node {

    private SocketInfo ownSocket, leftSide, rightSide;
    private ServerSocket inputServerSocket;

    private Map<Integer, String> ownResources = new HashMap<>();
    private Map<Integer, String> referencedResources = new HashMap<>();

    private Thread echo;
    private boolean underConstruction;

    //Constructors

    /**
     * Constructor used when it is the first node in network
     * @param port for serversocket
     */
    public Node(int port)
    {
        leftSide = new SocketInfo("",-1); // Initially empty
        rightSide = new SocketInfo("",-1);
        setUpServer(port);
        Runnable run = this::listenToServerSocket;
        Thread thread = new Thread(run);
        thread.start();
    }

    /**
     * Constructor used when connecting to existing network.
     * @param port of server socket
     * @param _otherPort port of existing node
     * @param _otherIP ip of existing node
     */
    public Node(int port, int _otherPort, String _otherIP)
    {
        leftSide = new SocketInfo("",-1); // Initially empty
        rightSide = new SocketInfo(_otherIP,_otherPort);
        setUpServer(port);

        ConnectToExistingNode();

        Runnable listen = this::listenToServerSocket;
        Thread listenServerSocket = new Thread(listen);
        listenServerSocket.start();
    }



    //Connection and network Methods

    /**
     * Used to update current nodes ip in case it was changed.
     * @throws UnknownHostException
     */
    private void updateCurrentNodeIp() throws UnknownHostException {
        ownSocket.setIp(inputServerSocket.getInetAddress().getLocalHost().getHostAddress());
    }

    /**
     * Creates a server socket with the node's own port.
     */
    public void setUpServer(int port)
    {
        try
        {
            ownSocket = new SocketInfo("",-1);// Initially empty

            ownSocket.setPort(port);
            inputServerSocket = new ServerSocket(ownSocket.getPort());
            updateCurrentNodeIp();

            System.out.println("Node Initiated.");
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

    /**
     * Listen to currents node server socket for incoming connection (From other nodes).
     */
    public void listenToServerSocket() {
        try {
            while (true) {
                Socket clientSocket = inputServerSocket.accept();
                Message inputMessage = readMessageFromInputStream(clientSocket); //Get incoming messages

                //Creates a new thread whenever a message is to be handled. To avoid bottlenecks
                if (inputMessage == null) return;
                if(!(inputMessage instanceof EchoMessage))
                    System.out.println("Received connection from - IP:"+ clientSocket.getInetAddress() + " Port " + clientSocket.getPort());

                MessageHandler messageHandler = new MessageHandler(inputMessage);

            }
        }
        catch (IOException e){} catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reconstructs the circular network in case of node failures (crash, disconnect etc.)
     * This is done by sending a message to the right side node that will eventually reach the left side end node.
     * If there is no nodes left, it will set its right and left to itself.
     */
    private void reconstruct()
    {
        underConstruction = true;
        System.out.println("Comparing right and left: "+rightSide.getPort() + "=" + leftSide.getPort() + " " + rightSide.getIp() + "=" + leftSide.getIp());

        //There is only one node left in system
        if (rightSide.equals(leftSide))
        {
            System.out.println("STOP");
            echo.interrupt();
            echo = null;
            rightSide.setIp("");
            leftSide.setIp("");
            if (!referencedResources.isEmpty())
            {
                String message;
                for (int key : referencedResources.keySet())
                {
                    message = referencedResources.get(key);
                    ownResources.put(key,message);
                }
                referencedResources.clear();
            }

        }

        //Sends a reconstruct message to right node.
        else
        {
            try {
                updateCurrentNodeIp();

                ReconstructMessage reconstructMessage = new ReconstructMessage(
                        rightSide.getIp(),
                        rightSide.getPort(),
                        ownSocket.getIp(),
                        ownSocket.getPort()
                );

                sendMessage(leftSide.getSocket(), reconstructMessage);

            } catch (IOException e) {
            }
        }
    }


    //Message methods

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
            clientOutputStream.close();

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




    //Message Handlers

    /**
     * This inner class is used to create new threads when incoming messages are to be handled.
     * This is to avoid bottlenecks when many incoming messages are to be handled
     */
    private class MessageHandler extends Thread
    {
        public MessageHandler(Message message)
        {
            synchronized (this)
            {
                handleMessage(message);
            }
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
        System.out.println("rightside: " + rightSide.getPort());
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
            // Only one node in network
            if (rightSide.getIp().equals("") && leftSide.getIp().equals(""))
            {
                //Sets sender ip and port to right and left side
                rightSide = new SocketInfo(newIp,newPort);
                leftSide = new SocketInfo(newIp,newPort);

                System.out.println("Connecting to RightSide - info: "+ rightSide.toString());

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

            //When there is more than one node in network.
            else
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
        if (underConstruction) underConstruction = false;
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
                sendMessage(leftSide.getSocket(), reconstructMessage);
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
            //Send echo-message return
            try {sendMessage(leftSide.getSocket(), new EchoMessage(true,ownSocket.getPort()));}
            catch (IOException e){e.printStackTrace();}
        }
        else
        {
            //Receive echo-message - Stop echo
            echo.interrupt();
            echo = null;
            //Initiate new one echo-thread
            initiateNewEcho();
        }
    }

    /**
     * Handles a ResourceMessage holding several resources by adding them to the node's referenced resources.
     * @param resourceMessage HashMap with several resources
     */
    public void handleResourceMessage(ResourceMessage resourceMessage)
    {
        Map<Integer, String> moreRefs = resourceMessage.getStoredResource();
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

        if (putMessage.isSentFromPut()) // Resource is put inside ownResources if
        {
            ownResources.put(key, resource);


            if (!rightSide.getIp().equals("")) // Send reference to right node socket if it exists
            {
                try
                {
                    PutMessage newPutMessage = new PutMessage(key,resource,false);
                    sendMessage(rightSide.getSocket(),newPutMessage);
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

            String message;
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
                sendMessage(rightSide.getSocket(), new GetMessage(key, ip, port));
            }
        }
        catch (IOException e) {e.printStackTrace();}
    }



    //Echo Methods

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
            sendMessage(rightSide.getSocket(), new EchoMessage(false, ownSocket.getPort()));
            Thread.sleep(60000);
            throw new DeadException();
        }
        catch (IOException e)
        {
            System.out.println("An IOException occurred : ALERT...");
            System.out.println("Reconstructing");
            reconstruct();
        }
        catch (InterruptedException e) {
            System.out.println("Neighbour is alive "+rightSide.toString()+"\n");
        }
        catch (DeadException e) {
            System.out.println(e.getMessage());
            System.out.println("Reconstructing");
            reconstruct();
        }
    }




    /**
     * To create a new node write this into terminal:
     * - First node in system
     *  Java Node ServerSocketPort
     *  eg:
     *  Java Node 66
     *
     * - Not first node in system
     * Java Node ServerSocketPort ExistingNodeIP ExistingNodePort
     * eg:
     * Java Node 66 55 127.0.0.1
     *
     *
     * @param args
     */
    public static void main(String[] args)
    {
        String input= UserInput.askUser("Welcome." +
                "\n To create a new network write 'new' without the quotes" +
                "\n To join an existing network please enter an existing node's IP and press enter.");


        if (input.equals("new")) //For the first port only
        {
            try
            {
                int port = Integer.parseInt(UserInput.askUser("Please enter Port for the server socket"));
                Node node = new Node(port);
            }
            catch (NumberFormatException e){
                System.out.println("Please enter a valid port...\nExiting.");
            };
        }
        else //For all other node there after.
        {
            try {
                int otherPort = Integer.parseInt(UserInput.askUser("Please enter Port of existing node"));
                int ownPort = Integer.parseInt(UserInput.askUser("Please enter Port for the server socket"));
                Node node = new Node(ownPort,otherPort,input);
            }
            catch (NumberFormatException e){
                System.out.println("Please enter a valid port.\nExiting.");
            }

        }
    }
}