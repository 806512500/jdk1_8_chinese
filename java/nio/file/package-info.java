/*
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 定义接口和类，供 Java 虚拟机访问文件、文件属性和文件系统。
 *
 * <p> java.nio.file 包定义了访问文件和文件系统的类。访问文件和文件系统属性的 API 定义在
 * {@link java.nio.file.attribute} 包中。{@link java.nio.file.spi}
 * 包用于服务提供商实现者希望扩展平台默认提供者，或构建其他提供者实现。 </p>
 *
 * <h3><a name="links">符号链接</a></h3>
 * <p> 许多操作系统和文件系统支持 <em>符号链接</em>。
 * 符号链接是一种特殊文件，用作对另一个文件的引用。
 * 对于大多数应用程序而言，符号链接是透明的，对符号链接的操作会自动重定向到链接的 <em>目标</em>。
 * 例外情况是当符号链接被删除或重命名/移动时，删除或移除的是链接本身，而不是链接的目标。此包包括对符号链接的支持，
 * 其中实现提供了这些语义。文件系统可能支持其他类型，这些类型的语义接近，但此包不包括对这些其他类型链接的支持。 </p>
 *
 * <h3><a name="interop">互操作性</a></h3>
 * <p> {@link java.io.File} 类定义了 {@link java.io.File#toPath
 * toPath} 方法，通过将 {@code java.io.File} 对象表示的抽象路径转换为 {@link java.nio.file.Path}。
 * 结果的 {@code Path} 可用于操作与 {@code File} 对象相同的文件。{@code Path} 规范提供了关于
 * {@code Path} 和 {@code java.io.File} 对象之间 <a href="Path.html#interop">互操作性</a> 的进一步信息。 </p>
 *
 * <h3>可见性</h3>
 * <p> 本包中的类提供的文件和文件系统的视图保证与其他实例在同一个 Java 虚拟机中提供的视图一致。
 * 但是，由于底层操作系统的缓存和网络文件系统协议引起的延迟，该视图可能与同时运行的其他程序看到的文件系统视图不一致。
 * 这对于用任何语言编写的其他程序都是如此，无论它们是在同一台机器上还是在其他机器上运行。
 * 这种不一致的性质是系统依赖的，因此未指定。 </p>
 *
 * <h3><a name="integrity">同步 I/O 文件完整性</a></h3>
 * <p> 使用 {@link java.nio.file.StandardOpenOption#SYNC SYNC} 和 {@link
 * java.nio.file.StandardOpenOption#DSYNC DSYNC} 选项打开文件时，要求对文件的更新同步写入底层存储设备。
 * 在默认提供者的情况下，如果文件位于本地存储设备上，并且 {@link java.nio.channels.SeekableByteChannel
 * seekable} 通道连接到一个使用这些选项打开的文件，则调用 {@link
 * java.nio.channels.WritableByteChannel#write(java.nio.ByteBuffer) write}
 * 方法只有在该调用对文件的所有更改都已写入设备时才会返回。这些选项对于确保在系统崩溃时关键信息不会丢失非常有用。
 * 如果文件不位于本地设备上，则不提供这种保证。其他 {@link
 * java.nio.file.spi.FileSystemProvider 提供者} 实现是否提供这种保证是特定于提供者的。 </p>
 *
 * <h3>通用异常</h3>
 * <p> 除非另有说明，否则将 {@code null} 参数传递给本包中任何类或接口的构造函数或方法将导致抛出
 * {@link java.lang.NullPointerException NullPointerException}。此外，除非另有说明，使用包含
 * {@code null} 元素的集合调用方法将导致抛出 {@code NullPointerException}。 </p>
 *
 * <p> 除非另有说明，尝试访问文件系统的方法在调用与已
 * {@link java.nio.file.FileSystem#close 关闭} 的 {@link java.nio.file.FileSystem} 关联的对象时，
 * 将抛出 {@link java.nio.file.ClosedFileSystemException}。此外，任何尝试对文件系统进行写访问的方法在调用与
 * 只提供只读访问的 {@link java.nio.file.FileSystem} 关联的对象时，将抛出 {@link
 * java.nio.file.ReadOnlyFileSystemException}。 </p>
 *
 * <p> 除非另有说明，使用一个 {@link java.nio.file.spi.FileSystemProvider
 * 提供者} 创建的参数调用本包中任何类或接口的方法时，如果该参数是由另一个提供者创建的对象，
 * 将抛出 {@link java.nio.file.ProviderMismatchException}。 </p>
 *
 * <h3>可选特定异常</h3>
 * 本包中定义的大多数访问文件系统的方法都指定了在 I/O 错误发生时抛出 {@link java.io.IOException}。
 * 在某些情况下，这些方法定义了特定的 I/O 异常来处理常见情况。这些异常，标记为 <i>可选特定异常</i>，
 * 由实现检测到特定错误时抛出。如果无法检测到特定错误，则抛出更通用的 {@code
 * IOException}。
 *
 * @since 1.7
 */
package java.nio.file;
