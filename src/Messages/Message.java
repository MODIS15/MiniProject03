package Messages;


public abstract class Message {

    private MessageTypeEnum messageType;

    public Message(MessageTypeEnum type){
        this.messageType = type;
    }

    public MessageTypeEnum getMessageType(){
        return messageType;
    }


}
