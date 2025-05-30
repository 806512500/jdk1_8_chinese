
/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.invoke;

import java.io.Serializable;
import java.util.Arrays;

/**
 * <p>提供方法以通过委托给提供的 {@link MethodHandle} 来创建实现一个或多个接口的简单“函数对象”，
 * 可能需要进行类型适配和部分参数求值。这些方法通常用作 {@code invokedynamic} 调用站点的
 * <em>引导方法</em>，以支持 Java 编程语言中的 <em>lambda 表达式</em> 和 <em>方法引用表达式</em> 功能。
 *
 * <p>对提供的 {@code MethodHandle} 指定的行为的间接访问按顺序通过三个阶段：
 * <ul>
 *     <li><em>链接</em> 发生在调用此类中的方法时。它们接受一个要实现的接口（通常是
 *     <em>函数接口</em>，即只有一个抽象方法的接口），该接口中要实现的方法的名称和签名，
 *     描述该方法所需实现行为的方法句柄，以及可能的其他元数据，并生成一个 {@link CallSite}，
 *     其目标可用于创建合适的函数对象。链接可能涉及动态加载一个实现目标接口的新类。
 *     {@code CallSite} 可以视为函数对象的“工厂”，因此这些链接方法被称为“元工厂”。</li>
 *
 *     <li><em>捕获</em> 发生在调用 {@code CallSite} 的目标时，通常通过一个 {@code invokedynamic} 调用站点，
 *     生成一个函数对象。这可能为单个工厂 {@code CallSite} 发生多次。捕获可能涉及分配一个新函数对象，
 *     或返回一个现有的函数对象。行为 {@code MethodHandle} 可能有超出指定接口方法的额外参数；
 *     这些参数称为 <em>捕获参数</em>，必须作为参数提供给 {@code CallSite} 目标，并且可以与行为
 *     {@code MethodHandle} 早期绑定。捕获参数的数量和类型在链接时确定。</li>
 *
 *     <li><em>调用</em> 发生在对函数对象上调用实现的接口方法时。这可能为单个函数对象发生多次。
 *     引用行为 {@code MethodHandle} 的方法将使用捕获的参数和调用时提供的任何额外参数调用，
 *     就像通过 {@link MethodHandle#invoke(Object...)} 调用一样。</li>
 * </ul>
 *
 * <p>有时限制调用时允许的输入或结果集是有用的。例如，当泛型接口 {@code Predicate<T>}
 * 被参数化为 {@code Predicate<String>} 时，输入必须是 {@code String}，即使实现的方法允许任何
 * {@code Object}。在链接时，一个额外的 {@link MethodType} 参数描述了“实例化”的方法类型；
 * 在调用时，参数和最终结果将根据此 {@code MethodType} 进行检查。
 *
 * <p>此类提供了两种形式的链接方法：标准版本
 * ({@link #metafactory(MethodHandles.Lookup, String, MethodType, MethodType, MethodHandle, MethodType)})
 * 使用优化协议，以及替代版本
 * ({@link #altMetafactory(MethodHandles.Lookup, String, MethodType, Object...)}。
 * 替代版本是标准版本的泛化，通过标志和额外参数提供对生成的函数对象行为的额外控制。替代版本增加了管理以下函数对象属性的能力：
 *
 * <ul>
 *     <li><em>桥接。</em> 有时实现方法签名的多个变体是有用的，涉及参数或返回类型适配。当语言逻辑上认为多个不同的 VM 签名是同一个方法时，就会发生这种情况。
 *     标志 {@code FLAG_BRIDGES} 表示将提供一个额外的 {@code MethodType} 列表，每个都将由生成的函数对象实现。
 *     这些方法将共享相同的名称和实例化类型。</li>
 *
 *     <li><em>多个接口。</em> 如果需要，函数对象可以实现多个接口。（这些额外的接口通常是没有任何方法的标记接口。）
 *     标志 {@code FLAG_MARKERS} 表示将提供一个额外的接口列表，每个都应由生成的函数对象实现。</li>
 *
 *     <li><em>可序列化性。</em> 生成的函数对象通常不支持序列化。如果需要，可以使用 {@code FLAG_SERIALIZABLE}
 *     表示函数对象应该是可序列化的。可序列化的函数对象将使用 {@code SerializedLambda} 类的实例作为其序列化形式，
 *     这需要捕获类（由 {@link MethodHandles.Lookup} 参数 {@code caller} 描述的类）的额外帮助；请参阅
 *     {@link SerializedLambda} 了解详细信息。</li>
 * </ul>
 *
 * <p>假设链接参数如下：
 * <ul>
 *      <li>{@code invokedType}（描述 {@code CallSite} 签名）有 K 个参数，类型为 (D1..Dk)，返回类型为 Rd；</li>
 *      <li>{@code samMethodType}（描述实现的方法类型）有 N 个参数，类型为 (U1..Un)，返回类型为 Ru；</li>
 *      <li>{@code implMethod}（提供实现的方法句柄）有 M 个参数，类型为 (A1..Am)，返回类型为 Ra
 *      （如果该方法描述实例方法，则该方法句柄的方法类型已经包含一个额外的首个参数，对应于接收者）；</li>
 *      <li>{@code instantiatedMethodType}（允许限制调用）有 N 个参数，类型为 (T1..Tn)，返回类型为 Rt。</li>
 * </ul>
 *
 * <p>那么必须满足以下链接不变性：
 * <ul>
 *     <li>Rd 是一个接口</li>
 *     <li>{@code implMethod} 是一个 <em>直接方法句柄</em></li>
 *     <li>{@code samMethodType} 和 {@code instantiatedMethodType} 有相同的参数数量 N，且对于 i=1..N，Ti 和 Ui 是相同的类型，或者 Ti 和 Ui 都是引用类型且 Ti 是 Ui 的子类型</li>
 *     <li>要么 Rt 和 Ru 是相同的类型，要么两者都是引用类型且 Rt 是 Ru 的子类型</li>
 *     <li>K + N = M</li>
 *     <li>对于 i=1..K，Di = Ai</li>
 *     <li>对于 i=1..N，Ti 可以适配到 Aj，其中 j=i+k</li>
 *     <li>返回类型 Rt 是 void，或者返回类型 Ra 不是 void 且可以适配到 Rt</li>
 * </ul>
 *
 * <p>此外，在捕获时，如果 {@code implMethod} 对应于实例方法，并且有任何捕获参数（即 {@code K > 0}），则第一个捕获参数（对应于接收者）必须是非空的。
 *
 * <p>类型 Q 被认为可以适配到 S 如下：
 * <table summary="可适配类型">
 *     <tr><th>Q</th><th>S</th><th>链接时检查</th><th>调用时检查</th></tr>
 *     <tr>
 *         <td>基本类型</td><td>基本类型</td>
 *         <td>Q 可以通过基本类型扩展转换为 S</td>
 *         <td>无</td>
 *     </tr>
 *     <tr>
 *         <td>基本类型</td><td>引用类型</td>
 *         <td>S 是 Wrapper(Q) 的超类型</td>
 *         <td>从 Wrapper(Q) 转换为 S</td>
 *     </tr>
 *     <tr>
 *         <td>引用类型</td><td>基本类型</td>
 *         <td>对于参数类型：Q 是基本类型的包装器且 Primitive(Q) 可以扩展到 S
 *         <br>对于返回类型：如果 Q 是基本类型的包装器，检查 Primitive(Q) 是否可以扩展到 S</td>
 *         <td>如果 Q 不是基本类型的包装器，将 Q 转换为 Wrapper(S) 的基类；
 *         例如，对于数值类型，转换为 Number</td>
 *     </tr>
 *     <tr>
 *         <td>引用类型</td><td>引用类型</td>
 *         <td>对于参数类型：S 是 Q 的超类型
 *         <br>对于返回类型：无</td>
 *         <td>从 Q 转换为 S</td>
 *     </tr>
 * </table>
 *
 * @apiNote 这些链接方法旨在支持 Java 语言中 <em>lambda 表达式</em> 和 <em>方法引用</em> 的求值。
 * 对于源代码中的每个 lambda 表达式或方法引用，都有一个目标类型，即一个函数接口。求值 lambda 表达式会产生一个其目标类型的对象。
 * 推荐的求值 lambda 表达式的方法是将 lambda 体反糖化为一个方法，调用一个静态参数列表描述函数接口的唯一方法和反糖化实现方法的
 * {@code invokedynamic} 调用站点，并返回一个实现目标类型的对象（lambda 对象）。（对于方法引用，实现方法就是引用的方法；不需要反糖化。）
 *
 * <p>实现方法的参数列表和接口方法的参数列表可能在几个方面不同。实现方法可能有额外的参数以容纳 lambda 表达式捕获的参数；
 * 还可能有由于允许的参数适配（如类型转换、装箱、拆箱和基本类型扩展）导致的差异。（可变参数适配不由元工厂处理；这些预计由调用者处理。）
 *
 * <p>{@code invokedynamic} 调用站点有两个参数列表：静态参数列表和动态参数列表。静态参数列表存储在常量池中；
 * 动态参数在捕获时推入操作数栈。引导方法可以访问整个静态参数列表（在这种情况下，包括描述实现方法、目标接口和目标接口方法的信息），
 * 以及一个描述动态参数的数量和静态类型（但不是值）和 {@code invokedynamic} 站点的静态返回类型的方法签名。
 *
 * @implNote 实现方法用方法句柄描述。理论上，任何方法句柄都可以使用。目前支持的是直接方法句柄，表示调用虚拟方法、接口方法、构造方法和静态方法。
 */
