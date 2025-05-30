/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.dnd;

import java.lang.annotation.Native;

/**
 * 该类包含表示
 * 拖放操作要执行的类型动作的常量值。
 * @since 1.2
 */
public final class DnDConstants {

    private DnDConstants() {} // 定义空的私有构造函数。

    /**
     * 表示无操作的 <code>int</code>。
     */
    @Native public static final int ACTION_NONE         = 0x0;

    /**
     * 表示“复制”操作的 <code>int</code>。
     */
    @Native public static final int ACTION_COPY         = 0x1;

    /**
     * 表示“移动”操作的 <code>int</code>。
     */
    @Native public static final int ACTION_MOVE         = 0x2;

    /**
     * 表示“复制”或“移动”操作的 <code>int</code>。
     */
    @Native public static final int ACTION_COPY_OR_MOVE = ACTION_COPY | ACTION_MOVE;

    /**
     * 表示“链接”操作的 <code>int</code>。
     *
     * 链接动词在许多，如果不是所有本地拖放平台中都能找到，其实际解释既依赖于平台
     * 也依赖于应用程序。大致来说，
     * 语义是“不要复制或移动操作数，而是创建一个引用
     * 到它”。定义“引用”的含义会引入歧义。
     *
     * 提供该动词是为了完整性，但不建议在逻辑上不同的应用程序之间使用
     * 拖放操作，因为对操作语义的误解可能导致用户困惑的结果。
     */

    @Native public static final int ACTION_LINK         = 0x40000000;

    /**
     * 表示“引用”操作的 <code>int</code>（ACTION_LINK 的同义词）。
     */
    @Native public static final int ACTION_REFERENCE    = ACTION_LINK;

}
