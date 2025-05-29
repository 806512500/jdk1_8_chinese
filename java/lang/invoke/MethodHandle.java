
/*
 * Copyright (c) 2008, 2020, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.invoke;


import java.util.*;

import static java.lang.invoke.MethodHandleStatics.*;

/**
 * 方法句柄是对其底层方法、构造函数、字段或类似低级操作的类型化、直接可执行的引用，可选地包括
 * 参数或返回值的转换。
 * 这些转换非常通用，包括诸如 {@linkplain #asType 转换}、
 * {@linkplain #bindTo 插入}、
 * {@linkplain java.lang.invoke.MethodHandles#dropArguments 删除} 和
 * {@linkplain java.lang.invoke.MethodHandles#filterArguments 替换} 等模式。
 *
 * <h1>方法句柄内容</h1>
 * 方法句柄根据其参数和返回类型动态且强类型化。
 * 它们不通过其底层方法的名称或定义类来区分。
 * 必须使用与方法句柄自身 {@linkplain #type 类型描述符} 匹配的符号类型描述符来调用方法句柄。
 * <p>
 * 每个方法句柄都通过 {@link #type 类型} 访问器报告其类型描述符。
 * 此类型描述符是一个 {@link java.lang.invoke.MethodType MethodType} 对象，
 * 其结构是一系列类，其中一个类是方法的返回类型（如果没有，则为 {@code void.class}）。
 * <p>
 * 方法句柄的类型控制它接受的调用类型以及适用的转换类型。
 * <p>
 * 方法句柄包含两个特殊调用方法，称为 {@link #invokeExact 调用精确} 和 {@link #invoke 调用}。
 * 两个调用方法都提供了对方法句柄底层方法、构造函数、字段或其他操作的直接访问，
 * 以及对参数和返回值的转换。
 * 两个调用者都接受与方法句柄自身类型完全匹配的调用。
 * 平凡的不精确调用者还接受其他一系列调用类型。
 * <p>
 * 方法句柄是不可变的，没有可见的状态。
 * 当然，它们可以绑定到表现出状态的底层方法或数据。
 * 关于 Java 内存模型，任何方法句柄的行为都
 * 好像它的所有（内部）字段都是最终变量。这意味着任何对应用程序可见的方法句柄
 * 始终是完全形成的。
 * 即使方法句柄是通过共享变量在数据竞争中发布的，也是如此。
 * <p>
 * 方法句柄不能被用户子类化。
 * 实现可能会（或可能不会）创建 {@code MethodHandle} 的内部子类，
 * 这些子类可能通过 {@link java.lang.Object#getClass Object.getClass}
 * 操作可见。程序员不应从方法句柄的具体类中得出结论，因为方法句柄类层次结构（如果有的话）
 * 可能会随着时间的推移或不同供应商的实现而变化。
 *
 * <h1>方法句柄编译</h1>
 * 命名 {@code invokeExact} 或 {@code invoke} 的 Java 方法调用表达式可以从 Java 源代码调用方法句柄。
 * 从源代码的角度来看，这些方法可以接受任何参数，其结果可以转换为任何返回类型。
 * 正式来说，这是通过给调用方法 {@code Object} 返回类型和可变参数 {@code Object} 参数来实现的，
 * 但它们具有称为 <em>签名多态性</em> 的额外特性，这将这种调用自由直接连接到 JVM 执行堆栈。
 * <p>
 * 与虚拟方法通常情况一样，源代码级别的 {@code invokeExact} 和 {@code invoke} 调用编译为 {@code invokevirtual} 指令。
 * 更不寻常的是，编译器必须记录实际参数类型，
 * 并且不得对参数执行方法调用转换。
 * 相反，它必须根据参数的未转换类型将它们推送到堆栈上。
 * 方法句柄对象本身在参数之前被推送到堆栈上。
 * 然后编译器使用符号类型描述符调用方法句柄，
 * 该描述符描述了参数和返回类型。
 * <p>
 * 为了发出完整的符号类型描述符，编译器还必须确定返回类型。
 * 这是基于方法调用表达式上的类型转换（如果有），或者是 {@code Object}（如果调用是表达式）
 * 或者是 {@code void}（如果调用是语句）。
 * 类型转换可以是原始类型（但不能是 {@code void}）。
 * <p>
 * 作为一个特殊情况，未转换的 {@code null} 参数被赋予
 * 符号类型描述符 {@code java.lang.Void}。
 * 与类型 {@code Void} 的歧义是无害的，因为除了空引用外，没有 {@code Void} 类型的引用。
 *
 * <h1>方法句柄调用</h1>
 * 第一次执行 {@code invokevirtual} 指令时，它通过符号解析指令中的名称并验证方法调用在静态上是合法的来链接。
 * 这适用于对 {@code invokeExact} 和 {@code invoke} 的调用。
 * 在这种情况下，编译器发出的符号类型描述符被检查
 * 以确保其语法正确且包含的名称已解析。
 * 因此，只要符号类型描述符语法良好且类型存在，
 * 调用方法句柄的 {@code invokevirtual} 指令总是会链接。
 * <p>
 * 当 {@code invokevirtual} 在链接后执行时，JVM 首先检查接收方法句柄的类型
 * 以确保其与符号类型描述符匹配。
 * 如果类型匹配失败，这意味着调用者调用的方法
 * 并不在被调用的特定方法句柄上。
 * <p>
 * 在 {@code invokeExact} 的情况下，调用的类型描述符（在解析符号类型名称后）必须与接收方法句柄的方法类型
 * 完全匹配。
 * 在普通、不精确的 {@code invoke} 的情况下，解析的类型描述符
 * 必须是接收者的 {@link #asType asType} 方法的有效参数。
 * 因此，普通的 {@code invoke} 比 {@code invokeExact} 更宽松。
 * <p>
 * 类型匹配后，对 {@code invokeExact} 的调用直接
 * 立即调用方法句柄的底层方法
 * （或其他行为，视情况而定）。
 * <p>
 * 如果调用者指定的符号类型描述符与方法句柄自身的类型完全匹配，
 * 对普通 {@code invoke} 的调用与对 {@code invokeExact} 的调用相同。
 * 如果类型不匹配，{@code invoke} 尝试
 * 调整接收方法句柄的类型，
 * 好像通过调用 {@link #asType asType} 一样，
 * 以获得一个可以精确调用的方法句柄 {@code M2}。
 * 这允许调用者和被调用者之间进行更强大的方法类型协商。
 * <p>
 * （<em>注意：</em> 调整后的方法句柄 {@code M2} 是不可直接观察的，
 * 因此实现不必将其具体化。）
 *
 * <h1>调用检查</h1>
 * 在典型的程序中，方法句柄类型匹配通常会成功。
 * 但如果匹配失败，JVM 将抛出 {@link WrongMethodTypeException}，
 * 要么直接（在 {@code invokeExact} 的情况下），要么间接地好像
 * 通过失败的 {@code asType} 调用（在 {@code invoke} 的情况下）。
 * <p>
 * 因此，可能在静态类型程序中表现为链接错误的方法类型不匹配
 * 在使用方法句柄的程序中可能表现为
 * 动态的 {@code WrongMethodTypeException}。
 * <p>
 * 由于方法类型包含“活的” {@code Class} 对象，
 * 方法类型匹配考虑到了类型名称和类加载器。
 * 因此，即使方法句柄 {@code M} 在一个
 * 类加载器 {@code L1} 中创建并在另一个 {@code L2} 中使用，
 * 方法句柄调用也是类型安全的，因为调用者的符号类型
 * 描述符（在 {@code L2} 中解析）与原始被调用方法的符号类型描述符匹配，
 * 该描述符在 {@code L1} 中解析。
 * 在 {@code L1} 中的解析发生在创建 {@code M} 时
 * 并分配其类型时，而在 {@code L2} 中的解析发生在
 * {@code invokevirtual} 指令链接时。
 * <p>
 * 除了类型描述符的检查外，
 * 方法句柄调用其底层方法的能力不受限制。
 * 如果一个方法句柄是在一个类上形成的，该类对该方法有访问权限，
 * 则生成的句柄可以在任何地方由任何接收到其引用的调用者使用。
 * <p>
 * 与核心反射 API 不同，每次调用反射方法时都会检查访问权限，
 * 方法句柄的访问检查是在
 * <a href="MethodHandles.Lookup.html#access">创建方法句柄时</a> 进行的。
 * 在 {@code ldc} 的情况下（见下文），访问检查是在链接
 * 常量方法句柄的常量池条目时进行的。
 * <p>
 * 因此，非公共方法或非公共类的方法的句柄
 * 通常应保密。
 * 除非从不可信代码使用它们是无害的，否则不应将它们传递给不可信代码。
 *
 * <h1>方法句柄创建</h1>
 * Java 代码可以创建一个直接访问
 * 任何对该代码可访问的方法、构造函数或字段的方法句柄。
 * 这是通过一个称为
 * {@link java.lang.invoke.MethodHandles.Lookup MethodHandles.Lookup} 的反射、基于能力的 API 完成的。
 * 例如，可以通过 {@link java.lang.invoke.MethodHandles.Lookup#findStatic Lookup.findStatic} 获得静态方法句柄。
 * 还有从核心反射 API 对象转换的方法，
 * 例如 {@link java.lang.invoke.MethodHandles.Lookup#unreflect Lookup.unreflect}。
 * <p>
 * 与类和字符串一样，方法句柄也可以直接表示为
 * 类文件常量池中的常量，由 {@code ldc} 字节码加载。
 * 新类型的常量池条目 {@code CONSTANT_MethodHandle}
 * 直接引用关联的 {@code CONSTANT_Methodref}、
 * {@code CONSTANT_InterfaceMethodref} 或 {@code CONSTANT_Fieldref}
 * 常量池条目。
 * （有关方法句柄常量的完整详细信息，
 * 请参阅 Java 虚拟机规范的第 4.4.8 节和第 5.4.3.5 节。）
 * <p>
 * 通过查找或从具有可变参数修饰符位（{@code 0x0080}）的方法或
 * 构造函数常量加载生成的方法句柄具有相应的可变参数，就像它们是通过
 * {@link #asVarargsCollector asVarargsCollector} 定义的一样。
 * <p>
 * 方法引用可以引用静态方法或非静态方法。
 * 在非静态情况下，方法句柄类型包括一个显式的
 * 接收器参数，位于其他任何参数之前。
 * 在方法句柄的类型中，初始接收器参数的类型
 * 根据最初请求方法的类来确定。
 * （例如，如果通过 {@code ldc} 获得非静态方法句柄，
 * 接收器的类型是常量池条目中命名的类。）
 * <p>
 * 方法句柄常量受与其对应的字节码指令相同的链接时访问检查，
 * 如果字节码行为会抛出此类错误，
 * 则 {@code ldc} 指令将抛出相应的链接错误。
 * <p>
 * 作为这一点的推论，对受保护成员的访问仅限于
 * 访问类或其子类的接收器，
 * 并且访问类本身必须是受保护成员的定义类的子类（或包兄弟类）。
 * 如果方法引用引用了当前包外类的受保护非静态方法或字段，
 * 接收器参数将
 * 被缩小为访问类的类型。
 * <p>
 * 当调用虚拟方法的方法句柄时，方法
 * 总是在接收器（即第一个参数）中查找。
 * <p>
 * 也可以创建一个特定虚拟方法实现的非虚拟方法句柄。
 * 这些方法句柄不会根据接收器类型执行虚拟查找。
 * 这样的方法句柄模拟了
 * 对同一方法的 {@code invokespecial} 指令的效果。
 *
 * <h1>使用示例</h1>
 * 以下是一些使用示例：
 * <blockquote><pre>{@code
Object x, y; String s; int i;
MethodType mt; MethodHandle mh;
MethodHandles.Lookup lookup = MethodHandles.lookup();
// mt 是 (char,char)String
mt = MethodType.methodType(String.class, char.class, char.class);
mh = lookup.findVirtual(String.class, "replace", mt);
s = (String) mh.invokeExact("daddy",'d','n');
// invokeExact(Ljava/lang/String;CC)Ljava/lang/String;
assertEquals(s, "nanny");
// 弱类型调用（使用 MHs.invoke）
s = (String) mh.invokeWithArguments("sappy", 'p', 'v');
assertEquals(s, "savvy");
// mt 是 (Object[])List
mt = MethodType.methodType(java.util.List.class, Object[].class);
mh = lookup.findStatic(java.util.Arrays.class, "asList", mt);
assert(mh.isVarargsCollector());
x = mh.invoke("one", "two");
// invoke(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;
assertEquals(x, java.util.Arrays.asList("one","two"));
// mt 是 (Object,Object,Object)Object
mt = MethodType.genericMethodType(3);
mh = mh.asType(mt);
x = mh.invokeExact((Object)1, (Object)2, (Object)3);
// invokeExact(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
assertEquals(x, java.util.Arrays.asList(1,2,3));
// mt 是 ()int
mt = MethodType.methodType(int.class);
mh = lookup.findVirtual(java.util.List.class, "size", mt);
i = (int) mh.invokeExact(java.util.Arrays.asList(1,2,3));
// invokeExact(Ljava/util/List;)I
assert(i == 3);
mt = MethodType.methodType(void.class, String.class);
mh = lookup.findVirtual(java.io.PrintStream.class, "println", mt);
mh.invokeExact(System.out, "Hello, world.");
// invokeExact(Ljava/io/PrintStream;Ljava/lang/String;)V
 * }</pre></blockquote>
 * 上面每个对 {@code invokeExact} 或普通 {@code invoke} 的调用
 * 都生成一个带有
 * 下面注释中指示的符号类型描述符的单个 {@code invokevirtual} 指令。
 * 在这些示例中，假设辅助方法 {@code assertEquals} 是
 * 一个调用 {@link java.util.Objects#equals(Object,Object) Objects.equals}
 * 的方法，并断言结果为真。
 *
 * <h1>异常</h1>
 * 方法 {@code invokeExact} 和 {@code invoke} 被声明为
 * 抛出 {@link java.lang.Throwable Throwable}，
 * 这意味着对方法句柄可以抛出的异常没有静态限制。
 * 由于 JVM 不区分检查异常
 * 和非检查异常（当然，除了它们的类之外），
 * 对方法句柄调用的检查异常不会影响字节码的形状。
 * 但在 Java 源代码中，执行方法句柄调用的方法必须显式
 * 抛出 {@code Throwable}，或者必须本地捕获所有
 * 异常，仅重新抛出在上下文中合法的异常，
 * 并包装非法的异常。
 *
 * <h1><a name="sigpoly"></a>签名多态性</h1>
 * {@code invokeExact} 和普通 {@code invoke} 的
 * 不寻常的编译和链接行为
 * 由术语 <em>签名多态性</em> 引用。
 * 根据 Java 语言规范，
 * 签名多态方法是可以使用
 * 广泛的调用签名和返回类型操作的方法。
 * <p>
 * 在源代码中，对签名多态方法的调用将
 * 编译，无论请求的符号类型描述符是什么。
 * 通常，Java 编译器会发出带有给定符号类型描述符的 {@code invokevirtual}
 * 指令，针对命名的方法。
 * 不寻常的部分是符号类型描述符是从
 * 实际参数和返回类型派生的，而不是从方法声明派生的。
 * <p>
 * 当 JVM 处理包含签名多态调用的字节码时，
 * 它将成功链接任何此类调用，无论其符号类型描述符如何。
 * （为了保持类型安全，JVM 将通过适当的
 * 动态类型检查来保护此类调用，如其他地方所述。）
 * <p>
 * 字节码生成器，包括编译器后端，必须发出
 * 这些方法的未转换的符号类型描述符。
 * 确定符号链接的工具必须接受此类
 * 未转换的描述符，而不报告链接错误。
 *
 * <h1>方法句柄与核心反射 API 之间的互操作性</h1>
 * 使用 {@link java.lang.invoke.MethodHandles.Lookup Lookup} API 中的工厂方法，
 * 可以将任何由核心反射 API 对象表示的类成员
 * 转换为行为等效的方法句柄。
 * 例如，可以通过
 * {@link java.lang.invoke.MethodHandles.Lookup#unreflect Lookup.unreflect} 将反射的
 * {@link java.lang.reflect.Method Method} 转换为方法句柄。
 * 生成的方法句柄通常提供对底层类成员的更直接和高效的访问。
 * <p>
 * 作为一个特殊情况，
 * 当使用核心反射 API 查看签名多态方法
 * {@code invokeExact} 或普通 {@code invoke} 时，
 * 它们显示为普通的非多态方法。
 * 它们通过
 * {@link java.lang.Class#getDeclaredMethod Class.getDeclaredMethod} 查看的反射外观
 * 不受其在此 API 中特殊状态的影响。
 * 例如，{@link java.lang.reflect.Method#getModifiers Method.getModifiers}
 * 将报告任何类似声明的方法所需的精确修饰符位，包括在这种情况下
 * {@code native} 和 {@code varargs} 位。
 * <p>
 * 与任何反射方法一样，这些方法（当反射时）可以通过
 * {@link java.lang.reflect.Method#invoke java.lang.reflect.Method.invoke} 调用。
 * 但是，这样的反射调用不会导致方法句柄调用。
 * 如果传递了所需的参数
 * （一个，类型为 {@code Object[]}），则将忽略该参数并
 * 抛出一个 {@code UnsupportedOperationException}。
 * <p>
 * 由于 {@code invokevirtual} 指令可以以任何符号类型描述符
 * 本机调用方法句柄，这种反射视图与通过字节码呈现这些方法的正常方式冲突。
 * 因此，通过 {@code Class.getDeclaredMethod} 反射查看的这两个本机方法
 * 可以被视为仅占位符。
 * <p>
 * 为了获得特定类型描述符的调用者方法，
 * 使用 {@link java.lang.invoke.MethodHandles#exactInvoker MethodHandles.exactInvoker}，
 * 或 {@link java.lang.invoke.MethodHandles#invoker MethodHandles.invoker}。
 * {@link java.lang.invoke.MethodHandles.Lookup#findVirtual Lookup.findVirtual}
 * API 还可以返回一个方法句柄
 * 以调用 {@code invokeExact} 或普通 {@code invoke}，
 * 对于任何指定的类型描述符。
 *
 * <h1>方法句柄与 Java 泛型之间的互操作性</h1>
 * 可以在声明为具有 Java 泛型类型的方法、构造函数或字段上获得方法句柄。
 * 与核心反射 API 一样，方法句柄的类型
 * 将从源级类型的擦除中构造。
 * 当调用方法句柄时，其参数的类型
 * 或返回值的类型转换类型可以是泛型类型或类型实例。
 * 如果发生这种情况，编译器将在构造
 * {@code invokevirtual} 指令的符号类型描述符时
 * 将这些类型替换为它们的擦除类型。
 * <p>
 * 方法句柄不以 Java 参数化（泛型）类型的形式
 * 表示它们的函数类型，
 * 因为函数类型和参数化
 * Java 类型之间存在三个不匹配。
 * <ul>
 * <li>方法类型可以跨越所有可能的元数，
 * 从没有参数到最多允许的参数数量。
 * 泛型不是可变参数的，因此不能表示这一点。</li>
 * <li>方法类型可以指定原始类型的参数，
 * 而 Java 泛型类型不能。</li>
 * <li>高阶函数（组合器）通常在广泛的函数类型上是泛型的，包括
 * 多个元数的类型。不可能用 Java 类型参数表示这种泛型。</li>
 * </ul>
 *
 * <h1><a name="maxarity"></a>元数限制</h1>
 * JVM 对任何类型的所有方法和构造函数施加了一个绝对
 * 255 个堆栈参数的限制。在某些情况下，这个限制可能显得更严格：
 * <ul>
 * <li>{@code long} 或 {@code double} 参数（为了元数限制的目的）计为两个参数槽。</li>
 * <li>非静态方法为调用该方法的对象消耗一个额外的参数。</li>
 * <li>构造函数为正在构造的对象消耗一个额外的参数。</li>
 * <li>由于方法句柄的 {@code invoke} 方法（或其他签名多态方法）是非虚拟的，
 *     它为方法句柄本身消耗一个额外的参数，此外还有任何非虚拟接收对象。</li>
 * </ul>
 * 这些限制意味着某些方法句柄无法创建，仅仅是因为 JVM 对堆栈参数的限制。
 * 例如，如果一个静态 JVM 方法接受恰好 255 个参数，则无法为其创建方法句柄。
 * 尝试创建具有不可能方法类型的方法句柄将导致 {@link IllegalArgumentException}。
 * 特别是，方法句柄的类型不能具有恰好最大值 255 的元数。
 *
 * @see MethodType
 * @see MethodHandles
 * @author John Rose, JSR 292 EG
 */
