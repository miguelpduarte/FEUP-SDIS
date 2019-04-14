package base.channels;

import base.ProtocolDefinitions;
import base.ThreadManager;
import base.messages.CommonMessage;
import base.messages.MessageFactory;
import base.messages.MessageWithChunkNo;
import base.messages.MessageWithPasvPort;
import base.protocol.EnhancedGetchunkHandler;
import base.protocol.task.*;
import base.protocol.task.extendable.Task;
import base.storage.StorageManager;
import base.storage.requested.RequestedBackupsState;
import base.storage.stored.ChunkBackupInfo;
import base.storage.stored.ChunkBackupState;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.concurrent.Future;

public class ControlChannelHandler extends ChannelHandler {
    public ControlChannelHandler(String hostname, int port) throws IOException {
        super(hostname, port);
    }

    @Override
    protected void handle(DatagramPacket dp) {
        final byte[] packet_data = dp.getData();
        final int packet_length = dp.getLength();


        ThreadManager.getInstance().executeLater(() -> {
            try {
                CommonMessage info = MessageFactory.getBasicInfo(packet_data, packet_length);

                if (info == null) {
                    System.out.println("MDC: Message couldn't be parsed");
                    return;
                }

                if (info.getSenderId().equals(ProtocolDefinitions.SERVER_ID)) {
                    // Own Message, ignoring
                    return;
                }

                System.out.printf("\t\tMDC: Received message of type %s\n", info.getMessageType().name());

                switch (info.getMessageType()) {
                    case STORED:
                        handleStored(info);
                        break;
                    case GETCHUNK:
                        if (info.getVersion().equals(ProtocolDefinitions.INITIAL_VERSION)) {
                            handleGetchunk(info);
                        } else if (info.getVersion().equals(ProtocolDefinitions.IMPROVED_VERSION) && info.getVersion().equals(ProtocolDefinitions.VERSION)) {
                            // Current version and message version MUST BE the Improved Version
                            handleGetchunkEnh(info);
                        }
                        break;
                    case CANSTORE:
                        handleCanStore(info, dp.getAddress());
                        break;
                    case DELETE:
                        handleDelete(info);
                        break;
                    case REMOVED:
                        handleRemoved(info);
                        break;
                    case PASVCHUNK:
                        handlePasvChunk((MessageWithPasvPort) info, dp.getAddress());
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void handleCanStore(CommonMessage info, InetAddress address) {
        final Task t = TaskManager.getInstance().getTask(info);
        if (t != null) {
            ((EnhancedPutchunkTask) t).notify((MessageWithPasvPort) info, address);
        }
    }

    private void handlePasvChunk(MessageWithPasvPort info, InetAddress address) {
        final Task t = TaskManager.getInstance().getTask(info);
        if (t != null) {
            ((EnhancedGetchunkTask) t).notify(info, address);
        }
    }

    private void handleRemoved(CommonMessage info) {
        final String file_id = info.getFileId();
        final int chunk_no = ((MessageWithChunkNo) info).getChunkNo();

        RequestedBackupsState.getInstance()
                .getRequestedFileBackupInfo(info.getFileId())
                .getChunk(((MessageWithChunkNo) info).getChunkNo())
                .removeReplicator(info.getSenderId());

        final ChunkBackupInfo chunk_backup_info = ChunkBackupState.getInstance().getChunkBackupInfo(file_id, chunk_no);
        chunk_backup_info.removeReplicator(info.getSenderId());

        if (!chunk_backup_info.isReplicated()) {
            System.out.printf("Chunk is now underreplicated, file_id '%s' and chunk_no '%d'\n", file_id, chunk_no);

            // Read from file
            final byte[] chunk_data = StorageManager.getInstance().getStoredChunk(file_id, chunk_no);
            assert chunk_data != null;

            final int replication_degree = chunk_backup_info.getReplicationDegree();

            Future f = ThreadManager.getInstance().executeLaterMilis(() -> {
                try {
                    // Start PUTCHUNK sub-protocol for this chunk
                    final PutchunkTask t = new PutchunkTask(file_id, chunk_no, replication_degree, chunk_data, true);
                    TaskManager.getInstance().registerTask(t);
                    t.start();
                } catch (Exception e) {
                    System.out.println("In REMOVED recovery");
                    e.printStackTrace();
                }
            }, ProtocolDefinitions.getRandomMessageDelayMilis());

            ChannelManager.getInstance().getBackup().registerPutchunkToSend(info.getFileId(), ((MessageWithChunkNo) info).getChunkNo(), f);
        } else {
            System.out.printf("No need to re-replicate with PUTCHUNK, file_id '%s' and chunk_no '%d'\n", file_id, chunk_no);
        }
    }

    private void handleDelete(CommonMessage info) {
        StorageManager.getInstance().removeFileChunksIfStored(info.getFileId());
        RequestedBackupsState.getInstance().unregisterRequestedFile(info.getFileId());
    }

    private void handleGetchunkEnh(CommonMessage info) {
        byte[] chunk_data = StorageManager.getInstance().getStoredChunk(info.getFileId(), ((MessageWithChunkNo) info).getChunkNo());
        if (chunk_data == null) {
            System.out.println("Don't have the chunk");
            return;
        }

        try {
            new EnhancedGetchunkHandler((MessageWithChunkNo) info, chunk_data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGetchunk(CommonMessage info) {
        byte[] chunk_data = StorageManager.getInstance().getStoredChunk(info.getFileId(), ((MessageWithChunkNo) info).getChunkNo());
        if (chunk_data == null) {
            return;
        }

        final byte[] chunk_message = MessageFactory.createChunkMessage(info.getFileId(), ((MessageWithChunkNo) info).getChunkNo(), chunk_data);
        Future f = ThreadManager.getInstance().executeLaterMilis(() -> {
            try {
                System.out.print("Broadcasting CHUNK\n");
                ChannelManager.getInstance().getRestore().broadcast(chunk_message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, ProtocolDefinitions.getRandomMessageDelayMilis());

        ChannelManager.getInstance().getRestore().registerChunkToSend(info.getFileId(), ((MessageWithChunkNo) info).getChunkNo(), f);
    }

    private void handleStored(CommonMessage info) {
        // If the chunk is backed up here, then count up the number of replicators in the system
        ChunkBackupState.getInstance()
                .getChunkBackupInfo(info.getFileId(), ((MessageWithChunkNo) info).getChunkNo())
                .addReplicator(info.getSenderId());

        RequestedBackupsState.getInstance()
                .getRequestedFileBackupInfo(info.getFileId())
                .getChunk(((MessageWithChunkNo) info).getChunkNo())
                .addReplicator(info.getSenderId());

        Task t = TaskManager.getInstance().getTask(info);
        if (t != null) {
            t.notify(info);
        }
    }
}
