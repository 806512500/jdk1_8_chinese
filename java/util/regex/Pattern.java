
/*
 * Copyright (c) 1999, 2021, Oracle and/or its affiliates. All rights reserved.
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

import java.text.Normalizer;
import java.util.Locale;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 * 正则表达式的编译表示。
 *
 * <p> 必须首先将指定为字符串的正则表达式编译为此类的实例。生成的模式然后可以用于创建一个 {@link Matcher} 对象，该对象可以将任意 {@linkplain
 * java.lang.CharSequence 字符序列} 与正则表达式进行匹配。所有与执行匹配相关的状态都存储在匹配器中，因此许多匹配器可以共享同一个模式。
 *
 * <p> 一个典型的调用序列如下：
 *
 * <blockquote><pre>
 * Pattern p = Pattern.{@link #compile compile}("a*b");
 * Matcher m = p.{@link #matcher matcher}("aaaaab");
 * boolean b = m.{@link Matcher#matches matches}();</pre></blockquote>
 *
 * <p> 为了方便起见，此类定义了一个 {@link #matches matches} 方法，当正则表达式仅使用一次时非常有用。此方法在单个调用中编译表达式并匹配输入序列。语句
 *
 * <blockquote><pre>
 * boolean b = Pattern.matches("a*b", "aaaaab");</pre></blockquote>
 *
 * 与上面的三个语句等效，但对于重复匹配，由于不能重用编译后的模式，因此效率较低。
 *
 * <p> 该类的实例是不可变的，可以安全地被多个并发线程使用。{@link Matcher} 类的实例则不安全。
 *
 *
 * <h3><a name="sum">正则表达式构造的总结</a></h3>
 *
 * <table border="0" cellpadding="1" cellspacing="0"
 *  summary="正则表达式构造及其匹配内容">
 *
 * <tr align="left">
 * <th align="left" id="construct">构造</th>
 * <th align="left" id="matches">匹配</th>
 * </tr>
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="characters">字符</th></tr>
 *
 * <tr><td valign="top" headers="construct characters"><i>x</i></td>
 *     <td headers="matches">字符 <i>x</i></td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>\\</tt></td>
 *     <td headers="matches">反斜杠字符</td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>\0</tt><i>n</i></td>
 *     <td headers="matches">八进制值为 <tt>0</tt><i>n</i> 的字符
 *         (0&nbsp;<tt>&lt;=</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>&nbsp;7)</td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>\0</tt><i>nn</i></td>
 *     <td headers="matches">八进制值为 <tt>0</tt><i>nn</i> 的字符
 *         (0&nbsp;<tt>&lt;=</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>&nbsp;7)</td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>\0</tt><i>mnn</i></td>
 *     <td headers="matches">八进制值为 <tt>0</tt><i>mnn</i> 的字符
 *         (0&nbsp;<tt>&lt;=</tt>&nbsp;<i>m</i>&nbsp;<tt>&lt;=</tt>&nbsp;3,
 *         0&nbsp;<tt>&lt;=</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>&nbsp;7)</td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>\x</tt><i>hh</i></td>
 *     <td headers="matches">十六进制值为 <tt>0x</tt><i>hh</i> 的字符</td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>&#92;u</tt><i>hhhh</i></td>
 *     <td headers="matches">十六进制值为 <tt>0x</tt><i>hhhh</i> 的字符</td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>&#92;x</tt><i>{h...h}</i></td>
 *     <td headers="matches">十六进制值为 <tt>0x</tt><i>h...h</i> 的字符
 *         ({@link java.lang.Character#MIN_CODE_POINT Character.MIN_CODE_POINT}
 *         &nbsp;&lt;=&nbsp;<tt>0x</tt><i>h...h</i>&nbsp;&lt;=&nbsp;
 *          {@link java.lang.Character#MAX_CODE_POINT Character.MAX_CODE_POINT})</td></tr>
 * <tr><td valign="top" headers="matches"><tt>\t</tt></td>
 *     <td headers="matches">制表符 (<tt>'&#92;u0009'</tt>)</td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>\n</tt></td>
 *     <td headers="matches">换行符 (行进符) (<tt>'&#92;u000A'</tt>)</td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>\r</tt></td>
 *     <td headers="matches">回车符 (<tt>'&#92;u000D'</tt>)</td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>\f</tt></td>
 *     <td headers="matches">换页符 (<tt>'&#92;u000C'</tt>)</td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>\a</tt></td>
 *     <td headers="matches">警报 (响铃) 字符 (<tt>'&#92;u0007'</tt>)</td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>\e</tt></td>
 *     <td headers="matches">转义字符 (<tt>'&#92;u001B'</tt>)</td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>\c</tt><i>x</i></td>
 *     <td headers="matches">与 <i>x</i> 对应的控制字符</td></tr>
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="classes">字符类</th></tr>
 *
 * <tr><td valign="top" headers="construct classes">{@code [abc]}</td>
 *     <td headers="matches">{@code a}, {@code b}, 或 {@code c} (简单类)</td></tr>
 * <tr><td valign="top" headers="construct classes">{@code [^abc]}</td>
 *     <td headers="matches">任何不是 {@code a}, {@code b}, 或 {@code c} 的字符 (否定)</td></tr>
 * <tr><td valign="top" headers="construct classes">{@code [a-zA-Z]}</td>
 *     <td headers="matches">{@code a} 到 {@code z}
 *         或 {@code A} 到 {@code Z}，包括 (范围)</td></tr>
 * <tr><td valign="top" headers="construct classes">{@code [a-d[m-p]]}</td>
 *     <td headers="matches">{@code a} 到 {@code d}，
 *      或 {@code m} 到 {@code p}: {@code [a-dm-p]} (并集)</td></tr>
 * <tr><td valign="top" headers="construct classes">{@code [a-z&&[def]]}</td>
 *     <td headers="matches">{@code d}, {@code e}, 或 {@code f} (交集)</tr>
 * <tr><td valign="top" headers="construct classes">{@code [a-z&&[^bc]]}</td>
 *     <td headers="matches">{@code a} 到 {@code z}，
 *         除了 {@code b} 和 {@code c}: {@code [ad-z]} (差集)</td></tr>
 * <tr><td valign="top" headers="construct classes">{@code [a-z&&[^m-p]]}</td>
 *     <td headers="matches">{@code a} 到 {@code z}，
 *          且不是 {@code m} 到 {@code p}: {@code [a-lq-z]}(差集)</td></tr>
 * <tr><th>&nbsp;</th></tr>
 *
 * <tr align="left"><th colspan="2" id="predef">预定义字符类</th></tr>
 *
 * <tr><td valign="top" headers="construct predef"><tt>.</tt></td>
 *     <td headers="matches">任何字符 (可能匹配或不匹配 <a href="#lt">行终止符</a>)</td></tr>
 * <tr><td valign="top" headers="construct predef"><tt>\d</tt></td>
 *     <td headers="matches">一个数字: <tt>[0-9]</tt></td></tr>
 * <tr><td valign="top" headers="construct predef"><tt>\D</tt></td>
 *     <td headers="matches">一个非数字: <tt>[^0-9]</tt></td></tr>
 * <tr><td valign="top" headers="construct predef"><tt>\h</tt></td>
 *     <td headers="matches">一个水平空白字符:
 *     <tt>[ \t\xA0&#92;u1680&#92;u180e&#92;u2000-&#92;u200a&#92;u202f&#92;u205f&#92;u3000]</tt></td></tr>
 * <tr><td valign="top" headers="construct predef"><tt>\H</tt></td>
 *     <td headers="matches">一个非水平空白字符: <tt>[^\h]</tt></td></tr>
 * <tr><td valign="top" headers="construct predef"><tt>\s</tt></td>
 *     <td headers="matches">一个空白字符: <tt>[ \t\n\x0B\f\r]</tt></td></tr>
 * <tr><td valign="top" headers="construct predef"><tt>\S</tt></td>
 *     <td headers="matches">一个非空白字符: <tt>[^\s]</tt></td></tr>
 * <tr><td valign="top" headers="construct predef"><tt>\v</tt></td>
 *     <td headers="matches">一个垂直空白字符: <tt>[\n\x0B\f\r\x85&#92;u2028&#92;u2029]</tt>
 *     </td></tr>
 * <tr><td valign="top" headers="construct predef"><tt>\V</tt></td>
 *     <td headers="matches">一个非垂直空白字符: <tt>[^\v]</tt></td></tr>
 * <tr><td valign="top" headers="construct predef"><tt>\w</tt></td>
 *     <td headers="matches">一个单词字符: <tt>[a-zA-Z_0-9]</tt></td></tr>
 * <tr><td valign="top" headers="construct predef"><tt>\W</tt></td>
 *     <td headers="matches">一个非单词字符: <tt>[^\w]</tt></td></tr>
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="posix"><b>POSIX 字符类 (仅限 US-ASCII)</b></th></tr>
 *
 * <tr><td valign="top" headers="construct posix">{@code \p{Lower}}</td>
 *     <td headers="matches">一个小写字母: {@code [a-z]}</td></tr>
 * <tr><td valign="top" headers="construct posix">{@code \p{Upper}}</td>
 *     <td headers="matches">一个大写字母:{@code [A-Z]}</td></tr>
 * <tr><td valign="top" headers="construct posix">{@code \p{ASCII}}</td>
 *     <td headers="matches">所有 ASCII:{@code [\x00-\x7F]}</td></tr>
 * <tr><td valign="top" headers="construct posix">{@code \p{Alpha}}</td>
 *     <td headers="matches">一个字母字符:{@code [\p{Lower}\p{Upper}]}</td></tr>
 * <tr><td valign="top" headers="construct posix">{@code \p{Digit}}</td>
 *     <td headers="matches">一个十进制数字: {@code [0-9]}</td></tr>
 * <tr><td valign="top" headers="construct posix">{@code \p{Alnum}}</td>
 *     <td headers="matches">一个字母数字字符:{@code [\p{Alpha}\p{Digit}]}</td></tr>
 * <tr><td valign="top" headers="construct posix">{@code \p{Punct}}</td>
 *     <td headers="matches">标点符号: 其中一个 {@code !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~}</td></tr>
 *     <!-- {@code [\!"#\$%&'\(\)\*\+,\-\./:;\<=\>\?@\[\\\]\^_`\{\|\}~]}
 *          {@code [\X21-\X2F\X31-\X40\X5B-\X60\X7B-\X7E]} -->
 * <tr><td valign="top" headers="construct posix">{@code \p{Graph}}</td>
 *     <td headers="matches">一个可见字符: {@code [\p{Alnum}\p{Punct}]}</td></tr>
 * <tr><td valign="top" headers="construct posix">{@code \p{Print}}</td>
 *     <td headers="matches">一个可打印字符: {@code [\p{Graph}\x20]}</td></tr>
 * <tr><td valign="top" headers="construct posix">{@code \p{Blank}}</td>
 *     <td headers="matches">一个空格或一个制表符: {@code [ \t]}</td></tr>
 * <tr><td valign="top" headers="construct posix">{@code \p{Cntrl}}</td>
 *     <td headers="matches">一个控制字符: {@code [\x00-\x1F\x7F]}</td></tr>
 * <tr><td valign="top" headers="construct posix">{@code \p{XDigit}}</td>
 *     <td headers="matches">一个十六进制数字: {@code [0-9a-fA-F]}</td></tr>
 * <tr><td valign="top" headers="construct posix">{@code \p{Space}}</td>
 *     <td headers="matches">一个空白字符: {@code [ \t\n\x0B\f\r]}</td></tr>
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2">java.lang.Character 类 (简单的 <a href="#jcc">java 字符类型</a>)</th></tr>
 *
 * <tr><td valign="top"><tt>\p{javaLowerCase}</tt></td>
 *     <td>等效于 java.lang.Character.isLowerCase()</td></tr>
 * <tr><td valign="top"><tt>\p{javaUpperCase}</tt></td>
 *     <td>等效于 java.lang.Character.isUpperCase()</td></tr>
 * <tr><td valign="top"><tt>\p{javaWhitespace}</tt></td>
 *     <td>等效于 java.lang.Character.isWhitespace()</td></tr>
 * <tr><td valign="top"><tt>\p{javaMirrored}</tt></td>
 *     <td>等效于 java.lang.Character.isMirrored()</td></tr>
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="unicode">Unicode 脚本、块、类别和二进制属性的类</th></tr>
 * <tr><td valign="top" headers="construct unicode">{@code \p{IsLatin}}</td>
 *     <td headers="matches">一个拉丁脚本字符 (<a href="#usc">脚本</a>)</td></tr>
 * <tr><td valign="top" headers="construct unicode">{@code \p{InGreek}}</td>
 *     <td headers="matches">一个希腊块中的字符 (<a href="#ubc">块</a>)</td></tr>
 * <tr><td valign="top" headers="construct unicode">{@code \p{Lu}}</td>
 *     <td headers="matches">一个大写字母 (<a href="#ucc">类别</a>)</td></tr>
 * <tr><td valign="top" headers="construct unicode">{@code \p{IsAlphabetic}}</td>
 *     <td headers="matches">一个字母字符 (<a href="#ubpc">二进制属性</a>)</td></tr>
 * <tr><td valign="top" headers="construct unicode">{@code \p{Sc}}</td>
 *     <td headers="matches">一个货币符号</td></tr>
 * <tr><td valign="top" headers="construct unicode">{@code \P{InGreek}}</td>
 *     <td headers="matches">任何不是希腊块中的字符 (否定)</td></tr>
 * <tr><td valign="top" headers="construct unicode">{@code [\p{L}&&[^\p{Lu}]]}</td>
 *     <td headers="matches">任何不是大写字母的字母 (差集)</td></tr>
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="bounds">边界匹配器</th></tr>
 *
 * <tr><td valign="top" headers="construct bounds"><tt>^</tt></td>
 *     <td headers="matches">行的开始</td></tr>
 * <tr><td valign="top" headers="construct bounds"><tt>$</tt></td>
 *     <td headers="matches">行的结束</td></tr>
 * <tr><td valign="top" headers="construct bounds"><tt>\b</tt></td>
 *     <td headers="matches">一个单词边界</td></tr>
 * <tr><td valign="top" headers="construct bounds"><tt>\B</tt></td>
 *     <td headers="matches">一个非单词边界</td></tr>
 * <tr><td valign="top" headers="construct bounds"><tt>\A</tt></td>
 *     <td headers="matches">输入的开始</td></tr>
 * <tr><td valign="top" headers="construct bounds"><tt>\G</tt></td>
 *     <td headers="matches">上一个匹配的结束</td></tr>
 * <tr><td valign="top" headers="construct bounds"><tt>\Z</tt></td>
 *     <td headers="matches">输入的结束，但不包括最终的
 *         <a href="#lt">终止符</a>，如果有的话</td></tr>
 * <tr><td valign="top" headers="construct bounds"><tt>\z</tt></td>
 *     <td headers="matches">输入的结束</td></tr>
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="lineending">行终止符匹配器</th></tr>
 * <tr><td valign="top" headers="construct lineending"><tt>\R</tt></td>
 *     <td headers="matches">任何 Unicode 行终止符序列，等效于
 *     <tt>&#92;u000D&#92;u000A|[&#92;u000A&#92;u000B&#92;u000C&#92;u000D&#92;u0085&#92;u2028&#92;u2029]
 *     </tt></td></tr>
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="greedy">贪婪量词</th></tr>
 *
 * <tr><td valign="top" headers="construct greedy"><i>X</i><tt>?</tt></td>
 *     <td headers="matches"><i>X</i>，一次或不出现</td></tr>
 * <tr><td valign="top" headers="construct greedy"><i>X</i><tt>*</tt></td>
 *     <td headers="matches"><i>X</i>，零次或多次</td></tr>
 * <tr><td valign="top" headers="construct greedy"><i>X</i><tt>+</tt></td>
 *     <td headers="matches"><i>X</i>，一次或多次</td></tr>
 * <tr><td valign="top" headers="construct greedy"><i>X</i><tt>{</tt><i>n</i><tt>}</tt></td>
 *     <td headers="matches"><i>X</i>，恰好 <i>n</i> 次</td></tr>
 * <tr><td valign="top" headers="construct greedy"><i>X</i><tt>{</tt><i>n</i><tt>,}</tt></td>
 *     <td headers="matches"><i>X</i>，至少 <i>n</i> 次</td></tr>
 * <tr><td valign="top" headers="construct greedy"><i>X</i><tt>{</tt><i>n</i><tt>,</tt><i>m</i><tt>}</tt></td>
 *     <td headers="matches"><i>X</i>，至少 <i>n</i> 次但不超过 <i>m</i> 次</td></tr>
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="reluc">不贪婪量词</th></tr>
 *
 * <tr><td valign="top" headers="construct reluc"><i>X</i><tt>??</tt></td>
 *     <td headers="matches"><i>X</i>，一次或不出现</td></tr>
 * <tr><td valign="top" headers="construct reluc"><i>X</i><tt>*?</tt></td>
 *     <td headers="matches"><i>X</i>，零次或多次</td></tr>
 * <tr><td valign="top" headers="construct reluc"><i>X</i><tt>+?</tt></td>
 *     <td headers="matches"><i>X</i>，一次或多次</td></tr>
 * <tr><td valign="top" headers="construct reluc"><i>X</i><tt>{</tt><i>n</i><tt>}?</tt></td>
 *     <td headers="matches"><i>X</i>，恰好 <i>n</i> 次</td></tr>
 * <tr><td valign="top" headers="construct reluc"><i>X</i><tt>{</tt><i>n</i><tt>,}?</tt></td>
 *     <td headers="matches"><i>X</i>，至少 <i>n</i> 次</td></tr>
 * <tr><td valign="top" headers="construct reluc"><i>X</i><tt>{</tt><i>n</i><tt>,</tt><i>m</i><tt>}?</tt></td>
 *     <td headers="matches"><i>X</i>，至少 <i>n</i> 次但不超过 <i>m</i> 次</td></tr>
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="poss">占有量词</th></tr>
 *
 * <tr><td valign="top" headers="construct poss"><i>X</i><tt>?+</tt></td>
 *     <td headers="matches"><i>X</i>，一次或不出现</td></tr>
 * <tr><td valign="top" headers="construct poss"><i>X</i><tt>*+</tt></td>
 *     <td headers="matches"><i>X</i>，零次或多次</td></tr>
 * <tr><td valign="top" headers="construct poss"><i>X</i><tt>++</tt></td>
 *     <td headers="matches"><i>X</i>，一次或多次</td></tr>
 * <tr><td valign="top" headers="construct poss"><i>X</i><tt>{</tt><i>n</i><tt>}+</tt></td>
 *     <td headers="matches"><i>X</i>，恰好 <i>n</i> 次</td></tr>
 * <tr><td valign="top" headers="construct poss"><i>X</i><tt>{</tt><i>n</i><tt>,}+</tt></td>
 *     <td headers="matches"><i>X</i>，至少 <i>n</i> 次</td></tr>
 * <tr><td valign="top" headers="construct poss"><i>X</i><tt>{</tt><i>n</i><tt>,</tt><i>m</i><tt>}+</tt></td>
 *     <td headers="matches"><i>X</i>，至少 <i>n</i> 次但不超过 <i>m</i> 次</td></tr>
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="logical">逻辑运算符</th></tr>
 *
 * <tr><td valign="top" headers="construct logical"><i>XY</i></td>
 *     <td headers="matches"><i>X</i> 后跟 <i>Y</i></td></tr>
 * <tr><td valign="top" headers="construct logical"><i>X</i><tt>|</tt><i>Y</i></td>
 *     <td headers="matches"><i>X</i> 或 <i>Y</i></td></tr>
 * <tr><td valign="top" headers="construct logical"><tt>(</tt><i>X</i><tt>)</tt></td>
 *     <td headers="matches">X，作为一个 <a href="#cg">捕获组</a></td></tr>
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="backref">回溯引用</th></tr>
 *
 * <tr><td valign="bottom" headers="construct backref"><tt>\</tt><i>n</i></td>
 *     <td valign="bottom" headers="matches">第 <i>n</i> 个 <a href="#cg">捕获组</a> 匹配的内容</td></tr>
 *
 * <tr><td valign="bottom" headers="construct backref"><tt>\</tt><i>k</i>&lt;<i>name</i>&gt;</td>
 *     <td valign="bottom" headers="matches">名为 "name" 的 <a href="#groupname">命名捕获组</a> 匹配的内容</td></tr>
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="quot">引用</th></tr>
 *
 * <tr><td valign="top" headers="construct quot"><tt>\</tt></td>
 *     <td headers="matches">无，但引用后一个字符</td></tr>
 * <tr><td valign="top" headers="construct quot"><tt>\Q</tt></td>
 *     <td headers="matches">无，但引用所有字符直到 <tt>\E</tt></td></tr>
 * <tr><td valign="top" headers="construct quot"><tt>\E</tt></td>
 *     <td headers="matches">无，但结束由 <tt>\Q</tt> 开始的引用</td></tr>
 *     <!-- Metachars: !$()*+.<>?[\]^{|} -->
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="special">特殊构造 (命名捕获和非捕获)</th></tr>
 *
 * <tr><td valign="top" headers="construct special"><tt>(?&lt;<a href="#groupname">name</a>&gt;</tt><i>X</i><tt>)</tt></td>
 *     <td headers="matches"><i>X</i>，作为一个命名捕获组</td></tr>
 * <tr><td valign="top" headers="construct special"><tt>(?:</tt><i>X</i><tt>)</tt></td>
 *     <td headers="matches"><i>X</i>，作为一个非捕获组</td></tr>
 * <tr><td valign="top" headers="construct special"><tt>(?idmsuxU-idmsuxU)&nbsp;</tt></td>
 *     <td headers="matches">无，但开启或关闭匹配标志 <a href="#CASE_INSENSITIVE">i</a>
 * <a href="#UNIX_LINES">d</a> <a href="#MULTILINE">m</a> <a href="#DOTALL">s</a>
 * <a href="#UNICODE_CASE">u</a> <a href="#COMMENTS">x</a> <a href="#UNICODE_CHARACTER_CLASS">U</a>
 * </td></tr>
 * <tr><td valign="top" headers="construct special"><tt>(?idmsux-idmsux:</tt><i>X</i><tt>)</tt>&nbsp;&nbsp;</td>
 *     <td headers="matches"><i>X</i>，作为一个 <a href="#cg">非捕获组</a>，并开启或关闭给定的标志 <a href="#CASE_INSENSITIVE">i</a> <a href="#UNIX_LINES">d</a>
 * <a href="#MULTILINE">m</a> <a href="#DOTALL">s</a> <a href="#UNICODE_CASE">u</a >
 * <a href="#COMMENTS">x</a> </td></tr>
 * <tr><td valign="top" headers="construct special"><tt>(?=</tt><i>X</i><tt>)</tt></td>
 *     <td headers="matches"><i>X</i>，通过零宽度正向预查</td></tr>
 * <tr><td valign="top" headers="construct special"><tt>(?!</tt><i>X</i><tt>)</tt></td>
 *     <td headers="matches"><i>X</i>，通过零宽度负向预查</td></tr>
 * <tr><td valign="top" headers="construct special"><tt>(?&lt;=</tt><i>X</i><tt>)</tt></td>
 *     <td headers="matches"><i>X</i>，通过零宽度正向后查</td></tr>
 * <tr><td valign="top" headers="construct special"><tt>(?&lt;!</tt><i>X</i><tt>)</tt></td>
 *     <td headers="matches"><i>X</i>，通过零宽度负向后查</td></tr>
 * <tr><td valign="top" headers="construct special"><tt>(?&gt;</tt><i>X</i><tt>)</tt></td>
 *     <td headers="matches"><i>X</i>，作为一个独立的非捕获组</td></tr>
 *
 * </table>
 *
 * <hr>
 *
 *
 * <h3><a name="bs">反斜杠、转义和引用</a></h3>
 *
 * <p> 反斜杠字符 (<tt>'\'</tt>) 用于引入转义构造，如上表所述，以及引用其他可能被解释为未转义构造的字符。因此，表达式 <tt>\\</tt> 匹配一个单独的反斜杠，而 <tt>\{</tt> 匹配一个左大括号。
 *
 * <p> 在任何字母字符前使用反斜杠是错误的，除非它表示一个转义构造；这些保留用于未来对正则表达式语言的扩展。反斜杠可以在非字母字符前使用，无论该字符是否是未转义构造的一部分。
 *
 * <p> Java 源代码中的字符串字面量中的反斜杠根据
 * <cite>The Java&trade; Language Specification</cite>
 * 被解释为 Unicode 转义 (第 3.3 节) 或其他字符转义 (第 3.10.6 节)。因此，必须在字符串字面量中加倍反斜杠以保护它们免受 Java 字节码编译器的解释。例如，字符串字面量
 * <tt>"&#92;b"</tt> 作为正则表达式时匹配一个单独的退格字符，而 <tt>"&#92;&#92;b"</tt> 匹配一个单词边界。字符串字面量 <tt>"&#92;(hello&#92;)"</tt> 是非法的
 * 并导致编译时错误；为了匹配字符串
 * <tt>(hello)</tt>，必须使用字符串字面量 <tt>"&#92;&#92;(hello&#92;&#92;)"</tt>。
 *
 * <h3><a name="cc">字符类</a></h3>
 *
 *    <p> 字符类可以在其他字符类中出现，并且可以通过并集运算符 (隐式) 和交集运算符 (<tt>&amp;&amp;</tt>) 组合。并集运算符表示一个包含其至少一个操作数类中每个字符的类。交集运算符
 *    表示一个包含其两个操作数类中每个字符的类。
 *
 *    <p> 字符类运算符的优先级如下，从最高到最低：
 *
 *    <blockquote><table border="0" cellpadding="1" cellspacing="0"
 *                 summary="字符类运算符的优先级。">
 *      <tr><th>1&nbsp;&nbsp;&nbsp;&nbsp;</th>
 *        <td>字面量转义&nbsp;&nbsp;&nbsp;&nbsp;</td>
 *        <td><tt>\x</tt></td></tr>
 *     <tr><th>2&nbsp;&nbsp;&nbsp;&nbsp;</th>
 *        <td>分组</td>
 *        <td><tt>[...]</tt></td></tr>
 *     <tr><th>3&nbsp;&nbsp;&nbsp;&nbsp;</th>
 *        <td>范围</td>
 *        <td><tt>a-z</tt></td></tr>
 *      <tr><th>4&nbsp;&nbsp;&nbsp;&nbsp;</th>
 *        <td>并集</td>
 *        <td><tt>[a-e][i-u]</tt></td></tr>
 *      <tr><th>5&nbsp;&nbsp;&nbsp;&nbsp;</th>
 *        <td>交集</td>
 *        <td>{@code [a-z&&[aeiou]]}</td></tr>
 *    </table></blockquote>
 *
 *    <p> 注意，在字符类中，一组不同的元字符生效，而在字符类之外则不同。例如，正则表达式 <tt>.</tt> 在字符类中失去其特殊含义，而表达式 <tt>-</tt> 成为范围形成元字符。
 *
 * <h3><a name="lt">行终止符</a></h3>
 *
 * <p> <i>行终止符</i> 是一个或两个字符序列，标记输入字符序列的行结束。以下被识别为行终止符：
 *
 * <ul>
 *
 *   <li> 换行符 (行进符) 字符&nbsp;(<tt>'\n'</tt>),
 *
 *   <li> 回车符后紧跟一个换行符
 *   字符&nbsp;(<tt>"\r\n"</tt>),
 *
 *   <li> 单独的回车符&nbsp;(<tt>'\r'</tt>),
 *
 *   <li> 换行符&nbsp;(<tt>'&#92;u0085'</tt>),
 *
 *   <li> 行分隔符字符&nbsp;(<tt>'&#92;u2028'</tt>), 或
 *
 *   <li> 段落分隔符字符&nbsp;(<tt>'&#92;u2029</tt>)。
 *
 * </ul>
 * <p>如果激活了 {@link #UNIX_LINES} 模式，则仅识别换行符。
 *
 * <p> 除非指定了 {@link #DOTALL} 标志，否则正则表达式 <tt>.</tt> 匹配任何字符，但不匹配行终止符。
 *
 * <p> 默认情况下，正则表达式 <tt>^</tt> 和 <tt>$</tt> 忽略行终止符，仅在输入序列的开始和结束处匹配。如果激活了 {@link #MULTILINE} 模式，则
 * <tt>^</tt> 在输入的开始和任何行终止符后匹配 (但不在输入的结束处)。当处于 {@link #MULTILINE} 模式时，<tt>$</tt>
 * 在行终止符前或输入序列的结束处匹配。
 *
 * <h3><a name="cg">组和捕获</a></h3>
 *
 * <h4><a name="gnumber">组号</a></h4>
 * <p> 捕获组通过从左到右计数其左括号来编号。例如，在表达式 <tt>((A)(B(C)))</tt> 中，有四个这样的组： </p>
 *
 * <blockquote><table cellpadding=1 cellspacing=0 summary="捕获组编号">
 * <tr><th>1&nbsp;&nbsp;&nbsp;&nbsp;</th>
 *     <td><tt>((A)(B(C)))</tt></td></tr>
 * <tr><th>2&nbsp;&nbsp;&nbsp;&nbsp;</th>
 *     <td><tt>(A)</tt></td></tr>
 * <tr><th>3&nbsp;&nbsp;&nbsp;&nbsp;</th>
 *     <td><tt>(B(C))</tt></td></tr>
 * <tr><th>4&nbsp;&nbsp;&nbsp;&nbsp;</th>
 *     <td><tt>(C)</tt></td></tr>
 * </table></blockquote>
 *
 * <p> 组零始终代表整个表达式。
 *
 * <p> 捕获组之所以称为捕获组，是因为在匹配过程中，每个子序列的输入序列与这样的组匹配时都会被保存。捕获的子序列可以稍后在表达式中通过回溯引用使用，并且也可以在匹配操作完成后从匹配器中检索。
 *
 * <h4><a name="groupname">组名</a></h4>
 * <p> 捕获组也可以分配一个 "名称"，一个 <tt>命名捕获组</tt>，然后可以通过 "名称" 进行回溯引用。组名由以下字符组成。第一个字符必须是 <tt>字母</tt>。
 *
 * <ul>
 *   <li> 大写字母 <tt>'A'</tt> 到 <tt>'Z'</tt>
 *        (<tt>'&#92;u0041'</tt>&nbsp;到&nbsp;<tt>'&#92;u005a'</tt>),
 *   <li> 小写字母 <tt>'a'</tt> 到 <tt>'z'</tt>
 *        (<tt>'&#92;u0061'</tt>&nbsp;到&nbsp;<tt>'&#92;u007a'</tt>),
 *   <li> 数字 <tt>'0'</tt> 到 <tt>'9'</tt>
 *        (<tt>'&#92;u0030'</tt>&nbsp;到&nbsp;<tt>'&#92;u0039'</tt>),
 * </ul>
 *
 * <p> 一个 <tt>命名捕获组</tt> 仍然按照 <a href="#gnumber">组号</a> 中描述的方式编号。
 *
 * <p> 与组关联的捕获输入始终是该组最近匹配的子序列。如果由于量词评估组第二次，则其先前捕获的值（如果有）将在第二次评估失败时保留。例如，匹配字符串
 * <tt>"aba"</tt> 与表达式 <tt>(a(b)?)+</tt> 会将组二设置为 <tt>"b"</tt>。所有捕获的输入在每次匹配开始时都会被丢弃。
 *
 * <p> 以 <tt>(?</tt> 开头的组要么是纯粹的、<i>非捕获</i> 组，不捕获文本且不计入组总数，要么是 <i>命名捕获</i> 组。
 *
 * <h3> Unicode 支持 </h3>
 *
 * <p> 该类符合 <a
 * href="http://www.unicode.org/reports/tr18/"><i>Unicode Technical
 * Standard #18: Unicode Regular Expression</i></a> 的 Level 1，加上 RL2.1
 * Canonical Equivalents。
 * <p>
 * <b>Unicode 转义序列</b> 如 <tt>&#92;u2014</tt> 在 Java 源代码中
 * 按照 <cite>The Java&trade; Language Specification</cite> 的第 3.3 节进行处理。
 * 这样的转义序列也直接由正则表达式解析器实现，因此可以在从文件或键盘读取的表达式中使用 Unicode 转义。因此，字符串 <tt>"&#92;u2014"</tt> 和
 * <tt>"\\u2014"</tt> 虽然不相等，但编译成相同的模式，匹配十六进制值为 <tt>0x2014</tt> 的字符。
 * <p>
 * 一个 Unicode 字符也可以通过其 <b>十六进制表示法</b>（十六进制代码点值）直接在正则表达式中表示，如构造
 * <tt>&#92;x{...}</tt> 所述，例如补充字符 U+2011F
 * 可以指定为 <tt>&#92;x{2011F}</tt>，而不是两个连续的 Unicode 转义序列
 * <tt>&#92;uD840</tt><tt>&#92;uDD1F</tt>。
 * <p>
 * Unicode 脚本、块、类别和二进制属性使用
 * <tt>\p</tt> 和 <tt>\P</tt> 构造，如 Perl 中的那样。
 * <tt>\p{</tt><i>prop</i><tt>}</tt> 匹配输入具有属性 <i>prop</i>，而 <tt>\P{</tt><i>prop</i><tt>}</tt>
 * 匹配输入不具有该属性。
 * <p>
 * 脚本、块、类别和二进制属性可以在字符类内外使用。
 *
 * <p>
 * <b><a name="usc">脚本</a></b> 可以用前缀 {@code Is} 指定，如
 * {@code IsHiragana}，也可以使用  <a href="#sc">脚本</a> 关键字 (或其短形式 {@code sc}) 如
 * {@code script=Hiragana} 或 {@code sc=Hiragana}。
 * <p>
 * 由 <code>Pattern</code> 支持的脚本名称是
 * {@link java.lang.Character.UnicodeScript#forName(String) UnicodeScript.forName} 接受和定义的有效脚本名称。
 *
 * <p>
 * <b><a name="ubc">块</a></b> 可以用前缀 {@code In} 指定，如
 * {@code InMongolian}，也可以使用 <a href="#blk">块</a> 关键字 (或其短形式 {@code blk}) 如
 * {@code block=Mongolian} 或 {@code blk=Mongolian}。
 * <p>
 * 由 <code>Pattern</code> 支持的块名称是
 * {@link java.lang.Character.UnicodeBlock#forName(String) UnicodeBlock.forName} 接受和定义的有效块名称。
 * <p>
 *
 * <b><a name="ucc">类别</a></b> 可以用可选的前缀 {@code Is} 指定：
 * 两者 {@code \p{L}} 和 {@code \p{IsL}} 都表示 Unicode
 * 字母类别。与脚本和块一样，类别也可以使用
 * <a href="#gc">类别</a> 关键字 (或其短形式
 * {@code gc}) 如 {@code general_category=Lu} 或 {@code gc=Lu} 指定。
 * <p>
 * 支持的类别是
 * <a href="http://www.unicode.org/unicode/standard/standard.html">
 * <i>The Unicode Standard</i></a> 在
 * {@link java.lang.Character Character} 类指定的版本中定义的类别。类别名称是标准中定义的，包括规范的和信息性的。
 * <p>
 *
 * <b><a name="ubpc">二进制属性</a></b> 用前缀 {@code Is} 指定，如
 * {@code IsAlphabetic}。由 <code>Pattern</code> 支持的二进制属性有
 * <ul>
 *   <li> Alphabetic
 *   <li> Ideographic
 *   <li> Letter
 *   <li> Lowercase
 *   <li> Uppercase
 *   <li> Titlecase
 *   <li> Punctuation
 *   <Li> Control
 *   <li> White_Space
 *   <li> Digit
 *   <li> Hex_Digit
 *   <li> Join_Control
 *   <li> Noncharacter_Code_Point
 *   <li> Assigned
 * </ul>
 * <p>
 * 以下 <b>预定义字符类</b> 和 <b>POSIX 字符类</b>
 * 在指定 {@link #UNICODE_CHARACTER_CLASS} 标志时符合 <i>Annex C: Compatibility Properties</i>
 * 的建议。
 *
 * <table border="0" cellpadding="1" cellspacing="0"
 *  summary="Unicode 模式下的预定义和 POSIX 字符类">
 * <tr align="left">
 * <th align="left" id="predef_classes">类</th>
 * <th align="left" id="predef_matches">匹配</th>
 *</tr>
 * <tr><td><tt>\p{Lower}</tt></td>
 *     <td>一个小写字母:<tt>\p{IsLowercase}</tt></td></tr>
 * <tr><td><tt>\p{Upper}</tt></td>
 *     <td>一个大写字母:<tt>\p{IsUppercase}</tt></td></tr>
 * <tr><td><tt>\p{ASCII}</tt></td>
 *     <td>所有 ASCII:<tt>[\x00-\x7F]</tt></td></tr>
 * <tr><td><tt>\p{Alpha}</tt></td>
 *     <td>一个字母字符:<tt>\p{IsAlphabetic}</tt></td></tr>
 * <tr><td><tt>\p{Digit}</tt></td>
 *     <td>一个十进制数字字符:<tt>p{IsDigit}</tt></td></tr>
 * <tr><td><tt>\p{Alnum}</tt></td>
 *     <td>一个字母数字字符:<tt>[\p{IsAlphabetic}\p{IsDigit}]</tt></td></tr>
 * <tr><td><tt>\p{Punct}</tt></td>
 *     <td>一个标点符号字符:<tt>p{IsPunctuation}</tt></td></tr>
 * <tr><td><tt>\p{Graph}</tt></td>
 *     <td>一个可见字符: <tt>[^\p{IsWhite_Space}\p{gc=Cc}\p{gc=Cs}\p{gc=Cn}]</tt></td></tr>
 * <tr><td><tt>\p{Print}</tt></td>
 *     <td>一个可打印字符: {@code [\p{Graph}\p{Blank}&&[^\p{Cntrl}]]}</td></tr>
 * <tr><td><tt>\p{Blank}</tt></td>
 *     <td>一个空格或一个制表符: {@code [\p{IsWhite_Space}&&[^\p{gc=Zl}\p{gc=Zp}\x0a\x0b\x0c\x0d\x85]]}</td></tr>
 * <tr><td><tt>\p{Cntrl}</tt></td>
 *     <td>一个控制字符: <tt>\p{gc=Cc}</tt></td></tr>
 * <tr><td><tt>\p{XDigit}</tt></td>
 *     <td>一个十六进制数字: <tt>[\p{gc=Nd}\p{IsHex_Digit}]</tt></td></tr>
 * <tr><td><tt>\p{Space}</tt></td>
 *     <td>一个空白字符:<tt>\p{IsWhite_Space}</tt></td></tr>
 * <tr><td><tt>\d</tt></td>
 *     <td>一个数字: <tt>\p{IsDigit}</tt></td></tr>
 * <tr><td><tt>\D</tt></td>
 *     <td>一个非数字: <tt>[^\d]</tt></td></tr>
 * <tr><td><tt>\s</tt></td>
 *     <td>一个空白字符: <tt>\p{IsWhite_Space}</tt></td></tr>
 * <tr><td><tt>\S</tt></td>
 *     <td>一个非空白字符: <tt>[^\s]</tt></td></tr>
 * <tr><td><tt>\w</tt></td>
 *     <td>一个单词字符: <tt>[\p{Alpha}\p{gc=Mn}\p{gc=Me}\p{gc=Mc}\p{Digit}\p{gc=Pc}\p{IsJoin_Control}]</tt></td></tr>
 * <tr><td><tt>\W</tt></td>
 *     <td>一个非单词字符: <tt>[^\w]</tt></td></tr>
 * </table>
 * <p>
 * <a name="jcc">
 * 行为类似于 java.lang.Character
 * 布尔 is<i>methodname</i> 方法 (不包括已弃用的方法) 的类别
 * 可以通过相同的 <tt>\p{</tt><i>prop</i><tt>}</tt> 语法使用，其中指定的属性名称为 <tt>java<i>methodname</i></tt></a>。
 *
 * <h3> 与 Perl 5 的比较 </h3>
 *
 * <p> <code>Pattern</code> 引擎执行传统的 NFA 基于有序交替的匹配，如 Perl 5 中的那样。
 *
 * <p> Perl 中不支持的构造： </p>
 *
 * <ul>
 *    <li><p> 预定义字符类 (Unicode 字符)
 *    <p><tt>\X&nbsp;&nbsp;&nbsp;&nbsp;</tt> 匹配 Unicode
 *    <a href="http://www.unicode.org/reports/tr18/#Default_Grapheme_Clusters">
 *    <i>扩展图形群集</i></a>
 *    </p></li>
 *
 *    <li><p> 回溯引用构造，<tt>\g{</tt><i>n</i><tt>}</tt> 用于第 <i>n</i> 个 <a href="#cg">捕获组</a> 和
 *    <tt>\g{</tt><i>name</i><tt>}</tt> 用于
 *    <a href="#groupname">命名捕获组</a>。
 *    </p></li>
 *
 *    <li><p> 命名字符构造，<tt>\N{</tt><i>name</i><tt>}</tt>
 *    用于通过名称指定的 Unicode 字符。
 *    </p></li>
 *
 *    <li><p> 条件构造
 *    <tt>(?(</tt><i>condition</i><tt>)</tt><i>X</i><tt>)</tt> 和
 *    <tt>(?(</tt><i>condition</i><tt>)</tt><i>X</i><tt>|</tt><i>Y</i><tt>)</tt>,
 *    </p></li>
 *
 *    <li><p> 嵌入代码构造 <tt>(?{</tt><i>code</i><tt>})</tt>
 *    和 <tt>(??{</tt><i>code</i><tt>})</tt>,</p></li>
 *
 *    <li><p> 嵌入注释语法 <tt>(?#comment)</tt>, 和 </p></li>
 *
 *    <li><p> 预处理操作 <tt>\l</tt> <tt>&#92;u</tt>,
 *    <tt>\L</tt>, 和 <tt>\U</tt>.  </p></li>
 *
 * </ul>
 *
 * <p> 由该类支持但 Perl 不支持的构造： </p>
 *
 * <ul>
 *
 *    <li><p> 字符类并集和交集，如 <a href="#cc">上文</a> 所述。</p></li>
 *
 * </ul>
 *
 * <p> 与 Perl 的显著差异： </p>
 *
 * <ul>
 *
 *    <li><p> 在 Perl 中，<tt>\1</tt> 到 <tt>\9</tt> 始终被解释为回溯引用；大于 <tt>9</tt> 的转义数字如果至少有那么多子表达式，则被解释为回溯引用，
 *    否则被解释为八进制转义。在该类中，八进制转义必须始终以零开头。在该类中，
 *    <tt>\1</tt> 到 <tt>\9</tt> 始终被解释为回溯引用，如果在正则表达式中至少有那么多子表达式，则接受更大的数字，否则解析器将删除数字，直到数字小于或等于现有组数或为一位数。
 *    </p></li>
 *
 *    <li><p> Perl 使用 <tt>g</tt> 标志请求从上次匹配结束处恢复的匹配。此功能由 {@link Matcher} 类隐式提供：重复调用 {@link
 *    Matcher#find find} 方法将从上次匹配结束处恢复，除非匹配器被重置。 </p></li>
 *
 *    <li><p> 在 Perl 中，嵌入的标志在表达式的顶层影响整个表达式。在该类中，嵌入的标志始终在出现的位置生效，无论它们是在顶层还是在组内；在后一种情况下，标志在组结束时恢复，就像在 Perl 中一样。 </p></li>
 *
 * </ul>
 *
 *
 * <p> 有关正则表达式构造行为的更精确描述，请参阅 <a href="http://www.oreilly.com/catalog/regex3/">
 * <i>Mastering Regular Expressions, 3nd Edition</i>, Jeffrey E. F. Friedl,
 * O'Reilly and Associates, 2006.</a>
 * </p>
 *
 * @see java.lang.String#split(String, int)
 * @see java.lang.String#split(String)
 *
 * @author      Mike McCloskey
 * @author      Mark Reinhold
 * @author      JSR-51 Expert Group
 * @since       1.4
 * @spec        JSR-51
 */


