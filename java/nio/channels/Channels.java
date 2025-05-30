
/*
 * Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.channels;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.util.concurrent.ExecutionException;
import sun.nio.ch.ChannelInputStream;
import sun.nio.cs.StreamDecoder;
import sun.nio.cs.StreamEncoder;


/**
 * 通道和流的实用方法。
 *
 * <p> 本类定义了静态方法，支持 <tt>{@link java.io}</tt> 包中的流类与本包中的通道类之间的互操作。 </p>
 *
 *
 * @author Mark Reinhold
 * @author Mike McCloskey
 * @author JSR-51 专家组
 * @since 1.4
 */

public final class Channels {

    private Channels() { }              // 不允许实例化

    private static void checkNotNull(Object o, String name) {
        if (o == null)
            throw new NullPointerException("\"" + name + "\" is null!");
    }

    /**
     * 将缓冲区中剩余的所有字节写入给定的通道。
     * 如果通道是可选择的，则必须配置为阻塞模式。
     */
    private static void writeFullyImpl(WritableByteChannel ch, ByteBuffer bb)
        throws IOException
    {
        while (bb.remaining() > 0) {
            int n = ch.write(bb);
            if (n <= 0)
                throw new RuntimeException("no bytes written");
        }
    }

    /**
     * 将缓冲区中剩余的所有字节写入给定的通道。
     *
     * @throws  IllegalBlockingModeException
     *          如果通道是可选择的并且配置为非阻塞模式。
     */
    private static void writeFully(WritableByteChannel ch, ByteBuffer bb)
        throws IOException
    {
        if (ch instanceof SelectableChannel) {
            SelectableChannel sc = (SelectableChannel)ch;
            synchronized (sc.blockingLock()) {
                if (!sc.isBlocking())
                    throw new IllegalBlockingModeException();
                writeFullyImpl(ch, bb);
            }
        } else {
            writeFullyImpl(ch, bb);
        }
    }

    // -- 从通道创建字节流 --

    /**
     * 构造一个从给定通道读取字节的流。
     *
     * <p> 结果流的 <tt>read</tt> 方法如果在底层通道处于非阻塞模式时调用，将抛出 {@link IllegalBlockingModeException}。
     * 该流将不会被缓冲，并且不支持 {@link InputStream#mark mark} 或 {@link InputStream#reset reset} 方法。
     * 该流将对多个并发线程访问是安全的。关闭流将导致通道被关闭。 </p>
     *
     * @param  ch
     *         从中读取字节的通道
     *
     * @return 一个新的输入流
     */
    public static InputStream newInputStream(ReadableByteChannel ch) {
        checkNotNull(ch, "ch");
        return new sun.nio.ch.ChannelInputStream(ch);
    }

    /**
     * 构造一个将字节写入给定通道的流。
     *
     * <p> 结果流的 <tt>write</tt> 方法如果在底层通道处于非阻塞模式时调用，将抛出 {@link IllegalBlockingModeException}。
     * 该流将不会被缓冲。该流将对多个并发线程访问是安全的。关闭流将导致通道被关闭。 </p>
     *
     * @param  ch
     *         将要写入字节的通道
     *
     * @return 一个新的输出流
     */
    public static OutputStream newOutputStream(final WritableByteChannel ch) {
        checkNotNull(ch, "ch");

        return new OutputStream() {

                private ByteBuffer bb = null;
                private byte[] bs = null;       // 调用者的前一个数组
                private byte[] b1 = null;

                public synchronized void write(int b) throws IOException {
                   if (b1 == null)
                        b1 = new byte[1];
                    b1[0] = (byte)b;
                    this.write(b1);
                }

                public synchronized void write(byte[] bs, int off, int len)
                    throws IOException
                {
                    if ((off < 0) || (off > bs.length) || (len < 0) ||
                        ((off + len) > bs.length) || ((off + len) < 0)) {
                        throw new IndexOutOfBoundsException();
                    } else if (len == 0) {
                        return;
                    }
                    ByteBuffer bb = ((this.bs == bs)
                                     ? this.bb
                                     : ByteBuffer.wrap(bs));
                    bb.limit(Math.min(off + len, bb.capacity()));
                    bb.position(off);
                    this.bb = bb;
                    this.bs = bs;
                    Channels.writeFully(ch, bb);
                }

                public void close() throws IOException {
                    ch.close();
                }

            };
    }

