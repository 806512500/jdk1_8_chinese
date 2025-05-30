
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

import java.io.Serializable;

/**
 * <code>Format</code> 是一个用于格式化和解析与区域设置相关的信息（如日期、消息和数字）的抽象基类。
 *
 * <p>
 * <code>Format</code> 定义了将区域设置敏感的对象格式化为 <code>String</code>（<code>format</code> 方法）和将 <code>String</code> 解析回对象（<code>parseObject</code> 方法）的编程接口。
 *
 * <p>
 * 通常，格式的 <code>parseObject</code> 方法必须能够解析由其 <code>format</code> 方法格式化的任何字符串。但是，可能会有特殊情况，这不可能实现。例如，<code>format</code> 方法可能会创建两个相邻的整数数字，中间没有分隔符，在这种情况下，<code>parseObject</code> 无法判断哪些数字属于哪个数字。
 *
 * <h3>子类化</h3>
 *
 * <p>
 * Java 平台提供了三个 <code>Format</code> 的专用子类——<code>DateFormat</code>、<code>MessageFormat</code> 和 <code>NumberFormat</code>——分别用于格式化日期、消息和数字。
 * <p>
 * 具体子类必须实现以下三个方法：
 * <ol>
 * <li> <code>format(Object obj, StringBuffer toAppendTo, FieldPosition pos)</code>
 * <li> <code>formatToCharacterIterator(Object obj)</code>
 * <li> <code>parseObject(String source, ParsePosition pos)</code>
 * </ol>
 * 这些通用方法允许对象的多态解析和格式化，例如，由 <code>MessageFormat</code> 使用。
 * 子类通常还提供特定输入类型的额外 <code>format</code> 方法以及特定结果类型的 <code>parse</code> 方法。任何不带 <code>ParsePosition</code> 参数的 <code>parse</code> 方法在输入文本开头没有所需格式的文本时应抛出 <code>ParseException</code>。
 *
 * <p>
 * 大多数子类还将实现以下工厂方法：
 * <ol>
 * <li>
 * <code>getInstance</code> 用于获取适用于当前区域设置的有用格式对象
 * <li>
 * <code>getInstance(Locale)</code> 用于获取适用于指定区域设置的有用格式对象
 * </ol>
 * 此外，某些子类可能还实现其他 <code>getXxxxInstance</code> 方法以进行更专业的控制。例如，<code>NumberFormat</code> 类提供了 <code>getPercentInstance</code> 和 <code>getCurrencyInstance</code> 方法以获取专门的数字格式化器。
 *
 * <p>
 * 允许程序员为区域设置创建对象（例如，使用 <code>getInstance(Locale)</code>）的 <code>Format</code> 子类还必须实现以下类方法：
 * <blockquote>
 * <pre>
 * public static Locale[] getAvailableLocales()
 * </pre>
 * </blockquote>
 *
 * <p>
 * 最后，子类可以定义一组常量来标识格式化输出中的各个字段。这些常量用于创建一个 <code>FieldPosition</code> 对象，该对象标识字段中包含的信息及其在格式化结果中的位置。这些常量应命名为 <code><em>item</em>_FIELD</code>，其中 <code><em>item</em></code> 标识字段。例如，参见 <code>ERA_FIELD</code> 及其在 {@link DateFormat} 中的同类。
 *
 * <h4><a name="synchronization">同步</a></h4>
 *
 * <p>
 * 格式化器通常不是同步的。
 * 建议为每个线程创建单独的格式化器实例。
 * 如果多个线程同时访问格式化器，必须从外部进行同步。
 *
 * @see          java.text.ParsePosition
 * @see          java.text.FieldPosition
 * @see          java.text.NumberFormat
 * @see          java.text.DateFormat
 * @see          java.text.MessageFormat
 * @author       Mark Davis
 */
public abstract class Format implements Serializable, Cloneable {

    private static final long serialVersionUID = -299282585814624189L;

    /**
     * 唯一的构造函数。通常由子类构造函数隐式调用。
     */
    protected Format() {
    }

