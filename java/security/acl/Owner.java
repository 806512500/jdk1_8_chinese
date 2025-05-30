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

package java.security.acl;

import java.security.Principal;

/**
 * 用于管理访问控制列表（ACL）或ACL配置的所有者的接口。（请注意，位于
 * {@code  java.security.acl} 包中的 Acl 接口扩展了此 Owner
 * 接口。）初始所有者 Principal 应作为实现此接口的类的构造函数的参数指定。
 *
 * @see java.security.acl.Acl
 *
 */
public interface Owner {

    /**
     * 添加一个所有者。只有所有者可以修改ACL内容。调用此方法的
     * 主体必须是ACL的所有者。也就是说，只有所有者可以添加另一个所有者。初始所有者
     * 在ACL构造时配置。
     *
     * @param caller 调用此方法的主体。它必须是ACL的所有者。
     *
     * @param owner 应添加到所有者列表中的所有者。
     *
     * @return 如果成功则返回 true，如果所有者已经是所有者则返回 false。
     * @exception NotOwnerException 如果调用主体不是ACL的所有者。
     */
    public boolean addOwner(Principal caller, Principal owner)
      throws NotOwnerException;

    /**
     * 删除一个所有者。如果这是ACL中的最后一个所有者，则会引发异常。<p>
     *
     * 调用此方法的主体必须是ACL的所有者。
     *
     * @param caller 调用此方法的主体。它必须是ACL的所有者。
     *
     * @param owner 要从所有者列表中移除的所有者。
     *
     * @return 如果所有者被移除则返回 true，如果所有者不在所有者列表中则返回 false。
     *
     * @exception NotOwnerException 如果调用主体不是ACL的所有者。
     *
     * @exception LastOwnerException 如果只剩下最后一个所有者，那么删除所有者将使ACL没有所有者。
     */
    public boolean deleteOwner(Principal caller, Principal owner)
      throws NotOwnerException, LastOwnerException;

    /**
     * 如果给定的主体是ACL的所有者，则返回 true。
     *
     * @param owner 要检查的主体，以确定其是否为所有者。
     *
     * @return 如果传递的主体在所有者列表中则返回 true，否则返回 false。
     */
    public boolean isOwner(Principal owner);

}
