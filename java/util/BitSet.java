
/*
 * 版权所有 (c) 1995, 2014, Oracle 和/或其附属公司。保留所有权利。
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

package java.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * 该类实现了一个按需增长的位向量。位集的每个组件都有一个 {@code boolean} 值。
 * 位集的位由非负整数索引。可以通过逻辑与、逻辑或和逻辑异或操作来修改一个位集的内容。
 *
 * <p>默认情况下，位集中的所有位最初都具有 {@code false} 值。
 *
 * <p>每个位集都有一个当前大小，这是位集当前使用的位数。请注意，大小与位集的实现有关，因此可能会随着实现的变化而变化。
 * 位集的长度与位集的逻辑长度有关，并且独立于实现定义。
 *
 * <p>除非另有说明，否则将 null 参数传递给位集中的任何方法将导致
 * {@code NullPointerException}。
 *
 * <p>位集在没有外部同步的情况下不适用于多线程使用。
 *
 * @author  Arthur van Hoff
 * @author  Michael McCloskey
 * @author  Martin Buchholz
 * @since   JDK1.0
 */
public class BitSet implements Cloneable, java.io.Serializable {
    /*
     * 位集被压缩到“字”数组中。目前一个字是一个 long，由 64 位组成，需要 6 个地址位。
     * 字大小的选择完全基于性能考虑。
     */
    private final static int ADDRESS_BITS_PER_WORD = 6;
    private final static int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;
    private final static int BIT_INDEX_MASK = BITS_PER_WORD - 1;

    /* 用于左移或右移以生成部分字掩码 */
    private static final long WORD_MASK = 0xffffffffffffffffL;

