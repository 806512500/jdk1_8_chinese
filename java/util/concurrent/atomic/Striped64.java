
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
import java.util.function.LongBinaryOperator;
import java.util.function.DoubleBinaryOperator;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 一个包级类，用于支持在64位值上动态分条的类的公共表示和机制。该类扩展了Number，因此具体子类必须公开扩展。
 */
@SuppressWarnings("serial")
abstract class Striped64 extends Number {
    /*
     * 该类维护一个延迟初始化的原子更新变量表，以及一个额外的“base”字段。表的大小是2的幂。索引使用每线程哈希码。该类中的大多数声明都是包级私有的，由子类直接访问。
     *
     * 表项是Cell类的实例；Cell类是AtomicLong的变体，通过@sun.misc.Contended进行填充以减少缓存争用。填充对于大多数原子类来说是过度的，因为它们通常在内存中不规则地分布，因此彼此之间的干扰不大。但是，数组中的原子对象往往会相邻放置，因此在没有这种预防措施的情况下，大多数情况下会共享缓存行（对性能有巨大的负面影响）。
     *
     * 由于Cell相对较大，我们避免在需要时创建它们。当没有争用时，所有更新都针对base字段进行。首次争用（在base更新时CAS失败）时，表初始化为大小2。进一步的争用时，表大小翻倍，直到达到大于或等于CPU数量的最近的2的幂。表槽在需要时保持为空（null）。
     *
     * 单个自旋锁（“cellsBusy”）用于初始化和调整表大小，以及用新Cell填充槽。不需要阻塞锁；当锁不可用时，线程尝试其他槽（或base）。在这些重试期间，争用增加，局部性降低，但这仍然比其他替代方案更好。
     *
     * 通过ThreadLocalRandom维护的线程探针字段用作每线程哈希码。我们让它们在初始化为零（如果它们以这种方式进入）时保持未初始化，直到它们在槽0处争用。然后它们初始化为通常不会与其他线程频繁冲突的值。争用和/或表冲突通过在执行更新操作时CAS失败来指示。发生冲突时，如果表大小小于容量，则表大小翻倍，除非其他线程持有锁。如果哈希槽为空，且锁可用，则创建新Cell。否则，如果槽存在，则尝试CAS。重试通过“双重哈希”进行，使用次级哈希（Marsaglia XorShift）尝试找到一个空槽。
     *
     * 表大小是有限制的，因为在线程数量多于CPU数量时，假设每个线程都绑定到一个CPU，则存在一个消除冲突的完美哈希函数。当我们达到容量时，我们通过随机改变冲突线程的哈希码来搜索这个映射。由于搜索是随机的，且冲突仅通过CAS失败来得知，因此收敛可能很慢，而且由于线程通常不会永远绑定到CPU，因此可能根本不会发生。然而，尽管有这些限制，观察到的争用率在这种情况下通常很低。
     *
     * 当曾经哈希到它的线程终止时，Cell可能会变得未使用，以及在表大小翻倍时，没有线程在扩展掩码下哈希到它。我们不尝试检测或移除这样的Cell，假设对于长时间运行的实例，观察到的争用水平会再次出现，因此这些Cell最终将再次需要；对于短时间运行的实例，这并不重要。
     */

