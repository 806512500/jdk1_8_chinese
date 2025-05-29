/*
 * 版权所有 (c) 1996, 2013, Oracle 和/或其附属公司。保留所有权利。
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
 * 版权所有 (C) 1996, 1997 - Taligent, Inc. 保留所有权利
 * 版权所有 (C) 1996 - 1998 - IBM Corp. 保留所有权利
 *
 * 本源代码和文档的原始版本由 Taligent, Inc. 版权所有并拥有，Taligent, Inc. 是 IBM 的全资子公司。这些材料是根据 Taligent 和 Sun 之间的许可协议提供的。这项技术受多项美国和国际专利保护。
 *
 * 本通知和对 Taligent 的归属不得删除。Taligent 是 Taligent, Inc. 的注册商标。
 *
 */

package java.text;

import java.lang.ref.SoftReference;
import java.text.spi.BreakIteratorProvider;
import java.util.Locale;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleServiceProviderPool;


/**
 * <code>BreakIterator</code> 类实现了用于查找文本中边界位置的方法。<code>BreakIterator</code> 的实例维护一个当前位置，并扫描文本，返回字符边界发生的位置。
 * 内部，<code>BreakIterator</code> 使用 <code>CharacterIterator</code> 扫描文本，因此能够扫描任何实现该协议的对象中的文本。<code>StringCharacterIterator</code>
 * 用于扫描传递给 <code>setText</code> 的 <code>String</code> 对象。
 *
 * <p>
 * 您可以使用此类提供的工厂方法来创建各种类型的断行迭代器实例。特别是，使用 <code>getWordInstance</code>、<code>getLineInstance</code>、
 * <code>getSentenceInstance</code> 和 <code>getCharacterInstance</code> 创建执行单词、行、句子和字符边界分析的 <code>BreakIterator</code>。
 * 单个 <code>BreakIterator</code> 只能处理一个单位（单词、行、句子等）。您必须为每个希望执行的单位边界分析使用不同的迭代器。
 *
 * <p><a name="line"></a>
 * 行边界分析确定文本字符串在换行时可以断开的位置。该机制正确处理标点符号和连字符单词。实际的换行还需要考虑可用的行宽，由更高层次的软件处理。
 *
 * <p><a name="sentence"></a>
 * 句子边界分析允许正确解释数字和缩写中的句点以及尾随的标点符号，如引号和括号。
 *
 * <p><a name="word"></a>
 * 单词边界分析用于搜索和替换功能，以及允许用户通过双击选择单词的文本编辑应用程序。单词选择提供对标点符号的正确解释。不属于单词的字符，如符号或标点符号，两边都有单词边界。
 *
 * <p><a name="character"></a>
 * 字符边界分析允许用户以他们期望的方式与字符交互，例如，在文本字符串中移动光标。字符边界分析提供正确的字符字符串导航，无论字符如何存储。返回的边界可能是补充字符、组合字符序列或连字簇的边界。
 * 例如，带重音的字符可能存储为基础字符和变音符号。用户认为的字符在不同语言中可能有所不同。
 *
 * <p>
 * 由此类工厂方法返回的 <code>BreakIterator</code> 实例仅用于自然语言文本，而不用于编程语言文本。但是，可以定义子类来对编程语言进行分词。
 *
 * <P>
 * <strong>示例</strong>:<P>
 * 创建和使用文本边界：
 * <blockquote>
 * <pre>
 * public static void main(String args[]) {
 *      if (args.length == 1) {
 *          String stringToExamine = args[0];
 *          // 按顺序打印每个单词
 *          BreakIterator boundary = BreakIterator.getWordInstance();
 *          boundary.setText(stringToExamine);
 *          printEachForward(boundary, stringToExamine);
 *          // 逆序打印每个句子
 *          boundary = BreakIterator.getSentenceInstance(Locale.US);
 *          boundary.setText(stringToExamine);
 *          printEachBackward(boundary, stringToExamine);
 *          printFirst(boundary, stringToExamine);
 *          printLast(boundary, stringToExamine);
 *      }
 * }
 * </pre>
 * </blockquote>
 *
 * 按顺序打印每个元素：
 * <blockquote>
 * <pre>
 * public static void printEachForward(BreakIterator boundary, String source) {
 *     int start = boundary.first();
 *     for (int end = boundary.next();
 *          end != BreakIterator.DONE;
 *          start = end, end = boundary.next()) {
 *          System.out.println(source.substring(start,end));
 *     }
 * }
 * </pre>
 * </blockquote>
 *
 * 逆序打印每个元素：
 * <blockquote>
 * <pre>
 * public static void printEachBackward(BreakIterator boundary, String source) {
 *     int end = boundary.last();
 *     for (int start = boundary.previous();
 *          start != BreakIterator.DONE;
 *          end = start, start = boundary.previous()) {
 *         System.out.println(source.substring(start,end));
 *     }
 * }
 * </pre>
 * </blockquote>
 *
 * 打印第一个元素：
 * <blockquote>
 * <pre>
 * public static void printFirst(BreakIterator boundary, String source) {
 *     int start = boundary.first();
 *     int end = boundary.next();
 *     System.out.println(source.substring(start,end));
 * }
 * </pre>
 * </blockquote>
 *
 * 打印最后一个元素：
 * <blockquote>
 * <pre>
 * public static void printLast(BreakIterator boundary, String source) {
 *     int end = boundary.last();
 *     int start = boundary.previous();
 *     System.out.println(source.substring(start,end));
 * }
 * </pre>
 * </blockquote>
 *
 * 打印指定位置的元素：
 * <blockquote>
 * <pre>
 * public static void printAt(BreakIterator boundary, int pos, String source) {
 *     int end = boundary.following(pos);
 *     int start = boundary.previous();
 *     System.out.println(source.substring(start,end));
 * }
 * </pre>
 * </blockquote>
 *
 * 查找下一个单词：
 * <blockquote>
 * <pre>{@code
 * public static int nextWordStartAfter(int pos, String text) {
 *     BreakIterator wb = BreakIterator.getWordInstance();
 *     wb.setText(text);
 *     int last = wb.following(pos);
 *     int current = wb.next();
 *     while (current != BreakIterator.DONE) {
 *         for (int p = last; p < current; p++) {
 *             if (Character.isLetter(text.codePointAt(p)))
 *                 return last;
 *         }
 *         last = current;
 *         current = wb.next();
 *     }
 *     return BreakIterator.DONE;
 * }
 * }</pre>
 * （由 BreakIterator.getWordInstance() 返回的迭代器具有独特性，即它返回的断点位置不同时表示被迭代对象的开始和结束。也就是说，句子断点迭代器返回的断点每个都表示一个句子的结束和下一个句子的开始。
 * 而对于单词断点迭代器，两个边界之间的字符可能是一个单词，也可能是一个单词之间的标点或空格。上述代码使用一个简单的启发式方法来确定哪个边界是单词的开始：如果这个边界和下一个边界之间的字符至少包含一个字母（可以是字母、CJK 汉字、韩文音节、假名字符等），则这个边界和下一个边界之间的文本是一个单词；否则，它是单词之间的材料。）
 * </blockquote>
 *
 * @see CharacterIterator
 *
 */


