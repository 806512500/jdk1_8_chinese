
/*
 * Copyright (c) 2012, 2017, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * 用于包装和委托拆分器的拆分器实现，用于实现 {@link Stream#spliterator()} 方法。
 *
 * @since 1.8
 */
class StreamSpliterators {

    /**
     * 抽象包装拆分器，首次操作时绑定到管道助手的拆分器。
     *
     * <p>此拆分器不是延迟绑定的，将在首次操作时绑定到源拆分器。
     *
     * <p>从顺序流生成的包装拆分器如果存在状态操作则不能拆分。
     */
    private static abstract class AbstractWrappingSpliterator<P_IN, P_OUT,
                                                              T_BUFFER extends AbstractSpinedBuffer>
            implements Spliterator<P_OUT> {

        // @@@ 检测是否存在状态操作
        //     如果不存在则可以拆分，否则不能拆分

        /**
         * 如果此拆分器支持拆分则为 true
         */
        final boolean isParallel;

        final PipelineHelper<P_OUT> ph;

        /**
         * 源拆分器的供应商。客户端提供拆分器或供应商之一。
         */
        private Supplier<Spliterator<P_IN>> spliteratorSupplier;

        /**
         * 源拆分器。由客户端提供或从供应商获取。
         */
        Spliterator<P_IN> spliterator;

        /**
         * 用于管道下游阶段的接收器链，最终指向缓冲区。在部分遍历时使用。
         */
        Sink<P_IN> bufferSink;

        /**
         * 一个函数，用于推进拆分器的一个元素并将其推送到 bufferSink。返回是否处理了任何元素。
         * 在部分遍历时使用。
         */
        BooleanSupplier pusher;

        /** 从缓冲区中要消费的下一个元素，用于部分遍历 */
        long nextToConsume;

        /** 用于部分遍历的缓冲区，元素被推送到其中。 */
        T_BUFFER buffer;

        /**
         * 如果已发生完整遍历（可能已取消），则为 true。
         * 如果进行部分遍历，缓冲区中可能仍有元素。
         */
        boolean finished;

        /**
         * 从 {@code Supplier<Spliterator>} 构造 AbstractWrappingSpliterator。
         */
        AbstractWrappingSpliterator(PipelineHelper<P_OUT> ph,
                                    Supplier<Spliterator<P_IN>> spliteratorSupplier,
                                    boolean parallel) {
            this.ph = ph;
            this.spliteratorSupplier = spliteratorSupplier;
            this.spliterator = null;
            this.isParallel = parallel;
        }

        /**
         * 从 {@code Spliterator} 构造 AbstractWrappingSpliterator。
         */
        AbstractWrappingSpliterator(PipelineHelper<P_OUT> ph,
                                    Spliterator<P_IN> spliterator,
                                    boolean parallel) {
            this.ph = ph;
            this.spliteratorSupplier = null;
            this.spliterator = spliterator;
            this.isParallel = parallel;
        }

        /**
         * 在推进之前调用以设置拆分器，如有需要。
         */
        final void init() {
            if (spliterator == null) {
                spliterator = spliteratorSupplier.get();
                spliteratorSupplier = null;
            }
        }

        /**
         * 从源获取一个元素，将其推送到接收器链中，如有需要则设置缓冲区
         * @return 是否有元素可从缓冲区中消费
         */
        final boolean doAdvance() {
            if (buffer == null) {
                if (finished)
                    return false;

                init();
                initPartialTraversalState();
                nextToConsume = 0;
                bufferSink.begin(spliterator.getExactSizeIfKnown());
                return fillBuffer();
            }
            else {
                ++nextToConsume;
                boolean hasNext = nextToConsume < buffer.count();
                if (!hasNext) {
                    nextToConsume = 0;
                    buffer.clear();
                    hasNext = fillBuffer();
                }
                return hasNext;
            }
        }

        /**
         * 使用提供的参数调用形状特定的构造函数并返回结果。
         */
        abstract AbstractWrappingSpliterator<P_IN, P_OUT, ?> wrap(Spliterator<P_IN> s);

        /**
         * 初始化缓冲区、接收器链和推进器，用于形状特定的实现。
         */
        abstract void initPartialTraversalState();

        @Override
        public Spliterator<P_OUT> trySplit() {
            if (isParallel && !finished) {
                init();

                Spliterator<P_IN> split = spliterator.trySplit();
                return (split == null) ? null : wrap(split);
            }
            else
                return null;
        }

        /**
         * 如果缓冲区为空，则将元素推送到接收器链中，直到源为空或请求取消。
         * @return 是否有元素可从缓冲区中消费
         */
        private boolean fillBuffer() {
            while (buffer.count() == 0) {
                if (bufferSink.cancellationRequested() || !pusher.getAsBoolean()) {
                    if (finished)
                        return false;
                    else {
                        bufferSink.end(); // 可能会触发更多元素
                        finished = true;
                    }
                }
            }
            return true;
        }

        @Override
        public final long estimateSize() {
            init();
            // 使用包装拆分器的估计值
            // 注意，如果存在过滤或扁平映射操作，此估计可能不准确
            return spliterator.estimateSize();
        }

        @Override
        public final long getExactSizeIfKnown() {
            init();
            return StreamOpFlag.SIZED.isKnown(ph.getStreamAndOpFlags())
                   ? spliterator.getExactSizeIfKnown()
                   : -1;
        }

        @Override
        public final int characteristics() {
            init();

            // 从管道获取特性
            int c = StreamOpFlag.toCharacteristics(StreamOpFlag.toStreamFlags(ph.getStreamAndOpFlags()));

            // 掩码掉大小和均匀特性，并替换为拆分器的特性
            // 注意，非均匀拆分器可以从具有确切大小的拆分器变为子拆分的估计值，例如
            // 在顶级拆分器中 HashSet 的大小是已知的，但在子拆分中只有估计值是已知的
            if ((c & Spliterator.SIZED) != 0) {
                c &= ~(Spliterator.SIZED | Spliterator.SUBSIZED);
                c |= (spliterator.characteristics() & (Spliterator.SIZED | Spliterator.SUBSIZED));
            }

            return c;
        }

        @Override
        public Comparator<? super P_OUT> getComparator() {
            if (!hasCharacteristics(SORTED))
                throw new IllegalStateException();
            return null;
        }

        @Override
        public final String toString() {
            return String.format("%s[%s]", getClass().getName(), spliterator);
        }
    }

