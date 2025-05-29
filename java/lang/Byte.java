/*
 * 版权所有 (c) 1996, 2013，Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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

package java.lang;

/**
 * {@code Byte} 类将原始类型 {@code byte} 的值包装在一个对象中。一个类型为 {@code Byte} 的对象包含一个类型为 {@code byte} 的单个字段。
 *
 * <p>此外，此类还提供了几种将 {@code byte} 转换为 {@code String} 和将 {@code String} 转换为 {@code byte} 的方法，以及其他在处理 {@code byte} 时有用的常量和方法。
 *
 * @author  Nakul Saraiya
 * @author  Joseph D. Darcy
 * @see     java.lang.Number
 * @since   JDK1.1
 */
public final class Byte extends Number implements Comparable<Byte> {

    /**
     * 一个常量，表示 {@code byte} 可以拥有的最小值，-2^7。
     */
    public static final byte   MIN_VALUE = -128;

    /**
     * 一个常量，表示 {@code byte} 可以拥有的最大值，2^7-1。
     */
    public static final byte   MAX_VALUE = 127;

    /**
     * 表示原始类型 {@code byte} 的 {@code Class} 实例。
     */
    @SuppressWarnings("unchecked")
    public static final Class<Byte>     TYPE = (Class<Byte>) Class.getPrimitiveClass("byte");

    /**
     * 返回一个表示指定 {@code byte} 的新 {@code String} 对象。基数假定为 10。
     *
     * @param b 要转换的 {@code byte}
     * @return 指定 {@code byte} 的字符串表示形式
     * @see java.lang.Integer#toString(int)
     */
    public static String toString(byte b) {
        return Integer.toString((int)b, 10);
    }

    private static class ByteCache {
        private ByteCache(){}

        static final Byte cache[] = new Byte[-(-128) + 127 + 1];

        static {
            for(int i = 0; i < cache.length; i++)
                cache[i] = new Byte((byte)(i - 128));
        }
    }

    /**
     * 返回一个表示指定 {@code byte} 值的 {@code Byte} 实例。
     * 如果不需要新的 {@code Byte} 实例，通常应优先使用此方法而不是构造函数 {@link #Byte(byte)}，因为此方法由于缓存了所有 byte 值，因此在空间和时间性能上可能会显著更好。
     *
     * @param  b 一个 byte 值。
     * @return 一个表示 {@code b} 的 {@code Byte} 实例。
     * @since  1.5
     */
    public static Byte valueOf(byte b) {
        final int offset = 128;
        return ByteCache.cache[(int)b + offset];
    }

    /**
     * 将字符串参数解析为指定基数的带符号 {@code byte}。字符串中的字符必须都是指定基数的数字（由 {@link java.lang.Character#digit(char, int)} 是否返回非负值确定），但第一个字符可以是 ASCII 减号 {@code '-'} ({@code '\u005Cu002D'}) 表示负值或 ASCII 加号 {@code '+'} ({@code '\u005Cu002B'}) 表示正值。返回解析的 {@code byte} 值。
     *
     * <p>如果发生以下任何情况，将抛出类型为 {@code NumberFormatException} 的异常：
     * <ul>
     * <li> 第一个参数为 {@code null} 或是一个长度为零的字符串。
     *
     * <li> 基数小于 {@link java.lang.Character#MIN_RADIX} 或大于 {@link java.lang.Character#MAX_RADIX}。
     *
     * <li> 字符串中的任何字符都不是指定基数的数字，但第一个字符可以是减号 {@code '-'} ({@code '\u005Cu002D'}) 或加号 {@code '+'} ({@code '\u005Cu002B'})，前提是字符串长度大于 1。
     *
     * <li> 字符串表示的值不是 {@code byte} 类型的值。
     * </ul>
     *
     * @param s 包含要解析的 {@code byte} 表示形式的 {@code String}
     * @param radix 解析 {@code s} 时使用的基数
     * @return 指定基数中字符串参数表示的 {@code byte} 值
     * @throws NumberFormatException 如果字符串不包含可解析的 {@code byte}。
     */
    public static byte parseByte(String s, int radix)
        throws NumberFormatException {
        int i = Integer.parseInt(s, radix);
        if (i < MIN_VALUE || i > MAX_VALUE)
            throw new NumberFormatException(
                "值超出范围。值:\"" + s + "\" 基数:" + radix);
        return (byte)i;
    }

