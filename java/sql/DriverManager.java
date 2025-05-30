
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

import java.util.Iterator;
import java.util.ServiceLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.CopyOnWriteArrayList;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;


/**
 * <P>管理一组 JDBC 驱动程序的基本服务。<br>
 * <B>注意：</B> {@link javax.sql.DataSource} 接口是 JDBC 2.0 API 中的新接口，提供了另一种连接到数据源的方式。
 * 使用 <code>DataSource</code> 对象是连接到数据源的首选方式。
 *
 * <P>作为其初始化的一部分，<code>DriverManager</code> 类将尝试加载在 "jdbc.drivers" 系统属性中引用的驱动程序类。
 * 这允许用户自定义其应用程序使用的 JDBC 驱动程序。例如，在您的
 * ~/.hotjava/properties 文件中，您可以指定：
 * <pre>
 * <CODE>jdbc.drivers=foo.bah.Driver:wombat.sql.Driver:bad.taste.ourDriver</CODE>
 * </pre>
 *<P> <code>DriverManager</code> 方法 <code>getConnection</code> 和 <code>getDrivers</code> 已经增强以支持 Java 标准版的
 * <a href="../../../technotes/guides/jar/jar.html#Service%20Provider">服务提供者</a> 机制。JDBC 4.0 驱动程序必须
 * 包含文件 <code>META-INF/services/java.sql.Driver</code>。此文件包含 <code>java.sql.Driver</code> 的实现类名。
 * 例如，要加载 <code>my.sql.Driver</code> 类，<code>META-INF/services/java.sql.Driver</code> 文件将包含以下条目：
 * <pre>
 * <code>my.sql.Driver</code>
 * </pre>
 *
 * <P>应用程序不再需要使用 <code>Class.forName()</code> 显式加载 JDBC 驱动程序。现有程序
 * 仍然可以继续使用 <code>Class.forName()</code> 加载 JDBC 驱动程序，而无需修改。
 *
 * <P>当调用 <code>getConnection</code> 方法时，
 * <code>DriverManager</code> 将尝试从初始化时加载的驱动程序和使用与当前小程序或应用程序相同的类加载器显式加载的驱动程序中
 * 选择一个合适的驱动程序。
 *
 * <P>
 * 从 Java 2 SDK，标准版，1.3 版开始，只有在授予适当权限的情况下才能设置日志流。
 * 通常这可以通过 PolicyTool 工具完成，该工具可以用于授予 <code>permission
 * java.sql.SQLPermission "setLog"</code>。
 * @see Driver
 * @see Connection
 */
public class DriverManager {


    // 注册的 JDBC 驱动程序列表
    private final static CopyOnWriteArrayList<DriverInfo> registeredDrivers = new CopyOnWriteArrayList<>();
    private static volatile int loginTimeout = 0;
    private static volatile java.io.PrintWriter logWriter = null;
    private static volatile java.io.PrintStream logStream = null;
    // 用于在 println() 中同步 logWriter
    private final static  Object logSync = new Object();

    /* 防止实例化 DriverManager 类。 */
    private DriverManager(){}


    /**
     * 通过检查系统属性 jdbc.properties 并使用 {@code ServiceLoader} 机制加载初始的 JDBC 驱动程序
     */
    static {
        loadInitialDrivers();
        println("JDBC DriverManager initialized");
    }

    /**
     * 允许设置日志流的 <code>SQLPermission</code> 常量。
     * @since 1.3
     */
    final static SQLPermission SET_LOG_PERMISSION =
        new SQLPermission("setLog");

    /**
     * 允许取消注册已注册的 JDBC 驱动程序的 {@code SQLPermission} 常量。
     * @since 1.8
     */
    final static SQLPermission DEREGISTER_DRIVER_PERMISSION =
        new SQLPermission("deregisterDriver");

    //--------------------------JDBC 2.0-----------------------------

