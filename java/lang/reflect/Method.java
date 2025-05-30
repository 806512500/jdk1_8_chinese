
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

package java.lang.reflect;

import sun.reflect.CallerSensitive;
import sun.reflect.MethodAccessor;
import sun.reflect.Reflection;
import sun.reflect.generics.repository.MethodRepository;
import sun.reflect.generics.factory.CoreReflectionFactory;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.scope.MethodScope;
import sun.reflect.annotation.AnnotationType;
import sun.reflect.annotation.AnnotationParser;
import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationFormatError;
import java.nio.ByteBuffer;

/**
 * {@code Method} 提供关于类或接口上单个方法的信息和访问。反射的方法可以是类方法或实例方法（包括抽象方法）。
 *
 * <p>{@code Method} 允许在调用底层方法时，实际参数与方法的正式参数之间进行扩展转换，但如果发生窄化转换，则会抛出 {@code IllegalArgumentException}。
 *
 * @see Member
 * @see java.lang.Class
 * @see java.lang.Class#getMethods()
 * @see java.lang.Class#getMethod(String, Class[])
 * @see java.lang.Class#getDeclaredMethods()
 * @see java.lang.Class#getDeclaredMethod(String, Class[])
 *
 * @author Kenneth Russell
 * @author Nakul Saraiya
 */
public final class Method extends Executable {
    private Class<?>            clazz;
    private int                 slot;
    // 由 VM 在 1.4 反射实现中保证 interned
    private String              name;
    private Class<?>            returnType;
    private Class<?>[]          parameterTypes;
    private Class<?>[]          exceptionTypes;
    private int                 modifiers;
    // 泛型和注解支持
    private transient String              signature;
    // 泛型信息存储库；惰性初始化
    private transient MethodRepository genericInfo;
    private byte[]              annotations;
    private byte[]              parameterAnnotations;
    private byte[]              annotationDefault;
    private volatile MethodAccessor methodAccessor;
    // 用于 MethodAccessor 的共享。这个分支结构目前只有两层深（即一个根 Method 和可能许多指向它的 Method 对象。）
    //
    // 如果这个分支结构包含循环，可能会在注解代码中发生死锁。
    private Method              root;

    // 获取泛型签名
    private String getGenericSignature() {return signature;}

    // 获取工厂
    private GenericsFactory getFactory() {
        // 创建作用域和工厂
        return CoreReflectionFactory.make(this, MethodScope.make(this));
    }

    // 获取泛型信息存储库
    @Override
    MethodRepository getGenericInfo() {
        // 必要时惰性初始化存储库
        if (genericInfo == null) {
            // 创建并缓存泛型信息存储库
            genericInfo = MethodRepository.make(getGenericSignature(),
                                                getFactory());
        }
        return genericInfo; // 返回缓存的存储库
    }

    /**
     * 包私有构造函数，由 ReflectAccess 使用，以便通过 sun.reflect.LangReflectAccess 从 java.lang 包中的 Java 代码实例化这些对象。
     */
    Method(Class<?> declaringClass,
           String name,
           Class<?>[] parameterTypes,
           Class<?> returnType,
           Class<?>[] checkedExceptions,
           int modifiers,
           int slot,
           String signature,
           byte[] annotations,
           byte[] parameterAnnotations,
           byte[] annotationDefault) {
        this.clazz = declaringClass;
        this.name = name;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.exceptionTypes = checkedExceptions;
        this.modifiers = modifiers;
        this.slot = slot;
        this.signature = signature;
        this.annotations = annotations;
        this.parameterAnnotations = parameterAnnotations;
        this.annotationDefault = annotationDefault;
    }

    /**
     * 包私有方法（通过 ReflectAccess 暴露给 java.lang.Class），返回此 Method 的副本。副本的 "root" 字段指向此 Method。
     */
    Method copy() {
        // 此方法允许在 Method 对象之间共享 MethodAccessor 对象，这些对象引用 VM 中相同的底层方法。（所有这些扭曲都是因为 AccessibleObject 中的 "可访问性" 位，这隐式要求为每个反射调用创建新的 java.lang.reflect 对象。）
        if (this.root != null)
            throw new IllegalArgumentException("不能复制非根 Method");

        Method res = new Method(clazz, name, parameterTypes, returnType,
                                exceptionTypes, modifiers, slot, signature,
                                annotations, parameterAnnotations, annotationDefault);
        res.root = this;
        // 如果已经存在，最好提前传播
        res.methodAccessor = methodAccessor;
        return res;
    }

    /**
     * 用于 Excecutable 的注解共享。
     */
    @Override
    Executable getRoot() {
        return root;
    }

