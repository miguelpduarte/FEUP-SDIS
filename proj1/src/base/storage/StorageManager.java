package base.storage;

import base.ProtocolDefinitions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class StorageManager {
    private static StorageManager instance = new StorageManager();
    private String backup_dirname;
    private String restored_dirname;

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
        this.backup_dirname = peer_dirname + "/" + ProtocolDefinitions.BACKUP_DIRNAME + "/";
        this.restored_dirname = peer_dirname + "/" + ProtocolDefinitions.RESTORED_DIRNAME + "/";

        // Creating the actual directories:
        new File(this.backup_dirname).mkdirs();
        new File(this.restored_dirname).mkdirs();

        //TODO: Check already stored chunks and insert that data into this.stored_chunks
    }

    public boolean storeChunk(String file_id, int chunkno, byte[] data, int data_length) {
        final String file_chunk_hash = String.format("%s_chk%d", file_id, chunkno);
        if (this.stored_chunks.containsKey(file_chunk_hash)) {
            System.out.printf("DBG:StorageManager.storeChunk::The file chunk with hash '%s' was already stored\n", file_chunk_hash);
            return true;
        }

        System.out.printf("StorageManager.storeChunk::Storing the file with hash '%s'\n", file_chunk_hash);

        // Ensuring that the parent directories exist so that the FileOutputStream can create the file correctly
        final String chunk_parent_dir = String.format("%s/%s/", this.backup_dirname, file_id);
        new File(chunk_parent_dir).mkdirs();

        final String chunk_path = String.format("%schk%d", chunk_parent_dir, chunkno);

        try (FileOutputStream fos = new FileOutputStream(chunk_path)) {
            fos.write(data, 0, data_length);
            //fos.close(); There is unnecessary since the instance of "fos" is created inside a try-with-resources statement, which will automatically close the FileOutputStream in case of failure
            // See https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
            this.stored_chunks.put(file_chunk_hash, CHUNK_STORED);
            return true;
        } catch (IOException e) {
            System.out.printf("StorageManager.storeChunk::Error in storing file chunk with hash '%s'\n", file_chunk_hash);
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] readFromFile(String file_path) throws IOException {
        // See https://stackoverflow.com/questions/858980/file-to-byte-in-java

        File f = new File(file_path);
        long file_size = f.length();

        // Arrays in Java have to be created using int and not long
        // Thus, checking if there is no "size overflow"
        if (file_size > Integer.MAX_VALUE) {
            throw new IOException("File too large to read into a byte array!");
        }

        byte[] file_data = new byte[(int) file_size];

        try (FileInputStream is = new FileInputStream(f)) {
            int offset = 0, n_bytes_read;
            while (offset < file_data.length && (n_bytes_read = is.read(file_data, offset, file_data.length - offset)) >= 0) {
                offset += n_bytes_read;
            }

            // Ensuring that all of the bytes have been read
            if (offset < file_data.length) {
                throw new IOException("Could not read full contents of file " + file_path);
            }

            return file_data;
        }
    }

    /**
     * For use in restoring
     * @param file_name
     */
    public boolean writeToFileEnd(String file_name, byte[] data) {
        final String file_path = String.format("%s/%s", this.restored_dirname, file_name);

        try (FileOutputStream fos = new FileOutputStream(file_path, true)) {
            fos.write(data, 0, data.length);
            return true;
        } catch (IOException e) {
            System.out.printf("StorageManager.writeToFileEnd::Error in appending to file '%s'\n", file_name);
            e.printStackTrace();
            return false;
        }
    }
}