    static final class WrappingSpliterator<P_IN, P_OUT>
            extends AbstractWrappingSpliterator<P_IN, P_OUT, SpinedBuffer<P_OUT>> {

        WrappingSpliterator(PipelineHelper<P_OUT> ph,
                            Supplier<Spliterator<P_IN>> supplier,
                            boolean parallel) {
            super(ph, supplier, parallel);
        }

        WrappingSpliterator(PipelineHelper<P_OUT> ph,
                            Spliterator<P_IN> spliterator,
                            boolean parallel) {
            super(ph, spliterator, parallel);
        }

        @Override
        WrappingSpliterator<P_IN, P_OUT> wrap(Spliterator<P_IN> s) {
            return new WrappingSpliterator<>(ph, s, isParallel);
        }

        @Override
        void initPartialTraversalState() {
            SpinedBuffer<P_OUT> b = new SpinedBuffer<>();
            buffer = b;
            bufferSink = ph.wrapSink(b::accept);
            pusher = () -> spliterator.tryAdvance(bufferSink);
        }

        @Override
        public boolean tryAdvance(Consumer<? super P_OUT> consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();
            if (hasNext)
                consumer.accept(buffer.get(nextToConsume));
            return hasNext;
        }

        @Override
        public void forEachRemaining(Consumer<? super P_OUT> consumer) {
            if (buffer == null && !finished) {
                Objects.requireNonNull(consumer);
                init();

                ph.wrapAndCopyInto((Sink<P_OUT>) consumer::accept, spliterator);
                finished = true;
            }
            else {
                do { } while (tryAdvance(consumer));
            }
        }
    }

    static final class IntWrappingSpliterator<P_IN>
            extends AbstractWrappingSpliterator<P_IN, Integer, SpinedBuffer.OfInt>
            implements Spliterator.OfInt {

        IntWrappingSpliterator(PipelineHelper<Integer> ph,
                               Supplier<Spliterator<P_IN>> supplier,
                               boolean parallel) {
            super(ph, supplier, parallel);
        }

        IntWrappingSpliterator(PipelineHelper<Integer> ph,
                               Spliterator<P_IN> spliterator,
                               boolean parallel) {
            super(ph, spliterator, parallel);
        }

        @Override
        AbstractWrappingSpliterator<P_IN, Integer, ?> wrap(Spliterator<P_IN> s) {
            return new IntWrappingSpliterator<>(ph, s, isParallel);
        }

        @Override
        void initPartialTraversalState() {
            SpinedBuffer.OfInt b = new SpinedBuffer.OfInt();
            buffer = b;
            bufferSink = ph.wrapSink((Sink.OfInt) b::accept);
            pusher = () -> spliterator.tryAdvance(bufferSink);
        }

        @Override
        public Spliterator.OfInt trySplit() {
            return (Spliterator.OfInt) super.trySplit();
        }

        @Override
        public boolean tryAdvance(IntConsumer consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();
            if (hasNext)
                consumer.accept(buffer.get(nextToConsume));
            return hasNext;
        }

        @Override
        public void forEachRemaining(IntConsumer consumer) {
            if (buffer == null && !finished) {
                Objects.requireNonNull(consumer);
                init();

                ph.wrapAndCopyInto((Sink.OfInt) consumer::accept, spliterator);
                finished = true;
            }
            else {
                do { } while (tryAdvance(consumer));
            }
        }
    }

    static final class LongWrappingSpliterator<P_IN>
            extends AbstractWrappingSpliterator<P_IN, Long, SpinedBuffer.OfLong>
            implements Spliterator.OfLong {

        LongWrappingSpliterator(PipelineHelper<Long> ph,
                                Supplier<Spliterator<P_IN>> supplier,
                                boolean parallel) {
            super(ph, supplier, parallel);
        }

        LongWrappingSpliterator(PipelineHelper<Long> ph,
                                Spliterator<P_IN> spliterator,
                                boolean parallel) {
            super(ph, spliterator, parallel);
        }

        @Override
        AbstractWrappingSpliterator<P_IN, Long, ?> wrap(Spliterator<P_IN> s) {
            return new LongWrappingSpliterator<>(ph, s, isParallel);
        }

        @Override
        void initPartialTraversalState() {
            SpinedBuffer.OfLong b = new SpinedBuffer.OfLong();
            buffer = b;
            bufferSink = ph.wrapSink((Sink.OfLong) b::accept);
            pusher = () -> spliterator.tryAdvance(bufferSink);
        }

        @Override
        public Spliterator.OfLong trySplit() {
            return (Spliterator.OfLong) super.trySplit();
        }

        @Override
        public boolean tryAdvance(LongConsumer consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();
            if (hasNext)
                consumer.accept(buffer.get(nextToConsume));
            return hasNext;
        }

        @Override
        public void forEachRemaining(LongConsumer consumer) {
            if (buffer == null && !finished) {
                Objects.requireNonNull(consumer);
                init();

                ph.wrapAndCopyInto((Sink.OfLong) consumer::accept, spliterator);
                finished = true;
            }
            else {
                do { } while (tryAdvance(consumer));
            }
        }
    }

