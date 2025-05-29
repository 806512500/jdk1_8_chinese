/*
 * Copyright (c) 2007, 2009, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 当文件系统操作被拒绝时抛出的检查异常，通常是因为文件权限或其他访问检查。
 *
 * <p> 此异常与访问控制器或安全管理者在拒绝文件访问时抛出的 {@link
 * java.security.AccessControlException AccessControlException} 或 {@link
 * SecurityException} 无关。
 *
 * @since 1.7
 */

public class AccessDeniedException
    extends FileSystemException
{
    private static final long serialVersionUID = 4943049599949219617L;

    /**
     * 构造此类的一个实例。
     *
     * @param   file
     *          一个标识文件的字符串，如果未知则为 {@code null}
     */
    public AccessDeniedException(String file) {
        super(file);
    }

    /**
     * 构造此类的一个实例。
     *
     * @param   file
     *          一个标识文件的字符串，如果未知则为 {@code null}
     * @param   other
     *          一个标识另一个文件的字符串，如果未知则为 {@code null}
     * @param   reason
     *          包含额外信息的原因消息，如果无则为 {@code null}
     */
    public AccessDeniedException(String file, String other, String reason) {
        super(file, other, reason);
    }
}
