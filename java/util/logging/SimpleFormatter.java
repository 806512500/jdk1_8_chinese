/*
 * Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved.
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
import java.text.*;
import java.util.Date;
import sun.util.logging.LoggingSupport;

/**
 * 以人类可读的格式简要打印 {@code LogRecord}。通常为 1 或 2 行。
 *
 * <p>
 * <a name="formatting">
 * <b>配置：</b></a>
 * {@code SimpleFormatter} 使用在 {@code java.util.logging.SimpleFormatter.format}
 * 属性中指定的 <a href="../Formatter.html#syntax">格式字符串</a> 初始化，以 {@linkplain #format 格式化} 日志消息。
 * 此属性可以在 {@linkplain LogManager#getProperty 日志属性} 配置文件中定义，也可以作为系统属性定义。
 * 如果此属性在日志属性和系统属性中都已定义，则使用系统属性中指定的格式字符串。
 * 如果未定义此属性或给定的格式字符串 {@linkplain java.util.IllegalFormatException 非法}，
 * 则默认格式由实现决定。
 *
 * @since 1.4
 * @see java.util.Formatter
 */

public class SimpleFormatter extends Formatter {

    // 用于打印日志记录的格式字符串
    private static final String format = LoggingSupport.getSimpleFormat();
    private final Date dat = new Date();

    /**
     * 格式化给定的 LogRecord。
     * <p>
     * 可以通过在 <a href="#formatting">
     * {@code java.util.logging.SimpleFormatter.format}</a> 属性中指定
     * <a href="../Formatter.html#syntax">格式字符串</a> 来自定义格式化。
     * 给定的 {@code LogRecord} 将通过以下方式格式化：
     * <pre>
     *    {@link String#format String.format}(format, date, source, logger, level, message, thrown);
     * </pre>
     * 其中参数为：<br>
     * <ol>
     * <li>{@code format} - 在 {@code java.util.logging.SimpleFormatter.format} 属性中指定的
     *     {@link java.util.Formatter java.util.Formatter} 格式字符串，或默认格式。</li>
     * <li>{@code date} - 表示日志记录 {@linkplain LogRecord#getMillis 事件时间} 的 {@link Date} 对象。</li>
     * <li>{@code source} - 如果可用，则表示调用者；否则，为记录器的名称。</li>
     * <li>{@code logger} - 记录器的名称。</li>
     * <li>{@code level} - {@linkplain Level#getLocalizedName 日志级别}。</li>
     * <li>{@code message} - 从 {@link Formatter#formatMessage(LogRecord)} 方法返回的格式化日志消息。
     *     它使用 {@link java.text.MessageFormat java.text} 格式化，不使用 {@code java.util.Formatter 格式} 参数。</li>
     * <li>{@code thrown} - 以换行符开头的字符串，表示与日志记录关联的 {@linkplain LogRecord#getThrown 可抛出对象}
     *     及其回溯信息，如果有的话；否则，为空字符串。</li>
     * </ol>
     *
     * <p>一些示例格式：<br>
     * <ul>
     * <li> {@code java.util.logging.SimpleFormatter.format="%4$s: %5$s [%1$tc]%n"}
     *     <p>这将打印一行，包含日志级别 ({@code 4$})、日志消息 ({@code 5$}) 和带方括号的时间戳 ({@code 1$})。
     *     <pre>
     *     WARNING: warning message [Tue Mar 22 13:11:31 PDT 2011]
     *     </pre></li>
     * <li> {@code java.util.logging.SimpleFormatter.format="%1$tc %2$s%n%4$s: %5$s%6$s%n"}
     *     <p>这将打印两行，第一行包含时间戳 ({@code 1$}) 和调用者 ({@code 2$})；
     *     第二行包含日志级别 ({@code 4$}) 和日志消息 ({@code 5$})，后面跟着可抛出对象及其回溯信息 ({@code 6$})，如果有的话：
     *     <pre>
     *     Tue Mar 22 13:11:31 PDT 2011 MyClass fatal
     *     SEVERE: several message with an exception
     *     java.lang.IllegalArgumentException: invalid argument
     *             at MyClass.mash(MyClass.java:9)
     *             at MyClass.crunch(MyClass.java:6)
     *             at MyClass.main(MyClass.java:3)
     *     </pre></li>
     * <li> {@code java.util.logging.SimpleFormatter.format="%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp %2$s%n%4$s: %5$s%n"}
     *      <p>这将打印两行，类似于上面的示例，但使用不同的日期/时间格式，不打印可抛出对象及其回溯信息：
     *     <pre>
     *     Mar 22, 2011 1:11:31 PM MyClass fatal
     *     SEVERE: several message with an exception
     *     </pre></li>
     * </ul>
     * <p>此方法也可以在子类中重写。
     * 建议使用 {@link Formatter#formatMessage} 方便方法来本地化和格式化消息字段。
     *
     * @param record 要格式化的日志记录。
     * @return 格式化的日志记录
     */
    public synchronized String format(LogRecord record) {
        dat.setTime(record.getMillis());
        String source;
        if (record.getSourceClassName() != null) {
            source = record.getSourceClassName();
            if (record.getSourceMethodName() != null) {
               source += " " + record.getSourceMethodName();
            }
        } else {
            source = record.getLoggerName();
        }
        String message = formatMessage(record);
        String throwable = "";
        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            record.getThrown().printStackTrace(pw);
            pw.close();
            throwable = sw.toString();
        }
        return String.format(format,
                             dat,
                             source,
                             record.getLoggerName(),
                             record.getLevel().getLocalizedLevelName(),
                             message,
                             throwable);
    }
}
