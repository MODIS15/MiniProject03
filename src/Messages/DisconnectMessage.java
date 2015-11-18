package Messages;

import java.io.Serializable;

public class DisconnectMessage extends Message implements Serializable{
    private boolean isDisconnect = false; //For future extensions

    private SocketInfo newConnectionInfo;

    public DisconnectMessage() {
        super(MessageTypeEnum.DisconnectMessage);
        isDisconnect = true;
    }

    public DisconnectMessage(SocketInfo socketInfo) {
        super(MessageTypeEnum.DisconnectMessage);
        newConnectionInfo = socketInfo;
    }

    public void setDisconnect(boolean bool){
        isDisconnect = bool;
    }

    public boolean getIsDisconnect(){
        return isDisconnect;
    }

    public SocketInfo getNewConnectionInfo() {
        return newConnectionInfo;
    }
}
