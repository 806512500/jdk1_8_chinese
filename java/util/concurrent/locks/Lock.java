
/*
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

/*
 *
 *
 *
 *
 *
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并根据
 * http://creativecommons.org/publicdomain/zero/1.0/ 解释的公共领域发布。
 */

package java.util.concurrent.locks;
import java.util.concurrent.TimeUnit;

/**
 * {@code Lock} 实现提供了比使用 {@code synchronized} 方法和语句所能获得的更广泛的锁定操作。它们允许更灵活的结构，可能具有非常不同的属性，并且可能支持多个关联的
 * {@link Condition} 对象。
 *
 * <p>锁是一种用于控制多个线程访问共享资源的工具。通常，锁提供对共享资源的独占访问：一次只有一个线程可以获取锁，所有对共享资源的访问都需要首先获取锁。
 * 然而，某些锁可能允许对共享资源进行并发访问，例如 {@link ReadWriteLock} 的读锁。
 *
 * <p>使用 {@code synchronized} 方法或语句提供了与每个对象关联的隐式监视器锁的访问，但强制所有锁的获取和释放必须以块结构的方式进行：
 * 当获取多个锁时，它们必须以相反的顺序释放，所有锁必须在获取它们的相同词法作用域中释放。
 *
 * <p>虽然 {@code synchronized} 方法和语句的范围机制使得使用监视器锁编程更加容易，并帮助避免了许多涉及锁的常见编程错误，
 * 但有时您需要以更灵活的方式使用锁。例如，一些遍历并发访问数据结构的算法需要使用
 * “手递手”或“链锁”：你获取节点 A 的锁，然后节点 B，然后释放 A 并获取 C，然后释放 B 并获取 D，依此类推。
 * {@code Lock} 接口的实现通过允许在一个作用域中获取锁并在另一个作用域中释放锁，以及允许以任何顺序获取和释放多个锁，使得使用此类技术成为可能。
 *
 * <p>这种增加的灵活性带来了额外的责任。块结构锁定的缺失消除了与 {@code synchronized} 方法和语句相关的锁的自动释放。
 * 在大多数情况下，应使用以下惯用法：
 *
 *  <pre> {@code
 * Lock l = ...;
 * l.lock();
 * try {
 *   // 访问此锁保护的资源
 * } finally {
 *   l.unlock();
 * }}</pre>
 *
 * 当锁定和解锁发生在不同的作用域时，必须小心确保所有在持有锁时执行的代码都受到 try-finally 或 try-catch 的保护，以确保在必要时释放锁。
 *
 * <p>{@code Lock} 实现通过提供非阻塞尝试获取锁的方法（{@link #tryLock()}）、可以被中断的尝试获取锁的方法（{@link #lockInterruptibly}）
 * 以及可以超时的尝试获取锁的方法（{@link #tryLock(long, TimeUnit)}），提供了比使用 {@code synchronized} 方法和语句更多的功能。
 *
 * <p>{@code Lock} 类还可以提供与隐式监视器锁完全不同的行为和语义，例如保证的顺序、非可重入使用或死锁检测。
 * 如果实现提供了此类专门的语义，则实现必须记录这些语义。
 *
 * <p>请注意，{@code Lock} 实例只是普通对象，可以作为 {@code synchronized} 语句的目标。
 * 获取 {@code Lock} 实例的监视器锁与调用该实例的任何 {@link #lock} 方法之间没有指定的关系。
 * 建议为了避免混淆，除了在其自身实现中，不要以这种方式使用 {@code Lock} 实例。
 *
 * <p>除非另有说明，传递任何参数的 {@code null} 值将导致抛出 {@link NullPointerException}。
 *
 * <h3>内存同步</h3>
 *
 * <p>所有 {@code Lock} 实现 <em>必须</em> 强制执行与内置监视器锁提供的相同的内存同步语义，如
 * <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-17.html#jls-17.4">
 * 《Java 语言规范（17.4 内存模型）》</a> 中所述：
 * <ul>
 * <li>成功的 {@code lock} 操作具有与成功的 <em>Lock</em> 操作相同的内存同步效果。
 * <li>成功的 {@code unlock} 操作具有与成功的 <em>Unlock</em> 操作相同的内存同步效果。
 * </ul>
 *
 * 不成功的锁定和解锁操作，以及可重入的锁定/解锁操作，不需要任何内存同步效果。
 *
 * <h3>实现注意事项</h3>
 *
 * <p>三种形式的锁获取（可中断、不可中断和定时）可能在性能特性、排序保证或其他实现质量方面有所不同。
 * 此外，给定的 {@code Lock} 类可能无法提供对 <em>正在进行的</em> 锁获取的中断能力。因此，实现不必为所有三种形式的锁获取定义完全相同的保证或语义，
 * 也不必支持对正在进行的锁获取的中断。实现必须清楚地记录每个锁定方法提供的语义和保证。它还必须遵守此接口中定义的中断语义，以支持锁获取的中断：要么完全支持，要么仅在方法入口处支持。
 *
 * <p>由于中断通常意味着取消，并且中断检查通常很少，实现可以优先响应中断而不是正常的方法返回。即使可以证明中断发生在可能解除线程阻塞的其他操作之后，也是如此。
 * 实现应记录此行为。
 *
 * @see ReentrantLock
 * @see Condition
 * @see ReadWriteLock
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface Lock {

                /**
     * 获取锁。
     *
     * <p>如果锁不可用，则当前线程将被禁用以进行线程调度，并处于休眠状态，直到锁被获取。
     *
     * <p><b>实现注意事项</b>
     *
     * <p>锁的实现可能能够检测锁的错误使用，例如会导致死锁的调用，并在这些情况下抛出（未检查的）异常。该锁的实现必须记录这些情况和异常类型。
     */
    void lock();

    /**
     * 如果当前线程未被 {@linkplain Thread#interrupt 中断}，则获取锁。
     *
     * <p>如果锁可用，则立即获取锁并返回。
     *
     * <p>如果锁不可用，则当前线程将被禁用以进行线程调度，并处于休眠状态，直到以下两种情况之一发生：
     *
     * <ul>
     * <li>当前线程获取了锁；或
     * <li>其他线程 {@linkplain Thread#interrupt 中断} 了当前线程，并且支持锁获取的中断。
     * </ul>
     *
     * <p>如果当前线程：
     * <ul>
     * <li>在进入此方法时其中断状态已设置；或
     * <li>在获取锁时被 {@linkplain Thread#interrupt 中断}，并且支持锁获取的中断，
     * </ul>
     * 则抛出 {@link InterruptedException} 并清除当前线程的中断状态。
     *
     * <p><b>实现注意事项</b>
     *
     * <p>在某些实现中，中断锁获取可能不可能，如果可能，可能是一个昂贵的操作。程序员应该意识到这一点。实现应记录这种情况。
     *
     * <p>实现可以优先响应中断而不是正常方法返回。
     *
     * <p>锁的实现可能能够检测锁的错误使用，例如会导致死锁的调用，并在这些情况下抛出（未检查的）异常。该锁的实现必须记录这些情况和异常类型。
     *
     * @throws InterruptedException 如果当前线程在获取锁时被中断（并且支持锁获取的中断）
     */
    void lockInterruptibly() throws InterruptedException;

    /**
     * 仅在调用时锁为空闲时获取锁。
     *
     * <p>如果锁可用，则立即获取锁并返回 {@code true}。
     * 如果锁不可用，则此方法将立即返回 {@code false}。
     *
     * <p>此方法的典型用法如下：
     *  <pre> {@code
     * Lock lock = ...;
     * if (lock.tryLock()) {
     *   try {
     *     // 操作受保护的状态
     *   } finally {
     *     lock.unlock();
     *   }
     * } else {
     *   // 执行替代操作
     * }}</pre>
     *
     * 此用法确保如果获取了锁，则解锁；如果没有获取锁，则不尝试解锁。
     *
     * @return 如果锁被获取则返回 {@code true}，否则返回 {@code false}
     */
    boolean tryLock();

    /**
     * 如果锁在给定的等待时间内为空闲，并且当前线程未被 {@linkplain Thread#interrupt 中断}，则获取锁。
     *
     * <p>如果锁可用，此方法将立即返回 {@code true}。
     * 如果锁不可用，则当前线程将被禁用以进行线程调度，并处于休眠状态，直到以下三种情况之一发生：
     * <ul>
     * <li>当前线程获取了锁；或
     * <li>其他线程 {@linkplain Thread#interrupt 中断} 了当前线程，并且支持锁获取的中断；或
     * <li>指定的等待时间已过期
     * </ul>
     *
     * <p>如果锁被获取，则返回值为 {@code true}。
     *
     * <p>如果当前线程：
     * <ul>
     * <li>在进入此方法时其中断状态已设置；或
     * <li>在获取锁时被 {@linkplain Thread#interrupt 中断}，并且支持锁获取的中断，
     * </ul>
     * 则抛出 {@link InterruptedException} 并清除当前线程的中断状态。
     *
     * <p>如果指定的等待时间已过期，则返回值为 {@code false}。
     * 如果时间小于或等于零，方法将不会等待。
     *
     * <p><b>实现注意事项</b>
     *
     * <p>在某些实现中，中断锁获取可能不可能，如果可能，可能是一个昂贵的操作。
     * 程序员应该意识到这一点。实现应记录这种情况。
     *
     * <p>实现可以优先响应中断而不是正常方法返回，或报告超时。
     *
     * <p>锁的实现可能能够检测锁的错误使用，例如会导致死锁的调用，并在这些情况下抛出（未检查的）异常。该锁的实现必须记录这些情况和异常类型。
     *
     * @param time 等待锁的最大时间
     * @param unit {@code time} 参数的时间单位
     * @return 如果锁被获取则返回 {@code true}，如果等待时间已过期而未获取锁则返回 {@code false}
     *
     * @throws InterruptedException 如果当前线程在获取锁时被中断（并且支持锁获取的中断）
     */
    boolean tryLock(long time, TimeUnit unit) throws InterruptedException;

                /**
     * 释放锁。
     *
     * <p><b>实现注意事项</b>
     *
     * <p>{@code Lock} 实现通常会对哪个线程可以释放锁施加限制（通常只有锁的持有者可以释放它），如果违反了这些限制，可能会抛出
     * 一个（未检查的）异常。任何限制和异常类型必须由该 {@code Lock} 实现文档化。
     */
    void unlock();

    /**
     * 返回一个绑定到此 {@code Lock} 实例的新 {@link Condition} 实例。
     *
     * <p>在等待条件之前，当前线程必须持有锁。
     * 调用 {@link Condition#await()} 将在等待前原子地释放锁，并在等待返回前重新获取锁。
     *
     * <p><b>实现注意事项</b>
     *
     * <p>{@link Condition} 实例的具体操作取决于 {@code Lock} 实现，并且必须由该
     * 实现文档化。
     *
     * @return 一个绑定到此 {@code Lock} 实例的新 {@link Condition} 实例
     * @throws UnsupportedOperationException 如果此 {@code Lock} 实现不支持条件
     */
    Condition newCondition();
}
