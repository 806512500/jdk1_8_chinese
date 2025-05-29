/*
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 一个Formatter提供了对LogRecord的格式化支持。
 * <p>
 * 通常每个日志处理程序（Handler）都会有一个与之关联的Formatter。Formatter接收一个LogRecord并将其转换为字符串。
 * <p>
 * 一些格式化器（如XMLFormatter）需要在一组格式化的记录周围包装头部和尾部字符串。可以使用getHeader和getTail方法来获取这些字符串。
 *
 * @since 1.4
 */

public abstract class Formatter {

    /**
     * 构造一个新的格式化器。
     */
    protected Formatter() {
    }

    /**
     * 格式化给定的日志记录并返回格式化的字符串。
     * <p>
     * 生成的格式化字符串通常会包含LogRecord消息字段的本地化和格式化版本。建议使用{@link Formatter#formatMessage}
     * 方法来本地化和格式化消息字段。
     *
     * @param record 要格式化的日志记录。
     * @return 格式化的日志记录
     */
    public abstract String format(LogRecord record);


    /**
     * 返回一组格式化记录的头部字符串。
     * <p>
     * 该基类返回一个空字符串，但子类可以重写此方法。
     *
     * @param   h  目标处理程序（可以为null）
     * @return  头部字符串
     */
    public String getHead(Handler h) {
        return "";
    }

    /**
     * 返回一组格式化记录的尾部字符串。
     * <p>
     * 该基类返回一个空字符串，但子类可以重写此方法。
     *
     * @param   h  目标处理程序（可以为null）
     * @return  尾部字符串
     */
    public String getTail(Handler h) {
        return "";
    }


    /**
     * 本地化并格式化来自日志记录的消息字符串。此方法作为Formatter子类在执行格式化时使用的便捷方法提供。
     * <p>
     * 消息字符串首先使用记录的ResourceBundle本地化为格式字符串。（如果没有ResourceBundle，或者找不到消息键，
     * 则使用键作为格式字符串。）格式字符串使用java.text风格的格式化。
     * <ul>
     * <li>如果没有参数，则不使用格式化器。
     * <li>否则，如果字符串包含"{0"，则使用java.text.MessageFormat来格式化字符串。
     * <li>否则不执行格式化。
     * </ul>
     * <p>
     *
     * @param  record  包含原始消息的日志记录
     * @return   本地化和格式化后的消息
     */
    public synchronized String formatMessage(LogRecord record) {
        String format = record.getMessage();
        java.util.ResourceBundle catalog = record.getResourceBundle();
        if (catalog != null) {
            try {
                format = catalog.getString(record.getMessage());
            } catch (java.util.MissingResourceException ex) {
                // 无法找到资源，使用记录消息作为格式
                format = record.getMessage();
            }
        }
        // 执行格式化。
        try {
            Object parameters[] = record.getParameters();
            if (parameters == null || parameters.length == 0) {
                // 没有参数。直接返回格式字符串。
                return format;
            }
            // 是否是java.text风格的格式？
            // 理想情况下，可以使用Pattern.compile("\\{\\d").matcher(format).find()匹配
            // 但是成本高14%，因此我们简单地检查前4个参数
            if (format.indexOf("{0") >= 0 || format.indexOf("{1") >=0 ||
                        format.indexOf("{2") >=0|| format.indexOf("{3") >=0) {
                return java.text.MessageFormat.format(format, parameters);
            }
            return format;

        } catch (Exception ex) {
            // 格式化失败：使用本地化的格式字符串。
            return format;
        }
    }
}
