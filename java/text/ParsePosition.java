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


/**
 * <code>ParsePosition</code> 是一个简单的类，用于 <code>Format</code>
 * 及其子类在解析过程中跟踪当前位置。
 * 各种 <code>Format</code> 类中的 <code>parseObject</code> 方法需要一个
 * <code>ParsePosition</code> 对象作为参数。
 *
 * <p>
 * 按设计，当您使用不同的格式解析字符串时，
 * 可以使用相同的 <code>ParsePosition</code>，因为索引参数记录了当前位置。
 *
 * @author      Mark Davis
 * @see         java.text.Format
 */

public class ParsePosition {

    /**
     * 输入：开始解析的位置。
     * <br>输出：解析停止的位置。
     * 这是为了连续使用而设计的，
     * 每次调用都会设置索引以供下一次使用。
     */
    int index = 0;
    int errorIndex = -1;

    /**
     * 获取当前解析位置。在解析方法的输入中，
     * 这是解析将开始的字符索引；在输出中，这是解析的最后一个字符之后的字符索引。
     *
     * @return 当前解析位置
     */
    public int getIndex() {
        return index;
    }

    /**
     * 设置当前解析位置。
     *
     * @param index 当前解析位置
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * 使用给定的初始索引创建一个新的 ParsePosition。
     *
     * @param index 初始索引
     */
    public ParsePosition(int index) {
        this.index = index;
    }
    /**
     * 设置发生解析错误的索引。格式化器
     * 应在从其 parseObject 方法返回错误代码之前设置此值。默认值为 -1，如果未设置则为 -1。
     *
     * @param ei 发生错误的索引
     * @since 1.2
     */
    public void setErrorIndex(int ei)
    {
        errorIndex = ei;
    }

    /**
     * 获取发生错误的索引，如果未设置错误索引则返回 -1。
     *
     * @return 发生错误的索引
     * @since 1.2
     */
    public int getErrorIndex()
    {
        return errorIndex;
    }

    /**
     * 覆盖 equals 方法
     */
    public boolean equals(Object obj)
    {
        if (obj == null) return false;
        if (!(obj instanceof ParsePosition))
            return false;
        ParsePosition other = (ParsePosition) obj;
        return (index == other.index && errorIndex == other.errorIndex);
    }

    /**
     * 返回此 ParsePosition 的哈希码。
     * @return 此对象的哈希码值
     */
    public int hashCode() {
        return (errorIndex << 16) | index;
    }

    /**
     * 返回此 ParsePosition 的字符串表示形式。
     * @return 此对象的字符串表示形式
     */
    public String toString() {
        return getClass().getName() +
            "[index=" + index +
            ",errorIndex=" + errorIndex + ']';
    }
}
