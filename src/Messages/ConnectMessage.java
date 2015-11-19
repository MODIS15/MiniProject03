package Messages;

import java.io.Serializable;

public class ConnectMessage extends Message implements Serializable{

    private String toFromOrClosure;
    private String ipAddress;
    private int port;

    public ConnectMessage(String toAndFrom,String ip, int port)
    {
        super(MessageTypeEnum.ConnectMessage);
        this.toFromOrClosure = toAndFrom;
        this.ipAddress = ip;
        this.port = port;
    }

    public  String getToAndFrom() {return toFromOrClosure;}

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }
}
