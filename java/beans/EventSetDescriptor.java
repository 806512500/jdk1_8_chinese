
/*
 * Copyright (c) 1996, 2012, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.ref.Reference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 一个 EventSetDescriptor 描述了一组由给定 Java
 * bean 触发的事件。
 * <P>
 * 给定的一组事件都是作为对单个事件监听器接口的方法调用传递的，可以通过调用事件源提供的注册方法来注册事件监听器对象。
 */
public class EventSetDescriptor extends FeatureDescriptor {

    private MethodDescriptor[] listenerMethodDescriptors;
    private MethodDescriptor addMethodDescriptor;
    private MethodDescriptor removeMethodDescriptor;
    private MethodDescriptor getMethodDescriptor;

    private Reference<Method[]> listenerMethodsRef;
    private Reference<? extends Class<?>> listenerTypeRef;

    private boolean unicast;
    private boolean inDefaultEventSet = true;

    /**
     * 创建一个 <TT>EventSetDescriptor</TT>，假设你遵循最简单的标准设计模式，其中名为
     * "fred" 的事件 (1) 作为对 FredListener 接口的单个方法的调用传递，(2) 有一个 FredEvent 类型的单个参数，
     * (3) 可以通过调用源组件的 addFredListener 方法来注册 FredListener，并通过调用 removeFredListener 方法来移除。
     *
     * @param sourceClass  触发事件的类。
     * @param eventSetName  事件的程序名称。例如 "fred"。
     *          注意，这通常应该以小写字母开头。
     * @param listenerType  事件将传递到的目标接口。
     * @param listenerMethodName  当事件传递到其目标监听器接口时将被调用的方法。
     * @exception IntrospectionException 如果在内省过程中发生异常。
     */
    public EventSetDescriptor(Class<?> sourceClass, String eventSetName,
                Class<?> listenerType, String listenerMethodName)
                throws IntrospectionException {
        this(sourceClass, eventSetName, listenerType,
             new String[] { listenerMethodName },
             Introspector.ADD_PREFIX + getListenerClassName(listenerType),
             Introspector.REMOVE_PREFIX + getListenerClassName(listenerType),
             Introspector.GET_PREFIX + getListenerClassName(listenerType) + "s");

        String eventName = NameGenerator.capitalize(eventSetName) + "Event";
        Method[] listenerMethods = getListenerMethods();
        if (listenerMethods.length > 0) {
            Class[] args = getParameterTypes(getClass0(), listenerMethods[0]);
            // 检查 EventSet 的合规性。特殊情况处理 vetoableChange。参见 4529996
            if (!"vetoableChange".equals(eventSetName) && !args[0].getName().endsWith(eventName)) {
                throw new IntrospectionException("方法 \"" + listenerMethodName +
                                                 "\" 应该有参数 \"" +
                                                 eventName + "\"");
            }
        }
    }

    private static String getListenerClassName(Class<?> cls) {
        String className = cls.getName();
        return className.substring(className.lastIndexOf('.') + 1);
    }

    /**
     * 使用字符串名称从头创建一个 <TT>EventSetDescriptor</TT>。
     *
     * @param sourceClass  触发事件的类。
     * @param eventSetName  事件集的程序名称。
     *          注意，这通常应该以小写字母开头。
     * @param listenerType  事件将传递到的目标接口的类。
     * @param listenerMethodNames  当事件传递到其目标监听器接口时将被调用的方法的名称。
     * @param addListenerMethodName  事件源上可用于注册事件监听器对象的方法的名称。
     * @param removeListenerMethodName  事件源上可用于注销事件监听器对象的方法的名称。
     * @exception IntrospectionException 如果在内省过程中发生异常。
     */
    public EventSetDescriptor(Class<?> sourceClass,
                String eventSetName,
                Class<?> listenerType,
                String listenerMethodNames[],
                String addListenerMethodName,
                String removeListenerMethodName)
                throws IntrospectionException {
        this(sourceClass, eventSetName, listenerType,
             listenerMethodNames, addListenerMethodName,
             removeListenerMethodName, null);
    }

