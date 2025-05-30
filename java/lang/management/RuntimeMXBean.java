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

/**
 * Java虚拟机的运行时系统的管理接口。
 *
 * <p> Java虚拟机有一个此类实现的单个实例。此接口的实现类是一个
 * <a href="ManagementFactory.html#MXBean">MXBean</a>，可以通过调用
 * {@link ManagementFactory#getRuntimeMXBean} 方法或
 * {@link ManagementFactory#getPlatformMBeanServer
 * 平台 <tt>MBeanServer</tt>} 方法获取。
 *
 * <p>用于在MBeanServer中唯一标识运行时系统MXBean的 <tt>ObjectName</tt> 是：
 * <blockquote>
 *    {@link ManagementFactory#RUNTIME_MXBEAN_NAME
 *           <tt>java.lang:type=Runtime</tt>}
 * </blockquote>
 *
 * 可以通过调用
 * {@link PlatformManagedObject#getObjectName} 方法获取。
 *
 * <p>此接口定义了几个方便的方法，用于访问有关Java虚拟机的系统属性。
 *
 * @see ManagementFactory#getPlatformMXBeans(Class)
 * @see <a href="../../../javax/management/package-summary.html">
 *      JMX规范。</a>
 * @see <a href="package-summary.html#examples">
 *      访问MXBeans的方法</a>
 *
 * @author  Mandy Chung
 * @since   1.5
 */
public interface RuntimeMXBean extends PlatformManagedObject {
    /**
     * 返回表示正在运行的Java虚拟机的名称。
     * 返回的名称字符串可以是任意字符串，Java虚拟机实现可以选择在返回的名称字符串中嵌入平台特定的有用信息。
     * 每个正在运行的虚拟机可以有不同的名称。
     *
     * @return 表示正在运行的Java虚拟机的名称。
     */
    public String getName();

    /**
     * 返回Java虚拟机实现名称。
     * 此方法等同于 {@link System#getProperty
     * System.getProperty("java.vm.name")}。
     *
     * @return Java虚拟机实现名称。
     *
     * @throws  java.lang.SecurityException
     *     如果存在安全经理且其
     *     <code>checkPropertiesAccess</code> 方法不允许访问此系统属性。
     * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
     * @see java.lang.System#getProperty
     */
    public String getVmName();

    /**
     * 返回Java虚拟机实现供应商。
     * 此方法等同于 {@link System#getProperty
     * System.getProperty("java.vm.vendor")}。
     *
     * @return Java虚拟机实现供应商。
     *
     * @throws  java.lang.SecurityException
     *     如果存在安全经理且其
     *     <code>checkPropertiesAccess</code> 方法不允许访问此系统属性。
     * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
     * @see java.lang.System#getProperty
     */
    public String getVmVendor();

    /**
     * 返回Java虚拟机实现版本。
     * 此方法等同于 {@link System#getProperty
     * System.getProperty("java.vm.version")}。
     *
     * @return Java虚拟机实现版本。
     *
     * @throws  java.lang.SecurityException
     *     如果存在安全经理且其
     *     <code>checkPropertiesAccess</code> 方法不允许访问此系统属性。
     * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
     * @see java.lang.System#getProperty
     */
    public String getVmVersion();

    /**
     * 返回Java虚拟机规范名称。
     * 此方法等同于 {@link System#getProperty
     * System.getProperty("java.vm.specification.name")}。
     *
     * @return Java虚拟机规范名称。
     *
     * @throws  java.lang.SecurityException
     *     如果存在安全经理且其
     *     <code>checkPropertiesAccess</code> 方法不允许访问此系统属性。
     * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
     * @see java.lang.System#getProperty
     */
    public String getSpecName();

    /**
     * 返回Java虚拟机规范供应商。
     * 此方法等同于 {@link System#getProperty
     * System.getProperty("java.vm.specification.vendor")}。
     *
     * @return Java虚拟机规范供应商。
     *
     * @throws  java.lang.SecurityException
     *     如果存在安全经理且其
     *     <code>checkPropertiesAccess</code> 方法不允许访问此系统属性。
     * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
     * @see java.lang.System#getProperty
     */
    public String getSpecVendor();

    /**
     * 返回Java虚拟机规范版本。
     * 此方法等同于 {@link System#getProperty
     * System.getProperty("java.vm.specification.version")}。
     *
     * @return Java虚拟机规范版本。
     *
     * @throws  java.lang.SecurityException
     *     如果存在安全经理且其
     *     <code>checkPropertiesAccess</code> 方法不允许访问此系统属性。
     * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
     * @see java.lang.System#getProperty
     */
    public String getSpecVersion();

