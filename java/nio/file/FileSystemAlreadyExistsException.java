/*
 * 版权所有 (c) 2007, 2009, Oracle 和/或其附属公司。保留所有权利。
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

/**
 * 当尝试创建一个已经存在的文件系统时抛出的运行时异常。
 */

public class FileSystemAlreadyExistsException
    extends RuntimeException
{
    static final long serialVersionUID = -5438419127181131148L;

    /**
     * 构造此类的一个实例。
     */
    public FileSystemAlreadyExistsException() {
    }

    /**
     * 构造此类的一个实例。
     *
     * @param   msg
     *          详细消息
     */
    public FileSystemAlreadyExistsException(String msg) {
        super(msg);
    }
}
