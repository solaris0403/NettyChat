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
    private Bootstrap mBootstrap = null;
    private Channel mSocket = null;

    private IMObserver connectionDoneObserver;
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
            LogUtils.w("Socket初始化出错：" + e.getMessage(), e);
        }
    }

    public void setConnectionDoneObserver(IMObserver connectionDoneObserver) {
        this.connectionDoneObserver = connectionDoneObserver;
    }

    /**
     * 重置Socket
     *
     * @return Socket
     */
    public Channel resetSocket() {
        try {
            closeSocket();
            initBootstrap();
            tryConnectToHost();
        } catch (Exception e) {
            LogUtils.w( "【IMCORE-TCP】重置Socket出错：" + e.getMessage(), e);
            closeSocket();
        }
        return mSocket;
    }

    /**
     * 连接Socket，属于异步方法，连接结果会通过回调传递
     */
    private void tryConnectToHost() {
        if (IMCoreSDK.DEBUG) {
            LogUtils.d( "【IMCORE-TCP】tryConnectToHost并获取connection开始了...");
        }

        try {
            ChannelFuture cf = mBootstrap.connect(ConfigEntity.serverIP, ConfigEntity.serverPort);
            mSocket = cf.channel();
            cf.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isDone()) {
                        if (channelFuture.isCancelled()) {
                            LogUtils.w("【IMCORE-tryConnectToHost-异步回调】Connection attempt cancelled by user");
                        } else if (!channelFuture.isSuccess()) {
                            LogUtils.w("【IMCORE-tryConnectToHost-异步回调】连接失败，原因是：", channelFuture.cause());
                        } else {
                            LogUtils.i( "【IMCORE-tryConnectToHost-异步回调】Connection established successfully");
                        }
                        if (connectionDoneObserver != null) {
                            connectionDoneObserver.update(channelFuture.isSuccess(), null);
                            connectionDoneObserver = null;
                        }
                    }
                }
            });

            mSocket.closeFuture().addListener((ChannelFutureListener) future -> {
                LogUtils.i( "【IMCORE-TCP】channel优雅退出开始。。。");
                if (future.channel() != null) {
                    future.channel().eventLoop().shutdownGracefully();
                }
                mSocket = null;
                LogUtils.i( "【IMCORE-TCP】channel优雅退出结束。");
            });

            if (IMCoreSDK.DEBUG) {
                LogUtils.d( "【IMCORE-TCP】tryConnectToHost并获取connection已完成。 .... continue ...");
            }
        } catch (Exception e) {
            LogUtils.e( String.format("【IMCORE-TCP】连接Server(IP[%s],PORT[%s])失败", ConfigEntity.serverIP, ConfigEntity.serverPort), e);
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

    /**
     * 关闭Socket
     */
    public void closeSocket() {
        if (IMCoreSDK.DEBUG) {
            LogUtils.d( "【IMCORE-TCP】正在closeSocket()...");
        }

        if (mBootstrap != null) {
            try {
                mBootstrap.config().group().shutdownGracefully();
                mBootstrap = null;
            } catch (Exception e) {
                LogUtils.w( "【IMCORE-TCP】在closeSocket方法中试图释放Bootstrap资源时：", e);
            }
        }

        if (mSocket != null) {
            try {
                mSocket.close();
                mSocket = null;
            } catch (Exception e) {
                LogUtils.w( "【IMCORE-TCP】在closeSocket方法中试图释放Socket资源时：", e);
            }
        } else {
            if (IMCoreSDK.DEBUG) {
                LogUtils.d( "【IMCORE-TCP】Socket == null，无需关闭。");
            }
        }
    }
}
