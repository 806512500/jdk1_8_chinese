
/*
 * 版权所有 (c) 1997, 2013, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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

package java.util;

import java.io.Serializable;
import java.io.IOException;
import java.security.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Collections;
import java.io.ObjectStreamField;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import sun.security.util.SecurityConstants;

/**
 * 本类用于属性权限。
 *
 * <P>
 * 名称是属性的名称（"java.home"，"os.name" 等）。命名
 * 约定遵循分层属性命名约定。
 * 另外，名称的末尾可以出现星号，跟随一个 "."，或单独出现，
 * 以表示通配符匹配。例如："java.*" 和 "*" 表示通配符
 * 匹配，而 "*java" 和 "a*b" 则不表示。
 * <P>
 * 要授予的权限操作在构造函数中以包含一个或多个逗号分隔的关键词的字符串形式传递。可能的关键词是
 * "read" 和 "write"。它们的含义定义如下：
 *
 * <DL>
 *    <DT> read
 *    <DD> 读取权限。允许调用 <code>System.getProperty</code>。
 *    <DT> write
 *    <DD> 写入权限。允许调用 <code>System.setProperty</code>。
 * </DL>
 * <P>
 * 操作字符串在处理前转换为小写。
 * <P>
 * 在授予代码访问某些系统属性的权限时应谨慎。例如，授予访问 "java.home" 系统属性的权限会给潜在的恶意代码提供关于系统环境（Java 安装目录）的敏感信息。同样，授予访问 "user.name" 和 "user.home" 系统属性的权限会给潜在的恶意代码提供关于用户环境（用户的账户名和主目录）的敏感信息。
 *
 * @see java.security.BasicPermission
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.PermissionCollection
 * @see java.lang.SecurityManager
 *
 *
 * @author Roland Schemers
 * @since 1.2
 *
 * @serial exclude
 */

public final class PropertyPermission extends BasicPermission {

    /**
     * 读取操作。
     */
    private final static int READ    = 0x1;

    /**
     * 写入操作。
     */
    private final static int WRITE   = 0x2;
    /**
     * 所有操作（读取，写入）；
     */
    private final static int ALL     = READ|WRITE;
    /**
     * 无操作。
     */
    private final static int NONE    = 0x0;

    /**
     * 操作掩码。
     *
     */
    private transient int mask;

    /**
     * 操作字符串。
     *
     * @serial
     */
    private String actions; // 尽可能长时间保持为空，然后
                            // 在 getAction 函数中创建并重用。

    /**
     * 初始化 PropertyPermission 对象。所有构造函数的公共部分。
     * 反序列化期间也会调用。
     *
     * @param mask 要使用的操作掩码。
     *
     */
    private void init(int mask) {
        if ((mask & ALL) != mask)
            throw new IllegalArgumentException("无效的操作掩码");

        if (mask == NONE)
            throw new IllegalArgumentException("无效的操作掩码");

        if (getName() == null)
            throw new NullPointerException("名称不能为空");

        this.mask = mask;
    }

    /**
     * 使用指定名称创建一个新的 PropertyPermission 对象。
     * 名称是系统属性的名称，<i>actions</i> 包含一个以逗号分隔的列表，列出了
     * 要授予的属性操作。可能的操作是
     * "read" 和 "write"。
     *
     * @param name PropertyPermission 的名称。
     * @param actions 操作字符串。
     *
     * @throws NullPointerException 如果 <code>name</code> 为 <code>null</code>。
     * @throws IllegalArgumentException 如果 <code>name</code> 为空或 <code>actions</code> 无效。
     */
    public PropertyPermission(String name, String actions) {
        super(name,actions);
        init(getMask(actions));
    }

