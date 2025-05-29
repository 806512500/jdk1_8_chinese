
/*
 * 版权所有 (c) 2000, 2020，Oracle 和/或其附属公司。保留所有权利。
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

import java.security.AccessController;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import sun.misc.JavaLangRefAccess;
import sun.misc.SharedSecrets;
import sun.misc.Unsafe;
import sun.misc.VM;

/**
 * 访问位，包括本地和其他方式。
 */

class Bits {                            // 包私有

    private Bits() { }


    // -- 交换 --

    static short swap(short x) {
        return Short.reverseBytes(x);
    }

    static char swap(char x) {
        return Character.reverseBytes(x);
    }

    static int swap(int x) {
        return Integer.reverseBytes(x);
    }

    static long swap(long x) {
        return Long.reverseBytes(x);
    }


    // -- 获取/放置 char --

    static private char makeChar(byte b1, byte b0) {
        return (char)((b1 << 8) | (b0 & 0xff));
    }

    static char getCharL(ByteBuffer bb, int bi) {
        return makeChar(bb._get(bi + 1),
                        bb._get(bi    ));
    }

    static char getCharL(long a) {
        return makeChar(_get(a + 1),
                        _get(a    ));
    }

    static char getCharB(ByteBuffer bb, int bi) {
        return makeChar(bb._get(bi    ),
                        bb._get(bi + 1));
    }

    static char getCharB(long a) {
        return makeChar(_get(a    ),
                        _get(a + 1));
    }

    static char getChar(ByteBuffer bb, int bi, boolean bigEndian) {
        return bigEndian ? getCharB(bb, bi) : getCharL(bb, bi);
    }

    static char getChar(long a, boolean bigEndian) {
        return bigEndian ? getCharB(a) : getCharL(a);
    }

    private static byte char1(char x) { return (byte)(x >> 8); }
    private static byte char0(char x) { return (byte)(x     ); }

    static void putCharL(ByteBuffer bb, int bi, char x) {
        bb._put(bi    , char0(x));
        bb._put(bi + 1, char1(x));
    }

    static void putCharL(long a, char x) {
        _put(a    , char0(x));
        _put(a + 1, char1(x));
    }

    static void putCharB(ByteBuffer bb, int bi, char x) {
        bb._put(bi    , char1(x));
        bb._put(bi + 1, char0(x));
    }

    static void putCharB(long a, char x) {
        _put(a    , char1(x));
        _put(a + 1, char0(x));
    }

    static void putChar(ByteBuffer bb, int bi, char x, boolean bigEndian) {
        if (bigEndian)
            putCharB(bb, bi, x);
        else
            putCharL(bb, bi, x);
    }

    static void putChar(long a, char x, boolean bigEndian) {
        if (bigEndian)
            putCharB(a, x);
        else
            putCharL(a, x);
    }


    // -- 获取/放置 short --

    static private short makeShort(byte b1, byte b0) {
        return (short)((b1 << 8) | (b0 & 0xff));
    }

    static short getShortL(ByteBuffer bb, int bi) {
        return makeShort(bb._get(bi + 1),
                         bb._get(bi    ));
    }

    static short getShortL(long a) {
        return makeShort(_get(a + 1),
                         _get(a    ));
    }

    static short getShortB(ByteBuffer bb, int bi) {
        return makeShort(bb._get(bi    ),
                         bb._get(bi + 1));
    }

    static short getShortB(long a) {
        return makeShort(_get(a    ),
                         _get(a + 1));
    }

    static short getShort(ByteBuffer bb, int bi, boolean bigEndian) {
        return bigEndian ? getShortB(bb, bi) : getShortL(bb, bi);
    }

    static short getShort(long a, boolean bigEndian) {
        return bigEndian ? getShortB(a) : getShortL(a);
    }

    private static byte short1(short x) { return (byte)(x >> 8); }
    private static byte short0(short x) { return (byte)(x     ); }

    static void putShortL(ByteBuffer bb, int bi, short x) {
        bb._put(bi    , short0(x));
        bb._put(bi + 1, short1(x));
    }

    static void putShortL(long a, short x) {
        _put(a    , short0(x));
        _put(a + 1, short1(x));
    }

    static void putShortB(ByteBuffer bb, int bi, short x) {
        bb._put(bi    , short1(x));
        bb._put(bi + 1, short0(x));
    }

    static void putShortB(long a, short x) {
        _put(a    , short1(x));
        _put(a + 1, short0(x));
    }

    static void putShort(ByteBuffer bb, int bi, short x, boolean bigEndian) {
        if (bigEndian)
            putShortB(bb, bi, x);
        else
            putShortL(bb, bi, x);
    }

