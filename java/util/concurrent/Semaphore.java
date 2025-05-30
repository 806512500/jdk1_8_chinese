
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

package java.util.concurrent;
import java.util.Collection;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * 一个计数信号量。概念上，信号量维护一组许可。每次 {@link #acquire} 都会阻塞，直到有许可可用，然后获取它。每次 {@link #release} 都会添加一个许可，可能会释放一个阻塞的获取者。
 * 然而，实际上并没有使用实际的许可对象；信号量只是保持可用许可的数量并相应地操作。
 *
 * <p>信号量通常用于限制可以访问某些（物理或逻辑）资源的线程数量。例如，以下是一个使用信号量控制池中项目访问的类：
 *  <pre> {@code
 * class Pool {
 *   private static final int MAX_AVAILABLE = 100;
 *   private final Semaphore available = new Semaphore(MAX_AVAILABLE, true);
 *
 *   public Object getItem() throws InterruptedException {
 *     available.acquire();
 *     return getNextAvailableItem();
 *   }
 *
 *   public void putItem(Object x) {
 *     if (markAsUnused(x))
 *       available.release();
 *   }
 *
 *   // 不是一个特别高效的结构；仅用于演示
 *
 *   protected Object[] items = ... 无论管理什么类型的项目
 *   protected boolean[] used = new boolean[MAX_AVAILABLE];
 *
 *   protected synchronized Object getNextAvailableItem() {
 *     for (int i = 0; i < MAX_AVAILABLE; ++i) {
 *       if (!used[i]) {
 *          used[i] = true;
 *          return items[i];
 *       }
 *     }
 *     return null; // 不会到达
 *   }
 *
 *   protected synchronized boolean markAsUnused(Object item) {
 *     for (int i = 0; i < MAX_AVAILABLE; ++i) {
 *       if (item == items[i]) {
 *          if (used[i]) {
 *            used[i] = false;
 *            return true;
 *          } else
 *            return false;
 *       }
 *     }
 *     return false;
 *   }
 * }}</pre>
 *
 * <p>每个线程在获取项目之前必须从信号量中获取一个许可，以确保有项目可用。当线程完成对项目的使用后，项目会被返回到池中，并且许可也会返回到信号量，允许另一个线程获取该项目。注意，调用 {@link #acquire} 时没有持有任何同步锁，因为这会阻止项目返回到池中。信号量封装了限制池访问所需的同步，而与维护池本身一致性所需的同步分开。
 *
 * <p>初始化为一且使用时最多只有一个许可可用的信号量可以作为互斥锁。这更常被称为 <em>二进制信号量</em>，因为它只有两种状态：一个许可可用，或零个许可可用。当以这种方式使用时，二进制信号量具有（与许多 {@link java.util.concurrent.locks.Lock} 实现不同）“锁”可以由非所有者线程释放的特性（因为信号量没有所有者概念）。这在某些特定上下文中（如死锁恢复）可能有用。
 *
 * <p>此类的构造函数可选地接受一个 <em>公平性</em> 参数。当设置为 false 时，此类不对线程获取许可的顺序做出任何保证。特别是，允许 <em>插队</em>，即一个调用 {@link #acquire} 的线程可以在等待的线程之前获得许可——逻辑上新线程将自己置于等待线程队列的头部。当公平性设置为 true 时，信号量保证调用任何 {@link #acquire() acquire} 方法的线程按其调用这些方法的顺序获取许可（先进先出；FIFO）。注意，FIFO 顺序必然适用于这些方法内部的特定执行点。因此，一个线程可能在另一个线程之前调用 {@code acquire}，但到达顺序点时却在另一个线程之后，同样地，从方法返回时也是如此。还请注意，未定时的 {@link #tryAcquire() tryAcquire} 方法不遵守公平性设置，但会获取任何可用的许可。
 *
 * <p>通常，用于控制资源访问的信号量应初始化为公平，以确保没有线程被饿死而无法访问资源。当使用信号量进行其他类型的同步控制时，非公平顺序的吞吐量优势通常超过公平性考虑。
 *
 * <p>此类还提供了方便的方法来 {@link #acquire(int) 同时获取} 和 {@link #release(int) 释放} 多个许可。当这些方法在公平性未设置为 true 的情况下使用时，应警惕无限期延迟的风险。
 *
 * <p>内存一致性效果：在一个线程中调用如 {@code release()} 之类的“释放”方法之前的操作 <a href="package-summary.html#MemoryVisibility"><i>先于</i></a> 另一个线程中调用如 {@code acquire()} 之类的“获取”方法之后的操作。
 *
 * @since 1.5
 * @author Doug Lea
 */
public class Semaphore implements java.io.Serializable {
    private static final long serialVersionUID = -3222578661600680210L;
    /** 所有机制通过 AbstractQueuedSynchronizer 子类实现 */
    private final Sync sync;

    /**
     * 信号量的同步实现。使用 AQS 状态表示许可。子类化为公平和非公平版本。
     */
    abstract static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 1192457210091910933L;

        Sync(int permits) {
            setState(permits);
        }

        final int getPermits() {
            return getState();
        }

        final int nonfairTryAcquireShared(int acquires) {
            for (;;) {
                int available = getState();
                int remaining = available - acquires;
                if (remaining < 0 ||
                    compareAndSetState(available, remaining))
                    return remaining;
            }
        }

        protected final boolean tryReleaseShared(int releases) {
            for (;;) {
                int current = getState();
                int next = current + releases;
                if (next < current) // 溢出
                    throw new Error("最大许可数超出");
                if (compareAndSetState(current, next))
                    return true;
            }
        }

        final void reducePermits(int reductions) {
            for (;;) {
                int current = getState();
                int next = current - reductions;
                if (next > current) // 溢出
                    throw new Error("许可数下溢");
                if (compareAndSetState(current, next))
                    return;
            }
        }

        final int drainPermits() {
            for (;;) {
                int current = getState();
                if (current == 0 || compareAndSetState(current, 0))
                    return current;
            }
        }
    }

    /**
     * 非公平版本
     */
    static final class NonfairSync extends Sync {
        private static final long serialVersionUID = -2694183684443567898L;

        NonfairSync(int permits) {
            super(permits);
        }

        protected int tryAcquireShared(int acquires) {
            return nonfairTryAcquireShared(acquires);
        }
    }

    /**
     * 公平版本
     */
    static final class FairSync extends Sync {
        private static final long serialVersionUID = 2014338818796000944L;

        FairSync(int permits) {
            super(permits);
        }

        protected int tryAcquireShared(int acquires) {
            for (;;) {
                if (hasQueuedPredecessors())
                    return -1;
                int available = getState();
                int remaining = available - acquires;
                if (remaining < 0 ||
                    compareAndSetState(available, remaining))
                    return remaining;
            }
        }
    }

    /**
     * 创建一个具有指定数量许可和非公平公平性设置的 {@code Semaphore}。
     *
     * @param permits 初始可用的许可数量。
     *        此值可以为负，在这种情况下，必须先释放许可，然后才能授予任何获取请求。
     */
    public Semaphore(int permits) {
        sync = new NonfairSync(permits);
    }

    /**
     * 创建一个具有指定数量许可和指定公平性设置的 {@code Semaphore}。
     *
     * @param permits 初始可用的许可数量。
     *        此值可以为负，在这种情况下，必须先释放许可，然后才能授予任何获取请求。
     * @param fair {@code true} 表示此信号量将在争用下保证先到先得的许可授予，否则为 {@code false}
     */
    public Semaphore(int permits, boolean fair) {
        sync = fair ? new FairSync(permits) : new NonfairSync(permits);
    }

    /**
     * 从这个信号量中获取一个许可，直到有许可可用或线程被 {@linkplain Thread#interrupt 中断}。
     *
     * <p>如果有一个许可可用，则立即获取许可并返回，减少可用许可的数量。
     *
     * <p>如果没有许可可用，则当前线程将被禁用以进行线程调度，并进入休眠状态，直到发生以下两种情况之一：
     * <ul>
     * <li>其他线程调用此信号量的 {@link #release} 方法，并且当前线程是下一个被分配许可的线程；或
     * <li>其他线程 {@linkplain Thread#interrupt 中断} 当前线程。
     * </ul>
     *
     * <p>如果当前线程：
     * <ul>
     * <li>在进入此方法时已设置中断状态；或
     * <li>在等待许可时被 {@linkplain Thread#interrupt 中断}，
     * </ul>
     * 则抛出 {@link InterruptedException} 并清除当前线程的中断状态。
     *
     * @throws InterruptedException 如果当前线程被中断
     */
    public void acquire() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    /**
     * 从这个信号量中获取一个许可，直到有许可可用。
     *
     * <p>如果有一个许可可用，则立即获取许可并返回，减少可用许可的数量。
     *
     * <p>如果没有许可可用，则当前线程将被禁用以进行线程调度，并进入休眠状态，直到其他线程调用此信号量的 {@link #release} 方法，并且当前线程是下一个被分配许可的线程。
     *
     * <p>如果当前线程在等待许可时被 {@linkplain Thread#interrupt 中断}，则它将继续等待，但分配许可的时间可能会与未发生中断时的时间不同。当线程从该方法返回时，其中断状态将被设置。
     */
    public void acquireUninterruptibly() {
        sync.acquireShared(1);
    }

    /**
     * 仅在调用时有许可可用的情况下从这个信号量中获取一个许可。
     *
     * <p>如果有一个许可可用，则立即获取许可并返回 {@code true}，减少可用许可的数量。
     *
     * <p>如果没有许可可用，则此方法将立即返回 {@code false}。
     *
     * <p>即使此信号量已设置为使用公平排序策略，调用 {@code tryAcquire()} <em>将</em> 立即获取许可（如果可用），无论是否有其他线程正在等待。
     * 这种“插队”行为在某些情况下可能有用，尽管它破坏了公平性。如果希望遵守公平性设置，则使用
     * {@link #tryAcquire(long, TimeUnit) tryAcquire(0, TimeUnit.SECONDS) }，这几乎是等效的（它还检测中断）。
     *
     * @return 如果获取了许可则返回 {@code true}，否则返回 {@code false}
     */
    public boolean tryAcquire() {
        return sync.nonfairTryAcquireShared(1) >= 0;
    }

    /**
     * 从这个信号量中获取一个许可，如果在指定的等待时间内有许可可用且当前线程未被 {@linkplain Thread#interrupt 中断}。
     *
     * <p>如果有一个许可可用，则立即获取许可并返回 {@code true}，减少可用许可的数量。
     *
     * <p>如果没有许可可用，则当前线程将被禁用以进行线程调度，并进入休眠状态，直到发生以下三种情况之一：
     * <ul>
     * <li>其他线程调用此信号量的 {@link #release} 方法，并且当前线程是下一个被分配许可的线程；或
     * <li>其他线程 {@linkplain Thread#interrupt 中断} 当前线程；或
     * <li>指定的等待时间已过。
     * </ul>
     *
     * <p>如果获取了许可，则返回 {@code true}。
     *
     * <p>如果当前线程：
     * <ul>
     * <li>在进入此方法时已设置中断状态；或
     * <li>在等待许可时被 {@linkplain Thread#interrupt 中断}，
     * </ul>
     * 则抛出 {@link InterruptedException} 并清除当前线程的中断状态。
     *
     * <p>如果指定的等待时间已过，则返回 {@code false}。如果时间小于或等于零，则该方法将不会等待。
     *
     * @param timeout 等待许可的最大时间
     * @param unit {@code timeout} 参数的时间单位
     * @return 如果获取了许可则返回 {@code true}，如果等待时间已过且未获取许可则返回 {@code false}
     * @throws InterruptedException 如果当前线程被中断
     */
    public boolean tryAcquire(long timeout, TimeUnit unit)
        throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }


                /**
     * 释放一个许可，返回给信号量。
     *
     * <p>释放一个许可，增加可用许可的数量
     * 一个。如果有线程正在尝试获取许可，那么选择一个
     * 并给予刚刚释放的许可。该线程
     * 被（重新）启用以进行线程调度。
     *
     * <p>没有要求释放许可的线程必须
     * 通过调用 {@link #acquire} 获取该许可。
     * 信号量的正确使用由应用程序中的编程约定建立。
     */
    public void release() {
        sync.releaseShared(1);
    }

    /**
     * 从这个信号量中获取指定数量的许可，
     * 阻塞直到所有许可都可用，
     * 或当前线程被 {@linkplain Thread#interrupt 中断}。
     *
     * <p>如果可用的许可足够，则获取指定数量的许可，
     * 并立即返回，减少可用许可的数量
     * 指定的数量。
     *
     * <p>如果可用的许可不足，则当前线程将
     * 被禁用以进行线程调度并处于休眠状态，直到
     * 发生以下两种情况之一：
     * <ul>
     * <li>其他线程调用此信号量的 {@link #release() release}
     * 方法，当前线程是下一个被分配许可的线程，且
     * 可用的许可数量满足此请求；或
     * <li>其他线程 {@linkplain Thread#interrupt 中断}
     * 当前线程。
     * </ul>
     *
     * <p>如果当前线程：
     * <ul>
     * <li>在进入此方法时已设置中断状态；或
     * <li>在等待许可时被 {@linkplain Thread#interrupt 中断}，
     * </ul>
     * 则抛出 {@link InterruptedException} 并清除当前线程的
     * 中断状态。原本要分配给此线程的许可
     * 而是分配给其他尝试获取许可的线程，就像
     * 许可是通过调用 {@link #release()} 释放的一样。
     *
     * @param permits 要获取的许可数量
     * @throws InterruptedException 如果当前线程被中断
     * @throws IllegalArgumentException 如果 {@code permits} 为负
     */
    public void acquire(int permits) throws InterruptedException {
        if (permits < 0) throw new IllegalArgumentException();
        sync.acquireSharedInterruptibly(permits);
    }

    /**
     * 从这个信号量中获取指定数量的许可，
     * 阻塞直到所有许可都可用。
     *
     * <p>如果可用的许可足够，则获取指定数量的许可，
     * 并立即返回，减少可用许可的数量
     * 指定的数量。
     *
     * <p>如果可用的许可不足，则当前线程将
     * 被禁用以进行线程调度并处于休眠状态，直到
     * 其他线程调用此信号量的 {@link #release() release}
     * 方法，当前线程是下一个被分配许可的线程，且
     * 可用的许可数量满足此请求。
     *
     * <p>如果当前线程被 {@linkplain Thread#interrupt 中断}
     * 在等待许可时，它将继续等待，其在队列中的位置不受影响。当
     * 线程从这个方法返回时，其中断状态将被设置。
     *
     * @param permits 要获取的许可数量
     * @throws IllegalArgumentException 如果 {@code permits} 为负
     */
    public void acquireUninterruptibly(int permits) {
        if (permits < 0) throw new IllegalArgumentException();
        sync.acquireShared(permits);
    }

    /**
     * 从这个信号量中获取指定数量的许可，仅
     * 如果所有许可在调用时都可用。
     *
     * <p>如果可用的许可足够，则获取指定数量的许可，
     * 并立即返回，值为 {@code true}，
     * 减少可用许可的数量
     * 指定的数量。
     *
     * <p>如果可用的许可不足，则此方法将立即返回
     * 值为 {@code false}，且可用的许可数量不变。
     *
     * <p>即使此信号量已设置为使用公平排序
     * 策略，调用 {@code tryAcquire} <em>将</em>
     * 立即获取许可，如果许可可用，无论
     * 其他线程是否当前正在等待。这种
     * “插队”行为在某些情况下是有用的，即使它破坏了公平性。如果您想
     * 尊重公平设置，则使用 {@link #tryAcquire(int,
     * long, TimeUnit) tryAcquire(permits, 0, TimeUnit.SECONDS) }
     * 这几乎是等效的（它还检测中断）。
     *
     * @param permits 要获取的许可数量
     * @return 如果获取了许可则返回 {@code true}，
     *         否则返回 {@code false}
     * @throws IllegalArgumentException 如果 {@code permits} 为负
     */
    public boolean tryAcquire(int permits) {
        if (permits < 0) throw new IllegalArgumentException();
        return sync.nonfairTryAcquireShared(permits) >= 0;
    }

    /**
     * 从这个信号量中获取指定数量的许可，如果所有
     * 许可在给定的等待时间内可用且当前
     * 线程未被 {@linkplain Thread#interrupt 中断}。
     *
     * <p>如果可用的许可足够，则获取指定数量的许可，
     * 并立即返回，值为 {@code true}，
     * 减少可用许可的数量
     * 指定的数量。
     *
     * <p>如果可用的许可不足，则当前线程将
     * 被禁用以进行线程调度
     * 目的并处于休眠状态，直到以下三种情况之一发生：
     * <ul>
     * <li>其他线程调用此信号量的 {@link #release() release}
     * 方法，当前线程是下一个被分配许可的线程，且
     * 可用的许可数量满足此请求；或
     * <li>其他线程 {@linkplain Thread#interrupt 中断}
     * 当前线程；或
     * <li>指定的等待时间已过。
     * </ul>
     *
     * <p>如果获取了许可，则返回值为 {@code true}。
     *
     * <p>如果当前线程：
     * <ul>
     * <li>在进入此方法时已设置中断状态；或
     * <li>在等待获取许可时被 {@linkplain Thread#interrupt 中断}，
     * </ul>
     * 则抛出 {@link InterruptedException} 并清除当前线程的
     * 中断状态。原本要分配给此线程的许可
     * 而是分配给其他尝试获取许可的线程，就像
     * 许可是通过调用 {@link #release()} 释放的一样。
     *
     * <p>如果指定的等待时间已过，则返回值为 {@code false}。
     * 如果时间小于或等于零，方法将不等待。
     * 任何原本要分配给此线程的许可
     * 而是分配给其他尝试获取许可的线程，就像
     * 许可是通过调用 {@link #release()} 释放的一样。
     *
     * @param permits 要获取的许可数量
     * @param timeout 等待许可的最大时间
     * @param unit {@code timeout} 参数的时间单位
     * @return 如果所有许可都被获取则返回 {@code true}，如果等待时间已过且未获取所有许可则返回 {@code false}
     * @throws InterruptedException 如果当前线程被中断
     * @throws IllegalArgumentException 如果 {@code permits} 为负
     */
    public boolean tryAcquire(int permits, long timeout, TimeUnit unit)
        throws InterruptedException {
        if (permits < 0) throw new IllegalArgumentException();
        return sync.tryAcquireSharedNanos(permits, unit.toNanos(timeout));
    }

    /**
     * 释放指定数量的许可，返回给信号量。
     *
     * <p>释放指定数量的许可，增加可用许可的数量
     * 该数量。如果有线程正在尝试获取许可，那么选择一个
     * 并给予刚刚释放的许可。如果可用的许可数量满足该线程的请求
     * 则该线程被（重新）启用以进行线程调度；
     * 否则该线程将等待直到有足够的许可可用。
     * 如果满足此线程的请求后仍有许可可用，
     * 则这些许可依次分配给其他尝试获取许可的线程。
     *
     * <p>没有要求释放许可的线程必须
     * 通过调用 {@link Semaphore#acquire acquire} 获取该许可。
     * 信号量的正确使用由应用程序中的编程约定建立。
     *
     * @param permits 要释放的许可数量
     * @throws IllegalArgumentException 如果 {@code permits} 为负
     */
    public void release(int permits) {
        if (permits < 0) throw new IllegalArgumentException();
        sync.releaseShared(permits);
    }

    /**
     * 返回此信号量中当前可用的许可数量。
     *
     * <p>此方法通常用于调试和测试目的。
     *
     * @return 此信号量中可用的许可数量
     */
    public int availablePermits() {
        return sync.getPermits();
    }

    /**
     * 获取并返回所有立即可用的许可。
     *
     * @return 获取的许可数量
     */
    public int drainPermits() {
        return sync.drainPermits();
    }

    /**
     * 将可用的许可数量减少指定的数量。此方法在子类中很有用，
     * 用于跟踪变得不可用的资源。此方法与 {@code acquire} 不同，
     * 它不会阻塞等待许可变得可用。
     *
     * @param reduction 要移除的许可数量
     * @throws IllegalArgumentException 如果 {@code reduction} 为负
     */
    protected void reducePermits(int reduction) {
        if (reduction < 0) throw new IllegalArgumentException();
        sync.reducePermits(reduction);
    }

    /**
     * 如果此信号量设置了公平性，则返回 {@code true}。
     *
     * @return 如果此信号量设置了公平性则返回 {@code true}
     */
    public boolean isFair() {
        return sync instanceof FairSync;
    }

    /**
     * 查询是否有线程正在等待获取。请注意，
     * 由于取消可能随时发生，因此 {@code true}
     * 返回值不保证任何其他线程将最终
     * 获取。此方法主要用于监控系统状态。
     *
     * @return 如果可能有其他线程正在等待获取锁则返回 {@code true}
     */
    public final boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    /**
     * 返回估计的等待获取的线程数量。
     * 由于线程数量可能在遍历内部数据
     * 结构时动态变化，因此返回的值只是一个估计值。此方法主要用于监控系统状态，而不是用于同步控制。
     *
     * @return 估计的等待此锁的线程数量
     */
    public final int getQueueLength() {
        return sync.getQueueLength();
    }

    /**
     * 返回可能正在等待获取的线程集合。
     * 由于实际的线程集可能在构建此结果时动态变化，
     * 返回的集合只是一个尽力而为的估计值。返回的集合中的元素没有特定的顺序。此方法旨在便于构建提供更广泛的监控设施的子类。
     *
     * @return 线程集合
     */
    protected Collection<Thread> getQueuedThreads() {
        return sync.getQueuedThreads();
    }

    /**
     * 返回一个标识此信号量及其状态的字符串。
     * 状态，用括号表示，包括字符串 {@code "Permits ="}
     * 后跟许可的数量。
     *
     * @return 一个标识此信号量及其状态的字符串
     */
    public String toString() {
        return super.toString() + "[Permits = " + sync.getPermits() + "]";
    }
}
