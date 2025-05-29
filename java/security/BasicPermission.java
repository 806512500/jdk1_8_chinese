
/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.security;

import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Collections;
import java.io.ObjectStreamField;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

/**
 * BasicPermission类扩展了Permission类，可以作为希望遵循与BasicPermission相同命名约定的权限的基类。
 * <P>
 * BasicPermission的名称是给定权限的名称（例如，"exit"，"setFactory"，"print.queueJob"等）。命名约定遵循层次属性命名约定。
 * 星号可以单独出现，或者如果紧接在"."之后，可以出现在名称的末尾，表示通配符匹配。例如，"*"和"java.*"表示通配符匹配，而"*java"，"a*b"和"java*"则不表示。
 * <P>
 * 动作字符串（从Permission继承）未使用。因此，BasicPermission通常用作“命名”权限的基类
 * （包含名称但不包含动作列表的权限；你要么拥有命名权限，要么没有。）
 * 子类可以在BasicPermission的基础上实现动作，如果需要的话。
 * <p>
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.PermissionCollection
 * @see java.lang.SecurityManager
 *
 * @author Marianne Mueller
 * @author Roland Schemers
 */

public abstract class BasicPermission extends Permission
    implements java.io.Serializable
{

    private static final long serialVersionUID = 6279438298436773498L;

    // 此权限是否在末尾带有通配符？
    private transient boolean wildcard;

    // 没有末尾通配符的名称
    private transient String path;

    // 此权限是否是旧版的exitVM权限（JDK 1.6之前）？
    private transient boolean exitVM;

    /**
     * 初始化一个BasicPermission对象。所有构造函数的公共部分。
     */
    private void init(String name) {
        if (name == null)
            throw new NullPointerException("name can't be null");

        int len = name.length();

        if (len == 0) {
            throw new IllegalArgumentException("name can't be empty");
        }

        char last = name.charAt(len - 1);

        // 是否是通配符或以".*"结尾？
        if (last == '*' && (len == 1 || name.charAt(len - 2) == '.')) {
            wildcard = true;
            if (len == 1) {
                path = "";
            } else {
                path = name.substring(0, len - 1);
            }
        } else {
            if (name.equals("exitVM")) {
                wildcard = true;
                path = "exitVM.";
                exitVM = true;
            } else {
                path = name;
            }
        }
    }

    /**
     * 创建一个具有指定名称的新BasicPermission。
     * 名称是权限的符号名称，例如
     * "setFactory"，
     * "print.queueJob"或"topLevelWindow"等。
     *
     * @param name BasicPermission的名称。
     *
     * @throws NullPointerException 如果 {@code name} 为 {@code null}。
     * @throws IllegalArgumentException 如果 {@code name} 为空。
     */
    public BasicPermission(String name) {
        super(name);
        init(name);
    }


    /**
     * 创建一个具有指定名称的新BasicPermission对象。
     * 名称是BasicPermission的符号名称，而
     * 动作字符串目前未使用。
     *
     * @param name BasicPermission的名称。
     * @param actions 忽略。
     *
     * @throws NullPointerException 如果 {@code name} 为 {@code null}。
     * @throws IllegalArgumentException 如果 {@code name} 为空。
     */
    public BasicPermission(String name, String actions) {
        super(name);
        init(name);
    }

    /**
     * 检查指定的权限是否被
     * 此对象“隐含”。
     * <P>
     * 更具体地说，如果满足以下条件，此方法返回true：
     * <ul>
     * <li> <i>p</i> 的类与此对象的类相同，且
     * <li> <i>p</i> 的名称等于或（在通配符的情况下）
     *      被此对象的名称隐含。例如，"a.b.*" 隐含 "a.b.c"。
     * </ul>
     *
     * @param p 要检查的权限。
     *
     * @return 如果传递的权限等于或
     * 被此权限隐含，返回true，否则返回false。
     */
    public boolean implies(Permission p) {
        if ((p == null) || (p.getClass() != getClass()))
            return false;

        BasicPermission that = (BasicPermission) p;

        if (this.wildcard) {
            if (that.wildcard) {
                // 一个通配符可以隐含另一个通配符
                return that.path.startsWith(path);
            } else {
                // 确保 that.path 更长，以便 a.b.* 不隐含 a.b
                return (that.path.length() > this.path.length()) &&
                    that.path.startsWith(this.path);
            }
        } else {
            if (that.wildcard) {
                // 非通配符不能隐含通配符
                return false;
            }
            else {
                return this.path.equals(that.path);
            }
        }
    }

    /**
     * 检查两个BasicPermission对象是否相等。
     * 检查 <i>obj</i> 的类是否与此对象的类相同
     * 并且具有与此对象相同的名称。
     * <P>
     * @param obj 要测试是否与此对象相等的对象。
     * @return 如果 <i>obj</i> 的类与此对象的类相同
     *  并且具有与此BasicPermission对象相同的名称，返回true，否则返回false。
     */
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if ((obj == null) || (obj.getClass() != getClass()))
            return false;


                    BasicPermission bp = (BasicPermission) obj;

        return getName().equals(bp.getName());
    }


    /**
     * 返回此对象的哈希码值。
     * 使用的哈希码是名称的哈希码，即，
     * {@code getName().hashCode()}，其中 {@code getName}
     * 来自 Permission 超类。
     *
     * @return 此对象的哈希码值。
     */
    public int hashCode() {
        return this.getName().hashCode();
    }

    /**
     * 返回动作的规范字符串表示形式，
     * 目前是空字符串 ""，因为 BasicPermission 没有动作。
     *
     * @return 空字符串 ""。
     */
    public String getActions() {
        return "";
    }

    /**
     * 返回一个用于存储 BasicPermission
     * 对象的新 PermissionCollection 对象。
     *
     * <p>BasicPermission 对象必须以允许它们按任何顺序插入的方式存储，但也要使
     * PermissionCollection {@code implies} 方法
     * 能够以高效（且一致）的方式实现。
     *
     * @return 一个适合存储 BasicPermissions 的新 PermissionCollection 对象。
     */
    public PermissionCollection newPermissionCollection() {
        return new BasicPermissionCollection(this.getClass());
    }

    /**
     * readObject 用于从流中恢复 BasicPermission 的状态。
     */
    private void readObject(ObjectInputStream s)
         throws IOException, ClassNotFoundException
    {
        s.defaultReadObject();
        // 调用 init 以初始化其余值。
        init(getName());
    }

    /**
     * 返回此 BasicPermission 的规范名称。
     * 所有内部调用 getName 都应调用此方法，以便
     * 使 pre-JDK 1.6 "exitVM" 和当前 "exitVM.*" 权限在 equals/hashCode 方法中等效。
     *
     * @return 此 BasicPermission 的规范名称。
     */
    final String getCanonicalName() {
        return exitVM ? "exitVM.*" : getName();
    }
}