    /**
     * @serialField bits long[]
     *
     * 该位集中的位。第 i 位存储在 bits[i/64] 中，位于 i % 64 位位置（其中位位置 0 指的是最低位，63 指的是最高位）。
     */
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("bits", long[].class),
    };

    /**
     * 与序列化字段 "bits" 对应的内部字段。
     */
    private long[] words;

    /**
     * 该位集逻辑大小中的字数。
     */
    private transient int wordsInUse = 0;

    /**
     * “words” 的大小是否由用户指定。如果是，我们假设用户知道自己在做什么，并更努力地保持它。
     */
    private transient boolean sizeIsSticky = false;

    /* 使用 JDK 1.0.2 的 serialVersionUID 以实现互操作性 */
    private static final long serialVersionUID = 7997698588986878753L;

    /**
     * 给定一个位索引，返回包含它的字索引。
     */
    private static int wordIndex(int bitIndex) {
        return bitIndex >> ADDRESS_BITS_PER_WORD;
    }

    /**
     * 每个公共方法都必须保持这些不变量。
     */
    private void checkInvariants() {
        assert(wordsInUse == 0 || words[wordsInUse - 1] != 0);
        assert(wordsInUse >= 0 && wordsInUse <= words.length);
        assert(wordsInUse == words.length || words[wordsInUse] == 0);
    }

    /**
     * 将字段 wordsInUse 设置为位集的逻辑大小（以字为单位）。
     * 警告：此方法假设实际使用的字数小于或等于 wordsInUse 的当前值！
     */
    private void recalculateWordsInUse() {
        // 遍历位集直到找到一个使用的字
        int i;
        for (i = wordsInUse-1; i >= 0; i--)
            if (words[i] != 0)
                break;

        wordsInUse = i+1; // 新的逻辑大小
    }

    /**
     * 创建一个新的位集。所有位最初都是 {@code false}。
     */
    public BitSet() {
        initWords(BITS_PER_WORD);
        sizeIsSticky = false;
    }

    /**
     * 创建一个位集，其初始大小足以显式表示索引在范围 {@code 0} 到
     * {@code nbits-1} 之间的位。所有位最初都是 {@code false}。
     *
     * @param  nbits 位集的初始大小
     * @throws NegativeArraySizeException 如果指定的初始大小为负数
     */
    public BitSet(int nbits) {
        // nbits 不能为负；大小 0 是可以的
        if (nbits < 0)
            throw new NegativeArraySizeException("nbits < 0: " + nbits);

        initWords(nbits);
        sizeIsSticky = true;
    }

    private void initWords(int nbits) {
        words = new long[wordIndex(nbits-1) + 1];
    }

    /**
     * 使用 words 作为内部表示创建一个位集。
     * 最后一个字（如果有）必须非零。
     */
    private BitSet(long[] words) {
        this.words = words;
        this.wordsInUse = words.length;
        checkInvariants();
    }

    /**
     * 返回一个包含给定长整型数组中所有位的新位集。
     *
     * <p>更具体地说，
     * <br>{@code BitSet.valueOf(longs).get(n) == ((longs[n/64] & (1L<<(n%64))) != 0)}
     * <br>对于所有 {@code n < 64 * longs.length}。
     *
     * <p>此方法等同于
     * {@code BitSet.valueOf(LongBuffer.wrap(longs))}。
     *
     * @param longs 包含位集初始位的小端表示的长整型数组
     * @return 包含长整型数组中所有位的 {@code BitSet}
     * @since 1.7
     */
    public static BitSet valueOf(long[] longs) {
        int n;
        for (n = longs.length; n > 0 && longs[n - 1] == 0; n--)
            ;
        return new BitSet(Arrays.copyOf(longs, n));
    }

                /**
     * 返回一个包含给定长整型缓冲区从其位置到限制之间的所有位的新位集。
     *
     * <p>更准确地说，
     * <br>{@code BitSet.valueOf(lb).get(n) == ((lb.get(lb.position()+n/64) & (1L<<(n%64))) != 0)}
     * <br>对于所有 {@code n < 64 * lb.remaining()}。
     *
     * <p>此方法不会修改长整型缓冲区，位集也不会保留对缓冲区的引用。
     *
     * @param lb 包含从其位置到限制之间的位序列的小端表示的长整型缓冲区，用作新位集的初始位
     * @return 包含缓冲区指定范围内所有位的 {@code BitSet}
     * @since 1.7
     */
    public static BitSet valueOf(LongBuffer lb) {
        lb = lb.slice();
        int n;
        for (n = lb.remaining(); n > 0 && lb.get(n - 1) == 0; n--)
            ;
        long[] words = new long[n];
        lb.get(words);
        return new BitSet(words);
    }

    /**
     * 返回一个包含给定字节数组中所有位的新位集。
     *
     * <p>更准确地说，
     * <br>{@code BitSet.valueOf(bytes).get(n) == ((bytes[n/8] & (1<<(n%8))) != 0)}
     * <br>对于所有 {@code n <  8 * bytes.length}。
     *
     * <p>此方法等同于
     * {@code BitSet.valueOf(ByteBuffer.wrap(bytes))}。
     *
     * @param bytes 包含位序列的小端表示的字节数组，用作新位集的初始位
     * @return 包含字节数组中所有位的 {@code BitSet}
     * @since 1.7
     */
    public static BitSet valueOf(byte[] bytes) {
        return BitSet.valueOf(ByteBuffer.wrap(bytes));
    }

    /**
     * 返回一个包含给定字节缓冲区从其位置到限制之间的所有位的新位集。
     *
     * <p>更准确地说，
     * <br>{@code BitSet.valueOf(bb).get(n) == ((bb.get(bb.position()+n/8) & (1<<(n%8))) != 0)}
     * <br>对于所有 {@code n < 8 * bb.remaining()}。
     *
     * <p>此方法不会修改字节缓冲区，位集也不会保留对缓冲区的引用。
     *
     * @param bb 包含从其位置到限制之间的位序列的小端表示的字节缓冲区，用作新位集的初始位
     * @return 包含缓冲区指定范围内所有位的 {@code BitSet}
     * @since 1.7
     */
    public static BitSet valueOf(ByteBuffer bb) {
        bb = bb.slice().order(ByteOrder.LITTLE_ENDIAN);
        int n;
        for (n = bb.remaining(); n > 0 && bb.get(n - 1) == 0; n--)
            ;
        long[] words = new long[(n + 7) / 8];
        bb.limit(n);
        int i = 0;
        while (bb.remaining() >= 8)
            words[i++] = bb.getLong();
        for (int remaining = bb.remaining(), j = 0; j < remaining; j++)
            words[i] |= (bb.get() & 0xffL) << (8 * j);
        return new BitSet(words);
    }

    /**
     * 返回一个包含此位集中所有位的新字节数组。
     *
     * <p>更准确地说，如果
     * <br>{@code byte[] bytes = s.toByteArray();}
     * <br>则 {@code bytes.length == (s.length()+7)/8} 并且
     * <br>{@code s.get(n) == ((bytes[n/8] & (1<<(n%8))) != 0)}
     * <br>对于所有 {@code n < 8 * bytes.length}。
     *
     * @return 包含此位集中所有位的小端表示的字节数组
     * @since 1.7
    */
    public byte[] toByteArray() {
        int n = wordsInUse;
        if (n == 0)
            return new byte[0];
        int len = 8 * (n-1);
        for (long x = words[n - 1]; x != 0; x >>>= 8)
            len++;
        byte[] bytes = new byte[len];
        ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < n - 1; i++)
            bb.putLong(words[i]);
        for (long x = words[n - 1]; x != 0; x >>>= 8)
            bb.put((byte) (x & 0xff));
        return bytes;
    }

    /**
     * 返回一个包含此位集中所有位的新长整型数组。
     *
     * <p>更准确地说，如果
     * <br>{@code long[] longs = s.toLongArray();}
     * <br>则 {@code longs.length == (s.length()+63)/64} 并且
     * <br>{@code s.get(n) == ((longs[n/64] & (1L<<(n%64))) != 0)}
     * <br>对于所有 {@code n < 64 * longs.length}。
     *
     * @return 包含此位集中所有位的小端表示的长整型数组
     * @since 1.7
    */
    public long[] toLongArray() {
        return Arrays.copyOf(words, wordsInUse);
    }

    /**
     * 确保位集可以容纳足够的单词。
     * @param wordsRequired 可接受的最小单词数。
     */
    private void ensureCapacity(int wordsRequired) {
        if (words.length < wordsRequired) {
            // 分配更大的双倍大小或所需大小
            int request = Math.max(2 * words.length, wordsRequired);
            words = Arrays.copyOf(words, request);
            sizeIsSticky = false;
        }
    }

    /**
     * 确保位集可以容纳给定的 wordIndex，暂时违反不变量。调用者必须在返回给用户之前恢复不变量，
     * 可能使用 recalculateWordsInUse()。
     * @param wordIndex 要容纳的索引。
     */
    private void expandTo(int wordIndex) {
        int wordsRequired = wordIndex+1;
        if (wordsInUse < wordsRequired) {
            ensureCapacity(wordsRequired);
            wordsInUse = wordsRequired;
        }
    }

    /**
     * 检查 fromIndex ... toIndex 是否是有效的位索引范围。
     */
    private static void checkRange(int fromIndex, int toIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
        if (toIndex < 0)
            throw new IndexOutOfBoundsException("toIndex < 0: " + toIndex);
        if (fromIndex > toIndex)
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex +
                                                " > toIndex: " + toIndex);
    }

                /**
     * 将指定索引处的位设置为其当前值的补码。
     *
     * @param  bitIndex 要翻转的位的索引
     * @throws IndexOutOfBoundsException 如果指定的索引为负数
     * @since  1.4
     */
    public void flip(int bitIndex) {
        if (bitIndex < 0)
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

        int wordIndex = wordIndex(bitIndex);
        expandTo(wordIndex);

        words[wordIndex] ^= (1L << bitIndex);

        recalculateWordsInUse();
        checkInvariants();
    }

    /**
     * 将从指定的 {@code fromIndex}（包含）到指定的 {@code toIndex}（不包含）之间的每个位设置为其当前值的补码。
     *
     * @param  fromIndex 第一个要翻转的位的索引
     * @param  toIndex 最后一个要翻转的位之后的索引
     * @throws IndexOutOfBoundsException 如果 {@code fromIndex} 为负数，
     *         或 {@code toIndex} 为负数，或 {@code fromIndex} 大于 {@code toIndex}
     * @since  1.4
     */
    public void flip(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);

        if (fromIndex == toIndex)
            return;

        int startWordIndex = wordIndex(fromIndex);
        int endWordIndex   = wordIndex(toIndex - 1);
        expandTo(endWordIndex);

        long firstWordMask = WORD_MASK << fromIndex;
        long lastWordMask  = WORD_MASK >>> -toIndex;
        if (startWordIndex == endWordIndex) {
            // 情况 1: 一个单词
            words[startWordIndex] ^= (firstWordMask & lastWordMask);
        } else {
            // 情况 2: 多个单词
            // 处理第一个单词
            words[startWordIndex] ^= firstWordMask;

            // 处理中间的单词，如果有
            for (int i = startWordIndex+1; i < endWordIndex; i++)
                words[i] ^= WORD_MASK;

            // 处理最后一个单词
            words[endWordIndex] ^= lastWordMask;
        }

        recalculateWordsInUse();
        checkInvariants();
    }

    /**
     * 将指定索引处的位设置为 {@code true}。
     *
     * @param  bitIndex 一个位索引
     * @throws IndexOutOfBoundsException 如果指定的索引为负数
     * @since  JDK1.0
     */
    public void set(int bitIndex) {
        if (bitIndex < 0)
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

        int wordIndex = wordIndex(bitIndex);
        expandTo(wordIndex);

        words[wordIndex] |= (1L << bitIndex); // 恢复不变量

        checkInvariants();
    }

    /**
     * 将指定索引处的位设置为指定的值。
     *
     * @param  bitIndex 一个位索引
     * @param  value 要设置的布尔值
     * @throws IndexOutOfBoundsException 如果指定的索引为负数
     * @since  1.4
     */
    public void set(int bitIndex, boolean value) {
        if (value)
            set(bitIndex);
        else
            clear(bitIndex);
    }

    /**
     * 将从指定的 {@code fromIndex}（包含）到指定的 {@code toIndex}（不包含）之间的位设置为 {@code true}。
     *
     * @param  fromIndex 第一个要设置的位的索引
     * @param  toIndex 最后一个要设置的位之后的索引
     * @throws IndexOutOfBoundsException 如果 {@code fromIndex} 为负数，
     *         或 {@code toIndex} 为负数，或 {@code fromIndex} 大于 {@code toIndex}
     * @since  1.4
     */
    public void set(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);

        if (fromIndex == toIndex)
            return;

        // 必要时增加容量
        int startWordIndex = wordIndex(fromIndex);
        int endWordIndex   = wordIndex(toIndex - 1);
        expandTo(endWordIndex);

        long firstWordMask = WORD_MASK << fromIndex;
        long lastWordMask  = WORD_MASK >>> -toIndex;
        if (startWordIndex == endWordIndex) {
            // 情况 1: 一个单词
            words[startWordIndex] |= (firstWordMask & lastWordMask);
        } else {
            // 情况 2: 多个单词
            // 处理第一个单词
            words[startWordIndex] |= firstWordMask;

            // 处理中间的单词，如果有
            for (int i = startWordIndex+1; i < endWordIndex; i++)
                words[i] = WORD_MASK;

            // 处理最后一个单词（恢复不变量）
            words[endWordIndex] |= lastWordMask;
        }

        checkInvariants();
    }

    /**
     * 将从指定的 {@code fromIndex}（包含）到指定的 {@code toIndex}（不包含）之间的位设置为指定的值。
     *
     * @param  fromIndex 第一个要设置的位的索引
     * @param  toIndex 最后一个要设置的位之后的索引
     * @param  value 要设置选定位的值
     * @throws IndexOutOfBoundsException 如果 {@code fromIndex} 为负数，
     *         或 {@code toIndex} 为负数，或 {@code fromIndex} 大于 {@code toIndex}
     * @since  1.4
     */
    public void set(int fromIndex, int toIndex, boolean value) {
        if (value)
            set(fromIndex, toIndex);
        else
            clear(fromIndex, toIndex);
    }

    /**
     * 将由索引指定的位设置为 {@code false}。
     *
     * @param  bitIndex 要清除的位的索引
     * @throws IndexOutOfBoundsException 如果指定的索引为负数
     * @since  JDK1.0
     */
    public void clear(int bitIndex) {
        if (bitIndex < 0)
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

        int wordIndex = wordIndex(bitIndex);
        if (wordIndex >= wordsInUse)
            return;

        words[wordIndex] &= ~(1L << bitIndex);

        recalculateWordsInUse();
        checkInvariants();
    }

    /**
     * 将从指定的 {@code fromIndex}（包含）到指定的 {@code toIndex}（不包含）之间的位设置为 {@code false}。
     *
     * @param  fromIndex 第一个要清除的位的索引
     * @param  toIndex 最后一个要清除的位之后的索引
     * @throws IndexOutOfBoundsException 如果 {@code fromIndex} 为负数，
     *         或 {@code toIndex} 为负数，或 {@code fromIndex} 大于 {@code toIndex}
     * @since  1.4
     */
    public void clear(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);


                    if (fromIndex == toIndex)
            return;

        int startWordIndex = wordIndex(fromIndex);
        if (startWordIndex >= wordsInUse)
            return;

        int endWordIndex = wordIndex(toIndex - 1);
        if (endWordIndex >= wordsInUse) {
            toIndex = length();
            endWordIndex = wordsInUse - 1;
        }

        long firstWordMask = WORD_MASK << fromIndex;
        long lastWordMask  = WORD_MASK >>> -toIndex;
        if (startWordIndex == endWordIndex) {
            // 情况 1: 一个单词
            words[startWordIndex] &= ~(firstWordMask & lastWordMask);
        } else {
            // 情况 2: 多个单词
            // 处理第一个单词
            words[startWordIndex] &= ~firstWordMask;

            // 处理中间的单词，如果有
            for (int i = startWordIndex+1; i < endWordIndex; i++)
                words[i] = 0;

            // 处理最后一个单词
            words[endWordIndex] &= ~lastWordMask;
        }

        recalculateWordsInUse();
        checkInvariants();
    }

    /**
     * 将此 BitSet 中的所有位设置为 {@code false}。
     *
     * @since 1.4
     */
    public void clear() {
        while (wordsInUse > 0)
            words[--wordsInUse] = 0;
    }

    /**
     * 返回指定索引的位的值。如果此 {@code BitSet} 中索引为 {@code bitIndex} 的位当前已设置，
     * 则值为 {@code true}；否则，结果为 {@code false}。
     *
     * @param  bitIndex   位索引
     * @return 指定索引的位的值
     * @throws IndexOutOfBoundsException 如果指定的索引为负数
     */
    public boolean get(int bitIndex) {
        if (bitIndex < 0)
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

        checkInvariants();

        int wordIndex = wordIndex(bitIndex);
        return (wordIndex < wordsInUse)
            && ((words[wordIndex] & (1L << bitIndex)) != 0);
    }

    /**
     * 返回一个由此 {@code BitSet} 中从 {@code fromIndex}（包含）到 {@code toIndex}（不包含）的位组成的新 {@code BitSet}。
     *
     * @param  fromIndex 第一个包含的位的索引
     * @param  toIndex 最后一个包含的位之后的索引
     * @return 一个从此 {@code BitSet} 的范围创建的新 {@code BitSet}
     * @throws IndexOutOfBoundsException 如果 {@code fromIndex} 为负数，或 {@code toIndex} 为负数，或 {@code fromIndex} 大于 {@code toIndex}
     * @since  1.4
     */
    public BitSet get(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);

        checkInvariants();

        int len = length();

        // 如果范围内没有设置的位，则返回空的 BitSet
        if (len <= fromIndex || fromIndex == toIndex)
            return new BitSet(0);

        // 优化
        if (toIndex > len)
            toIndex = len;

        BitSet result = new BitSet(toIndex - fromIndex);
        int targetWords = wordIndex(toIndex - fromIndex - 1) + 1;
        int sourceIndex = wordIndex(fromIndex);
        boolean wordAligned = ((fromIndex & BIT_INDEX_MASK) == 0);

        // 处理所有但最后一个单词
        for (int i = 0; i < targetWords - 1; i++, sourceIndex++)
            result.words[i] = wordAligned ? words[sourceIndex] :
                (words[sourceIndex] >>> fromIndex) |
                (words[sourceIndex+1] << -fromIndex);

        // 处理最后一个单词
        long lastWordMask = WORD_MASK >>> -toIndex;
        result.words[targetWords - 1] =
            ((toIndex-1) & BIT_INDEX_MASK) < (fromIndex & BIT_INDEX_MASK)
            ? /* 跨越源单词 */
            ((words[sourceIndex] >>> fromIndex) |
             (words[sourceIndex+1] & lastWordMask) << -fromIndex)
            :
            ((words[sourceIndex] & lastWordMask) >>> fromIndex);

        // 正确设置 wordsInUse
        result.wordsInUse = targetWords;
        result.recalculateWordsInUse();
        result.checkInvariants();

        return result;
    }

    /**
     * 返回从指定的起始索引开始或之后第一个设置为 {@code true} 的位的索引。如果不存在这样的位，则返回 {@code -1}。
     *
     * <p>要迭代 {@code BitSet} 中的 {@code true} 位，使用以下循环：
     *
     *  <pre> {@code
     * for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
     *     // 在这里操作索引 i
     *     if (i == Integer.MAX_VALUE) {
     *         break; // 或 (i+1) 会溢出
     *     }
     * }}</pre>
     *
     * @param  fromIndex 要开始检查的索引（包含）
     * @return 下一个设置的位的索引，或如果不存在这样的位则返回 {@code -1}
     * @throws IndexOutOfBoundsException 如果指定的索引为负数
     * @since  1.4
     */
    public int nextSetBit(int fromIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);

        checkInvariants();

        int u = wordIndex(fromIndex);
        if (u >= wordsInUse)
            return -1;

        long word = words[u] & (WORD_MASK << fromIndex);

        while (true) {
            if (word != 0)
                return (u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
            if (++u == wordsInUse)
                return -1;
            word = words[u];
        }
    }

    /**
     * 返回从指定的起始索引开始或之后第一个设置为 {@code false} 的位的索引。
     *
     * @param  fromIndex 要开始检查的索引（包含）
     * @return 下一个未设置的位的索引
     * @throws IndexOutOfBoundsException 如果指定的索引为负数
     * @since  1.4
     */
    public int nextClearBit(int fromIndex) {
        // 既没有规范也没有实现处理最大长度的 BitSet。
        // 参见 4816253。
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);

        checkInvariants();


                    int u = wordIndex(fromIndex);
        if (u >= wordsInUse)
            return fromIndex;

        long word = ~words[u] & (WORD_MASK << fromIndex);

        while (true) {
            if (word != 0)
                return (u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
            if (++u == wordsInUse)
                return wordsInUse * BITS_PER_WORD;
            word = ~words[u];
        }
    }

    /**
     * 返回指定起始索引之前（包括该索引）最近设置为 {@code true} 的位的索引。
     * 如果不存在这样的位，或者给定的起始索引为 {@code -1}，则返回 {@code -1}。
     *
     * <p>要迭代 {@code BitSet} 中的 {@code true} 位，可以使用以下循环：
     *
     *  <pre> {@code
     * for (int i = bs.length(); (i = bs.previousSetBit(i-1)) >= 0; ) {
     *     // 在这里操作索引 i
     * }}</pre>
     *
     * @param  fromIndex 要开始检查的索引（包括该索引）
     * @return 之前设置的位的索引，如果不存在这样的位则返回 {@code -1}
     * @throws IndexOutOfBoundsException 如果指定的索引小于 {@code -1}
     * @since  1.7
     */
    public int previousSetBit(int fromIndex) {
        if (fromIndex < 0) {
            if (fromIndex == -1)
                return -1;
            throw new IndexOutOfBoundsException(
                "fromIndex < -1: " + fromIndex);
        }

        checkInvariants();

        int u = wordIndex(fromIndex);
        if (u >= wordsInUse)
            return length() - 1;

        long word = words[u] & (WORD_MASK >>> -(fromIndex+1));

        while (true) {
            if (word != 0)
                return (u+1) * BITS_PER_WORD - 1 - Long.numberOfLeadingZeros(word);
            if (u-- == 0)
                return -1;
            word = words[u];
        }
    }

    /**
     * 返回指定起始索引之前（包括该索引）最近设置为 {@code false} 的位的索引。
     * 如果不存在这样的位，或者给定的起始索引为 {@code -1}，则返回 {@code -1}。
     *
     * @param  fromIndex 要开始检查的索引（包括该索引）
     * @return 之前清除的位的索引，如果不存在这样的位则返回 {@code -1}
     * @throws IndexOutOfBoundsException 如果指定的索引小于 {@code -1}
     * @since  1.7
     */
    public int previousClearBit(int fromIndex) {
        if (fromIndex < 0) {
            if (fromIndex == -1)
                return -1;
            throw new IndexOutOfBoundsException(
                "fromIndex < -1: " + fromIndex);
        }

        checkInvariants();

        int u = wordIndex(fromIndex);
        if (u >= wordsInUse)
            return fromIndex;

        long word = ~words[u] & (WORD_MASK >>> -(fromIndex+1));

        while (true) {
            if (word != 0)
                return (u+1) * BITS_PER_WORD -1 - Long.numberOfLeadingZeros(word);
            if (u-- == 0)
                return -1;
            word = ~words[u];
        }
    }

    /**
     * 返回此 {@code BitSet} 的“逻辑大小”：即 {@code BitSet} 中最高设置位的索引加一。如果 {@code BitSet} 中没有设置的位，则返回零。
     *
     * @return 此 {@code BitSet} 的逻辑大小
     * @since  1.2
     */
    public int length() {
        if (wordsInUse == 0)
            return 0;

        return BITS_PER_WORD * (wordsInUse - 1) +
            (BITS_PER_WORD - Long.numberOfLeadingZeros(words[wordsInUse - 1]));
    }

    /**
     * 如果此 {@code BitSet} 中没有设置为 {@code true} 的位，则返回 true。
     *
     * @return 表示此 {@code BitSet} 是否为空的布尔值
     * @since  1.4
     */
    public boolean isEmpty() {
        return wordsInUse == 0;
    }

    /**
     * 如果指定的 {@code BitSet} 中有任意位设置为 {@code true}，并且这些位在当前 {@code BitSet} 中也设置为 {@code true}，则返回 true。
     *
     * @param  set 要与当前 {@code BitSet} 进行交集的 {@code BitSet}
     * @return 表示此 {@code BitSet} 是否与指定的 {@code BitSet} 有交集的布尔值
     * @since  1.4
     */
    public boolean intersects(BitSet set) {
        for (int i = Math.min(wordsInUse, set.wordsInUse) - 1; i >= 0; i--)
            if ((words[i] & set.words[i]) != 0)
                return true;
        return false;
    }

    /**
     * 返回此 {@code BitSet} 中设置为 {@code true} 的位的数量。
     *
     * @return 此 {@code BitSet} 中设置为 {@code true} 的位的数量
     * @since  1.4
     */
    public int cardinality() {
        int sum = 0;
        for (int i = 0; i < wordsInUse; i++)
            sum += Long.bitCount(words[i]);
        return sum;
    }

    /**
     * 对此目标位集与参数位集执行逻辑 <b>AND</b> 操作。此位集被修改，使得其中的每个位只有在最初为 {@code true} 且参数位集中的相应位也为 {@code true} 时才为 {@code true}。
     *
     * @param set 一个位集
     */
    public void and(BitSet set) {
        if (this == set)
            return;

        while (wordsInUse > set.wordsInUse)
            words[--wordsInUse] = 0;

        // 对共同的单词执行逻辑 AND 操作
        for (int i = 0; i < wordsInUse; i++)
            words[i] &= set.words[i];

        recalculateWordsInUse();
        checkInvariants();
    }

    /**
     * 对此位集与参数位集执行逻辑 <b>OR</b> 操作。此位集被修改，使得其中的每个位只有在最初为 {@code true} 或参数位集中的相应位为 {@code true} 时才为 {@code true}。
     *
     * @param set 一个位集
     */
    public void or(BitSet set) {
        if (this == set)
            return;

        int wordsInCommon = Math.min(wordsInUse, set.wordsInUse);


                    if (wordsInUse < set.wordsInUse) {
            ensureCapacity(set.wordsInUse);
            wordsInUse = set.wordsInUse;
        }

        // 对公共的单词执行逻辑或操作
        for (int i = 0; i < wordsInCommon; i++)
            words[i] |= set.words[i];

        // 复制任何剩余的单词
        if (wordsInCommon < set.wordsInUse)
            System.arraycopy(set.words, wordsInCommon,
                             words, wordsInCommon,
                             wordsInUse - wordsInCommon);

        // 重新计算wordsInUse()是不必要的
        checkInvariants();
    }

    /**
     * 对此位集与位集参数执行逻辑<b>XOR</b>操作。此位集被修改，使得其中的位仅当以下任一条件成立时才具有值{@code true}：
     * <ul>
     * <li>该位最初具有值{@code true}，而参数中对应的位具有值{@code false}。
     * <li>该位最初具有值{@code false}，而参数中对应的位具有值{@code true}。
     * </ul>
     *
     * @param  set 一个位集
     */
    public void xor(BitSet set) {
        int wordsInCommon = Math.min(wordsInUse, set.wordsInUse);

        if (wordsInUse < set.wordsInUse) {
            ensureCapacity(set.wordsInUse);
            wordsInUse = set.wordsInUse;
        }

        // 对公共的单词执行逻辑XOR操作
        for (int i = 0; i < wordsInCommon; i++)
            words[i] ^= set.words[i];

        // 复制任何剩余的单词
        if (wordsInCommon < set.wordsInUse)
            System.arraycopy(set.words, wordsInCommon,
                             words, wordsInCommon,
                             set.wordsInUse - wordsInCommon);

        recalculateWordsInUse();
        checkInvariants();
    }

    /**
     * 清除此{@code BitSet}中所有在指定的{@code BitSet}中对应的位被设置的位。
     *
     * @param  set 用于屏蔽此{@code BitSet}的{@code BitSet}
     * @since  1.2
     */
    public void andNot(BitSet set) {
        // 对公共的单词执行逻辑(a & !b)操作
        for (int i = Math.min(wordsInUse, set.wordsInUse) - 1; i >= 0; i--)
            words[i] &= ~set.words[i];

        recalculateWordsInUse();
        checkInvariants();
    }

    /**
     * 返回此位集的哈希码值。哈希码仅取决于此{@code BitSet}中哪些位被设置。
     *
     * <p>哈希码定义为以下计算的结果：
     *  <pre> {@code
     * public int hashCode() {
     *     long h = 1234;
     *     long[] words = toLongArray();
     *     for (int i = words.length; --i >= 0; )
     *         h ^= words[i] * (i + 1);
     *     return (int)((h >> 32) ^ h);
     * }}</pre>
     * 注意，如果位集中的位被更改，哈希码也会改变。
     *
     * @return 此位集的哈希码值
     */
    public int hashCode() {
        long h = 1234;
        for (int i = wordsInUse; --i >= 0; )
            h ^= words[i] * (i + 1);

        return (int)((h >> 32) ^ h);
    }

    /**
     * 返回此{@code BitSet}实际用于表示位值的位数。
     * 集合中的最大元素是大小 - 1的元素。
     *
     * @return 当前此位集中的位数
     */
    public int size() {
        return words.length * BITS_PER_WORD;
    }

    /**
     * 将此对象与指定的对象进行比较。
     * 结果为{@code true}当且仅当参数不是{@code null}且是一个具有与此位集完全相同的位设置为{@code true}的{@code BitSet}对象。
     * 即，对于每个非负的{@code int}索引{@code k}，
     * <pre>((BitSet)obj).get(k) == this.get(k)</pre>
     * 必须为真。两个位集的当前大小不进行比较。
     *
     * @param  obj 要比较的对象
     * @return 如果对象相同则为{@code true}；否则为{@code false}
     * @see    #size()
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof BitSet))
            return false;
        if (this == obj)
            return true;

        BitSet set = (BitSet) obj;

        checkInvariants();
        set.checkInvariants();

        if (wordsInUse != set.wordsInUse)
            return false;

        // 检查两个BitSet都使用的单词
        for (int i = 0; i < wordsInUse; i++)
            if (words[i] != set.words[i])
                return false;

        return true;
    }

    /**
     * 克隆此{@code BitSet}生成一个新的与之相等的{@code BitSet}。
     * 位集的克隆是另一个位集，它具有与这个位集完全相同的位设置为{@code true}。
     *
     * @return 位集的克隆
     * @see    #size()
     */
    public Object clone() {
        if (! sizeIsSticky)
            trimToSize();

        try {
            BitSet result = (BitSet) super.clone();
            result.words = words.clone();
            result.checkInvariants();
            return result;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /**
     * 尝试减少此位集内部用于位的存储。
     * 调用此方法可能，但不要求，影响后续调用{@link #size()}方法返回的值。
     */
    private void trimToSize() {
        if (wordsInUse != words.length) {
            words = Arrays.copyOf(words, wordsInUse);
            checkInvariants();
        }
    }

    /**
     * 将{@code BitSet}实例的状态保存到流中（即，序列化它）。
     */
    private void writeObject(ObjectOutputStream s)
        throws IOException {

        checkInvariants();

        if (! sizeIsSticky)
            trimToSize();

        ObjectOutputStream.PutField fields = s.putFields();
        fields.put("bits", words);
        s.writeFields();
    }

                /**
     * 从流中重构 {@code BitSet} 实例（即，反序列化它）。
     */
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {

        ObjectInputStream.GetField fields = s.readFields();
        words = (long[]) fields.get("bits", null);

        // 假设最大长度然后找到实际长度
        // 因为 recalculateWordsInUse 假设维护或减少逻辑大小
        wordsInUse = words.length;
        recalculateWordsInUse();
        sizeIsSticky = (words.length > 0 && words[words.length-1] == 0L); // 启发式方法
        checkInvariants();
    }

    /**
     * 返回此位集的字符串表示形式。对于此 {@code BitSet} 中设置状态的每个索引，
     * 该索引的十进制表示形式将包含在结果中。这些索引按从低到高的顺序列出，
     * 由 ",&nbsp;"（逗号和空格）分隔，并被大括号包围，形成通常的数学整数集合表示法。
     *
     * <p>示例：
     * <pre>
     * BitSet drPepper = new BitSet();</pre>
     * 现在 {@code drPepper.toString()} 返回 "{@code {}}"。
     * <pre>
     * drPepper.set(2);</pre>
     * 现在 {@code drPepper.toString()} 返回 "{@code {2}}"。
     * <pre>
     * drPepper.set(4);
     * drPepper.set(10);</pre>
     * 现在 {@code drPepper.toString()} 返回 "{@code {2, 4, 10}}"。
     *
     * @return 此位集的字符串表示形式
     */
    public String toString() {
        checkInvariants();

        int numBits = (wordsInUse > 128) ?
            cardinality() : wordsInUse * BITS_PER_WORD;
        StringBuilder b = new StringBuilder(6*numBits + 2);
        b.append('{');

        int i = nextSetBit(0);
        if (i != -1) {
            b.append(i);
            while (true) {
                if (++i < 0) break;
                if ((i = nextSetBit(i)) < 0) break;
                int endOfRun = nextClearBit(i);
                do { b.append(", ").append(i); }
                while (++i != endOfRun);
            }
        }

        b.append('}');
        return b.toString();
    }

    /**
     * 返回一个流，其中包含此 {@code BitSet} 中处于设置状态的位的索引。索引按顺序返回，
     * 从最低到最高。流的大小是处于设置状态的位数，等于 {@link #cardinality()} 方法返回的值。
     *
     * <p>在执行终端流操作期间，位集必须保持不变。否则，终端流操作的结果是未定义的。
     *
     * @return 一个表示设置索引的整数流
     * @since 1.8
     */
    public IntStream stream() {
        class BitSetIterator implements PrimitiveIterator.OfInt {
            int next = nextSetBit(0);

            @Override
            public boolean hasNext() {
                return next != -1;
            }

            @Override
            public int nextInt() {
                if (next != -1) {
                    int ret = next;
                    next = nextSetBit(next+1);
                    return ret;
                } else {
                    throw new NoSuchElementException();
                }
            }
        }

        return StreamSupport.intStream(
                () -> Spliterators.spliterator(
                        new BitSetIterator(), cardinality(),
                        Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SORTED),
                Spliterator.SIZED | Spliterator.SUBSIZED |
                        Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SORTED,
                false);
    }
}
