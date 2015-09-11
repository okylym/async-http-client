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
package org.asynchttpclient.channel.pool;

/**
 * Provides an interface for decisions about HTTP connections.
 */
public interface ConnectionStrategy<REQUEST, RESPONSE> {

    /**
     * Determines whether the connection should be kept alive after this HTTP message exchange.
     * @param request the HTTP request
     * @param response the HTTP response
     * @return true if the connection should be kept alive, false if it should be closed.
     */
    boolean keepAlive(REQUEST request, RESPONSE response);
}
