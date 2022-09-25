package com.example.mylibrary.message;

import com.example.mylibrary.IMCoreSDK;
import com.example.mylibrary.core.SocketProvider;
import com.example.mylibrary.utils.IMObserver;
import com.example.mylibrary.utils.LogUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class SendHelper {
    private static SendHelper instance;

    public static SendHelper getInstance() {
        if (instance == null) {
            synchronized (SendHelper.class) {
                if (instance == null) {
                    instance = new SendHelper();
                }
            }
        }
        return instance;
    }

    private SendHelper() {
    }

    public void send(Message message) {
        byte[] data = null;
        if (IMCoreSDK.getInstance().getMessageHandler() != null) {
            data = IMCoreSDK.getInstance().getMessageHandler().onMessageSend(message);
        }
        send(SocketProvider.getInstance().getSocket(), data, message.getObserver());
    }

    /**
     * @param socket
     * @param data
     * @param resultObserver
     */
    public synchronized void send(Channel socket, byte[] data, final IMObserver resultObserver) {
        if (data == null || data.length == 0) {
            LogUtils.d("【IMCORE-TCP】无效参数，data == null || data.length == 0");
            if (resultObserver != null) {
                resultObserver.update(false, null);
            }
            return;
        }
        if (socket == null || !socket.isActive()) {
            LogUtils.w("【IMCORE-TCP】无效连接，socket == null || !socket.isActive()");
            if (resultObserver != null) {
                resultObserver.update(false, null);
            }
            return;
        }

        try {
            ByteBuf to = Unpooled.copiedBuffer(data, 0, data.length);
            ChannelFuture cf = socket.writeAndFlush(to);
            cf.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        LogUtils.i("[IMCORE-TCP异步回调] >> 数据已成功发出");
                    } else {
                        LogUtils.w("[IMCORE-TCP异步回调] >> 数据发送失败");
                    }
                    if (resultObserver != null) {
                        resultObserver.update(channelFuture.isSuccess(), null);
                    }
                }
            });
        } catch (Exception e) {
            LogUtils.e("【IMCORE-TCP】发送TCP数据报文时出错了，原因是：" + e.getMessage(), e);
            if (resultObserver != null) {
                resultObserver.update(false, null);
            }
        }
    }
}
