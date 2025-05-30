/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.management;

import javax.management.openmbean.CompositeData;
import java.util.concurrent.locks.*;
import sun.management.LockInfoCompositeData;

/**
 * 关于 <em>锁</em> 的信息。锁可以是内置对象监视器、
 * <em>可拥有同步器</em> 或与同步器关联的 {@link Condition Condition} 对象。
 * <p>
 * <a name="OwnableSynchronizer">可拥有同步器</a> 是
 * 可能被线程独占拥有的同步器，并使用
 * {@link AbstractOwnableSynchronizer AbstractOwnableSynchronizer}
 * （或其子类）来实现其同步属性。
 * {@link ReentrantLock ReentrantLock} 和
 * {@link ReentrantReadWriteLock ReentrantReadWriteLock} 是
 * 平台提供的两个可拥有同步器的例子。
 *
 * <h3><a name="MappedType">MXBean 映射</a></h3>
 * <tt>LockInfo</tt> 被映射为一个 {@link CompositeData CompositeData}
 * ，具体映射方式请参见 {@link #from from} 方法。
 *
 * @see java.util.concurrent.locks.AbstractOwnableSynchronizer
 * @see java.util.concurrent.locks.Condition
 *
 * @author  Mandy Chung
 * @since   1.6
 */

public class LockInfo {

    private String className;
    private int    identityHashCode;

    /**
     * 构造一个 <tt>LockInfo</tt> 对象。
     *
     * @param className 锁对象的类的完全限定名。
     * @param identityHashCode 锁对象的 {@link System#identityHashCode
     *                         身份哈希码}。
     */
    public LockInfo(String className, int identityHashCode) {
        if (className == null) {
            throw new NullPointerException("参数 className 不能为 null");
        }
        this.className = className;
        this.identityHashCode = identityHashCode;
    }

    /**
     * 包私有的构造函数
     */
    LockInfo(Object lock) {
        this.className = lock.getClass().getName();
        this.identityHashCode = System.identityHashCode(lock);
    }

    /**
     * 返回锁对象的类的完全限定名。
     *
     * @return 锁对象的类的完全限定名。
     */
    public String getClassName() {
        return className;
    }

    /**
     * 返回锁对象的 {@link System#identityHashCode} 方法返回的身份哈希码。
     *
     * @return 锁对象的身份哈希码。
     */
    public int getIdentityHashCode() {
        return identityHashCode;
    }

    /**
     * 返回由给定的 {@code CompositeData} 表示的 {@code LockInfo} 对象。
     * 给定的 {@code CompositeData} 必须包含以下属性：
     * <blockquote>
     * <table border summary="给定的 CompositeData 包含的属性和类型">
     * <tr>
     *   <th align=left>属性名</th>
     *   <th align=left>类型</th>
     * </tr>
     * <tr>
     *   <td>className</td>
     *   <td><tt>java.lang.String</tt></td>
     * </tr>
     * <tr>
     *   <td>identityHashCode</td>
     *   <td><tt>java.lang.Integer</tt></td>
     * </tr>
     * </table>
     * </blockquote>
     *
     * @param cd 表示一个 {@code LockInfo} 的 {@code CompositeData}
     *
     * @throws IllegalArgumentException 如果 {@code cd} 不表示具有上述属性的 {@code LockInfo}。
     * @return 如果 {@code cd} 不为 {@code null}，则返回由 {@code cd} 表示的 {@code LockInfo} 对象；
     *         否则返回 {@code null}。
     *
     * @since 1.8
     */
    public static LockInfo from(CompositeData cd) {
        if (cd == null) {
            return null;
        }

        if (cd instanceof LockInfoCompositeData) {
            return ((LockInfoCompositeData) cd).getLockInfo();
        } else {
            return LockInfoCompositeData.toLockInfo(cd);
        }
    }

    /**
     * 返回锁的字符串表示形式。返回的字符串表示形式由锁对象的类名、
     * @ 符号和对象的 <em>身份</em> 哈希码的无符号十六进制表示组成。
     * 该方法返回的字符串等于以下值：
     * <blockquote>
     * <pre>
     * lock.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(lock))
     * </pre></blockquote>
     * 其中 <tt>lock</tt> 是锁对象。
     *
     * @return 锁的字符串表示形式。
     */
    public String toString() {
        return className + '@' + Integer.toHexString(identityHashCode);
    }
}