public class LambdaMetafactory {

    /** 标志，表示替代元工厂中的 lambda 对象必须是可序列化的 */
    public static final int FLAG_SERIALIZABLE = 1 << 0;

    /**
     * 标志，表示替代元工厂中的 lambda 对象实现其他标记接口
     * 除了 Serializable
     */
    public static final int FLAG_MARKERS = 1 << 1;

    /**
     * 标志，表示替代元工厂中的 lambda 对象需要额外的桥接方法
     */
    public static final int FLAG_BRIDGES = 1 << 2;

    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
    private static final MethodType[] EMPTY_MT_ARRAY = new MethodType[0];

    /**
     * 通过委托给提供的 {@link MethodHandle} 来创建实现一个或多个接口的简单“函数对象”，
     * 在适当的类型适配和部分参数求值后。通常用作 {@code invokedynamic} 调用站点的
     * <em>引导方法</em>，以支持 Java 编程语言中的 <em>lambda 表达式</em> 和 <em>方法引用表达式</em> 功能。
     *
     * <p>这是标准的、简化版的元工厂；额外的灵活性由
     * {@link #altMetafactory(MethodHandles.Lookup, String, MethodType, Object...)} 提供。
     * 该方法的行为的一般描述见 {@link LambdaMetafactory 上方}。
     *
     * <p>当从该方法返回的 {@code CallSite} 的目标被调用时，生成的函数对象是实现由 {@code invokedType} 的返回类型命名的接口的类的实例，
     * 声明一个由 {@code invokedName} 给出名称和由 {@code samMethodType} 给出签名的方法。它还可能覆盖来自 {@code Object} 的其他方法。
     *
     * @param caller 表示具有调用者访问权限的查找上下文。当与 {@code invokedynamic} 一起使用时，
     *               由 VM 自动堆栈。
     * @param invokedName 要实现的方法的名称。当与 {@code invokedynamic} 一起使用时，
     *                    由 {@code InvokeDynamic} 结构的 {@code NameAndType} 提供，并由 VM 自动堆栈。
     * @param invokedType {@code CallSite} 的预期签名。参数类型表示捕获变量的类型；
     *                    返回类型是实现的接口。当与 {@code invokedynamic} 一起使用时，
     *                    由 {@code InvokeDynamic} 结构的 {@code NameAndType} 提供，并由 VM 自动堆栈。
     *                    如果实现方法是实例方法且此签名有参数，则调用签名中的第一个参数必须对应于接收者。
     * @param samMethodType 函数对象要实现的方法的签名和返回类型。
     * @param implMethod 描述实现方法的直接方法句柄，该方法应在调用时（通过适当的参数类型、返回类型适配和在调用参数前添加捕获参数）调用。
     * @param instantiatedMethodType 应在调用时动态强制执行的签名和返回类型。
     *                               这可以与 {@code samMethodType} 相同，也可以是其特化。
     * @return 一个 {@code CallSite}，其目标可用于执行捕获，生成实现由 {@code invokedType} 命名的接口的实例。
     * @throws LambdaConversionException 如果违反了 {@link LambdaMetafactory 上方} 描述的任何链接不变性
     */
    public static CallSite metafactory(MethodHandles.Lookup caller,
                                       String invokedName,
                                       MethodType invokedType,
                                       MethodType samMethodType,
                                       MethodHandle implMethod,
                                       MethodType instantiatedMethodType)
            throws LambdaConversionException {
        AbstractValidatingLambdaMetafactory mf;
        mf = new InnerClassLambdaMetafactory(caller, invokedType,
                                             invokedName, samMethodType,
                                             implMethod, instantiatedMethodType,
                                             false, EMPTY_CLASS_ARRAY, EMPTY_MT_ARRAY);
        mf.validateMetafactoryArgs();
        return mf.buildCallSite();
    }


