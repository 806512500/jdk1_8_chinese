/*
 * 版权所有 (c) 2005, Oracle 和/或其附属公司。保留所有权利。
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

package java.text.spi;

import java.text.BreakIterator;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

/**
 * 一个抽象类，用于提供 {@link java.text.BreakIterator BreakIterator} 类的具体实现。
 *
 * @since        1.6
 */
public abstract class BreakIteratorProvider extends LocaleServiceProvider {

    /**
     * 唯一的构造函数。 （通常由子类构造函数隐式调用。）
     */
    protected BreakIteratorProvider() {
    }

    /**
     * 返回给定区域设置的<a href="../BreakIterator.html#word">单词分隔</a>的新的 <code>BreakIterator</code> 实例。
     * @param locale 所需的区域设置
     * @return 用于单词分隔的分隔迭代器
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     * @exception IllegalArgumentException 如果 <code>locale</code> 不是
     *     {@link java.util.spi.LocaleServiceProvider#getAvailableLocales()
     *     getAvailableLocales()} 返回的区域设置之一。
     * @see java.text.BreakIterator#getWordInstance(java.util.Locale)
     */
    public abstract BreakIterator getWordInstance(Locale locale);

    /**
     * 返回给定区域设置的<a href="../BreakIterator.html#line">行分隔</a>的新的 <code>BreakIterator</code> 实例。
     * @param locale 所需的区域设置
     * @return 用于行分隔的分隔迭代器
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     * @exception IllegalArgumentException 如果 <code>locale</code> 不是
     *     {@link java.util.spi.LocaleServiceProvider#getAvailableLocales()
     *     getAvailableLocales()} 返回的区域设置之一。
     * @see java.text.BreakIterator#getLineInstance(java.util.Locale)
     */
    public abstract BreakIterator getLineInstance(Locale locale);

    /**
     * 返回给定区域设置的<a href="../BreakIterator.html#character">字符分隔</a>的新的 <code>BreakIterator</code> 实例。
     * @param locale 所需的区域设置
     * @return 用于字符分隔的分隔迭代器
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     * @exception IllegalArgumentException 如果 <code>locale</code> 不是
     *     {@link java.util.spi.LocaleServiceProvider#getAvailableLocales()
     *     getAvailableLocales()} 返回的区域设置之一。
     * @see java.text.BreakIterator#getCharacterInstance(java.util.Locale)
     */
    public abstract BreakIterator getCharacterInstance(Locale locale);

    /**
     * 返回给定区域设置的<a href="../BreakIterator.html#sentence">句子分隔</a>的新的 <code>BreakIterator</code> 实例。
     * @param locale 所需的区域设置
     * @return 用于句子分隔的分隔迭代器
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     * @exception IllegalArgumentException 如果 <code>locale</code> 不是
     *     {@link java.util.spi.LocaleServiceProvider#getAvailableLocales()
     *     getAvailableLocales()} 返回的区域设置之一。
     * @see java.text.BreakIterator#getSentenceInstance(java.util.Locale)
     */
    public abstract BreakIterator getSentenceInstance(Locale locale);
}
