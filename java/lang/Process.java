
/*
 * Copyright (c) 1995, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
import java.util.concurrent.TimeUnit;

/**
 * {@link ProcessBuilder#start()} 和
 * {@link Runtime#exec(String[],String[],File) Runtime.exec}
 * 方法创建一个本地进程并返回一个 {@code Process} 子类的实例，该实例可用于控制进程并获取有关进程的信息。{@code Process}
 * 类提供了从进程读取输入、向进程写入输出、等待进程完成、检查进程的退出状态以及销毁（终止）进程的方法。
 *
 * <p>在某些本地平台上，对于特殊进程（如本地窗口进程、守护进程、Microsoft Windows 上的 Win16/DOS 进程或 shell 脚本），创建进程的方法可能无法正常工作。
 *
 * <p>默认情况下，创建的子进程没有自己的终端或控制台。所有标准 I/O（即标准输入、标准输出、标准错误）操作都将重定向到父进程，可以通过使用
 * {@link #getOutputStream()}、
 * {@link #getInputStream()} 和
 * {@link #getErrorStream()} 方法获取的流来访问这些操作。
 * 父进程使用这些流向子进程提供输入并从子进程获取输出。由于某些本地平台只为标准输入和输出流提供有限的缓冲区大小，因此未能及时写入输入流或读取输出流
 * 可能会导致子进程阻塞，甚至死锁。
 *
 * <p>如果需要，可以使用 {@link ProcessBuilder} 类的方法
 * <a href="ProcessBuilder.html#redirect-input">重定向子进程 I/O</a>。
 *
 * <p>当没有更多对 {@code Process} 对象的引用时，子进程不会被终止，而是继续异步执行。
 *
 * <p>没有要求表示进程的 {@code Process} 对象必须相对于拥有该 {@code Process} 对象的 Java 进程异步或并发执行。
 *
 * <p>从 1.5 开始，{@link ProcessBuilder#start()} 是创建 {@code Process} 的首选方式。
 *
 * @since   JDK1.0
 */
public abstract class Process {
    /**
     * 返回连接到子进程标准输入的输出流。向此流写入的数据将被管道到此 {@code Process} 对象表示的进程的标准输入。
     *
     * <p>如果子进程的标准输入已使用
     * {@link ProcessBuilder#redirectInput(Redirect)
     * ProcessBuilder.redirectInput}
     * 重定向，则此方法将返回一个
     * <a href="ProcessBuilder.html#redirect-input">空输出流</a>。
     *
     * <p>实现注意事项：返回的输出流最好进行缓冲。
     *
     * @return 连接到子进程标准输入的输出流
     */
    public abstract OutputStream getOutputStream();

    /**
     * 返回连接到子进程标准输出的输入流。此流从此 {@code Process} 对象表示的进程的标准输出获取数据。
     *
     * <p>如果子进程的标准输出已使用
     * {@link ProcessBuilder#redirectOutput(Redirect)
     * ProcessBuilder.redirectOutput}
     * 重定向，则此方法将返回一个
     * <a href="ProcessBuilder.html#redirect-output">空输入流</a>。
     *
     * <p>否则，如果子进程的标准错误已使用
     * {@link ProcessBuilder#redirectErrorStream(boolean)
     * ProcessBuilder.redirectErrorStream}
     * 重定向，则此方法返回的输入流将接收子进程的合并标准输出和标准错误。
     *
     * <p>实现注意事项：返回的输入流最好进行缓冲。
     *
     * @return 连接到子进程标准输出的输入流
     */
    public abstract InputStream getInputStream();

    /**
     * 返回连接到子进程标准错误的输入流。此流从此 {@code Process} 对象表示的进程的标准错误获取数据。
     *
     * <p>如果子进程的标准错误已使用
     * {@link ProcessBuilder#redirectError(Redirect)
     * ProcessBuilder.redirectError} 或
     * {@link ProcessBuilder#redirectErrorStream(boolean)
     * ProcessBuilder.redirectErrorStream}
     * 重定向，则此方法将返回一个
     * <a href="ProcessBuilder.html#redirect-output">空输入流</a>。
     *
     * <p>实现注意事项：返回的输入流最好进行缓冲。
     *
     * @return 连接到子进程标准错误的输入流
     */
    public abstract InputStream getErrorStream();

