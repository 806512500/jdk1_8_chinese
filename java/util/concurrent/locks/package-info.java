/*
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

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

/**
 * 接口和类提供了一个框架，用于锁定和等待条件，这些条件与内置同步和监视器不同。该框架允许在锁和条件的使用上具有更大的灵活性，但代价是语法更加笨拙。
 *
 * <p>{@link java.util.concurrent.locks.Lock} 接口支持不同的锁定规则（可重入、公平等），并且可以在非块结构化的上下文中使用，包括手递手和锁重排序算法。主要实现是 {@link java.util.concurrent.locks.ReentrantLock}。
 *
 * <p>{@link java.util.concurrent.locks.ReadWriteLock} 接口同样定义了可以由多个读取者共享但对写入者独占的锁。仅提供了一个实现，即 {@link
 * java.util.concurrent.locks.ReentrantReadWriteLock}，因为它涵盖了大多数标准使用场景。但是，程序员可以创建自己的实现以满足非标准需求。
 *
 * <p>{@link java.util.concurrent.locks.Condition} 接口描述了可以与锁关联的条件变量。这些变量在使用上类似于通过 {@code Object.wait} 访问的隐式监视器，但提供了扩展功能。
 * 特别是，多个 {@code Condition} 对象可以与单个 {@code Lock} 关联。为了避免兼容性问题，{@code Condition} 方法的名称与相应的 {@code Object} 版本不同。
 *
 * <p>{@link java.util.concurrent.locks.AbstractQueuedSynchronizer} 类作为定义依赖于排队阻塞线程的锁和其他同步器的有用超类。
 * {@link java.util.concurrent.locks.AbstractQueuedLongSynchronizer} 类提供了相同的功能，但支持 64 位的同步状态。两者都扩展了类 {@link
 * java.util.concurrent.locks.AbstractOwnableSynchronizer}，这是一个简单的类，有助于记录当前独占同步的线程。
 * {@link java.util.concurrent.locks.LockSupport} 类提供了低级别的阻塞和解除阻塞支持，这对于实现自定义锁类的开发人员非常有用。
 *
 * @since 1.5
 */
package java.util.concurrent.locks;
