/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.Serializable;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

/**
 * 为 {@link Comparator} 提供支持的包私有类。
 */
class Comparators {
    private Comparators() {
        throw new AssertionError("no instances");
    }

    /**
     * 按自然顺序比较 {@link Comparable} 对象。
     *
     * @see Comparable
     */
    enum NaturalOrderComparator implements Comparator<Comparable<Object>> {
        INSTANCE;

        @Override
        public int compare(Comparable<Object> c1, Comparable<Object> c2) {
            return c1.compareTo(c2);
        }

        @Override
        public Comparator<Comparable<Object>> reversed() {
            return Comparator.reverseOrder();
        }
    }

    /**
     * 支持 null 的比较器
     */
    final static class NullComparator<T> implements Comparator<T>, Serializable {
        private static final long serialVersionUID = -7569533591570686392L;
        private final boolean nullFirst;
        // 如果为 null，则认为非 null 的 T 是相等的
        private final Comparator<T> real;

        @SuppressWarnings("unchecked")
        NullComparator(boolean nullFirst, Comparator<? super T> real) {
            this.nullFirst = nullFirst;
            this.real = (Comparator<T>) real;
        }

        @Override
        public int compare(T a, T b) {
            if (a == null) {
                return (b == null) ? 0 : (nullFirst ? -1 : 1);
            } else if (b == null) {
                return nullFirst ? 1 : -1;
            } else {
                return (real == null) ? 0 : real.compare(a, b);
            }
        }

        @Override
        public Comparator<T> thenComparing(Comparator<? super T> other) {
            Objects.requireNonNull(other);
            return new NullComparator<>(nullFirst, real == null ? other : real.thenComparing(other));
        }

        @Override
        public Comparator<T> reversed() {
            return new NullComparator<>(!nullFirst, real == null ? null : real.reversed());
        }
    }
}
