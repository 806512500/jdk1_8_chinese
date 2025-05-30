/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.callback.CallbackHandler;

/**
 * 该类定义了提供者的登录和注销方法。
 *
 * <p> 调用者可以直接调用 {@code login} 方法，
 * 但提供者也可以在确定某些操作之前必须执行登录时，
 * 代表调用者调用 {@code login} 方法。
 *
 * @since 1.5
 */
public abstract class AuthProvider extends Provider {

    private static final long serialVersionUID = 4197859053084546461L;

    /**
     * 使用指定的名称、版本号和信息构造提供者。
     *
     * @param name 提供者名称。
     * @param version 提供者版本号。
     * @param info 提供者及其服务的描述。
     */
    protected AuthProvider(String name, double version, String info) {
        super(name, version, info);
    }

    /**
     * 登录到此提供者。
     *
     * <p> 提供者依赖于 {@code CallbackHandler}
     * 从调用者处获取身份验证信息（例如 PIN）。如果调用者将 {@code null}
     * 处理程序传递给此方法，提供者将使用在 {@code setCallbackHandler} 方法中设置的处理程序。
     * 如果该方法中未设置处理程序，提供者将查询
     * <i>auth.login.defaultCallbackHandler</i> 安全属性
     * 以获取默认处理程序实现的完全限定类名。
     * 如果未设置安全属性，
     * 则假定提供者有其他方法获取身份验证信息。
     *
     * @param subject 可能包含用于身份验证的主体/凭据，
     *          或在成功完成身份验证后可能填充额外的主体/凭据。
     *          此参数可以为 {@code null}。
     * @param handler 由
     *          本提供者用于从调用者处获取身份验证信息的 {@code CallbackHandler}，
     *          可以为 {@code null}
     *
     * @exception LoginException 如果登录操作失败
     * @exception SecurityException 如果调用者未通过
     *  {@code SecurityPermission("authProvider.name")} 的安全检查，
     *  其中 {@code name} 是此提供者的 {@code getName} 方法返回的值
     */
    public abstract void login(Subject subject, CallbackHandler handler)
        throws LoginException;

    /**
     * 从该提供者注销。
     *
     * @exception LoginException 如果注销操作失败
     * @exception SecurityException 如果调用者未通过
     *  {@code SecurityPermission("authProvider.name")} 的安全检查，
     *  其中 {@code name} 是此提供者的 {@code getName} 方法返回的值
     */
    public abstract void logout() throws LoginException;

    /**
     * 设置一个 {@code CallbackHandler}。
     *
     * <p> 如果未通过 {@code login} 方法传递处理程序，提供者将使用此处理程序。
     * 如果提供者代表调用者调用 {@code login}，也会使用此处理程序。
     * 在这两种情况下，如果未通过此方法设置处理程序，
     * 提供者将查询
     * <i>auth.login.defaultCallbackHandler</i> 安全属性
     * 以获取默认处理程序实现的完全限定类名。
     * 如果未设置安全属性，
     * 则假定提供者有其他方法获取身份验证信息。
     *
     * @param handler 用于获取
     *          身份验证信息的 {@code CallbackHandler}，可以为 {@code null}
     *
     * @exception SecurityException 如果调用者未通过
     *  {@code SecurityPermission("authProvider.name")} 的安全检查，
     *  其中 {@code name} 是此提供者的 {@code getName} 方法返回的值
     */
    public abstract void setCallbackHandler(CallbackHandler handler);
}
