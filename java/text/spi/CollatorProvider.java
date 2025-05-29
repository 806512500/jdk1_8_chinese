/*
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

import java.text.Collator;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

/**
 * 一个抽象类，用于提供 {@link java.text.Collator Collator} 类的具体实现。
 *
 * @since        1.6
 */
public abstract class CollatorProvider extends LocaleServiceProvider {

    /**
     * 唯一的构造函数。通常由子类构造函数隐式调用。
     */
    protected CollatorProvider() {
    }

    /**
     * 返回指定区域设置的新的 <code>Collator</code> 实例。
     * @param locale 所需的区域设置。
     * @return 所需区域设置的 <code>Collator</code>。
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     * @exception IllegalArgumentException 如果 <code>locale</code> 不是
     *     从 {@link java.util.spi.LocaleServiceProvider#getAvailableLocales()
     *     getAvailableLocales()} 返回的区域设置之一。
     * @see java.text.Collator#getInstance(java.util.Locale)
     */
    public abstract Collator getInstance(Locale locale);
}
