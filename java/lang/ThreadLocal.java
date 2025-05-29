
/*
 * 版权所有 (c) 1997, 2013, Oracle 和/或其附属公司。保留所有权利。
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
import jdk.internal.misc.TerminatingThreadLocal;

import java.lang.ref.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * 该类提供线程局部变量。这些变量与其普通变量不同之处在于，每个访问它们的线程（通过其 {@code get} 或 {@code set} 方法）都有自己的、独立初始化的变量副本。{@code ThreadLocal} 实例通常是希望将状态与线程关联的类中的私有静态字段（例如，用户ID或事务ID）。
 *
 * <p>例如，下面的类为每个线程生成唯一的标识符。
 * 线程的ID在首次调用 {@code ThreadId.get()} 时分配，并在后续调用中保持不变。
 * <pre>
 * import java.util.concurrent.atomic.AtomicInteger;
 *
 * public class ThreadId {
 *     // 原子整数，包含下一个要分配的线程ID
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
 * <p>只要线程存活且 {@code ThreadLocal} 实例可访问，每个线程都会持有对其线程局部变量副本的隐式引用；线程结束后，所有其线程局部实例的副本都可能被垃圾回收（除非这些副本存在其他引用）。
 *
 * @author  Josh Bloch 和 Doug Lea
 * @since   1.2
 */
public class ThreadLocal<T> {
    /**
     * ThreadLocals 依赖于附加到每个线程的每个线程的线性探测哈希映射（Thread.threadLocals 和 inheritableThreadLocals）。ThreadLocal 对象作为键，通过 threadLocalHashCode 进行查找。这是一个自定义哈希码（仅在 ThreadLocalMaps 内有用），它消除了在常见情况下连续构造的 ThreadLocals 被相同线程使用时的冲突，同时在不太常见的情况下仍然表现良好。
     */
    private final int threadLocalHashCode = nextHashCode();

    /**
     * 要分配的下一个哈希码。原子更新。从零开始。
     */
    private static AtomicInteger nextHashCode =
        new AtomicInteger();

    /**
     * 依次生成的哈希码之间的差异 - 将隐式顺序线程局部ID转换为接近最优的乘法哈希值，适用于大小为2的幂的表。
     */
    private static final int HASH_INCREMENT = 0x61c88647;

    /**
     * 返回下一个哈希码。
     */
    private static int nextHashCode() {
        return nextHashCode.getAndAdd(HASH_INCREMENT);
    }

    /**
     * 返回此线程局部变量的当前线程的“初始值”。当线程首次使用 {@link #get} 方法访问变量时，将调用此方法，除非线程之前已调用 {@link #set} 方法，在这种情况下，不会为该线程调用 {@code initialValue} 方法。通常，此方法每个线程最多调用一次，但如果后续调用 {@link #remove} 后再调用 {@link #get}，则可能会再次调用此方法。
     *
     * <p>此实现简单地返回 {@code null}；如果程序员希望线程局部变量具有不同于 {@code null} 的初始值，则必须子类化 {@code ThreadLocal} 并覆盖此方法。通常，将使用匿名内部类。
     *
     * @return 此线程局部变量的初始值
     */
    protected T initialValue() {
        return null;
    }

    /**
     * 创建一个线程局部变量。变量的初始值通过调用 {@code Supplier} 的 {@code get} 方法确定。
     *
     * @param <S> 线程局部变量值的类型
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
     * 返回此线程局部变量的当前线程副本中的值。如果变量对当前线程没有值，则首先通过调用 {@link #initialValue} 方法初始化为返回的值。
     *
     * @return 此线程局部变量的当前线程值
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
     * 如果此线程局部变量的当前线程副本中有值，即使该值为 {@code null}，也返回 {@code true}。
     *
     * @return 如果当前线程在此线程局部变量中有关联的值，则返回 {@code true}；否则返回 {@code false}
     */
    boolean isPresent() {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        return map != null && map.getEntry(this) != null;
    }


                    /**
     * set() 的变体，用于设置初始值。如果用户重写了 set() 方法，则使用此方法。
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
     * 将当前线程的此线程局部变量的副本设置为指定的值。大多数子类不需要覆盖此方法，仅依赖于 {@link #initialValue}
     * 方法来设置线程局部变量的值。
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
     * 删除当前线程的此线程局部变量的值。如果此线程局部变量随后被当前线程 {@linkplain #get 读取}，其值将通过调用其 {@link #initialValue} 方法重新初始化，
     * 除非其值在此期间被当前线程 {@linkplain #set 设置}。这可能导致在当前线程中多次调用 {@code initialValue} 方法。
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
     * 获取与 ThreadLocal 关联的映射。在 InheritableThreadLocal 中被重写。
     *
     * @param  t 当前线程
     * @return 映射
     */
    ThreadLocalMap getMap(Thread t) {
        return t.threadLocals;
    }

