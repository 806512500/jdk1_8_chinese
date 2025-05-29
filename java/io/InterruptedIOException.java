/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
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

package java.io;

/**
 * 表示 I/O 操作已被中断。当执行 I/O 操作的线程被中断时，将抛出
 * <code>InterruptedIOException</code>，以指示输入或输出传输已终止。
 * 字段 {@link #bytesTransferred} 表示在中断发生之前成功传输了多少字节。
 *
 * @author  未署名
 * @see     java.io.InputStream
 * @see     java.io.OutputStream
 * @see     java.lang.Thread#interrupt()
 * @since   JDK1.0
 */
public
class InterruptedIOException extends IOException {
    private static final long serialVersionUID = 4020568460727500567L;

    /**
     * 构造一个 <code>InterruptedIOException</code>，其错误详细信息消息为
     * <code>null</code>。
     */
    public InterruptedIOException() {
        super();
    }

    /**
     * 使用指定的详细信息消息构造一个 <code>InterruptedIOException</code>。
     * 字符串 <code>s</code> 可以通过类 <code>java.lang.Throwable</code> 的
     * <code>{@link java.lang.Throwable#getMessage}</code> 方法稍后检索。
     *
     * @param   s   详细信息消息。
     */
    public InterruptedIOException(String s) {
        super(s);
    }

    /**
     * 报告在 I/O 操作被中断之前已传输了多少字节。
     *
     * @serial
     */
    public int bytesTransferred = 0;
}
