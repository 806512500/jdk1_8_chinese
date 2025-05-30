
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

import java.util.*;
import java.lang.reflect.*;
import java.util.Objects;
import sun.reflect.misc.*;


/**
 * <code>DefaultPersistenceDelegate</code> 是抽象类 <code>PersistenceDelegate</code> 的具体实现，
 * 并且是用于那些没有可用信息的类的默认委托。<code>DefaultPersistenceDelegate</code>
 * 提供了版本弹性的、基于公共 API 的持久化，适用于遵循 JavaBeans™ 规范的类，无需任何特定类的配置。
 * <p>
 * 关键假设是该类具有无参构造函数，并且其状态可以通过 Introspector 返回的顺序匹配的“setter”和“getter”方法对准确表示。
 * 除了为 JavaBeans 提供无代码持久化外，<code>DefaultPersistenceDelegate</code> 还提供了一种方便的手段，
 * 用于处理具有构造函数的类的持久化存储，这些构造函数虽然不是无参的，但只需要一些属性值作为参数。
 *
 * @see #DefaultPersistenceDelegate(String[])
 * @see java.beans.Introspector
 *
 * @since 1.4
 *
 * @author Philip Milne
 */

public class DefaultPersistenceDelegate extends PersistenceDelegate {
    private static final String[] EMPTY = {};
    private final String[] constructor;
    private Boolean definesEquals;

    /**
     * 创建一个具有无参构造函数的类的持久化委托。
     *
     * @see #DefaultPersistenceDelegate(java.lang.String[])
     */
    public DefaultPersistenceDelegate() {
        this.constructor = EMPTY;
    }

    /**
     * 创建一个具有指定属性名称作为构造函数参数的类的默认持久化委托。
     * 构造函数参数是通过按顺序评估属性名称创建的。
     * 要使用此类为特定类型指定一个首选构造函数以用于序列化，我们声明构成构造函数参数的属性名称。
     * 例如，<code>Font</code> 类没有定义无参构造函数，可以使用以下持久化委托处理：
     *
     * <pre>
     *     new DefaultPersistenceDelegate(new String[]{"name", "style", "size"});
     * </pre>
     *
     * @param  constructorPropertyNames 构造函数参数的属性名称。
     *
     * @see #instantiate
     */
    public DefaultPersistenceDelegate(String[] constructorPropertyNames) {
        this.constructor = (constructorPropertyNames == null) ? EMPTY : constructorPropertyNames.clone();
    }

    private static boolean definesEquals(Class<?> type) {
        try {
            return type == type.getMethod("equals", Object.class).getDeclaringClass();
        }
        catch(NoSuchMethodException e) {
            return false;
        }
    }

    private boolean definesEquals(Object instance) {
        if (definesEquals != null) {
            return (definesEquals == Boolean.TRUE);
        }
        else {
            boolean result = definesEquals(instance.getClass());
            definesEquals = result ? Boolean.TRUE : Boolean.FALSE;
            return result;
        }
    }

