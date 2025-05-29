
/*
 * Copyright (c) 1999, 2020, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

package java.math;

/**
 * 一个用于表示多精度整数的类，通过允许数字只占用数组的一部分来高效利用分配的空间，从而减少数组的重新分配。
 * 在进行多次迭代的操作时，用于保存数字的数组只有在必要时才会重新分配，并且不必与它所表示的数字大小相同。
 * 可变数字允许在同一个数字上进行计算，而不需要为计算的每一步都创建一个新数字，这与 BigInteger 的情况不同。
 *
 * @see     BigInteger
 * @author  Michael McCloskey
 * @author  Timothy Buktu
 * @since   1.3
 */

import static java.math.BigDecimal.INFLATED;
import static java.math.BigInteger.LONG_MASK;
import java.util.Arrays;

class MutableBigInteger {
    /**
     * 以大端序存储此 MutableBigInteger 的大小。
     * 大小可以从 value 数组的某个偏移位置开始，并且可能在 value 数组的长度之前结束。
     */
    int[] value;

    /**
     * 当前用于存储此 MutableBigInteger 大小的 value 数组中的 int 数量。
     * 大小从偏移位置开始，offset + intLen 可能小于 value.length。
     */
    int intLen;

    /**
     * 此 MutableBigInteger 大小在 value 数组中的起始偏移位置。
     */
    int offset = 0;

    // 常量
    /**
     * 具有一个元素值数组且值为 1 的 MutableBigInteger。用于 BigDecimal 的 divideAndRound 方法来递增商。
     * 仅当方法不会修改此对象时才使用此常量。
     */
    static final MutableBigInteger ONE = new MutableBigInteger(1);

    /**
     * 在除法前取消二的幂的最小 intLen。
     * 如果 int 的数量小于此阈值，divideKnuth 不会从被除数和除数中消除共同的二的幂。
     */
    static final int KNUTH_POW2_THRESH_LEN = 6;

    /**
     * 在除法前取消二的幂的最小尾部零 int 数量。
     * 如果被除数和除数在末尾没有至少这么多的零 int，divideKnuth 不会从被除数和除数中消除共同的二的幂。
     */
    static final int KNUTH_POW2_THRESH_ZEROS = 3;

    // 构造函数

    /**
     * 默认构造函数。创建一个具有一个字长容量的空 MutableBigInteger。
     */
    MutableBigInteger() {
        value = new int[1];
        intLen = 0;
    }

    /**
     * 使用指定的 int 值构造一个新的 MutableBigInteger。
     */
    MutableBigInteger(int val) {
        value = new int[1];
        intLen = 1;
        value[0] = val;
    }

    /**
     * 使用指定的 value 数组构造一个新的 MutableBigInteger，长度为数组的长度。
     */
    MutableBigInteger(int[] val) {
        value = val;
        intLen = val.length;
    }

    /**
     * 构造一个大小等于指定 BigInteger 的新 MutableBigInteger。
     */
    MutableBigInteger(BigInteger b) {
        intLen = b.mag.length;
        value = Arrays.copyOf(b.mag, intLen);
    }

    /**
     * 构造一个大小等于指定 MutableBigInteger 的新 MutableBigInteger。
     */
    MutableBigInteger(MutableBigInteger val) {
        intLen = val.intLen;
        value = Arrays.copyOfRange(val.value, val.offset, val.offset + intLen);
    }

    /**
     * 将此数字转换为一个 n-int 的数，其所有位都是 1。
     * 用于 Burnikel-Ziegler 除法。
     * @param n value 数组中的 int 数量
     * @return 等于 {@code ((1<<(32*n)))-1} 的数
     */
    private void ones(int n) {
        if (n > value.length)
            value = new int[n];
        Arrays.fill(value, -1);
        offset = 0;
        intLen = n;
    }

    /**
     * 内部辅助方法，返回大小数组。调用者不应修改返回的数组。
     */
    private int[] getMagnitudeArray() {
        if (offset > 0 || value.length != intLen)
            return Arrays.copyOfRange(value, offset, offset + intLen);
        return value;
    }

    /**
     * 将此 MutableBigInteger 转换为 long 值。调用者必须确保此 MutableBigInteger 可以转换为 long。
     */
    private long toLong() {
        assert (intLen <= 2) : "this MutableBigInteger exceeds the range of long";
        if (intLen == 0)
            return 0;
        long d = value[offset] & LONG_MASK;
        return (intLen == 2) ? d << 32 | (value[offset + 1] & LONG_MASK) : d;
    }

    /**
     * 将此 MutableBigInteger 转换为 BigInteger 对象。
     */
    BigInteger toBigInteger(int sign) {
        if (intLen == 0 || sign == 0)
            return BigInteger.ZERO;
        return new BigInteger(getMagnitudeArray(), sign);
    }

    /**
     * 将此数字转换为非负的 {@code BigInteger}。
     */
    BigInteger toBigInteger() {
        normalize();
        return toBigInteger(isZero() ? 0 : 1);
    }

    /**
     * 将此 MutableBigInteger 转换为具有指定符号和比例的 BigDecimal 对象。
     */
    BigDecimal toBigDecimal(int sign, int scale) {
        if (intLen == 0 || sign == 0)
            return BigDecimal.zeroValueOf(scale);
        int[] mag = getMagnitudeArray();
        int len = mag.length;
        int d = mag[0];
        // 如果此 MutableBigInteger 不能转换为 long，我们需要为结果的 BigDecimal 对象创建一个 BigInteger 对象。
        if (len > 2 || (d < 0 && len == 2))
            return new BigDecimal(new BigInteger(mag, sign), INFLATED, scale, 0);
        long v = (len == 2) ?
            ((mag[1] & LONG_MASK) | (d & LONG_MASK) << 32) :
            d & LONG_MASK;
        return BigDecimal.valueOf(sign == -1 ? -v : v, scale);
    }

                /**
     * 用于将 MutableBigInteger 对象转换为给定符号的 long 值。
     * 如果值无法放入 long 中，则返回 INFLATED。
     */
    long toCompactValue(int sign) {
        if (intLen == 0 || sign == 0)
            return 0L;
        int[] mag = getMagnitudeArray();
        int len = mag.length;
        int d = mag[0];
        // 如果这个 MutableBigInteger 无法放入 long 中，我们需要
        // 为结果的 BigDecimal 对象创建一个 BigInteger 对象。
        if (len > 2 || (d < 0 && len == 2))
            return INFLATED;
        long v = (len == 2) ?
            ((mag[1] & LONG_MASK) | (d & LONG_MASK) << 32) :
            d & LONG_MASK;
        return sign == -1 ? -v : v;
    }

    /**
     * 清除 MutableBigInteger 以供重用。
     */
    void clear() {
        offset = intLen = 0;
        for (int index=0, n=value.length; index < n; index++)
            value[index] = 0;
    }

    /**
     * 将 MutableBigInteger 设置为零，移除其偏移量。
     */
    void reset() {
        offset = intLen = 0;
    }

    /**
     * 比较两个 MutableBigInteger 的大小。返回 -1, 0 或 1
     * 表示此 MutableBigInteger 在数值上小于、等于或大于 <tt>b</tt>。
     */
    final int compare(MutableBigInteger b) {
        int blen = b.intLen;
        if (intLen < blen)
            return -1;
        if (intLen > blen)
           return 1;

        // 添加 Integer.MIN_VALUE 以使比较行为如同无符号整数比较。
        int[] bval = b.value;
        for (int i = offset, j = b.offset; i < intLen + offset; i++, j++) {
            int b1 = value[i] + 0x80000000;
            int b2 = bval[j]  + 0x80000000;
            if (b1 < b2)
                return -1;
            if (b1 > b2)
                return 1;
        }
        return 0;
    }

    /**
     * 返回一个值，该值等于 {@code b.leftShift(32*ints); return compare(b);}
     * 会返回的值，但不会改变 {@code b} 的值。
     */
    private int compareShifted(MutableBigInteger b, int ints) {
        int blen = b.intLen;
        int alen = intLen - ints;
        if (alen < blen)
            return -1;
        if (alen > blen)
           return 1;

        // 添加 Integer.MIN_VALUE 以使比较行为如同无符号整数比较。
        int[] bval = b.value;
        for (int i = offset, j = b.offset; i < alen + offset; i++, j++) {
            int b1 = value[i] + 0x80000000;
            int b2 = bval[j]  + 0x80000000;
            if (b1 < b2)
                return -1;
            if (b1 > b2)
                return 1;
        }
        return 0;
    }