    /**
     * 构造一个从给定通道读取字节的流。
     *
     * <p> 该流将不会被缓冲，并且不支持 {@link InputStream#mark mark} 或 {@link InputStream#reset reset} 方法。
     * 该流将对多个并发线程访问是安全的。关闭流将导致通道被关闭。 </p>
     *
     * @param  ch
     *         从中读取字节的通道
     *
     * @return 一个新的输入流
     *
     * @since 1.7
     */
    public static InputStream newInputStream(final AsynchronousByteChannel ch) {
        checkNotNull(ch, "ch");
        return new InputStream() {

            private ByteBuffer bb = null;
            private byte[] bs = null;           // 调用者的前一个数组
            private byte[] b1 = null;

            @Override
            public synchronized int read() throws IOException {
                if (b1 == null)
                    b1 = new byte[1];
                int n = this.read(b1);
                if (n == 1)
                    return b1[0] & 0xff;
                return -1;
            }

            @Override
            public synchronized int read(byte[] bs, int off, int len)
                throws IOException
            {
                if ((off < 0) || (off > bs.length) || (len < 0) ||
                    ((off + len) > bs.length) || ((off + len) < 0)) {
                    throw new IndexOutOfBoundsException();
                } else if (len == 0)
                    return 0;

                ByteBuffer bb = ((this.bs == bs)
                                 ? this.bb
                                 : ByteBuffer.wrap(bs));
                bb.position(off);
                bb.limit(Math.min(off + len, bb.capacity()));
                this.bb = bb;
                this.bs = bs;

                boolean interrupted = false;
                try {
                    for (;;) {
                        try {
                            return ch.read(bb).get();
                        } catch (ExecutionException ee) {
                            throw new IOException(ee.getCause());
                        } catch (InterruptedException ie) {
                            interrupted = true;
                        }
                    }
                } finally {
                    if (interrupted)
                        Thread.currentThread().interrupt();
                }
            }

            @Override
            public void close() throws IOException {
                ch.close();
            }
        };
    }

    /**
     * 构造一个将字节写入给定通道的流。
     *
     * <p> 该流将不会被缓冲。该流将对多个并发线程访问是安全的。关闭流将导致通道被关闭。 </p>
     *
     * @param  ch
     *         将要写入字节的通道
     *
     * @return 一个新的输出流
     *
     * @since 1.7
     */
    public static OutputStream newOutputStream(final AsynchronousByteChannel ch) {
        checkNotNull(ch, "ch");
        return new OutputStream() {

            private ByteBuffer bb = null;
            private byte[] bs = null;   // 调用者的前一个数组
            private byte[] b1 = null;

            @Override
            public synchronized void write(int b) throws IOException {
               if (b1 == null)
                    b1 = new byte[1];
                b1[0] = (byte)b;
                this.write(b1);
            }

            @Override
            public synchronized void write(byte[] bs, int off, int len)
                throws IOException
            {
                if ((off < 0) || (off > bs.length) || (len < 0) ||
                    ((off + len) > bs.length) || ((off + len) < 0)) {
                    throw new IndexOutOfBoundsException();
                } else if (len == 0) {
                    return;
                }
                ByteBuffer bb = ((this.bs == bs)
                                 ? this.bb
                                 : ByteBuffer.wrap(bs));
                bb.limit(Math.min(off + len, bb.capacity()));
                bb.position(off);
                this.bb = bb;
                this.bs = bs;

                boolean interrupted = false;
                try {
                    while (bb.remaining() > 0) {
                        try {
                            ch.write(bb).get();
                        } catch (ExecutionException ee) {
                            throw new IOException(ee.getCause());
                        } catch (InterruptedException ie) {
                            interrupted = true;
                        }
                    }
                } finally {
                    if (interrupted)
                        Thread.currentThread().interrupt();
                }
            }

            @Override
            public void close() throws IOException {
                ch.close();
            }
        };
    }


