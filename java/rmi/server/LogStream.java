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
package java.rmi.server;

import java.io.*;
import java.util.*;

/**
 * <code>LogStream</code> 提供了一种记录可能对监控系统感兴趣的错误的机制。
 *
 * @author  Ann Wollrath (大量代码借鉴自 Ken Arnold)
 * @since   JDK1.1
 * @deprecated 没有替代
 */
@Deprecated
public class LogStream extends PrintStream {

    /** 表格，将已知的日志名称映射到日志流对象 */
    private static Map<String,LogStream> known = new HashMap<>(5);
    /** 新日志的默认输出流 */
    private static PrintStream  defaultStream = System.err;

    /** 此日志的名称 */
    private String name;

    /** 此日志的输出流 */
    private OutputStream logOut;

    /** 用于将消息前缀写入日志流的字符串写入器 */
    private OutputStreamWriter logWriter;

    /** 用于构建日志消息前缀的字符串缓冲区 */
    private StringBuffer buffer = new StringBuffer();

    /** 用于缓冲行的流 */
    private ByteArrayOutputStream bufOut;

    /**
     * 创建一个新的 LogStream 对象。由于这是唯一的构造函数是私有的，
     * 用户必须通过 "log" 方法创建 LogStream。
     * @param name 识别此日志的消息的字符串
     * @param out 日志消息将发送到的输出流
     * @since JDK1.1
     * @deprecated 没有替代
     */
    @Deprecated
    private LogStream(String name, OutputStream out)
    {
        super(new ByteArrayOutputStream());
        bufOut = (ByteArrayOutputStream) super.out;

        this.name = name;
        setOutputStream(out);
    }

    /**
     * 返回由给定名称标识的 LogStream。如果不存在与 "name" 对应的日志，
     * 则创建一个使用默认流的日志。
     * @param name 识别所需 LogStream 的名称
     * @return 与给定名称关联的日志
     * @since JDK1.1
     * @deprecated 没有替代
     */
    @Deprecated
    public static LogStream log(String name) {
        LogStream stream;
        synchronized (known) {
            stream = known.get(name);
            if (stream == null) {
                stream = new LogStream(name, defaultStream);
            }
            known.put(name, stream);
        }
        return stream;
    }

    /**
     * 返回新日志的当前默认流。
     * @return 默认日志流
     * @see #setDefaultStream
     * @since JDK1.1
     * @deprecated 没有替代
     */
    @Deprecated
    public static synchronized PrintStream getDefaultStream() {
        return defaultStream;
    }

    /**
     * 设置新日志的默认流。
     * @param newDefault 新的默认日志流
     * @see #getDefaultStream
     * @since JDK1.1
     * @deprecated 没有替代
     */
    @Deprecated
    public static synchronized void setDefaultStream(PrintStream newDefault) {
        SecurityManager sm = System.getSecurityManager();

        if (sm != null) {
            sm.checkPermission(
                new java.util.logging.LoggingPermission("control", null));
        }

        defaultStream = newDefault;
    }

    /**
     * 返回此日志的当前输出流。
     * @return 此日志的输出流
     * @see #setOutputStream
     * @since JDK1.1
     * @deprecated 没有替代
     */
    @Deprecated
    public synchronized OutputStream getOutputStream()
    {
        return logOut;
    }

    /**
     * 设置此日志的输出流。
     * @param out 此日志的新输出流
     * @see #getOutputStream
     * @since JDK1.1
     * @deprecated 没有替代
     */
    @Deprecated
    public synchronized void setOutputStream(OutputStream out)
    {
        logOut = out;
        // 维护一个带有默认 CharToByteConvertor 的 OutputStreamWriter
        // （就像新的 PrintStream 一样）用于写入日志消息前缀。
        logWriter = new OutputStreamWriter(logOut);
    }

    /**
     * 将一个字节的数据写入流。如果不是换行符，则该字节将附加到内部缓冲区。
     * 如果是换行符，则将当前缓冲的行发送到日志的输出流，并带有适当的日志信息前缀。
     * @since JDK1.1
     * @deprecated 没有替代
     */
    @Deprecated
    public void write(int b)
    {
        if (b == '\n') {
            // 首先同步 "this" 以避免潜在的死锁
            synchronized (this) {
                synchronized (logOut) {
                    // 构建日志消息的前缀：
                    buffer.setLength(0);;
                    buffer.append(              // 日期/时间戳...
                        (new Date()).toString());
                    buffer.append(':');
                    buffer.append(name);        // ...日志名称...
                    buffer.append(':');
                    buffer.append(Thread.currentThread().getName());
                    buffer.append(':'); // ...和线程名称

                    try {
                        // 通过底层字节流写入前缀
                        logWriter.write(buffer.toString());
                        logWriter.flush();

                        // 最后，写入已经转换的字节
                        // 日志消息
                        bufOut.writeTo(logOut);
                        logOut.write(b);
                        logOut.flush();
                    } catch (IOException e) {
                        setError();
                    } finally {
                        bufOut.reset();
                    }
                }
            }
        }
        else
            super.write(b);
    }

    /**
     * 写入字节数组的子数组。通过写入字节方法传递每个字节。
     * @since JDK1.1
     * @deprecated 没有替代
     */
    @Deprecated
    public void write(byte b[], int off, int len)
    {
        if (len < 0)
            throw new ArrayIndexOutOfBoundsException(len);
        for (int i = 0; i < len; ++ i)
            write(b[off + i]);
    }

    /**
     * 返回日志名称的字符串表示。
     * @return 日志名称
     * @since JDK1.1
     * @deprecated 没有替代
     */
    @Deprecated
    public String toString()
    {
        return name;
    }

    /** 日志级别常量（无日志记录）。 */
    public static final int SILENT  = 0;
    /** 日志级别常量（简要日志记录）。 */
    public static final int BRIEF   = 10;
    /** 日志级别常量（详细日志记录）。 */
    public static final int VERBOSE = 20;

    /**
     * 将日志级别名称的字符串转换为其内部整数表示。
     * @param s 日志级别名称（例如，'SILENT', 'BRIEF', 'VERBOSE'）
     * @return 对应的整数日志级别
     * @since JDK1.1
     * @deprecated 没有替代
     */
    @Deprecated
    public static int parseLevel(String s)
    {
        if ((s == null) || (s.length() < 1))
            return -1;

        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
        }
        if (s.length() < 1)
            return -1;

        if ("SILENT".startsWith(s.toUpperCase()))
            return SILENT;
        else if ("BRIEF".startsWith(s.toUpperCase()))
            return BRIEF;
        else if ("VERBOSE".startsWith(s.toUpperCase()))
            return VERBOSE;

        return -1;
    }
}
