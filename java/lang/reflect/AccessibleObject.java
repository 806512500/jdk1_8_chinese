
/*
 * 版权所有 (c) 1997, 2014, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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

import java.security.AccessController;
import sun.reflect.Reflection;
import sun.reflect.ReflectionFactory;
import java.lang.annotation.Annotation;

/**
 * AccessibleObject 类是 Field、Method 和 Constructor 对象的基类。它提供了将反射对象标记为在使用时抑制默认 Java 语言访问控制检查的能力。
 * 访问检查——对于 public、默认（包）访问、protected 和 private 成员——在使用 Fields、Methods 或 Constructors 设置或获取字段、调用方法或创建和初始化类的新实例时执行。
 *
 * <p>在反射对象中设置 {@code accessible} 标志，允许具有足够权限的复杂应用程序，如 Java 对象序列化或其他持久化机制，以通常被禁止的方式操作对象。
 *
 * <p>默认情况下，反射对象是 <em>不可访问的</em>。
 *
 * @see Field
 * @see Method
 * @see Constructor
 * @see ReflectPermission
 *
 * @since 1.2
 */
public class AccessibleObject implements AnnotatedElement {

    /**
     * 用于检查客户端是否有足够权限来规避 Java 语言访问控制检查的 Permission 对象。
     */
    static final private java.security.Permission ACCESS_PERMISSION =
        new ReflectPermission("suppressAccessChecks");

