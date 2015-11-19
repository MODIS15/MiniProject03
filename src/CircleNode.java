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
 * Created by Jakob_P_Holm on 11/11/2015.
 */
public class CircleNode {

    //For all nodes
    int ownPort;
    private HashMap<Integer, String> ownResources = new HashMap<>();
    private HashMap<Integer, String> refferenceresources = new HashMap<>();
    private ServerSocket inputServerSocket;

    private String hostIp = "";

    int leftSidePort;
    String leftSideIp = "";

    int rightSidePort;
    String rightSideIp = "";

    Thread echo = null;
    boolean underConstruction = false;

    public CircleNode (int port)
    {
        ownPort = port;
        setUpServer();
        Runnable run = this::run;
        Thread thread = new Thread(run);
        thread.start();
    }

    public CircleNode (int port, int _ortherPort, String _otherIP)
    {
        ownPort = port;
        rightSideIp = _otherIP;
        rightSidePort = _ortherPort;
        setUpServer();

        //Set up connection to others.
        sendStartConnectessage();

        Runnable run = this::run;
        Thread thread = new Thread(run);
        thread.start();
    }

    public void setUpServer()
    {
        try
        {
            inputServerSocket = new ServerSocket(ownPort);
            hostIp = inputServerSocket.getInetAddress().getLocalHost().toString();
            int index  = hostIp.indexOf("/");
            hostIp = hostIp.substring(index+1,hostIp.length());
        }
        catch (IOException e){e.printStackTrace();}
    }

    public void sendStartConnectessage()
    {
        try
        {
            Socket startSocket = new Socket(rightSideIp, rightSidePort);

            String hostIP = inputServerSocket.getInetAddress().getLocalHost().toString();
            int index  = hostIP.indexOf("/");
            hostIP = hostIP.substring(index+1,hostIP.length());

            System.out.println(hostIP);
            System.out.println(inputServerSocket.getInetAddress().getLocalHost().getHostAddress());

            ConnectMessage connectMessage = new ConnectMessage("From",hostIP,ownPort);

            ObjectOutputStream clientOutputStream = new ObjectOutputStream(startSocket.getOutputStream());
            clientOutputStream.writeObject(connectMessage);
        }
        catch (IOException e){e.printStackTrace();}
    }

    private Message readMessageFromInputStream(Socket s) throws IOException, ClassNotFoundException {
        if(s==null) return null;
        ObjectInputStream inputStream = new ObjectInputStream(s.getInputStream());
        Object object = inputStream.readObject();

        if(object instanceof Message) return (Message)object;
        else return null;
    }

