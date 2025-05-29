
/*
 * Copyright (c) 2008, 2016, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.reflect.*;
import java.util.BitSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import sun.invoke.util.ValueConversions;
import sun.invoke.util.VerifyAccess;
import sun.invoke.util.Wrapper;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import sun.reflect.misc.ReflectUtil;
import sun.security.util.SecurityConstants;
import java.lang.invoke.LambdaForm.BasicType;
import static java.lang.invoke.LambdaForm.BasicType.*;
import static java.lang.invoke.MethodHandleStatics.*;
import static java.lang.invoke.MethodHandleImpl.Intrinsic;
import static java.lang.invoke.MethodHandleNatives.Constants.*;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 本类仅包含静态方法，这些方法操作或返回方法句柄。它们分为几类：
 * <ul>
 * <li>查找方法，帮助创建方法和字段的方法句柄。
 * <li>组合方法，将现有的方法句柄组合或转换为新的方法句柄。
 * <li>其他工厂方法，创建模拟常见JVM操作或控制流模式的方法句柄。
 * </ul>
 * <p>
 * @author John Rose, JSR 292 EG
 * @since 1.7
 */
public class MethodHandles {

    private MethodHandles() { }  // 不允许实例化

    private static final MemberName.Factory IMPL_NAMES = MemberName.getFactory();
    static { MethodHandleImpl.initStatics(); }
    // 参见下面的 IMPL_LOOKUP。

    //// 从普通方法创建方法句柄。

    /**
     * 返回一个 {@link Lookup 查找对象}，该对象具有模拟调用者支持的所有字节码行为的全部功能。
     * 这些功能包括对调用者的 <a href="MethodHandles.Lookup.html#privacc">私有访问</a>。
     * 查找对象上的工厂方法可以创建
     * <a href="MethodHandleInfo.html#directmh">直接方法句柄</a>
     * 以访问调用者通过字节码可访问的任何成员，
     * 包括受保护和私有的字段和方法。
     * 此查找对象是一个 <em>功能</em>，可以委托给受信任的代理。
     * 不要将其存储在不受信任的代码可以访问的地方。
     * <p>
     * 此方法是调用者敏感的，这意味着它可能会对不同的调用者返回不同的值。
     * <p>
     * 对于任何给定的调用者类 {@code C}，此调用返回的查找对象具有与JVM提供的查找对象相同的权限
     * 在相同的调用者类 {@code C} 中执行的
     * <a href="package-summary.html#indyinsn">invokedynamic 指令</a> 的引导方法。
     * @return 一个查找对象，用于调用此方法的类，具有私有访问权限
     */
    @CallerSensitive
    public static Lookup lookup() {
        return new Lookup(Reflection.getCallerClass());
    }

    /**
     * 返回一个信任度最低的 {@link Lookup 查找对象}。
     * 它只能用于创建公开可访问的字段和方法的方法句柄。
     * <p>
     * 作为纯粹的惯例，此查找对象的 {@linkplain Lookup#lookupClass 查找类}
     * 将是 {@link java.lang.Object}。
     *
     * <p style="font-size:smaller;">
     * <em>讨论：</em>
     * 可以使用形式为
     * {@link Lookup#in publicLookup().in(C.class)} 的表达式将查找类更改为任何其他类 {@code C}。
     * 由于所有类对公共名称具有相同的访问权限，
     * 这样的更改不会授予任何新的访问权限。
     * 公共查找对象始终受
     * <a href="MethodHandles.Lookup.html#secmgr">安全管理器检查</a> 的约束。
     * 此外，它不能访问
     * <a href="MethodHandles.Lookup.html#callsens">调用者敏感的方法</a>。
     * @return 一个信任度最低的查找对象
     */
    public static Lookup publicLookup() {
        return Lookup.PUBLIC_LOOKUP;
    }

