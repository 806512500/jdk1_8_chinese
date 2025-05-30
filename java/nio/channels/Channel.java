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

import java.io.IOException;
import java.io.Closeable;


/**
 * I/O 操作的中心。
 *
 * <p> 通道表示与实体（如硬件设备、文件、网络套接字或能够执行一个或多个不同 I/O 操作的程序组件，例如读取或写入）的开放连接。
 *
 * <p> 通道要么是打开的，要么是关闭的。通道在创建时是打开的，一旦关闭就保持关闭状态。一旦通道关闭，任何尝试在其上执行 I/O 操作的行为都会导致抛出 {@link ClosedChannelException}。
 * 是否通道是打开的可以通过调用其 {@link #isOpen isOpen} 方法来测试。
 *
 * <p> 一般来说，通道旨在支持多线程访问，如扩展和实现此接口的接口和类的规范中所述。
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 专家小组
 * @since 1.4
 */

public interface Channel extends Closeable {

    /**
     * 告诉此通道是否打开。
     *
     * @return 如果且仅当此通道打开时返回 <tt>true</tt>
     */
    public boolean isOpen();

    /**
     * 关闭此通道。
     *
     * <p> 通道关闭后，任何进一步尝试在其上执行 I/O 操作的行为都会导致抛出 {@link ClosedChannelException}。
     *
     * <p> 如果此通道已经关闭，则调用此方法不会产生任何效果。
     *
     * <p> 可以在任何时候调用此方法。但是，如果其他线程已经调用了它，那么另一次调用将阻塞，直到第一次调用完成，之后它将返回而不会产生任何效果。 </p>
     *
     * @throws  IOException  如果发生 I/O 错误
     */
    public void close() throws IOException;

}
