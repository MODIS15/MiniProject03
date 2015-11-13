import Messages.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class Node {

    private Socket leftSocket;
    private Socket rightSocket;
    private ServerSocket serverSocket;
    private Map<Integer, String> resources;




    /**
     * Constructor used when creating node in non existing system
     */
    public Node() {
        initialize();
    }

    /**
     * Constructor used to connect node in existing system
     *
     * @param ip   of node in a existing p2p system
     * @param connectport of a node in a existing p2p system
     */
    public Node(String ip, int connectport) {
        try {
            initialize();
            connectToExistingNode(ip, connectport);
        } catch (NumberFormatException e) {
            System.out.println("Please enter valid IP and port of a node in the system.\n Exiting...");
            System.exit(0);
        }
    }

    public static void main(String[] args) {

        try {
            if (args.length == 0) {
                Node node = new Node(); // Node this is not connected to any existing p2p-system
            } else {
                Node node = new Node(args[0], Integer.parseInt(args[1])); //Connect node to existing system
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter valid Port number.\nExiting...");
        }
    }




    private void initialize() {


        try {

            Runnable runnableServerSocket = this::listenServerSocket;
            Runnable runnableRight = this::listenRightSocket;
            Thread listenServerSocket = new Thread(runnableServerSocket);
            Thread listenRight = new Thread(runnableRight);
            resources = new HashMap<Integer, String>();

            System.out.println("Input port for incomming connections:");
            int port;
            port = Integer.parseInt(System.console().readLine().trim());
            serverSocket = new ServerSocket(port);
            listenServerSocket.start();
            listenRight.start();
            listenLeftSocket();
        }
        catch (IOException e){System.out.println(e.getStackTrace());}
        catch (NumberFormatException e) {
            System.out.println("Invalid Port");
            initialize();
        }
    }

    //Listeners

    /**
     * Listens for any incoming connection from new nodes.
     *
     * @throws NumberFormatException
     */
    private void listenServerSocket() throws NumberFormatException {
        try {
            System.out.println("Waiting for connection from new node...");

            while (true) {
                Socket s = serverSocket.accept();
                System.out.println("Connection from: " + s.getInetAddress() + " was established.");

                Message message = readMessageFromInputStream(s);
                if(message == null) continue;
                handleMessage(message,rightSocket);
            }
        }
        catch (IOException e) {e.printStackTrace();}
        catch (ClassNotFoundException e) {e.printStackTrace();}
    }

    /**
     * Listens for any incoming messages from right socket
     */
    private void listenRightSocket() {
        try
        {
            System.out.println("listening on right socket");
            listen(rightSocket,leftSocket);
        }
        catch (IOException e) {e.printStackTrace();}
        catch (ClassNotFoundException e) {e.printStackTrace();}
    }

    /**
     * Listens for any incoming messages from right socket
     */
    private void listenLeftSocket() {
        try
        {
            System.out.println("listening on left socket");
            listen(leftSocket,rightSocket);
        }
        catch (IOException e) {e.printStackTrace();}
        catch (ClassNotFoundException e) {e.printStackTrace();}
    }

    private void listen(Socket thisSocket, Socket toSocket) throws IOException, ClassNotFoundException {
        while (true) {
            if (thisSocket != null) {
                Message message = readMessageFromInputStream(thisSocket);
                if(message == null) continue;
                handleMessage(message, toSocket);
            }
        }
    }


    //Connection methods

    private void handleMessage(Message message, Socket toSocket) throws IOException {
        switch (message.getMessageType()) {
            case ConnectMessage:
                System.out.println("Received connect message from ");
                handleConnectMessage((ConnectMessage)message);
                break;

            case DisconnectMessage:
                if (((DisconnectMessage) message).getIsDisconnect()) {
                    System.out.println("Received disconnect message");
                    disconnectSocket(toSocket);
                }
                break;

            case PutMessage:
                System.out.println("Received propagated message");
                handlePutInput((PutMessage) message, toSocket);
                break;

            case GetMessage:
                System.out.println("Received get propagated request from ");
                handleGetInput((GetMessage) message, toSocket);
                break;

            case CapacityMessage:
                if (!((CapacityMessage) message).isSet()) {
                    returnCapacityRequest(toSocket, (CapacityMessage) message);
                }
                break;
            default:
                System.out.println("Message Type not recognized...");
                break;
        }
    }

    /**
     * Connect to existing node in network
     * @param ip   of new node
     * @param port of new node
     */
    private void connectToExistingNode(String ip, int port) {
        try {
            leftSocket = new Socket(ip, port);
            sendMessage(new ConnectMessage(rightSocket.getInetAddress().toString(),rightSocket.getPort()),leftSocket);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disconnect connection on a socket.
     */
    private void disconnectSocket(Socket socket) {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
                System.out.println("Disconnected");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


     /**
     * Propagate a given message to a neighbour.
     *
     * @param message
     */
    private void sendMessage(Object message, Socket socket) throws IOException {
            System.out.println("Propagating resource");
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(message);
    }

    private void requestCapacity(Socket socket){
        try
        {
            System.out.println("Request Capacity not implemented");
            sendMessage(new CapacityMessage(),socket);
        }
        catch (IOException e) {e.printStackTrace();}
    }

    private void handleConnectMessage(ConnectMessage message) throws IOException {
        if(!message.isNewJoin())
        {
            if(!rightSocket.isClosed())
                rightSocket.close();

            rightSocket = null;
            rightSocket = new Socket(message.getIpAddress(), message.getPort());
        }
        if(leftSocket==null){
            leftSocket = new Socket(message.getIpAddress(),message.getPort());
        }
        else
        {
            message.setIsNewJoin(false);
            sendMessage(message,leftSocket);
            leftSocket.close();
        }
    }

    private Message readMessageFromInputStream(Socket s) throws IOException, ClassNotFoundException {
        if(s==null) return null;
        ObjectInputStream inputStream = new ObjectInputStream(s.getInputStream());
        Object object = inputStream.readObject();

        if(object instanceof Message) return (Message)object;
        else return null;
    }


    //PutClient methods

    private void returnCapacityRequest(Socket s, CapacityMessage message) {
        try {
            message.setCapacity(resources.size());
            ObjectOutputStream output = new ObjectOutputStream(s.getOutputStream());
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle putmessage by either storing its resource or propagating it
     *
     * @param message
     * @param socket
     */
    private void handlePutInput(PutMessage message, Socket socket) {

        System.out.println("Handling incoming resource...");
        try {
            if (resources.containsKey(message.getKey())) sendMessage(message,rightSocket);
            resources.put(message.getKey(),message.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //TODO MAKE CAPACITY CHECK WORK
//        if(rightSocket != null && leftSocket != null){
//            int rightCapacity = requestCapacity(rightSocket);
//            int leftCapacity = requestCapacity(leftSocket);
//            int lowestCapacity;
//            Socket lowestNeighbour;
//
//
//            if(rightCapacity <= leftCapacity){
//                lowestNeighbour = rightSocket;
//                lowestCapacity = rightCapacity;
//            }
//            else {
//                lowestNeighbour = leftSocket;
//                lowestCapacity = leftCapacity;
//            }
//
//            if(resources.size() <= lowestCapacity){
//
//                if (isKeyAvailable(message)) {
//                    saveResource(message);
//                    System.out.println("Stored resource");
//                } else sendMessage(message, lowestNeighbour);
//            }
//            else sendMessage(message, lowestNeighbour);
//        }
    }

    /**
     * Stores resource into map
     *
     * @param message
     */
    private void saveResource(PutMessage message) {
        resources.put(message.getKey(), message.getMessage());
        System.out.println("Ressource stored.");
    }


    //GetClient methods

    /**
     * Check if key exists in node
     *
     * @param message
     * @return
     */
    private boolean isKeyAvailable(PutMessage message) {
        return !resources.containsKey(message.getKey());
    }

    /**
     * Handle getMessage by checking if requested resource is in this node. Else propagate.
     *
     * @param getMessage
     */
    private void handleGetInput(GetMessage getMessage, Socket socket) {
        try
        {
            if (resources.containsKey(getMessage.getKey())) sendResourceToGet(getMessage);
            else {
                System.out.println("resource not found. Request propagated");
                    sendMessage(getMessage, socket);
            }
        } catch (IOException e) {e.printStackTrace();}
    }

    /**
     * Sends a resource in node to GetClient-client
     *
     * @param message GetMessage
     */
    private void sendResourceToGet(GetMessage message) {
        try {

            System.out.println("Sending resources to: " + message.getIp());
            String resource = resources.get(message.getKey());
            Socket getSocket = new Socket(message.getIp(), message.getPort());
            ObjectOutputStream outputStream = new ObjectOutputStream(getSocket.getOutputStream());
            outputStream.writeUTF(resource);
            outputStream.close();

            System.out.println("Sent");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