    /**
     * 对一个
     * <a href="MethodHandleInfo.html#directmh">直接方法句柄</a>
     * 进行未检查的“破解”。
     * 结果就像用户获得了足够权限的查找对象，
     * 调用了 {@link java.lang.invoke.MethodHandles.Lookup#revealDirect Lookup.revealDirect}
     * 以获取目标的方法句柄的符号引用，然后调用了
     * {@link java.lang.invoke.MethodHandleInfo#reflectAs MethodHandleInfo.reflectAs}
     * 以将符号引用解析为成员。
     * <p>
     * 如果存在安全管理器，将调用其 {@code checkPermission} 方法
     * 并传递一个 {@code ReflectPermission("suppressAccessChecks")} 权限。
     * @param <T> 结果所需的类型，可以是 {@link Member} 或其子类型
     * @param target 要破解为符号引用组件的直接方法句柄
     * @param expected 表示所需结果类型 {@code T} 的类对象
     * @return 对方法、构造函数或字段对象的引用
     * @exception SecurityException 如果调用者没有权限调用 {@code setAccessible}
     * @exception NullPointerException 如果任一参数为 {@code null}
     * @exception IllegalArgumentException 如果目标不是直接方法句柄
     * @exception ClassCastException 如果成员不是预期的类型
     * @since 1.8
     */
    public static <T extends Member> T
    reflectAs(Class<T> expected, MethodHandle target) {
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null)  smgr.checkPermission(ACCESS_PERMISSION);
        Lookup lookup = Lookup.IMPL_LOOKUP;  // 使用权限最高的查找
        return lookup.revealDirect(target).reflectAs(expected, lookup);
    }
    // 从 AccessibleObject 复制，用于 Method.setAccessible 等：
    static final private java.security.Permission ACCESS_PERMISSION =
        new ReflectPermission("suppressAccessChecks");

                /**
     * 一个 <em>查找对象</em> 是一个用于创建方法句柄的工厂，
     * 当创建需要访问检查时。
     * 方法句柄在被调用时不会执行
     * 访问检查，而是在创建时执行。
     * 因此，方法句柄的访问
     * 限制必须在方法句柄创建时强制执行。
     * 执行这些限制的调用者类
     * 被称为 {@linkplain #lookupClass 查找类}。
     * <p>
     * 需要创建方法句柄的查找类将调用
     * {@link MethodHandles#lookup MethodHandles.lookup} 为自己创建一个工厂。
     * 当 {@code Lookup} 工厂对象被创建时，查找类的身份被
     * 确定，并安全地存储在 {@code Lookup} 对象中。
     * 查找类（或其委托）然后可以使用
     * {@code Lookup} 对象上的工厂方法来创建访问检查成员的方法句柄。
     * 这包括查找类允许访问的所有方法、构造函数和字段，
     * 即使是私有的。
     *
     * <h1><a name="lookups"></a>查找工厂方法</h1>
     * {@code Lookup} 对象上的工厂方法对应于方法、构造函数和字段的所有主要
     * 使用场景。
     * 通过每个工厂方法创建的每个方法句柄都是特定 <em>字节码行为</em> 的功能
     * 等效物。
     * （字节码行为在 Java 虚拟机规范的第 5.4.3.5 节中描述。）
     * 以下是这些工厂方法与
     * 结果方法句柄的行为之间的对应关系的总结：
     * <table border=1 cellpadding=5 summary="lookup method behaviors">
     * <tr>
     *     <th><a name="equiv"></a>查找表达式</th>
     *     <th>成员</th>
     *     <th>字节码行为</th>
     * </tr>
     * <tr>
     *     <td>{@link java.lang.invoke.MethodHandles.Lookup#findGetter lookup.findGetter(C.class,"f",FT.class)}</td>
     *     <td>{@code FT f;}</td><td>{@code (T) this.f;}</td>
     * </tr>
     * <tr>
     *     <td>{@link java.lang.invoke.MethodHandles.Lookup#findStaticGetter lookup.findStaticGetter(C.class,"f",FT.class)}</td>
     *     <td>{@code static}<br>{@code FT f;}</td><td>{@code (T) C.f;}</td>
     * </tr>
     * <tr>
     *     <td>{@link java.lang.invoke.MethodHandles.Lookup#findSetter lookup.findSetter(C.class,"f",FT.class)}</td>
     *     <td>{@code FT f;}</td><td>{@code this.f = x;}</td>
     * </tr>
     * <tr>
     *     <td>{@link java.lang.invoke.MethodHandles.Lookup#findStaticSetter lookup.findStaticSetter(C.class,"f",FT.class)}</td>
     *     <td>{@code static}<br>{@code FT f;}</td><td>{@code C.f = arg;}</td>
     * </tr>
     * <tr>
     *     <td>{@link java.lang.invoke.MethodHandles.Lookup#findVirtual lookup.findVirtual(C.class,"m",MT)}</td>
     *     <td>{@code T m(A*);}</td><td>{@code (T) this.m(arg*);}</td>
     * </tr>
     * <tr>
     *     <td>{@link java.lang.invoke.MethodHandles.Lookup#findStatic lookup.findStatic(C.class,"m",MT)}</td>
     *     <td>{@code static}<br>{@code T m(A*);}</td><td>{@code (T) C.m(arg*);}</td>
     * </tr>
     * <tr>
     *     <td>{@link java.lang.invoke.MethodHandles.Lookup#findSpecial lookup.findSpecial(C.class,"m",MT,this.class)}</td>
     *     <td>{@code T m(A*);}</td><td>{@code (T) super.m(arg*);}</td>
     * </tr>
     * <tr>
     *     <td>{@link java.lang.invoke.MethodHandles.Lookup#findConstructor lookup.findConstructor(C.class,MT)}</td>
     *     <td>{@code C(A*);}</td><td>{@code new C(arg*);}</td>
     * </tr>
     * <tr>
     *     <td>{@link java.lang.invoke.MethodHandles.Lookup#unreflectGetter lookup.unreflectGetter(aField)}</td>
     *     <td>({@code static})?<br>{@code FT f;}</td><td>{@code (FT) aField.get(thisOrNull);}</td>
     * </tr>
     * <tr>
     *     <td>{@link java.lang.invoke.MethodHandles.Lookup#unreflectSetter lookup.unreflectSetter(aField)}</td>
     *     <td>({@code static})?<br>{@code FT f;}</td><td>{@code aField.set(thisOrNull, arg);}</td>
     * </tr>
     * <tr>
     *     <td>{@link java.lang.invoke.MethodHandles.Lookup#unreflect lookup.unreflect(aMethod)}</td>
     *     <td>({@code static})?<br>{@code T m(A*);}</td><td>{@code (T) aMethod.invoke(thisOrNull, arg*);}</td>
     * </tr>
     * <tr>
     *     <td>{@link java.lang.invoke.MethodHandles.Lookup#unreflectConstructor lookup.unreflectConstructor(aConstructor)}</td>
     *     <td>{@code C(A*);}</td><td>{@code (C) aConstructor.newInstance(arg*);}</td>
     * </tr>
     * <tr>
     *     <td>{@link java.lang.invoke.MethodHandles.Lookup#unreflect lookup.unreflect(aMethod)}</td>
     *     <td>({@code static})?<br>{@code T m(A*);}</td><td>{@code (T) aMethod.invoke(thisOrNull, arg*);}</td>
     * </tr>
     * </table>
     *
     * 在这里，类型 {@code C} 是正在搜索成员的类或接口，
     * 在查找方法中记录为名为 {@code refc} 的参数。
     * 方法类型 {@code MT} 由返回类型 {@code T}
     * 和参数类型序列 {@code A*} 组成。
     * 构造函数也有一个参数类型序列 {@code A*} 并
     * 被认为返回类型为 {@code C} 的新创建的对象。
     * 两者 {@code MT} 和字段类型 {@code FT} 都记录为名为 {@code type} 的参数。
     * 形式参数 {@code this} 代表类型为 {@code C} 的自引用；
     * 如果存在，它总是方法句柄调用的第一个参数。
     * （在某些 {@code protected} 成员的情况下，{@code this} 可能
     * 被限制为查找类的类型；见下文。）
     * 名称 {@code arg} 代表所有其他方法句柄参数。
     * 在 Core Reflection API 的代码示例中，名称 {@code thisOrNull}
     * 代表如果访问的方法或字段是静态的，则为 null 引用，
     * 否则为 {@code this}。
     * 名称 {@code aMethod}、{@code aField} 和 {@code aConstructor} 代表
     * 与给定成员对应的反射对象。
     * <p>
     * 在给定成员是可变参数（即方法或构造函数）的情况下，
     * 返回的方法句柄也将是 {@linkplain MethodHandle#asVarargsCollector 可变参数}。
     * 在所有其他情况下，返回的方法句柄将是固定参数。
     * <p style="font-size:smaller;">
     * <em>讨论：</em>
     * 查找的方法句柄与底层
     * 类成员和字节码行为之间的等效性
     * 可能会以几种方式失效：
     * <ul style="font-size:smaller;">
     * <li>如果 {@code C} 不能从查找类的加载器中符号访问，
     * 查找仍然可以成功，即使没有等效的
     * Java 表达式或字节码常量。
     * <li>同样，如果 {@code T} 或 {@code MT}
     * 不能从查找类的加载器中符号访问，
     * 查找仍然可以成功。
     * 例如，查找 {@code MethodHandle.invokeExact} 和
     * {@code MethodHandle.invoke} 总是会成功，无论请求的类型如何。
     * <li>如果安装了安全经理，它可以禁止基于各种理由的查找
     * （<a href="MethodHandles.Lookup.html#secmgr">见下文</a>）。
     * 相比之下，{@code ldc} 指令在 {@code CONSTANT_MethodHandle}
     * 常量上不受安全经理检查的约束。
     * <li>如果查找的方法有
     * <a href="MethodHandle.html#maxarity">非常大的参数数量</a>，
     * 方法句柄的创建可能会失败，因为方法句柄
     * 类型的参数过多。
     * </ul>
     *
     * <h1><a name="access"></a>访问检查</h1>
     * 访问检查在 {@code Lookup} 的工厂方法中应用，
     * 当创建方法句柄时。
     * 这是与 Core Reflection API 的关键区别，因为
     * {@link java.lang.reflect.Method#invoke java.lang.reflect.Method.invoke}
     * 在每次调用时都会对每个调用者进行访问检查。
     * <p>
     * 所有访问检查都从一个 {@code Lookup} 对象开始，该对象
     * 将其记录的查找类与所有请求创建方法句柄的请求进行比较。
     * 单个 {@code Lookup} 对象可以用于创建任意数量
     * 的访问检查方法句柄，所有这些都针对单个
     * 查找类进行检查。
     * <p>
     * {@code Lookup} 对象可以与其他可信代码共享，
     * 例如元对象协议。
     * 共享的 {@code Lookup} 对象委托了
     * 创建查找类私有成员的方法句柄的能力。
     * 即使特权代码使用 {@code Lookup} 对象，
     * 访问检查也仅限于
     * 原始查找类的权限。
     * <p>
     * 查找可能会失败，因为
     * 包含类对查找类不可访问，或者
     * 所需的类成员不存在，或者
     * 所需的类成员对查找类不可访问，或者
     * 查找对象没有足够的权限来访问成员。
     * 在这些情况下，将从尝试的查找中抛出 {@code ReflectiveOperationException}。确切的类将是以下之一：
     * <ul>
     * <li>NoSuchMethodException &mdash; 如果请求的方法不存在
     * <li>NoSuchFieldException &mdash; 如果请求的字段不存在
     * <li>IllegalAccessException &mdash; 如果成员存在但访问检查失败
     * </ul>
     * <p>
     * 一般来说，查找方法句柄的条件
     * 对于方法 {@code M} 不比查找类编译、验证和解析对 {@code M} 的调用的条件更严格。
     * 在 JVM 会引发诸如 {@code NoSuchMethodError} 之类的异常的情况下，
     * 方法句柄查找通常会引发相应的
     * 检查异常，如 {@code NoSuchMethodException}。
     * 而且，调用从查找结果的方法句柄的效果
     * <a href="MethodHandles.Lookup.html#equiv">完全等同于</a>
     * 执行编译、验证和解析的对 {@code M} 的调用。
     * 字段和构造函数也是如此。
     * <p style="font-size:smaller;">
     * <em>讨论：</em>
     * 访问检查仅适用于命名和反射的方法、
     * 构造函数和字段。
     * 其他方法句柄创建方法，如
     * {@link MethodHandle#asType MethodHandle.asType}，
     * 不需要任何访问检查，并且独立于任何 {@code Lookup} 对象使用。
     * <p>
     * 如果所需的成员是 {@code protected}，则应用通常的 JVM 规则，
     * 包括查找类必须在
     * 与所需成员相同的包中，或者必须继承该成员的要求。
     * （参见 Java 虚拟机规范，第 4.9.2 节、第 5.4.3.5 节和第 6.4 节。）
     * 此外，如果所需的成员是非静态字段或方法
     * 在不同的包中，结果的方法句柄只能应用于
     * 查找类或其子类的对象。
     * 这个要求是通过将前导
     * {@code this} 参数的类型从 {@code C}
     * （这必然是查找类的超类）
     * 窄化到查找类本身来强制执行的。
     * <p>
     * JVM 对 {@code invokespecial} 指令施加了类似的要求，
     * 即接收者参数必须同时匹配解析的方法 <em>和</em>
     * 当前类。同样，这个要求是通过将
     * 结果方法句柄的前导参数类型窄化来强制执行的。
     * （参见 Java 虚拟机规范，第 4.10.1.9 节。）
     * <p>
     * JVM 将构造函数和静态初始化块表示为具有特殊名称的内部方法
     * （{@code "<init>"} 和 {@code "<clinit>"}）。
     * 调用指令的内部语法允许它们引用这样的内部
     * 方法，好像它们是普通方法一样，但 JVM 字节码验证器会拒绝它们。
     * 对此类内部方法的查找将产生一个 {@code NoSuchMethodException}。
     * <p>
     * 在某些情况下，嵌套类之间的访问是通过 Java 编译器创建
     * 一个包装方法来访问同一顶级声明中的另一个类的私有方法来实现的。
     * 例如，嵌套类 {@code C.D}
     * 可以访问相关类中的私有成员，如
     * {@code C}、{@code C.D.E} 或 {@code C.B}，
     * 但 Java 编译器可能需要在
     * 这些相关类中生成包装方法。在这种情况下，一个在
     * {@code C.E} 上的 {@code Lookup} 对象将无法访问这些私有成员。
     * 解决此限制的一个方法是 {@link Lookup#in Lookup.in} 方法，
     * 它可以将 {@code C.E} 上的查找转换为这些其他
     * 类上的查找，而无需特别提升权限。
     * <p>
     * 给定查找对象允许的访问可能受到限制，
     * 根据其 {@link #lookupModes lookupModes} 集，
     * 仅限于查找类通常可以访问的成员的子集。
     * 例如，{@link MethodHandles#publicLookup publicLookup}
     * 方法生成一个查找对象，该对象仅允许访问
     * 公共类中的公共成员。
     * 调用敏感方法 {@link MethodHandles#lookup lookup}
     * 生成一个具有相对于
     * 其调用者类的全部能力的查找对象，以模拟所有支持的字节码行为。
     * 此外，{@link Lookup#in Lookup.in} 方法可能生成一个查找对象
     * 其访问模式比原始查找对象少。
     *
     * <p style="font-size:smaller;">
     * <a name="privacc"></a>
     * <em>私有访问讨论：</em>
     * 我们说一个查找具有 <em>私有访问</em>
     * 如果其 {@linkplain #lookupModes 查找模式}
     * 包括访问 {@code private} 成员的可能性。
     * 如相关方法中所述，
     * 只有具有私有访问权限的查找才具备以下能力：
     * <ul style="font-size:smaller;">
     * <li>访问查找类的私有字段、方法和构造函数
     * <li>创建调用 <a href="MethodHandles.Lookup.html#callsens">调用敏感</a> 方法的方法句柄，
     *     如 {@code Class.forName}
     * <li>创建 {@link Lookup#findSpecial 模拟 invokespecial} 指令的方法句柄
     * <li>避免 <a href="MethodHandles.Lookup.html#secmgr">包访问检查</a>
     *     对查找类可访问的类
     * <li>创建具有对同一包成员的其他类具有私有访问权限的
     *     {@link Lookup#in 委托查找对象}
     * </ul>
     * <p style="font-size:smaller;">
     * 这些权限是由于查找对象
     * 具有私有访问权限可以安全地追溯到一个起始类，
     * 其 <a href="MethodHandles.Lookup.html#equiv">字节码行为</a> 和 Java 语言访问权限
     * 可以通过方法句柄可靠地确定和模拟。
     *
     * <h1><a name="secmgr"></a>安全经理交互</h1>
     * 虽然字节码指令只能引用
     * 相关类加载器中的类，但此 API 可以在任何
     * 类中搜索方法，只要可以引用其 {@code Class} 对象。
     * 这样的跨加载器引用在
     * Core Reflection API 中也是可能的，但在
     * 如 {@code invokestatic} 或 {@code getfield} 之类的字节码指令中是不可能的。
     * 有一个 {@linkplain java.lang.SecurityManager 安全经理 API}
     * 允许应用程序检查此类跨加载器引用。
     * 这些检查适用于 {@code MethodHandles.Lookup} API
     * 和 Core Reflection API
     * （如在 {@link java.lang.Class Class} 上找到的）。
     * <p>
     * 如果存在安全经理，成员查找将受到
     * 额外的检查。
     * 会向安全经理发出一到三个调用。
     * 这些调用中的任何一个都可以通过抛出
     * {@link java.lang.SecurityException SecurityException} 拒绝访问。
     * 定义 {@code smgr} 为安全经理，
     * {@code lookc} 为当前查找对象的查找类，
     * {@code refc} 为正在搜索成员的包含类，
     * {@code defc} 为成员实际定义的类。
     * 如果当前查找对象没有
     * <a href="MethodHandles.Lookup.html#privacc">私有访问</a>，
     * 则定义 {@code lookc} 为 <em>不存在</em>。
     * 调用根据以下规则进行：
     * <ul>
     * <li><b>步骤 1：</b>
     *     如果 {@code lookc} 不存在，或者其类加载器不是
     *     {@code refc} 的类加载器或其祖先，
     *     则调用 {@link SecurityManager#checkPackageAccess
     *     smgr.checkPackageAccess(refcPkg)}，
     *     其中 {@code refcPkg} 是 {@code refc} 的包。
     * <li><b>步骤 2：</b>
     *     如果检索到的成员不是公共的，并且
     *     {@code lookc} 不存在，则调用
     *     {@link SecurityManager#checkPermission smgr.checkPermission}
     *     带有 {@code RuntimePermission("accessDeclaredMembers")}。
     * <li><b>步骤 3：</b>
     *     如果检索到的成员不是公共的，
     *     并且 {@code lookc} 不存在，
     *     并且 {@code defc} 和 {@code refc} 不同，
     *     则调用 {@link SecurityManager#checkPackageAccess
     *     smgr.checkPackageAccess(defcPkg)}，
     *     其中 {@code defcPkg} 是 {@code defc} 的包。
     * </ul>
     * 安全检查在其他访问检查通过后执行。
     * 因此，上述规则假设成员是公共的，
     * 或者是从具有
     * 访问成员权利的查找类访问的。
     *
     * <h1><a name="callsens"></a>调用敏感方法</h1>
     * 一些 Java 方法具有称为调用敏感性的特殊属性。
     * <em>调用敏感</em> 方法的行为可能因
     * 其直接调用者的身份而异。
     * <p>
     * 如果请求调用敏感方法的方法句柄，
     * 一般规则为 <a href="MethodHandles.Lookup.html#equiv">字节码行为</a> 适用，
     * 但它们以特殊方式考虑查找类。
     * 结果的方法句柄表现得好像它
     * 是从查找类中的指令调用的一样，
     * 以便调用敏感方法检测到查找类。
     * （相比之下，方法句柄的调用者被忽略。）
     * 因此，在调用敏感方法的情况下，
     * 不同的查找类可能会产生
     * 行为不同的方法句柄。
     * <p>
     * 在查找对象是
     * {@link MethodHandles#publicLookup() publicLookup()}，
     * 或其他没有
     * <a href="MethodHandles.Lookup.html#privacc">私有访问</a> 的查找对象的情况下，
     * 查找类被忽略。
     * 在这种情况下，不能创建调用敏感方法的方法句柄，
     * 访问被禁止，查找以
     * {@code IllegalAccessException} 失败。
     * <p style="font-size:smaller;">
     * <em>讨论：</em>
     * 例如，调用敏感方法
     * {@link java.lang.Class#forName(String) Class.forName(x)}
     * 可以根据调用它的类的类加载器返回不同的类或抛出不同的异常。
     * 公共查找 {@code Class.forName} 将失败，因为
     * 没有合理的方法来确定其字节码行为。
     * <p style="font-size:smaller;">
     * 如果应用程序缓存方法句柄以广泛共享，
     * 它应该使用 {@code publicLookup()} 来创建它们。
     * 如果有 {@code Class.forName} 的查找，它将失败，
     * 应用程序必须在这种情况下采取适当的行动。
     * 可能稍后在引导方法的调用期间进行的查找，
     * 可以结合调用者的特定身份，
     * 使方法变得可访问。
     * <p style="font-size:smaller;">
     * 函数 {@code MethodHandles.lookup} 是调用敏感的，
     * 以便为查找提供安全的基础。
     * JSR 292 API 中的几乎所有其他方法都依赖于查找
     * 对象来检查访问请求。
     */
    public static final
    class Lookup {
        /** 代表查找正在为其执行的类。 */
        private final Class<?> lookupClass;


                    /** 允许查找的成员类型（PUBLIC等）。 */
        private final int allowedModes;

        /** 表示 {@code public} 访问的单比特掩码，
         *  可能会成为 {@link #lookupModes lookupModes} 的结果的一部分。
         *  值为 {@code 0x01}，恰好与 {@code public} 
         *  {@linkplain java.lang.reflect.Modifier#PUBLIC 修饰符位} 的值相同。
         */
        public static final int PUBLIC = Modifier.PUBLIC;

        /** 表示 {@code private} 访问的单比特掩码，
         *  可能会成为 {@link #lookupModes lookupModes} 的结果的一部分。
         *  值为 {@code 0x02}，恰好与 {@code private} 
         *  {@linkplain java.lang.reflect.Modifier#PRIVATE 修饰符位} 的值相同。
         */
        public static final int PRIVATE = Modifier.PRIVATE;

        /** 表示 {@code protected} 访问的单比特掩码，
         *  可能会成为 {@link #lookupModes lookupModes} 的结果的一部分。
         *  值为 {@code 0x04}，恰好与 {@code protected} 
         *  {@linkplain java.lang.reflect.Modifier#PROTECTED 修饰符位} 的值相同。
         */
        public static final int PROTECTED = Modifier.PROTECTED;

        /** 表示 {@code package} 访问（默认访问）的单比特掩码，
         *  可能会成为 {@link #lookupModes lookupModes} 的结果的一部分。
         *  值为 {@code 0x08}，没有特别对应于任何特定的
         *  {@linkplain java.lang.reflect.Modifier 修饰符位}。
         */
        public static final int PACKAGE = Modifier.STATIC;

        private static final int ALL_MODES = (PUBLIC | PRIVATE | PROTECTED | PACKAGE);
        private static final int TRUSTED   = -1;

        private static int fixmods(int mods) {
            mods &= (ALL_MODES - PACKAGE);
            return (mods != 0) ? mods : PACKAGE;
        }

        /** 告诉哪个类正在执行查找。这是对可见性和访问权限进行检查的类。
         *  <p>
         *  该类隐含了一个最大级别的访问权限，
         *  但权限可能通过位掩码 {@link #lookupModes lookupModes} 进一步限制，
         *  该位掩码控制是否可以访问非公共成员。
         *  @return 代表该查找对象查找成员的查找类
         */
        public Class<?> lookupClass() {
            return lookupClass;
        }

        // 仅用于调用 MethodHandleImpl。
        private Class<?> lookupClassOrNull() {
            return (allowedModes == TRUSTED) ? null : lookupClass;
        }

        /** 告诉此查找对象可以生成的成员的访问保护类。
         *  结果是一个位掩码，包含以下位：
         *  {@linkplain #PUBLIC PUBLIC (0x01)}，
         *  {@linkplain #PRIVATE PRIVATE (0x02)}，
         *  {@linkplain #PROTECTED PROTECTED (0x04)}，
         *  和 {@linkplain #PACKAGE PACKAGE (0x08)}。
         *  <p>
         *  新创建的查找对象
         *  在 {@linkplain java.lang.invoke.MethodHandles#lookup() 调用者的类} 上
         *  有所有可能的位设置，因为调用者类可以访问其所有成员。
         *  从先前的查找对象 {@linkplain java.lang.invoke.MethodHandles.Lookup#in 创建的新查找类}
         *  的查找对象可能有一些模式位设置为零。
         *  这样做的目的是通过新的查找对象限制访问，
         *  以便它只能访问原始查找对象和新查找类可以访问的名称。
         *  @return 限制此查找对象执行的访问类型的查找模式
         */
        public int lookupModes() {
            return allowedModes & ALL_MODES;
        }

        /** 将当前类（查找类）体现为用于创建方法句柄的查找类。
         * 必须由本包中的方法调用，
         * 该方法又由不在本包中的方法调用。
         */
        Lookup(Class<?> lookupClass) {
            this(lookupClass, ALL_MODES);
            // 确保我们没有意外地选择了一个特权类：
            checkUnprivilegedlookupClass(lookupClass, ALL_MODES);
        }

        private Lookup(Class<?> lookupClass, int allowedModes) {
            this.lookupClass = lookupClass;
            this.allowedModes = allowedModes;
        }

        /**
         * 在指定的新查找类上创建查找。
         * 结果对象将报告指定的类为其自己的 {@link #lookupClass lookupClass}。
         * <p>
         * 然而，结果的 {@code Lookup} 对象保证
         * 不会比原始对象有更多的访问能力。
         * 特别是，访问能力可能会以下列方式丢失：<ul>
         * <li>如果新的查找类与旧的查找类不同，
         * 则受保护的成员将不会通过继承访问。
         * （受保护的成员可能继续因为包共享而访问。）
         * <li>如果新的查找类与旧的查找类在不同的包中，
         * 则受保护的成员和默认（包）成员将不会访问。
         * <li>如果新的查找类不在与旧的查找类相同的包成员中，
         * 则私有成员将不会访问。
         * <li>如果新的查找类对旧的查找类不可访问，
         * 则没有成员，即使是公共成员，也不会访问。
         * （在所有其他情况下，公共成员将继续访问。）
         * </ul>
         *
         * @param requestedLookupClass 新查找对象所需的查找类
         * @return 报告所需查找类的查找对象
         * @throws NullPointerException 如果参数为 null
         */
        public Lookup in(Class<?> requestedLookupClass) {
            requestedLookupClass.getClass();  // null 检查
            if (allowedModes == TRUSTED)  // IMPL_LOOKUP 可以进行任何查找
                return new Lookup(requestedLookupClass, ALL_MODES);
            if (requestedLookupClass == this.lookupClass)
                return this;  // 保持相同的能力
            int newModes = (allowedModes & (ALL_MODES & ~PROTECTED));
            if ((newModes & PACKAGE) != 0
                && !VerifyAccess.isSamePackage(this.lookupClass, requestedLookupClass)) {
                newModes &= ~(PACKAGE|PRIVATE);
            }
            // 允许在没有特殊权限的情况下创建巢穴查找：
            if ((newModes & PRIVATE) != 0
                && !VerifyAccess.isSamePackageMember(this.lookupClass, requestedLookupClass)) {
                newModes &= ~PRIVATE;
            }
            if ((newModes & PUBLIC) != 0
                && !VerifyAccess.isClassAccessible(requestedLookupClass, this.lookupClass, allowedModes)) {
                // 请求的类对查找类不可访问。
                // 没有权限。
                newModes = 0;
            }
            checkUnprivilegedlookupClass(requestedLookupClass, newModes);
            return new Lookup(requestedLookupClass, newModes);
        }


                    // 确保外部类首先被初始化。
        static { IMPL_NAMES.getClass(); }

        /** 可以最小信任度使用的查找版本。
         *  仅可用于创建对公开可访问成员的方法句柄。
         */
        static final Lookup PUBLIC_LOOKUP = new Lookup(Object.class, PUBLIC);

        /** 可信任的包私有查找版本。 */
        static final Lookup IMPL_LOOKUP = new Lookup(Object.class, TRUSTED);

        private static void checkUnprivilegedlookupClass(Class<?> lookupClass, int allowedModes) {
            String name = lookupClass.getName();
            if (name.startsWith("java.lang.invoke."))
                throw newIllegalArgumentException("非法的 lookupClass: "+lookupClass);

            // 对于调用者敏感的 MethodHandles.lookup()
            // 禁止查找更受限制的包
            if (allowedModes == ALL_MODES && lookupClass.getClassLoader() == null) {
                if (name.startsWith("java.") ||
                        (name.startsWith("sun.")
                                && !name.startsWith("sun.invoke.")
                                && !name.equals("sun.reflect.ReflectionFactory"))) {
                    throw newIllegalArgumentException("非法的 lookupClass: " + lookupClass);
                }
            }
        }

        /**
         * 显示将要进行查找的类的名称。
         * （名称是由 {@link java.lang.Class#getName() Class.getName} 报告的。）
         * 如果对此查找的访问有权限限制，
         * 则通过在类名后添加一个斜杠和一个关键字的后缀来表示。
         * 关键字代表允许的最强访问权限，选择如下：
         * <ul>
         * <li>如果没有访问权限，后缀为 "/noaccess"。
         * <li>如果只有公开访问权限，后缀为 "/public"。
         * <li>如果只有公开和包级访问权限，后缀为 "/package"。
         * <li>如果只有公开、包级和私有访问权限，后缀为 "/private"。
         * </ul>
         * 如果以上情况都不适用，则表示允许完全访问（公开、包级、私有和受保护）。
         * 在这种情况下，不添加后缀。
         * 这仅适用于最初从
         * {@link java.lang.invoke.MethodHandles#lookup MethodHandles.lookup} 获取的对象。
         * 通过 {@link java.lang.invoke.MethodHandles.Lookup#in Lookup.in} 创建的对象
         * 总是有访问限制，并将显示后缀。
         * <p>
         * （可能看起来受保护的访问权限比私有访问权限更强。从包级访问权限独立来看，
         * 受保护的访问权限是第一个失去的，因为它要求调用者和被调用者之间有直接的子类关系。）
         * @see #in
         */
        @Override
        public String toString() {
            String cname = lookupClass.getName();
            switch (allowedModes) {
            case 0:  // 没有权限
                return cname + "/noaccess";
            case PUBLIC:
                return cname + "/public";
            case PUBLIC|PACKAGE:
                return cname + "/package";
            case ALL_MODES & ~PROTECTED:
                return cname + "/private";
            case ALL_MODES:
                return cname;
            case TRUSTED:
                return "/trusted";  // 仅内部使用；不导出
            default:  // 不应该发生，但它是位字段...
                cname = cname + "/" + Integer.toHexString(allowedModes);
                assert(false) : cname;
                return cname;
            }
        }

        /**
         * 为静态方法生成方法句柄。
         * 方法句柄的类型将是方法的类型。
         * （由于静态方法不接收接收者，因此方法句柄类型中不会插入额外的接收者参数，
         * 与 {@link #findVirtual findVirtual} 或 {@link #findSpecial findSpecial} 不同。）
         * 方法及其所有参数类型必须对查找对象可访问。
         * <p>
         * 如果返回的方法句柄被调用，方法的类将
         * 被初始化，如果它尚未被初始化。
         * <p><b>示例：</b>
         * <blockquote><pre>{@code
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.*;
...
MethodHandle MH_asList = publicLookup().findStatic(Arrays.class,
  "asList", methodType(List.class, Object[].class));
assertEquals("[x, y]", MH_asList.invoke("x", "y").toString());
         * }</pre></blockquote>
         * @param refc 访问方法的类
         * @param name 方法的名称
         * @param type 方法的类型
         * @return 所需的方法句柄
         * @throws NoSuchMethodException 如果方法不存在
         * @throws IllegalAccessException 如果访问检查失败，
         *                                或方法不是 {@code static}，
         *                                或方法的可变参数修饰符位
         *                                被设置且 {@code asVarargsCollector} 失败
         * @exception SecurityException 如果存在安全经理且它
         *                              <a href="MethodHandles.Lookup.html#secmgr">拒绝访问</a>
         * @throws NullPointerException 如果任何参数为 null
         */
        public
        MethodHandle findStatic(Class<?> refc, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
            MemberName method = resolveOrFail(REF_invokeStatic, refc, name, type);
            return getDirectMethod(REF_invokeStatic, refc, method, findBoundCallerClass(method));
        }


                    /**
         * 生成一个虚拟方法的方法句柄。
         * 方法句柄的类型将是该方法的类型，
         * 但在接收者类型（通常是 {@code refc}）之前添加。
         * 方法及其所有参数类型必须对查找对象可访问。
         * <p>
         * 调用时，句柄将把第一个参数视为接收者
         * 并根据接收者的类型分派以确定要进入的方法实现。
         * （分派操作与 {@code invokevirtual} 或 {@code invokeinterface} 指令执行的操作相同。）
         * <p>
         * 如果查找类具有完全权限访问成员，则第一个参数的类型为 {@code refc}。否则，
         * 成员必须为 {@code protected}，并且第一个参数的类型将限制为查找类。
         * <p>
         * 返回的方法句柄将具有
         * {@linkplain MethodHandle#asVarargsCollector 可变参数} 当且仅当
         * 方法的可变参数修饰符位（{@code 0x0080}）被设置。
         * <p>
         * 由于 {@code invokevirtual} 指令和由 {@code findVirtual} 生成的方法句柄之间的
         * 一般 <a href="MethodHandles.Lookup.html#equiv">等效性</a>，
         * 如果类为 {@code MethodHandle} 且名称字符串为
         * {@code invokeExact} 或 {@code invoke}，则生成的
         * 方法句柄等同于由
         * {@link java.lang.invoke.MethodHandles#exactInvoker MethodHandles.exactInvoker} 或
         * {@link java.lang.invoke.MethodHandles#invoker MethodHandles.invoker}
         * 生成的方法句柄，且具有相同的 {@code type} 参数。
         *
         * <b>示例：</b>
         * <blockquote><pre>{@code
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.*;
...
MethodHandle MH_concat = publicLookup().findVirtual(String.class,
  "concat", methodType(String.class, String.class));
MethodHandle MH_hashCode = publicLookup().findVirtual(Object.class,
  "hashCode", methodType(int.class));
MethodHandle MH_hashCode_String = publicLookup().findVirtual(String.class,
  "hashCode", methodType(int.class));
assertEquals("xy", (String) MH_concat.invokeExact("x", "y"));
assertEquals("xy".hashCode(), (int) MH_hashCode.invokeExact((Object)"xy"));
assertEquals("xy".hashCode(), (int) MH_hashCode_String.invokeExact("xy"));
// 接口方法：
MethodHandle MH_subSequence = publicLookup().findVirtual(CharSequence.class,
  "subSequence", methodType(CharSequence.class, int.class, int.class));
assertEquals("def", MH_subSequence.invoke("abcdefghi", 3, 6).toString());
// 构造函数“内部方法”必须以不同方式访问：
MethodType MT_newString = methodType(void.class); //()V for new String()
try { assertEquals("impossible", lookup()
        .findVirtual(String.class, "<init>", MT_newString));
 } catch (NoSuchMethodException ex) { } // OK
MethodHandle MH_newString = publicLookup()
  .findConstructor(String.class, MT_newString);
assertEquals("", (String) MH_newString.invokeExact());
         * }</pre></blockquote>
         *
         * @param refc 访问方法的类或接口
         * @param name 方法的名称
         * @param type 方法的类型，省略接收者参数
         * @return 所需的方法句柄
         * @throws NoSuchMethodException 如果方法不存在
         * @throws IllegalAccessException 如果访问检查失败，
         *                                或方法为 {@code static}
         *                                或方法的可变参数修饰符位
         *                                被设置且 {@code asVarargsCollector} 失败
         * @exception SecurityException 如果存在安全管理器且它
         *                              <a href="MethodHandles.Lookup.html#secmgr">拒绝访问</a>
         * @throws NullPointerException 如果任何参数为 null
         */
        public MethodHandle findVirtual(Class<?> refc, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
            if (refc == MethodHandle.class) {
                MethodHandle mh = findVirtualForMH(name, type);
                if (mh != null)  return mh;
            }
            byte refKind = (refc.isInterface() ? REF_invokeInterface : REF_invokeVirtual);
            MemberName method = resolveOrFail(refKind, refc, name, type);
            return getDirectMethod(refKind, refc, method, findBoundCallerClass(method));
        }
        private MethodHandle findVirtualForMH(String name, MethodType type) {
            // 这些名称需要特殊查找，因为有隐式的 MethodType 参数
            if ("invoke".equals(name))
                return invoker(type);
            if ("invokeExact".equals(name))
                return exactInvoker(type);
            assert(!MemberName.isMethodHandleInvokeName(name));
            return null;
        }

        /**
         * 生成一个方法句柄，用于创建并初始化一个对象，使用指定类型的构造函数。
         * 方法句柄的参数类型将是构造函数的参数类型，
         * 而返回类型将是构造函数的类的引用。
         * 构造函数及其所有参数类型必须对查找对象可访问。
         * <p>
         * 请求的类型必须具有 {@code void} 返回类型。
         * （这与 JVM 对构造函数类型描述符的处理一致。）
         * <p>
         * 返回的方法句柄将具有
         * {@linkplain MethodHandle#asVarargsCollector 可变参数} 当且仅当
         * 构造函数的可变参数修饰符位（{@code 0x0080}）被设置。
         * <p>
         * 如果调用返回的方法句柄，构造函数的类将
         * 被初始化，如果它尚未被初始化。
         * <p><b>示例：</b>
         * <blockquote><pre>{@code
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.*;
...
MethodHandle MH_newArrayList = publicLookup().findConstructor(
  ArrayList.class, methodType(void.class, Collection.class));
Collection orig = Arrays.asList("x", "y");
Collection copy = (ArrayList) MH_newArrayList.invokeExact(orig);
assert(orig != copy);
assertEquals(orig, copy);
// 可变参数构造函数：
MethodHandle MH_newProcessBuilder = publicLookup().findConstructor(
  ProcessBuilder.class, methodType(void.class, String[].class));
ProcessBuilder pb = (ProcessBuilder)
  MH_newProcessBuilder.invoke("x", "y", "z");
assertEquals("[x, y, z]", pb.command().toString());
         * }</pre></blockquote>
         * @param refc 访问方法的类或接口
         * @param type 方法的类型，省略接收者参数，且返回类型为 void
         * @return 所需的方法句柄
         * @throws NoSuchMethodException 如果构造函数不存在
         * @throws IllegalAccessException 如果访问检查失败
         *                                或方法的可变参数修饰符位
         *                                被设置且 {@code asVarargsCollector} 失败
         * @exception SecurityException 如果存在安全管理器且它
         *                              <a href="MethodHandles.Lookup.html#secmgr">拒绝访问</a>
         * @throws NullPointerException 如果任何参数为 null
         */
        public MethodHandle findConstructor(Class<?> refc, MethodType type) throws NoSuchMethodException, IllegalAccessException {
            if (refc.isArray()) {
                throw new NoSuchMethodException("no constructor for array class: " + refc.getName());
            }
            String name = "<init>";
            MemberName ctor = resolveOrFail(REF_newInvokeSpecial, refc, name, type);
            return getDirectConstructor(refc, ctor);
        }


                    /**
         * 为虚拟方法生成一个早期绑定的方法句柄。
         * 它将绕过接收者上覆盖方法的检查，
         * <a href="MethodHandles.Lookup.html#equiv">仿佛是从</a> 明确指定的 {@code specialCaller} 内的 {@code invokespecial}
         * 指令调用一样。
         * 方法句柄的类型将是方法的类型，但会在前面添加一个适当限制的接收者类型。
         * （接收者类型将是 {@code specialCaller} 或其子类型。）
         * 方法及其所有参数类型必须对查找对象可访问。
         * <p>
         * 在方法解析之前，
         * 如果明确指定的调用者类与查找类不相同，或者此查找对象没有
         * <a href="MethodHandles.Lookup.html#privacc">私有访问</a>
         * 权限，访问将失败。
         * <p>
         * 返回的方法句柄将具有
         * {@linkplain MethodHandle#asVarargsCollector 可变参数}，当且仅当
         * 方法的可变参数修饰符位 ({@code 0x0080}) 被设置。
         * <p style="font-size:smaller;">
         * <em>(注意: JVM 内部方法名为 {@code "<init>"} 的方法对这个 API 不可见，
         * 即使在特殊情况下 {@code invokespecial} 指令可以引用它们。
         * 使用 {@link #findConstructor findConstructor} 以安全的方式访问实例初始化方法。)</em>
         * <p><b>示例：</b>
         * <blockquote><pre>{@code
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.*;
...
static class Listie extends ArrayList {
  public String toString() { return "[wee Listie]"; }
  static Lookup lookup() { return MethodHandles.lookup(); }
}
...
// 无法通过 invokeSpecial 访问构造函数：
MethodHandle MH_newListie = Listie.lookup()
  .findConstructor(Listie.class, methodType(void.class));
Listie l = (Listie) MH_newListie.invokeExact();
try { assertEquals("impossible", Listie.lookup().findSpecial(
        Listie.class, "<init>", methodType(void.class), Listie.class));
 } catch (NoSuchMethodException ex) { } // OK
// 通过 invokeSpecial 访问超类和自身方法：
MethodHandle MH_super = Listie.lookup().findSpecial(
  ArrayList.class, "toString" , methodType(String.class), Listie.class);
MethodHandle MH_this = Listie.lookup().findSpecial(
  Listie.class, "toString" , methodType(String.class), Listie.class);
MethodHandle MH_duper = Listie.lookup().findSpecial(
  Object.class, "toString" , methodType(String.class), Listie.class);
assertEquals("[]", (String) MH_super.invokeExact(l));
assertEquals(""+l, (String) MH_this.invokeExact(l));
assertEquals("[]", (String) MH_duper.invokeExact(l)); // ArrayList 方法
try { assertEquals("inaccessible", Listie.lookup().findSpecial(
        String.class, "toString", methodType(String.class), Listie.class));
 } catch (IllegalAccessException ex) { } // OK
Listie subl = new Listie() { public String toString() { return "[subclass]"; } };
assertEquals(""+l, (String) MH_this.invokeExact(subl)); // Listie 方法
         * }</pre></blockquote>
         *
         * @param refc 访问方法的类或接口
         * @param name 方法的名称（不能是 "&lt;init&gt;"）
         * @param type 方法的类型，省略接收者参数
         * @param specialCaller 执行 {@code invokespecial} 的提议调用者类
         * @return 所需的方法句柄
         * @throws NoSuchMethodException 如果方法不存在
         * @throws IllegalAccessException 如果访问检查失败
         *                                或方法的可变参数修饰符位被设置且 {@code asVarargsCollector} 失败
         * @exception SecurityException 如果存在安全管理器且它
         *                              <a href="MethodHandles.Lookup.html#secmgr">拒绝访问</a>
         * @throws NullPointerException 如果任何参数为 null
         */
        public MethodHandle findSpecial(Class<?> refc, String name, MethodType type,
                                        Class<?> specialCaller) throws NoSuchMethodException, IllegalAccessException {
            checkSpecialCaller(specialCaller);
            Lookup specialLookup = this.in(specialCaller);
            MemberName method = specialLookup.resolveOrFail(REF_invokeSpecial, refc, name, type);
            return specialLookup.getDirectMethod(REF_invokeSpecial, refc, method, findBoundCallerClass(method));
        }

        /**
         * 生成一个方法句柄，提供对非静态字段的读取访问。
         * 方法句柄的类型将具有字段值类型的返回类型。
         * 方法句柄的单个参数将是包含该字段的实例。
         * 代表查找类立即执行访问检查。
         * @param refc 访问方法的类或接口
         * @param name 字段的名称
         * @param type 字段的类型
         * @return 可以从字段加载值的方法句柄
         * @throws NoSuchFieldException 如果字段不存在
         * @throws IllegalAccessException 如果访问检查失败，或字段为 {@code static}
         * @exception SecurityException 如果存在安全管理器且它
         *                              <a href="MethodHandles.Lookup.html#secmgr">拒绝访问</a>
         * @throws NullPointerException 如果任何参数为 null
         */
        public MethodHandle findGetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
            MemberName field = resolveOrFail(REF_getField, refc, name, type);
            return getDirectField(REF_getField, refc, field);
        }

        /**
         * 生成一个方法句柄，提供对非静态字段的写入访问。
         * 方法句柄的类型将具有 void 返回类型。
         * 方法句柄将接受两个参数，包含字段的实例和要存储的值。
         * 第二个参数将是字段值类型。
         * 代表查找类立即执行访问检查。
         * @param refc 访问方法的类或接口
         * @param name 字段的名称
         * @param type 字段的类型
         * @return 可以将值存储到字段中的方法句柄
         * @throws NoSuchFieldException 如果字段不存在
         * @throws IllegalAccessException 如果访问检查失败，或字段为 {@code static}
         * @exception SecurityException 如果存在安全管理器且它
         *                              <a href="MethodHandles.Lookup.html#secmgr">拒绝访问</a>
         * @throws NullPointerException 如果任何参数为 null
         */
        public MethodHandle findSetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
            MemberName field = resolveOrFail(REF_putField, refc, name, type);
            return getDirectField(REF_putField, refc, field);
        }


                    /**
         * 生成一个方法句柄，提供对静态字段的读取访问。
         * 方法句柄的类型将具有字段值类型的返回类型。
         * 方法句柄将不带任何参数。
         * 立即为查找类执行访问检查。
         * <p>
         * 如果调用返回的方法句柄，如果字段的类尚未初始化，将对其进行初始化。
         * @param refc 访问方法的类或接口
         * @param name 字段的名称
         * @param type 字段的类型
         * @return 可以从字段加载值的方法句柄
         * @throws NoSuchFieldException 如果字段不存在
         * @throws IllegalAccessException 如果访问检查失败，或者字段不是 {@code static}
         * @exception SecurityException 如果存在安全管理器并且它
         *                              <a href="MethodHandles.Lookup.html#secmgr">拒绝访问</a>
         * @throws NullPointerException 如果任何参数为 null
         */
        public MethodHandle findStaticGetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
            MemberName field = resolveOrFail(REF_getStatic, refc, name, type);
            return getDirectField(REF_getStatic, refc, field);
        }

        /**
         * 生成一个方法句柄，提供对静态字段的写入访问。
         * 方法句柄的类型将具有 void 返回类型。
         * 方法句柄将接受一个参数，即要存储的字段值类型。
         * 立即为查找类执行访问检查。
         * <p>
         * 如果调用返回的方法句柄，如果字段的类尚未初始化，将对其进行初始化。
         * @param refc 访问方法的类或接口
         * @param name 字段的名称
         * @param type 字段的类型
         * @return 可以将值存储到字段的方法句柄
         * @throws NoSuchFieldException 如果字段不存在
         * @throws IllegalAccessException 如果访问检查失败，或者字段不是 {@code static}
         * @exception SecurityException 如果存在安全管理器并且它
         *                              <a href="MethodHandles.Lookup.html#secmgr">拒绝访问</a>
         * @throws NullPointerException 如果任何参数为 null
         */
        public MethodHandle findStaticSetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
            MemberName field = resolveOrFail(REF_putStatic, refc, name, type);
            return getDirectField(REF_putStatic, refc, field);
        }

        /**
         * 生成一个绑定早期的方法句柄，用于非静态方法。
         * 接收者必须有一个超类型 {@code defc}，其中包含一个方法，该方法的名称和类型对查找类是可访问的。
         * 方法及其所有参数类型必须对查找对象是可访问的。
         * 方法句柄的类型将是方法的类型，不插入额外的接收者参数。
         * 给定的接收者将绑定到方法句柄中，因此每次调用方法句柄时，都会在给定的接收者上调用请求的方法。
         * <p>
         * 返回的方法句柄将具有
         * {@linkplain MethodHandle#asVarargsCollector 可变参数}，当且仅当
         * 方法的可变参数修饰符位 ({@code 0x0080}) 被设置
         * <em>并且</em> 尾随数组参数不是唯一参数。
         * （如果尾随数组参数是唯一参数，给定的接收者值将绑定到它。）
         * <p>
         * 这等效于以下代码：
         * <blockquote><pre>{@code
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.*;
...
MethodHandle mh0 = lookup().findVirtual(defc, name, type);
MethodHandle mh1 = mh0.bindTo(receiver);
MethodType mt1 = mh1.type();
if (mh0.isVarargsCollector())
  mh1 = mh1.asVarargsCollector(mt1.parameterType(mt1.parameterCount()-1));
return mh1;
         * }</pre></blockquote>
         * 其中 {@code defc} 是 {@code receiver.getClass()} 或该类的超类型，其中请求的方法对查找类是可访问的。
         * （注意 {@code bindTo} 不保留可变参数。）
         * @param receiver 访问方法的对象
         * @param name 方法的名称
         * @param type 方法的类型，省略接收者参数
         * @return 所需的方法句柄
         * @throws NoSuchMethodException 如果方法不存在
         * @throws IllegalAccessException 如果访问检查失败
         *                                或者方法的可变参数修饰符位被设置并且 {@code asVarargsCollector} 失败
         * @exception SecurityException 如果存在安全管理器并且它
         *                              <a href="MethodHandles.Lookup.html#secmgr">拒绝访问</a>
         * @throws NullPointerException 如果任何参数为 null
         * @see MethodHandle#bindTo
         * @see #findVirtual
         */
        public MethodHandle bind(Object receiver, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
            Class<? extends Object> refc = receiver.getClass(); // 可能会抛出 NPE
            MemberName method = resolveOrFail(REF_invokeSpecial, refc, name, type);
            MethodHandle mh = getDirectMethodNoRestrict(REF_invokeSpecial, refc, method, findBoundCallerClass(method));
            return mh.bindArgumentL(0, receiver).setVarargs(method);
        }

        /**
         * 如果查找类有权限，生成一个 <a href="MethodHandleInfo.html#directmh">直接方法句柄</a> 到 <i>m</i>。
         * 如果 <i>m</i> 是非静态的，接收者参数被视为初始参数。
         * 如果 <i>m</i> 是虚拟的，每次调用时都会尊重覆盖。
         * 与核心反射 API 不同，异常不会被包装。
         * 方法句柄的类型将是方法的类型，如果它是非静态的，则接收者类型将被前置。
         * 如果方法的 {@code accessible} 标志未设置，立即为查找类执行访问检查。
         * 如果 <i>m</i> 不是公共的，不要将生成的句柄与不可信方共享。
         * <p>
         * 返回的方法句柄将具有
         * {@linkplain MethodHandle#asVarargsCollector 可变参数}，当且仅当
         * 方法的可变参数修饰符位 ({@code 0x0080}) 被设置。
         * <p>
         * 如果 <i>m</i> 是静态的，并且
         * 如果调用返回的方法句柄，如果方法的类尚未初始化，将对其进行初始化。
         * @param m 反射的方法
         * @return 可以调用反射方法的方法句柄
         * @throws IllegalAccessException 如果访问检查失败
         *                                或者方法的可变参数修饰符位被设置并且 {@code asVarargsCollector} 失败
         * @throws NullPointerException 如果参数为 null
         */
        public MethodHandle unreflect(Method m) throws IllegalAccessException {
            if (m.getDeclaringClass() == MethodHandle.class) {
                MethodHandle mh = unreflectForMH(m);
                if (mh != null)  return mh;
            }
            MemberName method = new MemberName(m);
            byte refKind = method.getReferenceKind();
            if (refKind == REF_invokeSpecial)
                refKind = REF_invokeVirtual;
            assert(method.isMethod());
            Lookup lookup = m.isAccessible() ? IMPL_LOOKUP : this;
            return lookup.getDirectMethodNoSecurityManager(refKind, method.getDeclaringClass(), method, findBoundCallerClass(method));
        }
        private MethodHandle unreflectForMH(Method m) {
            // 这些名称需要特殊查找，因为它们会抛出 UnsupportedOperationException
            if (MemberName.isMethodHandleInvokeName(m.getName()))
                return MethodHandleImpl.fakeMethodHandleInvoke(new MemberName(m));
            return null;
        }

                    /**
         * 生成一个反射方法的方法句柄。
         * 它将绕过接收者上覆盖方法的检查，
         * <a href="MethodHandles.Lookup.html#equiv">就像从</a> 明确指定的 {@code specialCaller} 类内的 {@code invokespecial}
         * 指令调用一样。
         * 方法句柄的类型将是该方法的类型，
         * 前面附加了一个适当限制的接收者类型。
         * （接收者类型将是 {@code specialCaller} 或其子类型。）
         * 如果方法的 {@code accessible} 标志未设置，
         * 将立即代表查找类执行访问检查，
         * 就像 {@code invokespecial} 指令正在链接一样。
         * <p>
         * 在方法解析之前，
         * 如果显式指定的调用者类与
         * 查找类不相同，或者此查找对象没有
         * <a href="MethodHandles.Lookup.html#privacc">私有访问</a>
         * 权限，访问将失败。
         * <p>
         * 如果且仅当方法的可变参数修饰符位（{@code 0x0080}）被设置时，
         * 返回的方法句柄将具有
         * {@linkplain MethodHandle#asVarargsCollector 可变参数}。
         * @param m 反射方法
         * @param specialCaller 命名调用方法的类
         * @return 可以调用反射方法的方法句柄
         * @throws IllegalAccessException 如果访问检查失败
         *                                或方法的可变参数修饰符位被设置且 {@code asVarargsCollector} 失败
         * @throws NullPointerException 如果任何参数为 null
         */
        public MethodHandle unreflectSpecial(Method m, Class<?> specialCaller) throws IllegalAccessException {
            checkSpecialCaller(specialCaller);
            Lookup specialLookup = this.in(specialCaller);
            MemberName method = new MemberName(m, true);
            assert(method.isMethod());
            // 忽略 m.isAccessible: 这是一种新的访问方式
            return specialLookup.getDirectMethodNoSecurityManager(REF_invokeSpecial, method.getDeclaringClass(), method, findBoundCallerClass(method));
        }

        /**
         * 生成一个反射构造函数的方法句柄。
         * 方法句柄的类型将是构造函数的类型，
         * 将返回类型更改为声明类。
         * 方法句柄将执行一个 {@code newInstance} 操作，
         * 使用传递给方法句柄的参数创建构造函数类的新实例。
         * <p>
         * 如果构造函数的 {@code accessible} 标志未设置，
         * 将立即代表查找类执行访问检查。
         * <p>
         * 如果且仅当构造函数的可变参数修饰符位（{@code 0x0080}）被设置时，
         * 返回的方法句柄将具有
         * {@linkplain MethodHandle#asVarargsCollector 可变参数}。
         * <p>
         * 如果调用返回的方法句柄，构造函数的类将
         * 如果尚未初始化，则进行初始化。
         * @param c 反射构造函数
         * @return 可以调用反射构造函数的方法句柄
         * @throws IllegalAccessException 如果访问检查失败
         *                                或方法的可变参数修饰符位被设置且 {@code asVarargsCollector} 失败
         * @throws NullPointerException 如果参数为 null
         */
        public MethodHandle unreflectConstructor(Constructor<?> c) throws IllegalAccessException {
            MemberName ctor = new MemberName(c);
            assert(ctor.isConstructor());
            Lookup lookup = c.isAccessible() ? IMPL_LOOKUP : this;
            return lookup.getDirectConstructorNoSecurityManager(ctor.getDeclaringClass(), ctor);
        }

        /**
         * 生成一个方法句柄，提供对反射字段的读取访问。
         * 方法句柄的类型将具有字段值类型的返回类型。
         * 如果字段是静态的，方法句柄将不带参数。
         * 否则，其唯一参数将是包含字段的实例。
         * 如果字段的 {@code accessible} 标志未设置，
         * 将立即代表查找类执行访问检查。
         * <p>
         * 如果字段是静态的，并且
         * 如果调用返回的方法句柄，字段的类将
         * 如果尚未初始化，则进行初始化。
         * @param f 反射字段
         * @return 可以从反射字段加载值的方法句柄
         * @throws IllegalAccessException 如果访问检查失败
         * @throws NullPointerException 如果参数为 null
         */
        public MethodHandle unreflectGetter(Field f) throws IllegalAccessException {
            return unreflectField(f, false);
        }
        private MethodHandle unreflectField(Field f, boolean isSetter) throws IllegalAccessException {
            MemberName field = new MemberName(f, isSetter);
            assert(isSetter
                    ? MethodHandleNatives.refKindIsSetter(field.getReferenceKind())
                    : MethodHandleNatives.refKindIsGetter(field.getReferenceKind()));
            Lookup lookup = f.isAccessible() ? IMPL_LOOKUP : this;
            return lookup.getDirectFieldNoSecurityManager(field.getReferenceKind(), f.getDeclaringClass(), field);
        }

        /**
         * 生成一个方法句柄，提供对反射字段的写入访问。
         * 方法句柄的类型将具有 void 返回类型。
         * 如果字段是静态的，方法句柄将接受一个
         * 参数，即字段值类型，要存储的值。
         * 否则，两个参数将是包含字段的实例和要存储的值。
         * 如果字段的 {@code accessible} 标志未设置，
         * 将立即代表查找类执行访问检查。
         * <p>
         * 如果字段是静态的，并且
         * 如果调用返回的方法句柄，字段的类将
         * 如果尚未初始化，则进行初始化。
         * @param f 反射字段
         * @return 可以向反射字段存储值的方法句柄
         * @throws IllegalAccessException 如果访问检查失败
         * @throws NullPointerException 如果参数为 null
         */
        public MethodHandle unreflectSetter(Field f) throws IllegalAccessException {
            return unreflectField(f, true);
        }


                    /**
         * 解析由该查找对象或类似查找对象创建的<a href="MethodHandleInfo.html#directmh">直接方法句柄</a>。
         * 执行安全和访问检查，以确保此查找对象能够重现目标方法句柄。
         * 这意味着，如果目标是直接方法句柄，但由不相关的查找对象创建，则解析可能会失败。
         * 如果方法句柄是<a href="MethodHandles.Lookup.html#callsens">调用者敏感的</a>，并且是由不同类的查找对象创建的，则可能会发生这种情况。
         * @param target 要解析为符号引用组件的直接方法句柄
         * @return 一个符号引用，可以用于从该查找对象重建此方法句柄
         * @exception SecurityException 如果存在安全管理器并且它<a href="MethodHandles.Lookup.html#secmgr">拒绝访问</a>
         * @throws IllegalArgumentException 如果目标不是直接方法句柄或访问检查失败
         * @exception NullPointerException 如果目标为 {@code null}
         * @see MethodHandleInfo
         * @since 1.8
         */
        public MethodHandleInfo revealDirect(MethodHandle target) {
            MemberName member = target.internalMemberName();
            if (member == null || (!member.isResolved() && !member.isMethodHandleInvoke()))
                throw newIllegalArgumentException("not a direct method handle");
            Class<?> defc = member.getDeclaringClass();
            byte refKind = member.getReferenceKind();
            assert(MethodHandleNatives.refKindIsValid(refKind));
            if (refKind == REF_invokeSpecial && !target.isInvokeSpecial())
                // 去虚拟化方法调用通常形式上是虚拟的。
                // 为了避免为此常见情况创建额外的 MemberName 对象，
                // 我们使用 MH.isInvokeSpecial 编码这种额外的自由度。
                refKind = REF_invokeVirtual;
            if (refKind == REF_invokeVirtual && defc.isInterface())
                // 符号引用是通过接口解析的，但解析到 Object 方法（如 toString 等）。
                refKind = REF_invokeInterface;
            // 在解析之前检查安全管理器权限和成员访问。
            try {
                checkAccess(refKind, defc, member);
                checkSecurityManager(defc, member);
            } catch (IllegalAccessException ex) {
                throw new IllegalArgumentException(ex);
            }
            if (allowedModes != TRUSTED && member.isCallerSensitive()) {
                Class<?> callerClass = target.internalCallerClass();
                if (!hasPrivateAccess() || callerClass != lookupClass())
                    throw new IllegalArgumentException("method handle is caller sensitive: "+callerClass);
            }
            // 生成结果的句柄。
            return new InfoFromMemberName(this, member, refKind);
        }

        /// 辅助方法，全部为包私有。

        MemberName resolveOrFail(byte refKind, Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
            checkSymbolicClass(refc);  // 在尝试解析之前执行此操作
            name.getClass();  // NPE
            type.getClass();  // NPE
            return IMPL_NAMES.resolveOrFail(refKind, new MemberName(refc, name, type, refKind), lookupClassOrNull(),
                                            NoSuchFieldException.class);
        }

        MemberName resolveOrFail(byte refKind, Class<?> refc, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
            checkSymbolicClass(refc);  // 在尝试解析之前执行此操作
            name.getClass();  // NPE
            type.getClass();  // NPE
            checkMethodName(refKind, name);  // 对名称进行 NPE 检查
            return IMPL_NAMES.resolveOrFail(refKind, new MemberName(refc, name, type, refKind), lookupClassOrNull(),
                                            NoSuchMethodException.class);
        }

        MemberName resolveOrFail(byte refKind, MemberName member) throws ReflectiveOperationException {
            checkSymbolicClass(member.getDeclaringClass());  // 在尝试解析之前执行此操作
            member.getName().getClass();  // NPE
            member.getType().getClass();  // NPE
            return IMPL_NAMES.resolveOrFail(refKind, member, lookupClassOrNull(),
                                            ReflectiveOperationException.class);
        }

        void checkSymbolicClass(Class<?> refc) throws IllegalAccessException {
            refc.getClass();  // NPE
            Class<?> caller = lookupClassOrNull();
            if (caller != null && !VerifyAccess.isClassAccessible(refc, caller, allowedModes))
                throw new MemberName(refc).makeAccessException("符号引用类不是公共的", this);
        }

        /** 检查名称是否以非法的前导 "&lt;" 字符开头。 */
        void checkMethodName(byte refKind, String name) throws NoSuchMethodException {
            if (name.startsWith("<") && refKind != REF_newInvokeSpecial)
                throw new NoSuchMethodException("非法的方法名称: "+name);
        }


        /**
         * 如果 m 是调用者敏感的方法，找到我的可信调用者类。
         * 如果此查找对象具有私有访问权限，则调用者类是 lookupClass。
         * 否则，如果 m 是调用者敏感的，抛出 IllegalAccessException。
         */
        Class<?> findBoundCallerClass(MemberName m) throws IllegalAccessException {
            Class<?> callerClass = null;
            if (MethodHandleNatives.isCallerSensitive(m)) {
                // 只有具有私有访问权限的查找对象才允许解析调用者敏感的方法
                if (hasPrivateAccess()) {
                    callerClass = lookupClass;
                } else {
                    throw new IllegalAccessException("尝试使用受限查找对象查找调用者敏感的方法");
                }
            }
            return callerClass;
        }


                    private boolean hasPrivateAccess() {
            return (allowedModes & PRIVATE) != 0;
        }

        /**
         * 执行必要的 <a href="MethodHandles.Lookup.html#secmgr">访问检查</a>。
         * 确定一个可信的调用者类，以与 refc（符号引用类）进行比较。
         * 如果此查找对象具有私有访问权限，则调用者类是 lookupClass。
         */
        void checkSecurityManager(Class<?> refc, MemberName m) {
            SecurityManager smgr = System.getSecurityManager();
            if (smgr == null)  return;
            if (allowedModes == TRUSTED)  return;

            // 第一步：
            boolean fullPowerLookup = hasPrivateAccess();
            if (!fullPowerLookup ||
                !VerifyAccess.classLoaderIsAncestor(lookupClass, refc)) {
                ReflectUtil.checkPackageAccess(refc);
            }

            // 第二步：
            if (m.isPublic()) return;
            if (!fullPowerLookup) {
                smgr.checkPermission(SecurityConstants.CHECK_MEMBER_ACCESS_PERMISSION);
            }

            // 第三步：
            Class<?> defc = m.getDeclaringClass();
            if (!fullPowerLookup && defc != refc) {
                ReflectUtil.checkPackageAccess(defc);
            }
        }

        void checkMethod(byte refKind, Class<?> refc, MemberName m) throws IllegalAccessException {
            boolean wantStatic = (refKind == REF_invokeStatic);
            String message;
            if (m.isConstructor())
                message = "expected a method, not a constructor";
            else if (!m.isMethod())
                message = "expected a method";
            else if (wantStatic != m.isStatic())
                message = wantStatic ? "expected a static method" : "expected a non-static method";
            else
                { checkAccess(refKind, refc, m); return; }
            throw m.makeAccessException(message, this);
        }

        void checkField(byte refKind, Class<?> refc, MemberName m) throws IllegalAccessException {
            boolean wantStatic = !MethodHandleNatives.refKindHasReceiver(refKind);
            String message;
            if (wantStatic != m.isStatic())
                message = wantStatic ? "expected a static field" : "expected a non-static field";
            else
                { checkAccess(refKind, refc, m); return; }
            throw m.makeAccessException(message, this);
        }

        /** 检查符号引用类及其成员的 public/protected/private 位。 */
        void checkAccess(byte refKind, Class<?> refc, MemberName m) throws IllegalAccessException {
            assert(m.referenceKindIsConsistentWith(refKind) &&
                   MethodHandleNatives.refKindIsValid(refKind) &&
                   (MethodHandleNatives.refKindIsField(refKind) == m.isField()));
            int allowedModes = this.allowedModes;
            if (allowedModes == TRUSTED)  return;
            int mods = m.getModifiers();
            if (Modifier.isProtected(mods) &&
                    refKind == REF_invokeVirtual &&
                    m.getDeclaringClass() == Object.class &&
                    m.getName().equals("clone") &&
                    refc.isArray()) {
                // JVM 也执行此操作。
                // (参见 ClassVerifier::verify_invoke_instructions
                // 和 LinkResolver::check_method_accessability。)
                // 由于 JVM 不允许数组类型有单独的方法，
                // 因此没有单独的 int[].clone 方法。
                // 所有数组都继承 Object.clone。
                // 但在访问检查逻辑中，我们使 Object.clone
                // （通常是受保护的）看起来是公共的。
                // 后来，当创建 DirectMethodHandle 时，
                // 其第一个参数将被限制为请求的数组类型。
                // 注意：返回类型不会调整，因为
                // 这不是字节码的行为。
                mods ^= Modifier.PROTECTED | Modifier.PUBLIC;
            }
            if (Modifier.isProtected(mods) && refKind == REF_newInvokeSpecial) {
                // 不能在不同包中“new”一个受保护的构造函数
                mods ^= Modifier.PROTECTED;
            }
            if (Modifier.isFinal(mods) &&
                    MethodHandleNatives.refKindIsSetter(refKind))
                throw m.makeAccessException("unexpected set of a final field", this);
            if (Modifier.isPublic(mods) && Modifier.isPublic(refc.getModifiers()) && allowedModes != 0)
                return;  // 常见情况
            int requestedModes = fixmods(mods);  // 调整 0 => PACKAGE
            if ((requestedModes & allowedModes) != 0) {
                if (VerifyAccess.isMemberAccessible(refc, m.getDeclaringClass(),
                                                    mods, lookupClass(), allowedModes))
                    return;
            } else {
                // 受保护的成员也可以像包私有成员一样检查。
                if ((requestedModes & PROTECTED) != 0 && (allowedModes & PACKAGE) != 0
                        && VerifyAccess.isSamePackage(m.getDeclaringClass(), lookupClass()))
                    return;
            }
            throw m.makeAccessException(accessFailedMessage(refc, m), this);
        }

        String accessFailedMessage(Class<?> refc, MemberName m) {
            Class<?> defc = m.getDeclaringClass();
            int mods = m.getModifiers();
            // 首先检查类：
            boolean classOK = (Modifier.isPublic(defc.getModifiers()) &&
                               (defc == refc ||
                                Modifier.isPublic(refc.getModifiers())));
            if (!classOK && (allowedModes & PACKAGE) != 0) {
                classOK = (VerifyAccess.isClassAccessible(defc, lookupClass(), ALL_MODES) &&
                           (defc == refc ||
                            VerifyAccess.isClassAccessible(refc, lookupClass(), ALL_MODES)));
            }
            if (!classOK)
                return "class is not public";
            if (Modifier.isPublic(mods))
                return "access to public member failed";  // (怎么会？)
            if (Modifier.isPrivate(mods))
                return "member is private";
            if (Modifier.isProtected(mods))
                return "member is protected";
            return "member is private to package";
        }


                    private static final boolean ALLOW_NESTMATE_ACCESS = false;

        private void checkSpecialCaller(Class<?> specialCaller) throws IllegalAccessException {
            int allowedModes = this.allowedModes;
            if (allowedModes == TRUSTED)  return;
            if (!hasPrivateAccess()
                || (specialCaller != lookupClass()
                    && !(ALLOW_NESTMATE_ACCESS &&
                         VerifyAccess.isSamePackageMember(specialCaller, lookupClass()))))
                throw new MemberName(specialCaller).
                    makeAccessException("no private access for invokespecial", this);
        }

        private boolean restrictProtectedReceiver(MemberName method) {
            // 访问类只有权利使用受保护的成员
            // 仅限于自身或子类。强制执行此限制，来自 JVMS 5.4.4 等。
            if (!method.isProtected() || method.isStatic()
                || allowedModes == TRUSTED
                || method.getDeclaringClass() == lookupClass()
                || VerifyAccess.isSamePackage(method.getDeclaringClass(), lookupClass())
                || (ALLOW_NESTMATE_ACCESS &&
                    VerifyAccess.isSamePackageMember(method.getDeclaringClass(), lookupClass())))
                return false;
            return true;
        }
        private MethodHandle restrictReceiver(MemberName method, DirectMethodHandle mh, Class<?> caller) throws IllegalAccessException {
            assert(!method.isStatic());
            // mh 的接收者类型太宽；缩小到调用者
            if (!method.getDeclaringClass().isAssignableFrom(caller)) {
                throw method.makeAccessException("调用者类必须是方法的子类", caller);
            }
            MethodType rawType = mh.type();
            if (rawType.parameterType(0) == caller)  return mh;
            MethodType narrowType = rawType.changeParameterType(0, caller);
            assert(!mh.isVarargsCollector());  // viewAsType 将会丢失 varargs 属性
            assert(mh.viewAsTypeChecks(narrowType, true));
            return mh.copyWith(narrowType, mh.form);
        }

        /** 检查访问权限并获取请求的方法。 */
        private MethodHandle getDirectMethod(byte refKind, Class<?> refc, MemberName method, Class<?> callerClass) throws IllegalAccessException {
            final boolean doRestrict    = true;
            final boolean checkSecurity = true;
            return getDirectMethodCommon(refKind, refc, method, checkSecurity, doRestrict, callerClass);
        }
        /** 检查访问权限并获取请求的方法，省略接收者缩小规则。 */
        private MethodHandle getDirectMethodNoRestrict(byte refKind, Class<?> refc, MemberName method, Class<?> callerClass) throws IllegalAccessException {
            final boolean doRestrict    = false;
            final boolean checkSecurity = true;
            return getDirectMethodCommon(refKind, refc, method, checkSecurity, doRestrict, callerClass);
        }
        /** 检查访问权限并获取请求的方法，省略安全管理器检查。 */
        private MethodHandle getDirectMethodNoSecurityManager(byte refKind, Class<?> refc, MemberName method, Class<?> callerClass) throws IllegalAccessException {
            final boolean doRestrict    = true;
            final boolean checkSecurity = false;  // 对于反射或链接 CONSTANT_MH 常量不需要
            return getDirectMethodCommon(refKind, refc, method, checkSecurity, doRestrict, callerClass);
        }
        /** 所有方法的通用代码；除非从直接上方调用，否则不要直接调用。 */
        private MethodHandle getDirectMethodCommon(byte refKind, Class<?> refc, MemberName method,
                                                   boolean checkSecurity,
                                                   boolean doRestrict, Class<?> callerClass) throws IllegalAccessException {
            checkMethod(refKind, refc, method);
            // 选择性地检查安全管理器；对于 unreflect* 调用不需要。
            if (checkSecurity)
                checkSecurityManager(refc, method);
            assert(!method.isMethodHandleInvoke());

            if (refKind == REF_invokeSpecial &&
                refc != lookupClass() &&
                !refc.isInterface() &&
                refc != lookupClass().getSuperclass() &&
                refc.isAssignableFrom(lookupClass())) {
                assert(!method.getName().equals("<init>"));  // 不是此代码路径
                // 根据 JVMS 6.5，invokespecial 指令的描述：
                // 如果方法在 LC 的超类中，
                // 并且如果我们的原始搜索在 LC.super 之上，
                // 从 LC.super 重复搜索（符号查找）
                // 并继续到该类的直接超类，
                // 依此类推，直到找到匹配项或没有更多的超类。
                // FIXME: MemberName.resolve 应该处理这一点。
                Class<?> refcAsSuper = lookupClass();
                MemberName m2;
                do {
                    refcAsSuper = refcAsSuper.getSuperclass();
                    m2 = new MemberName(refcAsSuper,
                                        method.getName(),
                                        method.getMethodType(),
                                        REF_invokeSpecial);
                    m2 = IMPL_NAMES.resolveOrNull(refKind, m2, lookupClassOrNull());
                } while (m2 == null &&         // 尚未找到方法
                         refc != refcAsSuper); // 搜索到 refc
                if (m2 == null)  throw new InternalError(method.toString());
                method = m2;
                refc = refcAsSuper;
                // 重新执行基本检查
                checkMethod(refKind, refc, method);
            }

            DirectMethodHandle dmh = DirectMethodHandle.make(refKind, refc, method);
            MethodHandle mh = dmh;
            // 选择性地使用 restrictReceiver 将接收者参数缩小到 refc。
            if (doRestrict &&
                   (refKind == REF_invokeSpecial ||
                       (MethodHandleNatives.refKindHasReceiver(refKind) &&
                           restrictProtectedReceiver(method)))) {
                mh = restrictReceiver(method, dmh, lookupClass());
            }
            mh = maybeBindCaller(method, mh, callerClass);
            mh = mh.setVarargs(method);
            return mh;
        }
        private MethodHandle maybeBindCaller(MemberName method, MethodHandle mh,
                                             Class<?> callerClass)
                                             throws IllegalAccessException {
            if (allowedModes == TRUSTED || !MethodHandleNatives.isCallerSensitive(method))
                return mh;
            Class<?> hostClass = lookupClass;
            if (!hasPrivateAccess())  // 调用者必须具有私有访问权限
                hostClass = callerClass;  // callerClass 来自安全管理器风格的堆栈遍历
            MethodHandle cbmh = MethodHandleImpl.bindCaller(mh, hostClass);
            // 注意：调用者将在这一阶段之后应用 varargs。
            return cbmh;
        }
        /** 检查访问权限并获取请求的字段。 */
        private MethodHandle getDirectField(byte refKind, Class<?> refc, MemberName field) throws IllegalAccessException {
            final boolean checkSecurity = true;
            return getDirectFieldCommon(refKind, refc, field, checkSecurity);
        }
        /** 检查访问权限并获取请求的字段，省略安全管理器检查。 */
        private MethodHandle getDirectFieldNoSecurityManager(byte refKind, Class<?> refc, MemberName field) throws IllegalAccessException {
            final boolean checkSecurity = false;  // 对于反射或链接 CONSTANT_MH 常量不需要
            return getDirectFieldCommon(refKind, refc, field, checkSecurity);
        }
        /** 所有字段的通用代码；除非从直接上方调用，否则不要直接调用。 */
        private MethodHandle getDirectFieldCommon(byte refKind, Class<?> refc, MemberName field,
                                                  boolean checkSecurity) throws IllegalAccessException {
            checkField(refKind, refc, field);
            // 选择性地检查安全管理器；对于 unreflect* 调用不需要。
            if (checkSecurity)
                checkSecurityManager(refc, field);
            DirectMethodHandle dmh = DirectMethodHandle.make(refc, field);
            boolean doRestrict = (MethodHandleNatives.refKindHasReceiver(refKind) &&
                                    restrictProtectedReceiver(field));
            if (doRestrict)
                return restrictReceiver(field, dmh, lookupClass());
            return dmh;
        }
        /** 检查访问权限并获取请求的构造函数。 */
        private MethodHandle getDirectConstructor(Class<?> refc, MemberName ctor) throws IllegalAccessException {
            final boolean checkSecurity = true;
            return getDirectConstructorCommon(refc, ctor, checkSecurity);
        }
        /** 检查访问权限并获取请求的构造函数，省略安全管理器检查。 */
        private MethodHandle getDirectConstructorNoSecurityManager(Class<?> refc, MemberName ctor) throws IllegalAccessException {
            final boolean checkSecurity = false;  // 对于反射或链接 CONSTANT_MH 常量不需要
            return getDirectConstructorCommon(refc, ctor, checkSecurity);
        }
        /** 所有构造函数的通用代码；除非从直接上方调用，否则不要直接调用。 */
        private MethodHandle getDirectConstructorCommon(Class<?> refc, MemberName ctor,
                                                  boolean checkSecurity) throws IllegalAccessException {
            assert(ctor.isConstructor());
            checkAccess(REF_newInvokeSpecial, refc, ctor);
            // 选择性地检查安全管理器；对于 unreflect* 调用不需要。
            if (checkSecurity)
                checkSecurityManager(refc, ctor);
            assert(!MethodHandleNatives.isCallerSensitive(ctor));  // maybeBindCaller 在这里不相关
            return DirectMethodHandle.make(ctor).setVarargs(ctor);
        }


                    /** 从JVM（通过MethodHandleNatives）调用的Hook，用于链接MH常量：
         */
        /*non-public*/
        MethodHandle linkMethodHandleConstant(byte refKind, Class<?> defc, String name, Object type) throws ReflectiveOperationException {
            if (!(type instanceof Class || type instanceof MethodType))
                throw new InternalError("未解决的MemberName");
            MemberName member = new MemberName(refKind, defc, name, type);
            MethodHandle mh = LOOKASIDE_TABLE.get(member);
            if (mh != null) {
                checkSymbolicClass(defc);
                return mh;
            }
            // 特别处理 MethodHandle.invoke 和 invokeExact。
            if (defc == MethodHandle.class && refKind == REF_invokeVirtual) {
                mh = findVirtualForMH(member.getName(), member.getMethodType());
                if (mh != null) {
                    return mh;
                }
            }
            MemberName resolved = resolveOrFail(refKind, member);
            mh = getDirectMethodForConstant(refKind, defc, resolved);
            if (mh instanceof DirectMethodHandle
                    && canBeCached(refKind, defc, resolved)) {
                MemberName key = mh.internalMemberName();
                if (key != null) {
                    key = key.asNormalOriginal();
                }
                if (member.equals(key)) {  // 宁可安全，不可冒险
                    LOOKASIDE_TABLE.put(key, (DirectMethodHandle) mh);
                }
            }
            return mh;
        }
        private
        boolean canBeCached(byte refKind, Class<?> defc, MemberName member) {
            if (refKind == REF_invokeSpecial) {
                return false;
            }
            if (!Modifier.isPublic(defc.getModifiers()) ||
                    !Modifier.isPublic(member.getDeclaringClass().getModifiers()) ||
                    !member.isPublic() ||
                    member.isCallerSensitive()) {
                return false;
            }
            ClassLoader loader = defc.getClassLoader();
            if (!sun.misc.VM.isSystemDomainLoader(loader)) {
                ClassLoader sysl = ClassLoader.getSystemClassLoader();
                boolean found = false;
                while (sysl != null) {
                    if (loader == sysl) { found = true; break; }
                    sysl = sysl.getParent();
                }
                if (!found) {
                    return false;
                }
            }
            try {
                MemberName resolved2 = publicLookup().resolveOrFail(refKind,
                    new MemberName(refKind, defc, member.getName(), member.getType()));
                checkSecurityManager(defc, resolved2);
            } catch (ReflectiveOperationException | SecurityException ex) {
                return false;
            }
            return true;
        }
        private
        MethodHandle getDirectMethodForConstant(byte refKind, Class<?> defc, MemberName member)
                throws ReflectiveOperationException {
            if (MethodHandleNatives.refKindIsField(refKind)) {
                return getDirectFieldNoSecurityManager(refKind, defc, member);
            } else if (MethodHandleNatives.refKindIsMethod(refKind)) {
                return getDirectMethodNoSecurityManager(refKind, defc, member, lookupClass);
            } else if (refKind == REF_newInvokeSpecial) {
                return getDirectConstructorNoSecurityManager(defc, member);
            }
            // 哦哦
            throw newIllegalArgumentException("错误的MethodHandle常量 #"+member);
        }

        static ConcurrentHashMap<MemberName, DirectMethodHandle> LOOKASIDE_TABLE = new ConcurrentHashMap<>();
    }

    /**
     * 生成一个方法句柄，提供对数组元素的读取访问。
     * 方法句柄的类型将具有数组元素的返回类型。其第一个参数将是数组类型，
     * 第二个参数将是 {@code int}。
     * @param arrayClass 一个数组类型
     * @return 一个可以加载给定数组类型值的方法句柄
     * @throws NullPointerException 如果参数为 null
     * @throws  IllegalArgumentException 如果 arrayClass 不是数组类型
     */
    public static
    MethodHandle arrayElementGetter(Class<?> arrayClass) throws IllegalArgumentException {
        return MethodHandleImpl.makeArrayElementAccessor(arrayClass, false);
    }

    /**
     * 生成一个方法句柄，提供对数组元素的写入访问。
     * 方法句柄的类型将具有 void 返回类型。其最后一个参数将是数组的元素类型。
     * 第一个和第二个参数将是数组类型和 int。
     * @param arrayClass 数组的类
     * @return 一个可以将值存储到数组类型的方法句柄
     * @throws NullPointerException 如果参数为 null
     * @throws IllegalArgumentException 如果 arrayClass 不是数组类型
     */
    public static
    MethodHandle arrayElementSetter(Class<?> arrayClass) throws IllegalArgumentException {
        return MethodHandleImpl.makeArrayElementAccessor(arrayClass, true);
    }

    /// 方法句柄调用（反射风格）

    /**
     * 生成一个方法句柄，该方法句柄将调用给定 {@code type} 的任何方法句柄，用单个尾部 {@code Object[]} 数组替换一定数量的尾部参数。
     * 生成的调用者将是一个具有以下参数的方法句柄：
     * <ul>
     * <li>一个 {@code MethodHandle} 目标
     * <li>零个或多个前导值（由 {@code leadingArgCount} 计数）
     * <li>一个包含尾部参数的 {@code Object[]} 数组
     * </ul>
     * <p>
     * 调用者将像调用 {@link MethodHandle#invoke invoke} 一样调用其目标，使用指示的 {@code type}。
     * 也就是说，如果目标正好是给定的 {@code type}，它将表现得像 {@code invokeExact}；否则，它将表现得像使用 {@link MethodHandle#asType asType}
     * 将目标转换为所需的 {@code type}。
     * <p>
     * 返回的调用者的方法句柄类型不会是给定的 {@code type}，而是除了前 {@code leadingArgCount} 个参数外，所有参数都将被一个类型为 {@code Object[]} 的数组替换，该数组将是最后一个参数。
     * <p>
     * 在调用其目标之前，调用者将展开最终数组，必要时应用引用转换，并取消装箱和扩展基本类型参数。
     * 如果在调用调用者时，提供的数组参数的元素数量不正确，调用者将抛出 {@link IllegalArgumentException} 而不是调用目标。
     * <p>
     * 此方法等效于以下代码（尽管它可能更高效）：
     * <blockquote><pre>{@code
MethodHandle invoker = MethodHandles.invoker(type);
int spreadArgCount = type.parameterCount() - leadingArgCount;
invoker = invoker.asSpreader(Object[].class, spreadArgCount);
return invoker;
     * }</pre></blockquote>
     * 此方法不会抛出反射或安全异常。
     * @param type 所需的目标类型
     * @param leadingArgCount 固定参数的数量，将不变地传递给目标
     * @return 适合调用给定类型任何方法句柄的方法句柄
     * @throws NullPointerException 如果 {@code type} 为 null
     * @throws IllegalArgumentException 如果 {@code leadingArgCount} 不在 0 到 {@code type.parameterCount()}（包括）的范围内，
     *                  或者如果生成的方法句柄的类型将有 <a href="MethodHandle.html#maxarity">太多参数</a>
     */
    static public
    MethodHandle spreadInvoker(MethodType type, int leadingArgCount) {
        if (leadingArgCount < 0 || leadingArgCount > type.parameterCount())
            throw newIllegalArgumentException("错误的参数数量", leadingArgCount);
        type = type.asSpreaderType(Object[].class, type.parameterCount() - leadingArgCount);
        return type.invokers().spreadInvoker(leadingArgCount);
    }


                /**
     * 生成一个特殊的 <em>调用者方法句柄</em>，可以用于调用任何给定类型的任何方法句柄，就像通过 {@link MethodHandle#invokeExact invokeExact} 调用一样。
     * 生成的调用者将具有与所需类型完全相同的类型，只是它将接受一个额外的前导参数，类型为 {@code MethodHandle}。
     * <p>
     * 此方法等效于以下代码（尽管它可能更高效）：
     * {@code publicLookup().findVirtual(MethodHandle.class, "invokeExact", type)}
     *
     * <p style="font-size:smaller;">
     * <em>讨论：</em>
     * 调用者方法句柄在处理未知类型的可变方法句柄时非常有用。
     * 例如，要模拟对可变方法句柄 {@code M} 的 {@code invokeExact} 调用，提取其类型 {@code T}，
     * 查找类型 {@code T} 的调用者方法 {@code X}，
     * 并调用调用者方法，作为 {@code X.invoke(T, A...)}。
     * （不能调用 {@code X.invokeExact}，因为类型 {@code T} 是未知的。）
     * 如果需要展开、收集或其他参数转换，
     * 可以将它们应用于调用者 {@code X} 一次，并在许多 {@code M} 方法句柄值上重用，只要它们与 {@code X} 的类型兼容。
     * <p style="font-size:smaller;">
     * <em>(注意：调用者方法无法通过核心反射 API 获得。
     * 尝试调用 {@linkplain java.lang.reflect.Method#invoke java.lang.reflect.Method.invoke}
     * 声明的 {@code invokeExact} 或 {@code invoke} 方法将引发
     * {@link java.lang.UnsupportedOperationException UnsupportedOperationException}。)</em>
     * <p>
     * 此方法不会抛出任何反射或安全异常。
     * @param type 所需的目标类型
     * @return 适合调用任何给定类型的方法句柄的方法句柄
     * @throws IllegalArgumentException 如果生成的方法句柄的类型具有
     *          <a href="MethodHandle.html#maxarity">过多的参数</a>
     */
    static public
    MethodHandle exactInvoker(MethodType type) {
        return type.invokers().exactInvoker();
    }

    /**
     * 生成一个特殊的 <em>调用者方法句柄</em>，可以用于调用与给定类型兼容的任何方法句柄，就像通过 {@link MethodHandle#invoke invoke} 调用一样。
     * 生成的调用者将具有与所需类型完全相同的类型，只是它将接受一个额外的前导参数，类型为 {@code MethodHandle}。
     * <p>
     * 在调用其目标之前，如果目标与预期类型不同，
     * 调用者将根据需要应用引用转换，并装箱、拆箱或扩展原始值，就像通过 {@link MethodHandle#asType asType} 一样。
     * 同样，返回值将根据需要进行转换。
     * 如果目标是 {@linkplain MethodHandle#asVarargsCollector 可变参数方法句柄}，
     * 将进行必要的参数数量转换，同样就像通过 {@link MethodHandle#asType asType} 一样。
     * <p>
     * 此方法等效于以下代码（尽管它可能更高效）：
     * {@code publicLookup().findVirtual(MethodHandle.class, "invoke", type)}
     * <p style="font-size:smaller;">
     * <em>讨论：</em>
     * 一个 {@linkplain MethodType#genericMethodType 通用方法类型} 是一个仅提及 {@code Object} 参数和返回值的方法类型。
     * 这种类型的调用者能够调用与通用类型具有相同参数数量的任何方法句柄。
     * <p style="font-size:smaller;">
     * <em>(注意：调用者方法无法通过核心反射 API 获得。
     * 尝试调用 {@linkplain java.lang.reflect.Method#invoke java.lang.reflect.Method.invoke}
     * 声明的 {@code invokeExact} 或 {@code invoke} 方法将引发
     * {@link java.lang.UnsupportedOperationException UnsupportedOperationException}。)</em>
     * <p>
     * 此方法不会抛出任何反射或安全异常。
     * @param type 所需的目标类型
     * @return 适合调用任何可转换为给定类型的方法句柄的方法句柄
     * @throws IllegalArgumentException 如果生成的方法句柄的类型具有
     *          <a href="MethodHandle.html#maxarity">过多的参数</a>
     */
    static public
    MethodHandle invoker(MethodType type) {
        return type.invokers().genericInvoker();
    }

    static /*non-public*/
    MethodHandle basicInvoker(MethodType type) {
        return type.invokers().basicInvoker();
    }

     /// method handle modification (creation from other method handles)

    /**
     * 生成一个方法句柄，该方法句柄通过成对的参数和返回类型转换将给定方法句柄的类型适应为新类型。
     * 原始类型和新类型必须具有相同数量的参数。
     * 生成的方法句柄保证报告一个与所需新类型相等的类型。
     * <p>
     * 如果原始类型和新类型相等，返回目标。
     * <p>
     * 允许的转换与 {@link MethodHandle#asType MethodHandle.asType} 相同，
     * 并且如果这些转换失败，还会应用一些额外的转换。
     * 给定类型 <em>T0</em> 和 <em>T1</em>，如果可能，将应用以下转换之一，作为或代替 {@code asType} 执行的任何转换：
     * <ul>
     * <li>如果 <em>T0</em> 和 <em>T1</em> 是引用类型，并且 <em>T1</em> 是接口类型，
     *     则类型为 <em>T0</em> 的值将作为 <em>T1</em> 传递，无需强制转换。
     *     （这种接口处理遵循字节码验证器的用法。）
     * <li>如果 <em>T0</em> 是布尔类型，<em>T1</em> 是其他原始类型，
     *     布尔值将转换为字节值，true 转换为 1，false 转换为 0。
     *     （这种处理遵循字节码验证器的用法。）
     * <li>如果 <em>T1</em> 是布尔类型，<em>T0</em> 是其他原始类型，
     *     <em>T0</em> 将通过 Java 铸造转换（JLS 5.5）转换为字节，
     *     并测试结果的最低有效位，就像通过 {@code (x & 1) != 0} 一样。
     * <li>如果 <em>T0</em> 和 <em>T1</em> 是除布尔类型外的原始类型，
     *     则应用 Java 铸造转换（JLS 5.5）。
     *     （具体来说，<em>T0</em> 将通过扩展和/或缩小转换为 <em>T1</em>。）
     * <li>如果 <em>T0</em> 是引用类型，<em>T1</em> 是原始类型，将在运行时应用拆箱转换，
     *     可能会跟随对原始值的 Java 铸造转换（JLS 5.5），
     *     可能会跟随通过测试最低有效位从字节转换为布尔值。
     * <li>如果 <em>T0</em> 是引用类型，<em>T1</em> 是原始类型，
     *     并且运行时引用为 null，将引入零值。
     * </ul>
     * @param target 调用参数重新类型化后的目标方法句柄
     * @param newType 新方法句柄的预期类型
     * @return 一个委托给目标的方法句柄，在执行任何必要的参数转换后，
     *           并安排任何必要的返回值转换
     * @throws NullPointerException 如果任一参数为 null
     * @throws WrongMethodTypeException 如果无法进行转换
     * @see MethodHandle#asType
     */
    public static
    MethodHandle explicitCastArguments(MethodHandle target, MethodType newType) {
        explicitCastArgumentsChecks(target, newType);
        // 尽可能使用 asTypeCache：
        MethodType oldType = target.type();
        if (oldType == newType)  return target;
        if (oldType.explicitCastEquivalentToAsType(newType)) {
            return target.asFixedArity().asType(newType);
        }
        return MethodHandleImpl.makePairwiseConvert(target, newType, false);
    }


