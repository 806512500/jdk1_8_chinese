/*
 * Copyright (c) 1996, 1999, Oracle and/or its affiliates. All rights reserved.
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
 * 回调接口，允许验证对象图中的对象。
 * 允许在对象图完全反序列化后调用对象。
 *
 * @author  未署名
 * @see     ObjectInputStream
 * @see     ObjectInputStream#registerValidation(java.io.ObjectInputValidation, int)
 * @since   JDK1.1
 */
public interface ObjectInputValidation {
    /**
     * 验证对象。
     *
     * @exception InvalidObjectException 如果对象无法验证自身。
     */
    public void validateObject() throws InvalidObjectException;
}
