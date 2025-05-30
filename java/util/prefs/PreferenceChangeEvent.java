/*
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
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

package java.util.prefs;

import java.io.NotSerializableException;

/**
 * 一个由 <tt>Preferences</tt> 节点发出的事件，用于指示
 * 偏好设置已被添加、移除或其值已更改。<p>
 *
 * 注意，尽管 PreferenceChangeEvent 继承了 EventObject 的 Serializable 接口，
 * 但它并不是为了序列化而设计的。适当的方法实现会抛出 NotSerializableException。
 *
 * @author  Josh Bloch
 * @see Preferences
 * @see PreferenceChangeListener
 * @see NodeChangeEvent
 * @since   1.4
 * @serial exclude
 */
public class PreferenceChangeEvent extends java.util.EventObject {

    /**
     * 发生变化的偏好设置的键。
     *
     * @serial
     */
    private String key;

    /**
     * 偏好设置的新值，如果已被移除，则为 <tt>null</tt>。
     *
     * @serial
     */
    private String newValue;

    /**
     * 构造一个新的 <code>PreferenceChangeEvent</code> 实例。
     *
     * @param node  发出事件的 Preferences 节点。
     * @param key  被更改的偏好设置的键。
     * @param newValue  偏好设置的新值，如果偏好设置被移除，则为 <tt>null</tt>。
     */
    public PreferenceChangeEvent(Preferences node, String key,
                                 String newValue) {
        super(node);
        this.key = key;
        this.newValue = newValue;
    }

    /**
     * 返回发出事件的偏好设置节点。
     *
     * @return  发出事件的偏好设置节点。
     */
    public Preferences getNode() {
        return (Preferences) getSource();
    }

    /**
     * 返回被更改的偏好设置的键。
     *
     * @return  被更改的偏好设置的键。
     */
    public String getKey() {
        return key;
    }

    /**
     * 返回偏好设置的新值。
     *
     * @return  偏好设置的新值，如果偏好设置被移除，则为 <tt>null</tt>。
     */
    public String getNewValue() {
        return newValue;
    }

    /**
     * 抛出 NotSerializableException，因为 NodeChangeEvent 对象
     * 不打算被序列化。
     */
     private void writeObject(java.io.ObjectOutputStream out)
                                               throws NotSerializableException {
         throw new NotSerializableException("Not serializable.");
     }

    /**
     * 抛出 NotSerializableException，因为 PreferenceChangeEvent 对象
     * 不打算被序列化。
     */
     private void readObject(java.io.ObjectInputStream in)
                                               throws NotSerializableException {
         throw new NotSerializableException("Not serializable.");
     }

    // 定义此字段是为了防止在搜索缺少 serialVersionUID 字段时将此类标记为潜在问题。
    private static final long serialVersionUID = 793724513368024975L;
}
