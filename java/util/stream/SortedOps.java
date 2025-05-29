
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.IntFunction;


/**
 * 用于将流转换为排序流的工厂方法。
 *
 * @since 1.8
 */
final class SortedOps {

    private SortedOps() { }

    /**
     * 向提供的流追加一个“排序”操作。
     *
     * @param <T> 输入和输出元素的类型
     * @param upstream 具有元素类型 T 的引用流
     */
    static <T> Stream<T> makeRef(AbstractPipeline<?, T, ?> upstream) {
        return new OfRef<>(upstream);
    }

    /**
     * 向提供的流追加一个“排序”操作。
     *
     * @param <T> 输入和输出元素的类型
     * @param upstream 具有元素类型 T 的引用流
     * @param comparator 用于排序元素的比较器
     */
    static <T> Stream<T> makeRef(AbstractPipeline<?, T, ?> upstream,
                                Comparator<? super T> comparator) {
        return new OfRef<>(upstream, comparator);
    }

    /**
     * 向提供的流追加一个“排序”操作。
     *
     * @param <T> 输入和输出元素的类型
     * @param upstream 具有元素类型 T 的引用流
     */
    static <T> IntStream makeInt(AbstractPipeline<?, Integer, ?> upstream) {
        return new OfInt(upstream);
    }

    /**
     * 向提供的流追加一个“排序”操作。
     *
     * @param <T> 输入和输出元素的类型
     * @param upstream 具有元素类型 T 的引用流
     */
    static <T> LongStream makeLong(AbstractPipeline<?, Long, ?> upstream) {
        return new OfLong(upstream);
    }

    /**
     * 向提供的流追加一个“排序”操作。
     *
     * @param <T> 输入和输出元素的类型
     * @param upstream 具有元素类型 T 的引用流
     */
    static <T> DoubleStream makeDouble(AbstractPipeline<?, Double, ?> upstream) {
        return new OfDouble(upstream);
    }

    /**
     * 用于排序引用流的专用子类型
     */
    private static final class OfRef<T> extends ReferencePipeline.StatefulOp<T, T> {
        /**
         * 用于排序的比较器
         */
        private final boolean isNaturalSort;
        private final Comparator<? super T> comparator;

