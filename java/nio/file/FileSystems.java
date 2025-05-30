
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

package java.nio.file;

import java.nio.file.spi.FileSystemProvider;
import java.net.URI;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.lang.reflect.Constructor;

/**
 * 文件系统的工厂方法。此类定义了 {@link #getDefault
 * getDefault} 方法来获取默认文件系统，并定义了用于构建其他类型文件系统的工厂方法。
 *
 * <p> 该类的任何方法的首次调用会导致默认 {@link FileSystemProvider 提供商} 被加载。默认提供商，通过 URI 方案 "file" 识别，创建提供对 Java 虚拟机可访问的文件系统访问的 {@link FileSystem}。如果加载或初始化默认提供商失败，则会抛出未指定的错误。
 *
 * <p> {@link FileSystemProvider#installedProviders
 * installedProviders} 方法的首次调用，通过调用此类定义的任何 {@code
 * newFileSystem} 方法，会定位并加载所有已安装的文件系统提供商。已安装的提供商使用 {@link ServiceLoader} 类定义的服务提供程序加载设施加载。已安装的提供商使用系统类加载器加载。如果找不到系统类加载器，则使用扩展类加载器；如果没有扩展类加载器，则使用引导类加载器。提供程序通常通过将它们放在应用程序类路径上的 JAR 文件中或扩展目录中来安装，JAR 文件包含资源目录 {@code META-INF/services} 中的提供程序配置文件 {@code java.nio.file.spi.FileSystemProvider}，该文件列出了一个或多个具有无参数构造函数的 {@link FileSystemProvider} 具体子类的完全限定名称。已安装提供程序的定位顺序是实现特定的。如果提供程序实例化并且其 {@link FileSystemProvider#getScheme()
 * getScheme} 返回与之前实例化的提供程序相同的 URI 方案，则最近实例化的重复提供程序将被丢弃。URI 方案比较时不区分大小写。在构造期间，提供程序可以安全地访问与默认提供程序关联的文件，但需要注意避免循环加载其他已安装的提供程序。如果检测到已安装提供程序的循环加载，则会抛出未指定的错误。
 *
 * <p> 此类还定义了工厂方法，允许在定位提供程序时指定 {@link ClassLoader}。与已安装的提供程序一样，提供程序类通过将提供程序配置文件放在资源目录 {@code META-INF/services} 中来识别。
 *
 * <p> 如果一个线程启动了已安装文件系统提供程序的加载，而另一个线程调用了一个也尝试加载提供程序的方法，则该方法将阻塞，直到加载完成。
 *
 * @since 1.7
 */

public final class FileSystems {
    private FileSystems() {
    }

    // 默认文件系统的延迟初始化
    private static class DefaultFileSystemHolder {
        static final FileSystem defaultFileSystem = defaultFileSystem();

        // 返回默认文件系统
        private static FileSystem defaultFileSystem() {
            // 加载默认提供程序
            FileSystemProvider provider = AccessController
                .doPrivileged(new PrivilegedAction<FileSystemProvider>() {
                    public FileSystemProvider run() {
                        return getDefaultProvider();
                    }
                });

            // 返回文件系统
            return provider.getFileSystem(URI.create("file:///"));
        }

        // 返回默认提供程序
        private static FileSystemProvider getDefaultProvider() {
            FileSystemProvider provider = sun.nio.fs.DefaultFileSystemProvider.create();

            // 如果系统属性 java.nio.file.spi.DefaultFileSystemProvider
            // 已设置，则其值是默认提供程序的名称（或列表）
            String propValue = System
                .getProperty("java.nio.file.spi.DefaultFileSystemProvider");
            if (propValue != null) {
                for (String cn: propValue.split(",")) {
                    try {
                        Class<?> c = Class
                            .forName(cn, true, ClassLoader.getSystemClassLoader());
                        Constructor<?> ctor = c
                            .getDeclaredConstructor(FileSystemProvider.class);
                        provider = (FileSystemProvider)ctor.newInstance(provider);

                        // 必须是 "file"
                        if (!provider.getScheme().equals("file"))
                            throw new Error("默认提供程序必须使用方案 'file'");

                    } catch (Exception x) {
                        throw new Error(x);
                    }
                }
            }
            return provider;
        }
    }

