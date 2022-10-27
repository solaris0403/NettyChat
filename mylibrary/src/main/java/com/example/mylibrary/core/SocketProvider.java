package com.example.mylibrary.core;

import com.example.mylibrary.IMCoreSDK;
import com.example.mylibrary.conf.IMConfig;
import com.example.mylibrary.conf.ConfigEntity;
import com.example.mylibrary.netty.NettyChannelHandler;
import com.example.mylibrary.utils.IMObserver;
import com.example.mylibrary.utils.LogUtils;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;


/**
 * Socket维护类
 */
public class SocketProvider {
    private static SocketProvider instance;

    private SocketProvider() {
    }

    public static SocketProvider getInstance() {
        if (instance == null) {
            synchronized (SocketProvider.class) {
                if (instance == null) {
                    instance = new SocketProvider();
                }
            }
        }
        return instance;
    }

    private Bootstrap mBootstrap = null;
    private Channel mSocket = null;

    private IMObserver connectionDoneObserver;

    /**
     * 初始化Socket连接配置
     */
    private void initBootstrap() {
        try {
            EventLoopGroup loopGroup = new NioEventLoopGroup();
            mBootstrap = new Bootstrap();
            mBootstrap.group(loopGroup).channel(NioSocketChannel.class);
            mBootstrap.handler(new NettyChannelHandler());
            mBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            mBootstrap.option(ChannelOption.TCP_NODELAY, true);
            mBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, IMConfig.SOCKET_CONNECT_TIMEOUT_MILLIS);
        } catch (Exception e) {
            LogUtils.w("initBootstrap()出错：" + e.getMessage(), e);
        }
    }

    /**
     * 关闭Socket
     */
    public void closeSocket() {
        LogUtils.d("closeSocket()...");
        if (mBootstrap != null) {
            try {
                mBootstrap.config().group().shutdownGracefully();
                mBootstrap = null;
            } catch (Exception e) {
                LogUtils.w("在closeSocket方法中释放Bootstrap资源时出错：", e);
            }
        }

        if (mSocket != null) {
            try {
                mSocket.close();
                mSocket = null;
            } catch (Exception e) {
                LogUtils.w("在closeSocket方法中试图释放Socket资源时：", e);
            }
        }
    }

    /**
     * 关闭之前的socket，重新开始一个socket连接
     *
     * @return Socket
     */
    public Channel resetSocket() {
        if (!Auth.getInstance().isAuth()) {
            LogUtils.i("Auth未认证");
            return null;
        }
        try {
            closeSocket();
            initBootstrap();
            tryConnectToHost();
        } catch (Exception e) {
            LogUtils.w("resetSocket()出错：" + e.getMessage(), e);
            closeSocket();
        }
        return mSocket;
    }

    /**
     * 连接Socket，属于异步方法，连接结果会通过回调传递
     */
    private void tryConnectToHost() {
        try {
            LogUtils.d("tryConnectToHost：连接开始");
            ChannelFuture cf = mBootstrap.connect(ConfigEntity.serverIP, ConfigEntity.serverPort);
            mSocket = cf.channel();
            cf.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isDone()) {
                        if (channelFuture.isCancelled()) {
                            LogUtils.w("tryConnectToHost：异步回调-连接被取消");
                        } else if (!channelFuture.isSuccess()) {
                            LogUtils.w("tryConnectToHost：异步回调-连接失败", channelFuture.cause());
                        } else {
                            LogUtils.i("tryConnectToHost：异步回调-连接成功");
                        }
                        if (connectionDoneObserver != null) {
                            connectionDoneObserver.update(channelFuture.isSuccess(), null);
                            connectionDoneObserver = null;
                        }
                    }
                }
            });

            mSocket.closeFuture().addListener((ChannelFutureListener) future -> {
                LogUtils.i("tryConnectToHost：异步回调-mSocket正常关闭");
                if (future.channel() != null) {
                    future.channel().eventLoop().shutdownGracefully();
                }
                mSocket = null;
            });
        } catch (Exception e) {
            LogUtils.e(String.format("连接Server(IP[%s],PORT[%s])失败", ConfigEntity.serverIP, ConfigEntity.serverPort), e);
        } finally {
            LogUtils.d("tryConnectToHost：连接结束");
        }
    }

    /**
     * Socket是否存在
     *
     * @return
     */
    public boolean isSocketReady() {
        return mSocket != null && mSocket.isActive();
    }

    public Channel getSocket() {
        if (isSocketReady()) {
            return mSocket;
        } else {
            return resetSocket();
        }
    }

    public void setConnectionDoneObserver(IMObserver connectionDoneObserver) {
        this.connectionDoneObserver = connectionDoneObserver;
    }
}
