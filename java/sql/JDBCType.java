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
 * <P>定义用于标识通用 SQL 类型（称为 JDBC 类型）的常量。
 * <p>
 * @see SQLType
 * @since 1.8
 */
public enum JDBCType implements SQLType {

    /**
     * 标识通用 SQL 类型 {@code BIT}。
     */
    BIT(Types.BIT),
    /**
     * 标识通用 SQL 类型 {@code TINYINT}。
     */
    TINYINT(Types.TINYINT),
    /**
     * 标识通用 SQL 类型 {@code SMALLINT}。
     */
    SMALLINT(Types.SMALLINT),
    /**
     * 标识通用 SQL 类型 {@code INTEGER}。
     */
    INTEGER(Types.INTEGER),
    /**
     * 标识通用 SQL 类型 {@code BIGINT}。
     */
    BIGINT(Types.BIGINT),
    /**
     * 标识通用 SQL 类型 {@code FLOAT}。
     */
    FLOAT(Types.FLOAT),
    /**
     * 标识通用 SQL 类型 {@code REAL}。
     */
    REAL(Types.REAL),
    /**
     * 标识通用 SQL 类型 {@code DOUBLE}。
     */
    DOUBLE(Types.DOUBLE),
    /**
     * 标识通用 SQL 类型 {@code NUMERIC}。
     */
    NUMERIC(Types.NUMERIC),
    /**
     * 标识通用 SQL 类型 {@code DECIMAL}。
     */
    DECIMAL(Types.DECIMAL),
    /**
     * 标识通用 SQL 类型 {@code CHAR}。
     */
    CHAR(Types.CHAR),
    /**
     * 标识通用 SQL 类型 {@code VARCHAR}。
     */
    VARCHAR(Types.VARCHAR),
    /**
     * 标识通用 SQL 类型 {@code LONGVARCHAR}。
     */
    LONGVARCHAR(Types.LONGVARCHAR),
    /**
     * 标识通用 SQL 类型 {@code DATE}。
     */
    DATE(Types.DATE),
    /**
     * 标识通用 SQL 类型 {@code TIME}。
     */
    TIME(Types.TIME),
    /**
     * 标识通用 SQL 类型 {@code TIMESTAMP}。
     */
    TIMESTAMP(Types.TIMESTAMP),
    /**
     * 标识通用 SQL 类型 {@code BINARY}。
     */
    BINARY(Types.BINARY),
    /**
     * 标识通用 SQL 类型 {@code VARBINARY}。
     */
    VARBINARY(Types.VARBINARY),
    /**
     * 标识通用 SQL 类型 {@code LONGVARBINARY}。
     */
    LONGVARBINARY(Types.LONGVARBINARY),
    /**
     * 标识通用 SQL 值 {@code NULL}。
     */
    NULL(Types.NULL),
    /**
     * 表示 SQL 类型是数据库特定的，并映射到可以通过方法 getObject 和 setObject 访问的 Java 对象。
     */
    OTHER(Types.OTHER),
    /**
     * 表示 SQL 类型是数据库特定的，并映射到可以通过方法 getObject 和 setObject 访问的 Java 对象。
     */
    JAVA_OBJECT(Types.JAVA_OBJECT),
    /**
     * 标识通用 SQL 类型 {@code DISTINCT}。
     */
    DISTINCT(Types.DISTINCT),
    /**
     * 标识通用 SQL 类型 {@code STRUCT}。
     */
    STRUCT(Types.STRUCT),
    /**
     * 标识通用 SQL 类型 {@code ARRAY}。
     */
    ARRAY(Types.ARRAY),
    /**
     * 标识通用 SQL 类型 {@code BLOB}。
     */
    BLOB(Types.BLOB),
    /**
     * 标识通用 SQL 类型 {@code CLOB}。
     */
    CLOB(Types.CLOB),
    /**
     * 标识通用 SQL 类型 {@code REF}。
     */
    REF(Types.REF),
    /**
     * 标识通用 SQL 类型 {@code DATALINK}。
     */
    DATALINK(Types.DATALINK),
    /**
     * 标识通用 SQL 类型 {@code BOOLEAN}。
     */
    BOOLEAN(Types.BOOLEAN),

    /* JDBC 4.0 类型 */

    /**
     * 标识 SQL 类型 {@code ROWID}。
     */
    ROWID(Types.ROWID),
    /**
     * 标识通用 SQL 类型 {@code NCHAR}。
     */
    NCHAR(Types.NCHAR),
    /**
     * 标识通用 SQL 类型 {@code NVARCHAR}。
     */
    NVARCHAR(Types.NVARCHAR),
    /**
     * 标识通用 SQL 类型 {@code LONGNVARCHAR}。
     */
    LONGNVARCHAR(Types.LONGNVARCHAR),
    /**
     * 标识通用 SQL 类型 {@code NCLOB}。
     */
    NCLOB(Types.NCLOB),
    /**
     * 标识通用 SQL 类型 {@code SQLXML}。
     */
    SQLXML(Types.SQLXML),

    /* JDBC 4.2 类型 */

    /**
     * 标识通用 SQL 类型 {@code REF_CURSOR}。
     */
    REF_CURSOR(Types.REF_CURSOR),

    /**
     * 标识通用 SQL 类型 {@code TIME_WITH_TIMEZONE}。
     */
    TIME_WITH_TIMEZONE(Types.TIME_WITH_TIMEZONE),

    /**
     * 标识通用 SQL 类型 {@code TIMESTAMP_WITH_TIMEZONE}。
     */
    TIMESTAMP_WITH_TIMEZONE(Types.TIMESTAMP_WITH_TIMEZONE);

    /**
     * JDBCType 的整数值。它映射到 {@code Types.java} 中的值。
     */
    private Integer type;

    /**
     * 构造函数，用于指定此数据类型的 {@code Types} 值。
     * @param type 此数据类型的 {@code Types} 值
     */
    JDBCType(final Integer type) {
        this.type = type;
    }

    /**
     *{@inheritDoc }
     * @return 此 {@code SQLType} 的名称。
     */
    public String getName() {
        return name();
    }
    /**
     * 返回支持此数据类型的供应商名称。
     * @return 此数据类型的供应商名称，对于 {@code JDBCType}，该值为 {@literal java.sql}。
     */
    public String getVendor() {
        return "java.sql";
    }

    /**
     * 返回数据类型的供应商特定类型编号。
     * @return 一个表示数据类型的整数。对于 {@code JDBCType}，该值将与 {@code Types} 中的数据类型值相同。
     */
    public Integer getVendorTypeNumber() {
        return type;
    }
    /**
     * 返回与指定的 {@code Types} 值对应的 {@code JDBCType}。
     * @param type {@code Types} 值
     * @return {@code JDBCType} 常量
     * @throws IllegalArgumentException 如果此枚举类型没有具有指定的 {@code Types} 值的常量
     * @see Types
     */
    public static JDBCType valueOf(int type) {
        for( JDBCType sqlType : JDBCType.class.getEnumConstants()) {
            if(type == sqlType.type)
                return sqlType;
        }
        throw new IllegalArgumentException("Type:" + type + " is not a valid "
                + "Types.java value.");
    }
}