public abstract class MethodHandle {
    static { MethodHandleImpl.initStatics(); }

                /**
     * 内部标记接口，用于区分（对Java编译器而言）
     * 那些方法是 <a href="MethodHandle.html#sigpoly">签名多态</a> 的。
     */
    @java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @interface PolymorphicSignature { }

    private final MethodType type;
    /*private*/ final LambdaForm form;
    // form 不是私有的，以便调用者可以轻松获取它
    /*private*/ MethodHandle asTypeCache;
    // asTypeCache 不是私有的，以便调用者可以轻松获取它
    /*non-public*/ byte customizationCount;
    // customizationCount 应该对调用者可访问

    /**
     * 报告此方法句柄的类型。
     * 通过 {@code invokeExact} 调用此方法句柄的每次调用必须精确匹配此类型。
     * @return 方法句柄类型
     */
    public MethodType type() {
        return type;
    }

    /**
     * 方法句柄实现层次结构的包私有构造函数。
     * 方法句柄继承将完全包含在
     * {@code java.lang.invoke} 包内。
     */
    // @param type 新方法句柄的类型（永久分配）
    /*non-public*/ MethodHandle(MethodType type, LambdaForm form) {
        type.getClass();  // 显式 NPE
        form.getClass();  // 显式 NPE
        this.type = type;
        this.form = form.uncustomize();

        this.form.prepare();  // TO DO:  尝试将此步骤延迟到调用前。
    }

