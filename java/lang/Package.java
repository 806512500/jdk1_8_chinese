
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

import java.lang.reflect.AnnotatedElement;
import java.io.InputStream;
import java.util.Enumeration;

import java.util.StringTokenizer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import sun.net.www.ParseUtil;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;

import java.lang.annotation.Annotation;

/**
 * {@code Package} 对象包含有关 Java 包的实现和规范的版本信息。
 * 这些版本信息由加载该类的 {@link ClassLoader} 实例检索并提供。
 * 通常，这些信息存储在与类一起分发的清单中。
 *
 * <p>组成该包的类可能实现了特定的规范，如果确实如此，规范标题、版本号和供应商字符串将标识该规范。
 * 应用程序可以询问该包是否与特定版本兼容，详情请参阅 {@link
 * #isCompatibleWith isCompatibleWith}
 * 方法。
 *
 * <p>规范版本号使用由非负十进制整数组成的语法，整数之间用句点 "." 分隔，例如 "2.0" 或
 * "1.2.3.4.5.6.7"。这允许使用可扩展的数字来表示主要、次要、微小等版本。版本规范由以下正式语法描述：
 * <blockquote>
 * <dl>
 * <dt><i>SpecificationVersion:</i>
 * <dd><i>Digits RefinedVersion<sub>opt</sub></i>

 * <dt><i>RefinedVersion:</i>
 * <dd>{@code .} <i>Digits</i>
 * <dd>{@code .} <i>Digits RefinedVersion</i>
 *
 * <dt><i>Digits:</i>
 * <dd><i>Digit</i>
 * <dd><i>Digits</i>
 *
 * <dt><i>Digit:</i>
 * <dd>任何使 {@link Character#isDigit} 返回 {@code true} 的字符，例如 0, 1, 2, ...
 * </dl>
 * </blockquote>
 *
 * <p>实现标题、版本和供应商字符串标识实现，并提供方便的方法以在问题发生时准确报告涉及的包。所有三个实现字符串的内容都是供应商特定的。
 * 实现版本字符串没有指定的语法，只能与所需的版本标识符进行相等比较。
 *
 * <p>在每个 {@code ClassLoader} 实例中，来自同一 Java 包的所有类都具有相同的 Package 对象。静态方法允许通过名称查找包
 * 或查找当前类加载器已知的所有包。
 *
 * @see ClassLoader#definePackage
 */
public class Package implements java.lang.reflect.AnnotatedElement {
    /**
     * 返回此包的名称。
     *
     * @return 该包的完全限定名称，如《Java&trade; 语言规范》第 6.5.3 节所定义，
     *          例如，{@code java.lang}
     */
    public String getName() {
        return pkgName;
    }


    /**
     * 返回此包实现的规范的标题。
     * @return 规范标题，如果未知则返回 null。
     */
    public String getSpecificationTitle() {
        return specTitle;
    }

    /**
     * 返回此包实现的规范的版本号。
     * 该版本字符串必须是由非负十进制整数组成的序列，整数之间用 "." 分隔，可以有前导零。
     * 当比较版本字符串时，最显著的数字会被比较。
     * @return 规范版本，如果未知则返回 null。
     */
    public String getSpecificationVersion() {
        return specVersion;
    }

    /**
     * 返回实现此包的类的规范的所有者和维护者（组织、供应商或公司）的名称。
     * @return 规范供应商，如果未知则返回 null。
     */
    public String getSpecificationVendor() {
        return specVendor;
    }

    /**
     * 返回此包的标题。
     * @return 实现的标题，如果未知则返回 null。
     */
    public String getImplementationTitle() {
        return implTitle;
    }

    /**
     * 返回此实现的版本。它由供应商为该实现分配的任何字符串组成，
     * 并没有特定的语法要求。可以将其与其他供应商为该包的此实现分配的版本字符串进行相等比较。
     * @return 实现版本，如果未知则返回 null。
     */
    public String getImplementationVersion() {
        return implVersion;
    }

