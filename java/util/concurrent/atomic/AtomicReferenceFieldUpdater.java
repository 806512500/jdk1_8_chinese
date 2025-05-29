/*
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

/*
 *
 *
 *
 *
 *
 * 由 Doug Lea 编写，并在 JCP JSR-166 专家小组成员的帮助下发布到公共领域，如
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的那样。
 */

package java.util.concurrent.atomic;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;

/**
 * 一个基于反射的实用工具，允许对指定类的指定 {@code volatile} 引用字段进行原子更新。
 * 本类设计用于原子数据结构，其中同一节点的多个引用字段可以独立地进行原子更新。例如，树节点可以声明为
 *
 * <pre> {@code
 * class Node {
 *   private volatile Node left, right;
 *
 *   private static final AtomicReferenceFieldUpdater<Node, Node> leftUpdater =
 *     AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "left");
 *   private static AtomicReferenceFieldUpdater<Node, Node> rightUpdater =
 *     AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "right");
 *
 *   Node getLeft() { return left; }
 *   boolean compareAndSetLeft(Node expect, Node update) {
 *     return leftUpdater.compareAndSet(this, expect, update);
 *   }
 *   // ... and so on
 * }}</pre>
 *
 * <p>请注意，本类中 {@code compareAndSet} 方法的保证比其他原子类中的要弱。
 * 由于本类无法确保字段的所有使用都适合原子访问，因此它只能保证相对于同一更新器上的其他 {@code compareAndSet} 和 {@code set} 调用的原子性。
 *
 * @since 1.5
 * @author Doug Lea
 * @param <T> 持有可更新字段的对象类型
 * @param <V> 字段类型
 */
public abstract class AtomicReferenceFieldUpdater<T,V> {

    /**
     * 创建并返回一个用于具有给定字段的对象的更新器。
     * Class 参数用于检查反射类型和泛型类型是否匹配。
     *
     * @param tclass 持有字段的对象类
     * @param vclass 字段类
     * @param fieldName 要更新的字段名
     * @param <U> tclass 的实例类型
     * @param <W> vclass 的实例类型
     * @return 更新器
     * @throws ClassCastException 如果字段类型不正确
     * @throws IllegalArgumentException 如果字段不是 volatile 的
     * @throws RuntimeException 带有嵌套的基于反射的异常，如果类不包含字段或类型不正确，
     * 或者根据 Java 语言访问控制，字段对调用者不可访问
     */
    @CallerSensitive
    public static <U,W> AtomicReferenceFieldUpdater<U,W> newUpdater(Class<U> tclass,
                                                                    Class<W> vclass,
                                                                    String fieldName) {
        return new AtomicReferenceFieldUpdaterImpl<U,W>
            (tclass, vclass, fieldName, Reflection.getCallerClass());
    }

    /**
     * 用于子类的受保护的空构造函数。
     */
    protected AtomicReferenceFieldUpdater() {
    }

    /**
     * 如果给定对象中由本更新器管理的字段的当前值 {@code ==} 预期值，则原子地将该字段设置为给定的更新值。
     * 本方法保证相对于其他对 {@code compareAndSet} 和 {@code set} 的调用是原子的，但不一定相对于字段的其他更改。
     *
     * @param obj 要条件设置字段的对象
     * @param expect 预期值
     * @param update 新值
     * @return 如果成功则返回 {@code true}
     */
    public abstract boolean compareAndSet(T obj, V expect, V update);

    /**
     * 如果给定对象中由本更新器管理的字段的当前值 {@code ==} 预期值，则原子地将该字段设置为给定的更新值。
     * 本方法保证相对于其他对 {@code compareAndSet} 和 {@code set} 的调用是原子的，但不一定相对于字段的其他更改。
     *
     * <p><a href="package-summary.html#weakCompareAndSet">可能会无故失败且不提供顺序保证</a>，因此很少是 {@code compareAndSet} 的适当替代。
     *
     * @param obj 要条件设置字段的对象
     * @param expect 预期值
     * @param update 新值
     * @return 如果成功则返回 {@code true}
     */
    public abstract boolean weakCompareAndSet(T obj, V expect, V update);

    /**
     * 将给定对象中由本更新器管理的字段设置为给定的更新值。此操作保证相对于后续对 {@code compareAndSet} 的调用是易失性存储。
     *
     * @param obj 要设置字段的对象
     * @param newValue 新值
     */
    public abstract void set(T obj, V newValue);

    /**
     * 最终将给定对象中由本更新器管理的字段设置为给定的更新值。
     *
     * @param obj 要设置字段的对象
     * @param newValue 新值
     * @since 1.6
     */
    public abstract void lazySet(T obj, V newValue);

