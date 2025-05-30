
/*
 * Copyright (c) 1994, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

import java.io.ObjectStreamField;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Formatter;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * {@code String} 类表示字符字符串。Java 程序中的所有字符串字面量，例如 {@code "abc"}，都是这个类的实例。
 * <p>
 * 字符串是常量；它们的值在创建后不能改变。字符串缓冲区支持可变字符串。
 * 由于字符串对象是不可变的，因此可以共享。例如：
 * <blockquote><pre>
 *     String str = "abc";
 * </pre></blockquote><p>
 * 等效于：
 * <blockquote><pre>
 *     char data[] = {'a', 'b', 'c'};
 *     String str = new String(data);
 * </pre></blockquote><p>
 * 以下是一些关于如何使用字符串的更多示例：
 * <blockquote><pre>
 *     System.out.println("abc");
 *     String cde = "cde";
 *     System.out.println("abc" + cde);
 *     String c = "abc".substring(2,3);
 *     String d = cde.substring(1, 2);
 * </pre></blockquote>
 * <p>
 * {@code String} 类包括用于检查字符序列中的单个字符、比较字符串、搜索字符串、提取子字符串以及创建所有字符转换为大写或小写的字符串副本的方法。
 * 大小写映射基于 {@link java.lang.Character Character} 类指定的 Unicode 标准版本。
 * <p>
 * Java 语言为字符串连接运算符 (&nbsp;+&nbsp;) 和将其他对象转换为字符串提供了特殊支持。
 * 字符串连接通过 {@code StringBuilder}(或 {@code StringBuffer}) 类及其 {@code append} 方法实现。
 * 字符串转换通过 {@code toString} 方法实现，该方法由 {@code Object} 定义并被 Java 中的所有类继承。
 * 有关字符串连接和转换的更多详细信息，请参阅 Gosling, Joy, 和 Steele 的《Java 语言规范》。
 *
 * <p>除非另有说明，将 {@code null} 参数传递给此类的构造函数或方法将导致抛出 {@link NullPointerException}。
 *
 * <p>{@code String} 表示 UTF-16 格式的字符串，其中 <em>补充字符</em> 由 <em>代理对</em> 表示
 * （有关更多信息，请参阅 {@code Character} 类中的 <a href="Character.html#unicode">Unicode 字符表示</a> 部分）。
 * 索引值引用 {@code char} 代码单元，因此补充字符在 {@code String} 中占用两个位置。
 * <p>{@code String} 类提供了处理 Unicode 代码点（即字符）的方法，以及处理 Unicode 代码单元（即 {@code char} 值）的方法。
 *
 * @author  Lee Boynton
 * @author  Arthur van Hoff
 * @author  Martin Buchholz
 * @author  Ulf Zibis
 * @see     java.lang.Object#toString()
 * @see     java.lang.StringBuffer
 * @see     java.lang.StringBuilder
 * @see     java.nio.charset.Charset
 * @since   JDK1.0
 */

