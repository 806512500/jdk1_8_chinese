/*
 * Copyright (c) 2007, 2009, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.nio.file;

/**
 * {@link FileVisitor FileVisitor} 的结果类型。
 *
 * @since 1.7
 *
 * @see Files#walkFileTree
 */

public enum FileVisitResult {
    /**
     * 继续。当从 {@link FileVisitor#preVisitDirectory
     * preVisitDirectory} 方法返回时，目录中的条目也应被访问。
     */
    CONTINUE,
    /**
     * 终止。
     */
    TERMINATE,
    /**
     * 不访问此目录中的条目，继续访问。此结果仅在从 {@link
     * FileVisitor#preVisitDirectory preVisitDirectory} 方法返回时有意义；否则
     * 此结果类型与返回 {@link #CONTINUE} 相同。
     */
    SKIP_SUBTREE,
    /**
     * 不访问此文件或目录的 <em>同级</em>，继续访问。如果从 {@link FileVisitor#preVisitDirectory
     * preVisitDirectory} 方法返回，则目录中的条目也将被跳过，并且不会调用 {@link FileVisitor#postVisitDirectory postVisitDirectory}
     * 方法。
     */
    SKIP_SIBLINGS;
}
