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
package org.asynchttpclient.channel;

import static org.asynchttpclient.util.MiscUtils.isNonEmpty;

import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.util.SslUtils;

/**
 * Factory that creates an {@link SSLEngine} to be used for a single SSL connection.
 */
public interface SSLEngineFactory {

    /**
     * Creates new {@link SSLEngine}.
     *
     * @return new engine
     * @throws GeneralSecurityException if the SSLEngine cannot be created
     */
    SSLEngine newSSLEngine(String peerHost, int peerPort) throws GeneralSecurityException;

    class DefaultSSLEngineFactory implements SSLEngineFactory {

        private final AsyncHttpClientConfig config;

        public DefaultSSLEngineFactory(AsyncHttpClientConfig config) {
            this.config = config;
        }

        @Override
        public SSLEngine newSSLEngine(String peerHost, int peerPort) throws GeneralSecurityException {
            SSLContext sslContext = SslUtils.getInstance().getSSLContext(config);

            SSLEngine sslEngine = sslContext.createSSLEngine(peerHost, peerPort);
            if (!config.isAcceptAnyCertificate()) {
                SSLParameters params = sslEngine.getSSLParameters();
                params.setEndpointIdentificationAlgorithm("HTTPS");
                sslEngine.setSSLParameters(params);
            }
            sslEngine.setUseClientMode(true);

            if (isNonEmpty(config.getEnabledProtocols()))
                sslEngine.setEnabledProtocols(config.getEnabledProtocols());

            if (isNonEmpty(config.getEnabledCipherSuites()))
                sslEngine.setEnabledCipherSuites(config.getEnabledCipherSuites());

            return sslEngine;
        }
    }
}
