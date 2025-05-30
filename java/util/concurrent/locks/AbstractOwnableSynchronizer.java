/*
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

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.locks;

/**
 * 一个可以被线程独占拥有的同步器。此类提供了一个基础，用于创建可能涉及所有权概念的锁和相关同步器。
 * {@code AbstractOwnableSynchronizer} 类本身不管理或使用这些信息。但是，子类和工具可以使用适当维护的值来帮助控制和监控访问并提供诊断信息。
 *
 * @since 1.6
 * @author Doug Lea
 */
public abstract class AbstractOwnableSynchronizer
    implements java.io.Serializable {

    /** 即使所有字段都是瞬态的，也使用序列化 ID。 */
    private static final long serialVersionUID = 3737899427754241961L;

    /**
     * 用于子类的空构造函数。
     */
    protected AbstractOwnableSynchronizer() { }

    /**
     * 当前独占模式同步的所有者。
     */
    private transient Thread exclusiveOwnerThread;

    /**
     * 设置当前拥有独占访问权限的线程。
     * {@code null} 参数表示没有线程拥有访问权限。
     * 此方法不强制执行任何同步或 {@code volatile} 字段访问。
     * @param thread 拥有线程
     */
    protected final void setExclusiveOwnerThread(Thread thread) {
        exclusiveOwnerThread = thread;
    }

    /**
     * 返回由 {@code setExclusiveOwnerThread} 最后设置的线程，如果从未设置则返回 {@code null}。
     * 此方法不强制执行任何同步或 {@code volatile} 字段访问。
     * @return 拥有线程
     */
    protected final Thread getExclusiveOwnerThread() {
        return exclusiveOwnerThread;
    }
}
