
/*
 * 版权所有 (c) 1996, 2020, Oracle 和/或其附属公司。保留所有权利。
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

package java.security;

import java.io.*;
import java.util.*;
import static java.util.Locale.ENGLISH;
import java.lang.ref.*;
import java.lang.reflect.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 该类表示 Java 安全 API 的“提供者”，其中提供者实现 Java 安全的一部分或全部。
 * 提供者可能实现的服务包括：
 *
 * <ul>
 *
 * <li>算法（如 DSA、RSA、MD5 或 SHA-1）。
 *
 * <li>密钥生成、转换和管理设施（如特定算法的密钥）。
 *
 *</ul>
 *
 * <p>每个提供者都有一个名称和版本号，并在安装的每个运行时中进行配置。
 *
 * <p>有关特定类型的提供者（加密服务提供者）的工作原理和安装方法的详细信息，请参阅
 * <a href =
 * "../../../technotes/guides/security/crypto/CryptoSpec.html#Provider">“Java 加密架构 API 规范和参考”中的“提供者类”</a>。
 * 但是，请注意，提供者可用于实现 Java 中使用可插拔架构并提供多种实现选择的任何安全服务。
 *
 * <p>某些提供者实现可能在运行过程中遇到无法恢复的内部错误，例如与安全令牌通信失败。
 * 应使用 {@link ProviderException} 表示此类错误。
 *
 * <p>服务类型 {@code Provider} 保留供安全框架使用。应用程序不能添加、删除或修改此类服务。
 * 以下属性将自动放置在每个 Provider 对象中：
 * <table cellspacing=4>
 * <caption><b>自动放置在 Provider 对象中的属性</b></caption>
 * <tr><th>名称</th><th>值</th>
 * <tr><td>{@code Provider.id name}</td>
  *    <td>{@code String.valueOf(provider.getName())}</td>
 * <tr><td>{@code Provider.id version}</td>
 *     <td>{@code String.valueOf(provider.getVersion())}</td>
 * <tr><td>{@code Provider.id info}</td>
       <td>{@code String.valueOf(provider.getInfo())}</td>
 * <tr><td>{@code Provider.id className}</td>
 *     <td>{@code provider.getClass().getName()}</td>
 * </table>
 *
 * @author Benjamin Renaud
 * @author Andreas Sterbenz
 */
public abstract class Provider extends Properties {

    // 声明 serialVersionUID 以与 JDK1.1 兼容
    static final long serialVersionUID = -4298000515446427739L;

    private static final sun.security.util.Debug debug =
        sun.security.util.Debug.getInstance
        ("provider", "Provider");

    /**
     * 提供者名称。
     *
     * @serial
     */
    private String name;

    /**
     * 提供者及其服务的描述。
     *
     * @serial
     */
    private String info;

    /**
     * 提供者版本号。
     *
     * @serial
     */
    private double version;


    private transient Set<Map.Entry<Object,Object>> entrySet = null;
    private transient int entrySetCallCount = 0;

    private transient boolean initialized;

    /**
     * 使用指定的名称、版本号和信息构造提供者。
     *
     * @param name 提供者名称。
     *
     * @param version 提供者版本号。
     *
     * @param info 提供者及其服务的描述。
     */
    protected Provider(String name, double version, String info) {
        this.name = name;
        this.version = version;
        this.info = info;
        this.serviceMap = new ConcurrentHashMap<>();
        putId();
        initialized = true;
    }

    /**
     * 返回此提供者的名称。
     *
     * @return 此提供者的名称。
     */
    public String getName() {
        return name;
    }

    /**
     * 返回此提供者的版本号。
     *
     * @return 此提供者的版本号。
     */
    public double getVersion() {
        return version;
    }

    /**
     * 返回提供者及其服务的人类可读描述。这可能返回一个 HTML 页面，包含相关链接。
     *
     * @return 提供者及其服务的描述。
     */
    public String getInfo() {
        return info;
    }

    /**
     * 返回包含此提供者名称和版本号的字符串。
     *
     * @return 包含此提供者名称和版本号的字符串。
     */
    public String toString() {
        return name + " version " + version;
    }

    /*
     * 覆盖以下方法以确保只有调用者具有适当权限时才能更改提供者信息。
     */

    /**
     * 清除此提供者，使其不再包含用于查找提供者实现的设施的属性。
     *
     * <p>如果启用了安全经理，将调用其 {@code checkSecurityAccess}
     * 方法，参数为 {@code "clearProviderProperties."+name}
     * （其中 {@code name} 是提供者名称），以确定是否可以清除此提供者。
     *
     * @throws  SecurityException
     *          如果存在安全经理且其 {@link
     *          java.lang.SecurityManager#checkSecurityAccess} 方法
     *          拒绝访问以清除此提供者
     *
     * @since 1.2
     */
    @Override
    public synchronized void clear() {
        check("clearProviderProperties."+name);
        if (debug != null) {
            debug.println("Remove " + name + " provider properties");
        }
        implClear();
    }

    /**
     * 从输入流中读取属性列表（键和元素对）。
     *
     * @param inStream   输入流。
     * @exception  IOException  如果从输入流读取时发生错误。
     * @see java.util.Properties#load
     */
    @Override
    public synchronized void load(InputStream inStream) throws IOException {
        check("putProviderProperty."+name);
        if (debug != null) {
            debug.println("Load " + name + " provider properties");
        }
        Properties tempProperties = new Properties();
        tempProperties.load(inStream);
        implPutAll(tempProperties);
    }


    /**
     * 将指定的 Map 中的所有映射复制到此提供者中。
     * 这些映射将替换此提供者中当前在指定 Map 中的任何键的任何属性。
     *
     * @since 1.2
     */
    @Override
    public synchronized void putAll(Map<?,?> t) {
        check("putProviderProperty."+name);
        if (debug != null) {
            debug.println("Put all " + name + " provider properties");
        }
        implPutAll(t);
    }

    /**
     * 返回此提供者中包含的属性条目的不可修改 Set 视图。
     *
     * @see   java.util.Map.Entry
     * @since 1.2
     */
    @Override
    public synchronized Set<Map.Entry<Object,Object>> entrySet() {
        checkInitialized();
        if (entrySet == null) {
            if (entrySetCallCount++ == 0)  // 初始调用
                entrySet = Collections.unmodifiableMap(this).entrySet();
            else
                return super.entrySet();   // 递归调用
        }

        // 如果 Collections.unmodifiableMap.entrySet() 的实现发生更改，使得它不再调用支持 Map 的 entrySet()，
        // 则将抛出此异常。 （提供者的 entrySet 实现依赖于这个“实现细节”，这不太可能改变。）
        if (entrySetCallCount != 2)
            throw new RuntimeException("内部错误。");

        return entrySet;
    }

    /**
     * 返回此提供者中包含的属性键的不可修改 Set 视图。
     *
     * @since 1.2
     */
    @Override
    public Set<Object> keySet() {
        checkInitialized();
        return Collections.unmodifiableSet(super.keySet());
    }

    /**
     * 返回此提供者中包含的属性值的不可修改 Collection 视图。
     *
     * @since 1.2
     */
    @Override
    public Collection<Object> values() {
        checkInitialized();
        return Collections.unmodifiableCollection(super.values());
    }

