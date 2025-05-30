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

package java.rmi.dgc;

import java.rmi.server.UID;
import java.security.SecureRandom;

/**
 * VMID 是一个在所有 Java 虚拟机中唯一的标识符。VMID 用于分布式垃圾收集器
 * 以标识客户端虚拟机。
 *
 * @author      Ann Wollrath
 * @author      Peter Jones
 */
public final class VMID implements java.io.Serializable {
    /** 唯一标识此主机的字节数组 */
    private static final byte[] randomBytes;

    /**
     * @serial 创建此主机的唯一标识字节数组
     */
    private byte[] addr;

    /**
     * @serial 相对于创建此主机的唯一标识符
     */
    private UID uid;

    /** 表示与 JDK 1.1.x 版本的类兼容 */
    private static final long serialVersionUID = -538642295484486218L;

    static {
        // 生成 8 个字节的随机数据。
        SecureRandom secureRandom = new SecureRandom();
        byte bytes[] = new byte[8];
        secureRandom.nextBytes(bytes);
        randomBytes = bytes;
    }

    /**
     * 创建一个新的 VMID。从这个构造函数返回的每个新 VMID 在以下条件下
     * 对所有 Java 虚拟机都是唯一的：a) 满足类 <code>java.rmi.server.UID</code>
     * 对象的唯一性条件，b) 可以为此主机获取一个在整个对象生命周期内唯一且恒定的地址。
     */
    public VMID() {
        addr = randomBytes;
        uid = new UID();
    }

    /**
     * 如果可以为此主机确定准确的地址，则返回 true。如果返回 false，则无法从该主机生成可靠的 VMID
     * @return 如果可以确定主机地址，则返回 true，否则返回 false
     * @deprecated
     */
    @Deprecated
    public static boolean isUnique() {
        return true;
    }

    /**
     * 计算此 VMID 的哈希码。
     */
    public int hashCode() {
        return uid.hashCode();
    }

    /**
     * 比较此 VMID 与另一个 VMID，如果它们是相同的标识符，则返回 true。
     */
    public boolean equals(Object obj) {
        if (obj instanceof VMID) {
            VMID vmid = (VMID) obj;
            if (!uid.equals(vmid.uid))
                return false;
            if ((addr == null) ^ (vmid.addr == null))
                return false;
            if (addr != null) {
                if (addr.length != vmid.addr.length)
                    return false;
                for (int i = 0; i < addr.length; ++ i)
                    if (addr[i] != vmid.addr[i])
                        return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 返回此 VMID 的字符串表示形式。
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        if (addr != null)
            for (int i = 0; i < addr.length; ++ i) {
                int x = addr[i] & 0xFF;
                result.append((x < 0x10 ? "0" : "") +
                              Integer.toString(x, 16));
            }
        result.append(':');
        result.append(uid.toString());
        return result.toString();
    }
}
