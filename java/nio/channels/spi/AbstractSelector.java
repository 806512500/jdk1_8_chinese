
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

package java.nio.channels.spi;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashSet;
import java.util.Set;
import sun.nio.ch.Interruptible;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 选择器的基实现类。
 *
 * <p> 该类封装了实现选择操作中断所需的低级机制。具体的选择器类必须在调用可能无限期阻塞的 I/O 操作之前和之后分别调用 {@link #begin begin} 和 {@link #end end} 方法。
 * 为了确保 {@link #end end} 方法总是被调用，这些方法应该在 <tt>try</tt>&nbsp;...&nbsp;<tt>finally</tt> 块中使用：
 *
 * <blockquote><pre>
 * try {
 *     begin();
 *     // 在这里执行阻塞 I/O 操作
 *     ...
 * } finally {
 *     end();
 * }</pre></blockquote>
 *
 * <p> 该类还定义了用于维护选择器的已取消键集的方法，以及从其通道的键集中移除键的方法，并声明了由可选择通道的 {@link AbstractSelectableChannel#register register} 方法调用的抽象 {@link #register register} 方法，以执行实际的通道注册工作。 </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 专家组
 * @since 1.4
 */

public abstract class AbstractSelector
    extends Selector
{

    private AtomicBoolean selectorOpen = new AtomicBoolean(true);

    // 创建此选择器的提供者
    private final SelectorProvider provider;

    /**
     * 初始化此类的新实例。
     *
     * @param  provider
     *         创建此选择器的提供者
     */
    protected AbstractSelector(SelectorProvider provider) {
        this.provider = provider;
    }

    private final Set<SelectionKey> cancelledKeys = new HashSet<SelectionKey>();

    void cancel(SelectionKey k) {                       // 包私有
        synchronized (cancelledKeys) {
            cancelledKeys.add(k);
        }
    }

    /**
     * 关闭此选择器。
     *
     * <p> 如果选择器已经关闭，则此方法立即返回。否则，它将选择器标记为已关闭，然后调用 {@link #implCloseSelector implCloseSelector} 方法以完成关闭操作。 </p>
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public final void close() throws IOException {
        boolean open = selectorOpen.getAndSet(false);
        if (!open)
            return;
        implCloseSelector();
    }

    /**
     * 关闭此选择器。
     *
     * <p> 此方法由 {@link #close close} 方法调用，以执行关闭选择器的实际工作。此方法仅在选择器尚未关闭时调用，并且永远不会被调用超过一次。
     *
     * <p> 此方法的实现必须安排任何在此选择器上阻塞在选择操作中的其他线程立即返回，就像调用了 {@link
     * java.nio.channels.Selector#wakeup wakeup} 方法一样。 </p>
     *
     * @throws  IOException
     *          如果在关闭选择器时发生 I/O 错误
     */
    protected abstract void implCloseSelector() throws IOException;

    public final boolean isOpen() {
        return selectorOpen.get();
    }

    /**
     * 返回创建此通道的提供者。
     *
     * @return  创建此通道的提供者
     */
    public final SelectorProvider provider() {
        return provider;
    }

    /**
     * 检索此选择器的已取消键集。
     *
     * <p> 该集只能在同步时使用。 </p>
     *
     * @return  已取消键集
     */
    protected final Set<SelectionKey> cancelledKeys() {
        return cancelledKeys;
    }

    /**
     * 将给定的通道注册到此选择器。
     *
     * <p> 该方法由通道的 {@link
     * AbstractSelectableChannel#register register} 方法调用，以执行将通道注册到此选择器的实际工作。 </p>
     *
     * @param  ch
     *         要注册的通道
     *
     * @param  ops
     *         初始兴趣集，必须有效
     *
     * @param  att
     *         生成的键的初始附件
     *
     * @return  一个新键，表示给定通道在此选择器上的注册
     */
    protected abstract SelectionKey register(AbstractSelectableChannel ch,
                                             int ops, Object att);

    /**
     * 从其通道的键集中移除给定的键。
     *
     * <p> 该方法必须由选择器为每个取消注册的通道调用。 </p>
     *
     * @param  key
     *         要移除的选择键
     */
    protected final void deregister(AbstractSelectionKey key) {
        ((AbstractSelectableChannel)key.channel()).removeKey(key);
    }


    // -- 中断机制 --

    private Interruptible interruptor = null;

    /**
     * 标记可能无限期阻塞的 I/O 操作的开始。
     *
     * <p> 该方法应与 {@link #end end} 方法一起使用，使用 <tt>try</tt>&nbsp;...&nbsp;<tt>finally</tt> 块（如上所示）来实现此选择器的中断。
     *
     * <p> 调用此方法会安排在选择器上阻塞 I/O 操作的线程的 {@link
     * Thread#interrupt interrupt} 方法被调用时，调用选择器的 {@link
     * Selector#wakeup wakeup} 方法。 </p>
     */
    protected final void begin() {
        if (interruptor == null) {
            interruptor = new Interruptible() {
                    public void interrupt(Thread ignore) {
                        AbstractSelector.this.wakeup();
                    }};
        }
        AbstractInterruptibleChannel.blockedOn(interruptor);
        Thread me = Thread.currentThread();
        if (me.isInterrupted())
            interruptor.interrupt(me);
    }

                /**
     * 标记可能无限期阻塞的 I/O 操作的结束。
     *
     * <p> 此方法应与 {@link #begin begin} 方法一起使用，使用 <tt>try</tt>&nbsp;...&nbsp;<tt>finally</tt> 块，
     * 如<a href="#be">上文</a>所示，以便为此选择器实现中断。 </p>
     */
    protected final void end() {
        AbstractInterruptibleChannel.blockedOn(null);
    }

}
