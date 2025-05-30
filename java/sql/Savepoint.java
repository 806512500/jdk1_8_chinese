/*
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
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

package java.sql;

/**
 * 保存点的表示，这是当前事务中的一个点，可以从
 * <code>Connection.rollback</code> 方法引用。当事务
 * 回滚到保存点时，该保存点之后的所有更改都将被撤销。
 * <p>
 * 保存点可以是命名的或未命名的。未命名的保存点
 * 由底层数据源生成的ID标识。
 *
 * @since 1.4
 */

public interface Savepoint {

    /**
     * 检索此 <code>Savepoint</code> 对象表示的保存点的生成ID。
     * @return 此保存点的数字ID
     * @exception SQLException 如果这是命名保存点
     * @since 1.4
     */
    int getSavepointId() throws SQLException;

    /**
     * 检索此 <code>Savepoint</code> 对象表示的保存点的名称。
     * @return 此保存点的名称
     * @exception SQLException 如果这是未命名的保存点
     * @since 1.4
     */
    String getSavepointName() throws SQLException;
}
