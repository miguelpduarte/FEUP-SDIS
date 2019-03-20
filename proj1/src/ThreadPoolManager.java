import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ThreadPoolManager {
    private final BlockingQueue<Runnable> queue;
    private final int N_CORE_THREADS = 3;
    private final int N_MAX_THREADS = 10;
    private final int KEEP_ALIVE_TIME = 3;
    private ThreadPoolExecutor executor;

    private static ThreadPoolManager instance = new ThreadPoolManager();

    public static ThreadPoolManager getInstance() {
        return instance;
    }

    private ThreadPoolManager() {
        this.queue = new LinkedBlockingQueue<>();
        this.executor = new ThreadPoolExecutor(N_CORE_THREADS, N_MAX_THREADS, KEEP_ALIVE_TIME, SECONDS, this.queue);
    }

    public void executeLater(Runnable r) {
        this.executor.execute(r);
    }
}
