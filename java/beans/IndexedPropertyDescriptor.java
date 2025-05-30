
/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

/**
 * IndexedPropertyDescriptor 描述了一个行为类似于数组的属性，并且具有索引读取和/或索引写入方法来访问数组中的特定元素。
 * <p>
 * 索引属性也可以提供简单的非索引读取和写入方法。如果这些方法存在，它们读取和写入由索引读取方法返回的数组类型。
 */

public class IndexedPropertyDescriptor extends PropertyDescriptor {

    private Reference<? extends Class<?>> indexedPropertyTypeRef;
    private final MethodRef indexedReadMethodRef = new MethodRef();
    private final MethodRef indexedWriteMethodRef = new MethodRef();

    private String indexedReadMethodName;
    private String indexedWriteMethodName;

    /**
     * 此构造函数为遵循标准 Java 约定的属性构建 IndexedPropertyDescriptor，该约定具有 getFoo 和 setFoo 访问器方法，用于索引访问和数组访问。
     * <p>
     * 因此，如果参数名称为 "fred"，它将假设存在一个索引读取方法 "getFred"，一个非索引（数组）读取方法也称为 "getFred"，一个索引写入方法 "setFred"，
     * 以及最后一个是非索引写入方法 "setFred"。
     *
     * @param propertyName 属性的程序名称。
     * @param beanClass 目标 bean 的 Class 对象。
     * @exception IntrospectionException 如果在内省过程中发生异常。
     */
    public IndexedPropertyDescriptor(String propertyName, Class<?> beanClass)
                throws IntrospectionException {
        this(propertyName, beanClass,
             Introspector.GET_PREFIX + NameGenerator.capitalize(propertyName),
             Introspector.SET_PREFIX + NameGenerator.capitalize(propertyName),
             Introspector.GET_PREFIX + NameGenerator.capitalize(propertyName),
             Introspector.SET_PREFIX + NameGenerator.capitalize(propertyName));
    }

    /**
     * 此构造函数接受一个简单属性的名称，以及读取和写入该属性的方法名称，包括索引和非索引。
     *
     * @param propertyName 属性的程序名称。
     * @param beanClass  目标 bean 的 Class 对象。
     * @param readMethodName 用于读取属性值作为数组的方法名称。如果属性是只写或必须索引，则可以为 null。
     * @param writeMethodName 用于写入属性值作为数组的方法名称。如果属性是只读或必须索引，则可以为 null。
     * @param indexedReadMethodName 用于读取索引属性值的方法名称。如果属性是只写，则可以为 null。
     * @param indexedWriteMethodName 用于写入索引属性值的方法名称。如果属性是只读，则可以为 null。
     * @exception IntrospectionException 如果在内省过程中发生异常。
     */
    public IndexedPropertyDescriptor(String propertyName, Class<?> beanClass,
                String readMethodName, String writeMethodName,
                String indexedReadMethodName, String indexedWriteMethodName)
                throws IntrospectionException {
        super(propertyName, beanClass, readMethodName, writeMethodName);

        this.indexedReadMethodName = indexedReadMethodName;
        if (indexedReadMethodName != null && getIndexedReadMethod() == null) {
            throw new IntrospectionException("Method not found: " + indexedReadMethodName);
        }

        this.indexedWriteMethodName = indexedWriteMethodName;
        if (indexedWriteMethodName != null && getIndexedWriteMethod() == null) {
            throw new IntrospectionException("Method not found: " + indexedWriteMethodName);
        }
        // 仅用于类型检查。
        findIndexedPropertyType(getIndexedReadMethod(), getIndexedWriteMethod());
    }

    /**
     * 此构造函数接受一个简单属性的名称，以及用于读取和写入该属性的方法对象。
     *
     * @param propertyName 属性的程序名称。
     * @param readMethod 用于读取属性值作为数组的方法。如果属性是只写或必须索引，则可以为 null。
     * @param writeMethod 用于写入属性值作为数组的方法。如果属性是只读或必须索引，则可以为 null。
     * @param indexedReadMethod 用于读取索引属性值的方法。如果属性是只写，则可以为 null。
     * @param indexedWriteMethod 用于写入索引属性值的方法。如果属性是只读，则可以为 null。
     * @exception IntrospectionException 如果在内省过程中发生异常。
     */
    public IndexedPropertyDescriptor(String propertyName, Method readMethod, Method writeMethod,
                                            Method indexedReadMethod, Method indexedWriteMethod)
                throws IntrospectionException {
        super(propertyName, readMethod, writeMethod);

        setIndexedReadMethod0(indexedReadMethod);
        setIndexedWriteMethod0(indexedWriteMethod);

        // 类型检查
        setIndexedPropertyType(findIndexedPropertyType(indexedReadMethod, indexedWriteMethod));
    }

