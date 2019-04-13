package base.protocol.task.extendable;

public abstract class TaskWithChunkNo extends Task {
    protected final int chunk_no;

    public TaskWithChunkNo(String file_id, int chunk_no) {
        super(file_id);
        this.chunk_no = chunk_no;
    }
}
