package base.protocol.task.extendable;

public interface ITaskObserver {
    void notifyEnd(boolean success, int task_id);
}
