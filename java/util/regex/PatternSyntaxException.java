/*
 * Copyright (c) 1999, 2018, Oracle and/or its affiliates. All rights reserved.
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

package java.util.regex;

import sun.security.action.GetPropertyAction;


/**
 * 用于指示正则表达式模式中语法错误的未检查异常。
 *
 * @author  未署名
 * @since 1.4
 * @spec JSR-51
 */

public class PatternSyntaxException
    extends IllegalArgumentException
{
    private static final long serialVersionUID = -3864639126226059218L;

    private final String desc;
    private final String pattern;
    private final int index;

    /**
     * 构造此类的新实例。
     *
     * @param  desc
     *         错误的描述
     *
     * @param  regex
     *         错误的模式
     *
     * @param  index
     *         模式中错误的大致索引，
     *         或者如果索引未知则为 <tt>-1</tt>
     */
    public PatternSyntaxException(String desc, String regex, int index) {
        this.desc = desc;
        this.pattern = regex;
        this.index = index;
    }

    /**
     * 检索错误索引。
     *
     * @return  模式中错误的大致索引，
     *         或者如果索引未知则为 <tt>-1</tt>
     */
    public int getIndex() {
        return index;
    }

    /**
     * 检索错误的描述。
     *
     * @return  错误的描述
     */
    public String getDescription() {
        return desc;
    }

    /**
     * 检索错误的正则表达式模式。
     *
     * @return  错误的模式
     */
    public String getPattern() {
        return pattern;
    }

    private static final String nl =
        java.security.AccessController
            .doPrivileged(new GetPropertyAction("line.separator"));

    /**
     * 返回一个包含语法错误描述及其索引、错误的正则表达式模式以及模式中错误索引的视觉指示的多行字符串。
     *
     * @return  完整的详细消息
     */
    public String getMessage() {
        StringBuffer sb = new StringBuffer();
        sb.append(desc);
        if (index >= 0) {
            sb.append(" near index ");
            sb.append(index);
        }
        sb.append(nl);
        sb.append(pattern);
        if (index >= 0 && pattern != null && index < pattern.length()) {
            sb.append(nl);
            for (int i = 0; i < index; i++) sb.append(' ');
            sb.append('^');
        }
        return sb.toString();
    }

}