    /**
     * 将字符串参数解析为带符号的十进制 {@code byte}。字符串中的字符必须都是十进制数字，但第一个字符可以是 ASCII 减号 {@code '-'} ({@code '\u005Cu002D'}) 表示负值或 ASCII 加号 {@code '+'} ({@code '\u005Cu002B'}) 表示正值。返回解析的 {@code byte} 值，就像将参数和基数 10 作为参数传递给 {@link #parseByte(java.lang.String, int)} 方法一样。
     *
     * @param s 包含要解析的 {@code byte} 表示形式的 {@code String}
     * @return 字符串参数在十进制中表示的 {@code byte} 值
     * @throws NumberFormatException 如果字符串不包含可解析的 {@code byte}。
     */
    public static byte parseByte(String s) throws NumberFormatException {
        return parseByte(s, 10);
    }

    /**
     * 返回一个包含从指定 {@code String} 解析出的值的 {@code Byte} 对象。第一个参数被解释为指定基数的带符号 {@code byte}，就像将参数传递给 {@link #parseByte(java.lang.String, int)} 方法一样。结果是一个表示字符串指定的 {@code byte} 值的 {@code Byte} 对象。
     *
     * <p>换句话说，此方法返回一个等于以下值的 {@code Byte} 对象：
     *
     * <blockquote>
     * {@code new Byte(Byte.parseByte(s, radix))}
     * </blockquote>
     *
     * @param s 要解析的字符串
     * @param radix 解释 {@code s} 时使用的基数
     * @return 包含字符串参数在指定基数中表示的值的 {@code Byte} 对象。
     * @throws NumberFormatException 如果 {@code String} 不包含可解析的 {@code byte}。
     */
    public static Byte valueOf(String s, int radix)
        throws NumberFormatException {
        return valueOf(parseByte(s, radix));
    }

    /**
     * 返回一个包含指定 {@code String} 给出的值的 {@code Byte} 对象。参数被解释为带符号的十进制 {@code byte}，就像将参数传递给 {@link #parseByte(java.lang.String)} 方法一样。结果是一个表示字符串指定的 {@code byte} 值的 {@code Byte} 对象。
     *
     * <p>换句话说，此方法返回一个等于以下值的 {@code Byte} 对象：
     *
     * <blockquote>
     * {@code new Byte(Byte.parseByte(s))}
     * </blockquote>
     *
     * @param s 要解析的字符串
     * @return 包含字符串参数表示的值的 {@code Byte} 对象
     * @throws NumberFormatException 如果 {@code String} 不包含可解析的 {@code byte}。
     */
    public static Byte valueOf(String s) throws NumberFormatException {
        return valueOf(s, 10);
    }

