
/*
 * Copyright (c) 1996, 2015, Oracle and/or its affiliates. All rights reserved.
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
import java.lang.reflect.Constructor;
import sun.reflect.misc.ReflectUtil;

/**
 * PropertyDescriptor 描述了一个 Java Bean 通过一对访问方法导出的属性。
 */
public class PropertyDescriptor extends FeatureDescriptor {

    private Reference<? extends Class<?>> propertyTypeRef;
    private final MethodRef readMethodRef = new MethodRef();
    private final MethodRef writeMethodRef = new MethodRef();
    private Reference<? extends Class<?>> propertyEditorClassRef;

    private boolean bound;
    private boolean constrained;

    // 方法名的基本名称，将被读取和写入方法前缀。如果 name == "foo"，则 baseName 是 "Foo"
    private String baseName;

    private String writeMethodName;
    private String readMethodName;

    /**
     * 构造一个 PropertyDescriptor，该属性遵循标准的 Java 约定，具有 getFoo 和 setFoo
     * 访问方法。因此，如果参数名称是 "fred"，它将假设写入方法是 "setFred"，读取方法
     * 是 "getFred"（对于布尔属性，可以是 "isFred"）。注意属性名称应以小写字母开头，方法名
     * 中将被大写。
     *
     * @param propertyName 属性的程序名称。
     * @param beanClass 目标 Bean 的 Class 对象。例如 sun.beans.OurButton.class。
     * @exception IntrospectionException 如果在内省过程中发生异常。
     */
    public PropertyDescriptor(String propertyName, Class<?> beanClass)
                throws IntrospectionException {
        this(propertyName, beanClass,
                Introspector.IS_PREFIX + NameGenerator.capitalize(propertyName),
                Introspector.SET_PREFIX + NameGenerator.capitalize(propertyName));
    }

    /**
     * 该构造函数接受简单属性的名称，以及读取和写入属性的方法名称。
     *
     * @param propertyName 属性的程序名称。
     * @param beanClass 目标 Bean 的 Class 对象。例如 sun.beans.OurButton.class。
     * @param readMethodName 用于读取属性值的方法名称。如果属性是只写的，可以为 null。
     * @param writeMethodName 用于写入属性值的方法名称。如果属性是只读的，可以为 null。
     * @exception IntrospectionException 如果在内省过程中发生异常。
     */
    public PropertyDescriptor(String propertyName, Class<?> beanClass,
                String readMethodName, String writeMethodName)
                throws IntrospectionException {
        if (beanClass == null) {
            throw new IntrospectionException("目标 Bean 类为 null");
        }
        if (propertyName == null || propertyName.length() == 0) {
            throw new IntrospectionException("属性名称无效");
        }
        if ("".equals(readMethodName) || "".equals(writeMethodName)) {
            throw new IntrospectionException("读取或写入方法名称不应为空字符串");
        }
        setName(propertyName);
        setClass0(beanClass);

        this.readMethodName = readMethodName;
        if (readMethodName != null && getReadMethod() == null) {
            throw new IntrospectionException("未找到方法: " + readMethodName);
        }
        this.writeMethodName = writeMethodName;
        if (writeMethodName != null && getWriteMethod() == null) {
            throw new IntrospectionException("未找到方法: " + writeMethodName);
        }
        // 如果此类或其基类允许 PropertyChangeListener，则假设我们发现的任何属性都是“绑定的”。
        // 请参阅 Introspector.getTargetPropertyInfo() 方法。
        Class[] args = { PropertyChangeListener.class };
        this.bound = null != Introspector.findMethod(beanClass, "addPropertyChangeListener", args.length, args);
    }

