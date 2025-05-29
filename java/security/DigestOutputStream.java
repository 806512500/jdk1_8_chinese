/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
 * <p>为了完成消息摘要的计算，在调用此摘要输出流的{@link #write(int) write}方法后，需要调用关联消息
 * 摘要上的一个{@code digest}方法。
 *
 * <p>可以打开或关闭此流（参见
 * {@link #on(boolean) on}）。当它打开时，调用其中一个
 * {@code write}方法会导致
 * 消息摘要的更新。但当它关闭时，消息
 * 摘要不会被更新。默认情况下，流是打开的。
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
     * 创建一个摘要输出流，使用指定的输出流
     * 和消息摘要。
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
     * 使用指定的字节更新消息摘要（如果摘要功能已打开），并在任何情况下将字节
     * 写入输出流。也就是说，如果摘要功能已打开
     * （参见 {@link #on(boolean) on}），此方法将调用
     * 与此流关联的消息摘要上的{@code update}，传递给它字节{@code b}。然后此方法
     * 将字节写入输出流，直到字节实际写入为止。
     *
     * @param b 要用于更新和写入输出流的字节。
     *
     * @exception IOException 如果发生I/O错误。
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
     * 使用指定的子数组更新消息摘要（如果摘要功能已打开），并在任何情况下将子数组写入
     * 输出流。也就是说，如果摘要功能已打开（参见
     * {@link #on(boolean) on}），此方法将调用与此流关联的消息摘要上的{@code update}，
     * 传递给它子数组规范。然后此方法将子数组的字节写入输出流，直到字节实际写入为止。
     *
     * @param b 包含要用于更新和写入输出流的子数组的数组。
     *
     * @param off {@code b}中第一个要更新和写入的字节的偏移量。
     *
     * @param len 从{@code b}中要更新和写入的数据字节数，从偏移量{@code off}开始。
     *
     * @exception IOException 如果发生I/O错误。
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
     * 打开或关闭摘要功能。默认情况下是打开的。当
     * 它打开时，调用其中一个{@code write}方法会导致
     * 消息摘要的更新。但当它关闭时，消息
     * 摘要不会被更新。
     *
     * @param on true表示打开摘要功能，false表示关闭。
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