    /**
     * 将 {@code String} 解码为 {@code Byte}。接受十进制、十六进制和八进制数字，由以下语法定义：
     *
     * <blockquote>
     * <dl>
     * <dt><i>可解码字符串：</i>
     * <dd><i>可选符号 十进制数字</i>
     * <dd><i>可选符号 0x 十六进制数字</i>
     * <dd><i>可选符号 0X 十六进制数字</i>
     * <dd><i>可选符号 # 十六进制数字</i>
     * <dd><i>可选符号 0 八进制数字</i>
     *
     * <dt><i>符号：</i>
     * <dd>{@code -}
     * <dd>{@code +}
     * </dl>
     * </blockquote>
     *
     * <i>十进制数字</i>、<i>十六进制数字</i> 和 <i>八进制数字</i> 的定义见《Java&trade; 语言规范》第 3.10.1 节，但数字之间不允许使用下划线。
     *
     * <p>可选符号和/或基数指定符（“{@code 0x}”、“{@code 0X}”、“{@code #}”或前导零）之后的字符序列由 {@code Byte.parseByte} 方法以指定的基数（10、16 或 8）解析。此字符序列必须表示一个正值，否则将抛出 {@link NumberFormatException}。如果指定的 {@code String} 的第一个字符是减号，则结果取反。字符串中不允许有空白字符。
     *
     * @param nm 要解码的 {@code String}。
     * @return 包含 {@code nm} 表示的 {@code byte} 值的 {@code Byte} 对象
     * @throws NumberFormatException 如果 {@code String} 不包含可解析的 {@code byte}。
     * @see java.lang.Byte#parseByte(java.lang.String, int)
     */
    public static Byte decode(String nm) throws NumberFormatException {
        int i = Integer.decode(nm);
        if (i < MIN_VALUE || i > MAX_VALUE)
            throw new NumberFormatException(
                    "值 " + i + " 超出范围，输入 " + nm);
        return valueOf((byte)i);
    }

    /**
     * {@code Byte} 的值。
     *
     * @serial
     */
    private final byte value;

    /**
     * 构造一个新分配的表示指定 {@code byte} 值的 {@code Byte} 对象。
     *
     * @param value 要由 {@code Byte} 表示的值。
     */
    public Byte(byte value) {
        this.value = value;
    }

    /**
     * 构造一个新分配的表示由 {@code String} 参数指示的 {@code byte} 值的 {@code Byte} 对象。字符串以与基数 10 的 {@code parseByte} 方法完全相同的方式转换为 {@code byte} 值。
     *
     * @param s 要转换为 {@code Byte} 的 {@code String}
     * @throws NumberFormatException 如果 {@code String} 不包含可解析的 {@code byte}。
     * @see java.lang.Byte#parseByte(java.lang.String, int)
     */
    public Byte(String s) throws NumberFormatException {
        this.value = parseByte(s, 10);
    }

    /**
     * 返回此 {@code Byte} 的值作为 {@code byte}。
     */
    public byte byteValue() {
        return value;
    }

    /**
     * 返回此 {@code Byte} 的值作为扩展原始转换后的 {@code short}。
     * @jls 5.1.2 扩展原始转换
     */
    public short shortValue() {
        return (short)value;
    }

    /**
     * 返回此 {@code Byte} 的值作为扩展原始转换后的 {@code int}。
     * @jls 5.1.2 扩展原始转换
     */
    public int intValue() {
        return (int)value;
    }

    /**
     * 返回此 {@code Byte} 的值作为扩展原始转换后的 {@code long}。
     * @jls 5.1.2 扩展原始转换
     */
    public long longValue() {
        return (long)value;
    }

    /**
     * 返回此 {@code Byte} 的值作为扩展原始转换后的 {@code float}。
     * @jls 5.1.2 扩展原始转换
     */
    public float floatValue() {
        return (float)value;
    }

    /**
     * 返回此 {@code Byte} 的值作为扩展原始转换后的 {@code double}。
     * @jls 5.1.2 扩展原始转换
     */
    public double doubleValue() {
        return (double)value;
    }

    /**
     * 返回表示此 {@code Byte} 值的 {@code String} 对象。值被转换为带符号的十进制表示形式并作为字符串返回，就像将 {@code byte} 值作为参数传递给 {@link java.lang.Byte#toString(byte)} 方法一样。
     *
     * @return 以 10 为基数的此对象值的字符串表示形式。
     */
    public String toString() {
        return Integer.toString((int)value);
    }

    /**
     * 返回此 {@code Byte} 的哈希码；等于调用 {@code intValue()} 的结果。
     *
     * @return 此 {@code Byte} 的哈希码值
     */
    @Override
    public int hashCode() {
        return Byte.hashCode(value);
    }

