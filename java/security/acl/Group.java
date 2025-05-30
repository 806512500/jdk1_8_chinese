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

import java.util.Enumeration;
import java.security.Principal;

/**
 * 此接口用于表示一组主体。 (主体表示一个实体，如个人用户或公司)。 <p>
 *
 * 注意，Group 扩展了 Principal。因此，可以将 Principal 或 Group 作为参数传递给包含 Principal 参数的方法。例如，可以通过调用对象的 {@code addMember} 方法，将 Principal 或 Group 添加到 Group 对象中。
 *
 * @author      Satish Dharmaraj
 */
public interface Group extends Principal {

    /**
     * 将指定的成员添加到组中。
     *
     * @param user 要添加到此组的主体。
     *
     * @return 如果成员成功添加，则返回 true；如果主体已经是成员，则返回 false。
     */
    public boolean addMember(Principal user);

    /**
     * 从组中移除指定的成员。
     *
     * @param user 要从组中移除的主体。
     *
     * @return 如果移除了主体，则返回 true；如果主体不是成员，则返回 false。
     */
    public boolean removeMember(Principal user);

    /**
     * 如果传递的主体是组的成员，则返回 true。此方法执行递归搜索，因此如果主体属于一个组，而该组是此组的成员，则返回 true。
     *
     * @param member 要检查其成员资格的主体。
     *
     * @return 如果主体是此组的成员，则返回 true；否则返回 false。
     */
    public boolean isMember(Principal member);


    /**
     * 返回组中的成员枚举。返回的对象可以是 Principal 或 Group (Group 是 Principal 的子类) 的实例。
     *
     * @return 组成员的枚举。
     */
    public Enumeration<? extends Principal> members();

}
