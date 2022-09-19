package com.example.mylibrary.core;

import android.util.Log;

import com.example.mylibrary.utils.MBThreadPoolExecutor;

public class AutoReLoginDaemon {
    rivate final static String TAG = AutoReLoginDaemon.class.getSimpleName();
    private static AutoReLoginDaemon instance = null;
    public static int AUTO_RE$LOGIN_INTERVAL = 3000;//2000;

    private Handler handler = null;
    private Runnable runnable = null;
    private boolean autoReLoginRunning = false;
    private boolean _excuting = false;
    private boolean init = false;

    /** !本属性仅作DEBUG之用：DEBUG事件观察者 */
    private Observer debugObserver;

    public static AutoReLoginDaemon getInstance() {
        if (instance == null)
            instance = new AutoReLoginDaemon();
        return instance;
    }

    private AutoReLoginDaemon() {
        init();
    }

    private void init() {
        if (init)
            return;

        handler = new Handler();
        runnable = () -> {
            if (!_excuting) {
                MBThreadPoolExecutor.runInBackground(() -> {
                    final int code = doSendLogin();
                    MBThreadPoolExecutor.runOnMainThread(() -> onSendLogin(code));
                });
            }
        };

        init = true;
    }

    private int doSendLogin() {
        _excuting = true;
        if (ClientCoreSDK.DEBUG)
            Log.d(TAG, "【IMCORE-TCP】自动重新登陆线程执行中, autoReLogin?" + ClientCoreSDK.autoReLogin + "...");
        int code = -1;
        if (ClientCoreSDK.autoReLogin)
            code = LocalDataSender.getInstance().sendLogin(ClientCoreSDK.getInstance().getCurrentLoginInfo());
        return code;
    }

    private void onSendLogin(int result) {
        // for DEBUG
        if(this.debugObserver != null)
            this.debugObserver.update(null, 2);

        if (result == 0) {
//			LocalUDPDataReciever.getInstance().startup();
        }

        _excuting = false;
        handler.postDelayed(runnable, AUTO_RE$LOGIN_INTERVAL);
    }

    public void stop() {
        handler.removeCallbacks(runnable);
        autoReLoginRunning = false;

        // for DEBUG
        if(this.debugObserver != null)
            this.debugObserver.update(null, 0);
    }

    public void start(boolean immediately) {
        stop();
        handler.postDelayed(runnable, immediately ? 0 : AUTO_RE$LOGIN_INTERVAL);
        autoReLoginRunning = true;

        // for DEBUG
        if(this.debugObserver != null)
            this.debugObserver.update(null, 1);
    }

    public boolean isAutoReLoginRunning() {
        return autoReLoginRunning;
    }

    public boolean isInit() {
        return init;
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
