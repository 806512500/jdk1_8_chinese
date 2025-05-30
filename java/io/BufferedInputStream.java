
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
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * <code>BufferedInputStream</code> 为另一个输入流添加了功能——即缓冲输入的能力以及支持 <code>mark</code> 和 <code>reset</code> 方法。当创建 <code>BufferedInputStream</code> 时，会创建一个内部缓冲数组。当从流中读取或跳过字节时，如果需要，会从包含的输入流中一次读取多个字节以重新填充内部缓冲区。<code>mark</code> 操作会记住输入流中的一个点，而 <code>reset</code> 操作会使自上次 <code>mark</code> 操作以来读取的所有字节在从包含的输入流中读取新字节之前重新读取。
 *
 * @author  Arthur van Hoff
 * @since   JDK1.0
 */
public
class BufferedInputStream extends FilterInputStream {

    private static int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * 可分配的最大数组大小。某些虚拟机在数组中保留一些头字。尝试分配更大的数组可能会导致
     * OutOfMemoryError: 请求的数组大小超过虚拟机限制。
     */
    private static int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;

    /**
     * 存储数据的内部缓冲数组。必要时，它可以被另一个大小不同的数组替换。
     */
    protected volatile byte buf[];

    /**
     * 提供 buf 的 compareAndSet 的原子更新器。这是必要的，因为关闭可能是异步的。我们使用 buf[] 的 null 性作为此流是否已关闭的主要指示器。（“in”字段在关闭时也会被置为 null。）
     */
    private static final
        AtomicReferenceFieldUpdater<BufferedInputStream, byte[]> bufUpdater =
        AtomicReferenceFieldUpdater.newUpdater
        (BufferedInputStream.class,  byte[].class, "buf");

    /**
     * 缓冲区中最后一个有效字节的索引加一。
     * 这个值始终在 <code>0</code> 到 <code>buf.length</code> 的范围内；
     * 元素 <code>buf[0]</code> 到 <code>buf[count-1]</code> 包含从底层输入流中获取的缓冲输入数据。
     */
    protected int count;

    /**
     * 缓冲区中的当前位置。这是 <code>buf</code> 数组中下一个要读取的字符的索引。
     * <p>
     * 这个值始终在 <code>0</code> 到 <code>count</code> 的范围内。如果它小于 <code>count</code>，则 <code>buf[pos]</code> 是下一个要提供的输入字节；
     * 如果它等于 <code>count</code>，则下一个 <code>read</code> 或 <code>skip</code> 操作将需要从包含的输入流中读取更多字节。
     *
     * @see     java.io.BufferedInputStream#buf
     */
    protected int pos;

    /**
     * 上次调用 <code>mark</code> 方法时 <code>pos</code> 字段的值。
     * <p>
     * 这个值始终在 <code>-1</code> 到 <code>pos</code> 的范围内。
     * 如果输入流中没有标记位置，这个字段是 <code>-1</code>。如果输入流中有标记位置，则 <code>buf[markpos]</code> 是 <code>reset</code> 操作后要提供的第一个输入字节。如果 <code>markpos</code> 不是 <code>-1</code>，则从 <code>buf[markpos]</code> 到 <code>buf[pos-1]</code> 的所有字节必须保留在缓冲数组中（尽管它们可以移动到缓冲数组的另一个位置，并相应调整 <code>count</code>、<code>pos</code> 和 <code>markpos</code> 的值）；它们不能被丢弃，除非 <code>pos</code> 和 <code>markpos</code> 之间的差值超过 <code>marklimit</code>。
     *
     * @see     java.io.BufferedInputStream#mark(int)
     * @see     java.io.BufferedInputStream#pos
     */
    protected int markpos = -1;

    /**
     * 调用 <code>mark</code> 方法后允许的最大读取提前量，之后对 <code>reset</code> 方法的调用将失败。
     * 每当 <code>pos</code> 和 <code>markpos</code> 之间的差值超过 <code>marklimit</code> 时，可以通过将 <code>markpos</code> 设置为 <code>-1</code> 来取消标记。
     *
     * @see     java.io.BufferedInputStream#mark(int)
     * @see     java.io.BufferedInputStream#reset()
     */
    protected int marklimit;

    /**
     * 检查底层输入流是否因关闭而被置为 null；如果不是，则返回它。
     */
    private InputStream getInIfOpen() throws IOException {
        InputStream input = in;
        if (input == null)
            throw new IOException("Stream closed");
        return input;
    }

    /**
     * 检查缓冲区是否因关闭而被置为 null；如果不是，则返回它。
     */
    private byte[] getBufIfOpen() throws IOException {
        byte[] buffer = buf;
        if (buffer == null)
            throw new IOException("Stream closed");
        return buffer;
    }

    /**
     * 创建一个 <code>BufferedInputStream</code> 并保存其参数，即输入流 <code>in</code> 以供以后使用。创建一个内部缓冲数组并存储在 <code>buf</code> 中。
     *
     * @param   in   底层输入流。
     */
    public BufferedInputStream(InputStream in) {
        this(in, DEFAULT_BUFFER_SIZE);
    }

