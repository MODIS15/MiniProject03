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
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jakob_P_Holm on 09/11/2015.
 */
public class StringNode {

    private Socket leftSocket;
    private Socket rightSocket;

    private int port;

    private Map<Integer, String> resources;

    public StringNode (int portNumber)
    {
        instantiate(portNumber);
        System.out.println(portNumber);
        //Run
        run();
    }

    public StringNode (int ownPortNumber, int connectToPortNumber, String ip)
    {
        //Sets up the inputSerSocket
        instantiate(ownPortNumber);
        //For startes then the rightSide belongs to the given note.
        try {
            Socket startSocket = new Socket(ip,connectToPortNumber);
            sendConnectFrom(new ConnectMessage("from", rightSocket.getInetAddress().toString(), connectToPortNumber), startSocket);
        }
        catch (IOException e){System.out.println(e.getStackTrace());}
        //Run
        run();
    }

    private void sendConnectFrom(ConnectMessage connectMessage, Socket node) throws IOException
    {
        ObjectOutputStream clientOutputStream = new ObjectOutputStream(node.getOutputStream());
        clientOutputStream.writeObject(connectMessage);
    }

    private void sendConnectTo(ConnectMessage connectMessage) throws IOException
    {
        ObjectOutputStream clientOutputStream = new ObjectOutputStream(leftSocket.getOutputStream());
        clientOutputStream.writeObject(connectMessage);
    }

    private void instantiate (int portNumber)
    {
        resources = new HashMap<Integer, String>();
        /*
        try
        {
            //Creates the inputServerSocket
            port = portNumber;
            rightSocket = new Socket();
            //Runnable runnableNeighbour = this::listenForNewNeightBour;
            //new Thread(runnableNeighbour).start();
        }
        catch (IOException e){System.out.println(e.getStackTrace());}
        */
    }

    public void run()
    {
        try
        {
            while (true)
            {
                ObjectInputStream input = new ObjectInputStream(leftSocket.getInputStream());
                Object object = input.readObject();

                if (object instanceof ConnectMessage) {
                    System.out.println("Received connect message from " + leftSocket.getInetAddress());
                    String toAndFrom = ((ConnectMessage) object).getToAndFrom();
                    String ip = ((ConnectMessage) object).getIpAddress();
                    int port = ((ConnectMessage) object).getPort();
                    if (toAndFrom.equals("from")){
                        createNewNode(new Socket(ip,port));
                    }
                    else
                    {
                        rightSocket = new Socket(ip,port);
                    }
                }
            }
        }
        catch (IOException e){e.printStackTrace();}
        catch (ClassNotFoundException e){e.printStackTrace();}
    }

    public void createNewNode (Socket newNode)
    {
        try {
        if (leftSocket == null)
        {
            leftSocket = newNode;
            System.out.println("LeftSocket has been connected to port: " + port + " from: " + leftSocket.getPort());
            //Send connectFrom
            //sendConnectFrom(new ConnectMessage("to", inputServerSocket.getInetAddress().toString(),port),leftSocket);
        }
        else
        {
           sendConnectTo(new ConnectMessage("from", newNode.getInetAddress().toString(), newNode.getPort()));
        }
        }
        catch (IOException e){e.printStackTrace();}
    }

    /*
    private void listenLeftSocket()
    {

        try
        {

            while (true)
            {
                ObjectInputStream input = new ObjectInputStream(leftSocket.getInputStream());
                Object object = input.readObject();


                //STUFF

            }

        }
        catch (Exception e){System.out.println(e.getStackTrace());}


    }
    */


    public static void main(String[] args)
    {

        try {
            if (args.length == 1) {
                System.out.println(args[0]);
                StringNode stringNode = new StringNode(Integer.parseInt(args[0])); // Node this is not connected to any existing p2p-system
            }
            else
            {
                StringNode stringNode = new StringNode(Integer.parseInt(args[0]), Integer.parseInt(args[1]), "localhost"); //Connect node to existing system
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter valid Port number.\nExiting...");
        }

    }

}
