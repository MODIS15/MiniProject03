import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

public class Node{

    private Map<Integer,OutputStream> neighbourSockets;
    private ServerSocket getInputSocket;
    private ServerSocket putInputSocket;
    private Map<Integer,String> resources;

    public Node(){
        try {
            neighbourSockets = new HashMap<Integer, OutputStream>();
            getInputSocket = new ServerSocket();
            putInputSocket = new ServerSocket();
            resources = new HashMap<Integer, String>();
            initialize();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println("An IO Exception occurred when opening the socket");
        }
    }

    private void initialize(){
        System.out.println("Input port for getPort:");
        String getPort = System.console().readLine().trim();
        System.out.println("Input port for putPort:");
        String putPort = System.console().readLine().trim();


        try{
            listenForGet(Integer.parseInt(getPort));
            listenForPut(Integer.parseInt(putPort));
        }
        catch (NumberFormatException e){
            System.out.println("Invalid Port");
            initialize();
        }


    }

    void listenForPut(int port){
        try
        {
            putInputSocket = new ServerSocket(port);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
    void listenForGet(int port){
        try
        {
            getInputSocket = new ServerSocket(port);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    void saveNeighbourNode(Integer key, OutputStream node){neighbourSockets.put(key, node);}

    int notifyNeighbourAmount(){return neighbourSockets.size();}

    void updateResources(Integer key, String message){resources.put(key ,message);}

    String returnResource(Integer key){return resources.get(key);}

    OutputStream getNeighbourNode(Integer key){return neighbourSockets.get(key);}

    public void main(String[] args){
        Node node = new Node();
    }
}