    /**
     * 使用字符串名称从头创建一个 EventSetDescriptor。
     *
     * @param sourceClass  触发事件的类。
     * @param eventSetName  事件集的程序名称。
     *          注意，这通常应该以小写字母开头。
     * @param listenerType  事件将传递到的目标接口的类。
     * @param listenerMethodNames  当事件传递到其目标监听器接口时将被调用的方法的名称。
     * @param addListenerMethodName  事件源上可用于注册事件监听器对象的方法的名称。
     * @param removeListenerMethodName  事件源上可用于注销事件监听器对象的方法的名称。
     * @param getListenerMethodName  事件源上可用于访问事件监听器对象数组的方法。
     * @exception IntrospectionException 如果在内省过程中发生异常。
     * @since 1.4
     */
    public EventSetDescriptor(Class<?> sourceClass,
                String eventSetName,
                Class<?> listenerType,
                String listenerMethodNames[],
                String addListenerMethodName,
                String removeListenerMethodName,
                String getListenerMethodName)
                throws IntrospectionException {
        if (sourceClass == null || eventSetName == null || listenerType == null) {
            throw new NullPointerException();
        }
        setName(eventSetName);
        setClass0(sourceClass);
        setListenerType(listenerType);

        Method[] listenerMethods = new Method[listenerMethodNames.length];
        for (int i = 0; i < listenerMethodNames.length; i++) {
            // 检查空名称
            if (listenerMethodNames[i] == null) {
                throw new NullPointerException();
            }
            listenerMethods[i] = getMethod(listenerType, listenerMethodNames[i], 1);
        }
        setListenerMethods(listenerMethods);

        setAddListenerMethod(getMethod(sourceClass, addListenerMethodName, 1));
        setRemoveListenerMethod(getMethod(sourceClass, removeListenerMethodName, 1));

        // 对找不到 getListener 方法的情况更加宽容。
        Method method = Introspector.findMethod(sourceClass, getListenerMethodName, 0);
        if (method != null) {
            setGetListenerMethod(method);
        }
    }

    private static Method getMethod(Class<?> cls, String name, int args)
        throws IntrospectionException {
        if (name == null) {
            return null;
        }
        Method method = Introspector.findMethod(cls, name, args);
        if ((method == null) || Modifier.isStatic(method.getModifiers())) {
            throw new IntrospectionException("Method not found: " + name +
                                             " on class " + cls.getName());
        }
        return method;
    }

    /**
     * 使用 <TT>java.lang.reflect.Method</TT> 和 <TT>java.lang.Class</TT> 对象从头创建一个 <TT>EventSetDescriptor</TT>。
     *
     * @param eventSetName  事件集的程序名称。
     * @param listenerType  监听器接口的类。
     * @param listenerMethods  描述目标监听器中每个事件处理方法的 Method 对象数组。
     * @param addListenerMethod  事件源上可用于注册事件监听器对象的方法。
     * @param removeListenerMethod  事件源上可用于注销事件监听器对象的方法。
     * @exception IntrospectionException 如果在内省过程中发生异常。
     */
    public EventSetDescriptor(String eventSetName,
                Class<?> listenerType,
                Method listenerMethods[],
                Method addListenerMethod,
                Method removeListenerMethod)
                throws IntrospectionException {
        this(eventSetName, listenerType, listenerMethods,
             addListenerMethod, removeListenerMethod, null);
    }

    /**
     * 使用 java.lang.reflect.Method 和 java.lang.Class 对象从头创建一个 EventSetDescriptor。
     *
     * @param eventSetName  事件集的程序名称。
     * @param listenerType  监听器接口的类。
     * @param listenerMethods  描述目标监听器中每个事件处理方法的 Method 对象数组。
     * @param addListenerMethod  事件源上可用于注册事件监听器对象的方法。
     * @param removeListenerMethod  事件源上可用于注销事件监听器对象的方法。
     * @param getListenerMethod  事件源上可用于访问事件监听器对象数组的方法。
     * @exception IntrospectionException 如果在内省过程中发生异常。
     * @since 1.4
     */
    public EventSetDescriptor(String eventSetName,
                Class<?> listenerType,
                Method listenerMethods[],
                Method addListenerMethod,
                Method removeListenerMethod,
                Method getListenerMethod)
                throws IntrospectionException {
        setName(eventSetName);
        setListenerMethods(listenerMethods);
        setAddListenerMethod(addListenerMethod);
        setRemoveListenerMethod(removeListenerMethod);
        setGetListenerMethod(getListenerMethod);
        setListenerType(listenerType);
    }

