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

import java.net.SocketOption;
import java.net.SocketAddress;
import java.util.Set;
import java.io.IOException;

/**
 * 一个网络套接字的通道。
 *
 * <p> 实现此接口的通道是一个网络套接字的通道。{@link #bind(SocketAddress) bind} 方法用于将套接字绑定到本地 {@link SocketAddress 地址}，{@link #getLocalAddress()
 * getLocalAddress} 方法返回套接字绑定的地址，{@link #setOption(SocketOption,Object) setOption} 和 {@link
 * #getOption(SocketOption) getOption} 方法用于设置和查询套接字选项。此接口的实现应指定其支持的套接字选项。
 *
 * <p> 不返回值的 {@link #bind bind} 和 {@link #setOption setOption} 方法被指定为返回调用它们的网络通道。这允许方法调用链式调用。此接口的实现应专门化返回类型，以便可以在实现类上链式调用方法。
 *
 * @since 1.7
 */

public interface NetworkChannel
    extends Channel
{
    /**
     * 将通道的套接字绑定到本地地址。
     *
     * <p> 此方法用于在套接字和本地地址之间建立关联。一旦建立了关联，套接字将保持绑定状态，直到通道关闭。如果 {@code local} 参数的值为 {@code null}，则套接字将绑定到自动分配的地址。
     *
     * @param   local
     *          要绑定的套接字地址，或 {@code null} 表示绑定到自动分配的套接字地址
     *
     * @return  本通道
     *
     * @throws  AlreadyBoundException
     *          如果套接字已绑定
     * @throws  UnsupportedAddressTypeException
     *          如果给定地址的类型不受支持
     * @throws  ClosedChannelException
     *          如果通道已关闭
     * @throws  IOException
     *          如果发生其他 I/O 错误
     * @throws  SecurityException
     *          如果安装了安全经理并且它拒绝了未指定的权限。此接口的实现应指定任何必需的权限。
     *
     * @see #getLocalAddress
     */
    NetworkChannel bind(SocketAddress local) throws IOException;

    /**
     * 返回此通道的套接字绑定的套接字地址。
     *
     * <p> 如果通道绑定到 Internet 协议套接字地址，则此方法的返回值为 {@link
     * java.net.InetSocketAddress} 类型。
     *
     * @return  套接字绑定的套接字地址，或如果通道的套接字未绑定则返回 {@code null}
     *
     * @throws  ClosedChannelException
     *          如果通道已关闭
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    SocketAddress getLocalAddress() throws IOException;

    /**
     * 设置套接字选项的值。
     *
     * @param   <T>
     *          套接字选项值的类型
     * @param   name
     *          套接字选项
     * @param   value
     *          套接字选项的值。对于某些套接字选项，{@code null} 可能是有效值。
     *
     * @return  本通道
     *
     * @throws  UnsupportedOperationException
     *          如果此通道不支持该套接字选项
     * @throws  IllegalArgumentException
     *          如果该值不是此套接字选项的有效值
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @see java.net.StandardSocketOptions
     */
    <T> NetworkChannel setOption(SocketOption<T> name, T value) throws IOException;

    /**
     * 返回套接字选项的值。
     *
     * @param   <T>
     *          套接字选项值的类型
     * @param   name
     *          套接字选项
     *
     * @return  套接字选项的值。对于某些套接字选项，{@code null} 可能是有效值。
     *
     * @throws  UnsupportedOperationException
     *          如果此通道不支持该套接字选项
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @see java.net.StandardSocketOptions
     */
    <T> T getOption(SocketOption<T> name) throws IOException;

    /**
     * 返回此通道支持的套接字选项的集合。
     *
     * <p> 即使在通道关闭后，此方法仍将继续返回选项集。
     *
     * @return  此通道支持的套接字选项的集合
     */
    Set<SocketOption<?>> supportedOptions();
}
