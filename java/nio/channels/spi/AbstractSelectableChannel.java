/*
 * Copyright (c) 2000, 2018, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.channels.spi;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.IllegalSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;


/**
 * 可选择通道的基本实现类。
 *
 * <p> 该类定义了处理通道注册、注销和关闭的机制。它维护此通道的当前阻塞模式以及其当前的选择键集。
 * 它执行所有实现 {@link java.nio.channels.SelectableChannel} 规范所需的同步。该类中定义的抽象受保护方法的实现
 * 不需要对可能在同一操作中参与的其他线程进行同步。 </p>
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

    // 通过注册此通道到选择器创建的选择键。它们被保存起来，因为如果此通道被关闭，这些键必须被注销。受 keyLock 保护。
    //
    private SelectionKey[] keys = null;
    private int keyCount = 0;

    // 选择键集和计数的锁
    private final Object keyLock = new Object();

    // 注册和配置阻塞操作的锁
    private final Object regLock = new Object();

    // 当为非阻塞时为 true，需要 regLock 来更改；
    private volatile boolean nonBlocking;

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


    // -- 选择键集的实用方法 --

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
     * <p> 如果此通道已经注册到给定的选择器，则返回表示该注册的选择键，并将其兴趣集设置为给定值。
     *
     * <p> 否则，此通道尚未注册到给定的选择器，因此在持有适当锁的情况下调用选择器的 {@link AbstractSelector#register register} 方法。
     * 返回的结果键在返回之前被添加到此通道的键集中。 </p>
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
            if (isBlocking())
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
     * <p> 该方法在 {@link AbstractInterruptibleChannel} 类中指定，并由 {@link java.nio.channels.Channel#close close} 方法调用，
     * 该方法反过来调用 {@link #implCloseSelectableChannel implCloseSelectableChannel} 方法来执行关闭此通道的实际工作。
     * 然后取消此通道的所有键。 </p>
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
     * <p> 该方法由 {@link java.nio.channels.Channel#close close} 方法调用，以执行关闭通道的实际工作。
     * 仅在通道尚未关闭时调用此方法，并且它永远不会被调用超过一次。
     *
     * <p> 该方法的实现必须安排任何阻塞在此通道上的其他线程立即返回，无论是通过抛出异常还是正常返回。 </p>
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    protected abstract void implCloseSelectableChannel() throws IOException;


    // -- 阻塞 --

    public final boolean isBlocking() {
        return !nonBlocking;
    }

    public final Object blockingLock() {
        return regLock;
    }

    /**
     * 调整此通道的阻塞模式。
     *
     * <p> 如果给定的阻塞模式与当前阻塞模式不同，则此方法在持有适当锁的情况下调用 {@link #implConfigureBlocking implConfigureBlocking} 方法来更改模式。 </p>
     */
    public final SelectableChannel configureBlocking(boolean block)
        throws IOException
    {
        synchronized (regLock) {
            if (!isOpen())
                throw new ClosedChannelException();
            boolean blocking = !nonBlocking;
            if (block != blocking) {
                if (block && haveValidKeys())
                    throw new IllegalBlockingModeException();
                implConfigureBlocking(block);
                nonBlocking = !block;
            }
        }
        return this;
    }

    /**
     * 调整此通道的阻塞模式。
     *
     * <p> 该方法由 {@link #configureBlocking configureBlocking} 方法调用，以执行更改阻塞模式的实际工作。
     * 仅在新模式与当前模式不同时调用此方法。 </p>
     *
     * @param  block  如果 <tt>true</tt> 则此通道将被置于阻塞模式；如果 <tt>false</tt> 则它将被置于非阻塞模式
     *
     * @throws IOException
     *         如果发生 I/O 错误
     */
    protected abstract void implConfigureBlocking(boolean block)
        throws IOException;

}
