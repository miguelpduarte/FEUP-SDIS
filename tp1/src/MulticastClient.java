import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class MulticastClient {
    public static final int ADVERT_PACKET_SIZE = 128;
    private final DatagramPacket advert_packet;
    private final String multicast_addr;
    private final String operation;
    private final String[] operands;
    private final int multicast_port;
    private MulticastSocket ms;

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Invalid number of arguments.\nUsage: MulticastClient <multicast_addr> <multicast_port> <operation> <operands>");
            return;
        }

        String multicast_addr = args[0];
        int multicast_port = Integer.parseInt(args[1]);
        String operation = args[2];
        String[] operands = Arrays.copyOfRange(args, 3, args.length);

        new MulticastClient(multicast_addr, multicast_port, operation, operands);
    }

    public MulticastClient(String multicast_addr, int multicast_port, String operation, String[] operands) {
        this.multicast_addr = multicast_addr;
        this.operation = operation;
        this.operands = operands;
        this.multicast_port = multicast_port;

        this.advert_packet = new DatagramPacket(new byte[ADVERT_PACKET_SIZE], ADVERT_PACKET_SIZE);

        try {
            this.ms = new MulticastSocket(multicast_port);
            ms.joinGroup(InetAddress.getByName(multicast_addr));
            ms.setSoTimeout(4000);
            listenForBroadcast();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void listenForBroadcast() {
        while (true) {
            try {
                this.ms.receive(this.advert_packet);

                String str = new String(this.advert_packet.getData(), 0, this.advert_packet.getLength());
                System.out.println("DBG: Received from broadcast: " + str);

                String[] raw_str = str.split(" ");
                if (!raw_str[0].equals("plateserver")) {
                    System.out.println("DBG: Heard irrelevant broadcast, continuing to listen");
                    continue;
                }

                final int server_port = Integer.parseInt(raw_str[1]);

                System.out.printf("multicast: %s %d : %s %d\n", this.multicast_addr, this.multicast_port, this.advert_packet.getAddress(), server_port);
                this.communicateWithServer(this.advert_packet.getAddress(), server_port);
                break;

            } catch (SocketTimeoutException e) {
                System.out.println("Timeout ocurred, exiting");
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void communicateWithServer(InetAddress address, int port) {
        new Client(address, port, this.operation, this.operands);
    }
}
