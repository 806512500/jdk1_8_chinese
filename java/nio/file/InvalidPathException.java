/*
 * Copyright (c) 2007, 2009, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.file;

/**
 * 当路径字符串无法转换为 {@link Path} 时抛出的未经检查的异常，因为路径字符串包含无效字符，或
 * 由于其他文件系统特定的原因使得路径字符串无效。
 */

public class InvalidPathException
    extends IllegalArgumentException
{
    static final long serialVersionUID = 4355821422286746137L;

    private String input;
    private int index;

    /**
     * 从给定的输入字符串、原因和错误索引构造一个实例。
     *
     * @param  input   输入字符串
     * @param  reason  解释为什么输入被拒绝的字符串
     * @param  index   错误发生的位置的索引，
     *                 或 <tt>-1</tt> 如果索引未知
     *
     * @throws  NullPointerException
     *          如果输入或原因字符串为 <tt>null</tt>
     *
     * @throws  IllegalArgumentException
     *          如果错误索引小于 <tt>-1</tt>
     */
    public InvalidPathException(String input, String reason, int index) {
        super(reason);
        if ((input == null) || (reason == null))
            throw new NullPointerException();
        if (index < -1)
            throw new IllegalArgumentException();
        this.input = input;
        this.index = index;
    }

    /**
     * 从给定的输入字符串和原因构造一个实例。结果对象的错误索引为 <tt>-1</tt>。
     *
     * @param  input   输入字符串
     * @param  reason  解释为什么输入被拒绝的字符串
     *
     * @throws  NullPointerException
     *          如果输入或原因字符串为 <tt>null</tt>
     */
    public InvalidPathException(String input, String reason) {
        this(input, reason, -1);
    }

    /**
     * 返回输入字符串。
     *
     * @return  输入字符串
     */
    public String getInput() {
        return input;
    }

    /**
     * 返回一个解释为什么输入字符串被拒绝的字符串。
     *
     * @return  原因字符串
     */
    public String getReason() {
        return super.getMessage();
    }

    /**
     * 返回输入字符串中错误发生位置的索引，或 <tt>-1</tt> 如果位置未知。
     *
     * @return  错误索引
     */
    public int getIndex() {
        return index;
    }

    /**
     * 返回描述错误的字符串。结果字符串由原因字符串、冒号字符
     * (<tt>':'</tt>)、空格和输入字符串组成。如果错误索引已定义，则在原因字符串后和冒号字符前插入
     * <tt>" at index "</tt> 和索引的十进制表示。
     *
     * @return  描述错误的字符串
     */
    public String getMessage() {
        StringBuffer sb = new StringBuffer();
        sb.append(getReason());
        if (index > -1) {
            sb.append(" at index ");
            sb.append(index);
        }
        sb.append(": ");
        sb.append(input);
        return sb.toString();
    }
}