public final class String
    implements java.io.Serializable, Comparable<String>, CharSequence {
    /** 用于字符存储的值。 */
    private final char value[];

    /** 缓存字符串的哈希码 */
    private int hash; // 默认为 0

    /** 为了互操作性使用 JDK 1.0.2 的 serialVersionUID */
    private static final long serialVersionUID = -6849794470754667710L;

    /**
     * 初始化一个新创建的 {@code String} 对象，使其表示一个空字符序列。注意，由于字符串是不可变的，因此使用此构造函数是不必要的。
     */
    public String() {
        this.value = "".value;
    }

    /**
     * 初始化一个新创建的 {@code String} 对象，使其表示与参数相同的字符序列；换句话说，新创建的字符串是参数字符串的副本。
     * 除非需要 {@code original} 的显式副本，否则使用此构造函数是不必要的，因为字符串是不可变的。
     *
     * @param  original
     *         一个 {@code String}
     */
    public String(String original) {
        this.value = original.value;
        this.hash = original.hash;
    }

    /**
     * 分配一个新的 {@code String}，使其表示字符数组参数中当前包含的字符序列。字符数组的内容被复制；后续修改字符数组不会影响新创建的字符串。
     *
     * @param  value
     *         字符串的初始值
     */
    public String(char value[]) {
        this.value = Arrays.copyOf(value, value.length);
    }

    /**
     * 分配一个新的 {@code String}，使其包含字符数组参数的子数组中的字符。{@code offset} 参数是子数组的第一个字符的索引，
     * {@code count} 参数指定子数组的长度。子数组的内容被复制；后续修改字符数组不会影响新创建的字符串。
     *
     * @param  value
     *         字符数组，是字符的来源
     *
     * @param  offset
     *         初始偏移量
     *
     * @param  count
     *         长度
     *
     * @throws  IndexOutOfBoundsException
     *          如果 {@code offset} 和 {@code count} 参数索引的字符超出 {@code value} 数组的边界
     */
    public String(char value[], int offset, int count) {
        if (offset < 0) {
            throw new StringIndexOutOfBoundsException(offset);
        }
        if (count <= 0) {
            if (count < 0) {
                throw new StringIndexOutOfBoundsException(count);
            }
            if (offset <= value.length) {
                this.value = "".value;
                return;
            }
        }
        // 注意：offset 或 count 可能接近 -1>>>1。
        if (offset > value.length - count) {
            throw new StringIndexOutOfBoundsException(offset + count);
        }
        this.value = Arrays.copyOfRange(value, offset, offset+count);
    }

    /**
     * 分配一个新的 {@code String}，使其包含 <a href="Character.html#unicode">Unicode 代码点</a> 数组参数的子数组中的字符。
     * {@code offset} 参数是子数组的第一个代码点的索引，{@code count} 参数指定子数组的长度。子数组的内容被转换为 {@code char}；
     * 后续修改 {@code int} 数组不会影响新创建的字符串。
     *
     * @param  codePoints
     *         数组，是 Unicode 代码点的来源
     *
     * @param  offset
     *         初始偏移量
     *
     * @param  count
     *         长度
     *
     * @throws  IllegalArgumentException
     *          如果在 {@code codePoints} 中找到任何无效的 Unicode 代码点
     *
     * @throws  IndexOutOfBoundsException
     *          如果 {@code offset} 和 {@code count} 参数索引的字符超出 {@code codePoints} 数组的边界
     *
     * @since  1.5
     */
    public String(int[] codePoints, int offset, int count) {
        if (offset < 0) {
            throw new StringIndexOutOfBoundsException(offset);
        }
        if (count <= 0) {
            if (count < 0) {
                throw new StringIndexOutOfBoundsException(count);
            }
            if (offset <= codePoints.length) {
                this.value = "".value;
                return;
            }
        }
        // 注意：offset 或 count 可能接近 -1>>>1。
        if (offset > codePoints.length - count) {
            throw new StringIndexOutOfBoundsException(offset + count);
        }

        final int end = offset + count;

        // 第一次遍历：计算 char[] 的精确大小
        int n = count;
        for (int i = offset; i < end; i++) {
            int c = codePoints[i];
            if (Character.isBmpCodePoint(c))
                continue;
            else if (Character.isValidCodePoint(c))
                n++;
            else throw new IllegalArgumentException(Integer.toString(c));
        }

        // 第二次遍历：分配并填充 char[]
        final char[] v = new char[n];

        for (int i = offset, j = 0; i < end; i++, j++) {
            int c = codePoints[i];
            if (Character.isBmpCodePoint(c))
                v[j] = (char)c;
            else
                Character.toSurrogates(c, v, j++);
        }

        this.value = v;
    }

    /**
     * 从 8 位整数数组的子数组构造一个新的 {@code String}。
     *
     * <p> {@code offset} 参数是子数组的第一个字节的索引，{@code count} 参数指定子数组的长度。
     *
     * <p> 子数组中的每个 {@code byte} 都根据上述方法转换为一个 {@code char}。
     *
     * @deprecated 此方法不能正确地将字节转换为字符。自 JDK&nbsp;1.1 起，推荐使用带有 {@link
     * java.nio.charset.Charset}、字符集名称或使用平台默认字符集的 {@code String} 构造函数。
     *
     * @param  ascii
     *         要转换为字符的字节
     *
     * @param  hibyte
     *         每个 16 位 Unicode 代码单元的高 8 位
     *
     * @param  offset
     *         初始偏移量
     * @param  count
     *         长度
     *
     * @throws  IndexOutOfBoundsException
     *          如果 {@code offset} 或 {@code count} 参数无效
     *
     * @see  #String(byte[], int)
     * @see  #String(byte[], int, int, java.lang.String)
     * @see  #String(byte[], int, int, java.nio.charset.Charset)
     * @see  #String(byte[], int, int)
     * @see  #String(byte[], java.lang.String)
     * @see  #String(byte[], java.nio.charset.Charset)
     * @see  #String(byte[])
     */
    @Deprecated
    public String(byte ascii[], int hibyte, int offset, int count) {
        checkBounds(ascii, offset, count);
        char value[] = new char[count];

        if (hibyte == 0) {
            for (int i = count; i-- > 0;) {
                value[i] = (char)(ascii[i + offset] & 0xff);
            }
        } else {
            hibyte <<= 8;
            for (int i = count; i-- > 0;) {
                value[i] = (char)(hibyte | (ascii[i + offset] & 0xff));
            }
        }
        this.value = value;
    }

    /**
     * 从 8 位整数数组构造一个新的 {@code String}，每个字符 <i>c</i> 都从字节数组中的相应组件 <i>b</i> 构造，如下所示：
     *
     * <blockquote><pre>
     *     <b><i>c</i></b> == (char)(((hibyte &amp; 0xff) &lt;&lt; 8)
     *                         | (<b><i>b</i></b> &amp; 0xff))
     * </pre></blockquote>
     *
     * @deprecated  此方法不能正确地将字节转换为字符。自 JDK&nbsp;1.1 起，推荐使用带有 {@link
     * java.nio.charset.Charset}、字符集名称或使用平台默认字符集的 {@code String} 构造函数。
     *
     * @param  ascii
     *         要转换为字符的字节
     *
     * @param  hibyte
     *         每个 16 位 Unicode 代码单元的高 8 位
     *
     * @see  #String(byte[], int, int, java.lang.String)
     * @see  #String(byte[], int, int, java.nio.charset.Charset)
     * @see  #String(byte[], int, int)
     * @see  #String(byte[], java.lang.String)
     * @see  #String(byte[], java.nio.charset.Charset)
     * @see  #String(byte[])
     */
    @Deprecated
    public String(byte ascii[], int hibyte) {
        this(ascii, hibyte, 0, ascii.length);
    }

    /* 用于检查字节数组和请求的偏移量及长度值的通用私有实用方法，由 {@code String(byte[],..)} 构造函数使用。 */
    private static void checkBounds(byte[] bytes, int offset, int length) {
        if (length < 0)
            throw new StringIndexOutOfBoundsException(length);
        if (offset < 0)
            throw new StringIndexOutOfBoundsException(offset);
        if (offset > bytes.length - length)
            throw new StringIndexOutOfBoundsException(offset + length);
    }

    /**
     * 通过使用指定的字符集解码指定的字节数组子数组，构造一个新的 {@code String}。新 {@code String} 的长度是字符集的函数，因此可能不等于子数组的长度。
     *
     * <p> 当给定的字节在给定的字符集中无效时，此构造函数的行为是未指定的。当需要对解码过程进行更多控制时，应使用 {@link
     * java.nio.charset.CharsetDecoder} 类。
     *
     * @param  bytes
     *         要解码为字符的字节
     *
     * @param  offset
     *         要解码的第一个字节的索引
     *
     * @param  length
     *         要解码的字节数


    /**
     * 使用指定的 {@linkplain java.nio.charset.Charset 字符集} 解码指定的子数组来构造新的 {@code String}。
     * 新的 {@code String} 的长度是字符集的函数，因此可能不等于子数组的长度。
     *
     * <p>此方法始终将格式错误的输入和无法映射的字符序列替换为此字符集的默认替换字符串。如果需要对解码过程进行更多控制，应使用 {@link
     * java.nio.charset.CharsetDecoder} 类。
     *
     * @param  bytes
     *         要解码为字符的字节
     *
     * @param  offset
     *         要解码的第一个字节的索引
     *
     * @param  length
     *         要解码的字节数
     *
     * @param  charset
     *         要用于解码 {@code bytes} 的 {@linkplain java.nio.charset.Charset 字符集}
     *
     * @throws  IndexOutOfBoundsException
     *          如果 {@code offset} 和 {@code length} 参数索引的字符超出 {@code bytes} 数组的范围
     *
     * @since  1.6
     */
    public String(byte bytes[], int offset, int length, Charset charset) {
        if (charset == null)
            throw new NullPointerException("charset");
        checkBounds(bytes, offset, length);
        this.value =  StringCoding.decode(charset, bytes, offset, length);
    }

    /**
     * 使用指定的 {@linkplain java.nio.charset.Charset 字符集} 解码指定的字节数组来构造新的 {@code String}。
     * 新的 {@code String} 的长度是字符集的函数，因此可能不等于字节数组的长度。
     *
     * <p>当给定的字节在给定的字符集中无效时，此构造函数的行为是未指定的。如果需要对解码过程进行更多控制，应使用 {@link
     * java.nio.charset.CharsetDecoder} 类。
     *
     * @param  bytes
     *         要解码为字符的字节
     *
     * @param  charsetName
     *         支持的 {@linkplain java.nio.charset.Charset 字符集} 的名称
     *
     * @throws  UnsupportedEncodingException
     *          如果命名的字符集不受支持
     *
     * @since  JDK1.1
     */
    public String(byte bytes[], String charsetName)
            throws UnsupportedEncodingException {
        this(bytes, 0, bytes.length, charsetName);
    }

    /**
     * 使用指定的 {@linkplain java.nio.charset.Charset 字符集} 解码指定的字节数组来构造新的 {@code String}。
     * 新的 {@code String} 的长度是字符集的函数，因此可能不等于字节数组的长度。
     *
     * <p>此方法始终将格式错误的输入和无法映射的字符序列替换为此字符集的默认替换字符串。如果需要对解码过程进行更多控制，应使用 {@link
     * java.nio.charset.CharsetDecoder} 类。
     *
     * @param  bytes
     *         要解码为字符的字节
     *
     * @param  charset
     *         要用于解码 {@code bytes} 的 {@linkplain java.nio.charset.Charset 字符集}
     *
     * @since  1.6
     */
    public String(byte bytes[], Charset charset) {
        this(bytes, 0, bytes.length, charset);
    }

    /**
     * 使用平台的默认字符集解码指定的子数组来构造新的 {@code String}。新的 {@code String} 的长度是字符集的函数，因此可能不等于子数组的长度。
     *
     * <p>当给定的字节在默认字符集中无效时，此构造函数的行为是未指定的。如果需要对解码过程进行更多控制，应使用 {@link
     * java.nio.charset.CharsetDecoder} 类。
     *
     * @param  bytes
     *         要解码为字符的字节
     *
     * @param  offset
     *         要解码的第一个字节的索引
     *
     * @param  length
     *         要解码的字节数
     *
     * @throws  IndexOutOfBoundsException
     *          如果 {@code offset} 和 {@code length} 参数索引的字符超出 {@code bytes} 数组的范围
     *
     * @since  JDK1.1
     */
    public String(byte bytes[], int offset, int length) {
        checkBounds(bytes, offset, length);
        this.value = StringCoding.decode(bytes, offset, length);
    }

    /**
     * 使用平台的默认字符集解码指定的字节数组来构造新的 {@code String}。新的 {@code String} 的长度是字符集的函数，因此可能不等于字节数组的长度。
     *
     * <p>当给定的字节在默认字符集中无效时，此构造函数的行为是未指定的。如果需要对解码过程进行更多控制，应使用 {@link
     * java.nio.charset.CharsetDecoder} 类。
     *
     * @param  bytes
     *         要解码为字符的字节
     *
     * @since  JDK1.1
     */
    public String(byte bytes[]) {
        this(bytes, 0, bytes.length);
    }

    /**
     * 分配一个新的字符串，其中包含字符串缓冲区参数中当前包含的字符序列。字符串缓冲区的内容被复制；后续修改字符串缓冲区不会影响新创建的字符串。
     *
     * @param  buffer
     *         一个 {@code StringBuffer}
     */
    public String(StringBuffer buffer) {
        synchronized(buffer) {
            this.value = Arrays.copyOf(buffer.getValue(), buffer.length());
        }
    }

    /**
     * 分配一个新的字符串，其中包含字符串生成器参数中当前包含的字符序列。字符串生成器的内容被复制；后续修改字符串生成器不会影响新创建的字符串。
     *
     * <p>此构造函数是为了简化从 {@code
     * StringBuilder} 的迁移而提供的。通过 {@code
     * toString} 方法从字符串生成器获取字符串可能更快，并且通常更受推荐。
     *
     * @param   builder
     *          一个 {@code StringBuilder}
     *
     * @since  1.5
     */
    public String(StringBuilder builder) {
        this.value = Arrays.copyOf(builder.getValue(), builder.length());
    }

    /*
    * 包私有构造函数，为了速度共享 value 数组。
    * 该构造函数总是期望以 share==true 调用。
    * 需要一个单独的构造函数，因为我们已经有一个公共的 String(char[]) 构造函数，该构造函数会复制给定的 char[]。
    */
    String(char[] value, boolean share) {
        // assert share : "unshared not supported";
        this.value = value;
    }

    /**
     * 返回此字符串的长度。
     * 长度等于字符串中的 <a href="Character.html#unicode">Unicode
     * 代码单元</a> 数。
     *
     * @return  由该对象表示的字符序列的长度。
     */
    public int length() {
        return value.length;
    }

    /**
     * 如果且仅当 {@link #length()} 为 {@code 0} 时返回 {@code true}。
     *
     * @return 如果 {@link #length()} 为 {@code 0}，则返回 {@code true}，否则返回 {@code false}
     *
     * @since 1.6
     */
    public boolean isEmpty() {
        return value.length == 0;
    }

    /**
     * 返回指定索引处的 {@code char} 值。索引范围从 {@code 0} 到
     * {@code length() - 1}。序列中的第一个 {@code char} 值在索引 {@code 0} 处，下一个在索引 {@code 1} 处，
     * 以此类推，就像数组索引一样。
     *
     * <p>如果索引指定的 {@code char} 值是
     * <a href="Character.html#unicode">代理</a>，则返回代理值。
     *
     * @param      index   指定的 {@code char} 值的索引。
     * @return     该字符串中指定索引处的 {@code char} 值。
     *             第一个 {@code char} 值在索引 {@code 0} 处。
     * @exception  IndexOutOfBoundsException  如果 {@code index}
     *             参数为负或不小于该字符串的长度。
     */
    public char charAt(int index) {
        if ((index < 0) || (index >= value.length)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return value[index];
    }

    /**
     * 返回指定索引处的字符（Unicode 代码点）。索引指的是 {@code char} 值
     * （Unicode 代码单元），范围从 {@code 0} 到
     * {@link #length()}{@code  - 1}。
     *
     * <p>如果给定索引处的 {@code char} 值在高代理范围内，后续索引小于
     * 该字符串的长度，并且后续索引处的 {@code char} 值在
     * 低代理范围内，则返回该代理对对应的补充代码点。否则，
     * 返回给定索引处的 {@code char} 值。
     *
     * @param      index 指定的 {@code char} 值的索引
     * @return     指定索引处的字符的代码点值
     * @exception  IndexOutOfBoundsException  如果 {@code index}
     *             参数为负或不小于该字符串的长度。
     * @since      1.5
     */
    public int codePointAt(int index) {
        if ((index < 0) || (index >= value.length)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return Character.codePointAtImpl(value, index, value.length);
    }

    /**
     * 返回指定索引前的字符（Unicode 代码点）。索引指的是 {@code char} 值
     * （Unicode 代码单元），范围从 {@code 1} 到 {@link
     * CharSequence#length() length}。
     *
     * <p>如果 {@code (index - 1)} 处的 {@code char} 值在低代理范围内，{@code (index - 2)} 不为负，
     * 且 {@code (index - 2)} 处的 {@code char} 值在高代理范围内，则返回该代理对的补充代码点值。如果 {@code index - 1} 处的
     * {@code char} 值是未配对的低代理或高代理，则返回代理值。
     *
     * @param     index 应返回的代码点之后的索引
     * @return    给定索引之前的 Unicode 代码点值。
     * @exception IndexOutOfBoundsException 如果 {@code index}
     *            参数小于 1 或大于该字符串的长度。
     * @since     1.5
     */
    public int codePointBefore(int index) {
        int i = index - 1;
        if ((i < 0) || (i >= value.length)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return Character.codePointBeforeImpl(value, index, 0);
    }

    /**
     * 返回指定文本范围内的 Unicode 代码点数。文本范围从指定的 {@code beginIndex} 开始，扩展到
     * 索引 {@code endIndex - 1} 处的 {@code char}。因此，文本范围的长度（以 {@code char} 为单位）为
     * {@code endIndex-beginIndex}。未配对的代理在文本范围内每个计为一个代码点。
     *
     * @param beginIndex 第一个 {@code char} 的索引
     * @param endIndex 最后一个 {@code char} 之后的索引
     * @return 指定文本范围内的 Unicode 代码点数
     * @exception IndexOutOfBoundsException 如果
     * {@code beginIndex} 为负，或 {@code endIndex}
     * 大于该 {@code String} 的长度，或
     * {@code beginIndex} 大于 {@code endIndex}。
     * @since  1.5
     */
    public int codePointCount(int beginIndex, int endIndex) {
        if (beginIndex < 0 || endIndex > value.length || beginIndex > endIndex) {
            throw new IndexOutOfBoundsException();
        }
        return Character.codePointCountImpl(value, beginIndex, endIndex - beginIndex);
    }

    /**
     * 返回从给定的 {@code index} 开始，偏移 {@code codePointOffset} 代码点的索引。未配对的代理
     * 在由 {@code index} 和
     * {@code codePointOffset} 给出的文本范围内每个计为一个代码点。
     *
     * @param index 要偏移的索引
     * @param codePointOffset 代码点偏移量
     * @return 该字符串中的索引
     * @exception IndexOutOfBoundsException 如果 {@code index}
     *   为负或大于该
     *   {@code String} 的长度，或如果 {@code codePointOffset} 为正
     *   且从 {@code index} 开始的子字符串的代码点数少于 {@code codePointOffset}，
     *   或如果 {@code codePointOffset} 为负且 {@code index} 之前的子字符串的代码点数少于
     *   {@code codePointOffset} 的绝对值。
     * @since 1.5
     */
    public int offsetByCodePoints(int index, int codePointOffset) {
        if (index < 0 || index > value.length) {
            throw new IndexOutOfBoundsException();
        }
        return Character.offsetByCodePointsImpl(value, 0, value.length,
                index, codePointOffset);
    }

    /**
     * 将字符从该字符串复制到 dst，从 dstBegin 开始。
     * 该方法不执行任何范围检查。
     */
    void getChars(char dst[], int dstBegin) {
        System.arraycopy(value, 0, dst, dstBegin, value.length);
    }

    /**
     * 将字符从该字符串复制到目标字符数组。
     * <p>
     * 要复制的第一个字符的索引为 {@code srcBegin}；
     * 要复制的最后一个字符的索引为 {@code srcEnd-1}
     * （因此要复制的字符总数为
     * {@code srcEnd-srcBegin}）。字符被复制到
     * {@code dst} 的子数组中，从索引 {@code dstBegin} 开始，结束于索引：
     * <blockquote><pre>
     *     dstBegin + (srcEnd-srcBegin) - 1
     * </pre></blockquote>
     *
     * @param      srcBegin   字符串中要复制的第一个字符的索引。
     * @param      srcEnd     字符串中要复制的最后一个字符之后的索引。
     * @param      dst        目标数组。
     * @param      dstBegin   目标数组中的起始偏移量。
     * @exception IndexOutOfBoundsException 如果以下任何条件为真：
     *            <ul><li>{@code srcBegin} 为负。
     *            <li>{@code srcBegin} 大于 {@code srcEnd}
     *            <li>{@code srcEnd} 大于该字符串的长度
     *            <li>{@code dstBegin} 为负
     *            <li>{@code dstBegin+(srcEnd-srcBegin)} 大于
     *                {@code dst.length}</ul>
     */
    public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin) {
        if (srcBegin < 0) {
            throw new StringIndexOutOfBoundsException(srcBegin);
        }
        if (srcEnd > value.length) {
            throw new StringIndexOutOfBoundsException(srcEnd);
        }
        if (srcBegin > srcEnd) {
            throw new StringIndexOutOfBoundsException(srcEnd - srcBegin);
        }
        System.arraycopy(value, srcBegin, dst, dstBegin, srcEnd - srcBegin);
    }


                /**
     * 从该字符串中复制字符到目标字节数组。每个字节接收相应字符的8个低位。字符的8个高位不会被复制，也不会以任何方式参与传输。
     *
     * <p> 要复制的第一个字符的索引为 {@code srcBegin}；要复制的最后一个字符的索引为 {@code srcEnd-1}。要复制的字符总数为 {@code srcEnd-srcBegin}。转换为字节的字符被复制到 {@code
     * dst} 的子数组中，从索引 {@code dstBegin} 开始，到索引：
     *
     * <blockquote><pre>
     *     dstBegin + (srcEnd-srcBegin) - 1
     * </pre></blockquote>
     *
     * @deprecated  该方法不能正确地将字符转换为字节。自 JDK&nbsp;1.1 起，推荐使用 {@link #getBytes()} 方法，该方法使用平台的默认字符集。
     *
     * @param  srcBegin
     *         字符串中要复制的第一个字符的索引
     *
     * @param  srcEnd
     *         字符串中要复制的最后一个字符之后的索引
     *
     * @param  dst
     *         目标数组
     *
     * @param  dstBegin
     *         目标数组的起始偏移量
     *
     * @throws  IndexOutOfBoundsException
     *          如果以下任何条件为真：
     *          <ul>
     *            <li> {@code srcBegin} 为负
     *            <li> {@code srcBegin} 大于 {@code srcEnd}
     *            <li> {@code srcEnd} 大于该字符串的长度
     *            <li> {@code dstBegin} 为负
     *            <li> {@code dstBegin+(srcEnd-srcBegin)} 大于 {@code
     *                 dst.length}
     *          </ul>
     */
    @Deprecated
    public void getBytes(int srcBegin, int srcEnd, byte dst[], int dstBegin) {
        if (srcBegin < 0) {
            throw new StringIndexOutOfBoundsException(srcBegin);
        }
        if (srcEnd > value.length) {
            throw new StringIndexOutOfBoundsException(srcEnd);
        }
        if (srcBegin > srcEnd) {
            throw new StringIndexOutOfBoundsException(srcEnd - srcBegin);
        }
        Objects.requireNonNull(dst);

        int j = dstBegin;
        int n = srcEnd;
        int i = srcBegin;
        char[] val = value;   /* 避免 getfield 操作码 */

        while (i < n) {
            dst[j++] = (byte)val[i++];
        }
    }

    /**
     * 使用命名的字符集将此 {@code String} 编码为字节序列，并将结果存储到新的字节数组中。
     *
     * <p> 当此字符串无法用给定的字符集编码时，此方法的行为是未指定的。当需要对编码过程进行更多控制时，应使用 {@link
     * java.nio.charset.CharsetEncoder} 类。
     *
     * @param  charsetName
     *         支持的 {@linkplain java.nio.charset.Charset 字符集} 的名称
     *
     * @return  结果字节数组
     *
     * @throws  UnsupportedEncodingException
     *          如果命名的字符集不受支持
     *
     * @since  JDK1.1
     */
    public byte[] getBytes(String charsetName)
            throws UnsupportedEncodingException {
        if (charsetName == null) throw new NullPointerException();
        return StringCoding.encode(charsetName, value, 0, value.length);
    }

    /**
     * 使用给定的 {@linkplain java.nio.charset.Charset 字符集} 将此 {@code String} 编码为字节序列，并将结果存储到新的字节数组中。
     *
     * <p> 此方法总是用此字符集的默认替换字节数组替换格式错误的输入和无法映射的字符序列。当需要对编码过程进行更多控制时，应使用 {@link java.nio.charset.CharsetEncoder} 类。
     *
     * @param  charset
     *         用于编码 {@code String} 的 {@linkplain java.nio.charset.Charset}
     *
     * @return  结果字节数组
     *
     * @since  1.6
     */
    public byte[] getBytes(Charset charset) {
        if (charset == null) throw new NullPointerException();
        return StringCoding.encode(charset, value, 0, value.length);
    }

    /**
     * 使用平台的默认字符集将此 {@code String} 编码为字节序列，并将结果存储到新的字节数组中。
     *
     * <p> 当此字符串无法用默认字符集编码时，此方法的行为是未指定的。当需要对编码过程进行更多控制时，应使用 {@link
     * java.nio.charset.CharsetEncoder} 类。
     *
     * @return  结果字节数组
     *
     * @since      JDK1.1
     */
    public byte[] getBytes() {
        return StringCoding.encode(value, 0, value.length);
    }

    /**
     * 将此字符串与指定的对象进行比较。结果为 {@code
     * true} 当且仅当参数不为 {@code null} 并且是一个 {@code
     * String} 对象，表示与该对象相同的字符序列。
     *
     * @param  anObject
     *         要与该 {@code String} 比较的对象
     *
     * @return  如果给定对象表示一个与该字符串等效的 {@code String}，则返回 {@code true}，否则返回 {@code false}
     *
     * @see  #compareTo(String)
     * @see  #equalsIgnoreCase(String)
     */
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof String) {
            String anotherString = (String)anObject;
            int n = value.length;
            if (n == anotherString.value.length) {
                char v1[] = value;
                char v2[] = anotherString.value;
                int i = 0;
                while (n-- != 0) {
                    if (v1[i] != v2[i])
                        return false;
                    i++;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 将此字符串与指定的 {@code StringBuffer} 进行比较。结果为 {@code true} 当且仅当该 {@code String} 表示与指定的 {@code StringBuffer} 相同的字符序列。此方法同步 {@code StringBuffer}。
     *
     * @param  sb
     *         要与该 {@code String} 比较的 {@code StringBuffer}
     *
     * @return  如果该 {@code String} 表示与指定的 {@code StringBuffer} 相同的字符序列，则返回 {@code true}，否则返回 {@code false}
     *
     * @since  1.4
     */
    public boolean contentEquals(StringBuffer sb) {
        return contentEquals((CharSequence)sb);
    }

    private boolean nonSyncContentEquals(AbstractStringBuilder sb) {
        char v1[] = value;
        char v2[] = sb.getValue();
        int n = v1.length;
        if (n != sb.length()) {
            return false;
        }
        for (int i = 0; i < n; i++) {
            if (v1[i] != v2[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将此字符串与指定的 {@code CharSequence} 进行比较。结果为 {@code true} 当且仅当该 {@code String} 表示与指定的序列相同的字符值。注意，如果 {@code CharSequence} 是 {@code StringBuffer}，则该方法会同步它。
     *
     * @param  cs
     *         要与该 {@code String} 比较的序列
     *
     * @return  如果该 {@code String} 表示与指定的序列相同的字符值，则返回 {@code true}，否则返回 {@code
     *          false}
     *
     * @since  1.5
     */
    public boolean contentEquals(CharSequence cs) {
        // 参数是 StringBuffer 或 StringBuilder
        if (cs instanceof AbstractStringBuilder) {
            if (cs instanceof StringBuffer) {
                synchronized(cs) {
                   return nonSyncContentEquals((AbstractStringBuilder)cs);
                }
            } else {
                return nonSyncContentEquals((AbstractStringBuilder)cs);
            }
        }
        // 参数是 String
        if (cs instanceof String) {
            return equals(cs);
        }
        // 参数是通用 CharSequence
        char v1[] = value;
        int n = v1.length;
        if (n != cs.length()) {
            return false;
        }
        for (int i = 0; i < n; i++) {
            if (v1[i] != cs.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将此 {@code String} 与另一个 {@code String} 进行比较，忽略大小写。如果两个字符串长度相同且对应字符在忽略大小写的情况下相等，则认为它们相等。
     *
     * <p> 如果以下条件之一为真，则认为两个字符 {@code c1} 和 {@code c2} 在忽略大小写的情况下相同：
     * <ul>
     *   <li> 两个字符相同（由 {@code ==} 操作符比较）
     *   <li> 对每个字符应用 {@link
     *        java.lang.Character#toUpperCase(char)} 方法产生的结果相同
     *   <li> 对每个字符应用 {@link
     *        java.lang.Character#toLowerCase(char)} 方法产生的结果相同
     * </ul>
     *
     * @param  anotherString
     *         要与该 {@code String} 比较的 {@code String}
     *
     * @return  如果参数不为 {@code null} 且表示一个忽略大小写后与该字符串等效的 {@code String}，则返回 {@code true}；否则返回 {@code
     *          false}
     *
     * @see  #equals(Object)
     */
    public boolean equalsIgnoreCase(String anotherString) {
        return (this == anotherString) ? true
                : (anotherString != null)
                && (anotherString.value.length == value.length)
                && regionMatches(true, 0, anotherString, 0, value.length);
    }

    /**
     * 按字典顺序比较两个字符串。
     * 比较基于字符串中每个字符的 Unicode 值。该 {@code String} 对象表示的字符序列按字典顺序与参数字符串表示的字符序列进行比较。如果该 {@code String} 对象
     * 按字典顺序在参数字符串之前，则结果为负整数。如果该 {@code String} 对象按字典顺序在参数字符串之后，则结果为正整数。如果字符串相等，则结果为零；{@code compareTo} 返回 {@code 0} 时，{@link #equals(Object)} 方法也会返回 {@code true}。
     * <p>
     * 这是字典顺序的定义。如果两个字符串不同，则它们在某些索引处的字符不同，或者它们的长度不同，或者两者都有。如果它们在某个或多个索引处的字符不同，设 <i>k</i> 为最小的这样的索引；则字符值较小的字符串
     * 按字典顺序在另一个字符串之前。在这种情况下，{@code compareTo} 返回两个字符串在位置 {@code k} 的两个字符值之差
     * —— 即值：
     * <blockquote><pre>
     * this.charAt(k)-anotherString.charAt(k)
     * </pre></blockquote>
     * 如果没有索引处的字符不同，则较短的字符串按字典顺序在较长的字符串之前。在这种情况下，{@code compareTo} 返回字符串长度之差
     * —— 即值：
     * <blockquote><pre>
     * this.length()-anotherString.length()
     * </pre></blockquote>
     *
     * @param   anotherString   要比较的 {@code String}。
     * @return  如果参数字符串等于该字符串，则返回值 {@code 0}；如果该字符串按字典顺序小于字符串参数，则返回小于 {@code 0} 的值；如果该字符串按字典顺序大于字符串参数，则返回大于 {@code 0} 的值。
     */
    public int compareTo(String anotherString) {
        int len1 = value.length;
        int len2 = anotherString.value.length;
        int lim = Math.min(len1, len2);
        char v1[] = value;
        char v2[] = anotherString.value;

        int k = 0;
        while (k < lim) {
            char c1 = v1[k];
            char c2 = v2[k];
            if (c1 != c2) {
                return c1 - c2;
            }
            k++;
        }
        return len1 - len2;
    }

    /**
     * 一个按 {@code compareToIgnoreCase} 排序的 {@code String} 对象的比较器。此比较器是可序列化的。
     * <p>
     * 注意，此比较器不考虑区域设置，对于某些区域设置可能会产生不令人满意的排序结果。java.text 包提供了 <em>Collators</em> 以允许区域设置敏感的排序。
     *
     * @see     java.text.Collator#compare(String, String)
     * @since   1.2
     */
    public static final Comparator<String> CASE_INSENSITIVE_ORDER
                                         = new CaseInsensitiveComparator();
    private static class CaseInsensitiveComparator
            implements Comparator<String>, java.io.Serializable {
        // 为互操作性使用 JDK 1.2.2 的 serialVersionUID
        private static final long serialVersionUID = 8575799808933029326L;

        public int compare(String s1, String s2) {
            int n1 = s1.length();
            int n2 = s2.length();
            int min = Math.min(n1, n2);
            for (int i = 0; i < min; i++) {
                char c1 = s1.charAt(i);
                char c2 = s2.charAt(i);
                if (c1 != c2) {
                    c1 = Character.toUpperCase(c1);
                    c2 = Character.toUpperCase(c2);
                    if (c1 != c2) {
                        c1 = Character.toLowerCase(c1);
                        c2 = Character.toLowerCase(c2);
                        if (c1 != c2) {
                            // 由于数值提升，不会溢出
                            return c1 - c2;
                        }
                    }
                }
            }
            return n1 - n2;
        }

        /** 替换反序列化的对象。 */
        private Object readResolve() { return CASE_INSENSITIVE_ORDER; }
    }

    /**
     * 按字典顺序比较两个字符串，忽略大小写差异。此方法返回一个整数，其符号与调用 {@code compareTo} 方法时使用规范化版本的字符串相同，这些字符串通过调用
     * {@code Character.toLowerCase(Character.toUpperCase(character))} 消除了大小写差异。
     * <p>
     * 注意，此方法不考虑区域设置，对于某些区域设置可能会产生不令人满意的排序结果。java.text 包提供了 <em>collators</em> 以允许区域设置敏感的排序。
     *
     * @param   str   要比较的 {@code String}。
     * @return  一个负整数、零或正整数，分别表示指定的字符串大于、等于或小于此字符串，忽略大小写。
     * @see     java.text.Collator#compare(String, String)
     * @since   1.2
     */
    public int compareToIgnoreCase(String str) {
        return CASE_INSENSITIVE_ORDER.compare(this, str);
    }


                /**
     * 测试两个字符串区域是否相等。
     * <p>
     * 本 {@code String} 对象的一个子字符串与参数 other 的一个子字符串进行比较。如果这些子字符串表示相同的字符序列，则结果为 true。本
     * {@code String} 对象要比较的子字符串从索引 {@code toffset} 开始，长度为 {@code len}。other 要比较的子字符串
     * 从索引 {@code ooffset} 开始，长度为 {@code len}。结果为 {@code false} 当且仅当以下至少一项为真：
     * <ul><li>{@code toffset} 为负。
     * <li>{@code ooffset} 为负。
     * <li>{@code toffset+len} 大于本 {@code String} 对象的长度。
     * <li>{@code ooffset+len} 大于 other 参数的长度。
     * <li>存在一个小于 {@code len} 的非负整数 <i>k</i> 使得：
     * {@code this.charAt(toffset + }<i>k</i>{@code ) != other.charAt(ooffset + }
     * <i>k</i>{@code )}
     * </ul>
     *
     * @param   toffset   本字符串中子区域的起始偏移量。
     * @param   other     字符串参数。
     * @param   ooffset   字符串参数中子区域的起始偏移量。
     * @param   len       要比较的字符数。
     * @return  如果本字符串的指定子区域与字符串参数的指定子区域完全匹配，则返回 {@code true}；
     *          否则返回 {@code false}。
     */
    public boolean regionMatches(int toffset, String other, int ooffset,
            int len) {
        char ta[] = value;
        int to = toffset;
        char pa[] = other.value;
        int po = ooffset;
        // 注意：toffset, ooffset, 或 len 可能接近 -1>>>1。
        if ((ooffset < 0) || (toffset < 0)
                || (toffset > (long)value.length - len)
                || (ooffset > (long)other.value.length - len)) {
            return false;
        }
        while (len-- > 0) {
            if (ta[to++] != pa[po++]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 测试两个字符串区域是否相等。
     * <p>
     * 本 {@code String} 对象的一个子字符串与参数 {@code other} 的一个子字符串进行比较。如果这些子字符串表示的字符序列相同，忽略大小写当且仅当 {@code ignoreCase} 为真，则结果为 {@code true}。本
     * {@code String} 对象要比较的子字符串从索引 {@code toffset} 开始，长度为 {@code len}。other 要比较的子字符串
     * 从索引 {@code ooffset} 开始，长度为 {@code len}。结果为 {@code false} 当且仅当以下至少一项为真：
     * <ul><li>{@code toffset} 为负。
     * <li>{@code ooffset} 为负。
     * <li>{@code toffset+len} 大于本 {@code String} 对象的长度。
     * <li>{@code ooffset+len} 大于 other 参数的长度。
     * <li>{@code ignoreCase} 为 {@code false} 且存在一个小于 {@code len} 的非负整数 <i>k</i> 使得：
     * <blockquote><pre>
     * this.charAt(toffset+k) != other.charAt(ooffset+k)
     * </pre></blockquote>
     * <li>{@code ignoreCase} 为 {@code true} 且存在一个小于 {@code len} 的非负整数 <i>k</i> 使得：
     * <blockquote><pre>
     * Character.toLowerCase(this.charAt(toffset+k)) !=
     Character.toLowerCase(other.charAt(ooffset+k))
     * </pre></blockquote>
     * 并且：
     * <blockquote><pre>
     * Character.toUpperCase(this.charAt(toffset+k)) !=
     *         Character.toUpperCase(other.charAt(ooffset+k))
     * </pre></blockquote>
     * </ul>
     *
     * @param   ignoreCase   如果为 {@code true}，则在比较字符时忽略大小写。
     * @param   toffset      本字符串中子区域的起始偏移量。
     * @param   other        字符串参数。
     * @param   ooffset      字符串参数中子区域的起始偏移量。
     * @param   len          要比较的字符数。
     * @return  如果本字符串的指定子区域与字符串参数的指定子区域匹配，则返回 {@code true}；
     *          否则返回 {@code false}。匹配是否精确或忽略大小写取决于 {@code ignoreCase} 参数。
     */
    public boolean regionMatches(boolean ignoreCase, int toffset,
            String other, int ooffset, int len) {
        char ta[] = value;
        int to = toffset;
        char pa[] = other.value;
        int po = ooffset;
        // 注意：toffset, ooffset, 或 len 可能接近 -1>>>1。
        if ((ooffset < 0) || (toffset < 0)
                || (toffset > (long)value.length - len)
                || (ooffset > (long)other.value.length - len)) {
            return false;
        }
        while (len-- > 0) {
            char c1 = ta[to++];
            char c2 = pa[po++];
            if (c1 == c2) {
                continue;
            }
            if (ignoreCase) {
                // 如果字符不匹配但可以忽略大小写，则尝试将两个字符都转换为大写。
                // 如果结果匹配，则比较扫描应继续。
                char u1 = Character.toUpperCase(c1);
                char u2 = Character.toUpperCase(c2);
                if (u1 == u2) {
                    continue;
                }
                // 不幸的是，大写转换对格鲁吉亚字母表不起作用，因为格鲁吉亚字母表在大小写转换方面有奇怪的规则。
                // 因此，在退出之前需要进行最后一次检查。
                if (Character.toLowerCase(u1) == Character.toLowerCase(u2)) {
                    continue;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * 测试从指定索引开始的本字符串的子字符串是否以指定的前缀开头。
     *
     * @param   prefix    前缀。
     * @param   toffset   本字符串中开始查找的位置。
     * @return  如果由参数表示的字符序列是本对象从索引 {@code toffset} 开始的子字符串的前缀，则返回 {@code true}；否则返回 {@code false}。
     *          如果 {@code toffset} 为负或大于本 {@code String} 对象的长度，则结果为 {@code false}；否则结果与表达式
     *          <pre>
     *          this.substring(toffset).startsWith(prefix)
     *          </pre>
     *          的结果相同。
     */
    public boolean startsWith(String prefix, int toffset) {
        char ta[] = value;
        int to = toffset;
        char pa[] = prefix.value;
        int po = 0;
        int pc = prefix.value.length;
        // 注意：toffset 可能接近 -1>>>1。
        if ((toffset < 0) || (toffset > value.length - pc)) {
            return false;
        }
        while (--pc >= 0) {
            if (ta[to++] != pa[po++]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 测试本字符串是否以指定的前缀开头。
     *
     * @param   prefix   前缀。
     * @return  如果由参数表示的字符序列是本字符串表示的字符序列的前缀，则返回 {@code true}；否则返回 {@code false}。
     *          注意，如果参数是空字符串或与本 {@code String} 对象相等（由 {@link #equals(Object)} 方法确定），则返回 {@code true}。
     * @since   1. 0
     */
    public boolean startsWith(String prefix) {
        return startsWith(prefix, 0);
    }

    /**
     * 测试本字符串是否以指定的后缀结尾。
     *
     * @param   suffix   后缀。
     * @return  如果由参数表示的字符序列是本对象表示的字符序列的后缀，则返回 {@code true}；否则返回 {@code false}。注意，如果参数是空字符串或与本 {@code String} 对象相等（由 {@link #equals(Object)} 方法确定），则返回 {@code true}。
     */
    public boolean endsWith(String suffix) {
        return startsWith(suffix, value.length - suffix.value.length);
    }

    /**
     * 返回本字符串的哈希码。本 {@code String} 对象的哈希码计算如下：
     * <blockquote><pre>
     * s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
     * </pre></blockquote>
     * 使用 {@code int} 算术，其中 {@code s[i]} 是字符串的第 <i>i</i> 个字符，{@code n} 是字符串的长度，{@code ^} 表示乘方。
     * （空字符串的哈希值为零。）
     *
     * @return  本对象的哈希码值。
     */
    public int hashCode() {
        int h = hash;
        if (h == 0 && value.length > 0) {
            char val[] = value;

            for (int i = 0; i < value.length; i++) {
                h = 31 * h + val[i];
            }
            hash = h;
        }
        return h;
    }

    /**
     * 返回本字符串中指定字符第一次出现的索引。如果值为
     * {@code ch} 的字符出现在本 {@code String} 对象表示的字符序列中，则返回该字符（以 Unicode 代码单元表示）第一次出现的索引。对于
     * {@code ch} 在 0 到 0xFFFF（包括）范围内的值，这是最小的值 <i>k</i> 使得：
     * <blockquote><pre>
     * this.charAt(<i>k</i>) == ch
     * </pre></blockquote>
     * 为真。对于其他值的 {@code ch}，这是最小的值 <i>k</i> 使得：
     * <blockquote><pre>
     * this.codePointAt(<i>k</i>) == ch
     * </pre></blockquote>
     * 为真。在任何情况下，如果本字符串中没有这样的字符，则返回 {@code -1}。
     *
     * @param   ch   一个字符（Unicode 代码点）。
     * @return  本对象表示的字符序列中该字符第一次出现的索引，或者
     *          如果该字符未出现，则返回 {@code -1}。
     */
    public int indexOf(int ch) {
        return indexOf(ch, 0);
    }

    /**
     * 从指定索引开始搜索，返回本字符串中指定字符第一次出现的索引。
     * <p>
     * 如果值为 {@code ch} 的字符出现在本 {@code String}
     * 对象表示的字符序列中且索引不小于 {@code fromIndex}，则返回该字符第一次出现的索引。对于
     * {@code ch} 在 0 到 0xFFFF（包括）范围内的值，这是最小的值 <i>k</i> 使得：
     * <blockquote><pre>
     * (this.charAt(<i>k</i>) == ch) {@code &&} (<i>k</i> &gt;= fromIndex)
     * </pre></blockquote>
     * 为真。对于其他值的 {@code ch}，这是最小的值 <i>k</i> 使得：
     * <blockquote><pre>
     * (this.codePointAt(<i>k</i>) == ch) {@code &&} (<i>k</i> &gt;= fromIndex)
     * </pre></blockquote>
     * 为真。在任何情况下，如果本字符串中没有这样的字符或在位置 {@code fromIndex} 之后没有出现这样的字符，则返回
     * {@code -1}。
     *
     * <p>
     * 没有对 {@code fromIndex} 的值进行限制。如果它为负，则效果与零相同：搜索整个字符串。如果它大于本字符串的长度，则效果与等于本字符串的长度相同：返回
     * {@code -1}。
     *
     * <p>所有索引都以 {@code char} 值（Unicode 代码单元）指定。
     *
     * @param   ch          一个字符（Unicode 代码点）。
     * @param   fromIndex   开始搜索的索引。
     * @return  本对象表示的字符序列中该字符第一次出现的索引，该索引大于或等于 {@code fromIndex}，或者
     *          如果该字符未出现，则返回 {@code -1}。
     */
    public int indexOf(int ch, int fromIndex) {
        final int max = value.length;
        if (fromIndex < 0) {
            fromIndex = 0;
        } else if (fromIndex >= max) {
            // 注意：fromIndex 可能接近 -1>>>1。
            return -1;
        }

        if (ch < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
            // 处理大多数情况（ch 是 BMP 代码点或负值（无效代码点））
            final char[] value = this.value;
            for (int i = fromIndex; i < max; i++) {
                if (value[i] == ch) {
                    return i;
                }
            }
            return -1;
        } else {
            return indexOfSupplementary(ch, fromIndex);
        }
    }

    /**
     * 处理（罕见的）带有补充字符的 indexOf 调用。
     */
    private int indexOfSupplementary(int ch, int fromIndex) {
        if (Character.isValidCodePoint(ch)) {
            final char[] value = this.value;
            final char hi = Character.highSurrogate(ch);
            final char lo = Character.lowSurrogate(ch);
            final int max = value.length - 1;
            for (int i = fromIndex; i < max; i++) {
                if (value[i] == hi && value[i + 1] == lo) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 返回本字符串中指定字符最后一次出现的索引。对于
     * {@code ch} 在 0 到 0xFFFF（包括）范围内的值，返回的索引（以 Unicode 代码单元表示）是最大的值 <i>k</i> 使得：
     * <blockquote><pre>
     * this.charAt(<i>k</i>) == ch
     * </pre></blockquote>
     * 为真。对于其他值的 {@code ch}，这是最大的值 <i>k</i> 使得：
     * <blockquote><pre>
     * this.codePointAt(<i>k</i>) == ch
     * </pre></blockquote>
     * 为真。在任何情况下，如果本字符串中没有这样的字符，则返回 {@code -1}。搜索从最后一个字符开始向后进行。
     *
     * @param   ch   一个字符（Unicode 代码点）。
     * @return  本对象表示的字符序列中该字符最后一次出现的索引，或者
     *          如果该字符未出现，则返回 {@code -1}。
     */
    public int lastIndexOf(int ch) {
        return lastIndexOf(ch, value.length - 1);
    }

    /**
     * 从指定索引开始向后搜索，返回本字符串中指定字符最后一次出现的索引。对于
     * {@code ch} 在 0 到 0xFFFF（包括）范围内的值，返回的索引是最大的值 <i>k</i> 使得：
     * <blockquote><pre>
     * (this.charAt(<i>k</i>) == ch) {@code &&} (<i>k</i> &lt;= fromIndex)
     * </pre></blockquote>
     * 为真。对于其他值的 {@code ch}，这是最大的值 <i>k</i> 使得：
     * <blockquote><pre>
     * (this.codePointAt(<i>k</i>) == ch) {@code &&} (<i>k</i> &lt;= fromIndex)
     * </pre></blockquote>
     * 为真。在任何情况下，如果本字符串中没有这样的字符或在位置 {@code fromIndex} 之前没有出现这样的字符，则返回
     * {@code -1}。
     *
     * <p>所有索引都以 {@code char} 值（Unicode 代码单元）指定。
     *
     * @param   ch          一个字符（Unicode 代码点）。
     * @param   fromIndex   开始搜索的索引。没有对 {@code fromIndex} 的值进行限制。如果它大于或等于本字符串的长度，则效果与等于本字符串的长度减一相同：搜索整个字符串。如果它为负，则效果与 -1 相同：返回
     *          -1。
     * @return  本对象表示的字符序列中该字符最后一次出现的索引，该索引小于或等于 {@code fromIndex}，或者
     *          如果该字符未出现，则返回 {@code -1}。
     */
    public int lastIndexOf(int ch, int fromIndex) {
        if (ch < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
            // 处理大多数情况（ch 是 BMP 代码点或负值（无效代码点））
            final char[] value = this.value;
            int i = Math.min(fromIndex, value.length - 1);
            for (; i >= 0; i--) {
                if (value[i] == ch) {
                    return i;
                }
            }
            return -1;
        } else {
            return lastIndexOfSupplementary(ch, fromIndex);
        }
    }


                /**
     * 处理（罕见的）调用 lastIndexOf 时带有补充字符的情况。
     */
    private int lastIndexOfSupplementary(int ch, int fromIndex) {
        if (Character.isValidCodePoint(ch)) {
            final char[] value = this.value;
            char hi = Character.highSurrogate(ch);
            char lo = Character.lowSurrogate(ch);
            int i = Math.min(fromIndex, value.length - 2);
            for (; i >= 0; i--) {
                if (value[i] == hi && value[i + 1] == lo) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 返回此字符串中指定子字符串第一次出现的索引。
     *
     * <p>返回的索引是满足以下条件的最小值 <i>k</i>：
     * <blockquote><pre>
     * this.startsWith(str, <i>k</i>)
     * </pre></blockquote>
     * 如果不存在这样的 <i>k</i> 值，则返回 {@code -1}。
     *
     * @param   str   要搜索的子字符串。
     * @return  指定子字符串第一次出现的索引，
     *          或者如果不存在这样的出现，则返回 {@code -1}。
     */
    public int indexOf(String str) {
        return indexOf(str, 0);
    }

    /**
     * 返回此字符串中指定子字符串从指定索引开始第一次出现的索引。
     *
     * <p>返回的索引是满足以下条件的最小值 <i>k</i>：
     * <blockquote><pre>
     * <i>k</i> &gt;= fromIndex {@code &&} this.startsWith(str, <i>k</i>)
     * </pre></blockquote>
     * 如果不存在这样的 <i>k</i> 值，则返回 {@code -1}。
     *
     * @param   str         要搜索的子字符串。
     * @param   fromIndex   开始搜索的索引。
     * @return  指定子字符串从指定索引开始第一次出现的索引，
     *          或者如果不存在这样的出现，则返回 {@code -1}。
     */
    public int indexOf(String str, int fromIndex) {
        return indexOf(value, 0, value.length,
                str.value, 0, str.value.length, fromIndex);
    }

    /**
     * 由 String 和 AbstractStringBuilder 共享的搜索代码。源是被搜索的字符数组，目标是被搜索的字符串。
     *
     * @param   source       被搜索的字符。
     * @param   sourceOffset 源字符串的偏移量。
     * @param   sourceCount  源字符串的长度。
     * @param   target       被搜索的字符。
     * @param   fromIndex    开始搜索的索引。
     */
    static int indexOf(char[] source, int sourceOffset, int sourceCount,
            String target, int fromIndex) {
        return indexOf(source, sourceOffset, sourceCount,
                       target.value, 0, target.value.length,
                       fromIndex);
    }

    /**
     * 由 String 和 StringBuffer 共享的搜索代码。源是被搜索的字符数组，目标是被搜索的字符串。
     *
     * @param   source       被搜索的字符。
     * @param   sourceOffset 源字符串的偏移量。
     * @param   sourceCount  源字符串的长度。
     * @param   target       被搜索的字符。
     * @param   targetOffset 目标字符串的偏移量。
     * @param   targetCount  目标字符串的长度。
     * @param   fromIndex    开始搜索的索引。
     */
    static int indexOf(char[] source, int sourceOffset, int sourceCount,
            char[] target, int targetOffset, int targetCount,
            int fromIndex) {
        if (fromIndex >= sourceCount) {
            return (targetCount == 0 ? sourceCount : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetCount == 0) {
            return fromIndex;
        }

        char first = target[targetOffset];
        int max = sourceOffset + (sourceCount - targetCount);

        for (int i = sourceOffset + fromIndex; i <= max; i++) {
            /* 查找第一个字符。 */
            if (source[i] != first) {
                while (++i <= max && source[i] != first);
            }

            /* 找到第一个字符，现在查找其余的 v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                for (int k = targetOffset + 1; j < end && source[j]
                        == target[k]; j++, k++);

                if (j == end) {
                    /* 找到整个字符串。 */
                    return i - sourceOffset;
                }
            }
        }
        return -1;
    }

    /**
     * 返回此字符串中指定子字符串最后一次出现的索引。空字符串 "" 的最后一次出现被认为发生在索引值 {@code this.length()}。
     *
     * <p>返回的索引是满足以下条件的最大值 <i>k</i>：
     * <blockquote><pre>
     * this.startsWith(str, <i>k</i>)
     * </pre></blockquote>
     * 如果不存在这样的 <i>k</i> 值，则返回 {@code -1}。
     *
     * @param   str   要搜索的子字符串。
     * @return  指定子字符串最后一次出现的索引，
     *          或者如果不存在这样的出现，则返回 {@code -1}。
     */
    public int lastIndexOf(String str) {
        return lastIndexOf(str, value.length);
    }

    /**
     * 返回此字符串中指定子字符串从指定索引开始向后搜索最后一次出现的索引。
     *
     * <p>返回的索引是满足以下条件的最大值 <i>k</i>：
     * <blockquote><pre>
     * <i>k</i> {@code <=} fromIndex {@code &&} this.startsWith(str, <i>k</i>)
     * </pre></blockquote>
     * 如果不存在这样的 <i>k</i> 值，则返回 {@code -1}。
     *
     * @param   str         要搜索的子字符串。
     * @param   fromIndex   开始搜索的索引。
     * @return  指定子字符串从指定索引开始向后搜索最后一次出现的索引，
     *          或者如果不存在这样的出现，则返回 {@code -1}。
     */
    public int lastIndexOf(String str, int fromIndex) {
        return lastIndexOf(value, 0, value.length,
                str.value, 0, str.value.length, fromIndex);
    }

    /**
     * 由 String 和 AbstractStringBuilder 共享的搜索代码。源是被搜索的字符数组，目标是被搜索的字符串。
     *
     * @param   source       被搜索的字符。
     * @param   sourceOffset 源字符串的偏移量。
     * @param   sourceCount  源字符串的长度。
     * @param   target       被搜索的字符。
     * @param   fromIndex    开始搜索的索引。
     */
    static int lastIndexOf(char[] source, int sourceOffset, int sourceCount,
            String target, int fromIndex) {
        return lastIndexOf(source, sourceOffset, sourceCount,
                       target.value, 0, target.value.length,
                       fromIndex);
    }

    /**
     * 由 String 和 StringBuffer 共享的搜索代码。源是被搜索的字符数组，目标是被搜索的字符串。
     *
     * @param   source       被搜索的字符。
     * @param   sourceOffset 源字符串的偏移量。
     * @param   sourceCount  源字符串的长度。
     * @param   target       被搜索的字符。
     * @param   targetOffset 目标字符串的偏移量。
     * @param   targetCount  目标字符串的长度。
     * @param   fromIndex    开始搜索的索引。
     */
    static int lastIndexOf(char[] source, int sourceOffset, int sourceCount,
            char[] target, int targetOffset, int targetCount,
            int fromIndex) {
        /*
         * 检查参数；尽可能立即返回。为了保持一致性，不检查 null str。
         */
        int rightIndex = sourceCount - targetCount;
        if (fromIndex < 0) {
            return -1;
        }
        if (fromIndex > rightIndex) {
            fromIndex = rightIndex;
        }
        /* 空字符串总是匹配。 */
        if (targetCount == 0) {
            return fromIndex;
        }

        int strLastIndex = targetOffset + targetCount - 1;
        char strLastChar = target[strLastIndex];
        int min = sourceOffset + targetCount - 1;
        int i = min + fromIndex;

    startSearchForLastChar:
        while (true) {
            while (i >= min && source[i] != strLastChar) {
                i--;
            }
            if (i < min) {
                return -1;
            }
            int j = i - 1;
            int start = j - (targetCount - 1);
            int k = strLastIndex - 1;

            while (j > start) {
                if (source[j--] != target[k--]) {
                    i--;
                    continue startSearchForLastChar;
                }
            }
            return start - sourceOffset + 1;
        }
    }

    /**
     * 返回此字符串的一个子字符串。子字符串从指定的索引开始，延伸到此字符串的末尾。<p>
     * 示例：
     * <blockquote><pre>
     * "unhappy".substring(2) 返回 "happy"
     * "Harbison".substring(3) 返回 "bison"
     * "emptiness".substring(9) 返回 "" (一个空字符串)
     * </pre></blockquote>
     *
     * @param      beginIndex   开始索引，包含。
     * @return     指定的子字符串。
     * @exception  IndexOutOfBoundsException  如果
     *             {@code beginIndex} 是负数或大于此
     *             {@code String} 对象的长度。
     */
    public String substring(int beginIndex) {
        if (beginIndex < 0) {
            throw new StringIndexOutOfBoundsException(beginIndex);
        }
        int subLen = value.length - beginIndex;
        if (subLen < 0) {
            throw new StringIndexOutOfBoundsException(subLen);
        }
        return (beginIndex == 0) ? this : new String(value, beginIndex, subLen);
    }

    /**
     * 返回此字符串的一个子字符串。子字符串从指定的 {@code beginIndex} 开始，
     * 延伸到索引 {@code endIndex - 1} 的字符。因此，子字符串的长度为 {@code endIndex-beginIndex}。
     * <p>
     * 示例：
     * <blockquote><pre>
     * "hamburger".substring(4, 8) 返回 "urge"
     * "smiles".substring(1, 5) 返回 "mile"
     * </pre></blockquote>
     *
     * @param      beginIndex   开始索引，包含。
     * @param      endIndex     结束索引，不包含。
     * @return     指定的子字符串。
     * @exception  IndexOutOfBoundsException  如果
     *             {@code beginIndex} 是负数，或者
     *             {@code endIndex} 大于此
     *             {@code String} 对象的长度，或者
     *             {@code beginIndex} 大于
     *             {@code endIndex}。
     */
    public String substring(int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            throw new StringIndexOutOfBoundsException(beginIndex);
        }
        if (endIndex > value.length) {
            throw new StringIndexOutOfBoundsException(endIndex);
        }
        int subLen = endIndex - beginIndex;
        if (subLen < 0) {
            throw new StringIndexOutOfBoundsException(subLen);
        }
        return ((beginIndex == 0) && (endIndex == value.length)) ? this
                : new String(value, beginIndex, subLen);
    }

    /**
     * 返回此序列的一个子序列。
     *
     * <p> 以下形式的此方法调用
     *
     * <blockquote><pre>
     * str.subSequence(begin,&nbsp;end)</pre></blockquote>
     *
     * 的行为与以下形式的调用完全相同
     *
     * <blockquote><pre>
     * str.substring(begin,&nbsp;end)</pre></blockquote>
     *
     * @apiNote
     * 此方法定义为使 {@code String} 类可以实现
     * {@link CharSequence} 接口。
     *
     * @param   beginIndex   开始索引，包含。
     * @param   endIndex     结束索引，不包含。
     * @return  指定的子序列。
     *
     * @throws  IndexOutOfBoundsException
     *          如果 {@code beginIndex} 或 {@code endIndex} 是负数，
     *          如果 {@code endIndex} 大于 {@code length()}，
     *          或者如果 {@code beginIndex} 大于 {@code endIndex}
     *
     * @since 1.4
     * @spec JSR-51
     */
    public CharSequence subSequence(int beginIndex, int endIndex) {
        return this.substring(beginIndex, endIndex);
    }

    /**
     * 将指定的字符串连接到此字符串的末尾。
     * <p>
     * 如果参数字符串的长度为 {@code 0}，则返回此
     * {@code String} 对象。否则，返回一个
     * {@code String} 对象，表示此 {@code String} 对象的字符序列
     * 与参数字符串的字符序列的连接。<p>
     * 示例：
     * <blockquote><pre>
     * "cares".concat("s") 返回 "caress"
     * "to".concat("get").concat("her") 返回 "together"
     * </pre></blockquote>
     *
     * @param   str   连接到此 {@code String} 末尾的 {@code String}。
     * @return  一个字符串，表示此对象的字符序列后跟字符串参数的字符序列。
     */
    public String concat(String str) {
        int otherLen = str.length();
        if (otherLen == 0) {
            return this;
        }
        int len = value.length;
        char buf[] = Arrays.copyOf(value, len + otherLen);
        str.getChars(buf, len);
        return new String(buf, true);
    }

    /**
     * 返回一个字符串，该字符串是将此字符串中所有出现的
     * {@code oldChar} 替换为 {@code newChar} 后的结果。
     * <p>
     * 如果字符 {@code oldChar} 不出现在
     * 此 {@code String} 对象表示的字符序列中，
     * 则返回对此 {@code String} 对象的引用。
     * 否则，返回一个 {@code String} 对象，表示一个字符序列，
     * 该字符序列与此 {@code String} 对象表示的字符序列相同，
     * 但每个出现的 {@code oldChar} 都被一个出现的 {@code newChar} 替换。
     * <p>
     * 示例：
     * <blockquote><pre>
     * "mesquite in your cellar".replace('e', 'o')
     *         返回 "mosquito in your collar"
     * "the war of baronets".replace('r', 'y')
     *         返回 "the way of bayonets"
     * "sparring with a purple porpoise".replace('p', 't')
     *         返回 "starring with a turtle tortoise"
     * "JonL".replace('q', 'x') 返回 "JonL" (无变化)
     * </pre></blockquote>
     *
     * @param   oldChar   旧字符。
     * @param   newChar   新字符。
     * @return  一个从这个字符串派生的字符串，其中每个
     *          出现的 {@code oldChar} 都被 {@code newChar} 替换。
     */
    public String replace(char oldChar, char newChar) {
        if (oldChar != newChar) {
            int len = value.length;
            int i = -1;
            char[] val = value; /* 避免 getfield 操作码 */


    /**
     * 判断此字符串是否与给定的<a
     * href="../util/regex/Pattern.html#sum">正则表达式</a>匹配。
     *
     * <p> 该方法的调用形式为
     * <i>str</i>{@code .matches(}<i>regex</i>{@code )} 产生的结果与以下表达式完全相同
     *
     * <blockquote>
     * {@link java.util.regex.Pattern}.{@link java.util.regex.Pattern#matches(String,CharSequence)
     * matches(<i>regex</i>, <i>str</i>)}
     * </blockquote>
     *
     * @param   regex
     *          要与该字符串匹配的正则表达式
     *
     * @return  如果且仅当该字符串与给定的正则表达式匹配时返回 {@code true}
     *
     * @throws  PatternSyntaxException
     *          如果正则表达式的语法无效
     *
     * @see java.util.regex.Pattern
     *
     * @since 1.4
     * @spec JSR-51
     */
    public boolean matches(String regex) {
        return Pattern.matches(regex, this);
    }

    /**
     * 如果且仅当此字符串包含指定的字符序列时返回 true。
     *
     * @param s 要搜索的序列
     * @return 如果此字符串包含 {@code s}，则返回 true，否则返回 false
     * @since 1.5
     */
    public boolean contains(CharSequence s) {
        return indexOf(s.toString()) > -1;
    }

    /**
     * 用给定的替换项替换此字符串中第一个匹配给定<a
     * href="../util/regex/Pattern.html#sum">正则表达式</a>的子串。
     *
     * <p> 该方法的调用形式为
     * <i>str</i>{@code .replaceFirst(}<i>regex</i>{@code ,} <i>repl</i>{@code )}
     * 产生的结果与以下表达式完全相同
     *
     * <blockquote>
     * <code>
     * {@link java.util.regex.Pattern}.{@link
     * java.util.regex.Pattern#compile compile}(<i>regex</i>).{@link
     * java.util.regex.Pattern#matcher(java.lang.CharSequence) matcher}(<i>str</i>).{@link
     * java.util.regex.Matcher#replaceFirst replaceFirst}(<i>repl</i>)
     * </code>
     * </blockquote>
     *
     *<p>
     * 请注意，替换字符串中的反斜杠（{@code \}）和美元符号（{@code $}）可能会导致结果与将其视为字面替换字符串时不同；请参见
     * {@link java.util.regex.Matcher#replaceFirst}。
     * 如果需要，可以使用 {@link java.util.regex.Matcher#quoteReplacement} 来抑制这些字符的特殊含义。
     *
     * @param   regex
     *          要与该字符串匹配的正则表达式
     * @param   replacement
     *          要替换第一个匹配项的字符串
     *
     * @return  结果 {@code String}
     *
     * @throws  PatternSyntaxException
     *          如果正则表达式的语法无效
     *
     * @see java.util.regex.Pattern
     *
     * @since 1.4
     * @spec JSR-51
     */
    public String replaceFirst(String regex, String replacement) {
        return Pattern.compile(regex).matcher(this).replaceFirst(replacement);
    }

    /**
     * 用给定的替换项替换此字符串中每个匹配给定<a
     * href="../util/regex/Pattern.html#sum">正则表达式</a>的子串。
     *
     * <p> 该方法的调用形式为
     * <i>str</i>{@code .replaceAll(}<i>regex</i>{@code ,} <i>repl</i>{@code )}
     * 产生的结果与以下表达式完全相同
     *
     * <blockquote>
     * <code>
     * {@link java.util.regex.Pattern}.{@link
     * java.util.regex.Pattern#compile compile}(<i>regex</i>).{@link
     * java.util.regex.Pattern#matcher(java.lang.CharSequence) matcher}(<i>str</i>).{@link
     * java.util.regex.Matcher#replaceAll replaceAll}(<i>repl</i>)
     * </code>
     * </blockquote>
     *
     *<p>
     * 请注意，替换字符串中的反斜杠（{@code \}）和美元符号（{@code $}）可能会导致结果与将其视为字面替换字符串时不同；请参见
     * {@link java.util.regex.Matcher#replaceAll Matcher.replaceAll}。
     * 如果需要，可以使用 {@link java.util.regex.Matcher#quoteReplacement} 来抑制这些字符的特殊含义。
     *
     * @param   regex
     *          要与该字符串匹配的正则表达式
     * @param   replacement
     *          要替换每个匹配项的字符串
     *
     * @return  结果 {@code String}
     *
     * @throws  PatternSyntaxException
     *          如果正则表达式的语法无效
     *
     * @see java.util.regex.Pattern
     *
     * @since 1.4
     * @spec JSR-51
     */
    public String replaceAll(String regex, String replacement) {
        return Pattern.compile(regex).matcher(this).replaceAll(replacement);
    }

    /**
     * 用指定的字面替换序列替换此字符串中每个匹配字面目标序列的子串。替换从字符串的开头到结尾进行，例如，在字符串 "aaa" 中用 "b" 替换 "aa" 将得到
     * "ba" 而不是 "ab"。
     *
     * @param  target 要替换的字符序列
     * @param  replacement 替换的字符序列
     * @return  结果字符串
     * @since 1.5
     */
    public String replace(CharSequence target, CharSequence replacement) {
        return Pattern.compile(target.toString(), Pattern.LITERAL).matcher(
                this).replaceAll(Matcher.quoteReplacement(replacement.toString()));
    }

    /**
     * 按照给定的<a
     * href="../util/regex/Pattern.html#sum">正则表达式</a>匹配分割此字符串。
     *
     * <p> 该方法返回的数组包含此字符串的每个子串，这些子串由另一个匹配给定表达式的子串终止，或者由字符串的结尾终止。数组中的子串按其在字符串中出现的顺序排列。如果表达式不匹配输入的任何部分，则结果数组只有一个元素，即此字符串。
     *
     * <p> 如果在字符串的开头有一个正宽度匹配，则在结果数组的开头包含一个空的前导子串。然而，开头的零宽度匹配永远不会产生这样的空前导子串。
     *
     * <p> {@code limit} 参数控制模式应用的次数，因此影响结果数组的长度。如果限制 <i>n</i> 大于零，则模式最多应用 <i>n</i>&nbsp;-&nbsp;1 次，数组的长度不会大于 <i>n</i>，数组的最后一个条目将包含最后一个匹配分隔符之后的所有输入。如果 <i>n</i> 非正，则模式将尽可能多地应用，数组可以有任意长度。如果 <i>n</i> 为零，则模式将尽可能多地应用，数组可以有任意长度，尾随的空字符串将被丢弃。
     *
     * <p> 例如，字符串 {@code "boo:and:foo"} 与这些参数的分割结果如下：
     *
     * <blockquote><table cellpadding=1 cellspacing=0 summary="Split example showing regex, limit, and result">
     * <tr>
     *     <th>Regex</th>
     *     <th>Limit</th>
     *     <th>Result</th>
     * </tr>
     * <tr><td align=center>:</td>
     *     <td align=center>2</td>
     *     <td>{@code { "boo", "and:foo" }}</td></tr>
     * <tr><td align=center>:</td>
     *     <td align=center>5</td>
     *     <td>{@code { "boo", "and", "foo" }}</td></tr>
     * <tr><td align=center>:</td>
     *     <td align=center>-2</td>
     *     <td>{@code { "boo", "and", "foo" }}</td></tr>
     * <tr><td align=center>o</td>
     *     <td align=center>5</td>
     *     <td>{@code { "b", "", ":and:f", "", "" }}</td></tr>
     * <tr><td align=center>o</td>
     *     <td align=center>-2</td>
     *     <td>{@code { "b", "", ":and:f", "", "" }}</td></tr>
     * <tr><td align=center>o</td>
     *     <td align=center>0</td>
     *     <td>{@code { "b", "", ":and:f" }}</td></tr>
     * </table></blockquote>
     *
     * <p> 该方法的调用形式为
     * <i>str.</i>{@code split(}<i>regex</i>{@code ,}&nbsp;<i>n</i>{@code )}
     * 产生的结果与以下表达式完全相同
     *
     * <blockquote>
     * <code>
     * {@link java.util.regex.Pattern}.{@link
     * java.util.regex.Pattern#compile compile}(<i>regex</i>).{@link
     * java.util.regex.Pattern#split(java.lang.CharSequence,int) split}(<i>str</i>,&nbsp;<i>n</i>)
     * </code>
     * </blockquote>
     *
     *
     * @param  regex
     *         分隔的正则表达式
     *
     * @param  limit
     *         结果阈值，如上所述
     *
     * @return  由分割此字符串得到的字符串数组
     *
     * @throws  PatternSyntaxException
     *          如果正则表达式的语法无效
     *
     * @see java.util.regex.Pattern
     *
     * @since 1.4
     * @spec JSR-51
     */
    public String[] split(String regex, int limit) {
        /* 如果正则表达式是
         (1)一个字符的字符串且该字符不是正则表达式的元字符 ".$|()[{^?*+\\", 或
         (2)两个字符的字符串且第一个字符是反斜杠，第二个字符不是 ASCII 数字或 ASCII 字母。
         */
        char ch = 0;
        if (((regex.value.length == 1 &&
             ".$|()[{^?*+\\".indexOf(ch = regex.charAt(0)) == -1) ||
             (regex.length() == 2 &&
              regex.charAt(0) == '\\' &&
              (((ch = regex.charAt(1))-'0')|('9'-ch)) < 0 &&
              ((ch-'a')|('z'-ch)) < 0 &&
              ((ch-'A')|('Z'-ch)) < 0)) &&
            (ch < Character.MIN_HIGH_SURROGATE ||
             ch > Character.MAX_LOW_SURROGATE))
        {
            int off = 0;
            int next = 0;
            boolean limited = limit > 0;
            ArrayList<String> list = new ArrayList<>();
            while ((next = indexOf(ch, off)) != -1) {
                if (!limited || list.size() < limit - 1) {
                    list.add(substring(off, next));
                    off = next + 1;
                } else {    // 最后一个
                    //assert (list.size() == limit - 1);
                    list.add(substring(off, value.length));
                    off = value.length;
                    break;
                }
            }
            // 如果没有找到匹配项，返回此字符串
            if (off == 0)
                return new String[]{this};

            // 添加剩余段
            if (!limited || list.size() < limit)
                list.add(substring(off, value.length));

            // 构建结果
            int resultSize = list.size();
            if (limit == 0) {
                while (resultSize > 0 && list.get(resultSize - 1).length() == 0) {
                    resultSize--;
                }
            }
            String[] result = new String[resultSize];
            return list.subList(0, resultSize).toArray(result);
        }
        return Pattern.compile(regex).split(this, limit);
    }

    /**
     * 按照给定的<a
     * href="../util/regex/Pattern.html#sum">正则表达式</a>匹配分割此字符串。
     *
     * <p> 该方法的工作方式类似于调用带有给定表达式和零限制参数的两个参数的 {@link
     * #split(String, int) split} 方法。因此，结果数组中不包括尾随的空字符串。
     *
     * <p> 例如，字符串 {@code "boo:and:foo"} 与这些表达式的分割结果如下：
     *
     * <blockquote><table cellpadding=1 cellspacing=0 summary="Split examples showing regex and result">
     * <tr>
     *  <th>Regex</th>
     *  <th>Result</th>
     * </tr>
     * <tr><td align=center>:</td>
     *     <td>{@code { "boo", "and", "foo" }}</td></tr>
     * <tr><td align=center>o</td>
     *     <td>{@code { "b", "", ":and:f" }}</td></tr>
     * </table></blockquote>
     *
     *
     * @param  regex
     *         分隔的正则表达式
     *
     * @return  由分割此字符串得到的字符串数组
     *
     * @throws  PatternSyntaxException
     *          如果正则表达式的语法无效
     *
     * @see java.util.regex.Pattern
     *
     * @since 1.4
     * @spec JSR-51
     */
    public String[] split(String regex) {
        return split(regex, 0);
    }

    /**
     * 返回一个由 {@code CharSequence elements} 的副本组成的新字符串，这些元素通过指定的 {@code delimiter} 连接在一起。
     *
     * <blockquote>例如，
     * <pre>{@code
     *     String message = String.join("-", "Java", "is", "cool");
     *     // message 返回的是: "Java-is-cool"
     * }</pre></blockquote>
     *
     * 注意，如果元素为 null，则添加 {@code "null"}。
     *
     * @param  delimiter 分隔每个元素的分隔符
     * @param  elements 要连接在一起的元素
     *
     * @return 一个由 {@code elements} 组成的新 {@code String}，元素之间用 {@code delimiter} 分隔
     *
     * @throws NullPointerException 如果 {@code delimiter} 或 {@code elements} 为 {@code null}
     *
     * @see java.util.StringJoiner
     * @since 1.8
     */
    public static String join(CharSequence delimiter, CharSequence... elements) {
        Objects.requireNonNull(delimiter);
        Objects.requireNonNull(elements);
        // 元素数量不太可能值得使用 Arrays.stream 的开销。
        StringJoiner joiner = new StringJoiner(delimiter);
        for (CharSequence cs: elements) {
            joiner.add(cs);
        }
        return joiner.toString();
    }

    /**
     * 返回一个由 {@code CharSequence elements} 的副本组成的新 {@code String}，这些元素通过指定的 {@code delimiter} 连接在一起。
     *
     * <blockquote>例如，
     * <pre>{@code
     *     List<String> strings = new LinkedList<>();
     *     strings.add("Java");strings.add("is");
     *     strings.add("cool");
     *     String message = String.join(" ", strings);
     *     // message 返回的是: "Java is cool"
     *
     *     Set<String> strings = new LinkedHashSet<>();
     *     strings.add("Java"); strings.add("is");
     *     strings.add("very"); strings.add("cool");
     *     String message = String.join("-", strings);
     *     // message 返回的是: "Java-is-very-cool"
     * }</pre></blockquote>
     *
     * 注意，如果单个元素为 {@code null}，则添加 {@code "null"}。
     *
     * @param  delimiter 用于分隔 {@code elements} 中每个元素的字符序列
     * @param  elements 一个 {@code Iterable}，其 {@code elements} 将被连接在一起。
     *
     * @return 一个由 {@code elements} 参数组成的新的 {@code String}
     *
     * @throws NullPointerException 如果 {@code delimiter} 或 {@code elements} 为 {@code null}
     *
     * @see    #join(CharSequence,CharSequence...)
     * @see    java.util.StringJoiner
     * @since 1.8
     */
    public static String join(CharSequence delimiter,
            Iterable<? extends CharSequence> elements) {
        Objects.requireNonNull(delimiter);
        Objects.requireNonNull(elements);
        StringJoiner joiner = new StringJoiner(delimiter);
        for (CharSequence cs: elements) {
            joiner.add(cs);
        }
        return joiner.toString();
    }


                /**
     * 将此 {@code String} 中的所有字符转换为给定 {@code Locale} 的规则下的小写。大小写映射基于
     * {@link java.lang.Character Character} 类指定的 Unicode 标准版本。由于大小写映射不总是 1:1 的字符映射，
     * 因此转换后的 {@code String} 可能与原始 {@code String} 的长度不同。
     * <p>
     * 小写映射的例子如下表所示：
     * <table border="1" summary="Lowercase mapping examples showing language code of locale, upper case, lower case, and description">
     * <tr>
     *   <th>Locale 的语言代码</th>
     *   <th>大写</th>
     *   <th>小写</th>
     *   <th>描述</th>
     * </tr>
     * <tr>
     *   <td>tr (土耳其语)</td>
     *   <td>&#92;u0130</td>
     *   <td>&#92;u0069</td>
     *   <td>带点大写字母 I -&gt; 小写字母 i</td>
     * </tr>
     * <tr>
     *   <td>tr (土耳其语)</td>
     *   <td>&#92;u0049</td>
     *   <td>&#92;u0131</td>
     *   <td>大写字母 I -&gt; 无点小写字母 i</td>
     * </tr>
     * <tr>
     *   <td>(所有)</td>
     *   <td>French Fries</td>
     *   <td>french fries</td>
     *   <td>将字符串中的所有字符转换为小写</td>
     * </tr>
     * <tr>
     *   <td>(所有)</td>
     *   <td><img src="doc-files/capiota.gif" alt="capiota"><img src="doc-files/capchi.gif" alt="capchi">
     *       <img src="doc-files/captheta.gif" alt="captheta"><img src="doc-files/capupsil.gif" alt="capupsil">
     *       <img src="doc-files/capsigma.gif" alt="capsigma"></td>
     *   <td><img src="doc-files/iota.gif" alt="iota"><img src="doc-files/chi.gif" alt="chi">
     *       <img src="doc-files/theta.gif" alt="theta"><img src="doc-files/upsilon.gif" alt="upsilon">
     *       <img src="doc-files/sigma1.gif" alt="sigma"></td>
     *   <td>将字符串中的所有字符转换为小写</td>
     * </tr>
     * </table>
     *
     * @param locale 使用此 locale 的大小写转换规则
     * @return 转换为小写的 {@code String}。
     * @see     java.lang.String#toLowerCase()
     * @see     java.lang.String#toUpperCase()
     * @see     java.lang.String#toUpperCase(Locale)
     * @since   1.1
     */
    public String toLowerCase(Locale locale) {
        if (locale == null) {
            throw new NullPointerException();
        }

        int firstUpper;
        final int len = value.length;

        /* 现在检查是否有需要更改的字符。 */
        scan: {
            for (firstUpper = 0 ; firstUpper < len; ) {
                char c = value[firstUpper];
                if ((c >= Character.MIN_HIGH_SURROGATE)
                        && (c <= Character.MAX_HIGH_SURROGATE)) {
                    int supplChar = codePointAt(firstUpper);
                    if (supplChar != Character.toLowerCase(supplChar)) {
                        break scan;
                    }
                    firstUpper += Character.charCount(supplChar);
                } else {
                    if (c != Character.toLowerCase(c)) {
                        break scan;
                    }
                    firstUpper++;
                }
            }
            return this;
        }

        char[] result = new char[len];
        int resultOffset = 0;  /* result 可能会增长，因此 i+resultOffset
                                * 是 result 中的写入位置 */

        /* 复制前几个小写字符。 */
        System.arraycopy(value, 0, result, 0, firstUpper);

        String lang = locale.getLanguage();
        boolean localeDependent =
                (lang == "tr" || lang == "az" || lang == "lt");
        char[] lowerCharArray;
        int lowerChar;
        int srcChar;
        int srcCount;
        for (int i = firstUpper; i < len; i += srcCount) {
            srcChar = (int)value[i];
            if ((char)srcChar >= Character.MIN_HIGH_SURROGATE
                    && (char)srcChar <= Character.MAX_HIGH_SURROGATE) {
                srcChar = codePointAt(i);
                srcCount = Character.charCount(srcChar);
            } else {
                srcCount = 1;
            }
            if (localeDependent ||
                srcChar == '\u03A3' || // 希腊大写字母 SIGMA
                srcChar == '\u0130') { // 带点拉丁大写字母 I
                lowerChar = ConditionalSpecialCasing.toLowerCaseEx(this, i, locale);
            } else {
                lowerChar = Character.toLowerCase(srcChar);
            }
            if ((lowerChar == Character.ERROR)
                    || (lowerChar >= Character.MIN_SUPPLEMENTARY_CODE_POINT)) {
                if (lowerChar == Character.ERROR) {
                    lowerCharArray =
                            ConditionalSpecialCasing.toLowerCaseCharArray(this, i, locale);
                } else if (srcCount == 2) {
                    resultOffset += Character.toChars(lowerChar, result, i + resultOffset) - srcCount;
                    continue;
                } else {
                    lowerCharArray = Character.toChars(lowerChar);
                }

                /* 如果需要，扩展 result */
                int mapLen = lowerCharArray.length;
                if (mapLen > srcCount) {
                    char[] result2 = new char[result.length + mapLen - srcCount];
                    System.arraycopy(result, 0, result2, 0, i + resultOffset);
                    result = result2;
                }
                for (int x = 0; x < mapLen; ++x) {
                    result[i + resultOffset + x] = lowerCharArray[x];
                }
                resultOffset += (mapLen - srcCount);
            } else {
                result[i + resultOffset] = (char)lowerChar;
            }
        }
        return new String(result, 0, len + resultOffset);
    }

    /**
     * 使用默认 locale 的规则将此 {@code String} 中的所有字符转换为小写。这相当于调用
     * {@code toLowerCase(Locale.getDefault())}。
     * <p>
     * <b>注意：</b>此方法是 locale 敏感的，如果用于需要 locale 独立解释的字符串，可能会产生意外结果。
     * 例如编程语言标识符、协议键和 HTML 标签。
     * 例如，在土耳其 locale 中调用 {@code "TITLE".toLowerCase()} 会返回 {@code "t\u005Cu0131tle"}，其中 '\u005Cu0131' 是
     * 无点小写字母 i 字符。
     * 要为 locale 独立的字符串获得正确结果，请使用 {@code toLowerCase(Locale.ROOT)}。
     * <p>
     * @return 转换为小写的 {@code String}。
     * @see     java.lang.String#toLowerCase(Locale)
     */
    public String toLowerCase() {
        return toLowerCase(Locale.getDefault());
    }

    /**
     * 使用给定 {@code Locale} 的规则将此 {@code String} 中的所有字符转换为大写。大小写映射基于
     * {@link java.lang.Character Character} 类指定的 Unicode 标准版本。由于大小写映射不总是 1:1 的字符映射，
     * 因此转换后的 {@code String} 可能与原始 {@code String} 的长度不同。
     * <p>
     * 以下表格展示了 locale 敏感和 1:M 大小写映射的例子。
     *
     * <table border="1" summary="Examples of locale-sensitive and 1:M case mappings. Shows Language code of locale, lower case, upper case, and description.">
     * <tr>
     *   <th>Locale 的语言代码</th>
     *   <th>小写</th>
     *   <th>大写</th>
     *   <th>描述</th>
     * </tr>
     * <tr>
     *   <td>tr (土耳其语)</td>
     *   <td>&#92;u0069</td>
     *   <td>&#92;u0130</td>
     *   <td>小写字母 i -&gt; 带点大写字母 I</td>
     * </tr>
     * <tr>
     *   <td>tr (土耳其语)</td>
     *   <td>&#92;u0131</td>
     *   <td>&#92;u0049</td>
     *   <td>无点小写字母 i -&gt; 大写字母 I</td>
     * </tr>
     * <tr>
     *   <td>(所有)</td>
     *   <td>&#92;u00df</td>
     *   <td>&#92;u0053 &#92;u0053</td>
     *   <td>小写字母 sharp s -&gt; 两个字母: SS</td>
     * </tr>
     * <tr>
     *   <td>(所有)</td>
     *   <td>Fahrvergn&uuml;gen</td>
     *   <td>FAHRVERGN&Uuml;GEN</td>
     *   <td></td>
     * </tr>
     * </table>
     * @param locale 使用此 locale 的大小写转换规则
     * @return 转换为大写的 {@code String}。
     * @see     java.lang.String#toUpperCase()
     * @see     java.lang.String#toLowerCase()
     * @see     java.lang.String#toLowerCase(Locale)
     * @since   1.1
     */
    public String toUpperCase(Locale locale) {
        if (locale == null) {
            throw new NullPointerException();
        }

        int firstLower;
        final int len = value.length;

        /* 现在检查是否有需要更改的字符。 */
        scan: {
            for (firstLower = 0 ; firstLower < len; ) {
                int c = (int)value[firstLower];
                int srcCount;
                if ((c >= Character.MIN_HIGH_SURROGATE)
                        && (c <= Character.MAX_HIGH_SURROGATE)) {
                    c = codePointAt(firstLower);
                    srcCount = Character.charCount(c);
                } else {
                    srcCount = 1;
                }
                int upperCaseChar = Character.toUpperCaseEx(c);
                if ((upperCaseChar == Character.ERROR)
                        || (c != upperCaseChar)) {
                    break scan;
                }
                firstLower += srcCount;
            }
            return this;
        }

        /* result 可能会增长，因此 i+resultOffset 是 result 中的写入位置 */
        int resultOffset = 0;
        char[] result = new char[len]; /* 可能会增长 */

        /* 复制前几个大写字符。 */
        System.arraycopy(value, 0, result, 0, firstLower);

        String lang = locale.getLanguage();
        boolean localeDependent =
                (lang == "tr" || lang == "az" || lang == "lt");
        char[] upperCharArray;
        int upperChar;
        int srcChar;
        int srcCount;
        for (int i = firstLower; i < len; i += srcCount) {
            srcChar = (int)value[i];
            if ((char)srcChar >= Character.MIN_HIGH_SURROGATE &&
                (char)srcChar <= Character.MAX_HIGH_SURROGATE) {
                srcChar = codePointAt(i);
                srcCount = Character.charCount(srcChar);
            } else {
                srcCount = 1;
            }
            if (localeDependent) {
                upperChar = ConditionalSpecialCasing.toUpperCaseEx(this, i, locale);
            } else {
                upperChar = Character.toUpperCaseEx(srcChar);
            }
            if ((upperChar == Character.ERROR)
                    || (upperChar >= Character.MIN_SUPPLEMENTARY_CODE_POINT)) {
                if (upperChar == Character.ERROR) {
                    if (localeDependent) {
                        upperCharArray =
                                ConditionalSpecialCasing.toUpperCaseCharArray(this, i, locale);
                    } else {
                        upperCharArray = Character.toUpperCaseCharArray(srcChar);
                    }
                } else if (srcCount == 2) {
                    resultOffset += Character.toChars(upperChar, result, i + resultOffset) - srcCount;
                    continue;
                } else {
                    upperCharArray = Character.toChars(upperChar);
                }

                /* 如果需要，扩展 result */
                int mapLen = upperCharArray.length;
                if (mapLen > srcCount) {
                    char[] result2 = new char[result.length + mapLen - srcCount];
                    System.arraycopy(result, 0, result2, 0, i + resultOffset);
                    result = result2;
                }
                for (int x = 0; x < mapLen; ++x) {
                    result[i + resultOffset + x] = upperCharArray[x];
                }
                resultOffset += (mapLen - srcCount);
            } else {
                result[i + resultOffset] = (char)upperChar;
            }
        }
        return new String(result, 0, len + resultOffset);
    }

    /**
     * 使用默认 locale 的规则将此 {@code String} 中的所有字符转换为大写。此方法相当于
     * {@code toUpperCase(Locale.getDefault())}。
     * <p>
     * <b>注意：</b>此方法是 locale 敏感的，如果用于需要 locale 独立解释的字符串，可能会产生意外结果。
     * 例如编程语言标识符、协议键和 HTML 标签。
     * 例如，在土耳其 locale 中调用 {@code "title".toUpperCase()} 会返回 {@code "T\u005Cu0130TLE"}，其中 '\u005Cu0130' 是
     * 带点大写字母 I 字符。
     * 要为 locale 独立的字符串获得正确结果，请使用 {@code toUpperCase(Locale.ROOT)}。
     * <p>
     * @return 转换为大写的 {@code String}。
     * @see     java.lang.String#toUpperCase(Locale)
     */
    public String toUpperCase() {
        return toUpperCase(Locale.getDefault());
    }

    /**
     * 返回一个字符串，其值是此字符串，去除了任何前导和尾随的空白。
     * <p>
     * 如果此 {@code String} 对象表示一个空字符序列，或者此 {@code String} 对象表示的字符序列的
     * 第一个和最后一个字符的代码都大于 {@code '\u005Cu0020'}（空格字符），则返回此 {@code String} 对象的引用。
     * <p>
     * 否则，如果字符串中没有代码大于 {@code '\u005Cu0020'} 的字符，则返回一个表示空字符串的
     * {@code String} 对象。
     * <p>
     * 否则，设 <i>k</i> 是字符串中第一个代码大于 {@code '\u005Cu0020'} 的字符的索引，设
     * <i>m</i> 是字符串中最后一个代码大于 {@code '\u005Cu0020'} 的字符的索引。返回一个 {@code String}
     * 对象，表示此字符串的子字符串，从索引 <i>k</i> 开始，到索引 <i>m</i> 结束——即
     * {@code this.substring(k, m + 1)} 的结果。
     * <p>
     * 此方法可用于从字符串的开头和结尾修剪空白（如上所述）。
     *
     * @return 一个字符串，其值是此字符串，去除了任何前导和尾随的空白，或者如果此字符串没有前导或尾随空白，则返回此字符串。
     */
    public String trim() {
        int len = value.length;
        int st = 0;
        char[] val = value;    /* 避免 getfield 操作码 */

        while ((st < len) && (val[st] <= ' ')) {
            st++;
        }
        while ((st < len) && (val[len - 1] <= ' ')) {
            len--;
        }
        return ((st > 0) || (len < value.length)) ? substring(st, len) : this;
    }


                /**
     * 该对象（已经是一个字符串！）本身被返回。
     *
     * @return  字符串本身。
     */
    public String toString() {
        return this;
    }

    /**
     * 将此字符串转换为新的字符数组。
     *
     * @return  一个新分配的字符数组，其长度为该字符串的长度，内容初始化为包含
     *          该字符串表示的字符序列。
     */
    public char[] toCharArray() {
        // 不能使用 Arrays.copyOf，因为存在类初始化顺序问题
        char result[] = new char[value.length];
        System.arraycopy(value, 0, result, 0, value.length);
        return result;
    }

    /**
     * 使用指定的格式字符串和参数返回格式化的字符串。
     *
     * <p> 使用的区域设置始终是 {@link
     * java.util.Locale#getDefault() Locale.getDefault()} 返回的区域设置。
     *
     * @param  format
     *         一个 <a href="../util/Formatter.html#syntax">格式字符串</a>
     *
     * @param  args
     *         由格式字符串中的格式说明符引用的参数。如果参数多于格式说明符，则多余的参数将被忽略。参数的数量是可变的，可以为零。参数的最大数量受
     *         <cite>The Java&trade; Virtual Machine Specification</cite> 定义的 Java 数组的最大维度限制。
     *         对于 {@code null} 参数的行为取决于 <a
     *         href="../util/Formatter.html#syntax">转换</a>。
     *
     * @throws  java.util.IllegalFormatException
     *          如果格式字符串包含非法语法，格式说明符与给定参数不兼容，格式字符串的参数不足，或其他非法条件。有关所有可能的格式错误的详细说明，请参见
     *          格式化器类规范的 <a
     *          href="../util/Formatter.html#detail">详情</a> 部分。
     *
     * @return  一个格式化的字符串
     *
     * @see  java.util.Formatter
     * @since  1.5
     */
    public static String format(String format, Object... args) {
        return new Formatter().format(format, args).toString();
    }

    /**
     * 使用指定的区域设置、格式字符串和参数返回格式化的字符串。
     *
     * @param  l
     *         在格式化过程中应用的 {@linkplain java.util.Locale 区域设置}。如果 {@code l} 为 {@code null}，则不应用区域设置。
     *
     * @param  format
     *         一个 <a href="../util/Formatter.html#syntax">格式字符串</a>
     *
     * @param  args
     *         由格式字符串中的格式说明符引用的参数。如果参数多于格式说明符，则多余的参数将被忽略。参数的数量是可变的，可以为零。参数的最大数量受
     *         <cite>The Java&trade; Virtual Machine Specification</cite> 定义的 Java 数组的最大维度限制。
     *         对于 {@code null} 参数的行为取决于 <a
     *         href="../util/Formatter.html#syntax">转换</a>。
     *
     * @throws  java.util.IllegalFormatException
     *          如果格式字符串包含非法语法，格式说明符与给定参数不兼容，格式字符串的参数不足，或其他非法条件。有关所有可能的格式错误的详细说明，请参见
     *          格式化器类规范的 <a
     *          href="../util/Formatter.html#detail">详情</a> 部分
     *
     * @return  一个格式化的字符串
     *
     * @see  java.util.Formatter
     * @since  1.5
     */
    public static String format(Locale l, String format, Object... args) {
        return new Formatter(l).format(format, args).toString();
    }

    /**
     * 返回 {@code Object} 参数的字符串表示形式。
     *
     * @param   obj   一个 {@code Object}。
     * @return  如果参数为 {@code null}，则返回一个等于 {@code "null"} 的字符串；否则，返回
     *          {@code obj.toString()} 的值。
     * @see     java.lang.Object#toString()
     */
    public static String valueOf(Object obj) {
        return (obj == null) ? "null" : obj.toString();
    }

    /**
     * 返回 {@code char} 数组参数的字符串表示形式。字符数组的内容被复制；字符数组的后续修改不会影响返回的字符串。
     *
     * @param   data     字符数组。
     * @return  包含字符数组内容的 {@code String}。
     */
    public static String valueOf(char data[]) {
        return new String(data);
    }

    /**
     * 返回 {@code char} 数组参数的特定子数组的字符串表示形式。
     * <p>
     * {@code offset} 参数是子数组的第一个字符的索引。{@code count} 参数
     * 指定子数组的长度。子数组的内容被复制；字符数组的后续修改不会影响返回的字符串。
     *
     * @param   data     字符数组。
     * @param   offset   子数组的初始偏移量。
     * @param   count    子数组的长度。
     * @return  包含字符数组的指定子数组的 {@code String}。
     * @exception IndexOutOfBoundsException 如果 {@code offset} 为
     *          负数，或 {@code count} 为负数，或
     *          {@code offset+count} 大于
     *          {@code data.length}。
     */
    public static String valueOf(char data[], int offset, int count) {
        return new String(data, offset, count);
    }

    /**
     * 等同于 {@link #valueOf(char[], int, int)}。
     *
     * @param   data     字符数组。
     * @param   offset   子数组的初始偏移量。
     * @param   count    子数组的长度。
     * @return  包含字符数组的指定子数组的 {@code String}。
     * @exception IndexOutOfBoundsException 如果 {@code offset} 为
     *          负数，或 {@code count} 为负数，或
     *          {@code offset+count} 大于
     *          {@code data.length}。
     */
    public static String copyValueOf(char data[], int offset, int count) {
        return new String(data, offset, count);
    }

    /**
     * 等同于 {@link #valueOf(char[])}。
     *
     * @param   data   字符数组。
     * @return  包含字符数组内容的 {@code String}。
     */
    public static String copyValueOf(char data[]) {
        return new String(data);
    }

    /**
     * 返回 {@code boolean} 参数的字符串表示形式。
     *
     * @param   b   一个 {@code boolean}。
     * @return  如果参数为 {@code true}，则返回一个等于
     *          {@code "true"} 的字符串；否则，返回一个等于
     *          {@code "false"} 的字符串。
     */
    public static String valueOf(boolean b) {
        return b ? "true" : "false";
    }

    /**
     * 返回 {@code char} 参数的字符串表示形式。
     *
     * @param   c   一个 {@code char}。
     * @return  长度为 {@code 1} 的字符串，其中包含单个字符参数 {@code c}。
     */
    public static String valueOf(char c) {
        char data[] = {c};
        return new String(data, true);
    }

    /**
     * 返回 {@code int} 参数的字符串表示形式。
     * <p>
     * 表示形式与 {@code Integer.toString} 方法的单参数版本返回的表示形式完全相同。
     *
     * @param   i   一个 {@code int}。
     * @return  {@code int} 参数的字符串表示形式。
     * @see     java.lang.Integer#toString(int, int)
     */
    public static String valueOf(int i) {
        return Integer.toString(i);
    }

    /**
     * 返回 {@code long} 参数的字符串表示形式。
     * <p>
     * 表示形式与 {@code Long.toString} 方法的单参数版本返回的表示形式完全相同。
     *
     * @param   l   一个 {@code long}。
     * @return  {@code long} 参数的字符串表示形式。
     * @see     java.lang.Long#toString(long)
     */
    public static String valueOf(long l) {
        return Long.toString(l);
    }

    /**
     * 返回 {@code float} 参数的字符串表示形式。
     * <p>
     * 表示形式与 {@code Float.toString} 方法的单参数版本返回的表示形式完全相同。
     *
     * @param   f   一个 {@code float}。
     * @return  {@code float} 参数的字符串表示形式。
     * @see     java.lang.Float#toString(float)
     */
    public static String valueOf(float f) {
        return Float.toString(f);
    }

    /**
     * 返回 {@code double} 参数的字符串表示形式。
     * <p>
     * 表示形式与 {@code Double.toString} 方法的单参数版本返回的表示形式完全相同。
     *
     * @param   d   一个 {@code double}。
     * @return  {@code double} 参数的字符串表示形式。
     * @see     java.lang.Double#toString(double)
     */
    public static String valueOf(double d) {
        return Double.toString(d);
    }

    /**
     * 返回字符串对象的规范表示形式。
     * <p>
     * 一个初始为空的字符串池由 {@code String} 类私有维护。
     * <p>
     * 当调用 intern 方法时，如果池中已经包含一个与该 {@code String} 对象相等的字符串（由
     * {@link #equals(Object)} 方法确定），则返回池中的字符串。否则，将此 {@code String} 对象添加到池中，并返回此 {@code String} 对象的引用。
     * <p>
     * 因此，对于任何两个字符串 {@code s} 和 {@code t}，如果 {@code s.equals(t)} 为 {@code true}，则 {@code s.intern() == t.intern()} 也为 {@code true}。
     * <p>
     * 所有字符串字面量和字符串值常量表达式都被 intern。字符串字面量在 <cite>The Java&trade; Language Specification</cite> 的第 3.10.5 节中定义。
     *
     * @return  一个字符串，其内容与该字符串相同，但保证来自一个唯一的字符串池。
     */
    public native String intern();
}
