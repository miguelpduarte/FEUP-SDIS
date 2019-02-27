import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MulticastServer {
    private final String multicast_addr;
    private final int multicast_port;
    private MulticastSocket ms;
    private final int server_port;
    private DatagramPacket multicast_advert;

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Invalid number of arguments.\nUsage: MulticastServer <port> <multicast_addr> <multicast_port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        String multicast_addr = args[1];
        int multicast_port = Integer.parseInt(args[2]);

        new MulticastServer(port, multicast_addr, multicast_port);
    }

    public MulticastServer(int port, String multicast_addr, int multicast_port) {
        this.server_port = port;
        this.multicast_addr = multicast_addr;
        this.multicast_port = multicast_port;
        startRequestServer();

        byte[] b = ("plateserver " + this.server_port).getBytes();
        try {
            this.multicast_advert = new DatagramPacket(b, b.length, InetAddress.getByName(multicast_addr), multicast_port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        try {
            this.ms = new MulticastSocket(multicast_port);
            this.ms.joinGroup(InetAddress.getByName(multicast_addr));
            this.ms.setTimeToLive(1);
            this.ms.setLoopbackMode(false);

            this.startMulticastServer();

            this.ms.leaveGroup(InetAddress.getByName(multicast_addr));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startMulticastServer() {
        while (true) {
            try {
                this.ms.send(this.multicast_advert);
                System.out.printf("multicast: %s %d: %s %d\n", this.multicast_addr, this.multicast_port, this.multicast_advert.getAddress(), this.server_port);
                Thread.sleep(1000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void startRequestServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new Server(server_port, new PlateRequestHandler());
            }
        }).start();
    }
}
