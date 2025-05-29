
/*
 * 版权所有 (c) 2007, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.nio.file.attribute;

import java.util.*;

/**
 * 访问控制列表 (ACL) 中的一个条目。
 *
 * <p> 由此类表示的 ACL 条目基于 <a href="http://www.ietf.org/rfc/rfc3530.txt"><i>RFC&nbsp;3530:
 * 网络文件系统 (NFS) 版本 4 协议</i></a> 中指定的 ACL 模型。每个条目有四个组件，如下所示：
 *
 * <ol>
 *    <li><p> {@link #type() 类型} 组件确定条目是授予还是拒绝访问。 </p></li>
 *
 *    <li><p> {@link #principal() 主体} 组件，有时称为“谁”组件，是一个 {@link UserPrincipal}，对应于条目授予或拒绝访问的身份
 *    </p></li>
 *
 *    <li><p> {@link #permissions 权限} 组件是一组 {@link AclEntryPermission 权限}
 *    </p></li>
 *
 *    <li><p> {@link #flags 标志} 组件是一组 {@link AclEntryFlag 标志}，用于指示条目如何继承和传播 </p></li>
 * </ol>
 *
 * <p> 使用关联的 {@link Builder} 对象通过调用其 {@link Builder#build 构建} 方法来创建 ACL 条目。
 *
 * <p> ACL 条目是不可变的，并且可以安全地由多个并发线程使用。
 *
 * @since 1.7
 */

public final class AclEntry {

    private final AclEntryType type;
    private final UserPrincipal who;
    private final Set<AclEntryPermission> perms;
    private final Set<AclEntryFlag> flags;

    // 缓存的哈希码
    private volatile int hash;

    // 私有构造函数
    private AclEntry(AclEntryType type,
                     UserPrincipal who,
                     Set<AclEntryPermission> perms,
                     Set<AclEntryFlag> flags)
    {
        this.type = type;
        this.who = who;
        this.perms = perms;
        this.flags = flags;
    }

    /**
     * {@link AclEntry} 对象的构建器。
     *
     * <p> 通过调用 {@code AclEntry} 类定义的 {@link
     * AclEntry#newBuilder newBuilder} 方法之一获取 {@code Builder} 对象。
     *
     * <p> 构建器对象是可变的，并且在没有适当的同步的情况下，不安全用于多个并发线程。
     *
     * @since 1.7
     */
    public static final class Builder {
        private AclEntryType type;
        private UserPrincipal who;
        private Set<AclEntryPermission> perms;
        private Set<AclEntryFlag> flags;

        private Builder(AclEntryType type,
                        UserPrincipal who,
                        Set<AclEntryPermission> perms,
                        Set<AclEntryFlag> flags)
        {
            assert perms != null && flags != null;
            this.type = type;
            this.who = who;
            this.perms = perms;
            this.flags = flags;
        }

        /**
         * 从构建器的组件构造一个 {@link AclEntry}。类型和主体组件是必需的，以便构造一个 {@code AclEntry}。
         *
         * @return 一个新的 ACL 条目
         *
         * @throws  IllegalStateException
         *          如果类型或主体组件未设置
         */
        public AclEntry build() {
            if (type == null)
                throw new IllegalStateException("缺少类型组件");
            if (who == null)
                throw new IllegalStateException("缺少主体组件");
            return new AclEntry(type, who, perms, flags);
        }

        /**
         * 设置构建器的类型组件。
         *
         * @param   type  组件类型
         * @return  该构建器
         */
        public Builder setType(AclEntryType type) {
            if (type == null)
                throw new NullPointerException();
            this.type = type;
            return this;
        }

        /**
         * 设置构建器的主体组件。
         *
         * @param   who  主体组件
         * @return  该构建器
         */
        public Builder setPrincipal(UserPrincipal who) {
            if (who == null)
                throw new NullPointerException();
            this.who = who;
            return this;
        }

        // 检查集合中只包含给定类型的元素
        private static void checkSet(Set<?> set, Class<?> type) {
            for (Object e: set) {
                if (e == null)
                    throw new NullPointerException();
                type.cast(e);
            }
        }

        /**
         * 设置构建器的权限组件。返回时，构建器的权限组件是给定集合的副本。
         *
         * @param   perms  权限组件
         * @return  该构建器
         *
         * @throws  ClassCastException
         *          如果集合包含不是 {@code
         *          AclEntryPermission} 类型的元素
         */
        public Builder setPermissions(Set<AclEntryPermission> perms) {
            if (perms.isEmpty()) {
                // EnumSet.copyOf 不允许空集合
                perms = Collections.emptySet();
            } else {
                // 复制并检查错误元素
                perms = EnumSet.copyOf(perms);
                checkSet(perms, AclEntryPermission.class);
            }

            this.perms = perms;
            return this;
        }

        /**
         * 设置构建器的权限组件。返回时，构建器的权限组件是给定数组中权限的副本。
         *
         * @param   perms  权限组件
         * @return  该构建器
         */
        public Builder setPermissions(AclEntryPermission... perms) {
            Set<AclEntryPermission> set = EnumSet.noneOf(AclEntryPermission.class);
            // 复制并检查空元素
            for (AclEntryPermission p: perms) {
                if (p == null)
                    throw new NullPointerException();
                set.add(p);
            }
            this.perms = set;
            return this;
        }


