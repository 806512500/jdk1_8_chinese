
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;

import sun.reflect.misc.MethodUtil;
import sun.reflect.misc.ReflectUtil;

/**
 * <code>EventHandler</code> 类提供了支持动态生成事件监听器的方法，这些监听器的方法
 * 包含一个涉及传入事件对象和目标对象的简单语句。
 * <p>
 * <code>EventHandler</code> 类旨在用于交互式工具，例如
 * 应用程序构建器，这些工具允许开发人员在
 * beans 之间建立连接。通常，连接是从用户界面 bean
 * （事件 <em>源</em>）
 * 到应用逻辑 bean（<em>目标</em>）。最有效的
 * 这种连接将应用逻辑与用户界面隔离。例如，从
 * <code>JCheckBox</code> 到接受布尔值的方法的
 * <code>EventHandler</code> 可以处理提取复选框的状态
 * 并直接传递给方法，使方法与用户界面层隔离。
 * <p>
 * 内部类是另一种更通用的处理用户界面事件的方法。
 * <code>EventHandler</code> 类
 * 只处理使用内部类可能实现的一部分功能。然而，<code>EventHandler</code>
 * 在长期持久化方案中比内部类表现更好。
 * 此外，在大型应用程序中多次实现同一接口时使用
 * <code>EventHandler</code> 可以减少应用程序的磁盘和内存占用。
 * <p>
 * 使用 <code>EventHandler</code> 创建的监听器
 * 占用空间小的原因是 <code>Proxy</code> 类，
 * <code>EventHandler</code> 依赖于它，共享相同
 * 接口的实现。例如，如果你使用
 * <code>EventHandler</code> 的 <code>create</code> 方法来创建
 * 应用程序中的所有 <code>ActionListener</code>，
 * 那么所有这些动作监听器都将是单个类的实例
 * （由 <code>Proxy</code> 类创建）。
 * 通常，基于
 * <code>Proxy</code> 类的监听器需要为每种 <em>监听器类型</em>（接口）
 * 创建一个监听器类，
 * 而内部类方法则需要为每个 <em>监听器</em>
 * （实现接口的对象）创建一个类。
 *
 * <p>
 * 通常你不会直接处理 <code>EventHandler</code>
 * 实例。
 * 相反，你使用 <code>EventHandler</code> 的
 * <code>create</code> 方法之一来创建
 * 实现给定监听器接口的对象。
 * 这个监听器对象在后台使用 <code>EventHandler</code> 对象
 * 封装有关事件的信息，当事件发生时要发送消息的对象，
 * 要发送的消息（方法）以及方法的任何参数。
 * 以下部分提供了如何使用 <code>create</code> 方法创建监听器对象的示例。
 *
 * <h2>使用 EventHandler 的示例</h2>
 *
 * 使用 <code>EventHandler</code> 最简单的方法是安装
 * 一个监听器，该监听器在目标对象上调用一个没有参数的方法。
 * 在以下示例中，我们创建一个 <code>ActionListener</code>
 * 在 <code>javax.swing.JFrame</code> 的实例上调用 <code>toFront</code> 方法。
 *
 * <blockquote>
 *<pre>
 *myButton.addActionListener(
 *    (ActionListener)EventHandler.create(ActionListener.class, frame, "toFront"));
 *</pre>
 * </blockquote>
 *
 * 当 <code>myButton</code> 被按下时，将执行
 * <code>frame.toFront()</code> 语句。可以通过定义
 * <code>ActionListener</code> 接口的新实现并将其添加到按钮上，
 * 达到相同的效果，但编译时类型安全性更高：
 *
 * <blockquote>
 *<pre>
// 相当于使用内部类而不是 EventHandler 的代码。
 *myButton.addActionListener(new ActionListener() {
 *    public void actionPerformed(ActionEvent e) {
 *        frame.toFront();
 *    }
 *});
 *</pre>
 * </blockquote>
 *
 * 使用 <code>EventHandler</code> 的下一个最简单的方法是从监听器接口方法的
 * 第一个参数（通常是事件对象）中提取属性值
 * 并用它设置目标对象的属性值。
 * 在以下示例中，我们创建一个 <code>ActionListener</code>，
 * 将目标对象（myButton）的 <code>nextFocusableComponent</code> 属性
 * 设置为事件的 "source" 属性的值。
 *
 * <blockquote>
 *<pre>
 *EventHandler.create(ActionListener.class, myButton, "nextFocusableComponent", "source")
 *</pre>
 * </blockquote>
 *
 * 这相当于以下内部类实现：
 *
 * <blockquote>
 *<pre>
// 相当于使用内部类而不是 EventHandler 的代码。
 *new ActionListener() {
 *    public void actionPerformed(ActionEvent e) {
 *        myButton.setNextFocusableComponent((Component)e.getSource());
 *    }
 *}
 *</pre>
 * </blockquote>
 *
 * 也可以创建一个 <code>EventHandler</code>，它只是将传入的事件对象传递给目标的行动。
 * 如果 <code>EventHandler.create</code> 的第四个参数是
 * 空字符串，则事件将直接传递：
 *
 * <blockquote>
 *<pre>
 *EventHandler.create(ActionListener.class, target, "doActionEvent", "")
 *</pre>
 * </blockquote>
 *
 * 这相当于以下内部类实现：
 *
 * <blockquote>
 *<pre>
// 相当于使用内部类而不是 EventHandler 的代码。
 *new ActionListener() {
 *    public void actionPerformed(ActionEvent e) {
 *        target.doActionEvent(e);
 *    }
 *}
 *</pre>
 * </blockquote>
 *
 * 使用 <code>EventHandler</code> 最常见的方法是从
 * 事件对象的 <em>源</em> 中提取属性值，并将此值设置为
 * 目标对象的属性值。
 * 在以下示例中，我们创建一个 <code>ActionListener</code>，
 * 将目标对象的 "label" 属性
 * 设置为事件源（事件的 "source" 属性的值）的 "text" 属性的值。
 *
 * <blockquote>
 *<pre>
 *EventHandler.create(ActionListener.class, myButton, "label", "source.text")
 *</pre>
 * </blockquote>
 *
 * 这相当于以下内部类实现：
 *
 * <blockquote>
 *<pre>
// 相当于使用内部类而不是 EventHandler 的代码。
 *new ActionListener() {
 *    public void actionPerformed(ActionEvent e) {
 *        myButton.setLabel(((JTextField)e.getSource()).getText());
 *    }
 *}
 *</pre>
 * </blockquote>
 *
 * 事件属性可以用任意数量的属性前缀限定，这些前缀用 "." 字符分隔。"限定"
 * 名称出现在 "." 字符之前的名称被视为应应用于
 * 事件对象的属性名称。
 * <p>
 * 例如，以下动作监听器
 *
 * <blockquote>
 *<pre>
 *EventHandler.create(ActionListener.class, target, "a", "b.c.d")
 *</pre>
 * </blockquote>
 *
 * 可能会写成以下内部类
 * （假设所有属性都有规范的 getter 方法并返回适当的类型）：
 *
 * <blockquote>
 *<pre>
// 相当于使用内部类而不是 EventHandler 的代码。
 *new ActionListener() {
 *    public void actionPerformed(ActionEvent e) {
 *        target.setA(e.getB().getC().isD());
 *    }
 *}
 *</pre>
 * </blockquote>
 * 目标属性也可以用任意数量的属性前缀限定，这些前缀用 "." 字符分隔。例如，以下动作监听器：
 * <pre>
 *   EventHandler.create(ActionListener.class, target, "a.b", "c.d")
 * </pre>
 * 可能会写成以下内部类
 * （假设所有属性都有规范的 getter 方法并返回适当的类型）：
 * <pre>
 *   // 相当于使用内部类而不是 EventHandler 的代码。
 *   new ActionListener() {
 *     public void actionPerformed(ActionEvent e) {
 *         target.getA().setB(e.getC().isD());
 *    }
 *}
 *</pre>
 * <p>
 * 由于 <code>EventHandler</code> 最终依赖于反射来调用方法，我们不建议针对重载方法。例如，
 * 如果目标是 <code>MyTarget</code> 类的实例，该类定义如下：
 * <pre>
 *   public class MyTarget {
 *     public void doIt(String);
 *     public void doIt(Object);
 *   }
 * </pre>
 * 那么 <code>doIt</code> 方法就是重载的。EventHandler 将根据源调用适当的方法。如果源为
 * null，则两个方法都适用，调用哪个方法是不确定的。因此，我们不建议针对重载方法。
 *
 * @see java.lang.reflect.Proxy
 * @see java.util.EventObject
 *
 * @since 1.4
 *
 * @author Mark Davidson
 * @author Philip Milne
 * @author Hans Muller
 *
 */
