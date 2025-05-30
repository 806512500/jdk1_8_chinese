
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

package java.nio.channels;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.io.IOException;


/**
 * 一个表示 {@link SelectableChannel} 与 {@link Selector} 注册的令牌。
 *
 * <p> 每次通道注册到选择器时都会创建一个选择键。选择键在被调用其 {@link #cancel cancel} 方法、关闭其通道或关闭其选择器时取消。取消选择键不会立即将其从选择器中移除；而是将其添加到选择器的 <a
 * href="Selector.html#ks"><i>已取消键集</i></a> 中，在下一次选择操作中移除。可以通过调用其 {@link #isValid isValid} 方法来测试选择键的有效性。
 *
 * <a name="opsets"></a>
 *
 * <p> 选择键包含两个以整数值表示的 <i>操作集</i>。每个操作集的每一位表示由键的通道支持的操作类别。
 *
 * <ul>
 *
 *   <li><p> <i>兴趣集</i> 确定在选择器的下一个选择方法调用时将测试哪些操作类别是否准备好。兴趣集在键创建时初始化为给定值；稍后可以通过 {@link
 *   #interestOps(int)} 方法更改。 </p></li>
 *
 *   <li><p> <i>准备集</i> 识别由键的选择器检测到的键的通道已准备好的操作类别。准备集在键创建时初始化为零；稍后可以在选择操作期间由选择器更新，但不能直接更新。 </p></li>
 *
 * </ul>
 *
 * <p> 选择键的准备集指示其通道已准备好某些操作类别，这是一个提示，但不是保证，即线程可以执行此类别中的操作而不会导致线程阻塞。准备集在选择操作完成后最有可能是准确的。外部事件和在相应通道上调用的 I/O 操作可能会使其变得不准确。
 *
 * <p> 本类定义了所有已知的操作集位，但特定通道支持哪些位取决于通道的类型。每个 {@link SelectableChannel} 的子类定义了一个 {@link
 * SelectableChannel#validOps() validOps()} 方法，该方法返回一个集，标识通道支持的所有操作。尝试设置或测试键的通道不支持的操作集位将导致适当的运行时异常。
 *
 * <p> 通常需要将一些应用程序特定的数据与选择键关联，例如表示较高层次协议状态并处理准备通知的对象，以实现该协议。因此，选择键支持将单个任意对象 <i>附加</i> 到键上。可以通过 {@link #attach attach} 方法附加对象，然后通过 {@link #attachment() attachment} 方法检索。
 *
 * <p> 选择键可以安全地由多个并发线程使用。读取和写入兴趣集的操作通常会与选择器的某些操作同步。这种同步的执行方式取决于实现：在简单的实现中，读取或写入兴趣集可能会无限期阻塞，如果选择操作已经在进行中；在高性能实现中，读取或写入兴趣集可能会短暂阻塞，甚至根本不阻塞。无论如何，选择操作将始终使用操作开始时的兴趣集值。 </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 *
 * @see SelectableChannel
 * @see Selector
 */

public abstract class SelectionKey {

    /**
     * 构造此类的一个实例。
     */
    protected SelectionKey() { }


    // -- 通道和选择器操作 --

    /**
     * 返回创建此键的通道。即使在键被取消后，此方法仍将继续返回通道。
     *
     * @return  此键的通道
     */
    public abstract SelectableChannel channel();

    /**
     * 返回创建此键的选择器。即使在键被取消后，此方法仍将继续返回选择器。
     *
     * @return  此键的选择器
     */
    public abstract Selector selector();

    /**
     * 告诉此键是否有效。
     *
     * <p> 键在创建时有效，并且在取消、关闭其通道或关闭其选择器之前一直有效。 </p>
     *
     * @return  如果且仅当此键有效时返回 <tt>true</tt>
     */
    public abstract boolean isValid();

