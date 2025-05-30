
/*
 * Copyright (c) 1994, 2016, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.AccessControlContext;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.LockSupport;

import jdk.internal.misc.TerminatingThreadLocal;
import sun.nio.ch.Interruptible;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import sun.security.util.SecurityConstants;


/**
 * 一个<i>线程</i>是程序中的一个执行线程。Java虚拟机允许一个应用程序拥有多个线程并发执行。
 * <p>
 * 每个线程都有一个优先级。优先级较高的线程优先于优先级较低的线程执行。每个线程可以被标记为守护线程。当某个线程中的代码创建一个新的<code>Thread</code>对象时，新线程的优先级最初设置为创建线程的优先级，并且仅当创建线程是守护线程时，新线程才是守护线程。
 * <p>
 * 当Java虚拟机启动时，通常只有一个非守护线程（通常调用某个指定类的<code>main</code>方法）。Java虚拟机继续执行线程，直到以下情况之一发生：
 * <ul>
 * <li>类<code>Runtime</code>的<code>exit</code>方法已被调用，并且安全经理允许退出操作进行。
 * <li>所有非守护线程都已死亡，无论是通过从<code>run</code>方法返回还是通过抛出传播到<code>run</code>方法之外的异常。
 * </ul>
 * <p>
 * 创建新线程有两种方法。一种是声明一个类为<code>Thread</code>的子类。该子类应覆盖类<code>Thread</code>的<code>run</code>方法。然后可以分配该子类的实例并启动它。例如，一个计算大于指定值的质数的线程可以如下编写：
 * <hr><blockquote><pre>
 *     class PrimeThread extends Thread {
 *         long minPrime;
 *         PrimeThread(long minPrime) {
 *             this.minPrime = minPrime;
 *         }
 *
 *         public void run() {
 *             // 计算大于 minPrime 的质数
 *             &nbsp;.&nbsp;.&nbsp;.
 *         }
 *     }
 * </pre></blockquote><hr>
 * <p>
 * 以下代码将创建一个线程并启动它：
 * <blockquote><pre>
 *     PrimeThread p = new PrimeThread(143);
 *     p.start();
 * </pre></blockquote>
 * <p>
 * 创建线程的另一种方法是声明一个实现<code>Runnable</code>接口的类。该类然后实现<code>run</code>方法。然后可以分配该类的实例，作为创建<code>Thread</code>时的参数传递，并启动它。使用此方法的相同示例如下：
 * <hr><blockquote><pre>
 *     class PrimeRun implements Runnable {
 *         long minPrime;
 *         PrimeRun(long minPrime) {
 *             this.minPrime = minPrime;
 *         }
 *
 *         public void run() {
 *             // 计算大于 minPrime 的质数
 *             &nbsp;.&nbsp;.&nbsp;.
 *         }
 *     }
 * </pre></blockquote><hr>
 * <p>
 * 以下代码将创建一个线程并启动它：
 * <blockquote><pre>
 *     PrimeRun p = new PrimeRun(143);
 *     new Thread(p).start();
 * </pre></blockquote>
 * <p>
 * 每个线程都有一个名称，用于识别目的。多个线程可以具有相同的名称。如果在创建线程时未指定名称，则会为它生成一个新名称。
 * <p>
 * 除非另有说明，否则将<code>null</code>参数传递给此类的构造函数或方法将导致抛出<code>NullPointerException</code>。
 *
 * @author  未署名
 * @see     Runnable
 * @see     Runtime#exit(int)
 * @see     #run()
 * @see     #stop()
 * @since   JDK1.0
 */
public
class Thread implements Runnable {
    /* 确保 registerNatives 是 <clinit> 执行的第一件事。 */
    private static native void registerNatives();
    static {
        registerNatives();
    }

    private volatile String name;
    private int            priority;
    private Thread         threadQ;
    private long           eetop;

    /* 是否对这个线程进行单步调试。 */
    private boolean     single_step;

    /* 是否将线程标记为守护线程。 */
    private boolean     daemon = false;

    /* JVM 状态 */
    private boolean     stillborn = false;

    /* 将要运行的对象。 */
    private Runnable target;

    /* 线程所属的组。 */
    private ThreadGroup group;

    /* 该线程的上下文类加载器。 */
    private ClassLoader contextClassLoader;

    /* 该线程继承的访问控制上下文。 */
    private AccessControlContext inheritedAccessControlContext;

    /* 用于自动编号匿名线程。 */
    private static int threadInitNumber;
    private static synchronized int nextThreadNum() {
        return threadInitNumber++;
    }

    /* 与该线程相关的 ThreadLocal 值。此映射由 ThreadLocal 类维护。 */
    ThreadLocal.ThreadLocalMap threadLocals = null;

    /*
     * 与该线程相关的 InheritableThreadLocal 值。此映射由 InheritableThreadLocal 类维护。
     */
    ThreadLocal.ThreadLocalMap inheritableThreadLocals = null;

    /*
     * 该线程请求的堆栈大小，如果创建者未指定堆栈大小，则为 0。VM 可以根据需要处理这个数字；某些 VM 可能会忽略它。
     */
    private long stackSize;

    /*
     * 线程终止后仍保留的 JVM 私有状态。
     */
    private long nativeParkEventPointer;

    /*
     * 线程 ID
     */
    private long tid;

    /* 用于生成线程 ID */
    private static long threadSeqNumber;

    /* 用于工具的 Java 线程状态，初始化为表示线程“尚未启动” */
    private volatile int threadStatus = 0;

    private static synchronized long nextThreadID() {
        return ++threadSeqNumber;
    }

    /**
     * 当前调用 java.util.concurrent.locks.LockSupport.park 时的参数。
     * 由 (私有) java.util.concurrent.locks.LockSupport.setBlocker 设置
     * 使用 java.util.concurrent.locks.LockSupport.getBlocker 访问
     */
    volatile Object parkBlocker;

    /* 该线程在其中被阻塞的可中断 I/O 操作的对象，如果有。应在线程的中断状态设置后调用阻塞器的中断方法。 */
    private volatile Interruptible blocker;
    private final Object blockerLock = new Object();

    /* 设置阻塞器字段；通过 sun.misc.SharedSecrets 从 java.nio 代码调用 */
    void blockedOn(Interruptible b) {
        synchronized (blockerLock) {
            blocker = b;
        }
    }

    /**
     * 线程可以拥有的最小优先级。
     */
    public final static int MIN_PRIORITY = 1;

   /**
     * 分配给线程的默认优先级。
     */
    public final static int NORM_PRIORITY = 5;

    /**
     * 线程可以拥有的最大优先级。
     */
    public final static int MAX_PRIORITY = 10;

    /**
     * 返回当前正在执行的线程对象的引用。
     *
     * @return  当前正在执行的线程。
     */
    public static native Thread currentThread();

    /**
     * 向调度器提供一个提示，表示当前线程愿意放弃其当前对处理器的使用。调度器可以忽略此提示。
     *
     * <p> Yield 是一种启发式尝试，旨在改善否则会过度利用 CPU 的线程之间的相对进度。其使用应结合详细的性能分析和基准测试，以确保它确实产生了预期的效果。
     *
     * <p> 通常不适当使用此方法。它可能对调试或测试有用，因为它有助于重现由于竞态条件引起的错误。在设计如 {@link java.util.concurrent.locks} 包中的并发控制构造时，它也可能有用。
     */
    public static native void yield();

    /**
     * 使当前正在执行的线程睡眠（暂时停止执行）指定的毫秒数，受系统计时器和调度器的精度和准确性的影响。线程不会失去任何监视器的所有权。
     *
     * @param  millis
     *         睡眠的毫秒数
     *
     * @throws  IllegalArgumentException
     *          如果 {@code millis} 的值为负
     *
     * @throws  InterruptedException
     *          如果任何线程中断了当前线程。当前线程的 <i>中断状态</i> 在抛出此异常时被清除。
     */
    public static native void sleep(long millis) throws InterruptedException;

