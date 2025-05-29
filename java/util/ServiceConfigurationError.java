/*
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
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
 * 在加载服务提供者时出错时抛出的错误。
 *
 * <p> 以下情况下将抛出此错误：
 *
 * <ul>
 *
 *   <li> 提供者配置文件的格式违反了<a
 *   href="ServiceLoader.html#format">规范</a>； </li>
 *
 *   <li> 读取提供者配置文件时发生 {@link java.io.IOException IOException}； </li>
 *
 *   <li> 提供者配置文件中命名的具体提供者类找不到； </li>
 *
 *   <li> 具体提供者类不是服务类的子类； </li>
 *
 *   <li> 无法实例化具体提供者类；或
 *
 *   <li> 发生其他类型的错误。 </li>
 *
 * </ul>
 *
 *
 * @author Mark Reinhold
 * @since 1.6
 */

public class ServiceConfigurationError
    extends Error
{

    private static final long serialVersionUID = 74132770414881L;

    /**
     * 使用指定的消息构造新实例。
     *
     * @param  msg  消息，或 <tt>null</tt> 如果没有消息
     *
     */
    public ServiceConfigurationError(String msg) {
        super(msg);
    }

    /**
     * 使用指定的消息和原因构造新实例。
     *
     * @param  msg  消息，或 <tt>null</tt> 如果没有消息
     *
     * @param  cause  原因，或 <tt>null</tt> 如果原因不存在或未知
     */
    public ServiceConfigurationError(String msg, Throwable cause) {
        super(msg, cause);
    }

}
