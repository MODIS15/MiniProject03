import Messages.GetMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public class GetClient {

    private ServerSocket incomingFoundResourceSocket;

    /**
     * Constructor for GetClient
     * @param port
     * @throws IOException
     */
    public GetClient(int port) throws IOException {
        incomingFoundResourceSocket = new ServerSocket(port);
        initialize();
    }

    /**
     * Initializes thread for listening for incoming resources.
     * Starts listening on the main thread for get message input from the console.
     */
    private void initialize(){
        Runnable incoming = this::listenForIncomingResources;
        Thread incomingTread = new Thread(incoming);
        incomingTread.start();
        System.out.println("Use the following syntax for creating a getMessage:");
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
     * Listens for a console input starting with "get message",
     * when such an input is received, getResources() sends a GetMessage to a Node for the specified resource
     * @param request: User input from console to create a GetMessage for a specific file
     */
    private void getResource(String request)
    {
        while(true)
        {
            try
            {
                    String[] splitRequest = request.split(" ");

                    int key = Integer.parseInt(splitRequest[2]);
                    String ip = splitRequest[3];
                    int port = Integer.parseInt(splitRequest[4]);

                    GetMessage message = new GetMessage(key, ip, port);

                    Socket s = new Socket(ip, port);
                    ObjectOutputStream output = new ObjectOutputStream(s.getOutputStream());
                    output.writeObject(message);

                    output.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                System.out.println("The host could not be found");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("An IOException occurred when creating the socket");
            }
        }
    }

    /**
     * Validates a getmessage request String.
     * @param input: User get request from console
     * @return
     */
    private boolean isValid(String input)
    {
        //Pattern structure: getmessage "key" "ip" "port"
        Pattern pattern = Pattern.compile("(getmessage [0-9]* [\\w.]* [0-9]*)");
        return pattern.matcher(input).matches();

    }

    /**
     * Listens for inputs from ObjectInputStream using Socket incomingFoundResourceSocket.
     */
    private void listenForIncomingResources()
    {
        while(true)
        {
            try
            {
                Socket socket = incomingFoundResourceSocket.accept();
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

                if(input.readObject() != null)
                {
                    Object object = input.readObject();

                    handleIncomingResource(object);
                }

                socket.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
                System.out.println("An IOException occurred when creating the socket");
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
                System.out.println("The class of the serialized Object from ObjectInputStream could not be determined");
            }
        }
    }

    /**
     * Handles received input from listenForIncomingResources().
     * Only handles inputs which are instance of String.
     * Checks whether or not the received Object is of type String.
     * If Object is of type String, the method prints the Object.toString() to the console.
     * @param object
     */
    private void handleIncomingResource(Object object){

        String message = "";
        if(object.getClass().isInstance(message))
        {
            System.out.println();
            message = object.toString();
            System.out.println(message);
        }
        else
            System.out.println("The requested message could not be displayed");
    }

    /**
     * Main method
     * @param args
     */
    public static void main(String[] args) {
        try
        {
            int port = Integer.parseInt(System.console().readLine());
            GetClient get = new GetClient(port);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println("An IOException occurred when creating the socket for the GetClient");
        }
    }
}
