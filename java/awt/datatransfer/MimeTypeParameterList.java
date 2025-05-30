/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.datatransfer;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * 一个封装了 MimeType 参数列表的对象，如 RFC 2045 和 2046 中定义的。
 *
 * @author jeff.dunn@eng.sun.com
 */
class MimeTypeParameterList implements Cloneable {

    /**
     * 默认构造函数。
     */
    public MimeTypeParameterList() {
        parameters = new Hashtable<>();
    }

    public MimeTypeParameterList(String rawdata)
        throws MimeTypeParseException
    {
        parameters = new Hashtable<>();

        // 现在解析 rawdata
        parse(rawdata);
    }

    public int hashCode() {
        int code = Integer.MAX_VALUE / 45; // "随机"值，用于空列表
        String paramName = null;
        Enumeration<String> enum_ = this.getNames();

        while (enum_.hasMoreElements()) {
            paramName = enum_.nextElement();
            code += paramName.hashCode();
            code += this.get(paramName).hashCode();
        }

        return code;
    } // hashCode()

    /**
     * 如果两个参数列表具有完全相同的参数名称和关联值，则认为它们相等。参数的顺序不考虑。
     */
    public boolean equals(Object thatObject) {
        // System.out.println("MimeTypeParameterList.equals(" + this + "," + thatObject + ")");
        if (!(thatObject instanceof MimeTypeParameterList)) {
            return false;
        }
        MimeTypeParameterList that = (MimeTypeParameterList) thatObject;
        if (this.size() != that.size()) {
            return false;
        }
        String name = null;
        String thisValue = null;
        String thatValue = null;
        Set<Map.Entry<String, String>> entries = parameters.entrySet();
        Iterator<Map.Entry<String, String>> iterator = entries.iterator();
        Map.Entry<String, String> entry = null;
        while (iterator.hasNext()) {
            entry = iterator.next();
            name = entry.getKey();
            thisValue = entry.getValue();
            thatValue = that.parameters.get(name);
            if ((thisValue == null) || (thatValue == null)) {
                // 两者都为 null -> 相等，只有一个为 null -> 不相等
                if (thisValue != thatValue) {
                    return false;
                }
            } else if (!thisValue.equals(thatValue)) {
                return false;
            }
        } // while iterator

        return true;
    } // equals()

    /**
     * 从字符串中解析参数列表的例程。
     */
    protected void parse(String rawdata) throws MimeTypeParseException {
        int length = rawdata.length();
        if (length > 0) {
            int currentIndex = skipWhiteSpace(rawdata, 0);
            int lastIndex = 0;

            if (currentIndex < length) {
                char currentChar = rawdata.charAt(currentIndex);
                while ((currentIndex < length) && (currentChar == ';')) {
                    String name;
                    String value;
                    boolean foundit;

                    // 跳过 ';'
                    ++currentIndex;

                    // 现在解析参数名称

                    // 跳过空白
                    currentIndex = skipWhiteSpace(rawdata, currentIndex);

                    if (currentIndex < length) {
                        // 找到 token 字符的结束位置
                        lastIndex = currentIndex;
                        currentChar = rawdata.charAt(currentIndex);
                        while ((currentIndex < length) && isTokenChar(currentChar)) {
                            ++currentIndex;
                            currentChar = rawdata.charAt(currentIndex);
                        }
                        name = rawdata.substring(lastIndex, currentIndex).toLowerCase();

                        // 现在解析分隔名称和值的 '='

                        // 跳过空白
                        currentIndex = skipWhiteSpace(rawdata, currentIndex);

                        if ((currentIndex < length) && (rawdata.charAt(currentIndex) == '=')) {
                            // 跳过 '=' 并解析参数值
                            ++currentIndex;

                            // 跳过空白
                            currentIndex = skipWhiteSpace(rawdata, currentIndex);

                            if (currentIndex < length) {
                                // 现在判断是否为带引号的值
                                currentChar = rawdata.charAt(currentIndex);
                                if (currentChar == '"') {
                                    // 是带引号的值，跳过引号并捕获带引号的字符串
                                    ++currentIndex;
                                    lastIndex = currentIndex;

                                    if (currentIndex < length) {
                                        // 找到下一个未转义的引号
                                        foundit = false;
                                        while ((currentIndex < length) && !foundit) {
                                            currentChar = rawdata.charAt(currentIndex);
                                            if (currentChar == '\\') {
                                                // 找到转义序列，跳过此字符和下一个字符
                                                currentIndex += 2;
                                            } else if (currentChar == '"') {
                                                // 找到引号
                                                foundit = true;
                                            } else {
                                                ++currentIndex;
                                            }
                                        }
                                        if (currentChar == '"') {
                                            value = unquote(rawdata.substring(lastIndex, currentIndex));
                                            // 跳过引号
                                            ++currentIndex;
                                        } else {
                                            throw new MimeTypeParseException("遇到未终止的带引号的参数值。");
                                        }
                                    } else {
                                        throw new MimeTypeParseException("遇到未终止的带引号的参数值。");
                                    }
                                } else if (isTokenChar(currentChar)) {
                                    // 普通 token，以非 token 字符结束
                                    lastIndex = currentIndex;
                                    foundit = false;
                                    while ((currentIndex < length) && !foundit) {
                                        currentChar = rawdata.charAt(currentIndex);

                                        if (isTokenChar(currentChar)) {
                                            ++currentIndex;
                                        } else {
                                            foundit = true;
                                        }
                                    }
                                    value = rawdata.substring(lastIndex, currentIndex);
                                } else {
                                    // 不是值
                                    throw new MimeTypeParseException("在索引 " + currentIndex + " 处遇到意外字符。");
                                }

                                // 将数据放入哈希表
                                parameters.put(name, value);
                            } else {
                                throw new MimeTypeParseException("找不到名为 " + name + " 的参数的值。");
                            }
                        } else {
                            throw new MimeTypeParseException("找不到分隔参数名称和值的 '='。");
                        }
                    } else {
                        throw new MimeTypeParseException("找不到参数名称。");
                    }

                    // 设置下一次迭代
                    currentIndex = skipWhiteSpace(rawdata, currentIndex);
                    if (currentIndex < length) {
                        currentChar = rawdata.charAt(currentIndex);
                    }
                }
                if (currentIndex < length) {
                    throw new MimeTypeParseException("输入中遇到比预期更多的字符。");
                }
            }
        }
    }

