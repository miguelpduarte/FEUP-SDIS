import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public abstract class ChannelHandler implements Runnable {
    protected final DatagramPacket packet;
    protected final MulticastSocket channel_socket;
    private static final int PACKET_SIZE = 1024; // TODO: Update value
    private final String hostname;
    private final int port;
    private final String channel_identifier;

    public ChannelHandler(String hostname, int port, String channel_identifier) throws IOException {
        this.hostname = hostname;
        this.port = port;

        this.channel_identifier = channel_identifier;
        this.packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
        this.channel_socket = new MulticastSocket(port);
        this.channel_socket.joinGroup(InetAddress.getByName(hostname));
        this.channel_socket.setTimeToLive(1);
        this.channel_socket.setLoopbackMode(false); // Change?
    }

    @Override
    public void run() {
        while (true) {
            try {
                channel_socket.receive(packet);
                System.out.println(channel_identifier + " received message");
                this.handle();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract void handle();

    public void broadcast(byte[] message) throws IOException {
        DatagramPacket broadcast_packet = new DatagramPacket(message, message.length, InetAddress.getByName(this.hostname), this.port);
        this.channel_socket.send(broadcast_packet);
    }
}
