/*
 * 版权所有 (c) 2003, 2021，Oracle 及/或其附属公司。保留所有权利。
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
import java.io.Serializable;
import java.io.StreamCorruptedException;

/**
 * 一个可变的字符序列。此类提供了一个与 {@code StringBuffer} 兼容的 API，但不保证同步。
 * 本类设计用于在单个线程使用字符串缓冲区的地方（通常情况下是这样）作为 {@code StringBuffer} 的直接替代品。
 * 在可能的情况下，建议优先使用此类而不是 {@code StringBuffer}，因为它在大多数实现中会更快。
 *
 * <p>{@code StringBuilder} 的主要操作是 {@code append} 和 {@code insert} 方法，这些方法被重载以接受任何类型的数据。
 * 每个方法实际上都会将给定的数据转换为字符串，然后将该字符串的字符追加或插入到字符串构建器中。
 * {@code append} 方法始终在构建器的末尾添加这些字符；而 {@code insert} 方法则在指定的位置添加字符。
 * <p>
 * 例如，如果 {@code z} 指向一个当前内容为 "{@code start}" 的字符串构建器对象，
 * 那么方法调用 {@code z.append("le")} 会使字符串构建器包含 "{@code startle}"，而
 * {@code z.insert(4, "le")} 会将字符串构建器的内容更改为 "{@code starlet}"。
 * <p>
 * 通常，如果 sb 指向一个 {@code StringBuilder} 的实例，
 * 那么 {@code sb.append(x)} 的效果与 {@code sb.insert(sb.length(), x)} 相同。
 * <p>
 * 每个字符串构建器都有一个容量。只要字符串构建器中包含的字符序列的长度不超过容量，
 * 就不需要分配新的内部缓冲区。如果内部缓冲区溢出，它会自动扩大。
 *
 * <p>{@code StringBuilder} 的实例不适合多线程使用。如果需要这样的同步，则建议使用 {@link java.lang.StringBuffer}。
 *
 * <p>除非另有说明，否则将 {@code null} 参数传递给此类的构造函数或方法将导致抛出 {@link NullPointerException}。
 *
 * @author      Michael McCloskey
 * @see         java.lang.StringBuffer
 * @see         java.lang.String
 * @since       1.5
 */
