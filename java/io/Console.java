
/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.util.*;
import java.nio.charset.Charset;
import sun.nio.cs.StreamDecoder;
import sun.nio.cs.StreamEncoder;

/**
 * 提供方法以访问与当前 Java 虚拟机关联的基于字符的控制台设备（如果有）。
 *
 * <p> 虚拟机是否有控制台取决于底层平台以及虚拟机的启动方式。如果虚拟机是从交互式命令行启动且没有重定向标准输入和输出流，则其控制台将存在，并且通常会连接到启动虚拟机的键盘和显示器。如果虚拟机是自动启动的，例如由后台作业调度程序启动，则通常不会有控制台。
 * <p>
 * 如果此虚拟机有控制台，则它由一个唯一的此类实例表示，可以通过调用 {@link java.lang.System#console()} 方法获取。如果没有可用的控制台设备，则该方法的调用将返回 <tt>null</tt>。
 * <p>
 * 读取和写入操作是同步的，以保证关键操作的原子完成；因此调用方法 {@link #readLine()}、{@link #readPassword()}、{@link #format format()}、
 * {@link #printf printf()} 以及在 {@link #reader()} 和 {@link #writer()} 返回的对象上的读取、格式化和写入操作在多线程场景中可能会阻塞。
 * <p>
 * 调用 {@link #reader()} 和 {@link #writer()} 返回的对象的 <tt>close()</tt> 方法不会关闭这些对象的底层流。
 * <p>
 * 控制台读取方法在达到控制台输入流的末尾时返回 <tt>null</tt>，例如在 Unix 上输入控制-D 或在 Windows 上输入控制-Z。如果在控制台输入设备上稍后输入了更多字符，则后续读取操作将成功。
 * <p>
 * 除非另有说明，向此类的任何方法传递 <tt>null</tt> 参数将导致抛出 {@link NullPointerException}。
 * <p>
 * <b>安全注意事项：</b>
 * 如果应用程序需要读取密码或其他敏感数据，应使用 {@link #readPassword()} 或 {@link #readPassword(String, Object...)} 并在处理后手动将返回的字符数组置零，以尽量减少敏感数据在内存中的生命周期。
 *
 * <blockquote><pre>{@code
 * Console cons;
 * char[] passwd;
 * if ((cons = System.console()) != null &&
 *     (passwd = cons.readPassword("[%s]", "Password:")) != null) {
 *     ...
 *     java.util.Arrays.fill(passwd, ' ');
 * }
 * }</pre></blockquote>
 *
 * @author  Xueming Shen
 * @since   1.6
 */

public final class Console implements Flushable
{
   /**
    * 检索与此控制台关联的唯一 {@link java.io.PrintWriter PrintWriter} 对象。
    *
    * @return  与此控制台关联的 PrintWriter
    */
    public PrintWriter writer() {
        return pw;
    }

   /**
    * 检索与此控制台关联的唯一 {@link java.io.Reader Reader} 对象。
    * <p>
    * 此方法旨在由复杂的应用程序使用，例如，一个 {@link java.util.Scanner} 对象，该对象利用 <tt>Scanner</tt> 提供的丰富的解析/扫描功能：
    * <blockquote><pre>
    * Console con = System.console();
    * if (con != null) {
    *     Scanner sc = new Scanner(con.reader());
    *     ...
    * }
    * </pre></blockquote>
    * <p>
    * 对于只需要行导向读取的简单应用程序，使用 <tt>{@link #readLine}</tt>。
    * <p>
    * 在返回的对象上执行的批量读取操作 {@link java.io.Reader#read(char[]) read(char[]) }、
    * {@link java.io.Reader#read(char[], int, int) read(char[], int, int) } 和
    * {@link java.io.Reader#read(java.nio.CharBuffer) read(java.nio.CharBuffer)} 在每次调用时不会读取超过行边界，即使目标缓冲区有足够的空间。如果在控制台输入设备上未输入或未达到行边界，则 <tt>Reader</tt> 的 <tt>read</tt> 方法可能会阻塞。
    * 行边界被认为是以下任何一个：换行符 (<tt>'\n'</tt>)、回车符 (<tt>'\r'</tt>)、回车符后紧跟换行符，或流的结束。
    *
    * @return  与此控制台关联的 Reader
    */
    public Reader reader() {
        return reader;
    }

