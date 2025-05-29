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
 * 由 Doug Lea 编写，并在 JCP JSR-166 专家小组成员的帮助下发布到公共领域，如
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的。
 */

package java.util.concurrent.locks;

/**
 * 可以被线程独占拥有的同步器。此类提供了一个创建锁和相关同步器的基础，
 * 这些同步器可能涉及所有权的概念。{@code AbstractOwnableSynchronizer} 类本身不管理或
 * 使用这些信息。但是，子类和工具可以使用适当维护的值来帮助控制和监控访问并提供诊断。
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
     * 此方法不施加任何同步或 {@code volatile} 字段访问。
     * @param thread 所有者线程
     */
    protected final void setExclusiveOwnerThread(Thread thread) {
        exclusiveOwnerThread = thread;
    }

    /**
     * 返回由 {@code setExclusiveOwnerThread} 最后设置的线程，如果从未设置则返回 {@code null}。
     * 此方法不施加任何同步或 {@code volatile} 字段访问。
     * @return 所有者线程
     */
    protected final Thread getExclusiveOwnerThread() {
        return exclusiveOwnerThread;
    }
}
