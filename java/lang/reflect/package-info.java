/*
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 提供类和对象的反射信息的类和接口。反射允许程序访问已加载类的字段、方法和构造函数的信息，
 * 并在安全限制内使用反射字段、方法和构造函数来操作其底层对应物。
 *
 * <p>{@code AccessibleObject} 如果有必要的 {@code ReflectPermission}，则允许抑制访问检查。
 *
 * <p>{@code Array} 提供静态方法来动态创建和访问数组。
 *
 * <p>此包中的类，连同 {@code java.lang.Class}，支持诸如调试器、解释器、对象检查器、类浏览器等应用程序，
 * 以及需要访问目标对象（基于其运行时类）的公共成员或给定类声明的成员的服务，如对象序列化和 JavaBeans。
 *
 * @since JDK1.1
 */
package java.lang.reflect;
