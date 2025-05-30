/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic;

/**
 * 一个 {@code AtomicMarkableReference} 维护一个对象引用
 * 以及一个标记位，可以原子地更新。
 *
 * <p>实现说明：此实现通过创建表示“装箱”的 [引用, 布尔值] 对的内部对象来维护标记引用。
 *
 * @since 1.5
 * @author Doug Lea
 * @param <V> 此引用所引用的对象类型
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
     * 如果当前引用等于预期引用且当前标记等于预期标记，
     * 则原子地设置引用和标记的值为给定的更新值。
     *
     * <p><a href="package-summary.html#weakCompareAndSet">可能会失败
     * 并且不提供顺序保证</a>，因此通常很少作为 {@code compareAndSet} 的替代方案。
     *
     * @param expectedReference 预期的引用值
     * @param newReference 新的引用值
     * @param expectedMark 预期的标记值
     * @param newMark 新的标记值
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
     * 如果当前引用等于预期引用且当前标记等于预期标记，
     * 则原子地设置引用和标记的值为给定的更新值。
     *
     * @param expectedReference 预期的引用值
     * @param newReference 新的引用值
     * @param expectedMark 预期的标记值
     * @param newMark 新的标记值
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
     * @param newReference 新的引用值
     * @param newMark 新的标记值
     */
    public void set(V newReference, boolean newMark) {
        Pair<V> current = pair;
        if (newReference != current.reference || newMark != current.mark)
            this.pair = Pair.of(newReference, newMark);
    }

    /**
     * 如果当前引用等于预期引用，则原子地设置标记的值为给定的更新值。
     * 任何给定的调用可能会失败（返回 {@code false}），但当当前值持有预期值且没有其他线程也在尝试设置值时，
     * 重复调用最终会成功。
     *
     * @param expectedReference 预期的引用值
     * @param newMark 新的标记值
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
