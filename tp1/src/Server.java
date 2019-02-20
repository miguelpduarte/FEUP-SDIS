import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Server {
    private final AbstractRequestHandler request_handler;
    private DatagramSocket ds;

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        Server s = new Server(port, new PlateRequestHandler());
    }

    public Server(int port, AbstractRequestHandler request_handler) {
        this.request_handler = request_handler;

        System.out.println("Starting server in port " + port);

        try {
            this.ds = new DatagramSocket(port);

            this.startServer();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void startServer() {
        byte[] b = new byte[this.request_handler.getRequestMaxSize()];

        DatagramPacket dp = new DatagramPacket(b, b.length);

        while (true) {
            try {
                ds.receive(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.request_handler.handleRequest(dp);
        }
    }
}
