package com.github.alexvictoor.proxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;


public final class HttpProxyServer {

    private static final Logger logger = LoggerFactory.getLogger(HttpProxyServer.class);
    private final String targetHost;
    private final int targetPort;
    private final int proxyPort;
    private final List<FileSystemRoute> routes;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public HttpProxyServer(String targetHost, int targetPort, int proxyPort, List<FileSystemRoute> routes) {
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.proxyPort = proxyPort;
        this.routes = routes;
    }

    public void start() {

        // Configure the server.
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new HttpProxyServerInitializer(targetHost, targetPort, routes));
        try {
            b.bind(proxyPort).sync().channel();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.info("Open your web browser and navigate to http://127.0.0.1:{}/", proxyPort);
    }

    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