    /**
     * 使用 <TT>java.lang.reflect.MethodDescriptor</TT> 和 <TT>java.lang.Class</TT>
     * 对象从头创建一个 <TT>EventSetDescriptor</TT>。
     *
     * @param eventSetName  事件集的程序名称。
     * @param listenerType  监听器接口的类。
     * @param listenerMethodDescriptors  描述目标监听器中每个事件处理方法的 MethodDescriptor 对象数组。
     * @param addListenerMethod  事件源上可用于注册事件监听器对象的方法。
     * @param removeListenerMethod  事件源上可用于注销事件监听器对象的方法。
     * @exception IntrospectionException 如果在内省过程中发生异常。
     */
    public EventSetDescriptor(String eventSetName,
                Class<?> listenerType,
                MethodDescriptor listenerMethodDescriptors[],
                Method addListenerMethod,
                Method removeListenerMethod)
                throws IntrospectionException {
        setName(eventSetName);
        this.listenerMethodDescriptors = (listenerMethodDescriptors != null)
                ? listenerMethodDescriptors.clone()
                : null;
        setAddListenerMethod(addListenerMethod);
        setRemoveListenerMethod(removeListenerMethod);
        setListenerType(listenerType);
    }

    /**
     * 获取目标接口的 <TT>Class</TT> 对象。
     *
     * @return 当事件触发时将被调用的目标接口的 Class 对象。
     */
    public Class<?> getListenerType() {
        return (this.listenerTypeRef != null)
                ? this.listenerTypeRef.get()
                : null;
    }

    private void setListenerType(Class<?> cls) {
        this.listenerTypeRef = getWeakReference(cls);
    }

    /**
     * 获取目标监听器接口的方法。
     *
     * @return 一个 <TT>Method</TT> 对象数组，表示当事件触发时将被调用的目标监听器接口中的目标方法。
     */
    public synchronized Method[] getListenerMethods() {
        Method[] methods = getListenerMethods0();
        if (methods == null) {
            if (listenerMethodDescriptors != null) {
                methods = new Method[listenerMethodDescriptors.length];
                for (int i = 0; i < methods.length; i++) {
                    methods[i] = listenerMethodDescriptors[i].getMethod();
                }
            }
            setListenerMethods(methods);
        }
        return methods;
    }

    private void setListenerMethods(Method[] methods) {
        if (methods == null) {
            return;
        }
        if (listenerMethodDescriptors == null) {
            listenerMethodDescriptors = new MethodDescriptor[methods.length];
            for (int i = 0; i < methods.length; i++) {
                listenerMethodDescriptors[i] = new MethodDescriptor(methods[i]);
            }
        }
        this.listenerMethodsRef = getSoftReference(methods);
    }

    private Method[] getListenerMethods0() {
        return (this.listenerMethodsRef != null)
                ? this.listenerMethodsRef.get()
                : null;
    }


                /**
     * 获取目标监听器接口的 <code>MethodDescriptor</code>。
     *
     * @return 一个包含目标方法的 <code>MethodDescriptor</code> 对象数组，这些方法在事件触发时会被调用。
     */
    public synchronized MethodDescriptor[] getListenerMethodDescriptors() {
        return (this.listenerMethodDescriptors != null)
                ? this.listenerMethodDescriptors.clone()
                : null;
    }

    /**
     * 获取用于添加事件监听器的方法。
     *
     * @return 用于在事件源注册监听器的方法。
     */
    public synchronized Method getAddListenerMethod() {
        return getMethod(this.addMethodDescriptor);
    }

    private synchronized void setAddListenerMethod(Method method) {
        if (method == null) {
            return;
        }
        if (getClass0() == null) {
            setClass0(method.getDeclaringClass());
        }
        addMethodDescriptor = new MethodDescriptor(method);
        setTransient(method.getAnnotation(Transient.class));
    }

    /**
     * 获取用于移除事件监听器的方法。
     *
     * @return 用于在事件源移除监听器的方法。
     */
    public synchronized Method getRemoveListenerMethod() {
        return getMethod(this.removeMethodDescriptor);
    }

    private synchronized void setRemoveListenerMethod(Method method) {
        if (method == null) {
            return;
        }
        if (getClass0() == null) {
            setClass0(method.getDeclaringClass());
        }
        removeMethodDescriptor = new MethodDescriptor(method);
        setTransient(method.getAnnotation(Transient.class));
    }

    /**
     * 获取用于访问已注册事件监听器的方法。
     *
     * @return 用于访问事件源的监听器数组的方法，如果不存在则返回 null。
     * @since 1.4
     */
    public synchronized Method getGetListenerMethod() {
        return getMethod(this.getMethodDescriptor);
    }

