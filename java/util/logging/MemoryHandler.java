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
 * <tt>Handler</tt> 用于在内存中缓冲请求的循环缓冲区。
 * <p>
 * 通常，此 <tt>Handler</tt> 仅将传入的 <tt>LogRecords</tt> 存储在其内存缓冲区中并丢弃较早的记录。这种缓冲非常廉价，避免了格式化的成本。在某些触发条件下，<tt>MemoryHandler</tt> 会将其当前缓冲区的内容推送到目标 <tt>Handler</tt>，后者通常会将这些内容发布到外部世界。
 * <p>
 * 触发缓冲区推送的三种主要模型：
 * <ul>
 * <li>
 * 传入的 <tt>LogRecord</tt> 的类型大于预定义的级别 <tt>pushLevel</tt>。 </li>
 * <li>
 * 外部类显式调用 <tt>push</tt> 方法。 </li>
 * <li>
 * 子类重写 <tt>log</tt> 方法并扫描每个传入的 <tt>LogRecord</tt>，如果记录符合某些标准，则调用 <tt>push</tt>。 </li>
 * </ul>
 * <p>
 * <b>配置：</b>
 * 默认情况下，每个 <tt>MemoryHandler</tt> 使用以下 <tt>LogManager</tt> 配置属性进行初始化，其中 <tt>&lt;handler-name&gt;</tt> 指的是处理程序的完全限定类名。
 * 如果属性未定义
 * （或具有无效值）则使用指定的默认值。
 * 如果未定义默认值，则抛出 <tt>RuntimeException</tt>。
 * <ul>
 * <li>   &lt;handler-name&gt;.level
 *        指定 <tt>Handler</tt> 的级别
 *        （默认为 <tt>Level.ALL</tt>）。 </li>
 * <li>   &lt;handler-name&gt;.filter
 *        指定要使用的 <tt>Filter</tt> 类的名称
 *        （默认为不使用 <tt>Filter</tt>）。 </li>
 * <li>   &lt;handler-name&gt;.size
 *        定义缓冲区大小（默认为 1000）。 </li>
 * <li>   &lt;handler-name&gt;.push
 *        定义 <tt>pushLevel</tt>（默认为 <tt>level.SEVERE</tt>）。 </li>
 * <li>   &lt;handler-name&gt;.target
 *        指定目标 <tt>Handler </tt> 类的名称。
 *        （没有默认值）。 </li>
 * </ul>
 * <p>
 * 例如，<tt>MemoryHandler</tt> 的属性为：
 * <ul>
 * <li>   java.util.logging.MemoryHandler.level=INFO </li>
 * <li>   java.util.logging.MemoryHandler.formatter=java.util.logging.SimpleFormatter </li>
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

public class MemoryHandler extends Handler {
    private final static int DEFAULT_SIZE = 1000;
    private volatile Level pushLevel;
    private int size;
    private Handler target;
    private LogRecord buffer[];
    int start, count;

    // 私有方法，从 LogManager 属性和/或类
    // javadoc 中指定的默认值配置 MemoryHandler。
    private void configure() {
        LogManager manager = LogManager.getLogManager();
        String cname = getClass().getName();

        pushLevel = manager.getLevelProperty(cname +".push", Level.SEVERE);
        size = manager.getIntProperty(cname + ".size", DEFAULT_SIZE);
        if (size <= 0) {
            size = DEFAULT_SIZE;
        }
        setLevel(manager.getLevelProperty(cname +".level", Level.ALL));
        setFilter(manager.getFilterProperty(cname +".filter", null));
        setFormatter(manager.getFormatterProperty(cname +".formatter", new SimpleFormatter()));
    }

    /**
     * 创建一个 <tt>MemoryHandler</tt> 并根据
     * <tt>LogManager</tt> 配置属性进行配置。
     */
    public MemoryHandler() {
        sealed = false;
        configure();
        sealed = true;

        LogManager manager = LogManager.getLogManager();
        String handlerName = getClass().getName();
        String targetName = manager.getProperty(handlerName+".target");
        if (targetName == null) {
            throw new RuntimeException("The handler " + handlerName
                    + " does not specify a target");
        }
        Class<?> clz;
        try {
            clz = ClassLoader.getSystemClassLoader().loadClass(targetName);
            target = (Handler) clz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("MemoryHandler can't load handler target \"" + targetName + "\"" , e);
        }
        init();
    }

    // 初始化。Size 是 LogRecords 的数量。
    private void init() {
        buffer = new LogRecord[size];
        start = 0;
        count = 0;
    }

