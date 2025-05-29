/*
 * Copyright (c) 1996, 2001, Oracle and/or its affiliates. All rights reserved.
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
 * <code>ServerError</code> 是在远程方法调用过程中，当服务器处理调用时抛出 <code>Error</code> 时抛出的异常。这可能发生在解组参数、执行远程方法本身或组返回值时。
 *
 * <code>ServerError</code> 实例包含其原因的原始 <code>Error</code>。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 */
public class ServerError extends RemoteException {

    /* 表示与 JDK 1.1.x 版本的类兼容 */
    private static final long serialVersionUID = 8455284893909696482L;

    /**
     * 使用指定的详细消息和嵌套错误构造 <code>ServerError</code>。
     *
     * @param s 详细消息
     * @param err 嵌套错误
     * @since JDK1.1
     */
    public ServerError(String s, Error err) {
        super(s, err);
    }
}
