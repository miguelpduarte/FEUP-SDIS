package base.storage.requested;

import base.ProtocolDefinitions;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class RequestedBackupFileChunk implements Serializable {
    private final String file_id;
    private final int chunk_no;
    private final int replication_degree;
    private final ConcurrentHashMap<String, Boolean> replicators = new ConcurrentHashMap<>();

    public RequestedBackupFileChunk(String file_id, int chunk_no, int replication_degree) {
        this.file_id = file_id;
        this.chunk_no = chunk_no;
        this.replication_degree = replication_degree;
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

    public int getReplicationDegree() {
        return replication_degree;
    }

    @Override
    public String toString() {
        return String.format(
                "\tchunk_no: %d,\n\t#replicators: %d\n\tdiff to replication degree: %d\n",
                this.chunk_no,
                this.replicators.size(),
                this.getDiffToDesiredReplicationDegree());
    }

    public int getDiffToDesiredReplicationDegree() {
        return this.replicators.size() - this.replication_degree;
    }
}
