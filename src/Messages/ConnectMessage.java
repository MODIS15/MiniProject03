package Messages;

import java.io.Serializable;

public class ConnectMessage implements Serializable{

    private String ipAddress;
    private int port;
    private boolean isNewJoin;

    public ConnectMessage(String ip, int port)
    {
        this.ipAddress = ip;
        this.port = port;
        isNewJoin = true;
    }


    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public boolean isNewJoin() {
        return isNewJoin;
    }

    public void setIsNewJoin(boolean isNewJoin) {
        this.isNewJoin = isNewJoin;
    }
}
