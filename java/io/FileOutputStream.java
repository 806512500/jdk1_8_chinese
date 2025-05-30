
/*
 * Copyright (c) 1994, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.nio.channels.FileChannel;
import sun.nio.ch.FileChannelImpl;


/**
 * 文件输出流是用于将数据写入 <code>File</code> 或 <code>FileDescriptor</code> 的输出流。文件是否可用或可创建取决于底层平台。某些平台特别允许一个文件在同一时间只能由一个 <tt>FileOutputStream</tt>（或其他文件写入对象）打开。在这种情况下，如果涉及的文件已经打开，此类中的构造函数将失败。
 *
 * <p><code>FileOutputStream</code> 用于写入原始字节流，如图像数据。对于写入字符流，考虑使用 <code>FileWriter</code>。
 *
 * @author  Arthur van Hoff
 * @see     java.io.File
 * @see     java.io.FileDescriptor
 * @see     java.io.FileInputStream
 * @see     java.nio.file.Files#newOutputStream
 * @since   JDK1.0
 */
public
class FileOutputStream extends OutputStream
{
    /**
     * 系统依赖的文件描述符。
     */
    private final FileDescriptor fd;

    /**
     * 如果文件以追加模式打开，则为 true。
     */
    private final boolean append;

    /**
     * 关联的通道，初始化时懒加载。
     */
    private FileChannel channel;

    /**
     * 引用文件的路径
     * （如果流是通过文件描述符创建的，则为 null）
     */
    private final String path;

    private final Object closeLock = new Object();
    private volatile boolean closed = false;

    /**
     * 创建一个文件输出流以写入指定名称的文件。创建一个新的 <code>FileDescriptor</code> 对象来表示此文件连接。
     * <p>
     * 首先，如果有安全经理，其 <code>checkWrite</code> 方法将被调用，参数为 <code>name</code>。
     * <p>
     * 如果文件存在但是一个目录而不是常规文件，或者不存在但无法创建，或者由于其他任何原因无法打开，则抛出 <code>FileNotFoundException</code>。
     *
     * @param      name   系统依赖的文件名
     * @exception  FileNotFoundException  如果文件存在但是一个目录而不是常规文件，或者不存在但无法创建，或者由于其他任何原因无法打开
     * @exception  SecurityException  如果存在安全经理且其 <code>checkWrite</code> 方法拒绝写入文件的权限。
     * @see        java.lang.SecurityManager#checkWrite(java.lang.String)
     */
    public FileOutputStream(String name) throws FileNotFoundException {
        this(name != null ? new File(name) : null, false);
    }

    /**
     * 创建一个文件输出流以写入指定名称的文件。如果第二个参数为 <code>true</code>，则字节将被写入文件的末尾而不是开头。创建一个新的 <code>FileDescriptor</code> 对象来表示此文件连接。
     * <p>
     * 首先，如果有安全经理，其 <code>checkWrite</code> 方法将被调用，参数为 <code>name</code>。
     * <p>
     * 如果文件存在但是一个目录而不是常规文件，或者不存在但无法创建，或者由于其他任何原因无法打开，则抛出 <code>FileNotFoundException</code>。
     *
     * @param     name        系统依赖的文件名
     * @param     append      如果为 <code>true</code>，则字节将被写入文件的末尾而不是开头
     * @exception  FileNotFoundException  如果文件存在但是一个目录而不是常规文件，或者不存在但无法创建，或者由于其他任何原因无法打开。
     * @exception  SecurityException  如果存在安全经理且其 <code>checkWrite</code> 方法拒绝写入文件的权限。
     * @see        java.lang.SecurityManager#checkWrite(java.lang.String)
     * @since     JDK1.1
     */
    public FileOutputStream(String name, boolean append)
        throws FileNotFoundException
    {
        this(name != null ? new File(name) : null, append);
    }

