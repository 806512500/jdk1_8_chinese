
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

/*
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 *
 *   The original version of this source code and documentation is copyrighted
 * and owned by Taligent, Inc., a wholly-owned subsidiary of IBM. These
 * materials are provided under terms of a License Agreement between Taligent
 * and Sun. This technology is protected by multiple US and International
 * patents. This notice and attribution to Taligent may not be removed.
 *   Taligent is a registered trademark of Taligent, Inc.
 *
 */

package java.text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.spi.DecimalFormatSymbolsProvider;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleServiceProviderPool;
import sun.util.locale.provider.ResourceBundleBasedAdapter;

/**
 * 该类表示 <code>DecimalFormat</code> 格式化数字所需的一组符号（如小数分隔符、分组分隔符等）。
 * <code>DecimalFormat</code> 会从其区域设置数据中创建一个 <code>DecimalFormatSymbols</code> 实例。
 * 如果需要更改这些符号，可以从 <code>DecimalFormat</code> 获取 <code>DecimalFormatSymbols</code> 对象并进行修改。
 *
 * @see          java.util.Locale
 * @see          DecimalFormat
 * @author       Mark Davis
 * @author       Alan Liu
 */

public class DecimalFormatSymbols implements Cloneable, Serializable {

    /**
     * 为默认的 {@link java.util.Locale.Category#FORMAT FORMAT} 区域设置创建一个 <code>DecimalFormatSymbols</code> 对象。
     * 该构造函数只能为 Java 运行时环境支持的区域设置创建实例，而不能为已安装的
     * {@link java.text.spi.DecimalFormatSymbolsProvider DecimalFormatSymbolsProvider}
     * 实现支持的区域设置创建实例。要实现完整的区域设置覆盖，请使用
     * {@link #getInstance(Locale) getInstance} 方法。
     * <p>这相当于调用
     * {@link #DecimalFormatSymbols(Locale)
     *     DecimalFormatSymbols(Locale.getDefault(Locale.Category.FORMAT))}。
     * @see java.util.Locale#getDefault(java.util.Locale.Category)
     * @see java.util.Locale.Category#FORMAT
     */
    public DecimalFormatSymbols() {
        initialize( Locale.getDefault(Locale.Category.FORMAT) );
    }

    /**
     * 为给定的区域设置创建一个 <code>DecimalFormatSymbols</code> 对象。
     * 该构造函数只能为 Java 运行时环境支持的区域设置创建实例，而不能为已安装的
     * {@link java.text.spi.DecimalFormatSymbolsProvider DecimalFormatSymbolsProvider}
     * 实现支持的区域设置创建实例。要实现完整的区域设置覆盖，请使用
     * {@link #getInstance(Locale) getInstance} 方法。
     * 如果指定的区域设置包含 {@link java.util.Locale#UNICODE_LOCALE_EXTENSION}
     * 数字系统扩展，则如果 JRE 实现支持该数字系统，实例将使用指定的数字系统进行初始化。例如，
     * <pre>
     * NumberFormat.getNumberInstance(Locale.forLanguageTag("th-TH-u-nu-thai"))
     * </pre>
     * 这可能会返回一个使用泰文数字系统的 {@code NumberFormat} 实例，而不是拉丁数字系统。
     *
     * @param locale 所需的区域设置
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     */
    public DecimalFormatSymbols( Locale locale ) {
        initialize( locale );
    }

    /**
     * 返回该类的 <code>getInstance</code> 方法可以返回本地化实例的所有区域设置。
     * 返回的数组表示 Java 运行时支持的区域设置和已安装的
     * {@link java.text.spi.DecimalFormatSymbolsProvider DecimalFormatSymbolsProvider}
     * 实现支持的区域设置的并集。它必须至少包含一个等于 {@link java.util.Locale#US Locale.US} 的 <code>Locale</code> 实例。
     *
     * @return 可以获得本地化 <code>DecimalFormatSymbols</code> 实例的区域设置数组。
     * @since 1.6
     */
    public static Locale[] getAvailableLocales() {
        LocaleServiceProviderPool pool =
            LocaleServiceProviderPool.getPool(DecimalFormatSymbolsProvider.class);
        return pool.getAvailableLocales();
    }

