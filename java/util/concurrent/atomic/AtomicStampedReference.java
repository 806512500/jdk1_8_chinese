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
 * 一个 {@code AtomicStampedReference} 维护一个对象引用
 * 以及一个整数 "stamp"，可以原子地更新。
 *
 * <p>实现说明：此实现通过创建表示 "boxed"
 * [引用, 整数] 对的内部对象来维护带时间戳的引用。
 *
 * @since 1.5
 * @author Doug Lea
 * @param <V> 此引用所引用的对象类型
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
     * @param initialStamp 初始时间戳
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
     * 返回时间戳的当前值。
     *
     * @return 时间戳的当前值
     */
    public int getStamp() {
        return pair.stamp;
    }

    /**
     * 返回引用和时间戳的当前值。
     * 典型用法是 {@code int[1] holder; ref = v.get(holder); }。
     *
     * @param stampHolder 一个大小至少为一的数组。返回时，
     * {@code stampholder[0]} 将保存时间戳的值。
     * @return 引用的当前值
     */
    public V get(int[] stampHolder) {
        Pair<V> pair = this.pair;
        stampHolder[0] = pair.stamp;
        return pair.reference;
    }

    /**
     * 如果当前引用等于预期引用且当前时间戳等于预期时间戳，
     * 则原子地设置引用和时间戳的值为给定的更新值。
     *
     * <p><a href="package-summary.html#weakCompareAndSet">可能会失败
     * 并且不提供顺序保证</a>，因此通常很少作为 {@code compareAndSet} 的替代方案。
     *
     * @param expectedReference 预期的引用值
     * @param newReference 引用的新值
     * @param expectedStamp 预期的时间戳值
     * @param newStamp 时间戳的新值
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
     * 如果当前引用等于预期引用且当前时间戳等于预期时间戳，
     * 则原子地设置引用和时间戳的值为给定的更新值。
     *
     * @param expectedReference 预期的引用值
     * @param newReference 引用的新值
     * @param expectedStamp 预期的时间戳值
     * @param newStamp 时间戳的新值
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
     * 无条件地设置引用和时间戳的值。
     *
     * @param newReference 引用的新值
     * @param newStamp 时间戳的新值
     */
    public void set(V newReference, int newStamp) {
        Pair<V> current = pair;
        if (newReference != current.reference || newStamp != current.stamp)
            this.pair = Pair.of(newReference, newStamp);
    }

    /**
     * 如果当前引用等于预期引用，则原子地设置时间戳的值为给定的更新值。
     * 任何给定的调用可能会失败（返回 {@code false}），但当当前值包含预期值且没有其他线程也在尝试设置值时，
     * 重复调用最终会成功。
     *
     * @param expectedReference 预期的引用值
     * @param newStamp 时间戳的新值
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
