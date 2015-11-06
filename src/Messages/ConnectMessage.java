package Messages;

import java.io.Serializable;

public class ConnectMessage implements Serializable{

    private String ipAddress;
    private int port;

    public ConnectMessage(String ip, int port)
    {
        this.ipAddress = ip;
        this.port = port;
    }


    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }
}
