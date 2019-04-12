package base.storage;

import base.Keyable;
import base.ThreadManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class Restorer implements Keyable {
    private final String file_name;
    private final String file_id;
    private final BlockingQueue<RestorerData> chunks_to_store = new LinkedBlockingQueue<>();
    private Future restoring_thread;
    private boolean writer_running = true;

    public Restorer(String file_name, String file_id) {
        this.file_name = file_name;
        this.file_id = file_id;

        this.startWriter();
    }

    private void startWriter() {
        this.restoring_thread = ThreadManager.getInstance().executeLater(this::writer);
    }

    private void writer() {
        if (!StorageManager.getInstance().createEmptyFileForRestore(file_name)) {
            System.out.printf("Restorer.writer::Error creating empty file for file_name '%s', aborting!\n", file_name);
        }

        while (this.writer_running) {
            try {
                RestorerData chunk_to_store = this.chunks_to_store.take();
                System.out.println("Writing to file");
                StorageManager.getInstance().writeChunkToFullFile(file_name, chunk_to_store.getData(), chunk_to_store.getChunkNo());
            } catch (InterruptedException ignored) {
            }
        }

        System.out.println("\tDBG:Writer thread done");
    }

    public void haltWriter() {
        this.restoring_thread.cancel(true);
        unregister();
    }

    public synchronized void stopWriter() {
        System.out.println("Restorer.stopWriter");
        this.writer_running = false;
        unregister();
    }

    protected void unregister() {
        RestoreManager.getInstance().unregisterRestorer(this);
    }

    @Override
    public String toKey() {
        return this.file_id;
    }

    /**
     * Adds a chunk's data to the queue of chunks to store. Must be done sequentially in order to ensure that the file's data is not broken
     *
     * @param chunk_data
     */
    public void addChunk(byte[] chunk_data, int chunk_no) {
        this.chunks_to_store.add(new RestorerData(chunk_data, chunk_no));
    }
}
