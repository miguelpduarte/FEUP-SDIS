package base.channels;

import base.ProtocolDefinitions;
import base.ThreadManager;
import base.messages.*;
import base.protocol.EnhancedPutchunkHandler;
import base.storage.requested.RequestedBackupsState;
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
                    // Unless it is a PUTCHUNK request, which then we will reply with STORED if the file is stored to ensure that the network has a correct observed replication degree
                    if (info.getMessageType() == ProtocolDefinitions.MessageType.PUTCHUNK) {
                        MessageWithChunkNo mwcn = (MessageWithChunkNo) info;
                        final String file_id = mwcn.getFileId();
                        final int chunk_no = mwcn.getChunkNo();
                        if (StorageManager.getInstance().hasChunk(file_id, chunk_no)) {
                            final byte[] stored_message = MessageFactory.createStoredMessage(file_id, chunk_no);
                            ThreadManager.getInstance().executeLaterMilis(() -> {
                                ChannelManager.getInstance().getControl().broadcast(stored_message);
                            }, ProtocolDefinitions.getRandomMessageDelayMilis());
                        }
                    }
                    return;
                }

                System.out.printf("\t\tMDB: Received message of type %s\n", info.getMessageType().name());

                switch (info.getMessageType()) {
                    case PUTCHUNK:
                        // Middleware to ensure the rule: "A peer must never store the chunks of its own files"
                        if (RequestedBackupsState.getInstance().didRequestBackup(info.getFileId())) {
                            return;
                        }
                        if (info.getVersion().equals(ProtocolDefinitions.INITIAL_VERSION)) {
                            handlePutchunk(info);
                        } else if (info.getVersion().equals(ProtocolDefinitions.IMPROVED_VERSION) && info.getVersion().equals(ProtocolDefinitions.VERSION)) {
                            // Current version and message version MUST BE the Improved Version
                            handlePutchunkEnh((MessageWithChunkSize) info);
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void handlePutchunkEnh(MessageWithChunkSize info) {
        if (!StorageManager.getInstance().canStore(info.getChunkSize())) {
            System.out.println("Could not store the chunk");
            return;
        }

        try {
            new EnhancedPutchunkHandler(info);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handlePutchunk(CommonMessage info) {
        stopRepeatedPutchunkSending(info);
        storeChunk(info);
    }

    private void storeChunk(CommonMessage info) {
        try {
            final byte[] body = info.getBody();
            final String file_id = info.getFileId();
            final int chunk_no = ((MessageWithChunkNo)info).getChunkNo();
            final int replication_degree = ((MessageWithReplicationDegree)info).getReplicationDegree();

            // Registering that the chunk will be stored to ensure that the observed replication degree is correct (all STOREDs are taken into account even before the storage is finished)
            // This is unregistered if any problem occurs when actually storing the chunk
            ChunkBackupState.getInstance().registerBackup(file_id, chunk_no, replication_degree, body.length);

            if (!StorageManager.getInstance().storeChunk(file_id, chunk_no, body)) {
                System.out.printf("Storage of file id '%s' and chunk no '%d' was unsuccessful, aborting\n", file_id, chunk_no);
                // Unregistering because there was a problem when backing up
                ChunkBackupState.getInstance().unregisterBackup(file_id, chunk_no);
                return;
            }

            final byte[] stored_message = MessageFactory.createStoredMessage(file_id, chunk_no);

            System.out.printf("Stored file id '%s' - chunk no '%d' -> prepared reply STORED message and will make it broadcast after a random delay\n", file_id, chunk_no);

            ThreadManager.getInstance().executeLaterMilis(() -> {
                System.out.printf("Broadcasting STORED for file id '%s' and chunk no '%d'\n", file_id, chunk_no);
                ChannelManager.getInstance().getControl().broadcast(stored_message);
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
        final String chunk_hash = ProtocolDefinitions.calcChunkHash(info.getFileId(), ((MessageWithChunkNo)info).getChunkNo());
        Future f = this.putchunkMessagesToSend.get(chunk_hash);
        if (f == null) {
            return;
        }
        System.out.println("### REPEATED PUTCHUNK DETECTED - STOPPING THE SENDING");
        f.cancel(true);
        this.putchunkMessagesToSend.remove(chunk_hash);
    }
}
