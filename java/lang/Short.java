
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

package java.lang;

/**
 * {@code Short} 类将原始类型 {@code short} 的值包装在一个对象中。一个 {@code Short} 类型的对象包含一个 {@code short} 类型的字段。
 *
 * <p>此外，该类提供了几种将 {@code short} 转换为 {@code String} 和将 {@code String} 转换为 {@code short} 的方法，以及在处理 {@code short} 时有用的其他常量和方法。
 *
 * @author  Nakul Saraiya
 * @author  Joseph D. Darcy
 * @see     java.lang.Number
 * @since   JDK1.1
 */
public final class Short extends Number implements Comparable<Short> {

    /**
     * 一个常量，表示 {@code short} 可以具有的最小值，-2<sup>15</sup>。
     */
    public static final short   MIN_VALUE = -32768;

    /**
     * 一个常量，表示 {@code short} 可以具有的最大值，2<sup>15</sup>-1。
     */
    public static final short   MAX_VALUE = 32767;

    /**
     * 表示原始类型 {@code short} 的 {@code Class} 实例。
     */
    @SuppressWarnings("unchecked")
    public static final Class<Short>    TYPE = (Class<Short>) Class.getPrimitiveClass("short");

    /**
     * 返回一个表示指定 {@code short} 的新 {@code String} 对象。假设基数为 10。
     *
     * @param s 要转换的 {@code short}
     * @return 指定 {@code short} 的字符串表示形式
     * @see java.lang.Integer#toString(int)
     */
    public static String toString(short s) {
        return Integer.toString((int)s, 10);
    }

    /**
     * 将字符串参数解析为指定基数的带符号 {@code short}。字符串中的字符必须都是指定基数的数字（由 {@link java.lang.Character#digit(char, int)} 是否返回非负值确定），但第一个字符可以是 ASCII 减号 {@code '-'} ({@code '\u005Cu002D'}) 表示负值或 ASCII 加号 {@code '+'} ({@code '\u005Cu002B'}) 表示正值。返回解析后的 {@code short} 值。
     *
     * <p>如果发生以下任何情况，将抛出一个 {@code NumberFormatException} 异常：
     * <ul>
     * <li> 第一个参数为 {@code null} 或是一个长度为零的字符串。
     *
     * <li> 基数小于 {@link java.lang.Character#MIN_RADIX} 或大于 {@link java.lang.Character#MAX_RADIX}。
     *
     * <li> 字符串中的任何字符都不是指定基数的数字，但第一个字符可以是减号 {@code '-'} ({@code '\u005Cu002D'}) 或加号 {@code '+'} ({@code '\u005Cu002B'})，前提是字符串长度大于 1。
     *
     * <li> 字符串表示的值不是 {@code short} 类型的值。
     * </ul>
     *
     * @param s         包含要解析的 {@code short} 表示形式的 {@code String}
     * @param radix     解析 {@code s} 时使用的基数
     * @return          以指定基数表示的字符串参数的 {@code short} 值。
     * @throws          NumberFormatException 如果 {@code String} 不包含可解析的 {@code short}。
     */
    public static short parseShort(String s, int radix)
        throws NumberFormatException {
        int i = Integer.parseInt(s, radix);
        if (i < MIN_VALUE || i > MAX_VALUE)
            throw new NumberFormatException(
                "Value out of range. Value:\"" + s + "\" Radix:" + radix);
        return (short)i;
    }

    /**
     * 将字符串参数解析为带符号的十进制 {@code short}。字符串中的字符必须都是十进制数字，但第一个字符可以是 ASCII 减号 {@code '-'} ({@code '\u005Cu002D'}) 表示负值或 ASCII 加号 {@code '+'} ({@code '\u005Cu002B'}) 表示正值。返回解析后的 {@code short} 值，就像将参数和基数 10 传递给 {@link #parseShort(java.lang.String, int)} 方法一样。
     *
     * @param s 包含要解析的 {@code short} 表示形式的 {@code String}
     * @return  以十进制表示的字符串参数的 {@code short} 值。
     * @throws  NumberFormatException 如果字符串不包含可解析的 {@code short}。
     */
    public static short parseShort(String s) throws NumberFormatException {
        return parseShort(s, 10);
    }