   /**
    * 使用指定的格式字符串和参数将格式化的字符串写入此控制台的输出流。
    *
    * @param  fmt
    *         格式字符串，如 <a href="../util/Formatter.html#syntax">格式字符串语法</a> 所述。
    *
    * @param  args
    *         由格式字符串中的格式说明符引用的参数。如果参数多于格式说明符，则多余的参数将被忽略。参数的数量是可变的，可以为零。参数的最大数量由 <cite>The Java&trade; Virtual Machine Specification</cite> 定义的 Java 数组的最大维度限制。
    *         对于 <tt>null</tt> 参数的行为取决于 <a href="../util/Formatter.html#syntax">转换</a>。
    *
    * @throws  IllegalFormatException
    *          如果格式字符串包含非法语法、与给定参数不兼容的格式说明符、格式字符串的参数不足或其他非法条件。有关所有可能的格式错误的说明，请参见格式化器类规范的 <a href="../util/Formatter.html#detail">详细信息</a> 部分。
    *
    * @return  此控制台
    */
    public Console format(String fmt, Object ...args) {
        formatter.format(fmt, args).flush();
        return this;
    }

   /**
    * 一个方便的方法，使用指定的格式字符串和参数将格式化的字符串写入此控制台的输出流。
    *
    * <p> 以 <tt>con.printf(format, args)</tt> 形式调用此方法的行为与调用
    * <pre>con.format(format, args)</pre> 完全相同。
    *
    * @param  format
    *         格式字符串，如 <a href="../util/Formatter.html#syntax">格式字符串语法</a> 所述。
    *
    * @param  args
    *         由格式字符串中的格式说明符引用的参数。如果参数多于格式说明符，则多余的参数将被忽略。参数的数量是可变的，可以为零。参数的最大数量由 <cite>The Java&trade; Virtual Machine Specification</cite> 定义的 Java 数组的最大维度限制。
    *         对于 <tt>null</tt> 参数的行为取决于 <a href="../util/Formatter.html#syntax">转换</a>。
    *
    * @throws  IllegalFormatException
    *          如果格式字符串包含非法语法、与给定参数不兼容的格式说明符、格式字符串的参数不足或其他非法条件。有关所有可能的格式错误的说明，请参见格式化器类规范的 <a href="../util/Formatter.html#detail">详细信息</a> 部分。
    *
    * @return  此控制台
    */
    public Console printf(String format, Object ... args) {
        return format(format, args);
    }

   /**
    * 提供格式化的提示，然后从控制台读取一行文本。
    *
    * @param  fmt
    *         格式字符串，如 <a href="../util/Formatter.html#syntax">格式字符串语法</a> 所述。
    *
    * @param  args
    *         由格式字符串中的格式说明符引用的参数。如果参数多于格式说明符，则多余的参数将被忽略。参数的最大数量由 <cite>The Java&trade; Virtual Machine Specification</cite> 定义的 Java 数组的最大维度限制。
    *
    * @throws  IllegalFormatException
    *          如果格式字符串包含非法语法、与给定参数不兼容的格式说明符、格式字符串的参数不足或其他非法条件。有关所有可能的格式错误的说明，请参见格式化器类规范的 <a href="../util/Formatter.html#detail">详细信息</a> 部分。
    *
    * @throws IOError
    *         如果发生 I/O 错误。
    *
    * @return  一个包含从控制台读取的行的字符串，不包括任何行终止字符，或 <tt>null</tt> 如果已达到流的末尾。
    */
    public String readLine(String fmt, Object ... args) {
        String line = null;
        synchronized (writeLock) {
            synchronized(readLock) {
                if (fmt.length() != 0)
                    pw.format(fmt, args);
                try {
                    char[] ca = readline(false);
                    if (ca != null)
                        line = new String(ca);
                } catch (IOException x) {
                    throw new IOError(x);
                }
            }
        }
        return line;
    }

   /**
    * 从控制台读取一行文本。
    *
    * @throws IOError
    *         如果发生 I/O 错误。
    *
    * @return  一个包含从控制台读取的行的字符串，不包括任何行终止字符，或 <tt>null</tt> 如果已达到流的末尾。
    */
    public String readLine() {
        return readLine("");
    }

