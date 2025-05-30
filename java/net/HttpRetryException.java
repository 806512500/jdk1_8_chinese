/*
 * Copyright (c) 2004, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.net;

import java.io.IOException;

/**
 * 抛出以指示 HTTP 请求需要重试，但由于启用了流模式而无法自动重试。
 *
 * @author  Michael McMahon
 * @since   1.5
 */
public
class HttpRetryException extends IOException {
    private static final long serialVersionUID = -9186022286469111381L;

    private int responseCode;
    private String location;

    /**
     * 从指定的响应代码和异常详细消息构造新的 {@code HttpRetryException}。
     *
     * @param   detail   详细消息。
     * @param   code   服务器返回的 HTTP 响应代码。
     */
    public HttpRetryException(String detail, int code) {
        super(detail);
        responseCode = code;
    }

    /**
     * 用详细消息、响应代码和 Location 响应头字段的内容构造新的 {@code HttpRetryException}。
     *
     * @param   detail   详细消息。
     * @param   code   服务器返回的 HTTP 响应代码。
     * @param   location   要重定向到的 URL。
     */
    public HttpRetryException(String detail, int code, String location) {
        super (detail);
        responseCode = code;
        this.location = location;
    }

    /**
     * 返回 HTTP 响应代码。
     *
     * @return  HTTP 响应代码。
     */
    public int responseCode() {
        return responseCode;
    }

    /**
     * 返回一个解释为什么 HTTP 请求无法重试的字符串。
     *
     * @return  原因字符串。
     */
    public String getReason() {
        return super.getMessage();
    }

    /**
     * 如果错误是由重定向引起的，则返回 Location 头字段的值。
     *
     * @return  位置字符串。
     */
    public String getLocation() {
        return location;
    }
}
