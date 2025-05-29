
/*
 * 版权所有 (c) 2000, 2013, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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

package java.nio.channels;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.io.IOException;


/**
 * 表示将 {@link SelectableChannel} 注册到 {@link Selector} 的令牌。
 *
 * <p> 每次将通道注册到选择器时都会创建一个选择键。键在被调用其 {@link #cancel cancel} 方法、关闭其通道或关闭其选择器之前一直有效。取消键不会立即将其从选择器中移除；而是将其添加到选择器的 <a
 * href="Selector.html#ks"><i>已取消键集</i></a> 中，以便在下一次选择操作中移除。可以通过调用其 {@link #isValid isValid} 方法来测试键的有效性。
 *
 * <a name="opsets"></a>
 *
 * <p> 选择键包含两个用整数值表示的 <i>操作集</i>。操作集的每一位表示键的通道支持的操作类别。
 *
 * <ul>
 *
 *   <li><p> <i>兴趣集</i> 确定了在下一次调用选择器的选择方法时将测试哪些操作类别是否准备好。兴趣集在键创建时用给定的值初始化；稍后可以通过 {@link
 *   #interestOps(int)} 方法更改。 </p></li>
 *
 *   <li><p> <i>准备集</i> 标识键的选择器检测到键的通道已准备好执行的操作类别。准备集在键创建时初始化为零；稍后可以在选择操作期间由选择器更新，但不能直接更新。 </p></li>
 *
 * </ul>
 *
 * <p> 选择键的准备集指示其通道已准备好执行某些操作类别，这是一个提示，但不是保证，即线程可以执行此类别中的操作而不会导致线程阻塞。准备集在选择操作完成后最有可能是准确的。它可能会因外部事件和对相应通道调用的 I/O 操作而变得不准确。
 *
 * <p> 本类定义了所有已知的操作集位，但给定通道支持哪些位取决于通道的类型。每个 {@link SelectableChannel} 的子类定义了一个 {@link
 * SelectableChannel#validOps() validOps()} 方法，该方法返回一个集，标识通道支持的操作。尝试设置或测试键的通道不支持的操作集位将导致适当的运行时异常。
 *
 * <p> 通常需要将一些应用程序特定的数据与选择键关联，例如表示高级协议状态并处理准备通知的对象，以便实现该协议。因此，选择键支持将单个任意对象 <i>附加</i> 到键。可以通过 {@link #attach attach} 方法附加对象，然后通过 {@link #attachment() attachment} 方法检索。
 *
 * <p> 选择键可以安全地由多个并发线程使用。读取和写入兴趣集的操作通常会与选择器的某些操作同步。这种同步的具体实现方式是实现依赖的：在简单的实现中，读取或写入兴趣集可能会无限期阻塞，如果选择操作已经在进行中；在高性能实现中，读取或写入兴趣集可能会短暂阻塞，甚至根本不会阻塞。无论如何，选择操作总是使用操作开始时的兴趣集值。 </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 专家组
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
     * 返回为此键创建的通道。即使键被取消，此方法仍将继续返回通道。
     *
     * @return  此键的通道
     */
    public abstract SelectableChannel channel();

    /**
     * 返回为此键创建的选择器。即使键被取消，此方法仍将继续返回选择器。
     *
     * @return  此键的选择器
     */
    public abstract Selector selector();

    /**
     * 告诉此键是否有效。
     *
     * <p> 键在创建时有效，并且在被取消、其通道关闭或其选择器关闭之前一直有效。 </p>
     *
     * @return  如果且仅当此键有效时返回 <tt>true</tt>
     */
    public abstract boolean isValid();

    /**
     * 请求取消此键的通道与选择器的注册。返回时，键将无效，并已被添加到选择器的已取消键集中。键将在下一次选择操作中从选择器的所有键集中移除。
     *
     * <p> 如果此键已被取消，则调用此方法没有效果。一旦取消，键将永远无效。 </p>
     *
     * <p> 可以在任何时候调用此方法。它在选择器的已取消键集上同步，因此如果与涉及同一选择器的取消或选择操作并发调用，可能会短暂阻塞。 </p>
     */
    public abstract void cancel();


    // -- 操作集访问器 --

    /**
     * 检索此键的兴趣集。
     *
     * <p> 保证返回的集只包含对此键的通道有效的操作位。
     *
     * <p> 可以在任何时候调用此方法。是否阻塞以及阻塞多长时间是实现依赖的。 </p>
     *
     * @return  此键的兴趣集
     *
     * @throws  CancelledKeyException
     *          如果此键已被取消
     */
    public abstract int interestOps();

                /**
     * 将此键的兴趣集设置为给定的值。
     *
     * <p> 该方法可以在任何时候调用。是否阻塞以及阻塞多久取决于实现。 </p>
     *
     * @param  ops  新的兴趣集
     *
     * @return  此选择键
     *
     * @throws  IllegalArgumentException
     *          如果集合中的位不对应于此键的通道支持的操作，即
     *          {@code (ops & ~channel().validOps()) != 0}
     *
     * @throws  CancelledKeyException
     *          如果此键已被取消
     */
    public abstract SelectionKey interestOps(int ops);

    /**
     * 检索此键的准备操作集。
     *
     * <p> 保证返回的集合仅包含对此键的通道有效的操作位。 </p>
     *
     * @return  此键的准备操作集
     *
     * @throws  CancelledKeyException
     *          如果此键已被取消
     */
    public abstract int readyOps();


    // -- 操作位和位测试便捷方法 --

    /**
     * 读取操作的操作集位。
     *
     * <p> 假设选择键的兴趣集在<a
     * href="Selector.html#selop">选择操作</a>开始时包含
     * <tt>OP_READ</tt>。如果选择器检测到相应的通道已准备好读取，已到达流末，已被远程关闭以进一步读取，或有错误待处理，则它将
     * <tt>OP_READ</tt>添加到键的准备操作集中，并将键添加到其选定键集。 </p>
     */
    public static final int OP_READ = 1 << 0;

    /**
     * 写入操作的操作集位。
     *
     * <p> 假设选择键的兴趣集在<a
     * href="Selector.html#selop">选择操作</a>开始时包含
     * <tt>OP_WRITE</tt>。如果选择器检测到相应的通道已准备好写入，已被远程关闭以进一步写入，或有错误待处理，则它将
     * <tt>OP_WRITE</tt>添加到键的准备集中，并将键添加到其选定键集。 </p>
     */
    public static final int OP_WRITE = 1 << 2;

    /**
     * 套接字连接操作的操作集位。
     *
     * <p> 假设选择键的兴趣集在<a
     * href="Selector.html#selop">选择操作</a>开始时包含
     * <tt>OP_CONNECT</tt>。如果选择器检测到相应的套接字通道已准备好完成其连接序列，或有错误待处理，则它将
     * <tt>OP_CONNECT</tt>添加到键的准备集中，并将键添加到其选定键集。 </p>
     */
    public static final int OP_CONNECT = 1 << 3;

    /**
     * 套接字接受操作的操作集位。
     *
     * <p> 假设选择键的兴趣集在<a
     * href="Selector.html#selop">选择操作</a>开始时包含
     * <tt>OP_ACCEPT</tt>。如果选择器检测到相应的服务器套接字通道已准备好接受另一个连接，或有错误待处理，则它将
     * <tt>OP_ACCEPT</tt>添加到键的准备集中，并将键添加到其选定键集。 </p>
     */
    public static final int OP_ACCEPT = 1 << 4;

    /**
     * 测试此键的通道是否准备好读取。
     *
     * <p> 以 <tt>k.isReadable()</tt> 形式调用此方法的行为与表达式
     *
     * <blockquote><pre>{@code
     * k.readyOps() & OP_READ != 0
     * }</pre></blockquote>
     *
     * <p> 如果此键的通道不支持读取操作，则此方法始终返回 <tt>false</tt>。 </p>
     *
     * @return  <tt>true</tt> 如果且仅当
                {@code readyOps() & OP_READ} 非零
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
     * <p> 以 <tt>k.isWritable()</tt> 形式调用此方法的行为与表达式
     *
     * <blockquote><pre>{@code
     * k.readyOps() & OP_WRITE != 0
     * }</pre></blockquote>
     *
     * <p> 如果此键的通道不支持写入操作，则此方法始终返回 <tt>false</tt>。 </p>
     *
     * @return  <tt>true</tt> 如果且仅当
     *          {@code readyOps() & OP_WRITE} 非零
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
     * <p> 以 <tt>k.isConnectable()</tt> 形式调用此方法的行为与表达式
     *
     * <blockquote><pre>{@code
     * k.readyOps() & OP_CONNECT != 0
     * }</pre></blockquote>
     *
     * <p> 如果此键的通道不支持套接字连接操作，则此方法始终返回 <tt>false</tt>。 </p>
     *
     * @return  <tt>true</tt> 如果且仅当
     *          {@code readyOps() & OP_CONNECT} 非零
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
     * <p> 以 <tt>k.isAcceptable()</tt> 形式调用此方法的行为与表达式
     *
     * <blockquote><pre>{@code
     * k.readyOps() & OP_ACCEPT != 0
     * }</pre></blockquote>
     *
     * <p> 如果此键的通道不支持套接字接受操作，则此方法始终返回 <tt>false</tt>。 </p>
     *
     * @return  <tt>true</tt> 如果且仅当
     *          {@code readyOps() & OP_ACCEPT} 非零
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
     * 将给定的对象附加到此键。
     *
     * <p> 以后可以通过 {@link #attachment() attachment} 方法检索附加的对象。一次只能附加一个对象；调用此方法会导致任何先前的附加对象被丢弃。可以通过附加 <tt>null</tt> 来丢弃当前的附加对象。 </p>
     *
     * @param  ob
     *         要附加的对象；可以是 <tt>null</tt>
     *
     * @return  任何先前附加的对象，
     *          否则为 <tt>null</tt>
     */
    public final Object attach(Object ob) {
        return attachmentUpdater.getAndSet(this, ob);
    }

    /**
     * 检索当前的附件。
     *
     * @return  当前附加到此键的对象，
     *          或者如果没有附件则为 <tt>null</tt>
     */
    public final Object attachment() {
        return attachment;
    }

}
