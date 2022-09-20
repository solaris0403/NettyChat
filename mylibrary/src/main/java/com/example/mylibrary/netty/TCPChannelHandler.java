package com.example.mylibrary.netty;

import com.example.mylibrary.conf.CSConfig;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

public class TCPChannelHandler extends ChannelInitializer<Channel> {
    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new LengthFieldPrepender(CSConfig.TCP_FRAME_FIXED_HEADER_LENGTH));
        pipeline.addLast(new LengthFieldBasedFrameDecoder(CSConfig.TCP_FRAME_FIXED_HEADER_LENGTH + CSConfig.TCP_FRAME_MAX_BODY_LENGTH
                , 0, CSConfig.TCP_FRAME_FIXED_HEADER_LENGTH, 0, CSConfig.TCP_FRAME_FIXED_HEADER_LENGTH));
        pipeline.addLast(new TCPClientHandler());
    }
}
