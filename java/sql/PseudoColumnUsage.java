/*
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
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
 * 枚举用于伪/隐藏列的使用。
 *
 * @since 1.7
 * @see DatabaseMetaData#getPseudoColumns
 */
public enum PseudoColumnUsage {

    /**
     * 伪/隐藏列只能在 SELECT 列表中使用。
     */
    SELECT_LIST_ONLY,

    /**
     * 伪/隐藏列只能在 WHERE 子句中使用。
     */
    WHERE_CLAUSE_ONLY,

    /**
     * 伪/隐藏列的使用没有限制。
     */
    NO_USAGE_RESTRICTIONS,

    /**
     * 无法确定伪/隐藏列的使用。
     */
    USAGE_UNKNOWN

}
