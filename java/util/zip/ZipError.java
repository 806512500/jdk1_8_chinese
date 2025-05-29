/*
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 表示发生了不可恢复的错误。
 *
 * @author  Dave Bristor
 * @since   1.6
 */
public class ZipError extends InternalError {
    private static final long serialVersionUID = 853973422266861979L;

    /**
     * 使用给定的详细消息构造一个 ZipError。
     * @param s 包含详细消息的 {@code String}
     */
    public ZipError(String s) {
        super(s);
    }
}
