package NodeUtils;

//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.net.SocketException;
//import java.net.UnknownHostException;


/**
 * This is no longer used. It was an old simple implementation of a circle network.
 * IT was used to demo a circle network for the group.
 *  @deprecated
 */
public class SimpleCircleNodeDEPRICATED
{
//    //For all nodes
//    int ownPort;
//    private ServerSocket inputServerSocket;
//
//    int leftSidePort;
//    String leftSideIp = "";
//
//    int rightSidePort;
//    String rightSideIp = "";
//
//    Thread echo = null;
//
//    public SimpleCircleNodeDEPRICATED(int port)
//    {
//        ownPort = port;
//        setUpServer();
//        Runnable run = this::run;
//        Thread thread = new Thread(run);
//        thread.start();
//
//
//        try {
//            String star = inputServerSocket.getInetAddress().getLocalHost().toString();
//            int index  = star.indexOf("/");
//            star = star.substring(index+1,star.length());
//            System.out.println(star);
//        }catch (IOException e){}
//
//    }
//
//    public SimpleCircleNodeDEPRICATED(int port, int _ortherPort, String _otherIP)
//    {
//        ownPort = port;
//        rightSideIp = _otherIP;
//        rightSidePort = _ortherPort;
//        setUpServer();
//
//        //Set up connection to others.
//        sendStartConnectessage();
//
//        Runnable run = this::run;
//        Thread thread = new Thread(run);
//        thread.start();
//    }
//
//    public void setUpServer()
//    {
//        try{inputServerSocket = new ServerSocket(ownPort);}
//        catch (IOException e){e.printStackTrace();}
//    }
//
//    public void sendStartConnectessage()
//    {
//        try
//        {
//            Socket startSocket = new Socket(rightSideIp, rightSidePort);
//
//            String hostIP = inputServerSocket.getInetAddress().getLocalHost().toString();
//            int index  = hostIP.indexOf("/");
//            hostIP = hostIP.substring(index+1,hostIP.length());
//
//            ConnectMessage connectMessage = new ConnectMessage("From",hostIP,ownPort);
//
//            ObjectOutputStream clientOutputStream = new ObjectOutputStream(startSocket.getOutputStream());
//            clientOutputStream.writeObject(connectMessage);
//        }
//        catch (IOException e){e.printStackTrace();}
//
//    }
//
//    public void run()
//    {
//        try
//        {
//            while (true)
//            {
//                Socket clientSocket = inputServerSocket.accept();
//
//                ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
//                Object object = input.readObject();
//
//                if (object instanceof ConnectMessage)
//                {
//                    System.out.println("Received connect message from " + clientSocket.getInetAddress());
//                    String toFromOrClosure = ((ConnectMessage) object).getToAndFrom();
//                    String ip = ((ConnectMessage) object).getIpAddress();
//                    int port = ((ConnectMessage) object).getPort();
//                    handleConnectMessage(toFromOrClosure, ip, port);
//                    System.out.println("leftside: " + leftSidePort);
//                    System.out.println("rightside: " + rightSidePort);
//                    System.out.println();
//                    //Starts the echo / heartbeat
//                    System.out.println("ECHO-MESSAGE");
//                    intiateNewEcho();
//                }
//                else if (object instanceof ReconstructMessage)
//                {
//                    String foreignIP = ((ReconstructMessage) object).getLostSideIp();
//                    int foreignport = ((ReconstructMessage) object).getLostSidePort();
//                    String discoverIp = ((ReconstructMessage) object).getDiscoverIp();
//                    int discoverPort = ((ReconstructMessage) object).getDiscoverPort();
//
//                    if (foreignport == leftSidePort)
//                    {
//                        String localhost = inputServerSocket.getInetAddress().getLocalHost().toString();
//                        int index  = localhost.indexOf("/");
//                        localhost = localhost.substring(index+1,localhost.length());
//
//                        Socket newLeftSideSocket = new Socket(discoverIp, discoverPort);
//
//                        sendConnectMessage(newLeftSideSocket, new ConnectMessage("To", localhost, ownPort));
//                        System.out.println("Tries to reconnect with: " + discoverIp + " " + discoverPort);
//                    }
//                    else
//                    {
//                        ReconstructMessage reconstructMessage = new ReconstructMessage(
//                                foreignIP = foreignIP,
//                                foreignport = foreignport,
//                                discoverIp = discoverIp,
//                                discoverPort = discoverPort
//                        );
//                        System.out.println(foreignport + " " + reconstructMessage.getLostSidePort());
//                        sendReconstructMessage(new Socket(leftSideIp, leftSidePort), reconstructMessage);
//                        System.out.println("Sends reconstructionMessage to: " + leftSidePort);
//                    }
//                }
//                else if (object instanceof EchoMessage)
//                {
//                    boolean echoM = ((EchoMessage) object).getStillAlive();
//                    int port = ((EchoMessage) object).getPort();
//                    //System.out.println("Recived: " + echoM + " From: " + port);
//                    handleEchoMessage(echoM);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void sendConnectMessage(Socket socket, ConnectMessage connectMessage)
//    {
//        try
//        {
//            ObjectOutputStream clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
//            clientOutputStream.writeObject(connectMessage);
//        }
//        catch (IOException e){e.printStackTrace();}
//    }
//
//    private void handleConnectMessage(String toFromOrClosure, String ip, int port)
//    {
//        if (toFromOrClosure.equals("From"))
//        {
//            if (rightSideIp.equals("") && leftSideIp.equals("")) // When there only is one node
//            {
//                System.out.println("Start!!!");
//                rightSideIp = ip;
//                rightSidePort = port;
//
//                leftSideIp = ip;
//                leftSidePort = port;
//
//                try
//                {
//                    Socket rightSocket = new Socket(rightSideIp, rightSidePort);
//
//                    String hostIP = inputServerSocket.getInetAddress().getLocalHost().toString();
//                    int index  = hostIP.indexOf("/");
//                    hostIP = hostIP.substring(index+1,hostIP.length());
//
//                    ConnectMessage connectMessage = new ConnectMessage("Closure",hostIP,ownPort);
//
//                    sendConnectMessage(rightSocket, connectMessage);
//
//                }
//                catch (IOException e){e.printStackTrace();}
//            }
//            else //When there is more than one node
//            {
//                try
//                {
//                    Socket leftSocket = new Socket(leftSideIp, leftSidePort);
//
//                    ConnectMessage connectMessage = new ConnectMessage("To",ip,port);
//
//                    sendConnectMessage(leftSocket, connectMessage);
//
//                    //Left side is set to the sender.
//                    leftSideIp = ip;
//                    leftSidePort = port;
//                }
//                catch (IOException e){e.printStackTrace();}
//            }
//
//        }
//        else if(toFromOrClosure.equals("To"))
//        {
//            rightSideIp = ip;
//            rightSidePort = port;
//
//            try
//            {
//                String hostIP = inputServerSocket.getInetAddress().getLocalHost().toString();
//                int index  = hostIP.indexOf("/");
//                hostIP = hostIP.substring(index+1,hostIP.length());
//
//                System.out.println("SEND CLOSURE MESSAGE");
//                ConnectMessage connectMessage = new ConnectMessage("Closure",hostIP,ownPort);
//
//                Socket rightSocket = new Socket(rightSideIp, rightSidePort);
//                sendConnectMessage(rightSocket, connectMessage);
//            }
//            catch (IOException e){e.printStackTrace();}
//        }
//        else if (toFromOrClosure.equals("Closure"))
//        {
//            leftSideIp = ip;
//            leftSidePort = port;
//        }
//    }
//
//    private void handleEchoMessage(boolean echoMessageContent)
//    {
//        if (echoMessageContent == false)
//        {
//            //Send echo-message return
//            try {sendEchoMessage(new Socket(leftSideIp,leftSidePort), new EchoMessage(true,ownPort));}
//            catch (IOException e){e.printStackTrace();}
//        }
//        else
//        {
//            //Receive echo-message - Stop echo
//            echo.interrupt();
//            echo = null;
//            //Intiate new one echo-thread
//            intiateNewEcho();
//        }
//    }
//
//    public void sendEchoMessage(Socket socket, EchoMessage echoMessage)
//    {
//        try
//        {
//            ObjectOutputStream clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
//            clientOutputStream.writeObject(echoMessage);
//        }
//        catch (IOException e){
//            e.printStackTrace();
//            System.out.println("IGNORE");}
//    }
//
//    private void intiateNewEcho()
//    {
//        if (echo != null) // Better safe than sorry
//        {
//            echo.interrupt();
//            echo = null;
//        }
//        Runnable echoSend = this::sendEcho;
//        echo = new Thread(echoSend);
//        echo.start();
//    }
//
//    public void sendEcho()
//    {
//        try {
//            //Wait time
//            Thread.sleep(1500);
//            //Sends echo
//            sendEchoMessage(new Socket(rightSideIp, rightSidePort), new EchoMessage(false, ownPort));
//            //If the thread / echo hasn't been terminated / returned before this. Then something is wrong.
//            Thread.sleep(5000);
//            //NOT IMPLEMENTED. Restore from the left side
//            //System.out.println("IN ECHO : ALERT");
//            //reconstruct();
//        }
//        catch (InterruptedException e){}
//        catch (IOException e){
//            System.out.println("IOECEPTION : ALERT");
//            reconstruct();
//        }
//    }
//
//    private void reconstruct() // Sends a message to the left, that eventually will reach the right side.
//    {
//        System.out.println(rightSidePort + "=" + leftSidePort + " " + rightSideIp + "=" + leftSideIp);
//        if (rightSidePort == leftSidePort)
//        {
//            System.out.println("STOP");
//            echo.interrupt();
//            echo = null;
//            rightSideIp = "";
//            leftSideIp = "";
//        }
//        else
//        {
//            try {
//                String lostHostIP = inputServerSocket.getInetAddress().getLocalHost().toString();
//                int index = lostHostIP.indexOf("/");
//                lostHostIP = lostHostIP.substring(index + 1, lostHostIP.length());
//
//                ReconstructMessage reconstructMessage = new ReconstructMessage(
//                        rightSideIp,
//                        rightSidePort,
//                        lostHostIP,
//                        ownPort
//                );
//
//                sendReconstructMessage(new Socket(leftSideIp, leftSidePort), reconstructMessage);
//
//            } catch (IOException e) {
//            }
//        }
//    }
//
//    private void sendReconstructMessage(Socket leftSideSocket,ReconstructMessage reconstructMessage)
//    {
//        try
//        {
//            ObjectOutputStream clientOutputStream = new ObjectOutputStream(leftSideSocket.getOutputStream());
//            clientOutputStream.writeObject(reconstructMessage);
//        } catch (IOException e){e.printStackTrace();}
//    }
//
//    public static void main(String[] args)
//    {
//        if (args.length == 1) //For the first port only
//        {
//            SimpleCircleNodeDEPRICATED SimplecircleNode = new SimpleCircleNodeDEPRICATED(Integer.parseInt(args[0]));
//        }
//        else //For all other node there after.
//        {
//            SimpleCircleNodeDEPRICATED SimplecircleNode = new SimpleCircleNodeDEPRICATED(Integer.parseInt(args[0]),Integer.parseInt(args[1]),args[2]);
//        }
//    }
}
