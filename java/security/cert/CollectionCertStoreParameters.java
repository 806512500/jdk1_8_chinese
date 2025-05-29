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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

/**
 * 用于 Collection {@code CertStore} 算法的参数。
 * <p>
 * 该类用于向 Collection {@code CertStore} 算法的实现提供必要的配置参数。此类中包含的唯一参数是
 * {@code Collection}，从该集合中将检索证书和 CRL。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 除非另有说明，否则本类中定义的方法不是线程安全的。需要并发访问单个对象的多个线程应同步并提供必要的锁定。
 * 每个操作独立对象的多个线程不需要同步。
 *
 * @since       1.4
 * @author      Steve Hanna
 * @see         java.util.Collection
 * @see         CertStore
 */
public class CollectionCertStoreParameters
    implements CertStoreParameters {

    private Collection<?> coll;

    /**
     * 创建一个 {@code CollectionCertStoreParameters} 实例，该实例将允许从指定的
     * {@code Collection} 中检索证书和 CRL。如果指定的
     * {@code Collection} 包含不是 {@code Certificate} 或 {@code CRL} 的对象，
     * 则该对象将被 Collection {@code CertStore} 忽略。
     * <p>
     * {@code Collection} <b>不会</b> 被复制。而是使用引用。这允许调用者随后向
     * {@code Collection} 中添加或删除 {@code Certificates} 或 {@code CRL}s，
     * 从而改变 Collection {@code CertStore} 可用的 {@code Certificates} 或 {@code CRL}s 集合。
     * Collection {@code CertStore} 不会修改 {@code Collection} 的内容。
     * <p>
     * 如果一个线程将修改 {@code Collection}，而另一个线程正在调用使用此
     * {@code Collection} 初始化的 Collection {@code CertStore} 的方法，则
     * {@code Collection} 必须具有快速失败的迭代器。
     *
     * @param collection 一个包含 {@code Certificate}s 和 {@code CRL}s 的 {@code Collection}
     * @exception NullPointerException 如果 {@code collection} 为
     * {@code null}
     */
    public CollectionCertStoreParameters(Collection<?> collection) {
        if (collection == null)
            throw new NullPointerException();
        coll = collection;
    }

    /**
     * 使用默认参数值（一个空且不可变的 {@code Collection}）创建一个
     * {@code CollectionCertStoreParameters} 实例。
     */
    public CollectionCertStoreParameters() {
        coll = Collections.EMPTY_SET;
    }

    /**
     * 返回从中检索 {@code Certificate}s 和 {@code CRL}s 的 {@code Collection}。
     * 这 <b>不是</b> {@code Collection} 的副本，而是一个引用。这允许调用者随后向
     * {@code Collection} 中添加或删除 {@code Certificates} 或 {@code CRL}s。
     *
     * @return the {@code Collection} (never null)
     */
    public Collection<?> getCollection() {
        return coll;
    }

    /**
     * 返回此对象的副本。请注意，只有 {@code Collection} 的引用被复制，而不是内容。
     *
     * @return the copy
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            /* 不可能发生 */
            throw new InternalError(e.toString(), e);
        }
    }

    /**
     * 返回描述参数的格式化字符串。
     *
     * @return a formatted string describing the parameters
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("CollectionCertStoreParameters: [\n");
        sb.append("  collection: " + coll + "\n");
        sb.append("]");
        return sb.toString();
    }
}
