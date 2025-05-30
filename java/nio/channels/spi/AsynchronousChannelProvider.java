/*
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.channels.spi;

import java.nio.channels.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.ServiceConfigurationError;
import java.util.concurrent.*;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * 异步通道的服务提供者类。
 *
 * <p> 异步通道提供者是此类的具体子类，具有无参数构造函数并实现以下指定的抽象方法。给定的 Java 虚拟机调用维护一个系统范围的默认提供者实例，该实例由 {@link
 * #provider() provider} 方法返回。该方法的第一次调用将按照以下指定的方式定位默认提供者。
 *
 * <p> 本类中的所有方法都适用于多个并发线程。 </p>
 *
 * @since 1.7
 */

public abstract class AsynchronousChannelProvider {
    private static Void checkPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(new RuntimePermission("asynchronousChannelProvider"));
        return null;
    }
    private AsynchronousChannelProvider(Void ignore) { }

    /**
     * 初始化此类的新实例。
     *
     * @throws  SecurityException
     *          如果已安装了安全经理并且它拒绝
     *          {@link RuntimePermission}<tt>("asynchronousChannelProvider")</tt>
     */
    protected AsynchronousChannelProvider() {
        this(checkPermission());
    }

    // 默认提供者的延迟初始化
    private static class ProviderHolder {
        static final AsynchronousChannelProvider provider = load();

        private static AsynchronousChannelProvider load() {
            return AccessController
                .doPrivileged(new PrivilegedAction<AsynchronousChannelProvider>() {
                    public AsynchronousChannelProvider run() {
                        AsynchronousChannelProvider p;
                        p = loadProviderFromProperty();
                        if (p != null)
                            return p;
                        p = loadProviderAsService();
                        if (p != null)
                            return p;
                        return sun.nio.ch.DefaultAsynchronousChannelProvider.create();
                    }});
        }

        private static AsynchronousChannelProvider loadProviderFromProperty() {
            String cn = System.getProperty("java.nio.channels.spi.AsynchronousChannelProvider");
            if (cn == null)
                return null;
            try {
                Class<?> c = Class.forName(cn, true,
                                           ClassLoader.getSystemClassLoader());
                return (AsynchronousChannelProvider)c.newInstance();
            } catch (ClassNotFoundException x) {
                throw new ServiceConfigurationError(null, x);
            } catch (IllegalAccessException x) {
                throw new ServiceConfigurationError(null, x);
            } catch (InstantiationException x) {
                throw new ServiceConfigurationError(null, x);
            } catch (SecurityException x) {
                throw new ServiceConfigurationError(null, x);
            }
        }

        private static AsynchronousChannelProvider loadProviderAsService() {
            ServiceLoader<AsynchronousChannelProvider> sl =
                ServiceLoader.load(AsynchronousChannelProvider.class,
                                   ClassLoader.getSystemClassLoader());
            Iterator<AsynchronousChannelProvider> i = sl.iterator();
            for (;;) {
                try {
                    return (i.hasNext()) ? i.next() : null;
                } catch (ServiceConfigurationError sce) {
                    if (sce.getCause() instanceof SecurityException) {
                        // 忽略安全异常，尝试下一个提供者
                        continue;
                    }
                    throw sce;
                }
            }
        }
    }

    /**
     * 返回此 Java 虚拟机调用的系统范围的默认异步通道提供者。
     *
     * <p> 该方法的第一次调用按照以下方式定位默认提供者对象： </p>
     *
     * <ol>
     *
     *   <li><p> 如果系统属性
     *   <tt>java.nio.channels.spi.AsynchronousChannelProvider</tt> 已定义
     *   则将其视为具体提供者类的完全限定名称。
     *   加载并实例化该类；如果此过程失败，则抛出未指定的错误。  </p></li>
     *
     *   <li><p> 如果已将提供者类安装在系统类加载器可见的 jar 文件中，并且该 jar 文件包含一个名为
     *   <tt>java.nio.channels.spi.AsynchronousChannelProvider</tt> 的提供者配置文件
     *   在资源目录 <tt>META-INF/services</tt> 中，则采用该文件中指定的第一个类名。
     *   加载并实例化该类；如果此过程失败，则抛出未指定的错误。  </p></li>
     *
     *   <li><p> 最后，如果未通过上述任何方式指定提供者，则实例化系统默认提供者类并返回结果。  </p></li>
     *
     * </ol>
     *
     * <p> 该方法的后续调用返回第一次调用返回的提供者。 </p>
     *
     * @return  系统范围的默认 AsynchronousChannel 提供者
     */
    public static AsynchronousChannelProvider provider() {
        return ProviderHolder.provider;
    }

    /**
     * 使用固定线程池构造新的异步通道组。
     *
     * @param   nThreads
     *          线程池中的线程数
     * @param   threadFactory
     *          创建新线程时使用的工厂
     *
     * @return  新的异步通道组
     *
     * @throws  IllegalArgumentException
     *          如果 {@code nThreads <= 0}
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @see AsynchronousChannelGroup#withFixedThreadPool
     */
    public abstract AsynchronousChannelGroup
        openAsynchronousChannelGroup(int nThreads, ThreadFactory threadFactory) throws IOException;

    /**
     * 使用给定的线程池构造新的异步通道组。
     *
     * @param   executor
     *          线程池
     * @param   initialSize
     *          一个 {@code >=0} 的值或一个负值表示实现特定的默认值
     *
     * @return  新的异步通道组
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @see AsynchronousChannelGroup#withCachedThreadPool
     */
    public abstract AsynchronousChannelGroup
        openAsynchronousChannelGroup(ExecutorService executor, int initialSize) throws IOException;

    /**
     * 打开异步服务器套接字通道。
     *
     * @param   group
     *          绑定通道的组，或 {@code null} 以绑定到默认组
     *
     * @return  新的通道
     *
     * @throws  IllegalChannelGroupException
     *          如果创建组的提供者与此提供者不同
     * @throws  ShutdownChannelGroupException
     *          组已关闭
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public abstract AsynchronousServerSocketChannel openAsynchronousServerSocketChannel
        (AsynchronousChannelGroup group) throws IOException;

    /**
     * 打开异步套接字通道。
     *
     * @param   group
     *          绑定通道的组，或 {@code null} 以绑定到默认组
     *
     * @return  新的通道
     *
     * @throws  IllegalChannelGroupException
     *          如果创建组的提供者与此提供者不同
     * @throws  ShutdownChannelGroupException
     *          组已关闭
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public abstract AsynchronousSocketChannel openAsynchronousSocketChannel
        (AsynchronousChannelGroup group) throws IOException;
}