    static void putShort(long a, short x, boolean bigEndian) {
        if (bigEndian)
            putShortB(a, x);
        else
            putShortL(a, x);
    }


    // -- 获取/放置 int --

    static private int makeInt(byte b3, byte b2, byte b1, byte b0) {
        return (((b3       ) << 24) |
                ((b2 & 0xff) << 16) |
                ((b1 & 0xff) <<  8) |
                ((b0 & 0xff)      ));
    }

    static int getIntL(ByteBuffer bb, int bi) {
        return makeInt(bb._get(bi + 3),
                       bb._get(bi + 2),
                       bb._get(bi + 1),
                       bb._get(bi    ));
    }

    static int getIntL(long a) {
        return makeInt(_get(a + 3),
                       _get(a + 2),
                       _get(a + 1),
                       _get(a    ));
    }

    static int getIntB(ByteBuffer bb, int bi) {
        return makeInt(bb._get(bi    ),
                       bb._get(bi + 1),
                       bb._get(bi + 2),
                       bb._get(bi + 3));
    }

    static int getIntB(long a) {
        return makeInt(_get(a    ),
                       _get(a + 1),
                       _get(a + 2),
                       _get(a + 3));
    }

    static int getInt(ByteBuffer bb, int bi, boolean bigEndian) {
        return bigEndian ? getIntB(bb, bi) : getIntL(bb, bi) ;
    }

    static int getInt(long a, boolean bigEndian) {
        return bigEndian ? getIntB(a) : getIntL(a) ;
    }

    private static byte int3(int x) { return (byte)(x >> 24); }
    private static byte int2(int x) { return (byte)(x >> 16); }
    private static byte int1(int x) { return (byte)(x >>  8); }
    private static byte int0(int x) { return (byte)(x      ); }


                static void putIntL(ByteBuffer bb, int bi, int x) {
        bb._put(bi + 3, int3(x)); // 将 x 的最低 8 位写入 bb 的 (bi + 3) 位置
        bb._put(bi + 2, int2(x)); // 将 x 的第 9 到 16 位写入 bb 的 (bi + 2) 位置
        bb._put(bi + 1, int1(x)); // 将 x 的第 17 到 24 位写入 bb 的 (bi + 1) 位置
        bb._put(bi    , int0(x)); // 将 x 的最高 8 位写入 bb 的 bi 位置
    }

    static void putIntL(long a, int x) {
        _put(a + 3, int3(x)); // 将 x 的最低 8 位写入地址 a + 3
        _put(a + 2, int2(x)); // 将 x 的第 9 到 16 位写入地址 a + 2
        _put(a + 1, int1(x)); // 将 x 的第 17 到 24 位写入地址 a + 1
        _put(a    , int0(x)); // 将 x 的最高 8 位写入地址 a
    }

    static void putIntB(ByteBuffer bb, int bi, int x) {
        bb._put(bi    , int3(x)); // 将 x 的最高 8 位写入 bb 的 bi 位置
        bb._put(bi + 1, int2(x)); // 将 x 的第 17 到 24 位写入 bb 的 (bi + 1) 位置
        bb._put(bi + 2, int1(x)); // 将 x 的第 9 到 16 位写入 bb 的 (bi + 2) 位置
        bb._put(bi + 3, int0(x)); // 将 x 的最低 8 位写入 bb 的 (bi + 3) 位置
    }

    static void putIntB(long a, int x) {
        _put(a    , int3(x)); // 将 x 的最高 8 位写入地址 a
        _put(a + 1, int2(x)); // 将 x 的第 17 到 24 位写入地址 a + 1
        _put(a + 2, int1(x)); // 将 x 的第 9 到 16 位写入地址 a + 2
        _put(a + 3, int0(x)); // 将 x 的最低 8 位写入地址 a + 3
    }

    static void putInt(ByteBuffer bb, int bi, int x, boolean bigEndian) {
        if (bigEndian)
            putIntB(bb, bi, x); // 如果是大端模式，调用 putIntB
        else
            putIntL(bb, bi, x); // 如果是小端模式，调用 putIntL
    }

    static void putInt(long a, int x, boolean bigEndian) {
        if (bigEndian)
            putIntB(a, x); // 如果是大端模式，调用 putIntB
        else
            putIntL(a, x); // 如果是小端模式，调用 putIntL
    }


    // -- get/put long --

    static private long makeLong(byte b7, byte b6, byte b5, byte b4,
                                 byte b3, byte b2, byte b1, byte b0)
    {
        return ((((long)b7       ) << 56) | // 将 b7 移动到最高 8 位
                (((long)b6 & 0xff) << 48) | // 将 b6 移动到第 49 到 56 位
                (((long)b5 & 0xff) << 40) | // 将 b5 移动到第 41 到 48 位
                (((long)b4 & 0xff) << 32) | // 将 b4 移动到第 33 到 40 位
                (((long)b3 & 0xff) << 24) | // 将 b3 移动到第 25 到 32 位
                (((long)b2 & 0xff) << 16) | // 将 b2 移动到第 17 到 24 位
                (((long)b1 & 0xff) <<  8) | // 将 b1 移动到第 9 到 16 位
                (((long)b0 & 0xff)      )); // 将 b0 移动到最低 8 位
    }

