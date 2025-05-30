
/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;
import jdk.internal.misc.TerminatingThreadLocal;

import java.lang.ref.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * 该类提供线程局部变量。这些变量与它们的普通对应变量不同，每个访问一个（通过其
 * {@code get} 或 {@code set} 方法）的线程都有其自己的、独立初始化的变量副本。
 * {@code ThreadLocal} 实例通常是希望将状态与线程关联的类中的私有静态字段（例如，
 * 用户ID或事务ID）。
 *
 * <p>例如，下面的类生成每个线程的唯一标识符。
 * 线程的ID在第一次调用 {@code ThreadId.get()} 时分配，并在后续调用中保持不变。
 * <pre>
 * import java.util.concurrent.atomic.AtomicInteger;
 *
 * public class ThreadId {
 *     // 原子整数，包含要分配的下一个线程ID
 *     private static final AtomicInteger nextId = new AtomicInteger(0);
 *
 *     // 线程局部变量，包含每个线程的ID
 *     private static final ThreadLocal&lt;Integer&gt; threadId =
 *         new ThreadLocal&lt;Integer&gt;() {
 *             &#64;Override protected Integer initialValue() {
 *                 return nextId.getAndIncrement();
 *         }
 *     };
 *
 *     // 返回当前线程的唯一ID，必要时分配
 *     public static int get() {
 *         return threadId.get();
 *     }
 * }
 * </pre>
 * <p>只要线程存活且 {@code ThreadLocal} 实例可访问，每个线程都会持有其线程局部变量副本的隐式引用；
 * 线程结束后，所有线程局部实例的副本都将被垃圾回收（除非存在其他对这些副本的引用）。
 *
 * @author  Josh Bloch 和 Doug Lea
 * @since   1.2
 */
public class ThreadLocal<T> {
    /**
     * ThreadLocals 依赖于附加到每个线程的线程局部哈希映射（Thread.threadLocals 和
     * inheritableThreadLocals）。ThreadLocal 对象作为键，通过 threadLocalHashCode 进行查找。
     * 这是一个自定义哈希码（仅在 ThreadLocalMaps 内有用），在连续构造的 ThreadLocals 被同一线程使用时消除冲突，
 * 同时在不太常见的情况下保持良好的行为。
     */
    private final int threadLocalHashCode = nextHashCode();

    /**
     * 要分配的下一个哈希码。原子更新。从零开始。
     */
    private static AtomicInteger nextHashCode =
        new AtomicInteger();

    /**
     * 依次生成的哈希码之间的差值 - 将隐式顺序的线程局部ID转换为接近最优的乘法哈希值，适用于大小为2的幂的表。
     */
    private static final int HASH_INCREMENT = 0x61c88647;

    /**
     * 返回下一个哈希码。
     */
    private static int nextHashCode() {
        return nextHashCode.getAndAdd(HASH_INCREMENT);
    }

    /**
     * 返回当前线程的此线程局部变量的“初始值”。当线程第一次使用 {@link #get}
     * 方法访问变量时，将调用此方法，除非线程之前调用了 {@link #set} 方法，在这种情况下，
     * {@code initialValue} 方法将不会为该线程调用。通常，此方法每个线程最多调用一次，但如果后续调用了
     * {@link #remove} 然后再次调用 {@link #get}，则可能会再次调用此方法。
     *
     * <p>此实现简单地返回 {@code null}；如果程序员希望线程局部变量的初始值不是 {@code null}，
     * 必须子类化 {@code ThreadLocal} 并覆盖此方法。通常，将使用匿名内部类。
     *
     * @return 此线程局部变量的初始值
     */
    protected T initialValue() {
        return null;
    }

    /**
     * 创建一个线程局部变量。变量的初始值由调用 {@code Supplier} 的 {@code get} 方法确定。
     *
     * @param <S> 线程局部变量的值类型
     * @param supplier 用于确定初始值的供应商
     * @return 一个新的线程局部变量
     * @throws NullPointerException 如果指定的供应商为 null
     * @since 1.8
     */
    public static <S> ThreadLocal<S> withInitial(Supplier<? extends S> supplier) {
        return new SuppliedThreadLocal<>(supplier);
    }

