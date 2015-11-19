package Messages;

import java.io.Serializable;

/**
 * A Connect Message contains the ip and port from a new or existing node.
 * The field "toFromOrClosure" is used to determine the state of a join process in the network.
 * By way of example, a new node may want to join the network.
 * Consequently, existing nodes need to change the information in neighbor nodes in order to establish a circular structure.
 *
 * Example:
 * A is connected to B
 * C sends a "From message" to A ("I want to join")
 * A receives message and opens it
 * A sets left side node with ip and port from C
 * A changes the message type to "To message" and sends it to B
 * B receives message  from A and opens it
 * B sets its right side message with ip and port from C
 * B now knows C, C knows A and A knows B but C doesn't know B
 * B creates new "Closure message" with its own ip and port, which is then sent to right side node C
 * C opens the "Closure message" and sets its left side node to B
 * The network is now connected in a circle.
 */
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
