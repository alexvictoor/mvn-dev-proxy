package com.github.alexvictoor.proxy;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class HttpProxyServerTest {

    public static final String TYPE = "text/html";
    public static final String CONTENT = "Hello!";

    @Rule
    public WireMockRule targetServer = new WireMockRule();
    private HttpProxyServer proxyServer;

    @Before
    public void setUp() throws Exception {
        proxyServer = new HttpProxyServer("localhost", 8080, 8081);
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
                                                .withHeader("Content-Type", TYPE)
                                                .withBody(CONTENT)
                                )
                );
        // when
        URLConnection urlConnection = new URL("http://localhost:8081").openConnection();
        // then
        assertThat(urlConnection.getContentType()).isEqualTo(TYPE);
        assertThat(urlConnection.getContentLength()).isEqualTo(CONTENT.length());

    }


}