
/*
 * 版权所有 (c) 2003, 2013, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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

package java.lang.management;

/**
 * Java 虚拟机的运行时系统的管理接口。
 *
 * <p> Java 虚拟机有一个此接口实现类的单个实例。通过调用
 * {@link ManagementFactory#getRuntimeMXBean} 方法或
 * {@link ManagementFactory#getPlatformMBeanServer
 * 平台 <tt>MBeanServer</tt>} 方法可以获取实现此接口的实例。
 *
 * <p>用于在 MBeanServer 内唯一标识运行时系统 MXBean 的 <tt>ObjectName</tt> 是：
 * <blockquote>
 *    {@link ManagementFactory#RUNTIME_MXBEAN_NAME
 *           <tt>java.lang:type=Runtime</tt>}
 * </blockquote>
 *
 * 可以通过调用
 * {@link PlatformManagedObject#getObjectName} 方法获取。
 *
 * <p> 本接口定义了几个方便的方法，用于访问有关 Java 虚拟机的系统属性。
 *
 * @see ManagementFactory#getPlatformMXBeans(Class)
 * @see <a href="../../../javax/management/package-summary.html">
 *      JMX 规范。</a>
 * @see <a href="package-summary.html#examples">
 *      访问 MXBeans 的方法</a>
 *
 * @author  Mandy Chung
 * @since   1.5
 */
public interface RuntimeMXBean extends PlatformManagedObject {
    /**
     * 返回表示正在运行的 Java 虚拟机的名称。
     * 返回的名称字符串可以是任意字符串，
 * 且 Java 虚拟机实现可以选择在返回的名称字符串中嵌入平台特定的有用信息。每个正在运行的虚拟机可以有不同的名称。
     *
     * @return 表示正在运行的 Java 虚拟机的名称。
     */
    public String getName();

    /**
     * 返回 Java 虚拟机实现名称。
     * 此方法等同于 {@link System#getProperty
     * System.getProperty("java.vm.name")}。
     *
     * @return Java 虚拟机实现名称。
     *
     * @throws  java.lang.SecurityException
     *     如果存在安全经理，并且其
     *     <code>checkPropertiesAccess</code> 方法不允许访问
     *     此系统属性。
     * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
     * @see java.lang.System#getProperty
     */
    public String getVmName();

    /**
     * 返回 Java 虚拟机实现供应商。
     * 此方法等同于 {@link System#getProperty
     * System.getProperty("java.vm.vendor")}。
     *
     * @return Java 虚拟机实现供应商。
     *
     * @throws  java.lang.SecurityException
     *     如果存在安全经理，并且其
     *     <code>checkPropertiesAccess</code> 方法不允许访问
     *     此系统属性。
     * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
     * @see java.lang.System#getProperty
     */
    public String getVmVendor();

    /**
     * 返回 Java 虚拟机实现版本。
     * 此方法等同于 {@link System#getProperty
     * System.getProperty("java.vm.version")}。
     *
     * @return Java 虚拟机实现版本。
     *
     * @throws  java.lang.SecurityException
     *     如果存在安全经理，并且其
     *     <code>checkPropertiesAccess</code> 方法不允许访问
     *     此系统属性。
     * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
     * @see java.lang.System#getProperty
     */
    public String getVmVersion();

    /**
     * 返回 Java 虚拟机规范名称。
     * 此方法等同于 {@link System#getProperty
     * System.getProperty("java.vm.specification.name")}。
     *
     * @return Java 虚拟机规范名称。
     *
     * @throws  java.lang.SecurityException
     *     如果存在安全经理，并且其
     *     <code>checkPropertiesAccess</code> 方法不允许访问
     *     此系统属性。
     * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
     * @see java.lang.System#getProperty
     */
    public String getSpecName();

    /**
     * 返回 Java 虚拟机规范供应商。
     * 此方法等同于 {@link System#getProperty
     * System.getProperty("java.vm.specification.vendor")}。
     *
     * @return Java 虚拟机规范供应商。
     *
     * @throws  java.lang.SecurityException
     *     如果存在安全经理，并且其
     *     <code>checkPropertiesAccess</code> 方法不允许访问
     *     此系统属性。
     * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
     * @see java.lang.System#getProperty
     */
    public String getSpecVendor();

    /**
     * 返回 Java 虚拟机规范版本。
     * 此方法等同于 {@link System#getProperty
     * System.getProperty("java.vm.specification.version")}。
     *
     * @return Java 虚拟机规范版本。
     *
     * @throws  java.lang.SecurityException
     *     如果存在安全经理，并且其
     *     <code>checkPropertiesAccess</code> 方法不允许访问
     *     此系统属性。
     * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
     * @see java.lang.System#getProperty
     */
    public String getSpecVersion();


    /**
     * 返回正在运行的 Java 虚拟机实现的管理接口规范版本。
     *
     * @return 正在运行的 Java 虚拟机实现的管理接口规范版本。
     */
    public String getManagementSpecVersion();

