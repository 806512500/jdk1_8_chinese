
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
 * 由 Doug Lea 在 JCP JSR-166 专家组成员的帮助下编写，并根据
 * http://creativecommons.org/publicdomain/zero/1.0/ 解释的条款发布到公共领域。
 */

package java.util.concurrent.atomic;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Objects;
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;

/**
 * 一个基于反射的工具类，允许对指定类中的指定 {@code volatile long} 字段进行原子更新。
 * 本类设计用于原子数据结构，其中同一节点的多个字段可以独立地进行原子更新。
 *
 * <p>请注意，本类中 {@code compareAndSet} 方法的保证比其他原子类中的弱。
 * 由于本类无法确保所有字段的使用都适合原子访问，因此它只能保证相对于同一更新器上的其他 {@code compareAndSet} 和 {@code set} 调用的原子性。
 *
 * @since 1.5
 * @author Doug Lea
 * @param <T> 持有可更新字段的对象类型
 */
public abstract class AtomicLongFieldUpdater<T> {
    /**
     * 创建并返回一个用于具有给定字段的对象的更新器。
     * Class 参数用于检查反射类型和泛型类型是否匹配。
     *
     * @param tclass 持有字段的对象的类
     * @param fieldName 要更新的字段的名称
     * @param <U> tclass 的实例类型
     * @return 更新器
     * @throws IllegalArgumentException 如果字段不是 volatile long 类型
     * @throws RuntimeException 包含基于反射的异常，如果类不持有字段或类型错误，或者根据 Java 语言访问控制，字段对调用者不可访问
     */
    @CallerSensitive
    public static <U> AtomicLongFieldUpdater<U> newUpdater(Class<U> tclass,
                                                           String fieldName) {
        Class<?> caller = Reflection.getCallerClass();
        if (AtomicLong.VM_SUPPORTS_LONG_CAS)
            return new CASUpdater<U>(tclass, fieldName, caller);
        else
            return new LockedUpdater<U>(tclass, fieldName, caller);
    }

    /**
     * 供子类使用的保护性无操作构造函数。
     */
    protected AtomicLongFieldUpdater() {
    }

    /**
     * 如果给定对象的当前值 {@code ==} 期望值，则原子地将由本更新器管理的该对象的字段设置为给定的更新值。
     * 本方法保证相对于其他对 {@code compareAndSet} 和 {@code set} 的调用是原子的，但不一定相对于字段的其他更改。
     *
     * @param obj 要条件设置字段的对象
     * @param expect 期望值
     * @param update 新值
     * @return 如果成功则返回 {@code true}
     * @throws ClassCastException 如果 {@code obj} 不是构造函数中确定的持有字段的类的实例
     */
    public abstract boolean compareAndSet(T obj, long expect, long update);

    /**
     * 如果给定对象的当前值 {@code ==} 期望值，则原子地将由本更新器管理的该对象的字段设置为给定的更新值。
     * 本方法保证相对于其他对 {@code compareAndSet} 和 {@code set} 的调用是原子的，但不一定相对于字段的其他更改。
     *
     * <p><a href="package-summary.html#weakCompareAndSet">可能会无故失败且不提供顺序保证</a>，因此很少是 {@code compareAndSet} 的合适替代方案。
     *
     * @param obj 要条件设置字段的对象
     * @param expect 期望值
     * @param update 新值
     * @return 如果成功则返回 {@code true}
     * @throws ClassCastException 如果 {@code obj} 不是构造函数中确定的持有字段的类的实例
     */
    public abstract boolean weakCompareAndSet(T obj, long expect, long update);

    /**
     * 将由本更新器管理的给定对象的字段设置为给定的更新值。此操作保证相对于后续对 {@code compareAndSet} 的调用作为 volatile 存储。
     *
     * @param obj 要设置字段的对象
     * @param newValue 新值
     */
    public abstract void set(T obj, long newValue);

    /**
     * 最终将由本更新器管理的给定对象的字段设置为给定的更新值。
     *
     * @param obj 要设置字段的对象
     * @param newValue 新值
     * @since 1.6
     */
    public abstract void lazySet(T obj, long newValue);

