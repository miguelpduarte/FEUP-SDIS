package base.storage;

import base.Keyable;
import base.ThreadManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class Restorer implements Keyable {
    private final String file_name;
    private final String file_id;
    private final BlockingQueue<byte []> chunks_to_store = new LinkedBlockingQueue<>();
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
        while (this.writer_running) {
            try {
                byte[] chunk_to_store = this.chunks_to_store.take();
                StorageManager.getInstance().writeToFileEnd(this.file_name, chunk_to_store);
            } catch (InterruptedException ignored) {
            }
        }

        System.out.println("\tDBG:Writer thread done");
    }

    public void haltWriter() {
        this.restoring_thread.cancel(true);
        unregister();
    }

    public void stopWriter() {
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
     * @param chunk_data
     */
    public void addChunk(byte[] chunk_data) {
        this.chunks_to_store.add(chunk_data);
    }
}