    /**
     * 获取日志写入器。
     *
     * 应该使用 <code>getLogWriter</code> 和 <code>setLogWriter</code>
     * 方法而不是已弃用的 <code>get/setlogStream</code> 方法。
     * @return 一个 <code>java.io.PrintWriter</code> 对象
     * @see #setLogWriter
     * @since 1.2
     */
    public static java.io.PrintWriter getLogWriter() {
            return logWriter;
    }

    /**
     * 设置 <code>DriverManager</code> 和所有驱动程序使用的日志/跟踪 <code>PrintWriter</code> 对象。
     * <P>
     * 由于引入了 <code>setLogWriter</code> 方法，存在一个轻微的版本问题。<code>setLogWriter</code> 方法不能创建
     * 一个由 <code>getLogStream</code> 返回的 <code>PrintStream</code> 对象——Java 平台不提供向后转换。因此，一个新应用程序
     * 使用 <code>setLogWriter</code> 并且还使用一个使用 <code>getLogStream</code> 的 JDBC 1.0 驱动程序，很可能看不到该驱动程序写入的调试信息。
     *<P>
     * 从 Java 2 SDK，标准版，1.3 版开始，此方法检查是否存在 <code>SQLPermission</code> 对象，然后设置
     * 日志流。如果存在 <code>SecurityManager</code> 并且其
     * <code>checkPermission</code> 方法拒绝设置日志写入器，此方法将抛出 <code>java.lang.SecurityException</code>。
     *
     * @param out 新的日志/跟踪 <code>PrintStream</code> 对象；
     *      <code>null</code> 以禁用日志和跟踪
     * @throws SecurityException
     *    如果存在安全管理器并且其
     *    <code>checkPermission</code> 方法拒绝
     *    设置日志写入器
     *
     * @see SecurityManager#checkPermission
     * @see #getLogWriter
     * @since 1.2
     */
    public static void setLogWriter(java.io.PrintWriter out) {

        SecurityManager sec = System.getSecurityManager();
        if (sec != null) {
            sec.checkPermission(SET_LOG_PERMISSION);
        }
            logStream = null;
            logWriter = out;
    }


    //---------------------------------------------------------------

    /**
     * 尝试建立与给定数据库 URL 的连接。
     * <code>DriverManager</code> 试图从已注册的 JDBC 驱动程序集中选择一个合适的驱动程序。
     *<p>
     * <B>注意：</B> 如果属性作为 {@code url} 的一部分指定，并且也在 {@code Properties} 对象中指定，则
     * 实现定义哪个值将优先。为了最大限度的可移植性，应用程序应只指定一次属性。
     *
     * @param url 形如 <code> jdbc:<em>子协议</em>:<em>子名称</em></code> 的数据库 URL
     * @param info 作为连接参数的任意字符串标签/值对列表；通常至少应包括 "user" 和
     * "password" 属性
     * @return 到 URL 的连接
     * @exception SQLException 如果发生数据库访问错误或 URL 为
     * {@code null}
     * @throws SQLTimeoutException  当驱动程序确定由 {@code setLoginTimeout} 方法
     * 指定的超时值已超过，并且至少尝试取消当前的数据库连接尝试时
     */
    @CallerSensitive
    public static Connection getConnection(String url,
        java.util.Properties info) throws SQLException {

        return (getConnection(url, info, Reflection.getCallerClass()));
    }

