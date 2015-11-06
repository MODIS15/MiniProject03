package Messages;

import java.io.Serializable;

/**
 * Example of wrapper class for any type of objects that could be implemented
 */
public class ResourcePackage implements Serializable {
    private String type;
    private Object storedResource;

    public ResourcePackage(String type, Object resource){

    }

    //eg of an enum with different types
    enum ResourceType{
        STRING,
        PICTURE,
        MOVIE,
    }

}
