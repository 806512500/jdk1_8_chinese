
/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.lang.reflect;

import sun.reflect.CallerSensitive;
import sun.reflect.ConstructorAccessor;
import sun.reflect.Reflection;
import sun.reflect.annotation.TypeAnnotation;
import sun.reflect.annotation.TypeAnnotationParser;
import sun.reflect.generics.repository.ConstructorRepository;
import sun.reflect.generics.factory.CoreReflectionFactory;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.scope.ConstructorScope;
import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationFormatError;

/**
 * {@code Constructor} 提供关于类的单个构造函数的信息和访问。
 *
 * <p>{@code Constructor} 允许在将实际参数与底层构造函数的形式参数匹配时发生扩展转换，但如果发生缩小转换，则会抛出
 * {@code IllegalArgumentException}。
 *
 * @param <T> 声明构造函数的类
 *
 * @see Member
 * @see java.lang.Class
 * @see java.lang.Class#getConstructors()
 * @see java.lang.Class#getConstructor(Class[])
 * @see java.lang.Class#getDeclaredConstructors()
 *
 * @author      Kenneth Russell
 * @author      Nakul Saraiya
 */
public final class Constructor<T> extends Executable {
    private Class<T>            clazz;
    private int                 slot;
    private Class<?>[]          parameterTypes;
    private Class<?>[]          exceptionTypes;
    private int                 modifiers;
    // 泛型和注解支持
    private transient String    signature;
    // 泛型信息存储库；延迟初始化
    private transient ConstructorRepository genericInfo;
    private byte[]              annotations;
    private byte[]              parameterAnnotations;

    // 泛型基础设施
    // 工厂的访问器
    private GenericsFactory getFactory() {
        // 创建作用域和工厂
        return CoreReflectionFactory.make(this, ConstructorScope.make(this));
    }

    // 泛型信息存储库的访问器
    @Override
    ConstructorRepository getGenericInfo() {
        // 如果必要，延迟初始化存储库
        if (genericInfo == null) {
            // 创建并缓存泛型信息存储库
            genericInfo =
                ConstructorRepository.make(getSignature(),
                                           getFactory());
        }
        return genericInfo; // 返回缓存的存储库
    }

    private volatile ConstructorAccessor constructorAccessor;
    // 用于共享 ConstructorAccessors。此分支结构
    // 目前只有两层深（即一个根 Constructor
    // 和可能指向它的许多 Constructor 对象。）
    //
    // 如果此分支结构包含循环，注解代码中可能会发生死锁。
    private Constructor<T>      root;

    /**
     * 用于 Excecutable 的注解共享。
     */
    @Override
    Executable getRoot() {
        return root;
    }

    /**
     * 包私有构造函数，由 ReflectAccess 使用，以通过 sun.reflect.LangReflectAccess
     * 在 java.lang 包中的 Java 代码中实例化这些对象。
     */
    Constructor(Class<T> declaringClass,
                Class<?>[] parameterTypes,
                Class<?>[] checkedExceptions,
                int modifiers,
                int slot,
                String signature,
                byte[] annotations,
                byte[] parameterAnnotations) {
        this.clazz = declaringClass;
        this.parameterTypes = parameterTypes;
        this.exceptionTypes = checkedExceptions;
        this.modifiers = modifiers;
        this.slot = slot;
        this.signature = signature;
        this.annotations = annotations;
        this.parameterAnnotations = parameterAnnotations;
    }

    /**
     * 包私有例程（通过 ReflectAccess 暴露给 java.lang.Class），返回此 Constructor 的副本。副本的
     * "root" 字段指向此 Constructor。
     */
    Constructor<T> copy() {
        // 此例程允许在引用 VM 中相同底层方法的 Constructor 对象之间共享 ConstructorAccessor 对象。
        // （所有这些扭曲都是因为 AccessibleObject 中的“可访问性”位，
        // 它隐式要求为每个对 Class 对象的反射调用创建新的 java.lang.reflect 对象。）
        if (this.root != null)
            throw new IllegalArgumentException("Can not copy a non-root Constructor");

        Constructor<T> res = new Constructor<>(clazz,
                                               parameterTypes,
                                               exceptionTypes, modifiers, slot,
                                               signature,
                                               annotations,
                                               parameterAnnotations);
        res.root = this;
        // 如果已经存在，不妨急切地传播这一点
        res.constructorAccessor = constructorAccessor;
        return res;
    }

    @Override
    boolean hasGenericInformation() {
        return (getSignature() != null);
    }

