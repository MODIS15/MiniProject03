package Messages;

import java.io.Serializable;

/**
 * This message is used to reconstruct the circular network if it is broken due to a failing node (disconnect, crash etc).
 */
public class ReconstructMessage extends Message implements Serializable
{
    private String lostSideIp;
    private int lostSidePort;
    private String discoverIp;
    private int discoverPort;

    public ReconstructMessage(String lostSideIp, int lostSidePort,String discoverIp,int discoverPort)
    {
        super(MessageTypeEnum.ReconstructMessage);
        this.lostSideIp = lostSideIp;
        this.lostSidePort = lostSidePort;
        this.discoverIp = discoverIp;
        this.discoverPort = discoverPort;
    }

    public String getLostSideIp() {
        return lostSideIp;
    }

    public int getLostSidePort() {
        return lostSidePort;
    }

    public String getDiscoverIp() {
        return discoverIp;
    }

    public int getDiscoverPort() {
        return discoverPort;
    }
}
