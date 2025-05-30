
/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.management;
import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerPermission;
import javax.management.NotificationEmitter;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardEmitterMBean;
import javax.management.StandardMBean;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import javax.management.JMX;
import sun.management.ManagementFactoryHelper;
import sun.management.ExtendedPlatformComponent;

/**
 * {@code ManagementFactory} 类是一个用于获取 Java 平台管理 Bean 的工厂类。
 * 该类包含多个静态方法，每个方法返回一个或多个表示 Java 虚拟机组件管理接口的 <i>平台 MXBean</i>。
 *
 * <h3><a name="MXBean">平台 MXBean</a></h3>
 * <p>
 * 平台 MXBean 是一个符合 <a href="../../../javax/management/package-summary.html">JMX</a>
 * 仪表规范的 <i>管理 Bean</i>，仅使用一组基本数据类型。
 * JMX 管理应用程序和 {@linkplain
 * #getPlatformMBeanServer 平台 MBeanServer}
 * 可以在不需要 MXBean 特定数据类型的类的情况下进行互操作。
 * 通过 JMX 连接器服务器和连接器客户端之间传输的数据类型是
 * {@linkplain javax.management.openmbean.OpenType 开放类型}，
 * 这允许跨版本的互操作。
 * 有关 MXBean 的详细规范，请参见
 * <a href="../../../javax/management/MXBean.html#MXBean-spec">
 * MXBean 规范</a>。
 *
 * <a name="MXBeanNames"></a>
 * <p>每个平台 MXBean 都是一个 {@link PlatformManagedObject}
 * 并具有一个唯一的
 * {@link javax.management.ObjectName ObjectName}，
 * 用于在平台 {@code MBeanServer} 中注册，该 ObjectName 由
 * {@link PlatformManagedObject#getObjectName getObjectName}
 * 方法返回。
 *
 * <p>
 * 应用程序可以通过以下方式访问平台 MXBean：
 * <h4>1. 直接访问 MXBean 接口</h4>
 * <blockquote>
 * <ul>
 *     <li>通过调用
 *         {@link #getPlatformMXBean(Class) getPlatformMXBean} 或
 *         {@link #getPlatformMXBeans(Class) getPlatformMXBeans} 方法获取 MXBean 实例，
 *         并在运行中的虚拟机中本地访问 MXBean。
 *         </li>
 *     <li>通过调用
 *         {@link #getPlatformMXBean(MBeanServerConnection, Class)} 或
 *         {@link #getPlatformMXBeans(MBeanServerConnection, Class)} 方法构造一个 MXBean 代理实例，
 *         该代理实例将方法调用转发到给定的 {@link MBeanServer MBeanServer}。
 *         {@link #newPlatformMXBeanProxy newPlatformMXBeanProxy} 方法也可以用于构造给定
 *         {@code ObjectName} 的 MXBean 代理实例。
 *         代理通常用于远程访问另一个运行中的虚拟机的 MXBean。
 *         </li>
 * </ul>
 * <h4>2. 通过 MBeanServer 间接访问 MXBean 接口</h4>
 * <ul>
 *     <li>通过平台 {@code MBeanServer} 访问 MXBean，
 *         或通过特定的 <tt>MBeanServerConnection</tt> 远程访问 MXBean。
 *         MXBean 的属性和操作仅使用
 *         <em>JMX 开放类型</em>，包括基本数据类型、
 *         {@link javax.management.openmbean.CompositeData CompositeData} 和
 *         {@link javax.management.openmbean.TabularData TabularData}，
 *         这些类型定义在
 *         {@link javax.management.openmbean.OpenType OpenType} 中。
 *         映射关系在
 *         {@linkplain javax.management.MXBean MXBean} 规范中指定。
 *        </li>
 * </ul>
 * </blockquote>
 *
 * <p>
 * {@link #getPlatformManagementInterfaces getPlatformManagementInterfaces}
 * 方法返回 Java 虚拟机支持的所有管理接口，包括以下表格中列出的标准管理接口以及 JDK 实现扩展的管理接口。
 * <p>
 * Java 虚拟机有一个以下管理接口的单个实例：
 *
 * <blockquote>
 * <table border summary="管理接口及其单个实例的列表">
 * <tr>
 * <th>管理接口</th>
 * <th>ObjectName</th>
 * </tr>
 * <tr>
 * <td> {@link ClassLoadingMXBean} </td>
 * <td> {@link #CLASS_LOADING_MXBEAN_NAME
 *             java.lang:type=ClassLoading}</td>
 * </tr>
 * <tr>
 * <td> {@link MemoryMXBean} </td>
 * <td> {@link #MEMORY_MXBEAN_NAME
 *             java.lang:type=Memory}</td>
 * </tr>
 * <tr>
 * <td> {@link ThreadMXBean} </td>
 * <td> {@link #THREAD_MXBEAN_NAME
 *             java.lang:type=Threading}</td>
 * </tr>
 * <tr>
 * <td> {@link RuntimeMXBean} </td>
 * <td> {@link #RUNTIME_MXBEAN_NAME
 *             java.lang:type=Runtime}</td>
 * </tr>
 * <tr>
 * <td> {@link OperatingSystemMXBean} </td>
 * <td> {@link #OPERATING_SYSTEM_MXBEAN_NAME
 *             java.lang:type=OperatingSystem}</td>
 * </tr>
 * <tr>
 * <td> {@link PlatformLoggingMXBean} </td>
 * <td> {@link java.util.logging.LogManager#LOGGING_MXBEAN_NAME
 *             java.util.logging:type=Logging}</td>
 * </tr>
 * </table>
 * </blockquote>
 *
 * <p>
 * Java 虚拟机可能有一个以下管理接口的单个实例。
 *
 * <blockquote>
 * <table border summary="管理接口及其单个实例的列表">
 * <tr>
 * <th>管理接口</th>
 * <th>ObjectName</th>
 * </tr>
 * <tr>
 * <td> {@link CompilationMXBean} </td>
 * <td> {@link #COMPILATION_MXBEAN_NAME
 *             java.lang:type=Compilation}</td>
 * </tr>
 * </table>
 * </blockquote>
 *
 * <p>
 * Java 虚拟机可能有一个或多个以下管理接口的实例。
 * <blockquote>
 * <table border summary="管理接口及其单个实例的列表">
 * <tr>
 * <th>管理接口</th>
 * <th>ObjectName</th>
 * </tr>
 * <tr>
 * <td> {@link GarbageCollectorMXBean} </td>
 * <td> {@link #GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE
 *             java.lang:type=GarbageCollector}<tt>,name=</tt><i>收集器的名称</i></td>
 * </tr>
 * <tr>
 * <td> {@link MemoryManagerMXBean} </td>
 * <td> {@link #MEMORY_MANAGER_MXBEAN_DOMAIN_TYPE
 *             java.lang:type=MemoryManager}<tt>,name=</tt><i>管理器的名称</i></td>
 * </tr>
 * <tr>
 * <td> {@link MemoryPoolMXBean} </td>
 * <td> {@link #MEMORY_POOL_MXBEAN_DOMAIN_TYPE
 *             java.lang:type=MemoryPool}<tt>,name=</tt><i>池的名称</i></td>
 * </tr>
 * <tr>
 * <td> {@link BufferPoolMXBean} </td>
 * <td> {@code java.nio:type=BufferPool,name=}<i>池名称</i></td>
 * </tr>
 * </table>
 * </blockquote>
 *
 * @see <a href="../../../javax/management/package-summary.html">
 *      JMX 规范</a>
 * @see <a href="package-summary.html#examples">
 *      访问管理指标的方式</a>
 * @see javax.management.MXBean
 *
 * @author  Mandy Chung
 * @since   1.5
 */
