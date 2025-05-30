
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

package java.lang.reflect;

/**
 * {@code Array} 类提供了静态方法来动态创建和访问 Java 数组。
 *
 * <p>{@code Array} 允许在获取或设置操作期间发生扩展转换，但如果发生收缩转换，则会抛出 {@code IllegalArgumentException}。
 *
 * @author Nakul Saraiya
 */
public final
class Array {

    /**
     * 构造函数。类 Array 不能实例化。
     */
    private Array() {}

    /**
     * 创建具有指定组件类型和长度的新数组。
     * 调用此方法等同于如下创建数组：
     * <blockquote>
     * <pre>
     * int[] x = {length};
     * Array.newInstance(componentType, x);
     * </pre>
     * </blockquote>
     *
     * <p>新数组的维度数不得超过 255。
     *
     * @param componentType 表示新数组组件类型的 {@code Class} 对象
     * @param length 新数组的长度
     * @return 新数组
     * @exception NullPointerException 如果指定的 {@code componentType} 参数为 null
     * @exception IllegalArgumentException 如果 componentType 是 {@link
     * Void#TYPE} 或请求的数组实例的维度数超过 255。
     * @exception NegativeArraySizeException 如果指定的 {@code length} 为负数
     */
    public static Object newInstance(Class<?> componentType, int length)
        throws NegativeArraySizeException {
        return newArray(componentType, length);
    }

    /**
     * 创建具有指定组件类型和维度的新数组。
     * 如果 {@code componentType}
     * 表示非数组类或接口，新数组将具有 {@code dimensions.length} 维度和
     * {@code componentType} 作为其组件类型。如果
     * {@code componentType} 表示数组类，新数组的维度数等于
     * {@code dimensions.length} 和 {@code componentType} 的维度数之和。在这种情况下，新数组的组件类型为
     * {@code componentType} 的组件类型。
     *
     * <p>新数组的维度数不得超过 255。
     *
     * @param componentType 表示新数组组件类型的 {@code Class} 对象
     * @param dimensions 表示新数组维度的 {@code int} 数组
     * @return 新数组
     * @exception NullPointerException 如果指定的 {@code componentType} 参数为 null
     * @exception IllegalArgumentException 如果指定的 {@code dimensions} 参数是零维数组，如果 componentType 是 {@link
     * Void#TYPE}，或者请求的数组实例的维度数超过 255。
     * @exception NegativeArraySizeException 如果指定的 {@code dimensions} 参数中的任何组件为负数。
     */
    public static Object newInstance(Class<?> componentType, int... dimensions)
        throws IllegalArgumentException, NegativeArraySizeException {
        return multiNewArray(componentType, dimensions);
    }

    /**
     * 返回指定数组对象的长度，作为 {@code int}。
     *
     * @param array 数组
     * @return 数组的长度
     * @exception IllegalArgumentException 如果对象参数不是数组
     */
    public static native int getLength(Object array)
        throws IllegalArgumentException;

