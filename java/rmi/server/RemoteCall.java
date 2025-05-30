/*
 * Copyright (c) 1996, 2006, Oracle and/or its affiliates. All rights reserved.
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

package java.rmi.server;
import java.rmi.*;
import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.io.StreamCorruptedException;
import java.io.IOException;

/**
 * <code>RemoteCall</code> 是一个仅由 RMI 运行时（与远程对象的存根和骨架结合使用）使用的抽象，用于执行对远程对象的调用。 <code>RemoteCall</code> 接口已弃用，因为它仅由 <code>java.rmi.server.RemoteRef</code> 的已弃用方法使用。
 *
 * @since   JDK1.1
 * @author  Ann Wollrath
 * @author  Roger Riggs
 * @see     java.rmi.server.RemoteRef
 * @deprecated 没有替代品。
 */
@Deprecated
public interface RemoteCall {

    /**
     * 返回存根/骨架应将参数/结果放入的输出流。
     *
     * @return 用于参数/结果的输出流
     * @exception java.io.IOException 如果发生 I/O 错误。
     * @since JDK1.1
     * @deprecated 没有替代品
     */
    @Deprecated
    ObjectOutput getOutputStream()  throws IOException;

    /**
     * 释放输出流；在某些传输中，这将释放流。
     *
     * @exception java.io.IOException 如果发生 I/O 错误。
     * @since JDK1.1
     * @deprecated 没有替代品
     */
    @Deprecated
    void releaseOutputStream()  throws IOException;

    /**
     * 获取存根/骨架应从中获取结果/参数的输入流。
     *
     * @return 用于读取参数/结果的输入流
     * @exception java.io.IOException 如果发生 I/O 错误。
     * @since JDK1.1
     * @deprecated 没有替代品
     */
    @Deprecated
    ObjectInput getInputStream()  throws IOException;


    /**
     * 释放输入流。这将允许某些传输提前释放通道。
     *
     * @exception java.io.IOException 如果发生 I/O 错误。
     * @since JDK1.1
     * @deprecated 没有替代品
     */
    @Deprecated
    void releaseInputStream() throws IOException;

    /**
     * 返回输出流（可能输出与调用成功相关的信息）。每个远程调用只能成功一次。
     *
     * @param success 如果为 true，表示正常返回，否则表示异常返回。
     * @return 用于写入调用结果的输出流
     * @exception java.io.IOException              如果发生 I/O 错误。
     * @exception java.io.StreamCorruptedException 如果已调用。
     * @since JDK1.1
     * @deprecated 没有替代品
     */
    @Deprecated
    ObjectOutput getResultStream(boolean success) throws IOException,
        StreamCorruptedException;

    /**
     * 执行调用所需的操作。
     *
     * @exception java.lang.Exception 如果发生一般异常。
     * @since JDK1.1
     * @deprecated 没有替代品
     */
    @Deprecated
    void executeCall() throws Exception;

    /**
     * 允许在远程调用完成后进行清理。
     *
     * @exception java.io.IOException 如果发生 I/O 错误。
     * @since JDK1.1
     * @deprecated 没有替代品
     */
    @Deprecated
    void done() throws IOException;
}
