package base;

import base.channels.BackupChannelHandler;
import base.channels.ChannelManager;
import base.channels.ControlChannelHandler;
import base.channels.RestoreChannelHandler;
import base.messages.MessageFactory;
import base.storage.*;
import base.storage.requested.RequestedBackupFile;
import base.storage.requested.RequestedBackupFileChunk;
import base.storage.requested.RequestedBackupsState;
import base.storage.stored.ChunkBackupInfo;
import base.storage.stored.ChunkBackupState;
import base.tasks.*;

import java.io.File;
import java.io.IOException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.List;

public class Peer extends UnicastRemoteObject implements IPeer {
    public Peer(String mc_hostname, int mc_port, String mdb_hostname, int mdb_port, String mdr_hostname, int mdr_port) throws IOException {
        super(0); // required to avoid the 'rmic' step, see PeerMain.main

        createChannelHandlers(mc_hostname, mc_port, mdb_hostname, mdb_port, mdr_hostname, mdr_port);
    }

    private void createChannelHandlers(String mc_hostname, int mc_port, String mdb_hostname, int mdb_port, String mdr_hostname, int mdr_port) throws IOException {
        ChannelManager.getInstance().setChannelHandlers(
                createMcHandler(mc_hostname, mc_port),
                createMdbChannel(mdb_hostname, mdb_port),
                createMdrChannel(mdr_hostname, mdr_port)
        );
    }

    private ControlChannelHandler createMcHandler(String mc_hostname, int mc_port) throws IOException {
        ControlChannelHandler control = new ControlChannelHandler(mc_hostname, mc_port);
        new Thread(control).start();
        return control;
    }

    private BackupChannelHandler createMdbChannel(String mdb_hostname, int mdb_port) throws IOException {
        BackupChannelHandler backup = new BackupChannelHandler(mdb_hostname, mdb_port);
        new Thread(backup).start();
        return backup;
    }

    private RestoreChannelHandler createMdrChannel(String mdr_hostname, int mdr_port) throws IOException {
        RestoreChannelHandler restore = new RestoreChannelHandler(mdr_hostname, mdr_port);
        new Thread(restore).start();
        return restore;
    }

    @Override
    public int backup(String file_path, int replication_factor) {
        System.out.println("Peer.backup");
        System.out.println("file_path = [" + file_path + "], replication_factor = [" + replication_factor + "]");

        final String file_name = new File(file_path).getName();
        final String file_id = MessageFactory.filenameEncode(file_name);

        RequestedBackupsState.getInstance().registerRequestedFile(new RequestedBackupFile(file_id, file_name, replication_factor));

        try {
            byte[] file_data = StorageManager.readFromFile(file_path);
            byte[][] split_file_data = MessageFactory.splitFileContents(file_data);

            for (int i = 0; i < split_file_data.length; ++i) {
                TaskManager.getInstance().registerTask(new PutchunkTask(file_id, i, replication_factor, split_file_data[i]));
                RequestedBackupsState.getInstance().getRequestedFileBackupInfo(file_id).registerChunk(new RequestedBackupFileChunk(file_id, i, replication_factor));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        } catch (FileTooLargeException e) {
            System.err.println("File is too large for storage in this distributed backup system!");
            return -2;
        }

        return 0;
    }

    @Override
    public int restore(String file_path) {
        System.out.println("Peer.restore");
        System.out.println("file_path = [" + file_path + "]");

        final String file_name = new File(file_path).getName();
        final String file_id = MessageFactory.filenameEncode(file_name);

        TaskManager.getInstance().registerTask(new RestoreTask(file_id, file_name));

        return 0;
    }

    @Override
    public int delete(String file_path) {
        System.out.println("Peer.delete");
        System.out.println("file_path = [" + file_path + "]");

        final String file_name = new File(file_path).getName();
        final String file_id = MessageFactory.filenameEncode(file_name);

        TaskManager.getInstance().registerTask(new DeleteTask(file_id));
        // Also deleting own files if they exist
        StorageManager.getInstance().removeFileChunksIfStored(MessageFactory.filenameEncode(file_name));
        RequestedBackupsState.getInstance().unregisterRequestedFile(file_id);

        return 0;
    }

    @Override
    public int setMaxDiskSpace(int disk_space_kbs) {
        System.out.println("Peer.setMaxDiskSpace");
        System.out.println("disk_space_kbs = [" + disk_space_kbs + "]");

        try {
            StorageManager.getInstance().setMaxSpaceKbytes(disk_space_kbs);

            if (StorageManager.getInstance().storageOverCapacity()) {
                final List<ChunkBackupInfo> chunks_candidate_for_removal = ChunkBackupState.getInstance().getChunksCandidateForRemoval();

                int candidate_idx = 0;
                do {
                    if (candidate_idx >= chunks_candidate_for_removal.size()) {
                        System.err.print("Exhausted all the candidate chunks and still over capacity!\n");
                        return -1;
                    }

                    final ChunkBackupInfo candidate_chunk = chunks_candidate_for_removal.get(candidate_idx);
                    System.out.println("candidate_for_removal = " + candidate_chunk);

                    // Delete Chunk
                    if (!StorageManager.getInstance().removeChunk(candidate_chunk.getFileId(), candidate_chunk.getChunkNo())) {
                        System.err.printf("Error in removing chunk for file_id '%s' and no '%d'\n", candidate_chunk.getFileId(), candidate_chunk.getChunkNo());
                    } else {
                        // Start broadcasting that the chunk was removed
                        TaskManager.getInstance().registerTask(new RemovedTask(candidate_chunk.getFileId(), candidate_chunk.getChunkNo()));
                    }

                    candidate_idx++;
                } while (StorageManager.getInstance().storageOverCapacity());
            } else {
                System.out.println("Did not need to remove anything");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public String getServiceState() {
        StringBuilder sb = new StringBuilder();

        sb.append("Service State of Peer ").append(ProtocolDefinitions.SERVER_ID).append("\n");

        sb.append("\nInitiated Backups:\n");
        final Collection<RequestedBackupFile> allFilesInfo = RequestedBackupsState.getInstance().getAllFilesInfo();

        if (allFilesInfo.isEmpty()) {
            sb.append("No backups were initiated by this Peer.\n");
        } else {
            for (RequestedBackupFile rbf : allFilesInfo) {
                sb.append(rbf).append("\n");
            }
        }

        sb.append("\nStored Chunks:\n");

        final Collection<ChunkBackupInfo> allBackedUpChunksInfo = ChunkBackupState.getInstance().getAllBackedUpChunksInfo();

        if (allBackedUpChunksInfo.isEmpty()) {
            sb.append("No chunks are currently stored.\n");
        } else {
            for (ChunkBackupInfo chunkBackupInfo : allBackedUpChunksInfo) {
                sb.append(chunkBackupInfo).append("\n");
            }
        }

        sb.append("\nPeer storage (KBytes):\n");
        sb.append("Maximum Capacity: ").append(StorageManager.getInstance().getMaximumCapacity()).append("\tCurrent Usage: ").append(StorageManager.getInstance().getOccupiedSpaceBytes() / 1000);

        return sb.toString();
    }
}
