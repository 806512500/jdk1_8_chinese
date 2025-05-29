
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

import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/**
 * 用于操作和创建流的实用方法。
 *
 * <p>除非另有说明，流是作为顺序流创建的。 顺序流可以通过调用创建的流上的
 * {@code parallel()} 方法转换为并行流。
 *
 * @since 1.8
 */
final class Streams {

    private Streams() {
        throw new Error("no instances");
    }

    /**
     * 一个表示没有值的对象实例，不能是流的实际数据元素。 用于处理可能包含
     * {@code null} 元素的流，以区分 {@code null} 值和没有值。
     */
    static final Object NONE = new Object();

    /**
     * 一个 {@code int} 范围分段器。
     */
    static final class RangeIntSpliterator implements Spliterator.OfInt {
        // 永远不能大于 upTo，这避免了上界为 Integer.MAX_VALUE 时的溢出
        // 如果 from == upTo & last == 0，则遍历所有元素
        private int from;
        private final int upTo;
        // 如果范围是闭合的，并且最后一个元素尚未遍历，则为 1
        // 否则，如果范围是开放的，或者是一个闭合范围并且所有元素都已遍历，则为 0
        private int last;

        RangeIntSpliterator(int from, int upTo, boolean closed) {
            this(from, upTo, closed ? 1 : 0);
        }

        private RangeIntSpliterator(int from, int upTo, int last) {
            this.from = from;
            this.upTo = upTo;
            this.last = last;
        }

        @Override
        public boolean tryAdvance(IntConsumer consumer) {
            Objects.requireNonNull(consumer);

            final int i = from;
            if (i < upTo) {
                from++;
                consumer.accept(i);
                return true;
            }
            else if (last > 0) {
                last = 0;
                consumer.accept(i);
                return true;
            }
            return false;
        }

        @Override
        public void forEachRemaining(IntConsumer consumer) {
            Objects.requireNonNull(consumer);

            int i = from;
            final int hUpTo = upTo;
            int hLast = last;
            from = upTo;
            last = 0;
            while (i < hUpTo) {
                consumer.accept(i++);
            }
            if (hLast > 0) {
                // 闭合范围的最后一个元素
                consumer.accept(i);
            }
        }

        @Override
        public long estimateSize() {
            // 确保大小大于 Integer.MAX_VALUE 的范围报告正确的大小
            return ((long) upTo) - from + last;
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED |
                   Spliterator.IMMUTABLE | Spliterator.NONNULL |
                   Spliterator.DISTINCT | Spliterator.SORTED;
        }

        @Override
        public Comparator<? super Integer> getComparator() {
            return null;
        }

        @Override
        public Spliterator.OfInt trySplit() {
            long size = estimateSize();
            return size <= 1
                   ? null
                   // 左分段始终具有半开范围
                   : new RangeIntSpliterator(from, from = from + splitPoint(size), 0);
        }

        /**
         * 分段器大小低于此阈值时，分段器将在中点分割以产生平衡的分段。高于此大小时，
         * 分段器将以 1:(RIGHT_BALANCED_SPLIT_RATIO - 1) 的比例分割，以产生右平衡的分段。
         *
         * <p>这种分割确保对于非常大的范围，范围的左侧更可能在较低深度处理，而不是平衡树的右侧，
         * 以牺牲右侧范围的较高深度为代价。
         *
         * <p>这针对诸如 IntStream.ints() 的情况进行了优化，该实现是 0 到 Integer.MAX_VALUE 的范围，
         * 但可能通过限制操作将元素数量限制为低于此阈值的计数。
         */
        private static final int BALANCED_SPLIT_THRESHOLD = 1 << 24;

        /**
         * 当分段器大小高于 BALANCED_SPLIT_THRESHOLD 时，左分段和右分段的分割比例。
         */
        private static final int RIGHT_BALANCED_SPLIT_RATIO = 1 << 3;

        private int splitPoint(long size) {
            int d = (size < BALANCED_SPLIT_THRESHOLD) ? 2 : RIGHT_BALANCED_SPLIT_RATIO;
            // 转换为 int 是安全的，因为：
            //   2 <= size < 2^32
            //   2 <= d <= 8
            return (int) (size / d);
        }
    }

    /**
     * 一个 {@code long} 范围分段器。
     *
     * 该实现不能用于大小大于 Long.MAX_VALUE 的范围。
     */
    static final class RangeLongSpliterator implements Spliterator.OfLong {
        // 永远不能大于 upTo，这避免了上界为 Long.MAX_VALUE 时的溢出
        // 如果 from == upTo & last == 0，则遍历所有元素
        private long from;
        private final long upTo;
        // 如果范围是闭合的，并且最后一个元素尚未遍历，则为 1
        // 否则，如果范围是开放的，或者是一个闭合范围并且所有元素都已遍历，则为 0
        private int last;

