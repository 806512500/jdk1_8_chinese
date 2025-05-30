
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

package java.lang.reflect;

import sun.reflect.CallerSensitive;
import sun.reflect.FieldAccessor;
import sun.reflect.Reflection;
import sun.reflect.generics.repository.FieldRepository;
import sun.reflect.generics.factory.CoreReflectionFactory;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.scope.ClassScope;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;
import sun.reflect.annotation.AnnotationParser;
import sun.reflect.annotation.AnnotationSupport;
import sun.reflect.annotation.TypeAnnotation;
import sun.reflect.annotation.TypeAnnotationParser;

/**
 * {@code Field} 提供关于类或接口中单个字段的信息和动态访问。反射的字段可以是类（静态）字段或实例字段。
 *
 * <p>{@code Field} 在获取或设置访问操作时允许进行扩展转换，但如果发生缩小转换，则会抛出 {@code IllegalArgumentException}。
 *
 * @see Member
 * @see java.lang.Class
 * @see java.lang.Class#getFields()
 * @see java.lang.Class#getField(String)
 * @see java.lang.Class#getDeclaredFields()
 * @see java.lang.Class#getDeclaredField(String)
 *
 * @author Kenneth Russell
 * @author Nakul Saraiya
 */
public final
class Field extends AccessibleObject implements Member {

    private Class<?>            clazz;
    private int                 slot;
    // 该字段在 1.4 反射实现中由 VM 保证进行内部化
    private String              name;
    private Class<?>            type;
    private int                 modifiers;
    // 泛型和注解支持
    private transient String    signature;
    // 泛型信息存储库；按需初始化
    private transient FieldRepository genericInfo;
    private byte[]              annotations;
    // 缓存的字段访问器，不带覆盖
    private FieldAccessor fieldAccessor;
    // 缓存的字段访问器，带覆盖
    private FieldAccessor overrideFieldAccessor;
    // 用于共享 FieldAccessors。此分支结构目前只有两层深（即一个根 Field 和
    // 可能指向它的许多 Field 对象。）
    //
    // 如果此分支结构中存在循环，可能会在注解代码中发生死锁。
    private Field               root;

    // 泛型基础设施

    private String getGenericSignature() {return signature;}

    // 工厂访问器
    private GenericsFactory getFactory() {
        Class<?> c = getDeclaringClass();
        // 创建作用域和工厂
        return CoreReflectionFactory.make(c, ClassScope.make(c));
    }

    // 泛型信息存储库访问器
    private FieldRepository getGenericInfo() {
        // 必要时按需初始化存储库
        if (genericInfo == null) {
            // 创建并缓存泛型信息存储库
            genericInfo = FieldRepository.make(getGenericSignature(),
                                               getFactory());
        }
        return genericInfo; // 返回缓存的存储库
    }


    /**
     * 包私有构造函数，由 ReflectAccess 使用，以通过 sun.reflect.LangReflectAccess
     * 在 java.lang 包中的 Java 代码中实例化这些对象。
     */
    Field(Class<?> declaringClass,
          String name,
          Class<?> type,
          int modifiers,
          int slot,
          String signature,
          byte[] annotations)
    {
        this.clazz = declaringClass;
        this.name = name;
        this.type = type;
        this.modifiers = modifiers;
        this.slot = slot;
        this.signature = signature;
        this.annotations = annotations;
    }

    /**
     * 包私有方法（通过 ReflectAccess 暴露给 java.lang.Class），返回此 Field 的副本。副本的
     * "root" 字段指向此 Field。
     */
    Field copy() {
        // 此方法允许在引用 VM 中相同底层方法的 Field 对象之间共享 FieldAccessor 对象。
        // （所有这些扭曲仅在 AccessibleObject 中的“可访问性”位隐式要求为每个反射调用
        // 生成新的 java.lang.reflect 对象时才必要。）
        if (this.root != null)
            throw new IllegalArgumentException("不能复制非根 Field");

        Field res = new Field(clazz, name, type, modifiers, slot, signature, annotations);
        res.root = this;
        // 如果已经存在，可以提前传播此字段
        res.fieldAccessor = fieldAccessor;
        res.overrideFieldAccessor = overrideFieldAccessor;

        return res;
    }