/**
 * BasicPermissionCollection 存储 BasicPermission 权限的集合。BasicPermission 对象
 * 必须以允许它们按任何顺序插入的方式存储，但要使 implies 函数能够高效（且一致）地评估 implies
 * 方法。
 *
 * BasicPermissionCollection 处理比较如 "a.b.c.d.e" 的权限与 "a.b.*" 或 "*" 的权限。
 *
 * @see java.security.Permission
 * @see java.security.Permissions
 *
 *
 * @author Roland Schemers
 *
 * @serial include
 */

final class BasicPermissionCollection
    extends PermissionCollection
    implements java.io.Serializable
{

    private static final long serialVersionUID = 739301742472979399L;

    /**
      * 键是名称，值是权限。集合中的所有权限对象必须是同一类型。
      * 不序列化；请参阅类末尾的序列化部分。
      */
    private transient Map<String, Permission> perms;

    /**
     * 如果此 BasicPermissionCollection 包含一个权限名为 '*' 的 BasicPermission，则此值为 {@code true}。
     *
     * @see #serialPersistentFields
     */
    private boolean all_allowed;

    /**
     * 此 BasicPermissionCollection 中所有 BasicPermissions 所属的类。
     *
     * @see #serialPersistentFields
     */
    private Class<?> permClass;

    /**
     * 创建一个空的 BasicPermissionCollection 对象。
     *
     */

    public BasicPermissionCollection(Class<?> clazz) {
        perms = new HashMap<String, Permission>(11);
        all_allowed = false;
        permClass = clazz;
    }

    /**
     * 向 BasicPermissions 添加一个权限。哈希的键是
     * permission.path。
     *
     * @param permission 要添加的 Permission 对象。
     *
     * @exception IllegalArgumentException - 如果权限不是
     *                                       BasicPermission，或者
     *                                       权限不是此集合中其他
     *                                       权限的同一类。
     *
     * @exception SecurityException - 如果此 BasicPermissionCollection 对象
     *                                已被标记为只读
     */
    public void add(Permission permission) {
        if (! (permission instanceof BasicPermission))
            throw new IllegalArgumentException("invalid permission: "+
                                               permission);
        if (isReadOnly())
            throw new SecurityException("attempt to add a Permission to a readonly PermissionCollection");

        BasicPermission bp = (BasicPermission) permission;

        // 确保只添加同一类的新 BasicPermissions
        // 也检查 null 以与早期版本的反序列化形式兼容。
        if (permClass == null) {
            // 添加第一个权限
            permClass = bp.getClass();
        } else {
            if (bp.getClass() != permClass)
                throw new IllegalArgumentException("invalid permission: " +
                                                permission);
        }

        synchronized (this) {
            perms.put(bp.getCanonicalName(), permission);
        }

        // 不同步 all_allowed；陈旧性可接受
        if (!all_allowed) {
            if (bp.getCanonicalName().equals("*"))
                all_allowed = true;
        }
    }

    /**
     * 检查并查看此权限集是否隐含 "permission" 中表达的权限。
     *
     * @param permission 要比较的 Permission 对象
     *
     * @return 如果 "permission" 是权限集中权限的适当子集，则返回 true，否则返回 false。
     */
    public boolean implies(Permission permission) {
        if (! (permission instanceof BasicPermission))
            return false;

        BasicPermission bp = (BasicPermission) permission;


                    // 随机的 BasicPermission 子类之间不会相互隐含
        if (bp.getClass() != permClass)
            return false;

        // 如果添加了 "*" 权限，则直接返回 true
        if (all_allowed)
            return true;

        // 策略：
        // 首先检查完全匹配。然后沿着路径向上查找
        // 匹配 a.b..*

        String path = bp.getCanonicalName();
        //System.out.println("check "+path);

        Permission x;

        synchronized (this) {
            x = perms.get(path);
        }

        if (x != null) {
            // 我们找到了直接匹配！
            return x.implies(permission);
        }

        // 沿着树向上查找...
        int last, offset;

        offset = path.length()-1;

        while ((last = path.lastIndexOf(".", offset)) != -1) {

            path = path.substring(0, last+1) + "*";
            //System.out.println("check "+path);

            synchronized (this) {
                x = perms.get(path);
            }

            if (x != null) {
                return x.implies(permission);
            }
            offset = last -1;
        }

        // 我们不需要检查 "*"，因为已经在顶部检查过了
        // (all_allowed)，所以直接返回 false
        return false;
    }

    /**
     * 返回容器中所有 BasicPermission 对象的枚举。
     *
     * @return 所有 BasicPermission 对象的枚举。
     */
    public Enumeration<Permission> elements() {
        // 将 Map 值的迭代器转换为枚举
        synchronized (this) {
            return Collections.enumeration(perms.values());
        }
    }

    // 需要保持与早期版本的序列化互操作性，
    // 这些版本具有可序列化的字段：
    //
    // @serial Hashtable 以 BasicPermission 名称为索引
    //
    // private Hashtable permissions;
    /**
     * @serialField permissions java.util.Hashtable
     *    该 BasicPermissionCollection 中的 BasicPermissions。
     *    集合中的所有 BasicPermissions 必须属于同一类。
     *    Hashtable 以 BasicPermission 名称为索引；Hashtable 条目的值为权限。
     * @serialField all_allowed boolean
     *   如果此 BasicPermissionCollection 包含一个权限名称为 '*' 的 BasicPermission，则设置为 {@code true}。
     * @serialField permClass java.lang.Class
     *   该 BasicPermissionCollection 中所有 BasicPermissions 所属的类。
     */
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("permissions", Hashtable.class),
        new ObjectStreamField("all_allowed", Boolean.TYPE),
        new ObjectStreamField("permClass", Class.class),
    };

    /**
     * @serialData 默认字段。
     */
    /*
     * 将 perms 字段的内容作为 Hashtable 写出，以保持与早期版本的序列化兼容性。all_allowed
     * 和 permClass 保持不变。
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        // 不调用 out.defaultWriteObject()

        // 将 perms 复制到 Hashtable
        Hashtable<String, Permission> permissions =
                new Hashtable<>(perms.size()*2);

        synchronized (this) {
            permissions.putAll(perms);
        }

        // 写出可序列化的字段
        ObjectOutputStream.PutField pfields = out.putFields();
        pfields.put("all_allowed", all_allowed);
        pfields.put("permissions", permissions);
        pfields.put("permClass", permClass);
        out.writeFields();
    }

    /**
     * readObject 被调用来从流中恢复 BasicPermissionCollection 的状态。
     */
    private void readObject(java.io.ObjectInputStream in)
         throws IOException, ClassNotFoundException
    {
        // 不调用 defaultReadObject()

        // 读取序列化的字段
        ObjectInputStream.GetField gfields = in.readFields();

        // 获取 permissions
        // writeObject 将 permissions 键写为 Hashtable<String, Permission>，因此这个类型转换是安全的，除非数据损坏。
        @SuppressWarnings("unchecked")
        Hashtable<String, Permission> permissions =
                (Hashtable<String, Permission>)gfields.get("permissions", null);
        perms = new HashMap<String, Permission>(permissions.size()*2);
        perms.putAll(permissions);

        // 获取 all_allowed
        all_allowed = gfields.get("all_allowed", false);

        // 获取 permClass
        permClass = (Class<?>) gfields.get("permClass", null);

        if (permClass == null) {
            // 设置 permClass
            Enumeration<Permission> e = permissions.elements();
            if (e.hasMoreElements()) {
                Permission p = e.nextElement();
                permClass = p.getClass();
            }
        }
    }
}
