/*
 * 版权所有 (c) 1994, 2016, Oracle 和/或其关联公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款限制。
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
 * <i>线程</i>是程序中的一个执行线程。Java 虚拟机允许应用程序同时运行多个执行线程。
 * <p>
 * 每个线程都有一个优先级。优先级较高的线程优先于优先级较低的线程执行。每个线程
 * 还可以被标记为守护线程（daemon）。当某个线程中的代码创建一个新的 <code>Thread</code> 对象时，
 * 新线程的初始优先级设置为创建线程的优先级，并且仅当创建线程是守护线程时，新线程才是守护线程。
 * <p>
 * 当 Java 虚拟机启动时，通常有一个单一的非守护线程（通常调用某个指定类的
 * <code>main</code> 方法）。Java 虚拟机会继续执行线程，直到以下任一情况发生：
 * <ul>
 * <li>类 <code>Runtime</code> 的 <code>exit</code> 方法被调用，
 *     并且安全管理器允许执行退出操作。
 * <li>所有非守护线程都已终止，无论是通过从 <code>run</code> 方法返回，
 *     还是通过抛出超出 <code>run</code> 方法的异常。
 * </ul>
 * <p>
 * 有两种方式可以创建新的执行线程。一种是声明一个类作为 <code>Thread</code> 的子类。
 * 该子类应重写 <code>Thread</code> 类的 <code>run</code> 方法。然后可以分配并启动该子类的实例。
 * 例如，计算大于指定值的素数的线程可以编写如下：
 * <hr><blockquote><pre>
 *     class PrimeThread extends Thread {
 *         long minPrime;
 *         PrimeThread(long minPrime) {
 *             this.minPrime = minPrime;
 *         }
 *
 *         public void run() {
 *             // 计算大于 minPrime 的素数
 *              . . .
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
 * 另一种创建线程的方式是声明一个实现 <code>Runnable</code> 接口的类。
 * 该类需实现 <code>run</code> 方法。然后可以分配该类的实例，在创建
 * <code>Thread</code> 时作为参数传递，并启动。使用这种方式的相同示例如下：
 * <hr><blockquote><pre>
 *     class PrimeRun implements Runnable {
 *         long minPrime;
 *         PrimeRun(long minPrime) {
 *             this.minPrime = minPrime;
 *         }
 *
 *         public void run() {
 *             // 计算大于 minPrime 的素数
 *              . . .
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
 * 每个线程都有一个用于标识的名称。多个线程可以具有相同的名称。如果创建线程时未指定名称，
 * 将为它生成一个新名称。
 * <p>
 * 除非另有说明，将 <code>null</code> 参数传递给此类的构造方法或方法将导致抛出
 * <code>NullPointerException</code>。
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

    /* 是否对该线程进行单步调试。 */
    private boolean     single_step;

    /* 该线程是否为守护线程。 */
    private boolean     daemon = false;

    /* JVM 状态 */
    private boolean     stillborn = false;

    /* 将要运行的内容。 */
    private Runnable target;

    /* 该线程所属的线程组 */
    private ThreadGroup group;

    /* 该线程的上下文类加载器 */
    private ClassLoader contextClassLoader;

    /* 该线程继承的访问控制上下文 */
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
     * 该线程请求的堆栈大小，如果创建者未指定堆栈大小，则为 0。
     * 虚拟机可以根据需要处理此数字；某些虚拟机可能会忽略它。
     */
    private long stackSize;

    /*
     * 本地线程终止后保留的 JVM 私有状态。
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
     * 当前对 java.util.concurrent.locks.LockSupport.park 的调用的参数。
     * 由（私有）java.util.concurrent.locks.LockSupport.setBlocker 设置
     * 通过 java.util.concurrent.locks.LockSupport.getBlocker 访问
     */
    volatile Object parkBlocker;

    /* 该线程在可中断 I/O 操作中被阻塞的对象（如果有）。
     * 在设置该线程的中断状态后，应调用阻塞器的 interrupt 方法。
     */
    private volatile Interruptible blocker;
    private final Object blockerLock = new Object();

    /* 设置 blocker 字段；通过 sun.misc.SharedSecrets 从 java.nio 代码调用 */
    void blockedOn(Interruptible b) {
        synchronized (blockerLock) {
            blocker = b;
        }
    }

    /**
     * 线程可以具有的最小优先级。
     */
    public final static int MIN_PRIORITY = 1;

   /**
     * 分配给线程的默认优先级。
     */
    public final static int NORM_PRIORITY = 5;

    /**
     * 线程可以具有的最大优先级。
     */
    public final static int MAX_PRIORITY = 10;

    /**
     * 返回当前正在执行的线程对象的引用。
     *
     * @return  当前正在执行的线程。
     */
    public static native Thread currentThread();

    /**
     * 向调度器提示当前线程愿意让出当前对处理器的使用。调度器可以忽略此提示。
     *
     * <p> yield 是一种启发式尝试，用于改善线程之间的相对进度，
     * 否则可能会过度使用 CPU。应结合详细的性能分析和基准测试使用，
     * 以确保其确实具有预期效果。
     *
     * <p> 很少适合使用此方法。它可能在调试或测试中有用，
     * 可能有助于重现由于竞争条件导致的错误。在设计并发控制结构（如
     * {@link java.util.concurrent.locks} 包中的结构）时也可能有用。
     */
    public static native void yield();

    /**
     * 使当前执行的线程休眠（暂时停止执行）指定的毫秒数，
     * 受系统定时器和调度器的精度和准确性限制。线程不会失去任何监视器的所有权。
     *
     * @param  millis
     *         休眠的毫秒数
     *
     * @throws  IllegalArgumentException
     *          如果 <code>millis</code> 的值是负数
     *
     * @throws  InterruptedException
     *          如果某个线程中断了当前线程。当抛出此异常时，
     *          当前线程的<i>中断状态</i>将被清除。
     */
    public static native void sleep(long millis) throws InterruptedException;

    /**
     * 使当前执行的线程休眠（暂时停止执行）指定的毫秒数加上指定的纳秒数，
     * 受系统定时器和调度器的精度和准确性限制。线程不会失去任何监视器的所有权。
     *
     * @param  millis
     *         休眠的毫秒数
     *
     * @param  nanos
     *         <code>0-999999</code> 额外的休眠纳秒数
     *
     * @throws  IllegalArgumentException
     *          如果 <code>millis</code> 的值是负数，或 <code>nanos</code> 的值
     *          不在 <code>0-999999</code> 范围内
     *
     * @throws  InterruptedException
     *          如果某个线程中断了当前线程。当抛出此异常时，
     *          当前线程的<i>中断状态</i>将被清除。
     */
    public static void sleep(long millis, int nanos)
    throws InterruptedException {
        if (millis < 0) {
            throw new IllegalArgumentException("超时值是负数");
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
     * @param target 调用其 run() 方法的对象
     * @param name 新线程的名称
     * @param stackSize 新线程所需的堆栈大小，0 表示忽略此参数。
     * @param acc 继承的 AccessControlContext，如果为 null 则为 AccessController.getContext()
     * @param inheritThreadLocals 如果为 <code>true</code>，从构造线程继承可继承线程局部变量的初始值
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
            /* 判断是否为小程序 */

            /* 如果存在安全管理器，询问安全管理器如何处理。 */
            if (security != null) {
                g = security.getThreadGroup();
            }

            /* 如果安全管理器没有强烈意见，则使用父线程的线程组。 */
            if (g == null) {
                g = parent.getThreadGroup();
            }
        }

        /* 无论是否明确传递线程组，都检查访问权限。 */
        g.checkAccess();

        /*
         * 是否具有所需的权限？
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
        /* 存储指定的堆栈大小，以防虚拟机需要 */
        this.stackSize = stackSize;

        /* 设置线程 ID */
        tid = nextThreadID();
    }

    /**
     * 抛出 CloneNotSupportedException，因为线程无法被有意义地克隆。
     * 请构造一个新线程。
     *
     * @throws  CloneNotSupportedException
     *          总是抛出
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * 分配一个新的 <code>Thread</code> 对象。此构造方法与
     * {@link #Thread(ThreadGroup,Runnable,String) Thread}
     * <code>(null, null, gname)</code> 的效果相同，其中 <code>gname</code> 是新生成的名称。
     * 自动生成的名称格式为 <code>"Thread-"</code><i>n</i>，其中 <i>n</i> 是整数。
     */
    public Thread() {
        init(null, null, "Thread-" + nextThreadNum(), 0);
    }

    /**
     * 分配一个新的 <code>Thread</code> 对象。此构造方法与
     * {@link #Thread(ThreadGroup,Runnable,String) Thread}
     * <code>(null, target, gname)</code> 的效果相同，其中 <code>gname</code> 是新生成的名称。
     * 自动生成的名称格式为 <code>"Thread-"</code><i>n</i>，其中 <i>n</i> 是整数。
     *
     * @param  target
     *         当此线程启动时调用其 <code>run</code> 方法的对象。如果为 <code>null</code>，
     *         此类的 <code>run</code> 方法不执行任何操作。
     */
    public Thread(Runnable target) {
        init(null, target, "Thread-" + nextThreadNum(), 0);
    }

    /**
     * 创建一个继承指定 AccessControlContext 的新线程。
     * 这不是公共构造方法。
     */
    Thread(Runnable target, AccessControlContext acc) {
        init(null, target, "Thread-" + nextThreadNum(), 0, acc, false);
    }

    /**
     * 分配一个新的 <code>Thread</code> 对象。此构造方法与
     * {@link #Thread(ThreadGroup,Runnable,String) Thread}
     * <code>(group, target, gname)</code> 的效果相同，其中 <code>gname</code> 是新生成的名称。
     * 自动生成的名称格式为 <code>"Thread-"</code><i>n</i>，其中 <i>n</i> 是整数。
     *
     * @param  group
     *         线程组。如果为 <code>null</code> 且存在安全管理器，
     *         线程组由 {@link SecurityManager#getThreadGroup SecurityManager.getThreadGroup()} 确定。
     *         如果没有安全管理器或 <code>SecurityManager.getThreadGroup()</code> 返回 <code>null</code>，
     *         线程组设置为当前线程的线程组。
     *
     * @param  target
     *         当此线程启动时调用其 <code>run</code> 方法的对象。如果为 <code>null</code>，
     *         调用此线程的 run 方法。
     *
     * @throws  SecurityException
     *          如果当前线程无法在指定的线程组中创建线程
     */
    public Thread(ThreadGroup group, Runnable target) {
        init(group, target, "Thread-" + nextThreadNum(), 0);
    }

    /**
     * 分配一个新的 <code>Thread</code> 对象。此构造方法与
     * {@link #Thread(ThreadGroup,Runnable,String) Thread}
     * <code>(null, null, name)</code> 的效果相同。
     *
     * @param   name
     *          新线程的名称
     */
    public Thread(String name) {
        init(null, null, name, 0);
    }

    /**
     * 分配一个新的 <code>Thread</code> 对象。此构造方法与
     * {@link #Thread(ThreadGroup,Runnable,String) Thread}
     * <code>(group, null, name)</code> 的效果相同。
     *
     * @param  group
     *         线程组。如果为 <code>null</code> 且存在安全管理器，
     *         线程组由 {@link SecurityManager#getThreadGroup SecurityManager.getThreadGroup()} 确定。
     *         如果没有安全管理器或 <code>SecurityManager.getThreadGroup()</code> 返回 <code>null</code>，
     *         线程组设置为当前线程的线程组。
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
     * 分配一个新的 <code>Thread</code> 对象。此构造方法与
     * {@link #Thread(ThreadGroup,Runnable,String) Thread}
     * <code>(null, target, name)</code> 的效果相同。
     *
     * @param  target
     *         当此线程启动时调用其 <code>run</code> 方法的对象。如果为 <code>null</code>，
     *         调用此线程的 run 方法。
     *
     * @param  name
     *         新线程的名称
     */
    public Thread(Runnable target, String name) {
        init(null, target, name, 0);
    }

    /**
     * 分配一个新的 <code>Thread</code> 对象，使其以 <code>target</code> 作为运行对象，
     * 以指定的 <code>name</code> 作为名称，并属于 <code>group</code> 引用的线程组。
     *
     * <p>如果存在安全管理器，会以线程组作为参数调用其
     * {@link SecurityManager#checkAccess(ThreadGroup) checkAccess} 方法。
     *
     * <p>此外，当直接或间接由覆盖 <code>getContextClassLoader</code>
     * 或 <code>setContextClassLoader</code> 方法的子类构造方法调用时，
     * 会以 <code>RuntimePermission("enableContextClassLoaderOverride")</code> 权限
     * 调用其 <code>checkPermission</code> 方法。
     *
     * <p>新创建的线程的优先级设置为创建它的线程的优先级，即当前运行的线程。
     * 可以使用 {@link #setPriority setPriority} 方法将优先级更改为新值。
     *
     * <p>新创建的线程最初被标记为守护线程，仅当创建它的线程当前被标记为守护线程时。
     * 可以使用 {@link #setDaemon setDaemon} 方法更改线程是否为守护线程。
     *
     * @param  group
     *         线程组。如果为 <code>null</code> 且存在安全管理器，
     *         线程组由 {@link SecurityManager#getThreadGroup SecurityManager.getThreadGroup()} 确定。
     *         如果没有安全管理器或 <code>SecurityManager.getThreadGroup()</code> 返回 <code>null</code>，
     *         线程组设置为当前线程的线程组。
     *
     * @param  target
     *         当此线程启动时调用其 <code>run</code> 方法的对象。如果为 <code>null</code>，
     *         调用此线程的 run 方法。
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
     * 分配一个新的 <code>Thread</code> 对象，使其以 <code>target</code> 作为运行对象，
     * 以指定的 <code>name</code> 作为名称，属于 <code>group</code> 引用的线程组，
     * 并具有指定的<i>堆栈大小</i>。
     *
     * <p>此构造方法与 {@link #Thread(ThreadGroup,Runnable,String)} 相同，
     * 唯一的区别是它允许指定线程堆栈大小。堆栈大小是虚拟机为此线程的堆栈
     * 分配的地址空间的大约字节数。<b><code>stackSize</code> 参数的效果（如果有）高度依赖于平台。</b>
     *
     * <p>在某些平台上，为 <code>stackSize</code> 参数指定较高的值
     * 可能允许线程在抛出 {@link StackOverflowError} 之前实现更大的递归深度。
     * 同样，指定较低的值可能允许更多的线程同时存在，而不抛出
     * {@link OutOfMemoryError}（或其他内部错误）。<code>stackSize</code> 参数值
     * 与最大递归深度和并发级别的关系细节因平台而异。<b>在某些平台上，
     * <code>stackSize</code> 参数的值可能完全没有效果。</b>
     *
     * <p>虚拟机可以自由地将 <code>stackSize</code> 参数视为建议。
     * 如果指定的值对于平台来说过低，虚拟机可能使用某个平台特定的最小值；
     * 如果指定的值过高，虚拟机可能使用某个平台特定的最大值。
     * 同样，虚拟机可以根据需要向上或向下四舍五入指定的值（或完全忽略它）。
     *
     * <p>为 <code>stackSize</code> 参数指定零值将使此构造方法的行为
     * 与 <code>Thread(ThreadGroup, Runnable, String)</code> 构造方法完全相同。
     *
     * <p><i>由于此构造方法的行为因平台而异，因此使用时应格外小心。
     * 执行特定计算所需的线程堆栈大小可能会因 JRE 实现而异。
     * 鉴于这种差异，可能需要仔细调整堆栈大小参数，
     * 并且可能需要为应用程序运行的每个 JRE 实现重复调整。</i>
     *
     * <p>实现说明：鼓励 Java 平台实现者记录其实现关于
     * <code>stackSize</code> 参数的行为。
     *
     * @param  group
     *         线程组。如果为 <code>null</code> 且存在安全管理器，
     *         线程组由 {@link SecurityManager#getThreadGroup SecurityManager.getThreadGroup()} 确定。
     *         如果没有安全管理器或 <code>SecurityManager.getThreadGroup()</code> 返回 <code>null</code>，
     *         线程组设置为当前线程的线程组。
     *
     * @param  target
     *         当此线程启动时调用其 <code>run</code> 方法的对象。如果为 <code>null</code>，
     *         调用此线程的 run 方法。
     *
     * @param  name
     *         新线程的名称
     *
     * @param  stackSize
     *         新线程所需的堆栈大小，0 表示忽略此参数。
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
     * 使此线程开始执行；Java 虚拟机会调用此线程的 <code>run</code> 方法。
     * <p>
     * 结果是两个线程并发运行：当前线程（从调用 <code>start</code> 方法返回）
     * 和另一个线程（执行其 <code>run</code> 方法）。
     * <p>
     * 多次启动一个线程是非法的。特别是，线程在完成执行后不得重新启动。
     *
     * @exception  IllegalThreadStateException  如果线程已经被启动。
     * @see        #run()
     * @see        #stop()
     */
    public synchronized void start() {
        /**
         * 此方法不会为虚拟机创建/设置的主方法线程或“系统”组线程调用。
         * 未来添加到此方法的任何新功能可能也需要在虚拟机中添加。
         *
         * 状态值为零对应于“NEW”状态。
         */
        if (threadStatus != 0)
            throw new IllegalThreadStateException();

        /* 通知线程组此线程即将启动，以便将其添加到线程组的线程列表中，
         * 并减少线程组的未启动计数。 */
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
                /* 不执行任何操作。如果 start0 抛出 Throwable，
                   它将沿调用栈向上传播 */
            }
        }
    }

    private native void start0();

    /**
     * 如果此线程使用单独的 <code>Runnable</code> 运行对象构造，
     * 则调用该 <code>Runnable</code> 对象的 <code>run</code> 方法；
     * 否则，此方法不执行任何操作并返回。
     * <p>
     * <code>Thread</code> 的子类应重写此方法。
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
     * 系统调用此方法以便在线程实际退出之前有机会进行清理。
     */
    private void exit() {
        if (threadLocals != null && TerminatingThreadLocal.REGISTRY.isPresent()) {
            TerminatingThreadLocal.threadTerminated();
        }
        if (group != null) {
            group.threadTerminated(this);
            group = null;
        }
        /* 积极地将所有引用字段置空：参见错误 4006245 */
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
     * 如果安装了安全管理器，会以 <code>this</code> 作为参数调用其 <code>checkAccess</code> 方法。
     * 这可能导致在当前线程中抛出 <code>SecurityException</code>。
     * <p>
     * 如果此线程与当前线程不同（即当前线程试图停止除自身外的其他线程），
     * 还会调用安全管理器的 <code>checkPermission</code> 方法（使用
     * <code>RuntimePermission("stopThread")</code> 参数）。
     * 这也可能导致在当前线程中抛出 <code>SecurityException</code>。
     * <p>
     * 由该线程表示的线程被强制异常停止其正在执行的操作，并抛出新创建的
     * <code>ThreadDeath</code> 对象作为异常。
     * <p>
     * 允许停止尚未启动的线程。如果线程最终启动，它会立即终止。
     * <p>
     * 应用程序通常不应尝试捕获 <code>ThreadDeath</code>，除非必须执行一些特殊的清理操作
     * （注意，抛出 <code>ThreadDeath</code> 会导致 <code>try</code> 语句的
     * <code>finally</code> 子句在线程正式死亡之前执行）。
     * 如果 <code>catch</code> 子句捕获了 <code>ThreadDeath</code> 对象，
     * 重要的是要重新抛出该对象，以便线程实际终止。
     * <p>
     * 顶级错误处理程序对未捕获的异常做出反应时，如果未捕获的异常是
     * <code>ThreadDeath</code> 的实例，则不会打印消息或以其他方式通知应用程序。
     *
     * @exception  SecurityException  如果当前线程无法修改此线程。
     * @see        #interrupt()
     * @see        #checkAccess()
     * @see        #run()
     * @see        #start()
     * @see        ThreadDeath
     * @see        ThreadGroup#uncaughtException(Thread,Throwable)
     * @see        SecurityManager#checkAccess(Thread)
     * @see        SecurityManager#checkPermission
     * @deprecated 此方法本质上不安全。使用 Thread.stop 停止线程会导致它解锁所有已锁定的监视器
     *       （这是未检查的 <code>ThreadDeath</code> 异常沿栈向上传播的自然结果）。
     *       如果这些监视器保护的对象之前处于不一致状态，受损的对象将对其他线程可见，
     *       可能导致任意行为。许多 <code>stop</code> 的用法应替换为仅修改某个变量的代码，
     *       以指示目标线程应停止运行。目标线程应定期检查此变量，
     *       如果变量指示应停止运行，则以有序方式从其 run 方法返回。
     *       如果目标线程长时间等待（例如在条件变量上），应使用 <code>interrupt</code> 方法
     *       中断等待。更多信息，请参见
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
        // 状态值为零对应于“NEW”，因为我们持有锁，它无法更改为非 NEW。
        if (threadStatus != 0) {
            resume(); // 如果线程被挂起则唤醒它；否则无操作
        }

        // 虚拟机可以处理所有线程状态
        stop0(new ThreadDeath());
    }

    /**
     * 抛出 <code>UnsupportedOperationException</code>。
     *
     * @param obj 忽略
     *
     * @deprecated 此方法最初设计为强制线程停止并抛出给定的 <code>Throwable</code> 作为异常。
     *       它本质上不安全（详情参见 {@link #stop()}），
     *       而且可能被用来生成目标线程未准备处理的异常。
     *       更多信息，请参见
     *       <a href="{@docRoot}/../technotes/guides/concurrency/threadPrimitiveDeprecation.html">为什么
     *       Thread.stop、Thread.suspend 和 Thread.resume 被弃用？</a>。
     */
    @Deprecated
    public final synchronized void stop(Throwable obj) {
        throw new UnsupportedOperationException();
    }

    /**
     * 中断此线程。
     *
     * <p> 除非当前线程正在中断自身（这总是允许的），否则会调用此线程的
     * {@link #checkAccess() checkAccess} 方法，这可能导致抛出
     * {@link SecurityException}。
     *
     * <p> 如果此线程在调用 {@link Object} 类的
     * {@link Object#wait() wait()}、{@link Object#wait(long) wait(long)} 或
     * {@link Object#wait(long, int) wait(long, int)} 方法，
     * 或此类的 {@link #join()}、{@link #join(long)}、{@link #join(long, int)}、
     * {@link #sleep(long)} 或 {@link #sleep(long, int)} 方法时被阻塞，
     * 则其中断状态将被清除，并将收到一个 {@link InterruptedException}。
     *
     * <p> 如果此线程在 {@link java.nio.channels.InterruptibleChannel InterruptibleChannel}
     * 上的 I/O 操作中被阻塞，则通道将被关闭，线程的中断状态将被设置，
     * 并且线程将收到一个 {@link java.nio.channels.ClosedByInterruptException}。
     *
     * <p> 如果此线程在 {@link java.nio.channels.Selector} 中被阻塞，
     * 则线程的中断状态将被设置，并将立即从选择操作返回，
     * 可能带有非零值，就像调用了选择器的 {@link java.nio.channels.Selector#wakeup wakeup} 方法一样。
     *
     * <p> 如果上述条件均不成立，则此线程的中断状态将被设置。
     *
     * <p> 中断不活动的线程无需产生任何效果。
     *
     * @throws  SecurityException
     *          如果当前线程无法修改此线程
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
                interrupt0();           // 仅设置中断标志
                b.interrupt(this);
                return;
            }
        }
        interrupt0();
    }

    /**
     * 测试当前线程是否已被中断。此方法会清除线程的<i>中断状态</i>。
     * 换句话说，如果连续两次调用此方法，第二次调用将返回 false
     * （除非当前线程在第一次调用清除其中断状态后且第二次调用检查之前再次被中断）。
     *
     * <p>由于线程在中断时不活动而被忽略的中断将通过此方法返回 false。
     *
     * @return  <code>true</code> 如果当前线程已被中断；
     *          否则返回 <code>false</code>。
     * @see #isInterrupted()
     * @revised 6.0
     */
    public static boolean interrupted() {
        return currentThread().isInterrupted(true);
    }

    /**
     * 测试此线程是否已被中断。此方法不会影响线程的<i>中断状态</i>。
     *
     * <p>由于线程在中断时不活动而被忽略的中断将通过此方法返回 false。
     *
     * @return  <code>true</code> 如果此线程已被中断；
     *          否则返回 <code>false</code>。
     * @see     #interrupted()
     * @revised 6.0
     */
    public boolean isInterrupted() {
        return isInterrupted(false);
    }

    /**
     * 测试某个线程是否已被中断。根据传递的 ClearInterrupted 值决定是否重置中断状态。
     */
    private native boolean isInterrupted(boolean ClearInterrupted);

    /**
     * 抛出 {@link NoSuchMethodError}。
     *
     * @deprecated 此方法最初设计为销毁此线程而不进行任何清理。
     *       它持有的任何监视器将保持锁定。然而，该方法从未实现。
     *       如果实现，它将类似于 {@link #suspend} 那样容易导致死锁。
     *       如果目标线程在被销毁时持有保护关键系统资源的锁，
     *       任何线程都无法再次访问此资源。如果另一个线程尝试锁定此资源，
     *       将导致死锁。此类死锁通常表现为“冻结”进程。更多信息，请参见
     *       <a href="{@docRoot}/../technotes/guides/concurrency/threadPrimitiveDeprecation.html">
     *       为什么 Thread.stop、Thread.suspend 和 Thread.resume 被弃用？</a>。
     * @throws NoSuchMethodError 总是抛出
     */
    @Deprecated
    public void destroy() {
        throw new NoSuchMethodError();
    }

    /**
     * 测试此线程是否存活。如果线程已启动且尚未终止，则线程存活。
     *
     * @return  <code>true</code> 如果此线程存活；
     *          否则返回 <code>false</code>。
     */
    public final native boolean isAlive();

    /**
     * 挂起此线程。
     * <p>
     * 首先，无参数调用此线程的 <code>checkAccess</code> 方法。
     * 这可能导致在当前线程中抛出 <code>SecurityException</code>。
     * <p>
     * 如果线程存活，它将被挂起，除非被恢复，否则不会进一步进展。
     *
     * @exception  SecurityException  如果当前线程无法修改此线程。
     * @see #checkAccess
     * @deprecated   此方法已被弃用，因为它本质上容易导致死锁。
     *       如果目标线程在挂起时持有保护关键系统资源的监视器锁，
     *       在目标线程恢复之前，任何线程都无法访问此资源。
     *       如果尝试恢复目标线程的线程在调用 <code>resume</code> 之前尝试锁定此监视器，
     *       将导致死锁。此类死锁通常表现为“冻结”进程。
     *       更多信息，请参见
     *       <a href="{@docRoot}/../technotes/guides/concurrency/threadPrimitiveDeprecation.html">为什么
     *       Thread.stop、Thread.suspend 和 Thread.resume 被弃用？</a>。
     */
    @Deprecated
    public final void suspend() {
        checkAccess();
        suspend0();
    }

    /**
     * 恢复被挂起的线程。
     * <p>
     * 首先，无参数调用此线程的 <code>checkAccess</code> 方法。
     * 这可能导致在当前线程中抛出 <code>SecurityException</code>。
     * <p>
     * 如果线程存活但被挂起，它将被恢复，并允许继续执行。
     *
     * @exception  SecurityException  如果当前线程无法修改此线程。
     * @see        #checkAccess
     * @see        #suspend()
     * @deprecated 此方法仅为与 {@link #suspend} 一起使用而存在，
     *       因其容易导致死锁而被弃用。更多信息，请参见
     *       <a href="{@docRoot}/../technotes/guides/concurrency/threadPrimitiveDeprecation.html">为什么
     *       Thread.stop、Thread.suspend 和 Thread.resume 被弃用？</a>。
     */
    @Deprecated
    public final void resume() {
        checkAccess();
        resume0();
    }

    /**
     * 更改此线程的优先级。
     * <p>
     * 首先，无参数调用此线程的 <code>checkAccess</code> 方法。
     * 这可能导致抛出 <code>SecurityException</code>。
     * <p>
     * 否则，此线程的优先级设置为指定的 <code>newPriority</code>
     * 与线程所属线程组允许的最大优先级之间的较小值。
     *
     * @param newPriority 要设置的线程优先级
     * @exception  IllegalArgumentException  如果优先级不在
     *               <code>MIN_PRIORITY</code> 到 <code>MAX_PRIORITY</code> 范围内。
     * @exception  SecurityException  如果当前线程无法修改此线程。
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
     * 将此线程的名称更改为等于参数 <code>name</code>。
     * <p>
     * 首先，无参数调用此线程的 <code>checkAccess</code> 方法。
     * 这可能导致抛出 <code>SecurityException</code>。
     *
     * @param      name   此线程的新名称。
     * @exception  SecurityException  如果当前线程无法修改此线程。
     * @see        #getName
     * @see        #checkAccess()
     */
    public final synchronized void setName(String name) {
        checkAccess();
        if (name == null) {
            throw new NullPointerException("名称不能为空");
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
     * 返回此线程所属的线程组。如果此线程已终止（被停止），此方法返回 null。
     *
     * @return  此线程的线程组。
     */
    public final ThreadGroup getThreadGroup() {
        return group;
    }

    /**
     * 返回当前线程的 {@link java.lang.ThreadGroup 线程组} 及其子组中活动线程的估计数量。
     * 递归遍历当前线程线程组中的所有子组。
     *
     * <p> 返回的值只是一个估计值，因为在方法遍历内部数据结构时线程数量可能动态变化，
     * 并且可能受到某些系统线程的影响。此方法主要用于调试和监控目的。
     *
     * @return  当前线程线程组及其任何以当前线程线程组为祖先的线程组中活动线程的估计数量
     */
    public static int activeCount() {
        return currentThread().getThreadGroup().activeCount();
    }

    /**
     * 将当前线程的线程组及其子组中的每个活动线程复制到指定数组中。
     * 此方法仅调用当前线程线程组的 {@link java.lang.ThreadGroup#enumerate(Thread[])} 方法。
     *
     * <p> 应用程序可以使用 {@link #activeCount activeCount} 方法
     * 估算数组应有多大，但<i>如果数组太短，无法容纳所有线程，额外的线程将被静默忽略。</i>
     * 如果获取当前线程线程组及其子组中的每个活动线程至关重要，
     * 调用者应验证返回的整数值严格小于 <code>tarray</code> 的长度。
     *
     * <p> 由于此方法固有的竞争条件，建议仅将其用于调试和监控目的。
     *
     * @param  tarray
     *         用于存放线程列表的数组
     *
     * @return  放入数组的线程数量
     *
     * @throws  SecurityException
     *          如果 {@link java.lang.ThreadGroup#checkAccess} 确定当前线程无法访问其线程组
     */
    public static int enumerate(Thread tarray[]) {
        return currentThread().getThreadGroup().enumerate(tarray);
    }

    /**
     * 计算此线程中的堆栈框架数量。线程必须被挂起。
     *
     * @return     此线程中的堆栈框架数量。
     * @exception  IllegalThreadStateException  如果此线程未被挂起。
     * @deprecated 此调用的定义依赖于 {@link #suspend}，后者已被弃用。
     *             此外，此调用的结果从未明确定义。
     */
    @Deprecated
    public native int countStackFrames();

    /**
     * 最多等待 <code>millis</code> 毫秒，直到此线程终止。超时值为 <code>0</code> 表示永远等待。
     *
     * <p> 此实现使用基于 <code>this.isAlive</code> 条件的 <code>this.wait</code> 调用循环。
     * 当线程终止时，会调用 <code>this.notifyAll</code> 方法。
     * 建议应用程序不要在 <code>Thread</code> 实例上使用
     * <code>wait</code>、<code>notify</code> 或 <code>notifyAll</code>。
     *
     * @param  millis
     *         等待的毫秒数
     *
     * @throws  IllegalArgumentException
     *          如果 <code>millis</code> 的值是负数
     *
     * @throws  InterruptedException
     *          如果某个线程中断了当前线程。当抛出此异常时，
     *          当前线程的<i>中断状态</i>将被清除。
     */
    public final synchronized void join(long millis)
    throws InterruptedException {
        long base = System.currentTimeMillis();
        long now = 0;

        if (millis < 0) {
            throw new IllegalArgumentException("超时值是负数");
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
     * 最多等待 <code>millis</code> 毫秒加 <code>nanos</code> 纳秒，直到此线程终止。
     *
     * <p> 此实现使用基于 <code>this.isAlive</code> 条件的 <code>this.wait</code> 调用循环。
     * 当线程终止时，会调用 <code>this.notifyAll</code> 方法。
     * 建议应用程序不要在 <code>Thread</code> 实例上使用
     * <code>wait</code>、<code>notify</code> 或 <code>notifyAll</code>。
     *
     * @param  millis
     *         等待的毫秒数
     *
     * @param  nanos
     *         <code>0-999999</code> 额外的等待纳秒数
     *
     * @throws  IllegalArgumentException
     *          如果 <code>millis</code> 的值是负数，或 <code>nanos</code> 的值
     *          不在 <code>0-999999</code> 范围内
     *
     * @throws  InterruptedException
     *          如果某个线程中断了当前线程。当抛出此异常时，
     *          当前线程的<i>中断状态</i>将被清除。
     */
    public final synchronized void join(long millis, int nanos)
    throws InterruptedException {

        if (millis < 0) {
            throw new IllegalArgumentException("超时值是负数");
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
     * 等待此线程终止。
     *
     * <p> 此方法的调用行为与调用以下方法完全相同：
     *
     * <blockquote>
     * {@link #join(long) join}<code>(0)</code>
     * </blockquote>
     *
     * @throws  InterruptedException
     *          如果某个线程中断了当前线程。当抛出此异常时，
     *          当前线程的<i>中断状态</i>将被清除。
     */
    public final void join() throws InterruptedException {
        join(0);
    }

    /**
     * 将当前线程的堆栈跟踪打印到标准错误流。此方法仅用于调试。
     *
     * @see     Throwable#printStackTrace()
     */
    public static void dumpStack() {
        new Exception("堆栈跟踪").printStackTrace();
    }

    /**
     * 将此线程标记为 {@link #isDaemon 守护} 线程或用户线程。
     * 当仅剩的运行线程均为守护线程时，Java 虚拟机会退出。
     *
     * <p> 必须在线程启动之前调用此方法。
     *
     * @param  on
     *         如果为 <code>true</code>，将此线程标记为守护线程
     *
     * @throws  IllegalThreadStateException
     *          如果此线程 {@link #isAlive 存活}
     *
     * @throws  SecurityException
     *          如果 {@link #checkAccess} 确定当前线程无法修改此线程
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
     *          否则返回 <code>false</code>。
     * @see     #setDaemon(boolean)
     */
    public final boolean isDaemon() {
        return daemon;
    }

    /**
     * 确定当前运行的线程是否有权限修改此线程。
     * <p>
     * 如果存在安全管理器，会以当前线程作为参数调用其 <code>checkAccess</code> 方法。
     * 这可能导致抛出 <code>SecurityException</code>。
     *
     * @exception  SecurityException  如果当前线程不允许访问此线程。
     * @see        SecurityManager#checkAccess(Thread)
     */
    public final void checkAccess() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkAccess(this);
        }
    }

    /**
     * 返回此线程的字符串表示，包括线程的名称、优先级和线程组。
     *
     * @return  此线程的字符串表示。
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
     * 返回此线程的上下文类加载器。上下文类加载器由线程的创建者提供，
     * 供线程中的代码在加载类和资源时使用。如果未通过 {@link #setContextClassLoader set} 设置，
     * 默认值为父线程的类加载器上下文。初始线程的上下文类加载器通常设置为加载应用程序的类加载器。
     *
     * <p>如果存在安全管理器，且调用者的类加载器不为 <code>null</code> 且不是上下文类加载器
     * 或其祖先，则此方法会以 {@link RuntimePermission RuntimePermission}<code>("getClassLoader")</code>
     * 权限调用安全管理器的 {@link SecurityManager#checkPermission(java.security.Permission) checkPermission} 方法，
     * 以验证是否允许检索上下文类加载器。
     *
     * @return  此线程的上下文类加载器，或 <code>null</code> 表示系统类加载器
     *          （如果失败，则为引导类加载器）
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
     * 设置此线程的上下文类加载器。可以在创建线程时设置上下文类加载器，
     * 以便线程的创建者通过 {@code getContextClassLoader} 为线程中运行的代码
     * 提供适当的类加载器，用于加载类和资源。
     *
     * <p>如果存在安全管理器，会以 {@link RuntimePermission RuntimePermission}<code>("setContextClassLoader")</code>
     * 权限调用其 {@link SecurityManager#checkPermission(java.security.Permission) checkPermission} 方法，
     * 以检查是否允许设置上下文类加载器。
     *
     * @param  cl
     *         此线程的上下文类加载器，或 null 表示系统类加载器
     *         （如果失败，则为引导类加载器）
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
     * 仅当当前线程持有指定对象的监视器锁时返回 <code>true</code>。
     *
     * <p>此方法设计用于让程序断言当前线程已持有指定的锁：
     * <pre>
     *     assert Thread.holdsLock(obj);
     * </pre>
     *
     * @param  obj 要测试锁所有权的对象
     * @throws NullPointerException 如果 obj 为 <code>null</code>
     * @return <code>true</code> 如果当前线程持有指定对象的监视器锁。
     * @since 1.4
     */
    public static native boolean holdsLock(Object obj);

    private static final StackTraceElement[] EMPTY_STACK_TRACE
        = new StackTraceElement[0];

    /**
     * 返回表示此线程堆栈转储的堆栈跟踪元素数组。如果此线程尚未启动、
     * 已启动但尚未被系统调度运行，或已终止，此方法将返回一个零长度数组。
     * 如果返回的数组长度非零，则数组的第一个元素表示堆栈顶部，
     * 即序列中最近的方法调用。最后一个元素表示堆栈底部，即序列中最久远的方法调用。
     *
     * <p>如果存在安全管理器，且此线程不是当前线程，
     * 则以 <code>RuntimePermission("getStackTrace")</code> 权限
     * 调用安全管理器的 <code>checkPermission</code> 方法，以检查是否允许获取堆栈跟踪。
     *
     * <p>某些虚拟机在某些情况下可能从堆栈跟踪中省略一个或多个堆栈框架。
     * 在极端情况下，虚拟机如果没有关于此线程的堆栈跟踪信息，
     * 允许从此方法返回一个零长度数组。
     *
     * @return 一个 <code>StackTraceElement</code> 数组，每个元素表示一个堆栈框架。
     *
     * @throws SecurityException
     *        如果存在安全管理器，且其 <code>checkPermission</code> 方法
     *        不允许获取线程的堆栈跟踪。
     * @see SecurityManager#checkPermission
     * @see RuntimePermission
     * @see Throwable#getStackTrace
     *
     * @since 1.5
     */
    public StackTraceElement[] getStackTrace() {
        if (this != Thread.currentThread()) {
            // 检查 getStackTrace 权限
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkPermission(
                    SecurityConstants.GET_STACK_TRACE_PERMISSION);
            }
            // 优化以避免对尚未启动或已终止的线程调用虚拟机
            if (!isAlive()) {
                return EMPTY_STACK_TRACE;
            }
            StackTraceElement[][] stackTraceArray = dumpThreads(new Thread[] {this});
            StackTraceElement[] stackTrace = stackTraceArray[0];
            // 在之前的 isAlive 调用期间存活的线程可能已经终止，因此没有堆栈跟踪。
            if (stackTrace == null) {
                stackTrace = EMPTY_STACK_TRACE;
            }
            return stackTrace;
        } else {
            // 当前线程无需虚拟机帮助
            return (new Exception()).getStackTrace();
        }
    }

    /**
     * 返回所有存活线程的堆栈跟踪映射。映射的键是线程，
     * 每个映射值是一个表示相应 <code>Thread</code> 堆栈转储的
     * <code>StackTraceElement</code> 数组。
     * 返回的堆栈跟踪格式与 {@link #getStackTrace getStackTrace} 方法指定的格式相同。
     *
     * <p>在调用此方法时，线程可能正在执行。每个线程的堆栈跟踪仅代表一个快照，
     * 且每个堆栈跟踪可能在不同时间获取。如果虚拟机没有关于线程的堆栈跟踪信息，
     * 映射值将返回一个零长度数组。
     *
     * <p>如果存在安全管理器，则以 <code>RuntimePermission("getStackTrace")</code>
     * 以及 <code>RuntimePermission("modifyThreadGroup")</code> 权限
     * 调用安全管理器的 <code>checkPermission</code> 方法，
     * 以检查是否允许获取所有线程的堆栈跟踪。
     *
     * @return 一个从 <code>Thread</code> 到表示相应线程堆栈跟踪的
     * <code>StackTraceElement</code> 数组的 <code>Map</code>。
     *
     * @throws SecurityException
     *        如果存在安全管理器，且其 <code>checkPermission</code> 方法
     *        不允许获取线程的堆栈跟踪。
     * @see #getStackTrace
     * @see SecurityManager#checkPermission
     * @see RuntimePermission
     * @see Throwable#getStackTrace
     *
     * @since 1.5
     */
    public static Map<Thread, StackTraceElement[]> getAllStackTraces() {
        // 检查 getStackTrace 权限
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(
                SecurityConstants.GET_STACK_TRACE_PERMISSION);
            security.checkPermission(
                SecurityConstants.MODIFY_THREADGROUP_PERMISSION);
        }

        // 获取所有线程列表的快照
        Thread[] threads = getThreads();
        StackTraceElement[][] traces = dumpThreads(threads);
        Map<Thread, StackTraceElement[]> m = new HashMap<>(threads.length);
        for (int i = 0; i < threads.length; i++) {
            StackTraceElement[] stackTrace = traces[i];
            if (stackTrace != null) {
                m.put(threads[i], stackTrace);
            }
            // 否则已终止，因此不放入映射
        }
        return m;
    }

    private static final RuntimePermission SUBCLASS_IMPLEMENTATION_PERMISSION =
                    new RuntimePermission("enableContextClassLoaderOverride");

    /** 子类安全审计结果的缓存 */
    /* 当未来版本中出现 ConcurrentReferenceHashMap 时替换 */
    private static class Caches {
        /** 子类安全审计结果的缓存 */
        static final ConcurrentMap<WeakClassKey,Boolean> subclassAudits =
            new ConcurrentHashMap<>();

        /** 审计子类的弱引用队列 */
        static final ReferenceQueue<Class<?>> subclassAuditsQueue =
            new ReferenceQueue<>();
    }

    /**
     * 验证此（可能是子类）实例的构造是否违反安全约束：
     * 子类不得覆盖安全敏感的非 final 方法，否则会检查
     * "enableContextClassLoaderOverride" RuntimePermission。
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
     * 对给定的子类执行反射检查，以验证其是否覆盖了安全敏感的非 final 方法。
     * 如果子类覆盖了任何方法，返回 true，否则返回 false。
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
     * 返回此线程的标识符。线程 ID 是在创建此线程时生成的正 <code>long</code> 数。
     * 线程 ID 是唯一的，并且在其生命周期内保持不变。
     * 当线程终止时，此线程 ID 可能被重用。
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
     *     等待监视器锁的阻塞线程处于此状态。
     *     </li>
     * <li>{@link #WAITING}<br>
     *     无限期等待另一个线程执行特定操作的线程处于此状态。
     *     </li>
     * <li>{@link #TIMED_WAITING}<br>
     *     等待另一个线程在指定等待时间内执行操作的线程处于此状态。
     *     </li>
     * <li>{@link #TERMINATED}<br>
     *     已退出的线程处于此状态。
     *     </li>
     * </ul>
     *
     * <p>
     * 线程在任意时刻只能处于一种状态。
     * 这些状态是虚拟机状态，不反映任何操作系统线程状态。
     *
     * @since   1.5
     * @see #getState
     */
    public enum State {
        /**
         * 尚未启动的线程状态。
         */
        NEW,

        /**
         * 可运行线程状态。处于可运行状态的线程在 Java 虚拟机中执行，
         * 但可能在等待操作系统提供的其他资源，如处理器。
         */
        RUNNABLE,

        /**
         * 等待监视器锁的阻塞线程状态。
         * 处于阻塞状态的线程在等待监视器锁以进入同步块/方法，
         * 或在调用 {@link Object#wait() Object.wait} 后重新进入同步块/方法。
         */
        BLOCKED,

        /**
         * 等待线程状态。
         * 线程因调用以下方法之一而处于等待状态：
         * <ul>
         *   <li>{@link Object#wait() Object.wait} 无超时</li>
         *   <li>{@link #join() Thread.join} 无超时</li>
         *   <li>{@link LockSupport#park() LockSupport.park}</li>
         * </ul>
         *
         * <p>处于等待状态的线程在等待另一个线程执行特定操作。
         *
         * 例如，调用 <code>Object.wait()</code> 的线程
         * 在等待另一个线程在该对象上调用 <code>Object.notify()</code> 或
         * <code>Object.notifyAll()</code>。
         * 调用 <code>Thread.join()</code> 的线程在等待指定线程终止。
         */
        WAITING,

        /**
         * 具有指定等待时间的等待线程状态。
         * 线程因调用以下具有指定正等待时间的方法之一而处于定时等待状态：
         * <ul>
         *   <li>{@link #sleep Thread.sleep}</li>
         *   <li>{@link Object#wait(long) Object.wait} 带超时</li>
         *   <li>{@link #join(long) Thread.join} 带超时</li>
         *   <li>{@link LockSupport#parkNanos LockSupport.parkNanos}</li>
         *   <li>{@link LockSupport#parkUntil LockSupport}</li>
         * </ul>
         */
        TIMED_WAITING,

        /**
         * 已终止的线程状态。线程已完成执行。
         */
        TERMINATED;
    }

    /**
     * 返回此线程的状态。
     * 此方法设计用于监控系统状态，而不是用于同步控制。
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
     * 当 <code>Thread</code> 因未捕获的异常突然终止时调用的处理程序接口。
     * <p>当线程因未捕获的异常即将终止时，Java 虚拟机会通过
     * {@link #getUncaughtExceptionHandler} 查询线程的
     * <code>UncaughtExceptionHandler</code>，并调用处理程序的
     * <code>uncaughtException</code> 方法，将线程和异常作为参数传递。
     * 如果线程未明确设置 <code>UncaughtExceptionHandler</code>，
     * 则其 <code>ThreadGroup</code> 对象充当其 <code>UncaughtExceptionHandler</code>。
     * 如果 <code>ThreadGroup</code> 对象对处理异常没有特殊要求，
     * 它可以将调用转发到 {@link #getDefaultUncaughtExceptionHandler 默认未捕获异常处理程序}。
     *
     * @see #setDefaultUncaughtExceptionHandler
     * @see #setUncaughtExceptionHandler
     * @see ThreadGroup#uncaughtException
     * @since 1.5
     */
    @FunctionalInterface
    public interface UncaughtExceptionHandler {
        /**
         * 当给定线程因给定的未捕获异常终止时调用的方法。
         * <p>此方法抛出的任何异常将被 Java 虚拟机忽略。
         * @param t 线程
         * @param e 异常
         */
        void uncaughtException(Thread t, Throwable e);
    }

    // 除非明确设置，否则为 null
    private volatile UncaughtExceptionHandler uncaughtExceptionHandler;

    // 除非明确设置，否则为 null
    private static volatile UncaughtExceptionHandler defaultUncaughtExceptionHandler;

    /**
     * 设置当线程因未捕获的异常突然终止且没有为该线程定义其他处理程序时调用的默认处理程序。
     *
     * <p>未捕获异常的处理首先由线程控制，然后由线程的 {@link ThreadGroup} 对象控制，
     * 最后由默认未捕获异常处理程序控制。如果线程未明确设置未捕获异常处理程序，
     * 且线程的线程组（包括父线程组）未专门化其 <code>uncaughtException</code> 方法，
     * 则将调用默认处理程序的 <code>uncaughtException</code> 方法。
     * <p>通过设置默认未捕获异常处理程序，应用程序可以更改未捕获异常的处理方式
     * （例如记录到特定设备或文件），适用于那些原本接受系统提供的“默认”行为的线程。
     *
     * <p>请注意，默认未捕获异常处理程序通常不应推迟到线程的 <code>ThreadGroup</code> 对象，
     * 因为这可能导致无限递归。
     *
     * @param eh 用作默认未捕获异常处理程序的对象。
     * 如果为 <code>null</code>，则没有默认处理程序。
     *
     * @throws SecurityException 如果存在安全管理器且其拒绝
     *         <code>{@link RuntimePermission} ("setDefaultUncaughtExceptionHandler")</code>
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
     * 返回当线程因未捕获的异常突然终止时调用的默认处理程序。
     * 如果返回值是 <code>null</code>，则没有默认处理程序。
     * @since 1.5
     * @see #setDefaultUncaughtExceptionHandler
     * @return 所有线程的默认未捕获异常处理程序
     */
    public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler(){
        return defaultUncaughtExceptionHandler;
    }

    /**
     * 返回当此线程因未捕获的异常突然终止时调用的处理程序。
     * 如果此线程未明确设置未捕获异常处理程序，则返回其 <code>ThreadGroup</code> 对象，
     * 除非此线程已终止，在这种情况下返回 <code>null</code>。
     * @since 1.5
     * @return 此线程的未捕获异常处理程序
     */
    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return uncaughtExceptionHandler != null ?
            uncaughtExceptionHandler : group;
    }

    /**
     * 设置当此线程因未捕获的异常突然终止时调用的处理程序。
     * <p>线程可以通过明确设置其未捕获异常处理程序来完全控制如何响应未捕获的异常。
     * 如果未设置此类处理程序，则线程的 <code>ThreadGroup</code> 对象充当其处理程序。
     * @param eh 用作此线程未捕获异常处理程序的对象。如果为 <code>null</code>，
     *           则此线程没有明确的处理程序。
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
     * 将未捕获的异常分派到处理程序。此方法仅由 JVM 调用。
     */
    private void dispatchUncaughtException(Throwable e) {
        getUncaughtExceptionHandler().uncaughtException(this, e);
    }

    /**
     * 从指定映射中移除已在指定引用队列上入队的任何键。
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
     * 类对象的弱键。
     */
    static class WeakClassKey extends WeakReference<Class<?>> {
        /**
         * 保存引用的身份哈希码值，以在引用被清除后保持一致的哈希码
         */
        private final int hash;

        /**
         * 为给定对象创建新的 WeakClassKey，并注册到队列中。
         */
        WeakClassKey(Class<?> cl, ReferenceQueue<Class<?>> refQueue) {
            super(cl, refQueue);
            hash = System.identityHashCode(cl);
        }

        /**
         * 返回原始引用的身份哈希码。
         */
        @Override
        public int hashCode() {
            return hash;
        }

        /**
         * 如果给定对象是此 WeakClassKey 实例，则返回 true；
         * 或者，如果此对象的引用尚未被清除，且给定对象是另一个具有相同非空引用的 WeakClassKey 实例，
         * 也返回 true。
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

    // 以下三个初始未初始化的字段仅由 java.util.concurrent.ThreadLocalRandom 类管理。
    // 这些字段用于构建并发代码中的高性能伪随机数生成器，
    // 我们不能冒险发生意外的虚假共享。因此，这些字段使用 @Contended 隔离。

    /** ThreadLocalRandom 的当前种子 */
    @sun.misc.Contended("tlr")
    long threadLocalRandomSeed;

    /** 探测哈希值；如果 threadLocalRandomSeed 已初始化，则非零 */
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