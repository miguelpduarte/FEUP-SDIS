package base.storage.requested;

public class NullRequestedBackupFile extends RequestedBackupFile {
    private static final RequestedBackupFileChunk null_chunk = new NullRequestedBackupFileChunk();

    @Override
    public void registerChunk(RequestedBackupFileChunk rbfc) {
    }

    @Override
    public RequestedBackupFileChunk getChunk(int chunk_no) {
        return null_chunk;
    }

    @Override
    public String getFileId() {
        return "";
    }

    @Override
    public String getFilePath() {
        return "";
    }

    @Override
    public int getReplicationDegree() {
        return -1;
    }

    public NullRequestedBackupFile() {
        super("", "", -1);
    }
}
