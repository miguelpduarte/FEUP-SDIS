package base.tasks;

import base.ProtocolDefinitions;
import base.ThreadManager;
import base.channels.ChannelManager;
import base.messages.CommonMessage;
import base.messages.MessageFactory;
import base.messages.MessageWithChunkNo;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class EnhancedPutchunkHandler {
    private final ServerSocket server_socket;
    private final InetAddress address;
    private final byte[] chunk_data;
    private final int port;
    private final String file_id;
    private final int chunk_no;

    public EnhancedPutchunkHandler(MessageWithChunkNo info, InetAddress address, byte[] chunk_data) throws IOException {
        this.file_id = info.getFileId();
        this.chunk_no = info.getChunkNo();
        this.address = address;
        this.chunk_data = chunk_data;
        this.server_socket = new ServerSocket(0);
        // The timeout value is the accumulated exponential backoff time for retries in other subprotocols
        this.server_socket.setSoTimeout(ProtocolDefinitions.getAccumulatedMessageDelays());
        this.port = this.server_socket.getLocalPort();
        // TODO Wrong order here?
        startServer();
        advertiseService();
    }

    private void advertiseService() {
        ThreadManager.getInstance().executeLater(() -> {
            final byte[] message = MessageFactory.createPasvChunkMessage(this.file_id, this.chunk_no, this.port);
            //TODO Debate in which channel to send
            try {
                ChannelManager.getInstance().getControl().broadcast(message);
            } catch (IOException e) {
                System.out.println("Error when advertising TCP Restore service");
            }
        });
    }

    private void startServer() {
        try {
            final Socket connection = this.server_socket.accept();
            System.out.println("A connection was accepted!");
            // Using ObjectOutputStream because this ensures that the byte[] is written as an object (aka all at once)
            ObjectOutputStream oos = new ObjectOutputStream(connection.getOutputStream());
            oos.writeObject(this.chunk_data);
            System.out.println("Sending succesful! Now sending a STORED message");

            // Able to send, now communicating a STORED message
            final byte[] message = MessageFactory.createStoredMessage(this.file_id, this.chunk_no);
            ChannelManager.getInstance().getControl().broadcast(message);
            System.out.println("Success in TCP Restore protocol!");
        } catch (SocketTimeoutException e) {
            // Socket awaiting connection timed out
            System.out.println("Socket awaiting connection timed out!");
            return;
        } catch (IOException e) {
            System.out.println("EnhancedPutchunkHandler.startServer");
            e.printStackTrace();
        }
    }
}