    /**
     * 返回 {@code byte} 值的哈希码；与 {@code Byte.hashCode()} 兼容。
     *
     * @param value 要哈希的值
     * @return {@code byte} 值的哈希码值。
     * @since 1.8
     */
    public static int hashCode(byte value) {
        return (int)value;
    }

    /**
     * 将此对象与指定对象进行比较。结果为 {@code true} 当且仅当参数不为 {@code null} 且是一个包含与此对象相同的 {@code byte} 值的 {@code Byte} 对象。
     *
     * @param obj 要比较的对象
     * @return 如果对象相同，则返回 {@code true}；否则返回 {@code false}。
     */
    public boolean equals(Object obj) {
        if (obj instanceof Byte) {
            return value == ((Byte)obj).byteValue();
        }
        return false;
    }

    /**
     * 数值比较两个 {@code Byte} 对象。
     *
     * @param anotherByte 要比较的 {@code Byte}
     * @return 如果此 {@code Byte} 等于参数 {@code Byte}，则返回值为 {@code 0}；如果此 {@code Byte} 数值上小于参数 {@code Byte}，则返回值小于 {@code 0}；如果此 {@code Byte} 数值上大于参数 {@code Byte}，则返回值大于 {@code 0}（带符号比较）。
     * @since 1.2
     */
    public int compareTo(Byte anotherByte) {
        return compare(this.value, anotherByte.value);
    }

    /**
     * 数值比较两个 {@code byte} 值。
     * 返回的值与以下代码返回的值相同：
     * <pre>
     *    Byte.valueOf(x).compareTo(Byte.valueOf(y))
     * </pre>
     *
     * @param x 要比较的第一个 {@code byte}
     * @param y 要比较的第二个 {@code byte}
     * @return 如果 {@code x == y}，则返回值为 {@code 0}；如果 {@code x < y}，则返回值小于 {@code 0}；如果 {@code x > y}，则返回值大于 {@code 0}
     * @since 1.7
     */
    public static int compare(byte x, byte y) {
        return x - y;
    }

    /**
     * 通过无符号转换将参数转换为 {@code int}。在将 {@code byte} 转换为 {@code int} 的无符号转换中，{@code int} 的高 24 位为零，低 8 位等于 {@code byte} 参数的位。
     *
     * 因此，零和正的 {@code byte} 值映射到数值相等的 {@code int} 值，而负的 {@code byte} 值映射到等于输入加上 2^8 的 {@code int} 值。
     *
     * @param x 要转换为无符号 {@code int} 的值
     * @return 通过无符号转换将参数转换为 {@code int} 的结果
     * @since 1.8
     */
    public static int toUnsignedInt(byte x) {
        return ((int) x) & 0xff;
    }

    /**
     * 通过无符号转换将参数转换为 {@code long}。在将 {@code byte} 转换为 {@code long} 的无符号转换中，{@code long} 的高 56 位为零，低 8 位等于 {@code byte} 参数的位。
     *
     * 因此，零和正的 {@code byte} 值映射到数值相等的 {@code long} 值，而负的 {@code byte} 值映射到等于输入加上 2^8 的 {@code long} 值。
     *
     * @param x 要转换为无符号 {@code long} 的值
     * @return 通过无符号转换将参数转换为 {@code long} 的结果
     * @since 1.8
     */
    public static long toUnsignedLong(byte x) {
        return ((long) x) & 0xffL;
    }

    /**
     * 以二进制补码形式表示 {@code byte} 值所需的位数。
     *
     * @since 1.5
     */
    public static final int SIZE = 8;

    /**
     * 以二进制补码形式表示 {@code byte} 值所需的字节数。
     *
     * @since 1.8
     */
    public static final int BYTES = SIZE / Byte.SIZE;

    /** 为了互操作性使用 JDK 1.1 的 serialVersionUID */
    private static final long serialVersionUID = -7183698231559129828L;
}