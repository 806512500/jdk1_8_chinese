
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

import java.security.AccessController;
import sun.reflect.LangReflectAccess;
import sun.reflect.ReflectionFactory;

/**
 * Modifier 类提供 {@code static} 方法和常量来解码类和成员访问修饰符。修饰符集表示为整数，不同的位表示不同的修饰符。修饰符常量的值取自《Java&trade; 虚拟机规范》第 4.1、4.4、4.5 和 4.7 节中的表。
 *
 * @see Class#getModifiers()
 * @see Member#getModifiers()
 *
 * @author Nakul Saraiya
 * @author Kenneth Russell
 */
public class Modifier {

    /*
     * java.lang 和 java.lang.reflect 包之间的启动协议
     */
    static {
        sun.reflect.ReflectionFactory factory =
            AccessController.doPrivileged(
                new ReflectionFactory.GetReflectionFactoryAction());
        factory.setLangReflectAccess(new java.lang.reflect.ReflectAccess());
    }

    /**
     * 如果整数参数包含 {@code public} 修饰符，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   mod 修饰符集
     * @return 如果 {@code mod} 包含 {@code public} 修饰符，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isPublic(int mod) {
        return (mod & PUBLIC) != 0;
    }

    /**
     * 如果整数参数包含 {@code private} 修饰符，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   mod 修饰符集
     * @return 如果 {@code mod} 包含 {@code private} 修饰符，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isPrivate(int mod) {
        return (mod & PRIVATE) != 0;
    }

    /**
     * 如果整数参数包含 {@code protected} 修饰符，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   mod 修饰符集
     * @return 如果 {@code mod} 包含 {@code protected} 修饰符，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isProtected(int mod) {
        return (mod & PROTECTED) != 0;
    }

    /**
     * 如果整数参数包含 {@code static} 修饰符，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   mod 修饰符集
     * @return 如果 {@code mod} 包含 {@code static} 修饰符，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isStatic(int mod) {
        return (mod & STATIC) != 0;
    }

    /**
     * 如果整数参数包含 {@code final} 修饰符，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   mod 修饰符集
     * @return 如果 {@code mod} 包含 {@code final} 修饰符，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isFinal(int mod) {
        return (mod & FINAL) != 0;
    }

    /**
     * 如果整数参数包含 {@code synchronized} 修饰符，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   mod 修饰符集
     * @return 如果 {@code mod} 包含 {@code synchronized} 修饰符，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isSynchronized(int mod) {
        return (mod & SYNCHRONIZED) != 0;
    }

    /**
     * 如果整数参数包含 {@code volatile} 修饰符，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   mod 修饰符集
     * @return 如果 {@code mod} 包含 {@code volatile} 修饰符，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isVolatile(int mod) {
        return (mod & VOLATILE) != 0;
    }

    /**
     * 如果整数参数包含 {@code transient} 修饰符，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   mod 修饰符集
     * @return 如果 {@code mod} 包含 {@code transient} 修饰符，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isTransient(int mod) {
        return (mod & TRANSIENT) != 0;
    }

    /**
     * 如果整数参数包含 {@code native} 修饰符，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   mod 修饰符集
     * @return 如果 {@code mod} 包含 {@code native} 修饰符，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isNative(int mod) {
        return (mod & NATIVE) != 0;
    }

    /**
     * 如果整数参数包含 {@code interface} 修饰符，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   mod 修饰符集
     * @return 如果 {@code mod} 包含 {@code interface} 修饰符，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isInterface(int mod) {
        return (mod & INTERFACE) != 0;
    }

    /**
     * 如果整数参数包含 {@code abstract} 修饰符，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   mod 修饰符集
     * @return 如果 {@code mod} 包含 {@code abstract} 修饰符，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isAbstract(int mod) {
        return (mod & ABSTRACT) != 0;
    }

    /**
     * 如果整数参数包含 {@code strictfp} 修饰符，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   mod 修饰符集
     * @return 如果 {@code mod} 包含 {@code strictfp} 修饰符，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isStrict(int mod) {
        return (mod & STRICT) != 0;
    }

    /**
     * 返回一个描述指定修饰符中的访问修饰符标志的字符串。例如：
     * <blockquote><pre>
     *    public final synchronized strictfp
     * </pre></blockquote>
     * 修饰符名称的返回顺序与《Java&trade; 语言规范》第 8.1.1、8.3.1、8.4.3、8.8.3 和 9.1.1 节中建议的修饰符顺序一致。
     * 本方法使用的完整修饰符顺序为：
     * <blockquote> {@code
     * public protected private abstract static final transient
     * volatile synchronized native strictfp
     * interface } </blockquote>
     * 本类中讨论的 {@code interface} 修饰符不是 Java 语言中的真正修饰符，它出现在本方法列出的所有其他修饰符之后。此方法可能返回一个不是 Java 实体有效修饰符的修饰符字符串；换句话说，不会检查输入表示的修饰符组合的有效性。
     *
     * 要对已知类型的实体（如构造器或方法）进行此类检查，首先将 {@code toString} 的参数与 {@link #constructorModifiers} 或 {@link #methodModifiers} 等方法中的适当掩码进行 AND 操作。
     *
     * @param   mod 修饰符集
     * @return 代表 {@code mod} 中修饰符集的字符串表示形式
     */
    public static String toString(int mod) {
        StringBuilder sb = new StringBuilder();
        int len;

        if ((mod & PUBLIC) != 0)        sb.append("public ");
        if ((mod & PROTECTED) != 0)     sb.append("protected ");
        if ((mod & PRIVATE) != 0)       sb.append("private ");

        /* 标准顺序 */
        if ((mod & ABSTRACT) != 0)      sb.append("abstract ");
        if ((mod & STATIC) != 0)        sb.append("static ");
        if ((mod & FINAL) != 0)         sb.append("final ");
        if ((mod & TRANSIENT) != 0)     sb.append("transient ");
        if ((mod & VOLATILE) != 0)      sb.append("volatile ");
        if ((mod & SYNCHRONIZED) != 0)  sb.append("synchronized ");
        if ((mod & NATIVE) != 0)        sb.append("native ");
        if ((mod & STRICT) != 0)        sb.append("strictfp ");
        if ((mod & INTERFACE) != 0)     sb.append("interface ");

        if ((len = sb.length()) > 0)    /* 去除尾部空格 */
            return sb.toString().substring(0, len-1);
        return "";
    }

