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
 * 幽灵引用对象，在收集器确定它们的引用对象可能被回收后被入队。幽灵引用通常用于以比 Java 终结机制更灵活的方式调度预终结清理操作。
 *
 * <p> 如果垃圾收集器在某个时间点确定幽灵引用的引用对象是<a
 * href="package-summary.html#reachability">幽灵可达</a>的，那么在该时间点或稍后的时间点，它将把引用入队。
 *
 * <p> 为了确保可回收对象保持可回收，幽灵引用的引用对象不能被检索：幽灵引用的 <code>get</code> 方法总是返回 <code>null</code>。
 *
 * <p> 与软引用和弱引用不同，幽灵引用不会在入队时被垃圾收集器自动清除。通过幽灵引用可达的对象将保持可达状态，直到所有此类引用被清除或自身变得不可达。
 *
 * @author   Mark Reinhold
 * @since    1.2
 */

public class PhantomReference<T> extends Reference<T> {

    /**
     * 返回此引用对象的引用对象。由于幽灵引用的引用对象总是不可访问的，此方法总是返回 <code>null</code>。
     *
     * @return  <code>null</code>
     */
    public T get() {
        return null;
    }

    /**
     * 创建一个新的幽灵引用，该引用指向给定的对象，并注册到给定的队列。
     *
     * <p> 可以创建一个队列为 <tt>null</tt> 的幽灵引用，但这样的引用完全无用：它的 <tt>get</tt>
     * 方法将始终返回 null，并且由于它没有队列，它永远不会被入队。
     *
     * @param referent 新的幽灵引用将引用的对象
     * @param q 要注册引用的队列，或 <tt>null</tt> 如果不需要注册
     */
    public PhantomReference(T referent, ReferenceQueue<? super T> q) {
        super(referent, q);
    }

}
