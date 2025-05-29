/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 抛出以表示出现了一个格式错误的 URL。在规范字符串中找不到任何合法的协议，或者字符串无法解析。
 *
 * @author  Arthur van Hoff
 * @since   JDK1.0
 */
public class MalformedURLException extends IOException {
    private static final long serialVersionUID = -182787522200415866L;

    /**
     * 构造一个没有详细消息的 {@code MalformedURLException}。
     */
    public MalformedURLException() {
    }

    /**
     * 使用指定的详细消息构造一个 {@code MalformedURLException}。
     *
     * @param   msg   详细消息。
     */
    public MalformedURLException(String msg) {
        super(msg);
    }
}
