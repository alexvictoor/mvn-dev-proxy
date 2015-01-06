package com.github.alexvictoor.rule;

import org.junit.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static org.assertj.core.api.Assertions.assertThat;

public class SocketsTest {

    private ServerSocket serverSocket;

    @Before
    public void setUp() throws Exception {
        serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress("localhost", 0), 1);
    }

    @After
    public void tearDown() throws Exception {
        serverSocket.close();
    }

    @Test
    public void should_consider_port_available() throws IOException {
        // given
        int port = serverSocket.getLocalPort();
        serverSocket.close();
        // when
        boolean available = Sockets.isPortAvailable(port);
        // then
        assertThat(available).isTrue();
    }

    @Test
    public void should_consider_port_not_available() throws IOException {
        // given
        int port = serverSocket.getLocalPort();
        // when
        boolean available = Sockets.isPortAvailable(port);
        // then
        assertThat(available).isFalse();
    }

    @Test
    @Ignore
    public void should_consider_port_unavailable_after_connection_timeout() throws IOException {
        // given
        int port = serverSocket.getLocalPort();
        new Socket("localhost", port);
        // when
        boolean available = Sockets.isPortAvailable(port);
        // then
        assertThat(available).isFalse();
    }


    @Test
    public void should_consider_port_available_using_netstat() throws IOException {
        // given
        int port = serverSocket.getLocalPort();
        serverSocket.close();
        // when
        boolean available = Sockets.isPortAvailableUsingNetstat(port);
        // then
        assertThat(available).isTrue();
    }

    @Test
    public void should_consider_port_not_available_using_netstat() throws IOException {
        // given
        int port = serverSocket.getLocalPort();
        // when
        boolean available = Sockets.isPortAvailableUsingNetstat(port);
        // then
        assertThat(available).isFalse();
    }

    @Test
    public void should_consider_port_unavailable_after_connection_timeout_using_netstat() throws IOException {
        // given
        int port = serverSocket.getLocalPort();
        new Socket("localhost", port);
        // when
        boolean available = Sockets.isPortAvailableUsingNetstat(port);
        // then
        assertThat(available).isFalse();
    }

}