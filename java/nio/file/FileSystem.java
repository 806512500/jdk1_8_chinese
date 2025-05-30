
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

package java.nio.file;

import java.nio.file.attribute.*;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;
import java.io.Closeable;
import java.io.IOException;

/**
 * 提供对文件系统的接口，并且是访问文件系统中的文件和其他对象的工厂。
 *
 * <p> 通过调用 {@link FileSystems#getDefault FileSystems.getDefault} 方法获取的默认文件系统，提供了对 Java 虚拟机可访问的文件系统的访问。{@link FileSystems} 类定义了方法来创建提供对其他类型（自定义）文件系统的访问的文件系统。
 *
 * <p> 文件系统是几种类型对象的工厂：
 *
 * <ul>
 *   <li><p> {@link #getPath getPath} 方法将系统依赖的 <em>路径字符串</em> 转换为 {@link Path} 对象，该对象可用于定位和访问文件。 </p></li>
 *   <li><p> {@link #getPathMatcher  getPathMatcher} 方法用于创建 {@link PathMatcher}，该对象对路径执行匹配操作。 </p></li>
 *   <li><p> {@link #getFileStores getFileStores} 方法返回一个迭代器，用于迭代底层的 {@link FileStore 文件存储}。 </p></li>
 *   <li><p> {@link #getUserPrincipalLookupService getUserPrincipalLookupService} 方法返回 {@link UserPrincipalLookupService}，用于按名称查找用户或组。 </p></li>
 *   <li><p> {@link #newWatchService newWatchService} 方法创建一个 {@link WatchService}，可用于监视对象的变化和事件。 </p></li>
 * </ul>
 *
 * <p> 文件系统差异很大。在某些情况下，文件系统是一个具有一个顶级根目录的单一文件层次结构。在其他情况下，它可能有多个独立的文件层次结构，每个层次结构都有自己的顶级根目录。可以使用 {@link #getRootDirectories getRootDirectories} 方法迭代文件系统中的根目录。文件系统通常由一个或多个底层的 {@link FileStore 文件存储} 组成，这些文件存储提供了文件的存储。这些文件存储支持的功能和与文件关联的文件属性或 <em>元数据</em> 也会有所不同。
 *
 * <p> 文件系统在创建时打开，可以通过调用其 {@link #close() close} 方法来关闭。一旦关闭，任何进一步尝试访问文件系统中的对象都会导致 {@link ClosedFileSystemException} 被抛出。由默认 {@link FileSystemProvider 提供者} 创建的文件系统不能被关闭。
 *
 * <p> 文件系统可以提供只读或读写访问。文件系统是否提供只读访问是在创建文件系统时确定的，可以通过调用其 {@link #isReadOnly() isReadOnly} 方法来测试。尝试通过与只读文件系统关联的对象写入文件存储将抛出 {@link ReadOnlyFileSystemException}。
 *
 * <p> 文件系统是线程安全的，可以由多个并发线程使用。可以随时调用 {@link #close close} 方法来关闭文件系统，但文件系统是否可以 <i>异步关闭</i> 是提供者特定的，因此未指定。换句话说，如果一个线程正在访问文件系统中的对象，而另一个线程调用了 {@code close} 方法，则可能需要阻塞，直到第一个操作完成。关闭文件系统会导致所有打开的通道、监视服务和其他 {@link Closeable 关闭} 对象与文件系统关联的都被关闭。
 *
 * @since 1.7
 */