    /**
     * 返回默认的 {@code FileSystem}。默认文件系统创建提供对 Java
     * 虚拟机可访问的文件系统访问的对象。文件系统的 <em>工作目录</em>
     * 是当前用户目录，由系统属性 {@code user.dir} 命名。这允许与 {@link java.io.File java.io.File}
     * 类的互操作性。
     *
     * <p> 该类定义的任何方法的首次调用会定位默认的 {@link FileSystemProvider 提供商} 对象。如果未定义系统属性 {@code java.nio.file.spi.DefaultFileSystemProvider}
     * 则默认提供程序是一个系统默认提供程序，用于创建默认文件系统。
     *
     * <p> 如果定义了系统属性 {@code java.nio.file.spi.DefaultFileSystemProvider}
     * 则其值被视为一个或多个具体提供程序类的完全限定名称列表，这些类由 URI 方案
     * {@code "file"} 识别。如果属性是一个包含多个名称的列表，则名称之间用逗号分隔。每个类使用系统类加载器加载，并通过调用一个参数类型为 {@code FileSystemProvider} 的单参数构造函数实例化。提供程序按照属性中列出的顺序加载和实例化。如果此过程失败或提供程序的方案不等于 {@code "file"}
     * 则会抛出未指定的错误。URI 方案通常比较时不区分大小写，但对于默认提供程序，方案必须是 {@code "file"}。第一个提供程序类通过传递系统默认提供程序的引用实例化。第二个提供程序类通过传递第一个提供程序实例的引用实例化。第三个提供程序类通过传递第二个实例的引用实例化，依此类推。最后实例化的提供程序成为默认提供程序；其 {@code
     * getFileSystem} 方法通过 URI {@code "file:///"} 调用以获取默认文件系统的引用。
     *
     * <p> 此方法的后续调用返回首次调用返回的文件系统。
     *
     * @return  默认文件系统
     */
    public static FileSystem getDefault() {
        return DefaultFileSystemHolder.defaultFileSystem;
    }

    /**
     * 返回对现有 {@code FileSystem} 的引用。
     *
     * <p> 此方法遍历 {@link FileSystemProvider#installedProviders()
     * 已安装} 的提供程序，以定位由给定 URI 的 URI
     * {@link URI#getScheme 方案} 识别的提供程序。URI 方案比较时不区分大小写。URI 的确切形式高度依赖于提供程序。如果找到提供程序，则调用其 {@link FileSystemProvider#getFileSystem
     * getFileSystem} 方法以获取 {@code
     * FileSystem} 的引用。
     *
     * <p> 一旦由此提供程序创建的文件系统 {@link FileSystem#close
     * 关闭}，则此方法返回对已关闭文件系统的引用或抛出 {@link FileSystemNotFoundException} 是提供程序依赖的。如果提供程序允许使用与之前创建的文件系统相同的 URI 创建新的文件系统，则此方法在文件系统关闭后（并在通过 {@link #newFileSystem newFileSystem} 方法创建新的实例之前）调用时会抛出异常。
     *
     * <p> 如果安装了安全经理，则提供程序实现可能需要在返回现有文件系统的引用之前检查权限。对于 {@link FileSystems#getDefault
     * 默认} 文件系统，不需要权限检查。
     *
     * @param   uri  用于定位文件系统的 URI
     *
     * @return  文件系统的引用
     *
     * @throws  IllegalArgumentException
     *          如果 {@code uri} 参数的预条件不满足
     * @throws  FileSystemNotFoundException
     *          如果由 URI 识别的文件系统不存在
     * @throws  ProviderNotFoundException
     *          如果未安装支持 URI 方案的提供程序
     * @throws  SecurityException
     *          如果安装了安全经理并且它拒绝了未指定的权限
     */
    public static FileSystem getFileSystem(URI uri) {
        String scheme = uri.getScheme();
        for (FileSystemProvider provider: FileSystemProvider.installedProviders()) {
            if (scheme.equalsIgnoreCase(provider.getScheme())) {
                return provider.getFileSystem(uri);
            }
        }
        throw new ProviderNotFoundException("Provider \"" + scheme + "\" not found");
    }

    /**
     * 构建由 {@link URI} 识别的新文件系统
     *
     * <p> 此方法遍历 {@link FileSystemProvider#installedProviders()
     * 已安装} 的提供程序，以定位由给定 URI 的 URI
     * {@link URI#getScheme 方案} 识别的提供程序。URI 方案比较时不区分大小写。URI 的确切形式高度依赖于提供程序。如果找到提供程序，则调用其 {@link FileSystemProvider#newFileSystem(URI,Map)
     * newFileSystem(URI,Map)} 方法以构建新的文件系统。
     *
     * <p> 一旦文件系统 {@link FileSystem#close 关闭}，则提供程序是否允许使用与之前创建的文件系统相同的 URI 创建新的文件系统是提供程序依赖的。
     *
     * <p> <b>使用示例：</b>
     * 假设已安装了一个由方案 {@code "memory"}
     * 识别的提供程序：
     * <pre>
     *   Map&lt;String,String&gt; env = new HashMap&lt;&gt;();
     *   env.put("capacity", "16G");
     *   env.put("blockSize", "4k");
     *   FileSystem fs = FileSystems.newFileSystem(URI.create("memory:///?name=logfs"), env);
     * </pre>
     *
     * @param   uri
     *          识别文件系统的 URI
     * @param   env
     *          用于配置文件系统的提供程序特定属性的映射；可以为空
     *
     * @return  新的文件系统
     *
     * @throws  IllegalArgumentException
     *          如果 {@code uri} 参数的预条件不满足，或 {@code env} 参数不包含提供程序所需的属性，或属性值无效
     * @throws  FileSystemAlreadyExistsException
     *          如果文件系统已创建
     * @throws  ProviderNotFoundException
     *          如果未安装支持 URI 方案的提供程序
     * @throws  IOException
     *          如果在创建文件系统时发生 I/O 错误
     * @throws  SecurityException
     *          如果安装了安全经理并且它拒绝了文件系统提供程序实现所需的未指定权限
     */
    public static FileSystem newFileSystem(URI uri, Map<String,?> env)
        throws IOException
    {
        return newFileSystem(uri, env, null);
    }

