package base.storage;

import base.ThreadManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class Restorer {
    private final String file_name;
    private final String file_id;
    private final int n_chunks_to_backup;
    private final BlockingQueue<RestorerData> chunks_to_store = new LinkedBlockingQueue<>();
    private Future restoring_thread;
    // Access to this must be synchronized
    private int nr_backed_up_chunks;

    public Restorer(String file_name, String file_id, int n_chunks_to_backup) {
        this.file_name = file_name;
        this.file_id = file_id;
        this.n_chunks_to_backup = n_chunks_to_backup;
        this.nr_backed_up_chunks = 0;

        this.startWriter();
    }

    private void startWriter() {
        this.restoring_thread = ThreadManager.getInstance().executeLater(this::writer);
    }

    private void writer() {
        if (!StorageManager.getInstance().createEmptyFileForRestore(file_name)) {
            System.out.printf("Restorer.writer::Error creating empty file for file_name '%s', aborting!\n", file_name);
        }

        while (this.getNrBackedUpChunks() < this.nr_backed_up_chunks) {
            try {
                RestorerData chunk_to_store = this.chunks_to_store.take();
                System.out.printf("Writing to file, %d of %d\n", this.getNrBackedUpChunks(), this.nr_backed_up_chunks);
                if (StorageManager.getInstance().writeChunkToFullFile(file_name, chunk_to_store.getData(), chunk_to_store.getChunkNo())) {
                    this.incrementNrBackedUpChunks();
                }
            } catch (InterruptedException ignored) {
            }
        }

        System.out.println("\tDBG:Writer thread done");
    }

    public void haltWriter() {
        this.restoring_thread.cancel(true);
    }

    private synchronized void incrementNrBackedUpChunks() {
        this.nr_backed_up_chunks++;
    }

    private synchronized int getNrBackedUpChunks() {
        return this.nr_backed_up_chunks;
    }

    /**
     * Adds a chunk's data to the queue of chunks to store. Must be done sequentially in order to ensure that the file's data is not broken
     *
     * @param chunk_data
     */
    public void addChunk(byte[] chunk_data, int chunk_no) {
        this.chunks_to_store.add(new RestorerData(chunk_data, chunk_no));
    }

    private void addChunk(RestorerData chunk_to_store) {
        this.chunks_to_store.add(chunk_to_store);
    }
}