    /**
     * 尝试建立与给定数据库 URL 的连接。
     * <code>DriverManager</code> 试图从已注册的 JDBC 驱动程序集中选择一个合适的驱动程序。
     *<p>
     * <B>注意：</B> 如果 {@code user} 或 {@code password} 属性也作为 {@code url} 的一部分指定，则
     * 实现定义哪个值将优先。为了最大限度的可移植性，应用程序应只指定一次属性。
     *
     * @param url 形如 <code>jdbc:<em>子协议</em>:<em>子名称</em></code> 的数据库 URL
     * @param user 代表其建立连接的数据库用户
     * @param password 用户的密码
     * @return 到 URL 的连接
     * @exception SQLException 如果发生数据库访问错误或 URL 为
     * {@code null}
     * @throws SQLTimeoutException  当驱动程序确定由 {@code setLoginTimeout} 方法
     * 指定的超时值已超过，并且至少尝试取消当前的数据库连接尝试时
     */
    @CallerSensitive
    public static Connection getConnection(String url,
        String user, String password) throws SQLException {
        java.util.Properties info = new java.util.Properties();

        if (user != null) {
            info.put("user", user);
        }
        if (password != null) {
            info.put("password", password);
        }

        return (getConnection(url, info, Reflection.getCallerClass()));
    }

    /**
     * 尝试建立与给定数据库 URL 的连接。
     * <code>DriverManager</code> 试图从已注册的 JDBC 驱动程序集中选择一个合适的驱动程序。
     *
     * @param url 形如 <code> jdbc:<em>子协议</em>:<em>子名称</em></code> 的数据库 URL
     * @return 到 URL 的连接
     * @exception SQLException 如果发生数据库访问错误或 URL 为
     * {@code null}
     * @throws SQLTimeoutException  当驱动程序确定由 {@code setLoginTimeout} 方法
     * 指定的超时值已超过，并且至少尝试取消当前的数据库连接尝试时
     */
    @CallerSensitive
    public static Connection getConnection(String url)
        throws SQLException {

        java.util.Properties info = new java.util.Properties();
        return (getConnection(url, info, Reflection.getCallerClass()));
    }

    /**
     * 尝试找到一个理解给定 URL 的驱动程序。
     * <code>DriverManager</code> 试图从已注册的 JDBC 驱动程序集中选择一个合适的驱动程序。
     *
     * @param url 形如 <code>jdbc:<em>子协议</em>:<em>子名称</em></code> 的数据库 URL
     * @return 一个表示可以连接到给定 URL 的驱动程序的 <code>Driver</code> 对象
     * @exception SQLException 如果发生数据库访问错误
     */
    @CallerSensitive
    public static Driver getDriver(String url)
        throws SQLException {

        println("DriverManager.getDriver(\"" + url + "\")");

        Class<?> callerClass = Reflection.getCallerClass();

        // 遍历已加载的注册驱动程序，尝试找到理解给定 URL 的驱动程序。
        for (DriverInfo aDriver : registeredDrivers) {
            // 如果调用者没有权限加载驱动程序，则跳过。
            if(isDriverAllowed(aDriver.driver, callerClass)) {
                try {
                    if(aDriver.driver.acceptsURL(url)) {
                        // 成功！
                        println("getDriver returning " + aDriver.driver.getClass().getName());
                    return (aDriver.driver);
                    }

                } catch(SQLException sqe) {
                    // 跳过并尝试下一个驱动程序。
                }
            } else {
                println("    skipping: " + aDriver.driver.getClass().getName());
            }

        }

        println("getDriver: no suitable driver");
        throw new SQLException("No suitable driver", "08001");
    }


    /**
     * 将给定的驱动程序注册到 {@code DriverManager}。
     * 新加载的驱动程序类应调用
     * {@code registerDriver} 方法以使自己
     * 为 {@code DriverManager} 所知。如果驱动程序当前已注册，则不采取任何行动。
     *
     * @param driver 要注册的新 JDBC 驱动程序
     * @exception SQLException 如果发生数据库访问错误
     * @exception NullPointerException 如果 {@code driver} 为 null
     */
    public static synchronized void registerDriver(java.sql.Driver driver)
        throws SQLException {

        registerDriver(driver, null);
    }

