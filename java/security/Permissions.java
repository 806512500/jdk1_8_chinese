
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

package java.security;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.Collections;
import java.io.Serializable;
import java.io.ObjectStreamField;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;


/**
 * 该类表示一个异构的权限集合。也就是说，它包含不同类型的 Permission 对象，这些对象组织成
 * PermissionCollections。例如，如果任何 {@code java.io.FilePermission} 对象被添加到该类的一个实例中，
 * 它们将全部存储在一个 PermissionCollection 中。这个 PermissionCollection 是通过调用
 * FilePermission 类中的 {@code newPermissionCollection} 方法返回的。同样地，任何
 * {@code java.lang.RuntimePermission} 对象都存储在通过调用
 * RuntimePermission 类中的 {@code newPermissionCollection} 方法返回的 PermissionCollection 中。
 * 因此，这个类表示一个 PermissionCollections 的集合。
 *
 * <p>当调用 {@code add} 方法添加一个 Permission 时，该 Permission 将被存储在适当的 PermissionCollection 中。
 * 如果还没有这样的集合，将确定该 Permission 对象的类，并调用该类的 {@code newPermissionCollection} 方法来创建
 * PermissionCollection 并将其添加到 Permissions 对象中。如果 {@code newPermissionCollection} 返回 null，
 * 则将创建并使用一个默认的 PermissionCollection，该 PermissionCollection 使用一个 hashtable。每个 hashtable 条目
 * 都将一个 Permission 对象存储为键和值。
 *
 * <p>通过 {@code elements} 方法返回的枚举不是 <em>快速失败的</em>。在枚举一个集合时不应
 * 对该集合进行修改。
 *
 * @see Permission
 * @see PermissionCollection
 * @see AllPermission
 *
 *
 * @author Marianne Mueller
 * @author Roland Schemers
 *
 * @serial exclude
 */

