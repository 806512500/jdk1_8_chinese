/*
 * Copyright (c) 1995, 2004, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

/**
 * 一个类实现 <code>Cloneable</code> 接口以指示 {@link java.lang.Object#clone()} 方法可以对该类的实例进行
 * 字段到字段的复制。
 * <p>
 * 对于不实现 <code>Cloneable</code> 接口的实例，调用 Object 的 clone 方法将导致抛出
 * <code>CloneNotSupportedException</code> 异常。
 * <p>
 * 按照惯例，实现此接口的类应覆盖 <tt>Object.clone</tt>（该方法是受保护的）并提供一个公共方法。
 * 有关覆盖此方法的详细信息，请参阅 {@link java.lang.Object#clone()}。
 * <p>
 * 请注意，此接口不包含 <tt>clone</tt> 方法。因此，仅凭实现此接口并不能保证可以克隆一个对象。即使反射调用
 * clone 方法，也不能保证其成功。
 *
 * @author  未署名
 * @see     java.lang.CloneNotSupportedException
 * @see     java.lang.Object#clone()
 * @since   JDK1.0
 */
public interface Cloneable {
}
