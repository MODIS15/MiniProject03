import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Node{

    private Socket leftSocket;
    private Socket rightSocket;
    private ServerSocket getInputSocket;
    private ServerSocket putInputSocket;
    private ServerSocket neighbourInputSocket;


    private Map resources;


    public Node(){
        resources = new HashMap<Integer,PutMessage>();
        initialize();

    }

    private void initialize(){
        System.out.println("Input port for getPort:");
        String getPort = System.console().readLine().trim();
        System.out.println("Input port for putPort:");
        String putPort = System.console().readLine().trim();
        System.out.println("Input port for putPort:");
        String neighbourPort = System.console().readLine().trim();

        try{
            listenForGet(Integer.parseInt(getPort));
            listenForPut(Integer.parseInt(putPort));
            listenForNewNeighbour(Integer.parseInt(neighbourPort));
        }
        catch (NumberFormatException e){
            System.out.println("Invalid Port");
            initialize();
        }


    }

   private void listenForPut(int port){
        try
        {
            putInputSocket = new ServerSocket(port);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
    private void listenForGet(int port){
        try
        {
            getInputSocket = new ServerSocket(port);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void listenForNewNeighbour(int neighbourInputPort){
        try {
            neighbourInputSocket = new ServerSocket(neighbourInputPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

   private void saveNeighbourNode(Socket node) {
       if(leftSocket == null){
           leftSocket = node;
       }
       else if (rightSocket == null){
           rightSocket = node;
       }
       else{
           ConnectMessage connectMessage = new ConnectMessage(rightSocket.getInetAddress().toString(),rightSocket.getPort());
           //Send message og set den nye right socket til ny nabo
       }

   }

    private void updateResources(int key, PutMessage message){
        OutputStream
        for(OutputStream node : neighbourNodes)
        {
//            if(node.)
                resources.put(key ,message);
        }
    }
//    OutputStream getNeighbourNode(int key){return neighbourNodes.get(key);}


    public void main(String[] args){
        Node node = new Node();
    }
}
