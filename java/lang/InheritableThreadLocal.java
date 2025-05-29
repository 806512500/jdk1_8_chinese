/*
 * Copyright (c) 1998, 2012, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;
import java.lang.ref.*;

/**
 * 该类扩展了 <tt>ThreadLocal</tt> 以提供从父线程到子线程的值继承：当创建子线程时，
 * 子线程会接收所有父线程具有值的可继承线程局部变量的初始值。通常，子线程的值将与父线程的值相同；
 * 然而，子线程的值可以通过在此类中覆盖 <tt>childValue</tt> 方法来设置为父线程值的任意函数。
 *
 * <p>当需要自动将变量中维护的每个线程属性（例如，用户ID、事务ID）传输到创建的任何子线程时，
 * 优先使用可继承的线程局部变量而不是普通的线程局部变量。
 *
 * @author  Josh Bloch and Doug Lea
 * @see     ThreadLocal
 * @since   1.2
 */

public class InheritableThreadLocal<T> extends ThreadLocal<T> {
    /**
     * 计算此可继承线程局部变量在子线程创建时作为父线程值函数的子线程初始值。
     * 该方法在子线程启动之前由父线程内部调用。
     * <p>
     * 该方法仅返回其输入参数，如果需要不同的行为，应覆盖此方法。
     *
     * @param parentValue 父线程的值
     * @return 子线程的初始值
     */
    protected T childValue(T parentValue) {
        return parentValue;
    }

    /**
     * 获取与 ThreadLocal 关联的映射。
     *
     * @param t 当前线程
     */
    ThreadLocalMap getMap(Thread t) {
       return t.inheritableThreadLocals;
    }

    /**
     * 创建与 ThreadLocal 关联的映射。
     *
     * @param t 当前线程
     * @param firstValue 表的初始条目的值。
     */
    void createMap(Thread t, T firstValue) {
        t.inheritableThreadLocals = new ThreadLocalMap(this, firstValue);
    }
}
