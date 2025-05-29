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

package java.lang.reflect;

/**
 * 反射操作的权限类。
 * <P>
 * 下表提供了该权限允许的操作的简要描述，以及授予代码该权限的风险。
 *
 * <table border=1 cellpadding=5 summary="表格显示权限目标名称、权限允许的操作以及相关的风险">
 * <tr>
 * <th>权限目标名称</th>
 * <th>权限允许的操作</th>
 * <th>允许此权限的风险</th>
 * </tr>
 *
 * <tr>
 *   <td>suppressAccessChecks</td>
 *   <td>能够抑制类中字段和方法的标准 Java 语言访问检查；不仅允许访问公共成员，还允许访问默认（包）访问、受保护的和私有成员。</td>
 *   <td>这很危险，因为通常不可用的信息（可能是机密的）和方法将对恶意代码开放。</td>
 * </tr>
 * <tr>
 *   <td>newProxyInPackage.{包名}</td>
 *   <td>能够在指定包中创建代理实例，该代理类实现的非公共接口。</td>
 *   <td>这使代码能够访问通常无法访问的包中的类，并且动态代理类位于系统保护域中。恶意代码可能使用这些类来帮助其尝试破坏系统中的安全性。</td>
 * </tr>
 *
 * </table>
 *
 * @see java.security.Permission
 * @see java.security.BasicPermission
 * @see AccessibleObject
 * @see Field#get
 * @see Field#set
 * @see Method#invoke
 * @see Constructor#newInstance
 * @see Proxy#newProxyInstance
 *
 * @since 1.2
 */
public final
class ReflectPermission extends java.security.BasicPermission {

    private static final long serialVersionUID = 7412737110241507485L;

    /**
     * 使用指定的名称构造一个 ReflectPermission。
     *
     * @param name ReflectPermission 的名称
     *
     * @throws NullPointerException 如果 {@code name} 为 {@code null}。
     * @throws IllegalArgumentException 如果 {@code name} 为空。
     */
    public ReflectPermission(String name) {
        super(name);
    }

    /**
     * 使用指定的名称和操作构造一个 ReflectPermission。
     * 操作应为 null；它们将被忽略。
     *
     * @param name ReflectPermission 的名称
     *
     * @param actions 应为 null
     *
     * @throws NullPointerException 如果 {@code name} 为 {@code null}。
     * @throws IllegalArgumentException 如果 {@code name} 为空。
     */
    public ReflectPermission(String name, String actions) {
        super(name, actions);
    }

}
