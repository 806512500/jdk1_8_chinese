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
import java.util.Objects;

/**
 * 一个简单的文件访问者，其默认行为是访问所有文件，并重新抛出 I/O 错误。
 *
 * <p> 本类中的方法可以根据其通用合同进行重写。
 *
 * @param   <T>     文件的引用类型
 *
 * @since 1.7
 */

public class SimpleFileVisitor<T> implements FileVisitor<T> {
    /**
     * 初始化此类的新实例。
     */
    protected SimpleFileVisitor() {
    }

    /**
     * 在访问目录中的条目之前调用。
     *
     * <p> 除非被重写，此方法返回 {@link FileVisitResult#CONTINUE
     * CONTINUE}。
     */
    @Override
    public FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs)
        throws IOException
    {
        Objects.requireNonNull(dir);
        Objects.requireNonNull(attrs);
        return FileVisitResult.CONTINUE;
    }

    /**
     * 在访问目录中的文件时调用。
     *
     * <p> 除非被重写，此方法返回 {@link FileVisitResult#CONTINUE
     * CONTINUE}。
     */
    @Override
    public FileVisitResult visitFile(T file, BasicFileAttributes attrs)
        throws IOException
    {
        Objects.requireNonNull(file);
        Objects.requireNonNull(attrs);
        return FileVisitResult.CONTINUE;
    }

    /**
     * 在无法访问文件时调用。
     *
     * <p> 除非被重写，此方法重新抛出导致文件无法被访问的 I/O 异常。
     */
    @Override
    public FileVisitResult visitFileFailed(T file, IOException exc)
        throws IOException
    {
        Objects.requireNonNull(file);
        throw exc;
    }

    /**
     * 在访问目录中的条目及其所有后代之后调用。
     *
     * <p> 除非被重写，如果目录迭代没有 I/O 异常完成，此方法返回 {@link FileVisitResult#CONTINUE
     * CONTINUE}；否则，此方法重新抛出导致目录迭代提前终止的 I/O 异常。
     */
    @Override
    public FileVisitResult postVisitDirectory(T dir, IOException exc)
        throws IOException
    {
        Objects.requireNonNull(dir);
        if (exc != null)
            throw exc;
        return FileVisitResult.CONTINUE;
    }
}
