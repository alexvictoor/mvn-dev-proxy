package com.github.alexvictoor.proxy;

import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Echos an object string to the output screen.
 * @goal run
 * @requiresProject false
 */
public class ProxyMojo extends AbstractMojo {

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

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        List<FileSystemRoute> fsRoutes = new ArrayList<>();
        if (routes != null) {
            for (String route : routes) {
                String[] tokens = route.split("|");
                String uriPrefix = tokens[0];
                String path = tokens[1];
                fsRoutes.add(FileSystemRoute.create(uriPrefix, path));
            }
        }
        HttpProxyServer proxyServer = new HttpProxyServer(targetHost, targetPort, proxyPort, fsRoutes);
        proxyServer.start();
        try {
            System.in.read();
        } catch (IOException e) {
            proxyServer.stop();
        }
    }
}
