/*
 * Copyright (c) 1996, 2003, Oracle and/or its affiliates. All rights reserved.
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

package java.beans;

/**
 * PropertyEditor 类为希望允许用户编辑给定类型属性值的 GUI 提供支持。
 * <p>
 * PropertyEditor 支持显示和更新属性值的多种方式。大多数 PropertyEditor 只需支持此 API 中可用的不同选项的子集。
 * <P>
 * 简单的 PropertyEditor 可能仅支持 getAsText 和 setAsText 方法，而无需支持（例如）paintValue 或 getCustomEditor。更复杂的类型可能无法支持 getAsText 和 setAsText，但将支持 paintValue 和 getCustomEditor。
 * <p>
 * 每个 PropertyEditor 必须支持三种简单显示样式中的一种或多种。因此，它可以（1）支持 isPaintable，或（2）从 getTags() 返回非空 String[] 并从 getAsText 返回非空值，或（3）仅从 getAsText 返回非空 String。
 * <p>
 * 每个属性编辑器必须支持对 setValue 的调用，当参数对象是与此属性编辑器对应的类型时。此外，每个属性编辑器必须支持自定义编辑器或支持 setAsText。
 * <p>
 * 每个 PropertyEditor 应具有一个无参数构造函数。
 */

public interface PropertyEditor {

    /**
     * 设置（或更改）要编辑的对象。原始类型如 "int" 必须包装为相应的对象类型，如 "java.lang.Integer"。
     *
     * @param value 要编辑的新目标对象。请注意，此对象不应由 PropertyEditor 修改，而是 PropertyEditor 应创建一个新对象来保存任何修改后的值。
     */
    void setValue(Object value);

    /**
     * 获取属性值。
     *
     * @return 属性的值。原始类型如 "int" 将包装为相应的对象类型，如 "java.lang.Integer"。
     */

    Object getValue();

    //----------------------------------------------------------------------

    /**
     * 确定此属性编辑器是否可绘制。
     *
     * @return 如果类将支持 paintValue 方法，则返回 true。
     */

    boolean isPaintable();

    /**
     * 在给定的屏幕区域中绘制值的表示。请注意，PropertyEditor 负责进行适当的剪裁，以使其适合给定的矩形。
     * <p>
     * 如果 PropertyEditor 不支持绘制请求（参见 isPaintable），此方法应为静默的空操作。
     * <p>
     * 给定的 Graphics 对象将具有父容器的默认字体、颜色等。PropertyEditor 可以更改图形属性，如字体和颜色，无需恢复旧值。
     *
     * @param gfx 要绘制到的 Graphics 对象。
     * @param box 在 Graphics 对象中绘制的矩形。
     */
    void paintValue(java.awt.Graphics gfx, java.awt.Rectangle box);

    //----------------------------------------------------------------------

    /**
     * 返回一个 Java 代码片段，可用于设置属性以匹配编辑器的当前状态。此方法旨在生成 Java 代码以反映通过属性编辑器所做的更改。
     * <p>
     * 代码片段应是上下文无关的，并且必须是符合 JLS 的合法 Java 表达式。
     * <p>
     * 特别是，如果表达式表示计算，则所有类和静态成员都应完全限定。此规则适用于构造函数、静态方法和非原始参数。
     * <p>
     * 在评估表达式时应谨慎，因为它可能会抛出异常。特别是，代码生成器必须确保在存在可能抛出检查异常的表达式时生成的代码可以编译。
     * <p>
     * 示例结果：
     * <ul>
     * <li>原始表达式： <code>2</code>
     * <li>类构造函数： <code>new java.awt.Color(127,127,34)</code>
     * <li>静态字段： <code>java.awt.Color.orange</code>
     * <li>静态方法： <code>javax.swing.Box.createRigidArea(new
     *                                   java.awt.Dimension(0, 5))</code>
     * </ul>
     *
     * @return 一个表示当前值初始化器的 Java 代码片段。不应包含用于结束表达式的分号 ('<code>;</code>')。
     */
    String getJavaInitializationString();

    //----------------------------------------------------------------------

    /**
     * 以文本形式获取属性值。
     *
     * @return 作为可编辑字符串的属性值。
     * <p> 如果值不能表示为可编辑的字符串，则返回 null。
     * <p> 如果返回非 null 值，则 PropertyEditor 应准备好在 setAsText 中解析该字符串以设置值，并使用 getAsText 识别当前值。
     */
    String getAsText();

    /**
     * 通过解析给定的字符串来设置属性值。如果字符串格式不正确或此属性不能表示为文本，则可能引发 java.lang.IllegalArgumentException。
     * @param text 要解析的字符串。
     */
    void setAsText(String text) throws java.lang.IllegalArgumentException;

    //----------------------------------------------------------------------

    /**
     * 如果属性值必须是已知标记值集中的一个，则此方法应返回标记数组。这可以用于表示（例如）枚举值。如果 PropertyEditor 支持标记，则应支持使用 setAsText 与标记值设置值以及使用 getAsText 识别当前值。
     *
     * @return 此属性的标记值。如果此属性不能表示为标记值，则可能为 null。
     *
     */
    String[] getTags();

    //----------------------------------------------------------------------

    /**
     * PropertyEditor 可选择提供一个用于编辑其属性值的完整自定义组件。PropertyEditor 负责将自身连接到其编辑器组件，并通过触发 PropertyChange 事件报告属性值的变化。
     * <P>
     * 调用 getCustomEditor 的高级代码可以将组件嵌入到更大的属性表单中，或者将其放在自己的单独对话框中，或者……
     *
     * @return 一个允许用户直接编辑当前属性值的 java.awt.Component。如果不受支持，则可能为 null。
     */

    java.awt.Component getCustomEditor();

    /**
     * 确定此属性编辑器是否支持自定义编辑器。
     *
     * @return 如果属性编辑器可以提供自定义编辑器，则返回 true。
     */
    boolean supportsCustomEditor();

    //----------------------------------------------------------------------

    /**
     * 添加值更改的监听器。
     * 当属性编辑器更改其值时
     * 它应在其所有注册的 {@link PropertyChangeListener} 上触发一个 {@link PropertyChangeEvent}，
     * 指定属性名称为 {@code null}
     * 并将自身作为源。
     *
     * @param listener 要添加的 {@link PropertyChangeListener}
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * 移除值更改的监听器。
     *
     * @param listener 要移除的 {@link PropertyChangeListener}
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

}
