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

import java.util.logging.Logger;

/**
 * 每个驱动程序类必须实现的接口。
 * <P>Java SQL框架允许多个数据库驱动程序。
 *
 * <P>每个驱动程序应提供一个实现
 * Driver接口的类。
 *
 * <P>驱动程序管理器将尝试加载尽可能多的驱动程序
 * 并且对于任何给定的连接请求，它将依次询问每个
 * 驱动程序尝试连接到目标URL。
 *
 * <P>强烈建议每个Driver类都应小巧且独立，以便在加载和查询驱动程序时
 * 不会引入大量支持代码。
 *
 * <P>当加载Driver类时，它应创建一个自身的实例
 * 并将其注册到DriverManager。这意味着用户可以通过调用：
 * <p>
 * {@code Class.forName("foo.bah.Driver")}
 * <p>
 * 来加载和注册驱动程序。
 * JDBC驱动程序可以创建一个 {@linkplain DriverAction} 实现，以便在调用
 * {@linkplain DriverManager#deregisterDriver} 时接收通知。
 * @see DriverManager
 * @see Connection
 * @see DriverAction
 */
public interface Driver {

    /**
     * 尝试连接到给定URL的数据库。
     * 如果驱动程序意识到它不是连接到给定URL的正确驱动程序，则应返回"null"。这将很常见，因为
     * 当JDBC驱动程序管理器被要求连接到给定URL时，它会依次将URL传递给每个已加载的驱动程序。
     *
     * <P>如果驱动程序是连接到给定URL的正确驱动程序但在连接到数据库时遇到问题，
     * 驱动程序应抛出一个<code>SQLException</code>。
     *
     * <P>{@code Properties}参数可用于传递
     * 任意字符串标签/值对作为连接参数。
     * 通常至少应包含"用户"和"密码"属性。
     * <p>
     * <B>注意：</B>如果属性在{@code url}中指定并且也在{@code Properties}对象中指定，则
     * 实现定义哪个值将优先。为了最大可移植性，应用程序应仅指定一次属性。
     *
     * @param url 要连接的数据库的URL
     * @param info 任意字符串标签/值对列表，通常至少应包含"用户"和
     * "密码"属性。
     * @return 一个表示连接到URL的<code>Connection</code>对象
     * @exception SQLException 如果发生数据库访问错误或url为
     * {@code null}
     */
    Connection connect(String url, java.util.Properties info)
        throws SQLException;

    /**
     * 检查驱动程序是否认为它可以打开到给定URL的连接。通常驱动程序会在理解URL中指定的子协议时返回<code>true</code>，否则返回<code>false</code>。
     *
     * @param url 数据库的URL
     * @return 如果此驱动程序理解给定URL，则返回<code>true</code>；
     * 否则返回<code>false</code>
     * @exception SQLException 如果发生数据库访问错误或url为
     * {@code null}
     */
    boolean acceptsURL(String url) throws SQLException;


    /**
     * 获取此驱动程序的可能属性信息。
     * <P>
     * <code>getPropertyInfo</code>方法旨在允许通用的GUI工具发现应提示
     * 人类输入哪些属性以获取足够的信息来连接到数据库。请注意，根据
     * 人类已经提供的值，可能需要额外的值，因此可能需要多次调用
     * <code>getPropertyInfo</code>方法。
     *
     * @param url 要连接的数据库的URL
     * @param info 一个提议的标签/值对列表，将在连接打开时发送
     * @return 一个描述可能属性的<code>DriverPropertyInfo</code>对象数组。如果
     * 不需要属性，此数组可以为空数组。
     * @exception SQLException 如果发生数据库访问错误
     */
    DriverPropertyInfo[] getPropertyInfo(String url, java.util.Properties info)
                         throws SQLException;


    /**
     * 获取驱动程序的主要版本号。最初应为1。
     *
     * @return 此驱动程序的主要版本号
     */
    int getMajorVersion();

    /**
     * 获取驱动程序的次要版本号。最初应为0。
     * @return 此驱动程序的次要版本号
     */
    int getMinorVersion();


    /**
     * 报告此驱动程序是否是真正的JDBC
     * Compliant&trade;驱动程序。
     * 驱动程序只有通过JDBC
     * 合规测试才能在此处报告<code>true</code>；否则必须返回<code>false</code>。
     * <P>
     * JDBC合规要求完全支持JDBC API和SQL 92 Entry Level。预计JDBC合规驱动程序将
     * 适用于所有主要的商业数据库。
     * <P>
     * 此方法不旨在鼓励开发非JDBC
     * 合规驱动程序，而是认识到一些供应商
     * 对于轻量级数据库或不支持完整数据库功能的特殊数据库（如文档信息检索）有兴趣使用JDBC API和框架，其中SQL实现可能不可行。
     * @return 如果此驱动程序是JDBC合规的，则返回<code>true</code>；否则返回<code>false</code>
     */
    boolean jdbcCompliant();

    //------------------------- JDBC 4.1 -----------------------------------

    /**
     * 返回此驱动程序使用的所有Logger的父Logger。这应该是离根Logger最远的
     * 但仍作为此驱动程序使用的所有Logger的祖先的Logger。配置
     * 此Logger将影响驱动程序生成的所有日志消息。
     * 在最坏的情况下，这可能是根Logger。
     *
     * @return 此驱动程序的父Logger
     * @throws SQLFeatureNotSupportedException 如果驱动程序不使用
     * {@code java.util.logging}。
     * @since 1.7
     */
    public Logger getParentLogger() throws SQLFeatureNotSupportedException;
}