    /**
     * 如果指定构造函数的参数数量不为零，并且 <code>oldInstance</code> 类显式声明了“equals”方法，
     * 则此方法返回 <code>oldInstance.equals(newInstance)</code> 的值。
     * 否则，此方法使用超类的定义，如果两个实例的类相等，则返回 true。
     *
     * @param oldInstance 要复制的实例。
     * @param newInstance 要修改的实例。
     * @return 如果可以通过对 <code>oldInstance</code> 应用一系列变异来创建 <code>newInstance</code> 的等效副本，则返回 true。
     *
     * @see #DefaultPersistenceDelegate(String[])
     */
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        // 假设如果它具有无参构造函数，则实例是可变的或单例的。
        return (constructor.length == 0) || !definesEquals(oldInstance) ?
            super.mutatesTo(oldInstance, newInstance) :
            oldInstance.equals(newInstance);
    }

    /**
     * 此 <code>instantiate</code> 方法的默认实现返回一个包含预定义方法名“new”的表达式，
     * 表示调用 <code>DefaultPersistenceDelegate</code> 构造函数中指定的构造函数。
     *
     * @param  oldInstance 要实例化的实例。
     * @param  out 代码输出流。
     * @return 值为 <code>oldInstance</code> 的表达式。
     *
     * @throws NullPointerException 如果 {@code out} 为 {@code null} 并且此值在方法中使用，则抛出此异常。
     *
     * @see #DefaultPersistenceDelegate(String[])
     */
    protected Expression instantiate(Object oldInstance, Encoder out) {
        int nArgs = constructor.length;
        Class<?> type = oldInstance.getClass();
        Object[] constructorArgs = new Object[nArgs];
        for(int i = 0; i < nArgs; i++) {
            try {
                Method method = findMethod(type, this.constructor[i]);
                constructorArgs[i] = MethodUtil.invoke(method, oldInstance, new Object[0]);
            }
            catch (Exception e) {
                out.getExceptionListener().exceptionThrown(e);
            }
        }
        return new Expression(oldInstance, oldInstance.getClass(), "new", constructorArgs);
    }

    private Method findMethod(Class<?> type, String property) {
        if (property == null) {
            throw new IllegalArgumentException("属性名称为 null");
        }
        PropertyDescriptor pd = getPropertyDescriptor(type, property);
        if (pd == null) {
            throw new IllegalStateException("找不到名为 " + property + " 的属性");
        }
        Method method = pd.getReadMethod();
        if (method == null) {
            throw new IllegalStateException("找不到属性 " + property + " 的 getter");
        }
        return method;
    }

    private void doProperty(Class<?> type, PropertyDescriptor pd, Object oldInstance, Object newInstance, Encoder out) throws Exception {
        Method getter = pd.getReadMethod();
        Method setter = pd.getWriteMethod();

        if (getter != null && setter != null) {
            Expression oldGetExp = new Expression(oldInstance, getter.getName(), new Object[]{});
            Expression newGetExp = new Expression(newInstance, getter.getName(), new Object[]{});
            Object oldValue = oldGetExp.getValue();
            Object newValue = newGetExp.getValue();
            out.writeExpression(oldGetExp);
            if (!Objects.equals(newValue, out.get(oldValue))) {
                // 搜索具有此值的静态常量；
                Object e = (Object[])pd.getValue("enumerationValues");
                if (e instanceof Object[] && Array.getLength(e) % 3 == 0) {
                    Object[] a = (Object[])e;
                    for(int i = 0; i < a.length; i = i + 3) {
                        try {
                           Field f = type.getField((String)a[i]);
                           if (f.get(null).equals(oldValue)) {
                               out.remove(oldValue);
                               out.writeExpression(new Expression(oldValue, f, "get", new Object[]{null}));
                           }
                        }
                        catch (Exception ex) {}
                    }
                }
                invokeStatement(oldInstance, setter.getName(), new Object[]{oldValue}, out);
            }
        }
    }

    static void invokeStatement(Object instance, String methodName, Object[] args, Encoder out) {
        out.writeStatement(new Statement(instance, methodName, args));
    }

    // 写出此实例的属性。
    private void initBean(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        for (Field field : type.getFields()) {
            if (!ReflectUtil.isPackageAccessible(field.getDeclaringClass())) {
                continue;
            }
            int mod = field.getModifiers();
            if (Modifier.isFinal(mod) || Modifier.isStatic(mod) || Modifier.isTransient(mod)) {
                continue;
            }
            try {
                Expression oldGetExp = new Expression(field, "get", new Object[] { oldInstance });
                Expression newGetExp = new Expression(field, "get", new Object[] { newInstance });
                Object oldValue = oldGetExp.getValue();
                Object newValue = newGetExp.getValue();
                out.writeExpression(oldGetExp);
                if (!Objects.equals(newValue, out.get(oldValue))) {
                    out.writeStatement(new Statement(field, "set", new Object[] { oldInstance, oldValue }));
                }
            }
            catch (Exception exception) {
                out.getExceptionListener().exceptionThrown(exception);
            }
        }
        BeanInfo info;
        try {
            info = Introspector.getBeanInfo(type);
        } catch (IntrospectionException exception) {
            return;
        }
        // 属性
        for (PropertyDescriptor d : info.getPropertyDescriptors()) {
            if (d.isTransient()) {
                continue;
            }
            try {
                doProperty(type, d, oldInstance, newInstance, out);
            }
            catch (Exception e) {
                out.getExceptionListener().exceptionThrown(e);
            }
        }

        // 监听器
        /*
        待解决（milne）。截至 1.4，关于监听器的归档存在一个未解决的通用问题。许多将一个对象安装到另一个对象中的方法（通常是“add”方法或 setter）
        会自动在“子”对象上安装一个监听器，以便其“父”对象可以响应对其所做的更改。例如，JTable:setModel() 方法会自动将一个 TableModelListener
        （在这种情况下是 JTable 本身）添加到提供的表格模型中。

        在归档中，我们不需要显式地将这些监听器添加到模型中，因为它们将由 JTable 的“setModel”方法自动添加。在某些情况下，我们必须特别避免尝试这样做，
        因为监听器可能是无法使用公共 API 实例化的内部类。

        目前没有通用的机制来区分这些监听器和用户显式添加的监听器。必须创建一种机制来提供一种通用手段，以区分这些特殊情况，从而为通用情况提供可靠的监听器持久化。
        */
        if (!java.awt.Component.class.isAssignableFrom(type)) {
            return; // 目前仅处理 Component 的监听器。
        }
        for (EventSetDescriptor d : info.getEventSetDescriptors()) {
            if (d.isTransient()) {
                continue;
            }
            Class<?> listenerType = d.getListenerType();


            // 当调用父级的 Container:add 时，ComponentListener 会自动添加。
            if (listenerType == java.awt.event.ComponentListener.class) {
                continue;
            }

            // JMenuItems 在其“add”方法中为其添加了一个 ChangeListener 以支持可访问性 -
            // 有关详细信息，请参阅 JMenuItem 的 add 方法。我们无法实例化此实例，因为它是一个私有内部类，
            // 并且无论如何也不需要这样做，因为它将由“add”方法创建并安装。目前特殊情况处理，忽略 JMenuItems 上的所有 ChangeListener。
            if (listenerType == javax.swing.event.ChangeListener.class &&
                type == javax.swing.JMenuItem.class) {
                continue;
            }

            EventListener[] oldL = new EventListener[0];
            EventListener[] newL = new EventListener[0];
            try {
                Method m = d.getGetListenerMethod();
                oldL = (EventListener[])MethodUtil.invoke(m, oldInstance, new Object[]{});
                newL = (EventListener[])MethodUtil.invoke(m, newInstance, new Object[]{});
            }
            catch (Exception e2) {
                try {
                    Method m = type.getMethod("getListeners", new Class<?>[]{Class.class});
                    oldL = (EventListener[])MethodUtil.invoke(m, oldInstance, new Object[]{listenerType});
                    newL = (EventListener[])MethodUtil.invoke(m, newInstance, new Object[]{listenerType});
                }
                catch (Exception e3) {
                    return;
                }
            }

            // 假设监听器按相同顺序排列且没有空隙。
            // 最终可能需要进行真正的差异处理。
            String addListenerMethodName = d.getAddListenerMethod().getName();
            for (int i = newL.length; i < oldL.length; i++) {
                // System.out.println("Adding listener: " + addListenerMethodName + oldL[i]);
                invokeStatement(oldInstance, addListenerMethodName, new Object[]{oldL[i]}, out);
            }

            String removeListenerMethodName = d.getRemoveListenerMethod().getName();
            for (int i = oldL.length; i < newL.length; i++) {
                invokeStatement(oldInstance, removeListenerMethodName, new Object[]{newL[i]}, out);
            }
        }
    }


                /**
     * 此 <code>initialize</code> 方法的默认实现假设此类对象持有的所有状态都通过
     * Introspector 返回的匹配的“setter”和“getter”方法对来暴露。如果属性描述符
     * 定义了一个“transient”属性，其值等于 <code>Boolean.TRUE</code>，则该属性
     * 被此默认实现忽略。请注意，这里使用的“transient”一词与 <code>ObjectOutputStream</code>
     * 使用的字段修饰符完全独立。
     * <p>
     * 对于每个非瞬态属性，都会创建一个表达式，其中将零参数的“getter”方法应用于
     * <code>oldInstance</code>。此表达式的值是正在序列化的实例中的属性值。如果此表达式
     * 在克隆环境中 <code>mutatesTo</code> 目标值，则新值将被初始化以使其
     * 等同于旧值。在这种情况下，由于属性值没有改变，因此无需调用相应的“setter”方法，
     * 也不会生成任何语句。否则，此值的表达式将被另一个表达式（通常是构造函数）替换，
     * 并调用相应的“setter”方法以在对象中安装新的属性值。此方案从使用此代理的流
     * 生成的输出中删除默认信息。
     * <p>
     * 在将这些语句传递给输出流时，它们将在其中执行，从而对 <code>newInstance</code>
     * 产生副作用。在大多数情况下，这允许属性值相互依赖的问题实际上有助于
     * 序列化过程，通过减少需要写入输出的语句数量。一般来说，处理相互依赖属性的问题
     * 被简化为在类中找到属性的顺序，使得没有属性值依赖于后续属性的值。
     *
     * @param type 实例的类型
     * @param oldInstance 要复制的实例。
     * @param newInstance 要修改的实例。
     * @param out 应该写入任何初始化语句的流。
     *
     * @throws NullPointerException 如果 {@code out} 为 {@code null}
     *
     * @see java.beans.Introspector#getBeanInfo
     * @see java.beans.PropertyDescriptor
     */
    protected void initialize(Class<?> type,
                              Object oldInstance, Object newInstance,
                              Encoder out)
    {
        // System.out.println("DefulatPD:initialize" + type);
        super.initialize(type, oldInstance, newInstance, out);
        if (oldInstance.getClass() == type) { // !type.isInterface()) {
            initBean(type, oldInstance, newInstance, out);
        }
    }

    private static PropertyDescriptor getPropertyDescriptor(Class<?> type, String property) {
        try {
            for (PropertyDescriptor pd : Introspector.getBeanInfo(type).getPropertyDescriptors()) {
                if (property.equals(pd.getName()))
                    return pd;
            }
        } catch (IntrospectionException exception) {
        }
        return null;
    }
}
