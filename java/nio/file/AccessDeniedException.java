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
 * 当文件系统操作被拒绝时抛出的检查异常，通常是因为文件权限或其他访问检查。
 *
 * <p> 此异常与访问控制器或安全管理器因访问文件被拒绝而抛出的 {@link
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
     *          一个标识文件的字符串，或如果未知则为 {@code null}
     */
    public AccessDeniedException(String file) {
        super(file);
    }

    /**
     * 构造此类的一个实例。
     *
     * @param   file
     *          一个标识文件的字符串，或如果未知则为 {@code null}
     * @param   other
     *          一个标识其他文件的字符串，或如果未知则为 {@code null}
     * @param   reason
     *          一个包含额外信息的原因消息，或为 {@code null}
     */
    public AccessDeniedException(String file, String other, String reason) {
        super(file, other, reason);
    }
}
