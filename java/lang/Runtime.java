
/*
 * Copyright (c) 1995, 2022, Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;
import java.util.StringTokenizer;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;

/**
 * 每个 Java 应用程序都有一个类 <code>Runtime</code> 的单个实例，允许应用程序与应用程序运行的环境进行交互。可以通过 <code>getRuntime</code> 方法获取当前的运行时对象。
 * <p>
 * 应用程序不能创建此类的实例。
 *
 * @author  未署名
 * @see     java.lang.Runtime#getRuntime()
 * @since   JDK1.0
 */

public class Runtime {
    private static Runtime currentRuntime = new Runtime();

    /**
     * 返回与当前 Java 应用程序关联的运行时对象。类 <code>Runtime</code> 的大多数方法都是实例方法，必须针对当前运行时对象调用。
     *
     * @return  与当前 Java 应用程序关联的 <code>Runtime</code> 对象。
     */
    public static Runtime getRuntime() {
        return currentRuntime;
    }

    /** 不让其他人实例化这个类 */
    private Runtime() {}

    /**
     * 通过启动其关闭序列来终止当前正在运行的 Java 虚拟机。此方法永远不会正常返回。参数用作状态码；按照惯例，非零状态码表示异常终止。
     *
     * <p> 所有已注册的 {@linkplain #addShutdownHook 关闭钩子}（如果有）将以某种未指定的顺序启动并允许并发运行，直到它们完成。一旦完成，虚拟机将 {@linkplain #halt 停止}。
     *
     * <p> 如果在所有关闭钩子已经运行完毕后调用此方法且状态码为非零，则此方法将以给定的状态码停止虚拟机。否则，此方法将无限期阻塞。
     *
     * <p> {@link System#exit(int) System.exit} 方法是调用此方法的传统和方便的手段。
     *
     * @param  status
     *         终止状态。按照惯例，非零状态码表示异常终止。
     *
     * @throws SecurityException
     *         如果存在安全管理器且其 {@link SecurityManager#checkExit checkExit} 方法不允许以指定的状态退出
     *
     * @see java.lang.SecurityException
     * @see java.lang.SecurityManager#checkExit(int)
     * @see #addShutdownHook
     * @see #removeShutdownHook
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
     * 注册一个新的虚拟机关闭钩子。
     *
     * <p> Java 虚拟机在响应以下两种事件时 <i>关闭</i>：
     *
     *   <ul>
     *
     *   <li> 程序 <i>正常退出</i>，当最后一个非守护线程退出或调用 {@link #exit exit}（等效于 {@link System#exit(int) System.exit}）方法时，或者
     *
     *   <li> 虚拟机因用户中断（如输入 {@code ^C}）或系统事件（如用户注销或系统关闭）而 <i>终止</i>。
     *
     *   </ul>
     *
     * <p> <i>关闭钩子</i> 只是一个已初始化但未启动的线程。当虚拟机开始其关闭序列时，它将以某种未指定的顺序启动所有已注册的关闭钩子并让它们并发运行。当所有钩子完成后，它将停止。请注意，守护线程将在关闭序列期间继续运行，如果关闭是由调用 {@link #exit exit} 方法启动的，非守护线程也会继续运行。
     *
     * <p> 一旦关闭序列开始，只有通过调用 {@link #halt halt} 方法才能停止。一旦关闭序列开始，就无法注册新的关闭钩子或取消注册已注册的钩子。尝试这些操作将导致抛出 {@link IllegalStateException}。
     *
     * <p> 关闭钩子在虚拟机生命周期的敏感时期运行，因此应谨慎编写。特别是，它们应编写为线程安全的，并尽可能避免死锁。它们也不应盲目依赖可能已注册自己的关闭钩子的服务，因为这些服务本身可能正在关闭。例如，尝试使用其他基于线程的服务（如 AWT 事件分发线程）可能会导致死锁。
     *
     * <p> 关闭钩子应尽快完成其工作。当程序调用 {@link #exit exit} 时，期望虚拟机能够迅速关闭并退出。当虚拟机因用户注销或系统关闭而终止时，底层操作系统可能只允许固定的时间来关闭并退出。因此，不建议在关闭钩子中尝试用户交互或执行长时间的计算。
     *
     * <p> 未捕获的异常在关闭钩子中处理的方式与在任何其他线程中处理的方式相同，即调用线程的 {@link ThreadGroup} 对象的 {@link ThreadGroup#uncaughtException uncaughtException} 方法。该方法的默认实现将异常的堆栈跟踪打印到 {@link System#err} 并终止线程；它不会导致虚拟机退出或停止。
     *
     * <p> 在极少数情况下，虚拟机可能会 <i>中止</i>，即在没有干净关闭的情况下停止运行。这发生在虚拟机被外部终止时，例如在 Unix 上使用 {@code SIGKILL} 信号或在 Microsoft Windows 上使用 {@code TerminateProcess} 调用。虚拟机也可能因本机方法出错而中止，例如，通过破坏内部数据结构或尝试访问不存在的内存。如果虚拟机中止，则无法保证任何关闭钩子会被运行。
     *
     * @param   hook
     *          一个已初始化但未启动的 {@link Thread} 对象
     *
     * @throws  IllegalArgumentException
     *          如果指定的钩子已注册，或者可以确定钩子已经在运行或已经运行过
     *
     * @throws  IllegalStateException
     *          如果虚拟机已经在关闭过程中
     *
     * @throws  SecurityException
     *          如果存在安全管理器且它拒绝 {@link RuntimePermission}{@code ("shutdownHooks")}
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
     * 取消注册先前注册的虚拟机关闭钩子。 <p>
     *
     * @param hook 要移除的钩子
     * @return <tt>true</tt> 如果指定的钩子已注册并成功取消注册，<tt>false</tt> 否则。
     *
     * @throws  IllegalStateException
     *          如果虚拟机已经在关闭过程中
     *
     * @throws  SecurityException
     *          如果存在安全管理器且它拒绝 <tt>{@link RuntimePermission}("shutdownHooks")</tt>
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
     * 强制终止当前正在运行的 Java 虚拟机。此方法永远不会正常返回。
     *
     * <p> 应极其谨慎地使用此方法。与 {@link #exit exit} 方法不同，此方法不会启动关闭钩子。如果关闭序列已经启动，则此方法不会等待任何正在运行的关闭钩子完成其工作。
     *
     * @param  status
     *         终止状态。按照惯例，非零状态码表示异常终止。如果已调用 {@link Runtime#exit exit}（等效于 {@link System#exit(int) System.exit}）方法，则此状态码将覆盖传递给该方法的状态码。
     *
     * @throws SecurityException
     *         如果存在安全管理器且其 {@link SecurityManager#checkExit checkExit} 方法不允许以指定的状态退出
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
     * 抛出 {@code UnsupportedOperationException}。
     *
     * @param value 被忽略
     *
     * @deprecated 此方法最初设计用于在退出时启用或禁用运行终结器。默认情况下，退出时运行终结器是禁用的。如果启用，则在 Java 运行时退出之前，将运行所有尚未自动调用终结器的对象的终结器。这种行为本质上是不安全的。它可能导致在其他线程同时操作这些对象时调用终结器，从而导致不规则行为或死锁。
     *
     * @since JDK1.1
     */
    @Deprecated
    public static void runFinalizersOnExit(boolean value) {
        throw new UnsupportedOperationException();
    }