    @Override
    byte[] getAnnotationBytes() {
        return annotations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> getDeclaringClass() {
        return clazz;
    }

    /**
     * 返回此构造函数的名称，作为字符串。这是构造函数声明类的二进制名称。
     */
    @Override
    public String getName() {
        return getDeclaringClass().getName();
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
    public TypeVariable<Constructor<T>>[] getTypeParameters() {
      if (getSignature() != null) {
        return (TypeVariable<Constructor<T>>[])getGenericInfo().getTypeParameters();
      } else
          return (TypeVariable<Constructor<T>>[])new TypeVariable[0];
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
     * 将此 {@code Constructor} 与指定的对象进行比较。
     * 如果对象相同，则返回 true。如果两个 {@code Constructor} 对象由同一个类声明并且具有相同的正式参数类型，则它们是相同的。
     */
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Constructor) {
            Constructor<?> other = (Constructor<?>)obj;
            if (getDeclaringClass() == other.getDeclaringClass()) {
                return equalParamTypes(parameterTypes, other.parameterTypes);
            }
        }
        return false;
    }

    /**
     * 返回此 {@code Constructor} 的哈希码。哈希码与底层构造函数的声明类名的哈希码相同。
     */
    public int hashCode() {
        return getDeclaringClass().getName().hashCode();
    }

    /**
     * 返回描述此 {@code Constructor} 的字符串。字符串格式为构造函数的访问修饰符（如果有），后跟声明类的完全限定名，
     * 再后跟括号内逗号分隔的构造函数的正式参数类型列表。例如：
     * <pre>
     *    public java.util.Hashtable(int,float)
     * </pre>
     *
     * <p>构造函数的唯一可能的修饰符是访问修饰符 {@code public}、{@code protected} 或
     * {@code private}。只能出现其中一个，或者如果构造函数具有默认（包）访问权限，则不出现。
     *
     * @return 描述此 {@code Constructor} 的字符串
     * @jls 8.8.3. Constructor Modifiers
     */
    public String toString() {
        return sharedToString(Modifier.constructorModifiers(),
                              false,
                              parameterTypes,
                              exceptionTypes);
    }

    @Override
    void specificToStringHeader(StringBuilder sb) {
        sb.append(getDeclaringClass().getTypeName());
    }

    /**
     * 返回描述此 {@code Constructor} 的字符串，包括类型参数。字符串格式为构造函数的访问修饰符（如果有），后跟尖括号内逗号分隔的构造函数的类型参数列表（如果有），
     * 再后跟声明类的完全限定名，再后跟括号内逗号分隔的构造函数的泛型正式参数类型列表。
     *
     * 如果此构造函数被声明为接受可变数量的参数，则最后一个参数表示为 "<tt><i>Type</i>...</tt>" 而不是 "<tt><i>Type</i>[]</tt>"。
     *
     * 使用空格分隔访问修饰符，以及类型参数或返回类型。如果没有类型参数，则省略类型参数列表；如果存在类型参数列表，则列表与类名之间用空格分隔。如果构造函数声明为抛出异常，
     * 则参数列表后跟空格，后跟单词 "{@code throws}"，再后跟逗号分隔的抛出异常类型列表。
     *
     * <p>构造函数的唯一可能的修饰符是访问修饰符 {@code public}、{@code protected} 或
     * {@code private}。只能出现其中一个，或者如果构造函数具有默认（包）访问权限，则不出现。
     *
     * @return 描述此 {@code Constructor} 的字符串，包括类型参数
     *
     * @since 1.5
     * @jls 8.8.3. Constructor Modifiers
     */
    @Override
    public String toGenericString() {
        return sharedToGenericString(Modifier.constructorModifiers(), false);
    }

    @Override
    void specificToGenericStringHeader(StringBuilder sb) {
        specificToStringHeader(sb);
    }

    /**
     * 使用此 {@code Constructor} 对象表示的构造函数创建并初始化声明类的新实例，使用指定的初始化参数。
     * 单个参数会自动拆箱以匹配原始正式参数，并且原始和引用参数在必要时会进行方法调用转换。
     *
     * <p>如果底层构造函数所需的正式参数数量为 0，则提供的 {@code initargs} 数组可以是长度为 0 或 null。
     *
     * <p>如果构造函数的声明类是非静态上下文中的内部类，则构造函数的第一个参数需要是封闭实例；参见《Java&trade; 语言规范》的第 15.9.3 节。
     *
     * <p>如果所需的访问和参数检查成功并且实例化将进行，如果构造函数的声明类尚未初始化，则初始化该类。
     *
     * <p>如果构造函数正常完成，返回新创建并初始化的实例。
     *
     * @param initargs 要作为参数传递给构造函数调用的对象数组；原始类型的值将包装在适当类型的包装对象中（例如，一个 {@code float} 包装在 {@link java.lang.Float Float} 中）
     *
     * @return 通过调用此对象表示的构造函数创建的新对象
     *
     * @exception IllegalAccessException    如果此 {@code Constructor} 对象正在执行 Java 语言访问控制并且底层构造函数不可访问。
     * @exception IllegalArgumentException  如果实际参数数量和正式参数数量不同；如果原始参数的拆箱转换失败；或者如果，拆箱后，参数值不能通过方法调用转换转换为对应的正式参数类型；如果此构造函数涉及枚举类型。
     * @exception InstantiationException    如果声明底层构造函数的类表示抽象类。
     * @exception InvocationTargetException 如果底层构造函数抛出异常。
     * @exception ExceptionInInitializerError 如果此方法引发的初始化失败。
     */
    @CallerSensitive
    public T newInstance(Object ... initargs)
        throws InstantiationException, IllegalAccessException,
               IllegalArgumentException, InvocationTargetException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, null, modifiers);
            }
        }
        if ((clazz.getModifiers() & Modifier.ENUM) != 0)
            throw new IllegalArgumentException("Cannot reflectively create enum objects");
        ConstructorAccessor ca = constructorAccessor;   // 读取 volatile
        if (ca == null) {
            ca = acquireConstructorAccessor();
        }
        @SuppressWarnings("unchecked")
        T inst = (T) ca.newInstance(initargs);
        return inst;
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
     * @jls 13.1 The Form of a Binary
     * @since 1.5
     */
    @Override
    public boolean isSynthetic() {
        return super.isSynthetic();
    }

    // 注意这里没有使用同步。生成多个
    // ConstructorAccessor 对于给定的 Constructor 是正确的
    // （尽管效率不高）。但是，避免同步可能会使实现更具
    // 扩展性。
    private ConstructorAccessor acquireConstructorAccessor() {
        // 首先检查是否已经创建了一个，如果有则获取它
        ConstructorAccessor tmp = null;
        if (root != null) tmp = root.getConstructorAccessor();
        if (tmp != null) {
            constructorAccessor = tmp;
        } else {
            // 否则创建一个并传播到根
            tmp = reflectionFactory.newConstructorAccessor(this);
            setConstructorAccessor(tmp);
        }

        return tmp;
    }

    // 返回此 Constructor 对象的 ConstructorAccessor，不
    // 向上查找根
    ConstructorAccessor getConstructorAccessor() {
        return constructorAccessor;
    }

    // 设置此 Constructor 对象的 ConstructorAccessor 并
    // （递归地）设置其根
    void setConstructorAccessor(ConstructorAccessor accessor) {
        constructorAccessor = accessor;
        // 传播到根
        if (root != null) {
            root.setConstructorAccessor(accessor);
        }
    }

    int getSlot() {
        return slot;
    }

    String getSignature() {
        return signature;
    }

    byte[] getRawAnnotations() {
        return annotations;
    }

    byte[] getRawParameterAnnotations() {
        return parameterAnnotations;
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

    @Override
    void handleParameterNumberMismatch(int resultLength, int numParameters) {
        Class<?> declaringClass = getDeclaringClass();
        if (declaringClass.isEnum() ||
            declaringClass.isAnonymousClass() ||
            declaringClass.isLocalClass() )
            return ; // 无法可靠地计数参数
        else {
            if (!declaringClass.isMemberClass() || // 顶级
                // 检查非静态成员类的封闭实例参数
                (declaringClass.isMemberClass() &&
                ((declaringClass.getModifiers() & Modifier.STATIC) == 0)  &&
                resultLength + 1 != numParameters) ) {
                throw new AnnotationFormatError(
                        "Parameter annotations don't match number of parameters");
            }
        }
    }

    /**
     * {@inheritDoc}
     * @since 1.8
     */
    @Override
    public AnnotatedType getAnnotatedReturnType() {
        return getAnnotatedReturnType0(getDeclaringClass());
    }

    /**
     * {@inheritDoc}
     * @since 1.8
     */
    @Override
    public AnnotatedType getAnnotatedReceiverType() {
        if (getDeclaringClass().getEnclosingClass() == null)
            return super.getAnnotatedReceiverType();

        return TypeAnnotationParser.buildAnnotatedType(getTypeAnnotationBytes0(),
                sun.misc.SharedSecrets.getJavaLangAccess().
                        getConstantPool(getDeclaringClass()),
                this,
                getDeclaringClass(),
                getDeclaringClass().getEnclosingClass(),
                TypeAnnotation.TypeAnnotationTarget.METHOD_RECEIVER);
    }

}