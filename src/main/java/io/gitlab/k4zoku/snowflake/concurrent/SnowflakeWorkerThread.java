package io.gitlab.k4zoku.snowflake.concurrent;

public class SnowflakeWorkerThread extends Thread {
    private final SnowflakeWorker worker;

    public SnowflakeWorkerThread(SnowflakeWorker worker, Runnable target) {
        super(target);
        this.setDaemon(true);
        this.worker = worker;
        setName("SnowflakeWorker-" + worker.getId());
    }

    public SnowflakeWorker getWorker() {
        return worker;
    }

    public static SnowflakeWorkerThread getCurrentThread() {
        Thread thread = Thread.currentThread();
        if (thread instanceof SnowflakeWorkerThread) {
            return (SnowflakeWorkerThread) thread;
        }
        throw new IllegalStateException("Current thread is not a SnowflakeWorkerThread");
    }
}
