package NodeUtils;

import java.io.IOException;
import java.net.Socket;

/**
 * This class is responsible for keeping a node's information
 */
public class SocketInfo {
    private String ip;
    private int port;


    public SocketInfo(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }


    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }


    /**
     * Returns a connected socket with stored ip and port
     * @return socket
     * @throws IOException
     */
    public Socket getSocket() throws IOException {
        if(port < 1) return null; //To avoid creation of invalid sockets
        return new Socket(ip,port);
    }

    /**
     * Returns a string of stored ip and port
     * @return string
     */
    public String toString(){
        return "IP: "+ ip + " "+"Port: "+port;
    }
}