    /**
     * 获取由本更新器管理的给定对象的字段的当前值。
     *
     * @param obj 要获取字段的对象
     * @return 当前值
     */
    public abstract long get(T obj);

    /**
     * 原子地将由本更新器管理的给定对象的字段设置为给定值并返回旧值。
     *
     * @param obj 要获取和设置字段的对象
     * @param newValue 新值
     * @return 旧值
     */
    public long getAndSet(T obj, long newValue) {
        long prev;
        do {
            prev = get(obj);
        } while (!compareAndSet(obj, prev, newValue));
        return prev;
    }

    /**
     * 原子地将由本更新器管理的给定对象的字段的当前值加一。
     *
     * @param obj 要获取和设置字段的对象
     * @return 旧值
     */
    public long getAndIncrement(T obj) {
        long prev, next;
        do {
            prev = get(obj);
            next = prev + 1;
        } while (!compareAndSet(obj, prev, next));
        return prev;
    }


                /**
     * 原子地将给定对象的由该更新器管理的字段的当前值减一。
     *
     * @param obj 要获取和设置其字段的对象
     * @return 之前的值
     */
    public long getAndDecrement(T obj) {
        long prev, next;
        do {
            prev = get(obj);
            next = prev - 1;
        } while (!compareAndSet(obj, prev, next));
        return prev;
    }

    /**
     * 原子地将给定值加到给定对象的由该更新器管理的字段的当前值上。
     *
     * @param obj 要获取和设置其字段的对象
     * @param delta 要加的值
     * @return 之前的值
     */
    public long getAndAdd(T obj, long delta) {
        long prev, next;
        do {
            prev = get(obj);
            next = prev + delta;
        } while (!compareAndSet(obj, prev, next));
        return prev;
    }

    /**
     * 原子地将给定对象的由该更新器管理的字段的当前值加一。
     *
     * @param obj 要获取和设置其字段的对象
     * @return 更新后的值
     */
    public long incrementAndGet(T obj) {
        long prev, next;
        do {
            prev = get(obj);
            next = prev + 1;
        } while (!compareAndSet(obj, prev, next));
        return next;
    }

    /**
     * 原子地将给定对象的由该更新器管理的字段的当前值减一。
     *
     * @param obj 要获取和设置其字段的对象
     * @return 更新后的值
     */
    public long decrementAndGet(T obj) {
        long prev, next;
        do {
            prev = get(obj);
            next = prev - 1;
        } while (!compareAndSet(obj, prev, next));
        return next;
    }

    /**
     * 原子地将给定值加到给定对象的由该更新器管理的字段的当前值上。
     *
     * @param obj 要获取和设置其字段的对象
     * @param delta 要加的值
     * @return 更新后的值
     */
    public long addAndGet(T obj, long delta) {
        long prev, next;
        do {
            prev = get(obj);
            next = prev + delta;
        } while (!compareAndSet(obj, prev, next));
        return next;
    }

    /**
     * 原子地使用给定函数对给定对象的由该更新器管理的字段进行更新，返回之前的值。该函数应该是没有副作用的，因为当线程间竞争导致尝试更新失败时，它可能会被重新应用。
     *
     * @param obj 要获取和设置其字段的对象
     * @param updateFunction 没有副作用的函数
     * @return 之前的值
     * @since 1.8
     */
    public final long getAndUpdate(T obj, LongUnaryOperator updateFunction) {
        long prev, next;
        do {
            prev = get(obj);
            next = updateFunction.applyAsLong(prev);
        } while (!compareAndSet(obj, prev, next));
        return prev;
    }

    /**
     * 原子地使用给定函数对给定对象的由该更新器管理的字段进行更新，返回更新后的值。该函数应该是没有副作用的，因为当线程间竞争导致尝试更新失败时，它可能会被重新应用。
     *
     * @param obj 要获取和设置其字段的对象
     * @param updateFunction 没有副作用的函数
     * @return 更新后的值
     * @since 1.8
     */
    public final long updateAndGet(T obj, LongUnaryOperator updateFunction) {
        long prev, next;
        do {
            prev = get(obj);
            next = updateFunction.applyAsLong(prev);
        } while (!compareAndSet(obj, prev, next));
        return next;
    }

