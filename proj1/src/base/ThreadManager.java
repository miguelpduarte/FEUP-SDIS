package base;

import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ThreadManager {
    private final int N_THREADS = 19;
    private ScheduledThreadPoolExecutor scheduled_executor = new ScheduledThreadPoolExecutor(N_THREADS);

    private static ThreadManager instance = new ThreadManager();

    public static ThreadManager getInstance() {
        return instance;
    }

    private ThreadManager() {
    }

    public Future executeLater(Runnable r) {
        return this.scheduled_executor.submit(r);
    }

    public ScheduledFuture executeLater(Runnable r, long seconds) {
        return this.scheduled_executor.schedule(r, seconds, SECONDS);
    }

    public ScheduledFuture executeLaterMilis(Runnable r, long milliseconds) {
        return this.scheduled_executor.schedule(r, milliseconds, MILLISECONDS);
    }
}