    /**
     * 创建与 ThreadLocal 关联的映射。在 InheritableThreadLocal 中被重写。
     *
     * @param t 当前线程
     * @param firstValue 映射的初始条目的值
     */
    void createMap(Thread t, T firstValue) {
        t.threadLocals = new ThreadLocalMap(this, firstValue);
    }

    /**
     * 创建继承的线程局部变量的映射。设计为仅从线程构造函数中调用。
     *
     * @param  parentMap 与父线程关联的映射
     * @return 包含父线程的可继承绑定的映射
     */
    static ThreadLocalMap createInheritedMap(ThreadLocalMap parentMap) {
        return new ThreadLocalMap(parentMap);
    }

    /**
     * 方法 childValue 在子类 InheritableThreadLocal 中可见定义，但为了提供 createInheritedMap 工厂方法而在此内部定义，
     * 无需在 InheritableThreadLocal 中子类化映射类。这种技术优于在方法中嵌入 instanceof 测试。
     */
    T childValue(T parentValue) {
        throw new UnsupportedOperationException();
    }

    /**
     * ThreadLocal 的扩展，从指定的 {@code Supplier} 获取其初始值。
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
     * ThreadLocalMap 是一个定制的哈希映射，仅用于维护线程局部值。没有任何操作导出到 ThreadLocal 类之外。该类是包私有的，以便在 Thread 类中声明字段。
     * 为了帮助处理非常大和长时间运行的使用情况，哈希表条目使用弱引用作为键。然而，由于没有使用引用队列，因此只有当表开始空间不足时，才会保证删除陈旧条目。
     */
    static class ThreadLocalMap {

        /**
         * 此哈希映射中的条目扩展了 WeakReference，使用其主 ref 字段作为键（始终是 ThreadLocal 对象）。注意，null 键（即 entry.get() == null）表示键不再被引用，
         * 因此可以从表中删除该条目。在以下代码中，这样的条目被称为“陈旧条目”。
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
         * 初始容量——必须是 2 的幂。
         */
        private static final int INITIAL_CAPACITY = 16;

        /**
         * 表，根据需要调整大小。
         * table.length 必须始终是 2 的幂。
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
         * 将调整大小的阈值设置为最坏情况下 2/3 的负载因子。
         */
        private void setThreshold(int len) {
            threshold = len * 2 / 3;
        }

        /**
         * 对 len 取模后 i 的增量。
         */
        private static int nextIndex(int i, int len) {
            return ((i + 1 < len) ? i + 1 : 0);
        }

        /**
         * 对 len 取模后 i 的减量。
         */
        private static int prevIndex(int i, int len) {
            return ((i - 1 >= 0) ? i - 1 : len - 1);
        }

