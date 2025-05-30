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
import java.io.OutputStream;
import java.io.FilterOutputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

/**
 * 一个透明的流，使用通过流的位来更新关联的消息摘要。
 *
 * <p>为了完成消息摘要的计算，在调用此摘要输出流的 {@link #write(int) write} 方法后，需要调用关联消息摘要的其中一个 {@code digest} 方法。
 *
 * <p>可以开启或关闭此流（参见 {@link #on(boolean) on}）。当它开启时，调用其中一个 {@code write} 方法会导致消息摘要的更新。但当它关闭时，消息摘要不会被更新。默认情况下，流是开启的。
 *
 * @see MessageDigest
 * @see DigestInputStream
 *
 * @author Benjamin Renaud
 */
public class DigestOutputStream extends FilterOutputStream {

    private boolean on = true;

    /**
     * 与此流关联的消息摘要。
     */
    protected MessageDigest digest;

    /**
     * 创建一个摘要输出流，使用指定的输出流和消息摘要。
     *
     * @param stream 输出流。
     *
     * @param digest 要与此流关联的消息摘要。
     */
    public DigestOutputStream(OutputStream stream, MessageDigest digest) {
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
     * 使用指定的字节更新消息摘要（如果摘要功能开启），并在任何情况下将字节写入输出流。也就是说，如果摘要功能开启（参见 {@link #on(boolean) on}），此方法将调用与此流关联的消息摘要的 {@code update} 方法，传递字节 {@code b}。然后，此方法将字节写入输出流，直到字节实际写入为止。
     *
     * @param b 要用于更新和写入输出流的字节。
     *
     * @exception IOException 如果发生 I/O 错误。
     *
     * @see MessageDigest#update(byte)
     */
    public void write(int b) throws IOException {
        out.write(b);
        if (on) {
            digest.update((byte)b);
        }
    }

    /**
     * 使用指定的子数组更新消息摘要（如果摘要功能开启），并在任何情况下将子数组写入输出流。也就是说，如果摘要功能开启（参见 {@link #on(boolean) on}），此方法将调用与此流关联的消息摘要的 {@code update} 方法，传递子数组规范。然后，此方法将子数组字节写入输出流，直到字节实际写入为止。
     *
     * @param b 包含要用于更新和写入输出流的子数组的数组。
     *
     * @param off {@code b} 中第一个要更新和写入的字节的偏移量。
     *
     * @param len 从 {@code b} 开始，从偏移量 {@code off} 起要更新和写入的字节数。
     *
     * @exception IOException 如果发生 I/O 错误。
     *
     * @see MessageDigest#update(byte[], int, int)
     */
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        if (on) {
            digest.update(b, off, len);
        }
    }

    /**
     * 开启或关闭摘要功能。默认情况下是开启的。当它开启时，调用其中一个 {@code write} 方法会导致消息摘要的更新。但当它关闭时，消息摘要不会被更新。
     *
     * @param on true 表示开启摘要功能，false 表示关闭摘要功能。
     */
    public void on(boolean on) {
        this.on = on;
    }

    /**
     * 打印此摘要输出流及其关联的消息摘要对象的字符串表示形式。
     */
     public String toString() {
         return "[Digest Output Stream] " + digest.toString();
     }
}