    /**
     * 便捷方法，通过一次安全检查（为了效率）设置对象数组中的 {@code accessible} 标志。
     *
     * <p>首先，如果有安全经理，其 {@code checkPermission} 方法将被调用，权限为
     * {@code ReflectPermission("suppressAccessChecks")}。
     *
     * <p>如果 {@code flag} 为 {@code true}，但输入 {@code array} 中的任何元素的可访问性不能更改（例如，如果元素对象是 {@link Constructor} 对象，用于类 {@link
     * java.lang.Class}），则会引发 {@code SecurityException}。在这种情况下，对于引发异常的元素之前的（不包括该元素）数组元素，对象的可访问性将设置为 {@code flag}；对于引发异常的元素及其之后的元素，可访问性保持不变。
     *
     * @param array 要设置的 AccessibleObjects 数组
     * @param flag  每个对象中 {@code accessible} 标志的新值
     * @throws SecurityException 如果请求被拒绝。
     * @see SecurityManager#checkPermission
     * @see java.lang.RuntimePermission
     */
    public static void setAccessible(AccessibleObject[] array, boolean flag)
        throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(ACCESS_PERMISSION);
        for (int i = 0; i < array.length; i++) {
            setAccessible0(array[i], flag);
        }
    }

    /**
     * 将此对象的 {@code accessible} 标志设置为指定的布尔值。值为 {@code true} 表示反射对象在使用时应抑制 Java 语言访问检查。值为 {@code false} 表示反射对象应执行 Java 语言访问检查。
     *
     * <p>首先，如果有安全经理，其 {@code checkPermission} 方法将被调用，权限为
     * {@code ReflectPermission("suppressAccessChecks")}。
     *
     * <p>如果 {@code flag} 为 {@code true}，但此对象的可访问性不能更改（例如，如果此元素对象是 {@link Constructor} 对象，用于类 {@link java.lang.Class}），则会引发 {@code SecurityException}。
     *
     * <p>如果此对象是 {@link java.lang.reflect.Constructor} 对象，用于类
     * {@code java.lang.Class}，且 {@code flag} 为 true，则会引发 {@code SecurityException}。
     *
     * @param flag  {@code accessible} 标志的新值
     * @throws SecurityException 如果请求被拒绝。
     * @see SecurityManager#checkPermission
     * @see java.lang.RuntimePermission
     */
    public void setAccessible(boolean flag) throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(ACCESS_PERMISSION);
        setAccessible0(this, flag);
    }

    /* 检查你是否暴露了 java.lang.Class.<init> 或 java.lang.Class 中的敏感字段。 */
    private static void setAccessible0(AccessibleObject obj, boolean flag)
        throws SecurityException
    {
        if (obj instanceof Constructor && flag == true) {
            Constructor<?> c = (Constructor<?>)obj;
            if (c.getDeclaringClass() == Class.class) {
                throw new SecurityException("Cannot make a java.lang.Class" +
                                            " constructor accessible");
            }
        }
        obj.override = flag;
    }

    /**
     * 获取此对象的 {@code accessible} 标志的值。
     *
     * @return 对象的 {@code accessible} 标志的值
     */
    public boolean isAccessible() {
        return override;
    }

    /**
     * 构造器：仅由 Java 虚拟机使用。
     */
    protected AccessibleObject() {}

    // 表示此对象是否覆盖了语言级别的访问检查。初始化为 "false"。此字段由 Field、Method 和 Constructor 使用。
    //
    // 注意：出于安全目的，此字段不得在此包之外可见。
    boolean override;

    // 子类用于创建字段、方法和构造器访问器的反射工厂。注意，这在引导过程中非常早地被调用。
    static final ReflectionFactory reflectionFactory =
        AccessController.doPrivileged(
            new sun.reflect.ReflectionFactory.GetReflectionFactoryAction());

                /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.5
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        throw new AssertionError("All subclasses should override this method");
    }

    /**
     * {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @since 1.5
     */
    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return AnnotatedElement.super.isAnnotationPresent(annotationClass);
    }

   /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        throw new AssertionError("All subclasses should override this method");
    }

    /**
     * @since 1.5
     */
    public Annotation[] getAnnotations() {
        return getDeclaredAnnotations();
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        // Only annotations on classes are inherited, for all other
        // objects getDeclaredAnnotation is the same as
        // getAnnotation.
        return getAnnotation(annotationClass);
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        // Only annotations on classes are inherited, for all other
        // objects getDeclaredAnnotationsByType is the same as
        // getAnnotationsByType.
        return getAnnotationsByType(annotationClass);
    }

    /**
     * @since 1.5
     */
    public Annotation[] getDeclaredAnnotations()  {
        throw new AssertionError("All subclasses should override this method");
    }


    // 共享访问检查逻辑。

    // 对于非公共成员或包私有类中的成员，
    // 必须执行相对昂贵的安全检查。
    // 如果安全检查对于给定的类成功，它将始终成功（不受权限授予或撤销的影响）；
    // 通过记住上次检查成功的最后一个类来加速常见情况下的检查。
    //
    // 构造函数的简单安全检查是查看调用者是否已经被看到、验证并缓存。
    // （另见 Class.newInstance()，它使用类似的方法。）
    //
    // 方法和字段需要更复杂的安全检查缓存
    // 缓存可以是 null（空缓存），一个 2-数组 {caller,target}，
    // 或者一个调用者（目标隐式等于 this.clazz）。
    // 在 2-数组的情况下，目标总是不同于 clazz。
    volatile Object securityCheckCache;

    void checkAccess(Class<?> caller, Class<?> clazz, Object obj, int modifiers)
        throws IllegalAccessException
    {
        if (caller == clazz) {  // 快速检查
            return;             // 访问正常
        }
        Object cache = securityCheckCache;  // 读取 volatile
        Class<?> targetClass = clazz;
        if (obj != null
            && Modifier.isProtected(modifiers)
            && ((targetClass = obj.getClass()) != clazz)) {
            // 必须匹配一个 2-列表 { caller, targetClass }。
            if (cache instanceof Class[]) {
                Class<?>[] cache2 = (Class<?>[]) cache;
                if (cache2[1] == targetClass &&
                    cache2[0] == caller) {
                    return;     // 访问正常
                }
                // （先测试 cache[1]，因为 [1] 的范围检查包含 [0] 的范围检查。）
            }
        } else if (cache == caller) {
            // 非保护情况（或 obj.class == this.clazz）。
            return;             // 访问正常
        }

        // 如果没有返回，则进入慢路径。
        slowCheckMemberAccess(caller, clazz, obj, modifiers, targetClass);
    }

    // 将所有这些慢速操作移出内联：
    void slowCheckMemberAccess(Class<?> caller, Class<?> clazz, Object obj, int modifiers,
                               Class<?> targetClass)
        throws IllegalAccessException
    {
        Reflection.ensureMemberAccess(caller, clazz, obj, modifiers);

        // 成功：更新缓存。
        Object cache = ((targetClass == clazz)
                        ? caller
                        : new Class<?>[] { caller, targetClass });

        // 注意：这两个缓存元素不是 volatile 的，
        // 但它们实际上是 final 的。Java 内存模型
        // 保证缓存元素的初始化存储将在 volatile 写入之前发生。
        securityCheckCache = cache;         // 写入 volatile
    }
}
