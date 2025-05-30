/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 用于标识通用 SQL 类型（称为 JDBC 类型或供应商特定的数据类型）的对象。
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
     * 返回支持此数据类型的供应商的名称。返回的值通常是此供应商的包名。
     *
     * @return 此数据类型的供应商名称
     */
    String getVendor();

    /**
     * 返回数据类型的供应商特定类型编号。
     *
     * @return 代表供应商特定数据类型的整数
     */
    Integer getVendorTypeNumber();
}