    /**
     * 将此对象与另一个 MutableBigInteger 对象的一半进行比较（用于余数测试）。
     * 假设没有前导不必要的零，这适用于 divide() 的结果。
     */
    final int compareHalf(MutableBigInteger b) {
        int blen = b.intLen;
        int len = intLen;
        if (len <= 0)
            return blen <= 0 ? 0 : -1;
        if (len > blen)
            return 1;
        if (len < blen - 1)
            return -1;
        int[] bval = b.value;
        int bstart = 0;
        int carry = 0;
        // 只剩下两种情况：len == blen 或 len == blen - 1
        if (len != blen) { // len == blen - 1
            if (bval[bstart] == 1) {
                ++bstart;
                carry = 0x80000000;
            } else
                return -1;
        }
        // 比较值与 b 的右移值，
        // 携带移出的位跨字
        int[] val = value;
        for (int i = offset, j = bstart; i < len + offset;) {
            int bv = bval[j++];
            long hb = ((bv >>> 1) + carry) & LONG_MASK;
            long v = val[i++] & LONG_MASK;
            if (v != hb)
                return v < hb ? -1 : 1;
            carry = (bv & 1) << 31; // carry 将是 0x80000000 或 0
        }
        return carry == 0 ? 0 : -1;
    }

    /**
     * 返回此 MutableBigInteger 中最低设置位的索引。如果此 MutableBigInteger 的大小为零，则返回 -1。
     */
    private final int getLowestSetBit() {
        if (intLen == 0)
            return -1;
        int j, b;
        for (j=intLen-1; (j > 0) && (value[j+offset] == 0); j--)
            ;
        b = value[j+offset];
        if (b == 0)
            return -1;
        return ((intLen-1-j)<<5) + Integer.numberOfTrailingZeros(b);
    }

    /**
     * 返回此 MutableBigInteger 在指定索引处使用的 int。此方法未使用，因为它在所有平台上都不会内联。
     */
    private final int getInt(int index) {
        return value[offset+index];
    }

    /**
     * 返回一个 long，其值等于此 MutableBigInteger 在指定索引处使用的 int 的无符号值。此方法未使用，因为它在所有平台上都不会内联。
     */
    private final long getLong(int index) {
        return value[offset+index] & LONG_MASK;
    }

    /**
     * 确保 MutableBigInteger 处于正常形式，特别是确保没有前导零，并且如果大小为零，则 intLen 为零。
     */
    final void normalize() {
        if (intLen == 0) {
            offset = 0;
            return;
        }

        int index = offset;
        if (value[index] != 0)
            return;

        int indexBound = index+intLen;
        do {
            index++;
        } while(index < indexBound && value[index] == 0);

        int numZeros = index - offset;
        intLen -= numZeros;
        offset = (intLen == 0 ?  0 : offset+numZeros);
    }

    /**
     * 如果此 MutableBigInteger 无法容纳 len 个字，则将值数组的大小增加到 len 个字。
     */
    private final void ensureCapacity(int len) {
        if (value.length < len) {
            value = new int[len];
            offset = 0;
            intLen = len;
        }
    }

    /**
     * 将此 MutableBigInteger 转换为一个没有前导零的 int 数组，其长度等于此 MutableBigInteger 的 intLen。
     */
    int[] toIntArray() {
        int[] result = new int[intLen];
        for(int i=0; i < intLen; i++)
            result[i] = value[offset+i];
        return result;
    }


    /**
     * 将此 MutableBigInteger 的索引 + 偏移量处的 int 设置为 val。
     * 该方法在所有平台上可能不会内联，因此使用频率不如最初预期的那样高。
     */
    void setInt(int index, int val) {
        value[offset + index] = val;
    }

    /**
     * 将此 MutableBigInteger 的值数组设置为指定的数组。
     * intLen 被设置为指定的长度。
     */
    void setValue(int[] val, int length) {
        value = val;
        intLen = length;
        offset = 0;
    }

    /**
     * 将此 MutableBigInteger 的值数组设置为指定数组的副本。
     * intLen 被设置为新数组的长度。
     */
    void copyValue(MutableBigInteger src) {
        int len = src.intLen;
        if (value.length < len)
            value = new int[len];
        System.arraycopy(src.value, src.offset, value, 0, len);
        intLen = len;
        offset = 0;
    }

    /**
     * 将此 MutableBigInteger 的值数组设置为指定数组的副本。
     * intLen 被设置为指定数组的长度。
     */
    void copyValue(int[] val) {
        int len = val.length;
        if (value.length < len)
            value = new int[len];
        System.arraycopy(val, 0, value, 0, len);
        intLen = len;
        offset = 0;
    }

    /**
     * 如果此 MutableBigInteger 的值为一，则返回 true。
     */
    boolean isOne() {
        return (intLen == 1) && (value[offset] == 1);
    }

    /**
     * 如果此 MutableBigInteger 的值为零，则返回 true。
     */
    boolean isZero() {
        return (intLen == 0);
    }

    /**
     * 如果此 MutableBigInteger 是偶数，则返回 true。
     */
    boolean isEven() {
        return (intLen == 0) || ((value[offset + intLen - 1] & 1) == 0);
    }

    /**
     * 如果此 MutableBigInteger 是奇数，则返回 true。
     */
    boolean isOdd() {
        return isZero() ? false : ((value[offset + intLen - 1] & 1) == 1);
    }

    /**
     * 如果此 MutableBigInteger 处于正常形式，则返回 true。一个 MutableBigInteger 处于正常形式，如果它在偏移量之后没有前导零，并且 intLen + 偏移量 <= value.length。
     */
    boolean isNormal() {
        if (intLen + offset > value.length)
            return false;
        if (intLen == 0)
            return true;
        return (value[offset] != 0);
    }

    /**
     * 返回此 MutableBigInteger 的十进制字符串表示形式。
     */
    public String toString() {
        BigInteger b = toBigInteger(1);
        return b.toString();
    }

    /**
     * 类似于 {@link #rightShift(int)}，但 {@code n} 可以大于数字的长度。
     */
    void safeRightShift(int n) {
        if (n/32 >= intLen) {
            reset();
        } else {
            rightShift(n);
        }
    }

    /**
     * 右移此 MutableBigInteger n 位。MutableBigInteger 保持在正常形式。
     */
    void rightShift(int n) {
        if (intLen == 0)
            return;
        int nInts = n >>> 5;
        int nBits = n & 0x1F;
        this.intLen -= nInts;
        if (nBits == 0)
            return;
        int bitsInHighWord = BigInteger.bitLengthForInt(value[offset]);
        if (nBits >= bitsInHighWord) {
            this.primitiveLeftShift(32 - nBits);
            this.intLen--;
        } else {
            primitiveRightShift(nBits);
        }
    }

    /**
     * 类似于 {@link #leftShift(int)}，但 {@code n} 可以为零。
     */
    void safeLeftShift(int n) {
        if (n > 0) {
            leftShift(n);
        }
    }

    /**
     * 左移此 MutableBigInteger n 位。
     */
    void leftShift(int n) {
        /*
         * 如果此 MutableBigInteger 中已经有足够的存储空间，将使用可用空间。利用 value 数组中已使用整数右侧的空间更快，因此如果可能，额外空间将从右侧获取。
         */
        if (intLen == 0)
           return;
        int nInts = n >>> 5;
        int nBits = n&0x1F;
        int bitsInHighWord = BigInteger.bitLengthForInt(value[offset]);

        // 如果移动可以不移动单词完成，则这样做
        if (n <= (32-bitsInHighWord)) {
            primitiveLeftShift(nBits);
            return;
        }

        int newLen = intLen + nInts +1;
        if (nBits <= (32-bitsInHighWord))
            newLen--;
        if (value.length < newLen) {
            // 数组必须增长
            int[] result = new int[newLen];
            for (int i=0; i < intLen; i++)
                result[i] = value[offset+i];
            setValue(result, newLen);
        } else if (value.length - offset >= newLen) {
            // 使用右侧空间
            for(int i=0; i < newLen - intLen; i++)
                value[offset+intLen+i] = 0;
        } else {
            // 必须使用左侧空间
            for (int i=0; i < intLen; i++)
                value[i] = value[offset+i];
            for (int i=intLen; i < newLen; i++)
                value[i] = 0;
            offset = 0;
        }
        intLen = newLen;
        if (nBits == 0)
            return;
        if (nBits <= (32-bitsInHighWord))
            primitiveLeftShift(nBits);
        else
            primitiveRightShift(32 -nBits);
    }

    /**
     * 用于除法的原语。此方法在指定偏移量处将一个除数 a 的倍数加回到被除数 result 中。当 qhat 估计过大，必须进行调整时使用。
     */
    private int divadd(int[] a, int[] result, int offset) {
        long carry = 0;

        for (int j=a.length-1; j >= 0; j--) {
            long sum = (a[j] & LONG_MASK) +
                       (result[j+offset] & LONG_MASK) + carry;
            result[j+offset] = (int)sum;
            carry = sum >>> 32;
        }
        return (int)carry;
    }

    /**
     * 用于除法。它将一个 n 个字的输入 a 与一个字的输入 x 相乘，并从 q 中减去 n 个字的乘积。当从被除数中减去 qhat*除数时需要此操作。
     */
    private int mulsub(int[] q, int[] a, int x, int len, int offset) {
        long xLong = x & LONG_MASK;
        long carry = 0;
        offset += len;


                    for (int j=len-1; j >= 0; j--) {
            long product = (a[j] & LONG_MASK) * xLong + carry;
            long difference = q[offset] - product;
            q[offset--] = (int)difference;
            carry = (product >>> 32)
                     + (((difference & LONG_MASK) >
                         (((~(int)product) & LONG_MASK))) ? 1:0);
        }
        return (int)carry;
    }

