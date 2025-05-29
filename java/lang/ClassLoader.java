/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.AccessControlContext;
import java.security.CodeSource;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.Map;
import java.util.Vector;
import java.util.Hashtable;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import sun.misc.CompoundEnumeration;
import sun.misc.Resource;
import sun.misc.URLClassPath;
import sun.misc.VM;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import sun.reflect.misc.ReflectUtil;
import sun.security.util.SecurityConstants;

/**
 * 类加载器是一个负责加载类的对象。类 <tt>ClassLoader</tt> 是一个抽象类。
 * 给定类的 <a href="#name">二进制名称</a>，类加载器应尝试定位或生成构成类定义的数据。
 * 典型的策略是将名称转换为文件名，然后从文件系统中读取该名称的“类文件”。
 *
 * <p> 每个 {@link Class <tt>Class</tt>} 对象都包含一个指向定义它的 <tt>ClassLoader</tt> 的
 * {@link Class#getClassLoader() 引用}。
 *
 * <p> 数组类的 <tt>Class</tt> 对象不是由类加载器创建的，而是由 Java 运行时根据需要自动创建。
 * 数组类的类加载器，由 {@link Class#getClassLoader()} 返回，与其元素类型的类加载器相同；
 * 如果元素类型是基本类型，则数组类没有类加载器。
 *
 * <p> 应用程序通过实现 <tt>ClassLoader</tt> 的子类来扩展 Java 虚拟机动态加载类的方式。
 *
 * <p> 类加载器通常可被安全管理器用来指示安全域。
 *
 * <p> <tt>ClassLoader</tt> 类使用委托模型来搜索类和资源。每个 <tt>ClassLoader</tt> 实例
 * 都有一个关联的父类加载器。当被请求查找类或资源时，<tt>ClassLoader</tt> 实例会在尝试
 * 自己查找类或资源之前，将搜索类或资源的任务委托给其父类加载器。虚拟机内置的类加载器，
 * 称为“引导类加载器”，本身没有父类加载器，但可以作为 <tt>ClassLoader</tt> 实例的父类。
 *
 * <p> 支持并发加载类的类加载器被称为 <em>并行能力</em> 类加载器，并且需要在类初始化时通过
 * 调用 {@link #registerAsParallelCapable <tt>ClassLoader.registerAsParallelCapable</tt>} 方法
 * 注册自己。请注意，<tt>ClassLoader</tt> 类默认注册为并行能力类加载器。然而，其子类如果具有
 * 并行能力，仍然需要自行注册。<br>
 * 在委托模型不是严格层次结构的环境中，类加载器需要具备并行能力，否则类加载可能导致死锁，
 * 因为在类加载过程中会持有加载器锁（参见 {@link #loadClass <tt>loadClass</tt>} 方法）。
 *
 * <p> 通常，Java 虚拟机以平台相关的方式从本地文件系统加载类。例如，在 UNIX 系统上，
 * 虚拟机从由 <tt>CLASSPATH</tt> 环境变量定义的目录中加载类。
 *
 * <p> 然而，一些类可能并非源自文件；它们可能来自其他来源，如网络，或者由应用程序构造。
 * 方法 {@link #defineClass(String, byte[], int, int) <tt>defineClass</tt>} 将字节数组转换为
 * <tt>Class</tt> 类的实例。可以使用 {@link Class#newInstance <tt>Class.newInstance</tt>}
 * 创建此新定义类的实例。
 *
 * <p> 由类加载器创建的对象的构造方法和方法可能引用其他类。为了确定引用的类，Java 虚拟机会
 * 调用最初创建该类的类加载器的 {@link #loadClass <tt>loadClass</tt>} 方法。
 *
 * <p> 例如，应用程序可以创建一个网络类加载器从服务器下载类文件。示例代码可能如下：
 *
 * <blockquote><pre>
 *   ClassLoader loader = new NetworkClassLoader(host, port);
 *   Object main = loader.loadClass("Main", true).newInstance();
 *        . . .
 * </pre></blockquote>
 *
 * <p> 网络类加载器子类必须定义方法 {@link #findClass <tt>findClass</tt>} 和 <tt>loadClassData</tt>
 * 以从网络加载类。一旦下载了构成类的字节，它应使用方法 {@link #defineClass <tt>defineClass</tt>}
 * 创建类实例。示例实现如下：
 *
 * <blockquote><pre>
 *     class NetworkClassLoader extends ClassLoader {
 *         String host;
 *         int port;
 *
 *         public Class findClass(String name) {
 *             byte[] b = loadClassData(name);
 *             return defineClass(name, b, 0, b.length);
 *         }
 *
 *         private byte[] loadClassData(String name) {
 *             // 从连接加载类数据
 *              . . .
 *         }
 *     }
 * </pre></blockquote>
 *
 * <h3> <a name="name">二进制名称</a> </h3>
 *
 * <p> 提供给 <tt>ClassLoader</tt> 方法的任何类名称作为 {@link String} 参数，
 * 必须是 <cite>The Java™ Language Specification</cite> 定义的二进制名称。
 *
 * <p> 有效类名称的示例包括：
 * <blockquote><pre>
 *   "java.lang.String"
 *   "javax.swing.JSpinner$DefaultEditor"
 *   "java.security.KeyStore$Builder$FileBuilder$1"
 *   "java.net.URLClassLoader$3$1"
 * </pre></blockquote>
 *
 * @see      #resolveClass(Class)
 * @since 1.0
 */
public abstract class ClassLoader {

    private static native void registerNatives();
    static {
        registerNatives();
    }

    // 用于委托的父类加载器
    // 注意：虚拟机硬编码了此字段的偏移量，因此所有新字段必须在此字段之后添加。
    private final ClassLoader parent;

    /**
     * 封装支持并行加载的加载器类型集合。
     */
    private static class ParallelLoaders {
        private ParallelLoaders() {}

        // 并行能力加载器类型的集合
        private static final Set<Class<? extends ClassLoader>> loaderTypes =
            Collections.newSetFromMap(
                new WeakHashMap<Class<? extends ClassLoader>, Boolean>());
        static {
            synchronized (loaderTypes) { loaderTypes.add(ClassLoader.class); }
        }

        /**
         * 将给定的类加载器类型注册为具有并行能力。
         * 如果注册成功，返回 {@code true}；如果加载器的超类未注册，返回 {@code false}。
         */
        static boolean register(Class<? extends ClassLoader> c) {
            synchronized (loaderTypes) {
                if (loaderTypes.contains(c.getSuperclass())) {
                    // 如果且仅当所有超类都具有并行能力时，注册类加载器为并行能力。
                    // 注意：根据当前的类加载顺序，如果直接超类具有并行能力，
                    // 则更高层次的所有超类也必须具有并行能力。
                    loaderTypes.add(c);
                    return true;
                } else {
                    return false;
                }
            }
        }

        /**
         * 如果给定的类加载器类型已注册为具有并行能力，返回 {@code true}。
         */
        static boolean isRegistered(Class<? extends ClassLoader> c) {
            synchronized (loaderTypes) {
                return loaderTypes.contains(c);
            }
        }
    }

    // 当当前类加载器具有并行能力时，将类名称映射到对应的锁对象。
    // 注意：虚拟机也使用此字段来判断当前类加载器是否具有并行能力，
    // 并确定类加载的适当锁对象。
    private final ConcurrentHashMap<String, Object> parallelLockMap;

    // 将包映射到证书的哈希表
    private final Map <String, Certificate[]> package2certs;

    // 所有未签名类的包共享
    private static final Certificate[] nocerts = new Certificate[0];

    // 此类加载器加载的类。此表的唯一目的是防止类在加载器被垃圾回收之前被垃圾回收。
    private final Vector<Class<?>> classes = new Vector<>();

    // “默认”域。设置为新创建类的默认 ProtectionDomain。
    private final ProtectionDomain defaultDomain =
        new ProtectionDomain(new CodeSource(null, (Certificate[]) null),
                             null, this, null);

    // 由虚拟机调用，以记录使用此加载器加载的每个类。
    void addClass(Class<?> c) {
        classes.addElement(c);
    }

    // 在此类加载器中定义的包。每个包名称映射到其对应的 Package 对象。
    // @GuardedBy("itself")
    private final HashMap<String, Package> packages = new HashMap<>();

