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

package java.util.concurrent.atomic;

/**
 * 一个 {@code AtomicMarkableReference} 维护一个对象引用
 * 以及一个可以原子更新的标记位。
 *
 * <p>实现说明：此实现通过创建表示“装箱”
 * [引用, 布尔] 对的内部对象来维护可标记的引用。
 *
 * @since 1.5
 * @author Doug Lea
 * @param <V> 此引用所指向的对象类型
 */
public class AtomicMarkableReference<V> {

    private static class Pair<T> {
        final T reference;
        final boolean mark;
        private Pair(T reference, boolean mark) {
            this.reference = reference;
            this.mark = mark;
        }
        static <T> Pair<T> of(T reference, boolean mark) {
            return new Pair<T>(reference, mark);
        }
    }

    private volatile Pair<V> pair;

    /**
     * 使用给定的初始值创建一个新的 {@code AtomicMarkableReference}。
     *
     * @param initialRef 初始引用
     * @param initialMark 初始标记
     */
    public AtomicMarkableReference(V initialRef, boolean initialMark) {
        pair = Pair.of(initialRef, initialMark);
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
     * 返回标记的当前值。
     *
     * @return 标记的当前值
     */
    public boolean isMarked() {
        return pair.mark;
    }

    /**
     * 返回引用和标记的当前值。
     * 典型用法是 {@code boolean[1] holder; ref = v.get(holder); }。
     *
     * @param markHolder 一个大小至少为一的数组。返回时，
     * {@code markholder[0]} 将持有标记的值。
     * @return 引用的当前值
     */
    public V get(boolean[] markHolder) {
        Pair<V> pair = this.pair;
        markHolder[0] = pair.mark;
        return pair.reference;
    }

    /**
     * 如果当前引用 {@code ==} 预期引用
     * 且当前标记等于预期标记，则原子地将引用和标记的值
     * 设置为给定的更新值。
     *
     * <p><a href="package-summary.html#weakCompareAndSet">可能会无故失败且不提供顺序保证</a>，因此很少是 {@code compareAndSet} 的合适替代方案。
     *
     * @param expectedReference 引用的预期值
     * @param newReference 引用的新值
     * @param expectedMark 标记的预期值
     * @param newMark 标记的新值
     * @return 如果成功则返回 {@code true}
     */
    public boolean weakCompareAndSet(V       expectedReference,
                                     V       newReference,
                                     boolean expectedMark,
                                     boolean newMark) {
        return compareAndSet(expectedReference, newReference,
                             expectedMark, newMark);
    }

    /**
     * 如果当前引用 {@code ==} 预期引用
     * 且当前标记等于预期标记，则原子地将引用和标记的值
     * 设置为给定的更新值。
     *
     * @param expectedReference 引用的预期值
     * @param newReference 引用的新值
     * @param expectedMark 标记的预期值
     * @param newMark 标记的新值
     * @return 如果成功则返回 {@code true}
     */
    public boolean compareAndSet(V       expectedReference,
                                 V       newReference,
                                 boolean expectedMark,
                                 boolean newMark) {
        Pair<V> current = pair;
        return
            expectedReference == current.reference &&
            expectedMark == current.mark &&
            ((newReference == current.reference &&
              newMark == current.mark) ||
             casPair(current, Pair.of(newReference, newMark)));
    }

    /**
     * 无条件地设置引用和标记的值。
     *
     * @param newReference 引用的新值
     * @param newMark 标记的新值
     */
    public void set(V newReference, boolean newMark) {
        Pair<V> current = pair;
        if (newReference != current.reference || newMark != current.mark)
            this.pair = Pair.of(newReference, newMark);
    }

    /**
     * 如果当前引用 {@code ==} 预期引用，则原子地将标记的值设置为给定的更新值。
     * 任何给定的调用可能失败（返回 {@code false}），但当当前值持有预期值且没有其他线程也在尝试设置值时，重复调用最终会成功。
     *
     * @param expectedReference 引用的预期值
     * @param newMark 标记的新值
     * @return 如果成功则返回 {@code true}
     */
    public boolean attemptMark(V expectedReference, boolean newMark) {
        Pair<V> current = pair;
        return
            expectedReference == current.reference &&
            (newMark == current.mark ||
             casPair(current, Pair.of(expectedReference, newMark)));
    }

    // Unsafe 机制

    private static final sun.misc.Unsafe UNSAFE = sun.misc.Unsafe.getUnsafe();
    private static final long pairOffset =
        objectFieldOffset(UNSAFE, "pair", AtomicMarkableReference.class);

    private boolean casPair(Pair<V> cmp, Pair<V> val) {
        return UNSAFE.compareAndSwapObject(this, pairOffset, cmp, val);
    }

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