    /**
     * 构建由 {@link URI} 识别的新文件系统
     *
     * <p> 此方法首先尝试以与 {@link #newFileSystem(URI,Map) newFileSystem(URI,Map)}
     * 方法完全相同的方式定位已安装的提供程序。如果未找到支持 URI 方案的已安装提供程序，则尝试使用给定的类加载器定位提供程序。如果找到支持 URI 方案的提供程序，则调用其 {@link
     * FileSystemProvider#newFileSystem(URI,Map) newFileSystem(URI,Map)} 方法以构建新的文件系统。
     *
     * @param   uri
     *          识别文件系统的 URI
     * @param   env
     *          用于配置文件系统的提供程序特定属性的映射；可以为空
     * @param   loader
     *          用于定位提供程序的类加载器或 {@code null} 以仅尝试定位已安装的提供程序
     *
     * @return  新的文件系统
     *
     * @throws  IllegalArgumentException
     *          如果 {@code uri} 参数的预条件不满足，或 {@code env} 参数不包含提供程序所需的属性，或属性值无效
     * @throws  FileSystemAlreadyExistsException
     *          如果 URI 方案识别已安装的提供程序且文件系统已创建
     * @throws  ProviderNotFoundException
     *          如果未找到支持 URI 方案的提供程序
     * @throws  ServiceConfigurationError
     *          当加载服务提供程序时发生错误
     * @throws  IOException
     *          如果在创建文件系统时发生 I/O 错误
     * @throws  SecurityException
     *          如果安装了安全经理并且它拒绝了文件系统提供程序实现所需的未指定权限
     */
    public static FileSystem newFileSystem(URI uri, Map<String,?> env, ClassLoader loader)
        throws IOException
    {
        String scheme = uri.getScheme();


                    // 检查已安装的提供者
        for (FileSystemProvider provider: FileSystemProvider.installedProviders()) {
            if (scheme.equalsIgnoreCase(provider.getScheme())) {
                return provider.newFileSystem(uri, env);
            }
        }

        // 如果未找到，使用服务提供者加载设施
        if (loader != null) {
            ServiceLoader<FileSystemProvider> sl = ServiceLoader
                .load(FileSystemProvider.class, loader);
            for (FileSystemProvider provider: sl) {
                if (scheme.equalsIgnoreCase(provider.getScheme())) {
                    return provider.newFileSystem(uri, env);
                }
            }
        }

        throw new ProviderNotFoundException("Provider \"" + scheme + "\" not found");
    }

    /**
     * 构造一个新的 {@code FileSystem} 以访问文件的内容作为文件系统。
     *
     * <p> 此方法利用专门的提供者创建伪文件系统，其中一个或多个文件的内容被视为文件系统。
     *
     * <p> 此方法遍历 {@link FileSystemProvider#installedProviders() 已安装} 的提供者。它依次调用每个提供者的 {@link
     * FileSystemProvider#newFileSystem(Path,Map) newFileSystem(Path,Map)} 方法，并使用空映射。如果提供者返回一个文件系统，则迭代终止并返回该文件系统。如果所有已安装的提供者都没有返回 {@code FileSystem}，则尝试使用给定的类加载器定位提供者。如果提供者返回一个文件系统，则查找终止并返回该文件系统。
     *
     * @param   path
     *          文件的路径
     * @param   loader
     *          用于定位提供者的类加载器，或 {@code null} 仅尝试定位已安装的提供者
     *
     * @return  一个新的文件系统
     *
     * @throws  ProviderNotFoundException
     *          如果无法定位支持此文件类型的提供者
     * @throws  ServiceConfigurationError
     *          当加载服务提供者时发生错误
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          如果安装了安全经理并且它拒绝了未指定的权限
     */
    public static FileSystem newFileSystem(Path path,
                                           ClassLoader loader)
        throws IOException
    {
        if (path == null)
            throw new NullPointerException();
        Map<String,?> env = Collections.emptyMap();

        // 检查已安装的提供者
        for (FileSystemProvider provider: FileSystemProvider.installedProviders()) {
            try {
                return provider.newFileSystem(path, env);
            } catch (UnsupportedOperationException uoe) {
            }
        }

        // 如果未找到，使用服务提供者加载设施
        if (loader != null) {
            ServiceLoader<FileSystemProvider> sl = ServiceLoader
                .load(FileSystemProvider.class, loader);
            for (FileSystemProvider provider: sl) {
                try {
                    return provider.newFileSystem(path, env);
                } catch (UnsupportedOperationException uoe) {
                }
            }
        }

        throw new ProviderNotFoundException("Provider not found");
    }
}