public final class Pattern
    implements java.io.Serializable
{

    /**
     * 正则表达式修饰符值。除了作为参数传递外，它们也可以作为内联修饰符传递。
     * 例如，以下语句具有相同的效果。
     * <pre>
     * RegExp r1 = RegExp.compile("abc", Pattern.I|Pattern.M);
     * RegExp r2 = RegExp.compile("(?im)abc", 0);
     * </pre>
     *
     * 这些标志被复制，以便可以使用熟悉的 Perl 匹配标志名称。
     */

    /**
     * 启用 Unix 行模式。
     *
     * <p> 在此模式下，仅识别 <tt>'\n'</tt> 行终止符
     * 在 <tt>.</tt>、<tt>^</tt> 和 <tt>$</tt> 的行为中。
     *
     * <p> 也可以通过嵌入的标志表达式 <tt>(?d)</tt> 启用 Unix 行模式。
     */
    public static final int UNIX_LINES = 0x01;

    /**
     * 启用不区分大小写的匹配。
     *
     * <p> 默认情况下，不区分大小写的匹配假定仅匹配 US-ASCII 字符集中的字符。通过指定 {@link
     * #UNICODE_CASE} 标志可以启用 Unicode 意识的不区分大小写的匹配。
     *
     * <p> 也可以通过嵌入的标志表达式 <tt>(?i)</tt> 启用不区分大小写的匹配。
     *
     * <p> 指定此标志可能会带来轻微的性能损失。 </p>
     */
    public static final int CASE_INSENSITIVE = 0x02;

    /**
     * 允许模式中的空白和注释。
     *
     * <p> 在此模式下，空白被忽略，从 <tt>#</tt> 开始的嵌入注释被忽略，直到行尾。
     *
     * <p> 也可以通过嵌入的标志表达式 <tt>(?x)</tt> 启用注释模式。
     */
    public static final int COMMENTS = 0x04;

    /**
     * 启用多行模式。
     *
     * <p> 在多行模式下，表达式 <tt>^</tt> 和 <tt>$</tt> 仅在行终止符或输入序列的末尾之后或之前匹配。默认情况下，这些表达式仅在输入序列的开头和结尾匹配。
     *
     * <p> 也可以通过嵌入的标志表达式 <tt>(?m)</tt> 启用多行模式。 </p>
     */
    public static final int MULTILINE = 0x08;

    /**
     * 启用模式的字面解析。
     *
     * <p> 当指定此标志时，指定模式的输入字符串被视为一系列字面字符。输入序列中的元字符或转义序列将不具有特殊含义。
     *
     * <p> 标志 CASE_INSENSITIVE 和 UNICODE_CASE 在与该标志一起使用时仍会影响匹配。其他标志变得多余。
     *
     * <p> 没有用于启用字面解析的嵌入标志字符。
     * @since 1.5
     */
    public static final int LITERAL = 0x10;

    /**
     * 启用点通模式。
     *
     * <p> 在点通模式下，表达式 <tt>.</tt> 匹配任何字符，包括行终止符。默认情况下，此表达式不匹配行终止符。
     *
     * <p> 也可以通过嵌入的标志表达式 <tt>(?s)</tt> 启用点通模式。 （<tt>s</tt> 是“单行”模式的助记符，这是 Perl 中的叫法。） </p>
     */
    public static final int DOTALL = 0x20;

    /**
     * 启用 Unicode 意识的大小写折叠。
     *
     * <p> 当指定此标志时，当通过 {@link #CASE_INSENSITIVE} 标志启用不区分大小写的匹配时，将以与 Unicode 标准一致的方式进行匹配。默认情况下，不区分大小写的匹配假定仅匹配 US-ASCII 字符集中的字符。
     *
     * <p> 也可以通过嵌入的标志表达式 <tt>(?u)</tt> 启用 Unicode 意识的大小写折叠。
     *
     * <p> 指定此标志可能会带来性能损失。 </p>
     */
    public static final int UNICODE_CASE = 0x40;

    /**
     * 启用规范等效。
     *
     * <p> 当指定此标志时，两个字符将仅在它们的完整规范分解匹配时才被认为匹配。例如，表达式 <tt>"a&#92;u030A"</tt> 将在指定此标志时匹配字符串 <tt>"&#92;u00E5"</tt>。默认情况下，匹配不考虑规范等效。
     *
     * <p> 没有用于启用规范等效的嵌入标志字符。
     *
     * <p> 指定此标志可能会带来性能损失。 </p>
     */
    public static final int CANON_EQ = 0x80;

    /**
     * 启用 <i>预定义字符类</i> 和 <i>POSIX 字符类</i> 的 Unicode 版本。
     *
     * <p> 当指定此标志时，（仅限 US-ASCII 的）<i>预定义字符类</i> 和 <i>POSIX 字符类</i> 符合
     * <a href="http://www.unicode.org/reports/tr18/"><i>Unicode 技术标准 #18: Unicode 正则表达式</i></a>
     * <i>附录 C: 兼容性属性</i>。
     * <p>
     * 可以通过嵌入的标志表达式 <tt>(?U)</tt> 启用 UNICODE_CHARACTER_CLASS 模式。
     * <p>
     * 该标志隐含 UNICODE_CASE，即启用 Unicode 意识的大小写折叠。
     * <p>
     * 指定此标志可能会带来性能损失。 </p>
     * @since 1.7
     */
    public static final int UNICODE_CHARACTER_CLASS = 0x100;

    /* Pattern 仅有两个序列化组件：模式字符串和标志，这是重新编译模式所需的所有内容
     * 当它被反序列化时。
     */

    /** 使用 Merlin b59 的 serialVersionUID 以实现互操作性 */
    private static final long serialVersionUID = 5073258162644648461L;

    /**
     * 原始正则表达式模式字符串。
     *
     * @serial
     */
    private String pattern;

    /**
     * 原始模式标志。
     *
     * @serial
     */
    private int flags;

    /**
     * 布尔值，指示此 Pattern 是否已编译；这是为了惰性编译反序列化的 Patterns 所必需的。
     */
    private transient volatile boolean compiled = false;

    /**
     * 规范化模式字符串。
     */
    private transient String normalizedPattern;

    /**
     * find 操作的状态机的起始点。这允许从输入的任何位置开始匹配。
     */
    transient Node root;

    /**
     * 匹配操作的对象树的根。模式在开头匹配。这可能包括使用 BnM 或 First 节点的查找。
     */
    transient Node matchRoot;

    /**
     * 用于解析模式切片的临时存储。
     */
    transient int[] buffer;

    /**
     * 将“命名捕获组”的“名称”映射到其组 ID 节点。
     */
    transient volatile Map<String, Integer> namedGroups;

    /**
     * 用于解析组引用的临时存储。
     */
    transient GroupHead[] groupNodes;

    /**
     * 用于模式编译的临时终止代码点数组。
     */
    private transient int[] temp;

    /**
     * 此 Pattern 中的捕获组数。由匹配器用于分配执行匹配所需的空间。
     */
    transient int capturingGroupCount;

    /**
     * 由解析树使用的局部变量计数。由匹配器用于分配执行匹配所需的空间。
     */
    transient int localCount;

    /**
     * 指向模式字符串的索引，用于跟踪已解析的内容。
     */
    private transient int cursor;

    /**
     * 模式字符串的长度。
     */
    private transient int patternLength;

    /**
     * 如果 Start 节点可能匹配补充字符。
     * 如果在编译过程中满足以下条件之一，则设置为 true：
     * (1) 模式中包含补充字符，或
     * (2) 包含 Category 或 Block 的补节点
     */
    private transient boolean hasSupplementary;

    /**
     * 编译给定的正则表达式为模式。
     *
     * @param  regex
     *         要编译的表达式
     * @return 编译后的模式
     * @throws  PatternSyntaxException
     *          如果表达式的语法无效
     */
    public static Pattern compile(String regex) {
        return new Pattern(regex, 0);
    }

    /**
     * 使用给定的标志编译给定的正则表达式为模式。
     *
     * @param  regex
     *         要编译的表达式
     *
     * @param  flags
     *         匹配标志，位掩码，可以包括
     *         {@link #CASE_INSENSITIVE}, {@link #MULTILINE}, {@link #DOTALL},
     *         {@link #UNICODE_CASE}, {@link #CANON_EQ}, {@link #UNIX_LINES},
     *         {@link #LITERAL}, {@link #UNICODE_CHARACTER_CLASS}
     *         和 {@link #COMMENTS}
     *
     * @return 使用给定标志编译后的模式
     * @throws  IllegalArgumentException
     *          如果 <tt>flags</tt> 中设置了与定义的匹配标志对应的位值以外的位值
     *
     * @throws  PatternSyntaxException
     *          如果表达式的语法无效
     */
    public static Pattern compile(String regex, int flags) {
        return new Pattern(regex, flags);
    }

    /**
     * 返回用于编译此模式的正则表达式。
     *
     * @return  此模式的源
     */
    public String pattern() {
        return pattern;
    }

    /**
     * <p>返回此模式的字符串表示形式。这是用于编译此模式的正则表达式。</p>
     *
     * @return  此模式的字符串表示形式
     * @since 1.5
     */
    public String toString() {
        return pattern;
    }

    /**
     * 创建一个匹配器，用于将给定输入与此模式匹配。
     *
     * @param  input
     *         要匹配的字符序列
     *
     * @return  用于此模式的新匹配器
     */
    public Matcher matcher(CharSequence input) {
        if (!compiled) {
            synchronized(this) {
                if (!compiled)
                    compile();
            }
        }
        Matcher m = new Matcher(this, input);
        return m;
    }

    /**
     * 返回此模式的匹配标志。
     *
     * @return  编译此模式时指定的匹配标志
     */
    public int flags() {
        return flags;
    }

    /**
     * 编译给定的正则表达式并尝试将给定输入与此模式匹配。
     *
     * <p> 以下形式的此便捷方法的调用
     *
     * <blockquote><pre>
     * Pattern.matches(regex, input);</pre></blockquote>
     *
     * 行为与以下表达式完全相同
     *
     * <blockquote><pre>
     * Pattern.compile(regex).matcher(input).matches()</pre></blockquote>
     *
     * <p> 如果模式将多次使用，编译一次并重复使用将比每次调用此方法更高效。 </p>
     *
     * @param  regex
     *         要编译的表达式
     *
     * @param  input
     *         要匹配的字符序列
     * @return 正则表达式是否匹配输入
     * @throws  PatternSyntaxException
     *          如果表达式的语法无效
     */
    public static boolean matches(String regex, CharSequence input) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(input);
        return m.matches();
    }

    /**
     * 按此模式的匹配将给定输入序列拆分。
     *
     * <p> 由该方法返回的数组包含输入序列的每个子字符串，这些子字符串由另一个与此模式匹配的子序列终止，或由输入序列的末尾终止。数组中的子字符串按其在输入中的顺序排列。如果此模式不匹配输入的任何子序列，则结果数组只有一个元素，即输入序列的字符串形式。
     *
     * <p> 当在输入序列的开头有正宽度匹配时，结果数组的开头会包含一个空的前导子字符串。然而，开头的零宽度匹配永远不会产生这样的空前导子字符串。
     *
     * <p> <tt>limit</tt> 参数控制模式应用的次数，因此影响结果数组的长度。如果限制 <i>n</i> 大于零，则模式最多应用 <i>n</i>&nbsp;-&nbsp;1 次，数组的长度不会大于 <i>n</i>，数组的最后一个条目将包含最后一个匹配分隔符之后的所有输入。如果 <i>n</i> 为非正数，则模式将尽可能多次应用，数组可以具有任意长度。如果 <i>n</i> 为零，则模式将尽可能多次应用，数组可以具有任意长度，尾部的空字符串将被丢弃。
     *
     * <p> 例如，输入 <tt>"boo:and:foo"</tt> 与这些参数结合使用时，将产生以下结果：
     *
     * <blockquote><table cellpadding=1 cellspacing=0
     *              summary="Split examples showing regex, limit, and result">
     * <tr><th align="left"><i>Regex&nbsp;&nbsp;&nbsp;&nbsp;</i></th>
     *     <th align="left"><i>Limit&nbsp;&nbsp;&nbsp;&nbsp;</i></th>
     *     <th align="left"><i>Result&nbsp;&nbsp;&nbsp;&nbsp;</i></th></tr>
     * <tr><td align=center>:</td>
     *     <td align=center>2</td>
     *     <td><tt>{ "boo", "and:foo" }</tt></td></tr>
     * <tr><td align=center>:</td>
     *     <td align=center>5</td>
     *     <td><tt>{ "boo", "and", "foo" }</tt></td></tr>
     * <tr><td align=center>:</td>
     *     <td align=center>-2</td>
     *     <td><tt>{ "boo", "and", "foo" }</tt></td></tr>
     * <tr><td align=center>o</td>
     *     <td align=center>5</td>
     *     <td><tt>{ "b", "", ":and:f", "", "" }</tt></td></tr>
     * <tr><td align=center>o</td>
     *     <td align=center>-2</td>
     *     <td><tt>{ "b", "", ":and:f", "", "" }</tt></td></tr>
     * <tr><td align=center>o</td>
     *     <td align=center>0</td>
     *     <td><tt>{ "b", "", ":and:f" }</tt></td></tr>
     * </table></blockquote>
     *
     * @param  input
     *         要拆分的字符序列
     *
     * @param  limit
     *         结果阈值，如上所述
     *
     * @return  通过按此模式的匹配拆分输入计算的字符串数组
     */
    public String[] split(CharSequence input, int limit) {
        int index = 0;
        boolean matchLimited = limit > 0;
        ArrayList<String> matchList = new ArrayList<>();
        Matcher m = matcher(input);


                    // 在每个匹配项之前添加段落
        while(m.find()) {
            if (!matchLimited || matchList.size() < limit - 1) {
                if (index == 0 && index == m.start() && m.start() == m.end()) {
                    // 不包括在输入字符序列开头的零宽度匹配的空前导子字符串
                    continue;
                }
                String match = input.subSequence(index, m.start()).toString();
                matchList.add(match);
                index = m.end();
            } else if (matchList.size() == limit - 1) { // 最后一个
                String match = input.subSequence(index,
                                                 input.length()).toString();
                matchList.add(match);
                index = m.end();
            }
        }

        // 如果没有找到匹配项，返回此值
        if (index == 0)
            return new String[] {input.toString()};

        // 添加剩余段落
        if (!matchLimited || matchList.size() < limit)
            matchList.add(input.subSequence(index, input.length()).toString());

        // 构建结果
        int resultSize = matchList.size();
        if (limit == 0)
            while (resultSize > 0 && matchList.get(resultSize-1).equals(""))
                resultSize--;
        String[] result = new String[resultSize];
        return matchList.subList(0, resultSize).toArray(result);
    }

    /**
     * 按照此模式的匹配项分割给定的输入序列。
     *
     * <p>此方法的工作方式类似于调用带有给定输入序列和零限制参数的 {@link
     * #split(java.lang.CharSequence, int) split} 方法。因此，结果数组中不包括尾随的空字符串。</p>
     *
     * <p>例如，输入 <tt>"boo:and:foo"</tt> 使用以下表达式将产生以下结果：
     *
     * <blockquote><table cellpadding=1 cellspacing=0
     *              summary="Split examples showing regex and result">
     * <tr><th align="left"><i>正则表达式&nbsp;&nbsp;&nbsp;&nbsp;</i></th>
     *     <th align="left"><i>结果</i></th></tr>
     * <tr><td align=center>:</td>
     *     <td><tt>{ "boo", "and", "foo" }</tt></td></tr>
     * <tr><td align=center>o</td>
     *     <td><tt>{ "b", "", ":and:f" }</tt></td></tr>
     * </table></blockquote>
     *
     *
     * @param  input
     *         要分割的字符序列
     *
     * @return  通过按照此模式的匹配项分割输入而计算出的字符串数组
     */
    public String[] split(CharSequence input) {
        return split(input, 0);
    }

    /**
     * 返回指定字符串的字面模式字符串。
     *
     * <p>此方法生成一个可以用于创建一个 <code>Pattern</code> 的字符串，该模式将匹配字符串
     * <code>s</code>，就像它是字面模式一样。</p> 输入序列中的元字符或转义序列将不会被赋予特殊意义。
     *
     * @param  s 要字面化的字符串
     * @return  字面字符串替换
     * @since 1.5
     */
    public static String quote(String s) {
        int slashEIndex = s.indexOf("\\E");
        if (slashEIndex == -1)
            return "\\Q" + s + "\\E";

        StringBuilder sb = new StringBuilder(s.length() * 2);
        sb.append("\\Q");
        slashEIndex = 0;
        int current = 0;
        while ((slashEIndex = s.indexOf("\\E", current)) != -1) {
            sb.append(s.substring(current, slashEIndex));
            current = slashEIndex + 2;
            sb.append("\\E\\\\E\\Q");
        }
        sb.append(s.substring(current, s.length()));
        sb.append("\\E");
        return sb.toString();
    }

    /**
     * 从流中重新编译 Pattern 实例。读取原始模式字符串并从其重新编译对象树。
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {

        // 读取所有字段
        s.defaultReadObject();

        // 初始化计数
        capturingGroupCount = 1;
        localCount = 0;

        // 如果长度大于 0，则 Pattern 懒编译
        compiled = false;
        if (pattern.length() == 0) {
            root = new Start(lastAccept);
            matchRoot = lastAccept;
            compiled = true;
        }
    }

    /**
     * 此私有构造函数用于创建所有 Patterns。模式字符串和匹配标志是完全描述一个 Pattern 所需的全部。
     * 空模式字符串将生成一个仅包含 Start 节点和 LastNode 节点的对象树。
     */
    private Pattern(String p, int f) {
        pattern = p;
        flags = f;

        // 如果存在 UNICODE_CHARACTER_CLASS，则使用 UNICODE_CASE
        if ((flags & UNICODE_CHARACTER_CLASS) != 0)
            flags |= UNICODE_CASE;

        // 重置组索引计数
        capturingGroupCount = 1;
        localCount = 0;

        if (pattern.length() > 0) {
            try {
                compile();
            } catch (StackOverflowError soe) {
                throw error("模式编译期间堆栈溢出");
            }
        } else {
            root = new Start(lastAccept);
            matchRoot = lastAccept;
        }
    }

    /**
     * 将模式转换为规范化形式 D，然后构建一个纯组来匹配字符的规范等效项。
     */
    private void normalize() {
        boolean inCharClass = false;
        int lastCodePoint = -1;

        // 将模式转换为规范化形式 D
        normalizedPattern = Normalizer.normalize(pattern, Normalizer.Form.NFD);
        patternLength = normalizedPattern.length();

        // 修改模式以匹配规范等效项
        StringBuilder newPattern = new StringBuilder(patternLength);
        for(int i=0; i<patternLength; ) {
            int c = normalizedPattern.codePointAt(i);
            StringBuilder sequenceBuffer;
            if ((Character.getType(c) == Character.NON_SPACING_MARK)
                && (lastCodePoint != -1)) {
                sequenceBuffer = new StringBuilder();
                sequenceBuffer.appendCodePoint(lastCodePoint);
                sequenceBuffer.appendCodePoint(c);
                while(Character.getType(c) == Character.NON_SPACING_MARK) {
                    i += Character.charCount(c);
                    if (i >= patternLength)
                        break;
                    c = normalizedPattern.codePointAt(i);
                    sequenceBuffer.appendCodePoint(c);
                }
                String ea = produceEquivalentAlternation(
                                               sequenceBuffer.toString());
                newPattern.setLength(newPattern.length()-Character.charCount(lastCodePoint));
                newPattern.append("(?:").append(ea).append(")");
            } else if (c == '[' && lastCodePoint != '\\') {
                i = normalizeCharClass(newPattern, i);
            } else {
                newPattern.appendCodePoint(c);
            }
            lastCodePoint = c;
            i += Character.charCount(c);
        }
        normalizedPattern = newPattern.toString();
    }

    /**
     * 完成正在解析的字符类，并向其中添加一组交替项，这些交替项将匹配字符类中字符的规范等效项。
     */
    private int normalizeCharClass(StringBuilder newPattern, int i) {
        StringBuilder charClass = new StringBuilder();
        StringBuilder eq = null;
        int lastCodePoint = -1;
        String result;

        i++;
        if (i == normalizedPattern.length())
            throw error("未闭合的字符类");
        charClass.append("[");
        while(true) {
            int c = normalizedPattern.codePointAt(i);
            StringBuilder sequenceBuffer;

            if (c == ']' && lastCodePoint != '\\') {
                charClass.append((char)c);
                break;
            } else if (Character.getType(c) == Character.NON_SPACING_MARK) {
                sequenceBuffer = new StringBuilder();
                sequenceBuffer.appendCodePoint(lastCodePoint);
                while(Character.getType(c) == Character.NON_SPACING_MARK) {
                    sequenceBuffer.appendCodePoint(c);
                    i += Character.charCount(c);
                    if (i >= normalizedPattern.length())
                        break;
                    c = normalizedPattern.codePointAt(i);
                }
                String ea = produceEquivalentAlternation(
                                                  sequenceBuffer.toString());

                charClass.setLength(charClass.length()-Character.charCount(lastCodePoint));
                if (eq == null)
                    eq = new StringBuilder();
                eq.append('|');
                eq.append(ea);
            } else {
                charClass.appendCodePoint(c);
                i++;
            }
            if (i == normalizedPattern.length())
                throw error("未闭合的字符类");
            lastCodePoint = c;
        }

        if (eq != null) {
            result = "(?:"+charClass.toString()+eq.toString()+")";
        } else {
            result = charClass.toString();
        }

        newPattern.append(result);
        return i;
    }

    /**
     * 给定一个由常规字符和其后跟随的组合标记组成的特定序列，生成一个将匹配该序列所有规范等效项的交替项。
     */
    private String produceEquivalentAlternation(String source) {
        int len = countChars(source, 0, 1);
        if (source.length() == len)
            // 源字符串只有一个字符。
            return source;

        String base = source.substring(0,len);
        String combiningMarks = source.substring(len);

        String[] perms = producePermutations(combiningMarks);
        StringBuilder result = new StringBuilder(source);

        // 添加组合排列
        for(int x=0; x<perms.length; x++) {
            String next = base + perms[x];
            if (x>0)
                result.append("|"+next);
            next = composeOneStep(next);
            if (next != null)
                result.append("|"+produceEquivalentAlternation(next));
        }
        return result.toString();
    }

    /**
     * 返回一个字符串数组，其中包含输入字符串中字符的所有可能排列。
     * 这用于获取一组组合标记的所有可能顺序。注意，某些排列是无效的，因为存在组合类冲突，这些可能性必须被移除，因为它们不是规范等效的。
     */
    private String[] producePermutations(String input) {
        if (input.length() == countChars(input, 0, 1))
            return new String[] {input};

        if (input.length() == countChars(input, 0, 2)) {
            int c0 = Character.codePointAt(input, 0);
            int c1 = Character.codePointAt(input, Character.charCount(c0));
            if (getClass(c1) == getClass(c0)) {
                return new String[] {input};
            }
            String[] result = new String[2];
            result[0] = input;
            StringBuilder sb = new StringBuilder(2);
            sb.appendCodePoint(c1);
            sb.appendCodePoint(c0);
            result[1] = sb.toString();
            return result;
        }

        int length = 1;
        int nCodePoints = countCodePoints(input);
        for(int x=1; x<nCodePoints; x++)
            length = length * (x+1);

        String[] temp = new String[length];

        int combClass[] = new int[nCodePoints];
        for(int x=0, i=0; x<nCodePoints; x++) {
            int c = Character.codePointAt(input, i);
            combClass[x] = getClass(c);
            i +=  Character.charCount(c);
        }

        // 对每个字符，将其取出并添加剩余字符的排列
        int index = 0;
        int len;
        // offset 维护代码单元中的索引。
loop:   for(int x=0, offset=0; x<nCodePoints; x++, offset+=len) {
            len = countChars(input, offset, 1);
            boolean skip = false;
            for(int y=x-1; y>=0; y--) {
                if (combClass[y] == combClass[x]) {
                    continue loop;
                }
            }
            StringBuilder sb = new StringBuilder(input);
            String otherChars = sb.delete(offset, offset+len).toString();
            String[] subResult = producePermutations(otherChars);

            String prefix = input.substring(offset, offset+len);
            for(int y=0; y<subResult.length; y++)
                temp[index++] =  prefix + subResult[y];
        }
        String[] result = new String[index];
        for (int x=0; x<index; x++)
            result[x] = temp[x];
        return result;
    }

    private int getClass(int c) {
        return sun.text.Normalizer.getCombiningClass(c);
    }

    /**
     * 尝试通过将第一个字符与紧随其后的第一个组合标记组合来组合输入。返回一个字符串，该字符串是前导字符与其第一个组合标记的组合，后跟剩余的组合标记。如果前两个字符不能进一步组合，则返回 null。
     */
    private String composeOneStep(String input) {
        int len = countChars(input, 0, 2);
        String firstTwoCharacters = input.substring(0, len);
        String result = Normalizer.normalize(firstTwoCharacters, Normalizer.Form.NFC);

        if (result.equals(firstTwoCharacters))
            return null;
        else {
            String remainder = input.substring(len);
            return result + remainder;
        }
    }

    /**
     * 预处理 `temp' 中的任何 \Q...\E 序列，对其进行元字符转义。
     * 参见 perlfunc(1) 中的 `quotemeta' 描述。
     */
    private void RemoveQEQuoting() {
        final int pLen = patternLength;
        int i = 0;
        while (i < pLen-1) {
            if (temp[i] != '\\')
                i += 1;
            else if (temp[i + 1] != 'Q')
                i += 2;
            else
                break;
        }
        if (i >= pLen - 1)    // 未找到 \Q 序列
            return;
        int j = i;
        i += 2;
        int[] newtemp = new int[j + 3*(pLen-i) + 2];
        System.arraycopy(temp, 0, newtemp, 0, j);

        boolean inQuote = true;
        boolean beginQuote = true;
        while (i < pLen) {
            int c = temp[i++];
            if (!ASCII.isAscii(c) || ASCII.isAlpha(c)) {
                newtemp[j++] = c;
            } else if (ASCII.isDigit(c)) {
                if (beginQuote) {
                    /*
                     * 一个 Unicode 转义 \[0xu] 可能在此引用之前，我们不希望这个数字字符被处理为转义的一部分。
                     */
                    newtemp[j++] = '\\';
                    newtemp[j++] = 'x';
                    newtemp[j++] = '3';
                }
                newtemp[j++] = c;
            } else if (c != '\\') {
                if (inQuote) newtemp[j++] = '\\';
                newtemp[j++] = c;
            } else if (inQuote) {
                if (temp[i] == 'E') {
                    i++;
                    inQuote = false;
                } else {
                    newtemp[j++] = '\\';
                    newtemp[j++] = '\\';
                }
            } else {
                if (temp[i] == 'Q') {
                    i++;
                    inQuote = true;
                    beginQuote = true;
                    continue;
                } else {
                    newtemp[j++] = c;
                    if (i != pLen)
                        newtemp[j++] = temp[i++];
                }
            }


                        beginQuote = false;
        }

        patternLength = j;
        temp = Arrays.copyOf(newtemp, j + 2); // 双零终止
    }

    /**
     * 将正则表达式复制到 int 数组并调用表达式的解析
     * 以创建对象树。
     */
    private void compile() {
        // 处理规范等价
        if (has(CANON_EQ) && !has(LITERAL)) {
            normalize();
        } else {
            normalizedPattern = pattern;
        }
        patternLength = normalizedPattern.length();

        // 为方便起见将模式复制到 int 数组
        // 使用双零终止模式
        temp = new int[patternLength + 2];

        hasSupplementary = false;
        int c, count = 0;
        // 将所有字符转换为代码点
        for (int x = 0; x < patternLength; x += Character.charCount(c)) {
            c = normalizedPattern.codePointAt(x);
            if (isSupplementary(c)) {
                hasSupplementary = true;
            }
            temp[count++] = c;
        }

        patternLength = count;   // patternLength 现在以代码点为单位

        if (! has(LITERAL))
            RemoveQEQuoting();

        // 在此处分配所有临时对象。
        buffer = new int[32];
        groupNodes = new GroupHead[10];
        namedGroups = null;

        if (has(LITERAL)) {
            // 文本模式处理
            matchRoot = newSlice(temp, patternLength, hasSupplementary);
            matchRoot.next = lastAccept;
        } else {
            // 开始递归下降解析
            matchRoot = expr(lastAccept);
            // 检查额外的模式字符
            if (patternLength != cursor) {
                if (peek() == ')') {
                    throw error("不匹配的关闭 ')' ");
                } else {
                    throw error("意外的内部错误");
                }
            }
        }

        // 优化窥视孔
        if (matchRoot instanceof Slice) {
            root = BnM.optimize(matchRoot);
            if (root == matchRoot) {
                root = hasSupplementary ? new StartS(matchRoot) : new Start(matchRoot);
            }
        } else if (matchRoot instanceof Begin || matchRoot instanceof First) {
            root = matchRoot;
        } else {
            root = hasSupplementary ? new StartS(matchRoot) : new Start(matchRoot);
        }

        // 释放临时存储
        temp = null;
        buffer = null;
        groupNodes = null;
        patternLength = 0;
        compiled = true;
    }

    Map<String, Integer> namedGroups() {
        if (namedGroups == null)
            namedGroups = new HashMap<>(2);
        return namedGroups;
    }

    /**
     * 用于打印模式的子树以帮助调试。
     */
    private static void printObjectTree(Node node) {
        while(node != null) {
            if (node instanceof Prolog) {
                System.out.println(node);
                printObjectTree(((Prolog)node).loop);
                System.out.println("**** end contents prolog loop");
            } else if (node instanceof Loop) {
                System.out.println(node);
                printObjectTree(((Loop)node).body);
                System.out.println("**** end contents Loop body");
            } else if (node instanceof Curly) {
                System.out.println(node);
                printObjectTree(((Curly)node).atom);
                System.out.println("**** end contents Curly body");
            } else if (node instanceof GroupCurly) {
                System.out.println(node);
                printObjectTree(((GroupCurly)node).atom);
                System.out.println("**** end contents GroupCurly body");
            } else if (node instanceof GroupTail) {
                System.out.println(node);
                System.out.println("Tail next is "+node.next);
                return;
            } else {
                System.out.println(node);
            }
            node = node.next;
            if (node != null)
                System.out.println("->next:");
            if (node == Pattern.accept) {
                System.out.println("Accept Node");
                node = null;
            }
       }
    }

    /**
     * 用于累积对象图的子树信息
     * 以便可以对子树应用优化。
     */
    static final class TreeInfo {
        int minLength;
        int maxLength;
        boolean maxValid;
        boolean deterministic;

        TreeInfo() {
            reset();
        }
        void reset() {
            minLength = 0;
            maxLength = 0;
            maxValid = true;
            deterministic = true;
        }
    }

    /*
     * 以下私有方法主要用于提高代码的可读性。为了使 Java 编译器能够轻松地内联它们，
     * 我们不应该在其中放置许多断言或错误检查。
     */

    /**
     * 指示是否设置了特定的标志。
     */
    private boolean has(int f) {
        return (flags & f) != 0;
    }

    /**
     * 匹配下一个字符，如果失败则发出错误信号。
     */
    private void accept(int ch, String s) {
        int testChar = temp[cursor++];
        if (has(COMMENTS))
            testChar = parsePastWhitespace(testChar);
        if (ch != testChar) {
            throw error(s);
        }
    }

    /**
     * 用特定字符标记模式的结束。
     */
    private void mark(int c) {
        temp[patternLength] = c;
    }

    /**
     * 查看下一个字符，但不移动指针。
     */
    private int peek() {
        int ch = temp[cursor];
        if (has(COMMENTS))
            ch = peekPastWhitespace(ch);
        return ch;
    }

    /**
     * 读取下一个字符，并将指针向前移动一位。
     */
    private int read() {
        int ch = temp[cursor++];
        if (has(COMMENTS))
            ch = parsePastWhitespace(ch);
        return ch;
    }

    /**
     * 读取下一个字符，并将指针向前移动一位，
     * 忽略 COMMENTS 设置
     */
    private int readEscaped() {
        int ch = temp[cursor++];
        return ch;
    }

    /**
     * 将指针向前移动一位，并查看下一个字符。
     */
    private int next() {
        int ch = temp[++cursor];
        if (has(COMMENTS))
            ch = peekPastWhitespace(ch);
        return ch;
    }

    /**
     * 将指针向前移动一位，并查看下一个字符，
     * 忽略 COMMENTS 设置
     */
    private int nextEscaped() {
        int ch = temp[++cursor];
        return ch;
    }

    /**
     * 如果在 xmode 中，跳过空白和注释。
     */
    private int peekPastWhitespace(int ch) {
        while (ASCII.isSpace(ch) || ch == '#') {
            while (ASCII.isSpace(ch))
                ch = temp[++cursor];
            if (ch == '#') {
                ch = peekPastLine();
            }
        }
        return ch;
    }

    /**
     * 如果在 xmode 中，解析空白和注释。
     */
    private int parsePastWhitespace(int ch) {
        while (ASCII.isSpace(ch) || ch == '#') {
            while (ASCII.isSpace(ch))
                ch = temp[cursor++];
            if (ch == '#')
                ch = parsePastLine();
        }
        return ch;
    }

    /**
     * xmode 解析注释到行尾。
     */
    private int parsePastLine() {
        int ch = temp[cursor++];
        while (ch != 0 && !isLineSeparator(ch))
            ch = temp[cursor++];
        if (ch == 0 && cursor > patternLength) {
            cursor = patternLength;
            ch = temp[cursor++];
        }
        return ch;
    }

    /**
     * xmode 查看注释到行尾。
     */
    private int peekPastLine() {
        int ch = temp[++cursor];
        while (ch != 0 && !isLineSeparator(ch))
            ch = temp[++cursor];
        if (ch == 0 && cursor > patternLength) {
            cursor = patternLength;
            ch = temp[cursor];
        }
        return ch;
    }

    /**
     * 确定字符是否为当前模式中的行分隔符
     */
    private boolean isLineSeparator(int ch) {
        if (has(UNIX_LINES)) {
            return ch == '\n';
        } else {
            return (ch == '\n' ||
                    ch == '\r' ||
                    (ch|1) == '\u2029' ||
                    ch == '\u0085');
        }
    }

    /**
     * 读取下一个字符后的字符，并将指针向前移动两位。
     */
    private int skip() {
        int i = cursor;
        int ch = temp[i+1];
        cursor = i + 2;
        return ch;
    }

    /**
     * 退回一个字符，将指针向后移动一位。
     */
    private void unread() {
        cursor--;
    }

    /**
     * 用于处理所有语法错误的内部方法。模式将显示一个指针以帮助定位语法错误。
     */
    private PatternSyntaxException error(String s) {
        return new PatternSyntaxException(s, normalizedPattern,  cursor - 1);
    }

    /**
     * 确定指定范围内是否有任何补充字符或未配对的代理。
     */
    private boolean findSupplementary(int start, int end) {
        for (int i = start; i < end; i++) {
            if (isSupplementary(temp[i]))
                return true;
        }
        return false;
    }

    /**
     * 确定指定的代码点是否为补充字符或未配对的代理。
     */
    private static final boolean isSupplementary(int ch) {
        return ch >= Character.MIN_SUPPLEMENTARY_CODE_POINT ||
               Character.isSurrogate((char)ch);
    }

    /**
     * 以下方法处理主要解析。它们按优先级顺序排序，优先级最低的在前。
     */

    /**
     * 使用分支节点解析表达式以处理交替。这可以递归调用来解析可能包含交替的子表达式。
     */
    private Node expr(Node end) {
        Node prev = null;
        Node firstTail = null;
        Branch branch = null;
        Node branchConn = null;

        for (;;) {
            Node node = sequence(end);
            Node nodeTail = root;      // 双返回
            if (prev == null) {
                prev = node;
                firstTail = nodeTail;
            } else {
                // 分支
                if (branchConn == null) {
                    branchConn = new BranchConn();
                    branchConn.next = end;
                }
                if (node == end) {
                    // 如果从 sequence() 返回的节点是 "end"
                    // 我们有一个空的 expr，将一个 null 原子放入
                    // 分支中，表示直接转到 "next"。
                    node = null;
                } else {
                    // 每个原子的 "tail.next" 都指向 branchConn
                    nodeTail.next = branchConn;
                }
                if (prev == branch) {
                    branch.add(node);
                } else {
                    if (prev == end) {
                        prev = null;
                    } else {
                        // 当将 "prev" 作为第一个原子放入分支时
                        // 将 "end" 替换为 "branchConn" 作为其 tail.next。
                        firstTail.next = branchConn;
                    }
                    prev = branch = new Branch(prev, node, branchConn);
                }
            }
            if (peek() != '|') {
                return prev;
            }
            next();
        }
    }

    @SuppressWarnings("fallthrough")
    /**
     * 解析交替之间的序列。
     */
    private Node sequence(Node end) {
        Node head = null;
        Node tail = null;
        Node node = null;
    LOOP:
        for (;;) {
            int ch = peek();
            switch (ch) {
            case '(':
                // 由于组处理其自身的闭包，
                // 我们需要对其进行不同的处理
                node = group0();
                // 检查注释或标志组
                if (node == null)
                    continue;
                if (head == null)
                    head = node;
                else
                    tail.next = node;
                // 双返回：Tail 在 root 中返回
                tail = root;
                continue;
            case '[':
                node = clazz(true);
                break;
            case '\\':
                ch = nextEscaped();
                if (ch == 'p' || ch == 'P') {
                    boolean oneLetter = true;
                    boolean comp = (ch == 'P');
                    ch = next(); // 消耗 {（如果存在）
                    if (ch != '{') {
                        unread();
                    } else {
                        oneLetter = false;
                    }
                    node = family(oneLetter, comp);
                } else {
                    unread();
                    node = atom();
                }
                break;
            case '^':
                next();
                if (has(MULTILINE)) {
                    if (has(UNIX_LINES))
                        node = new UnixCaret();
                    else
                        node = new Caret();
                } else {
                    node = new Begin();
                }
                break;
            case '$':
                next();
                if (has(UNIX_LINES))
                    node = new UnixDollar(has(MULTILINE));
                else
                    node = new Dollar(has(MULTILINE));
                break;
            case '.':
                next();
                if (has(DOTALL)) {
                    node = new All();
                } else {
                    if (has(UNIX_LINES))
                        node = new UnixDot();
                    else {
                        node = new Dot();
                    }
                }
                break;
            case '|':
            case ')':
                break LOOP;
            case ']': // 现在将悬挂的 ] 和 } 解释为字面量
            case '}':
                node = atom();
                break;
            case '?':
            case '*':
            case '+':
                next();
                throw error("悬挂的元字符 '" + ((char)ch) + "'");
            case 0:
                if (cursor >= patternLength) {
                    break LOOP;
                }
                // 穿透
            default:
                node = atom();
                break;
            }

            node = closure(node);

            if (head == null) {
                head = tail = node;
            } else {
                tail.next = node;
                tail = node;
            }
        }
        if (head == null) {
            return end;
        }
        tail.next = end;
        root = tail;      // 双返回
        return head;
    }

    @SuppressWarnings("fallthrough")
    /**
     * 解析并添加新的 Single 或 Slice。
     */
    private Node atom() {
        int first = 0;
        int prev = -1;
        boolean hasSupplementary = false;
        int ch = peek();
        for (;;) {
            switch (ch) {
            case '*':
            case '+':
            case '?':
            case '{':
                if (first > 1) {
                    cursor = prev;    // 撤销一个字符
                    first--;
                }
                break;
            case '$':
            case '.':
            case '^':
            case '(':
            case '[':
            case '|':
            case ')':
                break;
            case '\\':
                ch = nextEscaped();
                if (ch == 'p' || ch == 'P') { // 属性
                    if (first > 0) { // Slice 正在等待；先处理它
                        unread();
                        break;
                    } else { // 没有 slice；直接返回 family 节点
                        boolean comp = (ch == 'P');
                        boolean oneLetter = true;
                        ch = next(); // 消耗 {（如果存在）
                        if (ch != '{')
                            unread();
                        else
                            oneLetter = false;
                        return family(oneLetter, comp);
                    }
                }
                unread();
                prev = cursor;
                ch = escape(false, first == 0, false);
                if (ch >= 0) {
                    append(ch, first);
                    first++;
                    if (isSupplementary(ch)) {
                        hasSupplementary = true;
                    }
                    ch = peek();
                    continue;
                } else if (first == 0) {
                    return root;
                }
                // 撤销元字符转义序列
                cursor = prev;
                break;
            case 0:
                if (cursor >= patternLength) {
                    break;
                }
                // 穿透
            default:
                prev = cursor;
                append(ch, first);
                first++;
                if (isSupplementary(ch)) {
                    hasSupplementary = true;
                }
                ch = next();
                continue;
            }
            break;
        }
        if (first == 1) {
            return newSingle(buffer[0]);
        } else {
            return newSlice(buffer, first, hasSupplementary);
        }
    }


                private void append(int ch, int len) {
        if (len >= buffer.length) {
            int[] tmp = new int[len+len];
            System.arraycopy(buffer, 0, tmp, 0, len);
            buffer = tmp;
        }
        buffer[len] = ch;
    }

    /**
     * 贪婪地解析回溯引用，尽可能多地取数字。第一个数字总是被视为回溯引用，但多数字的数字只有在当前正则表达式中存在至少那么多的回溯引用时才被视为回溯引用。
     */
    private Node ref(int refNum) {
        boolean done = false;
        while(!done) {
            int ch = peek();
            switch(ch) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                int newRefNum = (refNum * 10) + (ch - '0');
                // 如果添加的数字不会导致不存在的组，则添加另一个数字
                if (capturingGroupCount - 1 < newRefNum) {
                    done = true;
                    break;
                }
                refNum = newRefNum;
                read();
                break;
            default:
                done = true;
                break;
            }
        }
        if (has(CASE_INSENSITIVE))
            return new CIBackRef(refNum, has(UNICODE_CASE));
        else
            return new BackRef(refNum);
    }

    /**
     * 解析转义序列以确定需要匹配的实际值。
     * 如果返回 -1 并且 create 为 true，则会向树中添加一个新对象来处理转义序列。
     * 如果返回值大于零，则表示该值匹配转义序列。
     */
    private int escape(boolean inclass, boolean create, boolean isrange) {
        int ch = skip();
        switch (ch) {
        case '0':
            return o();
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            if (inclass) break;
            if (create) {
                root = ref((ch - '0'));
            }
            return -1;
        case 'A':
            if (inclass) break;
            if (create) root = new Begin();
            return -1;
        case 'B':
            if (inclass) break;
            if (create) root = new Bound(Bound.NONE, has(UNICODE_CHARACTER_CLASS));
            return -1;
        case 'C':
            break;
        case 'D':
            if (create) root = has(UNICODE_CHARACTER_CLASS)
                               ? new Utype(UnicodeProp.DIGIT).complement()
                               : new Ctype(ASCII.DIGIT).complement();
            return -1;
        case 'E':
        case 'F':
            break;
        case 'G':
            if (inclass) break;
            if (create) root = new LastMatch();
            return -1;
        case 'H':
            if (create) root = new HorizWS().complement();
            return -1;
        case 'I':
        case 'J':
        case 'K':
        case 'L':
        case 'M':
        case 'N':
        case 'O':
        case 'P':
        case 'Q':
            break;
        case 'R':
            if (inclass) break;
            if (create) root = new LineEnding();
            return -1;
        case 'S':
            if (create) root = has(UNICODE_CHARACTER_CLASS)
                               ? new Utype(UnicodeProp.WHITE_SPACE).complement()
                               : new Ctype(ASCII.SPACE).complement();
            return -1;
        case 'T':
        case 'U':
            break;
        case 'V':
            if (create) root = new VertWS().complement();
            return -1;
        case 'W':
            if (create) root = has(UNICODE_CHARACTER_CLASS)
                               ? new Utype(UnicodeProp.WORD).complement()
                               : new Ctype(ASCII.WORD).complement();
            return -1;
        case 'X':
        case 'Y':
            break;
        case 'Z':
            if (inclass) break;
            if (create) {
                if (has(UNIX_LINES))
                    root = new UnixDollar(false);
                else
                    root = new Dollar(false);
            }
            return -1;
        case 'a':
            return '\007';
        case 'b':
            if (inclass) break;
            if (create) root = new Bound(Bound.BOTH, has(UNICODE_CHARACTER_CLASS));
            return -1;
        case 'c':
            return c();
        case 'd':
            if (create) root = has(UNICODE_CHARACTER_CLASS)
                               ? new Utype(UnicodeProp.DIGIT)
                               : new Ctype(ASCII.DIGIT);
            return -1;
        case 'e':
            return '\033';
        case 'f':
            return '\f';
        case 'g':
            break;
        case 'h':
            if (create) root = new HorizWS();
            return -1;
        case 'i':
        case 'j':
            break;
        case 'k':
            if (inclass)
                break;
            if (read() != '<')
                throw error("\\k 后面没有跟 '<' 用于命名捕获组");
            String name = groupname(read());
            if (!namedGroups().containsKey(name))
                throw error("(命名捕获组 <" + name + "> 不存在");
            if (create) {
                if (has(CASE_INSENSITIVE))
                    root = new CIBackRef(namedGroups().get(name), has(UNICODE_CASE));
                else
                    root = new BackRef(namedGroups().get(name));
            }
            return -1;
        case 'l':
        case 'm':
            break;
        case 'n':
            return '\n';
        case 'o':
        case 'p':
        case 'q':
            break;
        case 'r':
            return '\r';
        case 's':
            if (create) root = has(UNICODE_CHARACTER_CLASS)
                               ? new Utype(UnicodeProp.WHITE_SPACE)
                               : new Ctype(ASCII.SPACE);
            return -1;
        case 't':
            return '\t';
        case 'u':
            return u();
        case 'v':
            // 在 < 1.8 的版本中，'\v' 被实现为 VT/0x0B（尽管未记录）。在 JDK8 中，'\v' 被指定为所有垂直空白字符的预定义字符类。
            // 因此返回 [-1, root=VertWS 节点] 对（而不是单个 0x0B）。如果 '\v' 用作范围的开始或结束值，如 [\v-...] 或 [...-\v]，
            // 则期望一个单一的确定值（0x0B）。出于兼容性考虑，如果 isrange 为 true，则返回 '\013'/0x0B。
            if (isrange)
                return '\013';
            if (create) root = new VertWS();
            return -1;
        case 'w':
            if (create) root = has(UNICODE_CHARACTER_CLASS)
                               ? new Utype(UnicodeProp.WORD)
                               : new Ctype(ASCII.WORD);
            return -1;
        case 'x':
            return x();
        case 'y':
            break;
        case 'z':
            if (inclass) break;
            if (create) root = new End();
            return -1;
        default:
            return ch;
        }
        throw error("非法/不支持的转义序列");
    }

    /**
     * 解析字符类，并返回匹配它的节点。
     *
     * 如果 consume 为 true，则在退出时消耗一个 ]。通常情况下 consume 为 true，除非在 [abc&&def] 这种情况下，def 是一个单独的右侧节点，其括号是“隐含的”。
     */
    private CharProperty clazz(boolean consume) {
        CharProperty prev = null;
        CharProperty node = null;
        BitClass bits = new BitClass();
        boolean include = true;
        boolean firstInClass = true;
        int ch = next();
        for (;;) {
            switch (ch) {
                case '^':
                    // 如果是类中的第一个字符，则取反，否则按字面意义处理
                    if (firstInClass) {
                        if (temp[cursor-1] != '[')
                            break;
                        ch = next();
                        include = !include;
                        continue;
                    } else {
                        // ^ 不是类中的第一个字符，按字面意义处理
                        break;
                    }
                case '[':
                    firstInClass = false;
                    node = clazz(true);
                    if (prev == null)
                        prev = node;
                    else
                        prev = union(prev, node);
                    ch = peek();
                    continue;
                case '&':
                    firstInClass = false;
                    ch = next();
                    if (ch == '&') {
                        ch = next();
                        CharProperty rightNode = null;
                        while (ch != ']' && ch != '&') {
                            if (ch == '[') {
                                if (rightNode == null)
                                    rightNode = clazz(true);
                                else
                                    rightNode = union(rightNode, clazz(true));
                            } else { // abc&&def
                                unread();
                                rightNode = clazz(false);
                            }
                            ch = peek();
                        }
                        if (rightNode != null)
                            node = rightNode;
                        if (prev == null) {
                            if (rightNode == null)
                                throw error("错误的类语法");
                            else
                                prev = rightNode;
                        } else {
                            prev = intersection(prev, node);
                        }
                    } else {
                        // 按字面意义处理 &
                        unread();
                        break;
                    }
                    continue;
                case 0:
                    firstInClass = false;
                    if (cursor >= patternLength)
                        throw error("未闭合的字符类");
                    break;
                case ']':
                    firstInClass = false;
                    if (prev != null) {
                        if (consume)
                            next();
                        return prev;
                    }
                    break;
                default:
                    firstInClass = false;
                    break;
            }
            node = range(bits);
            if (include) {
                if (prev == null) {
                    prev = node;
                } else {
                    if (prev != node)
                        prev = union(prev, node);
                }
            } else {
                if (prev == null) {
                    prev = node.complement();
                } else {
                    if (prev != node)
                        prev = setDifference(prev, node);
                }
            }
            ch = peek();
        }
    }

    private CharProperty bitsOrSingle(BitClass bits, int ch) {
        /* Bits 只能处理 [u+0000-u+00ff] 范围内的代码点。
           在处理以下代码点的 Unicode 大小写折叠时，使用 "single" 节点而不是 bits。
           (1) 超出范围的大写字母：u+00ff, u+00b5
              toUpperCase(u+00ff) -> u+0178
              toUpperCase(u+00b5) -> u+039c
           (2) LatinSmallLetterLongS u+17f
              toUpperCase(u+017f) -> u+0053
           (3) LatinSmallLetterDotlessI u+131
              toUpperCase(u+0131) -> u+0049
           (4) LatinCapitalLetterIWithDotAbove u+0130
              toLowerCase(u+0130) -> u+0069
           (5) KelvinSign u+212a
              toLowerCase(u+212a) ==> u+006B
           (6) AngstromSign u+212b
              toLowerCase(u+212b) ==> u+00e5
        */
        int d;
        if (ch < 256 &&
            !(has(CASE_INSENSITIVE) && has(UNICODE_CASE) &&
              (ch == 0xff || ch == 0xb5 ||
               ch == 0x49 || ch == 0x69 ||  //I and i
               ch == 0x53 || ch == 0x73 ||  //S and s
               ch == 0x4b || ch == 0x6b ||  //K and k
               ch == 0xc5 || ch == 0xe5)))  //A+ring
            return bits.add(ch, flags());
        return newSingle(ch);
    }

    /**
     * 解析字符类中的单个字符或字符范围，并返回其代表节点。
     */
    private CharProperty range(BitClass bits) {
        int ch = peek();
        if (ch == '\\') {
            ch = nextEscaped();
            if (ch == 'p' || ch == 'P') { // 属性
                boolean comp = (ch == 'P');
                boolean oneLetter = true;
                // 如果存在 {，则消耗它
                ch = next();
                if (ch != '{')
                    unread();
                else
                    oneLetter = false;
                return family(oneLetter, comp);
            } else { // 普通转义
                boolean isrange = temp[cursor+1] == '-';
                unread();
                ch = escape(true, true, isrange);
                if (ch == -1)
                    return (CharProperty) root;
            }
        } else {
            next();
        }
        if (ch >= 0) {
            if (peek() == '-') {
                int endRange = temp[cursor+1];
                if (endRange == '[') {
                    return bitsOrSingle(bits, ch);
                }
                if (endRange != ']') {
                    next();
                    int m = peek();
                    if (m == '\\') {
                        m = escape(true, false, true);
                    } else {
                        next();
                    }
                    if (m < ch) {
                        throw error("非法的字符范围");
                    }
                    if (has(CASE_INSENSITIVE))
                        return caseInsensitiveRangeFor(ch, m);
                    else
                        return rangeFor(ch, m);
                }
            }
            return bitsOrSingle(bits, ch);
        }
        throw error("意外的字符 '" + ((char)ch) + "'");
    }

    /**
     * 解析 Unicode 字符族，并返回其代表节点。
     */
    private CharProperty family(boolean singleLetter,
                                boolean maybeComplement)
    {
        next();
        String name;
        CharProperty node = null;

        if (singleLetter) {
            int c = temp[cursor];
            if (!Character.isSupplementaryCodePoint(c)) {
                name = String.valueOf((char)c);
            } else {
                name = new String(temp, cursor, 1);
            }
            read();
        } else {
            int i = cursor;
            mark('}');
            while(read() != '}') {
            }
            mark('\000');
            int j = cursor;
            if (j > patternLength)
                throw error("未闭合的字符族");
            if (i + 1 >= j)
                throw error("空的字符族");
            name = new String(temp, i, j-i-1);
        }


                    int i = name.indexOf('=');
        if (i != -1) {
            // 属性构造 \p{name=value}
            String value = name.substring(i + 1);
            name = name.substring(0, i).toLowerCase(Locale.ENGLISH);
            if ("sc".equals(name) || "script".equals(name)) {
                node = unicodeScriptPropertyFor(value);
            } else if ("blk".equals(name) || "block".equals(name)) {
                node = unicodeBlockPropertyFor(value);
            } else if ("gc".equals(name) || "general_category".equals(name)) {
                node = charPropertyNodeFor(value);
            } else {
                throw error("未知的 Unicode 属性 {name=<" + name + ">, "
                             + "value=<" + value + ">}");
            }
        } else {
            if (name.startsWith("In")) {
                // \p{inBlockName}
                node = unicodeBlockPropertyFor(name.substring(2));
            } else if (name.startsWith("Is")) {
                // \p{isGeneralCategory} 和 \p{isScriptName}
                name = name.substring(2);
                UnicodeProp uprop = UnicodeProp.forName(name);
                if (uprop != null)
                    node = new Utype(uprop);
                if (node == null)
                    node = CharPropertyNames.charPropertyFor(name);
                if (node == null)
                    node = unicodeScriptPropertyFor(name);
            } else {
                if (has(UNICODE_CHARACTER_CLASS)) {
                    UnicodeProp uprop = UnicodeProp.forPOSIXName(name);
                    if (uprop != null)
                        node = new Utype(uprop);
                }
                if (node == null)
                    node = charPropertyNodeFor(name);
            }
        }
        if (maybeComplement) {
            if (node instanceof Category || node instanceof Block)
                hasSupplementary = true;
            node = node.complement();
        }
        return node;
    }


    /**
     * 返回一个匹配所有属于 UnicodeScript 的字符的 CharProperty。
     */
    private CharProperty unicodeScriptPropertyFor(String name) {
        final Character.UnicodeScript script;
        try {
            script = Character.UnicodeScript.forName(name);
        } catch (IllegalArgumentException iae) {
            throw error("未知的字符脚本名称 {" + name + "}");
        }
        return new Script(script);
    }

    /**
     * 返回一个匹配所有属于 UnicodeBlock 的字符的 CharProperty。
     */
    private CharProperty unicodeBlockPropertyFor(String name) {
        final Character.UnicodeBlock block;
        try {
            block = Character.UnicodeBlock.forName(name);
        } catch (IllegalArgumentException iae) {
            throw error("未知的字符块名称 {" + name + "}");
        }
        return new Block(block);
    }

    /**
     * 返回一个匹配所有属于命名属性的字符的 CharProperty。
     */
    private CharProperty charPropertyNodeFor(String name) {
        CharProperty p = CharPropertyNames.charPropertyFor(name);
        if (p == null)
            throw error("未知的字符属性名称 {" + name + "}");
        return p;
    }

    /**
     * 解析并返回一个“命名捕获组”的名称，解析后会消耗尾随的 ">"。
     */
    private String groupname(int ch) {
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toChars(ch));
        while (ASCII.isLower(ch=read()) || ASCII.isUpper(ch) ||
               ASCII.isDigit(ch)) {
            sb.append(Character.toChars(ch));
        }
        if (sb.length() == 0)
            throw error("命名捕获组的名称长度为 0");
        if (ch != '>')
            throw error("命名捕获组缺少尾随的 '>'");
        return sb.toString();
    }

    /**
     * 解析一个组并返回处理该组的一组节点的头节点。有时使用双重返回系统，其中尾节点通过 root 返回。
     */
    private Node group0() {
        boolean capturingGroup = false;
        Node head = null;
        Node tail = null;
        int save = flags;
        root = null;
        int ch = next();
        if (ch == '?') {
            ch = skip();
            switch (ch) {
            case ':':   //  (?:xxx) 纯组
                head = createGroup(true);
                tail = root;
                head.next = expr(tail);
                break;
            case '=':   // (?=xxx) 和 (?!xxx) 前瞻
            case '!':
                head = createGroup(true);
                tail = root;
                head.next = expr(tail);
                if (ch == '=') {
                    head = tail = new Pos(head);
                } else {
                    head = tail = new Neg(head);
                }
                break;
            case '>':   // (?>xxx) 独立组
                head = createGroup(true);
                tail = root;
                head.next = expr(tail);
                head = tail = new Ques(head, INDEPENDENT);
                break;
            case '<':   // (?<xxx) 回溯
                ch = read();
                if (ASCII.isLower(ch) || ASCII.isUpper(ch)) {
                    // 命名捕获组
                    String name = groupname(ch);
                    if (namedGroups().containsKey(name))
                        throw error("命名捕获组 <" + name
                                    + "> 已经定义");
                    capturingGroup = true;
                    head = createGroup(false);
                    tail = root;
                    namedGroups().put(name, capturingGroupCount-1);
                    head.next = expr(tail);
                    break;
                }
                int start = cursor;
                head = createGroup(true);
                tail = root;
                head.next = expr(tail);
                tail.next = lookbehindEnd;
                TreeInfo info = new TreeInfo();
                head.study(info);
                if (info.maxValid == false) {
                    throw error("回溯组没有明显的最大长度");
                }
                boolean hasSupplementary = findSupplementary(start, patternLength);
                if (ch == '=') {
                    head = tail = (hasSupplementary ?
                                   new BehindS(head, info.maxLength,
                                               info.minLength) :
                                   new Behind(head, info.maxLength,
                                              info.minLength));
                } else if (ch == '!') {
                    head = tail = (hasSupplementary ?
                                   new NotBehindS(head, info.maxLength,
                                                  info.minLength) :
                                   new NotBehind(head, info.maxLength,
                                                 info.minLength));
                } else {
                    throw error("未知的回溯组");
                }
                break;
            case '$':
            case '@':
                throw error("未知的组类型");
            default:    // (?xxx:) 内联匹配标志
                unread();
                addFlag();
                ch = read();
                if (ch == ')') {
                    return null;    // 仅内联修饰符
                }
                if (ch != ':') {
                    throw error("未知的内联修饰符");
                }
                head = createGroup(true);
                tail = root;
                head.next = expr(tail);
                break;
            }
        } else { // (xxx) 普通组
            capturingGroup = true;
            head = createGroup(false);
            tail = root;
            head.next = expr(tail);
        }

        accept(')', "未闭合的组");
        flags = save;

        // 检查量词
        Node node = closure(head);
        if (node == head) { // 无闭合
            root = tail;
            return node;    // 双重返回
        }
        if (head == tail) { // 零长度断言
            root = node;
            return node;    // 双重返回
        }

        if (node instanceof Ques) {
            Ques ques = (Ques) node;
            if (ques.type == POSSESSIVE) {
                root = node;
                return node;
            }
            tail.next = new BranchConn();
            tail = tail.next;
            if (ques.type == GREEDY) {
                head = new Branch(head, null, tail);
            } else { // 不情愿的量词
                head = new Branch(null, head, tail);
            }
            root = tail;
            return head;
        } else if (node instanceof Curly) {
            Curly curly = (Curly) node;
            if (curly.type == POSSESSIVE) {
                root = node;
                return node;
            }
            // 发现组是否确定
            TreeInfo info = new TreeInfo();
            if (head.study(info)) { // 确定
                GroupTail temp = (GroupTail) tail;
                head = root = new GroupCurly(head.next, curly.cmin,
                                   curly.cmax, curly.type,
                                   ((GroupTail)tail).localIndex,
                                   ((GroupTail)tail).groupIndex,
                                             capturingGroup);
                return head;
            } else { // 不确定
                int temp = ((GroupHead) head).localIndex;
                Loop loop;
                if (curly.type == GREEDY)
                    loop = new Loop(this.localCount, temp);
                else  // 不情愿的 Curly
                    loop = new LazyLoop(this.localCount, temp);
                Prolog prolog = new Prolog(loop);
                this.localCount += 1;
                loop.cmin = curly.cmin;
                loop.cmax = curly.cmax;
                loop.body = head;
                tail.next = loop;
                root = loop;
                return prolog; // 双重返回
            }
        }
        throw error("内部逻辑错误");
    }

    /**
     * 使用双重返回创建组头和尾节点。如果组是使用匿名 true 创建的，则它是一个纯组，不应影响组计数。
     */
    private Node createGroup(boolean anonymous) {
        int localIndex = localCount++;
        int groupIndex = 0;
        if (!anonymous)
            groupIndex = capturingGroupCount++;
        GroupHead head = new GroupHead(localIndex);
        root = new GroupTail(localIndex, groupIndex);
        if (!anonymous && groupIndex < 10)
            groupNodes[groupIndex] = head;
        return head;
    }

    @SuppressWarnings("fallthrough")
    /**
     * 解析内联匹配标志并适当设置它们。
     */
    private void addFlag() {
        int ch = peek();
        for (;;) {
            switch (ch) {
            case 'i':
                flags |= CASE_INSENSITIVE;
                break;
            case 'm':
                flags |= MULTILINE;
                break;
            case 's':
                flags |= DOTALL;
                break;
            case 'd':
                flags |= UNIX_LINES;
                break;
            case 'u':
                flags |= UNICODE_CASE;
                break;
            case 'c':
                flags |= CANON_EQ;
                break;
            case 'x':
                flags |= COMMENTS;
                break;
            case 'U':
                flags |= (UNICODE_CHARACTER_CLASS | UNICODE_CASE);
                break;
            case '-': // 子标志然后穿透
                ch = next();
                subFlag();
            default:
                return;
            }
            ch = next();
        }
    }

    @SuppressWarnings("fallthrough")
    /**
     * 解析内联匹配标志的第二部分并适当关闭标志。
     */
    private void subFlag() {
        int ch = peek();
        for (;;) {
            switch (ch) {
            case 'i':
                flags &= ~CASE_INSENSITIVE;
                break;
            case 'm':
                flags &= ~MULTILINE;
                break;
            case 's':
                flags &= ~DOTALL;
                break;
            case 'd':
                flags &= ~UNIX_LINES;
                break;
            case 'u':
                flags &= ~UNICODE_CASE;
                break;
            case 'c':
                flags &= ~CANON_EQ;
                break;
            case 'x':
                flags &= ~COMMENTS;
                break;
            case 'U':
                flags &= ~(UNICODE_CHARACTER_CLASS | UNICODE_CASE);
            default:
                return;
            }
            ch = next();
        }
    }

    static final int MAX_REPS   = 0x7FFFFFFF;

    static final int GREEDY     = 0;

    static final int LAZY       = 1;

    static final int POSSESSIVE = 2;

    static final int INDEPENDENT = 3;

    /**
     * 处理重复。如果下一个窥探的字符是量词，则必须附加新节点来处理重复。
     * Prev 可能是单个或组，因此它可能是一个节点链。
     */
    private Node closure(Node prev) {
        Node atom;
        int ch = peek();
        switch (ch) {
        case '?':
            ch = next();
            if (ch == '?') {
                next();
                return new Ques(prev, LAZY);
            } else if (ch == '+') {
                next();
                return new Ques(prev, POSSESSIVE);
            }
            return new Ques(prev, GREEDY);
        case '*':
            ch = next();
            if (ch == '?') {
                next();
                return new Curly(prev, 0, MAX_REPS, LAZY);
            } else if (ch == '+') {
                next();
                return new Curly(prev, 0, MAX_REPS, POSSESSIVE);
            }
            return new Curly(prev, 0, MAX_REPS, GREEDY);
        case '+':
            ch = next();
            if (ch == '?') {
                next();
                return new Curly(prev, 1, MAX_REPS, LAZY);
            } else if (ch == '+') {
                next();
                return new Curly(prev, 1, MAX_REPS, POSSESSIVE);
            }
            return new Curly(prev, 1, MAX_REPS, GREEDY);
        case '{':
            ch = temp[cursor+1];
            if (ASCII.isDigit(ch)) {
                skip();
                int cmin = 0;
                do {
                    cmin = cmin * 10 + (ch - '0');
                } while (ASCII.isDigit(ch = read()));
                int cmax = cmin;
                if (ch == ',') {
                    ch = read();
                    cmax = MAX_REPS;
                    if (ch != '}') {
                        cmax = 0;
                        while (ASCII.isDigit(ch)) {
                            cmax = cmax * 10 + (ch - '0');
                            ch = read();
                        }
                    }
                }
                if (ch != '}')
                    throw error("未闭合的计数闭合");
                if (((cmin) | (cmax) | (cmax - cmin)) < 0)
                    throw error("非法的重复范围");
                Curly curly;
                ch = peek();
                if (ch == '?') {
                    next();
                    curly = new Curly(prev, cmin, cmax, LAZY);
                } else if (ch == '+') {
                    next();
                    curly = new Curly(prev, cmin, cmax, POSSESSIVE);
                } else {
                    curly = new Curly(prev, cmin, cmax, GREEDY);
                }
                return curly;
            } else {
                throw error("非法的重复");
            }
        default:
            return prev;
        }
    }


