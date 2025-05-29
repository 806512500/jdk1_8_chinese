/*
 * Copyright (c) 1995, 2010, Oracle and/or its affiliates. All rights reserved.
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

package java.util.zip;

import java.io.IOException;

/**
 * 表示某种类型的 Zip 异常已发生。
 *
 * @author  未指定
 * @see     java.io.IOException
 * @since   JDK1.0
 */

public
class ZipException extends IOException {
    private static final long serialVersionUID = 8000196834066748623L;

    /**
     * 构造一个 <code>ZipException</code>，其错误详细信息消息为 <code>null</code>。
     */
    public ZipException() {
        super();
    }

    /**
     * 使用指定的详细信息消息构造一个 <code>ZipException</code>。
     *
     * @param   s   详细信息消息。
     */

    public ZipException(String s) {
        super(s);
    }
}
