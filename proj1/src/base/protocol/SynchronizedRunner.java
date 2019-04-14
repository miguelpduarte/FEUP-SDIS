package base.protocol;

public class SynchronizedRunner {
    // Must have synchronized access
    private boolean is_running = true;

    // is_running can be used for stopping the repeated processing of accumulated calls (especially to synchronized methods)
    protected synchronized void stopRunning() {
        this.is_running = false;
    }

    protected synchronized boolean isRunning() {
        return this.is_running;
    }
}
