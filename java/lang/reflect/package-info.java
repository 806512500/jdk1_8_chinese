/*
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 提供类和对象的反射信息的类和接口。反射允许程序访问已加载类的字段、方法和构造函数的信息，
 * 并在安全限制内使用反射的字段、方法和构造函数操作其底层对应物。
 *
 * <p>{@code AccessibleObject} 允许在有必要的 {@code ReflectPermission} 时抑制访问检查。
 *
 * <p>{@code Array} 提供静态方法以动态创建和访问数组。
 *
 * <p>此包中的类，连同 {@code java.lang.Class}，支持需要访问目标对象的公共成员（基于其运行时类）
 * 或给定类声明的成员的应用程序，例如调试器、解释器、对象检查器、类浏览器以及需要访问目标对象的公共成员
 * 或给定类声明的成员的服务，如对象序列化和 JavaBeans。
 *
 * @since JDK1.1
 */
package java.lang.reflect;
