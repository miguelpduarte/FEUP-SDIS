package base.protocol.task;

public class NullTaskObserver implements ITaskObserver {
    @Override
    public void notifyEnd(boolean success, int task_id) {
    }
}
