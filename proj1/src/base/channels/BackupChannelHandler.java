package base.channels;

import base.ProtocolDefinitions;
import base.ThreadManager;
import base.messages.CommonMessage;
import base.messages.InvalidMessageFormatException;
import base.messages.MessageFactory;
import base.storage.stored.ChunkBackupState;
import base.storage.StorageManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class BackupChannelHandler extends ChannelHandler {
    public BackupChannelHandler(String hostname, int port) throws IOException {
        super(hostname, port);
    }

    private final ConcurrentHashMap<String, Future> putchunkMessagesToSend = new ConcurrentHashMap<String, Future>();

    public void registerPutchunkToSend(String file_id, int chunk_no, Future f) {
        this.putchunkMessagesToSend.put(ProtocolDefinitions.calcChunkHash(file_id, chunk_no), f);
    }

    @Override
    protected void handle(DatagramPacket dp) {
        final byte[] packet_data = dp.getData();
        final int packet_length = dp.getLength();

        ThreadManager.getInstance().executeLater(() -> {
            try {
                CommonMessage info = MessageFactory.getBasicInfo(packet_data, packet_length);
                if (info == null) {
                    System.out.println("MDB: Message couldn't be parsed");
                    return;
                }

                if (info.getSenderId().equals(ProtocolDefinitions.SERVER_ID)) {
                    // Own Message, ignoring
                    return;
                }

                System.out.printf("\t\tMDB: Received message of type %s\n", info.getMessageType().name());

                switch (info.getMessageType()) {
                    case PUTCHUNK:
                        handlePutchunk(info);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void handlePutchunk(CommonMessage info) {
        stopRepeatedPutchunkSending(info);
        storeChunk(info);
    }

    private void storeChunk(CommonMessage info) {
        try {
            final byte[] body = info.getBody();
            final String file_id = info.getFileId();
            final int chunk_no = info.getChunkNo();
            final int replication_degree = info.getReplicationDegree();

            if (!StorageManager.getInstance().storeChunk(file_id, chunk_no, body)) {
                System.out.printf("Storage of file id '%s' and chunk no '%d' was unsuccessful, aborting\n", file_id, chunk_no);
                return;
            }

            // Registering that the chunk was backed up successfully
            ChunkBackupState.getInstance().registerBackup(file_id, chunk_no, replication_degree, body.length);

            final byte[] stored_message = MessageFactory.createStoredMessage(file_id, chunk_no);

            System.out.printf("Stored file id '%s' - chunk no '%d' -> prepared reply STORED message and will make it broadcast after a random delay\n", file_id, chunk_no);

            ThreadManager.getInstance().executeLaterMilis(() -> {
                try {
                    System.out.printf("Broadcasting STORED for file id '%s' and chunk no '%d'\n", file_id, chunk_no);
                    ChannelManager.getInstance().getControl().broadcast(stored_message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, ProtocolDefinitions.getRandomMessageDelayMilis());
        } catch (InvalidMessageFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * For stopping the repeated sending of PUTCHUNKs for the reclaim protocol
     * @param info message to test for repeated PUTCHUNKs
     */
    private void stopRepeatedPutchunkSending(CommonMessage info) {
        final String chunk_hash = ProtocolDefinitions.calcChunkHash(info.getFileId(), info.getChunkNo());
        Future f = this.putchunkMessagesToSend.get(chunk_hash);
        if (f == null) {
            return;
        }
        System.out.println("### REPEATED PUTCHUNK DETECTED - STOPPING THE SENDING");
        f.cancel(true);
        this.putchunkMessagesToSend.remove(chunk_hash);
    }
}
