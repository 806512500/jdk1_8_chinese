
/*
 * Copyright (c) 1994, 2021, Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Arrays;

/**
 * 一个线程安全的、可变的字符序列。
 * 字符缓冲区类似于 {@link String}，但可以被修改。在任何时候，它包含某些特定的字符序列，但该序列的长度和内容可以通过某些方法调用进行更改。
 * <p>
 * 字符缓冲区可以安全地被多个线程使用。方法在必要时进行同步，以便所有针对特定实例的操作都像按某种顺序发生，该顺序与每个涉及的线程调用的方法顺序一致。
 * <p>
 * {@code StringBuffer} 的主要操作是 {@code append} 和 {@code insert} 方法，这些方法被重载以接受任何类型的数据。每个方法都会将给定的数据有效转换为字符串，然后将该字符串的字符追加或插入到字符缓冲区。{@code append} 方法始终将这些字符添加到缓冲区的末尾；而 {@code insert} 方法则将字符添加到指定的位置。
 * <p>
 * 例如，如果 {@code z} 引用一个当前内容为 {@code "start"} 的字符缓冲区对象，那么方法调用 {@code z.append("le")} 会使字符缓冲区包含 {@code "startle"}，而 {@code z.insert(4, "le")} 会使字符缓冲区包含 {@code "starlet"}。
 * <p>
 * 通常，如果 sb 引用一个 {@code StringBuffer} 实例，那么 {@code sb.append(x)} 与 {@code sb.insert(sb.length(), x)} 具有相同的效果。
 * <p>
 * 涉及源序列（如从源序列追加或插入）的任何操作，此类仅同步执行操作的字符缓冲区，而不同步源。请注意，虽然 {@code StringBuffer} 被设计为可以安全地由多个线程并发使用，但如果构造函数或 {@code append} 或 {@code insert} 操作传递了一个跨线程共享的源序列，调用代码必须确保在操作期间源序列具有一致且不变的视图。
 * 这可以通过调用者在操作调用期间持有锁、使用不可变的源序列或不跨线程共享源序列来满足。
 * <p>
 * 每个字符缓冲区都有一个容量。只要包含在字符缓冲区中的字符序列的长度不超过容量，就不需要分配新的内部缓冲数组。如果内部缓冲区溢出，它会自动变大。
 * <p>
 * 除非另有说明，否则将 {@code null} 参数传递给此类的构造函数或方法将导致抛出 {@link NullPointerException}。
 * <p>
 * 从 JDK 5 开始，此类已通过一个等效类 {@link StringBuilder} 进行补充，该类设计用于单线程使用。通常应优先使用 {@code StringBuilder}，因为它支持所有相同的操作，但速度更快，因为它不执行同步。
 *
 * @author      Arthur van Hoff
 * @see     java.lang.StringBuilder
 * @see     java.lang.String
 * @since   JDK1.0
 */
