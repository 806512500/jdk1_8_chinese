
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
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;

/**
 * 一个基于反射的实用工具，允许对指定类的指定 {@code volatile} 引用字段进行原子更新。此类设计用于在原子数据结构中使用，其中同一节点的多个引用字段可以独立地进行原子更新。例如，树节点可以声明为
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
 * <p>请注意，此类中的 {@code compareAndSet} 方法的保证比其他原子类中的弱。因为此类无法确保所有对字段的使用都适合原子访问，所以它只能保证相对于其他对 {@code compareAndSet} 和 {@code set} 的调用的原子性。
 *
 * @since 1.5
 * @author Doug Lea
 * @param <T> 持有可更新字段的对象的类型
 * @param <V> 字段的类型
 */
public abstract class AtomicReferenceFieldUpdater<T,V> {

    /**
     * 创建并返回一个用于具有给定字段的对象的更新器。Class 参数用于检查反射类型和泛型类型是否匹配。
     *
     * @param tclass 持有字段的对象的类
     * @param vclass 字段的类
     * @param fieldName 要更新的字段的名称
     * @param <U> tclass 的实例类型
     * @param <W> vclass 的实例类型
     * @return 更新器
     * @throws ClassCastException 如果字段类型不正确
     * @throws IllegalArgumentException 如果字段不是 volatile 类型
     * @throws RuntimeException 如果类不包含字段或类型不正确，或者根据 Java 语言访问控制，字段对调用者不可访问，则抛出包含反射异常的异常
     */
    @CallerSensitive
    public static <U,W> AtomicReferenceFieldUpdater<U,W> newUpdater(Class<U> tclass,
                                                                    Class<W> vclass,
                                                                    String fieldName) {
        return new AtomicReferenceFieldUpdaterImpl<U,W>
            (tclass, vclass, fieldName, Reflection.getCallerClass());
    }

    /**
     * 供子类使用的无操作保护构造函数。
     */
    protected AtomicReferenceFieldUpdater() {
    }

    /**
     * 如果当前值 {@code ==} 期望值，则原子地将由该更新器管理的给定对象的字段设置为给定的更新值。此方法保证相对于其他对 {@code compareAndSet} 和 {@code set} 的调用是原子的，但不一定相对于字段的其他更改。
     *
     * @param obj 要条件设置字段的对象
     * @param expect 期望值
     * @param update 新值
     * @return 如果成功则返回 {@code true}
     */
    public abstract boolean compareAndSet(T obj, V expect, V update);

    /**
     * 如果当前值 {@code ==} 期望值，则原子地将由该更新器管理的给定对象的字段设置为给定的更新值。此方法保证相对于其他对 {@code compareAndSet} 和 {@code set} 的调用是原子的，但不一定相对于字段的其他更改。
     *
     * <p><a href="package-summary.html#weakCompareAndSet">可能会失败且不提供顺序保证</a>，因此很少是 {@code compareAndSet} 的适当替代品。
     *
     * @param obj 要条件设置字段的对象
     * @param expect 期望值
     * @param update 新值
     * @return 如果成功则返回 {@code true}
     */
    public abstract boolean weakCompareAndSet(T obj, V expect, V update);

    /**
     * 将由该更新器管理的给定对象的字段设置为给定的更新值。此操作保证相对于后续对 {@code compareAndSet} 的调用是 volatile 存储。
     *
     * @param obj 要设置字段的对象
     * @param newValue 新值
     */
    public abstract void set(T obj, V newValue);

    /**
     * 最终将由该更新器管理的给定对象的字段设置为给定的更新值。
     *
     * @param obj 要设置字段的对象
     * @param newValue 新值
     * @since 1.6
     */
    public abstract void lazySet(T obj, V newValue);

    /**
     * 获取由该更新器管理的给定对象的字段的当前值。
     *
     * @param obj 要获取字段的对象
     * @return 当前值
     */
    public abstract V get(T obj);

    /**
     * 原子地将由该更新器管理的给定对象的字段设置为给定值并返回旧值。
     *
     * @param obj 要获取和设置字段的对象
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

    /**
     * 原子地将由该更新器管理的给定对象的字段更新为应用给定函数的结果，返回旧值。函数应该是无副作用的，因为当由于线程之间的争用导致尝试更新失败时，可能会重新应用该函数。
     *
     * @param obj 要获取和设置字段的对象
     * @param updateFunction 无副作用的函数
     * @return 旧值
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
     * 原子地将由该更新器管理的给定对象的字段更新为应用给定函数的结果，返回更新后的值。函数应该是无副作用的，因为当由于线程之间的争用导致尝试更新失败时，可能会重新应用该函数。
     *
     * @param obj 要获取和设置字段的对象
     * @param updateFunction 无副作用的函数
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
     * 原子地将由该更新器管理的给定对象的字段更新为应用给定函数到当前值和给定值的结果，返回旧值。函数应该是无副作用的，因为当由于线程之间的争用导致尝试更新失败时，可能会重新应用该函数。函数应用于当前值作为第一个参数，给定更新作为第二个参数。
     *
     * @param obj 要获取和设置字段的对象
     * @param x 更新值
     * @param accumulatorFunction 无副作用的双参数函数
     * @return 旧值
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
     * 原子地将由该更新器管理的给定对象的字段更新为应用给定函数到当前值和给定值的结果，返回更新后的值。函数应该是无副作用的，因为当由于线程之间的争用导致尝试更新失败时，可能会重新应用该函数。函数应用于当前值作为第一个参数，给定更新作为第二个参数。
     *
     * @param obj 要获取和设置字段的对象
     * @param x 更新值
     * @param accumulatorFunction 无副作用的双参数函数
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
         * 如果字段受保护，则为构建更新器的子类，否则与 tclass 相同
         */
        private final Class<?> cclass;
        /** 持有字段的类 */
        private final Class<T> tclass;
        /** 字段值类型 */
        private final Class<V> vclass;

        /*
         * 内部类型检查包含内部内联优化，用于检查最常见的类是 final（在这种情况下，简单的 getClass 比较就足够了）或类型为 Object（在这种情况下，不需要检查，因为所有对象都是 Object 的实例）。Object 情况通过在构造函数中将 vclass 设置为 null 来处理。当这些更快的筛选失败时，调用 targetCheck 和 updateCheck 方法。
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
                throw new IllegalArgumentException("必须是 volatile 类型");

            // 访问受保护的字段成员仅限于访问类的接收者，或其子类，且访问类必须是受保护成员定义类的子类（或包兄弟类）。
            // 如果更新器引用的是当前包外的声明类的受保护字段，则接收者参数将缩小为访问类的类型。
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
         * 等效于不可访问的：first.isAncestor(second)。
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
         * 如果两个类具有相同的类加载器和包限定符，则返回 true
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
         * 检查目标参数是否是 cclass 的实例。如果失败，抛出异常。
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
                        "Class " +
                        cclass.getName() +
                        " can not access a protected member of class " +
                        tclass.getName() +
                        " using an instance of " +
                        obj.getClass().getName()));
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
            // 当前实现与强形式相同
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
