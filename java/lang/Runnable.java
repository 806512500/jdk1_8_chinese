/*
 * 版权所有 (c) 1994, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.lang;

/**
 * <code>Runnable</code> 接口应由任何希望其实例由线程执行的类实现。该类必须定义一个无参数的方法，称为 <code>run</code>。
 * <p>
 * 该接口旨在为希望在活跃时执行代码的对象提供一个通用协议。例如，<code>Runnable</code> 由类 <code>Thread</code> 实现。
 * 活跃仅仅意味着线程已启动且尚未停止。
 * <p>
 * 此外，<code>Runnable</code> 提供了一种方式，使类可以在不继承 <code>Thread</code> 的情况下保持活跃。实现 <code>Runnable</code> 的类
 * 可以通过实例化一个 <code>Thread</code> 实例并将其自身作为目标传递来运行。在大多数情况下，如果仅计划覆盖 <code>run()</code>
 * 方法而不覆盖其他 <code>Thread</code> 方法，应使用 <code>Runnable</code> 接口。这是很重要的，因为除非程序员打算修改或增强类的
 * 核心行为，否则不应继承类。
 *
 * @author  Arthur van Hoff
 * @see     java.lang.Thread
 * @see     java.util.concurrent.Callable
 * @since   JDK1.0
 */
@FunctionalInterface
public interface Runnable {
    /**
     * 当实现 <code>Runnable</code> 接口的对象用于创建线程时，启动该线程会导致该对象的 <code>run</code> 方法在单独执行的线程中被调用。
     * <p>
     * <code>run</code> 方法的一般契约是它可以采取任何行动。
     *
     * @see     java.lang.Thread#run()
     */
    public abstract void run();
}