    /**
     * 将给定的驱动程序注册到 {@code DriverManager}。
     * 新加载的驱动程序类应调用
     * {@code registerDriver} 方法以使自己
     * 为 {@code DriverManager} 所知。如果驱动程序当前已注册，则不采取任何行动。
     *
     * @param driver 要注册的新 JDBC 驱动程序
     * @param da     当调用 {@code DriverManager#deregisterDriver} 时使用的
     *               {@code DriverAction} 实现
     * @exception SQLException 如果发生数据库访问错误
     * @exception NullPointerException 如果 {@code driver} 为 null
     * @since 1.8
     */
    public static synchronized void registerDriver(java.sql.Driver driver,
            DriverAction da)
        throws SQLException {

        /* 如果驱动程序尚未添加到列表中，则注册该驱动程序 */
        if(driver != null) {
            registeredDrivers.addIfAbsent(new DriverInfo(driver, da));
        } else {
            // 为了与原始的 DriverManager 兼容
            throw new NullPointerException();
        }

        println("registerDriver: " + driver);

    }

    /**
     * 从 {@code DriverManager} 的已注册驱动程序列表中移除指定的驱动程序。
     * <p>
     * 如果指定要移除的驱动程序为 {@code null}，则不采取任何行动。
     * <p>
     * 如果存在安全管理器并且其 {@code checkPermission} 拒绝
     * 权限，则将抛出 {@code SecurityException}。
     * <p>
     * 如果在已注册驱动程序列表中未找到指定的驱动程序，则不采取任何行动。如果找到驱动程序，将从已注册驱动程序列表中移除。
     * <p>
     * 如果在注册 JDBC 驱动程序时指定了 {@code DriverAction} 实例，则在从已注册驱动程序列表中移除驱动程序之前，将调用其 deregister 方法。
     *
     * @param driver 要移除的 JDBC 驱动程序
     * @exception SQLException 如果发生数据库访问错误
     * @throws SecurityException 如果存在安全管理器并且其
     * {@code checkPermission} 方法拒绝取消注册驱动程序的权限。
     *
     * @see SecurityManager#checkPermission
     */
    @CallerSensitive
    public static synchronized void deregisterDriver(Driver driver)
        throws SQLException {
        if (driver == null) {
            return;
        }


                    SecurityManager sec = System.getSecurityManager();
        if (sec != null) {
            sec.checkPermission(DEREGISTER_DRIVER_PERMISSION);
        }

        println("DriverManager.deregisterDriver: " + driver);

        DriverInfo aDriver = new DriverInfo(driver, null);
        if(registeredDrivers.contains(aDriver)) {
            if (isDriverAllowed(driver, Reflection.getCallerClass())) {
                DriverInfo di = registeredDrivers.get(registeredDrivers.indexOf(aDriver));
                 // 如果指定了 DriverAction，则调用它以通知驱动程序已被注销
                 if(di.action() != null) {
                     di.action().deregister();
                 }
                 registeredDrivers.remove(aDriver);
            } else {
                // 如果调用者没有加载驱动程序的权限，则抛出 SecurityException。
                throw new SecurityException();
            }
        } else {
            println("    无法找到要卸载的驱动程序");
        }
    }

    /**
     * 获取当前调用者有权访问的所有已加载 JDBC 驱动程序的 Enumeration。
     *
     * <P><B>注意：</B> 可以使用 <CODE>d.getClass().getName()</CODE> 查找驱动程序的类名。
     *
     * @return 由调用者的类加载器加载的 JDBC 驱动程序列表
     */
    @CallerSensitive
    public static java.util.Enumeration<Driver> getDrivers() {
        java.util.Vector<Driver> result = new java.util.Vector<>();

        Class<?> callerClass = Reflection.getCallerClass();

        // 遍历已加载的注册驱动程序。
        for(DriverInfo aDriver : registeredDrivers) {
            // 如果调用者没有加载驱动程序的权限，则跳过。
            if(isDriverAllowed(aDriver.driver, callerClass)) {
                result.addElement(aDriver.driver);
            } else {
                println("    跳过: " + aDriver.getClass().getName());
            }
        }
        return (result.elements());
    }


