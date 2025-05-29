
/*
 * 版权所有 (c) 1994, 2021，Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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
 * 字符缓冲区类似于 {@link String}，但可以被修改。在任何时候，它包含一些特定的字符序列，但
 * 该序列的长度和内容可以通过某些方法调用进行更改。
 * <p>
 * 字符缓冲区可以安全地被多个线程使用。方法在必要时是同步的，以便所有对任何特定实例的操作
 * 都像它们发生在某个与每个单独线程调用的方法顺序一致的串行顺序中一样。
 * <p>
 * {@code StringBuffer} 的主要操作是 {@code append} 和 {@code insert} 方法，这些方法
 * 被重载以接受任何类型的数据。每个方法有效地将给定的数据转换为字符串，然后将该字符串的字符
 * 追加或插入到字符缓冲区中。{@code append} 方法总是将这些字符添加到缓冲区的末尾；
 * {@code insert} 方法则在指定的位置添加字符。
 * <p>
 * 例如，如果 {@code z} 指向一个当前内容为 {@code "start"} 的字符缓冲区对象，
 * 则方法调用 {@code z.append("le")} 会使字符缓冲区包含 {@code "startle"}，而
 * {@code z.insert(4, "le")} 会将字符缓冲区更改为包含 {@code "starlet"}。
 * <p>
 * 一般来说，如果 sb 指向一个 {@code StringBuffer} 的实例，那么 {@code sb.append(x)}
 * 的效果与 {@code sb.insert(sb.length(), x)} 相同。
 * <p>
 * 每当涉及源序列的操作（如从源序列追加或插入）时，此类仅同步执行操作的字符缓冲区，而不是源。
 * 请注意，虽然 {@code StringBuffer} 被设计为可以安全地从多个线程并发使用，但如果构造函数或
 * {@code append} 或 {@code insert} 操作传递了一个跨线程共享的源序列，调用代码必须确保
 * 操作期间源序列具有一致且不变的视图。
 * 这可以通过调用者在操作调用期间持有锁、使用不可变的源序列或不跨线程共享源序列来满足。
 * <p>
 * 每个字符缓冲区都有一个容量。只要字符缓冲区中包含的字符序列的长度不超过容量，就不需要分配新的内部
 * 缓冲数组。如果内部缓冲区溢出，它将自动变大。
 * <p>
 * 除非另有说明，否则将 {@code null} 参数传递给此类的构造函数或方法将导致抛出 {@link NullPointerException}。
 * <p>
 * 从 JDK 5 版本开始，此类已被一个等效的类补充，该类专为单线程使用设计，即 {@link StringBuilder}。
 * 通常应优先使用 {@code StringBuilder} 类，因为它支持所有相同的操作，但速度更快，因为它不执行同步。
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
     * 由 toString 返回的最后一个值的缓存。每当 StringBuffer 被修改时清除。
     */
    private transient char[] toStringCache;

    /** 为了互操作性使用 JDK 1.0.2 的 serialVersionUID */
    static final long serialVersionUID = 3388685877147921107L;

    /**
     * 构造一个没有字符的字符缓冲区，初始容量为 16 个字符。
     */
    public StringBuffer() {
        super(16);
    }

    /**
     * 构造一个没有字符的字符缓冲区，并指定初始容量。
     *
     * @param      capacity  初始容量。
     * @exception  NegativeArraySizeException  如果 {@code capacity}
     *               参数小于 {@code 0}。
     */
    public StringBuffer(int capacity) {
        super(capacity);
    }

    /**
     * 构造一个初始化为指定字符串内容的字符缓冲区。字符缓冲区的初始容量是
     * {@code 16} 加上字符串参数的长度。
     *
     * @param   str   缓冲区的初始内容。
     */
    public StringBuffer(String str) {
        super(str.length() + 16);
        append(str);
    }

    /**
     * 构造一个包含与指定 {@code CharSequence} 相同字符的字符缓冲区。字符缓冲区的初始容量是
     * {@code 16} 加上 {@code CharSequence} 参数的长度。
     * <p>
     * 如果指定的 {@code CharSequence} 的长度小于或等于零，则返回一个容量为
     * {@code 16} 的空缓冲区。
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
     * Appends the specified {@code StringBuffer} to this sequence.
     * <p>
     * The characters of the {@code StringBuffer} argument are appended,
     * in order, to the contents of this {@code StringBuffer}, increasing the
     * length of this {@code StringBuffer} by the length of the argument.
     * If {@code sb} is {@code null}, then the four characters
     * {@code "null"} are appended to this {@code StringBuffer}.
     * <p>
     * Let <i>n</i> be the length of the old character sequence, the one
     * contained in the {@code StringBuffer} just prior to execution of the
     * {@code append} method. Then the character at index <i>k</i> in
     * the new character sequence is equal to the character at index <i>k</i>
     * in the old character sequence, if <i>k</i> is less than <i>n</i>;
     * otherwise, it is equal to the character at index <i>k-n</i> in the
     * argument {@code sb}.
     * <p>
     * This method synchronizes on {@code this}, the destination
     * object, but does not synchronize on the source ({@code sb}).
     *
     * @param   sb   the {@code StringBuffer} to append.
     * @return  a reference to this object.
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
     * Appends the specified {@code CharSequence} to this
     * sequence.
     * <p>
     * The characters of the {@code CharSequence} argument are appended,
     * in order, increasing the length of this sequence by the length of the
     * argument.
     *
     * <p>The result of this method is exactly the same as if it were an
     * invocation of this.append(s, 0, s.length());
     *
     * <p>This method synchronizes on {@code this}, the destination
     * object, but does not synchronize on the source ({@code s}).
     *
     * <p>If {@code s} is {@code null}, then the four characters
     * {@code "null"} are appended.
     *
     * @param   s the {@code CharSequence} to append.
     * @return  a reference to this object.
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
    // Note, synchronization achieved via invocations of other StringBuffer methods
    // after narrowing of s to specific type
    // Ditto for toStringCache clearing
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
    // Note, synchronization achieved via invocation of StringBuffer insert(int, String)
    // after conversion of b to String by super class method
    // Ditto for toStringCache clearing
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
    // Note, synchronization achieved via invocation of StringBuffer insert(int, String)
    // after conversion of i to String by super class method
    // Ditto for toStringCache clearing
    super.insert(offset, i);
    return this;
}

/**
 * @throws StringIndexOutOfBoundsException {@inheritDoc}
 */
