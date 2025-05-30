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
 * 当 {@linkplain Driver} 希望被 {@code DriverManager} 通知时必须实现的接口。
 *<P>
 * 一个 {@code DriverAction} 实现不打算直接由应用程序使用。JDBC 驱动程序可以选择
 * 在一个私有类中创建其 {@code DriverAction} 实现，以避免它被直接调用。
 * <p>
 * JDBC 驱动程序的静态初始化块必须调用
 * {@linkplain DriverManager#registerDriver(java.sql.Driver, java.sql.DriverAction) } 以通知
 * {@code DriverManager} 在 JDBC 驱动程序被注销时应调用哪个 {@code DriverAction} 实现。
 * @since 1.8
 */
public interface DriverAction {
    /**
     * 由
     * {@linkplain DriverManager#deregisterDriver(Driver) }
     * 调用的方法，以通知 JDBC 驱动程序它已被注销。
     * <p>
     * {@code deregister} 方法仅打算由 JDBC 驱动程序使用，而不由应用程序使用。建议 JDBC 驱动程序不要在公共类中实现
     * {@code DriverAction}。如果在调用 {@code deregister} 方法时有活动的数据库连接，是否关闭这些连接或允许它们继续运行是实现特定的。一旦调用此方法，驱动程序是否限制创建新的数据库连接、调用其他
     * {@code Driver} 方法或抛出 {@code SQLException} 也是实现特定的。请参阅您的 JDBC 驱动程序的文档以获取其行为的更多信息。
     * @see DriverManager#registerDriver(java.sql.Driver, java.sql.DriverAction)
     * @see DriverManager#deregisterDriver(Driver)
     * @since 1.8
     */
    void deregister();

}
