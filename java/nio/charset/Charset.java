
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

package java.nio.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.spi.CharsetProvider;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.ServiceLoader;
import java.util.ServiceConfigurationError;
import java.util.SortedMap;
import java.util.TreeMap;
import sun.misc.ASCIICaseInsensitiveComparator;
import sun.nio.cs.StandardCharsets;
import sun.nio.cs.ThreadLocalCoders;
import sun.security.action.GetPropertyAction;


/**
 * 命名的十六位 Unicode <a
 * href="../../lang/Character.html#unicode">代码单元</a>序列和字节序列之间的映射。此类定义了用于创建解码器和编码器的方法，以及用于检索与字符集相关的各种名称的方法。此类的实例是不可变的。
 *
 * <p> 本类还定义了静态方法，用于测试特定字符集是否受支持，通过名称查找字符集实例，以及构建一个包含当前 Java 虚拟机中所有受支持字符集的地图。可以通过 {@link
 * java.nio.charset.spi.CharsetProvider} 类中定义的服务提供者接口添加对新字符集的支持。
 *
 * <p> 本类中定义的所有方法都适用于多个并发线程。
 *
 *
 * <a name="names"></a><a name="charenc"></a>
 * <h2>字符集名称</h2>
 *
 * <p> 字符集由以下字符组成的字符串命名：
 *
 * <ul>
 *
 *   <li> 大写字母 <tt>'A'</tt> 到 <tt>'Z'</tt>
 *        (<tt>'&#92;u0041'</tt>&nbsp;到&nbsp;<tt>'&#92;u005a'</tt>),
 *
 *   <li> 小写字母 <tt>'a'</tt> 到 <tt>'z'</tt>
 *        (<tt>'&#92;u0061'</tt>&nbsp;到&nbsp;<tt>'&#92;u007a'</tt>),
 *
 *   <li> 数字 <tt>'0'</tt> 到 <tt>'9'</tt>
 *        (<tt>'&#92;u0030'</tt>&nbsp;到&nbsp;<tt>'&#92;u0039'</tt>),
 *
 *   <li> 连字符 <tt>'-'</tt>
 *        (<tt>'&#92;u002d'</tt>,&nbsp;<small>连字符减号</small>),
 *
 *   <li> 加号 <tt>'+'</tt>
 *        (<tt>'&#92;u002b'</tt>,&nbsp;<small>加号</small>),
 *
 *   <li> 句点 <tt>'.'</tt>
 *        (<tt>'&#92;u002e'</tt>,&nbsp;<small>句点</small>),
 *
 *   <li> 冒号 <tt>':'</tt>
 *        (<tt>'&#92;u003a'</tt>,&nbsp;<small>冒号</small>), 和
 *
 *   <li> 下划线 <tt>'_'</tt>
 *        (<tt>'&#92;u005f'</tt>,&nbsp;<small>下划线</small>)。
 *
 * </ul>
 *
 * 字符集名称必须以字母或数字开头。空字符串不是合法的字符集名称。字符集名称不区分大小写；也就是说，在比较字符集名称时总是忽略大小写。字符集名称通常遵循 <a
 * href="http://www.ietf.org/rfc/rfc2278.txt"><i>RFC&nbsp;2278:&nbsp;IANA 字符集
 * 注册程序</i></a> 中记录的约定。
 *
 * <p> 每个字符集都有一个 <i>规范名称</i>，并且可能有一个或多个 <i>别名</i>。规范名称由本类的 {@link #name() name} 方法返回。规范名称通常为大写。字符集的别名由 {@link #aliases() aliases}
 * 方法返回。
 *
 * <p><a name="hn">某些字符集有一个 <i>历史名称</i>，用于与 Java 平台的早期版本兼容。</a> 字符集的历史名称是其规范名称或其别名之一。历史名称由 {@link java.io.InputStreamReader#getEncoding InputStreamReader} 和 {@link
 * java.io.OutputStreamWriter#getEncoding OutputStreamWriter} 类的 <tt>getEncoding()</tt> 方法返回。
 *
 * <p><a name="iana"> </a>如果 Java 平台的实现支持 <a
 * href="http://www.iana.org/assignments/character-sets"><i>IANA 字符集
 * 注册表</i></a> 中列出的字符集，则其规范名称必须是注册表中列出的名称。许多字符集在注册表中被赋予多个名称，其中注册表将其中一个名称标识为 <i>MIME 首选</i>。如果字符集有多个注册表名称，则其规范名称必须是 MIME 首选名称，注册表中的其他名称必须是有效的别名。如果支持的字符集未列在 IANA 注册表中，则其规范名称必须以字符串 <tt>"X-"</tt> 或 <tt>"x-"</tt> 开头。
 *
 * <p> IANA 字符集注册表会随着时间而变化，因此特定字符集的规范名称和别名也可能随时间而变化。为了确保兼容性，建议不要从字符集中删除任何别名，并且如果更改了字符集的规范名称，则其先前的规范名称应成为别名。
 *
 *
 * <h2>标准字符集</h2>
 *
 *
 *
 * <p><a name="standard">每个 Java 平台的实现都必须支持以下标准字符集。</a> 请参阅您的实现的发行文档，以查看是否支持其他字符集。此类可选字符集的行为可能因实现而异。
 *
 * <blockquote><table width="80%" summary="标准字符集描述">
 * <tr><th align="left">字符集</th><th align="left">描述</th></tr>
 * <tr><td valign=top><tt>US-ASCII</tt></td>
 *     <td>七位 ASCII，也称为 <tt>ISO646-US</tt>，
 *         也称为 Unicode 字符集的基本拉丁块</td></tr>
 * <tr><td valign=top><tt>ISO-8859-1&nbsp;&nbsp;</tt></td>
 *     <td>ISO 拉丁字母表 No. 1，也称为 <tt>ISO-LATIN-1</tt></td></tr>
 * <tr><td valign=top><tt>UTF-8</tt></td>
 *     <td>八位 UCS 转换格式</td></tr>
 * <tr><td valign=top><tt>UTF-16BE</tt></td>
 *     <td>十六位 UCS 转换格式，
 *         大端字节序</td></tr>
 * <tr><td valign=top><tt>UTF-16LE</tt></td>
 *     <td>十六位 UCS 转换格式，
 *         小端字节序</td></tr>
 * <tr><td valign=top><tt>UTF-16</tt></td>
 *     <td>十六位 UCS 转换格式，
 *         由可选的字节序标记标识字节序</td></tr>
 * </table></blockquote>
 *
 * <p> <tt>UTF-8</tt> 字符集由 <a
 * href="http://www.ietf.org/rfc/rfc2279.txt"><i>RFC&nbsp;2279</i></a> 规定；其基础转换格式在 ISO&nbsp;10646-1 的修正案 2 中指定，也在 <a
 * href="http://www.unicode.org/unicode/standard/standard.html"><i>Unicode
 * 标准</i></a> 中描述。
 *
 * <p> <tt>UTF-16</tt> 字符集由 <a
 * href="http://www.ietf.org/rfc/rfc2781.txt"><i>RFC&nbsp;2781</i></a> 规定；其基础转换格式在 ISO&nbsp;10646-1 的修正案 1 中指定，也在 <a
 * href="http://www.unicode.org/unicode/standard/standard.html"><i>Unicode
 * 标准</i></a> 中描述。
 *
 * <p> <tt>UTF-16</tt> 字符集使用十六位量，因此对字节序敏感。在这些编码中，流的字节序可以通过初始 <i>字节序标记</i> 表示，该标记由 Unicode 字符 <tt>'&#92;uFEFF'</tt> 表示。字节序标记的处理方式如下：
 *
 * <ul>
 *
 *   <li><p> 在解码时，<tt>UTF-16BE</tt> 和 <tt>UTF-16LE</tt>
 *   字符集将初始字节序标记解释为 <small>零宽不换行空格</small>；在编码时，它们不写入字节序标记。 </p></li>

             *
 *   <li><p> 当解码时，<tt>UTF-16</tt> 字符集解释输入流开始处的字节顺序标记以指示流的字节顺序，但如果不存在字节顺序标记，则默认为大端字节序；当编码时，它使用大端字节序并写入大端字节顺序标记。 </p></li>
 *
 * </ul>
 *
 * 无论如何，输入序列中第一个元素之后出现的字节顺序标记不会被省略，因为相同的代码用于表示 <small>零宽度不间断空格</small>。
 *
 * <p> 每个 Java 虚拟机实例都有一个默认字符集，可能是或可能不是标准字符集之一。默认字符集在虚拟机启动时确定，通常取决于底层操作系统使用的区域设置和字符集。 </p>
 *
 * <p>{@link StandardCharsets} 类为每个标准字符集定义了常量。
 *
 * <h2>术语</h2>
 *
 * <p> 本类的名称取自 <a href="http://www.ietf.org/rfc/rfc2278.txt"><i>RFC&nbsp;2278</i></a> 中使用的术语。在该文档中，<i>字符集</i> 被定义为一个或多个编码字符集和字符编码方案的组合。（此定义令人困惑；某些其他软件系统将 <i>字符集</i> 定义为 <i>编码字符集</i> 的同义词。）
 *
 * <p> <i>编码字符集</i> 是一组抽象字符和一组整数之间的映射。US-ASCII、ISO&nbsp;8859-1、JIS&nbsp;X&nbsp;0201 和 Unicode 是编码字符集的示例。
 *
 * <p> 一些标准定义 <i>字符集</i> 为一组没有关联编号的抽象字符。字母表是这种字符集的一个示例。然而，<i>字符集</i> 和 <i>编码字符集</i> 之间的微妙区别在实践中很少使用；前者已成为后者的简称，包括在 Java API 规范中。
 *
 * <p> <i>字符编码方案</i> 是一个或多个编码字符集和一组八位字节（八位字节）序列之间的映射。UTF-8、UTF-16、ISO&nbsp;2022 和 EUC 是字符编码方案的示例。编码方案通常与特定的编码字符集相关联；例如，UTF-8 仅用于编码 Unicode。然而，某些方案与多个编码字符集相关联；例如，EUC 可用于编码各种亚洲编码字符集中的字符。
 *
 * <p> 当编码字符集仅与单个字符编码方案一起使用时，相应的字符集通常以编码字符集的名称命名；否则，字符集通常以编码方案的名称命名，可能还包括它支持的编码字符集的区域设置。因此，<tt>US-ASCII</tt> 既是编码字符集的名称，也是编码它的字符集的名称，而 <tt>EUC-JP</tt> 是编码日语语言的 JIS&nbsp;X&nbsp;0201、JIS&nbsp;X&nbsp;0208 和 JIS&nbsp;X&nbsp;0212 编码字符集的字符集的名称。
 *
 * <p> Java 编程语言的本地字符编码是 UTF-16。因此，Java 平台中的字符集定义了十六位 UTF-16 代码单元序列（即 char 序列）和字节序列之间的映射。 </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 专家小组
 * @since 1.4
 *
 * @see CharsetDecoder
 * @see CharsetEncoder
 * @see java.nio.charset.spi.CharsetProvider
 * @see java.lang.Character
 */

