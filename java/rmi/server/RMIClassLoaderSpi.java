/*
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
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

/**
 * <code>RMIClassLoaderSpi</code> 是 <code>RMIClassLoader</code> 的服务提供者接口。
 *
 * 特别地，<code>RMIClassLoaderSpi</code> 实例提供了 <code>RMIClassLoader</code> 的以下静态方法的实现：
 *
 * <ul>
 *
 * <li>{@link RMIClassLoader#loadClass(URL,String)}
 * <li>{@link RMIClassLoader#loadClass(String,String)}
 * <li>{@link RMIClassLoader#loadClass(String,String,ClassLoader)}
 * <li>{@link RMIClassLoader#loadProxyClass(String,String[],ClassLoader)}
 * <li>{@link RMIClassLoader#getClassLoader(String)}
 * <li>{@link RMIClassLoader#getClassAnnotation(Class)}
 *
 * </ul>
 *
 * 当调用这些方法之一时，其行为是委托给此类的一个实例上的相应方法。
 * 每个方法如何委托给提供者实例的详细信息在每个特定方法的文档中描述。
 * 有关如何选择提供者实例的描述，请参阅 {@link RMIClassLoader} 的文档。
 *
 * @author      Peter Jones
 * @author      Laird Dornin
 * @see         RMIClassLoader
 * @since       1.4
 */
public abstract class RMIClassLoaderSpi {

    /**
     * 为 {@link RMIClassLoader#loadClass(URL,String)}、
     * {@link RMIClassLoader#loadClass(String,String)} 和
     * {@link RMIClassLoader#loadClass(String,String,ClassLoader)} 提供实现。
     *
     * 从代码库 URL 路径加载类，可选地使用提供的加载器。
     *
     * 通常，提供者实现将尝试使用指定的 <code>defaultLoader</code>（如果指定了）解析命名类，
     * 然后再尝试从代码库 URL 路径解析类。
     *
     * <p>此方法的实现必须返回具有给定名称的类或抛出异常。
     *
     * @param   codebase 要从中加载类的 URL 列表（以空格分隔），或 <code>null</code>
     *
     * @param   name 要加载的类的名称
     *
     * @param   defaultLoader 额外的上下文类加载器，或 <code>null</code>
     *
     * @return  表示已加载类的 <code>Class</code> 对象
     *
     * @throws  MalformedURLException 如果 <code>codebase</code> 不为 <code>null</code> 且包含无效 URL，或
     * 如果 <code>codebase</code> 为 <code>null</code> 且用于加载类的提供者特定 URL 无效
     *
     * @throws  ClassNotFoundException 如果在指定位置找不到类的定义
     */
    public abstract Class<?> loadClass(String codebase, String name,
                                       ClassLoader defaultLoader)
        throws MalformedURLException, ClassNotFoundException;

    /**
     * 为 {@link RMIClassLoader#loadProxyClass(String,String[],ClassLoader)} 提供实现。
     *
     * 从代码库 URL 路径加载实现给定名称接口集的动态代理类（参见 {@link java.lang.reflect.Proxy}），
     * 可选地使用提供的加载器。
     *
     * <p>此方法的实现必须返回实现命名接口的代理类或抛出异常。
     *
     * @param   codebase 要从中加载类的 URL 列表（以空格分隔），或 <code>null</code>
     *
     * @param   interfaces 代理类要实现的接口名称
     *
     * @return  实现命名接口的动态代理类
     *
     * @param   defaultLoader 额外的上下文类加载器，或 <code>null</code>
     *
     * @throws  MalformedURLException 如果 <code>codebase</code> 不为 <code>null</code> 且包含无效 URL，或
     * 如果 <code>codebase</code> 为 <code>null</code> 且用于加载类的提供者特定 URL 无效
     *
     * @throws  ClassNotFoundException 如果在指定位置找不到命名接口之一的定义，或
     * 如果创建动态代理类失败（例如，如果 {@link java.lang.reflect.Proxy#getProxyClass(ClassLoader,Class[])}
     * 会为给定的接口列表抛出 <code>IllegalArgumentException</code>）
     */
    public abstract Class<?> loadProxyClass(String codebase,
                                            String[] interfaces,
                                            ClassLoader defaultLoader)
        throws MalformedURLException, ClassNotFoundException;

    /**
     * 为 {@link RMIClassLoader#getClassLoader(String)} 提供实现。
     *
     * 返回从给定代码库 URL 路径加载类的类加载器。
     *
     * <p>如果有安全经理，其 <code>checkPermission</code> 方法将被调用，权限为
     * <code>RuntimePermission("getClassLoader")</code>；这可能导致 <code>SecurityException</code>。
     * 此方法的实现也可能执行进一步的安全检查，以验证调用上下文是否有权限连接到代码库 URL 路径中的所有 URL。
     *
     * @param   codebase 返回的类加载器将从中加载类的 URL 列表（以空格分隔），或 <code>null</code>
     *
     * @return  从给定代码库 URL 路径加载类的类加载器
     *
     * @throws  MalformedURLException 如果 <code>codebase</code> 不为 <code>null</code> 且包含无效 URL，或
     * 如果 <code>codebase</code> 为 <code>null</code> 且用于标识类加载器的提供者特定 URL 无效
     *
     * @throws  SecurityException 如果存在安全经理且其 <code>checkPermission</code> 方法调用失败，或
     * 调用者没有权限连接到代码库 URL 路径中的所有 URL
     */
    public abstract ClassLoader getClassLoader(String codebase)
        throws MalformedURLException; // SecurityException

    /**
     * 为 {@link RMIClassLoader#getClassAnnotation(Class)} 提供实现。
     *
     * 返回 RMI 在序列化给定类的对象时用于注解类描述符的位置字符串（表示类定义的位置）。
     *
     * @param   cl 要获取注解的类
     *
     * @return  在序列化时用于注解给定类的字符串，或 <code>null</code>
     *
     * @throws  NullPointerException 如果 <code>cl</code> 为 <code>null</code>
     */
    public abstract String getClassAnnotation(Class<?> cl);
}
