/*
 * Copyright (c) 1997, 1999, Oracle and/or its affiliates. All rights reserved.
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

package java.rmi.activation;

/**
 * 当方法中的 <code>ActivationID</code> 参数被确定为无效时，<code>UnknownObjectException</code> 由 <code>java.rmi.activation</code> 包中的类和接口的方法抛出。如果 <code>ActivationID</code> 当前不被 <code>ActivationSystem</code> 知道，则它是无效的。通过 <code>ActivationSystem.registerObject</code> 方法可以获得 <code>ActivationID</code>。在 <code>Activatable.register</code> 调用期间也可以获得 <code>ActivationID</code>。
 *
 * @author  Ann Wollrath
 * @since   1.2
 * @see     java.rmi.activation.Activatable
 * @see     java.rmi.activation.ActivationGroup
 * @see     java.rmi.activation.ActivationID
 * @see     java.rmi.activation.ActivationMonitor
 * @see     java.rmi.activation.ActivationSystem
 * @see     java.rmi.activation.Activator
 */
public class UnknownObjectException extends ActivationException {

    /** 表示与 Java 2 SDK v1.2 版本的类兼容 */
    private static final long serialVersionUID = 3425547551622251430L;

    /**
     * 使用指定的详细消息构造 <code>UnknownObjectException</code>。
     *
     * @param s 详细消息
     * @since 1.2
     */
    public UnknownObjectException(String s) {
        super(s);
    }
}
