/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.beans;

/**
 * 一个 <code>Expression</code> 对象表示一个基本表达式，
 * 其中一个方法应用于目标和一组参数以返回结果 - 例如 <code>"a.getFoo()"</code>。
 * <p>
 * 除了超类的属性外，<code>Expression</code> 对象提供一个 <em>值</em>，
 * 这个值是当此表达式被求值时返回的对象。
 * 返回值通常不由调用者提供，而是通过动态查找方法并在首次调用 <code>getValue</code> 时调用该方法来计算。
 *
 * @see #getValue
 * @see #setValue
 *
 * @since 1.4
 *
 * @author Philip Milne
 */
public class Expression extends Statement {

    private static Object unbound = new Object();

    private Object value = unbound;

    /**
     * 创建一个新的 {@link Expression} 对象
     * 用于指定的目标对象调用由名称和参数数组指定的方法。
     * <p>
     * {@code target} 和 {@code methodName} 的值不应为 {@code null}。
     * 否则尝试执行此 {@code Expression} 将导致 {@code NullPointerException}。
     * 如果 {@code arguments} 的值为 {@code null}，
     * 则使用空数组作为 {@code arguments} 属性的值。
     *
     * @param target  此表达式的目标对象
     * @param methodName  要在指定目标上调用的方法名称
     * @param arguments  要调用指定方法的参数数组
     *
     * @see #getValue
     */
    @ConstructorProperties({"target", "methodName", "arguments"})
    public Expression(Object target, String methodName, Object[] arguments) {
        super(target, methodName, arguments);
    }

    /**
     * 创建一个新的 {@link Expression} 对象，指定值
     * 用于指定的目标对象调用由名称和参数数组指定的方法。
     * {@code value} 值用作 {@code value} 属性的值，
     * 因此 {@link #getValue} 方法将返回它
     * 而不执行此 {@code Expression}。
     * <p>
     * {@code target} 和 {@code methodName} 的值不应为 {@code null}。
     * 否则尝试执行此 {@code Expression} 将导致 {@code NullPointerException}。
     * 如果 {@code arguments} 的值为 {@code null}，
     * 则使用空数组作为 {@code arguments} 属性的值。
     *
     * @param value  此表达式的值
     * @param target  此表达式的目标对象
     * @param methodName  要在指定目标上调用的方法名称
     * @param arguments  要调用指定方法的参数数组
     *
     * @see #setValue
     */
    public Expression(Object value, Object target, String methodName, Object[] arguments) {
        this(target, methodName, arguments);
        setValue(value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 如果调用的方法正常完成，
     * 它返回的值将复制到 {@code value} 属性中。
     * 注意，如果底层方法的返回类型为 {@code void}，
     * 则 {@code value} 属性将被设置为 {@code null}。
     *
     * @throws NullPointerException 如果 {@code target} 或
     *                              {@code methodName} 属性的值为 {@code null}
     * @throws NoSuchMethodException 如果未找到匹配的方法
     * @throws SecurityException 如果存在安全管理者并且
     *                           它拒绝方法调用
     * @throws Exception 由调用的方法抛出的异常
     *
     * @see java.lang.reflect.Method
     * @since 1.7
     */
    @Override
    public void execute() throws Exception {
        setValue(invoke());
    }

    /**
     * 如果此实例的值属性尚未设置，
     * 此方法将动态查找具有指定
     * methodName 的目标上的方法并调用它。
     * 方法调用的结果首先复制
     * 到此表达式的值属性中，然后作为 <code>getValue</code> 的结果返回。
     * 如果值属性已通过调用 <code>setValue</code>
     * 或之前的 <code>getValue</code> 调用设置，
     * 则返回值属性而不查找或调用方法。
     * <p>
     * <code>Expression</code> 的值属性默认设置为
     * 一个唯一的私有（非-{@code null}）值，
     * 该值用作内部指示，表示方法尚未被调用。
     * 返回值为 <code>null</code> 会替换此默认值，
     * 与任何其他值的方式相同，确保表达式不会被多次求值。
     * <p>
     * 有关如何使用目标和参数的动态类型选择方法的详细信息，
     * 请参阅 <code>execute</code> 方法。
     *
     * @see Statement#execute
     * @see #setValue
     *
     * @return 应用此方法到这些参数的结果。
     * @throws Exception 如果具有指定 methodName 的方法
     * 抛出异常
     */
    public Object getValue() throws Exception {
        if (value == unbound) {
            setValue(invoke());
        }
        return value;
    }

    /**
     * 将此表达式的值设置为 <code>value</code>。
     * 此值将由 getValue 方法返回
     * 而不调用与此表达式关联的方法。
     *
     * @param value 此表达式的值。
     *
     * @see #getValue
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /*pp*/ String instanceName(Object instance) {
        return instance == unbound ? "<unbound>" : super.instanceName(instance);
    }

    /**
     * 使用 Java 风格的语法打印此表达式的值。
     */
    public String toString() {
        return instanceName(value) + "=" + super.toString();
    }
}
