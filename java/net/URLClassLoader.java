
/*
 * 版权所有 (c) 1997, 2021, Oracle 和/或其附属公司。保留所有权利。
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

package java.net;

import java.io.Closeable;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.SecureClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import sun.misc.Resource;
import sun.misc.SharedSecrets;
import sun.misc.URLClassPath;
import sun.net.www.ParseUtil;
import sun.security.util.SecurityConstants;

/**
 * 此类加载器用于从引用 JAR 文件和目录的 URL 搜索路径加载类和资源。任何以 '/' 结尾的 URL 都假定引用一个目录。否则，假定该 URL 引用一个 JAR 文件，将在需要时打开。
 * <p>
 * 创建 URLClassLoader 实例的线程的 AccessControlContext 将在随后加载类和资源时使用。
 * <p>
 * 默认情况下，加载的类仅被授予访问创建 URLClassLoader 时指定的 URL 的权限。
 *
 * @author  David Connelly
 * @since   1.2
 */
public class URLClassLoader extends SecureClassLoader implements Closeable {
    /* 类和资源的搜索路径 */
    private final URLClassPath ucp;

    /* 加载类和资源时使用的上下文 */
    private final AccessControlContext acc;

    /**
     * 为给定的 URL 构造一个新的 URLClassLoader。这些 URL 将在指定的父类加载器中首先搜索类和资源之后，按照指定的顺序进行搜索。任何以 '/' 结尾的 URL 都假定引用一个目录。否则，假定该 URL 引用一个 JAR 文件，将在需要时下载并打开。
     *
     * <p>如果有安全经理，此方法首先调用安全经理的 {@code checkCreateClassLoader} 方法，以确保允许创建类加载器。
     *
     * @param urls 从中加载类和资源的 URL
     * @param parent 用于委托的父类加载器
     * @exception  SecurityException 如果存在安全经理且其 {@code checkCreateClassLoader} 方法不允许创建类加载器。
     * @exception  NullPointerException 如果 {@code urls} 为 {@code null}。
     * @see SecurityManager#checkCreateClassLoader
     */
    public URLClassLoader(URL[] urls, ClassLoader parent) {
        super(parent);
        // 这是为了使堆栈深度与 1.1 保持一致
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        this.acc = AccessController.getContext();
        ucp = new URLClassPath(urls, acc);
    }

    URLClassLoader(URL[] urls, ClassLoader parent,
                   AccessControlContext acc) {
        super(parent);
        // 这是为了使堆栈深度与 1.1 保持一致
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        this.acc = acc;
        ucp = new URLClassPath(urls, acc);
    }

    /**
     * 使用默认委托父类加载器为指定的 URL 构造一个新的 URLClassLoader。这些 URL 将在父类加载器中首先搜索类和资源之后，按照指定的顺序进行搜索。任何以 '/' 结尾的 URL 都假定引用一个目录。否则，假定该 URL 引用一个 JAR 文件，将在需要时下载并打开。
     *
     * <p>如果有安全经理，此方法首先调用安全经理的 {@code checkCreateClassLoader} 方法，以确保允许创建类加载器。
     *
     * @param urls 从中加载类和资源的 URL
     *
     * @exception  SecurityException 如果存在安全经理且其 {@code checkCreateClassLoader} 方法不允许创建类加载器。
     * @exception  NullPointerException 如果 {@code urls} 为 {@code null}。
     * @see SecurityManager#checkCreateClassLoader
     */
    public URLClassLoader(URL[] urls) {
        super();
        // 这是为了使堆栈深度与 1.1 保持一致
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        this.acc = AccessController.getContext();
        ucp = new URLClassPath(urls, acc);
    }

    URLClassLoader(URL[] urls, AccessControlContext acc) {
        super();
        // 这是为了使堆栈深度与 1.1 保持一致
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        this.acc = acc;
        ucp = new URLClassPath(urls, acc);
    }

