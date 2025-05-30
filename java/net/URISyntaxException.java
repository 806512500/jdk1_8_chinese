/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.net;


/**
 * 表示字符串无法解析为 URI 引用时抛出的检查异常。
 *
 * @author Mark Reinhold
 * @see URI
 * @since 1.4
 */

public class URISyntaxException
    extends Exception
{
    private static final long serialVersionUID = 2137979680897488891L;

    private String input;
    private int index;

    /**
     * 从给定的输入字符串、原因和错误索引构造一个实例。
     *
     * @param  input   输入字符串
     * @param  reason  解释为什么输入无法解析的字符串
     * @param  index   解析错误发生的位置的索引，
     *                 或者如果索引未知则为 {@code -1}
     *
     * @throws  NullPointerException
     *          如果输入或原因字符串为 {@code null}
     *
     * @throws  IllegalArgumentException
     *          如果错误索引小于 {@code -1}
     */
    public URISyntaxException(String input, String reason, int index) {
        super(reason);
        if ((input == null) || (reason == null))
            throw new NullPointerException();
        if (index < -1)
            throw new IllegalArgumentException();
        this.input = input;
        this.index = index;
    }

    /**
     * 从给定的输入字符串和原因构造一个实例。结果对象的错误索引为 {@code -1}。
     *
     * @param  input   输入字符串
     * @param  reason  解释为什么输入无法解析的字符串
     *
     * @throws  NullPointerException
     *          如果输入或原因字符串为 {@code null}
     */
    public URISyntaxException(String input, String reason) {
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
     * 返回一个解释为什么输入字符串无法解析的字符串。
     *
     * @return  原因字符串
     */
    public String getReason() {
        return super.getMessage();
    }

    /**
     * 返回输入字符串中解析错误发生位置的索引，如果该位置未知则返回 {@code -1}。
     *
     * @return  错误索引
     */
    public int getIndex() {
        return index;
    }

    /**
     * 返回描述解析错误的字符串。结果字符串由原因字符串、冒号字符
     * ({@code ':'})、一个空格和输入字符串组成。如果错误索引已定义，则在原因字符串之后和冒号字符之前插入
     * 字符串 {@code " at index "} 和索引的十进制表示。
     *
     * @return  描述解析错误的字符串
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
