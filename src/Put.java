public class Put{

    public void putMessage(Node node, PutMessage message){
        int key = message.hashCode();
        node.updateResources(key, message);
    }
}
