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

import java.io.UnsupportedEncodingException;
/**
 * 一个 <tt>Handler</tt> 对象从 <tt>Logger</tt> 接收日志消息并导出它们。例如，它可以将它们写入控制台，
 * 或写入文件，或发送到网络日志服务，或转发到操作系统日志，等等。
 * <p>
 * 通过执行 <tt>setLevel(Level.OFF)</tt> 可以禁用 <tt>Handler</tt>，通过使用适当的级别执行 <tt>setLevel</tt> 可以重新启用它。
 * <p>
 * <tt>Handler</tt> 类通常使用 <tt>LogManager</tt> 属性来设置 <tt>Handler</tt> 的 <tt>Filter</tt>、<tt>Formatter</tt>
 * 和 <tt>Level</tt> 的默认值。请参阅每个具体 <tt>Handler</tt> 类的特定文档。
 *
 *
 * @since 1.4
 */

public abstract class Handler {
    private static final int offValue = Level.OFF.intValue();
    private final LogManager manager = LogManager.getLogManager();

    // 使用 volatile 以避免同步 getter，这会防止其他线程在执行 publish() 时调用 isLoggable()
    // 另一方面，setter 将同步以排除与更复杂的方法（如 StreamHandler.publish()）的并发执行。
    // 我们不希望在 'publish' 调用的执行过程中 'level' 被其他线程更改。
    private volatile Filter filter;
    private volatile Formatter formatter;
    private volatile Level logLevel = Level.ALL;
    private volatile ErrorManager errorManager = new ErrorManager();
    private volatile String encoding;

    // 包私有支持安全检查。当 sealed 为 true 时，我们检查类的更新。
    boolean sealed = true;

    /**
     * 默认构造函数。生成的 <tt>Handler</tt> 具有 <tt>Level.ALL</tt> 的日志级别，没有 <tt>Formatter</tt>，也没有 <tt>Filter</tt>。
     * 安装了一个默认的 <tt>ErrorManager</tt> 实例作为 <tt>ErrorManager</tt>。
     */
    protected Handler() {
    }

    /**
     * 发布一个 <tt>LogRecord</tt>。
     * <p>
     * 初始日志请求是通过 <tt>Logger</tt> 对象进行的，该对象初始化了 <tt>LogRecord</tt> 并将其转发到这里。
     * <p>
     * <tt>Handler</tt> 负责在必要时格式化消息。格式化应包括本地化。
     *
     * @param  record  日志事件的描述。如果记录为 null，则会被静默忽略且不发布。
     */
    public abstract void publish(LogRecord record);

    /**
     * 刷新任何缓冲的输出。
     */
    public abstract void flush();

    /**
     * 关闭 <tt>Handler</tt> 并释放所有相关资源。
     * <p>
     * close 方法将执行 <tt>flush</tt>，然后关闭 <tt>Handler</tt>。调用 close 后，此 <tt>Handler</tt> 不应再使用。
     * 方法调用可能会被静默忽略或抛出运行时异常。
     *
     * @exception  SecurityException  如果存在安全经理，并且调用者没有 <tt>LoggingPermission("control")</tt> 权限。
     */
    public abstract void close() throws SecurityException;

    /**
     * 设置一个 <tt>Formatter</tt>。此 <tt>Formatter</tt> 将用于格式化此 <tt>Handler</tt> 的 <tt>LogRecords</tt>。
     * <p>
     * 一些 <tt>Handlers</tt> 可能不使用 <tt>Formatters</tt>，在这种情况下，<tt>Formatter</tt> 将被记住，但不会使用。
     * <p>
     * @param newFormatter 要使用的 <tt>Formatter</tt>（不能为空）
     * @exception  SecurityException  如果存在安全经理，并且调用者没有 <tt>LoggingPermission("control")</tt> 权限。
     */
    public synchronized void setFormatter(Formatter newFormatter) throws SecurityException {
        checkPermission();
        // 检查空指针：
        newFormatter.getClass();
        formatter = newFormatter;
    }

    /**
     * 返回此 <tt>Handler</tt> 的 <tt>Formatter</tt>。
     * @return <tt>Formatter</tt>（可能为 null）。
     */
    public Formatter getFormatter() {
        return formatter;
    }

    /**
     * 设置此 <tt>Handler</tt> 使用的字符编码。
     * <p>
     * 应在任何 <tt>LogRecords</tt> 写入 <tt>Handler</tt> 之前设置编码。
     *
     * @param encoding  支持的字符编码名称。
     *        可以为 null，表示使用默认平台编码。
     * @exception  SecurityException  如果存在安全经理，并且调用者没有 <tt>LoggingPermission("control")</tt> 权限。
     * @exception  UnsupportedEncodingException 如果命名的编码不受支持。
     */
    public synchronized void setEncoding(String encoding)
                        throws SecurityException, java.io.UnsupportedEncodingException {
        checkPermission();
        if (encoding != null) {
            try {
                if(!java.nio.charset.Charset.isSupported(encoding)) {
                    throw new UnsupportedEncodingException(encoding);
                }
            } catch (java.nio.charset.IllegalCharsetNameException e) {
                throw new UnsupportedEncodingException(encoding);
            }
        }
        this.encoding = encoding;
    }