public abstract class FileSystem
    implements Closeable
{
    /**
     * 初始化此类的新实例。
     */
    protected FileSystem() {
    }

    /**
     * 返回创建此文件系统的提供者。
     *
     * @return  创建此文件系统的提供者。
     */
    public abstract FileSystemProvider provider();

    /**
     * 关闭此文件系统。
     *
     * <p> 文件系统关闭后，通过此类定义的方法或与此文件系统关联的对象对文件系统的任何后续访问都会抛出 {@link ClosedFileSystemException}。如果文件系统已经关闭，则调用此方法没有效果。
     *
     * <p> 关闭文件系统将关闭所有打开的 {@link java.nio.channels.Channel 通道}，{@link DirectoryStream 目录流}，{@link WatchService 监视服务} 和其他与文件系统关联的可关闭对象。默认文件系统不能关闭。
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  UnsupportedOperationException
     *          在默认文件系统的情况下抛出
     */
    @Override
    public abstract void close() throws IOException;

    /**
     * 告诉此文件系统是否打开。
     *
     * <p> 由默认提供者创建的文件系统始终打开。
     *
     * @return  如果且仅当此文件系统打开时返回 {@code true}
     */
    public abstract boolean isOpen();

    /**
     * 告诉此文件系统是否仅允许只读访问其文件存储。
     *
     * @return  如果且仅当此文件系统提供只读访问时返回 {@code true}
     */
    public abstract boolean isReadOnly();

    /**
     * 返回名称分隔符，表示为字符串。
     *
     * <p> 名称分隔符用于分隔路径字符串中的名称。实现可能支持多个名称分隔符，在这种情况下，此方法返回一个实现特定的 <em>默认</em> 名称分隔符。此分隔符在调用 {@link Path#toString() toString()} 方法创建路径字符串时使用。
     *
     * <p> 在默认提供者的情况下，此方法返回与 {@link java.io.File#separator} 相同的分隔符。
     *
     * @return  名称分隔符
     */
    public abstract String getSeparator();

    /**
     * 返回一个对象，用于迭代根目录的路径。
     *
     * <p> 文件系统提供了对可能由多个独立文件层次结构组成的文件存储的访问，每个层次结构都有自己的顶级根目录。除非被安全管理器拒绝，否则返回的迭代器中的每个元素对应一个独立文件层次结构的根目录。元素的顺序未定义。文件层次结构可能在 Java 虚拟机的生命周期中发生变化。例如，在某些实现中，插入可移动媒体可能导致创建具有自己顶级目录的新文件层次结构。
     *
     * <p> 当安装了安全管理器时，会调用它来检查对每个根目录的访问。如果被拒绝，则迭代器不会返回该根目录。在默认提供者的情况下，调用 {@link SecurityManager#checkRead(String)} 方法来检查对每个根目录的读取访问。权限检查是在获取迭代器时还是在迭代过程中进行，取决于系统。
     *
     * @return  用于迭代根目录的对象
     */
    public abstract Iterable<Path> getRootDirectories();

    /**
     * 返回一个对象，用于迭代底层的文件存储。
     *
     * <p> 返回的迭代器的元素是此文件系统的 {@link FileStore 文件存储}。元素的顺序未定义，文件存储可能在 Java 虚拟机的生命周期中发生变化。当发生 I/O 错误，可能是因为文件存储不可访问时，迭代器不会返回该文件存储。
     *
     * <p> 在默认提供者的情况下，如果安装了安全管理器，安全管理器会被调用以检查 {@link RuntimePermission}<tt>("getFileStoreAttributes")</tt>。如果被拒绝，则迭代器不会返回任何文件存储。此外，安全管理器的 {@link SecurityManager#checkRead(String)} 方法会被调用以检查文件存储的 <em>顶级</em> 目录的读取访问。如果被拒绝，迭代器不会返回该文件存储。权限检查是在获取迭代器时还是在迭代过程中进行，取决于系统。
     *
     * <p> <b>使用示例：</b>
     * 假设我们想要打印所有文件存储的空间使用情况：
     * <pre>
     *     for (FileStore store: FileSystems.getDefault().getFileStores()) {
     *         long total = store.getTotalSpace() / 1024;
     *         long used = (store.getTotalSpace() - store.getUnallocatedSpace()) / 1024;
     *         long avail = store.getUsableSpace() / 1024;
     *         System.out.format("%-20s %12d %12d %12d%n", store, total, used, avail);
     *     }
     * </pre>
     *
     * @return  用于迭代底层文件存储的对象
     */
    public abstract Iterable<FileStore> getFileStores();

    /**
     * 返回此 {@code FileSystem} 支持的文件属性视图的 {@link FileAttributeView#name 名称} 的集合。
     *
     * <p> 要求支持 {@link BasicFileAttributeView}，因此集合中至少包含一个元素，"basic"。
     *
     * <p> 可以使用 {@link FileStore#supportsFileAttributeView(String) supportsFileAttributeView(String)} 方法来测试底层的 {@link FileStore} 是否支持由文件属性视图标识的文件属性。
     *
     * @return  支持的文件属性视图名称的不可修改集合
     */
    public abstract Set<String> supportedFileAttributeViews();

    /**
     * 将路径字符串或一系列字符串（连接后形成路径字符串）转换为 {@code Path}。如果 {@code more} 没有指定任何元素，则 {@code first} 参数的值是要转换的路径字符串。如果 {@code more} 指定了一个或多个元素，则每个非空字符串（包括 {@code first}）都被视为一系列名称元素（参见 {@link Path}），并连接形成路径字符串。字符串连接的细节是提供者特定的，但通常会使用 {@link #getSeparator 名称分隔符} 作为分隔符。例如，如果名称分隔符是 "{@code /}"，并且调用了 {@code getPath("/foo","bar","gus")}，则路径字符串 {@code "/foo/bar/gus"} 被转换为 {@code Path}。如果 {@code first} 是空字符串且 {@code more} 不包含任何非空字符串，则返回表示空路径的 {@code Path}。
     *
     * <p> 路径字符串的解析和转换为路径对象本质上是实现依赖的。在最简单的情况下，如果路径字符串包含无法转换为文件存储中 <em>合法</em> 字符的字符，路径字符串将被拒绝，并抛出 {@link InvalidPathException}。例如，在 UNIX 系统上，NUL (&#92;u0000) 字符不允许出现在路径中。实现可以选择拒绝包含超过任何文件存储允许长度的名称的路径字符串，以及支持复杂路径语法的实现可以选择拒绝 <em>格式错误</em> 的路径字符串。
     *
     * <p> 在默认提供者的情况下，路径字符串的解析基于平台或虚拟文件系统级别的路径定义。例如，操作系统可能不允许特定字符出现在文件名中，但特定的底层文件存储可能对合法字符集施加不同的或额外的限制。
     *
     * <p> 当路径字符串无法转换为路径时，此方法会抛出 {@link InvalidPathException}。如果可能，并且适用，异常将使用一个 {@link InvalidPathException#getIndex 指数} 值创建，该值指示导致路径字符串被拒绝的 {@code path} 参数中的第一个位置。
     *
     * @param   first
     *          路径字符串或路径字符串的初始部分
     * @param   more
     *          用于形成路径字符串的附加字符串
     *
     * @return  结果的 {@code Path}
     *
     * @throws  InvalidPathException
     *          如果路径字符串无法转换
     */
    public abstract Path getPath(String first, String... more);

    /**
     * 返回一个 {@code PathMatcher}，该对象通过解释给定的模式对 {@link Path} 对象的 {@code String} 表示形式执行匹配操作。
     *
     * {@code syntaxAndPattern} 参数标识语法和模式，形式如下：
     * <blockquote><pre>
     * <i>syntax</i><b>:</b><i>pattern</i>
     * </pre></blockquote>
     * 其中 {@code ':'} 代表自身。
     *
     * <p> {@code FileSystem} 实现支持 "{@code glob}" 和 "{@code regex}" 语法，可能还支持其他语法。语法组件的值比较时忽略大小写。
     *
     * <p> 当语法是 "{@code glob}" 时，路径的 {@code String} 表示形式使用一种类似于正则表达式的有限模式语言进行匹配，但语法更简单。例如：
     *
     * <blockquote>
     * <table border="0" summary="Pattern Language">
     * <tr>
     *   <td>{@code *.java}</td>
     *   <td>匹配表示文件名以 {@code .java} 结尾的路径</td>
     * </tr>
     * <tr>
     *   <td>{@code *.*}</td>
     *   <td>匹配包含点的文件名</td>
     * </tr>
     * <tr>
     *   <td>{@code *.{java,class}}</td>
     *   <td>匹配以 {@code .java} 或 {@code .class} 结尾的文件名</td>
     * </tr>
     * <tr>
     *   <td>{@code foo.?}</td>
     *   <td>匹配以 {@code foo.} 开头且扩展名为单个字符的文件名</td>
     * </tr>
     * <tr>
     *   <td><tt>&#47;home&#47;*&#47;*</tt>
     *   <td>在 UNIX 平台上匹配 <tt>&#47;home&#47;gus&#47;data</tt></td>
     * </tr>
     * <tr>
     *   <td><tt>&#47;home&#47;**</tt>
     *   <td>在 UNIX 平台上匹配 <tt>&#47;home&#47;gus</tt> 和 <tt>&#47;home&#47;gus&#47;data</tt></td>
     * </tr>
     * <tr>
     *   <td><tt>C:&#92;&#92;*</tt>
     *   <td>在 Windows 平台上匹配 <tt>C:&#92;foo</tt> 和 <tt>C:&#92;bar</tt>（注意反斜杠被转义；在 Java 语言中，模式将为 <tt>"C:&#92;&#92;&#92;&#92;*"</tt>）</td>
     * </tr>
     *
     * </table>
     * </blockquote>
     *
     * <p> 以下规则用于解释 glob 模式：
     *
     * <ul>
     *   <li><p> {@code *} 字符匹配 {@link Path#getName(int) 名称} 组件中的零个或多个 {@link Character 字符}，但不跨越目录边界。 </p></li>
     *
     *   <li><p> {@code **} 字符匹配跨越目录边界的零个或多个 {@link Character 字符}。 </p></li>
     *
     *   <li><p> {@code ?} 字符匹配名称组件中的一个字符。 </p></li>
     *
     *   <li><p> 反斜杠字符 ({@code \}) 用于转义其他字符，这些字符否则将被解释为特殊字符。表达式 {@code \\} 匹配单个反斜杠，"\{" 匹配左大括号，例如。 </p></li>
     *
     *   <li><p> {@code [ ]} 字符是 <i>字符集表达式</i>，匹配名称组件中的一个字符集中的单个字符。例如，{@code [abc]} 匹配 {@code "a"}、{@code "b"} 或 {@code "c"}。连字符 ({@code -}) 可用于指定范围，因此 {@code [a-z]} 指定从 {@code "a"} 到 {@code "z"}（包括）的范围。这些形式可以混合使用，例如 [abce-g] 匹配 {@code "a"}、{@code "b"}、{@code "c"}、{@code "e"}、{@code "f"} 或 {@code "g"}。如果字符集表达式后的第一个字符是 {@code !}，则用于否定，例如 {@code [!a-c]} 匹配任何字符，除了 {@code "a"}、{@code "b"} 或 {@code "c"}。
     *   <p> 在字符集表达式中，{@code *}、{@code ?} 和 {@code \} 字符匹配自身。连字符 ({@code -}) 字符如果在括号内是第一个字符，或者在否定时是第一个字符后的第一个字符，则匹配自身。</p></li>
     *
     *   <li><p> {@code { }} 字符是子模式组，如果组中的任何子模式匹配，则组匹配。逗号 ({@code ,}) 用于分隔子模式。组不能嵌套。 </p></li>
     *
     *   <li><p> 文件名中的前导点<tt>&#47;</tt>字符在匹配操作中被视为普通字符。例如，{@code "*"} glob 模式匹配文件名 {@code ".login"}。可以使用 {@link Files#isHidden} 方法测试文件是否被视为隐藏。
     *   </p></li>
     *
     *   <li><p> 所有其他字符以实现依赖的方式匹配自身。这包括表示任何 {@link FileSystem#getSeparator 名称分隔符} 的字符。 </p></li>
     *
     *   <li><p> {@link Path#getRoot 根} 组件的匹配高度依赖于实现，因此未指定。 </p></li>
     *
     * </ul>
     *
     * <p> 当语法是 "{@code regex}" 时，模式组件是 {@link java.util.regex.Pattern} 类定义的正则表达式。
     *
     * <p> 对于 glob 和 regex 语法，匹配细节（如匹配是否区分大小写）是实现依赖的，因此未指定。
     *
     * @param   syntaxAndPattern
     *          语法和模式
     *
     * @return  可用于将路径与模式匹配的路径匹配器
     *
     * @throws  IllegalArgumentException
     *          如果参数不符合 {@code syntax:pattern} 的形式
     * @throws  java.util.regex.PatternSyntaxException
     *          如果模式无效
     * @throws  UnsupportedOperationException
     *          如果模式语法不为实现所知
     *
     * @see Files#newDirectoryStream(Path,String)
     */
    public abstract PathMatcher getPathMatcher(String syntaxAndPattern);


                /**
     * 返回此文件系统的 {@code UserPrincipalLookupService}
     * <i>(可选操作)</i>。结果查找服务可用于查找用户名或组名。
     *
     * <p> <b>使用示例：</b>
     * 假设我们希望将 "joe" 设置为文件的所有者：
     * <pre>
     *     UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
     *     Files.setOwner(path, lookupService.lookupPrincipalByName("joe"));
     * </pre>
     *
     * @throws  UnsupportedOperationException
     *          如果此 {@code FileSystem} 没有查找服务
     *
     * @return  此文件系统的 {@code UserPrincipalLookupService}
     */
    public abstract UserPrincipalLookupService getUserPrincipalLookupService();

    /**
     * 构造一个新的 {@link WatchService} <i>(可选操作)</i>。
     *
     * <p> 此方法构造一个新的监视服务，可用于监视已注册对象的更改和事件。
     *
     * @return  一个新的监视服务
     *
     * @throws  UnsupportedOperationException
     *          如果此 {@code FileSystem} 不支持监视文件系统对象的更改和事件。此异常不会由默认提供程序创建的 {@code FileSystems} 抛出。
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public abstract WatchService newWatchService() throws IOException;
}