    /**
     * 该方法与 mulsun 相同，只是 q 数组不会被更新，方法的唯一结果是借位标志。
     */
    private int mulsubBorrow(int[] q, int[] a, int x, int len, int offset) {
        long xLong = x & LONG_MASK;
        long carry = 0;
        offset += len;
        for (int j=len-1; j >= 0; j--) {
            long product = (a[j] & LONG_MASK) * xLong + carry;
            long difference = q[offset--] - product;
            carry = (product >>> 32)
                     + (((difference & LONG_MASK) >
                         (((~(int)product) & LONG_MASK))) ? 1:0);
        }
        return (int)carry;
    }

    /**
     * 将此 MutableBigInteger 右移 n 位，其中 n 小于 32。
     * 为了提高速度，假设 intLen > 0 且 n > 0。
     */
    private final void primitiveRightShift(int n) {
        int[] val = value;
        int n2 = 32 - n;
        for (int i=offset+intLen-1, c=val[i]; i > offset; i--) {
            int b = c;
            c = val[i-1];
            val[i] = (c << n2) | (b >>> n);
        }
        val[offset] >>>= n;
    }

    /**
     * 将此 MutableBigInteger 左移 n 位，其中 n 小于 32。
     * 为了提高速度，假设 intLen > 0 且 n > 0。
     */
    private final void primitiveLeftShift(int n) {
        int[] val = value;
        int n2 = 32 - n;
        for (int i=offset, c=val[i], m=i+intLen-1; i < m; i++) {
            int b = c;
            c = val[i+1];
            val[i] = (b << n) | (c >>> n2);
        }
        val[offset+intLen-1] <<= n;
    }

    /**
     * 返回一个等于此数字的低 n 个 int 的 {@code BigInteger}。
     */
    private BigInteger getLower(int n) {
        if (isZero()) {
            return BigInteger.ZERO;
        } else if (intLen < n) {
            return toBigInteger(1);
        } else {
            // 去除零
            int len = n;
            while (len > 0 && value[offset+intLen-len] == 0)
                len--;
            int sign = len > 0 ? 1 : 0;
            return new BigInteger(Arrays.copyOfRange(value, offset+intLen-len, offset+intLen), sign);
        }
    }

    /**
     * 丢弃索引大于 {@code n} 的所有 int。
     */
    private void keepLower(int n) {
        if (intLen >= n) {
            offset += intLen - n;
            intLen = n;
        }
    }

    /**
     * 将两个 MutableBigInteger 对象的内容相加。结果存储在当前的 MutableBigInteger 中。
     * 被加数的内容不会改变。
     */
    void add(MutableBigInteger addend) {
        int x = intLen;
        int y = addend.intLen;
        int resultLen = (intLen > addend.intLen ? intLen : addend.intLen);
        int[] result = (value.length < resultLen ? new int[resultLen] : value);

        int rstart = result.length-1;
        long sum;
        long carry = 0;

        // 将两个数字的公共部分相加
        while(x > 0 && y > 0) {
            x--; y--;
            sum = (value[x+offset] & LONG_MASK) +
                (addend.value[y+addend.offset] & LONG_MASK) + carry;
            result[rstart--] = (int)sum;
            carry = sum >>> 32;
        }

        // 将较长数字的剩余部分相加
        while(x > 0) {
            x--;
            if (carry == 0 && result == value && rstart == (x + offset))
                return;
            sum = (value[x+offset] & LONG_MASK) + carry;
            result[rstart--] = (int)sum;
            carry = sum >>> 32;
        }
        while(y > 0) {
            y--;
            sum = (addend.value[y+addend.offset] & LONG_MASK) + carry;
            result[rstart--] = (int)sum;
            carry = sum >>> 32;
        }

        if (carry > 0) { // 结果长度必须增加
            resultLen++;
            if (result.length < resultLen) {
                int temp[] = new int[resultLen];
                // 结果因进位而增加一个字；将低阶位复制到新结果中。
                System.arraycopy(result, 0, temp, 1, result.length);
                temp[0] = 1;
                result = temp;
            } else {
                result[rstart--] = 1;
            }
        }

        value = result;
        intLen = resultLen;
        offset = result.length - resultLen;
    }

    /**
     * 将 {@code addend} 的值左移 {@code n} 个 int 后与当前值相加。
     * 效果与 {@code addend.leftShift(32*ints); add(addend);} 相同，但不会改变 {@code addend} 的值。
     */
    void addShifted(MutableBigInteger addend, int n) {
        if (addend.isZero()) {
            return;
        }

        int x = intLen;
        int y = addend.intLen + n;
        int resultLen = (intLen > y ? intLen : y);
        int[] result = (value.length < resultLen ? new int[resultLen] : value);

        int rstart = result.length-1;
        long sum;
        long carry = 0;

        // 将两个数字的公共部分相加
        while (x > 0 && y > 0) {
            x--; y--;
            int bval = y+addend.offset < addend.value.length ? addend.value[y+addend.offset] : 0;
            sum = (value[x+offset] & LONG_MASK) +
                (bval & LONG_MASK) + carry;
            result[rstart--] = (int)sum;
            carry = sum >>> 32;
        }

        // 将较长数字的剩余部分相加
        while (x > 0) {
            x--;
            if (carry == 0 && result == value && rstart == (x + offset)) {
                return;
            }
            sum = (value[x+offset] & LONG_MASK) + carry;
            result[rstart--] = (int)sum;
            carry = sum >>> 32;
        }
        while (y > 0) {
            y--;
            int bval = y+addend.offset < addend.value.length ? addend.value[y+addend.offset] : 0;
            sum = (bval & LONG_MASK) + carry;
            result[rstart--] = (int)sum;
            carry = sum >>> 32;
        }


                    if (carry > 0) { // 结果长度必须增加
            resultLen++;
            if (result.length < resultLen) {
                int temp[] = new int[resultLen];
                // 由于进位，结果长度增加一位；将低位复制到新的结果中
                System.arraycopy(result, 0, temp, 1, result.length);
                temp[0] = 1;
                result = temp;
            } else {
                result[rstart--] = 1;
            }
        }

        value = result;
        intLen = resultLen;
        offset = result.length - resultLen;
    }

    /**
     * 类似于 {@link #addShifted(MutableBigInteger, int)}，但 {@code this.intLen} 不得大于 {@code n}。
     * 换句话说，将 {@code this} 和 {@code addend} 连接起来。
     */
    void addDisjoint(MutableBigInteger addend, int n) {
        if (addend.isZero())
            return;

        int x = intLen;
        int y = addend.intLen + n;
        int resultLen = (intLen > y ? intLen : y);
        int[] result;
        if (value.length < resultLen)
            result = new int[resultLen];
        else {
            result = value;
            Arrays.fill(value, offset+intLen, value.length, 0);
        }

        int rstart = result.length-1;

        // 如果需要，从 this 复制
        System.arraycopy(value, offset, result, rstart+1-x, x);
        y -= x;
        rstart -= x;

        int len = Math.min(y, addend.value.length-addend.offset);
        System.arraycopy(addend.value, addend.offset, result, rstart+1-y, len);

        // 填充空隙
        for (int i=rstart+1-y+len; i < rstart+1; i++)
            result[i] = 0;

        value = result;
        intLen = resultLen;
        offset = result.length - resultLen;
    }

    /**
     * 添加 {@code addend} 的低 {@code n} 个 int。
     */
    void addLower(MutableBigInteger addend, int n) {
        MutableBigInteger a = new MutableBigInteger(addend);
        if (a.offset + a.intLen >= n) {
            a.offset = a.offset + a.intLen - n;
            a.intLen = n;
        }
        a.normalize();
        add(a);
    }

    /**
     * 从较大数中减去较小数，并将结果放入此 MutableBigInteger 中。
     */
    int subtract(MutableBigInteger b) {
        MutableBigInteger a = this;

        int[] result = value;
        int sign = a.compare(b);

        if (sign == 0) {
            reset();
            return 0;
        }
        if (sign < 0) {
            MutableBigInteger tmp = a;
            a = b;
            b = tmp;
        }

        int resultLen = a.intLen;
        if (result.length < resultLen)
            result = new int[resultLen];

        long diff = 0;
        int x = a.intLen;
        int y = b.intLen;
        int rstart = result.length - 1;

        // 减去两个数的公共部分
        while (y > 0) {
            x--; y--;

            diff = (a.value[x+a.offset] & LONG_MASK) -
                   (b.value[y+b.offset] & LONG_MASK) - ((int)-(diff>>32));
            result[rstart--] = (int)diff;
        }
        // 减去较长数的剩余部分
        while (x > 0) {
            x--;
            diff = (a.value[x+a.offset] & LONG_MASK) - ((int)-(diff>>32));
            result[rstart--] = (int)diff;
        }

        value = result;
        intLen = resultLen;
        offset = value.length - resultLen;
        normalize();
        return sign;
    }

