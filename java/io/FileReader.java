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
 * 用于读取字符文件的便捷类。此类的构造函数假设默认字符编码和默认字节缓冲区大小是合适的。如果要指定这些值，
 * 请在 FileInputStream 上构造一个 InputStreamReader。
 *
 * <p><code>FileReader</code> 用于读取字符流。对于读取原始字节流，请考虑使用
 * <code>FileInputStream</code>。
 *
 * @see InputStreamReader
 * @see FileInputStream
 *
 * @author      Mark Reinhold
 * @since       JDK1.1
 */
public class FileReader extends InputStreamReader {

   /**
    * 给定要读取的文件名，创建一个新的 <tt>FileReader</tt>。
    *
    * @param fileName 要读取的文件名
    * @exception  FileNotFoundException  如果命名的文件不存在，是目录而不是普通文件，
    *                   或由于其他原因无法打开以进行读取。
    */
    public FileReader(String fileName) throws FileNotFoundException {
        super(new FileInputStream(fileName));
    }

   /**
    * 给定要读取的 <tt>File</tt>，创建一个新的 <tt>FileReader</tt>。
    *
    * @param file 要读取的 <tt>File</tt>
    * @exception  FileNotFoundException  如果文件不存在，是目录而不是普通文件，
    *                   或由于其他原因无法打开以进行读取。
    */
    public FileReader(File file) throws FileNotFoundException {
        super(new FileInputStream(file));
    }

   /**
    * 给定要读取的 <tt>FileDescriptor</tt>，创建一个新的 <tt>FileReader</tt>。
    *
    * @param fd 要读取的 FileDescriptor
    */
    public FileReader(FileDescriptor fd) {
        super(new FileInputStream(fd));
    }

}
