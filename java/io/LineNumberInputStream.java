
/*
 * Copyright (c) 1995, 2012, Oracle and/or its affiliates. All rights reserved.
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
 * 该类是一个输入流过滤器，提供了跟踪当前行号的附加功能。
 * <p>
 * 一行是以回车字符（{@code '\u005Cr'}）、换行字符（{@code '\u005Cn'}）或回车字符后紧跟换行字符的字节序列。
 * 在所有三种情况下，行终止字符被返回为单个换行字符。
 * <p>
 * 行号从 {@code 0} 开始，当 {@code read} 返回换行字符时，行号递增 {@code 1}。
 *
 * @author     Arthur van Hoff
 * @see        java.io.LineNumberReader
 * @since      JDK1.0
 * @deprecated 该类错误地假设字节可以充分表示字符。自 JDK&nbsp;1.1 起，操作字符流的首选方式是使用新的字符流类，其中包含一个用于计行号的类。
 */
@Deprecated
public
class LineNumberInputStream extends FilterInputStream {
    int pushBack = -1;
    int lineNumber;
    int markLineNumber;
    int markPushBack = -1;

    /**
     * 构造一个从指定输入流读取输入的新行号输入流。
     *
     * @param      in   底层输入流。
     */
    public LineNumberInputStream(InputStream in) {
        super(in);
    }

    /**
     * 从该输入流读取下一个字节的数据。该字节的值作为 {@code int} 返回，范围为 {@code 0} 到 {@code 255}。
     * 如果因为到达流的末尾而没有可用字节，则返回值为 {@code -1}。此方法会阻塞，直到有输入数据可用、
     * 检测到流的末尾或抛出异常。
     * <p>
     * {@code LineNumberInputStream} 的 {@code read} 方法调用底层输入流的 {@code read} 方法。
     * 它检查输入中的回车和换行字符，并根据需要修改当前行号。回车字符或回车后紧跟换行字符都会被转换为单个换行字符。
     *
     * @return     下一个字节的数据，或如果到达此流的末尾则返回 {@code -1}。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterInputStream#in
     * @see        java.io.LineNumberInputStream#getLineNumber()
     */
    @SuppressWarnings("fallthrough")
    public int read() throws IOException {
        int c = pushBack;

        if (c != -1) {
            pushBack = -1;
        } else {
            c = in.read();
        }

        switch (c) {
          case '\r':
            pushBack = in.read();
            if (pushBack == '\n') {
                pushBack = -1;
            }
          case '\n':
            lineNumber++;
            return '\n';
        }
        return c;
    }

    /**
     * 从该输入流读取最多 {@code len} 个字节的数据到字节数组中。此方法会阻塞，直到有输入数据可用。
     * <p>
     * {@code LineNumberInputStream} 的 {@code read} 方法反复调用无参数的 {@code read} 方法以填充字节数组。
     *
     * @param      b     读取数据的缓冲区。
     * @param      off   数据的起始偏移量。
     * @param      len   读取的最大字节数。
     * @return     读取到缓冲区中的总字节数，或如果因为到达此流的末尾而没有更多数据则返回 {@code -1}。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.LineNumberInputStream#read()
     */
    public int read(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                   ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int c = read();
        if (c == -1) {
            return -1;
        }
        b[off] = (byte)c;

        int i = 1;
        try {
            for (; i < len ; i++) {
                c = read();
                if (c == -1) {
                    break;
                }
                if (b != null) {
                    b[off + i] = (byte)c;
                }
            }
        } catch (IOException ee) {
        }
        return i;
    }

    /**
     * 跳过并丢弃此输入流中的 {@code n} 个字节的数据。由于各种原因，{@code skip} 方法可能会跳过更少的字节数，
     * 可能是 {@code 0}。实际跳过的字节数返回。如果 {@code n} 为负数，则不跳过任何字节。
     * <p>
     * {@code LineNumberInputStream} 的 {@code skip} 方法创建一个字节数组，然后反复读取直到读取了 {@code n} 个字节或到达流的末尾。
     *
     * @param      n   要跳过的字节数。
     * @return     实际跳过的字节数。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterInputStream#in
     */
    public long skip(long n) throws IOException {
        int chunk = 2048;
        long remaining = n;
        byte data[];
        int nr;

        if (n <= 0) {
            return 0;
        }

        data = new byte[chunk];
        while (remaining > 0) {
            nr = read(data, 0, (int) Math.min(chunk, remaining));
            if (nr < 0) {
                break;
            }
            remaining -= nr;
        }


                    return n - remaining;
    }

    /**
     * 设置行号为指定的参数。
     *
     * @param      lineNumber   新的行号。
     * @see #getLineNumber
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * 返回当前行号。
     *
     * @return     当前行号。
     * @see #setLineNumber
     */
    public int getLineNumber() {
        return lineNumber;
    }


    /**
     * 返回从该输入流中读取而不会阻塞的字节数。
     * <p>
     * 注意，如果底层输入流能够提供 <i>k</i> 个输入字符而不会阻塞，那么
     * {@code LineNumberInputStream} 只能保证提供 <i>k</i>/2 个字符而不会阻塞，因为
     * 从底层输入流中读取的 <i>k</i> 个字符可能由 <i>k</i>/2 对 {@code '\u005Cr'} 和
     * {@code '\u005Cn'} 组成，这些字符会被转换为仅 <i>k</i>/2 个 {@code '\u005Cn'} 字符。
     *
     * @return     从该输入流中读取而不会阻塞的字节数。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterInputStream#in
     */
    public int available() throws IOException {
        return (pushBack == -1) ? super.available()/2 : super.available()/2 + 1;
    }

    /**
     * 在此输入流中标记当前位置。随后调用 {@code reset} 方法会将此流重新定位到
     * 最后一次调用 {@code mark} 方法时的位置，以便后续读取重新读取相同的字节。
     * <p>
     * {@code LineNumberInputStream} 的 {@code mark} 方法会记住当前行号
     * 并调用底层输入流的 {@code mark} 方法。
     *
     * @param   readlimit   在标记位置失效之前可以读取的最大字节数。
     * @see     java.io.FilterInputStream#in
     * @see     java.io.LineNumberInputStream#reset()
     */
    public void mark(int readlimit) {
        markLineNumber = lineNumber;
        markPushBack   = pushBack;
        in.mark(readlimit);
    }

    /**
     * 将此流重新定位到上次调用 {@code mark} 方法时的位置。
     * <p>
     * {@code LineNumberInputStream} 的 {@code reset} 方法会将行号重置为
     * 调用 {@code mark} 方法时的行号，然后调用底层输入流的 {@code reset} 方法。
     * <p>
     * 流标记旨在用于需要向前读取一点以查看流中的内容的情况。通常这最容易通过调用某个通用解析器来完成。
     * 如果流是解析器处理的类型，它会愉快地继续处理。如果流不是那种类型，解析器在失败时应抛出异常，
     * 如果在 readlimit 字节内发生这种情况，外部代码可以重置流并尝试另一个解析器。
     *
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterInputStream#in
     * @see        java.io.LineNumberInputStream#mark(int)
     */
    public void reset() throws IOException {
        lineNumber = markLineNumber;
        pushBack   = markPushBack;
        in.reset();
    }
}