    /**
     * 从较大数中减去较小数，并将结果放入较大数中。如果结果在 a 中返回 1，如果在 b 中返回 -1，如果未执行操作返回 0。
     */
    private int difference(MutableBigInteger b) {
        MutableBigInteger a = this;
        int sign = a.compare(b);
        if (sign == 0)
            return 0;
        if (sign < 0) {
            MutableBigInteger tmp = a;
            a = b;
            b = tmp;
        }

        long diff = 0;
        int x = a.intLen;
        int y = b.intLen;

        // 减去两个数的公共部分
        while (y > 0) {
            x--; y--;
            diff = (a.value[a.offset+ x] & LONG_MASK) -
                (b.value[b.offset+ y] & LONG_MASK) - ((int)-(diff>>32));
            a.value[a.offset+x] = (int)diff;
        }
        // 减去较长数的剩余部分
        while (x > 0) {
            x--;
            diff = (a.value[a.offset+ x] & LONG_MASK) - ((int)-(diff>>32));
            a.value[a.offset+x] = (int)diff;
        }

        a.normalize();
        return sign;
    }

    /**
     * 乘以两个 MutableBigInteger 对象的内容。结果放入 MutableBigInteger z 中。y 的内容不变。
     */
    void multiply(MutableBigInteger y, MutableBigInteger z) {
        int xLen = intLen;
        int yLen = y.intLen;
        int newLen = xLen + yLen;

        // 将 z 置于接收乘积的适当状态
        if (z.value.length < newLen)
            z.value = new int[newLen];
        z.offset = 0;
        z.intLen = newLen;

        // 第一次迭代从循环中提取以避免额外的加法
        long carry = 0;
        for (int j=yLen-1, k=yLen+xLen-1; j >= 0; j--, k--) {
                long product = (y.value[j+y.offset] & LONG_MASK) *
                               (value[xLen-1+offset] & LONG_MASK) + carry;
                z.value[k] = (int)product;
                carry = product >>> 32;
        }
        z.value[xLen-1] = (int)carry;

        // 按字逐个执行乘法
        for (int i = xLen-2; i >= 0; i--) {
            carry = 0;
            for (int j=yLen-1, k=yLen+i; j >= 0; j--, k--) {
                long product = (y.value[j+y.offset] & LONG_MASK) *
                               (value[i+offset] & LONG_MASK) +
                               (z.value[k] & LONG_MASK) + carry;
                z.value[k] = (int)product;
                carry = product >>> 32;
            }
            z.value[i] = (int)carry;
        }

        // 从乘积中移除前导零
        z.normalize();
    }

    /**
     * 将此 MutableBigInteger 的内容乘以 y。结果放入 z 中。
     */
    void mul(int y, MutableBigInteger z) {
        if (y == 1) {
            z.copyValue(this);
            return;
        }


                    if (y == 0) {
            z.clear();
            return;
        }

        // 执行逐字乘法
        long ylong = y & LONG_MASK;
        int[] zval = (z.value.length < intLen+1 ? new int[intLen + 1]
                                              : z.value);
        long carry = 0;
        for (int i = intLen-1; i >= 0; i--) {
            long product = ylong * (value[i+offset] & LONG_MASK) + carry;
            zval[i+1] = (int)product;
            carry = product >>> 32;
        }

        if (carry == 0) {
            z.offset = 1;
            z.intLen = intLen;
        } else {
            z.offset = 0;
            z.intLen = intLen + 1;
            zval[0] = (int)carry;
        }
        z.value = zval;
    }

     /**
     * 该方法用于 n 字长被除数除以一个字长除数。商被放置在 quotient 中。一个字长除数由 divisor 指定。
     *
     * @return 返回除法的余数。
     *
     */
    int divideOneWord(int divisor, MutableBigInteger quotient) {
        long divisorLong = divisor & LONG_MASK;

        // 一个字长被除数的特殊情况
        if (intLen == 1) {
            long dividendValue = value[offset] & LONG_MASK;
            int q = (int) (dividendValue / divisorLong);
            int r = (int) (dividendValue - q * divisorLong);
            quotient.value[0] = q;
            quotient.intLen = (q == 0) ? 0 : 1;
            quotient.offset = 0;
            return r;
        }

        if (quotient.value.length < intLen)
            quotient.value = new int[intLen];
        quotient.offset = 0;
        quotient.intLen = intLen;

        // 归一化除数
        int shift = Integer.numberOfLeadingZeros(divisor);

        int rem = value[offset];
        long remLong = rem & LONG_MASK;
        if (remLong < divisorLong) {
            quotient.value[0] = 0;
        } else {
            quotient.value[0] = (int)(remLong / divisorLong);
            rem = (int) (remLong - (quotient.value[0] * divisorLong));
            remLong = rem & LONG_MASK;
        }
        int xlen = intLen;
        while (--xlen > 0) {
            long dividendEstimate = (remLong << 32) |
                    (value[offset + intLen - xlen] & LONG_MASK);
            int q;
            if (dividendEstimate >= 0) {
                q = (int) (dividendEstimate / divisorLong);
                rem = (int) (dividendEstimate - q * divisorLong);
            } else {
                long tmp = divWord(dividendEstimate, divisor);
                q = (int) (tmp & LONG_MASK);
                rem = (int) (tmp >>> 32);
            }
            quotient.value[intLen - xlen] = q;
            remLong = rem & LONG_MASK;
        }

        quotient.normalize();
        // 反归一化
        if (shift > 0)
            return rem % divisor;
        else
            return rem;
    }

    /**
     * 计算 this 除以 b 的商，并将商放置在提供的 MutableBigInteger 对象中，返回余数对象。
     *
     */
    MutableBigInteger divide(MutableBigInteger b, MutableBigInteger quotient) {
        return divide(b,quotient,true);
    }

    MutableBigInteger divide(MutableBigInteger b, MutableBigInteger quotient, boolean needRemainder) {
        if (b.intLen < BigInteger.BURNIKEL_ZIEGLER_THRESHOLD ||
                intLen - b.intLen < BigInteger.BURNIKEL_ZIEGLER_OFFSET) {
            return divideKnuth(b, quotient, needRemainder);
        } else {
            return divideAndRemainderBurnikelZiegler(b, quotient);
        }
    }

    /**
     * @see #divideKnuth(MutableBigInteger, MutableBigInteger, boolean)
     */
    MutableBigInteger divideKnuth(MutableBigInteger b, MutableBigInteger quotient) {
        return divideKnuth(b,quotient,true);
    }

    /**
     * 计算 this 除以 b 的商，并将商放置在提供的 MutableBigInteger 对象中，返回余数对象。
     *
     * 使用 Knuth 第 4.3.1 节中的算法 D。
     * 该算法的许多优化来自 Colin Plumb C 库。
     * 特殊处理一个字长的除数以提高速度。b 的内容不会改变。
     *
     */
    MutableBigInteger divideKnuth(MutableBigInteger b, MutableBigInteger quotient, boolean needRemainder) {
        if (b.intLen == 0)
            throw new ArithmeticException("BigInteger divide by zero");

        // 被除数为零
        if (intLen == 0) {
            quotient.intLen = quotient.offset = 0;
            return needRemainder ? new MutableBigInteger() : null;
        }

        int cmp = compare(b);
        // 被除数小于除数
        if (cmp < 0) {
            quotient.intLen = quotient.offset = 0;
            return needRemainder ? new MutableBigInteger(this) : null;
        }
        // 被除数等于除数
        if (cmp == 0) {
            quotient.value[0] = quotient.intLen = 1;
            quotient.offset = 0;
            return needRemainder ? new MutableBigInteger() : null;
        }

        quotient.clear();
        // 特殊处理一个字长的除数
        if (b.intLen == 1) {
            int r = divideOneWord(b.value[b.offset], quotient);
            if(needRemainder) {
                if (r == 0)
                    return new MutableBigInteger();
                return new MutableBigInteger(r);
            } else {
                return null;
            }
        }

        // 如果超过 KNUTH_POW2_* 阈值，则取消共同的 2 的幂
        if (intLen >= KNUTH_POW2_THRESH_LEN) {
            int trailingZeroBits = Math.min(getLowestSetBit(), b.getLowestSetBit());
            if (trailingZeroBits >= KNUTH_POW2_THRESH_ZEROS*32) {
                MutableBigInteger a = new MutableBigInteger(this);
                b = new MutableBigInteger(b);
                a.rightShift(trailingZeroBits);
                b.rightShift(trailingZeroBits);
                MutableBigInteger r = a.divideKnuth(b, quotient);
                r.leftShift(trailingZeroBits);
                return r;
            }
        }


                    return divideMagnitude(b, quotient, needRemainder);
    }