    public void run() {
        try {
            while (true) {
                Socket clientSocket = inputServerSocket.accept();

                Message inputMessage = readMessageFromInputStream(clientSocket);

                if (inputMessage == null) return;
                handleMessage(inputMessage);
            }
        }
        catch (IOException e){} catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void handleMessage(Message message)
    {
        switch (message.getMessageType())
        {
            case ConnectMessage: //Responsible for joining the nodes properly together.
                handleConnectMessage((ConnectMessage)message);
                System.out.println("ECHO-MESSAGE"); //Starts the echo / heartbeat
                intiateNewEcho();
                break;
            case ReconstructMessage: // Responsible for reconstructing nodes / making the circle complete again.
                handleReconstructMessage((ReconstructMessage)message);
                break;

            case EchoMessage: // Responsible for making sure that the right side are alive
                handleEchoMessage((EchoMessage) message);
                break;

            case ResourceMessage: // Responsible for sending entire hashmaps of resource to other nodes.
                handleRecourceMessage((ResourceMessage)message);
                break;

            case PutMessage: // Responsible for placing one resource in a given node.
                handlePutMessage((PutMessage)message);
                break;

            case GetMessage: // Responsible for getting a resource if it exsits in the system.
                handleGetMessage((GetMessage)message);
                break;
        }
    }

    private void sendConnectMessage(Socket socket, ConnectMessage connectMessage)
    {
        try
        {
            ObjectOutputStream clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
            clientOutputStream.writeObject(connectMessage);
        }
        catch (IOException e){e.printStackTrace();}
    }

    private void handleConnectMessage(ConnectMessage connectMessage)
    {
        //Gets the values from the sender of the message
        String toFromOrClosure = connectMessage.getToAndFrom(); //Determins how the overall message are supposed to be handeled.
        String ip = connectMessage.getIpAddress(); //The ip-id of the message
        int port = connectMessage.getPort(); //The port-key of the message.

        if (toFromOrClosure.equals("From"))// Means that a new node has entered the system. Note that the reciver of this message always sets it's left side to the sender.
        {
            handleConnectFromMessage(ip,port);
        }
        else if (toFromOrClosure.equals("To")) //Sets it's rightside to the ip and port and sends a closure message to it's new right side.
        {
            handleConnectToMessage(ip,port);
        }
        else if (toFromOrClosure.equals("Closure")) //Sets the left side the ip and port.
        {
            handleConnectClosureMessage(ip,port);
        }

        System.out.println("leftside: " + leftSidePort);
        System.out.println("rightside: " + rightSidePort);
    }

    private void handleConnectFromMessage(String ip, int port)
    {
        try {
        if (rightSideIp.equals("") && leftSideIp.equals("")) // When there only is one node
        {
            //Sets the sender's ip and port to it's right and left side
            rightSideIp = ip;   rightSidePort = port;
            leftSideIp = ip;    leftSidePort = port;

                //Sends information back that it's should put it's left side to this node.
                Socket rightSocket = new Socket(rightSideIp, rightSidePort);

                String hostIP = inputServerSocket.getInetAddress().getLocalHost().toString();
                int index  = hostIP.indexOf("/");
                hostIP = hostIP.substring(index+1,hostIP.length());

                ConnectMessage connectMessage = new ConnectMessage("Closure",hostIP,ownPort); //Tells the new node that it should set its leftside to this node

                sendConnectMessage(rightSocket, connectMessage);

                if (!ownResources.isEmpty()) // Send all information to the new node.
                {
                    sendResourceMessage(new Socket(rightSideIp,rightSidePort),
                                        new ResourceMessage(ownResources));
                }
            }
        else //When there is more than one node
        {
                Socket leftSocket;
                if (!refferenceresources.isEmpty()) // Sends all inherited references to new middle node
                {
                    leftSocket = new Socket(ip, port);
                    sendResourceMessage(leftSocket, new ResourceMessage(refferenceresources));
                    refferenceresources.clear();
                }

                //Inform the left side, that is should set it's rightside to the new node.
                leftSocket = new Socket(leftSideIp, leftSidePort);
                ConnectMessage connectMessage = new ConnectMessage("To",ip,port);
                sendConnectMessage(leftSocket, connectMessage);

                //Left side is set to the sender.
                leftSideIp = ip;
                leftSidePort = port;
            }
        }
        catch (IOException e){e.printStackTrace();}
    }

    private void handleConnectToMessage(String ip, int port)
    {
        //Sets the ip and port to current node's rightside
        rightSideIp = ip;
        rightSidePort = port;

        try
        {
            if (underConstruction) //Circle is being reconstructed.
            {
                if (!ownResources.isEmpty()) { //Send a copy of own resources to new right node so it can store them as references.
                    sendResourceMessage(new Socket(rightSideIp, rightSidePort),
                                        new ResourceMessage(ownResources));
                }
                underConstruction = false;
            }

            String hostIP = inputServerSocket.getInetAddress().getLocalHost().toString();
            int index  = hostIP.indexOf("/");
            hostIP = hostIP.substring(index+1,hostIP.length());

            //Informing the new right node that it should set its left side to this node.
            ConnectMessage connectMessage = new ConnectMessage("Closure",hostIP,ownPort);
            Socket rightSocket = new Socket(rightSideIp, rightSidePort);
            sendConnectMessage(rightSocket, connectMessage);
        }
        catch (IOException e){e.printStackTrace();}
    }
    private void handleConnectClosureMessage(String ip, int port)
    {
        //Sets it leftside to the given ip and port
        leftSideIp = ip;
        leftSidePort = port;
        if (underConstruction){underConstruction = false;} // REMOVE?
    }

    private void handleReconstructMessage(ReconstructMessage reconstructMessage)
    {
        try
        {
            String lostIP = reconstructMessage.getLostSideIp();
            int lostPort = reconstructMessage.getLostSidePort();
            String discoverIp = reconstructMessage.getDiscoverIp();
            int discoverPort = reconstructMessage.getDiscoverPort();

            if (lostPort == leftSidePort) //If the leftside equals the missing / lost port. Then it should try to reconnect with the discover.
            {
                //Sends all of it's refferences to the right, since it has inheriented all of it's references.
                if (!refferenceresources.isEmpty()) {
                    sendResourceMessage(new Socket(rightSideIp,rightSidePort), new ResourceMessage
                                    (
                                            refferenceresources
                                    )
                    );

                    String message = "";
                    for (Integer key : refferenceresources.keySet()) // Removes all of it's references. Since it's inside of ownResources.
                    {
                        message = refferenceresources.get(key);
                        ownResources.put(key, message);
                    }
                    refferenceresources.clear();
                }

                String localhost = inputServerSocket.getInetAddress().getLocalHost().toString();
                int index  = localhost.indexOf("/");
                localhost = localhost.substring(index+1,localhost.length());

                //Inform the new left side, that it should connect its right side with the current / this node.
                Socket newLeftSideSocket = new Socket(discoverIp, discoverPort);
                sendConnectMessage(newLeftSideSocket, new ConnectMessage("To", localhost, ownPort));
                System.out.println("Tries to reconnect with: " + discoverIp + " " + discoverPort);
                underConstruction = true; //Will now respond differently when a resourceMessage arrives.
            }
            else //Pass the information on to the right side.
            {
                //Can be removed.
                ReconstructMessage newReconstructMessage = new ReconstructMessage(
                        lostIP = lostIP,
                        lostPort = lostPort,
                        discoverIp = discoverIp,
                        discoverPort = discoverPort
                );
                sendReconstructMessage(new Socket(leftSideIp, leftSidePort), newReconstructMessage);
            }
        }catch (IOException e){e.printStackTrace();}
    }

    private void handleEchoMessage(EchoMessage echoMessage )
    {
        boolean echoMessageContent = echoMessage.getStillAlive();
        if (echoMessageContent == false)
        {
            //Send echo-message return
            try {sendEchoMessage(new Socket(leftSideIp,leftSidePort), new EchoMessage(true,ownPort));}
            catch (IOException e){e.printStackTrace();}
        }
        else
        {
            //Receive echo-message - Stop echo
            echo.interrupt();
            echo = null;
            //Intiate new one echo-thread
            intiateNewEcho();
        }
    }

    public void sendEchoMessage(Socket socket, EchoMessage echoMessage)
    {
        try
        {
            ObjectOutputStream clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
            clientOutputStream.writeObject(echoMessage);
        }
        catch (IOException e){
            e.printStackTrace();
            System.out.println("IGNORE");}
    }

    private void intiateNewEcho()
    {
        if (echo != null) // Better safe than sorry
        {
            echo.interrupt();
            echo = null;
        }
        Runnable echoSend = this::sendEcho;
        echo = new Thread(echoSend);
        echo.start();
    }

    public void sendEcho()
    {
        try {
            //Wait time
            Thread.sleep(1500);
            //Sends echo
            sendEchoMessage(new Socket(rightSideIp, rightSidePort), new EchoMessage(false, ownPort));
            //If the thread / echo hasn't been terminated / returned before this. Then something is wrong.
            Thread.sleep(5000);
            //NOT IMPLEMENTED. Restore from the left side
            //System.out.println("IN ECHO : ALERT");
            //reconstruct();
        }
        catch (InterruptedException e){}
        catch (IOException e){
            System.out.println("IOECEPTION : ALERT");
            reconstruct();
        }
    }

    public void handleRecourceMessage(ResourceMessage resourceMessage)
    {
        HashMap<Integer,String> moreRefs = resourceMessage.getStoredResource();
        for (int key : moreRefs.keySet())
        {
            String message = moreRefs.get(key);
            refferenceresources.put(key,message);
        }
        if (underConstruction) {underConstruction = false;}
    }

    private void handlePutMessage(PutMessage putMessage)
    {
        Integer key = putMessage.getKey();
        String message = putMessage.getMessage();
        boolean original = putMessage.getOriginal(); //If the message is send from a putClient. Then it's original.

        if (original) //If it's original. Then it' should be put inside ownResources and a reference should be send to the right side.
        {
            ownResources.put(key, message);

            //Send resource to the right socket. If it exsist's.
            if (!rightSideIp.equals(""))
            {
                try
                {
                    PutMessage newPutMessage = new PutMessage(key,message,false);
                    sendPutMessage(new Socket(rightSideIp,rightSidePort),newPutMessage);
                }
                catch (IOException e) {e.printStackTrace();}
            }
        }
        else //If it isn't original. Then it's a reference.
        {
            refferenceresources.put(key, message);
        }
    }

    private void handleGetMessage(GetMessage getMessage)
    {
        try {
            int key = getMessage.getKey();
            int port = getMessage.getPort();
            String ip = getMessage.getIp();

            String message = "";
            Socket getClientSocket = new Socket(ip, port); //connects to getClient

            if (ownResources.containsKey(key)) // Checks if ownResources has the key
            {
                message = ownResources.get(key);
                sendPutMessage(getClientSocket, new PutMessage(key, message, false));
            }
            else if (refferenceresources.containsKey(key)) // Checks if refferenceResources has the key
            {
                message = refferenceresources.get(key);
                sendPutMessage(getClientSocket, new PutMessage(key, message, false));
            }
            else //Otherwise propagate message to the rightside node.
            {
                sendGetMessage(new Socket(rightSideIp, rightSidePort), new GetMessage(key, ip, port));
            }
        }
        catch (IOException e) {e.printStackTrace();}
    }

    private void reconstruct() // Sends a message to the left, that eventually will reach the right side.
    {
        underConstruction = true;
        System.out.println(rightSidePort + "=" + leftSidePort + " " + rightSideIp + "=" + leftSideIp);
        if (rightSidePort == leftSidePort)
        {
            System.out.println("STOP");
            echo.interrupt();
            echo = null;
            rightSideIp = "";
            leftSideIp = "";
            if (!refferenceresources.isEmpty())
            {
                String message = "";
                for (int key : refferenceresources.keySet())
                {
                    message = refferenceresources.get(key);
                    ownResources.put(key,message);
                }
                refferenceresources.clear();
            }

        }
        else
        {
            try {
                String lostHostIP = inputServerSocket.getInetAddress().getLocalHost().toString();
                int index = lostHostIP.indexOf("/");
                lostHostIP = lostHostIP.substring(index + 1, lostHostIP.length());

                ReconstructMessage reconstructMessage = new ReconstructMessage(
                        rightSideIp,
                        rightSidePort,
                        lostHostIP,
                        ownPort
                );

                sendReconstructMessage(new Socket(leftSideIp, leftSidePort), reconstructMessage);

            } catch (IOException e) {
            }
        }
    }

    private void sendReconstructMessage(Socket leftSideSocket,ReconstructMessage reconstructMessage)
    {
        try
        {
            ObjectOutputStream clientOutputStream = new ObjectOutputStream(leftSideSocket.getOutputStream());
            clientOutputStream.writeObject(reconstructMessage);
        } catch (IOException e){e.printStackTrace();}
    }

    private void sendPutMessage(Socket rightSideSocket,PutMessage putMessage)
    {
        try {
            ObjectOutputStream clientOutputStream = new ObjectOutputStream(rightSideSocket.getOutputStream());
            clientOutputStream.writeObject(putMessage);
        }
        catch (IOException e){System.out.println("Couldn't send putMessage");}
    }

    private void sendGetMessage(Socket socket, GetMessage getMessage)
    {
        try {
            ObjectOutputStream clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
            clientOutputStream.writeObject(getMessage);
        }
        catch (IOException e){System.out.println("Couldn't send putMessage");}
    }

    private void sendResourceMessage(Socket socket, ResourceMessage resourceMessage)
    {
        try {
            ObjectOutputStream clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
            clientOutputStream.writeObject(resourceMessage);
        }
        catch (IOException e){System.out.println("Couldn't send putMessage");}
    }

    public static void main(String[] args)
    {
        if (args.length == 1) //For the first port only
        {
            CircleNode circleNode = new CircleNode(Integer.parseInt(args[0]));
        }
        else //For all other node there after.
        {
            CircleNode circleNode = new CircleNode(Integer.parseInt(args[0]),Integer.parseInt(args[1]),args[2]);
        }
    }
}