    /**
     * 调用方法句柄，允许任何调用者类型描述符，但要求类型精确匹配。
     * 调用点的符号类型描述符必须
     * 精确匹配此方法句柄的 {@link #type 类型}。
     * 不允许对参数或返回值进行转换。
     * <p>
     * 当通过核心反射API观察此方法时，
     * 它将显示为一个原生方法，接受一个对象数组并返回一个对象。
     * 如果此原生方法直接通过
     * {@link java.lang.reflect.Method#invoke java.lang.reflect.Method.invoke}，通过JNI，
     * 或间接通过 {@link java.lang.invoke.MethodHandles.Lookup#unreflect Lookup.unreflect} 调用，
     * 它将抛出一个 {@code UnsupportedOperationException}。
     * @param args 签名多态参数列表，静态表示使用 varargs
     * @return 签名多态结果，静态表示使用 {@code Object}
     * @throws WrongMethodTypeException 如果目标的类型与调用者的符号类型描述符不一致
     * @throws Throwable 由底层方法抛出的任何异常都会通过方法句柄调用不变地传播
     */
    public final native @PolymorphicSignature Object invokeExact(Object... args) throws Throwable;

    /**
     * 调用方法句柄，允许任何调用者类型描述符，
     * 并可选地对参数和返回值进行转换。
     * <p>
     * 如果调用点的符号类型描述符精确匹配此方法句柄的 {@link #type 类型}，
     * 调用将像 {@link #invokeExact invokeExact} 一样进行。
     * <p>
     * 否则，调用将像首先
     * 调用 {@link #asType asType} 调整此方法句柄
     * 到所需类型，然后调用像
     * {@link #invokeExact invokeExact} 在调整后的句柄上进行。
     * <p>
     * 没有保证实际会调用 {@code asType}。
     * 如果JVM可以预测调用的结果，它可能会直接对调用者的参数进行转换，
     * 并根据目标方法句柄的精确类型调用目标方法句柄。
     * <p>
     * 调用点的解析类型描述符必须
     * 是接收者 {@code asType} 方法的有效参数。
     * 特别是，调用者必须指定与被调用者类型相同的参数个数，
     * 如果被调用者不是一个 {@linkplain #asVarargsCollector 可变参数收集器}。
     * <p>
     * 当通过核心反射API观察此方法时，
     * 它将显示为一个原生方法，接受一个对象数组并返回一个对象。
     * 如果此原生方法直接通过
     * {@link java.lang.reflect.Method#invoke java.lang.reflect.Method.invoke}，通过JNI，
     * 或间接通过 {@link java.lang.invoke.MethodHandles.Lookup#unreflect Lookup.unreflect} 调用，
     * 它将抛出一个 {@code UnsupportedOperationException}。
     * @param args 签名多态参数列表，静态表示使用 varargs
     * @return 签名多态结果，静态表示使用 {@code Object}
     * @throws WrongMethodTypeException 如果目标的类型不能调整到调用者的符号类型描述符
     * @throws ClassCastException 如果目标的类型可以调整到调用者，但引用转换失败
     * @throws Throwable 由底层方法抛出的任何异常都会通过方法句柄调用不变地传播
     */
    public final native @PolymorphicSignature Object invoke(Object... args) throws Throwable;

    /**
     * 受信任的方法句柄调用的私有方法，尊重简化签名。
     * 类型不匹配不会抛出 {@code WrongMethodTypeException}，但可能会导致JVM崩溃。
     * <p>
     * 调用者的签名仅限于以下基本类型：
     * Object, int, long, float, double 和 void 返回。
     * <p>
     * 调用者负责通过确保
     * 每个传出参数值是对应
     * 被调用者参数类型的范围内成员来维护类型正确性。
     * （因此，调用者应该对传出参数值进行适当的类型转换和整数缩小操作。）
     * 调用者可以假设传入的结果值是被调用者返回类型的范围内的一部分。
     * @param args 签名多态参数列表，静态表示使用 varargs
     * @return 签名多态结果，静态表示使用 {@code Object}
     */
    /*non-public*/ final native @PolymorphicSignature Object invokeBasic(Object... args) throws Throwable;