@Override
public StringBuffer insert(int offset, long l) {
    // Note, synchronization achieved via invocation of StringBuffer insert(int, String)
    // after conversion of l to String by super class method
    // Ditto for toStringCache clearing
    super.insert(offset, l);
    return this;
}

/**
 * @throws StringIndexOutOfBoundsException {@inheritDoc}
 */
@Override
public StringBuffer insert(int offset, float f) {
    // Note, synchronization achieved via invocation of StringBuffer insert(int, String)
    // after conversion of f to String by super class method
    // Ditto for toStringCache clearing
    super.insert(offset, f);
    return this;
}

/**
 * @throws StringIndexOutOfBoundsException {@inheritDoc}
 */
@Override
public StringBuffer insert(int offset, double d) {
    // Note, synchronization achieved via invocation of StringBuffer insert(int, String)
    // after conversion of d to String by super class method
    // Ditto for toStringCache clearing
    super.insert(offset, d);
    return this;
}

/**
 * @since      1.4
 */
@Override
public int indexOf(String str) {
    // Note, synchronization achieved via invocations of other StringBuffer methods
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
        // Note, synchronization achieved via invocations of other StringBuffer methods
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
     * Serializable fields for StringBuffer.
     *
     * @serialField value  char[]
     *              The backing character array of this StringBuffer.
     * @serialField count int
     *              The number of characters in this StringBuffer.
     * @serialField shared  boolean
     *              A flag indicating whether the backing array is shared.
     *              The value is ignored upon deserialization.
     */
    private static final ObjectStreamField[] serialPersistentFields =
    {
        new ObjectStreamField("value", char[].class),
        new ObjectStreamField("count", Integer.TYPE),
        new ObjectStreamField("shared", Boolean.TYPE),
    };

    /**
     * The {@code writeObject} method is called to write the state of the
     * {@code StringBuffer} to a stream.
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
     * The {@code readObject} method is called to restore the state of the
     * {@code StringBuffer} from a stream.
     */
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = s.readFields();
        char[] val = (char[])fields.get("value", null);
        int c = fields.get("count", 0);
        if (c < 0 || c > val.length) {
            throw new StreamCorruptedException("count value invalid");
        }
        count = c;
        value = val;
    }
}
