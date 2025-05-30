
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
 * 该类的实例支持对随机访问文件的读取和写入。随机访问文件的行为类似于存储在文件系统中的一个大型字节数组。有一个类似于光标的指针，称为<em>文件指针</em>；输入操作从文件指针开始读取字节，并将文件指针移动到已读取的字节之后。如果随机访问文件是以读写模式创建的，则输出操作也有效；输出操作从文件指针开始写入字节，并将文件指针移动到已写入的字节之后。输出操作写入当前隐含数组末尾之后的字节会导致数组扩展。文件指针可以通过 {@code getFilePointer} 方法读取，并通过 {@code seek} 方法设置。
 * <p>
 * 通常，如果在读取所需字节数之前到达文件末尾，所有读取例程都会抛出一个 {@code EOFException}（它是 {@code IOException} 的一种）。如果由于任何原因（除了文件末尾）无法读取任何字节，将抛出一个 {@code IOException}，但不包括 {@code EOFException}。特别是，如果流已关闭，可能会抛出 {@code IOException}。
 *
 * @author  未署名
 * @since   JDK1.0
 */

public class RandomAccessFile implements DataOutput, DataInput, Closeable {

    private FileDescriptor fd;
    private FileChannel channel = null;
    private boolean rw;

    /**
     * 引用文件的路径
     * （如果流是通过文件描述符创建的，则为 null）
     */
    private final String path;

    private Object closeLock = new Object();
    private volatile boolean closed = false;

    private static final int O_RDONLY = 1;
    private static final int O_RDWR =   2;
    private static final int O_SYNC =   4;
    private static final int O_DSYNC =  8;

    /**
     * 创建一个随机访问文件流，用于读取和（可选地）写入具有指定名称的文件。创建一个新的 {@link FileDescriptor} 对象来表示与文件的连接。
     *
     * <p> <tt>mode</tt> 参数指定文件打开的访问模式。允许的值及其含义与 <a
     * href="#mode"><tt>RandomAccessFile(File,String)</tt></a> 构造函数中指定的相同。
     *
     * <p>
     * 如果存在安全管理器，其 {@code checkRead} 方法将被调用，参数为 {@code name}，以检查是否允许读取文件。如果模式允许写入，安全管理器的
     * {@code checkWrite} 方法也将被调用，参数为 {@code name}，以检查是否允许写入文件。
     *
     * @param      name   系统相关的文件名
     * @param      mode   访问 <a href="#mode">模式</a>
     * @exception  IllegalArgumentException  如果模式参数不等于 <tt>"r"</tt>、<tt>"rw"</tt>、<tt>"rws"</tt> 或 <tt>"rwd"</tt>
     * @exception FileNotFoundException
     *            如果模式为 <tt>"r"</tt> 但给定字符串不表示一个现有的普通文件，或者模式以 <tt>"rw"</tt> 开头但给定字符串不表示一个现有的、可写的普通文件，并且无法创建一个同名的新普通文件，或者在打开或创建文件时发生其他错误
     * @exception  SecurityException         如果存在安全管理器且其 {@code checkRead} 方法拒绝读取文件，或者模式为 "rw" 且安全管理器的 {@code checkWrite} 方法拒绝写入文件
     * @see        java.lang.SecurityException
     * @see        java.lang.SecurityManager#checkRead(java.lang.String)
     * @see        java.lang.SecurityManager#checkWrite(java.lang.String)
     * @revised 1.4
     * @spec JSR-51
     */
    public RandomAccessFile(String name, String mode)
        throws FileNotFoundException
    {
        this(name != null ? new File(name) : null, mode);
    }

