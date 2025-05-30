
/*
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

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Objects;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;

/**
 * 一个基于反射的实用工具，允许对指定类的指定 {@code volatile int} 字段进行原子更新。
 * 该类设计用于原子数据结构，其中同一节点的多个字段可以独立地进行原子更新。
 *
 * <p>请注意，此类中的 {@code compareAndSet} 方法的保证比其他原子类中的弱。
 * 由于该类无法确保所有对字段的使用都适合原子访问，因此它只能保证相对于其他对
 * {@code compareAndSet} 和 {@code set} 的调用是原子的。
 *
 * @since 1.5
 * @author Doug Lea
 * @param <T> 持有可更新字段的对象的类型
 */
public abstract class AtomicIntegerFieldUpdater<T> {
    /**
     * 为具有给定字段的对象创建并返回一个更新器。
     * Class 参数用于检查反射类型和泛型类型是否匹配。
     *
     * @param tclass 持有字段的对象的类
     * @param fieldName 要更新的字段的名称
     * @param <U> tclass 的实例类型
     * @return 更新器
     * @throws IllegalArgumentException 如果字段不是 volatile int 类型
     * @throws RuntimeException 如果类不包含字段或类型错误，或者根据 Java 语言访问控制，字段对调用者不可访问
     */
    @CallerSensitive
    public static <U> AtomicIntegerFieldUpdater<U> newUpdater(Class<U> tclass,
                                                              String fieldName) {
        return new AtomicIntegerFieldUpdaterImpl<U>
            (tclass, fieldName, Reflection.getCallerClass());
    }

    /**
     * 保护的无操作构造函数，供子类使用。
     */
    protected AtomicIntegerFieldUpdater() {
    }

    /**
     * 如果当前值 {@code ==} 期望值，则原子地将由该更新器管理的给定对象的字段设置为给定的更新值。
     * 该方法保证相对于其他对 {@code compareAndSet} 和 {@code set} 的调用是原子的，但不一定相对于字段的其他更改。
     *
     * @param obj 要条件设置字段的对象
     * @param expect 期望值
     * @param update 新值
     * @return 如果成功则返回 {@code true}
     * @throws ClassCastException 如果 {@code obj} 不是构造函数中确定的类的实例
     */
    public abstract boolean compareAndSet(T obj, int expect, int update);

    /**
     * 如果当前值 {@code ==} 期望值，则原子地将由该更新器管理的给定对象的字段设置为给定的更新值。
     * 该方法保证相对于其他对 {@code compareAndSet} 和 {@code set} 的调用是原子的，但不一定相对于字段的其他更改。
     *
     * <p><a href="package-summary.html#weakCompareAndSet">可能会失败，并且不提供顺序保证</a>，因此很少是 {@code compareAndSet} 的合适替代方案。
     *
     * @param obj 要条件设置字段的对象
     * @param expect 期望值
     * @param update 新值
     * @return 如果成功则返回 {@code true}
     * @throws ClassCastException 如果 {@code obj} 不是构造函数中确定的类的实例
     */
    public abstract boolean weakCompareAndSet(T obj, int expect, int update);

    /**
     * 将由该更新器管理的给定对象的字段设置为给定的更新值。此操作保证相对于后续对 {@code compareAndSet} 的调用是 volatile 存储。
     *
     * @param obj 要设置字段的对象
     * @param newValue 新值
     */
    public abstract void set(T obj, int newValue);

    /**
     * 最终将由该更新器管理的给定对象的字段设置为给定的更新值。
     *
     * @param obj 要设置字段的对象
     * @param newValue 新值
     * @since 1.6
     */
    public abstract void lazySet(T obj, int newValue);

    /**
     * 获取由该更新器管理的给定对象的字段的当前值。
     *
     * @param obj 要获取字段的对象
     * @return 当前值
     */
    public abstract int get(T obj);

    /**
     * 原子地将由该更新器管理的给定对象的字段设置为给定值并返回旧值。
     *
     * @param obj 要获取并设置字段的对象
     * @param newValue 新值
     * @return 旧值
     */
    public int getAndSet(T obj, int newValue) {
        int prev;
        do {
            prev = get(obj);
        } while (!compareAndSet(obj, prev, newValue));
        return prev;
    }

