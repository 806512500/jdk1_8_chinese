
/*
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.channels;

import java.nio.channels.spi.AsynchronousChannelProvider;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 一组异步通道，用于资源共享。
 *
 * <p> 异步通道组封装了处理由 {@link AsynchronousChannel 异步通道} 发起的 I/O 操作完成所需的机制，这些通道绑定到该组。组有一个关联的线程池，任务被提交到该线程池以处理 I/O 事件并调度到 {@link CompletionHandler 完成处理器}，这些处理器消费在组中通道上执行的异步操作的结果。除了处理 I/O 事件外，池中的线程还可以执行支持异步 I/O 操作执行所需的其他任务。
 *
 * <p> 通过调用此处定义的 {@link #withFixedThreadPool withFixedThreadPool} 或 {@link #withCachedThreadPool withCachedThreadPool} 方法创建异步通道组。通过在构造通道时指定组，通道绑定到该组。关联的线程池由组 <em>拥有</em>；组的终止导致关联线程池的关闭。
 *
 * <p> 除了显式创建的组外，Java 虚拟机还维护一个系统范围的 <em>默认组</em>，该组自动构建。在构造时未指定组的异步通道将绑定到默认组。默认组有一个关联的线程池，根据需要创建新线程。可以通过下表中定义的系统属性配置默认组。如果未配置默认组的 {@link java.util.concurrent.ThreadFactory ThreadFactory}，则默认组的池线程为 {@link Thread#isDaemon 守护线程}。
 *
 * <table border summary="System properties">
 *   <tr>
 *     <th>系统属性</th>
 *     <th>描述</th>
 *   </tr>
 *   <tr>
 *     <td> {@code java.nio.channels.DefaultThreadPool.threadFactory} </td>
 *     <td> 该属性的值被视为一个具体的 {@link java.util.concurrent.ThreadFactory ThreadFactory} 类的全限定名。使用系统类加载器加载该类并实例化。调用工厂的 {@link java.util.concurrent.ThreadFactory#newThread newThread} 方法为默认组的线程池创建每个线程。如果加载和实例化该属性值的过程失败，则在构造默认组时会抛出未指定的错误。 </td>
 *   </tr>
 *   <tr>
 *     <td> {@code java.nio.channels.DefaultThreadPool.initialSize} </td>
 *     <td> 该属性的值被视为 {@code String} 表示的 {@code Integer}，作为默认组的初始大小参数（参见 {@link #withCachedThreadPool withCachedThreadPool}）。如果该值不能解析为 {@code Integer}，则在构造默认组时会抛出未指定的错误。 </td>
 *   </tr>
 * </table>
 *
 * <a name="threading"></a><h2>线程</h2>
 *
 * <p> 为绑定到组的通道发起的 I/O 操作的完成处理器保证由组中的一个池线程调用。这确保了完成处理器由具有预期 <em>身份</em> 的线程运行。
 *
 * <p> 如果 I/O 操作立即完成，且发起线程是组中的一个池线程，则完成处理器可能由发起线程直接调用。为了避免堆栈溢出，实现可能会限制线程堆栈上的激活次数。某些 I/O 操作可能禁止由发起线程直接调用完成处理器（参见 {@link AsynchronousServerSocketChannel#accept(Object,CompletionHandler) accept}）。
 *
 * <a name="shutdown"></a><h2>关闭和终止</h2>
 *
 * <p> {@link #shutdown() shutdown} 方法用于启动组的 <em>有序关闭</em>。有序关闭将组标记为已关闭；进一步尝试构造绑定到该组的通道将抛出 {@link ShutdownChannelGroupException}。可以使用 {@link #isShutdown() isShutdown} 方法测试组是否已关闭。关闭后，当组中所有异步通道关闭、所有正在执行的完成处理器运行完成且组使用的资源已释放时，组 <em>终止</em>。不会尝试停止或中断正在执行完成处理器的线程。可以使用 {@link #isTerminated() isTerminated} 方法测试组是否已终止，可以使用 {@link #awaitTermination awaitTermination} 方法阻塞直到组终止。
 *
 * <p> 可以使用 {@link #shutdownNow() shutdownNow} 方法启动组的 <em>强制关闭</em>。除了有序关闭执行的操作外，{@code shutdownNow} 方法还会关闭组中所有打开的通道，就像调用了 {@link AsynchronousChannel#close close} 方法一样。
 *
 * @since 1.7
 *
 * @see AsynchronousSocketChannel#open(AsynchronousChannelGroup)
 * @see AsynchronousServerSocketChannel#open(AsynchronousChannelGroup)
 */

public abstract class AsynchronousChannelGroup {
    private final AsynchronousChannelProvider provider;

    /**
     * 初始化此类的新实例。
     *
     * @param   provider
     *          该组的异步通道提供者
     */
    protected AsynchronousChannelGroup(AsynchronousChannelProvider provider) {
        this.provider = provider;
    }

    /**
     * 返回创建此通道组的提供者。
     *
     * @return  创建此通道组的提供者
     */
    public final AsynchronousChannelProvider provider() {
        return provider;
    }

