
/*
 * Copyright (c) 1996, 2017, Oracle and/or its affiliates. All rights reserved.
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

/*
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1999 - All Rights Reserved
 *
 * The original version of this source code and documentation
 * is copyrighted and owned by Taligent, Inc., a wholly-owned
 * subsidiary of IBM. These materials are provided under terms
 * of a License Agreement between Taligent and Sun. This technology
 * is protected by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 *
 */

package java.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.JarEntry;
import java.util.spi.ResourceBundleControlProvider;

import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import sun.util.locale.BaseLocale;
import sun.util.locale.LocaleObjectCache;


/**
 *
 * 资源包包含特定于语言环境的对象。当您的程序需要特定于语言环境的资源时，例如一个 <code>String</code>，您的程序可以从当前用户语言环境对应的资源包中加载它。通过这种方式，您可以编写大部分独立于用户语言环境的程序代码，将大多数，如果不是全部，特定于语言环境的信息隔离在资源包中。
 *
 * <p>
 * 这允许您编写可以：
 * <UL>
 * <LI>轻松本地化，或翻译成不同语言
 * <LI>同时处理多个语言环境
 * <LI>以后轻松修改以支持更多语言环境
 * </UL>
 *
 * <P>
 * 资源包属于一个家族，其成员共享一个共同的基本名称，但名称中还有其他组件标识它们的语言环境。例如，一个资源包家族的基本名称可能是 "MyResources"。该家族应该有一个默认的资源包，其名称与家族名称相同 - "MyResources" - 并将作为最后的资源包，如果特定语言环境不受支持。该家族可以提供尽可能多的语言环境特定成员，例如一个德语版本 "MyResources_de"。
 *
 * <P>
 * 每个资源包家族成员包含相同的项目，但这些项目已翻译成该资源包代表的语言环境。例如，"MyResources" 和 "MyResources_de" 可能都包含一个用于取消操作的按钮上的 <code>String</code>。在 "MyResources" 中，<code>String</code> 可能包含 "Cancel"，而在 "MyResources_de" 中，它可能包含 "Abbrechen"。
 *
 * <P>
 * 如果不同国家有不同的资源，您可以创建特殊化版本：例如，"MyResources_de_CH" 包含德语 (de) 在瑞士 (CH) 的对象。如果您只想修改特殊化版本中的一些资源，您可以这样做。
 *
 * <P>
 * 当您的程序需要特定于语言环境的对象时，它使用
 * {@link #getBundle(java.lang.String, java.util.Locale) getBundle}
 * 方法加载 <code>ResourceBundle</code> 类：
 * <blockquote>
 * <pre>
 * ResourceBundle myResources =
 *      ResourceBundle.getBundle("MyResources", currentLocale);
 * </pre>
 * </blockquote>
 *
 * <P>
 * 资源包包含键/值对。键在包中唯一标识一个特定于语言环境的对象。以下是一个 <code>ListResourceBundle</code> 的示例，其中包含两个键/值对：
 * <blockquote>
 * <pre>
 * public class MyResources extends ListResourceBundle {
 *     protected Object[][] getContents() {
 *         return new Object[][] {
 *             // 本地化每个数组的第二个字符串（例如，"OK"）
 *             {"OkKey", "OK"},
 *             {"CancelKey", "Cancel"},
 *             // 本地化材料结束
 *        };
 *     }
 * }
 * </pre>
 * </blockquote>
 * 键始终是 <code>String</code>。在这个例子中，键是 "OkKey" 和 "CancelKey"。在上面的例子中，值
 * 也是 <code>String</code> - "OK" 和 "Cancel" - 但它们不必如此。值可以是任何类型的对象。
 *
 * <P>
 * 您使用适当的 getter 方法从资源包中检索对象。因为 "OkKey" 和 "CancelKey"
 * 都是字符串，您会使用 <code>getString</code> 来检索它们：
 * <blockquote>
 * <pre>
 * button1 = new Button(myResources.getString("OkKey"));
 * button2 = new Button(myResources.getString("CancelKey"));
 * </pre>
 * </blockquote>
 * getter 方法都需要键作为参数，并在找到对象时返回该对象。如果未找到对象，getter 方法
 * 将抛出 <code>MissingResourceException</code>。
 *
 * <P>
 * 除了 <code>getString</code>，<code>ResourceBundle</code> 还提供了
 * 获取字符串数组的 <code>getStringArray</code> 方法，以及获取任何其他类型对象的通用 <code>getObject</code> 方法。使用 <code>getObject</code> 时，您需要将结果转换为适当的类型。例如：
 * <blockquote>
 * <pre>
 * int[] myIntegers = (int[]) myResources.getObject("intList");
 * </pre>
 * </blockquote>
 *
 * <P>
 * Java 平台提供了两个 <code>ResourceBundle</code> 的子类，
 * <code>ListResourceBundle</code> 和 <code>PropertyResourceBundle</code>，
 * 它们提供了一种创建资源的相对简单的方法。正如您在前面的示例中简要看到的，<code>ListResourceBundle</code>
 * 将其资源管理为键/值对的列表。<code>PropertyResourceBundle</code> 使用属性文件来管理
 * 其资源。
 *
 * <p>
 * 如果 <code>ListResourceBundle</code> 或 <code>PropertyResourceBundle</code>
 * 不符合您的需求，您可以编写自己的 <code>ResourceBundle</code> 子类。您的子类必须重写两个方法： <code>handleGetObject</code>
 * 和 <code>getKeys()</code>。
 *
 * <p>
 * 如果 <code>ResourceBundle</code> 子类的实现同时被多个线程使用，必须是线程安全的。此类中的非抽象方法的默认实现，以及直接已知的具体子类 <code>ListResourceBundle</code> 和
 * <code>PropertyResourceBundle</code> 中的方法都是线程安全的。
 *
 * <h3>ResourceBundle.Control</h3>
 *
 * {@link ResourceBundle.Control} 类提供了执行 <code>getBundle</code>
 * 工厂方法所需的必要信息，这些方法接受一个 <code>ResourceBundle.Control</code>
 * 实例。您可以实现自己的子类以启用非标准资源包格式，更改搜索策略，或定义缓存参数。请参阅该类的描述和
 * {@link #getBundle(String, Locale, ClassLoader, Control) getBundle}
 * 工厂方法的描述以获取详细信息。
 *
 * <p><a name="modify_default_behavior">对于不接受 {@link Control} 实例的 {@code getBundle} 工厂</a>
 * 方法，可以通过安装的 {@link
 * ResourceBundleControlProvider} 实现来修改其 <a
 * href="#default_behavior"> 默认行为</a> 的资源包加载。任何已安装的提供者都在 {@code ResourceBundle} 类加载时被检测到。如果任何提供者为给定的基本名称提供了一个 {@link
 * Control}，则将使用该 {@link Control} 而不是默认的 {@link Control}。如果安装了多个支持相同基本名称的服务提供者，
 * 将使用 {@link ServiceLoader} 返回的第一个提供者。
 *
 * <h3>缓存管理</h3>
 *
 * 通过 <code>getBundle</code> 工厂方法创建的资源包实例默认情况下会被缓存，并且如果已缓存，工厂方法会多次返回相同的
 * 资源包实例。 <code>getBundle</code> 客户端可以清除缓存，使用生存时间值管理缓存资源包实例的生命周期，
 * 或指定不缓存资源包实例。请参阅 {@linkplain #getBundle(String, Locale, ClassLoader,
 * Control) <code>getBundle</code> 工厂方法}、{@link
 * #clearCache(ClassLoader) clearCache}、{@link
 * Control#getTimeToLive(String, Locale)
 * ResourceBundle.Control.getTimeToLive} 和 {@link
 * Control#needsReload(String, Locale, String, ClassLoader, ResourceBundle,
 * long) ResourceBundle.Control.needsReload} 的描述以获取详细信息。
 *
 * <h3>示例</h3>
 *
 * 以下是一个非常简单的 <code>ResourceBundle</code>
 * 子类 <code>MyResources</code> 的示例，它管理两个资源（对于更多的资源，您可能会使用 <code>Map</code>）。
 * 注意，如果 "父级" <code>ResourceBundle</code> 处理相同的键且值相同，则不需要提供值（如下例中的 okKey）。
 * <blockquote>
 * <pre>
 * // 默认（英语，美国）
 * public class MyResources extends ResourceBundle {
 *     public Object handleGetObject(String key) {
 *         if (key.equals("okKey")) return "Ok";
 *         if (key.equals("cancelKey")) return "Cancel";
 *         return null;
 *     }
 *
 *     public Enumeration&lt;String&gt; getKeys() {
 *         return Collections.enumeration(keySet());
 *     }
 *
 *     // 重写 handleKeySet() 以便 getKeys() 实现
 *     // 可以依赖 keySet() 值。
 *     protected Set&lt;String&gt; handleKeySet() {
 *         return new HashSet&lt;String&gt;(Arrays.asList("okKey", "cancelKey"));
 *     }
 * }
 *
 * // 德语
 * public class MyResources_de extends MyResources {
 *     public Object handleGetObject(String key) {
 *         // 不需要 okKey，因为父级处理它。
 *         if (key.equals("cancelKey")) return "Abbrechen";
 *         return null;
 *     }
 *
 *     protected Set&lt;String&gt; handleKeySet() {
 *         return new HashSet&lt;String&gt;(Arrays.asList("cancelKey"));
 *     }
 * }
 * </pre>
 * </blockquote>
 * 您不必限制自己只使用一个 <code>ResourceBundle</code> 家族。例如，您可以有一组用于异常消息的资源包，<code>ExceptionResources</code>
 * (<code>ExceptionResources_fr</code>, <code>ExceptionResources_de</code>, ...)，
 * 以及一组用于小部件的资源包，<code>WidgetResource</code> (<code>WidgetResources_fr</code>,
 * <code>WidgetResources_de</code>, ...)；根据需要拆分资源。
 *
 * @see ListResourceBundle
 * @see PropertyResourceBundle
 * @see MissingResourceException
 * @since JDK1.1
 */
public abstract class ResourceBundle {

    /** 缓存的初始大小 */
    private static final int INITIAL_CACHE_SIZE = 32;

    /** 表示没有资源包存在的常量 */
    private static final ResourceBundle NONEXISTENT_BUNDLE = new ResourceBundle() {
            public Enumeration<String> getKeys() { return null; }
            protected Object handleGetObject(String key) { return null; }
            public String toString() { return "NONEXISTENT_BUNDLE"; }
        };


    /**
     * 缓存是一个从缓存键（包含资源包基本名称、语言环境和类加载器）到资源包或 NONEXISTENT_BUNDLE 的映射，由
     * BundleReference 包装。
     *
     * 缓存是一个 ConcurrentMap，允许多个线程同时搜索缓存。这也将允许缓存键
     * 与其引用的 ClassLoaders 一起被回收。
     *
     * 此变量最好命名为 "cache"，但由于一些针对 bug 4212439 的变通方法，我们保留了旧的名称以保持兼容性。
     */
    private static final ConcurrentMap<CacheKey, BundleReference> cacheList
        = new ConcurrentHashMap<>(INITIAL_CACHE_SIZE);

    /**
     * 用于引用类加载器或资源包的引用对象队列。
     */
    private static final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();

    /**
     * 返回此包的基本名称，如果已知则返回，否则返回 {@code null}。
     *
     * 如果不为 null，则这是在加载资源包时传递给
     * {@code ResourceBundle.getBundle(...)} 方法的 {@code baseName} 参数的值。
     *
     * @return 资源包的基本名称，由 {@code ResourceBundle.getBundle(...)} 方法提供并期望。
     *
     * @see #getBundle(java.lang.String, java.util.Locale, java.lang.ClassLoader)
     *
     * @since 1.8
     */
    public String getBaseBundleName() {
        return name;
    }

    /**
     * 此包的父包。
     * 当此包不包含特定资源时，{@link #getObject getObject}
     * 会搜索父包。
     */
    protected ResourceBundle parent = null;

    /**
     * 此包的语言环境。
     */
    private Locale locale = null;

    /**
     * 此包的基本名称。
     */
    private String name;

    /**
     * 表示此包在缓存中已过期的标志。
     */
    private volatile boolean expired;

    /**
     * 回退到缓存键的链接。如果此包尚未在
     * 缓存中或已过期，则为 null。
     */
    private volatile CacheKey cacheKey;

    /**
     * 仅包含在此 ResourceBundle 中的键的集合。
     */
    private volatile Set<String> keySet;

    private static final List<ResourceBundleControlProvider> providers;

    static {
        List<ResourceBundleControlProvider> list = null;
        ServiceLoader<ResourceBundleControlProvider> serviceLoaders
                = ServiceLoader.loadInstalled(ResourceBundleControlProvider.class);
        for (ResourceBundleControlProvider provider : serviceLoaders) {
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(provider);
        }
        providers = list;
    }


                /**
     * 唯一的构造函数。 （通常由子类构造函数调用，通常是隐式的。）
     */
    public ResourceBundle() {
    }

    /**
     * 从这个资源包或其父级中获取给定键的字符串。
     * 调用此方法等同于调用
     * <blockquote>
     * <code>(String) {@link #getObject(java.lang.String) getObject}(key)</code>。
     * </blockquote>
     *
     * @param key 所需字符串的键
     * @exception NullPointerException 如果 <code>key</code> 为 <code>null</code>
     * @exception MissingResourceException 如果找不到给定键的对象
     * @exception ClassCastException 如果找到的给定键的对象不是字符串
     * @return 给定键的字符串
     */
    public final String getString(String key) {
        return (String) getObject(key);
    }