public class ManagementFactory {
    // 一个只有静态字段和方法的类。
    private ManagementFactory() {};

    /**
     * {@link ClassLoadingMXBean} 的
     * <tt>ObjectName</tt> 的字符串表示形式。
     */
    public final static String CLASS_LOADING_MXBEAN_NAME =
        "java.lang:type=ClassLoading";

    /**
     * {@link CompilationMXBean} 的
     * <tt>ObjectName</tt> 的字符串表示形式。
     */
    public final static String COMPILATION_MXBEAN_NAME =
        "java.lang:type=Compilation";

    /**
     * {@link MemoryMXBean} 的
     * <tt>ObjectName</tt> 的字符串表示形式。
     */
    public final static String MEMORY_MXBEAN_NAME =
        "java.lang:type=Memory";

    /**
     * {@link OperatingSystemMXBean} 的
     * <tt>ObjectName</tt> 的字符串表示形式。
     */
    public final static String OPERATING_SYSTEM_MXBEAN_NAME =
        "java.lang:type=OperatingSystem";

    /**
     * {@link RuntimeMXBean} 的
     * <tt>ObjectName</tt> 的字符串表示形式。
     */
    public final static String RUNTIME_MXBEAN_NAME =
        "java.lang:type=Runtime";

    /**
     * {@link ThreadMXBean} 的
     * <tt>ObjectName</tt> 的字符串表示形式。
     */
    public final static String THREAD_MXBEAN_NAME =
        "java.lang:type=Threading";

