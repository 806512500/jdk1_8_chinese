/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.management;

import javax.management.openmbean.CompositeData;
import java.util.concurrent.locks.*;
import sun.management.LockInfoCompositeData;

/**
 * 锁的信息。锁可以是内置的对象监视器、<em>可拥有的同步器</em>，或者是与同步器关联的 {@link Condition Condition} 对象。
 * <p>
 * <a name="OwnableSynchronizer">可拥有的同步器</a> 是一个可以被线程独占拥有的同步器，并使用
 * {@link AbstractOwnableSynchronizer AbstractOwnableSynchronizer}（或其子类）来实现其同步特性。
 * {@link ReentrantLock ReentrantLock} 和
 * {@link ReentrantReadWriteLock ReentrantReadWriteLock} 是平台提供的两个可拥有的同步器示例。
 *
 * <h3><a name="MappedType">MXBean 映射</a></h3>
 * <tt>LockInfo</tt> 被映射为一个 {@link CompositeData CompositeData}，具体映射方式在 {@link #from from} 方法中指定。
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
     * 返回锁对象的身份哈希码，该哈希码由 {@link System#identityHashCode} 方法返回。
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
     * 返回锁的字符串表示形式。返回的字符串表示形式由锁对象的类名、@ 符号和对象的 <em>身份</em> 哈希码的无符号十六进制表示组成。
     * 此方法返回的字符串等于以下值：
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
