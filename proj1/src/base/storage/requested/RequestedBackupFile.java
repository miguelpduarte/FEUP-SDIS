package base.storage.requested;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class RequestedBackupFile implements Serializable {
    private final ConcurrentHashMap<Integer, RequestedBackupFileChunk> chunks = new ConcurrentHashMap<>();

    private static final RequestedBackupFileChunk null_chunk_backup = new NullRequestedBackupFileChunk();
    private final String file_id;
    private final String file_path;
    private final int replication_degree;

    public RequestedBackupFile(String file_id, String file_path, int replication_degree) {
        this.file_id = file_id;
        this.file_path = file_path;
        this.replication_degree = replication_degree;
    }

    public void registerChunk(RequestedBackupFileChunk rbfc) {
        this.chunks.put(rbfc.getChunkNo(), rbfc);
    }

    public RequestedBackupFileChunk getChunk(int chunk_no) {
        return this.chunks.getOrDefault(chunk_no, null_chunk_backup);
    }

    public String getFileId() {
        return file_id;
    }

    public String getFilePath() {
        return file_path;
    }

    public int getReplicationDegree() {
        return replication_degree;
    }

    public int getNrChunks() {
        return this.chunks.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("file path: ").append(this.file_path).append("\n");
        sb.append("file id: ").append(this.file_id).append("\n");
        sb.append("desired replication degree: ").append(this.replication_degree).append("\n");

        sb.append("chunks (").append(this.getNrChunks()).append("):\n");
        for (RequestedBackupFileChunk chunk : this.chunks.values()) {
            sb.append(chunk).append("\n");
        }
        sb.append("\n");

        return sb.toString();
    }
}