    /**
     * 设置驱动程序尝试连接到数据库时的等待时间（秒）。
     *
     * @param seconds 等待时间限制（秒）；零表示没有限制
     * @see #getLoginTimeout
     */
    public static void setLoginTimeout(int seconds) {
        loginTimeout = seconds;
    }

    /**
     * 获取驱动程序尝试登录到数据库时的最大等待时间（秒）。
     *
     * @return 驱动程序登录时间限制（秒）
     * @see #setLoginTimeout
     */
    public static int getLoginTimeout() {
        return (loginTimeout);
    }

    /**
     * 设置 <code>DriverManager</code> 和所有驱动程序使用的日志/跟踪 PrintStream。
     *<P>
     * 在 Java 2 SDK，Standard Edition，version 1.3 版本中，此方法检查是否有 <code>SQLPermission</code> 对象，然后设置日志流。如果存在 <code>SecurityManager</code> 并且其 <code>checkPermission</code> 方法拒绝设置日志写入器，此方法将抛出 <code>java.lang.SecurityException</code>。
     *
     * @param out 新的日志/跟踪 PrintStream；要禁用，设置为 <code>null</code>
     * @deprecated 使用 {@code setLogWriter}
     * @throws SecurityException 如果存在安全经理并且其 <code>checkPermission</code> 方法拒绝设置日志流
     *
     * @see SecurityManager#checkPermission
     * @see #getLogStream
     */
    @Deprecated
    public static void setLogStream(java.io.PrintStream out) {

        SecurityManager sec = System.getSecurityManager();
        if (sec != null) {
            sec.checkPermission(SET_LOG_PERMISSION);
        }

        logStream = out;
        if ( out != null )
            logWriter = new java.io.PrintWriter(out);
        else
            logWriter = null;
    }

    /**
     * 获取 <code>DriverManager</code> 和所有驱动程序使用的日志/跟踪 PrintStream。
     *
     * @return 日志/跟踪 PrintStream；如果禁用，则为 <code>null</code>
     * @deprecated 使用 {@code getLogWriter}
     * @see #setLogStream
     */
    @Deprecated
    public static java.io.PrintStream getLogStream() {
        return logStream;
    }

    /**
     * 将消息打印到当前的 JDBC 日志流。
     *
     * @param message 日志或跟踪消息
     */
    public static void println(String message) {
        synchronized (logSync) {
            if (logWriter != null) {
                logWriter.println(message);

                // 自动刷新从未启用，因此我们必须自己刷新
                logWriter.flush();
            }
        }
    }

    //------------------------------------------------------------------------

    // 指示如果调用 DriverManager 的代码创建的类对象是否可访问。
    private static boolean isDriverAllowed(Driver driver, Class<?> caller) {
        ClassLoader callerCL = caller != null ? caller.getClassLoader() : null;
        return isDriverAllowed(driver, callerCL);
    }

    private static boolean isDriverAllowed(Driver driver, ClassLoader classLoader) {
        boolean result = false;
        if(driver != null) {
            Class<?> aClass = null;
            try {
                aClass =  Class.forName(driver.getClass().getName(), true, classLoader);
            } catch (Exception ex) {
                result = false;
            }

             result = ( aClass == driver.getClass() ) ? true : false;
        }

        return result;
    }

