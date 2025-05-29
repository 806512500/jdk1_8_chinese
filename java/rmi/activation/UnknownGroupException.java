/*
 * 版权所有 (c) 1997, 1999, Oracle 和/或其关联公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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
 * 当方法的 <code>ActivationGroupID</code> 参数被确定为无效，即不被 <code>ActivationSystem</code> 所知时，
 * <code>java.rmi.activation</code> 包中的类和接口的方法会抛出 <code>UnknownGroupException</code>。
 * 如果 <code>ActivationDesc</code> 中的 <code>ActivationGroupID</code> 指向未在 <code>ActivationSystem</code>
 * 中注册的组，也会抛出 <code>UnknownGroupException</code>。
 *
 * @author  Ann Wollrath
 * @since   1.2
 * @see     java.rmi.activation.Activatable
 * @see     java.rmi.activation.ActivationGroup
 * @see     java.rmi.activation.ActivationGroupID
 * @see     java.rmi.activation.ActivationMonitor
 * @see     java.rmi.activation.ActivationSystem
 */
public class UnknownGroupException extends ActivationException {

    /** 表示与 Java 2 SDK v1.2 版本的类兼容 */
    private static final long serialVersionUID = 7056094974750002460L;

    /**
     * 使用指定的详细消息构造 <code>UnknownGroupException</code>。
     *
     * @param s 详细消息
     * @since 1.2
     */
    public UnknownGroupException(String s) {
        super(s);
    }
}
