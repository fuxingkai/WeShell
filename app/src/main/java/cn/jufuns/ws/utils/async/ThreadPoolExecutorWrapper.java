package cn.jufuns.ws.utils.async;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class encapsulates 3 kinds of methods to "execute" code in background
 * thread, "execute" code in UI thread and "schedule" code to be run in the future.
 */

// an asynchronous task executor(thread pool)
public class ThreadPoolExecutorWrapper {
    private ExecutorService mThreadPoolExecutor;
    private ScheduledThreadPoolExecutor mScheduledThreadPoolExecutor;
    private Handler mMainHandler;
    private TaskQueue mActionQueue;

    public ThreadPoolExecutorWrapper(int activeThreadCount, int maxThreadCount, int maxScheTaskThread) {
        mThreadPoolExecutor = new ThreadPoolExecutor(activeThreadCount, maxThreadCount,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new MyThreadFactory(Thread.MIN_PRIORITY));

        if (maxScheTaskThread > 0) {
            mScheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(maxScheTaskThread);
        }

        mMainHandler = new Handler(Looper.getMainLooper());
        mActionQueue = new TaskQueue(Async.class.getName());
        mActionQueue.start();
    }

    public void executeTask(Runnable task) {
        mThreadPoolExecutor.execute(task);
    }

    public <T> Future<T> submitTask(Callable<T> task) {
        return mThreadPoolExecutor.submit(task);
    }

    public void scheduleTask(long delay, Runnable task) {
        mScheduledThreadPoolExecutor.schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    public void scheduleTaskAtFixedRateIgnoringTaskRunningTime(long initialDelay, long period, Runnable task) {
        mScheduledThreadPoolExecutor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    public void scheduleTaskAtFixedRateIncludingTaskRunningTime(long initialDelay, long period, Runnable task) {
        mScheduledThreadPoolExecutor.scheduleWithFixedDelay(task, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    public boolean removeScheduledTask(Runnable task) {
        return mScheduledThreadPoolExecutor.remove(task);
    }

    public void scheduleTaskOnUiThread(long delay, Runnable task) {
        mMainHandler.postDelayed(task, delay);
    }

    public void removeScheduledTaskOnUiThread(Runnable task) {
        mMainHandler.removeCallbacks(task);
    }

    public void runTaskOnUiThread(Runnable task) {
        mMainHandler.post(task);
    }

    public Handler getMainHandler() {
        return mMainHandler;
    }

    public void scheduleOnQueue(Runnable task) {
        mActionQueue.scheduleTask(task);
    }

    public void shutdown() {
        if (mThreadPoolExecutor != null) {
            mThreadPoolExecutor.shutdown();
            mThreadPoolExecutor = null;
        }

        if (mScheduledThreadPoolExecutor != null) {
            mScheduledThreadPoolExecutor.shutdown();
            mScheduledThreadPoolExecutor = null;
        }

        if (mActionQueue != null) {
            mActionQueue.stopTaskQueue();
        }
    }
}