public final class StringBuffer
    extends AbstractStringBuilder
    implements Serializable, CharSequence
{

    /**
     * 由 toString 返回的最后一个值的缓存。每当 StringBuffer 被修改时，此缓存将被清除。
     */
    private transient char[] toStringCache;

    /** 使用 JDK 1.0.2 的 serialVersionUID 以确保互操作性 */
    static final long serialVersionUID = 3388685877147921107L;

    /**
     * 构造一个不包含任何字符且初始容量为 16 个字符的字符缓冲区。
     */
    public StringBuffer() {
        super(16);
    }

    /**
     * 构造一个不包含任何字符且具有指定初始容量的字符缓冲区。
     *
     * @param      capacity  初始容量。
     * @exception  NegativeArraySizeException  如果 {@code capacity} 参数小于 {@code 0}。
     */
    public StringBuffer(int capacity) {
        super(capacity);
    }

    /**
     * 构造一个初始化为指定字符串内容的字符缓冲区。字符缓冲区的初始容量为 {@code 16} 加上字符串参数的长度。
     *
     * @param   str   缓冲区的初始内容。
     */
    public StringBuffer(String str) {
        super(str.length() + 16);
        append(str);
    }

    /**
     * 构造一个包含与指定 {@code CharSequence} 相同字符的字符缓冲区。字符缓冲区的初始容量为 {@code 16} 加上 {@code CharSequence} 参数的长度。
     * <p>
     * 如果指定的 {@code CharSequence} 的长度小于或等于零，则返回一个容量为 {@code 16} 的空缓冲区。
     *
     * @param      seq   要复制的序列。
     * @since 1.5
     */
    public StringBuffer(CharSequence seq) {
        this(seq.length() + 16);
        append(seq);
    }

    @Override
    public synchronized int length() {
        return count;
    }

    @Override
    public synchronized int capacity() {
        return value.length;
    }


    @Override
    public synchronized void ensureCapacity(int minimumCapacity) {
        super.ensureCapacity(minimumCapacity);
    }

    /**
     * @since      1.5
     */
    @Override
    public synchronized void trimToSize() {
        super.trimToSize();
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @see        #length()
     */
    @Override
    public synchronized void setLength(int newLength) {
        toStringCache = null;
        super.setLength(newLength);
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @see        #length()
     */
    @Override
    public synchronized char charAt(int index) {
        if ((index < 0) || (index >= count))
            throw new StringIndexOutOfBoundsException(index);
        return value[index];
    }

    /**
     * @since      1.5
     */
    @Override
    public synchronized int codePointAt(int index) {
        return super.codePointAt(index);
    }

    /**
     * @since     1.5
     */
    @Override
    public synchronized int codePointBefore(int index) {
        return super.codePointBefore(index);
    }

    /**
     * @since     1.5
     */
    @Override
    public synchronized int codePointCount(int beginIndex, int endIndex) {
        return super.codePointCount(beginIndex, endIndex);
    }

    /**
     * @since     1.5
     */
    @Override
    public synchronized int offsetByCodePoints(int index, int codePointOffset) {
        return super.offsetByCodePoints(index, codePointOffset);
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public synchronized void getChars(int srcBegin, int srcEnd, char[] dst,
                                      int dstBegin)
    {
        super.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @see        #length()
     */
    @Override
    public synchronized void setCharAt(int index, char ch) {
        if ((index < 0) || (index >= count))
            throw new StringIndexOutOfBoundsException(index);
        toStringCache = null;
        value[index] = ch;
    }

    @Override
    public synchronized StringBuffer append(Object obj) {
        toStringCache = null;
        super.append(String.valueOf(obj));
        return this;
    }

    @Override
    public synchronized StringBuffer append(String str) {
        toStringCache = null;
        super.append(str);
        return this;
    }

    /**
     * 将指定的 {@code StringBuffer} 追加到此序列。
     * <p>
     * 按顺序将 {@code StringBuffer} 参数的字符追加到此 {@code StringBuffer} 的内容中，使此 {@code StringBuffer} 的长度增加参数的长度。
     * 如果 {@code sb} 为 {@code null}，则追加四个字符 {@code "null"}。
     * <p>
     * 假设 <i>n</i> 是旧字符序列的长度，即在执行 {@code append} 方法之前包含在 {@code StringBuffer} 中的字符序列的长度。那么新字符序列中索引为 <i>k</i> 的字符等于旧字符序列中索引为 <i>k</i> 的字符，如果 <i>k</i> 小于 <i>n</i>；否则，它等于参数 {@code sb} 中索引为 <i>k-n</i> 的字符。
     * <p>
     * 此方法同步 {@code this}，即目标对象，但不同步源（{@code sb}）。
     *
     * @param   sb   要追加的 {@code StringBuffer}。
     * @return  对此对象的引用。
     * @since 1.4
     */
    public synchronized StringBuffer append(StringBuffer sb) {
        toStringCache = null;
        super.append(sb);
        return this;
    }

    /**
     * @since 1.8
     */
    @Override
    synchronized StringBuffer append(AbstractStringBuilder asb) {
        toStringCache = null;
        super.append(asb);
        return this;
    }

    /**
     * 将指定的 {@code CharSequence} 追加到此序列。
     * <p>
     * 按顺序将 {@code CharSequence} 参数的字符追加到此序列中，使此序列的长度增加参数的长度。
     *
     * <p>此方法的结果与调用 this.append(s, 0, s.length()) 完全相同。
     *
     * <p>此方法同步 {@code this}，即目标对象，但不同步源（{@code s}）。
     *
     * <p>如果 {@code s} 为 {@code null}，则追加四个字符 {@code "null"}。
     *
     * @param   s 要追加的 {@code CharSequence}。
     * @return  对此对象的引用。
     * @since 1.5
     */
    @Override
    public synchronized StringBuffer append(CharSequence s) {
        toStringCache = null;
        super.append(s);
        return this;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @since      1.5
     */
    @Override
    public synchronized StringBuffer append(CharSequence s, int start, int end)
    {
        toStringCache = null;
        super.append(s, start, end);
        return this;
    }

    @Override
    public synchronized StringBuffer append(char[] str) {
        toStringCache = null;
        super.append(str);
        return this;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public synchronized StringBuffer append(char[] str, int offset, int len) {
        toStringCache = null;
        super.append(str, offset, len);
        return this;
    }

    @Override
    public synchronized StringBuffer append(boolean b) {
        toStringCache = null;
        super.append(b);
        return this;
    }

    @Override
    public synchronized StringBuffer append(char c) {
        toStringCache = null;
        super.append(c);
        return this;
    }

    @Override
    public synchronized StringBuffer append(int i) {
        toStringCache = null;
        super.append(i);
        return this;
    }

    /**
     * @since 1.5
     */
    @Override
    public synchronized StringBuffer appendCodePoint(int codePoint) {
        toStringCache = null;
        super.appendCodePoint(codePoint);
        return this;
    }

    @Override
    public synchronized StringBuffer append(long lng) {
        toStringCache = null;
        super.append(lng);
        return this;
    }

    @Override
    public synchronized StringBuffer append(float f) {
        toStringCache = null;
        super.append(f);
        return this;
    }

    @Override
    public synchronized StringBuffer append(double d) {
        toStringCache = null;
        super.append(d);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     * @since      1.2
     */
    @Override
    public synchronized StringBuffer delete(int start, int end) {
        toStringCache = null;
        super.delete(start, end);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     * @since      1.2
     */
    @Override
    public synchronized StringBuffer deleteCharAt(int index) {
        toStringCache = null;
        super.deleteCharAt(index);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     * @since      1.2
     */
    @Override
    public synchronized StringBuffer replace(int start, int end, String str) {
        toStringCache = null;
        super.replace(start, end, str);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     * @since      1.2
     */
    @Override
    public synchronized String substring(int start) {
        return substring(start, count);
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @since      1.4
     */
    @Override
    public synchronized CharSequence subSequence(int start, int end) {
        return super.substring(start, end);
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     * @since      1.2
     */
    @Override
    public synchronized String substring(int start, int end) {
        return super.substring(start, end);
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     * @since      1.2
     */
    @Override
    public synchronized StringBuffer insert(int index, char[] str, int offset,
                                            int len)
    {
        toStringCache = null;
        super.insert(index, str, offset, len);
        return this;
    }


/**
 * @throws StringIndexOutOfBoundsException {@inheritDoc}
 */
@Override
public synchronized StringBuffer insert(int offset, Object obj) {
    toStringCache = null;
    super.insert(offset, String.valueOf(obj));
    return this;
}

/**
 * @throws StringIndexOutOfBoundsException {@inheritDoc}
 */
@Override
public synchronized StringBuffer insert(int offset, String str) {
    toStringCache = null;
    super.insert(offset, str);
    return this;
}

/**
 * @throws StringIndexOutOfBoundsException {@inheritDoc}
 */
@Override
public synchronized StringBuffer insert(int offset, char[] str) {
    toStringCache = null;
    super.insert(offset, str);
    return this;
}

/**
 * @throws IndexOutOfBoundsException {@inheritDoc}
 * @since      1.5
 */
@Override
public StringBuffer insert(int dstOffset, CharSequence s) {
    // 注意，同步通过调用其他 StringBuffer 方法实现
    // 在将 s 窄化为特定类型后
    // 同样适用于清除 toStringCache
    super.insert(dstOffset, s);
    return this;
}

/**
 * @throws IndexOutOfBoundsException {@inheritDoc}
 * @since      1.5
 */
@Override
public synchronized StringBuffer insert(int dstOffset, CharSequence s,
        int start, int end)
{
    toStringCache = null;
    super.insert(dstOffset, s, start, end);
    return this;
}

/**
 * @throws StringIndexOutOfBoundsException {@inheritDoc}
 */
@Override
public  StringBuffer insert(int offset, boolean b) {
    // 注意，同步通过调用 StringBuffer insert(int, String) 实现
    // 在通过超类方法将 b 转换为 String 之后
    // 同样适用于清除 toStringCache
    super.insert(offset, b);
    return this;
}

/**
 * @throws IndexOutOfBoundsException {@inheritDoc}
 */
@Override
public synchronized StringBuffer insert(int offset, char c) {
    toStringCache = null;
    super.insert(offset, c);
    return this;
}

/**
 * @throws StringIndexOutOfBoundsException {@inheritDoc}
 */
@Override
public StringBuffer insert(int offset, int i) {
    // 注意，同步通过调用 StringBuffer insert(int, String) 实现
    // 在通过超类方法将 i 转换为 String 之后
    // 同样适用于清除 toStringCache
    super.insert(offset, i);
    return this;
}

/**
 * @throws StringIndexOutOfBoundsException {@inheritDoc}
 */
@Override
public StringBuffer insert(int offset, long l) {
    // 注意，同步通过调用 StringBuffer insert(int, String) 实现
    // 在通过超类方法将 l 转换为 String 之后
    // 同样适用于清除 toStringCache
    super.insert(offset, l);
    return this;
}

/**
 * @throws StringIndexOutOfBoundsException {@inheritDoc}
 */
@Override
public StringBuffer insert(int offset, float f) {
    // 注意，同步通过调用 StringBuffer insert(int, String) 实现
    // 在通过超类方法将 f 转换为 String 之后
    // 同样适用于清除 toStringCache
    super.insert(offset, f);
    return this;
}

/**
 * @throws StringIndexOutOfBoundsException {@inheritDoc}
 */
@Override
public StringBuffer insert(int offset, double d) {
    // 注意，同步通过调用 StringBuffer insert(int, String) 实现
    // 在通过超类方法将 d 转换为 String 之后
    // 同样适用于清除 toStringCache
    super.insert(offset, d);
    return this;
}

/**
 * @since      1.4
 */
@Override
public int indexOf(String str) {
    // 注意，同步通过调用其他 StringBuffer 方法实现
    return super.indexOf(str);
}

/**
 * @since      1.4
 */
@Override
public synchronized int indexOf(String str, int fromIndex) {
    return super.indexOf(str, fromIndex);
}

/**
 * @since      1.4
 */
@Override
public int lastIndexOf(String str) {
    // 注意，同步通过调用其他 StringBuffer 方法实现
    return lastIndexOf(str, count);
}

/**
 * @since      1.4
 */
@Override
public synchronized int lastIndexOf(String str, int fromIndex) {
    return super.lastIndexOf(str, fromIndex);
}

/**
 * @since   JDK1.0.2
 */
@Override
public synchronized StringBuffer reverse() {
    toStringCache = null;
    super.reverse();
    return this;
}

@Override
public synchronized String toString() {
    if (toStringCache == null) {
        toStringCache = Arrays.copyOfRange(value, 0, count);
    }
    return new String(toStringCache, true);
}

/**
 * StringBuffer 的可序列化字段。
 *
 * @serialField value  char[]
 *              此 StringBuffer 的支持字符数组。
 * @serialField count int
 *              此 StringBuffer 中的字符数。
 * @serialField shared  boolean
 *              一个标志，表示支持数组是否共享。
 *              反序列化时忽略该值。
 */
private static final ObjectStreamField[] serialPersistentFields =
{
    new ObjectStreamField("value", char[].class),
    new ObjectStreamField("count", Integer.TYPE),
    new ObjectStreamField("shared", Boolean.TYPE),
};

/**
 * {@code writeObject} 方法被调用以将 {@code StringBuffer} 的状态写入流。
 */
private synchronized void writeObject(ObjectOutputStream s)
    throws IOException {
    ObjectOutputStream.PutField fields = s.putFields();
    fields.put("value", value);
    fields.put("count", count);
    fields.put("shared", false);
    s.writeFields();
}

/**
 * {@code readObject} 方法被调用以从流中恢复 {@code StringBuffer} 的状态。
 */
private void readObject(ObjectInputStream s)
    throws IOException, ClassNotFoundException {
    ObjectInputStream.GetField fields = s.readFields();
    value = (char[])fields.get("value", null);
    int c = fields.get("count", 0);
    if (c < 0 || c > value.length) {
        throw new StreamCorruptedException("count value invalid");
    }
    count = c;
}