                    /**
         * 设置此构建器的标志组件。返回时，此构建器的标志组件是给定集合的副本。
         *
         * @param   flags  标志组件
         * @return  此构建器
         *
         * @throws  ClassCastException
         *          如果集合包含不是 {@code AclEntryFlag} 类型的元素
         */
        public Builder setFlags(Set<AclEntryFlag> flags) {
            if (flags.isEmpty()) {
                // EnumSet.copyOf 不允许空集合
                flags = Collections.emptySet();
            } else {
                // 复制并检查错误元素
                flags = EnumSet.copyOf(flags);
                checkSet(flags, AclEntryFlag.class);
            }

            this.flags = flags;
            return this;
        }

        /**
         * 设置此构建器的标志组件。返回时，此构建器的标志组件是给定数组中的标志的副本。
         *
         * @param   flags  标志组件
         * @return  此构建器
         */
        public Builder setFlags(AclEntryFlag... flags) {
            Set<AclEntryFlag> set = EnumSet.noneOf(AclEntryFlag.class);
            // 复制并检查空元素
            for (AclEntryFlag f: flags) {
                if (f == null)
                    throw new NullPointerException();
                set.add(f);
            }
            this.flags = set;
            return this;
        }
    }

    /**
     * 构造一个新的构建器。类型的初始值和 who 组件为 {@code null}。权限和标志组件的初始值为空集合。
     *
     * @return  一个新的构建器
     */
    public static Builder newBuilder() {
        Set<AclEntryPermission> perms = Collections.emptySet();
        Set<AclEntryFlag> flags = Collections.emptySet();
        return new Builder(null, null, perms, flags);
    }

    /**
     * 使用现有 ACL 条目的组件构造一个新的构建器。
     *
     * @param   entry  一个 ACL 条目
     * @return  一个新的构建器
     */
    public static Builder newBuilder(AclEntry entry) {
        return new Builder(entry.type, entry.who, entry.perms, entry.flags);
    }

    /**
     * 返回 ACL 条目类型。
     *
     * @return ACL 条目类型
     */
    public AclEntryType type() {
        return type;
    }

    /**
     * 返回主体组件。
     *
     * @return 主体组件
     */
    public UserPrincipal principal() {
        return who;
    }

    /**
     * 返回权限组件的副本。
     *
     * <p> 返回的集合是权限的可修改副本。
     *
     * @return 权限组件
     */
    public Set<AclEntryPermission> permissions() {
        return new HashSet<AclEntryPermission>(perms);
    }

    /**
     * 返回标志组件的副本。
     *
     * <p> 返回的集合是标志的可修改副本。
     *
     * @return 标志组件
     */
    public Set<AclEntryFlag> flags() {
        return new HashSet<AclEntryFlag>(flags);
    }

    /**
     * 将指定对象与此 ACL 条目进行相等性比较。
     *
     * <p> 如果给定对象不是 {@code AclEntry}，则此方法立即返回 {@code false}。
     *
     * <p> 要认为两个 ACL 条目相等，需要它们类型相同，who 组件相等，权限组件相等，标志组件相等。
     *
     * <p> 此方法满足 {@link java.lang.Object#equals(Object) Object.equals} 方法的一般约定。 </p>
     *
     * @param   ob   要与此对象进行比较的对象
     *
     * @return  如果给定对象是一个与本 ACL 条目相同的 AclEntry，则返回 {@code true}
     */
    @Override
    public boolean equals(Object ob) {
        if (ob == this)
            return true;
        if (ob == null || !(ob instanceof AclEntry))
            return false;
        AclEntry other = (AclEntry)ob;
        if (this.type != other.type)
            return false;
        if (!this.who.equals(other.who))
            return false;
        if (!this.perms.equals(other.perms))
            return false;
        if (!this.flags.equals(other.flags))
            return false;
        return true;
    }

    private static int hash(int h, Object o) {
        return h * 127 + o.hashCode();
    }

    /**
     * 返回此 ACL 条目的哈希码值。
     *
     * <p> 此方法满足 {@link Object#hashCode} 方法的一般约定。
     */
    @Override
    public int hashCode() {
        // 如果可用，返回缓存的哈希值
        if (hash != 0)
            return hash;
        int h = type.hashCode();
        h = hash(h, who);
        h = hash(h, perms);
        h = hash(h, flags);
        hash = h;
        return hash;
    }

    /**
     * 返回此 ACL 条目的字符串表示形式。
     *
     * @return  此条目的字符串表示形式
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // who
        sb.append(who.getName());
        sb.append(':');

        // permissions
        for (AclEntryPermission perm: perms) {
            sb.append(perm.name());
            sb.append('/');
        }
        sb.setLength(sb.length()-1); // 删除最后一个斜杠
        sb.append(':');

        // flags
        if (!flags.isEmpty()) {
            for (AclEntryFlag flag: flags) {
                sb.append(flag.name());
                sb.append('/');
            }
            sb.setLength(sb.length()-1);  // 删除最后一个斜杠
            sb.append(':');
        }

        // type
        sb.append(type.name());
        return sb.toString();
    }
}