public abstract class BreakIterator implements Cloneable
{
    /**
     * 构造函数。BreakIterator 是无状态的，没有默认行为。
     */
    protected BreakIterator()
    {
    }

    /**
     * 创建此迭代器的副本
     * @return 此迭代器的副本
     */
    @Override
    public Object clone()
    {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /**
     * 当达到第一个或最后一个文本边界时，previous()、next()、next(int)、preceding(int)
     * 和 following(int) 方法返回 DONE。
     */
    public static final int DONE = -1;

    /**
     * 返回第一个边界。迭代器的当前位置设置为第一个文本边界。
     * @return 第一个文本边界的字符索引。
     */
    public abstract int first();

    /**
     * 返回最后一个边界。迭代器的当前位置设置为最后一个文本边界。
     * @return 最后一个文本边界的字符索引。
     */
    public abstract int last();

    /**
     * 从当前边界返回第 n 个边界。如果达到第一个或最后一个文本边界，则返回
     * <code>BreakIterator.DONE</code>，并且当前位置设置为第一个或最后一个文本边界，具体取决于达到的是哪一个。否则，
     * 迭代器的当前位置设置为新的边界。例如，如果迭代器的当前位置是第 m 个文本边界，并且从当前边界到最后一个文本边界还有三个边界，
     * 则 next(2) 调用将返回 m + 2。新的文本位置设置为第 (m + 2) 个文本边界。next(4) 调用将返回
     * <code>BreakIterator.DONE</code>，并且最后一个文本边界将成为新的文本位置。
     * @param n 要返回的边界。值为 0 时不执行任何操作。负值移动到前一个边界，正值移动到后一个边界。
     * @return 从当前位置开始的第 n 个边界的字符索引，或者如果达到第一个或最后一个文本边界，则返回
     * <code>BreakIterator.DONE</code>。
     */
    public abstract int next(int n);

    /**
     * 返回当前边界的下一个边界。如果当前边界是最后一个文本边界，则返回 <code>BreakIterator.DONE</code>，
     * 并且迭代器的当前位置不变。否则，迭代器的当前位置设置为当前边界的下一个边界。
     * @return 下一个文本边界的字符索引，或者如果当前边界是最后一个文本边界，则返回
     * <code>BreakIterator.DONE</code>。
     * 等效于 next(1)。
     * @see #next(int)
     */
    public abstract int next();

    /**
     * 返回当前边界的前一个边界。如果当前边界是第一个文本边界，则返回 <code>BreakIterator.DONE</code>，
     * 并且迭代器的当前位置不变。否则，迭代器的当前位置设置为当前边界的前一个边界。
     * @return 前一个文本边界的字符索引，或者如果当前边界是第一个文本边界，则返回
     * <code>BreakIterator.DONE</code>。
     */
    public abstract int previous();

    /**
     * 返回指定字符偏移量之后的第一个边界。如果指定的偏移量等于最后一个文本边界，则返回
     * <code>BreakIterator.DONE</code>，并且迭代器的当前位置不变。否则，迭代器的当前位置设置为返回的边界。
     * 返回的值总是大于偏移量或 <code>BreakIterator.DONE</code>。
     * @param offset 开始扫描的字符偏移量。
     * @return 指定偏移量之后的第一个边界，或者如果作为偏移量传递的是最后一个文本边界，则返回
     * <code>BreakIterator.DONE</code>。
     * @exception  IllegalArgumentException 如果指定的偏移量小于第一个文本边界或大于最后一个文本边界。
     */
    public abstract int following(int offset);

    /**
     * 返回指定字符偏移量之前最后一个边界。如果指定的偏移量等于第一个文本边界，则返回
     * <code>BreakIterator.DONE</code>，并且迭代器的当前位置不变。否则，迭代器的当前位置设置为返回的边界。
     * 返回的值总是小于偏移量或 <code>BreakIterator.DONE</code>。
     * @param offset 开始扫描的字符偏移量。
     * @return 指定偏移量之前的最后一个边界，或者如果作为偏移量传递的是第一个文本边界，则返回
     * <code>BreakIterator.DONE</code>。
     * @exception   IllegalArgumentException 如果指定的偏移量小于第一个文本边界或大于最后一个文本边界。
     * @since 1.2
     */
    public int preceding(int offset) {
        // 注意：此实现仅因为不能向现有类添加新的抽象方法而存在。几乎总是有更好的、更快的方法来实现这一点。
        int pos = following(offset);
        while (pos >= offset && pos != DONE) {
            pos = previous();
        }
        return pos;
    }

    /**
     * 如果指定的字符偏移量是文本边界，则返回 true。
     * @param offset 要检查的字符偏移量。
     * @return 如果 "offset" 是边界位置，则返回 <code>true</code>，否则返回 <code>false</code>。
     * @exception   IllegalArgumentException 如果指定的偏移量小于第一个文本边界或大于最后一个文本边界。
     * @since 1.2
     */
    public boolean isBoundary(int offset) {
        // 注意：此实现可能在大多数情况下是错误的，因为它没有考虑到传递给 setText() 的 CharacterIterator 可能没有 0 的开始偏移量。
        // 但由于抽象 BreakIterator 没有这个知识，它假定开始偏移量是 0。如果你子类化 BreakIterator，请将 SimpleTextBoundary 的此函数实现复制到你的子类中。
        // [这应该在这个级别是抽象的，但现在修复已经太晚了。]
        if (offset == 0) {
            return true;
        }
        int boundary = following(offset - 1);
        if (boundary == DONE) {
            throw new IllegalArgumentException();
        }
        return boundary == offset;
    }
}