    /**
     * 原子地将由该更新器管理的给定对象的字段的当前值加一。
     *
     * @param obj 要获取并设置字段的对象
     * @return 旧值
     */
    public int getAndIncrement(T obj) {
        int prev, next;
        do {
            prev = get(obj);
            next = prev + 1;
        } while (!compareAndSet(obj, prev, next));
        return prev;
    }

    /**
     * 原子地将由该更新器管理的给定对象的字段的当前值减一。
     *
     * @param obj 要获取并设置字段的对象
     * @return 旧值
     */
    public int getAndDecrement(T obj) {
        int prev, next;
        do {
            prev = get(obj);
            next = prev - 1;
        } while (!compareAndSet(obj, prev, next));
        return prev;
    }

    /**
     * 原子地将给定值加到由该更新器管理的给定对象的字段的当前值。
     *
     * @param obj 要获取并设置字段的对象
     * @param delta 要加的值
     * @return 旧值
     */
    public int getAndAdd(T obj, int delta) {
        int prev, next;
        do {
            prev = get(obj);
            next = prev + delta;
        } while (!compareAndSet(obj, prev, next));
        return prev;
    }

    /**
     * 原子地将由该更新器管理的给定对象的字段的当前值加一。
     *
     * @param obj 要获取并设置字段的对象
     * @return 更新后的值
     */
    public int incrementAndGet(T obj) {
        int prev, next;
        do {
            prev = get(obj);
            next = prev + 1;
        } while (!compareAndSet(obj, prev, next));
        return next;
    }

    /**
     * 原子地将由该更新器管理的给定对象的字段的当前值减一。
     *
     * @param obj 要获取并设置字段的对象
     * @return 更新后的值
     */
    public int decrementAndGet(T obj) {
        int prev, next;
        do {
            prev = get(obj);
            next = prev - 1;
        } while (!compareAndSet(obj, prev, next));
        return next;
    }

    /**
     * 原子地将给定值加到由该更新器管理的给定对象的字段的当前值。
     *
     * @param obj 要获取并设置字段的对象
     * @param delta 要加的值
     * @return 更新后的值
     */
    public int addAndGet(T obj, int delta) {
        int prev, next;
        do {
            prev = get(obj);
            next = prev + delta;
        } while (!compareAndSet(obj, prev, next));
        return next;
    }

    /**
     * 原子地将由该更新器管理的给定对象的字段更新为应用给定函数的结果，返回旧值。该函数应该是无副作用的，因为当线程间竞争导致尝试更新失败时，可能会重新应用该函数。
     *
     * @param obj 要获取并设置字段的对象
     * @param updateFunction 无副作用的函数
     * @return 旧值
     * @since 1.8
     */
    public final int getAndUpdate(T obj, IntUnaryOperator updateFunction) {
        int prev, next;
        do {
            prev = get(obj);
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSet(obj, prev, next));
        return prev;
    }

    /**
     * 原子地将由该更新器管理的给定对象的字段更新为应用给定函数的结果，返回更新后的值。该函数应该是无副作用的，因为当线程间竞争导致尝试更新失败时，可能会重新应用该函数。
     *
     * @param obj 要获取并设置字段的对象
     * @param updateFunction 无副作用的函数
     * @return 更新后的值
     * @since 1.8
     */
    public final int updateAndGet(T obj, IntUnaryOperator updateFunction) {
        int prev, next;
        do {
            prev = get(obj);
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSet(obj, prev, next));
        return next;
    }

    /**
     * 原子地将由该更新器管理的给定对象的字段更新为应用给定函数到当前值和给定值的结果，返回旧值。该函数应该是无副作用的，因为当线程间竞争导致尝试更新失败时，可能会重新应用该函数。该函数以当前值作为第一个参数，给定更新作为第二个参数。
     *
     * @param obj 要获取并设置字段的对象
     * @param x 更新值
     * @param accumulatorFunction 无副作用的二元函数
     * @return 旧值
     * @since 1.8
     */
    public final int getAndAccumulate(T obj, int x,
                                      IntBinaryOperator accumulatorFunction) {
        int prev, next;
        do {
            prev = get(obj);
            next = accumulatorFunction.applyAsInt(prev, x);
        } while (!compareAndSet(obj, prev, next));
        return prev;
    }

