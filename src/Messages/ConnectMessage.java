package Messages;

import java.io.Serializable;

public class ConnectMessage extends Message implements Serializable{

    private String ipAddress;
    private int port;
    private boolean isNewConnection;


    protected ConnectMessage() {
        super(MessageTypeEnum.ConnectMessage);
    }
    public ConnectMessage(String ip, int port)
    {
        super(MessageTypeEnum.ConnectMessage);
        this.ipAddress = ip;
        this.port = port;
        this.isNewConnection = true;
    }


    public boolean isNewConnection() {
        return isNewConnection;
    }

    public void setNewConnection(boolean isNewConnection) {
        this.isNewConnection = isNewConnection;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }


}
