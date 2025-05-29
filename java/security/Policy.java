
/*
 * Copyright (c) 1997, 2020, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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


package java.security;

import java.util.Enumeration;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import sun.security.jca.GetInstance;
import sun.security.util.Debug;
import sun.security.util.SecurityConstants;


/**
 * 一个 Policy 对象负责确定在 Java 运行时环境中执行的代码是否有权限执行
 * 安全敏感操作。
 *
 * <p> 在任何给定时间，运行时中只安装了一个 Policy 对象。可以通过调用
 * {@code setPolicy} 方法来安装 Policy 对象。可以通过调用 {@code getPolicy} 方法
 * 获取已安装的 Policy 对象。
 *
 * <p> 如果运行时中没有安装 Policy 对象，调用 {@code getPolicy} 会安装默认 Policy
 * 实现的一个实例（此抽象类的默认子类实现）。可以通过将 {@code policy.provider} 安全属性
 * 设置为所需 Policy 子类实现的完全限定名称来更改默认 Policy 实现。
 *
 * <p> 应用程序代码可以直接继承 Policy 以提供自定义实现。此外，可以通过调用带有标准类型的
 * 一个 {@code getInstance} 工厂方法来构造 Policy 对象的实例。默认策略类型是 "JavaPolicy"。
 *
 * <p> 一旦安装了 Policy 实例（无论是默认安装还是通过调用 {@code setPolicy} 安装），Java 运行时
 * 在需要确定执行代码（封装在 ProtectionDomain 中）是否可以执行 SecurityManager 保护的操作时，
 * 会调用其 {@code implies} 方法。Policy 对象如何检索其策略数据取决于 Policy 实现本身。
 * 策略数据可以存储在例如平面 ASCII 文件、Policy 类的序列化二进制文件或数据库中。
 *
 * <p> {@code refresh} 方法导致策略对象刷新/重新加载其数据。此操作是实现依赖的。
 * 例如，如果策略对象将其数据存储在配置文件中，调用 {@code refresh} 将导致其重新读取配置
 * 策略文件。如果刷新操作不支持，此方法将不执行任何操作。注意，刷新的策略可能不会对特定
 * ProtectionDomain 中的类产生影响。这取决于 Policy 提供者的 {@code implies}
 * 方法实现及其 PermissionCollection 缓存策略。
 *
 * @author Roland Schemers
 * @author Gary Ellison
 * @see java.security.Provider
 * @see java.security.ProtectionDomain
 * @see java.security.Permission
 * @see java.security.Security 安全属性
 */

public abstract class Policy {

    /**
     * 一个只读的空 PermissionCollection 实例。
     * @since 1.6
     */
    public static final PermissionCollection UNSUPPORTED_EMPTY_COLLECTION =
                        new UnsupportedEmptyCollection();

    // 关于系统范围策略的信息。
    private static class PolicyInfo {
        // 系统范围的策略
        final Policy policy;
        // 一个标志，表示系统范围的策略是否已初始化
        final boolean initialized;

        PolicyInfo(Policy policy, boolean initialized) {
            this.policy = policy;
            this.initialized = initialized;
        }
    }

    // PolicyInfo 存储在 AtomicReference 中
    private static AtomicReference<PolicyInfo> policy =
        new AtomicReference<>(new PolicyInfo(null, false));

    private static final Debug debug = Debug.getInstance("policy");

    // 缓存映射 ProtectionDomain.Key 到 PermissionCollection
    private WeakHashMap<ProtectionDomain.Key, PermissionCollection> pdMapping;

    /** 包私有，供 AccessControlContext 和 ProtectionDomain 使用 */
    static boolean isSet()
    {
        PolicyInfo pi = policy.get();
        return pi.policy != null && pi.initialized == true;
    }

