/*
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.beans.TypeResolver;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.ref.SoftReference;

import java.lang.reflect.Method;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map.Entry;

/**
 * FeatureDescriptor 类是 PropertyDescriptor、EventSetDescriptor 和 MethodDescriptor 等的公共基类。
 * <p>
 * 它支持一些可以为任何内省描述符设置和检索的通用信息。
 * <p>
 * 此外，它提供了一种扩展机制，以便将任意属性/值对与设计功能关联起来。
 */

public class FeatureDescriptor {
    private static final String TRANSIENT = "transient";

    private Reference<? extends Class<?>> classRef;

    /**
     * 构造一个 <code>FeatureDescriptor</code>。
     */
    public FeatureDescriptor() {
    }

    /**
     * 获取此功能的编程名称。
     *
     * @return 属性/方法/事件的编程名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置此功能的编程名称。
     *
     * @param name  属性/方法/事件的编程名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取此功能的本地化显示名称。
     *
     * @return 属性/方法/事件的本地化显示名称。默认情况下，它与 getName 返回的编程名称相同。
     */
    public String getDisplayName() {
        if (displayName == null) {
            return getName();
        }
        return displayName;
    }

    /**
     * 设置此功能的本地化显示名称。
     *
     * @param displayName  属性/方法/事件的本地化显示名称。
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * “专家”标志用于区分那些旨在供专家用户使用的功能与那些旨在供普通用户使用的功能。
     *
     * @return 如果此功能仅打算供专家使用，则返回 true。
     */
    public boolean isExpert() {
        return expert;
    }

    /**
     * “专家”标志用于区分那些旨在供专家用户使用的功能与那些旨在供普通用户使用的功能。
     *
     * @param expert 如果此功能仅打算供专家使用，则为 true。
     */
    public void setExpert(boolean expert) {
        this.expert = expert;
    }

    /**
     * “隐藏”标志用于标识那些仅打算供工具使用且不应向人类用户展示的功能。
     *
     * @return 如果此功能应隐藏在人类用户面前，则返回 true。
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * “隐藏”标志用于标识那些仅打算供工具使用且不应向人类用户展示的功能。
     *
     * @param hidden  如果此功能应隐藏在人类用户面前，则为 true。
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * “首选”标志用于标识那些特别重要以呈现给人类用户的功能。
     *
     * @return 如果此功能应优先展示给人类用户，则返回 true。
     */
    public boolean isPreferred() {
        return preferred;
    }

    /**
     * “首选”标志用于标识那些特别重要以呈现给人类用户的功能。
     *
     * @param preferred  如果此功能应优先展示给人类用户，则为 true。
     */
    public void setPreferred(boolean preferred) {
        this.preferred = preferred;
    }

    /**
     * 获取此功能的简短描述。
     *
     * @return 与此属性/方法/事件关联的本地化简短描述。默认情况下，它与显示名称相同。
     */
    public String getShortDescription() {
        if (shortDescription == null) {
            return getDisplayName();
        }
        return shortDescription;
    }

    /**
     * 您可以将一个简短的描述性字符串与一个功能关联起来。通常，这些描述性字符串应少于约 40 个字符。
     * @param text  与此属性/方法/事件关联的（本地化的）简短描述。
     */
    public void setShortDescription(String text) {
        shortDescription = text;
    }

    /**
     * 将一个命名属性与此功能关联起来。
     *
     * @param attributeName  属性的本地化独立名称
     * @param value  属性的值
     */
    public void setValue(String attributeName, Object value) {
        getTable().put(attributeName, value);
    }

    /**
     * 检索与此功能关联的命名属性。
     *
     * @param attributeName  属性的本地化独立名称
     * @return 属性的值。如果属性未知，则可能为 null。
     */
    public Object getValue(String attributeName) {
        return (this.table != null)
                ? this.table.get(attributeName)
                : null;
    }

    /**
     * 获取此功能的本地化独立名称的枚举。
     *
     * @return 通过 setValue 注册的任何属性的本地化独立名称的枚举。
     */
    public Enumeration<String> attributeNames() {
        return getTable().keys();
    }

    /**
     * 包私有构造函数，合并两个 FeatureDescriptors 的信息。
     * 合并的隐藏和专家标志是通过或操作值形成的。
     * 在其他冲突的情况下，第二个参数（y）的优先级高于第一个参数（x）。
     *
     * @param x  第一个（较低优先级）MethodDescriptor
     * @param y  第二个（较高优先级）MethodDescriptor
     */
    FeatureDescriptor(FeatureDescriptor x, FeatureDescriptor y) {
        expert = x.expert | y.expert;
        hidden = x.hidden | y.hidden;
        preferred = x.preferred | y.preferred;
        name = y.name;
        shortDescription = x.shortDescription;
        if (y.shortDescription != null) {
            shortDescription = y.shortDescription;
        }
        displayName = x.displayName;
        if (y.displayName != null) {
            displayName = y.displayName;
        }
        classRef = x.classRef;
        if (y.classRef != null) {
            classRef = y.classRef;
        }
        addTable(x.table);
        addTable(y.table);
    }

