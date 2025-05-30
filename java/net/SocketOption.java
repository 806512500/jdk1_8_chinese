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

package java.net;

/**
 * 与套接字关联的套接字选项。
 *
 * <p> 在 {@link java.nio.channels channels} 包中，{@link
 * java.nio.channels.NetworkChannel} 接口定义了 {@link
 * java.nio.channels.NetworkChannel#setOption(SocketOption,Object) setOption}
 * 和 {@link java.nio.channels.NetworkChannel#getOption(SocketOption) getOption}
 * 方法来设置和查询通道的套接字选项。
 *
 * @param   <T>     套接字选项值的类型。
 *
 * @since 1.7
 *
 * @see StandardSocketOptions
 */

public interface SocketOption<T> {

    /**
     * 返回套接字选项的名称。
     *
     * @return 套接字选项的名称
     */
    String name();

    /**
     * 返回套接字选项值的类型。
     *
     * @return 套接字选项值的类型
     */
    Class<T> type();
}
