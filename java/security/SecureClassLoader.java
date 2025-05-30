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

package java.security;

import java.util.HashMap;
import java.util.ArrayList;
import java.net.URL;

import sun.security.util.Debug;

/**
 * 该类扩展了 ClassLoader，增加了定义类时关联代码源和权限的支持，这些权限默认由系统策略检索。
 *
 * @author  Li Gong
 * @author  Roland Schemers
 */
public class SecureClassLoader extends ClassLoader {
    /*
     * 如果初始化成功，此值设置为 true，安全检查将成功。否则，对象未初始化，对象无用。
     */
    private final boolean initialized;

    // 将 CodeSource 映射到 ProtectionDomain 的 HashMap
    // @GuardedBy("pdcache")
    private final HashMap<CodeSource, ProtectionDomain> pdcache =
                        new HashMap<>(11);

    private static final Debug debug = Debug.getInstance("scl");

    static {
        ClassLoader.registerAsParallelCapable();
    }

    /**
     * 创建一个新的 SecureClassLoader，使用指定的父类加载器进行委托。
     *
     * <p>如果有安全经理，此方法首先调用安全经理的 {@code checkCreateClassLoader}
     * 方法，以确保允许创建类加载器。
     * <p>
     * @param parent 父 ClassLoader
     * @exception  SecurityException  如果存在安全经理且其
     *             {@code checkCreateClassLoader} 方法不允许
     *             创建类加载器。
     * @see SecurityManager#checkCreateClassLoader
     */
    protected SecureClassLoader(ClassLoader parent) {
        super(parent);
        // 这是为了使堆栈深度与 1.1 一致
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        initialized = true;
    }

    /**
     * 创建一个新的 SecureClassLoader，使用默认的父类加载器进行委托。
     *
     * <p>如果有安全经理，此方法首先调用安全经理的 {@code checkCreateClassLoader}
     * 方法，以确保允许创建类加载器。
     *
     * @exception  SecurityException  如果存在安全经理且其
     *             {@code checkCreateClassLoader} 方法不允许
     *             创建类加载器。
     * @see SecurityManager#checkCreateClassLoader
     */
    protected SecureClassLoader() {
        super();
        // 这是为了使堆栈深度与 1.1 一致
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        initialized = true;
    }

    /**
     * 将字节数组转换为类 Class 的实例，可选关联 CodeSource。在类可以使用之前，必须解析它。
     * <p>
     * 如果提供了非 null 的 CodeSource，则会构造一个 ProtectionDomain 并与正在定义的类关联。
     * <p>
     * @param      name 类的预期名称，或 {@code null} 如果未知，使用 '.' 而不是 '/' 作为分隔符，且不带 ".class" 后缀。
     * @param      b    组成类数据的字节。位置 {@code off} 到 {@code off+len-1} 的字节应具有
     *             《Java&trade; 虚拟机规范》中定义的有效类文件格式。
     * @param      off  类数据在 {@code b} 中的起始偏移量
     * @param      len  类数据的长度
     * @param      cs   关联的 CodeSource，或 {@code null} 如果没有
     * @return 从数据创建的 {@code Class} 对象，以及可选的 CodeSource。
     * @exception  ClassFormatError 如果数据不包含有效的类
     * @exception  IndexOutOfBoundsException 如果 {@code off} 或 {@code len} 为负，或
     *             {@code off+len} 大于 {@code b.length}。
     *
     * @exception  SecurityException 如果尝试将此类添加到包含由不同证书集签名的类的包中，或
     *             类名以 "java." 开头。
     */
    protected final Class<?> defineClass(String name,
                                         byte[] b, int off, int len,
                                         CodeSource cs)
    {
        return defineClass(name, b, off, len, getProtectionDomain(cs));
    }

    /**
     * 将 {@link java.nio.ByteBuffer ByteBuffer} 转换为类 {@code Class} 的实例，可选关联 CodeSource。在类可以使用之前，必须解析它。
     * <p>
     * 如果提供了非 null 的 CodeSource，则会构造一个 ProtectionDomain 并与正在定义的类关联。
     * <p>
     * @param      name 类的预期名称，或 {@code null} 如果未知，使用 '.' 而不是 '/' 作为分隔符，且不带 ".class" 后缀。
     * @param      b    组成类数据的字节。位置 {@code b.position()} 到 {@code b.position() + b.limit() -1} 的字节应具有
     *                  《Java&trade; 虚拟机规范》中定义的有效类文件格式。
     * @param      cs   关联的 CodeSource，或 {@code null} 如果没有
     * @return 从数据创建的 {@code Class} 对象，以及可选的 CodeSource。
     * @exception  ClassFormatError 如果数据不包含有效的类
     * @exception  SecurityException 如果尝试将此类添加到包含由不同证书集签名的类的包中，或
     *             类名以 "java." 开头。
     *
     * @since  1.5
     */
    protected final Class<?> defineClass(String name, java.nio.ByteBuffer b,
                                         CodeSource cs)
    {
        return defineClass(name, b, getProtectionDomain(cs));
    }

    /**
     * 返回给定 CodeSource 对象的权限。
     * <p>
     * 当 defineClass 方法在构造类的 ProtectionDomain 时，会调用此方法。
     * <p>
     * @param codesource 代码源。
     *
     * @return 授予代码源的权限。
     *
     */
    protected PermissionCollection getPermissions(CodeSource codesource)
    {
        check();
        return new Permissions(); // ProtectionDomain 延迟绑定
    }

    /*
     * 返回指定 CodeSource 的缓存 ProtectionDomain。
     */
    private ProtectionDomain getProtectionDomain(CodeSource cs) {
        if (cs == null)
            return null;

        ProtectionDomain pd = null;
        synchronized (pdcache) {
            pd = pdcache.get(cs);
            if (pd == null) {
                PermissionCollection perms = getPermissions(cs);
                pd = new ProtectionDomain(cs, perms, this, null);
                pdcache.put(cs, pd);
                if (debug != null) {
                    debug.println(" getPermissions "+ pd);
                    debug.println("");
                }
            }
        }
        return pd;
    }

    /*
     * 检查类加载器是否已初始化。
     */
    private void check() {
        if (!initialized) {
            throw new SecurityException("ClassLoader object not initialized");
        }
    }

}
