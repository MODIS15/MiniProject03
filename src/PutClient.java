import Messages.PutMessage;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public class PutClient {

    public PutClient() {
        while (true)
        {
            sendMessage();
        }
    }

    public static void main(String[] args) {
        PutClient put = new PutClient();
    }

    /**
     * Insert resource into existing p2p system
     */
    private void sendMessage() {

            try {
                System.out.println("Enter ip for node: ");
                String ip = System.console().readLine();
                System.out.println("Enter port for node: ");
                String port = System.console().readLine();

                System.out.println("Enter message to put: ");
                String input = System.console().readLine();

                System.out.println("Enter key of resource");
                int key = Integer.parseInt(System.console().readLine());
                PutMessage message = new PutMessage(key, input);
                System.out.println();

                Socket s = new Socket(ip, Integer.parseInt(port));
                ObjectOutputStream output = new ObjectOutputStream(s.getOutputStream());
                output.writeObject(message);

                output.close();

                System.out.println("Message Putted\n" +
                        "///////////Resetting///////////");
                System.out.println();


            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}

