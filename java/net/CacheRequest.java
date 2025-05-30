/*
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
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

import java.io.OutputStream;
import java.io.IOException;

/**
 * 表示用于在 ResponseCache 中存储资源的通道。此类的实例提供一个
 * OutputStream 对象，该对象由协议处理程序调用，以将资源数据存储到缓存中，
 * 还提供一个 abort() 方法，允许中断和放弃缓存存储操作。如果在读取响应或
 * 向缓存写入时遇到 IOException，当前的缓存存储操作将被放弃。
 *
 * @author Yingxian Wang
 * @since 1.5
 */
public abstract class CacheRequest {

    /**
     * 返回一个 OutputStream，可以将响应体写入其中。
     *
     * @return 一个 OutputStream，可以将响应体写入其中
     * @throws IOException 如果在写入响应体时发生 I/O 错误
     */
    public abstract OutputStream getBody() throws IOException;

    /**
     * 中断尝试缓存响应的操作。如果在读取响应或向缓存写入时遇到 IOException，
     * 当前的缓存存储操作将被放弃。
     */
    public abstract void abort();
}
