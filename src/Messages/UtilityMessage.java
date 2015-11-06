package Messages;

import java.io.Serializable;

public class UtilityMessage implements Serializable{
    private boolean isDisconnect = false;
    //Possible for future extensions

    public UtilityMessage(){}

    public void setDisconnect(boolean bool){
        isDisconnect = bool;
    }

    public boolean getIsDisconnect(){
        return isDisconnect;
    }
}