    static long getLongL(ByteBuffer bb, int bi) {
        return makeLong(bb._get(bi + 7), // 从 bb 的 (bi + 7) 位置读取最低 8 位
                        bb._get(bi + 6), // 从 bb 的 (bi + 6) 位置读取第 9 到 16 位
                        bb._get(bi + 5), // 从 bb 的 (bi + 5) 位置读取第 17 到 24 位
                        bb._get(bi + 4), // 从 bb 的 (bi + 4) 位置读取第 25 到 32 位
                        bb._get(bi + 3), // 从 bb 的 (bi + 3) 位置读取第 33 到 40 位
                        bb._get(bi + 2), // 从 bb 的 (bi + 2) 位置读取第 41 到 48 位
                        bb._get(bi + 1), // 从 bb 的 (bi + 1) 位置读取第 49 到 56 位
                        bb._get(bi    )); // 从 bb 的 bi 位置读取最高 8 位
    }

    static long getLongL(long a) {
        return makeLong(_get(a + 7), // 从地址 a + 7 读取最低 8 位
                        _get(a + 6), // 从地址 a + 6 读取第 9 到 16 位
                        _get(a + 5), // 从地址 a + 5 读取第 17 到 24 位
                        _get(a + 4), // 从地址 a + 4 读取第 25 到 32 位
                        _get(a + 3), // 从地址 a + 3 读取第 33 到 40 位
                        _get(a + 2), // 从地址 a + 2 读取第 41 到 48 位
                        _get(a + 1), // 从地址 a + 1 读取第 49 到 56 位
                        _get(a    )); // 从地址 a 读取最高 8 位
    }

    static long getLongB(ByteBuffer bb, int bi) {
        return makeLong(bb._get(bi    ), // 从 bb 的 bi 位置读取最高 8 位
                        bb._get(bi + 1), // 从 bb 的 (bi + 1) 位置读取第 49 到 56 位
                        bb._get(bi + 2), // 从 bb 的 (bi + 2) 位置读取第 41 到 48 位
                        bb._get(bi + 3), // 从 bb 的 (bi + 3) 位置读取第 33 到 40 位
                        bb._get(bi + 4), // 从 bb 的 (bi + 4) 位置读取第 25 到 32 位
                        bb._get(bi + 5), // 从 bb 的 (bi + 5) 位置读取第 17 到 24 位
                        bb._get(bi + 6), // 从 bb 的 (bi + 6) 位置读取第 9 到 16 位
                        bb._get(bi + 7)); // 从 bb 的 (bi + 7) 位置读取最低 8 位
    }

    static long getLongB(long a) {
        return makeLong(_get(a    ), // 从地址 a 读取最高 8 位
                        _get(a + 1), // 从地址 a + 1 读取第 49 到 56 位
                        _get(a + 2), // 从地址 a + 2 读取第 41 到 48 位
                        _get(a + 3), // 从地址 a + 3 读取第 33 到 40 位
                        _get(a + 4), // 从地址 a + 4 读取第 25 到 32 位
                        _get(a + 5), // 从地址 a + 5 读取第 17 到 24 位
                        _get(a + 6), // 从地址 a + 6 读取第 9 到 16 位
                        _get(a + 7)); // 从地址 a + 7 读取最低 8 位
    }

    static long getLong(ByteBuffer bb, int bi, boolean bigEndian) {
        return bigEndian ? getLongB(bb, bi) : getLongL(bb, bi); // 根据 bigEndian 选择大端或小端模式
    }

    static long getLong(long a, boolean bigEndian) {
        return bigEndian ? getLongB(a) : getLongL(a); // 根据 bigEndian 选择大端或小端模式
    }

    private static byte long7(long x) { return (byte)(x >> 56); } // 获取 x 的最高 8 位
    private static byte long6(long x) { return (byte)(x >> 48); } // 获取 x 的第 49 到 56 位
    private static byte long5(long x) { return (byte)(x >> 40); } // 获取 x 的第 41 到 48 位
    private static byte long4(long x) { return (byte)(x >> 32); } // 获取 x 的第 33 到 40 位
    private static byte long3(long x) { return (byte)(x >> 24); } // 获取 x 的第 25 到 32 位
    private static byte long2(long x) { return (byte)(x >> 16); } // 获取 x 的第 17 到 24 位
    private static byte long1(long x) { return (byte)(x >>  8); } // 获取 x 的第 9 到 16 位
    private static byte long0(long x) { return (byte)(x      ); } // 获取 x 的最低 8 位