    /**
     * 返回此 <tt>Handler</tt> 的字符编码。
     *
     * @return  编码名称。可能为 null，表示使用默认编码。
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * 设置一个 <tt>Filter</tt> 以控制此 <tt>Handler</tt> 的输出。
     * <P>
     * 对于每次 <tt>publish</tt> 调用，<tt>Handler</tt> 将调用此 <tt>Filter</tt>（如果它不为 null）以检查
     * <tt>LogRecord</tt> 是否应发布或丢弃。
     *
     * @param   newFilter  <tt>Filter</tt> 对象（可能为 null）
     * @exception  SecurityException  如果存在安全经理，并且调用者没有 <tt>LoggingPermission("control")</tt> 权限。
     */
    public synchronized void setFilter(Filter newFilter) throws SecurityException {
        checkPermission();
        filter = newFilter;
    }

    /**
     * 获取此 <tt>Handler</tt> 的当前 <tt>Filter</tt>。
     *
     * @return  <tt>Filter</tt> 对象（可能为 null）
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * 为此 Handler 定义一个 ErrorManager。
     * <p>
     * 如果在使用此 Handler 时发生任何错误，将调用 ErrorManager 的 "error" 方法。
     *
     * @param em  新的 ErrorManager
     * @exception  SecurityException  如果存在安全经理，并且调用者没有 <tt>LoggingPermission("control")</tt> 权限。
     */
    public synchronized void setErrorManager(ErrorManager em) {
        checkPermission();
        if (em == null) {
           throw new NullPointerException();
        }
        errorManager = em;
    }

    /**
     * 检索此 Handler 的 ErrorManager。
     *
     * @return 此 Handler 的 ErrorManager
     * @exception  SecurityException  如果存在安全经理，并且调用者没有 <tt>LoggingPermission("control")</tt> 权限。
     */
    public ErrorManager getErrorManager() {
        checkPermission();
        return errorManager;
    }

   /**
     * 受保护的便捷方法，用于向此 Handler 的 ErrorManager 报告错误。请注意，此方法检索并使用 ErrorManager
     * 而不执行安全检查。因此，它可以在调用者可能没有特权的环境中使用。
     *
     * @param msg    描述性字符串（可能为 null）
     * @param ex     异常（可能为 null）
     * @param code   ErrorManager 中定义的错误代码
     */
    protected void reportError(String msg, Exception ex, int code) {
        try {
            errorManager.error(msg, ex, code);
        } catch (Exception ex2) {
            System.err.println("Handler.reportError caught:");
            ex2.printStackTrace();
        }
    }

    /**
     * 设置日志级别，指定此 <tt>Handler</tt> 将记录哪些消息级别。低于此值的消息级别将被丢弃。
     * <p>
     * 目的是允许开发人员打开大量日志记录，但限制发送到某些 <tt>Handlers</tt> 的消息。
     *
     * @param newLevel   日志级别的新值
     * @exception  SecurityException  如果存在安全经理，并且调用者没有 <tt>LoggingPermission("control")</tt> 权限。
     */
    public synchronized void setLevel(Level newLevel) throws SecurityException {
        if (newLevel == null) {
            throw new NullPointerException();
        }
        checkPermission();
        logLevel = newLevel;
    }

    /**
     * 获取日志级别，指定此 <tt>Handler</tt> 将记录哪些消息。低于此级别的消息将被丢弃。
     * @return  正在记录的消息级别。
     */
    public Level getLevel() {
        return logLevel;
    }

    /**
     * 检查此 <tt>Handler</tt> 是否会实际记录给定的 <tt>LogRecord</tt>。
     * <p>
     * 此方法检查 <tt>LogRecord</tt> 是否具有适当的 <tt>Level</tt> 以及是否满足任何 <tt>Filter</tt>。
     * 它还可以进行其他特定于 <tt>Handler</tt> 的检查，这些检查可能会阻止 Handler 记录 <tt>LogRecord</tt>。如果 <tt>LogRecord</tt> 为 null，则返回 false。
     * <p>
     * @param record  <tt>LogRecord</tt>
     * @return 如果 <tt>LogRecord</tt> 会被记录，则返回 true。
     *
     */
    public boolean isLoggable(LogRecord record) {
        final int levelValue = getLevel().intValue();
        if (record.getLevel().intValue() < levelValue || levelValue == offValue) {
            return false;
        }
        final Filter filter = getFilter();
        if (filter == null) {
            return true;
        }
        return filter.isLoggable(record);
    }

    // 包私有支持方法，用于安全检查。
    // 如果 "sealed" 为 true，我们检查调用者是否有更新 Handler 状态的适当安全权限，如果没有则抛出 SecurityException。
    void checkPermission() throws SecurityException {
        if (sealed) {
            manager.checkPermission();
        }
    }
}
