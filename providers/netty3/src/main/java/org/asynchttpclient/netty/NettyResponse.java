/*
 * Copyright (c) 2014 AsyncHttpClient Project. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at
 *     http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.asynchttpclient.netty;

import static org.asynchttpclient.netty.util.ChannelBufferUtils.channelBuffer2bytes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseHeaders;
import org.asynchttpclient.HttpResponseStatus;
import org.asynchttpclient.ResponseBase;
import org.asynchttpclient.cookie.Cookie;
import org.asynchttpclient.cookie.CookieDecoder;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpHeaders;

/**
 * Wrapper around the {@link org.asynchttpclient.ning.http.client.Response} API.
 */
public class NettyResponse extends ResponseBase {

    public NettyResponse(HttpResponseStatus status, HttpResponseHeaders headers, List<HttpResponseBodyPart> bodyParts) {
        super(status, headers, bodyParts);
    }

    @Override
    public byte[] getResponseBodyAsBytes() throws IOException {
        return channelBuffer2bytes(getResponseBodyAsChannelBuffer());
    }

    @Override
    public ByteBuffer getResponseBodyAsByteBuffer() throws IOException {
        return getResponseBodyAsChannelBuffer().toByteBuffer();
    }

    @Override
    public String getResponseBody() throws IOException {
        return getResponseBody(null);
    }

    public String getResponseBody(Charset charset) throws IOException {
        return getResponseBodyAsChannelBuffer().toString(calculateCharset(charset));
    }

    @Override
    public InputStream getResponseBodyAsStream() throws IOException {
        return new ChannelBufferInputStream(getResponseBodyAsChannelBuffer());
    }

    public ChannelBuffer getResponseBodyAsChannelBuffer() throws IOException {
        ChannelBuffer b = null;
        switch (bodyParts.size()) {
        case 0:
            b = ChannelBuffers.EMPTY_BUFFER;
            break;
        case 1:
            b = NettyResponseBodyPart.class.cast(bodyParts.get(0)).getChannelBuffer();
            break;
        default:
            ChannelBuffer[] channelBuffers = new ChannelBuffer[bodyParts.size()];
            for (int i = 0; i < bodyParts.size(); i++) {
                channelBuffers[i] = NettyResponseBodyPart.class.cast(bodyParts.get(i)).getChannelBuffer();
            }
            b = ChannelBuffers.wrappedBuffer(channelBuffers);
        }

        return b;
    }

    @Override
    protected List<Cookie> buildCookies() {
        List<Cookie> cookies = new ArrayList<>();
        for (Map.Entry<String, List<String>> header : headers.getHeaders().entrySet()) {
            if (header.getKey().equalsIgnoreCase(HttpHeaders.Names.SET_COOKIE)) {
                // TODO: ask for parsed header
                List<String> v = header.getValue();
                for (String value : v) {
                    cookies.add(CookieDecoder.decode(value));
                }
            }
        }
        return cookies;
    }
}