    // -- 从流创建通道 --

    /**
     * 构造一个从给定流读取字节的通道。
     *
     * <p> 结果通道将不会被缓冲；它将简单地将 I/O 操作重定向到给定的流。关闭通道将导致流被关闭。 </p>
     *
     * @param  in
     *         从中读取字节的流
     *
     * @return 一个新的可读字节通道
     */
    public static ReadableByteChannel newChannel(final InputStream in) {
        checkNotNull(in, "in");

        if (in instanceof FileInputStream &&
            FileInputStream.class.equals(in.getClass())) {
            return ((FileInputStream)in).getChannel();
        }

        return new ReadableByteChannelImpl(in);
    }

    private static class ReadableByteChannelImpl
        extends AbstractInterruptibleChannel    // 实际上不可中断
        implements ReadableByteChannel
    {
        InputStream in;
        private static final int TRANSFER_SIZE = 8192;
        private byte buf[] = new byte[0];
        private boolean open = true;
        private Object readLock = new Object();

        ReadableByteChannelImpl(InputStream in) {
            this.in = in;
        }

        public int read(ByteBuffer dst) throws IOException {
            int len = dst.remaining();
            int totalRead = 0;
            int bytesRead = 0;
            synchronized (readLock) {
                while (totalRead < len) {
                    int bytesToRead = Math.min((len - totalRead),
                                               TRANSFER_SIZE);
                    if (buf.length < bytesToRead)
                        buf = new byte[bytesToRead];
                    if ((totalRead > 0) && !(in.available() > 0))
                        break; // 最多阻塞一次
                    try {
                        begin();
                        bytesRead = in.read(buf, 0, bytesToRead);
                    } finally {
                        end(bytesRead > 0);
                    }
                    if (bytesRead < 0)
                        break;
                    else
                        totalRead += bytesRead;
                    dst.put(buf, 0, bytesRead);
                }
                if ((bytesRead < 0) && (totalRead == 0))
                    return -1;

                return totalRead;
            }
        }

        protected void implCloseChannel() throws IOException {
            in.close();
            open = false;
        }
    }


    /**
     * 构造一个将字节写入给定流的通道。
     *
     * <p> 结果通道将不会被缓冲；它将简单地将 I/O 操作重定向到给定的流。关闭通道将导致流被关闭。 </p>
     *
     * @param  out
     *         将要写入字节的流
     *
     * @return 一个新的可写字节通道
     */
    public static WritableByteChannel newChannel(final OutputStream out) {
        checkNotNull(out, "out");

        if (out instanceof FileOutputStream &&
            FileOutputStream.class.equals(out.getClass())) {
                return ((FileOutputStream)out).getChannel();
        }

        return new WritableByteChannelImpl(out);
    }

    private static class WritableByteChannelImpl
        extends AbstractInterruptibleChannel    // 实际上不可中断
        implements WritableByteChannel
    {
        OutputStream out;
        private static final int TRANSFER_SIZE = 8192;
        private byte buf[] = new byte[0];
        private boolean open = true;
        private Object writeLock = new Object();


                    WritableByteChannelImpl(OutputStream out) {
            this.out = out;
        }

        public int write(ByteBuffer src) throws IOException {
            int len = src.remaining();
            int totalWritten = 0;
            synchronized (writeLock) {
                while (totalWritten < len) {
                    int bytesToWrite = Math.min((len - totalWritten),
                                                TRANSFER_SIZE);
                    if (buf.length < bytesToWrite)
                        buf = new byte[bytesToWrite];
                    src.get(buf, 0, bytesToWrite);
                    try {
                        begin();
                        out.write(buf, 0, bytesToWrite);
                    } finally {
                        end(bytesToWrite > 0);
                    }
                    totalWritten += bytesToWrite;
                }
                return totalWritten;
            }
        }

        protected void implCloseChannel() throws IOException {
            out.close();
            open = false;
        }
    }


    // -- 从通道创建字符流 --

