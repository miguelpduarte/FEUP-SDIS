package base.channels;

import base.ProtocolDefinitions;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public abstract class ChannelHandler implements Runnable {
    protected final DatagramPacket packet;
    protected final MulticastSocket channel_socket;
    private static final int PACKET_SIZE = ProtocolDefinitions.CHUNK_MAX_SIZE_BYTES + ProtocolDefinitions.HEADER_MAX_BYTES;
    private final String hostname;
    private final int port;

    public ChannelHandler(String hostname, int port) throws IOException {
        this.hostname = hostname;
        this.port = port;

        this.packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
        this.channel_socket = new MulticastSocket(port);
        this.channel_socket.joinGroup(InetAddress.getByName(hostname));
        // this.channel_socket.setTimeToLive(1);
        // this.channel_socket.setLoopbackMode(false); // Change?
    }

    @Override
    public void run() {
        while (true) {
            try {
                DatagramPacket p = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
                channel_socket.receive(p);
                // System.out.println("Who is this? " + p.getAddress());
                this.handle(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract void handle(DatagramPacket dp);

    public void broadcast(byte[] message) {
        try {
            DatagramPacket broadcast_packet = new DatagramPacket(message, message.length, InetAddress.getByName(this.hostname), this.port);
            this.channel_socket.send(broadcast_packet);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException ignored) {
        }
    }
}