private static void explicitCastArgumentsChecks(MethodHandle target, MethodType newType) {
    if (target.type().parameterCount() != newType.parameterCount()) {
        throw new WrongMethodTypeException("cannot explicitly cast " + target + " to " + newType);
    }
}

/**
 * 生成一个方法句柄，该方法句柄通过重新排序参数来调整给定方法句柄的调用序列以适应新类型。
 * 生成的方法句柄保证报告的类型与所需的新类型相等。
 * <p>
 * 给定的数组控制重新排序。
 * 假设 {@code #I} 是传入参数的数量（即 {@code newType.parameterCount()} 的值），而 {@code #O} 是传出参数的数量（即 {@code target.type().parameterCount()} 的值）。
 * 那么重新排序数组的长度必须为 {@code #O}，并且每个元素必须是非负数且小于 {@code #I}。
 * 对于每个小于 {@code #O} 的 {@code N}，第 {@code N} 个传出参数将从第 {@code I} 个传入参数中获取，其中 {@code I} 是 {@code reorder[N]}。
 * <p>
 * 不应用任何参数或返回值转换。
 * 每个传入参数的类型（由 {@code newType} 确定）必须与目标方法句柄中对应的传出参数或参数的类型相同。
 * {@code newType} 的返回类型必须与原始目标的返回类型相同。
 * <p>
 * 重新排序数组不必指定实际的排列。
 * 如果某个传入参数的索引在数组中出现多次，则该参数将被复制；如果某个传入参数的索引未出现在数组中，则该参数将被丢弃。
 * 与 {@link #dropArguments(MethodHandle,int,List) dropArguments} 类似，未在重新排序数组中提及的传入参数可以是任何类型，仅由 {@code newType} 确定。
 * <blockquote><pre>{@code
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.*;
...
MethodType intfn1 = methodType(int.class, int.class);
MethodType intfn2 = methodType(int.class, int.class, int.class);
MethodHandle sub = ... (int x, int y) -> (x-y) ...;
assert(sub.type().equals(intfn2));
MethodHandle sub1 = permuteArguments(sub, intfn2, 0, 1);
MethodHandle rsub = permuteArguments(sub, intfn2, 1, 0);
assert((int)rsub.invokeExact(1, 100) == 99);
MethodHandle add = ... (int x, int y) -> (x+y) ...;
assert(add.type().equals(intfn2));
MethodHandle twice = permuteArguments(add, intfn1, 0, 0);
assert(twice.type().equals(intfn1));
assert((int)twice.invokeExact(21) == 42);
 * }</pre></blockquote>
 * @param target 调用参数重新排序后的方法句柄
 * @param newType 新方法句柄的预期类型
 * @param reorder 控制重新排序的索引数组
 * @return 一个委托给目标的方法句柄，该方法句柄在丢弃未使用的参数并移动和/或复制其他参数后调用目标
 * @throws NullPointerException 如果任何参数为 null
 * @throws IllegalArgumentException 如果索引数组的长度不等于目标的元数，或者索引数组中的任何元素不是 {@code newType} 参数的有效索引，
 *                  或者 {@code target.type()} 和 {@code newType} 中的两个对应参数类型不相同，
 */
