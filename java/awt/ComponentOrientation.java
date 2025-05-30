/*
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
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
 * (C) Copyright IBM Corp. 1998 - All Rights Reserved
 *
 * The original version of this source code and documentation is copyrighted
 * and owned by IBM, Inc. These materials are provided under terms of a
 * License Agreement between IBM and Sun. This technology is protected by
 * multiple US and International patents. This notice and attribution to IBM
 * may not be removed.
 *
 */

package java.awt;

import java.util.Locale;
import java.util.ResourceBundle;

/**
  * ComponentOrientation 类封装了用于组件或文本元素排序的语言敏感方向。它用于反映西方字母、中东（如希伯来语）和远东（如日语）之间在排序上的差异。
  * <p>
  * 从根本上说，这管理了以行排列的项目（如字符），以及这些行在块中的排列。这也适用于小部件中的项目：例如，在复选框中，框相对于文本的位置。
  * <p>
  * 现代语言中使用了四种不同的方向，如下表所示。<br>
  * <pre>
  * LT          RT          TL          TR
  * A B C       C B A       A D G       G D A
  * D E F       F E D       B E H       H E B
  * G H I       I H G       C F I       I F C
  * </pre><br>
  * （在表头中，两个字母的缩写表示项目的方向在第一个字母，行的方向在第二个字母。例如，LT 表示“项目从左到右，行从上到下”，TL 表示“项目从上到下，行从左到右”，依此类推。）
  * <p>
  * 方向如下：
  * <ul>
  * <li>LT - 西欧（可选用于日语、中文、韩语）
  * <li>RT - 中东（阿拉伯语、希伯来语）
  * <li>TR - 日语、中文、韩语
  * <li>TL - 蒙古语
  * </ul>
  * 依赖方向的组件的视图和控制器代码应使用 <code>isLeftToRight()</code> 和 <code>isHorizontal()</code> 方法来确定其行为。不应包含以常量为键的类似开关的代码，例如：
  * <pre>
  * if (orientation == LEFT_TO_RIGHT) {
  *   ...
  * } else if (orientation == RIGHT_TO_LEFT) {
  *   ...
  * } else {
  *   // 哦，糟糕
  * }
  * </pre>
  * 这是不安全的，因为将来可能会添加更多的常量，并且不能保证方向对象是唯一的。
  */
public final class ComponentOrientation implements java.io.Serializable
{
    /*
     * 序列化版本ID
     */
    private static final long serialVersionUID = -4113291392143563828L;

    // 实现中使用的内部常量
    private static final int UNK_BIT      = 1;
    private static final int HORIZ_BIT    = 2;
    private static final int LTR_BIT      = 4;

    /**
     * 项目从左到右，行从上到下
     * 示例：英语、法语。
     */
    public static final ComponentOrientation LEFT_TO_RIGHT =
                    new ComponentOrientation(HORIZ_BIT|LTR_BIT);

    /**
     * 项目从右到左，行从上到下
     * 示例：阿拉伯语、希伯来语。
     */
    public static final ComponentOrientation RIGHT_TO_LEFT =
                    new ComponentOrientation(HORIZ_BIT);

    /**
     * 表示组件的方向未设置。
     * 为了保持现有应用程序的行为，isLeftToRight 将为该值返回 true。
     */
    public static final ComponentOrientation UNKNOWN =
                    new ComponentOrientation(HORIZ_BIT|LTR_BIT|UNK_BIT);

    /**
     * 行是否水平？
     * 对于水平、从左到右的书写系统（如罗马字母），这将返回 true。
     */
    public boolean isHorizontal() {
        return (orientation & HORIZ_BIT) != 0;
    }

    /**
     * 水平行：项目是否从左到右？<br>
     * 垂直线：行是否从左到右？<br>
     * 对于水平、从左到右的书写系统（如罗马字母），这将返回 true。
     */
    public boolean isLeftToRight() {
        return (orientation & LTR_BIT) != 0;
    }

    /**
     * 返回给定区域设置的适当方向。
     * @param locale 指定的区域设置
     */
    public static ComponentOrientation getOrientation(Locale locale) {
        // 更灵活的实现会从 ResourceBundle 中查找适当的方向。然而，在引入可插拔的区域设置之前，
        // 这种灵活性并不是真正需要的。因此，我们选择效率。
        String lang = locale.getLanguage();
        if( "iw".equals(lang) || "ar".equals(lang)
            || "fa".equals(lang) || "ur".equals(lang) )
        {
            return RIGHT_TO_LEFT;
        } else {
            return LEFT_TO_RIGHT;
        }
    }

    /**
     * 返回给定 ResourceBundle 的本地化方向。尝试以下三种方法，按顺序：
     * <ol>
     * <li>使用字符串 "Orientation" 作为键从 ResourceBundle 中检索 ComponentOrientation 对象。
     * <li>使用 ResourceBundle.getLocale 确定包的区域设置，然后返回该区域设置的方向。
     * <li>返回默认区域设置的方向。
     * </ol>
     *
     * @deprecated 从 J2SE 1.4 开始，使用 {@link #getOrientation(java.util.Locale)}。
     */
    @Deprecated
    public static ComponentOrientation getOrientation(ResourceBundle bdl)
    {
        ComponentOrientation result = null;

        try {
            result = (ComponentOrientation)bdl.getObject("Orientation");
        }
        catch (Exception e) {
        }

        if (result == null) {
            result = getOrientation(bdl.getLocale());
        }
        if (result == null) {
            result = getOrientation(Locale.getDefault());
        }
        return result;
    }

    private int orientation;

    private ComponentOrientation(int value)
    {
        orientation = value;
    }
}
