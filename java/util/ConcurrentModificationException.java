/*
 * 版权所有 (c) 1997, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.util;

/**
 * 当方法检测到对象的并发修改而这种修改是不允许的时，可能会抛出此异常。
 * <p>
 * 例如，通常不允许一个线程在另一个线程正在遍历集合时修改该集合。在这种情况下，迭代的结果通常是未定义的。一些迭代器实现（包括 JRE 提供的所有通用集合实现的迭代器）可能会在检测到这种行为时抛出此异常。这样的迭代器被称为 <i>快速失败</i> 迭代器，因为它们会迅速且干净地失败，而不是冒险在未来的某个不确定时间点出现任意的、非确定性行为。
 * <p>
 * 请注意，此异常并不总是表示对象已被 <i>不同</i> 线程并发修改。如果单个线程发出的调用序列违反了对象的契约，该对象可能会抛出此异常。例如，如果一个线程在使用快速失败迭代器遍历集合时直接修改集合，迭代器将抛出此异常。
 *
 * <p>请注意，快速失败行为不能保证，因为在存在未同步的并发修改的情况下，通常不可能做出任何硬性保证。快速失败操作在尽力而为的基础上抛出 {@code ConcurrentModificationException}。因此，编写依赖于此异常正确性的程序是错误的：{@code ConcurrentModificationException} 应仅用于检测错误。</i>
 *
 * @author  Josh Bloch
 * @see     Collection
 * @see     Iterator
 * @see     Spliterator
 * @see     ListIterator
 * @see     Vector
 * @see     LinkedList
 * @see     HashSet
 * @see     Hashtable
 * @see     TreeMap
 * @see     AbstractList
 * @since   1.2
 */
public class ConcurrentModificationException extends RuntimeException {
    private static final long serialVersionUID = -3666751008965953603L;

    /**
     * 构造一个没有详细消息的 ConcurrentModificationException。
     */
    public ConcurrentModificationException() {
    }

    /**
     * 使用指定的详细消息构造一个 {@code ConcurrentModificationException}。
     *
     * @param message 与此异常相关的详细消息。
     */
    public ConcurrentModificationException(String message) {
        super(message);
    }

    /**
     * 使用指定的原因和详细消息 {@code (cause==null ? null : cause.toString())}（通常包含 {@code cause} 的类和详细消息）构造一个新的异常。
     *
     * @param  cause 原因（稍后通过 {@link Throwable#getCause()} 方法检索）。允许 {@code null} 值，表示原因不存在或未知。
     * @since  1.7
     */
    public ConcurrentModificationException(Throwable cause) {
        super(cause);
    }

    /**
     * 使用指定的详细消息和原因构造一个新的异常。
     *
     * <p>请注意，与 <code>cause</code> 关联的详细消息 <i>不会</i> 自动包含在此异常的详细消息中。
     *
     * @param  message 详细消息（稍后通过 {@link Throwable#getMessage()} 方法检索）。
     * @param  cause 原因（稍后通过 {@link Throwable#getCause()} 方法检索）。允许 {@code null} 值，表示原因不存在或未知。
     * @since 1.7
     */
    public ConcurrentModificationException(String message, Throwable cause) {
        super(message, cause);
    }
}