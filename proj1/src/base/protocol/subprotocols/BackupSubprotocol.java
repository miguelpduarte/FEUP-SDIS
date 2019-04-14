package base.protocol.subprotocols;

import base.persistentstate.FileIdMapper;
import base.protocol.SynchronizedRunner;
import base.protocol.task.DeleteTask;
import base.protocol.task.EnhancedPutchunkTask;
import base.protocol.task.PutchunkTask;
import base.protocol.task.TaskManager;
import base.protocol.task.extendable.ITaskObserver;
import base.protocol.task.extendable.ObservableTask;
import base.protocol.task.extendable.Task;
import base.storage.requested.RequestedBackupFileChunk;
import base.storage.requested.RequestedBackupsState;

import java.util.concurrent.ConcurrentHashMap;

public class BackupSubprotocol extends SynchronizedRunner implements ITaskObserver {
    private static final int MAX_RUNNING_PUTCHUNK_TASKS = 10;

    private final String file_name;
    private final String file_id;
    private final int replication_degree;
    private final byte[][] chunks_data;
    private final ConcurrentHashMap<Integer, Task> running_tasks = new ConcurrentHashMap<>();
    private final int last_chunk_no;
    private final boolean is_enhanced_version;
    // Access to these fields must be synchronized
    private int last_running_chunk_no;
    private int n_backed_up;

    public BackupSubprotocol(String file_name, String file_id, int replication_degree, byte[][] chunks_data, boolean is_enhanced_version) {
        this.file_name = file_name;
        this.file_id = file_id;
        this.replication_degree = replication_degree;
        this.chunks_data = chunks_data;
        this.last_chunk_no = chunks_data.length;
        this.is_enhanced_version = is_enhanced_version;
        this.last_running_chunk_no = 0;

        launchInitialTasks();
    }

    private synchronized int getLastRunningChunkNo() {
        return this.last_running_chunk_no;
    }

    private synchronized void incrementLastRunningChunkNo() {
        this.last_running_chunk_no++;
    }

    private synchronized void incrementNrBackedUpChunks() {
        this.n_backed_up++;
    }

    private synchronized int getNrBackedUpChunks() {
        return this.n_backed_up;
    }

    private void stopAllTasks() {
        // Iterate over the hashmap keys and unregister all of the tasks. Print "not success"
        System.out.println("Stopping all of the tasks because one was not successful");
        this.running_tasks.values().forEach(Task::stopTask);
        this.running_tasks.clear();

        // Unregistering file from requested backups
        RequestedBackupsState.getInstance().unregisterRequestedFile(this.file_id);
        final DeleteTask t = new DeleteTask(this.file_id);
        TaskManager.getInstance().registerTask(t);
        t.start();
        FileIdMapper.getInstance().removeFile(this.file_name);

        System.out.println("All tasks stopped.");
        System.out.printf("-->Backup of file with id %s unsuccessful. Running tasks terminated, process aborted and Delete subprotocol launched to remove partial backup.\n", this.file_id);
    }

    @Override
    public void notifyEnd(boolean success, int task_id) {
        if (!this.isRunning()) {
            return;
        }

        if (!success) {
            System.out.printf("Task for chunk %d was not successful.\n", task_id);
            this.stopRunning();
            this.stopAllTasks();
        } else {
            // Task was successful
            this.incrementNrBackedUpChunks();

            final int nr_backed_up_chunks = this.getNrBackedUpChunks();
            displayProgressBar(nr_backed_up_chunks);

            if (nr_backed_up_chunks == this.last_chunk_no) {
                System.out.printf("-->File with id %s successfully backed up!!!\n", this.file_id);
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

        System.out.printf("Backup Progress: (%05d/%05d)", nr_backed_up_chunks, this.last_chunk_no);
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

        System.out.printf("-->Launching backup task for chunk_no %03d. #Running tasks: %03d\n", last_running_chunk_no, this.running_tasks.size());

        ObservableTask ot;
        if (this.is_enhanced_version) {
            ot = new EnhancedPutchunkTask(file_id, last_running_chunk_no, replication_degree, chunks_data[last_running_chunk_no]);
        } else {
            ot = new PutchunkTask(file_id, last_running_chunk_no, replication_degree, chunks_data[last_running_chunk_no]);
        }
        ot.observe(this);
        TaskManager.getInstance().registerTask(ot);
        RequestedBackupsState.getInstance().getRequestedFileBackupInfo(file_id).registerChunk(new RequestedBackupFileChunk(file_id, last_running_chunk_no, replication_degree));
        this.running_tasks.put(last_running_chunk_no, ot);
        ot.start();

        this.incrementLastRunningChunkNo();
    }
}