public final class Permissions extends PermissionCollection
implements Serializable
{
    /**
     * 键是权限类，值是该类的 PermissionCollection。不序列化；请参阅类末尾的序列化部分。
     */
    private transient Map<Class<?>, PermissionCollection> permsMap;

    // 优化。跟踪未解析的权限是否需要检查
    private transient boolean hasUnresolved = false;

    // 优化。跟踪 AllPermission 集合
    // - 包私有，以便 ProtectionDomain 优化
    PermissionCollection allPermission;

    /**
     * 创建一个新的 Permissions 对象，不包含任何 PermissionCollections。
     */
    public Permissions() {
        permsMap = new HashMap<Class<?>, PermissionCollection>(11);
        allPermission = null;
    }

    /**
     * 将一个权限对象添加到该权限所属类的 PermissionCollection 中。例如，如果 <i>permission</i> 是一个
     * FilePermission，它将被添加到此 Permissions 对象中存储的 FilePermissionCollection 中。
     *
     * 如果还没有适当的集合，此方法将创建一个新的 PermissionCollection 对象（并添加权限）。
     * <p>
     *
     * @param permission 要添加的 Permission 对象。
     *
     * @exception SecurityException 如果此 Permissions 对象被标记为只读。
     *
     * @see PermissionCollection#isReadOnly()
     */

    public void add(Permission permission) {
        if (isReadOnly())
            throw new SecurityException(
              "尝试向只读的 Permissions 对象添加权限");

        PermissionCollection pc;

        synchronized (this) {
            pc = getPermissionCollection(permission, true);
            pc.add(permission);
        }

        // 无需同步；陈旧性 -> 优化延迟，这是可以接受的
        if (permission instanceof AllPermission) {
            allPermission = pc;
        }
        if (permission instanceof UnresolvedPermission) {
            hasUnresolved = true;
        }
    }

    /**
     * 检查此对象的指定权限类的 PermissionCollection 是否隐含了 <i>permission</i> 对象中表达的权限。
     * 如果适当的 PermissionCollection（例如，对于 FilePermission 的 FilePermissionCollection）中的权限组合
     * 隐含了指定的权限，则返回 true。
     *
     * <p>例如，假设此 Permissions 对象中有一个 FilePermissionCollection，其中包含一个指定
     * "/tmp" 目录及其所有子目录中所有文件的 "read" 访问权限的 FilePermission，以及另一个指定
     * "/tmp/scratch/foo" 目录中所有文件的 "write" 访问权限的 FilePermission。
     * 那么，如果 {@code implies} 方法
     * 被调用时指定的权限是 "/tmp/scratch/foo" 目录中文件的 "read" 和 "write" 访问权限，
     * 则返回 {@code true}。
     *
     * <p>此外，如果此 PermissionCollection 包含 AllPermission，此方法将始终返回 true。
     * <p>
     * @param permission 要检查的 Permission 对象。
     *
     * @return 如果 "permission" 被其所属的 PermissionCollection 中的权限隐含，则返回 true，否则返回 false。
     */

    public boolean implies(Permission permission) {
        // 无需同步；陈旧性 -> 跳过优化，这是可以接受的
        if (allPermission != null) {
            return true; // AllPermission 已经被添加
        } else {
            synchronized (this) {
                PermissionCollection pc = getPermissionCollection(permission,
                    false);
                if (pc != null) {
                    return pc.implies(permission);
                } else {
                    // 未找到
                    return false;
                }
            }
        }
    }

    /**
     * 返回此 Permissions 对象中所有 PermissionCollections 中的所有 Permission 对象的枚举。
     *
     * @return 所有 Permissions 的枚举。
     */

    public Enumeration<Permission> elements() {
        // 遍历哈希表中的每个 Permissions
        // 并调用它们的 elements() 方法。

        synchronized (this) {
            return new PermissionsEnumerator(permsMap.values().iterator());
        }
    }

    /**
     * 获取此 Permissions 对象中类型与 <i>p</i> 相同的权限的 PermissionCollection。
     * 例如，如果 <i>p</i> 是一个 FilePermission，
     * 存储在此 Permissions 对象中的 FilePermissionCollection 将被返回。
     *
     * 如果 createEmpty 为 true，
     * 且尚未存在指定类型的 PermissionCollection 对象，此方法将创建一个新的 PermissionCollection 对象。
     * 为此，它首先调用 <i>p</i> 上的 {@code newPermissionCollection} 方法。
     * Permission 类的子类如果需要将其权限存储在特定的 PermissionCollection 对象中以提供
     * 调用 {@code PermissionCollection.implies} 方法时的正确语义，将覆盖该方法。
     * 如果调用返回一个 PermissionCollection，该集合将被存储在此 Permissions 对象中。如果调用返回 null 且 createEmpty 为 true，
     * 则此方法将实例化并存储一个默认的 PermissionCollection，该 PermissionCollection 使用 hashtable 存储其权限对象。
     *
     * 由于确定要使用的 PermissionCollection 的开销，对于未解析的权限创建空的 PermissionCollection 时忽略 createEmpty。
     *
     * 当从 implies() 调用此方法时，应将 createEmpty 设置为 false，因为这会增加创建和添加一个空的 PermissionCollection 的额外开销，
     * 而该 PermissionCollection 将仅返回 false。当从 add() 调用时，应将其设置为 true。
     */
    private PermissionCollection getPermissionCollection(Permission p,
        boolean createEmpty) {
        Class<?> c = p.getClass();

        PermissionCollection pc = permsMap.get(c);

        if (!hasUnresolved && !createEmpty) {
            return pc;
        } else if (pc == null) {

            // 检查未解析的权限
            pc = (hasUnresolved ? getUnresolvedPermissions(p) : null);

            // 如果仍然为 null，创建一个新的集合
            if (pc == null && createEmpty) {

                pc = p.newPermissionCollection();

                // 仍然没有 PermissionCollection？
                // 我们将为他们提供一个 PermissionsHash。
                if (pc == null)
                    pc = new PermissionsHash();
            }

            if (pc != null) {
                permsMap.put(c, pc);
            }
        }
        return pc;
    }

    /**
     * 解析类型为 p 的任何未解析权限。
     *
     * @param p 要解析的未解析权限的类型
     *
     * @return 包含未解析权限的 PermissionCollection，如果类型为 p 的未解析权限不存在，则返回 null。
     *
     */
    private PermissionCollection getUnresolvedPermissions(Permission p)
    {
        // 从同步方法中调用，因此 permsMap 不需要锁

        UnresolvedPermissionCollection uc =
        (UnresolvedPermissionCollection) permsMap.get(UnresolvedPermission.class);

        // 如果 uc 为 null，我们没有未解析的权限
        if (uc == null)
            return null;

        List<UnresolvedPermission> unresolvedPerms =
                                        uc.getUnresolvedPermissions(p);

        // 如果 unresolvedPerms 为 null，我们没有这种类型的未解析权限
        if (unresolvedPerms == null)
            return null;

        java.security.cert.Certificate certs[] = null;

        Object signers[] = p.getClass().getSigners();

        int n = 0;
        if (signers != null) {
            for (int j=0; j < signers.length; j++) {
                if (signers[j] instanceof java.security.cert.Certificate) {
                    n++;
                }
            }
            certs = new java.security.cert.Certificate[n];
            n = 0;
            for (int j=0; j < signers.length; j++) {
                if (signers[j] instanceof java.security.cert.Certificate) {
                    certs[n++] = (java.security.cert.Certificate)signers[j];
                }
            }
        }

        PermissionCollection pc = null;
        synchronized (unresolvedPerms) {
            int len = unresolvedPerms.size();
            for (int i = 0; i < len; i++) {
                UnresolvedPermission up = unresolvedPerms.get(i);
                Permission perm = up.resolve(p, certs);
                if (perm != null) {
                    if (pc == null) {
                        pc = p.newPermissionCollection();
                        if (pc == null)
                            pc = new PermissionsHash();
                    }
                    pc.add(perm);
                }
            }
        }
        return pc;
    }

    private static final long serialVersionUID = 4858622370623524688L;

    // 需要维护与早期版本的序列化互操作性，
    // 这些版本具有可序列化的字段：
    // private Hashtable perms;

    /**
     * @serialField perms java.util.Hashtable
     *     权限类和 PermissionCollections 的表。
     * @serialField allPermission java.security.PermissionCollection
     */
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("perms", Hashtable.class),
        new ObjectStreamField("allPermission", PermissionCollection.class),
    };

    /**
     * @serialData 默认字段。
     */
    /*
     * 将 permsMap 字段的内容作为 Hashtable 写出，以确保与早期版本的序列化兼容。allPermission 保持不变。
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        // 不调用 out.defaultWriteObject()

        // 将 perms 复制到 Hashtable
        Hashtable<Class<?>, PermissionCollection> perms =
            new Hashtable<>(permsMap.size()*2); // 无需同步；估计
        synchronized (this) {
            perms.putAll(permsMap);
        }

        // 写出可序列化的字段
        ObjectOutputStream.PutField pfields = out.putFields();

        pfields.put("allPermission", allPermission); // 无需同步；陈旧性可以接受
        pfields.put("perms", perms);
        out.writeFields();
    }

    /*
     * 读取 Class/PermissionCollections 的 Hashtable 并将其保存在 permsMap 字段中。读取 allPermission。
     */
    private void readObject(ObjectInputStream in) throws IOException,
    ClassNotFoundException {
        // 不调用 defaultReadObject()

        // 读取序列化的字段
        ObjectInputStream.GetField gfields = in.readFields();

        // 获取 allPermission
        allPermission = (PermissionCollection) gfields.get("allPermission", null);

        // 获取权限
        // writeObject 将 Hashtable<Class<?>, PermissionCollection> 写入 perms 键，因此此转换是安全的，除非数据损坏。
        @SuppressWarnings("unchecked")
        Hashtable<Class<?>, PermissionCollection> perms =
            (Hashtable<Class<?>, PermissionCollection>)gfields.get("perms", null);
        permsMap = new HashMap<Class<?>, PermissionCollection>(perms.size()*2);
        permsMap.putAll(perms);

        // 设置 hasUnresolved
        UnresolvedPermissionCollection uc =
        (UnresolvedPermissionCollection) permsMap.get(UnresolvedPermission.class);
        hasUnresolved = (uc != null && uc.elements().hasMoreElements());
    }
}

