package com.example.mylibrary.message;

import android.util.Log;

import com.example.mylibrary.core.SocketProvider;
import com.example.mylibrary.utils.IMObserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class SendHelper {
    private static final String TAG = SendHelper.class.getSimpleName();
    private SendHelper(){}
    private static SendHelper instance;
    public static SendHelper getInstance(){
        if (instance == null){
            synchronized (SendHelper.class){
                if (instance == null){
                    instance = new SendHelper();
                }
            }
        }
        return instance;
    }


    public boolean send(byte[] data) {
        return send(SocketProvider.getInstance().getSocket(), data, null);
    }
    /**
     * @param socket
     * @param data
     * @param resultObserver
     * @return
     */
    public synchronized boolean send(Channel socket, byte[] data, final IMObserver resultObserver) {
        if (data == null || data.length == 0) {
            Log.i(TAG, "【IMCORE-TCP】无效参数，data == null || data.length == 0");
            if (resultObserver != null) {
                resultObserver.update(false, null);
            }
            return false;
        }
        if (socket == null || !socket.isActive()) {
            Log.w(TAG, "【IMCORE-TCP】无效连接，socket == null || !socket.isActive()");
            if (resultObserver != null) {
                resultObserver.update(false, null);
            }
            return false;
        }

        boolean sendSuccess = false;
        try {
            ByteBuf to = Unpooled.copiedBuffer(data, 0, data.length);
            ChannelFuture cf = socket.writeAndFlush(to);
            sendSuccess = true;
            cf.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        Log.i(TAG, "[IMCORE-TCP异步回调] >> 数据已成功发出");
                    } else {
                        Log.w(TAG, "[IMCORE-TCP异步回调] >> 数据发送失败");
                    }
                    if (resultObserver != null){
                        resultObserver.update(channelFuture.isSuccess(), null);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "【IMCORE-TCP】发送TCP数据报文时出错了，原因是：" + e.getMessage(), e);
            sendSuccess = false;
            if (resultObserver != null) {
                resultObserver.update(false, null);
            }
        }
        return sendSuccess;
    }
}