    /**
     * 设置 {@code key} 属性具有指定的 {@code value}。
     *
     * <p>如果启用了安全管理器，将调用其 {@code checkSecurityAccess} 方法，使用字符串 {@code "putProviderProperty."+name}，
     * 其中 {@code name} 是提供者名称，以检查是否可以设置此提供者的属性值。
     *
     * @throws  SecurityException
     *          如果存在安全管理器，并且其 {@link java.lang.SecurityManager#checkSecurityAccess} 方法
     *          拒绝访问设置属性值。
     *
     * @since 1.2
     */
    @Override
    public synchronized Object put(Object key, Object value) {
        check("putProviderProperty."+name);
        if (debug != null) {
            debug.println("Set " + name + " provider property [" +
                          key + "/" + value +"]");
        }
        return implPut(key, value);
    }

    /**
     * 如果指定的键尚未与值关联（或映射到 {@code null}），则将其与给定值关联并返回 {@code null}，否则返回当前值。
     *
     * <p>如果启用了安全管理器，将调用其 {@code checkSecurityAccess} 方法，使用字符串 {@code "putProviderProperty."+name}，
     * 其中 {@code name} 是提供者名称，以检查是否可以设置此提供者的属性值。
     *
     * @throws  SecurityException
     *          如果存在安全管理器，并且其 {@link java.lang.SecurityManager#checkSecurityAccess} 方法
     *          拒绝访问设置属性值。
     *
     * @since 1.8
     */
    @Override
    public synchronized Object putIfAbsent(Object key, Object value) {
        check("putProviderProperty."+name);
        if (debug != null) {
            debug.println("Set " + name + " provider property [" +
                          key + "/" + value +"]");
        }
        return implPutIfAbsent(key, value);
    }

    /**
     * 删除 {@code key} 属性（及其对应的 {@code value}）。
     *
     * <p>如果启用了安全管理器，将调用其 {@code checkSecurityAccess} 方法，使用字符串 {@code "removeProviderProperty."+name}，
     * 其中 {@code name} 是提供者名称，以检查是否可以删除此提供者的属性。
     *
     * @throws  SecurityException
     *          如果存在安全管理器，并且其 {@link java.lang.SecurityManager#checkSecurityAccess} 方法
     *          拒绝访问删除此提供者的属性。
     *
     * @since 1.2
     */
    @Override
    public synchronized Object remove(Object key) {
        check("removeProviderProperty."+name);
        if (debug != null) {
            debug.println("Remove " + name + " provider property " + key);
        }
        return implRemove(key);
    }

    /**
     * 仅当指定的键当前映射到指定的值时，才删除该键的条目。
     *
     * <p>如果启用了安全管理器，将调用其 {@code checkSecurityAccess} 方法，使用字符串 {@code "removeProviderProperty."+name}，
     * 其中 {@code name} 是提供者名称，以检查是否可以删除此提供者的属性。
     *
     * @throws  SecurityException
     *          如果存在安全管理器，并且其 {@link java.lang.SecurityManager#checkSecurityAccess} 方法
     *          拒绝访问删除此提供者的属性。
     *
     * @since 1.8
     */
    @Override
    public synchronized boolean remove(Object key, Object value) {
        check("removeProviderProperty."+name);
        if (debug != null) {
            debug.println("Remove " + name + " provider property " + key);
        }
        return implRemove(key, value);
    }

    /**
     * 仅当指定的键当前映射到指定的值时，才替换该键的条目。
     *
     * <p>如果启用了安全管理器，将调用其 {@code checkSecurityAccess} 方法，使用字符串 {@code "putProviderProperty."+name}，
     * 其中 {@code name} 是提供者名称，以检查是否可以设置此提供者的属性值。
     *
     * @throws  SecurityException
     *          如果存在安全管理器，并且其 {@link java.lang.SecurityManager#checkSecurityAccess} 方法
     *          拒绝访问设置属性值。
     *
     * @since 1.8
     */
    @Override
    public synchronized boolean replace(Object key, Object oldValue,
            Object newValue) {
        check("putProviderProperty." + name);


                    if (debug != null) {
            debug.println("Replace " + name + " provider property " + key);
        }
        return implReplace(key, oldValue, newValue);
    }

    /**
     * 仅在指定键当前映射到某个值时，替换该键的条目。
     *
     * <p>如果启用了安全管理器，其 {@code checkSecurityAccess}
     * 方法将被调用，参数为字符串 {@code "putProviderProperty."+name}，
     * 其中 {@code name} 是提供者名称，以确定是否可以设置此提供者的属性值。
     *
     * @throws  SecurityException
     *          如果存在安全管理器且其 {@link
     *          java.lang.SecurityManager#checkSecurityAccess} 方法
     *          拒绝设置属性值的访问。
     *
     * @since 1.8
     */
    @Override
    public synchronized Object replace(Object key, Object value) {
        check("putProviderProperty." + name);

        if (debug != null) {
            debug.println("Replace " + name + " provider property " + key);
        }
        return implReplace(key, value);
    }

    /**
     * 以条目集迭代器返回的顺序调用给定函数，用该函数的结果替换每个条目的值，直到所有条目都被处理或函数抛出异常。
     *
     * <p>如果启用了安全管理器，其 {@code checkSecurityAccess}
     * 方法将被调用，参数为字符串 {@code "putProviderProperty."+name}，
     * 其中 {@code name} 是提供者名称，以确定是否可以设置此提供者的属性值。
     *
     * @throws  SecurityException
     *          如果存在安全管理器且其 {@link
     *          java.lang.SecurityManager#checkSecurityAccess} 方法
     *          拒绝设置属性值的访问。
     *
     * @since 1.8
     */
    @Override
    public synchronized void replaceAll(BiFunction<? super Object, ? super Object, ? extends Object> function) {
        check("putProviderProperty." + name);

        if (debug != null) {
            debug.println("ReplaceAll " + name + " provider property ");
        }
        implReplaceAll(function);
    }

    /**
     * 尝试为指定键及其当前映射值（如果当前没有映射，则为 {@code null}）计算映射。
     *
     * <p>如果启用了安全管理器，其 {@code checkSecurityAccess}
     * 方法将被调用，参数为字符串 {@code "putProviderProperty."+name}
     * 和 {@code "removeProviderProperty."+name}，其中 {@code name} 是
     * 提供者名称，以确定是否可以设置此提供者的属性值和删除此提供者的属性。
     *
     * @throws  SecurityException
     *          如果存在安全管理器且其 {@link
     *          java.lang.SecurityManager#checkSecurityAccess} 方法
     *          拒绝设置属性值或删除属性的访问。
     *
     * @since 1.8
     */
    @Override
    public synchronized Object compute(Object key,
        BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction) {
        check("putProviderProperty." + name);
        check("removeProviderProperty" + name);

        if (debug != null) {
            debug.println("Compute " + name + " provider property " + key);
        }
        return implCompute(key, remappingFunction);
    }

