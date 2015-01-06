package com.github.alexvictoor.rule;

import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

public class Sockets {

    public static final Logger logger = LoggerFactory.getLogger(Sockets.class);

    public static boolean isPortAvailable(int port) {
        boolean result = false;
        Socket socket = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress("localhost", port), 10000);
            logger.debug("Succeed to connect to localhost:" + port + ", hence port is not available");
            result = false;
        } catch (IOException e) {
            logger.debug("Exception while testing port availability: " + port, e);
            result = true;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    logger.error("Failure while attempting to close socket on port " + port, e);
                }
            }
        }
        return result;
    }

}