    /**
     * 使当前正在执行的线程睡眠（暂时停止执行）指定的毫秒数加上指定的纳秒数，受系统计时器和调度器的精度和准确性的影响。线程不会失去任何监视器的所有权。
     *
     * @param  millis
     *         睡眠的毫秒数
     *
     * @param  nanos
     *         {@code 0-999999} 附加纳秒数
     *
     * @throws  IllegalArgumentException
     *          如果 {@code millis} 的值为负，或 {@code nanos} 的值不在 {@code 0-999999} 范围内
     *
     * @throws  InterruptedException
     *          如果任何线程中断了当前线程。当前线程的 <i>中断状态</i> 在抛出此异常时被清除。
     */
    public static void sleep(long millis, int nanos)
    throws InterruptedException {
        if (millis < 0) {
            throw new IllegalArgumentException("超时值为负");
        }

        if (nanos < 0 || nanos > 999999) {
            throw new IllegalArgumentException(
                                "纳秒超时值超出范围");
        }

        if (nanos >= 500000 || (nanos != 0 && millis == 0)) {
            millis++;
        }

        sleep(millis);
    }

    /**
     * 使用当前的 AccessControlContext 初始化一个线程。
     * @see #init(ThreadGroup,Runnable,String,long,AccessControlContext,boolean)
     */
    private void init(ThreadGroup g, Runnable target, String name,
                      long stackSize) {
        init(g, target, name, stackSize, null, true);
    }

    /**
     * 初始化一个线程。
     *
     * @param g 线程组
     * @param target 其 <code>run</code> 方法被调用的对象
     * @param name 新线程的名称
     * @param stackSize 新线程所需的堆栈大小，或零表示忽略此参数
     * @param acc 要继承的 AccessControlContext，如果为 null，则使用 AccessController.getContext()
     * @param inheritThreadLocals 如果为 {@code true}，则从构造线程继承可继承的线程局部变量的初始值
     */
    private void init(ThreadGroup g, Runnable target, String name,
                      long stackSize, AccessControlContext acc,
                      boolean inheritThreadLocals) {
        if (name == null) {
            throw new NullPointerException("名称不能为空");
        }

        this.name = name;

        Thread parent = currentThread();
        SecurityManager security = System.getSecurityManager();
        if (g == null) {
            /* 确定是否为应用程序 */
            /* 如果有安全经理，询问安全经理怎么做。 */
            if (security != null) {
                g = security.getThreadGroup();
            }

            /* 如果安全经理对此没有强烈意见，则使用父线程组。 */
            if (g == null) {
                g = parent.getThreadGroup();
            }
        }

        /* 无论是否显式传递线程组，都要检查访问权限。 */
        g.checkAccess();

        /*
         * 我们是否有所需的权限？
         */
        if (security != null) {
            if (isCCLOverridden(getClass())) {
                security.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
            }
        }

        g.addUnstarted();

        this.group = g;
        this.daemon = parent.isDaemon();
        this.priority = parent.getPriority();
        if (security == null || isCCLOverridden(parent.getClass()))
            this.contextClassLoader = parent.getContextClassLoader();
        else
            this.contextClassLoader = parent.contextClassLoader;
        this.inheritedAccessControlContext =
                acc != null ? acc : AccessController.getContext();
        this.target = target;
        setPriority(priority);
        if (inheritThreadLocals && parent.inheritableThreadLocals != null)
            this.inheritableThreadLocals =
                ThreadLocal.createInheritedMap(parent.inheritableThreadLocals);
        /* 保存指定的堆栈大小，以供 VM 使用 */
        this.stackSize = stackSize;

        /* 设置线程 ID */
        tid = nextThreadID();
    }

    /**
     * 抛出 CloneNotSupportedException，因为线程不能有意义地克隆。应构造一个新的线程。
     *
     * @throws  CloneNotSupportedException
     *          始终抛出
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }


                /**
     * 分配一个新的 {@code Thread} 对象。此构造函数的效果与 {@linkplain #Thread(ThreadGroup,Runnable,String) Thread}
     * {@code (null, null, gname)} 相同，其中 {@code gname} 是新生成的名称。自动生成的名称形式为
     * {@code "Thread-"+}<i>n</i>，其中 <i>n</i> 是一个整数。
     */
    public Thread() {
        init(null, null, "Thread-" + nextThreadNum(), 0);
    }

    /**
     * 分配一个新的 {@code Thread} 对象。此构造函数的效果与 {@linkplain #Thread(ThreadGroup,Runnable,String) Thread}
     * {@code (null, target, gname)} 相同，其中 {@code gname} 是新生成的名称。自动生成的名称形式为
     * {@code "Thread-"+}<i>n</i>，其中 <i>n</i> 是一个整数。
     *
     * @param  target
     *         当此线程启动时调用其 {@code run} 方法的对象。如果为 {@code null}，则调用此类的 {@code run} 方法。
     */
    public Thread(Runnable target) {
        init(null, target, "Thread-" + nextThreadNum(), 0);
    }

    /**
     * 创建一个继承给定 AccessControlContext 的新线程。这不是一个公共构造函数。
     */
    Thread(Runnable target, AccessControlContext acc) {
        init(null, target, "Thread-" + nextThreadNum(), 0, acc, false);
    }

    /**
     * 分配一个新的 {@code Thread} 对象。此构造函数的效果与 {@linkplain #Thread(ThreadGroup,Runnable,String) Thread}
     * {@code (group, target, gname)} 相同，其中 {@code gname} 是新生成的名称。自动生成的名称形式为
     * {@code "Thread-"+}<i>n</i>，其中 <i>n</i> 是一个整数。
     *
     * @param  group
     *         线程组。如果为 {@code null} 且存在安全经理，则组由 {@linkplain
     *         SecurityManager#getThreadGroup SecurityManager.getThreadGroup()} 确定。
     *         如果没有安全经理或 {@code
     *         SecurityManager.getThreadGroup()} 返回 {@code null}，则组设置为当前线程的线程组。
     *
     * @param  target
     *         当此线程启动时调用其 {@code run} 方法的对象。如果为 {@code null}，则调用此线程的 {@code run} 方法。
     *
     * @throws  SecurityException
     *          如果当前线程无法在指定的线程组中创建线程
     */
    public Thread(ThreadGroup group, Runnable target) {
        init(group, target, "Thread-" + nextThreadNum(), 0);
    }

    /**
     * 分配一个新的 {@code Thread} 对象。此构造函数的效果与 {@linkplain #Thread(ThreadGroup,Runnable,String) Thread}
     * {@code (null, null, name)} 相同。
     *
     * @param   name
     *          新线程的名称
     */
    public Thread(String name) {
        init(null, null, name, 0);
    }

    /**
     * 分配一个新的 {@code Thread} 对象。此构造函数的效果与 {@linkplain #Thread(ThreadGroup,Runnable,String) Thread}
     * {@code (group, null, name)} 相同。
     *
     * @param  group
     *         线程组。如果为 {@code null} 且存在安全经理，则组由 {@linkplain
     *         SecurityManager#getThreadGroup SecurityManager.getThreadGroup()} 确定。
     *         如果没有安全经理或 {@code
     *         SecurityManager.getThreadGroup()} 返回 {@code null}，则组设置为当前线程的线程组。
     *
     * @param  name
     *         新线程的名称
     *
     * @throws  SecurityException
     *          如果当前线程无法在指定的线程组中创建线程
     */
    public Thread(ThreadGroup group, String name) {
        init(group, null, name, 0);
    }

    /**
     * 分配一个新的 {@code Thread} 对象。此构造函数的效果与 {@linkplain #Thread(ThreadGroup,Runnable,String) Thread}
     * {@code (null, target, name)} 相同。
     *
     * @param  target
     *         当此线程启动时调用其 {@code run} 方法的对象。如果为 {@code null}，则调用此线程的 {@code run} 方法。
     *
     * @param  name
     *         新线程的名称
     */
    public Thread(Runnable target, String name) {
        init(null, target, name, 0);
    }