    /**
     * 返回指定数组对象中索引组件的值。如果值具有基本类型，则自动包装在对象中。
     *
     * @param array 数组
     * @param index 索引
     * @return 指定数组中索引组件的值（可能已包装）
     * @exception NullPointerException 如果指定的对象为 null
     * @exception IllegalArgumentException 如果指定的对象不是数组
     * @exception ArrayIndexOutOfBoundsException 如果指定的 {@code index} 参数为负数，或者大于或等于指定数组的长度
     */
    public static native Object get(Object array, int index)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    /**
     * 返回指定数组对象中索引组件的值，作为 {@code boolean}。
     *
     * @param array 数组
     * @param index 索引
     * @return 指定数组中索引组件的值
     * @exception NullPointerException 如果指定的对象为 null
     * @exception IllegalArgumentException 如果指定的对象不是数组，或者索引元素不能通过身份或扩展转换转换为返回类型
     * @exception ArrayIndexOutOfBoundsException 如果指定的 {@code index} 参数为负数，或者大于或等于指定数组的长度
     * @see Array#get
     */
    public static native boolean getBoolean(Object array, int index)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    /**
     * 返回指定数组对象中索引组件的值，作为 {@code byte}。
     *
     * @param array 数组
     * @param index 索引
     * @return 指定数组中索引组件的值
     * @exception NullPointerException 如果指定的对象为 null
     * @exception IllegalArgumentException 如果指定的对象不是数组，或者索引元素不能通过身份或扩展转换转换为返回类型
     * @exception ArrayIndexOutOfBoundsException 如果指定的 {@code index} 参数为负数，或者大于或等于指定数组的长度
     * @see Array#get
     */
    public static native byte getByte(Object array, int index)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    /**
     * 返回指定数组对象中索引组件的值，作为 {@code char}。
     *
     * @param array 数组
     * @param index 索引
     * @return 指定数组中索引组件的值
     * @exception NullPointerException 如果指定的对象为 null
     * @exception IllegalArgumentException 如果指定的对象不是数组，或者索引元素不能通过身份或扩展转换转换为返回类型
     * @exception ArrayIndexOutOfBoundsException 如果指定的 {@code index} 参数为负数，或者大于或等于指定数组的长度
     * @see Array#get
     */
    public static native char getChar(Object array, int index)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    /**
     * 返回指定数组对象中索引组件的值，作为 {@code short}。
     *
     * @param array 数组
     * @param index 索引
     * @return 指定数组中索引组件的值
     * @exception NullPointerException 如果指定的对象为 null
     * @exception IllegalArgumentException 如果指定的对象不是数组，或者索引元素不能通过身份或扩展转换转换为返回类型
     * @exception ArrayIndexOutOfBoundsException 如果指定的 {@code index} 参数为负数，或者大于或等于指定数组的长度
     * @see Array#get
     */
    public static native short getShort(Object array, int index)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    /**
     * 返回指定数组对象中索引组件的值，作为 {@code int}。
     *
     * @param array 数组
     * @param index 索引
     * @return 指定数组中索引组件的值
     * @exception NullPointerException 如果指定的对象为 null
     * @exception IllegalArgumentException 如果指定的对象不是数组，或者索引元素不能通过身份或扩展转换转换为返回类型
     * @exception ArrayIndexOutOfBoundsException 如果指定的 {@code index} 参数为负数，或者大于或等于指定数组的长度
     * @see Array#get
     */
    public static native int getInt(Object array, int index)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    /**
     * 返回指定数组对象中索引组件的值，作为 {@code long}。
     *
     * @param array 数组
     * @param index 索引
     * @return 指定数组中索引组件的值
     * @exception NullPointerException 如果指定的对象为 null
     * @exception IllegalArgumentException 如果指定的对象不是数组，或者索引元素不能通过身份或扩展转换转换为返回类型
     * @exception ArrayIndexOutOfBoundsException 如果指定的 {@code index} 参数为负数，或者大于或等于指定数组的长度
     * @see Array#get
     */
    public static native long getLong(Object array, int index)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    /**
     * 返回指定数组对象中索引组件的值，作为 {@code float}。
     *
     * @param array 数组
     * @param index 索引
     * @return 指定数组中索引组件的值
     * @exception NullPointerException 如果指定的对象为 null
     * @exception IllegalArgumentException 如果指定的对象不是数组，或者索引元素不能通过身份或扩展转换转换为返回类型
     * @exception ArrayIndexOutOfBoundsException 如果指定的 {@code index} 参数为负数，或者大于或等于指定数组的长度
     * @see Array#get
     */
    public static native float getFloat(Object array, int index)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    /**
     * 返回指定数组对象中索引组件的值，作为 {@code double}。
     *
     * @param array 数组
     * @param index 索引
     * @return 指定数组中索引组件的值
     * @exception NullPointerException 如果指定的对象为 null
     * @exception IllegalArgumentException 如果指定的对象不是数组，或者索引元素不能通过身份或扩展转换转换为返回类型
     * @exception ArrayIndexOutOfBoundsException 如果指定的 {@code index} 参数为负数，或者大于或等于指定数组的长度
     * @see Array#get
     */
    public static native double getDouble(Object array, int index)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    /**
     * 将指定数组对象中索引组件的值设置为指定的新值。如果数组具有基本组件类型，则新值首先自动拆箱。
     * @param array 数组
     * @param index 数组中的索引
     * @param value 索引组件的新值
     * @exception NullPointerException 如果指定的对象参数为 null
     * @exception IllegalArgumentException 如果指定的对象参数不是数组，或者数组组件类型为基本类型且拆箱转换失败
     * @exception ArrayIndexOutOfBoundsException 如果指定的 {@code index} 参数为负数，或者大于或等于指定数组的长度
     */
    public static native void set(Object array, int index, Object value)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    /**
     * 将指定数组对象中索引组件的值设置为指定的 {@code boolean} 值。
     * @param array 数组
     * @param index 数组中的索引
     * @param z 索引组件的新值
     * @exception NullPointerException 如果指定的对象参数为 null
     * @exception IllegalArgumentException 如果指定的对象参数不是数组，或者指定的值不能通过身份或基本扩展转换转换为底层数组的组件类型
     * @exception ArrayIndexOutOfBoundsException 如果指定的 {@code index} 参数为负数，或者大于或等于指定数组的长度
     * @see Array#set
     */
    public static native void setBoolean(Object array, int index, boolean z)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    /**
     * 将指定数组对象中索引组件的值设置为指定的 {@code byte} 值。
     * @param array 数组
     * @param index 数组中的索引
     * @param b 索引组件的新值
     * @exception NullPointerException 如果指定的对象参数为 null
     * @exception IllegalArgumentException 如果指定的对象参数不是数组，或者指定的值不能通过身份或基本扩展转换转换为底层数组的组件类型
     * @exception ArrayIndexOutOfBoundsException 如果指定的 {@code index} 参数为负数，或者大于或等于指定数组的长度
     * @see Array#set
     */
    public static native void setByte(Object array, int index, byte b)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException;


