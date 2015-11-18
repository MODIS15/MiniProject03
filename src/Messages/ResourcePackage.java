package Messages;

import java.io.Serializable;

/**
 * Example of wrapper class for any type of objects that could be implemented
 */
public class ResourcePackage extends Message implements Serializable {
    private String type;
    private Object storedResource;

    public ResourcePackage(String type, Object resource){
        super(MessageTypeEnum.ResourcePackage);
    }

    //eg of an enum with different types
    enum ResourceType{
        STRING,
        PICTURE,
        MOVIE,
    }

}