    /**
     * 分配一个新的 {@code Thread} 对象，使其具有 {@code target}
     * 作为其运行对象，具有指定的 {@code name} 作为其名称，并且属于由 {@code group} 引用的线程组。
     *
     * <p>如果有安全经理，其
     * {@link SecurityManager#checkAccess(ThreadGroup) checkAccess}
     * 方法将使用线程组作为其参数调用。
     *
     * <p>此外，其 {@code checkPermission} 方法在直接或间接由子类的构造函数调用时，将使用
     * {@code RuntimePermission("enableContextClassLoaderOverride")}
     * 权限调用，该子类覆盖了 {@code getContextClassLoader}
     * 或 {@code setContextClassLoader} 方法。
     *
     * <p>新创建的线程的优先级设置为创建它的线程的优先级，即当前运行的线程。可以使用 {@linkplain #setPriority setPriority} 方法更改优先级。
     *
     * <p>新创建的线程最初被标记为守护线程，当且仅当创建它的线程当前被标记为守护线程。可以使用 {@linkplain #setDaemon setDaemon} 方法更改是否为守护线程。
     *
     * @param  group
     *         线程组。如果为 {@code null} 且存在安全经理，则组由 {@linkplain
     *         SecurityManager#getThreadGroup SecurityManager.getThreadGroup()} 确定。
     *         如果没有安全经理或 {@code
     *         SecurityManager.getThreadGroup()} 返回 {@code null}，则组设置为当前线程的线程组。
     *
     * @param  target
     *         当此线程启动时调用其 {@code run} 方法的对象。如果为 {@code null}，则调用此线程的 {@code run} 方法。
     *
     * @param  name
     *         新线程的名称
     *
     * @throws  SecurityException
     *          如果当前线程无法在指定的线程组中创建线程或无法覆盖上下文类加载器方法。
     */
    public Thread(ThreadGroup group, Runnable target, String name) {
        init(group, target, name, 0);
    }

    /**
     * 分配一个新的 {@code Thread} 对象，使其具有 {@code target}
     * 作为其运行对象，具有指定的 {@code name} 作为其名称，并且属于由 {@code group} 引用的线程组，并具有指定的 <i>堆栈大小</i>。
     *
     * <p>此构造函数与 {@link
     * #Thread(ThreadGroup,Runnable,String)} 相同，除了它允许指定线程堆栈大小。堆栈大小是虚拟机为该线程的堆栈分配的地址空间的大约字节数。 <b>堆栈大小参数的效果在不同平台上可能有很大差异。</b>
     *
     * <p>在某些平台上，指定较高的 {@code stackSize} 值可能允许线程在抛出 {@link StackOverflowError} 之前达到更大的递归深度。
     * 同样，指定较低的值可能允许更多的线程并发运行而不会抛出 {@link
     * OutOfMemoryError}（或其他内部错误）。堆栈大小参数与最大递归深度和并发级别的关系是平台依赖的。 <b>在某些平台上，堆栈大小参数可能根本没有效果。</b>
     *
     * <p>虚拟机可以将 {@code stackSize} 参数视为建议。如果指定的值对于平台来说不合理地低，虚拟机可能会使用某些平台特定的最小值；如果指定的值不合理地高，虚拟机可能会使用某些平台特定的最大值。同样，虚拟机可以自由地将指定的值向上或向下四舍五入，或完全忽略它。
     *
     * <p>指定零作为 {@code stackSize} 参数将使此构造函数的行为与
     * {@code Thread(ThreadGroup, Runnable, String)} 构造函数完全相同。
     *
     * <p><i>由于此构造函数的行为具有平台依赖性，因此在使用时应格外小心。
     * 在给定计算所需的线程堆栈大小可能会因 JRE 实现而异。鉴于这种变化，可能需要仔细调整堆栈大小参数，并且可能需要为应用程序运行的每个 JRE 实现重新调整。</i>
     *
     * <p>实现说明：Java 平台实现者应记录其实现对 {@code stackSize} 参数的行为。
     *
     *
     * @param  group
     *         线程组。如果为 {@code null} 且存在安全经理，则组由 {@linkplain
     *         SecurityManager#getThreadGroup SecurityManager.getThreadGroup()} 确定。
     *         如果没有安全经理或 {@code
     *         SecurityManager.getThreadGroup()} 返回 {@code null}，则组设置为当前线程的线程组。
     *
     * @param  target
     *         当此线程启动时调用其 {@code run} 方法的对象。如果为 {@code null}，则调用此线程的 {@code run} 方法。
     *
     * @param  name
     *         新线程的名称
     *
     * @param  stackSize
     *         新线程的期望堆栈大小，或零表示忽略此参数。
     *
     * @throws  SecurityException
     *          如果当前线程无法在指定的线程组中创建线程
     *
     * @since 1.4
     */
    public Thread(ThreadGroup group, Runnable target, String name,
                  long stackSize) {
        init(group, target, name, stackSize);
    }

    /**
     * 使此线程开始执行；Java 虚拟机调用此线程的 <code>run</code> 方法。
     * <p>
     * 结果是两个线程并发运行：当前线程（从对
     * <code>start</code> 方法的调用返回）和另一个线程（执行其
     * <code>run</code> 方法）。
     * <p>
     * 重复启动一个线程是非法的。特别是，一旦线程完成执行，就不得重新启动。
     *
     * @exception  IllegalThreadStateException  如果线程已被启动。
     * @see        #run()
     * @see        #stop()
     */
    public synchronized void start() {
        /**
         * 此方法不会为 main 方法线程或由 VM 创建/设置的 "system"
         * 组线程调用。将来在此方法中添加的任何新功能可能也需要添加到 VM 中。
         *
         * 零状态值对应于 "NEW" 状态。
         */
        if (threadStatus != 0)
            throw new IllegalThreadStateException();

        /* 通知组此线程即将启动
         * 以便将其添加到组的线程列表中
         * 并减少组的未启动计数。 */
        group.add(this);

        boolean started = false;
        try {
            start0();
            started = true;
        } finally {
            try {
                if (!started) {
                    group.threadStartFailed(this);
                }
            } catch (Throwable ignore) {
                /* 什么都不做。如果 start0 抛出 Throwable，则
                  它将传递到调用堆栈中。 */
            }
        }
    }

    private native void start0();

    /**
     * 如果此线程是使用单独的
     * <code>Runnable</code> 运行对象构造的，则调用该
     * <code>Runnable</code> 对象的 <code>run</code> 方法；
     * 否则，此方法不执行任何操作并返回。
     * <p>
     * <code>Thread</code> 的子类应覆盖此方法。
     *
     * @see     #start()
     * @see     #stop()
     * @see     #Thread(ThreadGroup, Runnable, String)
     */
    @Override
    public void run() {
        if (target != null) {
            target.run();
        }
    }

    /**
     * 系统调用此方法，以便在线程实际退出之前给线程一个清理的机会。
     */
    private void exit() {
        if (threadLocals != null && TerminatingThreadLocal.REGISTRY.isPresent()) {
            TerminatingThreadLocal.threadTerminated();
        }
        if (group != null) {
            group.threadTerminated(this);
            group = null;
        }
        /* 积极地将所有引用字段设置为 null：参见 bug 4006245 */
        target = null;
        /* 加速释放某些资源 */
        threadLocals = null;
        inheritableThreadLocals = null;
        inheritedAccessControlContext = null;
        blocker = null;
        uncaughtExceptionHandler = null;
    }

