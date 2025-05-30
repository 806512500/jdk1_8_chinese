/*
 * Copyright (c) 1995, 1998, Oracle and/or its affiliates. All rights reserved.
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
package java.awt.peer;

import java.awt.MenuItem;

/**
 * 菜单项的对等接口。此接口由 {@link MenuItem} 使用。
 *
 * 对等接口仅用于移植 AWT。不建议应用程序开发人员使用这些接口，也不应实现对等接口或直接调用对等实例上的任何对等方法。
 */
public interface MenuItemPeer extends MenuComponentPeer {

    /**
     * 设置此菜单项中显示的标签。
     *
     * @param label 要显示的标签
     */
    void setLabel(String label);

    /**
     * 启用或禁用菜单项。
     *
     * @param e {@code true} 启用菜单项，{@code false} 禁用菜单项
     */
    void setEnabled(boolean e);

}
