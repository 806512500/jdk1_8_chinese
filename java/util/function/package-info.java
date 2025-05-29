/*
 * 版权所有 (c) 2011, 2013, Oracle 和/或其附属公司。保留所有权利。
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

/**
 * <em>函数式接口</em>为 lambda 表达式和方法引用提供目标类型。每个函数式接口都有一个抽象方法，称为该函数式接口的<em>函数方法</em>，
 * lambda 表达式的参数和返回类型将与之匹配或适应。函数式接口可以在多种上下文中提供目标类型，例如赋值上下文、方法调用上下文或类型转换上下文：
 *
 * <pre>{@code
 *     // 赋值上下文
 *     Predicate<String> p = String::isEmpty;
 *
 *     // 方法调用上下文
 *     stream.filter(e -> e.getSize() > 10)...
 *
 *     // 类型转换上下文
 *     stream.map((ToIntFunction) e -> e.getSize())...
 * }</pre>
 *
 * <p>此包中的接口是 JDK 使用的通用函数式接口，用户代码也可以使用。虽然它们没有提供 lambda 表达式可能适应的所有函数形状的完整集合，
 * 但提供了足够的接口来满足常见需求。其他为特定目的提供的函数式接口，如 {@link java.io.FileFilter}，在它们被使用的包中定义。
 *
 * <p>此包中的接口被注解为 {@link java.lang.FunctionalInterface}。此注解不是编译器识别接口为函数式接口的必要条件，
 * 但仅作为捕捉设计意图的辅助工具，并帮助编译器识别设计意图的意外违反。
 *
 * <p>函数式接口通常表示抽象概念，如函数、动作或谓词。在记录函数式接口或引用类型为函数式接口的变量时，
 * 通常直接引用这些抽象概念，例如使用“此函数”而不是“此对象表示的函数”。当 API 方法以这种方式接受或返回函数式接口时，
 * 例如“应用提供的函数...”，这被理解为对实现适当函数式接口的对象的<i>非空</i>引用，除非明确指定了潜在的空值。
 *
 * <p>此包中的函数式接口遵循可扩展的命名约定，如下：
 *
 * <ul>
 *     <li>有几个基本的函数形状，包括
 *     {@link java.util.function.Function}（从 {@code T} 到 {@code R} 的一元函数），
 *     {@link java.util.function.Consumer}（从 {@code T} 到 {@code void} 的一元函数），
 *     {@link java.util.function.Predicate}（从 {@code T} 到 {@code boolean} 的一元函数），
 *     以及 {@link java.util.function.Supplier}（到 {@code R} 的零元函数）。
 *     </li>
 *
 *     <li>函数形状基于它们最常见的使用方式具有自然的元数。可以通过元数前缀修改基本形状以表示不同的元数，例如
 *     {@link java.util.function.BiFunction}（从 {@code T} 和 {@code U} 到 {@code R} 的二元函数）。
 *     </li>
 *
 *     <li>有额外的派生函数形状扩展了基本函数形状，包括 {@link java.util.function.UnaryOperator}
 *     （扩展 {@code Function}）和 {@link java.util.function.BinaryOperator}（扩展 {@code BiFunction}）。
 *     </li>
 *
 *     <li>函数式接口的类型参数可以使用额外的类型前缀专门化为原始类型。为了专门化具有通用返回类型和通用参数的类型的返回类型，我们使用 {@code ToXxx} 前缀，如 {@link java.util.function.ToIntFunction}。
 *     否则，类型参数从左到右专门化，如 {@link java.util.function.DoubleConsumer} 或 {@link java.util.function.ObjIntConsumer}。
 *     （类型前缀 {@code Obj} 用于表示我们不想专门化此参数，而是继续下一个参数，如 {@link java.util.function.ObjIntConsumer}。）
 *     这些方案可以组合，如 {@code IntToDoubleFunction}。
 *     </li>
 *
 *     <li>如果所有参数都有专门化前缀，则可以省略元数前缀（如 {@link java.util.function.ObjIntConsumer}）。
 *     </li>
 * </ul>
 *
 * @see java.lang.FunctionalInterface
 * @since 1.8
 */
package java.util.function;