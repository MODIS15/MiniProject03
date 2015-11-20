package NodeUtils;

public class DeadException extends Exception {
    public DeadException() {
        super("ALERT: Lost connection to neighbour");
    }
}
