/*
 * Copyright (c) 1996, 1998, Oracle and/or its affiliates. All rights reserved.
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

package java.rmi;

/**
 * 当在反序列化远程方法调用的参数或结果时，如果发生以下任何条件，可能会抛出 <code>UnmarshalException</code>：
 * <ul>
 * <li> 如果在反序列化调用头时发生异常
 * <li> 如果返回值的协议无效
 * <li> 如果在反序列化参数（在服务器端）或返回值（在客户端）时发生 <code>java.io.IOException</code>
 * <li> 如果在反序列化参数或返回值时发生 <code>java.lang.ClassNotFoundException</code>
 * <li> 如果在服务器端无法加载骨架；注意，骨架在1.1存根协议中是必需的，但在1.2存根协议中不是必需的。
 * <li> 如果方法哈希无效（即，缺少方法）。
 * <li> 如果在反序列化远程对象的存根时，无法创建远程引用对象。
 * </ul>
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 */
public class UnmarshalException extends RemoteException {

    /* 表示与JDK 1.1.x版本类的兼容性 */
    private static final long serialVersionUID = 594380845140740218L;

    /**
     * 使用指定的详细消息构造 <code>UnmarshalException</code>。
     *
     * @param s 详细消息
     * @since JDK1.1
     */
    public UnmarshalException(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和嵌套异常构造 <code>UnmarshalException</code>。
     *
     * @param s 详细消息
     * @param ex 嵌套异常
     * @since JDK1.1
     */
    public UnmarshalException(String s, Exception ex) {
        super(s, ex);
    }
}
