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
 * 一个 {@code ReadWriteLock} 维护一对关联的 {@link
 * Lock 锁}，一个用于只读操作，一个用于写操作。
 * {@link #readLock 读锁} 可以同时被多个读线程持有，前提是没有写线程。 {@link #writeLock 写锁} 是独占的。
 *
 * <p>所有 {@code ReadWriteLock} 实现必须保证 {@code writeLock} 操作的内存同步效果
 * （如 {@link Lock} 接口所指定）也适用于关联的 {@code readLock}。也就是说，成功获取读锁的线程将看到所有先前释放写锁时所做的更新。
 *
 * <p>读写锁允许对共享数据的访问具有比互斥锁更高的并发度。
 * 它利用了这样一个事实：虽然一次只能有一个线程（一个 <em>写线程</em>）修改共享数据，但在许多情况下，任何数量的线程可以同时读取数据（因此称为 <em>读线程</em>）。
 * 从理论上讲，使用读写锁允许的并发度增加将比使用互斥锁时的性能有所提高。但在实践中，这种并发度的增加只有在多处理器系统上才会完全实现，并且只有在共享数据的访问模式适合的情况下才会实现。
 *
 * <p>是否使用读写锁比互斥锁能提高性能取决于数据被读取的频率与被修改的频率、读写操作的持续时间以及数据的竞争程度——即同时尝试读取或写入数据的线程数量。
 * 例如，一个最初用数据填充并此后很少修改，但经常搜索的集合（如某种目录）是使用读写锁的理想候选者。然而，如果更新变得频繁，数据大部分时间都处于独占锁定状态，几乎没有并发度的提高。此外，如果读操作太短，读写锁实现的开销（比互斥锁复杂）可能会主导执行成本，特别是许多读写锁实现仍然在一小段代码中序列化所有线程。最终，只有通过分析和测量才能确定读写锁是否适合您的应用程序。
 *
 * <p>尽管读写锁的基本操作是直接的，但实现必须做出许多策略决策，这些决策可能会影响读写锁在给定应用程序中的有效性。这些策略决策的例子包括：
 * <ul>
 * <li>确定在写线程释放写锁时，是授予读锁还是写锁，当读线程和写线程都在等待时。常见的做法是偏向写线程，因为预期写操作是短暂且不频繁的。偏向读线程的情况较少，因为如果读线程频繁且持续时间较长，可能会导致写线程长时间延迟。还可以实现公平或“按顺序”的策略。
 *
 * <li>确定当读线程活跃且写线程等待时，请求读锁的读线程是否被授予读锁。偏向读线程可能会无限期地延迟写线程，而偏向写线程可能会减少并发的潜力。
 *
 * <li>确定锁是否可重入：持有写锁的线程是否可以重新获取它？它是否可以在持有写锁的同时获取读锁？读锁本身是否可重入？
 *
 * <li>写锁是否可以降级为读锁而不允许中间的写线程？读锁是否可以升级为写锁，优先于其他等待的读线程或写线程？
 *
 * </ul>
 * 在评估给定实现是否适合您的应用程序时，您应该考虑所有这些因素。
 *
 * @see ReentrantReadWriteLock
 * @see Lock
 * @see ReentrantLock
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface ReadWriteLock {
    /**
     * 返回用于读取的锁。
     *
     * @return 用于读取的锁
     */
    Lock readLock();

    /**
     * 返回用于写入的锁。
     *
     * @return 用于写入的锁
     */
    Lock writeLock();
}
