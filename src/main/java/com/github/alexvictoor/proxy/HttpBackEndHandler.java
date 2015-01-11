package com.github.alexvictoor.proxy;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpBackEndHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger logger = LoggerFactory.getLogger(HttpBackEndHandler.class);

    private final Channel inboundChannel;

    public HttpBackEndHandler(Channel inboundChannel) {
        super(false); // do not release messages
        this.inboundChannel = inboundChannel;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof HttpMessage) {
            logger.debug("Adding no cache headers");
            HttpHeaders headers = ((HttpMessage) msg).headers();
            headers.set("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.set("Pragma", "no-cache");
            headers.set("Expires", "0");
        }
        inboundChannel.writeAndFlush(msg);
    }
}
