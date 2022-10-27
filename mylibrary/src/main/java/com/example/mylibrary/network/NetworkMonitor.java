package com.example.mylibrary.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.mylibrary.IMCoreSDK;
import com.example.mylibrary.core.SocketProvider;
import com.example.mylibrary.utils.LogUtils;

// TODO: 2022/9/22 需要继续实现对各种网络类型的监听
public class NetworkMonitor {
    private static volatile NetworkMonitor instance;

    public static NetworkMonitor getInstance() {
        if (instance == null) {
            synchronized (NetworkMonitor.class) {
                if (instance == null) {
                    instance = new NetworkMonitor();
                }
            }
        }
        return instance;
    }

    private NetworkMonitor() {
    }

    private final NetworkStateReceiver mReceiver = new NetworkStateReceiver();

    public void start(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(mReceiver, intentFilter);
    }

    public void stop(Context context) {
        try {
            context.unregisterReceiver(mReceiver);
        } catch (Exception e) {
            LogUtils.w("网络监听器解除异常。", e);
        }
    }

    private static class NetworkStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);//获取WIFI连接的信息
            NetworkInfo mobileNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);//获取移动数据连接的信息
            NetworkInfo ethernetNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);//获取有线网连接的信息
            if (!(wifiNetworkInfo != null && wifiNetworkInfo.isConnected())//手机网
                    && !(mobileNetworkInfo != null && mobileNetworkInfo.isConnected())//wifi
                    && !(ethernetNetworkInfo != null && ethernetNetworkInfo.isConnected())//有线
            ) {
                LogUtils.w("网络已断开");
            } else {
                LogUtils.i("网络已连接");
                SocketProvider.getInstance().resetSocket();
            }
        }
    }
}