    /**
     * 返回一个表示从指定 {@code String} 中解析出的值的 {@code Short} 对象。第一个参数被解释为以第二个参数指定的基数表示的带符号 {@code short}，就像将参数传递给 {@link #parseShort(java.lang.String, int)} 方法一样。结果是一个表示字符串指定的 {@code short} 值的 {@code Short} 对象。
     *
     * <p>换句话说，此方法返回一个等于以下值的 {@code Short} 对象：
     *
     * <blockquote>
     *  {@code new Short(Short.parseShort(s, radix))}
     * </blockquote>
     *
     * @param s         要解析的字符串
     * @param radix     解释 {@code s} 时使用的基数
     * @return          一个表示字符串参数在指定基数下表示的值的 {@code Short} 对象。
     * @throws          NumberFormatException 如果 {@code String} 不包含可解析的 {@code short}。
     */
    public static Short valueOf(String s, int radix)
        throws NumberFormatException {
        return valueOf(parseShort(s, radix));
    }

    /**
     * 返回一个表示由指定 {@code String} 给出的值的 {@code Short} 对象。参数被解释为带符号的十进制 {@code short}，就像将参数传递给 {@link #parseShort(java.lang.String)} 方法一样。结果是一个表示字符串指定的 {@code short} 值的 {@code Short} 对象。
     *
     * <p>换句话说，此方法返回一个等于以下值的 {@code Short} 对象：
     *
     * <blockquote>
     *  {@code new Short(Short.parseShort(s))}
     * </blockquote>
     *
     * @param s 要解析的字符串
     * @return  一个表示字符串参数的值的 {@code Short} 对象
     * @throws  NumberFormatException 如果 {@code String} 不包含可解析的 {@code short}。
     */
    public static Short valueOf(String s) throws NumberFormatException {
        return valueOf(s, 10);
    }

    private static class ShortCache {
        private ShortCache(){}

        static final Short cache[] = new Short[-(-128) + 127 + 1];

        static {
            for(int i = 0; i < cache.length; i++)
                cache[i] = new Short((short)(i - 128));
        }
    }

    /**
     * 返回一个表示指定 {@code short} 值的 {@code Short} 实例。
     * 如果不需要新的 {@code Short} 实例，通常应优先使用此方法而不是构造函数 {@link #Short(short)}，因为此方法通过缓存常用值，可能会显著提高空间和时间性能。
     *
     * 此方法将始终缓存 -128 到 127（包括）范围内的值，并可能缓存此范围之外的其他值。
     *
     * @param  s 一个 short 值。
     * @return 一个表示 {@code s} 的 {@code Short} 实例。
     * @since  1.5
     */
    public static Short valueOf(short s) {
        final int offset = 128;
        int sAsInt = s;
        if (sAsInt >= -128 && sAsInt <= 127) { // 必须缓存
            return ShortCache.cache[sAsInt + offset];
        }
        return new Short(s);
    }

