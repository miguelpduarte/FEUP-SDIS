package base.protocol;

import base.ProtocolDefinitions;
import base.ThreadManager;
import base.channels.ChannelManager;
import base.messages.MessageFactory;
import base.persistentstate.FileDeletionLog;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class QueryDeletedHandler {
    private final ServerSocket server_socket;
    private final int port;

    public QueryDeletedHandler() throws IOException {
        this.server_socket = new ServerSocket(0);
        // The timeout value is the maximum exponential backoff time delay used for retries in other subprotocols
        this.server_socket.setSoTimeout(ProtocolDefinitions.getMaxMessageDelay() * ProtocolDefinitions.SECOND_TO_MILIS);
        this.port = this.server_socket.getLocalPort();
        advertiseService();
        listenAndReply();
        this.server_socket.close();
    }

    private void advertiseService() {
        final byte[] message = MessageFactory.createQueryDeletedMessage(this.port);
            ThreadManager.getInstance().executeLaterMilis(() -> {
                ChannelManager.getInstance().getControl().broadcast(message);
            }, ProtocolDefinitions.getRandomMessageDelayMilis());
    }

    private void listenAndReply() {
        try {
            final Socket connection = this.server_socket.accept();
            // Using ObjectOutputStream because this ensures that the byte[] is written as an object (aka all at once)
            ObjectInputStream ois = new ObjectInputStream(connection.getInputStream());
            FileDeletionLog file_deletion_log = (FileDeletionLog) ois.readObject();
            // System.out.println("Received deletion log from another peer.");
            FileDeletionLog.getInstance().joinLog(file_deletion_log);
            connection.close();
            System.out.println("Success in TCP Init protocol!");
        } catch (SocketTimeoutException e) {
            // Socket awaiting connection timed out
            System.out.println("No deletion log received from other peers.");
            return;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
