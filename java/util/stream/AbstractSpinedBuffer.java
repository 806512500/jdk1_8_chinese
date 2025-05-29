/*
 * 版权所有 (c) 2012, 2013, Oracle 和/或其附属公司。保留所有权利。
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
package java.util.stream;

/**
 * 用于将元素收集到缓冲区中然后迭代它们的数据结构的基类。维护一个逐渐增大的数组数组，因此数据结构增长时没有复制成本。
 * @since 1.8
 */
abstract class AbstractSpinedBuffer {
    /**
     * 第一个块的最小2的幂。
     */
    public static final int MIN_CHUNK_POWER = 4;

    /**
     * 第一个块的最小大小。
     */
    public static final int MIN_CHUNK_SIZE = 1 << MIN_CHUNK_POWER;

    /**
     * 块的最大2的幂。
     */
    public static final int MAX_CHUNK_POWER = 30;

    /**
     * 块数组的最小数组大小。
     */
    public static final int MIN_SPINE_SIZE = 8;

    /**
     * 第一个块的大小的log2。
     */
    protected final int initialChunkPower;

    /**
     * 要写入的*下一个*元素的索引；可能指向当前块内或外。
     */
    protected int elementIndex;

    /**
     * 如果脊椎数组非空，则脊椎数组中*当前*块的索引。
     */
    protected int spineIndex;

    /**
     * 所有先前块中的元素计数。
     */
    protected long[] priorElementCount;

    /**
     * 使用初始容量16构造。
     */
    protected AbstractSpinedBuffer() {
        this.initialChunkPower = MIN_CHUNK_POWER;
    }

    /**
     * 使用指定的初始容量构造。
     *
     * @param initialCapacity 预期的最小元素数
     */
    protected AbstractSpinedBuffer(int initialCapacity) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("非法容量: "+ initialCapacity);

        this.initialChunkPower = Math.max(MIN_CHUNK_POWER,
                                          Integer.SIZE - Integer.numberOfLeadingZeros(initialCapacity - 1));
    }

    /**
     * 缓冲区当前是否为空？
     */
    public boolean isEmpty() {
        return (spineIndex == 0) && (elementIndex == 0);
    }

    /**
     * 缓冲区中当前有多少元素？
     */
    public long count() {
        return (spineIndex == 0)
               ? elementIndex
               : priorElementCount[spineIndex] + elementIndex;
    }

    /**
     * 第n个块应该有多大？
     */
    protected int chunkSize(int n) {
        int power = (n == 0 || n == 1)
                    ? initialChunkPower
                    : Math.min(initialChunkPower + n - 1, AbstractSpinedBuffer.MAX_CHUNK_POWER);
        return 1 << power;
    }

    /**
     * 从缓冲区中移除所有数据
     */
    public abstract void clear();
}
