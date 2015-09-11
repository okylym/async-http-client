/*
 * Copyright (c) 2014 AsyncHttpClient Project. All rights reserved.
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
package org.asynchttpclient.cookie;

import org.asynchttpclient.util.StringUtils;

import java.util.Collection;

public final class CookieEncoder {

    private CookieEncoder() {
    }

    public static String encode(Collection<Cookie> cookies) {
        StringBuilder sb = StringUtils.stringBuilder();

        for (Cookie cookie : cookies) {
            add(sb, cookie.getName(), cookie.getValue(), cookie.isWrap());
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }

    private static void add(StringBuilder sb, String name, String val, boolean wrap) {

        if (val == null) {
            val = "";
        }

        sb.append(name);
        sb.append('=');
        if (wrap)
            sb.append('"').append(val).append('"');
        else
            sb.append(val);
        sb.append(';');
        sb.append(' ');
    }
}