    /**
     * 如果指定键尚未与值关联（或映射到 {@code null}），则尝试使用给定的映射函数计算其值，并将其输入到此映射中，除非结果为 {@code null}。
     *
     * <p>如果启用了安全管理器，其 {@code checkSecurityAccess}
     * 方法将被调用，参数为字符串 {@code "putProviderProperty."+name}
     * 和 {@code "removeProviderProperty."+name}，其中 {@code name} 是
     * 提供者名称，以确定是否可以设置此提供者的属性值和删除此提供者的属性。
     *
     * @throws  SecurityException
     *          如果存在安全管理器且其 {@link
     *          java.lang.SecurityManager#checkSecurityAccess} 方法
     *          拒绝设置属性值和删除属性的访问。
     *
     * @since 1.8
     */
    @Override
    public synchronized Object computeIfAbsent(Object key, Function<? super Object, ? extends Object> mappingFunction) {
        check("putProviderProperty." + name);
        check("removeProviderProperty" + name);

        if (debug != null) {
            debug.println("ComputeIfAbsent " + name + " provider property " +
                    key);
        }
        return implComputeIfAbsent(key, mappingFunction);
    }

    /**
     * 如果指定键的值存在且非 {@code null}，则尝试根据键及其当前映射值计算新的映射。
     *
     * <p>如果启用了安全管理器，其 {@code checkSecurityAccess}
     * 方法将被调用，参数为字符串 {@code "putProviderProperty."+name}
     * 和 {@code "removeProviderProperty."+name}，其中 {@code name} 是
     * 提供者名称，以确定是否可以设置此提供者的属性值和删除此提供者的属性。
     *
     * @throws  SecurityException
     *          如果存在安全管理器且其 {@link
     *          java.lang.SecurityManager#checkSecurityAccess} 方法
     *          拒绝设置属性值或删除属性的访问。
     *
     * @since 1.8
     */
    @Override
    public synchronized Object computeIfPresent(Object key, BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction) {
        check("putProviderProperty." + name);
        check("removeProviderProperty" + name);

        if (debug != null) {
            debug.println("ComputeIfPresent " + name + " provider property " +
                    key);
        }
        return implComputeIfPresent(key, remappingFunction);
    }

    /**
     * 如果指定键尚未与值关联或与 {@code null} 关联，则将其与给定值关联。否则，用给定的重新映射函数的结果替换该值，如果结果为 {@code null} 则删除。此方法在合并键的多个映射值时可能有用。
     *
     * <p>如果启用了安全管理器，其 {@code checkSecurityAccess}
     * 方法将被调用，参数为字符串 {@code "putProviderProperty."+name}
     * 和 {@code "removeProviderProperty."+name}，其中 {@code name} 是
     * 提供者名称，以确定是否可以设置此提供者的属性值和删除此提供者的属性。
     *
     * @throws  SecurityException
     *          如果存在安全管理器且其 {@link
     *          java.lang.SecurityManager#checkSecurityAccess} 方法
     *          拒绝设置属性值或删除属性的访问。
     *
     * @since 1.8
     */
    @Override
    public synchronized Object merge(Object key, Object value,  BiFunction<? super Object, ? super Object, ? extends Object>  remappingFunction) {
        check("putProviderProperty." + name);
        check("removeProviderProperty" + name);


                    if (debug != null) {
            debug.println("Merge " + name + " provider property " + key);
        }
        return implMerge(key, value, remappingFunction);
    }

    // 让 Javadoc 显示来自超类的文档
    @Override
    public Object get(Object key) {
        checkInitialized();
        return super.get(key);
    }
    /**
     * @since 1.8
     */
    @Override
    public synchronized Object getOrDefault(Object key, Object defaultValue) {
        checkInitialized();
        return super.getOrDefault(key, defaultValue);
    }

    /**
     * @since 1.8
     */
    @Override
    public synchronized void forEach(BiConsumer<? super Object, ? super Object> action) {
        checkInitialized();
        super.forEach(action);
    }

    // 让 Javadoc 显示来自超类的文档
    @Override
    public Enumeration<Object> keys() {
        checkInitialized();
        return super.keys();
    }

    // 让 Javadoc 显示来自超类的文档
    @Override
    public Enumeration<Object> elements() {
        checkInitialized();
        return super.elements();
    }