    /**
     * 原子地使用给定函数对给定对象的由该更新器管理的字段的当前值和给定值进行操作，返回之前的值。该函数应该是没有副作用的，因为当线程间竞争导致尝试更新失败时，它可能会被重新应用。该函数以当前值作为第一个参数，给定更新值作为第二个参数。
     *
     * @param obj 要获取和设置其字段的对象
     * @param x 更新值
     * @param accumulatorFunction 没有副作用的二元函数
     * @return 之前的值
     * @since 1.8
     */
    public final long getAndAccumulate(T obj, long x,
                                       LongBinaryOperator accumulatorFunction) {
        long prev, next;
        do {
            prev = get(obj);
            next = accumulatorFunction.applyAsLong(prev, x);
        } while (!compareAndSet(obj, prev, next));
        return prev;
    }

    /**
     * 原子地使用给定函数对给定对象的由该更新器管理的字段的当前值和给定值进行操作，返回更新后的值。该函数应该是没有副作用的，因为当线程间竞争导致尝试更新失败时，它可能会被重新应用。该函数以当前值作为第一个参数，给定更新值作为第二个参数。
     *
     * @param obj 要获取和设置其字段的对象
     * @param x 更新值
     * @param accumulatorFunction 没有副作用的二元函数
     * @return 更新后的值
     * @since 1.8
     */
    public final long accumulateAndGet(T obj, long x,
                                       LongBinaryOperator accumulatorFunction) {
        long prev, next;
        do {
            prev = get(obj);
            next = accumulatorFunction.applyAsLong(prev, x);
        } while (!compareAndSet(obj, prev, next));
        return next;
    }

    private static final class CASUpdater<T> extends AtomicLongFieldUpdater<T> {
        private static final sun.misc.Unsafe U = sun.misc.Unsafe.getUnsafe();
        private final long offset;
        /**
         * 如果字段是受保护的，则为构造更新器的子类，否则与 tclass 相同
         */
        private final Class<?> cclass;
        /** 持有该字段的类 */
        private final Class<T> tclass;


                    CASUpdater(final Class<T> tclass, final String fieldName,
                   final Class<?> caller) {
            final Field field;
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
            } catch (PrivilegedActionException pae) {
                throw new RuntimeException(pae.getException());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

            if (field.getType() != long.class)
                throw new IllegalArgumentException("必须是 long 类型");

            if (!Modifier.isVolatile(modifiers))
                throw new IllegalArgumentException("必须是 volatile 类型");

            // 访问受保护的字段成员仅限于访问类的接收者，或者其子类，并且访问类必须是受保护成员定义类的子类（或包兄弟类）。
            // 如果更新器引用了当前包外声明类的受保护字段，则接收者参数将被缩小为访问类的类型。
            this.cclass = (Modifier.isProtected(modifiers) &&
                           tclass.isAssignableFrom(caller) &&
                           !isSamePackage(tclass, caller))
                          ? caller : tclass;
            this.tclass = tclass;
            this.offset = U.objectFieldOffset(field);
        }

        /**
         * 检查目标参数是否为 cclass 的实例。如果失败，抛出异常。
         */
        private final void accessCheck(T obj) {
            if (!cclass.isInstance(obj))
                throwAccessCheckException(obj);
        }

        /**
         * 如果 accessCheck 失败是由于受保护访问，抛出访问异常，否则抛出 ClassCastException。
         */
        private final void throwAccessCheckException(T obj) {
            if (cclass == tclass)
                throw new ClassCastException();
            else
                throw new RuntimeException(
                    new IllegalAccessException(
                        "类 " +
                        cclass.getName() +
                        " 无法使用 " +
                        obj.getClass().getName() +
                        " 的实例访问类 " +
                        tclass.getName() +
                        " 的受保护成员"));
        }

        public final boolean compareAndSet(T obj, long expect, long update) {
            accessCheck(obj);
            return U.compareAndSwapLong(obj, offset, expect, update);
        }

        public final boolean weakCompareAndSet(T obj, long expect, long update) {
            accessCheck(obj);
            return U.compareAndSwapLong(obj, offset, expect, update);
        }

        public final void set(T obj, long newValue) {
            accessCheck(obj);
            U.putLongVolatile(obj, offset, newValue);
        }

