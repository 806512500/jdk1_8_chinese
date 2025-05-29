
/*
 * 版权所有 (c) 1997, 2013, Oracle 和/或其附属公司。保留所有权利。
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

/*
 * (C) 版权所有 Taligent, Inc. 1996-1998 - 保留所有权利
 * (C) 版权所有 IBM Corp. 1996-1998 - 保留所有权利
 *
 *   本源代码和文档的原始版本受版权保护并归 Taligent, Inc. 所有，它是 IBM 的全资子公司。这些
 * 材料是根据 Taligent 和 Sun 之间的许可协议提供的。这项技术受到多项美国和国际
 * 专利的保护。此通知和对 Taligent 的归属不得移除。
 *   Taligent 是 Taligent, Inc. 的注册商标。
 *
 */

package java.text;

import java.lang.ref.SoftReference;
import java.text.spi.CollatorProvider;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleServiceProviderPool;


/**
 * <code>Collator</code> 类执行与区域设置相关的 <code>String</code> 比较。您使用此类构建
 * 自然语言文本的搜索和排序例程。
 *
 * <p>
 * <code>Collator</code> 是一个抽象基类。子类实现特定的排序策略。一个子类，
 * <code>RuleBasedCollator</code>，目前随 Java 平台提供，并适用于广泛的语言。其他
 * 子类可以创建以处理更专业的需求。
 *
 * <p>
 * 与其他区域设置敏感的类一样，您可以使用静态
 * 工厂方法 <code>getInstance</code> 为给定的区域设置获取适当的
 * <code>Collator</code> 对象。如果您需要了解特定排序策略的详细信息或
 * 需要修改该策略，您只需要查看 <code>Collator</code> 的子类。
 *
 * <p>
 * 以下示例显示了如何使用默认区域设置的 <code>Collator</code> 比较两个字符串。
 * <blockquote>
 * <pre>{@code
 * // 在默认区域设置中比较两个字符串
 * Collator myCollator = Collator.getInstance();
 * if( myCollator.compare("abc", "ABC") < 0 )
 *     System.out.println("abc 小于 ABC");
 * else
 *     System.out.println("abc 大于或等于 ABC");
 * }</pre>
 * </blockquote>
 *
 * <p>
 * 您可以设置 <code>Collator</code> 的 <em>强度</em> 属性
 * 以确定在比较中视为重要的差异级别。提供了四种强度： <code>PRIMARY</code>,
 * <code>SECONDARY</code>, <code>TERTIARY</code> 和 <code>IDENTICAL</code>。
 * 强度与语言特征的分配是区域设置依赖的。例如，在捷克语中，"e" 和 "f" 被视为
 * 主要差异，而 "e" 和 "&#283;" 被视为次要差异，
 * "e" 和 "E" 被视为三级差异，"e" 和 "e" 是相同的。
 * 以下示例显示了如何在美式英语中忽略大小写和重音。
 * <blockquote>
 * <pre>
 * // 获取美式英语的 Collator 并将其强度设置为 PRIMARY
 * Collator usCollator = Collator.getInstance(Locale.US);
 * usCollator.setStrength(Collator.PRIMARY);
 * if( usCollator.compare("abc", "ABC") == 0 ) {
 *     System.out.println("字符串等效");
 * }
 * </pre>
 * </blockquote>
 * <p>
 * 对于仅需比较一次的 <code>String</code>，<code>compare</code>
 * 方法提供了最佳性能。然而，在对 <code>String</code> 列表进行排序时，
 * 通常需要多次比较每个 <code>String</code>。在这种情况下，<code>CollationKey</code>
 * 提供了更好的性能。<code>CollationKey</code> 类将 <code>String</code> 转换为一系列位，
 * 可以与其他 <code>CollationKey</code> 进行位比较。<code>CollationKey</code>
 * 由 <code>Collator</code> 对象为给定的 <code>String</code> 创建。
 * <br>
 * <strong>注意：</strong> 不同 <code>Collator</code> 创建的 <code>CollationKey</code>
 * 不能进行比较。有关使用 <code>CollationKey</code> 的示例，请参阅
 * {@link CollationKey} 类的描述。
 *
 * @see         RuleBasedCollator
 * @see         CollationKey
 * @see         CollationElementIterator
 * @see         Locale
 * @author      Helena Shih, Laura Werner, Richard Gillam
 */