    private static void loadInitialDrivers() {
        String drivers;
        try {
            drivers = AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty("jdbc.drivers");
                }
            });
        } catch (Exception ex) {
            drivers = null;
        }
        // 如果驱动程序被打包为服务提供者，则加载它。
        // 通过类加载器获取所有驱动程序，作为 java.sql.Driver 类的服务公开。
        // ServiceLoader.load() 替代了 sun.misc.Providers()

        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {

                ServiceLoader<Driver> loadedDrivers = ServiceLoader.load(Driver.class);
                Iterator<Driver> driversIterator = loadedDrivers.iterator();

                /* 加载这些驱动程序，以便它们可以被实例化。
                 * 可能的情况是驱动程序类可能不存在
                 * 即，可能有一个打包的驱动程序，其实现是 java.sql.Driver 的服务类，但实际的类可能缺失。在这种情况下，VM 在尝试定位和加载服务时会在运行时抛出 java.util.ServiceConfigurationError。
                 *
                 * 添加一个 try catch 块以捕获那些运行时错误
                 * 如果驱动程序不在类路径中但被打包为服务且该服务在类路径中。
                 */
                try{
                    while(driversIterator.hasNext()) {
                        driversIterator.next();
                    }
                } catch(Throwable t) {
                // 什么都不做
                }
                return null;
            }
        });

        println("DriverManager.initialize: jdbc.drivers = " + drivers);

        if (drivers == null || drivers.equals("")) {
            return;
        }
        String[] driversList = drivers.split(":");
        println("驱动程序数量:" + driversList.length);
        for (String aDriver : driversList) {
            try {
                println("DriverManager.Initialize: 加载 " + aDriver);
                Class.forName(aDriver, true,
                        ClassLoader.getSystemClassLoader());
            } catch (Exception ex) {
                println("DriverManager.Initialize: 加载失败: " + ex);
            }
        }
    }


    // 由公共 getConnection() 方法调用的工作方法。
    private static Connection getConnection(
        String url, java.util.Properties info, Class<?> caller) throws SQLException {
        /*
         * 当 callerCl 为 null 时，我们应该检查应用程序的
         * （间接调用此类的）
         * 类加载器，以便可以从这里加载 rt.jar 之外的 JDBC 驱动程序类。
         */
        ClassLoader callerCL = caller != null ? caller.getClassLoader() : null;
        synchronized(DriverManager.class) {
            // 同步加载正确的类加载器。
            if (callerCL == null) {
                callerCL = Thread.currentThread().getContextClassLoader();
            }
        }

        if(url == null) {
            throw new SQLException("URL 不能为 null", "08001");
        }

        println("DriverManager.getConnection(\"" + url + "\")");

        // 遍历已加载的注册驱动程序，尝试建立连接。
        // 记住第一次抛出的异常，以便我们可以重新抛出它。
        SQLException reason = null;

        for(DriverInfo aDriver : registeredDrivers) {
            // 如果调用者没有加载驱动程序的权限，则跳过。
            if(isDriverAllowed(aDriver.driver, callerCL)) {
                try {
                    println("    尝试 " + aDriver.driver.getClass().getName());
                    Connection con = aDriver.driver.connect(url, info);
                    if (con != null) {
                        // 成功！
                        println("getConnection 返回 " + aDriver.driver.getClass().getName());
                        return (con);
                    }
                } catch (SQLException ex) {
                    if (reason == null) {
                        reason = ex;
                    }
                }

            } else {
                println("    跳过: " + aDriver.getClass().getName());
            }

        }

        // 如果到达这里，说明没有人能够连接。
        if (reason != null)    {
            println("getConnection 失败: " + reason);
            throw reason;
        }

        println("getConnection: 未找到适用于 " + url + " 的合适驱动程序");
        throw new SQLException("未找到适用于 " + url + " 的合适驱动程序", "08001");
    }


}

/*
 * 注册驱动程序的包装类，以避免暴露 Driver.equals()
 * 以防止捕获正在比较的 Driver，因为它可能没有访问权限。
 */
class DriverInfo {

    final Driver driver;
    DriverAction da;
    DriverInfo(Driver driver, DriverAction action) {
        this.driver = driver;
        da = action;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof DriverInfo)
                && this.driver == ((DriverInfo) other).driver;
    }

    @Override
    public int hashCode() {
        return driver.hashCode();
    }

    @Override
    public String toString() {
        return ("driver[className="  + driver + "]");
    }

    DriverAction action() {
        return da;
    }
}
