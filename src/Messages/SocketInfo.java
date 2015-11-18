package Messages;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;

public class SocketInfo implements Serializable {
    private String ip;
    private int port;

    public SocketInfo(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public Socket getConnectableSocket() throws IOException {
        return new Socket(ip, port);
    }
}
