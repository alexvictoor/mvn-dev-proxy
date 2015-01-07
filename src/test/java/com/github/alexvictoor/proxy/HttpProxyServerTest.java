package com.github.alexvictoor.proxy;

import com.github.alexvictoor.rule.SocketRule;
import com.github.alexvictoor.rule.Sockets;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.netty.handler.codec.http.HttpHeaders;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

public class HttpProxyServerTest {

    public static final String TYPE = "text/html";
    public static final String CONTENT = "Hello!";

    @Rule
    public SocketRule socketRule = new SocketRule();
    @Rule
    public WireMockRule targetServer = new WireMockRule(new SocketRule().findFreePort());

    private HttpProxyServer proxyServer;
    private int proxyPort;

    @Before
    public void setUp() throws Exception {
        proxyPort = socketRule.findFreePort();
        List<FileSystemRoute> routes = Arrays.asList(FileSystemRoute.create("/static", "target/classes"));
        proxyServer = new HttpProxyServer("localhost", targetServer.port(), proxyPort, routes);
        proxyServer.start();
    }

    @After
    public void tearDown() throws Exception {
        proxyServer.stop();
    }

    @Test
    public void should_pass_request_to_target_server() throws IOException {
        // given
        targetServer
                .stubFor(
                        get(urlEqualTo("/"))
                                .willReturn(
                                        aResponse()
                                                .withHeader(CONTENT_TYPE, TYPE)
                                                .withBody(CONTENT)
                                )
                );
        // when
        URLConnection urlConnection = new URL("http://localhost:" + proxyPort).openConnection();
        // then
        assertThat(urlConnection.getContentType()).isEqualTo(TYPE);
        assertThat(urlConnection.getContentLength()).isEqualTo(CONTENT.length());

    }

    @Test
    public void should_act_as_a_file_server() throws IOException {
        // given
        targetServer
                .stubFor(
                        get(urlEqualTo("/"))
                                .willReturn(
                                        aResponse()
                                                .withHeader(CONTENT_TYPE, TYPE)
                                                .withBody(CONTENT)
                                )
                );
        // when
        URLConnection urlConnection = new URL("http://localhost:" + proxyPort + "/static/dummy.html").openConnection();
        // then
        assertThat(urlConnection.getContentType()).isEqualTo(TYPE);
        assertThat(urlConnection.getContentLength()).isNotEqualTo(CONTENT.length());

    }


}