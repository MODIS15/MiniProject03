import Messages.GetMessage;
import Messages.PutMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * The GetClient is used to retrieve resources (message, key) from a Node. It takes the IP/port of a Node and an integer key as arguments.
 * The GetClient then submits a GET(key, ip2, port2) message to the indicated Node. Then it listen on an ip2/port2 for a PUT(key, value) message.
 * If the PUT message arrives, the Node network has stored the association (key, value), thus some PutClient previously issued that PUT message.
 */
public class GetClient {

    private ServerSocket incomingFoundResourceSocket;

    private int ownPort;

    public GetClient(int port) throws IOException
    {
        ownPort = port;
        incomingFoundResourceSocket = new ServerSocket(port);
        initialize();
    }

    /**
     * Initializes a thread used to listen for incoming resources from a server socket.
     * The listener (server socket) starts listening on the main thread for a get message input from the console.
     */
    private void initialize()
    {
        Runnable listenForMessage = this::listenForIncomingResources; // Indicates that method is runnable for this class
        Thread thread = new Thread(listenForMessage);
        thread.start();
        System.out.println("Use the following syntax for creating a getResource: ");
        System.out.println("\"getmessage\" key ip port");

        while(true)
        {
            String request = System.console().readLine().toLowerCase().trim();
            if(isValid(request))
            {
                getResource(request);
            }
        }
    }

    /**
     * Sends a GetMessage to a Node with specified resource when using the "get message" command from console.
     * @param request: user input from console used to create a GetMessage for a specific resource.
     */
    private void getResource(String request)
    {
        try
        {
            String[] splitRequest = request.split(" ");

            int key = Integer.parseInt(splitRequest[0]);
            String ip = splitRequest[1];
            int port = Integer.parseInt(splitRequest[2]);

            // GetClient passes its own ip to nodes holding resources to be sent back
            String localhost = incomingFoundResourceSocket.getInetAddress().getLocalHost().toString();
            int index  = localhost.indexOf("/");
            localhost = localhost.substring(index+1,localhost.length());

            GetMessage message = new GetMessage(key, localhost, ownPort);

            sendSerializedMessage(ip, port, message);
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
            System.out.println("The host could not be found");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println("An IOException occurred when creating the socket");
        }
    }

    /**
     * Listens for input using a server socket, incomingFoundResourceSocket.
     * Incoming messages are deserialized from ObjectInputStream in deserializeIncomingMessage().
     * The content is then validated and handled as a Put message in handleIncomingResource().
     */
    private void listenForIncomingResources()
    {
        try
        {
            while (true)
            {
                Socket socket = incomingFoundResourceSocket.accept();
                deserializeIncomingMessage(socket);
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            System.out.println("An IOException occurred when creating the socket.");
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            System.out.println("The class of the serialized Object from ObjectInputStream could not be determined");
        }
    }

    /**
     * Deserializes the ObjectInputStream received from listening to a socket.
     * The content is then validated as a PutMessage in handleIncomingResource().
     * @param socket holding ObjectInputStream with message content
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void deserializeIncomingMessage(Socket socket) throws IOException, ClassNotFoundException
    {
        ObjectInputStream input = new ObjectInputStream(socket.getInputStream()); // Deserialize incoming Put message
        if(input.readObject() != null)
        {
            Object object = input.readObject();
            handleIncomingResource(object);
        }
        socket.close();
    }

    /**
     * Validates message content deserialized in listenForIncomingResources() and checks whether it is a Put message.
     * @param object message to check.
     */
    private void handleIncomingResource(Object object)
    {
        String message = "";
        if(object.getClass().isInstance(message))
        {
            int key = ((PutMessage) object).getKey();
            message = ((PutMessage) object).getResource();
            System.out.println("Received Put message");
            System.out.println("Key: " + key);
            System.out.println("Message: " + message);
        }
        else System.out.println("The requested message could not be displayed");
    }

    /**
     * Validates the syntax of a get message command request.
     * @param input: get request from user input in console
     * @return true if get message is correct
     */
    private boolean isValid(String input)
    {
        //Pattern structure: getmessage "key" "ip" "port"
        Pattern pattern = Pattern.compile("(getmessage [0-9]* [\\w.]* [0-9]*)");
        return pattern.matcher(input).matches();

    }

    /**
     * Serializes a message by writing it to an ObjectOutputStream and sends it to a Node.
     * @param ip of node with requested resource.
     * @param port of node with requested resource.
     * @param message GetMessage sent to node with requested resource.
     * @throws IOException
     */
    private void sendSerializedMessage(String ip, int port, GetMessage message) throws IOException
    {
        Socket socket = new Socket(ip, port);
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        output.writeObject(message);
        output.close();
    }

    public static void main(String[] args)
    {
        try
        {
            GetClient getClient = new GetClient(Integer.parseInt(args[0]));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println("An IOException occurred when creating the socket for the GetClient.");
        }
    }

}