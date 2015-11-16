package Messages;

import java.io.Serializable;

/**
 * Created by Jakob_P_Holm on 15/11/2015.
 */
public class ReconstructMessage implements Serializable
{
    private String lostSideIp;
    private int lostSidePort;
    private String discoverIp;
    private int discoverPort;

    public ReconstructMessage(String lostSideIp, int lostSidePort,String discoverIp,int discoverPort)
    {
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