   /**
    * 提供格式化的提示，然后从控制台读取密码或短语，禁用回显。
    *
    * @param  fmt
    *         提示文本的格式字符串，如 <a href="../util/Formatter.html#syntax">格式字符串语法</a> 所述。
    *
    * @param  args
    *         由格式字符串中的格式说明符引用的参数。如果参数多于格式说明符，则多余的参数将被忽略。参数的最大数量由 <cite>The Java&trade; Virtual Machine Specification</cite> 定义的 Java 数组的最大维度限制。
    *
    * @throws  IllegalFormatException
    *          如果格式字符串包含非法语法、与给定参数不兼容的格式说明符、格式字符串的参数不足或其他非法条件。有关所有可能的格式错误的说明，请参见格式化器类规范的 <a href="../util/Formatter.html#detail">详细信息</a> 部分。
    *
    * @throws IOError
    *         如果发生 I/O 错误。
    *
    * @return  一个包含从控制台读取的密码或短语的字符数组，不包括任何行终止字符，或 <tt>null</tt> 如果已达到流的末尾。
    */
    public char[] readPassword(String fmt, Object ... args) {
        char[] passwd = null;
        synchronized (writeLock) {
            synchronized(readLock) {
                try {
                    echoOff = echo(false);
                } catch (IOException x) {
                    throw new IOError(x);
                }
                IOError ioe = null;
                try {
                    if (fmt.length() != 0)
                        pw.format(fmt, args);
                    passwd = readline(true);
                } catch (IOException x) {
                    ioe = new IOError(x);
                } finally {
                    try {
                        echoOff = echo(true);
                    } catch (IOException x) {
                        if (ioe == null)
                            ioe = new IOError(x);
                        else
                            ioe.addSuppressed(x);
                    }
                    if (ioe != null)
                        throw ioe;
                }
                pw.println();
            }
        }
        return passwd;
    }

   /**
    * 从控制台读取密码或短语，禁用回显。
    *
    * @throws IOError
    *         如果发生 I/O 错误。
    *
    * @return  一个包含从控制台读取的密码或短语的字符数组，不包括任何行终止字符，或 <tt>null</tt> 如果已达到流的末尾。
    */
    public char[] readPassword() {
        return readPassword("");
    }

    /**
     * 刷新控制台并强制任何缓冲的输出立即写入。
     */
    public void flush() {
        pw.flush();
    }

    private Object readLock;
    private Object writeLock;
    private Reader reader;
    private Writer out;
    private PrintWriter pw;
    private Formatter formatter;
    private Charset cs;
    private char[] rcb;
    private static native String encoding();
    private static native boolean echo(boolean on) throws IOException;
    private static boolean echoOff;

    private char[] readline(boolean zeroOut) throws IOException {
        int len = reader.read(rcb, 0, rcb.length);
        if (len < 0)
            return null;  //EOL
        if (rcb[len-1] == '\r')
            len--;        //移除行尾的 CR
        else if (rcb[len-1] == '\n') {
            len--;        //移除行尾的 LF
            if (len > 0 && rcb[len-1] == '\r')
                len--;    //移除行尾的 CR，如果有的话
        }
        char[] b = new char[len];
        if (len > 0) {
            System.arraycopy(rcb, 0, b, 0, len);
            if (zeroOut) {
                Arrays.fill(rcb, 0, len, ' ');
            }
        }
        return b;
    }


                private char[] grow() {
        assert Thread.holdsLock(readLock);
        char[] t = new char[rcb.length * 2];
        System.arraycopy(rcb, 0, t, 0, rcb.length);
        rcb = t;
        return rcb;
    }

    class LineReader extends Reader {
        private Reader in;
        private char[] cb;
        private int nChars, nextChar;
        boolean leftoverLF;
        LineReader(Reader in) {
            this.in = in;
            cb = new char[1024];
            nextChar = nChars = 0;
            leftoverLF = false;
        }
        public void close () {}
        public boolean ready() throws IOException {
            // in.ready 已经在 readLock 上同步
            return in.ready();
        }