    /**
     * 创建一个线程局部变量。
     * @see #withInitial(java.util.function.Supplier)
     */
    public ThreadLocal() {
    }

    /**
     * 返回当前线程的此线程局部变量的副本中的值。如果变量对当前线程没有值，则首先通过调用
     * {@link #initialValue} 方法进行初始化。
     *
     * @return 当前线程的此线程局部变量的值
     */
    public T get() {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            ThreadLocalMap.Entry e = map.getEntry(this);
            if (e != null) {
                @SuppressWarnings("unchecked")
                T result = (T)e.value;
                return result;
            }
        }
        return setInitialValue();
    }

    /**
     * 如果当前线程的此线程局部变量的副本中有值，即使该值为 {@code null}，也返回 {@code true}。
     *
     * @return 如果当前线程在此线程局部变量中有关联的值，则返回 {@code true}；否则返回 {@code false}
     */
    boolean isPresent() {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        return map != null && map.getEntry(this) != null;
    }

    /**
     * 设置初始值的 set() 变体。如果用户覆盖了 set() 方法，则使用此方法。
     *
     * @return 初始值
     */
    private T setInitialValue() {
        T value = initialValue();
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            map.set(this, value);
        } else {
            createMap(t, value);
        }
        if (this instanceof TerminatingThreadLocal) {
            TerminatingThreadLocal.register((TerminatingThreadLocal<?>) this);
        }
        return value;
    }

    /**
     * 将当前线程的此线程局部变量的副本设置为指定值。大多数子类不需要覆盖此方法，仅依赖于
     * {@link #initialValue} 方法来设置线程局部变量的值。
     *
     * @param value 要存储在当前线程的此线程局部变量副本中的值
     */
    public void set(T value) {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            map.set(this, value);
        } else {
            createMap(t, value);
        }
    }

    /**
     * 删除当前线程的此线程局部变量的值。如果当前线程随后 {@linkplain #get 读取} 此线程局部变量，
     * 其值将通过调用其 {@link #initialValue} 方法重新初始化，除非在此期间其值被当前线程
     * {@linkplain #set 设置}。这可能导致在当前线程中多次调用 {@code initialValue} 方法。
     *
     * @since 1.5
     */
     public void remove() {
         ThreadLocalMap m = getMap(Thread.currentThread());
         if (m != null) {
             m.remove(this);
         }
     }

    /**
     * 获取与 ThreadLocal 关联的映射。在 InheritableThreadLocal 中被覆盖。
     *
     * @param  t 当前线程
     * @return 映射
     */
    ThreadLocalMap getMap(Thread t) {
        return t.threadLocals;
    }

    /**
     * 创建与 ThreadLocal 关联的映射。在 InheritableThreadLocal 中被覆盖。
     *
     * @param t 当前线程
     * @param firstValue 映射的初始条目的值
     */
    void createMap(Thread t, T firstValue) {
        t.threadLocals = new ThreadLocalMap(this, firstValue);
    }

    /**
     * 创建继承线程局部变量的映射。设计为仅在 Thread 构造函数中调用。
     *
     * @param  parentMap 与父线程关联的映射
     * @return 包含父线程的可继承绑定的映射
     */
    static ThreadLocalMap createInheritedMap(ThreadLocalMap parentMap) {
        return new ThreadLocalMap(parentMap);
    }

    /**
     * 方法 childValue 在子类 InheritableThreadLocal 中可见定义，但在此内部定义是为了提供
     * createInheritedMap 工厂方法，而无需在 InheritableThreadLocal 中子类化映射类。这种技术优于
     * 在方法中嵌入 instanceof 测试。
     */
    T childValue(T parentValue) {
        throw new UnsupportedOperationException();
    }

    /**
     * 从指定的 {@code Supplier} 获取初始值的 ThreadLocal 扩展。
     */
    static final class SuppliedThreadLocal<T> extends ThreadLocal<T> {

        private final Supplier<? extends T> supplier;

        SuppliedThreadLocal(Supplier<? extends T> supplier) {
            this.supplier = Objects.requireNonNull(supplier);
        }

        @Override
        protected T initialValue() {
            return supplier.get();
        }
    }

    /**
     * ThreadLocalMap 是一个定制的哈希映射，仅适用于维护线程局部值。没有操作导出到
     * ThreadLocal 类之外。该类是包私有的，以便在 Thread 类中声明字段。为了处理非常大和长生命周期的使用，
     * 哈希表条目使用 WeakReferences 作为键。但是，由于没有使用引用队列，因此只有在表开始空间不足时，
     * 才能保证删除陈旧条目。
     */
    static class ThreadLocalMap {

        /**
         * 该哈希映射中的条目扩展了 WeakReference，使用其主 ref 字段作为键（始终是
         * ThreadLocal 对象）。注意，null 键（即 entry.get() == null）表示键不再被引用，
         * 因此可以从表中删除该条目。以下代码中将此类条目称为“陈旧条目”。
         */
        static class Entry extends WeakReference<ThreadLocal<?>> {
            /** 与此 ThreadLocal 关联的值。 */
            Object value;

            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
        }

        /**
         * 初始容量 - 必须是2的幂。
         */
        private static final int INITIAL_CAPACITY = 16;

        /**
         * 表，根据需要调整大小。
         * table.length 必须始终是2的幂。
         */
        private Entry[] table;

        /**
         * 表中的条目数。
         */
        private int size = 0;

        /**
         * 下一个调整大小的大小值。
         */
        private int threshold; // 默认为 0

        /**
         * 将调整大小的阈值设置为最多 2/3 的负载因子。
         */
        private void setThreshold(int len) {
            threshold = len * 2 / 3;
        }

        /**
         * 增量 i 模 len。
         */
        private static int nextIndex(int i, int len) {
            return ((i + 1 < len) ? i + 1 : 0);
        }

        /**
         * 减量 i 模 len。
         */
        private static int prevIndex(int i, int len) {
            return ((i - 1 >= 0) ? i - 1 : len - 1);
        }

        /**
         * 构造一个新的映射，最初包含 (firstKey, firstValue)。
         * ThreadLocalMaps 是懒惰构造的，因此只有在我们至少有一个条目要放入时才创建一个。
         */
        ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
            table = new Entry[INITIAL_CAPACITY];
            int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);
            table[i] = new Entry(firstKey, firstValue);
            size = 1;
            setThreshold(INITIAL_CAPACITY);
        }

        /**
         * 构造一个新的映射，包含给定父映射中的所有可继承的 ThreadLocals。仅由 createInheritedMap 调用。
         *
         * @param parentMap 与父线程关联的映射。
         */
        private ThreadLocalMap(ThreadLocalMap parentMap) {
            Entry[] parentTable = parentMap.table;
            int len = parentTable.length;
            setThreshold(len);
            table = new Entry[len];

            for (int j = 0; j < len; j++) {
                Entry e = parentTable[j];
                if (e != null) {
                    @SuppressWarnings("unchecked")
                    ThreadLocal<Object> key = (ThreadLocal<Object>) e.get();
                    if (key != null) {
                        Object value = key.childValue(e.value);
                        Entry c = new Entry(key, value);
                        int h = key.threadLocalHashCode & (len - 1);
                        while (table[h] != null)
                            h = nextIndex(h, len);
                        table[h] = c;
                        size++;
                    }
                }
            }
        }

        /**
         * 获取与 key 关联的条目。此方法本身只处理快速路径：现有键的直接命中。
         * 否则，将委托给 getEntryAfterMiss。此设计旨在最大化直接命中的性能，部分原因是使此方法易于内联。
         *
         * @param  key 线程局部对象
         * @return 与 key 关联的条目，如果没有则返回 null
         */
        private Entry getEntry(ThreadLocal<?> key) {
            int i = key.threadLocalHashCode & (table.length - 1);
            Entry e = table[i];
            if (e != null && e.get() == key)
                return e;
            else
                return getEntryAfterMiss(key, i, e);
        }


                    /**
         * 当键未在直接哈希槽中找到时，getEntry 方法的版本。
         *
         * @param  key 线程本地对象
         * @param  i 键的哈希码的表索引
         * @param  e 表[i] 处的条目
         * @return 与键关联的条目，如果没有则返回 null
         */
        private Entry getEntryAfterMiss(ThreadLocal<?> key, int i, Entry e) {
            Entry[] tab = table;
            int len = tab.length;

            while (e != null) {
                ThreadLocal<?> k = e.get();
                if (k == key)
                    return e;
                if (k == null)
                    expungeStaleEntry(i);
                else
                    i = nextIndex(i, len);
                e = tab[i];
            }
            return null;
        }

        /**
         * 设置与键关联的值。
         *
         * @param key 线程本地对象
         * @param value 要设置的值
         */
        private void set(ThreadLocal<?> key, Object value) {

            // 我们不使用与 get() 相同的快速路径，因为使用 set() 创建新条目与替换现有条目一样常见，因此快速路径失败的频率可能更高。

            Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len-1);

            for (Entry e = tab[i];
                 e != null;
                 e = tab[i = nextIndex(i, len)]) {
                ThreadLocal<?> k = e.get();

                if (k == key) {
                    e.value = value;
                    return;
                }

                if (k == null) {
                    replaceStaleEntry(key, value, i);
                    return;
                }
            }

            tab[i] = new Entry(key, value);
            int sz = ++size;
            if (!cleanSomeSlots(i, sz) && sz >= threshold)
                rehash();
        }

        /**
         * 移除键的条目。
         */
        private void remove(ThreadLocal<?> key) {
            Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len-1);
            for (Entry e = tab[i];
                 e != null;
                 e = tab[i = nextIndex(i, len)]) {
                if (e.get() == key) {
                    e.clear();
                    expungeStaleEntry(i);
                    return;
                }
            }
        }

        /**
         * 在设置操作中用指定键的条目替换过时的条目。无论是否已存在与指定键关联的条目，都会存储传递的值参数中的值。
         *
         * 作为副作用，此方法会清除包含过时条目的“运行”中的所有过时条目。（运行是指两个空槽之间的条目序列。）
         *
         * @param  key 键
         * @param  value 要与键关联的值
         * @param  staleSlot 在搜索键时遇到的第一个过时条目的索引。
         */
        private void replaceStaleEntry(ThreadLocal<?> key, Object value,
                                       int staleSlot) {
            Entry[] tab = table;
            int len = tab.length;
            Entry e;

            // 向后查找以检查当前运行中的先前过时条目。
            // 我们一次清理整个运行，以避免由于垃圾收集器成批释放引用而导致的持续增量重新哈希（即，每当垃圾收集器运行时）。
            int slotToExpunge = staleSlot;
            for (int i = prevIndex(staleSlot, len);
                 (e = tab[i]) != null;
                 i = prevIndex(i, len))
                if (e.get() == null)
                    slotToExpunge = i;

            // 查找运行中键或尾随空槽，以先出现者为准
            for (int i = nextIndex(staleSlot, len);
                 (e = tab[i]) != null;
                 i = nextIndex(i, len)) {
                ThreadLocal<?> k = e.get();

                // 如果我们找到键，则需要将其与过时条目交换以保持哈希表顺序。
                // 新的过时槽或上述任何其他过时槽都可以发送到 expungeStaleEntry 以移除或重新哈希运行中的所有其他条目。
                if (k == key) {
                    e.value = value;

                    tab[i] = tab[staleSlot];
                    tab[staleSlot] = e;

                    // 如果存在先前的过时条目，则从该条目开始清除
                    if (slotToExpunge == staleSlot)
                        slotToExpunge = i;
                    cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
                    return;
                }

                // 如果在向后扫描时未找到过时条目，则在扫描键时看到的第一个过时条目是运行中仍然存在的第一个。
                if (k == null && slotToExpunge == staleSlot)
                    slotToExpunge = i;
            }

            // 如果未找到键，则在过时槽中放置新条目
            tab[staleSlot].value = null;
            tab[staleSlot] = new Entry(key, value);

            // 如果运行中还有其他过时条目，则清除它们
            if (slotToExpunge != staleSlot)
                cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
        }

        /**
         * 通过重新哈希可能在 staleSlot 和下一个空槽之间的任何冲突条目来清除过时条目。这也会清除在尾随空槽之前遇到的任何其他过时条目。参见 Knuth, Section 6.4
         *
         * @param staleSlot 已知具有空键的槽的索引
         * @return staleSlot 之后的下一个空槽的索引
         * （staleSlot 和此槽之间的所有槽都将被检查以清除）。
         */
        private int expungeStaleEntry(int staleSlot) {
            Entry[] tab = table;
            int len = tab.length;

            // 清除 staleSlot 处的条目
            tab[staleSlot].value = null;
            tab[staleSlot] = null;
            size--;

            // 重新哈希直到遇到空槽
            Entry e;
            int i;
            for (i = nextIndex(staleSlot, len);
                 (e = tab[i]) != null;
                 i = nextIndex(i, len)) {
                ThreadLocal<?> k = e.get();
                if (k == null) {
                    e.value = null;
                    tab[i] = null;
                    size--;
                } else {
                    int h = k.threadLocalHashCode & (len - 1);
                    if (h != i) {
                        tab[i] = null;

                        // 与 Knuth 6.4 算法 R 不同，我们必须扫描到空槽，因为可能有多个条目过时。
                        while (tab[h] != null)
                            h = nextIndex(h, len);
                        tab[h] = e;
                    }
                }
            }
            return i;
        }

        /**
         * 启发式扫描一些单元格以查找过时条目。当添加新元素或清除另一个过时条目时调用此方法。它执行对数数量的扫描，以在不扫描（快速但保留垃圾）和与元素数量成比例的扫描之间取得平衡，后者可以找到所有垃圾，但会导致某些插入操作花费 O(n) 时间。
         *
         * @param i 一个已知不包含过时条目的位置。扫描从 i 之后的元素开始。
         *
         * @param n 扫描控制：扫描 {@code log2(n)} 个单元格，除非找到过时条目，否则扫描 {@code log2(table.length)-1} 个额外的单元格。
         * 当从插入操作调用时，此参数是元素数量，但从 replaceStaleEntry 调用时，它是表的长度。（注意：所有这些都可以通过加权 n 来变得更积极或更不积极，而不是仅仅使用直接的 log n。但此版本简单、快速，并且似乎工作得很好。）
         *
         * @return 如果已移除任何过时条目，则返回 true。
         */
        private boolean cleanSomeSlots(int i, int n) {
            boolean removed = false;
            Entry[] tab = table;
            int len = tab.length;
            do {
                i = nextIndex(i, len);
                Entry e = tab[i];
                if (e != null && e.get() == null) {
                    n = len;
                    removed = true;
                    i = expungeStaleEntry(i);
                }
            } while ( (n >>>= 1) != 0);
            return removed;
        }

        /**
         * 重新打包和/或调整表的大小。首先扫描整个表以移除过时条目。如果这不足以显著缩小表的大小，则将表的大小加倍。
         */
        private void rehash() {
            expungeStaleEntries();

            // 使用较低的阈值来加倍以避免滞后
            if (size >= threshold - threshold / 4)
                resize();
        }

        /**
         * 将表的容量加倍。
         */
        private void resize() {
            Entry[] oldTab = table;
            int oldLen = oldTab.length;
            int newLen = oldLen * 2;
            Entry[] newTab = new Entry[newLen];
            int count = 0;

            for (int j = 0; j < oldLen; ++j) {
                Entry e = oldTab[j];
                if (e != null) {
                    ThreadLocal<?> k = e.get();
                    if (k == null) {
                        e.value = null; // 帮助垃圾回收
                    } else {
                        int h = k.threadLocalHashCode & (newLen - 1);
                        while (newTab[h] != null)
                            h = nextIndex(h, newLen);
                        newTab[h] = e;
                        count++;
                    }
                }
            }

            setThreshold(newLen);
            size = count;
            table = newTab;
        }

        /**
         * 清除表中的所有过时条目。
         */
        private void expungeStaleEntries() {
            Entry[] tab = table;
            int len = tab.length;
            for (int j = 0; j < len; j++) {
                Entry e = tab[j];
                if (e != null && e.get() == null)
                    expungeStaleEntry(j);
            }
        }
    }
}