                /**
     * 返回由 next()、next(int)、previous()、first()、last()、
     * following(int) 或 preceding(int) 最近返回的文本边界字符索引。如果这些方法中的任何一个返回
     * <code>BreakIterator.DONE</code>，因为已经到达第一个或最后一个文本边界，
     * 它将返回第一个或最后一个文本边界，具体取决于到达的是哪一个。
     * @return 从上述方法返回的文本边界，第一个或最后一个文本边界。
     * @see #next()
     * @see #next(int)
     * @see #previous()
     * @see #first()
     * @see #last()
     * @see #following(int)
     * @see #preceding(int)
     */
    public abstract int current();

    /**
     * 获取正在扫描的文本
     * @return 正在扫描的文本
     */
    public abstract CharacterIterator getText();

    /**
     * 设置要扫描的新文本字符串。当前扫描位置将重置为 first()。
     * @param newText 要扫描的新文本。
     */
    public void setText(String newText)
    {
        setText(new StringCharacterIterator(newText));
    }

    /**
     * 设置要扫描的新文本。当前扫描位置将重置为 first()。
     * @param newText 要扫描的新文本。
     */
    public abstract void setText(CharacterIterator newText);

    private static final int CHARACTER_INDEX = 0;
    private static final int WORD_INDEX = 1;
    private static final int LINE_INDEX = 2;
    private static final int SENTENCE_INDEX = 3;