    /**
     * {@link GarbageCollectorMXBean} 的
     * <tt>ObjectName</tt> 的域名和类型键属性。
     * 通过将此字符串与 "<tt>,name=</tt><i>收集器的名称</i>" 连接起来，
     * 可以形成 <tt>GarbageCollectorMXBean</tt> 的唯一 <tt>ObjectName</tt>。
     */
    public final static String GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE =
        "java.lang:type=GarbageCollector";

    /**
     * {@link MemoryManagerMXBean} 的
     * <tt>ObjectName</tt> 的域名和类型键属性。
     * 通过将此字符串与 "<tt>,name=</tt><i>管理器的名称</i>" 连接起来，
     * 可以形成 <tt>MemoryManagerMXBean</tt> 的唯一 <tt>ObjectName</tt>。
     */
    public final static String MEMORY_MANAGER_MXBEAN_DOMAIN_TYPE =
        "java.lang:type=MemoryManager";

    /**
     * {@link MemoryPoolMXBean} 的
     * <tt>ObjectName</tt> 的域名和类型键属性。
     * 通过将此字符串与 <tt>,name=</tt><i>池的名称</i> 连接起来，
     * 可以形成 <tt>MemoryPoolMXBean</tt> 的唯一 <tt>ObjectName</tt>。
     */
    public final static String MEMORY_POOL_MXBEAN_DOMAIN_TYPE =
        "java.lang:type=MemoryPool";

    /**
     * 返回 Java 虚拟机的类加载系统的管理 Bean。
     *
     * @return 一个 {@link ClassLoadingMXBean} 对象，用于 Java 虚拟机。
     */
    public static ClassLoadingMXBean getClassLoadingMXBean() {
        return ManagementFactoryHelper.getClassLoadingMXBean();
    }

    /**
     * 返回 Java 虚拟机的内存系统的管理 Bean。
     *
     * @return 一个 {@link MemoryMXBean} 对象，用于 Java 虚拟机。
     */
    public static MemoryMXBean getMemoryMXBean() {
        return ManagementFactoryHelper.getMemoryMXBean();
    }

    /**
     * 返回 Java 虚拟机的线程系统的管理 Bean。
     *
     * @return 一个 {@link ThreadMXBean} 对象，用于 Java 虚拟机。
     */
    public static ThreadMXBean getThreadMXBean() {
        return ManagementFactoryHelper.getThreadMXBean();
    }

    /**
     * 返回 Java 虚拟机的运行时系统的管理 Bean。
     *
     * @return 一个 {@link RuntimeMXBean} 对象，用于 Java 虚拟机。
     */
    public static RuntimeMXBean getRuntimeMXBean() {
        return ManagementFactoryHelper.getRuntimeMXBean();
    }

    /**
     * 返回 Java 虚拟机的编译系统的管理 Bean。
     * 如果 Java 虚拟机没有编译系统，此方法返回 <tt>null</tt>。
     *
     * @return 一个 {@link CompilationMXBean} 对象，用于 Java 虚拟机，
     *   或 <tt>null</tt>，如果 Java 虚拟机没有编译系统。
     */
    public static CompilationMXBean getCompilationMXBean() {
        return ManagementFactoryHelper.getCompilationMXBean();
    }