        RangeLongSpliterator(long from, long upTo, boolean closed) {
            this(from, upTo, closed ? 1 : 0);
        }


                    private RangeLongSpliterator(long from, long upTo, int last) {
            assert upTo - from + last > 0;
            this.from = from;
            this.upTo = upTo;
            this.last = last;
        }

        @Override
        public boolean tryAdvance(LongConsumer consumer) {
            Objects.requireNonNull(consumer);

            final long i = from;
            if (i < upTo) {
                from++;
                consumer.accept(i);
                return true;
            }
            else if (last > 0) {
                last = 0;
                consumer.accept(i);
                return true;
            }
            return false;
        }

        @Override
        public void forEachRemaining(LongConsumer consumer) {
            Objects.requireNonNull(consumer);

            long i = from;
            final long hUpTo = upTo;
            int hLast = last;
            from = upTo;
            last = 0;
            while (i < hUpTo) {
                consumer.accept(i++);
            }
            if (hLast > 0) {
                // 最后一个闭合范围的元素
                consumer.accept(i);
            }
        }

        @Override
        public long estimateSize() {
            return upTo - from + last;
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED |
                   Spliterator.IMMUTABLE | Spliterator.NONNULL |
                   Spliterator.DISTINCT | Spliterator.SORTED;
        }

        @Override
        public Comparator<? super Long> getComparator() {
            return null;
        }

        @Override
        public Spliterator.OfLong trySplit() {
            long size = estimateSize();
            return size <= 1
                   ? null
                   // 左侧分割总是具有半开范围
                   : new RangeLongSpliterator(from, from = from + splitPoint(size), 0);
        }

        /**
         * 当分隔器大小低于此阈值时，分隔器将在中点处分割以产生平衡的分割。超过此大小时，分隔器将按
         * 1:(RIGHT_BALANCED_SPLIT_RATIO - 1)
         * 的比例分割，以产生右侧平衡的分割。
         *
         * <p>这种分割确保对于非常大的范围，范围的左侧更可能在较低深度处理，而右侧的范围则以较高深度处理。
         *
         * <p>这针对如 LongStream.longs() 这样的情况进行了优化，该方法实现为从 0 到 Long.MAX_VALUE 的范围，但可能通过限制操作将元素数量限制为低于此阈值的计数。
         */
        private static final long BALANCED_SPLIT_THRESHOLD = 1 << 24;

        /**
         * 当分隔器大小超过 BALANCED_SPLIT_THRESHOLD 时，左侧和右侧分割的比例。
         */
        private static final long RIGHT_BALANCED_SPLIT_RATIO = 1 << 3;

        private long splitPoint(long size) {
            long d = (size < BALANCED_SPLIT_THRESHOLD) ? 2 : RIGHT_BALANCED_SPLIT_RATIO;
            // 2 <= size <= Long.MAX_VALUE
            return size / d;
        }
    }

    private static abstract class AbstractStreamBuilderImpl<T, S extends Spliterator<T>> implements Spliterator<T> {
        // 构建时 >= 0，构建后 < 0
        // -1 表示没有元素
        // -2 表示一个元素，由 first 持有
        // -3 表示两个或更多元素，由 buffer 持有
        int count;

        // 0 或 1 个元素的分隔器实现
        // count == -1 表示没有元素
        // count == -2 表示一个元素由 first 持有

        @Override
        public S trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return -count - 1;
        }