    /*
     * 《Java&trade; 虚拟机规范》第 4.1、4.4、4.5 和 4.7 节中的访问修饰符标志常量
     */

    /**
     * 代表 {@code public} 修饰符的 {@code int} 值。
     */
    public static final int PUBLIC           = 0x00000001;

    /**
     * 代表 {@code private} 修饰符的 {@code int} 值。
     */
    public static final int PRIVATE          = 0x00000002;

    /**
     * 代表 {@code protected} 修饰符的 {@code int} 值。
     */
    public static final int PROTECTED        = 0x00000004;

    /**
     * 代表 {@code static} 修饰符的 {@code int} 值。
     */
    public static final int STATIC           = 0x00000008;

    /**
     * 代表 {@code final} 修饰符的 {@code int} 值。
     */
    public static final int FINAL            = 0x00000010;

    /**
     * 代表 {@code synchronized} 修饰符的 {@code int} 值。
     */
    public static final int SYNCHRONIZED     = 0x00000020;

    /**
     * 代表 {@code volatile} 修饰符的 {@code int} 值。
     */
    public static final int VOLATILE         = 0x00000040;

    /**
     * 代表 {@code transient} 修饰符的 {@code int} 值。
     */
    public static final int TRANSIENT        = 0x00000080;

    /**
     * 代表 {@code native} 修饰符的 {@code int} 值。
     */
    public static final int NATIVE           = 0x00000100;

    /**
     * 代表 {@code interface} 修饰符的 {@code int} 值。
     */
    public static final int INTERFACE        = 0x00000200;

    /**
     * 代表 {@code abstract} 修饰符的 {@code int} 值。
     */
    public static final int ABSTRACT         = 0x00000400;

    /**
     * 代表 {@code strictfp} 修饰符的 {@code int} 值。
     */
    public static final int STRICT           = 0x00000800;

    // 当前未在公共 API 中公开的位，因为它们对字段和方法有不同的含义，或者因为它们不是 Java 编程语言的关键字
    static final int BRIDGE    = 0x00000040;
    static final int VARARGS   = 0x00000080;
    static final int SYNTHETIC = 0x00001000;
    static final int ANNOTATION  = 0x00002000;
    static final int ENUM      = 0x00004000;
    static final int MANDATED  = 0x00008000;
    static boolean isSynthetic(int mod) {
      return (mod & SYNTHETIC) != 0;
    }

    static boolean isMandated(int mod) {
      return (mod & MANDATED) != 0;
    }

    // 关于 FOO_MODIFIERS 字段和 fooModifiers() 方法的说明：
    // 修饰符集在时间和 Java SE 发行版中不是常量。因此，提供一个允许将这些值视为 Java 级常量的外部接口是不合适的，因为这些值可能会被常量折叠，从而错过对修饰符集的更新。因此，fooModifiers() 方法返回一个在给定发行版中不变的值，但该值可能会随着时间而变化。

