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


        try {
            String star = inputServerSocket.getInetAddress().getLocalHost().toString();
            int index  = star.indexOf("/");
            star = star.substring(index+1,star.length());
            System.out.println(star);
        }catch (IOException e){}

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
        try{inputServerSocket = new ServerSocket(ownPort);}
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

            ConnectMessage connectMessage = new ConnectMessage("From",hostIP,ownPort);

            ObjectOutputStream clientOutputStream = new ObjectOutputStream(startSocket.getOutputStream());
            clientOutputStream.writeObject(connectMessage);
        }
        catch (IOException e){e.printStackTrace();}

    }

    public void run()
    {
        try
        {
            while (true)
            {
                Socket clientSocket = inputServerSocket.accept();

                ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                Object object = input.readObject();

                if (object instanceof ConnectMessage)
                {
                    System.out.println("Received connect message from " + clientSocket.getInetAddress());
                    String toFromOrClosure = ((ConnectMessage) object).getToAndFrom();
                    String ip = ((ConnectMessage) object).getIpAddress();
                    int port = ((ConnectMessage) object).getPort();
                    handleConnectMessage(toFromOrClosure, ip, port);
                    System.out.println("leftside: " + leftSidePort);
                    System.out.println("rightside: " + rightSidePort);
                    System.out.println();
                    //Starts the echo / heartbeat
                    System.out.println("ECHO-MESSAGE");
                    intiateNewEcho();
                }
                else if (object instanceof ReconstructMessage)
                {
                    String foreignIP = ((ReconstructMessage) object).getLostSideIp();
                    int foreignport = ((ReconstructMessage) object).getLostSidePort();
                    String discoverIp = ((ReconstructMessage) object).getDiscoverIp();
                    int discoverPort = ((ReconstructMessage) object).getDiscoverPort();

                    if (foreignport == leftSidePort)
                    {
                        //Sends all of it's refferences to the right, since it has inheriented all of it's references.
                        if (!refferenceresources.isEmpty()) {
                            sendResourceMessage(new Socket(rightSideIp,rightSidePort), new ResourceMessage
                                            (
                                                    refferenceresources
                                            )
                            );

                            String message = "";
                            for (Integer key : refferenceresources.keySet()) // Removes all of it's references.
                            {
                                message = refferenceresources.get(key);
                                ownResources.put(key, message);
                            }
                            refferenceresources.clear();
                        }

                        String localhost = inputServerSocket.getInetAddress().getLocalHost().toString();
                        int index  = localhost.indexOf("/");
                        localhost = localhost.substring(index+1,localhost.length());

                        Socket newLeftSideSocket = new Socket(discoverIp, discoverPort);

                        sendConnectMessage(newLeftSideSocket, new ConnectMessage("To", localhost, ownPort));
                        System.out.println("Tries to reconnect with: " + discoverIp + " " + discoverPort);
                        underConstruction = true; //Will now respond differently when a resourceMessage arrives.
                    }
                    else
                    {
                        ReconstructMessage reconstructMessage = new ReconstructMessage(
                                foreignIP = foreignIP,
                                foreignport = foreignport,
                                discoverIp = discoverIp,
                                discoverPort = discoverPort
                        );
                        System.out.println(foreignport + " " + reconstructMessage.getLostSidePort());
                        sendReconstructMessage(new Socket(leftSideIp, leftSidePort), reconstructMessage);
                        System.out.println("Sends reconstructionMessage to: " + leftSidePort);
                    }
                }
                else if (object instanceof EchoMessage)
                {
                    boolean echoM = ((EchoMessage) object).getStillAlive();
                    int port = ((EchoMessage) object).getPort();
                    //System.out.println("Recived: " + echoM + " From: " + port);
                    handleEchoMessage(echoM);
                }
                else if (object instanceof PutMessage)
                {
                    Integer key = ((PutMessage) object).getKey();
                    String message = ((PutMessage) object).getMessage();
                    boolean original = ((PutMessage) object).getOriginal();
                    if (original) {
                        System.out.println("RESIVED ORIGINAL PUTMESSAGE");
                        ownResources.put(key, message);
                        System.out.println("Key: " + key + " Message: " + message);
                        System.out.println(ownResources.get(key));

                        //Send resource to the right socket. If it exsists
                        if (!rightSideIp.equals(""))
                        {
                            System.out.println("SEND PUTMESSAGE");
                            PutMessage putMessage = new PutMessage(key,message,false);
                            sendPutMessage(new Socket(rightSideIp,rightSidePort),putMessage);
                        }
                    }
                    else
                    {
                        System.out.println("RESIVED REFERENCED PUTMESSAGE");
                        refferenceresources.put(key, message);
                        System.out.println("Key: " + key + " Message: " + message);
                        System.out.println(refferenceresources.get(key));
                    }
                }
                if (object instanceof ResourceMessage)
                {
                    if (underConstruction)
                    {
                        HashMap<Integer,String> moreRefs = ((ResourceMessage) object).getStoredResource();
                        for (int key : moreRefs.keySet())
                        {
                            String message = moreRefs.get(key);
                            refferenceresources.put(key,message);
                        }

                        underConstruction = false;
                    }
                    else //If the node isn't under construction. Then it's just going to "inherited" more information.
                    {
                        HashMap<Integer,String> moreRefs = ((ResourceMessage) object).getStoredResource();
                        for (int key : moreRefs.keySet())
                        {
                            String message = moreRefs.get(key);
                            refferenceresources.put(key,message);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
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

    private void handleConnectMessage(String toFromOrClosure, String ip, int port)
    {
        if (toFromOrClosure.equals("From"))
        {
            if (rightSideIp.equals("") && leftSideIp.equals("")) // When there only is one node
            {
                System.out.println("Start!!!");
                rightSideIp = ip;
                rightSidePort = port;

                leftSideIp = ip;
                leftSidePort = port;

                try
                {
                    Socket rightSocket = new Socket(rightSideIp, rightSidePort);

                    String hostIP = inputServerSocket.getInetAddress().getLocalHost().toString();
                    int index  = hostIP.indexOf("/");
                    hostIP = hostIP.substring(index+1,hostIP.length());

                    ConnectMessage connectMessage = new ConnectMessage("Closure",hostIP,ownPort);

                    sendConnectMessage(rightSocket, connectMessage);

                    if (!ownResources.isEmpty()) // Send all information to the second node.
                    {
                        sendResourceMessage(new Socket(rightSideIp,rightSidePort),
                                new ResourceMessage
                                        (
                                                ownResources
                                        )
                        );
                    }
                }
                catch (IOException e){e.printStackTrace();}
            }
            else //When there is more than one node
            {
                try
                {
                    Socket leftSocket;
                    if (!refferenceresources.isEmpty()) // Sends the new left socket all the references it's going to have.
                    {
                        leftSocket = new Socket(ip, port);
                        sendResourceMessage(leftSocket,
                                new ResourceMessage
                                        (
                                                refferenceresources
                                        )
                        );
                        refferenceresources.clear();
                    }

                    leftSocket = new Socket(leftSideIp, leftSidePort);

                    ConnectMessage connectMessage = new ConnectMessage("To",ip,port);

                    sendConnectMessage(leftSocket, connectMessage);

                    //Left side is set to the sender.
                    leftSideIp = ip;
                    leftSidePort = port;
                }
                catch (IOException e){e.printStackTrace();}
            }

        }
        else if(toFromOrClosure.equals("To"))
        {
            rightSideIp = ip;
            rightSidePort = port;

            try
            {
                if (underConstruction)
                {
                    if (!ownResources.isEmpty()) {
                        sendResourceMessage(new Socket(rightSideIp, rightSidePort), new ResourceMessage
                                        (
                                                ownResources
                                        )
                        );
                    }
                    underConstruction = false;
                }

                String hostIP = inputServerSocket.getInetAddress().getLocalHost().toString();
                int index  = hostIP.indexOf("/");
                hostIP = hostIP.substring(index+1,hostIP.length());

                System.out.println("SEND CLOSURE MESSAGE");
                ConnectMessage connectMessage = new ConnectMessage("Closure",hostIP,ownPort);

                Socket rightSocket = new Socket(rightSideIp, rightSidePort);
                sendConnectMessage(rightSocket, connectMessage);
            }
            catch (IOException e){e.printStackTrace();}
        }
        else if (toFromOrClosure.equals("Closure"))
        {
            leftSideIp = ip;
            leftSidePort = port;
            if (underConstruction){underConstruction = false;}
        }
    }

    private void handleEchoMessage(boolean echoMessageContent)
    {
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
        /*
        try{Thread.sleep(600);}
        catch (InterruptedException e) {
            System.out.println("HERE!!!!");
            e.printStackTrace();}
            */
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
            for (int key : refferenceresources.keySet()) {System.out.println("REF : " + refferenceresources.get(key));}
            for (int key : ownResources.keySet()) {System.out.println("ORG : " + ownResources.get(key));}
            //Wait time
            Thread.sleep(1500);
            //Sends echo
            sendEchoMessage(new Socket(rightSideIp, rightSidePort), new EchoMessage(false, ownPort));
            //If the thread / echo hasn't been terminated / returned before this. Then something is wrong.
            Thread.sleep(5000);
            //TO-DO ALERT
            System.out.println("IN ECHO : ALERT");
            //reconstruct(); <-- ACTIVATE??
        }
        catch (InterruptedException e){}
        catch (IOException e){
            System.out.println("IOECEPTION : ALERT");
            reconstruct();
        }
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