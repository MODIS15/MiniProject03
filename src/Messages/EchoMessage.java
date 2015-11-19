package Messages;

import java.io.Serializable;

/**
 * This message is used to check if a given node is alive.
 */
public class EchoMessage extends Message implements Serializable
{
    private boolean stillAlive;
    private int port;

    public EchoMessage(boolean alive, int port)
    {
        super(MessageTypeEnum.EchoMessage);
        stillAlive = alive;
        this.port = port;
    }

    public boolean getStillAlive(){return stillAlive;}

    public int getPort(){return port;}

}
