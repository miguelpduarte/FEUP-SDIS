package base.storage;

/// Null Object Class (Design Pattern) for ChunkBackupInfo
public class NullChunkBackupInfo extends ChunkBackupInfo {
    public NullChunkBackupInfo() {
        super("", -1, 0, 0);
    }

    @Override
    public void incrementNumStored() {
    }

    @Override
    public void decrementNumStored() {
    }

    @Override
    public boolean isOverReplicationDegree() {
        return true;
    }
}
