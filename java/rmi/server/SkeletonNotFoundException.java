/*
 * 版权所有 (c) 1996, 2006, Oracle 和/或其附属公司。保留所有权利。
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

package java.rmi.server;

import java.rmi.RemoteException;

/**
 * 如果找不到正在导出的远程对象对应的 <code>Skeleton</code>，则抛出 <code>SkeletonNotFoundException</code>。
 * 由于 Skeletons 已不再需要，因此此异常永远不会被抛出。
 *
 * @since   JDK1.1
 * @deprecated 无需替代。自 Java 2 平台 v1.2 及更高版本起，远程方法调用不再需要 Skeletons。
 */
@Deprecated
public class SkeletonNotFoundException extends RemoteException {

    /* 表示与 JDK 1.1.x 版本类的兼容性 */
    private static final long serialVersionUID = -7860299673822761231L;

    /**
     * 使用指定的详细消息构造 <code>SkeletonNotFoundException</code>。
     *
     * @param s 详细消息。
     * @since JDK1.1
     */
    public SkeletonNotFoundException(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和嵌套异常构造 <code>SkeletonNotFoundException</code>。
     *
     * @param s 详细消息。
     * @param ex 嵌套异常
     * @since JDK1.1
     */
    public SkeletonNotFoundException(String s, Exception ex) {
        super(s, ex);
    }
}
