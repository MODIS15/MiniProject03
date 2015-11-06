import Messages.ConnectMessage;
import Messages.GetMessage;
import Messages.PutMessage;
import Messages.UtilityMessage;

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
    private ServerSocket getInputSocket;
    private ServerSocket putInputSocket;
    private ServerSocket neighbourInputSocket;
    private int rightNeighbourCapacity;
    private int leftNeighbourCapacity;


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
     * @param port of a node in a existing p2p system
     */
    public Node(String ip, int port) {
        try {
            rewireLeftSocket(ip, port);
            initialize();
        } catch (NumberFormatException e) {
            System.out.println("Please enter valid IP and port of a node in the system.\n Exiting...");
            System.exit(0);
        }
    }

    private void initialize() {
        resources = new HashMap<Integer, String>();
        try {



            Runnable runnableNeighbour = this::listenForNewNeighbour;
            Runnable runnableGet = this::listenForGet;
            Runnable runnablePut = this::listenForPut;
            Runnable runnableRight = this::listenRightSocket;
            Runnable runnableLeft = this::listenLeftSocket;
            Thread listenGet = new Thread(runnableGet);
            Thread listenPut = new Thread(runnablePut);
            Thread listenNeighbours = new Thread(runnableNeighbour);
            Thread listenRight = new Thread(runnableRight);
            Thread listenLeft = new Thread(runnableLeft);


            listenGet.start();
            listenPut.start();
            listenNeighbours.start();
            listenRight.start();
            listenLeft.start();

            if(leftSocket == null && rightSocket == null){
                leftNeighbourCapacity=0;
                rightNeighbourCapacity=0;
            }
            else requestCapacity();

        } catch (NumberFormatException e) {
            System.out.println("Invalid Port");
            initialize();
        }


    }

    //Listeners

    /**
     * Listens for any incoming connection from put-clients.
     *
     * @throws NumberFormatException
     */
    private void listenForPut() throws NumberFormatException {
        System.out.println("Input port for putPort:");
        int port = Integer.parseInt(System.console().readLine().trim());
        try {
            putInputSocket = new ServerSocket(port);

            while (true) {
                System.out.println("Waiting for connection from new put-client...");
                Socket s = putInputSocket.accept();
                System.out.println("Connection from Put-client: " + s.getInetAddress() + " was established.");
                ObjectInputStream input = new ObjectInputStream(s.getInputStream());

                handlePutInput((PutMessage) input.readObject(), s);

                s.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * Listens for any incoming connection from get-clients.
     *
     * @throws NumberFormatException
     */
    private void listenForGet() throws NumberFormatException {
        System.out.println("Input port for getPort:");
        int port = Integer.parseInt(System.console().readLine().trim());
        try {
            getInputSocket = new ServerSocket(port);
            while (true) {
                System.out.println("Waiting for connection from new get-client...");
                Socket s = getInputSocket.accept();
                System.out.println("Connection from Get-client: " + s.getInetAddress() + " was established.");
                ObjectInputStream input = new ObjectInputStream(s.getInputStream());

                handleGetInput((GetMessage) input.readObject(), s);

                s.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Listens for any incoming connection from new nodes.
     *
     * @throws NumberFormatException
     */
    private void listenForNewNeighbour() throws NumberFormatException {

        System.out.println("Input port for NodesPort:");
        int neighbourInputPort = Integer.parseInt(System.console().readLine().trim());

        try {
            System.out.println("Waiting for connection from new node...");
            neighbourInputSocket = new ServerSocket(neighbourInputPort);

            while (true) {
                Socket s = neighbourInputSocket.accept();
                System.out.println("Connection from new node: " + s.getInetAddress() + " was established.");

                saveNeighbourNode(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Listens for any incoming messages from right socket
     */
    private void listenRightSocket() {
        while (true) {
            if (rightSocket != null) {
                //TODO listen for any capacity updates
            }
        }
    }

    /**
     * Listens for any incoming messages from right socket
     */
    private void listenLeftSocket() {
        try {

            while (true) {
                System.out.println("Waiting for connection from new node...");
                if (leftSocket != null) {
                    ObjectInputStream input = new ObjectInputStream(leftSocket.getInputStream());
                    Object object = input.readObject();

                    if (object instanceof ConnectMessage) {
                        System.out.println("Received connect message from " + leftSocket.getInetAddress());
                        String ip = ((ConnectMessage) object).getIpAddress();
                        int port = ((ConnectMessage) object).getPort();
                        rewireLeftSocket(ip, port);
                    } else if (object instanceof UtilityMessage) {
                        if (((UtilityMessage) object).getIsDisconnect()) {
                            System.out.println("Received disconnect message from " + leftSocket.getInetAddress());
                            disconnectLeftSocket();
                        }
                    } else if (object instanceof PutMessage) {
                        System.out.println("Received propagated message from " + leftSocket.getInetAddress());
                        handlePutInput((PutMessage) object, rightSocket);
                    } else if (object instanceof GetMessage) {
                        System.out.println("Received get propagated request from " + leftSocket.getInetAddress());
                        handleGetInput((GetMessage) object, rightSocket);
                    }
                    //TODO ADD HANDLING FOR CAPACITY UPDATE
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    //Connection methods

    /**
     * Reconnect Leftsocket with a new node
     *
     * @param ip   of new node
     * @param port of new node
     */
    private void rewireLeftSocket(String ip, int port) {
        try {
            disconnectLeftSocket();
            leftSocket = new Socket(ip, port);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disconnect connection on left node.
     */
    private void disconnectLeftSocket() {
        try {
            if (leftSocket != null) {
                leftSocket.close();
                leftSocket = null;
                System.out.println("Disconnected");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saving new incoming nodes. If left and right sockets are occupied, rewire connecten.
     *
     * @param node socket of incoming node
     */
    private void saveNeighbourNode(Socket node) {
        if (leftSocket == null) {
            leftSocket = node;
            System.out.println("Left socket connected to " + node.getInetAddress());

        } else if (rightSocket == null) {
            rightSocket = node;
            System.out.println("Right socket connected to " + node.getInetAddress());
        } else {
            try {
                System.out.println("Sockets full.\nRMerging and rewiring incoming node...");
                ConnectMessage connectMessage = new ConnectMessage(rightSocket.getInetAddress().toString(), rightSocket.getPort());
                sendDisconnectMessage(rightSocket);

                if (!rightSocket.isClosed()) rightSocket.close();
                rightSocket = node;
                sendConnectMessage(connectMessage, node);
                System.out.println("Rewiring done.");

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Sends a disconnect message to neighbour node.
     * Used for rewiring
     *
     * @param node socket to node
     * @throws IOException
     */
    private void sendDisconnectMessage(Socket node) throws IOException {
        UtilityMessage message = new UtilityMessage();
        message.setDisconnect(true);
        ObjectOutputStream output = new ObjectOutputStream(node.getOutputStream());

        output.writeObject(message);
    }

    /**
     * Send ConnectMessage containing information of another node.
     * Used for rewiring
     *
     * @param connectMessage message with node infoes
     * @param node           socket of a node that is to receive the message.
     * @throws IOException
     */
    private void sendConnectMessage(ConnectMessage connectMessage, Socket node) throws IOException {
        ObjectOutputStream clientOutputStream = new ObjectOutputStream(node.getOutputStream());
        clientOutputStream.writeObject(connectMessage);
    }

    /**
     * Propagte a given message to the right neighbour.
     *
     * @param message
     */
    private void propagateMessage(Object message, Socket socket) {
        try {
            System.out.println("Propagating resource");
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Used to notify neighbours of current capacity of this node.
     */
    private void notifyCapacity(){
        try{
            ObjectOutputStream right = new ObjectOutputStream(rightSocket.getOutputStream());
            ObjectOutputStream left = new ObjectOutputStream(leftSocket.getOutputStream());

            right.write(resources.size());
            left.write(resources.size());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestCapacity(){
        //TODO NEED IMPLEMENTATION. EG. SEND REQUEST AND RECEIVE MESSAGE CONTAINING CAPACITY
        try
        {
            if (rightSocket == null) {
                ObjectOutputStream outLeft = new ObjectOutputStream(leftSocket.getOutputStream());
                ObjectInputStream inLeft = new ObjectInputStream(leftSocket.getInputStream());

            }
            if (leftSocket == null) {
                ObjectOutputStream outRight = new ObjectOutputStream(rightSocket.getOutputStream());
                ObjectInputStream inRight = new ObjectInputStream(rightSocket.getInputStream());



            } else {
                ObjectOutputStream outLeft = new ObjectOutputStream(leftSocket.getOutputStream());
                ObjectInputStream inLeft = new ObjectInputStream(leftSocket.getInputStream());

                ObjectOutputStream outRight = new ObjectOutputStream(rightSocket.getOutputStream());
                ObjectInputStream inRight = new ObjectInputStream(rightSocket.getInputStream());




            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    //Put methods

    /**
     * Handle putmessage by either storing its resource or propagating it
     *
     * @param message
     * @param socket
     */
    private void handlePutInput(PutMessage message, Socket socket) {
        System.out.println("Handling incoming ressource...");
        if (isKeyAvailable(message)) {
            saveResource(message);
            notifyCapacity();
        }
        else propagateMessage(message,socket);
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

    /**
     * Check if key exists in node
     *
     * @param message
     * @return
     */
    private boolean isKeyAvailable(PutMessage message) {
        return !resources.containsKey(message.getKey());
    }

    //Get methods

    /**
     * Handle getMessage by checking if requested resource is in this node. Else propagate.
     *
     * @param getMessage
     */
    private void handleGetInput(GetMessage getMessage, Socket socket) {
        if (resources.containsKey(getMessage.getKey())) sendResourceToGet(getMessage);
        else {
            System.out.println("resource not found. Request propagated");
            propagateMessage(getMessage, socket);
        }
    }

    /**
     * Sends a resource in node to Get-client
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

    public void main(String[] args) {
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
}
