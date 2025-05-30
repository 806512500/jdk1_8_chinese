/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
package java.util.stream;

/**
 * 用于将元素收集到缓冲区并迭代这些元素的数据结构的基类。维护一个逐渐增大的数组数组，因此在扩展数据结构时没有复制成本。
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
     * 块数组的最小大小。
     */
    public static final int MIN_SPINE_SIZE = 8;

    /**
     * 第一个块的大小的log2。
     */
    protected final int initialChunkPower;

    /**
     * 下一个要写入的元素的索引；可能指向当前块内或块外。
     */
    protected int elementIndex;

    /**
     * 如果脊椎数组非空，则脊椎数组中当前块的索引。
     */
    protected int spineIndex;

    /**
     * 所有先前块中的元素计数。
     */
    protected long[] priorElementCount;

    /**
     * 使用初始容量为16构建。
     */
    protected AbstractSpinedBuffer() {
        this.initialChunkPower = MIN_CHUNK_POWER;
    }

    /**
     * 使用指定的初始容量构建。
     *
     * @param initialCapacity 预期的最小元素数量
     */
    protected AbstractSpinedBuffer(int initialCapacity) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("非法容量: " + initialCapacity);

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
     * 当前缓冲区中有多少个元素？
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
     * 从缓冲区中移除所有数据。
     */
    public abstract void clear();
}
