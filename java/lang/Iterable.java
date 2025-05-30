/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * 实现此接口允许对象成为“for-each 循环”语句的目标。参见
 * <strong>
 * <a href="{@docRoot}/../technotes/guides/language/foreach.html">For-each 循环</a>
 * </strong>
 *
 * @param <T> 迭代器返回的元素类型
 *
 * @since 1.5
 * @jls 14.14.2 增强的 for 语句
 */
public interface Iterable<T> {
    /**
     * 返回一个迭代器，用于遍历类型为 {@code T} 的元素。
     *
     * @return 一个迭代器。
     */
    Iterator<T> iterator();

    /**
     * 对 {@code Iterable} 中的每个元素执行给定的操作，直到所有元素都被处理或操作抛出异常。除非实现类另有规定，否则操作按迭代顺序执行（如果指定了迭代顺序）。操作抛出的异常将传递给调用者。
     *
     * @implSpec
     * <p>默认实现的行为如下：
     * <pre>{@code
     *     for (T t : this)
     *         action.accept(t);
     * }</pre>
     *
     * @param action 要为每个元素执行的操作
     * @throws NullPointerException 如果指定的操作为 null
     * @since 1.8
     */
    default void forEach(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        for (T t : this) {
            action.accept(t);
        }
    }

    /**
     * 创建一个 {@link Spliterator}，用于遍历此 {@code Iterable} 描述的元素。
     *
     * @implSpec
     * 默认实现从可迭代对象的 {@code Iterator} 创建一个
     * <em><a href="Spliterator.html#binding">早期绑定</a></em>
     * 的 spliterator。该 spliterator 继承了可迭代对象的迭代器的 <em>快速失败</em> 属性。
     *
     * @implNote
     * 默认实现通常应被重写。默认实现返回的 spliterator 分割能力较差，没有大小，也不报告任何 spliterator 特性。实现类几乎总是可以提供更好的实现。
     *
     * @return 一个 {@code Spliterator}，用于遍历此 {@code Iterable} 描述的元素。
     * @since 1.8
     */
    default Spliterator<T> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(), 0);
    }
}
