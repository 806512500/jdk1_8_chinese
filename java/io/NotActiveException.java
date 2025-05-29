/*
 * Copyright (c) 1996, 2005, Oracle and/or its affiliates. All rights reserved.
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
 * 当序列化或反序列化未激活时抛出。
 *
 * @author  未署名
 * @since   JDK1.1
 */
public class NotActiveException extends ObjectStreamException {

    private static final long serialVersionUID = -3893467273049808895L;

    /**
     * 构造一个新的 NotActiveException，带有给定的原因。
     *
     * @param reason  一个描述异常原因的字符串。
     */
    public NotActiveException(String reason) {
        super(reason);
    }

    /**
     * 构造一个新的 NotActiveException，不带原因。
     */
    public NotActiveException() {
        super();
    }
}
