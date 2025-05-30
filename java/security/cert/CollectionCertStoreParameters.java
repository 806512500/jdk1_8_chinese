/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.security.cert;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

/**
 * 用于作为 Collection {@code CertStore} 算法的输入参数。
 * <p>
 * 该类用于向 Collection {@code CertStore} 算法的实现提供必要的配置参数。此类中包含的唯一参数是
 * {@code Collection}，从该集合中将检索证书和 CRL。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 除非另有说明，否则本类中定义的方法不是线程安全的。需要并发访问单个对象的多个线程应相互同步并提供必要的锁定。每个线程操作不同对象的多个线程不需要同步。
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
     * {@code Collection} 包含不是 {@code Certificate} 或 {@code CRL} 的对象，该对象将被
     * Collection {@code CertStore} 忽略。
     * <p>
     * 该 {@code Collection} <b>不会</b> 被复制。而是使用引用。这允许调用者随后向
     * {@code Collection} 中添加或删除 {@code Certificates} 或 {@code CRL}s，从而改变
     * Collection {@code CertStore} 可用的 {@code Certificates} 或 {@code CRL}s 集合。Collection {@code CertStore}
     * 不会修改 {@code Collection} 的内容。
     * <p>
     * 如果一个线程将修改 {@code Collection}，而另一个线程正在调用已使用此
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
     * 使用默认参数值（一个空的且不可变的
     * {@code Collection}）创建一个 {@code CollectionCertStoreParameters} 实例。
     */
    public CollectionCertStoreParameters() {
        coll = Collections.EMPTY_SET;
    }

    /**
     * 返回从中检索 {@code Certificate}s
     * 和 {@code CRL}s 的 {@code Collection}。这 <b>不是</b> {@code Collection} 的副本，而是一个引用。这允许调用者
     * 随后向 {@code Collection} 中添加或删除 {@code Certificates} 或
     * {@code CRL}s。
     *
     * @return the {@code Collection} (never null)
     */
    public Collection<?> getCollection() {
        return coll;
    }

    /**
     * 返回此对象的副本。请注意，仅复制 {@code Collection} 的引用，而不是其内容。
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
