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

/**
 * Boolean 类将基本类型 {@code boolean} 的值包装在一个对象中。一个类型为
 * {@code Boolean} 的对象包含一个类型为 {@code boolean} 的单个字段。
 * <p>
 * 此外，此类提供了许多方法，用于将 {@code boolean} 转换为 {@code String} 和
 * 将 {@code String} 转换为 {@code boolean}，以及其他在处理 {@code boolean} 时有用的常量和方法。
 *
 * @author  Arthur van Hoff
 * @since   JDK1.0
 */
public final class Boolean implements java.io.Serializable,
                                      Comparable<Boolean>
{
    /**
     * 对应于基本值 {@code true} 的 {@code Boolean} 对象。
     */
    public static final Boolean TRUE = new Boolean(true);

    /**
     * 对应于基本值 {@code false} 的 {@code Boolean} 对象。
     */
    public static final Boolean FALSE = new Boolean(false);

    /**
     * 表示基本类型 boolean 的 Class 对象。
     *
     * @since   JDK1.1
     */
    @SuppressWarnings("unchecked")
    public static final Class<Boolean> TYPE = (Class<Boolean>) Class.getPrimitiveClass("boolean");

    /**
     * Boolean 的值。
     *
     * @serial
     */
    private final boolean value;

    /** 使用 JDK 1.0.2 的 serialVersionUID 以确保互操作性 */
    private static final long serialVersionUID = -3665804199014368530L;

    /**
     * 分配一个表示 {@code value} 参数的 {@code Boolean} 对象。
     *
     * <p><b>注意：很少需要使用此构造函数。除非需要一个 <i>新</i> 实例，否则通常应使用静态工厂
     * {@link #valueOf(boolean)}。它可能会显著提高空间和时间性能。</b>
     *
     * @param   value   {@code Boolean} 的值。
     */
    public Boolean(boolean value) {
        this.value = value;
    }

    /**
     * 分配一个表示值 {@code true} 的 {@code Boolean} 对象，如果字符串参数不为 {@code null}
     * 且忽略大小写后等于字符串 {@code "true"}。否则，分配一个表示值 {@code false} 的
     * {@code Boolean} 对象。示例：<p>
     * {@code new Boolean("True")} 生成一个表示 {@code true} 的 {@code Boolean} 对象。<br>
     * {@code new Boolean("yes")} 生成一个表示 {@code false} 的 {@code Boolean} 对象。
     *
     * @param   s   要转换为 {@code Boolean} 的字符串。
     */
    public Boolean(String s) {
        this(parseBoolean(s));
    }

    /**
     * 将字符串参数解析为布尔值。返回的 {@code boolean} 表示如果字符串参数不为 {@code null}
     * 且忽略大小写后等于字符串 {@code "true"}。 <p>
     * 示例：{@code Boolean.parseBoolean("True")} 返回 {@code true}。<br>
     * 示例：{@code Boolean.parseBoolean("yes")} 返回 {@code false}。
     *
     * @param      s   包含要解析的布尔表示的 {@code String}
     * @return     由字符串参数表示的布尔值
     * @since 1.5
     */
    public static boolean parseBoolean(String s) {
        return ((s != null) && s.equalsIgnoreCase("true"));
    }

    /**
     * 返回此 {@code Boolean} 对象的值作为布尔基本类型。
     *
     * @return  此对象的原始 {@code boolean} 值。
     */
    public boolean booleanValue() {
        return value;
    }

    /**
     * 返回表示指定 {@code boolean} 值的 {@code Boolean} 实例。如果指定的 {@code boolean} 值
     * 是 {@code true}，此方法返回 {@code Boolean.TRUE}；如果它是 {@code false}，此方法返回
     * {@code Boolean.FALSE}。如果不需要新的 {@code Boolean} 实例，通常应优先使用此方法而不是构造函数
     * {@link #Boolean(boolean)}，因为此方法可能会显著提高空间和时间性能。
     *
     * @param  b 一个布尔值。
     * @return 一个表示 {@code b} 的 {@code Boolean} 实例。
     * @since  1.4
     */
    public static Boolean valueOf(boolean b) {
        return (b ? TRUE : FALSE);
    }

    /**
     * 返回表示由指定字符串表示的值的 {@code Boolean}。如果字符串参数不为 {@code null}
     * 且忽略大小写后等于字符串 {@code "true"}，则返回的 {@code Boolean} 表示一个真值。
     *
     * @param   s   一个字符串。
     * @return 由字符串表示的 {@code Boolean} 值。
     */
    public static Boolean valueOf(String s) {
        return parseBoolean(s) ? TRUE : FALSE;
    }

    /**
     * 返回表示指定布尔值的 {@code String} 对象。如果指定的布尔值是 {@code true}，则返回
     * 字符串 {@code "true"}，否则返回字符串 {@code "false"}。
     *
     * @param b 要转换的布尔值
     * @return 指定 {@code boolean} 的字符串表示形式
     * @since 1.4
     */
    public static String toString(boolean b) {
        return b ? "true" : "false";
    }

    /**
     * 返回表示此 Boolean 值的 {@code String} 对象。如果此对象表示的值是 {@code true}，
     * 则返回一个等于 {@code "true"} 的字符串。否则，返回一个等于 {@code "false"} 的字符串。
     *
     * @return  此对象的字符串表示形式。
     */
    public String toString() {
        return value ? "true" : "false";
    }

    /**
     * 返回此 {@code Boolean} 对象的哈希码。
     *
     * @return  如果此对象表示 {@code true}，则返回整数 {@code 1231}；如果此对象表示
     * {@code false}，则返回整数 {@code 1237}。
     */
    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }

    /**
     * 返回一个 {@code boolean} 值的哈希码；与 {@code Boolean.hashCode()} 兼容。
     *
     * @param value 要哈希的值
     * @return 一个 {@code boolean} 值的哈希码。
     * @since 1.8
     */
    public static int hashCode(boolean value) {
        return value ? 1231 : 1237;
    }

    /**
     * 如果且仅如果参数不是 {@code null} 并且是一个表示与此对象相同的 {@code boolean} 值的
     * {@code Boolean} 对象，则返回 {@code true}。
     *
     * @param   obj   要比较的对象。
     * @return  如果 Boolean 对象表示相同的值，则返回 {@code true}；否则返回 {@code false}。
     */
    public boolean equals(Object obj) {
        if (obj instanceof Boolean) {
            return value == ((Boolean)obj).booleanValue();
        }
        return false;
    }

    /**
     * 如果且仅如果以参数命名的系统属性存在且等于字符串 {@code "true"}，则返回 {@code true}。
     * （从 Java<small><sup>TM</sup></small> 平台的 1.0.2 版开始，此字符串的测试不区分大小写。）
     * 系统属性可以通过 {@code System} 类定义的 {@code getProperty} 方法访问。
     * <p>
     * 如果没有具有指定名称的属性，或者指定的名称为空或 null，则返回 {@code false}。
     *
     * @param   name   系统属性名称。
     * @return  系统属性的 {@code boolean} 值。
     * @throws  SecurityException 与 {@link System#getProperty(String) System.getProperty} 相同的原因
     * @see     java.lang.System#getProperty(java.lang.String)
     * @see     java.lang.System#getProperty(java.lang.String, java.lang.String)
     */
    public static boolean getBoolean(String name) {
        boolean result = false;
        try {
            result = parseBoolean(System.getProperty(name));
        } catch (IllegalArgumentException | NullPointerException e) {
        }
        return result;
    }

    /**
     * 比较此 {@code Boolean} 实例与另一个。
     *
     * @param   b 要比较的 {@code Boolean} 实例
     * @return  如果此对象表示与参数相同的布尔值，则返回零；如果此对象表示 true 而参数表示 false，则返回正值；
     *          如果此对象表示 false 而参数表示 true，则返回负值
     * @throws  NullPointerException 如果参数为 {@code null}
     * @see     Comparable
     * @since  1.5
     */
    public int compareTo(Boolean b) {
        return compare(this.value, b.value);
    }

    /**
     * 比较两个 {@code boolean} 值。返回的值与以下代码返回的值相同：
     * <pre>
     *    Boolean.valueOf(x).compareTo(Boolean.valueOf(y))
     * </pre>
     *
     * @param  x 第一个要比较的 {@code boolean}
     * @param  y 第二个要比较的 {@code boolean}
     * @return 如果 {@code x == y}，则返回 {@code 0}；如果 {@code !x && y}，则返回小于 {@code 0} 的值；
     *         如果 {@code x && !y}，则返回大于 {@code 0} 的值
     * @since 1.7
     */
    public static int compare(boolean x, boolean y) {
        return (x == y) ? 0 : (x ? 1 : -1);
    }

    /**
     * 返回对指定的 {@code boolean} 操作数应用逻辑 AND 操作符的结果。
     *
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @return  {@code a} 和 {@code b} 的逻辑 AND
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static boolean logicalAnd(boolean a, boolean b) {
        return a && b;
    }

    /**
     * 返回对指定的 {@code boolean} 操作数应用逻辑 OR 操作符的结果。
     *
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @return  {@code a} 和 {@code b} 的逻辑 OR
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static boolean logicalOr(boolean a, boolean b) {
        return a || b;
    }

    /**
     * 返回对指定的 {@code boolean} 操作数应用逻辑 XOR 操作符的结果。
     *
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @return  {@code a} 和 {@code b} 的逻辑 XOR
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static boolean logicalXor(boolean a, boolean b) {
        return a ^ b;
    }
}