    /**
     * 返回提供此实现的组织、供应商或公司的名称。
     * @return 实现此包的供应商。
     */
    public String getImplementationVendor() {
        return implVendor;
    }

    /**
     * 如果此包已密封，则返回 true。
     *
     * @return 如果包已密封，则返回 true，否则返回 false。
     */
    public boolean isSealed() {
        return sealBase != null;
    }

    /**
     * 如果此包相对于指定的代码源 URL 已密封，则返回 true。
     *
     * @param url 代码源 URL
     * @return 如果此包相对于 URL 已密封，则返回 true。
     */
    public boolean isSealed(URL url) {
        return url.equals(sealBase);
    }

    /**
     * 比较此包的规范版本与所需版本。如果
     * 此包的规范版本号大于或等于所需版本号，则返回 true。 <p>
     *
     * 版本号通过顺序比较所需和规范字符串的相应组件来比较。
     * 每个组件被转换为十进制整数并进行比较。
     * 如果规范值大于所需值，则返回 true。如果值小于所需值，则返回 false。
     * 如果值相等，则跳过句点并比较下一对组件。
     *
     * @param desired 所需版本的版本字符串。
     * @return 如果此包的版本号大于或等于所需版本号，则返回 true。
     *
     * @exception NumberFormatException 如果所需或当前版本不是正确的点分形式。
     */
    public boolean isCompatibleWith(String desired)
        throws NumberFormatException
    {
        if (specVersion == null || specVersion.length() < 1) {
            throw new NumberFormatException("Empty version string");
        }

        String [] sa = specVersion.split("\\.", -1);
        int [] si = new int[sa.length];
        for (int i = 0; i < sa.length; i++) {
            si[i] = Integer.parseInt(sa[i]);
            if (si[i] < 0)
                throw NumberFormatException.forInputString("" + si[i]);
        }

        String [] da = desired.split("\\.", -1);
        int [] di = new int[da.length];
        for (int i = 0; i < da.length; i++) {
            di[i] = Integer.parseInt(da[i]);
            if (di[i] < 0)
                throw NumberFormatException.forInputString("" + di[i]);
        }

        int len = Math.max(di.length, si.length);
        for (int i = 0; i < len; i++) {
            int d = (i < di.length ? di[i] : 0);
            int s = (i < si.length ? si[i] : 0);
            if (s < d)
                return false;
            if (s > d)
                return true;
        }
        return true;
    }

    /**
     * 在调用者的 {@code ClassLoader} 实例中按名称查找包。
     * 使用调用者的 {@code ClassLoader} 实例查找与命名类对应的包实例。如果调用者的
     * {@code ClassLoader} 实例为 null，则搜索系统 {@code ClassLoader} 实例加载的包集以查找
     * 命名的包。 <p>
     *
     * 只有当类加载器在创建包实例时使用了适当的属性，包才会有版本和规范属性。通常，
     * 这些属性在伴随类的清单中定义。
     *
     * @param name 包名称，例如，java.lang。
     * @return 请求名称的包。如果从存档或代码库中无法获取包信息，则可能为 null。
     */
    @CallerSensitive
    public static Package getPackage(String name) {
        ClassLoader l = ClassLoader.getClassLoader(Reflection.getCallerClass());
        if (l != null) {
            return l.getPackage(name);
        } else {
            return getSystemPackage(name);
        }
    }

    /**
     * 获取调用者的 {@code ClassLoader} 实例当前已知的所有包。
     * 这些包对应于通过该 {@code ClassLoader} 实例加载或可通过名称访问的类。如果调用者的
     * {@code ClassLoader} 实例是引导 {@code ClassLoader} 实例，这在某些实现中可能表示为
     * {@code null}，则仅返回由引导 {@code ClassLoader} 实例加载的包。
     *
     * @return 调用者的 {@code ClassLoader} 实例已知的包的新数组。如果没有已知的包，则返回零长度数组。
     */
    @CallerSensitive
    public static Package[] getPackages() {
        ClassLoader l = ClassLoader.getClassLoader(Reflection.getCallerClass());
        if (l != null) {
            return l.getPackages();
        } else {
            return getSystemPackages();
        }
    }