    /**
     * 返回系统类加载器用于搜索类文件的 Java 类路径。
     * 此方法等同于 {@link System#getProperty
     * System.getProperty("java.class.path")}。
     *
     * <p> Java 类路径中的多个路径由 Java 虚拟机所监控平台的路径分隔符字符分隔。
     *
     * @return Java 类路径。
     *
     * @throws  java.lang.SecurityException
     *     如果存在安全经理，并且其
     *     <code>checkPropertiesAccess</code> 方法不允许访问
     *     此系统属性。
     * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
     * @see java.lang.System#getProperty
     */
    public String getClassPath();


/**
 * 返回 Java 库路径。
 * 此方法等同于 {@link System#getProperty
 * System.getProperty("java.library.path")}.
 *
 * <p> Java 库路径中的多个路径由 Java 虚拟机所运行平台的路径分隔符字符分隔。
 *
 * @return Java 库路径。
 *
 * @throws  java.lang.SecurityException
 *     如果存在安全管理器，并且其
 *     <code>checkPropertiesAccess</code> 方法不允许访问此系统属性。
 * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
 * @see java.lang.System#getProperty
 */
public String getLibraryPath();

/**
 * 测试 Java 虚拟机是否支持引导类路径机制，该机制用于引导类加载器搜索类文件。
 *
 * @return 如果 Java 虚拟机支持类路径机制，则返回 <tt>true</tt>；否则返回 <tt>false</tt>。
 */
public boolean isBootClassPathSupported();

/**
 * 返回引导类加载器用于搜索类文件的引导类路径。
 *
 * <p> 引导类路径中的多个路径由 Java
 * 虚拟机运行平台的路径分隔符字符分隔。
 *
 * <p>Java 虚拟机实现可能不支持引导类路径机制，用于引导类加载器搜索类文件。
 * 可以使用 {@link #isBootClassPathSupported} 方法来确定 Java 虚拟机是否支持此方法。
 *
 * @return 引导类路径。
 *
 * @throws java.lang.UnsupportedOperationException
 *     如果 Java 虚拟机不支持此操作。
 *
 * @throws  java.lang.SecurityException
 *     如果存在安全管理器，并且调用者没有
 *     ManagementPermission("monitor") 权限。
 */
public String getBootClassPath();

/**
 * 返回传递给 Java 虚拟机的输入参数，不包括传递给 <tt>main</tt> 方法的参数。
 * 如果没有传递给 Java 虚拟机的输入参数，此方法返回一个空列表。
 * <p>
 * 一些 Java 虚拟机实现可能从多个不同的来源获取输入参数：例如，从启动 Java 虚拟机的应用程序（如
 * 'java' 命令）、环境变量、配置文件等传递的参数。
 * <p>
 * 通常，并非所有 'java' 命令的命令行选项都传递给 Java 虚拟机。
 * 因此，返回的输入参数可能不
 * 包含所有命令行选项。
 *
 * <p>
 * <b>MBeanServer 访问</b>:<br>
 * {@code List<String>} 的映射类型是 <tt>String[]</tt>。
 *
 * @return 一个 <tt>String</tt> 对象列表；每个元素
 * 是传递给 Java 虚拟机的参数。
 *
 * @throws  java.lang.SecurityException
 *     如果存在安全管理器，并且调用者没有
 *     ManagementPermission("monitor") 权限。
 */
public java.util.List<String> getInputArguments();

/**
 * 返回 Java 虚拟机的运行时间（以毫秒为单位）。
 *
 * @return Java 虚拟机的运行时间（以毫秒为单位）。
 */
public long getUptime();

/**
 * 返回 Java 虚拟机的启动时间（以毫秒为单位）。
 * 此方法返回 Java 虚拟机启动时的近似时间。
 *
 * @return Java 虚拟机的启动时间（以毫秒为单位）。
 *
 */
public long getStartTime();

/**
 * 返回所有系统属性的名称和值的映射。
 * 此方法调用 {@link System#getProperties} 获取所有
 * 系统属性。名称或值不是 <tt>String</tt> 的属性将被忽略。
 *
 * <p>
 * <b>MBeanServer 访问</b>:<br>
 * {@code Map<String,String>} 的映射类型是
 * {@link javax.management.openmbean.TabularData TabularData}
 * 每行包含两个项目，如下所示：
 * <blockquote>
 * <table border summary="每个项目的名称和类型">
 * <tr>
 *   <th>项目名称</th>
 *   <th>项目类型</th>
 *   </tr>
 * <tr>
 *   <td><tt>key</tt></td>
 *   <td><tt>String</tt></td>
 *   </tr>
 * <tr>
 *   <td><tt>value</tt></td>
 *   <td><tt>String</tt></td>
 *   </tr>
 * </table>
 * </blockquote>
 *
 * @return 所有系统属性的名称和值的映射。
 *
 * @throws  java.lang.SecurityException
 *     如果存在安全管理器，并且其
 *     <code>checkPropertiesAccess</code> 方法不允许访问系统属性。
 */
public java.util.Map<String, String> getSystemProperties();
}