public abstract class Charset
    implements Comparable<Charset>
{

    /* -- 静态方法 -- */

    private static volatile String bugLevel = null;

    static boolean atBugLevel(String bl) {              // 包私有
        String level = bugLevel;
        if (level == null) {
            if (!sun.misc.VM.isBooted())
                return false;
            bugLevel = level = AccessController.doPrivileged(
                new GetPropertyAction("sun.nio.cs.bugLevel", ""));
        }
        return level.equals(bl);
    }

    /**
     * 检查给定的字符串是否是合法的字符集名称。 </p>
     *
     * @param  s
     *         声称的字符集名称
     *
     * @throws  IllegalCharsetNameException
     *          如果给定的名称不是合法的字符集名称
     */
    private static void checkName(String s) {
        int n = s.length();
        if (!atBugLevel("1.4")) {
            if (n == 0)
                throw new IllegalCharsetNameException(s);
        }
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            if (c >= 'A' && c <= 'Z') continue;
            if (c >= 'a' && c <= 'z') continue;
            if (c >= '0' && c <= '9') continue;
            if (c == '-' && i != 0) continue;
            if (c == '+' && i != 0) continue;
            if (c == ':' && i != 0) continue;
            if (c == '_' && i != 0) continue;
            if (c == '.' && i != 0) continue;
            throw new IllegalCharsetNameException(s);
        }
    }

    /* 标准字符集集合 */
    private static CharsetProvider standardProvider = new StandardCharsets();

    // 最近返回的字符集的缓存，
    // 以及用于查找它们的名称
    //
    private static volatile Object[] cache1 = null; // "一级"缓存
    private static volatile Object[] cache2 = null; // "二级"缓存

    private static void cache(String charsetName, Charset cs) {
        cache2 = cache1;
        cache1 = new Object[] { charsetName, cs };
    }

    // 创建一个迭代器，遍历可用的提供者，忽略那些查找或实例化时导致安全异常被抛出的提供者。应以完全权限调用。
    //
    private static Iterator<CharsetProvider> providers() {
        return new Iterator<CharsetProvider>() {

                ClassLoader cl = ClassLoader.getSystemClassLoader();
                ServiceLoader<CharsetProvider> sl =
                    ServiceLoader.load(CharsetProvider.class, cl);
                Iterator<CharsetProvider> i = sl.iterator();


                            CharsetProvider next = null;

                private boolean getNext() {
                    while (next == null) {
                        try {
                            if (!i.hasNext())
                                return false;
                            next = i.next();
                        } catch (ServiceConfigurationError sce) {
                            if (sce.getCause() instanceof SecurityException) {
                                // 忽略安全异常
                                continue;
                            }
                            throw sce;
                        }
                    }
                    return true;
                }

                public boolean hasNext() {
                    return getNext();
                }

                public CharsetProvider next() {
                    if (!getNext())
                        throw new NoSuchElementException();
                    CharsetProvider n = next;
                    next = null;
                    return n;
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }

            };
    }

    // 线程本地门控，防止递归查找提供者
    private static ThreadLocal<ThreadLocal<?>> gate =
            new ThreadLocal<ThreadLocal<?>>();

    private static Charset lookupViaProviders(final String charsetName) {

        // 运行时启动序列会查找标准字符集，这是由于 VM 调用 System.initializeSystemClass
        // 以设置系统属性和编码文件名等。此时应用程序类加载器尚未初始化，
        // 因此我们不能查找提供者，因为这将导致加载器在信息不完整的情况下被提前初始化。
        //
        if (!sun.misc.VM.isBooted())
            return null;

        if (gate.get() != null)
            // 避免递归查找提供者
            return null;
        try {
            gate.set(gate);

            return AccessController.doPrivileged(
                new PrivilegedAction<Charset>() {
                    public Charset run() {
                        for (Iterator<CharsetProvider> i = providers();
                             i.hasNext();) {
                            CharsetProvider cp = i.next();
                            Charset cs = cp.charsetForName(charsetName);
                            if (cs != null)
                                return cs;
                        }
                        return null;
                    }
                });

        } finally {
            gate.set(null);
        }
    }

    /* 扩展字符集 */
    private static class ExtendedProviderHolder {
        static final CharsetProvider extendedProvider = extendedProvider();
        // 如果已安装，则返回 ExtendedProvider
        private static CharsetProvider extendedProvider() {
            return AccessController.doPrivileged(
                       new PrivilegedAction<CharsetProvider>() {
                           public CharsetProvider run() {
                                try {
                                    Class<?> epc
                                        = Class.forName("sun.nio.cs.ext.ExtendedCharsets");
                                    return (CharsetProvider)epc.newInstance();
                                } catch (ClassNotFoundException x) {
                                    // 扩展字符集不可用
                                    // （charsets.jar 不存在）
                                } catch (InstantiationException |
                                         IllegalAccessException x) {
                                  throw new Error(x);
                                }
                                return null;
                            }
                        });
        }
    }

    private static Charset lookupExtendedCharset(String charsetName) {
        CharsetProvider ecp = ExtendedProviderHolder.extendedProvider;
        return (ecp != null) ? ecp.charsetForName(charsetName) : null;
    }

    private static Charset lookup(String charsetName) {
        if (charsetName == null)
            throw new IllegalArgumentException("Null charset name");
        Object[] a;
        if ((a = cache1) != null && charsetName.equals(a[0]))
            return (Charset)a[1];
        // 我们期望大多数程序会重复使用一个字符集。
        // 我们通过将一级缓存未命中代码放在一个单独的方法中来向 VM 传达这一信息。
        return lookup2(charsetName);
    }

    private static Charset lookup2(String charsetName) {
        Object[] a;
        if ((a = cache2) != null && charsetName.equals(a[0])) {
            cache2 = cache1;
            cache1 = a;
            return (Charset)a[1];
        }
        Charset cs;
        if ((cs = standardProvider.charsetForName(charsetName)) != null ||
            (cs = lookupExtendedCharset(charsetName))           != null ||
            (cs = lookupViaProviders(charsetName))              != null)
        {
            cache(charsetName, cs);
            return cs;
        }

        /* 仅在未找到字符集时检查名称 */
        checkName(charsetName);
        return null;
    }

    /**
     * 告诉命名的字符集是否受支持。
     *
     * @param  charsetName
     *         请求的字符集的名称；可以是规范名称或别名
     *
     * @return  <tt>true</tt> 如果且仅当在当前 Java 虚拟机中支持命名的字符集
     *
     * @throws IllegalCharsetNameException
     *         如果给定的字符集名称不合法
     *
     * @throws  IllegalArgumentException
     *          如果给定的 <tt>charsetName</tt> 为 null
     */
    public static boolean isSupported(String charsetName) {
        return (lookup(charsetName) != null);
    }

                /**
     * 返回指定字符集的字符集对象。
     *
     * @param  charsetName
     *         请求的字符集的名称；可以是规范名称或别名
     *
     * @return  指定字符集的字符集对象
     *
     * @throws  IllegalCharsetNameException
     *          如果给定的字符集名称不合法
     *
     * @throws  IllegalArgumentException
     *          如果给定的 <tt>charsetName</tt> 为 null
     *
     * @throws  UnsupportedCharsetException
     *          如果当前 Java 虚拟机中不支持指定的字符集
     */
    public static Charset forName(String charsetName) {
        Charset cs = lookup(charsetName);
        if (cs != null)
            return cs;
        throw new UnsupportedCharsetException(charsetName);
    }

    // 将给定迭代器中的字符集合并到给定的映射中，忽略名称已在映射中存在条目的字符集。
    //
    private static void put(Iterator<Charset> i, Map<String,Charset> m) {
        while (i.hasNext()) {
            Charset cs = i.next();
            if (!m.containsKey(cs.name()))
                m.put(cs.name(), cs);
        }
    }

    /**
     * 构造一个从规范字符集名称到字符集对象的排序映射。
     *
     * <p> 通过此方法返回的映射将包含当前 Java 虚拟机中支持的每个字符集的条目。如果两个或多个支持的字符集具有相同的规范名称，则结果映射将只包含其中一个；具体包含哪一个未指定。 </p>
     *
     * <p> 调用此方法以及随后使用结果映射可能会导致耗时的磁盘或网络 I/O 操作。此方法适用于需要枚举所有可用字符集的应用程序，例如允许用户选择字符集。此方法不由 {@link #forName
     * forName} 方法使用，后者使用高效的增量查找算法。 </p>
     *
     * <p> 如果新的字符集提供者动态地提供给当前 Java 虚拟机，此方法在不同时间可能返回不同的结果。在没有此类更改的情况下，此方法返回的字符集正是可以通过 {@link
     * #forName forName} 方法检索到的那些。 </p>
     *
     * @return 一个从规范字符集名称到字符集对象的不可变、不区分大小写的映射
     */
    public static SortedMap<String,Charset> availableCharsets() {
        return AccessController.doPrivileged(
            new PrivilegedAction<SortedMap<String,Charset>>() {
                public SortedMap<String,Charset> run() {
                    TreeMap<String,Charset> m =
                        new TreeMap<String,Charset>(
                            ASCIICaseInsensitiveComparator.CASE_INSENSITIVE_ORDER);
                    put(standardProvider.charsets(), m);
                    CharsetProvider ecp = ExtendedProviderHolder.extendedProvider;
                    if (ecp != null)
                        put(ecp.charsets(), m);
                    for (Iterator<CharsetProvider> i = providers(); i.hasNext();) {
                        CharsetProvider cp = i.next();
                        put(cp.charsets(), m);
                    }
                    return Collections.unmodifiableSortedMap(m);
                }
            });
    }

    private static volatile Charset defaultCharset;

    /**
     * 返回此 Java 虚拟机的默认字符集。
     *
     * <p> 默认字符集在虚拟机启动时确定，通常取决于底层操作系统的区域设置和字符集。
     *
     * @return  默认字符集的字符集对象
     *
     * @since 1.5
     */
    public static Charset defaultCharset() {
        if (defaultCharset == null) {
            synchronized (Charset.class) {
                String csn = AccessController.doPrivileged(
                    new GetPropertyAction("file.encoding"));
                Charset cs = lookup(csn);
                if (cs != null)
                    defaultCharset = cs;
                else
                    defaultCharset = forName("UTF-8");
            }
        }
        return defaultCharset;
    }


    /* -- 实例字段和方法 -- */

    private final String name;          // 激活旧 javac 中的一个 bug
    private final String[] aliases;     // 激活旧 javac 中的一个 bug
    private Set<String> aliasSet = null;

    /**
     * 使用给定的规范名称和别名集初始化新的字符集。
     *
     * @param  canonicalName
     *         此字符集的规范名称
     *
     * @param  aliases
     *         此字符集的别名数组，如果没有任何别名则为 null
     *
     * @throws IllegalCharsetNameException
     *         如果规范名称或任何别名不合法
     */
    protected Charset(String canonicalName, String[] aliases) {
        checkName(canonicalName);
        String[] as = (aliases == null) ? new String[0] : aliases;
        for (int i = 0; i < as.length; i++)
            checkName(as[i]);
        this.name = canonicalName;
        this.aliases = as;
    }

    /**
     * 返回此字符集的规范名称。
     *
     * @return  此字符集的规范名称
     */
    public final String name() {
        return name;
    }

    /**
     * 返回一个包含此字符集别名的集合。
     *
     * @return  一个包含此字符集别名的不可变集合
     */
    public final Set<String> aliases() {
        if (aliasSet != null)
            return aliasSet;
        int n = aliases.length;
        HashSet<String> hs = new HashSet<String>(n);
        for (int i = 0; i < n; i++)
            hs.add(aliases[i]);
        aliasSet = Collections.unmodifiableSet(hs);
        return aliasSet;
    }

    /**
     * 返回此字符集在默认区域设置中的可读名称。
     *
     * <p> 该方法的默认实现简单地返回此字符集的规范名称。此类的具体子类可以重写此方法以提供本地化的显示名称。 </p>
     *
     * @return  此字符集在默认区域设置中的显示名称
     */
    public String displayName() {
        return name;
    }


                /**
     * 告知此字符集是否在 <a
     * href="http://www.iana.org/assignments/character-sets">IANA 字符集
     * 注册表</a> 中注册。
     *
     * @return  如果且仅当此字符集的实现者知道该字符集已注册到 IANA 时，返回 <tt>true</tt>
     */
    public final boolean isRegistered() {
        return !name.startsWith("X-") && !name.startsWith("x-");
    }

    /**
     * 返回给定区域设置下此字符集的人类可读名称。
     *
     * <p> 该方法的默认实现仅返回此字符集的规范名称。此类的具体子类可以重写此方法以提供本地化的显示名称。 </p>
     *
     * @param  locale
     *         要检索显示名称的区域设置
     *
     * @return  给定区域设置下此字符集的显示名称
     */
    public String displayName(Locale locale) {
        return name;
    }

    /**
     * 告知此字符集是否包含给定的字符集。
     *
     * <p> 如果且仅当在 <i>D</i> 中可表示的每个字符在 <i>C</i> 中也可表示时，字符集 <i>C</i> 被认为 <i>包含</i> 字符集 <i>D</i>。如果这种关系成立，则可以保证在 <i>D</i> 中可以编码的每个字符串在 <i>C</i> 中也可以编码，而无需执行任何替换。
     *
     * <p> <i>C</i> 包含 <i>D</i> 并不意味着 <i>C</i> 中由特定字节序列表示的每个字符在 <i>D</i> 中也由相同的字节序列表示，尽管有时情况确实如此。
     *
     * <p> 每个字符集都包含自身。
     *
     * <p> 该方法计算包含关系的近似值：如果返回 <tt>true</tt>，则给定的字符集已知被此字符集包含；如果返回 <tt>false</tt>，则不一定表示给定的字符集不被此字符集包含。
     *
     * @param   cs
     *          给定的字符集
     *
     * @return  如果给定的字符集被此字符集包含，则返回 <tt>true</tt>
     */
    public abstract boolean contains(Charset cs);

    /**
     * 为此字符集构造一个新的解码器。
     *
     * @return  为此字符集构造的新解码器
     */
    public abstract CharsetDecoder newDecoder();

    /**
     * 为此字符集构造一个新的编码器。
     *
     * @return  为此字符集构造的新编码器
     *
     * @throws  UnsupportedOperationException
     *          如果此字符集不支持编码
     */
    public abstract CharsetEncoder newEncoder();

    /**
     * 告知此字符集是否支持编码。
     *
     * <p> 几乎所有字符集都支持编码。主要的例外是一些特殊用途的 <i>自动检测</i> 字符集，其解码器可以通过检查输入字节序列来确定使用了几种可能的编码方案中的哪一种。这样的字符集不支持编码，因为没有方法可以确定输出时应使用哪种编码。此类字符集的实现应重写此方法以返回 <tt>false</tt>。 </p>
     *
     * @return  如果且仅当此字符集支持编码时，返回 <tt>true</tt>
     */
    public boolean canEncode() {
        return true;
    }

    /**
     * 便捷方法，将此字符集中的字节解码为 Unicode 字符。
     *
     * <p> 在字符集 <tt>cs</tt> 上调用此方法的调用返回与以下表达式相同的结果：
     *
     * <pre>
     *     cs.newDecoder()
     *       .onMalformedInput(CodingErrorAction.REPLACE)
     *       .onUnmappableCharacter(CodingErrorAction.REPLACE)
     *       .decode(bb); </pre>
     *
     * 除了它可能更高效，因为它可以在连续的调用之间缓存解码器。
     *
     * <p> 该方法总是用此字符集的默认替换字节数组替换格式错误的输入和无法映射的字符序列。为了检测这些序列，请直接使用 {@link
     * CharsetDecoder#decode(java.nio.ByteBuffer)} 方法。 </p>
     *
     * @param  bb  要解码的字节缓冲区
     *
     * @return  包含解码字符的字符缓冲区
     */
    public final CharBuffer decode(ByteBuffer bb) {
        try {
            return ThreadLocalCoders.decoderFor(this)
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE)
                .decode(bb);
        } catch (CharacterCodingException x) {
            throw new Error(x);         // 不可能发生
        }
    }

    /**
     * 便捷方法，将 Unicode 字符编码为此字符集中的字节。
     *
     * <p> 在字符集 <tt>cs</tt> 上调用此方法的调用返回与以下表达式相同的结果：
     *
     * <pre>
     *     cs.newEncoder()
     *       .onMalformedInput(CodingErrorAction.REPLACE)
     *       .onUnmappableCharacter(CodingErrorAction.REPLACE)
     *       .encode(bb); </pre>
     *
     * 除了它可能更高效，因为它可以在连续的调用之间缓存编码器。
     *
     * <p> 该方法总是用此字符集的默认替换字符串替换格式错误的输入和无法映射的字符序列。为了检测这些序列，请直接使用 {@link
     * CharsetEncoder#encode(java.nio.CharBuffer)} 方法。 </p>
     *
     * @param  cb  要编码的字符缓冲区
     *
     * @return  包含编码字符的字节缓冲区
     */
    public final ByteBuffer encode(CharBuffer cb) {
        try {
            return ThreadLocalCoders.encoderFor(this)
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE)
                .encode(cb);
        } catch (CharacterCodingException x) {
            throw new Error(x);         // 不可能发生
        }
    }

                /**
     * 方便方法，将字符串编码为此字符集的字节。
     *
     * <p> 对字符集 <tt>cs</tt> 的此方法的调用返回与表达式相同的结果
     *
     * <pre>
     *     cs.encode(CharBuffer.wrap(s)); </pre>
     *
     * @param  str  要编码的字符串
     *
     * @return  包含编码字符的字节缓冲区
     */
    public final ByteBuffer encode(String str) {
        return encode(CharBuffer.wrap(str));
    }

    /**
     * 比较此字符集与另一个字符集。
     *
     * <p> 字符集按其规范名称排序，不区分大小写。 </p>
     *
     * @param  that
     *         要与此字符集进行比较的字符集
     *
     * @return 如果此字符集小于、等于或大于指定的字符集，则返回一个负整数、零或正整数
     */
    public final int compareTo(Charset that) {
        return (name().compareToIgnoreCase(that.name()));
    }

    /**
     * 计算此字符集的哈希码。
     *
     * @return  整数哈希码
     */
    public final int hashCode() {
        return name().hashCode();
    }

    /**
     * 判断此对象是否等于另一个对象。
     *
     * <p> 两个字符集相等当且仅当它们具有相同的规范名称。字符集永远不会等于任何其他类型的对象。 </p>
     *
     * @return  如果且仅当此字符集等于给定对象时，返回 <tt>true</tt>
     */
    public final boolean equals(Object ob) {
        if (!(ob instanceof Charset))
            return false;
        if (this == ob)
            return true;
        return name.equals(((Charset)ob).name());
    }

    /**
     * 返回描述此字符集的字符串。
     *
     * @return  描述此字符集的字符串
     */
    public final String toString() {
        return name();
    }

}
