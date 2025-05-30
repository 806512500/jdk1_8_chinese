
/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

import java.security.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * 这个类用于运行时权限。一个 RuntimePermission 包含一个名称（也称为“目标名称”），但不包含操作列表；你要么拥有命名的权限，要么没有。
 *
 * <P>
 * 目标名称是运行时权限的名称（见下文）。命名约定遵循层次属性命名约定。此外，名称末尾可以出现一个星号，跟随一个“.”，或单独出现，以表示通配符匹配。例如：“loadLibrary.*”和“*”表示通配符匹配，而“*loadLibrary”和“a*b”则不表示。
 * <P>
 * 下表列出了所有可能的 RuntimePermission 目标名称，并为每个名称提供了允许的操作描述以及授予代码该权限的风险讨论。
 *
 * <table border=1 cellpadding=5 summary="permission target name,
 *  what the target allows,and associated risks">
 * <tr>
 * <th>权限目标名称</th>
 * <th>允许的操作</th>
 * <th>允许此权限的风险</th>
 * </tr>
 *
 * <tr>
 *   <td>createClassLoader</td>
 *   <td>创建类加载器</td>
 *   <td>这是一个非常危险的权限。恶意应用程序如果可以实例化自己的类加载器，就可以加载自己的恶意类到系统中。这些新加载的类可以通过类加载器放置在任何保护域中，从而自动授予这些类该域的权限。</td>
 * </tr>
 *
 * <tr>
 *   <td>getClassLoader</td>
 *   <td>检索类加载器（例如，调用类的类加载器）</td>
 *   <td>这将授予攻击者获取特定类的类加载器的权限。这是危险的，因为能够访问类的类加载器允许攻击者加载该类加载器可用的其他类。攻击者通常无法访问这些类。</td>
 * </tr>
 *
 * <tr>
 *   <td>setContextClassLoader</td>
 *   <td>设置线程使用的上下文类加载器</td>
 *   <td>上下文类加载器用于系统代码和扩展，当它们需要查找可能不存在于系统类加载器中的资源时。授予 setContextClassLoader 权限将允许代码更改特定线程使用的上下文类加载器，包括系统线程。</td>
 * </tr>
 *
 * <tr>
 *   <td>enableContextClassLoaderOverride</td>
 *   <td>子类实现线程上下文类加载器方法</td>
 *   <td>上下文类加载器用于系统代码和扩展，当它们需要查找可能不存在于系统类加载器中的资源时。授予 enableContextClassLoaderOverride 权限将允许 Thread 的子类覆盖用于获取或设置特定线程上下文类加载器的方法。</td>
 * </tr>
 *
 * <tr>
 *   <td>closeClassLoader</td>
 *   <td>关闭 ClassLoader</td>
 *   <td>授予此权限允许代码关闭它引用的任何 URLClassLoader。</td>
 * </tr>
 *
 * <tr>
 *   <td>setSecurityManager</td>
 *   <td>设置安全经理（可能替换现有的安全经理）</td>
 *   <td>安全经理是一个允许应用程序实现安全策略的类。授予 setSecurityManager 权限将允许代码通过安装不同的、可能更宽松的安全经理来更改使用哪个安全经理，从而绕过原始安全经理强制执行的检查。</td>
 * </tr>
 *
 * <tr>
 *   <td>createSecurityManager</td>
 *   <td>创建新的安全经理</td>
 *   <td>这使代码能够访问受保护的敏感方法，这些方法可能泄露有关其他类或执行堆栈的信息。</td>
 * </tr>
 *
 * <tr>
 *   <td>getenv.{variable name}</td>
 *   <td>读取指定环境变量的值</td>
 *   <td>这将允许代码读取特定环境变量的值或确定其是否存在。如果变量包含机密数据，这是危险的。</td>
 * </tr>
 *
 * <tr>
 *   <td>exitVM.{exit status}</td>
 *   <td>以指定的退出状态终止 Java 虚拟机</td>
 *   <td>这允许攻击者通过自动强制虚拟机终止来进行拒绝服务攻击。注意：“exitVM.*”权限自动授予从应用程序类路径加载的所有代码，从而允许应用程序终止自己。此外，“exitVM”权限等同于“exitVM.*”。</td>
 * </tr>
 *
 * <tr>
 *   <td>shutdownHooks</td>
 *   <td>注册和取消虚拟机关闭挂钩</td>
 *   <td>这允许攻击者注册恶意关闭挂钩，干扰虚拟机的干净关闭。</td>
 * </tr>
 *
 * <tr>
 *   <td>setFactory</td>
 *   <td>设置 ServerSocket 或 Socket 使用的套接字工厂，或 URL 使用的流处理器工厂</td>
 *   <td>这允许代码设置套接字、服务器套接字、流处理器或 RMI 套接字工厂的实际实现。攻击者可能设置一个有缺陷的实现，破坏数据流。</td>
 * </tr>
 *
 * <tr>
 *   <td>setIO</td>
 *   <td>设置 System.out、System.in 和 System.err</td>
 *   <td>这允许更改标准系统流的值。攻击者可能更改 System.in 以监控和窃取用户输入，或设置 System.err 为“null” OutputStream，这将隐藏发送到 System.err 的任何错误消息。</td>
 * </tr>
 *
 * <tr>
 *   <td>modifyThread</td>
 *   <td>修改线程，例如通过调用 Thread 的 <tt>interrupt</tt>、<tt>stop</tt>、<tt>suspend</tt>、<tt>resume</tt>、<tt>setDaemon</tt>、<tt>setPriority</tt>、<tt>setName</tt> 和 <tt>setUncaughtExceptionHandler</tt> 方法</td>
 *   <td>这允许攻击者修改系统中任何线程的行为。</td>
 * </tr>
 *
 * <tr>
 *   <td>stopThread</td>
 *   <td>通过调用 Thread 的 <code>stop</code> 方法停止线程</td>
 *   <td>这允许代码在已经授予访问该线程的权限的情况下停止系统中的任何线程。这构成威胁，因为代码可能通过杀死现有线程来破坏系统。</td>
 * </tr>
 *
 * <tr>
 *   <td>modifyThreadGroup</td>
 *   <td>修改线程组，例如通过调用 ThreadGroup 的 <code>destroy</code>、<code>getParent</code>、<code>resume</code>、<code>setDaemon</code>、<code>setMaxPriority</code>、<code>stop</code> 和 <code>suspend</code> 方法</td>
 *   <td>这允许攻击者创建线程组并设置其运行优先级。</td>
 * </tr>
 *
 * <tr>
 *   <td>getProtectionDomain</td>
 *   <td>检索类的 ProtectionDomain</td>
 *   <td>这允许代码获取特定代码源的策略信息。虽然获取策略信息不会危及系统的安全性，但它确实为攻击者提供了额外的信息，例如本地文件名，以便更好地瞄准攻击。</td>
 * </tr>
 *
 * <tr>
 *   <td>getFileSystemAttributes</td>
 *   <td>检索文件系统属性</td>
 *   <td>这允许代码获取文件系统信息，如磁盘使用情况或调用者可用的磁盘空间。这可能是危险的，因为它泄露了有关系统硬件配置和调用者写文件权限的一些信息。</td>
 * </tr>
 *
 * <tr>
 *   <td>readFileDescriptor</td>
 *   <td>读取文件描述符</td>
 *   <td>这将允许代码读取与文件描述符关联的特定文件。如果文件包含机密数据，这是危险的。</td>
 * </tr>
 *
 * <tr>
 *   <td>writeFileDescriptor</td>
 *   <td>写入文件描述符</td>
 *   <td>这允许代码写入与描述符关联的特定文件。这是危险的，因为它可能允许恶意代码植入病毒，或者至少填满整个磁盘。</td>
 * </tr>
 *
 * <tr>
 *   <td>loadLibrary.{library name}</td>
 *   <td>动态链接指定的库</td>
 *   <td>允许小程序权限加载本地代码库是危险的，因为 Java 安全架构没有设计来防止和防止本地代码级别的恶意行为。</td>
 * </tr>
 *
 * <tr>
 *   <td>accessClassInPackage.{package name}</td>
 *   <td>通过类加载器的 <code>loadClass</code> 方法访问指定包，当该类加载器调用 SecurityManager 的 <code>checkPackageAccess</code> 方法时</td>
 *   <td>这使代码能够访问通常无法访问的包中的类。恶意代码可能使用这些类来帮助其尝试破坏系统中的安全性。</td>
 * </tr>
 *
 * <tr>
 *   <td>defineClassInPackage.{package name}</td>
 *   <td>通过类加载器的 <code>defineClass</code> 方法在指定包中定义类，当该类加载器调用 SecurityManager 的 <code>checkPackageDefinition</code> 方法时</td>
 *   <td>这授予代码在特定包中定义类的权限。这是危险的，因为具有此权限的恶意代码可能在受信任的包（如 <code>java.security</code> 或 <code>java.lang</code>）中定义恶意类。</td>
 * </tr>
 *
 * <tr>
 *   <td>accessDeclaredMembers</td>
 *   <td>访问类的声明成员</td>
 *   <td>这授予代码查询类的公共、受保护、默认（包）访问和私有字段和/或方法的权限。虽然代码将能够访问私有和受保护的字段和方法名称，但无法访问私有/受保护的字段数据，也无法调用任何私有方法。尽管如此，恶意代码可能使用此信息更好地瞄准攻击。此外，它可能调用类中的任何公共方法和/或访问公共字段。这可能是危险的，如果代码通常无法调用这些方法和/或访问这些字段，因为它无法将对象转换为具有这些方法和字段的类/接口。</td>
 * </tr>
 * <tr>
 *   <td>queuePrintJob</td>
 *   <td>启动打印作业请求</td>
 *   <td>这可能将敏感信息打印到打印机，或简单地浪费纸张。</td>
 * </tr>
 *
 * <tr>
 *   <td>getStackTrace</td>
 *   <td>检索另一个线程的堆栈跟踪信息。</td>
 *   <td>这允许检索另一个线程的堆栈跟踪信息。这可能允许恶意代码监控线程的执行并发现应用程序中的漏洞。</td>
 * </tr>
 *
 * <tr>
 *   <td>setDefaultUncaughtExceptionHandler</td>
 *   <td>设置当线程因未捕获的异常而突然终止时使用的默认处理器</td>
 *   <td>这允许攻击者注册恶意的未捕获异常处理器，干扰线程的终止。</td>
 * </tr>
 *
 * <tr>
 *   <td>preferences</td>
 *   <td>表示获取 java.util.prefs.Preferences 实现的用户或系统根所需的权限，这反过来允许在 Preferences 持久存储中进行检索或更新操作。</td>
 *   <td>此权限允许用户在运行代码的用户具有读/写该持久存储的足够操作系统权限的情况下，从或向偏好设置持久存储读取或写入。实际的持久存储可能位于传统的文件系统目录中或位于注册表中，具体取决于平台操作系统。</td>
 * </tr>
 *
 * <tr>
 *   <td>usePolicy</td>
 *   <td>授予此权限将禁用 Java 插件的默认安全提示行为。</td>
 *   <td>有关更多信息，请参阅 Java 插件的指南，<a href=
 *   "../../../technotes/guides/plugin/developer_guide/security.html">
 *   Applet Security Basics</a> 和 <a href=
 *   "../../../technotes/guides/plugin/developer_guide/rsa_how.html#use">
 *   usePolicy Permission</a>。</td>
 * </tr>
 * </table>
 *
 * @see java.security.BasicPermission
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.PermissionCollection
 * @see java.lang.SecurityManager
 *
 *
 * @author Marianne Mueller
 * @author Roland Schemers
 */

public final class RuntimePermission extends BasicPermission {

    private static final long serialVersionUID = 7399184964622342223L;

    /**
     * 创建一个新的 RuntimePermission，指定名称。名称是 RuntimePermission 的符号名称，例如
     * "exit"、"setFactory" 等。名称末尾可以出现一个星号，跟随一个“.”，或单独出现，以表示通配符匹配。
     *
     * @param name RuntimePermission 的名称。
     *
     * @throws NullPointerException 如果 <code>name</code> 为 <code>null</code>。
     * @throws IllegalArgumentException 如果 <code>name</code> 为空。
     */

    public RuntimePermission(String name)
    {
        super(name);
    }

    /**
     * 创建一个新的 RuntimePermission 对象，指定名称。名称是 RuntimePermission 的符号名称，而
     * actions 字符串目前未使用，应为 null。
     *
     * @param name RuntimePermission 的名称。
     * @param actions 应为 null。
     *
     * @throws NullPointerException 如果 <code>name</code> 为 <code>null</code>。
     * @throws IllegalArgumentException 如果 <code>name</code> 为空。
     */

    public RuntimePermission(String name, String actions)
    {
        super(name, actions);
    }
}
