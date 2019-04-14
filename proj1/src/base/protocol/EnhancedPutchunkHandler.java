package base.protocol;

import base.ProtocolDefinitions;
import base.ThreadManager;
import base.channels.ChannelManager;
import base.messages.MessageFactory;
import base.messages.MessageWithChunkSize;
import base.storage.StorageManager;
import base.storage.stored.ChunkBackupState;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class EnhancedPutchunkHandler {
    private final ServerSocket server_socket;
    private final int port;
    private final String file_id;
    private final int chunk_no;
    private final int replication_degree;

    public EnhancedPutchunkHandler(MessageWithChunkSize info) throws IOException {
        this.file_id = info.getFileId();
        this.chunk_no = info.getChunkNo();
        this.replication_degree = info.getReplicationDegree();

        // Registering that the chunk will be stored to ensure that the observed replication degree is correct (all STOREDs are taken into account even before the storage is finished)
        // This is unregistered if any problem occurs when actually storing the chunk
        ChunkBackupState.getInstance().registerBackup(file_id, chunk_no, replication_degree, info.getChunkSize());

        this.server_socket = new ServerSocket(0);
        // The timeout value is the maximum exponential backoff time delay used for retries in other subprotocols
        this.server_socket.setSoTimeout(ProtocolDefinitions.getMaxMessageDelay() * ProtocolDefinitions.SECOND_TO_MILIS);
        this.port = this.server_socket.getLocalPort();

        advertiseService();
        listenAndReply();

        this.server_socket.close();
    }

    private void advertiseService() {
        final byte[] message = MessageFactory.createCanStoreMessage(this.file_id, this.chunk_no, this.port);
        System.out.println("Waiting for data on port " + this.port);
        ThreadManager.getInstance().executeLaterMilis(() -> {
            ChannelManager.getInstance().getControl().broadcast(message);
        }, ProtocolDefinitions.getRandomMessageDelayMilis());
    }

    private void listenAndReply() {
        try {
            final Socket connection = this.server_socket.accept();
            System.out.println("A connection was accepted!");
            ObjectInputStream ois = new ObjectInputStream(connection.getInputStream());
            // Using ObjectIntputStream because this ensures that the byte[] is read as an object (aka all at once)
            byte[] chunk_data = (byte[]) ois.readObject();
            System.out.printf("Receiving succesful! Now storing %d bytes!\n", chunk_data.length);

            if (!writeChunkToFile(chunk_data)) {
                System.out.println("Failed in writing chunk to file!");
                // Unregistering because there was a problem when backing up
                ChunkBackupState.getInstance().unregisterBackup(this.file_id, this.chunk_no);
                return;
            }

            System.out.println("Wrote stuff to file");
            connection.close();

            sendStored();

            System.out.println("Success in TCP Putchunk protocol!");
        } catch (SocketTimeoutException e) {
            // Socket awaiting connection timed out
            System.out.println("Socket awaiting connection to receive timed out!");
            // Unregistering because there was a problem when backing up
            ChunkBackupState.getInstance().unregisterBackup(this.file_id, this.chunk_no);
            if (StorageManager.getInstance().hasChunk(this.file_id, this.chunk_no)) {
                StorageManager.getInstance().removeChunk(this.file_id, this.chunk_no);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            // Unregistering because there was a problem when backing up
            ChunkBackupState.getInstance().unregisterBackup(this.file_id, this.chunk_no);
            if (StorageManager.getInstance().hasChunk(this.file_id, this.chunk_no)) {
                StorageManager.getInstance().removeChunk(this.file_id, this.chunk_no);
            }
        }
    }

    private boolean writeChunkToFile(byte[] chunk_data) {
        if (!StorageManager.getInstance().storeChunk(file_id, chunk_no, chunk_data)) {
            System.out.printf("Storage of file id '%s' and chunk no '%d' was unsuccessful, aborting\n", file_id, chunk_no);
            return false;
        }

        System.out.printf("Stored file id '%s' - chunk no '%d' -> will prepare STORED message and broadcast it after a random delay\n", file_id, chunk_no);
        return true;
    }

    private void sendStored() {
        final byte[] stored_message = MessageFactory.createStoredMessage(this.file_id, this.chunk_no);
        ThreadManager.getInstance().executeLaterMilis(() -> {
            System.out.println("Sending STORED Message after TCP transfer");
            ChannelManager.getInstance().getControl().broadcast(stored_message);
        }, ProtocolDefinitions.getRandomMessageDelayMilis());
    }
}