    /**
     * 创建一个文件输出流以写入由指定 <code>File</code> 对象表示的文件。创建一个新的 <code>FileDescriptor</code> 对象来表示此文件连接。
     * <p>
     * 首先，如果有安全经理，其 <code>checkWrite</code> 方法将被调用，参数为 <code>file</code> 表示的路径。
     * <p>
     * 如果文件存在但是一个目录而不是常规文件，或者不存在但无法创建，或者由于其他任何原因无法打开，则抛出 <code>FileNotFoundException</code>。
     *
     * @param      file               要打开以写入的文件。
     * @exception  FileNotFoundException  如果文件存在但是一个目录而不是常规文件，或者不存在但无法创建，或者由于其他任何原因无法打开
     * @exception  SecurityException  如果存在安全经理且其 <code>checkWrite</code> 方法拒绝写入文件的权限。
     * @see        java.io.File#getPath()
     * @see        java.lang.SecurityException
     * @see        java.lang.SecurityManager#checkWrite(java.lang.String)
     */
    public FileOutputStream(File file) throws FileNotFoundException {
        this(file, false);
    }

    /**
     * 创建一个文件输出流以写入由指定 <code>File</code> 对象表示的文件。如果第二个参数为 <code>true</code>，则字节将被写入文件的末尾而不是开头。创建一个新的 <code>FileDescriptor</code> 对象来表示此文件连接。
     * <p>
     * 首先，如果有安全经理，其 <code>checkWrite</code> 方法将被调用，参数为 <code>file</code> 表示的路径。
     * <p>
     * 如果文件存在但是一个目录而不是常规文件，或者不存在但无法创建，或者由于其他任何原因无法打开，则抛出 <code>FileNotFoundException</code>。
     *
     * @param      file               要打开以写入的文件。
     * @param     append      如果为 <code>true</code>，则字节将被写入文件的末尾而不是开头
     * @exception  FileNotFoundException  如果文件存在但是一个目录而不是常规文件，或者不存在但无法创建，或者由于其他任何原因无法打开
     * @exception  SecurityException  如果存在安全经理且其 <code>checkWrite</code> 方法拒绝写入文件的权限。
     * @see        java.io.File#getPath()
     * @see        java.lang.SecurityException
     * @see        java.lang.SecurityManager#checkWrite(java.lang.String)
     * @since 1.4
     */
    public FileOutputStream(File file, boolean append)
        throws FileNotFoundException
    {
        String name = (file != null ? file.getPath() : null);
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(name);
        }
        if (name == null) {
            throw new NullPointerException();
        }
        if (file.isInvalid()) {
            throw new FileNotFoundException("无效的文件路径");
        }
        this.fd = new FileDescriptor();
        fd.attach(this);
        this.append = append;
        this.path = name;

