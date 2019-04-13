package base.protocol.task;

public interface ITaskObserver {
    void notifyEnd(boolean success, int task_id);
}