    /**
     * 获取由本更新器管理的给定对象中字段的当前值。
     *
     * @param obj 要获取字段的对象
     * @return 当前值
     */
    public abstract V get(T obj);

    /**
     * 原子地将给定对象中由本更新器管理的字段设置为给定值并返回旧值。
     *
     * @param obj 要获取并设置字段的对象
     * @param newValue 新值
     * @return 旧值
     */
    public V getAndSet(T obj, V newValue) {
        V prev;
        do {
            prev = get(obj);
        } while (!compareAndSet(obj, prev, newValue));
        return prev;
    }

}


                /**
     * 原子地更新由这个更新器管理的给定对象的字段，返回更新前的值。该函数应该是无副作用的，因为当由于线程间的竞争导致尝试更新失败时，它可能会被重新应用。
     *
     * @param obj 需要获取和设置字段的对象
     * @param updateFunction 一个无副作用的函数
     * @return 更新前的值
     * @since 1.8
     */
    public final V getAndUpdate(T obj, UnaryOperator<V> updateFunction) {
        V prev, next;
        do {
            prev = get(obj);
            next = updateFunction.apply(prev);
        } while (!compareAndSet(obj, prev, next));
        return prev;
    }

    /**
     * 原子地更新由这个更新器管理的给定对象的字段，返回更新后的值。该函数应该是无副作用的，因为当由于线程间的竞争导致尝试更新失败时，它可能会被重新应用。
     *
     * @param obj 需要获取和设置字段的对象
     * @param updateFunction 一个无副作用的函数
     * @return 更新后的值
     * @since 1.8
     */
    public final V updateAndGet(T obj, UnaryOperator<V> updateFunction) {
        V prev, next;
        do {
            prev = get(obj);
            next = updateFunction.apply(prev);
        } while (!compareAndSet(obj, prev, next));
        return next;
    }

    /**
     * 原子地更新由这个更新器管理的给定对象的字段，使用给定的函数对当前值和给定值进行操作，返回更新前的值。该函数应该是无副作用的，因为当由于线程间的竞争导致尝试更新失败时，它可能会被重新应用。该函数以当前值作为第一个参数，给定的更新值作为第二个参数。
     *
     * @param obj 需要获取和设置字段的对象
     * @param x 更新值
     * @param accumulatorFunction 一个无副作用的二元函数
     * @return 更新前的值
     * @since 1.8
     */
    public final V getAndAccumulate(T obj, V x,
                                    BinaryOperator<V> accumulatorFunction) {
        V prev, next;
        do {
            prev = get(obj);
            next = accumulatorFunction.apply(prev, x);
        } while (!compareAndSet(obj, prev, next));
        return prev;
    }

    /**
     * 原子地更新由这个更新器管理的给定对象的字段，使用给定的函数对当前值和给定值进行操作，返回更新后的值。该函数应该是无副作用的，因为当由于线程间的竞争导致尝试更新失败时，它可能会被重新应用。该函数以当前值作为第一个参数，给定的更新值作为第二个参数。
     *
     * @param obj 需要获取和设置字段的对象
     * @param x 更新值
     * @param accumulatorFunction 一个无副作用的二元函数
     * @return 更新后的值
     * @since 1.8
     */
    public final V accumulateAndGet(T obj, V x,
                                    BinaryOperator<V> accumulatorFunction) {
        V prev, next;
        do {
            prev = get(obj);
            next = accumulatorFunction.apply(prev, x);
        } while (!compareAndSet(obj, prev, next));
        return next;
    }

    private static final class AtomicReferenceFieldUpdaterImpl<T,V>
        extends AtomicReferenceFieldUpdater<T,V> {
        private static final sun.misc.Unsafe U = sun.misc.Unsafe.getUnsafe();
        private final long offset;
        /**
         * 如果字段是受保护的，则是构造更新器的子类，否则与 tclass 相同
         */
        private final Class<?> cclass;
        /** 持有字段的类 */
        private final Class<T> tclass;
        /** 字段值类型 */
        private final Class<V> vclass;

        /*
         * 所有更新方法中的内部类型检查包含内部内联优化，用于检查最常见的两种情况：类是 final 的（在这种情况下，简单的 getClass 比较就足够了）或类型为 Object（在这种情况下，不需要检查，因为所有对象都是 Object 的实例）。Object 情况通过在构造函数中将 vclass 设置为 null 来简单处理。当这些更快的筛选失败时，将调用 targetCheck 和 updateCheck 方法。
         */

        AtomicReferenceFieldUpdaterImpl(final Class<T> tclass,
                                        final Class<V> vclass,
                                        final String fieldName,
                                        final Class<?> caller) {
            final Field field;
            final Class<?> fieldClass;
            final int modifiers;
            try {
                field = AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Field>() {
                        public Field run() throws NoSuchFieldException {
                            return tclass.getDeclaredField(fieldName);
                        }
                    });
                modifiers = field.getModifiers();
                sun.reflect.misc.ReflectUtil.ensureMemberAccess(
                    caller, tclass, null, modifiers);
                ClassLoader cl = tclass.getClassLoader();
                ClassLoader ccl = caller.getClassLoader();
                if ((ccl != null) && (ccl != cl) &&
                    ((cl == null) || !isAncestor(cl, ccl))) {
                    sun.reflect.misc.ReflectUtil.checkPackageAccess(tclass);
                }
                fieldClass = field.getType();
            } catch (PrivilegedActionException pae) {
                throw new RuntimeException(pae.getException());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }


                        if (vclass != fieldClass)
                throw new ClassCastException();
            if (vclass.isPrimitive())
                throw new IllegalArgumentException("必须是引用类型");

            if (!Modifier.isVolatile(modifiers))
                throw new IllegalArgumentException("必须是易失类型");

            // 访问受保护的字段成员仅限于访问类的接收者，或者是其子类，并且访问类必须是受保护成员定义类的子类（或包兄弟类）。
            // 如果更新器引用的是当前包外的声明类的受保护字段，则接收者参数将被缩小为访问类的类型。
            this.cclass = (Modifier.isProtected(modifiers) &&
                           tclass.isAssignableFrom(caller) &&
                           !isSamePackage(tclass, caller))
                          ? caller : tclass;
            this.tclass = tclass;
            this.vclass = vclass;
            this.offset = U.objectFieldOffset(field);
        }

        /**
         * 如果第二个类加载器可以在第一个类加载器的委托链中找到，则返回 true。
         * 等同于不可访问的：first.isAncestor(second)。
         */
        private static boolean isAncestor(ClassLoader first, ClassLoader second) {
            ClassLoader acl = first;
            do {
                acl = acl.getParent();
                if (second == acl) {
                    return true;
                }
            } while (acl != null);
            return false;
        }

        /**
         * 如果两个类具有相同的类加载器和包限定符，则返回 true。
         */
        private static boolean isSamePackage(Class<?> class1, Class<?> class2) {
            return class1.getClassLoader() == class2.getClassLoader()
                   && Objects.equals(getPackageName(class1), getPackageName(class2));
        }

        private static String getPackageName(Class<?> cls) {
            String cn = cls.getName();
            int dot = cn.lastIndexOf('.');
            return (dot != -1) ? cn.substring(0, dot) : "";
        }

        /**
         * 检查目标参数是否是 cclass 的实例。如果失败，抛出 cause。
         */
        private final void accessCheck(T obj) {
            if (!cclass.isInstance(obj))
                throwAccessCheckException(obj);
        }

        /**
         * 如果 accessCheck 失败是由于受保护的访问，则抛出访问异常，否则抛出 ClassCastException。
         */
        private final void throwAccessCheckException(T obj) {
            if (cclass == tclass)
                throw new ClassCastException();
            else
                throw new RuntimeException(
                    new IllegalAccessException(
                        "类 " +
                        cclass.getName() +
                        " 不能使用 " +
                        obj.getClass().getName() +
                        " 的实例访问类 " +
                        tclass.getName() +
                        " 的受保护成员"));
        }

        private final void valueCheck(V v) {
            if (v != null && !(vclass.isInstance(v)))
                throwCCE();
        }

        static void throwCCE() {
            throw new ClassCastException();
        }

        public final boolean compareAndSet(T obj, V expect, V update) {
            accessCheck(obj);
            valueCheck(update);
            return U.compareAndSwapObject(obj, offset, expect, update);
        }

        public final boolean weakCompareAndSet(T obj, V expect, V update) {
            // 目前实现与强形式相同
            accessCheck(obj);
            valueCheck(update);
            return U.compareAndSwapObject(obj, offset, expect, update);
        }

        public final void set(T obj, V newValue) {
            accessCheck(obj);
            valueCheck(newValue);
            U.putObjectVolatile(obj, offset, newValue);
        }

        public final void lazySet(T obj, V newValue) {
            accessCheck(obj);
            valueCheck(newValue);
            U.putOrderedObject(obj, offset, newValue);
        }

        @SuppressWarnings("unchecked")
        public final V get(T obj) {
            accessCheck(obj);
            return (V)U.getObjectVolatile(obj, offset);
        }

        @SuppressWarnings("unchecked")
        public final V getAndSet(T obj, V newValue) {
            accessCheck(obj);
            valueCheck(newValue);
            return (V)U.getAndSetObject(obj, offset, newValue);
        }
    }
}
