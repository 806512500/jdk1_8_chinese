/*
 * 版权所有 (c) 2000, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.security.cert;

/**
 * 定义了一组用于选择 {@code Certificate} 的标准的选择器。实现此接口的类
 * 通常用于指定应从 {@code CertStore} 中检索哪些 {@code Certificate}。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 除非另有说明，此接口中定义的方法不是线程安全的。多个线程需要并发访问单个
 * 对象时，应自行同步并提供必要的锁定。每个操作不同对象的多个线程无需同步。
 *
 * @see Certificate
 * @see CertStore
 * @see CertStore#getCertificates
 *
 * @author      Steve Hanna
 * @since       1.4
 */
public interface CertSelector extends Cloneable {

    /**
     * 决定是否选择一个 {@code Certificate}。
     *
     * @param   cert    要检查的 {@code Certificate}
     * @return  如果应选择 {@code Certificate}，则返回 {@code true}，否则返回 {@code false}
     */
    boolean match(Certificate cert);

    /**
     * 创建此 {@code CertSelector} 的副本。对副本的修改不会影响原始对象，反之亦然。
     *
     * @return 此 {@code CertSelector} 的副本
     */
    Object clone();
}
