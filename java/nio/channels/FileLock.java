/*
 * Copyright (c) 2001, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.channels;

import java.io.IOException;

/**
 * 一个表示文件区域锁定的令牌。
 *
 * <p> 每次通过 {@link FileChannel#lock(long,long,boolean) lock} 或 {@link
 * FileChannel#tryLock(long,long,boolean) tryLock} 方法在文件上获取锁时，都会创建一个文件锁对象。
 * 同样，通过 {@link AsynchronousFileChannel#lock(long,long,boolean,Object,CompletionHandler) lock}
 * 或 {@link AsynchronousFileChannel#tryLock(long,long,boolean) tryLock} 方法在文件上获取锁时，也会创建一个文件锁对象。
 *
 * <p> 文件锁对象最初是有效的。它在以下情况之一发生之前一直有效：调用 {@link #release release} 方法释放锁，
 * 关闭用于获取锁的通道，或 Java 虚拟机终止。可以通过调用其 {@link #isValid isValid} 方法来测试锁的有效性。
 *
 * <p> 文件锁可以是 <i>独占的</i> 或 <i>共享的</i>。共享锁防止其他并发运行的程序获取重叠的独占锁，但允许它们获取重叠的共享锁。
 * 独占锁防止其他程序获取任何形式的重叠锁。一旦释放，锁将不再影响其他程序可以获取的锁。
 *
 * <p> 可以通过调用其 {@link #isShared isShared} 方法来确定锁是独占的还是共享的。某些平台不支持共享锁，
 * 在这种情况下，共享锁的请求将自动转换为独占锁的请求。
 *
 * <p> 单个 Java 虚拟机在特定文件上持有的锁不会重叠。可以使用 {@link #overlaps overlaps} 方法来测试候选锁范围是否与现有锁重叠。
 *
 * <p> 文件锁对象记录了持有锁的文件通道、锁的类型和有效性以及锁定区域的位置和大小。锁的有效性是唯一会随时间变化的属性；
 * 锁状态的所有其他方面都是不可变的。
 *
 * <p> 文件锁代表整个 Java 虚拟机持有。它们不适合用于控制同一虚拟机内多个线程对文件的访问。
 *
 * <p> 文件锁对象可以安全地由多个并发线程使用。
 *
 *
 * <a name="pdep"></a><h2> 平台依赖性 </h2>
 *
 * <p> 本文件锁定 API 旨在直接映射到底层操作系统的本机锁定设施。因此，对文件持有的锁应对其具有访问权限的所有程序可见，
 * 无论这些程序是用什么语言编写的。
 *
 * <p> 锁是否实际防止其他程序访问锁定区域的内容是系统依赖的，因此未指定。某些系统的本机文件锁定设施仅仅是 <i>建议性的</i>，
 * 意味着程序必须合作遵守已知的锁定协议，以保证数据完整性。在其他系统上，本机文件锁是 <i>强制性的</i>，
 * 意味着如果一个程序锁定文件的某个区域，则其他程序实际上会被阻止以违反锁的方式访问该区域。在某些系统上，
 * 本机文件锁是建议性的还是强制性的可以针对每个文件进行配置。为了确保跨平台的一致和正确行为，
 * 强烈建议将本 API 提供的锁视为建议性锁使用。
 *
 * <p> 在某些系统上，获取文件区域的强制锁会阻止该区域被 {@link java.nio.channels.FileChannel#map
 * <i>映射到内存</i>}，反之亦然。程序应准备好这种组合可能会失败。
 *
 * <p> 在某些系统上，关闭通道会释放 Java 虚拟机在底层文件上持有的所有锁，无论这些锁是通过该通道还是通过打开同一文件的其他通道获取的。
 * 强烈建议在一个程序内，使用唯一的通道来获取给定文件上的所有锁。
 *
 * <p> 某些网络文件系统仅在锁定区域与硬件页面大小对齐且是其整数倍时才允许使用文件锁定与内存映射文件。
 * 某些网络文件系统不实现超出特定位置的文件锁，通常是 2<sup>30</sup> 或 2<sup>31</sup>。通常，锁定网络文件系统上的文件时应格外小心。
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 专家小组
 * @since 1.4
 */

public abstract class FileLock implements AutoCloseable {

    private final Channel channel;
    private final long position;
    private final long size;
    private final boolean shared;