    /**
     * 获取默认区域设置的 <code>DecimalFormatSymbols</code> 实例。该方法提供了对 Java 运行时本身支持的
     * <code>DecimalFormatSymbols</code> 实例以及已安装的
     * {@link java.text.spi.DecimalFormatSymbolsProvider
     * DecimalFormatSymbolsProvider} 实现支持的 <code>DecimalFormatSymbols</code> 实例的访问。
     * <p>这相当于调用
     * {@link #getInstance(Locale)
     *     getInstance(Locale.getDefault(Locale.Category.FORMAT))}。
     * @see java.util.Locale#getDefault(java.util.Locale.Category)
     * @see java.util.Locale.Category#FORMAT
     * @return 一个 <code>DecimalFormatSymbols</code> 实例。
     * @since 1.6
     */
    public static final DecimalFormatSymbols getInstance() {
        return getInstance(Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * 获取指定区域设置的 <code>DecimalFormatSymbols</code> 实例。该方法提供了对 Java 运行时本身支持的
     * <code>DecimalFormatSymbols</code> 实例以及已安装的
     * {@link java.text.spi.DecimalFormatSymbolsProvider
     * DecimalFormatSymbolsProvider} 实现支持的 <code>DecimalFormatSymbols</code> 实例的访问。
     * 如果指定的区域设置包含 {@link java.util.Locale#UNICODE_LOCALE_EXTENSION}
     * 数字系统扩展，则如果 JRE 实现支持该数字系统，实例将使用指定的数字系统进行初始化。例如，
     * <pre>
     * NumberFormat.getNumberInstance(Locale.forLanguageTag("th-TH-u-nu-thai"))
     * </pre>
     * 这可能会返回一个使用泰文数字系统的 {@code NumberFormat} 实例，而不是拉丁数字系统。
     *
     * @param locale 所需的区域设置。
     * @return 一个 <code>DecimalFormatSymbols</code> 实例。
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     * @since 1.6
     */
    public static final DecimalFormatSymbols getInstance(Locale locale) {
        LocaleProviderAdapter adapter;
        adapter = LocaleProviderAdapter.getAdapter(DecimalFormatSymbolsProvider.class, locale);
        DecimalFormatSymbolsProvider provider = adapter.getDecimalFormatSymbolsProvider();
        DecimalFormatSymbols dfsyms = provider.getInstance(locale);
        if (dfsyms == null) {
            provider = LocaleProviderAdapter.forJRE().getDecimalFormatSymbolsProvider();
            dfsyms = provider.getInstance(locale);
        }
        return dfsyms;
    }

    /**
     * 获取用于零的字符。对于阿拉伯语等语言，这个字符可能不同。
     *
     * @return 用于零的字符
     */
    public char getZeroDigit() {
        return zeroDigit;
    }

    /**
     * 设置用于零的字符。对于阿拉伯语等语言，这个字符可能不同。
     *
     * @param zeroDigit 用于零的字符
     */
    public void setZeroDigit(char zeroDigit) {
        this.zeroDigit = zeroDigit;
    }

    /**
     * 获取用于千位分隔符的字符。对于法语等语言，这个字符可能不同。
     *
     * @return 分组分隔符
     */
    public char getGroupingSeparator() {
        return groupingSeparator;
    }

    /**
     * 设置用于千位分隔符的字符。对于法语等语言，这个字符可能不同。
     *
     * @param groupingSeparator 分组分隔符
     */
    public void setGroupingSeparator(char groupingSeparator) {
        this.groupingSeparator = groupingSeparator;
    }

    /**
     * 获取用于小数点的字符。对于法语等语言，这个字符可能不同。
     *
     * @return 用于小数点的字符
     */
    public char getDecimalSeparator() {
        return decimalSeparator;
    }

    /**
     * 设置用于小数点的字符。对于法语等语言，这个字符可能不同。
     *
     * @param decimalSeparator 用于小数点的字符
     */
    public void setDecimalSeparator(char decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
    }

    /**
     * 获取用于千分之一符号的字符。对于阿拉伯语等语言，这个字符可能不同。
     *
     * @return 用于千分之一符号的字符
     */
    public char getPerMill() {
        return perMill;
    }

    /**
     * 设置用于千分之一符号的字符。对于阿拉伯语等语言，这个字符可能不同。
     *
     * @param perMill 用于千分之一符号的字符
     */
    public void setPerMill(char perMill) {
        this.perMill = perMill;
    }

    /**
     * 获取用于百分号的字符。对于阿拉伯语等语言，这个字符可能不同。
     *
     * @return 用于百分号的字符
     */
    public char getPercent() {
        return percent;
    }

    /**
     * 设置用于百分号的字符。对于阿拉伯语等语言，这个字符可能不同。
     *
     * @param percent 用于百分号的字符
     */
    public void setPercent(char percent) {
        this.percent = percent;
    }

    /**
     * 获取用于模式中数字的字符。
     *
     * @return 用于模式中数字的字符
     */
    public char getDigit() {
        return digit;
    }

    /**
     * 设置用于模式中数字的字符。
     *
     * @param digit 用于模式中数字的字符
     */
    public void setDigit(char digit) {
        this.digit = digit;
    }

    /**
     * 获取用于分隔正负子模式的字符。
     *
     * @return 模式分隔符
     */
    public char getPatternSeparator() {
        return patternSeparator;
    }

    /**
     * 设置用于分隔正负子模式的字符。
     *
     * @param patternSeparator 模式分隔符
     */
    public void setPatternSeparator(char patternSeparator) {
        this.patternSeparator = patternSeparator;
    }

    /**
     * 获取用于表示无穷大的字符串。几乎总是保持不变。
     *
     * @return 表示无穷大的字符串
     */
    public String getInfinity() {
        return infinity;
    }

    /**
     * 设置用于表示无穷大的字符串。几乎总是保持不变。
     *
     * @param infinity 表示无穷大的字符串
     */
    public void setInfinity(String infinity) {
        this.infinity = infinity;
    }

    /**
     * 获取用于表示“非数字”的字符串。几乎总是保持不变。
     *
     * @return 表示“非数字”的字符串
     */
    public String getNaN() {
        return NaN;
    }

    /**
     * 设置用于表示“非数字”的字符串。几乎总是保持不变。
     *
     * @param NaN 表示“非数字”的字符串
     */
    public void setNaN(String NaN) {
        this.NaN = NaN;
    }

    /**
     * 获取用于表示负号的字符。如果没有显式指定负格式，则通过在正格式前加上负号来形成负格式。
     *
     * @return 用于表示负号的字符
     */
    public char getMinusSign() {
        return minusSign;
    }

    /**
     * 设置用于表示负号的字符。如果没有显式指定负格式，则通过在正格式前加上负号来形成负格式。
     *
     * @param minusSign 用于表示负号的字符
     */
    public void setMinusSign(char minusSign) {
        this.minusSign = minusSign;
    }

    /**
     * 返回这些 <code>DecimalFormatSymbols</code> 的区域设置中的货币符号。
     *
     * @return 货币符号
     * @since 1.2
     */
    public String getCurrencySymbol()
    {
        return currencySymbol;
    }

    /**
     * 设置这些 <code>DecimalFormatSymbols</code> 的区域设置中的货币符号。
     *
     * @param currency 货币符号
     * @since 1.2
     */
    public void setCurrencySymbol(String currency)
    {
        currencySymbol = currency;
    }

    /**
     * 返回这些 <code>DecimalFormatSymbols</code> 的 ISO 4217 货币代码。
     *
     * @return 货币代码
     * @since 1.2
     */
    public String getInternationalCurrencySymbol()
    {
        return intlCurrencySymbol;
    }

    /**
     * 设置这些 <code>DecimalFormatSymbols</code> 的 ISO 4217 货币代码。
     * 如果货币代码有效（由
     * {@link java.util.Currency#getInstance(java.lang.String) Currency.getInstance} 定义），
     * 这还将设置货币属性为相应的 Currency 实例，并将货币符号属性设置为货币在 <code>DecimalFormatSymbols</code> 的区域设置中的符号。
     * 如果货币代码无效，则货币属性设置为 null，货币符号属性不修改。
     *
     * @param currencyCode 货币代码
     * @see #setCurrency
     * @see #setCurrencySymbol
     * @since 1.2
     */
    public void setInternationalCurrencySymbol(String currencyCode)
    {
        intlCurrencySymbol = currencyCode;
        currency = null;
        if (currencyCode != null) {
            try {
                currency = Currency.getInstance(currencyCode);
                currencySymbol = currency.getSymbol();
            } catch (IllegalArgumentException e) {
            }
        }
    }

    /**
     * 获取这些 <code>DecimalFormatSymbols</code> 的货币。如果货币符号属性之前设置为一个无效的 ISO 4217 货币代码，则可能为 null。
     *
     * @return 使用的货币，或 null
     * @since 1.4
     */
    public Currency getCurrency() {
        return currency;
    }


                /**
     * 设置这些 DecimalFormatSymbols 的货币。
     * 这也会将货币符号属性设置为 DecimalFormatSymbols 的区域设置中的货币符号，
     * 并将国际货币符号属性设置为货币的 ISO 4217 货币代码。
     *
     * @param currency 要使用的新的货币
     * @exception NullPointerException 如果 <code>currency</code> 为 null
     * @since 1.4
     * @see #setCurrencySymbol
     * @see #setInternationalCurrencySymbol
     */
    public void setCurrency(Currency currency) {
        if (currency == null) {
            throw new NullPointerException();
        }
        this.currency = currency;
        intlCurrencySymbol = currency.getCurrencyCode();
        currencySymbol = currency.getSymbol(locale);
    }


    /**
     * 返回货币小数分隔符。
     *
     * @return 货币小数分隔符
     * @since 1.2
     */
    public char getMonetaryDecimalSeparator()
    {
        return monetarySeparator;
    }

    /**
     * 设置货币小数分隔符。
     *
     * @param sep 货币小数分隔符
     * @since 1.2
     */
    public void setMonetaryDecimalSeparator(char sep)
    {
        monetarySeparator = sep;
    }

    //------------------------------------------------------------
    // BEGIN   Package Private methods ... to be made public later
    //------------------------------------------------------------

    /**
     * 返回用于分隔尾数和指数的字符。
     */
    char getExponentialSymbol()
    {
        return exponential;
    }
  /**
   * 返回用于分隔尾数和指数的字符串。
   * 示例：1.23x10^4 中的 "x10^"，1.23E4 中的 "E"。
   *
   * @return 指数分隔符字符串
   * @see #setExponentSeparator(java.lang.String)
   * @since 1.6
   */
    public String getExponentSeparator()
    {
        return exponentialSeparator;
    }

    /**
     * 设置用于分隔尾数和指数的字符。
     */
    void setExponentialSymbol(char exp)
    {
        exponential = exp;
    }

  /**
   * 设置用于分隔尾数和指数的字符串。
   * 示例：1.23x10^4 中的 "x10^"，1.23E4 中的 "E"。
   *
   * @param exp 指数分隔符字符串
   * @exception NullPointerException 如果 <code>exp</code> 为 null
   * @see #getExponentSeparator()
   * @since 1.6
   */
    public void setExponentSeparator(String exp)
    {
        if (exp == null) {
            throw new NullPointerException();
        }
        exponentialSeparator = exp;
     }


    //------------------------------------------------------------
    // END     Package Private methods ... to be made public later
    //------------------------------------------------------------

    /**
     * 标准重写。
     */
    @Override
    public Object clone() {
        try {
            return (DecimalFormatSymbols)super.clone();
            // 其他字段进行位复制
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /**
     * 重写 equals 方法。
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (getClass() != obj.getClass()) return false;
        DecimalFormatSymbols other = (DecimalFormatSymbols) obj;
        return (zeroDigit == other.zeroDigit &&
        groupingSeparator == other.groupingSeparator &&
        decimalSeparator == other.decimalSeparator &&
        percent == other.percent &&
        perMill == other.perMill &&
        digit == other.digit &&
        minusSign == other.minusSign &&
        patternSeparator == other.patternSeparator &&
        infinity.equals(other.infinity) &&
        NaN.equals(other.NaN) &&
        currencySymbol.equals(other.currencySymbol) &&
        intlCurrencySymbol.equals(other.intlCurrencySymbol) &&
        currency == other.currency &&
        monetarySeparator == other.monetarySeparator &&
        exponentialSeparator.equals(other.exponentialSeparator) &&
        locale.equals(other.locale));
    }

    /**
     * 重写 hashCode 方法。
     */
    @Override
    public int hashCode() {
            int result = zeroDigit;
            result = result * 37 + groupingSeparator;
            result = result * 37 + decimalSeparator;
            return result;
    }

    /**
     * 从 FormatData 资源包中初始化符号。
     */
    private void initialize( Locale locale ) {
        this.locale = locale;

        // 获取资源包数据
        LocaleProviderAdapter adapter = LocaleProviderAdapter.getAdapter(DecimalFormatSymbolsProvider.class, locale);
        // 避免潜在的递归
        if (!(adapter instanceof ResourceBundleBasedAdapter)) {
            adapter = LocaleProviderAdapter.getResourceBundleBased();
        }
        Object[] data = adapter.getLocaleResources(locale).getDecimalFormatSymbolsData();
        String[] numberElements = (String[]) data[0];

        decimalSeparator = numberElements[0].charAt(0);
        groupingSeparator = numberElements[1].charAt(0);
        patternSeparator = numberElements[2].charAt(0);
        percent = numberElements[3].charAt(0);
        zeroDigit = numberElements[4].charAt(0); // 不同于阿拉伯语等
        digit = numberElements[5].charAt(0);
        minusSign = numberElements[6].charAt(0);
        exponential = numberElements[7].charAt(0);
        exponentialSeparator = numberElements[7]; // 字符串表示，自 1.6 版本起
        perMill = numberElements[8].charAt(0);
        infinity  = numberElements[9];
        NaN = numberElements[10];

        // 尝试获取该区域设置国家使用的货币。
        // 单独检查空国家字符串，因为它是有效的
        // 区域设置 ID（用于 C 区域设置），但不是有效的
        // ISO 3166 国家代码，且异常代价高昂。
        if (locale.getCountry().length() > 0) {
            try {
                currency = Currency.getInstance(locale);
            } catch (IllegalArgumentException e) {
                // 使用默认值以保持兼容性
            }
        }
        if (currency != null) {
            intlCurrencySymbol = currency.getCurrencyCode();
            if (data[1] != null && data[1] == intlCurrencySymbol) {
                currencySymbol = (String) data[2];
            } else {
                currencySymbol = currency.getSymbol(locale);
                data[1] = intlCurrencySymbol;
                data[2] = currencySymbol;
            }
        } else {
            // 默认值
            intlCurrencySymbol = "XXX";
            try {
                currency = Currency.getInstance(intlCurrencySymbol);
            } catch (IllegalArgumentException e) {
            }
            currencySymbol = "\u00A4";
        }
        // 目前，所有支持的区域设置中，货币小数分隔符与
        // 标准小数分隔符相同。
        // 如果发生变化，需在 NumberElements 中添加新条目。
        monetarySeparator = decimalSeparator;
    }

    /**
     * 读取默认的可序列化字段，为旧序列版本中的对象提供默认值，
     * 并初始化不可序列化的字段。
     * 如果 <code>serialVersionOnStream</code>
     * 小于 1，将 <code>monetarySeparator</code> 初始化为
     * 与 <code>decimalSeparator</code> 相同，并将 <code>exponential</code>
     * 初始化为 'E'。
     * 如果 <code>serialVersionOnStream</code> 小于 2，
     * 将 <code>locale</code> 初始化为根区域设置，并初始化
     * 如果 <code>serialVersionOnStream</code> 小于 3，它将使用 <code>exponential</code>
     * 初始化 <code>exponentialSeparator</code>。
     * 将 <code>serialVersionOnStream</code> 设置回最大允许值，以便
     * 如果此对象再次流式传输，将正确执行默认序列化。
     * 从 intlCurrencySymbol 字段初始化货币。
     *
     * @since JDK 1.1.6
     */
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (serialVersionOnStream < 1) {
            // 没有 monetarySeparator 或 exponential 字段；
            // 使用默认值。
            monetarySeparator = decimalSeparator;
            exponential       = 'E';
        }
        if (serialVersionOnStream < 2) {
            // 没有 locale；使用根区域设置
            locale = Locale.ROOT;
        }
        if (serialVersionOnStream < 3) {
            // 没有 exponentialSeparator。使用 exponential 创建一个
            exponentialSeparator = Character.toString(exponential);
        }
        serialVersionOnStream = currentSerialVersion;

        if (intlCurrencySymbol != null) {
            try {
                 currency = Currency.getInstance(intlCurrencySymbol);
            } catch (IllegalArgumentException e) {
            }
        }
    }

    /**
     * 用于零的字符。
     *
     * @serial
     * @see #getZeroDigit
     */
    private  char    zeroDigit;

    /**
     * 用于千位分隔符的字符。
     *
     * @serial
     * @see #getGroupingSeparator
     */
    private  char    groupingSeparator;

    /**
     * 用于小数点的字符。
     *
     * @serial
     * @see #getDecimalSeparator
     */
    private  char    decimalSeparator;

    /**
     * 用于千分之一符号的字符。
     *
     * @serial
     * @see #getPerMill
     */
    private  char    perMill;

    /**
     * 用于百分号的字符。
     * @serial
     * @see #getPercent
     */
    private  char    percent;

    /**
     * 用于模式中的数字的字符。
     *
     * @serial
     * @see #getDigit
     */
    private  char    digit;

    /**
     * 用于在模式中分隔正负子模式的字符。
     *
     * @serial
     * @see #getPatternSeparator
     */
    private  char    patternSeparator;

    /**
     * 用于表示无穷大的字符串。
     * @serial
     * @see #getInfinity
     */
    private  String  infinity;

    /**
     * 用于表示“非数字”的字符串。
     * @serial
     * @see #getNaN
     */
    private  String  NaN;

    /**
     * 用于表示负号的字符。
     * @serial
     * @see #getMinusSign
     */
    private  char    minusSign;

    /**
     * 用于表示本地货币的字符串，例如“$”。
     * @serial
     * @see #getCurrencySymbol
     */
    private  String  currencySymbol;

    /**
     * 用于表示本地货币的 ISO 4217 货币代码，例如“USD”。
     * @serial
     * @see #getInternationalCurrencySymbol
     */
    private  String  intlCurrencySymbol;

    /**
     * 用于格式化货币值的小数分隔符。
     * @serial
     * @since JDK 1.1.6
     * @see #getMonetaryDecimalSeparator
     */
    private  char    monetarySeparator; // JDK 1.1.6 中新增的字段

    /**
     * 用于在指数表示法中区分指数的字符，例如“1.23E45”中的“E”。
     * <p>
     * 注意，公共 API 不提供设置此字段的方法，
     * 尽管实现和流格式支持它。意图是在未来将其添加到 API 中。
     *
     * @serial
     * @since JDK 1.1.6
     */
    private  char    exponential;       // JDK 1.1.6 中新增的字段

  /**
   * 用于分隔尾数和指数的字符串。
   * 示例：1.23x10^4 中的 "x10^"，1.23E4 中的 "E"。
   * <p>
   * 如果 <code>exponential</code> 和 <code>exponentialSeparator</code>
   * 都存在，则此 <code>exponentialSeparator</code> 优先。
   *
   * @serial
   * @since 1.6
   */
    private  String    exponentialSeparator;       // JDK 1.6 中新增的字段

    /**
     * 这些货币格式符号的区域设置。
     *
     * @serial
     * @since 1.4
     */
    private Locale locale;

    // 货币；仅序列化 ISO 代码。
    private transient Currency currency;

    // 声明 JDK 1.1 FCS 兼容性
    static final long serialVersionUID = 5772796243397350300L;

    // 内部序列版本，表示写入的版本
    // - 0（默认）：JDK 1.1.5 及之前的版本
    // - 1：JDK 1.1.6 及之后的版本，包含两个新字段：
    //     monetarySeparator 和 exponential。
    // - 2：J2SE 1.4 及之后的版本，包含 locale 字段。
    // - 3：J2SE 1.6 及之后的版本，包含 exponentialSeparator 字段。
    private static final int currentSerialVersion = 3;

    /**
     * 描述流中 <code>DecimalFormatSymbols</code> 的版本。
     * 可能的值为：
     * <ul>
     * <li><b>0</b>（或未初始化）：JDK 1.1.6 之前的版本。
     *
     * <li><b>1</b>：JDK 1.1.6 或之后的版本，包含
     *      两个新字段：<code>monetarySeparator</code> 和 <code>exponential</code>。
     * <li><b>2</b>：J2SE 1.4 或之后的版本，包含
     *      新的 <code>locale</code> 字段。
     * <li><b>3</b>：J2SE 1.6 或之后的版本，包含
     *      新的 <code>exponentialSeparator</code> 字段。
     * </ul>
     * 当流式传输 <code>DecimalFormatSymbols</code> 时，总是写入最新格式
     * （对应于最高的允许 <code>serialVersionOnStream</code>）。
     *
     * @serial
     * @since JDK 1.1.6
     */
    private int serialVersionOnStream = currentSerialVersion;
}
