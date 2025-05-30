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

package java.sql;

/**
 * <P>定义用于标识通用 SQL 类型（称为 JDBC 类型）的常量的类。
 * <p>
 * 该类从未被实例化。
 */
public class Types {

/**
 * <P>在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
 * <code>BIT</code>。
 */
        public final static int BIT             =  -7;

/**
 * <P>在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
 * <code>TINYINT</code>。
 */
        public final static int TINYINT         =  -6;

/**
 * <P>在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
 * <code>SMALLINT</code>。
 */
        public final static int SMALLINT        =   5;

/**
 * <P>在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
 * <code>INTEGER</code>。
 */
        public final static int INTEGER         =   4;

/**
 * <P>在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
 * <code>BIGINT</code>。
 */
        public final static int BIGINT          =  -5;

/**
 * <P>在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
 * <code>FLOAT</code>。
 */
        public final static int FLOAT           =   6;

/**
 * <P>在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
 * <code>REAL</code>。
 */
        public final static int REAL            =   7;


/**
 * <P>在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
 * <code>DOUBLE</code>。
 */
        public final static int DOUBLE          =   8;

/**
 * <P>在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
 * <code>NUMERIC</code>。
 */
        public final static int NUMERIC         =   2;

/**
 * <P>在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
 * <code>DECIMAL</code>。
 */
        public final static int DECIMAL         =   3;

/**
 * <P>在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
 * <code>CHAR</code>。
 */
        public final static int CHAR            =   1;

/**
 * <P>在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
 * <code>VARCHAR</code>。
 */
        public final static int VARCHAR         =  12;

/**
 * <P>在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
 * <code>LONGVARCHAR</code>。
 */
        public final static int LONGVARCHAR     =  -1;


/**
 * <P>在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
 * <code>DATE</code>。
 */
        public final static int DATE            =  91;

/**
 * <P>在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
 * <code>TIME</code>。
 */
        public final static int TIME            =  92;

/**
 * <P>在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
 * <code>TIMESTAMP</code>。
 */
        public final static int TIMESTAMP       =  93;


/**
 * <P>在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
 * <code>BINARY</code>。
 */
        public final static int BINARY          =  -2;

/**
 * <P>在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
 * <code>VARBINARY</code>。
 */
        public final static int VARBINARY       =  -3;

/**
 * <P>在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
 * <code>LONGVARBINARY</code>。
 */
        public final static int LONGVARBINARY   =  -4;

/**
 * <P>在 Java 编程语言中的常量，用于标识通用 SQL 值
 * <code>NULL</code>。
 */
        public final static int NULL            =   0;

    /**
     * 在 Java 编程语言中的常量，表示 SQL 类型是数据库特定的，并且
     * 映射到可以通过 <code>getObject</code> 和 <code>setObject</code> 方法访问的 Java 对象。
     */
        public final static int OTHER           = 1111;



    /**
     * 在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
     * <code>JAVA_OBJECT</code>。
     * @since 1.2
     */
        public final static int JAVA_OBJECT         = 2000;

    /**
     * 在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
     * <code>DISTINCT</code>。
     * @since 1.2
     */
        public final static int DISTINCT            = 2001;

    /**
     * 在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
     * <code>STRUCT</code>。
     * @since 1.2
     */
        public final static int STRUCT              = 2002;

    /**
     * 在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
     * <code>ARRAY</code>。
     * @since 1.2
     */
        public final static int ARRAY               = 2003;

    /**
     * 在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
     * <code>BLOB</code>。
     * @since 1.2
     */
        public final static int BLOB                = 2004;

    /**
     * 在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
     * <code>CLOB</code>。
     * @since 1.2
     */
        public final static int CLOB                = 2005;

    /**
     * 在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
     * <code>REF</code>。
     * @since 1.2
     */
        public final static int REF                 = 2006;

    /**
     * 在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型 <code>DATALINK</code>。
     *
     * @since 1.4
     */
    public final static int DATALINK = 70;

    /**
     * 在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型 <code>BOOLEAN</code>。
     *
     * @since 1.4
     */
    public final static int BOOLEAN = 16;

    //------------------------- JDBC 4.0 -----------------------------------

    /**
     * 在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型 <code>ROWID</code>
     *
     * @since 1.6
     *
     */
    public final static int ROWID = -8;

    /**
     * 在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型 <code>NCHAR</code>
     *
     * @since 1.6
     */
    public static final int NCHAR = -15;

    /**
     * 在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型 <code>NVARCHAR</code>。
     *
     * @since 1.6
     */
    public static final int NVARCHAR = -9;

    /**
     * 在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型 <code>LONGNVARCHAR</code>。
     *
     * @since 1.6
     */
    public static final int LONGNVARCHAR = -16;

    /**
     * 在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型 <code>NCLOB</code>。
     *
     * @since 1.6
     */
    public static final int NCLOB = 2011;

    /**
     * 在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型 <code>XML</code>。
     *
     * @since 1.6
     */
    public static final int SQLXML = 2009;

    //--------------------------JDBC 4.2 -----------------------------

    /**
     * 在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型 {@code REF CURSOR}。
     *
     * @since 1.8
     */
    public static final int REF_CURSOR = 2012;

    /**
     * 在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
     * {@code TIME WITH TIMEZONE}。
     *
     * @since 1.8
     */
    public static final int TIME_WITH_TIMEZONE = 2013;

    /**
     * 在 Java 编程语言中的常量，有时称为类型代码，用于标识通用 SQL 类型
     * {@code TIMESTAMP WITH TIMEZONE}。
     *
     * @since 1.8
     */
    public static final int TIMESTAMP_WITH_TIMEZONE = 2014;

    // 防止实例化
    private Types() {}
}
