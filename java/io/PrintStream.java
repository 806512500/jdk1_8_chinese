
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

import java.util.Formatter;
import java.util.Locale;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

/**
 * <code>PrintStream</code> 类为另一个输出流添加了功能，即能够方便地打印各种数据值的表示形式。还提供了其他两个功能。与其他输出流不同，<code>PrintStream</code> 从不抛出 <code>IOException</code>；而是将异常情况设置为一个内部标志，可以通过 <code>checkError</code> 方法进行测试。可以选择创建一个自动刷新的 <code>PrintStream</code>；这意味着在写入字节数组、调用 <code>println</code> 方法之一或写入换行符（<code>'\n'</code>）时，会自动调用 <code>flush</code> 方法。
 *
 * <p> 所有由 <code>PrintStream</code> 打印的字符都使用平台的默认字符编码转换为字节。在需要写入字符而不是字节的情况下，应使用 <code>{@link PrintWriter}</code> 类。
 *
 * @author     Frank Yellin
 * @author     Mark Reinhold
 * @since      JDK1.0
 */

public class PrintStream extends FilterOutputStream
    implements Appendable, Closeable
{

    private final boolean autoFlush;
    private boolean trouble = false;
    private Formatter formatter;

    /**
     * 跟踪文本和字符输出流，以便在不刷新整个流的情况下刷新它们的缓冲区。
     */
    private BufferedWriter textOut;
    private OutputStreamWriter charOut;

    /**
     * 显式声明 requireNonNull 以避免对 java.util.Objects.requireNonNull 的额外依赖。PrintStream 在系统初始化早期加载。
     */
    private static <T> T requireNonNull(T obj, String message) {
        if (obj == null)
            throw new NullPointerException(message);
        return obj;
    }

    /**
     * 返回给定字符集名称的字符集对象。
     * @throws NullPointerException          如果 csn 为 null
     * @throws UnsupportedEncodingException  如果字符集不受支持
     */
    private static Charset toCharset(String csn)
        throws UnsupportedEncodingException
    {
        requireNonNull(csn, "charsetName");
        try {
            return Charset.forName(csn);
        } catch (IllegalCharsetNameException|UnsupportedCharsetException unused) {
            // 应该抛出 UnsupportedEncodingException
            throw new UnsupportedEncodingException(csn);
        }
    }

    /* 私有构造函数 */
    private PrintStream(boolean autoFlush, OutputStream out) {
        super(out);
        this.autoFlush = autoFlush;
        this.charOut = new OutputStreamWriter(this);
        this.textOut = new BufferedWriter(charOut);
    }

    private PrintStream(boolean autoFlush, OutputStream out, Charset charset) {
        super(out);
        this.autoFlush = autoFlush;
        this.charOut = new OutputStreamWriter(this, charset);
        this.textOut = new BufferedWriter(charOut);
    }

    /* 私有构造函数的变体，以便在评估 OutputStream 参数之前验证给定的字符集名称。用于创建 FileOutputStream 的构造函数，同时接受字符集名称。 */
    private PrintStream(boolean autoFlush, Charset charset, OutputStream out)
        throws UnsupportedEncodingException
    {
        this(autoFlush, out, charset);
    }

    /**
     * 创建一个新的打印流。此流不会自动刷新。
     *
     * @param  out        将打印值和对象的输出流
     *
     * @see java.io.PrintWriter#PrintWriter(java.io.OutputStream)
     */
    public PrintStream(OutputStream out) {
        this(out, false);
    }

    /**
     * 创建一个新的打印流。
     *
     * @param  out        将打印值和对象的输出流
     * @param  autoFlush  布尔值；如果为 true，则在写入字节数组、调用 <code>println</code> 方法之一或写入换行符（<code>'\n'</code>）时，将刷新输出缓冲区
     *
     * @see java.io.PrintWriter#PrintWriter(java.io.OutputStream, boolean)
     */
    public PrintStream(OutputStream out, boolean autoFlush) {
        this(autoFlush, requireNonNull(out, "Null output stream"));
    }

    /**
     * 创建一个新的打印流。
     *
     * @param  out        将打印值和对象的输出流
     * @param  autoFlush  布尔值；如果为 true，则在写入字节数组、调用 <code>println</code> 方法之一或写入换行符（<code>'\n'</code>）时，将刷新输出缓冲区
     * @param  encoding   支持的 <a href="../lang/package-summary.html#charenc">字符编码</a> 的名称
     *
     * @throws  UnsupportedEncodingException
     *          如果命名的编码不受支持
     *
     * @since  1.4
     */
    public PrintStream(OutputStream out, boolean autoFlush, String encoding)
        throws UnsupportedEncodingException
    {
        this(autoFlush,
             requireNonNull(out, "Null output stream"),
             toCharset(encoding));
    }

    /**
     * 使用指定的文件名创建一个新的打印流，不自动刷新行。此便利构造函数创建必要的中间 {@link java.io.OutputStreamWriter OutputStreamWriter}，该对象将使用此 Java 虚拟机实例的 {@linkplain java.nio.charset.Charset#defaultCharset() 默认字符集} 编码字符。
     *
     * @param  fileName
     *         用作此打印流目标的文件的名称。如果文件存在，则将其截断为零大小；否则，将创建一个新文件。输出将写入文件并进行缓冲。
     *
     * @throws  FileNotFoundException
     *          如果给定的文件对象不表示一个现有的、可写的普通文件，并且无法创建一个同名的新普通文件，或者在打开或创建文件时发生其他错误
     *
     * @throws  SecurityException
     *          如果存在安全管理器，并且 {@link SecurityManager#checkWrite checkWrite(fileName)} 拒绝对文件的写访问
     *
     * @since  1.5
     */
    public PrintStream(String fileName) throws FileNotFoundException {
        this(false, new FileOutputStream(fileName));
    }

    /**
     * 使用指定的文件名和字符集创建一个新的打印流，不自动刷新行。此便利构造函数创建必要的中间 {@link java.io.OutputStreamWriter OutputStreamWriter}，该对象将使用提供的字符集编码字符。
     *
     * @param  fileName
     *         用作此打印流目标的文件的名称。如果文件存在，则将其截断为零大小；否则，将创建一个新文件。输出将写入文件并进行缓冲。
     *
     * @param  csn
     *         支持的 {@linkplain java.nio.charset.Charset 字符集} 的名称
     *
     * @throws  FileNotFoundException
     *          如果给定的文件对象不表示一个现有的、可写的普通文件，并且无法创建一个同名的新普通文件，或者在打开或创建文件时发生其他错误
     *
     * @throws  SecurityException
     *          如果存在安全管理器，并且 {@link SecurityManager#checkWrite checkWrite(fileName)} 拒绝对文件的写访问
     *
     * @throws  UnsupportedEncodingException
     *          如果命名的字符集不受支持
     *
     * @since  1.5
     */
    public PrintStream(String fileName, String csn)
        throws FileNotFoundException, UnsupportedEncodingException
    {
        // 确保在打开文件之前检查字符集
        this(false, toCharset(csn), new FileOutputStream(fileName));
    }

    /**
     * 使用指定的文件创建一个新的打印流，不自动刷新行。此便利构造函数创建必要的中间 {@link java.io.OutputStreamWriter OutputStreamWriter}，该对象将使用此 Java 虚拟机实例的 {@linkplain java.nio.charset.Charset#defaultCharset() 默认字符集} 编码字符。
     *
     * @param  file
     *         用作此打印流目标的文件。如果文件存在，则将其截断为零大小；否则，将创建一个新文件。输出将写入文件并进行缓冲。
     *
     * @throws  FileNotFoundException
     *          如果给定的文件对象不表示一个现有的、可写的普通文件，并且无法创建一个同名的新普通文件，或者在打开或创建文件时发生其他错误
     *
     * @throws  SecurityException
     *          如果存在安全管理器，并且 {@link SecurityManager#checkWrite checkWrite(file.getPath())} 拒绝对文件的写访问
     *
     * @since  1.5
     */
    public PrintStream(File file) throws FileNotFoundException {
        this(false, new FileOutputStream(file));
    }

    /**
     * 使用指定的文件和字符集创建一个新的打印流，不自动刷新行。此便利构造函数创建必要的中间 {@link java.io.OutputStreamWriter OutputStreamWriter}，该对象将使用提供的字符集编码字符。
     *
     * @param  file
     *         用作此打印流目标的文件。如果文件存在，则将其截断为零大小；否则，将创建一个新文件。输出将写入文件并进行缓冲。
     *
     * @param  csn
     *         支持的 {@linkplain java.nio.charset.Charset 字符集} 的名称
     *
     * @throws  FileNotFoundException
     *          如果给定的文件对象不表示一个现有的、可写的普通文件，并且无法创建一个同名的新普通文件，或者在打开或创建文件时发生其他错误
     *
     * @throws  SecurityException
     *          如果存在安全管理器，并且 {@link SecurityManager#checkWrite checkWrite(file.getPath())} 拒绝对文件的写访问
     *
     * @throws  UnsupportedEncodingException
     *          如果命名的字符集不受支持
     *
     * @since  1.5
     */
    public PrintStream(File file, String csn)
        throws FileNotFoundException, UnsupportedEncodingException
    {
        // 确保在打开文件之前检查字符集
        this(false, toCharset(csn), new FileOutputStream(file));
    }

    /** 检查流是否已关闭 */
    private void ensureOpen() throws IOException {
        if (out == null)
            throw new IOException("Stream closed");
    }

    /**
     * 刷新流。这是通过将任何缓冲的输出字节写入底层输出流并刷新该流来完成的。
     *
     * @see        java.io.OutputStream#flush()
     */
    public void flush() {
        synchronized (this) {
            try {
                ensureOpen();
                out.flush();
            }
            catch (IOException x) {
                trouble = true;
            }
        }
    }

    private boolean closing = false; /* 以避免递归关闭 */

    /**
     * 关闭流。这是通过刷新流然后关闭底层输出流来完成的。
     *
     * @see        java.io.OutputStream#close()
     */
    public void close() {
        synchronized (this) {
            if (! closing) {
                closing = true;
                try {
                    textOut.close();
                    out.close();
                }
                catch (IOException x) {
                    trouble = true;
                }
                textOut = null;
                charOut = null;
                out = null;
            }
        }
    }

    /**
     * 刷新流并检查其错误状态。如果底层输出流抛出 <code>IOException</code>（不包括 <code>InterruptedIOException</code>），则内部错误状态将设置为 <code>true</code>，或者调用了 <code>setError</code> 方法。如果底层输出流抛出 <code>InterruptedIOException</code>，则 <code>PrintStream</code> 会通过以下方式将异常转换回中断：
     * <pre>
     *     Thread.currentThread().interrupt();
     * </pre>
     * 或等效操作。
     *
     * @return 如果且仅当此流遇到 <code>IOException</code>（不包括 <code>InterruptedIOException</code>），或调用了 <code>setError</code> 方法时，返回 <code>true</code>
     */
    public boolean checkError() {
        if (out != null)
            flush();
        if (out instanceof java.io.PrintStream) {
            PrintStream ps = (PrintStream) out;
            return ps.checkError();
        }
        return trouble;
    }


    /*
     * Copyright (c) 1996, 1999, ...
     */
    /**
     * 将流的错误状态设置为 <code>true</code>。
     *
     * <p> 调用此方法将导致后续调用 {@link
     * #checkError()} 返回 <tt>true</tt>，直到调用 {@link
     * #clearError()}。
     *
     * @since JDK1.1
     */
    protected void setError() {
        trouble = true;
    }

    /**
     * 清除此流的内部错误状态。
     *
     * <p> 调用此方法将导致后续调用 {@link
     * #checkError()} 返回 <tt>false</tt>，直到另一个写操作失败并调用 {@link #setError()}。
     *
     * @since 1.6
     */
    protected void clearError() {
        trouble = false;
    }

    /*
     * 捕获异常的、同步的输出操作，
     * 这些操作也实现了 OutputStream 的 write() 方法
     */

    /**
     * 将指定的字节写入此流。如果字节是换行符并且启用了自动刷新，则将调用 <code>flush</code> 方法。
     *
     * <p> 注意，字节将按原样写入；要写入将根据平台的默认字符编码进行转换的字符，请使用 <code>print(char)</code> 或 <code>println(char)</code>
     * 方法。
     *
     * @param  b  要写入的字节
     * @see #print(char)
     * @see #println(char)
     */
    public void write(int b) {
        try {
            synchronized (this) {
                ensureOpen();
                out.write(b);
                if ((b == '\n') && autoFlush)
                    out.flush();
            }
        }
        catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        catch (IOException x) {
            trouble = true;
        }
    }

    /**
     * 从指定的字节数组中写入 <code>len</code> 个字节，从偏移量 <code>off</code> 开始写入此流。如果启用了自动刷新，则将调用 <code>flush</code> 方法。
     *
     * <p> 注意，字节将按原样写入；要写入将根据平台的默认字符编码进行转换的字符，请使用 <code>print(char)</code> 或 <code>println(char)</code>
     * 方法。
     *
     * @param  buf   字节数组
     * @param  off   从哪个偏移量开始取字节
     * @param  len   要写入的字节数
     */
    public void write(byte buf[], int off, int len) {
        try {
            synchronized (this) {
                ensureOpen();
                out.write(buf, off, len);
                if (autoFlush)
                    out.flush();
            }
        }
        catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        catch (IOException x) {
            trouble = true;
        }
    }

    /*
     * 以下对文本和字符输出流的私有方法总是刷新流缓冲区，以便对底层字节流的写操作与原始 PrintStream 一样及时。
     */

    private void write(char buf[]) {
        try {
            synchronized (this) {
                ensureOpen();
                textOut.write(buf);
                textOut.flushBuffer();
                charOut.flushBuffer();
                if (autoFlush) {
                    for (int i = 0; i < buf.length; i++)
                        if (buf[i] == '\n')
                            out.flush();
                }
            }
        }
        catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        catch (IOException x) {
            trouble = true;
        }
    }

    private void write(String s) {
        try {
            synchronized (this) {
                ensureOpen();
                textOut.write(s);
                textOut.flushBuffer();
                charOut.flushBuffer();
                if (autoFlush && (s.indexOf('\n') >= 0))
                    out.flush();
            }
        }
        catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        catch (IOException x) {
            trouble = true;
        }
    }

    private void newLine() {
        try {
            synchronized (this) {
                ensureOpen();
                textOut.newLine();
                textOut.flushBuffer();
                charOut.flushBuffer();
                if (autoFlush)
                    out.flush();
            }
        }
        catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        catch (IOException x) {
            trouble = true;
        }
    }

    /* 不终止行的方法 */

    /**
     * 打印一个布尔值。由 <code>{@link
     * java.lang.String#valueOf(boolean)}</code> 生成的字符串将根据平台的默认字符编码转换为字节，并以与
     * <code>{@link #write(int)}</code> 方法完全相同的方式写入。
     *
     * @param      b   要打印的 <code>boolean</code>
     */
    public void print(boolean b) {
        write(b ? "true" : "false");
    }

    /**
     * 打印一个字符。该字符将根据平台的默认字符编码转换为一个或多个字节，并以与
     * <code>{@link #write(int)}</code> 方法完全相同的方式写入。
     *
     * @param      c   要打印的 <code>char</code>
     */
    public void print(char c) {
        write(String.valueOf(c));
    }

    /**
     * 打印一个整数。由 <code>{@link
     * java.lang.String#valueOf(int)}</code> 生成的字符串将根据平台的默认字符编码转换为字节，并以与
     * <code>{@link #write(int)}</code> 方法完全相同的方式写入。
     *
     * @param      i   要打印的 <code>int</code>
     * @see        java.lang.Integer#toString(int)
     */
    public void print(int i) {
        write(String.valueOf(i));
    }

    /**
     * 打印一个长整数。由 <code>{@link
     * java.lang.String#valueOf(long)}</code> 生成的字符串将根据平台的默认字符编码转换为字节，并以与
     * <code>{@link #write(int)}</code> 方法完全相同的方式写入。
     *
     * @param      l   要打印的 <code>long</code>
     * @see        java.lang.Long#toString(long)
     */
    public void print(long l) {
        write(String.valueOf(l));
    }

    /**
     * 打印一个浮点数。由 <code>{@link
     * java.lang.String#valueOf(float)}</code> 生成的字符串将根据平台的默认字符编码转换为字节，并以与
     * <code>{@link #write(int)}</code> 方法完全相同的方式写入。
     *
     * @param      f   要打印的 <code>float</code>
     * @see        java.lang.Float#toString(float)
     */
    public void print(float f) {
        write(String.valueOf(f));
    }

    /**
     * 打印一个双精度浮点数。由 <code>{@link java.lang.String#valueOf(double)}</code> 生成的字符串将根据平台的默认字符编码转换为字节，并以与
     * <code>{@link #write(int)}</code> 方法完全相同的方式写入。
     *
     * @param      d   要打印的 <code>double</code>
     * @see        java.lang.Double#toString(double)
     */
    public void print(double d) {
        write(String.valueOf(d));
    }

    /**
     * 打印一个字符数组。字符将根据平台的默认字符编码转换为字节，并以与
     * <code>{@link #write(int)}</code> 方法完全相同的方式写入。
     *
     * @param      s   要打印的字符数组
     *
     * @throws  NullPointerException  如果 <code>s</code> 为 <code>null</code>
     */
    public void print(char s[]) {
        write(s);
    }

    /**
     * 打印一个字符串。如果参数为 <code>null</code>，则打印字符串 <code>"null"</code>。否则，字符串的字符将根据平台的默认字符编码转换为字节，并以与
     * <code>{@link #write(int)}</code> 方法完全相同的方式写入。
     *
     * @param      s   要打印的 <code>String</code>
     */
    public void print(String s) {
        if (s == null) {
            s = "null";
        }
        write(s);
    }

    /**
     * 打印一个对象。由 <code>{@link
     * java.lang.String#valueOf(Object)}</code> 方法生成的字符串将根据平台的默认字符编码转换为字节，并以与
     * <code>{@link #write(int)}</code> 方法完全相同的方式写入。
     *
     * @param      obj   要打印的 <code>Object</code>
     * @see        java.lang.Object#toString()
     */
    public void print(Object obj) {
        write(String.valueOf(obj));
    }

    /* 终止行的方法 */

    /**
     * 通过写入行分隔符字符串来终止当前行。行分隔符字符串由系统属性
     * <code>line.separator</code> 定义，不一定是单个换行符 (<code>'\n'</code>)。
     */
    public void println() {
        newLine();
    }

    /**
     * 打印一个布尔值并终止行。此方法的行为类似于调用 <code>{@link #print(boolean)}</code> 然后调用
     * <code>{@link #println()}</code>。
     *
     * @param x  要打印的 <code>boolean</code>
     */
    public void println(boolean x) {
        synchronized (this) {
            print(x);
            newLine();
        }
    }

    /**
     * 打印一个字符并终止行。此方法的行为类似于调用 <code>{@link #print(char)}</code> 然后调用
     * <code>{@link #println()}</code>。
     *
     * @param x  要打印的 <code>char</code>。
     */
    public void println(char x) {
        synchronized (this) {
            print(x);
            newLine();
        }
    }

    /**
     * 打印一个整数并终止行。此方法的行为类似于调用 <code>{@link #print(int)}</code> 然后调用
     * <code>{@link #println()}</code>。
     *
     * @param x  要打印的 <code>int</code>。
     */
    public void println(int x) {
        synchronized (this) {
            print(x);
            newLine();
        }
    }

    /**
     * 打印一个长整数并终止行。此方法的行为类似于调用 <code>{@link #print(long)}</code> 然后调用
     * <code>{@link #println()}</code>。
     *
     * @param x  要打印的 <code>long</code>。
     */
    public void println(long x) {
        synchronized (this) {
            print(x);
            newLine();
        }
    }

    /**
     * 打印一个浮点数并终止行。此方法的行为类似于调用 <code>{@link #print(float)}</code> 然后调用
     * <code>{@link #println()}</code>。
     *
     * @param x  要打印的 <code>float</code>。
     */
    public void println(float x) {
        synchronized (this) {
            print(x);
            newLine();
        }
    }

    /**
     * 打印一个双精度浮点数并终止行。此方法的行为类似于调用 <code>{@link #print(double)}</code> 然后调用
     * <code>{@link #println()}</code>。
     *
     * @param x  要打印的 <code>double</code>。
     */
    public void println(double x) {
        synchronized (this) {
            print(x);
            newLine();
        }
    }

    /**
     * 打印一个字符数组并终止行。此方法的行为类似于调用 <code>{@link #print(char[])}</code> 然后调用
     * <code>{@link #println()}</code>。
     *
     * @param x  要打印的字符数组。
     */
    public void println(char x[]) {
        synchronized (this) {
            print(x);
            newLine();
        }
    }

    /**
     * 打印一个字符串并终止行。此方法的行为类似于调用 <code>{@link #print(String)}</code> 然后调用
     * <code>{@link #println()}</code>。
     *
     * @param x  要打印的 <code>String</code>。
     */
    public void println(String x) {
        synchronized (this) {
            print(x);
            newLine();
        }
    }

    /**
     * 打印一个对象并终止行。此方法首先调用 String.valueOf(x) 获取要打印的对象的字符串值，然后的行为类似于
     * 调用 <code>{@link #print(String)}</code> 然后调用 <code>{@link #println()}</code>。
     *
     * @param x  要打印的 <code>Object</code>。
     */
    public void println(Object x) {
        String s = String.valueOf(x);
        synchronized (this) {
            print(s);
            newLine();
        }
    }

    /**
     * 一个方便的方法，使用指定的格式字符串和参数将格式化的字符串写入此输出流。
     *
     * <p> 以 <tt>out.printf(format,
     * args)</tt> 形式调用此方法的行为与调用
     *
     * <pre>
     *     out.format(format, args) </pre>
     *
     * 完全相同。
     *
     * @param  format
     *         一个格式字符串，如 <a
     *         href="../util/Formatter.html#syntax">格式字符串语法</a> 所述
     *
     * @param  args
     *         由格式字符串中的格式说明符引用的参数。如果有更多的参数比格式说明符多，多余的参数将被忽略。参数的数量是可变的，可以为零。参数的最大数量受
     *         <cite>The Java&trade; Virtual Machine Specification</cite> 定义的 Java 数组的最大维度限制。
     *         对于 <tt>null</tt> 参数的行为取决于 <a
     *         href="../util/Formatter.html#syntax">转换</a>。
     *
     * @throws  java.util.IllegalFormatException
     *          如果格式字符串包含非法语法，格式说明符与给定的参数不兼容，给定的格式字符串的参数不足，或其他非法条件。有关所有可能的格式错误的规范，请参见
     *          格式化器类规范的 <a
     *          href="../util/Formatter.html#detail">详细信息</a> 部分。
     *
     * @throws  NullPointerException
     *          如果 <tt>format</tt> 为 <tt>null</tt>
     *
     * @return  此输出流
     *
     * @since  1.5
     */
    public PrintStream printf(String format, Object ... args) {
        return format(format, args);
    }

    /**
     * 一个方便的方法，使用指定的格式字符串和参数将格式化的字符串写入此输出流。
     *
     * <p> 以 <tt>out.printf(l, format,
     * args)</tt> 形式调用此方法的行为与调用
     *
     * <pre>
     *     out.format(l, format, args) </pre>
     *
     * 完全相同。
     *
     * @param  l
     *         在格式化期间应用的 {@linkplain java.util.Locale 本地化}。如果 <tt>l</tt> 为 <tt>null</tt>，则不应用本地化。
     *
     * @param  format
     *         一个格式字符串，如 <a
     *         href="../util/Formatter.html#syntax">格式字符串语法</a> 所述
     *
     * @param  args
     *         由格式字符串中的格式说明符引用的参数。如果有更多的参数比格式说明符多，多余的参数将被忽略。参数的数量是可变的，可以为零。参数的最大数量受
     *         <cite>The Java&trade; Virtual Machine Specification</cite> 定义的 Java 数组的最大维度限制。
     *         对于 <tt>null</tt> 参数的行为取决于 <a
     *         href="../util/Formatter.html#syntax">转换</a>。
     *
     * @throws  java.util.IllegalFormatException
     *          如果格式字符串包含非法语法，格式说明符与给定的参数不兼容，给定的格式字符串的参数不足，或其他非法条件。有关所有可能的格式错误的规范，请参见
     *          格式化器类规范的 <a
     *          href="../util/Formatter.html#detail">详细信息</a> 部分。
     *
     * @throws  NullPointerException
     *          如果 <tt>format</tt> 为 <tt>null</tt>
     *
     * @return  此输出流
     *
     * @since  1.5
     */
    public PrintStream printf(Locale l, String format, Object ... args) {
        return format(l, format, args);
    }


                /**
     * 向此输出流写入使用指定格式字符串和参数格式化的字符串。
     *
     * <p> 始终使用的区域设置是 {@link
     * java.util.Locale#getDefault() Locale.getDefault()} 返回的区域设置，无论此对象上先前对其他格式化方法的调用如何。
     *
     * @param  format
     *         如 <a
     *         href="../util/Formatter.html#syntax">格式字符串语法</a> 中所述的格式字符串。
     *
     * @param  args
     *         由格式字符串中的格式说明符引用的参数。如果参数多于格式说明符，则多余的参数将被忽略。参数的数量是可变的，可以为零。参数的最大数量由 <cite>The Java&trade; Virtual Machine Specification</cite> 定义的 Java 数组的最大维度限制。对于 <tt>null</tt> 参数的行为取决于 <a
     *         href="../util/Formatter.html#syntax">转换</a>。
     *
     * @throws  java.util.IllegalFormatException
     *          如果格式字符串包含非法语法、与给定参数不兼容的格式说明符、格式字符串的参数不足或其他非法条件。有关所有可能的格式错误的规范，请参阅格式化器类规范的 <a
     *          href="../util/Formatter.html#detail">详细信息</a> 部分。
     *
     * @throws  NullPointerException
     *          如果 <tt>format</tt> 为 <tt>null</tt>。
     *
     * @return  此输出流。
     *
     * @since  1.5
     */
    public PrintStream format(String format, Object ... args) {
        try {
            synchronized (this) {
                ensureOpen();
                if ((formatter == null)
                    || (formatter.locale() != Locale.getDefault()))
                    formatter = new Formatter((Appendable) this);
                formatter.format(Locale.getDefault(), format, args);
            }
        } catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        } catch (IOException x) {
            trouble = true;
        }
        return this;
    }

    /**
     * 向此输出流写入使用指定格式字符串和参数格式化的字符串。
     *
     * @param  l
     *         在格式化期间应用的 {@linkplain java.util.Locale 区域设置}。如果 <tt>l</tt> 为 <tt>null</tt>，则不应用区域设置。
     *
     * @param  format
     *         如 <a
     *         href="../util/Formatter.html#syntax">格式字符串语法</a> 中所述的格式字符串。
     *
     * @param  args
     *         由格式字符串中的格式说明符引用的参数。如果参数多于格式说明符，则多余的参数将被忽略。参数的数量是可变的，可以为零。参数的最大数量由 <cite>The Java&trade; Virtual Machine Specification</cite> 定义的 Java 数组的最大维度限制。对于 <tt>null</tt> 参数的行为取决于 <a
     *         href="../util/Formatter.html#syntax">转换</a>。
     *
     * @throws  java.util.IllegalFormatException
     *          如果格式字符串包含非法语法、与给定参数不兼容的格式说明符、格式字符串的参数不足或其他非法条件。有关所有可能的格式错误的规范，请参阅格式化器类规范的 <a
     *          href="../util/Formatter.html#detail">详细信息</a> 部分。
     *
     * @throws  NullPointerException
     *          如果 <tt>format</tt> 为 <tt>null</tt>。
     *
     * @return  此输出流。
     *
     * @since  1.5
     */
    public PrintStream format(Locale l, String format, Object ... args) {
        try {
            synchronized (this) {
                ensureOpen();
                if ((formatter == null)
                    || (formatter.locale() != l))
                    formatter = new Formatter(this, l);
                formatter.format(l, format, args);
            }
        } catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        } catch (IOException x) {
            trouble = true;
        }
        return this;
    }

    /**
     * 将指定的字符序列附加到此输出流。
     *
     * <p> 以 <tt>out.append(csq)</tt> 形式调用此方法的行为与以下调用完全相同：
     *
     * <pre>
     *     out.print(csq.toString()) </pre>
     *
     * <p> 根据字符序列 <tt>csq</tt> 的 <tt>toString</tt> 方法的规范，整个序列可能不会被附加。例如，调用字符缓冲区的 <tt>toString</tt> 方法将返回一个子序列，其内容取决于缓冲区的位置和限制。
     *
     * @param  csq
     *         要附加的字符序列。如果 <tt>csq</tt> 为 <tt>null</tt>，则将四个字符 <tt>"null"</tt> 附加到此输出流。
     *
     * @return  此输出流。
     *
     * @since  1.5
     */
    public PrintStream append(CharSequence csq) {
        if (csq == null)
            print("null");
        else
            print(csq.toString());
        return this;
    }

    /**
     * 将指定字符序列的子序列附加到此输出流。
     *
     * <p> 以 <tt>out.append(csq, start, end)</tt> 形式调用此方法且 <tt>csq</tt> 不为 <tt>null</tt> 时，其行为与以下调用完全相同：
     *
     * <pre>
     *     out.print(csq.subSequence(start, end).toString()) </pre>
     *
     * @param  csq
     *         从中将附加子序列的字符序列。如果 <tt>csq</tt> 为 <tt>null</tt>，则将字符附加为 <tt>csq</tt> 包含四个字符 <tt>"null"</tt>。
     *
     * @param  start
     *         子序列中第一个字符的索引。
     *
     * @param  end
     *         子序列中最后一个字符之后的字符的索引。
     *
     * @return  此输出流。
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>start</tt> 或 <tt>end</tt> 为负数，<tt>start</tt> 大于 <tt>end</tt>，或 <tt>end</tt> 大于 <tt>csq.length()</tt>。
     *
     * @since  1.5
     */
    public PrintStream append(CharSequence csq, int start, int end) {
        CharSequence cs = (csq == null ? "null" : csq);
        write(cs.subSequence(start, end).toString());
        return this;
    }

    /**
     * 将指定字符附加到此输出流。
     *
     * <p> 以 <tt>out.append(c)</tt> 形式调用此方法的行为与以下调用完全相同：
     *
     * <pre>
     *     out.print(c) </pre>
     *
     * @param  c
     *         要附加的 16 位字符。
     *
     * @return  此输出流。
     *
     * @since  1.5
     */
    public PrintStream append(char c) {
        print(c);
        return this;
    }

}
