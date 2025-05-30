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

import java.lang.reflect.*;
import java.util.*;
import java.lang.invoke.MethodHandleNatives.Constants;
import java.lang.invoke.MethodHandles.Lookup;
import static java.lang.invoke.MethodHandleStatics.*;

/**
 * 通过分解直接方法句柄获得的符号引用。
 * 要分解直接方法句柄，调用 {@link Lookup#revealDirect Lookup.revealDirect}。
 * <h1><a name="directmh"></a>直接方法句柄</h1>
 * <em>直接方法句柄</em>表示一个方法、构造器或字段，没有任何中间参数绑定或其他转换。
 * 直接方法句柄所引用的方法、构造器或字段称为其<em>底层成员</em>。
 * 可以通过以下方式获得直接方法句柄：
 * <ul>
 * <li>通过执行 {@code ldc} 指令对 {@code CONSTANT_MethodHandle} 常量进行操作。
 *     （参见 Java 虚拟机规范，第 4.4.8 和 5.4.3 节。）
 * <li>通过调用 <a href="MethodHandles.Lookup.html#lookups">查找工厂方法</a>，
 *     如 {@link Lookup#findVirtual Lookup.findVirtual}，
 *     将符号引用解析为方法句柄。
 *     符号引用由类、名称字符串和类型组成。
 * <li>通过调用工厂方法 {@link Lookup#unreflect Lookup.unreflect}
 *     或 {@link Lookup#unreflectSpecial Lookup.unreflectSpecial}
 *     将 {@link Method} 转换为方法句柄。
 * <li>通过调用工厂方法 {@link Lookup#unreflectConstructor Lookup.unreflectConstructor}
 *     将 {@link Constructor} 转换为方法句柄。
 * <li>通过调用工厂方法 {@link Lookup#unreflectGetter Lookup.unreflectGetter}
 *     或 {@link Lookup#unreflectSetter Lookup.unreflectSetter}
 *     将 {@link Field} 转换为方法句柄。
 * </ul>
 *
 * <h1>分解限制</h1>
 * 给定一个合适的 {@code Lookup} 对象，可以分解任何直接方法句柄以恢复底层方法、构造器或字段的符号引用。
 * 分解必须通过与创建目标方法句柄的 {@code Lookup} 对象等效的 {@code Lookup} 对象进行，
 * 或者具有足够的访问权限以重新创建等效的方法句柄。
 * <p>
 * 如果底层方法是 <a href="MethodHandles.Lookup.html#callsens">调用者敏感的</a>，
 * 直接方法句柄将被“绑定”到特定的调用者类，即用于创建它的查找对象的
 * {@linkplain java.lang.invoke.MethodHandles.Lookup#lookupClass() 查找类}。
 * 使用不同的查找类分解此方法句柄将失败，
 * 即使底层方法是公共的（如 {@code Class.forName}）。
 * <p>
 * 查找对象匹配的要求为程序提供了“快速失败”行为，
 * 否则程序可能会信任错误地揭示带有符号信息（或调用者绑定）的方法句柄。
 * 使用 {@link java.lang.invoke.MethodHandles#reflectAs} 覆盖此限制。
 *
 * <h1><a name="refkinds"></a>引用类型</h1>
 * <a href="MethodHandles.Lookup.html#lookups">查找工厂方法</a>
 * 对应于方法、构造器和字段的所有主要用例。
 * 这些用例可以使用小整数进行区分，如下所示：
 * <table border=1 cellpadding=5 summary="引用类型">
 * <tr><th>引用类型</th><th>描述性名称</th><th>范围</th><th>成员</th><th>行为</th></tr>
 * <tr>
 *     <td>{@code 1}</td><td>{@code REF_getField}</td><td>{@code class}</td>
 *     <td>{@code FT f;}</td><td>{@code (T) this.f;}</td>
 * </tr>
 * <tr>
 *     <td>{@code 2}</td><td>{@code REF_getStatic}</td><td>{@code class} 或 {@code interface}</td>
 *     <td>{@code static}<br>{@code FT f;}</td><td>{@code (T) C.f;}</td>
 * </tr>
 * <tr>
 *     <td>{@code 3}</td><td>{@code REF_putField}</td><td>{@code class}</td>
 *     <td>{@code FT f;}</td><td>{@code this.f = x;}</td>
 * </tr>
 * <tr>
 *     <td>{@code 4}</td><td>{@code REF_putStatic}</td><td>{@code class}</td>
 *     <td>{@code static}<br>{@code FT f;}</td><td>{@code C.f = arg;}</td>
 * </tr>
 * <tr>
 *     <td>{@code 5}</td><td>{@code REF_invokeVirtual}</td><td>{@code class}</td>
 *     <td>{@code T m(A*);}</td><td>{@code (T) this.m(arg*);}</td>
 * </tr>
 * <tr>
 *     <td>{@code 6}</td><td>{@code REF_invokeStatic}</td><td>{@code class} 或 {@code interface}</td>
 *     <td>{@code static}<br>{@code T m(A*);}</td><td>{@code (T) C.m(arg*);}</td>
 * </tr>
 * <tr>
 *     <td>{@code 7}</td><td>{@code REF_invokeSpecial}</td><td>{@code class} 或 {@code interface}</td>
 *     <td>{@code T m(A*);}</td><td>{@code (T) super.m(arg*);}</td>
 * </tr>
 * <tr>
 *     <td>{@code 8}</td><td>{@code REF_newInvokeSpecial}</td><td>{@code class}</td>
 *     <td>{@code C(A*);}</td><td>{@code new C(arg*);}</td>
 * </tr>
 * <tr>
 *     <td>{@code 9}</td><td>{@code REF_invokeInterface}</td><td>{@code interface}</td>
 *     <td>{@code T m(A*);}</td><td>{@code (T) this.m(arg*);}</td>
 * </tr>
 * </table>
 * @since 1.8
 */
