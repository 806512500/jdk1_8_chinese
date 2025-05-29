/*
 * Copyright (c) 1996, 2008, Oracle and/or its affiliates. All rights reserved.
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
 * 表示同步操作失败的信号。
 *
 * @author  Ken Arnold
 * @see     java.io.FileDescriptor#sync
 * @see     java.io.IOException
 * @since   JDK1.1
 */
public class SyncFailedException extends IOException {
    private static final long serialVersionUID = -2353342684412443330L;

    /**
     * 使用详细消息构造 SyncFailedException。
     * 详细消息是一个描述此特定异常的字符串。
     *
     * @param desc  描述异常的字符串。
     */
    public SyncFailedException(String desc) {
        super(desc);
    }
}
