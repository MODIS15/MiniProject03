import java.net.Inet4Address;
import java.net.Socket;

public class GetMessage {
    private int key;
    private Inet4Address ip;
    private Socket port;

    public GetMessage(Integer key, Inet4Address ip, Socket port){
        this.key = key;
        this.ip = ip;
        this.port = port;
    }

    public int getKey(){return key;}
    public Inet4Address getIp(){return ip;}
    public Socket getPort(){return port;}
}
