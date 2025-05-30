
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

package java.rmi.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * <code>RMIClassLoader</code> 包含支持 RMI 动态类加载的静态方法。包括从网络位置（一个或多个 URL）加载类的方法，以及获取远程方应从哪个位置加载现有类的方法。这些方法在 RMI 运行时进行远程方法调用时的参数和返回值的序列化和反序列化时使用，也可以由应用程序直接调用以模拟 RMI 的动态类加载行为。
 *
 * <p>以下静态方法的实现
 *
 * <ul>
 *
 * <li>{@link #loadClass(URL,String)}
 * <li>{@link #loadClass(String,String)}
 * <li>{@link #loadClass(String,String,ClassLoader)}
 * <li>{@link #loadProxyClass(String,String[],ClassLoader)}
 * <li>{@link #getClassLoader(String)}
 * <li>{@link #getClassAnnotation(Class)}
 *
 * </ul>
 *
 * 由 {@link RMIClassLoaderSpi} 的实例提供，这是这些方法的服务提供者接口。当调用这些方法之一时，其行为是委托给服务提供者实例上的相应方法。每个方法如何委托给提供者实例的详细信息在每个特定方法的文档中描述。
 *
 * <p>服务提供者实例的选择如下：
 *
 * <ul>
 *
 * <li>如果系统属性 <code>java.rmi.server.RMIClassLoaderSpi</code> 已定义，那么如果其值等于字符串 <code>"default"</code>，提供者实例将是 {@link #getDefaultProviderInstance()} 方法调用返回的值，对于任何其他值，如果可以通过系统类加载器（参见 {@link ClassLoader#getSystemClassLoader}）加载名为属性值的类，并且该类可以分配给 {@link RMIClassLoaderSpi} 并具有公共无参数构造函数，则将调用该构造函数来创建提供者实例。如果属性已定义但上述任何条件不满足，则将向尝试使用 <code>RMIClassLoader</code> 的代码抛出一个未指定的 <code>Error</code>，指示获取提供者实例失败。
 *
 * <li>如果系统类加载器可见的资源名为 <code>META-INF/services/java.rmi.server.RMIClassLoaderSpi</code>，则该资源的内容被解释为提供者配置文件，文件中指定的第一个类名用作提供者类名。如果可以通过系统类加载器加载该类名的类，并且该类可以分配给 {@link RMIClassLoaderSpi} 并具有公共无参数构造函数，则将调用该构造函数来创建提供者实例。如果找到资源但无法如上所述实例化提供者，则将向尝试使用 <code>RMIClassLoader</code> 的代码抛出一个未指定的 <code>Error</code>，指示获取提供者实例失败。
 *
 * <li>否则，提供者实例将是 {@link #getDefaultProviderInstance()} 方法调用返回的值。
 *
 * </ul>
 *
 * @author      Ann Wollrath
 * @author      Peter Jones
 * @author      Laird Dornin
 * @see         RMIClassLoaderSpi
 * @since       JDK1.1
 */
public class RMIClassLoader {

    /** "default" 提供者实例 */
    private static final RMIClassLoaderSpi defaultProvider =
        newDefaultProviderInstance();

    /** 提供者实例 */
    private static final RMIClassLoaderSpi provider =
        AccessController.doPrivileged(
            new PrivilegedAction<RMIClassLoaderSpi>() {
                public RMIClassLoaderSpi run() { return initializeProvider(); }
            });

    /*
     * 禁止任何人创建此类的实例。
     */
    private RMIClassLoader() {}

    /**
     * 加载指定 <code>name</code> 的类。
     *
     * <p>此方法委托给 {@link #loadClass(String,String)}，传递 <code>null</code> 作为第一个参数，<code>name</code> 作为第二个参数。
     *
     * @param   name 要加载的类的名称
     *
     * @return  表示已加载类的 <code>Class</code> 对象
     *
     * @throws MalformedURLException 如果用于加载类的提供者特定 URL 无效
     *
     * @throws  ClassNotFoundException 如果在代码库位置找不到类的定义
     *
     * @deprecated 被 <code>loadClass(String,String)</code> 方法取代
     * @see #loadClass(String,String)
     */
    @Deprecated
    public static Class<?> loadClass(String name)
        throws MalformedURLException, ClassNotFoundException
    {
        return loadClass((String) null, name);
    }

    /**
     * 从代码库 URL 加载类。
     *
     * 如果 <code>codebase</code> 为 <code>null</code>，则此方法的行为与 {@link #loadClass(String,String)} 以 <code>null</code> 作为 <code>codebase</code> 和给定类名相同。
     *
     * <p>此方法委托给提供者实例的 {@link RMIClassLoaderSpi#loadClass(String,String,ClassLoader)} 方法，传递调用 {@link URL#toString} 方法的结果（如果 <code>codebase</code> 为 null，则传递 <code>null</code>）作为第一个参数，<code>name</code> 作为第二个参数，<code>null</code> 作为第三个参数。
     *
     * @param   codebase 要从其加载类的 URL，或 <code>null</code>
     *
     * @param   name 要加载的类的名称
     *
     * @return  表示已加载类的 <code>Class</code> 对象
     *
     * @throws MalformedURLException 如果 <code>codebase</code> 为 <code>null</code> 且用于加载类的提供者特定 URL 无效
     *
     * @throws  ClassNotFoundException 如果在指定 URL 找不到类的定义
     */
    public static Class<?> loadClass(URL codebase, String name)
        throws MalformedURLException, ClassNotFoundException
    {
        return provider.loadClass(
            codebase != null ? codebase.toString() : null, name, null);
    }

    /**
     * 从代码库 URL 路径加载类。
     *
     * <p>此方法委托给提供者实例的 {@link RMIClassLoaderSpi#loadClass(String,String,ClassLoader)} 方法，传递 <code>codebase</code> 作为第一个参数，<code>name</code> 作为第二个参数，<code>null</code> 作为第三个参数。
     *
     * @param   codebase 要从其加载类的 URL 列表（以空格分隔），或 <code>null</code>
     *
     * @param   name 要加载的类的名称
     *
     * @return  表示已加载类的 <code>Class</code> 对象
     *
     * @throws MalformedURLException 如果 <code>codebase</code> 不为 <code>null</code> 且包含无效 URL，或者 <code>codebase</code> 为 <code>null</code> 且用于加载类的提供者特定 URL 无效
     *
     * @throws  ClassNotFoundException 如果在指定位置找不到类的定义
     *
     * @since   1.2
     */
    public static Class<?> loadClass(String codebase, String name)
        throws MalformedURLException, ClassNotFoundException
    {
        return provider.loadClass(codebase, name, null);
    }

    /**
     * 从代码库 URL 路径加载类，可选地使用提供的加载器。
     *
     * 当调用者希望向提供者实现提供一个额外的上下文类加载器以考虑时，应使用此方法，例如堆栈上的调用者的加载器。通常，提供者实现将尝试使用给定的 <code>defaultLoader</code>（如果已指定）解析命名类，然后再尝试从代码库 URL 路径解析类。
     *
     * <p>此方法委托给提供者实例的 {@link RMIClassLoaderSpi#loadClass(String,String,ClassLoader)} 方法，传递 <code>codebase</code> 作为第一个参数，<code>name</code> 作为第二个参数，<code>defaultLoader</code> 作为第三个参数。
     *
     * @param   codebase 要从其加载类的 URL 列表（以空格分隔），或 <code>null</code>
     *
     * @param   name 要加载的类的名称
     *
     * @param   defaultLoader 要使用的额外上下文类加载器，或 <code>null</code>
     *
     * @return  表示已加载类的 <code>Class</code> 对象
     *
     * @throws MalformedURLException 如果 <code>codebase</code> 不为 <code>null</code> 且包含无效 URL，或者 <code>codebase</code> 为 <code>null</code> 且用于加载类的提供者特定 URL 无效
     *
     * @throws  ClassNotFoundException 如果在指定位置找不到类的定义
     *
     * @since   1.4
     */
    public static Class<?> loadClass(String codebase, String name,
                                     ClassLoader defaultLoader)
        throws MalformedURLException, ClassNotFoundException
    {
        return provider.loadClass(codebase, name, defaultLoader);
    }

    /**
     * 从代码库 URL 路径加载实现给定名称接口集的动态代理类（参见 {@link java.lang.reflect.Proxy}）。
     *
     * <p>接口将类似于通过 {@link #loadClass(String,String)} 方法使用给定的 <code>codebase</code> 加载的类进行解析。
     *
     * <p>此方法委托给提供者实例的 {@link RMIClassLoaderSpi#loadProxyClass(String,String[],ClassLoader)} 方法，传递 <code>codebase</code> 作为第一个参数，<code>interfaces</code> 作为第二个参数，<code>defaultLoader</code> 作为第三个参数。
     *
     * @param   codebase 要从其加载类的 URL 列表（以空格分隔），或 <code>null</code>
     *
     * @param   interfaces 代理类要实现的接口名称
     *
     * @param   defaultLoader 要使用的额外上下文类加载器，或 <code>null</code>
     *
     * @return  实现命名接口的动态代理类
     *
     * @throws  MalformedURLException 如果 <code>codebase</code> 不为 <code>null</code> 且包含无效 URL，或者 <code>codebase</code> 为 <code>null</code> 且用于加载类的提供者特定 URL 无效
     *
     * @throws  ClassNotFoundException 如果在指定位置找不到命名接口的定义，或者创建动态代理类失败（例如，如果 {@link java.lang.reflect.Proxy#getProxyClass(ClassLoader,Class[])} 会因给定的接口列表抛出 <code>IllegalArgumentException</code>）
     *
     * @since   1.4
     */
    public static Class<?> loadProxyClass(String codebase, String[] interfaces,
                                          ClassLoader defaultLoader)
        throws ClassNotFoundException, MalformedURLException
    {
        return provider.loadProxyClass(codebase, interfaces, defaultLoader);
    }

    /**
     * 返回从给定代码库 URL 路径加载类的类加载器。
     *
     * <p>返回的类加载器是 {@link #loadClass(String,String)} 方法用于加载类的类加载器，对于相同的 <code>codebase</code> 参数。
     *
     * <p>此方法委托给提供者实例的 {@link RMIClassLoaderSpi#getClassLoader(String)} 方法，传递 <code>codebase</code> 作为参数。
     *
     * <p>如果有安全经理，其 <code>checkPermission</code> 方法将被调用，传递 <code>RuntimePermission("getClassLoader")</code> 权限；这可能导致 <code>SecurityException</code>。此方法的提供者实现也可能执行进一步的安全检查，以验证调用上下文是否有权限连接到代码库 URL 路径中的所有 URL。
     *
     * @param   codebase 返回的类加载器将从其加载类的 URL 列表（以空格分隔），或 <code>null</code>
     *
     * @return  从给定代码库 URL 路径加载类的类加载器
     *
     * @throws  MalformedURLException 如果 <code>codebase</code> 不为 <code>null</code> 且包含无效 URL，或者 <code>codebase</code> 为 <code>null</code> 且用于标识类加载器的提供者特定 URL 无效
     *
     * @throws  SecurityException 如果有安全经理且其 <code>checkPermission</code> 方法调用失败，或者调用者没有权限连接到代码库 URL 路径中的所有 URL
     *
     * @since   1.3
     */
    public static ClassLoader getClassLoader(String codebase)
        throws MalformedURLException, SecurityException
    {
        return provider.getClassLoader(codebase);
    }

    /**
     * 返回 RMI 在序列化给定类的对象时用于注解类描述符的注解字符串（表示类定义的位置）。
     *
     * <p>此方法委托给提供者实例的 {@link RMIClassLoaderSpi#getClassAnnotation(Class)} 方法，传递 <code>cl</code> 作为参数。
     *
     * @param   cl 要获取注解的类
     *
     * @return  用于注解给定类的字符串，当其被序列化时，或 <code>null</code>
     *
     * @throws  NullPointerException 如果 <code>cl</code> 为 <code>null</code>
     *
     * @since   1.2
     */
    /*
     * REMIND: 我们是否应该说明返回的类注解将或应该是（以空格分隔）的 URL 列表？
     */
    public static String getClassAnnotation(Class<?> cl) {
        return provider.getClassAnnotation(cl);
    }


                /**
     * 返回服务提供者接口 {@link RMIClassLoaderSpi} 的默认提供者的规范实例。
     * 如果系统属性 <code>java.rmi.server.RMIClassLoaderSpi</code>
     * 未定义，则 <code>RMIClassLoader</code> 静态
     * 方法
     *
     * <ul>
     *
     * <li>{@link #loadClass(URL,String)}
     * <li>{@link #loadClass(String,String)}
     * <li>{@link #loadClass(String,String,ClassLoader)}
     * <li>{@link #loadProxyClass(String,String[],ClassLoader)}
     * <li>{@link #getClassLoader(String)}
     * <li>{@link #getClassAnnotation(Class)}
     *
     * </ul>
     *
     * 将使用默认提供者的规范实例作为服务提供者实例。
     *
     * <p>如果有安全经理，其
     * <code>checkPermission</code> 方法将被调用，权限为
     * <code>RuntimePermission("setFactory")</code>；这可能导致
     * <code>SecurityException</code>。
     *
     * <p>默认服务提供者实例实现了
     * {@link RMIClassLoaderSpi} 如下：
     *
     * <blockquote>
     *
     * <p><b>{@link RMIClassLoaderSpi#getClassAnnotation(Class)
     * getClassAnnotation}</b> 方法返回一个 <code>String</code>
     * 表示远程方应使用以下载指定类的定义的代码库 URL 路径。返回的字符串格式为
     * 以空格分隔的 URL 路径。
     *
     * 返回的代码库字符串取决于指定类的定义类加载器：
     *
     * <ul>
     *
     * <li><p>如果类加载器是系统类加载器（参见
     * {@link ClassLoader#getSystemClassLoader}），系统类加载器的父类加载器（如用于安装扩展的加载器），
     * 或引导类加载器（可能表示为 <code>null</code>），则返回
     * <code>java.rmi.server.codebase</code> 属性的值（或可能的早期缓存值），或
     * 如果该属性未设置，则返回 <code>null</code>。
     *
     * <li><p>否则，如果类加载器是
     * <code>URLClassLoader</code> 的实例，则返回的字符串是通过调用加载器的
     * <code>getURLs</code> 方法返回的 URL 的外部形式的空格分隔列表。如果
     * <code>URLClassLoader</code> 是由此提供者创建以服务其
     * <code>loadClass</code> 或 <code>loadProxyClass</code> 方法的调用，则不需要权限即可获取关联的代码库字符串。如果它是任意其他
     * <code>URLClassLoader</code> 实例，则如果有安全经理，其
     * <code>checkPermission</code> 方法将为通过
     * <code>getURLs</code> 方法返回的每个 URL 调用一次，权限由调用
     * <code>openConnection().getPermission()</code> 于每个 URL 返回；如果这些调用中的任何一个抛出
     * <code>SecurityException</code> 或 <code>IOException</code>，则返回
     * <code>java.rmi.server.codebase</code> 属性的值（或可能的早期缓存值），或
     * 如果该属性未设置，则返回 <code>null</code>。
     *
     * <li><p>最后，如果类加载器不是
     * <code>URLClassLoader</code> 的实例，则返回
     * <code>java.rmi.server.codebase</code> 属性的值（或可能的早期缓存值），或
     * 如果该属性未设置，则返回 <code>null</code>。
     *
     * </ul>
     *
     * <p>对于下面描述的方法的实现，
     * 它们都接受一个名为 <code>codebase</code> 的 <code>String</code> 参数，该参数是一个以空格分隔的 URL 列表，
     * 每个调用都有一个与 <code>codebase</code> 参数结合当前线程的上下文类加载器（参见
     * {@link Thread#getContextClassLoader()}) 确定的 <i>代码库加载器</i>。当存在安全经理时，此提供者维护一个类加载器实例的内部表（至少是 {@link
     * java.net.URLClassLoader} 的实例），这些实例以其父类加载器和代码库 URL 路径（URL 的有序列表）为键。如果
     * <code>codebase</code> 参数为 <code>null</code>，则代码库 URL 路径是系统属性
     * <code>java.rmi.server.codebase</code> 的值或可能的早期缓存值。对于给定的代码库 URL 路径，作为
     * <code>codebase</code> 参数传递给给定上下文中一个以下方法的调用，代码库加载器是表中具有指定代码库 URL 路径和当前线程的上下文类加载器作为其父类的加载器。如果不存在这样的加载器，则创建一个并添加到表中。
     * 表不会对其包含的加载器保持强引用，以允许它们及其定义的类在未被其他方式引用时被垃圾回收。为了防止任意不受信任的代码被隐式加载到没有安全经理的虚拟机中，如果未设置安全经理，代码库加载器就是当前线程的上下文类加载器（提供的代码库 URL 路径被忽略，因此远程类加载被禁用）。
     *
     * <p><b>{@link RMIClassLoaderSpi#getClassLoader(String)
     * getClassLoader}</b> 方法返回指定代码库 URL 路径的代码库加载器。如果存在安全经理，
     * 则如果调用上下文没有权限连接到代码库 URL 路径中的所有 URL，将抛出
     * <code>SecurityException</code>。
     *
     * <p><b>{@link
     * RMIClassLoaderSpi#loadClass(String,String,ClassLoader)
     * loadClass}</b> 方法尝试以如下方式加载具有指定名称的类：
     *
     * <blockquote>
     *
     * 如果 <code>defaultLoader</code> 参数不是
     * <code>null</code>，则首先尝试使用
     * <code>defaultLoader</code> 加载具有指定 <code>name</code> 的类，例如通过评估
     *
     * <pre>
     *     Class.forName(name, false, defaultLoader)
     * </pre>
     *
     * 如果类成功从 <code>defaultLoader</code> 加载，则返回该类。如果抛出的异常不是
     * <code>ClassNotFoundException</code>，则将该异常抛给调用者。
     *
     * <p>接下来，<code>loadClass</code> 方法尝试使用指定代码库 URL 路径的代码库加载器加载具有指定 <code>name</code> 的类。
     * 如果存在安全经理，则调用上下文必须有权限连接到代码库 URL 路径中的所有 URL；否则，将使用当前线程的上下文类加载器而不是代码库加载器。
     *
     * </blockquote>
     *
     * <p><b>{@link
     * RMIClassLoaderSpi#loadProxyClass(String,String[],ClassLoader)
     * loadProxyClass}</b> 方法尝试以如下方式返回一个具有命名接口的动态代理类：
     *
     * <blockquote>
     *
     * <p>如果 <code>defaultLoader</code> 参数不是
     * <code>null</code> 且所有命名接口都可以通过该加载器解析，则
     *
     * <ul>
     *
     * <li>如果所有解析的接口都是 <code>public</code>，则首先尝试使用
     * {@link
     * java.lang.reflect.Proxy#getProxyClass(ClassLoader,Class[])
     * Proxy.getProxyClass} 为代码库加载器中定义的解析接口获取动态代理类；如果该尝试抛出
     * <code>IllegalArgumentException</code>，则尝试为 <code>defaultLoader</code> 中定义的解析接口获取动态代理类。如果两次尝试都抛出
     * <code>IllegalArgumentException</code>，则此方法抛出 <code>ClassNotFoundException</code>。如果抛出其他任何异常，则将该异常抛给调用者。
     *
     * <li>如果所有非 <code>public</code> 的解析接口都在同一个类加载器中定义，则尝试为该加载器中定义的解析接口获取动态代理类。
     *
     * <li>否则，抛出 <code>LinkageError</code>（因为无法在任何加载器中定义实现所有指定接口的类）。
     *
     * </ul>
     *
     * <p>否则，如果所有命名接口都可以通过代码库加载器解析，则
     *
     * <ul>
     *
     * <li>如果所有解析的接口都是 <code>public</code>，则尝试在代码库加载器中为解析的接口获取动态代理类。如果尝试抛出
     * <code>IllegalArgumentException</code>，则此方法抛出 <code>ClassNotFoundException</code>。
     *
     * <li>如果所有非 <code>public</code> 的解析接口都在同一个类加载器中定义，则尝试为该加载器中定义的解析接口获取动态代理类。
     *
     * <li>否则，抛出 <code>LinkageError</code>（因为无法在任何加载器中定义实现所有指定接口的类）。
     *
     * </ul>
     *
     * <p>否则，对于无法解析的命名接口之一，抛出 <code>ClassNotFoundException</code>。
     *
     * </blockquote>
     *
     * </blockquote>
     *
     * @return  默认服务提供者的规范实例
     *
     * @throws  SecurityException 如果存在安全经理且其
     * <code>checkPermission</code> 方法调用失败
     *
     * @since   1.4
     */
    public static RMIClassLoaderSpi getDefaultProviderInstance() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("setFactory"));
        }
        return defaultProvider;
    }

    /**
     * 返回给定类加载器的安全上下文。
     *
     * @param   loader 从中获取安全上下文的类加载器
     *
     * @return  安全上下文
     *
     * @deprecated 无替代。自 Java 2 平台 v1.2 起，RMI 不再使用此方法来获取类加载器的安全上下文。
     * @see java.lang.SecurityManager#getSecurityContext()
     */
    @Deprecated
    public static Object getSecurityContext(ClassLoader loader)
    {
        return sun.rmi.server.LoaderHandler.getSecurityContext(loader);
    }

    /**
     * 创建默认提供者类的实例。
     */
    private static RMIClassLoaderSpi newDefaultProviderInstance() {
        return new RMIClassLoaderSpi() {
            public Class<?> loadClass(String codebase, String name,
                                      ClassLoader defaultLoader)
                throws MalformedURLException, ClassNotFoundException
            {
                return sun.rmi.server.LoaderHandler.loadClass(
                    codebase, name, defaultLoader);
            }

            public Class<?> loadProxyClass(String codebase,
                                           String[] interfaces,
                                           ClassLoader defaultLoader)
                throws MalformedURLException, ClassNotFoundException
            {
                return sun.rmi.server.LoaderHandler.loadProxyClass(
                    codebase, interfaces, defaultLoader);
            }

            public ClassLoader getClassLoader(String codebase)
                throws MalformedURLException
            {
                return sun.rmi.server.LoaderHandler.getClassLoader(codebase);
            }

            public String getClassAnnotation(Class<?> cl) {
                return sun.rmi.server.LoaderHandler.getClassAnnotation(cl);
            }
        };
    }

    /**
     * 根据上述文档选择提供者实例。
     *
     * 假设此方法在特权块中被调用。
     */
    private static RMIClassLoaderSpi initializeProvider() {
        /*
         * 首先检查系统属性是否已设置：
         */
        String providerClassName =
            System.getProperty("java.rmi.server.RMIClassLoaderSpi");

        if (providerClassName != null) {
            if (providerClassName.equals("default")) {
                return defaultProvider;
            }

            try {
                Class<? extends RMIClassLoaderSpi> providerClass =
                    Class.forName(providerClassName, false,
                                  ClassLoader.getSystemClassLoader())
                    .asSubclass(RMIClassLoaderSpi.class);
                return providerClass.newInstance();

            } catch (ClassNotFoundException e) {
                throw new NoClassDefFoundError(e.getMessage());
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (InstantiationException e) {
                throw new InstantiationError(e.getMessage());
            } catch (ClassCastException e) {
                Error error = new LinkageError(
                    "提供者类不能赋值给 RMIClassLoaderSpi");
                error.initCause(e);
                throw error;
            }
        }

        /*
         * 接下来查找已安装的提供者配置文件：
         */
        Iterator<RMIClassLoaderSpi> iter =
            ServiceLoader.load(RMIClassLoaderSpi.class,
                               ClassLoader.getSystemClassLoader()).iterator();
        if (iter.hasNext()) {
            try {
                return iter.next();
            } catch (ClassCastException e) {
                Error error = new LinkageError(
                    "提供者类不能赋值给 RMIClassLoaderSpi");
                error.initCause(e);
                throw error;
            }
        }

        /*
         * 最后，返回默认提供者的规范实例。
         */
        return defaultProvider;
    }
}
