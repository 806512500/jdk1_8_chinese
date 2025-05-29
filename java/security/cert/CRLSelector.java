/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security.cert;

/**
 * 一个定义了选择 {@code CRL} 的一组标准的选择器。
 * 实现此接口的类通常用于指定应从 {@code CertStore} 中检索哪些 {@code CRL}。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 除非另有说明，否则此接口中定义的方法不是线程安全的。需要并发访问单个对象的多个线程应自行同步并提供必要的锁定。每个操作不同对象的多个线程不需要同步。
 *
 * @see CRL
 * @see CertStore
 * @see CertStore#getCRLs
 *
 * @author      Steve Hanna
 * @since       1.4
 */
public interface CRLSelector extends Cloneable {

    /**
     * 决定是否选择一个 {@code CRL}。
     *
     * @param   crl     要检查的 {@code CRL}
     * @return  如果应选择 {@code CRL}，则返回 {@code true}，否则返回 {@code false}
     */
    boolean match(CRL crl);

    /**
     * 创建此 {@code CRLSelector} 的副本。对副本的修改不会影响原始对象，反之亦然。
     *
     * @return 此 {@code CRLSelector} 的副本
     */
    Object clone();
}