    @Override
    boolean hasGenericInformation() {
        return (getGenericSignature() != null);
    }

    @Override
    byte[] getAnnotationBytes() {
        return annotations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getDeclaringClass() {
        return clazz;
    }

    /**
     * 返回此 {@code Method} 对象表示的方法的名称，作为 {@code String}。
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getModifiers() {
        return modifiers;
    }

    /**
     * {@inheritDoc}
     * @throws GenericSignatureFormatError {@inheritDoc}
     * @since 1.5
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public TypeVariable<Method>[] getTypeParameters() {
        if (getGenericSignature() != null)
            return (TypeVariable<Method>[])getGenericInfo().getTypeParameters();
        else
            return (TypeVariable<Method>[])new TypeVariable[0];
    }

    /**
     * 返回一个表示此 {@code Method} 对象表示的方法的正式返回类型的 {@code Class} 对象。
     *
     * @return 此对象表示的方法的返回类型
     */
    public Class<?> getReturnType() {
        return returnType;
    }

    /**
     * 返回一个表示此 {@code Method} 对象表示的方法的正式返回类型的 {@code Type} 对象。
     *
     * <p>如果返回类型是参数化类型，返回的 {@code Type} 对象必须准确反映源代码中使用的实际类型参数。
     *
     * <p>如果返回类型是类型变量或参数化类型，则创建它。否则，解析它。
     *
     * @return 一个表示底层方法的正式返回类型的 {@code Type} 对象
     * @throws GenericSignatureFormatError
     *     如果泛型方法签名不符合
     *     <cite>The Java&trade; Virtual Machine Specification</cite>
     *     中指定的格式
     * @throws TypeNotPresentException 如果底层方法的返回类型引用了不存在的类型声明
     * @throws MalformedParameterizedTypeException 如果底层方法的返回类型引用了无法实例化的参数化类型
     * @since 1.5
     */
    public Type getGenericReturnType() {
      if (getGenericSignature() != null) {
        return getGenericInfo().getReturnType();
      } else { return getReturnType();}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?>[] getParameterTypes() {
        return parameterTypes.clone();
    }

    /**
     * {@inheritDoc}
     * @since 1.8
     */
    public int getParameterCount() { return parameterTypes.length; }


    /**
     * {@inheritDoc}
     * @throws GenericSignatureFormatError {@inheritDoc}
     * @throws TypeNotPresentException {@inheritDoc}
     * @throws MalformedParameterizedTypeException {@inheritDoc}
     * @since 1.5
     */
    @Override
    public Type[] getGenericParameterTypes() {
        return super.getGenericParameterTypes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?>[] getExceptionTypes() {
        return exceptionTypes.clone();
    }

    /**
     * {@inheritDoc}
     * @throws GenericSignatureFormatError {@inheritDoc}
     * @throws TypeNotPresentException {@inheritDoc}
     * @throws MalformedParameterizedTypeException {@inheritDoc}
     * @since 1.5
     */
    @Override
    public Type[] getGenericExceptionTypes() {
        return super.getGenericExceptionTypes();
    }

    /**
     * 将此 {@code Method} 与指定对象进行比较。如果对象相同，则返回 true。两个 {@code Methods} 相同，如果它们由同一个类声明，具有相同的名称、正式参数类型和返回类型。
     */
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Method) {
            Method other = (Method)obj;
            if ((getDeclaringClass() == other.getDeclaringClass())
                && (getName() == other.getName())) {
                if (!returnType.equals(other.getReturnType()))
                    return false;
                return equalParamTypes(parameterTypes, other.parameterTypes);
            }
        }
        return false;
    }

    /**
     * 返回此 {@code Method} 的哈希码。哈希码是底层方法的声明类名称和方法名称的哈希码的异或。
     */
    public int hashCode() {
        return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
    }

    /**
     * 返回描述此 {@code Method} 的字符串。字符串格式为方法访问修饰符（如果有），后跟方法返回类型，后跟空格，后跟声明方法的类，后跟点，后跟方法名称，后跟括号内的逗号分隔的正式参数类型列表。如果方法抛出检查异常，参数列表后跟空格，后跟 throws，后跟逗号分隔的抛出异常类型列表。例如：
     * <pre>
     *    public boolean java.lang.Object.equals(java.lang.Object)
     * </pre>
     *
     * <p>访问修饰符按 "The Java Language Specification" 中指定的规范顺序排列。这是 {@code public}，{@code protected} 或 {@code private} 首先，然后是其他修饰符，按以下顺序：
     * {@code abstract}，{@code default}，{@code static}，{@code final}，
     * {@code synchronized}，{@code native}，{@code strictfp}。
     *
     * @return 描述此 {@code Method} 的字符串
     *
     * @jls 8.4.3 Method Modifiers
     */
    public String toString() {
        return sharedToString(Modifier.methodModifiers(),
                              isDefault(),
                              parameterTypes,
                              exceptionTypes);
    }

    @Override
    void specificToStringHeader(StringBuilder sb) {
        sb.append(getReturnType().getTypeName()).append(' ');
        sb.append(getDeclaringClass().getTypeName()).append('.');
        sb.append(getName());
    }

    /**
     * 返回描述此 {@code Method} 的字符串，包括类型参数。字符串格式为方法访问修饰符（如果有），后跟尖括号内的逗号分隔的类型参数列表（如果有），后跟方法的泛型返回类型，后跟空格，后跟声明方法的类，后跟点，后跟方法名称，后跟括号内的逗号分隔的泛型正式参数类型列表。
     *
     * 如果此方法声明为可变参数，最后一个参数表示为 "<tt><i>Type</i>...</tt>" 而不是 "<tt><i>Type</i>[]</tt>"。
     *
     * 空格用于分隔访问修饰符，类型参数或返回类型。如果没有类型参数，类型参数列表被省略；如果存在类型参数列表，列表与类名之间用空格分隔。如果方法声明为抛出异常，参数列表后跟空格，后跟 throws，后跟逗号分隔的泛型抛出异常类型列表。
     *
     * <p>访问修饰符按 "The Java Language Specification" 中指定的规范顺序排列。这是 {@code public}，{@code protected} 或 {@code private} 首先，然后是其他修饰符，按以下顺序：
     * {@code abstract}，{@code default}，{@code static}，{@code final}，
     * {@code synchronized}，{@code native}，{@code strictfp}。
     *
     * @return 描述此 {@code Method} 的字符串，包括类型参数
     *
     * @since 1.5
     *
     * @jls 8.4.3 Method Modifiers
     */
    @Override
    public String toGenericString() {
        return sharedToGenericString(Modifier.methodModifiers(), isDefault());
    }

    @Override
    void specificToGenericStringHeader(StringBuilder sb) {
        Type genRetType = getGenericReturnType();
        sb.append(genRetType.getTypeName()).append(' ');
        sb.append(getDeclaringClass().getTypeName()).append('.');
        sb.append(getName());
    }


                /**
     * 调用此 {@code Method} 对象表示的底层方法，使用指定的对象和参数。
     * 个别参数会自动拆箱以匹配原始形式参数，原始和引用参数根据需要进行方法调用转换。
     *
     * <p>如果底层方法是静态的，则指定的 {@code obj} 参数将被忽略。它可以为 null。
     *
     * <p>如果底层方法所需的正式参数数量为 0，则提供的 {@code args} 数组可以为空或 null。
     *
     * <p>如果底层方法是实例方法，则使用《Java 语言规范》第二版第 15.12.4.4 节中记录的动态方法查找来调用它；特别是，将基于目标对象的运行时类型进行覆盖。
     *
     * <p>如果底层方法是静态的，则声明该方法的类将在尚未初始化的情况下进行初始化。
     *
     * <p>如果方法正常完成，它返回的值将返回给调用者；如果该值具有原始类型，则首先适当地将其包装在对象中。但是，如果该值是原始类型的数组，则数组中的元素 <i>不会</i> 被包装在对象中；换句话说，返回的是原始类型的数组。如果底层方法的返回类型为 void，则调用返回 null。
     *
     * @param obj  调用底层方法的对象
     * @param args 用于方法调用的参数
     * @return 调用此对象表示的方法在 {@code obj} 上的结果，参数为 {@code args}
     *
     * @exception IllegalAccessException    如果此 {@code Method} 对象正在执行 Java 语言访问控制并且底层方法不可访问。
     * @exception IllegalArgumentException  如果方法是实例方法且指定的对象参数不是声明底层方法的类或接口的实例（或其子类或实现者）；如果实际参数和形式参数的数量不同；如果原始参数的拆箱转换失败；或者，拆箱后，参数值不能通过方法调用转换转换为对应的形式参数类型。
     * @exception InvocationTargetException 如果底层方法抛出异常。
     * @exception NullPointerException      如果指定的对象为 null 且方法是实例方法。
     * @exception ExceptionInInitializerError 如果此方法引发的初始化失败。
     */
    @CallerSensitive
    public Object invoke(Object obj, Object... args)
        throws IllegalAccessException, IllegalArgumentException,
           InvocationTargetException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        MethodAccessor ma = methodAccessor;             // read volatile
        if (ma == null) {
            ma = acquireMethodAccessor();
        }
        return ma.invoke(obj, args);
    }

