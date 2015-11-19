package Messages;

import java.io.Serializable;

/**
 * This class functions as a base class for all different types of messages sent in the network.
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
