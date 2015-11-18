package Messages;

import java.io.Serializable;

public class PutMessage implements Serializable{
    private int key;
    private String message;
    private boolean original;

    public PutMessage(int key, String message, boolean original){
        this.key = key;
        this.message = message;
        this.original = original;
    }

    public boolean getOriginal(){return original;}

    public int getKey() {
        return key;
    }

    public String getMessage(){
        return message;
    }
}