    /**
     * 使用 <a href="http://cr.yp.to/bib/1998/burnikel.ps"> Burnikel-Ziegler 算法</a> 计算 {@code this/b} 和 {@code this%b}。
     * 该方法实现了 Burnikel-Ziegler 论文第 9 页的算法 3。
     * 参数 beta 被选择为 2<sup>32</sup>，因此几乎所有移位都是 32 位的倍数。<br/>
     * {@code this} 和 {@code b} 必须是非负数。
     * @param b 除数
     * @param quotient 输出参数，用于存储 {@code this/b} 的结果
     * @return 余数
     */
    MutableBigInteger divideAndRemainderBurnikelZiegler(MutableBigInteger b, MutableBigInteger quotient) {
        int r = intLen;
        int s = b.intLen;

        // 清除商
        quotient.offset = quotient.intLen = 0;

        if (r < s) {
            return this;
        } else {
            // 与 Knuth 除法不同，我们在这里不检查共同的 2 的幂，因为如果两个数都包含 2 的幂，BZ 已经运行得更快，取消它们没有额外的好处。

            // 步骤 1: 令 m = min{2^k | (2^k)*BURNIKEL_ZIEGLER_THRESHOLD > s}
            int m = 1 << (32-Integer.numberOfLeadingZeros(s/BigInteger.BURNIKEL_ZIEGLER_THRESHOLD));

            int j = (s+m-1) / m;      // 步骤 2a: j = ceil(s/m)
            int n = j * m;            // 步骤 2b: 块长度为 32 位单位
            long n32 = 32L * n;         // 块长度为位
            int sigma = (int) Math.max(0, n32 - b.bitLength());   // 步骤 3: sigma = max{T | (2^T)*B < beta^n}
            MutableBigInteger bShifted = new MutableBigInteger(b);
            bShifted.safeLeftShift(sigma);   // 步骤 4a: 移位 b 使其长度为 n 的倍数
            MutableBigInteger aShifted = new MutableBigInteger (this);
            aShifted.safeLeftShift(sigma);     // 步骤 4b: 以相同数量移位 a

            // 步骤 5: t 是容纳 a 加上一个额外位所需的块数
            int t = (int) ((aShifted.bitLength()+n32) / n32);
            if (t < 2) {
                t = 2;
            }

            // 步骤 6: 概念上将 a 分成块 a[t-1], ..., a[0]
            MutableBigInteger a1 = aShifted.getBlock(t-1, t, n);   // a 的最高有效块

            // 步骤 7: z[t-2] = [a[t-1], a[t-2]]
            MutableBigInteger z = aShifted.getBlock(t-2, t, n);    // 第二个最高有效块
            z.addDisjoint(a1, n);   // z[t-2]

            // 对块进行学校书式除法，将 2 块数字除以 1 块数字
            MutableBigInteger qi = new MutableBigInteger();
            MutableBigInteger ri;
            for (int i=t-2; i > 0; i--) {
                // 步骤 8a: 计算 (qi,ri) 使得 z=b*qi+ri
                ri = z.divide2n1n(bShifted, qi);

                // 步骤 8b: z = [ri, a[i-1]]
                z = aShifted.getBlock(i-1, t, n);   // a[i-1]
                z.addDisjoint(ri, n);
                quotient.addShifted(qi, i*n);   // 更新 q（步骤 9 的一部分）
            }
            // 步骤 8 的最终迭代：再为 i=0 进行一次循环，但保持 z 不变
            ri = z.divide2n1n(bShifted, qi);
            quotient.add(qi);

            ri.rightShift(sigma);   // 步骤 9: a 和 b 被移位，所以移回去
            return ri;
        }
    }

    /**
     * 该方法实现了 Burnikel-Ziegler 论文第 4 页的算法 1。
     * 它将一个 2n 位数除以一个 n 位数。<br/>
     * 参数 beta 是 2<sup>32</sup>，因此所有移位都是 32 位的倍数。
     * <br/>
     * {@code this} 必须是非负数，且 {@code this.bitLength() <= 2*b.bitLength()}
     * @param b 一个正数，其 {@code b.bitLength()} 是偶数
     * @param quotient 输出参数，用于存储 {@code this/b} 的结果
     * @return {@code this%b}
     */
    private MutableBigInteger divide2n1n(MutableBigInteger b, MutableBigInteger quotient) {
        int n = b.intLen;

        // 步骤 1: 基本情况
        if (n%2 != 0 || n < BigInteger.BURNIKEL_ZIEGLER_THRESHOLD) {
            return divideKnuth(b, quotient);
        }

        // 步骤 2: 将 this 视为 [a1,a2,a3,a4]，其中每个 ai 是 n/2 个整数或更少
        MutableBigInteger aUpper = new MutableBigInteger(this);
        aUpper.safeRightShift(32*(n/2));   // aUpper = [a1,a2,a3]
        keepLower(n/2);   // this = a4

        // 步骤 3: q1=aUpper/b, r1=aUpper%b
        MutableBigInteger q1 = new MutableBigInteger();
        MutableBigInteger r1 = aUpper.divide3n2n(b, q1);

        // 步骤 4: quotient=[r1,this]/b, r2=[r1,this]%b
        addDisjoint(r1, n/2);   // this = [r1,this]
        MutableBigInteger r2 = divide3n2n(b, quotient);

        // 步骤 5: 令 quotient=[q1,quotient] 并返回 r2
        quotient.addDisjoint(q1, n/2);
        return r2;
    }

    /**
     * 该方法实现了 Burnikel-Ziegler 论文第 5 页的算法 2。
     * 它将一个 3n 位数除以一个 2n 位数。<br/>
     * 参数 beta 是 2<sup>32</sup>，因此所有移位都是 32 位的倍数。<br/>
     * <br/>
     * {@code this} 必须是非负数，且 {@code 2*this.bitLength() <= 3*b.bitLength()}
     * @param quotient 输出参数，用于存储 {@code this/b} 的结果
     * @return {@code this%b}
     */
    private MutableBigInteger divide3n2n(MutableBigInteger b, MutableBigInteger quotient) {
        int n = b.intLen / 2;   // b 的一半长度，以整数为单位

        // 步骤 1: 将 this 视为 [a1,a2,a3]，其中每个 ai 是 n 个整数或更少；令 a12=[a1,a2]
        MutableBigInteger a12 = new MutableBigInteger(this);
        a12.safeRightShift(32*n);

        // 步骤 2: 将 b 视为 [b1,b2]，其中每个 bi 是 n 个整数或更少
        MutableBigInteger b1 = new MutableBigInteger(b);
        b1.safeRightShift(n * 32);
        BigInteger b2 = b.getLower(n);

        MutableBigInteger r;
        MutableBigInteger d;
        if (compareShifted(b, n) < 0) {
            // 步骤 3a: 如果 a1<b1，令 quotient=a12/b1 和 r=a12%b1
            r = a12.divide2n1n(b1, quotient);

            // 步骤 4: d=quotient*b2
            d = new MutableBigInteger(quotient.toBigInteger().multiply(b2));
        } else {
            // 步骤 3b: 如果 a1>=b1，令 quotient=beta^n-1 和 r=a12-b1*2^n+b1
            quotient.ones(n);
            a12.add(b1);
            b1.leftShift(32*n);
            a12.subtract(b1);
            r = a12;


                        // 第 4 步: d=商*b2=(b2 << 32*n) - b2
            d = new MutableBigInteger(b2);
            d.leftShift(32 * n);
            d.subtract(new MutableBigInteger(b2));
        }

        // 第 5 步: r = r*beta^n + a3 - d (论文中说是 a4)
        // 但是，不要在 while 循环之前减去 d，以防止 r 变为负数
        r.leftShift(32 * n);
        r.addLower(this, n);

        // 第 6 步: 加上 b 直到 r>=d
        while (r.compare(d) < 0) {
            r.add(b);
            quotient.subtract(MutableBigInteger.ONE);
        }
        r.subtract(d);

        return r;
    }

    /**
     * 返回一个包含 {@code blockLength} 个 int 的 {@code MutableBigInteger}，从
     * {@code this} 数字的 {@code index*blockLength} 位置开始。<br/>
     * 用于 Burnikel-Ziegler 除法。
     * @param index 块索引
     * @param numBlocks {@code this} 数字中的块总数
     * @param blockLength 一个块的长度，以 32 位为单位
     * @return
     */
    private MutableBigInteger getBlock(int index, int numBlocks, int blockLength) {
        int blockStart = index * blockLength;
        if (blockStart >= intLen) {
            return new MutableBigInteger();
        }

        int blockEnd;
        if (index == numBlocks-1) {
            blockEnd = intLen;
        } else {
            blockEnd = (index+1) * blockLength;
        }
        if (blockEnd > intLen) {
            return new MutableBigInteger();
        }

        int[] newVal = Arrays.copyOfRange(value, offset+intLen-blockEnd, offset+intLen-blockStart);
        return new MutableBigInteger(newVal);
    }

