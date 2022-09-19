package com.example.mylibrary.utils;

import android.util.Log;

import java.net.SocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class TCPUtils {
    private final static String TAG = TCPUtils.class.getSimpleName();

    public static boolean send(Channel skt, byte[] d, int dataLen) {
        return TCPUtils.send(skt, d, dataLen, null);
    }

    public static synchronized boolean send(Channel skt, byte[] d, final int dataLen, final MBObserver resultObserver) {
        boolean sendSucess = false;
        if ((skt != null) && (d != null)) {

            // TODO: 正式发布时，关闭掉此Log，当前仅用于调试时！
//			Log.d(TAG, "【IMCORE-TCP】正在send()TCP数据时，[d.len="+d.length+",remoteIpAndPort="
//					+ TCPUtils.getSocketAdressInfo(skt.remoteAddress())+"]"
//					+ "，本地端口是："+ TCPUtils.getSocketAdressInfo(skt.localAddress())+" ...");

            if (skt.isActive()) {
                try {
                    ByteBuf to = Unpooled.copiedBuffer(d, 0, dataLen);

                    ChannelFuture cf = skt.writeAndFlush(to);//.sync();
                    sendSucess = true;

                    cf.addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            Log.i(TAG, "[IMCORE-netty-send异步回调] >> 数据已成功发出[dataLen=" + dataLen + "].");
                        } else {
                            Log.w(TAG, "[IMCORE-netty-send异步回调] >> 数据发送失败！[dataLen=" + dataLen + "].");
                        }

                        if (resultObserver != null)
                            resultObserver.update(future.isSuccess(), null);
                    });

                    return sendSucess;
                } catch (Exception e) {

                    Log.e(TAG, "【IMCORE-TCP】send方法中》》发送TCP数据报文时出错了，原因是：" + e.getMessage(), e);
                    if (resultObserver != null)
                        resultObserver.update(false, null);
                }
            } else {
                Log.e(TAG, "【IMCORE-TCP】send方法中》》无法发送TCP数据，原因是：skt.isActive()=" + skt.isActive());
            }
        } else {
            Log.w(TAG, "【IMCORE-TCP】send方法中》》无效的参数：skt==null || d == null!");
        }

        if (resultObserver != null)
            resultObserver.update(false, null);

        return sendSucess;
    }

    public static String getSocketAdressInfo(SocketAddress s) {
        return (s != null) ? s.toString() : null;
    }
}