    /**
     * 原子地将由该更新器管理的给定对象的字段更新为应用给定函数到当前值和给定值的结果，返回更新后的值。该函数应该是无副作用的，因为当线程间竞争导致尝试更新失败时，可能会重新应用该函数。该函数以当前值作为第一个参数，给定更新作为第二个参数。
     *
     * @param obj 要获取并设置字段的对象
     * @param x 更新值
     * @param accumulatorFunction 无副作用的二元函数
     * @return 更新后的值
     * @since 1.8
     */
    public final int accumulateAndGet(T obj, int x,
                                      IntBinaryOperator accumulatorFunction) {
        int prev, next;
        do {
            prev = get(obj);
            next = accumulatorFunction.applyAsInt(prev, x);
        } while (!compareAndSet(obj, prev, next));
        return next;
    }

    /**
     * 标准的 Hotspot 实现，使用内在函数。
     */
    private static final class AtomicIntegerFieldUpdaterImpl<T>
        extends AtomicIntegerFieldUpdater<T> {
        private static final sun.misc.Unsafe U = sun.misc.Unsafe.getUnsafe();
        private final long offset;
        /**
         * 如果字段是受保护的，则为构造更新器的子类，否则与 tclass 相同
         */
        private final Class<?> cclass;
        /** 持有字段的类 */
        private final Class<T> tclass;

        AtomicIntegerFieldUpdaterImpl(final Class<T> tclass,
                                      final String fieldName,
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


                        if (field.getType() != int.class)
                throw new IllegalArgumentException("必须是整型");

            if (!Modifier.isVolatile(modifiers))
                throw new IllegalArgumentException("必须是易失类型");

            // 访问受保护的字段成员仅限于访问类本身或其子类的接收者，并且访问类必须是受保护成员定义类的子类（或包内兄弟类）。
            // 如果更新器引用的是当前包外的声明类的受保护字段，则接收者参数将被缩小为访问类的类型。
            this.cclass = (Modifier.isProtected(modifiers) &&
                           tclass.isAssignableFrom(caller) &&
                           !isSamePackage(tclass, caller))
                          ? caller : tclass;
            this.tclass = tclass;
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
         * 检查目标参数是否为 cclass 的实例。如果失败，则抛出异常。
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
                        " 无法访问类 " +
                        tclass.getName() +
                        " 的受保护成员，使用实例 " +
                        obj.getClass().getName()));
        }

        public final boolean compareAndSet(T obj, int expect, int update) {
            accessCheck(obj);
            return U.compareAndSwapInt(obj, offset, expect, update);
        }

        public final boolean weakCompareAndSet(T obj, int expect, int update) {
            accessCheck(obj);
            return U.compareAndSwapInt(obj, offset, expect, update);
        }

        public final void set(T obj, int newValue) {
            accessCheck(obj);
            U.putIntVolatile(obj, offset, newValue);
        }

        public final void lazySet(T obj, int newValue) {
            accessCheck(obj);
            U.putOrderedInt(obj, offset, newValue);
        }

        public final int get(T obj) {
            accessCheck(obj);
            return U.getIntVolatile(obj, offset);
        }

        public final int getAndSet(T obj, int newValue) {
            accessCheck(obj);
            return U.getAndSetInt(obj, offset, newValue);
        }

        public final int getAndAdd(T obj, int delta) {
            accessCheck(obj);
            return U.getAndAddInt(obj, offset, delta);
        }

        public final int getAndIncrement(T obj) {
            return getAndAdd(obj, 1);
        }

        public final int getAndDecrement(T obj) {
            return getAndAdd(obj, -1);
        }

        public final int incrementAndGet(T obj) {
            return getAndAdd(obj, 1) + 1;
        }

        public final int decrementAndGet(T obj) {
            return getAndAdd(obj, -1) - 1;
        }

        public final int addAndGet(T obj, int delta) {
            return getAndAdd(obj, delta) + delta;
        }

    }
}
