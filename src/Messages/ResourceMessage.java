package Messages;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Jakob_P_Holm on 17/11/2015.
 */
public class ResourceMessage implements Serializable {

    private HashMap<Integer,String> storedResource;

    public ResourceMessage(HashMap<Integer,String> resource){
        this.storedResource = resource;
    }

    public HashMap<Integer,String> getStoredResource(){return storedResource;}
}