    /** @see BigInteger#bitLength() */
    long bitLength() {
        if (intLen == 0)
            return 0;
        return intLen*32L - Integer.numberOfLeadingZeros(value[offset]);
    }

    /**
     * 内部用于计算 this 除以 v 的商，并将商放置在提供的 MutableBigInteger 对象中，余数被返回。
     *
     * @return 返回除法的余数。
     */
    long divide(long v, MutableBigInteger quotient) {
        if (v == 0)
            throw new ArithmeticException("BigInteger 除以零");

        // 被除数为零
        if (intLen == 0) {
            quotient.intLen = quotient.offset = 0;
            return 0;
        }
        if (v < 0)
            v = -v;

        int d = (int)(v >>> 32);
        quotient.clear();
        // 单词除数的特殊情况
        if (d == 0)
            return divideOneWord((int)v, quotient) & LONG_MASK;
        else {
            return divideLongMagnitude(v, quotient).toLong();
        }
    }

    private static void copyAndShift(int[] src, int srcFrom, int srcLen, int[] dst, int dstFrom, int shift) {
        int n2 = 32 - shift;
        int c=src[srcFrom];
        for (int i=0; i < srcLen-1; i++) {
            int b = c;
            c = src[++srcFrom];
            dst[dstFrom+i] = (b << shift) | (c >>> n2);
        }
        dst[dstFrom+srcLen-1] = c << shift;
    }

    /**
     * 用除数除以这个 MutableBigInteger。
     * 商将被放置在提供的商对象中，余数对象被返回。
     */
    private MutableBigInteger divideMagnitude(MutableBigInteger div,
                                              MutableBigInteger quotient,
                                              boolean needRemainder ) {
        // 断言 div.intLen > 1
        // D1 归一化除数
        int shift = Integer.numberOfLeadingZeros(div.value[div.offset]);
        // 复制除数值以保护除数
        final int dlen = div.intLen;
        int[] divisor;
        MutableBigInteger rem; // 余数从被除数开始，带有一个前导零的空间
        if (shift > 0) {
            divisor = new int[dlen];
            copyAndShift(div.value,div.offset,dlen,divisor,0,shift);
            if (Integer.numberOfLeadingZeros(value[offset]) >= shift) {
                int[] remarr = new int[intLen + 1];
                rem = new MutableBigInteger(remarr);
                rem.intLen = intLen;
                rem.offset = 1;
                copyAndShift(value,offset,intLen,remarr,1,shift);
            } else {
                int[] remarr = new int[intLen + 2];
                rem = new MutableBigInteger(remarr);
                rem.intLen = intLen+1;
                rem.offset = 1;
                int rFrom = offset;
                int c=0;
                int n2 = 32 - shift;
                for (int i=1; i < intLen+1; i++,rFrom++) {
                    int b = c;
                    c = value[rFrom];
                    remarr[i] = (b << shift) | (c >>> n2);
                }
                remarr[intLen+1] = c << shift;
            }
        } else {
            divisor = Arrays.copyOfRange(div.value, div.offset, div.offset + div.intLen);
            rem = new MutableBigInteger(new int[intLen + 1]);
            System.arraycopy(value, offset, rem.value, 1, intLen);
            rem.intLen = intLen;
            rem.offset = 1;
        }

        int nlen = rem.intLen;

        // 设置商的大小
        final int limit = nlen - dlen + 1;
        if (quotient.value.length < limit) {
            quotient.value = new int[limit];
            quotient.offset = 0;
        }
        quotient.intLen = limit;
        int[] q = quotient.value;


        // 如果 rem 的长度没有改变，必须在 rem 中插入前导 0
        if (rem.intLen == nlen) {
            rem.offset = 0;
            rem.value[0] = 0;
            rem.intLen++;
        }

        int dh = divisor[0];
        long dhLong = dh & LONG_MASK;
        int dl = divisor[1];

        // D2 初始化 j
        for (int j=0; j < limit-1; j++) {
            // D3 计算 qhat
            // 估计 qhat
            int qhat = 0;
            int qrem = 0;
            boolean skipCorrection = false;
            int nh = rem.value[j+rem.offset];
            int nh2 = nh + 0x80000000;
            int nm = rem.value[j+1+rem.offset];

            if (nh == dh) {
                qhat = ~0;
                qrem = nh + nm;
                skipCorrection = qrem + 0x80000000 < nh2;
            } else {
                long nChunk = (((long)nh) << 32) | (nm & LONG_MASK);
                if (nChunk >= 0) {
                    qhat = (int) (nChunk / dhLong);
                    qrem = (int) (nChunk - (qhat * dhLong));
                } else {
                    long tmp = divWord(nChunk, dh);
                    qhat = (int) (tmp & LONG_MASK);
                    qrem = (int) (tmp >>> 32);
                }
            }


                        if (qhat == 0)
                continue;

            if (!skipCorrection) { // 修正 qhat
                long nl = rem.value[j+2+rem.offset] & LONG_MASK;
                long rs = ((qrem & LONG_MASK) << 32) | nl;
                long estProduct = (dl & LONG_MASK) * (qhat & LONG_MASK);

                if (unsignedLongCompare(estProduct, rs)) {
                    qhat--;
                    qrem = (int)((qrem & LONG_MASK) + dhLong);
                    if ((qrem & LONG_MASK) >=  dhLong) {
                        estProduct -= (dl & LONG_MASK);
                        rs = ((qrem & LONG_MASK) << 32) | nl;
                        if (unsignedLongCompare(estProduct, rs))
                            qhat--;
                    }
                }
            }

            // D4 乘法和减法
            rem.value[j+rem.offset] = 0;
            int borrow = mulsub(rem.value, divisor, qhat, dlen, j+rem.offset);

            // D5 测试余数
            if (borrow + 0x80000000 > nh2) {
                // D6 加回
                divadd(divisor, rem.value, j+1+rem.offset);
                qhat--;
            }

            // 存储商的数字
            q[j] = qhat;
        } // D7 j 的循环
        // D3 计算 qhat
        // 估计 qhat
        int qhat = 0;
        int qrem = 0;
        boolean skipCorrection = false;
        int nh = rem.value[limit - 1 + rem.offset];
        int nh2 = nh + 0x80000000;
        int nm = rem.value[limit + rem.offset];

        if (nh == dh) {
            qhat = ~0;
            qrem = nh + nm;
            skipCorrection = qrem + 0x80000000 < nh2;
        } else {
            long nChunk = (((long) nh) << 32) | (nm & LONG_MASK);
            if (nChunk >= 0) {
                qhat = (int) (nChunk / dhLong);
                qrem = (int) (nChunk - (qhat * dhLong));
            } else {
                long tmp = divWord(nChunk, dh);
                qhat = (int) (tmp & LONG_MASK);
                qrem = (int) (tmp >>> 32);
            }
        }
        if (qhat != 0) {
            if (!skipCorrection) { // 修正 qhat
                long nl = rem.value[limit + 1 + rem.offset] & LONG_MASK;
                long rs = ((qrem & LONG_MASK) << 32) | nl;
                long estProduct = (dl & LONG_MASK) * (qhat & LONG_MASK);

                if (unsignedLongCompare(estProduct, rs)) {
                    qhat--;
                    qrem = (int) ((qrem & LONG_MASK) + dhLong);
                    if ((qrem & LONG_MASK) >= dhLong) {
                        estProduct -= (dl & LONG_MASK);
                        rs = ((qrem & LONG_MASK) << 32) | nl;
                        if (unsignedLongCompare(estProduct, rs))
                            qhat--;
                    }
                }
            }


            // D4 乘法和减法
            int borrow;
            rem.value[limit - 1 + rem.offset] = 0;
            if(needRemainder)
                borrow = mulsub(rem.value, divisor, qhat, dlen, limit - 1 + rem.offset);
            else
                borrow = mulsubBorrow(rem.value, divisor, qhat, dlen, limit - 1 + rem.offset);

            // D5 测试余数
            if (borrow + 0x80000000 > nh2) {
                // D6 加回
                if(needRemainder)
                    divadd(divisor, rem.value, limit - 1 + 1 + rem.offset);
                qhat--;
            }

            // 存储商的数字
            q[(limit - 1)] = qhat;
        }


        if (needRemainder) {
            // D8 反归一化
            if (shift > 0)
                rem.rightShift(shift);
            rem.normalize();
        }
        quotient.normalize();
        return needRemainder ? rem : null;
    }