public static
MethodHandle permuteArguments(MethodHandle target, MethodType newType, int... reorder) {
    reorder = reorder.clone();  // 获取一个私有副本
    MethodType oldType = target.type();
    permuteArgumentChecks(reorder, newType, oldType);
    // 首先检测并单独处理丢弃的参数
    int[] originalReorder = reorder;
    BoundMethodHandle result = target.rebind();
    LambdaForm form = result.form;
    int newArity = newType.parameterCount();
    // 将重新排序规范化为实际的排列，
    // 通过删除重复项和添加丢弃的元素。
    // 这在一定程度上改善了 Lambda 形式的缓存，并且通过分步骤简化了转换。
    for (int ddIdx; (ddIdx = findFirstDupOrDrop(reorder, newArity)) != 0; ) {
        if (ddIdx > 0) {
            // 在 reorder[ddIdx] 处找到了重复项。
            // 示例： (x,y,z)->asList(x,y,z)
            // 通过 [1*,0,1] 重新排序 => (a0,a1)=>asList(a1,a0,a1)
            // 通过 [0,1,0*] 重新排序 => (a0,a1)=>asList(a0,a1,a0)
            // 带星号的元素对应于由 dupArgumentForm 转换删除的参数。
            int srcPos = ddIdx, dstPos = srcPos, dupVal = reorder[srcPos];
            boolean killFirst = false;
            for (int val; (val = reorder[--dstPos]) != dupVal; ) {
                // 如果重复项大于中间位置，则设置 killFirst。
                // 这将从排列中移除至少一个反转。
                if (dupVal > val) killFirst = true;
            }
            if (!killFirst) {
                srcPos = dstPos;
                dstPos = ddIdx;
            }
            form = form.editor().dupArgumentForm(1 + srcPos, 1 + dstPos);
            assert (reorder[srcPos] == reorder[dstPos]);
            oldType = oldType.dropParameterTypes(dstPos, dstPos + 1);
            // 通过删除 dstPos 处的元素来收缩重新排序
            int tailPos = dstPos + 1;
            System.arraycopy(reorder, tailPos, reorder, dstPos, reorder.length - tailPos);
            reorder = Arrays.copyOf(reorder, reorder.length - 1);
        } else {
            int dropVal = ~ddIdx, insPos = 0;
            while (insPos < reorder.length && reorder[insPos] < dropVal) {
                // 找到第一个大于 dropVal 的重新排序元素。
                // 这是我们将插入 dropVal 的位置。
                insPos += 1;
            }
            Class<?> ptype = newType.parameterType(dropVal);
            form = form.editor().addArgumentForm(1 + insPos, BasicType.basicType(ptype));
            oldType = oldType.insertParameterTypes(insPos, ptype);
            // 通过在 insPos 处插入一个元素来扩展重新排序
            int tailPos = insPos + 1;
            reorder = Arrays.copyOf(reorder, reorder.length + 1);
            System.arraycopy(reorder, insPos, reorder, tailPos, reorder.length - tailPos);
            reorder[insPos] = dropVal;
        }
        assert (permuteArgumentChecks(reorder, newType, oldType));
    }
    assert (reorder.length == newArity);  // 完美的排列
    // 注意：这可能会缓存过多不同的 LF。考虑退回到可变参数代码。
    form = form.editor().permuteArgumentsForm(1, reorder);
    if (newType == result.type() && form == result.internalForm())
        return result;
    return result.copyWith(newType, form);
}

                /**
     * 返回重新排序中任何重复或遗漏的指示。
     * 如果重新排序包含重复项，返回第二个出现的索引。
     * 否则，返回 ~(n)，对于 [0..newArity-1] 中第一个不在重新排序中的 n。
     * 否则，返回零。
     * 如果遇到不在 [0..newArity-1] 范围内的元素，返回 reorder.length。
     */
    private static int findFirstDupOrDrop(int[] reorder, int newArity) {
        final int BIT_LIMIT = 63;  // 位掩码中最大的位数
        if (newArity < BIT_LIMIT) {
            long mask = 0;
            for (int i = 0; i < reorder.length; i++) {
                int arg = reorder[i];
                if (arg >= newArity) {
                    return reorder.length;
                }
                long bit = 1L << arg;
                if ((mask & bit) != 0) {
                    return i;  // >0 表示重复
                }
                mask |= bit;
            }
            if (mask == (1L << newArity) - 1) {
                assert(Long.numberOfTrailingZeros(Long.lowestOneBit(~mask)) == newArity);
                return 0;
            }
            // 查找第一个零
            long zeroBit = Long.lowestOneBit(~mask);
            int zeroPos = Long.numberOfTrailingZeros(zeroBit);
            assert(zeroPos <= newArity);
            if (zeroPos == newArity) {
                return 0;
            }
            return ~zeroPos;
        } else {
            // 相同的算法，不同的位集
            BitSet mask = new BitSet(newArity);
            for (int i = 0; i < reorder.length; i++) {
                int arg = reorder[i];
                if (arg >= newArity) {
                    return reorder.length;
                }
                if (mask.get(arg)) {
                    return i;  // >0 表示重复
                }
                mask.set(arg);
            }
            int zeroPos = mask.nextClearBit(0);
            assert(zeroPos <= newArity);
            if (zeroPos == newArity) {
                return 0;
            }
            return ~zeroPos;
        }
    }

    private static boolean permuteArgumentChecks(int[] reorder, MethodType newType, MethodType oldType) {
        if (newType.returnType() != oldType.returnType())
            throw newIllegalArgumentException("返回类型不匹配",
                    oldType, newType);
        if (reorder.length == oldType.parameterCount()) {
            int limit = newType.parameterCount();
            boolean bad = false;
            for (int j = 0; j < reorder.length; j++) {
                int i = reorder[j];
                if (i < 0 || i >= limit) {
                    bad = true; break;
                }
                Class<?> src = newType.parameterType(i);
                Class<?> dst = oldType.parameterType(j);
                if (src != dst)
                    throw newIllegalArgumentException("重新排序后参数类型不匹配",
                            oldType, newType);
            }
            if (!bad)  return true;
        }
        throw newIllegalArgumentException("错误的重新排序数组: "+Arrays.toString(reorder));
    }

    /**
     * 生成一个请求返回类型的方法句柄，每次调用时返回给定的常量值。
     * <p>
     * 在返回方法句柄之前，传递的值将转换为请求的类型。
     * 如果请求的类型是基本类型，将尝试扩展基本类型转换，
     * 否则尝试引用转换。
     * <p>返回的方法句柄等同于 {@code identity(type).bindTo(value)}。
     * @param type 所需方法句柄的返回类型
     * @param value 要返回的值
     * @return 一个给定返回类型且无参数的方法句柄，始终返回给定值
     * @throws NullPointerException 如果 {@code type} 参数为 null
     * @throws ClassCastException 如果值不能转换为所需的返回类型
     * @throws IllegalArgumentException 如果给定类型是 {@code void.class}
     */
    public static
    MethodHandle constant(Class<?> type, Object value) {
        if (type.isPrimitive()) {
            if (type == void.class)
                throw newIllegalArgumentException("void 类型");
            Wrapper w = Wrapper.forPrimitiveType(type);
            value = w.convert(value, type);
            if (w.zero().equals(value))
                return zero(w, type);
            return insertArguments(identity(type), 0, value);
        } else {
            if (value == null)
                return zero(Wrapper.OBJECT, type);
            return identity(type).bindTo(value);
        }
    }

    /**
     * 生成一个方法句柄，当调用时返回其唯一参数。
     * @param type 所需方法句柄的唯一参数和返回值的类型
     * @return 一个接受并返回给定类型的单参数方法句柄
     * @throws NullPointerException 如果参数为 null
     * @throws IllegalArgumentException 如果给定类型是 {@code void.class}
     */
    public static
    MethodHandle identity(Class<?> type) {
        Wrapper btw = (type.isPrimitive() ? Wrapper.forPrimitiveType(type) : Wrapper.OBJECT);
        int pos = btw.ordinal();
        MethodHandle ident = IDENTITY_MHS[pos];
        if (ident == null) {
            ident = setCachedMethodHandle(IDENTITY_MHS, pos, makeIdentity(btw.primitiveType()));
        }
        if (ident.type().returnType() == type)
            return ident;
        // 类似于 identity(Foo.class); 不需要缓存这些
        assert(btw == Wrapper.OBJECT);
        return makeIdentity(type);
    }
    private static final MethodHandle[] IDENTITY_MHS = new MethodHandle[Wrapper.values().length];
    private static MethodHandle makeIdentity(Class<?> ptype) {
        MethodType mtype = MethodType.methodType(ptype, ptype);
        LambdaForm lform = LambdaForm.identityForm(BasicType.basicType(ptype));
        return MethodHandleImpl.makeIntrinsic(mtype, lform, Intrinsic.IDENTITY);
    }


                private static MethodHandle zero(Wrapper btw, Class<?> rtype) {
        int pos = btw.ordinal();
        MethodHandle zero = ZERO_MHS[pos];
        if (zero == null) {
            zero = setCachedMethodHandle(ZERO_MHS, pos, makeZero(btw.primitiveType()));
        }
        if (zero.type().returnType() == rtype)
            return zero;
        assert(btw == Wrapper.OBJECT);
        return makeZero(rtype);
    }
    private static final MethodHandle[] ZERO_MHS = new MethodHandle[Wrapper.values().length];
    private static MethodHandle makeZero(Class<?> rtype) {
        MethodType mtype = MethodType.methodType(rtype);
        LambdaForm lform = LambdaForm.zeroForm(BasicType.basicType(rtype));
        return MethodHandleImpl.makeIntrinsic(mtype, lform, Intrinsic.ZERO);
    }

    synchronized private static MethodHandle setCachedMethodHandle(MethodHandle[] cache, int pos, MethodHandle value) {
        // 模拟一个 CAS，以避免结果的竞态复制。
        MethodHandle prev = cache[pos];
        if (prev != null) return prev;
        return cache[pos] = value;
    }

    /**
     * 提供一个目标方法句柄，该句柄在调用前预先绑定一个或多个<em>绑定参数</em>。
     * 对应于绑定参数的目标形式参数称为<em>绑定参数</em>。
     * 返回一个新的方法句柄，该句柄保存绑定参数。
     * 当它被调用时，它接收任何非绑定参数的参数，
     * 将保存的参数绑定到相应的参数，
     * 并调用原始目标。
     * <p>
     * 新方法句柄的类型将从原始目标类型中删除绑定参数的类型，
     * 因为新方法句柄不再需要调用者提供这些参数。
     * <p>
     * 每个给定的参数对象必须与相应的绑定参数类型匹配。
     * 如果绑定参数类型是原始类型，参数对象
     * 必须是一个包装器，并将被拆箱以生成原始值。
     * <p>
     * {@code pos} 参数选择要绑定的参数。
     * 它的范围可以是从零到 <i>N-L</i>（包括两端），
     * 其中 <i>N</i> 是目标方法句柄的元数，
     * <i>L</i> 是值数组的长度。
     * @param target 在插入参数后调用的方法句柄
     * @param pos 插入参数的位置（零表示第一个位置）
     * @param values 要插入的参数序列
     * @return 一个方法句柄，该句柄插入一个额外的参数，
     *         然后调用原始方法句柄
     * @throws NullPointerException 如果目标或 {@code values} 数组为 null
     * @see MethodHandle#bindTo
     */
    public static
    MethodHandle insertArguments(MethodHandle target, int pos, Object... values) {
        int insCount = values.length;
        Class<?>[] ptypes = insertArgumentsChecks(target, insCount, pos);
        if (insCount == 0)  return target;
        BoundMethodHandle result = target.rebind();
        for (int i = 0; i < insCount; i++) {
            Object value = values[i];
            Class<?> ptype = ptypes[pos+i];
            if (ptype.isPrimitive()) {
                result = insertArgumentPrimitive(result, pos, ptype, value);
            } else {
                value = ptype.cast(value);  // 如果需要，抛出 CCE
                result = result.bindArgumentL(pos, value);
            }
        }
        return result;
    }

    private static BoundMethodHandle insertArgumentPrimitive(BoundMethodHandle result, int pos,
                                                             Class<?> ptype, Object value) {
        Wrapper w = Wrapper.forPrimitiveType(ptype);
        // 执行拆箱和/或原始类型转换
        value = w.convert(value, ptype);
        switch (w) {
        case INT:     return result.bindArgumentI(pos, (int)value);
        case LONG:    return result.bindArgumentJ(pos, (long)value);
        case FLOAT:   return result.bindArgumentF(pos, (float)value);
        case DOUBLE:  return result.bindArgumentD(pos, (double)value);
        default:      return result.bindArgumentI(pos, ValueConversions.widenSubword(value));
        }
    }

    private static Class<?>[] insertArgumentsChecks(MethodHandle target, int insCount, int pos) throws RuntimeException {
        MethodType oldType = target.type();
        int outargs = oldType.parameterCount();
        int inargs  = outargs - insCount;
        if (inargs < 0)
            throw newIllegalArgumentException("too many values to insert");
        if (pos < 0 || pos > inargs)
            throw newIllegalArgumentException("no argument type to append");
        return oldType.ptypes();
    }

    /**
     * 生成一个方法句柄，该句柄在调用其他指定的 <i>target</i> 方法句柄之前丢弃一些虚拟参数。
     * 新方法句柄的类型将与目标类型相同，
     * 除了它还将包括虚拟参数类型，
     * 在某个给定位置。
     * <p>
     * {@code pos} 参数的范围可以是从零到 <i>N</i>，
     * 其中 <i>N</i> 是目标的元数。
     * 如果 {@code pos} 为零，虚拟参数将位于
     * 目标的真实参数之前；如果 {@code pos} 为 <i>N</i>
     * 它们将位于其后。
     * <p>
     * <b>示例：</b>
     * <blockquote><pre>{@code
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.*;
...
MethodHandle cat = lookup().findVirtual(String.class,
  "concat", methodType(String.class, String.class));
assertEquals("xy", (String) cat.invokeExact("x", "y"));
MethodType bigType = cat.type().insertParameterTypes(0, int.class, String.class);
MethodHandle d0 = dropArguments(cat, 0, bigType.parameterList().subList(0,2));
assertEquals(bigType, d0.type());
assertEquals("yz", (String) d0.invokeExact(123, "x", "y", "z"));
     * }</pre></blockquote>
     * <p>
     * 此方法也等同于以下代码：
     * <blockquote><pre>
     * {@link #dropArguments(MethodHandle,int,Class...) dropArguments}{@code (target, pos, valueTypes.toArray(new Class[0]))}
     * </pre></blockquote>
     * @param target 在丢弃参数后调用的方法句柄
     * @param valueTypes 要丢弃的参数的类型
     * @param pos 要丢弃的第一个参数的位置（零表示最左边）
     * @return 一个方法句柄，该句柄丢弃给定类型的参数，
     *         然后调用原始方法句柄
     * @throws NullPointerException 如果目标为 null，
     *                              或者 {@code valueTypes} 列表或其任何元素为 null
     * @throws IllegalArgumentException 如果 {@code valueTypes} 的任何元素为 {@code void.class}，
     *                  或者 {@code pos} 为负数或大于目标的元数，
     *                  或者新方法句柄的类型参数过多
     */
    public static
    MethodHandle dropArguments(MethodHandle target, int pos, List<Class<?>> valueTypes) {
        valueTypes = copyTypes(valueTypes);
        MethodType oldType = target.type();  // 获取 NPE
        int dropped = dropArgumentChecks(oldType, pos, valueTypes);
        MethodType newType = oldType.insertParameterTypes(pos, valueTypes);
        if (dropped == 0)  return target;
        BoundMethodHandle result = target.rebind();
        LambdaForm lform = result.form;
        int insertFormArg = 1 + pos;
        for (Class<?> ptype : valueTypes) {
            lform = lform.editor().addArgumentForm(insertFormArg++, BasicType.basicType(ptype));
        }
        result = result.copyWith(newType, lform);
        return result;
    }


                private static List<Class<?>> copyTypes(List<Class<?>> types) {
        Object[] a = types.toArray();
        return Arrays.asList(Arrays.copyOf(a, a.length, Class[].class));
    }

    private static int dropArgumentChecks(MethodType oldType, int pos, List<Class<?>> valueTypes) {
        int dropped = valueTypes.size();
        MethodType.checkSlotCount(dropped);
        int outargs = oldType.parameterCount();
        int inargs  = outargs + dropped;
        if (pos < 0 || pos > outargs)
            throw newIllegalArgumentException("no argument type to remove"
                    + Arrays.asList(oldType, pos, valueTypes, inargs, outargs)
                    );
        return dropped;
    }

    /**
     * 生成一个方法句柄，该方法句柄在调用其他指定的<i>目标</i>方法句柄之前会丢弃一些虚拟参数。
     * 新方法句柄的类型将与目标的类型相同，但还会包括虚拟参数类型，
     * 位于某个给定位置。
     * <p>
     * {@code pos} 参数的范围可以是 0 到 <i>N</i>，
     * 其中 <i>N</i> 是目标的元数。
     * 如果 {@code pos} 为零，虚拟参数将位于目标的真实参数之前；
     * 如果 {@code pos} 为 <i>N</i>，则位于其后。
     * <p>
     * <b>示例：</b>
     * <blockquote><pre>{@code
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.*;
...
MethodHandle cat = lookup().findVirtual(String.class,
  "concat", methodType(String.class, String.class));
assertEquals("xy", (String) cat.invokeExact("x", "y"));
MethodHandle d0 = dropArguments(cat, 0, String.class);
assertEquals("yz", (String) d0.invokeExact("x", "y", "z"));
MethodHandle d1 = dropArguments(cat, 1, String.class);
assertEquals("xz", (String) d1.invokeExact("x", "y", "z"));
MethodHandle d2 = dropArguments(cat, 2, String.class);
assertEquals("xy", (String) d2.invokeExact("x", "y", "z"));
MethodHandle d12 = dropArguments(cat, 1, int.class, boolean.class);
assertEquals("xz", (String) d12.invokeExact("x", 12, true, "z"));
     * }</pre></blockquote>
     * <p>
     * 此方法等同于以下代码：
     * <blockquote><pre>
     * {@link #dropArguments(MethodHandle,int,List) dropArguments}{@code (target, pos, Arrays.asList(valueTypes))}
     * </pre></blockquote>
     * @param target 调用虚拟参数后要调用的方法句柄
     * @param valueTypes 要丢弃的参数类型
     * @param pos 要丢弃的第一个参数的位置（最左边为零）
     * @return 一个在调用原始方法句柄之前丢弃指定类型参数的方法句柄
     * @throws NullPointerException 如果目标为 null，
     *                              或者 {@code valueTypes} 数组或其任何元素为 null
     * @throws IllegalArgumentException 如果 {@code valueTypes} 的任何元素为 {@code void.class}，
     *                  或者 {@code pos} 为负数或大于目标的元数，
     *                  或者新方法句柄的类型将有
     *                  <a href="MethodHandle.html#maxarity">太多参数</a>
     */
    public static
    MethodHandle dropArguments(MethodHandle target, int pos, Class<?>... valueTypes) {
        return dropArguments(target, pos, Arrays.asList(valueTypes));
    }

    /**
     * 通过预处理目标方法句柄的一个或多个参数，每个参数使用其自己的单参数过滤函数，
     * 然后调用目标，用每个预处理参数的过滤函数结果替换该参数。
     * <p>
     * 预处理由 {@code filters} 数组中的一个或多个方法句柄执行。
     * 过滤数组的第一个元素对应于目标的 {@code pos} 参数，依此类推。
     * <p>
     * 数组中的 null 参数被视为恒等函数，
     * 对应的参数保持不变。
     * （如果数组中没有非 null 元素，则返回原始目标。）
     * 每个过滤器应用于适配器的相应参数。
     * <p>
     * 如果过滤器 {@code F} 应用于目标的第 {@code N} 个参数，
     * 则 {@code F} 必须是一个恰好接受一个参数的方法句柄。
     * {@code F} 的唯一参数类型替换目标的相应参数类型
     * 在生成的适配方法句柄中。
     * {@code F} 的返回类型必须与目标的相应参数类型相同。
     * <p>
     * 如果 {@code filters} 中的元素
     * （null 或非 null）
     * 不对应于目标的参数位置，则为错误。
     * <p><b>示例：</b>
     * <blockquote><pre>{@code
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.*;
...
MethodHandle cat = lookup().findVirtual(String.class,
  "concat", methodType(String.class, String.class));
MethodHandle upcase = lookup().findVirtual(String.class,
  "toUpperCase", methodType(String.class));
assertEquals("xy", (String) cat.invokeExact("x", "y"));
MethodHandle f0 = filterArguments(cat, 0, upcase);
assertEquals("Xy", (String) f0.invokeExact("x", "y")); // Xy
MethodHandle f1 = filterArguments(cat, 1, upcase);
assertEquals("xY", (String) f1.invokeExact("x", "y")); // xY
MethodHandle f2 = filterArguments(cat, 0, upcase, upcase);
assertEquals("XY", (String) f2.invokeExact("x", "y")); // XY
     * }</pre></blockquote>
     * <p> 以下是生成的适配器的伪代码：
     * <blockquote><pre>{@code
     * V target(P... p, A[i]... a[i], B... b);
     * A[i] filter[i](V[i]);
     * T adapter(P... p, V[i]... v[i], B... b) {
     *   return target(p..., f[i](v[i])..., b...);
     * }
     * }</pre></blockquote>
     *
     * @param target 调用参数过滤后的目标方法句柄
     * @param pos 要过滤的第一个参数的位置
     * @param filters 要在过滤参数上最初调用的方法句柄
     * @return 包含指定参数过滤逻辑的方法句柄
     * @throws NullPointerException 如果目标为 null
     *                              或者 {@code filters} 数组为 null
     * @throws IllegalArgumentException 如果 {@code filters} 的非 null 元素
     *          不符合上述目标的相应参数类型，
     *          或者 {@code pos+filters.length} 大于 {@code target.type().parameterCount()}，
     *          或者生成的方法句柄的类型将有
     *          <a href="MethodHandle.html#maxarity">太多参数</a>
     */
    public static
    MethodHandle filterArguments(MethodHandle target, int pos, MethodHandle... filters) {
        filterArgumentsCheckArity(target, pos, filters);
        MethodHandle adapter = target;
        int curPos = pos-1;  // 预增量
        for (MethodHandle filter : filters) {
            curPos += 1;
            if (filter == null)  continue;  // 忽略 filters 中的 null 元素
            adapter = filterArgument(adapter, curPos, filter);
        }
        return adapter;
    }


                /*non-public*/ static
    MethodHandle filterArgument(MethodHandle target, int pos, MethodHandle filter) {
        filterArgumentChecks(target, pos, filter);
        MethodType targetType = target.type();
        MethodType filterType = filter.type();
        BoundMethodHandle result = target.rebind();
        Class<?> newParamType = filterType.parameterType(0);
        LambdaForm lform = result.editor().filterArgumentForm(1 + pos, BasicType.basicType(newParamType));
        MethodType newType = targetType.changeParameterType(pos, newParamType);
        result = result.copyWithExtendL(newType, lform, filter);
        return result;
    }

    private static void filterArgumentsCheckArity(MethodHandle target, int pos, MethodHandle[] filters) {
        MethodType targetType = target.type();
        int maxPos = targetType.parameterCount();
        if (pos + filters.length > maxPos)
            throw newIllegalArgumentException("too many filters");
    }

    private static void filterArgumentChecks(MethodHandle target, int pos, MethodHandle filter) throws RuntimeException {
        MethodType targetType = target.type();
        MethodType filterType = filter.type();
        if (filterType.parameterCount() != 1
            || filterType.returnType() != targetType.parameterType(pos))
            throw newIllegalArgumentException("target and filter types do not match", targetType, filterType);
    }

    /**
     * 通过预处理目标方法句柄的子参数序列来适应目标方法句柄（另一个方法句柄）。
     * 预处理的参数被过滤函数的结果（如果有）替换。
     * 然后在修改后的（通常更短的）参数列表上调用目标。
     * <p>
     * 如果过滤器返回一个值，目标必须接受该值作为其位置 {@code pos} 的参数，
     * 该参数前面和/或后面跟着未传递给过滤器的任何参数。
     * 如果过滤器返回 void，目标必须接受所有未传递给过滤器的参数。
     * 没有参数被重新排序，过滤器返回的结果（如果有）将按顺序替换最初传递给适配器的整个子参数序列。
     * <p>
     * 过滤器的参数类型（如果有）替换目标在位置 {@code pos} 的零个或一个参数类型，
     * 在结果适配方法句柄中。
     * 过滤器的返回类型（如果有）必须与目标在位置 {@code pos} 的参数类型相同，
     * 该目标参数由过滤器的返回值提供。
     * <p>
     * 在所有情况下，{@code pos} 必须大于或等于零，并且
     * {@code pos} 也必须小于或等于目标的元数。
     * <p><b>示例：</b>
     * <blockquote><pre>{@code
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.*;
...
MethodHandle deepToString = publicLookup()
  .findStatic(Arrays.class, "deepToString", methodType(String.class, Object[].class));

MethodHandle ts1 = deepToString.asCollector(String[].class, 1);
assertEquals("[strange]", (String) ts1.invokeExact("strange"));

MethodHandle ts2 = deepToString.asCollector(String[].class, 2);
assertEquals("[up, down]", (String) ts2.invokeExact("up", "down"));

MethodHandle ts3 = deepToString.asCollector(String[].class, 3);
MethodHandle ts3_ts2 = collectArguments(ts3, 1, ts2);
assertEquals("[top, [up, down], strange]",
             (String) ts3_ts2.invokeExact("top", "up", "down", "strange"));

MethodHandle ts3_ts2_ts1 = collectArguments(ts3_ts2, 3, ts1);
assertEquals("[top, [up, down], [strange]]",
             (String) ts3_ts2_ts1.invokeExact("top", "up", "down", "strange"));

MethodHandle ts3_ts2_ts3 = collectArguments(ts3_ts2, 1, ts3);
assertEquals("[top, [[up, down, strange], charm], bottom]",
             (String) ts3_ts2_ts3.invokeExact("top", "up", "down", "strange", "charm", "bottom"));
     * }</pre></blockquote>
     * <p> 这是结果适配器的伪代码：
     * <blockquote><pre>{@code
     * T target(A...,V,C...);
     * V filter(B...);
     * T adapter(A... a,B... b,C... c) {
     *   V v = filter(b...);
     *   return target(a...,v,c...);
     * }
     * // 如果过滤器没有参数：
     * T target2(A...,V,C...);
     * V filter2();
     * T adapter2(A... a,C... c) {
     *   V v = filter2();
     *   return target2(a...,v,c...);
     * }
     * // 如果过滤器返回 void：
     * T target3(A...,C...);
     * void filter3(B...);
     * void adapter3(A... a,B... b,C... c) {
     *   filter3(b...);
     *   return target3(a...,c...);
     * }
     * }</pre></blockquote>
     * <p>
     * 集合适配器 {@code collectArguments(mh, 0, coll)} 等效于
     * 先“折叠”受影响的参数，然后删除它们，如下所示：
     * <blockquote><pre>{@code
     * mh = MethodHandles.dropArguments(mh, 1, coll.type().parameterList()); //step 2
     * mh = MethodHandles.foldArguments(mh, coll); //step 1
     * }</pre></blockquote>
     * 如果目标方法句柄除了过滤器 {@code coll} 的结果（如果有）之外不消耗任何参数，
     * 那么 {@code collectArguments(mh, 0, coll)} 等效于 {@code filterReturnValue(coll, mh)}。
     * 如果过滤器方法句柄 {@code coll} 消耗一个参数并产生非 void 结果，
     * 那么 {@code collectArguments(mh, N, coll)} 等效于 {@code filterArguments(mh, N, coll)}。
     * 其他等效性是可能的，但需要参数排列。
     *
     * @param target 过滤子参数序列后要调用的方法句柄
     * @param pos 要传递给过滤器的第一个适配器参数的位置，
     *            和/或接收过滤器结果的目标参数的位置
     * @param filter 要在子参数序列上调用的方法句柄
     * @return 包含指定参数子序列过滤逻辑的方法句柄
     * @throws NullPointerException 如果任一参数为 null
     * @throws IllegalArgumentException 如果 {@code filter} 的返回类型
     *          是非 void 且与目标的 {@code pos} 参数不同，
     *          或者 {@code pos} 不在 0 和目标的元数之间（包括 0 和元数），
     *          或者结果方法句柄的类型将有
     *          <a href="MethodHandle.html#maxarity">太多参数</a>
     * @see MethodHandles#foldArguments
     * @see MethodHandles#filterArguments
     * @see MethodHandles#filterReturnValue
     */
    public static
    MethodHandle collectArguments(MethodHandle target, int pos, MethodHandle filter) {
        MethodType newType = collectArgumentsChecks(target, pos, filter);
        MethodType collectorType = filter.type();
        BoundMethodHandle result = target.rebind();
        LambdaForm lform;
        if (collectorType.returnType().isArray() && filter.intrinsicName() == Intrinsic.NEW_ARRAY) {
            lform = result.editor().collectArgumentArrayForm(1 + pos, filter);
            if (lform != null) {
                return result.copyWith(newType, lform);
            }
        }
        lform = result.editor().collectArgumentsForm(1 + pos, collectorType.basicType());
        return result.copyWithExtendL(newType, lform, filter);
    }