        public int read(char cbuf[], int offset, int length)
            throws IOException
        {
            int off = offset;
            int end = offset + length;
            if (offset < 0 || offset > cbuf.length || length < 0 ||
                end < 0 || end > cbuf.length) {
                throw new IndexOutOfBoundsException();
            }
            synchronized(readLock) {
                boolean eof = false;
                char c = 0;
                for (;;) {
                    if (nextChar >= nChars) {   // 填充
                        int n = 0;
                        do {
                            n = in.read(cb, 0, cb.length);
                        } while (n == 0);
                        if (n > 0) {
                            nChars = n;
                            nextChar = 0;
                            if (n < cb.length &&
                                cb[n-1] != '\n' && cb[n-1] != '\r') {
                                /*
                                 * 我们处于规范模式，因此每次“填充”都应该返回一个 eol。如果返回的字节末尾没有 LF 或 NL，
                                 * 则表示已达到 EOF。
                                 */
                                eof = true;
                            }
                        } else { /* EOF */
                            if (off - offset == 0)
                                return -1;
                            return off - offset;
                        }
                    }
                    if (leftoverLF && cbuf == rcb && cb[nextChar] == '\n') {
                        /*
                         * 如果由我们的 readline 调用，跳过剩余的，否则返回 LF。
                         */
                        nextChar++;
                    }
                    leftoverLF = false;
                    while (nextChar < nChars) {
                        c = cbuf[off++] = cb[nextChar];
                        cb[nextChar++] = 0;
                        if (c == '\n') {
                            return off - offset;
                        } else if (c == '\r') {
                            if (off == end) {
                                /* 即使下一个字符是 LF，也没有空间了，因此如果调用者不是我们的 readLine()，则返回当前已读取的内容 */
                                if (cbuf == rcb) {
                                    cbuf = grow();
                                    end = cbuf.length;
                                } else {
                                    leftoverLF = true;
                                    return off - offset;
                                }
                            }
                            if (nextChar == nChars && in.ready()) {
                                /*
                                 * 我们有一个 CR，并且已达到读取缓冲区的末尾，填充以确保不会错过 LF（如果有），可能在上一轮读取时由于缓冲区已满而被截断。
                                 */
                                nChars = in.read(cb, 0, cb.length);
                                nextChar = 0;
                            }
                            if (nextChar < nChars && cb[nextChar] == '\n') {
                                cbuf[off++] = '\n';
                                nextChar++;
                            }
                            return off - offset;
                        } else if (off == end) {
                           if (cbuf == rcb) {
                                cbuf = grow();
                                end = cbuf.length;
                           } else {
                               return off - offset;
                           }
                        }
                    }
                    if (eof)
                        return off - offset;
                }
            }
        }
    }

    // 在 SharedSecrets 中设置 JavaIOAccess
    static {
        try {
            // 添加一个关闭钩子，以在必要时恢复控制台的回显状态。
            sun.misc.SharedSecrets.getJavaLangAccess()
                .registerShutdownHook(0 /* 关闭钩子调用顺序 */,
                    false /* 仅在关闭未进行时注册 */,
                    new Runnable() {
                        public void run() {
                            try {
                                if (echoOff) {
                                    echo(true);
                                }
                            } catch (IOException x) { }
                        }
                    });
        } catch (IllegalStateException e) {
            // 关闭已在进行中，且控制台首次由关闭钩子使用
        }

        sun.misc.SharedSecrets.setJavaIOAccess(new sun.misc.JavaIOAccess() {
            public Console console() {
                if (istty()) {
                    if (cons == null)
                        cons = new Console();
                    return cons;
                }
                return null;
            }

            public Charset charset() {
                // 此方法在 sun.security.util.Password 中调用，当此方法被调用时，cons 已存在
                return cons.cs;
            }
        });
    }
    private static Console cons;
    private native static boolean istty();
    private Console() {
        readLock = new Object();
        writeLock = new Object();
        String csname = encoding();
        if (csname != null) {
            try {
                cs = Charset.forName(csname);
            } catch (Exception x) {}
        }
        if (cs == null)
            cs = Charset.defaultCharset();
        out = StreamEncoder.forOutputStreamWriter(
                  new FileOutputStream(FileDescriptor.out),
                  writeLock,
                  cs);
        pw = new PrintWriter(out, true) { public void close() {} };
        formatter = new Formatter(out);
        reader = new LineReader(StreamDecoder.forInputStreamReader(
                     new FileInputStream(FileDescriptor.in),
                     readLock,
                     cs));
        rcb = new char[1024];
    }
}
