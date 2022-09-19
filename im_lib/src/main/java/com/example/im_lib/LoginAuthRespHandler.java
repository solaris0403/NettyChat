package com.example.im_lib;

import org.json.JSONObject;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 握手认证消息响应处理handler
 * 是当客户端与服务端长连接建立成功后，客户端主动向服务端发送一条登录认证消息，
 * 带入与当前用户相关的参数，比如token，服务端收到此消息后，到数据库查询该用户信息，
 * 如果是合法有效的用户，则返回一条登录成功消息给该客户端，
 * 反之，返回一条登录失败消息给该客户端，这里，就是在接收到服务端返回的登录状态后的处理handler。
 */
public class LoginAuthRespHandler extends ChannelInboundHandlerAdapter {
    private NettyTcpClient imsClient;

    public LoginAuthRespHandler(NettyTcpClient imsClient) {
        this.imsClient = imsClient;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        MessageProtobuf.Msg handshakeRespMsg = (MessageProtobuf.Msg) msg;
        if (handshakeRespMsg == null || handshakeRespMsg.getHead() == null) {
            return;
        }

        MessageProtobuf.Msg handshakeMsg = imsClient.getHandshakeMsg();
        if (handshakeMsg == null || handshakeMsg.getHead() == null) {
            return;
        }

        int handshakeMsgType = handshakeMsg.getHead().getMsgType();
        if (handshakeMsgType == handshakeRespMsg.getHead().getMsgType()) {
            System.out.println("收到服务端握手响应消息，message=" + handshakeRespMsg);
            int status = -1;
            try {
                JSONObject jsonObj = new JSONObject(handshakeRespMsg.getHead().getExtend());
                status = jsonObj.optInt("status");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (status == 1) {
                    // 握手成功，马上先发送一条心跳消息，至于心跳机制管理，交由HeartbeatHandler
                    MessageProtobuf.Msg heartbeatMsg = imsClient.getHeartbeatMsg();
                    if (heartbeatMsg == null) {
                        return;
                    }

                    // 握手成功，检查消息发送超时管理器里是否有发送超时的消息，如果有，则全部重发
                    imsClient.getMsgTimeoutTimerManager().onResetConnected();

                    System.out.println("发送心跳消息：" + heartbeatMsg + "当前心跳间隔为：" + imsClient.getHeartbeatInterval() + "ms\n");
                    imsClient.sendMsg(heartbeatMsg);

                    // 添加心跳消息管理handler
                    imsClient.addHeartbeatHandler();
                } else {
                    imsClient.resetConnect(false);// 握手失败，触发重连
                }
            }
        } else {
            // 消息透传
            ctx.fireChannelRead(msg);
        }
    }
}