public
interface MethodHandleInfo {
    /**
     * 直接方法句柄引用类型，
     * 如上表<a href="MethodHandleInfo.html#refkinds">定义</a>。
     */
    public static final int
        REF_getField                = Constants.REF_getField,
        REF_getStatic               = Constants.REF_getStatic,
        REF_putField                = Constants.REF_putField,
        REF_putStatic               = Constants.REF_putStatic,
        REF_invokeVirtual           = Constants.REF_invokeVirtual,
        REF_invokeStatic            = Constants.REF_invokeStatic,
        REF_invokeSpecial           = Constants.REF_invokeSpecial,
        REF_newInvokeSpecial        = Constants.REF_newInvokeSpecial,
        REF_invokeInterface         = Constants.REF_invokeInterface;

    /**
     * 返回分解方法句柄的引用类型，从而确定方法句柄的底层成员是构造器、方法还是字段。
     * 请参见上表<a href="MethodHandleInfo.html#refkinds">定义</a>。
     * @return 用于访问底层成员的引用类型的整数代码
     */
    public int getReferenceKind();

    /**
     * 返回分解方法句柄的底层成员定义的类。
     * @return 底层成员的声明类
     */
    public Class<?> getDeclaringClass();

    /**
     * 返回分解方法句柄的底层成员的名称。
     * 如果底层成员是构造器，则名称为 {@code "<init>"}，
     * 否则为简单的方法名称或字段名称。
     * @return 底层成员的简单名称
     */
    public String getName();

    /**
     * 返回分解符号引用的名义类型，表示为方法类型。
     * 如果引用是构造器，返回类型将是 {@code void}。
     * 如果是实例方法，方法类型将不包含 {@code this} 参数。
     * 如果是字段且请求的访问是读取字段，
     * 方法类型将没有参数且返回字段类型。
     * 如果是字段且请求的访问是写入字段，
     * 方法类型将有一个字段类型的参数且返回 {@code void}。
     * <p>
     * 请注意，原始直接方法句柄可能包含一个前导的 {@code this} 参数，
     * 或者（在构造器的情况下）将 {@code void} 返回类型替换为构造的类。
     * 名义类型不包含任何 {@code this} 参数，
     * 且（在构造器的情况下）将返回 {@code void}。
     * @return 底层成员的类型，表示为方法类型
     */
    public MethodType getMethodType();

    // 实用方法。
    // 注意：类/名称/类型和引用类型构成符号引用
    // 成员和修饰符是附加的，从核心反射（或等效物）派生

    /**
     * 将底层成员反射为方法、构造器或字段对象。
     * 如果底层成员是公共的，则反射方式为
     * {@code getMethod}、{@code getConstructor} 或 {@code getField}。
     * 否则，反射方式为
     * {@code getDeclaredMethod}、{@code getDeclaredConstructor} 或 {@code getDeclaredField}。
     * 底层成员必须对给定的查找对象可访问。
     * @param <T> 结果的期望类型，可以是 {@link Member} 或其子类型
     * @param expected 代表期望结果类型 {@code T} 的类对象
     * @param lookup 创建此 MethodHandleInfo 的查找对象，或具有等效访问权限的查找对象
     * @return 对方法、构造器或字段对象的引用
     * @exception ClassCastException 如果成员不是期望的类型
     * @exception NullPointerException 如果任一参数为 {@code null}
     * @exception IllegalArgumentException 如果底层成员对给定的查找对象不可访问
     */
    public <T extends Member> T reflectAs(Class<T> expected, Lookup lookup);