    /**
     * 检查此 PropertyPermission 对象是否 "隐含" 指定的
     * 权限。
     * <P>
     * 更具体地说，如果满足以下条件，此方法返回 true：
     * <ul>
     * <li> <i>p</i> 是 PropertyPermission 的实例，
     * <li> <i>p</i> 的操作是此
     * 对象操作的子集，且
     * <li> <i>p</i> 的名称由此对象的
     * 名称隐含。例如，"java.*" 隐含 "java.home"。
     * </ul>
     * @param p 要检查的权限。
     *
     * @return 如果指定的权限由此对象隐含，则返回 true，否则返回 false。
     */
    public boolean implies(Permission p) {
        if (!(p instanceof PropertyPermission))
            return false;

        PropertyPermission that = (PropertyPermission) p;

        // 我们获取有效的掩码。即，此对象和 that 对象的 "与"。
        // 它们必须等于 that.mask 才能使 implies 返回 true。

        return ((this.mask & that.mask) == that.mask) && super.implies(that);
    }

    /**
     * 检查两个 PropertyPermission 对象是否相等。检查 <i>obj</i> 是否
     * 是 PropertyPermission，并且具有与此对象相同的名称和操作。
     * <P>
     * @param obj 要与本对象进行相等性测试的对象。
     * @return 如果 obj 是 PropertyPermission，并且具有与此 PropertyPermission 对象相同的名称和
     * 操作，则返回 true。
     */
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (! (obj instanceof PropertyPermission))
            return false;

        PropertyPermission that = (PropertyPermission) obj;

        return (this.mask == that.mask) &&
            (this.getName().equals(that.getName()));
    }

    /**
     * 返回此对象的哈希码值。
     * 使用的哈希码是此权限名称的哈希码，即，
     * <code>getName().hashCode()</code>，其中 <code>getName</code> 来自
     * Permission 超类。
     *
     * @return 此对象的哈希码值。
     */
    public int hashCode() {
        return this.getName().hashCode();
    }


/**
 * 将动作字符串转换为动作掩码。
 *
 * @param actions 动作字符串。
 * @return 动作掩码。
 */
private static int getMask(String actions) {

    int mask = NONE;

    if (actions == null) {
        return mask;
    }

    // 使用对象身份比较已知的内部字符串以提高性能（这些值在JDK中大量使用）。
    if (actions == SecurityConstants.PROPERTY_READ_ACTION) {
        return READ;
    } if (actions == SecurityConstants.PROPERTY_WRITE_ACTION) {
        return WRITE;
    } else if (actions == SecurityConstants.PROPERTY_RW_ACTION) {
        return READ|WRITE;
    }

    char[] a = actions.toCharArray();

    int i = a.length - 1;
    if (i < 0)
        return mask;

    while (i != -1) {
        char c;

        // 跳过空白字符
        while ((i!=-1) && ((c = a[i]) == ' ' ||
                           c == '\r' ||
                           c == '\n' ||
                           c == '\f' ||
                           c == '\t'))
            i--;

        // 检查已知字符串
        int matchlen;

        if (i >= 3 && (a[i-3] == 'r' || a[i-3] == 'R') &&
                        (a[i-2] == 'e' || a[i-2] == 'E') &&
                        (a[i-1] == 'a' || a[i-1] == 'A') &&
                        (a[i] == 'd' || a[i] == 'D'))
        {
            matchlen = 4;
            mask |= READ;

        } else if (i >= 4 && (a[i-4] == 'w' || a[i-4] == 'W') &&
                             (a[i-3] == 'r' || a[i-3] == 'R') &&
                             (a[i-2] == 'i' || a[i-2] == 'I') &&
                             (a[i-1] == 't' || a[i-1] == 'T') &&
                             (a[i] == 'e' || a[i] == 'E'))
        {
            matchlen = 5;
            mask |= WRITE;

        } else {
            // 解析错误
            throw new IllegalArgumentException(
                    "无效的权限: " + actions);
        }

        // 确保我们没有匹配到单词的尾部，例如 "ackbarfaccept"。同时，跳到逗号。
        boolean seencomma = false;
        while (i >= matchlen && !seencomma) {
            switch(a[i-matchlen]) {
            case ',':
                seencomma = true;
                break;
            case ' ': case '\r': case '\n':
            case '\f': case '\t':
                break;
            default:
                throw new IllegalArgumentException(
                        "无效的权限: " + actions);
            }
            i--;
        }

        // 将 i 指向逗号前一个位置（或 -1）。
        i -= matchlen;
    }

    return mask;
}