    /**
     * 返回 Java 虚拟机运行所在的操作系统的管理 Bean。
     *
     * @return 一个 {@link OperatingSystemMXBean} 对象，用于 Java 虚拟机。
     */
    public static OperatingSystemMXBean getOperatingSystemMXBean() {
        return ManagementFactoryHelper.getOperatingSystemMXBean();
    }

    /**
     * 返回 Java 虚拟机中的 {@link MemoryPoolMXBean} 对象列表。
     * Java 虚拟机可以有一个或多个内存池。
     * 它可能在执行过程中添加或删除内存池。
     *
     * @return 一个 <tt>MemoryPoolMXBean</tt> 对象列表。
     *
     */
    public static List<MemoryPoolMXBean> getMemoryPoolMXBeans() {
        return ManagementFactoryHelper.getMemoryPoolMXBeans();
    }

    /**
     * 返回 Java 虚拟机中的 {@link MemoryManagerMXBean} 对象列表。
     * Java 虚拟机可以有一个或多个内存管理器。
     * 它可能在执行过程中添加或删除内存管理器。
     *
     * @return 一个 <tt>MemoryManagerMXBean</tt> 对象列表。
     *
     */
    public static List<MemoryManagerMXBean> getMemoryManagerMXBeans() {
        return ManagementFactoryHelper.getMemoryManagerMXBeans();
    }


    /**
     * 返回 Java 虚拟机中的 {@link GarbageCollectorMXBean} 对象列表。
     * Java 虚拟机可能有一个或多个
     * <tt>GarbageCollectorMXBean</tt> 对象。
     * 它可能在执行过程中添加或删除 <tt>GarbageCollectorMXBean</tt>。
     *
     * @return 一个 <tt>GarbageCollectorMXBean</tt> 对象列表。
     *
     */
    public static List<GarbageCollectorMXBean> getGarbageCollectorMXBeans() {
        return ManagementFactoryHelper.getGarbageCollectorMXBeans();
    }

