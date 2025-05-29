/*
 * 版权所有 (c) 1996, 2013, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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
import java.io.InputStream;
import java.io.FilterInputStream;
import java.io.PrintStream;
import java.io.ByteArrayInputStream;

/**
 * 一个透明的流，使用通过流的位来更新关联的消息摘要。
 *
 * <p>为了完成消息摘要的计算，在调用此摘要输入流的 {@code read} 方法之一后，调用关联消息摘要的
 * {@code digest} 方法之一。
 *
 * <p>可以打开或关闭此流（参见 {@link #on(boolean) on}）。当它打开时，调用 {@code read} 方法之一
 * 会导致消息摘要的更新。但当它关闭时，消息摘要不会更新。默认情况下，流是打开的。
 *
 * <p>请注意，摘要对象只能计算一个摘要（参见 {@link MessageDigest}），
 * 因此为了计算中间摘要，调用者应该保留一个对摘要对象的句柄，并为每个要计算的摘要克隆它，
 * 使原始摘要保持不变。
 *
 * @see MessageDigest
 *
 * @see DigestOutputStream
 *
 * @author Benjamin Renaud
 */

public class DigestInputStream extends FilterInputStream {

    /* 注意：这应该被做成一个通用的 UpdaterInputStream */

    /* 我们是打开还是关闭？ */
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
     * 读取一个字节，并更新消息摘要（如果摘要功能已打开）。也就是说，此方法从输入流中读取一个字节，
     * 直到实际读取字节之前会阻塞。如果摘要功能已打开（参见 {@link #on(boolean) on}），此方法
     * 将调用与此流关联的消息摘要的 {@code update} 方法，传递读取的字节。
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
     * 读取到字节数组中，并更新消息摘要（如果摘要功能已打开）。也就是说，此方法从输入流中读取最多
     * {@code len} 个字节到数组 {@code b} 中，从偏移量 {@code off} 开始。此方法
     * 会阻塞直到实际读取数据。如果摘要功能已打开（参见 {@link #on(boolean) on}），此方法将调用
     * 与此流关联的消息摘要的 {@code update} 方法，传递数据。
     *
     * @param b 读取数据的数组。
     *
     * @param off 数据应放置在 {@code b} 中的起始偏移量。
     *
     * @param len 从输入流中读取的最大字节数到 b 中，从偏移量 {@code off} 开始。
     *
     * @return 实际读取的字节数。如果在读取 {@code len} 个字节之前已到达流的末尾，则此值小于
     * {@code len}。如果在调用时流的末尾已到达且未读取任何字节，则返回 -1。
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
     * 打开或关闭摘要功能。默认情况下是打开的。当它打开时，调用 {@code read} 方法之一
     * 会导致消息摘要的更新。但当它关闭时，消息摘要不会更新。
     *
     * @param on true 表示打开摘要功能，false 表示关闭。
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
