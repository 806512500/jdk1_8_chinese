/*
 * Copyright (c) 1996, 2005, Oracle and/or its affiliates. All rights reserved.
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

package java.io;

/**
 * 表示在写操作期间抛出了 ObjectStreamExceptions 之一。在读操作期间，当在写操作期间抛出了 ObjectStreamExceptions 之一时抛出此异常。终止写操作的异常可以在 detail 字段中找到。流被重置为其初始状态，所有已反序列化的对象的引用都被丢弃。
 *
 * <p>从 1.4 版本开始，此异常已被重新设计以符合通用异常链接机制。构造时提供的“导致中止的异常”以及通过公共 {@link #detail} 字段访问的异常现在被称为<i>原因</i>，也可以通过 {@link Throwable#getCause()} 方法访问，以及上述“遗留字段”。
 *
 * @author  未署名
 * @since   JDK1.1
 */
public class WriteAbortedException extends ObjectStreamException {
    private static final long serialVersionUID = -3326426625597282442L;

    /**
     * 在写 ObjectStream 时捕获的异常。
     *
     * <p>此字段早于通用异常链接机制。现在 {@link Throwable#getCause()} 方法是获取此信息的首选方式。
     *
     * @serial
     */
    public Exception detail;

    /**
     * 使用描述异常的字符串和导致中止的异常构造 WriteAbortedException。
     * @param s   描述异常的字符串。
     * @param ex  导致中止的异常。
     */
    public WriteAbortedException(String s, Exception ex) {
        super(s);
        initCause(null);  // 禁止后续的 initCause
        detail = ex;
    }

    /**
     * 生成消息并包括嵌套异常的消息（如果有）。
     */
    public String getMessage() {
        if (detail == null)
            return super.getMessage();
        else
            return super.getMessage() + "; " + detail.toString();
    }

    /**
     * 返回终止操作的异常（<i>原因</i>）。
     *
     * @return  终止操作的异常（<i>原因</i>），可能为 null。
     * @since   1.4
     */
    public Throwable getCause() {
        return detail;
    }
}
