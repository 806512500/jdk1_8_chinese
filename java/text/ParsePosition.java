/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 及其子类在解析过程中跟踪当前的位置。
 * 各种 <code>Format</code> 类中的 <code>parseObject</code> 方法需要一个 <code>ParsePosition</code> 对象作为参数。
 *
 * <p>
 * 按设计，当您使用不同的格式解析字符串时，
 * 可以使用相同的 <code>ParsePosition</code>，因为索引参数记录了当前的位置。
 *
 * @author      Mark Davis
 * @see         java.text.Format
 */

public class ParsePosition {

    /**
     * 输入：开始解析的位置。
     * <br>输出：解析停止的位置。
     * 这是为了连续使用而设计的，
     * 每次调用都会设置索引以供下一次调用使用。
     */
    int index = 0;
    int errorIndex = -1;

    /**
     * 获取当前的解析位置。在解析方法的输入时，
     * 这是解析将开始的字符的索引；在输出时，这是解析的最后一个字符之后的字符的索引。
     *
     * @return 当前的解析位置
     */
    public int getIndex() {
        return index;
    }

    /**
     * 设置当前的解析位置。
     *
     * @param index 当前的解析位置
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
     * 设置解析错误发生的位置。格式化器
     * 应在从其 parseObject 方法返回错误代码之前设置此值。默认值为 -1，如果未设置则为 -1。
     *
     * @param ei 错误发生的位置
     * @since 1.2
     */
    public void setErrorIndex(int ei)
    {
        errorIndex = ei;
    }

    /**
     * 获取错误发生的位置，如果未设置错误索引，则返回 -1。
     *
     * @return 错误发生的位置
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
