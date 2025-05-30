/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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


package java.util.logging;

import java.io.*;

/**
 * 基于流的日志处理程序。
 * <p>
 * 这主要是一个基类或支持类，用于实现其他日志处理程序。
 * <p>
 * <tt>LogRecords</tt> 被发布到给定的 <tt>java.io.OutputStream</tt>。
 * <p>
 * <b>配置：</b>
 * 默认情况下，每个 <tt>StreamHandler</tt> 使用以下 <tt>LogManager</tt> 配置属性进行初始化，其中 <tt>&lt;handler-name&gt;</tt>
 * 指的是处理程序的完全限定类名。
 * 如果属性未定义
 * （或有无效值）则使用指定的默认值。
 * <ul>
 * <li>   &lt;handler-name&gt;.level
 *        指定 <tt>Handler</tt> 的默认级别
 *        （默认为 <tt>Level.INFO</tt>）。 </li>
 * <li>   &lt;handler-name&gt;.filter
 *        指定要使用的 <tt>Filter</tt> 类
 *         （默认为无 <tt>Filter</tt>）。 </li>
 * <li>   &lt;handler-name&gt;.formatter
 *        指定要使用的 <tt>Formatter</tt> 类
 *        （默认为 <tt>java.util.logging.SimpleFormatter</tt>）。 </li>
 * <li>   &lt;handler-name&gt;.encoding
 *        要使用的字符集编码名称（默认为
 *        平台默认编码）。 </li>
 * </ul>
 * <p>
 * 例如，<tt>StreamHandler</tt> 的属性为：
 * <ul>
 * <li>   java.util.logging.StreamHandler.level=INFO </li>
 * <li>   java.util.logging.StreamHandler.formatter=java.util.logging.SimpleFormatter </li>
 * </ul>
 * <p>
 * 对于自定义处理程序，例如 com.foo.MyHandler，属性为：
 * <ul>
 * <li>   com.foo.MyHandler.level=INFO </li>
 * <li>   com.foo.MyHandler.formatter=java.util.logging.SimpleFormatter </li>
 * </ul>
 * <p>
 * @since 1.4
 */

public class StreamHandler extends Handler {
    private OutputStream output;
    private boolean doneHeader;
    private volatile Writer writer;

    // 私有方法，用于从 LogManager 属性和/或类
    // javadoc 中指定的默认值配置 StreamHandler。
    private void configure() {
        LogManager manager = LogManager.getLogManager();
        String cname = getClass().getName();

        setLevel(manager.getLevelProperty(cname +".level", Level.INFO));
        setFilter(manager.getFilterProperty(cname +".filter", null));
        setFormatter(manager.getFormatterProperty(cname +".formatter", new SimpleFormatter()));
        try {
            setEncoding(manager.getStringProperty(cname +".encoding", null));
        } catch (Exception ex) {
            try {
                setEncoding(null);
            } catch (Exception ex2) {
                // 使用 null 进行 setEncoding 应该总是有效。
                // assert false;
            }
        }
    }

    /**
     * 创建一个没有当前输出流的 <tt>StreamHandler</tt>。
     */
    public StreamHandler() {
        sealed = false;
        configure();
        sealed = true;
    }

    /**
     * 使用给定的 <tt>Formatter</tt> 和输出流创建一个 <tt>StreamHandler</tt>。
     * <p>
     * @param out         目标输出流
     * @param formatter   用于格式化输出的 Formatter
     */
    public StreamHandler(OutputStream out, Formatter formatter) {
        sealed = false;
        configure();
        setFormatter(formatter);
        setOutputStream(out);
        sealed = true;
    }

    /**
     * 更改输出流。
     * <P>
     * 如果当前有输出流，则会写入 <tt>Formatter</tt> 的尾部字符串，并刷新和关闭流。
     * 然后用新的输出流替换当前输出流。
     *
     * @param out   新的输出流。不允许为 null。
     * @exception  SecurityException  如果存在安全经理，并且调用者没有 <tt>LoggingPermission("control")</tt>。
     */
    protected synchronized void setOutputStream(OutputStream out) throws SecurityException {
        if (out == null) {
            throw new NullPointerException();
        }
        flushAndClose();
        output = out;
        doneHeader = false;
        String encoding = getEncoding();
        if (encoding == null) {
            writer = new OutputStreamWriter(output);
        } else {
            try {
                writer = new OutputStreamWriter(output, encoding);
            } catch (UnsupportedEncodingException ex) {
                // 这不应该发生。setEncoding 方法
                // 应该已经验证了编码是有效的。
                throw new Error("Unexpected exception " + ex);
            }
        }
    }

