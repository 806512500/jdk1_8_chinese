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
 * 由 <tt>Preferences</tt> 节点发出的事件，表示该节点的子节点已被添加或删除。<p>
 *
 * 注意，虽然 NodeChangeEvent 继承了 java.util.EventObject 的 Serializable 接口，但它不打算被序列化。适当的序列化方法会抛出 NotSerializableException。
 *
 * @author  Josh Bloch
 * @see     Preferences
 * @see     NodeChangeListener
 * @see     PreferenceChangeEvent
 * @since   1.4
 * @serial  exclude
 */

public class NodeChangeEvent extends java.util.EventObject {
    /**
     * 被添加或删除的节点。
     *
     * @serial
     */
    private Preferences child;

    /**
     * 构造一个新的 <code>NodeChangeEvent</code> 实例。
     *
     * @param parent  被添加或删除的节点的父节点。
     * @param child   被添加或删除的节点。
     */
    public NodeChangeEvent(Preferences parent, Preferences child) {
        super(parent);
        this.child = child;
    }

    /**
     * 返回被添加或删除的节点的父节点。
     *
     * @return  被添加或删除的子节点的父节点。
     */
    public Preferences getParent() {
        return (Preferences) getSource();
    }

    /**
     * 返回被添加或删除的节点。
     *
     * @return  被添加或删除的节点。
     */
    public Preferences getChild() {
        return child;
    }

    /**
     * 抛出 NotSerializableException，因为 NodeChangeEvent 对象不打算被序列化。
     */
     private void writeObject(java.io.ObjectOutputStream out)
                                               throws NotSerializableException {
         throw new NotSerializableException("Not serializable.");
     }

    /**
     * 抛出 NotSerializableException，因为 NodeChangeEvent 对象不打算被序列化。
     */
     private void readObject(java.io.ObjectInputStream in)
                                               throws NotSerializableException {
         throw new NotSerializableException("Not serializable.");
     }

    // 定义此字段是为了在搜索缺少 serialVersionUID 字段时不会将此类标记为潜在问题。
    private static final long serialVersionUID = 8068949086596572957L;
}