    private static Void checkCreateClassLoader() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        return null;
    }

    private ClassLoader(Void unused, ClassLoader parent) {
        this.parent = parent;
        if (ParallelLoaders.isRegistered(this.getClass())) {
            parallelLockMap = new ConcurrentHashMap<>();
            package2certs = new ConcurrentHashMap<>();
            assertionLock = new Object();
        } else {
            // 没有更细粒度的锁；锁定类加载器实例
            parallelLockMap = null;
            package2certs = new Hashtable<>();
            assertionLock = this;
        }
    }

    /**
     * 使用指定的父类加载器进行委托，创建一个新的类加载器。
     *
     * <p> 如果存在安全管理器，会调用其 {@link SecurityManager#checkCreateClassLoader()
     * <tt>checkCreateClassLoader</tt>} 方法。这可能导致安全异常。 </p>
     *
     * @param  parent
     *         父类加载器
     *
     * @throws  SecurityException
     *          如果存在安全管理器，并且其 <tt>checkCreateClassLoader</tt> 方法
     *          不允许创建新的类加载器。
     *
     * @since  1.2
     */
    protected ClassLoader(ClassLoader parent) {
        this(checkCreateClassLoader(), parent);
    }

    /**
     * 使用由 {@link #getSystemClassLoader() <tt>getSystemClassLoader()</tt>} 方法返回的
     * <tt>ClassLoader</tt> 作为父类加载器，创建一个新的类加载器。
     *
     * <p> 如果存在安全管理器，会调用其 {@link SecurityManager#checkCreateClassLoader()
     * <tt>checkCreateClassLoader</tt>} 方法。这可能导致安全异常。 </p>
     *
     * @throws  SecurityException
     *          如果存在安全管理器，并且其 <tt>checkCreateClassLoader</tt> 方法
     *          不允许创建新的类加载器。
     */
    protected ClassLoader() {
        this(checkCreateClassLoader(), getSystemClassLoader());
    }

    // -- 类 --

    /**
     * 加载具有指定 <a href="#name">二进制名称</a> 的类。
     * 此方法以与 {@link #loadClass(String, boolean)} 方法相同的方式搜索类。
     * 它由 Java 虚拟机调用以解析类引用。调用此方法等效于调用
     * {@link #loadClass(String, boolean) <tt>loadClass(name, false)</tt>}。
     *
     * @param  name
     *         类的 <a href="#name">二进制名称</a>
     *
     * @return  结果 <tt>Class</tt> 对象
     *
     * @throws  ClassNotFoundException
     *          如果未找到类
     */
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    /**
     * 加载具有指定 <a href="#name">二进制名称</a> 的类。此方法的默认实现按以下顺序搜索类：
     *
     * <ol>
     *
     *   <li><p> 调用 {@link #findLoadedClass(String)} 检查类是否已被加载。 </p></li>
     *
     *   <li><p> 在父类加载器上调用 {@link #loadClass(String) <tt>loadClass</tt>} 方法。
     *   如果父类加载器为 <tt>null</tt>，则使用虚拟机内置的类加载器。 </p></li>
     *
     *   <li><p> 调用 {@link #findClass(String)} 方法查找类。 </p></li>
     *
     * </ol>
     *
     * <p> 如果通过上述步骤找到类，并且 <tt>resolve</tt> 标志为 true，
     * 则此方法将对结果 <tt>Class</tt> 对象调用 {@link #resolveClass(Class)} 方法。
     *
     * <p> 鼓励 <tt>ClassLoader</tt> 的子类重写 {@link #findClass(String)}，而不是此方法。 </p>
     *
     * <p> 除非被重写，此方法在整个类加载过程中会对 {@link #getClassLoadingLock
     * <tt>getClassLoadingLock</tt>} 方法的结果进行同步。
     *
     * @param  name
     *         类的 <a href="#name">二进制名称</a>
     *
     * @param  resolve
     *         如果为 <tt>true</tt>，则解析类
     *
     * @return  结果 <tt>Class</tt> 对象
     *
     * @throws  ClassNotFoundException
     *          如果未找到类
     */
    protected Class<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        synchronized (getClassLoadingLock(name)) {
            // 首先，检查类是否已被加载
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                long t0 = System.nanoTime();
                try {
                    if (parent != null) {
                        c = parent.loadClass(name, false);
                    } else {
                        c = findBootstrapClassOrNull(name);
                    }
                } catch (ClassNotFoundException e) {
                    // 如果从非空的父类加载器未找到类，则抛出 ClassNotFoundException
                }

                if (c == null) {
                    // 如果仍未找到，则调用 findClass 以查找类。
                    long t1 = System.nanoTime();
                    c = findClass(name);

                    // 这是定义类加载器；记录统计信息
                    sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
                    sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
                    sun.misc.PerfCounter.getFindClasses().increment();
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }

    /**
     * 返回用于类加载操作的锁对象。
     * 为了向后兼容，此方法的默认实现行为如下。如果此 ClassLoader 对象注册为具有并行能力，
     * 则该方法返回与指定类名称关联的专用对象。否则，该方法返回此 ClassLoader 对象。
     *
     * @param  className
     *         要加载的类名称
     *
     * @return  用于类加载操作的锁
     *
     * @throws NullPointerException
     *         如果注册为具有并行能力且 <tt>className</tt> 为 null
     *
     * @see #loadClass(String, boolean)
     *
     * @since  1.7
     */
    protected Object getClassLoadingLock(String className) {
        Object lock = this;
        if (parallelLockMap != null) {
            Object newLock = new Object();
            lock = parallelLockMap.putIfAbsent(className, newLock);
            if (lock == null) {
                lock = newLock;
            }
        }
        return lock;
    }

    // 由虚拟机调用以加载类。
    private Class<?> loadClassInternal(String name)
        throws ClassNotFoundException
    {
        // 为了向后兼容，当当前类加载器不具有并行能力时，显式锁定 'this'。
        if (parallelLockMap == null) {
            synchronized (this) {
                 return loadClass(name);
            }
        } else {
            return loadClass(name);
        }
    }

    // 在虚拟机使用此加载器加载类后调用。
    private void checkPackageAccess(Class<?> cls, ProtectionDomain pd) {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (ReflectUtil.isNonPublicProxyClass(cls)) {
                for (Class<?> intf: cls.getInterfaces()) {
                    checkPackageAccess(intf, pd);
                }
                return;
            }

            final String name = cls.getName();
            final int i = name.lastIndexOf('.');
            if (i != -1) {
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    public Void run() {
                        sm.checkPackageAccess(name.substring(0, i));
                        return null;
                    }
                }, new AccessControlContext(new ProtectionDomain[] {pd}));
            }
        }
    }

    /**
     * 查找具有指定 <a href="#name">二进制名称</a> 的类。
     * 遵循加载类委托模型的类加载器实现应重写此方法，
     * 并且在检查父类加载器以查找请求的类后，由 {@link #loadClass <tt>loadClass</tt>} 方法调用。
     * 默认实现抛出 <tt>ClassNotFoundException</tt>。
     *
     * @param  name
     *         类的 <a href="#name">二进制名称</a>
     *
     * @return  结果 <tt>Class</tt> 对象
     *
     * @throws  ClassNotFoundException
     *          如果未找到类
     *
     * @since  1.2
     */
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        throw new ClassNotFoundException(name);
    }

    /**
     * 将字节数组转换为 <tt>Class</tt> 类的实例。
     * 在使用 <tt>Class</tt> 之前，必须对其进行解析。
     * 此方法已被弃用，推荐使用接受 <a href="#name">二进制名称</a> 作为第一个参数的版本，
     * 它更安全。
     *
     * @param  b
     *         构成类数据的字节。从 <tt>off</tt> 到 <tt>off+len-1</tt> 的字节
     *         应具有 <cite>The Java™ Virtual Machine Specification</cite> 定义的有效类文件格式。
     *
     * @param  off
     *         类数据在 <tt>b</tt> 中的起始偏移量
     *
     * @param  len
     *         类数据的长度
     *
     * @return  从指定类数据创建的 <tt>Class</tt> 对象
     *
     * @throws  ClassFormatError
     *          如果数据不包含有效类
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>off</tt> 或 <tt>len</tt> 为负数，或者
     *          <tt>off+len</tt> 大于 <tt>b.length</tt>。
     *
     * @throws  SecurityException
     *          如果尝试将此类添加到包含由不同证书集签名的类的包中，
     *          或者尝试在完全限定名称以“{@code java.}”开头的包中定义类。
     *
     * @see  #loadClass(String, boolean)
     * @see  #resolveClass(Class)
     *
     * @deprecated  由 {@link #defineClass(String, byte[], int, int)
     * defineClass(String, byte[], int, int)} 替换
     */
    @Deprecated
    protected final Class<?> defineClass(byte[] b, int off, int len)
        throws ClassFormatError
    {
        return defineClass(null, b, off, len, null);
    }

    /**
     * 将字节数组转换为 <tt>Class</tt> 类的实例。
     * 在使用 <tt>Class</tt> 之前，必须对其进行解析。
     *
     * <p> 此方法为新定义的类分配一个默认的 {@link java.security.ProtectionDomain
     * <tt>ProtectionDomain</tt>}。该 <tt>ProtectionDomain</tt> 实际上被授予与调用
     * {@link java.security.Policy#getPermissions(java.security.CodeSource)
     * <tt>Policy.getPolicy().getPermissions(new CodeSource(null, null))</tt>} 时返回的相同权限集。
     * 默认域在第一次调用 {@link #defineClass(String, byte[], int, int) <tt>defineClass</tt>} 时创建，
     * 并在后续调用中重用。
     *
     * <p> 要为类分配特定的 <tt>ProtectionDomain</tt>，请使用接受 <tt>ProtectionDomain</tt> 作为
     * 参数之一的 {@link #defineClass(String, byte[], int, int, java.security.ProtectionDomain)
     * <tt>defineClass</tt>} 方法。 </p>
     *
     * @param  name
     *         类的预期 <a href="#name">二进制名称</a>，如果未知则为 <tt>null</tt>
     *
     * @param  b
     *         构成类数据的字节。从 <tt>off</tt> 到 <tt>off+len-1</tt> 的字节
     *         应具有 <cite>The Java™ Virtual Machine Specification</cite> 定义的有效类文件格式。
     *
     * @param  off
     *         类数据在 <tt>b</tt> 中的起始偏移量
     *
     * @param  len
     *         类数据的长度
     *
     * @return  从指定类数据创建的 <tt>Class</tt> 对象。
     *
     * @throws  ClassFormatError
     *          如果数据不包含有效类
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>off</tt> 或 <tt>len</tt> 为负数，或者
     *          <tt>off+len</tt> 大于 <tt>b.length</tt>。
     *
     * @throws  SecurityException
     *          如果尝试将此类添加到包含由不同证书集签名的类的包中（该类未签名），
     *          或者 <tt>name</tt> 以“<tt>java.</tt>”开头。
     *
     * @see  #loadClass(String, boolean)
     * @see  #resolveClass(Class)
     * @see  java.security.CodeSource
     * @see  java.security.SecureClassLoader
     *
     * @since  1.1
     */
    protected final Class<?> defineClass(String name, byte[] b, int off, int len)
        throws ClassFormatError
    {
        return defineClass(name, b, off, len, null);
    }

    /* 确定保护域，并检查：
        - 不定义 java.* 类，
        - 此类的签名者与包中其他类的签名者匹配。
    */
    private ProtectionDomain preDefineClass(String name,
                                            ProtectionDomain pd)
    {
        if (!checkName(name))
            throw new NoClassDefFoundError("IllegalName: " + name);

        // 注意：java.lang.invoke.MemberName.checkForTypeAlias 中的检查逻辑
        // 依赖于如果类名称形如“java.*”，则无法进行伪造
        if ((name != null) && name.startsWith("java.")) {
            throw new SecurityException
                ("Prohibited package name: " +
                 name.substring(0, name.lastIndexOf('.')));
        }
        if (pd == null) {
            pd = defaultDomain;
        }

        if (name != null) checkCerts(name, pd.getCodeSource());

        return pd;
    }

    private String defineClassSourceLocation(ProtectionDomain pd)
    {
        CodeSource cs = pd.getCodeSource();
        String source = null;
        if (cs != null && cs.getLocation() != null) {
            source = cs.getLocation().toString();
        }
        return source;
    }

    private void postDefineClass(Class<?> c, ProtectionDomain pd)
    {
        if (pd.getCodeSource() != null) {
            Certificate certs[] = pd.getCodeSource().getCertificates();
            if (certs != null)
                setSigners(c, certs);
        }
    }

    /**
     * 将字节数组转换为 <tt>Class</tt> 类的实例，带有可选的 <tt>ProtectionDomain</tt>。
     * 如果域为 <tt>null</tt>，则根据 {@link #defineClass(String, byte[], int, int)}
     * 的文档说明为类分配默认域。在使用类之前，必须对其进行解析。
     *
     * <p> 在包中定义的第一个类决定了该包中所有后续定义的类必须包含的确切证书集。
     * 类的证书集从类的 <tt>ProtectionDomain</tt> 中的 {@link java.security.CodeSource
     * <tt>CodeSource</tt>} 获取。添加到该包的任何类必须包含相同的证书集，
     * 否则将抛出 <tt>SecurityException</tt>。请注意，如果 <tt>name</tt> 为 <tt>null</tt>，
     * 则不执行此检查。你应始终传入你正在定义的类的 <a href="#name">二进制名称</a>
     * 以及字节。这确保你定义的类确实是你认为的类。
     *
     * <p> 指定的 <tt>name</tt> 不能以“<tt>java.</tt>”开头，因为“<tt>java.*</tt>”包中的
     * 所有类只能由引导类加载器定义。如果 <tt>name</tt> 不为 <tt>null</tt>，
     * 它必须等于字节数组“<tt>b</tt>”指定的类的 <a href="#name">二进制名称</a>，
     * 否则将抛出 {@link NoClassDefFoundError <tt>NoClassDefFoundError</tt>}。 </p>
     *
     * @param  name
     *         类的预期 <a href="#name">二进制名称</a>，如果未知则为 <tt>null</tt>
     *
     * @param  b
     *         构成类数据的字节。从 <tt>off</tt> 到 <tt>off+len-1</tt> 的字节
     *         应具有 <cite>The Java™ Virtual Machine Specification</cite> 定义的有效类文件格式。
     *
     * @param  off
     *         类数据在 <tt>b</tt> 中的起始偏移量
     *
     * @param  len
     *         类数据的长度
     *
     * @param  protectionDomain
     *         类的 ProtectionDomain
     *
     * @return  从数据和可选的 <tt>ProtectionDomain</tt> 创建的 <tt>Class</tt> 对象。
     *
     * @throws  ClassFormatError
     *          如果数据不包含有效类
     *
     * @throws  NoClassDefFoundError
     *          如果 <tt>name</tt> 不等于 <tt>b</tt> 指定的类的 <a href="#name">二进制名称</a>
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>off</tt> 或 <tt>len</tt> 为负数，或者
     *          <tt>off+len</tt> 大于 <tt>b.length</tt>。
     *
     * @throws  SecurityException
     *          如果尝试将此类添加到包含由不同证书集签名的类的包中，
     *          或者 <tt>name</tt> 以“<tt>java.</tt>”开头。
     */
    protected final Class<?> defineClass(String name, byte[] b, int off, int len,
                                         ProtectionDomain protectionDomain)
        throws ClassFormatError
    {
        protectionDomain = preDefineClass(name, protectionDomain);
        String source = defineClassSourceLocation(protectionDomain);
        Class<?> c = defineClass1(name, b, off, len, protectionDomain, source);
        postDefineClass(c, protectionDomain);
        return c;
    }

    /**
     * 将 {@link java.nio.ByteBuffer <tt>ByteBuffer</tt>} 转换为 <tt>Class</tt> 类的实例，
     * 带有可选的 <tt>ProtectionDomain</tt>。如果域为 <tt>null</tt>，
     * 则根据 {@link #defineClass(String, byte[], int, int)} 的文档说明为类分配默认域。
     * 在使用类之前，必须对其进行解析。
     *
     * <p> 关于包中定义的第一个类决定包的证书集的规则，以及对类名称的限制，
     * 与 {@link #defineClass(String, byte[], int, int, ProtectionDomain)} 的文档说明相同。
     *
     * <p> 以 <i>cl</i><tt>.defineClass(</tt><i>name</i><tt>,</tt>
     * <i>bBuffer</i><tt>,</tt> <i>pd</i><tt>)</tt> 形式调用此方法的结果与以下语句完全相同：
     *
     *<p> <tt>
     * ...<br>
     * byte[] temp = new byte[bBuffer.{@link java.nio.ByteBuffer#remaining remaining}()];<br>
     *     bBuffer.{@link java.nio.ByteBuffer#get(byte[]) get}(temp);<br>
     *     return {@link #defineClass(String, byte[], int, int, ProtectionDomain)
     * cl.defineClass}(name, temp, 0, temp.length, pd);<br>
     * </tt></p>
     *
     * @param  name
     *         类的预期 <a href="#name">二进制名称</a>，如果未知则为 <tt>null</tt>
     *
     * @param  b
     *         构成类数据的字节。从 <tt>b.position()</tt> 到
     *         <tt>b.position() + b.limit() -1</tt> 的字节应具有
     *         <cite>The Java™ Virtual Machine Specification</cite> 定义的有效类文件格式。
     *
     * @param  protectionDomain
     *         类的 ProtectionDomain，或 <tt>null</tt>。
     *
     * @return  从数据和可选的 <tt>ProtectionDomain</tt> 创建的 <tt>Class</tt> 对象。
     *
     * @throws  ClassFormatError
     *          如果数据不包含有效类。
     *
     * @throws  NoClassDefFoundError
     *          如果 <tt>name</tt> 不等于 <tt>b</tt> 指定的类的 <a href="#name">二进制名称</a>
     *
     * @throws  SecurityException
     *          如果尝试将此类添加到包含由不同证书集签名的类的包中，
     *          或者 <tt>name</tt> 以“<tt>java.</tt>”开头。
     *
     * @see      #defineClass(String, byte[], int, int, ProtectionDomain)
     *
     * @since  1.5
     */
    protected final Class<?> defineClass(String name, java.nio.ByteBuffer b,
                                         ProtectionDomain protectionDomain)
        throws ClassFormatError
    {
        int len = b.remaining();

        // 如果不是直接 ByteBuffer，则使用 byte[]：
        if (!b.isDirect()) {
            if (b.hasArray()) {
                return defineClass(name, b.array(),
                                   b.position() + b.arrayOffset(), len,
                                   protectionDomain);
            } else {
                // 无数组，或只读数组
                byte[] tb = new byte[len];
                b.get(tb);  // 从字节缓冲区获取字节。
                return defineClass(name, tb, 0, len, protectionDomain);
            }
        }

        protectionDomain = preDefineClass(name, protectionDomain);
        String source = defineClassSourceLocation(protectionDomain);
        Class<?> c = defineClass2(name, b, b.position(), len, protectionDomain, source);
        postDefineClass(c, protectionDomain);
        return c;
    }

    private native Class<?> defineClass0(String name, byte[] b, int off, int len,
                                         ProtectionDomain pd);

    private native Class<?> defineClass1(String name, byte[] b, int off, int len,
                                         ProtectionDomain pd, String source);

    private native Class<?> defineClass2(String name, java.nio.ByteBuffer b,
                                         int off, int len, ProtectionDomain pd,
                                         String source);

    // 如果名称为 null 或可能是有效二进制名称，返回 true
    private boolean checkName(String name) {
        if ((name == null) || (name.isEmpty()))
            return true;
        if ((name.indexOf('/') != -1)
            || (!VM.allowArraySyntax() && (name.charAt(0) == '[')))
            return false;
        return true;
    }

    private void checkCerts(String name, CodeSource cs) {
        int i = name.lastIndexOf('.');
        String pname = (i == -1) ? "" : name.substring(0, i);

        Certificate[] certs = null;
        if (cs != null) {
            certs = cs.getCertificates();
        }
        Certificate[] pcerts = null;
        if (parallelLockMap == null) {
            synchronized (this) {
                pcerts = package2certs.get(pname);
                if (pcerts == null) {
                    package2certs.put(pname, (certs == null? nocerts:certs));
                }
            }
        } else {
            pcerts = ((ConcurrentHashMap<String, Certificate[]>)package2certs).
                putIfAbsent(pname, (certs == null? nocerts:certs));
        }
        if (pcerts != null && !compareCerts(pcerts, certs)) {
            throw new SecurityException("class \""+ name +
                 "\"'s signer information does not match signer information of other classes in the same package");
        }
    }

    /**
     * 检查以确保新类（certs）的证书与包中插入的第一个类的证书（pcerts）相同。
     */
    private boolean compareCerts(Certificate[] pcerts,
                                 Certificate[] certs)
    {
        // certs 可以为 null，表示没有证书。
        if ((certs == null) || (certs.length == 0)) {
            return pcerts.length == 0;
        }

        // 此时长度必须相同
        if (certs.length != pcerts.length)
            return false;

        // 遍历并确保一个数组中的所有证书都在另一个数组中，反之亦然。
        boolean match;
        for (int i = 0; i < certs.length; i++) {
            match = false;
            for (int j = 0; j < pcerts.length; j++) {
                if (certs[i].equals(pcerts[j])) {
                    match = true;
                    break;
                }
            }
            if (!match) return false;
        }

        // 现在对 pcerts 做同样的事情
        for (int i = 0; i < pcerts.length; i++) {
            match = false;
            for (int j = 0; j < certs.length; j++) {
                if (pcerts[i].equals(certs[j])) {
                    match = true;
                    break;
                }
            }
            if (!match) return false;
        }

        return true;
    }

    /**
     * 链接指定的类。此（名称具有误导性）方法可由类加载器用来链接类。
     * 如果类 <tt>c</tt> 已被链接，则此方法直接返回。否则，
     * 类将按 <cite>The Java™ Language Specification</cite> 的“执行”章节描述进行链接。
     *
     * @param  c
     *         要链接的类
     *
     * @throws  NullPointerException
     *          如果 <tt>c</tt> 为 <tt>null</tt>。
     *
     * @see  #defineClass(String, byte[], int, int)
     */
    protected final void resolveClass(Class<?> c) {
        resolveClass0(c);
    }

    private native void resolveClass0(Class<?> c);

    /**
     * 查找具有指定 <a href="#name">二进制名称</a> 的类，必要时加载它。
     *
     * <p> 此方法通过系统类加载器加载类（参见 {@link #getSystemClassLoader()}）。
     * 返回的 <tt>Class</tt> 对象可能与多个 <tt>ClassLoader</tt> 关联。
     * <tt>ClassLoader</tt> 的子类通常不需要调用此方法，因为大多数类加载器只需要重写
     * {@link #findClass(String)}。 </p>
     *
     * @param  name
     *         类的 <a href="#name">二进制名称</a>
     *
     * @return  指定 <tt>name</tt> 的 <tt>Class</tt> 对象
     *
     * @throws  ClassNotFoundException
     *          如果未找到类
     *
     * @see  #ClassLoader(ClassLoader)
     * @see  #getParent()
     */
    protected final Class<?> findSystemClass(String name)
        throws ClassNotFoundException
    {
        ClassLoader system = getSystemClassLoader();
        if (system == null) {
            if (!checkName(name))
                throw new ClassNotFoundException(name);
            Class<?> cls = findBootstrapClass(name);
            if (cls == null) {
                throw new ClassNotFoundException(name);
            }
            return cls;
        }
        return system.loadClass(name);
    }

    /**
     * 返回由引导类加载器加载的类；
     * 如果未找到，则返回 null。
     */
    private Class<?> findBootstrapClassOrNull(String name)
    {
        if (!checkName(name)) return null;

        return findBootstrapClass(name);
    }

    // 如果未找到，返回 null
    private native Class<?> findBootstrapClass(String name);

    /**
     * 返回具有给定<a href="#name">二进制名称</a>的类，如果此加载器已被Java虚拟机记录为具有该<a href="#name">二进制名称</a>的类的初始化加载器。否则返回<tt>null</tt>。
     *
     * @param  name
     *         类的<a href="#name">二进制名称</a>
     *
     * @return  <tt>Class</tt>对象，如果类尚未加载，则返回<tt>null</tt>
     *
     * @since  1.1
     */
    protected final Class<?> findLoadedClass(String name) {
        if (!checkName(name))
            return null;
        return findLoadedClass0(name);
    }

    private native final Class<?> findLoadedClass0(String name);

    /**
     * 设置类的签名者。此方法应在定义类之后调用。
     *
     * @param  c
     *         <tt>Class</tt>对象
     *
     * @param  signers
     *         类的签名者
     *
     * @since  1.1
     */
    protected final void setSigners(Class<?> c, Object[] signers) {
        c.setSigners(signers);
    }

    // -- 资源 --

    /**
     * 查找具有给定名称的资源。资源是可以通过类代码以独立于代码位置的方式访问的一些数据（图像、音频、文本等）。
     *
     * <p> 资源的名称是一个以'<tt>/</tt>'分隔的路径名，用于标识资源。
     *
     * <p> 此方法将首先在父类加载器中搜索资源；如果父类加载器为<tt>null</tt>，则搜索虚拟机内置的类加载器的路径。如果上述操作失败，此方法将调用{@link #findResource(String)}来查找资源。 </p>
     *
     * @apiNote 覆盖此方法时，建议实现确保任何委托与{@link #getResources(java.lang.String) getResources(String)}方法一致。
     *
     * @param  name
     *         资源名称
     *
     * @return  用于读取资源的<tt>URL</tt>对象，如果资源未找到或调用者没有足够的权限获取资源，则返回<tt>null</tt>。
     *
     * @since  1.1
     */
    public URL getResource(String name) {
        URL url;
        if (parent != null) {
            url = parent.getResource(name);
        } else {
            url = getBootstrapResource(name);
        }
        if (url == null) {
            url = findResource(name);
        }
        return url;
    }

    /**
     * 查找具有给定名称的所有资源。资源是可以通过类代码以独立于代码位置的方式访问的一些数据（图像、音频、文本等）。
     *
     * <p> 资源的名称是一个以<tt>/</tt>分隔的路径名，用于标识资源。
     *
     * <p> 搜索顺序在{@link #getResource(String)}的文档中有描述。 </p>
     *
     * @apiNote 覆盖此方法时，建议实现确保任何委托与{@link #getResource(java.lang.String) getResource(String)}方法一致。这应确保枚举的{@code nextElement}方法返回的第一个元素与{@code getResource(String)}方法返回的资源相同。
     *
     * @param  name
     *         资源名称
     *
     * @return  资源的{@link java.net.URL <tt>URL</tt>}对象的枚举。如果未找到资源，枚举将为空。类加载器无权访问的资源不会包含在枚举中。
     *
     * @throws  IOException
     *          如果发生I/O错误
     *
     * @see  #findResources(String)
     *
     * @since  1.2
     */
    public Enumeration<URL> getResources(String name) throws IOException {
        @SuppressWarnings("unchecked")
        Enumeration<URL>[] tmp = (Enumeration<URL>[]) new Enumeration<?>[2];
        if (parent != null) {
            tmp[0] = parent.getResources(name);
        } else {
            tmp[0] = getBootstrapResources(name);
        }
        tmp[1] = findResources(name);

        return new CompoundEnumeration<>(tmp);
    }

    /**
     * 查找具有给定名称的资源。类加载器实现应覆盖此方法以指定从何处查找资源。
     *
     * @param  name
     *         资源名称
     *
     * @return  用于读取资源的<tt>URL</tt>对象，如果资源未找到，则返回<tt>null</tt>
     *
     * @since  1.2
     */
    protected URL findResource(String name) {
        return null;
    }

    /**
     * 返回表示具有给定名称的所有资源的{@link java.net.URL <tt>URL</tt>}对象的枚举。类加载器实现应覆盖此方法以指定从何处加载资源。
     *
     * @param  name
     *         资源名称
     *
     * @return  资源的{@link java.net.URL <tt>URL</tt>}对象的枚举
     *
     * @throws  IOException
     *          如果发生I/O错误
     *
     * @since  1.2
     */
    protected Enumeration<URL> findResources(String name) throws IOException {
        return java.util.Collections.emptyEnumeration();
    }

    /**
     * 将调用者注册为支持并行加载。注册成功当且仅当满足以下所有条件：
     * <ol>
     * <li> 调用者的实例尚未创建</li>
     * <li> 调用者的所有超类（除了Object类）都已注册为支持并行加载</li>
     * </ol>
     * <p> 请注意，一旦类加载器注册为支持并行加载，就无法更改回不支持状态。</p>
     *
     * @return  如果调用者成功注册为支持并行加载，则返回true，否则返回false。
     *
     * @since   1.7
     */
    @CallerSensitive
    protected static boolean registerAsParallelCapable() {
        Class<? extends ClassLoader> callerClass =
            Reflection.getCallerClass().asSubclass(ClassLoader.class);
        return ParallelLoaders.register(callerClass);
    }

    /**
     * 从用于加载类的搜索路径中查找指定名称的资源。此方法通过系统类加载器定位资源（参见{@link #getSystemClassLoader()}）。
     *
     * @param  name
     *         资源名称
     *
     * @return  用于读取资源的{@link java.net.URL <tt>URL</tt>}对象，如果资源未找到，则返回<tt>null</tt>
     *
     * @since  1.1
     */
    public static URL getSystemResource(String name) {
        ClassLoader system = getSystemClassLoader();
        if (system == null) {
            return getBootstrapResource(name);
        }
        return system.getResource(name);
    }

    /**
     * 从用于加载类的搜索路径中查找指定名称的所有资源。找到的资源作为{@link java.util.Enumeration <tt>Enumeration</tt>}的{@link java.net.URL <tt>URL</tt>}对象返回。
     *
     * <p> 搜索顺序在{@link #getSystemResource(String)}的文档中有描述。 </p>
     *
     * @param  name
     *         资源名称
     *
     * @return  资源的{@link java.net.URL <tt>URL</tt>}对象的枚举
     *
     * @throws  IOException
     *          如果发生I/O错误
     *
     * @since  1.2
     */
    public static Enumeration<URL> getSystemResources(String name)
        throws IOException
    {
        ClassLoader system = getSystemClassLoader();
        if (system == null) {
            return getBootstrapResources(name);
        }
        return system.getResources(name);
    }

    /**
     * 从虚拟机的内置类加载器中查找资源。
     */
    private static URL getBootstrapResource(String name) {
        URLClassPath ucp = getBootstrapClassPath();
        Resource res = ucp.getResource(name);
        return res != null ? res.getURL() : null;
    }

    /**
     * 从虚拟机的内置类加载器中查找资源。
     */
    private static Enumeration<URL> getBootstrapResources(String name)
        throws IOException
    {
        final Enumeration<Resource> e =
            getBootstrapClassPath().getResources(name);
        return new Enumeration<URL> () {
            public URL nextElement() {
                return e.nextElement().getURL();
            }
            public boolean hasMoreElements() {
                return e.hasMoreElements();
            }
        };
    }

    // 返回用于查找系统资源的URLClassPath。
    static URLClassPath getBootstrapClassPath() {
        return sun.misc.Launcher.getBootstrapClassPath();
    }

    /**
     * 返回用于读取指定资源的输入流。
     *
     * <p> 搜索顺序在{@link #getResource(String)}的文档中有描述。 </p>
     *
     * @param  name
     *         资源名称
     *
     * @return  用于读取资源的输入流，如果资源未找到，则返回<tt>null</tt>
     *
     * @since  1.1
     */
    public InputStream getResourceAsStream(String name) {
        URL url = getResource(name);
        try {
            return url != null ? url.openStream() : null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 从用于加载类的搜索路径中打开指定名称的资源以供读取。此方法通过系统类加载器定位资源（参见{@link #getSystemClassLoader()}）。
     *
     * @param  name
     *         资源名称
     *
     * @return  用于读取资源的输入流，如果资源未找到，则返回<tt>null</tt>
     *
     * @since  1.1
     */
    public static InputStream getSystemResourceAsStream(String name) {
        URL url = getSystemResource(name);
        try {
            return url != null ? url.openStream() : null;
        } catch (IOException e) {
            return null;
        }
    }

    // -- 层次结构 --

    /**
     * 返回用于委托的父类加载器。某些实现可能使用<tt>null</tt>表示引导类加载器。如果此类加载器的父类是引导类加载器，此方法将返回<tt>null</tt>。
     *
     * <p> 如果存在安全管理器，且调用者的类加载器不为<tt>null</tt>且不是此类加载器的祖先，则此方法将调用安全管理器的{@link SecurityManager#checkPermission(java.security.Permission) <tt>checkPermission</tt>}方法，检查{@link RuntimePermission#RuntimePermission(String) <tt>RuntimePermission("getClassLoader")</tt>}权限，以验证是否允许访问父类加载器。如果不允许，将抛出<tt>SecurityException</tt>。 </p>
     *
     * @return  父<tt>ClassLoader</tt>
     *
     * @throws  SecurityException
     *          如果存在安全管理器且其<tt>checkPermission</tt>方法不允许访问此类加载器的父类加载器。
     *
     * @since  1.2
     */
    @CallerSensitive
    public final ClassLoader getParent() {
        if (parent == null)
            return null;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            // 检查对父类加载器的访问权限
            // 如果调用者的类加载器与此类加载器相同，则执行权限检查。
            checkClassLoaderPermission(parent, Reflection.getCallerClass());
        }
        return parent;
    }

    /**
     * 返回用于委托的系统类加载器。这是新<tt>ClassLoader</tt>实例的默认委托父类，通常是用于启动应用程序的类加载器。
     *
     * <p> 此方法首先在运行时启动序列的早期调用，此时它创建系统类加载器并将其设置为调用<tt>Thread</tt>的上下文类加载器。
     *
     * <p> 默认系统类加载器是依赖于实现的此类的一个实例。
     *
     * <p> 如果在首次调用此方法时定义了系统属性"<tt>java.system.class.loader</tt>"，则该属性的值被视为系统类加载器的类名称。该类使用默认系统类加载器加载，并且必须定义一个接受单个<tt>ClassLoader</tt>类型参数的公共构造函数，该参数用作委托父类。然后使用此构造函数创建实例，以默认系统类加载器作为参数。生成的类加载器被定义为系统类加载器。
     *
     * <p> 如果存在安全管理器，且调用者的类加载器不为<tt>null</tt>且不是系统类加载器或其祖先，则此方法将调用安全管理器的{@link SecurityManager#checkPermission(java.security.Permission) <tt>checkPermission</tt>}方法，检查{@link RuntimePermission#RuntimePermission(String) <tt>RuntimePermission("getClassLoader")</tt>}权限，以验证是否允许访问系统类加载器。如果不允许，将抛出<tt>SecurityException</tt>。 </p>
     *
     * @return  用于委托的系统<tt>ClassLoader</tt>，如果没有则返回<tt>null</tt>
     *
     * @throws  SecurityException
     *          如果存在安全管理器且其<tt>checkPermission</tt>方法不允许访问系统类加载器。
     *
     * @throws  IllegalStateException
     *          如果在构造由"<tt>java.system.class.loader</tt>"属性指定的类加载器期间递归调用。
     *
     * @throws  Error
     *          如果定义了系统属性"<tt>java.system.class.loader</tt>"，但无法加载指定的类，或者提供者类未定义所需的构造函数，或者调用该构造函数时抛出异常。错误的根本原因可以通过{@link Throwable#getCause()}方法检索。
     *
     * @revised  1.4
     */
    @CallerSensitive
    public static ClassLoader getSystemClassLoader() {
        initSystemClassLoader();
        if (scl == null) {
            return null;
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkClassLoaderPermission(scl, Reflection.getCallerClass());
        }
        return scl;
    }

    private static synchronized void initSystemClassLoader() {
        if (!sclSet) {
            if (scl != null)
                throw new IllegalStateException("递归调用");
            sun.misc.Launcher l = sun.misc.Launcher.getLauncher();
            if (l != null) {
                Throwable oops = null;
                scl = l.getClassLoader();
                try {
                    scl = AccessController.doPrivileged(
                        new SystemClassLoaderAction(scl));
                } catch (PrivilegedActionException pae) {
                    oops = pae.getCause();
                    if (oops instanceof InvocationTargetException) {
                        oops = oops.getCause();
                    }
                }
                if (oops != null) {
                    if (oops instanceof Error) {
                        throw (Error) oops;
                    } else {
                        // 包装异常
                        throw new Error(oops);
                    }
                }
            }
            sclSet = true;
        }
    }

    // 如果指定的类加载器可以在此类加载器的委托链中找到，则返回true。
    boolean isAncestor(ClassLoader cl) {
        ClassLoader acl = this;
        do {
            acl = acl.parent;
            if (cl == acl) {
                return true;
            }
        } while (acl != null);
        return false;
    }

    // 测试类加载器访问是否需要“getClassLoader”权限检查。
    // 类加载器'from'可以访问类加载器'to'，如果类加载器'from'与类加载器'to'相同或为其祖先。
    // 系统域中的类加载器可以访问任何类加载器。
    private static boolean needsClassLoaderPermissionCheck(ClassLoader from,
                                                        ClassLoader to)
    {
        if (from == to)
            return false;

        if (from == null)
            return false;

        return !to.isAncestor(from);
    }

    // 返回类的类加载器，如果没有则返回null。
    static ClassLoader getClassLoader(Class<?> caller) {
        // 如果虚拟机请求，可能是null
        if (caller == null) {
            return null;
        }
        // 绕过安全检查，因为这是包私有方法
        return caller.getClassLoader0();
    }

    /*
    * 如果调用者的类加载器不为null且与给定cl参数的类加载器不同或不是其祖先，
    * 则检查RuntimePermission("getClassLoader")权限。
    */
    static void checkClassLoaderPermission(ClassLoader cl, Class<?> caller) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            // 调用者可能是null，如果是虚拟机请求
            ClassLoader ccl = getClassLoader(caller);
            if (needsClassLoaderPermissionCheck(ccl, cl)) {
                sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);
            }
        }
    }

    // 系统类加载器
    // @GuardedBy("ClassLoader.class")
    private static ClassLoader scl;

    // 系统类加载器一旦被设置，此标志置为true
    // @GuardedBy("ClassLoader.class")
    private static boolean sclSet;

    // -- 包 --

    /**
     * 在此<tt>ClassLoader</tt>中按名称定义一个包。这允许类加载器为其类定义包。必须在定义类之前创建包，包名称在类加载器中必须是唯一的，且一旦创建不可重新定义或更改。
     *
     * @param  name
     *         包名称
     *
     * @param  specTitle
     *         规范标题
     *
     * @param  specVersion
     *         规范版本
     *
     * @param  specVendor
     *         规范供应商
     *
     * @param  implTitle
     *         实现标题
     *
     * @param  implVersion
     *         实现版本
     *
     * @param  implVendor
     *         实现供应商
     *
     * @param  sealBase
     *         如果不为<tt>null</tt>，则此包相对于给定的代码源{@link java.net.URL <tt>URL</tt>}对象被密封。否则，包未被密封。
     *
     * @return  新定义的<tt>Package</tt>对象
     *
     * @throws  IllegalArgumentException
     *          如果包名称与此类加载器或其祖先中的现有包重复
     *
     * @since  1.2
     */
    protected Package definePackage(String name, String specTitle,
                                    String specVersion, String specVendor,
                                    String implTitle, String implVersion,
                                    String implVendor, URL sealBase)
        throws IllegalArgumentException
    {
        synchronized (packages) {
            Package pkg = getPackage(name);
            if (pkg != null) {
                throw new IllegalArgumentException(name);
            }
            pkg = new Package(name, specTitle, specVersion, specVendor,
                            implTitle, implVersion, implVendor,
                            sealBase, this);
            packages.put(name, pkg);
            return pkg;
        }
    }

    /**
     * 返回由此类加载器或其任何祖先定义的<tt>Package</tt>。
     *
     * @param  name
     *         包名称
     *
     * @return  对应于给定名称的<tt>Package</tt>，如果未找到则返回<tt>null</tt>
     *
     * @since  1.2
     */
    protected Package getPackage(String name) {
        Package pkg;
        synchronized (packages) {
            pkg = packages.get(name);
        }
        if (pkg == null) {
            if (parent != null) {
                pkg = parent.getPackage(name);
            } else {
                pkg = Package.getSystemPackage(name);
            }
            if (pkg != null) {
                synchronized (packages) {
                    Package pkg2 = packages.get(name);
                    if (pkg2 == null) {
                        packages.put(name, pkg);
                    } else {
                        pkg = pkg2;
                    }
                }
            }
        }
        return pkg;
    }

    /**
     * 返回由此类加载器及其祖先定义的所有<tt>Packages</tt>。
     *
     * @return  由此<tt>ClassLoader</tt>定义的<tt>Package</tt>对象数组
     *
     * @since  1.2
     */
    protected Package[] getPackages() {
        Map<String, Package> map;
        synchronized (packages) {
            map = new HashMap<>(packages);
        }
        Package[] pkgs;
        if (parent != null) {
            pkgs = parent.getPackages();
        } else {
            pkgs = Package.getSystemPackages();
        }
        if (pkgs != null) {
            for (int i = 0; i < pkgs.length; i++) {
                String pkgName = pkgs[i].getName();
                if (map.get(pkgName) == null) {
                    map.put(pkgName, pkgs[i]);
                }
            }
        }
        return map.values().toArray(new Package[map.size()]);
    }

    // -- 原生库访问 --

    /**
     * 返回原生库的绝对路径名称。虚拟机会调用此方法来定位属于由此类加载器加载的类的原生库。如果此方法返回<tt>null</tt>，虚拟机将沿指定为"<tt>java.library.path</tt>"属性的路径搜索库。
     *
     * @param  libname
     *         库名称
     *
     * @return  原生库的绝对路径
     *
     * @see  System#loadLibrary(String)
     * @see  System#mapLibraryName(String)
     *
     * @since  1.2
     */
    protected String findLibrary(String libname) {
        return null;
    }

    /**
     * 内部类NativeLibrary表示已加载的原生库实例。每个类加载器包含一个已加载原生库的向量，存储在私有字段<tt>nativeLibraries</tt>中。加载到系统中的原生库被添加到<tt>systemNativeLibraries</tt>向量中。
     *
     * <p> 每个原生库需要特定的JNI版本。这由私有<tt>jniVersion</tt>字段表示。此字段由虚拟机在加载库时设置，并由虚拟机用于将正确的JNI版本传递给原生方法。 </p>
     *
     * @see      ClassLoader
     * @since    1.2
     */
    static class NativeLibrary {
        // 原生库的不透明句柄，用于原生代码。
        long handle;
        // 原生库所需的JNI环境版本。
        private int jniVersion;
        // 加载库的类，也指示此原生库所属的加载器。
        private final Class<?> fromClass;
        // 原生库的规范化名称。
        // 或静态库名称
        String name;
        // 指示原生库是否链接到虚拟机
        boolean isBuiltin;
        // 指示原生库是否已加载
        boolean loaded;

        private static final boolean loadLibraryOnlyIfPresent = ClassLoaderHelper.loadLibraryOnlyIfPresent();

        native void load(String name, boolean isBuiltin, boolean throwExceptionIfFail);

        native long find(String name);
        native void unload(String name, boolean isBuiltin);

        public NativeLibrary(Class<?> fromClass, String name, boolean isBuiltin) {
            this.name = name;
            this.fromClass = fromClass;
            this.isBuiltin = isBuiltin;
        }

        protected void finalize() {
            synchronized (loadedLibraryNames) {
                if (fromClass.getClassLoader() != null && loaded) {
                    /* 移除原生库名称 */
                    int size = loadedLibraryNames.size();
                    for (int i = 0; i < size; i++) {
                        if (name.equals(loadedLibraryNames.elementAt(i))) {
                            loadedLibraryNames.removeElementAt(i);
                            break;
                        }
                    }
                    /* 卸载库 */
                    ClassLoader.nativeLibraryContext.push(this);
                    try {
                        unload(name, isBuiltin);
                    } finally {
                        ClassLoader.nativeLibraryContext.pop();
                    }
                }
            }
        }
        // 在虚拟机中调用以确定JNI_Load/JNI_Unload中的上下文类
        static Class<?> getFromClass() {
            return ClassLoader.nativeLibraryContext.peek().fromClass;
        }
    }

    // 我们加载的所有原生库名称。
    private static Vector<String> loadedLibraryNames = new Vector<>();

    // 属于系统类的原生库。
    private static Vector<NativeLibrary> systemNativeLibraries
        = new Vector<>();

    // 与类加载器关联的原生库。
    private Vector<NativeLibrary> nativeLibraries = new Vector<>();

    // 正在加载/卸载的原生库。
    private static Stack<NativeLibrary> nativeLibraryContext = new Stack<>();

    // 搜索库的路径
    private static String usr_paths[];
    private static String sys_paths[];

    private static String[] initializePath(String propname) {
        String ldpath = System.getProperty(propname, "");
        String ps = File.pathSeparator;
        int ldlen = ldpath.length();
        int i, j, n;
        // 计算路径中的分隔符数量
        i = ldpath.indexOf(ps);
        n = 0;
        while (i >= 0) {
            n++;
            i = ldpath.indexOf(ps, i + 1);
        }

        // 分配路径数组 - n个分隔符 = n + 1个路径元素
        String[] paths = new String[n + 1];

        // 用ldpath中的路径填充数组
        n = i = 0;
        j = ldpath.indexOf(ps);
        while (j >= 0) {
            if (j - i > 0) {
                paths[n++] = ldpath.substring(i, j);
            } else if (j - i == 0) {
                paths[n++] = ".";
            }
            i = j + 1;
            j = ldpath.indexOf(ps, i);
        }
        paths[n] = ldpath.substring(i, ldlen);
        return paths;
    }

    // 在java.lang.Runtime类中调用以实现load和loadLibrary。
    static void loadLibrary(Class<?> fromClass, String name,
                            boolean isAbsolute) {
        ClassLoader loader =
            (fromClass == null) ? null : fromClass.getClassLoader();
        if (sys_paths == null) {
            usr_paths = initializePath("java.library.path");
            sys_paths = initializePath("sun.boot.library.path");
        }
        if (isAbsolute) {
            if (loadLibrary0(fromClass, new File(name))) {
                return;
            }
            throw new UnsatisfiedLinkError("无法加载库: " + name);
        }
        if (loader != null) {
            String libfilename = loader.findLibrary(name);
            if (libfilename != null) {
                File libfile = new File(libfilename);
                if (!libfile.isAbsolute()) {
                    throw new UnsatisfiedLinkError(
    "ClassLoader.findLibrary未能返回绝对路径: " + libfilename);
                }
                if (loadLibrary0(fromClass, libfile)) {
                    return;
                }
                throw new UnsatisfiedLinkError("无法加载 " + libfilename);
            }
        }
        for (int i = 0 ; i < sys_paths.length ; i++) {
            File libfile = new File(sys_paths[i], System.mapLibraryName(name));
            if (loadLibrary0(fromClass, libfile)) {
                return;
            }
            libfile = ClassLoaderHelper.mapAlternativeName(libfile);
            if (libfile != null && loadLibrary0(fromClass, libfile)) {
                return;
            }
        }
        if (loader != null) {
            for (int i = 0 ; i < usr_paths.length ; i++) {
                File libfile = new File(usr_paths[i],
                                        System.mapLibraryName(name));
                if (loadLibrary0(fromClass, libfile)) {
                    return;
                }
                libfile = ClassLoaderHelper.mapAlternativeName(libfile);
                if (libfile != null && loadLibrary0(fromClass, libfile)) {
                    return;
                }
            }
        }
        // 加载失败
        throw new UnsatisfiedLinkError("在java.library.path中没有 " + name);
    }

    private static native String findBuiltinLib(String name);

    private static boolean loadLibrary0(Class<?> fromClass, final File file) {
        // 检查是否尝试访问静态库
        String name = findBuiltinLib(file.getName());
        boolean isBuiltin = (name != null);
        if (!isBuiltin) {
            boolean exists = AccessController.doPrivileged(
                new PrivilegedAction<Object>() {
                    public Object run() {
                        return file.exists() ? Boolean.TRUE : null;
                    }})
                != null;
            if (NativeLibrary.loadLibraryOnlyIfPresent && !exists) {
                return false;
            }
            try {
                name = file.getCanonicalPath();
            } catch (IOException e) {
                return false;
            }
        }
        ClassLoader loader =
            (fromClass == null) ? null : fromClass.getClassLoader();
        Vector<NativeLibrary> libs =
            loader != null ? loader.nativeLibraries : systemNativeLibraries;
        synchronized (libs) {
            int size = libs.size();
            for (int i = 0; i < size; i++) {
                NativeLibrary lib = libs.elementAt(i);
                if (name.equals(lib.name)) {
                    return true;
                }
            }

            synchronized (loadedLibraryNames) {
                if (loadedLibraryNames.contains(name)) {
                    throw new UnsatisfiedLinkError
                        ("原生库 " +
                        name +
                        " 已在另一个类加载器中加载");
                }
                /* 如果库正在加载（必须由同一线程执行，
                * 因为Runtime.load和Runtime.loadLibrary是同步的）。
                * 可能发生的原因是JNI_OnLoad函数可能导致另一个loadLibrary调用。
                *
                * 因此，我们可以使用静态栈来保存我们正在加载的库列表。
                *
                * 如果库有待加载操作，我们立即返回成功；
                * 否则，抛出UnsatisfiedLinkError。
                */
                int n = nativeLibraryContext.size();
                for (int i = 0; i < n; i++) {
                    NativeLibrary lib = nativeLibraryContext.elementAt(i);
                    if (name.equals(lib.name)) {
                        if (loader == lib.fromClass.getClassLoader()) {
                            return true;
                        } else {
                            throw new UnsatisfiedLinkError
                                ("原生库 " +
                                name +
                                " 正在另一个类加载器中加载");
                        }
                    }
                }
                NativeLibrary lib = new NativeLibrary(fromClass, name, isBuiltin);
                nativeLibraryContext.push(lib);
                try {
                    lib.load(name, isBuiltin, NativeLibrary.loadLibraryOnlyIfPresent);
                } finally {
                    nativeLibraryContext.pop();
                }
                if (lib.loaded) {
                    loadedLibraryNames.addElement(name);
                    libs.addElement(lib);
                    return true;
                }
                return false;
            }
        }
    }

    // 在虚拟机类链接代码中调用。
    static long findNative(ClassLoader loader, String name) {
        Vector<NativeLibrary> libs =
            loader != null ? loader.nativeLibraries : systemNativeLibraries;
        synchronized (libs) {
            int size = libs.size();
            for (int i = 0; i < size; i++) {
                NativeLibrary lib = libs.elementAt(i);
                long entry = lib.find(name);
                if (entry != 0)
                    return entry;
            }
        }
        return 0;
    }

    // -- 断言管理 --

    final Object assertionLock;

    // 断言检查的默认开关。
    // @GuardedBy("assertionLock")
    private boolean defaultAssertionStatus = false;

    // 将String包名称映射到Boolean包默认断言状态。
    // 注意，默认包放置在null映射键下。如果此字段为null，则我们将断言状态查询委托给虚拟机，即，
    // 此类加载器的断言状态修改方法均未被调用。
    // @GuardedBy("assertionLock")
    private Map<String, Boolean> packageAssertionStatus = null;

    // 将String完全限定类名映射到Boolean断言状态。
    // 如果此字段为null，则我们将断言状态查询委托给虚拟机，即，
    // 此类加载器的断言状态修改方法均未被调用。
    // @GuardedBy("assertionLock")
    Map<String, Boolean> classAssertionStatus = null;

    /**
     * 设置此类加载器的默认断言状态。此设置决定由此类加载器加载并在未来初始化的类默认启用还是禁用断言。
     * 可以通过调用{@link #setPackageAssertionStatus(String, boolean)}或{@link #setClassAssertionStatus(String, boolean)}按包或按类覆盖此设置。
     *
     * @param  enabled
     *         如果由此类加载器加载的类将默认启用断言，则为<tt>true</tt>，
     *         如果将默认禁用断言，则为<tt>false</tt>。
     *
     * @since  1.4
     */
    public void setDefaultAssertionStatus(boolean enabled) {
        synchronized (assertionLock) {
            if (classAssertionStatus == null)
                initializeJavaAssertionMaps();

            defaultAssertionStatus = enabled;
        }
    }

    /**
     * 为指定的包设置包默认断言状态。包默认断言状态决定属于指定包或其任何“子包”的未来初始化的类的断言状态。
     *
     * <p> 包p的子包是名称以"<tt>p.</tt>"开头的任何包。例如，<tt>javax.swing.text</tt>是<tt>javax.swing</tt>的子包，
     * <tt>java.util</tt>和<tt>java.lang.reflect</tt>都是<tt>java</tt>的子包。
     *
     * <p> 如果多个包默认适用于某个类，则与最具体包相关的包默认优先于其他包。例如，如果<tt>javax.lang</tt>和<tt>javax.lang.reflect</tt>都有关联的包默认，
     * 则后者的包默认适用于<tt>javax.lang.reflect</tt>中的类。
     *
     * <p> 包默认优先于类加载器的默认断言状态，并可通过调用{@link #setClassAssertionStatus(String, boolean)}按类覆盖。 </p>
     *
     * @param  packageName
     *         要设置包默认断言状态的包名称。<tt>null</tt>值表示“当前”未命名包
     *         （参见<cite>Java™语言规范</cite>第7.4.2节。）
     *
     * @param  enabled
     *         如果由此类加载器加载并属于指定包或其任何子包的类将默认启用断言，则为<tt>true</tt>，
     *         如果将默认禁用断言，则为<tt>false</tt>。
     *
     * @since  1.4
     */
    public void setPackageAssertionStatus(String packageName,
                                        boolean enabled) {
        synchronized (assertionLock) {
            if (packageAssertionStatus == null)
                initializeJavaAssertionMaps();

            packageAssertionStatus.put(packageName, enabled);
        }
    }

    /**
     * 为此类加载器中指定的顶级类及其包含的任何嵌套类设置所需的断言状态。此设置优先于类加载器的默认断言状态以及任何适用的按包默认。
     * 如果指定类已初始化，此方法无效。（一旦类初始化，其断言状态不可更改。）
     *
     * <p> 如果指定类不是顶级类，此调用对任何类的实际断言状态无影响。 </p>
     *
     * @param  className
     *         要设置断言状态的顶级类的完全限定类名。
     *
     * @param  enabled
     *         如果指定类在（如果）初始化时启用断言，则为<tt>true</tt>，
     *         如果禁用断言，则为<tt>false</tt>。
     *
     * @since  1.4
     */
    public void setClassAssertionStatus(String className, boolean enabled) {
        synchronized (assertionLock) {
            if (classAssertionStatus == null)
                initializeJavaAssertionMaps();

            classAssertionStatus.put(className, enabled);
        }
    }

    /**
     * 将此类加载器的默认断言状态设置为<tt>false</tt>，并丢弃与类加载器关联的任何包默认或类断言状态设置。
     * 提供此方法以便类加载器可以忽略任何命令行或持久断言状态设置，并“从头开始”。
     *
     * @since  1.4
     */
    public void clearAssertionStatus() {
        /*
        * 无论“Java断言映射”是否初始化，都将它们设置为空映射，
        * 有效地忽略任何现有设置。
        */
        synchronized (assertionLock) {
            classAssertionStatus = new HashMap<>();
            packageAssertionStatus = new HashMap<>();
            defaultAssertionStatus = false;
        }
    }

    /**
     * 返回如果在调用此方法时初始化指定类，将分配给该类的断言状态。
     * 如果已为指定类设置了断言状态，将返回最近的设置；
     * 否则，如果任何包默认断言状态适用于此类，将返回与最具体相关包默认断言状态的最近设置；
     * 否则，返回此类加载器的默认断言状态。
     *
     * @param  className
     *         要查询所需断言状态的类的完全限定类名。
     *
     * @return  指定类的所需断言状态。
     *
     * @see  #setClassAssertionStatus(String, boolean)
     * @see  #setPackageAssertionStatus(String, boolean)
     * @see  #setDefaultAssertionStatus(boolean)
     *
     * @since  1.4
     */
    boolean desiredAssertionStatus(String className) {
        synchronized (assertionLock) {
            // 断言 classAssertionStatus != null;
            // 断言 packageAssertionStatus != null;

            // 检查类条目
            Boolean result = classAssertionStatus.get(className);
            if (result != null)
                return result.booleanValue();

            // 检查最具体的包条目
            int dotIndex = className.lastIndexOf(".");
            if (dotIndex < 0) { // 默认包
                result = packageAssertionStatus.get(null);
                if (result != null)
                    return result.booleanValue();
            }
            while(dotIndex > 0) {
                className = className.substring(0, dotIndex);
                result = packageAssertionStatus.get(className);
                if (result != null)
                    return result.booleanValue();
                dotIndex = className.lastIndexOf(".", dotIndex-1);
            }

            // 返回类加载器默认值
            return defaultAssertionStatus;
        }
    }

    // 使用虚拟机提供的信息设置断言。
    // 注意：仅应在同步块内调用
    private void initializeJavaAssertionMaps() {
        // 断言 Thread.holdsLock(assertionLock);

        classAssertionStatus = new HashMap<>();
        packageAssertionStatus = new HashMap<>();
        AssertionStatusDirectives directives = retrieveDirectives();

        for(int i = 0; i < directives.classes.length; i++)
            classAssertionStatus.put(directives.classes[i],
                                    directives.classEnabled[i]);

        for(int i = 0; i < directives.packages.length; i++)
            packageAssertionStatus.put(directives.packages[i],
                                    directives.packageEnabled[i]);

        defaultAssertionStatus = directives.deflt;
    }

    // 从虚拟机检索断言指令。
    private static native AssertionStatusDirectives retrieveDirectives();
    
}



class SystemClassLoaderAction
    implements PrivilegedExceptionAction<ClassLoader> {
    private ClassLoader parent;

    SystemClassLoaderAction(ClassLoader parent) {
        this.parent = parent;
    }

    public ClassLoader run() throws Exception {
        String cls = System.getProperty("java.system.class.loader");
        if (cls == null) {
            return parent;
        }

        Constructor<?> ctor = Class.forName(cls, true, parent)
            .getDeclaredConstructor(new Class<?>[] { ClassLoader.class });
        ClassLoader sys = (ClassLoader) ctor.newInstance(
            new Object[] { parent });
        Thread.currentThread().setContextClassLoader(sys);
        return sys;
    }
}