    /**
     * 返回底层成员的访问修饰符。
     * @return 底层成员的 Java 语言修饰符，
     *         或 -1 如果成员不可访问
     * @see Modifier
     * @see #reflectAs
     */
    public int getModifiers();

    /**
     * 确定底层成员是否为可变参数方法或构造器。
     * 这样的成员由可变参数收集器方法句柄表示。
     * @implSpec
     * 这产生与以下等效的结果：
     * <pre>{@code
     *     getReferenceKind() >= REF_invokeVirtual && Modifier.isTransient(getModifiers())
     * }</pre>
     *
     *
     * @return 如果且仅当底层成员被声明为可变参数时返回 {@code true}。
     */
    // 拼写源自 java.lang.reflect.Executable，而非 MethodHandle.isVarargsCollector
    public default boolean isVarArgs()  {
        // 字段永远不会是可变参数：
        if (MethodHandleNatives.refKindIsField((byte) getReferenceKind()))
            return false;
        // 不在公共 API 中：Modifier.VARARGS
        final int ACC_VARARGS = 0x00000080;  // 从 JVMS 4.6 (Table 4.20)
        assert(ACC_VARARGS == Modifier.TRANSIENT);
        return Modifier.isTransient(getModifiers());
    }

    /**
     * 返回给定引用类型的描述性名称，
     * 如上表<a href="MethodHandleInfo.html#refkinds">定义</a>。
     * 常规前缀 "REF_" 被省略。
     * @param referenceKind 用于访问类成员的引用类型的整数代码
     * @return 一个混合大小写的字符串，如 {@code "getField"}
     * @exception IllegalArgumentException 如果参数不是有效的
     *            <a href="MethodHandleInfo.html#refkinds">引用类型编号</a>
     */
    public static String referenceKindToString(int referenceKind) {
        if (!MethodHandleNatives.refKindIsValid(referenceKind))
            throw newIllegalArgumentException("invalid reference kind", referenceKind);
        return MethodHandleNatives.refKindName((byte)referenceKind);
    }

    /**
     * 返回 {@code MethodHandleInfo} 的字符串表示形式，
     * 给定其符号引用的四个部分。
     * 定义为形式 {@code "RK C.N:MT"}，其中 {@code RK} 是
     * {@linkplain #referenceKindToString 引用类型字符串} 对于 {@code kind}，
     * {@code C} 是 {@linkplain java.lang.Class#getName 名称} 对于 {@code defc}
     * {@code N} 是 {@code name}，且
     * {@code MT} 是 {@code type}。
     * 这四个值可以从
     * {@linkplain #getReferenceKind 引用类型}，
     * {@linkplain #getDeclaringClass 声明类}，
     * {@linkplain #getName 成员名称}，
     * 和 {@linkplain #getMethodType 方法类型}
     * 的 {@code MethodHandleInfo} 对象中获得。
     *
     * @implSpec
     * 这产生与以下等效的结果：
     * <pre>{@code
     *     String.format("%s %s.%s:%s", referenceKindToString(kind), defc.getName(), name, type)
     * }</pre>
     *
     * @param kind 符号引用的 {@linkplain #getReferenceKind 引用类型} 部分
     * @param defc 符号引用的 {@linkplain #getDeclaringClass 声明类} 部分
     * @param name 符号引用的 {@linkplain #getName 成员名称} 部分
     * @param type 符号引用的 {@linkplain #getMethodType 方法类型} 部分
     * @return 形式为 {@code "RK C.N:MT"} 的字符串
     * @exception IllegalArgumentException 如果第一个参数不是有效的
     *            <a href="MethodHandleInfo.html#refkinds">引用类型编号</a>
     * @exception NullPointerException 如果任何引用参数为 {@code null}
     */
    public static String toString(int kind, Class<?> defc, String name, MethodType type) {
        Objects.requireNonNull(name); Objects.requireNonNull(type);
        return String.format("%s %s.%s:%s", referenceKindToString(kind), defc.getName(), name, type);
    }
}
