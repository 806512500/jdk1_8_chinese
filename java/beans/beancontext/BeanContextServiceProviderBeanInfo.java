/*
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
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

import java.beans.BeanInfo;

/**
 * 如果 BeanContextServiceProvider 希望提供有关其 bean 可能提供的服务的显式信息，
 * 则应实现一个实现此 BeanInfo 子接口的 BeanInfo 类，并提供有关其服务的方法、属性、事件等的显式信息。
 */

public interface BeanContextServiceProviderBeanInfo extends BeanInfo {

    /**
     * 获取一个 <code>BeanInfo</code> 数组，每个数组元素对应此 ServiceProvider 静态提供的一个服务类或接口。
     * @return <code>BeanInfo</code> 数组
     */
    BeanInfo[] getServicesBeanInfo();
}
