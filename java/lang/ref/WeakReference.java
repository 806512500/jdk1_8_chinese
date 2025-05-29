/*
 * 版权所有 (c) 1997, 2003, Oracle 和/或其附属公司。保留所有权利。
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

package java.lang.ref;


/**
 * 弱引用对象，这些对象不会阻止其引用对象被最终化、最终化和回收。弱引用最常用于实现规范映射。
 *
 * <p> 假设垃圾收集器在某个时间点确定一个对象是<a href="package-summary.html#reachability">弱可到达的</a>。在那个时间点，它将原子地清除对该对象的所有弱引用以及通过强引用和软引用链从该对象可到达的所有其他弱可到达对象的所有弱引用。同时，它将声明所有先前的弱可到达对象为可最终化。同时或稍后，它将把那些新清除的弱引用（如果已注册到引用队列）入队。
 *
 * @author   Mark Reinhold
 * @since    1.2
 */

public class WeakReference<T> extends Reference<T> {

    /**
     * 创建一个新的弱引用，该引用指向给定对象。新的引用未注册到任何队列。
     *
     * @param referent 新的弱引用将指向的对象
     */
    public WeakReference(T referent) {
        super(referent);
    }

    /**
     * 创建一个新的弱引用，该引用指向给定对象并注册到给定队列。
     *
     * @param referent 新的弱引用将指向的对象
     * @param q 要注册引用的队列，或 <tt>null</tt> 如果不需要注册
     */
    public WeakReference(T referent, ReferenceQueue<? super T> q) {
        super(referent, q);
    }

}
