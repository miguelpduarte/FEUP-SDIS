package base;

import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ThreadManager {
    private final int N_THREADS = 7;
    private ScheduledThreadPoolExecutor scheduled_executor;

    private static ThreadManager instance = new ThreadManager();

    public static ThreadManager getInstance() {
        return instance;
    }

    private ThreadManager() {
        this.scheduled_executor = new ScheduledThreadPoolExecutor(N_THREADS);
    }

    public void executeLater(Runnable r) {
        this.scheduled_executor.execute(r);
    }

    public ScheduledFuture executeLater(Runnable r, long seconds) {
        return this.scheduled_executor.schedule(r, seconds, SECONDS);
    }
}