    /**
     * 格式化一个对象以生成字符串。这等同于
     * <blockquote>
     * {@link #format(Object, StringBuffer, FieldPosition) format}<code>(obj,
     *         new StringBuffer(), new FieldPosition(0)).toString();</code>
     * </blockquote>
     *
     * @param obj    要格式化的对象
     * @return       格式化的字符串。
     * @exception IllegalArgumentException 如果格式化器无法格式化给定的对象
     */
    public final String format (Object obj) {
        return format(obj, new StringBuffer(), new FieldPosition(0)).toString();
    }

    /**
     * 格式化一个对象并将结果文本追加到给定的字符串缓冲区。
     * 如果 <code>pos</code> 参数标识格式使用的字段，则其索引将设置为遇到的第一个此类字段的开始和结束位置。
     *
     * @param obj    要格式化的对象
     * @param toAppendTo    要追加文本的位置
     * @param pos    一个 <code>FieldPosition</code>，标识格式化文本中的字段
     * @return       作为 <code>toAppendTo</code> 传递的字符串缓冲区，带有追加的格式化文本
     * @exception NullPointerException 如果 <code>toAppendTo</code> 或 <code>pos</code> 为 null
     * @exception IllegalArgumentException 如果格式化器无法格式化给定的对象
     */
    public abstract StringBuffer format(Object obj,
                    StringBuffer toAppendTo,
                    FieldPosition pos);

