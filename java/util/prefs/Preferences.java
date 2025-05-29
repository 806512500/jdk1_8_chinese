
/*
 * 版权所有 (c) 2000, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.util.prefs;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.ServiceConfigurationError;

// 这些导入仅作为 JavaDoc 错误的解决方法
import java.lang.RuntimePermission;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Float;
import java.lang.Double;

/**
 * 偏好数据的分层集合中的一个节点。此类允许应用程序存储和检索用户和系统的
 * 偏好和配置数据。这些数据被持久地存储在实现依赖的后端存储中。典型的实现包括
 * 平面文件、操作系统特定的注册表、目录服务器和 SQL 数据库。此类的用户无需关注
 * 后端存储的细节。
 *
 * <p>有两个独立的偏好节点树，一个用于用户偏好，一个用于系统偏好。每个用户
 * 有一个独立的用户偏好树，系统中的所有用户共享同一个系统偏好树。"用户"和"系统"
 * 的精确描述将因实现而异。通常存储在用户偏好树中的信息可能包括字体选择、颜色选择
 * 或特定应用程序的首选窗口位置和大小。通常存储在系统偏好树中的信息可能包括应用程序的
 * 安装配置数据。
 *
 * <p>偏好树中的节点命名方式类似于分层文件系统中的目录。每个偏好树中的每个节点
 * 都有一个<i>节点名称</i>（不一定是唯一的），一个唯一的<i>绝对路径名称</i>，以及
 * 每个祖先（包括自身）的<i>相对</i>路径名称。
 *
 * <p>根节点的节点名称为空字符串("")。每个其他节点都有一个任意的节点名称，在创建时
 * 指定。此名称的唯一限制是它不能是空字符串，并且不能包含斜杠字符('/')。
 *
 * <p>根节点的绝对路径名称为<tt>"/"</tt>。根节点的子节点的绝对路径名称为
 * <tt>"/" + </tt><i>&lt;节点名称&gt;</i>。所有其他节点的绝对路径名称为
 * <i>&lt;父节点的绝对路径名称&gt;</i><tt> + "/" + </tt><i>&lt;节点名称&gt;</i>。
 * 请注意，所有绝对路径名称都以斜杠字符开头。
 *
 * <p>节点<i>n</i>相对于其祖先<i>a</i>的路径名称只是必须附加到<i>a</i>的绝对路径名称
 * 以形成<i>n</i>的绝对路径名称的字符串，初始斜杠字符（如果存在）被移除。请注意：
 * <ul>
 * <li>没有相对路径名称以斜杠字符开头。
 * <li>每个节点相对于自身的路径名称为空字符串。
 * <li>每个节点相对于其父节点的路径名称是其节点名称（根节点除外，根节点没有父节点）。
 * <li>每个节点相对于根节点的路径名称是其绝对路径名称，初始斜杠字符被移除。
 * </ul>
 *
 * <p>最后请注意：
 * <ul>
 * <li>没有路径名称包含多个连续的斜杠字符。
 * <li>除了根节点的绝对路径名称外，没有路径名称以斜杠字符结尾。
 * <li>任何符合这两个规则的字符串都是有效的路径名称。
 * </ul>
 *
 * <p>所有修改偏好数据的方法都允许异步操作；它们可能立即返回，更改最终会
 * 以实现依赖的延迟传播到持久的后端存储。可以使用<tt>flush</tt>方法同步强制
 * 更新到后端存储。Java 虚拟机的正常终止<i>不会</i>导致待处理更新的丢失——在终止
 * 时显式调用<tt>flush</tt>以确保待处理更新持久化<i>不是</i>必需的。
 *
 * <p>所有从<tt>Preferences</tt>对象读取偏好的方法都需要调用者提供一个默认值。
 * 如果没有先前设置值<i>或后端存储不可用</i>，则返回默认值。目的是允许应用程序即使
 * 在后端存储不可用的情况下也能运行，尽管功能可能略有下降。几个方法，如<tt>flush</tt>，
 * 在后端存储不可用时无法操作。普通应用程序无需调用这些方法，这些方法可以通过它们
 * 声明抛出{@link BackingStoreException}来识别。
 *
 * <p>此类中的方法可以由单个 JVM 中的多个线程并发调用，无需外部同步，结果将等同于
 * 某些串行执行。如果此类<i>由多个 JVM</i>并发使用，这些 JVM 在相同的后端存储中
 * 存储其偏好数据，数据存储不会被破坏，但对偏好数据的一致性不作任何其他保证。
 *
 * <p>此类包含导出/导入功能，允许偏好“导出”到 XML 文档，以及将表示偏好的 XML 文档
 * “导入”回系统。此功能可用于备份偏好树的全部或部分，并随后从备份中恢复。
 *
 * <p>XML 文档具有以下 DOCTYPE 声明：
 * <pre>{@code
 * <!DOCTYPE preferences SYSTEM "http://java.sun.com/dtd/preferences.dtd">
 * }</pre>
 * 请注意，系统 URI (http://java.sun.com/dtd/preferences.dtd) 在导出或导入偏好时
 * <i>不会</i>被访问；它仅作为识别 DTD 的字符串，该 DTD 为：
 * <pre>{@code
 *    <?xml version="1.0" encoding="UTF-8"?>
 *
 *    <!-- DTD for a Preferences tree. -->
 *
 *    <!-- The preferences element is at the root of an XML document
 *         representing a Preferences tree. -->
 *    <!ELEMENT preferences (root)>
 *
 *    <!-- The preferences element contains an optional version attribute,
 *          which specifies version of DTD. -->
 *    <!ATTLIST preferences EXTERNAL_XML_VERSION CDATA "0.0" >
 *
 *    <!-- The root element has a map representing the root's preferences
 *         (if any), and one node for each child of the root (if any). -->
 *    <!ELEMENT root (map, node*) >
 *
 *    <!-- Additionally, the root contains a type attribute, which
 *         specifies whether it's the system or user root. -->
 *    <!ATTLIST root
 *              type (system|user) #REQUIRED >
 *
 *    <!-- Each node has a map representing its preferences (if any),
 *         and one node for each child (if any). -->
 *    <!ELEMENT node (map, node*) >
 *
 *    <!-- Additionally, each node has a name attribute -->
 *    <!ATTLIST node
 *              name CDATA #REQUIRED >
 *
 *    <!-- A map represents the preferences stored at a node (if any). -->
 *    <!ELEMENT map (entry*) >
 *
 *    <!-- An entry represents a single preference, which is simply
 *          a key-value pair. -->
 *    <!ELEMENT entry EMPTY >
 *    <!ATTLIST entry
 *              key   CDATA #REQUIRED
 *              value CDATA #REQUIRED >
 * }</pre>
 *
 * 每个<tt>Preferences</tt>实现都必须有一个关联的{@link
 * PreferencesFactory}实现。每个 Java(TM) SE 实现都必须提供某种方法来指定
 * 用于生成根偏好节点的<tt>PreferencesFactory</tt>实现。这允许管理员用替代实现
 * 替换默认的偏好实现。
 *
 * <p>实现说明：在 Sun 的 JRE 中，<tt>PreferencesFactory</tt>实现的定位方式如下：
 *
 * <ol>
 *
 * <li><p>如果定义了系统属性
 * <tt>java.util.prefs.PreferencesFactory</tt>，则认为它是实现
 * <tt>PreferencesFactory</tt>接口的类的完全限定名称。加载并实例化该类；如果此过程失败，
 * 则抛出未指定的错误。</p></li>
 *
 * <li><p>如果<tt>PreferencesFactory</tt>实现类文件已安装在
 * {@link java.lang.ClassLoader#getSystemClassLoader 系统类加载器}可见的 jar 文件中，
 * 且该 jar 文件包含资源目录<tt>META-INF/services</tt>中的提供者配置文件
 * <tt>java.util.prefs.PreferencesFactory</tt>，则采用该文件中指定的第一个类名。如果提供
 * 了多个这样的 jar 文件，则使用找到的第一个。加载并实例化该类；如果此过程失败，则抛出
 * 未指定的错误。</p></li>
 *
 * <li><p>最后，如果既没有提供上述系统属性，也没有提供扩展 jar 文件，则加载并实例化
 * 适用于底层平台的系统默认<tt>PreferencesFactory</tt>实现。</p></li>
 *
 * </ol>
 *
 * @author  Josh Bloch
 * @since   1.4
 */
