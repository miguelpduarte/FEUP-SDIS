package base.protocol.task;

import base.ProtocolDefinitions;
import base.ThreadManager;
import base.channels.ChannelHandler;
import base.channels.ChannelManager;
import base.messages.MessageFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class EnhancedGetchunkTask extends GetchunkTask {
    private final ServerSocket server_socket;
    private final int port;

    public EnhancedGetchunkTask(String file_id, String file_name, int chunk_no) throws IOException {
        super(file_id, file_name, chunk_no);
        this.server_socket = new ServerSocket(0);
        // The timeout value is the maximum exponential backoff time delay used for retries in other subprotocols
        this.server_socket.setSoTimeout(ProtocolDefinitions.getSumMessageDelay() * ProtocolDefinitions.SECOND_TO_MILIS);
        this.port = this.server_socket.getLocalPort();
        prepareMessage();
        ThreadManager.getInstance().executeLater(() -> {
            listen();
            try {
                this.server_socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void listen() {
        try {
            final Socket connection = this.server_socket.accept();
            // Using ObjectOutputStream because this ensures that the byte[] is written as an object (aka all at once)
            ObjectInputStream ois = new ObjectInputStream(connection.getInputStream());
            byte[] chunk_data = (byte[]) ois.readObject();
            // System.out.println("\tReceiving succesful! chk " + chunk_no);
            connection.close();

            if (this.storeInFile(chunk_data)) {
                System.out.println("Success in TCP Restore protocol! chk " + chunk_no);
                this.stopTask();
                this.notifyObserver(true);
            }
        } catch (SocketTimeoutException e) {
            // Socket awaiting connection timed out
            System.out.println("Getchunk Socket awaiting data timed out!");
            this.notifyObserver(false);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /*public synchronized void notify(MessageWithPasvPort msg, InetAddress address) {
        if (!this.isRunning()) {
            return;
        }

        if (msg.getMessageType() != ProtocolDefinitions.MessageType.PASVCHUNK) {
            System.out.println("DBG:EnhancedGetchunkTask.notify::Message was not of type PASVCHUNK!");
            return;
        }

        if (msg.getChunkNo() != this.chunk_no || !msg.getFileId().equals(this.file_id)) {
            System.out.println("DBG:EnhancedGetchunkTask.notify::Message target was not this specific task");
            return;
        }

        System.out.printf("Now processing PASVCHUNK for %s and %d\n", this.file_id, this.chunk_no);

        this.pauseCommunication();

        try (Socket s = new Socket(address, msg.getPasvPort()); ObjectInputStream ois = new ObjectInputStream(s.getInputStream())) {
            byte[] chunk_data = (byte[]) ois.readObject();

            if (this.storeInFile(chunk_data)) {
                this.stopTask();
                this.notifyObserver(true);
                return;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        this.resumeCommuncation();
    }*/

    @Override
    protected byte[] createMessage() {
        return MessageFactory.createEnhancedGetchunkMessage(file_id, this.chunk_no, this.port);
    }

    @Override
    protected void handleMaxRetriesReached() {
        System.out.printf("Maximum retries reached for EnhancedGetchunkTask for fileid '%s', at chunk_no '%d'\n", this.file_id, this.chunk_no);
        this.stopTask();
        this.notifyObserver(false);
    }

    @Override
    protected void printSendingMessage() {
        System.out.printf("Sending EnhancedGETCHUNK message for fileid '%s' and chunk_no '%d' - attempt #%d\n", this.file_id, this.chunk_no, this.getCurrentAttempt() + 1);
    }

    @Override
    public String toKey() {
        // "EXTRA_GETCHUNK_PREFIX" is added to ensure that there are no collisions with hashes of just file_ids
        return "EXTRA_GETCHUNK_PREFIX" + this.file_id + this.chunk_no;
    }

    @Override
    protected ChannelHandler getChannel() {
        return ChannelManager.getInstance().getControl();
    }
}
