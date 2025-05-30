/*
 * Copyright (c) 2005, 2011, Oracle and/or its affiliates. All rights reserved.
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

package java.util.spi;

import java.util.Locale;

/**
 * 一个抽象类，为 {@link java.util.Locale Locale} 类提供本地化名称。
 *
 * @since        1.6
 */
public abstract class LocaleNameProvider extends LocaleServiceProvider {

    /**
     * 唯一的构造函数。通常由子类构造函数隐式调用。
     */
    protected LocaleNameProvider() {
    }

    /**
     * 返回给定的 <a href="http://www.rfc-editor.org/rfc/bcp/bcp47.txt">
     * IETF BCP47</a> 语言代码和给定区域设置的本地化名称，适合显示给用户。
     * 例如，如果 <code>languageCode</code> 是 "fr" 且 <code>locale</code>
     * 是 en_US，getDisplayLanguage() 将返回 "French"；如果 <code>languageCode</code>
     * 是 "en" 且 <code>locale</code> 是 fr_FR，getDisplayLanguage() 将返回 "anglais"。
     * 如果返回的名称无法根据 <code>locale</code> 进行本地化，
     * （例如，提供者没有克罗地亚语的日语名称），
     * 此方法返回 null。
     * @param languageCode 形式为两到八个介于 'a' (U+0061) 和 'z' (U+007A) 之间的小写字母的字符串
     * @param locale 所需的区域设置
     * @return 给定语言代码在指定区域设置下的名称，或如果不可用则返回 null。
     * @exception NullPointerException 如果 <code>languageCode</code> 或 <code>locale</code> 为 null
     * @exception IllegalArgumentException 如果 <code>languageCode</code> 不是两个或三个小写字母的形式，或 <code>locale</code> 不是
     *     {@link java.util.spi.LocaleServiceProvider#getAvailableLocales()
     *     getAvailableLocales()} 返回的区域设置之一。
     * @see java.util.Locale#getDisplayLanguage(java.util.Locale)
     */
    public abstract String getDisplayLanguage(String languageCode, Locale locale);

    /**
     * 返回给定的 <a href="http://www.rfc-editor.org/rfc/bcp/bcp47.txt">
     * IETF BCP47</a> 脚本代码和给定区域设置的本地化名称，适合显示给用户。
     * 例如，如果 <code>scriptCode</code> 是 "Latn" 且 <code>locale</code>
     * 是 en_US，getDisplayScript() 将返回 "Latin"；如果 <code>scriptCode</code>
     * 是 "Cyrl" 且 <code>locale</code> 是 fr_FR，getDisplayScript() 将返回 "cyrillique"。
     * 如果返回的名称无法根据 <code>locale</code> 进行本地化，
     * （例如，提供者没有西里尔文的日语名称），
     * 此方法返回 null。默认实现返回 null。
     * @param scriptCode 形式为四个首字母大写（介于 'A' (U+0041) 和 'Z' (U+005A) 之间）后跟三个小写字母（介于 'a' (U+0061)
     *     和 'z' (U+007A) 之间）的字符串。
     * @param locale 所需的区域设置
     * @return 给定脚本代码在指定区域设置下的名称，或如果不可用则返回 null。
     * @exception NullPointerException 如果 <code>scriptCode</code> 或 <code>locale</code> 为 null
     * @exception IllegalArgumentException 如果 <code>scriptCode</code> 不是四个首字母大写的形式，或 <code>locale</code> 不是
     *     {@link java.util.spi.LocaleServiceProvider#getAvailableLocales()
     *     getAvailableLocales()} 返回的区域设置之一。
     * @see java.util.Locale#getDisplayScript(java.util.Locale)
     * @since 1.7
     */
    public String getDisplayScript(String scriptCode, Locale locale) {
        return null;
    }

    /**
     * 返回给定的 <a href="http://www.rfc-editor.org/rfc/bcp/bcp47.txt">
     * IETF BCP47</a> 区域代码（ISO 3166 国家代码或 UN M.49 区域代码）和给定区域设置的本地化名称，适合显示给用户。
     * 例如，如果 <code>countryCode</code> 是 "FR" 且 <code>locale</code>
     * 是 en_US，getDisplayCountry() 将返回 "France"；如果 <code>countryCode</code>
     * 是 "US" 且 <code>locale</code> 是 fr_FR，getDisplayCountry() 将返回 "Etats-Unis"。
     * 如果返回的名称无法根据 <code>locale</code> 进行本地化，
     * （例如，提供者没有克罗地亚的日语名称），
     * 此方法返回 null。
     * @param countryCode 形式为两个大写字母（介于 'A' (U+0041) 和 'Z' (U+005A) 之间）或三个数字（介于 '0' (U+0030) 和 '9' (U+0039) 之间）的字符串。
     * @param locale 所需的区域设置
     * @return 给定国家代码在指定区域设置下的名称，或如果不可用则返回 null。
     * @exception NullPointerException 如果 <code>countryCode</code> 或 <code>locale</code> 为 null
     * @exception IllegalArgumentException 如果 <code>countryCode</code> 不是两个大写字母或三个数字的形式，或 <code>locale</code> 不是
     *     {@link java.util.spi.LocaleServiceProvider#getAvailableLocales()
     *     getAvailableLocales()} 返回的区域设置之一。
     * @see java.util.Locale#getDisplayCountry(java.util.Locale)
     */
    public abstract String getDisplayCountry(String countryCode, Locale locale);

    /**
     * 返回给定变体代码和给定区域设置的本地化名称，适合显示给用户。
     * 如果返回的名称无法根据 <code>locale</code> 进行本地化，
     * 此方法返回 null。
     * @param variant 变体字符串
     * @param locale 所需的区域设置
     * @return 给定变体字符串在指定区域设置下的名称，或如果不可用则返回 null。
     * @exception NullPointerException 如果 <code>variant</code> 或 <code>locale</code> 为 null
     * @exception IllegalArgumentException 如果 <code>locale</code> 不是
     *     {@link java.util.spi.LocaleServiceProvider#getAvailableLocales()
     *     getAvailableLocales()} 返回的区域设置之一。
     * @see java.util.Locale#getDisplayVariant(java.util.Locale)
     */
    public abstract String getDisplayVariant(String variant, Locale locale);
}
