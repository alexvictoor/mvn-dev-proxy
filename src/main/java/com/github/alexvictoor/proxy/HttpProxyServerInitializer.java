package com.github.alexvictoor.proxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.util.List;

public class HttpProxyServerInitializer extends ChannelInitializer<SocketChannel> {

    private final String targetHost;
    private final List<FileSystemRoute> routes;
    private final int targetPort;

    public HttpProxyServerInitializer(String targetHost, int targetPort, List<FileSystemRoute> routes) {
        this.targetPort = targetPort;
        this.targetHost = targetHost;
        this.routes = routes;
    }

    @Override
    public void initChannel(SocketChannel ch) {

        ChannelPipeline p = ch.pipeline();

        p.addLast(new HttpRequestDecoder());
        p.addLast(new HttpObjectAggregator(1048576));
        p.addLast(new HttpResponseEncoder());
        // Remove the following line if you don't want automatic content compression.
        //p.addLast(new HttpContentCompressor());
        p.addLast(new HttpFrontEndHandler(targetHost, targetPort, routes));
    }
}
