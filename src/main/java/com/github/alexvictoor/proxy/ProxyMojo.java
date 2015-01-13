package com.github.alexvictoor.proxy;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Echos an object string to the output screen.
 * @goal run
 * @requiresProject false
 */
public class ProxyMojo extends AbstractMojo {

    private static final Logger logger = LoggerFactory.getLogger(ProxyMojo.class);

    /**
     * @parameter
     */
    private String[] routes;
    /**
     * @parameter default-value="localhost"
     */
    private String targetHost;
    /**
     * @parameter default-value="8080"
     */
    private int targetPort;
    /**
     * @parameter default-value="8081"
     */
    private int proxyPort;
    /**
     * @parameter default-value=true
     */
    private boolean livereload;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        List<FileSystemRoute> fsRoutes = new ArrayList<>();
        if (routes != null) {
            for (String route : routes) {
                fsRoutes.add(FileSystemRoute.parse(route));
            }
        }
        HttpProxyServer proxyServer = new HttpProxyServer(targetHost, targetPort, proxyPort, fsRoutes);
        proxyServer.start();


        List<File> watchedFolders = new ArrayList<>();
        for (FileSystemRoute route : fsRoutes) {
            watchedFolders.add(route.directory);
        }
        LivereloadServer livereloadServer = null;
        if (livereload && !watchedFolders.isEmpty()) {
            livereloadServer = new LivereloadServer(watchedFolders);
            livereloadServer.start();
        }

        logger.info("Press a key to shutdown server");

        try {
            System.in.read();
        } catch (IOException e) {
            proxyServer.stop();
            if (livereloadServer != null) {
                livereloadServer.stop();
            }
        }
    }
}
