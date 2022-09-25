package com.example.mylibrary.netty;


import com.example.mylibrary.IMCoreSDK;
import com.example.mylibrary.core.KeepAliveDaemon;
import com.example.mylibrary.message.MessageDispatcher;
import com.example.mylibrary.core.SocketProvider;
import com.example.mylibrary.utils.IMThreadPool;
import com.example.mylibrary.utils.LogUtils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

public class NettyClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        LogUtils.d("【IMCORE-netty-channelActive】连接已成功建立，(isSocketReady = " + SocketProvider.getInstance().isSocketReady() + ")");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        LogUtils.d("【IMCORE-netty-channelInactive】连接已断开，(isSocketReady=" + SocketProvider.getInstance().isSocketReady()
                    + ", ClientCoreSDK.connectedToServer = " + IMCoreSDK.getInstance().isConnectedToServer() + ")");

        // - 20200709 add by Jack Jiang：适应用TCP协议，用于快速响应tcp连接断开事件，第一时间反馈给上层，提升用户体验
        if (IMCoreSDK.getInstance().isConnectedToServer()) {
            LogUtils.d("【IMCORE-netty-channelInactive】连接已断开，立即提前进入框架的“通信通道”断开处理逻辑(而不是等心跳线程探测到，那就已经比较迟了)......");
            IMThreadPool.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    KeepAliveDaemon.getInstance().notifyConnectionLost();
                }
            });
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        LogUtils.d("【IMCORE-netty-channelRead0】收到消息：" + buf.toString(CharsetUtil.UTF_8));
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        MessageDispatcher.getInstance().onReceive(data);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LogUtils.w("【IMCORE-netty-exceptionCaught】异常被触发了，原因是：" + cause.getMessage());
        ctx.close();
    }
}