/**
 * 用于解析控制转义序列的实用方法。
 */
private int c() {
    if (cursor < patternLength) {
        return read() ^ 64;
    }
    throw error("非法的控制转义序列");
}

/**
 * 用于解析八进制转义序列的实用方法。
 */
private int o() {
    int n = read();
    if (((n-'0')|('7'-n)) >= 0) {
        int m = read();
        if (((m-'0')|('7'-m)) >= 0) {
            int o = read();
            if ((((o-'0')|('7'-o)) >= 0) && (((n-'0')|('3'-n)) >= 0)) {
                return (n - '0') * 64 + (m - '0') * 8 + (o - '0');
            }
            unread();
            return (n - '0') * 8 + (m - '0');
        }
        unread();
        return (n - '0');
    }
    throw error("非法的八进制转义序列");
}

/**
 * 用于解析十六进制转义序列的实用方法。
 */
private int x() {
    int n = read();
    if (ASCII.isHexDigit(n)) {
        int m = read();
        if (ASCII.isHexDigit(m)) {
            return ASCII.toDigit(n) * 16 + ASCII.toDigit(m);
        }
    } else if (n == '{' && ASCII.isHexDigit(peek())) {
        int ch = 0;
        while (ASCII.isHexDigit(n = read())) {
            ch = (ch << 4) + ASCII.toDigit(n);
            if (ch > Character.MAX_CODE_POINT)
                throw error("十六进制码点太大");
        }
        if (n != '}')
            throw error("未闭合的十六进制转义序列");
        return ch;
    }
    throw error("非法的十六进制转义序列");
}

