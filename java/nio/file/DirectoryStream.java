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

package java.nio.file;

import java.util.Iterator;
import java.io.Closeable;
import java.io.IOException;

/**
 * 一个用于遍历目录中条目的对象。目录流允许方便地使用 for-each 构造来遍历目录。
 *
 * <p> <b> 虽然 {@code DirectoryStream} 扩展了 {@code Iterable}，但它不是一个通用的 {@code Iterable}，因为它只支持一个 {@code
 * Iterator}；调用 {@link #iterator iterator} 方法获取第二个或后续的迭代器会抛出 {@code IllegalStateException}。 </b>
 *
 * <p> 目录流的 {@code Iterator} 的一个重要属性是，其 {@link Iterator#hasNext() hasNext} 方法保证至少提前读取一个元素。如果 {@code hasNext} 方法返回 {@code true}，并随后调用 {@code next} 方法，可以保证 {@code next} 方法不会因为 I/O 错误或流已被 {@link #close 关闭} 而抛出异常。{@code Iterator} 不支持 {@link Iterator#remove remove} 操作。
 *
 * <p> 创建时打开 {@code DirectoryStream}，并通过调用 {@code close} 方法关闭。关闭目录流会释放与流关联的任何资源。未能关闭流可能导致资源泄漏。try-with-resources 语句提供了一个有用的构造来确保流被关闭：
 * <pre>
 *   Path dir = ...
 *   try (DirectoryStream&lt;Path&gt; stream = Files.newDirectoryStream(dir)) {
 *       for (Path entry: stream) {
 *           ...
 *       }
 *   }
 * </pre>
 *
 * <p> 一旦目录流被关闭，使用 {@code Iterator} 进一步访问目录的行为就像已到达流的末尾。由于提前读取，{@code Iterator} 可能在目录流关闭后返回一个或多个元素。一旦这些缓冲的元素被读取，后续调用 {@code hasNext} 方法将返回 {@code false}，后续调用 {@code next} 方法将抛出 {@code NoSuchElementException}。
 *
 * <p> 目录流不需要是 <i>异步可关闭的</i>。如果一个线程在从目录读取的目录流的迭代器上被阻塞，另一个线程调用 {@code close} 方法，那么第二个线程可能会阻塞直到读操作完成。
 *
 * <p> 如果在访问目录时遇到 I/O 错误，它会导致 {@code Iterator} 的 {@code hasNext} 或 {@code next} 方法抛出带有 {@link IOException} 作为原因的 {@link DirectoryIteratorException}。如上所述，{@code hasNext} 方法保证至少提前读取一个元素。这意味着如果 {@code hasNext} 方法返回 {@code true}，并随后调用 {@code next} 方法，可以保证 {@code next} 方法不会因 {@code DirectoryIteratorException} 而失败。
 *
 * <p> 迭代器返回的元素没有特定的顺序。某些文件系统维护指向目录本身和目录父目录的特殊链接。表示这些链接的条目不会由迭代器返回。
 *
 * <p> 迭代器是 <i>弱一致的</i>。它是线程安全的，但在迭代时不会冻结目录，因此它可能会（也可能不会）反映在创建 {@code DirectoryStream} 之后对目录的更新。
 *
 * <p> <b>使用示例：</b>
 * 假设我们想要列出目录中的源文件。此示例同时使用 for-each 和 try-with-resources 构造。
 * <pre>
 *   List&lt;Path&gt; listSourceFiles(Path dir) throws IOException {
 *       List&lt;Path&gt; result = new ArrayList&lt;&gt;();
 *       try (DirectoryStream&lt;Path&gt; stream = Files.newDirectoryStream(dir, "*.{c,h,cpp,hpp,java}")) {
 *           for (Path entry: stream) {
 *               result.add(entry);
 *           }
 *       } catch (DirectoryIteratorException ex) {
 *           // 在迭代期间遇到 I/O 错误，原因是 IOException
 *           throw ex.getCause();
 *       }
 *       return result;
 *   }
 * </pre>
 * @param   <T>     迭代器返回的元素类型
 *
 * @since 1.7
 *
 * @see Files#newDirectoryStream(Path)
 */

public interface DirectoryStream<T>
    extends Closeable, Iterable<T> {
    /**
     * 由决定目录条目是否应被接受或过滤的对象实现的接口。当打开目录以迭代目录中的条目时，{@code Filter} 作为参数传递给 {@link Files#newDirectoryStream(Path,DirectoryStream.Filter)}
     * 方法。
     *
     * @param   <T>     目录条目的类型
     *
     * @since 1.7
     */
    @FunctionalInterface
    public static interface Filter<T> {
        /**
         * 决定给定的目录条目是否应被接受或过滤。
         *
         * @param   entry
         *          要测试的目录条目
         *
         * @return  {@code true} 如果目录条目应被接受
         *
         * @throws  IOException
         *          如果发生 I/O 错误
         */
        boolean accept(T entry) throws IOException;
    }

    /**
     * 返回与此 {@code DirectoryStream} 关联的迭代器。
     *
     * @return  与此 {@code DirectoryStream} 关联的迭代器
     *
     * @throws  IllegalStateException
     *          如果此目录流已关闭或迭代器已被返回
     */
    @Override
    Iterator<T> iterator();
}
