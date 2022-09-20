package com.example.mylibrary.message;

import android.util.Log;

import com.example.mylibrary.core.SocketProvider;
import com.example.mylibrary.utils.IMObserver;

import net.x52im.mobileimsdk.server.protocal.ErrorCode;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class IMClient {
    private static final String TAG = IMClient.class.getSimpleName();
    public static void send(Message message){
        MessageDispatcher.getInstance().onSend(message);
    }
}
