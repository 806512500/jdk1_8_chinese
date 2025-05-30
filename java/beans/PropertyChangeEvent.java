/*
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.util.EventObject;

/**
 * 当一个 bean 更改了一个“绑定”或“受限”属性时，会发送一个“属性更改”事件。PropertyChangeEvent 对象作为参数发送给 PropertyChangeListener 和 VetoableChangeListener 方法。
 * <P>
 * 通常，PropertyChangeEvents 会附带更改属性的名称和旧值及新值。如果新值是基本类型（如 int 或 boolean），则必须将其包装为相应的 java.lang.* 对象类型（如 Integer 或 Boolean）。
 * <P>
 * 如果旧值和新值的真实值未知，可以提供 null 值。
 * <P>
 * 事件源可以发送一个 null 对象作为名称，以表示其任意一组属性已更改。在这种情况下，旧值和新值也应为 null。
 */
public class PropertyChangeEvent extends EventObject {
    private static final long serialVersionUID = 7042693688939648123L;

    /**
     * 构造一个新的 {@code PropertyChangeEvent}。
     *
     * @param source        触发事件的 bean
     * @param propertyName  更改的属性的程序名称
     * @param oldValue      属性的旧值
     * @param newValue      属性的新值
     *
     * @throws IllegalArgumentException 如果 {@code source} 为 {@code null}
     */
    public PropertyChangeEvent(Object source, String propertyName,
                               Object oldValue, Object newValue) {
        super(source);
        this.propertyName = propertyName;
        this.newValue = newValue;
        this.oldValue = oldValue;
    }

    /**
     * 获取更改的属性的程序名称。
     *
     * @return 更改的属性的程序名称。如果多个属性已更改，则可能为 null。
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * 获取属性的新值，表示为对象。
     *
     * @return 属性的新值，表示为对象。如果多个属性已更改，则可能为 null。
     */
    public Object getNewValue() {
        return newValue;
    }

    /**
     * 获取属性的旧值，表示为对象。
     *
     * @return 属性的旧值，表示为对象。如果多个属性已更改，则可能为 null。
     */
    public Object getOldValue() {
        return oldValue;
    }

    /**
     * 设置事件的传播 ID。
     *
     * @param propagationId 事件的传播 ID。
     */
    public void setPropagationId(Object propagationId) {
        this.propagationId = propagationId;
    }

    /**
     * “传播 ID”字段保留供将来使用。在 Beans 1.0 中，唯一的要求是如果监听器捕获到一个 PropertyChangeEvent 并触发自己的 PropertyChangeEvent，则应确保将传入事件的传播 ID 字段传播到传出事件。
     *
     * @return 与绑定/受限属性更新关联的传播 ID 对象。
     */
    public Object getPropagationId() {
        return propagationId;
    }

    /**
     * 更改的属性的名称。如果未知，可能为 null。
     * @serial
     */
    private String propertyName;

    /**
     * 属性的新值。如果未知，可能为 null。
     * @serial
     */
    private Object newValue;

    /**
     * 属性的旧值。如果未知，可能为 null。
     * @serial
     */
    private Object oldValue;

    /**
     * 传播 ID。可能为 null。
     * @serial
     * @see #getPropagationId
     */
    private Object propagationId;

    /**
     * 返回对象的字符串表示形式。
     *
     * @return 对象的字符串表示形式
     *
     * @since 1.7
     */
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
        sb.append("[propertyName=").append(getPropertyName());
        appendTo(sb);
        sb.append("; oldValue=").append(getOldValue());
        sb.append("; newValue=").append(getNewValue());
        sb.append("; propagationId=").append(getPropagationId());
        sb.append("; source=").append(getSource());
        return sb.append("]").toString();
    }

    void appendTo(StringBuilder sb) {
    }
}