    /**
     * 将 {@code String} 解码为 {@code Short}。
     * 接受十进制、十六进制和八进制数字，由以下语法定义：
     *
     * <blockquote>
     * <dl>
     * <dt><i>DecodableString:</i>
     * <dd><i>Sign<sub>opt</sub> DecimalNumeral</i>
     * <dd><i>Sign<sub>opt</sub></i> {@code 0x} <i>HexDigits</i>
     * <dd><i>Sign<sub>opt</sub></i> {@code 0X} <i>HexDigits</i>
     * <dd><i>Sign<sub>opt</sub></i> {@code #} <i>HexDigits</i>
     * <dd><i>Sign<sub>opt</sub></i> {@code 0} <i>OctalDigits</i>
     *
     * <dt><i>Sign:</i>
     * <dd>{@code -}
     * <dd>{@code +}
     * </dl>
     * </blockquote>
     *
     * <i>DecimalNumeral</i>、<i>HexDigits</i> 和 <i>OctalDigits</i> 的定义见《Java&trade; 语言规范》第 3.10.1 节，但数字之间不允许使用下划线。
     *
     * <p>可选符号和/或基数指定符（“{@code 0x}”、“{@code 0X}”、“{@code #}”或前导零）之后的字符序列由 {@code Short.parseShort} 方法以指定的基数（10、16 或 8）解析。此字符序列必须表示一个正值，否则将抛出 {@link NumberFormatException}。如果指定的 {@code String} 的第一个字符是减号，则结果为负值。字符串中不允许有空白字符。
     *
     * @param     nm 要解码的 {@code String}。
     * @return    一个表示 {@code nm} 中的 {@code short} 值的 {@code Short} 对象
     * @throws    NumberFormatException 如果 {@code String} 不包含可解析的 {@code short}。
     * @see java.lang.Short#parseShort(java.lang.String, int)
     */
    public static Short decode(String nm) throws NumberFormatException {
        int i = Integer.decode(nm);
        if (i < MIN_VALUE || i > MAX_VALUE)
            throw new NumberFormatException(
                    "Value " + i + " out of range from input " + nm);
        return valueOf((short)i);
    }

    /**
     * {@code Short} 的值。
     *
     * @serial
     */
    private final short value;

    /**
     * 构造一个新分配的 {@code Short} 对象，表示指定的 {@code short} 值。
     *
     * @param value     要表示的值
     */
    public Short(short value) {
        this.value = value;
    }

    /**
     * 构造一个新分配的 {@code Short} 对象，表示由 {@code String} 参数指示的 {@code short} 值。字符串以与 {@code parseShort} 方法对基数 10 的处理方式完全相同的方式转换为 {@code short} 值。
     *
     * @param s 要转换为 {@code Short} 的 {@code String}
     * @throws  NumberFormatException 如果 {@code String} 不包含可解析的 {@code short}。
     * @see     java.lang.Short#parseShort(java.lang.String, int)
     */
    public Short(String s) throws NumberFormatException {
        this.value = parseShort(s, 10);
    }

    /**
     * 返回此 {@code Short} 的值作为 {@code byte}，经过窄化原始转换。
     * @jls 5.1.3 Narrowing Primitive Conversions
     */
    public byte byteValue() {
        return (byte)value;
    }

    /**
     * 返回此 {@code Short} 的值作为 {@code short}。
     */
    public short shortValue() {
        return value;
    }

    /**
     * 返回此 {@code Short} 的值作为 {@code int}，经过扩展原始转换。
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public int intValue() {
        return (int)value;
    }

    /**
     * 返回此 {@code Short} 的值作为 {@code long}，经过扩展原始转换。
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public long longValue() {
        return (long)value;
    }

    /**
     * 返回此 {@code Short} 的值作为 {@code float}，经过扩展原始转换。
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public float floatValue() {
        return (float)value;
    }

    /**
     * 返回此 {@code Short} 的值作为 {@code double}，经过扩展原始转换。
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public double doubleValue() {
        return (double)value;
    }

    /**
     * 返回一个表示此 {@code Short} 值的 {@code String} 对象。该值被转换为带符号的十进制表示形式并作为字符串返回，就像将 {@code short} 值作为参数传递给 {@link java.lang.Short#toString(short)} 方法一样。
     *
     * @return  以十进制表示的此对象的值的字符串表示形式。
     */
    public String toString() {
        return Integer.toString((int)value);
    }

    /**
     * 返回此 {@code Short} 的哈希码；等于调用 {@code intValue()} 的结果。
     *
     * @return 此 {@code Short} 的哈希码值
     */
    @Override
    public int hashCode() {
        return Short.hashCode(value);
    }

    /**
     * 返回一个 {@code short} 值的哈希码；与 {@code Short.hashCode()} 兼容。
     *
     * @param value 要哈希的值
     * @return 一个 {@code short} 值的哈希码值。
     * @since 1.8
     */
    public static int hashCode(short value) {
        return (int)value;
    }

