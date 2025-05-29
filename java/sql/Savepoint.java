/*
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
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

package java.sql;

/**
 * 保存点的表示，这是当前事务中的一个点，可以从
 * <code>Connection.rollback</code> 方法中引用。当事务
 * 回滚到保存点时，该保存点之后的所有更改都将被撤销。
 * <p>
 * 保存点可以是有名的或无名的。无名保存点
 * 通过底层数据源生成的ID来识别。
 *
 * @since 1.4
 */

public interface Savepoint {

    /**
     * 获取此 <code>Savepoint</code> 对象表示的保存点的生成ID。
     * @return 此保存点的数字ID
     * @exception SQLException 如果这是一个有名保存点
     * @since 1.4
     */
    int getSavepointId() throws SQLException;

    /**
     * 获取此 <code>Savepoint</code> 对象表示的保存点的名称。
     * @return 此保存点的名称
     * @exception SQLException 如果这是一个无名保存点
     * @since 1.4
     */
    String getSavepointName() throws SQLException;
}
