/*
 * Copyright 2010 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.asynchttpclient;

import static java.nio.charset.StandardCharsets.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.apache.commons.io.IOUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.cookie.Cookie;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for remote site.
 * <p/>
 * see http://github.com/MSch/ning-async-http-client-bug/tree/master
 * 
 * @author Martin Schurrer
 */
public abstract class RemoteSiteTest extends AbstractBasicTest {

    public static final String URL = "http://google.com?q=";
    public static final String REQUEST_PARAM = "github github \n" + "github";

    @Test(groups = { "online", "default_provider" })
    public void testGoogleCom() throws Exception {
        try (AsyncHttpClient c = getAsyncHttpClient(new AsyncHttpClientConfig.Builder().setRequestTimeout(10000).build())) {
            Response response = c.prepareGet("http://www.google.com/").execute().get(10, TimeUnit.SECONDS);
            assertNotNull(response);
        }
    }

    @Test(groups = { "online", "default_provider" })
    public void testMailGoogleCom() throws Exception {
        try (AsyncHttpClient c = getAsyncHttpClient(new AsyncHttpClientConfig.Builder().setRequestTimeout(10000).build())) {
            Response response = c.prepareGet("http://mail.google.com/").execute().get(10, TimeUnit.SECONDS);
            assertNotNull(response);
            assertEquals(response.getStatusCode(), 200);
        }
    }

    @Test(groups = { "online", "default_provider" }, enabled = false)
    // FIXME
    public void testMicrosoftCom() throws Exception {
        try (AsyncHttpClient c = getAsyncHttpClient(new AsyncHttpClientConfig.Builder().setRequestTimeout(10000).build())) {
            Response response = c.prepareGet("http://microsoft.com/").execute().get(10, TimeUnit.SECONDS);
            assertNotNull(response);
            assertEquals(response.getStatusCode(), 301);
        }
    }

    @Test(groups = { "online", "default_provider" }, enabled = false)
    // FIXME
    public void testWwwMicrosoftCom() throws Exception {
        try (AsyncHttpClient c = getAsyncHttpClient(new AsyncHttpClientConfig.Builder().setRequestTimeout(10000).build())) {
            Response response = c.prepareGet("http://www.microsoft.com/").execute().get(10, TimeUnit.SECONDS);
            assertNotNull(response);
            assertEquals(response.getStatusCode(), 302);
        }
    }

    @Test(groups = { "online", "default_provider" }, enabled = false)
    // FIXME
    public void testUpdateMicrosoftCom() throws Exception {
        try (AsyncHttpClient c = getAsyncHttpClient(new AsyncHttpClientConfig.Builder().setRequestTimeout(10000).build())) {
            Response response = c.prepareGet("http://update.microsoft.com/").execute().get(10, TimeUnit.SECONDS);
            assertNotNull(response);
            assertEquals(response.getStatusCode(), 302);
        }
    }

    @Test(groups = { "online", "default_provider" })
    public void testGoogleComWithTimeout() throws Exception {
        try (AsyncHttpClient c = getAsyncHttpClient(new AsyncHttpClientConfig.Builder().setRequestTimeout(10000).build())) {
            Response response = c.prepareGet("http://google.com/").execute().get(10, TimeUnit.SECONDS);
            assertNotNull(response);
            assertTrue(response.getStatusCode() == 301 || response.getStatusCode() == 302);
        }
    }

    @Test(groups = { "online", "default_provider" })
    public void asyncStatusHEADContentLenghtTest() throws Exception {
        try (AsyncHttpClient p = getAsyncHttpClient(new AsyncHttpClientConfig.Builder().setFollowRedirect(true).build())) {
            final CountDownLatch l = new CountDownLatch(1);
            Request request = new RequestBuilder("HEAD").setUrl("http://www.google.com/").build();

            p.executeRequest(request, new AsyncCompletionHandlerAdapter() {
                @Override
                public Response onCompleted(Response response) throws Exception {
                    try {
                        assertEquals(response.getStatusCode(), 200);
                        return response;
                    } finally {
                        l.countDown();
                    }
                }
            }).get();

            if (!l.await(5, TimeUnit.SECONDS)) {
                fail("Timeout out");
            }
        }
    }

