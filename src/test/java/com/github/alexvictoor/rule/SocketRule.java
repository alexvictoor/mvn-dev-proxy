package com.github.alexvictoor.rule;

import junit.framework.AssertionFailedError;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SocketRule implements TestRule {

    public static final Logger logger = LoggerFactory.getLogger(SocketRule.class);

    private Map<Integer, Exception> providedPorts = new HashMap<Integer, Exception>();
    private int lastPort = 8000;

    public int findFreePort() {
        do {
            lastPort++;
        } while (!Sockets.isPortAvailable(lastPort));
        providedPorts.put(lastPort, new Exception());

        return lastPort;
    }

    public boolean checkProvidedPorts() {
        boolean result = true;
        for (Integer port : providedPorts.keySet()) {
            if (Sockets.isPortAvailable(port)) {
                logger.debug("Port {} is available", port);
            } else {
                logger.error("Port " + port + " has not been released properly, it has been used first at:",
                        providedPorts.get(port));
            }
            result = result && Sockets.isPortAvailable(port);
        }
        return result;
    }

    @Override
    public Statement apply(final Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                statement.evaluate();
                if (!checkProvidedPorts()) {
                    throw new AssertionFailedError("All ports have not been released");
                }
            }
        };
    }
}