    /**
     * 该构造函数接受简单属性的名称，以及用于读取和写入属性的 Method 对象。
     *
     * @param propertyName 属性的程序名称。
     * @param readMethod 用于读取属性值的方法。如果属性是只写的，可以为 null。
     * @param writeMethod 用于写入属性值的方法。如果属性是只读的，可以为 null。
     * @exception IntrospectionException 如果在内省过程中发生异常。
     */
    public PropertyDescriptor(String propertyName, Method readMethod, Method writeMethod)
                throws IntrospectionException {
        if (propertyName == null || propertyName.length() == 0) {
            throw new IntrospectionException("属性名称无效");
        }
        setName(propertyName);
        setReadMethod(readMethod);
        setWriteMethod(writeMethod);
    }

    /**
     * 为指定的 Bean 创建 PropertyDescriptor，并指定读取和写入属性值的方法。
     *
     * @param bean 目标 Bean 的类型
     * @param base 属性的基本名称（方法名的其余部分）
     * @param read 用于读取属性值的方法
     * @param write 用于写入属性值的方法
     * @exception IntrospectionException 如果在内省过程中发生异常
     *
     * @since 1.7
     */
    PropertyDescriptor(Class<?> bean, String base, Method read, Method write) throws IntrospectionException {
        if (bean == null) {
            throw new IntrospectionException("目标 Bean 类为 null");
        }
        setClass0(bean);
        setName(Introspector.decapitalize(base));
        setReadMethod(read);
        setWriteMethod(write);
        this.baseName = base;
    }

    /**
     * 返回属性的 Java 类型信息。
     * 注意，Class 对象可能描述原始 Java 类型，如 int。
     * 该类型由读取方法返回，或用作写入方法的参数类型。
     * 如果类型是不支持非索引访问的索引属性，则返回 null。
     *
     * @return 表示 Java 类型信息的 Class 对象，如果无法确定类型，则返回 null
     */
    public synchronized Class<?> getPropertyType() {
        Class<?> type = getPropertyType0();
        if (type  == null) {
            try {
                type = findPropertyType(getReadMethod(), getWriteMethod());
                setPropertyType(type);
            } catch (IntrospectionException ex) {
                // 忽略
            }
        }
        return type;
    }

    private void setPropertyType(Class<?> type) {
        this.propertyTypeRef = getWeakReference(type);
    }

    private Class<?> getPropertyType0() {
        return (this.propertyTypeRef != null)
                ? this.propertyTypeRef.get()
                : null;
    }

    /**
     * 获取用于读取属性值的方法。
     *
     * @return 应用于读取属性值的方法。如果属性不可读，则返回 null。
     */
    public synchronized Method getReadMethod() {
        Method readMethod = this.readMethodRef.get();
        if (readMethod == null) {
            Class<?> cls = getClass0();
            if (cls == null || (readMethodName == null && !this.readMethodRef.isSet())) {
                // 读取方法被显式设置为 null。
                return null;
            }
            String nextMethodName = Introspector.GET_PREFIX + getBaseName();
            if (readMethodName == null) {
                Class<?> type = getPropertyType0();
                if (type == boolean.class || type == null) {
                    readMethodName = Introspector.IS_PREFIX + getBaseName();
                } else {
                    readMethodName = nextMethodName;
                }
            }

            // 由于可以有多个写入方法，但只有一个 getter 方法，因此首先找到 getter 方法，以便知道属性类型。
            // 对于布尔值，可以有“is”和“get”方法。如果存在“is”方法，则这是官方的读取方法，因此首先查找此方法。
            readMethod = Introspector.findMethod(cls, readMethodName, 0);
            if ((readMethod == null) && !readMethodName.equals(nextMethodName)) {
                readMethodName = nextMethodName;
                readMethod = Introspector.findMethod(cls, readMethodName, 0);
            }
            try {
                setReadMethod(readMethod);
            } catch (IntrospectionException ex) {
                // 忽略
            }
        }
        return readMethod;
    }