    /**
     * 从这个资源包或其父级中获取给定键的字符串数组。
     * 调用此方法等同于调用
     * <blockquote>
     * <code>(String[]) {@link #getObject(java.lang.String) getObject}(key)</code>。
     * </blockquote>
     *
     * @param key 所需字符串数组的键
     * @exception NullPointerException 如果 <code>key</code> 为 <code>null</code>
     * @exception MissingResourceException 如果找不到给定键的对象
     * @exception ClassCastException 如果找到的给定键的对象不是字符串数组
     * @return 给定键的字符串数组
     */
    public final String[] getStringArray(String key) {
        return (String[]) getObject(key);
    }

    /**
     * 从这个资源包或其父级中获取给定键的对象。
     * 此方法首先尝试使用
     * {@link #handleGetObject(java.lang.String) handleGetObject} 从这个资源包中获取对象。
     * 如果不成功，并且父资源包不为 null，
     * 它调用父级的 <code>getObject</code> 方法。
     * 如果仍然不成功，则抛出 MissingResourceException。
     *
     * @param key 所需对象的键
     * @exception NullPointerException 如果 <code>key</code> 为 <code>null</code>
     * @exception MissingResourceException 如果找不到给定键的对象
     * @return 给定键的对象
     */
    public final Object getObject(String key) {
        Object obj = handleGetObject(key);
        if (obj == null) {
            if (parent != null) {
                obj = parent.getObject(key);
            }
            if (obj == null) {
                throw new MissingResourceException("Can't find resource for bundle "
                                                   +this.getClass().getName()
                                                   +", key "+key,
                                                   this.getClass().getName(),
                                                   key);
            }
        }
        return obj;
    }

    /**
     * 返回此资源包的区域设置。此方法可以在调用 getBundle() 之后使用，以确定返回的资源包是否确实对应于请求的区域设置或是一个回退。
     *
     * @return 此资源包的区域设置
     */
    public Locale getLocale() {
        return locale;
    }

    /*
     * 自动确定用于代表客户端加载资源的类加载器。
     */
    private static ClassLoader getLoader(Class<?> caller) {
        ClassLoader cl = caller == null ? null : caller.getClassLoader();
        if (cl == null) {
            // 当调用者的加载器是引导类加载器时，cl 在此处为 null。
            // 在这种情况下，ClassLoader.getSystemClassLoader() 可能会返回应用程序正在使用的相同类加载器。
            // 因此，我们使用一个包装类加载器来为 Java 运行时代表加载的捆绑包创建一个单独的作用域，以防止这些捆绑包从缓存中返回给应用程序（5048280）。
            cl = RBClassLoader.INSTANCE;
        }
        return cl;
    }

    /**
     * 扩展类加载器的包装器
     */
    private static class RBClassLoader extends ClassLoader {
        private static final RBClassLoader INSTANCE = AccessController.doPrivileged(
                    new PrivilegedAction<RBClassLoader>() {
                        public RBClassLoader run() {
                            return new RBClassLoader();
                        }
                    });
        private static final ClassLoader loader;
        static {
            // 查找扩展类加载器。
            ClassLoader ld = ClassLoader.getSystemClassLoader();
            ClassLoader parent;
            while ((parent = ld.getParent()) != null) {
                ld = parent;
            }
            loader = ld;
        }

