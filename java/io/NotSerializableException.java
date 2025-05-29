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
 * 当需要一个实现 Serializable 接口的实例时抛出此异常。
 * 序列化运行时或实例的类可以抛出此异常。参数应该是类的名称。
 *
 * @author  未署名
 * @since   JDK1.1
 */
public class NotSerializableException extends ObjectStreamException {

    private static final long serialVersionUID = 2906642554793891381L;

    /**
     * 使用消息字符串构造 NotSerializableException 对象。
     *
     * @param classname 正在序列化/反序列化的实例的类。
     */
    public NotSerializableException(String classname) {
        super(classname);
    }

    /**
     *  构造一个 NotSerializableException 对象。
     */
    public NotSerializableException() {
        super();
    }
}
