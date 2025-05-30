/*
 * Copyright (c) 2005, 2012, Oracle and/or its affiliates. All rights reserved.
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

package java.text.spi;

import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

/**
 * 一个抽象类，用于提供
 * {@link java.text.DecimalFormatSymbols DecimalFormatSymbols} 类的实例。
 *
 * <p>请求的 {@code Locale} 可能包含一个 <a
 * href="../../util/Locale.html#def_locale_extension">扩展</a>，用于指定所需的数字系统。例如，{@code "ar-u-nu-arab"}
 * （以 BCP 47 语言标签形式）指定使用阿拉伯-印度数字和符号的阿拉伯语，而 {@code "ar-u-nu-latn"} 指定使用拉丁数字和符号的阿拉伯语。有关数字系统的详细信息，请参阅 <em>Unicode Locale Data Markup
 * Language (LDML)</em> 规范。
 *
 * @since        1.6
 * @see Locale#forLanguageTag(String)
 * @see Locale#getExtension(char)
 */
public abstract class DecimalFormatSymbolsProvider extends LocaleServiceProvider {

    /**
     * 唯一的构造函数。 （通常由子类构造函数隐式调用。）
     */
    protected DecimalFormatSymbolsProvider() {
    }

    /**
     * 返回指定区域设置的 <code>DecimalFormatSymbols</code> 实例。
     *
     * @param locale 所需的区域设置
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     * @exception IllegalArgumentException 如果 <code>locale</code> 不是
     *     {@link java.util.spi.LocaleServiceProvider#getAvailableLocales()
     *     getAvailableLocales()} 返回的区域设置之一。
     * @return 一个 <code>DecimalFormatSymbols</code> 实例。
     * @see java.text.DecimalFormatSymbols#getInstance(java.util.Locale)
     */
    public abstract DecimalFormatSymbols getInstance(Locale locale);
}
