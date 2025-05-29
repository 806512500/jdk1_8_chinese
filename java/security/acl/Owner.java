/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security.acl;

import java.security.Principal;

/**
 * 用于管理访问控制列表（ACL）或ACL配置的所有者的接口。（注意，java.security.acl包中的Acl接口扩展了此Owner接口。）
 * 初始所有者Principal应在实现此接口的类的构造函数中指定。
 *
 * @see java.security.acl.Acl
 *
 */
public interface Owner {

    /**
     * 添加一个所有者。只有所有者才能修改ACL内容。调用此方法的主体必须是ACL的所有者。
     * 也就是说，只有所有者才能添加另一个所有者。初始所有者在ACL构建时配置。
     *
     * @param caller 调用此方法的主体。它必须是ACL的所有者。
     *
     * @param owner 应该添加到所有者列表中的所有者。
     *
     * @return 如果成功则返回true，如果所有者已经是所有者则返回false。
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
     * @return 如果所有者被移除则返回true，如果所有者不在所有者列表中则返回false。
     *
     * @exception NotOwnerException 如果调用主体不是ACL的所有者。
     *
     * @exception LastOwnerException 如果只剩下最后一个所有者，那么删除所有者将使ACL没有所有者。
     */
    public boolean deleteOwner(Principal caller, Principal owner)
      throws NotOwnerException, LastOwnerException;

    /**
     * 如果给定的主体是ACL的所有者，则返回true。
     *
     * @param owner 要检查的主体，以确定它是否是所有者。
     *
     * @return 如果传递的主体在所有者列表中则返回true，否则返回false。
     */
    public boolean isOwner(Principal owner);

}
