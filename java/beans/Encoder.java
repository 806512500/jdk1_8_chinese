/*
 * Copyright (c) 2000, 2011, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.beans.finder.PersistenceDelegateFinder;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * <code>Encoder</code> 是一个可以用于创建文件或流的类，这些文件或流编码了一组 JavaBeans 的状态，使用它们的公共 API。编码器及其持久性代理负责将对象图分解为一系列 <code>Statements</code> 和 <code>Expression</code>，这些可以用于创建对象图。子类通常提供一种使用某种人类可读形式（如 Java 源代码或 XML）表示这些表达式的语法。
 *
 * @since 1.4
 *
 * @author Philip Milne
 */

public class Encoder {
    private final PersistenceDelegateFinder finder = new PersistenceDelegateFinder();
    private Map<Object, Expression> bindings = new IdentityHashMap<>();
    private ExceptionListener exceptionListener;
    boolean executeStatements = true;
    private Map<Object, Object> attributes;

    /**
     * 将指定的对象写入输出流。
     * 序列化形式将表示一系列表达式，这些表达式的综合效果是在读取输入流时创建一个等效的对象。
     * 默认情况下，假设对象是一个具有无参构造函数的 <em>JavaBean</em>，其状态由 Introspector 返回的匹配的“setter”和“getter”方法对定义。
     *
     * @param o 要写入流的对象。
     *
     * @see XMLDecoder#readObject
     */
    protected void writeObject(Object o) {
        if (o == this) {
            return;
        }
        PersistenceDelegate info = getPersistenceDelegate(o == null ? null : o.getClass());
        info.writeObject(o, this);
    }

    /**
     * 将此流的异常处理程序设置为 <code>exceptionListener</code>。
     * 当此流捕获可恢复的异常时，异常处理程序将收到通知。
     *
     * @param exceptionListener 此流的异常处理程序；
     *       如果为 <code>null</code>，则使用默认的异常处理程序。
     *
     * @see #getExceptionListener
     */
    public void setExceptionListener(ExceptionListener exceptionListener) {
        this.exceptionListener = exceptionListener;
    }

    /**
     * 获取此流的异常处理程序。
     *
     * @return 此流的异常处理程序；
     *    如果未显式设置，则返回默认的异常处理程序。
     *
     * @see #setExceptionListener
     */
    public ExceptionListener getExceptionListener() {
        return (exceptionListener != null) ? exceptionListener : Statement.defaultExceptionListener;
    }

    Object getValue(Expression exp) {
        try {
            return (exp == null) ? null : exp.getValue();
        }
        catch (Exception e) {
            getExceptionListener().exceptionThrown(e);
            throw new RuntimeException("failed to evaluate: " + exp.toString());
        }
    }

    /**
     * 返回给定类型的持久性代理。
     * 持久性代理是通过以下规则按顺序计算的：
     * <ol>
     * <li>
     * 如果使用 {@link #setPersistenceDelegate} 方法为给定类型关联了持久性代理，则返回该持久性代理。
     * <li>
     * 然后通过查找由给定类型的完全限定名称和 "PersistenceDelegate" 后缀组成的名称来查找持久性代理。
     * 例如，对于 {@code Bean} 类，持久性代理应命名为 {@code BeanPersistenceDelegate} 并位于同一包中。
     * <pre>
     * public class Bean { ... }
     * public class BeanPersistenceDelegate { ... }</pre>
     * 将返回 {@code Bean} 类的 {@code BeanPersistenceDelegate} 类的实例。
     * <li>
     * 如果类型为 {@code null}，则返回一个用于编码 {@code null} 值的共享内部持久性代理。
     * <li>
     * 如果类型是 {@code enum} 声明，则返回一个用于通过其名称编码此枚举常量的共享内部持久性代理。
     * <li>
     * 如果类型是基本类型或相应的包装器，则返回一个用于编码给定类型值的共享内部持久性代理。
     * <li>
     * 如果类型是数组，则返回一个用于编码适当类型和长度的数组及其每个元素（如同它们是属性）的共享内部持久性代理。
     * <li>
     * 如果类型是代理，则返回一个使用 {@link java.lang.reflect.Proxy#newProxyInstance} 方法编码代理实例的共享内部持久性代理。
     * <li>
     * 如果此类型的 {@link BeanInfo} 有一个定义了 "persistenceDelegate" 属性的 {@link BeanDescriptor}，则返回该命名属性的值。
     * <li>
     * 在所有其他情况下，返回默认的持久性代理。
     * 默认的持久性代理假设类型是一个 <em>JavaBean</em>，这意味着它具有默认构造函数，其状态可以通过 {@link Introspector} 类返回的匹配的“setter”和“getter”方法对来描述。
     * 默认构造函数是具有最多参数且具有 {@link ConstructorProperties} 注解的构造函数。
     * 如果没有构造函数具有 {@code ConstructorProperties} 注解，则使用无参构造函数（没有参数的构造函数）。
     * 例如，在以下代码片段中，将使用 {@code Foo} 类的无参构造函数，而将使用 {@code Bar} 类的两参数构造函数。
     * <pre>
     * public class Foo {
     *     public Foo() { ... }
     *     public Foo(int x) { ... }
     * }
     * public class Bar {
     *     public Bar() { ... }
     *     &#64;ConstructorProperties({"x"})
     *     public Bar(int x) { ... }
     *     &#64;ConstructorProperties({"x", "y"})
     *     public Bar(int x, int y) { ... }
     * }</pre>
     * </ol>
     *
     * @param type  对象的类
     * @return 给定类型的持久性代理
     *
     * @see #setPersistenceDelegate
     * @see java.beans.Introspector#getBeanInfo
     * @see java.beans.BeanInfo#getBeanDescriptor
     */
    public PersistenceDelegate getPersistenceDelegate(Class<?> type) {
        PersistenceDelegate pd = this.finder.find(type);
        if (pd == null) {
            pd = MetaData.getPersistenceDelegate(type);
            if (pd != null) {
                this.finder.register(type, pd);
            }
        }
        return pd;
    }

