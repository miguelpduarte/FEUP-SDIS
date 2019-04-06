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

    private static final ChunkBackupInfo null_chunk_backup_info = new NullChunkBackupInfo();

    public void registerBackup(String file_id, int chunk_no, int replication_degree, int size_bytes) {
        this.backed_up_chunks_info.put(ProtocolDefinitions.calcChunkHash(file_id, chunk_no), new ChunkBackupInfo(file_id, chunk_no, replication_degree, size_bytes));
    }

    public ChunkBackupInfo getChunkBackupInfo(String file_id, int chunk_no) {
        return this.backed_up_chunks_info.getOrDefault(ProtocolDefinitions.calcChunkHash(file_id, chunk_no), null_chunk_backup_info);
    }

    public void unregisterBackup(String file_id, int chunk_no) {
        this.backed_up_chunks_info.remove(ProtocolDefinitions.calcChunkHash(file_id, chunk_no));
    }

    public boolean isChunkBackedUp(String file_id, int chunk_no) {
        return this.backed_up_chunks_info.containsKey(ProtocolDefinitions.calcChunkHash(file_id, chunk_no));
    }
}