    /**
     * 为指定的 bean 创建 PropertyDescriptor，指定名称和读取/写入属性值的方法。
     *
     * @param bean          目标 bean 的类型
     * @param base          属性的基本名称（方法名称的其余部分）
     * @param read          用于读取属性值的方法
     * @param write         用于写入属性值的方法
     * @param readIndexed   用于读取索引属性值的方法
     * @param writeIndexed  用于写入索引属性值的方法
     * @exception IntrospectionException 如果在内省过程中发生异常
     *
     * @since 1.7
     */
    IndexedPropertyDescriptor(Class<?> bean, String base, Method read, Method write, Method readIndexed, Method writeIndexed) throws IntrospectionException {
        super(bean, base, read, write);

        setIndexedReadMethod0(readIndexed);
        setIndexedWriteMethod0(writeIndexed);

        // 类型检查
        setIndexedPropertyType(findIndexedPropertyType(readIndexed, writeIndexed));
    }

    /**
     * 获取应用于读取索引属性值的方法。
     *
     * @return 应用于读取索引属性值的方法。
     * 如果属性不是索引或只写，则可能返回 null。
     */
    public synchronized Method getIndexedReadMethod() {
        Method indexedReadMethod = this.indexedReadMethodRef.get();
        if (indexedReadMethod == null) {
            Class<?> cls = getClass0();
            if (cls == null ||
                (indexedReadMethodName == null && !this.indexedReadMethodRef.isSet())) {
                // 索引读取方法被显式设置为 null。
                return null;
            }
            String nextMethodName = Introspector.GET_PREFIX + getBaseName();
            if (indexedReadMethodName == null) {
                Class<?> type = getIndexedPropertyType0();
                if (type == boolean.class || type == null) {
                    indexedReadMethodName = Introspector.IS_PREFIX + getBaseName();
                } else {
                    indexedReadMethodName = nextMethodName;
                }
            }

            Class<?>[] args = { int.class };
            indexedReadMethod = Introspector.findMethod(cls, indexedReadMethodName, 1, args);
            if ((indexedReadMethod == null) && !indexedReadMethodName.equals(nextMethodName)) {
                // 没有 "is" 方法，所以查找 "get" 方法。
                indexedReadMethodName = nextMethodName;
                indexedReadMethod = Introspector.findMethod(cls, indexedReadMethodName, 1, args);
            }
            setIndexedReadMethod0(indexedReadMethod);
        }
        return indexedReadMethod;
    }

    /**
     * 设置应用于读取索引属性值的方法。
     *
     * @param readMethod 新的索引读取方法。
     * @throws IntrospectionException 如果在内省过程中发生异常。
     */
    public synchronized void setIndexedReadMethod(Method readMethod)
        throws IntrospectionException {

        // 索引属性类型由读取器设置。
        setIndexedPropertyType(findIndexedPropertyType(readMethod,
                                                       this.indexedWriteMethodRef.get()));
        setIndexedReadMethod0(readMethod);
    }

    private void setIndexedReadMethod0(Method readMethod) {
        this.indexedReadMethodRef.set(readMethod);
        if (readMethod == null) {
            indexedReadMethodName = null;
            return;
        }
        setClass0(readMethod.getDeclaringClass());

        indexedReadMethodName = readMethod.getName();
        setTransient(readMethod.getAnnotation(Transient.class));
    }


    /**
     * 获取应应用于写入索引属性值的方法。
     *
     * @return 应应用于写入索引属性值的方法。
     * 如果属性不是索引或只读，则可能返回 null。
     */
    public synchronized Method getIndexedWriteMethod() {
        Method indexedWriteMethod = this.indexedWriteMethodRef.get();
        if (indexedWriteMethod == null) {
            Class<?> cls = getClass0();
            if (cls == null ||
                (indexedWriteMethodName == null && !this.indexedWriteMethodRef.isSet())) {
                // 索引写入方法被显式设置为 null。
                return null;
            }

            // 需要索引类型以确保获取正确的方法。
            // 不能使用 getIndexedPropertyType 方法，因为这可能导致无限循环。
            Class<?> type = getIndexedPropertyType0();
            if (type == null) {
                try {
                    type = findIndexedPropertyType(getIndexedReadMethod(), null);
                    setIndexedPropertyType(type);
                } catch (IntrospectionException ex) {
                    // 将 iprop 类型设置为经典类型
                    Class<?> propType = getPropertyType();
                    if (propType.isArray()) {
                        type = propType.getComponentType();
                    }
                }
            }

            if (indexedWriteMethodName == null) {
                indexedWriteMethodName = Introspector.SET_PREFIX + getBaseName();
            }

            Class<?>[] args = (type == null) ? null : new Class<?>[] { int.class, type };
            indexedWriteMethod = Introspector.findMethod(cls, indexedWriteMethodName, 2, args);
            if (indexedWriteMethod != null) {
                if (!indexedWriteMethod.getReturnType().equals(void.class)) {
                    indexedWriteMethod = null;
                }
            }
            setIndexedWriteMethod0(indexedWriteMethod);
        }
        return indexedWriteMethod;
    }

