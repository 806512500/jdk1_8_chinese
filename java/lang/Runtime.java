/*
 * 版权所有 (c) 1995, 2020，Oracle 和/或其附属公司。保留所有权利。
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

import java.io.*;
import java.util.StringTokenizer;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;

/**
 * 每个 Java 应用程序都有一个类 <code>Runtime</code> 的单个实例，允许应用程序与应用程序运行的环境进行交互。当前的运行时可以通过 <code>getRuntime</code> 方法获取。
 * <p>
 * 应用程序不能创建此类的自己的实例。
 *
 * @author 未署名
 * @see     java.lang.Runtime#getRuntime()
 * @since   JDK1.0
 */

public class Runtime {
    private static Runtime currentRuntime = new Runtime();

    /**
     * 返回与当前 Java 应用程序关联的运行时对象。类 <code>Runtime</code> 的大多数方法都是实例方法，必须针对当前运行时对象进行调用。
     *
     * @return 与当前 Java 应用程序关联的 <code>Runtime</code> 对象。
     */
    public static Runtime getRuntime() {
        return currentRuntime;
    }

    /** 不允许其他人实例化此类 */
    private Runtime() {}

    /**
     * 通过启动其关闭序列来终止当前运行的 Java 虚拟机。此方法永远不会正常返回。参数作为状态码；按照惯例，非零状态码表示异常终止。
     *
     * <p> 虚拟机的关闭序列包括两个阶段。在第一阶段，如果注册了任何 {@link #addShutdownHook 关闭挂钩}，则以某种未指定的顺序启动它们，并允许它们并发运行直到完成。在第二阶段，如果启用了 {@link #runFinalizersOnExit 退出时的终结}，则运行所有未调用的终结器。完成这些后，虚拟机 {@link #halt 停止}。
     *
     * <p> 如果在虚拟机已经开始其关闭序列后调用此方法，则如果正在运行关闭挂钩，此方法将无限期阻塞。如果关闭挂钩已经运行并且启用了退出时的终结，则如果状态码非零，此方法将以给定的状态码停止虚拟机；否则，它将无限期阻塞。
     *
     * <p> <tt>{@link System#exit(int) System.exit}</tt> 方法是调用此方法的传统和方便的手段。 <p>
     *
     * @param  status
     *         终止状态。按照惯例，非零状态码表示异常终止。
     *
     * @throws SecurityException
     *         如果存在安全管理者，并且其 <tt>{@link
     *         SecurityManager#checkExit checkExit}</tt> 方法不允许以指定的状态退出
     *
     * @see java.lang.SecurityException
     * @see java.lang.SecurityManager#checkExit(int)
     * @see #addShutdownHook
     * @see #removeShutdownHook
     * @see #runFinalizersOnExit
     * @see #halt(int)
     */
    public void exit(int status) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkExit(status);
        }
        Shutdown.exit(status);
    }

    /**
     * 注册一个新的虚拟机关闭挂钩。
     *
     * <p> Java 虚拟机在响应两种事件时 <i>关闭</i>：
     *
     *   <ul>
     *
     *   <li> 程序 <i>正常退出</i>，当最后一个非守护线程退出或调用 <tt>{@link #exit exit}</tt>（等效于 {@link System#exit(int) System.exit}）方法时，或者
     *
     *   <li> 虚拟机因用户中断（如键入 <tt>^C</tt>）或系统范围的事件（如用户注销或系统关闭）而 <i>终止</i>。
     *
     *   </ul>
     *
     * <p> <i>关闭挂钩</i> 只是一个已初始化但未启动的线程。当虚拟机开始其关闭序列时，它将以某种未指定的顺序启动所有注册的关闭挂钩，并让它们并发运行。当所有挂钩完成后，如果启用了退出时的终结，它将运行所有未调用的终结器。最后，虚拟机将停止。注意，守护线程将在关闭序列期间继续运行，如果关闭是由调用 <tt>{@link #exit exit}</tt> 方法发起的，非守护线程也将继续运行。
     *
     * <p> 一旦关闭序列开始，只有通过调用 <tt>{@link #halt halt}</tt> 方法才能停止它，该方法强制终止虚拟机。
     *
     * <p> 一旦关闭序列开始，就不可能注册新的关闭挂钩或取消注册先前注册的挂钩。尝试这些操作将导致 <tt>{@link IllegalStateException}</tt> 被抛出。
     *
     * <p> 关闭挂钩在虚拟机生命周期的敏感时刻运行，因此应谨慎编码。它们特别是应编写为线程安全的，并尽可能避免死锁。它们也不应盲目依赖可能已注册自己的关闭挂钩的服务，因此可能正在关闭过程中。例如，尝试使用其他基于线程的服务（如 AWT 事件分发线程）可能导致死锁。
     *
     * <p> 关闭挂钩也应快速完成其工作。当程序调用 <tt>{@link #exit exit}</tt> 时，期望虚拟机迅速关闭并退出。当虚拟机因用户注销或系统关闭而终止时，底层操作系统可能只允许固定的时间来关闭和退出。因此，不建议在关闭挂钩中尝试用户交互或执行长时间运行的计算。
     *
     * <p> 未捕获的异常在关闭挂钩中处理方式与任何其他线程相同，通过调用线程的 <tt>{@link ThreadGroup#uncaughtException uncaughtException}</tt> 方法。该方法的默认实现将异常的堆栈跟踪打印到 <tt>{@link System#err}</tt> 并终止线程；它不会导致虚拟机退出或停止。
     *
     * <p> 在极少数情况下，虚拟机可能会 <i>中止</i>，即在没有干净关闭的情况下停止运行。这发生在虚拟机外部终止时，例如在 Unix 上使用 <tt>SIGKILL</tt> 信号或在 Microsoft Windows 上使用 <tt>TerminateProcess</tt> 调用。如果本机方法出现异常，例如通过破坏内部数据结构或尝试访问不存在的内存，虚拟机也可能中止。如果虚拟机中止，则不能保证任何关闭挂钩是否会被运行。 <p>
     *
     * @param   hook
     *          一个已初始化但未启动的 <tt>{@link Thread}</tt> 对象
     *
     * @throws  IllegalArgumentException
     *          如果指定的挂钩已注册，或者可以确定挂钩已经在运行或已经运行过
     *
     * @throws  IllegalStateException
     *          如果虚拟机已经在关闭过程中
     *
     * @throws  SecurityException
     *          如果存在安全管理者，并且它拒绝 <tt>{@link RuntimePermission}("shutdownHooks")</tt>
     *
     * @see #removeShutdownHook
     * @see #halt(int)
     * @see #exit(int)
     * @since 1.3
     */
    public void addShutdownHook(Thread hook) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("shutdownHooks"));
        }
        ApplicationShutdownHooks.add(hook);
    }

    /**
     * 注销先前注册的虚拟机关闭挂钩。 <p>
     *
     * @param hook 要移除的挂钩
     * @return 如果指定的挂钩之前已注册并且成功注销，则返回 <tt>true</tt>，否则返回 <tt>false</tt>。
     *
     * @throws  IllegalStateException
     *          如果虚拟机已经在关闭过程中
     *
     * @throws  SecurityException
     *          如果存在安全经理并且它拒绝
     *          <tt>{@link RuntimePermission}("shutdownHooks")</tt>
     *
     * @see #addShutdownHook
     * @see #exit(int)
     * @since 1.3
     */
    public boolean removeShutdownHook(Thread hook) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("shutdownHooks"));
        }
        return ApplicationShutdownHooks.remove(hook);
    }

    /**
     * 强制终止当前运行的Java虚拟机。此方法永远不会正常返回。
     *
     * <p> 使用此方法时应极其谨慎。与
     * <tt>{@link #exit exit}</tt> 方法不同，此方法不会启动关闭挂钩，也不会在启用退出时运行未调用的终结器。如果关闭序列已经启动，则此方法不会等待任何正在运行的关闭挂钩或终结器完成其工作。 <p>
     *
     * @param  status
     *         终止状态。按照惯例，非零状态代码表示异常终止。如果 <tt>{@link Runtime#exit
     *         exit}</tt>（等效于 <tt>{@link System#exit(int)
     *         System.exit}</tt>）方法已被调用，则此状态代码将覆盖传递给该方法的状态代码。
     *
     * @throws SecurityException
     *         如果存在安全经理并且其 <tt>{@link
     *         SecurityManager#checkExit checkExit}</tt> 方法不允许以指定状态退出
     *
     * @see #exit
     * @see #addShutdownHook
     * @see #removeShutdownHook
     * @since 1.3
     */
    public void halt(int status) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkExit(status);
        }
        Shutdown.beforeHalt();
        Shutdown.halt(status);
    }

    /**
     * 启用或禁用退出时的终结；这样做指定在Java运行时退出之前，所有具有尚未自动调用的终结器的对象的终结器都要运行。
     * 默认情况下，退出时的终结是禁用的。
     *
     * <p>如果存在安全经理，
     * 其 <code>checkExit</code> 方法首先被调用
     * 以0作为其参数，以确保允许退出。
     * 这可能导致 SecurityException。
     *
     * @param value true 启用退出时的终结，false 禁用
     * @deprecated  此方法本质上是不安全的。它可能导致在其他线程同时操作这些对象时对活动对象调用终结器，从而导致不规则行为或死锁。
     *
     * @throws  SecurityException
     *        如果存在安全经理并且其 <code>checkExit</code>
     *        方法不允许退出。
     *
     * @see     java.lang.Runtime#exit(int)
     * @see     java.lang.Runtime#gc()
     * @see     java.lang.SecurityManager#checkExit(int)
     * @since   JDK1.1
     */
    @Deprecated
    public static void runFinalizersOnExit(boolean value) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            try {
                security.checkExit(0);
            } catch (SecurityException e) {
                throw new SecurityException("runFinalizersOnExit");
            }
        }
        Shutdown.setRunFinalizersOnExit(value);
    }

    /**
     * 在单独的进程中执行指定的字符串命令。
     *
     * <p>这是一个便利方法。形式为
     * <tt>exec(command)</tt>
     * 的调用
     * 与形式为
     * <tt>{@link #exec(String, String[], File) exec}(command, null, null)</tt>
     * 的调用行为完全相同。
     *
     * @param   command   指定的系统命令。
     *
     * @return  一个新的 {@link Process} 对象，用于管理子进程
     *
     * @throws  SecurityException
     *          如果存在安全经理并且其
     *          {@link SecurityManager#checkExec checkExec}
     *          方法不允许创建子进程
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @throws  NullPointerException
     *          如果 <code>command</code> 为 <code>null</code>
     *
     * @throws  IllegalArgumentException
     *          如果 <code>command</code> 为空
     *
     * @see     #exec(String[], String[], File)
     * @see     ProcessBuilder
     */
    public Process exec(String command) throws IOException {
        return exec(command, null, null);
    }

    /**
     * 在单独的进程中使用指定的环境执行指定的字符串命令。
     *
     * <p>这是一个便利方法。形式为
     * <tt>exec(command, envp)</tt>
     * 的调用
     * 与形式为
     * <tt>{@link #exec(String, String[], File) exec}(command, envp, null)</tt>
     * 的调用行为完全相同。
     *
     * @param   command   指定的系统命令。
     *
     * @param   envp      字符串数组，每个元素
     *                    以 <i>name</i>=<i>value</i> 的格式设置环境变量，或者
     *                    <tt>null</tt>，如果子进程应继承
     *                    当前进程的环境。
     *
     * @return  一个新的 {@link Process} 对象，用于管理子进程
     *
     * @throws  SecurityException
     *          如果存在安全经理并且其
     *          {@link SecurityManager#checkExec checkExec}
     *          方法不允许创建子进程
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @throws  NullPointerException
     *          如果 <code>command</code> 为 <code>null</code>，
     *          或 <code>envp</code> 的一个元素为 <code>null</code>
     *
     * @throws  IllegalArgumentException
     *          如果 <code>command</code> 为空
     *
     * @see     #exec(String[], String[], File)
     * @see     ProcessBuilder
     */
    public Process exec(String command, String[] envp) throws IOException {
        return exec(command, envp, null);
    }


    /**
     * 在单独的进程中执行指定的字符串命令，并指定环境和工作目录。
     *
     * <p>这是一个便捷方法。形式为
     * <tt>exec(command, envp, dir)</tt>
     * 的调用与形式为
     * <tt>{@link #exec(String[], String[], File) exec}(cmdarray, envp, dir)</tt>
     * 的调用行为完全相同，其中 <code>cmdarray</code> 是 <code>command</code> 中所有标记的数组。
     *
     * <p>更准确地说，<code>command</code> 字符串使用通过调用
     * <code>new {@link StringTokenizer}(command)</code> 创建的 <code>StringTokenizer</code> 分割成标记，不进一步修改字符类别。由分词器生成的标记然后按顺序放置在新的字符串数组 <code>cmdarray</code> 中。
     *
     * @param   command   指定的系统命令。
     *
     * @param   envp      字符串数组，每个元素的格式为环境变量设置
     *                    <i>name</i>=<i>value</i>，或
     *                    <tt>null</tt> 如果子进程应继承当前进程的环境。
     *
     * @param   dir       子进程的工作目录，或
     *                    <tt>null</tt> 如果子进程应继承当前进程的工作目录。
     *
     * @return  一个新的 {@link Process} 对象，用于管理子进程
     *
     * @throws  SecurityException
     *          如果存在安全管理者，并且其
     *          {@link SecurityManager#checkExec checkExec}
     *          方法不允许创建子进程
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @throws  NullPointerException
     *          如果 <code>command</code> 为 <code>null</code>，
     *          或 <code>envp</code> 的一个元素为 <code>null</code>
     *
     * @throws  IllegalArgumentException
     *          如果 <code>command</code> 为空
     *
     * @see     ProcessBuilder
     * @since 1.3
     */
    public Process exec(String command, String[] envp, File dir)
        throws IOException {
        if (command.isEmpty())
            throw new IllegalArgumentException("Empty command");

        StringTokenizer st = new StringTokenizer(command);
        String[] cmdarray = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++)
            cmdarray[i] = st.nextToken();
        return exec(cmdarray, envp, dir);
    }

    /**
     * 在单独的进程中执行指定的命令和参数。
     *
     * <p>这是一个便捷方法。形式为
     * <tt>exec(cmdarray)</tt>
     * 的调用与形式为
     * <tt>{@link #exec(String[], String[], File) exec}(cmdarray, null, null)</tt>
     * 的调用行为完全相同。
     *
     * @param   cmdarray  包含要调用的命令及其参数的数组。
     *
     * @return  一个新的 {@link Process} 对象，用于管理子进程
     *
     * @throws  SecurityException
     *          如果存在安全管理者，并且其
     *          {@link SecurityManager#checkExec checkExec}
     *          方法不允许创建子进程
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @throws  NullPointerException
     *          如果 <code>cmdarray</code> 为 <code>null</code>，
     *          或 <code>cmdarray</code> 的一个元素为 <code>null</code>
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <code>cmdarray</code> 是一个空数组
     *          （长度为 <code>0</code>）
     *
     * @see     ProcessBuilder
     */
    public Process exec(String cmdarray[]) throws IOException {
        return exec(cmdarray, null, null);
    }

    /**
     * 在单独的进程中执行指定的命令和参数，并指定环境。
     *
     * <p>这是一个便捷方法。形式为
     * <tt>exec(cmdarray, envp)</tt>
     * 的调用与形式为
     * <tt>{@link #exec(String[], String[], File) exec}(cmdarray, envp, null)</tt>
     * 的调用行为完全相同。
     *
     * @param   cmdarray  包含要调用的命令及其参数的数组。
     *
     * @param   envp      字符串数组，每个元素的格式为环境变量设置
     *                    <i>name</i>=<i>value</i>，或
     *                    <tt>null</tt> 如果子进程应继承当前进程的环境。
     *
     * @return  一个新的 {@link Process} 对象，用于管理子进程
     *
     * @throws  SecurityException
     *          如果存在安全管理者，并且其
     *          {@link SecurityManager#checkExec checkExec}
     *          方法不允许创建子进程
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @throws  NullPointerException
     *          如果 <code>cmdarray</code> 为 <code>null</code>，
     *          或 <code>cmdarray</code> 的一个元素为 <code>null</code>，
     *          或 <code>envp</code> 的一个元素为 <code>null</code>
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <code>cmdarray</code> 是一个空数组
     *          （长度为 <code>0</code>）
     *
     * @see     ProcessBuilder
     */
    public Process exec(String[] cmdarray, String[] envp) throws IOException {
        return exec(cmdarray, envp, null);
    }


    /**
     * 在单独的进程中执行指定的命令和参数，并指定环境和工作目录。
     *
     * <p>给定一个字符串数组 <code>cmdarray</code>，表示命令行的标记，和一个字符串数组 <code>envp</code>，
     * 表示“环境”变量设置，此方法创建一个新的进程来执行指定的命令。
     *
     * <p>此方法检查 <code>cmdarray</code> 是否为有效的操作系统命令。哪些命令有效是系统依赖的，
     * 但至少命令必须是非空的非空字符串列表。
     *
     * <p>如果 <tt>envp</tt> 为 <tt>null</tt>，子进程将继承当前进程的环境设置。
     *
     * <p>在某些操作系统上，启动进程可能需要一组最小的系统依赖环境变量。
     * 因此，子进程可能继承指定环境之外的额外环境变量设置。
     *
     * <p>{@link ProcessBuilder#start()} 现在是启动具有修改环境的进程的首选方法。
     *
     * <p>新子进程的工作目录由 <tt>dir</tt> 指定。如果 <tt>dir</tt> 为 <tt>null</tt>，子进程将继承
     * 当前进程的当前工作目录。
     *
     * <p>如果存在安全管理者，其
     * {@link SecurityManager#checkExec checkExec}
     * 方法将使用数组 <code>cmdarray</code> 的第一个组件作为参数调用。这可能导致
     * 抛出 {@link SecurityException}。
     *
     * <p>启动操作系统进程高度依赖于系统。可能出现的错误包括：
     * <ul>
     * <li>未找到操作系统程序文件。
     * <li>访问程序文件被拒绝。
     * <li>工作目录不存在。
     * </ul>
     *
     * <p>在这种情况下将抛出异常。异常的性质是系统依赖的，但始终是 {@link IOException} 的子类。
     *
     *
     * @param   cmdarray  包含要调用的命令及其参数的数组。
     *
     * @param   envp      字符串数组，每个元素的格式为环境变量设置
     *                    <i>name</i>=<i>value</i>，或
     *                    <tt>null</tt> 如果子进程应继承当前进程的环境。
     *
     * @param   dir       子进程的工作目录，或
     *                    <tt>null</tt> 如果子进程应继承当前进程的工作目录。
     *
     * @return  一个新的 {@link Process} 对象，用于管理子进程
     *
     * @throws  SecurityException
     *          如果存在安全管理者，并且其
     *          {@link SecurityManager#checkExec checkExec}
     *          方法不允许创建子进程
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @throws  NullPointerException
     *          如果 <code>cmdarray</code> 为 <code>null</code>，
     *          或 <code>cmdarray</code> 的一个元素为 <code>null</code>，
     *          或 <code>envp</code> 的一个元素为 <code>null</code>
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <code>cmdarray</code> 是一个空数组
     *          （长度为 <code>0</code>）
     *
     * @see     ProcessBuilder
     * @since 1.3
     */
    public Process exec(String[] cmdarray, String[] envp, File dir)
        throws IOException {
        return new ProcessBuilder(cmdarray)
            .environment(envp)
            .directory(dir)
            .start();
    }

    /**
     * 返回 Java 虚拟机可用的处理器数量。
     *
     * <p> 在虚拟机的特定调用过程中，此值可能会发生变化。因此，对可用处理器数量敏感的应用程序应定期轮询此属性，并相应地调整其资源使用。 </p>
     *
     * @return  虚拟机可用的最大处理器数量；永远不会小于一
     * @since 1.4
     */
    public native int availableProcessors();

    /**
     * 返回 Java 虚拟机中的空闲内存量。
     * 调用
     * <code>gc</code> 方法可能会导致 <code>freeMemory</code> 返回的值增加。
     *
     * @return  当前可用于未来分配对象的内存总量的近似值，以字节为单位。
     */
    public native long freeMemory();

    /**
     * 返回 Java 虚拟机中的总内存量。
     * 该方法返回的值可能随时间变化，具体取决于主机环境。
     * <p>
     * 注意，任何给定类型的对象所需的内存量可能因实现而异。
     *
     * @return  当前可用于当前和未来对象的总内存量，以字节为单位。
     */
    public native long totalMemory();

    /**
     * 返回 Java 虚拟机将尝试使用的最大内存量。如果没有固有的限制，则返回值为 {@link
     * java.lang.Long#MAX_VALUE}。
     *
     * @return  虚拟机将尝试使用的最大内存量，以字节为单位
     * @since 1.4
     */
    public native long maxMemory();

    /**
     * 运行垃圾回收器。
     * 调用此方法建议 Java 虚拟机花费精力回收未使用的对象，以便它们当前占用的内存可以快速重新使用。当方法调用返回时，虚拟机已尽力回收所有已丢弃的对象。
     * <p>
     * 名称 <code>gc</code> 代表“垃圾回收器”。即使不显式调用 <code>gc</code> 方法，虚拟机也会根据需要自动在单独的线程中执行此回收过程。
     * <p>
     * 方法 {@link System#gc()} 是调用此方法的传统和方便的方式。
     */
    public native void gc();

    /* 用于调用 java.lang.ref.Finalizer.runFinalization 的通道 */
    private static native void runFinalization0();

    /**
     * 运行任何待定终结的对象的终结方法。
     * 调用此方法建议 Java 虚拟机花费精力运行已发现被丢弃但其 <code>finalize</code> 方法尚未运行的对象的 <code>finalize</code> 方法。当方法调用返回时，虚拟机已尽力完成所有未完成的终结。
     * <p>
     * 即使不显式调用 <code>runFinalization</code> 方法，虚拟机也会根据需要自动在单独的线程中执行终结过程。
     * <p>
     * 方法 {@link System#runFinalization()} 是调用此方法的传统和方便的方式。
     *
     * @see     java.lang.Object#finalize()
     */
    public void runFinalization() {
        runFinalization0();
    }

    /**
     * 启用/禁用指令跟踪。
     * 如果 <code>boolean</code> 参数为 <code>true</code>，此方法建议 Java 虚拟机在执行虚拟机中的每条指令时发出调试信息。此信息的格式以及它被发出的文件或其他输出流取决于主机环境。如果虚拟机不支持此功能，它可以忽略此请求。跟踪输出的目的地取决于系统。
     * <p>
     * 如果 <code>boolean</code> 参数为 <code>false</code>，此方法导致虚拟机停止执行详细的指令跟踪。
     *
     * @param   on   <code>true</code> 以启用指令跟踪；
     *               <code>false</code> 以禁用此功能。
     */
    public native void traceInstructions(boolean on);

    /**
     * 启用/禁用方法调用跟踪。
     * 如果 <code>boolean</code> 参数为 <code>true</code>，此方法建议 Java 虚拟机在调用虚拟机中的每个方法时发出调试信息。此信息的格式以及它被发出的文件或其他输出流取决于主机环境。如果虚拟机不支持此功能，它可以忽略此请求。
     * <p>
     * 使用参数 false 调用此方法建议虚拟机停止发出每次调用的调试信息。
     *
     * @param   on   <code>true</code> 以启用指令跟踪；
     *               <code>false</code> 以禁用此功能。
     */
    public native void traceMethodCalls(boolean on);

    /**
     * 加载由文件名参数指定的本地库。文件名参数必须是绝对路径名。
     * （例如
     * <code>Runtime.getRuntime().load("/home/avh/lib/libX11.so");</code>）。
     *
     * 如果文件名参数，去除了任何平台特定的库前缀、路径和文件扩展名后，指示一个名为 L 的库，并且一个名为 L 的本地库与 VM 静态链接，则导出该库的 JNI_OnLoad_L 函数将被调用，而不是尝试加载动态库。
     * 不需要文件系统中存在与参数匹配的文件。有关更多详细信息，请参阅 JNI 规范。
     *
     * 否则，文件名参数以实现依赖的方式映射到本地库映像。
     * <p>
     * 首先，如果有安全管理者，其 <code>checkLink</code>
     * 方法将使用 <code>filename</code> 作为其参数被调用。
     * 这可能导致安全异常。
     * <p>
     * 这类似于方法 {@link #loadLibrary(String)}，但它接受一般文件名作为参数，而不仅仅是库名，允许加载任何本地代码文件。
     * <p>
     * 方法 {@link System#load(String)} 是调用此方法的传统和方便的方式。
     *
     * @param      filename   要加载的文件。
     * @exception  SecurityException  如果存在安全管理者，其
     *             <code>checkLink</code> 方法不允许
     *             加载指定的动态库
     * @exception  UnsatisfiedLinkError  如果文件名不是绝对路径名，本地库未与 VM 静态链接，或者主机系统无法将库映射到本地库映像。
     * @exception  NullPointerException 如果 <code>filename</code> 为
     *             <code>null</code>
     * @see        java.lang.Runtime#getRuntime()
     * @see        java.lang.SecurityException
     * @see        java.lang.SecurityManager#checkLink(java.lang.String)
     */
    @CallerSensitive
    public void load(String filename) {
        load0(Reflection.getCallerClass(), filename);
    }


                    synchronized void load0(Class<?> fromClass, String filename) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkLink(filename);
        }
        if (!(new File(filename).isAbsolute())) {
            throw new UnsatisfiedLinkError(
                "期望库的绝对路径: " + filename);
        }
        ClassLoader.loadLibrary(fromClass, filename, true);
    }

    /**
     * 加载由 <code>libname</code> 参数指定的本地库。 <code>libname</code> 参数不得包含任何平台特定的前缀、文件扩展名或路径。如果一个名为 <code>libname</code> 的本地库与 VM 静态链接，则由库导出的 JNI_OnLoad_<code>libname</code> 函数将被调用。
     * 有关更多详细信息，请参阅 JNI 规范。
     *
     * 否则，<code>libname</code> 参数将从系统库位置加载，并以实现依赖的方式映射到本地库映像。
     * <p>
     * 首先，如果有安全经理，其 <code>checkLink</code> 方法将被调用，参数为 <code>libname</code>。
     * 这可能会导致安全异常。
     * <p>
     * 方法 {@link System#loadLibrary(String)} 是调用此方法的传统和便捷方式。如果类的实现中使用了本地方法，一个标准策略是将本地代码放入库文件（称为 <code>LibFile</code>）中，然后在类声明中放置一个静态初始化器：
     * <blockquote><pre>
     * static { System.loadLibrary("LibFile"); }
     * </pre></blockquote>
     * 当类被加载和初始化时，必要的本地代码实现也将被加载。
     * <p>
     * 如果此方法被多次调用且库名相同，则第二次及后续调用将被忽略。
     *
     * @param      libname   库的名称。
     * @exception  SecurityException  如果存在安全经理且其 <code>checkLink</code> 方法不允许
     *             加载指定的动态库
     * @exception  UnsatisfiedLinkError 如果 <code>libname</code> 参数包含文件路径，本地库未与 VM 静态链接，或库无法被主机系统映射到本地库映像。
     * @exception  NullPointerException 如果 <code>libname</code> 为
     *             <code>null</code>
     * @see        java.lang.SecurityException
     * @see        java.lang.SecurityManager#checkLink(java.lang.String)
     */
    @CallerSensitive
    public void loadLibrary(String libname) {
        loadLibrary0(Reflection.getCallerClass(), libname);
    }

    synchronized void loadLibrary0(Class<?> fromClass, String libname) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkLink(libname);
        }
        if (libname.indexOf((int)File.separatorChar) != -1) {
            throw new UnsatisfiedLinkError(
    "库名中不应出现目录分隔符: " + libname);
        }
        ClassLoader.loadLibrary(fromClass, libname, false);
    }

    /**
     * 创建输入流的本地化版本。此方法接受一个 <code>InputStream</code> 并返回一个在所有方面都等同于参数的 <code>InputStream</code>，但它是本地化的：从流中读取的本地字符集中的字符会自动转换为 Unicode。
     * <p>
     * 如果参数已经是本地化流，则可以将其作为结果返回。
     *
     * @param      in 要本地化的输入流
     * @return     本地化输入流
     * @see        java.io.InputStream
     * @see        java.io.BufferedReader#BufferedReader(java.io.Reader)
     * @see        java.io.InputStreamReader#InputStreamReader(java.io.InputStream)
     * @deprecated 自 JDK&nbsp;1.1 起，将本地编码的字节流转换为 Unicode 字符流的首选方法是通过 <code>InputStreamReader</code> 和 <code>BufferedReader</code> 类。
     */
    @Deprecated
    public InputStream getLocalizedInputStream(InputStream in) {
        return in;
    }

    /**
     * 创建输出流的本地化版本。此方法接受一个 <code>OutputStream</code> 并返回一个在所有方面都等同于参数的 <code>OutputStream</code>，但它是本地化的：将 Unicode 字符写入流时，它们会自动转换为本地字符集。
     * <p>
     * 如果参数已经是本地化流，则可以将其作为结果返回。
     *
     * @deprecated 自 JDK&nbsp;1.1 起，将 Unicode 字符流转换为本地编码的字节流的首选方法是通过 <code>OutputStreamWriter</code>、<code>BufferedWriter</code> 和 <code>PrintWriter</code> 类。
     *
     * @param      out 要本地化的输出流
     * @return     本地化输出流
     * @see        java.io.OutputStream
     * @see        java.io.BufferedWriter#BufferedWriter(java.io.Writer)
     * @see        java.io.OutputStreamWriter#OutputStreamWriter(java.io.OutputStream)
     * @see        java.io.PrintWriter#PrintWriter(java.io.OutputStream)
     */
    @Deprecated
    public OutputStream getLocalizedOutputStream(OutputStream out) {
        return out;
    }

}