    /**
     * 为指定的 URL、父类加载器和 URLStreamHandlerFactory 构造一个新的 URLClassLoader。父参数将用作委托的父类加载器。工厂参数将用作创建新 jar URL 时的流处理程序工厂。
     *
     * <p>如果有安全经理，此方法首先调用安全经理的 {@code checkCreateClassLoader} 方法，以确保允许创建类加载器。
     *
     * @param urls 从中加载类和资源的 URL
     * @param parent 用于委托的父类加载器
     * @param factory 创建 URL 时使用的 URLStreamHandlerFactory
     *
     * @exception  SecurityException 如果存在安全经理且其 {@code checkCreateClassLoader} 方法不允许创建类加载器。
     * @exception  NullPointerException 如果 {@code urls} 为 {@code null}。
     * @see SecurityManager#checkCreateClassLoader
     */
    public URLClassLoader(URL[] urls, ClassLoader parent,
                          URLStreamHandlerFactory factory) {
        super(parent);
        // 这是为了使堆栈深度与 1.1 保持一致
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        acc = AccessController.getContext();
        ucp = new URLClassPath(urls, factory, acc);
    }


                /* 一个映射（用作集合）用于跟踪可关闭的本地资源
     * （要么是 JarFiles，要么是 FileInputStreams）。我们不关心
     * Http 资源，因为它们不需要关闭。
     *
     * 如果资源来自 jar 文件
     * 我们将保留对 JarFile 对象的（弱）引用，该对象可以在调用 URLClassLoader.close() 时关闭。由于 jar 文件
     * 缓存，通常每个底层 jar 文件只有一个 JarFile 对象。
     *
     * 对于文件资源，这可能是一种不太常见的情况
     * 我们必须保留对每个流的弱引用。
     */

    private WeakHashMap<Closeable,Void>
        closeables = new WeakHashMap<>();

    /**
     * 返回用于读取指定资源的输入流。
     * 如果此加载器已关闭，则通过此方法打开的任何资源
     * 将被关闭。
     *
     * <p> 搜索顺序在 {@link
     * #getResource(String)} 的文档中描述。 </p>
     *
     * @param  name
     *         资源名称
     *
     * @return 用于读取资源的输入流，如果找不到资源则返回 {@code null}
     *
     * @since  1.7
     */
    public InputStream getResourceAsStream(String name) {
        URL url = getResource(name);
        try {
            if (url == null) {
                return null;
            }
            URLConnection urlc = url.openConnection();
            InputStream is = urlc.getInputStream();
            if (urlc instanceof JarURLConnection) {
                JarURLConnection juc = (JarURLConnection)urlc;
                JarFile jar = juc.getJarFile();
                synchronized (closeables) {
                    if (!closeables.containsKey(jar)) {
                        closeables.put(jar, null);
                    }
                }
            } else if (urlc instanceof sun.net.www.protocol.file.FileURLConnection) {
                synchronized (closeables) {
                    closeables.put(is, null);
                }
            }
            return is;
        } catch (IOException e) {
            return null;
        }
    }