        public final void lazySet(T obj, long newValue) {
            accessCheck(obj);
            U.putOrderedLong(obj, offset, newValue);
        }

        public final long get(T obj) {
            accessCheck(obj);
            return U.getLongVolatile(obj, offset);
        }

        public final long getAndSet(T obj, long newValue) {
            accessCheck(obj);
            return U.getAndSetLong(obj, offset, newValue);
        }

        public final long getAndAdd(T obj, long delta) {
            accessCheck(obj);
            return U.getAndAddLong(obj, offset, delta);
        }

        public final long getAndIncrement(T obj) {
            return getAndAdd(obj, 1);
        }

        public final long getAndDecrement(T obj) {
            return getAndAdd(obj, -1);
        }

        public final long incrementAndGet(T obj) {
            return getAndAdd(obj, 1) + 1;
        }

        public final long decrementAndGet(T obj) {
            return getAndAdd(obj, -1) - 1;
        }

        public final long addAndGet(T obj, long delta) {
            return getAndAdd(obj, delta) + delta;
        }
    }

    private static final class LockedUpdater<T> extends AtomicLongFieldUpdater<T> {
        private static final sun.misc.Unsafe U = sun.misc.Unsafe.getUnsafe();
        private final long offset;
        /**
         * 如果字段受保护，则为构建更新器的子类，否则与 tclass 相同
         */
        private final Class<?> cclass;
        /** 持有字段的类 */
        private final Class<T> tclass;

        LockedUpdater(final Class<T> tclass, final String fieldName,
                      final Class<?> caller) {
            Field field = null;
            int modifiers = 0;
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
            } catch (PrivilegedActionException pae) {
                throw new RuntimeException(pae.getException());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }


                        if (field.getType() != long.class)
                throw new IllegalArgumentException("必须是 long 类型");

            if (!Modifier.isVolatile(modifiers))
                throw new IllegalArgumentException("必须是 volatile 类型");

            // 访问受保护的字段成员仅限于访问类的接收者，或者是其子类，并且访问类必须是受保护成员定义类的子类（或包兄弟类）。
            // 如果更新器引用的是当前包外的声明类的受保护字段，则接收者参数将被缩小为访问类的类型。
            this.cclass = (Modifier.isProtected(modifiers) &&
                           tclass.isAssignableFrom(caller) &&
                           !isSamePackage(tclass, caller))
                          ? caller : tclass;
            this.tclass = tclass;
            this.offset = U.objectFieldOffset(field);
        }

        /**
         * 检查目标参数是否为 cclass 的实例。如果失败，抛出 cause。
         */
        private final void accessCheck(T obj) {
            if (!cclass.isInstance(obj))
                throw accessCheckException(obj);
        }

        /**
         * 如果 accessCheck 失败是由于受保护的访问，则返回访问异常，否则返回 ClassCastException。
         */
        private final RuntimeException accessCheckException(T obj) {
            if (cclass == tclass)
                return new ClassCastException();
            else
                return new RuntimeException(
                    new IllegalAccessException(
                        "类 " +
                        cclass.getName() +
                        " 不能访问类 " +
                        tclass.getName() +
                        " 的受保护成员，使用的是 " +
                        obj.getClass().getName() +
                        " 的实例"));
        }

        public final boolean compareAndSet(T obj, long expect, long update) {
            accessCheck(obj);
            synchronized (this) {
                long v = U.getLong(obj, offset);
                if (v != expect)
                    return false;
                U.putLong(obj, offset, update);
                return true;
            }
        }

        public final boolean weakCompareAndSet(T obj, long expect, long update) {
            return compareAndSet(obj, expect, update);
        }

        public final void set(T obj, long newValue) {
            accessCheck(obj);
            synchronized (this) {
                U.putLong(obj, offset, newValue);
            }
        }

        public final void lazySet(T obj, long newValue) {
            set(obj, newValue);
        }

        public final long get(T obj) {
            accessCheck(obj);
            synchronized (this) {
                return U.getLong(obj, offset);
            }
        }
    }

    /**
     * 如果第二个类加载器可以在第一个类加载器的委托链中找到，则返回 true。
     * 等效于不可访问的方法：first.isAncestor(second)。
     */
    static boolean isAncestor(ClassLoader first, ClassLoader second) {
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
}