    // 让 Javadoc 显示来自超类的文档
    public String getProperty(String key) {
        checkInitialized();
        return super.getProperty(key);
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException();
        }
    }

    private void check(String directive) {
        checkInitialized();
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSecurityAccess(directive);
        }
    }

    // 自上次调用任何服务方法后，旧属性是否已更改？
    private transient boolean legacyChanged;
    // 自上次调用 getServices() 后，serviceMap 是否已更改
    private volatile transient boolean servicesChanged;

    // 用于跟踪旧注册的 Map<String,String>
    private transient Map<String,String> legacyStrings;

    // 用于通过 putService() 添加的服务的 Map<ServiceKey,Service>
    // 按需初始化
    private transient Map<ServiceKey,Service> serviceMap;

    // 为了向后兼容，当使用此提供者时，需要为 SecureRandom (RNG) 算法保留
    // "new SecureRandom()" 调用的注册顺序
    private transient Set<String> prngAlgos;

    // 用于通过旧方法添加的服务的 Map<ServiceKey,Service>
    // 按需初始化
    private transient Map<ServiceKey,Service> legacyMap;

    // 所有服务的不可修改集合。按需初始化。
    private transient Set<Service> serviceSet;

    // 为此提供者注册 id 属性
    // 这是为了确保 equals() 和 hashCode() 不会错误地报告不同的提供者对象为相同
    private void putId() {
        // 注意：name 和 info 可能为 null
        super.put("Provider.id name", String.valueOf(name));
        super.put("Provider.id version", String.valueOf(version));
        super.put("Provider.id info", String.valueOf(info));
        super.put("Provider.id className", this.getClass().getName());
    }

    private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
        Map<Object,Object> copy = new HashMap<>();
        for (Map.Entry<Object,Object> entry : super.entrySet()) {
            copy.put(entry.getKey(), entry.getValue());
        }
        defaults = null;
        in.defaultReadObject();
        this.serviceMap = new ConcurrentHashMap<>();
        implClear();
        initialized = true;
        putAll(copy);
    }

    // 检查是否需要使用指定的 key 更新 'legacyString'
    private boolean checkLegacy(Object key) {
        String keyString = (String)key;
        if (keyString.startsWith("Provider.")) {
            return false;
        }

        legacyChanged = true;
        if (legacyStrings == null) {
            legacyStrings = new LinkedHashMap<>();
        }
        return true;
    }

    /**
     * 将指定 Map 中的所有映射复制到此提供者。
     * 内部方法，应在执行安全检查后调用。
     */
    private void implPutAll(Map<?,?> t) {
        for (Map.Entry<?,?> e : t.entrySet()) {
            implPut(e.getKey(), e.getValue());
        }
    }

    private Object implRemove(Object key) {
        if (key instanceof String) {
            if (!checkLegacy(key)) {
                return null;
            }
            legacyStrings.remove((String)key);
        }
        return super.remove(key);
    }

    private boolean implRemove(Object key, Object value) {
        if (key instanceof String && value instanceof String) {
            if (!checkLegacy(key)) {
                return false;
            }
            legacyStrings.remove((String)key, (String)value);
        }
        return super.remove(key, value);
    }

    private boolean implReplace(Object key, Object oldValue, Object newValue) {
        if ((key instanceof String) && (oldValue instanceof String) &&
                (newValue instanceof String)) {
            if (!checkLegacy(key)) {
                return false;
            }
            legacyStrings.replace((String)key, (String)oldValue,
                    (String)newValue);
        }
        return super.replace(key, oldValue, newValue);
    }

    private Object implReplace(Object key, Object value) {
        if ((key instanceof String) && (value instanceof String)) {
            if (!checkLegacy(key)) {
                return null;
            }
            legacyStrings.replace((String)key, (String)value);
        }
        return super.replace(key, value);
    }

    private void implReplaceAll(BiFunction<? super Object, ? super Object,
            ? extends Object> function) {
        legacyChanged = true;
        if (legacyStrings == null) {
            legacyStrings = new LinkedHashMap<>();
        } else {
            legacyStrings.replaceAll((BiFunction<? super String, ? super String,
                    ? extends String>) function);
        }
        super.replaceAll(function);
    }


    private Object implMerge(Object key, Object value,
            BiFunction<? super Object, ? super Object, ? extends Object>
            remappingFunction) {
        if ((key instanceof String) && (value instanceof String)) {
            if (!checkLegacy(key)) {
                return null;
            }
            legacyStrings.merge((String)key, (String)value,
                    (BiFunction<? super String, ? super String,
                    ? extends String>) remappingFunction);
        }
        return super.merge(key, value, remappingFunction);
    }

    private Object implCompute(Object key, BiFunction<? super Object,
            ? super Object, ? extends Object> remappingFunction) {
        if (key instanceof String) {
            if (!checkLegacy(key)) {
                return null;
            }
            legacyStrings.compute((String) key,
                    (BiFunction<? super String,? super String,
                    ? extends String>) remappingFunction);
        }
        return super.compute(key, remappingFunction);
    }

    private Object implComputeIfAbsent(Object key, Function<? super Object, ? extends Object> mappingFunction) {
        if (key instanceof String) {
            if (!checkLegacy(key)) {
                return null;
            }
            legacyStrings.computeIfAbsent((String) key,
                    (Function<? super String, ? extends String>)
                    mappingFunction);
        }
        return super.computeIfAbsent(key, mappingFunction);
    }

    private Object implComputeIfPresent(Object key, BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction) {
        if (key instanceof String) {
            if (!checkLegacy(key)) {
                return null;
            }
            legacyStrings.computeIfPresent((String) key,
                    (BiFunction<? super String, ? super String,
                    ? extends String>) remappingFunction);
        }
        return super.computeIfPresent(key, remappingFunction);
    }

    private Object implPut(Object key, Object value) {
        if ((key instanceof String) && (value instanceof String)) {
            if (!checkLegacy(key)) {
                return null;
            }
            legacyStrings.put((String)key, (String)value);
        }
        return super.put(key, value);
    }

    private Object implPutIfAbsent(Object key, Object value) {
        if ((key instanceof String) && (value instanceof String)) {
            if (!checkLegacy(key)) {
                return null;
            }
            legacyStrings.putIfAbsent((String)key, (String)value);
        }
        return super.putIfAbsent(key, value);
    }

    private void implClear() {
        if (legacyStrings != null) {
            legacyStrings.clear();
        }
        if (legacyMap != null) {
            legacyMap.clear();
        }
        serviceMap.clear();
        legacyChanged = false;
        servicesChanged = false;
        serviceSet = null;
        prngAlgos = null;
        super.clear();
        putId();
    }

    // 用作 serviceMap 和 legacyMap HashMaps 中的键
    private static class ServiceKey {
        private final String type;
        private final String algorithm;
        private final String originalAlgorithm;
        private ServiceKey(String type, String algorithm, boolean intern) {
            this.type = type;
            this.originalAlgorithm = algorithm;
            algorithm = algorithm.toUpperCase(ENGLISH);
            this.algorithm = intern ? algorithm.intern() : algorithm;
        }
        public int hashCode() {
            return Objects.hash(type, algorithm);
        }
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ServiceKey)) {
                return false;
            }
            ServiceKey other = (ServiceKey)obj;
            return this.type.equals(other.type)
                && this.algorithm.equals(other.algorithm);
        }
        boolean matches(String type, String algorithm) {
            return (this.type == type) && (this.originalAlgorithm == algorithm);
        }
        public String toString() {
            return type + ":" + algorithm;
        }
    }

    /**
     * 确保所有旧版字符串属性完全解析为服务对象。
     */
    private void ensureLegacyParsed() {
        if (legacyChanged == false || (legacyStrings == null)) {
            return;
        }
        serviceSet = null;
        if (legacyMap == null) {
            legacyMap = new ConcurrentHashMap<>();
        } else {
            legacyMap.clear();
        }
        for (Map.Entry<String,String> entry : legacyStrings.entrySet()) {
            parseLegacyPut(entry.getKey(), entry.getValue());
        }
        removeInvalidServices(legacyMap, serviceMap);
        legacyChanged = false;
    }

    /**
     * 从 Map 中移除所有无效的服务。无效的服务只能在旧版属性不一致或不完整时发生。
     */
    private static void removeInvalidServices(Map<ServiceKey,Service> map,
            Map<ServiceKey,Service> fallbackMap) {
        for (Iterator<Map.Entry<ServiceKey, Service>> t =
                map.entrySet().iterator(); t.hasNext(); ) {
            Map.Entry<ServiceKey, Service> e = t.next();
            Service s = e.getValue();
            if (!s.isValid()) {
                ServiceKey k = e.getKey();

                // 特殊处理无效的别名映射，以解决调用者通过旧版 put() 添加别名但提供者注册已转移到基于服务的情况
                if (!k.algorithm.equals(s.algorithm) && s.className == null) {
                    // 如果 fallbackMap 中有可用的值，则用它替换旧版别名映射下的值
                    ServiceKey stdKey = new ServiceKey(s.type, s.algorithm,
                            true);
                    Service s2 = fallbackMap.get(stdKey);
                    if (s2 != null) {
                        e.setValue(s2);
                        continue;
                    }
                }
                if (debug != null) {
                    debug.println("Remove invalid legacy mapping under " + k);
                }
                t.remove();
            }
        }
    }


                private static String[] getTypeAndAlgorithm(String key) {
        int i = key.indexOf('.');
        if (i < 1) {
            if (debug != null) {
                debug.println("Ignoring invalid entry in provider: "
                        + key);
            }
            return null;
        }
        String type = key.substring(0, i);
        String alg = key.substring(i + 1);
        return new String[] {type, alg};
    }

    private final static String ALIAS_PREFIX = "Alg.Alias.";
    private final static String ALIAS_PREFIX_LOWER = "alg.alias.";
    private final static int ALIAS_LENGTH = ALIAS_PREFIX.length();

    private void parseLegacyPut(String name, String value) {
        if (name.toLowerCase(ENGLISH).startsWith(ALIAS_PREFIX_LOWER)) {
            // 例如：put("Alg.Alias.MessageDigest.SHA", "SHA-1");
            // aliasKey ~ MessageDigest.SHA
            String stdAlg = value;
            String aliasKey = name.substring(ALIAS_LENGTH);
            String[] typeAndAlg = getTypeAndAlgorithm(aliasKey);
            if (typeAndAlg == null) {
                return;
            }
            String type = getEngineName(typeAndAlg[0]);
            String aliasAlg = typeAndAlg[1].intern();
            ServiceKey key = new ServiceKey(type, stdAlg, true);
            Service s = legacyMap.get(key);
            if (s == null) {
                s = new Service(this);
                s.type = type;
                s.algorithm = stdAlg;
                legacyMap.put(key, s);
            }
            legacyMap.put(new ServiceKey(type, aliasAlg, true), s);
            s.addAlias(aliasAlg);
        } else {
            String[] typeAndAlg = getTypeAndAlgorithm(name);
            if (typeAndAlg == null) {
                return;
            }
            int i = typeAndAlg[1].indexOf(' ');
            if (i == -1) {
                // 例如：put("MessageDigest.SHA-1", "sun.security.provider.SHA");
                String type = getEngineName(typeAndAlg[0]);
                String stdAlg = typeAndAlg[1].intern();
                String className = value;
                ServiceKey key = new ServiceKey(type, stdAlg, true);
                Service s = legacyMap.get(key);
                if (s == null) {
                    s = new Service(this);
                    s.type = type;
                    s.algorithm = stdAlg;
                    legacyMap.put(key, s);
                }
                s.className = className;

                if (type.equals("SecureRandom")) {
                    updateSecureRandomEntries(true, s.algorithm);
                }
            } else { // 属性
                // 例如：put("MessageDigest.SHA-1 ImplementedIn", "Software");
                String attributeValue = value;
                String type = getEngineName(typeAndAlg[0]);
                String attributeString = typeAndAlg[1];
                String stdAlg = attributeString.substring(0, i).intern();
                String attributeName = attributeString.substring(i + 1);
                // 去除多余的空格
                while (attributeName.startsWith(" ")) {
                    attributeName = attributeName.substring(1);
                }
                attributeName = attributeName.intern();
                ServiceKey key = new ServiceKey(type, stdAlg, true);
                Service s = legacyMap.get(key);
                if (s == null) {
                    s = new Service(this);
                    s.type = type;
                    s.algorithm = stdAlg;
                    legacyMap.put(key, s);
                }
                s.addAttribute(attributeName, attributeValue);
            }
        }
    }

    /**
     * 获取描述此提供者实现的指定类型和算法的服务。如果不存在这样的实现，此方法返回 null。如果有两个匹配的服务，一个通过
     * {@link #putService putService()} 添加到此提供者，另一个通过 {@link #put put()} 添加，则返回通过
     * {@link #putService putService()} 添加的服务。
     *
     * @param type 请求的服务类型（例如，{@code MessageDigest}）
     * @param algorithm 请求的服务的算法名称（或别名），不区分大小写（例如，{@code SHA-1}）
     *
     * @return 描述此提供者匹配服务的服务，如果不存在这样的服务则返回 null
     *
     * @throws NullPointerException 如果 type 或 algorithm 为 null
     *
     * @since 1.5
     */
    public Service getService(String type, String algorithm) {
        checkInitialized();

        // 尽可能避免分配新的 ServiceKey 对象
        ServiceKey key = previousKey;
        if (key.matches(type, algorithm) == false) {
            key = new ServiceKey(type, algorithm, false);
            previousKey = key;
        }
        if (!serviceMap.isEmpty()) {
            Service s = serviceMap.get(key);
            if (s != null) {
                return s;
            }
        }
        synchronized (this) {
            ensureLegacyParsed();
            if (legacyMap != null && !legacyMap.isEmpty()) {
                return legacyMap.get(key);
            }
        }
        return null;
    }

    // 从上一次 getService() 调用中获取的 ServiceKey
    // 如果可能，通过重用它来避免分配新对象
    // 和 toUpperCase() 调用。
    // 例如，当框架遍历提供者列表并使用相同的值查询每个提供者，直到找到匹配的服务时，将发生重用
    private static volatile ServiceKey previousKey =
                                            new ServiceKey("", "", false);

    /**
     * 获取此提供者支持的所有服务的不可修改的 Set。
     *
     * @return 此提供者支持的所有服务的不可修改的 Set
     *
     * @since 1.5
     */
    public synchronized Set<Service> getServices() {
        checkInitialized();
        if (legacyChanged || servicesChanged) {
            serviceSet = null;
        }
        if (serviceSet == null) {
            ensureLegacyParsed();
            Set<Service> set = new LinkedHashSet<>();
            if (!serviceMap.isEmpty()) {
                set.addAll(serviceMap.values());
            }
            if (legacyMap != null && !legacyMap.isEmpty()) {
                set.addAll(legacyMap.values());
            }
            serviceSet = Collections.unmodifiableSet(set);
            servicesChanged = false;
        }
        return serviceSet;
    }


                /**
     * 添加一个服务。如果存在相同类型和相同算法名称的服务，并且它是通过 {@link #putService putService()} 添加的，
     * 则它将被新服务替换。此方法还会将此服务的信息以《Java Cryptography Architecture API Specification &amp; Reference》中描述的格式
     * 放置在提供者的 Hashtable 值中。
     *
     * <p>此外，如果有安全管理器，将调用其 {@code checkSecurityAccess} 方法，使用字符串
     * {@code "putProviderProperty."+name}，其中 {@code name} 是提供者的名称，以检查是否可以设置此提供者的属性值。
     * 如果使用了 {@code checkSecurityAccess} 的默认实现（即，该方法未被重写），则这将导致调用安全管理器的
     * {@code checkPermission} 方法，使用权限 {@code SecurityPermission("putProviderProperty."+name)}。
     *
     * @param s 要添加的服务
     *
     * @throws SecurityException
     *      如果存在安全管理器，并且其 {@link
     *      java.lang.SecurityManager#checkSecurityAccess} 方法拒绝访问以设置属性值。
     * @throws NullPointerException 如果 s 为 null
     *
     * @since 1.5
     */
    protected void putService(Service s) {
        check("putProviderProperty." + name);
        if (debug != null) {
            debug.println(name + ".putService(): " + s);
        }
        if (s == null) {
            throw new NullPointerException();
        }
        if (s.getProvider() != this) {
            throw new IllegalArgumentException
                    ("service.getProvider() must match this Provider object");
        }
        String type = s.getType();
        String algorithm = s.getAlgorithm();
        ServiceKey key = new ServiceKey(type, algorithm, true);
        implRemoveService(serviceMap.get(key));
        serviceMap.put(key, s);
        for (String alias : s.getAliases()) {
            serviceMap.put(new ServiceKey(type, alias, true), s);
        }
        servicesChanged = true;
        synchronized (this) {
            putPropertyStrings(s);
            if (type.equals("SecureRandom")) {
                updateSecureRandomEntries(true, s.algorithm);
            }
        }
    }

    // 跟踪已注册的安全随机算法并按顺序存储它们
    private void updateSecureRandomEntries(boolean doAdd, String s) {
        Objects.requireNonNull(s);
        if (doAdd) {
            if (prngAlgos == null) {
                prngAlgos = new LinkedHashSet<String>();
            }
            prngAlgos.add(s);
        } else {
            prngAlgos.remove(s);
        }

        if (debug != null) {
            debug.println((doAdd? "Add":"Remove") + " SecureRandom algo " + s);
        }
    }

    // 用于 new SecureRandom() 以查找此提供者的默认 SecureRandom 服务
    synchronized Service getDefaultSecureRandomService() {
        checkInitialized();

        if (legacyChanged) {
            prngAlgos = null;
            ensureLegacyParsed();
        }

        if (prngAlgos != null && !prngAlgos.isEmpty()) {
            // 重要：使用 getService(...) 调用返回的 Service 对象
            // 因为提供者可能会重写 putService(...)/getService(...) 并返回自己的 Service 对象
            return getService("SecureRandom", prngAlgos.iterator().next());
        }

        return null;
    }

    /**
     * 将此服务的字符串属性放入此提供者的 Hashtable 中。
     */
    private void putPropertyStrings(Service s) {
        String type = s.getType();
        String algorithm = s.getAlgorithm();
        // 使用 super() 以避免权限检查和其他处理
        super.put(type + "." + algorithm, s.getClassName());
        for (String alias : s.getAliases()) {
            super.put(ALIAS_PREFIX + type + "." + alias, algorithm);
        }
        for (Map.Entry<UString,String> entry : s.attributes.entrySet()) {
            String key = type + "." + algorithm + " " + entry.getKey();
            super.put(key, entry.getValue());
        }
    }

    /**
     * 从此提供者的 Hashtable 中移除此服务的字符串属性。
     */
    private void removePropertyStrings(Service s) {
        String type = s.getType();
        String algorithm = s.getAlgorithm();
        // 使用 super() 以避免权限检查和其他处理
        super.remove(type + "." + algorithm);
        for (String alias : s.getAliases()) {
            super.remove(ALIAS_PREFIX + type + "." + alias);
        }
        for (Map.Entry<UString,String> entry : s.attributes.entrySet()) {
            String key = type + "." + algorithm + " " + entry.getKey();
            super.remove(key);
        }
    }

    /**
     * 移除使用 {@link #putService putService()} 之前添加的服务。指定的服务将从提供者中移除。
     * 它将不再由 {@link #getService getService()} 返回，其信息将从提供者的 Hashtable 中移除。
     *
     * <p>此外，如果有安全管理器，将调用其 {@code checkSecurityAccess} 方法，使用字符串
     * {@code "removeProviderProperty."+name}，其中 {@code name} 是提供者的名称，以检查是否可以移除此提供者的属性。
     * 如果使用了 {@code checkSecurityAccess} 的默认实现（即，该方法未被重写），则这将导致调用安全管理器的
     * {@code checkPermission} 方法，使用权限 {@code SecurityPermission("removeProviderProperty."+name)}。
     *
     * @param s 要移除的服务
     *
     * @throws  SecurityException
     *          如果存在安全管理器，并且其 {@link
     *          java.lang.SecurityManager#checkSecurityAccess} 方法拒绝访问以移除此提供者的属性。
     * @throws NullPointerException 如果 s 为 null
     *
     * @since 1.5
     */
    protected void removeService(Service s) {
        check("removeProviderProperty." + name);
        if (debug != null) {
            debug.println(name + ".removeService(): " + s);
        }
        if (s == null) {
            throw new NullPointerException();
        }
        implRemoveService(s);
    }


                private void implRemoveService(Service s) {
        if ((s == null) || serviceMap.isEmpty()) {
            return;
        }
        String type = s.getType();
        String algorithm = s.getAlgorithm();
        ServiceKey key = new ServiceKey(type, algorithm, false);
        Service oldService = serviceMap.get(key);
        if (s != oldService) {
            return;
        }
        servicesChanged = true;
        serviceMap.remove(key);
        for (String alias : s.getAliases()) {
            serviceMap.remove(new ServiceKey(type, alias, false));
        }
        synchronized (this) {
            removePropertyStrings(s);
            if (type.equals("SecureRandom")) {
                updateSecureRandomEntries(false, s.algorithm);
            }
        }
    }

    // 包装的字符串，以不区分大小写的方式实现 equals 和 hashCode 方法
    private static class UString {
        final String string;
        final String lowerString;

        UString(String s) {
            this.string = s;
            this.lowerString = s.toLowerCase(ENGLISH);
        }

        public int hashCode() {
            return lowerString.hashCode();
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof UString == false) {
                return false;
            }
            UString other = (UString)obj;
            return lowerString.equals(other.lowerString);
        }

        public String toString() {
            return string;
        }
    }

    // 描述引擎类型的属性
    private static class EngineDescription {
        final String name;
        final boolean supportsParameter;
        final String constructorParameterClassName;
        private volatile Class<?> constructorParameterClass;

        EngineDescription(String name, boolean sp, String paramName) {
            this.name = name;
            this.supportsParameter = sp;
            this.constructorParameterClassName = paramName;
        }
        Class<?> getConstructorParameterClass() throws ClassNotFoundException {
            Class<?> clazz = constructorParameterClass;
            if (clazz == null) {
                clazz = Class.forName(constructorParameterClassName);
                constructorParameterClass = clazz;
            }
            return clazz;
        }
    }

    // JDK 中内置的引擎类型知识
    private static final Map<String,EngineDescription> knownEngines;

    private static void addEngine(String name, boolean sp, String paramName) {
        EngineDescription ed = new EngineDescription(name, sp, paramName);
        // 也按规范名称索引，以避免某些查找时调用 toLowerCase()
        knownEngines.put(name.toLowerCase(ENGLISH), ed);
        knownEngines.put(name, ed);
    }

    static {
        knownEngines = new HashMap<String,EngineDescription>();
        // JCA
        addEngine("AlgorithmParameterGenerator",        false, null);
        addEngine("AlgorithmParameters",                false, null);
        addEngine("KeyFactory",                         false, null);
        addEngine("KeyPairGenerator",                   false, null);
        addEngine("KeyStore",                           false, null);
        addEngine("MessageDigest",                      false, null);
        addEngine("SecureRandom",                       false, null);
        addEngine("Signature",                          true,  null);
        addEngine("CertificateFactory",                 false, null);
        addEngine("CertPathBuilder",                    false, null);
        addEngine("CertPathValidator",                  false, null);
        addEngine("CertStore",                          false,
                            "java.security.cert.CertStoreParameters");
        // JCE
        addEngine("Cipher",                             true,  null);
        addEngine("ExemptionMechanism",                 false, null);
        addEngine("Mac",                                true,  null);
        addEngine("KeyAgreement",                       true,  null);
        addEngine("KeyGenerator",                       false, null);
        addEngine("SecretKeyFactory",                   false, null);
        // JSSE
        addEngine("KeyManagerFactory",                  false, null);
        addEngine("SSLContext",                         false, null);
        addEngine("TrustManagerFactory",                false, null);
        // JGSS
        addEngine("GssApiMechanism",                    false, null);
        // SASL
        addEngine("SaslClientFactory",                  false, null);
        addEngine("SaslServerFactory",                  false, null);
        // POLICY
        addEngine("Policy",                             false,
                            "java.security.Policy$Parameters");
        // CONFIGURATION
        addEngine("Configuration",                      false,
                            "javax.security.auth.login.Configuration$Parameters");
        // XML DSig
        addEngine("XMLSignatureFactory",                false, null);
        addEngine("KeyInfoFactory",                     false, null);
        addEngine("TransformService",                   false, null);
        // Smart Card I/O
        addEngine("TerminalFactory",                    false,
                            "java.lang.Object");
    }

    // 获取任意大小写引擎名称的“标准”（混合大小写）引擎名称
    // 如果没有已知的该名称的引擎，则返回 s
    private static String getEngineName(String s) {
        // 首先尝试原始大小写，通常是正确的
        EngineDescription e = knownEngines.get(s);
        if (e == null) {
            e = knownEngines.get(s.toLowerCase(ENGLISH));
        }
        return (e == null) ? s : e.name;
    }

    /**
     * 安全服务的描述。它封装了服务的属性，并包含一个工厂方法来获取该服务的新实例。
     *
     * <p>每个服务都有一个提供该服务的提供者、类型、算法名称以及实现该服务的类的名称。可选地，它还包括此服务的备用算法名称列表（别名）和属性，这些属性是一组 (名称, 值) 字符串对。
     *
     * <p>此类定义了 {@link #supportsParameter supportsParameter()} 和 {@link #newInstance newInstance()}
     * 方法，这些方法在 Java 安全框架搜索合适的服务并实例化它们时使用。这些方法的有效参数取决于服务的类型。对于 Java SE 中定义的服务类型，请参阅
     * <a href="../../../technotes/guides/security/crypto/CryptoSpec.html">
     * Java Cryptography Architecture API Specification &amp; Reference </a>
     * 以获取有效值。请注意，Java SE 之外的组件可以定义额外的服务类型及其行为。
     *
     * <p>此类的实例是不可变的。
     *
     * @since 1.5
     */
    public static class Service {


                    private String type, algorithm, className;
        private final Provider provider;
        private List<String> aliases;
        private Map<UString,String> attributes;

        // 引用缓存的实现类对象
        private volatile Reference<Class<?>> classRef;

        // 标记此服务是否设置了支持的密钥格式或支持的密钥类属性
        // 如果为 null，表示值未初始化
        // 如果为 TRUE，表示至少有一个支持的格式/类非空
        private volatile Boolean hasKeyAttributes;

        // 支持的编码格式
        private String[] supportedFormats;

        // 支持的密钥（超）类名称
        private Class[] supportedClasses;

        // 表示此服务是否已注册到 Provider
        private boolean registered;

        private static final Class<?>[] CLASS0 = new Class<?>[0];

        // 此构造函数和这些方法用于解析
        // 旧版字符串属性。

        private Service(Provider provider) {
            this.provider = provider;
            aliases = Collections.<String>emptyList();
            attributes = Collections.<UString,String>emptyMap();
        }

        private boolean isValid() {
            return (type != null) && (algorithm != null) && (className != null);
        }

        private void addAlias(String alias) {
            if (aliases.isEmpty()) {
                aliases = new ArrayList<String>(2);
            }
            aliases.add(alias);
        }

        void addAttribute(String type, String value) {
            if (attributes.isEmpty()) {
                attributes = new HashMap<UString,String>(8);
            }
            attributes.put(new UString(type), value);
        }

        /**
         * 构造一个新的服务。
         *
         * @param provider 提供此服务的提供者
         * @param type 此服务的类型
         * @param algorithm 算法名称
         * @param className 实现此服务的类的名称
         * @param aliases 别名列表，如果没有别名则为 null
         * @param attributes 属性映射，如果没有属性则为 null
         *
         * @throws NullPointerException 如果 provider, type, algorithm, 或 className 为 null
         */
        public Service(Provider provider, String type, String algorithm,
                String className, List<String> aliases,
                Map<String,String> attributes) {
            if ((provider == null) || (type == null) ||
                    (algorithm == null) || (className == null)) {
                throw new NullPointerException();
            }
            this.provider = provider;
            this.type = getEngineName(type);
            this.algorithm = algorithm;
            this.className = className;
            if (aliases == null) {
                this.aliases = Collections.<String>emptyList();
            } else {
                this.aliases = new ArrayList<String>(aliases);
            }
            if (attributes == null) {
                this.attributes = Collections.<UString,String>emptyMap();
            } else {
                this.attributes = new HashMap<UString,String>();
                for (Map.Entry<String,String> entry : attributes.entrySet()) {
                    this.attributes.put(new UString(entry.getKey()), entry.getValue());
                }
            }
        }

        /**
         * 获取此服务的类型。例如，{@code MessageDigest}。
         *
         * @return 此服务的类型
         */
        public final String getType() {
            return type;
        }

        /**
         * 返回此服务的算法名称。例如，{@code SHA-1}。
         *
         * @return 此服务的算法名称
         */
        public final String getAlgorithm() {
            return algorithm;
        }

        /**
         * 返回此服务的提供者。
         *
         * @return 此服务的提供者
         */
        public final Provider getProvider() {
            return provider;
        }

        /**
         * 返回实现此服务的类的名称。
         *
         * @return 实现此服务的类的名称
         */
        public final String getClassName() {
            return className;
        }

        // 仅内部使用
        private final List<String> getAliases() {
            return aliases;
        }

        /**
         * 返回指定属性的值，如果此 Service 未设置该属性，则返回 null。
         *
         * @param name 请求的属性名称
         *
         * @return 指定属性的值，如果属性不存在则返回 null
         *
         * @throws NullPointerException 如果 name 为 null
         */
        public final String getAttribute(String name) {
            if (name == null) {
                throw new NullPointerException();
            }
            return attributes.get(new UString(name));
        }

        /**
         * 返回此服务描述的实现的新实例。安全提供者框架使用此方法来构造实现。
         * 应用程序通常不需要调用它。
         *
         * <p>默认实现使用反射调用此服务类型的标准构造函数。
         * 安全提供者可以重写此方法以不同的方式实现实例化。
         * 有关各种服务类型的有效构造函数参数值的详细信息，请参阅
         * <a href="../../../technotes/guides/security/crypto/CryptoSpec.html">
         * Java Cryptography Architecture API Specification &amp;
         * Reference</a>。
         *
         * @param constructorParameter 要传递给构造函数的值，如果此服务类型不使用构造函数参数则为 null。
         *
         * @return 此服务的新实现
         *
         * @throws InvalidParameterException 如果构造函数参数值对于此服务类型无效。
         * @throws NoSuchAlgorithmException 如果实例化因任何其他原因失败。
         */
        public Object newInstance(Object constructorParameter)
                throws NoSuchAlgorithmException {
            if (registered == false) {
                if (provider.getService(type, algorithm) != this) {
                    throw new NoSuchAlgorithmException
                        ("Service not registered with Provider "
                        + provider.getName() + ": " + this);
                }
                registered = true;
            }
            try {
                EngineDescription cap = knownEngines.get(type);
                if (cap == null) {
                    // 未知引擎类型，使用通用代码
                    // 这是未来非核心
                    // 可选包的代码路径
                    return newInstanceGeneric(constructorParameter);
                }
                if (cap.constructorParameterClassName == null) {
                    if (constructorParameter != null) {
                        throw new InvalidParameterException
                            ("constructorParameter not used with " + type
                            + " engines");
                    }
                    Class<?> clazz = getImplClass();
                    Class<?>[] empty = {};
                    Constructor<?> con = clazz.getConstructor(empty);
                    return con.newInstance();
                } else {
                    Class<?> paramClass = cap.getConstructorParameterClass();
                    if (constructorParameter != null) {
                        Class<?> argClass = constructorParameter.getClass();
                        if (paramClass.isAssignableFrom(argClass) == false) {
                            throw new InvalidParameterException
                            ("constructorParameter must be instanceof "
                            + cap.constructorParameterClassName.replace('$', '.')
                            + " for engine type " + type);
                        }
                    }
                    Class<?> clazz = getImplClass();
                    Constructor<?> cons = clazz.getConstructor(paramClass);
                    return cons.newInstance(constructorParameter);
                }
            } catch (NoSuchAlgorithmException e) {
                throw e;
            } catch (InvocationTargetException e) {
                throw new NoSuchAlgorithmException
                    ("Error constructing implementation (algorithm: "
                    + algorithm + ", provider: " + provider.getName()
                    + ", class: " + className + ")", e.getCause());
            } catch (Exception e) {
                throw new NoSuchAlgorithmException
                    ("Error constructing implementation (algorithm: "
                    + algorithm + ", provider: " + provider.getName()
                    + ", class: " + className + ")", e);
            }
        }


                    // 返回此服务的实现类对象
        private Class<?> getImplClass() throws NoSuchAlgorithmException {
            try {
                Reference<Class<?>> ref = classRef;
                Class<?> clazz = (ref == null) ? null : ref.get();
                if (clazz == null) {
                    ClassLoader cl = provider.getClass().getClassLoader();
                    if (cl == null) {
                        clazz = Class.forName(className);
                    } else {
                        clazz = cl.loadClass(className);
                    }
                    if (!Modifier.isPublic(clazz.getModifiers())) {
                        throw new NoSuchAlgorithmException
                            ("class configured for " + type + " (provider: " +
                            provider.getName() + ") is not public.");
                    }
                    classRef = new WeakReference<Class<?>>(clazz);
                }
                return clazz;
            } catch (ClassNotFoundException e) {
                throw new NoSuchAlgorithmException
                    ("class configured for " + type + " (provider: " +
                    provider.getName() + ") cannot be found.", e);
            }
        }

        /**
         * 未知引擎类型的通用代码路径。如果 constructorParameter 为 null，则调用无参构造函数，否则使用第一个匹配的构造函数。
         */
        private Object newInstanceGeneric(Object constructorParameter)
                throws Exception {
            Class<?> clazz = getImplClass();
            if (constructorParameter == null) {
                // 如果存在公共无参构造函数，则创建实例
                try {
                    Class<?>[] empty = {};
                    Constructor<?> con = clazz.getConstructor(empty);
                    return con.newInstance();
                } catch (NoSuchMethodException e) {
                    throw new NoSuchAlgorithmException("No public no-arg "
                        + "constructor found in class " + className);
                }
            }
            Class<?> argClass = constructorParameter.getClass();
            Constructor[] cons = clazz.getConstructors();
            // 查找可以接受参数作为参数的第一个公共构造函数
            for (Constructor<?> con : cons) {
                Class<?>[] paramTypes = con.getParameterTypes();
                if (paramTypes.length != 1) {
                    continue;
                }
                if (paramTypes[0].isAssignableFrom(argClass) == false) {
                    continue;
                }
                return con.newInstance(constructorParameter);
            }
            throw new NoSuchAlgorithmException("No public constructor matching "
                + argClass.getName() + " found in class " + className);
        }

        /**
         * 测试此服务是否可以使用指定的参数。如果此服务不能使用参数，则返回 false。如果此服务可以使用参数、快速测试不可行或状态未知，则返回 true。
         *
         * <p>安全提供程序框架使用此方法与某些类型的服务快速排除不匹配的实现以供考虑。
         * 应用程序通常不需要调用它。
         *
         * <p>有关各种类型服务的有效参数值的详细信息，请参阅此类的顶部和
         * <a href="../../../technotes/guides/security/crypto/CryptoSpec.html">
         * Java Cryptography Architecture API Specification &amp;
         * Reference</a>。
         * 安全提供程序可以覆盖它以实现自己的测试。
         *
         * @param parameter 要测试的参数
         *
         * @return 如果此服务不能使用指定的参数，则返回 false；如果它可以使用参数，则返回 true
         *
         * @throws InvalidParameterException 如果参数的值对此类型的服务无效或此方法不能与此类型的服务一起使用
         */
        public boolean supportsParameter(Object parameter) {
            EngineDescription cap = knownEngines.get(type);
            if (cap == null) {
                // 未知引擎类型，默认返回 true
                return true;
            }
            if (cap.supportsParameter == false) {
                throw new InvalidParameterException("supportsParameter() not "
                    + "used with " + type + " engines");
            }
            // 为了兼容性，允许没有属性的密钥为 null
            if ((parameter != null) && (parameter instanceof Key == false)) {
                throw new InvalidParameterException
                    ("Parameter must be instanceof Key for engine " + type);
            }
            if (hasKeyAttributes() == false) {
                return true;
            }
            if (parameter == null) {
                return false;
            }
            Key key = (Key)parameter;
            if (supportsKeyFormat(key)) {
                return true;
            }
            if (supportsKeyClass(key)) {
                return true;
            }
            return false;
        }

        /**
         * 返回此服务是否定义了密钥的 Supported* 属性。如果尚未初始化，则解析属性。
         */
        private boolean hasKeyAttributes() {
            Boolean b = hasKeyAttributes;
            if (b == null) {
                synchronized (this) {
                    String s;
                    s = getAttribute("SupportedKeyFormats");
                    if (s != null) {
                        supportedFormats = s.split("\\|");
                    }
                    s = getAttribute("SupportedKeyClasses");
                    if (s != null) {
                        String[] classNames = s.split("\\|");
                        List<Class<?>> classList =
                            new ArrayList<>(classNames.length);
                        for (String className : classNames) {
                            Class<?> clazz = getKeyClass(className);
                            if (clazz != null) {
                                classList.add(clazz);
                            }
                        }
                        supportedClasses = classList.toArray(CLASS0);
                    }
                    boolean bool = (supportedFormats != null)
                        || (supportedClasses != null);
                    b = Boolean.valueOf(bool);
                    hasKeyAttributes = b;
                }
            }
            return b.booleanValue();
        }


                    // 获取指定名称的键类对象
        private Class<?> getKeyClass(String name) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e) {
                // 忽略
            }
            try {
                ClassLoader cl = provider.getClass().getClassLoader();
                if (cl != null) {
                    return cl.loadClass(name);
                }
            } catch (ClassNotFoundException e) {
                // 忽略
            }
            return null;
        }

        private boolean supportsKeyFormat(Key key) {
            if (supportedFormats == null) {
                return false;
            }
            String format = key.getFormat();
            if (format == null) {
                return false;
            }
            for (String supportedFormat : supportedFormats) {
                if (supportedFormat.equals(format)) {
                    return true;
                }
            }
            return false;
        }

        private boolean supportsKeyClass(Key key) {
            if (supportedClasses == null) {
                return false;
            }
            Class<?> keyClass = key.getClass();
            for (Class<?> clazz : supportedClasses) {
                if (clazz.isAssignableFrom(keyClass)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 返回此服务的字符串表示形式。
         *
         * @return 此服务的字符串表示形式。
         */
        public String toString() {
            String aString = aliases.isEmpty()
                ? "" : "\r\n  aliases: " + aliases.toString();
            String attrs = attributes.isEmpty()
                ? "" : "\r\n  attributes: " + attributes.toString();
            return provider.getName() + ": " + type + "." + algorithm
                + " -> " + className + aString + attrs + "\r\n";
        }

    }

}
