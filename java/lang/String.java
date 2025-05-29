/*
 * Copyright (c) 1994, 2020, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 版权所有 (c) 1994, 2020, Oracle 及其关联公司。保留所有权利。
 * Oracle 专有/机密。使用受许可条款限制。
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
 * {@code String} 类表示字符字符串。Java 程序中的所有字符串字面量（如 {@code "abc"}）都被实现为此类的实例。
 * <p>
 * 字符串是常量；它们的值在创建后无法更改。字符串缓冲区支持可变字符串。
 * 由于字符串对象是不可变的，它们可以被共享。例如：
 * <blockquote><pre>
 *     String str = "abc";
 * </pre></blockquote><p>
 * 等价于：
 * <blockquote><pre>
 *     char data[] = {'a', 'b', 'c'};
 *     String str = new String(data);
 * </pre></blockquote><p>
 * 以下是字符串使用的一些示例：
 * <blockquote><pre>
 *     System.out.println("abc");
 *     String cde = "cde";
 *     System.out.println("abc" + cde);
 *     String c = "abc".substring(2,3);
 *     String d = cde.substring(1, 2);
 * </pre></blockquote>
 * <p>
 * {@code String} 类包含用于检查字符串中单个字符、比较字符串、搜索字符串、提取子字符串以及创建字符串副本（将所有字符转换为大写或小写）的方法。大小写映射基于 {@link java.lang.Character Character} 类指定的 Unicode 标准版本。
 * <p>
 * Java 语言为字符串连接运算符（+）以及其他对象到字符串的转换提供了特殊支持。字符串连接通过 {@code StringBuilder}（或 {@code StringBuffer}）类及其 {@code append} 方法实现。
 * 字符串转换通过 {@code toString} 方法实现，该方法由 {@code Object} 定义并被 Java 中所有类继承。有关字符串连接和转换的更多信息，请参见 Gosling、Joy 和 Steele 的《Java 语言规范》。
 *
 * <p> 除非另有说明，向此类的构造函数或方法传递 <tt>null</tt> 参数将导致抛出 {@link NullPointerException}。
 *
 * <p> {@code String} 以 UTF-16 格式表示字符串，其中<em>增补字符</em>由<em>代理对</em>表示（有关更多信息，请参见 {@code Character} 类中的 <a href="Character.html#unicode">Unicode 字符表示</a> 部分）。
 * 索引值指的是 {@code char} 代码单元，因此一个增补字符在 {@code String} 中占用两个位置。
 * <p> {@code String} 类提供了处理 Unicode 代码点（即字符）的方法，以及处理 Unicode 代码单元（即 {@code char} 值）的方法。
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

    /** 为互操作性使用 JDK 1.0.2 的 serialVersionUID */
    private static final long serialVersionUID = -6849794470754667710L;

    /**
     * String 类在序列化流协议中被特殊处理。
     * String 实例根据 <a href="{@docRoot}/../platform/serialization/spec/output.html">
     * 对象序列化规范，第 6.2 节，“流元素”</a> 写入 ObjectOutputStream。
     */
    private static final ObjectStreamField[] serialPersistentFields =
        new ObjectStreamField[0];

    /**
     * 初始化一个新创建的 {@code String} 对象，使其表示一个空的字符序列。
     * 注意：由于字符串是不可变的，使用此构造函数是不必要的。
     */
    public String() {
        this.value = "".value;
    }

    /**
     * 初始化一个新创建的 {@code String} 对象，使其表示与参数相同的字符序列；换句话说，新创建的字符串是参数字符串的副本。
     * 除非需要显式复制 {@code original}，否则使用此构造函数是不必要的，因为字符串是不可变的。
     *
     * @param  original
     *         一个 {@code String}
     */
    public String(String original) {
        this.value = original.value;
        this.hash = original.hash;
    }

    /**
     * 分配一个新的 {@code String}，使其表示字符数组参数当前包含的字符序列。
     * 字符数组的内容被复制；后续对字符数组的修改不会影响新创建的字符串。
     *
     * @param  value
     *         字符串的初始值
     */
    public String(char value[]) {
        this.value = Arrays.copyOf(value, value.length);
    }

    /**
     * 分配一个新的 {@code String}，包含字符数组参数中子数组的字符。
     * {@code offset} 参数是子数组第一个字符的索引，{@code count} 参数指定子数组的长度。
     * 子数组的内容被复制；后续对字符数组的修改不会影响新创建的字符串。
     *
     * @param  value
     *         字符源数组
     *
     * @param  offset
     *         初始偏移量
     *
     * @param  count
     *         长度
     *
     * @throws  IndexOutOfBoundsException
     *          如果 {@code offset} 和 {@code count} 参数索引超出 {@code value} 数组的边界
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
     * 分配一个新的 {@code String}，包含 <a href="Character.html#unicode">Unicode 代码点</a> 数组参数中子数组的字符。
     * {@code offset} 参数是子数组第一个代码点的索引，{@code count} 参数指定子数组的长度。
     * 子数组的内容被转换为 {@code char}；后续对 {@code int} 数组的修改不会影响新创建的字符串。
     *
     * @param  codePoints
     *         Unicode 代码点源数组
     *
     * @param  offset
     *         初始偏移量
     *
     * @param  count
     *         长度
     *
     * @throws  IllegalArgumentException
     *          如果在 {@code codePoints} 中发现任何无效的 Unicode 代码点
     *
     * @throws  IndexOutOfBoundsException
     *          如果 {@code offset} 和 {@code count} 参数索引超出 {@code codePoints} 数组的边界
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

        // 第一遍：计算 char[] 的精确大小
        int n = count;
        for (int i = offset; i < end; i++) {
            int c = codePoints[i];
            if (Character.isBmpCodePoint(c))
                continue;
            else if (Character.isValidCodePoint(c))
                n++;
            else throw new IllegalArgumentException(Integer.toString(c));
        }

        // 第二遍：分配并填充 char[]
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
     * 从 8 位整数值数组的子数组构造一个新的 {@code String}。
     *
     * <p> {@code offset} 参数是子数组第一个字节的索引，{@code count} 参数指定子数组的长度。
     *
     * <p> 子数组中的每个 {@code byte} 按上述方法转换为 {@code char}。
     *
     * @deprecated 此方法无法正确将字节转换为字符。从 JDK 1.1 开始，推荐使用接受 {@link java.nio.charset.Charset}、字符集名称或平台默认字符集的 {@code String} 构造函数。
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
     * 从 8 位整数值数组构造包含字符的新的 {@code String}。结果字符串中的每个字符 <i>c</i> 根据字节数组中对应的 <i>b</i> 构造，如下：
     *
     * <blockquote><pre>
     *     <b><i>c</i></b> == (char)(((hibyte & 0xff) << 8)
     *                         | (<b><i>b</i></b> & 0xff))
     * </pre></blockquote>
     *
     * @deprecated 此方法无法正确将字节转换为字符。从 JDK 1.1 开始，推荐使用接受 {@link java.nio.charset.Charset}、字符集名称或平台默认字符集的 {@code String} 构造函数。
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

    /* 用于检查 String(byte[],...) 构造函数使用的字节数组及请求的偏移量和长度值的边界检查的通用私有工具方法。 */
    private static void checkBounds(byte[] bytes, int offset, int length) {
        if (length < 0)
            throw new StringIndexOutOfBoundsException(length);
        if (offset < 0)
            throw new StringIndexOutOfBoundsException(offset);
        if (offset > bytes.length - length)
            throw new StringIndexOutOfBoundsException(offset + length);
    }

    /**
     * 使用指定的字符集解码指定的字节子数组，构造一个新的 {@code String}。新 {@code String} 的长度取决于字符集，因此可能不等于子数组的长度。
     *
     * <p> 当给定字节在指定字符集中无效时，此构造函数的行为未定义。需要更多控制解码过程时，应使用 {@link java.nio.charset.CharsetDecoder} 类。
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
     * @param  charsetName
     *         支持的 {@linkplain java.nio.charset.Charset 字符集} 的名称
     *
     * @throws  UnsupportedEncodingException
     *          如果指定的字符集不受支持
     *
     * @throws  IndexOutOfBoundsException
     *          如果 {@code offset} 和 {@code length} 参数索引超出 {@code bytes} 数组的边界
     *
     * @since  JDK1.1
     */
    public String(byte bytes[], int offset, int length, String charsetName)
            throws UnsupportedEncodingException {
        if (charsetName == null)
            throw new NullPointerException("charsetName");
        checkBounds(bytes, offset, length);
        this.value = StringCoding.decode(charsetName, bytes, offset, length);
    }

    /**
     * 使用指定的 {@linkplain java.nio.charset.Charset 字符集} 解码指定的字节子数组，构造一个新的 {@code String}。
     * 新 {@code String} 的长度取决于字符集，因此可能不等于子数组的长度。
     *
     * <p> 此方法始终将格式错误的输入和不可映射的字符序列替换为此字符集的默认替换字符串。需要更多控制解码过程时，应使用 {@link java.nio.charset.CharsetDecoder} 类。
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
     *         用于解码 {@code bytes} 的 {@linkplain java.nio.charset.Charset 字符集}
     *
     * @throws  IndexOutOfBoundsException
     *          如果 {@code offset} 和 {@code length} 参数索引超出 {@code bytes} 数组的边界
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
     * 使用指定的 {@linkplain java.nio.charset.Charset 字符集} 解码指定的字节数组，构造一个新的 {@code String}。
     * 新 {@code String} 的长度取决于字符集，因此可能不等于字节数组的长度。
     *
     * <p> 当给定字节在指定字符集中无效时，此构造函数的行为未定义。需要更多控制解码过程时，应使用 {@link java.nio.charset.CharsetDecoder} 类。
     *
     * @param  bytes
     *         要解码为字符的字节
     *
     * @param  charsetName
     *         支持的 {@linkplain java.nio.charset.Charset 字符集} 的名称
     *
     * @throws  UnsupportedEncodingException
     *          如果指定的字符集不受支持
     *
     * @since  JDK1.1
     */
    public String(byte bytes[], String charsetName)
            throws UnsupportedEncodingException {
        this(bytes, 0, bytes.length, charsetName);
    }

    /**
     * 使用指定的 {@linkplain java.nio.charset.Charset 字符集} 解码指定的字节数组，构造一个新的 {@code String}。
     * 新 {@code String} 的长度取决于字符集，因此可能不等于字节数组的长度。
     *
     * <p> 此方法始终将格式错误的输入和不可映射的字符序列替换为此字符集的默认替换字符串。需要更多控制解码过程时，应使用 {@link java.nio.charset.CharsetDecoder} 类。
     *
     * @param  bytes
     *         要解码为字符的字节
     *
     * @param  charset
     *         用于解码 {@code bytes} 的 {@linkplain java.nio.charset.Charset 字符集}
     *
     * @since  1.6
     */
    public String(byte bytes[], Charset charset) {
        this(bytes, 0, bytes.length, charset);
    }

    /**
     * 使用平台的默认字符集解码指定的字节子数组，构造一个新的 {@code String}。
     * 新 {@code String} 的长度取决于字符集，因此可能不等于子数组的长度。
     *
     * <p> 当给定字节在默认字符集中无效时，此构造函数的行为未定义。需要更多控制解码过程时，应使用 {@link java.nio.charset.CharsetDecoder} 类。
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
     *          如果 {@code offset} 和 {@code length} 参数索引超出 {@code bytes} 数组的边界
     *
     * @since  JDK1.1
     */
    public String(byte bytes[], int offset, int length) {
        checkBounds(bytes, offset, length);
        this.value = StringCoding.decode(bytes, offset, length);
    }

    /**
     * 使用平台的默认字符集解码指定的字节数组，构造一个新的 {@code String}。
     * 新 {@code String} 的长度取决于字符集，因此可能不等于字节数组的长度。
     *
     * <p> 当给定字节在默认字符集中无效时，此构造函数的行为未定义。需要更多控制解码过程时，应使用 {@link java.nio.charset.CharsetDecoder} 类。
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
     * 分配一个新的字符串，包含字符串缓冲区参数当前包含的字符序列。
     * 字符串缓冲区的内容被复制；后续对字符串缓冲区的修改不会影响新创建的字符串。
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
     * 分配一个新的字符串，包含字符串构建器参数当前包含的字符序列。
     * 字符串构建器的内容被复制；后续对字符串构建器的修改不会影响新创建的字符串。
     *
     * <p> 提供此构造函数是为了便于迁移到 {@code StringBuilder}。通过 {@code toString} 方法从字符串构建器获取字符串通常运行更快，通常是首选方式。
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
     * 包私有构造函数，为了速度共享值数组。
     * 此构造函数总是期望以 share==true 调用。
     * 需要一个单独的构造函数，因为我们已经有一个公共的 String(char[]) 构造函数，它会复制给定的 char[]。
     */
    String(char[] value, boolean share) {
        // assert share : "不支持非共享";
        this.value = value;
    }

    /**
     * 返回此字符串的长度。
     * 长度等于字符串中的 <a href="Character.html#unicode">Unicode 代码单元</a> 的数量。
     *
     * @return  此对象表示的字符序列的长度。
     */
    public int length() {
        return value.length;
    }

    /**
     * 仅当 {@link #length()} 为 {@code 0} 时返回 {@code true}。
     *
     * @return 如果 {@link #length()} 为 {@code 0}，则返回 {@code true}，否则返回 {@code false}
     *
     * @since 1.6
     */
    public boolean isEmpty() {
        return value.length == 0;
    }

    /**
     * 返回指定索引处的 {@code char} 值。索引范围从 {@code 0} 到 {@code length() - 1}。
     * 序列的第一个 {@code char} 值位于索引 {@code 0}，下一个位于索引 {@code 1}，依此类推，如同数组索引。
     *
     * <p>如果索引指定的 {@code char} 值是 <a href="Character.html#unicode">代理</a>，则返回代理值。
     *
     * @param      index   {@code char} 值的索引。
     * @return     此字符串指定索引处的 {@code char} 值。第一个 {@code char} 值位于索引 {@code 0}。
     * @exception  IndexOutOfBoundsException  如果 {@code index} 参数为负或不小于此字符串的长度。
     */
    public char charAt(int index) {
        if ((index < 0) || (index >= value.length)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return value[index];
    }

    /**
     * 返回指定索引处的字符（Unicode 代码点）。索引指的是 {@code char} 值（Unicode 代码单元），范围从 {@code 0} 到 {@link #length()}{@code  - 1}。
     *
     * <p> 如果给定索引处的 {@code char} 值在高代理范围内，接下来的索引小于此 {@code String} 的长度，并且接下来的索引处的 {@code char} 值在低代理范围内，则返回此代理对对应的增补代码点。
     * 否则，返回给定索引处的 {@code char} 值。
     *
     * @param      index 指向 {@code char} 值的索引
     * @return     指定 {@code index} 处的代码点值
     * @exception  IndexOutOfBoundsException  如果 {@code index} 参数为负或不小于此字符串的长度。
     * @since      1.5
     */
    public int codePointAt(int index) {
        if ((index < 0) || (index >= value.length)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return Character.codePointAtImpl(value, index, value.length);
    }

    /**
     * 返回指定索引之前的字符（Unicode 代码点）。索引指的是 {@code char} 值（Unicode 代码单元），范围从 {@code 1} 到 {@link CharSequence#length() length}。
     *
     * <p> 如果 {@code (index - 1)} 处的 {@code char} 值在低代理范围内，{@code (index - 2)} 不为负，并且 {@code (index - 2)} 处的 {@code char} 值在高代理范围内，则返回此代理对的增补代码点值。
     * 如果 {@code index - 1} 处的 {@code char} 值是未配对的低代理或高代理，则返回代理值。
     *
     * @param     index 要返回的代码点之后的索引
     * @return    给定索引之前的 Unicode 代码点值。
     * @exception IndexOutOfBoundsException 如果 {@code index} 参数小于 1 或大于此字符串的长度。
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
     * 返回此 {@code String} 指定文本范围内 Unicode 代码点的数量。文本范围从指定的 {@code beginIndex} 开始，扩展到索引 {@code endIndex - 1} 处的 {@code char}。
     * 因此，文本范围的长度（以 {@code char} 为单位）为 {@code endIndex-beginIndex}。文本范围内的未配对代理每个计为一个代码点。
     *
     * @param beginIndex 文本范围第一个 {@code char} 的索引。
     * @param endIndex 文本范围最后一个 {@code char} 之后的索引。
     * @return 指定文本范围内的 Unicode 代码点数量
     * @exception IndexOutOfBoundsException 如果 {@code beginIndex} 为负，或 {@code endIndex} 大于此 {@code String} 的长度，或 {@code beginIndex} 大于 {@code endIndex}。
     * @since  1.5
     */
    public int codePointCount(int beginIndex, int endIndex) {
        if (beginIndex < 0 || endIndex > value.length || beginIndex > endIndex) {
            throw new IndexOutOfBoundsException();
        }
        return Character.codePointCountImpl(value, beginIndex, endIndex - beginIndex);
    }

    /**
     * 返回此 {@code String} 中从给定 {@code index} 偏移 {@code codePointOffset} 个代码点的索引。给定由 {@code index} 和 {@code codePointOffset} 的文本范围内的未配对代理每个计为一个代码点。
     *
     * @param index 要偏移的索引
     * @param codePointOffset 代码点的偏移量
     * @return 此 {@code String} 中的索引
     * @exception IndexOutOfBoundsException 如果 {@code index} 为负或大于此 {@code String} 的长度，或者如果 {@code codePointOffset} 为正且从 {@code index} 开始的子字符串少于 {@code codePointOffset} 个代码点，或者如果 {@code codePointOffset} 为负且 {@code index} 之前的子字符串少于 {@code codePointOffset} 的绝对值的代码点。
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
     * 将此字符串的字符复制到目标数组 dst，从 dstBegin 开始。
     * 此方法不执行任何范围检查。
     */
    void getChars(char dst[], int dstBegin) {
        System.arraycopy(value, 0, dst, dstBegin, value.length);
    }

    /**
     * 将此字符串的字符复制到目标字符数组中。
     * <p>
     * 要复制的第一个字符位于索引 {@code srcBegin}；最后一个字符位于索引 {@code srcEnd-1}（因此要复制的字符总数为 {@code srcEnd-srcBegin}）。
     * 字符被复制到 {@code dst} 的子数组中，从索引 {@code dstBegin} 开始，到索引：
     * <blockquote><pre>
     *     dstBegin + (srcEnd-srcBegin) - 1
     * </pre></blockquote>
     *
     * @param      srcBegin   要复制的字符串中第一个字符的索引。
     * @param      srcEnd     要复制的字符串中最后一个字符之后的索引。
     * @param      dst        目标数组。
     * @param      dstBegin   目标数组中的起始偏移量。
     * @exception IndexOutOfBoundsException 如果以下任一条件为真：
     *            <ul><li>{@code srcBegin} 为负。
     *            <li>{@code srcBegin} 大于 {@code srcEnd}
     *            <li>{@code srcEnd} 大于此字符串的长度
     *            <li>{@code dstBegin} 为负
     *            <li>{@code dstBegin+(srcEnd-srcBegin)} 大于 {@code dst.length}</ul>
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
     * 将此字符串的字符复制到目标字节数组中。每个字节接收对应字符的低 8 位。每个字符的高 8 位不被复制，也不以任何方式参与传输。
     *
     * <p> 要复制的第一个字符位于索引 {@code srcBegin}；最后一个字符位于索引 {@code srcEnd-1}。要复制的字符总数为 {@code srcEnd-srcBegin}。
     * 字符转换为字节后，被复制到 {@code dst} 的子数组中，从索引 {@code dstBegin} 开始，到索引：
     *
     * <blockquote><pre>
     *     dstBegin + (srcEnd-srcBegin) - 1
     * </pre></blockquote>
     *
     * @deprecated 此方法无法正确将字符转换为字节。从 JDK 1.1 开始，推荐使用 {@link #getBytes()} 方法，它使用平台的默认字符集。
     *
     * @param  srcBegin
     *         要复制的字符串中第一个字符的索引
     *
     * @param  srcEnd
     *         要复制的字符串中最后一个字符之后的索引
     *
     * @param  dst
     *         目标数组
     *
     * @param  dstBegin
     *         目标数组中的起始偏移量
     *
     * @throws  IndexOutOfBoundsException
     *          如果以下任一条件为真：
     *          <ul>
     *            <li> {@code srcBegin} 为负
     *            <li> {@code srcBegin} 大于 {@code srcEnd}
     *            <li> {@code srcEnd} 大于此字符串的长度
     *            <li> {@code dstBegin} 为负
     *            <li> {@code dstBegin+(srcEnd-srcBegin)} 大于 {@code dst.length}
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
     * 使用指定的字符集将此 {@code String} 编码为字节序列，将结果存储到新的字节数组中。
     *
     * <p> 当此字符串无法在给定字符集中编码时，此方法的行为未定义。需要更多控制编码过程时，应使用 {@link java.nio.charset.CharsetEncoder} 类。
     *
     * @param  charsetName
     *         支持的 {@linkplain java.nio.charset.Charset 字符集} 的名称
     *
     * @return  结果字节数组
     *
     * @throws  UnsupportedEncodingException
     *          如果指定的字符集不受支持
     *
     * @since  JDK1.1
     */
    public byte[] getBytes(String charsetName)
            throws UnsupportedEncodingException {
        if (charsetName == null) throw new NullPointerException();
        return StringCoding.encode(charsetName, value, 0, value.length);
    }

    /**
     * 使用给定的 {@linkplain java.nio.charset.Charset 字符集} 将此 {@code String} 编码为字节序列，将结果存储到新的字节数组中。
     *
     * <p> 此方法始终将格式错误的输入和不可映射的字符序列替换为此字符集的默认替换字节数组。需要更多控制编码过程时，应使用 {@link java.nio.charset.CharsetEncoder} 类。
     *
     * @param  charset
     *         用于编码 {@code String} 的 {@linkplain java.nio.charset.Charset 字符集}
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
     * 使用平台的默认字符集将此 {@code String} 编码为字节序列，将结果存储到新的字节数组中。
     *
     * <p> 当此字符串无法在默认字符集中编码时，此方法的行为未定义。需要更多控制编码过程时，应使用 {@link java.nio.charset.CharsetEncoder} 类。
     *
     * @return  结果字节数组
     *
     * @since  JDK1.1
     */
    public byte[] getBytes() {
        return StringCoding.encode(value, 0, value.length);
    }

    /**
     * 将此字符串与指定对象比较。仅当参数不为 {@code null} 且是表示与此对象相同字符序列的 {@code String} 对象时，结果为 {@code true}。
     *
     * @param  anObject
     *         要与此 {@code String} 比较的对象
     *
     * @return  如果给定对象表示与此字符串等价的 {@code String}，则返回 {@code true}；否则返回 {@code false}
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
     * 将此字符串与指定的 {@code StringBuffer} 比较。仅当此 {@code String} 表示与指定的 {@code StringBuffer} 相同的字符序列时，结果为 {@code true}。此方法对 {@code StringBuffer} 进行同步。
     *
     * @param  sb
     *         要与此 {@code String} 比较的 {@code StringBuffer}
     *
     * @return  如果此 {@code String} 表示与指定的 {@code StringBuffer} 相同的字符序列，则返回 {@code true}；否则返回 {@code false}
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
     * 将此字符串与指定的 {@code CharSequence} 比较。仅当此 {@code String} 表示与指定序列相同的 char 值序列时，结果为 {@code true}。注意，如果 {@code CharSequence} 是 {@code StringBuffer}，则此方法对其进行同步。
     *
     * @param  cs
     *         要与此 {@code String} 比较的序列
     *
     * @return  如果此 {@code String} 表示与指定序列相同的 char 值序列，则返回 {@code true}；否则返回 {@code false}
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
        // 参数是通用的 CharSequence
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
     * 将此 {@code String} 与另一个 {@code String} 进行比较，忽略大小写。两个字符串在忽略大小写的情况下被认为是相等的，当且仅当它们的长度相同，并且两个字符串中的对应字符在忽略大小写的情况下相等。
     *
     * <p> 如果以下任一条件为真，则两个字符 {@code c1} 和 {@code c2} 在忽略大小写的情况下被认为是相同的：
     * <ul>
     *   <li> 两个字符相同（通过 {@code ==} 运算符比较）
     *   <li> 对每个字符应用 {@link java.lang.Character#toUpperCase(char)} 方法产生相同的结果
     *   <li> 对每个字符应用 {@link java.lang.Character#toLowerCase(char)} 方法产生相同的结果
     * </ul>
     *
     * @param  anotherString
     *         要与此 {@code String} 比较的 {@code String}
     *
     * @return  如果参数不为 {@code null} 且表示与此字符串忽略大小写等价的 {@code String}，则返回 {@code true}；否则返回 {@code false}
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
     * 按字典序比较两个字符串。
     * 比较基于字符串中每个字符的 Unicode 值。此 {@code String} 对象表示的字符序列与参数字符串表示的字符序列按字典序比较。
     * 如果此 {@code String} 对象在字典序上先于参数字符串，则结果为负整数。如果此 {@code String} 对象在字典序上后于参数字符串，则结果为正整数。如果字符串相等，则结果为零；{@code compareTo} 返回 {@code 0} 仅当 {@link #equals(Object)} 方法返回 {@code true} 时。
     * <p>
     * 这是字典序的定义。如果两个字符串不同，则它们在某个有效索引处有不同的字符，或者它们的长度不同，或者两者兼有。如果它们在一个或多个索引位置有不同的字符，令 <i>k</i> 为最小的此类索引；那么在位置 <i>k</i> 处字符值较小的字符串（通过 < 运算符确定）在字典序上先于另一个字符串。在这种情况下，{@code compareTo} 返回两个字符串在位置 {@code k} 处的字符值的差值，即：
     * <blockquote><pre>
     * this.charAt(k)-anotherString.charAt(k)
     * </pre></blockquote>
     * 如果没有索引位置不同，则较短的字符串在字典序上先于较长的字符串。在这种情况下，{@code compareTo} 返回字符串长度的差值，即：
     * <blockquote><pre>
     * this.length()-anotherString.length()
     * </pre></blockquote>
     *
     * @param   anotherString   要比较的 {@code String}。
     * @return  如果参数字符串等于此字符串，则返回 {@code 0}；如果此字符串在字典序上小于参数字符串，则返回小于 {@code 0} 的值；如果此字符串在字典序上大于参数字符串，则返回大于 {@code 0} 的值。
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
     * 一个按 {@code compareToIgnoreCase} 排序 {@code String} 对象的比较器。此比较器是可序列化的。
     * <p>
     * 注意，此比较器不考虑语言环境，对于某些语言环境会导致不满意的排序结果。java.text 包提供了 <em>Collators</em> 以允许语言环境敏感的排序。
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
                            // 由于数值提升，不会出现溢出
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
     * 按字典序比较两个字符串，忽略大小写差异。此方法返回一个整数，其符号与调用 {@code compareTo} 时对字符串规范化版本（通过对每个字符调用 {@code Character.toLowerCase(Character.toUpperCase(character))} 消除大小写差异）的结果相同。
     * <p>
     * 注意，此方法不考虑语言环境，对于某些语言环境会导致不满意的排序结果。java.text 包提供了 <em>collators</em> 以允许语言环境敏感的排序。
     *
     * @param   str   要比较的 {@code String}。
     * @return  一个负整数、零或正整数，表示指定字符串大于、等于或小于此字符串，忽略大小写。
     * @see     java.text.Collator#compare(String, String)
     * @since   1.2
     */
    public int compareToIgnoreCase(String str) {
        return CASE_INSENSITIVE_ORDER.compare(this, str);
    }

    /**
     * 测试两个字符串区域是否相等。
     * <p>
     * 将此 {@code String} 对象的子字符串与参数 other 的子字符串进行比较。如果这些子字符串表示相同的字符序列，则结果为 true。
     * 要比较的此 {@code String} 对象的子字符串从索引 {@code toffset} 开始，长度为 {@code len}。要比较的 other 的子字符串从索引 {@code ooffset} 开始，长度为 {@code len}。
     * 仅当以下至少一个条件为真时，结果为 {@code false}：
     * <ul><li>{@code toffset} 为负。
     * <li>{@code ooffset} 为负。
     * <li>{@code toffset+len} 大于此 {@code String} 对象的长度。
     * <li>{@code ooffset+len} 大于其他参数的长度。
     * <li>存在某个非负整数 <i>k</i> 小于 {@code len}，使得：
     * {@code this.charAt(toffset + }<i>k</i>{@code ) != other.charAt(ooffset + }<i>k</i>{@code )}
     * </ul>
     *
     * @param   toffset   此字符串中子区域的起始偏移量。
     * @param   other     字符串参数。
     * @param   ooffset   字符串参数中子区域的起始偏移量。
     * @param   len       要比较的字符数。
     * @return  如果此字符串的指定子区域与字符串参数的指定子区域完全匹配，则返回 {@code true}；否则返回 {@code false}。
     */
    public boolean regionMatches(int toffset, String other, int ooffset,
            int len) {
        char ta[] = value;
        int to = toffset;
        char pa[] = other.value;
        int po = ooffset;
        // 注意：toffset、ooffset 或 len 可能接近 -1>>>1。
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
     * 将此 {@code String} 对象的子字符串与参数 {@code other} 的子字符串进行比较。如果这些子字符串表示的字符序列相同，则结果为 {@code true}，仅当 {@code ignoreCase} 为 true 时忽略大小写。
     * 要比较的此 {@code String} 对象的子字符串从索引 {@code toffset} 开始，长度为 {@code len}。要比较的 {@code other} 的子字符串从索引 {@code ooffset} 开始，长度为 {@code len}。
     * 仅当以下至少一个条件为真时，结果为 {@code false}：
     * <ul><li>{@code toffset} 为负。
     * <li>{@code ooffset} 为负。
     * <li>{@code toffset+len} 大于此 {@code String} 对象的长度。
     * <li>{@code ooffset+len} 大于其他参数的长度。
     * <li>{@code ignoreCase} 为 {@code false} 且存在某个非负整数 <i>k</i> 小于 {@code len}，使得：
     * <blockquote><pre>
     * this.charAt(toffset+k) != other.charAt(ooffset+k)
     * </pre></blockquote>
     * <li>{@code ignoreCase} 为 {@code true} 且存在某个非负整数 <i>k</i> 小于 {@code len}，使得：
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
     * @param   ignoreCase   如果为 {@code true}，比较字符时忽略大小写。
     * @param   toffset      此字符串中子区域的起始偏移量。
     * @param   other        字符串参数。
     * @param   ooffset      字符串参数中子区域的起始偏移量。
     * @param   len          要比较的字符数。
     * @return  如果此字符串的指定子区域与字符串参数的指定子区域匹配，则返回 {@code true}；否则返回 {@code false}。匹配是精确还是忽略大小写取决于 {@code ignoreCase} 参数。
     */
    public boolean regionMatches(boolean ignoreCase, int toffset,
            String other, int ooffset, int len) {
        char ta[] = value;
        int to = toffset;
        char pa[] = other.value;
        int po = ooffset;
        // 注意：toffset、ooffset 或 len 可能接近 -1>>>1。
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
                // 如果字符不匹配但可以忽略大小写，
                // 尝试将两个字符都转换为大写。
                // 如果结果匹配，则继续比较。
                char u1 = Character.toUpperCase(c1);
                char u2 = Character.toUpperCase(c2);
                if (u1 == u2) {
                    continue;
                }
                // 不幸的是，大写转换对格鲁吉亚字母表不起作用，
                // 它的案例转换规则很奇怪。因此需要最后检查一次。
                if (Character.toLowerCase(u1) == Character.toLowerCase(u2)) {
                    continue;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * 测试此字符串从指定索引开始的子字符串是否以指定的前缀开头。
     *
     * @param   prefix    前缀。
     * @param   toffset   在此字符串中开始查找的位置。
     * @return  如果参数表示的字符序列是此对象从索引 {@code toffset} 开始的子字符串的前缀，则返回 {@code true}；否则返回 {@code false}。
     *          如果 {@code toffset} 为负或大于此 {@code String} 对象的长度，则结果为 {@code false}；否则结果与以下表达式的结果相同：
     *          <pre>
     *          this.substring(toffset).startsWith(prefix)
     *          </pre>
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
     * 测试此字符串是否以指定的前缀开头。
     *
     * @param   prefix   前缀。
     * @return  如果参数表示的字符序列是此字符串表示的字符序列的前缀，则返回 {@code true}；否则返回 {@code false}。
     *          注意，如果参数是空字符串或等于此 {@code String} 对象（由 {@link #equals(Object)} 方法确定），也将返回 {@code true}。
     * @since   1.0
     */
    public boolean startsWith(String prefix) {
        return startsWith(prefix, 0);
    }

    /**
     * 测试此字符串是否以指定的后缀结尾。
     *
     * @param   suffix   后缀。
     * @return  如果参数表示的字符序列是此对象表示的字符序列的后缀，则返回 {@code true}；否则返回 {@code false}。注意，如果参数是空字符串或等于此 {@code String} 对象（由 {@link #equals(Object)} 方法确定），结果也将为 {@code true}。
     */
    public boolean endsWith(String suffix) {
        return startsWith(suffix, value.length - suffix.value.length);
    }

    /**
     * 返回此字符串的哈希码。{@code String} 对象的哈希码按以下公式计算：
     * <blockquote><pre>
     * s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
     * </pre></blockquote>
     * 使用 {@code int} 算术，其中 {@code s[i]} 是字符串的第 <i>i</i> 个字符，{@code n} 是字符串的长度，{@code ^} 表示幂运算。
     * （空字符串的哈希值为零。）
     *
     * @return  此对象的哈希码值。
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
     * 返回此字符串中指定字符第一次出现的索引。如果此 {@code String} 对象表示的字符序列中出现值为 {@code ch} 的字符，则返回第一次出现的索引（以 Unicode 代码单元为单位）。
     * 对于值在 0 到 0xFFFF（包含）范围内的 {@code ch}，这是最小的 <i>k</i> 值，使得：
     * <blockquote><pre>
     * this.charAt(<i>k</i>) == ch
     * </pre></blockquote>
     * 为真。对于其他 {@code ch} 值，这是最小的 <i>k</i> 值，使得：
     * <blockquote><pre>
     * this.codePointAt(<i>k</i>) == ch
     * </pre></blockquote>
     * 为真。在任一情况下，如果此字符串中没有这样的字符，则返回 {@code -1}。
     *
     * @param   ch   一个字符（Unicode 代码点）。
     * @return  此对象表示的字符序列中字符第一次出现的索引，或者如果字符未出现则返回 {@code -1}。
     */
    public int indexOf(int ch) {
        return indexOf(ch, 0);
    }

    /**
     * 返回此字符串中指定字符第一次出现的索引，从指定索引开始搜索。
     * <p>
     * 如果此 {@code String} 对象表示的字符序列中在不小于 {@code fromIndex} 的索引处出现值为 {@code ch} 的字符，则返回第一次出现的索引。
     * 对于值在 0 到 0xFFFF（包含）范围内的 {@code ch}，这是最小的 <i>k</i> 值，使得：
     * <blockquote><pre>
     * (this.charAt(<i>k</i>) == ch) {@code &&} (<i>k</i> >= fromIndex)
     * </pre></blockquote>
     * 为真。对于其他 {@code ch} 值，这是最小的 <i>k</i> 值，使得：
     * <blockquote><pre>
     * (this.codePointAt(<i>k</i>) == ch) {@code &&} (<i>k</i> >= fromIndex)
     * </pre></blockquote>
     * 为真。在任一情况下，如果此字符串在位置 {@code fromIndex} 或之后没有这样的字符，则返回 {@code -1}。
     *
     * <p>
     * 对 {@code fromIndex} 的值没有限制。如果它为负，则效果等同于零：将搜索整个字符串。如果它大于此字符串的长度，则效果等同于字符串的长度：返回 {@code -1}。
     *
     * <p>所有索引都以 {@code char} 值（Unicode 代码单元）指定。
     *
     * @param   ch          一个字符（Unicode 代码点）。
     * @param   fromIndex   开始搜索的索引。
     * @return  此对象表示的字符序列中大于或等于 {@code fromIndex} 的字符第一次出现的索引，或者如果字符未出现则返回 {@code -1}。
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
     * 处理 indexOf 的（罕见）增补字符调用。
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
     * 返回此字符串中指定字符最后一次出现的索引。对于值在 0 到 0xFFFF（包含）范围内的 {@code ch}，返回的索引（以 Unicode 代码单元为单位）是最大的 <i>k</i> 值，使得：
     * <blockquote><pre>
     * this.charAt(<i>k</i>) == ch
     * </pre></blockquote>
     * 为真。对于其他 {@code ch} 值，这是最大的 <i>k</i> 值，使得：
     * <blockquote><pre>
     * this.codePointAt(<i>k</i>) == ch
     * </pre></blockquote>
     * 为真。在任一情况下，如果此字符串中没有这样的字符，则返回 {@code -1}。从最后一个字符开始向后搜索 {@code String}。
     *
     * @param   ch   一个字符（Unicode 代码点）。
     * @return  此对象表示的字符序列中字符最后一次出现的索引，或者如果字符未出现则返回 {@code -1}。
     */
    public int lastIndexOf(int ch) {
        return lastIndexOf(ch, value.length - 1);
    }

    /**
     * 返回此字符串中指定字符最后一次出现的索引，从指定索引开始向后搜索。对于值在 0 到 0xFFFF（包含）范围内的 {@code ch}，返回的索引是最大的 <i>k</i> 值，使得：
     * <blockquote><pre>
     * (this.charAt(<i>k</i>) == ch) && (<i>k</i> <= fromIndex)
     * </pre></span>
     * 为真。对于其他 {@code ch} 值，这是最大的 <i>k</i> 值，使得：
     * <blockquote><pre>
     (this.codePointAt(<i>k</i>) == ch) && (<i>k</i> <= fromIndex)
     * </pre></span>
     * 为真。在任一情况下，如果此字符串在位置 或之前没有这样的字符，则返回 {@code -1}。
     *
     * <p>所有索引都以 {@code char} 值（Unicode 代码单元）指定。
     *
     * @param   ch          一个字符（Unicode 代码点）。
     * @param   fromIndex   开始搜索的索引。对此索引值没有限制。如果它大于或等于此字符串的长度，则效果等同于字符串长度减一：将搜索整个字符串。如果它为负，则效果等同于 -1：返回 -1。
     * @return  此对象表示的字符序列中小于或等于 {@code fromIndex} 的字符最后一次出现的索引，或者如果该点之前没有该字符则返回 {@code -1}。
     */
    public int lastIndexOf(int ch, int fromIndex) {
        if (ch < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
            // 处理大多数情况（ch 是 BMP 代码点或负值（无效代码点））
            final char[] chars = this.value;
            int i = Math.min(fromIndex, chars.length - 1);
            for (; i >= 0; i--) {
                if (chars[i] == ch) {
                    return i;
                }
            }
            return -1;
        } else {
            return lastIndexOfSupplementary(ch, fromIndex);
        }
    }

    /**
     * 处理 lastIndexOf 的（罕见）扩展字符调用。
     */
    private int lastIndexOfSupplementary(int ch, int fromIndex) {
        if (Character.isValidCodePoint(ch)) {
            final char[] chars = this.value;
            char hi = Character.highSurrogate(ch);
            char lo = Character.lowSurrogate(ch);
            int i = Math.min(fromIndex, chars.length - 2);
            for (; i >= 0; i--) {
                if (chars[i] == hi && chars[i + 1] == lo) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 返回此字符串中指定子字符串第一次出现的索引。
     *
     * <p>返回的索引是最小的值 <i>k</i>，满足：
     * <pre>
     * this.startsWith(str, <i>k</i>)
     * </pre>
     * 如果不存在这样的值，则返回 {@code -1}。
     *
     * @param   str   要搜索的子字符串。
     * @return  指定子字符串第一次出现的索引，或者如果没有这样的出现，则返回 {@code -1}。
     */
    public int indexOf(String str) {
        return indexOf(str, 0);
    }

    /**
     * 返回此字符串中指定子字符串第一次出现的索引，从指定索引开始。
     *
     * <p>返回的索引是最小的值 <i>k</i>，满足：
     * <pre>
     * <i>k</i> >= fromIndex && this.startsWith(str, <i>k</i>)
     * </pre>
     * 如果不存在这样的值，则返回 {@code -1}。
     *
     * @param   str         要搜索的子字符串。
     * @param   fromIndex   开始搜索的索引。
     * @return  指定子字符串从指定索引开始第一次出现的索引，或者如果没有这样的出现，则返回 {@code -1}。
     */
    public int indexOf(String str, int fromIndex) {
        return indexOf(value, 0, value.length,
                str.value, 0, str.value.length, fromIndex);
    }

    /**
     * String 和 AbstractStringBuilder 用于搜索的共享代码。源是正在搜索的字符数组，目标是正在搜索的字符串。
     *
     * @param   source       正在搜索的字符。
     * @param   sourceOffset 源字符串的偏移量。
     * @param   sourceCount  源字符串的计数。
     * @param   target       要搜索的字符。
     * @param   fromIndex    开始搜索的索引。
     */
    static int indexOf(char[] source, int sourceOffset, int sourceCount,
            String target, int fromIndex) {
        return indexOf(source, sourceOffset, sourceCount,
                       target.value, 0, target.value.length,
                       fromIndex);
    }

    /**
     * String 和 StringBuffer 用于搜索的共享代码。源是正在搜索的字符数组，目标是正在搜索的字符串。
     *
     * @param   source       正在搜索的字符。
     * @param   sourceOffset 源字符串的偏移量。
     * @param   sourceCount  源字符串的计数。
     * @param   target       要搜索的字符。
     * @param   targetOffset 目标字符串的偏移量。
     * @param   targetCount  目标字符串的计数。
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
        int max = sourceOffset + (targetCount - 1);

        for (int i = sourceOffset + fromIndex; i <= max; i++) {
            /* 查找第一个字符。 */
            if (source[i] != first) {
                while (++i <= max && source[i] != first);
            }

            /* 找到第一个字符后，检查 v2 的其余部分 */
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
     * <p>返回的索引是最大的 <i>k</i>，满足：
     * <pre>
     * this.startsWith(str, <i>k</i>)
     * </pre>
     * 如果不存在这样的值，则返回 {@code -1}。
     *
     * @param   str   要搜索的子字符串。
     * @return  指定子字符串的最后一次出现的索引，或者如果没有找到，则返回 {@code -1}。
     */
    public int lastIndexOf(String str) {
        return lastIndexOf(str, value.length);
    }

    /**
     * 返回此字符串中指定子字符串最后一次出现的索引，从指定索引开始向后搜索。
     *
     * <p>返回的索引是最大的值 <i>k</i>，满足：
     * <pre>
     * <i>k</i> <= fromIndex && this.startsWith(str, <i>k</i>)
     * </pre>
     * 如果不存在这样的值，则返回 {@code -1}。
     *
     * @param   str         要搜索的子字符串。
     * @param   fromIndex   开始搜索的索引。
     * @return  指定子字符串从指定索引向后搜索的最后一次出现的索引，或者如果没有找到，则返回 {@code -1}。
     */
    public int lastIndexOf(String str, int fromIndex) {
        return lastIndexOf(value, 0, value.length,
                str.value, 0, str.value.length, fromIndex);
    }

    /**
     * String 和 AbstractStringBuilder 用于搜索的共享代码。源是正在搜索的字符数组，目标是搜索的字符串。
     *
     * @param   source       正在搜索的字符。
     * @param   sourceOffset 源字符串的偏移量。
     * @param   sourceCount  源字符串的计数。
     * @param   target       要搜索的字符。
     * @param   fromIndex    开始搜索的索引。
     */
    static int lastIndexOf(char[] source, int sourceOffset, int sourceCount,
            String target, int fromIndex) {
        return lastIndexOf(source, sourceOffset, sourceCount,
                       target.value, 0, target.value.length,
                       fromIndex);
    }

    /**
     * String 和 StringBuffer 用于搜索的共享代码。源是正在搜索的字符数组，目标是正在搜索的字符串。
     *
     * @param   source       正在搜索的字符。
     * @param   sourceOffset 源字符串的偏移量。
     * @param   sourceCount  源字符串的计数。
     * @param   target       要搜索的字符。
     * @param   targetOffset 目标字符串的偏移量。
     * @param   targetCount  目标字符串的计数。
     * @param   fromIndex    开始搜索的索引。
     */
    static int lastIndexOf(char[] source, int sourceOffset, int sourceCount,
            char[] target, int targetOffset, int targetCount,
            int fromIndex) {
        /*
         * 检查参数，尽可能快速返回。
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
     * 返回此字符串的子字符串。子字符串从指定索引处的字符开始，延伸到此字符串的末尾。
     * <p>
     * 示例：
     * <pre>
     * "unhappy".substring(2) 返回 "happy"
     * "Harbison".substring(3) 返回 "bison"
     * "emptiness".substring(9) 返回 ""（空字符串）
     * </pre>
     *
     * @param      beginIndex   开始索引（包含）。
     * @return     指定的子字符串。
     * @exception  IndexOutOfBoundsException  如果 {@code beginIndex} 为负或大于此 {@code String} 对象的长度。
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
     * 返回此字符串的子字符串。子字符串从指定的 {@code beginIndex} 开始，延伸到索引 {@code endIndex - 1} 处的字符。因此子字符串的长度为 {@code endIndex-beginIndex}。
     * <p>
     * 示例：
     * <pre>
     * "hamburger".substring(4, 8) 返回 "urge"
     * "smiles".substring(1, 5) 返回 "mile"
     * </pre>
     *
     * @param      beginIndex   开始索引（包含）。
     * @param      endIndex     结束索引（不包含）。
     * @return     指定的子字符串。
     * @exception  IndexOutOfBoundsException  如果 {@code beginIndex} 为负，或 {@code endIndex} 大于此 {@code String} 对象的长度，或 {@code beginIndex} 大于 {@code endIndex}。
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
     * 返回此字符串的子序列的字符序列。
     *
     * <p> 以以下形式调用此方法：
     *
     * <blockquote><pre>
     * str.subSequence(begin, end)
     * </pre></blockquote>
     *
     * 与调用以下方法的行为完全相同：
     *
     * <blockquote><pre>
     * str.substring(begin, end)
     * </pre></blockquote>
     *
     * @apiNote
     * 此方法被定义以使 {@code String} 类能够实现 {@link CharSequence} 接口。
     *
     * @param   beginIndex   开始索引（包含）。
     * @param   endIndex     结束索引（不包含）。
     * @return  指定的子序列。
     *
     * @throws  IndexOutOfBoundsException
     *          如果 {@code beginIndex} 或 {@code endIndex} 为负，
     *          如果 {@code endIndex} 大于 {@code length()}，
     *          或如果 {@code beginIndex} 大于 {@code endIndex}。
     *
     * @since 1.4
     * @spec JSR-51
     */
    public CharSequence subSequence(int beginIndex, int endIndex) {
        return this.substring(beginIndex, endIndex);
    }

    /**
     * 将指定字符串连接到此字符串的末尾。
     * <p>
     * 如果参数字符串的长度为 {@code 0}，则返回此 {@code String} 对象。
     * 否则，返回一个新的 {@code String} 对象，表示此 {@code String} 对象表示的字符序列与参数字符串表示的字符序列的连接。
     * <p>
     * 示例：
     * <blockquote><pre>
     * "cares".concat("s") 返回 "caress"
     * "to".concat("get").concat("her") 返回 "together"
     * </pre></blockquote>
     *
     * @param   str   要连接到此 {@code String} 末尾的 {@code String}。
     * @return  表示此对象字符后跟参数字符串字符的字符串。
     */
    public String concat(String str) {
        if (str.isEmpty()) {
            return this;
        }
        int len = value.length;
        int otherLen = str.length();
        char buf[] = Arrays.copyOf(value, len + otherLen);
        str.getChars(buf, len);
        return new String(buf, true);
    }

    /**
     * 返回一个字符串，通过将此字符串中所有出现的 {@code oldChar} 替换为 {@code newChar}。
     * <p>
     * 如果字符 {@code oldChar} 未出现在此 {@code String} 对象表示的字符序列中，则返回此 {@code String} 对象的引用。
     * 否则，返回一个新的 {@code String} 对象，表示的字符序列与此 {@code String} 对象表示的字符序列相同，只是每个出现的 {@code oldChar} 都被替换为 {@code newChar}。
     * <p>
     * 示例：
     * <blockquote><pre>
     * "mesquite in your cellar".replace('e', 'o')
     *         返回 "mosquito in your collar"
     * "the war of baronets".replace('r', 'y')
     *         返回 "the way of bayonets"
     * "sparring with a purple porpoise".replace('p', 't')
     *         返回 "starring with a turtle tortoise"
     * "JonL".replace('q', 'x') 返回 "JonL" （无变化）
     * </pre></blockquote>
     *
     * @param   oldChar   旧字符。
     * @param   newChar   新字符。
     * @return  通过将每个出现的 {@code oldChar} 替换为 {@code newChar} 得到的字符串。
     */
    public String replace(char oldChar, char newChar) {
        if (oldChar != newChar) {
            int len = value.length;
            int i = -1;
            char[] val = value; /* 避免 getfield 操作码 */

            while (++i < len) {
                if (val[i] == oldChar) {
                    break;
                }
            }
            if (i < len) {
                char buf[] = new char[len];
                for (int j = 0; j < i; j++) {
                    buf[j] = val[j];
                }
                while (i < len) {
                    char c = val[i];
                    buf[i] = (c == oldChar) ? newChar : c;
                    i++;
                }
                return new String(buf, true);
            }
        }
        return this;
    }

    /**
     * 判断此字符串是否匹配给定的 <a href="../util/regex/Pattern.html#sum">正则表达式</a>。
     *
     * <p> 以以下形式调用此方法：
     * <i>str</i>{@code .matches(}<i>regex</i>{@code )} 的结果与以下表达式的结果完全相同：
     *
     * <blockquote>
     * {@link java.util.regex.Pattern}.{@link java.util.regex.Pattern#matches(String,CharSequence) matches(<i>regex</i>, <i>str</i>)}
     * </blockquote>
     *
     * @param   regex
     *          要匹配此字符串的正则表达式
     *
     * @return  当且仅当此字符串匹配给定的正则表达式时，返回 {@code true}
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
     * 当且仅当此字符串包含指定的字符值序列时返回 true。
     *
     * @param s 要搜索的序列
     * @return 如果此字符串包含 {@code s}，则返回 true，否则返回 false
     * @since 1.5
     */
    public boolean contains(CharSequence s) {
        return indexOf(s.toString()) > -1;
    }

    /**
     * 将此字符串中第一个匹配给定 <a href="../util/regex/Pattern.html#sum">正则表达式</a> 的子字符串替换为给定的替换字符串。
     *
     * <p> 以以下形式调用此方法：
     * <i>str</i>{@code .replaceFirst(}<i>regex</i>{@code ,} <i>repl</i>{@code )}
     * 的结果与以下表达式的结果完全相同：
     *
     * <blockquote>
     * <code>
     * {@link java.util.regex.Pattern}.{@link java.util.regex.Pattern#compile compile}(<i>regex</i>).{@link java.util.regex.Pattern#matcher(java.lang.CharSequence) matcher}(<i>str</i>).{@link java.util.regex.Matcher#replaceFirst replaceFirst}(<i>repl</i>)
     * </code>
     * </blockquote>
     *
     * <p>
     * 请注意，替换字符串中的反斜杠 ({@code \}) 和美元符号 ({@code $}) 可能导致结果与将其视为字面替换字符串时不同；请参见 {@link java.util.regex.Matcher#replaceFirst}。
     * 如果需要，可以使用 {@link java.util.regex.Matcher#quoteReplacement} 来抑制这些字符的特殊含义。
     *
     * @param   regex
     *          要匹配此字符串的正则表达式
     * @param   replacement
     *          用于替换第一个匹配的字符串
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
     * 将此字符串中每个匹配给定 <a href="../util/regex/Pattern.html#sum">正则表达式</a> 的子字符串替换为给定的替换字符串。
     *
     * <p> 以以下形式调用此方法：
     * <i>str</i>{@code .replaceAll(}<i>regex</i>{@code ,} <i>repl</i>{@code )}
     * 的结果与以下表达式的结果完全相同：
     *
     * <blockquote>
     * <code>
     * {@link java.util.regex.Pattern}.{@link java.util.regex.Pattern#compile compile}(<i>regex</i>).{@link java.util.regex.Pattern#matcher(java.lang.CharSequence) matcher}(<i>str</i>).{@link java.util.regex.Matcher#replaceAll replaceAll}(<i>repl</i>)
     * </code>
     * </blockquote>
     *
     * <p>
     * 请注意，替换字符串中的反斜杠 ({@code \}) 和美元符号 ({@code $}) 可能导致结果与将其视为字面替换字符串时不同；请参见 {@link java.util.regex.Matcher#replaceAll}。
     * 如果需要，可以使用 {@link java.util.regex.Matcher#quoteReplacement} 来抑制这些字符的特殊含义。
     *
     * @param   regex
     *          要匹配此字符串的正则表达式
     * @param   replacement
     *          用于替换每个匹配的字符串
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
     * 将此字符串中每个匹配字面目标序列的子字符串替换为指定的字面替换序列。替换从字符串开头到结尾进行，例如，在字符串“aaa”中将“aa”替换为“b”将得到“ba”而不是“ab”。
     *
     * @param  target 要替换的字符值序列
     * @param  replacement 替换的字符值序列
     * @return  结果字符串
     * @since 1.5
     */
    public String replace(CharSequence target, CharSequence replacement) {
        return Pattern.compile(target.toString(), Pattern.LITERAL).matcher(
                this).replaceAll(Matcher.quoteReplacement(replacement.toString()));
    }

    /**
     * 根据给定的 <a href="../util/regex/Pattern.html#sum">正则表达式</a> 匹配来分割此字符串。
     *
     * <p> 此方法返回的数组包含此字符串的每个子字符串，这些子字符串由匹配给定表达式的另一个子字符串终止，或由字符串的末尾终止。数组中的子字符串按它们在此字符串中出现的顺序排列。如果表达式不匹配输入的任何部分，则结果数组只有一个元素，即此字符串。
     *
     * <p> 当此字符串开头有一个正宽度匹配时，结果数组开头将包含一个空的领先子字符串。然而，字符串开头的零宽度匹配永远不会产生这样的空领先子字符串。
     *
     * <p> {@code limit} 参数控制模式应用的次数，因此影响结果数组的长度。如果限制 <i>n</i> 大于零，则模式最多应用 <i>n</i> - 1 次，数组的长度不超过 <i>n</i>，且数组的最后一条目将包含最后一个匹配分隔符之后的所有输入。如果 <i>n</i> 为非正数，则模式将尽可能多次应用，数组可以具有任意长度。如果 <i>n</i> 为零，则模式将尽可能多次应用，数组可以具有任意长度，且尾随的空字符串将被丢弃。
     *
     * <p> 例如，字符串 {@code "boo:and:foo"} 使用以下参数会产生以下结果：
     *
     * <blockquote><table cellpadding=1 cellspacing=0 summary="Split example showing regex, limit, and result">
     * <tr>
     *     <th>正则表达式</th>
     *     <th>限制</th>
     *     <th>结果</th>
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
     * <p> 以以下形式调用此方法：
     * <i>str.</i>{@code split(}<i>regex</i>{@code ,} <i>n</i>{@code )}
     * 的结果与以下表达式的结果相同：
     *
     * <blockquote>
     * <code>
     * {@link java.util.regex.Pattern}.{@link java.util.regex.Pattern#compile compile}(<i>regex</i>).{@link java.util.regex.Pattern#split(java.lang.CharSequence,int) split}(<i>str</i>, <i>n</i>)
     * </code>
     * </blockquote>
     *
     * @param  regex
     *         分隔的正则表达式
     *
     * @param  limit
     *         结果阈值，如上所述
     *
     * @return  通过围绕给定正则表达式的匹配分割此字符串计算出的字符串数组
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
        /* 如果正则表达式是：
         (1) 单个字符的字符串，且此字符不是正则表达式的元字符 ".$|()[{^?*+\\"，或
         (2) 两个字符的字符串，第一个字符是反斜杠，第二个字符不是 ASCII 数字或字母，
         则走快速路径 */
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
                    // assert (list.size() == limit - 1);
                    list.add(substring(off, value.length));
                    off = value.length;
                    break;
                }
            }
            // 如果未找到匹配项，返回此字符串
            if (off == 0)
                return new String[]{this};

            // 添加剩余部分
            if (!limited || list.size() < limit)
                list.add(substring(off, value.length));

            // 构造结果
            int resultSize = list.size();
            if (limit == 0) {
                while (resultSize > 0 && list.get(resultSize - 1).isEmpty()) {
                    resultSize--;
                }
            }
            String[] result = new String[resultSize];
            return list.subList(0, resultSize).toArray(result);
        }
        return Pattern.compile(regex).split(this, limit);
    }

    /**
     * 根据给定的 <a href="../util/regex/Pattern.html#sum">正则表达式</a> 匹配来分割此字符串。
     *
     * <p> 此方法的工作方式如同调用带给定表达式和限制参数为零的二参数 {@link #split(String, int) split} 方法。因此，结果数组中不包括尾随的空字符串。
     *
     * <p> 例如，字符串 {@code "boo:and:foo"} 使用以下表达式会产生以下结果：
     *
     * <blockquote><table cellpadding=1 cellspacing=0 summary="Split examples showing regex and result">
     * <tr>
     *  <th>正则表达式</th>
     *  <th>结果</th>
     * </tr>
     * <tr><td align=center>:</td>
     *     <td>{@code { "boo", "and", "foo" }}</td></tr>
     * <tr><td align=center>o</td>
     *     <td>{@code { "b", "", ":and:f" }}</td></tr>
     * </table></blockquote>
     *
     * @param  regex
     *         分隔的正则表达式
     *
     * @return  通过围绕给定正则表达式的匹配分割此字符串计算出的字符串数组
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
     * 返回一个新的字符串，由 {@code CharSequence elements} 的副本组成，用指定的 {@code delimiter} 的副本连接在一起。
     *
     * <blockquote>例如，
     * <pre>{@code
     *     String message = String.join("-", "Java", "is", "cool");
     *     // 返回的消息是："Java-is-cool"
     * }</pre></blockquote>
     *
     * 请注意，如果某个元素为 null，则添加 {@code "null"}。
     *
     * @param  delimiter 分隔每个元素的定界符
     * @param  elements 要连接在一起的元素
     *
     * @return  一个新的 {@code String}，由 {@code elements} 组成，元素之间由 {@code delimiter} 分隔
     *
     * @throws NullPointerException 如果 {@code delimiter} 或 {@code elements} 为 {@code null}
     *
     * @see java.util.StringJoiner
     * @since 1.8
     */
    public static String join(CharSequence delimiter, CharSequence... elements) {
        Objects.requireNonNull(delimiter);
        Objects.requireNonNull(elements);
        // 元素数量通常不值得使用 Arrays.stream 的开销。
        StringJoiner joiner = new StringJoiner(delimiter);
        for (CharSequence cs: elements) {
            joiner.add(cs);
        }
        return joiner.toString();
    }

    /**
     * 返回一个新的 {@code String}，由 {@code CharSequence elements} 的副本组成，用指定的 {@code delimiter} 的副本连接在一起。
     *
     * <blockquote>例如，
     * <pre>{@code
     *     List<String> strings = new LinkedList<>();
     *     strings.add("Java");strings.add("is");
     *     strings.add("cool");
     *     String message = String.join(" ", strings);
     *     // 返回的消息是："Java is cool"
     *
     *     Set<String> strings = new LinkedHashSet<>();
     *     strings.add("Java"); strings.add("is");
     *     strings.add("very"); strings.add("cool");
     *     String message = String.join("-", strings);
     *     // 返回的消息是："Java-is-very-cool"
     * }</pre></blockquote>
     *
     * 请注意，如果某个单独元素为 {@code null}，则添加 {@code "null"}。
     *
     * @param  delimiter 用于分隔结果 {@code String} 中每个 {@code elements} 的字符序列
     * @param  elements 一个 {@code Iterable}，其 {@code elements} 将被连接在一起
     *
     * @return  从 {@code elements} 参数组成的新 {@code String}
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
     * 使用给定 {@code Locale} 的规则将此 {@code String} 中的所有字符转换为小写。
     * 大小写映射基于 {@link java.lang.Character Character} 类指定的 Unicode 标准版本。
     * 由于大小写映射并非总是 1:1 的字符映射，结果 {@code String} 的长度可能与原始 {@code String} 不同。
     * <p>
     * 小写映射的示例见下表：
     * <table border="1" summary="小写映射示例，显示语言环境语言代码、大写、小写和描述">
     * <tr>
     *   <th>语言环境的语言代码</th>
     *   <th>大写</th>
     *   <th>小写</th>
     *   <th>描述</th>
     * </tr>
     * <tr>
     *   <td>tr（土耳其语）</td>
     *   <td>\u0130</td>
     *   <td>\u0069</td>
     *   <td>带点的大写 I -> 小写 i</td>
     * </tr>
     * <tr>
     *   <td>tr（土耳其语）</td>
     *   <td>\u0049</td>
     *   <td>\u0131</td>
     *   <td>大写 I -> 无点小写 i</td>
     * </tr>
     * <tr>
     *   <td>（所有）</td>
     *   <td>French Fries</td>
     *   <td>french fries</td>
     *   <td>将字符串中的所有字符转换为小写</td>
     * </tr>
     * <tr>
     *   <td>（所有）</td>
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
     * @param locale 使用此语言环境的大小写转换规则
     * @return 转换为小写的 {@code String}
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
        int resultOffset = 0;  /* 结果可能增长，因此 i+resultOffset 是结果中的写入位置 */

        /* 只复制前几个小写字符。 */
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
                srcChar == '\u0130') { // 带点的大写拉丁字母 I
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

                /* 如果需要，扩展结果数组 */
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
     * 使用默认语言环境的规则将此 {@code String} 中的所有字符转换为小写。
     * 这等价于调用 {@code toLowerCase(Locale.getDefault())}。
     * <p>
     * <b>注意：</b> 此方法对语言环境敏感，如果用于需要独立于语言环境解释的字符串，可能会产生意外结果。
     * 例如，编程语言标识符、协议键和 HTML 标签。
     * 例如，在土耳其语言环境中，{@code "TITLE".toLowerCase()} 返回 {@code "t\u005Cu0131tle"}，其中 '\u005Cu0131' 是无点小写拉丁字母 I。
     * 要获得对语言环境不敏感的字符串的正确结果，请使用 {@code toLowerCase(Locale.ROOT)}。
     * <p>
     * @return 转换为小写的 {@code String}
     * @see     java.lang.String#toLowerCase(Locale)
     */
    public String toLowerCase() {
        return toLowerCase(Locale.getDefault());
    }

    /**
     * 使用给定 {@code Locale} 的规则将此 {@code String} 中的所有字符转换为大写。
     * 大小写映射基于 {@link java.lang.Character Character} 类指定的 Unicode 标准版本。
     * 由于大小写映射并非总是 1:1 的字符映射，结果 {@code String} 的长度可能与原始 {@code String} 不同。
     * <p>
     * 语言环境敏感和 1:M 大小写映射的示例见下表：
     *
     * <table border="1" summary="语言环境敏感和 1:M 大小写映射示例。显示语言环境语言代码、小写、大写和描述。">
     * <tr>
     *   <th>语言环境的语言代码</th>
     *   <th>小写</th>
     *   <th>大写</th>
     *   <th>描述</th>
     * </tr>
     * <tr>
     *   <td>tr（土耳其语）</td>
     *   <td>\u0069</td>
     *   <td>\u0130</td>
     *   <td>小写 i -> 带点的大写 I</td>
     * </tr>
     * <tr>
     *   <td>tr（土耳其语）</td>
     *   <td>\u0131</td>
     *   <td>\u0049</td>
     *   <td>无点小写 i -> 大写 I</td>
     * </tr>
     * <tr>
     *   <td>（所有）</td>
     *   <td>\u00df</td>
     *   <td>\u0053 \u0053</td>
     *   <td>小写尖锐 s -> 两个字母：SS</td>
     * </tr>
     * <tr>
     *   <td>（所有）</td>
     *   <td>Fahrvergnügen</td>
     *   <td>FAHRVERGNÜGEN</td>
     *   <td></td>
     * </tr>
     * </table>
     * @param locale 使用此语言环境的大小写转换规则
     * @return 转换为大写的 {@code String}
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

        /* 结果可能增长，因此 i+resultOffset 是结果中的写入位置 */
        int resultOffset = 0;
        char[] result = new char[len]; /* 可能增长 */

        /* 只复制前几个大写字符。 */
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

                /* 如果需要，扩展结果数组 */
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
     * 使用默认语言环境的规则将此 {@code String} 中的所有字符转换为大写。
     * 此方法等价于 {@code toUpperCase(Locale.getDefault())}。
     * <p>
     * <b>注意：</b> 此方法对语言环境敏感，如果用于需要独立于语言环境解释的字符串，可能会产生意外结果。
     * 例如，编程语言标识符、协议键和 HTML 标签。
     * 例如，在土耳其语言环境中，{@code "title".toUpperCase()} 返回 {@code "T\u005Cu0130TLE"}，其中 '\u005Cu0130' 是带点的大写拉丁字母 I。
     * 要获得对语言环境不敏感的字符串的正确结果，请使用 {@code toUpperCase(Locale.ROOT)}。
     * <p>
     * @return 转换为大写的 {@code String}
     * @see     java.lang.String#toUpperCase(Locale)
     */
    public String toUpperCase() {
        return toUpperCase(Locale.getDefault());
    }

    /**
     * 返回此字符串的值，去除任何前导和尾随的空白字符。
     * <p>
     * 如果此 {@code String} 对象表示一个空的字符序列，或者此 {@code String} 对象表示的字符序列的第一个和最后一个字符的代码都大于 {@code '\u005Cu0020'}（空格字符），则返回此 {@code String} 对象的引用。
     * <p>
     * 否则，如果字符串中没有代码大于 {@code '\u005Cu0020'} 的字符，则返回表示空字符串的 {@code String} 对象。
     * <p>
     * 否则，设 <i>k</i> 为字符串中第一个代码大于 {@code '\u005Cu0020'} 的字符的索引，设 <i>m</i> 为字符串中最后一个代码大于 {@code '\u005Cu0020'} 的字符的索引。
     * 返回一个 {@code String} 对象，表示此字符串的子字符串，从索引 <i>k</i> 处的字符开始，到索引 <i>m</i> 处的字符结束，即 {@code this.substring(k, m + 1)} 的结果。
     * <p>
     * 此方法可用于从字符串的开头和结尾修剪空白字符（如上定义）。
     *
     * @return  此字符串的值，去除任何前导和尾随的空白字符；如果没有前导或尾随的空白字符，则返回此字符串。
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
     * 返回此对象本身（它已经是一个字符串！）。
     *
     * @return  字符串本身。
     */
    public String toString() {
        return this;
    }

    /**
     * 将此字符串转换为一个新的字符数组。
     *
     * @return  一个新分配的字符数组，其长度为此字符串的长度，内容初始化为包含此字符串表示的字符序列。
     */
    public char[] toCharArray() {
        // 由于类初始化顺序问题，不能使用 Arrays.copyOf
        char result[] = new char[value.length];
        System.arraycopy(value, 0, result, 0, value.length);
        return result;
    }

    /**
     * 使用指定的格式字符串和参数返回一个格式化的字符串。
     *
     * <p> 始终使用的语言环境是 {@link java.util.Locale#getDefault() Locale.getDefault()} 返回的语言环境。
     *
     * @param  format
     *         一个 <a href="../util/Formatter.html#syntax">格式字符串</a>
     *
     * @param  args
     *         格式说明符引用的参数。如果参数多于格式说明符，多余的参数将被忽略。参数数量可变，可能为零。参数的最大数量受 <cite>Java™ 虚拟机规范</cite> 定义的 Java 数组最大维度的限制。
     *         对于 {@code null} 参数的行为取决于 <a href="../util/Formatter.html#syntax">转换</a>。
     *
     * @throws  java.util.IllegalFormatException
     *          如果格式字符串包含非法语法、与给定参数不兼容的格式说明符、格式字符串给定的参数不足，或其他非法条件。有关所有可能格式错误的规范，请参见格式化程序类规范的 <a href="../util/Formatter.html#detail">详情</a> 部分。
     *
     * @return  格式化的字符串
     *
     * @see  java.util.Formatter
     * @since  1.5
     */
    public static String format(String format, Object... args) {
        return new Formatter().format(format, args).toString();
    }

    /**
     * 使用指定的语言环境、格式字符串和参数返回一个格式化的字符串。
     *
     * @param  l
     *         在格式化期间应用的 {@linkplain java.util.Locale 语言环境}。如果 {@code l} 为 {@code null}，则不应用本地化。
     *
     * @param  format
     *         一个 <a href="../util/Formatter.html#syntax">格式字符串</a>
     *
     * @param  args
     *         格式说明符引用的参数。如果参数多于格式说明符，多余的参数将被忽略。参数数量可变，可能为零。参数的最大数量受 <cite>Java™ 虚拟机规范</cite> 定义的 Java 数组最大维度的限制。
     *         对于 {@code null} 参数的行为取决于 <a href="../util/Formatter.html#syntax">转换</a>。
     *
     * @throws  java.util.IllegalFormatException
     *          如果格式字符串包含非法语法、与给定参数不兼容的格式说明符、格式字符串给定的参数不足，或其他非法条件。有关所有可能格式错误的规范，请参见格式化程序类规范的 <a href="../util/Formatter.html#detail">详情</a> 部分。
     *
     * @return  格式化的字符串
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
     * @param   obj   一个 {@code Object}
     * @return  如果参数为 {@code null}，则返回等于 {@code "null"} 的字符串；否则，返回 {@code obj.toString()} 的值。
     * @see     java.lang.Object#toString()
     */
    public static String valueOf(Object obj) {
        return (obj == null) ? "null" : obj.toString();
    }

    /**
     * 返回 {@code char} 数组参数的字符串表示形式。字符数组的内容被复制；后续对字符数组的修改不会影响返回的字符串。
     *
     * @param   data     字符数组。
     * @return  包含字符数组中字符的 {@code String}。
     */
    public static String valueOf(char data[]) {
        return new String(data);
    }

    /**
     * 返回 {@code char} 数组参数中指定子数组的字符串表示形式。
     * <p>
     * {@code offset} 参数是子数组第一个字符的索引。{@code count} 参数指定子数组的长度。子数组的内容被复制；后续对字符数组的修改不会影响返回的字符串。
     *
     * @param   data     字符数组。
     * @param   offset   子数组的初始偏移量。
     * @param   count    子数组的长度。
     * @return  包含字符数组指定子数组中字符的 {@code String}。
     * @exception IndexOutOfBoundsException 如果 {@code offset} 为负，或 {@code count} 为负，或 {@code offset+count} 大于 {@code data.length}。
     */
    public static String valueOf(char data[], int offset, int count) {
        return new String(data, offset, count);
    }

    /**
     * 等价于 {@link #valueOf(char[], int, int)}。
     *
     * @param   data     字符数组。
     * @param   offset   子数组的初始偏移量。
     * @param   count    子数组的长度。
     * @return  包含字符数组指定子数组中字符的 {@code String}。
     * @exception IndexOutOfBoundsException 如果 {@code offset} 为负，或 {@code count} 为负，或 {@code offset+count} 大于 {@code data.length}。
     */
    public static String copyValueOf(char data[], int offset, int count) {
        return new String(data, offset, count);
    }

    /**
     * 等价于 {@link #valueOf(char[])}。
     *
     * @param   data   字符数组。
     * @return  包含字符数组中字符的 {@code String}。
     */
    public static String copyValueOf(char data[]) {
        return new String(data);
    }

    /**
     * 返回 {@code boolean} 参数的字符串表示形式。
     *
     * @param   b   一个 {@code boolean}。
     * @return  如果参数为 {@code true}，返回等于 {@code "true"} 的字符串；否则，返回等于 {@code "false"} 的字符串。
     */
    public static String valueOf(boolean b) {
        return b ? "true" : "false";
    }

    /**
     * 返回 {@code char} 参数的字符串表示形式。
     *
     * @param   c   一个 {@code char}。
     * @return  长度为 {@code 1} 的字符串，包含作为其唯一字符的参数 {@code c}。
     */
    public static String valueOf(char c) {
        char data[] = {c};
        return new String(data, true);
    }

    /**
     * 返回 {@code int} 参数的字符串表示形式。
     * <p>
     * 表示形式与单参数的 {@code Integer.toString} 方法返回的完全相同。
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
     * 表示形式与单参数的 {@code Long.toString} 方法返回的完全相同。
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
     * 表示形式与单参数的 {@code Float.toString} 方法返回的完全相同。
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
     * 表示形式与单参数的 {@code Double.toString} 方法返回的完全相同。
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
     * 类 {@code String} 私下维护一个初始为空的字符串池。
     * <p>
     * 当调用 intern 方法时，如果池中已经包含一个与此 {@code String} 对象相等的字符串（由 {@link #equals(Object)} 方法确定），则返回池中的字符串。
     * 否则，此 {@code String} 对象被添加到池中，并返回对此 {@code String} 对象的引用。
     * <p>
     * 因此，对于任意两个字符串 {@code s} 和 {@code t}，{@code s.intern() == t.intern()} 为 {@code true} 当且仅当 {@code s.equals(t)} 为 {@code true}。
     * <p>
     * 所有字面量字符串和字符串值的常量表达式都会被 intern。字符串字面量在 <cite>Java™ 语言规范</cite> 的第 3.10.5 节中定义。
     *
     * @return  与此字符串内容相同但保证来自唯一字符串池的字符串。
     */
    public native String intern();
}
    