/*
 * Copyright (c) 2012, 2015, Oracle and/or its affiliates. All rights reserved.
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
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Copyright (c) 2008-2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package java.time.format;

import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 用于日期和时间格式化的本地化小数样式。
 * <p>
 * 处理日期和时间的一个重要部分是本地化。
 * 此类作为访问这些信息的中心点。
 *
 * @implSpec
 * 此类是不可变的且线程安全的。
 *
 * @since 1.8
 */
public final class DecimalStyle {

    /**
     * 标准的非本地化小数样式符号集。
     * <p>
     * 此集使用标准的 ASCII 字符表示零、正号、负号和小数点。
     */
    public static final DecimalStyle STANDARD = new DecimalStyle('0', '+', '-', '.');
    /**
     * DecimalStyle 实例的缓存。
     */
    private static final ConcurrentMap<Locale, DecimalStyle> CACHE = new ConcurrentHashMap<>(16, 0.75f, 2);

    /**
     * 零数字。
     */
    private final char zeroDigit;
    /**
     * 正号。
     */
    private final char positiveSign;
    /**
     * 负号。
     */
    private final char negativeSign;
    /**
     * 小数分隔符。
     */
    private final char decimalSeparator;

    //-----------------------------------------------------------------------
    /**
     * 列出所有支持的区域设置。
     * <p>
     * 区域设置 'en_US' 将始终存在。
     *
     * @return 一个包含支持本地化的区域设置的 Set
     */
    public static Set<Locale> getAvailableLocales() {
        Locale[] l = DecimalFormatSymbols.getAvailableLocales();
        Set<Locale> locales = new HashSet<>(l.length);
        Collections.addAll(locales, l);
        return locales;
    }