    /**
     * 初始化此类的新实例。
     *
     * @param  channel
     *         持有此锁的文件通道
     *
     * @param  position
     *         锁定区域在文件中的起始位置；必须是非负数
     *
     * @param  size
     *         锁定区域的大小；必须是非负数，且 <tt>position</tt>&nbsp;+&nbsp;<tt>size</tt> 的和必须是非负数
     *
     * @param  shared
     *         <tt>true</tt> 表示此锁是共享的，
     *         <tt>false</tt> 表示它是独占的
     *
     * @throws IllegalArgumentException
     *         如果参数的前置条件不成立
     */
    protected FileLock(FileChannel channel,
                       long position, long size, boolean shared)
    {
        if (position < 0)
            throw new IllegalArgumentException("Negative position");
        if (size < 0)
            throw new IllegalArgumentException("Negative size");
        if (position + size < 0)
            throw new IllegalArgumentException("Negative position + size");
        this.channel = channel;
        this.position = position;
        this.size = size;
        this.shared = shared;
    }

    /**
     * 初始化此类的新实例。
     *
     * @param  channel
     *         持有此锁的通道
     *
     * @param  position
     *         锁定区域在文件中的起始位置；必须是非负数
     *
     * @param  size
     *         锁定区域的大小；必须是非负数，且 <tt>position</tt>&nbsp;+&nbsp;<tt>size</tt> 的和必须是非负数
     *
     * @param  shared
     *         <tt>true</tt> 表示此锁是共享的，
     *         <tt>false</tt> 表示它是独占的
     *
     * @throws IllegalArgumentException
     *         如果参数的前置条件不成立
     *
     * @since 1.7
     */
    protected FileLock(AsynchronousFileChannel channel,
                       long position, long size, boolean shared)
    {
        if (position < 0)
            throw new IllegalArgumentException("Negative position");
        if (size < 0)
            throw new IllegalArgumentException("Negative size");
        if (position + size < 0)
            throw new IllegalArgumentException("Negative position + size");
        this.channel = channel;
        this.position = position;
        this.size = size;
        this.shared = shared;
    }

    /**
     * 返回获取此锁的文件通道。
     *
     * <p> 此方法已被 {@link #acquiredBy acquiredBy} 方法取代。
     *
     * @return  文件通道，如果文件锁不是由文件通道获取的，则返回 {@code null}。
     */
    public final FileChannel channel() {
        return (channel instanceof FileChannel) ? (FileChannel)channel : null;
    }

    /**
     * 返回获取此锁的通道。
     *
     * @return  获取此锁的通道。
     *
     * @since 1.7
     */
    public Channel acquiredBy() {
        return channel;
    }

    /**
     * 返回锁定区域的第一个字节在文件中的位置。
     *
     * <p> 锁定区域不必包含在实际的底层文件中，甚至不必与之重叠，因此此方法返回的值可能超过文件的当前大小。 </p>
     *
     * @return  位置
     */
    public final long position() {
        return position;
    }

    /**
     * 返回锁定区域的大小（以字节为单位）。
     *
     * <p> 锁定区域不必包含在实际的底层文件中，甚至不必与之重叠，因此此方法返回的值可能超过文件的当前大小。 </p>
     *
     * @return  锁定区域的大小
     */
    public final long size() {
        return size;
    }

    /**
     * 告诉此锁是否为共享锁。
     *
     * @return <tt>true</tt> 表示锁是共享的，
     *         <tt>false</tt> 表示它是独占的
     */
    public final boolean isShared() {
        return shared;
    }

    /**
     * 告诉此锁是否与给定的锁范围重叠。
     *
     * @param   position
     *          锁定范围的起始位置
     * @param   size
     *          锁定范围的大小
     *
     * @return  <tt>true</tt> 表示且仅表示此锁与给定的锁定范围至少重叠一个字节
     */
    public final boolean overlaps(long position, long size) {
        if (position + size <= this.position)
            return false;               // 那个在下面
        if (this.position + this.size <= position)
            return false;               // 这个在下面
        return true;
    }

    /**
     * 告诉此锁是否有效。
     *
     * <p> 如果锁对象有效，则调用此方法会释放锁并使对象无效。如果锁对象无效，则调用此方法没有效果。 </p>
     *
     * @return  <tt>true</tt> 表示且仅表示此锁有效
     */
    public abstract boolean isValid();

    /**
     * 释放此锁。
     *
     * <p> 如果此锁对象有效，则调用此方法会释放锁并使对象无效。如果此锁对象无效，则调用此方法没有效果。 </p>
     *
     * @throws  ClosedChannelException
     *          如果用于获取此锁的通道已不再打开
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public abstract void release() throws IOException;

    /**
     * 调用 {@link #release} 方法。此方法被添加到类中，以便可以与自动资源管理块构造一起使用。
     *
     * @since 1.7
     */
    public final void close() throws IOException {
        release();
    }

    /**
     * 返回描述此锁的范围、类型和有效性的字符串。
     *
     * @return  描述性字符串
     */
    public final String toString() {
        return (this.getClass().getName()
                + "[" + position
                + ":" + size
                + " " + (shared ? "shared" : "exclusive")
                + " " + (isValid() ? "valid" : "invalid")
                + "]");
    }

}
