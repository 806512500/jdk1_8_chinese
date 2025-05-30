/*
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
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

package java.security;

import java.util.*;
import java.io.ObjectStreamField;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

/**
 * 一个 UnresolvedPermissionCollection 存储一个 UnresolvedPermission 权限的集合。
 *
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.UnresolvedPermission
 *
 *
 * @author Roland Schemers
 *
 * @serial include
 */

final class UnresolvedPermissionCollection
extends PermissionCollection
implements java.io.Serializable
{
    /**
     * 键是权限类型，值是相同类型的 UnresolvedPermissions 列表。
     * 不序列化；请参阅类末尾的序列化部分。
     */
    private transient Map<String, List<UnresolvedPermission>> perms;

    /**
     * 创建一个空的 UnresolvedPermissionCollection 对象。
     *
     */
    public UnresolvedPermissionCollection() {
        perms = new HashMap<String, List<UnresolvedPermission>>(11);
    }

    /**
     * 向此 UnresolvedPermissionCollection 中添加一个权限。
     * 哈希的键是未解析权限的类型（类）名称。
     *
     * @param permission 要添加的 Permission 对象。
     */

    public void add(Permission permission)
    {
        if (! (permission instanceof UnresolvedPermission))
            throw new IllegalArgumentException("无效的权限: "+
                                               permission);
        UnresolvedPermission up = (UnresolvedPermission) permission;

        List<UnresolvedPermission> v;
        synchronized (this) {
            v = perms.get(up.getName());
            if (v == null) {
                v = new ArrayList<UnresolvedPermission>();
                perms.put(up.getName(), v);
            }
        }
        synchronized (v) {
            v.add(up);
        }
    }

    /**
     * 获取与 p 类型相同的任何未解析权限，并返回包含它们的列表。
     */
    List<UnresolvedPermission> getUnresolvedPermissions(Permission p) {
        synchronized (this) {
            return perms.get(p.getClass().getName());
        }
    }

    /**
     * 对于未解析的权限，始终返回 false。
     *
     */
    public boolean implies(Permission permission)
    {
        return false;
    }

    /**
     * 返回容器中所有 UnresolvedPermission 列表的枚举。
     *
     * @return 所有 UnresolvedPermission 对象的枚举。
     */

    public Enumeration<Permission> elements() {
        List<Permission> results =
            new ArrayList<>(); // 存储结果的地方

        // 获取 Map 值的迭代器（这些值是权限列表）
        synchronized (this) {
            for (List<UnresolvedPermission> l : perms.values()) {
                synchronized (l) {
                    results.addAll(l);
                }
            }
        }

        return Collections.enumeration(results);
    }

    private static final long serialVersionUID = -7176153071733132400L;

    // 需要保持与早期版本的序列化互操作性，
    // 这些版本具有可序列化的字段：
    // private Hashtable permissions; // 以类型为键

    /**
     * @serialField permissions java.util.Hashtable
     *     以类型为键的 UnresolvedPermissions 表，值是权限的 Vector
     */
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("permissions", Hashtable.class),
    };

    /**
     * @serialData 默认字段。
     */
    /*
     * 将 perms 字段的内容写入 Hashtable
     * 其中值是 Vector
     * 以保持与早期版本的序列化兼容性。
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        // 不调用 out.defaultWriteObject()

        // 将 perms 复制到 Hashtable
        Hashtable<String, Vector<UnresolvedPermission>> permissions =
            new Hashtable<>(perms.size()*2);

        // 将每个条目（List）转换为 Vector
        synchronized (this) {
            Set<Map.Entry<String, List<UnresolvedPermission>>> set = perms.entrySet();
            for (Map.Entry<String, List<UnresolvedPermission>> e : set) {
                // 将列表转换为 Vector
                List<UnresolvedPermission> list = e.getValue();
                Vector<UnresolvedPermission> vec = new Vector<>(list.size());
                synchronized (list) {
                    vec.addAll(list);
                }

                // 添加到要序列化的 Hashtable
                permissions.put(e.getKey(), vec);
            }
        }

        // 写出可序列化的字段
        ObjectOutputStream.PutField pfields = out.putFields();
        pfields.put("permissions", permissions);
        out.writeFields();
    }

    /*
     * 读取一个 Hashtable，其中值是 UnresolvedPermissions 的 Vector，并将它们保存在 perms 字段中。
     */
    private void readObject(ObjectInputStream in) throws IOException,
    ClassNotFoundException {
        // 不调用 defaultReadObject()

        // 读取序列化的字段
        ObjectInputStream.GetField gfields = in.readFields();

        // 获取权限
        @SuppressWarnings("unchecked")
        // writeObject 将 Hashtable<String, Vector<UnresolvedPermission>> 写入 permissions 键，因此这个转换是安全的，除非数据损坏。
        Hashtable<String, Vector<UnresolvedPermission>> permissions =
                (Hashtable<String, Vector<UnresolvedPermission>>)
                gfields.get("permissions", null);
        perms = new HashMap<String, List<UnresolvedPermission>>(permissions.size()*2);

        // 将每个条目（Vector）转换为 List
        Set<Map.Entry<String, Vector<UnresolvedPermission>>> set = permissions.entrySet();
        for (Map.Entry<String, Vector<UnresolvedPermission>> e : set) {
            // 将 Vector 转换为 ArrayList
            Vector<UnresolvedPermission> vec = e.getValue();
            List<UnresolvedPermission> list = new ArrayList<>(vec.size());
            list.addAll(vec);

            // 添加到要序列化的 Hashtable
            perms.put(e.getKey(), list);
        }
    }
}
