package Messages;

import java.io.Serializable;

public class EchoMessage  extends Message implements Serializable {
    private Boolean isAlive;

    public EchoMessage() {
        super(MessageTypeEnum.EchoMessage);
        this.isAlive = false;
    }

    public Boolean IsAlive() {
        return isAlive;
    }

    public void setIsAlive(Boolean isAlive) {
        this.isAlive = isAlive;
    }
}
