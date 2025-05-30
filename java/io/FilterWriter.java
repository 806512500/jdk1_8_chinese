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
 * 用于写入过滤字符流的抽象类。
 * 抽象类 <code>FilterWriter</code> 本身
 * 提供了将所有请求传递给
 * 包含的流的默认方法。 <code>FilterWriter</code>
 * 的子类应覆盖某些这些方法，并且可以
 * 提供额外的方法和字段。
 *
 * @author      Mark Reinhold
 * @since       JDK1.1
 */

public abstract class FilterWriter extends Writer {

    /**
     * 底层字符输出流。
     */
    protected Writer out;

    /**
     * 创建一个新的过滤写入器。
     *
     * @param out  一个 Writer 对象，提供底层流。
     * @throws NullPointerException 如果 <code>out</code> 为 <code>null</code>
     */
    protected FilterWriter(Writer out) {
        super(out);
        this.out = out;
    }

    /**
     * 写入单个字符。
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public void write(int c) throws IOException {
        out.write(c);
    }

    /**
     * 写入字符数组的一部分。
     *
     * @param  cbuf  要写入的字符缓冲区
     * @param  off   开始读取字符的偏移量
     * @param  len   要写入的字符数
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public void write(char cbuf[], int off, int len) throws IOException {
        out.write(cbuf, off, len);
    }

    /**
     * 写入字符串的一部分。
     *
     * @param  str  要写入的字符串
     * @param  off  开始读取字符的偏移量
     * @param  len  要写入的字符数
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public void write(String str, int off, int len) throws IOException {
        out.write(str, off, len);
    }

    /**
     * 刷新流。
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public void flush() throws IOException {
        out.flush();
    }

    public void close() throws IOException {
        out.close();
    }

}