    /*
     * 包私有复制构造函数
     * 这必须使新对象与旧对象的任何更改隔离。
     */
    FeatureDescriptor(FeatureDescriptor old) {
        expert = old.expert;
        hidden = old.hidden;
        preferred = old.preferred;
        name = old.name;
        shortDescription = old.shortDescription;
        displayName = old.displayName;
        classRef = old.classRef;

        addTable(old.table);
    }

    /**
     * 从指定的属性表中复制所有值。
     * 如果某个属性已存在，则其值应被覆盖。
     *
     * @param table  包含新值的属性表
     */
    private void addTable(Hashtable<String, Object> table) {
        if ((table != null) && !table.isEmpty()) {
            getTable().putAll(table);
        }
    }

    /**
     * 返回已初始化的属性表。
     *
     * @return 已初始化的属性表
     */
    private Hashtable<String, Object> getTable() {
        if (this.table == null) {
            this.table = new Hashtable<>();
        }
        return this.table;
    }

    /**
     * 根据注解设置“瞬态”属性。
     * 如果“瞬态”属性已设置，则不应更改。
     *
     * @param annotation  功能元素的注解
     */
    void setTransient(Transient annotation) {
        if ((annotation != null) && (null == getValue(TRANSIENT))) {
            setValue(TRANSIENT, annotation.value());
        }
    }

    /**
     * 指示功能是否为瞬态。
     *
     * @return 如果功能为瞬态，则返回 {@code true}，否则返回 {@code false}
     */
    boolean isTransient() {
        Object value = getValue(TRANSIENT);
        return (value instanceof Boolean)
                ? (Boolean) value
                : false;
    }

    // 包私有方法，用于重新创建弱/软引用

    void setClass0(Class<?> cls) {
        this.classRef = getWeakReference(cls);
    }

    Class<?> getClass0() {
        return (this.classRef != null)
                ? this.classRef.get()
                : null;
    }

    /**
     * 创建一个新的软引用，引用给定的对象。
     *
     * @return 一个新的软引用或 <code>null</code>（如果对象为 <code>null</code>）
     *
     * @see SoftReference
     */
    static <T> Reference<T> getSoftReference(T object) {
        return (object != null)
                ? new SoftReference<>(object)
                : null;
    }

    /**
     * 创建一个新的弱引用，引用给定的对象。
     *
     * @return 一个新的弱引用或 <code>null</code>（如果对象为 <code>null</code>）
     *
     * @see WeakReference
     */
    static <T> Reference<T> getWeakReference(T object) {
        return (object != null)
                ? new WeakReference<>(object)
                : null;
    }

    /**
     * 解析方法的返回类型。
     *
     * @param base    包含方法的类
     * @param method  代表方法的对象
     * @return 一个标识方法返回类型的类
     *
     * @see Method#getGenericReturnType
     * @see Method#getReturnType
     */
    static Class<?> getReturnType(Class<?> base, Method method) {
        if (base == null) {
            base = method.getDeclaringClass();
        }
        return TypeResolver.erase(TypeResolver.resolveInClass(base, method.getGenericReturnType()));
    }

    /**
     * 解析方法的参数类型。
     *
     * @param base    包含方法的类
     * @param method  代表方法的对象
     * @return 一个标识方法参数类型的类数组
     *
     * @see Method#getGenericParameterTypes
     * @see Method#getParameterTypes
     */
    static Class<?>[] getParameterTypes(Class<?> base, Method method) {
        if (base == null) {
            base = method.getDeclaringClass();
        }
        return TypeResolver.erase(TypeResolver.resolveInClass(base, method.getGenericParameterTypes()));
    }

    private boolean expert;
    private boolean hidden;
    private boolean preferred;
    private String shortDescription;
    private String name;
    private String displayName;
    private Hashtable<String, Object> table;

    /**
     * 返回对象的字符串表示形式。
     *
     * @return 对象的字符串表示形式
     *
     * @since 1.7
     */
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
        sb.append("[name=").append(this.name);
        appendTo(sb, "displayName", this.displayName);
        appendTo(sb, "shortDescription", this.shortDescription);
        appendTo(sb, "preferred", this.preferred);
        appendTo(sb, "hidden", this.hidden);
        appendTo(sb, "expert", this.expert);
        if ((this.table != null) && !this.table.isEmpty()) {
            sb.append("; values={");
            for (Entry<String, Object> entry : this.table.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("; ");
            }
            sb.setLength(sb.length() - 2);
            sb.append("}");
        }
        appendTo(sb);
        return sb.append("]").toString();
    }

    void appendTo(StringBuilder sb) {
    }

    static void appendTo(StringBuilder sb, String name, Reference<?> reference) {
        if (reference != null) {
            appendTo(sb, name, reference.get());
        }
    }

    static void appendTo(StringBuilder sb, String name, Object value) {
        if (value != null) {
            sb.append("; ").append(name).append("=").append(value);
        }
    }

    static void appendTo(StringBuilder sb, String name, boolean value) {
        if (value) {
            sb.append("; ").append(name);
        }
    }
}
