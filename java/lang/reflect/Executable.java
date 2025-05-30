
/*
 * Copyright (c) 2012, 2015, Oracle and/or its affiliates. All rights reserved.
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
import java.util.Map;
import java.util.Objects;
import sun.reflect.annotation.AnnotationParser;
import sun.reflect.annotation.AnnotationSupport;
import sun.reflect.annotation.TypeAnnotationParser;
import sun.reflect.annotation.TypeAnnotation;
import sun.reflect.generics.repository.ConstructorRepository;

/**
 * 一个共享的超类，用于 {@link Method} 和 {@link Constructor} 的通用功能。
 *
 * @since 1.8
 */
public abstract class Executable extends AccessibleObject
    implements Member, GenericDeclaration {
    /*
     * 仅授予包可见性的构造函数。
     */
    Executable() {}

    /**
     * 允许代码共享的访问器方法。
     */
    abstract byte[] getAnnotationBytes();

    /**
     * 允许代码共享的访问器方法。
     */
    abstract Executable getRoot();

    /**
     * 该可执行对象是否有泛型信息。
     */
    abstract boolean hasGenericInformation();

    abstract ConstructorRepository getGenericInfo();

    boolean equalParamTypes(Class<?>[] params1, Class<?>[] params2) {
        /* 避免不必要的克隆 */
        if (params1.length == params2.length) {
            for (int i = 0; i < params1.length; i++) {
                if (params1[i] != params2[i])
                    return false;
            }
            return true;
        }
        return false;
    }

    Annotation[][] parseParameterAnnotations(byte[] parameterAnnotations) {
        return AnnotationParser.parseParameterAnnotations(
               parameterAnnotations,
               sun.misc.SharedSecrets.getJavaLangAccess().
               getConstantPool(getDeclaringClass()),
               getDeclaringClass());
    }

    void separateWithCommas(Class<?>[] types, StringBuilder sb) {
        for (int j = 0; j < types.length; j++) {
            sb.append(types[j].getTypeName());
            if (j < (types.length - 1))
                sb.append(",");
        }

    }

    void printModifiersIfNonzero(StringBuilder sb, int mask, boolean isDefault) {
        int mod = getModifiers() & mask;

        if (mod != 0 && !isDefault) {
            sb.append(Modifier.toString(mod)).append(' ');
        } else {
            int access_mod = mod & Modifier.ACCESS_MODIFIERS;
            if (access_mod != 0)
                sb.append(Modifier.toString(access_mod)).append(' ');
            if (isDefault)
                sb.append("default ");
            mod = (mod & ~Modifier.ACCESS_MODIFIERS);
            if (mod != 0)
                sb.append(Modifier.toString(mod)).append(' ');
        }
    }

    String sharedToString(int modifierMask,
                          boolean isDefault,
                          Class<?>[] parameterTypes,
                          Class<?>[] exceptionTypes) {
        try {
            StringBuilder sb = new StringBuilder();

            printModifiersIfNonzero(sb, modifierMask, isDefault);
            specificToStringHeader(sb);

            sb.append('(');
            separateWithCommas(parameterTypes, sb);
            sb.append(')');
            if (exceptionTypes.length > 0) {
                sb.append(" throws ");
                separateWithCommas(exceptionTypes, sb);
            }
            return sb.toString();
        } catch (Exception e) {
            return "<" + e + ">";
        }
    }

    /**
     * 生成特定于方法或构造函数的 toString 头信息。
     */
    abstract void specificToStringHeader(StringBuilder sb);

    String sharedToGenericString(int modifierMask, boolean isDefault) {
        try {
            StringBuilder sb = new StringBuilder();

            printModifiersIfNonzero(sb, modifierMask, isDefault);

            TypeVariable<?>[] typeparms = getTypeParameters();
            if (typeparms.length > 0) {
                boolean first = true;
                sb.append('<');
                for(TypeVariable<?> typeparm: typeparms) {
                    if (!first)
                        sb.append(',');
                    // 类对象不会出现在这里；无需测试并调用 Class.getName()。
                    sb.append(typeparm.toString());
                    first = false;
                }
                sb.append("> ");
            }

            specificToGenericStringHeader(sb);

            sb.append('(');
            Type[] params = getGenericParameterTypes();
            for (int j = 0; j < params.length; j++) {
                String param = params[j].getTypeName();
                if (isVarArgs() && (j == params.length - 1)) // 将 T[] 替换为 T...
                    param = param.replaceFirst("\\[\\]$", "...");
                sb.append(param);
                if (j < (params.length - 1))
                    sb.append(',');
            }
            sb.append(')');
            Type[] exceptions = getGenericExceptionTypes();
            if (exceptions.length > 0) {
                sb.append(" throws ");
                for (int k = 0; k < exceptions.length; k++) {
                    sb.append((exceptions[k] instanceof Class)?
                              ((Class)exceptions[k]).getName():
                              exceptions[k].toString());
                    if (k < (exceptions.length - 1))
                        sb.append(',');
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "<" + e + ">";
        }
    }

    /**
     * 生成特定于方法或构造函数的 toGenericString 头信息。
     */
    abstract void specificToGenericStringHeader(StringBuilder sb);

    /**
     * 返回表示此对象表示的可执行对象的声明类或接口的 {@code Class} 对象。
     */
    public abstract Class<?> getDeclaringClass();

    /**
     * 返回此对象表示的可执行对象的名称。
     */
    public abstract String getName();

    /**
     * 返回此对象表示的可执行对象的 Java 语言 {@linkplain Modifier 修饰符}。
     */
    public abstract int getModifiers();

    /**
     * 返回一个 {@code TypeVariable} 对象数组，表示此 {@code GenericDeclaration} 对象表示的泛型声明中声明的类型变量，按声明顺序排列。如果底层泛型声明未声明类型变量，则返回长度为 0 的数组。
     *
     * @return 一个 {@code TypeVariable} 对象数组，表示此泛型声明声明的类型变量
     * @throws GenericSignatureFormatError 如果此泛型声明的泛型签名不符合
     *     <cite>The Java&trade; Virtual Machine Specification</cite> 中指定的格式
     */
    public abstract TypeVariable<?>[] getTypeParameters();

    /**
     * 返回一个 {@code Class} 对象数组，表示此对象表示的可执行对象的正式参数类型，按声明顺序排列。如果底层可执行对象不接受任何参数，则返回长度为 0 的数组。
     *
     * @return 此对象表示的可执行对象的参数类型
     */
    public abstract Class<?>[] getParameterTypes();

    /**
     * 返回此对象表示的可执行对象的正式参数数量（无论是显式声明、隐式声明还是两者都不是）。
     *
     * @return 此对象表示的可执行对象的正式参数数量
     */
    public int getParameterCount() {
        throw new AbstractMethodError();
    }

    /**
     * 返回一个 {@code Type} 对象数组，表示此对象表示的可执行对象的正式参数类型，按声明顺序排列。如果底层可执行对象不接受任何参数，则返回长度为 0 的数组。
     *
     * <p>如果正式参数类型是参数化类型，则返回的 {@code Type} 对象必须准确反映源代码中使用的实际类型参数。
     *
     * <p>如果正式参数类型是类型变量或参数化类型，则创建它。否则，解析它。
     *
     * @return 一个 {@code Type} 数组，表示底层可执行对象的正式参数类型，按声明顺序排列
     * @throws GenericSignatureFormatError
     *     如果泛型方法签名不符合
     *     <cite>The Java&trade; Virtual Machine Specification</cite> 中指定的格式
     * @throws TypeNotPresentException 如果底层可执行对象的任何参数类型引用了不存在的类型声明
     * @throws MalformedParameterizedTypeException 如果底层可执行对象的任何参数类型引用了由于任何原因无法实例化的参数化类型
     */
    public Type[] getGenericParameterTypes() {
        if (hasGenericInformation())
            return getGenericInfo().getParameterTypes();
        else
            return getParameterTypes();
    }

    /**
     * 行为类似于 {@code getGenericParameterTypes}，但返回所有参数的类型信息，包括合成参数。
     */
    Type[] getAllGenericParameterTypes() {
        final boolean genericInfo = hasGenericInformation();

        // 简单情况：我们没有泛型参数信息。在这种情况下，我们只返回
        // getParameterTypes() 的结果。
        if (!genericInfo) {
            return getParameterTypes();
        } else {
            final boolean realParamData = hasRealParameterData();
            final Type[] genericParamTypes = getGenericParameterTypes();
            final Type[] nonGenericParamTypes = getParameterTypes();
            final Type[] out = new Type[nonGenericParamTypes.length];
            final Parameter[] params = getParameters();
            int fromidx = 0;
            // 如果我们有实际的参数数据，那么我们利用
            // 合成和强制标志。
            if (realParamData) {
                for (int i = 0; i < out.length; i++) {
                    final Parameter param = params[i];
                    if (param.isSynthetic() || param.isImplicit()) {
                        // 如果我们遇到合成或强制参数，
                        // 使用非泛型参数信息。
                        out[i] = nonGenericParamTypes[i];
                    } else {
                        // 否则，使用泛型参数信息。
                        out[i] = genericParamTypes[fromidx];
                        fromidx++;
                    }
                }
            } else {
                // 否则，使用非泛型参数数据。
                // 没有方法参数反射数据，我们
                // 无法确定哪些参数是合成/强制的，因此无法匹配索引。
                return genericParamTypes.length == nonGenericParamTypes.length ?
                    genericParamTypes : nonGenericParamTypes;
            }
            return out;
        }
    }

    /**
     * 返回一个 {@code Parameter} 对象数组，表示此对象表示的底层可执行对象的所有参数。如果可执行对象没有参数，则返回长度为 0 的数组。
     *
     * <p>底层可执行对象的参数不一定具有唯一名称，或在 Java 编程语言中是合法标识符（JLS 3.8）。
     *
     * @throws MalformedParametersException 如果类文件包含格式不正确的 MethodParameters 属性。
     * @return 一个 {@code Parameter} 对象数组，表示此对象表示的可执行对象的所有参数。
     */
    public Parameter[] getParameters() {
        // TODO: 这可能最终需要类似于 Field、Method 等中的安全机制。
        //
        // 需要复制缓存的数组以防止用户篡改。
        // 由于参数是不可变的，我们可以浅复制。
        return privateGetParameters().clone();
    }

    private Parameter[] synthesizeAllParams() {
        final int realparams = getParameterCount();
        final Parameter[] out = new Parameter[realparams];
        for (int i = 0; i < realparams; i++)
            // TODO: 是否有方法可以合成地推导出修饰符？通常情况下可能不行，因为我们无法知道它们，但可能有特定情况。
            out[i] = new Parameter("arg" + i, 0, this, i);
        return out;
    }

    private void verifyParameters(final Parameter[] parameters) {
        final int mask = Modifier.FINAL | Modifier.SYNTHETIC | Modifier.MANDATED;

        if (getParameterTypes().length != parameters.length)
            throw new MalformedParametersException("MethodParameters 属性中的参数数量不正确");

        for (Parameter parameter : parameters) {
            final String name = parameter.getRealName();
            final int mods = parameter.getModifiers();

            if (name != null) {
                if (name.isEmpty() || name.indexOf('.') != -1 ||
                    name.indexOf(';') != -1 || name.indexOf('[') != -1 ||
                    name.indexOf('/') != -1) {
                    throw new MalformedParametersException("无效的参数名称 \"" + name + "\"");
                }
            }

            if (mods != (mods & mask)) {
                throw new MalformedParametersException("无效的参数修饰符");
            }
        }
    }

    private Parameter[] privateGetParameters() {
        // 使用 tmp 以避免对易失变量进行多次写入。
        Parameter[] tmp = parameters;


                    if (tmp == null) {

            // 否则，从JVM获取它们
            try {
                tmp = getParameters0();
            } catch(IllegalArgumentException e) {
                // 重新抛出ClassFormatErrors
                throw new MalformedParametersException("无效的常量池索引");
            }

            // 如果返回为空，则合成参数
            if (tmp == null) {
                hasRealParameterData = false;
                tmp = synthesizeAllParams();
            } else {
                hasRealParameterData = true;
                verifyParameters(tmp);
            }

            parameters = tmp;
        }

        return tmp;
    }

    boolean hasRealParameterData() {
        // 如果在参数初始化之前以某种方式调用此方法，强制其存在。
        if (parameters == null) {
            privateGetParameters();
        }
        return hasRealParameterData;
    }

    private transient volatile boolean hasRealParameterData;
    private transient volatile Parameter[] parameters;

    private native Parameter[] getParameters0();
    native byte[] getTypeAnnotationBytes0();

    // reflectaccess需要
    byte[] getTypeAnnotationBytes() {
        return getTypeAnnotationBytes0();
    }

    /**
     * 返回一个表示此对象所表示的底层可执行代码声明为抛出的异常类型的{@code Class}对象数组。如果可执行代码在其{@code
     * throws}子句中未声明任何异常，则返回长度为0的数组。
     *
     * @return 由此对象表示的可执行代码声明为抛出的异常类型
     */
    public abstract Class<?>[] getExceptionTypes();

    /**
     * 返回一个表示此可执行对象声明为抛出的异常类型的{@code Type}对象数组。如果底层可执行代码在其{@code throws}子句中未声明任何异常，则返回长度为0的数组。
     *
     * <p>如果异常类型是类型变量或参数化类型，则创建它。否则，解析它。
     *
     * @return 一个表示底层可执行代码抛出的异常类型的Type数组
     * @throws GenericSignatureFormatError
     *     如果泛型方法签名不符合
     *     <cite>The Java&trade; Virtual Machine Specification</cite>
     *     中指定的格式
     * @throws TypeNotPresentException 如果底层可执行代码的
     *     {@code throws}子句引用了不存在的类型声明
     * @throws MalformedParameterizedTypeException 如果
     *     底层可执行代码的{@code throws}子句引用了由于任何原因无法实例化的参数化类型
     */
    public Type[] getGenericExceptionTypes() {
        Type[] result;
        if (hasGenericInformation() &&
            ((result = getGenericInfo().getExceptionTypes()).length > 0))
            return result;
        else
            return getExceptionTypes();
    }

    /**
     * 返回一个描述此{@code Executable}的字符串，包括任何类型参数。
     * @return 一个描述此{@code Executable}的字符串，包括任何类型参数
     */
    public abstract String toGenericString();

    /**
     * 如果此可执行代码被声明为接受可变数量的参数，则返回{@code true}；否则返回{@code false}。
     *
     * @return 如果且仅当此可执行代码被声明为接受可变数量的参数时返回{@code true}。
     */
    public boolean isVarArgs()  {
        return (getModifiers() & Modifier.VARARGS) != 0;
    }

    /**
     * 如果此可执行代码是合成构造；否则返回{@code false}。
     *
     * @return 如果且仅当此可执行代码是合成构造，如
     * <cite>The Java&trade; Language Specification</cite>中定义的那样，则返回true。
     * @jls 13.1 The Form of a Binary
     */
    public boolean isSynthetic() {
        return Modifier.isSynthetic(getModifiers());
    }

    /**
     * 返回一个表示此对象所表示的可执行代码的正式参数和隐式参数上的注解的注解数组，按声明顺序排列。合成和强制参数（见下文解释），如内部类构造函数的外部"this"参数，将在返回的数组中表示。如果可执行代码没有参数（意味着没有正式的、合成的和强制的参数），将返回长度为0的数组。如果可执行代码有一个或多个参数，对于没有注解的每个参数，将返回长度为0的嵌套数组。返回的注解对象是可序列化的。此方法的调用者可以自由修改返回的数组；它不会影响返回给其他调用者的数组。
     *
     * 编译器可能会向方法的参数列表中添加在源代码中隐式声明的额外参数（"强制"），以及在源代码中既未隐式也未显式声明的参数（"合成"）。有关更多信息，请参见{@link
     * java.lang.reflect.Parameter}。
     *
     * @see java.lang.reflect.Parameter
     * @see java.lang.reflect.Parameter#getAnnotations
     * @return 一个表示此对象所表示的可执行代码的正式和隐式参数上的注解的数组，按声明顺序排列
     */
    public abstract Annotation[][] getParameterAnnotations();

    Annotation[][] sharedGetParameterAnnotations(Class<?>[] parameterTypes,
                                                 byte[] parameterAnnotations) {
        int numParameters = parameterTypes.length;
        if (parameterAnnotations == null)
            return new Annotation[numParameters][0];

        Annotation[][] result = parseParameterAnnotations(parameterAnnotations);

        if (result.length != numParameters)
            handleParameterNumberMismatch(result.length, numParameters);
        return result;
    }

    abstract void handleParameterNumberMismatch(int resultLength, int numParameters);

    /**
     * {@inheritDoc}
     * @throws NullPointerException  {@inheritDoc}
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
    public Annotation[] getDeclaredAnnotations()  {
        return AnnotationParser.toArray(declaredAnnotations());
    }

    private transient volatile Map<Class<? extends Annotation>, Annotation> declaredAnnotations;

    private Map<Class<? extends Annotation>, Annotation> declaredAnnotations() {
        Map<Class<? extends Annotation>, Annotation> declAnnos;
        if ((declAnnos = declaredAnnotations) == null) {
            synchronized (this) {
                if ((declAnnos = declaredAnnotations) == null) {
                    Executable root = getRoot();
                    if (root != null) {
                        declAnnos = root.declaredAnnotations();
                    } else {
                        declAnnos = AnnotationParser.parseAnnotations(
                                getAnnotationBytes(),
                                sun.misc.SharedSecrets.getJavaLangAccess().
                                        getConstantPool(getDeclaringClass()),
                                getDeclaringClass()
                        );
                    }
                    declaredAnnotations = declAnnos;
                }
            }
        }
        return declAnnos;
    }

    /**
     * 返回一个表示使用类型指定此可执行代码所表示的方法/构造函数的返回类型的{@code AnnotatedType}对象。
     *
     * 如果此{@code Executable}对象表示一个构造函数，则{@code
     * AnnotatedType}对象表示构造对象的类型。
     *
     * 如果此{@code Executable}对象表示一个方法，则{@code
     * AnnotatedType}对象表示使用类型指定方法的返回类型。
     *
     * @return 一个表示此可执行代码所表示的方法或构造函数的返回类型的对象
     */
    public abstract AnnotatedType getAnnotatedReturnType();

    /* 子类的辅助方法。
     *
     * 返回一个表示使用类型指定此可执行代码所表示的方法/构造函数的返回类型的{@code AnnotatedType}对象。
     */
    AnnotatedType getAnnotatedReturnType0(Type returnType) {
        return TypeAnnotationParser.buildAnnotatedType(getTypeAnnotationBytes0(),
                sun.misc.SharedSecrets.getJavaLangAccess().
                        getConstantPool(getDeclaringClass()),
                this,
                getDeclaringClass(),
                returnType,
                TypeAnnotation.TypeAnnotationTarget.METHOD_RETURN);
    }

    /**
     * 返回一个表示使用类型指定此可执行代码对象所表示的方法/构造函数的接收类型（receiver type）的{@code AnnotatedType}对象。方法/构造函数的接收类型仅在方法/构造函数具有<em>接收参数</em>（JLS 8.4.1）时可用。
     *
     * 如果此{@code Executable}对象表示一个没有接收参数的构造函数或实例方法，或者接收参数的类型没有注解，则返回值是一个表示没有注解的元素的{@code AnnotatedType}对象。
     *
     * 如果此{@code Executable}对象表示一个静态方法，则返回值为null。
     *
     * @return 一个表示此可执行代码所表示的方法或构造函数的接收类型的对象
     */
    public AnnotatedType getAnnotatedReceiverType() {
        if (Modifier.isStatic(this.getModifiers()))
            return null;
        return TypeAnnotationParser.buildAnnotatedType(getTypeAnnotationBytes0(),
                sun.misc.SharedSecrets.getJavaLangAccess().
                        getConstantPool(getDeclaringClass()),
                this,
                getDeclaringClass(),
                getDeclaringClass(),
                TypeAnnotation.TypeAnnotationTarget.METHOD_RECEIVER);
    }

    /**
     * 返回一个表示使用类型指定此可执行代码所表示的方法/构造函数的形式参数类型的{@code AnnotatedType}对象数组。数组中对象的顺序对应于方法/构造函数声明中形式参数类型的顺序。
     *
     * 如果方法/构造函数未声明任何参数，则返回长度为0的数组。
     *
     * @return 一个表示此可执行代码所表示的方法或构造函数的形式参数类型的对象数组
     */
    public AnnotatedType[] getAnnotatedParameterTypes() {
        return TypeAnnotationParser.buildAnnotatedTypes(getTypeAnnotationBytes0(),
                sun.misc.SharedSecrets.getJavaLangAccess().
                        getConstantPool(getDeclaringClass()),
                this,
                getDeclaringClass(),
                getAllGenericParameterTypes(),
                TypeAnnotation.TypeAnnotationTarget.METHOD_FORMAL_PARAMETER);
    }

    /**
     * 返回一个表示使用类型指定此可执行代码所表示的方法/构造函数的声明异常的{@code AnnotatedType}对象数组。数组中对象的顺序对应于方法/构造函数声明中异常类型的顺序。
     *
     * 如果方法/构造函数未声明任何异常，则返回长度为0的数组。
     *
     * @return 一个表示此可执行代码所表示的方法或构造函数的声明异常的对象数组
     */
    public AnnotatedType[] getAnnotatedExceptionTypes() {
        return TypeAnnotationParser.buildAnnotatedTypes(getTypeAnnotationBytes0(),
                sun.misc.SharedSecrets.getJavaLangAccess().
                        getConstantPool(getDeclaringClass()),
                this,
                getDeclaringClass(),
                getGenericExceptionTypes(),
                TypeAnnotation.TypeAnnotationTarget.THROWS);
    }

}