                /**
     * 私有方法，用于可信调用类型为 {@code REF_invokeVirtual} 的 MemberName。
     * 调用者签名仅限于基本类型，如同 {@code invokeBasic}。
     * 尾随（而非前导）参数必须是 MemberName。
     * @param args 签名多态参数列表，静态表示使用 varargs
     * @return 签名多态结果，静态表示使用 {@code Object}
     */
    /*non-public*/ static native @PolymorphicSignature Object linkToVirtual(Object... args) throws Throwable;

    /**
     * 私有方法，用于可信调用类型为 {@code REF_invokeStatic} 的 MemberName。
     * 调用者签名仅限于基本类型，如同 {@code invokeBasic}。
     * 尾随（而非前导）参数必须是 MemberName。
     * @param args 签名多态参数列表，静态表示使用 varargs
     * @return 签名多态结果，静态表示使用 {@code Object}
     */
    /*non-public*/ static native @PolymorphicSignature Object linkToStatic(Object... args) throws Throwable;

    /**
     * 私有方法，用于可信调用类型为 {@code REF_invokeSpecial} 的 MemberName。
     * 调用者签名仅限于基本类型，如同 {@code invokeBasic}。
     * 尾随（而非前导）参数必须是 MemberName。
     * @param args 签名多态参数列表，静态表示使用 varargs
     * @return 签名多态结果，静态表示使用 {@code Object}
     */
    /*non-public*/ static native @PolymorphicSignature Object linkToSpecial(Object... args) throws Throwable;

    /**
     * 私有方法，用于可信调用类型为 {@code REF_invokeInterface} 的 MemberName。
     * 调用者签名仅限于基本类型，如同 {@code invokeBasic}。
     * 尾随（而非前导）参数必须是 MemberName。
     * @param args 签名多态参数列表，静态表示使用 varargs
     * @return 签名多态结果，静态表示使用 {@code Object}
     */
    /*non-public*/ static native @PolymorphicSignature Object linkToInterface(Object... args) throws Throwable;

    /**
     * 执行可变参数调用，将给定列表中的参数传递给方法句柄，就像通过一个仅提及类型 {@code Object} 的调用点进行不精确的 {@link #invoke invoke} 调用一样，
     * 该调用点的参数个数为参数列表的长度。
     * <p>
     * 具体来说，执行过程如下，尽管如果 JVM 可以预测这些方法的效果，则不保证调用这些方法。
     * <ul>
     * <li>确定参数数组的长度为 {@code N}。对于 null 引用，{@code N=0}。</li>
     * <li>确定 {@code N} 个参数的一般类型 {@code TN}，即 {@code TN=MethodType.genericMethodType(N)}。</li>
     * <li>强制原始目标方法句柄 {@code MH0} 转换为所需类型，即 {@code MH1 = MH0.asType(TN)}。</li>
     * <li>将数组展开为 {@code N} 个单独的参数 {@code A0, ...}。</li>
     * <li>在解包后的参数上调用类型调整后的句柄：MH1.invokeExact(A0, ...)。</li>
     * <li>将返回值作为 {@code Object} 引用。</li>
     * </ul>
     * <p>
     * 由于 {@code asType} 步骤的作用，必要时将应用以下参数转换：
     * <ul>
     * <li>引用类型转换
     * <li>拆箱
     * <li>扩展基本类型转换
     * </ul>
     * <p>
     * 如果返回类型是基本类型，调用的结果将被装箱；如果返回类型是 void，则结果将被强制为 null。
     * <p>
     * 此调用等同于以下代码：
     * <blockquote><pre>{@code
     * MethodHandle invoker = MethodHandles.spreadInvoker(this.type(), 0);
     * Object result = invoker.invokeExact(this, arguments);
     * }</pre></blockquote>
     * <p>
     * 与签名多态方法 {@code invokeExact} 和 {@code invoke} 不同，{@code invokeWithArguments} 可以通过核心反射 API 和 JNI 正常访问。
     * 因此，它可以作为本地或反射代码与方法句柄之间的桥梁。
     *
     * @param arguments 要传递给目标的参数
     * @return 目标返回的结果
     * @throws ClassCastException 如果参数不能通过引用类型转换
     * @throws WrongMethodTypeException 如果目标类型不能调整为接受给定数量的 {@code Object} 参数
     * @throws Throwable 目标方法调用抛出的任何异常
     * @see MethodHandles#spreadInvoker
     */
    public Object invokeWithArguments(Object... arguments) throws Throwable {
        MethodType invocationType = MethodType.genericMethodType(arguments == null ? 0 : arguments.length);
        return invocationType.invokers().spreadInvoker(0).invokeExact(asType(invocationType), arguments);
    }

    /**
     * 执行可变参数调用，将给定数组中的参数传递给方法句柄，就像通过一个仅提及类型 {@code Object} 的调用点进行不精确的 {@link #invoke invoke} 调用一样，
     * 该调用点的参数个数为参数数组的长度。
     * <p>
     * 此方法也等同于以下代码：
     * <blockquote><pre>{@code
     *   invokeWithArguments(arguments.toArray()
     * }</pre></blockquote>
     *
     * @param arguments 要传递给目标的参数
     * @return 目标返回的结果
     * @throws NullPointerException 如果 {@code arguments} 是 null 引用
     * @throws ClassCastException 如果参数不能通过引用类型转换
     * @throws WrongMethodTypeException 如果目标类型不能调整为接受给定数量的 {@code Object} 参数
     * @throws Throwable 目标方法调用抛出的任何异常
     */
    public Object invokeWithArguments(java.util.List<?> arguments) throws Throwable {
        return invokeWithArguments(arguments.toArray());
    }

