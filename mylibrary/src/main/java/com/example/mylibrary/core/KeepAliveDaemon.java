package com.example.mylibrary.core;

import android.os.Handler;
import android.util.Log;

import com.example.mylibrary.IMCoreSDK;
import com.example.mylibrary.utils.MBSimpleTimer;
import com.example.mylibrary.utils.IMThreadPool;

import java.util.Observer;
import java.util.concurrent.atomic.AtomicLong;

public class KeepAliveDaemon {
    private final static String TAG = KeepAliveDaemon.class.getSimpleName();

    private static KeepAliveDaemon instance = null;

    public static int KEEP_ALIVE_INTERVAL = 15000;//3000;//1000;
    public static int NETWORK_CONNECTION_TIME_OUT = KEEP_ALIVE_INTERVAL + 5000;//20 * 1000;//10 * 1000;
    public static int NETWORK_CONNECTION_TIME_OUT_CHECK_INTERVAL = 2 * 1000;

    private boolean keepAliveRunning = false;
    private final AtomicLong lastGetKeepAliveResponseFromServerTimstamp = new AtomicLong(0);
    private Observer networkConnectionLostObserver = null;

    private Handler keepAliveHandler = null;
    private Runnable keepAliveRunnable = null;
    private boolean keepAliveTaskExcuting = false;
    private boolean keepAliveWillStop = false;

    private MBSimpleTimer keepAliveTimeoutTimer = null;

    private boolean init = false;

    /** !本属性仅作DEBUG之用：DEBUG事件观察者 */
    private Observer debugObserver;

    public static KeepAliveDaemon getInstance() {
        if(instance == null)
            instance = new KeepAliveDaemon();
        return instance;
    }

    private KeepAliveDaemon() {
        init();
    }

    private void init() {
        if(init)
            return;

        keepAliveHandler = new Handler();
        keepAliveRunnable = () -> {
            if(!keepAliveTaskExcuting) {
                IMThreadPool.runInBackground(() -> {
                    final int code = doKeepAlive();
                    IMThreadPool.runOnMainThread(() -> onKeepAlive(code));
                });
            }
        };

        keepAliveTimeoutTimer = new MBSimpleTimer(NETWORK_CONNECTION_TIME_OUT_CHECK_INTERVAL){
            @Override
            protected void doAction(){
                if(IMCoreSDK.DEBUG)
                    Log.i(TAG, "【IMCORE-TCP】心跳[超时检查]线程执行中...");

                doTimeoutCheck();
            }
        };
        keepAliveTimeoutTimer.init();

        init = true;
    }

    private int doKeepAlive() {
        keepAliveTaskExcuting = true;
        if(IMCoreSDK.DEBUG)
            Log.i(TAG, "【IMCORE-TCP】心跳包[发送]线程执行中...");
//        int code = LocalDataSender.getInstance().sendKeepAlive();

        return -1;
    }

    private void onKeepAlive(int code) {
        // for DEBUG
        if(this.debugObserver != null)
            this.debugObserver.update(null, 2);

        boolean isInitialedForKeepAlive = isInitialedForKeepAlive();
        //## Bug FIX 20190513 v4.0.1 START
        if(isInitialedForKeepAlive)
            lastGetKeepAliveResponseFromServerTimstamp.set(System.currentTimeMillis());
        //## Bug FIX 20190513 v4.0.1 END

        keepAliveTaskExcuting = false;
        if(!keepAliveWillStop)
            keepAliveHandler.postDelayed(keepAliveRunnable, KEEP_ALIVE_INTERVAL);
    }

    private void doTimeoutCheck() {
        boolean isInitialedForKeepAlive = isInitialedForKeepAlive();
        if(!isInitialedForKeepAlive) {
            long now = System.currentTimeMillis();
            if(now - lastGetKeepAliveResponseFromServerTimstamp.longValue() >= NETWORK_CONNECTION_TIME_OUT) {
                if(IMCoreSDK.DEBUG)
                    Log.w(TAG, "【IMCORE-TCP】心跳机制已判定网络断开，将进入断网通知和重连处理逻辑 ...");

                notifyConnectionLost();
                keepAliveWillStop = true;
            }
        }
    }

    private boolean isInitialedForKeepAlive() {
        return (lastGetKeepAliveResponseFromServerTimstamp.longValue() == 0);
    }

    public void notifyConnectionLost() {
        stop();
        if(networkConnectionLostObserver != null)
            networkConnectionLostObserver.update(null, null);
    }

    public void stop() {
        keepAliveTimeoutTimer.stop();

        keepAliveHandler.removeCallbacks(keepAliveRunnable);
        keepAliveRunning = false;
        keepAliveWillStop = false;
        lastGetKeepAliveResponseFromServerTimstamp.set(0);

        // for DEBUG
        if(this.debugObserver != null)
            this.debugObserver.update(null, 0);
    }

    public void start(boolean immediately) {
        stop();
        keepAliveHandler.postDelayed(keepAliveRunnable, immediately ? 0 : KEEP_ALIVE_INTERVAL);
        keepAliveRunning = true;
        keepAliveWillStop = false;

        keepAliveTimeoutTimer.start(immediately);

        // for DEBUG
        if(this.debugObserver != null)
            this.debugObserver.update(null, 1);
    }

    public boolean isKeepAliveRunning() {
        return keepAliveRunning;
    }

    public boolean isInit() {
        return init;
    }

    public void updateGetKeepAliveResponseFromServerTimstamp() {
        lastGetKeepAliveResponseFromServerTimstamp.set(System.currentTimeMillis());
    }

    public void setNetworkConnectionLostObserver(Observer networkConnectionLostObserver) {
        this.networkConnectionLostObserver = networkConnectionLostObserver;
    }

    /**
     * !本方法仅用于DEBUG，开发者无需关注！
     *
     * @return DEBUG事件观察者
     */
    public Observer getDebugObserver() {
        return debugObserver;
    }

    /**
     * !本方法仅用于DEBUG，开发者无需关注！
     *
     * @param debugObserver DEBUG事件观察者
     */
    public void setDebugObserver(Observer debugObserver) {
        this.debugObserver = debugObserver;
    }
}
