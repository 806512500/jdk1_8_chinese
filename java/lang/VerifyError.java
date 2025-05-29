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

package java.lang;

/**
 * 当“验证器”检测到类文件虽然格式正确，但包含某种内部不一致或安全问题时抛出。
 *
 * @author  未署名
 * @since   JDK1.0
 */
public
class VerifyError extends LinkageError {
    private static final long serialVersionUID = 7001962396098498785L;

    /**
     * 构造一个没有详细消息的 <code>VerifyError</code>。
     */
    public VerifyError() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>VerifyError</code>。
     *
     * @param   s   详细消息。
     */
    public VerifyError(String s) {
        super(s);
    }
}