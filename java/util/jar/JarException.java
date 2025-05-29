/*
 * Copyright (c) 1997, 2008, Oracle and/or its affiliates. All rights reserved.
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

package java.util.jar;

/**
 * 表示在从 JAR 文件读取或写入时发生某种错误。
 *
 * @author  David Connelly
 * @since   1.2
 */
public
class JarException extends java.util.zip.ZipException {
    private static final long serialVersionUID = 7159778400963954473L;

    /**
     * 构造一个没有详细消息的 JarException。
     */
    public JarException() {
    }

    /**
     * 使用指定的详细消息构造一个 JarException。
     * @param s 详细消息
     */
    public JarException(String s) {
        super(s);
    }
}
