
/*
 * Copyright (c) 1996, 2005, Oracle and/or its affiliates. All rights reserved.
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

package java.io;

/**
 * 该类实现了一个可以作为字符输入流的字符缓冲区。
 *
 * @author      Herb Jellinek
 * @since       JDK1.1
 */
public class CharArrayReader extends Reader {
    /** 字符缓冲区。 */
    protected char buf[];

    /** 当前缓冲区位置。 */
    protected int pos;

    /** 缓冲区中的标记位置。 */
    protected int markedPos = 0;

    /**
     * 缓冲区的结束索引。此索引及之后的数据无效。
     */
    protected int count;

    /**
     * 从指定的字符数组创建一个 CharArrayReader。
     * @param buf       输入缓冲区（不复制）
     */
    public CharArrayReader(char buf[]) {
        this.buf = buf;
        this.pos = 0;
        this.count = buf.length;
    }

    /**
     * 从指定的字符数组创建一个 CharArrayReader。
     *
     * <p> 结果的读取器将从给定的 <tt>offset</tt> 开始读取。从该读取器中可以读取的 <tt>char</tt> 值的总数将是 <tt>length</tt> 或
     * <tt>buf.length-offset</tt> 中较小的一个。
     *
     * @throws IllegalArgumentException
     *         如果 <tt>offset</tt> 为负数或大于 <tt>buf.length</tt>，或者 <tt>length</tt> 为负数，或者
     *         这两个值的和为负数。
     *
     * @param buf       输入缓冲区（不复制）
     * @param offset    要读取的第一个字符的偏移量
     * @param length    要读取的字符数
     */
    public CharArrayReader(char buf[], int offset, int length) {
        if ((offset < 0) || (offset > buf.length) || (length < 0) ||
            ((offset + length) < 0)) {
            throw new IllegalArgumentException();
        }
        this.buf = buf;
        this.pos = offset;
        this.count = Math.min(offset + length, buf.length);
        this.markedPos = offset;
    }

    /** 检查流是否未关闭 */
    private void ensureOpen() throws IOException {
        if (buf == null)
            throw new IOException("Stream closed");
    }

    /**
     * 读取单个字符。
     *
     * @exception   IOException  如果发生 I/O 错误
     */
    public int read() throws IOException {
        synchronized (lock) {
            ensureOpen();
            if (pos >= count)
                return -1;
            else
                return buf[pos++];
        }
    }

    /**
     * 将字符读入数组的一部分。
     * @param b  目标缓冲区
     * @param off  开始存储字符的偏移量
     * @param len   最大读取的字符数
     * @return  实际读取的字符数，或者如果已到达流的末尾则返回 -1
     *
     * @exception   IOException  如果发生 I/O 错误
     */
    public int read(char b[], int off, int len) throws IOException {
        synchronized (lock) {
            ensureOpen();
            if ((off < 0) || (off > b.length) || (len < 0) ||
                ((off + len) > b.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }

            if (pos >= count) {
                return -1;
            }

            int avail = count - pos;
            if (len > avail) {
                len = avail;
            }
            if (len <= 0) {
                return 0;
            }
            System.arraycopy(buf, pos, b, off, len);
            pos += len;
            return len;
        }
    }

    /**
     * 跳过字符。返回实际跳过的字符数。
     *
     * <p><code>n</code> 参数可以为负，即使在这种情况下 <code>skip</code> 方法的 {@link Reader} 超类会抛出异常。如果 <code>n</code> 为负，则
     * 此方法不执行任何操作并返回 <code>0</code>。
     *
     * @param n 要跳过的字符数
     * @return       实际跳过的字符数
     * @exception  IOException 如果流已关闭，或者发生 I/O 错误
     */
    public long skip(long n) throws IOException {
        synchronized (lock) {
            ensureOpen();

            long avail = count - pos;
            if (n > avail) {
                n = avail;
            }
            if (n < 0) {
                return 0;
            }
            pos += n;
            return n;
        }
    }

    /**
     * 告诉此流是否准备好读取。字符数组读取器总是准备好读取。
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public boolean ready() throws IOException {
        synchronized (lock) {
            ensureOpen();
            return (count - pos) > 0;
        }
    }

    /**
     * 告诉此流是否支持 mark() 操作，它确实支持。
     */
    public boolean markSupported() {
        return true;
    }

    /**
     * 标记流中的当前位置。后续调用 reset() 将重新定位流到此点。
     *
     * @param  readAheadLimit  读取时仍保留标记的字符数限制。由于流的输入来自字符数组，
     *                         实际上没有限制；因此忽略此参数。
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public void mark(int readAheadLimit) throws IOException {
        synchronized (lock) {
            ensureOpen();
            markedPos = pos;
        }
    }

    /**
     * 重置流到最近的标记，或者如果从未标记过则重置到开头。
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public void reset() throws IOException {
        synchronized (lock) {
            ensureOpen();
            pos = markedPos;
        }
    }

                /**
     * 关闭流并释放与之关联的任何系统资源。
     * 一旦流被关闭，进一步的 read()、ready()、mark()、reset() 或 skip() 调用将抛出 IOException。
     * 关闭一个已经关闭的流没有任何效果。
     */
    public void close() {
        buf = null;
    }
}