                /**
     * 促进创建通过委托给提供的 {@link MethodHandle} 实现一个或多个接口的简单“函数对象”，
     * 在适当类型适应和部分评估参数之后。通常用作 {@code invokedynamic} 调用站点的
     * <em>引导方法</em>，以支持 Java 编程语言的 <em>lambda 表达式</em> 和 <em>方法引用表达式</em> 特性。
     *
     * <p>这是更通用、更灵活的元工厂；简化版本由
     * {@link #metafactory(java.lang.invoke.MethodHandles.Lookup, String, MethodType, MethodType, MethodHandle, MethodType)} 提供。
     * 该方法的行为描述见 {@link LambdaMetafactory 上方}。
     *
     * <p>此方法的参数列表包括三个固定参数，对应于 VM 在 {@code invokedynamic} 调用中自动堆栈的引导方法参数，
     * 以及一个包含其他参数的 {@code Object[]} 参数。该方法声明的参数列表为：
     *
     * <pre>{@code
     *  CallSite altMetafactory(MethodHandles.Lookup caller,
     *                          String invokedName,
     *                          MethodType invokedType,
     *                          Object... args)
     * }</pre>
     *
     * <p>但其行为如同参数列表如下：
     *
     * <pre>{@code
     *  CallSite altMetafactory(MethodHandles.Lookup caller,
     *                          String invokedName,
     *                          MethodType invokedType,
     *                          MethodType samMethodType,
     *                          MethodHandle implMethod,
     *                          MethodType instantiatedMethodType,
     *                          int flags,
     *                          int markerInterfaceCount,  // IF flags has MARKERS set
     *                          Class... markerInterfaces, // IF flags has MARKERS set
     *                          int bridgeCount,           // IF flags has BRIDGES set
     *                          MethodType... bridges      // IF flags has BRIDGES set
     *                          )
     * }</pre>
     *
     * <p>在 {@link #metafactory(MethodHandles.Lookup, String, MethodType, MethodType, MethodHandle, MethodType)}
     * 参数列表中出现的参数具有与该方法相同的规范。其他参数的解释如下：
     * <ul>
     *     <li>{@code flags} 表示附加选项；这是所需标志的按位 OR。定义的标志有 {@link #FLAG_BRIDGES}、
     *     {@link #FLAG_MARKERS} 和 {@link #FLAG_SERIALIZABLE}。</li>
     *     <li>{@code markerInterfaceCount} 是函数对象应实现的附加接口数量，仅当设置了 {@code FLAG_MARKERS} 标志时才存在。</li>
     *     <li>{@code markerInterfaces} 是一个可变长度的附加接口列表，其长度等于 {@code markerInterfaceCount}，
     *     仅当设置了 {@code FLAG_MARKERS} 标志时才存在。</li>
     *     <li>{@code bridgeCount} 是函数对象应实现的附加方法签名数量，仅当设置了 {@code FLAG_BRIDGES} 标志时才存在。</li>
     *     <li>{@code bridges} 是一个可变长度的附加方法签名列表，其长度等于 {@code bridgeCount}，
     *     仅当设置了 {@code FLAG_BRIDGES} 标志时才存在。</li>
     * </ul>
     *
     * <p>{@code markerInterfaces} 中命名的每个类都受与 {@code Rd}（即 {@code invokedType} 的返回类型）相同的限制，
     * 如 {@link LambdaMetafactory 上方} 所述。{@code bridges} 中命名的每个 {@code MethodType} 都受与
     * {@code samMethodType} 相同的限制，如 {@link LambdaMetafactory 上方} 所述。
     *
     * <p>当在 {@code flags} 中设置了 FLAG_SERIALIZABLE 时，函数对象将实现 {@code Serializable}，
     * 并具有一个返回适当 {@link SerializedLambda} 的 {@code writeReplace} 方法。{@code caller} 类必须具有一个适当的
     * {@code $deserializeLambda$} 方法，如 {@link SerializedLambda} 中所述。
     *
     * <p>当从该方法返回的 {@code CallSite} 的目标被调用时，生成的函数对象是具有以下属性的类的实例：
     * <ul>
     *     <li>该类实现了由 {@code invokedType} 的返回类型命名的接口以及由 {@code markerInterfaces} 命名的任何接口</li>
     *     <li>该类声明了由 {@code invokedName} 给出名称的方法，以及由 {@code samMethodType} 和 {@code bridges} 给出签名的方法</li>
     *     <li>该类可以覆盖来自 {@code Object} 的方法，并可以实现与序列化相关的其他方法。</li>
     * </ul>
     *
     * @param caller 表示具有调用者访问权限的查找上下文。当与 {@code invokedynamic} 一起使用时，这是由 VM 自动堆栈的。
     * @param invokedName 要实现的方法的名称。当与 {@code invokedynamic} 一起使用时，这是由 {@code InvokeDynamic}
     *                    结构的 {@code NameAndType} 提供并由 VM 自动堆栈的。
     * @param invokedType {@code CallSite} 的预期签名。参数类型表示捕获变量的类型；
     *                    返回类型是要实现的接口。当与 {@code invokedynamic} 一起使用时，这是由 {@code InvokeDynamic}
     *                    结构的 {@code NameAndType} 提供并由 VM 自动堆栈的。如果实现方法是实例方法且此签名有参数，
     *                    则调用签名中的第一个参数必须对应于接收者。
     * @param  args       包含所需参数 {@code samMethodType}、{@code implMethod}、
     *                    {@code instantiatedMethodType}、{@code flags} 和任何可选参数的 {@code Object[]} 数组，
     *                    如 {@link #altMetafactory(MethodHandles.Lookup, String, MethodType, Object...)} 上方所述。
     * @return 一个其目标可用于执行捕获的 {@code CallSite}，生成由 {@code invokedType} 命名的接口的实例。
     * @throws LambdaConversionException 如果违反了 {@link LambdaMetafactory 上方} 所述的任何链接不变性。
     */
    public static CallSite altMetafactory(MethodHandles.Lookup caller,
                                          String invokedName,
                                          MethodType invokedType,
                                          Object... args)
            throws LambdaConversionException {
        MethodType samMethodType = (MethodType)args[0];
        MethodHandle implMethod = (MethodHandle)args[1];
        MethodType instantiatedMethodType = (MethodType)args[2];
        int flags = (Integer) args[3];
        Class<?>[] markerInterfaces;
        MethodType[] bridges;
        int argIndex = 4;
        if ((flags & FLAG_MARKERS) != 0) {
            int markerCount = (Integer) args[argIndex++];
            markerInterfaces = new Class<?>[markerCount];
            System.arraycopy(args, argIndex, markerInterfaces, 0, markerCount);
            argIndex += markerCount;
        }
        else
            markerInterfaces = EMPTY_CLASS_ARRAY;
        if ((flags & FLAG_BRIDGES) != 0) {
            int bridgeCount = (Integer) args[argIndex++];
            bridges = new MethodType[bridgeCount];
            System.arraycopy(args, argIndex, bridges, 0, bridgeCount);
            argIndex += bridgeCount;
        }
        else
            bridges = EMPTY_MT_ARRAY;

        boolean isSerializable = ((flags & FLAG_SERIALIZABLE) != 0);
        if (isSerializable) {
            boolean foundSerializableSupertype = Serializable.class.isAssignableFrom(invokedType.returnType());
            for (Class<?> c : markerInterfaces)
                foundSerializableSupertype |= Serializable.class.isAssignableFrom(c);
            if (!foundSerializableSupertype) {
                markerInterfaces = Arrays.copyOf(markerInterfaces, markerInterfaces.length + 1);
                markerInterfaces[markerInterfaces.length-1] = Serializable.class;
            }
        }

        AbstractValidatingLambdaMetafactory mf
                = new InnerClassLambdaMetafactory(caller, invokedType,
                                                  invokedName, samMethodType,
                                                  implMethod,
                                                  instantiatedMethodType,
                                                  isSerializable,
                                                  markerInterfaces, bridges);
        mf.validateMetafactoryArgs();
        return mf.buildCallSite();
    }
}
