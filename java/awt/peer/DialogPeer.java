/*
 * Copyright (c) 1995, 2007, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.*;

/**
 * {@link Dialog} 的对等接口。这在 {@link WindowPeer} 接口的基础上添加了一些特定于对话框的功能。
 *
 * 对等接口仅用于移植 AWT。不建议应用程序开发人员使用这些接口，也不应直接调用对等实例上的任何对等方法。
 */
public interface DialogPeer extends WindowPeer {

    /**
     * 设置对话框窗口的标题。
     *
     * @param title 要设置的标题
     *
     * @see Dialog#setTitle(String)
     */
    void setTitle(String title);

    /**
     * 设置对话框是否可调整大小。
     *
     * @param resizeable 如果对话框应可调整大小，则为 {@code true}，否则为 {@code false}
     *
     * @see Dialog#setResizable(boolean)
     */
    void setResizable(boolean resizeable);

    /**
     * 阻塞指定的窗口。这用于模态对话框。
     *
     * @param windows 要阻塞的窗口
     *
     * @see Dialog#modalShow()
     * @see Dialog#blockWindows()
     */
    void blockWindows(java.util.List<Window> windows);
}
