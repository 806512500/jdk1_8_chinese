
/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.spi.*;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * 用于读取、写入、映射和操作文件的通道。
 *
 * <p> 文件通道是一个 {@link SeekableByteChannel}，连接到一个文件。它在文件中有一个当前<i>位置</i>，可以被<i>查询</i>和<i>修改</i>。文件本身包含一个可变长度的字节序列，可以读取和写入，其当前<i>大小</i>可以查询。当在文件的当前位置之外写入字节时，文件的大小会增加；当文件被<i>截断</i>时，文件的大小会减小。文件可能还具有一些<i>元数据</i>，如访问权限、内容类型和最后修改时间；此类不定义用于访问元数据的方法。
 *
 * <p> 除了字节通道熟悉的读取、写入和关闭操作外，此类还定义了以下特定于文件的操作： </p>
 *
 * <ul>
 *
 *   <li><p> 可以在文件的绝对位置<i>读取</i>或<i>写入</i>字节，而不影响通道的当前位置。 </p></li>
 *
 *   <li><p> 文件的一个区域可以<i>映射</i>到内存中；对于大文件，这通常比调用通常的<tt>read</tt>或<tt>write</tt>方法要高效得多。 </p></li>
 *
 *   <li><p> 对文件的更新可以<i>强制</i>写入底层存储设备，确保在系统崩溃时数据不会丢失。 </p></li>
 *
 *   <li><p> 可以将字节从文件<i>传输到其他通道</i>，反之亦然，许多操作系统可以将此操作优化为非常快速的传输，直接从或到文件系统缓存。 </p></li>
 *
 *   <li><p> 文件的一个区域可以<i>锁定</i>，防止其他程序访问。 </p></li>
 *
 * </ul>
 *
 * <p> 文件通道可以由多个并发线程安全使用。{@link Channel#close close} 方法可以随时调用，如 {@link Channel} 接口所指定。在任何给定时间，只有一个涉及通道位置或可以改变文件大小的操作可以进行；尝试在第一个操作仍在进行时启动第二个此类操作将被阻塞，直到第一个操作完成。其他操作，特别是那些带有显式位置的操作，可以并发进行；它们是否确实这样做取决于底层实现，因此是未指定的。
 *
 * <p> 由此类的实例提供的文件视图保证与其他程序中的其他实例提供的相同文件的视图一致。然而，由于底层操作系统执行的缓存和网络文件系统协议引起的延迟，此类的实例提供的视图可能与其他并发运行的程序看到的视图不一致。这与这些其他程序使用的语言无关，无论它们是在同一台机器上还是在其他机器上运行。任何此类不一致的具体性质都是系统依赖的，因此是未指定的。
 *
 * <p> 通过调用此类定义的 {@link #open open} 方法之一来创建文件通道。也可以从现有的 {@link java.io.FileInputStream#getChannel FileInputStream}、{@link java.io.FileOutputStream#getChannel FileOutputStream} 或 {@link java.io.RandomAccessFile#getChannel RandomAccessFile} 对象中获取文件通道，通过调用该对象的 <tt>getChannel</tt> 方法，返回一个连接到同一底层文件的文件通道。当文件通道是从现有的流或随机访问文件中获取时，文件通道的状态与调用 <tt>getChannel</tt> 方法返回通道的对象的状态密切相关。更改通道的位置，无论是显式地还是通过读取或写入字节，都会更改源对象的文件位置，反之亦然。通过文件通道更改文件的长度会更改源对象看到的长度，反之亦然。通过写入字节更改文件的内容会更改源对象看到的内容，反之亦然。
 *
 * <a name="open-mode"></a> <p> 在多个点上，此类指定了需要一个“打开以读取”、“打开以写入”或“打开以读取和写入”的实例。通过 {@link java.io.FileInputStream#getChannel getChannel} 方法从 {@link java.io.FileInputStream} 实例获取的通道将打开以读取。通过 {@link java.io.FileOutputStream#getChannel getChannel} 方法从 {@link java.io.FileOutputStream} 实例获取的通道将打开以写入。最后，通过 {@link java.io.RandomAccessFile#getChannel getChannel} 方法从 {@link java.io.RandomAccessFile} 实例获取的通道如果该实例是用模式 <tt>"r"</tt> 创建的，则将打开以读取；如果该实例是用模式 <tt>"rw"</tt> 创建的，则将打开以读取和写入。
 *
 * <a name="append-mode"></a><p> 一个打开以写入的文件通道可能处于<i>追加模式</i>，例如，如果它是从通过调用 {@link java.io.FileOutputStream#FileOutputStream(java.io.File,boolean) FileOutputStream(File,boolean)} 构造函数并传递 <tt>true</tt> 作为第二个参数创建的文件输出流中获取的。在这种模式下，每次调用相对写入操作都会先将位置推进到文件的末尾，然后写入请求的数据。位置的推进和数据的写入是否在一个原子操作中完成是系统依赖的，因此是未指定的。
 *
 * @see java.io.FileInputStream#getChannel()
 * @see java.io.FileOutputStream#getChannel()
 * @see java.io.RandomAccessFile#getChannel()
 *
 * @author Mark Reinhold
 * @author Mike McCloskey
 * @author JSR-51 Expert Group
 * @since 1.4
 */

public abstract class FileChannel
    extends AbstractInterruptibleChannel
    implements SeekableByteChannel, GatheringByteChannel, ScatteringByteChannel
{
    /**
     * 初始化此类的新实例。
     */
    protected FileChannel() { }

    /**
     * 打开或创建一个文件，返回一个用于访问该文件的文件通道。
     *
     * <p> {@code options} 参数确定如何打开文件。{@link StandardOpenOption#READ READ} 和 {@link StandardOpenOption#WRITE WRITE} 选项确定文件是否应打开以读取和/或写入。如果数组中不包含这两个选项（或 {@link StandardOpenOption#APPEND APPEND} 选项），则文件将打开以读取。默认情况下，读取或写入从文件的开头开始。
     *
     * <p> 除了 {@code READ} 和 {@code WRITE} 之外，还可能包含以下选项：
     *
     * <table border=1 cellpadding=5 summary="">
     * <tr> <th>选项</th> <th>描述</th> </tr>
     * <tr>
     *   <td> {@link StandardOpenOption#APPEND APPEND} </td>
     *   <td> 如果此选项存在，则文件将打开以写入，并且每次调用通道的 {@code write} 方法都会先将位置推进到文件的末尾，然后写入请求的数据。位置的推进和数据的写入是否在一个原子操作中完成是系统依赖的，因此是未指定的。此选项不能与 {@code READ} 或 {@code TRUNCATE_EXISTING} 选项一起使用。 </td>
     * </tr>
     * <tr>
     *   <td> {@link StandardOpenOption#TRUNCATE_EXISTING TRUNCATE_EXISTING} </td>
     *   <td> 如果此选项存在，则现有文件将被截断为 0 字节。如果文件仅打开以读取，则此选项将被忽略。 </td>
     * </tr>
     * <tr>
     *   <td> {@link StandardOpenOption#CREATE_NEW CREATE_NEW} </td>
     *   <td> 如果此选项存在，则将创建一个新文件，如果文件已存在则失败。创建文件时，检查文件是否存在和创建文件（如果不存在）是相对于其他文件系统操作的原子操作。如果文件仅打开以读取，则此选项将被忽略。 </td>
     * </tr>
     * <tr>
     *   <td > {@link StandardOpenOption#CREATE CREATE} </td>
     *   <td> 如果此选项存在，则如果文件存在则打开现有文件，否则创建新文件。创建文件时，检查文件是否存在和创建文件（如果不存在）是相对于其他文件系统操作的原子操作。如果 {@code CREATE_NEW} 选项也存在或文件仅打开以读取，则此选项将被忽略。 </td>
     * </tr>
     * <tr>
     *   <td > {@link StandardOpenOption#DELETE_ON_CLOSE DELETE_ON_CLOSE} </td>
     *   <td> 当此选项存在时，实现将尽力在通过 {@link #close close} 方法关闭时删除文件。如果未调用 {@code close} 方法，则在 Java 虚拟机终止时将尽力删除文件。 </td>
     * </tr>
     * <tr>
     *   <td>{@link StandardOpenOption#SPARSE SPARSE} </td>
     *   <td> 创建新文件时，此选项是一个提示，表示新文件将是稀疏文件。如果未创建新文件，则此选项将被忽略。 </td>
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
     * <p> 实现还可能支持其他选项。
     *
     * <p> {@code attrs} 参数是创建文件时原子设置的可选文件 {@link FileAttribute 文件属性} 数组。
     *
     * <p> 通过调用创建 {@code Path} 的提供程序上的 {@link FileSystemProvider#newFileChannel newFileChannel} 方法来创建新的通道。
     *
     * @param   path
     *          要打开或创建的文件的路径
     * @param   options
     *          指定如何打开文件的选项
     * @param   attrs
     *          创建文件时原子设置的可选文件属性列表
     *
     * @return  一个新的文件通道
     *
     * @throws  IllegalArgumentException
     *          如果集合包含无效的选项组合
     * @throws  UnsupportedOperationException
     *          如果 {@code path} 与不支持创建文件通道的提供程序关联，或指定了不支持的打开选项，或数组包含在创建文件时不能原子设置的属性
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          如果安装了安全管理器并且它拒绝实现所需的未指定权限。对于默认提供程序，如果文件打开以读取，则调用 {@link SecurityManager#checkRead(String)} 方法检查读取权限。如果文件打开以写入，则调用 {@link SecurityManager#checkWrite(String)} 方法检查写入权限
     *
     * @since   1.7
     */
    public static FileChannel open(Path path,
                                   Set<? extends OpenOption> options,
                                   FileAttribute<?>... attrs)
        throws IOException
    {
        FileSystemProvider provider = path.getFileSystem().provider();
        return provider.newFileChannel(path, options, attrs);
    }

    @SuppressWarnings({"unchecked", "rawtypes"}) // generic array construction
    private static final FileAttribute<?>[] NO_ATTRIBUTES = new FileAttribute[0];

    /**
     * 打开或创建一个文件，返回一个用于访问该文件的文件通道。
     *
     * <p> 调用此方法的行为与以下调用完全相同：
     * <pre>
     *     fc.{@link #open(Path,Set,FileAttribute[]) open}(file, opts, new FileAttribute&lt;?&gt;[0]);
     * </pre>
     * 其中 {@code opts} 是 {@code options} 数组中指定的选项的集合。
     *
     * @param   path
     *          要打开或创建的文件的路径
     * @param   options
     *          指定如何打开文件的选项
     *
     * @return  一个新的文件通道
     *
     * @throws  IllegalArgumentException
     *          如果集合包含无效的选项组合
     * @throws  UnsupportedOperationException
     *          如果 {@code path} 与不支持创建文件通道的提供程序关联，或指定了不支持的打开选项
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          如果安装了安全管理器并且它拒绝实现所需的未指定权限。对于默认提供程序，如果文件打开以读取，则调用 {@link SecurityManager#checkRead(String)} 方法检查读取权限。如果文件打开以写入，则调用 {@link SecurityManager#checkWrite(String)} 方法检查写入权限
     *
     * @since   1.7
     */
    public static FileChannel open(Path path, OpenOption... options)
        throws IOException
    {
        Set<OpenOption> set = new HashSet<OpenOption>(options.length);
        Collections.addAll(set, options);
        return open(path, set, NO_ATTRIBUTES);
    }


                // -- Channel operations --

    /**
     * 从该通道读取一系列字节到给定的缓冲区。
     *
     * <p> 从该通道的当前文件位置开始读取字节，并更新实际读取的字节数。否则，此方法的行为与 {@link
     * ReadableByteChannel} 接口中的规定完全相同。 </p>
     */
    public abstract int read(ByteBuffer dst) throws IOException;

    /**
     * 从该通道读取一系列字节到给定缓冲区的子序列。
     *
     * <p> 从该通道的当前文件位置开始读取字节，并更新实际读取的字节数。否则，此方法的行为与 {@link
     * ScatteringByteChannel} 接口中的规定完全相同。 </p>
     */
    public abstract long read(ByteBuffer[] dsts, int offset, int length)
        throws IOException;

    /**
     * 从该通道读取一系列字节到给定的缓冲区。
     *
     * <p> 从该通道的当前文件位置开始读取字节，并更新实际读取的字节数。否则，此方法的行为与 {@link
     * ScatteringByteChannel} 接口中的规定完全相同。 </p>
     */
    public final long read(ByteBuffer[] dsts) throws IOException {
        return read(dsts, 0, dsts.length);
    }

    /**
     * 从给定的缓冲区写入一系列字节到该通道。
     *
     * <p> 从给定的缓冲区写入字节，从该通道的当前文件位置开始，除非通道处于追加模式，在这种情况下，位置首先被推进到文件的末尾。根据需要扩展文件以容纳写入的字节，然后更新实际写入的字节数。否则，此方法的行为与 {@link WritableByteChannel}
     * 接口中的规定完全相同。 </p>
     */
    public abstract int write(ByteBuffer src) throws IOException;

    /**
     * 从给定缓冲区的子序列写入一系列字节到该通道。
     *
     * <p> 从给定缓冲区的子序列写入字节，从该通道的当前文件位置开始，除非通道处于追加模式，在这种情况下，位置首先被推进到文件的末尾。根据需要扩展文件以容纳写入的字节，然后更新实际写入的字节数。否则，此方法的行为与 {@link GatheringByteChannel}
     * 接口中的规定完全相同。 </p>
     */
    public abstract long write(ByteBuffer[] srcs, int offset, int length)
        throws IOException;

    /**
     * 从给定的缓冲区写入一系列字节到该通道。
     *
     * <p> 从给定的缓冲区写入字节，从该通道的当前文件位置开始，除非通道处于追加模式，在这种情况下，位置首先被推进到文件的末尾。根据需要扩展文件以容纳写入的字节，然后更新实际写入的字节数。否则，此方法的行为与 {@link GatheringByteChannel}
     * 接口中的规定完全相同。 </p>
     */
    public final long write(ByteBuffer[] srcs) throws IOException {
        return write(srcs, 0, srcs.length);
    }


    // -- Other operations --

    /**
     * 返回该通道的文件位置。
     *
     * @return 该通道的文件位置，一个从文件开头到当前位置的非负整数。
     *
     * @throws  ClosedChannelException
     *          如果该通道已关闭
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public abstract long position() throws IOException;

    /**
     * 设置该通道的文件位置。
     *
     * <p> 将位置设置为大于文件当前大小的值是合法的，但不会改变文件的大小。稍后尝试在该位置读取字节将立即返回一个文件结束的指示。稍后尝试在该位置写入字节将导致文件扩展以容纳新字节；从之前的文件结束到新写入字节之间的字节值是未指定的。 </p>
     *
     * @param  newPosition
     *         新的位置，一个从文件开头到新位置的非负整数。
     *
     * @return 该文件通道
     *
     * @throws  ClosedChannelException
     *          如果该通道已关闭
     *
     * @throws  IllegalArgumentException
     *          如果新位置为负
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public abstract FileChannel position(long newPosition) throws IOException;

    /**
     * 返回该通道文件的当前大小。
     *
     * @return 该通道文件的当前大小，以字节为单位。
     *
     * @throws  ClosedChannelException
     *          如果该通道已关闭
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public abstract long size() throws IOException;

    /**
     * 将该通道的文件截断到给定的大小。
     *
     * <p> 如果给定的大小小于文件的当前大小，则文件被截断，丢弃新文件末尾之后的任何字节。如果给定的大小大于或等于文件的当前大小，则文件不会被修改。在这两种情况下，如果该通道的文件位置大于给定的大小，则将其设置为该大小。 </p>
     *
     * @param  size
     *         新的大小，一个非负的字节数。
     *
     * @return 该文件通道
     *
     * @throws  NonWritableChannelException
     *          如果该通道未打开以进行写入
     *
     * @throws  ClosedChannelException
     *          如果该通道已关闭
     *
     * @throws  IllegalArgumentException
     *          如果新的大小为负
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public abstract FileChannel truncate(long size) throws IOException;

    /**
     * 强制将对该通道文件的任何更新写入存储该文件的存储设备。
     *
     * <p> 如果该通道的文件位于本地存储设备上，则当此方法返回时，可以保证自该通道创建以来或自上次调用此方法以来对文件的所有更改都将被写入该设备。这对于确保在系统崩溃时关键信息不会丢失很有用。
     *
     * <p> 如果文件不位于本地设备上，则不提供此类保证。
     *
     * <p> <tt>metaData</tt> 参数可用于限制此方法必须执行的 I/O 操作次数。将 <tt>false</tt> 传递给此参数表示只需将文件内容的更新写入存储；传递 <tt>true</tt> 表示必须将文件内容和元数据的更新写入存储，这通常需要至少多一次 I/O 操作。此参数是否有效取决于底层操作系统，因此未指定。
     *
     * <p> 即使该通道仅打开以进行读取，调用此方法也可能导致 I/O 操作。例如，某些操作系统会维护文件的最后访问时间，作为文件元数据的一部分，每次读取文件时都会更新此时间。是否实际执行此操作取决于系统，因此未指定。
     *
     * <p> 此方法仅保证强制通过此类定义的方法对该通道文件进行的更改。它可能或可能不会强制通过调用 {@link #map map} 方法获得的 {@link MappedByteBuffer <i>映射字节缓冲区</i>} 修改的内容更改。调用映射字节缓冲区的 {@link
     * MappedByteBuffer#force force} 方法将强制将缓冲区内容的更改写入。 </p>
     *
     * @param   metaData
     *          如果为 <tt>true</tt>，则此方法必须强制将文件内容和元数据的更改写入存储；否则，只需将内容更改写入。
     *
     * @throws  ClosedChannelException
     *          如果该通道已关闭
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public abstract void force(boolean metaData) throws IOException;

    /**
     * 从该通道的文件传输字节到给定的可写字节通道。
     *
     * <p> 尝试从该通道的文件中从给定的 <tt>position</tt> 位置开始读取最多 <tt>count</tt> 个字节，并将它们写入目标通道。此方法可能或可能不会传输所有请求的字节；是否传输取决于通道的性质和状态。如果该通道的文件从给定的 <tt>position</tt> 位置开始包含少于 <tt>count</tt> 个字节，或者目标通道是非阻塞的且其输出缓冲区中少于 <tt>count</tt> 个字节可用，则传输的字节数将少于请求的字节数。
     *
     * <p> 此方法不会修改该通道的位置。如果给定的位置大于文件的当前大小，则不传输任何字节。如果目标通道有位置，则从该位置开始写入字节，然后将位置增加实际写入的字节数。
     *
     * <p> 此方法可能比简单的从该通道读取并写入目标通道的循环效率高得多。许多操作系统可以直接从文件系统缓存将字节传输到目标通道，而无需实际复制它们。 </p>
     *
     * @param  position
     *         文件中传输开始的位置；必须是非负数。
     *
     * @param  count
     *         要传输的最大字节数；必须是非负数。
     *
     * @param  target
     *         目标通道
     *
     * @return 实际传输的字节数，可能是零。
     *
     * @throws IllegalArgumentException
     *         如果参数的前置条件不成立
     *
     * @throws  NonReadableChannelException
     *          如果该通道未打开以进行读取
     *
     * @throws  NonWritableChannelException
     *          如果目标通道未打开以进行写入
     *
     * @throws  ClosedChannelException
     *          如果该通道或目标通道已关闭
     *
     * @throws  AsynchronousCloseException
     *          如果在传输过程中另一个线程关闭了任一通道
     *
     * @throws  ClosedByInterruptException
     *          如果在传输过程中另一个线程中断了当前线程，从而关闭了两个通道并设置了当前线程的中断状态
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public abstract long transferTo(long position, long count,
                                    WritableByteChannel target)
        throws IOException;

    /**
     * 从给定的可读字节通道传输字节到该通道的文件。
     *
     * <p> 尝试从源通道读取最多 <tt>count</tt> 个字节，并将它们写入该通道的文件，从给定的 <tt>position</tt> 位置开始。此方法可能或可能不会传输所有请求的字节；是否传输取决于通道的性质和状态。如果源通道中剩余的字节少于 <tt>count</tt> 个，或者源通道是非阻塞的且其输入缓冲区中立即可用的字节少于 <tt>count</tt> 个，则传输的字节数将少于请求的字节数。
     *
     * <p> 此方法不会修改该通道的位置。如果给定的位置大于文件的当前大小，则不传输任何字节。如果源通道有位置，则从该位置开始读取字节，然后将位置增加实际读取的字节数。
     *
     * <p> 此方法可能比简单的从源通道读取并写入该通道的循环效率高得多。许多操作系统可以直接从源通道将字节传输到文件系统缓存，而无需实际复制它们。 </p>
     *
     * @param  src
     *         源通道
     *
     * @param  position
     *         文件中传输开始的位置；必须是非负数。
     *
     * @param  count
     *         要传输的最大字节数；必须是非负数。
     *
     * @return 实际传输的字节数，可能是零。
     *
     * @throws IllegalArgumentException
     *         如果参数的前置条件不成立
     *
     * @throws  NonReadableChannelException
     *          如果源通道未打开以进行读取
     *
     * @throws  NonWritableChannelException
     *          如果该通道未打开以进行写入
     *
     * @throws  ClosedChannelException
     *          如果该通道或源通道已关闭
     *
     * @throws  AsynchronousCloseException
     *          如果在传输过程中另一个线程关闭了任一通道
     *
     * @throws  ClosedByInterruptException
     *          如果在传输过程中另一个线程中断了当前线程，从而关闭了两个通道并设置了当前线程的中断状态
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public abstract long transferFrom(ReadableByteChannel src,
                                      long position, long count)
        throws IOException;

    /**
     * 从该通道读取一系列字节到给定的缓冲区，从给定的文件位置开始。
     *
     * <p> 此方法的工作方式与 {@link
     * #read(ByteBuffer)} 方法相同，只是字节是从给定的文件位置开始读取，而不是从通道的当前位置开始。此方法不会修改该通道的位置。如果给定的位置大于文件的当前大小，则不读取任何字节。 </p>
     *
     * @param  dst
     *         要传输字节的缓冲区。
     *
     * @param  position
     *         传输开始的文件位置；必须是非负数。
     *
     * @return 读取的字节数，可能是零，如果给定的位置大于或等于文件的当前大小，则返回 <tt>-1</tt>。
     *
     * @throws  IllegalArgumentException
     *          如果位置为负
     *
     * @throws  NonReadableChannelException
     *          如果该通道未打开以进行读取
     *
     * @throws  ClosedChannelException
     *          如果该通道已关闭
     *
     * @throws  AsynchronousCloseException
     *          如果在读取操作过程中另一个线程关闭了该通道
     *
     * @throws  ClosedByInterruptException
     *          如果在读取操作过程中另一个线程中断了当前线程，从而关闭了通道并设置了当前线程的中断状态
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public abstract int read(ByteBuffer dst, long position) throws IOException;


                /**
     * 将给定缓冲区中的字节序列写入此通道，从给定的文件位置开始。
     *
     * <p> 此方法的工作方式与 {@link
     * #write(ByteBuffer)} 方法相同，但字节是从给定的文件位置开始写入，而不是从通道的当前位置开始。
     * 此方法不会修改此通道的位置。如果给定的位置大于文件的当前大小，则文件将被扩展以容纳新的字节；新字节之间的值是未指定的。 </p>
     *
     * @param  src
     *         要传输字节的缓冲区
     *
     * @param  position
     *         转移开始的文件位置；
     *         必须是非负数
     *
     * @return  写入的字节数，可能为零
     *
     * @throws  IllegalArgumentException
     *          如果位置为负数
     *
     * @throws  NonWritableChannelException
     *          如果此通道未打开以进行写入
     *
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     *
     * @throws  AsynchronousCloseException
     *          如果另一个线程在写操作进行时关闭此通道
     *
     * @throws  ClosedByInterruptException
     *          如果另一个线程在写操作进行时中断当前线程，
     *          从而关闭通道并设置当前线程的中断状态
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public abstract int write(ByteBuffer src, long position) throws IOException;


    // -- 内存映射缓冲区 --

    /**
     * 文件映射模式的安全枚举。
     *
     * @since 1.4
     *
     * @see java.nio.channels.FileChannel#map
     */
    public static class MapMode {

        /**
         * 用于只读映射的模式。
         */
        public static final MapMode READ_ONLY
            = new MapMode("READ_ONLY");

        /**
         * 用于读/写映射的模式。
         */
        public static final MapMode READ_WRITE
            = new MapMode("READ_WRITE");

        /**
         * 用于私有（写时复制）映射的模式。
         */
        public static final MapMode PRIVATE
            = new MapMode("PRIVATE");

        private final String name;

        private MapMode(String name) {
            this.name = name;
        }

        /**
         * 返回描述此文件映射模式的字符串。
         *
         * @return  描述字符串
         */
        public String toString() {
            return name;
        }

    }

    /**
     * 将此通道文件的区域直接映射到内存中。
     *
     * <p> 文件的区域可以以三种模式之一映射到内存中：
     * </p>
     *
     * <ul>
     *
     *   <li><p> <i>只读：</i> 对生成的缓冲区的任何修改尝试都将导致 {@link java.nio.ReadOnlyBufferException} 被抛出。
     *   ({@link MapMode#READ_ONLY MapMode.READ_ONLY}) </p></li>
     *
     *   <li><p> <i>读/写：</i> 对生成的缓冲区的修改最终将传播到文件；它们可能或可能不会对映射同一文件的其他程序可见。 ({@link
     *   MapMode#READ_WRITE MapMode.READ_WRITE}) </p></li>
     *
     *   <li><p> <i>私有：</i> 对生成的缓冲区的修改不会传播到文件，也不会对映射同一文件的其他程序可见；相反，它们将导致创建修改部分的私有副本。 ({@link
     *   MapMode#PRIVATE MapMode.PRIVATE}) </p></li>
     *
     * </ul>
     *
     * <p> 对于只读映射，此通道必须已打开以进行读取；对于读/写或私有映射，此通道必须已打开以进行读取和写入。
     *
     * <p> 通过此方法返回的 {@link MappedByteBuffer <i>映射字节缓冲区</i>}
     * 将具有零位置和 <tt>size</tt> 的限制和容量；其标记将未定义。缓冲区及其表示的映射将一直有效，直到缓冲区本身被垃圾回收。
     *
     * <p> 一旦建立映射，它就不依赖于用于创建它的文件通道。特别是，关闭通道不会影响映射的有效性。
     *
     * <p> 许多内存映射文件的细节取决于底层操作系统，因此未指定。当请求的区域未完全包含在此通道的文件中时，此方法的行为未指定。对底层文件内容或大小的修改（由此程序或其他程序）是否传播到缓冲区未指定。缓冲区的修改传播到文件的速率未指定。
     *
     * <p> 对于大多数操作系统，将文件映射到内存比通过通常的 {@link #read read} 和 {@link #write write} 方法读取或写入几十千字节的数据更昂贵。从性能的角度来看，通常只值得将相对较大的文件映射到内存中。 </p>
     *
     * @param  mode
     *         {@link MapMode} 类中定义的常量之一 {@link MapMode#READ_ONLY READ_ONLY}、{@link
     *         MapMode#READ_WRITE READ_WRITE} 或 {@link MapMode#PRIVATE
     *         PRIVATE}，分别表示文件是否应以只读、读/写或私有（写时复制）模式映射
     *
     * @param  position
     *         要开始映射的文件位置；必须是非负数
     *
     * @param  size
     *         要映射的区域大小；必须是非负数且不大于 {@link java.lang.Integer#MAX_VALUE}
     *
     * @return  映射的字节缓冲区
     *
     * @throws NonReadableChannelException
     *         如果 <tt>mode</tt> 是 {@link MapMode#READ_ONLY READ_ONLY} 但此通道未打开以进行读取
     *
     * @throws NonWritableChannelException
     *         如果 <tt>mode</tt> 是 {@link MapMode#READ_WRITE READ_WRITE} 或
     *         {@link MapMode#PRIVATE PRIVATE} 但此通道未打开以进行读取和写入
     *
     * @throws IllegalArgumentException
     *         如果参数的前置条件不成立
     *
     * @throws IOException
     *         如果发生其他 I/O 错误
     *
     * @see java.nio.channels.FileChannel.MapMode
     * @see java.nio.MappedByteBuffer
     */
    public abstract MappedByteBuffer map(MapMode mode,
                                         long position, long size)
        throws IOException;


    // -- 锁 --

    /**
     * 获取此通道文件的给定区域的锁。
     *
     * <p> 此方法的调用将阻塞，直到可以锁定该区域，此通道关闭，或调用线程被中断，以先发生者为准。
     *
     * <p> 如果此通道在调用此方法期间被另一个线程关闭，则会抛出 {@link AsynchronousCloseException}。
     *
     * <p> 如果调用线程在等待获取锁时被中断，则其中断状态将被设置并抛出 {@link
     * FileLockInterruptionException}。如果调用者的中断状态在调用此方法时已设置，则该异常将立即抛出；线程的中断状态不会改变。
     *
     * <p> 由 <tt>position</tt> 和 <tt>size</tt> 参数指定的区域不必包含在实际的底层文件中，甚至不必重叠。锁区域是固定大小的；如果锁定的区域最初包含文件的末尾，而文件增长超出该区域，则文件的新部分将不受锁的保护。如果文件预计会增长，并且需要整个文件的锁，则应锁定从零开始且不小于文件预期最大大小的区域。零参数的 {@link #lock()} 方法简单地锁定大小为 {@link
     * Long#MAX_VALUE} 的区域。
     *
     * <p> 一些操作系统不支持共享锁，在这种情况下，对共享锁的请求将自动转换为对独占锁的请求。可以通过调用新获取的锁对象的 {@link
     * FileLock#isShared() isShared} 方法来测试新获取的锁是共享的还是独占的。
     *
     * <p> 文件锁代表整个 Java 虚拟机持有。它们不适合用于控制同一虚拟机内多个线程对文件的访问。 </p>
     *
     * @param  position
     *         锁定区域的起始位置；必须是非负数
     *
     * @param  size
     *         锁定区域的大小；必须是非负数，且 <tt>position</tt>&nbsp;+&nbsp;<tt>size</tt> 的和必须是非负数
     *
     * @param  shared
     *         <tt>true</tt> 表示请求共享锁，此时此通道必须已打开以进行读取（可能还包括写入）；
     *         <tt>false</tt> 表示请求独占锁，此时此通道必须已打开以进行写入（可能还包括读取）
     *
     * @return  代表新获取的锁的锁对象
     *
     * @throws  IllegalArgumentException
     *          如果参数的前置条件不成立
     *
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     *
     * @throws  AsynchronousCloseException
     *          如果另一个线程在调用线程阻塞在此方法中时关闭此通道
     *
     * @throws  FileLockInterruptionException
     *          如果调用线程在阻塞在此方法中时被中断
     *
     * @throws  OverlappingFileLockException
     *          如果此 Java 虚拟机已持有与请求区域重叠的锁，或者另一个线程已阻塞在此方法中并尝试锁定同一文件的重叠区域
     *
     * @throws  NonReadableChannelException
     *          如果 <tt>shared</tt> 是 <tt>true</tt> 但此通道未打开以进行读取
     *
     * @throws  NonWritableChannelException
     *          如果 <tt>shared</tt> 是 <tt>false</tt> 但此通道未打开以进行写入
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     *
     * @see     #lock()
     * @see     #tryLock()
     * @see     #tryLock(long,long,boolean)
     */
    public abstract FileLock lock(long position, long size, boolean shared)
        throws IOException;

    /**
     * 获取此通道文件的独占锁。
     *
     * <p> 形如 <tt>fc.lock()</tt> 的此方法调用的行为与以下调用完全相同：
     *
     * <pre>
     *     fc.{@link #lock(long,long,boolean) lock}(0L, Long.MAX_VALUE, false) </pre>
     *
     * @return  代表新获取的锁的锁对象
     *
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     *
     * @throws  AsynchronousCloseException
     *          如果另一个线程在调用线程阻塞在此方法中时关闭此通道
     *
     * @throws  FileLockInterruptionException
     *          如果调用线程在阻塞在此方法中时被中断
     *
     * @throws  OverlappingFileLockException
     *          如果此 Java 虚拟机已持有与请求区域重叠的锁，或者另一个线程已阻塞在此方法中并尝试锁定同一文件的重叠区域
     *
     * @throws  NonWritableChannelException
     *          如果此通道未打开以进行写入
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     *
     * @see     #lock(long,long,boolean)
     * @see     #tryLock()
     * @see     #tryLock(long,long,boolean)
     */
    public final FileLock lock() throws IOException {
        return lock(0L, Long.MAX_VALUE, false);
    }

    /**
     * 尝试获取此通道文件的给定区域的锁。
     *
     * <p> 此方法不会阻塞。调用总是立即返回，要么成功获取请求区域的锁，要么未能获取锁。如果因另一个程序持有重叠的锁而未能获取锁，则返回
     * <tt>null</tt>。如果因其他原因未能获取锁，则抛出适当的异常。
     *
     * <p> 由 <tt>position</tt> 和 <tt>size</tt> 参数指定的区域不必包含在实际的底层文件中，甚至不必重叠。锁区域是固定大小的；如果锁定的区域最初包含文件的末尾，而文件增长超出该区域，则文件的新部分将不受锁的保护。如果文件预计会增长，并且需要整个文件的锁，则应锁定从零开始且不小于文件预期最大大小的区域。零参数的 {@link #tryLock()} 方法简单地锁定大小为 {@link
     * Long#MAX_VALUE} 的区域。
     *
     * <p> 一些操作系统不支持共享锁，在这种情况下，对共享锁的请求将自动转换为对独占锁的请求。可以通过调用新获取的锁对象的 {@link
     * FileLock#isShared() isShared} 方法来测试新获取的锁是共享的还是独占的。
     *
     * <p> 文件锁代表整个 Java 虚拟机持有。它们不适合用于控制同一虚拟机内多个线程对文件的访问。 </p>
     *
     * @param  position
     *         锁定区域的起始位置；必须是非负数
     *
     * @param  size
     *         锁定区域的大小；必须是非负数，且 <tt>position</tt>&nbsp;+&nbsp;<tt>size</tt> 的和必须是非负数
     *
     * @param  shared
     *         <tt>true</tt> 表示请求共享锁，
     *         <tt>false</tt> 表示请求独占锁
     *
     * @return  代表新获取的锁的锁对象，
     *          或 <tt>null</tt>，如果因另一个程序持有重叠的锁而无法获取锁
     *
     * @throws  IllegalArgumentException
     *          如果参数的前置条件不成立
     *
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     *
     * @throws  OverlappingFileLockException
     *          如果此 Java 虚拟机已持有与请求区域重叠的锁，或者另一个线程已阻塞在此方法中并尝试锁定同一文件的重叠区域
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     *
     * @see     #lock()
     * @see     #lock(long,long,boolean)
     * @see     #tryLock()
     */
    public abstract FileLock tryLock(long position, long size, boolean shared)
        throws IOException;


                /**
     * 尝试获取此通道文件的独占锁。
     *
     * <p> 该方法的调用形式 <tt>fc.tryLock()</tt>
     * 与以下调用完全相同：
     *
     * <pre>
     *     fc.{@link #tryLock(long,long,boolean) tryLock}(0L, Long.MAX_VALUE, false) </pre>
     *
     * @return  一个表示新获取的锁的锁对象，
     *          如果另一个程序持有重叠的锁，则返回 <tt>null</tt>
     *
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     *
     * @throws  OverlappingFileLockException
     *          如果此 Java 虚拟机已经持有一个与请求区域重叠的锁，
     *          或者另一个线程正在此方法中阻塞并试图锁定一个重叠的区域
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     *
     * @see     #lock()
     * @see     #lock(long,long,boolean)
     * @see     #tryLock(long,long,boolean)
     */
    public final FileLock tryLock() throws IOException {
        return tryLock(0L, Long.MAX_VALUE, false);
    }

}
