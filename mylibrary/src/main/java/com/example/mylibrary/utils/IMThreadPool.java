package com.example.mylibrary.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class IMThreadPool {
    private static final String TAG = IMThreadPool.class.getSimpleName();

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final long KEEP_ALIVE_TIME = 30L;
    private static final int WAIT_COUNT = 128;

    private static ThreadPoolExecutor mExecutor = createThreadPoolExecutor();

    /**
     * 线程池创建类
     *
     * @return
     */
    private static ThreadPoolExecutor createThreadPoolExecutor() {
        if (mExecutor == null) {
            mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(WAIT_COUNT),
                    new IMThreadFactory("IMThreadPool", Thread.NORM_PRIORITY - 2),
                    new IMExceptionHandler());
        }
        return mExecutor;
    }

    public static class IMThreadFactory implements ThreadFactory {
        private AtomicInteger counter = new AtomicInteger(1);
        private String prefix = "";
        private int priority = Thread.NORM_PRIORITY;

        public IMThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        public IMThreadFactory(String prefix, int priority) {
            this.prefix = prefix;
            this.priority = priority;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, prefix + " #" + counter.getAndIncrement());
            thread.setDaemon(true);
            thread.setPriority(priority);
            return thread;
        }
    }

    private static class IMExceptionHandler extends ThreadPoolExecutor.AbortPolicy {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            Log.d(TAG, "rejectedExecution:" + r);
            Log.e(TAG, logAllThreadStackTrace().toString());
            if (!mExecutor.isShutdown()) {
                mExecutor.shutdown();
                mExecutor = null;
            }
            mExecutor = createThreadPoolExecutor();
        }
    }

    /**
     * 启动一个后台线程执行任务
     *
     * @param runnable
     */
    public static void runInBackground(Runnable runnable) {
        if (mExecutor == null) {
            createThreadPoolExecutor();
        }
        mExecutor.execute(runnable);
    }

    /**
     * 在Main线程执行任务
     *
     * @param runnable
     */
    public static void runOnMainThread(Runnable runnable) {
        if (isOnMainThread()) {
            runnable.run();
        } else {
            new Handler(Looper.getMainLooper()).post(runnable);
        }
    }

    public static boolean isOnMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    public static StringBuilder logAllThreadStackTrace() {
        StringBuilder builder = new StringBuilder();
        Map<Thread, StackTraceElement[]> liveThreads = Thread.getAllStackTraces();
        for (Iterator<Thread> i = liveThreads.keySet().iterator(); i.hasNext(); ) {
            Thread key = i.next();
            builder.append("Thread ").append(key.getName()).append("\n");
            StackTraceElement[] trace = liveThreads.get(key);
            for (int j = 0; j < trace.length; j++) {
                builder.append("\tat ").append(trace[j]).append("\n");
            }
        }
        return builder;
    }
}
