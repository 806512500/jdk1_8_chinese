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
 * PersistenceDelegate 类负责以给定类的公共 API 中的方法来表示该类的实例状态。与将持久化责任与类本身关联的做法不同，例如通过
 * <code>readObject</code> 和 <code>writeObject</code> 方法由 <code>ObjectOutputStream</code> 使用，像 <code>XMLEncoder</code>
 * 这样的流使用这种委托模型可以独立于类本身控制其行为。通常，类是放置此类信息的最佳位置，可以通过这种委托方案轻松表达这些约定。
 * 但是，有时单个类中的小问题可能会阻止整个对象图的写入，这可能会使应用程序开发人员别无选择，只能尝试在本地影子化问题类或使用替代的持久化技术。
 * 在这种情况下，委托模型为应用程序开发人员提供了一种相对干净的机制，可以在不修改不属于应用程序的类的实现的情况下干预序列化过程的所有部分。
 * <p>
 * 除了使用委托模型外，这种持久化方案还要求有一个 <code>writeObject</code> 方法的类似物，但没有相应的 <code>readObject</code> 方法。
 * <code>writeObject</code> 类似物根据其公共 API 编码每个实例，因此不需要定义 <code>readObject</code> 类似物，
 * 因为序列化形式的读取过程由 Java 语言规范中定义的方法调用语义决定。
 * 通过打破 <code>writeObject</code> 和 <code>readObject</code> 实现之间的依赖关系，这些实现可能会随版本变化，
 * 这种技术生成的归档文件可以免疫其所引用类的私有实现的变化。
 * <p>
 * 持久化委托可以控制对象持久化的所有方面，包括：
 * <ul>
 * <li>
 * 决定一个实例是否可以被变异为同一类的另一个实例。
 * <li>
 * 通过调用公共构造函数或公共工厂方法来实例化对象。
 * <li>
 * 执行对象的初始化。
 * </ul>
 * @see XMLEncoder
 *
 * @since 1.4
 *
 * @author Philip Milne
 */

public abstract class PersistenceDelegate {

    /**
     * <code>writeObject</code> 是持久化的单一入口点，由 <code>Encoder</code> 在传统的委托模式中使用。
     * 虽然此方法不是最终的，但在正常情况下通常不需要子类化。
     * <p>
     * 此实现首先检查流是否已经遇到该对象。接下来调用 <code>mutatesTo</code> 方法以查看从流中返回的候选对象
     * 是否可以被变异为 <code>oldInstance</code> 的准确副本。如果可以，调用 <code>initialize</code> 方法执行初始化。
     * 如果不可以，从流中移除候选对象，并调用 <code>instantiate</code> 方法创建此对象的新候选对象。
     *
     * @param oldInstance 将由该表达式创建的实例。
     * @param out 该表达式将写入的流。
     *
     * @throws NullPointerException 如果 {@code out} 为 {@code null}
     */
    public void writeObject(Object oldInstance, Encoder out) {
        Object newInstance = out.get(oldInstance);
        if (!mutatesTo(oldInstance, newInstance)) {
            out.remove(oldInstance);
            out.writeExpression(instantiate(oldInstance, out));
        }
        else {
            initialize(oldInstance.getClass(), oldInstance, newInstance, out);
        }
    }

    /**
     * 如果可以通过对 <code>newInstance</code> 应用一系列语句来创建 <code>oldInstance</code> 的 <em>等效</em> 副本，则返回 true。
     * 在此方法的规范中，我们所说的等效是指修改后的实例在相关方法的公共 API 行为上与 <code>oldInstance</code> 无法区分。
     * [注意：我们使用 <em>相关</em> 方法而不是 <em>所有</em> 方法，只是因为严格来说，像 <code>hashCode</code>
     * 和 <code>toString</code> 这样的方法会阻止大多数类生成其实例的真正无法区分的副本]。
     * <p>
     * 默认行为是在两个实例的类相同时返回 <code>true</code>。
     *
     * @param oldInstance 要复制的实例。
     * @param newInstance 要修改的实例。
     * @return 如果可以通过对 <code>oldInstance</code> 应用一系列变异来创建 <code>newInstance</code> 的等效副本，则返回 true。
     */
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return (newInstance != null && oldInstance != null &&
                oldInstance.getClass() == newInstance.getClass());
    }

    /**
     * 返回一个值为 <code>oldInstance</code> 的表达式。此方法用于描述应使用哪个构造函数或工厂方法来创建给定对象。
     * 例如，<code>Field</code> 类的持久化委托的 <code>instantiate</code> 方法可以定义如下：
     * <pre>
     * Field f = (Field)oldInstance;
     * return new Expression(f, f.getDeclaringClass(), "getField", new Object[]{f.getName()});
     * </pre>
     * 注意，我们声明返回表达式的值，以便表达式的值（由 <code>getValue</code> 返回）与 <code>oldInstance</code> 相同。
     *
     * @param oldInstance 将由该表达式创建的实例。
     * @param out 该表达式将写入的流。
     * @return 一个值为 <code>oldInstance</code> 的表达式。
     *
     * @throws NullPointerException 如果 {@code out} 为 {@code null} 且此值在方法中使用
     */
    protected abstract Expression instantiate(Object oldInstance, Encoder out);

    /**
     * 生成一系列对 <code>newInstance</code> 有副作用的语句，使新实例变得 <em>等效</em> 于 <code>oldInstance</code>。
     * 在此方法的规范中，我们所说的等效是指在返回后，修改后的实例在所有方法的公共 API 行为上与 <code>newInstance</code> 无法区分。
     * <p>
     * 实现通常通过生成涉及 <code>oldInstance</code> 及其公共可用状态的“发生了什么”语句来实现这一目标。这些语句使用流的
     * <code>writeExpression</code> 方法发送到输出流，该方法返回一个涉及克隆环境中元素的表达式，这些元素模拟了读取过程中输入流的状态。
     * 返回的每个语句都将旧环境中所有实例替换为新环境中的对象。特别是，这些语句的目标引用，最初是 <code>oldInstance</code> 的引用，
     * 将被替换为 <code>newInstance</code> 的引用。执行这些语句会逐步对新环境中的对象进行修改，使两个对象的状态逐渐对齐。
     * 当 <code>initialize</code> 方法返回时，应该无法通过使用公共 API 区分这两个实例。最重要的是，用于使这些对象看起来等效的步骤序列
     * 将被输出流记录下来，并在流刷新时形成实际输出。
     * <p>
     * 默认实现调用类型的超类的 <code>initialize</code> 方法。
     *
     * @param type 实例的类型
     * @param oldInstance 要复制的实例。
     * @param newInstance 要修改的实例。
     * @param out 任何初始化语句应写入的流。
     *
     * @throws NullPointerException 如果 {@code out} 为 {@code null}
     */
    protected void initialize(Class<?> type,
                              Object oldInstance, Object newInstance,
                              Encoder out)
    {
        Class<?> superType = type.getSuperclass();
        PersistenceDelegate info = out.getPersistenceDelegate(superType);
        info.initialize(superType, oldInstance, newInstance, out);
    }
}
