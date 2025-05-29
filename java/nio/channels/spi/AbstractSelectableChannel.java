
/*
 * 版权所有 (c) 2000, 2013, Oracle 及/或其附属公司。保留所有权利。
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
import java.nio.channels.*;


/**
 * 可选择通道的基实现类。
 *
 * <p> 该类定义了处理通道注册、注销和关闭机制的方法。它维护此通道的当前阻塞模式及其当前的选择键集。
 * 它执行实现 {@link java.nio.channels.SelectableChannel} 规范所需的所有同步。此类中定义的抽象受保护方法的实现
 * 无需针对可能执行相同操作的其他线程进行同步。 </p>
 *
 *
 * @author Mark Reinhold
 * @author Mike McCloskey
 * @author JSR-51 专家组
 * @since 1.4
 */

public abstract class AbstractSelectableChannel
    extends SelectableChannel
{

    // 创建此通道的提供者
    private final SelectorProvider provider;

    // 通过注册此通道到选择器创建的键。
    // 保存这些键是因为如果此通道关闭，必须注销这些键。受 keyLock 保护。
    //
    private SelectionKey[] keys = null;
    private int keyCount = 0;

    // 键集和计数的锁
    private final Object keyLock = new Object();

    // 注册和配置阻塞操作的锁
    private final Object regLock = new Object();

    // 阻塞模式，受 regLock 保护
    boolean blocking = true;

    /**
     * 初始化此类的新实例。
     *
     * @param  provider
     *         创建此通道的提供者
     */
    protected AbstractSelectableChannel(SelectorProvider provider) {
        this.provider = provider;
    }

    /**
     * 返回创建此通道的提供者。
     *
     * @return  创建此通道的提供者
     */
    public final SelectorProvider provider() {
        return provider;
    }


    // -- 键集的实用方法 --

    private void addKey(SelectionKey k) {
        assert Thread.holdsLock(keyLock);
        int i = 0;
        if ((keys != null) && (keyCount < keys.length)) {
            // 查找键数组中的空元素
            for (i = 0; i < keys.length; i++)
                if (keys[i] == null)
                    break;
        } else if (keys == null) {
            keys =  new SelectionKey[3];
        } else {
            // 扩展键数组
            int n = keys.length * 2;
            SelectionKey[] ks =  new SelectionKey[n];
            for (i = 0; i < keys.length; i++)
                ks[i] = keys[i];
            keys = ks;
            i = keyCount;
        }
        keys[i] = k;
        keyCount++;
    }

    private SelectionKey findKey(Selector sel) {
        synchronized (keyLock) {
            if (keys == null)
                return null;
            for (int i = 0; i < keys.length; i++)
                if ((keys[i] != null) && (keys[i].selector() == sel))
                    return keys[i];
            return null;
        }
    }

    void removeKey(SelectionKey k) {                    // 包私有
        synchronized (keyLock) {
            for (int i = 0; i < keys.length; i++)
                if (keys[i] == k) {
                    keys[i] = null;
                    keyCount--;
                }
            ((AbstractSelectionKey)k).invalidate();
        }
    }

    private boolean haveValidKeys() {
        synchronized (keyLock) {
            if (keyCount == 0)
                return false;
            for (int i = 0; i < keys.length; i++) {
                if ((keys[i] != null) && keys[i].isValid())
                    return true;
            }
            return false;
        }
    }


    // -- 注册 --

    public final boolean isRegistered() {
        synchronized (keyLock) {
            return keyCount != 0;
        }
    }

    public final SelectionKey keyFor(Selector sel) {
        return findKey(sel);
    }

    /**
     * 将此通道注册到给定的选择器，返回一个选择键。
     *
     * <p> 该方法首先验证此通道是否打开以及给定的初始兴趣集是否有效。
     *
     * <p> 如果此通道已注册到给定的选择器，则在设置其兴趣集为给定值后，返回表示该注册的选择键。
     *
     * <p> 否则，此通道尚未注册到给定的选择器，因此在持有适当锁的情况下调用选择器的 {@link AbstractSelector#register register} 方法。
     * 返回的选择键在返回前被添加到此通道的键集中。
     * </p>
     *
     * @throws  ClosedSelectorException {@inheritDoc}
     *
     * @throws  IllegalBlockingModeException {@inheritDoc}
     *
     * @throws  IllegalSelectorException {@inheritDoc}
     *
     * @throws  CancelledKeyException {@inheritDoc}
     *
     * @throws  IllegalArgumentException {@inheritDoc}
     */
    public final SelectionKey register(Selector sel, int ops,
                                       Object att)
        throws ClosedChannelException
    {
        synchronized (regLock) {
            if (!isOpen())
                throw new ClosedChannelException();
            if ((ops & ~validOps()) != 0)
                throw new IllegalArgumentException();
            if (blocking)
                throw new IllegalBlockingModeException();
            SelectionKey k = findKey(sel);
            if (k != null) {
                k.interestOps(ops);
                k.attach(att);
            }
            if (k == null) {
                // 新注册
                synchronized (keyLock) {
                    if (!isOpen())
                        throw new ClosedChannelException();
                    k = ((AbstractSelector)sel).register(this, ops, att);
                    addKey(k);
                }
            }
            return k;
        }
    }

    // -- 关闭 --

    /**
     * 关闭此通道。
     *
     * <p> 此方法在 {@link
     * AbstractInterruptibleChannel} 类中指定，并由 {@link
     * java.nio.channels.Channel#close close} 方法调用，进而调用
     * {@link #implCloseSelectableChannel implCloseSelectableChannel} 方法
     * 以执行关闭此通道的实际工作。然后取消此通道的所有密钥。 </p>
     */
    protected final void implCloseChannel() throws IOException {
        implCloseSelectableChannel();
        synchronized (keyLock) {
            int count = (keys == null) ? 0 : keys.length;
            for (int i = 0; i < count; i++) {
                SelectionKey k = keys[i];
                if (k != null)
                    k.cancel();
            }
        }
    }

    /**
     * 关闭此可选择通道。
     *
     * <p> 此方法由 {@link java.nio.channels.Channel#close
     * close} 方法调用，以执行关闭通道的实际工作。此方法仅在通道尚未关闭时调用，并且不会调用超过一次。
     *
     * <p> 此方法的实现必须安排任何阻塞在此通道上的其他线程立即返回，无论是通过抛出异常还是正常返回。
     * </p>
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    protected abstract void implCloseSelectableChannel() throws IOException;


    // -- 阻塞 --

    public final boolean isBlocking() {
        synchronized (regLock) {
            return blocking;
        }
    }

    public final Object blockingLock() {
        return regLock;
    }

    /**
     * 调整此通道的阻塞模式。
     *
     * <p> 如果给定的阻塞模式与当前的阻塞模式不同，则此方法将在持有适当锁的情况下调用
     * {@link #implConfigureBlocking implConfigureBlocking} 方法来更改模式。 </p>
     */
    public final SelectableChannel configureBlocking(boolean block)
        throws IOException
    {
        synchronized (regLock) {
            if (!isOpen())
                throw new ClosedChannelException();
            if (blocking == block)
                return this;
            if (block && haveValidKeys())
                throw new IllegalBlockingModeException();
            implConfigureBlocking(block);
            blocking = block;
        }
        return this;
    }

    /**
     * 调整此通道的阻塞模式。
     *
     * <p> 此方法由 {@link #configureBlocking
     * configureBlocking} 方法调用，以执行更改阻塞模式的实际工作。此方法仅在新模式与当前模式不同时调用。 </p>
     *
     * @param  block  如果 <tt>true</tt> 则此通道将被置于阻塞模式；如果 <tt>false</tt> 则将置于非阻塞模式
     *
     * @throws IOException
     *         如果发生 I/O 错误
     */
    protected abstract void implConfigureBlocking(boolean block)
        throws IOException;

}
