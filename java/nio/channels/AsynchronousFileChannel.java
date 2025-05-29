
/*
 * 版权所有 (c) 2007, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.nio.channels;

import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.spi.*;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * 用于读取、写入和操作文件的异步通道。
 *
 * <p> 当文件通过调用此类定义的 {@link #open open} 方法之一打开时，会创建一个异步文件通道。文件包含一个可读写的可变长度的字节序列，其当前大小可以 {@link #size() 查询}。当在文件当前大小之外写入字节时，文件的大小会增加；当文件被 {@link #truncate 截断} 时，文件的大小会减少。
 *
 * <p> 异步文件通道没有文件内的 <i>当前位置</i>。相反，文件位置在每次启动异步操作的读取和写入方法中指定。作为参数指定了一个 {@link CompletionHandler}，用于处理 I/O 操作的结果。此类还定义了启动异步操作的读取和写入方法，返回一个 {@link Future} 以表示操作的待处理结果。可以使用 {@code Future} 检查操作是否已完成、等待其完成并获取结果。
 *
 * <p> 除了读取和写入操作外，此类还定义了以下操作： </p>
 *
 * <ul>
 *
 *   <li><p> 对文件所做的更新可以 {@link #force <i>强制</i>} 到底层存储设备，确保在系统崩溃时数据不会丢失。  </p></li>
 *
 *   <li><p> 可以 {@link #lock <i>锁定</i>} 文件的一个区域，以防止其他程序访问。  </p></li>
 *
 * </ul>
 *
 * <p> 一个 {@code AsynchronousFileChannel} 与一个线程池关联，任务被提交到该线程池以处理 I/O 事件并调度完成处理程序来消费通道上 I/O 操作的结果。为通道启动的 I/O 操作的完成处理程序保证由线程池中的一个线程调用（这确保了完成处理程序由具有预期 <em>身份</em> 的线程运行）。如果 I/O 操作立即完成，并且启动线程本身是线程池中的一个线程，那么完成处理程序可能由启动线程直接调用。当创建 {@code AsynchronousFileChannel} 时未指定线程池，则通道与一个系统依赖的默认线程池关联，该线程池可能与其他通道共享。默认线程池由 {@link AsynchronousChannelGroup} 类定义的系统属性配置。
 *
 * <p> 此类型的通道可以安全地由多个并发线程使用。如 {@link Channel} 接口所指定，可以随时调用 {@link Channel#close close} 方法。这会导致通道上所有未完成的异步操作以 {@link AsynchronousCloseException} 异常完成。可以同时有多个读取和写入操作挂起。当有多个读取和写入操作挂起时，I/O 操作的顺序以及完成处理程序被调用的顺序未指定；特别是，它们不保证按操作启动的顺序执行。用于读取或写入的 {@link java.nio.ByteBuffer ByteBuffers} 不安全用于多个并发 I/O 操作。此外，在启动 I/O 操作后，应小心确保在操作完成之前不访问缓冲区。
 *
 * <p> 与 {@link FileChannel} 一样，此类的实例提供的文件视图保证与其他程序中其他实例提供的同一文件的视图一致。然而，此类的实例提供的视图可能与其他并发运行的程序的视图不一致，这是由于底层操作系统执行的缓存以及网络文件系统协议引起的延迟。无论这些其他程序是用什么语言编写的，无论它们是在同一台机器上还是在其他机器上运行，这一点都是如此。任何此类不一致的具体性质都是系统依赖的，因此未指定。
 *
 * @since 1.7
 */

