import Messages.PutMessage;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Put {

    public Put() {
        sendMessage();
    }

    public static void main(String[] args) {
        Put put = new Put();
    }

    /**
     * Insert resource into existing p2p system
     */
    private void sendMessage() {
        while (true) {
            try {
                System.out.println("Enter ip for node: ");
                String ip = System.console().readLine();
                System.out.println("Enter port for node: ");
                String port = System.console().readLine();

                System.out.println("Enter message to put: ");
                String input = System.console().readLine();
                int key = input.hashCode();
                System.out.println("Key of resource: "+key);
                PutMessage message = new PutMessage(key, input);

                Socket s = new Socket(ip, Integer.parseInt(port));
                ObjectOutputStream output = new ObjectOutputStream(s.getOutputStream());
                output.writeObject(message);

                output.close();

                System.out.println("Message Putted\n" +
                        "///////////Resetting///////////");


            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}