    /**
     * 将指定的持久性代理与给定类型关联。
     *
     * @param type  指定持久性代理适用的对象的类
     * @param delegate  给定类型的实例的持久性代理
     *
     * @see #getPersistenceDelegate
     * @see java.beans.Introspector#getBeanInfo
     * @see java.beans.BeanInfo#getBeanDescriptor
     */
    public void setPersistenceDelegate(Class<?> type, PersistenceDelegate delegate) {
        this.finder.register(type, delegate);
    }

    /**
     * 移除此实例的条目，返回旧条目。
     *
     * @param oldInstance 要移除的条目。
     * @return 被移除的条目。
     *
     * @see #get
     */
    public Object remove(Object oldInstance) {
        Expression exp = bindings.remove(oldInstance);
        return getValue(exp);
    }

    /**
     * 返回 <code>oldInstance</code> 在此流创建的环境中的临时值。持久性
     * 代理可以使用其 <code>mutatesTo</code> 方法来
     * 确定此值是否可以初始化以
     * 在输出中形成等效对象，或者是否必须重新实例化新对象。如果
     * 流尚未看到此值，则返回 null。
     *
     * @param  oldInstance 要查找的实例。
     * @return 对象，如果对象之前未见过，则返回 null。
     */
    public Object get(Object oldInstance) {
        if (oldInstance == null || oldInstance == this ||
            oldInstance.getClass() == String.class) {
            return oldInstance;
        }
        Expression exp = bindings.get(oldInstance);
        return getValue(exp);
    }

    private Object writeObject1(Object oldInstance) {
        Object o = get(oldInstance);
        if (o == null) {
            writeObject(oldInstance);
            o = get(oldInstance);
        }
        return o;
    }

    private Statement cloneStatement(Statement oldExp) {
        Object oldTarget = oldExp.getTarget();
        Object newTarget = writeObject1(oldTarget);

        Object[] oldArgs = oldExp.getArguments();
        Object[] newArgs = new Object[oldArgs.length];
        for (int i = 0; i < oldArgs.length; i++) {
            newArgs[i] = writeObject1(oldArgs[i]);
        }
        Statement newExp = Statement.class.equals(oldExp.getClass())
                ? new Statement(newTarget, oldExp.getMethodName(), newArgs)
                : new Expression(newTarget, oldExp.getMethodName(), newArgs);
        newExp.loader = oldExp.loader;
        return newExp;
    }

    /**
     * 将 <code>oldStm</code> 语句写入流。
     * <code>oldStm</code> 应该完全
     * 用调用者的环境表示，即
     * 目标和所有参数应该是
     * 被写入的对象图的一部分。这些表达式
     * 表示一系列“发生了什么”表达式
     * 告诉输出流如何生成一个
     * 类似于原始对象图的对象图。
     * <p>
     * 此方法的实现将生成
     * 一个表示相同表达式的第二个表达式
     * 在流被读取时存在的环境中。
     * 这是通过调用 <code>writeObject</code>
     * 在目标和所有参数上并使用结果构建一个新
     * 表达式来实现的。
     *
     * @param oldStm 要写入流的表达式。
     */
    public void writeStatement(Statement oldStm) {
        // System.out.println("writeStatement: " + oldExp);
        Statement newStm = cloneStatement(oldStm);
        if (oldStm.getTarget() != this && executeStatements) {
            try {
                newStm.execute();
            } catch (Exception e) {
                getExceptionListener().exceptionThrown(new Exception("Encoder: discarding statement "
                                                                     + newStm, e));
            }
        }
    }

    /**
     * 实现首先检查是否已经写入了具有此值的表达式。
     * 如果没有，表达式将被克隆，使用与 <code>writeStatement</code> 相同的过程，
     * 并通过调用 <code>writeObject</code> 将此表达式的值与克隆表达式的值进行协调。
     *
     * @param oldExp 要写入流的表达式。
     */
    public void writeExpression(Expression oldExp) {
        // System.out.println("Encoder::writeExpression: " + oldExp);
        Object oldValue = getValue(oldExp);
        if (get(oldValue) != null) {
            return;
        }
        bindings.put(oldValue, (Expression)cloneStatement(oldExp));
        writeObject(oldValue);
    }

    void clear() {
        bindings.clear();
    }

    // 包私有方法，用于为编码器设置属性表
    void setAttribute(Object key, Object value) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.put(key, value);
    }

    Object getAttribute(Object key) {
        if (attributes == null) {
            return null;
        }
        return attributes.get(key);
    }
}