        open(name, append);
    }

    /**
     * 创建一个文件输出流以写入指定的文件描述符，该文件描述符表示文件系统中已存在的文件连接。
     * <p>
     * 首先，如果有安全经理，其 <code>checkWrite</code> 方法将被调用，参数为文件描述符 <code>fdObj</code>。
     * <p>
     * 如果 <code>fdObj</code> 为 null，则抛出 <code>NullPointerException</code>。
     * <p>
     * 如果 <code>fdObj</code> 无效，此构造函数不会抛出异常。但是，如果在结果流上调用方法尝试进行 I/O 操作，将抛出 <code>IOException</code>。
     *
     * @param      fdObj   要打开以写入的文件描述符
     * @exception  SecurityException  如果存在安全经理且其 <code>checkWrite</code> 方法拒绝写入文件描述符的权限。
     * @see        java.lang.SecurityManager#checkWrite(java.io.FileDescriptor)
     */
    public FileOutputStream(FileDescriptor fdObj) {
        SecurityManager security = System.getSecurityManager();
        if (fdObj == null) {
            throw new NullPointerException();
        }
        if (security != null) {
            security.checkWrite(fdObj);
        }
        this.fd = fdObj;
        this.append = false;
        this.path = null;

        fd.attach(this);
    }

    /**
     * 打开具有指定名称的文件，以覆盖或追加模式。
     * @param name 要打开的文件名
     * @param append 文件是否以追加模式打开
     */
    private native void open0(String name, boolean append)
        throws FileNotFoundException;

    // 包装原生调用以允许仪器化
    /**
     * 打开具有指定名称的文件，以覆盖或追加模式。
     * @param name 要打开的文件名
     * @param append 文件是否以追加模式打开
     */
    private void open(String name, boolean append)
        throws FileNotFoundException {
        open0(name, append);
    }

    /**
     * 将指定的字节写入此文件输出流。
     *
     * @param   b   要写入的字节。
     * @param   append   如果写操作首先将位置移动到文件末尾，则为 {@code true}
     */
    private native void write(int b, boolean append) throws IOException;

    /**
     * 将指定的字节写入此文件输出流。实现 <code>OutputStream</code> 的 <code>write</code> 方法。
     *
     * @param      b   要写入的字节。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public void write(int b) throws IOException {
        write(b, append);
    }

    /**
     * 将子数组作为一系列字节写入。
     * @param b 要写入的数据
     * @param off 数据中的起始偏移量
     * @param len 要写入的字节数
     * @param append {@code true} 以首先将位置移动到文件末尾
     * @exception IOException 如果发生 I/O 错误。
     */
    private native void writeBytes(byte b[], int off, int len, boolean append)
        throws IOException;

    /**
     * 将指定字节数组的 <code>b.length</code> 字节写入此文件输出流。
     *
     * @param      b   数据。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public void write(byte b[]) throws IOException {
        writeBytes(b, 0, b.length, append);
    }

    /**
     * 从指定字节数组的偏移量 <code>off</code> 开始写入 <code>len</code> 字节到此文件输出流。
     *
     * @param      b     数据。
     * @param      off   数据中的起始偏移量。
     * @param      len   要写入的字节数。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public void write(byte b[], int off, int len) throws IOException {
        writeBytes(b, off, len, append);
    }

    /**
     * 关闭此文件输出流并释放与此流关联的任何系统资源。此文件输出流将不能再用于写入字节。
     *
     * <p> 如果此流有关联的通道，则通道也将被关闭。
     *
     * @exception  IOException  如果发生 I/O 错误。
     *
     * @revised 1.4
     * @spec JSR-51
     */
    public void close() throws IOException {
        synchronized (closeLock) {
            if (closed) {
                return;
            }
            closed = true;
        }

        if (channel != null) {
            channel.close();
        }

        fd.closeAll(new Closeable() {
            public void close() throws IOException {
               close0();
           }
        });
    }

    /**
     * 返回与此流关联的文件描述符。
     *
     * @return  表示此 <code>FileOutputStream</code> 对象使用的文件系统中文件连接的 <code>FileDescriptor</code> 对象。
     *
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FileDescriptor
     */
     public final FileDescriptor getFD()  throws IOException {
        if (fd != null) {
            return fd;
        }
        throw new IOException();
     }

    /**
     * 返回与此文件输出流关联的唯一 {@link java.nio.channels.FileChannel FileChannel} 对象。
     *
     * <p> 返回的通道的初始 {@link java.nio.channels.FileChannel#position()
     * 位置} 将等于已写入文件的字节数，除非此流处于追加模式，此时它将等于文件的大小。将字节写入此流将相应地增加通道的位置。显式或通过写入更改通道的位置将更改此流的文件位置。
     *
     * @return  与此文件输出流关联的文件通道
     *
     * @since 1.4
     * @spec JSR-51
     */
    public FileChannel getChannel() {
        synchronized (this) {
            if (channel == null) {
                channel = FileChannelImpl.open(fd, path, false, true, append, this);
            }
            return channel;
        }
    }


                /**
     * 清理与文件的连接，并确保当没有更多引用此流时，此文件输出流的
     * <code>close</code> 方法被调用。
     *
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FileInputStream#close()
     */
    protected void finalize() throws IOException {
        if (fd != null) {
            if (fd == FileDescriptor.out || fd == FileDescriptor.err) {
                flush();
            } else {
                /* 如果 fd 是共享的，FileDescriptor 中的引用将确保
                 * 当安全时才调用终结器。所有使用 fd 的引用都已
                 * 变得不可达。我们可以调用 close()
                 */
                close();
            }
        }
    }

    private native void close0() throws IOException;

    private static native void initIDs();

    static {
        initIDs();
    }

}
