
/*
 * Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved.
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


package java.util.logging;

import java.io.*;
import java.text.*;
import java.util.Date;
import sun.util.logging.LoggingSupport;

/**
 * 以人类可读的格式简要打印 {@code LogRecord} 的摘要。摘要通常为1到2行。
 *
 * <p>
 * <a name="formatting">
 * <b>配置：</b></a>
 * {@code SimpleFormatter} 使用在 {@code java.util.logging.SimpleFormatter.format}
 * 属性中指定的 <a href="../Formatter.html#syntax">格式字符串</a> 初始化，以 {@linkplain #format 格式化} 日志消息。
 * 此属性可以在 {@linkplain LogManager#getProperty 日志属性} 配置文件中定义
 * 或作为系统属性定义。如果此属性在日志属性和系统属性中都已设置，
 * 则使用系统属性中指定的格式字符串。
 * 如果未定义此属性或给定的格式字符串 {@linkplain java.util.IllegalFormatException 不合法}，
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
     * 给定的 {@code LogRecord} 将被格式化，如同调用了：
     * <pre>
     *    {@link String#format String.format}(format, date, source, logger, level, message, thrown);
     * </pre>
     * 其中参数为：<br>
     * <ol>
     * <li>{@code format} - 在 {@code java.util.logging.SimpleFormatter.format} 属性中指定的
     *     {@link java.util.Formatter java.util.Formatter} 格式字符串，或默认格式。</li>
     * <li>{@code date} - 表示日志记录 {@linkplain LogRecord#getMillis 事件时间} 的 {@link Date} 对象。</li>
     * <li>{@code source} - 如果可用，表示调用者的字符串；否则，为日志记录器的名称。</li>
     * <li>{@code logger} - 日志记录器的名称。</li>
     * <li>{@code level} - 日志级别，由 {@linkplain Level#getLocalizedName 获取}。</li>
     * <li>{@code message} - 由 {@link Formatter#formatMessage(LogRecord)} 方法返回的格式化日志消息。
     *     它使用 {@link java.text.MessageFormat java.text} 格式化，不使用 {@code java.util.Formatter
     *     格式} 参数。</li>
     * <li>{@code thrown} - 与日志记录关联的 {@linkplain LogRecord#getThrown 可抛出对象} 及其回溯，
     *     以换行符开头，如果有；否则，为空字符串。</li>
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
     *     <p>这将打印两行，第一行包含时间戳 ({@code 1$}) 和来源 ({@code 2$})；
     *     第二行包含日志级别 ({@code 4$}) 和日志消息 ({@code 5$})，后跟可抛出对象及其回溯 ({@code 6$})，如果有：
     *     <pre>
     *     Tue Mar 22 13:11:31 PDT 2011 MyClass fatal
     *     SEVERE: several message with an exception
     *     java.lang.IllegalArgumentException: invalid argument
     *             at MyClass.mash(MyClass.java:9)
     *             at MyClass.crunch(MyClass.java:6)
     *             at MyClass.main(MyClass.java:3)
     *     </pre></li>
     * <li> {@code java.util.logging.SimpleFormatter.format="%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp %2$s%n%4$s: %5$s%n"}
     *      <p>这将打印两行，类似于上面的示例，但使用不同的日期/时间格式，不打印可抛出对象及其回溯：
     *     <pre>
     *     Mar 22, 2011 1:11:31 PM MyClass fatal
     *     SEVERE: several message with an exception
     *     </pre></li>
     * </ul>
     * <p>此方法也可以在子类中重写。
     * 建议使用 {@link Formatter#formatMessage} 便捷方法来本地化和格式化消息字段。
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
