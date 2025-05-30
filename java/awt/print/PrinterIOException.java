/*
 * Copyright (c) 1998, 2000, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.print;
import java.io.IOException;

/**
 * <code>PrinterIOException</code> 类是 {@link PrinterException} 的子类，
 * 用于表示在打印过程中发生了一些 IO 错误。
 *
 * <p>从 1.4 版本开始，此异常已被改造以符合通用的异常链机制。
 * 构造时提供的 "<code>IOException</code> 使打印任务终止" 通过
 * {@link #getIOException()} 方法访问，现在被称为 <i>原因</i>，
 * 也可以通过 {@link Throwable#getCause()} 方法访问，以及上述“遗留方法”。
 */
public class PrinterIOException extends PrinterException {
    static final long serialVersionUID = 5850870712125932846L;

    /**
     * 使打印任务终止的 IO 错误。
     * @serial
     */
    private IOException mException;

    /**
     * 使用指定的 {@link IOException} 的字符串表示构造新的 <code>PrinterIOException</code>。
     * @param exception 指定的 <code>IOException</code>
     */
    public PrinterIOException(IOException exception) {
        initCause(null);  // 禁止后续的 initCause
        mException = exception;
    }

    /**
     * 返回使打印任务终止的 <code>IOException</code>。
     *
     * <p>此方法早于通用的异常链机制。
     * 现在 {@link Throwable#getCause()} 方法是获取这些信息的首选方式。
     *
     * @return 使打印任务终止的 <code>IOException</code>。
     * @see IOException
     */
    public IOException getIOException() {
        return mException;
    }

    /**
     * 返回此异常的原因（使打印任务终止的 <code>IOException</code>）。
     *
     * @return 此异常的原因。
     * @since 1.4
     */
    public Throwable getCause() {
        return mException;
    }
}
