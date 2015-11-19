package Messages;

import java.io.Serializable;
import java.util.HashMap;

/**
 * This message contains a resource stored in the network.
 */
public class ResourceMessage extends Message implements Serializable {

    private HashMap<Integer,String> storedResource;

    public ResourceMessage(HashMap<Integer,String> resource){
        super(MessageTypeEnum.ReconstructMessage);
        this.storedResource = resource;
    }

    public HashMap<Integer,String> getStoredResource(){return storedResource;}
}