    /**
     * 返回正在运行的Java虚拟机实现的管理接口规范版本。
     *
     * @return 正在运行的Java虚拟机实现的管理接口规范版本。
     */
    public String getManagementSpecVersion();

    /**
     * 返回系统类加载器用于搜索类文件的Java类路径。
     * 此方法等同于 {@link System#getProperty
     * System.getProperty("java.class.path")}。
     *
     * <p> Java类路径中的多个路径由正在被监控的Java虚拟机平台的路径分隔符字符分隔。
     *
     * @return Java类路径。
     *
     * @throws  java.lang.SecurityException
     *     如果存在安全经理且其
     *     <code>checkPropertiesAccess</code> 方法不允许访问此系统属性。
     * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
     * @see java.lang.System#getProperty
     */
    public String getClassPath();

    /**
     * 返回Java库路径。
     * 此方法等同于 {@link System#getProperty
     * System.getProperty("java.library.path")}。
     *
     * <p> Java库路径中的多个路径由正在被监控的Java虚拟机平台的路径分隔符字符分隔。
     *
     * @return Java库路径。
     *
     * @throws  java.lang.SecurityException
     *     如果存在安全经理且其
     *     <code>checkPropertiesAccess</code> 方法不允许访问此系统属性。
     * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
     * @see java.lang.System#getProperty
     */
    public String getLibraryPath();

    /**
     * 测试Java虚拟机是否支持引导类路径机制，该机制用于引导类加载器搜索类文件。
     *
     * @return 如果Java虚拟机支持引导类路径机制，则返回 <tt>true</tt>；否则返回 <tt>false</tt>。
     */
    public boolean isBootClassPathSupported();

    /**
     * 返回引导类加载器用于搜索类文件的引导类路径。
     *
     * <p> 引导类路径中的多个路径由Java虚拟机运行平台的路径分隔符字符分隔。
     *
     * <p>Java虚拟机实现可能不支持引导类加载器用于搜索类文件的引导类路径机制。
     * 可以使用 {@link #isBootClassPathSupported} 方法来确定Java虚拟机是否支持此方法。
     *
     * @return 引导类路径。
     *
     * @throws java.lang.UnsupportedOperationException
     *     如果Java虚拟机不支持此操作。
     *
     * @throws  java.lang.SecurityException
     *     如果存在安全经理且调用者没有
     *     ManagementPermission("monitor") 权限。
     */
    public String getBootClassPath();

    /**
     * 返回传递给Java虚拟机的输入参数，不包括传递给 <tt>main</tt> 方法的参数。
     * 如果没有传递给Java虚拟机的输入参数，此方法返回一个空列表。
     * <p>
     * 一些Java虚拟机实现可能从多个不同的来源获取输入参数：例如，从启动Java虚拟机的应用程序（如
     * 'java' 命令）、环境变量、配置文件等。
     * <p>
     * 通常，并不是所有传递给 'java' 命令的命令行选项都传递给Java虚拟机。
     * 因此，返回的输入参数可能不包括所有命令行选项。
     *
     * <p>
     * <b>MBeanServer访问</b>：<br>
     * {@code List<String>} 的映射类型是 <tt>String[]</tt>。
     *
     * @return 一个 <tt>String</tt> 对象列表；每个元素
     * 是传递给Java虚拟机的参数。
     *
     * @throws  java.lang.SecurityException
     *     如果存在安全经理且调用者没有
     *     ManagementPermission("monitor") 权限。
     */
    public java.util.List<String> getInputArguments();

    /**
     * 返回Java虚拟机的运行时间（以毫秒为单位）。
     *
     * @return Java虚拟机的运行时间（以毫秒为单位）。
     */
    public long getUptime();

    /**
     * 返回Java虚拟机的启动时间（以毫秒为单位）。
     * 此方法返回Java虚拟机启动的大约时间。
     *
     * @return Java虚拟机的启动时间（以毫秒为单位）。
     *
     */
    public long getStartTime();

    /**
     * 返回所有系统属性的名称和值的映射。
     * 此方法调用 {@link System#getProperties} 来获取所有系统属性。名称或值不是
     * <tt>String</tt> 的属性将被忽略。
     *
     * <p>
     * <b>MBeanServer访问</b>：<br>
     * {@code Map<String,String>} 的映射类型是
     * {@link javax.management.openmbean.TabularData TabularData}，每行包含两个项目，如下所示：
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
     *     如果存在安全经理且其
     *     <code>checkPropertiesAccess</code> 方法不允许访问系统属性。
     */
    public java.util.Map<String, String> getSystemProperties();
}
