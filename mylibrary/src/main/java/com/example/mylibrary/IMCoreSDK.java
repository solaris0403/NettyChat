package com.example.mylibrary;

import android.app.Application;
import android.content.Context;

import com.example.mylibrary.core.Auth;
import com.example.mylibrary.core.KeepAliveDaemon;
import com.example.mylibrary.core.SocketProvider;
import com.example.mylibrary.event.ChatBaseEvent;
import com.example.mylibrary.event.ChatMessageEvent;
import com.example.mylibrary.event.MessageQoSEvent;
import com.example.mylibrary.message.IMessageHandler;
import com.example.mylibrary.network.NetworkMonitor;
import com.example.mylibrary.utils.IMThreadPool;
import com.example.mylibrary.utils.LogUtils;

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
    private Context context;

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
            if (context instanceof Application) {
                this.context = context;
            } else {
                this.context = context.getApplicationContext();
            }
            LogUtils.init(DEBUG, TAG);
            Auth.getInstance().setAuth(true);
            // TODO: 2022/9/20 线程池的初始化
            //初始化
//            AutoReLoginDaemon.getInstance();
//            KeepAliveDaemon.getInstance();
//            LocalDataReciever.getInstance();
//            QoS4ReciveDaemon.getInstance();
//            QoS4SendDaemon.getInstance();
            NetworkMonitor.getInstance().start(this.context);
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
        NetworkMonitor.getInstance().stop(this.context);
        SocketProvider.getInstance().closeSocket();
//        AutoReLoginDaemon.getInstance().stop();
//        QoS4SendDaemon.getInstance().stop();
//        QoS4ReciveDaemon.getInstance().stop();
        KeepAliveDaemon.getInstance().stop();
//        QoS4SendDaemon.getInstance().clear();
//        QoS4ReciveDaemon.getInstance().clear();

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
}
