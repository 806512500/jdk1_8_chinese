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

/**
 * 抽象类，用于表示对系统资源的访问。
 * 所有权限都有一个名称（其解释取决于子类），
 * 以及定义特定权限子类语义的抽象函数。
 *
 * <p>大多数权限对象还包括一个“操作”列表，用于指定允许的操作。
 * 例如，对于 {@code java.io.FilePermission} 对象，权限名称是
 * 文件（或目录）的路径，操作列表（如“read, write”）指定了
 * 对指定文件（或目录中的文件）允许的操作。
 * 操作列表对于某些权限对象（如 {@code java.lang.RuntimePermission}）是可选的；
 * 你要么有命名的权限（如“system.exit”），要么没有。
 *
 * <p>每个子类必须实现的一个重要方法是
 * {@code implies} 方法，用于比较权限。基本上，
 * “权限 p1 暗示权限 p2”意味着
 * 如果授予了权限 p1，则自然授予了权限 p2。
 * 因此，这不是一个等价测试，而更像是一种子集测试。
 *
 * <P> 权限对象类似于字符串对象，一旦创建就不可变。子类不应
 * 提供可以改变权限状态的方法。
 *
 * @see Permissions
 * @see PermissionCollection
 *
 *
 * @author Marianne Mueller
 * @author Roland Schemers
 */

public abstract class Permission implements Guard, java.io.Serializable {

    private static final long serialVersionUID = -5636570222231596674L;

    private String name;

    /**
     * 使用指定的名称构造权限。
     *
     * @param name 要创建的权限对象的名称。
     *
     */

    public Permission(String name) {
        this.name = name;
    }

    /**
     * 为权限实现守护接口。调用
     * {@code SecurityManager.checkPermission} 方法，
     * 将此权限对象作为要检查的权限传递。
     * 如果允许访问，则静默返回。否则，抛出
 * 安全异常。
     *
     * @param object 被守护的对象（当前被忽略）。
     *
     * @throws SecurityException
     *        如果存在安全经理，并且其
     *        {@code checkPermission} 方法不允许访问。
     *
     * @see Guard
     * @see GuardedObject
     * @see SecurityManager#checkPermission
     *
     */
    public void checkGuard(Object object) throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(this);
    }

    /**
     * 检查指定权限的操作是否被
     * 此对象的操作“暗示”。
     * <P>
     * 这必须由权限的子类实现，因为它们是唯一可以
     * 对权限对象施加语义的类。
     *
     * <p>{@code implies} 方法由 AccessController 使用，以确定
     * 是否请求的权限被当前执行上下文中已知有效的权限所暗示。
     *
     * @param permission 要检查的权限。
     *
     * @return 如果指定的权限被此对象暗示，则返回 true，否则返回 false。
     */

    public abstract boolean implies(Permission permission);

    /**
     * 检查两个权限对象是否相等。
     * <P>
     * 不要使用 {@code equals} 方法进行访问控制决策；
     * 使用 {@code implies} 方法。
     *
     * @param obj 要测试是否与此对象相等的对象。
     *
     * @return 如果两个权限对象相等，则返回 true。
     */

    public abstract boolean equals(Object obj);

    /**
     * 返回此权限对象的哈希码值。
     * <P>
     * 权限对象的 {@code hashCode} 行为要求如下：
     * <ul>
     * <li>在同一 Java 应用程序的执行过程中多次调用
     *     {@code hashCode} 方法时，必须始终返回相同的整数。
     *     这个整数不必在应用程序的一次执行与另一次执行之间保持一致。
     * <li>如果两个权限对象根据
     *     {@code equals}
     *     方法相等，则调用两个权限对象的 {@code hashCode} 方法必须产生相同的整数结果。
     * </ul>
     *
     * @return 此对象的哈希码值。
     */

    public abstract int hashCode();

    /**
     * 返回此权限的名称。
     * 例如，对于 {@code java.io.FilePermission}，
     * 名称将是一个路径名。
     *
     * @return 此权限的名称。
     *
     */

    public final String getName() {
        return name;
    }

    /**
     * 以字符串形式返回操作。这是抽象的
     * 以便子类可以推迟创建字符串表示，直到需要时。
     * 子类应始终返回它们认为的
     * 规范形式的操作。例如，通过以下方式创建的两个 FilePermission 对象：
     *
     * <pre>
     *   perm1 = new FilePermission(p1,"read,write");
     *   perm2 = new FilePermission(p2,"write,read");
     * </pre>
     *
     * 调用 {@code getActions} 方法时都会返回
     * "read,write"。
     *
     * @return 此权限的操作。
     *
     */

    public abstract String getActions();

    /**
     * 返回给定权限对象的空 PermissionCollection，或如果未定义则返回 null。
     * 权限类的子类应
     * 覆盖此方法，如果它们需要将权限存储在特定的
     * PermissionCollection 对象中，以在调用
     * {@code PermissionCollection.implies} 方法时提供正确的语义。
     * 如果返回 null，
     * 则此方法的调用者可以自由选择将此类型的权限存储在任何 PermissionCollection 中（使用 Hashtable、Vector 等）。
     *
     * @return 用于此类型权限的新 PermissionCollection 对象，或如果未定义则返回 null。
     */

    public PermissionCollection newPermissionCollection() {
        return null;
    }

    /**
     * 返回描述此权限的字符串。约定是
     * 指定类名、权限名称和操作，格式如下：'("ClassName" "name" "actions")'，或
     * 如果操作列表为空或 null，则为 '("ClassName" "name")'。
     *
     * @return 有关此权限的信息。
     */
    public String toString() {
        String actions = getActions();
        if ((actions == null) || (actions.length() == 0)) { // 可选
            return "(\"" + getClass().getName() + "\" \"" + name + "\")";
        } else {
            return "(\"" + getClass().getName() + "\" \"" + name +
                 "\" \"" + actions + "\")";
        }
    }
}
