
/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
import java.net.URL;
import sun.security.util.Debug;
import sun.security.util.PropertyExpander;

import sun.security.jca.*;

/**
 * <p>此类集中管理所有安全属性和通用安全方法。其主要用途之一是管理提供者。
 *
 * <p>安全属性的默认值从实现特定的位置读取，通常是 Java 安装目录中的属性文件
 * {@code lib/security/java.security}。
 *
 * @author Benjamin Renaud
 */

public final class Security {

    /* 是否调试？——仅限开发人员 */
    private static final Debug sdebug =
                        Debug.getInstance("properties");

    /* Java 安全属性 */
    private static Properties props;

    // 缓存中的一个元素
    private static class ProviderProperty {
        String className;
        Provider provider;
    }

    static {
        // doPrivileged 在这里是因为 initialize 中有多个
        // 可能需要特权的操作。
        // （FileInputStream 调用和 File.exists 调用，
        // securityPropFile 调用等）
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                initialize();
                return null;
            }
        });
    }

    private static void initialize() {
        props = new Properties();
        boolean loadedProps = false;
        boolean overrideAll = false;

        // 首先加载系统属性文件
        // 以确定 security.overridePropertiesFile 的值
        File propFile = securityPropFile("java.security");
        if (propFile.exists()) {
            InputStream is = null;
            try {
                FileInputStream fis = new FileInputStream(propFile);
                is = new BufferedInputStream(fis);
                props.load(is);
                loadedProps = true;

                if (sdebug != null) {
                    sdebug.println("读取安全属性文件: " +
                                propFile);
                }
            } catch (IOException e) {
                if (sdebug != null) {
                    sdebug.println("无法从 " +
                                propFile + " 加载安全属性");
                    e.printStackTrace();
                }
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ioe) {
                        if (sdebug != null) {
                            sdebug.println("无法关闭输入流");
                        }
                    }
                }
            }
        }

        if ("true".equalsIgnoreCase(props.getProperty
                ("security.overridePropertiesFile"))) {

            String extraPropFile = System.getProperty
                                        ("java.security.properties");
            if (extraPropFile != null && extraPropFile.startsWith("=")) {
                overrideAll = true;
                extraPropFile = extraPropFile.substring(1);
            }

            if (overrideAll) {
                props = new Properties();
                if (sdebug != null) {
                    sdebug.println
                        ("覆盖其他安全属性文件!");
                }
            }

            // 现在加载用户指定的文件，以便其值
            // 如果与早期值冲突，将获胜
            if (extraPropFile != null) {
                BufferedInputStream bis = null;
                try {
                    URL propURL;

                    extraPropFile = PropertyExpander.expand(extraPropFile);
                    propFile = new File(extraPropFile);
                    if (propFile.exists()) {
                        propURL = new URL
                                ("file:" + propFile.getCanonicalPath());
                    } else {
                        propURL = new URL(extraPropFile);
                    }
                    bis = new BufferedInputStream(propURL.openStream());
                    props.load(bis);
                    loadedProps = true;

                    if (sdebug != null) {
                        sdebug.println("读取安全属性文件: " +
                                        propURL);
                        if (overrideAll) {
                            sdebug.println
                                ("覆盖其他安全属性文件!");
                        }
                    }
                } catch (Exception e) {
                    if (sdebug != null) {
                        sdebug.println
                                ("无法从 " +
                                extraPropFile + " 加载安全属性");
                        e.printStackTrace();
                    }
                } finally {
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (IOException ioe) {
                            if (sdebug != null) {
                                sdebug.println("无法关闭输入流");
                            }
                        }
                    }
                }
            }
        }

        if (!loadedProps) {
            initializeStatic();
            if (sdebug != null) {
                sdebug.println("无法加载安全属性 " +
                        "-- 使用默认值");
            }
        }

    }

    /*
     * 如果未找到 <java.home>/lib/java.security，
     * 初始化为默认值。
     */
    private static void initializeStatic() {
        props.put("security.provider.1", "sun.security.provider.Sun");
        props.put("security.provider.2", "sun.security.rsa.SunRsaSign");
        props.put("security.provider.3", "com.sun.net.ssl.internal.ssl.Provider");
        props.put("security.provider.4", "com.sun.crypto.provider.SunJCE");
        props.put("security.provider.5", "sun.security.jgss.SunProvider");
        props.put("security.provider.6", "com.sun.security.sasl.Provider");
    }

    /**
     * 不允许任何人实例化此类。
     */
    private Security() {
    }

    private static File securityPropFile(String filename) {
        // 也许将来会检查一个系统属性来指定查找位置。
        String sep = File.separator;
        return new File(System.getProperty("java.home") + sep + "lib" + sep +
                        "security" + sep + filename);
    }

    /**
     * 查找提供者，并返回映射键的属性（及其关联的提供者），如果有的话。
     * 查找提供者的顺序是提供者优先级顺序，如安全
     * 属性文件中指定的。
     */
    private static ProviderProperty getProviderProperty(String key) {
        ProviderProperty entry = null;

        List<Provider> providers = Providers.getProviderList().providers();
        for (int i = 0; i < providers.size(); i++) {

            String matchKey = null;
            Provider prov = providers.get(i);
            String prop = prov.getProperty(key);

            if (prop == null) {
                // 如果我们进行不区分大小写的属性名称比较，是否有匹配？让我们试试 ...
                for (Enumeration<Object> e = prov.keys();
                                e.hasMoreElements() && prop == null; ) {
                    matchKey = (String)e.nextElement();
                    if (key.equalsIgnoreCase(matchKey)) {
                        prop = prov.getProperty(matchKey);
                        break;
                    }
                }
            }

            if (prop != null) {
                ProviderProperty newEntry = new ProviderProperty();
                newEntry.className = prop;
                newEntry.provider = prov;
                return newEntry;
            }
        }

        return entry;
    }

    /**
     * 返回给定提供者的键映射的属性（如果有）。
     */
    private static String getProviderProperty(String key, Provider provider) {
        String prop = provider.getProperty(key);
        if (prop == null) {
            // 如果我们进行不区分大小写的属性名称比较，是否有匹配？让我们试试 ...
            for (Enumeration<Object> e = provider.keys();
                                e.hasMoreElements() && prop == null; ) {
                String matchKey = (String)e.nextElement();
                if (key.equalsIgnoreCase(matchKey)) {
                    prop = provider.getProperty(matchKey);
                    break;
                }
            }
        }
        return prop;
    }

    /**
     * 获取指定算法的属性。算法名称应为标准名称。有关标准算法名称的信息，请参阅
     * <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html">
     * Java 密码架构标准算法名称文档</a>。
     *
     * 一种可能的用途是由专门的算法解析器使用，这些解析器可能将类映射到它们理解的算法
     * （类似于密钥解析器所做的）。
     *
     * @param algName 算法名称。
     *
     * @param propName 要获取的属性名称。
     *
     * @return 指定属性的值。
     *
     * @deprecated 此方法以前用于返回 "SUN" 密码服务提供者的主文件中的专有属性值
     * 以确定如何解析特定于算法的参数。请改用新的基于提供者且与算法无关的
     * {@code AlgorithmParameters} 和 {@code KeyFactory} 引擎类
     * （在 J2SE 1.2 平台中引入）。
     */
    @Deprecated
    public static String getAlgorithmProperty(String algName,
                                              String propName) {
        ProviderProperty entry = getProviderProperty("Alg." + propName
                                                     + "." + algName);
        if (entry != null) {
            return entry.className;
        } else {
            return null;
        }
    }

    /**
     * 在指定位置添加新的提供者。位置是搜索请求算法时提供者的优先级顺序。
     * 位置从 1 开始，即 1 是最优先的，其次是 2，依此类推。
     *
     * <p>如果在请求的位置安装了给定的提供者，则该位置上原有的提供者
     * 以及所有位置大于 {@code position} 的提供者都会向上移动
     * 一个位置（向已安装提供者的列表末尾移动）。
     *
     * <p>如果提供者已安装，则无法添加。
     *
     * <p>如果有安全经理，将调用
     * {@link java.lang.SecurityManager#checkSecurityAccess} 方法
     * 以 {@code "insertProvider"} 权限目标名称检查是否可以添加新的提供者。
     * 如果此权限检查被拒绝，则再次调用 {@code checkSecurityAccess}
     * 以 {@code "insertProvider."+provider.getName()} 权限目标名称。
     * 如果两次检查都被拒绝，则抛出 {@code SecurityException}。
     *
     * @param provider 要添加的提供者。
     *
     * @param position 调用者希望此提供者所在的优先级位置。
     *
     * @return 提供者实际添加的优先级位置，如果提供者未添加（因为它已安装），则返回 -1。
     *
     * @throws  NullPointerException 如果 provider 为 null
     * @throws  SecurityException
     *          如果存在安全经理且其 {@link
     *          java.lang.SecurityManager#checkSecurityAccess} 方法
     *          拒绝添加新的提供者
     *
     * @see #getProvider
     * @see #removeProvider
     * @see java.security.SecurityPermission
     */
    public static synchronized int insertProviderAt(Provider provider,
            int position) {
        String providerName = provider.getName();
        checkInsertProvider(providerName);
        ProviderList list = Providers.getFullProviderList();
        ProviderList newList = ProviderList.insertAt(list, provider, position - 1);
        if (list == newList) {
            return -1;
        }
        Providers.setProviderList(newList);
        return newList.getIndex(providerName) + 1;
    }

    /**
     * 将提供者添加到下一个可用位置。
     *
     * <p>如果有安全经理，将调用
     * {@link java.lang.SecurityManager#checkSecurityAccess} 方法
     * 以 {@code "insertProvider"} 权限目标名称检查是否可以添加新的提供者。
     * 如果此权限检查被拒绝，则再次调用 {@code checkSecurityAccess}
     * 以 {@code "insertProvider."+provider.getName()} 权限目标名称。
     * 如果两次检查都被拒绝，则抛出 {@code SecurityException}。
     *
     * @param provider 要添加的提供者。
     *
     * @return 提供者实际添加的优先级位置，如果提供者未添加（因为它已安装），则返回 -1。
     *
     * @throws  NullPointerException 如果 provider 为 null
     * @throws  SecurityException
     *          如果存在安全经理且其 {@link
     *          java.lang.SecurityManager#checkSecurityAccess} 方法
     *          拒绝添加新的提供者
     *
     * @see #getProvider
     * @see #removeProvider
     * @see java.security.SecurityPermission
     */
    public static int addProvider(Provider provider) {
        /*
         * 我们不能在这里分配位置，因为静态注册的提供者可能尚未安装。
         * insertProviderAt() 将在加载静态提供者后修复该值。
         */
        return insertProviderAt(provider, 0);
    }


                /**
     * 移除具有指定名称的提供者。
     *
     * <p>当指定的提供者被移除时，所有位于指定提供者位置之后的提供者都会向下移动一个位置（向已安装提供者列表的头部移动）。
     *
     * <p>如果提供者未安装或名称为 null，此方法将静默返回。
     *
     * <p>首先，如果有安全管理者，其 {@code checkSecurityAccess}
     * 方法将被调用，参数为 {@code "removeProvider."+name}
     * 以检查是否可以移除提供者。
     * 如果使用的是 {@code checkSecurityAccess} 的默认实现（即该方法未被重写），则这将导致调用安全管理者的 {@code checkPermission} 方法
     * 并传递一个 {@code SecurityPermission("removeProvider."+name)}
     * 权限。
     *
     * @param name 要移除的提供者的名称。
     *
     * @throws  SecurityException
     *          如果存在安全管理者且其 {@link
     *          java.lang.SecurityManager#checkSecurityAccess} 方法
     *          拒绝移除提供者的访问权限
     *
     * @see #getProvider
     * @see #addProvider
     */
    public static synchronized void removeProvider(String name) {
        check("removeProvider." + name);
        ProviderList list = Providers.getFullProviderList();
        ProviderList newList = ProviderList.remove(list, name);
        Providers.setProviderList(newList);
    }

    /**
     * 返回一个包含所有已安装提供者的数组。数组中提供者的顺序是它们的优先顺序。
     *
     * @return 一个包含所有已安装提供者的数组。
     */
    public static Provider[] getProviders() {
        return Providers.getFullProviderList().toArray();
    }

    /**
     * 返回具有指定名称的已安装提供者，如果未安装具有指定名称的提供者或名称为 null，则返回 null。
     *
     * @param name 要获取的提供者的名称。
     *
     * @return 指定名称的提供者。
     *
     * @see #removeProvider
     * @see #addProvider
     */
    public static Provider getProvider(String name) {
        return Providers.getProviderList().getProvider(name);
    }

    /**
     * 返回一个包含所有满足指定选择标准的已安装提供者的数组，如果未安装满足选择标准的提供者，则返回 null。返回的提供者按其
     * {@linkplain #insertProviderAt(java.security.Provider, int) 优先顺序} 排序。
     *
     * <p>加密服务总是与特定的算法或类型相关联。例如，数字签名服务总是与特定的算法（如 DSA）相关联，
     * 证书工厂服务总是与特定的证书类型（如 X.509）相关联。
     *
     * <p>选择标准必须以以下两种格式之一指定：
     * <ul>
     * <li> <i>{@literal <crypto_service>.<algorithm_or_type>}</i>
     * <p> 加密服务名称不得包含任何点。
     * <p> 如果提供者实现了指定的加密服务的指定算法或类型，则该提供者满足指定的选择标准。
     * <p> 例如，"CertificateFactory.X.509"
     * 会满足任何提供 X.509 证书的 CertificateFactory 实现的提供者。
     * <li> <i>{@literal <crypto_service>.<algorithm_or_type>
     * <attribute_name>:<attribute_value>}</i>
     * <p> 加密服务名称不得包含任何点。在
     * <i>{@literal <algorithm_or_type>}</i> 和
     * <i>{@literal <attribute_name>}</i> 之间必须有一个或多个空格字符。
     *  <p> 如果提供者实现了指定的加密服务的指定算法或类型，并且其实现满足指定的属性名/值对表达的约束，则该提供者满足此选择标准。
     * <p> 例如，"Signature.SHA1withDSA KeySize:1024" 会满足任何实现了
     * SHA1withDSA 签名算法且密钥大小为 1024（或更大）的提供者。
     *
     * </ul>
     *
     * <p> 有关标准加密服务名称、标准算法名称和标准属性名称的信息，请参阅
     * <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>。
     *
     * @param filter 选择提供者的标准。过滤器不区分大小写。
     *
     * @return 满足选择标准的所有已安装提供者，如果未安装满足选择标准的提供者，则返回 null。
     *
     * @throws InvalidParameterException
     *         如果过滤器的格式不正确
     * @throws NullPointerException 如果过滤器为 null
     *
     * @see #getProviders(java.util.Map)
     * @since 1.3
     */
    public static Provider[] getProviders(String filter) {
        String key = null;
        String value = null;
        int index = filter.indexOf(':');

        if (index == -1) {
            key = filter;
            value = "";
        } else {
            key = filter.substring(0, index);
            value = filter.substring(index + 1);
        }

        Hashtable<String, String> hashtableFilter = new Hashtable<>(1);
        hashtableFilter.put(key, value);

        return (getProviders(hashtableFilter));
    }

    /**
     * 返回一个包含所有满足指定选择标准的已安装提供者的数组，如果未安装满足选择标准的提供者，则返回 null。返回的提供者按其
     * {@linkplain #insertProviderAt(java.security.Provider, int)
     * 优先顺序} 排序。
     *
     * <p>选择标准由一个映射表示。映射中的每个条目代表一个选择标准。
     * 如果提供者满足所有选择标准，则选择该提供者。映射中的任何条目的键必须是以下两种格式之一：
     * <ul>
     * <li> <i>{@literal <crypto_service>.<algorithm_or_type>}</i>
     * <p> 加密服务名称不得包含任何点。
     * <p> 与键关联的值必须是一个空字符串。
     * <p> 如果提供者实现了指定的加密服务的指定算法或类型，则该提供者满足此选择标准。
     * <li>  <i>{@literal <crypto_service>}.
     * {@literal <algorithm_or_type> <attribute_name>}</i>
     * <p> 加密服务名称不得包含任何点。在
     * <i>{@literal <algorithm_or_type>}</i>
     * 和 <i>{@literal <attribute_name>}</i> 之间必须有一个或多个空格字符。
     * <p> 与键关联的值必须是一个非空字符串。
     * 如果提供者实现了指定的加密服务的指定算法或类型，并且其实现满足指定的属性名/值对表达的约束，则该提供者满足此选择标准。
     * </ul>
     *
     * <p> 有关标准加密服务名称、标准算法名称和标准属性名称的信息，请参阅
     * <a href=
     * "../../../technotes/guides/security/StandardNames.html">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>。
     *
     * @param filter 选择提供者的标准。过滤器不区分大小写。
     *
     * @return 满足选择标准的所有已安装提供者，如果未安装满足选择标准的提供者，则返回 null。
     *
     * @throws InvalidParameterException
     *         如果过滤器的格式不正确
     * @throws NullPointerException 如果过滤器为 null
     *
     * @see #getProviders(java.lang.String)
     * @since 1.3
     */
    public static Provider[] getProviders(Map<String,String> filter) {
        // 首先获取所有已安装的提供者。
        // 然后仅返回满足选择标准的提供者。
        Provider[] allProviders = Security.getProviders();
        Set<String> keySet = filter.keySet();
        LinkedHashSet<Provider> candidates = new LinkedHashSet<>(5);

        // 如果选择标准为 null，则返回所有已安装的提供者。
        if ((keySet == null) || (allProviders == null)) {
            return allProviders;
        }

        boolean firstSearch = true;

        // 对于每个选择标准，从候选集中移除不满足该标准的提供者。
        for (Iterator<String> ite = keySet.iterator(); ite.hasNext(); ) {
            String key = ite.next();
            String value = filter.get(key);

            LinkedHashSet<Provider> newCandidates = getAllQualifyingCandidates(key, value,
                                                               allProviders);
            if (firstSearch) {
                candidates = newCandidates;
                firstSearch = false;
            }

            if ((newCandidates != null) && !newCandidates.isEmpty()) {
                // 对于候选集中的每个提供者，如果它不在 newCandidate 集中，
                // 则应从候选集中移除。
                for (Iterator<Provider> cansIte = candidates.iterator();
                     cansIte.hasNext(); ) {
                    Provider prov = cansIte.next();
                    if (!newCandidates.contains(prov)) {
                        cansIte.remove();
                    }
                }
            } else {
                candidates = null;
                break;
            }
        }

        if ((candidates == null) || (candidates.isEmpty()))
            return null;

        Object[] candidatesArray = candidates.toArray();
        Provider[] result = new Provider[candidatesArray.length];

        for (int i = 0; i < result.length; i++) {
            result[i] = (Provider)candidatesArray[i];
        }

        return result;
    }

    // 包含指定类型 Spi 类对象的缓存映射
    private static final Map<String, Class<?>> spiMap =
            new ConcurrentHashMap<>();

    /**
     * 返回给定引擎类型（例如 "MessageDigest"）的 Class 对象。仅适用于 java.security 包中的 Spis。
     */
    private static Class<?> getSpiClass(String type) {
        Class<?> clazz = spiMap.get(type);
        if (clazz != null) {
            return clazz;
        }
        try {
            clazz = Class.forName("java.security." + type + "Spi");
            spiMap.put(type, clazz);
            return clazz;
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Spi class not found", e);
        }
    }

    /*
     * 返回一个对象数组：数组中的第一个对象是请求的算法和类型的实现实例，
     * 数组中的第二个对象标识该实现的提供者。
     * {@code provider} 参数可以为 null，在这种情况下，将按优先顺序搜索所有配置的提供者。
     */
    static Object[] getImpl(String algorithm, String type, String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        if (provider == null) {
            return GetInstance.getInstance
                (type, getSpiClass(type), algorithm).toArray();
        } else {
            return GetInstance.getInstance
                (type, getSpiClass(type), algorithm, provider).toArray();
        }
    }

    static Object[] getImpl(String algorithm, String type, String provider,
            Object params) throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidAlgorithmParameterException {
        if (provider == null) {
            return GetInstance.getInstance
                (type, getSpiClass(type), algorithm, params).toArray();
        } else {
            return GetInstance.getInstance
                (type, getSpiClass(type), algorithm, params, provider).toArray();
        }
    }

    /*
     * 返回一个对象数组：数组中的第一个对象是请求的算法和类型的实现实例，
     * 数组中的第二个对象标识该实现的提供者。
     * {@code provider} 参数不能为 null。
     */
    static Object[] getImpl(String algorithm, String type, Provider provider)
            throws NoSuchAlgorithmException {
        return GetInstance.getInstance
            (type, getSpiClass(type), algorithm, provider).toArray();
    }

    static Object[] getImpl(String algorithm, String type, Provider provider,
            Object params) throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        return GetInstance.getInstance
            (type, getSpiClass(type), algorithm, params, provider).toArray();
    }

    /**
     * 获取安全属性值。
     *
     * <p>首先，如果有安全管理者，其 {@code checkPermission} 方法将被调用，参数为
     * {@code java.security.SecurityPermission("getProperty."+key)}
     * 以检查是否可以检索指定的安全属性值。
     *
     * @param key 要检索的属性的键。
     *
     * @return 与键对应的安全属性值。
     *
     * @throws  SecurityException
     *          如果存在安全管理者且其 {@link
     *          java.lang.SecurityManager#checkPermission} 方法
     *          拒绝检索指定的安全属性值
     * @throws  NullPointerException 如果键为 null
     *
     * @see #setProperty
     * @see java.security.SecurityPermission
     */
    public static String getProperty(String key) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SecurityPermission("getProperty."+
                                                      key));
        }
        String name = props.getProperty(key);
        if (name != null)
            name = name.trim(); // 可能是一个带有尾随空格的类名
        return name;
    }

    /**
     * 设置安全属性值。
     *
     * <p>首先，如果有安全管理者，其 {@code checkPermission} 方法将被调用，参数为
     * {@code java.security.SecurityPermission("setProperty."+key)}
     * 以检查是否可以设置指定的安全属性值。
     *
     * @param key 要设置的属性的名称。
     *
     * @param datum 要设置的属性的值。
     *
     * @throws  SecurityException
     *          如果存在安全管理者且其 {@link
     *          java.lang.SecurityManager#checkPermission} 方法
     *          拒绝设置指定的安全属性值
     * @throws  NullPointerException 如果键或数据为 null
     *
     * @see #getProperty
     * @see java.security.SecurityPermission
     */
    public static void setProperty(String key, String datum) {
        check("setProperty."+key);
        props.put(key, datum);
        invalidateSMCache(key);  /* 参见下文。 */
    }


                /*
     * 实现细节：如果在 setProperty() 中设置的属性是 "package.access" 或 "package.definition"，
     * 我们需要向 SecurityManager 类发出信号，表明值已更改，并且它应该使本地缓存值失效。
     *
     * 而不是为此功能创建新的 API 入口，我们使用反射来设置私有变量。
     */
    private static void invalidateSMCache(String key) {

        final boolean pa = key.equals("package.access");
        final boolean pd = key.equals("package.definition");

        if (pa || pd) {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    try {
                        /* 通过引导类加载器获取类。 */
                        Class<?> cl = Class.forName(
                            "java.lang.SecurityManager", false, null);
                        Field f = null;
                        boolean accessible = false;

                        if (pa) {
                            f = cl.getDeclaredField("packageAccessValid");
                            accessible = f.isAccessible();
                            f.setAccessible(true);
                        } else {
                            f = cl.getDeclaredField("packageDefinitionValid");
                            accessible = f.isAccessible();
                            f.setAccessible(true);
                        }
                        f.setBoolean(f, false);
                        f.setAccessible(accessible);
                    }
                    catch (Exception e1) {
                        /* 如果无法获取类，说明它尚未加载。如果不存在这样的字段，我们不应该尝试设置它。这里不应该有安全异常，因为我们是由引导类加载器加载的，并且我们在 doPrivileged() 内部。
                         *
                         * 无操作：不执行任何操作...
                         */
                    }
                    return null;
                }  /* run */
            });  /* PrivilegedAction */
        }  /* if */
    }

    private static void check(String directive) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSecurityAccess(directive);
        }
    }

    private static void checkInsertProvider(String name) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            try {
                security.checkSecurityAccess("insertProvider");
            } catch (SecurityException se1) {
                try {
                    security.checkSecurityAccess("insertProvider." + name);
                } catch (SecurityException se2) {
                    // 抛出第一个异常，但将第二个添加到被抑制的异常中
                    se1.addSuppressed(se2);
                    throw se1;
                }
            }
        }
    }

    /*
    * 返回满足指定条件的所有提供者。
    */
    private static LinkedHashSet<Provider> getAllQualifyingCandidates(
                                                String filterKey,
                                                String filterValue,
                                                Provider[] allProviders) {
        String[] filterComponents = getFilterComponents(filterKey,
                                                        filterValue);

        // 第一个组件是服务名称。
        // 第二个是算法名称。
        // 如果第三个不为空，那是属性名称。
        String serviceName = filterComponents[0];
        String algName = filterComponents[1];
        String attrName = filterComponents[2];

        return getProvidersNotUsingCache(serviceName, algName, attrName,
                                         filterValue, allProviders);
    }

    private static LinkedHashSet<Provider> getProvidersNotUsingCache(
                                                String serviceName,
                                                String algName,
                                                String attrName,
                                                String filterValue,
                                                Provider[] allProviders) {
        LinkedHashSet<Provider> candidates = new LinkedHashSet<>(5);
        for (int i = 0; i < allProviders.length; i++) {
            if (isCriterionSatisfied(allProviders[i], serviceName,
                                     algName,
                                     attrName, filterValue)) {
                candidates.add(allProviders[i]);
            }
        }
        return candidates;
    }

    /*
     * 如果给定的提供者满足选择条件 key:value，则返回 true。
     */
    private static boolean isCriterionSatisfied(Provider prov,
                                                String serviceName,
                                                String algName,
                                                String attrName,
                                                String filterValue) {
        String key = serviceName + '.' + algName;

        if (attrName != null) {
            key += ' ' + attrName;
        }
        // 检查提供者是否有与给定键相同的属性。
        String propValue = getProviderProperty(key, prov);

        if (propValue == null) {
            // 检查键中是否有别名而不是标准名称。
            String standardName = getProviderProperty("Alg.Alias." +
                                                      serviceName + "." +
                                                      algName,
                                                      prov);
            if (standardName != null) {
                key = serviceName + "." + standardName;

                if (attrName != null) {
                    key += ' ' + attrName;
                }

                propValue = getProviderProperty(key, prov);
            }

            if (propValue == null) {
                // 提供者在其属性列表中没有给定的键。
                return false;
            }
        }

        // 如果键的格式为：
        // <crypto_service>.<algorithm_or_type>，
        // 则无需检查值。

        if (attrName == null) {
            return true;
        }

        // 如果我们到达这里，键必须是
        // <crypto_service>.<algorithm_or_provider> <attribute_name> 的格式。
        if (isStandardAttr(attrName)) {
            return isConstraintSatisfied(attrName, filterValue, propValue);
        } else {
            return filterValue.equalsIgnoreCase(propValue);
        }
    }

    /*
     * 如果属性是标准属性，则返回 true；否则返回 false。
     */
    private static boolean isStandardAttr(String attribute) {
        // 目前，我们只有两个标准属性：
        // KeySize 和 ImplementedIn。
        if (attribute.equalsIgnoreCase("KeySize"))
            return true;

        if (attribute.equalsIgnoreCase("ImplementedIn"))
            return true;

        return false;
    }

    /*
     * 如果请求的属性值受支持，则返回 true；否则返回 false。
     */
    private static boolean isConstraintSatisfied(String attribute,
                                                 String value,
                                                 String prop) {
        // 对于 KeySize，prop 是提供者为特定 <crypto_service>.<algorithm> 支持的最大密钥大小。
        if (attribute.equalsIgnoreCase("KeySize")) {
            int requestedSize = Integer.parseInt(value);
            int maxSize = Integer.parseInt(prop);
            if (requestedSize <= maxSize) {
                return true;
            } else {
                return false;
            }
        }

        // 对于 Type，prop 是特定 <crypto service>.<algorithm> 的实现类型。
        if (attribute.equalsIgnoreCase("ImplementedIn")) {
            return value.equalsIgnoreCase(prop);
        }

        return false;
    }

    static String[] getFilterComponents(String filterKey, String filterValue) {
        int algIndex = filterKey.indexOf('.');

        if (algIndex < 0) {
            // 过滤器中必须有一个点，并且点不能位于字符串的开头。
            throw new InvalidParameterException("无效的过滤器");
        }

        String serviceName = filterKey.substring(0, algIndex);
        String algName = null;
        String attrName = null;

        if (filterValue.length() == 0) {
            // filterValue 是一个空字符串。因此 filterKey
            // 应该是 <crypto_service>.<algorithm_or_type> 的格式。
            algName = filterKey.substring(algIndex + 1).trim();
            if (algName.length() == 0) {
                // 必须有一个算法或类型名称。
                throw new InvalidParameterException("无效的过滤器");
            }
        } else {
            // filterValue 是一个非空字符串。因此 filterKey 必须是
            // <crypto_service>.<algorithm_or_type> <attribute_name> 的格式。
            int attrIndex = filterKey.indexOf(' ');

            if (attrIndex == -1) {
                // 过滤器中没有属性名称。
                throw new InvalidParameterException("无效的过滤器");
            } else {
                attrName = filterKey.substring(attrIndex + 1).trim();
                if (attrName.length() == 0) {
                    // 过滤器中没有属性名称。
                    throw new InvalidParameterException("无效的过滤器");
                }
            }

            // 过滤器中必须有一个算法名称。
            if ((attrIndex < algIndex) ||
                (algIndex == attrIndex - 1)) {
                throw new InvalidParameterException("无效的过滤器");
            } else {
                algName = filterKey.substring(algIndex + 1, attrIndex);
            }
        }

        String[] result = new String[3];
        result[0] = serviceName;
        result[1] = algName;
        result[2] = attrName;

        return result;
    }

    /**
     * 返回一个包含指定 Java 加密服务（例如，Signature, MessageDigest, Cipher, Mac, KeyStore）所有可用算法或类型的字符串集。如果没有任何提供者支持指定的服务或 serviceName 为 null，则返回一个空集。有关完整的 Java 加密服务列表，请参阅
     * <a href="../../../technotes/guides/security/crypto/CryptoSpec.html">Java
     * 加密架构 API 规范和参考</a>。
     * 注意：返回的集合是不可变的。
     *
     * @param serviceName 指定 Java 加密服务的名称（例如，Signature, MessageDigest, Cipher, Mac, KeyStore）。
     * 注意：此参数不区分大小写。
     *
     * @return 一个包含指定 Java 加密服务所有可用算法或类型的字符串集，如果没有任何提供者支持指定的服务，则返回一个空集。
     *
     * @since 1.4
     **/
    public static Set<String> getAlgorithms(String serviceName) {

        if ((serviceName == null) || (serviceName.length() == 0) ||
            (serviceName.endsWith("."))) {
            return Collections.emptySet();
        }

        HashSet<String> result = new HashSet<>();
        Provider[] providers = Security.getProviders();

        for (int i = 0; i < providers.length; i++) {
            // 检查每个提供者的键。
            for (Enumeration<Object> e = providers[i].keys();
                                                e.hasMoreElements(); ) {
                String currentKey =
                        ((String)e.nextElement()).toUpperCase(Locale.ENGLISH);
                if (currentKey.startsWith(
                        serviceName.toUpperCase(Locale.ENGLISH))) {
                    // 如果 currentKey 包含空格，我们应该跳过它。原因是：提供者属性中的此类条目包含算法实现的属性。我们只对指向实现类的条目感兴趣。
                    if (currentKey.indexOf(" ") < 0) {
                        result.add(currentKey.substring(
                                                serviceName.length() + 1));
                    }
                }
            }
        }
        return Collections.unmodifiableSet(result);
    }
}
