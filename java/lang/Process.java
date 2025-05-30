/*
 * Copyright (c) 1995, 2012, Oracle and/or its affiliates. All rights reserved.
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
import java.util.concurrent.TimeUnit;

/**
 * {@link ProcessBuilder#start()} 和
 * {@link Runtime#exec(String[],String[],File) Runtime.exec}
 * 方法创建一个本地进程并返回一个 {@code Process} 子类的实例，可以用来控制进程并获取有关进程的信息。类 {@code Process}
 * 提供了用于从进程读取输入、向进程写入输出、等待进程完成、检查进程的退出状态以及销毁（终止）进程的方法。
 *
 * <p>在某些本地平台上，创建进程的方法可能对特殊进程不起作用，例如本地窗口进程、守护进程、Microsoft
 * Windows 上的 Win16/DOS 进程或 shell 脚本。
 *
 * <p>默认情况下，创建的子进程没有自己的终端或控制台。所有标准 I/O（即 stdin、stdout、stderr）操作都将被重定向到父进程，
 * 可以通过使用方法 {@link #getOutputStream()},
 * {@link #getInputStream()} 和
 * {@link #getErrorStream()} 获取的流来访问这些操作。由于某些本地平台只为标准输入和输出流提供有限的缓冲区大小，
 * 未能及时写入输入流或读取输出流可能会导致子进程阻塞，甚至死锁。
 *
 * <p>如果需要，可以使用 {@link ProcessBuilder} 类的方法
 * <a href="ProcessBuilder.html#redirect-input">重定向子进程 I/O</a>。
 *
 * <p>当没有更多引用 {@code Process} 对象时，子进程不会被终止，而是继续异步执行。
 *
 * <p>没有要求 {@code Process} 对象表示的进程必须相对于拥有 {@code Process} 对象的 Java 进程异步或并发执行。
 *
 * <p>自 1.5 起，{@link ProcessBuilder#start()} 是创建 {@code Process} 的首选方法。
 *
 * @since   JDK1.0
 */
public abstract class Process {
    /**
     * 返回连接到子进程正常输入的输出流。写入此流的数据将被管道传输到由该 {@code Process} 对象表示的进程的标准输入。
     *
     * <p>如果子进程的标准输入已使用
     * {@link ProcessBuilder#redirectInput(Redirect)
     * ProcessBuilder.redirectInput}
     * 重定向，则此方法将返回一个
     * <a href="ProcessBuilder.html#redirect-input">空输出流</a>。
     *
     * <p>实现说明：返回的输出流最好进行缓冲。
     *
     * @return 连接到子进程正常输入的输出流
     */
    public abstract OutputStream getOutputStream();

    /**
     * 返回连接到子进程正常输出的输入流。此流从由该 {@code Process} 对象表示的进程的标准输出获取数据。
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
     * <p>实现说明：返回的输入流最好进行缓冲。
     *
     * @return 连接到子进程正常输出的输入流
     */
    public abstract InputStream getInputStream();

    /**
     * 返回连接到子进程错误输出的输入流。此流从由该 {@code Process} 对象表示的进程的错误输出获取数据。
     *
     * <p>如果子进程的标准错误已使用
     * {@link ProcessBuilder#redirectError(Redirect)
     * ProcessBuilder.redirectError} 或
     * {@link ProcessBuilder#redirectErrorStream(boolean)
     * ProcessBuilder.redirectErrorStream}
     * 重定向，则此方法将返回一个
     * <a href="ProcessBuilder.html#redirect-output">空输入流</a>。
     *
     * <p>实现说明：返回的输入流最好进行缓冲。
     *
     * @return 连接到子进程错误输出的输入流
     */
    public abstract InputStream getErrorStream();

    /**
     * 如果必要，使当前线程等待，直到由该 {@code Process} 对象表示的进程终止。如果子进程已经终止，此方法立即返回。
     * 如果子进程尚未终止，调用线程将被阻塞，直到子进程退出。
     *
     * @return 由该 {@code Process} 对象表示的子进程的退出值。按照惯例，值
     *         {@code 0} 表示正常终止。
     * @throws InterruptedException 如果当前线程在等待时被其他线程
     *         {@linkplain Thread#interrupt() 中断}，则等待结束并抛出
     *         {@link InterruptedException}。
     */
    public abstract int waitFor() throws InterruptedException;

    /**
     * 如果必要，使当前线程等待，直到由该 {@code Process} 对象表示的进程终止，或指定的等待时间过去。
     *
     * <p>如果子进程已经终止，则此方法立即返回值 {@code true}。如果进程尚未终止且超时值小于或等于零，则
     * 此方法立即返回值 {@code false}。
     *
     * <p>此方法的默认实现轮询 {@code exitValue} 以检查进程是否已终止。强烈建议此类的具体实现重写此方法以提供更高效的实现。
     *
     * @param timeout 最大等待时间
     * @param unit {@code timeout} 参数的时间单位
     * @return 如果子进程已退出则返回 {@code true}，如果等待时间过去而子进程尚未退出则返回 {@code false}。
     * @throws InterruptedException 如果当前线程在等待时被中断
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
     * @return 由该 {@code Process} 对象表示的子进程的退出值。按照惯例，值
     *         {@code 0} 表示正常终止。
     * @throws IllegalThreadStateException 如果由该 {@code Process} 对象表示的子进程尚未终止
     */
    public abstract int exitValue();

    /**
     * 终止子进程。由该 {@code Process} 对象表示的子进程是否被强制终止取决于实现。
     */
    public abstract void destroy();

    /**
     * 终止子进程。由该 {@code Process} 对象表示的子进程被强制终止。
     *
     * <p>此方法的默认实现调用 {@link #destroy}，因此可能不会强制终止进程。强烈建议此类的具体实现重写此方法以提供符合要求的实现。
     * 调用此方法返回的 {@code Process} 对象由 {@link ProcessBuilder#start} 和
     * {@link Runtime#exec} 创建的进程将被强制终止。
     *
     * <p>注意：子进程可能不会立即终止。
     * 即，调用 {@code destroyForcibly()} 后，{@code isAlive()} 可能在短时间内返回 true。如果需要，可以将此方法与 {@code waitFor()} 链接使用。
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
