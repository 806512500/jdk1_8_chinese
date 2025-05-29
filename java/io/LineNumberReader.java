/*
 * 版权所有 (c) 1996, 2011, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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
 * 一个带缓冲的字符输入流，用于跟踪行号。此类定义了方法 {@link #setLineNumber(int)} 和 {@link
 * #getLineNumber()} 用于分别设置和获取当前行号。
 *
 * <p> 默认情况下，行号从 0 开始。每当读取数据时遇到 <a href="#lt">行终止符</a>，行号会递增，也可以通过调用
 * <tt>setLineNumber(int)</tt> 来更改行号。然而，需要注意的是，<tt>setLineNumber(int)</tt> 并不会实际改变流中的当前位置；它只会改变 <tt>getLineNumber()</tt>
 * 返回的值。
 *
 * <p> 行被认为是由以下任何一个终止：<a name="lt">换行符 ('\n')</a>，回车符 ('\r')，或者回车符紧接着换行符。
 *
 * @author      Mark Reinhold
 * @since       JDK1.1
 */

public class LineNumberReader extends BufferedReader {

    /** 当前行号 */
    private int lineNumber = 0;

    /** 标记的行号，如果有的话 */
    private int markedLineNumber; // 默认为 0

    /** 如果下一个字符是换行符，则跳过它 */
    private boolean skipLF;

    /** 设置标记时的 skipLF 标志 */
    private boolean markedSkipLF;

    /**
     * 创建一个新的行号读取器，使用默认的输入缓冲区大小。
     *
     * @param  in
     *         提供底层流的 Reader 对象
     */
    public LineNumberReader(Reader in) {
        super(in);
    }

    /**
     * 创建一个新的行号读取器，将字符读入给定大小的缓冲区。
     *
     * @param  in
     *         提供底层流的 Reader 对象
     *
     * @param  sz
     *         指定缓冲区大小的 int
     */
    public LineNumberReader(Reader in, int sz) {
        super(in, sz);
    }

    /**
     * 设置当前行号。
     *
     * @param  lineNumber
     *         指定行号的 int
     *
     * @see #getLineNumber
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * 获取当前行号。
     *
     * @return  当前行号
     *
     * @see #setLineNumber
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * 读取单个字符。 <a href="#lt">行终止符</a> 被压缩成单个换行符 ('\n')。每当读取到行终止符时，当前行号会递增。
     *
     * @return  读取的字符，或者如果已到达流的末尾则返回 -1
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    @SuppressWarnings("fallthrough")
    public int read() throws IOException {
        synchronized (lock) {
            int c = super.read();
            if (skipLF) {
                if (c == '\n')
                    c = super.read();
                skipLF = false;
            }
            switch (c) {
            case '\r':
                skipLF = true;
            case '\n':          /* Fall through */
                lineNumber++;
                return '\n';
            }
            return c;
        }
    }

    /**
     * 读取字符到数组的一部分。每当读取到 <a href="#lt">行终止符</a> 时，当前行号会递增。
     *
     * @param  cbuf
     *         目标缓冲区
     *
     * @param  off
     *         开始存储字符的偏移量
     *
     * @param  len
     *         最大读取字符数
     *
     * @return  读取的字节数，或者如果已到达流的末尾则返回 -1
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    @SuppressWarnings("fallthrough")
    public int read(char cbuf[], int off, int len) throws IOException {
        synchronized (lock) {
            int n = super.read(cbuf, off, len);

            for (int i = off; i < off + n; i++) {
                int c = cbuf[i];
                if (skipLF) {
                    skipLF = false;
                    if (c == '\n')
                        continue;
                }
                switch (c) {
                case '\r':
                    skipLF = true;
                case '\n':      /* Fall through */
                    lineNumber++;
                    break;
                }
            }

            return n;
        }
    }

    /**
     * 读取一行文本。每当读取到 <a href="#lt">行终止符</a> 时，当前行号会递增。
     *
     * @return  包含行内容的字符串，不包括 <a href="#lt">行终止符</a>，或者如果已到达流的末尾则返回 <tt>null</tt>
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public String readLine() throws IOException {
        synchronized (lock) {
            String l = super.readLine(skipLF);
            skipLF = false;
            if (l != null)
                lineNumber++;
            return l;
        }
    }

    /** 最大跳过缓冲区大小 */
    private static final int maxSkipBufferSize = 8192;

    /** 跳过缓冲区，未分配前为 null */
    private char skipBuffer[] = null;

    /**
     * 跳过字符。
     *
     * @param  n
     *         要跳过的字符数
     *
     * @return  实际跳过的字符数
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @throws  IllegalArgumentException
     *          如果 <tt>n</tt> 为负数
     */
    public long skip(long n) throws IOException {
        if (n < 0)
            throw new IllegalArgumentException("skip() value is negative");
        int nn = (int) Math.min(n, maxSkipBufferSize);
        synchronized (lock) {
            if ((skipBuffer == null) || (skipBuffer.length < nn))
                skipBuffer = new char[nn];
            long r = n;
            while (r > 0) {
                int nc = read(skipBuffer, 0, (int) Math.min(r, nn));
                if (nc == -1)
                    break;
                r -= nc;
            }
            return n - r;
        }
    }

}

                /**
     * 标记流中的当前位置。后续对 reset() 的调用将尝试将流重新定位到此位置，并适当重置行号。
     *
     * @param  readAheadLimit
     *         仍可保留标记的情况下可读取的字符数限制。读取这么多字符后，
     *         尝试重置流可能会失败。
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public void mark(int readAheadLimit) throws IOException {
        synchronized (lock) {
            super.mark(readAheadLimit);
            markedLineNumber = lineNumber;
            markedSkipLF     = skipLF;
        }
    }

    /**
     * 将流重置到最近的标记位置。
     *
     * @throws  IOException
     *          如果流未被标记，或者标记已失效
     */
    public void reset() throws IOException {
        synchronized (lock) {
            super.reset();
            lineNumber = markedLineNumber;
            skipLF     = markedSkipLF;
        }
    }

}