/**
 * 用于解析 Unicode 转义序列的实用方法。
 */
private int cursor() {
    return cursor;
}

private void setcursor(int pos) {
    cursor = pos;
}

private int uxxxx() {
    int n = 0;
    for (int i = 0; i < 4; i++) {
        int ch = read();
        if (!ASCII.isHexDigit(ch)) {
            throw error("非法的 Unicode 转义序列");
        }
        n = n * 16 + ASCII.toDigit(ch);
    }
    return n;
}

private int u() {
    int n = uxxxx();
    if (Character.isHighSurrogate((char)n)) {
        int cur = cursor();
        if (read() == '\\' && read() == 'u') {
            int n2 = uxxxx();
            if (Character.isLowSurrogate((char)n2))
                return Character.toCodePoint((char)n, (char)n2);
        }
        setcursor(cur);
    }
    return n;
}

//
// 用于码点支持的实用方法
//

private static final int countChars(CharSequence seq, int index,
                                    int lengthInCodePoints) {
    // 优化
    if (lengthInCodePoints == 1 && index >= 0 && index < seq.length() &&
        !Character.isHighSurrogate(seq.charAt(index))) {
        return 1;
    }
    int length = seq.length();
    int x = index;
    if (lengthInCodePoints >= 0) {
        assert ((length == 0 && index == 0) || index >= 0 && index < length);
        for (int i = 0; x < length && i < lengthInCodePoints; i++) {
            if (Character.isHighSurrogate(seq.charAt(x++))) {
                if (x < length && Character.isLowSurrogate(seq.charAt(x))) {
                    x++;
                }
            }
        }
        return x - index;
    }

    assert (index >= 0 && index <= length);
    if (index == 0) {
        return 0;
    }
    int len = -lengthInCodePoints;
    for (int i = 0; x > 0 && i < len; i++) {
        if (Character.isLowSurrogate(seq.charAt(--x))) {
            if (x > 0 && Character.isHighSurrogate(seq.charAt(x-1))) {
                x--;
            }
        }
    }
    return index - x;
}

