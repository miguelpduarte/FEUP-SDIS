import java.util.concurrent.ConcurrentHashMap;

public class TaskManager {
    private static TaskManager instance = new TaskManager();

    private ConcurrentHashMap<String, Task> tasks = new ConcurrentHashMap<>();
    private int task_id = 0;

    public static TaskManager getInstance() {
        return instance;
    }

    private TaskManager() {
    }

    public Task getTask(Keyable k) {
        return tasks.get(k.toKey());
    }

    public void registerTask(Task t) {
        this.tasks.put(t.toKey(), t);
    }

    public void unregisterTask(Keyable k) {
        tasks.remove(k.toKey());
    }
}
