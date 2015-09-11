package org.asynchttpclient;

import static org.asynchttpclient.util.AsyncHttpProviderUtils.*;
import static org.asynchttpclient.util.MiscUtils.isNonEmpty;

import org.asynchttpclient.cookie.Cookie;
import org.asynchttpclient.uri.Uri;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

public abstract class ResponseBase implements Response {

    protected final List<HttpResponseBodyPart> bodyParts;
    protected final HttpResponseHeaders headers;
    protected final HttpResponseStatus status;
    private List<Cookie> cookies;

    protected ResponseBase(HttpResponseStatus status, HttpResponseHeaders headers, List<HttpResponseBodyPart> bodyParts) {
        this.bodyParts = bodyParts;
        this.headers = headers;
        this.status = status;
    }

    protected abstract List<Cookie> buildCookies();

    protected Charset calculateCharset(Charset charset) {

        if (charset == null) {
            String contentType = getContentType();
            if (contentType != null)
                charset = parseCharset(contentType); // parseCharset can return
                                                     // null
        }
        return charset != null ? charset : DEFAULT_CHARSET;
    }

    @Override
    public final int getStatusCode() {
        return status.getStatusCode();
    }

    @Override
    public final String getStatusText() {
        return status.getStatusText();
    }

    @Override
    public final Uri getUri() {
        return status.getUri();
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return status.getRemoteAddress();
    }

    @Override
    public SocketAddress getLocalAddress() {
        return status.getLocalAddress();
    }

    @Override
    public final String getContentType() {
        return headers != null ? getHeader("Content-Type") : null;
    }

    @Override
    public final String getHeader(String name) {
        return headers != null ? getHeaders().getFirstValue(name) : null;
    }

    @Override
    public final List<String> getHeaders(String name) {
        return headers != null ? getHeaders().get(name) : Collections.<String> emptyList();
    }

    @Override
    public final FluentCaseInsensitiveStringsMap getHeaders() {
        return headers != null ? headers.getHeaders() : new FluentCaseInsensitiveStringsMap();
    }

    @Override
    public final boolean isRedirected() {
        switch (status.getStatusCode()) {
        case 301:
        case 302:
        case 303:
        case 307:
        case 308:
            return true;
        default:
            return false;
        }
    }

    @Override
    public List<Cookie> getCookies() {

        if (headers == null) {
            return Collections.emptyList();
        }

        if (cookies == null) {
            cookies = buildCookies();
        }
        return cookies;

    }

    @Override
    public boolean hasResponseStatus() {
        return status != null;
    }

    @Override
    public boolean hasResponseHeaders() {
        return headers != null && isNonEmpty(headers.getHeaders());
    }

    @Override
    public boolean hasResponseBody() {
        return isNonEmpty(bodyParts);
    }

    @Override
    public String toString() {
        try {
            return new StringBuilder()//
                    .append(getClass().getSimpleName()).append(" {\n")//
                    .append("\tstatusCode=").append(getStatusCode()).append("\n")//
                    .append("\theaders=").append(getHeaders()).append("\n")//
                    .append("\tbody=\n").append(getResponseBody()).append("\n")//
                    .append("}").toString();
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred while trying to print response body", e);
        }
    }
}