public abstract class AsynchronousFileChannel
    implements AsynchronousChannel
{
    /**
     * 初始化此类的新实例。
     */
    protected AsynchronousFileChannel() {
    }

    /**
     * 打开或创建一个文件以供读取和/或写入，返回一个异步文件通道以访问该文件。
     *
     * <p> {@code options} 参数确定如何打开文件。{@link StandardOpenOption#READ READ} 和 {@link StandardOpenOption#WRITE WRITE} 选项确定文件是否应打开以供读取和/或写入。如果数组中不包含这两个选项，则打开现有文件以供读取。
     *
     * <p> 除了 {@code READ} 和 {@code WRITE} 之外，还可能包含以下选项：
     *
     * <table border=1 cellpadding=5 summary="">
     * <tr> <th>选项</th> <th>描述</th> </tr>
     * <tr>
     *   <td> {@link StandardOpenOption#TRUNCATE_EXISTING TRUNCATE_EXISTING} </td>
     *   <td> 当打开现有文件时，文件首先被截断为 0 字节大小。如果文件仅打开以供读取，则忽略此选项。</td>
     * </tr>
     * <tr>
     *   <td> {@link StandardOpenOption#CREATE_NEW CREATE_NEW} </td>
     *   <td> 如果此选项存在，则创建一个新文件，如果文件已存在则失败。创建文件时，检查文件是否存在以及如果文件不存在则创建文件的操作相对于其他文件系统操作是原子的。如果文件仅打开以供读取，则忽略此选项。 </td>
     * </tr>
     * <tr>
     *   <td > {@link StandardOpenOption#CREATE CREATE} </td>
     *   <td> 如果此选项存在，则如果文件存在则打开现有文件，否则创建新文件。创建文件时，检查文件是否存在以及如果文件不存在则创建文件的操作相对于其他文件系统操作是原子的。如果同时存在 {@code CREATE_NEW} 选项或文件仅打开以供读取，则忽略此选项。 </td>
     * </tr>
     * <tr>
     *   <td > {@link StandardOpenOption#DELETE_ON_CLOSE DELETE_ON_CLOSE} </td>
     *   <td> 当此选项存在时，实现将 <em>尽力</em> 在调用 {@link #close close} 方法时删除文件。如果未调用 {@code close} 方法，则在 Java 虚拟机终止时 <em>尽力</em> 删除文件。 </td>
     * </tr>
     * <tr>
     *   <td>{@link StandardOpenOption#SPARSE SPARSE} </td>
     *   <td> 创建新文件时，此选项是一个 <em>提示</em>，表示新文件将是稀疏的。如果不是创建新文件，则忽略此选项。 </td>
     * </tr>
     * <tr>
     *   <td> {@link StandardOpenOption#SYNC SYNC} </td>
     *   <td> 要求文件内容或元数据的每次更新都同步写入底层存储设备。（参见 <a href="../file/package-summary.html#integrity">同步 I/O 文件完整性</a>）。 </td>
     * </tr>
     * <tr>
     *   <td> {@link StandardOpenOption#DSYNC DSYNC} </td>
     *   <td> 要求文件内容的每次更新都同步写入底层存储设备。（参见 <a href="../file/package-summary.html#integrity">同步 I/O 文件完整性</a>）。 </td>
     * </tr>
     * </table>
     *
     * <p> 实现也可能支持其他选项。
     *
     * <p> {@code executor} 参数是 {@link ExecutorService}，任务被提交到该服务以处理 I/O 事件并调度完成结果。这些任务的性质高度依赖于具体实现，因此在配置 {@code Executor} 时应小心。最小要求是它应支持无界工作队列，并且不应在 {@link ExecutorService#execute execute} 方法的调用线程上运行任务。在通道打开时关闭执行器服务会导致未指定的行为。
     *
     * <p> {@code attrs} 参数是可选的文件 {@link FileAttribute 文件属性} 数组，用于在创建文件时原子地设置。
     *
     * <p> 新通道是通过调用创建 {@code Path} 的提供者的 {@link FileSystemProvider#newFileChannel newFileChannel} 方法创建的。
     *
     * @param   file
     *          要打开或创建的文件的路径
     * @param   options
     *          指定如何打开文件的选项
     * @param   executor
     *          线程池或 {@code null} 以将通道与默认线程池关联
     * @param   attrs
     *          创建文件时原子地设置的可选文件属性列表
     *
     * @return  一个新的异步文件通道
     *
     * @throws  IllegalArgumentException
     *          如果集合包含无效的选项组合
     * @throws  UnsupportedOperationException
     *          如果 {@code file} 与不支持创建异步文件通道的提供者关联，或者指定了不支持的打开选项，或者数组包含在创建文件时无法原子地设置的属性
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          如果安装了安全管理器并且它拒绝实现所需的未指定权限。对于默认提供者，如果文件打开以供读取，则调用 {@link SecurityManager#checkRead(String)} 方法检查读取权限。如果文件打开以供写入，则调用 {@link SecurityManager#checkWrite(String)} 方法检查写入权限
     */
    public static AsynchronousFileChannel open(Path file,
                                               Set<? extends OpenOption> options,
                                               ExecutorService executor,
                                               FileAttribute<?>... attrs)
        throws IOException
    {
        FileSystemProvider provider = file.getFileSystem().provider();
        return provider.newAsynchronousFileChannel(file, options, executor, attrs);
    }


@SuppressWarnings({"unchecked", "rawtypes"}) // 泛型数组构造
private static final FileAttribute<?>[] NO_ATTRIBUTES = new FileAttribute[0];

/**
 * 打开或创建一个文件以供读取和/或写入，返回一个异步文件通道以访问该文件。
 *
 * <p> 本方法的调用行为与以下调用完全相同：
 * <pre>
 *     ch.{@link #open(Path,Set,ExecutorService,FileAttribute[])
 *       open}(file, opts, null, new FileAttribute&lt;?&gt;[0]);
 * </pre>
 * 其中 {@code opts} 是一个包含传递给此方法的选项的 {@code Set}。
 *
 * <p> 生成的通道与默认线程池关联，任务提交到该线程池以处理 I/O 事件并调度到完成处理器，这些处理器消费在生成的通道上执行的异步操作的结果。
 *
 * @param   file
 *          要打开或创建的文件的路径
 * @param   options
 *          指定文件如何打开的选项
 *
 * @return  一个新的异步文件通道
 *
 * @throws  IllegalArgumentException
 *          如果选项集中包含无效的选项组合
 * @throws  UnsupportedOperationException
 *          如果 {@code file} 与不支持创建文件通道的提供程序关联，或者指定了不支持的打开选项
 * @throws  IOException
 *          如果发生 I/O 错误
 * @throws  SecurityException
 *          如果安装了安全经理并且它拒绝了实现所需的未指定权限。
 *          在默认提供程序的情况下，如果文件以读取方式打开，则调用 {@link
 *          SecurityManager#checkRead(String)} 方法检查读取权限。如果文件以写入方式打开，则调用 {@link
 *          SecurityManager#checkWrite(String)} 方法检查写入权限
 */
public static AsynchronousFileChannel open(Path file, OpenOption... options)
    throws IOException
{
    Set<OpenOption> set = new HashSet<OpenOption>(options.length);
    Collections.addAll(set, options);
    return open(file, set, null, NO_ATTRIBUTES);
}

/**
 * 返回此通道文件的当前大小。
 *
 * @return  此通道文件的当前大小，以字节为单位
 *
 * @throws  ClosedChannelException
 *          如果此通道已关闭
 * @throws  IOException
 *          如果发生其他 I/O 错误
 */
public abstract long size() throws IOException;

/**
 * 将此通道文件截断到给定的大小。
 *
 * <p> 如果给定的大小小于文件的当前大小，则文件将被截断，丢弃新文件末尾之后的任何字节。如果给定的大小大于或等于文件的当前大小，则文件不会被修改。 </p>
 *
 * @param  size
 *         新的大小，一个非负字节数
 *
 * @return  此文件通道
 *
 * @throws  NonWritableChannelException
 *          如果此通道未以写入方式打开
 *
 * @throws  ClosedChannelException
 *          如果此通道已关闭
 *
 * @throws  IllegalArgumentException
 *          如果新的大小为负数
 *
 * @throws  IOException
 *          如果发生其他 I/O 错误
 */
public abstract AsynchronousFileChannel truncate(long size) throws IOException;

/**
 * 强制将对此通道文件的任何更新写入包含它的存储设备。
 *
 * <p> 如果此通道的文件位于本地存储设备上，则当此方法返回时，可以保证自通道创建以来或自上次调用此方法以来对文件的所有更改都将被写入该设备。这对于确保在系统崩溃时不会丢失关键信息非常有用。
 *
 * <p> 如果文件不位于本地设备上，则不作此保证。
 *
 * <p> {@code metaData} 参数可用于限制此方法需要执行的 I/O 操作数量。传递 {@code false} 表示只需将文件内容的更新写入存储；传递 {@code true} 表示必须将文件内容和元数据的更新写入存储，这通常需要至少多一次 I/O 操作。此参数是否实际生效取决于底层操作系统，因此未指定。
 *
 * <p> 调用此方法可能会导致 I/O 操作发生，即使通道仅以读取方式打开。例如，某些操作系统会维护文件元数据中的上次访问时间，每次读取文件时都会更新该时间。是否实际执行此操作取决于系统，因此未指定。
 *
 * <p> 本方法仅保证通过此类定义的方法对文件所做的更改被强制写入。
 *
 * @param   metaData
 *          如果为 {@code true}，则此方法必须强制将文件内容和元数据的更改写入存储；否则，只需将内容更改写入
 *
 * @throws  ClosedChannelException
 *          如果此通道已关闭
 *
 * @throws  IOException
 *          如果发生其他 I/O 错误
 */
public abstract void force(boolean metaData) throws IOException;

/**
 * 获取此通道文件给定区域的锁。
 *
 * <p> 本方法启动一个操作，以获取此通道文件给定区域的锁。{@code handler} 参数是一个完成处理器，当锁被获取（或操作失败）时调用。传递给完成处理器的结果是生成的 {@code FileLock}。
 *
 * <p> 由 {@code position} 和 {@code size} 参数指定的区域不必包含在实际的底层文件中，甚至不必重叠。锁定区域的大小是固定的；如果锁定区域最初包含文件的末尾，而文件增长超出该区域，则文件的新部分将不受锁的保护。如果预期文件会增长并且需要整个文件的锁，则应锁定一个从零开始且不小于文件预期最大大小的区域。两参数的 {@link #lock(Object,CompletionHandler)} 方法简单地锁定一个大小为 {@link Long#MAX_VALUE} 的区域。如果此 Java 虚拟机已经持有与请求区域重叠的锁，或者此方法已被调用以锁定重叠区域且该操作尚未完成，则此方法将抛出 {@link OverlappingFileLockException}。
 *
 * <p> 有些操作系统不支持异步方式获取文件锁的机制。因此，实现可能在后台线程中或由关联线程池中的线程执行的任务中获取文件锁。如果有许多锁操作未完成，则可能会在 Java 虚拟机中消耗线程无限期地。
 *
 * <p> 有些操作系统不支持共享锁，因此请求共享锁会自动转换为请求独占锁。可以通过调用生成的锁对象的 {@link FileLock#isShared() isShared} 方法来测试新获取的锁是共享锁还是独占锁。
 *
 * <p> 文件锁代表整个 Java 虚拟机持有。它们不适合控制同一虚拟机内多个线程对文件的访问。
 *
 * @param   <A>
 *          附件的类型
 * @param   position
 *          锁定区域开始的位置；必须是非负数
 * @param   size
 *          锁定区域的大小；必须是非负数，且 {@code position}&nbsp;+&nbsp;{@code size} 的和必须是非负数
 * @param   shared
 *          如果为 {@code true}，则请求共享锁，此时此通道必须以读取方式打开（可能还以写入方式打开）；
 *          如果为 {@code false}，则请求独占锁，此时此通道必须以写入方式打开（可能还以读取方式打开）
 * @param   attachment
 *          附加到 I/O 操作的对象；可以为 {@code null}
 * @param   handler
 *          用于消费结果的处理器
 *
 * @throws  OverlappingFileLockException
 *          如果此 Java 虚拟机已经持有与请求区域重叠的锁，或者已经有一个尝试锁定重叠区域的操作未完成
 * @throws  IllegalArgumentException
 *          如果参数的前置条件不满足
 * @throws  NonReadableChannelException
 *          如果 {@code shared} 为 true 但此通道未以读取方式打开
 * @throws  NonWritableChannelException
 *          如果 {@code shared} 为 false 但此通道未以写入方式打开
 */
public abstract <A> void lock(long position,
                              long size,
                              boolean shared,
                              A attachment,
                              CompletionHandler<FileLock,? super A> handler);

                /**
     * 获取对此通道文件的独占锁。
     *
     * <p> 此方法启动一个操作，以获取此通道文件给定区域的锁。{@code handler} 参数是一个完成处理器，当锁被获取（或操作失败）时被调用。传递给完成处理器的结果是生成的 {@code FileLock}。
     *
     * <p> 以 {@code ch.lock(att,handler)} 形式调用此方法的行为与以下调用完全相同：
     * <pre>
     *     ch.{@link #lock(long,long,boolean,Object,CompletionHandler) lock}(0L, Long.MAX_VALUE, false, att, handler)
     * </pre>
     *
     * @param   <A>
     *          附件的类型
     * @param   attachment
     *          要附加到 I/O 操作的对象；可以为 {@code null}
     * @param   handler
     *          用于消耗结果的处理器
     *
     * @throws  OverlappingFileLockException
     *          如果此 Java 虚拟机已经持有锁，或者已经有尝试锁定区域的挂起尝试
     * @throws  NonWritableChannelException
     *          如果此通道未打开以进行写入
     */
    public final <A> void lock(A attachment,
                               CompletionHandler<FileLock,? super A> handler)
    {
        lock(0L, Long.MAX_VALUE, false, attachment, handler);
    }

    /**
     * 获取此通道文件给定区域的锁。
     *
     * <p> 此方法启动一个操作，以获取此通道文件给定区域的锁。此方法的行为与 {@link #lock(long, long, boolean, Object, CompletionHandler)}
     * 方法完全相同，只是此方法返回一个表示挂起结果的 {@code Future}，而不是指定完成处理器。{@code Future} 的 {@link Future#get() get} 方法在成功完成时返回 {@link
     * FileLock}。
     *
     * @param   position
     *          锁定区域开始的位置；必须是非负数
     * @param   size
     *          锁定区域的大小；必须是非负数，且 {@code position}&nbsp;+&nbsp;{@code size} 的和必须是非负数
     * @param   shared
     *          {@code true} 表示请求共享锁，此时此通道必须打开以进行读取（可能还包括写入）；
     *          {@code false} 表示请求独占锁，此时此通道必须打开以进行写入（可能还包括读取）
     *
     * @return  一个表示挂起结果的 {@code Future} 对象
     *
     * @throws  OverlappingFileLockException
     *          如果此 Java 虚拟机已经持有锁，或者已经有尝试锁定区域的挂起尝试
     * @throws  IllegalArgumentException
     *          如果参数的先决条件不成立
     * @throws  NonReadableChannelException
     *          如果 {@code shared} 为 true 但此通道未打开以进行读取
     * @throws  NonWritableChannelException
     *          如果 {@code shared} 为 false 但此通道未打开以进行写入
     */
    public abstract Future<FileLock> lock(long position, long size, boolean shared);

    /**
     * 获取对此通道文件的独占锁。
     *
     * <p> 此方法启动一个操作，以获取对此通道文件的独占锁。该方法返回一个表示操作挂起结果的 {@code Future}。{@code Future} 的 {@link Future#get()
     * get} 方法在成功完成时返回 {@link FileLock}。
     *
     * <p> 调用此方法的行为与以下调用完全相同：
     * <pre>
     *     ch.{@link #lock(long,long,boolean) lock}(0L, Long.MAX_VALUE, false)
     * </pre>
     *
     * @return  一个表示挂起结果的 {@code Future} 对象
     *
     * @throws  OverlappingFileLockException
     *          如果此 Java 虚拟机已经持有锁，或者已经有尝试锁定区域的挂起尝试
     * @throws  NonWritableChannelException
     *          如果此通道未打开以进行写入
     */
    public final Future<FileLock> lock() {
        return lock(0L, Long.MAX_VALUE, false);
    }

    /**
     * 尝试获取此通道文件给定区域的锁。
     *
     * <p> 此方法不会阻塞。调用总是立即返回，要么成功获取请求区域的锁，要么未能获取锁。如果由于其他程序持有重叠锁而无法获取锁，则返回 {@code null}。如果由于其他原因无法获取锁，则抛出适当的异常。
     *
     * @param  position
     *         锁定区域开始的位置；必须是非负数
     *
     * @param  size
     *         锁定区域的大小；必须是非负数，且 {@code position}&nbsp;+&nbsp;{@code size} 的和必须是非负数
     *
     * @param  shared
     *         {@code true} 表示请求共享锁，
     *         {@code false} 表示请求独占锁
     *
     * @return  一个表示新获取的锁的锁对象，
     *          或者如果由于其他程序持有重叠锁而无法获取锁，则返回 {@code null}
     *
     * @throws  IllegalArgumentException
     *          如果参数的先决条件不成立
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     * @throws  OverlappingFileLockException
     *          如果此 Java 虚拟机已经持有重叠请求区域的锁，或者另一个线程已经在本方法中阻塞并尝试锁定同一文件的重叠区域
     * @throws  NonReadableChannelException
     *          如果 {@code shared} 为 true 但此通道未打开以进行读取
     * @throws  NonWritableChannelException
     *          如果 {@code shared} 为 false 但此通道未打开以进行写入
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     *
     * @see     #lock(Object,CompletionHandler)
     * @see     #lock(long,long,boolean,Object,CompletionHandler)
     * @see     #tryLock()
     */
    public abstract FileLock tryLock(long position, long size, boolean shared)
        throws IOException;

                /**
     * 尝试获取对此通道文件的独占锁。
     *
     * <p> 以 {@code ch.tryLock()} 形式调用此方法的行为与调用
     *
     * <pre>
     *     ch.{@link #tryLock(long,long,boolean) tryLock}(0L, Long.MAX_VALUE, false) </pre>
     *
     * 完全相同。
     *
     * @return  一个表示新获取的锁的锁对象，
     *          如果无法获取锁（因为其他程序持有重叠的锁）则返回 {@code null}
     *
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     * @throws  OverlappingFileLockException
     *          如果此 Java 虚拟机已经持有一个与请求区域重叠的锁，或者另一个线程已经在本方法中阻塞并试图锁定一个重叠的区域
     * @throws  NonWritableChannelException
     *          如果 {@code shared} 为 false 但此通道未打开以供写入
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     *
     * @see     #lock(Object,CompletionHandler)
     * @see     #lock(long,long,boolean,Object,CompletionHandler)
     * @see     #tryLock(long,long,boolean)
     */
    public final FileLock tryLock() throws IOException {
        return tryLock(0L, Long.MAX_VALUE, false);
    }

    /**
     * 从给定的文件位置开始，将一系列字节从此通道读入给定的缓冲区。
     *
     * <p> 此方法从给定的文件位置开始，将一系列字节从此通道读入给定的缓冲区。读取的结果是读取的字节数或如果给定的位置大于或等于尝试读取时文件的大小，则返回 {@code -1}。
     *
     * <p> 此方法的工作方式与 {@link
     * AsynchronousByteChannel#read(ByteBuffer,Object,CompletionHandler)}
     * 方法相同，只是从给定的文件位置开始读取字节。如果给定的文件位置大于尝试读取时文件的大小，则不读取任何字节。
     *
     * @param   <A>
     *          附件的类型
     * @param   dst
     *          要传输字节的缓冲区
     * @param   position
     *          要开始传输的文件位置；
     *          必须是非负数
     * @param   attachment
     *          要附加到 I/O 操作的对象；可以是 {@code null}
     * @param   handler
     *          用于处理结果的处理器
     *
     * @throws  IllegalArgumentException
     *          如果位置为负数或缓冲区为只读
     * @throws  NonReadableChannelException
     *          如果此通道未打开以供读取
     */
    public abstract <A> void read(ByteBuffer dst,
                                  long position,
                                  A attachment,
                                  CompletionHandler<Integer,? super A> handler);

    /**
     * 从给定的文件位置开始，将一系列字节从此通道读入给定的缓冲区。
     *
     * <p> 此方法从给定的文件位置开始，将一系列字节从此通道读入给定的缓冲区。此方法返回一个表示操作挂起结果的 {@code Future}。{@code Future} 的 {@link Future#get() get} 方法返回读取的字节数或如果给定的位置大于或等于尝试读取时文件的大小，则返回 {@code -1}。
     *
     * <p> 此方法的工作方式与 {@link
     * AsynchronousByteChannel#read(ByteBuffer)} 方法相同，只是从给定的文件位置开始读取字节。如果给定的文件位置大于尝试读取时文件的大小，则不读取任何字节。
     *
     * @param   dst
     *          要传输字节的缓冲区
     * @param   position
     *          要开始传输的文件位置；
     *          必须是非负数
     *
     * @return  一个表示挂起结果的 {@code Future} 对象
     *
     * @throws  IllegalArgumentException
     *          如果位置为负数或缓冲区为只读
     * @throws  NonReadableChannelException
     *          如果此通道未打开以供读取
     */
    public abstract Future<Integer> read(ByteBuffer dst, long position);

    /**
     * 从给定的文件位置开始，将一系列字节从此缓冲区写入此通道。
     *
     * <p> 此方法的工作方式与 {@link
     * AsynchronousByteChannel#write(ByteBuffer,Object,CompletionHandler)}
     * 方法相同，只是从给定的文件位置开始写入字节。如果给定的位置大于尝试写入时文件的大小，则文件将扩展以容纳新的字节；从以前的文件末尾到新写入的字节之间的任何字节的值是未指定的。
     *
     * @param   <A>
     *          附件的类型
     * @param   src
     *          要传输字节的缓冲区
     * @param   position
     *          要开始传输的文件位置；
     *          必须是非负数
     * @param   attachment
     *          要附加到 I/O 操作的对象；可以是 {@code null}
     * @param   handler
     *          用于处理结果的处理器
     *
     * @throws  IllegalArgumentException
     *          如果位置为负数
     * @throws  NonWritableChannelException
     *          如果此通道未打开以供写入
     */
    public abstract <A> void write(ByteBuffer src,
                                   long position,
                                   A attachment,
                                   CompletionHandler<Integer,? super A> handler);

                /**
     * 从给定的缓冲区写入一系列字节到此通道，从给定的文件位置开始。
     *
     * <p> 该方法从给定的缓冲区开始，在给定的文件位置启动一系列字节的写入操作。该方法返回一个表示写入操作待处理结果的 {@code Future}。
     * {@code Future} 的 {@link Future#get() get} 方法返回写入的字节数。
     *
     * <p> 该方法的工作方式与 {@link AsynchronousByteChannel#write(ByteBuffer)} 方法相同，只是字节是从给定的文件位置开始写入的。
     * 如果给定的位置大于尝试写入时文件的大小，那么文件将被扩展以容纳新的字节；从之前的文件末尾到新写入的字节之间的任何字节的值是未指定的。
     *
     * @param   src
     *          要从中传输字节的缓冲区
     * @param   position
     *          转移开始的文件位置；必须是非负数
     *
     * @return  一个表示待处理结果的 {@code Future} 对象
     *
     * @throws  IllegalArgumentException
     *          如果位置为负数
     * @throws  NonWritableChannelException
     *          如果此通道未打开以供写入
     */
    public abstract Future<Integer> write(ByteBuffer src, long position);
}
