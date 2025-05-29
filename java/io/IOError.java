/*
 * 版权所有 (c) 2005, 2006, Oracle 和/或其附属公司。保留所有权利。
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

package java.io;

/**
 * 当发生严重的 I/O 错误时抛出。
 *
 * @author  Xueming Shen
 * @since   1.6
 */
public class IOError extends Error {
    /**
     * 使用指定的原因构造一个新的 IOError 实例。IOError 用
     * <tt>(cause==null ? null : cause.toString())</tt>（通常包含
     * 原因的类和详细消息）作为详细消息创建。
     *
     * @param  cause
     *         该错误的原因，或 <tt>null</tt> 如果原因未知
     */
    public IOError(Throwable cause) {
        super(cause);
    }

    private static final long serialVersionUID = 67100927991680413L;
}
