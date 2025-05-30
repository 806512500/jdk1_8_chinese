
/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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


import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 从字符输入流中读取文本，通过缓冲字符以提供高效读取字符、数组和行的方法。
 *
 * <p> 可以指定缓冲区大小，也可以使用默认大小。默认大小对于大多数用途来说已经足够大。
 *
 * <p> 通常，对 Reader 的每次读取请求都会导致对底层字符或字节流的相应读取请求。因此，建议在任何读取操作可能昂贵的 Reader（如 FileReader 和 InputStreamReader）周围包装一个 BufferedReader。例如，
 *
 * <pre>
 * BufferedReader in
 *   = new BufferedReader(new FileReader("foo.in"));
 * </pre>
 *
 * 将缓冲来自指定文件的输入。如果不使用缓冲，每次调用 read() 或 readLine() 都可能导致从文件中读取字节，将其转换为字符，然后返回，这可能非常低效。
 *
 * <p> 使用 DataInputStream 进行文本输入的程序可以通过将每个 DataInputStream 替换为适当的 BufferedReader 来实现本地化。
 *
 * @see FileReader
 * @see InputStreamReader
 * @see java.nio.file.Files#newBufferedReader
 *
 * @author      Mark Reinhold
 * @since       JDK1.1
 */

public class BufferedReader extends Reader {

    private Reader in;

    private char cb[];
    private int nChars, nextChar;

    private static final int INVALIDATED = -2;
    private static final int UNMARKED = -1;
    private int markedChar = UNMARKED;
    private int readAheadLimit = 0; /* 仅在 markedChar > 0 时有效 */

    /** 如果下一个字符是换行符，则跳过它 */
    private boolean skipLF = false;

    /** 设置标记时的 skipLF 标志 */
    private boolean markedSkipLF = false;

    private static int defaultCharBufferSize = 8192;
    private static int defaultExpectedLineLength = 80;

    /**
     * 创建一个使用指定大小输入缓冲区的缓冲字符输入流。
     *
     * @param  in   一个 Reader
     * @param  sz   输入缓冲区大小
     *
     * @exception  IllegalArgumentException  如果 {@code sz <= 0}
     */
    public BufferedReader(Reader in, int sz) {
        super(in);
        if (sz <= 0)
            throw new IllegalArgumentException("缓冲区大小 <= 0");
        this.in = in;
        cb = new char[sz];
        nextChar = nChars = 0;
    }

    /**
     * 创建一个使用默认大小输入缓冲区的缓冲字符输入流。
     *
     * @param  in   一个 Reader
     */
    public BufferedReader(Reader in) {
        this(in, defaultCharBufferSize);
    }

    /** 检查流是否已关闭 */
    private void ensureOpen() throws IOException {
        if (in == null)
            throw new IOException("流已关闭");
    }

    /**
     * 填充输入缓冲区，如果标记有效则考虑标记。
     */
    private void fill() throws IOException {
        int dst;
        if (markedChar <= UNMARKED) {
            /* 无标记 */
            dst = 0;
        } else {
            /* 已标记 */
            int delta = nextChar - markedChar;
            if (delta >= readAheadLimit) {
                /* 超过读取预读限制：使标记无效 */
                markedChar = INVALIDATED;
                readAheadLimit = 0;
                dst = 0;
            } else {
                if (readAheadLimit <= cb.length) {
                    /* 在当前缓冲区中重新排列 */
                    System.arraycopy(cb, markedChar, cb, 0, delta);
                    markedChar = 0;
                    dst = delta;
                } else {
                    /* 重新分配缓冲区以容纳读取预读限制 */
                    char ncb[] = new char[readAheadLimit];
                    System.arraycopy(cb, markedChar, ncb, 0, delta);
                    cb = ncb;
                    markedChar = 0;
                    dst = delta;
                }
                nextChar = nChars = delta;
            }
        }

        int n;
        do {
            n = in.read(cb, dst, cb.length - dst);
        } while (n == 0);
        if (n > 0) {
            nChars = dst + n;
            nextChar = dst;
        }
    }

    /**
     * 读取单个字符。
     *
     * @return 读取的字符，作为 0 到 65535 (<tt>0x00-0xffff</tt>) 范围内的整数，或 -1 表示已到达流的末尾
     * @exception  IOException  如果发生 I/O 错误
     */
    public int read() throws IOException {
        synchronized (lock) {
            ensureOpen();
            for (;;) {
                if (nextChar >= nChars) {
                    fill();
                    if (nextChar >= nChars)
                        return -1;
                }
                if (skipLF) {
                    skipLF = false;
                    if (cb[nextChar] == '\n') {
                        nextChar++;
                        continue;
                    }
                }
                return cb[nextChar++];
            }
        }
    }

