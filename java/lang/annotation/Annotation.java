/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.annotation;

/**
 * 所有注解类型扩展的公共接口。注意，手动扩展此接口的接口不会定义注解类型。此外，此接口本身也不定义注解类型。
 *
 * 更多关于注解类型的信息可以在《Java&trade; 语言规范》的第 9.6 节中找到。
 *
 * {@link java.lang.reflect.AnnotatedElement} 接口讨论了当将注解类型从不可重复变为可重复时的兼容性问题。
 *
 * @author  Josh Bloch
 * @since   1.5
 */
public interface Annotation {
    /**
     * 如果指定的对象表示一个逻辑上等价于此注解的注解，则返回 true。换句话说，如果指定的对象是与此实例属于同一注解类型的实例，并且其所有成员都与此注解的相应成员相等（如下定义），则返回 true：
     * <ul>
     *    <li>两个对应的基本类型成员，其值分别为 <tt>x</tt> 和 <tt>y</tt>，如果 <tt>x == y</tt>，则认为相等，除非它们的类型是 <tt>float</tt> 或 <tt>double</tt>。
     *
     *    <li>两个对应的 <tt>float</tt> 成员，其值分别为 <tt>x</tt> 和 <tt>y</tt>，如果 <tt>Float.valueOf(x).equals(Float.valueOf(y))</tt>，则认为相等。
     *    （与 <tt>==</tt> 运算符不同，NaN 被认为等于自身，而 <tt>0.0f</tt> 不等于 <tt>-0.0f</tt>。）
     *
     *    <li>两个对应的 <tt>double</tt> 成员，其值分别为 <tt>x</tt> 和 <tt>y</tt>，如果 <tt>Double.valueOf(x).equals(Double.valueOf(y))</tt>，则认为相等。
     *    （与 <tt>==</tt> 运算符不同，NaN 被认为等于自身，而 <tt>0.0</tt> 不等于 <tt>-0.0</tt>。）
     *
     *    <li>两个对应的 <tt>String</tt>、<tt>Class</tt>、枚举或注解类型成员，其值分别为 <tt>x</tt> 和 <tt>y</tt>，如果 <tt>x.equals(y)</tt>，则认为相等。（对于注解类型成员，此定义是递归的。）
     *
     *    <li>两个对应的数组类型成员 <tt>x</tt> 和 <tt>y</tt>，如果 <tt>Arrays.equals(x, y)</tt>，则认为相等，使用适当的 {@link java.util.Arrays#equals} 重载方法。
     * </ul>
     *
     * @return 如果指定的对象表示一个逻辑上等价于此注解的注解，则返回 true，否则返回 false
     */
    boolean equals(Object obj);

    /**
     * 返回此注解的哈希码，如下定义：
     *
     * <p>注解的哈希码是其成员（包括具有默认值的成员）的哈希码之和，如下定义：
     *
     * 注解成员的哈希码是（127 乘以成员名的哈希码，由 {@link String#hashCode()} 计算）XOR 成员值的哈希码，如下定义：
     *
     * <p>成员值的哈希码取决于其类型：
     * <ul>
     * <li>基本值 <tt><i>v</i></tt> 的哈希码等于 <tt><i>WrapperType</i>.valueOf(<i>v</i>).hashCode()</tt>，其中 <tt><i>WrapperType</i></tt> 是对应于 <tt><i>v</i></tt> 的基本类型的包装类型（{@link Byte}、{@link Character}、{@link Double}、{@link Float}、{@link Integer}、{@link Long}、{@link Short} 或 {@link Boolean}）。
     *
     * <li>字符串、枚举、类或注解成员值 <tt><i>v</i></tt> 的哈希码通过调用 <tt><i>v</i>.hashCode()</tt> 计算。（对于注解成员值，这是一个递归定义。）
     *
     * <li>数组成员值的哈希码通过调用适当的 {@link java.util.Arrays#hashCode(long[]) Arrays.hashCode} 重载方法计算。（每个基本类型有一个重载方法，对象引用类型也有一个。）
     * </ul>
     *
     * @return 此注解的哈希码
     */
    int hashCode();

    /**
     * 返回此注解的字符串表示形式。表示形式的细节取决于实现，但以下内容可以视为典型：
     * <pre>
     *   &#064;com.acme.util.Name(first=Alfred, middle=E., last=Neuman)
     * </pre>
     *
     * @return 此注解的字符串表示形式
     */
    String toString();

    /**
     * 返回此注解的注解类型。
     * @return 此注解的注解类型
     */
    Class<? extends Annotation> annotationType();
}
