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

/**
 * <p> 当 AccessController 检测到请求访问（例如文件系统或网络等关键系统资源）被拒绝时，抛出此异常。
 *
 * <p> 拒绝访问的原因可能各不相同。例如，请求的权限可能类型不正确，包含无效值，或者根据安全策略不允许访问。在抛出异常时，应尽可能提供此类信息。
 *
 * @author Li Gong
 * @author Roland Schemers
 */

public class AccessControlException extends SecurityException {

    private static final long serialVersionUID = 5138225684096988535L;

    // 导致异常被抛出的权限。
    private Permission perm;

    /**
     * 使用指定的详细消息构造一个 {@code AccessControlException}。
     *
     * @param   s   详细消息。
     */
    public AccessControlException(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和导致异常的请求权限构造一个 {@code AccessControlException}。
     *
     * @param   s   详细消息。
     * @param   p   导致异常的权限。
     */
    public AccessControlException(String s, Permission p) {
        super(s);
        perm = p;
    }

    /**
     * 获取与此异常关联的 Permission 对象，如果没有对应的 Permission 对象，则返回 null。
     *
     * @return Permission 对象。
     */
    public Permission getPermission() {
        return perm;
    }
}
