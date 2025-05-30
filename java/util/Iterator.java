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

package java.util;

import java.util.function.Consumer;

/**
 * 一个用于遍历集合的迭代器。{@code Iterator} 在 Java 集合框架中取代了 {@link Enumeration}。迭代器与枚举相比有两点不同：
 *
 * <ul>
 *      <li> 迭代器允许调用者在迭代过程中从底层集合中删除元素，并且具有明确定义的语义。
 *      <li> 方法名得到了改进。
 * </ul>
 *
 * <p>此接口是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a> 的成员。
 *
 * @param <E> 此迭代器返回的元素类型
 *
 * @author  Josh Bloch
 * @see Collection
 * @see ListIterator
 * @see Iterable
 * @since 1.2
 */
public interface Iterator<E> {
    /**
     * 如果迭代还有更多元素，则返回 {@code true}。
     * （换句话说，如果 {@link #next} 会返回一个元素而不是抛出异常，则返回 {@code true}。）
     *
     * @return 如果迭代还有更多元素，则返回 {@code true}
     */
    boolean hasNext();

    /**
     * 返回迭代中的下一个元素。
     *
     * @return 迭代中的下一个元素
     * @throws NoSuchElementException 如果迭代没有更多元素
     */
    E next();

    /**
     * 从此迭代器返回的最后一个元素中删除底层集合中的元素（可选操作）。此方法只能在每次调用 {@link #next} 后调用一次。如果在迭代过程中以任何其他方式修改了底层集合，迭代器的行为是未指定的。
     *
     * @implSpec
     * 默认实现抛出一个 {@link UnsupportedOperationException} 实例，并且不执行其他操作。
     *
     * @throws UnsupportedOperationException 如果此迭代器不支持 {@code remove} 操作
     *
     * @throws IllegalStateException 如果尚未调用 {@code next} 方法，或者在上次调用 {@code next} 方法后已经调用了 {@code remove} 方法
     */
    default void remove() {
        throw new UnsupportedOperationException("remove");
    }

    /**
     * 对每个剩余元素执行给定的操作，直到所有元素都被处理或操作抛出异常。如果指定了迭代顺序，则按该顺序执行操作。操作中抛出的异常将传递给调用者。
     *
     * @implSpec
     * <p>默认实现的行为类似于：
     * <pre>{@code
     *     while (hasNext())
     *         action.accept(next());
     * }</pre>
     *
     * @param action 要为每个元素执行的操作
     * @throws NullPointerException 如果指定的操作为 null
     * @since 1.8
     */
    default void forEachRemaining(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        while (hasNext())
            action.accept(next());
    }
}
