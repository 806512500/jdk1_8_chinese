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

import java.io.IOException;

/**
 * 当文件系统操作在一个或两个文件上失败时抛出。此类是文件系统异常的一般类。
 *
 * @since 1.7
 */

public class FileSystemException
    extends IOException
{
    static final long serialVersionUID = -3055425747967319812L;

    private final String file;
    private final String other;

    /**
     * 构造此类的一个实例。当涉及一个文件的操作失败且没有额外信息解释原因时，应使用此构造函数。
     *
     * @param   file
     *          一个标识文件的字符串，或如果未知则为 {@code null}。
     */
    public FileSystemException(String file) {
        super((String)null);
        this.file = file;
        this.other = null;
    }

    /**
     * 构造此类的一个实例。当涉及两个文件的操作失败，或有额外信息解释原因时，应使用此构造函数。
     *
     * @param   file
     *          一个标识文件的字符串，或如果未知则为 {@code null}。
     * @param   other
     *          一个标识另一个文件的字符串，或如果没有另一个文件或未知则为 {@code null}。
     * @param   reason
     *          包含额外信息的原因消息，或为 {@code null}。
     */
    public FileSystemException(String file, String other, String reason) {
        super(reason);
        this.file = file;
        this.other = other;
    }

    /**
     * 返回用于创建此异常的文件。
     *
     * @return  文件（可以是 {@code null}）。
     */
    public String getFile() {
        return file;
    }

    /**
     * 返回用于创建此异常的另一个文件。
     *
     * @return  另一个文件（可以是 {@code null}）。
     */
    public String getOtherFile() {
        return other;
    }

    /**
     * 返回解释文件系统操作失败原因的字符串。
     *
     * @return  解释文件系统操作失败原因的字符串。
     */
    public String getReason() {
        return super.getMessage();
    }

    /**
     * 返回详细消息字符串。
     */
    @Override
    public String getMessage() {
        if (file == null && other == null)
            return getReason();
        StringBuilder sb = new StringBuilder();
        if (file != null)
            sb.append(file);
        if (other != null) {
            sb.append(" -> ");
            sb.append(other);
        }
        if (getReason() != null) {
            sb.append(": ");
            sb.append(getReason());
        }
        return sb.toString();
    }
}
