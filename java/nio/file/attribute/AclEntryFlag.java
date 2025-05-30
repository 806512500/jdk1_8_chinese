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

package java.nio.file.attribute;

/**
 * 定义用于 ACL {@link AclEntry 条目} 的标志组件的标志。
 *
 * <p> 在此版本中，此类不定义与 {@link
 * AclEntryType#AUDIT} 和 {@link AclEntryType#ALARM} 条目类型相关的标志。
 *
 * @since 1.7
 */

public enum AclEntryFlag {

    /**
     * 可以放置在目录上，表示应将 ACL 条目添加到每个新创建的非目录文件中。
     */
    FILE_INHERIT,

    /**
     * 可以放置在目录上，表示应将 ACL 条目添加到每个新创建的目录中。
     */
    DIRECTORY_INHERIT,

    /**
     * 可以放置在目录上，表示不应将 ACL 条目放置在新创建的目录上，该目录可由创建的子目录继承。
     */
    NO_PROPAGATE_INHERIT,

    /**
     * 可以放置在目录上，但不适用于目录，仅适用于由 {@link #FILE_INHERIT} 和 {@link #DIRECTORY_INHERIT} 标志指定的新创建的文件/目录。
     */
    INHERIT_ONLY;
}
