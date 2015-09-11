/*
 * Copyright (c) 2010-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.asynchttpclient.proxy;

import static org.asynchttpclient.test.TestUtils.findFreePort;
import static org.asynchttpclient.test.TestUtils.newJettyHttpServer;
import static org.asynchttpclient.test.TestUtils.newJettyHttpsServer;
import static org.testng.Assert.assertEquals;

import org.asynchttpclient.AbstractBasicTest;
import org.asynchttpclient.AsyncCompletionHandlerBase;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.asynchttpclient.proxy.ProxyServer;
import org.asynchttpclient.simple.SimpleAsyncHttpClient;
import org.asynchttpclient.test.EchoHandler;
import org.eclipse.jetty.proxy.ConnectHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * Proxy usage tests.
 */
public abstract class ProxyTunnellingTest extends AbstractBasicTest {

    private Server server2;

    public AbstractHandler configureHandler() throws Exception {
        return new ConnectHandler();
    }

    @BeforeClass(alwaysRun = true)
    public void setUpGlobal() throws Exception {
        port1 = findFreePort();
        server = newJettyHttpServer(port1);
        server.setHandler(configureHandler());
        server.start();

        port2 = findFreePort();

        server2 = newJettyHttpsServer(port2);
        server2.setHandler(new EchoHandler());
        server2.start();

        logger.info("Local HTTP server started successfully");
    }

    @AfterClass(alwaysRun = true)
    public void tearDownGlobal() throws Exception {
        server.stop();
        server2.stop();
    }

    @Test(groups = { "online", "default_provider" })
    public void testRequestProxy() throws IOException, InterruptedException, ExecutionException, TimeoutException {

        ProxyServer ps = new ProxyServer(ProxyServer.Protocol.HTTPS, "127.0.0.1", port1);

        AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder()//
        .setFollowRedirect(true)//
        .setAcceptAnyCertificate(true)//
        .build();

        try (AsyncHttpClient asyncHttpClient = getAsyncHttpClient(config)) {
            RequestBuilder rb = new RequestBuilder("GET").setProxyServer(ps).setUrl(getTargetUrl2());
            Future<Response> responseFuture = asyncHttpClient.executeRequest(rb.build(), new AsyncCompletionHandlerBase() {

                public void onThrowable(Throwable t) {
                    t.printStackTrace();
                    logger.debug(t.getMessage(), t);
                }

                @Override
                public Response onCompleted(Response response) throws Exception {
                    return response;
                }
            });
            Response r = responseFuture.get();
            assertEquals(r.getStatusCode(), 200);
            assertEquals(r.getHeader("X-Connection"), "keep-alive");
        }
    }

    @Test(groups = { "online", "default_provider" })
    public void testConfigProxy() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder()//
                .setFollowRedirect(true)//
                .setProxyServer(new ProxyServer(ProxyServer.Protocol.HTTPS, "127.0.0.1", port1))//
                .setAcceptAnyCertificate(true)//
                .build();
        try (AsyncHttpClient asyncHttpClient = getAsyncHttpClient(config)) {
            Future<Response> responseFuture = asyncHttpClient.executeRequest(new RequestBuilder("GET").setUrl(getTargetUrl2()).build(), new AsyncCompletionHandlerBase() {

                public void onThrowable(Throwable t) {
                    t.printStackTrace();
                    logger.debug(t.getMessage(), t);
                }

                @Override
                public Response onCompleted(Response response) throws Exception {
                    return response;
                }
            });
            Response r = responseFuture.get();
            assertEquals(r.getStatusCode(), 200);
            assertEquals(r.getHeader("X-Connection"), "keep-alive");
        }
    }

    @Test(groups = { "online", "default_provider" })
    public void testSimpleAHCConfigProxy() throws IOException, InterruptedException, ExecutionException, TimeoutException {

        try (SimpleAsyncHttpClient client = new SimpleAsyncHttpClient.Builder()//
                .setProxyProtocol(ProxyServer.Protocol.HTTPS)//
                .setProxyHost("127.0.0.1")//
                .setProxyPort(port1)//
                .setFollowRedirect(true)//
                .setUrl(getTargetUrl2())//
                .setAcceptAnyCertificate(true)//
                .setHeader("Content-Type", "text/html")//
                .build()) {
            Response r = client.get().get();

            assertEquals(r.getStatusCode(), 200);
            assertEquals(r.getHeader("X-Connection"), "keep-alive");
        }
    }
}
