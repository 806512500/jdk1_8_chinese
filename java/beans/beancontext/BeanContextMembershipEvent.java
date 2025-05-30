/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.beans.beancontext;

import java.util.EventObject;

import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * 一个 <code>BeanContextMembershipEvent</code> 封装了
 * 添加到或从特定 <code>BeanContext</code> 的成员中移除的子项列表。
 * 每当成功调用给定 <code>BeanContext</code> 实例的 add()、remove()、retainAll()、removeAll() 或 clear() 时，
 * 将触发此类事件的一个实例。
 * 对此类事件感兴趣的对象必须实现 <code>BeanContextMembershipListener</code> 接口，
 * 并且必须通过 <code>BeanContext</code> 的
 * <code>addBeanContextMembershipListener(BeanContextMembershipListener bcml)</code> 方法注册其意图。
 *
 * @author      Laurence P. G. Cable
 * @since       1.2
 * @see         java.beans.beancontext.BeanContext
 * @see         java.beans.beancontext.BeanContextEvent
 * @see         java.beans.beancontext.BeanContextMembershipListener
 */
public class BeanContextMembershipEvent extends BeanContextEvent {
    private static final long serialVersionUID = 3499346510334590959L;

    /**
     * 构造一个 BeanContextMembershipEvent
     *
     * @param bc        BeanContext 源
     * @param changes   受影响的子项
     * @throws NullPointerException 如果 <CODE>changes</CODE> 为 <CODE>null</CODE>
     */

    @SuppressWarnings("rawtypes")
    public BeanContextMembershipEvent(BeanContext bc, Collection changes) {
        super(bc);

        if (changes == null) throw new NullPointerException(
            "BeanContextMembershipEvent 构造函数:  changes 为 null。");

        children = changes;
    }

    /**
     * 构造一个 BeanContextMembershipEvent
     *
     * @param bc        BeanContext 源
     * @param changes   受影响的子项
     * @exception       NullPointerException 如果与此事件关联的 changes 为 null。
     */

    public BeanContextMembershipEvent(BeanContext bc, Object[] changes) {
        super(bc);

        if (changes == null) throw new NullPointerException(
            "BeanContextMembershipEvent:  changes 为 null。");

        children = Arrays.asList(changes);
    }

    /**
     * 获取受通知影响的子项数量。
     * @return 受通知影响的子项数量
     */
    public int size() { return children.size(); }

    /**
     * 指定的子项是否受此事件影响？
     * @return 如果受影响则为 <code>true</code>，否则为 <code>false</code>
     * @param child 要检查是否受影响的对象
     */
    public boolean contains(Object child) {
        return children.contains(child);
    }

    /**
     * 获取受此事件影响的子项数组。
     * @return 受影响的子项数组
     */
    public Object[] toArray() { return children.toArray(); }

    /**
     * 获取受此事件影响的子项迭代器。
     * @return 受影响的子项迭代器
     */
    @SuppressWarnings("rawtypes")
    public Iterator iterator() { return children.iterator(); }

    /*
     * 字段
     */

   /**
    * 受此事件通知影响的子项列表。
    */
    @SuppressWarnings("rawtypes")
    protected Collection children;
}
