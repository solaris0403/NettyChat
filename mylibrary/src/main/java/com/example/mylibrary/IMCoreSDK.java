package com.example.mylibrary;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;

import com.example.mylibrary.core.KeepAliveDaemon;
import com.example.mylibrary.core.SocketProvider;
import com.example.mylibrary.event.ChatBaseEvent;
import com.example.mylibrary.event.ChatMessageEvent;
import com.example.mylibrary.event.MessageQoSEvent;
import com.example.mylibrary.message.IMessageHandler;
import com.example.mylibrary.network.INetworkListener;
import com.example.mylibrary.network.NetworkStateReceiver;
import com.example.mylibrary.utils.IMThreadPool;

public class IMCoreSDK {
    private final static String TAG = IMCoreSDK.class.getSimpleName();
    //调试日志开关
    public static boolean DEBUG = true;
    //是否在登陆成功后掉线时自动重新登陆线程中实质性发起登陆请求，
    // true表示将在线程运行周期中正常发起，否则不发起 （即关闭实质性的重新登陆请求）。
    public static boolean autoReLogin = true;

    //是否初始化
    private boolean _init = false;
    //是否连接到服务器
    private boolean connectedToServer = true;
    private boolean loginHasInit = false;
    private ChatBaseEvent chatBaseEvent = null;
    private ChatMessageEvent chatMessageEvent = null;
    private MessageQoSEvent messageQoSEvent = null;
    private Context context = null;

    private IMessageHandler mMessageHandler;
    private static IMCoreSDK instance;

    public static IMCoreSDK getInstance() {
        if (instance == null) {
            synchronized (IMCoreSDK.class) {
                if (instance == null) {
                    instance = new IMCoreSDK();
                }
            }
        }
        return instance;
    }

    private IMCoreSDK() {
    }

    /**
     * IMCoreSDK初始化类
     *
     * @param context Android顶级Context
     */
    public void init(Context context) {
        if (!_init) {
            if (context == null) {
                throw new IllegalArgumentException("context can't be null!");
            }
            if (!IMThreadPool.isOnMainThread()) {
                throw new IllegalArgumentException("init must in main thread!");
            }
            if (context instanceof Application)
                this.context = context;
            else {
                this.context = context.getApplicationContext();
            }
            //注册网络变化广播
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            this.context.registerReceiver(mNetworkStateReceiver, intentFilter);

            // TODO: 2022/9/20 线程池的初始化
            //初始化
//            AutoReLoginDaemon.getInstance();
//            KeepAliveDaemon.getInstance();
//            LocalDataReciever.getInstance();
//            QoS4ReciveDaemon.getInstance();
//            QoS4SendDaemon.getInstance();
            _init = true;
        }
    }

    public IMessageHandler getMessageHandler() {
        return mMessageHandler;
    }

    public void setMessageHandler(IMessageHandler mMessageHandler) {
        this.mMessageHandler = mMessageHandler;
    }

    /**
     * 断开，关闭所有相关功能，释放所有资源
     */
    public void release() {
        //关闭套接字
        SocketProvider.getInstance().closeSocket();
//        AutoReLoginDaemon.getInstance().stop();
//        QoS4SendDaemon.getInstance().stop();
//        QoS4ReciveDaemon.getInstance().stop();
        KeepAliveDaemon.getInstance().stop();
//        QoS4SendDaemon.getInstance().clear();
//        QoS4ReciveDaemon.getInstance().clear();

        //关闭注册的网络广播
        try {
            context.unregisterReceiver(mNetworkStateReceiver);
        } catch (Exception e) {
            Log.i(TAG, "还未注册android网络事件广播的监听器，本次取消注册已被正常忽略哦.");
        }
        //更新断开标记
        this.setLoginHasInit(false);
        this.setConnectedToServer(false);
        _init = false;
    }

    public boolean isLoginHasInit() {
        return loginHasInit;
    }

    public IMCoreSDK setLoginHasInit(boolean loginHasInit) {
        this.loginHasInit = loginHasInit;
        return this;
    }

    public boolean isConnectedToServer() {
        return connectedToServer;
    }

    public void setConnectedToServer(boolean connectedToServer) {
        this.connectedToServer = connectedToServer;
    }

    public boolean isInitialed() {
        return this._init;
    }

    public void setChatBaseEvent(ChatBaseEvent chatBaseEvent) {
        this.chatBaseEvent = chatBaseEvent;
    }

    public ChatBaseEvent getChatBaseEvent() {
        return chatBaseEvent;
    }

    public void setChatMessageEvent(ChatMessageEvent chatMessageEvent) {
        this.chatMessageEvent = chatMessageEvent;
    }

    public ChatMessageEvent getChatMessageEvent() {
        return chatMessageEvent;
    }

    public void setMessageQoSEvent(MessageQoSEvent messageQoSEvent) {
        this.messageQoSEvent = messageQoSEvent;
    }

    public MessageQoSEvent getMessageQoSEvent() {
        return messageQoSEvent;
    }

    private final NetworkStateReceiver mNetworkStateReceiver = new NetworkStateReceiver(new INetworkListener() {
        @Override
        public void onNetworkChange(boolean connected) {
            // TODO: 2022/9/20
            SocketProvider.getInstance().closeSocket();
        }
    });
}
