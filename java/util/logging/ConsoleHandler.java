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

/**
 * 此 <tt>Handler</tt> 将日志记录发布到 <tt>System.err</tt>。
 * 默认情况下，使用 <tt>SimpleFormatter</tt> 生成简短摘要。
 * <p>
 * <b>配置：</b>
 * 默认情况下，每个 <tt>ConsoleHandler</tt> 使用以下 <tt>LogManager</tt> 配置属性进行初始化，其中 {@code <handler-name>}
 * 指的是处理程序的完全限定类名。
 * 如果属性未定义
 * （或具有无效值），则使用指定的默认值。
 * <ul>
 * <li>   &lt;handler-name&gt;.level
 *        指定 <tt>Handler</tt> 的默认级别
 *        （默认为 <tt>Level.INFO</tt>）。 </li>
 * <li>   &lt;handler-name&gt;.filter
 *        指定要使用的 <tt>Filter</tt> 类的名称
 *        （默认为不使用 <tt>Filter</tt>）。 </li>
 * <li>   &lt;handler-name&gt;.formatter
 *        指定要使用的 <tt>Formatter</tt> 类的名称
 *        （默认为 <tt>java.util.logging.SimpleFormatter</tt>）。 </li>
 * <li>   &lt;handler-name&gt;.encoding
 *        要使用的字符集编码的名称（默认为
 *        平台默认编码）。 </li>
 * </ul>
 * <p>
 * 例如，{@code ConsoleHandler} 的属性为：
 * <ul>
 * <li>   java.util.logging.ConsoleHandler.level=INFO </li>
 * <li>   java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter </li>
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
public class ConsoleHandler extends StreamHandler {
    // 私有方法，用于从 LogManager 属性和/或类
    // javadoc 中指定的默认值配置 ConsoleHandler。
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
                // 使用 null 进行 setEncoding 应该总是有效的。
                // assert false;
            }
        }
    }

    /**
     * 为 <tt>System.err</tt> 创建一个 <tt>ConsoleHandler</tt>。
     * <p>
     * <tt>ConsoleHandler</tt> 基于
     * <tt>LogManager</tt> 属性（或其默认值）进行配置。
     *
     */
    public ConsoleHandler() {
        sealed = false;
        configure();
        setOutputStream(System.err);
        sealed = true;
    }

    /**
     * 发布一个 <tt>LogRecord</tt>。
     * <p>
     * 初始日志请求是向 <tt>Logger</tt> 对象发出的，
     * 该对象初始化了 <tt>LogRecord</tt> 并将其转发到这里。
     * <p>
     * @param  record  日志事件的描述。如果记录为 null，
     *                 则会被静默忽略且不会发布
     */
    @Override
    public void publish(LogRecord record) {
        super.publish(record);
        flush();
    }

    /**
     * 覆盖 <tt>StreamHandler.close</tt> 以执行刷新但不
     * 关闭输出流。也就是说，我们 <b>不</b>
     * 关闭 <tt>System.err</tt>。
     */
    @Override
    public void close() {
        flush();
    }
}
