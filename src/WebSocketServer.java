import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WebSocketServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Server started ");
            while(true) {
                try (Socket socketClient = serverSocket.accept()) {
                    System.out.println("New client connection:  " +socketClient);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