final class PermissionsEnumerator implements Enumeration<Permission> {


                // 所有权限
    private Iterator<PermissionCollection> perms;
    // 当前集合
    private Enumeration<Permission> permset;

    PermissionsEnumerator(Iterator<PermissionCollection> e) {
        perms = e;
        permset = getNextEnumWithMore();
    }

    // 无需同步；调用者应根据需要同步对象
    public boolean hasMoreElements() {
        // 如果我们进入时 permissionimpl 为 null，我们知道
        // 没有更多的了。

        if (permset == null)
            return  false;

        // 尝试查看当前集合中是否还有剩余

        if (permset.hasMoreElements())
            return true;

        // 获取下一个有内容的集合...
        permset = getNextEnumWithMore();

        // 如果为 null，表示已完成！
        return (permset != null);
    }

    // 无需同步；调用者应根据需要同步对象
    public Permission nextElement() {

        // hasMoreElements 将更新 permset 到下一个有内容的 permset...

        if (hasMoreElements()) {
            return permset.nextElement();
        } else {
            throw new NoSuchElementException("PermissionsEnumerator");
        }

    }

    private Enumeration<Permission> getNextEnumWithMore() {
        while (perms.hasNext()) {
            PermissionCollection pc = perms.next();
            Enumeration<Permission> next =pc.elements();
            if (next.hasMoreElements())
                return next;
        }
        return null;

    }
}