private static MethodType collectArgumentsChecks(MethodHandle target, int pos, MethodHandle filter) throws RuntimeException {
    MethodType targetType = target.type();
    MethodType filterType = filter.type();
    Class<?> rtype = filterType.returnType();
    List<Class<?>> filterArgs = filterType.parameterList();
    if (rtype == void.class) {
        return targetType.insertParameterTypes(pos, filterArgs);
    }
    if (rtype != targetType.parameterType(pos)) {
        throw newIllegalArgumentException("target and filter types do not match", targetType, filterType);
    }
    return targetType.dropParameterTypes(pos, pos+1).insertParameterTypes(pos, filterArgs);
}

/**
 * 通过后处理目标方法句柄的返回值（如果有）来适应目标方法句柄
 * 使用过滤器（另一个方法句柄）处理返回值。
 * 过滤器的结果从适配器返回。
 * <p>
 * 如果目标返回一个值，过滤器必须接受该值作为其唯一参数。
 * 如果目标返回void，过滤器必须不接受任何参数。
 * <p>
 * 过滤器的返回类型
 * 替换目标的返回类型
 * 在结果适配方法句柄中。
 * 过滤器的参数类型（如果有）必须与目标的返回类型相同。
 * <p><b>示例：</b>
 * <blockquote><pre>{@code
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.*;
...
MethodHandle cat = lookup().findVirtual(String.class,
  "concat", methodType(String.class, String.class));
MethodHandle length = lookup().findVirtual(String.class,
  "length", methodType(int.class));
System.out.println((String) cat.invokeExact("x", "y")); // xy
MethodHandle f0 = filterReturnValue(cat, length);
System.out.println((int) f0.invokeExact("x", "y")); // 2
 * }</pre></blockquote>
 * <p>以下是结果适配器的伪代码：
 * <blockquote><pre>{@code
 * V target(A...);
 * T filter(V);
 * T adapter(A... a) {
 *   V v = target(a...);
 *   return filter(v);
 * }
 * // 如果目标返回void：
 * void target2(A...);
 * T filter2();
 * T adapter2(A... a) {
 *   target2(a...);
 *   return filter2();
 * }
 * // 如果过滤器返回void：
 * V target3(A...);
 * void filter3(V);
 * void adapter3(A... a) {
 *   V v = target3(a...);
 *   filter3(v);
 * }
 * }</pre></blockquote>
 * @param target 调用前过滤返回值的方法句柄
 * @param filter 在返回值上调用的方法句柄
 * @return 包含指定返回值过滤逻辑的方法句柄
 * @throws NullPointerException 如果任一参数为null
 * @throws IllegalArgumentException 如果过滤器的参数列表
 *          与目标的返回类型不匹配，如上所述
 */