    /**
     * 使用指定的缓冲区大小创建一个 <code>BufferedInputStream</code>，并保存其参数，即输入流 <code>in</code> 以供以后使用。创建一个长度为 <code>size</code> 的内部缓冲数组并存储在 <code>buf</code> 中。
     *
     * @param   in     底层输入流。
     * @param   size   缓冲区大小。
     * @exception IllegalArgumentException 如果 {@code size <= 0}。
     */
    public BufferedInputStream(InputStream in, int size) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        }
        buf = new byte[size];
    }

    /**
     * 用更多数据填充缓冲区，考虑到处理标记的技巧。假设它是由同步方法调用的。
     * 此方法还假设所有数据都已读取完毕，因此 pos > count。
     */
    private void fill() throws IOException {
        byte[] buffer = getBufIfOpen();
        if (markpos < 0)
            pos = 0;            /* 没有标记：丢弃缓冲区 */
        else if (pos >= buffer.length)  /* 缓冲区中没有空间 */
            if (markpos > 0) {  /* 可以丢弃缓冲区的早期部分 */
                int sz = pos - markpos;
                System.arraycopy(buffer, markpos, buffer, 0, sz);
                pos = sz;
                markpos = 0;
            } else if (buffer.length >= marklimit) {
                markpos = -1;   /* 缓冲区太大，取消标记 */
                pos = 0;        /* 丢弃缓冲区内容 */
            } else if (buffer.length >= MAX_BUFFER_SIZE) {
                throw new OutOfMemoryError("Required array size too large");
            } else {            /* 增大缓冲区 */
                int nsz = (pos <= MAX_BUFFER_SIZE - pos) ?
                        pos * 2 : MAX_BUFFER_SIZE;
                if (nsz > marklimit)
                    nsz = marklimit;
                byte nbuf[] = new byte[nsz];
                System.arraycopy(buffer, 0, nbuf, 0, pos);
                if (!bufUpdater.compareAndSet(this, buffer, nbuf)) {
                    // 如果有异步关闭，不能替换 buf。
                    // 注意：如果 fill() 有朝一日对多个线程开放，这需要改变。
                    // 但目前，CAS 失败的唯一方式是通过关闭。
                    // assert buf == null;
                    throw new IOException("Stream closed");
                }
                buffer = nbuf;
            }
        count = pos;
        int n = getInIfOpen().read(buffer, pos, buffer.length - pos);
        if (n > 0)
            count = n + pos;
    }

    /**
     * 参见 <code>InputStream</code> 的 <code>read</code> 方法的通用合同。
     *
     * @return     下一个字节的数据，或者如果已到达流的末尾则返回 <code>-1</code>。
     * @exception  IOException  如果通过调用其 {@link #close()} 方法关闭了此输入流，或者发生 I/O 错误。
     * @see        java.io.FilterInputStream#in
     */
    public synchronized int read() throws IOException {
        if (pos >= count) {
            fill();
            if (pos >= count)
                return -1;
        }
        return getBufIfOpen()[pos++] & 0xff;
    }

    /**
     * 将字节读入数组的一部分，如果必要，从底层流中读取一次。
     */
    private int read1(byte[] b, int off, int len) throws IOException {
        int avail = count - pos;
        if (avail <= 0) {
            /* 如果请求的长度至少与缓冲区一样大，并且没有标记/重置活动，则不要将字节复制到本地缓冲区。这样缓冲流将无害地级联。 */
            if (len >= getBufIfOpen().length && markpos < 0) {
                return getInIfOpen().read(b, off, len);
            }
            fill();
            avail = count - pos;
            if (avail <= 0) return -1;
        }
        int cnt = (avail < len) ? avail : len;
        System.arraycopy(getBufIfOpen(), pos, b, off, cnt);
        pos += cnt;
        return cnt;
    }

    /**
     * 从这个字节输入流中读取字节到指定的字节数组，从给定的偏移量开始。
     *
     * <p> 此方法实现了 <code>{@link InputStream}</code> 类中相应的
     * <code>{@link InputStream#read(byte[], int, int) read}</code> 方法的通用合同。作为额外的便利，它通过反复调用底层流的 <code>read</code> 方法尝试尽可能多地读取字节。这种迭代的 <code>read</code> 会一直进行，直到以下条件之一变为真： <ul>
     *
     *   <li> 已读取指定的字节数，
     *
     *   <li> 底层流的 <code>read</code> 方法返回 <code>-1</code>，表示文件结束，或者
     *
     *   <li> 底层流的 <code>available</code> 方法返回零，表示进一步的输入请求将阻塞。
     *
     * </ul> 如果底层流的第一个 <code>read</code> 返回 <code>-1</code> 表示文件结束，则此方法返回 <code>-1</code>。否则此方法返回实际读取的字节数。
     *
     * <p> 本类的子类被鼓励但不要求以相同的方式尝试尽可能多地读取字节。
     *
     * @param      b     目标缓冲区。
     * @param      off   开始存储字节的偏移量。
     * @param      len   最大读取字节数。
     * @return     读取的字节数，或者如果已到达流的末尾则返回 <code>-1</code>。
     * @exception  IOException  如果通过调用其 {@link #close()} 方法关闭了此输入流，或者发生 I/O 错误。
     */
    public synchronized int read(byte b[], int off, int len)
        throws IOException
    {
        getBufIfOpen(); // 检查流是否已关闭
        if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int n = 0;
        for (;;) {
            int nread = read1(b, off + n, len - n);
            if (nread <= 0)
                return (n == 0) ? nread : n;
            n += nread;
            if (n >= len)
                return n;
            // 如果未关闭但没有可用字节，则返回
            InputStream input = in;
            if (input != null && input.available() <= 0)
                return n;
        }
    }

    /**
     * 参见 <code>InputStream</code> 的 <code>skip</code> 方法的通用合同。
     *
     * @exception  IOException  如果流不支持 seek，或者通过调用其 {@link #close()} 方法关闭了此输入流，或者发生 I/O 错误。
     */
    public synchronized long skip(long n) throws IOException {
        getBufIfOpen(); // 检查流是否已关闭
        if (n <= 0) {
            return 0;
        }
        long avail = count - pos;

        if (avail <= 0) {
            // 如果没有设置标记位置，则不要保留在缓冲区中
            if (markpos < 0)
                return getInIfOpen().skip(n);

            // 填充缓冲区以保存重置所需的字节
            fill();
            avail = count - pos;
            if (avail <= 0)
                return 0;
        }

        long skipped = (avail < n) ? avail : n;
        pos += skipped;
        return skipped;
    }

    /**
     * 返回从这个输入流中可以读取（或跳过）的字节数的估计值，而不会阻塞下一次调用此输入流的方法。下一次调用可能是同一个线程或另一个线程。单次读取或跳过这么多字节不会阻塞，但可能会读取或跳过更少的字节。
     * <p>
     * 此方法返回缓冲区中剩余可读字节数（<code>count&nbsp;- pos</code>）和调用 {@link java.io.FilterInputStream#in in}.available() 的结果之和。
     *
     * @return     从这个输入流中可以读取（或跳过）的字节数的估计值。
     * @exception  IOException  如果通过调用其 {@link #close()} 方法关闭了此输入流，或者发生 I/O 错误。
     */
    public synchronized int available() throws IOException {
        int n = count - pos;
        int avail = getInIfOpen().available();
        return n > (Integer.MAX_VALUE - avail)
                    ? Integer.MAX_VALUE
                    : n + avail;
    }


                /**
     * 参见 <code>InputStream</code> 的 <code>mark</code>
     * 方法的一般约定。
     *
     * @param   readlimit   在标记位置失效之前可以读取的最大字节数。
     * @see     java.io.BufferedInputStream#reset()
     */
    public synchronized void mark(int readlimit) {
        marklimit = readlimit;
        markpos = pos;
    }

    /**
     * 参见 <code>InputStream</code> 的 <code>reset</code>
     * 方法的一般约定。
     * <p>
     * 如果 <code>markpos</code> 为 <code>-1</code>
     * （未设置标记或标记已失效），则抛出 <code>IOException</code>
     * 。否则，<code>pos</code> 被设置为 <code>markpos</code>。
     *
     * @exception  IOException  如果此流未被标记或标记已失效，或者流已通过调用其 {@link #close()}
     *                  方法关闭，或发生 I/O 错误。
     * @see        java.io.BufferedInputStream#mark(int)
     */
    public synchronized void reset() throws IOException {
        getBufIfOpen(); // 如果已关闭，则引发异常
        if (markpos < 0)
            throw new IOException("重置到无效的标记");
        pos = markpos;
    }

    /**
     * 测试此输入流是否支持 <code>mark</code>
     * 和 <code>reset</code> 方法。<code>BufferedInputStream</code> 的 <code>markSupported</code>
     * 方法返回 <code>true</code>。
     *
     * @return  一个 <code>boolean</code>，指示此流类型是否支持
     *          <code>mark</code> 和 <code>reset</code> 方法。
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.InputStream#reset()
     */
    public boolean markSupported() {
        return true;
    }

    /**
     * 关闭此输入流并释放与流关联的任何系统资源。
     * 一旦流被关闭，进一步的 read()、available()、reset()
     * 或 skip() 调用将抛出 IOException。
     * 关闭已关闭的流没有效果。
     *
     * @exception  IOException  如果发生 I/O 错误。
     */
    public void close() throws IOException {
        byte[] buffer;
        while ( (buffer = buf) != null) {
            if (bufUpdater.compareAndSet(this, buffer, null)) {
                InputStream input = in;
                in = null;
                if (input != null)
                    input.close();
                return;
            }
            // 否则，如果在 fill() 中 CAS 了一个新的 buf，则重试
        }
    }
}
