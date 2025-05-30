/*
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.security.BasicPermission;

/**
 * 用于链接创建操作的 {@code Permission} 类。
 *
 * <p> 下表提供了权限允许的内容的简要描述，以及授予代码此权限的风险。
 *
 * <table border=1 cellpadding=5
 *        summary="表格显示了权限目标名称、权限允许的内容和允许此权限的风险">
 * <tr>
 * <th>权限目标名称</th>
 * <th>权限允许的内容</th>
 * <th>允许此权限的风险</th>
 * </tr>
 * <tr>
 *   <td>hard</td>
 *   <td> 将现有文件添加到目录的能力。这有时被称为创建链接或硬链接。 </td>
 *   <td> 授予此权限时应极其谨慎。它允许链接到文件系统中的任何文件或目录，从而允许攻击者访问所有文件。 </td>
 * </tr>
 * <tr>
 *   <td>symbolic</td>
 *   <td> 创建符号链接的能力。 </td>
 *   <td> 授予此权限时应极其谨慎。它允许链接到文件系统中的任何文件或目录，从而允许攻击者访问所有文件。 </td>
 * </tr>
 * </table>
 *
 * @since 1.7
 *
 * @see Files#createLink
 * @see Files#createSymbolicLink
 */
public final class LinkPermission extends BasicPermission {
    static final long serialVersionUID = -1441492453772213220L;

    private void checkName(String name) {
        if (!name.equals("hard") && !name.equals("symbolic")) {
            throw new IllegalArgumentException("name: " + name);
        }
    }

    /**
     * 使用指定的名称构造一个 {@code LinkPermission}。
     *
     * @param   name
     *          权限的名称。必须是 "hard" 或 "symbolic"。
     *
     * @throws  IllegalArgumentException
     *          如果名称为空或无效
     */
    public LinkPermission(String name) {
        super(name);
        checkName(name);
    }

    /**
     * 使用指定的名称和操作构造一个 {@code LinkPermission}。
     *
     * @param   name
     *          权限的名称；必须是 "hard" 或 "symbolic"。
     * @param   actions
     *          权限的操作；必须是空字符串或 {@code null}。
     *
     * @throws  IllegalArgumentException
     *          如果名称为空或无效，或操作是非空字符串
     */
    public LinkPermission(String name, String actions) {
        super(name);
        checkName(name);
        if (actions != null && actions.length() > 0) {
            throw new IllegalArgumentException("actions: " + actions);
        }
    }
}
