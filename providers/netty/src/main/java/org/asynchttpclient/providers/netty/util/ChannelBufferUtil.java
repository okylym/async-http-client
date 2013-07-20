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
package org.asynchttpclient.providers.netty.util;

import org.jboss.netty.buffer.ChannelBuffer;

public class ChannelBufferUtil {

    public static byte[] channelBuffer2bytes(ChannelBuffer b) {
        int readable = b.readableBytes();
        int readerIndex = b.readerIndex();
        if (b.hasArray()) {
            byte[] array = b.array();
            if (b.arrayOffset() == 0 && readerIndex == 0 && array.length == readable) {
                return array;
            }
        }
        byte[] array = new byte[readable];
        b.getBytes(readerIndex, array);
        return array;
    }
}