    /**
     * 返回表示声明此 {@code Field} 对象所表示字段的类或接口的 {@code Class} 对象。
     */
    public Class<?> getDeclaringClass() {
        return clazz;
    }

    /**
     * 返回此 {@code Field} 对象所表示字段的名称。
     */
    public String getName() {
        return name;
    }

    /**
     * 返回此 {@code Field} 对象所表示字段的 Java 语言修饰符，作为整数。应使用 {@code Modifier} 类来解码修饰符。
     *
     * @see Modifier
     */
    public int getModifiers() {
        return modifiers;
    }

    /**
     * 如果此字段表示枚举类型的元素，则返回 {@code true}；否则返回 {@code false}。
     *
     * @return 如果且仅如果此字段表示枚举类型的元素，则返回 {@code true}。
     * @since 1.5
     */
    public boolean isEnumConstant() {
        return (getModifiers() & Modifier.ENUM) != 0;
    }

    /**
     * 如果此字段是合成字段，则返回 {@code true}；否则返回 {@code false}。
     *
     * @return 如果且仅如果此字段是根据 Java 语言规范定义的合成字段，则返回 {@code true}。
     * @since 1.5
     */
    public boolean isSynthetic() {
        return Modifier.isSynthetic(getModifiers());
    }

    /**
     * 返回一个 {@code Class} 对象，标识此 {@code Field} 对象所表示字段的声明类型。
     *
     * @return 一个 {@code Class} 对象，标识此对象所表示字段的声明类型
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * 返回一个 {@code Type} 对象，表示此 {@code Field} 对象所表示字段的声明类型。
     *
     * <p>如果 {@code Type} 是参数化类型，则返回的 {@code Type} 对象必须准确反映源代码中使用的实际类型参数。
     *
     * <p>如果底层字段的类型是类型变量或参数化类型，则创建它。否则，解析它。
     *
     * @return 一个 {@code Type} 对象，表示此 {@code Field} 对象所表示字段的声明类型
     * @throws GenericSignatureFormatError 如果泛型字段签名不符合
     *     <cite>The Java&trade; Virtual Machine Specification</cite> 中指定的格式
     * @throws TypeNotPresentException 如果底层字段的泛型签名引用了不存在的类型声明
     * @throws MalformedParameterizedTypeException 如果底层字段的泛型签名引用了无法实例化的参数化类型
     * @since 1.5
     */
    public Type getGenericType() {
        if (getGenericSignature() != null)
            return getGenericInfo().getGenericType();
        else
            return getType();
    }


