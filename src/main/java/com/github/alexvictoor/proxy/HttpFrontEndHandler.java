package com.github.alexvictoor.proxy;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpFrontEndHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HttpFrontEndHandler.class);

    private final String host;
    private final int port;
    private ChannelFuture channelFuture;

    public HttpFrontEndHandler(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, final FullHttpRequest msg) throws Exception {
        logger.debug("REQ URI {}", msg.getUri());
        final Channel inboundChannel = ctx.channel();
        // Start the connection attempt.
        Bootstrap b = new Bootstrap();
        ChannelInitializer<SocketChannel> initializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline p = ch.pipeline();
                p.addLast(new HttpClientCodec());
                // Remove the following line if you don't want automatic content decompression.
                //p.addLast(new HttpContentDecompressor());
                p.addLast(new HttpObjectAggregator(1048576));
                p.addLast(new HttpBackEndHandler(inboundChannel));
            }
        };
        b.group(inboundChannel.eventLoop())
                .channel(NioSocketChannel.class)
                .handler(initializer);


        // Make the connection attempt.
        if (channelFuture == null || (channelFuture.isSuccess() && !channelFuture.channel().isOpen())) {
            logger.debug("Instantiating new connection");
            channelFuture = b.connect(host, port);
        } else {
            logger.debug("Reusing connection");
        }

        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    // Prepare the HTTP request.
                    HttpRequest request = new DefaultFullHttpRequest(
                            HttpVersion.HTTP_1_1, msg.getMethod(), msg.getUri());
                    request.headers().add(msg.headers());
                    request.headers().set(HttpHeaders.Names.HOST, host);
                    //request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                    //request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);

                    future.channel().writeAndFlush(request);
                } else {
                    logger.info("Connection issue", future.cause());
                }
            }
        });
    }
}