                        /**
         * 构造一个初始包含 (firstKey, firstValue) 的新映射。
         * ThreadLocalMaps 是惰性构造的，所以我们只有在至少有一个条目要放入时才创建一个。
         */
        ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
            table = new Entry[INITIAL_CAPACITY];
            int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);
            table[i] = new Entry(firstKey, firstValue);
            size = 1;
            setThreshold(INITIAL_CAPACITY);
        }

        /**
         * 构造一个新映射，包含来自给定父映射的所有可继承的 ThreadLocals。
         * 仅由 createInheritedMap 调用。
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
         * 获取与 key 关联的条目。此方法本身仅处理快速路径：现有 key 的直接命中。
         * 否则，它将传递给 getEntryAfterMiss。此设计旨在最大化直接命中的性能，部分原因是使此方法易于内联。
         *
         * @param  key 线程局部对象
         * @return 与 key 关联的条目，如果没有这样的条目则返回 null
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
         * 当 key 未在直接哈希槽中找到时使用的 getEntry 方法的版本。
         *
         * @param  key 线程局部对象
         * @param  i key 的哈希码的表索引
         * @param  e table[i] 处的条目
         * @return 与 key 关联的条目，如果没有这样的条目则返回 null
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
         * 设置与 key 关联的值。
         *
         * @param key 线程局部对象
         * @param value 要设置的值
         */
        private void set(ThreadLocal<?> key, Object value) {

            // 我们不使用与 get() 一样的快速路径，因为使用 set() 创建新条目与替换现有条目一样常见，
            // 在这种情况下，快速路径失败的次数可能比成功的时候多。

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
         * 删除与 key 关联的条目。
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
         * 在 set 操作中遇到过时条目时，用指定 key 的条目替换它。
         * 无论是否已经存在与指定 key 关联的条目，都会将 value 参数中传递的值存储在条目中。
         *
         * 作为副作用，此方法会清除包含过时条目的“运行”中的所有过时条目。（“运行”是指两个 null 插槽之间的条目序列。）
         *
         * @param  key 键
         * @param  value 要与 key 关联的值
         * @param  staleSlot 在搜索 key 时遇到的第一个过时条目的索引。
         */
        private void replaceStaleEntry(ThreadLocal<?> key, Object value,
                                       int staleSlot) {
            Entry[] tab = table;
            int len = tab.length;
            Entry e;

            // 向后退一步，检查当前运行中的前一个过时条目。
            // 我们一次清理整个运行，以避免由于垃圾收集器一次释放多个引用而导致的持续增量重新哈希。
            int slotToExpunge = staleSlot;
            for (int i = prevIndex(staleSlot, len);
                 (e = tab[i]) != null;
                 i = prevIndex(i, len))
                if (e.get() == null)
                    slotToExpunge = i;


                            // 查找运行中的键或尾随的空槽，以先出现的为准
            // occurs first
            for (int i = nextIndex(staleSlot, len);
                 (e = tab[i]) != null;
                 i = nextIndex(i, len)) {
                ThreadLocal<?> k = e.get();

                // 如果找到键，则需要将其与陈旧条目交换以保持哈希表顺序。
                // 新的陈旧槽，或在此之上遇到的任何其他陈旧槽，
                // 可以发送到 expungeStaleEntry 以移除或重新哈希运行中的所有其他条目。
                if (k == key) {
                    e.value = value;

                    tab[i] = tab[staleSlot];
                    tab[staleSlot] = e;

                    // 如果存在前一个陈旧条目，则从该条目开始清除
                    if (slotToExpunge == staleSlot)
                        slotToExpunge = i;
                    cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
                    return;
                }

                // 如果在向后扫描时没有找到陈旧条目，
                // 则在扫描键时看到的第一个陈旧条目是运行中仍然存在的第一个。
                if (k == null && slotToExpunge == staleSlot)
                    slotToExpunge = i;
            }

            // 如果未找到键，将新条目放入陈旧槽中
            tab[staleSlot].value = null;
            tab[staleSlot] = new Entry(key, value);

            // 如果运行中还有其他陈旧条目，清除它们
            if (slotToExpunge != staleSlot)
                cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
        }

        /**
         * 通过重新哈希可能在 staleSlot 和下一个空槽之间发生冲突的条目来清除陈旧条目。
         * 这也会清除在尾随空槽之前遇到的任何其他陈旧条目。参见 Knuth, Section 6.4
         *
         * @param staleSlot 已知具有空键的槽的索引
         * @return staleSlot 之后的下一个空槽的索引
         * （staleSlot 和此槽之间的所有条目都将被检查以进行清除）。
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

                        // 与 Knuth 6.4 算法 R 不同，我们必须扫描直到空槽，
                        // 因为可能有多个条目是陈旧的。
                        while (tab[h] != null)
                            h = nextIndex(h, len);
                        tab[h] = e;
                    }
                }
            }
            return i;
        }

        /**
         * 启发式地扫描一些单元格，寻找陈旧条目。
         * 当添加新元素或另一个陈旧条目已被清除时调用此方法。
         * 它执行对数数量的扫描，作为不扫描（快速但保留垃圾）和与元素数量成比例的扫描数量之间的平衡，
         * 后者会找到所有垃圾，但会导致某些插入操作花费 O(n) 时间。
         *
         * @param i 一个已知不包含陈旧条目的位置。扫描从 i 之后的元素开始。
         *
         * @param n 扫描控制：扫描 {@code log2(n)} 个单元格，除非找到陈旧条目，
         * 在这种情况下，将额外扫描 {@code log2(table.length)-1} 个单元格。
         * 当从插入调用时，此参数是元素数量，但当从 replaceStaleEntry 调用时，它是表的长度。
         * （注意：所有这些都可以通过加权 n 而不是直接使用 log n 来变得更积极或更不积极。但这个版本简单、快速，而且似乎效果很好。）
         *
         * @return 如果已移除任何陈旧条目，则返回 true。
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
         * 重新打包和/或调整表的大小。首先扫描整个表以移除陈旧条目。
         * 如果这不足以显著缩小表的大小，则将表的大小翻倍。
         */
        private void rehash() {
            expungeStaleEntries();

            // 使用较低的阈值来加倍以避免滞后
            if (size >= threshold - threshold / 4)
                resize();
        }

        /**
         * 将表的容量翻倍。
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
         * 清除表中的所有陈旧条目。
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
