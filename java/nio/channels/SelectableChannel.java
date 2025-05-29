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

import java.io.IOException;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.channels.spi.SelectorProvider;


/**
 * 可以通过 {@link Selector} 复用的通道。
 *
 * <p> 为了与选择器一起使用，此类的实例必须首先通过 {@link #register(Selector,int,Object)
 * register} 方法 <i>注册</i>。此方法返回一个新的 {@link SelectionKey} 对象，表示通道在选择器上的注册。
 *
 * <p> 一旦注册到选择器，通道将保持注册状态，直到被 <i>注销</i>。这涉及释放选择器为通道分配的任何资源。
 *
 * <p> 通道不能直接注销；相反，表示其注册的键必须被 <i>取消</i>。取消一个键会请求在选择器的下一个选择操作期间注销该通道。
 * 可以通过调用键的 {@link
 * SelectionKey#cancel() cancel} 方法显式取消键。当通道关闭时，无论是通过调用其 {@link
 * Channel#close close} 方法还是通过中断在该通道上阻塞的 I/O 操作的线程，通道的所有键都会被隐式取消。
 *
 * <p> 如果选择器本身被关闭，则通道将被注销，表示其注册的键将被立即失效。
 *
 * <p> 一个通道最多只能与任何一个特定的选择器注册一次。
 *
 * <p> 是否将通道注册到一个或多个选择器可以通过调用 {@link #isRegistered isRegistered} 方法来确定。
 *
 * <p> 可选择的通道可以安全地由多个并发线程使用。 </p>
 *
 *
 * <a name="bm"></a>
 * <h2>阻塞模式</h2>
 *
 * 可选择的通道要么处于 <i>阻塞</i> 模式，要么处于 <i>非阻塞</i> 模式。在阻塞模式下，调用通道的每个 I/O 操作都会阻塞，直到操作完成。
 * 在非阻塞模式下，I/O 操作永远不会阻塞，并且可能传输的字节数少于请求的字节数，甚至可能一个字节也不传输。
 * 可选择的通道的阻塞模式可以通过调用其 {@link #isBlocking isBlocking} 方法来确定。
 *
 * <p> 新创建的可选择通道始终处于阻塞模式。
 * 非阻塞模式通常与基于选择器的多路复用结合使用。在将通道注册到选择器之前，必须将其置于非阻塞模式，并且在注销之前不能返回到阻塞模式。
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 专家小组
 * @since 1.4
 *
 * @see SelectionKey
 * @see Selector
 */

