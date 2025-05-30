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

package java.security;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Properties;

/**
 * <p>此类表示身份的范围。它本身是一个身份，因此具有名称和范围。它还可以选择具有公钥和关联的证书。
 *
 * <p>IdentityScope 可以包含所有类型的身份对象，包括签名者。所有类型的 Identity 对象都可以使用相同的方法进行检索、添加和删除。请注意，不同的身份范围可能会
 * 对其各种操作应用不同的策略。
 *
 * <p>密钥和身份之间存在一对一的映射关系，并且每个范围中只能有一个密钥的副本。例如，假设 <b>Acme Software, Inc</b> 是一个用户已知的软件发行商。
 * 假设它是一个身份，即它具有公钥和一组关联的证书。它在范围中使用名称 "Acme Software" 进行命名。范围中的其他任何命名身份都不会具有相同的
 * 公钥。当然，也没有相同的名称。
 *
 * @see Identity
 * @see Signer
 * @see Principal
 * @see Key
 *
 * @author Benjamin Renaud
 *
 * @deprecated 此类不再使用。其功能已被 {@code java.security.KeyStore}、
 * {@code java.security.cert} 包和
 * {@code java.security.Principal} 所取代。
 */
@Deprecated
public abstract
class IdentityScope extends Identity {

    private static final long serialVersionUID = -2337346281189773310L;

    /* 系统的范围 */
    private static IdentityScope scope;

    // 初始化系统范围
    private static void initializeSystemScope() {

        String classname = AccessController.doPrivileged(
                                new PrivilegedAction<String>() {
            public String run() {
                return Security.getProperty("system.scope");
            }
        });

        if (classname == null) {
            return;

        } else {

            try {
                Class.forName(classname);
            } catch (ClassNotFoundException e) {
                System.err.println("无法从 " +
                             classname + " 建立系统范围");
                e.printStackTrace();
            }
        }
    }

    /**
     * 仅用于序列化，子类不应使用此构造函数。
     */
    protected IdentityScope() {
        this("正在恢复...");
    }

    /**
     * 使用指定的名称构造新的身份范围。
     *
     * @param name 范围名称。
     */
    public IdentityScope(String name) {
        super(name);
    }

    /**
     * 使用指定的名称和范围构造新的身份范围。
     *
     * @param name 范围名称。
     * @param scope 新身份范围的范围。
     *
     * @exception KeyManagementException 如果范围中已经存在同名的身份。
     */
    public IdentityScope(String name, IdentityScope scope)
    throws KeyManagementException {
        super(name, scope);
    }

    /**
     * 返回系统的身份范围。
     *
     * @return 系统的身份范围，如果未设置则返回 {@code null}。
     *
     * @see #setSystemScope
     */
    public static IdentityScope getSystemScope() {
        if (scope == null) {
            initializeSystemScope();
        }
        return scope;
    }


    /**
     * 设置系统的身份范围。
     *
     * <p>首先，如果有安全经理，其
     * {@code checkSecurityAccess}
     * 方法将被调用，参数为 {@code "setSystemScope"}
     * 以检查是否允许设置身份范围。
     *
     * @param scope 要设置的范围。
     *
     * @exception  SecurityException  如果存在安全经理且其
     * {@code checkSecurityAccess} 方法不允许
     * 设置身份范围。
     *
     * @see #getSystemScope
     * @see SecurityManager#checkSecurityAccess
     */
    protected static void setSystemScope(IdentityScope scope) {
        check("setSystemScope");
        IdentityScope.scope = scope;
    }

    /**
     * 返回此身份范围内的身份数量。
     *
     * @return 此身份范围内的身份数量。
     */
    public abstract int size();

    /**
     * 返回此范围中具有指定名称的身份（如果有）。
     *
     * @param name 要检索的身份的名称。
     *
     * @return 名称为 {@code name} 的身份，如果此范围中没有名为 {@code name} 的身份，则返回 null。
     */
    public abstract Identity getIdentity(String name);

    /**
     * 检索名称与指定主体相同的身份。（注意：Identity 实现 Principal。）
     *
     * @param principal 对应于要检索的身份的主体。
     *
     * @return 名称与指定主体相同的身份，如果此范围中没有相同名称的身份，则返回 null。
     */
    public Identity getIdentity(Principal principal) {
        return getIdentity(principal.getName());
    }

    /**
     * 检索具有指定公钥的身份。
     *
     * @param key 要返回的身份的公钥。
     *
     * @return 具有给定密钥的身份，如果此范围中没有该密钥的身份，则返回 null。
     */
    public abstract Identity getIdentity(PublicKey key);

    /**
     * 向此身份范围添加身份。
     *
     * @param identity 要添加的身份。
     *
     * @exception KeyManagementException 如果身份无效、名称冲突、另一个身份具有与要添加的身份相同的
     * 公钥，或发生其他异常。
     */
    public abstract void addIdentity(Identity identity)
    throws KeyManagementException;

    /**
     * 从此身份范围中移除身份。
     *
     * @param identity 要移除的身份。
     *
     * @exception KeyManagementException 如果身份不存在，或发生其他异常。
     */
    public abstract void removeIdentity(Identity identity)
    throws KeyManagementException;

    /**
     * 返回此身份范围中所有身份的枚举。
     *
     * @return 此身份范围中所有身份的枚举。
     */
    public abstract Enumeration<Identity> identities();

    /**
     * 返回此身份范围的字符串表示形式，包括其名称、范围名称和此
     * 身份范围中的身份数量。
     *
     * @return 此身份范围的字符串表示形式。
     */
    public String toString() {
        return super.toString() + "[" + size() + "]";
    }

    private static void check(String directive) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSecurityAccess(directive);
        }
    }

}