    /**
     * 返回此列表中的名称-值对的数量。
     */
    public int size() {
        return parameters.size();
    }

    /**
     * 确定此列表是否为空。
     */
    public boolean isEmpty() {
        return parameters.isEmpty();
    }

    /**
     * 检索与给定名称关联的值，如果没有当前关联，则返回 null。
     */
    public String get(String name) {
        return parameters.get(name.trim().toLowerCase());
    }

    /**
     * 设置与给定名称关联的值，替换任何先前的关联。
     */
    public void set(String name, String value) {
        parameters.put(name.trim().toLowerCase(), value);
    }

    /**
     * 移除与给定名称关联的任何值。
     */
    public void remove(String name) {
        parameters.remove(name.trim().toLowerCase());
    }

    /**
     * 检索此列表中所有名称的枚举。
     */
    public Enumeration<String> getNames() {
        return parameters.keys();
    }

    public String toString() {
        // 启发式：每个字段 8 个字符
        StringBuilder buffer = new StringBuilder(parameters.size() * 16);

        Enumeration<String> keys = parameters.keys();
        while (keys.hasMoreElements()) {
            buffer.append("; ");

            String key = keys.nextElement();
            buffer.append(key);
            buffer.append('=');
            buffer.append(quote(parameters.get(key)));
        }

        return buffer.toString();
    }

    /**
     * 返回此对象的克隆。
     */

    public Object clone() {
        MimeTypeParameterList newObj = null;
        try {
            newObj = (MimeTypeParameterList) super.clone();
        } catch (CloneNotSupportedException cannotHappen) {
        }
        newObj.parameters = (Hashtable) parameters.clone();
        return newObj;
    }

    private Hashtable<String, String> parameters;

    // 以下是与解析相关的吓人内容

    /**
     * 确定给定字符是否属于合法的 token。
     */
    private static boolean isTokenChar(char c) {
        return ((c > 040) && (c < 0177)) && (TSPECIALS.indexOf(c) < 0);
    }

    /**
     * 返回 rawdata 中从索引 i 开始的第一个非空白字符的索引。
     */
    private static int skipWhiteSpace(String rawdata, int i) {
        int length = rawdata.length();
        if (i < length) {
            char c = rawdata.charAt(i);
            while ((i < length) && Character.isWhitespace(c)) {
                ++i;
                c = rawdata.charAt(i);
            }
        }

        return i;
    }

    /**
     * 知道何时以及如何引用和转义给定值的例程。
     */
    private static String quote(String value) {
        boolean needsQuotes = false;

        // 检查是否需要引用此值
        int length = value.length();
        for (int i = 0; (i < length) && !needsQuotes; ++i) {
            needsQuotes = !isTokenChar(value.charAt(i));
        }

        if (needsQuotes) {
            StringBuilder buffer = new StringBuilder((int) (length * 1.5));

            // 添加初始引号
            buffer.append('"');

            // 添加适当转义的文本
            for (int i = 0; i < length; ++i) {
                char c = value.charAt(i);
                if ((c == '\\') || (c == '"')) {
                    buffer.append('\\');
                }
                buffer.append(c);
            }

            // 添加结束引号
            buffer.append('"');

            return buffer.toString();
        } else {
            return value;
        }
    }

    /**
     * 知道如何从给定值中剥离引号和转义序列的例程。
     */
    private static String unquote(String value) {
        int valueLength = value.length();
        StringBuilder buffer = new StringBuilder(valueLength);

        boolean escaped = false;
        for (int i = 0; i < valueLength; ++i) {
            char currentChar = value.charAt(i);
            if (!escaped && (currentChar != '\\')) {
                buffer.append(currentChar);
            } else if (escaped) {
                buffer.append(currentChar);
                escaped = false;
            } else {
                escaped = true;
            }
        }

        return buffer.toString();
    }

    /**
     * 一个包含所有特殊字符的字符串。
     */
    private static final String TSPECIALS = "()<>@,;:\\\"/[]?=";

}