public abstract class SelectableChannel
    extends AbstractInterruptibleChannel
    implements Channel
{

    /**
     * 初始化此类的新实例。
     */
    protected SelectableChannel() { }

    /**
     * 返回创建此通道的提供者。
     *
     * @return  创建此通道的提供者
     */
    public abstract SelectorProvider provider();

    /**
     * 返回一个 <a href="SelectionKey.html#opsets">操作集</a>，标识此通道支持的操作。
     * 此整数值中设置的位恰好表示对此通道有效的操作。对于给定的具体通道类，此方法始终返回相同的值。
     *
     * @return  有效操作集
     */
    public abstract int validOps();

    // 内部状态：
    //   keySet，可能为空但永远不会为 null，通常是一个小数组
    //   boolean isRegistered，由 key set 保护
    //   regLock，防止重复注册的锁对象
    //   boolean isBlocking，由 regLock 保护

    /**
     * 告知此通道当前是否已注册到任何选择器。新创建的通道未注册。
     *
     * <p> 由于键取消与通道注销之间的固有延迟，即使所有键都被取消后，通道也可能在一段时间内保持注册状态。
     * 通道也可能在关闭后的一段时间内保持注册状态。 </p>
     *
     * @return <tt>true</tt>，当且仅当此通道已注册
     */
    public abstract boolean isRegistered();
    //
    // sync(keySet) { return isRegistered; }

    /**
     * 检索表示通道在给定选择器上的注册的键。
     *
     * @param   sel
     *          选择器
     *
     * @return  当此通道上次在给定选择器上注册时返回的键，如果此通道当前未在该选择器上注册，则返回 <tt>null</tt>
     */
    public abstract SelectionKey keyFor(Selector sel);
    //
    // sync(keySet) { return findKey(sel); }

    /**
     * 将此通道注册到给定的选择器，返回一个选择键。
     *
     * <p> 如果此通道当前已注册到给定的选择器，则返回表示该注册的键。键的兴趣集将被更改为 <tt>ops</tt>，就像调用了
     * {@link SelectionKey#interestOps(int) interestOps(int)} 方法一样。如果 <tt>att</tt> 参数不为 <tt>null</tt>，
     * 则键的附件将被设置为该值。如果键已被取消，则会抛出 {@link CancelledKeyException}。
     *
     * <p> 否则，此通道尚未注册到给定的选择器，因此将其注册并返回新创建的键。键的初始兴趣集将为 <tt>ops</tt>，其附件将为 <tt>att</tt>。
     *
     * <p> 可以随时调用此方法。如果在此方法或 {@link
     * #configureBlocking(boolean) configureBlocking} 方法的另一个调用正在进行时调用此方法，则它将首先阻塞，直到另一个操作完成。
     * 然后，此方法将同步选择器的键集，因此如果与涉及同一选择器的另一个注册或选择操作并发调用，可能会阻塞。 </p>
     *
     * <p> 如果在操作进行中关闭此通道，则此方法返回的键将被取消，因此无效。 </p>
     *
     * @param  sel
     *         要注册此通道的选择器
     *
     * @param  ops
     *         结果键的兴趣集
     *
     * @param  att
     *         结果键的附件；可以为 <tt>null</tt>
     *
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     *
     * @throws  ClosedSelectorException
     *          如果选择器已关闭
     *
     * @throws  IllegalBlockingModeException
     *          如果此通道处于阻塞模式
     *
     * @throws  IllegalSelectorException
     *          如果此通道不是由与给定选择器相同的提供者创建的
     *
     * @throws  CancelledKeyException
     *          如果此通道当前已注册到给定的选择器，但相应的键已被取消
     *
     * @throws  IllegalArgumentException
     *          如果 <tt>ops</tt> 集中的位不对应于此通道支持的操作，即，如果
     *          {@code set & ~validOps() != 0}
     *
     * @return  代表此通道在给定选择器上注册的键
     */
    public abstract SelectionKey register(Selector sel, int ops, Object att)
        throws ClosedChannelException;
    //
    // sync(regLock) {
    //   sync(keySet) { 查找选择器 }
    //   if (找到通道) { 设置兴趣集 -- 可能在选择器中阻塞；
    //                        返回键; }
    //   创建新键 -- 可能在选择器中的某个地方阻塞；
    //   sync(keySet) { 添加键; }
    //   attach(附件);
    //   返回键;
    // }

                /**
     * 将此通道注册到给定的选择器，返回一个选择键。
     *
     * <p> 以如下形式调用此便捷方法
     *
     * <blockquote><tt>sc.register(sel, ops)</tt></blockquote>
     *
     * 的行为与调用
     *
     * <blockquote><tt>sc.{@link
     * #register(java.nio.channels.Selector,int,java.lang.Object)
     * register}(sel, ops, null)</tt></blockquote>
     * 完全相同。
     *
     * @param  sel
     *         要注册此通道的选择器。
     *
     * @param  ops
     *         结果键的兴趣集。
     *
     * @throws  ClosedChannelException
     *          如果此通道已关闭。
     *
     * @throws  ClosedSelectorException
     *          如果选择器已关闭。
     *
     * @throws  IllegalBlockingModeException
     *          如果此通道处于阻塞模式。
     *
     * @throws  IllegalSelectorException
     *          如果此通道不是由与给定选择器相同的提供者创建的。
     *
     * @throws  CancelledKeyException
     *          如果此通道当前已注册到给定选择器，但对应的键已被取消。
     *
     * @throws  IllegalArgumentException
     *          如果 <tt>ops</tt> 中的位不对应于此通道支持的操作，即，如果 {@code set &
     *          ~validOps() != 0}。
     *
     * @return  一个键，表示此通道与给定选择器的注册。
     */
    public final SelectionKey register(Selector sel, int ops)
        throws ClosedChannelException
    {
        return register(sel, ops, null);
    }

    /**
     * 调整此通道的阻塞模式。
     *
     * <p> 如果此通道已注册到一个或多个选择器，则尝试将其置于阻塞模式将导致抛出 {@link
     * IllegalBlockingModeException}。
     *
     * <p> 本方法可在任何时候调用。新的阻塞模式仅会影响此方法返回后启动的 I/O 操作。对于某些实现，这可能需要阻塞直到所有挂起的 I/O 操作完成。
     *
     * <p> 如果在本方法的另一个调用或 {@link #register(Selector, int) register} 方法的调用正在进行时调用本方法，则它将首先阻塞，直到另一个操作完成。 </p>
     *
     * @param  block  如果 <tt>true</tt> 则此通道将置于阻塞模式；如果 <tt>false</tt> 则置于非阻塞模式。
     *
     * @return  此可选择通道。
     *
     * @throws  ClosedChannelException
     *          如果此通道已关闭。
     *
     * @throws  IllegalBlockingModeException
     *          如果 <tt>block</tt> 为 <tt>true</tt> 且此通道已注册到一个或多个选择器。
     *
     * @throws IOException
     *         如果发生 I/O 错误。
     */
    public abstract SelectableChannel configureBlocking(boolean block)
        throws IOException;
    //
    // sync(regLock) {
    //   sync(keySet) { throw IBME if block && isRegistered; }
    //   change mode;
    // }

    /**
     * 告知此通道上的每个 I/O 操作是否会阻塞直到完成。新创建的通道总是处于阻塞模式。
     *
     * <p> 如果此通道已关闭，则此方法返回的值未指定。 </p>
     *
     * @return <tt>true</tt> 如果且仅如果此通道处于阻塞模式。
     */
    public abstract boolean isBlocking();

    /**
     * 检索 {@link #configureBlocking configureBlocking} 和 {@link #register register} 方法同步的对象。
     * 这在需要在短时间内维持特定阻塞模式的适配器实现中通常很有用。
     *
     * @return  阻塞模式锁对象。
     */
    public abstract Object blockingLock();

}
