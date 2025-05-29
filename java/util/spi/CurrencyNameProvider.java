/*
 * 版权所有 (c) 2005, 2012, Oracle 和/或其附属公司。保留所有权利。
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

package java.util.spi;

import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle.Control;

/**
 * 一个抽象类，为 {@link java.util.Currency Currency} 类提供本地化的货币符号和显示名称。
 * 注意，货币符号在确定 {@link java.util.spi.LocaleServiceProvider LocaleServiceProvider}
 * 规范中描述的行为时被视为名称。
 *
 * @since        1.6
 */
public abstract class CurrencyNameProvider extends LocaleServiceProvider {

    /**
     * 唯一的构造函数。通常由子类构造函数隐式调用。
     */
    protected CurrencyNameProvider() {
    }

    /**
     * 获取指定区域设置的给定货币代码的符号。
     * 例如，对于 "USD"（美国美元），如果指定的区域设置是美国，则符号为 "$"，而对于其他区域设置，可能是 "US$"。如果无法确定符号，应返回 null。
     *
     * @param currencyCode ISO 4217 货币代码，由 'A' (U+0041) 和 'Z' (U+005A) 之间的三个大写字母组成
     * @param locale 所需的区域设置
     * @return 指定区域设置下给定货币代码的符号，如果该区域设置下没有可用的符号，则返回 null
     * @exception NullPointerException 如果 <code>currencyCode</code> 或 <code>locale</code> 为 null
     * @exception IllegalArgumentException 如果 <code>currencyCode</code> 不是三个大写字母的形式，或者 <code>locale</code> 不是
     *     {@link java.util.spi.LocaleServiceProvider#getAvailableLocales()
     *     getAvailableLocales()} 返回的区域设置之一。
     * @see java.util.Currency#getSymbol(java.util.Locale)
     */
    public abstract String getSymbol(String currencyCode, Locale locale);

    /**
     * 返回适合显示给用户的货币名称。默认实现返回 null。
     *
     * @param currencyCode ISO 4217 货币代码，由 'A' (U+0041) 和 'Z' (U+005A) 之间的三个大写字母组成
     * @param locale 所需的区域设置
     * @return 适合显示给用户的货币名称，如果该区域设置下没有可用的名称，则返回 null
     * @exception IllegalArgumentException 如果 <code>currencyCode</code> 不是三个大写字母的形式，或者 <code>locale</code> 不是
     *     {@link java.util.spi.LocaleServiceProvider#getAvailableLocales()
     *     getAvailableLocales()} 返回的区域设置之一。
     * @exception NullPointerException 如果 <code>currencyCode</code> 或 <code>locale</code> 为 <code>null</code>
     * @since 1.7
     */
    public String getDisplayName(String currencyCode, Locale locale) {
        if (currencyCode == null || locale == null) {
            throw new NullPointerException();
        }

        // 检查货币代码是否有效
        char[] charray = currencyCode.toCharArray();
        if (charray.length != 3) {
            throw new IllegalArgumentException("货币代码不是三个大写字母的形式。");
        }
        for (char c : charray) {
            if (c < 'A' || c > 'Z') {
                throw new IllegalArgumentException("货币代码不是三个大写字母的形式。");
            }
        }

        // 检查区域设置是否有效
        Control c = Control.getNoFallbackControl(Control.FORMAT_DEFAULT);
        for (Locale l : getAvailableLocales()) {
            if (c.getCandidateLocales("", l).contains(locale)) {
                return null;
            }
        }

        throw new IllegalArgumentException("区域设置不可用");
    }
}
