package base.storage;

import base.ProtocolDefinitions;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class StorageManager {
    private static StorageManager instance = new StorageManager();

    public static StorageManager getInstance() {
        return instance;
    }

    // Using a concurrent hash map because several threads might operate over it simultaneously
    private final ConcurrentHashMap<String, Object> stored_chunks = new ConcurrentHashMap<>();

    // Dummy value to associate with an Object in the backing Map (idea taken from Java's implementation of HashSet)
    private static final Object CHUNK_STORED = new Object();

    private StorageManager() {
    }

    public void initStorage() {
        // Create the directories for this peer (see Moodle):
        // peerX/backup and peerX/restored
        final String peer_dirname = String.format("peer%s", ProtocolDefinitions.SERVER_ID);
        final String backup_dirname = peer_dirname + "/" + ProtocolDefinitions.BACKUP_DIRNAME + "/";
        final String restored_dirname = peer_dirname + "/" + ProtocolDefinitions.RESTORED_DIRNAME + "/";

        // Creating the actual directories:
        new File(backup_dirname).mkdirs();
        new File(restored_dirname).mkdirs();
    }

    public boolean storeChunk(String file_id, int chunkno, byte[] data) {
        System.out.println("StorageManager.storeChunk");
        // TODO Discuss to confirm:
        // This method does not need to be synchronized since the threads will always write to separate files and the concurrent accesses
        // to class fields are being done using a ConcurrentHashMap so it should be fine

        String file_chunk_hash = String.format("%s_chk%d", file_id, chunkno);
        if (this.stored_chunks.containsKey(file_chunk_hash)) {
            System.out.printf("DBG:StorageManager.storeChunk::The file chunk with hash '%s' was already stored in the System :)\n", file_chunk_hash);
            return true;
        }

        System.out.printf("DBG:StorageManager.storeChunk::Storing the file with hash '%s'\n", file_chunk_hash);

        // TODO: Actually store it!

        this.stored_chunks.put(file_chunk_hash, CHUNK_STORED);

        // TODO: Actually return based on the success or not
        return true;
    }
}
