/*
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.io.IOException;

/**
 * 表示加入 Internet 协议 (IP) 组播组的成员资格的令牌。
 *
 * <p> 成员资格键可以表示接收发送到组的所有数据报的成员资格，也可以是<em>特定源</em>的，这意味着它仅表示接收来自特定源地址的数据报的成员资格。是否为特定源的成员资格可以通过调用其 {@link #sourceAddress() sourceAddress} 方法来确定。
 *
 * <p> 成员资格键在创建时有效，并且在调用 {@link #drop() drop} 方法取消成员资格或通道关闭之前一直有效。可以通过调用其 {@link #isValid() isValid} 方法来测试成员资格键的有效性。
 *
 * <p> 如果成员资格键不是特定源的，并且底层操作系统支持源过滤，则可以使用 {@link #block block} 和 {@link #unblock unblock} 方法来阻止或解除阻止来自特定源地址的组播数据报。
 *
 * @see MulticastChannel
 *
 * @since 1.7
 */
public abstract class MembershipKey {

    /**
     * 初始化此类的新实例。
     */
    protected MembershipKey() {
    }

    /**
     * 告知此成员资格是否有效。
     *
     * <p> 组播组成员资格在创建时有效，并且在调用 {@link #drop() drop} 方法取消成员资格或通道关闭之前一直有效。
     *
     * @return  如果此成员资格键有效，则返回 {@code true}，否则返回 {@code false}
     */
    public abstract boolean isValid();

    /**
     * 取消成员资格。
     *
     * <p> 如果成员资格键表示接收所有数据报的成员资格，则取消成员资格后，通道将不再接收发送到组的任何数据报。如果成员资格键是特定源的，则通道将不再接收从该源地址发送到组的数据报。
     *
     * <p> 取消成员资格后，仍可能接收到发送到组的数据报。这可能是因为数据报正在等待从套接字的接收缓冲区接收。取消成员资格后，通道可以再次 {@link MulticastChannel#join 加入} 组，此时将返回一个新的成员资格键。
     *
     * <p> 返回时，此成员资格对象将 {@link #isValid() 无效}。如果组播组成员资格已经无效，则调用此方法没有效果。一旦组播组成员资格无效，它将永远无效。
     */
    public abstract void drop();

    /**
     * 阻止来自给定源地址的组播数据报。
     *
     * <p> 如果此成员资格键不是特定源的，并且底层操作系统支持源过滤，则此方法将阻止来自给定源地址的组播数据报。如果给定的源地址已经被阻止，则此方法没有效果。源地址被阻止后，仍可能接收到来自该源的数据报。这可能是因为数据报正在等待从套接字的接收缓冲区接收。
     *
     * @param   source
     *          要阻止的源地址
     *
     * @return  此成员资格键
     *
     * @throws  IllegalArgumentException
     *          如果 {@code source} 参数不是单播地址或与组播组的地址类型不同
     * @throws  IllegalStateException
     *          如果此成员资格键是特定源的或不再有效
     * @throws  UnsupportedOperationException
     *          如果底层操作系统不支持源过滤
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public abstract MembershipKey block(InetAddress source) throws IOException;

    /**
     * 解除阻止之前使用 {@link #block(InetAddress) block} 方法阻止的来自给定源地址的组播数据报。
     *
     * @param   source
     *          要解除阻止的源地址
     *
     * @return  此成员资格键
     *
     * @throws  IllegalStateException
     *          如果给定的源地址当前未被阻止或成员资格键不再有效
     */
    public abstract MembershipKey unblock(InetAddress source);

    /**
     * 返回创建此成员资格键的通道。即使成员资格变为 {@link #isValid 无效}，此方法仍将继续返回通道。
     *
     * @return  通道
     */
    public abstract MulticastChannel channel();

    /**
     * 返回创建此成员资格键的组播组。即使成员资格变为 {@link #isValid 无效}，此方法仍将继续返回组播组。
     *
     * @return  组播组
     */
    public abstract InetAddress group();

    /**
     * 返回创建此成员资格键的网络接口。即使成员资格变为 {@link #isValid 无效}，此方法仍将继续返回网络接口。
     *
     * @return  网络接口
     */
    public abstract NetworkInterface networkInterface();

    /**
     * 如果此成员资格键是特定源的，则返回源地址，否则返回 {@code null}。
     *
     * @return  如果此成员资格键是特定源的，则返回源地址，否则返回 {@code null}
     */
    public abstract InetAddress sourceAddress();
}
