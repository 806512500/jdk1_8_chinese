/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * 一个 <tt>CharSequence</tt> 是一个可读的 <code>char</code> 值序列。此接口提供了对许多不同类型的
 * <code>char</code> 序列的统一读取访问。一个 <code>char</code> 值表示一个在 <i>基本多文种平面 (BMP)</i>
 * 或代理中的字符。详细信息请参见 <a href="Character.html#unicode">Unicode 字符表示</a>。
 *
 * <p> 此接口没有细化 {@link java.lang.Object#equals(java.lang.Object) equals} 和 {@link
 * java.lang.Object#hashCode() hashCode} 方法的一般约定。因此，比较两个实现 <tt>CharSequence</tt> 的对象
 * 的结果通常是不确定的。每个对象可能由不同的类实现，没有保证每个类都能测试其实例与另一个类的实例是否相等。
 * 因此，使用任意 <tt>CharSequence</tt> 实例作为集合中的元素或映射中的键是不合适的。 </p>
 *
 * @author Mike McCloskey
 * @since 1.4
 * @spec JSR-51
 */

public interface CharSequence {

    /**
     * 返回此字符序列的长度。长度是序列中的 16 位 <code>char</code> 的数量。
     *
     * @return  此序列中的 <code>char</code> 数量
     */
    int length();

    /**
     * 返回指定索引处的 <code>char</code> 值。索引范围从零到 <tt>length() - 1</tt>。序列中的第一个 <code>char</code>
     * 值在索引零处，下一个在索引一处，依此类推，就像数组索引一样。
     *
     * <p>如果索引指定的 <code>char</code> 值是一个 <a href="{@docRoot}/java/lang/Character.html#unicode">代理</a>，
     * 则返回代理值。</p>
     *
     * @param   index   要返回的 <code>char</code> 值的索引
     *
     * @return  指定的 <code>char</code> 值
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>index</tt> 参数为负或不小于 <tt>length()</tt>
     */
    char charAt(int index);

    /**
     * 返回此序列的一个子序列。子序列从指定索引处的 <code>char</code> 值开始，到索引 <tt>end - 1</tt> 处的
     * <code>char</code> 值结束。返回的序列的长度（以 <code>char</code> 计）为 <tt>end - start</tt>，
     * 因此如果 <tt>start == end</tt>，则返回一个空序列。
     *
     * @param   start   起始索引，包含
     * @param   end     结束索引，不包含
     *
     * @return  指定的子序列
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>start</tt> 或 <tt>end</tt> 为负，<tt>end</tt> 大于 <tt>length()</tt>，
     *          或 <tt>start</tt> 大于 <tt>end</tt>
     */
    CharSequence subSequence(int start, int end);

    /**
     * 返回一个包含此序列中字符的字符串，顺序与此序列相同。字符串的长度将与此序列的长度相同。
     *
     * @return  一个由恰好此序列的字符组成的字符串
     */
    public String toString();

    /**
     * 返回一个从此序列扩展 <code>char</code> 值的 {@code int} 流。任何映射到 <a
     * href="{@docRoot}/java/lang/Character.html#unicode">代理代码点</a> 的 <code>char</code> 都会无解释地传递给流。
     *
     * <p>如果在读取流时修改了序列，结果是不确定的。</p>
     *
     * @return 一个包含此序列的 <code>char</code> 值的 IntStream
     * @since 1.8
     */
    public default IntStream chars() {
        class CharIterator implements PrimitiveIterator.OfInt {
            int cur = 0;

            public boolean hasNext() {
                return cur < length();
            }

            public int nextInt() {
                if (hasNext()) {
                    return charAt(cur++);
                } else {
                    throw new NoSuchElementException();
                }
            }

            @Override
            public void forEachRemaining(IntConsumer block) {
                for (; cur < length(); cur++) {
                    block.accept(charAt(cur));
                }
            }
        }

        return StreamSupport.intStream(() ->
                Spliterators.spliterator(
                        new CharIterator(),
                        length(),
                        Spliterator.ORDERED),
                Spliterator.SUBSIZED | Spliterator.SIZED | Spliterator.ORDERED,
                false);
    }

    /**
     * 返回一个从此序列的代码点值流。遇到的任何代理对都会像 {@linkplain
     * Character#toCodePoint Character.toCodePoint} 一样组合，并将结果传递给流。任何其他代码单元，包括普通的 BMP 字符、
     * 未配对的代理和未定义的代码单元，都会零扩展为 {@code int} 值，然后传递给流。
     *
     * <p>如果在读取流时修改了序列，结果是不确定的。</p>
     *
     * @return 一个包含此序列的 Unicode 代码点的 IntStream
     * @since 1.8
     */
    public default IntStream codePoints() {
        class CodePointIterator implements PrimitiveIterator.OfInt {
            int cur = 0;

            @Override
            public void forEachRemaining(IntConsumer block) {
                final int length = length();
                int i = cur;
                try {
                    while (i < length) {
                        char c1 = charAt(i++);
                        if (!Character.isHighSurrogate(c1) || i >= length) {
                            block.accept(c1);
                        } else {
                            char c2 = charAt(i);
                            if (Character.isLowSurrogate(c2)) {
                                i++;
                                block.accept(Character.toCodePoint(c1, c2));
                            } else {
                                block.accept(c1);
                            }
                        }
                    }
                } finally {
                    cur = i;
                }
            }

            public boolean hasNext() {
                return cur < length();
            }

            public int nextInt() {
                final int length = length();

                if (cur >= length) {
                    throw new NoSuchElementException();
                }
                char c1 = charAt(cur++);
                if (Character.isHighSurrogate(c1) && cur < length) {
                    char c2 = charAt(cur);
                    if (Character.isLowSurrogate(c2)) {
                        cur++;
                        return Character.toCodePoint(c1, c2);
                    }
                }
                return c1;
            }
        }

        return StreamSupport.intStream(() ->
                Spliterators.spliteratorUnknownSize(
                        new CodePointIterator(),
                        Spliterator.ORDERED),
                Spliterator.ORDERED,
                false);
    }
}
