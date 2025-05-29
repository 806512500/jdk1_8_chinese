
/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

package java.lang.reflect;

import java.security.AccessController;
import sun.reflect.LangReflectAccess;
import sun.reflect.ReflectionFactory;

/**
 * Modifier 类提供了静态方法和常量来解码类和成员的访问修饰符。修饰符集表示为整数，不同的位位置表示不同的修饰符。表示修饰符的常量值取自《Java&trade; 虚拟机规范》第 4.1、4.4、4.5 和 4.7 节中的表。
 *
 * @see Class#getModifiers()
 * @see Member#getModifiers()
 *
 * @author Nakul Saraiya
 * @author Kenneth Russell
 */
public class Modifier {

    /*
     * java.lang 和 java.lang.reflect 包之间的引导协议
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
     * @param   mod 一组修饰符
     * @return 如果 {@code mod} 包含 {@code public} 修饰符，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isPublic(int mod) {
        return (mod & PUBLIC) != 0;
    }

    /**
     * 如果整数参数包含 {@code private} 修饰符，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   mod 一组修饰符
     * @return 如果 {@code mod} 包含 {@code private} 修饰符，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isPrivate(int mod) {
        return (mod & PRIVATE) != 0;
    }

    /**
     * 如果整数参数包含 {@code protected} 修饰符，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   mod 一组修饰符
     * @return 如果 {@code mod} 包含 {@code protected} 修饰符，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isProtected(int mod) {
        return (mod & PROTECTED) != 0;
    }

    /**
     * 如果整数参数包含 {@code static} 修饰符，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   mod 一组修饰符
     * @return 如果 {@code mod} 包含 {@code static} 修饰符，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isStatic(int mod) {
        return (mod & STATIC) != 0;
    }

    /**
     * 如果整数参数包含 {@code final} 修饰符，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   mod 一组修饰符
     * @return 如果 {@code mod} 包含 {@code final} 修饰符，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isFinal(int mod) {
        return (mod & FINAL) != 0;
    }

    /**
     * 如果整数参数包含 {@code synchronized} 修饰符，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   mod 一组修饰符
     * @return 如果 {@code mod} 包含 {@code synchronized} 修饰符，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isSynchronized(int mod) {
        return (mod & SYNCHRONIZED) != 0;
    }

    /**
     * 如果整数参数包含 {@code volatile} 修饰符，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   mod 一组修饰符
     * @return 如果 {@code mod} 包含 {@code volatile} 修饰符，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isVolatile(int mod) {
        return (mod & VOLATILE) != 0;
    }

    /**
     * 如果整数参数包含 {@code transient} 修饰符，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   mod 一组修饰符
     * @return 如果 {@code mod} 包含 {@code transient} 修饰符，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isTransient(int mod) {
        return (mod & TRANSIENT) != 0;
    }

    /**
     * 如果整数参数包含 {@code native} 修饰符，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   mod 一组修饰符
     * @return 如果 {@code mod} 包含 {@code native} 修饰符，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isNative(int mod) {
        return (mod & NATIVE) != 0;
    }

    /**
     * 如果整数参数包含 {@code interface} 修饰符，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   mod 一组修饰符
     * @return 如果 {@code mod} 包含 {@code interface} 修饰符，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isInterface(int mod) {
        return (mod & INTERFACE) != 0;
    }

    /**
     * 如果整数参数包含 {@code abstract} 修饰符，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   mod 一组修饰符
     * @return 如果 {@code mod} 包含 {@code abstract} 修饰符，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isAbstract(int mod) {
        return (mod & ABSTRACT) != 0;
    }

    /**
     * 如果整数参数包含 {@code strictfp} 修饰符，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   mod 一组修饰符
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
     * 修饰符名称以与《Java&trade; 语言规范》第 8.1.1、8.3.1、8.4.3、8.8.3 和 9.1.1 节中给出的建议修饰符顺序一致的顺序返回。
     * 本方法使用的完整修饰符顺序为：
     * <blockquote> {@code
     * public protected private abstract static final transient
     * volatile synchronized native strictfp
     * interface } </blockquote>
     * 本类中讨论的 {@code interface} 修饰符不是 Java 语言中的真实修饰符，它出现在本方法列出的所有其他修饰符之后。此方法可能返回一个不是 Java 实体的有效修饰符的修饰符字符串；换句话说，不会对输入表示的修饰符组合的有效性进行检查。
     *
     * 注意，要对已知类型的实体（如构造器或方法）进行此类检查，首先应将 {@code toString} 的参数与 {@link #constructorModifiers} 或 {@link #methodModifiers} 等方法中的适当掩码进行 AND 运算。
     *
     * @param   mod 一组修饰符
     * @return  代表 {@code mod} 中修饰符集的字符串表示形式
     */
    public static String toString(int mod) {
        StringBuilder sb = new StringBuilder();
        int len;


                    if ((mod & PUBLIC) != 0)        sb.append("public ");
        if ((mod & PROTECTED) != 0)     sb.append("protected ");
        if ((mod & PRIVATE) != 0)       sb.append("private ");

        /* 顺序规范 */
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
     * 《Java&trade;虚拟机规范》表4.1、4.4、4.5和4.7中的访问修饰符标志常量
     */

    /**
     * 表示 {@code public} 修饰符的 {@code int} 值。
     */
    public static final int PUBLIC           = 0x00000001;

    /**
     * 表示 {@code private} 修饰符的 {@code int} 值。
     */
    public static final int PRIVATE          = 0x00000002;

    /**
     * 表示 {@code protected} 修饰符的 {@code int} 值。
     */
    public static final int PROTECTED        = 0x00000004;

    /**
     * 表示 {@code static} 修饰符的 {@code int} 值。
     */
    public static final int STATIC           = 0x00000008;

    /**
     * 表示 {@code final} 修饰符的 {@code int} 值。
     */
    public static final int FINAL            = 0x00000010;

    /**
     * 表示 {@code synchronized} 修饰符的 {@code int} 值。
     */
    public static final int SYNCHRONIZED     = 0x00000020;

    /**
     * 表示 {@code volatile} 修饰符的 {@code int} 值。
     */
    public static final int VOLATILE         = 0x00000040;

    /**
     * 表示 {@code transient} 修饰符的 {@code int} 值。
     */
    public static final int TRANSIENT        = 0x00000080;

    /**
     * 表示 {@code native} 修饰符的 {@code int} 值。
     */
    public static final int NATIVE           = 0x00000100;

    /**
     * 表示 {@code interface} 修饰符的 {@code int} 值。
     */
    public static final int INTERFACE        = 0x00000200;

    /**
     * 表示 {@code abstract} 修饰符的 {@code int} 值。
     */
    public static final int ABSTRACT         = 0x00000400;

    /**
     * 表示 {@code strictfp} 修饰符的 {@code int} 值。
     */
    public static final int STRICT           = 0x00000800;

    // 当前未在公共API中公开的位，因为它们对于字段和方法具有不同的含义，或者因为它们不是Java编程语言的关键字
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

    // 关于 FOO_MODIFIERS 字段和 fooModifiers() 方法的注释：
    // 修饰符集不保证在时间和Java SE版本中保持不变。因此，不适宜提供一个外部接口来公开这些信息，因为这些值可能会被当作Java级别的常量处理，导致值被常量折叠，从而错过修饰符集的更新。因此，fooModifiers() 方法返回给定版本中不变的值，但这些值可能会随着时间而变化。

    /**
     * 可以应用于类的Java源修饰符。
     * @jls 8.1.1 类修饰符
     */
    private static final int CLASS_MODIFIERS =
        Modifier.PUBLIC         | Modifier.PROTECTED    | Modifier.PRIVATE |
        Modifier.ABSTRACT       | Modifier.STATIC       | Modifier.FINAL   |
        Modifier.STRICT;

    /**
     * 可以应用于接口的Java源修饰符。
     * @jls 9.1.1 接口修饰符
     */
    private static final int INTERFACE_MODIFIERS =
        Modifier.PUBLIC         | Modifier.PROTECTED    | Modifier.PRIVATE |
        Modifier.ABSTRACT       | Modifier.STATIC       | Modifier.STRICT;


    /**
     * 可以应用于构造函数的Java源修饰符。
     * @jls 8.8.3 构造函数修饰符
     */
    private static final int CONSTRUCTOR_MODIFIERS =
        Modifier.PUBLIC         | Modifier.PROTECTED    | Modifier.PRIVATE;

    /**
     * 可以应用于方法的Java源修饰符。
     * @jls 8.4.3 方法修饰符
     */
    private static final int METHOD_MODIFIERS =
        Modifier.PUBLIC         | Modifier.PROTECTED    | Modifier.PRIVATE |
        Modifier.ABSTRACT       | Modifier.STATIC       | Modifier.FINAL   |
        Modifier.SYNCHRONIZED   | Modifier.NATIVE       | Modifier.STRICT;

    /**
     * 可以应用于字段的Java源修饰符。
     * @jls 8.3.1 字段修饰符
     */
    private static final int FIELD_MODIFIERS =
        Modifier.PUBLIC         | Modifier.PROTECTED    | Modifier.PRIVATE |
        Modifier.STATIC         | Modifier.FINAL        | Modifier.TRANSIENT |
        Modifier.VOLATILE;

    /**
     * 可以应用于方法或构造函数参数的Java源修饰符。
     * @jls 8.4.1 形式参数
     */
    private static final int PARAMETER_MODIFIERS =
        Modifier.FINAL;


                /**
     *
     */
    static final int ACCESS_MODIFIERS =
        Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE;

    /**
     * 返回一个 {@code int} 值，该值是可以通过 OR 操作符组合在一起应用于类的源语言修饰符。
     * @return 一个 {@code int} 值，该值是可以通过 OR 操作符组合在一起应用于类的源语言修饰符。
     *
     * @jls 8.1.1 类修饰符
     * @since 1.7
     */
    public static int classModifiers() {
        return CLASS_MODIFIERS;
    }

    /**
     * 返回一个 {@code int} 值，该值是可以通过 OR 操作符组合在一起应用于接口的源语言修饰符。
     * @return 一个 {@code int} 值，该值是可以通过 OR 操作符组合在一起应用于接口的源语言修饰符。
     *
     * @jls 9.1.1 接口修饰符
     * @since 1.7
     */
    public static int interfaceModifiers() {
        return INTERFACE_MODIFIERS;
    }

    /**
     * 返回一个 {@code int} 值，该值是可以通过 OR 操作符组合在一起应用于构造函数的源语言修饰符。
     * @return 一个 {@code int} 值，该值是可以通过 OR 操作符组合在一起应用于构造函数的源语言修饰符。
     *
     * @jls 8.8.3 构造函数修饰符
     * @since 1.7
     */
    public static int constructorModifiers() {
        return CONSTRUCTOR_MODIFIERS;
    }

    /**
     * 返回一个 {@code int} 值，该值是可以通过 OR 操作符组合在一起应用于方法的源语言修饰符。
     * @return 一个 {@code int} 值，该值是可以通过 OR 操作符组合在一起应用于方法的源语言修饰符。
     *
     * @jls 8.4.3 方法修饰符
     * @since 1.7
     */
    public static int methodModifiers() {
        return METHOD_MODIFIERS;
    }

    /**
     * 返回一个 {@code int} 值，该值是可以通过 OR 操作符组合在一起应用于字段的源语言修饰符。
     * @return 一个 {@code int} 值，该值是可以通过 OR 操作符组合在一起应用于字段的源语言修饰符。
     *
     * @jls 8.3.1 字段修饰符
     * @since 1.7
     */
    public static int fieldModifiers() {
        return FIELD_MODIFIERS;
    }

    /**
     * 返回一个 {@code int} 值，该值是可以通过 OR 操作符组合在一起应用于参数的源语言修饰符。
     * @return 一个 {@code int} 值，该值是可以通过 OR 操作符组合在一起应用于参数的源语言修饰符。
     *
     * @jls 8.4.1 形式参数
     * @since 1.8
     */
    public static int parameterModifiers() {
        return PARAMETER_MODIFIERS;
    }
}