        private RBClassLoader() {
        }
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (loader != null) {
                return loader.loadClass(name);
            }
            return Class.forName(name);
        }
        public URL getResource(String name) {
            if (loader != null) {
                return loader.getResource(name);
            }
            return ClassLoader.getSystemResource(name);
        }
        public InputStream getResourceAsStream(String name) {
            if (loader != null) {
                return loader.getResourceAsStream(name);
            }
            return ClassLoader.getSystemResourceAsStream(name);
        }
    }

    /**
     * 设置此捆绑包的父级捆绑包。
     * 当此捆绑包不包含特定资源时，父级捆绑包将由 {@link #getObject getObject} 搜索。
     *
     * @param parent 此捆绑包的父级捆绑包。
     */
    protected void setParent(ResourceBundle parent) {
        assert parent != NONEXISTENT_BUNDLE;
        this.parent = parent;
    }

    /**
     * 用于缓存资源包的键。该键检查基础名称、区域设置和类加载器以确定资源是否与请求的资源匹配。加载器可以为 null，但基础名称和区域设置必须具有非 null 值。
     */
    private static class CacheKey implements Cloneable {
        // 这三个是 Map 中查找的实际键。
        private String name;
        private Locale locale;
        private LoaderReference loaderRef;

        // 调用 Control.needsReload() 时需要的捆绑格式。
        private String format;

        // 这些时间值在 CacheKey 中，以便 NONEXISTENT_BUNDLE 不需要为缓存克隆。

        // 捆绑包加载的时间
        private volatile long loadTime;

        // 捆绑包在缓存中过期的时间，或者 Control.TTL_DONT_CACHE 或 Control.TTL_NO_EXPIRATION_CONTROL。
        private volatile long expirationTime;

        // 由 Throwable 报告的错误占位符
        private Throwable cause;

        // 用于避免重新计算此实例的哈希码的哈希码值缓存。
        private int hashCodeCache;

        CacheKey(String baseName, Locale locale, ClassLoader loader) {
            this.name = baseName;
            this.locale = locale;
            if (loader == null) {
                this.loaderRef = null;
            } else {
                loaderRef = new LoaderReference(loader, referenceQueue, this);
            }
            calculateHashCode();
        }

        String getName() {
            return name;
        }

        CacheKey setName(String baseName) {
            if (!this.name.equals(baseName)) {
                this.name = baseName;
                calculateHashCode();
            }
            return this;
        }

        Locale getLocale() {
            return locale;
        }

        CacheKey setLocale(Locale locale) {
            if (!this.locale.equals(locale)) {
                this.locale = locale;
                calculateHashCode();
            }
            return this;
        }

        ClassLoader getLoader() {
            return (loaderRef != null) ? loaderRef.get() : null;
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            try {
                final CacheKey otherEntry = (CacheKey)other;
                // 快速检查它们是否不相等
                if (hashCodeCache != otherEntry.hashCodeCache) {
                    return false;
                }
                // 名称是否相同？
                if (!name.equals(otherEntry.name)) {
                    return false;
                }
                // 区域设置是否相同？
                if (!locale.equals(otherEntry.locale)) {
                    return false;
                }
                // 引用（两者非空）或（两者都为空）？
                if (loaderRef == null) {
                    return otherEntry.loaderRef == null;
                }
                ClassLoader loader = loaderRef.get();
                return (otherEntry.loaderRef != null)
                        // 如果引用为 null，我们无法再找出引用的类加载器；因此，将其视为不相等
                        && (loader != null)
                        && (loader == otherEntry.loaderRef.get());
            } catch (    NullPointerException | ClassCastException e) {
            }
            return false;
        }

        public int hashCode() {
            return hashCodeCache;
        }

        private void calculateHashCode() {
            hashCodeCache = name.hashCode() << 3;
            hashCodeCache ^= locale.hashCode();
            ClassLoader loader = getLoader();
            if (loader != null) {
                hashCodeCache ^= loader.hashCode();
            }
        }

        public Object clone() {
            try {
                CacheKey clone = (CacheKey) super.clone();
                if (loaderRef != null) {
                    clone.loaderRef = new LoaderReference(loaderRef.get(),
                                                          referenceQueue, clone);
                }
                // 清除对 Throwable 的引用
                clone.cause = null;
                return clone;
            } catch (CloneNotSupportedException e) {
                // 这不应该发生
                throw new InternalError(e);
            }
        }

        String getFormat() {
            return format;
        }

        void setFormat(String format) {
            this.format = format;
        }

        private void setCause(Throwable cause) {
            if (this.cause == null) {
                this.cause = cause;
            } else {
                // 如果之前的错误是 ClassNotFoundException，则覆盖原因。
                if (this.cause instanceof ClassNotFoundException) {
                    this.cause = cause;
                }
            }
        }

        private Throwable getCause() {
            return cause;
        }

        public String toString() {
            String l = locale.toString();
            if (l.length() == 0) {
                if (locale.getVariant().length() != 0) {
                    l = "__" + locale.getVariant();
                } else {
                    l = "\"\"";
                }
            }
            return "CacheKey[" + name + ", lc=" + l + ", ldr=" + getLoader()
                + "(format=" + format + ")]";
        }
    }

    /**
     * 在 LoaderReference 和 BundleReference 中获取 CacheKey 的通用接口。
     */
    private static interface CacheKeyReference {
        public CacheKey getCacheKey();
    }

    /**
     * 对类加载器的引用是弱引用，以便在没有其他地方使用它们时可以被垃圾回收。ResourceBundle 类没有理由保持类加载器的存活。
     */
    private static class LoaderReference extends WeakReference<ClassLoader>
                                         implements CacheKeyReference {
        private CacheKey cacheKey;

        LoaderReference(ClassLoader referent, ReferenceQueue<Object> q, CacheKey key) {
            super(referent, q);
            cacheKey = key;
        }

        public CacheKey getCacheKey() {
            return cacheKey;
        }
    }

    /**
     * 对捆绑包的引用是软引用，以便在没有硬引用时可以被垃圾回收。
     */
    private static class BundleReference extends SoftReference<ResourceBundle>
                                         implements CacheKeyReference {
        private CacheKey cacheKey;

        BundleReference(ResourceBundle referent, ReferenceQueue<Object> q, CacheKey key) {
            super(referent, q);
            cacheKey = key;
        }

        public CacheKey getCacheKey() {
            return cacheKey;
        }
    }

    /**
     * 使用指定的基础名称、默认区域设置和调用者的类加载器获取资源包。调用此方法等同于调用
     * <blockquote>
     * <code>getBundle(baseName, Locale.getDefault(), this.getClass().getClassLoader())</code>，
     * </blockquote>
     * 除了 <code>getClassLoader()</code> 是以 <code>ResourceBundle</code> 的安全权限运行的。
     * 有关搜索和实例化策略的完整描述，请参阅 {@link #getBundle(String, Locale, ClassLoader) getBundle}。
     *
     * @param baseName 资源包的基础名称，一个完全限定的类名称
     * @exception java.lang.NullPointerException
     *     如果 <code>baseName</code> 为 <code>null</code>
     * @exception MissingResourceException
     *     如果找不到指定基础名称的资源包
     * @return 给定基础名称和默认区域设置的资源包
     */
    @CallerSensitive
    public static final ResourceBundle getBundle(String baseName)
    {
        return getBundleImpl(baseName, Locale.getDefault(),
                             getLoader(Reflection.getCallerClass()),
                             getDefaultControl(baseName));
    }

    /**
     * 使用指定的基础名称、默认区域设置和指定的控制返回资源包。调用此方法等同于调用
     * <pre>
     * getBundle(baseName, Locale.getDefault(),
     *           this.getClass().getClassLoader(), control),
     * </pre>
     * 除了 <code>getClassLoader()</code> 是以 <code>ResourceBundle</code> 的安全权限运行的。有关使用 <code>ResourceBundle.Control</code> 的资源包加载过程的完整描述，请参阅 {@link
     * #getBundle(String, Locale, ClassLoader, Control) getBundle}。
     *
     * @param baseName
     *        资源包的基础名称，一个完全限定的类名称
     * @param control
     *        提供资源包加载过程信息的控制
     * @return 给定基础名称和默认区域设置的资源包
     * @exception NullPointerException
     *        如果 <code>baseName</code> 或 <code>control</code> 为
     *        <code>null</code>
     * @exception MissingResourceException
     *        如果找不到指定基础名称的资源包
     * @exception IllegalArgumentException
     *        如果给定的 <code>control</code> 表现不正常
     *        （例如，<code>control.getCandidateLocales</code> 返回 null。）
     *        请注意，<code>control</code> 的验证是按需进行的。
     * @since 1.6
     */
    @CallerSensitive
    public static final ResourceBundle getBundle(String baseName,
                                                 Control control) {
        return getBundleImpl(baseName, Locale.getDefault(),
                             getLoader(Reflection.getCallerClass()),
                             control);
    }


                /**
     * 使用指定的基本名称和区域设置获取资源包，
     * 以及调用者的类加载器。调用此方法等同于调用
     * <blockquote>
     * <code>getBundle(baseName, locale, this.getClass().getClassLoader())</code>,
     * </blockquote>
     * 除了 <code>getClassLoader()</code> 是以 <code>ResourceBundle</code> 的安全权限运行的。
     * 有关搜索和实例化策略的完整描述，请参见 {@link #getBundle(String, Locale, ClassLoader) getBundle}。
     *
     * @param baseName
     *        资源包的基本名称，一个完全限定的类名
     * @param locale
     *        所需的区域设置
     * @exception NullPointerException
     *        如果 <code>baseName</code> 或 <code>locale</code> 为 <code>null</code>
     * @exception MissingResourceException
     *        如果找不到指定基本名称的资源包
     * @return 给定基本名称和区域设置的资源包
     */
    @CallerSensitive
    public static final ResourceBundle getBundle(String baseName,
                                                 Locale locale)
    {
        return getBundleImpl(baseName, locale,
                             getLoader(Reflection.getCallerClass()),
                             getDefaultControl(baseName));
    }

    /**
     * 使用指定的基本名称、目标区域设置和控制，以及调用者的类加载器返回资源包。调用此
     * 方法等同于调用
     * <pre>
     * getBundle(baseName, targetLocale, this.getClass().getClassLoader(),
     *           control),
     * </pre>
     * 除了 <code>getClassLoader()</code> 是以 <code>ResourceBundle</code> 的安全权限运行的。有关使用 <code>ResourceBundle.Control</code> 的资源包加载过程的完整描述，请参见 {@link
     * #getBundle(String, Locale, ClassLoader, Control) getBundle}。
     *
     * @param baseName
     *        资源包的基本名称，一个完全限定的类名
     * @param targetLocale
     *        所需的区域设置
     * @param control
     *        提供资源包加载过程信息的控制
     * @return 给定基本名称和 <code>Locale</code> 列表中的一个区域设置的资源包
     * @exception NullPointerException
     *        如果 <code>baseName</code>、<code>locales</code> 或
     *        <code>control</code> 为 <code>null</code>
     * @exception MissingResourceException
     *        如果在 <code>locales</code> 中的任何区域设置中都找不到指定基本名称的资源包。
     * @exception IllegalArgumentException
     *        如果给定的 <code>control</code> 无法正常工作
     *        （例如，<code>control.getCandidateLocales</code> 返回 null。）
     *        注意，<code>control</code> 的验证是按需进行的。
     * @since 1.6
     */
    @CallerSensitive
    public static final ResourceBundle getBundle(String baseName, Locale targetLocale,
                                                 Control control) {
        return getBundleImpl(baseName, targetLocale,
                             getLoader(Reflection.getCallerClass()),
                             control);
    }

    /**
     * 使用指定的基本名称、区域设置和类加载器获取资源包。
     *
     * <p>此方法的行为与调用
     * {@link #getBundle(String, Locale, ClassLoader, Control)} 传递一个
     * 默认的 {@link Control} 实例相同，除非通过 {@link ResourceBundleControlProvider} SPI 提供了另一个 {@link Control}。有关修改默认行为的描述，请参见 <a href="#modify_default_behavior">修改默认行为</a>。
     *
     * <p><a name="default_behavior">以下描述了默认行为</a>。
     *
     * <p><code>getBundle</code> 使用基本名称、指定的区域设置和默认区域设置（从 {@link java.util.Locale#getDefault()
     * Locale.getDefault} 获取）生成一系列 <a
     * name="candidates"><em>候选包名称</em></a>。如果指定的区域设置的语言、脚本、国家和变体都是空字符串，
     * 则基本名称是唯一的候选包名称。否则，从指定区域设置的属性值（语言、脚本、国家和变体）生成候选区域设置列表并附加到基本名称。通常，这看起来像以下内容：
     *
     * <pre>
     *     baseName + "_" + language + "_" + script + "_" + country + "_" + variant
     *     baseName + "_" + language + "_" + script + "_" + country
     *     baseName + "_" + language + "_" + script
     *     baseName + "_" + language + "_" + country + "_" + variant
     *     baseName + "_" + language + "_" + country
     *     baseName + "_" + language
     * </pre>
     *
     * <p>如果候选包名称的最后一个组件是空字符串，则省略该组件及其前面的下划线。例如，如果国家是空字符串，则上面的第二个和第五个候选包名称将被省略。同样，如果脚本是空字符串，则包含脚本的候选名称将被省略。例如，语言为 "de" 且变体为 "JAVA" 的区域设置将生成以下基本名称为 "MyResource" 的候选名称。
     *
     * <pre>
     *     MyResource_de__JAVA
     *     MyResource_de
     * </pre>
     *
     * 如果变体包含一个或多个下划线 ('_')，则通过截断最后一个下划线及其后面的部分生成的包名称序列将插入到具有原始变体的候选包名称之后。例如，对于语言为 "en"、脚本为 "Latn"、国家为 "US" 且变体为 "WINDOWS_VISTA" 的区域设置，以及包基本名称为 "MyResource" 的情况，将生成以下候选包名称列表：
     *
     * <pre>
     * MyResource_en_Latn_US_WINDOWS_VISTA
     * MyResource_en_Latn_US_WINDOWS
     * MyResource_en_Latn_US
     * MyResource_en_Latn
     * MyResource_en_US_WINDOWS_VISTA
     * MyResource_en_US_WINDOWS
     * MyResource_en_US
     * MyResource_en
     * </pre>
     *
     * <blockquote><b>注意：</b>对于某些 <code>Locale</code>，候选包名称列表包含额外的名称，或者包名称的顺序略有修改。有关详细信息，请参见默认实现的描述 {@link Control#getCandidateLocales(String, Locale)
     * getCandidateLocales}。</blockquote>
     *
     * <p><code>getBundle</code> 然后遍历候选包名称，以找到可以 <em>实例化</em> 的第一个实际资源包。它使用默认控制的 {@link Control#getFormats
     * getFormats} 方法，该方法为每个生成的名称生成两个包名称，第一个是类名称，第二个是属性文件名称。对于每个候选包名称，它尝试创建一个资源包：
     *
     * <ul><li>首先，它尝试使用生成的类名称加载一个类。如果可以使用指定的类加载器找到并加载此类，且该类与 ResourceBundle 兼容、可以从 ResourceBundle 访问且可以实例化，则 <code>getBundle</code> 创建此类的新实例并将其用作 <em>结果资源包</em>。
     *
     * <li>否则，<code>getBundle</code> 尝试使用生成的属性文件名称定位属性资源文件。它通过将候选包名称中的所有 "." 字符替换为 "/" 并附加字符串 ".properties" 来生成路径名称。它尝试使用 {@link
     * java.lang.ClassLoader#getResource(java.lang.String)
     * ClassLoader.getResource} 查找具有此名称的 "资源"。（请注意，这里的 "资源" 与资源包的内容无关，它只是一个数据容器，例如文件。）如果找到 "资源"，它尝试从其内容创建一个新的 {@link
     * PropertyResourceBundle} 实例。如果成功，此实例将成为 <em>结果资源包</em>。 </ul>
     *
     * <p>这会一直持续到实例化结果资源包或候选包名称列表耗尽。如果找不到匹配的资源包，将调用默认控制的 {@link Control#getFallbackLocale
     * getFallbackLocale} 方法，该方法返回当前的默认区域设置。然后使用此区域设置生成新的候选区域设置名称列表并再次搜索，如上所述。
     *
     * <p>如果仍然找不到结果包，将单独查找基本名称。如果这仍然失败，将抛出 <code>MissingResourceException</code>。
     *
     * <p><a name="parent_chain"> 一旦找到结果资源包，其 <em>父链</em> 就会被实例化</a>。如果结果包已经有一个父包（可能是因为它是从缓存中返回的），则链已完成。
     *
     * <p>否则，<code>getBundle</code> 检查在生成结果资源包时使用的候选区域设置列表的其余部分。（如前所述，候选包名称的最后一个组件为空字符串的名称将被省略。）当到达候选列表的末尾时，它尝试使用普通包名称。对于每个候选包名称，它尝试实例化一个资源包（首先查找类，然后查找属性文件，如上所述）。
     *
     * <p>每当它成功时，它都会调用先前实例化的资源包的 {@link #setParent(java.util.ResourceBundle) setParent} 方法，将新的资源包传递给它。这会一直持续到名称列表耗尽或当前包已经有一个非空的父包。
     *
     * <p>一旦父链完成，包将被返回。
     *
     * <p><b>注意：</b> <code>getBundle</code> 会缓存实例化的资源包，并且可能会多次返回相同的资源包实例。
     *
     * <p><b>注意：</b><code>baseName</code> 参数应该是完全限定的类名。然而，为了与早期版本的兼容性，Sun 的 Java SE 运行时环境不会验证这一点，因此可以通过指定路径名称（使用 "/"）而不是完全限定的类名（使用
     * "."）来访问 <code>PropertyResourceBundle</code>s。
     *
     * <p><a name="default_behavior_example">
     * <strong>示例：</strong></a>
     * <p>
     * 提供了以下类和属性文件：
     * <pre>
     *     MyResources.class
     *     MyResources.properties
     *     MyResources_fr.properties
     *     MyResources_fr_CH.class
     *     MyResources_fr_CH.properties
     *     MyResources_en.properties
     *     MyResources_es_ES.class
     * </pre>
     *
     * 所有文件的内容都是有效的（即，".class" 文件是 <code>ResourceBundle</code> 的公共非抽象子类，".properties" 文件在语法上是正确的）。默认区域设置为 <code>Locale("en", "GB")</code>。
     *
     * <p>使用以下区域设置参数调用 <code>getBundle</code> 将实例化资源包如下：
     *
     * <table summary="getBundle() locale to resource bundle mapping">
     * <tr><td>Locale("fr", "CH")</td><td>MyResources_fr_CH.class, 父包 MyResources_fr.properties, 父包 MyResources.class</td></tr>
     * <tr><td>Locale("fr", "FR")</td><td>MyResources_fr.properties, 父包 MyResources.class</td></tr>
     * <tr><td>Locale("de", "DE")</td><td>MyResources_en.properties, 父包 MyResources.class</td></tr>
     * <tr><td>Locale("en", "US")</td><td>MyResources_en.properties, 父包 MyResources.class</td></tr>
     * <tr><td>Locale("es", "ES")</td><td>MyResources_es_ES.class, 父包 MyResources.class</td></tr>
     * </table>
     *
     * <p>文件 MyResources_fr_CH.properties 从未使用，因为它被 MyResources_fr_CH.class 隐藏。同样，MyResources.properties 也被 MyResources.class 隐藏。
     *
     * @param baseName 资源包的基本名称，一个完全限定的类名
     * @param locale 所需的区域设置
     * @param loader 从中加载资源包的类加载器
     * @return 给定基本名称和区域设置的资源包
     * @exception java.lang.NullPointerException
     *        如果 <code>baseName</code>、<code>locale</code> 或 <code>loader</code> 为 <code>null</code>
     * @exception MissingResourceException
     *        如果找不到指定基本名称的资源包
     * @since 1.2
     */
    public static ResourceBundle getBundle(String baseName, Locale locale,
                                           ClassLoader loader)
    {
        if (loader == null) {
            throw new NullPointerException();
        }
        return getBundleImpl(baseName, locale, loader, getDefaultControl(baseName));
    }

    /**
     * 使用指定的基本名称、目标区域设置、类加载器和控制返回资源包。与没有 <code>control</code> 参数的 {@linkplain
     * #getBundle(String, Locale, ClassLoader) <code>getBundle</code>
     * 工厂方法不同}，给定的 <code>control</code> 指定了如何定位和实例化资源包。概念上，使用给定的 <code>control</code> 加载包的过程按以下步骤执行。
     *
     * <ol>
     * <li>此工厂方法在缓存中查找指定的 <code>baseName</code>、<code>targetLocale</code> 和
     * <code>loader</code> 的资源包。如果在缓存中找到了请求的资源包实例，且该实例及其所有父实例的时间生存期均未过期，则将该实例返回给调用者。否则，此工厂方法继续执行以下加载过程。</li>
     *
     * <li>调用 {@link ResourceBundle.Control#getFormats(String)
     * control.getFormats} 方法以获取用于生成包或资源名称的资源包格式。字符串
     * <code>"java.class"</code> 和 <code>"java.properties"</code>
     * 分别指定基于类和 {@linkplain PropertyResourceBundle
     * 属性} 的资源包。其他以 <code>"java."</code> 开头的字符串保留用于未来的扩展，不得用于应用程序定义的格式。其他字符串指定应用程序定义的格式。</li>
     *
     * <li>调用 {@link ResourceBundle.Control#getCandidateLocales(String,
     * Locale) control.getCandidateLocales} 方法，使用目标区域设置获取一个 <em>候选 <code>Locale</code>s</em> 列表，用于搜索资源包。</li>
     *
     * <li>调用 {@link ResourceBundle.Control#newBundle(String, Locale,
     * String, ClassLoader, boolean) control.newBundle} 方法，为基本包名称、候选区域设置和格式实例化一个 <code>ResourceBundle</code>。（有关缓存查找的说明，请参见下文。）此步骤将迭代所有候选区域设置和格式的组合，直到 <code>newBundle</code> 方法返回一个 <code>ResourceBundle</code> 实例或迭代使用了所有组合。例如，如果候选区域设置为 <code>Locale("de", "DE")</code>、<code>Locale("de")</code> 和
     * <code>Locale("")</code> 且格式为 <code>"java.class"</code> 和 <code>"java.properties"</code>，则以下为调用
     * <code>control.newBundle</code> 时使用的区域设置-格式组合序列。
     *
     * <table style="width: 50%; text-align: left; margin-left: 40px;"
     *  border="0" cellpadding="2" cellspacing="2" summary="locale-format combinations for newBundle">
     * <tbody>
     * <tr>
     * <td
     * style="vertical-align: top; text-align: left; font-weight: bold; width: 50%;"><code>Locale</code><br>
     * </td>
     * <td
     * style="vertical-align: top; text-align: left; font-weight: bold; width: 50%;"><code>format</code><br>
     * </td>
     * </tr>
     * <tr>
     * <td style="vertical-align: top; width: 50%;"><code>Locale("de", "DE")</code><br>
     * </td>
     * <td style="vertical-align: top; width: 50%;"><code>java.class</code><br>
     * </td>
     * </tr>
     * <tr>
     * <td style="vertical-align: top; width: 50%;"><code>Locale("de", "DE")</code></td>
     * <td style="vertical-align: top; width: 50%;"><code>java.properties</code><br>
     * </td>
     * </tr>
     * <tr>
     * <td style="vertical-align: top; width: 50%;"><code>Locale("de")</code></td>
     * <td style="vertical-align: top; width: 50%;"><code>java.class</code></td>
     * </tr>
     * <tr>
     * <td style="vertical-align: top; width: 50%;"><code>Locale("de")</code></td>
     * <td style="vertical-align: top; width: 50%;"><code>java.properties</code></td>
     * </tr>
     * <tr>
     * <td style="vertical-align: top; width: 50%;"><code>Locale("")</code><br>
     * </td>
     * <td style="vertical-align: top; width: 50%;"><code>java.class</code></td>
     * </tr>
     * <tr>
     * <td style="vertical-align: top; width: 50%;"><code>Locale("")</code></td>
     * <td style="vertical-align: top; width: 50%;"><code>java.properties</code></td>
     * </tr>
     * </tbody>
     * </table>
     * </li>
     *
     * <li>如果上一步没有找到资源包，继续执行第 6 步。如果找到的包是基础包（<code>Locale("")</code> 的包），且候选区域设置列表仅包含 <code>Locale("")</code>，则将包返回给调用者。如果找到的包是基础包，但候选区域设置列表包含其他区域设置（除了 Locale("")），将包保留并继续执行第 6 步。如果找到的包不是基础包，继续执行第 7 步。</li>
     *
     * <li>调用 {@link ResourceBundle.Control#getFallbackLocale(String,
     * Locale) control.getFallbackLocale} 方法，获取一个备用区域设置（当前目标区域设置的替代）以进一步查找资源包。如果该方法返回非空区域设置，它将成为下一个目标区域设置，加载过程将从第 3 步重新开始。否则，如果在之前的第 5 步中找到了基础包并保留，则现在将其返回给调用者。否则，将抛出 <code>MissingResourceException</code>。</li>
     *
     * <li>此时，我们找到了一个不是基础包的资源包。如果此包在实例化期间设置了其父包，则将其返回给调用者。否则，基于找到此包的候选区域设置列表实例化其 <a
     * href="./ResourceBundle.html#parent_chain">父链</a>。最后，将包返回给调用者。</li>
     * </ol>
     *
     * <p>在上述资源包加载过程中，此工厂方法在调用 {@link
     * Control#newBundle(String, Locale, String, ClassLoader, boolean)
     * control.newBundle} 方法之前查找缓存。如果在缓存中找到的资源包的时间生存期已过期，工厂方法将调用 {@link ResourceBundle.Control#needsReload(String, Locale,
     * String, ClassLoader, ResourceBundle, long) control.needsReload}
     * 方法以确定是否需要重新加载资源包。如果需要重新加载，工厂方法将调用
     * <code>control.newBundle</code> 以重新加载资源包。如果
     * <code>control.newBundle</code> 返回 <code>null</code>，工厂方法将在缓存中放置一个虚拟资源包作为不存在资源包的标记，以避免后续请求的查找开销。这些虚拟资源包的过期控制与 <code>control</code> 指定的相同。
     *
     * <p>所有加载的资源包默认都会被缓存。有关详细信息，请参见
     * {@link Control#getTimeToLive(String,Locale)
     * control.getTimeToLive}。
     *
     * <p>以下是一个使用默认 <code>ResourceBundle.Control</code> 实现的包加载过程示例。
     *
     * <p>条件：
     * <ul>
     * <li>基础包名称： <code>foo.bar.Messages</code>
     * <li>请求的 <code>Locale</code>： {@link Locale#ITALY}</li>
     * <li>默认 <code>Locale</code>： {@link Locale#FRENCH}</li>
     * <li>可用的资源包：
     * <code>foo/bar/Messages_fr.properties</code> 和
     * <code>foo/bar/Messages.properties</code></li>
     * </ul>
     *
     * <p>首先，<code>getBundle</code> 尝试按以下顺序加载资源包。
     *
     * <ul>
     * <li>类 <code>foo.bar.Messages_it_IT</code>
     * <li>文件 <code>foo/bar/Messages_it_IT.properties</code>
     * <li>类 <code>foo.bar.Messages_it</code></li>
     * <li>文件 <code>foo/bar/Messages_it.properties</code></li>
     * <li>类 <code>foo.bar.Messages</code></li>
     * <li>文件 <code>foo/bar/Messages.properties</code></li>
     * </ul>
     *
     * <p>此时，<code>getBundle</code> 找到了
     * <code>foo/bar/Messages.properties</code>，但由于它是基础包，因此将其保留。 <code>getBundle</code> 调用 {@link
     * Control#getFallbackLocale(String, Locale)
     * control.getFallbackLocale("foo.bar.Messages", Locale.ITALY)}，该方法返回 <code>Locale.FRENCH</code>。接下来，<code>getBundle</code> 尝试按以下顺序加载包。
     *
     * <ul>
     * <li>类 <code>foo.bar.Messages_fr</code></li>
     * <li>文件 <code>foo/bar/Messages_fr.properties</code></li>
     * <li>类 <code>foo.bar.Messages</code></li>
     * <li>文件 <code>foo/bar/Messages.properties</code></li>
     * </ul>
     *
     * <p><code>getBundle</code> 找到了
     * <code>foo/bar/Messages_fr.properties</code> 并创建了一个 <code>ResourceBundle</code> 实例。然后，<code>getBundle</code>
     * 从候选区域设置列表中设置其父链。在列表中仅找到 <code>foo/bar/Messages.properties</code>，<code>getBundle</code> 创建一个 <code>ResourceBundle</code> 实例，该实例成为
     * <code>foo/bar/Messages_fr.properties</code> 实例的父包。
     *
     * @param baseName
     *        资源包的基本名称，一个完全限定的类名
     * @param targetLocale
     *        所需的区域设置
     * @param loader
     *        从中加载资源包的类加载器
     * @param control
     *        提供资源包加载过程信息的控制
     * @return 给定基本名称和区域设置的资源包
     * @exception NullPointerException
     *        如果 <code>baseName</code>、<code>targetLocale</code>、
     *        <code>loader</code> 或 <code>control</code> 为
     *        <code>null</code>
     * @exception MissingResourceException
     *        如果找不到指定基本名称的资源包
     * @exception IllegalArgumentException
     *        如果给定的 <code>control</code> 无法正常工作
     *        （例如，<code>control.getCandidateLocales</code> 返回 null。）
     *        注意，<code>control</code> 的验证是按需进行的。
     * @since 1.6
     */
    public static ResourceBundle getBundle(String baseName, Locale targetLocale,
                                           ClassLoader loader, Control control) {
        if (loader == null || control == null) {
            throw new NullPointerException();
        }
        return getBundleImpl(baseName, targetLocale, loader, control);
    }


                private static Control getDefaultControl(String baseName) {
        if (providers != null) {
            for (ResourceBundleControlProvider provider : providers) {
                Control control = provider.getControl(baseName);
                if (control != null) {
                    return control;
                }
            }
        }
        return Control.INSTANCE;
    }

    private static ResourceBundle getBundleImpl(String baseName, Locale locale,
                                                ClassLoader loader, Control control) {
        if (locale == null || control == null) {
            throw new NullPointerException();
        }

        // 我们在这里为此次调用创建一个 CacheKey。基础名称和加载器在加载过程中不会改变。
        // 我们必须确保在将其用作缓存键之前设置 locale。
        CacheKey cacheKey = new CacheKey(baseName, locale, loader);
        ResourceBundle bundle = null;

        // 快速查找缓存。
        BundleReference bundleRef = cacheList.get(cacheKey);
        if (bundleRef != null) {
            bundle = bundleRef.get();
            bundleRef = null;
        }

        // 如果此 bundle 及其所有父 bundle 有效（未过期），则返回此 bundle。
        // 如果任何 bundle 已过期，我们不会在这里调用 control.needsReload，而是进入下面的完整加载过程。
        if (isValidBundle(bundle) && hasValidParentChain(bundle)) {
            return bundle;
        }

        // 在缓存中未找到有效的 bundle，因此我们需要加载 resource bundle 及其父 bundle。

        boolean isKnownControl = (control == Control.INSTANCE) ||
                                   (control instanceof SingleFormatControl);
        List<String> formats = control.getFormats(baseName);
        if (!isKnownControl && !checkList(formats)) {
            throw new IllegalArgumentException("无效的 Control: getFormats");
        }

        ResourceBundle baseBundle = null;
        for (Locale targetLocale = locale;
             targetLocale != null;
             targetLocale = control.getFallbackLocale(baseName, targetLocale)) {
            List<Locale> candidateLocales = control.getCandidateLocales(baseName, targetLocale);
            if (!isKnownControl && !checkList(candidateLocales)) {
                throw new IllegalArgumentException("无效的 Control: getCandidateLocales");
            }

            bundle = findBundle(cacheKey, candidateLocales, formats, 0, control, baseBundle);

            // 如果加载的 bundle 是基础 bundle 且正好是请求的 locale 或唯一的候选 locale，
            // 则将此 bundle 作为结果。如果加载的 bundle 是基础 bundle，我们将其保留，直到处理完所有回退 locale。
            if (isValidBundle(bundle)) {
                boolean isBaseBundle = Locale.ROOT.equals(bundle.locale);
                if (!isBaseBundle || bundle.locale.equals(locale)
                    || (candidateLocales.size() == 1
                        && bundle.locale.equals(candidateLocales.get(0)))) {
                    break;
                }

                // 如果已加载基础 bundle，将其引用保留在 baseBundle 中，以避免在控制指定不缓存 bundle 时进行冗余加载。
                if (isBaseBundle && baseBundle == null) {
                    baseBundle = bundle;
                }
            }
        }

        if (bundle == null) {
            if (baseBundle == null) {
                throwMissingResourceException(baseName, locale, cacheKey.getCause());
            }
            bundle = baseBundle;
        }

        keepAlive(loader);
        return bundle;
    }

    /**
     * 保持参数 ClassLoader 的活动状态。
     */
    private static void keepAlive(ClassLoader loader){
        // 什么都不做。
    }

    /**
     * 检查给定的 <code>List</code> 是否不为空，不为空且不包含空元素。
     */
    private static boolean checkList(List<?> a) {
        boolean valid = (a != null && !a.isEmpty());
        if (valid) {
            int size = a.size();
            for (int i = 0; valid && i < size; i++) {
                valid = (a.get(i) != null);
            }
        }
        return valid;
    }

    private static ResourceBundle findBundle(CacheKey cacheKey,
                                             List<Locale> candidateLocales,
                                             List<String> formats,
                                             int index,
                                             Control control,
                                             ResourceBundle baseBundle) {
        Locale targetLocale = candidateLocales.get(index);
        ResourceBundle parent = null;
        if (index != candidateLocales.size() - 1) {
            parent = findBundle(cacheKey, candidateLocales, formats, index + 1,
                                control, baseBundle);
        } else if (baseBundle != null && Locale.ROOT.equals(targetLocale)) {
            return baseBundle;
        }

        // 在进行实际加载工作之前，检查是否需要进行一些清理工作：如果类加载器或资源 bundle 的引用已被置为 null，
        // 从缓存中移除所有相关信息。
        Object ref;
        while ((ref = referenceQueue.poll()) != null) {
            cacheList.remove(((CacheKeyReference)ref).getCacheKey());
        }

        // 标志表示资源 bundle 在缓存中已过期
        boolean expiredBundle = false;

        // 首先，在不尝试加载 bundle 的情况下，查找缓存以查看是否在缓存中。
        cacheKey.setLocale(targetLocale);
        ResourceBundle bundle = findBundleInCache(cacheKey, control);
        if (isValidBundle(bundle)) {
            expiredBundle = bundle.expired;
            if (!expiredBundle) {
                // 如果其父 bundle 是候选 locale（运行时查找路径）请求的，我们可以使用缓存的 bundle。
                // （如果它们不一致，我们需要检查父 bundle 的父 bundle 以与请求的一致。）
                if (bundle.parent == parent) {
                    return bundle;
                }
                // 否则，移除缓存的 bundle，因为我们不能保留具有不同父 bundle 的相同 bundle。
                BundleReference bundleRef = cacheList.get(cacheKey);
                if (bundleRef != null && bundleRef.get() == bundle) {
                    cacheList.remove(cacheKey, bundleRef);
                }
            }
        }

        if (bundle != NONEXISTENT_BUNDLE) {
            CacheKey constKey = (CacheKey) cacheKey.clone();

            try {
                bundle = loadBundle(cacheKey, formats, control, expiredBundle);
                if (bundle != null) {
                    if (bundle.parent == null) {
                        bundle.setParent(parent);
                    }
                    bundle.locale = targetLocale;
                    bundle = putBundleInCache(cacheKey, bundle, control);
                    return bundle;
                }

                // 将 NONEXISTENT_BUNDLE 放入缓存中，作为没有该 locale 的 bundle 实例的标记。
                putBundleInCache(cacheKey, NONEXISTENT_BUNDLE, control);
            } finally {
                if (constKey.getCause() instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return parent;
    }

    private static ResourceBundle loadBundle(CacheKey cacheKey,
                                             List<String> formats,
                                             Control control,
                                             boolean reload) {

        // 这里我们按照 getFormats() 返回的格式顺序实际加载 bundle。
        Locale targetLocale = cacheKey.getLocale();

        ResourceBundle bundle = null;
        int size = formats.size();
        for (int i = 0; i < size; i++) {
            String format = formats.get(i);
            try {
                bundle = control.newBundle(cacheKey.getName(), targetLocale, format,
                                           cacheKey.getLoader(), reload);
            } catch (LinkageError error) {
                // 需要处理 LinkageError 情况，因为 ClassLoader 中存在不一致的大小写敏感性。
                // 详情见 6572242。
                cacheKey.setCause(error);
            } catch (Exception cause) {
                cacheKey.setCause(cause);
            }
            if (bundle != null) {
                // 在缓存键中设置格式，以便在调用 needsReload 时使用。
                cacheKey.setFormat(format);
                bundle.name = cacheKey.getName();
                bundle.locale = targetLocale;
                // Bundle 提供者可能会重用实例。因此，我们需要在这里清除过期标志。
                bundle.expired = false;
                break;
            }
        }

        return bundle;
    }

    private static boolean isValidBundle(ResourceBundle bundle) {
        return bundle != null && bundle != NONEXISTENT_BUNDLE;
    }

    /**
     * 确定资源 bundle 的父链中，包括叶子节点，是否有任何已过期的 bundle。
     */
    private static boolean hasValidParentChain(ResourceBundle bundle) {
        long now = System.currentTimeMillis();
        while (bundle != null) {
            if (bundle.expired) {
                return false;
            }
            CacheKey key = bundle.cacheKey;
            if (key != null) {
                long expirationTime = key.expirationTime;
                if (expirationTime >= 0 && expirationTime <= now) {
                    return false;
                }
            }
            bundle = bundle.parent;
        }
        return true;
    }

    /**
     * 抛出带有适当消息的 MissingResourceException
     */
    private static void throwMissingResourceException(String baseName,
                                                      Locale locale,
                                                      Throwable cause) {
        // 如果 cause 是 MissingResourceException，避免创建长链。 (6355009)
        if (cause instanceof MissingResourceException) {
            cause = null;
        }
        throw new MissingResourceException("找不到基础名称为 " + baseName + "，locale 为 " + locale + " 的 bundle",
                                           baseName + "_" + locale, // className
                                           "",                      // key
                                           cause);
    }

    /**
     * 在缓存中查找 bundle。任何已过期的 bundle 在返回时都会被标记为 `已过期` 并从缓存中移除。
     *
     * @param cacheKey 用于查找缓存的键
     * @param control 用于过期控制的 Control
     * @return 缓存中的 bundle，或如果未在缓存中找到 bundle 或其父 bundle 已过期，则返回 null。
     * <code>bundle.expired</code> 在返回时为 true，表示缓存中的 bundle 已过期。
     */
    private static ResourceBundle findBundleInCache(CacheKey cacheKey,
                                                    Control control) {
        BundleReference bundleRef = cacheList.get(cacheKey);
        if (bundleRef == null) {
            return null;
        }
        ResourceBundle bundle = bundleRef.get();
        if (bundle == null) {
            return null;
        }
        ResourceBundle p = bundle.parent;
        assert p != NONEXISTENT_BUNDLE;
        // 如果父 bundle 已过期，则此 bundle 也必须过期。我们只检查直接的父 bundle，因为实际加载是从根（基础）到叶（子）进行的，
        // 检查的目的是将过期信息向叶传播。例如，如果请求的 locale 是 ja_JP_JP，且所有候选 locale 的 bundle 都在缓存中，我们有列表，
        //
        // base <- ja <- ja_JP <- ja_JP_JP
        //
        // 如果 ja 已过期，则将重新加载 ja，列表将变成树。
        //
        // base <- ja (新的)
        //  "   <- ja (已过期) <- ja_JP <- ja_JP_JP
        //
        // 当在缓存中查找 ja_JP 时，它在缓存中找到 ja_JP，该 ja_JP 引用已过期的 ja。然后，ja_JP 被标记为已过期并从缓存中移除。
        // 这将传播到 ja_JP_JP。
        //
        // 现在，例如，在加载新的 ja_JP 时，其他人可能已经开始加载相同的 bundle 并发现基础 bundle 已过期。
        // 那么，从第一次 getBundle 调用中得到的结果将包括已过期的基础 bundle。但是，如果其他人没有开始加载，
        // 我们在加载过程结束时将不知道基础 bundle 是否已过期。过期控制不保证返回的 bundle 及其父 bundle 没有过期。
        //
        // 我们可以检查整个父链以查看链中是否有任何已过期的 bundle。但这个过程可能永远不会结束。极端情况是 getTimeToLive 返回 0 且
        // needsReload 总是返回 true。
        if (p != null && p.expired) {
            assert bundle != NONEXISTENT_BUNDLE;
            bundle.expired = true;
            bundle.cacheKey = null;
            cacheList.remove(cacheKey, bundleRef);
            bundle = null;
        } else {
            CacheKey key = bundleRef.getCacheKey();
            long expirationTime = key.expirationTime;
            if (!bundle.expired && expirationTime >= 0 &&
                expirationTime <= System.currentTimeMillis()) {
                // 其 TTL 期限已过期。
                if (bundle != NONEXISTENT_BUNDLE) {
                    // 在这里同步调用 needsReload 以避免对同一 bundle 的冗余并发调用。
                    synchronized (bundle) {
                        expirationTime = key.expirationTime;
                        if (!bundle.expired && expirationTime >= 0 &&
                            expirationTime <= System.currentTimeMillis()) {
                            try {
                                bundle.expired = control.needsReload(key.getName(),
                                                                     key.getLocale(),
                                                                     key.getFormat(),
                                                                     key.getLoader(),
                                                                     bundle,
                                                                     key.loadTime);
                            } catch (Exception e) {
                                cacheKey.setCause(e);
                            }
                            if (bundle.expired) {
                                // 如果 bundle 需要重新加载，则从缓存中移除该 bundle，但返回带有已过期标志的 bundle。
                                bundle.cacheKey = null;
                                cacheList.remove(cacheKey, bundleRef);
                            } else {
                                // 更新过期控制信息并重用相同的 bundle 实例。
                                setExpirationTime(key, control);
                            }
                        }
                    }
                } else {
                    // 我们只是从缓存中移除 NONEXISTENT_BUNDLE。
                    cacheList.remove(cacheKey, bundleRef);
                    bundle = null;
                }
            }
        }
        return bundle;
    }


/**
 * 将新的包放入缓存中。
 *
 * @param cacheKey 资源包的键
 * @param bundle 要放入缓存的资源包
 * @return 对于 cacheKey 的资源包；如果有人在这次调用之前已经放入了相同的包，那么返回缓存中的包。
 */
private static ResourceBundle putBundleInCache(CacheKey cacheKey,
                                               ResourceBundle bundle,
                                               Control control) {
    setExpirationTime(cacheKey, control);
    if (cacheKey.expirationTime != Control.TTL_DONT_CACHE) {
        CacheKey key = (CacheKey) cacheKey.clone();
        BundleReference bundleRef = new BundleReference(bundle, referenceQueue, key);
        bundle.cacheKey = key;

        // 如果包尚未在缓存中，则将其放入缓存。
        BundleReference result = cacheList.putIfAbsent(key, bundleRef);

        // 如果其他人在这之前已经将相同的包放入缓存并且它尚未过期，我们应该使用缓存中的包。
        if (result != null) {
            ResourceBundle rb = result.get();
            if (rb != null && !rb.expired) {
                // 清除到缓存键的后向链接
                bundle.cacheKey = null;
                bundle = rb;
                // 清除 BundleReference 中的引用，以防止其被放入队列。
                bundleRef.clear();
            } else {
                // 用有效的实例替换无效的（已被垃圾回收或过期）实例。
                cacheList.put(key, bundleRef);
            }
        }
    }
    return bundle;
}

private static void setExpirationTime(CacheKey cacheKey, Control control) {
    long ttl = control.getTimeToLive(cacheKey.getName(),
                                     cacheKey.getLocale());
    if (ttl >= 0) {
        // 如果指定了任何过期时间，则设置在缓存中过期的时间。
        long now = System.currentTimeMillis();
        cacheKey.loadTime = now;
        cacheKey.expirationTime = now + ttl;
    } else if (ttl >= Control.TTL_NO_EXPIRATION_CONTROL) {
        cacheKey.expirationTime = ttl;
    } else {
        throw new IllegalArgumentException("无效的 Control: TTL=" + ttl);
    }
}

/**
 * 从缓存中移除所有使用调用者的类加载器加载的资源包。
 *
 * @since 1.6
 * @see ResourceBundle.Control#getTimeToLive(String,Locale)
 */
@CallerSensitive
public static final void clearCache() {
    clearCache(getLoader(Reflection.getCallerClass()));
}

/**
 * 从缓存中移除所有使用给定类加载器加载的资源包。
 *
 * @param loader 类加载器
 * @exception NullPointerException 如果 <code>loader</code> 为 null
 * @since 1.6
 * @see ResourceBundle.Control#getTimeToLive(String,Locale)
 */
public static final void clearCache(ClassLoader loader) {
    if (loader == null) {
        throw new NullPointerException();
    }
    Set<CacheKey> set = cacheList.keySet();
    for (CacheKey key : set) {
        if (key.getLoader() == loader) {
            set.remove(key);
        }
    }
}

/**
 * 从这个资源包中获取给定键的对象。如果此资源包不包含给定键的对象，则返回 null。
 *
 * @param key 所需对象的键
 * @exception NullPointerException 如果 <code>key</code> 为 <code>null</code>
 * @return 给定键的对象，或 null
 */
protected abstract Object handleGetObject(String key);

/**
 * 返回键的枚举。
 *
 * @return 包含在此 <code>ResourceBundle</code> 和其父包中的键的 <code>Enumeration</code>。
 */
public abstract Enumeration<String> getKeys();

/**
 * 确定给定的 <code>key</code> 是否包含在此 <code>ResourceBundle</code> 或其父包中。
 *
 * @param key
 *        资源 <code>key</code>
 * @return <code>true</code> 如果给定的 <code>key</code> 包含在此 <code>ResourceBundle</code> 或其
 *        父包中；<code>false</code> 否则。
 * @exception NullPointerException
 *         如果 <code>key</code> 为 <code>null</code>
 * @since 1.6
 */
public boolean containsKey(String key) {
    if (key == null) {
        throw new NullPointerException();
    }
    for (ResourceBundle rb = this; rb != null; rb = rb.parent) {
        if (rb.handleKeySet().contains(key)) {
            return true;
        }
    }
    return false;
}

/**
 * 返回包含在此 <code>ResourceBundle</code> 和其父包中的所有键的 <code>Set</code>。
 *
 * @return 包含在此 <code>ResourceBundle</code> 和其父包中的所有键的 <code>Set</code>。
 * @since 1.6
 */
public Set<String> keySet() {
    Set<String> keys = new HashSet<>();
    for (ResourceBundle rb = this; rb != null; rb = rb.parent) {
        keys.addAll(rb.handleKeySet());
    }
    return keys;
}

/**
 * 返回 <em>仅</em> 包含在此 <code>ResourceBundle</code> 中的键的 <code>Set</code>。
 *
 * <p>默认实现返回由 {@link #getKeys() getKeys} 方法返回的键的 <code>Set</code>，但不包括
 * {@link #handleGetObject(String) handleGetObject} 方法返回 <code>null</code> 的键。一旦
 * <code>Set</code> 创建完成，值将被保留在此 <code>ResourceBundle</code> 中，以避免在后续调用中
 * 产生相同的 <code>Set</code>。子类可以重写此方法以实现更快的处理。
 *
 * @return 仅包含在此 <code>ResourceBundle</code> 中的键的 <code>Set</code>
 * @since 1.6
 */
protected Set<String> handleKeySet() {
    if (keySet == null) {
        synchronized (this) {
            if (keySet == null) {
                Set<String> keys = new HashSet<>();
                Enumeration<String> enumKeys = getKeys();
                while (enumKeys.hasMoreElements()) {
                    String key = enumKeys.nextElement();
                    if (handleGetObject(key) != null) {
                        keys.add(key);
                    }
                }
                keySet = keys;
            }
        }
    }
    return keySet;
}

/**
 * <code>ResourceBundle.Control</code> 定义了一组回调方法，这些方法在 {@link ResourceBundle#getBundle(String,
 * Locale, ClassLoader, Control) ResourceBundle.getBundle} 工厂方法加载资源包的过程中被调用。换句话说，
 * <code>ResourceBundle.Control</code> 与工厂方法协作以加载资源包。默认的回调方法实现提供了工厂方法执行
 * <a href="./ResourceBundle.html#default_behavior">默认行为</a> 所需的信息。
 *
 * <p>除了回调方法外，还定义了 {@link
 * #toBundleName(String, Locale) toBundleName} 和 {@link
 * #toResourceName(String, String) toResourceName} 方法，主要为了方便实现回调方法。然而，<code>toBundleName</code>
 * 方法可以被重写以提供不同的本地化资源组织和打包约定。<code>toResourceName</code> 方法是 <code>final</code>
 * 的，以避免使用错误的资源和类名分隔符。
 *
 * <p>两个工厂方法，{@link #getControl(List)} 和 {@link
 * #getNoFallbackControl(List)}，提供了实现默认资源包加载过程常见变体的 <code>ResourceBundle.Control</code>
 * 实例。
 *
 * <p>{@link Control#getFormats(String) getFormats} 方法返回的格式和 {@link
 * ResourceBundle.Control#getCandidateLocales(String, Locale)
 * getCandidateLocales} 方法返回的候选语言环境在所有针对同一基础包的 <code>ResourceBundle.getBundle</code>
 * 调用中必须一致。否则，<code>ResourceBundle.getBundle</code> 方法可能会返回意外的包。例如，如果
 * <code>getFormats</code> 方法在第一次调用 <code>ResourceBundle.getBundle</code> 时仅返回
 * <code>"java.class"</code>，而在第二次调用时仅返回 <code>"java.properties"</code>，那么第二次调用将返回
 * 在第一次调用期间缓存的类基础包。
 *
 * <p>如果 <code>ResourceBundle.Control</code> 实例被多个线程同时使用，则必须是线程安全的。
 * <code>ResourceBundle.getBundle</code> 不会同步调用 <code>ResourceBundle.Control</code> 方法。默认的方法实现
 * 是线程安全的。
 *
 * <p>应用程序可以指定由 <code>getControl</code> 工厂方法返回的 <code>ResourceBundle.Control</code> 实例或
 * 从 <code>ResourceBundle.Control</code> 子类创建的实例来自定义资源包加载过程。以下是更改默认资源包加载过程的示例。
 *
 * <p><b>示例 1</b>
 *
 * <p>以下代码让 <code>ResourceBundle.getBundle</code> 仅查找基于属性的资源。
 *
 * <pre>
 * import java.util.*;
 * import static java.util.ResourceBundle.Control.*;
 * ...
 * ResourceBundle bundle =
 *   ResourceBundle.getBundle("MyResources", new Locale("fr", "CH"),
 *                            ResourceBundle.Control.getControl(FORMAT_PROPERTIES));
 * </pre>
 *
 * 给定 <a
 * href="./ResourceBundle.html#default_behavior_example">示例</a> 中的资源包，
 * <code>ResourceBundle.getBundle</code> 调用将加载 <code>MyResources_fr_CH.properties</code>，其父包是
 * <code>MyResources_fr.properties</code>，其父包是 <code>MyResources.properties</code>。
 * (<code>MyResources_fr_CH.properties</code> 未被隐藏，但 <code>MyResources_fr_CH.class</code> 被隐藏。)
 *
 * <p><b>示例 2</b>
 *
 * <p>以下示例使用 {@link Properties#loadFromXML(java.io.InputStream)
 * Properties.loadFromXML} 加载基于 XML 的包。
 *
 * <pre>
 * ResourceBundle rb = ResourceBundle.getBundle("Messages",
 *     new ResourceBundle.Control() {
 *         public List&lt;String&gt; getFormats(String baseName) {
 *             if (baseName == null)
 *                 throw new NullPointerException();
 *             return Arrays.asList("xml");
 *         }
 *         public ResourceBundle newBundle(String baseName,
 *                                         Locale locale,
 *                                         String format,
 *                                         ClassLoader loader,
 *                                         boolean reload)
 *                          throws IllegalAccessException,
 *                                 InstantiationException,
 *                                 IOException {
 *             if (baseName == null || locale == null
 *                   || format == null || loader == null)
 *                 throw new NullPointerException();
 *             ResourceBundle bundle = null;
 *             if (format.equals("xml")) {
 *                 String bundleName = toBundleName(baseName, locale);
 *                 String resourceName = toResourceName(bundleName, format);
 *                 InputStream stream = null;
 *                 if (reload) {
 *                     URL url = loader.getResource(resourceName);
 *                     if (url != null) {
 *                         URLConnection connection = url.openConnection();
 *                         if (connection != null) {
 *                             // 禁用缓存以获取重新加载的新数据。
 *                             connection.setUseCaches(false);
 *                             stream = connection.getInputStream();
 *                         }
 *                     }
 *                 } else {
 *                     stream = loader.getResourceAsStream(resourceName);
 *                 }
 *                 if (stream != null) {
 *                     BufferedInputStream bis = new BufferedInputStream(stream);
 *                     bundle = new XMLResourceBundle(bis);
 *                     bis.close();
 *                 }
 *             }
 *             return bundle;
 *         }
 *     });
 *
 * ...
 *
 * private static class XMLResourceBundle extends ResourceBundle {
 *     private Properties props;
 *     XMLResourceBundle(InputStream stream) throws IOException {
 *         props = new Properties();
 *         props.loadFromXML(stream);
 *     }
 *     protected Object handleGetObject(String key) {
 *         return props.getProperty(key);
 *     }
 *     public Enumeration&lt;String&gt; getKeys() {
 *         ...
 *     }
 * }
 * </pre>
 *
 * @since 1.6
 */
public static class Control {
    /**
     * 默认格式 <code>List</code>，包含字符串 <code>"java.class"</code> 和 <code>"java.properties"</code>，
     * 按此顺序。此 <code>List</code> 是 {@linkplain
     * Collections#unmodifiableList(List) 不可修改的}。
     *
     * @see #getFormats(String)
     */
    public static final List<String> FORMAT_DEFAULT
        = Collections.unmodifiableList(Arrays.asList("java.class",
                                                     "java.properties"));

    /**
     * 仅包含 <code>"java.class"</code> 的类格式 <code>List</code>。此 <code>List</code> 是
     * {@linkplain Collections#unmodifiableList(List) 不可修改的}。
     *
     * @see #getFormats(String)
     */
    public static final List<String> FORMAT_CLASS
        = Collections.unmodifiableList(Arrays.asList("java.class"));

    /**
     * 仅包含 <code>"java.properties"</code> 的属性格式 <code>List</code>。此 <code>List</code> 是
     * {@linkplain Collections#unmodifiableList(List) 不可修改的}。
     *
     * @see #getFormats(String)
     */
    public static final List<String> FORMAT_PROPERTIES
        = Collections.unmodifiableList(Arrays.asList("java.properties"));


                    /**
         * 不缓存加载的资源包实例的时间生存期常量。
         *
         * @see #getTimeToLive(String, Locale)
         */
        public static final long TTL_DONT_CACHE = -1;

        /**
         * 禁用缓存中加载的资源包实例的过期控制的时间生存期常量。
         *
         * @see #getTimeToLive(String, Locale)
         */
        public static final long TTL_NO_EXPIRATION_CONTROL = -2;

        private static final Control INSTANCE = new Control();

        /**
         * 唯一构造函数。（用于子类构造函数的调用，通常是隐式的。）
         */
        protected Control() {
        }

        /**
         * 返回一个 <code>ResourceBundle.Control</code>，其中 {@link
         * #getFormats(String) getFormats} 方法返回指定的 <code>formats</code>。<code>formats</code> 必须等于
         * {@link Control#FORMAT_PROPERTIES}、{@link
         * Control#FORMAT_CLASS} 或 {@link
         * Control#FORMAT_DEFAULT} 之一。<code>ResourceBundle.Control</code>
         * 实例由该方法返回的是单例且线程安全的。
         *
         * <p>指定 {@link Control#FORMAT_DEFAULT} 等同于实例化 <code>ResourceBundle.Control</code> 类，
         * 除了此方法返回单例。
         *
         * @param formats
         *        要由 <code>ResourceBundle.Control.getFormats</code> 方法返回的格式
         * @return 支持指定 <code>formats</code> 的 <code>ResourceBundle.Control</code>
         * @exception NullPointerException
         *        如果 <code>formats</code> 为 <code>null</code>
         * @exception IllegalArgumentException
         *        如果 <code>formats</code> 未知
         */
        public static final Control getControl(List<String> formats) {
            if (formats.equals(Control.FORMAT_PROPERTIES)) {
                return SingleFormatControl.PROPERTIES_ONLY;
            }
            if (formats.equals(Control.FORMAT_CLASS)) {
                return SingleFormatControl.CLASS_ONLY;
            }
            if (formats.equals(Control.FORMAT_DEFAULT)) {
                return Control.INSTANCE;
            }
            throw new IllegalArgumentException();
        }

        /**
         * 返回一个 <code>ResourceBundle.Control</code>，其中 {@link
         * #getFormats(String) getFormats} 方法返回指定的 <code>formats</code>，且 {@link
         * Control#getFallbackLocale(String, Locale) getFallbackLocale}
         * 方法返回 <code>null</code>。<code>formats</code> 必须等于
         * {@link Control#FORMAT_PROPERTIES}、{@link
         * Control#FORMAT_CLASS} 或 {@link Control#FORMAT_DEFAULT} 之一。
         * <code>ResourceBundle.Control</code> 实例由该方法返回的是单例且线程安全的。
         *
         * @param formats
         *        要由 <code>ResourceBundle.Control.getFormats</code> 方法返回的格式
         * @return 支持指定 <code>formats</code> 且无回退 <code>Locale</code> 支持的 <code>ResourceBundle.Control</code>
         * @exception NullPointerException
         *        如果 <code>formats</code> 为 <code>null</code>
         * @exception IllegalArgumentException
         *        如果 <code>formats</code> 未知
         */
        public static final Control getNoFallbackControl(List<String> formats) {
            if (formats.equals(Control.FORMAT_DEFAULT)) {
                return NoFallbackControl.NO_FALLBACK;
            }
            if (formats.equals(Control.FORMAT_PROPERTIES)) {
                return NoFallbackControl.PROPERTIES_ONLY_NO_FALLBACK;
            }
            if (formats.equals(Control.FORMAT_CLASS)) {
                return NoFallbackControl.CLASS_ONLY_NO_FALLBACK;
            }
            throw new IllegalArgumentException();
        }

        /**
         * 返回一个包含用于给定 <code>baseName</code> 加载资源包的格式的 <code>List</code>。
         * <code>ResourceBundle.getBundle</code> 工厂方法尝试按照列表指定的顺序加载资源包。
         * 该方法返回的列表必须至少包含一个 <code>String</code>。预定义的格式是 <code>"java.class"</code>
         * 用于基于类的资源包，<code>"java.properties"</code> 用于基于属性的资源包。
         * 以 <code>"java."</code> 开头的字符串保留用于未来的扩展，不得用于应用程序定义的格式。
         *
         * <p>不要求返回不可变（不可修改）的 <code>List</code>。但是，返回的 <code>List</code> 在
         * 由 <code>getFormats</code> 返回后不得被修改。
         *
         * <p>默认实现返回 {@link #FORMAT_DEFAULT}，以便 <code>ResourceBundle.getBundle</code> 工厂方法
         * 首先查找基于类的资源包，然后查找基于属性的资源包。
         *
         * @param baseName
         *        资源包的基本名称，一个完全限定的类名
         * @return 一个包含用于加载资源包的格式的 <code>List</code>。
         * @exception NullPointerException
         *        如果 <code>baseName</code> 为 null
         * @see #FORMAT_DEFAULT
         * @see #FORMAT_CLASS
         * @see #FORMAT_PROPERTIES
         */
        public List<String> getFormats(String baseName) {
            if (baseName == null) {
                throw new NullPointerException();
            }
            return FORMAT_DEFAULT;
        }

        /**
         * 返回一个包含 <code>baseName</code> 和 <code>locale</code> 的候选 <code>Locale</code> 的 <code>List</code>。
         * 每次工厂方法尝试为一个目标 <code>Locale</code> 查找资源包时，此方法由 <code>ResourceBundle.getBundle</code>
         * 工厂方法调用。
         *
         * <p>候选 <code>Locale</code> 的序列也对应于运行时资源查找路径（也称为 <I>父链</I>），如果对应的资源包
         * 存在且其父级不由加载的资源包本身定义。列表的最后一个元素必须是 {@linkplain Locale#ROOT 根 <code>Locale</code>}
         * 如果希望将基础包作为父链的终端。
         *
         * <p>如果给定的 <code>locale</code> 等于 <code>Locale.ROOT</code>（根 <code>Locale</code>），则必须返回
         * 仅包含根 <code>Locale</code> 的 <code>List</code>。在这种情况下，<code>ResourceBundle.getBundle</code>
         * 工厂方法仅加载基础包作为结果资源包。
         *
         * <p>不要求返回不可变（不可修改）的 <code>List</code>。但是，返回的 <code>List</code> 在
         * 由 <code>getCandidateLocales</code> 返回后不得被修改。
         *
         * <p>默认实现返回一个使用以下规则的 <code>List</code>。在以下描述中，<em>L</em>、<em>S</em>、<em>C</em> 和 <em>V</em>
         * 分别表示非空的语言、脚本、国家和变体。例如，[<em>L</em>, <em>C</em>] 表示一个只有语言和国家值非空的 <code>Locale</code>。
         * 形式 <em>L</em>("xx") 表示（非空）语言值为 "xx"。对于所有情况，<code>Locale</code> 的最终组件值为空字符串的被省略。
         *
         * <ol><li>对于一个脚本值为空的输入 <code>Locale</code>，通过省略最终组件逐个添加候选 <code>Locale</code>，如下所示：
         *
         * <ul>
         * <li> [<em>L</em>, <em>C</em>, <em>V</em>] </li>
         * <li> [<em>L</em>, <em>C</em>] </li>
         * <li> [<em>L</em>] </li>
         * <li> <code>Locale.ROOT</code> </li>
         * </ul></li>
         *
         * <li>对于一个脚本值非空的输入 <code>Locale</code>，通过省略最终组件直到语言，然后添加从恢复国家和变体的 <code>Locale</code>
         * 生成的候选 <code>Locale</code>：
         *
         * <ul>
         * <li> [<em>L</em>, <em>S</em>, <em>C</em>, <em>V</em>]</li>
         * <li> [<em>L</em>, <em>S</em>, <em>C</em>]</li>
         * <li> [<em>L</em>, <em>S</em>]</li>
         * <li> [<em>L</em>, <em>C</em>, <em>V</em>]</li>
         * <li> [<em>L</em>, <em>C</em>]</li>
         * <li> [<em>L</em>]</li>
         * <li> <code>Locale.ROOT</code></li>
         * </ul></li>
         *
         * <li>对于一个变体值由多个子标签通过下划线分隔的输入 <code>Locale</code>，通过逐个省略变体子标签生成候选 <code>Locale</code>，
         * 然后将它们插入原始列表中每个包含完整变体值的 <code>Locale</code> 之后。例如，如果变体由两个子标签 <em>V1</em> 和 <em>V2</em> 组成：
         *
         * <ul>
         * <li> [<em>L</em>, <em>S</em>, <em>C</em>, <em>V1</em>, <em>V2</em>]</li>
         * <li> [<em>L</em>, <em>S</em>, <em>C</em>, <em>V1</em>]</li>
         * <li> [<em>L</em>, <em>S</em>, <em>C</em>]</li>
         * <li> [<em>L</em>, <em>S</em>]</li>
         * <li> [<em>L</em>, <em>C</em>, <em>V1</em>, <em>V2</em>]</li>
         * <li> [<em>L</em>, <em>C</em>, <em>V1</em>]</li>
         * <li> [<em>L</em>, <em>C</em>]</li>
         * <li> [<em>L</em>]</li>
         * <li> <code>Locale.ROOT</code></li>
         * </ul></li>
         *
         * <li>中文的特殊情况。当输入 <code>Locale</code> 的语言为 "zh"（中文）且脚本值为空时，根据国家可能提供 "Hans"（简体）或 "Hant"（繁体）。
         * 当国家为 "CN"（中国）或 "SG"（新加坡）时，提供 "Hans"。当国家为 "HK"（中国香港特别行政区）、"MO"（中国澳门特别行政区）或 "TW"（台湾）时，
         * 提供 "Hant"。对于所有其他国家或国家为空时，不提供脚本。例如，对于 <code>Locale("zh", "CN")</code>，候选列表将为：
         * <ul>
         * <li> [<em>L</em>("zh"), <em>S</em>("Hans"), <em>C</em>("CN")]</li>
         * <li> [<em>L</em>("zh"), <em>S</em>("Hans")]</li>
         * <li> [<em>L</em>("zh"), <em>C</em>("CN")]</li>
         * <li> [<em>L</em>("zh")]</li>
         * <li> <code>Locale.ROOT</code></li>
         * </ul>
         *
         * 对于 <code>Locale("zh", "TW")</code>，候选列表将为：
         * <ul>
         * <li> [<em>L</em>("zh"), <em>S</em>("Hant"), <em>C</em>("TW")]</li>
         * <li> [<em>L</em>("zh"), <em>S</em>("Hant")]</li>
         * <li> [<em>L</em>("zh"), <em>C</em>("TW")]</li>
         * <li> [<em>L</em>("zh")]</li>
         * <li> <code>Locale.ROOT</code></li>
         * </ul></li>
         *
         * <li>挪威语的特殊情况。<code>Locale("no", "NO", "NY")</code> 和 <code>Locale("nn", "NO")</code> 都表示挪威新挪威语。
         * 当 <code>Locale</code> 的语言为 "nn" 时，标准候选列表生成到 [<em>L</em>("nn")]，然后添加以下候选：
         *
         * <ul><li> [<em>L</em>("no"), <em>C</em>("NO"), <em>V</em>("NY")]</li>
         * <li> [<em>L</em>("no"), <em>C</em>("NO")]</li>
         * <li> [<em>L</em>("no")]</li>
         * <li> <code>Locale.ROOT</code></li>
         * </ul>
         *
         * 如果 <code>Locale</code> 恰好为 <code>Locale("no", "NO", "NY")</code>，则首先将其转换为 <code>Locale("nn", "NO")</code>，然后遵循上述过程。
         *
         * <p>此外，Java 将语言 "no" 视为挪威博克马尔 "nb" 的同义词。除了单个情况 <code>Locale("no", "NO", "NY")</code>（如上处理），
         * 当输入 <code>Locale</code> 的语言为 "no" 或 "nb" 时，候选 <code>Locale</code> 的语言代码 "no" 和 "nb" 交替出现，首先使用请求的语言，
         * 然后使用其同义词。例如，<code>Locale("nb", "NO", "POSIX")</code> 生成以下候选列表：
         *
         * <ul>
         * <li> [<em>L</em>("nb"), <em>C</em>("NO"), <em>V</em>("POSIX")]</li>
         * <li> [<em>L</em>("no"), <em>C</em>("NO"), <em>V</em>("POSIX")]</li>
         * <li> [<em>L</em>("nb"), <em>C</em>("NO")]</li>
         * <li> [<em>L</em>("no"), <em>C</em>("NO")]</li>
         * <li> [<em>L</em>("nb")]</li>
         * <li> [<em>L</em>("no")]</li>
         * <li> <code>Locale.ROOT</code></li>
         * </ul>
         *
         * <code>Locale("no", "NO", "POSIX")</code> 生成的列表相同，只是 "no" 的 <code>Locale</code> 会出现在相应的 "nb" 的 <code>Locale</code> 之前。</li>
         * </ol>
         *
         * <p>默认实现使用一个 {@link ArrayList}，重写实现可以在返回给调用者之前对其进行修改。但是，子类在由 <code>getCandidateLocales</code>
         * 返回后不得对其进行修改。
         *
         * <p>例如，如果给定的 <code>baseName</code> 为 "Messages"，给定的 <code>locale</code> 为
         * <code>Locale("ja",&nbsp;"",&nbsp;"XX")</code>，则返回一个 <code>List</code> 的 <code>Locale</code>：
         * <pre>
         *     Locale("ja", "", "XX")
         *     Locale("ja")
         *     Locale.ROOT
         * </pre>
         * 如果找到了 "ja" 和 "" 的资源包，则运行时资源查找路径（父链）为：
         * <pre>{@code
         *     Messages_ja -> Messages
         * }</pre>
         *
         * @param baseName
         *        资源包的基本名称，一个完全限定的类名
         * @param locale
         *        所需的 <code>Locale</code>
         * @return 给定 <code>locale</code> 的候选 <code>Locale</code> 的 <code>List</code>
         * @exception NullPointerException
         *        如果 <code>baseName</code> 或 <code>locale</code> 为 <code>null</code>
         */
        public List<Locale> getCandidateLocales(String baseName, Locale locale) {
            if (baseName == null) {
                throw new NullPointerException();
            }
            return new ArrayList<>(CANDIDATES_CACHE.get(locale.getBaseLocale()));
        }


                    private static final CandidateListCache CANDIDATES_CACHE = new CandidateListCache();

        private static class CandidateListCache extends LocaleObjectCache<BaseLocale, List<Locale>> {
            protected List<Locale> createObject(BaseLocale base) {
                String language = base.getLanguage();
                String script = base.getScript();
                String region = base.getRegion();
                String variant = base.getVariant();

                // 特殊处理挪威语
                boolean isNorwegianBokmal = false;
                boolean isNorwegianNynorsk = false;
                if (language.equals("no")) {
                    if (region.equals("NO") && variant.equals("NY")) {
                        variant = "";
                        isNorwegianNynorsk = true;
                    } else {
                        isNorwegianBokmal = true;
                    }
                }
                if (language.equals("nb") || isNorwegianBokmal) {
                    List<Locale> tmpList = getDefaultList("nb", script, region, variant);
                    // 为每个列表条目插入一个将 "nb" 替换为 "no" 的区域设置
                    List<Locale> bokmalList = new LinkedList<>();
                    for (Locale l : tmpList) {
                        bokmalList.add(l);
                        if (l.getLanguage().length() == 0) {
                            break;
                        }
                        bokmalList.add(Locale.getInstance("no", l.getScript(), l.getCountry(),
                                l.getVariant(), null));
                    }
                    return bokmalList;
                } else if (language.equals("nn") || isNorwegianNynorsk) {
                    // 在 nn 后插入 no_NO_NY, no_NO, no
                    List<Locale> nynorskList = getDefaultList("nn", script, region, variant);
                    int idx = nynorskList.size() - 1;
                    nynorskList.add(idx++, Locale.getInstance("no", "NO", "NY"));
                    nynorskList.add(idx++, Locale.getInstance("no", "NO", ""));
                    nynorskList.add(idx++, Locale.getInstance("no", "", ""));
                    return nynorskList;
                }
                // 特殊处理中文
                else if (language.equals("zh")) {
                    if (script.length() == 0 && region.length() > 0) {
                        // 为希望使用 zh_Hans/zh_Hant 作为捆绑名称的用户提供脚本（推荐用于 Java7+）
                        switch (region) {
                        case "TW":
                        case "HK":
                        case "MO":
                            script = "Hant";
                            break;
                        case "CN":
                        case "SG":
                            script = "Hans";
                            break;
                        }
                    } else if (script.length() > 0 && region.length() == 0) {
                        // 为仍然使用旧约定打包中文捆绑的用户提供地区（国家）
                        switch (script) {
                        case "Hans":
                            region = "CN";
                            break;
                        case "Hant":
                            region = "TW";
                            break;
                        }
                    }
                }

                return getDefaultList(language, script, region, variant);
            }

            private static List<Locale> getDefaultList(String language, String script, String region, String variant) {
                List<String> variants = null;

                if (variant.length() > 0) {
                    variants = new LinkedList<>();
                    int idx = variant.length();
                    while (idx != -1) {
                        variants.add(variant.substring(0, idx));
                        idx = variant.lastIndexOf('_', --idx);
                    }
                }

                List<Locale> list = new LinkedList<>();

                if (variants != null) {
                    for (String v : variants) {
                        list.add(Locale.getInstance(language, script, region, v, null));
                    }
                }
                if (region.length() > 0) {
                    list.add(Locale.getInstance(language, script, region, "", null));
                }
                if (script.length() > 0) {
                    list.add(Locale.getInstance(language, script, "", "", null));

                    // 有脚本时，在截断变体、地区和脚本后，重新开始不带脚本的处理
                    if (variants != null) {
                        for (String v : variants) {
                            list.add(Locale.getInstance(language, "", region, v, null));
                        }
                    }
                    if (region.length() > 0) {
                        list.add(Locale.getInstance(language, "", region, "", null));
                    }
                }
                if (language.length() > 0) {
                    list.add(Locale.getInstance(language, "", "", "", null));
                }
                // 在列表末尾添加根区域设置
                list.add(Locale.ROOT);

                return list;
            }
        }

        /**
         * 返回一个 <code>Locale</code> 作为进一步资源捆绑搜索的回退区域设置，由
         * <code>ResourceBundle.getBundle</code> 工厂方法使用。每次找不到 <code>baseName</code> 和
         * <code>locale</code> 的资源捆绑时，工厂方法都会调用此方法，其中 <code>locale</code> 是
         * <code>ResourceBundle.getBundle</code> 的参数或此方法之前返回的回退区域设置。
         *
         * <p>如果不需要进一步回退搜索，则返回 <code>null</code>。
         *
         * <p>默认实现如果给定的 <code>locale</code> 不是默认区域设置，则返回 {@linkplain
         * Locale#getDefault() 默认 <code>Locale</code>}。否则，返回 <code>null</code>。
         *
         * @param baseName
         *        资源捆绑的基本名称，一个完全限定的类名，对于
         *        <code>ResourceBundle.getBundle</code> 无法找到任何资源捆绑（除了基本捆绑）的情况
         * @param locale
         *        资源捆绑的 <code>Locale</code>，对于
         *        <code>ResourceBundle.getBundle</code> 无法找到任何资源捆绑（除了基本捆绑）的情况
         * @return 用于回退搜索的 <code>Locale</code>，
         *        或 <code>null</code> 如果不需要进一步回退搜索。
         * @exception NullPointerException
         *        如果 <code>baseName</code> 或 <code>locale</code>
         *        为 <code>null</code>
         */
        public Locale getFallbackLocale(String baseName, Locale locale) {
            if (baseName == null) {
                throw new NullPointerException();
            }
            Locale defaultLocale = Locale.getDefault();
            return locale.equals(defaultLocale) ? null : defaultLocale;
        }

        /**
         * 为给定的捆绑名称、格式和区域设置实例化一个资源捆绑，必要时使用给定的类加载器。如果给定参数没有可用的资源捆绑，则此方法返回 <code>null</code>。如果由于意外错误无法实例化资源捆绑，则必须通过抛出 <code>Error</code> 或
         * <code>Exception</code> 来报告错误，而不仅仅是返回 <code>null</code>。
         *
         * <p>如果 <code>reload</code> 标志为 <code>true</code>，则表示此方法因之前加载的资源捆绑已过期而被调用。
         *
         * <p>默认实现按照以下方式实例化一个 <code>ResourceBundle</code>。
         *
         * <ul>
         *
         * <li>通过调用 {@link
         * #toBundleName(String, Locale) toBundleName(baseName,
         * locale)} 获取捆绑名称。</li>
         *
         * <li>如果 <code>format</code> 是 <code>"java.class"</code>，则通过调用
         * {@link ClassLoader#loadClass(String)} 加载由捆绑名称指定的 {@link Class}。然后，通过调用 {@link
         * Class#newInstance()} 实例化一个 <code>ResourceBundle</code>。注意，在此默认实现中，加载基于类的资源捆绑时忽略 <code>reload</code> 标志。</li>
         *
         * <li>如果 <code>format</code> 是 <code>"java.properties"</code>，则调用 {@link #toResourceName(String, String) toResourceName(bundlename,
         * "properties")} 获取资源名称。如果 <code>reload</code> 为 <code>true</code>，则调用 {@link
         * ClassLoader#getResource(String) load.getResource} 获取一个 {@link URL} 以创建一个 {@link
         * URLConnection}。此 <code>URLConnection</code> 用于 {@linkplain URLConnection#setUseCaches(boolean) 禁用底层资源加载层的缓存}，
         * 并 {@linkplain URLConnection#getInputStream() 获取一个 <code>InputStream</code>}。否则，调用 {@link ClassLoader#getResourceAsStream(String)
         * loader.getResourceAsStream} 获取一个 {@link
         * InputStream}。然后，使用 <code>InputStream</code> 构造一个 {@link
         * PropertyResourceBundle}。</li>
         *
         * <li>如果 <code>format</code> 既不是 <code>"java.class"</code>
         * 也不是 <code>"java.properties"</code>，则抛出一个 <code>IllegalArgumentException</code>。</li>
         *
         * </ul>
         *
         * @param baseName
         *        资源捆绑的基本名称，一个完全限定的类名
         * @param locale
         *        应实例化的资源捆绑的区域设置
         * @param format
         *        要加载的资源捆绑格式
         * @param loader
         *        用于加载捆绑的 <code>ClassLoader</code>
         * @param reload
         *        指示捆绑重新加载的标志；如果重新加载已过期的资源捆绑，则为 <code>true</code>，
         *        否则为 <code>false</code>
         * @return 资源捆绑实例，
         *        或 <code>null</code> 如果没有找到。
         * @exception NullPointerException
         *        如果 <code>bundleName</code>、<code>locale</code>、
         *        <code>format</code> 或 <code>loader</code> 为
         *        <code>null</code>，或者如果 {@link #toBundleName(String, Locale) toBundleName}
         *        返回 <code>null</code>
         * @exception IllegalArgumentException
         *        如果 <code>format</code> 未知，或者给定参数找到的资源包含格式错误的数据。
         * @exception ClassCastException
         *        如果加载的类不能转换为 <code>ResourceBundle</code>
         * @exception IllegalAccessException
         *        如果类或其无参数构造函数不可访问。
         * @exception InstantiationException
         *        如果类实例化失败。
         * @exception ExceptionInInitializerError
         *        如果此方法引发的初始化失败。
         * @exception SecurityException
         *        如果存在安全管理器并且创建新实例被拒绝。详情参见 {@link Class#newInstance()}
         * @exception IOException
         *        如果在使用任何 I/O 操作读取资源时发生错误
         */
        public ResourceBundle newBundle(String baseName, Locale locale, String format,
                                        ClassLoader loader, boolean reload)
                    throws IllegalAccessException, InstantiationException, IOException {
            String bundleName = toBundleName(baseName, locale);
            ResourceBundle bundle = null;
            if (format.equals("java.class")) {
                try {
                    @SuppressWarnings("unchecked")
                    Class<? extends ResourceBundle> bundleClass
                        = (Class<? extends ResourceBundle>)loader.loadClass(bundleName);

                    // 如果类不是 ResourceBundle 子类，抛出 ClassCastException。
                    if (ResourceBundle.class.isAssignableFrom(bundleClass)) {
                        bundle = bundleClass.newInstance();
                    } else {
                        throw new ClassCastException(bundleClass.getName()
                                     + " cannot be cast to ResourceBundle");
                    }
                } catch (ClassNotFoundException e) {
                }
            } else if (format.equals("java.properties")) {
                final String resourceName = toResourceName0(bundleName, "properties");
                if (resourceName == null) {
                    return bundle;
                }
                final ClassLoader classLoader = loader;
                final boolean reloadFlag = reload;
                InputStream stream = null;
                try {
                    stream = AccessController.doPrivileged(
                        new PrivilegedExceptionAction<InputStream>() {
                            public InputStream run() throws IOException {
                                InputStream is = null;
                                if (reloadFlag) {
                                    URL url = classLoader.getResource(resourceName);
                                    if (url != null) {
                                        URLConnection connection = url.openConnection();
                                        if (connection != null) {
                                            // 禁用缓存以获取重新加载的新数据。
                                            connection.setUseCaches(false);
                                            is = connection.getInputStream();
                                        }
                                    }
                                } else {
                                    is = classLoader.getResourceAsStream(resourceName);
                                }
                                return is;
                            }
                        });
                } catch (PrivilegedActionException e) {
                    throw (IOException) e.getException();
                }
                if (stream != null) {
                    try {
                        bundle = new PropertyResourceBundle(stream);
                    } finally {
                        stream.close();
                    }
                }
            } else {
                throw new IllegalArgumentException("unknown format: " + format);
            }
            return bundle;
        }


        /**
         * 返回为此加载的资源包设置的时间生存期（TTL）值。
         * 正的时间生存期值指定了资源包可以在缓存中停留而无需验证其构建源数据的毫秒数。
         * 值 0 表示每次从缓存中检索资源包时都必须进行验证。{@link
         * #TTL_DONT_CACHE} 指定加载的资源包不放入缓存。{@link #TTL_NO_EXPIRATION_CONTROL} 指定
         * 加载的资源包放入缓存且没有过期控制。
         *
         * <p>过期仅影响 <code>ResourceBundle.getBundle</code> 工厂方法的资源包加载过程。
         * 即，如果工厂方法在缓存中找到已过期的资源包，工厂方法将调用 {@link
         * #needsReload(String, Locale, String, ClassLoader, ResourceBundle,
         * long) needsReload} 方法以确定是否需要重新加载资源包。如果 <code>needsReload</code> 返回
         * <code>true</code>，则从缓存中移除缓存的资源包实例。否则，实例保留在缓存中，
         * 并使用此方法返回的新 TTL 值更新。
         *
         * <p>所有缓存的资源包都可能因运行时环境的内存限制而从缓存中移除。
         * 返回一个较大的正值并不意味着将加载的资源包锁定在缓存中。
         *
         * <p>默认实现返回 {@link #TTL_NO_EXPIRATION_CONTROL}。
         *
         * @param baseName
         *        指定过期值的资源包的基本名称。
         * @param locale
         *        指定过期值的资源包的区域设置。
         * @return 资源包在缓存中过期的时间（0 或从缓存时间起的正毫秒偏移量），
         *        {@link #TTL_NO_EXPIRATION_CONTROL} 以禁用过期控制，或 {@link #TTL_DONT_CACHE} 以禁用缓存。
         * @exception NullPointerException
         *        如果 <code>baseName</code> 或 <code>locale</code> 为 <code>null</code>
         */
        public long getTimeToLive(String baseName, Locale locale) {
            if (baseName == null || locale == null) {
                throw new NullPointerException();
            }
            return TTL_NO_EXPIRATION_CONTROL;
        }

        /**
         * 根据 <code>loadTime</code> 给出的加载时间或其他标准确定缓存中已过期的 <code>bundle</code> 是否需要重新加载。
         * 如果需要重新加载，该方法返回 <code>true</code>；否则返回 <code>false</code>。
         * <code>loadTime</code> 是自 <a href="Calendar.html#Epoch"> <code>Calendar</code>
         * Epoch</a> 以来的毫秒偏移量。
         *
         * 调用的 <code>ResourceBundle.getBundle</code> 工厂方法在当前调用中使用的 <code>ResourceBundle.Control</code>
         * 实例上调用此方法，而不是在最初加载资源包时使用的实例。
         *
         * <p>默认实现比较 <code>loadTime</code> 和资源包源数据的最后修改时间。
         * 如果确定源数据自 <code>loadTime</code> 以来已被修改，则返回 <code>true</code>。
         * 否则，返回 <code>false</code>。此实现假设给定的 <code>format</code> 如果不是默认格式之一，
         * <code>"java.class"</code> 或 <code>"java.properties"</code>，则为文件后缀的相同字符串。
         *
         * @param baseName
         *        资源包的基本名称，一个完全限定的类名
         * @param locale
         *        资源包应实例化的区域设置
         * @param format
         *        要加载的资源包格式
         * @param loader
         *        用于加载资源包的 <code>ClassLoader</code>
         * @param bundle
         *        在缓存中已过期的资源包实例
         * @param loadTime
         *        资源包被加载并放入缓存的时间
         * @return 如果过期的资源包需要重新加载，则返回 <code>true</code>；否则返回 <code>false</code>。
         * @exception NullPointerException
         *        如果 <code>baseName</code>、<code>locale</code>、
         *        <code>format</code>、<code>loader</code> 或 <code>bundle</code> 为 <code>null</code>
         */
        public boolean needsReload(String baseName, Locale locale,
                                   String format, ClassLoader loader,
                                   ResourceBundle bundle, long loadTime) {
            if (bundle == null) {
                throw new NullPointerException();
            }
            if (format.equals("java.class") || format.equals("java.properties")) {
                format = format.substring(5);
            }
            boolean result = false;
            try {
                String resourceName = toResourceName0(toBundleName(baseName, locale), format);
                if (resourceName == null) {
                    return result;
                }
                URL url = loader.getResource(resourceName);
                if (url != null) {
                    long lastModified = 0;
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        // 禁用缓存以获取正确数据
                        connection.setUseCaches(false);
                        if (connection instanceof JarURLConnection) {
                            JarEntry ent = ((JarURLConnection)connection).getJarEntry();
                            if (ent != null) {
                                lastModified = ent.getTime();
                                if (lastModified == -1) {
                                    lastModified = 0;
                                }
                            }
                        } else {
                            lastModified = connection.getLastModified();
                        }
                    }
                    result = lastModified >= loadTime;
                }
            } catch (NullPointerException npe) {
                throw npe;
            } catch (Exception e) {
                // 忽略其他异常
            }
            return result;
        }

        /**
         * 将给定的 <code>baseName</code> 和 <code>locale</code> 转换为资源包名称。
         * 此方法由 {@link #newBundle(String, Locale, String,
         * ClassLoader, boolean) newBundle} 和 {@link #needsReload(String,
         * Locale, String, ClassLoader, ResourceBundle, long) needsReload}
         * 方法的默认实现调用。
         *
         * <p>此实现返回以下值：
         * <pre>
         *     baseName + "_" + language + "_" + script + "_" + country + "_" + variant
         * </pre>
         * 其中 <code>language</code>、<code>script</code>、<code>country</code> 和 <code>variant</code>
         * 分别是 <code>locale</code> 的语言、脚本、国家和变体值。最终组件值为空字符串的将被省略，连同前面的 '_'。
         * 当脚本为空时，脚本值及其前面的 '_' 也将被省略。如果所有值都是空字符串，则返回 <code>baseName</code>。
         *
         * <p>例如，如果 <code>baseName</code> 是 <code>"baseName"</code> 且 <code>locale</code> 是
         * <code>Locale("ja",&nbsp;"",&nbsp;"XX")</code>，则返回 <code>"baseName_ja_&thinsp;_XX"</code>。
         * 如果给定的区域设置是 <code>Locale("en")</code>，则返回 <code>"baseName_en"</code>。
         *
         * <p>重写此方法允许应用程序使用不同的本地化资源组织和打包约定。
         *
         * @param baseName
         *        资源包的基本名称，一个完全限定的类名
         * @param locale
         *        要加载的资源包的区域设置
         * @return 资源包的名称
         * @exception NullPointerException
         *        如果 <code>baseName</code> 或 <code>locale</code> 为 <code>null</code>
         */
        public String toBundleName(String baseName, Locale locale) {
            if (locale == Locale.ROOT) {
                return baseName;
            }

            String language = locale.getLanguage();
            String script = locale.getScript();
            String country = locale.getCountry();
            String variant = locale.getVariant();

            if (language == "" && country == "" && variant == "") {
                return baseName;
            }

            StringBuilder sb = new StringBuilder(baseName);
            sb.append('_');
            if (script != "") {
                if (variant != "") {
                    sb.append(language).append('_').append(script).append('_').append(country).append('_').append(variant);
                } else if (country != "") {
                    sb.append(language).append('_').append(script).append('_').append(country);
                } else {
                    sb.append(language).append('_').append(script);
                }
            } else {
                if (variant != "") {
                    sb.append(language).append('_').append(country).append('_').append(variant);
                } else if (country != "") {
                    sb.append(language).append('_').append(country);
                } else {
                    sb.append(language);
                }
            }
            return sb.toString();

        }

        /**
         * 将给定的 <code>bundleName</code> 转换为 {@link ClassLoader#getResource ClassLoader.getResource}
         * 方法所需的格式，即将 <code>bundleName</code> 中所有出现的 <code>'.'</code> 替换为 <code>'/'</code>，
         * 并追加一个 <code>'.'</code> 和给定的文件 <code>suffix</code>。例如，如果 <code>bundleName</code> 是
         * <code>"foo.bar.MyResources_ja_JP"</code> 且 <code>suffix</code> 是 <code>"properties"</code>，
         * 则返回 <code>"foo/bar/MyResources_ja_JP.properties"</code>。
         *
         * @param bundleName
         *        资源包名称
         * @param suffix
         *        文件类型后缀
         * @return 转换后的资源名称
         * @exception NullPointerException
         *         如果 <code>bundleName</code> 或 <code>suffix</code> 为 <code>null</code>
         */
        public final String toResourceName(String bundleName, String suffix) {
            StringBuilder sb = new StringBuilder(bundleName.length() + 1 + suffix.length());
            sb.append(bundleName.replace('.', '/')).append('.').append(suffix);
            return sb.toString();
        }

        private String toResourceName0(String bundleName, String suffix) {
            // 应用程序协议检查
            if (bundleName.contains("://")) {
                return null;
            } else {
                return toResourceName(bundleName, suffix);
            }
        }
    }

    private static class SingleFormatControl extends Control {
        private static final Control PROPERTIES_ONLY
            = new SingleFormatControl(FORMAT_PROPERTIES);

        private static final Control CLASS_ONLY
            = new SingleFormatControl(FORMAT_CLASS);

        private final List<String> formats;

        protected SingleFormatControl(List<String> formats) {
            this.formats = formats;
        }

        public List<String> getFormats(String baseName) {
            if (baseName == null) {
                throw new NullPointerException();
            }
            return formats;
        }
    }

    private static final class NoFallbackControl extends SingleFormatControl {
        private static final Control NO_FALLBACK
            = new NoFallbackControl(FORMAT_DEFAULT);

        private static final Control PROPERTIES_ONLY_NO_FALLBACK
            = new NoFallbackControl(FORMAT_PROPERTIES);

        private static final Control CLASS_ONLY_NO_FALLBACK
            = new NoFallbackControl(FORMAT_CLASS);

        protected NoFallbackControl(List<String> formats) {
            super(formats);
        }

        public Locale getFallbackLocale(String baseName, Locale locale) {
            if (baseName == null || locale == null) {
                throw new NullPointerException();
            }
            return null;
        }
    }
}
