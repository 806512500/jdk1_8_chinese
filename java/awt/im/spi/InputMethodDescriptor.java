/*
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
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


package java.awt.im.spi;

import java.awt.AWTException;
import java.awt.Image;
import java.util.Locale;

/**
 * 定义了提供有关输入方法的足够信息的方法，以便选择和加载该输入方法。
 * 输入方法本身仅在实际使用时加载。
 *
 * @since 1.3
 */

public interface InputMethodDescriptor {

    /**
     * 返回对应输入方法支持的区域设置。
     * 区域设置可能仅描述语言，或者在需要时也可以包括国家和变体信息。
     * 该信息用于按区域设置选择输入方法
     * ({@link java.awt.im.InputContext#selectInputMethod(Locale)})。它也可以用于按区域设置对用户可见的
     * 输入方法列表进行排序。
     * <p>
     * 只应返回输入方法的主要区域设置。
     * 例如，如果一个日语输入方法还具有罗马字符的直通模式，通常仍然只返回日语。因此，返回的区域设置列表通常是
     * 对应输入方法的实现中 {@link java.awt.im.spi.InputMethod#setLocale} 返回 true 的区域设置的子集。
     * <p>
     * 如果 {@link #hasDynamicLocaleList} 返回 true，则每次需要该信息时都会调用此方法。这
     * 为依赖网络资源的输入方法提供了在资源可用或不可用时添加或删除区域设置的机会。
     *
     * @return 输入方法支持的区域设置
     * @exception AWTException 如果可以确定输入方法无法运行，例如，因为安装不完整。
     */
    Locale[] getAvailableLocales() throws AWTException;

    /**
     * 返回可用区域设置列表是否可以在运行时更改。
     * 例如，对于通过网络访问实际输入方法的适配器，可能会出现这种情况。
     */
    boolean hasDynamicLocaleList();

    /**
     * 返回对应输入方法的用户可见名称，该名称以指定的显示语言显示。
     * <p>
     * inputLocale 参数指定输入文本的区域设置。
     * 此参数只能取从此描述符的 {@link #getAvailableLocales} 方法或 null。如果它是 null，则应返回
     * 与输入区域设置无关的输入方法名称。
     * <p>
     * 如果没有可用的所需显示语言的名称，该方法可以回退到其他语言。
     *
     * @param inputLocale 支持文本输入的区域设置，或 null
     * @param displayLanguage 名称将显示的语言
     */
    String getInputMethodDisplayName(Locale inputLocale, Locale displayLanguage);

    /**
     * 返回对应输入方法的图标。
     * 该图标可用于用户界面中选择输入方法。
     * <p>
     * inputLocale 参数指定输入文本的区域设置。
     * 此参数只能取从此描述符的 {@link #getAvailableLocales} 方法或 null。如果它是 null，则应返回
     * 与输入区域设置无关的输入方法图标。
     * <p>
     * 图标的大小应为 16×16 像素。
     *
     * @param inputLocale 支持文本输入的区域设置，或 null
     * @return 对应输入方法的图标，或 null
     */
    Image getInputMethodIcon(Locale inputLocale);

    /**
     * 创建对应输入方法的新实例。
     *
     * @return 对应输入方法的新实例
     * @exception Exception 创建输入方法实例时可能发生的任何异常
     */
    InputMethod createInputMethod() throws Exception;
}
