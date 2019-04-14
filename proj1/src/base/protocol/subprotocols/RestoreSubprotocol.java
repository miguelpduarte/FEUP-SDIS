package base.protocol.subprotocols;

import base.protocol.SynchronizedRunner;
import base.protocol.task.GetchunkTask;
import base.protocol.task.TaskManager;
import base.protocol.task.extendable.ITaskObserver;
import base.protocol.task.extendable.ObservableTask;
import base.protocol.task.extendable.Task;
import base.storage.Restorer;
import base.storage.requested.RequestedBackupsState;

import java.util.concurrent.ConcurrentHashMap;

public class RestoreSubprotocol extends SynchronizedRunner implements ITaskObserver {
    private static final int MAX_RUNNING_PUTCHUNK_TASKS = 10;

    private final String file_name;
    private final String file_id;
    private final ConcurrentHashMap<Integer, Task> running_tasks = new ConcurrentHashMap<>();
    private final int last_chunk_no;
    private final boolean is_enhanced_version;
    // Access to these fields must be synchronized
    private int last_running_chunk_no = 0;
    private int n_restored = 0;
    // private final Restorer restorer;

    public RestoreSubprotocol(String file_name, String file_id, boolean is_enhanced_version) {
        this.file_name = file_name;
        this.file_id = file_id;
        this.is_enhanced_version = is_enhanced_version;

        this.last_chunk_no = RequestedBackupsState.getInstance().getRequestedFileBackupInfo(file_id).getNrChunks();

        // this.restorer = initRestorer();
        launchInitialTasks();
    }

    private Restorer initRestorer() {
        return new Restorer(this.file_name, this.file_id, this.last_chunk_no);
    }

    private void haltRestorer() {
        // restorer.haltWriter();
    }

    private synchronized int getLastRunningChunkNo() {
        return this.last_running_chunk_no;
    }

    private synchronized void incrementLastRunningChunkNo() {
        this.last_running_chunk_no++;
    }

    private synchronized void incrementNrRestoredChunks() {
        this.n_restored++;
    }

    private synchronized int getNrRestoredChunks() {
        return this.n_restored;
    }

    private void stopAllTasks() {
        // this.haltRestorer();
        // TODO
        // Iterate over the hashmap keys and unregister all of the tasks. Print "not success"
        System.out.println("Stopping all of the tasks because one was not successful");
        this.running_tasks.values().forEach(Task::stopTask);
        this.running_tasks.clear();

        System.out.println("All tasks stopped.");
        System.out.printf("-->Restore of file with id %s unsuccessful. Running tasks terminated and process aborted.\n", this.file_id);
    }

    @Override
    public void notifyEnd(boolean success, int task_id) {
        if (!this.isRunning()) {
            System.out.println("Subprotocol no longer running"); // TODO remove?
            return;
        }

        if (!success) {
            System.out.printf("Task for chunk %d was not successful.\n", task_id);
            this.stopRunning();
            this.stopAllTasks();
        } else {
            // Task was successful
            this.incrementNrRestoredChunks();

            final int nr_restored_chunks = this.getNrRestoredChunks();
            displayProgressBar(nr_restored_chunks);

            if (nr_restored_chunks == this.last_chunk_no) {
                System.out.printf("-->File with id %s successfully restored!1!\n", this.file_id);
                this.stopRunning();
                return;
            }
            // (This task is no longer being executed)
            this.running_tasks.remove(task_id);
            this.launchNextTask();
        }
    }

    private static final int PROGRESS_BAR_SIZE = 20;
    protected void displayProgressBar(int nr_backed_up_chunks) {
        final int progress = (int) (((double)nr_backed_up_chunks / this.last_chunk_no) * PROGRESS_BAR_SIZE);

        System.out.printf("Restore Progress: (%05d/%05d)", nr_backed_up_chunks, this.last_chunk_no);
        int i = 0;
        for (; i < progress; ++i) {
            System.out.print("=");
        }
        for (; i < 20; ++i) {
            System.out.print("_");
        }
        System.out.println();
    }

    private void launchInitialTasks() {
        while (this.running_tasks.size() < MAX_RUNNING_PUTCHUNK_TASKS && this.getLastRunningChunkNo() < this.last_chunk_no) {
            this.launchNextTask();
        }
    }

    private synchronized void launchNextTask() {
        if (this.running_tasks.size() >= MAX_RUNNING_PUTCHUNK_TASKS || this.getLastRunningChunkNo() >= this.last_chunk_no) {
            // Preventing launching more tasks than desired
            return;
        }

        final int last_running_chunk_no = this.getLastRunningChunkNo();

        System.out.printf("-->Launching restore task for chunk_no %03d. #Running tasks: %03d\n", last_running_chunk_no, this.running_tasks.size());

        ObservableTask ot = null;
        if (this.is_enhanced_version) {
            //ot = new EnhancedGetchunkTask(file_id, last_running_chunk_no);
        } else {
            ot = new GetchunkTask(file_id, file_name, last_running_chunk_no);
        }
        ot.observe(this);
        TaskManager.getInstance().registerTask(ot);
        this.running_tasks.put(last_running_chunk_no, ot);

        this.incrementLastRunningChunkNo();
    }
}