/**
 * 返回动作的规范字符串表示形式。
 * 始终按以下顺序返回存在的动作：
 * 读取，写入。
 *
 * @return 动作的规范字符串表示形式。
 */
static String getActions(int mask) {
    StringBuilder sb = new StringBuilder();
    boolean comma = false;

    if ((mask & READ) == READ) {
        comma = true;
        sb.append("read");
    }

    if ((mask & WRITE) == WRITE) {
        if (comma) sb.append(',');
        else comma = true;
        sb.append("write");
    }
    return sb.toString();
}

/**
 * 返回动作的“规范字符串表示形式”。
 * 即，此方法始终按以下顺序返回存在的动作：
 * 读取，写入。例如，如果此 PropertyPermission 对象
 * 允许读取和写入动作，调用 <code>getActions</code>
 * 将返回字符串 "read,write"。
 *
 * @return 动作的规范字符串表示形式。
 */
public String getActions() {
    if (actions == null)
        actions = getActions(this.mask);

    return actions;
}

/**
 * 返回当前的动作掩码。
 * 由 PropertyPermissionCollection 使用
 *
 * @return 动作掩码。
 */
int getMask() {
    return mask;
}

/**
 * 返回一个用于存储 PropertyPermission 对象的新 PermissionCollection 对象。
 * <p>
 *
 * @return 一个适合存储 PropertyPermissions 的新 PermissionCollection 对象。
 */
public PermissionCollection newPermissionCollection() {
    return new PropertyPermissionCollection();
}


private static final long serialVersionUID = 885438825399942851L;

/**
 * WriteObject 被调用来将 PropertyPermission 的状态保存到流中。动作被序列化，超类
 * 负责名称。
 */
private synchronized void writeObject(java.io.ObjectOutputStream s)
    throws IOException
{
    // 写出动作。超类负责名称
    // 调用 getActions 以确保 actions 字段已初始化
    if (actions == null)
        getActions();
    s.defaultWriteObject();
}

/**
 * readObject 被调用来从流中恢复 PropertyPermission 的状态。
 */
private synchronized void readObject(java.io.ObjectInputStream s)
     throws IOException, ClassNotFoundException
{
    // 读取动作，然后初始化其余部分
    s.defaultReadObject();
    init(getMask(actions));
}
}

/**
 * PropertyPermissionCollection 存储一组 PropertyPermission
 * 权限。
 *
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.PermissionCollection
 *
 *
 * @author Roland Schemers
 *
 * @serial include
 */
