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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并根据以下条款发布到公共领域：
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic;
import java.util.function.LongBinaryOperator;
import java.util.function.DoubleBinaryOperator;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 一个包级类，用于支持 64 位值动态分条的类的通用表示和机制。该类扩展了 Number，因此具体子类必须公开这样做。
 */
@SuppressWarnings("serial")
abstract class Striped64 extends Number {
    /*
     * 该类维护一个延迟初始化的原子更新变量表，以及一个额外的“base”字段。表的大小是 2 的幂。索引使用每个线程的哈希代码。该类中的几乎所有声明都是包级私有的，由子类直接访问。
     *
     * 表项是 Cell 类；它是 AtomicLong 的变体，通过 @sun.misc.Contended 填充以减少缓存争用。对于大多数原子对象来说，填充是过度的，因为它们通常在内存中分布不均，因此彼此之间的干扰不大。但是，数组中的原子对象将倾向于彼此相邻放置，因此如果没有此预防措施，大多数情况下会共享缓存行（对性能有巨大的负面影响）。
     *
     * 由于 Cell 相对较大，我们避免在需要时创建它们。当没有争用时，所有更新都对 base 字段进行。首次发生争用（在 base 更新时 CAS 失败）时，表初始化为大小 2。进一步争用时，表大小翻倍，直到达到大于或等于 CPU 数的最近的 2 的幂。表槽在需要之前保持为空（null）。
     *
     * 单个自旋锁（“cellsBusy”）用于初始化和调整表的大小，以及用新 Cell 填充槽。不需要阻塞锁；当锁不可用时，线程尝试其他槽（或 base）。在这些重试期间，争用增加，局部性降低，但这仍然比其他替代方案更好。
     *
     * 通过 ThreadLocalRandom 维护的线程探针字段作为每个线程的哈希代码。我们让它们保持未初始化为零（如果它们以这种方式进入），直到它们在槽 0 争用。然后它们被初始化为通常与其他线程不经常冲突的值。争用和/或表冲突通过执行更新操作时 CAS 失败来指示。发生冲突时，如果表大小小于容量，则在没有其他线程持有锁的情况下，表大小翻倍。如果哈希槽为空，且锁可用，则创建新 Cell。否则，如果槽存在，则尝试 CAS。重试通过“双重哈希”进行，使用次级哈希（Marsaglia XorShift）尝试找到一个空槽。
     *
     * 表的大小是有限制的，因为在线程数多于 CPU 的情况下，假设每个线程都绑定到一个 CPU，存在一个完美的哈希函数可以将线程映射到槽，从而消除冲突。当我们达到容量时，我们通过随机改变冲突线程的哈希代码来搜索这种映射。由于搜索是随机的，并且冲突仅通过 CAS 失败来得知，收敛可能很慢，并且由于线程通常不会永远绑定到 CPU，可能根本不会发生。然而，尽管存在这些限制，观察到的争用率在这种情况下通常较低。
     *
     * 当曾经哈希到它的线程终止时，Cell 可能会变得未使用，以及在表大小翻倍导致没有线程在扩展掩码下哈希到它的情况下。我们不尝试检测或删除这样的单元格，假设对于长期运行的实例，观察到的争用水平将再次出现，因此这些单元格最终将再次需要；对于短期运行的实例，这并不重要。
     */