    /**
     * 设置用于读取属性值的方法。
     *
     * @param readMethod 新的读取方法。
     * @throws IntrospectionException 如果读取方法无效
     */
    public synchronized void setReadMethod(Method readMethod)
                                throws IntrospectionException {
        this.readMethodRef.set(readMethod);
        if (readMethod == null) {
            readMethodName = null;
            return;
        }
        // 属性类型由读取方法确定。
        setPropertyType(findPropertyType(readMethod, this.writeMethodRef.get()));
        setClass0(readMethod.getDeclaringClass());

        readMethodName = readMethod.getName();
        setTransient(readMethod.getAnnotation(Transient.class));
    }

    /**
     * 获取用于写入属性值的方法。
     *
     * @return 应用于写入属性值的方法。如果属性不可写，则返回 null。
     */
    public synchronized Method getWriteMethod() {
        Method writeMethod = this.writeMethodRef.get();
        if (writeMethod == null) {
            Class<?> cls = getClass0();
            if (cls == null || (writeMethodName == null && !this.writeMethodRef.isSet())) {
                // 写入方法被显式设置为 null。
                return null;
            }

            // 需要知道类型以获取正确的方法。
            Class<?> type = getPropertyType0();
            if (type == null) {
                try {
                    // 不能使用 getPropertyType，因为它会导致递归循环。
                    type = findPropertyType(getReadMethod(), null);
                    setPropertyType(type);
                } catch (IntrospectionException ex) {
                    // 没有正确的属性类型，无法保证找到正确的方法。
                    return null;
                }
            }

            if (writeMethodName == null) {
                writeMethodName = Introspector.SET_PREFIX + getBaseName();
            }

            Class<?>[] args = (type == null) ? null : new Class<?>[] { type };
            writeMethod = Introspector.findMethod(cls, writeMethodName, 1, args);
            if (writeMethod != null) {
                if (!writeMethod.getReturnType().equals(void.class)) {
                    writeMethod = null;
                }
            }
            try {
                setWriteMethod(writeMethod);
            } catch (IntrospectionException ex) {
                // 忽略
            }
        }
        return writeMethod;
    }

    /**
     * 设置用于写入属性值的方法。
     *
     * @param writeMethod 新的写入方法。
     * @throws IntrospectionException 如果写入方法无效
     */
    public synchronized void setWriteMethod(Method writeMethod)
                                throws IntrospectionException {
        this.writeMethodRef.set(writeMethod);
        if (writeMethod == null) {
            writeMethodName = null;
            return;
        }
        // 设置属性类型 - 验证方法
        setPropertyType(findPropertyType(getReadMethod(), writeMethod));
        setClass0(writeMethod.getDeclaringClass());

        writeMethodName = writeMethod.getName();
        setTransient(writeMethod.getAnnotation(Transient.class));
    }

    /**
     * 覆盖以确保超类不会优先。
     */
    void setClass0(Class<?> clz) {
        if (getClass0() != null && clz.isAssignableFrom(getClass0())) {
            // 不要用超类替换子类
            return;
        }
        super.setClass0(clz);
    }

    /**
     * “绑定”属性的更新将在属性更改时触发“PropertyChange”事件。
     *
     * @return 如果这是绑定属性，则返回 true。
     */
    public boolean isBound() {
        return bound;
    }

    /**
     * “绑定”属性的更新将在属性更改时触发“PropertyChange”事件。
     *
     * @param bound 如果这是绑定属性，则为 true。
     */
    public void setBound(boolean bound) {
        this.bound = bound;
    }

    /**
     * 尝试更新“受约束”属性将在属性更改时触发“VetoableChange”事件。
     *
     * @return 如果这是受约束属性，则返回 true。
     */
    public boolean isConstrained() {
        return constrained;
    }

    /**
     * 尝试更新“受约束”属性将在属性更改时触发“VetoableChange”事件。
     *
     * @param constrained 如果这是受约束属性，则为 true。
     */
    public void setConstrained(boolean constrained) {
        this.constrained = constrained;
    }


