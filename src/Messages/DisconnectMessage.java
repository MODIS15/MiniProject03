package Messages;

import java.io.Serializable;

public class DisconnectMessage extends Message implements Serializable{
    private boolean isDisconnect = false;
    //Possible for future extensions

    public DisconnectMessage(){super(MessageTypeEnum.DisconnectMessage);}

    public void setDisconnect(boolean bool){
        isDisconnect = bool;
    }

    public boolean getIsDisconnect(){
        return isDisconnect;
    }
}