    /**
     * 仅支持原始访问和 CAS 的 AtomicLong 的填充变体。
     *
     * JVM 内在函数注释：如果提供，可以在这里使用仅释放形式的 CAS。
     */
    @sun.misc.Contended static final class Cell {
        volatile long value;
        Cell(long x) { value = x; }
        final boolean cas(long cmp, long val) {
            return UNSAFE.compareAndSwapLong(this, valueOffset, cmp, val);
        }

        // Unsafe 机制
        private static final sun.misc.Unsafe UNSAFE;
        private static final long valueOffset;
        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> ak = Cell.class;
                valueOffset = UNSAFE.objectFieldOffset
                    (ak.getDeclaredField("value"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    /** CPU 数量，用于限制表大小 */
    static final int NCPU = Runtime.getRuntime().availableProcessors();

    /**
     * 单元格表。当非空时，大小是 2 的幂。
     */
    transient volatile Cell[] cells;

    /**
     * 基础值，主要用于没有争用时，但在表初始化竞争期间也作为回退。通过 CAS 更新。
     */
    transient volatile long base;

    /**
     * 用于调整大小和/或创建单元格时的自旋锁（通过 CAS 锁定）。
     */
    transient volatile int cellsBusy;

    /**
     * 包级默认构造函数
     */
    Striped64() {
    }

    /**
     * CAS 基础字段。
     */
    final boolean casBase(long cmp, long val) {
        return UNSAFE.compareAndSwapLong(this, BASE, cmp, val);
    }

}


                /**
     * 将 cellsBusy 字段从 0 CAS 到 1 以获取锁。
     */
    final boolean casCellsBusy() {
        return UNSAFE.compareAndSwapInt(this, CELLSBUSY, 0, 1);
    }

    /**
     * 返回当前线程的探针值。
     * 由于包限制，从 ThreadLocalRandom 复制而来。
     */
    static final int getProbe() {
        return UNSAFE.getInt(Thread.currentThread(), PROBE);
    }

    /**
     * 伪随机地推进并记录给定线程的探针值。
     * 由于包限制，从 ThreadLocalRandom 复制而来。
     */
    static final int advanceProbe(int probe) {
        probe ^= probe << 13;   // xorshift
        probe ^= probe >>> 17;
        probe ^= probe << 5;
        UNSAFE.putInt(Thread.currentThread(), PROBE, probe);
        return probe;
    }

    /**
     * 处理涉及初始化、调整大小、创建新 Cells 和/或争用的更新情况。参见上方解释。
     * 此方法通常具有乐观重试代码的非模块化问题，依赖于重新检查的读取集。
     *
     * @param x 值
     * @param fn 更新函数，或 null 表示加法（此约定避免在 LongAdder 中需要额外的字段或函数）。
     * @param wasUncontended 如果在调用前 CAS 失败，则为 false
     */
    final void longAccumulate(long x, LongBinaryOperator fn,
                              boolean wasUncontended) {
        int h;
        if ((h = getProbe()) == 0) {
            ThreadLocalRandom.current(); // 强制初始化
            h = getProbe();
            wasUncontended = true;
        }
        boolean collide = false;                // 如果最后一个槽非空，则为 true
        for (;;) {
            Cell[] as; Cell a; int n; long v;
            if ((as = cells) != null && (n = as.length) > 0) {
                if ((a = as[(n - 1) & h]) == null) {
                    if (cellsBusy == 0) {       // 尝试附加新 Cell
                        Cell r = new Cell(x);   // 乐观创建
                        if (cellsBusy == 0 && casCellsBusy()) {
                            boolean created = false;
                            try {               // 在锁下重新检查
                                Cell[] rs; int m, j;
                                if ((rs = cells) != null &&
                                    (m = rs.length) > 0 &&
                                    rs[j = (m - 1) & h] == null) {
                                    rs[j] = r;
                                    created = true;
                                }
                            } finally {
                                cellsBusy = 0;
                            }
                            if (created)
                                break;
                            continue;           // 槽现在非空
                        }
                    }
                    collide = false;
                }
                else if (!wasUncontended)       // 已知 CAS 失败
                    wasUncontended = true;      // 重新哈希后继续
                else if (a.cas(v = a.value, ((fn == null) ? v + x :
                                             fn.applyAsLong(v, x))))
                    break;
                else if (n >= NCPU || cells != as)
                    collide = false;            // 达到最大大小或过期
                else if (!collide)
                    collide = true;
                else if (cellsBusy == 0 && casCellsBusy()) {
                    try {
                        if (cells == as) {      // 除非过期，否则扩展表
                            Cell[] rs = new Cell[n << 1];
                            for (int i = 0; i < n; ++i)
                                rs[i] = as[i];
                            cells = rs;
                        }
                    } finally {
                        cellsBusy = 0;
                    }
                    collide = false;
                    continue;                   // 使用扩展表重试
                }
                h = advanceProbe(h);
            }
            else if (cellsBusy == 0 && cells == as && casCellsBusy()) {
                boolean init = false;
                try {                           // 初始化表
                    if (cells == as) {
                        Cell[] rs = new Cell[2];
                        rs[h & 1] = new Cell(x);
                        cells = rs;
                        init = true;
                    }
                } finally {
                    cellsBusy = 0;
                }
                if (init)
                    break;
            }
            else if (casBase(v = base, ((fn == null) ? v + x :
                                        fn.applyAsLong(v, x))))
                break;                          // 回退到使用 base
        }
    }

    /**
     * 与 longAccumulate 相同，但在太多地方注入了 long/double 转换，无法合理地与 long 版本合并，
     * 鉴于此类的低开销要求。因此必须通过复制/粘贴/调整来维护。
     */
    final void doubleAccumulate(double x, DoubleBinaryOperator fn,
                                boolean wasUncontended) {
        int h;
        if ((h = getProbe()) == 0) {
            ThreadLocalRandom.current(); // 强制初始化
            h = getProbe();
            wasUncontended = true;
        }
        boolean collide = false;                // 如果最后一个槽非空，则为 true
        for (;;) {
            Cell[] as; Cell a; int n; long v;
            if ((as = cells) != null && (n = as.length) > 0) {
                if ((a = as[(n - 1) & h]) == null) {
                    if (cellsBusy == 0) {       // 尝试附加新 Cell
                        Cell r = new Cell(Double.doubleToRawLongBits(x));
                        if (cellsBusy == 0 && casCellsBusy()) {
                            boolean created = false;
                            try {               // 在锁下重新检查
                                Cell[] rs; int m, j;
                                if ((rs = cells) != null &&
                                    (m = rs.length) > 0 &&
                                    rs[j = (m - 1) & h] == null) {
                                    rs[j] = r;
                                    created = true;
                                }
                            } finally {
                                cellsBusy = 0;
                            }
                            if (created)
                                break;
                            continue;           // 槽现在非空
                        }
                    }
                    collide = false;
                }
                else if (!wasUncontended)       // 已知 CAS 失败
                    wasUncontended = true;      // 重新哈希后继续
                else if (a.cas(v = a.value,
                               ((fn == null) ?
                                Double.doubleToRawLongBits
                                (Double.longBitsToDouble(v) + x) :
                                Double.doubleToRawLongBits
                                (fn.applyAsDouble
                                 (Double.longBitsToDouble(v), x)))))
                    break;
                else if (n >= NCPU || cells != as)
                    collide = false;            // 达到最大大小或过期
                else if (!collide)
                    collide = true;
                else if (cellsBusy == 0 && casCellsBusy()) {
                    try {
                        if (cells == as) {      // 除非过期，否则扩展表
                            Cell[] rs = new Cell[n << 1];
                            for (int i = 0; i < n; ++i)
                                rs[i] = as[i];
                            cells = rs;
                        }
                    } finally {
                        cellsBusy = 0;
                    }
                    collide = false;
                    continue;                   // 使用扩展表重试
                }
                h = advanceProbe(h);
            }
            else if (cellsBusy == 0 && cells == as && casCellsBusy()) {
                boolean init = false;
                try {                           // 初始化表
                    if (cells == as) {
                        Cell[] rs = new Cell[2];
                        rs[h & 1] = new Cell(Double.doubleToRawLongBits(x));
                        cells = rs;
                        init = true;
                    }
                } finally {
                    cellsBusy = 0;
                }
                if (init)
                    break;
            }
            else if (casBase(v = base,
                             ((fn == null) ?
                              Double.doubleToRawLongBits
                              (Double.longBitsToDouble(v) + x) :
                              Double.doubleToRawLongBits
                              (fn.applyAsDouble
                               (Double.longBitsToDouble(v), x)))))
                break;                          // 回退到使用 base
        }
    }

                // 不安全机制
    private static final sun.misc.Unsafe UNSAFE;
    private static final long BASE;
    private static final long CELLSBUSY;
    private static final long PROBE;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> sk = Striped64.class;
            BASE = UNSAFE.objectFieldOffset
                (sk.getDeclaredField("base"));
            CELLSBUSY = UNSAFE.objectFieldOffset
                (sk.getDeclaredField("cellsBusy"));
            Class<?> tk = Thread.class;
            PROBE = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("threadLocalRandomProbe"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
