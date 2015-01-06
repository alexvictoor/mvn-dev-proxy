package com.github.alexvictoor.rule;


import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.net.ServerSocket;

import static org.assertj.core.api.Assertions.assertThat;


public class SocketRuleTest {

    @Rule
    public SocketRule socketRule = new SocketRule();

    private ServerSocket serverSocket;

    @After
    public void tearDown() throws Exception {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    @Test
    public void should_provide_a_free_port() {
        // given
        // when
        int port = socketRule.findFreePort();
        // then
        assertThat(Sockets.isPortAvailable(port)).isTrue();
    }

    @Test
    public void should_not_provide_twice_the_same_port() throws Exception {
        // given
        int port = socketRule.findFreePort();
        // when
        int port2 = socketRule.findFreePort();
        // then
        assertThat(port).isNotEqualTo(port2);
    }

    @Test
    public void should_detect_port_currently_used() throws Exception {
        // given
        socketRule.findFreePort();
        int port = socketRule.findFreePort();
        serverSocket = new ServerSocket(port);
        // when
        boolean available = socketRule.checkProvidedPorts();
        // then
        assertThat(available).isFalse();
    }

    @Test
    public void should_detect_ports_not_used_anymore() throws Exception {
        // given
        int port = socketRule.findFreePort();
        int port2 = socketRule.findFreePort();
        serverSocket = new ServerSocket(port);
        serverSocket.close();
        // when
        boolean available = socketRule.checkProvidedPorts();
        // then
        assertThat(available).isTrue();
    }
}