   /**
    * 关闭此 URLClassLoader，使其不能再用于加载
    * 由此加载器定义的新类或资源。
    * 仍然可以访问此加载器的父级在委托层次结构中定义的任何类或资源。此外，任何已加载的类或资源
    * 仍然可以访问。
    * <p>
    * 对于 jar: 和 file: URL，它还会关闭由其打开的任何文件。如果在调用 {@code close} 方法时另一个线程正在加载一个
    * 类，那么该加载的结果是不确定的。
    * <p>
    * 该方法会尽力关闭所有打开的文件，通过内部捕获 {@link IOException}s。未检查的异常
    * 和错误不会被捕获。对已关闭的加载器调用 close 没有影响。
    * <p>
    * @exception IOException 如果关闭此类加载器打开的任何文件
    * 导致 IOException。任何此类异常都会在内部捕获。
    * 如果只捕获到一个异常，则重新抛出。如果捕获到多个异常，则将第二个及后续异常作为第一个捕获异常的抑制异常添加，然后重新抛出第一个异常。
    *
    * @exception SecurityException 如果设置了安全经理，并且它拒绝
    *   {@link RuntimePermission}{@code ("closeClassLoader")}
    *
    * @since 1.7
    */
    public void close() throws IOException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(new RuntimePermission("closeClassLoader"));
        }
        List<IOException> errors = ucp.closeLoaders();

        // 现在关闭任何剩余的流。

        synchronized (closeables) {
            Set<Closeable> keys = closeables.keySet();
            for (Closeable c : keys) {
                try {
                    c.close();
                } catch (IOException ioex) {
                    errors.add(ioex);
                }
            }
            closeables.clear();
        }

        if (errors.isEmpty()) {
            return;
        }

        IOException firstex = errors.remove(0);

        // 抑制任何剩余的异常

        for (IOException error: errors) {
            firstex.addSuppressed(error);
        }
        throw firstex;
    }

    /**
     * 将指定的 URL 添加到用于搜索类和资源的 URL 列表中。
     * <p>
     * 如果指定的 URL 为 {@code null} 或已经在
     * URL 列表中，或者此加载器已关闭，则调用此方法没有效果。
     *
     * @param url 要添加到 URL 搜索路径中的 URL
     */
    protected void addURL(URL url) {
        ucp.addURL(url);
    }

    /**
     * 返回用于加载类和资源的 URL 搜索路径。
     * 这包括构造函数中指定的原始 URL 列表，以及随后通过 addURL() 方法添加的任何 URL。
     * @return 用于加载类和资源的 URL 搜索路径。
     */
    public URL[] getURLs() {
        return ucp.getURLs();
    }

    /**
     * 从 URL 搜索路径中查找并加载指定名称的类。任何引用 JAR 文件的 URL 都会按需加载和打开
     * 直到找到该类。
     *
     * @param name 类的名称
     * @return 结果类
     * @exception ClassNotFoundException 如果找不到该类，
     *            或加载器已关闭。
     * @exception NullPointerException 如果 {@code name} 为 {@code null}。
     */
    protected Class<?> findClass(final String name)
        throws ClassNotFoundException
    {
        final Class<?> result;
        try {
            result = AccessController.doPrivileged(
                new PrivilegedExceptionAction<Class<?>>() {
                    public Class<?> run() throws ClassNotFoundException {
                        String path = name.replace('.', '/').concat(".class");
                        Resource res = ucp.getResource(path, false);
                        if (res != null) {
                            try {
                                return defineClass(name, res);
                            } catch (IOException e) {
                                throw new ClassNotFoundException(name, e);
                            } catch (ClassFormatError e2) {
                                if (res.getDataError() != null) {
                                    e2.addSuppressed(res.getDataError());
                                }
                                throw e2;
                            }
                        } else {
                            return null;
                        }
                    }
                }, acc);
        } catch (java.security.PrivilegedActionException pae) {
            throw (ClassNotFoundException) pae.getException();
        }
        if (result == null) {
            throw new ClassNotFoundException(name);
        }
        return result;
    }


                /*
     * 使用指定的包名检索包。
     * 如果非空，则使用指定的代码源和清单验证包。
     */
    private Package getAndVerifyPackage(String pkgname,
                                        Manifest man, URL url) {
        Package pkg = getPackage(pkgname);
        if (pkg != null) {
            // 找到包，因此检查包的密封性。
            if (pkg.isSealed()) {
                // 验证代码源URL是否相同。
                if (!pkg.isSealed(url)) {
                    throw new SecurityException(
                        "密封性违规: 包 " + pkgname + " 已密封");
                }
            } else {
                // 确保我们不会尝试在此代码源URL上密封包。
                if ((man != null) && isSealed(pkgname, man)) {
                    throw new SecurityException(
                        "密封性违规: 无法密封包 " + pkgname +
                        ": 已加载");
                }
            }
        }
        return pkg;
    }

    // 也由VM调用，为从CDS存档加载的类定义包
    private void definePackageInternal(String pkgname, Manifest man, URL url)
    {
        if (getAndVerifyPackage(pkgname, man, url) == null) {
            try {
                if (man != null) {
                    definePackage(pkgname, man, url);
                } else {
                    definePackage(pkgname, null, null, null, null, null, null, null);
                }
            } catch (IllegalArgumentException iae) {
                // 并发类加载器：在竞争条件下重新验证
                if (getAndVerifyPackage(pkgname, man, url) == null) {
                    // 不应发生
                    throw new AssertionError("无法找到包 " +
                                             pkgname);
                }
            }
        }
    }

    /*
     * 使用从指定资源获取的类字节定义一个类。在类可以使用之前，必须解析该类。
     */
    private Class<?> defineClass(String name, Resource res) throws IOException {
        long t0 = System.nanoTime();
        int i = name.lastIndexOf('.');
        URL url = res.getCodeSourceURL();
        if (i != -1) {
            String pkgname = name.substring(0, i);
            // 检查包是否已加载。
            Manifest man = res.getManifest();
            definePackageInternal(pkgname, man, url);
        }
        // 现在读取类字节并定义类
        java.nio.ByteBuffer bb = res.getByteBuffer();
        if (bb != null) {
            // 使用（直接）ByteBuffer：
            CodeSigner[] signers = res.getCodeSigners();
            CodeSource cs = new CodeSource(url, signers);
            sun.misc.PerfCounter.getReadClassBytesTime().addElapsedTimeFrom(t0);
            return defineClass(name, bb, cs);
        } else {
            byte[] b = res.getBytes();
            // 必须在读取字节后读取证书。
            CodeSigner[] signers = res.getCodeSigners();
            CodeSource cs = new CodeSource(url, signers);
            sun.misc.PerfCounter.getReadClassBytesTime().addElapsedTimeFrom(t0);
            return defineClass(name, b, 0, b.length, cs);
        }
    }

    /**
     * 在此类加载器中通过名称定义一个新包。指定清单中的属性将用于获取包的版本和密封信息。对于密封的包，额外的URL指定了加载包的代码源URL。
     *
     * @param name  包名
     * @param man   包含包版本和密封信息的清单
     * @param url   包的代码源URL，或如果无则为null
     * @exception   IllegalArgumentException 如果包名重复了此类加载器或其祖先中的现有包
     * @return 新定义的Package对象
     */
    protected Package definePackage(String name, Manifest man, URL url)
        throws IllegalArgumentException
    {
        String specTitle = null, specVersion = null, specVendor = null;
        String implTitle = null, implVersion = null, implVendor = null;
        String sealed = null;
        URL sealBase = null;

        Attributes attr = SharedSecrets.javaUtilJarAccess()
                .getTrustedAttributes(man, name.replace('.', '/').concat("/"));
        if (attr != null) {
            specTitle   = attr.getValue(Name.SPECIFICATION_TITLE);
            specVersion = attr.getValue(Name.SPECIFICATION_VERSION);
            specVendor  = attr.getValue(Name.SPECIFICATION_VENDOR);
            implTitle   = attr.getValue(Name.IMPLEMENTATION_TITLE);
            implVersion = attr.getValue(Name.IMPLEMENTATION_VERSION);
            implVendor  = attr.getValue(Name.IMPLEMENTATION_VENDOR);
            sealed      = attr.getValue(Name.SEALED);
        }
        attr = man.getMainAttributes();
        if (attr != null) {
            if (specTitle == null) {
                specTitle = attr.getValue(Name.SPECIFICATION_TITLE);
            }
            if (specVersion == null) {
                specVersion = attr.getValue(Name.SPECIFICATION_VERSION);
            }
            if (specVendor == null) {
                specVendor = attr.getValue(Name.SPECIFICATION_VENDOR);
            }
            if (implTitle == null) {
                implTitle = attr.getValue(Name.IMPLEMENTATION_TITLE);
            }
            if (implVersion == null) {
                implVersion = attr.getValue(Name.IMPLEMENTATION_VERSION);
            }
            if (implVendor == null) {
                implVendor = attr.getValue(Name.IMPLEMENTATION_VENDOR);
            }
            if (sealed == null) {
                sealed = attr.getValue(Name.SEALED);
            }
        }
        if ("true".equalsIgnoreCase(sealed)) {
            sealBase = url;
        }
        return definePackage(name, specTitle, specVersion, specVendor,
                             implTitle, implVersion, implVendor, sealBase);
    }


                /*
     * 如果根据给定的清单，指定的包名是密封的，则返回 true。
     *
     * @throws SecurityException 如果包名在清单中不受信任
     */
    private boolean isSealed(String name, Manifest man) {
        Attributes attr = SharedSecrets.javaUtilJarAccess()
                .getTrustedAttributes(man, name.replace('.', '/').concat("/"));
        String sealed = null;
        if (attr != null) {
            sealed = attr.getValue(Name.SEALED);
        }
        if (sealed == null) {
            if ((attr = man.getMainAttributes()) != null) {
                sealed = attr.getValue(Name.SEALED);
            }
        }
        return "true".equalsIgnoreCase(sealed);
    }

    /**
     * 在 URL 搜索路径上查找具有指定名称的资源。
     *
     * @param name 资源的名称
     * @return 资源的 {@code URL}，如果找不到资源或加载器已关闭，则返回 {@code null}。
     */
    public URL findResource(final String name) {
        /*
         * 查找类的相同限制也适用于资源
         */
        URL url = AccessController.doPrivileged(
            new PrivilegedAction<URL>() {
                public URL run() {
                    return ucp.findResource(name, true);
                }
            }, acc);

        return url != null ? ucp.checkURL(url) : null;
    }

    /**
     * 返回一个枚举，表示 URL 搜索路径上具有指定名称的所有资源的 URL。
     *
     * @param name 资源名称
     * @exception IOException 如果发生 I/O 异常
     * @return 一个 {@code Enumeration} 的 {@code URL}。
     *         如果加载器已关闭，枚举将为空。
     */
    public Enumeration<URL> findResources(final String name)
        throws IOException
    {
        final Enumeration<URL> e = ucp.findResources(name, true);

        return new Enumeration<URL>() {
            private URL url = null;

            private boolean next() {
                if (url != null) {
                    return true;
                }
                do {
                    URL u = AccessController.doPrivileged(
                        new PrivilegedAction<URL>() {
                            public URL run() {
                                if (!e.hasMoreElements())
                                    return null;
                                return e.nextElement();
                            }
                        }, acc);
                    if (u == null)
                        break;
                    url = ucp.checkURL(u);
                } while (url == null);
                return url != null;
            }

            public URL nextElement() {
                if (!next()) {
                    throw new NoSuchElementException();
                }
                URL u = url;
                url = null;
                return u;
            }

            public boolean hasMoreElements() {
                return next();
            }
        };
    }

    /**
     * 返回给定代码源对象的权限。
     * 该方法的实现首先调用 super.getPermissions，然后根据代码源的 URL 添加权限。
     * <p>
     * 如果此 URL 的协议是 "jar"，则授予的权限基于 Jar 文件 URL 所需的权限。
     * <p>
     * 如果协议是 "file" 并且有权威组件，则可能授予连接到和接受来自该权威的连接的权限。如果协议是 "file"
     * 并且路径指定一个文件，则授予读取该文件的权限。如果协议是 "file" 并且路径是一个目录，则授予读取该目录中
     * 所有文件和（递归地）所有文件和子目录的权限。
     * <p>
     * 如果协议不是 "file"，则授予连接到和接受来自 URL 主机的连接的权限。
     * @param codesource 代码源
     * @exception NullPointerException 如果 {@code codesource} 为 {@code null}。
     * @return 授予代码源的权限
     */
    protected PermissionCollection getPermissions(CodeSource codesource)
    {
        PermissionCollection perms = super.getPermissions(codesource);

        URL url = codesource.getLocation();

        Permission p;
        URLConnection urlConnection;

        try {
            urlConnection = url.openConnection();
            p = urlConnection.getPermission();
        } catch (java.io.IOException ioe) {
            p = null;
            urlConnection = null;
        }

        if (p instanceof FilePermission) {
            // 如果权限在末尾有分隔符字符，
            // 则意味着代码库是一个目录，我们需要
            // 添加一个额外的权限以递归读取
            String path = p.getName();
            if (path.endsWith(File.separator)) {
                path += "-";
                p = new FilePermission(path, SecurityConstants.FILE_READ_ACTION);
            }
        } else if ((p == null) && (url.getProtocol().equals("file"))) {
            String path = url.getFile().replace('/', File.separatorChar);
            path = ParseUtil.decode(path);
            if (path.endsWith(File.separator))
                path += "-";
            p =  new FilePermission(path, SecurityConstants.FILE_READ_ACTION);
        } else {
            /**
             * 如果不是从 'file:' URL 加载，我们希望在确保主机正确且有效后，
             * 授予类连接到和接受来自远程主机的权限。
             */
            URL locUrl = url;
            if (urlConnection instanceof JarURLConnection) {
                locUrl = ((JarURLConnection)urlConnection).getJarFileURL();
            }
            String host = locUrl.getHost();
            if (host != null && !host.isEmpty())
                p = new SocketPermission(host,
                                         SecurityConstants.SOCKET_CONNECT_ACCEPT_ACTION);
        }


                    // 确保创建此类加载器的人
        // 具有此权限

        if (p != null) {
            final SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                final Permission fp = p;
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    public Void run() throws SecurityException {
                        sm.checkPermission(fp);
                        return null;
                    }
                }, acc);
            }
            perms.add(p);
        }
        return perms;
    }

    /**
     * 为指定的URL和父类加载器创建一个新的URLClassLoader实例。如果安装了安全管理者，
     * 则此方法返回的URLClassLoader的{@code loadClass}方法将在加载类之前调用
     * {@code SecurityManager.checkPackageAccess}方法。
     *
     * @param urls 用于查找类和资源的URL
     * @param parent 委托的父类加载器
     * @exception  NullPointerException 如果 {@code urls} 为 {@code null}。
     * @return 结果类加载器
     */
    public static URLClassLoader newInstance(final URL[] urls,
                                             final ClassLoader parent) {
        // 保存调用者的上下文
        final AccessControlContext acc = AccessController.getContext();
        // 需要一个特权块来创建类加载器
        URLClassLoader ucl = AccessController.doPrivileged(
            new PrivilegedAction<URLClassLoader>() {
                public URLClassLoader run() {
                    return new FactoryURLClassLoader(urls, parent, acc);
                }
            });
        return ucl;
    }

    /**
     * 为指定的URL和默认父类加载器创建一个新的URLClassLoader实例。如果安装了安全管理者，
     * 则此方法返回的URLClassLoader的{@code loadClass}方法将在加载类之前调用
     * {@code SecurityManager.checkPackageAccess}。
     *
     * @param urls 用于查找类和资源的URL
     * @exception  NullPointerException 如果 {@code urls} 为 {@code null}。
     * @return 结果类加载器
     */
    public static URLClassLoader newInstance(final URL[] urls) {
        // 保存调用者的上下文
        final AccessControlContext acc = AccessController.getContext();
        // 需要一个特权块来创建类加载器
        URLClassLoader ucl = AccessController.doPrivileged(
            new PrivilegedAction<URLClassLoader>() {
                public URLClassLoader run() {
                    return new FactoryURLClassLoader(urls, acc);
                }
            });
        return ucl;
    }

    static {
        sun.misc.SharedSecrets.setJavaNetAccess (
            new sun.misc.JavaNetAccess() {
                public URLClassPath getURLClassPath (URLClassLoader u) {
                    return u.ucp;
                }

                public String getOriginalHostName(InetAddress ia) {
                    return ia.holder.getOriginalHostName();
                }
            }
        );
        ClassLoader.registerAsParallelCapable();
    }
}

final class FactoryURLClassLoader extends URLClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    FactoryURLClassLoader(URL[] urls, ClassLoader parent,
                          AccessControlContext acc) {
        super(urls, parent, acc);
    }

    FactoryURLClassLoader(URL[] urls, AccessControlContext acc) {
        super(urls, acc);
    }

    public final Class<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        // 首先检查我们是否有权限访问该包。一旦我们添加了对导出包的支持，这应该被移除。
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            int i = name.lastIndexOf('.');
            if (i != -1) {
                sm.checkPackageAccess(name.substring(0, i));
            }
        }
        return super.loadClass(name, resolve);
    }
}