    /**
     * 在单独的进程中执行指定的字符串命令。
     *
     * <p>这是一个便捷方法。形式为 <tt>exec(command)</tt> 的调用
     * 与形式为 <tt>{@link #exec(String, String[], File) exec}(command, null, null)</tt> 的调用行为完全相同。
     *
     * @param   command   指定的系统命令。
     *
     * @return  用于管理子进程的新 {@link Process} 对象
     *
     * @throws  SecurityException
     *          如果存在安全管理器且其 {@link SecurityManager#checkExec checkExec}
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
     * 在指定的环境中以单独的进程执行指定的字符串命令。
     *
     * <p>这是一个便捷方法。形式为 <tt>exec(command, envp)</tt> 的调用
     * 与形式为 <tt>{@link #exec(String, String[], File) exec}(command, envp, null)</tt> 的调用行为完全相同。
     *
     * @param   command   指定的系统命令。
     *
     * @param   envp      字符串数组，每个元素的格式为 <i>name</i>=<i>value</i>，表示环境变量设置，或者
     *                    <tt>null</tt> 表示子进程应继承当前进程的环境。
     *
     * @return  用于管理子进程的新 {@link Process} 对象
     *
     * @throws  SecurityException
     *          如果存在安全管理器且其 {@link SecurityManager#checkExec checkExec}
     *          方法不允许创建子进程
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @throws  NullPointerException
     *          如果 <code>command</code> 为 <code>null</code>，
     *          或 <code>envp</code> 的某个元素为 <code>null</code>
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
     * 在指定的环境和工作目录中以单独的进程执行指定的字符串命令。
     *
     * <p>这是一个便捷方法。形式为 <tt>exec(command, envp, dir)</tt> 的调用
     * 与形式为 <tt>{@link #exec(String[], String[], File) exec}(cmdarray, envp, dir)</tt> 的调用行为完全相同，其中 <code>cmdarray</code> 是 <code>command</code> 中所有标记的数组。
     *
     * <p>更精确地说，<code>command</code> 字符串使用 {@link StringTokenizer} 通过调用 <code>new {@link StringTokenizer}(command)</code> 分割成标记，不进一步修改字符类别。然后将分词器生成的标记按顺序放入新的字符串数组 <code>cmdarray</code> 中。
     *
     * @param   command   指定的系统命令。
     *
     * @param   envp      字符串数组，每个元素的格式为 <i>name</i>=<i>value</i>，表示环境变量设置，或者
     *                    <tt>null</tt> 表示子进程应继承当前进程的环境。
     *
     * @param   dir       子进程的工作目录，或者
     *                    <tt>null</tt> 表示子进程应继承当前进程的工作目录。
     *
     * @return  用于管理子进程的新 {@link Process} 对象
     *
     * @throws  SecurityException
     *          如果存在安全管理器且其 {@link SecurityManager#checkExec checkExec}
     *          方法不允许创建子进程
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @throws  NullPointerException
     *          如果 <code>command</code> 为 <code>null</code>，
     *          或 <code>envp</code> 的某个元素为 <code>null</code>
     *
     * @throws  IllegalArgumentException
     *          如果 <code>command</code> 为空
     *
     * @see     ProcessBuilder
     * @since 1.3
     */
    public Process exec(String command, String[] envp, File dir)
        throws IOException {
        if (command.length() == 0)
            throw new IllegalArgumentException("Empty command");


                    StringTokenizer st = new StringTokenizer(command);
        String[] cmdarray = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++)
            cmdarray[i] = st.nextToken();
        return exec(cmdarray, envp, dir);
    }

    /**
     * 执行指定的命令和参数，以单独的进程运行。
     *
     * <p>这是一个便捷方法。形式为 <tt>exec(cmdarray)</tt> 的调用
     * 与形式为 <tt>{@link #exec(String[], String[], File) exec}(cmdarray, null, null)</tt> 的调用
     * 完全相同。
     *
     * @param   cmdarray  包含要调用的命令及其参数的数组。
     *
     * @return  一个新的 {@link Process} 对象，用于管理子进程
     *
     * @throws  SecurityException
     *          如果存在安全管理器，并且其
     *          {@link SecurityManager#checkExec checkExec}
     *          方法不允许创建子进程
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @throws  NullPointerException
     *          如果 <code>cmdarray</code> 为 <code>null</code>，
     *          或 <code>cmdarray</code> 的某个元素为 <code>null</code>
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
     * 以指定的环境执行指定的命令和参数，以单独的进程运行。
     *
     * <p>这是一个便捷方法。形式为 <tt>exec(cmdarray, envp)</tt> 的调用
     * 与形式为 <tt>{@link #exec(String[], String[], File) exec}(cmdarray, envp, null)</tt> 的调用
     * 完全相同。
     *
     * @param   cmdarray  包含要调用的命令及其参数的数组。
     *
     * @param   envp      字符串数组，每个元素的格式为
     *                    <i>name</i>=<i>value</i>，表示环境变量设置，或
     *                    <tt>null</tt> 表示子进程应继承当前进程的环境。
     *
     * @return  一个新的 {@link Process} 对象，用于管理子进程
     *
     * @throws  SecurityException
     *          如果存在安全管理器，并且其
     *          {@link SecurityManager#checkExec checkExec}
     *          方法不允许创建子进程
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @throws  NullPointerException
     *          如果 <code>cmdarray</code> 为 <code>null</code>，
     *          或 <code>cmdarray</code> 的某个元素为 <code>null</code>，
     *          或 <code>envp</code> 的某个元素为 <code>null</code>
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
     * 以指定的环境和工作目录执行指定的命令和参数，以单独的进程运行。
     *
     * <p>给定一个字符串数组 <code>cmdarray</code>，表示命令行的标记，
     * 和一个字符串数组 <code>envp</code>，表示“环境”变量设置，此方法创建
     * 一个新的进程来执行指定的命令。
     *
     * <p>此方法检查 <code>cmdarray</code> 是否为有效的操作系统命令。哪些命令有效是系统相关的，
     * 但至少命令必须是非空的非空字符串列表。
     *
     * <p>如果 <tt>envp</tt> 为 <tt>null</tt>，子进程将继承当前进程的环境设置。
     *
     * <p>在某些操作系统上，启动进程可能需要一组最小的系统依赖环境变量。
     * 因此，子进程可能继承的环境变量设置可能超出指定的环境。
     *
     * <p>{@link ProcessBuilder#start()} 现在是启动具有修改环境的进程的首选方法。
     *
     * <p>子进程的工作目录由 <tt>dir</tt> 指定。如果 <tt>dir</tt> 为 <tt>null</tt>，子进程将继承
     * 当前进程的当前工作目录。
     *
     * <p>如果存在安全管理器，其
     * {@link SecurityManager#checkExec checkExec}
     * 方法将使用数组 <code>cmdarray</code> 的第一个组件作为其参数调用。这可能导致
     * 抛出一个 {@link SecurityException}。
     *
     * <p>启动操作系统进程高度依赖于系统。可能出错的情况包括：
     * <ul>
     * <li>操作系统程序文件未找到。
     * <li>访问程序文件被拒绝。
     * <li>工作目录不存在。
     * </ul>
     *
     * <p>在这种情况下将抛出异常。异常的性质是系统相关的，但总是 {@link IOException} 的子类。
     *
     *
     * @param   cmdarray  包含要调用的命令及其参数的数组。
     *
     * @param   envp      字符串数组，每个元素的格式为
     *                    <i>name</i>=<i>value</i>，表示环境变量设置，或
     *                    <tt>null</tt> 表示子进程应继承当前进程的环境。
     *
     * @param   dir       子进程的工作目录，或
     *                    <tt>null</tt> 表示子进程应继承当前进程的工作目录。
     *
     * @return  一个新的 {@link Process} 对象，用于管理子进程
     *
     * @throws  SecurityException
     *          如果存在安全管理器，并且其
     *          {@link SecurityManager#checkExec checkExec}
     *          方法不允许创建子进程
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @throws  NullPointerException
     *          如果 <code>cmdarray</code> 为 <code>null</code>，
     *          或 <code>cmdarray</code> 的某个元素为 <code>null</code>，
     *          或 <code>envp</code> 的某个元素为 <code>null</code>
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
     * <p>此值在虚拟机的特定调用期间可能会发生变化。因此，对可用处理器数量敏感的应用程序应定期
     * 轮询此属性，并根据需要调整资源使用情况。</p>
     *
     * @return  虚拟机可用的最大处理器数量；从不小于一
     * @since 1.4
     */
    public native int availableProcessors();

    /**
     * 返回 Java 虚拟机中的空闲内存量。
     * 调用 <code>gc</code> 方法可能会增加 <code>freeMemory</code> 返回的值。
     *
     * @return  一个近似值，表示当前可用于未来分配对象的总内存量，以字节为单位。
     */
    public native long freeMemory();

    /**
     * 返回 Java 虚拟机中的总内存量。
     * 此方法返回的值可能随时间变化，取决于主机环境。
     * <p>
     * 请注意，任何给定类型对象所需的内存量可能是实现依赖的。
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
     * 运行垃圾收集器。
     * 调用此方法建议 Java 虚拟机花费努力回收未使用的对象，以便使它们当前占用的内存可用于快速重用。当
     * 方法调用返回时，虚拟机已尽力回收所有已丢弃的对象。
     * <p>
     * 名称 <code>gc</code> 代表“垃圾收集器”。虚拟机会根据需要自动执行此回收过程，
     * 在单独的线程中，即使 <code>gc</code> 方法未显式调用。
     * <p>
     * 方法 {@link System#gc()} 是调用此方法的传统和便捷方式。
     */
    public native void gc();

    /* 用于调用 java.lang.ref.Finalizer.runFinalization 的通道 */
    private static native void runFinalization0();

    /**
     * 运行任何待定终结的对象的终结方法。
     * 调用此方法建议 Java 虚拟机花费努力运行已发现被丢弃但其 <code>finalize</code>
     * 方法尚未运行的对象的 <code>finalize</code> 方法。当方法调用返回时，虚拟机已尽力
     * 完成所有未完成的终结。
     * <p>
     * 虚拟机会根据需要自动执行终结过程，在单独的线程中，即使 <code>runFinalization</code>
     * 方法未显式调用。
     * <p>
     * 方法 {@link System#runFinalization()} 是调用此方法的传统和便捷方式。
     *
     * @see     java.lang.Object#finalize()
     */
    public void runFinalization() {
        runFinalization0();
    }

    /**
     * 启用/禁用指令跟踪。
     * 如果 <code>boolean</code> 参数为 <code>true</code>，此方法建议 Java 虚拟机在执行虚拟机中的每个指令时
     * 发出调试信息。此信息的格式以及它被发出的文件或其他输出流取决于主机环境。如果虚拟机不支持此功能，
     * 则可以忽略此请求。跟踪输出的目标是系统相关的。
     * <p>
     * 如果 <code>boolean</code> 参数为 <code>false</code>，此方法将导致虚拟机停止执行
     * 详细的指令跟踪。
     *
     * @param   on   <code>true</code> 表示启用指令跟踪；
     *               <code>false</code> 表示禁用此功能。
     */
    public native void traceInstructions(boolean on);

    /**
     * 启用/禁用方法调用跟踪。
     * 如果 <code>boolean</code> 参数为 <code>true</code>，此方法建议 Java 虚拟机在调用虚拟机中的每个方法时
     * 发出调试信息。此信息的格式以及它被发出的文件或其他输出流取决于主机环境。如果虚拟机不支持此功能，
     * 则可以忽略此请求。
     * <p>
     * 使用参数 false 调用此方法建议虚拟机停止发出每次调用的调试信息。
     *
     * @param   on   <code>true</code> 表示启用指令跟踪；
     *               <code>false</code> 表示禁用此功能。
     */
    public native void traceMethodCalls(boolean on);

    /**
     * 加载由文件名参数指定的本机库。文件名参数必须是绝对路径名。
     * （例如 <code>Runtime.getRuntime().load("/home/avh/lib/libX11.so");</code>）。
     *
     * 如果文件名参数，去掉任何平台特定的库前缀、路径和文件扩展名后，表示一个名为 L 的库，
     * 并且一个名为 L 的本机库与 VM 静态链接，则导出该库的 JNI_OnLoad_L 函数将被调用，
     * 而不是尝试加载动态库。文件系统中不需要存在与参数匹配的文件。有关更多详细信息，请参阅 JNI 规范。
     *
     * 否则，文件名参数将以实现依赖的方式映射到本机库映像。
     * <p>
     * 首先，如果存在安全管理器，其 <code>checkLink</code>
     * 方法将使用 <code>filename</code> 作为其参数调用。这可能导致安全异常。
     * <p>
     * 这类似于方法 {@link #loadLibrary(String)}，但它接受一般文件名作为参数，而不仅仅是库名，
     * 允许加载任何本机代码文件。
     * <p>
     * 方法 {@link System#load(String)} 是调用此方法的传统和便捷方式。
     *
     * @param      filename   要加载的文件。
     * @exception  SecurityException  如果存在安全管理器，并且其
     *             <code>checkLink</code> 方法不允许
     *             加载指定的动态库
     * @exception  UnsatisfiedLinkError  如果文件名不是绝对路径名，
     *             本机库未与 VM 静态链接，或主机系统无法将库映射到
     *             本机库映像。
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
     * 加载由 <code>libname</code> 参数指定的本地库。 <code>libname</code> 参数不得包含任何平台特定的前缀、文件扩展名或路径。如果名为 <code>libname</code> 的本地库与 VM 静态链接，则库导出的 JNI_OnLoad_<code>libname</code> 函数将被调用。有关更多详细信息，请参阅 JNI 规范。
     *
     * 否则，<code>libname</code> 参数将从系统库位置加载，并以实现依赖的方式映射到本地库映像。
     * <p>
     * 首先，如果有安全经理，其 <code>checkLink</code> 方法将使用 <code>libname</code> 作为参数调用。这可能导致安全异常。
     * <p>
     * 方法 {@link System#loadLibrary(String)} 是调用此方法的传统和便捷手段。如果类的实现中使用了本地方法，一种标准策略是将本地代码放入库文件（称为 <code>LibFile</code>）中，然后在类声明中放置一个静态初始化器：
     * <blockquote><pre>
     * static { System.loadLibrary("LibFile"); }
     * </pre></blockquote>
     * 当类被加载和初始化时，必要的本地代码实现将与本地方法一起加载。
     * <p>
     * 如果此方法被多次调用且库名相同，则第二次及后续调用将被忽略。
     *
     * @param      libname   库的名称。
     * @exception  SecurityException  如果存在安全经理且其 <code>checkLink</code> 方法不允许加载指定的动态库
     * @exception  UnsatisfiedLinkError 如果 <code>libname</code> 参数包含文件路径，本地库未与 VM 静态链接，或库无法映射到主机系统的本地库映像。
     * @exception  NullPointerException 如果 <code>libname</code> 为 <code>null</code>
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
     * 创建输入流的本地化版本。此方法接受一个 <code>InputStream</code> 并返回一个在所有方面都等同于参数的 <code>InputStream</code>，但它是本地化的：从流中读取本地字符集中的字符时，它们会自动转换为 Unicode。
     * <p>
     * 如果参数已经是本地化流，则可以将其作为结果返回。
     *
     * @param      in 要本地化的 InputStream
     * @return     本地化的输入流
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
     * @param      out 要本地化的 OutputStream
     * @return     本地化的输出流
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