    /**
     * 创建一个 <tt>MemoryHandler</tt>。
     * <p>
     * <tt>MemoryHandler</tt> 根据 <tt>LogManager</tt>
     * 属性（或其默认值）进行配置，但使用给定的 <tt>pushLevel</tt>
     * 参数和缓冲区大小参数。
     *
     * @param target  要发布输出的目标 <tt>Handler</tt>。
     * @param size    要缓冲的日志记录数（必须大于零）
     * @param pushLevel  推送消息的级别
     *
     * @throws IllegalArgumentException 如果 {@code size is <= 0}
     */
    public MemoryHandler(Handler target, int size, Level pushLevel) {
        if (target == null || pushLevel == null) {
            throw new NullPointerException();
        }
        if (size <= 0) {
            throw new IllegalArgumentException();
        }
        sealed = false;
        configure();
        sealed = true;
        this.target = target;
        this.pushLevel = pushLevel;
        this.size = size;
        init();
    }

    /**
     * 在内部缓冲区中存储一个 <tt>LogRecord</tt>。
     * <p>
     * 如果存在 <tt>Filter</tt>，则调用其 <tt>isLoggable</tt>
     * 方法检查给定的日志记录是否可记录。
     * 如果不可记录，则返回。否则，将给定的记录复制到
     * 内部循环缓冲区中。然后将记录的级别属性与 <tt>pushLevel</tt>
     * 进行比较。如果给定的级别大于或等于 <tt>pushLevel</tt>，
     * 则调用 <tt>push</tt> 将所有缓冲的记录写入目标输出
     * <tt>Handler</tt>。
     *
     * @param  record  日志事件的描述。如果记录为 null，则
     *                 静默忽略且不发布
     */
    @Override
    public synchronized void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        int ix = (start+count)%buffer.length;
        buffer[ix] = record;
        if (count < buffer.length) {
            count++;
        } else {
            start++;
            start %= buffer.length;
        }
        if (record.getLevel().intValue() >= pushLevel.intValue()) {
            push();
        }
    }

    /**
     * 将任何缓冲的输出推送到目标 <tt>Handler</tt>。
     * <p>
     * 然后清空缓冲区。
     */
    public synchronized void push() {
        for (int i = 0; i < count; i++) {
            int ix = (start+i)%buffer.length;
            LogRecord record = buffer[ix];
            target.publish(record);
        }
        // 清空缓冲区。
        start = 0;
        count = 0;
    }

    /**
     * 在目标 <tt>Handler</tt> 上执行刷新。
     * <p>
     * 注意，<tt>MemoryHandler</tt>
     * 缓冲区的当前内容 <b>不会</b> 被写入。这需要一个 "push"。
     */
    @Override
    public void flush() {
        target.flush();
    }

    /**
     * 关闭 <tt>Handler</tt> 并释放所有相关资源。
     * 这也将关闭目标 <tt>Handler</tt>。
     *
     * @exception  SecurityException  如果存在安全经理且
     *             调用者没有 <tt>LoggingPermission("control")</tt>。
     */
    @Override
    public void close() throws SecurityException {
        target.close();
        setLevel(Level.OFF);
    }

    /**
     * 设置 <tt>pushLevel</tt>。在将 <tt>LogRecord</tt> 复制
     * 到内部缓冲区后，如果其级别大于或等于 <tt>pushLevel</tt>，
     * 则调用 <tt>push</tt>。
     *
     * @param newLevel <tt>pushLevel</tt> 的新值
     * @exception  SecurityException  如果存在安全经理且
     *             调用者没有 <tt>LoggingPermission("control")</tt>。
     */
    public synchronized void setPushLevel(Level newLevel) throws SecurityException {
        if (newLevel == null) {
            throw new NullPointerException();
        }
        checkPermission();
        pushLevel = newLevel;
    }

    /**
     * 获取 <tt>pushLevel</tt>。
     *
     * @return <tt>pushLevel</tt> 的值
     */
    public Level getPushLevel() {
        return pushLevel;
    }

    /**
     * 检查此 <tt>Handler</tt> 是否会将给定的
     * <tt>LogRecord</tt> 记录到其内部缓冲区中。
     * <p>
     * 此方法检查 <tt>LogRecord</tt> 是否具有适当的级别以及
     * 是否满足任何 <tt>Filter</tt>。但是它 <b>不会</b>
     * 检查 <tt>LogRecord</tt> 是否会导致缓冲区内容的 "push"。
     * 如果 <tt>LogRecord</tt> 为 null，则返回 false。
     * <p>
     * @param record  一个 <tt>LogRecord</tt>
     * @return 如果 <tt>LogRecord</tt> 会被记录，则返回 true。
     *
     */
    @Override
    public boolean isLoggable(LogRecord record) {
        return super.isLoggable(record);
    }
}