    /**
     * 将此对象与指定对象进行比较。结果为 {@code true} 当且仅当参数不为 {@code null} 且是一个包含与此对象相同的 {@code short} 值的 {@code Short} 对象。
     *
     * @param obj       要比较的对象
     * @return          如果对象相同，则返回 {@code true}；否则返回 {@code false}。
     */
    public boolean equals(Object obj) {
        if (obj instanceof Short) {
            return value == ((Short)obj).shortValue();
        }
        return false;
    }


                /**
     * 比较两个 {@code Short} 对象的数值。
     *
     * @param   anotherShort   要比较的 {@code Short}。
     * @return  如果此 {@code Short} 等于参数 {@code Short}，则返回值为 {@code 0}；
     *          如果此 {@code Short} 数值上小于参数 {@code Short}，则返回值小于 {@code 0}；
     *          如果此 {@code Short} 数值上大于参数 {@code Short}，则返回值大于 {@code 0}（有符号比较）。
     * @since   1.2
     */
    public int compareTo(Short anotherShort) {
        return compare(this.value, anotherShort.value);
    }

    /**
     * 比较两个 {@code short} 值的数值。
     * 返回的值与以下代码返回的值相同：
     * <pre>
     *    Short.valueOf(x).compareTo(Short.valueOf(y))
     * </pre>
     *
     * @param  x 要比较的第一个 {@code short}
     * @param  y 要比较的第二个 {@code short}
     * @return 如果 {@code x == y}，则返回值为 {@code 0}；
     *         如果 {@code x < y}，则返回值小于 {@code 0}；
     *         如果 {@code x > y}，则返回值大于 {@code 0}
     * @since 1.7
     */
    public static int compare(short x, short y) {
        return x - y;
    }

    /**
     * 用于表示 {@code short} 值的二进制补码形式的位数。
     * @since 1.5
     */
    public static final int SIZE = 16;

    /**
     * 用于表示 {@code short} 值的二进制补码形式的字节数。
     *
     * @since 1.8
     */
    public static final int BYTES = SIZE / Byte.SIZE;

    /**
     * 返回通过反转指定 {@code short} 值的二进制补码表示中的字节顺序所得到的值。
     *
     * @param i 要反转字节的值
     * @return 通过反转（或等效地交换）指定 {@code short} 值中的字节所得到的值。
     * @since 1.5
     */
    public static short reverseBytes(short i) {
        return (short) (((i & 0xFF00) >> 8) | (i << 8));
    }


    /**
     * 通过无符号转换将参数转换为 {@code int}。在无符号转换为 {@code int} 时，{@code int} 的高 16 位为零，
     * 低 16 位等于 {@code short} 参数的位。
     *
     * 因此，零和正的 {@code short} 值被映射到数值上相等的 {@code int} 值，而负的 {@code short} 值被映射到
     * 等于输入值加上 2<sup>16</sup> 的 {@code int} 值。
     *
     * @param  x 要转换为无符号 {@code int} 的值
     * @return 通过无符号转换将参数转换为 {@code int} 的值
     * @since 1.8
     */
    public static int toUnsignedInt(short x) {
        return ((int) x) & 0xffff;
    }

    /**
     * 通过无符号转换将参数转换为 {@code long}。在无符号转换为 {@code long} 时，{@code long} 的高 48 位为零，
     * 低 16 位等于 {@code short} 参数的位。
     *
     * 因此，零和正的 {@code short} 值被映射到数值上相等的 {@code long} 值，而负的 {@code short} 值被映射到
     * 等于输入值加上 2<sup>16</sup> 的 {@code long} 值。
     *
     * @param  x 要转换为无符号 {@code long} 的值
     * @return 通过无符号转换将参数转换为 {@code long} 的值
     * @since 1.8
     */
    public static long toUnsignedLong(short x) {
        return ((long) x) & 0xffffL;
    }

    /** 使用 JDK 1.1 的 serialVersionUID 以实现互操作性 */
    private static final long serialVersionUID = 7515723908773894738L;
}