        /**
         * 使用 {@literal <T>} 的自然顺序进行排序，必须是 {@code Comparable}。
         */
        OfRef(AbstractPipeline<?, T, ?> upstream) {
            super(upstream, StreamShape.REFERENCE,
                  StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
            this.isNaturalSort = true;
            // 如果 T 不是 Comparable，尝试排序时会抛出 CCE
            @SuppressWarnings("unchecked")
            Comparator<? super T> comp = (Comparator<? super T>) Comparator.naturalOrder();
            this.comparator = comp;
        }

        /**
         * 使用提供的比较器进行排序。
         *
         * @param comparator 用于评估排序的比较器。
         */
        OfRef(AbstractPipeline<?, T, ?> upstream, Comparator<? super T> comparator) {
            super(upstream, StreamShape.REFERENCE,
                  StreamOpFlag.IS_ORDERED | StreamOpFlag.NOT_SORTED);
            this.isNaturalSort = false;
            this.comparator = Objects.requireNonNull(comparator);
        }

        @Override
        public Sink<T> opWrapSink(int flags, Sink<T> sink) {
            Objects.requireNonNull(sink);

            // 如果输入已经是自然排序且此操作也是自然排序，则此操作无效
            if (StreamOpFlag.SORTED.isKnown(flags) && isNaturalSort)
                return sink;
            else if (StreamOpFlag.SIZED.isKnown(flags))
                return new SizedRefSortingSink<>(sink, comparator);
            else
                return new RefSortingSink<>(sink, comparator);
        }

        @Override
        public <P_IN> Node<T> opEvaluateParallel(PipelineHelper<T> helper,
                                                 Spliterator<P_IN> spliterator,
                                                 IntFunction<T[]> generator) {
            // 如果输入已经是自然排序且此操作自然排序，则收集输出
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags()) && isNaturalSort) {
                return helper.evaluate(spliterator, false, generator);
            }
            else {
                // @@@ 弱两遍并行实现；并行收集，并行排序
                T[] flattenedData = helper.evaluate(spliterator, true, generator).asArray(generator);
                Arrays.parallelSort(flattenedData, comparator);
                return Nodes.node(flattenedData);
            }
        }
    }

    /**
     * 用于排序 int 流的专用子类型。
     */
    private static final class OfInt extends IntPipeline.StatefulOp<Integer> {
        OfInt(AbstractPipeline<?, Integer, ?> upstream) {
            super(upstream, StreamShape.INT_VALUE,
                  StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        @Override
        public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
            Objects.requireNonNull(sink);

            if (StreamOpFlag.SORTED.isKnown(flags))
                return sink;
            else if (StreamOpFlag.SIZED.isKnown(flags))
                return new SizedIntSortingSink(sink);
            else
                return new IntSortingSink(sink);
        }

        @Override
        public <P_IN> Node<Integer> opEvaluateParallel(PipelineHelper<Integer> helper,
                                                       Spliterator<P_IN> spliterator,
                                                       IntFunction<Integer[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            else {
                Node.OfInt n = (Node.OfInt) helper.evaluate(spliterator, true, generator);


                            int[] content = n.asPrimitiveArray();
                Arrays.parallelSort(content);

                return Nodes.node(content);
            }
        }
    }

    /**
     * 专门用于排序长整型流的子类型。
     */
    private static final class OfLong extends LongPipeline.StatefulOp<Long> {
        OfLong(AbstractPipeline<?, Long, ?> upstream) {
            super(upstream, StreamShape.LONG_VALUE,
                  StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        @Override
        public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
            Objects.requireNonNull(sink);

            if (StreamOpFlag.SORTED.isKnown(flags))
                return sink;
            else if (StreamOpFlag.SIZED.isKnown(flags))
                return new SizedLongSortingSink(sink);
            else
                return new LongSortingSink(sink);
        }

        @Override
        public <P_IN> Node<Long> opEvaluateParallel(PipelineHelper<Long> helper,
                                                    Spliterator<P_IN> spliterator,
                                                    IntFunction<Long[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            else {
                Node.OfLong n = (Node.OfLong) helper.evaluate(spliterator, true, generator);

                long[] content = n.asPrimitiveArray();
                Arrays.parallelSort(content);

                return Nodes.node(content);
            }
        }
    }

    /**
     * 专门用于排序双精度浮点型流的子类型。
     */
    private static final class OfDouble extends DoublePipeline.StatefulOp<Double> {
        OfDouble(AbstractPipeline<?, Double, ?> upstream) {
            super(upstream, StreamShape.DOUBLE_VALUE,
                  StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        @Override
        public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
            Objects.requireNonNull(sink);

            if (StreamOpFlag.SORTED.isKnown(flags))
                return sink;
            else if (StreamOpFlag.SIZED.isKnown(flags))
                return new SizedDoubleSortingSink(sink);
            else
                return new DoubleSortingSink(sink);
        }

        @Override
        public <P_IN> Node<Double> opEvaluateParallel(PipelineHelper<Double> helper,
                                                      Spliterator<P_IN> spliterator,
                                                      IntFunction<Double[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            else {
                Node.OfDouble n = (Node.OfDouble) helper.evaluate(spliterator, true, generator);

                double[] content = n.asPrimitiveArray();
                Arrays.parallelSort(content);

                return Nodes.node(content);
            }
        }
    }

    /**
     * 用于实现引用流排序的抽象 {@link Sink}。
     *
     * <p>
     * 注意：以下文档适用于引用和所有原始类型的 Sink。
     * <p>
     * 排序 Sink 首先接受所有元素，并将它们缓冲到数组或可调整大小的数据结构中，具体取决于管道的大小是已知的还是未知的。在 Sink 协议结束时，这些元素将被排序，然后推送到下游。
     * 该类记录了 {@link #cancellationRequested} 是否被调用。如果是这样，可以推断出将元素推入管道的源知道管道正在短路。在这种情况下，子类在将元素推送到下游时将通过调用 {@code downstream.cancellationRequested()} 并检查结果是否为 {@code false} 来保留短路协议。
     * <p>
     * 注意，上述行为是针对顺序流排序的优化。更多的元素（比严格需要的多）可能通过管道流动并不是错误。这可能发生在一般情况下（不仅限于排序），对于短路并行管道。
     */
    private static abstract class AbstractRefSortingSink<T> extends Sink.ChainedReference<T, T> {
        protected final Comparator<? super T> comparator;
        // @@@ 如果/当支持添加时，可以是延迟的最终值
        protected boolean cancellationWasRequested;

        AbstractRefSortingSink(Sink<? super T> downstream, Comparator<? super T> comparator) {
            super(downstream);
            this.comparator = comparator;
        }

        /**
         * 记录取消请求，以便在排序后的元素推送到下游时保留短路行为。
         *
         * @return false，因为此 Sink 从不短路。
         */
        @Override
        public final boolean cancellationRequested() {
            cancellationWasRequested = true;
            return false;
        }
    }

    /**
     * 用于实现 SIZED 引用流排序的 {@link Sink}。
     */
    private static final class SizedRefSortingSink<T> extends AbstractRefSortingSink<T> {
        private T[] array;
        private int offset;

        SizedRefSortingSink(Sink<? super T> sink, Comparator<? super T> comparator) {
            super(sink, comparator);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void begin(long size) {
            if (size >= Nodes.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            array = (T[]) new Object[(int) size];
        }

        @Override
        public void end() {
            Arrays.sort(array, 0, offset, comparator);
            downstream.begin(offset);
            if (!cancellationWasRequested) {
                for (int i = 0; i < offset; i++)
                    downstream.accept(array[i]);
            }
            else {
                for (int i = 0; i < offset && !downstream.cancellationRequested(); i++)
                    downstream.accept(array[i]);
            }
            downstream.end();
            array = null;
        }


                    @Override
        public void accept(T t) {
            array[offset++] = t;
        }
    }

    /**
     * 用于实现引用流排序的 {@link Sink}。
     */
    private static final class RefSortingSink<T> extends AbstractRefSortingSink<T> {
        private ArrayList<T> list;

        RefSortingSink(Sink<? super T> sink, Comparator<? super T> comparator) {
            super(sink, comparator);
        }

        @Override
        public void begin(long size) {
            if (size >= Nodes.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            list = (size >= 0) ? new ArrayList<T>((int) size) : new ArrayList<T>();
        }

        @Override
        public void end() {
            list.sort(comparator);
            downstream.begin(list.size());
            if (!cancellationWasRequested) {
                list.forEach(downstream::accept);
            }
            else {
                for (T t : list) {
                    if (downstream.cancellationRequested()) break;
                    downstream.accept(t);
                }
            }
            downstream.end();
            list = null;
        }

        @Override
        public void accept(T t) {
            list.add(t);
        }
    }

    /**
     * 用于实现 int 流排序的抽象 {@link Sink}。
     */
    private static abstract class AbstractIntSortingSink extends Sink.ChainedInt<Integer> {
        protected boolean cancellationWasRequested;

        AbstractIntSortingSink(Sink<? super Integer> downstream) {
            super(downstream);
        }

        @Override
        public final boolean cancellationRequested() {
            cancellationWasRequested = true;
            return false;
        }
    }

    /**
     * 用于实现 SIZED int 流排序的 {@link Sink}。
     */
    private static final class SizedIntSortingSink extends AbstractIntSortingSink {
        private int[] array;
        private int offset;

        SizedIntSortingSink(Sink<? super Integer> downstream) {
            super(downstream);
        }

        @Override
        public void begin(long size) {
            if (size >= Nodes.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            array = new int[(int) size];
        }

        @Override
        public void end() {
            Arrays.sort(array, 0, offset);
            downstream.begin(offset);
            if (!cancellationWasRequested) {
                for (int i = 0; i < offset; i++)
                    downstream.accept(array[i]);
            }
            else {
                for (int i = 0; i < offset && !downstream.cancellationRequested(); i++)
                    downstream.accept(array[i]);
            }
            downstream.end();
            array = null;
        }

        @Override
        public void accept(int t) {
            array[offset++] = t;
        }
    }

    /**
     * 用于实现 int 流排序的 {@link Sink}。
     */
    private static final class IntSortingSink extends AbstractIntSortingSink {
        private SpinedBuffer.OfInt b;

        IntSortingSink(Sink<? super Integer> sink) {
            super(sink);
        }

        @Override
        public void begin(long size) {
            if (size >= Nodes.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            b = (size > 0) ? new SpinedBuffer.OfInt((int) size) : new SpinedBuffer.OfInt();
        }

        @Override
        public void end() {
            int[] ints = b.asPrimitiveArray();
            Arrays.sort(ints);
            downstream.begin(ints.length);
            if (!cancellationWasRequested) {
                for (int anInt : ints)
                    downstream.accept(anInt);
            }
            else {
                for (int anInt : ints) {
                    if (downstream.cancellationRequested()) break;
                    downstream.accept(anInt);
                }
            }
            downstream.end();
        }

        @Override
        public void accept(int t) {
            b.accept(t);
        }
    }

    /**
     * 用于实现 long 流排序的抽象 {@link Sink}。
     */
    private static abstract class AbstractLongSortingSink extends Sink.ChainedLong<Long> {
        protected boolean cancellationWasRequested;

        AbstractLongSortingSink(Sink<? super Long> downstream) {
            super(downstream);
        }

        @Override
        public final boolean cancellationRequested() {
            cancellationWasRequested = true;
            return false;
        }
    }

    /**
     * 用于实现 SIZED long 流排序的 {@link Sink}。
     */
    private static final class SizedLongSortingSink extends AbstractLongSortingSink {
        private long[] array;
        private int offset;

        SizedLongSortingSink(Sink<? super Long> downstream) {
            super(downstream);
        }

        @Override
        public void begin(long size) {
            if (size >= Nodes.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            array = new long[(int) size];
        }

        @Override
        public void end() {
            Arrays.sort(array, 0, offset);
            downstream.begin(offset);
            if (!cancellationWasRequested) {
                for (int i = 0; i < offset; i++)
                    downstream.accept(array[i]);
            }
            else {
                for (int i = 0; i < offset && !downstream.cancellationRequested(); i++)
                    downstream.accept(array[i]);
            }
            downstream.end();
            array = null;
        }

        @Override
        public void accept(long t) {
            array[offset++] = t;
        }
    }

    /**
     * 用于实现 long 流排序的 {@link Sink}。
     */
    private static final class LongSortingSink extends AbstractLongSortingSink {
        private SpinedBuffer.OfLong b;

        LongSortingSink(Sink<? super Long> sink) {
            super(sink);
        }

        @Override
        public void begin(long size) {
            if (size >= Nodes.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            b = (size > 0) ? new SpinedBuffer.OfLong((int) size) : new SpinedBuffer.OfLong();
        }


                    @Override
        public void end() {
            long[] longs = b.asPrimitiveArray();
            Arrays.sort(longs);
            downstream.begin(longs.length);
            if (!cancellationWasRequested) {
                for (long aLong : longs)
                    downstream.accept(aLong);
            }
            else {
                for (long aLong : longs) {
                    if (downstream.cancellationRequested()) break;
                    downstream.accept(aLong);
                }
            }
            downstream.end();
        }

        @Override
        public void accept(long t) {
            b.accept(t);
        }
    }

    /**
     * 抽象 {@link Sink} 用于实现 long 流的排序。
     */
    private static abstract class AbstractDoubleSortingSink extends Sink.ChainedDouble<Double> {
        protected boolean cancellationWasRequested;

        AbstractDoubleSortingSink(Sink<? super Double> downstream) {
            super(downstream);
        }

        @Override
        public final boolean cancellationRequested() {
            cancellationWasRequested = true;
            return false;
        }
    }

    /**
     * {@link Sink} 用于实现 SIZED double 流的排序。
     */
    private static final class SizedDoubleSortingSink extends AbstractDoubleSortingSink {
        private double[] array;
        private int offset;

        SizedDoubleSortingSink(Sink<? super Double> downstream) {
            super(downstream);
        }

        @Override
        public void begin(long size) {
            if (size >= Nodes.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            array = new double[(int) size];
        }

        @Override
        public void end() {
            Arrays.sort(array, 0, offset);
            downstream.begin(offset);
            if (!cancellationWasRequested) {
                for (int i = 0; i < offset; i++)
                    downstream.accept(array[i]);
            }
            else {
                for (int i = 0; i < offset && !downstream.cancellationRequested(); i++)
                    downstream.accept(array[i]);
            }
            downstream.end();
            array = null;
        }

        @Override
        public void accept(double t) {
            array[offset++] = t;
        }
    }

    /**
     * {@link Sink} 用于实现 double 流的排序。
     */
    private static final class DoubleSortingSink extends AbstractDoubleSortingSink {
        private SpinedBuffer.OfDouble b;

        DoubleSortingSink(Sink<? super Double> sink) {
            super(sink);
        }

        @Override
        public void begin(long size) {
            if (size >= Nodes.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            b = (size > 0) ? new SpinedBuffer.OfDouble((int) size) : new SpinedBuffer.OfDouble();
        }

        @Override
        public void end() {
            double[] doubles = b.asPrimitiveArray();
            Arrays.sort(doubles);
            downstream.begin(doubles.length);
            if (!cancellationWasRequested) {
                for (double aDouble : doubles)
                    downstream.accept(aDouble);
            }
            else {
                for (double aDouble : doubles) {
                    if (downstream.cancellationRequested()) break;
                    downstream.accept(aDouble);
                }
            }
            downstream.end();
        }

        @Override
        public void accept(double t) {
            b.accept(t);
        }
    }
}
