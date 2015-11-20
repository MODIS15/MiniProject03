import Messages.GetMessage;
import Messages.Message;
import Messages.PutMessage;
import NodeUtils.UserInput;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * The GetClient is used to retrieve resources (message, key) from a Node. It takes the IP/port of a Node and an integer key as arguments.
 * The GetClient then submits a GET(key, ip2, port2) message to the indicated Node. Then it listen on an ip2/port2 for a PUT(key, value) message.
 * If the PUT message arrives, the Node network has stored the association (key, value), thus some PutClient previously issued that PUT message.
 */
public class GetClient {

    private ServerSocket incomingFoundResourceSocket;
    Thread listenThread;

    private int ownPort;

    public GetClient() throws IOException
    {
        ownPort = Integer.parseInt(UserInput.askUser("Please enter port for incoming messages"));
        incomingFoundResourceSocket = new ServerSocket(ownPort);
        initialize();
    }

    /**
     * Initializes a thread used to listen for incoming resources from a server socket.
     * The listener (server socket) starts listening on the main thread for a get message input from the console.
     */
    private void initialize()
    {
        System.out.println("Listening...");
        Runnable listenForMessage = this::listenForIncomingResources; // Indicates that method is runnable for this class
        listenThread = new Thread(listenForMessage);
        listenThread.start();
        System.out.println("Welcome to GetClient");


        while(true)
        {
            getResource();
            System.out.println("Done.");
        }
    }

    /**
     * Sends a GetMessage to a Node with specified resource when using the "get message" command from console.
     */
    private void getResource()
    {
        try
        {
            int key =   Integer.parseInt(UserInput.askUser("Please enter key of the requested resource:"));
            String ip = UserInput.askUser("Please enter IP of an existing node: ");
            int port =  Integer.parseInt(UserInput.askUser("Please enter port of an existing node:"));

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
        catch (NumberFormatException e){
            System.out.println("key or port may only be digits.. Retrying.");
            getResource();
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
                System.out.println("Received connection from - IP:"+ socket.getInetAddress() + " Port " + socket.getPort());
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

        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        Object object = inputStream.readObject();  // Deserialize incoming Put message
        handleIncomingResource(object);

        socket.close();
    }

    /**
     * Validates message content deserialized in listenForIncomingResources() and checks whether it is a Put message.
     * @param object message to check.
     */
    private void handleIncomingResource(Object object)
    {
        String message = "";
        if(object instanceof Message)
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
        output.flush();
        output.close();
        System.out.println("Sending get request.");


    }

    public static void main(String[] args)
    {
        try
        {
            GetClient getClient = new GetClient();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println("An IOException occurred when creating the socket for the GetClient.");
        }
    }

}