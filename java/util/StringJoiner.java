
/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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
 * {@code StringJoiner} 用于构建由分隔符分隔的字符序列，并且可以选择以提供的前缀开始并以提供的后缀结束。
 * <p>
 * 在向 {@code StringJoiner} 添加任何内容之前，其 {@code sj.toString()} 方法将默认返回 {@code prefix + suffix}。
 * 但是，如果调用了 {@code setEmptyValue} 方法，则将返回提供的 {@code emptyValue}。例如，当使用集合表示法创建表示空集的字符串时，
 * 即 <code>"{}"</code>，其中 {@code prefix} 是 <code>"{"</code>，{@code suffix} 是 <code>"}"</code>，而 {@code StringJoiner} 中尚未添加任何内容。
 *
 * @apiNote
 * <p>字符串 {@code "[George:Sally:Fred]"} 可以如下构建：
 *
 * <pre> {@code
 * StringJoiner sj = new StringJoiner(":", "[", "]");
 * sj.add("George").add("Sally").add("Fred");
 * String desiredString = sj.toString();
 * }</pre>
 * <p>
 * {@code StringJoiner} 可以用于从 {@link java.util.stream.Stream} 创建格式化的输出，使用
 * {@link java.util.stream.Collectors#joining(CharSequence)}。例如：
 *
 * <pre> {@code
 * List<Integer> numbers = Arrays.asList(1, 2, 3, 4);
 * String commaSeparatedNumbers = numbers.stream()
 *     .map(i -> i.toString())
 *     .collect(Collectors.joining(", "));
 * }</pre>
 *
 * @see java.util.stream.Collectors#joining(CharSequence)
 * @see java.util.stream.Collectors#joining(CharSequence, CharSequence, CharSequence)
 * @since  1.8
*/
public final class StringJoiner {
    private final String prefix;
    private final String delimiter;
    private final String suffix;

    /*
     * StringBuilder value -- 任何时候，由前缀、添加的元素和分隔符构建的字符，但不包括后缀，这样可以更轻松地添加元素而无需每次调整后缀。
     */
    private StringBuilder value;

    /*
     * 默认情况下，当尚未添加任何元素时，即为空时，由 {@code toString()} 方法返回的字符串由 {@code prefix + suffix} 组成，
     * 或者是 {@code value} 的属性。用户可以覆盖此值，使其为其他值，包括空字符串。
     */
    private String emptyValue;

    /**
     * 构造一个没有字符的 {@code StringJoiner}，没有 {@code prefix} 或 {@code suffix}，并且复制提供的 {@code delimiter}。
     * 如果没有向 {@code StringJoiner} 添加任何字符并且调用了访问其值的方法，除非首先调用了 {@code setEmptyValue}，
     * 否则结果中不会返回 {@code prefix} 或 {@code suffix}（或其属性）。
     *
     * @param  delimiter 用于分隔添加到 {@code StringJoiner} 值的每个元素的字符序列
     * @throws NullPointerException 如果 {@code delimiter} 为 {@code null}
     */
    public StringJoiner(CharSequence delimiter) {
        this(delimiter, "", "");
    }

    /**
     * 构造一个没有字符的 {@code StringJoiner}，使用提供的 {@code prefix}、{@code delimiter} 和 {@code suffix} 的副本。
     * 如果没有向 {@code StringJoiner} 添加任何字符并且调用了访问其字符串值的方法，除非首先调用了 {@code setEmptyValue}，
     * 否则结果中将返回 {@code prefix + suffix}（或其属性）。
     *
     * @param  delimiter 用于分隔添加到 {@code StringJoiner} 的每个元素的字符序列
     * @param  prefix 用于开始的字符序列
     * @param  suffix 用于结束的字符序列
     * @throws NullPointerException 如果 {@code prefix}、{@code delimiter} 或 {@code suffix} 为 {@code null}
     */
    public StringJoiner(CharSequence delimiter,
                        CharSequence prefix,
                        CharSequence suffix) {
        Objects.requireNonNull(prefix, "前缀不能为空");
        Objects.requireNonNull(delimiter, "分隔符不能为空");
        Objects.requireNonNull(suffix, "后缀不能为空");
        // 防御性地复制参数
        this.prefix = prefix.toString();
        this.delimiter = delimiter.toString();
        this.suffix = suffix.toString();
        this.emptyValue = this.prefix + this.suffix;
    }

    /**
     * 设置在确定此 {@code StringJoiner} 的字符串表示形式且尚未添加任何元素时使用的字符序列，即为空时。
     * 为此目的复制 {@code emptyValue} 参数。请注意，一旦调用了添加方法，即使添加的元素对应于空字符串，
     * {@code StringJoiner} 也不再被视为为空。
     *
     * @param  emptyValue 作为空 {@code StringJoiner} 的值返回的字符
     * @return 本身，以便可以链式调用
     * @throws NullPointerException 当 {@code emptyValue} 参数为 {@code null} 时
     */
    public StringJoiner setEmptyValue(CharSequence emptyValue) {
        this.emptyValue = Objects.requireNonNull(emptyValue,
            "空值不能为空").toString();
        return this;
    }


                /**
     * 返回当前值，包括 {@code prefix}、迄今为止添加的值（由 {@code delimiter} 分隔）和 {@code suffix}，
     * 除非没有添加任何元素，在这种情况下，返回 {@code prefix + suffix} 或 {@code emptyValue} 字符。
     *
     * @return 此 {@code StringJoiner} 的字符串表示形式
     */
    @Override
    public String toString() {
        if (value == null) {
            return emptyValue;
        } else {
            if (suffix.equals("")) {
                return value.toString();
            } else {
                int initialLength = value.length();
                String result = value.append(suffix).toString();
                // 将 value 重置为追加前的初始长度
                value.setLength(initialLength);
                return result;
            }
        }
    }

    /**
     * 添加给定的 {@code CharSequence} 值的副本作为 {@code StringJoiner} 值的下一个元素。如果 {@code newElement} 为
     * {@code null}，则添加 {@code "null"}。
     *
     * @param  newElement 要添加的元素
     * @return 对此 {@code StringJoiner} 的引用
     */
    public StringJoiner add(CharSequence newElement) {
        prepareBuilder().append(newElement);
        return this;
    }

    /**
     * 如果给定的 {@code StringJoiner} 非空，则添加其内容（不包括前缀和后缀）作为下一个元素。如果给定的 {@code
     * StringJoiner} 为空，则调用无效。
     *
     * <p>{@code StringJoiner} 为空是指 {@link #add(CharSequence) add()}
     * 从未被调用过，且 {@code merge()} 从未被调用过带有非空 {@code StringJoiner} 参数的情况。
     *
     * <p>如果其他 {@code StringJoiner} 使用不同的分隔符，则来自其他 {@code StringJoiner} 的元素将使用该分隔符连接，
     * 并将结果追加到此 {@code StringJoiner} 作为一个单独的元素。
     *
     * @param other 要合并到此中的 {@code StringJoiner}
     * @throws NullPointerException 如果其他 {@code StringJoiner} 为 null
     * @return 此 {@code StringJoiner}
     */
    public StringJoiner merge(StringJoiner other) {
        Objects.requireNonNull(other);
        if (other.value != null) {
            final int length = other.value.length();
            // 锁定长度，以便在开始复制之前捕获要追加的数据，特别是当合并 'this' 时
            StringBuilder builder = prepareBuilder();
            builder.append(other.value, other.prefix.length(), length);
        }
        return this;
    }

    private StringBuilder prepareBuilder() {
        if (value != null) {
            value.append(delimiter);
        } else {
            value = new StringBuilder().append(prefix);
        }
        return value;
    }

    /**
     * 返回此 {@code StringJoiner} 的 {@code String} 表示形式的长度。注意，如果
     * 从未调用过任何 add 方法，则返回 {@code String} 表示形式的长度（即 {@code prefix + suffix} 或 {@code emptyValue}）。
     * 该值应等同于 {@code toString().length()}。
     *
     * @return 当前 {@code StringJoiner} 值的长度
     */
    public int length() {
        // 请记住，除非我们返回完整的（当前）值或其某个子字符串或长度，否则我们实际上从未追加后缀，
        // 这样我们可以在需要时添加更多内容。
        return (value != null ? value.length() + suffix.length() :
                emptyValue.length());
    }
}