                /**
     * 生成一个适配器方法句柄，该适配器方法句柄将当前方法句柄的类型转换为新类型。
     * 生成的方法句柄保证报告的类型与所需的新类型相等。
     * <p>
     * 如果原始类型和新类型相等，则返回 {@code this}。
     * <p>
     * 当调用新方法句柄时，它将执行以下步骤：
     * <ul>
     * <li>将传入的参数列表转换为匹配原始方法句柄的参数列表。
     * <li>在转换后的参数列表上调用原始方法句柄。
     * <li>将原始方法句柄返回的任何结果转换为新方法句柄的返回类型。
     * </ul>
     * <p>
     * 该方法提供了 {@link #invokeExact invokeExact} 和普通的、不精确的 {@link #invoke invoke} 之间至关重要的行为差异。
     * 当调用者和被调用者的类型描述符完全匹配时，这两个方法执行相同的步骤，但当类型不同时，普通的 {@link #invoke invoke} 还会调用 {@code asType}（或某些内部等效方法）以匹配调用者和被调用者的类型。
     * <p>
     * 如果当前方法是一个可变参数方法句柄，则参数列表转换可能涉及将多个参数转换并收集到一个数组中，如 {@linkplain #asVarargsCollector 其他地方所述}。
     * 在所有其他情况下，所有转换都是成对应用的，这意味着每个参数或返回值都被转换为恰好一个参数或返回值（或没有返回值）。
     * 应用的转换是通过咨询旧方法句柄类型和新方法句柄类型对应的组件类型来定义的。
     * <p>
     * 设 <em>T0</em> 和 <em>T1</em> 是对应的新旧参数类型，或旧新返回类型。具体来说，对于某个有效的索引 {@code i}，设
     * <em>T0</em>{@code =newType.parameterType(i)} 和 <em>T1</em>{@code =this.type().parameterType(i)}。
     * 或者，对于返回值，设
     * <em>T0</em>{@code =this.type().returnType()} 和 <em>T1</em>{@code =newType.returnType()}。
     * 如果类型相同，则新方法句柄不会更改对应的参数或返回值（如果有的话）。
     * 否则，如果可能，将应用以下转换之一：
     * <ul>
     * <li>如果 <em>T0</em> 和 <em>T1</em> 是引用类型，则应用到 <em>T1</em> 的类型转换。
     *     （这些类型不需要以任何特定方式相关。这是因为动态值 null 可以转换为任何引用类型。）
     * <li>如果 <em>T0</em> 和 <em>T1</em> 是基本类型，则应用 Java 方法调用转换（JLS 5.3），如果存在的话。
     *     （具体来说，<em>T0</em> 必须通过扩展基本类型转换为 <em>T1</em>。）
     * <li>如果 <em>T0</em> 是基本类型而 <em>T1</em> 是引用类型，则应用 Java 类型转换（JLS 5.5），如果存在的话。
     *     （具体来说，值从 <em>T0</em> 装箱为其包装类，然后根据需要扩展为 <em>T1</em>。）
     * <li>如果 <em>T0</em> 是引用类型而 <em>T1</em> 是基本类型，则在运行时应用拆箱转换，可能随后应用 Java 方法调用转换（JLS 5.3）
     *     对基本类型值进行转换。（这些是基本类型的扩展转换。）
     *     <em>T0</em> 必须是包装类或其超类型。（在 <em>T0</em> 是 Object 的情况下，这些是 {@link java.lang.reflect.Method#invoke java.lang.reflect.Method.invoke} 允许的转换。）
     *     拆箱转换必须有成功的可能性，这意味着如果 <em>T0</em> 本身不是包装类，则必须存在至少一个
     *     包装类 <em>TW</em>，它是 <em>T0</em> 的子类型，并且其拆箱后的基本类型值可以扩展为 <em>T1</em>。
     * <li>如果返回类型 <em>T1</em> 被标记为 void，则任何返回值都将被丢弃。
     * <li>如果返回类型 <em>T0</em> 是 void 而 <em>T1</em> 是引用类型，则引入一个 null 值。
     * <li>如果返回类型 <em>T0</em> 是 void 而 <em>T1</em> 是基本类型，则引入一个零值。
     * </ul>
     * （<em>注意：</em> 两个 <em>T0</em> 和 <em>T1</em> 都可以被视为静态类型，因为它们并不具体对应于任何实际参数或返回值的 <em>动态类型</em>。）
     * <p>
     * 如果任何所需的成对转换无法进行，则无法进行方法句柄转换。
     * <p>
     * 在运行时，应用于引用参数或返回值的转换可能需要额外的运行时检查，这些检查可能会失败。
     * 拆箱操作可能因为原始引用是 null 而失败，导致 {@link java.lang.NullPointerException NullPointerException}。
     * 拆箱操作或引用类型转换也可能因为引用的对象类型不正确而失败，
     * 导致 {@link java.lang.ClassCastException ClassCastException}。
     * 虽然拆箱操作可以接受几种包装器，但如果没有任何可用的包装器，将抛出 {@code ClassCastException}。
     *
     * @param newType 新方法句柄的预期类型
     * @return 一个方法句柄，该方法句柄在执行任何必要的参数转换后委托给 {@code this}，并安排任何必要的返回值转换
     * @throws NullPointerException 如果 {@code newType} 是 null 引用
     * @throws WrongMethodTypeException 如果无法进行转换
     * @see MethodHandles#explicitCastArguments
     */
    public MethodHandle asType(MethodType newType) {
        // 快速路径替代重量级的 {@code asType} 调用。
        // 如果转换将是一个无操作，则返回 'this'。
        if (newType == type) {
            return this;
        }
        // 如果转换已经缓存，则返回 'this.asTypeCache'。
        MethodHandle atc = asTypeCached(newType);
        if (atc != null) {
            return atc;
        }
        return asTypeUncached(newType);
    }


                private MethodHandle asTypeCached(MethodType newType) {
        MethodHandle atc = asTypeCache;
        if (atc != null && newType == atc.type) {
            return atc;
        }
        return null;
    }

    /** 覆盖此方法以更改 asType 行为。 */
    /*non-public*/ MethodHandle asTypeUncached(MethodType newType) {
        if (!type.isConvertibleTo(newType))
            throw new WrongMethodTypeException("无法将 " + this + " 转换为 " + newType);
        return asTypeCache = MethodHandleImpl.makePairwiseConvert(this, newType, true);
    }

    /**
     * 创建一个 <em>数组展开</em> 方法句柄，该方法句柄接受一个尾随的数组参数
     * 并将其元素作为位置参数展开。
     * 新的方法句柄将其 <i>目标</i> 适配为当前方法句柄。适配器的类型将与目标的类型相同，
     * 但目标类型的最后 {@code arrayLength} 个参数将被替换为一个类型为 {@code arrayType} 的单个数组参数。
     * <p>
     * 如果数组元素类型与原始目标的任何对应参数类型不同，
     * 则原始目标将被适配为直接接受数组元素，
     * 就像调用 {@link #asType asType} 一样。
     * <p>
     * 当调用适配器时，它将尾随的数组参数替换为数组的元素，
     * 每个元素作为目标的一个独立参数。参数的顺序保持不变。
     * 它们通过强制转换和/或拆箱转换为目标尾随参数的类型。
     * 最后调用目标。目标最终返回的内容由适配器不变地返回。
     * <p>
     * 在调用目标之前，适配器会验证数组是否包含足够的元素以提供正确的参数数量
     * 给目标方法句柄。（当需要零个元素时，数组也可以为 null。）
     * <p>
     * 如果在调用适配器时，提供的数组参数的元素数量不正确，适配器将抛出
     * 一个 {@link IllegalArgumentException} 而不是调用目标。
     * <p>
     * 以下是一些简单的数组展开方法句柄示例：
     * <blockquote><pre>{@code
MethodHandle equals = publicLookup()
  .findVirtual(String.class, "equals", methodType(boolean.class, Object.class));
assert( (boolean) equals.invokeExact("me", (Object)"me"));
assert(!(boolean) equals.invokeExact("me", (Object)"thee"));
// 从 2-数组中展开两个参数：
MethodHandle eq2 = equals.asSpreader(Object[].class, 2);
assert( (boolean) eq2.invokeExact(new Object[]{ "me", "me" }));
assert(!(boolean) eq2.invokeExact(new Object[]{ "me", "thee" }));
// 尝试从非 2-数组中展开：
for (int n = 0; n <= 10; n++) {
  Object[] badArityArgs = (n == 2 ? null : new Object[n]);
  try { assert((boolean) eq2.invokeExact(badArityArgs) && false); }
  catch (IllegalArgumentException ex) { } // OK
}
// 从 String 数组中展开两个参数：
MethodHandle eq2s = equals.asSpreader(String[].class, 2);
assert( (boolean) eq2s.invokeExact(new String[]{ "me", "me" }));
assert(!(boolean) eq2s.invokeExact(new String[]{ "me", "thee" }));
// 从 1-数组中展开第二个参数：
MethodHandle eq1 = equals.asSpreader(Object[].class, 1);
assert( (boolean) eq1.invokeExact("me", new Object[]{ "me" }));
assert(!(boolean) eq1.invokeExact("me", new Object[]{ "thee" }));
// 从 0-数组或 null 中展开零个参数：
MethodHandle eq0 = equals.asSpreader(Object[].class, 0);
assert( (boolean) eq0.invokeExact("me", (Object)"me", new Object[0]));
assert(!(boolean) eq0.invokeExact("me", (Object)"thee", (Object[])null));
// asSpreader 和 asCollector 是近似互逆的：
for (int n = 0; n <= 2; n++) {
    for (Class<?> a : new Class<?>[]{Object[].class, String[].class, CharSequence[].class}) {
        MethodHandle equals2 = equals.asSpreader(a, n).asCollector(a, n);
        assert( (boolean) equals2.invokeWithArguments("me", "me"));
        assert(!(boolean) equals2.invokeWithArguments("me", "thee"));
    }
}
MethodHandle caToString = publicLookup()
  .findStatic(Arrays.class, "toString", methodType(String.class, char[].class));
assertEquals("[A, B, C]", (String) caToString.invokeExact("ABC".toCharArray()));
MethodHandle caString3 = caToString.asCollector(char[].class, 3);
assertEquals("[A, B, C]", (String) caString3.invokeExact('A', 'B', 'C'));
MethodHandle caToString2 = caString3.asSpreader(char[].class, 2);
assertEquals("[A, B, C]", (String) caToString2.invokeExact('A', "BC".toCharArray()));
     * }</pre></blockquote>
     * @param arrayType 通常为 {@code Object[]}，从其中提取展开参数的数组参数类型
     * @param arrayLength 从传入的数组参数中展开的参数数量
     * @return 一个新的方法句柄，该方法句柄在其最终数组参数展开后调用原始方法句柄
     * @throws NullPointerException 如果 {@code arrayType} 是 null 引用
     * @throws IllegalArgumentException 如果 {@code arrayType} 不是数组类型，
     *         或者目标没有至少 {@code arrayLength} 个参数类型，
     *         或者 {@code arrayLength} 为负数，
     *         或者结果方法句柄的类型将有 <a href="MethodHandle.html#maxarity">过多的参数</a>
     * @throws WrongMethodTypeException 如果隐含的 {@code asType} 调用失败
     * @see #asCollector
     */
    public MethodHandle asSpreader(Class<?> arrayType, int arrayLength) {
        MethodType postSpreadType = asSpreaderChecks(arrayType, arrayLength);
        int arity = type().parameterCount();
        int spreadArgPos = arity - arrayLength;
        MethodHandle afterSpread = this.asType(postSpreadType);
        BoundMethodHandle mh = afterSpread.rebind();
        LambdaForm lform = mh.editor().spreadArgumentsForm(1 + spreadArgPos, arrayType, arrayLength);
        MethodType preSpreadType = postSpreadType.replaceParameterTypes(spreadArgPos, arity, arrayType);
        return mh.copyWith(preSpreadType, lform);
    }

