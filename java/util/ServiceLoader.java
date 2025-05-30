
/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.AccessController;
import java.security.AccessControlContext;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * 一个简单的服务提供者加载设施。
 *
 * <p> 一个 <i>服务</i> 是一组众所周知的接口和（通常是抽象的）类。一个 <i>服务提供者</i> 是该服务的具体实现。提供者中的类通常实现服务中定义的接口并继承服务中定义的类，提供特定于提供者的数据和代码。服务提供者可以以扩展的形式安装在 Java 平台的实现中，即放置在通常的扩展目录中的 jar 文件。提供者也可以通过将其添加到应用程序的类路径中或通过其他平台特定的方式提供。
 *
 * <p> 对于加载目的，服务由单个类型表示，即单个接口或抽象类。（可以使用具体类，但不推荐。）给定服务的提供者包含一个或多个具体类，这些类扩展了这个 <i>服务类型</i>，并包含特定于提供者的数据和代码。提供者类通常不是提供者本身的全部，而是一个代理，其中包含足够的信息来决定提供者是否能够满足特定请求，以及按需创建实际提供者的代码。提供者类的细节高度依赖于特定服务；没有一个单一的类或接口可以统一它们，因此这里没有定义这样的类型。此设施唯一要求的是提供者类必须有一个无参数构造函数，以便在加载时实例化。
 *
 * <p><a name="format"> 服务提供者通过在资源目录 <tt>META-INF/services</tt> 中放置一个 <i>提供者配置文件</i> 来标识。</a> 文件的名称是服务类型的完全限定 <a href="../lang/ClassLoader.html#name">二进制名称</a>。文件包含一个具体提供者类的完全限定二进制名称列表，每个名称一行。每个名称周围的空格和制表符以及空行将被忽略。注释字符是 <tt>'#'</tt> (<tt>'&#92;u0023'</tt>，<font style="font-size:smaller;">NUMBER SIGN</font>)；在每一行中，所有跟随第一个注释字符的字符将被忽略。文件必须使用 UTF-8 编码。
 *
 * <p> 如果某个具体提供者类在多个配置文件中被命名，或者在同一个配置文件中被命名多次，则重复项将被忽略。命名特定提供者的配置文件不必与提供者本身在同一个 jar 文件或其他分发单元中。提供者必须可以从最初查询以定位配置文件的类加载器访问；请注意，这不一定是实际加载文件的类加载器。
 *
 * <p> 提供者在需要时才会被定位和实例化。服务加载器维护一个已经加载的提供者的缓存。每次调用 {@link #iterator iterator} 方法时，返回的迭代器首先生成缓存中的所有元素，按实例化顺序，然后懒惰地定位和实例化任何剩余的提供者，每次将一个添加到缓存中。可以通过 {@link #reload reload} 方法清除缓存。
 *
 * <p> 服务加载器始终在调用者的安全上下文中执行。受信任的系统代码通常应该从特权安全上下文中调用此类的方法，以及这些方法返回的迭代器的方法。
 *
 * <p> 本类的实例不是多线程安全的。
 *
 * <p> 除非另有说明，向此类的任何方法传递 <tt>null</tt> 参数将导致抛出 {@link NullPointerException}。
 *
 *
 * <p><span style="font-weight: bold; padding-right: 1em">示例</span>
 * 假设我们有一个服务类型 <tt>com.example.CodecSet</tt>，用于表示某些协议的编码器/解码器对集合。在这种情况下，它是一个抽象类，有两个抽象方法：
 *
 * <blockquote><pre>
 * public abstract Encoder getEncoder(String encodingName);
 * public abstract Decoder getDecoder(String encodingName);</pre></blockquote>
 *
 * 每个方法返回一个合适的对象或 <tt>null</tt>，如果提供者不支持给定的编码。典型的提供者支持多种编码。
 *
 * <p> 如果 <tt>com.example.impl.StandardCodecs</tt> 是 <tt>CodecSet</tt> 服务的一个实现，那么它的 jar 文件还包含一个名为
 *
 * <blockquote><pre>
 * META-INF/services/com.example.CodecSet</pre></blockquote>
 *
 * <p> 的文件。该文件包含一行：
 *
 * <blockquote><pre>
 * com.example.impl.StandardCodecs    # 标准编解码器</pre></blockquote>
 *
 * <p> <tt>CodecSet</tt> 类在初始化时创建并保存一个服务实例：
 *
 * <blockquote><pre>
 * private static ServiceLoader&lt;CodecSet&gt; codecSetLoader
 *     = ServiceLoader.load(CodecSet.class);</pre></blockquote>
 *
 * <p> 为了为给定的编码名称定位编码器，它定义了一个静态工厂方法，该方法遍历已知和可用的提供者，仅在找到合适的编码器或用尽提供者时返回。
 *
 * <blockquote><pre>
 * public static Encoder getEncoder(String encodingName) {
 *     for (CodecSet cp : codecSetLoader) {
 *         Encoder enc = cp.getEncoder(encodingName);
 *         if (enc != null)
 *             return enc;
 *     }
 *     return null;
 * }</pre></blockquote>
 *
 * <p> <tt>getDecoder</tt> 方法的定义类似。
 *
 *
 * <p><span style="font-weight: bold; padding-right: 1em">使用说明</span> 如果用于提供者加载的类加载器的类路径包含远程网络 URL，则这些 URL 将在搜索提供者配置文件的过程中被解析。
 *
 * <p> 这种活动是正常的，尽管它可能会导致 web 服务器日志中出现令人困惑的条目。然而，如果 web 服务器配置不正确，这种活动可能会导致提供者加载算法错误地失败。
 *
 * <p> 当请求的资源不存在时，web 服务器应返回 HTTP 404（未找到）响应。然而，有时 web 服务器错误地配置为在这种情况下返回 HTTP 200（OK）响应以及一个有用的 HTML 错误页面。这将导致此类在尝试将 HTML 页面解析为提供者配置文件时抛出 {@link ServiceConfigurationError}。解决此问题的最佳方法是修复配置错误的 web 服务器，使其返回正确的响应代码（HTTP 404）以及 HTML 错误页面。
 *
 * @param  <S>
 *         由此加载器加载的服务类型
 *
 * @author Mark Reinhold
 * @since 1.6
 */

public final class ServiceLoader<S>
    implements Iterable<S>
{

    private static final String PREFIX = "META-INF/services/";

    // 代表正在加载的服务的类或接口
    private final Class<S> service;

    // 用于定位、加载和实例化提供者的类加载器
    private final ClassLoader loader;

    // 创建 ServiceLoader 时获取的访问控制上下文
    private final AccessControlContext acc;

    // 按实例化顺序缓存的提供者
    private LinkedHashMap<String,S> providers = new LinkedHashMap<>();

    // 当前的懒惰查找迭代器
    private LazyIterator lookupIterator;

    /**
     * 清除此加载器的提供者缓存，以便重新加载所有提供者。
     *
     * <p> 调用此方法后，后续对 {@link #iterator() iterator} 方法的调用将从头开始懒惰地查找和实例化提供者，就像新创建的加载器一样。
     *
     * <p> 此方法适用于在运行中的 Java 虚拟机中可以安装新提供者的情况。
     */
    public void reload() {
        providers.clear();
        lookupIterator = new LazyIterator(service, loader);
    }

    private ServiceLoader(Class<S> svc, ClassLoader cl) {
        service = Objects.requireNonNull(svc, "服务接口不能为 null");
        loader = (cl == null) ? ClassLoader.getSystemClassLoader() : cl;
        acc = (System.getSecurityManager() != null) ? AccessController.getContext() : null;
        reload();
    }

    private static void fail(Class<?> service, String msg, Throwable cause)
        throws ServiceConfigurationError
    {
        throw new ServiceConfigurationError(service.getName() + ": " + msg,
                                            cause);
    }

    private static void fail(Class<?> service, String msg)
        throws ServiceConfigurationError
    {
        throw new ServiceConfigurationError(service.getName() + ": " + msg);
    }

    private static void fail(Class<?> service, URL u, int line, String msg)
        throws ServiceConfigurationError
    {
        fail(service, u + ":" + line + ": " + msg);
    }

    // 从给定的配置文件中解析一行，将行上的名称添加到名称列表中。
    //
    private int parseLine(Class<?> service, URL u, BufferedReader r, int lc,
                          List<String> names)
        throws IOException, ServiceConfigurationError
    {
        String ln = r.readLine();
        if (ln == null) {
            return -1;
        }
        int ci = ln.indexOf('#');
        if (ci >= 0) ln = ln.substring(0, ci);
        ln = ln.trim();
        int n = ln.length();
        if (n != 0) {
            if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0))
                fail(service, u, lc, "非法的配置文件语法");
            int cp = ln.codePointAt(0);
            if (!Character.isJavaIdentifierStart(cp))
                fail(service, u, lc, "非法的提供者类名: " + ln);
            for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
                cp = ln.codePointAt(i);
                if (!Character.isJavaIdentifierPart(cp) && (cp != '.'))
                    fail(service, u, lc, "非法的提供者类名: " + ln);
            }
            if (!providers.containsKey(ln) && !names.contains(ln))
                names.add(ln);
        }
        return lc + 1;
    }

    // 解析给定 URL 的内容作为提供者配置文件。
    //
    // @param  service
    //         正在寻找提供者的服务类型；用于构造错误详细字符串
    //
    // @param  u
    //         命名要解析的配置文件的 URL
    //
    // @return 一个（可能是空的）迭代器，它将生成给定配置文件中尚未成为返回集合成员的提供者类名
    //
    // @throws ServiceConfigurationError
    //         如果在从给定 URL 读取时发生 I/O 错误，或检测到配置文件格式错误
    //
    private Iterator<String> parse(Class<?> service, URL u)
        throws ServiceConfigurationError
    {
        InputStream in = null;
        BufferedReader r = null;
        ArrayList<String> names = new ArrayList<>();
        try {
            in = u.openStream();
            r = new BufferedReader(new InputStreamReader(in, "utf-8"));
            int lc = 1;
            while ((lc = parseLine(service, u, r, lc, names)) >= 0);
        } catch (IOException x) {
            fail(service, "读取配置文件时发生错误", x);
        } finally {
            try {
                if (r != null) r.close();
                if (in != null) in.close();
            } catch (IOException y) {
                fail(service, "关闭配置文件时发生错误", y);
            }
        }
        return names.iterator();
    }

    // 私有内部类实现完全懒惰的提供者查找
    //
    private class LazyIterator
        implements Iterator<S>
    {

        Class<S> service;
        ClassLoader loader;
        Enumeration<URL> configs = null;
        Iterator<String> pending = null;
        String nextName = null;

        private LazyIterator(Class<S> service, ClassLoader loader) {
            this.service = service;
            this.loader = loader;
        }

        private boolean hasNextService() {
            if (nextName != null) {
                return true;
            }
            if (configs == null) {
                try {
                    String fullName = PREFIX + service.getName();
                    if (loader == null)
                        configs = ClassLoader.getSystemResources(fullName);
                    else
                        configs = loader.getResources(fullName);
                } catch (IOException x) {
                    fail(service, "定位配置文件时发生错误", x);
                }
            }
            while ((pending == null) || !pending.hasNext()) {
                if (!configs.hasMoreElements()) {
                    return false;
                }
                pending = parse(service, configs.nextElement());
            }
            nextName = pending.next();
            return true;
        }

        private S nextService() {
            if (!hasNextService())
                throw new NoSuchElementException();
            String cn = nextName;
            nextName = null;
            Class<?> c = null;
            try {
                c = Class.forName(cn, false, loader);
            } catch (ClassNotFoundException x) {
                fail(service,
                     "未找到提供者 " + cn);
            }
            if (!service.isAssignableFrom(c)) {
                fail(service,
                     "提供者 " + cn  + " 不是子类型");
            }
            try {
                S p = service.cast(c.newInstance());
                providers.put(cn, p);
                return p;
            } catch (Throwable x) {
                fail(service,
                     "无法实例化提供者 " + cn,
                     x);
            }
            throw new Error();          // 这不可能发生
        }
    }


    /**
     * 惰性加载此加载器服务的可用提供者。
     *
     * <p> 该方法返回的迭代器首先按实例化顺序生成提供者缓存中的所有元素。然后惰性地加载并实例化剩余的提供者，将每个提供者依次添加到缓存中。
     *
     * <p> 为了实现惰性，解析可用提供者配置文件并实例化提供者的工作必须由迭代器本身完成。因此，其 {@link java.util.Iterator#hasNext hasNext} 和
     * {@link java.util.Iterator#next next} 方法可能会在定位和实例化下一个提供者时抛出 {@link ServiceConfigurationError}，如果提供者配置文件违反了指定的格式，或者它命名了一个无法找到和实例化的提供者类，或者实例化类的结果无法分配给服务类型，或者在定位和实例化下一个提供者时抛出了其他任何类型的异常或错误。编写健壮的代码时，只需在使用服务迭代器时捕获 {@link
     * ServiceConfigurationError}。
     *
     * <p> 如果抛出此类错误，则后续调用迭代器将尽力定位并实例化下一个可用提供者，但通常无法保证此类恢复。
     *
     * <blockquote style="font-size: smaller; line-height: 1.2"><span
     * style="padding-right: 1em; font-weight: bold">设计说明</span>
     * 在这些情况下抛出错误可能显得极端。这种行为的合理性在于，畸形的提供者配置文件，就像畸形的类文件一样，表明 Java 虚拟机的配置或使用方式存在严重问题。因此，最好抛出错误而不是尝试恢复，更糟糕的是，默默地失败。</blockquote>
     *
     * <p> 该方法返回的迭代器不支持删除。调用其 {@link java.util.Iterator#remove() remove} 方法将导致抛出 {@link UnsupportedOperationException}。
     *
     * @implNote 在将提供者添加到缓存时，{@link #iterator Iterator} 按照 {@link
     * java.lang.ClassLoader#getResources(java.lang.String)
     * ClassLoader.getResources(String)} 方法找到服务配置文件的顺序处理资源。
     *
     * @return  一个惰性加载此加载器服务提供者的迭代器
     */
    public Iterator<S> iterator() {
        return new Iterator<S>() {

            Iterator<Map.Entry<String,S>> knownProviders
                = providers.entrySet().iterator();

            public boolean hasNext() {
                if (knownProviders.hasNext())
                    return true;
                return lookupIterator.hasNext();
            }

            public S next() {
                if (knownProviders.hasNext())
                    return knownProviders.next().getValue();
                return lookupIterator.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    /**
     * 为给定的服务类型和类加载器创建一个新的服务加载器。
     *
     * @param  <S> 服务类型的类
     *
     * @param  service
     *         表示服务的接口或抽象类
     *
     * @param  loader
     *         用于加载提供者配置文件和提供者类的类加载器，或 <tt>null</tt>，如果使用系统类加载器（或失败时使用引导类加载器）
     *
     * @return 一个新的服务加载器
     */
    public static <S> ServiceLoader<S> load(Class<S> service,
                                            ClassLoader loader)
    {
        return new ServiceLoader<>(service, loader);
    }

    /**
     * 为给定的服务类型创建一个新的服务加载器，使用当前线程的 {@linkplain java.lang.Thread#getContextClassLoader
     * 上下文类加载器}。
     *
     * <p> 该便捷方法的调用形式
     *
     * <blockquote><pre>
     * ServiceLoader.load(<i>service</i>)</pre></blockquote>
     *
     * 等价于
     *
     * <blockquote><pre>
     * ServiceLoader.load(<i>service</i>,
     *                    Thread.currentThread().getContextClassLoader())</pre></blockquote>
     *
     * @param  <S> 服务类型的类
     *
     * @param  service
     *         表示服务的接口或抽象类
     *
     * @return 一个新的服务加载器
     */
    public static <S> ServiceLoader<S> load(Class<S> service) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return ServiceLoader.load(service, cl);
    }

    /**
     * 为给定的服务类型创建一个新的服务加载器，使用扩展类加载器。
     *
     * <p> 该便捷方法简单地定位扩展类加载器，假设为 <tt><i>extClassLoader</i></tt>，然后返回
     *
     * <blockquote><pre>
     * ServiceLoader.load(<i>service</i>, <i>extClassLoader</i>)</pre></blockquote>
     *
     * <p> 如果找不到扩展类加载器，则使用系统类加载器；如果没有系统类加载器，则使用引导类加载器。
     *
     * <p> 该方法旨在仅使用已安装的提供者。生成的服务将仅查找并加载已安装到当前 Java 虚拟机中的提供者；应用程序类路径上的提供者将被忽略。
     *
     * @param  <S> 服务类型的类
     *
     * @param  service
     *         表示服务的接口或抽象类
     *
     * @return 一个新的服务加载器
     */
    public static <S> ServiceLoader<S> loadInstalled(Class<S> service) {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        ClassLoader prev = null;
        while (cl != null) {
            prev = cl;
            cl = cl.getParent();
        }
        return ServiceLoader.load(service, prev);
    }

    /**
     * 返回描述此服务的字符串。
     *
     * @return  描述字符串
     */
    public String toString() {
        return "java.util.ServiceLoader[" + service.getName() + "]";
    }

}