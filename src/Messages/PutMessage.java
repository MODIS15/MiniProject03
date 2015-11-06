package Messages;

import java.io.Serializable;

public class PutMessage implements Serializable{
    private int key;
    private String message;

    public PutMessage(int key, String message){
        this.key = key;
        this.message = message;
    }

    public int getKey() {
        return key;
    }

    public String getMessage(){
        return message;
    }
}