final class PropertyPermissionCollection extends PermissionCollection
    implements Serializable
{

    /**
     * 键是属性名称；值是 PropertyPermission。
     * 不序列化；请参阅类末尾的序列化部分。
     */
    private transient Map<String, PropertyPermission> perms;

                /**
     * 布尔值，表示集合中是否包含 "*"。
     *
     * @see #serialPersistentFields
     */
    // 无需同步访问；允许此值过期。
    private boolean all_allowed;

    /**
     * 创建一个空的 PropertyPermissionCollection 对象。
     */
    public PropertyPermissionCollection() {
        perms = new HashMap<>(32);     // 默认策略的容量
        all_allowed = false;
    }

    /**
     * 向 PropertyPermissions 中添加一个权限。哈希的键是名称。
     *
     * @param permission 要添加的权限对象。
     *
     * @exception IllegalArgumentException - 如果权限不是 PropertyPermission
     *
     * @exception SecurityException - 如果此 PropertyPermissionCollection
     *                                对象已被标记为只读
     */
    public void add(Permission permission) {
        if (! (permission instanceof PropertyPermission))
            throw new IllegalArgumentException("无效的权限: "+
                                               permission);
        if (isReadOnly())
            throw new SecurityException(
                "尝试向只读 PermissionCollection 添加权限");

        PropertyPermission pp = (PropertyPermission) permission;
        String propName = pp.getName();

        synchronized (this) {
            PropertyPermission existing = perms.get(propName);

            if (existing != null) {
                int oldMask = existing.getMask();
                int newMask = pp.getMask();
                if (oldMask != newMask) {
                    int effective = oldMask | newMask;
                    String actions = PropertyPermission.getActions(effective);
                    perms.put(propName, new PropertyPermission(propName, actions));
                }
            } else {
                perms.put(propName, pp);
            }
        }

        if (!all_allowed) {
            if (propName.equals("*"))
                all_allowed = true;
        }
    }

    /**
     * 检查并查看此权限集是否隐含 "permission" 表达的权限。
     *
     * @param permission 要比较的权限对象。
     *
     * @return 如果 "permission" 是权限集中权限的适当子集，则返回 true，否则返回 false。
     */
    public boolean implies(Permission permission) {
        if (! (permission instanceof PropertyPermission))
                return false;

        PropertyPermission pp = (PropertyPermission) permission;
        PropertyPermission x;

        int desired = pp.getMask();
        int effective = 0;

        // 如果添加了 "*" 权限，则进行短路处理
        if (all_allowed) {
            synchronized (this) {
                x = perms.get("*");
            }
            if (x != null) {
                effective |= x.getMask();
                if ((effective & desired) == desired)
                    return true;
            }
        }

        // 策略：
        // 首先检查完全匹配。然后逐步向上查找 a.b.* 的匹配项。

        String name = pp.getName();
        //System.out.println("check "+name);

        synchronized (this) {
            x = perms.get(name);
        }

        if (x != null) {
            // 我们有直接命中！
            effective |= x.getMask();
            if ((effective & desired) == desired)
                return true;
        }

        // 逐步向上遍历...
        int last, offset;

        offset = name.length()-1;

        while ((last = name.lastIndexOf(".", offset)) != -1) {

            name = name.substring(0, last+1) + "*";
            //System.out.println("check "+name);
            synchronized (this) {
                x = perms.get(name);
            }

            if (x != null) {
                effective |= x.getMask();
                if ((effective & desired) == desired)
                    return true;
            }
            offset = last -1;
        }

        // 不需要检查 "*"，因为已经在顶部检查过（all_allowed），所以直接返回 false
        return false;
    }

    /**
     * 返回容器中所有 PropertyPermission 对象的枚举。
     *
     * @return 所有 PropertyPermission 对象的枚举。
     */
    @SuppressWarnings("unchecked")
    public Enumeration<Permission> elements() {
        // 将 Map 值的迭代器转换为枚举
        synchronized (this) {
            /**
             * 转换为原始类型，因为 Enumeration<PropertyPermission>
             * 不能直接转换为 Enumeration<Permission>
             */
            return (Enumeration)Collections.enumeration(perms.values());
        }
    }

    private static final long serialVersionUID = 7015263904581634791L;

    // 需要保持与早期版本的序列化互操作性，
    // 这些版本具有可序列化的字段：
    //
    // 权限表。
    //
    // @serial
    //
    // private Hashtable permissions;
    /**
     * @serialField permissions java.util.Hashtable
     *     PropertyPermissions 的表。
     * @serialField all_allowed boolean
     *     布尔值，表示集合中是否包含 "*"。
     */
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("permissions", Hashtable.class),
        new ObjectStreamField("all_allowed", Boolean.TYPE),
    };

    /**
     * @serialData 默认字段。
     */
    /*
     * 将 perms 字段的内容写入 Hashtable，以确保与早期版本的序列化兼容。all_allowed 保持不变。
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
        out.writeFields();
    }

                /*
     * 读取 PropertyPermissions 的 Hashtable 并将其保存在 perms 字段中。读取 all_allowed。
     */
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        // 不调用 defaultReadObject()

        // 读取序列化的字段
        ObjectInputStream.GetField gfields = in.readFields();

        // 获取 all_allowed
        all_allowed = gfields.get("all_allowed", false);

        // 获取权限
        @SuppressWarnings("unchecked")
        Hashtable<String, PropertyPermission> permissions =
            (Hashtable<String, PropertyPermission>)gfields.get("permissions", null);
        perms = new HashMap<>(permissions.size()*2);
        perms.putAll(permissions);
    }
}
