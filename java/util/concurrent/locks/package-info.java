/*
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

/*
 *
 *
 *
 *
 *
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并发布到公共领域，如
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的。
 */

/**
 * 提供锁定和等待条件的框架接口和类，这些条件与内置同步和监视器不同。该框架允许在使用锁和条件时具有更大的灵活性，但代价是语法更加笨拙。
 *
 * <p>{@link java.util.concurrent.locks.Lock} 接口支持语义不同的锁定规则（可重入、公平等），并且可以在非块结构上下文中使用，包括手递手和锁重新排序算法。主要实现是 {@link java.util.concurrent.locks.ReentrantLock}。
 *
 * <p>{@link java.util.concurrent.locks.ReadWriteLock} 接口类似地定义了可以由读者共享但对写者独占的锁。只提供了一种实现，即 {@link
 * java.util.concurrent.locks.ReentrantReadWriteLock}，因为它涵盖了大多数标准使用场景。但程序员可以创建自己的实现以满足非标准需求。
 *
 * <p>{@link java.util.concurrent.locks.Condition} 接口描述了可以与锁关联的条件变量。这些在用法上类似于使用 {@code Object.wait} 访问的隐式监视器，但提供了扩展功能。
 * 特别是，多个 {@code Condition} 对象可以与单个 {@code Lock} 关联。为了避免兼容性问题，{@code Condition} 方法的名称与相应的 {@code Object} 版本不同。
 *
 * <p>{@link java.util.concurrent.locks.AbstractQueuedSynchronizer} 类作为定义依赖于排队阻塞线程的锁和其他同步器的有用超类。{@link
 * java.util.concurrent.locks.AbstractQueuedLongSynchronizer} 类提供了相同的功能，但扩展了对 64 位同步状态的支持。两者都扩展了类 {@link
 * java.util.concurrent.locks.AbstractOwnableSynchronizer}，这是一个简单的类，有助于记录当前持有独占同步的线程。{@link java.util.concurrent.locks.LockSupport}
 * 类提供了对开发人员实现自己的自定义锁类有用的低级阻塞和解除阻塞支持。
 *
 * @since 1.5
 */
package java.util.concurrent.locks;
