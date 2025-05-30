/*
 * Copyright (c) 2008, 2013, Oracle and/or its affiliates. All rights reserved.
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

/**
 * {@code java.lang.invoke} 包包含由 Java 核心类库和虚拟机直接提供的动态语言支持。
 *
 * <p>
 * 如 Java 虚拟机规范所述，此包中的某些类型与虚拟机中的动态语言支持有特殊关系：
 * <ul>
 * <li>{@link java.lang.invoke.MethodHandle MethodHandle} 类包含
 * <a href="MethodHandle.html#sigpoly">签名多态方法</a>，这些方法可以无视其类型描述符进行链接。
 * 通常，方法链接需要类型描述符的精确匹配。
 * </li>
 *
 * <li>JVM 字节码格式支持 {@link java.lang.invoke.MethodHandle MethodHandle} 和 {@link java.lang.invoke.MethodType MethodType} 类的即时常量。
 * </li>
 * </ul>
 *
 * <h1><a name="jvm_mods"></a>相关的 Java 虚拟机变更概要</h1>
 * 以下低级信息总结了 Java 虚拟机规范的相关部分。请参阅该规范的当前版本以获取完整详细信息。
 *
 * 每个 {@code invokedynamic} 指令的出现称为一个 <em>动态调用点</em>。
 * <h2><a name="indyinsn"></a>{@code invokedynamic} 指令</h2>
 * 动态调用点最初处于未链接状态。在这种状态下，调用点没有要调用的目标方法。
 * <p>
 * 在 JVM 可以执行动态调用点（一个 {@code invokedynamic} 指令）之前，调用点必须首先被 <em>链接</em>。
 * 链接通过调用一个 <em>引导方法</em> 来完成，该方法接收调用点的静态信息内容，并且必须生成一个 {@link java.lang.invoke.MethodHandle method handle}，
 * 该方法句柄定义了调用点的行为。
 * <p>
 * 每个 {@code invokedynamic} 指令静态指定其自己的引导方法作为常量池引用。
 * 常量池引用还指定了调用点的名称和类型描述符，就像 {@code invokevirtual} 和其他调用指令一样。
 * <p>
 * 链接从解析引导方法的常量池条目开始，并解析一个 {@link java.lang.invoke.MethodType MethodType} 对象，
 * 该对象表示动态调用点的类型描述符。
 * 此解析过程可能会触发类加载。
 * 因此，如果类加载失败，可能会抛出错误。
 * 该错误将成为动态调用点执行的异常终止。
 * 链接不会触发类初始化。
 * <p>
 * 引导方法至少在三个值上调用：
 * <ul>
 * <li>一个 {@code MethodHandles.Lookup}，即发生动态调用点的 <em>调用者类</em> 的查找对象</li>
 * <li>一个 {@code String}，即调用点中提到的方法名称</li>
 * <li>一个 {@code MethodType}，即解析后的调用类型描述符</li>
 * <li>可选地，从常量池中获取的 1 到 251 个附加静态参数</li>
 * </ul>
 * 调用方式类似于
 * {@link java.lang.invoke.MethodHandle#invoke MethodHandle.invoke}。
 * 返回的结果必须是一个 {@link java.lang.invoke.CallSite CallSite}（或其子类）。
 * 调用点的目标类型必须与从动态调用点的类型描述符派生的类型完全相同，并传递给引导方法。
 * 调用点然后永久链接到动态调用点。
 * <p>
 * 如 JVM 规范所述，所有因动态调用点链接失败而产生的错误都由
 * {@link java.lang.BootstrapMethodError BootstrapMethodError} 报告，
 * 该错误作为动态调用点执行的异常终止被抛出。
 * 如果发生这种情况，对于所有后续尝试执行动态调用点，将抛出相同的错误。
 *
 * <h2>链接时间</h2>
 * 动态调用点在其首次执行之前链接。
 * 实现链接的引导方法调用发生在尝试首次执行的线程中。
 * <p>
 * 如果有多个这样的线程，引导方法可能会在多个线程中并发调用。
 * 因此，访问全局应用程序数据的引导方法必须采取通常的预防措施以防止竞争条件。
 * 无论如何，每个 {@code invokedynamic} 指令要么未链接，要么链接到一个唯一的 {@code CallSite} 对象。
 * <p>
 * 对于需要具有单独可变行为的动态调用点的应用程序，其引导方法应生成不同的
 * {@link java.lang.invoke.CallSite CallSite} 对象，每个链接请求一个。
 * 或者，应用程序可以将单个 {@code CallSite} 对象链接到多个 {@code invokedynamic} 指令，这样
 * 目标方法的更改将在每个指令中可见。
 * <p>
 * 如果多个线程同时为单个动态调用点执行引导方法，JVM 必须选择一个 {@code CallSite} 对象并使其对所有线程可见。
 * 其他引导方法调用允许完成，但其结果被忽略，其动态调用点调用将使用最初选择的目标对象继续执行。

 * <p style="font-size:smaller;">
 * <em>讨论：</em>
 * 这些规则不允许 JVM 复制动态调用点，或发出“无缘无故”的引导方法调用。
 * 每个动态调用点最多从未链接状态转换到链接状态一次，就在其首次调用之前。
 * 没有办法撤销已完成的引导方法调用的效果。
 *
 * <h2>引导方法的类型</h2>
 * 只要每个引导方法可以正确地由 {@code MethodHandle.invoke} 调用，其详细类型是任意的。
 * 例如，第一个参数可以是 {@code Object} 而不是 {@code MethodHandles.Lookup}，返回类型
 * 也可以是 {@code Object} 而不是 {@code CallSite}。
 * （请注意，堆栈参数的类型和数量限制了合法的引导方法类型，仅限于适当类型的静态方法和 {@code CallSite} 子类的构造函数。）
 * <p>
 * 如果给定的 {@code invokedynamic} 指令没有指定静态参数，
 * 则该指令的引导方法将在三个参数上调用，传达指令的调用者类、名称和方法类型。
 * 如果 {@code invokedynamic} 指令指定了一个或多个静态参数，
 * 那些值将作为附加参数传递给方法句柄。
 * （请注意，由于任何方法的参数数量限制为 255 个，最多可以提供 251 个额外参数，因为引导方法句柄本身及其前三个参数也必须入栈。）
 * 引导方法将被调用，就像通过 {@code MethodHandle.invoke} 或 {@code invokeWithArguments} 调用一样。（无法区分这两种调用方式。）
 * <p>
 * {@code MethodHandle.invoke} 的正常参数转换规则适用于所有堆栈参数。
 * 例如，如果推送的值是基本类型，可以通过装箱转换转换为引用。
 * 如果引导方法是可变参数方法（其修饰符位 {@code 0x0080} 被设置），则这里指定的一些或所有参数可以收集到一个尾随数组参数中。
 * （这不是特殊规则，而是 {@code CONSTANT_MethodHandle} 常量、可变参数方法修饰符位和
 * {@link java.lang.invoke.MethodHandle#asVarargsCollector asVarargsCollector} 转换之间交互的有用结果。）
 * <p>
 * 根据这些规则，以下是合法的引导方法声明示例，给定不同数量的额外参数 {@code N}。
 * 第一行（标记为 {@code *}) 可以适用于任何数量的额外参数。
 * <table border=1 cellpadding=5 summary="静态参数类型">
 * <tr><th>N</th><th>示例引导方法</th></tr>
 * <tr><td>*</td><td><code>CallSite bootstrap(Lookup caller, String name, MethodType type, Object... args)</code></td></tr>
 * <tr><td>*</td><td><code>CallSite bootstrap(Object... args)</code></td></tr>
 * <tr><td>*</td><td><code>CallSite bootstrap(Object caller, Object... nameAndTypeWithArgs)</code></td></tr>
 * <tr><td>0</td><td><code>CallSite bootstrap(Lookup caller, String name, MethodType type)</code></td></tr>
 * <tr><td>0</td><td><code>CallSite bootstrap(Lookup caller, Object... nameAndType)</code></td></tr>
 * <tr><td>1</td><td><code>CallSite bootstrap(Lookup caller, String name, MethodType type, Object arg)</code></td></tr>
 * <tr><td>2</td><td><code>CallSite bootstrap(Lookup caller, String name, MethodType type, Object... args)</code></td></tr>
 * <tr><td>2</td><td><code>CallSite bootstrap(Lookup caller, String name, MethodType type, String... args)</code></td></tr>
 * <tr><td>2</td><td><code>CallSite bootstrap(Lookup caller, String name, MethodType type, String x, int y)</code></td></tr>
 * </table>
 * 最后一个示例假设额外参数的类型分别为 {@code CONSTANT_String} 和 {@code CONSTANT_Integer}。
 * 倒数第二个示例假设所有额外参数的类型为 {@code CONSTANT_String}。
 * 其他示例适用于所有类型的额外参数。
 * <p>
 * 如上所述，引导方法的实际方法类型可以变化。
 * 例如，第四个参数可以是 {@code MethodHandle}，如果这是 {@code CONSTANT_InvokeDynamic} 条目中对应常量的类型。
 * 在这种情况下，{@code MethodHandle.invoke} 调用将把额外的方法句柄常量作为 {@code Object} 传递，但 {@code MethodHandle.invoke} 的类型匹配机制
 * 将在调用引导方法之前将引用转换回 {@code MethodHandle}。
 * （如果传递的是字符串常量，由错误生成的代码，该转换将失败，导致 {@code BootstrapMethodError}。）
 * <p>
 * 请注意，根据上述规则，引导方法可以接受一个基本类型参数，如果它可以由常量池条目表示。
 * 但是，类型为 {@code boolean}、{@code byte}、{@code short} 或 {@code char} 的参数不能为引导方法创建，
 * 因为这些常量不能直接表示在常量池中，引导方法的调用也不会执行必要的窄化基本类型转换。
 * <p>
 * 额外的引导方法参数旨在允许语言实现者安全且紧凑地编码元数据。
 * 原则上，名称和额外参数是冗余的，因为每个调用点可以有自己的唯一引导方法。
 * 这种做法可能会产生较大的类文件和常量池。
 *
 * @author John Rose, JSR 292 EG
 * @since 1.7
 */

package java.lang.invoke;