private static final int countCodePoints(CharSequence seq) {
    int length = seq.length();
    int n = 0;
    for (int i = 0; i < length; ) {
        n++;
        if (Character.isHighSurrogate(seq.charAt(i++))) {
            if (i < length && Character.isLowSurrogate(seq.charAt(i))) {
                i++;
            }
        }
    }
    return n;
}

/**
 * 创建一个用于匹配 Latin-1 值的位向量。普通的 BitClass 从不匹配高于 Latin-1 的值，而补码 BitClass 总是匹配高于 Latin-1 的值。
 */
private static final class BitClass extends BmpCharProperty {
    final boolean[] bits;
    BitClass() { bits = new boolean[256]; }
    private BitClass(boolean[] bits) { this.bits = bits; }
    BitClass add(int c, int flags) {
        assert c >= 0 && c <= 255;
        if ((flags & CASE_INSENSITIVE) != 0) {
            if (ASCII.isAscii(c)) {
                bits[ASCII.toUpper(c)] = true;
                bits[ASCII.toLower(c)] = true;
            } else if ((flags & UNICODE_CASE) != 0) {
                bits[Character.toLowerCase(c)] = true;
                bits[Character.toUpperCase(c)] = true;
            }
        }
        bits[c] = true;
        return this;
    }
    boolean isSatisfiedBy(int ch) {
        return ch < 256 && bits[ch];
    }
}

/**
 * 返回一个优化的单字符匹配器。
 */
private CharProperty newSingle(final int ch) {
    if (has(CASE_INSENSITIVE)) {
        int lower, upper;
        if (has(UNICODE_CASE)) {
            upper = Character.toUpperCase(ch);
            lower = Character.toLowerCase(upper);
            if (upper != lower)
                return new SingleU(lower);
        } else if (ASCII.isAscii(ch)) {
            lower = ASCII.toLower(ch);
            upper = ASCII.toUpper(ch);
            if (lower != upper)
                return new SingleI(lower, upper);
        }
    }
    if (isSupplementary(ch))
        return new SingleS(ch);    // 匹配给定的 Unicode 字符
    return new Single(ch);         // 匹配给定的 BMP 字符
}

/**
 * 用于创建字符串切片匹配器的实用方法。
 */
private Node newSlice(int[] buf, int count, boolean hasSupplementary) {
    int[] tmp = new int[count];
    if (has(CASE_INSENSITIVE)) {
        if (has(UNICODE_CASE)) {
            for (int i = 0; i < count; i++) {
                tmp[i] = Character.toLowerCase(
                             Character.toUpperCase(buf[i]));
            }
            return hasSupplementary? new SliceUS(tmp) : new SliceU(tmp);
        }
        for (int i = 0; i < count; i++) {
            tmp[i] = ASCII.toLower(buf[i]);
        }
        return hasSupplementary? new SliceIS(tmp) : new SliceI(tmp);
    }
    for (int i = 0; i < count; i++) {
        tmp[i] = buf[i];
    }
    return hasSupplementary ? new SliceS(tmp) : new Slice(tmp);
}

/**
 * 以下类是表示编译正则表达式的对象树的构建组件。对象树由处理 Pattern 中构造的单个元素组成。每种类型的对象都知道如何使用 match() 方法匹配其等效构造。
 */

/**
 * 所有节点类的基类。子类应根据需要覆盖 match() 方法。此类是一个接受节点，因此其 match() 始终返回 true。
 */
static class Node extends Object {
    Node next;
    Node() {
        next = Pattern.accept;
    }
    /**
     * 此方法实现经典的接受节点。
     */
    boolean match(Matcher matcher, int i, CharSequence seq) {
        matcher.last = i;
        matcher.groups[0] = matcher.first;
        matcher.groups[1] = matcher.last;
        return true;
    }
    /**
     * 此方法适用于所有零长度断言。
     */
    boolean study(TreeInfo info) {
        if (next != null) {
            return next.study(info);
        } else {
            return info.deterministic;
        }
    }
}

static class LastNode extends Node {
    /**
     * 此方法实现经典的接受节点，增加了检查匹配是否使用了所有输入的检查。
     */
    boolean match(Matcher matcher, int i, CharSequence seq) {
        if (matcher.acceptMode == Matcher.ENDANCHOR && i != matcher.to)
            return false;
        matcher.last = i;
        matcher.groups[0] = matcher.first;
        matcher.groups[1] = matcher.last;
        return true;
    }
}

/**
 * 用于可以在输入字符串中任何位置开始的正则表达式。这基本上尝试在输入字符串的每个位置重复匹配，每次尝试后向前移动。锚定搜索或 BnM 将完全绕过此节点。
 */
static class Start extends Node {
    int minLength;
    Start(Node node) {
        this.next = node;
        TreeInfo info = new TreeInfo();
        next.study(info);
        minLength = info.minLength;
    }
    boolean match(Matcher matcher, int i, CharSequence seq) {
        if (i > matcher.to - minLength) {
            matcher.hitEnd = true;
            return false;
        }
        int guard = matcher.to - minLength;
        for (; i <= guard; i++) {
            if (next.match(matcher, i, seq)) {
                matcher.first = i;
                matcher.groups[0] = matcher.first;
                matcher.groups[1] = matcher.last;
                return true;
            }
        }
        matcher.hitEnd = true;
        return false;
    }
    boolean study(TreeInfo info) {
        next.study(info);
        info.maxValid = false;
        info.deterministic = false;
        return false;
    }
}

