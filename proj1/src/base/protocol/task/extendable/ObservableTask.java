package base.protocol.task.extendable;

public abstract class ObservableTask extends TaskWithChunkNo {
    private ITaskObserver observer = new NullTaskObserver();

    public ObservableTask(String file_id, int chunk_no) {
        super(file_id, chunk_no);
    }

    public void observe(ITaskObserver observer) {
        this.observer = observer;
    }

    protected final void notifyObserver(boolean success) {
        this.observer.notifyEnd(success, this.chunk_no);
    }
}