    /**
     * 获取指定类的包。
     * 使用类的类加载器查找与指定类对应的包实例。如果类加载器
     * 是引导类加载器，这在某些实现中可能表示为
     * {@code null}，则搜索引导类加载器加载的包集以查找包。
     * <p>
     * 只有当类加载器在创建包实例时使用了适当的属性，包才会有版本和规范属性。通常这些
     * 属性在伴随类的清单中定义。
     *
     * @param c 要获取包的类。
     * @return 类的包。如果从存档或代码库中无法获取包信息，则可能为 null。
     */
    static Package getPackage(Class<?> c) {
        String name = c.getName();
        int i = name.lastIndexOf('.');
        if (i != -1) {
            name = name.substring(0, i);
            ClassLoader cl = c.getClassLoader();
            if (cl != null) {
                return cl.getPackage(name);
            } else {
                return getSystemPackage(name);
            }
        } else {
            return null;
        }
    }

    /**
     * 从包名称计算哈希码。
     * @return 从包名称计算的哈希码。
     */
    public int hashCode(){
        return pkgName.hashCode();
    }

    /**
     * 返回此 Package 的字符串表示形式。
     * 其值是字符串 "package " 和包名称。如果定义了包标题，则附加包标题。
     * 如果定义了包版本，则附加包版本。
     * @return 包的字符串表示形式。
     */
    public String toString() {
        String spec = specTitle;
        String ver =  specVersion;
        if (spec != null && spec.length() > 0)
            spec = ", " + spec;
        else
            spec = "";
        if (ver != null && ver.length() > 0)
            ver = ", version " + ver;
        else
            ver = "";
        return "package " + pkgName + spec + ver;
    }

    private Class<?> getPackageInfo() {
        if (packageInfo == null) {
            try {
                packageInfo = Class.forName(pkgName + ".package-info", false, loader);
            } catch (ClassNotFoundException ex) {
                // 存储一个没有注解的包信息代理
                class PackageInfoProxy {}
                packageInfo = PackageInfoProxy.class;
            }
        }
        return packageInfo;
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.5
     */
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return getPackageInfo().getAnnotation(annotationClass);
    }