    /**
     * 读取字符到数组的一部分，必要时从底层流中读取。
     */
    private int read1(char[] cbuf, int off, int len) throws IOException {
        if (nextChar >= nChars) {
            /* 如果请求的长度至少与缓冲区一样大，并且没有标记/重置活动，并且不跳过换行符，则不必将字符复制到本地缓冲区。这样缓冲流将无害地级联。 */
            if (len >= cb.length && markedChar <= UNMARKED && !skipLF) {
                return in.read(cbuf, off, len);
            }
            fill();
        }
        if (nextChar >= nChars) return -1;
        if (skipLF) {
            skipLF = false;
            if (cb[nextChar] == '\n') {
                nextChar++;
                if (nextChar >= nChars)
                    fill();
                if (nextChar >= nChars)
                    return -1;
            }
        }
        int n = Math.min(len, nChars - nextChar);
        System.arraycopy(cb, nextChar, cbuf, off, n);
        nextChar += n;
        return n;
    }

    /**
     * 读取字符到数组的一部分。
     *
     * <p> 此方法实现了 {@link Reader} 类中相应 <code>{@link Reader#read(char[], int, int) read}</code> 方法的一般契约。作为额外的便利，它通过反复调用底层流的 <code>read</code> 方法尝试尽可能多地读取字符。这种迭代的 <code>read</code> 会继续，直到以下条件之一变为真： <ul>
     *
     *   <li> 已读取指定数量的字符，
     *
     *   <li> 底层流的 <code>read</code> 方法返回 <code>-1</code>，表示文件结束，或
     *
     *   <li> 底层流的 <code>ready</code> 方法返回 <code>false</code>，表示进一步的输入请求将阻塞。
     *
     * </ul> 如果底层流的第一个 <code>read</code> 返回 <code>-1</code> 表示文件结束，则此方法返回 <code>-1</code>。否则此方法返回实际读取的字符数。
     *
     * <p> 本类的子类被鼓励但不要求以相同的方式尝试尽可能多地读取字符。
     *
     * <p> 通常此方法从本流的字符缓冲区中读取字符，必要时从底层流中填充。但是，如果缓冲区为空，标记无效，并且请求的长度至少与缓冲区一样大，则此方法将直接从底层流中读取字符到给定数组。因此，冗余的 <code>BufferedReader</code> 不会不必要的复制数据。
     *
     * @param      cbuf  目标缓冲区
     * @param      off   存储字符的偏移量
     * @param      len   最大读取字符数
     *
     * @return     实际读取的字符数，或 -1 表示已到达流的末尾
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public int read(char cbuf[], int off, int len) throws IOException {
        synchronized (lock) {
            ensureOpen();
            if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                ((off + len) > cbuf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }

            int n = read1(cbuf, off, len);
            if (n <= 0) return n;
            while ((n < len) && in.ready()) {
                int n1 = read1(cbuf, off + n, len - n);
                if (n1 <= 0) break;
                n += n1;
            }
            return n;
        }
    }

    /**
     * 读取一行文本。行终止符可以是换行符 ('\n')、回车符 ('\r') 或回车符后紧跟换行符。
     *
     * @param      ignoreLF  如果为 true，则下一个 '\n' 将被跳过
     *
     * @return     包含行内容的字符串，不包括任何行终止符，或如果已到达流的末尾则返回 null
     *
     * @see        java.io.LineNumberReader#readLine()
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    String readLine(boolean ignoreLF) throws IOException {
        StringBuilder s = null;
        int startChar;

        synchronized (lock) {
            ensureOpen();
            boolean omitLF = ignoreLF || skipLF;

        bufferLoop:
            for (;;) {

                if (nextChar >= nChars)
                    fill();
                if (nextChar >= nChars) { /* 文件结束 */
                    if (s != null && s.length() > 0)
                        return s.toString();
                    else
                        return null;
                }
                boolean eol = false;
                char c = 0;
                int i;

                /* 必要时跳过剩余的 '\n' */
                if (omitLF && (cb[nextChar] == '\n'))
                    nextChar++;
                skipLF = false;
                omitLF = false;

            charLoop:
                for (i = nextChar; i < nChars; i++) {
                    c = cb[i];
                    if ((c == '\n') || (c == '\r')) {
                        eol = true;
                        break charLoop;
                    }
                }

                startChar = nextChar;
                nextChar = i;

                if (eol) {
                    String str;
                    if (s == null) {
                        str = new String(cb, startChar, i - startChar);
                    } else {
                        s.append(cb, startChar, i - startChar);
                        str = s.toString();
                    }
                    nextChar++;
                    if (c == '\r') {
                        skipLF = true;
                    }
                    return str;
                }

                if (s == null)
                    s = new StringBuilder(defaultExpectedLineLength);
                s.append(cb, startChar, i - startChar);
            }
        }
    }

    /**
     * 读取一行文本。行终止符可以是换行符 ('\n')、回车符 ('\r') 或回车符后紧跟换行符。
     *
     * @return     包含行内容的字符串，不包括任何行终止符，或如果已到达流的末尾则返回 null
     *
     * @exception  IOException  如果发生 I/O 错误
     *
     * @see java.nio.file.Files#readAllLines
     */
    public String readLine() throws IOException {
        return readLine(false);
    }

    /**
     * 跳过字符。
     *
     * @param  n  要跳过的字符数
     *
     * @return    实际跳过的字符数
     *
     * @exception  IllegalArgumentException  如果 <code>n</code> 为负数。
     * @exception  IOException  如果发生 I/O 错误
     */
    public long skip(long n) throws IOException {
        if (n < 0L) {
            throw new IllegalArgumentException("跳过的值为负数");
        }
        synchronized (lock) {
            ensureOpen();
            long r = n;
            while (r > 0) {
                if (nextChar >= nChars)
                    fill();
                if (nextChar >= nChars) /* 文件结束 */
                    break;
                if (skipLF) {
                    skipLF = false;
                    if (cb[nextChar] == '\n') {
                        nextChar++;
                    }
                }
                long d = nChars - nextChar;
                if (r <= d) {
                    nextChar += r;
                    r = 0;
                    break;
                }
                else {
                    r -= d;
                    nextChar = nChars;
                }
            }
            return n - r;
        }
    }

    /**
     * 告诉此流是否准备好读取。缓冲字符流在缓冲区不为空或底层字符流准备好时准备好。
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public boolean ready() throws IOException {
        synchronized (lock) {
            ensureOpen();


        }

    }
```

```java
                        /*
             * 如果需要跳过换行符，并且下一个要读取的字符是换行符，则立即跳过它。
             */
            if (skipLF) {
                /* 注意，in.ready() 仅当流的下一次读取不会阻塞时返回 true。 */
                if (nextChar >= nChars && in.ready()) {
                    fill();
                }
                if (nextChar < nChars) {
                    if (cb[nextChar] == '\n')
                        nextChar++;
                    skipLF = false;
                }
            }
            return (nextChar < nChars) || in.ready();
        }
    }

    /**
     * 告诉此流是否支持 mark() 操作，它确实支持。
     */
    public boolean markSupported() {
        return true;
    }

    /**
     * 标记流中的当前位置。后续调用 reset() 将尝试将流重新定位到此点。
     *
     * @param readAheadLimit   读取字符数量的限制，同时仍保留标记。尝试在读取字符达到此限制或超过此限制后重置流可能会失败。
     *                         较大的限制值将导致分配一个不小于限制值的新缓冲区。因此，应谨慎使用较大值。
     *
     * @exception  IllegalArgumentException  如果 {@code readAheadLimit < 0}
     * @exception  IOException  如果发生 I/O 错误
     */
    public void mark(int readAheadLimit) throws IOException {
        if (readAheadLimit < 0) {
            throw new IllegalArgumentException("Read-ahead limit < 0");
        }
        synchronized (lock) {
            ensureOpen();
            this.readAheadLimit = readAheadLimit;
            markedChar = nextChar;
            markedSkipLF = skipLF;
        }
    }

    /**
     * 将流重置到最近的标记。
     *
     * @exception  IOException  如果流从未被标记，或者标记已失效
     */
    public void reset() throws IOException {
        synchronized (lock) {
            ensureOpen();
            if (markedChar < 0)
                throw new IOException((markedChar == INVALIDATED)
                                      ? "Mark invalid"
                                      : "Stream not marked");
            nextChar = markedChar;
            skipLF = markedSkipLF;
        }
    }

    public void close() throws IOException {
        synchronized (lock) {
            if (in == null)
                return;
            try {
                in.close();
            } finally {
                in = null;
                cb = null;
            }
        }
    }

    /**
     * 返回一个 {@code Stream}，其元素是从此 {@code BufferedReader} 读取的行。该 {@link Stream} 是惰性填充的，
     * 即，仅在 <a href="../util/stream/package-summary.html#StreamOps">终端流操作</a> 期间发生读取。
     *
     * <p> 在执行终端流操作期间，不得操作读取器。否则，终端流操作的结果是未定义的。
     *
     * <p> 在执行终端流操作之后，没有保证读取器将处于从其读取下一个字符或行的特定位置。
     *
     * <p> 如果在访问底层 {@code BufferedReader} 时抛出 {@link IOException}，则将其包装在 {@link
     * UncheckedIOException} 中，并从导致读取发生的 {@code Stream} 方法中抛出。如果在 BufferedReader 关闭后调用此方法，
     * 该方法将返回一个 Stream。在此 Stream 上执行需要在 BufferedReader 关闭后读取的任何操作，将导致抛出 UncheckedIOException。
     *
     * @return 一个 {@code Stream<String>}，提供由此 {@code BufferedReader} 描述的文本行
     *
     * @since 1.8
     */
    public Stream<String> lines() {
        Iterator<String> iter = new Iterator<String>() {
            String nextLine = null;

            @Override
            public boolean hasNext() {
                if (nextLine != null) {
                    return true;
                } else {
                    try {
                        nextLine = readLine();
                        return (nextLine != null);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }

            @Override
            public String next() {
                if (nextLine != null || hasNext()) {
                    String line = nextLine;
                    nextLine = null;
                    return line;
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                iter, Spliterator.ORDERED | Spliterator.NONNULL), false);
    }
}
