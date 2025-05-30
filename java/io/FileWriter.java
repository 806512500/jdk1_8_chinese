/*
 * Copyright (c) 1996, 2001, Oracle and/or its affiliates. All rights reserved.
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
 * 用于编写字符文件的便捷类。此类的构造函数假设默认字符编码和默认字节缓冲区大小是可接受的。如果要指定这些值，请在 FileOutputStream 上构造一个 OutputStreamWriter。
 *
 * <p>文件是否可用或可创建取决于底层平台。特别是，某些平台允许一个文件一次只能由一个 <tt>FileWriter</tt>（或其他文件写入对象）打开。在这种情况下，此类中的构造函数如果涉及的文件已打开，则会失败。
 *
 * <p><code>FileWriter</code> 用于编写字符流。对于编写原始字节流，请考虑使用 <code>FileOutputStream</code>。
 *
 * @see OutputStreamWriter
 * @see FileOutputStream
 *
 * @author      Mark Reinhold
 * @since       JDK1.1
 */

public class FileWriter extends OutputStreamWriter {

    /**
     * 给定文件名构造一个 FileWriter 对象。
     *
     * @param fileName  String 系统依赖的文件名。
     * @throws IOException  如果命名的文件存在但不是常规文件，或者不存在但无法创建，或者由于任何其他原因无法打开
     */
    public FileWriter(String fileName) throws IOException {
        super(new FileOutputStream(fileName));
    }

    /**
     * 给定文件名和一个布尔值，指示是否将写入的数据附加到文件末尾。
     *
     * @param fileName  String 系统依赖的文件名。
     * @param append    boolean 如果 <code>true</code>，则数据将写入文件末尾而不是开头。
     * @throws IOException  如果命名的文件存在但不是常规文件，或者不存在但无法创建，或者由于任何其他原因无法打开
     */
    public FileWriter(String fileName, boolean append) throws IOException {
        super(new FileOutputStream(fileName, append));
    }

    /**
     * 给定 File 对象构造一个 FileWriter 对象。
     *
     * @param file  要写入的 File 对象。
     * @throws IOException  如果文件存在但不是常规文件，或者不存在但无法创建，或者由于任何其他原因无法打开
     */
    public FileWriter(File file) throws IOException {
        super(new FileOutputStream(file));
    }

    /**
     * 给定 File 对象构造一个 FileWriter 对象。如果第二个参数为 <code>true</code>，则字节将写入文件末尾而不是开头。
     *
     * @param file  要写入的 File 对象
     * @param     append    如果 <code>true</code>，则字节将写入文件末尾而不是开头
     * @throws IOException  如果文件存在但不是常规文件，或者不存在但无法创建，或者由于任何其他原因无法打开
     * @since 1.4
     */
    public FileWriter(File file, boolean append) throws IOException {
        super(new FileOutputStream(file, append));
    }

    /**
     * 与文件描述符关联的 FileWriter 对象构造函数。
     *
     * @param fd  要写入的 FileDescriptor 对象。
     */
    public FileWriter(FileDescriptor fd) {
        super(new FileOutputStream(fd));
    }

}
