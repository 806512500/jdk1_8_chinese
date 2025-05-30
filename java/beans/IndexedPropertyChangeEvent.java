/*
 * Copyright (c) 2003, 2011, Oracle and/or its affiliates. All rights reserved.
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
 * 当符合 JavaBeans™ 规范的组件（“bean”）更改绑定的索引属性时，会发送一个 "IndexedPropertyChange" 事件。此类是 <code>PropertyChangeEvent</code> 的扩展，
 * 但包含已更改属性的索引。
 * <P>
 * 如果旧值和新值的真实值未知，可以提供 null 值。
 * <P>
 * 事件源可以提供一个 null 对象作为名称，以表示其任意一组属性已更改。在这种情况下，旧值和新值也应该是 null。
 *
 * @since 1.5
 * @author Mark Davidson
 */
public class IndexedPropertyChangeEvent extends PropertyChangeEvent {
    private static final long serialVersionUID = -320227448495806870L;

    private int index;

    /**
     * 构造一个新的 <code>IndexedPropertyChangeEvent</code> 对象。
     *
     * @param source  触发事件的 bean。
     * @param propertyName  被更改的属性的程序名称。
     * @param oldValue      属性的旧值。
     * @param newValue      属性的新值。
     * @param index 已更改的属性元素的索引。
     */
    public IndexedPropertyChangeEvent(Object source, String propertyName,
                                      Object oldValue, Object newValue,
                                      int index) {
        super (source, propertyName, oldValue, newValue);
        this.index = index;
    }

    /**
     * 获取已更改的属性的索引。
     *
     * @return 指定已更改的属性元素的索引。
     */
    public int getIndex() {
        return index;
    }

    void appendTo(StringBuilder sb) {
        sb.append("; index=").append(getIndex());
    }
}
