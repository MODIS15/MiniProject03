import jdk.nashorn.internal.runtime.regexp.joni.Regex;

import java.io.Console;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.List;
import java.util.Map;

public class Node{

    private List<OutputStream> neighbourSockets;
    private ServerSocket getInputSocket;
    private ServerSocket putInputSocket;
    private Map<Integer,String> ressources;

    public Node(){
        initialize();
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
    void saveNeighbourNode(OutputStream node){}
    void notifyNeighbourCapacity(){}
    void updateResourceCapacity(){}
    void returnResource(){}

    OutputStream getNeighbourNode(){
        return null;
    }

    public void main(String[] args){
        Node node = new Node();
    }
}