    /**
     * 获取默认
     * {@link java.util.Locale.Category#FORMAT FORMAT} 区域设置的 DecimalStyle。
     * <p>
     * 此方法提供对本地敏感的小数样式符号的访问。
     * <p>
     * 这相当于调用
     * {@link #of(Locale)
     *     of(Locale.getDefault(Locale.Category.FORMAT))}。
     *
     * @see java.util.Locale.Category#FORMAT
     * @return 小数样式，不为空
     */
    public static DecimalStyle ofDefaultLocale() {
        return of(Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * 获取指定区域设置的 DecimalStyle。
     * <p>
     * 此方法提供对本地敏感的小数样式符号的访问。
     *
     * @param locale  区域设置，不为空
     * @return 小数样式，不为空
     */
    public static DecimalStyle of(Locale locale) {
        Objects.requireNonNull(locale, "locale");
        DecimalStyle info = CACHE.get(locale);
        if (info == null) {
            info = create(locale);
            CACHE.putIfAbsent(locale, info);
            info = CACHE.get(locale);
        }
        return info;
    }

    private static DecimalStyle create(Locale locale) {
        DecimalFormatSymbols oldSymbols = DecimalFormatSymbols.getInstance(locale);
        char zeroDigit = oldSymbols.getZeroDigit();
        char positiveSign = '+';
        char negativeSign = oldSymbols.getMinusSign();
        char decimalSeparator = oldSymbols.getDecimalSeparator();
        if (zeroDigit == '0' && negativeSign == '-' && decimalSeparator == '.') {
            return STANDARD;
        }
        return new DecimalStyle(zeroDigit, positiveSign, negativeSign, decimalSeparator);
    }

    //-----------------------------------------------------------------------
    /**
     * 受限构造函数。
     *
     * @param zeroChar  用于表示零的字符
     * @param positiveSignChar  用于表示正号的字符
     * @param negativeSignChar  用于表示负号的字符
     * @param decimalPointChar  用于表示小数点的字符
     */
    private DecimalStyle(char zeroChar, char positiveSignChar, char negativeSignChar, char decimalPointChar) {
        this.zeroDigit = zeroChar;
        this.positiveSign = positiveSignChar;
        this.negativeSign = negativeSignChar;
        this.decimalSeparator = decimalPointChar;
    }

    //-----------------------------------------------------------------------
    /**
     * 获取表示零的字符。
     * <p>
     * 用于表示数字的字符可能因文化而异。
     * 此方法指定要使用的零字符，这隐含了从一到九的字符。
     *
     * @return 零字符
     */
    public char getZeroDigit() {
        return zeroDigit;
    }

    /**
     * 返回一个带有新零字符的副本。
     * <p>
     * 用于表示数字的字符可能因文化而异。
     * 此方法指定要使用的零字符，这隐含了从一到九的字符。
     *
     * @param zeroDigit  零字符
     * @return 一个带有新零字符的副本，不为空
     */
    public DecimalStyle withZeroDigit(char zeroDigit) {
        if (zeroDigit == this.zeroDigit) {
            return this;
        }
        return new DecimalStyle(zeroDigit, positiveSign, negativeSign, decimalSeparator);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取表示正号的字符。
     * <p>
     * 用于表示正数的字符可能因文化而异。
     * 此方法指定要使用的字符。
     *
     * @return 正号字符
     */
    public char getPositiveSign() {
        return positiveSign;
    }

    /**
     * 返回一个带有新正号字符的副本。
     * <p>
     * 用于表示正数的字符可能因文化而异。
     * 此方法指定要使用的字符。
     *
     * @param positiveSign  正号字符
     * @return 一个带有新正号字符的副本，不为空
     */
    public DecimalStyle withPositiveSign(char positiveSign) {
        if (positiveSign == this.positiveSign) {
            return this;
        }
        return new DecimalStyle(zeroDigit, positiveSign, negativeSign, decimalSeparator);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取表示负号的字符。
     * <p>
     * 用于表示负数的字符可能因文化而异。
     * 此方法指定要使用的字符。
     *
     * @return 负号字符
     */
    public char getNegativeSign() {
        return negativeSign;
    }

    /**
     * 返回一个带有新负号字符的副本。
     * <p>
     * 用于表示负数的字符可能因文化而异。
     * 此方法指定要使用的字符。
     *
     * @param negativeSign  负号字符
     * @return 一个带有新负号字符的副本，不为空
     */
    public DecimalStyle withNegativeSign(char negativeSign) {
        if (negativeSign == this.negativeSign) {
            return this;
        }
        return new DecimalStyle(zeroDigit, positiveSign, negativeSign, decimalSeparator);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取表示小数点的字符。
     * <p>
     * 用于表示小数点的字符可能因文化而异。
     * 此方法指定要使用的字符。
     *
     * @return 小数点字符
     */
    public char getDecimalSeparator() {
        return decimalSeparator;
    }

    /**
     * 返回一个带有新小数点字符的副本。
     * <p>
     * 用于表示小数点的字符可能因文化而异。
     * 此方法指定要使用的字符。
     *
     * @param decimalSeparator  小数点字符
     * @return 一个带有新小数点字符的副本，不为空
     */
    public DecimalStyle withDecimalSeparator(char decimalSeparator) {
        if (decimalSeparator == this.decimalSeparator) {
            return this;
        }
        return new DecimalStyle(zeroDigit, positiveSign, negativeSign, decimalSeparator);
    }

    //-----------------------------------------------------------------------
    /**
     * 检查字符是否为数字，基于当前设置的零字符。
     *
     * @param ch  要检查的字符
     * @return 字符的值，0 到 9，如果不是数字则为 -1
     */
    int convertToDigit(char ch) {
        int val = ch - zeroDigit;
        return (val >= 0 && val <= 9) ? val : -1;
    }

    /**
     * 使用零字符将输入的数字文本转换为国际化形式。
     *
     * @param numericText  要转换的文本，由 0 到 9 的数字组成，不为空
     * @return 国际化文本，不为空
     */
    String convertNumberToI18N(String numericText) {
        if (zeroDigit == '0') {
            return numericText;
        }
        int diff = zeroDigit - '0';
        char[] array = numericText.toCharArray();
        for (int i = 0; i < array.length; i++) {
            array[i] = (char) (array[i] + diff);
        }
        return new String(array);
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此 DecimalStyle 是否等于另一个 DecimalStyle。
     *
     * @param obj  要检查的对象，null 返回 false
     * @return 如果此对象等于其他对象，则返回 true
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DecimalStyle) {
            DecimalStyle other = (DecimalStyle) obj;
            return (zeroDigit == other.zeroDigit && positiveSign == other.positiveSign &&
                    negativeSign == other.negativeSign && decimalSeparator == other.decimalSeparator);
        }
        return false;
    }

    /**
     * 此 DecimalStyle 的哈希码。
     *
     * @return 一个合适的哈希码
     */
    @Override
    public int hashCode() {
        return zeroDigit + positiveSign + negativeSign + decimalSeparator;
    }

    //-----------------------------------------------------------------------
    /**
     * 返回描述此 DecimalStyle 的字符串。
     *
     * @return 字符串描述，不为空
     */
    @Override
    public String toString() {
        return "DecimalStyle[" + zeroDigit + positiveSign + negativeSign + decimalSeparator + "]";
    }

}