    /**
     * 格式化一个对象以生成一个 <code>AttributedCharacterIterator</code>。
     * 可以使用返回的 <code>AttributedCharacterIterator</code> 构建结果字符串，以及确定结果字符串的信息。
     * <p>
     * <code>AttributedCharacterIterator</code> 的每个属性键都是 <code>Field</code> 类型。每个 <code>Format</code> 实现应定义 <code>AttributedCharacterIterator</code> 中每个属性的合法值，但通常属性键也被用作属性值。
     * <p>默认实现创建一个没有属性的 <code>AttributedCharacterIterator</code>。支持字段的子类应覆盖此方法并创建一个具有有意义属性的 <code>AttributedCharacterIterator</code>。
     *
     * @exception NullPointerException 如果 obj 为 null。
     * @exception IllegalArgumentException 当格式化器无法格式化给定的对象时。
     * @param obj 要格式化的对象
     * @return 描述格式化值的 <code>AttributedCharacterIterator</code>。
     * @since 1.4
     */
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        return createAttributedCharacterIterator(format(obj));
    }

    /**
     * 从字符串中解析文本以生成一个对象。
     * <p>
     * 该方法尝试从 <code>pos</code> 给定的索引开始解析文本。
     * 如果解析成功，则 <code>pos</code> 的索引将更新为使用后的最后一个字符的索引（解析不一定使用字符串末尾的所有字符），并返回解析的对象。更新的 <code>pos</code> 可用于指示下一次调用此方法的起始点。
     * 如果发生错误，则 <code>pos</code> 的索引不会更改，<code>pos</code> 的错误索引将设置为发生错误的字符的索引，并返回 null。
     *
     * @param source 一个 <code>String</code>，其部分文本应被解析。
     * @param pos 一个 <code>ParsePosition</code> 对象，包含上述的索引和错误索引信息。
     * @return 从字符串解析的对象。在错误情况下，返回 null。
     * @exception NullPointerException 如果 <code>pos</code> 为 null。
     */
    public abstract Object parseObject (String source, ParsePosition pos);

    /**
     * 从给定字符串的开头解析文本以生成一个对象。
     * 该方法可能不会使用给定字符串的全部文本。
     *
     * @param source 一个 <code>String</code>，其开头应被解析。
     * @return 从字符串解析的对象。
     * @exception ParseException 如果指定字符串的开头无法解析。
     */
    public Object parseObject(String source) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Object result = parseObject(source, pos);
        if (pos.index == 0) {
            throw new ParseException("Format.parseObject(String) failed",
                pos.errorIndex);
        }
        return result;
    }

    /**
     * 创建并返回此对象的副本。
     *
     * @return 此实例的克隆。
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // 永远不会发生
            throw new InternalError(e);
        }
    }

    //
    // 从不同参数创建 AttributedCharacterIterators 的便捷方法。
    //

    /**
     * 为字符串 <code>s</code> 创建一个 <code>AttributedCharacterIterator</code>。
     *
     * @param s 用于创建 AttributedCharacterIterator 的字符串
     * @return 包含 s 的 AttributedCharacterIterator
     */
    AttributedCharacterIterator createAttributedCharacterIterator(String s) {
        AttributedString as = new AttributedString(s);

        return as.getIterator();
    }

    /**
     * 从传递的 <code>AttributedCharacterIterator</code> 的连接内容创建一个 <code>AttributedCharacterIterator</code>。
     *
     * @param iterators 用于创建结果 AttributedCharacterIterators 的 AttributedCharacterIterators
     * @return 包含传递的 AttributedCharacterIterators 的 AttributedCharacterIterator
     */
    AttributedCharacterIterator createAttributedCharacterIterator(
                       AttributedCharacterIterator[] iterators) {
        AttributedString as = new AttributedString(iterators);

        return as.getIterator();
    }

    /**
     * 返回一个包含字符串 <code>string</code> 和附加的键值对 <code>key</code>、<code>value</code> 的 AttributedCharacterIterator。
     *
     * @param string 用于创建 AttributedCharacterIterator 的字符串
     * @param key AttributedCharacterIterator 的键
     * @param value 与键关联的值
     * @return 包含 args 的 AttributedCharacterIterator
     */
    AttributedCharacterIterator createAttributedCharacterIterator(
                      String string, AttributedCharacterIterator.Attribute key,
                      Object value) {
        AttributedString as = new AttributedString(string);

        as.addAttribute(key, value);
        return as.getIterator();
    }

    /**
     * 使用 <code>iterator</code> 的内容和附加的属性 <code>key</code> <code>value</code> 创建一个 AttributedCharacterIterator。
     *
     * @param iterator 要添加 arg 的初始 AttributedCharacterIterator
     * @param key AttributedCharacterIterator 的键
     * @param value 与键关联的值
     * @return 包含 args 的 AttributedCharacterIterator
     */
    AttributedCharacterIterator createAttributedCharacterIterator(
              AttributedCharacterIterator iterator,
              AttributedCharacterIterator.Attribute key, Object value) {
        AttributedString as = new AttributedString(iterator);

        as.addAttribute(key, value);
        return as.getIterator();
    }


    /**
     * 定义在 <code>Format.formatToCharacterIterator</code> 返回的 <code>AttributedCharacterIterator</code> 中用作属性键的常量，以及在 <code>FieldPosition</code> 中用作字段标识符的常量。
     *
     * @since 1.4
     */
    public static class Field extends AttributedCharacterIterator.Attribute {

        // 声明与 1.4 FCS 兼容
        private static final long serialVersionUID = 276966692217360283L;

        /**
         * 使用指定的名称创建一个 Field。
         *
         * @param name 属性的名称
         */
        protected Field(String name) {
            super(name);
        }
    }


    /**
     * 当各种 <code>Format</code> 实现格式化对象时，FieldDelegate 会收到通知。这允许存储格式化字符串的各个部分，以便稍后使用，例如在 <code>FieldPosition</code> 中或在 <code>AttributedCharacterIterator</code> 中。
     * <p>
     * 委托不应假设 <code>Format</code> 会以任何特定顺序通知委托字段。
     *
     * @see FieldPosition#getFieldDelegate
     * @see CharacterIteratorFieldDelegate
     */
    interface FieldDelegate {
        /**
         * 当字符串的特定区域被格式化时收到通知。如果 <code>attr</code> 没有对应的整数字段 ID，则会调用此方法。
         *
         * @param attr 标识匹配的字段
         * @param value 与字段关联的值
         * @param start 字段的开始位置，将 >= 0
         * @param end 字段的结束位置，将 >= start 且 <= buffer.length()
         * @param buffer 包含当前格式化值，接收者不应修改它。
         */
        public void formatted(Format.Field attr, Object value, int start,
                              int end, StringBuffer buffer);


                    /**
         * 当字符串的特定区域被格式化时通知。
         *
         * @param fieldID 通过整数标识字段
         * @param attr 识别匹配的字段
         * @param value 与字段关联的值
         * @param start 字段的起始位置，将 >= 0
         * @param end 字段的结束位置，将 >= start 且 <= buffer.length()
         * @param buffer 包含当前格式化的值，接收者不应修改它。
         */
        public void formatted(int fieldID, Format.Field attr, Object value,
                              int start, int end, StringBuffer buffer);
    }
}