    static void putLongL(ByteBuffer bb, int bi, long x) {
        bb._put(bi + 7, long7(x)); // 将 x 的最低 8 位写入 bb 的 (bi + 7) 位置
        bb._put(bi + 6, long6(x)); // 将 x 的第 9 到 16 位写入 bb 的 (bi + 6) 位置
        bb._put(bi + 5, long5(x)); // 将 x 的第 17 到 24 位写入 bb 的 (bi + 5) 位置
        bb._put(bi + 4, long4(x)); // 将 x 的第 25 到 32 位写入 bb 的 (bi + 4) 位置
        bb._put(bi + 3, long3(x)); // 将 x 的第 33 到 40 位写入 bb 的 (bi + 3) 位置
        bb._put(bi + 2, long2(x)); // 将 x 的第 41 到 48 位写入 bb 的 (bi + 2) 位置
        bb._put(bi + 1, long1(x)); // 将 x 的第 49 到 56 位写入 bb 的 (bi + 1) 位置
        bb._put(bi    , long0(x)); // 将 x 的最高 8 位写入 bb 的 bi 位置
    }

    static void putLongL(long a, long x) {
        _put(a + 7, long7(x)); // 将 x 的最低 8 位写入地址 a + 7
        _put(a + 6, long6(x)); // 将 x 的第 9 到 16 位写入地址 a + 6
        _put(a + 5, long5(x)); // 将 x 的第 17 到 24 位写入地址 a + 5
        _put(a + 4, long4(x)); // 将 x 的第 25 到 32 位写入地址 a + 4
        _put(a + 3, long3(x)); // 将 x 的第 33 到 40 位写入地址 a + 3
        _put(a + 2, long2(x)); // 将 x 的第 41 到 48 位写入地址 a + 2
        _put(a + 1, long1(x)); // 将 x 的第 49 到 56 位写入地址 a + 1
        _put(a    , long0(x)); // 将 x 的最高 8 位写入地址 a
    }

    static void putLongB(ByteBuffer bb, int bi, long x) {
        bb._put(bi    , long7(x)); // 将 x 的最高 8 位写入 bb 的 bi 位置
        bb._put(bi + 1, long6(x)); // 将 x 的第 49 到 56 位写入 bb 的 (bi + 1) 位置
        bb._put(bi + 2, long5(x)); // 将 x 的第 41 到 48 位写入 bb 的 (bi + 2) 位置
        bb._put(bi + 3, long4(x)); // 将 x 的第 33 到 40 位写入 bb 的 (bi + 3) 位置
        bb._put(bi + 4, long3(x)); // 将 x 的第 25 到 32 位写入 bb 的 (bi + 4) 位置
        bb._put(bi + 5, long2(x)); // 将 x 的第 17 到 24 位写入 bb 的 (bi + 5) 位置
        bb._put(bi + 6, long1(x)); // 将 x 的第 9 到 16 位写入 bb 的 (bi + 6) 位置
        bb._put(bi + 7, long0(x)); // 将 x 的最低 8 位写入 bb 的 (bi + 7) 位置
    }

    static void putLongB(long a, long x) {
        _put(a    , long7(x)); // 将 x 的最高 8 位写入地址 a
        _put(a + 1, long6(x)); // 将 x 的第 49 到 56 位写入地址 a + 1
        _put(a + 2, long5(x)); // 将 x 的第 41 到 48 位写入地址 a + 2
        _put(a + 3, long4(x)); // 将 x 的第 33 到 40 位写入地址 a + 3
        _put(a + 4, long3(x)); // 将 x 的第 25 到 32 位写入地址 a + 4
        _put(a + 5, long2(x)); // 将 x 的第 17 到 24 位写入地址 a + 5
        _put(a + 6, long1(x)); // 将 x 的第 9 到 16 位写入地址 a + 6
        _put(a + 7, long0(x)); // 将 x 的最低 8 位写入地址 a + 7
    }

    static void putLong(ByteBuffer bb, int bi, long x, boolean bigEndian) {
        if (bigEndian)
            putLongB(bb, bi, x); // 如果是大端模式，调用 putLongB
        else
            putLongL(bb, bi, x); // 如果是小端模式，调用 putLongL
    }

    static void putLong(long a, long x, boolean bigEndian) {
        if (bigEndian)
            putLongB(a, x); // 如果是大端模式，调用 putLongB
        else
            putLongL(a, x); // 如果是小端模式，调用 putLongL
    }


    // -- get/put float --

