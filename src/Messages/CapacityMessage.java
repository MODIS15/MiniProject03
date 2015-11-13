package Messages;


import java.io.Serializable;

public class CapacityMessage extends Message implements Serializable{
   private int capacity;
   private boolean isSet = false;

    public CapacityMessage() {
        super(MessageTypeEnum.CapacityMessage);
    }


    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        isSet = true;
        this.capacity = capacity;
    }

    public boolean isSet() {
        return isSet;
    }




}