public abstract class Collator
    implements java.util.Comparator<Object>, Cloneable
{
    /**
     * Collator 强度值。设置后，仅考虑 PRIMARY 差异在比较中显著。强度与语言特征的分配是区域设置依赖的。
     * 一个常见的例子是不同的基础字母（"a" vs "b"）被视为 PRIMARY 差异。
     * @see java.text.Collator#setStrength
     * @see java.text.Collator#getStrength
     */
    public final static int PRIMARY = 0;
    /**
     * Collator 强度值。设置后，仅考虑 SECONDARY 及以上差异在比较中显著。强度与语言特征的分配是区域设置依赖的。
     * 一个常见的例子是相同基础字母的不同重音形式（"a" vs "\u00E4"）被视为 SECONDARY 差异。
     * @see java.text.Collator#setStrength
     * @see java.text.Collator#getStrength
     */
    public final static int SECONDARY = 1;
    /**
     * Collator 强度值。设置后，仅考虑 TERTIARY 及以上差异在比较中显著。强度与语言特征的分配是区域设置依赖的。
     * 一个常见的例子是大小写差异（"a" vs "A"）被视为 TERTIARY 差异。
     * @see java.text.Collator#setStrength
     * @see java.text.Collator#getStrength
     */
    public final static int TERTIARY = 2;

    /**
     * Collator 强度值。设置后，所有差异在比较中均视为显著。强度与语言特征的分配是区域设置依赖的。
     * 一个常见的例子是控制字符（"&#092;u0001" vs "&#092;u0002"）在
     * PRIMARY, SECONDARY, 和 TERTIARY 级别上被视为等效，但在 IDENTICAL 级别上不同。
     * 此外，如果分解设置为 NO_DECOMPOSITION，则预组合重音（如 "&#092;u00C0" (A-grave)）
     * 和组合重音（如 "A&#092;u0300" (A, combining-grave)）之间的差异在 IDENTICAL 级别上被视为显著。
     */
    public final static int IDENTICAL = 3;


                /**
     * 分解模式值。当设置为 NO_DECOMPOSITION 时，带重音的字符在排序时不会被分解。这是默认设置，提供最快的排序，
     * 但对于不使用重音的语言，只能产生正确的结果。
     * @see java.text.Collator#getDecomposition
     * @see java.text.Collator#setDecomposition
     */
    public final static int NO_DECOMPOSITION = 0;

    /**
     * 分解模式值。当设置为 CANONICAL_DECOMPOSITION 时，根据 Unicode 标准的规范变体字符将在排序时被分解。这应该用于获得
     * 带重音字符的正确排序。
     * <p>
     * CANONICAL_DECOMPOSITION 对应于 Unicode 技术报告 #15 中描述的规范化形式 D。
     * @see java.text.Collator#getDecomposition
     * @see java.text.Collator#setDecomposition
     */
    public final static int CANONICAL_DECOMPOSITION = 1;

    /**
     * 分解模式值。当设置为 FULL_DECOMPOSITION 时，Unicode 规范变体和 Unicode 兼容变体都将被分解用于排序。这不仅会导致带重音的
     * 字符被排序，还会导致具有特殊格式的字符与其规范形式一起排序。例如，半角和全角 ASCII 和片假名字符将被一起排序。
     * FULL_DECOMPOSITION 是最完整也是最慢的分解模式。
     * <p>
     * FULL_DECOMPOSITION 对应于 Unicode 技术报告 #15 中描述的规范化形式 KD。
     * @see java.text.Collator#getDecomposition
     * @see java.text.Collator#setDecomposition
     */
    public final static int FULL_DECOMPOSITION = 2;

    /**
     * 获取当前默认区域设置的 Collator。
     * 默认区域设置由 java.util.Locale.getDefault 确定。
     * @return 默认区域设置的 Collator（例如，en_US）。
     * @see java.util.Locale#getDefault
     */
    public static synchronized Collator getInstance() {
        return getInstance(Locale.getDefault());
    }

    /**
     * 获取所需区域设置的 Collator。
     * @param desiredLocale 所需的区域设置。
     * @return 所需区域设置的 Collator。
     * @see java.util.Locale
     * @see java.util.ResourceBundle
     */
    public static Collator getInstance(Locale desiredLocale) {
        SoftReference<Collator> ref = cache.get(desiredLocale);
        Collator result = (ref != null) ? ref.get() : null;
        if (result == null) {
            LocaleProviderAdapter adapter;
            adapter = LocaleProviderAdapter.getAdapter(CollatorProvider.class,
                                                       desiredLocale);
            CollatorProvider provider = adapter.getCollatorProvider();
            result = provider.getInstance(desiredLocale);
            if (result == null) {
                result = LocaleProviderAdapter.forJRE()
                             .getCollatorProvider().getInstance(desiredLocale);
            }
            while (true) {
                if (ref != null) {
                    // 如果有任何空的 SoftReference，则移除
                    cache.remove(desiredLocale, ref);
                }
                ref = cache.putIfAbsent(desiredLocale, new SoftReference<>(result));
                if (ref == null) {
                    break;
                }
                Collator cachedColl = ref.get();
                if (cachedColl != null) {
                    result = cachedColl;
                    break;
                }
            }
        }
        return (Collator) result.clone(); // 使世界更安全
    }

    /**
     * 根据此 Collator 的排序规则比较源字符串和目标字符串。如果源字符串小于、等于或大于目标字符串，则返回一个小于、等于或大于零的整数。
     * 有关使用的示例，请参阅 Collator 类描述。
     * <p>
     * 对于一次性比较，此方法具有最佳性能。如果一个给定的字符串将涉及多次比较，则 CollationKey.compareTo 的性能最佳。
     * 有关使用 CollationKeys 的示例，请参阅 Collator 类描述。
     * @param source 源字符串。
     * @param target 目标字符串。
     * @return 返回一个整数值。如果源小于目标，则值小于零；如果源和目标相等，则值为零；如果源大于目标，则值大于零。
     * @see java.text.CollationKey
     * @see java.text.Collator#getCollationKey
     */
    public abstract int compare(String source, String target);

    /**
     * 按顺序比较其两个参数。如果第一个参数小于、等于或大于第二个参数，则返回一个负整数、零或正整数。
     * <p>
     * 此实现仅返回 <code> compare((String)o1, (String)o2) </code>。
     *
     * @return 如果第一个参数小于、等于或大于第二个参数，则返回一个负整数、零或正整数。
     * @exception ClassCastException 参数不能转换为字符串。
     * @see java.util.Comparator
     * @since   1.2
     */
    @Override
    public int compare(Object o1, Object o2) {
    return compare((String)o1, (String)o2);
    }

    /**
     * 将字符串转换为一系列可以与其他 CollationKeys 逐位比较的位。当字符串涉及多次比较时，CollationKeys 的性能优于 Collator.compare。
     * 有关使用 CollationKeys 的示例，请参阅 Collator 类描述。
     * @param source 要转换为排序键的字符串。
     * @return 基于此 Collator 的排序规则的给定字符串的 CollationKey。如果源字符串为 null，则返回 null CollationKey。
     * @see java.text.CollationKey
     * @see java.text.Collator#compare
     */
    public abstract CollationKey getCollationKey(String source);


                /**
     * 用于根据此 Collator 的排序规则比较两个字符串的相等性。
     * @param source 要比较的源字符串。
     * @param target 要比较的目标字符串。
     * @return 如果字符串根据排序规则相等，则返回 true。否则返回 false。
     * @see java.text.Collator#compare
     */
    public boolean equals(String source, String target)
    {
        return (compare(source, target) == Collator.EQUAL);
    }

    /**
     * 返回此 Collator 的强度属性。强度属性确定在比较过程中被认为重要的最小差异级别。
     * 有关使用的示例，请参阅 Collator 类描述。
     * @return 此 Collator 当前的强度属性。
     * @see java.text.Collator#setStrength
     * @see java.text.Collator#PRIMARY
     * @see java.text.Collator#SECONDARY
     * @see java.text.Collator#TERTIARY
     * @see java.text.Collator#IDENTICAL
     */
    public synchronized int getStrength()
    {
        return strength;
    }

    /**
     * 设置此 Collator 的强度属性。强度属性确定在比较过程中被认为重要的最小差异级别。
     * 有关使用的示例，请参阅 Collator 类描述。
     * @param newStrength 新的强度值。
     * @see java.text.Collator#getStrength
     * @see java.text.Collator#PRIMARY
     * @see java.text.Collator#SECONDARY
     * @see java.text.Collator#TERTIARY
     * @see java.text.Collator#IDENTICAL
     * @exception  IllegalArgumentException 如果新的强度值不是 PRIMARY、SECONDARY、TERTIARY 或 IDENTICAL 之一。
     */
    public synchronized void setStrength(int newStrength) {
        if ((newStrength != PRIMARY) &&
            (newStrength != SECONDARY) &&
            (newStrength != TERTIARY) &&
            (newStrength != IDENTICAL)) {
            throw new IllegalArgumentException("不正确的比较级别。");
        }
        strength = newStrength;
    }

    /**
     * 获取此 Collator 的分解模式。分解模式确定如何处理 Unicode 组合字符。调整分解模式允许用户在更快和更完整的排序行为之间进行选择。
     * <p>分解模式的三个值为：
     * <UL>
     * <LI>NO_DECOMPOSITION，
     * <LI>CANONICAL_DECOMPOSITION
     * <LI>FULL_DECOMPOSITION。
     * </UL>
     * 有关这三个常量的含义，请参阅其文档。
     * @return 分解模式
     * @see java.text.Collator#setDecomposition
     * @see java.text.Collator#NO_DECOMPOSITION
     * @see java.text.Collator#CANONICAL_DECOMPOSITION
     * @see java.text.Collator#FULL_DECOMPOSITION
     */
    public synchronized int getDecomposition()
    {
        return decmp;
    }
    /**
     * 设置此 Collator 的分解模式。有关分解模式的描述，请参阅 getDecomposition。
     * @param decompositionMode 新的分解模式。
     * @see java.text.Collator#getDecomposition
     * @see java.text.Collator#NO_DECOMPOSITION
     * @see java.text.Collator#CANONICAL_DECOMPOSITION
     * @see java.text.Collator#FULL_DECOMPOSITION
     * @exception IllegalArgumentException 如果给定的值不是有效的分解模式。
     */
    public synchronized void setDecomposition(int decompositionMode) {
        if ((decompositionMode != NO_DECOMPOSITION) &&
            (decompositionMode != CANONICAL_DECOMPOSITION) &&
            (decompositionMode != FULL_DECOMPOSITION)) {
            throw new IllegalArgumentException("错误的分解模式。");
        }
        decmp = decompositionMode;
    }

    /**
     * 返回此类的 <code>getInstance</code> 方法可以返回本地化实例的所有区域设置。
     * 返回的数组表示由 Java 运行时和已安装的
     * {@link java.text.spi.CollatorProvider CollatorProvider} 实现支持的区域设置的并集。
     * 它必须至少包含一个等于
     * {@link java.util.Locale#US Locale.US} 的区域设置实例。
     *
     * @return 可以获得本地化 <code>Collator</code> 实例的区域设置数组。
     */
    public static synchronized Locale[] getAvailableLocales() {
        LocaleServiceProviderPool pool =
            LocaleServiceProviderPool.getPool(CollatorProvider.class);
        return pool.getAvailableLocales();
    }

    /**
     * 覆盖 Cloneable
     */
    @Override
    public Object clone()
    {
        try {
            return (Collator)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /**
     * 比较两个 Collator 的相等性。
     * @param that 要与之比较的 Collator。
     * @return 如果此 Collator 与 that Collator 相同，则返回 true；否则返回 false。
     */
    @Override
    public boolean equals(Object that)
    {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Collator other = (Collator) that;
        return ((strength == other.strength) &&
                (decmp == other.decmp));
    }

    /**
     * 生成此 Collator 的哈希码。
     */
    @Override
    abstract public int hashCode();

    /**
     * 默认构造函数。此构造函数受保护，以便子类可以访问它。用户通常通过调用工厂方法 getInstance 创建 Collator 子类。
     * @see java.text.Collator#getInstance
     */
    protected Collator()
    {
        strength = TERTIARY;
        decmp = CANONICAL_DECOMPOSITION;
    }

    private int strength = 0;
    private int decmp = 0;
    private static final ConcurrentMap<Locale, SoftReference<Collator>> cache
            = new ConcurrentHashMap<>();

                //
    // FIXME: 这三个常量应该被移除。
    //
    /**
     * 如果在 compare() 方法中源字符串被比较为小于目标字符串，则返回 LESS。
     * @see java.text.Collator#compare
     */
    final static int LESS = -1;
    /**
     * 如果在 compare() 方法中源字符串被比较为等于目标字符串，则返回 EQUAL。
     * @see java.text.Collator#compare
     */
    final static int EQUAL = 0;
    /**
     * 如果在 compare() 方法中源字符串被比较为大于目标字符串，则返回 GREATER。
     * @see java.text.Collator#compare
     */
    final static int GREATER = 1;
 }
