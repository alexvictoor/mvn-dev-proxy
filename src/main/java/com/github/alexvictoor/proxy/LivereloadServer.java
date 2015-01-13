package com.github.alexvictoor.proxy;

import com.github.alexvictoor.livereload.Broadcaster;
import com.github.alexvictoor.livereload.FileReader;
import com.github.alexvictoor.livereload.FileSystemWatcher;
import com.github.alexvictoor.livereload.WebSocketServerInitializer;
import com.google.common.base.Throwables;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class LivereloadServer {

    private static final Logger logger = LoggerFactory.getLogger(LivereloadServer.class);
    static final int DEFAULT_PORT = 35729;

    public final int port;

    private final List<File> files;
    private NioEventLoopGroup workerGroup;
    private NioEventLoopGroup bossGroup;

    public LivereloadServer(List<File> files) {
        this.files = files;
        port = DEFAULT_PORT;
    }

    public void start() {

        // Configure the server.
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        ChannelGroup allChannels =
                new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        Broadcaster broadcaster = new Broadcaster(allChannels);
        try {
            for (File file : files) {
                FileSystemWatcher watcher = new FileSystemWatcher(file.getCanonicalPath());
                watcher.addCallback(broadcaster);
                watcher.start();
            }
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }

        FileReader fileReader = new FileReader();
        String jsContent = fileReader.readFileFromClassPath("/livereload.js");

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new WebSocketServerInitializer(allChannels, jsContent));
        try {
            b.bind(port).sync().channel();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.info("Livereload server ready");
    }

    public void stop() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