    static float getFloatL(ByteBuffer bb, int bi) {
        return Float.intBitsToFloat(getIntL(bb, bi)); // 将小端模式的 int 转换为 float
    }

    static float getFloatL(long a) {
        return Float.intBitsToFloat(getIntL(a)); // 将小端模式的 int 转换为 float
    }

    static float getFloatB(ByteBuffer bb, int bi) {
        return Float.intBitsToFloat(getIntB(bb, bi)); // 将大端模式的 int 转换为 float
    }

    static float getFloatB(long a) {
        return Float.intBitsToFloat(getIntB(a)); // 将大端模式的 int 转换为 float
    }

    static float getFloat(ByteBuffer bb, int bi, boolean bigEndian) {
        return bigEndian ? getFloatB(bb, bi) : getFloatL(bb, bi); // 根据 bigEndian 选择大端或小端模式
    }

    static float getFloat(long a, boolean bigEndian) {
        return bigEndian ? getFloatB(a) : getFloatL(a); // 根据 bigEndian 选择大端或小端模式
    }


                static void putFloatL(ByteBuffer bb, int bi, float x) {
        putIntL(bb, bi, Float.floatToRawIntBits(x));
    }

    static void putFloatL(long a, float x) {
        putIntL(a, Float.floatToRawIntBits(x));
    }

    static void putFloatB(ByteBuffer bb, int bi, float x) {
        putIntB(bb, bi, Float.floatToRawIntBits(x));
    }

    static void putFloatB(long a, float x) {
        putIntB(a, Float.floatToRawIntBits(x));
    }

    static void putFloat(ByteBuffer bb, int bi, float x, boolean bigEndian) {
        if (bigEndian)
            putFloatB(bb, bi, x);
        else
            putFloatL(bb, bi, x);
    }

    static void putFloat(long a, float x, boolean bigEndian) {
        if (bigEndian)
            putFloatB(a, x);
        else
            putFloatL(a, x);
    }


    // -- get/put double --

    static double getDoubleL(ByteBuffer bb, int bi) {
        return Double.longBitsToDouble(getLongL(bb, bi));
    }

    static double getDoubleL(long a) {
        return Double.longBitsToDouble(getLongL(a));
    }

    static double getDoubleB(ByteBuffer bb, int bi) {
        return Double.longBitsToDouble(getLongB(bb, bi));
    }

    static double getDoubleB(long a) {
        return Double.longBitsToDouble(getLongB(a));
    }

    static double getDouble(ByteBuffer bb, int bi, boolean bigEndian) {
        return bigEndian ? getDoubleB(bb, bi) : getDoubleL(bb, bi);
    }

    static double getDouble(long a, boolean bigEndian) {
        return bigEndian ? getDoubleB(a) : getDoubleL(a);
    }

    static void putDoubleL(ByteBuffer bb, int bi, double x) {
        putLongL(bb, bi, Double.doubleToRawLongBits(x));
    }

    static void putDoubleL(long a, double x) {
        putLongL(a, Double.doubleToRawLongBits(x));
    }

    static void putDoubleB(ByteBuffer bb, int bi, double x) {
        putLongB(bb, bi, Double.doubleToRawLongBits(x));
    }

    static void putDoubleB(long a, double x) {
        putLongB(a, Double.doubleToRawLongBits(x));
    }

    static void putDouble(ByteBuffer bb, int bi, double x, boolean bigEndian) {
        if (bigEndian)
            putDoubleB(bb, bi, x);
        else
            putDoubleL(bb, bi, x);
    }

    static void putDouble(long a, double x, boolean bigEndian) {
        if (bigEndian)
            putDoubleB(a, x);
        else
            putDoubleL(a, x);
    }


    // -- Unsafe access --

    private static final Unsafe unsafe = Unsafe.getUnsafe();

    private static byte _get(long a) {
        return unsafe.getByte(a);
    }

    private static void _put(long a, byte b) {
        unsafe.putByte(a, b);
    }

    static Unsafe unsafe() {
        return unsafe;
    }


    // -- Processor and memory-system properties --

    private static final ByteOrder byteOrder;

    static ByteOrder byteOrder() {
        if (byteOrder == null)
            throw new Error("Unknown byte order");
        return byteOrder;
    }

    static {
        long a = unsafe.allocateMemory(8);
        try {
            unsafe.putLong(a, 0x0102030405060708L);
            byte b = unsafe.getByte(a);
            switch (b) {
            case 0x01: byteOrder = ByteOrder.BIG_ENDIAN;     break;
            case 0x08: byteOrder = ByteOrder.LITTLE_ENDIAN;  break;
            default:
                assert false;
                byteOrder = null;
            }
        } finally {
            unsafe.freeMemory(a);
        }
    }


    private static int pageSize = -1;