public final class StringBuilder
    extends AbstractStringBuilder
    implements Serializable, CharSequence
{

    /** 用于互操作的 serialVersionUID */
    static final long serialVersionUID = 4383685877147921099L;

    /**
     * 构造一个没有字符的字符串构建器，初始容量为 16 个字符。
     */
    public StringBuilder() {
        super(16);
    }

    /**
     * 构造一个没有字符的字符串构建器，初始容量由 {@code capacity} 参数指定。
     *
     * @param      capacity  初始容量。
     * @throws     NegativeArraySizeException  如果 {@code capacity}
     *               参数小于 {@code 0}。
     */
    public StringBuilder(int capacity) {
        super(capacity);
    }

    /**
     * 构造一个初始化为指定字符串内容的字符串构建器。字符串构建器的初始容量为
     * {@code 16} 加上字符串参数的长度。
     *
     * @param   str   缓冲区的初始内容。
     */
    public StringBuilder(String str) {
        super(str.length() + 16);
        append(str);
    }

    /**
     * 构造一个包含与指定 {@code CharSequence} 相同字符的字符串构建器。
     * 字符串构建器的初始容量为 {@code 16} 加上
     * {@code CharSequence} 参数的长度。
     *
     * @param      seq   要复制的序列。
     */
    public StringBuilder(CharSequence seq) {
        this(seq.length() + 16);
        append(seq);
    }

    @Override
    public StringBuilder append(Object obj) {
        return append(String.valueOf(obj));
    }

    @Override
    public StringBuilder append(String str) {
        super.append(str);
        return this;
    }

    /**
     * 将指定的 {@code StringBuffer} 追加到此序列。
     * <p>
     * 按顺序将 {@code StringBuffer} 参数中的字符追加到此序列，使此序列的长度增加参数的长度。
     * 如果 {@code sb} 为 {@code null}，则将四个字符 {@code "null"} 追加到此序列。
     * <p>
     * 假设 <i>n</i> 是执行 {@code append} 方法之前此字符序列的长度。那么新字符序列中索引为
     * <i>k</i> 的字符等于旧字符序列中索引为 <i>k</i> 的字符，如果 <i>k</i> 小于 <i>n</i>；
     * 否则，它等于参数 {@code sb} 中索引为 <i>k-n</i> 的字符。
     *
     * @param   sb   要追加的 {@code StringBuffer}。
     * @return  对此对象的引用。
     */
    public StringBuilder append(StringBuffer sb) {
        super.append(sb);
        return this;
    }

    @Override
    public StringBuilder append(CharSequence s) {
        super.append(s);
        return this;
    }

    /**
     * @throws     IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder append(CharSequence s, int start, int end) {
        super.append(s, start, end);
        return this;
    }

    @Override
    public StringBuilder append(char[] str) {
        super.append(str);
        return this;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder append(char[] str, int offset, int len) {
        super.append(str, offset, len);
        return this;
    }

    @Override
    public StringBuilder append(boolean b) {
        super.append(b);
        return this;
    }

    @Override
    public StringBuilder append(char c) {
        super.append(c);
        return this;
    }

    @Override
    public StringBuilder append(int i) {
        super.append(i);
        return this;
    }

    @Override
    public StringBuilder append(long lng) {
        super.append(lng);
        return this;
    }

    @Override
    public StringBuilder append(float f) {
        super.append(f);
        return this;
    }

    @Override
    public StringBuilder append(double d) {
        super.append(d);
        return this;
    }

    /**
     * @since 1.5
     */
    @Override
    public StringBuilder appendCodePoint(int codePoint) {
        super.appendCodePoint(codePoint);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder delete(int start, int end) {
        super.delete(start, end);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder deleteCharAt(int index) {
        super.deleteCharAt(index);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder replace(int start, int end, String str) {
        super.replace(start, end, str);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder insert(int index, char[] str, int offset,
                                int len)
    {
        super.insert(index, str, offset, len);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder insert(int offset, Object obj) {
            super.insert(offset, obj);
            return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder insert(int offset, String str) {
        super.insert(offset, str);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder insert(int offset, char[] str) {
        super.insert(offset, str);
        return this;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder insert(int dstOffset, CharSequence s) {
            super.insert(dstOffset, s);
            return this;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder insert(int dstOffset, CharSequence s,
                                int start, int end)
    {
        super.insert(dstOffset, s, start, end);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder insert(int offset, boolean b) {
        super.insert(offset, b);
        return this;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder insert(int offset, char c) {
        super.insert(offset, c);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder insert(int offset, int i) {
        super.insert(offset, i);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder insert(int offset, long l) {
        super.insert(offset, l);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder insert(int offset, float f) {
        super.insert(offset, f);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder insert(int offset, double d) {
        super.insert(offset, d);
        return this;
    }

    @Override
    public int indexOf(String str) {
        return super.indexOf(str);
    }

    @Override
    public int indexOf(String str, int fromIndex) {
        return super.indexOf(str, fromIndex);
    }

    @Override
    public int lastIndexOf(String str) {
        return super.lastIndexOf(str);
    }

    @Override
    public int lastIndexOf(String str, int fromIndex) {
        return super.lastIndexOf(str, fromIndex);
    }

    @Override
    public StringBuilder reverse() {
        super.reverse();
        return this;
    }

    @Override
    public String toString() {
        // 创建一个副本，不共享数组
        return new String(value, 0, count);
    }

    /**
     * 将 {@code StringBuilder} 实例的状态保存到流中（即序列化它）。
     *
     * @serialData 字符串构建器中当前存储的字符数（{@code int}），后跟字符串构建器中的字符（{@code char[]}）。 
     *             {@code char} 数组的长度可能大于字符串构建器中当前存储的字符数，在这种情况下，多余的字符将被忽略。
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(count);
        s.writeObject(value);
    }

    /**
     * readObject 被调用来从流中恢复 StringBuilder 的状态。
     */
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        int c = s.readInt();
        char[] val = (char[]) s.readObject();
        if (c < 0 || c > val.length) {
            throw new StreamCorruptedException("count value invalid");
        }
        count = c;
        value = val;
    }
    
}