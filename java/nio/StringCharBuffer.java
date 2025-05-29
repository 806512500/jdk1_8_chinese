/*
 * 版权所有 (c) 2000, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.nio;


// ## 如果序列是一个字符串，使用反射来共享其数组

class StringCharBuffer                                  // 包私有
    extends CharBuffer
{
    CharSequence str;

    StringCharBuffer(CharSequence s, int start, int end) { // 包私有
        super(-1, start, end, s.length());
        int n = s.length();
        if ((start < 0) || (start > n) || (end < start) || (end > n))
            throw new IndexOutOfBoundsException();
        str = s;
    }

    public CharBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        int rem = (pos <= lim ? lim - pos : 0);
        return new StringCharBuffer(str,
                                    -1,
                                    0,
                                    rem,
                                    rem,
                                    offset + pos);
    }

    private StringCharBuffer(CharSequence s,
                             int mark,
                             int pos,
                             int limit,
                             int cap,
                             int offset) {
        super(mark, pos, limit, cap, null, offset);
        str = s;
    }

    public CharBuffer duplicate() {
        return new StringCharBuffer(str, markValue(),
                                    position(), limit(), capacity(), offset);
    }

    public CharBuffer asReadOnlyBuffer() {
        return duplicate();
    }

    public final char get() {
        return str.charAt(nextGetIndex() + offset);
    }

    public final char get(int index) {
        return str.charAt(checkIndex(index) + offset);
    }

    char getUnchecked(int index) {
        return str.charAt(index + offset);
    }

    // ## 重写批量获取方法以提高性能

    public final CharBuffer put(char c) {
        throw new ReadOnlyBufferException();
    }

    public final CharBuffer put(int index, char c) {
        throw new ReadOnlyBufferException();
    }

    public final CharBuffer compact() {
        throw new ReadOnlyBufferException();
    }

    public final boolean isReadOnly() {
        return true;
    }

    final String toString(int start, int end) {
        return str.toString().substring(start + offset, end + offset);
    }

    public final CharBuffer subSequence(int start, int end) {
        try {
            int pos = position();
            return new StringCharBuffer(str,
                                        -1,
                                        pos + checkIndex(start, pos),
                                        pos + checkIndex(end, pos),
                                        capacity(),
                                        offset);
        } catch (IllegalArgumentException x) {
            throw new IndexOutOfBoundsException();
        }
    }

    public boolean isDirect() {
        return false;
    }

    public ByteOrder order() {
        return ByteOrder.nativeOrder();
    }

}
