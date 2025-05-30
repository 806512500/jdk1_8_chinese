/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security;

import java.io.IOException;
import java.io.EOFException;
import java.io.InputStream;
import java.io.FilterInputStream;
import java.io.PrintStream;
import java.io.ByteArrayInputStream;

/**
 * 一个透明的流，使用通过流的位来更新关联的消息摘要。
 *
 * <p>为了完成消息摘要的计算，在调用此摘要输入流的 {@link #read() read} 方法后，需要调用关联消息摘要的其中一个 {@code digest} 方法。
 *
 * <p>可以开启或关闭此流（参见 {@link #on(boolean) on}）。当它开启时，调用 {@code read} 方法会导致消息摘要的更新。但当它关闭时，消息摘要不会被更新。默认情况下，流是开启的。
 *
 * <p>注意，摘要对象只能计算一个摘要（参见 {@link MessageDigest}），因此为了计算中间摘要，调用者应保留对摘要对象的句柄，并为每个要计算的摘要克隆它，保持原始摘要不变。
 *
 * @see MessageDigest
 *
 * @see DigestOutputStream
 *
 * @author Benjamin Renaud
 */

public class DigestInputStream extends FilterInputStream {

    /* NOTE: This should be made a generic UpdaterInputStream */

    /* 我们是开启还是关闭？ */
    private boolean on = true;

    /**
     * 与此流关联的消息摘要。
     */
    protected MessageDigest digest;

    /**
     * 使用指定的输入流和消息摘要创建一个摘要输入流。
     *
     * @param stream 输入流。
     *
     * @param digest 要与此流关联的消息摘要。
     */
    public DigestInputStream(InputStream stream, MessageDigest digest) {
        super(stream);
        setMessageDigest(digest);
    }

    /**
     * 返回与此流关联的消息摘要。
     *
     * @return 与此流关联的消息摘要。
     * @see #setMessageDigest(java.security.MessageDigest)
     */
    public MessageDigest getMessageDigest() {
        return digest;
    }

    /**
     * 将指定的消息摘要与此流关联。
     *
     * @param digest 要与此流关联的消息摘要。
     * @see #getMessageDigest()
     */
    public void setMessageDigest(MessageDigest digest) {
        this.digest = digest;
    }

    /**
     * 读取一个字节，并更新消息摘要（如果摘要功能开启）。也就是说，此方法从输入流中读取一个字节，直到字节实际读取为止会阻塞。如果摘要功能开启（参见 {@link #on(boolean) on}），此方法将调用与此流关联的消息摘要的 {@code update} 方法，传递读取的字节。
     *
     * @return 读取的字节。
     *
     * @exception IOException 如果发生 I/O 错误。
     *
     * @see MessageDigest#update(byte)
     */
    public int read() throws IOException {
        int ch = in.read();
        if (on && ch != -1) {
            digest.update((byte)ch);
        }
        return ch;
    }

    /**
     * 读取到字节数组中，并更新消息摘要（如果摘要功能开启）。也就是说，此方法从输入流中读取最多 {@code len} 个字节到数组 {@code b} 中，从偏移量 {@code off} 开始。此方法会阻塞直到数据实际读取。如果摘要功能开启（参见 {@link #on(boolean) on}），此方法将调用与此流关联的消息摘要的 {@code update} 方法，传递数据。
     *
     * @param b 数据读取到的数组。
     *
     * @param off 数据应放置在 {@code b} 中的起始偏移量。
     *
     * @param len 从输入流中读取的最大字节数，从偏移量 {@code off} 开始。
     *
     * @return 实际读取的字节数。如果在读取 {@code len} 个字节之前已到达流的末尾，则此值小于 {@code len}。如果调用时流的末尾已到达且未读取任何字节，则返回 -1。
     *
     * @exception IOException 如果发生 I/O 错误。
     *
     * @see MessageDigest#update(byte[], int, int)
     */
    public int read(byte[] b, int off, int len) throws IOException {
        int result = in.read(b, off, len);
        if (on && result != -1) {
            digest.update(b, off, result);
        }
        return result;
    }

    /**
     * 开启或关闭摘要功能。默认情况下是开启的。当它开启时，调用 {@code read} 方法会导致消息摘要的更新。但当它关闭时，消息摘要不会被更新。
     *
     * @param on true 表示开启摘要功能，false 表示关闭。
     */
    public void on(boolean on) {
        this.on = on;
    }

    /**
     * 打印此摘要输入流及其关联的消息摘要对象的字符串表示形式。
     */
     public String toString() {
         return "[Digest Input Stream] " + digest.toString();
     }
}
