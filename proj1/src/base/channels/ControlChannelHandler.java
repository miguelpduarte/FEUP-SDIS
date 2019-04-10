package base.channels;

import base.ProtocolDefinitions;
import base.ThreadManager;
import base.messages.CommonMessage;
import base.messages.MessageFactory;
import base.storage.ChunkBackupInfo;
import base.storage.ChunkBackupState;
import base.storage.StorageManager;
import base.tasks.PutchunkTask;
import base.tasks.Task;
import base.tasks.TaskManager;

import java.io.IOException;
import java.net.DatagramPacket;
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
                        handleGetchunk(info);
                        break;
                    case DELETE:
                        handleDelete(info);
                        break;
                    case REMOVED:
                        handleRemoved(info);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void handleRemoved(CommonMessage info) {
        final String file_id = info.getFileId();
        final int chunk_no = info.getChunkNo();

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
                    TaskManager.getInstance().registerTask(new PutchunkTask(file_id, chunk_no, replication_degree, chunk_data));
                } catch (Exception e) {
                    System.out.println("In REMOVED recovery");
                    e.printStackTrace();
                }
            }, ProtocolDefinitions.getRandomMessageDelayMilis());

            ChannelManager.getInstance().getBackup().registerPutchunkToSend(info.getFileId(), info.getChunkNo(), f);
        } else {
            System.out.printf("No need to re-replicate with PUTCHUNK, file_id '%s' and chunk_no '%d'\n", file_id, chunk_no);
        }
    }

    private void handleDelete(CommonMessage info) {
        StorageManager.getInstance().removeFileChunksIfStored(info.getFileId());
    }

    private void handleGetchunk(CommonMessage info) {
        byte[] chunk_data = StorageManager.getInstance().getStoredChunk(info.getFileId(), info.getChunkNo());
        if (chunk_data == null) {
            return;
        }

        final byte[] chunk_message = MessageFactory.createChunkMessage(info.getFileId(), info.getChunkNo(), chunk_data);
        Future f = ThreadManager.getInstance().executeLaterMilis(() -> {
            try {
                System.out.print("Broadcasting CHUNK\n");
                ChannelManager.getInstance().getRestore().broadcast(chunk_message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, ProtocolDefinitions.getRandomMessageDelayMilis());

        ChannelManager.getInstance().getRestore().registerChunkToSend(info.getFileId(), info.getChunkNo(), f);
    }

    private void handleStored(CommonMessage info) {
        // If the chunk is backed up here, then count up the number of replicators in the system
        ChunkBackupState.getInstance().getChunkBackupInfo(info.getFileId(), info.getChunkNo()).addReplicator(info.getSenderId());

        Task t = TaskManager.getInstance().getTask(info);
        if (t != null) {
            t.notify(info);
        }
    }
}
