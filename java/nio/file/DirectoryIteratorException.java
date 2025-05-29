/*
 * 版权所有 (c) 2010, 2011, Oracle 和/或其附属公司。保留所有权利。
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

import java.util.ConcurrentModificationException;
import java.util.Objects;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.InvalidObjectException;

/**
 * 运行时异常，当在目录中迭代条目时遇到 I/O 错误时抛出。I/O 错误可以通过 {@link
 * IOException} 使用 {@link #getCause() getCause()} 方法获取。
 *
 * @since 1.7
 * @see DirectoryStream
 */

public final class DirectoryIteratorException
    extends ConcurrentModificationException
{
    private static final long serialVersionUID = -6012699886086212874L;

    /**
     * 构造此类的一个实例。
     *
     * @param   cause
     *          导致目录迭代失败的 {@code IOException}
     *
     * @throws  NullPointerException
     *          如果原因 {@code null}
     */
    public DirectoryIteratorException(IOException cause) {
        super(Objects.requireNonNull(cause));
    }

    /**
     * 返回此异常的原因。
     *
     * @return  原因
     */
    @Override
    public IOException getCause() {
        return (IOException)super.getCause();
    }

    /**
     * 从流中读取对象时调用。
     *
     * @throws  InvalidObjectException
     *          如果对象无效或其原因不是
     *          一个 {@code IOException}
     */
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException
    {
        s.defaultReadObject();
        Throwable cause = super.getCause();
        if (!(cause instanceof IOException))
            throw new InvalidObjectException("Cause must be an IOException");
    }
}
