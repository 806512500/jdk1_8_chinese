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
 * 表示一个或多个反序列化的对象未通过验证测试。参数应提供失败的原因。
 *
 * @see ObjectInputValidation
 * @since JDK1.1
 *
 * @author  未署名
 * @since   JDK1.1
 */
public class InvalidObjectException extends ObjectStreamException {

    private static final long serialVersionUID = 3233174318281839583L;

    /**
     * 构造一个 <code>InvalidObjectException</code>。
     * @param reason 说明失败原因的详细消息。
     *
     * @see ObjectInputValidation
     */
    public  InvalidObjectException(String reason) {
        super(reason);
    }
}
