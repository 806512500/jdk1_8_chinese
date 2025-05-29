/*
 * Copyright (c) 1997, 2005, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 如果密钥库中的密钥无法恢复，则抛出此异常。
 *
 *
 * @since 1.2
 */

public class UnrecoverableKeyException extends UnrecoverableEntryException {

    private static final long serialVersionUID = 7275063078190151277L;

    /**
     * 构造一个没有详细消息的 UnrecoverableKeyException。
     */
    public UnrecoverableKeyException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 UnrecoverableKeyException，该消息提供了更多关于此异常为何被抛出的信息。
     *
     * @param msg 详细消息。
     */
   public UnrecoverableKeyException(String msg) {
       super(msg);
    }
}