                /**
     * 检查 {@code asSpreader} 是否可以使用给定的参数有效调用。
     * 返回在展开但转换前的方法句柄调用的类型。
     */
    private MethodType asSpreaderChecks(Class<?> arrayType, int arrayLength) {
        spreadArrayChecks(arrayType, arrayLength);
        int nargs = type().parameterCount();
        if (nargs < arrayLength || arrayLength < 0)
            throw newIllegalArgumentException("bad spread array length");
        Class<?> arrayElement = arrayType.getComponentType();
        MethodType mtype = type();
        boolean match = true, fail = false;
        for (int i = nargs - arrayLength; i < nargs; i++) {
            Class<?> ptype = mtype.parameterType(i);
            if (ptype != arrayElement) {
                match = false;
                if (!MethodType.canConvert(arrayElement, ptype)) {
                    fail = true;
                    break;
                }
            }
        }
        if (match)  return mtype;
        MethodType needType = mtype.asSpreaderType(arrayType, arrayLength);
        if (!fail)  return needType;
        // 引发错误：
        this.asType(needType);
        throw newInternalError("should not return", null);
    }

    private void spreadArrayChecks(Class<?> arrayType, int arrayLength) {
        Class<?> arrayElement = arrayType.getComponentType();
        if (arrayElement == null)
            throw newIllegalArgumentException("not an array type", arrayType);
        if ((arrayLength & 0x7F) != arrayLength) {
            if ((arrayLength & 0xFF) != arrayLength)
                throw newIllegalArgumentException("array length is not legal", arrayLength);
            assert(arrayLength >= 128);
            if (arrayElement == long.class ||
                arrayElement == double.class)
                throw newIllegalArgumentException("array length is not legal for long[] or double[]", arrayLength);
        }
    }

    /**
     * 创建一个 <em>数组收集</em> 方法句柄，该方法句柄接受一定数量的尾随位置参数并将它们收集到一个数组参数中。
     * 新的方法句柄作为其 <i>目标</i> 适配当前方法句柄。适配器的类型将与目标的类型相同，但最后一个尾随参数（通常是 {@code arrayType} 类型）将被替换为 {@code arrayLength} 个参数，这些参数的类型是 {@code arrayType} 的元素类型。
     * <p>
     * 如果数组类型与原始目标上的最后一个参数类型不同，则原始目标将被适配为直接接受数组类型，就像通过调用 {@link #asType asType} 一样。
     * <p>
     * 调用时，适配器会用一个类型为 {@code arrayType} 的新数组替换其尾随的 {@code arrayLength} 个参数，该数组的元素按顺序组成被替换的参数。
     * 最后调用目标。目标最终返回的内容将由适配器不变地返回。
     * <p>
     * （当 {@code arrayLength} 为零时，数组也可以是一个共享常量。）
     * <p>
     * （<em>注意：</em> {@code arrayType} 通常与原始目标的最后一个参数类型相同。
     * 它是一个显式参数，以与 {@code asSpreader} 保持对称，并允许目标使用简单的 {@code Object} 作为其最后一个参数类型。）
     * <p>
     * 若要创建一个不限制收集参数数量的收集适配器，请使用 {@link #asVarargsCollector asVarargsCollector}。
     * <p>
     * 以下是一些数组收集方法句柄的示例：
     * <blockquote><pre>{@code
MethodHandle deepToString = publicLookup()
  .findStatic(Arrays.class, "deepToString", methodType(String.class, Object[].class));
assertEquals("[won]",   (String) deepToString.invokeExact(new Object[]{"won"}));
MethodHandle ts1 = deepToString.asCollector(Object[].class, 1);
assertEquals(methodType(String.class, Object.class), ts1.type());
//assertEquals("[won]", (String) ts1.invokeExact(         new Object[]{"won"})); //FAIL
assertEquals("[[won]]", (String) ts1.invokeExact((Object) new Object[]{"won"}));
// arrayType 可以是 Object[] 的子类型
MethodHandle ts2 = deepToString.asCollector(String[].class, 2);
assertEquals(methodType(String.class, String.class, String.class), ts2.type());
assertEquals("[two, too]", (String) ts2.invokeExact("two", "too"));
MethodHandle ts0 = deepToString.asCollector(Object[].class, 0);
assertEquals("[]", (String) ts0.invokeExact());
// 收集器可以嵌套，Lisp 风格
MethodHandle ts22 = deepToString.asCollector(Object[].class, 3).asCollector(String[].class, 2);
assertEquals("[A, B, [C, D]]", ((String) ts22.invokeExact((Object)'A', (Object)"B", "C", "D")));
// arrayType 可以是任何原始数组类型
MethodHandle bytesToString = publicLookup()
  .findStatic(Arrays.class, "toString", methodType(String.class, byte[].class))
  .asCollector(byte[].class, 3);
assertEquals("[1, 2, 3]", (String) bytesToString.invokeExact((byte)1, (byte)2, (byte)3));
MethodHandle longsToString = publicLookup()
  .findStatic(Arrays.class, "toString", methodType(String.class, long[].class))
  .asCollector(long[].class, 1);
assertEquals("[123]", (String) longsToString.invokeExact((long)123));
     * }</pre></blockquote>
     * @param arrayType 通常是 {@code Object[]}，将用于收集参数的数组类型
     * @param arrayLength 要收集到新数组参数中的参数数量
     * @return 一个新的方法句柄，该方法句柄在调用原始方法句柄之前，将一些尾随参数收集到一个数组中
     * @throws NullPointerException 如果 {@code arrayType} 是 null 引用
     * @throws IllegalArgumentException 如果 {@code arrayType} 不是数组类型
     *         或 {@code arrayType} 不能分配给此方法句柄的尾随参数类型，
     *         或 {@code arrayLength} 不是合法的数组大小，
     *         或结果方法句柄的类型将有
     *         <a href="MethodHandle.html#maxarity">过多的参数</a>
     * @throws WrongMethodTypeException 如果隐含的 {@code asType} 调用失败
     * @see #asSpreader
     * @see #asVarargsCollector
     */
    public MethodHandle asCollector(Class<?> arrayType, int arrayLength) {
        asCollectorChecks(arrayType, arrayLength);
        int collectArgPos = type().parameterCount() - 1;
        BoundMethodHandle mh = rebind();
        MethodType resultType = type().asCollectorType(arrayType, arrayLength);
        MethodHandle newArray = MethodHandleImpl.varargsArray(arrayType, arrayLength);
        LambdaForm lform = mh.editor().collectArgumentArrayForm(1 + collectArgPos, newArray);
        if (lform != null) {
            return mh.copyWith(resultType, lform);
        }
        lform = mh.editor().collectArgumentsForm(1 + collectArgPos, newArray.type().basicType());
        return mh.copyWithExtendL(resultType, lform, newArray);
    }


    /**
     * 检查 {@code asCollector} 是否可以使用给定的参数有效调用。
     * 如果最后一个参数与 arrayType 不完全匹配，则返回 false。
     */
    /*non-public*/ boolean asCollectorChecks(Class<?> arrayType, int arrayLength) {
        spreadArrayChecks(arrayType, arrayLength);
        int nargs = type().parameterCount();
        if (nargs != 0) {
            Class<?> lastParam = type().parameterType(nargs-1);
            if (lastParam == arrayType)  return true;
            if (lastParam.isAssignableFrom(arrayType))  return false;
        }
        throw newIllegalArgumentException("array type not assignable to trailing argument", this, arrayType);
    }