    private synchronized void setGetListenerMethod(Method method) {
        if (method == null) {
            return;
        }
        if (getClass0() == null) {
            setClass0(method.getDeclaringClass());
        }
        getMethodDescriptor = new MethodDescriptor(method);
        setTransient(method.getAnnotation(Transient.class));
    }

    /**
     * 标记事件集是否为单播（或不是）。
     *
     * @param unicast 如果事件集为单播，则为 true。
     */
    public void setUnicast(boolean unicast) {
        this.unicast = unicast;
    }

    /**
     * 通常事件源是多播的。但有些例外情况是严格单播的。
     *
     * @return 如果事件集为单播，则返回 <TT>true</TT>，默认为 <TT>false</TT>。
     */
    public boolean isUnicast() {
        return unicast;
    }

    /**
     * 标记事件集是否属于“默认”集（或不是）。默认为 <TT>true</TT>。
     *
     * @param inDefaultEventSet 如果事件集属于“默认”集，则为 <code>true</code>，否则为 <code>false</code>
     */
    public void setInDefaultEventSet(boolean inDefaultEventSet) {
        this.inDefaultEventSet = inDefaultEventSet;
    }

    /**
     * 报告事件集是否属于“默认”集。
     *
     * @return 如果事件集属于“默认”集，则返回 <TT>true</TT>，默认为 <TT>true</TT>。
     */
    public boolean isInDefaultEventSet() {
        return inDefaultEventSet;
    }

    /*
     * 包私有构造函数
     * 合并两个事件集描述符。如果有冲突，优先使用第二个参数（y）。
     *
     * @param x 第一个（优先级较低的）EventSetDescriptor
     * @param y 第二个（优先级较高的）EventSetDescriptor
     */
    EventSetDescriptor(EventSetDescriptor x, EventSetDescriptor y) {
        super(x,y);
        listenerMethodDescriptors = x.listenerMethodDescriptors;
        if (y.listenerMethodDescriptors != null) {
            listenerMethodDescriptors = y.listenerMethodDescriptors;
        }

        listenerTypeRef = x.listenerTypeRef;
        if (y.listenerTypeRef != null) {
            listenerTypeRef = y.listenerTypeRef;
        }

        addMethodDescriptor = x.addMethodDescriptor;
        if (y.addMethodDescriptor != null) {
            addMethodDescriptor = y.addMethodDescriptor;
        }

        removeMethodDescriptor = x.removeMethodDescriptor;
        if (y.removeMethodDescriptor != null) {
            removeMethodDescriptor = y.removeMethodDescriptor;
        }

        getMethodDescriptor = x.getMethodDescriptor;
        if (y.getMethodDescriptor != null) {
            getMethodDescriptor = y.getMethodDescriptor;
        }

        unicast = y.unicast;
        if (!x.inDefaultEventSet || !y.inDefaultEventSet) {
            inDefaultEventSet = false;
        }
    }

    /*
     * 包私有复制构造函数
     * 必须使新对象与旧对象的任何更改隔离。
     */
    EventSetDescriptor(EventSetDescriptor old) {
        super(old);
        if (old.listenerMethodDescriptors != null) {
            int len = old.listenerMethodDescriptors.length;
            listenerMethodDescriptors = new MethodDescriptor[len];
            for (int i = 0; i < len; i++) {
                listenerMethodDescriptors[i] = new MethodDescriptor(
                                        old.listenerMethodDescriptors[i]);
            }
        }
        listenerTypeRef = old.listenerTypeRef;

        addMethodDescriptor = old.addMethodDescriptor;
        removeMethodDescriptor = old.removeMethodDescriptor;
        getMethodDescriptor = old.getMethodDescriptor;

        unicast = old.unicast;
        inDefaultEventSet = old.inDefaultEventSet;
    }

    void appendTo(StringBuilder sb) {
        appendTo(sb, "unicast", this.unicast);
        appendTo(sb, "inDefaultEventSet", this.inDefaultEventSet);
        appendTo(sb, "listenerType", this.listenerTypeRef);
        appendTo(sb, "getListenerMethod", getMethod(this.getMethodDescriptor));
        appendTo(sb, "addListenerMethod", getMethod(this.addMethodDescriptor));
        appendTo(sb, "removeListenerMethod", getMethod(this.removeMethodDescriptor));
    }

    private static Method getMethod(MethodDescriptor descriptor) {
        return (descriptor != null)
                ? descriptor.getMethod()
                : null;
    }
}