public abstract class Preferences {


                private static final PreferencesFactory factory = factory();

    private static PreferencesFactory factory() {
        // 1. 尝试使用用户指定的系统属性
        String factoryName = AccessController.doPrivileged(
            new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(
                        "java.util.prefs.PreferencesFactory");}});
        if (factoryName != null) {
            // FIXME: 该代码应在 doPrivileged 中运行，并且
            // 不应使用上下文类加载器，以避免依赖调用线程。
            // 检查 AllPermission 似乎也是错误的。
            try {
                return (PreferencesFactory)
                    Class.forName(factoryName, false,
                                  ClassLoader.getSystemClassLoader())
                    .newInstance();
            } catch (Exception ex) {
                try {
                    // 为 javaws、插件等提供解决方法，
                    // 使用非系统类加载器加载工厂类
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        sm.checkPermission(new java.security.AllPermission());
                    }
                    return (PreferencesFactory)
                        Class.forName(factoryName, false,
                                      Thread.currentThread()
                                      .getContextClassLoader())
                        .newInstance();
                } catch (Exception e) {
                    throw new InternalError(
                        "无法实例化 Preferences 工厂 "
                        + factoryName, e);
                }
            }
        }

        return AccessController.doPrivileged(
            new PrivilegedAction<PreferencesFactory>() {
                public PreferencesFactory run() {
                    return factory1();}});
    }

    private static PreferencesFactory factory1() {
        // 2. 尝试使用服务提供者接口
        Iterator<PreferencesFactory> itr = ServiceLoader
            .load(PreferencesFactory.class, ClassLoader.getSystemClassLoader())
            .iterator();

        // 选择第一个提供者实例
        while (itr.hasNext()) {
            try {
                return itr.next();
            } catch (ServiceConfigurationError sce) {
                if (sce.getCause() instanceof SecurityException) {
                    // 忽略安全异常，尝试下一个提供者
                    continue;
                }
                throw sce;
            }
        }

        // 3. 使用特定平台的系统默认值
        String osName = System.getProperty("os.name");
        String platformFactory;
        if (osName.startsWith("Windows")) {
            platformFactory = "java.util.prefs.WindowsPreferencesFactory";
        } else if (osName.contains("OS X")) {
            platformFactory = "java.util.prefs.MacOSXPreferencesFactory";
        } else {
            platformFactory = "java.util.prefs.FileSystemPreferencesFactory";
        }
        try {
            return (PreferencesFactory)
                Class.forName(platformFactory, false,
                              Preferences.class.getClassLoader()).newInstance();
        } catch (Exception e) {
            throw new InternalError(
                "无法实例化平台默认的 Preferences 工厂 "
                + platformFactory, e);
        }
    }

    /**
     * 允许作为键的字符串的最大长度（80个字符）。
     */
    public static final int MAX_KEY_LENGTH = 80;

    /**
     * 允许作为值的字符串的最大长度（8192个字符）。
     */
    public static final int MAX_VALUE_LENGTH = 8*1024;

    /**
     * 允许的节点名称的最大长度（80个字符）。
     */
    public static final int MAX_NAME_LENGTH = 80;

    /**
     * 返回与指定类的包关联的调用用户的首选项节点。
     * 约定如下：节点的绝对路径名是完全限定的包名，前面加上斜杠（<tt>'/'</tt>），并且每个点（<tt>'.'</tt>）被替换为斜杠。例如，与类
     * <tt>com.acme.widget.Foo</tt> 关联的节点的绝对路径名是 <tt>/com/acme/widget</tt>。
     *
     * <p>此约定不适用于无名包，其关联的首选项节点是 <tt>&lt;unnamed&gt;</tt>。此节点不应用于长期使用，但用于程序开发早期阶段的便利，这些程序尚未属于任何包，以及“一次性”程序。<i>不应在此节点存储有价值的数据，因为它被所有使用它的程序共享。</i>
     *
     * <p>希望访问与其包相关的首选项的类 <tt>Foo</tt> 可以如下获取一个首选项节点：<pre>
     *    static Preferences prefs = Preferences.userNodeForPackage(Foo.class);
     * </pre>
     * 这种用法避免了使用字符串描述首选项节点的需要，减少了运行时失败的可能性。（如果类名拼写错误，通常会导致编译时错误。）
     *
     * <p>调用此方法将导致返回的节点及其祖先节点的创建（如果它们尚不存在）。如果返回的节点在调用之前不存在，则此节点和由此次调用创建的任何祖先节点在调用 <tt>flush</tt> 方法之前不会保证成为永久节点（或其祖先或后代节点之一）。
     *
     * @param c 希望获取用户首选项节点的类。
     * @return 与 <tt>c</tt> 所属的包关联的用户首选项节点。
     * @throws NullPointerException 如果 <tt>c</tt> 为 <tt>null</tt>。
     * @throws SecurityException 如果存在安全经理，并且它拒绝 <tt>RuntimePermission("preferences")</tt>。
     * @see    RuntimePermission
     */
    public static Preferences userNodeForPackage(Class<?> c) {
        return userRoot().node(nodeName(c));
    }

                /**
     * 返回与指定类的包（按惯例）关联的系统首选项树中的节点。惯例如下：节点的绝对路径名是完全限定的包名，前面加上斜杠（<tt>'/'</tt>），并且每个点（<tt>'.'</tt>）替换为斜杠。例如，与类
     * <tt>com.acme.widget.Foo</tt>关联的节点的绝对路径名是<tt>/com/acme/widget</tt>。
     *
     * <p>此惯例不适用于未命名的包，其关联的首选项节点是<tt>&lt;unnamed&gt;</tt>。此节点不打算长期使用，但在程序尚未属于任何包的早期开发中，以及用于“一次性”程序时，出于便利考虑。 <i>不应在此节点存储有价值的数据，因为它被所有使用它的程序共享。</i>
     *
     * <p>希望访问与其包相关的首选项的类<tt>Foo</tt>可以如下获取首选项节点：<pre>
     *  static Preferences prefs = Preferences.systemNodeForPackage(Foo.class);
     * </pre>
     * 这种用法避免了使用字符串来描述首选项节点的需要，减少了运行时失败的可能性。（如果类名拼写错误，通常会导致编译时错误。）
     *
     * <p>调用此方法将导致返回的节点及其祖先节点（如果它们尚不存在）被创建。如果返回的节点在调用之前不存在，则此节点和通过此调用创建的任何祖先节点在调用返回的节点（或其祖先或后代之一）的<tt>flush</tt>方法之前，不保证成为永久节点。
     *
     * @param c 欲获取其包的系统首选项节点的类。
     * @return 与<tt>c</tt>所属的包关联的系统首选项节点。
     * @throws NullPointerException 如果<tt>c</tt>为<tt>null</tt>。
     * @throws SecurityException 如果存在安全管理器，并且它拒绝<tt>RuntimePermission("preferences")</tt>。
     * @see    RuntimePermission
     */
    public static Preferences systemNodeForPackage(Class<?> c) {
        return systemRoot().node(nodeName(c));
    }

    /**
     * 返回与指定对象的包对应的节点的绝对路径名。
     *
     * @throws IllegalArgumentException 如果包没有关联的节点首选项。
     */
    private static String nodeName(Class<?> c) {
        if (c.isArray())
            throw new IllegalArgumentException(
                "数组没有关联的首选项节点。");
        String className = c.getName();
        int pkgEndIndex = className.lastIndexOf('.');
        if (pkgEndIndex < 0)
            return "/<unnamed>";
        String packageName = className.substring(0, pkgEndIndex);
        return "/" + packageName.replace('.', '/');
    }

    /**
     * 此权限对象表示获取用户或系统根节点（这反过来允许所有其他操作）所需的权限。
     */
    private static Permission prefsPerm = new RuntimePermission("preferences");

    /**
     * 返回调用用户的根首选项节点。
     *
     * @return 调用用户的根首选项节点。
     * @throws SecurityException 如果存在安全管理器，并且它拒绝<tt>RuntimePermission("preferences")</tt>。
     * @see    RuntimePermission
     */
    public static Preferences userRoot() {
        SecurityManager security = System.getSecurityManager();
        if (security != null)
            security.checkPermission(prefsPerm);

        return factory.userRoot();
    }

    /**
     * 返回系统的根首选项节点。
     *
     * @return 系统的根首选项节点。
     * @throws SecurityException 如果存在安全管理器，并且它拒绝<tt>RuntimePermission("preferences")</tt>。
     * @see    RuntimePermission
     */
    public static Preferences systemRoot() {
        SecurityManager security = System.getSecurityManager();
        if (security != null)
            security.checkPermission(prefsPerm);

        return factory.systemRoot();
    }

    /**
     * 唯一的构造函数。（通常由子类构造函数隐式调用。）
     */
    protected Preferences() {
    }

    /**
     * 在此首选项节点中将指定的值与指定的键关联。
     *
     * @param key 与指定值关联的键。
     * @param value 与指定键关联的值。
     * @throws NullPointerException 如果键或值为<tt>null</tt>。
     * @throws IllegalArgumentException 如果<tt>key.length()</tt>超过<tt>MAX_KEY_LENGTH</tt>或<tt>value.length</tt>超过<tt>MAX_VALUE_LENGTH</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被{@link #removeNode()}方法移除。
     */
    public abstract void put(String key, String value);

    /**
     * 返回与此首选项节点中指定键关联的值。如果键没有关联的值，或后端存储不可访问，则返回指定的默认值。
     *
     * <p>某些实现可能在后端存储中存储默认值。如果指定的键没有关联的值，但存在这样的<i>存储默认值</i>，则优先返回存储的默认值，而不是指定的默认值。
     *
     * @param key 要返回其关联值的键。
     * @param def 如果此首选项节点没有与<tt>key</tt>关联的值，则返回的值。
     * @return 与<tt>key</tt>关联的值，或如果<tt>key</tt>没有关联的值，或后端存储不可访问，则返回<tt>def</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被{@link #removeNode()}方法移除。
     * @throws NullPointerException 如果<tt>key</tt>为<tt>null</tt>。（允许<tt>def</tt>为<tt>null</tt>。）
     */
    public abstract String get(String key, String def);

                /**
     * 从此首选项节点中移除与指定键关联的值（如果存在）。
     *
     * <p>如果此实现支持<i>存储的默认值</i>，并且指定的首选项存在这样的默认值，
     * 则此调用将“暴露”存储的默认值，即，它将在后续调用<tt>get</tt>时返回。
     *
     * @param key 要从首选项节点中移除映射的键。
     * @throws NullPointerException 如果<tt>key</tt>为<tt>null</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被使用{@link #removeNode()}方法移除。
     */
    public abstract void remove(String key);

    /**
     * 移除此首选项节点中的所有首选项（键值关联）。此调用对本节点的任何后代没有影响。
     *
     * <p>如果此实现支持<i>存储的默认值</i>，并且此首选项层次结构中的节点包含任何这样的默认值，
     * 则此调用将“暴露”存储的默认值，即，它们将在后续调用<tt>get</tt>时返回。
     *
     * @throws BackingStoreException 如果由于后端存储失败或无法与其通信而无法完成此操作。
     * @throws IllegalStateException 如果此节点（或其祖先）已被使用{@link #removeNode()}方法移除。
     * @see #removeNode()
     */
    public abstract void clear() throws BackingStoreException;

    /**
     * 将指定int值的字符串表示形式与指定键关联到此首选项节点。关联的字符串是如果将int值传递给
     * {@link Integer#toString(int)}将返回的字符串。此方法旨在与{@link #getInt}一起使用。
     *
     * @param key 要与值的字符串形式关联的键。
     * @param value 要与键关联的值的字符串形式。
     * @throws NullPointerException 如果<tt>key</tt>为<tt>null</tt>。
     * @throws IllegalArgumentException 如果<tt>key.length()</tt>超过<tt>MAX_KEY_LENGTH</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被使用{@link #removeNode()}方法移除。
     * @see #getInt(String,int)
     */
    public abstract void putInt(String key, int value);

    /**
     * 返回与此首选项节点中指定键关联的字符串表示的int值。字符串将如{@link Integer#parseInt(String)}那样转换为整数。
     * 如果没有与键关联的值，后端存储不可访问，或者如果将关联的值传递给<tt>Integer.parseInt(String)</tt>会抛出
     * {@link NumberFormatException}，则返回指定的默认值。此方法旨在与{@link #putInt}一起使用。
     *
     * <p>如果实现支持<i>存储的默认值</i>，并且存在这样的默认值，可访问，并且可以使用<tt>Integer.parseInt</tt>转换为整数，
     * 则优先返回此整数而不是指定的默认值。
     *
     * @param key 要作为整数返回的关联值的键。
     * @param def 如果此首选项节点没有与<tt>key</tt>关联的值，或者关联的值不能解释为整数，或者后端存储不可访问时，要返回的值。
     * @return 与此首选项节点中的<tt>key</tt>关联的字符串表示的int值，或者如果关联的值不存在或不能解释为整数，则返回<tt>def</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被使用{@link #removeNode()}方法移除。
     * @throws NullPointerException 如果<tt>key</tt>为<tt>null</tt>。
     * @see #putInt(String,int)
     * @see #get(String,String)
     */
    public abstract int getInt(String key, int def);

    /**
     * 将指定long值的字符串表示形式与指定键关联到此首选项节点。关联的字符串是如果将long值传递给
     * {@link Long#toString(long)}将返回的字符串。此方法旨在与{@link #getLong}一起使用。
     *
     * @param key 要与值的字符串形式关联的键。
     * @param value 要与键关联的值的字符串形式。
     * @throws NullPointerException 如果<tt>key</tt>为<tt>null</tt>。
     * @throws IllegalArgumentException 如果<tt>key.length()</tt>超过<tt>MAX_KEY_LENGTH</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被使用{@link #removeNode()}方法移除。
     * @see #getLong(String,long)
     */
    public abstract void putLong(String key, long value);

    /**
     * 返回与此首选项节点中指定键关联的字符串表示的long值。字符串将如{@link Long#parseLong(String)}那样转换为长整数。
     * 如果没有与键关联的值，后端存储不可访问，或者如果将关联的值传递给<tt>Long.parseLong(String)</tt>会抛出
     * {@link NumberFormatException}，则返回指定的默认值。此方法旨在与{@link #putLong}一起使用。
     *
     * <p>如果实现支持<i>存储的默认值</i>，并且存在这样的默认值，可访问，并且可以使用<tt>Long.parseLong</tt>转换为长整数，
     * 则优先返回此长整数而不是指定的默认值。
     *
     * @param key 要作为长整数返回的关联值的键。
     * @param def 如果此首选项节点没有与<tt>key</tt>关联的值，或者关联的值不能解释为长整数，或者后端存储不可访问时，要返回的值。
     * @return 与此首选项节点中的<tt>key</tt>关联的字符串表示的long值，或者如果关联的值不存在或不能解释为长整数，则返回<tt>def</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被使用{@link #removeNode()}方法移除。
     * @throws NullPointerException 如果<tt>key</tt>为<tt>null</tt>。
     * @see #putLong(String,long)
     * @see #get(String,String)
     */
    public abstract long getLong(String key, long def);

                /**
     * 将表示指定布尔值的字符串与此首选项节点中的指定键关联。如果值为 true，则关联的字符串为 <tt>"true"</tt>，如果值为 false，则为 <tt>"false"</tt>。此方法旨在与
     * {@link #getBoolean} 一起使用。
     *
     * @param key 要与值的字符串形式关联的键。
     * @param value 要与键关联的值的字符串形式。
     * @throws NullPointerException 如果 <tt>key</tt> 为 <tt>null</tt>。
     * @throws IllegalArgumentException 如果 <tt>key.length()</tt> 超过 <tt>MAX_KEY_LENGTH</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     * @see #getBoolean(String,boolean)
     * @see #get(String,String)
     */
    public abstract void putBoolean(String key, boolean value);

    /**
     * 返回与此首选项节点中的指定键关联的字符串表示的布尔值。有效的字符串是 <tt>"true"</tt>，表示 true，以及 <tt>"false"</tt>，表示 false。忽略大小写，因此例如 <tt>"TRUE"</tt>
     * 和 <tt>"False"</tt> 也是有效的。此方法旨在与 {@link #putBoolean} 一起使用。
     *
     * <p>如果键没有关联的值，后端存储不可访问，或者关联的值不是 <tt>"true"</tt> 或 <tt>"false"</tt>（忽略大小写），则返回指定的默认值。
     *
     * <p>如果实现支持 <i>存储的默认值</i> 并且存在这样的默认值且可访问，则优先使用存储的默认值，除非存储的默认值不是 <tt>"true"</tt> 或 <tt>"false"</tt>（忽略大小写），在这种情况下使用指定的默认值。
     *
     * @param key 要作为布尔值返回的关联值的键。
     * @param def 如果此首选项节点没有与 <tt>key</tt> 关联的值，或者关联的值不能被解释为布尔值，或者后端存储不可访问时，要返回的值。
     * @return 与此首选项节点中的 <tt>key</tt> 关联的字符串表示的布尔值，或者如果关联的值不存在或不能被解释为布尔值，则返回 <tt>def</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     * @throws NullPointerException 如果 <tt>key</tt> 为 <tt>null</tt>。
     * @see #get(String,String)
     * @see #putBoolean(String,boolean)
     */
    public abstract boolean getBoolean(String key, boolean def);

    /**
     * 将表示指定浮点值的字符串与此首选项节点中的指定键关联。关联的字符串是如果将浮点值传递给 {@link Float#toString(float)} 时将返回的字符串。此方法旨在与
     * {@link #getFloat} 一起使用。
     *
     * @param key 要与值的字符串形式关联的键。
     * @param value 要与键关联的值的字符串形式。
     * @throws NullPointerException 如果 <tt>key</tt> 为 <tt>null</tt>。
     * @throws IllegalArgumentException 如果 <tt>key.length()</tt> 超过 <tt>MAX_KEY_LENGTH</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     * @see #getFloat(String,float)
     */
    public abstract void putFloat(String key, float value);

    /**
     * 返回与此首选项节点中的指定键关联的字符串表示的浮点值。字符串将像 {@link Float#parseFloat(String)} 那样转换为浮点数。如果键没有关联的值，后端存储不可访问，或者如果将关联的值传递给
     * <tt>Float.parseFloat(String)</tt> 会抛出 {@link NumberFormatException}，则返回指定的默认值。此方法旨在与 {@link #putFloat} 一起使用。
     *
     * <p>如果实现支持 <i>存储的默认值</i> 并且存在这样的默认值，可访问，并且可以用 <tt>Float.parseFloat</tt> 转换为浮点数，则优先返回此浮点数，而不是指定的默认值。
     *
     * @param key 要作为浮点值返回的关联值的键。
     * @param def 如果此首选项节点没有与 <tt>key</tt> 关联的值，或者关联的值不能被解释为浮点数，或者后端存储不可访问时，要返回的值。
     * @return 与此首选项节点中的 <tt>key</tt> 关联的字符串表示的浮点值，或者如果关联的值不存在或不能被解释为浮点数，则返回 <tt>def</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     * @throws NullPointerException 如果 <tt>key</tt> 为 <tt>null</tt>。
     * @see #putFloat(String,float)
     * @see #get(String,String)
     */
    public abstract float getFloat(String key, float def);

    /**
     * 将表示指定双精度值的字符串与此首选项节点中的指定键关联。关联的字符串是如果将双精度值传递给 {@link Double#toString(double)} 时将返回的字符串。此方法旨在与
     * {@link #getDouble} 一起使用。
     *
     * @param key 要与值的字符串形式关联的键。
     * @param value 要与键关联的值的字符串形式。
     * @throws NullPointerException 如果 <tt>key</tt> 为 <tt>null</tt>。
     * @throws IllegalArgumentException 如果 <tt>key.length()</tt> 超过 <tt>MAX_KEY_LENGTH</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     * @see #getDouble(String,double)
     */
    public abstract void putDouble(String key, double value);

                /**
     * 返回与此首选项节点中指定键关联的字符串表示的 double 值。字符串将按照 {@link Double#parseDouble(String)} 的方式转换为 double。如果键没有关联值，后端存储不可访问，或者如果将关联值传递给 <tt>Double.parseDouble(String)</tt> 会抛出 {@link NumberFormatException}，则返回指定的默认值。此方法旨在与 {@link #putDouble} 结合使用。
     *
     * <p>如果实现支持 <i>存储的默认值</i> 并且存在这样的默认值，且该默认值可访问，并且可以使用 <tt>Double.parseDouble</tt> 转换为 double，则优先返回此 double，而不是指定的默认值。
     *
     * @param key 要作为 double 返回的关联值的键。
     * @param def 如果此首选项节点没有与 <tt>key</tt> 关联的值，或者关联值不能被解释为 double，或者后端存储不可访问时，返回的值。
     * @return 与此首选项节点中 <tt>key</tt> 关联的字符串表示的 double 值，或者如果关联值不存在或不能被解释为 double，则返回 <tt>def</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     * @throws NullPointerException 如果 <tt>key</tt> 为 <tt>null</tt>。
     * @see #putDouble(String,double)
     * @see #get(String,String)
     */
    public abstract double getDouble(String key, double def);

    /**
     * 将表示指定字节数组的字符串与此首选项节点中的指定键关联。关联的字符串是字节数组的 <i>Base64</i> 编码，如 <a
     * href=http://www.ietf.org/rfc/rfc2045.txt>RFC 2045</a> 第 6.8 节所定义，但有一个小的改动：字符串将仅由 <i>Base64 字母表</i> 中的字符组成；它不会包含任何换行符。注意，字节数组的最大长度限制为 <tt>MAX_VALUE_LENGTH</tt> 的四分之三，以确保 Base64 编码后的字符串长度不超过 <tt>MAX_VALUE_LENGTH</tt>。此方法旨在与 {@link #getByteArray} 结合使用。
     *
     * @param key 要与值的字符串形式关联的键。
     * @param value 要与键关联的值的字符串形式。
     * @throws NullPointerException 如果 key 或 value 为 <tt>null</tt>。
     * @throws IllegalArgumentException 如果 key.length() 超过 MAX_KEY_LENGTH 或 value.length 超过 MAX_VALUE_LENGTH*3/4。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     * @see #getByteArray(String,byte[])
     * @see #get(String,String)
     */
    public abstract void putByteArray(String key, byte[] value);

    /**
     * 返回与此首选项节点中指定键关联的字符串表示的字节数组值。有效字符串是 <i>Base64</i> 编码的二进制数据，如 <a
     * href=http://www.ietf.org/rfc/rfc2045.txt>RFC 2045</a> 第 6.8 节所定义，但有一个小的改动：字符串必须仅由 <i>Base64 字母表</i> 中的字符组成；不允许有换行符或额外的字符。此方法旨在与 {@link #putByteArray} 结合使用。
     *
     * <p>如果键没有关联值，后端存储不可访问，或者关联值不是有效的 Base64 编码的字节数组（如上所述），则返回指定的默认值。
     *
     * <p>如果实现支持 <i>存储的默认值</i> 并且存在这样的默认值且可访问，则优先使用存储的默认值，除非存储的默认值不是有效的 Base64 编码的字节数组（如上所述），在这种情况下，使用指定的默认值。
     *
     * @param key 要作为字节数组返回的关联值的键。
     * @param def 如果此首选项节点没有与 <tt>key</tt> 关联的值，或者关联值不能被解释为字节数组，或者后端存储不可访问时，返回的值。
     * @return 与此首选项节点中 <tt>key</tt> 关联的字符串表示的字节数组值，或者如果关联值不存在或不能被解释为字节数组，则返回 <tt>def</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     * @throws NullPointerException 如果 <tt>key</tt> 为 <tt>null</tt>。（允许 <tt>def</tt> 为 <tt>null</tt>。）
     * @see #get(String,String)
     * @see #putByteArray(String,byte[])
     */
    public abstract byte[] getByteArray(String key, byte[] def);

    /**
     * 返回与此首选项节点有关联值的所有键。（如果此节点没有任何首选项，返回的数组大小将为零。）
     *
     * <p>如果实现支持 <i>存储的默认值</i> 并且在此节点上有任何未被显式首选项覆盖的此类默认值，则这些默认值也将包含在返回的数组中，除了任何显式首选项。
     *
     * @return 与此首选项节点有关联值的所有键的数组。
     * @throws BackingStoreException 如果由于后端存储失败或无法与之通信而无法完成此操作。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     */
    public abstract String[] keys() throws BackingStoreException;


                /**
     * 返回此首选项节点的子节点名称，相对于此节点。 （如果此节点没有子节点，则返回的数组大小为零。）
     *
     * @return 此首选项节点的子节点名称。
     * @throws BackingStoreException 如果由于后端存储失败或无法与之通信而无法完成此操作。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     */
    public abstract String[] childrenNames() throws BackingStoreException;

    /**
     * 返回此首选项节点的父节点，如果这是根节点，则返回 <tt>null</tt>。
     *
     * @return 此首选项节点的父节点。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     */
    public abstract Preferences parent();

    /**
     * 返回与此节点在同一树中的指定名称的首选项节点，如果它们尚不存在，则创建它及其所有祖先。
     * 接受相对路径名或绝对路径名。 相对路径名（不以斜杠字符 <tt>('/')</tt> 开头）将相对于此首选项节点解释。
     *
     * <p>如果返回的节点在调用之前不存在，则此节点和通过此调用创建的任何祖先节点都不会保证在调用 <tt>flush</tt> 方法之前成为永久节点
     * （返回的节点或其祖先或后代之一）。
     *
     * @param pathName 要返回的首选项节点的路径名。
     * @return 指定的首选项节点。
     * @throws IllegalArgumentException 如果路径名无效（即，它包含多个连续的斜杠字符，或以斜杠字符结尾且长度超过一个字符）。
     * @throws NullPointerException 如果路径名是 <tt>null</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     * @see #flush()
     */
    public abstract Preferences node(String pathName);

    /**
     * 如果指定名称的首选项节点存在于与此节点相同的树中，则返回 <tt>true</tt>。 相对路径名（不以斜杠字符 <tt>('/')</tt> 开头）将相对于此首选项节点解释。
     *
     * <p>如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除，则调用此方法是合法的，但仅限于路径名为 <tt>""</tt>；调用将返回 <tt>false</tt>。
     * 因此，可以使用 <tt>p.nodeExists("")</tt> 语法来测试 <tt>p</tt> 是否已被移除。
     *
     * @param pathName 要检查其存在的节点的路径名。
     * @return 如果指定的节点存在，则返回 <tt>true</tt>。
     * @throws BackingStoreException 如果由于后端存储失败或无法与之通信而无法完成此操作。
     * @throws IllegalArgumentException 如果路径名无效（即，它包含多个连续的斜杠字符，或以斜杠字符结尾且长度超过一个字符）。
     * @throws NullPointerException 如果路径名是 <tt>null</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除且 <tt>pathName</tt> 不是空字符串 (<tt>""</tt>)。
     */
    public abstract boolean nodeExists(String pathName)
        throws BackingStoreException;

    /**
     * 移除此首选项节点及其所有后代，使移除节点中包含的所有首选项失效。 一旦节点被移除，尝试在此 <tt>Preferences</tt> 实例上除 {@link #name()}、
     * {@link #absolutePath()}、{@link #isUserNode()}、{@link #flush()} 或 {@link #node(String) nodeExists("")} 之外的任何方法都将因 <tt>IllegalStateException</tt> 而失败。
     * （在节点被移除后，仍然可以在节点上调用 {@link Object} 上定义的方法；它们不会抛出 <tt>IllegalStateException</tt>。）
     *
     * <p>移除操作在调用此节点（或其祖先）的 <tt>flush</tt> 方法之前不会保证持久化。
     *
     * <p>如果此实现支持 <i>存储的默认值</i>，移除节点将暴露此节点及其以下的任何存储的默认值。 因此，后续调用 <tt>nodeExists</tt> 可能会返回 <tt>true</tt>，
     * 后续调用 <tt>node</tt> 可能会返回一个（不同的）<tt>Preferences</tt> 实例，表示非空的首选项和/或子节点集合。
     *
     * @throws BackingStoreException 如果由于后端存储失败或无法与之通信而无法完成此操作。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     * @throws UnsupportedOperationException 如果此方法在根节点上调用。
     * @see #flush()
     */
    public abstract void removeNode() throws BackingStoreException;

    /**
     * 返回此首选项节点的名称，相对于其父节点。
     *
     * @return 此首选项节点的名称，相对于其父节点。
     */
    public abstract String name();

    /**
     * 返回此首选项节点的绝对路径名。
     *
     * @return 此首选项节点的绝对路径名。
     */
    public abstract String absolutePath();

    /**
     * 如果此首选项节点在用户首选项树中，则返回 <tt>true</tt>，如果在系统首选项树中，则返回 <tt>false</tt>。
     *
     * @return 如果此首选项节点在用户首选项树中，则返回 <tt>true</tt>，如果在系统首选项树中，则返回 <tt>false</tt>。
     */
    public abstract boolean isUserNode();

                /**
     * 返回此首选项节点的字符串表示形式，
     * 好像由表达式计算得出：<tt>(this.isUserNode() ? "User" :
     * "System") + " Preference Node: " + this.absolutePath()</tt>。
     */
    public abstract String toString();

    /**
     * 强制将此首选项节点及其后代的内容中的任何更改刷新到持久存储中。 一旦此方法成功返回，
     * 就可以安全地假设在此节点为根的子树中在此方法调用之前所做的所有更改都已成为永久性的。
     *
     * <p>实现可以随时将更改刷新到持久存储中。 它们不需要等待此方法被调用。
     *
     * <p>当刷新发生在新创建的节点上时，该节点将被持久化，
     * 以及尚未被持久化的任何祖先（和后代）。 但是请注意，祖先中的任何首选项值更改
     * <i>不</i>保证被持久化。
     *
     * <p>如果在此方法上调用已使用 {@link #removeNode()} 方法移除的节点，
     * 则调用 flushSpi()，但不会调用其他节点。
     *
     * @throws BackingStoreException 如果由于持久存储中的故障或无法与其通信而导致此操作无法完成。
     * @see    #sync()
     */
    public abstract void flush() throws BackingStoreException;

    /**
     * 确保从此首选项节点及其后代的未来读取反映在持久存储中（从任何虚拟机）提交的任何更改
     * 在 <tt>sync</tt> 调用之前。 作为副作用，强制将此首选项节点及其后代的内容中的任何更改
     * 刷新到持久存储中，就像在该节点上调用了 <tt>flush</tt> 方法一样。
     *
     * @throws BackingStoreException 如果由于持久存储中的故障或无法与其通信而导致此操作无法完成。
     * @throws IllegalStateException 如果此节点（或其祖先）已使用 {@link #removeNode()} 方法移除。
     * @see    #flush()
     */
    public abstract void sync() throws BackingStoreException;

    /**
     * 注册指定的侦听器以接收此首选项节点的 <i>首选项更改事件</i>。 当首选项添加到此节点、
     * 从此节点移除或与此首选项关联的值更改时，将生成首选项更改事件。
     * （首选项更改事件<i>不</i>由 {@link #removeNode()} 方法生成，该方法生成 <i>节点更改事件</i>。
     * 首选项更改事件<i>是</i>由 <tt>clear</tt> 方法生成的。）
     *
     * <p>仅保证在与注册侦听器相同的 JVM 中进行的更改会生成事件，尽管某些实现可能会为
     * 该 JVM 之外的更改生成事件。 事件可能在更改被持久化之前生成。 当此节点的后代中的首选项被修改时，
     * 不会生成事件；希望接收此类事件的调用者必须注册每个后代。
     *
     * @param pcl 要添加的首选项更改侦听器。
     * @throws NullPointerException 如果 <tt>pcl</tt> 为 null。
     * @throws IllegalStateException 如果此节点（或其祖先）已使用 {@link #removeNode()} 方法移除。
     * @see #removePreferenceChangeListener(PreferenceChangeListener)
     * @see #addNodeChangeListener(NodeChangeListener)
     */
    public abstract void addPreferenceChangeListener(
        PreferenceChangeListener pcl);

    /**
     * 移除指定的首选项更改侦听器，使其不再接收首选项更改事件。
     *
     * @param pcl 要移除的首选项更改侦听器。
     * @throws IllegalArgumentException 如果 <tt>pcl</tt> 不是此节点上注册的首选项更改侦听器。
     * @throws IllegalStateException 如果此节点（或其祖先）已使用 {@link #removeNode()} 方法移除。
     * @see #addPreferenceChangeListener(PreferenceChangeListener)
     */
    public abstract void removePreferenceChangeListener(
        PreferenceChangeListener pcl);

    /**
     * 注册指定的侦听器以接收此节点的 <i>节点更改事件</i>。 当子节点添加到或从该节点移除时，
     * 将生成节点更改事件。（单个 {@link #removeNode()} 调用会导致多个 <i>节点更改事件</i>，
     * 每个被移除节点的子树中的每个节点一个。）
     *
     * <p>仅保证在与注册侦听器相同的 JVM 中进行的更改会生成事件，尽管某些实现可能会为
     * 该 JVM 之外的更改生成事件。 事件可能在更改成为永久之前生成。 当此节点的间接后代
     * 被添加或移除时，不会生成事件；希望接收此类事件的调用者必须注册每个后代。
     *
     * <p>关于节点创建的保证很少。 由于节点在访问时隐式创建，因此实现可能无法确定
     * 子节点在访问之前是否存在于持久存储中（例如，因为持久存储无法访问或缓存的信息已过期）。
     * 在这些情况下，实现既不要求生成节点更改事件，也不禁止这样做。
     *
     * @param ncl 要添加的 <tt>NodeChangeListener</tt>。
     * @throws NullPointerException 如果 <tt>ncl</tt> 为 null。
     * @throws IllegalStateException 如果此节点（或其祖先）已使用 {@link #removeNode()} 方法移除。
     * @see #removeNodeChangeListener(NodeChangeListener)
     * @see #addPreferenceChangeListener(PreferenceChangeListener)
     */
    public abstract void addNodeChangeListener(NodeChangeListener ncl);

                /**
     * 移除指定的 <tt>NodeChangeListener</tt>，使其不再接收更改事件。
     *
     * @param ncl 要移除的 <tt>NodeChangeListener</tt>。
     * @throws IllegalArgumentException 如果 <tt>ncl</tt> 不是此节点上注册的
     *         <tt>NodeChangeListener</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被
     *         {@link #removeNode()} 方法移除。
     * @see #addNodeChangeListener(NodeChangeListener)
     */
    public abstract void removeNodeChangeListener(NodeChangeListener ncl);

    /**
     * 在指定的输出流上生成一个 XML 文档，表示此节点中包含的所有偏好设置（但不包括其后代）。
     * 该 XML 文档实际上是该节点的离线备份。
     *
     * <p>XML 文档将具有以下 DOCTYPE 声明：
     * <pre>{@code
     * <!DOCTYPE preferences SYSTEM "http://java.sun.com/dtd/preferences.dtd">
     * }</pre>
     * 将使用 UTF-8 字符编码。
     *
     * <p>此方法是此类中并发执行多个方法的一般规则的例外，该规则的结果等同于某些串行执行的结果。
     * 如果在此方法调用的同时修改了此节点的偏好设置，导出的偏好设置将包含该节点中偏好设置的“模糊快照”；
     * 一些并发修改可能反映在导出的数据中，而其他修改可能不会。
     *
     * @param os 用于生成 XML 文档的输出流。
     * @throws IOException 如果向指定的输出流写入时导致 <tt>IOException</tt>。
     * @throws BackingStoreException 如果无法从后端存储读取偏好设置数据。
     * @see    #importPreferences(InputStream)
     * @throws IllegalStateException 如果此节点（或其祖先）已被
     *         {@link #removeNode()} 方法移除。
     */
    public abstract void exportNode(OutputStream os)
        throws IOException, BackingStoreException;

    /**
     * 生成一个 XML 文档，表示此节点及其所有后代中包含的所有偏好设置。
     * 该 XML 文档实际上是根节点为该节点的子树的离线备份。
     *
     * <p>XML 文档将具有以下 DOCTYPE 声明：
     * <pre>{@code
     * <!DOCTYPE preferences SYSTEM "http://java.sun.com/dtd/preferences.dtd">
     * }</pre>
     * 将使用 UTF-8 字符编码。
     *
     * <p>此方法是此类中并发执行多个方法的一般规则的例外，该规则的结果等同于某些串行执行的结果。
     * 如果在此方法调用的同时修改了以该节点为根的子树中的偏好设置或节点，导出的偏好设置将包含该子树的“模糊快照”；
     * 一些并发修改可能反映在导出的数据中，而其他修改可能不会。
     *
     * @param os 用于生成 XML 文档的输出流。
     * @throws IOException 如果向指定的输出流写入时导致 <tt>IOException</tt>。
     * @throws BackingStoreException 如果无法从后端存储读取偏好设置数据。
     * @throws IllegalStateException 如果此节点（或其祖先）已被
     *         {@link #removeNode()} 方法移除。
     * @see    #importPreferences(InputStream)
     * @see    #exportNode(OutputStream)
     */
    public abstract void exportSubtree(OutputStream os)
        throws IOException, BackingStoreException;

    /**
     * 从指定的输入流中导入 XML 文档表示的所有偏好设置。该文档可以表示用户偏好设置或系统偏好设置。
     * 如果它表示用户偏好设置，偏好设置将导入到调用用户的偏好设置树中（即使它们最初来自不同用户的偏好设置树）。
     * 如果文档中描述的任何偏好设置位于不存在的偏好设置节点中，将创建这些节点。
     *
     * <p>XML 文档必须具有以下 DOCTYPE 声明：
     * <pre>{@code
     * <!DOCTYPE preferences SYSTEM "http://java.sun.com/dtd/preferences.dtd">
     * }</pre>
     * （此方法设计用于与 {@link #exportNode(OutputStream)} 和
     * {@link #exportSubtree(OutputStream)} 配合使用。
     *
     * <p>此方法是此类中并发执行多个方法的一般规则的例外，该规则的结果等同于某些串行执行的结果。
     * 该方法的行为类似于基于此类中的其他公共方法实现的，特别是 {@link #node(String)} 和 {@link #put(String, String)}。
     *
     * @param is 用于读取 XML 文档的输入流。
     * @throws IOException 如果从指定的输入流读取时导致 <tt>IOException</tt>。
     * @throws InvalidPreferencesFormatException 输入流中的数据不构成具有强制文档类型的有效的 XML 文档。
     * @throws SecurityException 如果存在安全经理，并且它拒绝 <tt>RuntimePermission("preferences")</tt>。
     * @see    RuntimePermission
     */
    public static void importPreferences(InputStream is)
        throws IOException, InvalidPreferencesFormatException
    {
        XmlSupport.importPreferences(is);
    }
}
