package base.storage;

import base.ProtocolDefinitions;

import java.util.concurrent.ConcurrentHashMap;

public class ChunkBackupState {
    private static ChunkBackupState instance = new ChunkBackupState();

    public static ChunkBackupState getInstance() {
        return instance;
    }

    private ChunkBackupState() {
    }

    private final ConcurrentHashMap<String, ChunkBackupInfo> backed_up_chunks_info = new ConcurrentHashMap<>();

    public void registerBackup(String file_id, int chunk_no, int replication_degree) {
        this.backed_up_chunks_info.put(ProtocolDefinitions.calcChunkHash(file_id, chunk_no), new ChunkBackupInfo(file_id, chunk_no, replication_degree));
    }

    public void incrementBackupCount(String file_id, int chunk_no) {
        this.backed_up_chunks_info.getOrDefault(ProtocolDefinitions.calcChunkHash(file_id, chunk_no), new NullChunkBackupInfo()).incrementNumStored();
    }

    public void decrementBackupCount(String file_id, int chunk_no) {
        this.backed_up_chunks_info.getOrDefault(ProtocolDefinitions.calcChunkHash(file_id, chunk_no), new NullChunkBackupInfo()).decrementNumStored();
    }

    public void unregisterBackup(String file_id, int chunk_no) {
        this.backed_up_chunks_info.remove(ProtocolDefinitions.calcChunkHash(file_id, chunk_no));
    }

    public boolean isChunkBackedUp(String file_id, int chunk_no) {
        return this.backed_up_chunks_info.containsKey(ProtocolDefinitions.calcChunkHash(file_id, chunk_no));
    }

    public boolean isChunkOverReplicationDegree(String file_id, int chunk_no) {
        return this.backed_up_chunks_info.getOrDefault(ProtocolDefinitions.calcChunkHash(file_id, chunk_no), new NullChunkBackupInfo()).isOverReplicationDegree();
    }
}
