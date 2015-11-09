package Messages;


import java.io.Serializable;

public class CapacityMessage implements Serializable{

    private int capacity;


    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }


}
