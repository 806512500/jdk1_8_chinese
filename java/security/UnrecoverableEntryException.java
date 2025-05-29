/*
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
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
 * 当无法恢复密钥库中的条目时，将抛出此异常。
 *
 *
 * @since 1.5
 */

public class UnrecoverableEntryException extends GeneralSecurityException {

    private static final long serialVersionUID = -4527142945246286535L;

    /**
     * 构造一个没有详细消息的 UnrecoverableEntryException。
     */
    public UnrecoverableEntryException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 UnrecoverableEntryException，该消息提供了更多关于此异常被抛出的原因的信息。
     *
     * @param msg 详细消息。
     */
   public UnrecoverableEntryException(String msg) {
       super(msg);
    }
}
