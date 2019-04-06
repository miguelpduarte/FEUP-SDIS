package base.storage;

public class ChunkBackupInfo {
    private final String file_id;
    private final int chunk_no;
    private final int replication_degree;
    private int n_stored;

    public ChunkBackupInfo(String file_id, int chunk_no, int replication_degree, int n_stored) {
        this.file_id = file_id;
        this.chunk_no = chunk_no;
        this.replication_degree = replication_degree;
        this.n_stored = n_stored;
    }

    public ChunkBackupInfo(String file_id, int chunk_no, int replication_degree) {
        // This own peer also counts (thus the count starts at 1)
        this(file_id, chunk_no, replication_degree, 1);
    }

    public void incrementNumStored() {
        this.n_stored++;
        System.out.printf("INC! Chunk with file_id '%s' and no '%d' was now backed up by %d peers (replication degree of %d)\n", this.file_id, this.chunk_no, this.n_stored, this.replication_degree);
    }

    public void decrementNumStored() {
        this.n_stored--;
        System.out.printf("DEC! Chunk with file_id '%s' and no '%d' was now backed up by %d peers (replication degree of %d)\n", this.file_id, this.chunk_no, this.n_stored, this.replication_degree);
    }

    public boolean isOverReplicationDegree() {
        return this.n_stored >= this.replication_degree;
    }

    public String getFileId() {
        return file_id;
    }

    public int getChunkNo() {
        return chunk_no;
    }
}
