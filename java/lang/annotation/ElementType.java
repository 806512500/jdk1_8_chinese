/*
 * 版权所有 (c) 2003, 2013, Oracle 及/或其附属公司。保留所有权利。
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

package java.lang.annotation;

/**
 * 该枚举类型的常量提供了一个简单的分类，用于描述注解在 Java 程序中可能出现的语法位置。这些常量用于
 * {@link Target java.lang.annotation.Target} 元注解中，以指定可以写入特定类型注解的合法位置。
 *
 * <p>注解可能出现的语法位置分为 <em>声明上下文</em>，其中注解适用于声明，和 <em>类型上下文</em>，其中注解适用于声明和表达式中使用的类型。
 *
 * <p>常量 {@link #ANNOTATION_TYPE} , {@link #CONSTRUCTOR} , {@link
 * #FIELD} , {@link #LOCAL_VARIABLE} , {@link #METHOD} , {@link #PACKAGE} ,
 * {@link #PARAMETER} , {@link #TYPE} , 和 {@link #TYPE_PARAMETER} 对应于 JLS 9.6.4.1 中的声明上下文。
 *
 * <p>例如，类型被元注解为 {@code @Target(ElementType.FIELD)} 的注解只能作为字段声明的修饰符出现。
 *
 * <p>常量 {@link #TYPE_USE} 对应于 JLS 4.11 中的 15 个类型上下文，以及两个声明上下文：类型声明（包括注解类型声明）和类型参数声明。
 *
 * <p>例如，类型被元注解为 {@code @Target(ElementType.TYPE_USE)} 的注解可以写在字段的类型上（如果字段是嵌套的、参数化的或数组类型，则可以在其类型内部），也可以作为类声明的修饰符出现。
 *
 * <p>{@code TYPE_USE} 常量包括类型声明和类型参数声明，方便类型检查器设计者为注解类型赋予语义。例如，如果注解类型
 * {@code NonNull} 被元注解为 {@code @Target(ElementType.TYPE_USE)}，则 {@code @NonNull}
 * {@code class C {...}} 可以被类型检查器视为表示所有 {@code C} 类的变量都是非空的，同时仍然允许其他类的变量根据 {@code @NonNull} 是否出现在变量声明处而为非空或非非空。
 *
 * @author  Joshua Bloch
 * @since 1.5
 * @jls 9.6.4.1 @Target
 * @jls 4.1 类型和值的种类
 */
public enum ElementType {
    /** 类、接口（包括注解类型）或枚举声明 */
    TYPE,

    /** 字段声明（包括枚举常量） */
    FIELD,

    /** 方法声明 */
    METHOD,

    /** 形式参数声明 */
    PARAMETER,

    /** 构造器声明 */
    CONSTRUCTOR,

    /** 局部变量声明 */
    LOCAL_VARIABLE,

    /** 注解类型声明 */
    ANNOTATION_TYPE,

    /** 包声明 */
    PACKAGE,

    /**
     * 类型参数声明
     *
     * @since 1.8
     */
    TYPE_PARAMETER,

    /**
     * 类型的使用
     *
     * @since 1.8
     */
    TYPE_USE
}