    /**
     * 通常情况下，PropertyEditors 会通过 PropertyEditorManager 找到。
     * 但是，如果出于某种原因，您希望将特定的 PropertyEditor 与给定属性关联，
     * 则可以使用此方法。
     *
     * @param propertyEditorClass  所需 PropertyEditor 的类。
     */
    public void setPropertyEditorClass(Class<?> propertyEditorClass) {
        this.propertyEditorClassRef = getWeakReference(propertyEditorClass);
    }

    /**
     * 获取为此属性注册的任何显式 PropertyEditor 类。
     *
     * @return 为此属性注册的任何显式 PropertyEditor 类。通常这将返回 "null"，
     *         表示没有注册特殊编辑器，因此应使用 PropertyEditorManager 来查找
     *         适合的 PropertyEditor。
     */
    public Class<?> getPropertyEditorClass() {
        return (this.propertyEditorClassRef != null)
                ? this.propertyEditorClassRef.get()
                : null;
    }

    /**
     * 使用当前的属性编辑器类构造属性编辑器的实例。
     * <p>
     * 如果属性编辑器类有一个接受 Object 参数的公共构造函数，则将使用 bean 参数作为参数调用它。
     * 否则，将调用默认构造函数。
     *
     * @param bean 源对象
     * @return 属性编辑器实例，如果没有定义属性编辑器或无法创建，则返回 null
     * @since 1.5
     */
    public PropertyEditor createPropertyEditor(Object bean) {
        Object editor = null;

        final Class<?> cls = getPropertyEditorClass();
        if (cls != null && PropertyEditor.class.isAssignableFrom(cls)
                && ReflectUtil.isPackageAccessible(cls)) {
            Constructor<?> ctor = null;
            if (bean != null) {
                try {
                    ctor = cls.getConstructor(new Class<?>[] { Object.class });
                } catch (Exception ex) {
                    // Fall through
                }
            }
            try {
                if (ctor == null) {
                    editor = cls.newInstance();
                } else {
                    editor = ctor.newInstance(new Object[] { bean });
                }
            } catch (Exception ex) {
                // Fall through
            }
        }
        return (PropertyEditor)editor;
    }


