/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.rmi.activation;

import java.rmi.server.UID;

/**
 * 注册的激活组标识符有几个用途： <ul>
 * <li>在激活系统中唯一标识该组，和
 * <li>包含对组的激活系统的引用，以便组在必要时可以联系其激活系统。</ul><p>
 *
 * <code>ActivationGroupID</code> 是从调用 <code>ActivationSystem.registerGroup</code> 返回的，
 * 用于在激活系统中标识该组。此组 ID 在创建/重新创建激活组时作为参数之一传递给激活组的特殊构造函数。
 *
 * @author      Ann Wollrath
 * @see         ActivationGroup
 * @see         ActivationGroupDesc
 * @since       1.2
 */
public class ActivationGroupID implements java.io.Serializable {
    /**
     * @serial 组的激活系统。
     */
    private ActivationSystem system;

    /**
     * @serial 组的唯一 ID。
     */
    private UID uid = new UID();

    /** 表示与 Java 2 SDK v1.2 版本的类兼容 */
    private  static final long serialVersionUID = -1648432278909740833L;

    /**
     * 构造一个唯一的组 ID。
     *
     * @param system 组的激活系统
     * @throws UnsupportedOperationException 如果此实现不支持激活，则抛出此异常
     * @since 1.2
     */
    public ActivationGroupID(ActivationSystem system) {
        this.system = system;
    }

    /**
     * 返回组的激活系统。
     * @return 组的激活系统
     * @since 1.2
     */
    public ActivationSystem getSystem() {
        return system;
    }

    /**
     * 返回组标识符的哈希码。两个引用相同远程组的组标识符将具有相同的哈希码。
     *
     * @see java.util.Hashtable
     * @since 1.2
     */
    public int hashCode() {
        return uid.hashCode();
    }

    /**
     * 比较两个组标识符的内容是否相等。
     * 如果以下两个条件都为真，则返回 true：
     * 1) 唯一标识符等效（按内容），和
     * 2) 每个指定的激活系统引用相同的远程对象。
     *
     * @param   obj     要比较的对象
     * @return  如果这些对象相等，则返回 true；否则返回 false。
     * @see             java.util.Hashtable
     * @since 1.2
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof ActivationGroupID) {
            ActivationGroupID id = (ActivationGroupID)obj;
            return (uid.equals(id.uid) && system.equals(id.system));
        } else {
            return false;
        }
    }
}