    /**
     * 请求取消此键的通道与选择器的注册。返回时，键将无效，并且已被添加到选择器的已取消键集中。键将在下一次选择操作中从选择器的所有键集中移除。
     *
     * <p> 如果此键已经被取消，则调用此方法不会产生任何效果。一旦取消，键将永远无效。 </p>
     *
     * <p> 可以在任何时候调用此方法。它同步于选择器的已取消键集，因此如果与同一选择器的取消或选择操作并发调用，可能会短暂阻塞。 </p>
     */
    public abstract void cancel();


    // -- 操作集访问器 --

    /**
     * 检索此键的兴趣集。
     *
     * <p> 保证返回的集只包含对此键的通道有效的操作位。
     *
     * <p> 可以在任何时候调用此方法。是否阻塞以及阻塞多长时间取决于实现。 </p>
     *
     * @return  此键的兴趣集
     *
     * @throws  CancelledKeyException
     *          如果此键已被取消
     */
    public abstract int interestOps();

    /**
     * 将此键的兴趣集设置为给定值。
     *
     * <p> 可以在任何时候调用此方法。是否阻塞以及阻塞多长时间取决于实现。 </p>
     *
     * @param  ops  新的兴趣集
     *
     * @return  此选择键
     *
     * @throws  IllegalArgumentException
     *          如果集中的位不对应于此键的通道支持的操作，即
     *          {@code (ops & ~channel().validOps()) != 0}
     *
     * @throws  CancelledKeyException
     *          如果此键已被取消
     */
    public abstract SelectionKey interestOps(int ops);

    /**
     * 检索此键的准备操作集。
     *
     * <p> 保证返回的集只包含对此键的通道有效的操作位。 </p>
     *
     * @return  此键的准备操作集
     *
     * @throws  CancelledKeyException
     *          如果此键已被取消
     */
    public abstract int readyOps();


    // -- 操作位和位测试便捷方法 --

    /**
     * 读操作的操作集位。
     *
     * <p> 假设选择键的兴趣集在 <a
     * href="Selector.html#selop">选择操作</a> 开始时包含 <tt>OP_READ</tt>。如果选择器检测到相应的通道已准备好读取、到达流末、已被远程关闭以进一步读取或有错误待处理，则会将 <tt>OP_READ</tt> 添加到键的准备操作集中，并将键添加到其已选择键集中。 </p>
     */
    public static final int OP_READ = 1 << 0;

    /**
     * 写操作的操作集位。
     *
     * <p> 假设选择键的兴趣集在 <a
     * href="Selector.html#selop">选择操作</a> 开始时包含 <tt>OP_WRITE</tt>。如果选择器检测到相应的通道已准备好写入、已被远程关闭以进一步写入或有错误待处理，则会将 <tt>OP_WRITE</tt> 添加到键的准备集中，并将键添加到其已选择键集中。 </p>
     */
    public static final int OP_WRITE = 1 << 2;

    /**
     * 套接字连接操作的操作集位。
     *
     * <p> 假设选择键的兴趣集在 <a
     * href="Selector.html#selop">选择操作</a> 开始时包含 <tt>OP_CONNECT</tt>。如果选择器检测到相应的套接字通道已准备好完成其连接序列或有错误待处理，则会将 <tt>OP_CONNECT</tt> 添加到键的准备集中，并将键添加到其已选择键集中。 </p>
     */
    public static final int OP_CONNECT = 1 << 3;

    /**
     * 套接字接受操作的操作集位。
     *
     * <p> 假设选择键的兴趣集在 <a
     * href="Selector.html#selop">选择操作</a> 开始时包含 <tt>OP_ACCEPT</tt>。如果选择器检测到相应的服务器套接字通道已准备好接受另一个连接或有错误待处理，则会将 <tt>OP_ACCEPT</tt> 添加到键的准备集中，并将键添加到其已选择键集中。 </p>
     */
    public static final int OP_ACCEPT = 1 << 4;