public class EventHandler implements InvocationHandler {
    private Object target;
    private String action;
    private final String eventPropertyName;
    private final String listenerMethodName;
    private final AccessControlContext acc = AccessController.getContext();

    /**
     * 创建一个新的 <code>EventHandler</code> 对象；
     * 通常你使用其中一个 <code>create</code> 方法
     * 而不是直接调用此构造函数。请参阅
     * {@link java.beans.EventHandler#create(Class, Object, String, String)
     * 通用版本的 create} 以获取 <code>eventPropertyName</code> 和 <code>listenerMethodName</code>
     * 参数的完整描述。
     *
     * @param target 将执行操作的对象
     * @param action 目标上的（可能限定的）属性或方法的名称
     * @param eventPropertyName 传入事件的（可能限定的）可读属性的名称
     * @param listenerMethodName 应触发操作的监听器接口中的方法的名称
     *
     * @throws NullPointerException 如果 <code>target</code> 为 null
     * @throws NullPointerException 如果 <code>action</code> 为 null
     *
     * @see EventHandler
     * @see #create(Class, Object, String, String, String)
     * @see #getTarget
     * @see #getAction
     * @see #getEventPropertyName
     * @see #getListenerMethodName
     */
    @ConstructorProperties({"target", "action", "eventPropertyName", "listenerMethodName"})
    public EventHandler(Object target, String action, String eventPropertyName, String listenerMethodName) {
        this.target = target;
        this.action = action;
        if (target == null) {
            throw new NullPointerException("target must be non-null");
        }
        if (action == null) {
            throw new NullPointerException("action must be non-null");
        }
        this.eventPropertyName = eventPropertyName;
        this.listenerMethodName = listenerMethodName;
    }

