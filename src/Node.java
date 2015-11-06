import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node{

    private List<OutputStream> neighbourNodes;
    private ServerSocket getInputSocket;
    private ServerSocket putInputSocket;
    private Map<Integer,PutMessage> resources;

    public Node(){
        try {
            neighbourNodes = new ArrayList<>();
            getInputSocket = new ServerSocket();
            putInputSocket = new ServerSocket();
            resources = new HashMap<>();
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

    void saveNeighbourNode(OutputStream node){
        neighbourNodes.add(node);}
    int notifyNeighbourAmount(){return neighbourNodes.size();}
    PutMessage returnResource(int key){return resources.get(key);}
    void updateResources(int key, PutMessage message){
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