        @Override
        public int characteristics() {
            return Spliterator.SIZED | Spliterator.SUBSIZED |
                   Spliterator.ORDERED | Spliterator.IMMUTABLE;
        }
    }

    static final class StreamBuilderImpl<T>
            extends AbstractStreamBuilderImpl<T, Spliterator<T>>
            implements Stream.Builder<T> {
        // 流中的第一个元素
        // 当 count == 1 时有效
        T first;

        // 流中的第一个和后续元素
        // 当 count == 2 时非空
        SpinedBuffer<T> buffer;

        /**
         * 构造函数，用于构建包含 0 个或更多元素的流。
         */
        StreamBuilderImpl() { }

        /**
         * 单元素流的构造函数。
         *
         * @param t 单个元素
         */
        StreamBuilderImpl(T t) {
            first = t;
            count = -2;
        }

        // StreamBuilder 实现

        @Override
        public void accept(T t) {
            if (count == 0) {
                first = t;
                count++;
            }
            else if (count > 0) {
                if (buffer == null) {
                    buffer = new SpinedBuffer<>();
                    buffer.accept(first);
                    count++;
                }

                buffer.accept(t);
            }
            else {
                throw new IllegalStateException();
            }
        }

        public Stream.Builder<T> add(T t) {
            accept(t);
            return this;
        }

        @Override
        public Stream<T> build() {
            int c = count;
            if (c >= 0) {
                // 将 count 切换为负值，表示构建器已构建
                count = -count - 1;
                // 如果有 0 或 1 个元素，使用此分隔器，否则使用 SpinedBuffer 的分隔器
                return (c < 2) ? StreamSupport.stream(this, false) : StreamSupport.stream(buffer.spliterator(), false);
            }

            throw new IllegalStateException();
        }

        // 0 或 1 个元素的分隔器实现
        // count == -1 表示没有元素
        // count == -2 表示一个元素由 first 持有


                    @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            Objects.requireNonNull(action);

            if (count == -2) {
                action.accept(first);
                count = -1;
                return true;
            }
            else {
                return false;
            }
        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            Objects.requireNonNull(action);

            if (count == -2) {
                action.accept(first);
                count = -1;
            }
        }
    }

    static final class IntStreamBuilderImpl
            extends AbstractStreamBuilderImpl<Integer, Spliterator.OfInt>
            implements IntStream.Builder, Spliterator.OfInt {
        // 流中的第一个元素
        // 当 count == 1 时有效
        int first;

        // 流中的第一个和后续元素
        // 当 count == 2 时非空
        SpinedBuffer.OfInt buffer;

        /**
         * 构造函数，用于构建包含 0 个或多个元素的流。
         */
        IntStreamBuilderImpl() { }

        /**
         * 构造函数，用于构建单元素流。
         *
         * @param t 单个元素
         */
        IntStreamBuilderImpl(int t) {
            first = t;
            count = -2;
        }

        // StreamBuilder 实现

        @Override
        public void accept(int t) {
            if (count == 0) {
                first = t;
                count++;
            }
            else if (count > 0) {
                if (buffer == null) {
                    buffer = new SpinedBuffer.OfInt();
                    buffer.accept(first);
                    count++;
                }

                buffer.accept(t);
            }
            else {
                throw new IllegalStateException();
            }
        }

        @Override
        public IntStream build() {
            int c = count;
            if (c >= 0) {
                // 将 count 切换为负值，表示构建器已构建
                count = -count - 1;
                // 如果有 0 或 1 个元素，使用此 spliterator，否则使用 spined buffer 的 spliterator
                return (c < 2) ? StreamSupport.intStream(this, false) : StreamSupport.intStream(buffer.spliterator(), false);
            }

            throw new IllegalStateException();
        }

        // 0 或 1 个元素的 Spliterator 实现
        // count == -1 表示没有元素
        // count == -2 表示由 first 持有的一个元素

        @Override
        public boolean tryAdvance(IntConsumer action) {
            Objects.requireNonNull(action);

            if (count == -2) {
                action.accept(first);
                count = -1;
                return true;
            }
            else {
                return false;
            }
        }

        @Override
        public void forEachRemaining(IntConsumer action) {
            Objects.requireNonNull(action);

            if (count == -2) {
                action.accept(first);
                count = -1;
            }
        }
    }

    static final class LongStreamBuilderImpl
            extends AbstractStreamBuilderImpl<Long, Spliterator.OfLong>
            implements LongStream.Builder, Spliterator.OfLong {
        // 流中的第一个元素
        // 当 count == 1 时有效
        long first;

        // 流中的第一个和后续元素
        // 当 count == 2 时非空
        SpinedBuffer.OfLong buffer;

        /**
         * 构造函数，用于构建包含 0 个或多个元素的流。
         */
        LongStreamBuilderImpl() { }

        /**
         * 构造函数，用于构建单元素流。
         *
         * @param t 单个元素
         */
        LongStreamBuilderImpl(long t) {
            first = t;
            count = -2;
        }

        // StreamBuilder 实现

        @Override
        public void accept(long t) {
            if (count == 0) {
                first = t;
                count++;
            }
            else if (count > 0) {
                if (buffer == null) {
                    buffer = new SpinedBuffer.OfLong();
                    buffer.accept(first);
                    count++;
                }

                buffer.accept(t);
            }
            else {
                throw new IllegalStateException();
            }
        }

        @Override
        public LongStream build() {
            int c = count;
            if (c >= 0) {
                // 将 count 切换为负值，表示构建器已构建
                count = -count - 1;
                // 如果有 0 或 1 个元素，使用此 spliterator，否则使用 spined buffer 的 spliterator
                return (c < 2) ? StreamSupport.longStream(this, false) : StreamSupport.longStream(buffer.spliterator(), false);
            }

            throw new IllegalStateException();
        }

        // 0 或 1 个元素的 Spliterator 实现
        // count == -1 表示没有元素
        // count == -2 表示由 first 持有的一个元素

        @Override
        public boolean tryAdvance(LongConsumer action) {
            Objects.requireNonNull(action);

            if (count == -2) {
                action.accept(first);
                count = -1;
                return true;
            }
            else {
                return false;
            }
        }

        @Override
        public void forEachRemaining(LongConsumer action) {
            Objects.requireNonNull(action);

            if (count == -2) {
                action.accept(first);
                count = -1;
            }
        }
    }

    static final class DoubleStreamBuilderImpl
            extends AbstractStreamBuilderImpl<Double, Spliterator.OfDouble>
            implements DoubleStream.Builder, Spliterator.OfDouble {
        // 流中的第一个元素
        // 当 count == 1 时有效
        double first;

        // 流中的第一个和后续元素
        // 当 count == 2 时非空
        SpinedBuffer.OfDouble buffer;


                    /**
         * 构造函数，用于构建包含0个或多个元素的流。
         */
        DoubleStreamBuilderImpl() { }

        /**
         * 单元素流的构造函数。
         *
         * @param t 单个元素
         */
        DoubleStreamBuilderImpl(double t) {
            first = t;
            count = -2;
        }

        // StreamBuilder 实现

        @Override
        public void accept(double t) {
            if (count == 0) {
                first = t;
                count++;
            }
            else if (count > 0) {
                if (buffer == null) {
                    buffer = new SpinedBuffer.OfDouble();
                    buffer.accept(first);
                    count++;
                }

                buffer.accept(t);
            }
            else {
                throw new IllegalStateException();
            }
        }

        @Override
        public DoubleStream build() {
            int c = count;
            if (c >= 0) {
                // 将计数切换为负值，表示构建器已构建
                count = -count - 1;
                // 如果元素为0或1，则使用此拆分器，否则使用
                // 脊缓冲区的拆分器
                return (c < 2) ? StreamSupport.doubleStream(this, false) : StreamSupport.doubleStream(buffer.spliterator(), false);
            }

            throw new IllegalStateException();
        }

        // 0或1个元素的拆分器实现
        // count == -1 表示没有元素
        // count == -2 表示由 first 持有一个元素

        @Override
        public boolean tryAdvance(DoubleConsumer action) {
            Objects.requireNonNull(action);

            if (count == -2) {
                action.accept(first);
                count = -1;
                return true;
            }
            else {
                return false;
            }
        }

        @Override
        public void forEachRemaining(DoubleConsumer action) {
            Objects.requireNonNull(action);

            if (count == -2) {
                action.accept(first);
                count = -1;
            }
        }
    }

    abstract static class ConcatSpliterator<T, T_SPLITR extends Spliterator<T>>
            implements Spliterator<T> {
        protected final T_SPLITR aSpliterator;
        protected final T_SPLITR bSpliterator;
        // 如果没有发生拆分则为 true，否则为 false
        boolean beforeSplit;
        // 拆分后不再读取
        final boolean unsized;

        public ConcatSpliterator(T_SPLITR aSpliterator, T_SPLITR bSpliterator) {
            this.aSpliterator = aSpliterator;
            this.bSpliterator = bSpliterator;
            beforeSplit = true;
            // 如果估计值的总和溢出，则在拆分前拆分器被认为是无界的
            unsized = aSpliterator.estimateSize() + bSpliterator.estimateSize() < 0;
        }

        @Override
        public T_SPLITR trySplit() {
            @SuppressWarnings("unchecked")
            T_SPLITR ret = beforeSplit ? aSpliterator : (T_SPLITR) bSpliterator.trySplit();
            beforeSplit = false;
            return ret;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> consumer) {
            boolean hasNext;
            if (beforeSplit) {
                hasNext = aSpliterator.tryAdvance(consumer);
                if (!hasNext) {
                    beforeSplit = false;
                    hasNext = bSpliterator.tryAdvance(consumer);
                }
            }
            else
                hasNext = bSpliterator.tryAdvance(consumer);
            return hasNext;
        }

        @Override
        public void forEachRemaining(Consumer<? super T> consumer) {
            if (beforeSplit)
                aSpliterator.forEachRemaining(consumer);
            bSpliterator.forEachRemaining(consumer);
        }

        @Override
        public long estimateSize() {
            if (beforeSplit) {
                // 如果一个或两个估计值为 Long.MAX_VALUE，则总和
                // 将要么是 Long.MAX_VALUE，要么溢出为负值
                long size = aSpliterator.estimateSize() + bSpliterator.estimateSize();
                return (size >= 0) ? size : Long.MAX_VALUE;
            }
            else {
                return bSpliterator.estimateSize();
            }
        }

        @Override
        public int characteristics() {
            if (beforeSplit) {
                // 连接会丢失 DISTINCT 和 SORTED 特性
                return aSpliterator.characteristics() & bSpliterator.characteristics()
                       & ~(Spliterator.DISTINCT | Spliterator.SORTED
                           | (unsized ? Spliterator.SIZED | Spliterator.SUBSIZED : 0));
            }
            else {
                return bSpliterator.characteristics();
            }
        }

        @Override
        public Comparator<? super T> getComparator() {
            if (beforeSplit)
                throw new IllegalStateException();
            return bSpliterator.getComparator();
        }

        static class OfRef<T> extends ConcatSpliterator<T, Spliterator<T>> {
            OfRef(Spliterator<T> aSpliterator, Spliterator<T> bSpliterator) {
                super(aSpliterator, bSpliterator);
            }
        }

        private static abstract class OfPrimitive<T, T_CONS, T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>>
                extends ConcatSpliterator<T, T_SPLITR>
                implements Spliterator.OfPrimitive<T, T_CONS, T_SPLITR> {
            private OfPrimitive(T_SPLITR aSpliterator, T_SPLITR bSpliterator) {
                super(aSpliterator, bSpliterator);
            }

            @Override
            public boolean tryAdvance(T_CONS action) {
                boolean hasNext;
                if (beforeSplit) {
                    hasNext = aSpliterator.tryAdvance(action);
                    if (!hasNext) {
                        beforeSplit = false;
                        hasNext = bSpliterator.tryAdvance(action);
                    }
                }
                else
                    hasNext = bSpliterator.tryAdvance(action);
                return hasNext;
            }


                        @Override
            public void forEachRemaining(T_CONS action) {
                if (beforeSplit)
                    aSpliterator.forEachRemaining(action);
                bSpliterator.forEachRemaining(action);
            }
        }

        static class OfInt
                extends ConcatSpliterator.OfPrimitive<Integer, IntConsumer, Spliterator.OfInt>
                implements Spliterator.OfInt {
            OfInt(Spliterator.OfInt aSpliterator, Spliterator.OfInt bSpliterator) {
                super(aSpliterator, bSpliterator);
            }
        }

        static class OfLong
                extends ConcatSpliterator.OfPrimitive<Long, LongConsumer, Spliterator.OfLong>
                implements Spliterator.OfLong {
            OfLong(Spliterator.OfLong aSpliterator, Spliterator.OfLong bSpliterator) {
                super(aSpliterator, bSpliterator);
            }
        }

        static class OfDouble
                extends ConcatSpliterator.OfPrimitive<Double, DoubleConsumer, Spliterator.OfDouble>
                implements Spliterator.OfDouble {
            OfDouble(Spliterator.OfDouble aSpliterator, Spliterator.OfDouble bSpliterator) {
                super(aSpliterator, bSpliterator);
            }
        }
    }

    /**
     * 给定两个 Runnable，返回一个 Runnable，该 Runnable 按顺序执行两者，
     * 即使第一个抛出异常，如果两者都抛出异常，则将第二个抛出的任何异常作为第一个异常的附加异常。
     */
    static Runnable composeWithExceptions(Runnable a, Runnable b) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    a.run();
                }
                catch (Throwable e1) {
                    try {
                        b.run();
                    }
                    catch (Throwable e2) {
                        try {
                            e1.addSuppressed(e2);
                        } catch (Throwable ignore) {}
                    }
                    throw e1;
                }
                b.run();
            }
        };
    }

    /**
     * 给定两个流，返回一个 Runnable，该 Runnable 按顺序执行它们的 {@link BaseStream#close} 方法，
     * 即使第一个抛出异常，如果两者都抛出异常，则将第二个抛出的任何异常作为第一个异常的附加异常。
     */
    static Runnable composedClose(BaseStream<?, ?> a, BaseStream<?, ?> b) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    a.close();
                }
                catch (Throwable e1) {
                    try {
                        b.close();
                    }
                    catch (Throwable e2) {
                        try {
                            e1.addSuppressed(e2);
                        } catch (Throwable ignore) {}
                    }
                    throw e1;
                }
                b.close();
            }
        };
    }
}