public static
MethodHandle filterReturnValue(MethodHandle target, MethodHandle filter) {
    MethodType targetType = target.type();
    MethodType filterType = filter.type();
    filterReturnValueChecks(targetType, filterType);
    BoundMethodHandle result = target.rebind();
    BasicType rtype = BasicType.basicType(filterType.returnType());
    LambdaForm lform = result.editor().filterReturnForm(rtype, false);
    MethodType newType = targetType.changeReturnType(filterType.returnType());
    result = result.copyWithExtendL(newType, lform, filter);
    return result;
}

private static void filterReturnValueChecks(MethodType targetType, MethodType filterType) throws RuntimeException {
    Class<?> rtype = targetType.returnType();
    int filterValues = filterType.parameterCount();
    if (filterValues == 0
            ? (rtype != void.class)
            : (rtype != filterType.parameterType(0) || filterValues != 1))
        throw newIllegalArgumentException("target and filter types do not match", targetType, filterType);
}

/**
 * 通过预处理目标方法句柄的一些参数来适应目标方法句柄，然后调用目标
 * 将预处理的结果插入到原始参数序列中。
 * <p>
 * 预处理由 {@code combiner}，第二个方法句柄执行。
 * 传递给适配器的参数中，前 {@code N} 个参数
 * 被复制到组合器中，然后调用组合器。
 * （这里，{@code N} 定义为组合器的参数计数。）
 * 之后，控制传递给目标，组合器的任何结果
 * 插入到原始 {@code N} 个传入参数之前。
 * <p>
 * 如果组合器返回一个值，目标的第一个参数类型
 * 必须与组合器的返回类型相同，接下来的
 * {@code N} 个参数类型必须与组合器的参数完全匹配。
 * <p>
 * 如果组合器返回void，不会插入任何结果，
 * 目标的前 {@code N} 个参数类型
 * 必须与组合器的参数完全匹配。
 * <p>
 * 结果适配器与目标具有相同的类型，只是
 * 如果它对应于组合器的结果，则第一个参数类型被删除。
 * <p>
 * （注意，可以使用 {@link #dropArguments(MethodHandle,int,List) dropArguments} 来删除组合器或目标不希望接收的任何参数。
 * 如果一些传入的参数仅用于组合器，
 * 考虑使用 {@link MethodHandle#asCollector asCollector}，因为这些参数在进入目标时不需要在堆栈上存活。）
 * <p><b>示例：</b>
 * <blockquote><pre>{@code
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.*;
...
MethodHandle trace = publicLookup().findVirtual(java.io.PrintStream.class,
  "println", methodType(void.class, String.class))
    .bindTo(System.out);
MethodHandle cat = lookup().findVirtual(String.class,
  "concat", methodType(String.class, String.class));
assertEquals("boojum", (String) cat.invokeExact("boo", "jum"));
MethodHandle catTrace = foldArguments(cat, trace);
// 也打印 "boo"：
assertEquals("boojum", (String) catTrace.invokeExact("boo", "jum"));
 * }</pre></blockquote>
 * <p>以下是结果适配器的伪代码：
 * <blockquote><pre>{@code
 * // A... 中有 N 个参数
 * T target(V, A[N]..., B...);
 * V combiner(A...);
 * T adapter(A... a, B... b) {
 *   V v = combiner(a...);
 *   return target(v, a..., b...);
 * }
 * // 如果组合器返回void：
 * T target2(A[N]..., B...);
 * void combiner2(A...);
 * T adapter2(A... a, B... b) {
 *   combiner2(a...);
 *   return target2(a..., b...);
 * }
 * }</pre></blockquote>
 * @param target 在参数组合后调用的方法句柄
 * @param combiner 初始时在传入参数上调用的方法句柄
 * @return 包含指定参数折叠逻辑的方法句柄
 * @throws NullPointerException 如果任一参数为null
 * @throws IllegalArgumentException 如果 {@code combiner} 的返回类型
 *          不是void且与目标的第一个参数类型不同，或者目标的前 {@code N} 个参数类型
 *          （跳过一个与 {@code combiner} 的返回类型匹配的参数）
 *          与 {@code combiner} 的参数类型不相同
 */
