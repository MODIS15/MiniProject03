package Messages;


import java.io.Serializable;

public abstract class Message implements Serializable {

    private MessageTypeEnum messageType;


    protected Message() {
    }

    public Message(MessageTypeEnum type){
        this.messageType = type;
    }

    public MessageTypeEnum getMessageType(){
        return messageType;
    }


}
