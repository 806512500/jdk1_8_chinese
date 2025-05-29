
/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.rmi.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * <code>RMIClassLoader</code> 包含支持 RMI 动态类加载的静态方法。其中包括从网络位置（一个或多个 URL）加载类的方法，以及获取现有类应由远程方加载的位置的方法。这些方法在 RMI 运行时对远程方法调用的参数和返回值中包含的类进行序列化和反序列化时使用，也可以由应用程序直接调用以模拟 RMI 的动态类加载行为。
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
 * 是由 {@link RMIClassLoaderSpi} 的一个实例提供的，这是这些方法的服务提供者接口。当调用这些方法之一时，其行为是委托给服务提供者实例上的相应方法。每个方法如何委托给提供者实例的详细信息在每个特定方法的文档中描述。
 *
 * <p>服务提供者实例的选择如下：
 *
 * <ul>
 *
 * <li>如果定义了系统属性 <code>java.rmi.server.RMIClassLoaderSpi</code>，那么如果其值等于字符串 <code>"default"</code>，提供者实例将是 {@link #getDefaultProviderInstance()} 方法调用返回的值，对于任何其他值，如果可以通过系统类加载器（参见 {@link ClassLoader#getSystemClassLoader}）加载名为该属性值的类，并且该类可以分配给 {@link RMIClassLoaderSpi} 并具有公共无参数构造函数，则将调用该构造函数以创建提供者实例。如果定义了该属性但上述任何条件不成立，则将向尝试使用 <code>RMIClassLoader</code> 的代码抛出未指定的 <code>Error</code>，表示无法获取提供者实例。
 *
 * <li>如果系统类加载器可以访问名为 <code>META-INF/services/java.rmi.server.RMIClassLoaderSpi</code> 的资源，则该资源的内容将被解释为提供者配置文件，文件中指定的第一个类名将用作提供者类名。如果可以通过系统类加载器加载具有该名称的类，并且该类可以分配给 {@link RMIClassLoaderSpi} 并具有公共无参数构造函数，则将调用该构造函数以创建提供者实例。如果找到了资源但无法如上所述实例化提供者，则将向尝试使用 <code>RMIClassLoader</code> 的代码抛出未指定的 <code>Error</code>，表示无法获取提供者实例。
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

    /** "default" provider instance */
    private static final RMIClassLoaderSpi defaultProvider =
        newDefaultProviderInstance();

    /** provider instance */
    private static final RMIClassLoaderSpi provider =
        AccessController.doPrivileged(
            new PrivilegedAction<RMIClassLoaderSpi>() {
                public RMIClassLoaderSpi run() { return initializeProvider(); }
            });

    /*
     * Disallow anyone from creating one of these.
     */
    private RMIClassLoader() {}

    /**
     * 加载指定 <code>name</code> 的类。
     *
     * <p>此方法委托给 {@link #loadClass(String,String)}，传递 <code>null</code> 作为第一个参数，<code>name</code> 作为第二个参数。
     *
     * @param   name 要加载的类的名称
     *
     * @return  表示加载的类的 <code>Class</code> 对象
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
     * 如果 <code>codebase</code> 为 <code>null</code>，则此方法的行为将与 {@link #loadClass(String,String)} 以 <code>null</code> 作为 <code>codebase</code> 和给定类名相同。
     *
     * <p>此方法委托给提供者实例的 {@link RMIClassLoaderSpi#loadClass(String,String,ClassLoader)} 方法，传递调用 {@link URL#toString} 于给定 URL 的结果（如果 <code>codebase</code> 为 null，则传递 <code>null</code>）作为第一个参数，<code>name</code> 作为第二个参数，<code>null</code> 作为第三个参数。
     *
     * @param   codebase 要从其加载类的 URL，或 <code>null</code>
     *
     * @param   name 要加载的类的名称
     *
     * @return  表示加载的类的 <code>Class</code> 对象
     *
     * @throws MalformedURLException 如果 <code>codebase</code> 为 <code>null</code> 且用于加载类的提供者特定 URL 无效
     *
     * @throws  ClassNotFoundException 如果在指定的 URL 找不到类的定义
     */
    public static Class<?> loadClass(URL codebase, String name)
        throws MalformedURLException, ClassNotFoundException
    {
        return provider.loadClass(
            codebase != null ? codebase.toString() : null, name, null);
    }

                /**
     * 从代码库 URL 路径加载一个类。
     *
     * <p>此方法将调用提供者实例的
     * {@link RMIClassLoaderSpi#loadClass(String,String,ClassLoader)}
     * 方法，将 <code>codebase</code> 作为第一个参数，<code>name</code> 作为第二个参数，
     * 并将 <code>null</code> 作为第三个参数。
     *
     * @param   codebase 要从中加载类的 URL 列表（以空格分隔），或 <code>null</code>
     *
     * @param   name 要加载的类的名称
     *
     * @return  代表加载的类的 <code>Class</code> 对象
     *
     * @throws MalformedURLException 如果 <code>codebase</code> 不为
     * <code>null</code> 且包含无效的 URL，或者如果 <code>codebase</code> 为 <code>null</code>
     * 且用于加载类的提供者特定 URL 无效
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
     * 从代码库 URL 路径加载一个类，可选地使用提供的加载器。
     *
     * 当调用者希望向提供者实现提供一个额外的上下文类加载器以考虑时，应使用此方法，例如调用者堆栈上的加载器。
     * 通常，提供者实现将尝试使用给定的 <code>defaultLoader</code>（如果指定了）解析命名类，
     * 然后尝试从代码库 URL 路径解析类。
     *
     * <p>此方法将调用提供者实例的
     * {@link RMIClassLoaderSpi#loadClass(String,String,ClassLoader)}
     * 方法，将 <code>codebase</code> 作为第一个参数，<code>name</code> 作为第二个参数，
     * 并将 <code>defaultLoader</code> 作为第三个参数。
     *
     * @param   codebase 要从中加载类的 URL 列表（以空格分隔），或 <code>null</code>
     *
     * @param   name 要加载的类的名称
     *
     * @param   defaultLoader 要使用的额外上下文类加载器，或 <code>null</code>
     *
     * @return  代表加载的类的 <code>Class</code> 对象
     *
     * @throws MalformedURLException 如果 <code>codebase</code> 不为
     * <code>null</code> 且包含无效的 URL，或者如果 <code>codebase</code> 为 <code>null</code>
     * 且用于加载类的提供者特定 URL 无效
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
     * 从代码库 URL 路径加载一个实现给定名称接口集的动态代理类（参见 {@link java.lang.reflect.Proxy}）。
     *
     * <p>接口将类似于通过 {@link #loadClass(String,String)} 方法加载的类进行解析，使用给定的
     * <code>codebase</code>。
     *
     * <p>此方法将调用提供者实例的
     * {@link RMIClassLoaderSpi#loadProxyClass(String,String[],ClassLoader)}
     * 方法，将 <code>codebase</code> 作为第一个参数，<code>interfaces</code> 作为第二个参数，
     * 并将 <code>defaultLoader</code> 作为第三个参数。
     *
     * @param   codebase 要从中加载类的 URL 列表（以空格分隔），或 <code>null</code>
     *
     * @param   interfaces 代理类要实现的接口名称
     *
     * @param   defaultLoader 要使用的额外上下文类加载器，或 <code>null</code>
     *
     * @return  实现命名接口的动态代理类
     *
     * @throws  MalformedURLException 如果 <code>codebase</code> 不为
     * <code>null</code> 且包含无效的 URL，或者如果 <code>codebase</code> 为 <code>null</code>
     * 且用于加载类的提供者特定 URL 无效
     *
     * @throws  ClassNotFoundException 如果在指定位置找不到命名接口的定义之一，
     * 或者如果创建动态代理类失败（例如，如果
     * {@link java.lang.reflect.Proxy#getProxyClass(ClassLoader,Class[])}
     * 会因为给定的接口列表抛出 <code>IllegalArgumentException</code>）
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
     * 返回一个从给定代码库 URL 路径加载类的类加载器。
     *
     * <p>返回的类加载器是 {@link #loadClass(String,String)} 方法将用于加载类的类加载器，
     * 对于相同的 <code>codebase</code> 参数。
     *
     * <p>此方法将调用提供者实例的
     * {@link RMIClassLoaderSpi#getClassLoader(String)} 方法，
     * 将 <code>codebase</code> 作为参数。
     *
     * <p>如果有安全经理，其 <code>checkPermission</code>
     * 方法将被调用，权限为 <code>RuntimePermission("getClassLoader")</code>；
     * 这可能导致 <code>SecurityException</code>。
     * 此方法的提供者实现也可能执行进一步的安全检查，以验证调用上下文是否有权限连接到代码库 URL 路径中的所有 URL。
     *
     * @param   codebase 返回的类加载器将从中加载类的 URL 列表（以空格分隔），或 <code>null</code>
     *
     * @return  从给定代码库 URL 路径加载类的类加载器
     *
     * @throws  MalformedURLException 如果 <code>codebase</code> 不为
     * <code>null</code> 且包含无效的 URL，或者如果 <code>codebase</code> 为 <code>null</code>
     * 且用于标识类加载器的提供者特定 URL 无效
     *
     * @throws  SecurityException 如果存在安全经理且其 <code>checkPermission</code> 方法调用失败，
     * 或者调用者没有权限连接到代码库 URL 路径中的所有 URL
     *
     * @since   1.3
     */
    public static ClassLoader getClassLoader(String codebase)
        throws MalformedURLException, SecurityException
    {
        return provider.getClassLoader(codebase);
    }


                /**
     * 返回 RMI 在序列化给定类的对象时将用于注释类描述符的注释字符串（表示类定义的位置）。
     *
     * <p>此方法委托给提供程序实例的
     * {@link RMIClassLoaderSpi#getClassAnnotation(Class)} 方法，将 <code>cl</code> 作为参数传递。
     *
     * @param   cl 要获取注释的类
     *
     * @return  用于注释给定类的字符串，当该类被序列化时，或者返回 <code>null</code>
     *
     * @throws  NullPointerException 如果 <code>cl</code> 为 <code>null</code>
     *
     * @since   1.2
     */
    /*
     * 提醒：我们是否应该说明返回的类注释将或应是一个（以空格分隔的）URL列表？
     */
    public static String getClassAnnotation(Class<?> cl) {
        return provider.getClassAnnotation(cl);
    }

    /**
     * 返回服务提供接口 {@link RMIClassLoaderSpi} 的默认提供程序的规范实例。
     * 如果系统属性 <code>java.rmi.server.RMIClassLoaderSpi</code> 未定义，则
     * <code>RMIClassLoader</code> 静态方法
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
     * 将使用默认提供程序的规范实例作为服务提供程序实例。
     *
     * <p>如果有安全管理者，其 <code>checkPermission</code> 方法将被调用，传递一个
     * <code>RuntimePermission("setFactory")</code> 权限；这可能导致 <code>SecurityException</code>。
     *
     * <p>默认服务提供程序实例实现 {@link RMIClassLoaderSpi} 如下：
     *
     * <blockquote>
     *
     * <p><b>{@link RMIClassLoaderSpi#getClassAnnotation(Class)
     * getClassAnnotation}</b> 方法返回一个 <code>String</code>，表示远程方应使用来下载指定类定义的代码库 URL 路径。返回的字符串格式是用空格分隔的 URL 路径。
     *
     * 返回的代码库字符串取决于指定类的定义类加载器：
     *
     * <ul>
     *
     * <li><p>如果类加载器是系统类加载器（参见
     * {@link ClassLoader#getSystemClassLoader}），系统类加载器的父加载器，如用于安装扩展的加载器，或引导类加载器（可能表示为 <code>null</code>），则返回
     * <code>java.rmi.server.codebase</code> 属性的值（或可能是较早的缓存值），如果该属性未设置，则返回 <code>null</code>。
     *
     * <li><p>否则，如果类加载器是 <code>URLClassLoader</code> 的实例，则返回的字符串是调用加载器的 <code>getURLs</code> 方法返回的 URL 的外部形式的空格分隔列表。如果
     * <code>URLClassLoader</code> 是由此提供程序创建以服务其 <code>loadClass</code> 或
     * <code>loadProxyClass</code> 方法的调用，则不需要任何权限即可获取关联的代码库字符串。如果是任意其他 <code>URLClassLoader</code> 实例，则如果有安全管理者，其
     * <code>checkPermission</code> 方法将为 <code>getURLs</code> 方法返回的每个 URL 调用一次，权限由调用
     * <code>openConnection().getPermission()</code> 于每个 URL 上返回；如果这些调用中的任何一个抛出
     * <code>SecurityException</code> 或 <code>IOException</code>，则返回
     * <code>java.rmi.server.codebase</code> 属性的值（或可能是较早的缓存值），如果该属性未设置，则返回 <code>null</code>。
     *
     * <li><p>最后，如果类加载器不是 <code>URLClassLoader</code> 的实例，则返回
     * <code>java.rmi.server.codebase</code> 属性的值（或可能是较早的缓存值），如果该属性未设置，则返回 <code>null</code>。
     *
     * </ul>
     *
     * <p>对于以下描述的方法的实现，所有这些方法都接受一个名为
     * <code>codebase</code> 的 <code>String</code> 参数，该参数是一个空格分隔的 URL 列表，每个调用都有一个与
     * <code>codebase</code> 参数结合当前线程的上下文类加载器（参见
     * {@link Thread#getContextClassLoader()}）标识的 <i>代码库加载器</i>。当存在安全管理者时，此提供程序维护一个类加载器实例的内部表（至少是 {@link
     * java.net.URLClassLoader} 的实例），这些实例以其父类加载器和代码库 URL 路径（URL 的有序列表）为键。如果
     * <code>codebase</code> 参数为 <code>null</code>，则代码库 URL 路径是系统属性
     * <code>java.rmi.server.codebase</code> 的值或可能是较早的缓存值。对于给定上下文中给定的代码库 URL 路径，作为
     * <code>codebase</code> 参数传递给以下方法之一的调用的代码库加载器是表中具有指定代码库 URL 路径和当前线程的上下文类加载器作为其父类的加载器。如果不存在这样的加载器，则创建一个并添加到表中。表不维护对其包含的加载器的强引用，以允许它们及其定义的类在其他情况下不可达时被垃圾回收。为了防止任意不受信任的代码被隐式加载到没有安全管理器的虚拟机中，如果未设置安全管理器，代码库加载器就是当前线程的上下文类加载器（提供的代码库 URL 路径被忽略，因此远程类加载被禁用）。
     *
     * <p><b>{@link RMIClassLoaderSpi#getClassLoader(String)
     * getClassLoader}</b> 方法返回指定代码库 URL 路径的代码库加载器。如果有安全管理者，如果调用上下文没有权限连接到代码库 URL 路径中的所有 URL，则会抛出
     * <code>SecurityException</code>。
     *
     * <p><b>{@link
     * RMIClassLoaderSpi#loadClass(String,String,ClassLoader)
     * loadClass}</b> 方法尝试按如下方式加载具有指定名称的类：
     *
     * <blockquote>
     *
     * 如果 <code>defaultLoader</code> 参数非 <code>null</code>，则首先尝试使用
     * <code>defaultLoader</code> 加载具有指定 <code>name</code> 的类，例如通过评估
     *
     * <pre>
     *     Class.forName(name, false, defaultLoader)
     * </pre>
     *
     * 如果类成功从 <code>defaultLoader</code> 加载，则返回该类。如果抛出的异常不是
     * <code>ClassNotFoundException</code>，则将该异常抛给调用者。
     *
     * <p>接下来，<code>loadClass</code> 方法尝试使用指定代码库 URL 路径的代码库加载器加载具有指定
     * <code>name</code> 的类。如果有安全管理者，调用上下文必须有权限连接到代码库 URL 路径中的所有 URL；否则，将使用当前线程的上下文类加载器而不是代码库加载器。
     *
     * </blockquote>
     *
     * <p><b>{@link
     * RMIClassLoaderSpi#loadProxyClass(String,String[],ClassLoader)
     * loadProxyClass}</b> 方法尝试按如下方式返回具有命名接口的动态代理类：
     *
     * <blockquote>
     *
     * <p>如果 <code>defaultLoader</code> 参数非 <code>null</code> 且所有命名接口都可以通过该加载器解析，则
     *
     * <ul>
     *
     * <li>如果所有解析的接口都是 <code>public</code>，则首先尝试为代码库加载器中定义的解析接口获取动态代理类（使用
     * {@link
     * java.lang.reflect.Proxy#getProxyClass(ClassLoader,Class[])
     * Proxy.getProxyClass}）；如果该尝试抛出 <code>IllegalArgumentException</code>，则尝试为
     * <code>defaultLoader</code> 中定义的解析接口获取动态代理类。如果两次尝试都抛出
     * <code>IllegalArgumentException</code>，则此方法抛出 <code>ClassNotFoundException</code>。如果抛出任何其他异常，则将该异常抛给调用者。
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
     * @return  默认服务提供程序的规范实例
     *
     * @throws  SecurityException 如果存在安全管理者且其 <code>checkPermission</code> 方法调用失败
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
     * @deprecated 没有替代方法。从 Java 2 平台 v1.2 开始，RMI 不再使用此方法来获取类加载器的安全上下文。
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
     * 该方法假定它是在特权块中被调用的。
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
                    "提供者类无法分配给 RMIClassLoaderSpi");
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
                    "提供者类无法分配给 RMIClassLoaderSpi");
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
