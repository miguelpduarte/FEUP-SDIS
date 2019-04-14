package base.storage.stored;

import base.ProtocolDefinitions;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChunkBackupState implements Serializable {
    private static ChunkBackupState instance = new ChunkBackupState();

    public static ChunkBackupState getInstance() {
        return instance;
    }

    public static void setInstance(ChunkBackupState instance) {
        ChunkBackupState.instance = instance;
    }

    private ChunkBackupState() {
    }

    private final ConcurrentHashMap<String, ChunkBackupInfo> backed_up_chunks_info = new ConcurrentHashMap<>();

    private static final ChunkBackupInfo null_chunk_backup_info = new NullChunkBackupInfo();

    public void registerBackup(String file_id, int chunk_no, int replication_degree, int size_bytes) {
        final String map_key = ProtocolDefinitions.calcChunkHash(file_id, chunk_no);
        if (this.backed_up_chunks_info.containsKey(map_key)) {
            return;
        }
        this.backed_up_chunks_info.put(map_key, new ChunkBackupInfo(file_id, chunk_no, replication_degree, size_bytes));
    }

    public ChunkBackupInfo getChunkBackupInfo(String file_id, int chunk_no) {
        return this.backed_up_chunks_info.getOrDefault(ProtocolDefinitions.calcChunkHash(file_id, chunk_no), null_chunk_backup_info);
    }

    public void unregisterBackup(ChunkBackupInfo chunk_backup_info) {
        this.backed_up_chunks_info.remove(ProtocolDefinitions.calcChunkHash(chunk_backup_info.getFileId(), chunk_backup_info.getChunkNo()));
    }

    public void unregisterBackup(String file_id, int chunk_no) {
        this.backed_up_chunks_info.remove(ProtocolDefinitions.calcChunkHash(file_id, chunk_no));
    }

    public boolean isChunkBackedUp(String file_id, int chunk_no) {
        return this.backed_up_chunks_info.containsKey(ProtocolDefinitions.calcChunkHash(file_id, chunk_no));
    }

    /**
     * Calculates the best chunks to remove, in order
     *
     * @return The best chunks to remove for the reclaim protocol, ordered by how good they are. It is a Stream and thus should be consumed using Stream.findFirst() until the space restriction is respected.
     */
    public List<ChunkBackupInfo> getChunksCandidateForRemoval() {
        long start_time = System.currentTimeMillis();

        final Stream<ChunkBackupInfo> overReplicated = this.backed_up_chunks_info.values()
                .stream()
                .filter(ChunkBackupInfo::isOverReplicated)
                // Sorting by the difference between the desired and perceived replication degree
                .sorted(Comparator.comparingInt(ChunkBackupInfo::getDiffToDesiredReplicationDegree).reversed());

        final Stream<ChunkBackupInfo> underReplicated = this.backed_up_chunks_info.values()
                .stream()
                .filter(cbi -> !cbi.isOverReplicated())
                .sorted(Comparator.comparingInt(ChunkBackupInfo::getSizeBytes));

        final Stream<ChunkBackupInfo> candidatesForRemoval = Stream.concat(overReplicated, underReplicated);

        // candidatesForRemoval.forEach(chunkBackupInfo -> System.out.println("chunkBackupInfo = " + chunkBackupInfo)); // DBG Line -> Careful as this consumes the Stream

        List<ChunkBackupInfo> output = candidatesForRemoval.collect(Collectors.toList());

        System.out.println("getChunksCandidateForRemoval::Time elapsed: " + (System.currentTimeMillis() - start_time));

        return output;
    }

    public Collection<ChunkBackupInfo> getAllBackedUpChunksInfo() {
        return this.backed_up_chunks_info.values();
    }
}
