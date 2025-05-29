/*
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
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

package java.util;

/**
 * 一个抽象的 {@code EventListener} 类的包装类，
 * 该类将一组额外的参数与监听器关联起来。
 * 子类必须提供额外参数的存储和访问方法。
 * <p>
 * 例如，支持命名属性的 bean
 * 会有一个用于添加 {@code PropertyChangeListener} 的两参数方法签名：
 * <pre>
 * public void addPropertyChangeListener(String propertyName,
 *                                       PropertyChangeListener listener)
 * </pre>
 * 如果 bean 还实现了零参数的获取监听器方法：
 * <pre>
 * public PropertyChangeListener[] getPropertyChangeListeners()
 * </pre>
 * 那么数组中可能包含内部的 {@code PropertyChangeListeners}，
 * 这些监听器同时也是 {@code PropertyChangeListenerProxy} 对象。
 * <p>
 * 如果调用方法对检索命名属性感兴趣，
 * 则必须测试元素是否为代理类。
 *
 * @since 1.4
 */
public abstract class EventListenerProxy<T extends EventListener>
        implements EventListener {

    private final T listener;

    /**
     * 为指定的监听器创建代理。
     *
     * @param listener  监听器对象
     */
    public EventListenerProxy(T listener) {
        this.listener = listener;
    }

    /**
     * 返回与代理关联的监听器。
     *
     * @return  与代理关联的监听器
     */
    public T getListener() {
        return this.listener;
    }
}
