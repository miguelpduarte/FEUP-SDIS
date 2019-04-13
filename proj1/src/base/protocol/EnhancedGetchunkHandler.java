package base.protocol;

import base.ProtocolDefinitions;
import base.ThreadManager;
import base.channels.ChannelManager;
import base.messages.MessageFactory;
import base.messages.MessageWithChunkNo;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class EnhancedGetchunkHandler {
    private final ServerSocket server_socket;
    private final byte[] chunk_data;
    private final int port;
    private final String file_id;
    private final int chunk_no;

    public EnhancedGetchunkHandler(MessageWithChunkNo info, byte[] chunk_data) throws IOException {
        this.file_id = info.getFileId();
        this.chunk_no = info.getChunkNo();
        this.chunk_data = chunk_data;
        this.server_socket = new ServerSocket(0);
        // The timeout value is the maximum exponential backoff time delay used for retries in other subprotocols
        this.server_socket.setSoTimeout(ProtocolDefinitions.getMaxMessageDelay() * ProtocolDefinitions.SECOND_TO_MILIS);
        this.port = this.server_socket.getLocalPort();
        advertiseService();
        listenAndReply();
        this.server_socket.close();
    }

    private void advertiseService() {
        ThreadManager.getInstance().executeLaterMilis(() -> {
            final byte[] message = MessageFactory.createPasvChunkMessage(this.file_id, this.chunk_no, this.port);
            try {
                ChannelManager.getInstance().getControl().broadcast(message);
            } catch (IOException e) {
                System.out.println("Error when advertising TCP Restore service");
            }
        }, ProtocolDefinitions.getRandomMessageDelayMilis());
    }

    private void listenAndReply() {
        try {
            final Socket connection = this.server_socket.accept();
            System.out.println("A connection was accepted!");
            // Using ObjectOutputStream because this ensures that the byte[] is written as an object (aka all at once)
            ObjectOutputStream oos = new ObjectOutputStream(connection.getOutputStream());
            oos.writeObject(this.chunk_data);
            System.out.println("Sending succesful!");
            connection.close();

            System.out.println("Success in TCP Restore protocol!");
        } catch (SocketTimeoutException e) {
            // Socket awaiting connection timed out
            System.out.println("Socket awaiting connection timed out!");
            return;
        } catch (IOException e) {
            System.out.println("EnhancedGetchunkHandler.listenAndReply");
            e.printStackTrace();
        }
    }
}