public static
MethodHandle foldArguments(MethodHandle target, MethodHandle combiner) {
    int foldPos = 0;
    MethodType targetType = target.type();
    MethodType combinerType = combiner.type();
    Class<?> rtype = foldArgumentChecks(foldPos, targetType, combinerType);
    BoundMethodHandle result = target.rebind();
    boolean dropResult = (rtype == void.class);
    // 注意：这可能会缓存太多不同的 LF。考虑退回到 varargs 代码。
    LambdaForm lform = result.editor().foldArgumentsForm(1 + foldPos, dropResult, combinerType.basicType());
    MethodType newType = targetType;
    if (!dropResult)
        newType = newType.dropParameterTypes(foldPos, foldPos + 1);
    result = result.copyWithExtendL(newType, lform, combiner);
    return result;
}


private static Class<?> foldArgumentChecks(int foldPos, MethodType targetType, MethodType combinerType) {
    int foldArgs   = combinerType.parameterCount();
    Class<?> rtype = combinerType.returnType();
    int foldVals = rtype == void.class ? 0 : 1;
    int afterInsertPos = foldPos + foldVals;
    boolean ok = (targetType.parameterCount() >= afterInsertPos + foldArgs);
    if (ok && !(combinerType.parameterList()
                .equals(targetType.parameterList().subList(afterInsertPos,
                                                           afterInsertPos + foldArgs))))
        ok = false;
    if (ok && foldVals != 0 && combinerType.returnType() != targetType.parameterType(0))
        ok = false;
    if (!ok)
        throw misMatchedTypes("target and combiner types", targetType, combinerType);
    return rtype;
}

