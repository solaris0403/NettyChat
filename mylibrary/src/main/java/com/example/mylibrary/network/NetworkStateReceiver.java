package com.example.mylibrary.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkStateReceiver extends BroadcastReceiver {
    private static final String TAG = NetworkStateReceiver.class.getSimpleName();
    private INetworkListener mListener;

    public NetworkStateReceiver(INetworkListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //获得ConnectivityManager对象
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);//获取WIFI连接的信息
        NetworkInfo mobileNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);//获取移动数据连接的信息
        NetworkInfo ethernetNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);//获取有线网连接的信息
        if (!(wifiNetworkInfo != null && wifiNetworkInfo.isConnected())//手机网
                && !(mobileNetworkInfo != null && mobileNetworkInfo.isConnected())//wifi
                && !(ethernetNetworkInfo != null && ethernetNetworkInfo.isConnected())//有线
        ) {
            Log.w(TAG, "【IMCORE-TCP】【本地网络通知】网络断开");
            if (mListener != null) {
                mListener.onNetworkChange(false);
            }
        } else {
            Log.i(TAG, "【IMCORE-TCP】【本地网络通知】网络已连接");
            if (mListener != null) {
                mListener.onNetworkChange(true);
            }
        }
    }
}
