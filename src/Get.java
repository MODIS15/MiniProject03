import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Get {

    private ServerSocket incomingFoundResourceSocket;

    public void Get(int port) throws IOException {
        incomingFoundResourceSocket = new ServerSocket(port);
        initialize();
    }

    private void initialize(){
        Runnable incoming = this::listenForIncomingResources;
        Thread incomingTread = new Thread(incoming);
        incomingTread.start();

        getResource();
    }

    private void getResource()
    {
        while(true){
            //TODO NOT IMPLEMENTED
            return;
        }
    }

    private void listenForIncomingResources()
    {
        while(true)
        {
            //TODO NOT IMPLEMENTED
            return;
        }
    }

    private void handleIncomingResource(Socket socket){

        //TODO NEED IMPLEMENTATION
        String message = "Resource";
        System.out.println(message);
    }
}