    private static void checkPermission(String type) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SecurityPermission("createPolicy." + type));
        }
    }

    /**
     * 返回已安装的 Policy 对象。此值不应被缓存，
     * 因为它可能通过调用 {@code setPolicy} 被更改。
     * 此方法首先调用
     * {@code SecurityManager.checkPermission}，使用
     * {@code SecurityPermission("getPolicy")} 权限
     * 以确保获取 Policy 对象是允许的。
     *
     * @return 已安装的 Policy。
     *
     * @throws SecurityException
     *        如果存在安全管理器且其
     *        {@code checkPermission} 方法不允许
     *        获取 Policy 对象。
     *
     * @see SecurityManager#checkPermission(Permission)
     * @see #setPolicy(java.security.Policy)
     */
    public static Policy getPolicy()
    {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(SecurityConstants.GET_POLICY_PERMISSION);
        return getPolicyNoCheck();
    }

    /**
     * 返回已安装的 Policy 对象，跳过安全检查。
     * 由 ProtectionDomain 和 getPolicy 使用。
     *
     * @return 已安装的 Policy。
     */
    static Policy getPolicyNoCheck()
    {
        PolicyInfo pi = policy.get();
        // 使用双重检查模式以避免在系统范围策略已初始化时锁定
        if (pi.initialized == false || pi.policy == null) {
            synchronized (Policy.class) {
                PolicyInfo pinfo = policy.get();
                if (pinfo.policy == null) {
                    String policy_class = AccessController.doPrivileged(
                        new PrivilegedAction<String>() {
                        public String run() {
                            return Security.getProperty("policy.provider");
                        }
                    });
                    if (policy_class == null) {
                        policy_class = "sun.security.provider.PolicyFile";
                    }


                                try {
                        pinfo = new PolicyInfo(
                            (Policy) Class.forName(policy_class).newInstance(),
                            true);
                    } catch (Exception e) {
                        /*
                         * 策略类似乎是扩展的一部分
                         * 因此我们必须通过引导加载位于引导类路径上的策略提供者来加载它。
                         * 如果加载成功，则切换到使用配置的提供者。
                         */

                        // 安装引导提供者以避免递归
                        Policy polFile = new sun.security.provider.PolicyFile();
                        pinfo = new PolicyInfo(polFile, false);
                        policy.set(pinfo);

                        final String pc = policy_class;
                        Policy pol = AccessController.doPrivileged(
                            new PrivilegedAction<Policy>() {
                            public Policy run() {
                                try {
                                    ClassLoader cl =
                                            ClassLoader.getSystemClassLoader();
                                    // 我们需要扩展加载器
                                    ClassLoader extcl = null;
                                    while (cl != null) {
                                        extcl = cl;
                                        cl = cl.getParent();
                                    }
                                    return (extcl != null ? (Policy)Class.forName(
                                            pc, true, extcl).newInstance() : null);
                                } catch (Exception e) {
                                    if (debug != null) {
                                        debug.println("策略提供者 " +
                                                    pc +
                                                    " 不可用");
                                        e.printStackTrace();
                                    }
                                    return null;
                                }
                            }
                        });
                        /*
                         * 如果加载成功，则将其安装为策略提供者。否则
                         * 继续使用系统默认实现
                         */
                        if (pol != null) {
                            pinfo = new PolicyInfo(pol, true);
                        } else {
                            if (debug != null) {
                                debug.println("使用 sun.security.provider.PolicyFile");
                            }
                            pinfo = new PolicyInfo(polFile, true);
                        }
                    }
                    policy.set(pinfo);
                }
                return pinfo.policy;
            }
        }
        return pi.policy;
    }

    /**
     * 设置系统范围的 Policy 对象。此方法首先调用
     * {@code SecurityManager.checkPermission} 并传递
     * {@code SecurityPermission("setPolicy")}
     * 权限以确保设置 Policy 是允许的。
     *
     * @param p 新的系统 Policy 对象。
     *
     * @throws SecurityException
     *        如果存在安全经理，并且其
     *        {@code checkPermission} 方法不允许
     *        设置 Policy。
     *
     * @see SecurityManager#checkPermission(Permission)
     * @see #getPolicy()
     *
     */
    public static void setPolicy(Policy p)
    {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(
                                 new SecurityPermission("setPolicy"));
        if (p != null) {
            initPolicy(p);
        }
        synchronized (Policy.class) {
            policy.set(new PolicyInfo(p, p != null));
        }
    }

    /**
     * 初始化超类状态，以便遗留提供者可以处理自身的查询。
     *
     * @since 1.4
     */
    private static void initPolicy (final Policy p) {
        /*
         * 未在引导类路径上的策略提供者可能会触发
         * 对 Policy.implies 或 Policy.getPermissions 的调用的安全检查。如果确实发生了这种情况，提供者
         * 必须能够回答其自身的 ProtectionDomain 而不会触发额外的安全检查，否则
         * 策略实现将陷入无限递归。
         *
         * 为了解决这个问题，提供者可以在安装过程中收集其自身的
         * ProtectionDomain 并关联一个 PermissionCollection。当前安装的策略
         * 提供者（如果有）将在这一过程中处理对 Policy.implies 或 Policy.getPermissions 的调用。
         *
         * 这个 Policy 超类缓存 ProtectionDomain 并静态绑定权限，以确保遗留 Policy
         * 实现能够继续正常工作。
         */

        ProtectionDomain policyDomain =
        AccessController.doPrivileged(new PrivilegedAction<ProtectionDomain>() {
            public ProtectionDomain run() {
                return p.getClass().getProtectionDomain();
            }
        });

        /*
         * 收集授予此保护域的权限
         * 以便在处理 Policy.implies 或 Policy.getPermissions 的调用时对提供者进行安全检查。
         */
        PermissionCollection policyPerms = null;
        synchronized (p) {
            if (p.pdMapping == null) {
                p.pdMapping = new WeakHashMap<>();
           }
        }

        if (policyDomain.getCodeSource() != null) {
            Policy pol = policy.get().policy;
            if (pol != null) {
                policyPerms = pol.getPermissions(policyDomain);
            }


                        if (policyPerms == null) { // 假设它具有所有权限
                policyPerms = new Permissions();
                policyPerms.add(SecurityConstants.ALL_PERMISSION);
            }

            synchronized (p.pdMapping) {
                // pd 到权限的缓存
                p.pdMapping.put(policyDomain.key, policyPerms);
            }
        }
        return;
    }


    /**
     * 返回指定类型的 Policy 对象。
     *
     * <p> 该方法遍历注册的安全提供者列表，
     * 从最优先的 Provider 开始。
     * 返回一个新的 Policy 对象，封装了
     * 第一个支持指定类型的 Provider 的
     * PolicySpi 实现。
     *
     * <p> 注册的提供者列表可以通过
     * {@link Security#getProviders() Security.getProviders()} 方法获取。
     *
     * @param type 指定的 Policy 类型。请参阅 Policy 部分的
     *    <a href=
     *    "{@docRoot}/../technotes/guides/security/StandardNames.html#Policy">
     *    Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     *    以获取标准 Policy 类型的列表。
     *
     * @param params Policy 的参数，可以为 null。
     *
     * @return 新的 Policy 对象。
     *
     * @exception SecurityException 如果调用者没有权限获取指定类型的 Policy 实例。
     *
     * @exception NullPointerException 如果指定的类型为 null。
     *
     * @exception IllegalArgumentException 如果指定的参数不被选定的 Provider 的 PolicySpi 实现所理解。
     *
     * @exception NoSuchAlgorithmException 如果没有 Provider 支持指定类型的 PolicySpi 实现。
     *
     * @see Provider
     * @since 1.6
     */
    public static Policy getInstance(String type, Policy.Parameters params)
                throws NoSuchAlgorithmException {

        checkPermission(type);
        try {
            GetInstance.Instance instance = GetInstance.getInstance("Policy",
                                                        PolicySpi.class,
                                                        type,
                                                        params);
            return new PolicyDelegate((PolicySpi)instance.impl,
                                                        instance.provider,
                                                        type,
                                                        params);
        } catch (NoSuchAlgorithmException nsae) {
            return handleException(nsae);
        }
    }

    /**
     * 返回指定类型的 Policy 对象。
     *
     * <p> 返回一个新的 Policy 对象，封装了
     * 指定提供者的 PolicySpi 实现。
     * 指定的提供者必须在提供者列表中注册。
     *
     * <p> 注册的提供者列表可以通过
     * {@link Security#getProviders() Security.getProviders()} 方法获取。
     *
     * @param type 指定的 Policy 类型。请参阅 Policy 部分的
     *    <a href=
     *    "{@docRoot}/../technotes/guides/security/StandardNames.html#Policy">
     *    Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     *    以获取标准 Policy 类型的列表。
     *
     * @param params Policy 的参数，可以为 null。
     *
     * @param provider 指定的提供者。
     *
     * @return 新的 Policy 对象。
     *
     * @exception SecurityException 如果调用者没有权限获取指定类型的 Policy 实例。
     *
     * @exception NullPointerException 如果指定的类型为 null。
     *
     * @exception IllegalArgumentException 如果指定的提供者为 null 或为空，
     *          或者指定的参数不被指定提供者的 PolicySpi 实现所理解。
     *
     * @exception NoSuchProviderException 如果指定的提供者未在安全提供者列表中注册。
     *
     * @exception NoSuchAlgorithmException 如果指定的提供者不支持指定类型的 PolicySpi 实现。
     *
     * @see Provider
     * @since 1.6
     */
    public static Policy getInstance(String type,
                                Policy.Parameters params,
                                String provider)
                throws NoSuchProviderException, NoSuchAlgorithmException {

        if (provider == null || provider.isEmpty()) {
            throw new IllegalArgumentException("缺少提供者");
        }

        checkPermission(type);
        try {
            GetInstance.Instance instance = GetInstance.getInstance("Policy",
                                                        PolicySpi.class,
                                                        type,
                                                        params,
                                                        provider);
            return new PolicyDelegate((PolicySpi)instance.impl,
                                                        instance.provider,
                                                        type,
                                                        params);
        } catch (NoSuchAlgorithmException nsae) {
            return handleException(nsae);
        }
    }

    /**
     * 返回指定类型的 Policy 对象。
     *
     * <p> 返回一个新的 Policy 对象，封装了
     * 指定 Provider 对象的 PolicySpi 实现。
     * 注意，指定的 Provider 对象
     * 不必在提供者列表中注册。
     *
     * @param type 指定的 Policy 类型。请参阅 Policy 部分的
     *    <a href=
     *    "{@docRoot}/../technotes/guides/security/StandardNames.html#Policy">
     *    Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     *    以获取标准 Policy 类型的列表。
     *
     * @param params Policy 的参数，可以为 null。
     *
     * @param provider 指定的 Provider。
     *
     * @return 新的 Policy 对象。
     *
     * @exception SecurityException 如果调用者没有权限获取指定类型的 Policy 实例。
     *
     * @exception NullPointerException 如果指定的类型为 null。
     *
     * @exception IllegalArgumentException 如果指定的 Provider 为 null，
     *          或者指定的参数不被指定 Provider 的 PolicySpi 实现所理解。
     *
     * @exception NoSuchAlgorithmException 如果指定的 Provider 不支持指定类型的 PolicySpi 实现。
     *
     * @see Provider
     * @since 1.6
     */
    public static Policy getInstance(String type,
                                Policy.Parameters params,
                                Provider provider)
                throws NoSuchAlgorithmException {


                    if (provider == null) {
            throw new IllegalArgumentException("缺少提供者");
        }

        checkPermission(type);
        try {
            GetInstance.Instance instance = GetInstance.getInstance("Policy",
                                                        PolicySpi.class,
                                                        type,
                                                        params,
                                                        provider);
            return new PolicyDelegate((PolicySpi)instance.impl,
                                                        instance.provider,
                                                        type,
                                                        params);
        } catch (NoSuchAlgorithmException nsae) {
            return handleException(nsae);
        }
    }

    private static Policy handleException(NoSuchAlgorithmException nsae)
                throws NoSuchAlgorithmException {
        Throwable cause = nsae.getCause();
        if (cause instanceof IllegalArgumentException) {
            throw (IllegalArgumentException)cause;
        }
        throw nsae;
    }

    /**
     * 返回此 Policy 的提供者。
     *
     * <p> 仅当此 Policy 实例是通过调用 {@code Policy.getInstance} 获得时，它才会有提供者。
     * 否则，此方法返回 null。
     *
     * @return 此 Policy 的提供者，或 null。
     *
     * @since 1.6
     */
    public Provider getProvider() {
        return null;
    }

    /**
     * 返回此 Policy 的类型。
     *
     * <p> 仅当此 Policy 实例是通过调用 {@code Policy.getInstance} 获得时，它才会有类型。
     * 否则，此方法返回 null。
     *
     * @return 此 Policy 的类型，或 null。
     *
     * @since 1.6
     */
    public String getType() {
        return null;
    }

    /**
     * 返回 Policy 参数。
     *
     * <p> 仅当此 Policy 实例是通过调用 {@code Policy.getInstance} 获得时，它才会有参数。
     * 否则，此方法返回 null。
     *
     * @return Policy 参数，或 null。
     *
     * @since 1.6
     */
    public Policy.Parameters getParameters() {
        return null;
    }

    /**
     * 返回一个包含授予指定 CodeSource 的权限集的 PermissionCollection 对象。
     *
     * <p> 不建议应用程序调用此方法，因为并非所有策略实现都支持此操作。
     * 应用程序应仅依赖于 {@code implies} 方法来执行策略检查。如果应用程序必须调用
     * getPermissions 方法，它应该调用 {@code getPermissions(ProtectionDomain)}。
     *
     * <p> 此方法的默认实现返回 Policy.UNSUPPORTED_EMPTY_COLLECTION。如果策略实现可以返回
     * 授予 CodeSource 的权限集，可以重写此方法。
     *
     * @param codesource 授予返回的 PermissionCollection 的 CodeSource。
     *
     * @return 授予指定 CodeSource 的权限集。如果此操作受支持，返回的权限集必须是一个新的可变实例，
     *         并且必须支持异构权限类型。如果此操作不受支持，返回 Policy.UNSUPPORTED_EMPTY_COLLECTION。
     */
    public PermissionCollection getPermissions(CodeSource codesource) {
        return Policy.UNSUPPORTED_EMPTY_COLLECTION;
    }

    /**
     * 返回一个包含授予指定 ProtectionDomain 的权限集的 PermissionCollection 对象。
     *
     * <p> 不建议应用程序调用此方法，因为并非所有策略实现都支持此操作。
     * 应用程序应依赖于 {@code implies} 方法来执行策略检查。
     *
     * <p> 此方法的默认实现首先通过 {@code getPermissions(CodeSource)}（从指定的 ProtectionDomain 获取 CodeSource）
     * 获取权限，以及指定 ProtectionDomain 内部的权限。所有这些权限随后被组合并返回在一个新的 PermissionCollection 对象中。
     * 如果 {@code getPermissions(CodeSource)} 返回 Policy.UNSUPPORTED_EMPTY_COLLECTION，则此方法返回
     * 包含在指定 ProtectionDomain 内部的权限的新 PermissionCollection 对象。
     *
     * <p> 如果策略实现支持返回授予 ProtectionDomain 的权限集，可以重写此方法。
     *
     * @param domain 授予返回的 PermissionCollection 的 ProtectionDomain。
     *
     * @return 授予指定 ProtectionDomain 的权限集。如果此操作受支持，返回的权限集必须是一个新的可变实例，
     *         并且必须支持异构权限类型。如果此操作不受支持，返回 Policy.UNSUPPORTED_EMPTY_COLLECTION。
     *
     * @since 1.4
     */
    public PermissionCollection getPermissions(ProtectionDomain domain) {
        PermissionCollection pc = null;

        if (domain == null)
            return new Permissions();

        if (pdMapping == null) {
            initPolicy(this);
        }

        synchronized (pdMapping) {
            pc = pdMapping.get(domain.key);
        }

        if (pc != null) {
            Permissions perms = new Permissions();
            synchronized (pc) {
                for (Enumeration<Permission> e = pc.elements() ; e.hasMoreElements() ;) {
                    perms.add(e.nextElement());
                }
            }
            return perms;
        }

        pc = getPermissions(domain.getCodeSource());
        if (pc == null || pc == UNSUPPORTED_EMPTY_COLLECTION) {
            pc = new Permissions();
        }


                    addStaticPerms(pc, domain.getPermissions());
        return pc;
    }

    /**
     * 向提供的权限集合中添加静态权限
     */
    private void addStaticPerms(PermissionCollection perms,
                                PermissionCollection statics) {
        if (statics != null) {
            synchronized (statics) {
                Enumeration<Permission> e = statics.elements();
                while (e.hasMoreElements()) {
                    perms.add(e.nextElement());
                }
            }
        }
    }

    /**
     * 评估授予 ProtectionDomain 的全局策略权限，并测试是否授予了指定的权限。
     *
     * @param domain 要测试的 ProtectionDomain
     * @param permission 要测试的 Permission 对象。
     *
     * @return 如果 "permission" 是授予此 ProtectionDomain 的权限的适当子集，则返回 true。
     *
     * @see java.security.ProtectionDomain
     * @since 1.4
     */
    public boolean implies(ProtectionDomain domain, Permission permission) {
        PermissionCollection pc;

        if (pdMapping == null) {
            initPolicy(this);
        }

        synchronized (pdMapping) {
            pc = pdMapping.get(domain.key);
        }

        if (pc != null) {
            return pc.implies(permission);
        }

        pc = getPermissions(domain);
        if (pc == null) {
            return false;
        }

        synchronized (pdMapping) {
            // 缓存它
            pdMapping.put(domain.key, pc);
        }

        return pc.implies(permission);
    }

    /**
     * 刷新/重新加载策略配置。此方法的行为取决于实现。例如，对基于文件的策略调用 {@code refresh}
     * 将导致文件被重新读取。
     *
     * <p> 默认实现此方法不执行任何操作。如果策略实现支持刷新操作，
     * 则应覆盖此方法。
     */
    public void refresh() { }

    /**
     * 由 getInstance 调用返回的子类。所有 Policy 调用都委托给底层的 PolicySpi。
     */
    private static class PolicyDelegate extends Policy {

        private PolicySpi spi;
        private Provider p;
        private String type;
        private Policy.Parameters params;

        private PolicyDelegate(PolicySpi spi, Provider p,
                        String type, Policy.Parameters params) {
            this.spi = spi;
            this.p = p;
            this.type = type;
            this.params = params;
        }

        @Override public String getType() { return type; }

        @Override public Policy.Parameters getParameters() { return params; }

        @Override public Provider getProvider() { return p; }

        @Override
        public PermissionCollection getPermissions(CodeSource codesource) {
            return spi.engineGetPermissions(codesource);
        }
        @Override
        public PermissionCollection getPermissions(ProtectionDomain domain) {
            return spi.engineGetPermissions(domain);
        }
        @Override
        public boolean implies(ProtectionDomain domain, Permission perm) {
            return spi.engineImplies(domain, perm);
        }
        @Override
        public void refresh() {
            spi.engineRefresh();
        }
    }

    /**
     * 此接口表示 Policy 参数的标记接口。
     *
     * @since 1.6
     */
    public static interface Parameters { }

    /**
     * 此类表示一个只读的空 PermissionCollection 对象，当 Policy 实现不支持
     * {@code getPermissions(CodeSource)} 和 {@code getPermissions(ProtectionDomain)}
     * 方法的操作时，从 Policy 类的这些方法返回。
     */
    private static class UnsupportedEmptyCollection
        extends PermissionCollection {

        private static final long serialVersionUID = -8492269157353014774L;

        private Permissions perms;

        /**
         * 创建一个只读的空 PermissionCollection 对象。
         */
        public UnsupportedEmptyCollection() {
            this.perms = new Permissions();
            perms.setReadOnly();
        }

        /**
         * 向当前权限对象集合中添加一个权限对象。
         *
         * @param permission 要添加的 Permission 对象。
         *
         * @exception SecurityException - 如果此 PermissionCollection 对象已被标记为只读
         */
        @Override public void add(Permission permission) {
            perms.add(permission);
        }

        /**
         * 检查指定的权限是否被此 PermissionCollection 中持有的权限对象集合所隐含。
         *
         * @param permission 要比较的 Permission 对象。
         *
         * @return 如果 "permission" 被集合中的权限隐含，则返回 true，否则返回 false。
         */
        @Override public boolean implies(Permission permission) {
            return perms.implies(permission);
        }

        /**
         * 返回集合中所有 Permission 对象的枚举。
         *
         * @return 所有 Permissions 的枚举。
         */
        @Override public Enumeration<Permission> elements() {
            return perms.elements();
        }
    }
}