    /**
     * 可以应用于类的 Java 源修饰符。
     * @jls 8.1.1 Class Modifiers
     */
    private static final int CLASS_MODIFIERS =
        Modifier.PUBLIC         | Modifier.PROTECTED    | Modifier.PRIVATE |
        Modifier.ABSTRACT       | Modifier.STATIC       | Modifier.FINAL   |
        Modifier.STRICT;

    /**
     * 可以应用于接口的 Java 源修饰符。
     * @jls 9.1.1 Interface Modifiers
     */
    private static final int INTERFACE_MODIFIERS =
        Modifier.PUBLIC         | Modifier.PROTECTED    | Modifier.PRIVATE |
        Modifier.ABSTRACT       | Modifier.STATIC       | Modifier.STRICT;

    /**
     * 可以应用于构造器的 Java 源修饰符。
     * @jls 8.8.3 Constructor Modifiers
     */
    private static final int CONSTRUCTOR_MODIFIERS =
        Modifier.PUBLIC         | Modifier.PROTECTED    | Modifier.PRIVATE;

    /**
     * 可以应用于方法的 Java 源修饰符。
     * @jls 8.4.3 Method Modifiers
     */
    private static final int METHOD_MODIFIERS =
        Modifier.PUBLIC         | Modifier.PROTECTED    | Modifier.PRIVATE |
        Modifier.ABSTRACT       | Modifier.STATIC       | Modifier.FINAL   |
        Modifier.SYNCHRONIZED   | Modifier.NATIVE       | Modifier.STRICT;

    /**
     * 可以应用于字段的 Java 源修饰符。
     * @jls 8.3.1 Field Modifiers
     */
    private static final int FIELD_MODIFIERS =
        Modifier.PUBLIC         | Modifier.PROTECTED    | Modifier.PRIVATE |
        Modifier.STATIC         | Modifier.FINAL        | Modifier.TRANSIENT |
        Modifier.VOLATILE;

    /**
     * 可以应用于方法或构造器参数的 Java 源修饰符。
     * @jls 8.4.1 Formal Parameters
     */
    private static final int PARAMETER_MODIFIERS =
        Modifier.FINAL;

    /**
     *
     */
    static final int ACCESS_MODIFIERS =
        Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE;

    /**
     * 返回一个 OR 运算的 {@code int} 值，表示可以应用于类的源语言修饰符。
     * @return 一个 OR 运算的 {@code int} 值，表示可以应用于类的源语言修饰符。
     *
     * @jls 8.1.1 Class Modifiers
     * @since 1.7
     */
    public static int classModifiers() {
        return CLASS_MODIFIERS;
    }

    /**
     * 返回一个 OR 运算的 {@code int} 值，表示可以应用于接口的源语言修饰符。
     * @return 一个 OR 运算的 {@code int} 值，表示可以应用于接口的源语言修饰符。
     *
     * @jls 9.1.1 Interface Modifiers
     * @since 1.7
     */
    public static int interfaceModifiers() {
        return INTERFACE_MODIFIERS;
    }


                /**
     * 返回一个 {@code int} 值，该值是可以通过 OR 运算符组合在一起的构造函数源语言修饰符。
     * @return 一个 {@code int} 值，该值是可以通过 OR 运算符组合在一起的构造函数源语言修饰符。
     *
     * @jls 8.8.3 构造函数修饰符
     * @since 1.7
     */
    public static int constructorModifiers() {
        return CONSTRUCTOR_MODIFIERS;
    }

    /**
     * 返回一个 {@code int} 值，该值是可以通过 OR 运算符组合在一起的方法源语言修饰符。
     * @return 一个 {@code int} 值，该值是可以通过 OR 运算符组合在一起的方法源语言修饰符。
     *
     * @jls 8.4.3 方法修饰符
     * @since 1.7
     */
    public static int methodModifiers() {
        return METHOD_MODIFIERS;
    }

    /**
     * 返回一个 {@code int} 值，该值是可以通过 OR 运算符组合在一起的字段源语言修饰符。
     * @return 一个 {@code int} 值，该值是可以通过 OR 运算符组合在一起的字段源语言修饰符。
     *
     * @jls 8.3.1 字段修饰符
     * @since 1.7
     */
    public static int fieldModifiers() {
        return FIELD_MODIFIERS;
    }

    /**
     * 返回一个 {@code int} 值，该值是可以通过 OR 运算符组合在一起的参数源语言修饰符。
     * @return 一个 {@code int} 值，该值是可以通过 OR 运算符组合在一起的参数源语言修饰符。
     *
     * @jls 8.4.1 形式参数
     * @since 1.8
     */
    public static int parameterModifiers() {
        return PARAMETER_MODIFIERS;
    }
}
