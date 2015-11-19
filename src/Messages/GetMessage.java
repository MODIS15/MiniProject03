package Messages;

import java.io.Serializable;

/**
 * A GetMessage contains the ip and port of the node with a resource specified by the key.
 */
public class GetMessage extends Message implements Serializable {
    private int key;
    private String ip;
    private int port;

    public GetMessage(Integer key, String ip, int port){
        super(MessageTypeEnum.GetMessage);
        this.key = key;
        this.ip = ip;
        this.port = port;
    }

    public int getKey(){return key;}

    public String getIp(){return ip;}

    public int getPort(){return port;}

}