    @Test(groups = { "online", "default_provider" }, enabled = false)
    public void invalidStreamTest2() throws Exception {
        AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder().setRequestTimeout(10000).setFollowRedirect(true)
                .setAllowPoolingConnections(false).setMaxRedirects(6).build();

        try (AsyncHttpClient c = getAsyncHttpClient(config)) {
            Response response = c.prepareGet("http://bit.ly/aUjTtG").execute().get();
            if (response != null) {
                System.out.println(response);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            assertNotNull(t.getCause());
            assertEquals(t.getCause().getMessage(), "invalid version format: ICY");
        }
    }

    @Test(groups = { "online", "default_provider" })
    public void asyncFullBodyProperlyRead() throws Exception {
        try (AsyncHttpClient client = getAsyncHttpClient(null)) {
            Response r = client.prepareGet("http://www.cyberpresse.ca/").execute().get();

            InputStream stream = r.getResponseBodyAsStream();
            int contentLength = Integer.valueOf(r.getHeader("Content-Length"));

            assertEquals(contentLength, IOUtils.toByteArray(stream).length);
        }
    }

    // FIXME Get a 302 in France...
    @Test(groups = { "online", "default_provider" }, enabled = false)
    public void testUrlRequestParametersEncoding() throws Exception {
        try (AsyncHttpClient client = getAsyncHttpClient(null)) {
            String requestUrl2 = URL + URLEncoder.encode(REQUEST_PARAM, UTF_8.name());
            logger.info(String.format("Executing request [%s] ...", requestUrl2));
            Response response = client.prepareGet(requestUrl2).execute().get();
            assertEquals(response.getStatusCode(), 302);
        }
    }

    @Test(groups = { "online", "default_provider" })
    public void stripQueryStringTest() throws Exception {

        AsyncHttpClientConfig cg = new AsyncHttpClientConfig.Builder().setFollowRedirect(true).build();
        try (AsyncHttpClient c = getAsyncHttpClient(cg)) {
            Response response = c.prepareGet("http://www.freakonomics.com/?p=55846").execute().get();

            assertNotNull(response);
            assertEquals(response.getStatusCode(), 200);
        }
    }

    @Test(groups = { "online", "default_provider" })
    public void evilCoookieTest() throws Exception {
        try (AsyncHttpClient c = getAsyncHttpClient(null)) {
            RequestBuilder builder2 = new RequestBuilder("GET");
            builder2.setFollowRedirect(true);
            builder2.setUrl("http://www.google.com/");
            builder2.addHeader("Content-Type", "text/plain");
            builder2.addCookie(new Cookie("evilcookie", "test", false, ".google.com", "/", Long.MIN_VALUE, false, false));
            Request request2 = builder2.build();
            Response response = c.executeRequest(request2).get();

            assertNotNull(response);
            assertEquals(response.getStatusCode(), 200);
        }
    }

    @Test(groups = { "online", "default_provider" }, enabled = false)
    public void testAHC62Com() throws Exception {
        try (AsyncHttpClient c = getAsyncHttpClient(new AsyncHttpClientConfig.Builder().setFollowRedirect(true).build())) {
            Response response = c.prepareGet("http://api.crunchbase.com/v/1/financial-organization/kinsey-hills-group.js")
                    .execute(new AsyncHandler<Response>() {

                        private Response.ResponseBuilder builder = new Response.ResponseBuilder();

                        public void onThrowable(Throwable t) {
                            t.printStackTrace();
                        }

                        public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
                            System.out.println(bodyPart.getBodyPartBytes().length);
                            builder.accumulate(bodyPart);

                            return State.CONTINUE;
                        }

                        public State onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
                            builder.accumulate(responseStatus);
                            return State.CONTINUE;
                        }

                        public State onHeadersReceived(HttpResponseHeaders headers) throws Exception {
                            builder.accumulate(headers);
                            return State.CONTINUE;
                        }

                        public Response onCompleted() throws Exception {
                            return builder.build();
                        }
                    }).get(10, TimeUnit.SECONDS);
            assertNotNull(response);
            assertTrue(response.getResponseBody().length() >= 3870);
        }
    }
}
