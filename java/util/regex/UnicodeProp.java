/*
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.util.HashMap;
import java.util.Locale;

enum UnicodeProp {

    ALPHABETIC {
        // 判断字符是否为字母。
        public boolean is(int ch) {
            return Character.isAlphabetic(ch);
        }
    },

    LETTER {
        // 判断字符是否为字母。
        public boolean is(int ch) {
            return Character.isLetter(ch);
        }
    },

    IDEOGRAPHIC {
        // 判断字符是否为表意文字。
        public boolean is(int ch) {
            return Character.isIdeographic(ch);
        }
    },

    LOWERCASE {
        // 判断字符是否为小写字母。
        public boolean is(int ch) {
            return Character.isLowerCase(ch);
        }
    },

    UPPERCASE {
        // 判断字符是否为大写字母。
        public boolean is(int ch) {
            return Character.isUpperCase(ch);
        }
    },

    TITLECASE {
        // 判断字符是否为标题大小写。
        public boolean is(int ch) {
            return Character.isTitleCase(ch);
        }
    },

    WHITE_SPACE {
        // \p{Whitespace}
        // 判断字符是否为空白字符。
        public boolean is(int ch) {
            return ((((1 << Character.SPACE_SEPARATOR) |
                      (1 << Character.LINE_SEPARATOR) |
                      (1 << Character.PARAGRAPH_SEPARATOR)) >> Character.getType(ch)) & 1)
                   != 0 || (ch >= 0x9 && ch <= 0xd) || (ch == 0x85);
        }
    },

    CONTROL {
        // \p{gc=Control}
        // 判断字符是否为控制字符。
        public boolean is(int ch) {
            return Character.getType(ch) == Character.CONTROL;
        }
    },

    PUNCTUATION {
        // \p{gc=Punctuation}
        // 判断字符是否为标点符号。
        public boolean is(int ch) {
            return ((((1 << Character.CONNECTOR_PUNCTUATION) |
                      (1 << Character.DASH_PUNCTUATION) |
                      (1 << Character.START_PUNCTUATION) |
                      (1 << Character.END_PUNCTUATION) |
                      (1 << Character.OTHER_PUNCTUATION) |
                      (1 << Character.INITIAL_QUOTE_PUNCTUATION) |
                      (1 << Character.FINAL_QUOTE_PUNCTUATION)) >> Character.getType(ch)) & 1)
                   != 0;
        }
    },

    HEX_DIGIT {
        // \p{gc=Decimal_Number}
        // \p{Hex_Digit}    -> PropList.txt: Hex_Digit
        // 判断字符是否为十六进制数字。
        public boolean is(int ch) {
            return DIGIT.is(ch) ||
                   (ch >= 0x0030 && ch <= 0x0039) ||
                   (ch >= 0x0041 && ch <= 0x0046) ||
                   (ch >= 0x0061 && ch <= 0x0066) ||
                   (ch >= 0xFF10 && ch <= 0xFF19) ||
                   (ch >= 0xFF21 && ch <= 0xFF26) ||
                   (ch >= 0xFF41 && ch <= 0xFF46);
        }
    },

    ASSIGNED {
        // 判断字符是否已分配。
        public boolean is(int ch) {
            return Character.getType(ch) != Character.UNASSIGNED;
        }
    },

    NONCHARACTER_CODE_POINT {
        // PropList.txt:Noncharacter_Code_Point
        // 判断字符是否为非字符代码点。
        public boolean is(int ch) {
            return (ch & 0xfffe) == 0xfffe || (ch >= 0xfdd0 && ch <= 0xfdef);
        }
    },

    DIGIT {
        // \p{gc=Decimal_Number}
        // 判断字符是否为数字。
        public boolean is(int ch) {
            return Character.isDigit(ch);
        }
    },

    ALNUM {
        // \p{alpha}
        // \p{digit}
        // 判断字符是否为字母或数字。
        public boolean is(int ch) {
            return ALPHABETIC.is(ch) || DIGIT.is(ch);
        }
    },

    BLANK {
        // \p{Whitespace} --
        // [\N{LF} \N{VT} \N{FF} \N{CR} \N{NEL}  -> 0xa, 0xb, 0xc, 0xd, 0x85
        //  \p{gc=Line_Separator}
        //  \p{gc=Paragraph_Separator}]
        // 判断字符是否为空白字符。
        public boolean is(int ch) {
            return Character.getType(ch) == Character.SPACE_SEPARATOR ||
                   ch == 0x9; // \N{HT}
        }
    },

    GRAPH {
        // [^
        //  \p{space}
        //  \p{gc=Control}
        //  \p{gc=Surrogate}
        //  \p{gc=Unassigned}]
        // 判断字符是否为图形字符。
        public boolean is(int ch) {
            return ((((1 << Character.SPACE_SEPARATOR) |
                      (1 << Character.LINE_SEPARATOR) |
                      (1 << Character.PARAGRAPH_SEPARATOR) |
                      (1 << Character.CONTROL) |
                      (1 << Character.SURROGATE) |
                      (1 << Character.UNASSIGNED)) >> Character.getType(ch)) & 1)
                   == 0;
        }
    },

    PRINT {
        // \p{graph}
        // \p{blank}
        // -- \p{cntrl}
        // 判断字符是否为可打印字符。
        public boolean is(int ch) {
            return (GRAPH.is(ch) || BLANK.is(ch)) && !CONTROL.is(ch);
        }
    },

    WORD {
        //  \p{alpha}
        //  \p{gc=Mark}
        //  \p{digit}
        //  \p{gc=Connector_Punctuation}
        //  \p{Join_Control}    200C..200D
        // 判断字符是否为单词字符。
        public boolean is(int ch) {
            return ALPHABETIC.is(ch) ||
                   ((((1 << Character.NON_SPACING_MARK) |
                      (1 << Character.ENCLOSING_MARK) |
                      (1 << Character.COMBINING_SPACING_MARK) |
                      (1 << Character.DECIMAL_DIGIT_NUMBER) |
                      (1 << Character.CONNECTOR_PUNCTUATION)) >> Character.getType(ch)) & 1)
                   != 0 ||
                   JOIN_CONTROL.is(ch);
        }
    },

    JOIN_CONTROL {
        //  200C..200D    PropList.txt:Join_Control
        // 判断字符是否为连接控制字符。
        public boolean is(int ch) {
           return (ch == 0x200C || ch == 0x200D);
        }
    };

    private final static HashMap<String, String> posix = new HashMap<>();
    private final static HashMap<String, String> aliases = new HashMap<>();
    static {
        // 将POSIX名称映射到Unicode属性名称。
        posix.put("ALPHA", "ALPHABETIC");
        posix.put("LOWER", "LOWERCASE");
        posix.put("UPPER", "UPPERCASE");
        posix.put("SPACE", "WHITE_SPACE");
        posix.put("PUNCT", "PUNCTUATION");
        posix.put("XDIGIT","HEX_DIGIT");
        posix.put("ALNUM", "ALNUM");
        posix.put("CNTRL", "CONTROL");
        posix.put("DIGIT", "DIGIT");
        posix.put("BLANK", "BLANK");
        posix.put("GRAPH", "GRAPH");
        posix.put("PRINT", "PRINT");

        // 将别名映射到Unicode属性名称。
        aliases.put("WHITESPACE", "WHITE_SPACE");
        aliases.put("HEXDIGIT","HEX_DIGIT");
        aliases.put("NONCHARACTERCODEPOINT", "NONCHARACTER_CODE_POINT");
        aliases.put("JOINCONTROL", "JOIN_CONTROL");
    }

    // 根据属性名称获取Unicode属性。
    public static UnicodeProp forName(String propName) {
        propName = propName.toUpperCase(Locale.ENGLISH);
        String alias = aliases.get(propName);
        if (alias != null)
            propName = alias;
        try {
            return valueOf (propName);
        } catch (IllegalArgumentException x) {}
        return null;
    }

    // 根据POSIX名称获取Unicode属性。
    public static UnicodeProp forPOSIXName(String propName) {
        propName = posix.get(propName.toUpperCase(Locale.ENGLISH));
        if (propName == null)
            return null;
        return valueOf (propName);
    }

    // 抽象方法，判断字符是否具有特定属性。
    public abstract boolean is(int ch);
}
