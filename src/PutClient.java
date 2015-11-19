import Messages.PutMessage;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/*
The PutClient is used to send resources (message, key) to a Node.
It takes as arguments the IP/port of a Node, an integer key and a string value (user input from terminal).
The client then submits a PUT(key, value) message to the indicated node and terminates.
 */
public class PutClient {

    public PutClient() {
        while (true)
        {
            sendResourceMessage();
        }
    }

    /**
     * Insert a resource into existing the P2P network system.
     */
    private void sendResourceMessage()
    {
            try
            {
                System.out.println("Please enter a valid ip for a given node in the network: ");
                String ip = System.console().readLine();
                System.out.println("Please enter the port of the node : ");
                String port = System.console().readLine();

                System.out.println("Please enter the message to put as a resource in the network: ");
                String resourceInput = System.console().readLine();
                int resourceKey = resourceInput.hashCode();
                System.out.println("The resource key is: "+resourceKey);
                PutMessage message = new PutMessage(resourceKey, resourceInput,true);
                System.out.println();

                sendSerializedMessage(ip, Integer.parseInt(port), message);

                System.out.println("Message has been put.\n" +
                        "///////////Resetting...///////////");
                System.out.println();


            }
            catch (UnknownHostException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
    }

    /**
     * Serializes a message by writing it to an ObjectOutputStream and sends it to a Node.
     */
    private void sendSerializedMessage(String ip, int port, PutMessage message) throws IOException
    {
        Socket socket = new Socket(ip, port);
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        output.writeObject(message);
        output.close();
    }

    public static void main(String[] args)
    {
        PutClient put = new PutClient();
    }

}

