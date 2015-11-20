package Messages;

import java.io.Serializable;

/**
 * This resource is used to insert resources in the network by passing a resource with a given key.
 */
public class PutMessage extends Message implements Serializable{
    private int key;
    private String resource;
    private boolean isSentFromPut;

    public PutMessage(int key, String resource, boolean isFromPut){
        super(MessageTypeEnum.PutMessage);
        this.key = key;
        this.resource = resource;
        this.isSentFromPut = isFromPut;
    }

    public boolean isSentFromPut(){return isSentFromPut;}

    public int getKey() {
        return key;
    }

    public String getResource(){
        return resource;
    }
}