    /**
     * 如果此方法是桥接方法，则返回 {@code true}；否则返回 {@code false}。
     *
     * @return 如果且仅如果此方法是《Java 语言规范》定义的桥接方法，则返回 true。
     * @since 1.5
     */
    public boolean isBridge() {
        return (getModifiers() & Modifier.BRIDGE) != 0;
    }

    /**
     * {@inheritDoc}
     * @since 1.5
     */
    @Override
    public boolean isVarArgs() {
        return super.isVarArgs();
    }

    /**
     * {@inheritDoc}
     * @jls 13.1 二进制的形式
     * @since 1.5
     */
    @Override
    public boolean isSynthetic() {
        return super.isSynthetic();
    }

    /**
     * 如果此方法是默认方法，则返回 {@code true}；否则返回 {@code false}。
     *
     * 默认方法是接口类型中声明的公共非抽象实例方法，即非静态且有方法体的方法。
     *
     * @return 如果且仅如果此方法是《Java 语言规范》定义的默认方法，则返回 true。
     * @since 1.8
     */
    public boolean isDefault() {
        // 默认方法是接口中声明的公共非抽象实例方法。
        return ((getModifiers() & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) ==
                Modifier.PUBLIC) && getDeclaringClass().isInterface();
    }

    // 注意这里没有使用同步。生成多个 MethodAccessor
    // 对于给定的 Method 是正确的（尽管效率不高）。但是，避免同步可能会使实现更具可扩展性。
    private MethodAccessor acquireMethodAccessor() {
        // 首先检查是否已经创建了一个，如果是，则使用它
        MethodAccessor tmp = null;
        if (root != null) tmp = root.getMethodAccessor();
        if (tmp != null) {
            methodAccessor = tmp;
        } else {
            // 否则创建一个并传播到根
            tmp = reflectionFactory.newMethodAccessor(this);
            setMethodAccessor(tmp);
        }

        return tmp;
    }