    /**
     * 强制线程停止执行。
     * <p>
     * 如果安装了安全经理，其 <code>checkAccess</code>
     * 方法将使用 <code>this</code>
     * 作为其参数调用。这可能导致在当前线程中抛出
     * <code>SecurityException</code>。
     * <p>
     * 如果此线程与当前线程不同（即，当前线程试图停止其他线程），则
     * 安全经理的 <code>checkPermission</code> 方法（使用
     * <code>RuntimePermission("stopThread")</code> 参数）也会被调用。
     * 同样，这可能导致在当前线程中抛出
     * <code>SecurityException</code>。
     * <p>
     * 由此线程表示的线程被迫异常地停止其正在执行的任何操作，并抛出新创建的
     * <code>ThreadDeath</code> 对象作为异常。
     * <p>
     * 允许停止尚未启动的线程。如果线程最终启动，它将立即终止。
     * <p>
     * 应用程序通常不应尝试捕获
     * <code>ThreadDeath</code>，除非必须执行某些非常规
     * 清理操作（注意抛出
     * <code>ThreadDeath</code> 会导致 <code>finally</code> 子句在
     * <code>try</code> 语句中执行，然后线程正式死亡）。如果
     * <code>catch</code> 子句捕获了
     * <code>ThreadDeath</code> 对象，重要的是重新抛出该对象，以便线程实际死亡。
     * <p>
     * 顶层错误处理程序对未捕获的异常作出反应，如果未捕获的异常是
     * <code>ThreadDeath</code> 的实例，则不会打印消息或以其他方式通知应用程序。
     *
     * @exception  SecurityException  如果当前线程无法
     *               修改此线程。
     * @see        #interrupt()
     * @see        #checkAccess()
     * @see        #run()
     * @see        #start()
     * @see        ThreadDeath
     * @see        ThreadGroup#uncaughtException(Thread,Throwable)
     * @see        SecurityManager#checkAccess(Thread)
     * @see        SecurityManager#checkPermission
     * @deprecated 此方法本质上是不安全的。使用 Thread.stop 停止线程会导致它解锁所有已锁定的监视器（作为未检查的
     *       <code>ThreadDeath</code> 异常沿堆栈传播的自然结果）。如果任何先前由这些监视器保护的对象处于不一致状态，受损的对象将对其他线程可见，可能导致任意行为。许多
     *       使用 <code>stop</code> 的情况应被替换为仅修改某些变量以指示目标线程应停止运行的代码。目标线程应定期检查此变量，
     *       如果变量指示应停止运行，则以有序的方式从其 run 方法返回。如果目标线程长时间等待（例如，在条件变量上等待），则应使用
     *       <code>interrupt</code> 方法中断等待。
     *       有关更多信息，请参见
     *       <a href="{@docRoot}/../technotes/guides/concurrency/threadPrimitiveDeprecation.html">为什么
     *       Thread.stop、Thread.suspend 和 Thread.resume 被弃用？</a>。
     */
    @Deprecated
    public final void stop() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            checkAccess();
            if (this != Thread.currentThread()) {
                security.checkPermission(SecurityConstants.STOP_THREAD_PERMISSION);
            }
        }
        // 零状态值对应于 "NEW"，由于我们持有锁，它不能变为非 NEW。
        if (threadStatus != 0) {
            resume(); // 如果线程被挂起，则唤醒线程；否则为 no-op
        }


                    // The VM can handle all thread states
        stop0(new ThreadDeath());
    }

    /**
     * 抛出 {@code UnsupportedOperationException}。
     *
     * @param obj 被忽略
     *
     * @deprecated 此方法最初设计用于强制线程停止并抛出给定的 {@code Throwable} 作为异常。它本质上是不安全的（请参阅 {@link #stop()} 了解详细信息），并且还可能用于生成目标线程未准备处理的异常。
     *        有关更多信息，请参阅
     *        <a href="{@docRoot}/../technotes/guides/concurrency/threadPrimitiveDeprecation.html">为什么
     *        Thread.stop, Thread.suspend 和 Thread.resume 已被弃用？</a>。
     */
    @Deprecated
    public final synchronized void stop(Throwable obj) {
        throw new UnsupportedOperationException();
    }

    /**
     * 中断此线程。
     *
     * <p> 除非当前线程正在中断自身，这是始终允许的，否则将调用此线程的 {@link #checkAccess() checkAccess} 方法，可能会抛出 {@link
     * SecurityException}。
     *
     * <p> 如果此线程在 {@link
     * Object#wait() wait()}、{@link Object#wait(long) wait(long)} 或 {@link
     * Object#wait(long, int) wait(long, int)} 方法的调用中被阻塞，或者在 {@link #join()}、{@link #join(long)}、{@link
     * #join(long, int)}、{@link #sleep(long)} 或 {@link #sleep(long, int)} 方法的调用中被阻塞，那么它的中断状态将被清除，并将收到一个 {@link InterruptedException}。
     *
     * <p> 如果此线程在 {@link
     * java.nio.channels.InterruptibleChannel InterruptibleChannel} 上的 I/O 操作中被阻塞，则通道将被关闭，线程的中断状态将被设置，并且线程将收到一个 {@link
     * java.nio.channels.ClosedByInterruptException}。
     *
     * <p> 如果此线程在 {@link java.nio.channels.Selector} 中被阻塞，则线程的中断状态将被设置，并且它将立即从选择操作中返回，可能带有非零值，就像调用了选择器的 {@link
     * java.nio.channels.Selector#wakeup wakeup} 方法一样。
     *
     * <p> 如果上述条件都不满足，则此线程的中断状态将被设置。 </p>
     *
     * <p> 中断一个未存活的线程可能没有任何效果。
     *
     * @throws  SecurityException
     *          如果当前线程不能修改此线程
     *
     * @revised 6.0
     * @spec JSR-51
     */
    public void interrupt() {
        if (this != Thread.currentThread())
            checkAccess();

        synchronized (blockerLock) {
            Interruptible b = blocker;
            if (b != null) {
                interrupt0();           // 仅用于设置中断标志
                b.interrupt(this);
                return;
            }
        }
        interrupt0();
    }

    /**
     * 测试当前线程是否已被中断。此方法会清除线程的 <i>中断状态</i>。换句话说，如果此方法连续调用两次，第二次调用将返回 false（除非在第一次调用清除中断状态后，当前线程再次被中断，并且在第二次调用检查中断状态之前）。
     *
     * <p> 由于线程未存活时忽略的中断将反映为此方法返回 false。
     *
     * @return  <code>true</code> 如果当前线程已被中断；<code>false</code> 否则。
     * @see #isInterrupted()
     * @revised 6.0
     */
    public static boolean interrupted() {
        return currentThread().isInterrupted(true);
    }

    /**
     * 测试此线程是否已被中断。此方法不会影响线程的 <i>中断状态</i>。
     *
     * <p> 由于线程未存活时忽略的中断将反映为此方法返回 false。
     *
     * @return  <code>true</code> 如果此线程已被中断；<code>false</code> 否则。
     * @see     #interrupted()
     * @revised 6.0
     */
    public boolean isInterrupted() {
        return isInterrupted(false);
    }

    /**
     * 测试某个线程是否已被中断。中断状态根据传递的 ClearInterrupted 值重置或不重置。
     */
    private native boolean isInterrupted(boolean ClearInterrupted);

    /**
     * 抛出 {@link NoSuchMethodError}。
     *
     * @deprecated 此方法最初设计用于在没有任何清理的情况下销毁此线程。任何它持有的锁将保持锁定状态。然而，该方法从未实现。如果实现，它将容易导致死锁，类似于 {@link #suspend}。如果目标线程在被销毁时持有一个保护关键系统资源的锁，那么任何线程都无法再次访问此资源。如果另一个线程尝试在调用 <code>destroy</code> 之前锁定此资源，将导致死锁。此类死锁通常表现为“冻结”的进程。有关更多信息，请参阅
     *     <a href="{@docRoot}/../technotes/guides/concurrency/threadPrimitiveDeprecation.html">
     *     为什么 Thread.stop, Thread.suspend 和 Thread.resume 已被弃用？</a>。
     * @throws NoSuchMethodError 始终抛出
     */
    @Deprecated
    public void destroy() {
        throw new NoSuchMethodError();
    }

    /**
     * 测试此线程是否存活。线程在启动后且未死亡时被视为存活。
     *
     * @return  <code>true</code> 如果此线程存活；<code>false</code> 否则。
     */
    public final native boolean isAlive();

    /**
     * 暂停此线程。
     * <p>
     * 首先调用此线程的 <code>checkAccess</code> 方法，不带任何参数。这可能会导致抛出一个
     * <code>SecurityException </code>（在当前线程中）。
     * <p>
     * 如果线程存活，它将被暂停，并且除非恢复，否则不会进一步进展。
     *
     * @exception  SecurityException  如果当前线程不能修改此线程。
     * @see #checkAccess
     * @deprecated   此方法已被弃用，因为它本质上容易导致死锁。如果目标线程在被暂停时持有一个保护关键系统资源的锁，那么任何线程都无法再次访问此资源，直到目标线程恢复。如果尝试恢复目标线程的线程在调用 <code>resume</code> 之前尝试锁定此资源，将导致死锁。此类死锁通常表现为“冻结”的进程。
     *   有关更多信息，请参阅
     *   <a href="{@docRoot}/../technotes/guides/concurrency/threadPrimitiveDeprecation.html">为什么
     *   Thread.stop, Thread.suspend 和 Thread.resume 已被弃用？</a>。
     */
    @Deprecated
    public final void suspend() {
        checkAccess();
        suspend0();
    }

    /**
     * 恢复已暂停的线程。
     * <p>
     * 首先调用此线程的 <code>checkAccess</code> 方法，不带任何参数。这可能会导致抛出一个
     * <code>SecurityException</code>（在当前线程中）。
     * <p>
     * 如果线程存活但已暂停，它将被恢复，并允许继续执行。
     *
     * @exception  SecurityException  如果当前线程不能修改此线程。
     * @see        #checkAccess
     * @see        #suspend()
     * @deprecated 此方法仅用于与 {@link #suspend} 一起使用，而 {@link #suspend} 已被弃用，因为它容易导致死锁。
     *     有关更多信息，请参阅
     *     <a href="{@docRoot}/../technotes/guides/concurrency/threadPrimitiveDeprecation.html">为什么
     *     Thread.stop, Thread.suspend 和 Thread.resume 已被弃用？</a>。
     */
    @Deprecated
    public final void resume() {
        checkAccess();
        resume0();
    }

    /**
     * 更改此线程的优先级。
     * <p>
     * 首先调用此线程的 <code>checkAccess</code> 方法，不带任何参数。这可能会导致抛出一个
     * <code>SecurityException</code>。
     * <p>
     * 否则，此线程的优先级将被设置为指定的 <code>newPriority</code> 和线程组允许的最大优先级中的较小值。
     *
     * @param newPriority 要设置的优先级
     * @exception  IllegalArgumentException  如果优先级不在 <code>MIN_PRIORITY</code> 到
     *               <code>MAX_PRIORITY</code> 范围内。
     * @exception  SecurityException  如果当前线程不能修改此线程。
     * @see        #getPriority
     * @see        #checkAccess()
     * @see        #getThreadGroup()
     * @see        #MAX_PRIORITY
     * @see        #MIN_PRIORITY
     * @see        ThreadGroup#getMaxPriority()
     */
    public final void setPriority(int newPriority) {
        ThreadGroup g;
        checkAccess();
        if (newPriority > MAX_PRIORITY || newPriority < MIN_PRIORITY) {
            throw new IllegalArgumentException();
        }
        if((g = getThreadGroup()) != null) {
            if (newPriority > g.getMaxPriority()) {
                newPriority = g.getMaxPriority();
            }
            setPriority0(priority = newPriority);
        }
    }

    /**
     * 返回此线程的优先级。
     *
     * @return  此线程的优先级。
     * @see     #setPriority
     */
    public final int getPriority() {
        return priority;
    }

    /**
     * 将此线程的名称更改为与参数 <code>name</code> 相等。
     * <p>
     * 首先调用此线程的 <code>checkAccess</code> 方法，不带任何参数。这可能会导致抛出一个
     * <code>SecurityException</code>。
     *
     * @param      name   此线程的新名称。
     * @exception  SecurityException  如果当前线程不能修改此线程。
     * @see        #getName
     * @see        #checkAccess()
     */
    public final synchronized void setName(String name) {
        checkAccess();
        if (name == null) {
            throw new NullPointerException("name cannot be null");
        }

        this.name = name;
        if (threadStatus != 0) {
            setNativeName(name);
        }
    }

    /**
     * 返回此线程的名称。
     *
     * @return  此线程的名称。
     * @see     #setName(String)
     */
    public final String getName() {
        return name;
    }

    /**
     * 返回此线程所属的线程组。如果此线程已死亡（已停止），则返回 null。
     *
     * @return  此线程的线程组。
     */
    public final ThreadGroup getThreadGroup() {
        return group;
    }

    /**
     * 返回当前线程的 {@linkplain java.lang.ThreadGroup 线程组} 及其子组中的活动线程数的估计值。递归遍历当前线程的线程组及其所有子组。
     *
     * <p> 返回的值只是一个估计值，因为线程数可能会在方法遍历内部数据结构时动态变化，并且可能受到某些系统线程的影响。此方法主要用于调试和监控目的。
     *
     * @return  当前线程的线程组及其任何其他具有当前线程的线程组作为祖先的线程组中的活动线程数的估计值。
     */
    public static int activeCount() {
        return currentThread().getThreadGroup().activeCount();
    }

    /**
     * 将当前线程的线程组及其子组中的每个活动线程复制到指定的数组中。此方法只是调用当前线程的线程组的 {@link java.lang.ThreadGroup#enumerate(Thread[])}
     * 方法。
     *
     * <p> 应用程序可以使用 {@linkplain #activeCount activeCount} 方法来估计数组应该有多大，但是
     * <i>如果数组太短而无法容纳所有线程，多余的线程将被静默忽略。</i>  如果必须获取当前线程的线程组及其子组中的每个活动线程，调用者应验证返回的 int 值严格小于 {@code tarray} 的长度。
     *
     * <p> 由于此方法中的固有竞态条件，建议仅用于调试和监控目的。
     *
     * @param  tarray
     *         用于存放线程列表的数组
     *
     * @return  放入数组中的线程数
     *
     * @throws  SecurityException
     *          如果 {@link java.lang.ThreadGroup#checkAccess} 确定当前线程不能访问其线程组
     */
    public static int enumerate(Thread tarray[]) {
        return currentThread().getThreadGroup().enumerate(tarray);
    }

    /**
     * 计算此线程中的堆栈帧数。线程必须被暂停。
     *
     * @return     此线程中的堆栈帧数。
     * @exception  IllegalThreadStateException  如果此线程未被暂停。
     * @deprecated 此调用的定义依赖于 {@link #suspend}，而 {@link #suspend} 已被弃用。此外，此调用的结果从未被很好地定义。
     */
    @Deprecated
    public native int countStackFrames();

    /**
     * 最多等待 {@code millis} 毫秒，直到此线程死亡。等待时间为 {@code 0} 表示永远等待。
     *
     * <p> 此实现使用一个以 {@code this.wait} 调用为条件的循环，条件是 {@code this.isAlive}。当线程终止时，将调用 {@code this.notifyAll} 方法。建议应用程序不要在 {@code Thread} 实例上使用 {@code wait}、{@code notify} 或 {@code notifyAll}。
     *
     * @param  millis
     *         等待时间（毫秒）
     *
     * @throws  IllegalArgumentException
     *          如果 {@code millis} 的值为负数
     *
     * @throws  InterruptedException
     *          如果任何线程中断了当前线程。当前线程的 <i>中断状态</i> 在抛出此异常时被清除。
     */
    public final synchronized void join(long millis)
    throws InterruptedException {
        long base = System.currentTimeMillis();
        long now = 0;


                    if (millis < 0) {
            throw new IllegalArgumentException("超时值为负数");
        }

        if (millis == 0) {
            while (isAlive()) {
                wait(0);
            }
        } else {
            while (isAlive()) {
                long delay = millis - now;
                if (delay <= 0) {
                    break;
                }
                wait(delay);
                now = System.currentTimeMillis() - base;
            }
        }
    }

    /**
     * 等待最多 {@code millis} 毫秒加上
     * {@code nanos} 纳秒，直到该线程死亡。
     *
     * <p> 该实现使用一个以 {@code this.wait} 调用为条件的循环
     * 依赖于 {@code this.isAlive}。当线程终止时，会调用
     * {@code this.notifyAll} 方法。建议应用程序不要在
     * {@code Thread} 实例上使用 {@code wait}、{@code notify} 或
     * {@code notifyAll}。
     *
     * @param  millis
     *         等待时间，以毫秒为单位
     *
     * @param  nanos
     *         额外的纳秒等待时间，范围为 {@code 0-999999}
     *
     * @throws  IllegalArgumentException
     *          如果 {@code millis} 的值为负数，或者 {@code nanos} 的值不在
     *          范围 {@code 0-999999} 内
     *
     * @throws  InterruptedException
     *          如果任何线程中断了当前线程。当前线程的
     *          <i>中断状态</i>在抛出此异常时被清除。
     */
    public final synchronized void join(long millis, int nanos)
    throws InterruptedException {

        if (millis < 0) {
            throw new IllegalArgumentException("超时值为负数");
        }

        if (nanos < 0 || nanos > 999999) {
            throw new IllegalArgumentException(
                                "纳秒超时值超出范围");
        }

        if (nanos >= 500000 || (nanos != 0 && millis == 0)) {
            millis++;
        }

        join(millis);
    }

    /**
     * 等待该线程死亡。
     *
     * <p> 该方法的调用行为与以下调用完全相同：
     *
     * <blockquote>
     * {@linkplain #join(long) join}{@code (0)}
     * </blockquote>
     *
     * @throws  InterruptedException
     *          如果任何线程中断了当前线程。当前线程的
     *          <i>中断状态</i>在抛出此异常时被清除。
     */
    public final void join() throws InterruptedException {
        join(0);
    }

    /**
     * 将当前线程的堆栈跟踪打印到标准错误流。
     * 该方法仅用于调试。
     *
     * @see     Throwable#printStackTrace()
     */
    public static void dumpStack() {
        new Exception("堆栈跟踪").printStackTrace();
    }

    /**
     * 将此线程标记为 {@linkplain #isDaemon 守护线程}
     * 或用户线程。当唯一运行的线程都是守护线程时，
     * Java 虚拟机退出。
     *
     * <p> 必须在启动线程之前调用此方法。
     *
     * @param  on
     *         如果为 {@code true}，则将此线程标记为守护线程
     *
     * @throws  IllegalThreadStateException
     *          如果此线程已 {@linkplain #isAlive 运行}
     *
     * @throws  SecurityException
     *          如果 {@link #checkAccess} 确定当前线程
     *          无法修改此线程
     */
    public final void setDaemon(boolean on) {
        checkAccess();
        if (isAlive()) {
            throw new IllegalThreadStateException();
        }
        daemon = on;
    }

    /**
     * 测试此线程是否为守护线程。
     *
     * @return  <code>true</code> 如果此线程是守护线程；
     *          <code>false</code> 否则。
     * @see     #setDaemon(boolean)
     */
    public final boolean isDaemon() {
        return daemon;
    }

    /**
     * 确定当前运行的线程是否有权限
     * 修改此线程。
     * <p>
     * 如果存在安全经理，其 <code>checkAccess</code> 方法
     * 将以此线程作为参数调用。这可能导致抛出
     * <code>SecurityException</code>。
     *
     * @exception  SecurityException  如果当前线程不允许
     *               访问此线程。
     * @see        SecurityManager#checkAccess(Thread)
     */
    public final void checkAccess() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkAccess(this);
        }
    }

    /**
     * 返回此线程的字符串表示形式，包括
     * 线程的名称、优先级和线程组。
     *
     * @return  此线程的字符串表示形式。
     */
    public String toString() {
        ThreadGroup group = getThreadGroup();
        if (group != null) {
            return "Thread[" + getName() + "," + getPriority() + "," +
                           group.getName() + "]";
        } else {
            return "Thread[" + getName() + "," + getPriority() + "," +
                            "" + "]";
        }
    }

    /**
     * 返回此线程的上下文类加载器。上下文
     * 类加载器由线程的创建者提供，供在此线程中运行的代码
     * 加载类和资源时使用。
     * 如果未 {@linkplain #setContextClassLoader 设置}，则默认为
     * 父线程的类加载器上下文。初始线程的上下文类加载器通常设置为
     * 用于加载应用程序的类加载器。
     *
     * <p>如果存在安全经理，且调用者的类加载器不为
     * {@code null} 且不是上下文类加载器或其祖先，则此方法调用安全经理的
     * {@link SecurityManager#checkPermission(java.security.Permission) checkPermission}
     * 方法，使用 {@link RuntimePermission RuntimePermission}{@code
     * ("getClassLoader")} 权限来验证是否允许检索上下文类加载器。
     *
     * @return  此线程的上下文类加载器，或 {@code null}
     *          表示系统类加载器（或，如果不存在系统类加载器，则为引导类加载器）
     *
     * @throws  SecurityException
     *          如果当前线程无法获取上下文类加载器
     *
     * @since 1.2
     */
    @CallerSensitive
    public ClassLoader getContextClassLoader() {
        if (contextClassLoader == null)
            return null;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            ClassLoader.checkClassLoaderPermission(contextClassLoader,
                                                   Reflection.getCallerClass());
        }
        return contextClassLoader;
    }

    /**
     * 设置此线程的上下文类加载器。上下文
     * 类加载器可以在创建线程时设置，允许
     * 通过 {@code getContextClassLoader} 提供适当的类加载器，
     * 供在此线程中运行的代码加载类和资源时使用。
     *
     * <p>如果存在安全经理，其 {@link
     * SecurityManager#checkPermission(java.security.Permission) checkPermission}
     * 方法将使用 {@link RuntimePermission RuntimePermission}{@code
     * ("setContextClassLoader")} 权限来验证是否允许设置上下文类加载器。
     *
     * @param  cl
     *         此线程的上下文类加载器，或 null  表示
     *         系统类加载器（或，如果不存在系统类加载器，则为引导类加载器）
     *
     * @throws  SecurityException
     *          如果当前线程无法设置上下文类加载器
     *
     * @since 1.2
     */
    public void setContextClassLoader(ClassLoader cl) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("setContextClassLoader"));
        }
        contextClassLoader = cl;
    }

    /**
     * 如果且仅如果当前线程持有
     * 指定对象的监视器锁，则返回 <tt>true</tt>。
     *
     * <p>此方法设计用于使程序断言
     * 当前线程已经持有指定的锁：
     * <pre>
     *     assert Thread.holdsLock(obj);
     * </pre>
     *
     * @param  obj 要测试锁所有权的对象
     * @throws NullPointerException 如果 obj 为 <tt>null</tt>
     * @return <tt>true</tt> 如果当前线程持有指定对象的监视器锁。
     * @since 1.4
     */
    public static native boolean holdsLock(Object obj);

    private static final StackTraceElement[] EMPTY_STACK_TRACE
        = new StackTraceElement[0];

    /**
     * 返回表示此线程堆栈转储的堆栈跟踪元素数组。
     * 如果此线程尚未启动、已启动但尚未被系统调度运行，
     * 或已终止，则此方法将返回一个零长度数组。
     * 如果返回的数组长度非零，则数组的第一个元素表示堆栈的顶部，
     * 即最近的方法调用。数组的最后一个元素表示堆栈的底部，
     * 即最远的方法调用。
     *
     * <p>如果存在安全经理，且此线程不是
     * 当前线程，则安全经理的
     * <tt>checkPermission</tt> 方法将使用
     * <tt>RuntimePermission("getStackTrace")</tt> 权限
     * 来验证是否允许获取堆栈跟踪。
     *
     * <p>在某些情况下，某些虚拟机可能会省略一个
     * 或多个堆栈帧。在极端情况下，虚拟机允许
     * 没有关于此线程的堆栈跟踪信息的虚拟机从此
     * 方法返回一个零长度数组。
     *
     * @return 一个 <tt>StackTraceElement</tt> 数组，
     * 每个元素表示一个堆栈帧。
     *
     * @throws SecurityException
     *        如果存在安全经理且其
     *        <tt>checkPermission</tt> 方法不允许
     *        获取线程的堆栈跟踪。
     * @see SecurityManager#checkPermission
     * @see RuntimePermission
     * @see Throwable#getStackTrace
     *
     * @since 1.5
     */
    public StackTraceElement[] getStackTrace() {
        if (this != Thread.currentThread()) {
            // 检查获取堆栈跟踪的权限
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkPermission(
                    SecurityConstants.GET_STACK_TRACE_PERMISSION);
            }
            // 优化，避免在虚拟机中调用未启动或已终止的线程
            if (!isAlive()) {
                return EMPTY_STACK_TRACE;
            }
            StackTraceElement[][] stackTraceArray = dumpThreads(new Thread[] {this});
            StackTraceElement[] stackTrace = stackTraceArray[0];
            // 在之前的 isAlive 调用期间可能已终止的线程可能没有堆栈跟踪。
            if (stackTrace == null) {
                stackTrace = EMPTY_STACK_TRACE;
            }
            return stackTrace;
        } else {
            // 当前线程不需要虚拟机帮助
            return (new Exception()).getStackTrace();
        }
    }

    /**
     * 返回所有活动线程的堆栈跟踪映射。
     * 映射的键是线程，每个映射值是一个
     * <tt>StackTraceElement</tt> 数组，表示相应 <tt>Thread</tt> 的堆栈转储。
     * 返回的堆栈跟踪格式与
     * {@link #getStackTrace getStackTrace} 方法指定的格式相同。
     *
     * <p>在调用此方法时，线程可能正在执行。
     * 每个线程的堆栈跟踪仅代表一个快照，
     * 每个堆栈跟踪可能在不同时间获取。如果虚拟机没有
     * 关于线程的堆栈跟踪信息，则将在映射值中返回一个零长度数组。
     *
     * <p>如果存在安全经理，则安全经理的
     * <tt>checkPermission</tt> 方法将使用
     * <tt>RuntimePermission("getStackTrace")</tt> 权限以及
     * <tt>RuntimePermission("modifyThreadGroup")</tt> 权限
     * 来验证是否允许获取所有线程的堆栈跟踪。
     *
     * @return 一个从 <tt>Thread</tt> 到 <tt>StackTraceElement</tt> 数组的
     * 映射，表示相应线程的堆栈跟踪。
     *
     * @throws SecurityException
     *        如果存在安全经理且其
     *        <tt>checkPermission</tt> 方法不允许
     *        获取线程的堆栈跟踪。
     * @see #getStackTrace
     * @see SecurityManager#checkPermission
     * @see RuntimePermission
     * @see Throwable#getStackTrace
     *
     * @since 1.5
     */
    public static Map<Thread, StackTraceElement[]> getAllStackTraces() {
        // 检查获取堆栈跟踪的权限
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(
                SecurityConstants.GET_STACK_TRACE_PERMISSION);
            security.checkPermission(
                SecurityConstants.MODIFY_THREADGROUP_PERMISSION);
        }

        // 获取所有线程的快照
        Thread[] threads = getThreads();
        StackTraceElement[][] traces = dumpThreads(threads);
        Map<Thread, StackTraceElement[]> m = new HashMap<>(threads.length);
        for (int i = 0; i < threads.length; i++) {
            StackTraceElement[] stackTrace = traces[i];
            if (stackTrace != null) {
                m.put(threads[i], stackTrace);
            }
            // 否则已终止，不将其放入映射中
        }
        return m;
    }


    private static final RuntimePermission SUBCLASS_IMPLEMENTATION_PERMISSION =
                    new RuntimePermission("enableContextClassLoaderOverride");

    /** 子类安全审计结果缓存 */
    /* 未来版本中使用 ConcurrentReferenceHashMap 替换 */
    private static class Caches {
        /** 子类安全审计结果缓存 */
        static final ConcurrentMap<WeakClassKey,Boolean> subclassAudits =
            new ConcurrentHashMap<>();

        /** 已审计子类的 WeakReferences 队列 */
        static final ReferenceQueue<Class<?>> subclassAuditsQueue =
            new ReferenceQueue<>();
    }

    /**
     * 验证此（可能是子类）实例可以构造
     * 而不违反安全约束：子类不得覆盖安全敏感的非最终方法，
     * 否则将检查 "enableContextClassLoaderOverride" RuntimePermission。
     */
    private static boolean isCCLOverridden(Class<?> cl) {
        if (cl == Thread.class)
            return false;


                    processQueue(Caches.subclassAuditsQueue, Caches.subclassAudits);
        WeakClassKey key = new WeakClassKey(cl, Caches.subclassAuditsQueue);
        Boolean result = Caches.subclassAudits.get(key);
        if (result == null) {
            result = Boolean.valueOf(auditSubclass(cl));
            Caches.subclassAudits.putIfAbsent(key, result);
        }

        return result.booleanValue();
    }

    /**
     * 对给定的子类执行反射检查，以验证它是否覆盖了安全敏感的非最终方法。如果子类覆盖了任何方法，则返回 true，否则返回 false。
     */
    private static boolean auditSubclass(final Class<?> subcl) {
        Boolean result = AccessController.doPrivileged(
            new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    for (Class<?> cl = subcl;
                         cl != Thread.class;
                         cl = cl.getSuperclass())
                    {
                        try {
                            cl.getDeclaredMethod("getContextClassLoader", new Class<?>[0]);
                            return Boolean.TRUE;
                        } catch (NoSuchMethodException ex) {
                        }
                        try {
                            Class<?>[] params = {ClassLoader.class};
                            cl.getDeclaredMethod("setContextClassLoader", params);
                            return Boolean.TRUE;
                        } catch (NoSuchMethodException ex) {
                        }
                    }
                    return Boolean.FALSE;
                }
            }
        );
        return result.booleanValue();
    }

    private native static StackTraceElement[][] dumpThreads(Thread[] threads);
    private native static Thread[] getThreads();

    /**
     * 返回此线程的标识符。线程 ID 是一个正 <tt>long</tt> 数字，当此线程被创建时生成。
     * 线程 ID 是唯一的，并在其生命周期内保持不变。当线程终止时，此线程 ID 可能会被重用。
     *
     * @return 此线程的 ID。
     * @since 1.5
     */
    public long getId() {
        return tid;
    }

    /**
     * 线程状态。线程可以处于以下状态之一：
     * <ul>
     * <li>{@link #NEW}<br>
     *     尚未启动的线程处于此状态。
     *     </li>
     * <li>{@link #RUNNABLE}<br>
     *     在 Java 虚拟机中执行的线程处于此状态。
     *     </li>
     * <li>{@link #BLOCKED}<br>
     *     被阻塞等待监视器锁的线程处于此状态。
     *     </li>
     * <li>{@link #WAITING}<br>
     *     无限期等待其他线程执行特定操作的线程处于此状态。
     *     </li>
     * <li>{@link #TIMED_WAITING}<br>
     *     等待其他线程执行操作且指定了等待时间的线程处于此状态。
     *     </li>
     * <li>{@link #TERMINATED}<br>
     *     已退出的线程处于此状态。
     *     </li>
     * </ul>
     *
     * <p>
     * 线程在任何给定时间只能处于一种状态。这些状态是虚拟机状态，不反映任何操作系统的线程状态。
     *
     * @since   1.5
     * @see #getState
     */
    public enum State {
        /**
         * 尚未启动的线程的状态。
         */
        NEW,

        /**
         * 可运行线程的状态。处于可运行状态的线程在 Java 虚拟机中执行，但可能在等待操作系统的其他资源，如处理器。
         */
        RUNNABLE,

        /**
         * 被阻塞等待监视器锁的线程的状态。处于阻塞状态的线程正在等待进入同步块/方法或在调用
         * {@link Object#wait() Object.wait} 后重新进入同步块/方法。
         */
        BLOCKED,

        /**
         * 等待线程的状态。
         * 线程处于等待状态是因为调用了以下方法之一：
         * <ul>
         *   <li>{@link Object#wait() Object.wait} 且没有超时</li>
         *   <li>{@link #join() Thread.join} 且没有超时</li>
         *   <li>{@link LockSupport#park() LockSupport.park}</li>
         * </ul>
         *
         * <p>处于等待状态的线程正在等待其他线程执行特定操作。
         *
         * 例如，调用 <tt>Object.wait()</tt> 的线程正在等待其他线程调用
         * <tt>Object.notify()</tt> 或 <tt>Object.notifyAll()</tt>。调用 <tt>Thread.join()</tt>
         * 的线程正在等待指定的线程终止。
         */
        WAITING,

        /**
         * 指定了等待时间的等待线程的状态。
         * 线程处于定时等待状态是因为调用了以下方法之一并指定了正的等待时间：
         * <ul>
         *   <li>{@link #sleep Thread.sleep}</li>
         *   <li>{@link Object#wait(long) Object.wait} 且有超时</li>
         *   <li>{@link #join(long) Thread.join} 且有超时</li>
         *   <li>{@link LockSupport#parkNanos LockSupport.parkNanos}</li>
         *   <li>{@link LockSupport#parkUntil LockSupport.parkUntil}</li>
         * </ul>
         */
        TIMED_WAITING,

        /**
         * 已终止线程的状态。
         * 线程已完成执行。
         */
        TERMINATED;
    }

    /**
     * 返回此线程的状态。
     * 此方法用于监控系统状态，而不是用于同步控制。
     *
     * @return 此线程的状态。
     * @since 1.5
     */
    public State getState() {
        // 获取当前线程状态
        return sun.misc.VM.toThreadState(threadStatus);
    }

    // 在 JSR-166 中添加

    /**
     * 当线程因未捕获的异常而突然终止时调用的处理程序接口。
     * <p>当线程即将因未捕获的异常而终止时，Java 虚拟机会查询该线程的
     * <tt>UncaughtExceptionHandler</tt> 使用
     * {@link #getUncaughtExceptionHandler} 并调用处理程序的
     * <tt>uncaughtException</tt> 方法，传递线程和异常作为参数。
     * 如果线程没有显式设置 <tt>UncaughtExceptionHandler</tt>，则其
     * <tt>ThreadGroup</tt> 对象充当其 <tt>UncaughtExceptionHandler</tt>。如果
     * <tt>ThreadGroup</tt> 对象对处理异常没有特殊要求，它可以将调用转发给
     * {@linkplain #getDefaultUncaughtExceptionHandler 默认未捕获异常处理程序}。
     *
     * @see #setDefaultUncaughtExceptionHandler
     * @see #setUncaughtExceptionHandler
     * @see ThreadGroup#uncaughtException
     * @since 1.5
     */
    @FunctionalInterface
    public interface UncaughtExceptionHandler {
        /**
         * 当给定线程因给定的未捕获异常而终止时调用的方法。
         * <p>此方法抛出的任何异常将被 Java 虚拟机忽略。
         * @param t 线程
         * @param e 异常
         */
        void uncaughtException(Thread t, Throwable e);
    }

    // 除非显式设置，否则为 null
    private volatile UncaughtExceptionHandler uncaughtExceptionHandler;

    // 除非显式设置，否则为 null
    private static volatile UncaughtExceptionHandler defaultUncaughtExceptionHandler;

    /**
     * 设置当线程因未捕获的异常而突然终止且没有为该线程定义其他处理程序时调用的默认处理程序。
     *
     * <p>未捕获异常处理首先由线程控制，然后由线程的 {@link ThreadGroup} 对象控制，最后由默认
     * 未捕获异常处理程序控制。如果线程没有显式设置未捕获异常处理程序，并且线程的线程组
     * （包括父线程组）没有专门化其 <tt>uncaughtException</tt> 方法，则将调用默认处理程序的
     * <tt>uncaughtException</tt> 方法。
     * <p>通过设置默认未捕获异常处理程序，应用程序可以更改未捕获异常的处理方式（例如，记录到特定设备或文件）
     * 对于那些已经接受系统提供的任何“默认”行为的线程。
     *
     * <p>注意，默认未捕获异常处理程序通常不应委托给线程的 <tt>ThreadGroup</tt> 对象，因为这可能导致无限递归。
     *
     * @param eh 用作默认未捕获异常处理程序的对象。如果为 <tt>null</tt>，则没有默认处理程序。
     *
     * @throws SecurityException 如果存在安全管理器且其拒绝 <tt>{@link RuntimePermission}
     *         (&quot;setDefaultUncaughtExceptionHandler&quot;)</tt>
     *
     * @see #setUncaughtExceptionHandler
     * @see #getUncaughtExceptionHandler
     * @see ThreadGroup#uncaughtException
     * @since 1.5
     */
    public static void setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(
                new RuntimePermission("setDefaultUncaughtExceptionHandler")
                    );
        }

         defaultUncaughtExceptionHandler = eh;
     }

    /**
     * 返回当线程因未捕获的异常而突然终止时调用的默认处理程序。如果返回值为 <tt>null</tt>，则没有默认处理程序。
     * @since 1.5
     * @see #setDefaultUncaughtExceptionHandler
     * @return 所有线程的默认未捕获异常处理程序
     */
    public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler(){
        return defaultUncaughtExceptionHandler;
    }

    /**
     * 返回当此线程因未捕获的异常而突然终止时调用的处理程序。如果此线程没有显式设置未捕获异常处理程序，
     * 则返回此线程的 <tt>ThreadGroup</tt> 对象，除非此线程已终止，在这种情况下返回 <tt>null</tt>。
     * @since 1.5
     * @return 此线程的未捕获异常处理程序
     */
    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return uncaughtExceptionHandler != null ?
            uncaughtExceptionHandler : group;
    }

    /**
     * 设置当此线程因未捕获的异常而突然终止时调用的处理程序。
     * <p>线程可以通过显式设置未捕获异常处理程序来完全控制其对未捕获异常的响应。如果没有设置这样的处理程序，
     * 则线程的 <tt>ThreadGroup</tt> 对象充当其处理程序。
     * @param eh 用作此线程的未捕获异常处理程序的对象。如果为 <tt>null</tt>，则此线程没有显式处理程序。
     * @throws  SecurityException  如果当前线程不允许修改此线程。
     * @see #setDefaultUncaughtExceptionHandler
     * @see ThreadGroup#uncaughtException
     * @since 1.5
     */
    public void setUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
        checkAccess();
        uncaughtExceptionHandler = eh;
    }

    /**
     * 派发未捕获的异常到处理程序。此方法旨在仅由 JVM 调用。
     */
    private void dispatchUncaughtException(Throwable e) {
        getUncaughtExceptionHandler().uncaughtException(this, e);
    }

    /**
     * 从指定的映射中移除已入队的引用队列中的所有键。
     */
    static void processQueue(ReferenceQueue<Class<?>> queue,
                             ConcurrentMap<? extends
                             WeakReference<Class<?>>, ?> map)
    {
        Reference<? extends Class<?>> ref;
        while((ref = queue.poll()) != null) {
            map.remove(ref);
        }
    }

    /**
     * 用于 Class 对象的弱键。
     **/
    static class WeakClassKey extends WeakReference<Class<?>> {
        /**
         * 引用对象的身份哈希码，以在引用对象被清除后保持一致的哈希码。
         */
        private final int hash;

        /**
         * 创建一个新的 WeakClassKey，注册到队列。
         */
        WeakClassKey(Class<?> cl, ReferenceQueue<Class<?>> refQueue) {
            super(cl, refQueue);
            hash = System.identityHashCode(cl);
        }

        /**
         * 返回原始引用对象的身份哈希码。
         */
        @Override
        public int hashCode() {
            return hash;
        }

        /**
         * 如果给定对象是此相同的 WeakClassKey 实例，或者，如果此对象的引用对象未被清除，
         * 且给定对象是另一个具有与此对象相同的非空引用对象的 WeakClassKey 实例，则返回 true。
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;

            if (obj instanceof WeakClassKey) {
                Object referent = get();
                return (referent != null) &&
                       (referent == ((WeakClassKey) obj).get());
            } else {
                return false;
            }
        }
    }


    // 以下三个最初未初始化的字段由类 java.util.concurrent.ThreadLocalRandom 独家管理。
    // 这些字段用于构建并发代码中的高性能 PRNG，我们不能冒险出现意外的虚假共享。
    // 因此，这些字段使用 @Contended 隔离。

    /** ThreadLocalRandom 的当前种子 */
    @sun.misc.Contended("tlr")
    long threadLocalRandomSeed;

    /** 探测哈希值；如果 threadLocalRandomSeed 已初始化则为非零 */
    @sun.misc.Contended("tlr")
    int threadLocalRandomProbe;

    /** 与公共 ThreadLocalRandom 序列隔离的二级种子 */
    @sun.misc.Contended("tlr")
    int threadLocalRandomSecondarySeed;

    /* 一些私有辅助方法 */
    private native void setPriority0(int newPriority);
    private native void stop0(Object o);
    private native void suspend0();
    private native void resume0();
    private native void interrupt0();
    private native void setNativeName(String name);
}