    /**
     * 设置应应用于写入索引属性值的方法。
     *
     * @param writeMethod 新的索引写入方法。
     * @throws IntrospectionException 如果在内省过程中发生异常。
     */
    public synchronized void setIndexedWriteMethod(Method writeMethod)
        throws IntrospectionException {

        // 如果索引属性类型尚未设置，则设置它。
        Class<?> type = findIndexedPropertyType(getIndexedReadMethod(),
                                             writeMethod);
        setIndexedPropertyType(type);
        setIndexedWriteMethod0(writeMethod);
    }

    private void setIndexedWriteMethod0(Method writeMethod) {
        this.indexedWriteMethodRef.set(writeMethod);
        if (writeMethod == null) {
            indexedWriteMethodName = null;
            return;
        }
        setClass0(writeMethod.getDeclaringClass());

        indexedWriteMethodName = writeMethod.getName();
        setTransient(writeMethod.getAnnotation(Transient.class));
    }

    /**
     * 返回索引属性的 Java 类型信息。
     * 注意，{@code Class} 对象可能描述原始 Java 类型，如 {@code int}。
     * 此类型由索引读取方法返回，或用作索引写入方法的参数类型。
     *
     * @return 代表 Java 类型信息的 {@code Class} 对象，如果无法确定类型，则返回 {@code null}。
     */
    public synchronized Class<?> getIndexedPropertyType() {
        Class<?> type = getIndexedPropertyType0();
        if (type == null) {
            try {
                type = findIndexedPropertyType(getIndexedReadMethod(),
                                               getIndexedWriteMethod());
                setIndexedPropertyType(type);
            } catch (IntrospectionException ex) {
                // fall
            }
        }
        return type;
    }

    // 私有方法，用于设置和获取 Reference 对象

    private void setIndexedPropertyType(Class<?> type) {
        this.indexedPropertyTypeRef = getWeakReference(type);
    }

    private Class<?> getIndexedPropertyType0() {
        return (this.indexedPropertyTypeRef != null)
                ? this.indexedPropertyTypeRef.get()
                : null;
    }

    private Class<?> findIndexedPropertyType(Method indexedReadMethod,
                                          Method indexedWriteMethod)
        throws IntrospectionException {
        Class<?> indexedPropertyType = null;


        if (indexedReadMethod != null) {
            Class params[] = getParameterTypes(getClass0(), indexedReadMethod);
            if (params.length != 1) {
                throw new IntrospectionException("索引读取方法参数数量错误");
            }
            if (params[0] != Integer.TYPE) {
                throw new IntrospectionException("索引读取方法的索引不是整数类型");
            }
            indexedPropertyType = getReturnType(getClass0(), indexedReadMethod);
            if (indexedPropertyType == Void.TYPE) {
                throw new IntrospectionException("索引读取方法返回值为void");
            }
        }
        if (indexedWriteMethod != null) {
            Class params[] = getParameterTypes(getClass0(), indexedWriteMethod);
            if (params.length != 2) {
                throw new IntrospectionException("索引写入方法参数数量错误");
            }
            if (params[0] != Integer.TYPE) {
                throw new IntrospectionException("索引写入方法的索引不是整数类型");
            }
            if (indexedPropertyType == null || params[1].isAssignableFrom(indexedPropertyType)) {
                indexedPropertyType = params[1];
            } else if (!indexedPropertyType.isAssignableFrom(params[1])) {
                throw new IntrospectionException(
                                                 "索引读取和索引写入方法之间的类型不匹配: "
                                                 + getName());
            }
        }
        Class<?> propertyType = getPropertyType();
        if (propertyType != null && (!propertyType.isArray() ||
                                     propertyType.getComponentType() != indexedPropertyType)) {
            throw new IntrospectionException("索引和非索引方法之间的类型不匹配: "
                                             + getName());
        }
        return indexedPropertyType;
    }