                /**
     * 设置指定数组对象的索引组件的值为指定的 {@code char} 值。
     * @param array 数组
     * @param index 数组中的索引
     * @param c 索引组件的新值
     * @exception NullPointerException 如果指定的对象参数为 null
     * @exception IllegalArgumentException 如果指定的对象参数不是数组，或者指定的值不能通过身份转换或原始类型扩展转换转换为底层数组的组件类型
     * @exception ArrayIndexOutOfBoundsException 如果指定的 {@code index} 参数为负数，或者大于或等于指定数组的长度
     * @see Array#set
     */
    public static native void setChar(Object array, int index, char c)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    /**
     * 设置指定数组对象的索引组件的值为指定的 {@code short} 值。
     * @param array 数组
     * @param index 数组中的索引
     * @param s 索引组件的新值
     * @exception NullPointerException 如果指定的对象参数为 null
     * @exception IllegalArgumentException 如果指定的对象参数不是数组，或者指定的值不能通过身份转换或原始类型扩展转换转换为底层数组的组件类型
     * @exception ArrayIndexOutOfBoundsException 如果指定的 {@code index} 参数为负数，或者大于或等于指定数组的长度
     * @see Array#set
     */
    public static native void setShort(Object array, int index, short s)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    /**
     * 设置指定数组对象的索引组件的值为指定的 {@code int} 值。
     * @param array 数组
     * @param index 数组中的索引
     * @param i 索引组件的新值
     * @exception NullPointerException 如果指定的对象参数为 null
     * @exception IllegalArgumentException 如果指定的对象参数不是数组，或者指定的值不能通过身份转换或原始类型扩展转换转换为底层数组的组件类型
     * @exception ArrayIndexOutOfBoundsException 如果指定的 {@code index} 参数为负数，或者大于或等于指定数组的长度
     * @see Array#set
     */
    public static native void setInt(Object array, int index, int i)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    /**
     * 设置指定数组对象的索引组件的值为指定的 {@code long} 值。
     * @param array 数组
     * @param index 数组中的索引
     * @param l 索引组件的新值
     * @exception NullPointerException 如果指定的对象参数为 null
     * @exception IllegalArgumentException 如果指定的对象参数不是数组，或者指定的值不能通过身份转换或原始类型扩展转换转换为底层数组的组件类型
     * @exception ArrayIndexOutOfBoundsException 如果指定的 {@code index} 参数为负数，或者大于或等于指定数组的长度
     * @see Array#set
     */
    public static native void setLong(Object array, int index, long l)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    /**
     * 设置指定数组对象的索引组件的值为指定的 {@code float} 值。
     * @param array 数组
     * @param index 数组中的索引
     * @param f 索引组件的新值
     * @exception NullPointerException 如果指定的对象参数为 null
     * @exception IllegalArgumentException 如果指定的对象参数不是数组，或者指定的值不能通过身份转换或原始类型扩展转换转换为底层数组的组件类型
     * @exception ArrayIndexOutOfBoundsException 如果指定的 {@code index} 参数为负数，或者大于或等于指定数组的长度
     * @see Array#set
     */
    public static native void setFloat(Object array, int index, float f)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    /**
     * 设置指定数组对象的索引组件的值为指定的 {@code double} 值。
     * @param array 数组
     * @param index 数组中的索引
     * @param d 索引组件的新值
     * @exception NullPointerException 如果指定的对象参数为 null
     * @exception IllegalArgumentException 如果指定的对象参数不是数组，或者指定的值不能通过身份转换或原始类型扩展转换转换为底层数组的组件类型
     * @exception ArrayIndexOutOfBoundsException 如果指定的 {@code index} 参数为负数，或者大于或等于指定数组的长度
     * @see Array#set
     */
    public static native void setDouble(Object array, int index, double d)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    /*
     * Private
     */

    private static native Object newArray(Class<?> componentType, int length)
        throws NegativeArraySizeException;

    private static native Object multiNewArray(Class<?> componentType,
        int[] dimensions)
        throws IllegalArgumentException, NegativeArraySizeException;


}