    /**
     * 仅支持原始访问和CAS的AtomicLong的填充变体。
     *
     * JVM内联注释：可以使用仅包含释放的CAS形式，如果它被提供的话。
     */
    @sun.misc.Contended static final class Cell {
        volatile long value;
        Cell(long x) { value = x; }
        final boolean cas(long cmp, long val) {
            return UNSAFE.compareAndSwapLong(this, valueOffset, cmp, val);
        }

        // Unsafe机制
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

    /** CPU数量，用于限制表大小 */
    static final int NCPU = Runtime.getRuntime().availableProcessors();

    /**
     * Cell表。当非null时，大小是2的幂。
     */
    transient volatile Cell[] cells;

    /**
     * 基础值，主要用于没有争用时，但在表初始化竞争期间也作为回退。通过CAS更新。
     */
    transient volatile long base;

    /**
     * 自旋锁（通过CAS锁定）用于调整大小和/或创建Cells。
     */
    transient volatile int cellsBusy;

    /**
     * 包级默认构造函数
     */
    Striped64() {
    }

    /**
     * CAS基础字段。
     */
    final boolean casBase(long cmp, long val) {
        return UNSAFE.compareAndSwapLong(this, BASE, cmp, val);
    }

    /**
     * CAS cellsBusy字段从0到1以获取锁。
     */
    final boolean casCellsBusy() {
        return UNSAFE.compareAndSwapInt(this, CELLSBUSY, 0, 1);
    }

    /**
     * 返回当前线程的探针值。
     * 由于包限制，从ThreadLocalRandom复制。
     */
    static final int getProbe() {
        return UNSAFE.getInt(Thread.currentThread(), PROBE);
    }

    /**
     * 伪随机地推进并记录给定线程的给定探针值。
     * 由于包限制，从ThreadLocalRandom复制。
     */
    static final int advanceProbe(int probe) {
        probe ^= probe << 13;   // xorshift
        probe ^= probe >>> 17;
        probe ^= probe << 5;
        UNSAFE.putInt(Thread.currentThread(), PROBE, probe);
        return probe;
    }

    /**
     * 处理涉及初始化、调整大小、创建新Cells和/或争用的更新情况。参见上述解释。此方法遭受乐观重试代码的通常非模块化问题，依赖于重新检查的读取集。
     *
     * @param x 值
     * @param fn 更新函数，或null表示加法（此约定避免了在LongAdder中需要额外的字段或函数）。
     * @param wasUncontended 如果CAS在调用前失败，则为false
     */
    final void longAccumulate(long x, LongBinaryOperator fn,
                              boolean wasUncontended) {
        int h;
        if ((h = getProbe()) == 0) {
            ThreadLocalRandom.current(); // 强制初始化
            h = getProbe();
            wasUncontended = true;
        }
        boolean collide = false;                // 如果最后一个槽非空，则为true
        for (;;) {
            Cell[] as; Cell a; int n; long v;
            if ((as = cells) != null && (n = as.length) > 0) {
                if ((a = as[(n - 1) & h]) == null) {
                    if (cellsBusy == 0) {       // 尝试附加新Cell
                        Cell r = new Cell(x);   // 乐观地创建
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
                else if (!wasUncontended)       // CAS已知失败
                    wasUncontended = true;      // 重新哈希后继续
                else if (a.cas(v = a.value, ((fn == null) ? v + x :
                                             fn.applyAsLong(v, x))))
                    break;
                else if (n >= NCPU || cells != as)
                    collide = false;            // 达到最大大小或过时
                else if (!collide)
                    collide = true;
                else if (cellsBusy == 0 && casCellsBusy()) {
                    try {
                        if (cells == as) {      // 除非过时，否则扩展表
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
                break;                          // 回退到使用base
        }
    }

    /**
     * 与longAccumulate相同，但在太多地方注入了long/double转换，无法合理地与long版本合并，鉴于此类的低开销要求。因此必须通过复制/粘贴/适应来维护。
     */
    final void doubleAccumulate(double x, DoubleBinaryOperator fn,
                                boolean wasUncontended) {
        int h;
        if ((h = getProbe()) == 0) {
            ThreadLocalRandom.current(); // 强制初始化
            h = getProbe();
            wasUncontended = true;
        }
        boolean collide = false;                // 如果最后一个槽非空，则为true
        for (;;) {
            Cell[] as; Cell a; int n; long v;
            if ((as = cells) != null && (n = as.length) > 0) {
                if ((a = as[(n - 1) & h]) == null) {
                    if (cellsBusy == 0) {       // 尝试附加新Cell
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
                else if (!wasUncontended)       // CAS已知失败
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
                    collide = false;            // 达到最大大小或过时
                else if (!collide)
                    collide = true;
                else if (cellsBusy == 0 && casCellsBusy()) {
                    try {
                        if (cells == as) {      // 除非过时，否则扩展表
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
                break;                          // 回退到使用base
        }
    }


                // Unsafe 机制
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
