package com.example.mylibrary;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.mylibrary.core.AutoReLoginDaemon;
import com.example.mylibrary.core.KeepAliveDaemon;
import com.example.mylibrary.core.LocalDataReciever;
import com.example.mylibrary.core.LocalSocketProvider;
import com.example.mylibrary.core.QoS4ReciveDaemon;
import com.example.mylibrary.core.QoS4SendDaemon;
import com.example.mylibrary.event.ChatBaseEvent;
import com.example.mylibrary.event.ChatMessageEvent;
import com.example.mylibrary.event.MessageQoSEvent;

import net.x52im.mobileimsdk.server.protocal.c.PLoginInfo;

public class IMCoreSDK {
    private final static String TAG = IMCoreSDK.class.getSimpleName();
    //true表示开启MobileIMSDK Debug信息在Logcat下的输出，否则关闭。
    public static boolean DEBUG = true;
    //是否在登陆成功后掉线时自动重新登陆线程中实质性发起登陆请求，
    // true表示将在线程运行周期中正常发起，否则不发起 （即关闭实质性的重新登陆请求）。
    public static boolean autoReLogin = true;

    //是否初始化了
    private boolean _init = false;
    private boolean connectedToServer = true;
    private boolean loginHasInit = false;
    private PLoginInfo currentLoginInfo = null;
    private ChatBaseEvent chatBaseEvent = null;
    private ChatMessageEvent chatMessageEvent = null;
    private MessageQoSEvent messageQoSEvent = null;
    private Context context = null;
    private static IMCoreSDK instance = null;
    public static IMCoreSDK  getInstance(){
        if (instance == null){
            synchronized (IMCoreSDK.class){
                if (instance == null){
                    instance = new IMCoreSDK();
                }
            }
        }
        return instance;
    }
    private IMCoreSDK(){
    }

    public void init(Context context){
        if (!_init){
            if (context == null){
                throw new IllegalArgumentException("context can't be null!");
            }
            if (context instanceof Application)
                this.context = context;
            else {
                this.context = context.getApplicationContext();
            }
            // Register for broadcasts when network status changed
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            this.context.registerReceiver(networkConnectionStatusBroadcastReceiver, intentFilter);

            //初始化
            AutoReLoginDaemon.getInstance();
            KeepAliveDaemon.getInstance();
            LocalDataReciever.getInstance();
            QoS4ReciveDaemon.getInstance();
            QoS4SendDaemon.getInstance();
            _init = true;
        }
    }

    public void release(){
        //断开
        this.setConnectedToServer(false);
        LocalSocketProvider.getInstance().closeLocalSocket();
        AutoReLoginDaemon.getInstance().stop();
        QoS4SendDaemon.getInstance().stop();
        QoS4ReciveDaemon.getInstance().stop();
        KeepAliveDaemon.getInstance().stop();

        QoS4SendDaemon.getInstance().clear();
        QoS4ReciveDaemon.getInstance().clear();

        try {
            context.unregisterReceiver(networkConnectionStatusBroadcastReceiver);
        } catch (Exception e) {
            Log.i(TAG, "还未注册android网络事件广播的监听器，本次取消注册已被正常忽略哦.");
        }

        _init = false;
        this.setLoginHasInit(false);
        this.setConnectedToServer(false);
    }

    public void setCurrentLoginInfo(PLoginInfo currentLoginInfo) {
        this.currentLoginInfo = currentLoginInfo;
    }

    public PLoginInfo getCurrentLoginInfo() {
        return this.currentLoginInfo;
    }

    public void saveFirstLoginTime(long firstLoginTime) {
        if(this.currentLoginInfo != null)
            this.currentLoginInfo.setFirstLoginTime(firstLoginTime);
    }

    @Deprecated
    public String getCurrentLoginUserId()
    {
        return this.currentLoginInfo.getLoginUserId();
    }

    @Deprecated
    public String getCurrentLoginToken()
    {
        return this.currentLoginInfo.getLoginToken();
    }

    @Deprecated
    public String getCurrentLoginExtra()
    {
        return this.currentLoginInfo.getExtra();
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

    private final BroadcastReceiver networkConnectionStatusBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
            NetworkInfo mobNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo ethernetInfo = connectMgr.getNetworkInfo(9);
            if (!(mobNetInfo != null && mobNetInfo.isConnected())
                    && !(wifiNetInfo != null && wifiNetInfo.isConnected())
                    // ## Bug FIX 20170228: 解决当Android系统用有线网连接时没有判断此网事件的问题
                    && !(ethernetInfo != null && ethernetInfo.isConnected())
            ) {
                Log.w(TAG, "【IMCORE-TCP】【本地网络通知】检测本地网络连接断开了!");
                LocalSocketProvider.getInstance().closeLocalSocket();
            } else {
                if (ClientCoreSDK.DEBUG)
                    Log.i(TAG, "【IMCORE-TCP】【本地网络通知】检测本地网络已连接上了!");
                LocalSocketProvider.getInstance().closeLocalSocket();
            }
        }
    };
}
