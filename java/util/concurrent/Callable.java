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

package java.util.concurrent;

/**
 * 一个返回结果并可能抛出异常的任务。
 * 实现者定义一个无参数的方法，称为 {@code call}。
 *
 * <p>{@code Callable} 接口类似于 {@link
 * java.lang.Runnable}，因为两者都设计用于其实例可能由另一个线程执行的类。但是，
 * {@code Runnable} 不返回结果，也不能抛出检查异常。
 *
 * <p>{@link Executors} 类包含将其他常见形式转换为 {@code Callable} 类的实用方法。
 *
 * @see Executor
 * @since 1.5
 * @author Doug Lea
 * @param <V> 方法 {@code call} 的结果类型
 */
@FunctionalInterface
public interface Callable<V> {
    /**
     * 计算结果，如果无法计算则抛出异常。
     *
     * @return 计算结果
     * @throws Exception 如果无法计算结果
     */
    V call() throws Exception;
}