/*
 * StartS 支持补充字符，包括未配对的代理字符。
 */
static final class StartS extends Start {
    StartS(Node node) {
        super(node);
    }
    boolean match(Matcher matcher, int i, CharSequence seq) {
        if (i > matcher.to - minLength) {
            matcher.hitEnd = true;
            return false;
        }
        int guard = matcher.to - minLength;
        while (i <= guard) {
            //if ((ret = next.match(matcher, i, seq)) || i == guard)
            if (next.match(matcher, i, seq)) {
                matcher.first = i;
                matcher.groups[0] = matcher.first;
                matcher.groups[1] = matcher.last;
                return true;
            }
            if (i == guard)
                break;
            // 优化以移动到下一个字符。这比 countChars(seq, i, 1) 更快。
            if (Character.isHighSurrogate(seq.charAt(i++))) {
                if (i < seq.length() &&
                    Character.isLowSurrogate(seq.charAt(i))) {
                    i++;
                }
            }
        }
        matcher.hitEnd = true;
        return false;
    }
}

/**
 * 锚定在输入开始处的节点。此对象实现 \A 序列的匹配，如果不在多行模式下，插入符号锚点将使用此对象。
 */
static final class Begin extends Node {
    boolean match(Matcher matcher, int i, CharSequence seq) {
        int fromIndex = (matcher.anchoringBounds) ?
            matcher.from : 0;
        if (i == fromIndex && next.match(matcher, i, seq)) {
            matcher.first = i;
            matcher.groups[0] = i;
            matcher.groups[1] = matcher.last;
            return true;
        } else {
            return false;
        }
    }
}

/**
 * 锚定在输入结束处的节点。这是绝对的结束，因此这不应在最后一个换行符之前匹配，就像 $ 会匹配的那样。
 */
static final class End extends Node {
    boolean match(Matcher matcher, int i, CharSequence seq) {
        int endIndex = (matcher.anchoringBounds) ?
            matcher.to : matcher.getTextLength();
        if (i == endIndex) {
            matcher.hitEnd = true;
            return next.match(matcher, i, seq);
        }
        return false;
    }
}

/**
 * 锚定在行开始处的节点。这基本上是多行模式下 ^ 的匹配对象。
 */
static final class Caret extends Node {
    boolean match(Matcher matcher, int i, CharSequence seq) {
        int startIndex = matcher.from;
        int endIndex = matcher.to;
        if (!matcher.anchoringBounds) {
            startIndex = 0;
            endIndex = matcher.getTextLength();
        }
        // Perl 不会在输入结束处匹配 ^，即使在换行符之后
        if (i == endIndex) {
            matcher.hitEnd = true;
            return false;
        }
        if (i > startIndex) {
            char ch = seq.charAt(i-1);
            if (ch != '\n' && ch != '\r'
                && (ch|1) != '\u2029'
                && ch != '\u0085' ) {
                return false;
            }
            // 应将 \r\n 视为一个换行符
            if (ch == '\r' && seq.charAt(i) == '\n')
                return false;
        }
        return next.match(matcher, i, seq);
    }
}

/**
 * 在 unixdot 模式下行开始处锚定的节点。
 */
static final class UnixCaret extends Node {
    boolean match(Matcher matcher, int i, CharSequence seq) {
        int startIndex = matcher.from;
        int endIndex = matcher.to;
        if (!matcher.anchoringBounds) {
            startIndex = 0;
            endIndex = matcher.getTextLength();
        }
        // Perl 不会在输入结束处匹配 ^，即使在换行符之后
        if (i == endIndex) {
            matcher.hitEnd = true;
            return false;
        }
        if (i > startIndex) {
            char ch = seq.charAt(i-1);
            if (ch != '\n') {
                return false;
            }
        }
        return next.match(matcher, i, seq);
    }
}

/**
 * 匹配上次匹配结束位置的节点。这是用于 \G 构造的。
 */
static final class LastMatch extends Node {
    boolean match(Matcher matcher, int i, CharSequence seq) {
        if (i != matcher.oldLast)
            return false;
        return next.match(matcher, i, seq);
    }
}

/**
 * 根据多行模式在行结束处或输入结束处锚定的节点。
 *
 * 当不在多行模式下时，$ 只能在输入的非常末尾匹配，除非输入以行终止符结束，这时它在最后一个行终止符之前匹配。
 *
 * 注意 \r\n 被视为一个原子行终止符。
 *
 * 像 ^ 一样，$ 操作符在位置匹配，而不是匹配行终止符本身。
 */
