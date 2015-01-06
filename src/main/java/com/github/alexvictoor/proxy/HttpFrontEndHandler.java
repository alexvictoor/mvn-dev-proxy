package com.github.alexvictoor.proxy;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

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
        final String uri = msg.getUri();
        logger.debug("REQ URI {}", uri);
        if (uri.contains("webjar")) {
            handleFileRequest(ctx, msg);
        } else {
            handleProxyRequest(ctx, msg);
        }

    }

    private void handleProxyRequest(final ChannelHandlerContext ctx, final FullHttpRequest msg) {
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
                    sendError(ctx, HttpResponseStatus.BAD_GATEWAY);
                }
            }
        });
    }

    private void handleFileRequest(ChannelHandlerContext ctx, FullHttpRequest msg) throws IOException {
        if (msg.getMethod() != GET) {
            sendError(ctx, METHOD_NOT_ALLOWED);
            return;
        }

        String path = getClass().getResource("/dummy.html").getFile();
        File file = new File(path);

        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException ignore) {
            sendError(ctx, NOT_FOUND);
            return;
        }
        long fileLength = raf.length();

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        HttpHeaders.setContentLength(response, fileLength);
        setContentTypeHeader(response, file);
        if (HttpHeaders.isKeepAlive(msg)) {
            response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        // Write the initial line and the header.
        ctx.write(response);

        // Write the content.
        ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());

        // Write the end marker
        ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

        // Decide whether to close the connection or not.
        if (!HttpHeaders.isKeepAlive(msg)) {
            // Close the connection when the whole content is written out.
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }

    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void setContentTypeHeader(HttpResponse response, File file) {
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        response.headers().set(CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));
    }
}