    /**
     * 用正的 long 值表示的除数除以这个 MutableBigInteger。商将被放置到提供的商对象中，余数对象将被返回。
     */
    private MutableBigInteger divideLongMagnitude(long ldivisor, MutableBigInteger quotient) {
        // 余数开始为被除数，带有一个前导零的空间
        MutableBigInteger rem = new MutableBigInteger(new int[intLen + 1]);
        System.arraycopy(value, offset, rem.value, 1, intLen);
        rem.intLen = intLen;
        rem.offset = 1;

        int nlen = rem.intLen;

        int limit = nlen - 2 + 1;
        if (quotient.value.length < limit) {
            quotient.value = new int[limit];
            quotient.offset = 0;
        }
        quotient.intLen = limit;
        int[] q = quotient.value;

        // D1 归一化除数
        int shift = Long.numberOfLeadingZeros(ldivisor);
        if (shift > 0) {
            ldivisor<<=shift;
            rem.leftShift(shift);
        }

        // 如果 rem 的长度没有改变，必须在 rem 中插入前导 0
        if (rem.intLen == nlen) {
            rem.offset = 0;
            rem.value[0] = 0;
            rem.intLen++;
        }

        int dh = (int)(ldivisor >>> 32);
        long dhLong = dh & LONG_MASK;
        int dl = (int)(ldivisor & LONG_MASK);

        // D2 初始化 j
        for (int j = 0; j < limit; j++) {
            // D3 计算 qhat
            // 估计 qhat
            int qhat = 0;
            int qrem = 0;
            boolean skipCorrection = false;
            int nh = rem.value[j + rem.offset];
            int nh2 = nh + 0x80000000;
            int nm = rem.value[j + 1 + rem.offset];

            if (nh == dh) {
                qhat = ~0;
                qrem = nh + nm;
                skipCorrection = qrem + 0x80000000 < nh2;
            } else {
                long nChunk = (((long) nh) << 32) | (nm & LONG_MASK);
                if (nChunk >= 0) {
                    qhat = (int) (nChunk / dhLong);
                    qrem = (int) (nChunk - (qhat * dhLong));
                } else {
                    long tmp = divWord(nChunk, dh);
                    qhat =(int)(tmp & LONG_MASK);
                    qrem = (int)(tmp>>>32);
                }
            }

            if (qhat == 0)
                continue;

            if (!skipCorrection) { // 修正 qhat
                long nl = rem.value[j + 2 + rem.offset] & LONG_MASK;
                long rs = ((qrem & LONG_MASK) << 32) | nl;
                long estProduct = (dl & LONG_MASK) * (qhat & LONG_MASK);


                            if (unsignedLongCompare(estProduct, rs)) {
                    qhat--;
                    qrem = (int) ((qrem & LONG_MASK) + dhLong);
                    if ((qrem & LONG_MASK) >= dhLong) {
                        estProduct -= (dl & LONG_MASK);
                        rs = ((qrem & LONG_MASK) << 32) | nl;
                        if (unsignedLongCompare(estProduct, rs))
                            qhat--;
                    }
                }
            }

            // D4 乘法和减法
            rem.value[j + rem.offset] = 0;
            int borrow = mulsubLong(rem.value, dh, dl, qhat,  j + rem.offset);

            // D5 测试余数
            if (borrow + 0x80000000 > nh2) {
                // D6 加回
                divaddLong(dh,dl, rem.value, j + 1 + rem.offset);
                qhat--;
            }

            // 存储商的位
            q[j] = qhat;
        } // D7 j 的循环

        // D8 反归一化
        if (shift > 0)
            rem.rightShift(shift);

        quotient.normalize();
        rem.normalize();
        return rem;
    }

    /**
     * 用于长除法的基本方法。
     * divadd 方法的专用版本。
     * dh 是除数的高部分，dl 是低部分
     */
    private int divaddLong(int dh, int dl, int[] result, int offset) {
        long carry = 0;

        long sum = (dl & LONG_MASK) + (result[1+offset] & LONG_MASK);
        result[1+offset] = (int)sum;

        sum = (dh & LONG_MASK) + (result[offset] & LONG_MASK) + carry;
        result[offset] = (int)sum;
        carry = sum >>> 32;
        return (int)carry;
    }

    /**
     * 用于长除法的方法。
     * mulsub 方法的专用版本。
     * dh 是除数的高部分，dl 是低部分
     */
    private int mulsubLong(int[] q, int dh, int dl, int x, int offset) {
        long xLong = x & LONG_MASK;
        offset += 2;
        long product = (dl & LONG_MASK) * xLong;
        long difference = q[offset] - product;
        q[offset--] = (int)difference;
        long carry = (product >>> 32)
                 + (((difference & LONG_MASK) >
                     (((~(int)product) & LONG_MASK))) ? 1:0);
        product = (dh & LONG_MASK) * xLong + carry;
        difference = q[offset] - product;
        q[offset--] = (int)difference;
        carry = (product >>> 32)
                 + (((difference & LONG_MASK) >
                     (((~(int)product) & LONG_MASK))) ? 1:0);
        return (int)carry;
    }

    /**
     * 将两个长整数视为无符号进行比较。
     * 如果一个大于两个则返回 true。
     */
    private boolean unsignedLongCompare(long one, long two) {
        return (one+Long.MIN_VALUE) > (two+Long.MIN_VALUE);
    }

    /**
     * 该方法将一个长整数除以一个整数来估计两个多精度数的 qhat。
     * 当 n 的有符号值小于零时使用。
     * 返回一个长整数值，其中高 32 位包含余数值，低 32 位包含商值。
     */
    static long divWord(long n, int d) {
        long dLong = d & LONG_MASK;
        long r;
        long q;
        if (dLong == 1) {
            q = (int)n;
            r = 0;
            return (r << 32) | (q & LONG_MASK);
        }

        // 估计商和余数
        q = (n >>> 1) / (dLong >>> 1);
        r = n - q*dLong;

        // 修正估计值
        while (r < 0) {
            r += dLong;
            q--;
        }
        while (r >= dLong) {
            r -= dLong;
            q++;
        }
        // n - q*dlong == r && 0 <= r <dLong, 因此已完成。
        return (r << 32) | (q & LONG_MASK);
    }

    /**
     * 计算 this 和 b 的最大公约数。计算过程中会改变 this 和 b。
     */
    MutableBigInteger hybridGCD(MutableBigInteger b) {
        // 使用欧几里得算法直到数字长度大致相同，然后使用二进制 GCD 算法来找到最大公约数。
        MutableBigInteger a = this;
        MutableBigInteger q = new MutableBigInteger();

        while (b.intLen != 0) {
            if (Math.abs(a.intLen - b.intLen) < 2)
                return a.binaryGCD(b);

            MutableBigInteger r = a.divide(b, q);
            a = b;
            b = r;
        }
        return a;
    }

    /**
     * 计算 this 和 v 的最大公约数。
     * 假设 this 和 v 都不为零。
     */
    private MutableBigInteger binaryGCD(MutableBigInteger v) {
        // Knuth 第 4.5.2 节的算法 B
        MutableBigInteger u = this;
        MutableBigInteger r = new MutableBigInteger();

        // 步骤 B1
        int s1 = u.getLowestSetBit();
        int s2 = v.getLowestSetBit();
        int k = (s1 < s2) ? s1 : s2;
        if (k != 0) {
            u.rightShift(k);
            v.rightShift(k);
        }

        // 步骤 B2
        boolean uOdd = (k == s1);
        MutableBigInteger t = uOdd ? v: u;
        int tsign = uOdd ? -1 : 1;

        int lb;
        while ((lb = t.getLowestSetBit()) >= 0) {
            // 步骤 B3 和 B4
            t.rightShift(lb);
            // 步骤 B5
            if (tsign > 0)
                u = t;
            else
                v = t;

            // 特殊情况：单字数
            if (u.intLen < 2 && v.intLen < 2) {
                int x = u.value[u.offset];
                int y = v.value[v.offset];
                x  = binaryGcd(x, y);
                r.value[0] = x;
                r.intLen = 1;
                r.offset = 0;
                if (k > 0)
                    r.leftShift(k);
                return r;
            }

            // 步骤 B6
            if ((tsign = u.difference(v)) == 0)
                break;
            t = (tsign >= 0) ? u : v;
        }

        if (k > 0)
            u.leftShift(k);
        return u;
    }

    /**
     * 计算 a 和 b 解释为无符号整数的最大公约数。
     */
    static int binaryGcd(int a, int b) {
        if (b == 0)
            return a;
        if (a == 0)
            return b;

        // 右移 a 和 b 直到它们的最低位为 1。
        int aZeros = Integer.numberOfTrailingZeros(a);
        int bZeros = Integer.numberOfTrailingZeros(b);
        a >>>= aZeros;
        b >>>= bZeros;


                    int t = (aZeros < bZeros ? aZeros : bZeros);

        while (a != b) {
            if ((a+0x80000000) > (b+0x80000000)) {  // a > b as unsigned
                a -= b;
                a >>>= Integer.numberOfTrailingZeros(a);
            } else {
                b -= a;
                b >>>= Integer.numberOfTrailingZeros(b);
            }
        }
        return a<<t;
    }

