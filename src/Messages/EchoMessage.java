package Messages;

import java.io.Serializable;

/**
 * Created by Jakob_P_Holm on 15/11/2015.
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
