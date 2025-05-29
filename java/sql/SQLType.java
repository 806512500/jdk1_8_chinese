/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 一个用于标识通用 SQL 类型的对象，称为 JDBC 类型或供应商特定的数据类型。
 *
 * @since 1.8
 */
public interface SQLType {

    /**
     * 返回表示 SQL 数据类型的 {@code SQLType} 名称。
     *
     * @return 此 {@code SQLType} 的名称。
     */
    String getName();

    /**
     * 返回支持此数据类型的供应商名称。返回的值通常是此供应商的包名。
     *
     * @return 此数据类型的供应商名称
     */
    String getVendor();

    /**
     * 返回数据类型的供应商特定类型编号。
     *
     * @return 一个表示供应商特定数据类型的整数
     */
    Integer getVendorTypeNumber();
}