    @SuppressWarnings("unchecked")
    private static final SoftReference<BreakIteratorCache>[] iterCache = (SoftReference<BreakIteratorCache>[]) new SoftReference<?>[4];

    /**
     * 返回一个新的 <code>BreakIterator</code> 实例，用于默认区域设置的单词边界。
     * @return 用于单词边界的断点迭代器
     */
    public static BreakIterator getWordInstance()
    {
        return getWordInstance(Locale.getDefault());
    }

    /**
     * 返回一个新的 <code>BreakIterator</code> 实例，用于给定区域设置的单词边界。
     * @param locale 所需的区域设置
     * @return 用于单词边界的断点迭代器
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     */
    public static BreakIterator getWordInstance(Locale locale)
    {
        return getBreakInstance(locale, WORD_INDEX);
    }

    /**
     * 返回一个新的 <code>BreakIterator</code> 实例，用于默认区域设置的行边界。
     * @return 用于行边界的断点迭代器
     */
    public static BreakIterator getLineInstance()
    {
        return getLineInstance(Locale.getDefault());
    }

    /**
     * 返回一个新的 <code>BreakIterator</code> 实例，用于给定区域设置的行边界。
     * @param locale 所需的区域设置
     * @return 用于行边界的断点迭代器
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     */
    public static BreakIterator getLineInstance(Locale locale)
    {
        return getBreakInstance(locale, LINE_INDEX);
    }

    /**
     * 返回一个新的 <code>BreakIterator</code> 实例，用于默认区域设置的字符边界。
     * @return 用于字符边界的断点迭代器
     */
    public static BreakIterator getCharacterInstance()
    {
        return getCharacterInstance(Locale.getDefault());
    }