    // 返回此 Method 对象的 MethodAccessor，不查找链到根
    MethodAccessor getMethodAccessor() {
        return methodAccessor;
    }

    // 设置此 Method 对象及其根的 MethodAccessor
    void setMethodAccessor(MethodAccessor accessor) {
        methodAccessor = accessor;
        // 传播到根
        if (root != null) {
            root.setMethodAccessor(accessor);
        }
    }

    /**
     * 返回此 {@code Method} 实例表示的注解成员的默认值。如果成员是原始类型，则返回相应包装类型的实例。如果成员没有默认值，或者方法实例不表示注解类型中声明的成员，则返回 null。
     *
     * @return 此 {@code Method} 实例表示的注解成员的默认值。
     * @throws TypeNotPresentException 如果注解的类型为 {@link Class} 且找不到默认类值的定义。
     * @since  1.5
     */
    public Object getDefaultValue() {
        if  (annotationDefault == null)
            return null;
        Class<?> memberType = AnnotationType.invocationHandlerReturnType(
            getReturnType());
        Object result = AnnotationParser.parseMemberValue(
            memberType, ByteBuffer.wrap(annotationDefault),
            sun.misc.SharedSecrets.getJavaLangAccess().
                getConstantPool(getDeclaringClass()),
            getDeclaringClass());
        if (result instanceof sun.reflect.annotation.ExceptionProxy)
            throw new AnnotationFormatError("Invalid default: " + this);
        return result;
    }

    /**
     * {@inheritDoc}
     * @throws NullPointerException  {@inheritDoc}
     * @since 1.5
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return super.getAnnotation(annotationClass);
    }

    /**
     * {@inheritDoc}
     * @since 1.5
     */
    public Annotation[] getDeclaredAnnotations()  {
        return super.getDeclaredAnnotations();
    }

    /**
     * {@inheritDoc}
     * @since 1.5
     */
    @Override
    public Annotation[][] getParameterAnnotations() {
        return sharedGetParameterAnnotations(parameterTypes, parameterAnnotations);
    }

    /**
     * {@inheritDoc}
     * @since 1.8
     */
    @Override
    public AnnotatedType getAnnotatedReturnType() {
        return getAnnotatedReturnType0(getGenericReturnType());
    }

    @Override
    void handleParameterNumberMismatch(int resultLength, int numParameters) {
        throw new AnnotationFormatError("参数注解与参数数量不匹配");
    }
}
