/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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
package java.lang.reflect;

import java.lang.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import sun.reflect.annotation.AnnotationSupport;

/**
 * 方法参数的信息。
 *
 * {@code Parameter} 提供了关于方法参数的信息，包括其名称和修饰符。它还提供了另一种获取参数属性的方法。
 *
 * @since 1.8
 */
public final class Parameter implements AnnotatedElement {

    private final String name;
    private final int modifiers;
    private final Executable executable;
    private final int index;

    /**
     * 包私有的 {@code Parameter} 构造函数。
     *
     * 如果类文件中存在方法参数数据，则 JVM 直接创建 {@code Parameter} 对象。如果不存在，则 {@code Executable} 使用此构造函数来合成它们。
     *
     * @param name 参数的名称。
     * @param modifiers 参数的修饰符标志。
     * @param executable 定义此参数的可执行对象。
     * @param index 参数的索引。
     */
    Parameter(String name,
              int modifiers,
              Executable executable,
              int index) {
        this.name = name;
        this.modifiers = modifiers;
        this.executable = executable;
        this.index = index;
    }

    /**
     * 基于可执行对象和索引进行比较。
     *
     * @param obj 要比较的对象。
     * @return 是否与此参数相等。
     */
    public boolean equals(Object obj) {
        if(obj instanceof Parameter) {
            Parameter other = (Parameter)obj;
            return (other.executable.equals(executable) &&
                    other.index == index);
        }
        return false;
    }

    /**
     * 返回基于可执行对象的哈希码和索引的哈希码。
     *
     * @return 基于可执行对象的哈希码。
     */
    public int hashCode() {
        return executable.hashCode() ^ index;
    }

    /**
     * 如果参数在类文件中具有名称，则返回 true；否则返回 false。参数是否具有名称由声明该参数的方法的 {@literal MethodParameters} 属性决定。
     *
     * @return 如果参数在类文件中具有名称，则返回 true。
     */
    public boolean isNamePresent() {
        return executable.hasRealParameterData() && name != null;
    }

    /**
     * 返回描述此参数的字符串。格式为参数的修饰符（如果有），按照《Java&trade; 语言规范》推荐的规范顺序，后跟参数的完全限定类型（如果参数是可变参数，则排除最后一个 []），如果参数是可变参数，则后跟 "..."，再后跟一个空格，最后是参数的名称。
     *
     * @return 参数及其相关信息的字符串表示。
     */
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final Type type = getParameterizedType();
        final String typename = type.getTypeName();

        sb.append(Modifier.toString(getModifiers()));

        if(0 != modifiers)
            sb.append(' ');

        if(isVarArgs())
            sb.append(typename.replaceFirst("\\[\\]$", "..."));
        else
            sb.append(typename);

        sb.append(' ');
        sb.append(getName());