    /**
     * 创建具有固定线程池的异步通道组。
     *
     * <p> 结果异步通道组重用固定数量的线程。在任何时间点，最多 {@code nThreads} 个线程将处于活动状态，处理提交以处理 I/O 事件和调度操作完成结果的任务，这些操作是在组中的异步通道上发起的。
     *
     * <p> 通过调用系统范围的默认 {@link AsynchronousChannelProvider} 对象的 {@link AsynchronousChannelProvider#openAsynchronousChannelGroup(int,ThreadFactory) openAsynchronousChannelGroup(int,ThreadFactory)} 方法创建组。
     *
     * @param   nThreads
     *          线程池中的线程数
     * @param   threadFactory
     *          创建新线程时使用的工厂
     *
     * @return  新的异步通道组
     *
     * @throws  IllegalArgumentException
     *          如果 {@code nThreads <= 0}
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public static AsynchronousChannelGroup withFixedThreadPool(int nThreads,
                                                               ThreadFactory threadFactory)
        throws IOException
    {
        return AsynchronousChannelProvider.provider()
            .openAsynchronousChannelGroup(nThreads, threadFactory);
    }

    /**
     * 创建具有给定线程池的异步通道组，该线程池根据需要创建新线程。
     *
     * <p> {@code executor} 参数是一个 {@code ExecutorService}，根据需要创建新线程以执行处理 I/O 事件和调度操作完成结果的任务，这些操作是在组中的异步通道上发起的。如果可用，它可以重用先前构造的线程。
     *
     * <p> {@code initialSize} 参数可以用作实现的 <em>提示</em>，指示它可能提交的初始任务数。例如，它可以用于指示等待 I/O 事件的初始线程数。
     *
     * <p> 执行器服务旨在由结果异步通道组独占使用。组的终止导致执行器服务的有序 {@link ExecutorService#shutdown shutdown}。通过其他方式关闭执行器服务会导致未指定的行为。
     *
     * <p> 通过调用系统范围的默认 {@link AsynchronousChannelProvider} 对象的 {@link AsynchronousChannelProvider#openAsynchronousChannelGroup(ExecutorService,int) openAsynchronousChannelGroup(ExecutorService,int)} 方法创建组。
     *
     * @param   executor
     *          结果组的线程池
     * @param   initialSize
     *          值 {@code >=0} 或负值表示实现特定的默认值
     *
     * @return  新的异步通道组
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @see java.util.concurrent.Executors#newCachedThreadPool
     */
    public static AsynchronousChannelGroup withCachedThreadPool(ExecutorService executor,
                                                                int initialSize)
        throws IOException
    {
        return AsynchronousChannelProvider.provider()
            .openAsynchronousChannelGroup(executor, initialSize);
    }

    /**
     * 创建具有给定线程池的异步通道组。
     *
     * <p> {@code executor} 参数是一个 {@code ExecutorService}，执行提交以调度操作完成结果的任务，这些操作是在组中的异步通道上发起的。
     *
     * <p> 在配置执行器服务时应谨慎。它应支持 <em>直接传递</em> 或 <em>无界排队</em> 提交的任务，且调用 {@link ExecutorService#execute execute} 方法的线程不应直接调用任务。实现可能要求额外的约束。
     *
     * <p> 执行器服务旨在由结果异步通道组独占使用。组的终止导致执行器服务的有序 {@link ExecutorService#shutdown shutdown}。通过其他方式关闭执行器服务会导致未指定的行为。
     *
     * <p> 通过调用系统范围的默认 {@link AsynchronousChannelProvider} 对象的 {@link AsynchronousChannelProvider#openAsynchronousChannelGroup(ExecutorService,int) openAsynchronousChannelGroup(ExecutorService,int)} 方法创建组，初始大小为 {@code 0}。
     *
     * @param   executor
     *          结果组的线程池
     *
     * @return  新的异步通道组
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public static AsynchronousChannelGroup withThreadPool(ExecutorService executor)
        throws IOException
    {
        return AsynchronousChannelProvider.provider()
            .openAsynchronousChannelGroup(executor, 0);
    }

    /**
     * 告知此异步通道组是否已关闭。
     *
     * @return  如果此异步通道组已关闭或已被标记为关闭，则返回 {@code true}
     */
    public abstract boolean isShutdown();

    /**
     * 告知此组是否已终止。
     *
     * <p> 如果此方法返回 {@code true}，则关联的线程池也已 {@link ExecutorService#isTerminated 终止}。
     *
     * @return  如果此组已终止，则返回 {@code true}
     */
    public abstract boolean isTerminated();

    /**
     * 启动组的有序关闭。
     *
     * <p> 此方法将组标记为已关闭。进一步尝试构造绑定到此组的通道将抛出 {@link ShutdownChannelGroupException}。当组中的所有异步通道关闭、所有正在执行的完成处理器运行完成且所有资源已释放时，组终止。如果组已关闭，此方法无效。
     */
    public abstract void shutdown();

    /**
     * 关闭组并关闭组中所有打开的通道。
     *
     * <p> 除了执行 {@link #shutdown() shutdown} 方法执行的操作外，此方法还调用组中所有打开的通道的 {@link AsynchronousChannel#close close} 方法。此方法不会尝试停止或中断正在执行完成处理器的线程。当所有正在执行的完成处理器运行完成且所有资源已释放时，组终止。此方法可以在任何时间调用。如果其他线程已经调用它，则另一个调用将阻塞直到第一次调用完成，之后它将返回且无效。
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public abstract void shutdownNow() throws IOException;

    /**
     * 等待组的终止。
     *
     * <p> 此方法阻塞直到组终止，或超时发生，或当前线程被中断，以先发生者为准。
     *
     * @param   timeout
     *          最大等待时间，或零或更小表示不等待
     * @param   unit
     *          超时参数的时间单位
     *
     * @return  如果组已终止，则返回 {@code true}；如果超时发生前未终止，则返回 {@code false}
     *
     * @throws  InterruptedException
     *          如果在等待时被中断
     */
    public abstract boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException;
}
