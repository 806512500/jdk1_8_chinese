/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.beans.*;

/**
 * 这是一个帮助构建属性编辑器的支持类。
 * <p>
 * 它可以作为基类或委托使用。
 */

public class PropertyEditorSupport implements PropertyEditor {

    /**
     * 构造一个 <code>PropertyEditorSupport</code> 对象。
     *
     * @since 1.5
     */
    public PropertyEditorSupport() {
        setSource(this);
    }

    /**
     * 构造一个 <code>PropertyEditorSupport</code> 对象。
     *
     * @param source 用于事件触发的源
     * @since 1.5
     */
    public PropertyEditorSupport(Object source) {
        if (source == null) {
           throw new NullPointerException();
        }
        setSource(source);
    }

    /**
     * 返回用作事件源的 bean。如果源未显式设置，则返回此 <code>PropertyEditorSupport</code> 实例。
     *
     * @return 源对象或此实例
     * @since 1.5
     */
    public Object getSource() {
        return source;
    }

    /**
     * 设置源 bean。
     * <p>
     * 源 bean 用于属性更改的事件源。此源仅用于信息目的，不应由 PropertyEditor 修改。
     *
     * @param source 用于事件的源对象
     * @since 1.5
     */
    public void setSource(Object source) {
        this.source = source;
    }

    /**
     * 设置（或更改）要编辑的对象。
     *
     * @param value 要编辑的新目标对象。注意，此对象不应由 PropertyEditor 修改，而是应创建一个新对象来保存任何修改后的值。
     */
    public void setValue(Object value) {
        this.value = value;
        firePropertyChange();
    }

    /**
     * 获取属性的值。
     *
     * @return 属性的值。
     */
    public Object getValue() {
        return value;
    }

    //----------------------------------------------------------------------

    /**
     * 确定类是否支持 paintValue 方法。
     *
     * @return 如果类支持 paintValue 方法，则返回 True。
     */

    public boolean isPaintable() {
        return false;
    }

    /**
     * 在给定的屏幕区域中绘制值的表示。注意，属性编辑器负责进行剪切，以确保其适合给定的矩形。
     * <p>
     * 如果 PropertyEditor 不支持绘制请求（参见 isPaintable），则此方法应为空操作。
     *
     * @param gfx 用于绘制的 Graphics 对象。
     * @param box 在 Graphics 对象中绘制的矩形区域。
     */
    public void paintValue(java.awt.Graphics gfx, java.awt.Rectangle box) {
    }

    //----------------------------------------------------------------------

    /**
     * 此方法用于生成设置属性值的 Java 代码。它应返回一个 Java 代码片段，可用于初始化一个变量，以表示当前属性值。
     * <p>
     * 示例结果包括 "2"、"new Color(127,127,34)"、"Color.orange" 等。
     *
     * @return 表示当前值的 Java 代码片段。
     */
    public String getJavaInitializationString() {
        return "???";
    }

    //----------------------------------------------------------------------

    /**
     * 获取适合人类编辑的属性值的字符串表示。
     *
     * @return 适合人类编辑的属性值的字符串表示。如果值不能表示为字符串，则返回 null。
     * <p> 如果返回非 null 值，则 PropertyEditor 应准备好在 setAsText() 中解析该字符串。
     */
    public String getAsText() {
        return (this.value != null)
                ? this.value.toString()
                : null;
    }

    /**
     * 通过解析给定的字符串设置属性值。如果字符串格式不正确或此属性不能表示为文本，则可能引发 java.lang.IllegalArgumentException。
     *
     * @param text 要解析的字符串。
     */
    public void setAsText(String text) throws java.lang.IllegalArgumentException {
        if (value instanceof String) {
            setValue(text);
            return;
        }
        throw new java.lang.IllegalArgumentException(text);
    }

    //----------------------------------------------------------------------

    /**
     * 如果属性值必须是已知标记值之一，则此方法应返回标记值数组。这可以用于表示（例如）枚举值。如果 PropertyEditor 支持标记，则应支持使用 setAsText 与标记值设置值。
     *
     * @return 此属性的标记值。如果此属性不能表示为标记值，则返回 null。
     *
     */
    public String[] getTags() {
        return null;
    }

    //----------------------------------------------------------------------

    /**
     * PropertyEditor 可以选择提供一个用于编辑其属性值的完整自定义组件。PropertyEditor 负责将其自身连接到其编辑器组件，并通过触发 PropertyChange 事件报告属性值更改。
     * <P>
     * 调用 getCustomEditor 的高级代码可以将组件嵌入到更大的属性表中，或者将其放在自己的单独对话框中，等等。
     *
     * @return 一个允许人类直接编辑当前属性值的 java.awt.Component。如果不受支持，则返回 null。
     */

    public java.awt.Component getCustomEditor() {
        return null;
    }

    /**
     * 确定 propertyEditor 是否可以提供自定义编辑器。
     *
     * @return 如果 propertyEditor 可以提供自定义编辑器，则返回 True。
     */
    public boolean supportsCustomEditor() {
        return false;
    }

    //----------------------------------------------------------------------

    /**
     * 添加一个值更改的监听器。
     * 当属性编辑器更改其值时，它应在其所有注册的 {@link PropertyChangeListener} 上触发一个 {@link PropertyChangeEvent}，
     * 指定属性名称的 {@code null} 值。
     * 如果设置了源属性，则应将其用作事件的源。
     * <p>
     * 同一个监听器对象可以添加多次，并且每次添加都会被调用。如果 {@code listener} 为 {@code null}，则不抛出异常且不采取任何操作。
     *
     * @param listener 要添加的 {@link PropertyChangeListener}
     */
    public synchronized void addPropertyChangeListener(
                                PropertyChangeListener listener) {
        if (listeners == null) {
            listeners = new java.util.Vector<>();
        }
        listeners.addElement(listener);
    }

    /**
     * 移除一个值更改的监听器。
     * <p>
     * 如果同一个监听器添加了多次，则移除后将少通知一次。如果 {@code listener} 为 {@code null} 或从未添加，则不抛出异常且不采取任何操作。
     *
     * @param listener 要移除的 {@link PropertyChangeListener}
     */
    public synchronized void removePropertyChangeListener(
                                PropertyChangeListener listener) {
        if (listeners == null) {
            return;
        }
        listeners.removeElement(listener);
    }

    /**
     * 向所有感兴趣的监听器报告已修改。
     */
    public void firePropertyChange() {
        java.util.Vector<PropertyChangeListener> targets;
        synchronized (this) {
            if (listeners == null) {
                return;
            }
            targets = unsafeClone(listeners);
        }
        // 告知我们的监听器“所有”内容已更改。
        PropertyChangeEvent evt = new PropertyChangeEvent(source, null, null, null);

        for (int i = 0; i < targets.size(); i++) {
            PropertyChangeListener target = targets.elementAt(i);
            target.propertyChange(evt);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> java.util.Vector<T> unsafeClone(java.util.Vector<T> v) {
        return (java.util.Vector<T>)v.clone();
    }

    //----------------------------------------------------------------------

    private Object value;
    private Object source;
    private java.util.Vector<PropertyChangeListener> listeners;
}