    /**
     * 测试此键的通道是否准备好读取。
     *
     * <p> 形如 <tt>k.isReadable()</tt> 的此方法调用的行为与表达式
     *
     * <blockquote><pre>{@code
     * k.readyOps() & OP_READ != 0
     * }</pre></blockquote>
     *
     * 完全相同。
     *
     * <p> 如果此键的通道不支持读取操作，则此方法始终返回 <tt>false</tt>。 </p>
     *
     * @return  如果且仅当
                {@code readyOps() & OP_READ} 非零时返回 <tt>true</tt>
     *
     * @throws  CancelledKeyException
     *          如果此键已被取消
     */
    public final boolean isReadable() {
        return (readyOps() & OP_READ) != 0;
    }

    /**
     * 测试此键的通道是否准备好写入。
     *
     * <p> 形如 <tt>k.isWritable()</tt> 的此方法调用的行为与表达式
     *
     * <blockquote><pre>{@code
     * k.readyOps() & OP_WRITE != 0
     * }</pre></blockquote>
     *
     * 完全相同。
     *
     * <p> 如果此键的通道不支持写入操作，则此方法始终返回 <tt>false</tt>。 </p>
     *
     * @return  如果且仅当
     *          {@code readyOps() & OP_WRITE} 非零时返回 <tt>true</tt>
     *
     * @throws  CancelledKeyException
     *          如果此键已被取消
     */
    public final boolean isWritable() {
        return (readyOps() & OP_WRITE) != 0;
    }

    /**
     * 测试此键的通道是否已完成或未能完成其套接字连接操作。
     *
     * <p> 形如 <tt>k.isConnectable()</tt> 的此方法调用的行为与表达式
     *
     * <blockquote><pre>{@code
     * k.readyOps() & OP_CONNECT != 0
     * }</pre></blockquote>
     *
     * 完全相同。
     *
     * <p> 如果此键的通道不支持套接字连接操作，则此方法始终返回 <tt>false</tt>。 </p>
     *
     * @return  如果且仅当
     *          {@code readyOps() & OP_CONNECT} 非零时返回 <tt>true</tt>
     *
     * @throws  CancelledKeyException
     *          如果此键已被取消
     */
    public final boolean isConnectable() {
        return (readyOps() & OP_CONNECT) != 0;
    }

    /**
     * 测试此键的通道是否准备好接受新的套接字连接。
     *
     * <p> 形如 <tt>k.isAcceptable()</tt> 的此方法调用的行为与表达式
     *
     * <blockquote><pre>{@code
     * k.readyOps() & OP_ACCEPT != 0
     * }</pre></blockquote>
     *
     * 完全相同。
     *
     * <p> 如果此键的通道不支持套接字接受操作，则此方法始终返回 <tt>false</tt>。 </p>
     *
     * @return  如果且仅当
     *          {@code readyOps() & OP_ACCEPT} 非零时返回 <tt>true</tt>
     *
     * @throws  CancelledKeyException
     *          如果此键已被取消
     */
    public final boolean isAcceptable() {
        return (readyOps() & OP_ACCEPT) != 0;
    }


    // -- 附件 --

    private volatile Object attachment = null;

    private static final AtomicReferenceFieldUpdater<SelectionKey,Object>
        attachmentUpdater = AtomicReferenceFieldUpdater.newUpdater(
            SelectionKey.class, Object.class, "attachment"
        );

    /**
     * 将给定对象附加到此键。
     *
     * <p> 可以通过 {@link #attachment() attachment} 方法检索附加的对象。一次只能附加一个对象；调用此方法会导致任何先前的附件被丢弃。可以通过附加 <tt>null</tt> 来丢弃当前附件。 </p>
     *
     * @param  ob
     *         要附加的对象；可以为 <tt>null</tt>
     *
     * @return  之前的附件，如果有，否则返回 <tt>null</tt>
     */
    public final Object attach(Object ob) {
        return attachmentUpdater.getAndSet(this, ob);
    }

    /**
     * 检索当前附件。
     *
     * @return  当前附加到此键的对象，如果没有附件则返回 <tt>null</tt>
     */
    public final Object attachment() {
        return attachment;
    }

}
