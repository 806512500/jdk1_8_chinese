/*
 * Copyright (c) 1995, 2005, Oracle and/or its affiliates. All rights reserved.
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
 * {@link Container} 的对等接口。这是所有容器类小部件的父接口。
 *
 * 对等接口仅用于移植 AWT。不建议应用程序开发人员使用这些接口，也不应直接在对等实例上调用任何对等方法。
 */
public interface ContainerPeer extends ComponentPeer {

    /**
     * 返回此容器的内边距。内边距通常是被边框等占用的空间。
     *
     * @return 此容器的内边距
     */
    Insets getInsets();

    /**
     * 通知对等对象组件树的验证即将开始。
     *
     * @see Container#validate()
     */
    void beginValidate();

    /**
     * 通知对等对象组件树的验证已完成。
     *
     * @see Container#validate()
     */
    void endValidate();

    /**
     * 通知对等对象布局即将开始。在容器本身及其子组件布局之前调用此方法。
     *
     * @see Container#validateTree()
     */
    void beginLayout();

    /**
     * 通知对等对象布局已完成。在容器及其子组件布局之后调用此方法。
     *
     * @see Container#validateTree()
     */
    void endLayout();
}
