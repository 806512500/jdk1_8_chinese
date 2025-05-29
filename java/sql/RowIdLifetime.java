/*
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
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

import java.util.*;

/**
 * 枚举类型，用于表示 RowId 的生命周期值。
 *
 * @since 1.6
 */

public enum RowIdLifetime {

    /**
     * 表示此数据源不支持 ROWID 类型。
     */
    ROWID_UNSUPPORTED,

    /**
     * 表示来自此数据源的 RowId 的生命周期是不确定的；
     * 但不是 ROWID_VALID_TRANSACTION, ROWID_VALID_SESSION, 或 ROWID_VALID_FOREVER 中的任何一个。
     */
    ROWID_VALID_OTHER,

    /**
     * 表示来自此数据源的 RowId 的生命周期至少是包含的会话。
     */
    ROWID_VALID_SESSION,

    /**
     * 表示来自此数据源的 RowId 的生命周期至少是包含的事务。
     */
    ROWID_VALID_TRANSACTION,

    /**
     * 表示来自此数据源的 RowId 的生命周期实际上是无限的。
     */
    ROWID_VALID_FOREVER
}
