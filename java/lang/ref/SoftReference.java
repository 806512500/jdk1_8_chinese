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
 * 软引用对象，这些对象在内存需求响应时由垃圾收集器自行决定清理。软引用通常用于实现内存敏感的缓存。
 *
 * <p> 假设垃圾收集器在某个时间点确定一个对象是<a href="package-summary.html#reachability">软可到达的</a>。在那个时间点，它可以选择原子地清除所有指向该对象的软引用，以及所有通过强引用链从该对象可到达的其他软可到达对象的软引用。同时或稍后，它将把那些新清除的已注册到引用队列的软引用入队。
 *
 * <p> 在虚拟机抛出<code>OutOfMemoryError</code>之前，所有指向软可到达对象的软引用都保证已被清除。否则，对软引用何时清除或对不同对象的此类引用集何时清除的顺序不作任何限制。然而，虚拟机实现应偏向于不清除最近创建或最近使用的软引用。
 *
 * <p> 该类的直接实例可用于实现简单的缓存；该类或派生子类也可用于更复杂的数据结构中实现更复杂的缓存。只要软引用的引用对象是强可达的，即实际上正在使用，软引用就不会被清除。因此，复杂的缓存可以通过保持对最近使用条目的强引用，防止这些条目被丢弃，而让剩余条目由垃圾收集器自行决定丢弃。
 *
 * @author   Mark Reinhold
 * @since    1.2
 */

public class SoftReference<T> extends Reference<T> {

    /**
     * 时间戳时钟，由垃圾收集器更新
     */
    static private long clock;

    /**
     * 每次调用 get 方法时更新的时间戳。虚拟机在选择要清除的软引用时可能会使用此字段，但不要求必须这样做。
     */
    private long timestamp;

    /**
     * 创建一个新的软引用，该引用指向给定的对象。新引用未注册到任何队列。
     *
     * @param referent 新软引用将引用的对象
     */
    public SoftReference(T referent) {
        super(referent);
        this.timestamp = clock;
    }

    /**
     * 创建一个新的软引用，该引用指向给定的对象并注册到给定的队列。
     *
     * @param referent 新软引用将引用的对象
     * @param q 要注册引用的队列，或 <tt>null</tt> 如果不需要注册
     *
     */
    public SoftReference(T referent, ReferenceQueue<? super T> q) {
        super(referent, q);
        this.timestamp = clock;
    }

    /**
     * 返回此引用对象的引用对象。如果此引用对象已被程序或垃圾收集器清除，则此方法返回 <code>null</code>。
     *
     * @return   此引用引用的对象，或
     *           <code>null</code> 如果此引用对象已被清除
     */
    public T get() {
        T o = super.get();
        if (o != null && this.timestamp != clock)
            this.timestamp = clock;
        return o;
    }

}
