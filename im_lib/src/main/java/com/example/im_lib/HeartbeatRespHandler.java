package com.example.im_lib;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 心跳消息响应处理handler
 * 是当客户端接收到服务端登录成功的消息后，主动向服务端发送一条心跳消息，
 * 心跳消息可以是一个空包，消息包体越小越好，服务端收到客户端的心跳包后，
 * 原样返回给客户端，这里，就是收到服务端返回的心跳消息响应的处理handler。
 */
public class HeartbeatRespHandler extends ChannelInboundHandlerAdapter {
    private NettyTcpClient imsClient;

    public HeartbeatRespHandler(NettyTcpClient imsClient) {
        this.imsClient = imsClient;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MessageProtobuf.Msg heartbeatRespMsg = (MessageProtobuf.Msg) msg;
        if (heartbeatRespMsg == null || heartbeatRespMsg.getHead() == null) {
            return;
        }

        MessageProtobuf.Msg heartbeatMsg = imsClient.getHeartbeatMsg();
        if (heartbeatMsg == null || heartbeatMsg.getHead() == null) {
            return;
        }

        int heartbeatMsgType = heartbeatMsg.getHead().getMsgType();
        if (heartbeatMsgType == heartbeatRespMsg.getHead().getMsgType()) {
            System.out.println("收到服务端心跳响应消息，message=" + heartbeatRespMsg);
        } else {
            // 消息透传
            ctx.fireChannelRead(msg);
        }
    }
}
