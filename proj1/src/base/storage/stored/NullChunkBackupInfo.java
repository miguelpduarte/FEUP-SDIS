package base.storage.stored;

/// Null Object Class (Design Pattern) for ChunkBackupInfo
public class NullChunkBackupInfo extends ChunkBackupInfo {
    public NullChunkBackupInfo() {
        super("", -1, 0, 0);
    }

    @Override
    public void addReplicator(String peer_id) {
    }

    @Override
    public void removeReplicator(String peer_id) {
    }

    @Override
    public boolean isReplicated() {
        return true;
    }
}
