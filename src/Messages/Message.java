package Messages;

import java.io.Serializable;

/**
 * Created by Jakob_P_Holm on 19/11/2015.
 */
public abstract class Message implements Serializable {
    private MessageTypeEnum messageType;

    public Message(MessageTypeEnum type) {
        this.messageType = type;
    }

    public MessageTypeEnum getMessageType() {
        return messageType;
    }
}