        return sb.toString();
    }

    /**
     * 返回声明此参数的 {@code Executable}。
     *
     * @return 声明此参数的 {@code Executable}。
     */
    public Executable getDeclaringExecutable() {
        return executable;
    }

    /**
     * 获取此 {@code Parameter} 对象表示的参数的修饰符标志。
     *
     * @return 此参数的修饰符标志。
     */
    public int getModifiers() {
        return modifiers;
    }

    /**
     * 返回参数的名称。如果参数的名称 {@linkplain #isNamePresent() 存在}，则此方法返回类文件提供的名称。否则，此方法合成一个形式为 argN 的名称，其中 N 是参数在声明该参数的方法描述符中的索引。
     *
     * @return 参数的名称，由类文件提供或如果类文件未提供名称则合成。
     */
    public String getName() {
        // 注意：空字符串作为参数名称现在是非法的。
        // .equals("") 是为了与当前 JVM 的行为兼容。将来可能会移除。
        if(name == null || name.equals(""))
            return "arg" + index;
        else
            return name;
    }

    // 包私有的访问器，用于获取实际名称字段。
    String getRealName() {
        return name;
    }

    /**
     * 返回一个 {@code Type} 对象，标识此 {@code Parameter} 对象表示的参数的参数化类型。
     *
     * @return 一个 {@code Type} 对象，标识此对象表示的参数的参数化类型。
     */
    public Type getParameterizedType() {
        Type tmp = parameterTypeCache;
        if (null == tmp) {
            tmp = executable.getAllGenericParameterTypes()[index];
            parameterTypeCache = tmp;
        }

        return tmp;
    }

    private transient volatile Type parameterTypeCache = null;

    /**
     * 返回一个 {@code Class} 对象，标识此 {@code Parameter} 对象表示的参数的声明类型。
     *
     * @return 一个 {@code Class} 对象，标识此对象表示的参数的声明类型。
     */
    public Class<?> getType() {
        Class<?> tmp = parameterClassCache;
        if (null == tmp) {
            tmp = executable.getParameterTypes()[index];
            parameterClassCache = tmp;
        }
        return tmp;
    }

    /**
     * 返回一个 AnnotatedType 对象，表示使用类型来指定此 Parameter 表示的形式参数的类型。
     *
     * @return 一个 {@code AnnotatedType} 对象，表示使用类型来指定此 Parameter 表示的形式参数的类型。
     */
    public AnnotatedType getAnnotatedType() {
        // 目前不缓存
        return executable.getAnnotatedParameterTypes()[index];
    }

    private transient volatile Class<?> parameterClassCache = null;

    /**
     * 如果此参数在源代码中隐式声明，则返回 {@code true}；否则返回 {@code false}。
     *
     * @return 如果此参数根据《Java&trade; 语言规范》定义为隐式声明，则返回 true。
     */
    public boolean isImplicit() {
        return Modifier.isMandated(getModifiers());
    }

    /**
     * 如果此参数既不在源代码中隐式声明也不显式声明，则返回 {@code true}；否则返回 {@code false}。
     *
     * @jls 13.1 二进制的形式
     * @return 如果此参数是根据《Java&trade; 语言规范》定义的合成构造，则返回 true。
     */
    public boolean isSynthetic() {
        return Modifier.isSynthetic(getModifiers());
    }

    /**
     * 如果此参数表示一个可变参数列表，则返回 {@code true}；否则返回 {@code false}。
     *
     * @return 如果此参数表示一个可变参数列表，则返回 true。
     */
    public boolean isVarArgs() {
        return executable.isVarArgs() &&
            index == executable.getParameterCount() - 1;
    }


    /**
     * {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        Objects.requireNonNull(annotationClass);
        return annotationClass.cast(declaredAnnotations().get(annotationClass));
    }

    /**
     * {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        Objects.requireNonNull(annotationClass);

        return AnnotationSupport.getDirectlyAndIndirectlyPresent(declaredAnnotations(), annotationClass);
    }

    /**
     * {@inheritDoc}
     */
    public Annotation[] getDeclaredAnnotations() {
        return executable.getParameterAnnotations()[index];
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     */
    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        // 只有类上的注解是继承的，对于所有其他对象，getDeclaredAnnotation 与 getAnnotation 相同。
        return getAnnotation(annotationClass);
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        // 只有类上的注解是继承的，对于所有其他对象，getDeclaredAnnotations 与 getAnnotations 相同。
        return getAnnotationsByType(annotationClass);
    }

    /**
     * {@inheritDoc}
     */
    public Annotation[] getAnnotations() {
        return getDeclaredAnnotations();
    }

    private transient Map<Class<? extends Annotation>, Annotation> declaredAnnotations;

    private synchronized Map<Class<? extends Annotation>, Annotation> declaredAnnotations() {
        if(null == declaredAnnotations) {
            declaredAnnotations =
                new HashMap<Class<? extends Annotation>, Annotation>();
            Annotation[] ann = getDeclaredAnnotations();
            for(int i = 0; i < ann.length; i++)
                declaredAnnotations.put(ann[i].annotationType(), ann[i]);
        }
        return declaredAnnotations;
   }

}