static final class Dollar extends Node {
    boolean multiline;
    Dollar(boolean mul) {
        multiline = mul;
    }
    boolean match(Matcher matcher, int i, CharSequence seq) {
        int endIndex = (matcher.anchoringBounds) ?
            matcher.to : matcher.getTextLength();
        if (!multiline) {
            if (i < endIndex - 2)
                return false;
            if (i == endIndex - 2) {
                char ch = seq.charAt(i);
                if (ch != '\r')
                    return false;
                ch = seq.charAt(i + 1);
                if (ch != '\n')
                    return false;
            }
        }
        // 在任何行终止符之前匹配；也匹配在输入结束处
        // 如果是多行模式，我们在这里匹配
        // 如果不是多行模式，跳过以便标记结束
        // 这必须是一个 \r\n 或 \n
        // 在非常末尾，因此结束被命中；更多输入
        // 可能会使这里不匹配
        if (i < endIndex) {
            char ch = seq.charAt(i);
             if (ch == '\n') {
                 // 不在 \r\n 之间匹配
                 if (i > 0 && seq.charAt(i-1) == '\r')
                     return false;
                 if (multiline)
                     return next.match(matcher, i, seq);
             } else if (ch == '\r' || ch == '\u0085' ||
                        (ch|1) == '\u2029') {
                 if (multiline)
                     return next.match(matcher, i, seq);
             } else { // 没有行终止符，不匹配
                 return false;
             }
        }
        // 在当前结束处匹配，因此命中结束
        matcher.hitEnd = true;
        // 如果 $ 因为输入结束而匹配，那么更多输入
        // 可能会使它失败！
        matcher.requireEnd = true;
        return next.match(matcher, i, seq);
    }
    boolean study(TreeInfo info) {
        next.study(info);
        return info.deterministic;
    }
}


                /**
     * 在 unix 行模式下，根据多行模式在行尾或输入结束处锚定的节点。
     */
    static final class UnixDollar extends Node {
        boolean multiline;
        UnixDollar(boolean mul) {
            multiline = mul;
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int endIndex = (matcher.anchoringBounds) ?
                matcher.to : matcher.getTextLength();
            if (i < endIndex) {
                char ch = seq.charAt(i);
                if (ch == '\n') {
                    // 如果不是多行模式，则只能在非常末尾或倒数第二个位置匹配
                    if (multiline == false && i != endIndex - 1)
                        return false;
                    // 如果是多行模式，则返回 next.match 而不设置 matcher.hitEnd
                    if (multiline)
                        return next.match(matcher, i, seq);
                } else {
                    return false;
                }
            }
            // 由于在末尾或倒数第二个位置匹配；更多的输入可能会改变这一点，因此设置 hitEnd
            matcher.hitEnd = true;
            // 如果因为输入结束而匹配 $，则更多的输入可能会导致它失败！
            matcher.requireEnd = true;
            return next.match(matcher, i, seq);
        }
        boolean study(TreeInfo info) {
            next.study(info);
            return info.deterministic;
        }
    }

    /**
     * 匹配 Unicode 行结束符 '\R' 的节点类。
     */
    static final class LineEnding extends Node {
        boolean match(Matcher matcher, int i, CharSequence seq) {
            // (u+000Du+000A|[u+000Au+000Bu+000Cu+000Du+0085u+2028u+2029])
            if (i < matcher.to) {
                int ch = seq.charAt(i);
                if (ch == 0x0A || ch == 0x0B || ch == 0x0C ||
                    ch == 0x85 || ch == 0x2028 || ch == 0x2029)
                    return next.match(matcher, i + 1, seq);
                if (ch == 0x0D) {
                    i++;
                    if (i < matcher.to && seq.charAt(i) == 0x0A)
                        i++;
                    return next.match(matcher, i, seq);
                }
            } else {
                matcher.hitEnd = true;
            }
            return false;
        }
        boolean study(TreeInfo info) {
            info.minLength++;
            info.maxLength += 2;
            return next.study(info);
        }
    }

    /**
     * 抽象节点类，用于匹配满足某些布尔属性的一个字符。
     */
    private static abstract class CharProperty extends Node {
        abstract boolean isSatisfiedBy(int ch);
        CharProperty complement() {
            return new CharProperty() {
                    boolean isSatisfiedBy(int ch) {
                        return ! CharProperty.this.isSatisfiedBy(ch);}};
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            if (i < matcher.to) {
                int ch = Character.codePointAt(seq, i);
                return isSatisfiedBy(ch)
                    && next.match(matcher, i+Character.charCount(ch), seq);
            } else {
                matcher.hitEnd = true;
                return false;
            }
        }
        boolean study(TreeInfo info) {
            info.minLength++;
            info.maxLength++;
            return next.study(info);
        }
    }

    /**
     * 优化版本的 CharProperty，仅适用于从不满足补充字符属性的情况。
     */
    private static abstract class BmpCharProperty extends CharProperty {
        boolean match(Matcher matcher, int i, CharSequence seq) {
            if (i < matcher.to) {
                return isSatisfiedBy(seq.charAt(i))
                    && next.match(matcher, i+1, seq);
            } else {
                matcher.hitEnd = true;
                return false;
            }
        }
    }

    /**
     * 匹配一个补充 Unicode 字符的节点类。
     */
    static final class SingleS extends CharProperty {
        final int c;
        SingleS(int c) { this.c = c; }
        boolean isSatisfiedBy(int ch) {
            return ch == c;
        }
    }

    /**
     * 优化 —— 匹配给定的 BMP 字符。
     */
    static final class Single extends BmpCharProperty {
        final int c;
        Single(int c) { this.c = c; }
        boolean isSatisfiedBy(int ch) {
            return ch == c;
        }
    }

    /**
     * 匹配给定的 BMP 字符（不区分大小写）。
     */
    static final class SingleI extends BmpCharProperty {
        final int lower;
        final int upper;
        SingleI(int lower, int upper) {
            this.lower = lower;
            this.upper = upper;
        }
        boolean isSatisfiedBy(int ch) {
            return ch == lower || ch == upper;
        }
    }

    /**
     * 匹配给定的 Unicode 字符（Unicode 不区分大小写）。
     */
    static final class SingleU extends CharProperty {
        final int lower;
        SingleU(int lower) {
            this.lower = lower;
        }
        boolean isSatisfiedBy(int ch) {
            return lower == ch ||
                lower == Character.toLowerCase(Character.toUpperCase(ch));
        }
    }

    /**
     * 匹配 Unicode 块的节点类。
     */
    static final class Block extends CharProperty {
        final Character.UnicodeBlock block;
        Block(Character.UnicodeBlock block) {
            this.block = block;
        }
        boolean isSatisfiedBy(int ch) {
            return block == Character.UnicodeBlock.of(ch);
        }
    }

    /**
     * 匹配 Unicode 脚本的节点类。
     */
    static final class Script extends CharProperty {
        final Character.UnicodeScript script;
        Script(Character.UnicodeScript script) {
            this.script = script;
        }
        boolean isSatisfiedBy(int ch) {
            return script == Character.UnicodeScript.of(ch);
        }
    }

    /**
     * 匹配 Unicode 类别的节点类。
     */
    static final class Category extends CharProperty {
        final int typeMask;
        Category(int typeMask) { this.typeMask = typeMask; }
        boolean isSatisfiedBy(int ch) {
            return (typeMask & (1 << Character.getType(ch))) != 0;
        }
    }

    /**
     * 匹配 Unicode "类型" 的节点类。
     */
    static final class Utype extends CharProperty {
        final UnicodeProp uprop;
        Utype(UnicodeProp uprop) { this.uprop = uprop; }
        boolean isSatisfiedBy(int ch) {
            return uprop.is(ch);
        }
    }

    /**
     * 匹配 POSIX 类型的节点类。
     */
    static final class Ctype extends BmpCharProperty {
        final int ctype;
        Ctype(int ctype) { this.ctype = ctype; }
        boolean isSatisfiedBy(int ch) {
            return ch < 128 && ASCII.isType(ch, ctype);
        }
    }

    /**
     * 匹配 Perl 垂直空白的节点类。
     */
    static final class VertWS extends BmpCharProperty {
        boolean isSatisfiedBy(int cp) {
            return (cp >= 0x0A && cp <= 0x0D) ||
                   cp == 0x85 || cp == 0x2028 || cp == 0x2029;
        }
    }

    /**
     * 匹配 Perl 水平空白的节点类。
     */
    static final class HorizWS extends BmpCharProperty {
        boolean isSatisfiedBy(int cp) {
            return cp == 0x09 || cp == 0x20 || cp == 0xa0 ||
                   cp == 0x1680 || cp == 0x180e ||
                   cp >= 0x2000 && cp <= 0x200a ||
                   cp == 0x202f || cp == 0x205f || cp == 0x3000;
        }
    }

    /**
     * 所有 Slice 节点的基类。
     */
    static class SliceNode extends Node {
        int[] buffer;
        SliceNode(int[] buf) {
            buffer = buf;
        }
        boolean study(TreeInfo info) {
            info.minLength += buffer.length;
            info.maxLength += buffer.length;
            return next.study(info);
        }
    }

    /**
     * 用于匹配大小写敏感/仅 BMP 的字面字符序列的节点类。
     */
    static final class Slice extends SliceNode {
        Slice(int[] buf) {
            super(buf);
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int[] buf = buffer;
            int len = buf.length;
            for (int j=0; j<len; j++) {
                if ((i+j) >= matcher.to) {
                    matcher.hitEnd = true;
                    return false;
                }
                if (buf[j] != seq.charAt(i+j))
                    return false;
            }
            return next.match(matcher, i+len, seq);
        }
    }

    /**
     * 用于匹配大小写不敏感/仅 BMP 的字面字符序列的节点类。
     */
    static class SliceI extends SliceNode {
        SliceI(int[] buf) {
            super(buf);
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int[] buf = buffer;
            int len = buf.length;
            for (int j=0; j<len; j++) {
                if ((i+j) >= matcher.to) {
                    matcher.hitEnd = true;
                    return false;
                }
                int c = seq.charAt(i+j);
                if (buf[j] != c &&
                    buf[j] != ASCII.toLower(c))
                    return false;
            }
            return next.match(matcher, i+len, seq);
        }
    }

    /**
     * 用于匹配大小写不敏感/仅 BMP 的字面字符序列的节点类。使用 Unicode 大小写折叠。
     */
    static final class SliceU extends SliceNode {
        SliceU(int[] buf) {
            super(buf);
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int[] buf = buffer;
            int len = buf.length;
            for (int j=0; j<len; j++) {
                if ((i+j) >= matcher.to) {
                    matcher.hitEnd = true;
                    return false;
                }
                int c = seq.charAt(i+j);
                if (buf[j] != c &&
                    buf[j] != Character.toLowerCase(Character.toUpperCase(c)))
                    return false;
            }
            return next.match(matcher, i+len, seq);
        }
    }

    /**
     * 用于匹配大小写敏感的字面字符序列（包括补充字符）的节点类。
     */
    static final class SliceS extends SliceNode {
        SliceS(int[] buf) {
            super(buf);
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int[] buf = buffer;
            int x = i;
            for (int j = 0; j < buf.length; j++) {
                if (x >= matcher.to) {
                    matcher.hitEnd = true;
                    return false;
                }
                int c = Character.codePointAt(seq, x);
                if (buf[j] != c)
                    return false;
                x += Character.charCount(c);
                if (x > matcher.to) {
                    matcher.hitEnd = true;
                    return false;
                }
            }
            return next.match(matcher, x, seq);
        }
    }

    /**
     * 用于匹配大小写不敏感的字面字符序列（包括补充字符）的节点类。
     */
    static class SliceIS extends SliceNode {
        SliceIS(int[] buf) {
            super(buf);
        }
        int toLower(int c) {
            return ASCII.toLower(c);
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int[] buf = buffer;
            int x = i;
            for (int j = 0; j < buf.length; j++) {
                if (x >= matcher.to) {
                    matcher.hitEnd = true;
                    return false;
                }
                int c = Character.codePointAt(seq, x);
                if (buf[j] != c && buf[j] != toLower(c))
                    return false;
                x += Character.charCount(c);
                if (x > matcher.to) {
                    matcher.hitEnd = true;
                    return false;
                }
            }
            return next.match(matcher, x, seq);
        }
    }

    /**
     * 用于匹配大小写不敏感的字面字符序列的节点类。使用 Unicode 大小写折叠。
     */
    static final class SliceUS extends SliceIS {
        SliceUS(int[] buf) {
            super(buf);
        }
        int toLower(int c) {
            return Character.toLowerCase(Character.toUpperCase(c));
        }
    }

    private static boolean inRange(int lower, int ch, int upper) {
        return lower <= ch && ch <= upper;
    }

    /**
     * 返回用于匹配显式值范围内的字符的节点。
     */
    private static CharProperty rangeFor(final int lower,
                                         final int upper) {
        return new CharProperty() {
                boolean isSatisfiedBy(int ch) {
                    return inRange(lower, ch, upper);}};
    }

    /**
     * 返回用于以大小写不敏感方式匹配显式值范围内的字符的节点。
     */
    private CharProperty caseInsensitiveRangeFor(final int lower,
                                                 final int upper) {
        if (has(UNICODE_CASE))
            return new CharProperty() {
                boolean isSatisfiedBy(int ch) {
                    if (inRange(lower, ch, upper))
                        return true;
                    int up = Character.toUpperCase(ch);
                    return inRange(lower, up, upper) ||
                           inRange(lower, Character.toLowerCase(up), upper);}};
        return new CharProperty() {
            boolean isSatisfiedBy(int ch) {
                return inRange(lower, ch, upper) ||
                    ASCII.isAscii(ch) &&
                        (inRange(lower, ASCII.toUpper(ch), upper) ||
                         inRange(lower, ASCII.toLower(ch), upper));
            }};
    }

    /**
     * 实现 Unicode 类别 ALL 和点元字符在 dotall 模式下的匹配。
     */
    static final class All extends CharProperty {
        boolean isSatisfiedBy(int ch) {
            return true;
        }
    }

    /**
     * 在未启用 dotall 模式时，用于点元字符的节点类。
     */
    static final class Dot extends CharProperty {
        boolean isSatisfiedBy(int ch) {
            return (ch != '\n' && ch != '\r'
                    && (ch|1) != '\u2029'
                    && ch != '\u0085');
        }
    }

    /**
     * 在未启用 dotall 模式但启用 UNIX_LINES 模式时，用于点元字符的节点类。
     */
    static final class UnixDot extends CharProperty {
        boolean isSatisfiedBy(int ch) {
            return ch != '\n';
        }
    }

    /**
     * 0 或 1 量词。这个类实现了所有三种类型。
     */
    static final class Ques extends Node {
        Node atom;
        int type;
        Ques(Node node, int type) {
            this.atom = node;
            this.type = type;
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            switch (type) {
            case GREEDY:
                return (atom.match(matcher, i, seq) && next.match(matcher, matcher.last, seq))
                    || next.match(matcher, i, seq);
            case LAZY:
                return next.match(matcher, i, seq)
                    || (atom.match(matcher, i, seq) && next.match(matcher, matcher.last, seq));
            case POSSESSIVE:
                if (atom.match(matcher, i, seq)) i = matcher.last;
                return next.match(matcher, i, seq);
            default:
                return atom.match(matcher, i, seq) && next.match(matcher, matcher.last, seq);
            }
        }
        boolean study(TreeInfo info) {
            if (type != INDEPENDENT) {
                int minL = info.minLength;
                atom.study(info);
                info.minLength = minL;
                info.deterministic = false;
                return next.study(info);
            } else {
                atom.study(info);
                return next.study(info);
            }
        }
    }


                /**
     * 处理带有指定最小和最大出现次数的大括号风格的重复。* 量词作为特殊情况处理。
     * 该类处理三种类型。
     */
    static final class Curly extends Node {
        Node atom;
        int type;
        int cmin;
        int cmax;

        Curly(Node node, int cmin, int cmax, int type) {
            this.atom = node;
            this.type = type;
            this.cmin = cmin;
            this.cmax = cmax;
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int j;
            for (j = 0; j < cmin; j++) {
                if (atom.match(matcher, i, seq)) {
                    i = matcher.last;
                    continue;
                }
                return false;
            }
            if (type == GREEDY)
                return match0(matcher, i, j, seq);
            else if (type == LAZY)
                return match1(matcher, i, j, seq);
            else
                return match2(matcher, i, j, seq);
        }
        // 贪婪匹配。
        // i 是开始匹配的索引
        // j 是已匹配的原子数量
        boolean match0(Matcher matcher, int i, int j, CharSequence seq) {
            if (j >= cmax) {
                // 已匹配最大数量... 继续匹配其余的正则表达式
                return next.match(matcher, i, seq);
            }
            int backLimit = j;
            while (atom.match(matcher, i, seq)) {
                // k 是此次匹配的长度
                int k = matcher.last - i;
                if (k == 0) // 零长度匹配
                    break;
                // 移动索引和已匹配数量
                i = matcher.last;
                j++;
                // 贪婪匹配，尽可能多地匹配
                while (j < cmax) {
                    if (!atom.match(matcher, i, seq))
                        break;
                    if (i + k != matcher.last) {
                        if (match0(matcher, matcher.last, j+1, seq))
                            return true;
                        break;
                    }
                    i += k;
                    j++;
                }
                // 处理匹配失败时的回退
                while (j >= backLimit) {
                   if (next.match(matcher, i, seq))
                        return true;
                    i -= k;
                    j--;
                }
                return false;
            }
            return next.match(matcher, i, seq);
        }
        // 不情愿匹配。此时，最小匹配数量已满足。
        // i 是开始匹配的索引
        // j 是已匹配的原子数量
        boolean match1(Matcher matcher, int i, int j, CharSequence seq) {
            for (;;) {
                // 尝试在不消耗更多的情况下完成匹配
                if (next.match(matcher, i, seq))
                    return true;
                // 达到最大数量，未找到匹配
                if (j >= cmax)
                    return false;
                // 好的，必须再尝试一个原子
                if (!atom.match(matcher, i, seq))
                    return false;
                // 如果没有向前移动则必须退出
                if (i == matcher.last)
                    return false;
                // 移动索引和已匹配数量
                i = matcher.last;
                j++;
            }
        }
        boolean match2(Matcher matcher, int i, int j, CharSequence seq) {
            for (; j < cmax; j++) {
                if (!atom.match(matcher, i, seq))
                    break;
                if (i == matcher.last)
                    break;
                i = matcher.last;
            }
            return next.match(matcher, i, seq);
        }
        boolean study(TreeInfo info) {
            // 保存原始信息
            int minL = info.minLength;
            int maxL = info.maxLength;
            boolean maxV = info.maxValid;
            boolean detm = info.deterministic;
            info.reset();

            atom.study(info);

            int temp = info.minLength * cmin + minL;
            if (temp < minL) {
                temp = 0xFFFFFFF; // 任意大的数字
            }
            info.minLength = temp;

            if (maxV & info.maxValid) {
                temp = info.maxLength * cmax + maxL;
                info.maxLength = temp;
                if (temp < maxL) {
                    info.maxValid = false;
                }
            } else {
                info.maxValid = false;
            }

            if (info.deterministic && cmin == cmax)
                info.deterministic = detm;
            else
                info.deterministic = false;
            return next.study(info);
        }
    }

    /**
     * 处理确定性情况下的带有指定最小和最大出现次数的大括号风格的重复。这是对 Prolog 和 Loop 系统的迭代优化，
     * 后者会以递归方式处理这种情况。* 量词作为特殊情况处理。
     * 如果 capture 为 true，则该类保存组设置，并确保在组匹配回退时取消设置组。
     */
    static final class GroupCurly extends Node {
        Node atom;
        int type;
        int cmin;
        int cmax;
        int localIndex;
        int groupIndex;
        boolean capture;

        GroupCurly(Node node, int cmin, int cmax, int type, int local,
                   int group, boolean capture) {
            this.atom = node;
            this.type = type;
            this.cmin = cmin;
            this.cmax = cmax;
            this.localIndex = local;
            this.groupIndex = group;
            this.capture = capture;
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int[] groups = matcher.groups;
            int[] locals = matcher.locals;
            int save0 = locals[localIndex];
            int save1 = 0;
            int save2 = 0;

            if (capture) {
                save1 = groups[groupIndex];
                save2 = groups[groupIndex+1];
            }

            // 通知 GroupTail 无需设置组信息
            // 因为它将在这里设置
            locals[localIndex] = -1;

            boolean ret = true;
            for (int j = 0; j < cmin; j++) {
                if (atom.match(matcher, i, seq)) {
                    if (capture) {
                        groups[groupIndex] = i;
                        groups[groupIndex+1] = matcher.last;
                    }
                    i = matcher.last;
                } else {
                    ret = false;
                    break;
                }
            }
            if (ret) {
                if (type == GREEDY) {
                    ret = match0(matcher, i, cmin, seq);
                } else if (type == LAZY) {
                    ret = match1(matcher, i, cmin, seq);
                } else {
                    ret = match2(matcher, i, cmin, seq);
                }
            }
            if (!ret) {
                locals[localIndex] = save0;
                if (capture) {
                    groups[groupIndex] = save1;
                    groups[groupIndex+1] = save2;
                }
            }
            return ret;
        }
        // 激进的组匹配
        boolean match0(Matcher matcher, int i, int j, CharSequence seq) {
            // 不回退到起始的 "j"
            int min = j;
            int[] groups = matcher.groups;
            int save0 = 0;
            int save1 = 0;
            if (capture) {
                save0 = groups[groupIndex];
                save1 = groups[groupIndex+1];
            }
            for (;;) {
                if (j >= cmax)
                    break;
                if (!atom.match(matcher, i, seq))
                    break;
                int k = matcher.last - i;
                if (k <= 0) {
                    if (capture) {
                        groups[groupIndex] = i;
                        groups[groupIndex+1] = i + k;
                    }
                    i = i + k;
                    break;
                }
                for (;;) {
                    if (capture) {
                        groups[groupIndex] = i;
                        groups[groupIndex+1] = i + k;
                    }
                    i = i + k;
                    if (++j >= cmax)
                        break;
                    if (!atom.match(matcher, i, seq))
                        break;
                    if (i + k != matcher.last) {
                        if (match0(matcher, i, j, seq))
                            return true;
                        break;
                    }
                }
                while (j > min) {
                    if (next.match(matcher, i, seq)) {
                        if (capture) {
                            groups[groupIndex+1] = i;
                            groups[groupIndex] = i - k;
                        }
                        return true;
                    }
                    // 回退
                    i = i - k;
                    if (capture) {
                        groups[groupIndex+1] = i;
                        groups[groupIndex] = i - k;
                    }
                    j--;

                }
                break;
            }
            if (capture) {
                groups[groupIndex] = save0;
                groups[groupIndex+1] = save1;
            }
            return next.match(matcher, i, seq);
        }
        // 不情愿匹配
        boolean match1(Matcher matcher, int i, int j, CharSequence seq) {
            for (;;) {
                if (next.match(matcher, i, seq))
                    return true;
                if (j >= cmax)
                    return false;
                if (!atom.match(matcher, i, seq))
                    return false;
                if (i == matcher.last)
                    return false;
                if (capture) {
                    matcher.groups[groupIndex] = i;
                    matcher.groups[groupIndex+1] = matcher.last;
                }
                i = matcher.last;
                j++;
            }
        }
        // 拥有匹配
        boolean match2(Matcher matcher, int i, int j, CharSequence seq) {
            for (; j < cmax; j++) {
                if (!atom.match(matcher, i, seq)) {
                    break;
                }
                if (capture) {
                    matcher.groups[groupIndex] = i;
                    matcher.groups[groupIndex+1] = matcher.last;
                }
                if (i == matcher.last) {
                    break;
                }
                i = matcher.last;
            }
            return next.match(matcher, i, seq);
        }
        boolean study(TreeInfo info) {
            // 保存原始信息
            int minL = info.minLength;
            int maxL = info.maxLength;
            boolean maxV = info.maxValid;
            boolean detm = info.deterministic;
            info.reset();

            atom.study(info);

            int temp = info.minLength * cmin + minL;
            if (temp < minL) {
                temp = 0xFFFFFFF; // 任意大的数字
            }
            info.minLength = temp;

            if (maxV & info.maxValid) {
                temp = info.maxLength * cmax + maxL;
                info.maxLength = temp;
                if (temp < maxL) {
                    info.maxValid = false;
                }
            } else {
                info.maxValid = false;
            }

            if (info.deterministic && cmin == cmax) {
                info.deterministic = detm;
            } else {
                info.deterministic = false;
            }
            return next.study(info);
        }
    }

    /**
     * 每个原子节点分支末尾的 Guard 节点。它的作用是将 "match" 操作链接到 "next"，
     * 但不链接 "study"，这样我们可以在不包括 "next" 的 TreeInfo 的情况下收集每个原子节点的 TreeInfo。
     */
    static final class BranchConn extends Node {
        BranchConn() {};
        boolean match(Matcher matcher, int i, CharSequence seq) {
            return next.match(matcher, i, seq);
        }
        boolean study(TreeInfo info) {
            return info.deterministic;
        }
    }

    /**
     * 处理交替的分支。注意这也用于 ? 量词，以在匹配一次和不出现之间进行分支。
     */
    static final class Branch extends Node {
        Node[] atoms = new Node[2];
        int size = 2;
        Node conn;
        Branch(Node first, Node second, Node branchConn) {
            conn = branchConn;
            atoms[0] = first;
            atoms[1] = second;
        }

        void add(Node node) {
            if (size >= atoms.length) {
                Node[] tmp = new Node[atoms.length*2];
                System.arraycopy(atoms, 0, tmp, 0, atoms.length);
                atoms = tmp;
            }
            atoms[size++] = node;
        }

        boolean match(Matcher matcher, int i, CharSequence seq) {
            for (int n = 0; n < size; n++) {
                if (atoms[n] == null) {
                    if (conn.next.match(matcher, i, seq))
                        return true;
                } else if (atoms[n].match(matcher, i, seq)) {
                    return true;
                }
            }
            return false;
        }

        boolean study(TreeInfo info) {
            int minL = info.minLength;
            int maxL = info.maxLength;
            boolean maxV = info.maxValid;

            int minL2 = Integer.MAX_VALUE; // 任意大的数字
            int maxL2 = -1;
            for (int n = 0; n < size; n++) {
                info.reset();
                if (atoms[n] != null)
                    atoms[n].study(info);
                minL2 = Math.min(minL2, info.minLength);
                maxL2 = Math.max(maxL2, info.maxLength);
                maxV = (maxV & info.maxValid);
            }

            minL += minL2;
            maxL += maxL2;

            info.reset();
            conn.next.study(info);

            info.minLength += minL;
            info.maxLength += maxL;
            info.maxValid &= maxV;
            info.deterministic = false;
            return false;
        }
    }

    /**
     * GroupHead 保存组开始的位置在 locals 中，并在匹配完成后恢复它们。
     *
     * matchRef 用于在表达式的后面引用此组时。locals 将包含一个负值，以指示如果引用不匹配，则不取消设置组。
     */
    static final class GroupHead extends Node {
        int localIndex;
        GroupHead(int localCount) {
            localIndex = localCount;
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int save = matcher.locals[localIndex];
            matcher.locals[localIndex] = i;
            boolean ret = next.match(matcher, i, seq);
            matcher.locals[localIndex] = save;
            return ret;
        }
        boolean matchRef(Matcher matcher, int i, CharSequence seq) {
            int save = matcher.locals[localIndex];
            matcher.locals[localIndex] = ~i; // HACK
            boolean ret = next.match(matcher, i, seq);
            matcher.locals[localIndex] = save;
            return ret;
        }
    }


                /**
     * 递归引用正则表达式中的一个组。它调用 matchRef，因为如果引用匹配失败，我们不会取消设置该组。
     */
    static final class GroupRef extends Node {
        GroupHead head;
        GroupRef(GroupHead head) {
            this.head = head;
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            return head.matchRef(matcher, i, seq)
                && next.match(matcher, matcher.last, seq);
        }
        boolean study(TreeInfo info) {
            info.maxValid = false;
            info.deterministic = false;
            return next.study(info);
        }
    }

    /**
     * GroupTail 处理组成功匹配时的组开始和结束位置的设置。它还必须能够取消设置需要回溯的组。
     *
     * 当引用前一个组时，GroupTail 节点也被使用，在这种情况下不需要设置任何组信息。
     */
    static final class GroupTail extends Node {
        int localIndex;
        int groupIndex;
        GroupTail(int localCount, int groupCount) {
            localIndex = localCount;
            groupIndex = groupCount + groupCount;
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int tmp = matcher.locals[localIndex];
            if (tmp >= 0) { // 这是正常的组情况。
                // 保存组，以便如果它回溯匹配时可以取消设置。
                int groupStart = matcher.groups[groupIndex];
                int groupEnd = matcher.groups[groupIndex+1];

                matcher.groups[groupIndex] = tmp;
                matcher.groups[groupIndex+1] = i;
                if (next.match(matcher, i, seq)) {
                    return true;
                }
                matcher.groups[groupIndex] = groupStart;
                matcher.groups[groupIndex+1] = groupEnd;
                return false;
            } else {
                // 这是组引用情况。我们不需要保存任何组信息，因为它实际上不是一个组。
                matcher.last = i;
                return true;
            }
        }
    }

    /**
     * 这设置了一个循环来处理递归量词结构。
     */
    static final class Prolog extends Node {
        Loop loop;
        Prolog(Loop loop) {
            this.loop = loop;
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            return loop.matchInit(matcher, i, seq);
        }
        boolean study(TreeInfo info) {
            return loop.study(info);
        }
    }

    /**
     * 处理贪婪 Curly 的重复计数。matchInit 由 Prolog 调用，以保存组开始位置的索引。零长度组检查在正常匹配中发生，但在 matchInit 中被跳过。
     */
    static class Loop extends Node {
        Node body;
        int countIndex; // matcher locals 中的本地计数索引
        int beginIndex; // 组开始索引
        int cmin, cmax;
        Loop(int countIndex, int beginIndex) {
            this.countIndex = countIndex;
            this.beginIndex = beginIndex;
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            // 避免在零长度情况下无限循环。
            if (i > matcher.locals[beginIndex]) {
                int count = matcher.locals[countIndex];

                // 这个块是在我们达到循环匹配所需的最小迭代次数之前。
                if (count < cmin) {
                    matcher.locals[countIndex] = count + 1;
                    boolean b = body.match(matcher, i, seq);
                    // 如果匹配失败，我们必须回溯，因此循环计数不应该递增。
                    if (!b)
                        matcher.locals[countIndex] = count;
                    // 返回成功或失败，因为我们处于最小值以下。
                    return b;
                }
                // 这个块是在我们达到循环匹配所需的最小迭代次数之后。
                if (count < cmax) {
                    matcher.locals[countIndex] = count + 1;
                    boolean b = body.match(matcher, i, seq);
                    // 如果匹配失败，我们必须回溯，因此循环计数不应该递增。
                    if (!b)
                        matcher.locals[countIndex] = count;
                    else
                        return true;
                }
            }
            return next.match(matcher, i, seq);
        }
        boolean matchInit(Matcher matcher, int i, CharSequence seq) {
            int save = matcher.locals[countIndex];
            boolean ret = false;
            if (0 < cmin) {
                matcher.locals[countIndex] = 1;
                ret = body.match(matcher, i, seq);
            } else if (0 < cmax) {
                matcher.locals[countIndex] = 1;
                ret = body.match(matcher, i, seq);
                if (ret == false)
                    ret = next.match(matcher, i, seq);
            } else {
                ret = next.match(matcher, i, seq);
            }
            matcher.locals[countIndex] = save;
            return ret;
        }
        boolean study(TreeInfo info) {
            info.maxValid = false;
            info.deterministic = false;
            return false;
        }
    }

    /**
     * 处理不情愿 Curly 的重复计数。matchInit 由 Prolog 调用，以保存组开始位置的索引。零长度组检查在正常匹配中发生，但在 matchInit 中被跳过。
     */
    static final class LazyLoop extends Loop {
        LazyLoop(int countIndex, int beginIndex) {
            super(countIndex, beginIndex);
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            // 检查零长度组
            if (i > matcher.locals[beginIndex]) {
                int count = matcher.locals[countIndex];
                if (count < cmin) {
                    matcher.locals[countIndex] = count + 1;
                    boolean result = body.match(matcher, i, seq);
                    // 如果匹配失败，我们必须回溯，因此循环计数不应该递增。
                    if (!result)
                        matcher.locals[countIndex] = count;
                    return result;
                }
                if (next.match(matcher, i, seq))
                    return true;
                if (count < cmax) {
                    matcher.locals[countIndex] = count + 1;
                    boolean result = body.match(matcher, i, seq);
                    // 如果匹配失败，我们必须回溯，因此循环计数不应该递增。
                    if (!result)
                        matcher.locals[countIndex] = count;
                    return result;
                }
                return false;
            }
            return next.match(matcher, i, seq);
        }
        boolean matchInit(Matcher matcher, int i, CharSequence seq) {
            int save = matcher.locals[countIndex];
            boolean ret = false;
            if (0 < cmin) {
                matcher.locals[countIndex] = 1;
                ret = body.match(matcher, i, seq);
            } else if (next.match(matcher, i, seq)) {
                ret = true;
            } else if (0 < cmax) {
                matcher.locals[countIndex] = 1;
                ret = body.match(matcher, i, seq);
            }
            matcher.locals[countIndex] = save;
            return ret;
        }
        boolean study(TreeInfo info) {
            info.maxValid = false;
            info.deterministic = false;
            return false;
        }
    }

    /**
     * 引用正则表达式中的一个组。尝试匹配组上次匹配的内容。
     */
    static class BackRef extends Node {
        int groupIndex;
        BackRef(int groupCount) {
            super();
            groupIndex = groupCount + groupCount;
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int j = matcher.groups[groupIndex];
            int k = matcher.groups[groupIndex+1];

            int groupSize = k - j;
            // 如果引用的组没有匹配，这个也不能匹配。
            if (j < 0)
                return false;

            // 如果输入不足，无法匹配。
            if (i + groupSize > matcher.to) {
                matcher.hitEnd = true;
                return false;
            }
            // 检查每个新字符，确保它与组上次匹配的内容相同。
            for (int index=0; index<groupSize; index++)
                if (seq.charAt(i+index) != seq.charAt(j+index))
                    return false;

            return next.match(matcher, i+groupSize, seq);
        }
        boolean study(TreeInfo info) {
            info.maxValid = false;
            return next.study(info);
        }
    }

    static class CIBackRef extends Node {
        int groupIndex;
        boolean doUnicodeCase;
        CIBackRef(int groupCount, boolean doUnicodeCase) {
            super();
            groupIndex = groupCount + groupCount;
            this.doUnicodeCase = doUnicodeCase;
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int j = matcher.groups[groupIndex];
            int k = matcher.groups[groupIndex+1];

            int groupSize = k - j;

            // 如果引用的组没有匹配，这个也不能匹配。
            if (j < 0)
                return false;

            // 如果输入不足，无法匹配。
            if (i + groupSize > matcher.to) {
                matcher.hitEnd = true;
                return false;
            }

            // 检查每个新字符，确保它与组上次匹配的内容相同。
            int x = i;
            for (int index=0; index<groupSize; index++) {
                int c1 = Character.codePointAt(seq, x);
                int c2 = Character.codePointAt(seq, j);
                if (c1 != c2) {
                    if (doUnicodeCase) {
                        int cc1 = Character.toUpperCase(c1);
                        int cc2 = Character.toUpperCase(c2);
                        if (cc1 != cc2 &&
                            Character.toLowerCase(cc1) !=
                            Character.toLowerCase(cc2))
                            return false;
                    } else {
                        if (ASCII.toLower(c1) != ASCII.toLower(c2))
                            return false;
                    }
                }
                x += Character.charCount(c1);
                j += Character.charCount(c2);
            }

            return next.match(matcher, i+groupSize, seq);
        }
        boolean study(TreeInfo info) {
            info.maxValid = false;
            return next.study(info);
        }
    }

    /**
     * 搜索下一个原子实例。这对于高效地找到原子而不需要传递其实例（贪婪问题）和避免大量浪费的搜索时间（不情愿问题）非常有用。
     */
    static final class First extends Node {
        Node atom;
        First(Node node) {
            this.atom = BnM.optimize(node);
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            if (atom instanceof BnM) {
                return atom.match(matcher, i, seq)
                    && next.match(matcher, matcher.last, seq);
            }
            for (;;) {
                if (i > matcher.to) {
                    matcher.hitEnd = true;
                    return false;
                }
                if (atom.match(matcher, i, seq)) {
                    return next.match(matcher, matcher.last, seq);
                }
                i += countChars(seq, i, 1);
                matcher.first++;
            }
        }
        boolean study(TreeInfo info) {
            atom.study(info);
            info.maxValid = false;
            info.deterministic = false;
            return next.study(info);
        }
    }

    static final class Conditional extends Node {
        Node cond, yes, not;
        Conditional(Node cond, Node yes, Node not) {
            this.cond = cond;
            this.yes = yes;
            this.not = not;
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            if (cond.match(matcher, i, seq)) {
                return yes.match(matcher, i, seq);
            } else {
                return not.match(matcher, i, seq);
            }
        }
        boolean study(TreeInfo info) {
            int minL = info.minLength;
            int maxL = info.maxLength;
            boolean maxV = info.maxValid;
            info.reset();
            yes.study(info);

            int minL2 = info.minLength;
            int maxL2 = info.maxLength;
            boolean maxV2 = info.maxValid;
            info.reset();
            not.study(info);

            info.minLength = minL + Math.min(minL2, info.minLength);
            info.maxLength = maxL + Math.max(maxL2, info.maxLength);
            info.maxValid = (maxV & maxV2 & info.maxValid);
            info.deterministic = false;
            return next.study(info);
        }
    }

    /**
     * 零宽度正向肯定。
     */
    static final class Pos extends Node {
        Node cond;
        Pos(Node cond) {
            this.cond = cond;
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int savedTo = matcher.to;
            boolean conditionMatched = false;

            // 放宽透明区域边界以进行正向肯定。
            if (matcher.transparentBounds)
                matcher.to = matcher.getTextLength();
            try {
                conditionMatched = cond.match(matcher, i, seq);
            } finally {
                // 恢复区域边界。
                matcher.to = savedTo;
            }
            return conditionMatched && next.match(matcher, i, seq);
        }
    }

    /**
     * 零宽度正向否定。
     */
    static final class Neg extends Node {
        Node cond;
        Neg(Node cond) {
            this.cond = cond;
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int savedTo = matcher.to;
            boolean conditionMatched = false;

            // 放宽透明区域边界以进行正向否定。
            if (matcher.transparentBounds)
                matcher.to = matcher.getTextLength();
            try {
                if (i < matcher.to) {
                    conditionMatched = !cond.match(matcher, i, seq);
                } else {
                    // 如果正向否定成功，则更多的输入可能会导致它失败！
                    matcher.requireEnd = true;
                    conditionMatched = !cond.match(matcher, i, seq);
                }
            } finally {
                // 恢复区域边界。
                matcher.to = savedTo;
            }
            return conditionMatched && next.match(matcher, i, seq);
        }
    }


                /**
     * 用于与后向查找配合；匹配后向查找遇到的位置。
     */
    static Node lookbehindEnd = new Node() {
        boolean match(Matcher matcher, int i, CharSequence seq) {
            return i == matcher.lookbehindTo;
        }
    };

    /**
     * 零宽度正向后向查找。
     */
    static class Behind extends Node {
        Node cond;
        int rmax, rmin;
        Behind(Node cond, int rmax, int rmin) {
            this.cond = cond;
            this.rmax = rmax;
            this.rmin = rmin;
        }

        boolean match(Matcher matcher, int i, CharSequence seq) {
            int savedFrom = matcher.from;
            boolean conditionMatched = false;
            int startIndex = (!matcher.transparentBounds) ?
                             matcher.from : 0;
            int from = Math.max(i - rmax, startIndex);
            // 设置结束边界
            int savedLBT = matcher.lookbehindTo;
            matcher.lookbehindTo = i;
            // 放松透明区域边界以进行后向查找
            if (matcher.transparentBounds)
                matcher.from = 0;
            for (int j = i - rmin; !conditionMatched && j >= from; j--) {
                conditionMatched = cond.match(matcher, j, seq);
            }
            matcher.from = savedFrom;
            matcher.lookbehindTo = savedLBT;
            return conditionMatched && next.match(matcher, i, seq);
        }
    }

    /**
     * 零宽度正向后向查找，包括补充字符或未配对的代理字符。
     */
    static final class BehindS extends Behind {
        BehindS(Node cond, int rmax, int rmin) {
            super(cond, rmax, rmin);
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int rmaxChars = countChars(seq, i, -rmax);
            int rminChars = countChars(seq, i, -rmin);
            int savedFrom = matcher.from;
            int startIndex = (!matcher.transparentBounds) ?
                             matcher.from : 0;
            boolean conditionMatched = false;
            int from = Math.max(i - rmaxChars, startIndex);
            // 设置结束边界
            int savedLBT = matcher.lookbehindTo;
            matcher.lookbehindTo = i;
            // 放松透明区域边界以进行后向查找
            if (matcher.transparentBounds)
                matcher.from = 0;

            for (int j = i - rminChars;
                 !conditionMatched && j >= from;
                 j -= j>from ? countChars(seq, j, -1) : 1) {
                conditionMatched = cond.match(matcher, j, seq);
            }
            matcher.from = savedFrom;
            matcher.lookbehindTo = savedLBT;
            return conditionMatched && next.match(matcher, i, seq);
        }
    }

    /**
     * 零宽度负向后向查找。
     */
    static class NotBehind extends Node {
        Node cond;
        int rmax, rmin;
        NotBehind(Node cond, int rmax, int rmin) {
            this.cond = cond;
            this.rmax = rmax;
            this.rmin = rmin;
        }

        boolean match(Matcher matcher, int i, CharSequence seq) {
            int savedLBT = matcher.lookbehindTo;
            int savedFrom = matcher.from;
            boolean conditionMatched = false;
            int startIndex = (!matcher.transparentBounds) ?
                             matcher.from : 0;
            int from = Math.max(i - rmax, startIndex);
            matcher.lookbehindTo = i;
            // 放松透明区域边界以进行后向查找
            if (matcher.transparentBounds)
                matcher.from = 0;
            for (int j = i - rmin; !conditionMatched && j >= from; j--) {
                conditionMatched = cond.match(matcher, j, seq);
            }
            // 恢复区域边界
            matcher.from = savedFrom;
            matcher.lookbehindTo = savedLBT;
            return !conditionMatched && next.match(matcher, i, seq);
        }
    }

    /**
     * 零宽度负向后向查找，包括补充字符或未配对的代理字符。
     */
    static final class NotBehindS extends NotBehind {
        NotBehindS(Node cond, int rmax, int rmin) {
            super(cond, rmax, rmin);
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int rmaxChars = countChars(seq, i, -rmax);
            int rminChars = countChars(seq, i, -rmin);
            int savedFrom = matcher.from;
            int savedLBT = matcher.lookbehindTo;
            boolean conditionMatched = false;
            int startIndex = (!matcher.transparentBounds) ?
                             matcher.from : 0;
            int from = Math.max(i - rmaxChars, startIndex);
            matcher.lookbehindTo = i;
            // 放松透明区域边界以进行后向查找
            if (matcher.transparentBounds)
                matcher.from = 0;
            for (int j = i - rminChars;
                 !conditionMatched && j >= from;
                 j -= j>from ? countChars(seq, j, -1) : 1) {
                conditionMatched = cond.match(matcher, j, seq);
            }
            // 恢复区域边界
            matcher.from = savedFrom;
            matcher.lookbehindTo = savedLBT;
            return !conditionMatched && next.match(matcher, i, seq);
        }
    }

    /**
     * 返回两个 CharProperty 节点的并集。
     */
    private static CharProperty union(final CharProperty lhs,
                                      final CharProperty rhs) {
        return new CharProperty() {
                boolean isSatisfiedBy(int ch) {
                    return lhs.isSatisfiedBy(ch) || rhs.isSatisfiedBy(ch);}};
    }

    /**
     * 返回两个 CharProperty 节点的交集。
     */
    private static CharProperty intersection(final CharProperty lhs,
                                             final CharProperty rhs) {
        return new CharProperty() {
                boolean isSatisfiedBy(int ch) {
                    return lhs.isSatisfiedBy(ch) && rhs.isSatisfiedBy(ch);}};
    }

    /**
     * 返回两个 CharProperty 节点的差集。
     */
    private static CharProperty setDifference(final CharProperty lhs,
                                              final CharProperty rhs) {
        return new CharProperty() {
                boolean isSatisfiedBy(int ch) {
                    return ! rhs.isSatisfiedBy(ch) && lhs.isSatisfiedBy(ch);}};
    }

    /**
     * 处理单词边界。包括一个字段，允许这个类处理不同类型的单词边界。单词字符包括下划线、字母和数字。非间距标记如果有一个基础字符，也属于单词的一部分，否则在查找单词边界时会被忽略。
     */
    static final class Bound extends Node {
        static int LEFT = 0x1;
        static int RIGHT= 0x2;
        static int BOTH = 0x3;
        static int NONE = 0x4;
        int type;
        boolean useUWORD;
        Bound(int n, boolean useUWORD) {
            type = n;
            this.useUWORD = useUWORD;
        }

        boolean isWord(int ch) {
            return useUWORD ? UnicodeProp.WORD.is(ch)
                            : (ch == '_' || Character.isLetterOrDigit(ch));
        }

        int check(Matcher matcher, int i, CharSequence seq) {
            int ch;
            boolean left = false;
            int startIndex = matcher.from;
            int endIndex = matcher.to;
            if (matcher.transparentBounds) {
                startIndex = 0;
                endIndex = matcher.getTextLength();
            }
            if (i > startIndex) {
                ch = Character.codePointBefore(seq, i);
                left = (isWord(ch) ||
                    ((Character.getType(ch) == Character.NON_SPACING_MARK)
                     && hasBaseCharacter(matcher, i-1, seq)));
            }
            boolean right = false;
            if (i < endIndex) {
                ch = Character.codePointAt(seq, i);
                right = (isWord(ch) ||
                    ((Character.getType(ch) == Character.NON_SPACING_MARK)
                     && hasBaseCharacter(matcher, i, seq)));
            } else {
                // 尝试访问超出结尾的字符
                matcher.hitEnd = true;
                // 添加另一个字符可能会破坏边界
                matcher.requireEnd = true;
            }
            return ((left ^ right) ? (right ? LEFT : RIGHT) : NONE);
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            return (check(matcher, i, seq) & type) > 0
                && next.match(matcher, i, seq);
        }
    }

    /**
     * 非间距标记只有在有基础字符时才在边界计算中被视为单词字符。
     */
    private static boolean hasBaseCharacter(Matcher matcher, int i,
                                            CharSequence seq)
    {
        int start = (!matcher.transparentBounds) ?
            matcher.from : 0;
        for (int x=i; x >= start; x--) {
            int ch = Character.codePointAt(seq, x);
            if (Character.isLetterOrDigit(ch))
                return true;
            if (Character.getType(ch) == Character.NON_SPACING_MARK)
                continue;
            return false;
        }
        return false;
    }

    /**
     * 尝试使用 Boyer-Moore 字符串匹配算法在输入中匹配一个切片。该算法基于从右到左匹配模式的思想，这样可以将模式向右移动更远的距离。
     * <p>
     * 模式从右到左逐字符与输入进行比较。如果所有字符都匹配，则找到模式。如果某个字符不匹配，则将模式向右移动的距离是两个函数的最大值：坏字符移位和好后缀移位。这种移位比逐位置检查输入的方法更快。
     * <p>
     * 坏字符移位基于未匹配的文本字符。如果该字符不在模式中，模式可以完全移出该坏字符。如果该字符出现在模式中，模式可以移动到该字符的下一个出现位置。
     * <p>
     * 好后缀移位基于模式右侧的某个子集已经匹配。当找到坏字符时，如果该子集在模式中不再出现，模式可以向右移动模式长度；否则，模式可以移动到该子集在模式中的下一个出现位置。
     *
     * Boyer-Moore 搜索方法改编自 Amy Yu 的代码。
     */
    static class BnM extends Node {
        int[] buffer;
        int[] lastOcc;
        int[] optoSft;

        /**
         * 预计算生成坏字符移位和好后缀移位所需的数组。只使用最后七个位来检查字符是否匹配；这使表保持较小，并覆盖了常用的 ASCII 范围，但偶尔会导致坏字符移位的别名匹配。
         */
        static Node optimize(Node node) {
            if (!(node instanceof Slice)) {
                return node;
            }

            int[] src = ((Slice) node).buffer;
            int patternLength = src.length;
            // BM 算法需要一些开销；
            // 如果模式较短，则不使用它，因为
            // 大于模式长度的移位不能使用。
            if (patternLength < 4) {
                return node;
            }
            int i, j, k;
            int[] lastOcc = new int[128];
            int[] optoSft = new int[patternLength];
            // 预计算部分坏字符移位
            // 它是一个表，表示每个
            // 低 7 位值在模式中的位置
            for (i = 0; i < patternLength; i++) {
                lastOcc[src[i]&0x7F] = i + 1;
            }
            // 预计算好后缀移位
            // i 是正在考虑的移位量
NEXT:       for (i = patternLength; i > 0; i--) {
                // j 是正在考虑的后缀的开始索引
                for (j = patternLength - 1; j >= i; j--) {
                    // 测试好后缀
                    if (src[j] == src[j-i]) {
                        // src[j..len] 是一个好后缀
                        optoSft[j-1] = i;
                    } else {
                        // 不匹配。数组已经
                        // 用正确的值填充。
                        continue NEXT;
                    }
                }
                // 这填充了 optoSft 的剩余部分
                // 任何后缀的移位量不能大于
                // 其子后缀的移位量。为什么？
                while (j > 0) {
                    optoSft[--j] = i;
                }
            }
            // 设置保护值，因为有 Unicode 压缩
            optoSft[patternLength-1] = 1;
            if (node instanceof SliceS)
                return new BnMS(src, lastOcc, optoSft, node.next);
            return new BnM(src, lastOcc, optoSft, node.next);
        }
        BnM(int[] src, int[] lastOcc, int[] optoSft, Node next) {
            this.buffer = src;
            this.lastOcc = lastOcc;
            this.optoSft = optoSft;
            this.next = next;
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int[] src = buffer;
            int patternLength = src.length;
            int last = matcher.to - patternLength;

            // 循环遍历文本中所有可能的匹配位置
NEXT:       while (i <= last) {
                // 循环遍历从右到左的模式
                for (int j = patternLength - 1; j >= 0; j--) {
                    int ch = seq.charAt(i+j);
                    if (ch != src[j]) {
                        // 通过坏字符移位和好后缀移位的最大值
                        // 将搜索向右移动
                        i += Math.max(j + 1 - lastOcc[ch&0x7F], optoSft[j]);
                        continue NEXT;
                    }
                }
                // 模式从 i 开始完全匹配
                matcher.first = i;
                boolean ret = next.match(matcher, i + patternLength, seq);
                if (ret) {
                    matcher.first = i;
                    matcher.groups[0] = matcher.first;
                    matcher.groups[1] = matcher.last;
                    return true;
                }
                i++;
            }
            // BnM 仅在未锚定的情况下作为前导节点使用，
            // 并替换了始终搜索到结尾的 Start()，
            // 如果没有找到要查找的内容，则 hitEnd 为 true。
            matcher.hitEnd = true;
            return false;
        }
        boolean study(TreeInfo info) {
            info.minLength += buffer.length;
            info.maxValid = false;
            return next.study(info);
        }
    }


                /**
     * BnM() 的补充支持版本。未配对的代理也由此类处理。
     */
    static final class BnMS extends BnM {
        int lengthInChars;

        BnMS(int[] src, int[] lastOcc, int[] optoSft, Node next) {
            super(src, lastOcc, optoSft, next);
            for (int x = 0; x < buffer.length; x++) {
                lengthInChars += Character.charCount(buffer[x]);
            }
        }
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int[] src = buffer;
            int patternLength = src.length;
            int last = matcher.to - lengthInChars;

            // 遍历文本中所有可能的匹配位置
NEXT:       while (i <= last) {
                // 从右到左遍历模式
                int ch;
                for (int j = countChars(seq, i, patternLength), x = patternLength - 1;
                     j > 0; j -= Character.charCount(ch), x--) {
                    ch = Character.codePointBefore(seq, i+j);
                    if (ch != src[x]) {
                        // 将搜索向右移动最大值，即坏字符移动和好后缀移动中的较大值
                        int n = Math.max(x + 1 - lastOcc[ch&0x7F], optoSft[x]);
                        i += countChars(seq, i, n);
                        continue NEXT;
                    }
                }
                // 模式从 i 开始完全匹配
                matcher.first = i;
                boolean ret = next.match(matcher, i + lengthInChars, seq);
                if (ret) {
                    matcher.first = i;
                    matcher.groups[0] = matcher.first;
                    matcher.groups[1] = matcher.last;
                    return true;
                }
                i += countChars(seq, i, 1);
            }
            matcher.hitEnd = true;
            return false;
        }
    }

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

    /**
     * 这必须是第一个初始化器。
     */
    static Node accept = new Node();

    static Node lastAccept = new LastNode();

    private static class CharPropertyNames {

        static CharProperty charPropertyFor(String name) {
            CharPropertyFactory m = map.get(name);
            return m == null ? null : m.make();
        }

        private static abstract class CharPropertyFactory {
            abstract CharProperty make();
        }

        private static void defCategory(String name,
                                        final int typeMask) {
            map.put(name, new CharPropertyFactory() {
                    CharProperty make() { return new Category(typeMask);}});
        }

        private static void defRange(String name,
                                     final int lower, final int upper) {
            map.put(name, new CharPropertyFactory() {
                    CharProperty make() { return rangeFor(lower, upper);}});
        }

        private static void defCtype(String name,
                                     final int ctype) {
            map.put(name, new CharPropertyFactory() {
                    CharProperty make() { return new Ctype(ctype);}});
        }

        private static abstract class CloneableProperty
            extends CharProperty implements Cloneable
        {
            public CloneableProperty clone() {
                try {
                    return (CloneableProperty) super.clone();
                } catch (CloneNotSupportedException e) {
                    throw new AssertionError(e);
                }
            }
        }

        private static void defClone(String name,
                                     final CloneableProperty p) {
            map.put(name, new CharPropertyFactory() {
                    CharProperty make() { return p.clone();}});
        }

        private static final HashMap<String, CharPropertyFactory> map
            = new HashMap<>();

        static {
            // Unicode 字符属性别名，定义在
            // http://www.unicode.org/Public/UNIDATA/PropertyValueAliases.txt
            defCategory("Cn", 1<<Character.UNASSIGNED);
            defCategory("Lu", 1<<Character.UPPERCASE_LETTER);
            defCategory("Ll", 1<<Character.LOWERCASE_LETTER);
            defCategory("Lt", 1<<Character.TITLECASE_LETTER);
            defCategory("Lm", 1<<Character.MODIFIER_LETTER);
            defCategory("Lo", 1<<Character.OTHER_LETTER);
            defCategory("Mn", 1<<Character.NON_SPACING_MARK);
            defCategory("Me", 1<<Character.ENCLOSING_MARK);
            defCategory("Mc", 1<<Character.COMBINING_SPACING_MARK);
            defCategory("Nd", 1<<Character.DECIMAL_DIGIT_NUMBER);
            defCategory("Nl", 1<<Character.LETTER_NUMBER);
            defCategory("No", 1<<Character.OTHER_NUMBER);
            defCategory("Zs", 1<<Character.SPACE_SEPARATOR);
            defCategory("Zl", 1<<Character.LINE_SEPARATOR);
            defCategory("Zp", 1<<Character.PARAGRAPH_SEPARATOR);
            defCategory("Cc", 1<<Character.CONTROL);
            defCategory("Cf", 1<<Character.FORMAT);
            defCategory("Co", 1<<Character.PRIVATE_USE);
            defCategory("Cs", 1<<Character.SURROGATE);
            defCategory("Pd", 1<<Character.DASH_PUNCTUATION);
            defCategory("Ps", 1<<Character.START_PUNCTUATION);
            defCategory("Pe", 1<<Character.END_PUNCTUATION);
            defCategory("Pc", 1<<Character.CONNECTOR_PUNCTUATION);
            defCategory("Po", 1<<Character.OTHER_PUNCTUATION);
            defCategory("Sm", 1<<Character.MATH_SYMBOL);
            defCategory("Sc", 1<<Character.CURRENCY_SYMBOL);
            defCategory("Sk", 1<<Character.MODIFIER_SYMBOL);
            defCategory("So", 1<<Character.OTHER_SYMBOL);
            defCategory("Pi", 1<<Character.INITIAL_QUOTE_PUNCTUATION);
            defCategory("Pf", 1<<Character.FINAL_QUOTE_PUNCTUATION);
            defCategory("L", ((1<<Character.UPPERCASE_LETTER) |
                              (1<<Character.LOWERCASE_LETTER) |
                              (1<<Character.TITLECASE_LETTER) |
                              (1<<Character.MODIFIER_LETTER)  |
                              (1<<Character.OTHER_LETTER)));
            defCategory("M", ((1<<Character.NON_SPACING_MARK) |
                              (1<<Character.ENCLOSING_MARK)   |
                              (1<<Character.COMBINING_SPACING_MARK)));
            defCategory("N", ((1<<Character.DECIMAL_DIGIT_NUMBER) |
                              (1<<Character.LETTER_NUMBER)        |
                              (1<<Character.OTHER_NUMBER)));
            defCategory("Z", ((1<<Character.SPACE_SEPARATOR) |
                              (1<<Character.LINE_SEPARATOR)  |
                              (1<<Character.PARAGRAPH_SEPARATOR)));
            defCategory("C", ((1<<Character.CONTROL)     |
                              (1<<Character.FORMAT)      |
                              (1<<Character.PRIVATE_USE) |
                              (1<<Character.SURROGATE))); // 其他
            defCategory("P", ((1<<Character.DASH_PUNCTUATION)      |
                              (1<<Character.START_PUNCTUATION)     |
                              (1<<Character.END_PUNCTUATION)       |
                              (1<<Character.CONNECTOR_PUNCTUATION) |
                              (1<<Character.OTHER_PUNCTUATION)     |
                              (1<<Character.INITIAL_QUOTE_PUNCTUATION) |
                              (1<<Character.FINAL_QUOTE_PUNCTUATION)));
            defCategory("S", ((1<<Character.MATH_SYMBOL)     |
                              (1<<Character.CURRENCY_SYMBOL) |
                              (1<<Character.MODIFIER_SYMBOL) |
                              (1<<Character.OTHER_SYMBOL)));
            defCategory("LC", ((1<<Character.UPPERCASE_LETTER) |
                               (1<<Character.LOWERCASE_LETTER) |
                               (1<<Character.TITLECASE_LETTER)));
            defCategory("LD", ((1<<Character.UPPERCASE_LETTER) |
                               (1<<Character.LOWERCASE_LETTER) |
                               (1<<Character.TITLECASE_LETTER) |
                               (1<<Character.MODIFIER_LETTER)  |
                               (1<<Character.OTHER_LETTER)     |
                               (1<<Character.DECIMAL_DIGIT_NUMBER)));
            defRange("L1", 0x00, 0xFF); // Latin-1
            map.put("all", new CharPropertyFactory() {
                    CharProperty make() { return new All(); }});

            // Posix 正则表达式字符类，定义在
            // http://www.unix.org/onlinepubs/009695399/basedefs/xbd_chap09.html
            defRange("ASCII", 0x00, 0x7F);   // ASCII
            defCtype("Alnum", ASCII.ALNUM);  // 字母数字字符
            defCtype("Alpha", ASCII.ALPHA);  // 字母字符
            defCtype("Blank", ASCII.BLANK);  // 空格和制表符
            defCtype("Cntrl", ASCII.CNTRL);  // 控制字符
            defRange("Digit", '0', '9');     // 数字字符
            defCtype("Graph", ASCII.GRAPH);  // 可打印且可见的字符
            defRange("Lower", 'a', 'z');     // 小写字母
            defRange("Print", 0x20, 0x7E);   // 可打印字符
            defCtype("Punct", ASCII.PUNCT);  // 标点符号
            defCtype("Space", ASCII.SPACE);  // 空格字符
            defRange("Upper", 'A', 'Z');     // 大写字母
            defCtype("XDigit",ASCII.XDIGIT); // 十六进制数字

            // Java 字符属性，由 Character.java 中的方法定义
            defClone("javaLowerCase", new CloneableProperty() {
                boolean isSatisfiedBy(int ch) {
                    return Character.isLowerCase(ch);}});
            defClone("javaUpperCase", new CloneableProperty() {
                boolean isSatisfiedBy(int ch) {
                    return Character.isUpperCase(ch);}});
            defClone("javaAlphabetic", new CloneableProperty() {
                boolean isSatisfiedBy(int ch) {
                    return Character.isAlphabetic(ch);}});
            defClone("javaIdeographic", new CloneableProperty() {
                boolean isSatisfiedBy(int ch) {
                    return Character.isIdeographic(ch);}});
            defClone("javaTitleCase", new CloneableProperty() {
                boolean isSatisfiedBy(int ch) {
                    return Character.isTitleCase(ch);}});
            defClone("javaDigit", new CloneableProperty() {
                boolean isSatisfiedBy(int ch) {
                    return Character.isDigit(ch);}});
            defClone("javaDefined", new CloneableProperty() {
                boolean isSatisfiedBy(int ch) {
                    return Character.isDefined(ch);}});
            defClone("javaLetter", new CloneableProperty() {
                boolean isSatisfiedBy(int ch) {
                    return Character.isLetter(ch);}});
            defClone("javaLetterOrDigit", new CloneableProperty() {
                boolean isSatisfiedBy(int ch) {
                    return Character.isLetterOrDigit(ch);}});
            defClone("javaJavaIdentifierStart", new CloneableProperty() {
                boolean isSatisfiedBy(int ch) {
                    return Character.isJavaIdentifierStart(ch);}});
            defClone("javaJavaIdentifierPart", new CloneableProperty() {
                boolean isSatisfiedBy(int ch) {
                    return Character.isJavaIdentifierPart(ch);}});
            defClone("javaUnicodeIdentifierStart", new CloneableProperty() {
                boolean isSatisfiedBy(int ch) {
                    return Character.isUnicodeIdentifierStart(ch);}});
            defClone("javaUnicodeIdentifierPart", new CloneableProperty() {
                boolean isSatisfiedBy(int ch) {
                    return Character.isUnicodeIdentifierPart(ch);}});
            defClone("javaIdentifierIgnorable", new CloneableProperty() {
                boolean isSatisfiedBy(int ch) {
                    return Character.isIdentifierIgnorable(ch);}});
            defClone("javaSpaceChar", new CloneableProperty() {
                boolean isSatisfiedBy(int ch) {
                    return Character.isSpaceChar(ch);}});
            defClone("javaWhitespace", new CloneableProperty() {
                boolean isSatisfiedBy(int ch) {
                    return Character.isWhitespace(ch);}});
            defClone("javaISOControl", new CloneableProperty() {
                boolean isSatisfiedBy(int ch) {
                    return Character.isISOControl(ch);}});
            defClone("javaMirrored", new CloneableProperty() {
                boolean isSatisfiedBy(int ch) {
                    return Character.isMirrored(ch);}});
        }
    }

    /**
     * 创建一个可以用于匹配字符串的谓词。
     *
     * @return  可用于字符串匹配的谓词
     * @since   1.8
     */
    public Predicate<String> asPredicate() {
        return s -> matcher(s).find();
    }

    /**
     * 从给定的输入序列中创建一个流，该流围绕此模式的匹配项。
     *
     * <p> 该方法返回的流包含输入序列中的每个子字符串，这些子字符串由另一个与此模式匹配的子序列终止，或由输入序列的末尾终止。流中的子字符串按其在输入中出现的顺序排列。尾随的空字符串将被丢弃，不会出现在流中。
     *
     * <p> 如果此模式不匹配输入的任何子序列，则结果流只有一个元素，即输入序列的字符串形式。
     *
     * <p> 当在输入序列的开头有一个正宽度匹配时，则在流的开头包含一个空的前导子字符串。然而，开头的零宽度匹配永远不会产生这样的空前导子字符串。
     *
     * <p> 如果输入序列是可变的，则在执行终端流操作期间必须保持不变。否则，终端流操作的结果是未定义的。
     *
     * @param   input
     *          要拆分的字符序列
     *
     * @return  通过围绕此模式的匹配项拆分输入而计算出的字符串流
     * @see     #split(CharSequence)
     * @since   1.8
     */
    public Stream<String> splitAsStream(final CharSequence input) {
        class MatcherIterator implements Iterator<String> {
            private final Matcher matcher;
            // 下一个输入子序列的起始位置
            // 当 current == input.length 时，没有更多元素
            private int current;
            // 如果下一个元素（如果有）需要获取，则为 null
            private String nextElement;
            // > 0 表示有 N 个下一个空元素
            private int emptyElementCount;

            MatcherIterator() {
                this.matcher = matcher(input);
            }


                        public String next() {
                if (!hasNext())
                    throw new NoSuchElementException();

                if (emptyElementCount == 0) {
                    String n = nextElement;
                    nextElement = null;
                    return n;
                } else {
                    emptyElementCount--;
                    return "";
                }
            }

            /**
             * 检查是否还有下一个元素。
             * 
             * @return 如果还有下一个元素，则返回 true；否则返回 false。
             */
            public boolean hasNext() {
                if (nextElement != null || emptyElementCount > 0)
                    return true;

                if (current == input.length())
                    return false;

                // 消耗下一个匹配的元素
                // 计算匹配的空元素序列
                while (matcher.find()) {
                    nextElement = input.subSequence(current, matcher.start()).toString();
                    current = matcher.end();
                    if (!nextElement.isEmpty()) {
                        return true;
                    } else if (current > 0) { // 在输入开始处忽略零宽度匹配的空前导子字符串
                        emptyElementCount++;
                    }
                }

                // 消耗最后一个匹配的元素
                nextElement = input.subSequence(current, input.length()).toString();
                current = input.length();
                if (!nextElement.isEmpty()) {
                    return true;
                } else {
                    // 忽略末尾的匹配空元素序列
                    emptyElementCount = 0;
                    nextElement = null;
                    return false;
                }
            }
        }
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                new MatcherIterator(), Spliterator.ORDERED | Spliterator.NONNULL), false);
    }
}