    /**
     * 创建一个 <em>可变参数</em> 适配器，该适配器能够接受任意数量的尾随位置参数，并将它们收集到一个数组参数中。
     * <p>
     * 适配器的类型和行为将与目标的类型和行为相同，但某些 {@code invoke} 和 {@code asType} 请求可能会导致
     * 尾随位置参数被收集到目标的尾随参数中。
     * 另外，适配器的最后一个参数类型将是 {@code arrayType}，即使目标有不同的最后一个参数类型。
     * <p>
     * 如果方法句柄已经是可变参数的，并且其尾随参数类型与 {@code arrayType} 相同，则此转换可能会返回 {@code this}。
     * <p>
     * 当使用 {@link #invokeExact invokeExact} 调用时，适配器会以不改变参数的方式调用目标。
     * （<em>注意：</em> 这种行为与 {@linkplain #asCollector 固定参数收集器} 不同，
     * 因为它接受一个长度不确定的整个数组，而不是固定数量的参数。）
     * <p>
     * 当使用普通的、不精确的 {@link #invoke invoke} 调用时，如果调用者类型与适配器相同，适配器将以 {@code invokeExact} 的方式调用目标。
     * （这是当类型匹配时 {@code invoke} 的正常行为。）
     * <p>
     * 否则，如果调用者和适配器的参数数量相同，并且调用者的尾随参数类型是引用类型，且与适配器的尾随参数类型相同或可赋值，
     * 则参数和返回值将成对转换，就像在固定参数数量的方法句柄上使用 {@link #asType asType} 一样。
     * <p>
     * 否则，参数数量不同，或者适配器的尾随参数类型不能从相应的调用者类型赋值。
     * 在这种情况下，适配器将从原始尾随参数位置开始的所有尾随参数替换为一个新数组，该数组的类型为 {@code arrayType}，
     * 其元素按顺序包含被替换的参数。
     * <p>
     * 调用者类型必须提供足够的参数，并且类型正确，以满足目标在尾随数组参数之前的位置参数要求。
     * 因此，调用者至少必须提供 {@code N-1} 个参数，其中 {@code N} 是目标的参数数量。
     * 另外，必须存在从传入参数到目标参数的转换。
     * 与其他使用普通 {@code invoke} 的情况一样，如果这些基本要求未得到满足，可能会抛出 {@code WrongMethodTypeException}。
     * <p>
     * 在所有情况下，目标最终返回的内容将由适配器不变地返回。
     * <p>
     * 在最后一种情况下，就好像目标方法句柄被临时适配为一个 {@linkplain #asCollector 固定参数收集器}，
     * 以满足调用者类型所需的参数数量。
     * （与 {@code asCollector} 一样，如果数组长度为零，可能会使用共享常量而不是新数组。
     * 如果对 {@code asCollector} 的隐式调用会抛出 {@code IllegalArgumentException} 或 {@code WrongMethodTypeException}，
     * 则对可变参数适配器的调用必须抛出 {@code WrongMethodTypeException}。）
     * <p>
     * {@link #asType asType} 的行为也针对可变参数适配器进行了专门化，以保持不变性，即
     * 普通的、不精确的 {@code invoke} 始终等同于对目标类型进行调整的 {@code asType} 调用，然后是 {@code invokeExact}。
     * 因此，可变参数适配器响应 {@code asType} 请求的方式是构建一个固定参数收集器，
     * 仅当适配器和请求类型在参数数量或尾随参数类型上不同时才这样做。
     * 构建的固定参数收集器的类型（如果需要）将通过成对转换进一步调整为请求的类型，就像再次应用 {@code asType} 一样。
     * <p>
     * 当通过执行 {@code ldc} 指令获取一个 {@code CONSTANT_MethodHandle} 常量的方法句柄时，如果目标方法被标记为可变参数方法
     * （使用修饰符位 {@code 0x0080}），则该方法句柄将接受多个参数数量，就像该方法句柄常量是通过调用 {@code asVarargsCollector} 创建的一样。
     * <p>
     * 为了创建一个收集预定数量参数的收集适配器，并且其类型反映了这个预定数量，应使用 {@link #asCollector asCollector}。
     * <p>
     * 没有方法句柄转换会产生新的具有可变参数的方法句柄，除非它们被记录为这样做。
     * 因此，除了 {@code asVarargsCollector} 之外，
     * {@code MethodHandle} 和 {@code MethodHandles} 中的所有方法都将返回具有固定参数数量的方法句柄，
     * 除非它们被指定返回其原始操作数（例如，方法句柄的自身类型上的 {@code asType}）。
     * <p>
     * 对已经具有可变参数的方法句柄调用 {@code asVarargsCollector} 将产生一个具有相同类型和行为的方法句柄。
     * 它可能会（也可能不会）返回原始的可变参数方法句柄。
     * <p>
     * 以下是一个创建可变参数列表的方法句柄的示例：
     * <blockquote><pre>{@code
MethodHandle deepToString = publicLookup()
  .findStatic(Arrays.class, "deepToString", methodType(String.class, Object[].class));
MethodHandle ts1 = deepToString.asVarargsCollector(Object[].class);
assertEquals("[won]",   (String) ts1.invokeExact(    new Object[]{"won"}));
assertEquals("[won]",   (String) ts1.invoke(         new Object[]{"won"}));
assertEquals("[won]",   (String) ts1.invoke(                      "won" ));
assertEquals("[[won]]", (String) ts1.invoke((Object) new Object[]{"won"}));
// findStatic of Arrays.asList(...) 产生一个可变参数方法句柄：
MethodHandle asList = publicLookup()
  .findStatic(Arrays.class, "asList", methodType(List.class, Object[].class));
assertEquals(methodType(List.class, Object[].class), asList.type());
assert(asList.isVarargsCollector());
assertEquals("[]", asList.invoke().toString());
assertEquals("[1]", asList.invoke(1).toString());
assertEquals("[two, too]", asList.invoke("two", "too").toString());
String[] argv = { "three", "thee", "tee" };
assertEquals("[three, thee, tee]", asList.invoke(argv).toString());
assertEquals("[three, thee, tee]", asList.invoke((Object[])argv).toString());
List ls = (List) asList.invoke((Object)argv);
assertEquals(1, ls.size());
assertEquals("[three, thee, tee]", Arrays.toString((Object[])ls.get(0)));
     * }</pre></blockquote>
     * <p style="font-size:smaller;">
     * <em>讨论：</em>
     * 这些规则被设计为 Java 可变参数方法规则的动态类型变体。
     * 在这两种情况下，调用者可以传递零个或多个位置参数，或者传递任意长度的预收集数组。用户应意识到
     * 最后一个参数的特殊角色，以及该最后一个参数的类型匹配效果，这决定了单个尾随参数是被解释为整个数组
     * 还是被收集到数组中的单个元素。
     * 请注意，尾随参数的动态类型对此决定没有影响，只有调用站点的符号类型描述符与方法句柄的类型描述符之间的比较有影响。
     *
     * @param arrayType 通常是 {@code Object[]}，用于收集参数的数组参数类型
     * @return 一个新的方法句柄，可以将任意数量的尾随参数收集到一个数组中，然后调用原始方法句柄
     * @throws NullPointerException 如果 {@code arrayType} 是空引用
     * @throws IllegalArgumentException 如果 {@code arrayType} 不是数组类型
     *         或 {@code arrayType} 不能赋值给此方法句柄的尾随参数类型
     * @see #asCollector
     * @see #isVarargsCollector
     * @see #asFixedArity
     */
    public MethodHandle asVarargsCollector(Class<?> arrayType) {
        arrayType.getClass(); // 显式 NPE
        boolean lastMatch = asCollectorChecks(arrayType, 0);
        if (isVarargsCollector() && lastMatch)
            return this;
        return MethodHandleImpl.makeVarargsCollector(this, arrayType);
    }


                /**
     * 确定此方法句柄是否支持 {@linkplain #asVarargsCollector 可变参数} 调用。
     * 这样的方法句柄来源于以下几种情况：
     * <ul>
     * <li>对 {@linkplain #asVarargsCollector asVarargsCollector} 的调用
     * <li>对 {@linkplain java.lang.invoke.MethodHandles.Lookup 查找方法} 的调用，该方法解析为可变参数的 Java 方法或构造函数
     * <li>一个 {@code ldc} 指令的 {@code CONSTANT_MethodHandle}，该指令解析为可变参数的 Java 方法或构造函数
     * </ul>
     * @return 如果此方法句柄接受多于一个参数数量的普通、不精确的 {@code invoke} 调用，则返回 true
     * @see #asVarargsCollector
     * @see #asFixedArity
     */
    public boolean isVarargsCollector() {
        return false;
    }