    private static MBeanServer platformMBeanServer;
    /**
     * 返回平台 {@link javax.management.MBeanServer MBeanServer}。
     * 在首次调用此方法时，它首先通过调用
     * {@link javax.management.MBeanServerFactory#createMBeanServer
     * MBeanServerFactory.createMBeanServer}
     * 方法创建平台 {@code MBeanServer}，并将每个平台 MXBean 以
     * 其 {@link PlatformManagedObject#getObjectName ObjectName}
     * 注册到此平台 {@code MBeanServer} 中。
     * 在后续调用中，此方法将简单地返回最初创建的平台 {@code MBeanServer}。
     * <p>
     * 动态创建和销毁的 MXBean，例如内存 {@link MemoryPoolMXBean 池} 和
     * {@link MemoryManagerMXBean 管理器}，
     * 将自动注册和注销到平台 {@code MBeanServer} 中。
     * <p>
     * 如果系统属性 {@code javax.management.builder.initial}
     * 已设置，平台 {@code MBeanServer} 的创建将由指定的
     * {@link javax.management.MBeanServerBuilder} 完成。
     * <p>
     * 建议除了平台 MXBean 之外，还使用此平台 MBeanServer 注册其他应用程序管理 Bean。
     * 这将允许所有 MBean 通过同一个
     * {@code MBeanServer} 发布，从而便于网络发布和发现。
     * 应避免与平台 MXBean 的名称冲突。
     *
     * @return 平台 {@code MBeanServer}；在首次调用此方法时，平台
     *         MXBean 将注册到平台 {@code MBeanServer} 中。
     *
     * @exception SecurityException 如果存在安全管理器，并且调用者没有
     * {@link javax.management.MBeanServerFactory#createMBeanServer} 所需的权限。
     *
     * @see javax.management.MBeanServerFactory
     * @see javax.management.MBeanServerFactory#createMBeanServer
     */
    public static synchronized MBeanServer getPlatformMBeanServer() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            Permission perm = new MBeanServerPermission("createMBeanServer");
            sm.checkPermission(perm);
        }


                    if (platformMBeanServer == null) {
            platformMBeanServer = MBeanServerFactory.createMBeanServer();
            for (PlatformComponent pc : PlatformComponent.values()) {
                List<? extends PlatformManagedObject> list =
                    pc.getMXBeans(pc.getMXBeanInterface());
                for (PlatformManagedObject o : list) {
                    // 每个 PlatformComponent 代表一个管理
                    // 接口。某些 MXBean 可能扩展另一个接口。
                    // 一个平台组件的 MXBean 实例
                    // （由 pc.getMXBeans() 返回）也可能是
                    // 另一个平台组件的 MXBean 实例。
                    // 例如：com.sun.management.GarbageCollectorMXBean
                    //
                    // 因此在注册到平台 MBeanServer 之前需要检查
                    // 一个 MXBean 实例是否已注册
                    if (!platformMBeanServer.isRegistered(o.getObjectName())) {
                        addMXBean(platformMBeanServer, o);
                    }
                }
            }
            HashMap<ObjectName, DynamicMBean> dynmbeans =
                    ManagementFactoryHelper.getPlatformDynamicMBeans();
            for (Map.Entry<ObjectName, DynamicMBean> e : dynmbeans.entrySet()) {
                addDynamicMBean(platformMBeanServer, e.getValue(), e.getKey());
            }
            for (final PlatformManagedObject o :
                                       ExtendedPlatformComponent.getMXBeans()) {
                if (!platformMBeanServer.isRegistered(o.getObjectName())) {
                    addMXBean(platformMBeanServer, o);
                }
            }
        }
        return platformMBeanServer;
    }

    /**
     * 返回一个代理，该代理实现了给定的平台 MXBean 接口
     * 并通过给定的 <tt>MBeanServerConnection</tt> 转发其方法调用。
     *
     * <p>此方法等效于：
     * <blockquote>
     * {@link java.lang.reflect.Proxy#newProxyInstance
     *        Proxy.newProxyInstance}<tt>(mxbeanInterface.getClassLoader(),
     *        new Class[] { mxbeanInterface }, handler)</tt>
     * </blockquote>
     *
     * 其中 <tt>handler</tt> 是一个 {@link java.lang.reflect.InvocationHandler
     * InvocationHandler}，方法调用将被分派到 MXBean 接口。此 <tt>handler</tt> 在转发到
     * <tt>MBeanServer</tt> 之前，将输入参数从 MXBean 数据类型转换为其映射的开放类型，
     * 并将通过 <tt>MBeanServer</tt> 的 MXBean 方法调用的返回值从开放类型转换为
     * MXBean 接口中声明的相应返回类型。
     *
     * <p>
     * 如果 MXBean 是一个通知发射器（即，
     * 实现了
     * {@link javax.management.NotificationEmitter NotificationEmitter}），
     * 则此代理将同时实现 <tt>mxbeanInterface</tt> 和 <tt>NotificationEmitter</tt>。
     *
     * <p>
     * <b>注意事项：</b>
     * <ol>
     * <li>使用 MXBean 代理是方便远程访问运行中的虚拟机的平台 MXBean。所有对 MXBean 代理的方法
     * 调用都将转发到一个 <tt>MBeanServerConnection</tt>，当与连接服务器的通信出现问题时，
     * 可能会抛出 {@link java.io.IOException IOException}。
     * 使用代理远程访问平台 MXBeans 的应用程序应准备好捕获 <tt>IOException</tt>，就像
     * 使用 <tt>MBeanServerConnector</tt> 接口访问一样。</li>
     *
     * <li>当客户端应用程序设计为远程访问运行中的虚拟机的 MXBeans，而该虚拟机的版本与
     * 应用程序运行的版本不同，
     * 它应准备好捕获
     * {@link java.io.InvalidObjectException InvalidObjectException}
     * 当 MXBean 代理接收到一个枚举常量名称，而该枚举常量在客户端应用程序加载的枚举类中缺失时，将抛出此异常。</li>
     *
     * <li>{@link javax.management.MBeanServerInvocationHandler
     * MBeanServerInvocationHandler} 或其
     * {@link javax.management.MBeanServerInvocationHandler#newProxyInstance
     * newProxyInstance} 方法不能用于创建
     * 平台 MXBean 的代理。由 <tt>MBeanServerInvocationHandler</tt> 创建的代理对象不处理
     * 平台 MXBeans 在
     * <a href="#MXBean">类规范</a> 中描述的属性。
     *</li>
     * </ol>
     *
     * @param connection 要转发到的 <tt>MBeanServerConnection</tt>。
     * @param mxbeanName 要转发到的平台 MXBean 的名称，该 MXBean 位于 <tt>connection</tt> 中。<tt>mxbeanName</tt> 必须是
     * {@link ObjectName ObjectName} 格式。
     * @param mxbeanInterface 由代理实现的 MXBean 接口。
     * @param <T> 一个 {@code mxbeanInterface} 类型参数
     *
     * @return 一个代理，该代理实现了给定的平台 MXBean 接口
     * 并通过给定的 <tt>MBeanServerConnection</tt> 转发其方法调用，如果不存在则返回 {@code null}。
     *
     * @throws IllegalArgumentException 如果
     * <ul>
     * <li><tt>mxbeanName</tt> 不是有效的
     *     {@link ObjectName ObjectName} 格式，或者</li>
     * <li>在 <tt>connection</tt> 中命名的 MXBean 不是由平台提供的 MXBean，或者</li>
     * <li>在 <tt>MBeanServerConnection</tt> 中未注册命名的 MXBean，或者</li>
     * <li>命名的 MXBean 不是给定的 <tt>mxbeanInterface</tt> 的实例</li>
     * </ul>
     *
     * @throws java.io.IOException 如果在访问 <tt>MBeanServerConnection</tt> 时发生通信问题。
     */
    public static <T> T
        newPlatformMXBeanProxy(MBeanServerConnection connection,
                               String mxbeanName,
                               Class<T> mxbeanInterface)
            throws java.io.IOException {

        // 仅允许由引导类加载器从 rt.jar 加载的 MXBean 接口
        final Class<?> cls = mxbeanInterface;
        ClassLoader loader =
            AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                public ClassLoader run() {
                    return cls.getClassLoader();
                }
            });
        if (!sun.misc.VM.isSystemDomainLoader(loader)) {
            throw new IllegalArgumentException(mxbeanName +
                " 不是平台 MXBean");
        }

        try {
            final ObjectName objName = new ObjectName(mxbeanName);
            // 跳过 LoggingMXBean 的 isInstanceOf 检查
            String intfName = mxbeanInterface.getName();
            if (!connection.isInstanceOf(objName, intfName)) {
                throw new IllegalArgumentException(mxbeanName +
                    " 不是 " + mxbeanInterface + " 的实例");
            }

            final Class[] interfaces;
            // 检查已注册的 MBean 是否是通知发射器
            boolean emitter = connection.isInstanceOf(objName, NOTIF_EMITTER);

            // 创建一个 MXBean 代理
            return JMX.newMXBeanProxy(connection, objName, mxbeanInterface,
                                      emitter);
        } catch (InstanceNotFoundException|MalformedObjectNameException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 返回实现给定的 {@code mxbeanInterface} 的平台 MXBean，该接口在 Java 虚拟机中指定
     * 为只有一个实例。如果 Java 虚拟机未实现该管理接口（例如，
     * 没有编译系统的 Java 虚拟机不实现 {@link CompilationMXBean}），
     * 则此方法可能返回 {@code null}；
     * 否则，此方法等效于调用：
     * <pre>
     *    {@link #getPlatformMXBeans(Class)
     *      getPlatformMXBeans(mxbeanInterface)}.get(0);
     * </pre>
     *
     * @param mxbeanInterface 一个管理接口，用于在 Java 虚拟机中实现的平台
     *     MXBean，如果实现则只有一个实例。
     * @param <T> 一个 {@code mxbeanInterface} 类型参数
     *
     * @return 实现 {@code mxbeanInterface} 的平台 MXBean，如果不存在则返回 {@code null}。
     *
     * @throws IllegalArgumentException 如果 {@code mxbeanInterface}
     * 不是平台管理接口或不是单例平台 MXBean。
     *
     * @since 1.7
     */
    public static <T extends PlatformManagedObject>
            T getPlatformMXBean(Class<T> mxbeanInterface) {
        PlatformComponent pc = PlatformComponent.getPlatformComponent(mxbeanInterface);
        if (pc == null) {
            T mbean = ExtendedPlatformComponent.getMXBean(mxbeanInterface);
            if (mbean != null) {
                return mbean;
            }
            throw new IllegalArgumentException(mxbeanInterface.getName() +
                " 不是平台管理接口");
        }
        if (!pc.isSingleton())
            throw new IllegalArgumentException(mxbeanInterface.getName() +
                " 可以有零个或多个实例");

        return pc.getSingletonMXBean(mxbeanInterface);
    }

    /**
     * 返回实现给定的 {@code mxbeanInterface} 的平台 MXBean 列表。
     * 返回的列表可能包含零个、一个或多个实例。
     * 返回列表中的实例数量由给定管理接口的规范定义。
     * 返回列表的顺序是未定义的，没有保证返回的列表与之前的调用顺序相同。
     *
     * @param mxbeanInterface 一个管理接口，用于平台
     *                        MXBean
     * @param <T> 一个 {@code mxbeanInterface} 类型参数
     *
     * @return 实现 {@code mxbeanInterface} 的平台 MXBean 列表。
     *
     * @throws IllegalArgumentException 如果 {@code mxbeanInterface}
     * 不是平台管理接口。
     *
     * @since 1.7
     */
    public static <T extends PlatformManagedObject> List<T>
            getPlatformMXBeans(Class<T> mxbeanInterface) {
        PlatformComponent pc = PlatformComponent.getPlatformComponent(mxbeanInterface);
        if (pc == null) {
            T mbean = ExtendedPlatformComponent.getMXBean(mxbeanInterface);
            if (mbean != null) {
                return Collections.singletonList(mbean);
            }
            throw new IllegalArgumentException(mxbeanInterface.getName() +
                " 不是平台管理接口");
        }
        return Collections.unmodifiableList(pc.getMXBeans(mxbeanInterface));
    }

    /**
     * 返回一个平台 MXBean 代理，该代理用于实现给定的
     * {@code mxbeanInterface}，该接口在 Java 虚拟机中指定为只有一个实例，
     * 并且该代理将通过给定的 {@code MBeanServerConnection} 转发方法调用。
     * 如果 Java 虚拟机未实现该管理接口（例如，
     * 没有编译系统的 Java 虚拟机不实现 {@link CompilationMXBean}），
     * 则此方法可能返回 {@code null}；
     * 否则，此方法等效于调用：
     * <pre>
     *     {@link #getPlatformMXBeans(MBeanServerConnection, Class)
     *        getPlatformMXBeans(connection, mxbeanInterface)}.get(0);
     * </pre>
     *
     * @param connection 要转发到的 {@code MBeanServerConnection}。
     * @param mxbeanInterface 一个管理接口，用于在被监控的 Java 虚拟机中实现的平台
     *     MXBean，如果实现则只有一个实例。
     * @param <T> 一个 {@code mxbeanInterface} 类型参数
     *
     * @return 一个平台 MXBean 代理，用于通过给定的 {@code MBeanServerConnection}
     * 转发 {@code mxbeanInterface} 的方法调用，如果不存在则返回 {@code null}。
     *
     * @throws IllegalArgumentException 如果 {@code mxbeanInterface}
     * 不是平台管理接口或不是单例平台 MXBean。
     * @throws java.io.IOException 如果在访问 {@code MBeanServerConnection} 时发生通信问题。
     *
     * @see #newPlatformMXBeanProxy
     * @since 1.7
     */
    public static <T extends PlatformManagedObject>
            T getPlatformMXBean(MBeanServerConnection connection,
                                Class<T> mxbeanInterface)
        throws java.io.IOException
    {
        PlatformComponent pc = PlatformComponent.getPlatformComponent(mxbeanInterface);
        if (pc == null) {
            T mbean = ExtendedPlatformComponent.getMXBean(mxbeanInterface);
            if (mbean != null) {
                ObjectName on = mbean.getObjectName();
                return ManagementFactory.newPlatformMXBeanProxy(connection,
                                                                on.getCanonicalName(),
                                                                mxbeanInterface);
            }
            throw new IllegalArgumentException(mxbeanInterface.getName() +
                " 不是平台管理接口");
        }
        if (!pc.isSingleton())
            throw new IllegalArgumentException(mxbeanInterface.getName() +
                " 可以有零个或多个实例");
        return pc.getSingletonMXBean(connection, mxbeanInterface);
    }

    /**
     * 返回一个平台 MXBean 代理列表，用于通过给定的 {@code MBeanServerConnection}
     * 转发 {@code mxbeanInterface} 的方法调用。
     * 返回的列表可能包含零个、一个或多个实例。
     * 返回列表中的实例数量由给定管理接口的规范定义。
     * 返回列表的顺序是未定义的，没有保证返回的列表与之前的调用顺序相同。
     *
     * @param connection 要转发到的 {@code MBeanServerConnection}。
     * @param mxbeanInterface 一个管理接口，用于平台
     *                        MXBean
     * @param <T> 一个 {@code mxbeanInterface} 类型参数
     *
     * @return 一个平台 MXBean 代理列表，用于通过给定的 {@code MBeanServerConnection}
     * 转发 {@code mxbeanInterface} 的方法调用。
     *
     * @throws IllegalArgumentException 如果 {@code mxbeanInterface}
     * 不是平台管理接口。
     *
     * @throws java.io.IOException 如果在访问 {@code MBeanServerConnection} 时发生通信问题。
     *
     * @see #newPlatformMXBeanProxy
     * @since 1.7
     */
    public static <T extends PlatformManagedObject>
            List<T> getPlatformMXBeans(MBeanServerConnection connection,
                                       Class<T> mxbeanInterface)
        throws java.io.IOException
    {
        PlatformComponent pc = PlatformComponent.getPlatformComponent(mxbeanInterface);
        if (pc == null) {
            T mbean = ExtendedPlatformComponent.getMXBean(mxbeanInterface);
            if (mbean != null) {
                ObjectName on = mbean.getObjectName();
                T proxy = ManagementFactory.newPlatformMXBeanProxy(connection,
                            on.getCanonicalName(), mxbeanInterface);
                return Collections.singletonList(proxy);
            }
            throw new IllegalArgumentException(mxbeanInterface.getName() +
                " 不是平台管理接口");
        }
        return Collections.unmodifiableList(pc.getMXBeans(connection, mxbeanInterface));
    }


                /**
     * 返回表示用于
     * 监控和管理 Java 平台的所有管理接口的 {@code Class} 对象集合，
     * 这些接口是 {@link PlatformManagedObject} 的子接口。
     *
     * @return 表示用于
     * 监控和管理 Java 平台的管理接口的 {@code Class} 对象集合，
     * 这些接口是 {@link PlatformManagedObject} 的子接口。
     *
     * @since 1.7
     */
    public static Set<Class<? extends PlatformManagedObject>>
           getPlatformManagementInterfaces()
    {
        Set<Class<? extends PlatformManagedObject>> result =
            new HashSet<>();
        for (PlatformComponent component: PlatformComponent.values()) {
            result.add(component.getMXBeanInterface());
        }
        return Collections.unmodifiableSet(result);
    }

    private static final String NOTIF_EMITTER =
        "javax.management.NotificationEmitter";

    /**
     * 注册一个 MXBean。
     */
    private static void addMXBean(final MBeanServer mbs, final PlatformManagedObject pmo) {
        // 通过使用 StandardMBean 包装 MXBean 使其成为 DynamicMBean
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                public Void run() throws InstanceAlreadyExistsException,
                                         MBeanRegistrationException,
                                         NotCompliantMBeanException {
                    final DynamicMBean dmbean;
                    if (pmo instanceof DynamicMBean) {
                        dmbean = DynamicMBean.class.cast(pmo);
                    } else if (pmo instanceof NotificationEmitter) {
                        dmbean = new StandardEmitterMBean(pmo, null, true, (NotificationEmitter) pmo);
                    } else {
                        dmbean = new StandardMBean(pmo, null, true);
                    }

                    mbs.registerMBean(dmbean, pmo.getObjectName());
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw new RuntimeException(e.getException());
        }
    }

    /**
     * 注册一个 DynamicMBean。
     */
    private static void addDynamicMBean(final MBeanServer mbs,
                                        final DynamicMBean dmbean,
                                        final ObjectName on) {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                @Override
                public Void run() throws InstanceAlreadyExistsException,
                                         MBeanRegistrationException,
                                         NotCompliantMBeanException {
                    mbs.registerMBean(dmbean, on);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw new RuntimeException(e.getException());
        }
    }
}