/**
 * PermissionsHash 在哈希表中存储一组同质权限。
 *
 * @see Permission
 * @see Permissions
 *
 *
 * @author Roland Schemers
 *
 * @serial include
 */

final class PermissionsHash extends PermissionCollection
implements Serializable
{
    /**
     * 键和值是（相同的）权限对象。
     * 不序列化；请参阅类末尾的序列化部分。
     */
    private transient Map<Permission, Permission> permsMap;

    /**
     * 创建一个空的 PermissionsHash 对象。
     */

    PermissionsHash() {
        permsMap = new HashMap<Permission, Permission>(11);
    }

    /**
     * 向 PermissionsHash 中添加一个权限。
     *
     * @param permission 要添加的 Permission 对象。
     */

    public void add(Permission permission) {
        synchronized (this) {
            permsMap.put(permission, permission);
        }
    }

    /**
     * 检查并查看此权限集是否隐含 "permission" 中表达的权限。
     *
     * @param permission 要比较的 Permission 对象
     *
     * @return 如果 "permission" 是权限集中某个权限的适当子集，则返回 true，否则返回 false。
     */

    public boolean implies(Permission permission) {
        // 尝试快速查找和隐含。如果失败
        // 则枚举所有权限。
        synchronized (this) {
            Permission p = permsMap.get(permission);

            // 如果找到权限，则 p.equals(permission)
            if (p == null) {
                for (Permission p_ : permsMap.values()) {
                    if (p_.implies(permission))
                        return true;
                }
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * 返回容器中所有 Permission 对象的枚举。
     *
     * @return 所有权限的枚举。
     */

    public Enumeration<Permission> elements() {
        // 将 Map 值的 Iterator 转换为枚举
        synchronized (this) {
            return Collections.enumeration(permsMap.values());
        }
    }

    private static final long serialVersionUID = -8491988220802933440L;
    // 需要与早期版本保持序列化互操作性，
    // 这些版本具有可序列化的字段：
    // private Hashtable perms;
    /**
     * @serialField perms java.util.Hashtable
     *     权限表（键和值相同）。
     */
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("perms", Hashtable.class),
    };

    /**
     * @serialData 默认字段。
     */
    /*
     * 将 permsMap 字段的内容作为 Hashtable 写出
     * 以保持与早期版本的序列化兼容性。
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        // 不调用 out.defaultWriteObject()

        // 将 perms 复制到 Hashtable
        Hashtable<Permission, Permission> perms =
                new Hashtable<>(permsMap.size()*2);
        synchronized (this) {
            perms.putAll(permsMap);
        }

        // 写出可序列化的字段
        ObjectOutputStream.PutField pfields = out.putFields();
        pfields.put("perms", perms);
        out.writeFields();
    }

    /*
     * 读取 Permission/Permission 的 Hashtable 并将其保存在
     * permsMap 字段中。
     */
    private void readObject(ObjectInputStream in) throws IOException,
    ClassNotFoundException {
        // 不调用 defaultReadObject()

        // 读取序列化的字段
        ObjectInputStream.GetField gfields = in.readFields();

        // 获取权限
        // writeObject 为 perms 键写入 Hashtable<Class<?>, PermissionCollection>，
        // 因此此转换是安全的，除非数据损坏。
        @SuppressWarnings("unchecked")
        Hashtable<Permission, Permission> perms =
                (Hashtable<Permission, Permission>)gfields.get("perms", null);
        permsMap = new HashMap<Permission, Permission>(perms.size()*2);
        permsMap.putAll(perms);
    }
}