    /**
     * 将此 <code>PropertyDescriptor</code> 与指定对象进行比较。
     * 如果对象相同，则返回 true。两个 <code>PropertyDescriptor</code> 相同，
     * 如果它们的读取、写入、属性类型、属性编辑器和标志等价。
     *
     * @since 1.4
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && obj instanceof PropertyDescriptor) {
            PropertyDescriptor other = (PropertyDescriptor)obj;
            Method otherReadMethod = other.getReadMethod();
            Method otherWriteMethod = other.getWriteMethod();

            if (!compareMethods(getReadMethod(), otherReadMethod)) {
                return false;
            }

            if (!compareMethods(getWriteMethod(), otherWriteMethod)) {
                return false;
            }

            if (getPropertyType() == other.getPropertyType() &&
                getPropertyEditorClass() == other.getPropertyEditorClass() &&
                bound == other.isBound() && constrained == other.isConstrained() &&
                writeMethodName == other.writeMethodName &&
                readMethodName == other.readMethodName) {
                return true;
            }
        }
        return false;
    }

    /**
     * 包私有的辅助方法，用于 Descriptor .equals 方法。
     *
     * @param a 第一个要比较的方法
     * @param b 第二个要比较的方法
     * @return 布尔值，表示这两个方法是否等价
     */
    boolean compareMethods(Method a, Method b) {
        // 注意：这可能是 FeatureDescriptor 中的一个受保护的方法
        if ((a == null) != (b == null)) {
            return false;
        }

        if (a != null && b != null) {
            if (!a.equals(b)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 包私有的构造函数。
     * 合并两个属性描述符。如果它们冲突，给第二个参数 (y) 优先于第一个参数 (x)。
     *
     * @param x  第一个（优先级较低的）PropertyDescriptor
     * @param y  第二个（优先级较高的）PropertyDescriptor
     */
    PropertyDescriptor(PropertyDescriptor x, PropertyDescriptor y) {
        super(x,y);

        if (y.baseName != null) {
            baseName = y.baseName;
        } else {
            baseName = x.baseName;
        }

        if (y.readMethodName != null) {
            readMethodName = y.readMethodName;
        } else {
            readMethodName = x.readMethodName;
        }

        if (y.writeMethodName != null) {
            writeMethodName = y.writeMethodName;
        } else {
            writeMethodName = x.writeMethodName;
        }

        if (y.propertyTypeRef != null) {
            propertyTypeRef = y.propertyTypeRef;
        } else {
            propertyTypeRef = x.propertyTypeRef;
        }

        // 确定合并后的读取方法。
        Method xr = x.getReadMethod();
        Method yr = y.getReadMethod();

        // 通常给 y 的读取方法优先级。
        try {
            if (isAssignable(xr, yr)) {
                setReadMethod(yr);
            } else {
                setReadMethod(xr);
            }
        } catch (IntrospectionException ex) {
            // fall through
        }

        // 但是，如果 x 和 y 引用的读取方法在同一个类中，
        // 给布尔 "is" 方法优先于布尔 "get" 方法。
        if (xr != null && yr != null &&
                   xr.getDeclaringClass() == yr.getDeclaringClass() &&
                   getReturnType(getClass0(), xr) == boolean.class &&
                   getReturnType(getClass0(), yr) == boolean.class &&
                   xr.getName().indexOf(Introspector.IS_PREFIX) == 0 &&
                   yr.getName().indexOf(Introspector.GET_PREFIX) == 0) {
            try {
                setReadMethod(xr);
            } catch (IntrospectionException ex) {
                // fall through
            }
        }

        Method xw = x.getWriteMethod();
        Method yw = y.getWriteMethod();

        try {
            if (yw != null) {
                setWriteMethod(yw);
            } else {
                setWriteMethod(xw);
            }
        } catch (IntrospectionException ex) {
            // Fall through
        }

        if (y.getPropertyEditorClass() != null) {
            setPropertyEditorClass(y.getPropertyEditorClass());
        } else {
            setPropertyEditorClass(x.getPropertyEditorClass());
        }


        bound = x.bound | y.bound;
        constrained = x.constrained | y.constrained;
    }

    /*
     * 包私有的复制构造函数。
     * 这必须使新对象与旧对象的任何更改隔离。
     */
    PropertyDescriptor(PropertyDescriptor old) {
        super(old);
        propertyTypeRef = old.propertyTypeRef;
        this.readMethodRef.set(old.readMethodRef.get());
        this.writeMethodRef.set(old.writeMethodRef.get());
        propertyEditorClassRef = old.propertyEditorClassRef;

        writeMethodName = old.writeMethodName;
        readMethodName = old.readMethodName;
        baseName = old.baseName;

        bound = old.bound;
        constrained = old.constrained;
    }

    void updateGenericsFor(Class<?> type) {
        setClass0(type);
        try {
            setPropertyType(findPropertyType(this.readMethodRef.get(), this.writeMethodRef.get()));
        }
        catch (IntrospectionException exception) {
            setPropertyType(null);
        }
    }

    /**
     * 返回与读取和写入方法对应的属性类型。
     * 类型优先级给予读取方法。
     *
     * @return 属性描述符的类型，如果读取和写入方法都为 null，则返回 null。
     * @throws IntrospectionException 如果读取或写入方法无效
     */
    private Class<?> findPropertyType(Method readMethod, Method writeMethod)
        throws IntrospectionException {
        Class<?> propertyType = null;
        try {
            if (readMethod != null) {
                Class<?>[] params = getParameterTypes(getClass0(), readMethod);
                if (params.length != 0) {
                    throw new IntrospectionException("bad read method arg count: "
                                                     + readMethod);
                }
                propertyType = getReturnType(getClass0(), readMethod);
                if (propertyType == Void.TYPE) {
                    throw new IntrospectionException("read method " +
                                        readMethod.getName() + " returns void");
                }
            }
            if (writeMethod != null) {
                Class<?>[] params = getParameterTypes(getClass0(), writeMethod);
                if (params.length != 1) {
                    throw new IntrospectionException("bad write method arg count: "
                                                     + writeMethod);
                }
                if (propertyType != null && !params[0].isAssignableFrom(propertyType)) {
                    throw new IntrospectionException("type mismatch between read and write methods");
                }
                propertyType = params[0];
            }
        } catch (IntrospectionException ex) {
            throw ex;
        }
        return propertyType;
    }


    /**
     * 返回此对象的哈希码值。
     * 有关完整描述，请参见 {@link java.lang.Object#hashCode}。
     *
     * @return 此对象的哈希码值。
     * @since 1.5
     */
    public int hashCode() {
        int result = 7;

        result = 37 * result + ((getPropertyType() == null) ? 0 :
                                getPropertyType().hashCode());
        result = 37 * result + ((getReadMethod() == null) ? 0 :
                                getReadMethod().hashCode());
        result = 37 * result + ((getWriteMethod() == null) ? 0 :
                                getWriteMethod().hashCode());
        result = 37 * result + ((getPropertyEditorClass() == null) ? 0 :
                                getPropertyEditorClass().hashCode());
        result = 37 * result + ((writeMethodName == null) ? 0 :
                                writeMethodName.hashCode());
        result = 37 * result + ((readMethodName == null) ? 0 :
                                readMethodName.hashCode());
        result = 37 * result + getName().hashCode();
        result = 37 * result + ((bound == false) ? 0 : 1);
        result = 37 * result + ((constrained == false) ? 0 : 1);

        return result;
    }

    // 由于 capitalize() 开销较大，因此只计算一次。
    String getBaseName() {
        if (baseName == null) {
            baseName = NameGenerator.capitalize(getName());
        }
        return baseName;
    }

    void appendTo(StringBuilder sb) {
        appendTo(sb, "bound", this.bound);
        appendTo(sb, "constrained", this.constrained);
        appendTo(sb, "propertyEditorClass", this.propertyEditorClassRef);
        appendTo(sb, "propertyType", this.propertyTypeRef);
        appendTo(sb, "readMethod", this.readMethodRef.get());
        appendTo(sb, "writeMethod", this.writeMethodRef.get());
    }

    boolean isAssignable(Method m1, Method m2) {
        if (m1 == null) {
            return true; // 选择第二个方法
        }
        if (m2 == null) {
            return false; // 选择第一个方法
        }
        if (!m1.getName().equals(m2.getName())) {
            return true; // 默认选择第二个方法
        }
        Class<?> type1 = m1.getDeclaringClass();
        Class<?> type2 = m2.getDeclaringClass();
        if (!type1.isAssignableFrom(type2)) {
            return false; // 选择第一个方法：它声明得更晚
        }
        type1 = getReturnType(getClass0(), m1);
        type2 = getReturnType(getClass0(), m2);
        if (!type1.isAssignableFrom(type2)) {
            return false; // 选择第一个方法：它覆盖了返回类型
        }
        Class<?>[] args1 = getParameterTypes(getClass0(), m1);
        Class<?>[] args2 = getParameterTypes(getClass0(), m2);
        if (args1.length != args2.length) {
            return true; // 默认选择第二个方法
        }
        for (int i = 0; i < args1.length; i++) {
            if (!args1[i].isAssignableFrom(args2[i])) {
                return false; // 选择第一个方法：它覆盖了参数
            }
        }
        return true; // 选择第二个方法
    }
}
