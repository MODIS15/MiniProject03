package Messages;

import java.io.Serializable;

public class DisconnectMessage implements Serializable{
    private boolean isDisconnect = false;
    //Possible for future extensions

    public DisconnectMessage(){}

    public void setDisconnect(boolean bool){
        isDisconnect = bool;
    }

    public boolean getIsDisconnect(){
        return isDisconnect;
    }
}