    /**
     * 创建一个随机访问文件流，用于读取和（可选地）写入由 {@link File} 参数指定的文件。创建一个新的 {@link
     * FileDescriptor} 对象来表示此文件连接。
     *
     * <p><a name="mode"><tt>mode</tt></a> 参数指定文件打开的访问模式。允许的值及其含义如下：
     *
     * <table summary="访问模式允许的值及其含义">
     * <tr><th align="left">值</th><th align="left">含义</th></tr>
     * <tr><td valign="top"><tt>"r"</tt></td>
     *     <td> 仅用于读取。调用由此对象生成的任何 <tt>write</tt>
     *     方法将导致抛出 {@link
     *     java.io.IOException}。 </td></tr>
     * <tr><td valign="top"><tt>"rw"</tt></td>
     *     <td> 用于读取和写入。如果文件尚不存在，则将尝试创建它。 </td></tr>
     * <tr><td valign="top"><tt>"rws"</tt></td>
     *     <td> 用于读取和写入，与 <tt>"rw"</tt> 相同，并且要求文件内容或元数据的每次更新都同步写入底层存储设备。 </td></tr>
     * <tr><td valign="top"><tt>"rwd"&nbsp;&nbsp;</tt></td>
     *     <td> 用于读取和写入，与 <tt>"rw"</tt> 相同，并且要求文件内容的每次更新都同步写入底层存储设备。 </td></tr>
     * </table>
     *
     * <tt>"rws"</tt> 和 <tt>"rwd"</tt> 模式类似于 {@link
     * java.nio.channels.FileChannel#force(boolean) force(boolean)} 方法，分别传递 <tt>true</tt> 和 <tt>false</tt> 参数，但它们始终适用于每次 I/O 操作，因此通常更高效。如果文件位于本地存储设备上，则当此类方法的调用返回时，可以保证该调用对文件的所有更改都已写入该设备。这对于确保在系统崩溃时不会丢失关键信息很有用。如果文件不位于本地设备上，则不作此保证。
     *
     * <p><tt>"rwd"</tt> 模式可用于减少执行的 I/O 操作次数。使用 <tt>"rwd"</tt> 只要求文件内容的更新写入存储；使用 <tt>"rws"</tt> 要求文件内容和元数据的更新写入，这通常需要至少一次额外的低级 I/O 操作。
     *
     * <p>如果存在安全管理器，其 {@code checkRead} 方法将被调用，参数为 {@code file} 参数的路径名，以检查是否允许读取文件。如果模式允许写入，安全管理器的 {@code checkWrite} 方法也将被调用，参数为路径，以检查是否允许写入文件。
     *
     * @param      file   文件对象
     * @param      mode   访问模式，如上所述
     * @exception  IllegalArgumentException  如果模式参数不等于 <tt>"r"</tt>、<tt>"rw"</tt>、<tt>"rws"</tt> 或 <tt>"rwd"</tt>
     * @exception FileNotFoundException
     *            如果模式为 <tt>"r"</tt> 但给定文件对象不表示一个现有的普通文件，或者模式以 <tt>"rw"</tt> 开头但给定文件对象不表示一个现有的、可写的普通文件，并且无法创建一个同名的新普通文件，或者在打开或创建文件时发生其他错误
     * @exception  SecurityException         如果存在安全管理器且其 {@code checkRead} 方法拒绝读取文件，或者模式为 "rw" 且安全管理器的 {@code checkWrite} 方法拒绝写入文件
     * @see        java.lang.SecurityManager#checkRead(java.lang.String)
     * @see        java.lang.SecurityManager#checkWrite(java.lang.String)
     * @see        java.nio.channels.FileChannel#force(boolean)
     * @revised 1.4
     * @spec JSR-51
     */
    public RandomAccessFile(File file, String mode)
        throws FileNotFoundException
    {
        String name = (file != null ? file.getPath() : null);
        int imode = -1;
        if (mode.equals("r"))
            imode = O_RDONLY;
        else if (mode.startsWith("rw")) {
            imode = O_RDWR;
            rw = true;
            if (mode.length() > 2) {
                if (mode.equals("rws"))
                    imode |= O_SYNC;
                else if (mode.equals("rwd"))
                    imode |= O_DSYNC;
                else
                    imode = -1;
            }
        }
        if (imode < 0)
            throw new IllegalArgumentException("非法模式 \"" + mode
                                               + "\" 必须是 "
                                               + "\"r\", \"rw\", \"rws\","
                                               + " 或 \"rwd\"");
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(name);
            if (rw) {
                security.checkWrite(name);
            }
        }
        if (name == null) {
            throw new NullPointerException();
        }
        if (file.isInvalid()) {
            throw new FileNotFoundException("无效的文件路径");
        }
        fd = new FileDescriptor();
        fd.attach(this);
        path = name;
        open(name, imode);
    }

    /**
     * 返回与此流关联的不透明文件描述符对象。
     *
     * @return     与此流关联的文件描述符对象。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FileDescriptor
     */
    public final FileDescriptor getFD() throws IOException {
        if (fd != null) {
            return fd;
        }
        throw new IOException();
    }

    /**
     * 返回与此文件关联的唯一 {@link java.nio.channels.FileChannel FileChannel} 对象。
     *
     * <p> 返回的通道的 {@link java.nio.channels.FileChannel#position()
     * 位置} 始终等于此对象的文件指针偏移量，如 {@link
     * #getFilePointer getFilePointer} 方法返回的值。更改此对象的文件指针偏移量，无论是显式更改还是通过读取或写入字节，都会更改通道的位置，反之亦然。通过此对象更改文件的长度将更改通过文件通道看到的长度，反之亦然。
     *
     * @return  与此文件关联的文件通道
     *
     * @since 1.4
     * @spec JSR-51
     */
    public final FileChannel getChannel() {
        synchronized (this) {
            if (channel == null) {
                channel = FileChannelImpl.open(fd, path, true, rw, this);
            }
            return channel;
        }
    }

    /**
     * 打开文件并返回文件描述符。如果 {@code mode} 中的 O_RDWR 位为 true，则文件以读写模式打开，否则文件以只读模式打开。
     * 如果 {@code name} 指向一个目录，将抛出 IOException。
     *
     * @param name 文件名
     * @param mode 模式标志，是上述定义的 O_ 常量的组合
     */
    private native void open0(String name, int mode)
        throws FileNotFoundException;

    // 包装原生调用以允许仪器化
    /**
     * 打开文件并返回文件描述符。如果 {@code mode} 中的 O_RDWR 位为 true，则文件以读写模式打开，否则文件以只读模式打开。
     * 如果 {@code name} 指向一个目录，将抛出 IOException。
     *
     * @param name 文件名
     * @param mode 模式标志，是上述定义的 O_ 常量的组合
     */
    private void open(String name, int mode)
        throws FileNotFoundException {
        open0(name, mode);
    }

    // 'Read' 原语

    /**
     * 从文件中读取一个字节的数据。该字节作为 0 到 255（{@code 0x00-0x0ff}）范围内的整数返回。如果没有输入可用，此方法将阻塞。
     * <p>
     * 虽然 {@code RandomAccessFile} 不是 {@code InputStream} 的子类，但此方法的行为与 {@code InputStream} 的 {@link InputStream#read()} 方法完全相同。
     *
     * @return     下一个字节的数据，如果已到达文件末尾，则返回 {@code -1}。
     * @exception  IOException  如果发生 I/O 错误。如果已到达文件末尾，则不会抛出此异常。
     */
    public int read() throws IOException {
        return read0();
    }

    private native int read0() throws IOException;

    /**
     * 读取一个子数组作为一系列字节。
     * @param b 用于读取数据的缓冲区。
     * @param off 数据的起始偏移量。
     * @param len 要读取的字节数。
     * @exception IOException 如果发生 I/O 错误。
     */
    private native int readBytes(byte b[], int off, int len) throws IOException;


                /**
     * 从这个文件中读取最多 {@code len} 个字节的数据到一个字节数组中。此方法会阻塞，直到至少有一个字节的输入可用。
     * <p>
     * 虽然 {@code RandomAccessFile} 不是 {@code InputStream} 的子类，但此方法的行为与 {@code InputStream} 的
     * {@link InputStream#read(byte[], int, int)} 方法完全相同。
     *
     * @param      b     读取数据的缓冲区。
     * @param      off   数组 {@code b} 中数据写入的起始偏移量。
     * @param      len   最大读取的字节数。
     * @return     读入缓冲区的总字节数，或者如果文件已到达末尾且没有更多数据，则返回 {@code -1}。
     * @exception  IOException 如果由于任何原因（除了文件末尾）无法读取第一个字节，或者随机访问文件已关闭，或者发生其他 I/O 错误。
     * @exception  NullPointerException 如果 {@code b} 为 {@code null}。
     * @exception  IndexOutOfBoundsException 如果 {@code off} 为负数，{@code len} 为负数，或者 {@code len} 大于
     * {@code b.length - off}
     */
    public int read(byte b[], int off, int len) throws IOException {
        return readBytes(b, off, len);
    }

    /**
     * 从这个文件中读取最多 {@code b.length} 个字节的数据到一个字节数组中。此方法会阻塞，直到至少有一个字节的输入可用。
     * <p>
     * 虽然 {@code RandomAccessFile} 不是 {@code InputStream} 的子类，但此方法的行为与 {@code InputStream} 的
     * {@link InputStream#read(byte[])} 方法完全相同。
     *
     * @param      b   读取数据的缓冲区。
     * @return     读入缓冲区的总字节数，或者如果文件已到达末尾且没有更多数据，则返回 {@code -1}。
     * @exception  IOException 如果由于任何原因（除了文件末尾）无法读取第一个字节，或者随机访问文件已关闭，或者发生其他 I/O 错误。
     * @exception  NullPointerException 如果 {@code b} 为 {@code null}。
     */
    public int read(byte b[]) throws IOException {
        return readBytes(b, 0, b.length);
    }

    /**
     * 从这个文件中读取 {@code b.length} 个字节的数据到字节数组中，从当前文件指针开始。此方法会重复从文件中读取，直到请求的字节数被读取。
     * 此方法会阻塞，直到请求的字节数被读取，检测到流的末尾，或者抛出异常。
     *
     * @param      b   读取数据的缓冲区。
     * @exception  EOFException  如果文件在读取所有字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    public final void readFully(byte b[]) throws IOException {
        readFully(b, 0, b.length);
    }

    /**
     * 从这个文件中读取正好 {@code len} 个字节的数据到字节数组中，从当前文件指针开始。此方法会重复从文件中读取，直到请求的字节数被读取。
     * 此方法会阻塞，直到请求的字节数被读取，检测到流的末尾，或者抛出异常。
     *
     * @param      b     读取数据的缓冲区。
     * @param      off   数据的起始偏移量。
     * @param      len   要读取的字节数。
     * @exception  EOFException  如果文件在读取所有字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    public final void readFully(byte b[], int off, int len) throws IOException {
        int n = 0;
        do {
            int count = this.read(b, off + n, len - n);
            if (count < 0)
                throw new EOFException();
            n += count;
        } while (n < len);
    }

    /**
     * 尝试跳过并丢弃 {@code n} 个输入字节。
     * <p>
     *
     * 此方法可能跳过更少的字节数，甚至可能是零。这可能是由于多种情况导致的；到达文件末尾之前没有跳过 {@code n} 个字节只是其中一种可能性。
     * 此方法从不抛出 {@code EOFException}。实际跳过的字节数会返回。如果 {@code n} 为负数，则不跳过任何字节。
     *
     * @param      n   要跳过的字节数。
     * @return     实际跳过的字节数。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public int skipBytes(int n) throws IOException {
        long pos;
        long len;
        long newpos;

        if (n <= 0) {
            return 0;
        }
        pos = getFilePointer();
        len = length();
        newpos = pos + n;
        if (newpos > len) {
            newpos = len;
        }
        seek(newpos);

        /* 返回实际跳过的字节数 */
        return (int) (newpos - pos);
    }

    // 'Write' 原语

    /**
     * 将指定的字节写入此文件。写入从当前文件指针开始。
     *
     * @param      b   要写入的 {@code byte}。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public void write(int b) throws IOException {
        write0(b);
    }

    private native void write0(int b) throws IOException;

    /**
     * 将子数组作为一系列字节写入。
     * @param b 要写入的数据
     * @param off 数据的起始偏移量
     * @param len 要写入的字节数
     * @exception IOException 如果发生 I/O 错误。
     */
    private native void writeBytes(byte b[], int off, int len) throws IOException;

    /**
     * 将指定字节数组的 {@code b.length} 个字节写入此文件，从当前文件指针开始。
     *
     * @param      b   要写入的数据。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public void write(byte b[]) throws IOException {
        writeBytes(b, 0, b.length);
    }

    /**
     * 从偏移量 {@code off} 开始将指定字节数组的 {@code len} 个字节写入此文件。
     *
     * @param      b     要写入的数据。
     * @param      off   数据的起始偏移量。
     * @param      len   要写入的字节数。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public void write(byte b[], int off, int len) throws IOException {
        writeBytes(b, off, len);
    }

    // 'Random access' 功能

    /**
     * 返回此文件中的当前偏移量。
     *
     * @return     从文件开头开始，以字节为单位的偏移量，表示下一次读取或写入的位置。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public native long getFilePointer() throws IOException;

    /**
     * 设置从文件开头开始，以字节为单位的文件指针偏移量，表示下一次读取或写入的位置。偏移量可以设置在文件末尾之外。
     * 将偏移量设置在文件末尾之外不会改变文件长度。只有在写入后文件长度才会改变。
     *
     * @param      pos   从文件开头开始，以字节为单位的偏移量，表示要设置的文件指针位置。
     * @exception  IOException  如果 {@code pos} 小于 {@code 0} 或发生 I/O 错误。
     */
    public void seek(long pos) throws IOException {
        if (pos < 0) {
            throw new IOException("负的 seek 偏移量");
        } else {
            seek0(pos);
        }
    }

    private native void seek0(long pos) throws IOException;

    /**
     * 返回此文件的长度。
     *
     * @return     以字节为单位的文件长度。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public native long length() throws IOException;

    /**
     * 设置此文件的长度。
     *
     * <p> 如果通过 {@code length} 方法返回的当前文件长度大于 {@code newLength} 参数，则文件将被截断。在这种情况下，如果通过
     * {@code getFilePointer} 方法返回的文件偏移量大于 {@code newLength}，则在方法返回后偏移量将等于 {@code newLength}。
     *
     * <p> 如果通过 {@code length} 方法返回的当前文件长度小于 {@code newLength} 参数，则文件将被扩展。在这种情况下，扩展部分的内容未定义。
     *
     * @param      newLength    文件的期望长度
     * @exception  IOException  如果发生 I/O 错误
     * @since      1.2
     */
    public native void setLength(long newLength) throws IOException;

    /**
     * 关闭此随机访问文件流并释放与流关联的系统资源。关闭的随机访问文件不能执行输入或输出操作，也不能重新打开。
     *
     * <p> 如果此文件有关联的通道，则通道也会被关闭。
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

    //
    // 从 DataInputStream 和 DataOutputStream 中“借用”的一些“读取/写入 Java 数据类型”方法。
    //

    /**
     * 从这个文件中读取一个 {@code boolean}。此方法从文件中读取一个字节，从当前文件指针开始。
     * 值 {@code 0} 表示 {@code false}。任何其他值表示 {@code true}。
     * 此方法会阻塞，直到字节被读取，检测到流的末尾，或者抛出异常。
     *
     * @return     读取的 {@code boolean} 值。
     * @exception  EOFException  如果文件已到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    public final boolean readBoolean() throws IOException {
        int ch = this.read();
        if (ch < 0)
            throw new EOFException();
        return (ch != 0);
    }

    /**
     * 从这个文件中读取一个带符号的 8 位值。此方法从文件中读取一个字节，从当前文件指针开始。
     * 如果读取的字节为 {@code b}，其中
     * <code>0&nbsp;&lt;=&nbsp;b&nbsp;&lt;=&nbsp;255</code>，
     * 则结果为：
     * <blockquote><pre>
     *     (byte)(b)
     * </pre></blockquote>
     * <p>
     * 此方法会阻塞，直到字节被读取，检测到流的末尾，或者抛出异常。
     *
     * @return     以带符号的 8 位 {@code byte} 表示的文件中的下一个字节。
     * @exception  EOFException  如果文件已到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    public final byte readByte() throws IOException {
        int ch = this.read();
        if (ch < 0)
            throw new EOFException();
        return (byte)(ch);
    }

    /**
     * 从这个文件中读取一个无符号的 8 位数。此方法从文件中读取一个字节，从当前文件指针开始，并返回该字节。
     * <p>
     * 此方法会阻塞，直到字节被读取，检测到流的末尾，或者抛出异常。
     *
     * @return     以无符号的 8 位数表示的文件中的下一个字节。
     * @exception  EOFException  如果文件已到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    public final int readUnsignedByte() throws IOException {
        int ch = this.read();
        if (ch < 0)
            throw new EOFException();
        return ch;
    }

    /**
     * 从这个文件中读取一个带符号的 16 位数。此方法从文件中读取两个字节，从当前文件指针开始。
     * 如果按顺序读取的两个字节为
     * {@code b1} 和 {@code b2}，其中每个值都在 {@code 0} 和 {@code 255} 之间（包括这两个值），则结果等于：
     * <blockquote><pre>
     *     (short)((b1 &lt;&lt; 8) | b2)
     * </pre></blockquote>
     * <p>
     * 此方法会阻塞，直到两个字节被读取，检测到流的末尾，或者抛出异常。
     *
     * @return     以带符号的 16 位数表示的文件中的下一个两个字节。
     * @exception  EOFException  如果文件在读取两个字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    public final short readShort() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (short)((ch1 << 8) + (ch2 << 0));
    }

    /**
     * 从这个文件中读取一个无符号的 16 位数。此方法从文件中读取两个字节，从当前文件指针开始。
     * 如果按顺序读取的两个字节为
     * {@code b1} 和 {@code b2}，其中
     * <code>0&nbsp;&lt;=&nbsp;b1, b2&nbsp;&lt;=&nbsp;255</code>，
     * 则结果等于：
     * <blockquote><pre>
     *     (b1 &lt;&lt; 8) | b2
     * </pre></blockquote>
     * <p>
     * 此方法会阻塞，直到两个字节被读取，检测到流的末尾，或者抛出异常。
     *
     * @return     以无符号的 16 位整数表示的文件中的下一个两个字节。
     * @exception  EOFException  如果文件在读取两个字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    public final int readUnsignedShort() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (ch1 << 8) + (ch2 << 0);
    }

    /**
     * 从这个文件中读取一个字符。此方法从文件中读取两个字节，从当前文件指针开始。
     * 如果按顺序读取的两个字节为
     * {@code b1} 和 {@code b2}，其中
     * <code>0&nbsp;&lt;=&nbsp;b1,&nbsp;b2&nbsp;&lt;=&nbsp;255</code>，
     * 则结果等于：
     * <blockquote><pre>
     *     (char)((b1 &lt;&lt; 8) | b2)
     * </pre></blockquote>
     * <p>
     * 此方法会阻塞，直到两个字节被读取，检测到流的末尾，或者抛出异常。
     *
     * @return     以 {@code char} 表示的文件中的下一个两个字节。
     * @exception  EOFException  如果文件在读取两个字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    public final char readChar() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (char)((ch1 << 8) + (ch2 << 0));
    }


                /**
     * 从文件中读取一个有符号的32位整数。此方法从文件的当前文件指针开始读取4个字节。
     * 如果读取的字节依次为 {@code b1},
     * {@code b2}, {@code b3}, 和 {@code b4}，其中
     * <code>0&nbsp;&lt;=&nbsp;b1, b2, b3, b4&nbsp;&lt;=&nbsp;255</code>,
     * 那么结果等于：
     * <blockquote><pre>
     *     (b1 &lt;&lt; 24) | (b2 &lt;&lt; 16) + (b3 &lt;&lt; 8) + b4
     * </pre></blockquote>
     * <p>
     * 此方法会阻塞，直到读取了四个字节，检测到流的末尾，或抛出异常。
     *
     * @return     从文件中读取的下一个四个字节，解释为一个
     *             {@code int}。
     * @exception  EOFException  如果文件在读取四个字节之前到达末尾。
     * @exception  IOException   如果发生I/O错误。
     */
    public final int readInt() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        int ch3 = this.read();
        int ch4 = this.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    /**
     * 从文件中读取一个有符号的64位整数。此方法从文件的当前文件指针开始读取八个字节。
     * 如果读取的字节依次为
     * {@code b1}, {@code b2}, {@code b3},
     * {@code b4}, {@code b5}, {@code b6},
     * {@code b7}, 和 {@code b8,} 其中：
     * <blockquote><pre>
     *     0 &lt;= b1, b2, b3, b4, b5, b6, b7, b8 &lt;=255,
     * </pre></blockquote>
     * <p>
     * 那么结果等于：
     * <blockquote><pre>
     *     ((long)b1 &lt;&lt; 56) + ((long)b2 &lt;&lt; 48)
     *     + ((long)b3 &lt;&lt; 40) + ((long)b4 &lt;&lt; 32)
     *     + ((long)b5 &lt;&lt; 24) + ((long)b6 &lt;&lt; 16)
     *     + ((long)b7 &lt;&lt; 8) + b8
     * </pre></blockquote>
     * <p>
     * 此方法会阻塞，直到读取了八个字节，检测到流的末尾，或抛出异常。
     *
     * @return     从文件中读取的下一个八个字节，解释为一个
     *             {@code long}。
     * @exception  EOFException  如果文件在读取八个字节之前到达末尾。
     * @exception  IOException   如果发生I/O错误。
     */
    public final long readLong() throws IOException {
        return ((long)(readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
    }

    /**
     * 从文件中读取一个 {@code float}。此方法从文件的当前文件指针开始读取一个
     * {@code int} 值，就像 {@code readInt} 方法一样，
     * 然后使用 {@code Float} 类中的 {@code intBitsToFloat} 方法
     * 将该 {@code int} 转换为一个 {@code float}。
     * <p>
     * 此方法会阻塞，直到读取了四个字节，检测到流的末尾，或抛出异常。
     *
     * @return     从文件中读取的下一个四个字节，解释为一个
     *             {@code float}。
     * @exception  EOFException  如果文件在读取四个字节之前到达末尾。
     * @exception  IOException   如果发生I/O错误。
     * @see        java.io.RandomAccessFile#readInt()
     * @see        java.lang.Float#intBitsToFloat(int)
     */
    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * 从文件中读取一个 {@code double}。此方法从文件的当前文件指针开始读取一个
     * {@code long} 值，就像 {@code readLong} 方法一样，
     * 然后使用 {@code Double} 类中的 {@code longBitsToDouble} 方法
     * 将该 {@code long} 转换为一个 {@code double}。
     * <p>
     * 此方法会阻塞，直到读取了八个字节，检测到流的末尾，或抛出异常。
     *
     * @return     从文件中读取的下一个八个字节，解释为一个
     *             {@code double}。
     * @exception  EOFException  如果文件在读取八个字节之前到达末尾。
     * @exception  IOException   如果发生I/O错误。
     * @see        java.io.RandomAccessFile#readLong()
     * @see        java.lang.Double#longBitsToDouble(long)
     */
    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    /**
     * 从文件中读取下一行文本。此方法从文件的当前文件指针开始依次读取字节，
     * 直到遇到行终止符或文件末尾。每个字节通过取其值的低八位作为字符的低八位，
     * 并将字符的高八位设置为零来转换为字符。因此，此方法不支持完整的Unicode字符集。
     *
     * <p> 行终止符可以是回车符
     * ({@code '\u005Cr'})，换行符 ({@code '\u005Cn'})，
     * 回车符后紧跟一个换行符，或文件末尾。行终止符被丢弃，不作为返回字符串的一部分。
     *
     * <p> 此方法会阻塞，直到读取到换行符，回车符及其后的字节（以检查是否为换行符），
     * 文件末尾，或抛出异常。
     *
     * @return     从文件中读取的下一行文本，如果在读取任何字节之前遇到文件末尾，则返回null。
     * @exception  IOException  如果发生I/O错误。
     */

    public final String readLine() throws IOException {
        StringBuffer input = new StringBuffer();
        int c = -1;
        boolean eol = false;

        while (!eol) {
            switch (c = read()) {
            case -1:
            case '\n':
                eol = true;
                break;
            case '\r':
                eol = true;
                long cur = getFilePointer();
                if ((read()) != '\n') {
                    seek(cur);
                }
                break;
            default:
                input.append((char)c);
                break;
            }
        }

        if ((c == -1) && (input.length() == 0)) {
            return null;
        }
        return input.toString();
    }

    /**
     * 从文件中读取一个字符串。该字符串使用
     * <a href="DataInput.html#modified-utf-8">修改的UTF-8</a>
     * 格式编码。
     * <p>
     * 从文件的当前文件指针开始读取前两个字节，就像
     * {@code readUnsignedShort} 方法一样。这个值给出了编码字符串的字节数，而不是
     * 结果字符串的长度。然后将这些字节解释为修改的UTF-8格式的字符编码，并转换为字符。
     * <p>
     * 此方法会阻塞，直到读取了所有字节，检测到流的末尾，或抛出异常。
     *
     * @return     一个Unicode字符串。
     * @exception  EOFException            如果文件在读取所有字节之前到达末尾。
     * @exception  IOException             如果发生I/O错误。
     * @exception  UTFDataFormatException  如果字节不表示
     *               有效的修改UTF-8编码的Unicode字符串。
     * @see        java.io.RandomAccessFile#readUnsignedShort()
     */
    public final String readUTF() throws IOException {
        return DataInputStream.readUTF(this);
    }

    /**
     * 将一个 {@code boolean} 写入文件，作为一个字节的值。值 {@code true} 被写入为值
     * {@code (byte)1}；值 {@code false} 被写入为值
     * {@code (byte)0}。写入从文件指针的当前位置开始。
     *
     * @param      v   要写入的 {@code boolean} 值。
     * @exception  IOException  如果发生I/O错误。
     */
    public final void writeBoolean(boolean v) throws IOException {
        write(v ? 1 : 0);
        //written++;
    }

    /**
     * 将一个 {@code byte} 写入文件，作为一个字节的值。写入从文件指针的当前位置开始。
     *
     * @param      v   要写入的 {@code byte} 值。
     * @exception  IOException  如果发生I/O错误。
     */
    public final void writeByte(int v) throws IOException {
        write(v);
        //written++;
    }

    /**
     * 将一个 {@code short} 写入文件，作为两个字节，高字节在前。写入从文件指针的当前位置开始。
     *
     * @param      v   要写入的 {@code short}。
     * @exception  IOException  如果发生I/O错误。
     */
    public final void writeShort(int v) throws IOException {
        write((v >>> 8) & 0xFF);
        write((v >>> 0) & 0xFF);
        //written += 2;
    }

    /**
     * 将一个 {@code char} 写入文件，作为两个字节的值，高字节在前。写入从文件指针的当前位置开始。
     *
     * @param      v   要写入的 {@code char} 值。
     * @exception  IOException  如果发生I/O错误。
     */
    public final void writeChar(int v) throws IOException {
        write((v >>> 8) & 0xFF);
        write((v >>> 0) & 0xFF);
        //written += 2;
    }

    /**
     * 将一个 {@code int} 写入文件，作为四个字节，高字节在前。写入从文件指针的当前位置开始。
     *
     * @param      v   要写入的 {@code int}。
     * @exception  IOException  如果发生I/O错误。
     */
    public final void writeInt(int v) throws IOException {
        write((v >>> 24) & 0xFF);
        write((v >>> 16) & 0xFF);
        write((v >>>  8) & 0xFF);
        write((v >>>  0) & 0xFF);
        //written += 4;
    }

    /**
     * 将一个 {@code long} 写入文件，作为八个字节，高字节在前。写入从文件指针的当前位置开始。
     *
     * @param      v   要写入的 {@code long}。
     * @exception  IOException  如果发生I/O错误。
     */
    public final void writeLong(long v) throws IOException {
        write((int)(v >>> 56) & 0xFF);
        write((int)(v >>> 48) & 0xFF);
        write((int)(v >>> 40) & 0xFF);
        write((int)(v >>> 32) & 0xFF);
        write((int)(v >>> 24) & 0xFF);
        write((int)(v >>> 16) & 0xFF);
        write((int)(v >>>  8) & 0xFF);
        write((int)(v >>>  0) & 0xFF);
        //written += 8;
    }

    /**
     * 使用 {@code Float} 类中的 {@code floatToIntBits} 方法将
     * {@code float} 参数转换为一个 {@code int}，然后将该 {@code int} 值作为
     * 四个字节的量写入文件，高字节在前。写入从文件指针的当前位置开始。
     *
     * @param      v   要写入的 {@code float} 值。
     * @exception  IOException  如果发生I/O错误。
     * @see        java.lang.Float#floatToIntBits(float)
     */
    public final void writeFloat(float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }

    /**
     * 使用 {@code Double} 类中的 {@code doubleToLongBits} 方法将
     * {@code double} 参数转换为一个 {@code long}，然后将该 {@code long} 值作为
     * 八个字节的量写入文件，高字节在前。写入从文件指针的当前位置开始。
     *
     * @param      v   要写入的 {@code double} 值。
     * @exception  IOException  如果发生I/O错误。
     * @see        java.lang.Double#doubleToLongBits(double)
     */
    public final void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

    /**
     * 将字符串作为一系列字节写入文件。字符串中的每个字符依次写入，丢弃其高八位。写入从文件指针的当前位置开始。
     *
     * @param      s   要写入的字符串。
     * @exception  IOException  如果发生I/O错误。
     */
    @SuppressWarnings("deprecation")
    public final void writeBytes(String s) throws IOException {
        int len = s.length();
        byte[] b = new byte[len];
        s.getBytes(0, len, b, 0);
        writeBytes(b, 0, len);
    }

    /**
     * 将字符串作为一系列字符写入文件。每个字符都像 {@code writeChar} 方法一样写入数据输出流。写入从文件指针的当前位置开始。
     *
     * @param      s   要写入的 {@code String} 值。
     * @exception  IOException  如果发生I/O错误。
     * @see        java.io.RandomAccessFile#writeChar(int)
     */
    public final void writeChars(String s) throws IOException {
        int clen = s.length();
        int blen = 2*clen;
        byte[] b = new byte[blen];
        char[] c = new char[clen];
        s.getChars(0, clen, c, 0);
        for (int i = 0, j = 0; i < clen; i++) {
            b[j++] = (byte)(c[i] >>> 8);
            b[j++] = (byte)(c[i] >>> 0);
        }
        writeBytes(b, 0, blen);
    }

    /**
     * 以机器无关的方式使用
     * <a href="DataInput.html#modified-utf-8">修改的UTF-8</a>
     * 编码将字符串写入文件。
     * <p>
     * 首先，从文件的当前文件指针开始写入两个字节，就像
     * {@code writeShort} 方法一样，给出要跟随的字节数。这个值是实际写入的字节数，
     * 而不是字符串的长度。在长度之后，字符串中的每个字符依次输出，使用修改的UTF-8编码。
     *
     * @param      str   要写入的字符串。
     * @exception  IOException  如果发生I/O错误。
     */
    public final void writeUTF(String str) throws IOException {
        DataOutputStream.writeUTF(str, this);
    }

    private static native void initIDs();

    private native void close0() throws IOException;

    static {
        initIDs();
    }
}
