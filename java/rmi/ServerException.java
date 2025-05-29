/*
 * 版权所有 (c) 1996, 2003, Oracle 和/或其附属公司。保留所有权利。
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

package java.rmi;

/**
 * 当远程方法调用时，如果在服务器端处理调用过程中抛出了 <code>RemoteException</code>，无论是解包参数还是执行远程方法本身，
 * 将抛出 <code>ServerException</code>。
 *
 * <code>ServerException</code> 实例包含作为其原因的原始 <code>RemoteException</code>。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 */
public class ServerException extends RemoteException {

    /* 表示与 JDK 1.1.x 版本的类兼容 */
    private static final long serialVersionUID = -4775845313121906682L;

    /**
     * 使用指定的详细消息构造 <code>ServerException</code>。
     *
     * @param s 详细消息
     * @since JDK1.1
     */
    public ServerException(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和嵌套异常构造 <code>ServerException</code>。
     *
     * @param s 详细消息
     * @param ex 嵌套异常
     * @since JDK1.1
     */
    public ServerException(String s, Exception ex) {
        super(s, ex);
    }
}
