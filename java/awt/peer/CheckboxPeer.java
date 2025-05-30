/*
 * Copyright (c) 1995, 2014, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Checkbox;
import java.awt.CheckboxGroup;

/**
 * {@link Checkbox} 的对等接口。
 *
 * 对等接口仅用于移植 AWT。不建议应用程序开发人员使用这些接口，也不应直接在对等实例上调用任何对等方法。
 */
public interface CheckboxPeer extends ComponentPeer {

    /**
     * 将复选框的状态设置为选中 {@code true} 或未选中 {@code false}。
     *
     * @param state 要在复选框上设置的状态
     *
     * @see Checkbox#setState(boolean)
     */
    void setState(boolean state);

    /**
     * 为该复选框设置复选框组。一个复选框组中的复选框只能互斥选择（类似于单选按钮）。值为 {@code null} 表示将此复选框从任何复选框组中移除。
     *
     * @param g 要设置的复选框组，或当此复选框不应放在任何组中时为 {@code null}
     *
     * @see Checkbox#setCheckboxGroup(CheckboxGroup)
     */
    void setCheckboxGroup(CheckboxGroup g);

    /**
     * 设置应显示在复选框上的标签。值为 {@code null} 表示不应显示任何标签。
     *
     * @param label 要显示在复选框上的标签，或当不应显示任何标签时为 {@code null}
     */
    void setLabel(String label);

}
