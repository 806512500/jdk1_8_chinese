/*
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 一个格式化器提供支持以格式化 LogRecord。
 * <p>
 * 通常每个日志处理程序都会有一个与之关联的格式化器。格式化器接收一个 LogRecord 并将其转换为字符串。
 * <p>
 * 一些格式化器（如 XMLFormatter）需要在一组格式化记录周围包装头和尾字符串。可以使用 getHeader
 * 和 getTail 方法来获取这些字符串。
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
     * 生成的格式化字符串通常包括 LogRecord 的消息字段的本地化和格式化版本。
     * 建议使用 {@link Formatter#formatMessage} 便捷方法来本地化和格式化消息字段。
     *
     * @param record 要格式化的日志记录。
     * @return 格式化的日志记录
     */
    public abstract String format(LogRecord record);


    /**
     * 返回一组格式化记录的头字符串。
     * <p>
     * 该基类返回一个空字符串，但子类可以重写此方法。
     *
     * @param   h  目标处理程序（可以为 null）
     * @return  头字符串
     */
    public String getHead(Handler h) {
        return "";
    }

    /**
     * 返回一组格式化记录的尾字符串。
     * <p>
     * 该基类返回一个空字符串，但子类可以重写此方法。
     *
     * @param   h  目标处理程序（可以为 null）
     * @return  尾字符串
     */
    public String getTail(Handler h) {
        return "";
    }


    /**
     * 本地化并格式化日志记录中的消息字符串。此方法作为便捷方法提供，供格式化器子类在执行格式化时使用。
     * <p>
     * 消息字符串首先使用记录的 ResourceBundle 进行本地化为格式字符串。（如果没有 ResourceBundle，
     * 或者找不到消息键，则使用键作为格式字符串。）格式字符串使用 java.text 风格的格式化。
     * <ul>
     * <li>如果没有参数，则不使用格式化器。
     * <li>否则，如果字符串包含 "{0"，则使用 java.text.MessageFormat 对字符串进行格式化。
     * <li>否则不进行格式化。
     * </ul>
     * <p>
     *
     * @param  record  包含原始消息的日志记录
     * @return   本地化并格式化的消息
     */
    public synchronized String formatMessage(LogRecord record) {
        String format = record.getMessage();
        java.util.ResourceBundle catalog = record.getResourceBundle();
        if (catalog != null) {
            try {
                format = catalog.getString(record.getMessage());
            } catch (java.util.MissingResourceException ex) {
                // 未找到资源，使用记录消息作为格式
                format = record.getMessage();
            }
        }
        // 执行格式化。
        try {
            Object parameters[] = record.getParameters();
            if (parameters == null || parameters.length == 0) {
                // 没有参数，直接返回格式字符串。
                return format;
            }
            // 是否为 java.text 风格的格式？
            // 理想情况下，我们可以使用 Pattern.compile("\\{\\d").matcher(format).find()
            // 但是成本高 14%，所以我们简单地检查前 4 个参数
            if (format.indexOf("{0") >= 0 || format.indexOf("{1") >=0 ||
                        format.indexOf("{2") >=0|| format.indexOf("{3") >=0) {
                return java.text.MessageFormat.format(format, parameters);
            }
            return format;

        } catch (Exception ex) {
            // 格式化失败：使用本地化格式字符串。
            return format;
        }
    }
}
