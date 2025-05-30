
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;

/**
 * 一个有序的元素集合。元素可以添加，但不能移除。
 * 经历一个构建阶段，在此期间可以添加元素，以及一个遍历阶段，在此期间可以按顺序遍历元素，但不允许进一步修改。
 *
 * <p> 使用一个或多个数组来存储元素。使用多个数组的性能特性优于 {@link ArrayList} 使用的单个数组，
 * 因为当列表的容量需要增加时，不需要复制元素。这在结果将被遍历少量次数的情况下通常是有利的。
 *
 * @param <E> 此列表中的元素类型
 * @since 1.8
 */
class SpinedBuffer<E>
        extends AbstractSpinedBuffer
        implements Consumer<E>, Iterable<E> {

    /*
     * 我们乐观地希望所有数据都能适应第一个块，因此我们尽量避免过早地扩展 spine[] 和 priorElementCount[] 数组。
     * 因此方法必须准备好处理这些数组为空的情况。如果 spine 非空，则 spineIndex 指向 spine 中的当前块，
     * 否则它为零。spine 和 priorElementCount 数组的大小总是相同的，对于任何 i <= spineIndex，
     * priorElementCount[i] 是所有先前块大小的总和。
     *
     * curChunk 指针始终有效。elementIndex 是 curChunk 中下一个要写入的元素的索引；这可能超出 curChunk 的末尾，
     * 因此在写入前需要检查。当我们扩展 spine 数组时，curChunk 成为其第一个元素。当我们清除缓冲区时，
     * 我们丢弃所有块，除了第一个块，我们清除它，恢复到初始的单块状态。
     */

    /**
     * 当前正在写入的块；可能与 spine 的第一个元素相同，也可能不同。
     */
    protected E[] curChunk;

    /**
     * 所有块，或者如果只有一个块则为 null。
     */
    protected E[][] spine;

    /**
     * 构造一个具有指定初始容量的空列表。
     *
     * @param  initialCapacity  列表的初始容量
     * @throws IllegalArgumentException 如果指定的初始容量为负数
     */
    @SuppressWarnings("unchecked")
    SpinedBuffer(int initialCapacity) {
        super(initialCapacity);
        curChunk = (E[]) new Object[1 << initialChunkPower];
    }

    /**
     * 构造一个初始容量为十六的空列表。
     */
    @SuppressWarnings("unchecked")
    SpinedBuffer() {
        super();
        curChunk = (E[]) new Object[1 << initialChunkPower];
    }

    /**
     * 返回缓冲区的当前容量
     */
    protected long capacity() {
        return (spineIndex == 0)
               ? curChunk.length
               : priorElementCount[spineIndex] + spine[spineIndex].length;
    }

    @SuppressWarnings("unchecked")
    private void inflateSpine() {
        if (spine == null) {
            spine = (E[][]) new Object[MIN_SPINE_SIZE][];
            priorElementCount = new long[MIN_SPINE_SIZE];
            spine[0] = curChunk;
        }
    }

    /**
     * 确保缓冲区至少具有目标大小的容量
     */
    @SuppressWarnings("unchecked")
    protected final void ensureCapacity(long targetSize) {
        long capacity = capacity();
        if (targetSize > capacity) {
            inflateSpine();
            for (int i=spineIndex+1; targetSize > capacity; i++) {
                if (i >= spine.length) {
                    int newSpineSize = spine.length * 2;
                    spine = Arrays.copyOf(spine, newSpineSize);
                    priorElementCount = Arrays.copyOf(priorElementCount, newSpineSize);
                }
                int nextChunkSize = chunkSize(i);
                spine[i] = (E[]) new Object[nextChunkSize];
                priorElementCount[i] = priorElementCount[i-1] + spine[i-1].length;
                capacity += nextChunkSize;
            }
        }
    }

    /**
     * 强制缓冲区增加其容量。
     */
    protected void increaseCapacity() {
        ensureCapacity(capacity() + 1);
    }

    /**
     * 检索指定索引处的元素。
     */
    public E get(long index) {
        // @@@ 可以通过缓存最后看到的 spineIndex 进一步优化，
        // 大多数情况下它会是正确的

        // 由于 spine 数组索引是当前 spine 的索引减去之前的元素计数，因此转换为 int 是安全的
        if (spineIndex == 0) {
            if (index < elementIndex)
                return curChunk[((int) index)];
            else
                throw new IndexOutOfBoundsException(Long.toString(index));
        }

        if (index >= count())
            throw new IndexOutOfBoundsException(Long.toString(index));

        for (int j=0; j <= spineIndex; j++)
            if (index < priorElementCount[j] + spine[j].length)
                return spine[j][((int) (index - priorElementCount[j]))];

        throw new IndexOutOfBoundsException(Long.toString(index));
    }

    /**
     * 从指定偏移量开始，将元素复制到指定的数组中。
     */
    public void copyInto(E[] array, int offset) {
        long finalOffset = offset + count();
        if (finalOffset > array.length || finalOffset < offset) {
            throw new IndexOutOfBoundsException("does not fit");
        }

        if (spineIndex == 0)
            System.arraycopy(curChunk, 0, array, offset, elementIndex);
        else {
            // 完整的块
            for (int i=0; i < spineIndex; i++) {
                System.arraycopy(spine[i], 0, array, offset, spine[i].length);
                offset += spine[i].length;
            }
            if (elementIndex > 0)
                System.arraycopy(curChunk, 0, array, offset, elementIndex);
        }
    }

    /**
     * 使用指定的数组工厂创建一个新数组，并将元素复制到其中。
     */
    public E[] asArray(IntFunction<E[]> arrayFactory) {
        long size = count();
        if (size >= Nodes.MAX_ARRAY_SIZE)
            throw new IllegalArgumentException(Nodes.BAD_SIZE);
        E[] result = arrayFactory.apply((int) size);
        copyInto(result, 0);
        return result;
    }

    @Override
    public void clear() {
        if (spine != null) {
            curChunk = spine[0];
            for (int i=0; i<curChunk.length; i++)
                curChunk[i] = null;
            spine = null;
            priorElementCount = null;
        }
        else {
            for (int i=0; i<elementIndex; i++)
                curChunk[i] = null;
        }
        elementIndex = 0;
        spineIndex = 0;
    }

    @Override
    public Iterator<E> iterator() {
        return Spliterators.iterator(spliterator());
    }

    @Override
    public void forEach(Consumer<? super E> consumer) {
        // 完整的块，如果有
        for (int j = 0; j < spineIndex; j++)
            for (E t : spine[j])
                consumer.accept(t);

        // 当前块
        for (int i=0; i<elementIndex; i++)
            consumer.accept(curChunk[i]);
    }

    @Override
    public void accept(E e) {
        if (elementIndex == curChunk.length) {
            inflateSpine();
            if (spineIndex+1 >= spine.length || spine[spineIndex+1] == null)
                increaseCapacity();
            elementIndex = 0;
            ++spineIndex;
            curChunk = spine[spineIndex];
        }
        curChunk[elementIndex++] = e;
    }

    @Override
    public String toString() {
        List<E> list = new ArrayList<>();
        forEach(list::add);
        return "SpinedBuffer:" + list.toString();
    }

    private static final int SPLITERATOR_CHARACTERISTICS
            = Spliterator.SIZED | Spliterator.ORDERED | Spliterator.SUBSIZED;

    /**
     * 返回描述缓冲区内容的 {@link Spliterator}。
     */
    public Spliterator<E> spliterator() {
        class Splitr implements Spliterator<E> {
            // 当前 spine 索引
            int splSpineIndex;

            // 最后一个 spine 索引
            final int lastSpineIndex;

            // 当前 spine 中的当前元素索引
            int splElementIndex;

            // 最后一个 spine 的最后一个元素索引 + 1
            final int lastSpineElementFence;

            // 当 splSpineIndex >= lastSpineIndex 且
            // splElementIndex >= lastSpineElementFence 时，
            // 此 spliterator 已完全遍历
            // tryAdvance 可以将 splSpineIndex 设置为大于 spineIndex，如果最后一个 spine 是满的

            // 当前 spine 数组
            E[] splChunk;

            Splitr(int firstSpineIndex, int lastSpineIndex,
                   int firstSpineElementIndex, int lastSpineElementFence) {
                this.splSpineIndex = firstSpineIndex;
                this.lastSpineIndex = lastSpineIndex;
                this.splElementIndex = firstSpineElementIndex;
                this.lastSpineElementFence = lastSpineElementFence;
                assert spine != null || firstSpineIndex == 0 && lastSpineIndex == 0;
                splChunk = (spine == null) ? curChunk : spine[firstSpineIndex];
            }

            @Override
            public long estimateSize() {
                return (splSpineIndex == lastSpineIndex)
                       ? (long) lastSpineElementFence - splElementIndex
                       : // 到结束前的元素数 -
                       priorElementCount[lastSpineIndex] + lastSpineElementFence -
                       // 到当前的元素数
                       priorElementCount[splSpineIndex] - splElementIndex;
            }

            @Override
            public int characteristics() {
                return SPLITERATOR_CHARACTERISTICS;
            }

            @Override
            public boolean tryAdvance(Consumer<? super E> consumer) {
                Objects.requireNonNull(consumer);

                if (splSpineIndex < lastSpineIndex
                    || (splSpineIndex == lastSpineIndex && splElementIndex < lastSpineElementFence)) {
                    consumer.accept(splChunk[splElementIndex++]);

                    if (splElementIndex == splChunk.length) {
                        splElementIndex = 0;
                        ++splSpineIndex;
                        if (spine != null && splSpineIndex <= lastSpineIndex)
                            splChunk = spine[splSpineIndex];
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void forEachRemaining(Consumer<? super E> consumer) {
                Objects.requireNonNull(consumer);

                if (splSpineIndex < lastSpineIndex
                    || (splSpineIndex == lastSpineIndex && splElementIndex < lastSpineElementFence)) {
                    int i = splElementIndex;
                    // 完整的块，如果有
                    for (int sp = splSpineIndex; sp < lastSpineIndex; sp++) {
                        E[] chunk = spine[sp];
                        for (; i < chunk.length; i++) {
                            consumer.accept(chunk[i]);
                        }
                        i = 0;
                    }
                    // 最后一个（或当前未完成的）块
                    E[] chunk = (splSpineIndex == lastSpineIndex) ? splChunk : spine[lastSpineIndex];
                    int hElementIndex = lastSpineElementFence;
                    for (; i < hElementIndex; i++) {
                        consumer.accept(chunk[i]);
                    }
                    // 标记已消费
                    splSpineIndex = lastSpineIndex;
                    splElementIndex = lastSpineElementFence;
                }
            }

            @Override
            public Spliterator<E> trySplit() {
                if (splSpineIndex < lastSpineIndex) {
                    // 在最后一个块之前拆分（如果它是满的，这意味着 50:50 拆分）
                    Spliterator<E> ret = new Splitr(splSpineIndex, lastSpineIndex - 1,
                                                    splElementIndex, spine[lastSpineIndex-1].length);
                    // 定位到最后一个块的开始
                    splSpineIndex = lastSpineIndex;
                    splElementIndex = 0;
                    splChunk = spine[splSpineIndex];
                    return ret;
                }
                else if (splSpineIndex == lastSpineIndex) {
                    int t = (lastSpineElementFence - splElementIndex) / 2;
                    if (t == 0)
                        return null;
                    else {
                        Spliterator<E> ret = Arrays.spliterator(splChunk, splElementIndex, splElementIndex + t);
                        splElementIndex += t;
                        return ret;
                    }
                }
                else {
                    return null;
                }
            }
        }
        return new Splitr(0, spineIndex, 0, elementIndex);
    }

    /**
     * 一个有序的原始值集合。元素可以添加，但不能移除。经历一个构建阶段，在此期间可以添加元素，以及一个遍历阶段，在此期间可以按顺序遍历元素，但不允许进一步修改。
     *
     * <p> 使用一个或多个数组来存储元素。使用多个数组的性能特性优于 {@link ArrayList} 使用的单个数组，
     * 因为当列表的容量需要增加时，不需要复制元素。这在结果将被遍历少量次数的情况下通常是有利的。
     *
     * @param <E> 此原始类型的包装类型
     * @param <T_ARR> 此原始类型的数组类型
     * @param <T_CONS> 此原始类型的 Consumer 类型
     */
    abstract static class OfPrimitive<E, T_ARR, T_CONS>
            extends AbstractSpinedBuffer implements Iterable<E> {


                    /*
         * 我们乐观地希望所有数据都能装进第一个块中，
         * 因此我们尽量避免过早地扩展 spine[] 和 priorElementCount[] 数组。
         * 因此方法必须准备好处理这些数组为 null 的情况。
         * 如果 spine 不为 null，则 spineIndex 指向 spine 中的当前块，
         * 否则它为零。spine 和 priorElementCount 数组总是相同大小，
         * 并且对于任何 i <= spineIndex，priorElementCount[i] 是所有先前块大小的总和。
         *
         * curChunk 指针总是有效的。elementIndex 是 curChunk 中下一个要写入的元素的索引；
         * 这可能超过 curChunk 的末尾，因此在写入前需要检查。
         * 当我们扩展 spine 数组时，curChunk 成为其中的第一个元素。
         * 当我们清除缓冲区时，我们丢弃所有块，除了第一个块，我们清除它，
         * 恢复到初始的单块状态。
         */

        // 当前正在写入的块
        T_ARR curChunk;

        // 所有块，如果只有一个块则为 null
        T_ARR[] spine;

        /**
         * 构造一个具有指定初始容量的空列表。
         *
         * @param  initialCapacity  列表的初始容量
         * @throws IllegalArgumentException 如果指定的初始容量为负数
         */
        OfPrimitive(int initialCapacity) {
            super(initialCapacity);
            curChunk = newArray(1 << initialChunkPower);
        }

        /**
         * 构造一个初始容量为十六的空列表。
         */
        OfPrimitive() {
            super();
            curChunk = newArray(1 << initialChunkPower);
        }

        @Override
        public abstract Iterator<E> iterator();

        @Override
        public abstract void forEach(Consumer<? super E> consumer);

        /** 创建一个适当类型和大小的数组 */
        protected abstract T_ARR[] newArrayArray(int size);

        /** 创建一个适当类型和大小的数组 */
        public abstract T_ARR newArray(int size);

        /** 获取数组的长度 */
        protected abstract int arrayLength(T_ARR array);

        /** 使用提供的消费者迭代数组 */
        protected abstract void arrayForEach(T_ARR array, int from, int to,
                                             T_CONS consumer);

        protected long capacity() {
            return (spineIndex == 0)
                   ? arrayLength(curChunk)
                   : priorElementCount[spineIndex] + arrayLength(spine[spineIndex]);
        }

        private void inflateSpine() {
            if (spine == null) {
                spine = newArrayArray(MIN_SPINE_SIZE);
                priorElementCount = new long[MIN_SPINE_SIZE];
                spine[0] = curChunk;
            }
        }

        protected final void ensureCapacity(long targetSize) {
            long capacity = capacity();
            if (targetSize > capacity) {
                inflateSpine();
                for (int i=spineIndex+1; targetSize > capacity; i++) {
                    if (i >= spine.length) {
                        int newSpineSize = spine.length * 2;
                        spine = Arrays.copyOf(spine, newSpineSize);
                        priorElementCount = Arrays.copyOf(priorElementCount, newSpineSize);
                    }
                    int nextChunkSize = chunkSize(i);
                    spine[i] = newArray(nextChunkSize);
                    priorElementCount[i] = priorElementCount[i-1] + arrayLength(spine[i - 1]);
                    capacity += nextChunkSize;
                }
            }
        }

        protected void increaseCapacity() {
            ensureCapacity(capacity() + 1);
        }

        protected int chunkFor(long index) {
            if (spineIndex == 0) {
                if (index < elementIndex)
                    return 0;
                else
                    throw new IndexOutOfBoundsException(Long.toString(index));
            }

            if (index >= count())
                throw new IndexOutOfBoundsException(Long.toString(index));

            for (int j=0; j <= spineIndex; j++)
                if (index < priorElementCount[j] + arrayLength(spine[j]))
                    return j;

            throw new IndexOutOfBoundsException(Long.toString(index));
        }

        public void copyInto(T_ARR array, int offset) {
            long finalOffset = offset + count();
            if (finalOffset > arrayLength(array) || finalOffset < offset) {
                throw new IndexOutOfBoundsException("无法适应");
            }

            if (spineIndex == 0)
                System.arraycopy(curChunk, 0, array, offset, elementIndex);
            else {
                // 完整块
                for (int i=0; i < spineIndex; i++) {
                    System.arraycopy(spine[i], 0, array, offset, arrayLength(spine[i]));
                    offset += arrayLength(spine[i]);
                }
                if (elementIndex > 0)
                    System.arraycopy(curChunk, 0, array, offset, elementIndex);
            }
        }

        public T_ARR asPrimitiveArray() {
            long size = count();
            if (size >= Nodes.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            T_ARR result = newArray((int) size);
            copyInto(result, 0);
            return result;
        }

        protected void preAccept() {
            if (elementIndex == arrayLength(curChunk)) {
                inflateSpine();
                if (spineIndex+1 >= spine.length || spine[spineIndex+1] == null)
                    increaseCapacity();
                elementIndex = 0;
                ++spineIndex;
                curChunk = spine[spineIndex];
            }
        }

        public void clear() {
            if (spine != null) {
                curChunk = spine[0];
                spine = null;
                priorElementCount = null;
            }
            elementIndex = 0;
            spineIndex = 0;
        }

        @SuppressWarnings("overloads")
        public void forEach(T_CONS consumer) {
            // 完整块，如果有
            for (int j = 0; j < spineIndex; j++)
                arrayForEach(spine[j], 0, arrayLength(spine[j]), consumer);

            // 当前块
            arrayForEach(curChunk, 0, elementIndex, consumer);
        }

        abstract class BaseSpliterator<T_SPLITR extends Spliterator.OfPrimitive<E, T_CONS, T_SPLITR>>
                implements Spliterator.OfPrimitive<E, T_CONS, T_SPLITR> {
            // 当前脊索索引
            int splSpineIndex;

            // 最后一个脊索索引
            final int lastSpineIndex;

            // 当前脊索中的当前元素索引
            int splElementIndex;

            // 最后一个脊索的最后一个元素索引 + 1
            final int lastSpineElementFence;

            // 当 splSpineIndex >= lastSpineIndex 且
            // splElementIndex >= lastSpineElementFence 时，
            // 该迭代器已完全遍历
            // tryAdvance 可以将 splSpineIndex 设置为大于 spineIndex，如果最后一个脊索是满的

            // 当前脊索数组
            T_ARR splChunk;

            BaseSpliterator(int firstSpineIndex, int lastSpineIndex,
                            int firstSpineElementIndex, int lastSpineElementFence) {
                this.splSpineIndex = firstSpineIndex;
                this.lastSpineIndex = lastSpineIndex;
                this.splElementIndex = firstSpineElementIndex;
                this.lastSpineElementFence = lastSpineElementFence;
                assert spine != null || firstSpineIndex == 0 && lastSpineIndex == 0;
                splChunk = (spine == null) ? curChunk : spine[firstSpineIndex];
            }

            abstract T_SPLITR newSpliterator(int firstSpineIndex, int lastSpineIndex,
                                             int firstSpineElementIndex, int lastSpineElementFence);

            abstract void arrayForOne(T_ARR array, int index, T_CONS consumer);

            abstract T_SPLITR arraySpliterator(T_ARR array, int offset, int len);

            @Override
            public long estimateSize() {
                return (splSpineIndex == lastSpineIndex)
                       ? (long) lastSpineElementFence - splElementIndex
                       : // 到结束前的元素数 -
                       priorElementCount[lastSpineIndex] + lastSpineElementFence -
                       // 到当前的元素数
                       priorElementCount[splSpineIndex] - splElementIndex;
            }

            @Override
            public int characteristics() {
                return SPLITERATOR_CHARACTERISTICS;
            }

            @Override
            public boolean tryAdvance(T_CONS consumer) {
                Objects.requireNonNull(consumer);

                if (splSpineIndex < lastSpineIndex
                    || (splSpineIndex == lastSpineIndex && splElementIndex < lastSpineElementFence)) {
                    arrayForOne(splChunk, splElementIndex++, consumer);

                    if (splElementIndex == arrayLength(splChunk)) {
                        splElementIndex = 0;
                        ++splSpineIndex;
                        if (spine != null && splSpineIndex <= lastSpineIndex)
                            splChunk = spine[splSpineIndex];
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void forEachRemaining(T_CONS consumer) {
                Objects.requireNonNull(consumer);

                if (splSpineIndex < lastSpineIndex
                    || (splSpineIndex == lastSpineIndex && splElementIndex < lastSpineElementFence)) {
                    int i = splElementIndex;
                    // 完整块，如果有
                    for (int sp = splSpineIndex; sp < lastSpineIndex; sp++) {
                        T_ARR chunk = spine[sp];
                        arrayForEach(chunk, i, arrayLength(chunk), consumer);
                        i = 0;
                    }
                    // 最后（或当前未完成）块
                    T_ARR chunk = (splSpineIndex == lastSpineIndex) ? splChunk : spine[lastSpineIndex];
                    arrayForEach(chunk, i, lastSpineElementFence, consumer);
                    // 标记已消费
                    splSpineIndex = lastSpineIndex;
                    splElementIndex = lastSpineElementFence;
                }
            }

            @Override
            public T_SPLITR trySplit() {
                if (splSpineIndex < lastSpineIndex) {
                    // 在最后一个块之前拆分（如果它是满的，这意味着 50:50 拆分）
                    T_SPLITR ret = newSpliterator(splSpineIndex, lastSpineIndex - 1,
                                                  splElementIndex, arrayLength(spine[lastSpineIndex - 1]));
                    // 将我们定位到最后一个块的开始
                    splSpineIndex = lastSpineIndex;
                    splElementIndex = 0;
                    splChunk = spine[splSpineIndex];
                    return ret;
                }
                else if (splSpineIndex == lastSpineIndex) {
                    int t = (lastSpineElementFence - splElementIndex) / 2;
                    if (t == 0)
                        return null;
                    else {
                        T_SPLITR ret = arraySpliterator(splChunk, splElementIndex, t);
                        splElementIndex += t;
                        return ret;
                    }
                }
                else {
                    return null;
                }
            }
        }
    }

    /**
     * 一个有序的 {@code int} 值集合。
     */
    static class OfInt extends SpinedBuffer.OfPrimitive<Integer, int[], IntConsumer>
            implements IntConsumer {
        OfInt() { }

        OfInt(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public void forEach(Consumer<? super Integer> consumer) {
            if (consumer instanceof IntConsumer) {
                forEach((IntConsumer) consumer);
            }
            else {
                if (Tripwire.ENABLED)
                    Tripwire.trip(getClass(), "{0} calling SpinedBuffer.OfInt.forEach(Consumer)");
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        protected int[][] newArrayArray(int size) {
            return new int[size][];
        }

        @Override
        public int[] newArray(int size) {
            return new int[size];
        }

        @Override
        protected int arrayLength(int[] array) {
            return array.length;
        }

        @Override
        protected void arrayForEach(int[] array,
                                    int from, int to,
                                    IntConsumer consumer) {
            for (int i = from; i < to; i++)
                consumer.accept(array[i]);
        }

        @Override
        public void accept(int i) {
            preAccept();
            curChunk[elementIndex++] = i;
        }

        public int get(long index) {
            // 转换为 int 是安全的，因为脊索数组索引是当前脊索的索引减去
            // 之前的元素计数
            int ch = chunkFor(index);
            if (spineIndex == 0 && ch == 0)
                return curChunk[(int) index];
            else
                return spine[ch][(int) (index - priorElementCount[ch])];
        }

        @Override
        public PrimitiveIterator.OfInt iterator() {
            return Spliterators.iterator(spliterator());
        }

        public Spliterator.OfInt spliterator() {
            class Splitr extends BaseSpliterator<Spliterator.OfInt>
                    implements Spliterator.OfInt {
                Splitr(int firstSpineIndex, int lastSpineIndex,
                       int firstSpineElementIndex, int lastSpineElementFence) {
                    super(firstSpineIndex, lastSpineIndex,
                          firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                Splitr newSpliterator(int firstSpineIndex, int lastSpineIndex,
                                      int firstSpineElementIndex, int lastSpineElementFence) {
                    return new Splitr(firstSpineIndex, lastSpineIndex,
                                      firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                void arrayForOne(int[] array, int index, IntConsumer consumer) {
                    consumer.accept(array[index]);
                }

                @Override
                Spliterator.OfInt arraySpliterator(int[] array, int offset, int len) {
                    return Arrays.spliterator(array, offset, offset+len);
                }
            }
            return new Splitr(0, spineIndex, 0, elementIndex);
        }


                    @Override
        public String toString() {
            int[] array = asPrimitiveArray();
            if (array.length < 200) {
                return String.format("%s[length=%d, chunks=%d]%s",
                                     getClass().getSimpleName(), array.length,
                                     spineIndex, Arrays.toString(array));
            }
            else {
                int[] array2 = Arrays.copyOf(array, 200);
                return String.format("%s[length=%d, chunks=%d]%s...",
                                     getClass().getSimpleName(), array.length,
                                     spineIndex, Arrays.toString(array2));
            }
        }
    }

    /**
     * 有序的 {@code long} 值集合。
     */
    static class OfLong extends SpinedBuffer.OfPrimitive<Long, long[], LongConsumer>
            implements LongConsumer {
        OfLong() { }

        OfLong(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public void forEach(Consumer<? super Long> consumer) {
            if (consumer instanceof LongConsumer) {
                forEach((LongConsumer) consumer);
            }
            else {
                if (Tripwire.ENABLED)
                    Tripwire.trip(getClass(), "{0} calling SpinedBuffer.OfLong.forEach(Consumer)");
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        protected long[][] newArrayArray(int size) {
            return new long[size][];
        }

        @Override
        public long[] newArray(int size) {
            return new long[size];
        }

        @Override
        protected int arrayLength(long[] array) {
            return array.length;
        }

        @Override
        protected void arrayForEach(long[] array,
                                    int from, int to,
                                    LongConsumer consumer) {
            for (int i = from; i < to; i++)
                consumer.accept(array[i]);
        }

        @Override
        public void accept(long i) {
            preAccept();
            curChunk[elementIndex++] = i;
        }

        public long get(long index) {
            // 转换为 int 是安全的，因为脊柱数组索引是当前脊柱的索引减去之前的元素计数
            int ch = chunkFor(index);
            if (spineIndex == 0 && ch == 0)
                return curChunk[(int) index];
            else
                return spine[ch][(int) (index - priorElementCount[ch])];
        }

        @Override
        public PrimitiveIterator.OfLong iterator() {
            return Spliterators.iterator(spliterator());
        }


        public Spliterator.OfLong spliterator() {
            class Splitr extends BaseSpliterator<Spliterator.OfLong>
                    implements Spliterator.OfLong {
                Splitr(int firstSpineIndex, int lastSpineIndex,
                       int firstSpineElementIndex, int lastSpineElementFence) {
                    super(firstSpineIndex, lastSpineIndex,
                          firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                Splitr newSpliterator(int firstSpineIndex, int lastSpineIndex,
                                      int firstSpineElementIndex, int lastSpineElementFence) {
                    return new Splitr(firstSpineIndex, lastSpineIndex,
                                      firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                void arrayForOne(long[] array, int index, LongConsumer consumer) {
                    consumer.accept(array[index]);
                }

                @Override
                Spliterator.OfLong arraySpliterator(long[] array, int offset, int len) {
                    return Arrays.spliterator(array, offset, offset+len);
                }
            }
            return new Splitr(0, spineIndex, 0, elementIndex);
        }

        @Override
        public String toString() {
            long[] array = asPrimitiveArray();
            if (array.length < 200) {
                return String.format("%s[length=%d, chunks=%d]%s",
                                     getClass().getSimpleName(), array.length,
                                     spineIndex, Arrays.toString(array));
            }
            else {
                long[] array2 = Arrays.copyOf(array, 200);
                return String.format("%s[length=%d, chunks=%d]%s...",
                                     getClass().getSimpleName(), array.length,
                                     spineIndex, Arrays.toString(array2));
            }
        }
    }

    /**
     * 有序的 {@code double} 值集合。
     */
    static class OfDouble
            extends SpinedBuffer.OfPrimitive<Double, double[], DoubleConsumer>
            implements DoubleConsumer {
        OfDouble() { }

        OfDouble(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public void forEach(Consumer<? super Double> consumer) {
            if (consumer instanceof DoubleConsumer) {
                forEach((DoubleConsumer) consumer);
            }
            else {
                if (Tripwire.ENABLED)
                    Tripwire.trip(getClass(), "{0} calling SpinedBuffer.OfDouble.forEach(Consumer)");
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        protected double[][] newArrayArray(int size) {
            return new double[size][];
        }

        @Override
        public double[] newArray(int size) {
            return new double[size];
        }

        @Override
        protected int arrayLength(double[] array) {
            return array.length;
        }

        @Override
        protected void arrayForEach(double[] array,
                                    int from, int to,
                                    DoubleConsumer consumer) {
            for (int i = from; i < to; i++)
                consumer.accept(array[i]);
        }

        @Override
        public void accept(double i) {
            preAccept();
            curChunk[elementIndex++] = i;
        }

        public double get(long index) {
            // 转换为 int 是安全的，因为脊柱数组索引是当前脊柱的索引减去之前的元素计数
            int ch = chunkFor(index);
            if (spineIndex == 0 && ch == 0)
                return curChunk[(int) index];
            else
                return spine[ch][(int) (index - priorElementCount[ch])];
        }

        @Override
        public PrimitiveIterator.OfDouble iterator() {
            return Spliterators.iterator(spliterator());
        }

        public Spliterator.OfDouble spliterator() {
            class Splitr extends BaseSpliterator<Spliterator.OfDouble>
                    implements Spliterator.OfDouble {
                Splitr(int firstSpineIndex, int lastSpineIndex,
                       int firstSpineElementIndex, int lastSpineElementFence) {
                    super(firstSpineIndex, lastSpineIndex,
                          firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                Splitr newSpliterator(int firstSpineIndex, int lastSpineIndex,
                                      int firstSpineElementIndex, int lastSpineElementFence) {
                    return new Splitr(firstSpineIndex, lastSpineIndex,
                                      firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                void arrayForOne(double[] array, int index, DoubleConsumer consumer) {
                    consumer.accept(array[index]);
                }

                @Override
                Spliterator.OfDouble arraySpliterator(double[] array, int offset, int len) {
                    return Arrays.spliterator(array, offset, offset+len);
                }
            }
            return new Splitr(0, spineIndex, 0, elementIndex);
        }

        @Override
        public String toString() {
            double[] array = asPrimitiveArray();
            if (array.length < 200) {
                return String.format("%s[length=%d, chunks=%d]%s",
                                     getClass().getSimpleName(), array.length,
                                     spineIndex, Arrays.toString(array));
            }
            else {
                double[] array2 = Arrays.copyOf(array, 200);
                return String.format("%s[length=%d, chunks=%d]%s...",
                                     getClass().getSimpleName(), array.length,
                                     spineIndex, Arrays.toString(array2));
            }
        }
    }
}
