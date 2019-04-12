package base.storage.stored;

import base.ProtocolDefinitions;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkBackupInfo implements Serializable {
    private final String file_id;
    private final int chunk_no;
    private final int replication_degree;
    private final ConcurrentHashMap<String, Boolean> replicators = new ConcurrentHashMap<>();
    private final int size_bytes;

    public ChunkBackupInfo(String file_id, int chunk_no, int replication_degree, int size_bytes) {
        this.file_id = file_id;
        this.chunk_no = chunk_no;
        this.replication_degree = replication_degree;
        // This own peer also counts (thus the count starts at 1, with the own peer being the storer)
        this.replicators.put(ProtocolDefinitions.SERVER_ID, ProtocolDefinitions.MOCK_HASHMAP_SET_VALUE);
        this.size_bytes = size_bytes;
    }

    public void addReplicator(String peer_id) {
        this.replicators.put(peer_id, ProtocolDefinitions.MOCK_HASHMAP_SET_VALUE);
        // System.out.printf("INC->Peer:%s! Chunk with file_id '%s' and no '%d' was now backed up by %d peers (replication degree of %d)\n", peer_id, this.file_id, this.chunk_no, this.replicators.size(), this.replication_degree);
    }

    public void removeReplicator(String peer_id) {
        this.replicators.remove(peer_id);
        // System.out.printf("DEC->Peer:%s! Chunk with file_id '%s' and no '%d' was now backed up by %d peers (replication degree of %d)\n", peer_id, this.file_id, this.chunk_no, this.replicators.size(), this.replication_degree);
    }

    public boolean isReplicated() {
        return this.replicators.size() >= this.replication_degree;
    }

    public boolean isOverReplicated() {
        return this.replicators.size() > this.replication_degree;
    }

    public String getFileId() {
        return file_id;
    }

    public int getChunkNo() {
        return chunk_no;
    }

    public int getSizeBytes() {
        return size_bytes;
    }

    public int getReplicationDegree() {
        return replication_degree;
    }

    @Override
    public String toString() {
        return String.format(
                "file_id: %s, chunk_no: %d,\nreplication degree: %d, #replicators: %d, size(bytes): %d\ndiff to replication degree: %d\n",
                this.file_id, this.chunk_no,
                this.replication_degree, this.replicators.size(), this.size_bytes,
                this.getDiffToDesiredReplicationDegree());
    }

    public int getDiffToDesiredReplicationDegree() {
        return this.replicators.size() - this.replication_degree;
    }
}
