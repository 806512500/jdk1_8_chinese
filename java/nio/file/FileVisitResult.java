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
 * 文件访问者的结果类型。
 *
 * @since 1.7
 *
 * @see Files#walkFileTree
 */

public enum FileVisitResult {
    /**
     * 继续。当从 {@link FileVisitor#preVisitDirectory
     * preVisitDirectory} 方法返回时，表示目录中的条目也应被访问。
     */
    CONTINUE,
    /**
     * 终止。
     */
    TERMINATE,
    /**
     * 继续但不访问此目录中的条目。此结果仅在从 {@link
     * FileVisitor#preVisitDirectory preVisitDirectory} 方法返回时有意义；否则，此结果类型与返回 {@link #CONTINUE} 相同。
     */
    SKIP_SUBTREE,
    /**
     * 继续但不访问此文件或目录的<em>同级</em>。如果从 {@link FileVisitor#preVisitDirectory
     * preVisitDirectory} 方法返回，则目录中的条目也将被跳过，并且不会调用 {@link FileVisitor#postVisitDirectory postVisitDirectory} 方法。
     */
    SKIP_SIBLINGS;
}
