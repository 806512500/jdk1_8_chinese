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
 * 当 {@linkplain Driver} 希望被 {@code DriverManager} 通知时，必须实现的接口。
 *<P>
 * 一个 {@code DriverAction} 实现不打算直接由应用程序使用。JDBC 驱动程序可以选择
 * 在私有类中创建其 {@code DriverAction} 实现，以避免其被直接调用。
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
     * 调用的方法，通知 JDBC 驱动程序已被注销。
     * <p>
     * {@code deregister} 方法仅打算由 JDBC 驱动程序使用，而不由应用程序使用。建议 JDBC 驱动程序不要在公共类中实现
     * {@code DriverAction}。如果在调用 {@code deregister} 方法时存在与数据库的活动连接，则具体实现可能决定
     * 是否关闭这些连接或允许其继续。一旦调用此方法，具体实现可能决定驱动程序是否限制创建新的数据库连接、调用其他
     * {@code Driver} 方法或抛出 {@code SQLException}。有关其行为的更多信息，请参阅您的 JDBC 驱动程序的文档。
     * @see DriverManager#registerDriver(java.sql.Driver, java.sql.DriverAction)
     * @see DriverManager#deregisterDriver(Driver)
     * @since 1.8
     */
    void deregister();

}