    /**
     * 设置（或更改）此 <tt>Handler</tt> 使用的字符编码。
     * <p>
     * 应在任何 <tt>LogRecords</tt> 写入 <tt>Handler</tt> 之前设置编码。
     *
     * @param encoding  支持的字符编码名称。
     *        可以为 null，表示使用默认平台编码。
     * @exception  SecurityException  如果存在安全经理，并且调用者没有 <tt>LoggingPermission("control")</tt>。
     * @exception  UnsupportedEncodingException 如果指定的编码不受支持。
     */
    @Override
    public synchronized void setEncoding(String encoding)
                        throws SecurityException, java.io.UnsupportedEncodingException {
        super.setEncoding(encoding);
        if (output == null) {
            return;
        }
        // 用新编码的写入器替换当前写入器。
        flush();
        if (encoding == null) {
            writer = new OutputStreamWriter(output);
        } else {
            writer = new OutputStreamWriter(output, encoding);
        }
    }

    /**
     * 格式化并发布一个 <tt>LogRecord</tt>。
     * <p>
     * <tt>StreamHandler</tt> 首先检查是否有 <tt>OutputStream</tt>，以及给定的 <tt>LogRecord</tt> 是否至少具有所需的日志级别。
     * 如果没有，它将静默返回。如果有，它会调用任何关联的
     * <tt>Filter</tt> 检查记录是否应发布。如果是，它会调用其 <tt>Formatter</tt> 格式化记录，然后将结果写入当前输出流。
     * <p>
     * 如果这是写入给定 <tt>OutputStream</tt> 的第一个 <tt>LogRecord</tt>，则在写入 <tt>LogRecord</tt> 之前，会先写入 <tt>Formatter</tt> 的 "head" 字符串。
     *
     * @param  record  日志事件的描述。null 记录将被静默忽略，不会发布。
     */
    @Override
    public synchronized void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        String msg;
        try {
            msg = getFormatter().format(record);
        } catch (Exception ex) {
            // 我们不希望在这里抛出异常，但我们会将异常报告给任何注册的 ErrorManager。
            reportError(null, ex, ErrorManager.FORMAT_FAILURE);
            return;
        }

        try {
            if (!doneHeader) {
                writer.write(getFormatter().getHead(this));
                doneHeader = true;
            }
            writer.write(msg);
        } catch (Exception ex) {
            // 我们不希望在这里抛出异常，但我们会将异常报告给任何注册的 ErrorManager。
            reportError(null, ex, ErrorManager.WRITE_FAILURE);
        }
    }


    /**
     * 检查此 <tt>Handler</tt> 是否会实际记录给定的 <tt>LogRecord</tt>。
     * <p>
     * 此方法检查 <tt>LogRecord</tt> 是否具有适当的级别，并且是否满足任何 <tt>Filter</tt>。如果尚未分配输出流或 <tt>LogRecord</tt> 为 null，也会返回 false。
     * <p>
     * @param record  一个 <tt>LogRecord</tt>
     * @return 如果 <tt>LogRecord</tt> 会被记录，则返回 true。
     *
     */
    @Override
    public boolean isLoggable(LogRecord record) {
        if (writer == null || record == null) {
            return false;
        }
        return super.isLoggable(record);
    }

    /**
     * 刷新任何缓冲的消息。
     */
    @Override
    public synchronized void flush() {
        if (writer != null) {
            try {
                writer.flush();
            } catch (Exception ex) {
                // 我们不希望在这里抛出异常，但我们会将异常报告给任何注册的 ErrorManager。
                reportError(null, ex, ErrorManager.FLUSH_FAILURE);
            }
        }
    }

    private synchronized void flushAndClose() throws SecurityException {
        checkPermission();
        if (writer != null) {
            try {
                if (!doneHeader) {
                    writer.write(getFormatter().getHead(this));
                    doneHeader = true;
                }
                writer.write(getFormatter().getTail(this));
                writer.flush();
                writer.close();
            } catch (Exception ex) {
                // 我们不希望在这里抛出异常，但我们会将异常报告给任何注册的 ErrorManager。
                reportError(null, ex, ErrorManager.CLOSE_FAILURE);
            }
            writer = null;
            output = null;
        }
    }

    /**
     * 关闭当前输出流。
     * <p>
     * 在关闭流之前，会写入 <tt>Formatter</tt> 的 "tail" 字符串。此外，如果 <tt>Formatter</tt> 的 "head" 字符串尚未写入流，则会在 "tail" 字符串之前写入。
     *
     * @exception  SecurityException  如果存在安全经理，并且调用者没有 <tt>LoggingPermission("control")</tt>。
     */
    @Override
    public synchronized void close() throws SecurityException {
        flushAndClose();
    }
}
