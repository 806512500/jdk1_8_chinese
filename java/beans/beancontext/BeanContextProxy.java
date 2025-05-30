/*
 * Copyright (c) 1998, 2002, Oracle and/or its affiliates. All rights reserved.
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

/**
 * <p>
 * 该接口由一个 JavaBean 实现，该 JavaBean 没有直接关联的 BeanContext(Child)（通过实现该接口或其子接口），
 * 但有一个从其委托的公共 BeanContext(Child)。
 * 例如，java.awt.Container 的子类可能有一个与之关联的 BeanContext，该 BeanContext 包含该容器的所有 Component 子组件。
 * </p>
 * <p>
 * 一个对象不能同时实现此接口和 BeanContextChild 接口
 * （或其任何子接口），它们是互斥的。
 * </p>
 * <p>
 * 该接口的调用者应检查返回类型，以便获取特定的 BeanContextChild 子接口，如下所示：
 * <code>
 * BeanContextChild bcc = o.getBeanContextProxy();
 *
 * if (bcc instanceof BeanContext) {
 *      // ...
 * }
 * </code>
 * 或
 * <code>
 * BeanContextChild bcc = o.getBeanContextProxy();
 * BeanContext      bc  = null;
 *
 * try {
 *     bc = (BeanContext)bcc;
 * } catch (ClassCastException cce) {
 *     // 类型转换失败，bcc 不是 BeanContext 的实例
 * }
 * </code>
 * </p>
 * <p>
 * 返回值在实现实例的生命周期内是常量
 * </p>
 * @author Laurence P. G. Cable
 * @since 1.2
 *
 * @see java.beans.beancontext.BeanContextChild
 * @see java.beans.beancontext.BeanContextChildSupport
 */

public interface BeanContextProxy {

    /**
     * 获取与此对象关联的 <code>BeanContextChild</code>（或子接口）。
     * @return 与此对象关联的 <code>BeanContextChild</code>（或子接口）
     */
    BeanContextChild getBeanContextProxy();
}