    /**
     * 将此 {@code Field} 与指定对象进行比较。如果对象相同，则返回 true。两个 {@code Field} 对象相同
     * 如果它们由同一个类声明并且具有相同的名称和类型。
     */
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Field) {
            Field other = (Field)obj;
            return (getDeclaringClass() == other.getDeclaringClass())
                && (getName() == other.getName())
                && (getType() == other.getType());
        }
        return false;
    }

    /**
     * 返回此 {@code Field} 的哈希码。这是底层字段的声明类名和其名称的哈希码的异或。
     */
    public int hashCode() {
        return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
    }

    /**
     * 返回描述此 {@code Field} 的字符串。格式是字段的访问修饰符（如果有），后跟字段类型，后跟空格，
     * 后跟声明字段的类的完全限定名称，后跟点，后跟字段的名称。例如：
     * <pre>
     *    public static final int java.lang.Thread.MIN_PRIORITY
     *    private int java.io.FileDescriptor.fd
     * </pre>
     *
     * <p>修饰符按照 "The Java Language Specification" 中指定的规范顺序排列。这是 {@code public}，
     * {@code protected} 或 {@code private} 首先，然后是其他修饰符，按以下顺序： {@code static}， {@code final}，
     * {@code transient}， {@code volatile}。
     *
     * @return 描述此 {@code Field} 的字符串
     * @jls 8.3.1 Field Modifiers
     */
    public String toString() {
        int mod = getModifiers();
        return (((mod == 0) ? "" : (Modifier.toString(mod) + " "))
            + getType().getTypeName() + " "
            + getDeclaringClass().getTypeName() + "."
            + getName());
    }

    /**
     * 返回描述此 {@code Field} 的字符串，包括其泛型类型。格式是字段的访问修饰符（如果有），后跟泛型字段类型，后跟空格，
     * 后跟声明字段的类的完全限定名称，后跟点，后跟字段的名称。
     *
     * <p>修饰符按照 "The Java Language Specification" 中指定的规范顺序排列。这是 {@code public}，
     * {@code protected} 或 {@code private} 首先，然后是其他修饰符，按以下顺序： {@code static}， {@code final}，
     * {@code transient}， {@code volatile}。
     *
     * @return 描述此 {@code Field} 的字符串，包括其泛型类型
     *
     * @since 1.5
     * @jls 8.3.1 Field Modifiers
     */
    public String toGenericString() {
        int mod = getModifiers();
        Type fieldType = getGenericType();
        return (((mod == 0) ? "" : (Modifier.toString(mod) + " "))
            + fieldType.getTypeName() + " "
            + getDeclaringClass().getTypeName() + "."
            + getName());
    }

    /**
     * 返回此 {@code Field} 对象所表示字段在指定对象上的值。如果值具有基本类型，则自动包装在对象中。
     *
     * <p>底层字段的值按以下方式获取：
     *
     * <p>如果底层字段是静态字段，则忽略 {@code obj} 参数；它可以为 null。
     *
     * <p>否则，底层字段是实例字段。如果指定的 {@code obj} 参数为 null，则方法抛出 {@code NullPointerException}。
     * 如果指定的对象不是声明底层字段的类或接口的实例（或其子类或实现者），则方法抛出 {@code IllegalArgumentException}。
     *
     * <p>如果此 {@code Field} 对象正在执行 Java 语言访问控制，且底层字段不可访问，则方法抛出
     * {@code IllegalAccessException}。如果底层字段是静态的，如果声明该字段的类尚未初始化，则初始化该类。
     *
     * <p>否则，从底层实例或静态字段中检索值。如果字段具有基本类型，则在返回前将值包装在对象中，否则直接返回。
     *
     * <p>如果字段在 {@code obj} 的类型中被隐藏，则根据上述规则获取字段的值。
     *
     * @param obj 从中提取表示字段值的对象
     * @return 在对象 {@code obj} 中表示字段的值；基本值在返回前被包装在适当对象中
     *
     * @exception IllegalAccessException    如果此 {@code Field} 对象
     *              正在执行 Java 语言访问控制且底层字段不可访问。
     * @exception IllegalArgumentException  如果指定对象不是声明底层
     *              字段的类或接口的实例（或其子类或实现者）。
     * @exception NullPointerException      如果指定对象为 null
     *              且字段是实例字段。
     * @exception ExceptionInInitializerError 如果此方法引发的初始化失败。
     */
    @CallerSensitive
    public Object get(Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        return getFieldAccessor(obj).get(obj);
    }

    /**
     * 获取静态或实例 {@code boolean} 字段的值。
     *
     * @param obj 从中提取 {@code boolean} 值的对象
     * @return {@code boolean} 字段的值
     *
     * @exception IllegalAccessException    如果此 {@code Field} 对象
     *              正在执行 Java 语言访问控制且底层字段不可访问。
     * @exception IllegalArgumentException  如果指定对象不是
     *              声明底层字段的类或接口的实例（或其子类或实现者），或字段值不能
     *              通过扩展转换转换为类型 {@code boolean}。
     * @exception NullPointerException      如果指定对象为 null
     *              且字段是实例字段。
     * @exception ExceptionInInitializerError 如果此方法引发的初始化失败。
     * @see       Field#get
     */
    @CallerSensitive
    public boolean getBoolean(Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        return getFieldAccessor(obj).getBoolean(obj);
    }


                /**
     * 获取静态或实例 {@code byte} 字段的值。
     *
     * @param obj 提取 {@code byte} 值的对象
     * @return {@code byte} 字段的值
     *
     * @exception IllegalAccessException    如果此 {@code Field} 对象
     *              正在执行 Java 语言访问控制并且底层字段不可访问。
     * @exception IllegalArgumentException  如果指定的对象不是
     *              声明底层字段的类或接口的实例（或子类或实现者），
     *              或者字段值不能通过扩展转换转换为 {@code byte} 类型。
     * @exception NullPointerException      如果指定的对象为 null
     *              并且字段是实例字段。
     * @exception ExceptionInInitializerError 如果此方法引发的初始化失败。
     * @see       Field#get
     */
    @CallerSensitive
    public byte getByte(Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        return getFieldAccessor(obj).getByte(obj);
    }

    /**
     * 获取静态或实例字段的值，类型为
     * {@code char} 或其他可通过扩展转换转换为
     * {@code char} 类型的原始类型。
     *
     * @param obj 提取 {@code char} 值的对象
     * @return 转换为 {@code char} 类型的字段值
     *
     * @exception IllegalAccessException    如果此 {@code Field} 对象
     *              正在执行 Java 语言访问控制并且底层字段不可访问。
     * @exception IllegalArgumentException  如果指定的对象不是
     *              声明底层字段的类或接口的实例（或子类或实现者），
     *              或者字段值不能通过扩展转换转换为 {@code char} 类型。
     * @exception NullPointerException      如果指定的对象为 null
     *              并且字段是实例字段。
     * @exception ExceptionInInitializerError 如果此方法引发的初始化失败。
     * @see Field#get
     */
    @CallerSensitive
    public char getChar(Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        return getFieldAccessor(obj).getChar(obj);
    }

    /**
     * 获取静态或实例字段的值，类型为
     * {@code short} 或其他可通过扩展转换转换为
     * {@code short} 类型的原始类型。
     *
     * @param obj 提取 {@code short} 值的对象
     * @return 转换为 {@code short} 类型的字段值
     *
     * @exception IllegalAccessException    如果此 {@code Field} 对象
     *              正在执行 Java 语言访问控制并且底层字段不可访问。
     * @exception IllegalArgumentException  如果指定的对象不是
     *              声明底层字段的类或接口的实例（或子类或实现者），
     *              或者字段值不能通过扩展转换转换为 {@code short} 类型。
     * @exception NullPointerException      如果指定的对象为 null
     *              并且字段是实例字段。
     * @exception ExceptionInInitializerError 如果此方法引发的初始化失败。
     * @see       Field#get
     */
    @CallerSensitive
    public short getShort(Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        return getFieldAccessor(obj).getShort(obj);
    }

    /**
     * 获取静态或实例字段的值，类型为
     * {@code int} 或其他可通过扩展转换转换为
     * {@code int} 类型的原始类型。
     *
     * @param obj 提取 {@code int} 值的对象
     * @return 转换为 {@code int} 类型的字段值
     *
     * @exception IllegalAccessException    如果此 {@code Field} 对象
     *              正在执行 Java 语言访问控制并且底层字段不可访问。
     * @exception IllegalArgumentException  如果指定的对象不是
     *              声明底层字段的类或接口的实例（或子类或实现者），
     *              或者字段值不能通过扩展转换转换为 {@code int} 类型。
     * @exception NullPointerException      如果指定的对象为 null
     *              并且字段是实例字段。
     * @exception ExceptionInInitializerError 如果此方法引发的初始化失败。
     * @see       Field#get
     */
    @CallerSensitive
    public int getInt(Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        return getFieldAccessor(obj).getInt(obj);
    }

    /**
     * 获取静态或实例字段的值，类型为
     * {@code long} 或其他可通过扩展转换转换为
     * {@code long} 类型的原始类型。
     *
     * @param obj 提取 {@code long} 值的对象
     * @return 转换为 {@code long} 类型的字段值
     *
     * @exception IllegalAccessException    如果此 {@code Field} 对象
     *              正在执行 Java 语言访问控制并且底层字段不可访问。
     * @exception IllegalArgumentException  如果指定的对象不是
     *              声明底层字段的类或接口的实例（或子类或实现者），
     *              或者字段值不能通过扩展转换转换为 {@code long} 类型。
     * @exception NullPointerException      如果指定的对象为 null
     *              并且字段是实例字段。
     * @exception ExceptionInInitializerError 如果此方法引发的初始化失败。
     * @see       Field#get
     */
    @CallerSensitive
    public long getLong(Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        return getFieldAccessor(obj).getLong(obj);
    }

    /**
     * 获取静态或实例字段的值，类型为
     * {@code float} 或其他可通过扩展转换转换为
     * {@code float} 类型的原始类型。
     *
     * @param obj 提取 {@code float} 值的对象
     * @return 转换为 {@code float} 类型的字段值
     *
     * @exception IllegalAccessException    如果此 {@code Field} 对象
     *              正在执行 Java 语言访问控制并且底层字段不可访问。
     * @exception IllegalArgumentException  如果指定的对象不是
     *              声明底层字段的类或接口的实例（或子类或实现者），
     *              或者字段值不能通过扩展转换转换为 {@code float} 类型。
     * @exception NullPointerException      如果指定的对象为 null
     *              并且字段是实例字段。
     * @exception ExceptionInInitializerError 如果此方法引发的初始化失败。
     * @see Field#get
     */
    @CallerSensitive
    public float getFloat(Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        return getFieldAccessor(obj).getFloat(obj);
    }

    /**
     * 获取静态或实例字段的值，类型为
     * {@code double} 或其他可通过扩展转换转换为
     * {@code double} 类型的原始类型。
     *
     * @param obj 提取 {@code double} 值的对象
     * @return 转换为 {@code double} 类型的字段值
     *
     * @exception IllegalAccessException    如果此 {@code Field} 对象
     *              正在执行 Java 语言访问控制并且底层字段不可访问。
     * @exception IllegalArgumentException  如果指定的对象不是
     *              声明底层字段的类或接口的实例（或子类或实现者），
     *              或者字段值不能通过扩展转换转换为 {@code double} 类型。
     * @exception NullPointerException      如果指定的对象为 null
     *              并且字段是实例字段。
     * @exception ExceptionInInitializerError 如果此方法引发的初始化失败。
     * @see       Field#get
     */
    @CallerSensitive
    public double getDouble(Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        return getFieldAccessor(obj).getDouble(obj);
    }

    /**
     * 将此 {@code Field} 对象表示的字段设置为指定对象参数的指定新值。如果底层字段具有原始类型，新值将自动拆箱。
     *
     * <p>操作如下进行：
     *
     * <p>如果底层字段是静态的，则忽略 {@code obj} 参数；它可以为 null。
     *
     * <p>否则，底层字段是实例字段。如果指定的对象参数为 null，方法将抛出
     * {@code NullPointerException}。如果指定的对象参数不是声明底层
     * 字段的类或接口的实例，方法将抛出 {@code IllegalArgumentException}。
     *
     * <p>如果此 {@code Field} 对象正在执行 Java 语言访问控制，并且
     * 底层字段不可访问，方法将抛出 {@code IllegalAccessException}。
     *
     * <p>如果底层字段是 final 的，方法将抛出
     * {@code IllegalAccessException}，除非 {@code setAccessible(true)}
     * 对此 {@code Field} 对象成功调用且字段是非静态的。在这种情况下，设置 final 字段
     * 仅在反序列化或重建具有空白 final 字段的类的实例时有意义，这些实例在程序的其他部分可用之前。
     * 在其他上下文中使用可能会产生不可预测的效果，包括程序的其他部分继续使用此字段的原始值的情况。
     *
     * <p>如果底层字段是原始类型，将尝试拆箱转换以将新值转换为原始类型。如果此尝试失败，方法将抛出
     * {@code IllegalArgumentException}。
     *
     * <p>如果在可能拆箱后，新值不能通过身份或扩展转换转换为底层字段的类型，方法将抛出
     * {@code IllegalArgumentException}。
     *
     * <p>如果底层字段是静态的，如果声明该字段的类尚未初始化，则初始化该类。
     *
     * <p>字段被设置为可能拆箱并扩展的新值。
     *
     * <p>如果在 {@code obj} 类型中隐藏了字段，根据上述规则设置字段的值。
     *
     * @param obj 应该修改其字段的对象
     * @param value 应该修改的 {@code obj} 字段的新值
     *
     * @exception IllegalAccessException    如果此 {@code Field} 对象
     *              正在执行 Java 语言访问控制并且底层字段不可访问或为 final。
     * @exception IllegalArgumentException  如果指定的对象不是
     *              声明底层字段的类或接口的实例（或子类或实现者），
     *              或者拆箱转换失败。
     * @exception NullPointerException      如果指定的对象为 null
     *              并且字段是实例字段。
     * @exception ExceptionInInitializerError 如果此方法引发的初始化失败。
     */
    @CallerSensitive
    public void set(Object obj, Object value)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        getFieldAccessor(obj).set(obj, value);
    }

    /**
     * 将指定对象的字段值设置为 {@code boolean} 类型。
     * 此方法等同于
     * {@code set(obj, zObj)},
     * 其中 {@code zObj} 是一个 {@code Boolean} 对象且
     * {@code zObj.booleanValue() == z}。
     *
     * @param obj 应该修改其字段的对象
     * @param z   应该修改的 {@code obj} 字段的新值
     *
     * @exception IllegalAccessException    如果此 {@code Field} 对象
     *              正在执行 Java 语言访问控制并且底层字段不可访问或为 final。
     * @exception IllegalArgumentException  如果指定的对象不是
     *              声明底层字段的类或接口的实例（或子类或实现者），
     *              或者拆箱转换失败。
     * @exception NullPointerException      如果指定的对象为 null
     *              并且字段是实例字段。
     * @exception ExceptionInInitializerError 如果此方法引发的初始化失败。
     * @see       Field#set
     */
    @CallerSensitive
    public void setBoolean(Object obj, boolean z)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        getFieldAccessor(obj).setBoolean(obj, z);
    }


                /**
     * 设置指定对象的字段值为 {@code byte}。
     * 此方法等同于
     * {@code set(obj, bObj)},
     * 其中 {@code bObj} 是一个 {@code Byte} 对象，且
     * {@code bObj.byteValue() == b}。
     *
     * @param obj 要修改字段值的对象
     * @param b   要设置为 {@code obj} 字段的新值
     *
     * @exception IllegalAccessException    如果此 {@code Field} 对象
     *              正在执行 Java 语言访问控制，且底层字段不可访问或为 final。
     * @exception IllegalArgumentException  如果指定的对象不是声明底层字段的类或接口的实例
     *              （或其子类或实现者），或者拆箱转换失败。
     * @exception NullPointerException      如果指定的对象为 null
     *              且字段为实例字段。
     * @exception ExceptionInInitializerError 如果此方法引发的初始化失败。
     * @see       Field#set
     */
    @CallerSensitive
    public void setByte(Object obj, byte b)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        getFieldAccessor(obj).setByte(obj, b);
    }

    /**
     * 设置指定对象的字段值为 {@code char}。
     * 此方法等同于
     * {@code set(obj, cObj)},
     * 其中 {@code cObj} 是一个 {@code Character} 对象，且
     * {@code cObj.charValue() == c}。
     *
     * @param obj 要修改字段值的对象
     * @param c   要设置为 {@code obj} 字段的新值
     *
     * @exception IllegalAccessException    如果此 {@code Field} 对象
     *              正在执行 Java 语言访问控制，且底层字段不可访问或为 final。
     * @exception IllegalArgumentException  如果指定的对象不是声明底层字段的类或接口的实例
     *              （或其子类或实现者），或者拆箱转换失败。
     * @exception NullPointerException      如果指定的对象为 null
     *              且字段为实例字段。
     * @exception ExceptionInInitializerError 如果此方法引发的初始化失败。
     * @see       Field#set
     */
    @CallerSensitive
    public void setChar(Object obj, char c)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        getFieldAccessor(obj).setChar(obj, c);
    }

    /**
     * 设置指定对象的字段值为 {@code short}。
     * 此方法等同于
     * {@code set(obj, sObj)},
     * 其中 {@code sObj} 是一个 {@code Short} 对象，且
     * {@code sObj.shortValue() == s}。
     *
     * @param obj 要修改字段值的对象
     * @param s   要设置为 {@code obj} 字段的新值
     *
     * @exception IllegalAccessException    如果此 {@code Field} 对象
     *              正在执行 Java 语言访问控制，且底层字段不可访问或为 final。
     * @exception IllegalArgumentException  如果指定的对象不是声明底层字段的类或接口的实例
     *              （或其子类或实现者），或者拆箱转换失败。
     * @exception NullPointerException      如果指定的对象为 null
     *              且字段为实例字段。
     * @exception ExceptionInInitializerError 如果此方法引发的初始化失败。
     * @see       Field#set
     */
    @CallerSensitive
    public void setShort(Object obj, short s)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        getFieldAccessor(obj).setShort(obj, s);
    }

    /**
     * 设置指定对象的字段值为 {@code int}。
     * 此方法等同于
     * {@code set(obj, iObj)},
     * 其中 {@code iObj} 是一个 {@code Integer} 对象，且
     * {@code iObj.intValue() == i}。
     *
     * @param obj 要修改字段值的对象
     * @param i   要设置为 {@code obj} 字段的新值
     *
     * @exception IllegalAccessException    如果此 {@code Field} 对象
     *              正在执行 Java 语言访问控制，且底层字段不可访问或为 final。
     * @exception IllegalArgumentException  如果指定的对象不是声明底层字段的类或接口的实例
     *              （或其子类或实现者），或者拆箱转换失败。
     * @exception NullPointerException      如果指定的对象为 null
     *              且字段为实例字段。
     * @exception ExceptionInInitializerError 如果此方法引发的初始化失败。
     * @see       Field#set
     */
    @CallerSensitive
    public void setInt(Object obj, int i)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        getFieldAccessor(obj).setInt(obj, i);
    }

    /**
     * 设置指定对象的字段值为 {@code long}。
     * 此方法等同于
     * {@code set(obj, lObj)},
     * 其中 {@code lObj} 是一个 {@code Long} 对象，且
     * {@code lObj.longValue() == l}。
     *
     * @param obj 要修改字段值的对象
     * @param l   要设置为 {@code obj} 字段的新值
     *
     * @exception IllegalAccessException    如果此 {@code Field} 对象
     *              正在执行 Java 语言访问控制，且底层字段不可访问或为 final。
     * @exception IllegalArgumentException  如果指定的对象不是声明底层字段的类或接口的实例
     *              （或其子类或实现者），或者拆箱转换失败。
     * @exception NullPointerException      如果指定的对象为 null
     *              且字段为实例字段。
     * @exception ExceptionInInitializerError 如果此方法引发的初始化失败。
     * @see       Field#set
     */
    @CallerSensitive
    public void setLong(Object obj, long l)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        getFieldAccessor(obj).setLong(obj, l);
    }

    /**
     * 设置指定对象的字段值为 {@code float}。
     * 此方法等同于
     * {@code set(obj, fObj)},
     * 其中 {@code fObj} 是一个 {@code Float} 对象，且
     * {@code fObj.floatValue() == f}。
     *
     * @param obj 要修改字段值的对象
     * @param f   要设置为 {@code obj} 字段的新值
     *
     * @exception IllegalAccessException    如果此 {@code Field} 对象
     *              正在执行 Java 语言访问控制，且底层字段不可访问或为 final。
     * @exception IllegalArgumentException  如果指定的对象不是声明底层字段的类或接口的实例
     *              （或其子类或实现者），或者拆箱转换失败。
     * @exception NullPointerException      如果指定的对象为 null
     *              且字段为实例字段。
     * @exception ExceptionInInitializerError 如果此方法引发的初始化失败。
     * @see       Field#set
     */
    @CallerSensitive
    public void setFloat(Object obj, float f)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        getFieldAccessor(obj).setFloat(obj, f);
    }

    /**
     * 设置指定对象的字段值为 {@code double}。
     * 此方法等同于
     * {@code set(obj, dObj)},
     * 其中 {@code dObj} 是一个 {@code Double} 对象，且
     * {@code dObj.doubleValue() == d}。
     *
     * @param obj 要修改字段值的对象
     * @param d   要设置为 {@code obj} 字段的新值
     *
     * @exception IllegalAccessException    如果此 {@code Field} 对象
     *              正在执行 Java 语言访问控制，且底层字段不可访问或为 final。
     * @exception IllegalArgumentException  如果指定的对象不是声明底层字段的类或接口的实例
     *              （或其子类或实现者），或者拆箱转换失败。
     * @exception NullPointerException      如果指定的对象为 null
     *              且字段为实例字段。
     * @exception ExceptionInInitializerError 如果此方法引发的初始化失败。
     * @see       Field#set
     */
    @CallerSensitive
    public void setDouble(Object obj, double d)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        getFieldAccessor(obj).setDouble(obj, d);
    }

    // 在调用此方法之前进行安全检查
    private FieldAccessor getFieldAccessor(Object obj)
        throws IllegalAccessException
    {
        boolean ov = override;
        FieldAccessor a = (ov) ? overrideFieldAccessor : fieldAccessor;
        return (a != null) ? a : acquireFieldAccessor(ov);
    }

    // 注意这里没有使用同步。生成多个 FieldAccessor
    // 对于给定的 Field 是正确的（尽管效率不高）。但是，避免同步可能会
    // 使实现更具可扩展性。
    private FieldAccessor acquireFieldAccessor(boolean overrideFinalCheck) {
        // 首先检查是否已创建，如果是，则使用它
        FieldAccessor tmp = null;
        if (root != null) tmp = root.getFieldAccessor(overrideFinalCheck);
        if (tmp != null) {
            if (overrideFinalCheck)
                overrideFieldAccessor = tmp;
            else
                fieldAccessor = tmp;
        } else {
            // 否则创建一个并将其传播到根
            tmp = reflectionFactory.newFieldAccessor(this, overrideFinalCheck);
            setFieldAccessor(tmp, overrideFinalCheck);
        }

        return tmp;
    }

    // 返回此 Field 对象的 FieldAccessor，不查找链到根
    private FieldAccessor getFieldAccessor(boolean overrideFinalCheck) {
        return (overrideFinalCheck)? overrideFieldAccessor : fieldAccessor;
    }

    // 设置此 Field 对象及其根的 FieldAccessor
    private void setFieldAccessor(FieldAccessor accessor, boolean overrideFinalCheck) {
        if (overrideFinalCheck)
            overrideFieldAccessor = accessor;
        else
            fieldAccessor = accessor;
        // 传播到根
        if (root != null) {
            root.setFieldAccessor(accessor, overrideFinalCheck);
        }
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.5
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        Objects.requireNonNull(annotationClass);
        return annotationClass.cast(declaredAnnotations().get(annotationClass));
    }

    /**
     * {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
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
                    Field root = this.root;
                    if (root != null) {
                        declAnnos = root.declaredAnnotations();
                    } else {
                        declAnnos = AnnotationParser.parseAnnotations(
                                annotations,
                                sun.misc.SharedSecrets.getJavaLangAccess()
                                        .getConstantPool(getDeclaringClass()),
                                getDeclaringClass());
                    }
                    declaredAnnotations = declAnnos;
                }
            }
        }
        return declAnnos;
    }

    private native byte[] getTypeAnnotationBytes0();

    /**
     * 返回一个 AnnotatedType 对象，表示使用类型来指定此 Field 表示的字段的声明类型。
     * @return 一个表示此 Field 表示的字段的声明类型的对象
     *
     * @since 1.8
     */
    public AnnotatedType getAnnotatedType() {
        return TypeAnnotationParser.buildAnnotatedType(getTypeAnnotationBytes0(),
                                                       sun.misc.SharedSecrets.getJavaLangAccess().
                                                           getConstantPool(getDeclaringClass()),
                                                       this,
                                                       getDeclaringClass(),
                                                       getGenericType(),
                                                       TypeAnnotation.TypeAnnotationTarget.FIELD);
}
}