    /**
     * 如果需要，使当前线程等待，直到此 {@code Process} 对象表示的进程终止。如果子进程已经终止，此方法立即返回。如果子进程尚未终止，
     * 调用线程将被阻塞，直到子进程退出。
     *
     * @return 此 {@code Process} 对象表示的子进程的退出值。按照惯例，值 {@code 0} 表示正常终止。
     * @throws InterruptedException 如果当前线程在等待时被其他线程 {@linkplain Thread#interrupt() 中断}，则等待结束并抛出
     *         {@link InterruptedException}。
     */
    public abstract int waitFor() throws InterruptedException;

    /**
     * 如果需要，使当前线程等待，直到此 {@code Process} 对象表示的子进程终止，或者指定的等待时间过去。
     *
     * <p>如果子进程已经终止，则此方法立即返回值 {@code true}。如果进程尚未终止且超时值小于或等于零，则此方法立即返回值 {@code false}。
     *
     * <p>此方法的默认实现轮询 {@code exitValue} 以检查进程是否已终止。强烈建议此类的具体实现重写此方法以提供更高效的实现。
     *
     * @param timeout 最大等待时间
     * @param unit {@code timeout} 参数的时间单位
     * @return 如果子进程已退出则返回 {@code true}，如果在子进程退出前等待时间已过去则返回 {@code false}。
     * @throws InterruptedException 如果当前线程在等待时被中断。
     * @throws NullPointerException 如果 unit 为 null
     * @since 1.8
     */
    public boolean waitFor(long timeout, TimeUnit unit)
        throws InterruptedException
    {
        long startTime = System.nanoTime();
        long rem = unit.toNanos(timeout);


                        do {
            try {
                exitValue();
                return true;
            } catch(IllegalThreadStateException ex) {
                if (rem > 0)
                    Thread.sleep(
                        Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100));
            }
            rem = unit.toNanos(timeout) - (System.nanoTime() - startTime);
        } while (rem > 0);
        return false;
    }

    /**
     * 返回子进程的退出值。
     *
     * @return 由该 {@code Process} 对象表示的子进程的退出值。按惯例，值
     *         {@code 0} 表示正常终止。
     * @throws IllegalThreadStateException 如果由该 {@code Process} 对象表示的子进程
     *         尚未终止
     */
    public abstract int exitValue();

    /**
     * 终止子进程。由该 {@code Process} 对象表示的子进程是否被强制终止
     * 取决于实现。
     */
    public abstract void destroy();

    /**
     * 终止子进程。由该 {@code Process} 对象表示的子进程被强制终止。
     *
     * <p>此方法的默认实现调用 {@link #destroy}，因此可能不会强制终止进程。此类的具体实现
     * 强烈建议覆盖此方法以提供符合规范的实现。在 {@link ProcessBuilder#start} 和
     * {@link Runtime#exec} 返回的 {@code Process} 对象上调用此方法将强制终止进程。
     *
     * <p>注意：子进程可能不会立即终止。
     * 即，在调用 {@code destroyForcibly()} 后，{@code isAlive()} 可能会在短时间内返回 true。如果需要，
     * 可以将此方法与 {@code waitFor()} 链接使用。
     *
     * @return 代表要强制终止的子进程的 {@code Process} 对象。
     * @since 1.8
     */
    public Process destroyForcibly() {
        destroy();
        return this;
    }

    /**
     * 测试由该 {@code Process} 表示的子进程是否存活。
     *
     * @return 如果由该 {@code Process} 对象表示的子进程尚未终止，则返回 {@code true}。
     * @since 1.8
     */
    public boolean isAlive() {
        try {
            exitValue();
            return false;
        } catch(IllegalThreadStateException e) {
            return true;
        }
    }
}
