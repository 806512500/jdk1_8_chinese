
/*
 * 版权所有 (c) 2001, 2013，Oracle 和/或其附属公司。保留所有权利。
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

/**
 * 定义通道，这些通道表示与能够执行 I/O 操作的实体（如文件和套接字）的连接；定义选择器，用于多路复用、非阻塞 I/O 操作。
 *
 * <a name="channels"></a>
 *
 * <blockquote><table cellspacing=1 cellpadding=0 summary="列出通道及其描述">
 * <tr><th align="left">通道</th><th align="left">描述</th></tr>
 * <tr><td valign=top><tt><i>{@link java.nio.channels.Channel}</i></tt></td>
 *     <td>I/O 操作的中心点</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;<i>{@link java.nio.channels.ReadableByteChannel}</i></tt></td>
 *     <td>可以读取到缓冲区</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;&nbsp;&nbsp;<i>{@link java.nio.channels.ScatteringByteChannel}&nbsp;&nbsp;</i></tt></td>
 *     <td>可以读取到一系列缓冲区</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;<i>{@link java.nio.channels.WritableByteChannel}</i></tt></td>
 *     <td>可以从缓冲区写入</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;&nbsp;&nbsp;<i>{@link java.nio.channels.GatheringByteChannel}</i></tt></td>
 *     <td>可以从一系列缓冲区写入</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;<i>{@link java.nio.channels.ByteChannel}</i></tt></td>
 *     <td>可以读取/写入到缓冲区</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;&nbsp;&nbsp;<i>{@link java.nio.channels.SeekableByteChannel}</i></tt></td>
 *     <td>一个连接到包含可变长度字节序列的实体的 {@code ByteChannel}</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;<i>{@link java.nio.channels.AsynchronousChannel}</i></tt></td>
 *     <td>支持异步 I/O 操作。</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;&nbsp;&nbsp;<i>{@link java.nio.channels.AsynchronousByteChannel}</i></tt></td>
 *     <td>可以异步读取和写入字节</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;<i>{@link java.nio.channels.NetworkChannel}</i></tt></td>
 *     <td>连接到网络套接字的通道</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;&nbsp;&nbsp;<i>{@link java.nio.channels.MulticastChannel}</i></tt></td>
 *     <td>可以加入 Internet 协议 (IP) 组播组</td></tr>
 * <tr><td valign=top><tt>{@link java.nio.channels.Channels}</tt></td>
 *     <td>用于通道/流互操作的实用方法</td></tr>
 * </table></blockquote>
 *
 * <p> 一个 <i>通道</i> 表示与实体（如硬件设备、文件、网络套接字或能够执行一个或多个不同 I/O 操作的程序组件）的开放连接，例如读取或写入。如 {@link java.nio.channels.Channel} 接口所指定，通道要么是打开的，要么是关闭的，并且它们都是 <i>异步可关闭的</i> 和 <i>可中断的</i>。
 *
 * <p> {@link java.nio.channels.Channel} 接口由几个其他接口扩展。
 *
 * <p> {@link java.nio.channels.ReadableByteChannel} 接口指定了一个 {@link java.nio.channels.ReadableByteChannel#read read} 方法，该方法从通道读取字节到缓冲区；类似地，{@link java.nio.channels.WritableByteChannel} 接口指定了一个 {@link java.nio.channels.WritableByteChannel#write write} 方法，该方法从缓冲区写入字节到通道。{@link java.nio.channels.ByteChannel} 接口统一了这两个接口，用于常见的可以读取和写入字节的通道。{@link java.nio.channels.SeekableByteChannel} 接口扩展了 {@code ByteChannel} 接口，添加了 {@link java.nio.channels.SeekableByteChannel#position() 查询} 和 {@link java.nio.channels.SeekableByteChannel#position(long) 修改} 通道当前位置的方法，以及其 {@link java.nio.channels.SeekableByteChannel#size 大小}。
 *
 * <p> {@link java.nio.channels.ScatteringByteChannel} 和 {@link java.nio.channels.GatheringByteChannel} 接口分别扩展了 {@link java.nio.channels.ReadableByteChannel} 和 {@link java.nio.channels.WritableByteChannel} 接口，添加了 {@link java.nio.channels.ScatteringByteChannel#read read} 和 {@link java.nio.channels.GatheringByteChannel#write write} 方法，这些方法接受一系列缓冲区而不是单个缓冲区。
 *
 * <p> {@link java.nio.channels.NetworkChannel} 接口指定了用于 {@link java.nio.channels.NetworkChannel#bind 绑定} 通道的套接字、获取套接字绑定的地址以及 {@link java.nio.channels.NetworkChannel#getOption 获取} 和 {@link java.nio.channels.NetworkChannel#setOption 设置} 套接字选项的方法。{@link java.nio.channels.MulticastChannel} 接口指定了用于加入 Internet 协议 (IP) 组播组的方法。
 *
 * <p> {@link java.nio.channels.Channels} 实用类定义了支持 <tt>{@link java.io}</tt> 包中的流类与本包中的通道类互操作的静态方法。可以从 {@link java.io.InputStream} 或 {@link java.io.OutputStream} 构造适当的通道，反之亦然，可以从通道构造 {@link java.io.InputStream} 或 {@link java.io.OutputStream}。可以构造一个使用给定字符集从给定的可读字节通道解码字节的 {@link java.io.Reader}，反之亦然，可以构造一个使用给定字符集将字符编码为字节并写入给定的可写字节通道的 {@link java.io.Writer}。
 *
 * <blockquote><table cellspacing=1 cellpadding=0 summary="列出文件通道及其描述">
 * <tr><th align="left">文件通道</th><th align="left">描述</th></tr>
 * <tr><td valign=top><tt>{@link java.nio.channels.FileChannel}</tt></td>
 *     <td>读取、写入、映射和操作文件</td></tr>
 * <tr><td valign=top><tt>{@link java.nio.channels.FileLock}</tt></td>
 *     <td>对（文件区域的）文件锁</td></tr>
 * <tr><td valign=top><tt>{@link java.nio.MappedByteBuffer}&nbsp;&nbsp;</tt></td>
 *     <td>映射到文件区域的直接字节缓冲区</td></tr>
 * </table></blockquote>
 *
 * <p> {@link java.nio.channels.FileChannel} 类支持从连接到文件的通道读取字节和写入字节的常规操作，以及查询和修改当前文件位置和将文件截断为特定大小的操作。它定义了获取整个文件或文件特定区域锁的方法；这些方法返回 {@link java.nio.channels.FileLock} 类的实例。最后，它定义了强制更新文件写入存储设备、在文件和其他通道之间高效传输字节以及将文件区域直接映射到内存的方法。
 *
 * <p> 通过调用其静态 {@link java.nio.channels.FileChannel#open open} 方法之一，或通过调用 {@link java.io.FileInputStream}、{@link java.io.FileOutputStream} 或 {@link java.io.RandomAccessFile} 的 {@code getChannel} 方法，可以创建一个 {@code FileChannel}，该通道连接到与 <tt>{@link java.io}</tt> 类相同的底层文件。
 *
 * <a name="multiplex"></a>
 * <blockquote><table cellspacing=1 cellpadding=0 summary="列出多路复用、非阻塞通道及其描述">
 * <tr><th align="left">多路复用、非阻塞 I/O</th><th align="left"><p>描述</th></tr>
 * <tr><td valign=top><tt>{@link java.nio.channels.SelectableChannel}</tt></td>
 *     <td>可以多路复用的通道</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;{@link java.nio.channels.DatagramChannel}</tt></td>
 *     <td>连接到数据报套接字的通道</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;{@link java.nio.channels.Pipe.SinkChannel}</tt></td>
 *     <td>管道的写端</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;{@link java.nio.channels.Pipe.SourceChannel}</tt></td>
 *     <td>管道的读端</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;{@link java.nio.channels.ServerSocketChannel}&nbsp;&nbsp;</tt></td>
 *     <td>连接到流导向监听套接字的通道</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;{@link java.nio.channels.SocketChannel}</tt></td>
 *     <td>连接到流导向连接套接字的通道</td></tr>
 * <tr><td valign=top><tt>{@link java.nio.channels.Selector}</tt></td>
 *     <td>可选择通道的多路复用器</td></tr>
 * <tr><td valign=top><tt>{@link java.nio.channels.SelectionKey}</tt></td>
 *     <td>表示通道注册到选择器的令牌</td></tr>
 * <tr><td valign=top><tt>{@link java.nio.channels.Pipe}</tt></td>
 *     <td>形成单向管道的两个通道</td></tr>
 * </table></blockquote>
 *
 * <p> 多路复用、非阻塞 I/O 比线程导向、阻塞 I/O 更具可扩展性，由 <i>选择器</i>、<i>可选择通道</i> 和 <i>选择键</i> 提供支持。
 *
 * <p> 一个 <a href="Selector.html"><i>选择器</i></a> 是 <a href="SelectableChannel.html"><i>可选择通道</i></a> 的多路复用器，后者是一种可以置于 <a href="SelectableChannel.html#bm"><i>非阻塞模式</i></a> 的特殊类型的通道。为了执行多路复用 I/O 操作，首先创建一个或多个可选择通道，将其置于非阻塞模式，并 {@link java.nio.channels.SelectableChannel#register <i>注册</i>} 到选择器。注册通道指定了选择器将测试其准备情况的 I/O 操作集，并返回一个 <a href="SelectionKey.html"><i>选择键</i></a>，表示注册。
 *
 * <p> 一旦一些通道注册到选择器，可以执行 <a href="Selector.html#selop"><i>选择操作</i></a> 以发现哪些通道（如果有）已经准备好执行之前声明感兴趣的其中一个或多个操作。如果通道已准备好，则注册时返回的键将被添加到选择器的 <i>已选择键集</i>。可以检查键集及其内部的键，以确定每个通道已准备好执行的操作。可以从每个键检索相应的通道，以执行所需的 I/O 操作。
 *
 * <p> 选择键指示其通道已准备好进行某些操作是一个提示，但不是保证，线程可以执行该操作而不会阻塞。编写执行多路复用 I/O 的代码时，必须忽略这些不正确的提示。
 *
 * <p> 本包定义了与 <tt>{@link java.net}</tt> 包中定义的 {@link java.net.DatagramSocket}、{@link java.net.ServerSocket} 和 {@link java.net.Socket} 类对应的可选择通道类。对这些类进行了少量修改，以支持与通道关联的套接字。本包还定义了一个实现单向管道的简单类。在所有情况下，通过调用相应类的静态 <tt>open</tt> 方法创建新的可选择通道。如果通道需要关联的套接字，则作为此操作的副作用将创建一个套接字。
 *
 * <p> 通过“插入” <tt>{@link java.nio.channels.spi}</tt> 包中定义的 {@link java.nio.channels.spi.SelectorProvider} 类的替代定义或实例，可以选择器、可选择通道和选择键的实现可以被替换。不期望许多开发人员实际使用此功能；它主要提供给需要在非常高的性能要求下利用操作系统特定的 I/O 多路复用机制的高级用户。
 *
 * <p> 本包中的 {@link java.nio.channels.spi.AbstractInterruptibleChannel}、{@link java.nio.channels.spi.AbstractSelectableChannel}、{@link java.nio.channels.spi.AbstractSelectionKey} 和 {@link java.nio.channels.spi.AbstractSelector} 类执行实现多路复用 I/O 抽象所需的大部分簿记和同步。定义自定义选择器提供程序时，应仅直接扩展 {@link java.nio.channels.spi.AbstractSelector} 和 {@link java.nio.channels.spi.AbstractSelectionKey} 类；自定义通道类应扩展本包中定义的适当 {@link java.nio.channels.SelectableChannel} 子类。
 *
 * <a name="async"></a>
 *
 * <blockquote><table cellspacing=1 cellpadding=0 summary="列出异步通道及其描述">
 * <tr><th align="left">异步 I/O</th><th align="left">描述</th></tr>
 * <tr><td valign=top><tt>{@link java.nio.channels.AsynchronousFileChannel}</tt></td>
 *     <td>用于读取、写入和操作文件的异步通道</td></tr>
 * <tr><td valign=top><tt>{@link java.nio.channels.AsynchronousSocketChannel}</tt></td>
 *     <td>连接到流导向连接套接字的异步通道</td></tr>
 * <tr><td valign=top><tt>{@link java.nio.channels.AsynchronousServerSocketChannel}&nbsp;&nbsp;</tt></td>
 *     <td>连接到流导向监听套接字的异步通道</td></tr>
 * <tr><td valign=top><tt>{@link java.nio.channels.CompletionHandler}</tt></td>
 *     <td>用于消费异步操作结果的处理器</td></tr>
 * <tr><td valign=top><tt>{@link java.nio.channels.AsynchronousChannelGroup}</tt></td>
 *     <td>用于资源共享的异步通道组</td></tr>
 * </table></blockquote>
 *
 * <p> {@link java.nio.channels.AsynchronousChannel 异步通道} 是一种能够执行异步 I/O 操作的特殊类型的通道。异步通道是非阻塞的，并定义了方法来启动异步操作，返回一个 {@link java.util.concurrent.Future} 表示每个操作的待处理结果。可以使用 {@code Future} 来轮询或等待操作结果。异步 I/O 操作还可以指定一个 {@link java.nio.channels.CompletionHandler}，在操作完成时调用。完成处理器是用户提供的代码，用于消费 I/O 操作的结果。
 *
 * <p> 本包定义了连接到流导向连接或监听套接字，或数据报导向套接字的异步通道类。它还定义了用于异步读取、写入和操作文件的 {@link java.nio.channels.AsynchronousFileChannel} 类。与 {@link java.nio.channels.FileChannel} 一样，它支持将文件截断为特定大小、强制更新文件写入存储设备或获取整个文件或文件特定区域的锁的操作。与 {@code FileChannel} 不同，它不定义将文件区域直接映射到内存的方法。如果需要内存映射 I/O，则可以使用 {@code FileChannel}。
 *
 * <p> 异步通道绑定到异步通道组，用于资源共享。组有一个关联的 {@link java.util.concurrent.ExecutorService}，任务将提交到该服务以处理 I/O 事件并调度到消费异步操作结果的完成处理器。组可以可选地在创建通道时指定，或者通道可以绑定到 <em>默认组</em>。高级用户可能希望创建自己的异步通道组或配置将用于默认组的 {@code ExecutorService}。
 *
 * <p> 与选择器一样，可以通过“插入” <tt>{@link java.nio.channels.spi}</tt> 包中定义的 {@link java.nio.channels.spi.AsynchronousChannelProvider} 类的替代定义或实例来替换异步通道的实现。不期望许多开发人员实际使用此功能；它主要提供给需要在非常高的性能要求下利用操作系统特定的异步 I/O 机制的高级用户。
 *
 * <hr width="80%">
 * <p> 除非另有说明，否则将 <tt>null</tt> 参数传递给本包中任何类或接口的构造函数或方法将导致抛出 {@link java.lang.NullPointerException NullPointerException}。
 *
 * @since 1.4
 * @author Mark Reinhold
 * @author JSR-51 专家组
 */

package java.nio.channels;