    /**
     * 返回一个新的 <code>BreakIterator</code> 实例，用于给定区域设置的字符边界。
     * @param locale 所需的区域设置
     * @return 用于字符边界的断点迭代器
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     */
    public static BreakIterator getCharacterInstance(Locale locale)
    {
        return getBreakInstance(locale, CHARACTER_INDEX);
    }

    /**
     * 返回一个新的 <code>BreakIterator</code> 实例，用于默认区域设置的句子边界。
     * @return 用于句子边界的断点迭代器
     */
    public static BreakIterator getSentenceInstance()
    {
        return getSentenceInstance(Locale.getDefault());
    }

    /**
     * 返回一个新的 <code>BreakIterator</code> 实例，用于给定区域设置的句子边界。
     * @param locale 所需的区域设置
     * @return 用于句子边界的断点迭代器
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     */
    public static BreakIterator getSentenceInstance(Locale locale)
    {
        return getBreakInstance(locale, SENTENCE_INDEX);
    }

    private static BreakIterator getBreakInstance(Locale locale, int type) {
        if (iterCache[type] != null) {
            BreakIteratorCache cache = iterCache[type].get();
            if (cache != null) {
                if (cache.getLocale().equals(locale)) {
                    return cache.createBreakInstance();
                }
            }
        }

        BreakIterator result = createBreakInstance(locale, type);
        BreakIteratorCache cache = new BreakIteratorCache(locale, result);
        iterCache[type] = new SoftReference<>(cache);
        return result;
    }

    private static BreakIterator createBreakInstance(Locale locale,
                                                     int type) {
        LocaleProviderAdapter adapter = LocaleProviderAdapter.getAdapter(BreakIteratorProvider.class, locale);
        BreakIterator iterator = createBreakInstance(adapter, locale, type);
        if (iterator == null) {
            iterator = createBreakInstance(LocaleProviderAdapter.forJRE(), locale, type);
        }
        return iterator;
    }

    private static BreakIterator createBreakInstance(LocaleProviderAdapter adapter, Locale locale, int type) {
        BreakIteratorProvider breakIteratorProvider = adapter.getBreakIteratorProvider();
        BreakIterator iterator = null;
        switch (type) {
        case CHARACTER_INDEX:
            iterator = breakIteratorProvider.getCharacterInstance(locale);
            break;
        case WORD_INDEX:
            iterator = breakIteratorProvider.getWordInstance(locale);
            break;
        case LINE_INDEX:
            iterator = breakIteratorProvider.getLineInstance(locale);
            break;
        case SENTENCE_INDEX:
            iterator = breakIteratorProvider.getSentenceInstance(locale);
            break;
        }
        return iterator;
    }


                /**
     * 返回一个数组，包含此类的 <code>get*Instance</code> 方法可以返回本地化实例的所有区域设置。
     * 返回的数组表示 Java 运行时支持的区域设置和已安装的
     * {@link java.text.spi.BreakIteratorProvider BreakIteratorProvider} 实现支持的区域设置的并集。
     * 它必须至少包含一个等于 {@link java.util.Locale#US Locale.US} 的 <code>Locale</code> 实例。
     *
     * @return 可以获取本地化 <code>BreakIterator</code> 实例的区域设置数组。
     */
    public static synchronized Locale[] getAvailableLocales()
    {
        LocaleServiceProviderPool pool =
            LocaleServiceProviderPool.getPool(BreakIteratorProvider.class);
        return pool.getAvailableLocales();
    }

    private static final class BreakIteratorCache {

        private BreakIterator iter;
        private Locale locale;

        BreakIteratorCache(Locale locale, BreakIterator iter) {
            this.locale = locale;
            this.iter = (BreakIterator) iter.clone();
        }

        Locale getLocale() {
            return locale;
        }

        BreakIterator createBreakInstance() {
            return (BreakIterator) iter.clone();
        }
    }
}