    /**
     * 构造一个从给定通道解码字节的读取器。
     *
     * <p> 结果流将包含一个至少 <tt>minBufferCap</tt> 字节的内部输入缓冲区。流的 <tt>read</tt> 方法将根据需要通过从底层通道读取字节来填充缓冲区；如果在需要读取字节时通道处于非阻塞模式，则将抛出 {@link IllegalBlockingModeException}。结果流将不会被缓冲，也不支持 {@link Reader#mark mark} 或 {@link Reader#reset reset} 方法。关闭流将导致通道被关闭。 </p>
     *
     * @param  ch
     *         将从中读取字节的通道
     *
     * @param  dec
     *         要使用的字符集解码器
     *
     * @param  minBufferCap
     *         内部字节缓冲区的最小容量，
     *         或 <tt>-1</tt> 表示使用实现依赖的默认容量
     *
     * @return  一个新的读取器
     */
    public static Reader newReader(ReadableByteChannel ch,
                                   CharsetDecoder dec,
                                   int minBufferCap)
    {
        checkNotNull(ch, "ch");
        return StreamDecoder.forDecoder(ch, dec.reset(), minBufferCap);
    }

    /**
     * 构造一个根据指定字符集名称解码字节的读取器。
     *
     * <p> 该方法的调用形式
     *
     * <blockquote><pre>
     * Channels.newReader(ch, csname)</pre></blockquote>
     *
     * 与表达式
     *
     * <blockquote><pre>
     * Channels.newReader(ch,
     *                    Charset.forName(csName)
     *                        .newDecoder(),
     *                    -1);</pre></blockquote>
     *
     * 的行为完全相同。
     *
     * @param  ch
     *         将从中读取字节的通道
     *
     * @param  csName
     *         要使用的字符集名称
     *
     * @return  一个新的读取器
     *
     * @throws  UnsupportedCharsetException
     *          如果此 Java 虚拟机实例中没有支持指定的字符集
     */
    public static Reader newReader(ReadableByteChannel ch,
                                   String csName)
    {
        checkNotNull(csName, "csName");
        return newReader(ch, Charset.forName(csName).newDecoder(), -1);
    }

    /**
     * 构造一个使用给定编码器编码字符并将结果字节写入给定通道的写入器。
     *
     * <p> 结果流将包含一个至少 <tt>minBufferCap</tt> 字节的内部输出缓冲区。流的 <tt>write</tt> 方法将根据需要通过将字节写入底层通道来刷新缓冲区；如果在需要写入字节时通道处于非阻塞模式，则将抛出 {@link IllegalBlockingModeException}。结果流将不会被缓冲。关闭流将导致通道被关闭。 </p>
     *
     * @param  ch
     *         将写入字节的通道
     *
     * @param  enc
     *         要使用的字符集编码器
     *
     * @param  minBufferCap
     *         内部字节缓冲区的最小容量，
     *         或 <tt>-1</tt> 表示使用实现依赖的默认容量
     *
     * @return  一个新的写入器
     */
    public static Writer newWriter(final WritableByteChannel ch,
                                   final CharsetEncoder enc,
                                   final int minBufferCap)
    {
        checkNotNull(ch, "ch");
        return StreamEncoder.forEncoder(ch, enc.reset(), minBufferCap);
    }

    /**
     * 构造一个根据指定字符集名称编码字符并将结果字节写入给定通道的写入器。
     *
     * <p> 该方法的调用形式
     *
     * <blockquote><pre>
     * Channels.newWriter(ch, csname)</pre></blockquote>
     *
     * 与表达式
     *
     * <blockquote><pre>
     * Channels.newWriter(ch,
     *                    Charset.forName(csName)
     *                        .newEncoder(),
     *                    -1);</pre></blockquote>
     *
     * 的行为完全相同。
     *
     * @param  ch
     *         将写入字节的通道
     *
     * @param  csName
     *         要使用的字符集名称
     *
     * @return  一个新的写入器
     *
     * @throws  UnsupportedCharsetException
     *          如果此 Java 虚拟机实例中没有支持指定的字符集
     */
    public static Writer newWriter(WritableByteChannel ch,
                                   String csName)
    {
        checkNotNull(csName, "csName");
        return newWriter(ch, Charset.forName(csName).newEncoder(), -1);
    }
}
