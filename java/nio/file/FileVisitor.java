/*
 * 版权所有 (c) 2007, 2011, Oracle 和/或其附属公司。保留所有权利。
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

package java.nio.file;

import java.nio.file.attribute.BasicFileAttributes;
import java.io.IOException;

/**
 * 文件访问者。此接口的实现提供给 {@link Files#walkFileTree Files.walkFileTree} 方法，用于访问文件树中的每个文件。
 *
 * <p> <b>使用示例：</b>
 * 假设我们要删除一个文件树。在这种情况下，每个目录应该在目录中的条目被删除后删除。
 * <pre>
 *     Path start = ...
 *     Files.walkFileTree(start, new SimpleFileVisitor&lt;Path&gt;() {
 *         &#64;Override
 *         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
 *             throws IOException
 *         {
 *             Files.delete(file);
 *             return FileVisitResult.CONTINUE;
 *         }
 *         &#64;Override
 *         public FileVisitResult postVisitDirectory(Path dir, IOException e)
 *             throws IOException
 *         {
 *             if (e == null) {
 *                 Files.delete(dir);
 *                 return FileVisitResult.CONTINUE;
 *             } else {
 *                 // 目录迭代失败
 *                 throw e;
 *             }
 *         }
 *     });
 * </pre>
 * <p> 此外，假设我们要将一个文件树复制到目标位置。在这种情况下，应该跟随符号链接，并且在复制目录中的条目前应该创建目标目录。
 * <pre>
 *     final Path source = ...
 *     final Path target = ...
 *
 *     Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
 *         new SimpleFileVisitor&lt;Path&gt;() {
 *             &#64;Override
 *             public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
 *                 throws IOException
 *             {
 *                 Path targetdir = target.resolve(source.relativize(dir));
 *                 try {
 *                     Files.copy(dir, targetdir);
 *                 } catch (FileAlreadyExistsException e) {
 *                      if (!Files.isDirectory(targetdir))
 *                          throw e;
 *                 }
 *                 return CONTINUE;
 *             }
 *             &#64;Override
 *             public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
 *                 throws IOException
 *             {
 *                 Files.copy(file, target.resolve(source.relativize(file)));
 *                 return CONTINUE;
 *             }
 *         });
 * </pre>
 *
 * @since 1.7
 */

public interface FileVisitor<T> {

    /**
     * 在访问目录中的条目前调用。
     *
     * <p> 如果此方法返回 {@link FileVisitResult#CONTINUE CONTINUE}，则访问目录中的条目。如果此方法返回 {@link
     * FileVisitResult#SKIP_SUBTREE SKIP_SUBTREE} 或 {@link
     * FileVisitResult#SKIP_SIBLINGS SKIP_SIBLINGS}，则目录中的条目（及其所有后代）将不会被访问。
     *
     * @param   dir
     *          对目录的引用
     * @param   attrs
     *          目录的基本属性
     *
     * @return  访问结果
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs)
        throws IOException;

    /**
     * 调用目录中的文件。
     *
     * @param   file
     *          对文件的引用
     * @param   attrs
     *          文件的基本属性
     *
     * @return  访问结果
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    FileVisitResult visitFile(T file, BasicFileAttributes attrs)
        throws IOException;

    /**
     * 调用无法访问的文件。如果无法读取文件的属性，文件是无法打开的目录，以及其他原因，将调用此方法。
     *
     * @param   file
     *          对文件的引用
     * @param   exc
     *          阻止文件被访问的 I/O 异常
     *
     * @return  访问结果
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    FileVisitResult visitFileFailed(T file, IOException exc)
        throws IOException;

    /**
     * 在访问目录中的条目及其所有后代后调用。当目录迭代提前完成（由 {@link #visitFile visitFile}
     * 方法返回 {@link FileVisitResult#SKIP_SIBLINGS SKIP_SIBLINGS}，或在迭代目录时发生 I/O 错误）时，也会调用此方法。
     *
     * @param   dir
     *          对目录的引用
     * @param   exc
     *          如果目录迭代没有错误地完成，则为 {@code null}；否则为导致目录迭代提前完成的 I/O 异常
     *
     * @return  访问结果
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    FileVisitResult postVisitDirectory(T dir, IOException exc)
        throws IOException;
}