    static final class DoubleWrappingSpliterator<P_IN>
            extends AbstractWrappingSpliterator<P_IN, Double, SpinedBuffer.OfDouble>
            implements Spliterator.OfDouble {


        }

    }
```

```java
                    DoubleWrappingSpliterator(PipelineHelper<Double> ph,
                                  Supplier<Spliterator<P_IN>> supplier,
                                  boolean parallel) {
            super(ph, supplier, parallel);
        }

        DoubleWrappingSpliterator(PipelineHelper<Double> ph,
                                  Spliterator<P_IN> spliterator,
                                  boolean parallel) {
            super(ph, spliterator, parallel);
        }

        @Override
        AbstractWrappingSpliterator<P_IN, Double, ?> wrap(Spliterator<P_IN> s) {
            return new DoubleWrappingSpliterator<>(ph, s, isParallel);
        }

        @Override
        void initPartialTraversalState() {
            SpinedBuffer.OfDouble b = new SpinedBuffer.OfDouble();
            buffer = b;
            bufferSink = ph.wrapSink((Sink.OfDouble) b::accept);
            pusher = () -> spliterator.tryAdvance(bufferSink);
        }

        @Override
        public Spliterator.OfDouble trySplit() {
            return (Spliterator.OfDouble) super.trySplit();
        }

        @Override
        public boolean tryAdvance(DoubleConsumer consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();
            if (hasNext)
                consumer.accept(buffer.get(nextToConsume));
            return hasNext;
        }

        @Override
        public void forEachRemaining(DoubleConsumer consumer) {
            if (buffer == null && !finished) {
                Objects.requireNonNull(consumer);
                init();

                ph.wrapAndCopyInto((Sink.OfDouble) consumer::accept, spliterator);
                finished = true;
            }
            else {
                do { } while (tryAdvance(consumer));
            }
        }
    }

    /**
     * 实现了委托给底层 Spliterator 的 Spliterator，从 {@code Supplier<Spliterator>} 中获取 Spliterator，
     * 在首次调用任何 Spliterator 方法时。
     * @param <T>
     */
    static class DelegatingSpliterator<T, T_SPLITR extends Spliterator<T>>
            implements Spliterator<T> {
        private final Supplier<? extends T_SPLITR> supplier;

        private T_SPLITR s;

        DelegatingSpliterator(Supplier<? extends T_SPLITR> supplier) {
            this.supplier = supplier;
        }

        T_SPLITR get() {
            if (s == null) {
                s = supplier.get();
            }
            return s;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T_SPLITR trySplit() {
            return (T_SPLITR) get().trySplit();
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> consumer) {
            return get().tryAdvance(consumer);
        }

        @Override
        public void forEachRemaining(Consumer<? super T> consumer) {
            get().forEachRemaining(consumer);
        }

        @Override
        public long estimateSize() {
            return get().estimateSize();
        }

        @Override
        public int characteristics() {
            return get().characteristics();
        }

        @Override
        public Comparator<? super T> getComparator() {
            return get().getComparator();
        }

        @Override
        public long getExactSizeIfKnown() {
            return get().getExactSizeIfKnown();
        }

        @Override
        public String toString() {
            return getClass().getName() + "[" + get() + "]";
        }

        static class OfPrimitive<T, T_CONS, T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>>
            extends DelegatingSpliterator<T, T_SPLITR>
            implements Spliterator.OfPrimitive<T, T_CONS, T_SPLITR> {
            OfPrimitive(Supplier<? extends T_SPLITR> supplier) {
                super(supplier);
            }

            @Override
            public boolean tryAdvance(T_CONS consumer) {
                return get().tryAdvance(consumer);
            }

            @Override
            public void forEachRemaining(T_CONS consumer) {
                get().forEachRemaining(consumer);
            }
        }

        static final class OfInt
                extends OfPrimitive<Integer, IntConsumer, Spliterator.OfInt>
                implements Spliterator.OfInt {

            OfInt(Supplier<Spliterator.OfInt> supplier) {
                super(supplier);
            }
        }

        static final class OfLong
                extends OfPrimitive<Long, LongConsumer, Spliterator.OfLong>
                implements Spliterator.OfLong {

            OfLong(Supplier<Spliterator.OfLong> supplier) {
                super(supplier);
            }
        }

        static final class OfDouble
                extends OfPrimitive<Double, DoubleConsumer, Spliterator.OfDouble>
                implements Spliterator.OfDouble {

            OfDouble(Supplier<Spliterator.OfDouble> supplier) {
                super(supplier);
            }
        }
    }

    /**
     * 一个从源 Spliterator 中切片的 Spliterator，报告 {@code SUBSIZED}。
     *
     */
    static abstract class SliceSpliterator<T, T_SPLITR extends Spliterator<T>> {
        // 切片的起始索引
        final long sliceOrigin;
        // 切片的最后一个索引加一
        final long sliceFence;

        // 要切片的 Spliterator
        T_SPLITR s;
        // 当前（绝对）索引，在 advance/split 时修改
        long index;
        // 最后一个（绝对）索引加一或 sliceFence，取较小值
        long fence;

        SliceSpliterator(T_SPLITR s, long sliceOrigin, long sliceFence, long origin, long fence) {
            assert s.hasCharacteristics(Spliterator.SUBSIZED);
            this.s = s;
            this.sliceOrigin = sliceOrigin;
            this.sliceFence = sliceFence;
            this.index = origin;
            this.fence = fence;
        }

        protected abstract T_SPLITR makeSpliterator(T_SPLITR s, long sliceOrigin, long sliceFence, long origin, long fence);

        public T_SPLITR trySplit() {
            if (sliceOrigin >= fence)
                return null;

            if (index >= fence)
                return null;

            // 一直分割直到左分割和右分割与切片相交，从而确保大小估计减少。
            // 这也避免了创建空的 Spliterator，这可能导致 F/J 任务在没有元素的情况下执行冗余工作。
            while (true) {
                @SuppressWarnings("unchecked")
                T_SPLITR leftSplit = (T_SPLITR) s.trySplit();
                if (leftSplit == null)
                    return null;

                long leftSplitFenceUnbounded = index + leftSplit.estimateSize();
                long leftSplitFence = Math.min(leftSplitFenceUnbounded, sliceFence);
                if (sliceOrigin >= leftSplitFence) {
                    // 左分割不与切片相交，且位于切片左侧
                    // 右分割与切片相交
                    // 丢弃左分割并使用右分割进一步分割
                    index = leftSplitFence;
                }
                else if (leftSplitFence >= sliceFence) {
                    // 右分割不与切片相交，且位于切片右侧
                    // 左分割与切片相交
                    // 丢弃右分割并使用左分割进一步分割
                    s = leftSplit;
                    fence = leftSplitFence;
                }
                else if (index >= sliceOrigin && leftSplitFenceUnbounded <= sliceFence) {
                    // 左分割包含在切片内，返回底层的左分割
                    // 右分割包含在切片内或与切片相交
                    index = leftSplitFence;
                    return leftSplit;
                } else {
                    // 左分割与切片相交
                    // 右分割包含在切片内或与切片相交
                    return makeSpliterator(leftSplit, sliceOrigin, sliceFence, index, index = leftSplitFence);
                }
            }
        }

        public long estimateSize() {
            return (sliceOrigin < fence)
                   ? fence - Math.max(sliceOrigin, index) : 0;
        }

        public int characteristics() {
            return s.characteristics();
        }

        static final class OfRef<T>
                extends SliceSpliterator<T, Spliterator<T>>
                implements Spliterator<T> {

            OfRef(Spliterator<T> s, long sliceOrigin, long sliceFence) {
                this(s, sliceOrigin, sliceFence, 0, Math.min(s.estimateSize(), sliceFence));
            }

            private OfRef(Spliterator<T> s,
                          long sliceOrigin, long sliceFence, long origin, long fence) {
                super(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            protected Spliterator<T> makeSpliterator(Spliterator<T> s,
                                                     long sliceOrigin, long sliceFence,
                                                     long origin, long fence) {
                return new OfRef<>(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                Objects.requireNonNull(action);

                if (sliceOrigin >= fence)
                    return false;

                while (sliceOrigin > index) {
                    s.tryAdvance(e -> {});
                    index++;
                }

                if (index >= fence)
                    return false;

                index++;
                return s.tryAdvance(action);
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                Objects.requireNonNull(action);

                if (sliceOrigin >= fence)
                    return;

                if (index >= fence)
                    return;

                if (index >= sliceOrigin && (index + s.estimateSize()) <= sliceFence) {
                    // Spliterator 包含在切片内
                    s.forEachRemaining(action);
                    index = fence;
                } else {
                    // Spliterator 与切片相交
                    while (sliceOrigin > index) {
                        s.tryAdvance(e -> {});
                        index++;
                    }
                    // 遍历到 fence 之前的元素
                    for (;index < fence; index++) {
                        s.tryAdvance(action);
                    }
                }
            }
        }

        static abstract class OfPrimitive<T,
                T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>,
                T_CONS>
                extends SliceSpliterator<T, T_SPLITR>
                implements Spliterator.OfPrimitive<T, T_CONS, T_SPLITR> {

            OfPrimitive(T_SPLITR s, long sliceOrigin, long sliceFence) {
                this(s, sliceOrigin, sliceFence, 0, Math.min(s.estimateSize(), sliceFence));
            }

            private OfPrimitive(T_SPLITR s,
                                long sliceOrigin, long sliceFence, long origin, long fence) {
                super(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            public boolean tryAdvance(T_CONS action) {
                Objects.requireNonNull(action);

                if (sliceOrigin >= fence)
                    return false;

                while (sliceOrigin > index) {
                    s.tryAdvance(emptyConsumer());
                    index++;
                }

                if (index >= fence)
                    return false;

                index++;
                return s.tryAdvance(action);
            }

            @Override
            public void forEachRemaining(T_CONS action) {
                Objects.requireNonNull(action);

                if (sliceOrigin >= fence)
                    return;

                if (index >= fence)
                    return;

                if (index >= sliceOrigin && (index + s.estimateSize()) <= sliceFence) {
                    // Spliterator 包含在切片内
                    s.forEachRemaining(action);
                    index = fence;
                } else {
                    // Spliterator 与切片相交
                    while (sliceOrigin > index) {
                        s.tryAdvance(emptyConsumer());
                        index++;
                    }
                    // 遍历到 fence 之前的元素
                    for (;index < fence; index++) {
                        s.tryAdvance(action);
                    }
                }
            }

            protected abstract T_CONS emptyConsumer();
        }

        static final class OfInt extends OfPrimitive<Integer, Spliterator.OfInt, IntConsumer>
                implements Spliterator.OfInt {
            OfInt(Spliterator.OfInt s, long sliceOrigin, long sliceFence) {
                super(s, sliceOrigin, sliceFence);
            }

            OfInt(Spliterator.OfInt s,
                  long sliceOrigin, long sliceFence, long origin, long fence) {
                super(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            protected Spliterator.OfInt makeSpliterator(Spliterator.OfInt s,
                                                        long sliceOrigin, long sliceFence,
                                                        long origin, long fence) {
                return new SliceSpliterator.OfInt(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            protected IntConsumer emptyConsumer() {
                return e -> {};
            }
        }

        static final class OfLong extends OfPrimitive<Long, Spliterator.OfLong, LongConsumer>
                implements Spliterator.OfLong {
            OfLong(Spliterator.OfLong s, long sliceOrigin, long sliceFence) {
                super(s, sliceOrigin, sliceFence);
            }

            OfLong(Spliterator.OfLong s,
                   long sliceOrigin, long sliceFence, long origin, long fence) {
                super(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            protected Spliterator.OfLong makeSpliterator(Spliterator.OfLong s,
                                                         long sliceOrigin, long sliceFence,
                                                         long origin, long fence) {
                return new SliceSpliterator.OfLong(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            protected LongConsumer emptyConsumer() {
                return e -> {};
            }
        }

        static final class OfDouble extends OfPrimitive<Double, Spliterator.OfDouble, DoubleConsumer>
                implements Spliterator.OfDouble {
            OfDouble(Spliterator.OfDouble s, long sliceOrigin, long sliceFence) {
                super(s, sliceOrigin, sliceFence);
            }


                        OfDouble(Spliterator.OfDouble s,
                     long sliceOrigin, long sliceFence, long origin, long fence) {
                super(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            protected Spliterator.OfDouble makeSpliterator(Spliterator.OfDouble s,
                                                           long sliceOrigin, long sliceFence,
                                                           long origin, long fence) {
                return new SliceSpliterator.OfDouble(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            protected DoubleConsumer emptyConsumer() {
                return e -> {};
            }
        }
    }

    /**
     * 一个不保留源 Spliterator 顺序的切片 Spliterator。
     *
     * 注意：源 spliterator 可能报告 {@code ORDERED}，因为该 spliterator 可能是之前管道阶段的结果，
     * 该阶段被收集到一个 {@code Node}。是否使用此切片 spliterator 由管道阶段的顺序决定。
     */
    static abstract class UnorderedSliceSpliterator<T, T_SPLITR extends Spliterator<T>> {
        static final int CHUNK_SIZE = 1 << 7;

        // 要切片的 spliterator
        protected final T_SPLITR s;
        protected final boolean unlimited;
        protected final int chunkSize;
        private final long skipThreshold;
        private final AtomicLong permits;

        UnorderedSliceSpliterator(T_SPLITR s, long skip, long limit) {
            this.s = s;
            this.unlimited = limit < 0;
            this.skipThreshold = limit >= 0 ? limit : 0;
            this.chunkSize = limit >= 0 ? (int)Math.min(CHUNK_SIZE,
                                                        ((skip + limit) / AbstractTask.getLeafTarget()) + 1) : CHUNK_SIZE;
            this.permits = new AtomicLong(limit >= 0 ? skip + limit : skip);
        }

        UnorderedSliceSpliterator(T_SPLITR s,
                                  UnorderedSliceSpliterator<T, T_SPLITR> parent) {
            this.s = s;
            this.unlimited = parent.unlimited;
            this.permits = parent.permits;
            this.skipThreshold = parent.skipThreshold;
            this.chunkSize = parent.chunkSize;
        }

        /**
         * 获取跳过或处理元素的权限。调用者必须首先获取元素，然后咨询此方法以确定如何处理数据。
         *
         * <p>我们使用一个 {@code AtomicLong} 来原子地维护一个计数器，该计数器初始化为 skip+limit（如果我们有限制），
         * 或仅初始化为 skip（如果我们没有限制）。用户应在获取数据元素之前咨询 {@code checkPermits()} 方法。
         *
         * @param numElements 调用者手中的元素数量
         * @return 应该处理的元素数量；剩余的元素应被丢弃。
         */
        protected final long acquirePermits(long numElements) {
            long remainingPermits;
            long grabbing;
            // permits 从不增加，且不会低于零
            assert numElements > 0;
            do {
                remainingPermits = permits.get();
                if (remainingPermits == 0)
                    return unlimited ? numElements : 0;
                grabbing = Math.min(remainingPermits, numElements);
            } while (grabbing > 0 &&
                     !permits.compareAndSet(remainingPermits, remainingPermits - grabbing));

            if (unlimited)
                return Math.max(numElements - grabbing, 0);
            else if (remainingPermits > skipThreshold)
                return Math.max(grabbing - (remainingPermits - skipThreshold), 0);
            else
                return grabbing;
        }

        enum PermitStatus { NO_MORE, MAYBE_MORE, UNLIMITED }

        /** 调用以检查在获取数据之前是否有权限可用 */
        protected final PermitStatus permitStatus() {
            if (permits.get() > 0)
                return PermitStatus.MAYBE_MORE;
            else
                return unlimited ?  PermitStatus.UNLIMITED : PermitStatus.NO_MORE;
        }

        public final T_SPLITR trySplit() {
            // 当没有更多的限制权限时停止拆分
            if (permits.get() == 0)
                return null;
            @SuppressWarnings("unchecked")
            T_SPLITR split = (T_SPLITR) s.trySplit();
            return split == null ? null : makeSpliterator(split);
        }

        protected abstract T_SPLITR makeSpliterator(T_SPLITR s);

        public final long estimateSize() {
            return s.estimateSize();
        }

        public final int characteristics() {
            return s.characteristics() &
                   ~(Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED);
        }

        static final class OfRef<T> extends UnorderedSliceSpliterator<T, Spliterator<T>>
                implements Spliterator<T>, Consumer<T> {
            T tmpSlot;

            OfRef(Spliterator<T> s, long skip, long limit) {
                super(s, skip, limit);
            }

            OfRef(Spliterator<T> s, OfRef<T> parent) {
                super(s, parent);
            }

            @Override
            public final void accept(T t) {
                tmpSlot = t;
            }

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                Objects.requireNonNull(action);

                while (permitStatus() != PermitStatus.NO_MORE) {
                    if (!s.tryAdvance(this))
                        return false;
                    else if (acquirePermits(1) == 1) {
                        action.accept(tmpSlot);
                        tmpSlot = null;
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                Objects.requireNonNull(action);

                ArrayBuffer.OfRef<T> sb = null;
                PermitStatus permitStatus;
                while ((permitStatus = permitStatus()) != PermitStatus.NO_MORE) {
                    if (permitStatus == PermitStatus.MAYBE_MORE) {
                        // 乐观地遍历元素，直到达到 chunkSize 的阈值
                        if (sb == null)
                            sb = new ArrayBuffer.OfRef<>(chunkSize);
                        else
                            sb.reset();
                        long permitsRequested = 0;
                        do { } while (s.tryAdvance(sb) && ++permitsRequested < chunkSize);
                        if (permitsRequested == 0)
                            return;
                        sb.forEach(action, acquirePermits(permitsRequested));
                    }
                    else {
                        // 必须是 UNLIMITED；放手一搏
                        s.forEachRemaining(action);
                        return;
                    }
                }
            }

            @Override
            protected Spliterator<T> makeSpliterator(Spliterator<T> s) {
                return new UnorderedSliceSpliterator.OfRef<>(s, this);
            }
        }

        /**
         * 具体子类型也必须是类型 {@code T_CONS} 的实例。
         *
         * @param <T_BUFF> 脊柱缓冲区的类型。也必须是 {@code T_CONS} 的类型。
         */
        static abstract class OfPrimitive<
                T,
                T_CONS,
                T_BUFF extends ArrayBuffer.OfPrimitive<T_CONS>,
                T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>>
                extends UnorderedSliceSpliterator<T, T_SPLITR>
                implements Spliterator.OfPrimitive<T, T_CONS, T_SPLITR> {
            OfPrimitive(T_SPLITR s, long skip, long limit) {
                super(s, skip, limit);
            }

            OfPrimitive(T_SPLITR s, UnorderedSliceSpliterator.OfPrimitive<T, T_CONS, T_BUFF, T_SPLITR> parent) {
                super(s, parent);
            }

            @Override
            public boolean tryAdvance(T_CONS action) {
                Objects.requireNonNull(action);
                @SuppressWarnings("unchecked")
                T_CONS consumer = (T_CONS) this;

                while (permitStatus() != PermitStatus.NO_MORE) {
                    if (!s.tryAdvance(consumer))
                        return false;
                    else if (acquirePermits(1) == 1) {
                        acceptConsumed(action);
                        return true;
                    }
                }
                return false;
            }

            protected abstract void acceptConsumed(T_CONS action);

            @Override
            public void forEachRemaining(T_CONS action) {
                Objects.requireNonNull(action);

                T_BUFF sb = null;
                PermitStatus permitStatus;
                while ((permitStatus = permitStatus()) != PermitStatus.NO_MORE) {
                    if (permitStatus == PermitStatus.MAYBE_MORE) {
                        // 乐观地遍历元素，直到达到 chunkSize 的阈值
                        if (sb == null)
                            sb = bufferCreate(chunkSize);
                        else
                            sb.reset();
                        @SuppressWarnings("unchecked")
                        T_CONS sbc = (T_CONS) sb;
                        long permitsRequested = 0;
                        do { } while (s.tryAdvance(sbc) && ++permitsRequested < chunkSize);
                        if (permitsRequested == 0)
                            return;
                        sb.forEach(action, acquirePermits(permitsRequested));
                    }
                    else {
                        // 必须是 UNLIMITED；放手一搏
                        s.forEachRemaining(action);
                        return;
                    }
                }
            }

            protected abstract T_BUFF bufferCreate(int initialCapacity);
        }

        static final class OfInt
                extends OfPrimitive<Integer, IntConsumer, ArrayBuffer.OfInt, Spliterator.OfInt>
                implements Spliterator.OfInt, IntConsumer {

            int tmpValue;

            OfInt(Spliterator.OfInt s, long skip, long limit) {
                super(s, skip, limit);
            }

            OfInt(Spliterator.OfInt s, UnorderedSliceSpliterator.OfInt parent) {
                super(s, parent);
            }

            @Override
            public void accept(int value) {
                tmpValue = value;
            }

            @Override
            protected void acceptConsumed(IntConsumer action) {
                action.accept(tmpValue);
            }

            @Override
            protected ArrayBuffer.OfInt bufferCreate(int initialCapacity) {
                return new ArrayBuffer.OfInt(initialCapacity);
            }

            @Override
            protected Spliterator.OfInt makeSpliterator(Spliterator.OfInt s) {
                return new UnorderedSliceSpliterator.OfInt(s, this);
            }
        }

        static final class OfLong
                extends OfPrimitive<Long, LongConsumer, ArrayBuffer.OfLong, Spliterator.OfLong>
                implements Spliterator.OfLong, LongConsumer {

            long tmpValue;

            OfLong(Spliterator.OfLong s, long skip, long limit) {
                super(s, skip, limit);
            }

            OfLong(Spliterator.OfLong s, UnorderedSliceSpliterator.OfLong parent) {
                super(s, parent);
            }

            @Override
            public void accept(long value) {
                tmpValue = value;
            }

            @Override
            protected void acceptConsumed(LongConsumer action) {
                action.accept(tmpValue);
            }

            @Override
            protected ArrayBuffer.OfLong bufferCreate(int initialCapacity) {
                return new ArrayBuffer.OfLong(initialCapacity);
            }

            @Override
            protected Spliterator.OfLong makeSpliterator(Spliterator.OfLong s) {
                return new UnorderedSliceSpliterator.OfLong(s, this);
            }
        }

        static final class OfDouble
                extends OfPrimitive<Double, DoubleConsumer, ArrayBuffer.OfDouble, Spliterator.OfDouble>
                implements Spliterator.OfDouble, DoubleConsumer {

            double tmpValue;

            OfDouble(Spliterator.OfDouble s, long skip, long limit) {
                super(s, skip, limit);
            }

            OfDouble(Spliterator.OfDouble s, UnorderedSliceSpliterator.OfDouble parent) {
                super(s, parent);
            }

            @Override
            public void accept(double value) {
                tmpValue = value;
            }

            @Override
            protected void acceptConsumed(DoubleConsumer action) {
                action.accept(tmpValue);
            }

            @Override
            protected ArrayBuffer.OfDouble bufferCreate(int initialCapacity) {
                return new ArrayBuffer.OfDouble(initialCapacity);
            }

            @Override
            protected Spliterator.OfDouble makeSpliterator(Spliterator.OfDouble s) {
                return new UnorderedSliceSpliterator.OfDouble(s, this);
            }
        }
    }

    /**
     * 一个包装的 spliterator，仅报告底层 spliterator 的不同元素。不保留大小和遇到的顺序。
     */
    static final class DistinctSpliterator<T> implements Spliterator<T>, Consumer<T> {

        // 用于在 ConcurrentHashMap 中表示 null 的值
        private static final Object NULL_VALUE = new Object();

        // 底层的 spliterator
        private final Spliterator<T> s;

        // 以不同元素为键的 ConcurrentHashMap
        private final ConcurrentHashMap<T, Boolean> seen;

        // 临时元素，仅用于 tryAdvance
        private T tmpSlot;

        DistinctSpliterator(Spliterator<T> s) {
            this(s, new ConcurrentHashMap<>());
        }

        private DistinctSpliterator(Spliterator<T> s, ConcurrentHashMap<T, Boolean> seen) {
            this.s = s;
            this.seen = seen;
        }

        @Override
        public void accept(T t) {
            this.tmpSlot = t;
        }

        @SuppressWarnings("unchecked")
        private T mapNull(T t) {
            return t != null ? t : (T) NULL_VALUE;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            while (s.tryAdvance(this)) {
                if (seen.putIfAbsent(mapNull(tmpSlot), Boolean.TRUE) == null) {
                    action.accept(tmpSlot);
                    tmpSlot = null;
                    return true;
                }
            }
            return false;
        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            s.forEachRemaining(t -> {
                if (seen.putIfAbsent(mapNull(t), Boolean.TRUE) == null) {
                    action.accept(t);
                }
            });
        }


                    @Override
        public Spliterator<T> trySplit() {
            Spliterator<T> split = s.trySplit();
            return (split != null) ? new DistinctSpliterator<>(split, seen) : null;
        }

        @Override
        public long estimateSize() {
            return s.estimateSize();
        }

        @Override
        public int characteristics() {
            return (s.characteristics() & ~(Spliterator.SIZED | Spliterator.SUBSIZED |
                                            Spliterator.SORTED | Spliterator.ORDERED))
                   | Spliterator.DISTINCT;
        }

        @Override
        public Comparator<? super T> getComparator() {
            return s.getComparator();
        }
    }

    /**
     * 一个无限供应元素的 Spliterator，没有特定的顺序。
     *
     * <p>拆分时将估计大小分为两部分，当估计大小为 0 时停止拆分。
     *
     * <p>如果调用 {@code forEachRemaining} 方法，它将永远不会终止。
     * {@code tryAdvance} 方法总是返回 true。
     *
     */
    static abstract class InfiniteSupplyingSpliterator<T> implements Spliterator<T> {
        long estimate;

        protected InfiniteSupplyingSpliterator(long estimate) {
            this.estimate = estimate;
        }

        @Override
        public long estimateSize() {
            return estimate;
        }

        @Override
        public int characteristics() {
            return IMMUTABLE;
        }

        static final class OfRef<T> extends InfiniteSupplyingSpliterator<T> {
            final Supplier<T> s;

            OfRef(long size, Supplier<T> s) {
                super(size);
                this.s = s;
            }

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                Objects.requireNonNull(action);

                action.accept(s.get());
                return true;
            }

            @Override
            public Spliterator<T> trySplit() {
                if (estimate == 0)
                    return null;
                return new InfiniteSupplyingSpliterator.OfRef<>(estimate >>>= 1, s);
            }
        }

        static final class OfInt extends InfiniteSupplyingSpliterator<Integer>
                implements Spliterator.OfInt {
            final IntSupplier s;

            OfInt(long size, IntSupplier s) {
                super(size);
                this.s = s;
            }

            @Override
            public boolean tryAdvance(IntConsumer action) {
                Objects.requireNonNull(action);

                action.accept(s.getAsInt());
                return true;
            }

            @Override
            public Spliterator.OfInt trySplit() {
                if (estimate == 0)
                    return null;
                return new InfiniteSupplyingSpliterator.OfInt(estimate = estimate >>> 1, s);
            }
        }

        static final class OfLong extends InfiniteSupplyingSpliterator<Long>
                implements Spliterator.OfLong {
            final LongSupplier s;

            OfLong(long size, LongSupplier s) {
                super(size);
                this.s = s;
            }

            @Override
            public boolean tryAdvance(LongConsumer action) {
                Objects.requireNonNull(action);

                action.accept(s.getAsLong());
                return true;
            }

            @Override
            public Spliterator.OfLong trySplit() {
                if (estimate == 0)
                    return null;
                return new InfiniteSupplyingSpliterator.OfLong(estimate = estimate >>> 1, s);
            }
        }

        static final class OfDouble extends InfiniteSupplyingSpliterator<Double>
                implements Spliterator.OfDouble {
            final DoubleSupplier s;

            OfDouble(long size, DoubleSupplier s) {
                super(size);
                this.s = s;
            }

            @Override
            public boolean tryAdvance(DoubleConsumer action) {
                Objects.requireNonNull(action);

                action.accept(s.getAsDouble());
                return true;
            }

            @Override
            public Spliterator.OfDouble trySplit() {
                if (estimate == 0)
                    return null;
                return new InfiniteSupplyingSpliterator.OfDouble(estimate = estimate >>> 1, s);
            }
        }
    }

    // @@@ 与 Node.Builder 合并
    static abstract class ArrayBuffer {
        int index;

        void reset() {
            index = 0;
        }

        static final class OfRef<T> extends ArrayBuffer implements Consumer<T> {
            final Object[] array;

            OfRef(int size) {
                this.array = new Object[size];
            }

            @Override
            public void accept(T t) {
                array[index++] = t;
            }

            public void forEach(Consumer<? super T> action, long fence) {
                for (int i = 0; i < fence; i++) {
                    @SuppressWarnings("unchecked")
                    T t = (T) array[i];
                    action.accept(t);
                }
            }
        }

        static abstract class OfPrimitive<T_CONS> extends ArrayBuffer {
            int index;

            @Override
            void reset() {
                index = 0;
            }

            abstract void forEach(T_CONS action, long fence);
        }

        static final class OfInt extends OfPrimitive<IntConsumer>
                implements IntConsumer {
            final int[] array;

            OfInt(int size) {
                this.array = new int[size];
            }

            @Override
            public void accept(int t) {
                array[index++] = t;
            }

            @Override
            public void forEach(IntConsumer action, long fence) {
                for (int i = 0; i < fence; i++) {
                    action.accept(array[i]);
                }
            }
        }

        static final class OfLong extends OfPrimitive<LongConsumer>
                implements LongConsumer {
            final long[] array;

            OfLong(int size) {
                this.array = new long[size];
            }

            @Override
            public void accept(long t) {
                array[index++] = t;
            }

            @Override
            public void forEach(LongConsumer action, long fence) {
                for (int i = 0; i < fence; i++) {
                    action.accept(array[i]);
                }
            }
        }

        static final class OfDouble extends OfPrimitive<DoubleConsumer>
                implements DoubleConsumer {
            final double[] array;

            OfDouble(int size) {
                this.array = new double[size];
            }

            @Override
            public void accept(double t) {
                array[index++] = t;
            }

            @Override
            void forEach(DoubleConsumer action, long fence) {
                for (int i = 0; i < fence; i++) {
                    action.accept(array[i]);
                }
            }
        }
    }
}