    /**
     * {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @since 1.5
     */
    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return AnnotatedElement.super.isAnnotationPresent(annotationClass);
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    public  <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationClass) {
        return getPackageInfo().getAnnotationsByType(annotationClass);
    }

    /**
     * @since 1.5
     */
    public Annotation[] getAnnotations() {
        return getPackageInfo().getAnnotations();
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    public <A extends Annotation> A getDeclaredAnnotation(Class<A> annotationClass) {
        return getPackageInfo().getDeclaredAnnotation(annotationClass);
    }


                /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    public <A extends Annotation> A[] getDeclaredAnnotationsByType(Class<A> annotationClass) {
        return getPackageInfo().getDeclaredAnnotationsByType(annotationClass);
    }

    /**
     * @since 1.5
     */
    public Annotation[] getDeclaredAnnotations()  {
        return getPackageInfo().getDeclaredAnnotations();
    }

    /**
     * 构造一个具有指定版本信息的包实例。
     * @param name 包的名称
     * @param spectitle 规范的标题
     * @param specversion 规范的版本
     * @param specvendor 维护规范的组织
     * @param impltitle 实现的标题
     * @param implversion 实现的版本
     * @param implvendor 维护实现的组织
     */
    Package(String name,
            String spectitle, String specversion, String specvendor,
            String impltitle, String implversion, String implvendor,
            URL sealbase, ClassLoader loader)
    {
        pkgName = name;
        implTitle = impltitle;
        implVersion = implversion;
        implVendor = implvendor;
        specTitle = spectitle;
        specVersion = specversion;
        specVendor = specvendor;
        sealBase = sealbase;
        this.loader = loader;
    }

    /*
     * 使用指定清单中的属性构造一个包。
     *
     * @param name 包的名称
     * @param man 包的可选清单
     * @param url 包的可选代码源 URL
     */
    private Package(String name, Manifest man, URL url, ClassLoader loader) {
        String path = name.replace('.', '/').concat("/");
        String sealed = null;
        String specTitle= null;
        String specVersion= null;
        String specVendor= null;
        String implTitle= null;
        String implVersion= null;
        String implVendor= null;
        URL sealBase= null;
        Attributes attr = man.getAttributes(path);
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
        pkgName = name;
        this.specTitle = specTitle;
        this.specVersion = specVersion;
        this.specVendor = specVendor;
        this.implTitle = implTitle;
        this.implVersion = implVersion;
        this.implVendor = implVendor;
        this.sealBase = sealBase;
        this.loader = loader;
    }

    /*
     * 返回指定名称的已加载系统包。
     */
    static Package getSystemPackage(String name) {
        synchronized (pkgs) {
            Package pkg = pkgs.get(name);
            if (pkg == null) {
                name = name.replace('.', '/').concat("/");
                String fn = getSystemPackage0(name);
                if (fn != null) {
                    pkg = defineSystemPackage(name, fn);
                }
            }
            return pkg;
        }
    }

    /*
     * 返回已加载的系统包数组。
     */
    static Package[] getSystemPackages() {
        // 首先，使用新的包名称更新系统包映射
        String[] names = getSystemPackages0();
        synchronized (pkgs) {
            for (int i = 0; i < names.length; i++) {
                defineSystemPackage(names[i], getSystemPackage0(names[i]));
            }
            return pkgs.values().toArray(new Package[pkgs.size()]);
        }
    }

    private static Package defineSystemPackage(final String iname,
                                               final String fn)
    {
        return AccessController.doPrivileged(new PrivilegedAction<Package>() {
            public Package run() {
                String name = iname;
                // 获取文件名的缓存代码源 URL
                URL url = urls.get(fn);
                if (url == null) {
                    // 未找到 URL，因此创建一个
                    File file = new File(fn);
                    try {
                        url = ParseUtil.fileToEncodedURL(file);
                    } catch (MalformedURLException e) {
                    }
                    if (url != null) {
                        urls.put(fn, url);
                        // 如果加载的是 JAR 文件，则还缓存清单
                        if (file.isFile()) {
                            mans.put(fn, loadManifest(fn));
                        }
                    }
                }
                // 转换为以 "." 分隔的包名
                name = name.substring(0, name.length() - 1).replace('/', '.');
                Package pkg;
                Manifest man = mans.get(fn);
                if (man != null) {
                    pkg = new Package(name, man, url, null);
                } else {
                    pkg = new Package(name, null, null, null,
                                      null, null, null, null, null);
                }
                pkgs.put(name, pkg);
                return pkg;
            }
        });
    }

    /*
     * 返回指定 JAR 文件名的清单。
     */
    private static Manifest loadManifest(String fn) {
        try (FileInputStream fis = new FileInputStream(fn);
             JarInputStream jis = new JarInputStream(fis, false))
        {
            return jis.getManifest();
        } catch (IOException e) {
            return null;
        }
    }

    // 已加载的系统包映射
    private static Map<String, Package> pkgs = new HashMap<>(31);

    // 每个目录或 ZIP 文件名映射到其对应的 URL
    private static Map<String, URL> urls = new HashMap<>(10);

    // 每个 JAR 文件的代码源 URL 映射到其清单
    private static Map<String, Manifest> mans = new HashMap<>(10);

    private static native String getSystemPackage0(String name);
    private static native String[] getSystemPackages0();

    /*
     * 包名称和属性的私有存储。
     */
    private final String pkgName;
    private final String specTitle;
    private final String specVersion;
    private final String specVendor;
    private final String implTitle;
    private final String implVersion;
    private final String implVendor;
    private final URL sealBase;
    private transient final ClassLoader loader;
    private transient Class<?> packageInfo;
}
