
/*
 * 版权所有 (c) 2007, 2013, Oracle 和/或其附属公司。保留所有权利。
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
 * getDefault} 方法以获取默认文件系统，并定义了构造其他类型文件系统的工厂方法。
 *
 * <p> 本类定义的任何方法的首次调用会导致默认 {@link FileSystemProvider 提供商} 被加载。默认提供商通过 URI 方案 "file" 识别，创建提供对 Java 虚拟机可访问的文件系统访问的 {@link FileSystem}。如果加载或初始化默认提供商失败，则会抛出未指定的错误。
 *
 * <p> 本类定义的任何 {@code
 * newFileSystem} 方法的首次调用，通过调用 {@link FileSystemProvider#installedProviders
 * installedProviders} 方法，会定位并加载所有已安装的文件系统提供商。已安装的提供商使用 {@link ServiceLoader} 类定义的服务提供商加载设施加载，使用系统类加载器加载。如果找不到系统类加载器，则使用扩展类加载器；如果没有扩展类加载器，则使用引导类加载器。通常通过将它们放在应用程序类路径或扩展目录中的 JAR 文件中来安装提供商，JAR 文件包含资源目录 {@code META-INF/services} 中名为 {@code java.nio.file.spi.FileSystemProvider} 的提供程序配置文件，文件中列出了一个或多个 {@link FileSystemProvider} 的具体子类的完全限定名，这些子类具有无参数构造函数。
 * 已安装的提供商的定位顺序是实现特定的。如果实例化了一个提供商，其 {@link FileSystemProvider#getScheme()
 * getScheme} 返回与先前实例化的提供商相同的 URI 方案，则最近实例化的重复项将被丢弃。URI 方案比较时不区分大小写。在构造过程中，提供商可以安全地访问与默认提供商关联的文件，但需要注意避免循环加载其他已安装的提供商。如果检测到已安装提供商的循环加载，则会抛出未指定的错误。
 *
 * <p> 本类还定义了工厂方法，允许在定位提供商时指定一个 {@link ClassLoader}。与已安装的提供商一样，提供商类通过将提供程序配置文件放置在资源目录 {@code META-INF/services} 中来识别。
 *
 * <p> 如果一个线程启动已安装文件系统提供商的加载，而另一个线程调用一个也尝试加载提供商的方法，则该方法将阻塞，直到加载完成。
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
            // 加载默认提供商
            FileSystemProvider provider = AccessController
                .doPrivileged(new PrivilegedAction<FileSystemProvider>() {
                    public FileSystemProvider run() {
                        return getDefaultProvider();
                    }
                });

            // 返回文件系统
            return provider.getFileSystem(URI.create("file:///"));
        }

        // 返回默认提供商
        private static FileSystemProvider getDefaultProvider() {
            FileSystemProvider provider = sun.nio.fs.DefaultFileSystemProvider.instance();

            // 如果设置了系统属性 java.nio.file.spi.DefaultFileSystemProvider
            // 则其值是默认提供商（或列表）的名称
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
                            throw new Error("默认提供商必须使用方案 'file'");

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
     * 是当前用户目录，由系统属性 {@code user.dir} 命名。
     * 这允许与 {@link java.io.File java.io.File} 类互操作。
     *
     * <p> 本类定义的任何方法的首次调用会定位默认 {@link FileSystemProvider 提供商} 对象。如果未定义系统属性 {@code java.nio.file.spi.DefaultFileSystemProvider}，则默认提供商是一个系统默认提供商，用于创建默认文件系统。
     *
     * <p> 如果定义了系统属性 {@code java.nio.file.spi.DefaultFileSystemProvider}
     * 则将其视为一个或多个具体提供商类的完全限定名列表，这些类由 URI 方案
     * {@code "file"} 识别。如果属性是一个包含多个名称的列表，则名称之间用逗号分隔。每个类使用系统类加载器加载，并通过调用一个参数为 {@code FileSystemProvider} 的构造函数实例化。提供商按属性中列出的顺序加载和实例化。如果此过程失败或提供商的方案不等于 {@code "file"}
     * 则会抛出未指定的错误。URI 方案通常比较时不区分大小写，但对于默认提供商，方案必须是 {@code "file"}。第一个提供商类通过传递系统默认提供商的引用实例化。
     * 第二个提供商类通过传递第一个提供商实例的引用实例化。第三个提供商类通过传递第二个实例的引用实例化，依此类推。最后实例化的提供商成为默认提供商；其 {@code
     * getFileSystem} 方法通过 URI {@code "file:///"} 调用以获取默认文件系统的引用。
     *
     * <p> 该方法的后续调用返回首次调用返回的文件系统。
     *
     * @return  默认文件系统
     */
    public static FileSystem getDefault() {
        return DefaultFileSystemHolder.defaultFileSystem;
    }
}

                /**
     * 返回对现有 {@code FileSystem} 的引用。
     *
     * <p> 此方法遍历 {@link FileSystemProvider#installedProviders()
     * 已安装} 的提供程序，以找到由给定 URI 的 URI
     * {@link URI#getScheme 方案} 标识的提供程序。URI 方案比较时不区分大小写。URI 的确切形式高度依赖于提供程序。如果找到，将调用提供程序的 {@link FileSystemProvider#getFileSystem
     * getFileSystem} 方法以获取对 {@code
     * FileSystem} 的引用。
     *
     * <p> 一旦由该提供程序创建的文件系统被 {@link FileSystem#close
     * 关闭}，此方法返回对已关闭文件系统的引用或抛出 {@link FileSystemNotFoundException} 是提供程序依赖的。如果提供程序允许使用与之前创建的文件系统相同的 URI 创建新的文件系统，那么在文件系统关闭后（且在通过 {@link #newFileSystem newFileSystem} 方法创建新实例之前）调用此方法将抛出异常。
     *
     * <p> 如果安装了安全管理者，则提供程序实现可能需要在返回对现有文件系统的引用之前检查权限。对于 {@link FileSystems#getDefault
     * 默认} 文件系统，不需要权限检查。
     *
     * @param   uri  用于定位文件系统的 URI
     *
     * @return  文件系统的引用
     *
     * @throws  IllegalArgumentException
     *          如果 {@code uri} 参数的预条件未满足
     * @throws  FileSystemNotFoundException
     *          如果由 URI 标识的文件系统不存在
     * @throws  ProviderNotFoundException
     *          如果没有安装支持 URI 方案的提供程序
     * @throws  SecurityException
     *          如果安装了安全管理者且它拒绝了未指定的权限
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
     * 构建由 {@link URI} 标识的新文件系统
     *
     * <p> 此方法遍历 {@link FileSystemProvider#installedProviders()
     * 已安装} 的提供程序，以找到由给定 URI 的 URI
     * {@link URI#getScheme 方案} 标识的提供程序。URI 方案比较时不区分大小写。URI 的确切形式高度依赖于提供程序。如果找到，将调用提供程序的 {@link FileSystemProvider#newFileSystem(URI,Map)
     * newFileSystem(URI,Map)} 方法以构建新的文件系统。
     *
     * <p> 一旦文件系统被 {@link FileSystem#close 关闭}，提供程序是否允许使用与之前创建的文件系统相同的 URI 创建新的文件系统是提供程序依赖的。
     *
     * <p> <b>使用示例：</b>
     * 假设安装了一个标识为方案 {@code "memory"}
     * 的提供程序：
     * <pre>
     *   Map&lt;String,String&gt; env = new HashMap&lt;&gt;();
     *   env.put("capacity", "16G");
     *   env.put("blockSize", "4k");
     *   FileSystem fs = FileSystems.newFileSystem(URI.create("memory:///?name=logfs"), env);
     * </pre>
     *
     * @param   uri
     *          用于标识文件系统的 URI
     * @param   env
     *          用于配置文件系统的提供程序特定属性的映射；可以为空
     *
     * @return  新的文件系统
     *
     * @throws  IllegalArgumentException
     *          如果 {@code uri} 参数的预条件未满足，或者 {@code env} 参数不包含提供程序所需的属性，或者属性值无效
     * @throws  FileSystemAlreadyExistsException
     *          如果文件系统已创建
     * @throws  ProviderNotFoundException
     *          如果没有安装支持 URI 方案的提供程序
     * @throws  IOException
     *          如果创建文件系统时发生 I/O 错误
     * @throws  SecurityException
     *          如果安装了安全管理者且它拒绝了文件系统提供程序实现所需的未指定权限
     */
    public static FileSystem newFileSystem(URI uri, Map<String,?> env)
        throws IOException
    {
        return newFileSystem(uri, env, null);
    }

    /**
     * 构建由 {@link URI} 标识的新文件系统
     *
     * <p> 此方法首先尝试以与 {@link #newFileSystem(URI,Map) newFileSystem(URI,Map)}
     * 方法完全相同的方式定位已安装的提供程序。如果没有任何已安装的提供程序支持 URI 方案，则尝试使用给定的类加载器定位提供程序。如果找到支持 URI 方案的提供程序，则调用其 {@link
     * FileSystemProvider#newFileSystem(URI,Map) newFileSystem(URI,Map)} 方法以构建新的文件系统。
     *
     * @param   uri
     *          用于标识文件系统的 URI
     * @param   env
     *          用于配置文件系统的提供程序特定属性的映射；可以为空
     * @param   loader
     *          用于定位提供程序的类加载器，或 {@code null} 仅尝试定位已安装的提供程序
     *
     * @return  新的文件系统
     *
     * @throws  IllegalArgumentException
     *          如果 {@code uri} 参数的预条件未满足，或者 {@code env} 参数不包含提供程序所需的属性，或者属性值无效
     * @throws  FileSystemAlreadyExistsException
     *          如果 URI 方案标识了一个已安装的提供程序且文件系统已创建
     * @throws  ProviderNotFoundException
     *          如果未找到支持 URI 方案的提供程序
     * @throws  ServiceConfigurationError
     *          当加载服务提供程序时发生错误
     * @throws  IOException
     *          创建文件系统时发生 I/O 错误
     * @throws  SecurityException
     *          如果安装了安全管理者且它拒绝了文件系统提供程序实现所需的未指定权限
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
     * 构造一个新的 {@code FileSystem} 以访问文件的内容作为一个文件系统。
     *
     * <p> 此方法利用专门的提供者创建伪文件系统，其中一个或多个文件的内容被视为文件系统。
     *
     * <p> 此方法遍历 {@link FileSystemProvider#installedProviders() 已安装} 的提供者。它依次调用每个提供者的 {@link
     * FileSystemProvider#newFileSystem(Path,Map) newFileSystem(Path,Map)} 方法，并传递一个空映射。如果提供者返回一个文件系统，则迭代终止并返回该文件系统。如果所有已安装的提供者都没有返回 {@code FileSystem}，则尝试使用给定的类加载器定位提供者。如果提供者返回一个文件系统，则查找终止并返回该文件系统。
     *
     * @param   path
     *          文件的路径
     * @param   loader
     *          用于定位提供者的类加载器，或 {@code null} 仅尝试定位已安装的提供者
     *
     * @return  一个新的文件系统
     *
     * @throws  ProviderNotFoundException
     *          如果无法找到支持此文件类型的提供者
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