/**
 * 创建一个方法句柄，该方法句柄通过一个测试方法句柄来保护目标方法句柄。
 * 如果测试失败，将调用一个备用句柄。
 * 所有三个方法句柄必须具有相同的对应参数和返回类型，但测试的返回类型必须为布尔值，
 * 并且测试允许具有比其他两个方法句柄更少的参数。
 * <p>以下是生成的适配器的伪代码：
 * <blockquote><pre>{@code
 * boolean test(A...);
 * T target(A...,B...);
 * T fallback(A...,B...);
 * T adapter(A... a,B... b) {
 *   if (test(a...))
 *     return target(a..., b...);
 *   else
 *     return fallback(a..., b...);
 * }
 * }</pre></blockquote>
 * 注意，测试参数（伪代码中的{@code a...}）不能被测试的执行修改，因此它们将不变地从调用者传递到目标或备用句柄。
 * @param test 用于测试的方法句柄，必须返回布尔值
 * @param target 如果测试通过要调用的方法句柄
 * @param fallback 如果测试失败要调用的方法句柄
 * @return 包含指定的if/then/else逻辑的方法句柄
 * @throws NullPointerException 如果任何参数为null
 * @throws IllegalArgumentException 如果测试不返回布尔值，或者所有三种方法类型不匹配（测试的返回类型已更改为与目标匹配）。
 */
public static
MethodHandle guardWithTest(MethodHandle test,
                           MethodHandle target,
                           MethodHandle fallback) {
    MethodType gtype = test.type();
    MethodType ttype = target.type();
    MethodType ftype = fallback.type();
    if (!ttype.equals(ftype))
        throw misMatchedTypes("target and fallback types", ttype, ftype);
    if (gtype.returnType() != boolean.class)
        throw newIllegalArgumentException("guard type is not a predicate "+gtype);
    List<Class<?>> targs = ttype.parameterList();
    List<Class<?>> gargs = gtype.parameterList();
    if (!targs.equals(gargs)) {
        int gpc = gargs.size(), tpc = targs.size();
        if (gpc >= tpc || !targs.subList(0, gpc).equals(gargs))
            throw misMatchedTypes("target and test types", ttype, gtype);
        test = dropArguments(test, gpc, targs.subList(gpc, tpc));
        gtype = test.type();
    }
    return MethodHandleImpl.makeGuardWithTest(test, target, fallback);
}

static RuntimeException misMatchedTypes(String what, MethodType t1, MethodType t2) {
    return newIllegalArgumentException(what + " must match: " + t1 + " != " + t2);
}

/**
 * 创建一个方法句柄，该方法句柄通过在异常处理程序中运行目标方法句柄来适配它。
 * 如果目标正常返回，适配器将返回该值。
 * 如果抛出与指定类型匹配的异常，将调用备用句柄，该句柄将接收异常和原始参数。
 * <p>
 * 目标和处理程序必须具有相同的对应参数和返回类型，但处理程序可以省略尾随参数
 * （类似于{@link #guardWithTest guardWithTest}中的谓词）。
 * 另外，处理程序必须有一个额外的前导参数，类型为{@code exType}或其超类型。
 * <p>以下是生成的适配器的伪代码：
 * <blockquote><pre>{@code
 * T target(A..., B...);
 * T handler(ExType, A...);
 * T adapter(A... a, B... b) {
 *   try {
 *     return target(a..., b...);
 *   } catch (ExType ex) {
 *     return handler(ex, a...);
 *   }
 * }
 * }</pre></blockquote>
 * 注意，保存的参数（伪代码中的{@code a...}）不能被目标的执行修改，因此它们将不变地从调用者传递到处理程序，如果调用了处理程序的话。
 * <p>
 * 目标和处理程序必须返回相同的类型，即使处理程序总是抛出异常。（例如，处理程序可能模拟一个{@code finally}子句）。
 * 要创建这样一个抛出异常的处理程序，可以将处理程序创建逻辑与{@link #throwException throwException}组合，
 * 以创建具有正确返回类型的方法句柄。
 * @param target 要调用的方法句柄
 * @param exType 处理程序将捕获的异常类型
 * @param handler 如果抛出匹配的异常要调用的方法句柄
 * @return 包含指定的try/catch逻辑的方法句柄
 * @throws NullPointerException 如果任何参数为null
 * @throws IllegalArgumentException 如果处理程序不接受给定的异常类型，或者方法句柄类型在返回类型和对应参数上不匹配
 */
public static
MethodHandle catchException(MethodHandle target,
                            Class<? extends Throwable> exType,
                            MethodHandle handler) {
    MethodType ttype = target.type();
    MethodType htype = handler.type();
    if (htype.parameterCount() < 1 ||
        !htype.parameterType(0).isAssignableFrom(exType))
        throw newIllegalArgumentException("handler does not accept exception type "+exType);
    if (htype.returnType() != ttype.returnType())
        throw misMatchedTypes("target and handler return types", ttype, htype);
    List<Class<?>> targs = ttype.parameterList();
    List<Class<?>> hargs = htype.parameterList();
    hargs = hargs.subList(1, hargs.size());  // 从处理程序中省略前导参数
    if (!targs.equals(hargs)) {
        int hpc = hargs.size(), tpc = targs.size();
        if (hpc >= tpc || !targs.subList(0, hpc).equals(hargs))
            throw misMatchedTypes("target and handler types", ttype, htype);
        handler = dropArguments(handler, 1+hpc, targs.subList(hpc, tpc));
        htype = handler.type();
    }
    return MethodHandleImpl.makeGuardWithCatch(target, exType, handler);
}

                /**
     * 生成一个方法句柄，该方法句柄将抛出给定的 {@code exType} 类型的异常。
     * 方法句柄将接受一个 {@code exType} 类型的单个参数，
     * 并立即将其作为异常抛出。
     * 方法类型将名义上指定一个 {@code returnType} 类型的返回值。
     * 返回类型可以是任何方便的类型：它对方法句柄的行为没有影响，因为方法句柄永远不会正常返回。
     * @param returnType 所需方法句柄的返回类型
     * @param exType 所需方法句柄的参数类型
     * @return 可以抛出给定异常的方法句柄
     * @throws NullPointerException 如果任一参数为 null
     */
    public static
    MethodHandle throwException(Class<?> returnType, Class<? extends Throwable> exType) {
        if (!Throwable.class.isAssignableFrom(exType))
            throw new ClassCastException(exType.getName());
        return MethodHandleImpl.throwException(MethodType.methodType(returnType, exType));
    }
}