    static int pageSize() {
        if (pageSize == -1)
            pageSize = unsafe().pageSize();
        return pageSize;
    }

    static int pageCount(long size) {
        return (int)(size + (long)pageSize() - 1L) / pageSize();
    }

    private static boolean unaligned;
    private static boolean unalignedKnown = false;

    static boolean unaligned() {
        if (unalignedKnown)
            return unaligned;
        String arch = AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("os.arch"));
        unaligned = arch.equals("i386") || arch.equals("x86")
            || arch.equals("amd64") || arch.equals("x86_64")
            || arch.equals("ppc64") || arch.equals("ppc64le")
            || arch.equals("aarch64");
        unalignedKnown = true;
        return unaligned;
    }


    // -- Direct memory management --

    // A user-settable upper limit on the maximum amount of allocatable
    // direct buffer memory.  This value may be changed during VM
    // initialization if it is launched with "-XX:MaxDirectMemorySize=<size>".
    private static volatile long maxMemory = VM.maxDirectMemory();
    private static final AtomicLong reservedMemory = new AtomicLong();
    private static final AtomicLong totalCapacity = new AtomicLong();
    private static final AtomicLong count = new AtomicLong();
    private static volatile boolean memoryLimitSet = false;
    // max. number of sleeps during try-reserving with exponentially
    // increasing delay before throwing OutOfMemoryError:
    // 1, 2, 4, 8, 16, 32, 64, 128, 256 (total 511 ms ~ 0.5 s)
    // which means that OOME will be thrown after 0.5 s of trying
    private static final int MAX_SLEEPS = 9;

    // These methods should be called whenever direct memory is allocated or
    // freed.  They allow the user to control the amount of direct memory
    // which a process may access.  All sizes are specified in bytes.
    static void reserveMemory(long size, int cap) {

        if (!memoryLimitSet && VM.isBooted()) {
            maxMemory = VM.maxDirectMemory();
            memoryLimitSet = true;
        }

        // optimist!
        if (tryReserveMemory(size, cap)) {
            return;
        }

        final JavaLangRefAccess jlra = SharedSecrets.getJavaLangRefAccess();

        // retry while helping enqueue pending Reference objects
        // which includes executing pending Cleaner(s) which includes
        // Cleaner(s) that free direct buffer memory
        while (jlra.tryHandlePendingReference()) {
            if (tryReserveMemory(size, cap)) {
                return;
            }
        }

        // trigger VM's Reference processing
        System.gc();


                    // 一个带有指数退避延迟的重试循环
        // （这给VM一些时间来完成其工作）
        boolean interrupted = false;
        try {
            long sleepTime = 1;
            int sleeps = 0;
            while (true) {
                if (tryReserveMemory(size, cap)) {
                    return;
                }
                if (sleeps >= MAX_SLEEPS) {
                    break;
                }
                if (!jlra.tryHandlePendingReference()) {
                    try {
                        Thread.sleep(sleepTime);
                        sleepTime <<= 1;
                        sleeps++;
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                }
            }

            // 没有成功
            throw new OutOfMemoryError("Direct buffer memory");

        } finally {
            if (interrupted) {
                // 不要忽略中断
                Thread.currentThread().interrupt();
            }
        }
    }

    private static boolean tryReserveMemory(long size, int cap) {

        // -XX:MaxDirectMemorySize 限制的是总容量而不是实际内存使用量，这在缓冲区页面对齐时会有所不同。
        long totalCap;
        while (cap <= maxMemory - (totalCap = totalCapacity.get())) {
            if (totalCapacity.compareAndSet(totalCap, totalCap + cap)) {
                reservedMemory.addAndGet(size);
                count.incrementAndGet();
                return true;
            }
        }

        return false;
    }


    static void unreserveMemory(long size, int cap) {
        long cnt = count.decrementAndGet();
        long reservedMem = reservedMemory.addAndGet(-size);
        long totalCap = totalCapacity.addAndGet(-cap);
        assert cnt >= 0 && reservedMem >= 0 && totalCap >= 0;
    }

    // -- 直接缓冲区使用情况监控 --

    static {
        // 在 SharedSecrets 中设置对本包的访问
        sun.misc.SharedSecrets.setJavaNioAccess(
            new sun.misc.JavaNioAccess() {
                @Override
                public sun.misc.JavaNioAccess.BufferPool getDirectBufferPool() {
                    return new sun.misc.JavaNioAccess.BufferPool() {
                        @Override
                        public String getName() {
                            return "direct";
                        }
                        @Override
                        public long getCount() {
                            return Bits.count.get();
                        }
                        @Override
                        public long getTotalCapacity() {
                            return Bits.totalCapacity.get();
                        }
                        @Override
                        public long getMemoryUsed() {
                            return Bits.reservedMemory.get();
                        }
                    };
                }
                @Override
                public ByteBuffer newDirectByteBuffer(long addr, int cap, Object ob) {
                    return new DirectByteBuffer(addr, cap, ob);
                }
                @Override
                public void truncate(Buffer buf) {
                    buf.truncate();
                }
        });
    }

    // -- 批量获取/放置加速 --

    // 这些数字代表我们通过实证确定的平均 JNI 调用成本超过逐个元素复制成本的点。这些数字可能会随时间变化。
    static final int JNI_COPY_TO_ARRAY_THRESHOLD   = 6;
    static final int JNI_COPY_FROM_ARRAY_THRESHOLD = 6;

    // 这个数字限制了每次调用 Unsafe 的 copyMemory 方法时复制的字节数。设置限制是为了在大复制过程中允许安全点轮询
    static final long UNSAFE_COPY_THRESHOLD = 1024L * 1024L;

    // 这些方法不进行边界检查。应该在调用前验证复制不会导致内存损坏。
    // 所有位置和长度都以字节为单位指定。

    /**
     * 从给定的源数组复制到目标地址。
     *
     * @param   src
     *          源数组
     * @param   srcBaseOffset
     *          源数组中第一个元素的存储偏移量
     * @param   srcPos
     *          源数组中第一个要读取的元素的偏移量
     * @param   dstAddr
     *          目标地址
     * @param   length
     *          要复制的字节数
     */
    static void copyFromArray(Object src, long srcBaseOffset, long srcPos,
                              long dstAddr, long length)
    {
        long offset = srcBaseOffset + srcPos;
        while (length > 0) {
            long size = (length > UNSAFE_COPY_THRESHOLD) ? UNSAFE_COPY_THRESHOLD : length;
            unsafe.copyMemory(src, offset, null, dstAddr, size);
            length -= size;
            offset += size;
            dstAddr += size;
        }
    }

    /**
     * 从源地址复制到给定的目标数组。
     *
     * @param   srcAddr
     *          源地址
     * @param   dst
     *          目标数组
     * @param   dstBaseOffset
     *          目标数组中第一个元素的存储偏移量
     * @param   dstPos
     *          目标数组中第一个要写入的元素的偏移量
     * @param   length
     *          要复制的字节数
     */
    static void copyToArray(long srcAddr, Object dst, long dstBaseOffset, long dstPos,
                            long length)
    {
        long offset = dstBaseOffset + dstPos;
        while (length > 0) {
            long size = (length > UNSAFE_COPY_THRESHOLD) ? UNSAFE_COPY_THRESHOLD : length;
            unsafe.copyMemory(null, srcAddr, dst, offset, size);
            length -= size;
            srcAddr += size;
            offset += size;
        }
    }

    /**
     * 从堆数组复制并无条件地进行16位元素的字节交换到非堆内存
     *
     * @param src
     *        源数组，必须是16位原始数组类型
     * @param srcPos
     *        源数组中第一个要读取的元素的字节偏移量
     * @param dstAddr
     *        目标地址
     * @param length
     *        要复制的字节数
     */
    static void copyFromCharArray(Object src, long srcPos, long dstAddr, long length) {
        copySwapMemory(src, unsafe.arrayBaseOffset(src.getClass()) + srcPos, null, dstAddr, length, 2);
    }


                /**
     * 从非堆内存复制并无条件地字节交换16位元素到堆数组
     *
     * @param srcAddr
     *        源地址
     * @param dst
     *        目标数组，必须是16位基本数组类型
     * @param dstPos
     *        目标数组中第一个元素的字节偏移量
     * @param length
     *        要复制的字节数
     */
    static void copyToCharArray(long srcAddr, Object dst, long dstPos, long length) {
        copySwapMemory(null, srcAddr, dst, unsafe.arrayBaseOffset(dst.getClass()) + dstPos, length, 2);
    }

    /**
     * 从堆数组复制并无条件地字节交换16位元素到非堆内存
     *
     * @param src
     *        源数组，必须是16位基本数组类型
     * @param srcPos
     *        源数组中第一个元素的字节偏移量
     * @param dstAddr
     *        目标地址
     * @param length
     *        要复制的字节数
     */
    static void copyFromShortArray(Object src, long srcPos, long dstAddr, long length) {
        copySwapMemory(src, unsafe.arrayBaseOffset(src.getClass()) + srcPos, null, dstAddr, length, 2);
    }

    /**
     * 从非堆内存复制并无条件地字节交换16位元素到堆数组
     *
     * @param srcAddr
     *        源地址
     * @param dst
     *        目标数组，必须是16位基本数组类型
     * @param dstPos
     *        目标数组中第一个元素的字节偏移量
     * @param length
     *        要复制的字节数
     */
    static void copyToShortArray(long srcAddr, Object dst, long dstPos, long length) {
        copySwapMemory(null, srcAddr, dst, unsafe.arrayBaseOffset(dst.getClass()) + dstPos, length, 2);
    }

    /**
     * 从堆数组复制并无条件地字节交换32位元素到非堆内存
     *
     * @param src
     *        源数组，必须是32位基本数组类型
     * @param srcPos
     *        源数组中第一个元素的字节偏移量
     * @param dstAddr
     *        目标地址
     * @param length
     *        要复制的字节数
     */
    static void copyFromIntArray(Object src, long srcPos, long dstAddr, long length) {
        copySwapMemory(src, unsafe.arrayBaseOffset(src.getClass()) + srcPos, null, dstAddr, length, 4);
    }

    /**
     * 从非堆内存复制并无条件地字节交换32位元素到堆数组
     *
     * @param srcAddr
     *        源地址
     * @param dst
     *        目标数组，必须是32位基本数组类型
     * @param dstPos
     *        目标数组中第一个元素的字节偏移量
     * @param length
     *        要复制的字节数
     */
    static void copyToIntArray(long srcAddr, Object dst, long dstPos, long length) {
        copySwapMemory(null, srcAddr, dst, unsafe.arrayBaseOffset(dst.getClass()) + dstPos, length, 4);
    }

    /**
     * 从堆数组复制并无条件地字节交换64位元素到非堆内存
     *
     * @param src
     *        源数组，必须是64位基本数组类型
     * @param srcPos
     *        源数组中第一个元素的字节偏移量
     * @param dstAddr
     *        目标地址
     * @param length
     *        要复制的字节数
     */
    static void copyFromLongArray(Object src, long srcPos, long dstAddr, long length) {
        copySwapMemory(src, unsafe.arrayBaseOffset(src.getClass()) + srcPos, null, dstAddr, length, 8);
    }

    /**
     * 从非堆内存复制并无条件地字节交换64位元素到堆数组
     *
     * @param srcAddr
     *        源地址
     * @param dst
     *        目标数组，必须是64位基本数组类型
     * @param dstPos
     *        目标数组中第一个元素的字节偏移量
     * @param length
     *        要复制的字节数
     */
    static void copyToLongArray(long srcAddr, Object dst, long dstPos, long length) {
        copySwapMemory(null, srcAddr, dst, unsafe.arrayBaseOffset(dst.getClass()) + dstPos, length, 8);
    }

    private static boolean isPrimitiveArray(Class<?> c) {
        Class<?> componentType = c.getComponentType();
        return componentType != null && componentType.isPrimitive();
    }

    private native static void copySwapMemory0(Object srcBase, long srcOffset,
                                        Object destBase, long destOffset,
                                        long bytes, long elemSize);

    /**
     * 从一个内存块复制所有元素到另一个内存块，并*无条件地*在复制过程中字节交换元素。
     *
     * <p>此方法通过两个参数确定每个内存块的基地址，因此它提供了（实际上）<em>双寄存器</em>寻址模式，
     * 如 {@link sun.misc.Unsafe#getInt(Object,long)} 中所述。当对象引用为null时，偏移量提供绝对基地址。
     *
     * @since 8u201
     */
    private static void copySwapMemory(Object srcBase, long srcOffset,
                               Object destBase, long destOffset,
                               long bytes, long elemSize) {
        if (bytes < 0) {
            throw new IllegalArgumentException();
        }
        if (elemSize != 2 && elemSize != 4 && elemSize != 8) {
            throw new IllegalArgumentException();
        }
        if (bytes % elemSize != 0) {
            throw new IllegalArgumentException();
        }
        if ((srcBase == null && srcOffset == 0) ||
            (destBase == null && destOffset == 0)) {
            throw new NullPointerException();
        }

        // 必须是非堆内存，或者是基本类型的堆数组
        if (srcBase != null && (srcOffset < 0 || !isPrimitiveArray(srcBase.getClass()))) {
            throw new IllegalArgumentException();
        }
        if (destBase != null && (destOffset < 0 || !isPrimitiveArray(destBase.getClass()))) {
            throw new IllegalArgumentException();
        }


                    // 检查 32 位平台上的大小和偏移量。最高 32 位必须为零。
        // 最高 32 位必须为零。
        if (unsafe.addressSize() == 4 &&
            (bytes >>> 32 != 0 || srcOffset >>> 32 != 0 || destOffset >>> 32 != 0)) {
            throw new IllegalArgumentException();
        }

        if (bytes == 0) {
            return;
        }

        copySwapMemory0(srcBase, srcOffset, destBase, destOffset, bytes, elemSize);
    }

}
