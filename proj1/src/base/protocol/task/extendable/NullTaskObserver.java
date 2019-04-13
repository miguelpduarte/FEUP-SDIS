package base.protocol.task.extendable;

public class NullTaskObserver implements ITaskObserver {
    @Override
    public void notifyEnd(boolean success, int task_id) {
    }
}