    /**
     * 创建一个与当前方法句柄等效的 <em>固定参数数量</em> 的方法句柄。
     * <p>
     * 如果当前方法句柄不是 {@linkplain #asVarargsCollector 可变参数} 的，
     * 则返回当前方法句柄。
     * 即使当前方法句柄不能作为 {@code asVarargsCollector} 的有效输入，也是如此。
     * <p>
     * 否则，生成的固定参数数量的方法句柄与当前方法句柄具有相同的类型和行为，
     * 除了 {@link #isVarargsCollector isVarargsCollector} 将返回 false。
     * 固定参数数量的方法句柄可能是（也可能不是） {@code asVarargsCollector} 的先前参数。
     * <p>
     * 以下是一个创建列表的可变参数方法句柄的示例：
     * <blockquote><pre>{@code
MethodHandle asListVar = publicLookup()
  .findStatic(Arrays.class, "asList", methodType(List.class, Object[].class))
  .asVarargsCollector(Object[].class);
MethodHandle asListFix = asListVar.asFixedArity();
assertEquals("[1]", asListVar.invoke(1).toString());
Exception caught = null;
try { asListFix.invoke((Object)1); }
catch (Exception ex) { caught = ex; }
assert(caught instanceof ClassCastException);
assertEquals("[two, too]", asListVar.invoke("two", "too").toString());
try { asListFix.invoke("two", "too"); }
catch (Exception ex) { caught = ex; }
assert(caught instanceof WrongMethodTypeException);
Object[] argv = { "three", "thee", "tee" };
assertEquals("[three, thee, tee]", asListVar.invoke(argv).toString());
assertEquals("[three, thee, tee]", asListFix.invoke(argv).toString());
assertEquals(1, ((List) asListVar.invoke((Object)argv)).size());
assertEquals("[three, thee, tee]", asListFix.invoke((Object)argv).toString());
     * }</pre></blockquote>
     *
     * @return 一个仅接受固定数量参数的新方法句柄
     * @see #asVarargsCollector
     * @see #isVarargsCollector
     */
    public MethodHandle asFixedArity() {
        assert(!isVarargsCollector());
        return this;
    }

    /**
     * 将值 {@code x} 绑定到方法句柄的第一个参数，而不调用它。
     * 新的方法句柄以当前方法句柄作为其 <i>目标</i>，通过将它绑定到给定的参数来适应。
     * 绑定句柄的类型将与目标的类型相同，只是省略了一个前导的引用参数。
     * <p>
     * 当调用时，绑定句柄将给定的值 {@code x} 作为新的前导参数插入到目标中。其他参数也保持不变传递。
     * 目标最终返回的内容由绑定句柄不变地返回。
     * <p>
     * 引用 {@code x} 必须可以转换为目标的第一个参数类型。
     * <p>
     * (<em>注意：</em> 由于方法句柄是不可变的，目标方法句柄保留其原始类型和行为。)
     * @param x  要绑定到目标第一个参数的值
     * @return 一个新的方法句柄，该句柄在调用原始方法句柄之前，将给定值添加到传入的参数列表的前面
     * @throws IllegalArgumentException 如果目标没有一个前导的引用类型参数
     * @throws ClassCastException 如果 {@code x} 不能转换为目标的前导参数类型
     * @see MethodHandles#insertArguments
     */
    public MethodHandle bindTo(Object x) {
        x = type.leadingReferenceParameter().cast(x);  // 如果需要，抛出 CCE
        return bindArgumentL(0, x);
    }

    /**
     * 返回方法句柄的字符串表示形式，
     * 以字符串 {@code "MethodHandle"} 开头，
     * 以方法句柄类型的字符串表示形式结尾。
     * 换句话说，此方法返回一个字符串，其值等于以下表达式的值：
     * <blockquote><pre>{@code
     * "MethodHandle" + type().toString()
     * }</pre></blockquote>
     * <p>
     * (<em>注意：</em> 未来版本的此 API 可能会向字符串表示形式中添加更多信息。
     * 因此，当前的语法不应被应用程序解析。)
     *
     * @return 方法句柄的字符串表示形式
     */
    @Override
    public String toString() {
        if (DEBUG_METHOD_HANDLE_NAMES)  return "MethodHandle"+debugString();
        return standardString();
    }
    String standardString() {
        return "MethodHandle"+type;
    }
    /** 返回一个包含多行描述方法句柄结构的字符串。
     *  该字符串适合在 IDE 调试器中显示。
     */
    String debugString() {
        return type+" : "+internalForm()+internalProperties();
    }

    //// 实现方法。
    //// 子类可以覆盖这些默认实现。
    //// 所有这些方法都假设参数已经验证。

    // 其他转换：convert, explicitCast, permute, drop, filter, fold, GWT, catch

    BoundMethodHandle bindArgumentL(int pos, Object value) {
        return rebind().bindArgumentL(pos, value);
    }

    /*non-public*/
    MethodHandle setVarargs(MemberName member) throws IllegalAccessException {
        if (!member.isVarargs())  return this;
        Class<?> arrayType = type().lastParameterType();
        if (arrayType.isArray()) {
            return MethodHandleImpl.makeVarargsCollector(this, arrayType);
        }
        throw member.makeAccessException("无法创建可变参数", null);
    }


                /*非公开*/
    MethodHandle viewAsType(MethodType newType, boolean strict) {
        // 没有实际的转换，只是对同一方法的新视图。
        // 注意，此操作不得生成 DirectMethodHandle，
        // 因为重新类型的 DMH，像任何转换的 MH 一样，
        // 不能被分解成 MethodHandleInfo。
        assert viewAsTypeChecks(newType, strict);
        BoundMethodHandle mh = rebind();
        assert(!((MethodHandle)mh instanceof DirectMethodHandle));
        return mh.copyWith(newType, mh.form);
    }

    /*非公开*/
    boolean viewAsTypeChecks(MethodType newType, boolean strict) {
        if (strict) {
            assert(type().isViewableAs(newType, true))
                : Arrays.asList(this, newType);
        } else {
            assert(type().basicType().isViewableAs(newType.basicType(), true))
                : Arrays.asList(this, newType);
        }
        return true;
    }

    // 解码

    /*非公开*/
    LambdaForm internalForm() {
        return form;
    }

    /*非公开*/
    MemberName internalMemberName() {
        return null;  // DMH 返回 DMH.member
    }

    /*非公开*/
    Class<?> internalCallerClass() {
        return null;  // 为 @CallerSensitive 方法绑定的调用者返回调用者
    }

    /*非公开*/
    MethodHandleImpl.Intrinsic intrinsicName() {
        // 大多数 MH 没有特殊的内在含义
        return MethodHandleImpl.Intrinsic.NONE;
    }

    /*非公开*/
    MethodHandle withInternalMemberName(MemberName member, boolean isInvokeSpecial) {
        if (member != null) {
            return MethodHandleImpl.makeWrappedMember(this, member, isInvokeSpecial);
        } else if (internalMemberName() == null) {
            // 所需的 internaMemberName 为 null，且此 MH（像大多数一样）没有一个。
            return this;
        } else {
            // 以下情况很少见。通过将 MH 包装在 BMH 中来隐藏 internalMemberName。
            MethodHandle result = rebind();
            assert (result.internalMemberName() == null);
            return result;
        }
    }

    /*非公开*/
    boolean isInvokeSpecial() {
        return false;  // DMH.Special 返回 true
    }

    /*非公开*/
    Object internalValues() {
        return null;
    }

    /*非公开*/
    Object internalProperties() {
        // 覆盖为跟随 this.form 的内容，如 "\n& FOO=bar"
        return "";
    }

    //// 方法句柄实现方法。
    //// 子类可以覆盖这些默认实现。
    //// 所有这些方法都假设参数已经验证。

    /*非公开*/
    abstract MethodHandle copyWith(MethodType mt, LambdaForm lf);

    /** 要求此方法句柄为 BMH，否则用“包装”BMH 替换它。
     *  许多转换仅针对 BMH 实现。
     *  @return 行为等效的 BMH
     */
    abstract BoundMethodHandle rebind();

    /**
     * 用新的 lambda 形式替换此方法句柄的旧 lambda 形式。
     * 新的形式必须与旧的形式功能等效。
     * 线程可能会无限期地继续运行旧形式，
     * 但新形式可能会被优先用于新的执行。
     * 谨慎使用。
     */
    /*非公开*/
    void updateForm(LambdaForm newForm) {
        assert(newForm.customized == null || newForm.customized == this);
        if (form == newForm)  return;
        newForm.prepare();  // 如 MethodHandle.<init>
        UNSAFE.putObject(this, FORM_OFFSET, newForm);
        UNSAFE.fullFence();
    }

    /** 为这个特定的 MethodHandle 定制一个 LambdaForm */
    /*非公开*/
    void customize() {
        if (form.customized == null) {
            LambdaForm newForm = form.customize(this);
            updateForm(newForm);
        } else {
            assert(form.customized == this);
        }
    }

    private static final long FORM_OFFSET;
    static {
        try {
            FORM_OFFSET = UNSAFE.objectFieldOffset(MethodHandle.class.getDeclaredField("form"));
        } catch (ReflectiveOperationException ex) {
            throw newInternalError(ex);
        }
    }
}