    /**
     * 返回此事件处理器将发送消息的对象。
     *
     * @return 此事件处理器的目标
     * @see #EventHandler(Object, String, String, String)
     */
    public Object getTarget()  {
        return target;
    }

    /**
     * 返回此事件处理器将设置的目标的可写属性的名称，
     * 或此事件处理器将在目标上调用的方法的名称。
     *
     * @return 此事件处理器的操作
     * @see #EventHandler(Object, String, String, String)
     */
    public String getAction()  {
        return action;
    }

    /**
     * 返回应用于目标操作的事件属性。
     *
     * @return 事件的属性
     *
     * @see #EventHandler(Object, String, String, String)
     */
    public String getEventPropertyName()  {
        return eventPropertyName;
    }

    /**
     * 返回将触发操作的方法的名称。
     * 返回值为 <code>null</code> 表示监听器接口中的所有方法都触发操作。
     *
     * @return 将触发操作的方法的名称
     *
     * @see #EventHandler(Object, String, String, String)
     */
    public String getListenerMethodName()  {
        return listenerMethodName;
    }

    private Object applyGetters(Object target, String getters) {
        if (getters == null || getters.equals("")) {
            return target;
        }
        int firstDot = getters.indexOf('.');
        if (firstDot == -1) {
            firstDot = getters.length();
        }
        String first = getters.substring(0, firstDot);
        String rest = getters.substring(Math.min(firstDot + 1, getters.length()));

        try {
            Method getter = null;
            if (target != null) {
                getter = Statement.getMethod(target.getClass(),
                                      "get" + NameGenerator.capitalize(first),
                                      new Class<?>[]{});
                if (getter == null) {
                    getter = Statement.getMethod(target.getClass(),
                                   "is" + NameGenerator.capitalize(first),
                                   new Class<?>[]{});
                }
                if (getter == null) {
                    getter = Statement.getMethod(target.getClass(), first, new Class<?>[]{});
                }
            }
            if (getter == null) {
                throw new RuntimeException("No method called: " + first +
                                           " defined on " + target);
            }
            Object newTarget = MethodUtil.invoke(getter, target, new Object[]{});
            return applyGetters(newTarget, rest);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to call method: " + first +
                                       " on " + target, e);
        }
    }


                /**
     * 从事件中提取适当的属性值，
     * 并将其传递给与此 <code>EventHandler</code> 关联的操作。
     *
     * @param proxy 代理对象
     * @param method 监听器接口中的方法
     * @return 将操作应用于目标的结果
     *
     * @see EventHandler
     */
    public Object invoke(final Object proxy, final Method method, final Object[] arguments) {
        AccessControlContext acc = this.acc;
        if ((acc == null) && (System.getSecurityManager() != null)) {
            throw new SecurityException("AccessControlContext 未设置");
        }
        return AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                return invokeInternal(proxy, method, arguments);
            }
        }, acc);
    }

    private Object invokeInternal(Object proxy, Method method, Object[] arguments) {
        String methodName = method.getName();
        if (method.getDeclaringClass() == Object.class)  {
            // 处理 Object 的公共方法。
            if (methodName.equals("hashCode"))  {
                return new Integer(System.identityHashCode(proxy));
            } else if (methodName.equals("equals")) {
                return (proxy == arguments[0] ? Boolean.TRUE : Boolean.FALSE);
            } else if (methodName.equals("toString")) {
                return proxy.getClass().getName() + '@' + Integer.toHexString(proxy.hashCode());
            }
        }

        if (listenerMethodName == null || listenerMethodName.equals(methodName)) {
            Class[] argTypes = null;
            Object[] newArgs = null;

            if (eventPropertyName == null) {     // 无参数方法。
                newArgs = new Object[]{};
                argTypes = new Class<?>[]{};
            }
            else {
                Object input = applyGetters(arguments[0], getEventPropertyName());
                newArgs = new Object[]{input};
                argTypes = new Class<?>[]{input == null ? null :
                                       input.getClass()};
            }
            try {
                int lastDot = action.lastIndexOf('.');
                if (lastDot != -1) {
                    target = applyGetters(target, action.substring(0, lastDot));
                    action = action.substring(lastDot + 1);
                }
                Method targetMethod = Statement.getMethod(
                             target.getClass(), action, argTypes);
                if (targetMethod == null) {
                    targetMethod = Statement.getMethod(target.getClass(),
                             "set" + NameGenerator.capitalize(action), argTypes);
                }
                if (targetMethod == null) {
                    String argTypeString = (argTypes.length == 0)
                        ? " 无参数"
                        : " 参数 " + argTypes[0];
                    throw new RuntimeException(
                        "没有名为 " + action + " 的方法在 " +
                        target.getClass() + argTypeString);
                }
                return MethodUtil.invoke(targetMethod, target, newArgs);
            }
            catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
            catch (InvocationTargetException ex) {
                Throwable th = ex.getTargetException();
                throw (th instanceof RuntimeException)
                        ? (RuntimeException) th
                        : new RuntimeException(th);
            }
        }
        return null;
    }

    /**
     * 创建一个实现 <code>listenerInterface</code> 的对象，其中
     * 监听器接口中的 <em>所有</em> 方法都将
     * 将操作应用于目标。此方法通过调用更通用的
     * <code>create</code> 方法实现，其中 <code>eventPropertyName</code>
     * 和 <code>listenerMethodName</code> 均为 <code>null</code>。
     * 有关 <code>action</code> 参数的完整描述，请参阅
     * {@link java.beans.EventHandler#create(Class, Object, String, String)
     * 通用版本的 create}。
     * <p>
     * 要创建一个显示 <code>JDialog</code> 的 <code>ActionListener</code>，
     * 可以编写如下代码：
     *
     *<blockquote>
     *<pre>
     *EventHandler.create(ActionListener.class, dialog, "show")
     *</pre>
     *</blockquote>
     *
     * @param <T> 要创建的类型
     * @param listenerInterface 要为其创建代理的监听器接口
     * @param target 执行操作的对象
     * @param action 目标上的（可能限定的）属性或方法的名称
     * @return 实现 <code>listenerInterface</code> 的对象
     *
     * @throws NullPointerException 如果 <code>listenerInterface</code> 为 null
     * @throws NullPointerException 如果 <code>target</code> 为 null
     * @throws NullPointerException 如果 <code>action</code> 为 null
     *
     * @see #create(Class, Object, String, String)
     */
    public static <T> T create(Class<T> listenerInterface,
                               Object target, String action)
    {
        return create(listenerInterface, target, action, null, null);
    }

    /**
     * 创建一个实现 <code>listenerInterface</code> 的对象，其中
     * <em>所有</em> 方法都将事件表达式的值
     * <code>eventPropertyName</code> 传递给最终的语句方法 <code>action</code>，
     * 该方法应用于目标。此方法通过调用更通用的
     * <code>create</code> 方法实现，其中 <code>listenerMethodName</code>
     * 为 <code>null</code>。有关 <code>action</code> 和 <code>eventPropertyName</code>
     * 参数的完整描述，请参阅
     * {@link java.beans.EventHandler#create(Class, Object, String, String)
     * 通用版本的 create}。
     * <p>
     * 要创建一个将 <code>JTextField</code> 源的文本值设置为
     * <code>JLabel</code> 的文本的 <code>ActionListener</code>，
     * 可以使用以下代码：
     *
     *<blockquote>
     *<pre>
     *EventHandler.create(ActionListener.class, label, "text", "source.text");
     *</pre>
     *</blockquote>
     *
     * 这相当于以下代码：
     *<blockquote>
     *<pre>
// 使用内部类而不是 EventHandler 的等效代码。
     *new ActionListener() {
     *    public void actionPerformed(ActionEvent event) {
     *        label.setText(((JTextField)(event.getSource())).getText());
     *     }
     *};
     *</pre>
     *</blockquote>
     *
     * @param <T> 要创建的类型
     * @param listenerInterface 要为其创建代理的监听器接口
     * @param target 执行操作的对象
     * @param action 目标上的（可能限定的）属性或方法的名称
     * @param eventPropertyName 进入事件的（可能限定的）可读属性的名称
     *
     * @return 实现 <code>listenerInterface</code> 的对象
     *
     * @throws NullPointerException 如果 <code>listenerInterface</code> 为 null
     * @throws NullPointerException 如果 <code>target</code> 为 null
     * @throws NullPointerException 如果 <code>action</code> 为 null
     *
     * @see #create(Class, Object, String, String, String)
     */
    public static <T> T create(Class<T> listenerInterface,
                               Object target, String action,
                               String eventPropertyName)
    {
        return create(listenerInterface, target, action, eventPropertyName, null);
    }

    /**
     * 创建一个实现 <code>listenerInterface</code> 的对象，其中
     * 命名为 <code>listenerMethodName</code> 的方法
     * 将事件表达式的值 <code>eventPropertyName</code>
     * 传递给最终的语句方法 <code>action</code>，该方法
     * 应用于目标。其他所有监听器方法什么都不做。
     * <p>
     * <code>eventPropertyName</code> 字符串用于从进入的事件对象中提取一个值，
     * 该值传递给目标方法。常见情况是目标方法不带参数，此时
     * 应使用 null 作为 <code>eventPropertyName</code>。或者，如果希望
     * 将进入的事件对象直接传递给目标方法，可以使用空字符串。
     * <code>eventPropertyName</code> 字符串的格式是一系列方法或属性，
     * 每个方法或属性应用于前一个方法返回的值，从进入的事件对象开始。
     * 语法是： <code>propertyName{.propertyName}*</code>
     * 其中 <code>propertyName</code> 匹配一个方法或
     * 属性。例如，要从 <code>MouseEvent</code> 中提取 <code>point</code>
     * 属性，可以使用 <code>"point"</code> 或 <code>"getPoint"</code>
     * 作为 <code>eventPropertyName</code>。要从 <code>MouseEvent</code>
     * 中提取 <code>JLabel</code> 源的 "text" 属性，可以使用以下任何一种
     * 作为 <code>eventPropertyName</code>：
     * <code>"source.text"</code>，
     * <code>"getSource.text"</code> <code>"getSource.getText"</code> 或
     * <code>"source.getText"</code>。如果找不到方法，或在调用方法时生成异常，
     * 将在调度时抛出 <code>RuntimeException</code>。例如，如果进入的事件对象为 null，
     * 且 <code>eventPropertyName</code> 非空，将抛出 <code>RuntimeException</code>。
     * <p>
     * <code>action</code> 参数的格式与 <code>eventPropertyName</code> 参数相同，
     * 其中最后一个属性名称标识一个方法名称或可写属性。
     * <p>
     * 如果 <code>listenerMethodName</code> 为 <code>null</code>，
     * 则接口中的 <em>所有</em> 方法都会触发在目标上执行 <code>action</code>。
     * <p>
     * 例如，要创建一个 <code>MouseListener</code>，每次按下鼠标按钮时，
     * 将目标对象的 <code>origin</code> 属性设置为进入的 <code>MouseEvent</code> 的
     * 位置（即 <code>mouseEvent.getPoint()</code>），可以编写如下代码：
     *<blockquote>
     *<pre>
     *EventHandler.create(MouseListener.class, target, "origin", "point", "mousePressed");
     *</pre>
     *</blockquote>
     *
     * 这相当于编写一个 <code>MouseListener</code>，其中所有方法
     * 除了 <code>mousePressed</code> 都是空操作：
     *
     *<blockquote>
     *<pre>
// 使用内部类而不是 EventHandler 的等效代码。
     *new MouseAdapter() {
     *    public void mousePressed(MouseEvent e) {
     *        target.setOrigin(e.getPoint());
     *    }
     *};
     * </pre>
     *</blockquote>
     *
     * @param <T> 要创建的类型
     * @param listenerInterface 要为其创建代理的监听器接口
     * @param target 执行操作的对象
     * @param action 目标上的（可能限定的）属性或方法的名称
     * @param eventPropertyName 进入事件的（可能限定的）可读属性的名称
     * @param listenerMethodName 监听器接口中应触发操作的方法的名称
     *
     * @return 实现 <code>listenerInterface</code> 的对象
     *
     * @throws NullPointerException 如果 <code>listenerInterface</code> 为 null
     * @throws NullPointerException 如果 <code>target</code> 为 null
     * @throws NullPointerException 如果 <code>action</code> 为 null
     *
     * @see EventHandler
     */
    public static <T> T create(Class<T> listenerInterface,
                               Object target, String action,
                               String eventPropertyName,
                               String listenerMethodName)
    {
        // 首先创建此对象以验证 target/action 非空
        final EventHandler handler = new EventHandler(target, action,
                                                     eventPropertyName,
                                                     listenerMethodName);
        if (listenerInterface == null) {
            throw new NullPointerException(
                          "listenerInterface 必须非空");
        }
        final ClassLoader loader = getClassLoader(listenerInterface);
        final Class<?>[] interfaces = {listenerInterface};
        return AccessController.doPrivileged(new PrivilegedAction<T>() {
            @SuppressWarnings("unchecked")
            public T run() {
                return (T) Proxy.newProxyInstance(loader, interfaces, handler);
            }
        });
    }

    private static ClassLoader getClassLoader(Class<?> type) {
        ReflectUtil.checkPackageAccess(type);
        ClassLoader loader = type.getClassLoader();
        if (loader == null) {
            loader = Thread.currentThread().getContextClassLoader(); // 避免使用 BCP
            if (loader == null) {
                loader = ClassLoader.getSystemClassLoader();
            }
        }
        return loader;
    }
}
