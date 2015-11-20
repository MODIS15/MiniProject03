package Messages;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This message contains a resource stored in the network.
 */
public class ResourceMessage extends Message implements Serializable {

    private Map<Integer, String> storedResource;

    public ResourceMessage(Map<Integer, String> resource){
        super(MessageTypeEnum.ResourceMessage);
        this.storedResource = resource;
    }

    public Map<Integer, String> getStoredResource(){return storedResource;}
}
