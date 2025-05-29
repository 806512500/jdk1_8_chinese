
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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并根据以下网址的解释发布到公共领域：
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic;

/**
 * 一个 {@code AtomicStampedReference} 维护一个对象引用
 * 以及一个可以原子更新的整数“戳记”。
 *
 * <p>实现说明：此实现通过创建表示“装箱”
 * [引用, 整数] 对的内部对象来维护带戳记的引用。
 *
 * @since 1.5
 * @author Doug Lea
 * @param <V> 此引用所指向的对象类型
 */
public class AtomicStampedReference<V> {

    private static class Pair<T> {
        final T reference;
        final int stamp;
        private Pair(T reference, int stamp) {
            this.reference = reference;
            this.stamp = stamp;
        }
        static <T> Pair<T> of(T reference, int stamp) {
            return new Pair<T>(reference, stamp);
        }
    }

    private volatile Pair<V> pair;

    /**
     * 使用给定的初始值创建一个新的 {@code AtomicStampedReference}。
     *
     * @param initialRef 初始引用
     * @param initialStamp 初始戳记
     */
    public AtomicStampedReference(V initialRef, int initialStamp) {
        pair = Pair.of(initialRef, initialStamp);
    }

    /**
     * 返回引用的当前值。
     *
     * @return 引用的当前值
     */
    public V getReference() {
        return pair.reference;
    }

    /**
     * 返回戳记的当前值。
     *
     * @return 戳记的当前值
     */
    public int getStamp() {
        return pair.stamp;
    }

    /**
     * 返回引用和戳记的当前值。
     * 典型用法是 {@code int[1] holder; ref = v.get(holder); }。
     *
     * @param stampHolder 大小至少为一的数组。返回时，
     * {@code stampholder[0]} 将保存戳记的值。
     * @return 引用的当前值
     */
    public V get(int[] stampHolder) {
        Pair<V> pair = this.pair;
        stampHolder[0] = pair.stamp;
        return pair.reference;
    }

    /**
     * 如果当前引用 {@code ==} 预期引用
     * 且当前戳记等于预期戳记，则原子地将引用和戳记的值
     * 设置为给定的更新值。
     *
     * <p><a href="package-summary.html#weakCompareAndSet">可能会无故失败且不提供顺序保证</a>，因此很少是 {@code compareAndSet} 的适当替代。
     *
     * @param expectedReference 引用的预期值
     * @param newReference 引用的新值
     * @param expectedStamp 戳记的预期值
     * @param newStamp 戳记的新值
     * @return 如果成功则返回 {@code true}
     */
    public boolean weakCompareAndSet(V   expectedReference,
                                     V   newReference,
                                     int expectedStamp,
                                     int newStamp) {
        return compareAndSet(expectedReference, newReference,
                             expectedStamp, newStamp);
    }

    /**
     * 如果当前引用 {@code ==} 预期引用
     * 且当前戳记等于预期戳记，则原子地将引用和戳记的值
     * 设置为给定的更新值。
     *
     * @param expectedReference 引用的预期值
     * @param newReference 引用的新值
     * @param expectedStamp 戳记的预期值
     * @param newStamp 戳记的新值
     * @return 如果成功则返回 {@code true}
     */
    public boolean compareAndSet(V   expectedReference,
                                 V   newReference,
                                 int expectedStamp,
                                 int newStamp) {
        Pair<V> current = pair;
        return
            expectedReference == current.reference &&
            expectedStamp == current.stamp &&
            ((newReference == current.reference &&
              newStamp == current.stamp) ||
             casPair(current, Pair.of(newReference, newStamp)));
    }

    /**
     * 无条件地设置引用和戳记的值。
     *
     * @param newReference 引用的新值
     * @param newStamp 戳记的新值
     */
    public void set(V newReference, int newStamp) {
        Pair<V> current = pair;
        if (newReference != current.reference || newStamp != current.stamp)
            this.pair = Pair.of(newReference, newStamp);
    }

    /**
     * 如果当前引用 {@code ==} 预期引用，则原子地将戳记的值
     * 设置为给定的更新值。任何给定的调用可能会失败（返回 {@code false}），
     * 但在当前值持有预期值且没有其他线程也在尝试设置值的情况下重复调用最终会成功。
     *
     * @param expectedReference 引用的预期值
     * @param newStamp 戳记的新值
     * @return 如果成功则返回 {@code true}
     */
    public boolean attemptStamp(V expectedReference, int newStamp) {
        Pair<V> current = pair;
        return
            expectedReference == current.reference &&
            (newStamp == current.stamp ||
             casPair(current, Pair.of(expectedReference, newStamp)));
    }

    // Unsafe 机制

    private static final sun.misc.Unsafe UNSAFE = sun.misc.Unsafe.getUnsafe();
    private static final long pairOffset =
        objectFieldOffset(UNSAFE, "pair", AtomicStampedReference.class);

    private boolean casPair(Pair<V> cmp, Pair<V> val) {
        return UNSAFE.compareAndSwapObject(this, pairOffset, cmp, val);
    }


                static long objectFieldOffset(sun.misc.Unsafe UNSAFE,
                                  String field, Class<?> klazz) {
        try {
            return UNSAFE.objectFieldOffset(klazz.getDeclaredField(field));
        } catch (NoSuchFieldException e) {
            // 将异常转换为相应的错误
            NoSuchFieldError error = new NoSuchFieldError(field);
            error.initCause(e);
            throw error;
        }
    }
}
