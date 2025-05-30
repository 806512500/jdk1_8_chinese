/*
 * Copyright (c) 1999, 2006, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.reflect;

/**
 * {@code InvocationHandler} 是由代理实例的 <i>调用处理器</i> 实现的接口。
 *
 * <p>每个代理实例都有一个关联的调用处理器。
 * 当在代理实例上调用方法时，方法调用会被编码并分派给其调用处理器的 {@code invoke}
 * 方法。
 *
 * @author      Peter Jones
 * @see         Proxy
 * @since       1.3
 */
public interface InvocationHandler {

    /**
     * 处理在代理实例上的方法调用并返回结果。当在与调用处理器关联的代理实例上调用方法时，
     * 将调用此方法。
     *
     * @param   proxy 被调用方法的代理实例。
     *
     * @param   method 与在代理实例上调用的接口方法相对应的 {@code Method} 实例。
     * 该 {@code Method} 对象的声明类将是该方法声明所在的接口，这可能是代理接口继承该方法的超接口。
     *
     * @param   args 包含在代理实例上调用方法时传递的参数值的对象数组，如果接口方法没有参数，则为 {@code null}。
     * 基本类型的参数将被包装在适当的原始包装类的实例中，例如
     * {@code java.lang.Integer} 或 {@code java.lang.Boolean}。
     *
     * @return  从代理实例上的方法调用返回的值。如果接口方法的声明返回类型是基本类型，
     * 则此方法返回的值必须是相应基本类型的包装类的实例；否则，它必须是可以分配给声明返回类型的类型。
     * 如果此方法返回的值为 {@code null} 且接口方法的返回类型是基本类型，
     * 则代理实例上的方法调用将抛出 {@code NullPointerException}。如果此方法返回的值
     * 与上述描述的接口方法的声明返回类型不兼容，则代理实例上的方法调用将抛出 {@code ClassCastException}。
     *
     * @throws  Throwable 从代理实例上的方法调用抛出的异常。异常的类型必须是可以分配给接口方法的
     * {@code throws} 子句中声明的任何异常类型，或者可以分配给未检查的异常类型
     * {@code java.lang.RuntimeException} 或 {@code java.lang.Error}。如果此方法抛出的检查异常
     * 不能分配给接口方法的 {@code throws} 子句中声明的任何异常类型，则代理实例上的方法调用将抛出
     * 包含此方法抛出的异常的 {@link UndeclaredThrowableException}。
     *
     * @see     UndeclaredThrowableException
     */
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable;
}
