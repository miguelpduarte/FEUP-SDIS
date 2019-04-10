package base.storage.requested;

public class NullRequestedBackupFileChunk extends RequestedBackupFileChunk {
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

    @Override
    public boolean isOverReplicated() {
        return true;
    }

    @Override
    public String getFileId() {
        return "";
    }

    @Override
    public int getChunkNo() {
        return -1;
    }

    @Override
    public int getReplicationDegree() {
        return -1;
    }

    @Override
    public String toString() {
        return "";
    }

    @Override
    public int getDiffToDesiredReplicationDegree() {
        return 0;
    }

    public NullRequestedBackupFileChunk() {
        super("", -1, -1);
    }
}