    /**
     * 返回此数模 p 的模逆。此数和 p 在操作过程中不会被改变。
     */
    MutableBigInteger mutableModInverse(MutableBigInteger p) {
        // 模数是奇数，使用 Schroeppel 的算法
        if (p.isOdd())
            return modInverse(p);

        // 基数和模数都是偶数，抛出异常
        if (isEven())
            throw new ArithmeticException("BigInteger 不可逆。");

        // 获取模数的偶数部分，表示为 2 的幂
        int powersOf2 = p.getLowestSetBit();

        // 构造模数的奇数部分
        MutableBigInteger oddMod = new MutableBigInteger(p);
        oddMod.rightShift(powersOf2);

        if (oddMod.isOne())
            return modInverseMP2(powersOf2);

        // 计算 1/a 模 oddMod
        MutableBigInteger oddPart = modInverse(oddMod);

        // 计算 1/a 模 evenMod
        MutableBigInteger evenPart = modInverseMP2(powersOf2);

        // 使用中国剩余定理组合结果
        MutableBigInteger y1 = modInverseBP2(oddMod, powersOf2);
        MutableBigInteger y2 = oddMod.modInverseMP2(powersOf2);

        MutableBigInteger temp1 = new MutableBigInteger();
        MutableBigInteger temp2 = new MutableBigInteger();
        MutableBigInteger result = new MutableBigInteger();

        oddPart.leftShift(powersOf2);
        oddPart.multiply(y1, result);

        evenPart.multiply(oddMod, temp1);
        temp1.multiply(y2, temp2);

        result.add(temp2);
        return result.divide(p, temp1);
    }

    /*
     * 计算此数模 2^k 的乘法逆元。
     */
    MutableBigInteger modInverseMP2(int k) {
        if (isEven())
            throw new ArithmeticException("不可逆。 (GCD != 1)");

        if (k > 64)
            return euclidModInverse(k);

        int t = inverseMod32(value[offset+intLen-1]);

        if (k < 33) {
            t = (k == 32 ? t : t & ((1 << k) - 1));
            return new MutableBigInteger(t);
        }

        long pLong = (value[offset+intLen-1] & LONG_MASK);
        if (intLen > 1)
            pLong |=  ((long)value[offset+intLen-2] << 32);
        long tLong = t & LONG_MASK;
        tLong = tLong * (2 - pLong * tLong);  // 1 more Newton iter step
        tLong = (k == 64 ? tLong : tLong & ((1L << k) - 1));

        MutableBigInteger result = new MutableBigInteger(new int[2]);
        result.value[0] = (int)(tLong >>> 32);
        result.value[1] = (int)tLong;
        result.intLen = 2;
        result.normalize();
        return result;
    }

    /**
     * 返回 val 模 2^32 的乘法逆元。假设 val 是奇数。
     */
    static int inverseMod32(int val) {
        // Newton 迭代法！
        int t = val;
        t *= 2 - val*t;
        t *= 2 - val*t;
        t *= 2 - val*t;
        t *= 2 - val*t;
        return t;
    }

    /**
     * 返回 val 模 2^64 的乘法逆元。假设 val 是奇数。
     */
    static long inverseMod64(long val) {
        // Newton 迭代法！
        long t = val;
        t *= 2 - val*t;
        t *= 2 - val*t;
        t *= 2 - val*t;
        t *= 2 - val*t;
        t *= 2 - val*t;
        assert(t * val == 1);
        return t;
    }

    /**
     * 计算 2^k 模 mod 的乘法逆元，其中 mod 是奇数。
     */
    static MutableBigInteger modInverseBP2(MutableBigInteger mod, int k) {
        // 复制 mod 以保护原始值
        return fixup(new MutableBigInteger(1), new MutableBigInteger(mod), k);
    }

    /**
     * 计算此数模 mod 的乘法逆元，其中 mod 参数是奇数。此数和 mod 在计算过程中不会被改变。
     *
     * 此方法实现了一个由 Richard Schroeppel 提出的算法，该算法使用与 Montgomery Reduction
     * ("Montgomery Form") 相同的中间表示。该算法在一篇未发表的手稿 "Fast Modular Reciprocals" 中有描述。
     */
    private MutableBigInteger modInverse(MutableBigInteger mod) {
        MutableBigInteger p = new MutableBigInteger(mod);
        MutableBigInteger f = new MutableBigInteger(this);
        MutableBigInteger g = new MutableBigInteger(p);
        SignedMutableBigInteger c = new SignedMutableBigInteger(1);
        SignedMutableBigInteger d = new SignedMutableBigInteger();
        MutableBigInteger temp = null;
        SignedMutableBigInteger sTemp = null;

        int k = 0;
        // 右移 f k 次直到奇数，左移 d k 次
        if (f.isEven()) {
            int trailingZeros = f.getLowestSetBit();
            f.rightShift(trailingZeros);
            d.leftShift(trailingZeros);
            k = trailingZeros;
        }

        // 几乎逆算法
        while (!f.isOne()) {
            // 如果 gcd(f, g) != 1，数在模 mod 下不可逆
            if (f.isZero())
                throw new ArithmeticException("BigInteger 不可逆。");

            // 如果 f < g 交换 f, g 和 c, d
            if (f.compare(g) < 0) {
                temp = f; f = g; g = temp;
                sTemp = d; d = c; c = sTemp;
            }

            // 如果 f == g (mod 4)
            if (((f.value[f.offset + f.intLen - 1] ^
                 g.value[g.offset + g.intLen - 1]) & 3) == 0) {
                f.subtract(g);
                c.signedSubtract(d);
            } else { // 如果 f != g (mod 4)
                f.add(g);
                c.signedAdd(d);
            }

            // 右移 f k 次直到奇数，左移 d k 次
            int trailingZeros = f.getLowestSetBit();
            f.rightShift(trailingZeros);
            d.leftShift(trailingZeros);
            k += trailingZeros;
        }

        if (c.compare(p) >= 0) { // c 的绝对值大于 p
            MutableBigInteger remainder = c.divide(p,
                new MutableBigInteger());
            // 前一行忽略了符号，所以我们把数据复制回 c，这将根据需要恢复符号（并将其转换回 SignedMutableBigInteger）
            c.copyValue(remainder);
        }


                    if (c.sign < 0) {
           c.signedAdd(p);
        }

        return fixup(c, p, k);
    }

    /**
     * The Fixup Algorithm
     * 计算 X 使得 X = C * 2^(-k) (mod P)
     * 假设 C<P 且 P 是奇数。
     */
    static MutableBigInteger fixup(MutableBigInteger c, MutableBigInteger p,
                                                                      int k) {
        MutableBigInteger temp = new MutableBigInteger();
        // 将 r 设置为 p 模 2^32 的乘法逆元
        int r = -inverseMod32(p.value[p.offset+p.intLen-1]);

        for (int i=0, numWords = k >> 5; i < numWords; i++) {
            // V = R * c (mod 2^j)
            int  v = r * c.value[c.offset + c.intLen-1];
            // c = c + (v * p)
            p.mul(v, temp);
            c.add(temp);
            // c = c / 2^j
            c.intLen--;
        }
        int numBits = k & 0x1f;
        if (numBits != 0) {
            // V = R * c (mod 2^j)
            int v = r * c.value[c.offset + c.intLen-1];
            v &= ((1<<numBits) - 1);
            // c = c + (v * p)
            p.mul(v, temp);
            c.add(temp);
            // c = c / 2^j
            c.rightShift(numBits);
        }

        // 理论上，此时 c 可能大于 p（非常罕见！）
        if (c.compare(p) >= 0)
            c = c.divide(p, new MutableBigInteger());

        return c;
    }

    /**
     * 使用扩展欧几里得算法计算 base 的模逆元，模数是 2 的幂。模数是 2^k。
     */
    MutableBigInteger euclidModInverse(int k) {
        MutableBigInteger b = new MutableBigInteger(1);
        b.leftShift(k);
        MutableBigInteger mod = new MutableBigInteger(b);

        MutableBigInteger a = new MutableBigInteger(this);
        MutableBigInteger q = new MutableBigInteger();
        MutableBigInteger r = b.divide(a, q);

        MutableBigInteger swapper = b;
        // 交换 b 和 r
        b = r;
        r = swapper;

        MutableBigInteger t1 = new MutableBigInteger(q);
        MutableBigInteger t0 = new MutableBigInteger(1);
        MutableBigInteger temp = new MutableBigInteger();

        while (!b.isOne()) {
            r = a.divide(b, q);

            if (r.intLen == 0)
                throw new ArithmeticException("BigInteger not invertible.");

            swapper = r;
            a = swapper;

            if (q.intLen == 1)
                t1.mul(q.value[q.offset], temp);
            else
                q.multiply(t1, temp);
            swapper = q;
            q = temp;
            temp = swapper;
            t0.add(q);

            if (a.isOne())
                return t0;

            r = b.divide(a, q);

            if (r.intLen == 0)
                throw new ArithmeticException("BigInteger not invertible.");

            swapper = b;
            b =  r;

            if (q.intLen == 1)
                t0.mul(q.value[q.offset], temp);
            else
                q.multiply(t0, temp);
            swapper = q; q = temp; temp = swapper;

            t1.add(q);
        }
        mod.subtract(t1);
        return mod;
    }
}