    /**
     * 将此 <code>PropertyDescriptor</code> 与指定对象进行比较。
     * 如果对象相同，则返回 true。两个 <code>PropertyDescriptor</code> 相同的条件是读取、写入、属性类型、属性编辑器和标志都等价。
     *
     * @since 1.4
     */
    public boolean equals(Object obj) {
        // 注意：这与 PropertyDescriptor 完全相同，但它们没有共享相同的字段。
        if (this == obj) {
            return true;
        }

        if (obj != null && obj instanceof IndexedPropertyDescriptor) {
            IndexedPropertyDescriptor other = (IndexedPropertyDescriptor)obj;
            Method otherIndexedReadMethod = other.getIndexedReadMethod();
            Method otherIndexedWriteMethod = other.getIndexedWriteMethod();

            if (!compareMethods(getIndexedReadMethod(), otherIndexedReadMethod)) {
                return false;
            }

            if (!compareMethods(getIndexedWriteMethod(), otherIndexedWriteMethod)) {
                return false;
            }

            if (getIndexedPropertyType() != other.getIndexedPropertyType()) {
                return false;
            }
            return super.equals(obj);
        }
        return false;
    }

    /**
     * 包私有构造函数。
     * 合并两个属性描述符。当它们冲突时，给第二个参数 (y) 优先权高于第一个参数 (x)。
     *
     * @param x  第一个（优先级较低）的 PropertyDescriptor
     * @param y  第二个（优先级较高）的 PropertyDescriptor
     */

    IndexedPropertyDescriptor(PropertyDescriptor x, PropertyDescriptor y) {
        super(x,y);
        Method tr = null;
        Method tw = null;

        if (x instanceof IndexedPropertyDescriptor) {
            IndexedPropertyDescriptor ix = (IndexedPropertyDescriptor) x;
            tr = ix.getIndexedReadMethod();
            tw = ix.getIndexedWriteMethod();
        }
        if (y instanceof IndexedPropertyDescriptor) {
            IndexedPropertyDescriptor iy = (IndexedPropertyDescriptor) y;
            Method yr = iy.getIndexedReadMethod();
            if (isAssignable(tr, yr)) {
                tr = yr;
            }

            Method yw = iy.getIndexedWriteMethod();
            if (isAssignable(tw, yw)) {
                tw = yw;
            }
        }

        try {
            if(tr != null) {
                setIndexedReadMethod(tr);
            }
            if(tw != null) {
                setIndexedWriteMethod(tw);
            }
        } catch(IntrospectionException ex) {
            // 不应该发生
            throw new AssertionError(ex);
        }
    }

    /*
     * 包私有复制构造函数
     * 必须使新对象与旧对象的任何更改隔离。
     */
    IndexedPropertyDescriptor(IndexedPropertyDescriptor old) {
        super(old);
        this.indexedReadMethodRef.set(old.indexedReadMethodRef.get());
        this.indexedWriteMethodRef.set(old.indexedWriteMethodRef.get());
        indexedPropertyTypeRef = old.indexedPropertyTypeRef;
        indexedWriteMethodName = old.indexedWriteMethodName;
        indexedReadMethodName = old.indexedReadMethodName;
    }

    void updateGenericsFor(Class<?> type) {
        super.updateGenericsFor(type);
        try {
            setIndexedPropertyType(findIndexedPropertyType(this.indexedReadMethodRef.get(), this.indexedWriteMethodRef.get()));
        }
        catch (IntrospectionException exception) {
            setIndexedPropertyType(null);
        }
    }

    /**
     * 返回此对象的哈希码值。
     * 请参阅 {@link java.lang.Object#hashCode} 以获取完整描述。
     *
     * @return 此对象的哈希码值。
     * @since 1.5
     */
    public int hashCode() {
        int result = super.hashCode();

        result = 37 * result + ((indexedWriteMethodName == null) ? 0 :
                                indexedWriteMethodName.hashCode());
        result = 37 * result + ((indexedReadMethodName == null) ? 0 :
                                indexedReadMethodName.hashCode());
        result = 37 * result + ((getIndexedPropertyType() == null) ? 0 :
                                getIndexedPropertyType().hashCode());

        return result;
    }

    void appendTo(StringBuilder sb) {
        super.appendTo(sb);
        appendTo(sb, "indexedPropertyType", this.indexedPropertyTypeRef);
        appendTo(sb, "indexedReadMethod", this.indexedReadMethodRef.get());
        appendTo(sb, "indexedWriteMethod", this.indexedWriteMethodRef.get());
    }
}
