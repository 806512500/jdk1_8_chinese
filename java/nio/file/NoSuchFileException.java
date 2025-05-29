/*
 * 版权所有 (c) 2007, 2009, Oracle 和/或其关联公司。保留所有权利。
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
 * 当尝试访问不存在的文件时抛出的检查异常。
 *
 * @since 1.7
 */

public class NoSuchFileException
    extends FileSystemException
{
    static final long serialVersionUID = -1390291775875351931L;

    /**
     * 构造此类的一个实例。
     *
     * @param   file
     *          识别文件的字符串，或如果未知则为 {@code null}。
     */
    public NoSuchFileException(String file) {
        super(file);
    }

    /**
     * 构造此类的一个实例。
     *
     * @param   file
     *          识别文件的字符串，或如果未知则为 {@code null}。
     * @param   other
     *          识别另一个文件的字符串，或如果未知则为 {@code null}。
     * @param   reason
     *          包含额外信息的原因消息，或如果未知则为 {@code null}
     */
    public NoSuchFileException(String file, String other, String reason) {
        super(file, other, reason);
    }
}
