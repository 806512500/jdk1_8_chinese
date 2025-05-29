/*
 * Copyright (c) 2004, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
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
     * 从指定的响应代码和异常详细消息构造一个新的 {@code HttpRetryException}
     *
     * @param   detail   详细消息。
     * @param   code   服务器返回的 HTTP 响应代码。
     */
    public HttpRetryException(String detail, int code) {
        super(detail);
        responseCode = code;
    }

    /**
     * 使用详细消息、响应代码和 Location 响应头字段的内容构造一个新的 {@code HttpRetryException}
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
     * 返回 HTTP 响应代码
     *
     * @return  HTTP 响应代码。
     */
    public int responseCode() {
        return responseCode;
    }

    /**
     * 返回一个字符串，解释为什么 HTTP 请求无法重试。
     *
     * @return  原因字符串
     */
    public String getReason() {
        return super.getMessage();
    }

    /**
     * 如果错误是由于重定向导致的，则返回 Location 头字段的值。
     *
     * @return  位置字符串
     */
    public String getLocation() {
        return location;
    }
}
