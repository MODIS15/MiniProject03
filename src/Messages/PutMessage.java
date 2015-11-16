package Messages;

import java.io.Serializable;

public class PutMessage extends Message implements Serializable{
    private int key;
    private String message;


    protected PutMessage() {
    }

    public PutMessage(int key, String message){
        super(MessageTypeEnum.PutMessage);
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
