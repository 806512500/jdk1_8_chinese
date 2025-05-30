/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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
package java.awt;

/**
 * <code>CheckboxGroup</code> 类用于将一组 <code>Checkbox</code> 按钮组合在一起。
 * <p>
 * 在 <code>CheckboxGroup</code> 中，任何时候只能有一个复选按钮处于“选中”状态。点击任何按钮会将其状态设置为“选中”，并强制其他处于“选中”状态的按钮变为“未选中”状态。
 * <p>
 * 以下代码示例生成一个新的复选按钮组，包含三个复选按钮：
 *
 * <hr><blockquote><pre>
 * setLayout(new GridLayout(3, 1));
 * CheckboxGroup cbg = new CheckboxGroup();
 * add(new Checkbox("one", cbg, true));
 * add(new Checkbox("two", cbg, false));
 * add(new Checkbox("three", cbg, false));
 * </pre></blockquote><hr>
 * <p>
 * 该示例创建的复选按钮组如下图所示：
 * <p>
 * <img src="doc-files/CheckboxGroup-1.gif"
 * alt="显示三个垂直排列的复选按钮，分别标记为 one、two 和 three。复选按钮 one 处于选中状态。"
 * style="float:center; margin: 7px 10px;">
 * <p>
 * @author      Sami Shaio
 * @see         java.awt.Checkbox
 * @since       JDK1.0
 */
public class CheckboxGroup implements java.io.Serializable {
    /**
     * 当前的选择。
     * @serial
     * @see #getSelectedCheckbox()
     * @see #setSelectedCheckbox(Checkbox)
     */
    Checkbox selectedCheckbox = null;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = 3729780091441768983L;

    /**
     * 创建一个新的 <code>CheckboxGroup</code> 实例。
     */
    public CheckboxGroup() {
    }

    /**
     * 从这个复选按钮组中获取当前的选择。
     * 当前选择是该组中当前处于“选中”状态的复选按钮，
     * 或者如果组中的所有复选按钮都未选中，则返回 <code>null</code>。
     * @return   当前处于“选中”状态的复选按钮，或者 <code>null</code>。
     * @see      java.awt.Checkbox
     * @see      java.awt.CheckboxGroup#setSelectedCheckbox
     * @since    JDK1.1
     */
    public Checkbox getSelectedCheckbox() {
        return getCurrent();
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>getSelectedCheckbox()</code>。
     */
    @Deprecated
    public Checkbox getCurrent() {
        return selectedCheckbox;
    }

    /**
     * 将此组中的当前选中复选按钮设置为指定的复选按钮。
     * 该方法将指定复选按钮的状态设置为“选中”，并将组中的所有其他复选按钮设置为“未选中”。
     * <p>
     * 如果复选按钮参数为 <tt>null</tt>，则取消选中此复选按钮组中的所有复选按钮。如果复选按钮参数属于不同的复选按钮组，则此方法不执行任何操作。
     * @param     box   要设置为当前选择的 <code>Checkbox</code>。
     * @see      java.awt.Checkbox
     * @see      java.awt.CheckboxGroup#getSelectedCheckbox
     * @since    JDK1.1
     */
    public void setSelectedCheckbox(Checkbox box) {
        setCurrent(box);
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>setSelectedCheckbox(Checkbox)</code>。
     */
    @Deprecated
    public synchronized void setCurrent(Checkbox box) {
        if (box != null && box.group != this) {
            return;
        }
        Checkbox oldChoice = this.selectedCheckbox;
        this.selectedCheckbox = box;
        if (oldChoice != null && oldChoice != box && oldChoice.group == this) {
            oldChoice.setState(false);
        }
        if (box != null && oldChoice != box && !box.getState()) {
            box.setStateInternal(true);
        }
    }

    /**
     * 返回此复选按钮组的字符串表示形式，包括其当前选择的值。
     * @return    此复选按钮组的字符串表示形式。
     */
    public String toString() {
        return getClass().getName() + "[selectedCheckbox=" + selectedCheckbox + "]";
    }

}
