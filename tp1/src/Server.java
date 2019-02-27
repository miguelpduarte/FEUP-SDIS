import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Server {
    private final AbstractRequestHandler request_handler;
    private final int port;
    private DatagramSocket ds;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Invalid number of arguments.\nUsage: Server <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);

        new Server(port, new PlateRequestHandler());
    }

    public Server(int port, AbstractRequestHandler request_handler) {
        this.port = port;
        this.request_handler = request_handler;

        System.out.println("Starting server in port " + port + " ...");

        try {
            this.ds = new DatagramSocket(port);

            this.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void start() {
        System.out.println("Server listening in port " + port);
        byte[] b = new byte[this.request_handler.getRequestMaxSize()];

        DatagramPacket dp = new DatagramPacket(b, b.length);

        while (true) {
            try {
                ds.receive(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Request from <" + dp.getAddress() + ", " + dp.getPort() + ">");

            byte[] res = this.request_handler.handleRequest(dp);
            dp.setData(res);

            try {
                ds.send(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }

            dp.setData(b);  // If not, dp length will the the same as the length of the received response
        }
    }
}
