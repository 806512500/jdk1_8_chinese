
/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
package java.util.stream;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CountedCompleter;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;

/**
 * 用于构建 {@link Node} 和 {@link Node.Builder} 及其原始类型特化实现的工厂方法。Fork/Join 任务用于从 {@link PipelineHelper} 收集输出到 {@link Node} 并展平 {@link Node}。
 *
 * @since 1.8
 */
final class Nodes {

    private Nodes() {
        throw new Error("no instances");
    }

    /**
     * 可分配数组的最大大小。
     */
    static final long MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    // IllegalArgumentException 消息
    static final String BAD_SIZE = "Stream size exceeds max array size";

    @SuppressWarnings("rawtypes")
    private static final Node EMPTY_NODE = new EmptyNode.OfRef();
    private static final Node.OfInt EMPTY_INT_NODE = new EmptyNode.OfInt();
    private static final Node.OfLong EMPTY_LONG_NODE = new EmptyNode.OfLong();
    private static final Node.OfDouble EMPTY_DOUBLE_NODE = new EmptyNode.OfDouble();

    // 基于形状的节点创建方法

    /**
     * 生成一个计数为零、没有子节点且没有内容的空节点。
     *
     * @param <T> 创建的节点的元素类型
     * @param shape 要创建的节点的形状
     * @return 一个空节点。
     */
    @SuppressWarnings("unchecked")
    static <T> Node<T> emptyNode(StreamShape shape) {
        switch (shape) {
            case REFERENCE:    return (Node<T>) EMPTY_NODE;
            case INT_VALUE:    return (Node<T>) EMPTY_INT_NODE;
            case LONG_VALUE:   return (Node<T>) EMPTY_LONG_NODE;
            case DOUBLE_VALUE: return (Node<T>) EMPTY_DOUBLE_NODE;
            default:
                throw new IllegalStateException("Unknown shape " + shape);
        }
    }

    /**
     * 生成一个具有两个或多个子节点的连接的 {@link Node}。
     * <p>连接节点的计数等于每个子节点的计数之和。遍历连接节点时，将按子节点列表的顺序遍历每个子节点的内容。从连接节点获取的迭代器的拆分保留子节点列表的顺序。
     *
     * <p>结果可能是一个连接节点，如果列表的大小为 1，则为输入的单个节点，或者为空节点。
     *
     * @param <T> 连接节点的元素类型
     * @param shape 要创建的连接节点的形状
     * @param left 左输入节点
     * @param right 右输入节点
     * @return 覆盖输入节点元素的 {@code Node}
     * @throws IllegalStateException 如果列表中的所有 {@link Node} 元素都不是此工厂支持的类型。
     */
    @SuppressWarnings("unchecked")
    static <T> Node<T> conc(StreamShape shape, Node<T> left, Node<T> right) {
        switch (shape) {
            case REFERENCE:
                return new ConcNode<>(left, right);
            case INT_VALUE:
                return (Node<T>) new ConcNode.OfInt((Node.OfInt) left, (Node.OfInt) right);
            case LONG_VALUE:
                return (Node<T>) new ConcNode.OfLong((Node.OfLong) left, (Node.OfLong) right);
            case DOUBLE_VALUE:
                return (Node<T>) new ConcNode.OfDouble((Node.OfDouble) left, (Node.OfDouble) right);
            default:
                throw new IllegalStateException("Unknown shape " + shape);
        }
    }

    // 基于引用的节点方法

    /**
     * 生成描述数组的 {@link Node}。
     *
     * <p>节点将引用数组，不会复制数组。
     *
     * @param <T> 节点持有的元素类型
     * @param array 数组
     * @return 持有数组的节点
     */
    static <T> Node<T> node(T[] array) {
        return new ArrayNode<>(array);
    }

    /**
     * 生成描述 {@link Collection} 的 {@link Node}。
     * <p>
     * 节点将引用集合，不会复制集合。
     *
     * @param <T> 节点持有的元素类型
     * @param c 集合
     * @return 持有集合的节点
     */
    static <T> Node<T> node(Collection<T> c) {
        return new CollectionNode<>(c);
    }

    /**
     * 生成一个 {@link Node.Builder}。
     *
     * @param exactSizeIfKnown 如果请求可变大小构建器，则为 -1，否则为所需的精确容量。如果添加到构建器的元素数量不正确，固定容量构建器将失败。
     * @param generator 数组工厂
     * @param <T> 节点构建器的元素类型
     * @return 一个 {@code Node.Builder}
     */
    static <T> Node.Builder<T> builder(long exactSizeIfKnown, IntFunction<T[]> generator) {
        return (exactSizeIfKnown >= 0 && exactSizeIfKnown < MAX_ARRAY_SIZE)
               ? new FixedNodeBuilder<>(exactSizeIfKnown, generator)
               : builder();
    }

    /**
     * 生成一个可变大小的 @{link Node.Builder}。
     *
     * @param <T> 节点构建器的元素类型
     * @return 一个 {@code Node.Builder}
     */
    static <T> Node.Builder<T> builder() {
        return new SpinedNodeBuilder<>();
    }

    // Int 节点

    /**
     * 生成描述 int[] 数组的 {@link Node.OfInt}。
     *
     * <p>节点将引用数组，不会复制数组。
     *
     * @param array 数组
     * @return 持有数组的节点
     */
    static Node.OfInt node(int[] array) {
        return new IntArrayNode(array);
    }


                /**
     * 生成一个 {@link Node.Builder.OfInt}。
     *
     * @param exactSizeIfKnown 如果请求的是可变大小的构建器，则为 -1；
     * 否则为所需的精确容量。如果添加到构建器的元素数量不正确，固定容量的构建器将失败。
     * @return 一个 {@code Node.Builder.OfInt}
     */
    static Node.Builder.OfInt intBuilder(long exactSizeIfKnown) {
        return (exactSizeIfKnown >= 0 && exactSizeIfKnown < MAX_ARRAY_SIZE)
               ? new IntFixedNodeBuilder(exactSizeIfKnown)
               : intBuilder();
    }

    /**
     * 生成一个可变大小的 @{link Node.Builder.OfInt}。
     *
     * @return 一个 {@code Node.Builder.OfInt}
     */
    static Node.Builder.OfInt intBuilder() {
        return new IntSpinedNodeBuilder();
    }

    // 长整型节点

    /**
     * 生成一个描述 long[] 数组的 {@link Node.OfLong}。
     * <p>
     * 该节点将持有数组的引用，而不会复制数组。
     *
     * @param array 数组
     * @return 持有数组的节点
     */
    static Node.OfLong node(final long[] array) {
        return new LongArrayNode(array);
    }

    /**
     * 生成一个 {@link Node.Builder.OfLong}。
     *
     * @param exactSizeIfKnown 如果请求的是可变大小的构建器，则为 -1；
     * 否则为所需的精确容量。如果添加到构建器的元素数量不正确，固定容量的构建器将失败。
     * @return 一个 {@code Node.Builder.OfLong}
     */
    static Node.Builder.OfLong longBuilder(long exactSizeIfKnown) {
        return (exactSizeIfKnown >= 0 && exactSizeIfKnown < MAX_ARRAY_SIZE)
               ? new LongFixedNodeBuilder(exactSizeIfKnown)
               : longBuilder();
    }

    /**
     * 生成一个可变大小的 @{link Node.Builder.OfLong}。
     *
     * @return 一个 {@code Node.Builder.OfLong}
     */
    static Node.Builder.OfLong longBuilder() {
        return new LongSpinedNodeBuilder();
    }

    // 双精度浮点型节点

    /**
     * 生成一个描述 double[] 数组的 {@link Node.OfDouble}。
     *
     * <p>该节点将持有数组的引用，而不会复制数组。
     *
     * @param array 数组
     * @return 持有数组的节点
     */
    static Node.OfDouble node(final double[] array) {
        return new DoubleArrayNode(array);
    }

    /**
     * 生成一个 {@link Node.Builder.OfDouble}。
     *
     * @param exactSizeIfKnown 如果请求的是可变大小的构建器，则为 -1；
     * 否则为所需的精确容量。如果添加到构建器的元素数量不正确，固定容量的构建器将失败。
     * @return 一个 {@code Node.Builder.OfDouble}
     */
    static Node.Builder.OfDouble doubleBuilder(long exactSizeIfKnown) {
        return (exactSizeIfKnown >= 0 && exactSizeIfKnown < MAX_ARRAY_SIZE)
               ? new DoubleFixedNodeBuilder(exactSizeIfKnown)
               : doubleBuilder();
    }

    /**
     * 生成一个可变大小的 @{link Node.Builder.OfDouble}。
     *
     * @return 一个 {@code Node.Builder.OfDouble}
     */
    static Node.Builder.OfDouble doubleBuilder() {
        return new DoubleSpinedNodeBuilder();
    }

    // 管道并行评估到节点

    /**
     * 并行收集管道输出的元素，并用一个 {@link Node} 描述这些元素。
     *
     * @implSpec
     * 如果管道输出的确切大小已知，并且源 {@link Spliterator} 具有 {@link Spliterator#SUBSIZED} 特性，
     * 则将返回一个内容为数组的扁平 {@link Node}，因为大小已知，可以在提前构造数组，并由叶任务在正确的偏移量处并发地将输出元素放入数组中。
     * 如果确切大小未知，则将输出元素收集到一个形状与计算相匹配的 conc-node 中。如果需要，可以并行地将此 conc-node 展平以生成一个扁平的 {@code Node}。
     *
     * @param helper 描述管道的管道助手
     * @param flattenTree 是否应在返回前将 conc-node 展平为描述数组的节点
     * @param generator 数组生成器
     * @return 描述输出元素的 {@link Node}
     */
    public static <P_IN, P_OUT> Node<P_OUT> collect(PipelineHelper<P_OUT> helper,
                                                    Spliterator<P_IN> spliterator,
                                                    boolean flattenTree,
                                                    IntFunction<P_OUT[]> generator) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size >= 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            P_OUT[] array = generator.apply((int) size);
            new SizedCollectorTask.OfRef<>(spliterator, helper, array).invoke();
            return node(array);
        } else {
            Node<P_OUT> node = new CollectorTask.OfRef<>(helper, generator, spliterator).invoke();
            return flattenTree ? flatten(node, generator) : node;
        }
    }

    /**
     * 并行收集整数值管道输出的元素，并用一个 {@link Node.OfInt} 描述这些元素。
     *
     * @implSpec
     * 如果管道输出的确切大小已知，并且源 {@link Spliterator} 具有 {@link Spliterator#SUBSIZED} 特性，
     * 则将返回一个内容为数组的扁平 {@link Node}，因为大小已知，可以在提前构造数组，并由叶任务在正确的偏移量处并发地将输出元素放入数组中。
     * 如果确切大小未知，则将输出元素收集到一个形状与计算相匹配的 conc-node 中。如果需要，可以并行地将此 conc-node 展平以生成一个扁平的 {@code Node.OfInt}。
     *
     * @param <P_IN> 源 Spliterator 的元素类型
     * @param helper 描述管道的管道助手
     * @param flattenTree 是否应在返回前将 conc-node 展平为描述数组的节点
     * @return 描述输出元素的 {@link Node.OfInt}
     */
    public static <P_IN> Node.OfInt collectInt(PipelineHelper<Integer> helper,
                                               Spliterator<P_IN> spliterator,
                                               boolean flattenTree) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size >= 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            int[] array = new int[(int) size];
            new SizedCollectorTask.OfInt<>(spliterator, helper, array).invoke();
            return node(array);
        }
        else {
            Node.OfInt node = new CollectorTask.OfInt<>(helper, spliterator).invoke();
            return flattenTree ? flattenInt(node) : node;
        }
    }


                /**
     * 并行收集从长整型管道输出的元素，并使用 {@link Node.OfLong} 描述这些元素。
     *
     * @implSpec
     * 如果管道输出的确切大小已知，并且源 {@link Spliterator} 具有 {@link Spliterator#SUBSIZED} 特性，
     * 则将返回一个内容为数组的扁平 {@link Node}，因为大小已知，所以可以在事先构建数组，
     * 并且叶任务可以并发地将输出元素放置在数组的正确偏移处。如果确切大小未知，输出元素将被收集到一个
     * 形状与计算相匹配的 conc-node 中。如果需要，可以并行地将此 conc-node 展平以生成一个扁平的
     * {@code Node.OfLong}。
     *
     * @param <P_IN> 源 Spliterator 中元素的类型
     * @param helper 描述管道的管道辅助器
     * @param flattenTree 是否应在返回前将 conc-node 展平为描述数组的节点
     * @return 描述输出元素的 {@link Node.OfLong}
     */
    public static <P_IN> Node.OfLong collectLong(PipelineHelper<Long> helper,
                                                 Spliterator<P_IN> spliterator,
                                                 boolean flattenTree) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size >= 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            long[] array = new long[(int) size];
            new SizedCollectorTask.OfLong<>(spliterator, helper, array).invoke();
            return node(array);
        }
        else {
            Node.OfLong node = new CollectorTask.OfLong<>(helper, spliterator).invoke();
            return flattenTree ? flattenLong(node) : node;
        }
    }

    /**
     * 并行收集从双精度浮点型管道输出的元素，并使用 {@link Node.OfDouble} 描述这些元素。
     *
     * @implSpec
     * 如果管道输出的确切大小已知，并且源 {@link Spliterator} 具有 {@link Spliterator#SUBSIZED} 特性，
     * 则将返回一个内容为数组的扁平 {@link Node}，因为大小已知，所以可以在事先构建数组，
     * 并且叶任务可以并发地将输出元素放置在数组的正确偏移处。如果确切大小未知，输出元素将被收集到一个
     * 形状与计算相匹配的 conc-node 中。如果需要，可以并行地将此 conc-node 展平以生成一个扁平的
     * {@code Node.OfDouble}。
     *
     * @param <P_IN> 源 Spliterator 中元素的类型
     * @param helper 描述管道的管道辅助器
     * @param flattenTree 是否应在返回前将 conc-node 展平为描述数组的节点
     * @return 描述输出元素的 {@link Node.OfDouble}
     */
    public static <P_IN> Node.OfDouble collectDouble(PipelineHelper<Double> helper,
                                                     Spliterator<P_IN> spliterator,
                                                     boolean flattenTree) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size >= 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            double[] array = new double[(int) size];
            new SizedCollectorTask.OfDouble<>(spliterator, helper, array).invoke();
            return node(array);
        }
        else {
            Node.OfDouble node = new CollectorTask.OfDouble<>(helper, spliterator).invoke();
            return flattenTree ? flattenDouble(node) : node;
        }
    }

    // 并行展平节点

    /**
     * 并行展平一个 {@link Node}。展平的节点是没有子节点的节点。如果节点已经是扁平的，则直接返回。
     *
     * @implSpec
     * 如果需要创建新节点，生成器将用于创建长度为 {@link Node#count()} 的数组。然后遍历节点树，
     * 并且叶任务将在正确偏移处并发地将叶节点元素放置在数组中。
     *
     * @param <T> 节点包含的元素类型
     * @param node 要展平的节点
     * @param generator 用于创建数组实例的数组工厂
     * @return 一个扁平的 {@code Node}
     */
    public static <T> Node<T> flatten(Node<T> node, IntFunction<T[]> generator) {
        if (node.getChildCount() > 0) {
            long size = node.count();
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            T[] array = generator.apply((int) size);
            new ToArrayTask.OfRef<>(node, array, 0).invoke();
            return node(array);
        } else {
            return node;
        }
    }

    /**
     * 并行展平一个 {@link Node.OfInt}。展平的节点是没有子节点的节点。如果节点已经是扁平的，则直接返回。
     *
     * @implSpec
     * 如果需要创建新节点，将创建一个长度为 {@link Node#count()} 的新 int[] 数组。然后遍历节点树，
     * 并且叶任务将在正确偏移处并发地将叶节点元素放置在数组中。
     *
     * @param node 要展平的节点
     * @return 一个扁平的 {@code Node.OfInt}
     */
    public static Node.OfInt flattenInt(Node.OfInt node) {
        if (node.getChildCount() > 0) {
            long size = node.count();
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            int[] array = new int[(int) size];
            new ToArrayTask.OfInt(node, array, 0).invoke();
            return node(array);
        } else {
            return node;
        }
    }


                /**
     * 并行展平一个 {@link Node.OfLong}。展平的节点是没有子节点的节点。如果节点已经是展平的，则直接返回。
     *
     * @implSpec
     * 如果需要创建新节点，则创建一个长度为 {@link Node#count()} 的新 long[] 数组。然后并行地遍历节点树，并将叶节点
     * 元素放置在数组中，由叶任务在正确的偏移量处放置。
     *
     * @param node 要展平的节点
     * @return 展平的 {@code Node.OfLong}
     */
    public static Node.OfLong flattenLong(Node.OfLong node) {
        if (node.getChildCount() > 0) {
            long size = node.count();
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            long[] array = new long[(int) size];
            new ToArrayTask.OfLong(node, array, 0).invoke();
            return node(array);
        } else {
            return node;
        }
    }

    /**
     * 并行展平一个 {@link Node.OfDouble}。展平的节点是没有子节点的节点。如果节点已经是展平的，则直接返回。
     *
     * @implSpec
     * 如果需要创建新节点，则创建一个长度为 {@link Node#count()} 的新 double[] 数组。然后并行地遍历节点树，并将叶节点
     * 元素放置在数组中，由叶任务在正确的偏移量处放置。
     *
     * @param node 要展平的节点
     * @return 展平的 {@code Node.OfDouble}
     */
    public static Node.OfDouble flattenDouble(Node.OfDouble node) {
        if (node.getChildCount() > 0) {
            long size = node.count();
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            double[] array = new double[(int) size];
            new ToArrayTask.OfDouble(node, array, 0).invoke();
            return node(array);
        } else {
            return node;
        }
    }

    // 实现

    private static abstract class EmptyNode<T, T_ARR, T_CONS> implements Node<T> {
        EmptyNode() { }

        @Override
        public T[] asArray(IntFunction<T[]> generator) {
            return generator.apply(0);
        }

        public void copyInto(T_ARR array, int offset) { }

        @Override
        public long count() {
            return 0;
        }

        public void forEach(T_CONS consumer) { }

        private static class OfRef<T> extends EmptyNode<T, T[], Consumer<? super T>> {
            private OfRef() {
                super();
            }

            @Override
            public Spliterator<T> spliterator() {
                return Spliterators.emptySpliterator();
            }
        }

        private static final class OfInt
                extends EmptyNode<Integer, int[], IntConsumer>
                implements Node.OfInt {

            OfInt() { } // 避免创建特殊访问器

            @Override
            public Spliterator.OfInt spliterator() {
                return Spliterators.emptyIntSpliterator();
            }

            @Override
            public int[] asPrimitiveArray() {
                return EMPTY_INT_ARRAY;
            }
        }

        private static final class OfLong
                extends EmptyNode<Long, long[], LongConsumer>
                implements Node.OfLong {

            OfLong() { } // 避免创建特殊访问器

            @Override
            public Spliterator.OfLong spliterator() {
                return Spliterators.emptyLongSpliterator();
            }

            @Override
            public long[] asPrimitiveArray() {
                return EMPTY_LONG_ARRAY;
            }
        }

        private static final class OfDouble
                extends EmptyNode<Double, double[], DoubleConsumer>
                implements Node.OfDouble {

            OfDouble() { } // 避免创建特殊访问器

            @Override
            public Spliterator.OfDouble spliterator() {
                return Spliterators.emptyDoubleSpliterator();
            }

            @Override
            public double[] asPrimitiveArray() {
                return EMPTY_DOUBLE_ARRAY;
            }
        }
    }

    /** 用于引用数组的节点类 */
    private static class ArrayNode<T> implements Node<T> {
        final T[] array;
        int curSize;

        @SuppressWarnings("unchecked")
        ArrayNode(long size, IntFunction<T[]> generator) {
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            this.array = generator.apply((int) size);
            this.curSize = 0;
        }

        ArrayNode(T[] array) {
            this.array = array;
            this.curSize = array.length;
        }

        // Node

        @Override
        public Spliterator<T> spliterator() {
            return Arrays.spliterator(array, 0, curSize);
        }

        @Override
        public void copyInto(T[] dest, int destOffset) {
            System.arraycopy(array, 0, dest, destOffset, curSize);
        }

        @Override
        public T[] asArray(IntFunction<T[]> generator) {
            if (array.length == curSize) {
                return array;
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public long count() {
            return curSize;
        }

        @Override
        public void forEach(Consumer<? super T> consumer) {
            for (int i = 0; i < curSize; i++) {
                consumer.accept(array[i]);
            }
        }

        //

        @Override
        public String toString() {
            return String.format("ArrayNode[%d][%s]",
                                 array.length - curSize, Arrays.toString(array));
        }
    }

    /** 用于集合的节点类 */
    private static final class CollectionNode<T> implements Node<T> {
        private final Collection<T> c;

        CollectionNode(Collection<T> c) {
            this.c = c;
        }

        // Node

        @Override
        public Spliterator<T> spliterator() {
            return c.stream().spliterator();
        }


                    @Override
        public void copyInto(T[] array, int offset) {
            for (T t : c)
                array[offset++] = t;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T[] asArray(IntFunction<T[]> generator) {
            return c.toArray(generator.apply(c.size()));
        }

        @Override
        public long count() {
            return c.size();
        }

        @Override
        public void forEach(Consumer<? super T> consumer) {
            c.forEach(consumer);
        }

        //

        @Override
        public String toString() {
            return String.format("CollectionNode[%d][%s]", c.size(), c);
        }
    }

    /**
     * Node 类用于具有两个或更多子节点的内部节点
     */
    private static abstract class AbstractConcNode<T, T_NODE extends Node<T>> implements Node<T> {
        protected final T_NODE left;
        protected final T_NODE right;
        private final long size;

        AbstractConcNode(T_NODE left, T_NODE right) {
            this.left = left;
            this.right = right;
            // 在构建树时自底向上积极计算节点数，而不是稍后从顶向下遍历树，这样更便宜
            this.size = left.count() + right.count();
        }

        @Override
        public int getChildCount() {
            return 2;
        }

        @Override
        public T_NODE getChild(int i) {
            if (i == 0) return left;
            if (i == 1) return right;
            throw new IndexOutOfBoundsException();
        }

        @Override
        public long count() {
            return size;
        }
    }

    static final class ConcNode<T>
            extends AbstractConcNode<T, Node<T>>
            implements Node<T> {

        ConcNode(Node<T> left, Node<T> right) {
            super(left, right);
        }

        @Override
        public Spliterator<T> spliterator() {
            return new Nodes.InternalNodeSpliterator.OfRef<>(this);
        }

        @Override
        public void copyInto(T[] array, int offset) {
            Objects.requireNonNull(array);
            left.copyInto(array, offset);
            // 将 long 转换为 int 是安全的，因为调用者有责任确保数组中有足够的空间
            right.copyInto(array, offset + (int) left.count());
        }

        @Override
        public T[] asArray(IntFunction<T[]> generator) {
            long size = count();
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            T[] array = generator.apply((int) size);
            copyInto(array, 0);
            return array;
        }

        @Override
        public void forEach(Consumer<? super T> consumer) {
            left.forEach(consumer);
            right.forEach(consumer);
        }

        @Override
        public Node<T> truncate(long from, long to, IntFunction<T[]> generator) {
            if (from == 0 && to == count())
                return this;
            long leftCount = left.count();
            if (from >= leftCount)
                return right.truncate(from - leftCount, to - leftCount, generator);
            else if (to <= leftCount)
                return left.truncate(from, to, generator);
            else {
                return Nodes.conc(getShape(), left.truncate(from, leftCount, generator),
                                  right.truncate(0, to - leftCount, generator));
            }
        }

        @Override
        public String toString() {
            if (count() < 32) {
                return String.format("ConcNode[%s.%s]", left, right);
            } else {
                return String.format("ConcNode[size=%d]", count());
            }
        }

        private abstract static class OfPrimitive<E, T_CONS, T_ARR,
                                                  T_SPLITR extends Spliterator.OfPrimitive<E, T_CONS, T_SPLITR>,
                                                  T_NODE extends Node.OfPrimitive<E, T_CONS, T_ARR, T_SPLITR, T_NODE>>
                extends AbstractConcNode<E, T_NODE>
                implements Node.OfPrimitive<E, T_CONS, T_ARR, T_SPLITR, T_NODE> {

            OfPrimitive(T_NODE left, T_NODE right) {
                super(left, right);
            }

            @Override
            public void forEach(T_CONS consumer) {
                left.forEach(consumer);
                right.forEach(consumer);
            }

            @Override
            public void copyInto(T_ARR array, int offset) {
                left.copyInto(array, offset);
                // 将 long 转换为 int 是安全的，因为调用者有责任确保数组中有足够的空间
                right.copyInto(array, offset + (int) left.count());
            }

            @Override
            public T_ARR asPrimitiveArray() {
                long size = count();
                if (size >= MAX_ARRAY_SIZE)
                    throw new IllegalArgumentException(BAD_SIZE);
                T_ARR array = newArray((int) size);
                copyInto(array, 0);
                return array;
            }

            @Override
            public String toString() {
                if (count() < 32)
                    return String.format("%s[%s.%s]", this.getClass().getName(), left, right);
                else
                    return String.format("%s[size=%d]", this.getClass().getName(), count());
            }
        }

        static final class OfInt
                extends ConcNode.OfPrimitive<Integer, IntConsumer, int[], Spliterator.OfInt, Node.OfInt>
                implements Node.OfInt {

            OfInt(Node.OfInt left, Node.OfInt right) {
                super(left, right);
            }

            @Override
            public Spliterator.OfInt spliterator() {
                return new InternalNodeSpliterator.OfInt(this);
            }
        }

        static final class OfLong
                extends ConcNode.OfPrimitive<Long, LongConsumer, long[], Spliterator.OfLong, Node.OfLong>
                implements Node.OfLong {


                        OfLong(Node.OfLong left, Node.OfLong right) {
                super(left, right);
            }

            @Override
            public Spliterator.OfLong spliterator() {
                return new InternalNodeSpliterator.OfLong(this);
            }
        }

        static final class OfDouble
                extends ConcNode.OfPrimitive<Double, DoubleConsumer, double[], Spliterator.OfDouble, Node.OfDouble>
                implements Node.OfDouble {

            OfDouble(Node.OfDouble left, Node.OfDouble right) {
                super(left, right);
            }

            @Override
            public Spliterator.OfDouble spliterator() {
                return new InternalNodeSpliterator.OfDouble(this);
            }
        }
    }

    /** 抽象类，用于所有内部节点类的 spliterator */
    private static abstract class InternalNodeSpliterator<T,
                                                          S extends Spliterator<T>,
                                                          N extends Node<T>>
            implements Spliterator<T> {
        // 当前指向的节点
        // 如果完整遍历已经发生，则为 null
        N curNode;

        // 当前节点要消费的下一个子节点
        int curChildIndex;

        // 如果当前节点是最后一个且没有子节点，则为该节点的 spliterator。
        // 这个 spliterator 将用于拆分和遍历。
        // 如果当前节点有子节点，则为 null
        S lastNodeSpliterator;

        // 在使用 tryAdvance 遍历时使用的 spliterator
        // 如果没有部分遍历发生，则为 null
        S tryAdvanceSpliterator;

        // 在遍历时用于搜索和查找叶节点的节点栈
        // 如果没有部分遍历发生，则为 null
        Deque<N> tryAdvanceStack;

        InternalNodeSpliterator(N curNode) {
            this.curNode = curNode;
        }

        /**
         * 初始化一个栈，按从左到右的顺序包含此 spliterator 覆盖的子节点
         */
        @SuppressWarnings("unchecked")
        protected final Deque<N> initStack() {
            // 偏向于叶节点接近此节点的情况
            // 8 是 ArrayDeque 实现的最小初始容量
            Deque<N> stack = new ArrayDeque<>(8);
            for (int i = curNode.getChildCount() - 1; i >= curChildIndex; i--)
                stack.addFirst((N) curNode.getChild(i));
            return stack;
        }

        /**
         * 使用显式栈按从左到右的顺序深度优先搜索节点树，以找到下一个非空叶节点。
         */
        @SuppressWarnings("unchecked")
        protected final N findNextLeafNode(Deque<N> stack) {
            N n = null;
            while ((n = stack.pollFirst()) != null) {
                if (n.getChildCount() == 0) {
                    if (n.count() > 0)
                        return n;
                } else {
                    for (int i = n.getChildCount() - 1; i >= 0; i--)
                        stack.addFirst((N) n.getChild(i));
                }
            }

            return null;
        }

        @SuppressWarnings("unchecked")
        protected final boolean initTryAdvance() {
            if (curNode == null)
                return false;

            if (tryAdvanceSpliterator == null) {
                if (lastNodeSpliterator == null) {
                    // 初始化节点栈
                    tryAdvanceStack = initStack();
                    N leaf = findNextLeafNode(tryAdvanceStack);
                    if (leaf != null)
                        tryAdvanceSpliterator = (S) leaf.spliterator();
                    else {
                        // 没有找到非空叶节点
                        // 没有元素可以遍历
                        curNode = null;
                        return false;
                    }
                }
                else
                    tryAdvanceSpliterator = lastNodeSpliterator;
            }
            return true;
        }

        @Override
        @SuppressWarnings("unchecked")
        public final S trySplit() {
            if (curNode == null || tryAdvanceSpliterator != null)
                return null; // 如果已经完全或部分遍历，则不能拆分
            else if (lastNodeSpliterator != null)
                return (S) lastNodeSpliterator.trySplit();
            else if (curChildIndex < curNode.getChildCount() - 1)
                return (S) curNode.getChild(curChildIndex++).spliterator();
            else {
                curNode = (N) curNode.getChild(curChildIndex);
                if (curNode.getChildCount() == 0) {
                    lastNodeSpliterator = (S) curNode.spliterator();
                    return (S) lastNodeSpliterator.trySplit();
                }
                else {
                    curChildIndex = 0;
                    return (S) curNode.getChild(curChildIndex++).spliterator();
                }
            }
        }

        @Override
        public final long estimateSize() {
            if (curNode == null)
                return 0;

            // 不会反映部分遍历的效果。
            // 这符合规范
            if (lastNodeSpliterator != null)
                return lastNodeSpliterator.estimateSize();
            else {
                long size = 0;
                for (int i = curChildIndex; i < curNode.getChildCount(); i++)
                    size += curNode.getChild(i).count();
                return size;
            }
        }

        @Override
        public final int characteristics() {
            return Spliterator.SIZED;
        }

        private static final class OfRef<T>
                extends InternalNodeSpliterator<T, Spliterator<T>, Node<T>> {

            OfRef(Node<T> curNode) {
                super(curNode);
            }

            @Override
            public boolean tryAdvance(Consumer<? super T> consumer) {
                if (!initTryAdvance())
                    return false;


                            boolean hasNext = tryAdvanceSpliterator.tryAdvance(consumer);
                if (!hasNext) {
                    if (lastNodeSpliterator == null) {
                        // 转到下一个非空叶节点的Spliterator
                        Node<T> leaf = findNextLeafNode(tryAdvanceStack);
                        if (leaf != null) {
                            tryAdvanceSpliterator = leaf.spliterator();
                            // 由于节点非空，Spliterator可以前进
                            return tryAdvanceSpliterator.tryAdvance(consumer);
                        }
                    }
                    // 没有更多元素可以遍历
                    curNode = null;
                }
                return hasNext;
            }

            @Override
            public void forEachRemaining(Consumer<? super T> consumer) {
                if (curNode == null)
                    return;

                if (tryAdvanceSpliterator == null) {
                    if (lastNodeSpliterator == null) {
                        Deque<Node<T>> stack = initStack();
                        Node<T> leaf;
                        while ((leaf = findNextLeafNode(stack)) != null) {
                            leaf.forEach(consumer);
                        }
                        curNode = null;
                    }
                    else
                        lastNodeSpliterator.forEachRemaining(consumer);
                }
                else
                    while(tryAdvance(consumer)) { }
            }
        }

        private static abstract class OfPrimitive<T, T_CONS, T_ARR,
                                                  T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>,
                                                  N extends Node.OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, N>>
                extends InternalNodeSpliterator<T, T_SPLITR, N>
                implements Spliterator.OfPrimitive<T, T_CONS, T_SPLITR> {

            OfPrimitive(N cur) {
                super(cur);
            }

            @Override
            public boolean tryAdvance(T_CONS consumer) {
                if (!initTryAdvance())
                    return false;

                boolean hasNext = tryAdvanceSpliterator.tryAdvance(consumer);
                if (!hasNext) {
                    if (lastNodeSpliterator == null) {
                        // 转到下一个非空叶节点的Spliterator
                        N leaf = findNextLeafNode(tryAdvanceStack);
                        if (leaf != null) {
                            tryAdvanceSpliterator = leaf.spliterator();
                            // 由于节点非空，Spliterator可以前进
                            return tryAdvanceSpliterator.tryAdvance(consumer);
                        }
                    }
                    // 没有更多元素可以遍历
                    curNode = null;
                }
                return hasNext;
            }

            @Override
            public void forEachRemaining(T_CONS consumer) {
                if (curNode == null)
                    return;

                if (tryAdvanceSpliterator == null) {
                    if (lastNodeSpliterator == null) {
                        Deque<N> stack = initStack();
                        N leaf;
                        while ((leaf = findNextLeafNode(stack)) != null) {
                            leaf.forEach(consumer);
                        }
                        curNode = null;
                    }
                    else
                        lastNodeSpliterator.forEachRemaining(consumer);
                }
                else
                    while(tryAdvance(consumer)) { }
            }
        }

        private static final class OfInt
                extends OfPrimitive<Integer, IntConsumer, int[], Spliterator.OfInt, Node.OfInt>
                implements Spliterator.OfInt {

            OfInt(Node.OfInt cur) {
                super(cur);
            }
        }

        private static final class OfLong
                extends OfPrimitive<Long, LongConsumer, long[], Spliterator.OfLong, Node.OfLong>
                implements Spliterator.OfLong {

            OfLong(Node.OfLong cur) {
                super(cur);
            }
        }

        private static final class OfDouble
                extends OfPrimitive<Double, DoubleConsumer, double[], Spliterator.OfDouble, Node.OfDouble>
                implements Spliterator.OfDouble {

            OfDouble(Node.OfDouble cur) {
                super(cur);
            }
        }
    }

    /**
     * 用于引用节点的固定大小构建器类
     */
    private static final class FixedNodeBuilder<T>
            extends ArrayNode<T>
            implements Node.Builder<T> {

        FixedNodeBuilder(long size, IntFunction<T[]> generator) {
            super(size, generator);
            assert size < MAX_ARRAY_SIZE;
        }

        @Override
        public Node<T> build() {
            if (curSize < array.length)
                throw new IllegalStateException(String.format("当前大小 %d 小于固定大小 %d",
                                                              curSize, array.length));
            return this;
        }

        @Override
        public void begin(long size) {
            if (size != array.length)
                throw new IllegalStateException(String.format("开始大小 %d 不等于固定大小 %d",
                                                              size, array.length));
            curSize = 0;
        }

        @Override
        public void accept(T t) {
            if (curSize < array.length) {
                array[curSize++] = t;
            } else {
                throw new IllegalStateException(String.format("接受的元素超过了固定大小 %d",
                                                              array.length));
            }
        }

        @Override
        public void end() {
            if (curSize < array.length)
                throw new IllegalStateException(String.format("结束大小 %d 小于固定大小 %d",
                                                              curSize, array.length));
        }


                    @Override
        public String toString() {
            return String.format("FixedNodeBuilder[%d][%s]",
                                 array.length - curSize, Arrays.toString(array));
        }
    }

    /**
     * 可变大小的引用节点构建器类
     */
    private static final class SpinedNodeBuilder<T>
            extends SpinedBuffer<T>
            implements Node<T>, Node.Builder<T> {
        private boolean building = false;

        SpinedNodeBuilder() {} // 避免创建特殊访问器

        @Override
        public Spliterator<T> spliterator() {
            assert !building : "在构建期间";
            return super.spliterator();
        }

        @Override
        public void forEach(Consumer<? super T> consumer) {
            assert !building : "在构建期间";
            super.forEach(consumer);
        }

        //
        @Override
        public void begin(long size) {
            assert !building : "已经在构建中";
            building = true;
            clear();
            ensureCapacity(size);
        }

        @Override
        public void accept(T t) {
            assert building : "未在构建中";
            super.accept(t);
        }

        @Override
        public void end() {
            assert building : "未在构建中";
            building = false;
            // @@@ 检查 begin(size) 和 size
        }

        @Override
        public void copyInto(T[] array, int offset) {
            assert !building : "在构建期间";
            super.copyInto(array, offset);
        }

        @Override
        public T[] asArray(IntFunction<T[]> arrayFactory) {
            assert !building : "在构建期间";
            return super.asArray(arrayFactory);
        }

        @Override
        public Node<T> build() {
            assert !building : "在构建期间";
            return this;
        }
    }

    //

    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final long[] EMPTY_LONG_ARRAY = new long[0];
    private static final double[] EMPTY_DOUBLE_ARRAY = new double[0];

    private static class IntArrayNode implements Node.OfInt {
        final int[] array;
        int curSize;

        IntArrayNode(long size) {
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            this.array = new int[(int) size];
            this.curSize = 0;
        }

        IntArrayNode(int[] array) {
            this.array = array;
            this.curSize = array.length;
        }

        // Node

        @Override
        public Spliterator.OfInt spliterator() {
            return Arrays.spliterator(array, 0, curSize);
        }

        @Override
        public int[] asPrimitiveArray() {
            if (array.length == curSize) {
                return array;
            } else {
                return Arrays.copyOf(array, curSize);
            }
        }

        @Override
        public void copyInto(int[] dest, int destOffset) {
            System.arraycopy(array, 0, dest, destOffset, curSize);
        }

        @Override
        public long count() {
            return curSize;
        }

        @Override
        public void forEach(IntConsumer consumer) {
            for (int i = 0; i < curSize; i++) {
                consumer.accept(array[i]);
            }
        }

        @Override
        public String toString() {
            return String.format("IntArrayNode[%d][%s]",
                                 array.length - curSize, Arrays.toString(array));
        }
    }

    private static class LongArrayNode implements Node.OfLong {
        final long[] array;
        int curSize;

        LongArrayNode(long size) {
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            this.array = new long[(int) size];
            this.curSize = 0;
        }

        LongArrayNode(long[] array) {
            this.array = array;
            this.curSize = array.length;
        }

        @Override
        public Spliterator.OfLong spliterator() {
            return Arrays.spliterator(array, 0, curSize);
        }

        @Override
        public long[] asPrimitiveArray() {
            if (array.length == curSize) {
                return array;
            } else {
                return Arrays.copyOf(array, curSize);
            }
        }

        @Override
        public void copyInto(long[] dest, int destOffset) {
            System.arraycopy(array, 0, dest, destOffset, curSize);
        }

        @Override
        public long count() {
            return curSize;
        }

        @Override
        public void forEach(LongConsumer consumer) {
            for (int i = 0; i < curSize; i++) {
                consumer.accept(array[i]);
            }
        }

        @Override
        public String toString() {
            return String.format("LongArrayNode[%d][%s]",
                                 array.length - curSize, Arrays.toString(array));
        }
    }

    private static class DoubleArrayNode implements Node.OfDouble {
        final double[] array;
        int curSize;

        DoubleArrayNode(long size) {
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            this.array = new double[(int) size];
            this.curSize = 0;
        }

        DoubleArrayNode(double[] array) {
            this.array = array;
            this.curSize = array.length;
        }

        @Override
        public Spliterator.OfDouble spliterator() {
            return Arrays.spliterator(array, 0, curSize);
        }

        @Override
        public double[] asPrimitiveArray() {
            if (array.length == curSize) {
                return array;
            } else {
                return Arrays.copyOf(array, curSize);
            }
        }

        @Override
        public void copyInto(double[] dest, int destOffset) {
            System.arraycopy(array, 0, dest, destOffset, curSize);
        }

        @Override
        public long count() {
            return curSize;
        }


                    @Override
        public void forEach(DoubleConsumer consumer) {
            for (int i = 0; i < curSize; i++) {
                consumer.accept(array[i]);
            }
        }

        @Override
        public String toString() {
            return String.format("DoubleArrayNode[%d][%s]",
                                 array.length - curSize, Arrays.toString(array));
        }
    }

    private static final class IntFixedNodeBuilder
            extends IntArrayNode
            implements Node.Builder.OfInt {

        IntFixedNodeBuilder(long size) {
            super(size);
            assert size < MAX_ARRAY_SIZE;
        }

        @Override
        public Node.OfInt build() {
            if (curSize < array.length) {
                throw new IllegalStateException(String.format("当前大小 %d 小于固定大小 %d",
                                                              curSize, array.length));
            }

            return this;
        }

        @Override
        public void begin(long size) {
            if (size != array.length) {
                throw new IllegalStateException(String.format("开始大小 %d 不等于固定大小 %d",
                                                              size, array.length));
            }

            curSize = 0;
        }

        @Override
        public void accept(int i) {
            if (curSize < array.length) {
                array[curSize++] = i;
            } else {
                throw new IllegalStateException(String.format("接受超出固定大小 %d",
                                                              array.length));
            }
        }

        @Override
        public void end() {
            if (curSize < array.length) {
                throw new IllegalStateException(String.format("结束大小 %d 小于固定大小 %d",
                                                              curSize, array.length));
            }
        }

        @Override
        public String toString() {
            return String.format("IntFixedNodeBuilder[%d][%s]",
                                 array.length - curSize, Arrays.toString(array));
        }
    }

    private static final class LongFixedNodeBuilder
            extends LongArrayNode
            implements Node.Builder.OfLong {

        LongFixedNodeBuilder(long size) {
            super(size);
            assert size < MAX_ARRAY_SIZE;
        }

        @Override
        public Node.OfLong build() {
            if (curSize < array.length) {
                throw new IllegalStateException(String.format("当前大小 %d 小于固定大小 %d",
                                                              curSize, array.length));
            }

            return this;
        }

        @Override
        public void begin(long size) {
            if (size != array.length) {
                throw new IllegalStateException(String.format("开始大小 %d 不等于固定大小 %d",
                                                              size, array.length));
            }

            curSize = 0;
        }

        @Override
        public void accept(long i) {
            if (curSize < array.length) {
                array[curSize++] = i;
            } else {
                throw new IllegalStateException(String.format("接受超出固定大小 %d",
                                                              array.length));
            }
        }

        @Override
        public void end() {
            if (curSize < array.length) {
                throw new IllegalStateException(String.format("结束大小 %d 小于固定大小 %d",
                                                              curSize, array.length));
            }
        }

        @Override
        public String toString() {
            return String.format("LongFixedNodeBuilder[%d][%s]",
                                 array.length - curSize, Arrays.toString(array));
        }
    }

    private static final class DoubleFixedNodeBuilder
            extends DoubleArrayNode
            implements Node.Builder.OfDouble {

        DoubleFixedNodeBuilder(long size) {
            super(size);
            assert size < MAX_ARRAY_SIZE;
        }

        @Override
        public Node.OfDouble build() {
            if (curSize < array.length) {
                throw new IllegalStateException(String.format("当前大小 %d 小于固定大小 %d",
                                                              curSize, array.length));
            }

            return this;
        }

        @Override
        public void begin(long size) {
            if (size != array.length) {
                throw new IllegalStateException(String.format("开始大小 %d 不等于固定大小 %d",
                                                              size, array.length));
            }

            curSize = 0;
        }

        @Override
        public void accept(double i) {
            if (curSize < array.length) {
                array[curSize++] = i;
            } else {
                throw new IllegalStateException(String.format("接受超出固定大小 %d",
                                                              array.length));
            }
        }

        @Override
        public void end() {
            if (curSize < array.length) {
                throw new IllegalStateException(String.format("结束大小 %d 小于固定大小 %d",
                                                              curSize, array.length));
            }
        }

        @Override
        public String toString() {
            return String.format("DoubleFixedNodeBuilder[%d][%s]",
                                 array.length - curSize, Arrays.toString(array));
        }
    }

    private static final class IntSpinedNodeBuilder
            extends SpinedBuffer.OfInt
            implements Node.OfInt, Node.Builder.OfInt {
        private boolean building = false;

        IntSpinedNodeBuilder() {} // 避免创建特殊访问器

        @Override
        public Spliterator.OfInt spliterator() {
            assert !building : "在构建期间";
            return super.spliterator();
        }


                    @Override
        public void forEach(IntConsumer consumer) {
            assert !building : "during building";
            super.forEach(consumer);
        }

        //
        @Override
        public void begin(long size) {
            assert !building : "was already building";
            building = true;
            clear();
            ensureCapacity(size);
        }

        @Override
        public void accept(int i) {
            assert building : "not building";
            super.accept(i);
        }

        @Override
        public void end() {
            assert building : "was not building";
            building = false;
            // @@@ check begin(size) and size
        }

        @Override
        public void copyInto(int[] array, int offset) throws IndexOutOfBoundsException {
            assert !building : "during building";
            super.copyInto(array, offset);
        }

        @Override
        public int[] asPrimitiveArray() {
            assert !building : "during building";
            return super.asPrimitiveArray();
        }

        @Override
        public Node.OfInt build() {
            assert !building : "during building";
            return this;
        }
    }

    private static final class LongSpinedNodeBuilder
            extends SpinedBuffer.OfLong
            implements Node.OfLong, Node.Builder.OfLong {
        private boolean building = false;

        LongSpinedNodeBuilder() {} // Avoid creation of special accessor

        @Override
        public Spliterator.OfLong spliterator() {
            assert !building : "during building";
            return super.spliterator();
        }

        @Override
        public void forEach(LongConsumer consumer) {
            assert !building : "during building";
            super.forEach(consumer);
        }

        //
        @Override
        public void begin(long size) {
            assert !building : "was already building";
            building = true;
            clear();
            ensureCapacity(size);
        }

        @Override
        public void accept(long i) {
            assert building : "not building";
            super.accept(i);
        }

        @Override
        public void end() {
            assert building : "was not building";
            building = false;
            // @@@ check begin(size) and size
        }

        @Override
        public void copyInto(long[] array, int offset) {
            assert !building : "during building";
            super.copyInto(array, offset);
        }

        @Override
        public long[] asPrimitiveArray() {
            assert !building : "during building";
            return super.asPrimitiveArray();
        }

        @Override
        public Node.OfLong build() {
            assert !building : "during building";
            return this;
        }
    }

    private static final class DoubleSpinedNodeBuilder
            extends SpinedBuffer.OfDouble
            implements Node.OfDouble, Node.Builder.OfDouble {
        private boolean building = false;

        DoubleSpinedNodeBuilder() {} // Avoid creation of special accessor

        @Override
        public Spliterator.OfDouble spliterator() {
            assert !building : "during building";
            return super.spliterator();
        }

        @Override
        public void forEach(DoubleConsumer consumer) {
            assert !building : "during building";
            super.forEach(consumer);
        }

        //
        @Override
        public void begin(long size) {
            assert !building : "was already building";
            building = true;
            clear();
            ensureCapacity(size);
        }

        @Override
        public void accept(double i) {
            assert building : "not building";
            super.accept(i);
        }

        @Override
        public void end() {
            assert building : "was not building";
            building = false;
            // @@@ check begin(size) and size
        }

        @Override
        public void copyInto(double[] array, int offset) {
            assert !building : "during building";
            super.copyInto(array, offset);
        }

        @Override
        public double[] asPrimitiveArray() {
            assert !building : "during building";
            return super.asPrimitiveArray();
        }

        @Override
        public Node.OfDouble build() {
            assert !building : "during building";
            return this;
        }
    }

    /*
     * This and subclasses are not intended to be serializable
     */
    @SuppressWarnings("serial")
    private static abstract class SizedCollectorTask<P_IN, P_OUT, T_SINK extends Sink<P_OUT>,
                                                     K extends SizedCollectorTask<P_IN, P_OUT, T_SINK, K>>
            extends CountedCompleter<Void>
            implements Sink<P_OUT> {
        protected final Spliterator<P_IN> spliterator;
        protected final PipelineHelper<P_OUT> helper;
        protected final long targetSize;
        protected long offset;
        protected long length;
        // For Sink implementation
        protected int index, fence;

        SizedCollectorTask(Spliterator<P_IN> spliterator,
                           PipelineHelper<P_OUT> helper,
                           int arrayLength) {
            assert spliterator.hasCharacteristics(Spliterator.SUBSIZED);
            this.spliterator = spliterator;
            this.helper = helper;
            this.targetSize = AbstractTask.suggestTargetSize(spliterator.estimateSize());
            this.offset = 0;
            this.length = arrayLength;
        }

        SizedCollectorTask(K parent, Spliterator<P_IN> spliterator,
                           long offset, long length, int arrayLength) {
            super(parent);
            assert spliterator.hasCharacteristics(Spliterator.SUBSIZED);
            this.spliterator = spliterator;
            this.helper = parent.helper;
            this.targetSize = parent.targetSize;
            this.offset = offset;
            this.length = length;

            if (offset < 0 || length < 0 || (offset + length - 1 >= arrayLength)) {
                throw new IllegalArgumentException(
                        String.format("offset and length interval [%d, %d + %d) is not within array size interval [0, %d)",
                                      offset, offset, length, arrayLength));
            }
        }


                    @Override
        public void compute() {
            SizedCollectorTask<P_IN, P_OUT, T_SINK, K> task = this;
            Spliterator<P_IN> rightSplit = spliterator, leftSplit;
            while (rightSplit.estimateSize() > task.targetSize &&
                   (leftSplit = rightSplit.trySplit()) != null) {
                task.setPendingCount(1);
                long leftSplitSize = leftSplit.estimateSize();
                task.makeChild(leftSplit, task.offset, leftSplitSize).fork();
                task = task.makeChild(rightSplit, task.offset + leftSplitSize,
                                      task.length - leftSplitSize);
            }

            assert task.offset + task.length < MAX_ARRAY_SIZE;
            @SuppressWarnings("unchecked")
            T_SINK sink = (T_SINK) task;
            task.helper.wrapAndCopyInto(sink, rightSplit);
            task.propagateCompletion();
        }

        abstract K makeChild(Spliterator<P_IN> spliterator, long offset, long size);

        @Override
        public void begin(long size) {
            if (size > length)
                throw new IllegalStateException("传递给 Sink.begin 的大小超过了数组长度");
            // 转换为 int 是安全的，因为绝对大小在构建根具体的 SizedCollectorTask 时
            // 已经验证在共享数组的范围内
            index = (int) offset;
            fence = index + (int) length;
        }

        @SuppressWarnings("serial")
        static final class OfRef<P_IN, P_OUT>
                extends SizedCollectorTask<P_IN, P_OUT, Sink<P_OUT>, OfRef<P_IN, P_OUT>>
                implements Sink<P_OUT> {
            private final P_OUT[] array;

            OfRef(Spliterator<P_IN> spliterator, PipelineHelper<P_OUT> helper, P_OUT[] array) {
                super(spliterator, helper, array.length);
                this.array = array;
            }

            OfRef(OfRef<P_IN, P_OUT> parent, Spliterator<P_IN> spliterator,
                  long offset, long length) {
                super(parent, spliterator, offset, length, parent.array.length);
                this.array = parent.array;
            }

            @Override
            OfRef<P_IN, P_OUT> makeChild(Spliterator<P_IN> spliterator,
                                         long offset, long size) {
                return new OfRef<>(this, spliterator, offset, size);
            }

            @Override
            public void accept(P_OUT value) {
                if (index >= fence) {
                    throw new IndexOutOfBoundsException(Integer.toString(index));
                }
                array[index++] = value;
            }
        }

        @SuppressWarnings("serial")
        static final class OfInt<P_IN>
                extends SizedCollectorTask<P_IN, Integer, Sink.OfInt, OfInt<P_IN>>
                implements Sink.OfInt {
            private final int[] array;

            OfInt(Spliterator<P_IN> spliterator, PipelineHelper<Integer> helper, int[] array) {
                super(spliterator, helper, array.length);
                this.array = array;
            }

            OfInt(SizedCollectorTask.OfInt<P_IN> parent, Spliterator<P_IN> spliterator,
                  long offset, long length) {
                super(parent, spliterator, offset, length, parent.array.length);
                this.array = parent.array;
            }

            @Override
            SizedCollectorTask.OfInt<P_IN> makeChild(Spliterator<P_IN> spliterator,
                                                     long offset, long size) {
                return new SizedCollectorTask.OfInt<>(this, spliterator, offset, size);
            }

            @Override
            public void accept(int value) {
                if (index >= fence) {
                    throw new IndexOutOfBoundsException(Integer.toString(index));
                }
                array[index++] = value;
            }
        }

        @SuppressWarnings("serial")
        static final class OfLong<P_IN>
                extends SizedCollectorTask<P_IN, Long, Sink.OfLong, OfLong<P_IN>>
                implements Sink.OfLong {
            private final long[] array;

            OfLong(Spliterator<P_IN> spliterator, PipelineHelper<Long> helper, long[] array) {
                super(spliterator, helper, array.length);
                this.array = array;
            }

            OfLong(SizedCollectorTask.OfLong<P_IN> parent, Spliterator<P_IN> spliterator,
                   long offset, long length) {
                super(parent, spliterator, offset, length, parent.array.length);
                this.array = parent.array;
            }

            @Override
            SizedCollectorTask.OfLong<P_IN> makeChild(Spliterator<P_IN> spliterator,
                                                      long offset, long size) {
                return new SizedCollectorTask.OfLong<>(this, spliterator, offset, size);
            }

            @Override
            public void accept(long value) {
                if (index >= fence) {
                    throw new IndexOutOfBoundsException(Integer.toString(index));
                }
                array[index++] = value;
            }
        }

        @SuppressWarnings("serial")
        static final class OfDouble<P_IN>
                extends SizedCollectorTask<P_IN, Double, Sink.OfDouble, OfDouble<P_IN>>
                implements Sink.OfDouble {
            private final double[] array;

            OfDouble(Spliterator<P_IN> spliterator, PipelineHelper<Double> helper, double[] array) {
                super(spliterator, helper, array.length);
                this.array = array;
            }

            OfDouble(SizedCollectorTask.OfDouble<P_IN> parent, Spliterator<P_IN> spliterator,
                     long offset, long length) {
                super(parent, spliterator, offset, length, parent.array.length);
                this.array = parent.array;
            }

            @Override
            SizedCollectorTask.OfDouble<P_IN> makeChild(Spliterator<P_IN> spliterator,
                                                        long offset, long size) {
                return new SizedCollectorTask.OfDouble<>(this, spliterator, offset, size);
            }


                        @Override
            public void accept(double value) {
                if (index >= fence) {
                    throw new IndexOutOfBoundsException(Integer.toString(index));
                }
                array[index++] = value;
            }
        }
    }

    @SuppressWarnings("serial")
    private static abstract class ToArrayTask<T, T_NODE extends Node<T>,
                                              K extends ToArrayTask<T, T_NODE, K>>
            extends CountedCompleter<Void> {
        // 保护成员变量，表示当前处理的节点
        protected final T_NODE node;
        // 保护成员变量，表示当前节点的偏移量
        protected final int offset;

        // 构造函数，初始化节点和偏移量
        ToArrayTask(T_NODE node, int offset) {
            this.node = node;
            this.offset = offset;
        }

        // 构造函数，用于子任务的创建，继承自父任务的节点和偏移量
        ToArrayTask(K parent, T_NODE node, int offset) {
            super(parent);
            this.node = node;
            this.offset = offset;
        }

        // 抽象方法，用于将节点数据复制到数组中
        abstract void copyNodeToArray();

        // 抽象方法，用于创建子任务
        abstract K makeChild(int childIndex, int offset);

        @Override
        public void compute() {
            ToArrayTask<T, T_NODE, K> task = this;
            while (true) {
                if (task.node.getChildCount() == 0) {
                    // 如果当前节点没有子节点，直接复制数据到数组并传播完成信号
                    task.copyNodeToArray();
                    task.propagateCompletion();
                    return;
                }
                else {
                    // 设置待处理的子任务数量
                    task.setPendingCount(task.node.getChildCount() - 1);

                    int size = 0;
                    int i = 0;
                    // 遍历当前节点的所有子节点，创建子任务并启动
                    for (;i < task.node.getChildCount() - 1; i++) {
                        K leftTask = task.makeChild(i, task.offset + size);
                        size += leftTask.node.count();
                        leftTask.fork();
                    }
                    // 创建最后一个子任务
                    task = task.makeChild(i, task.offset + size);
                }
            }
        }

        @SuppressWarnings("serial")
        private static final class OfRef<T>
                extends ToArrayTask<T, Node<T>, OfRef<T>> {
            // 成员变量，表示目标数组
            private final T[] array;

            // 构造函数，初始化节点、数组和偏移量
            private OfRef(Node<T> node, T[] array, int offset) {
                super(node, offset);
                this.array = array;
            }

            // 构造函数，用于子任务的创建，继承自父任务的节点、数组和偏移量
            private OfRef(OfRef<T> parent, Node<T> node, int offset) {
                super(parent, node, offset);
                this.array = parent.array;
            }

            @Override
            OfRef<T> makeChild(int childIndex, int offset) {
                return new OfRef<>(this, node.getChild(childIndex), offset);
            }

            @Override
            void copyNodeToArray() {
                node.copyInto(array, offset);
            }
        }

        @SuppressWarnings("serial")
        private static class OfPrimitive<T, T_CONS, T_ARR,
                                         T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>,
                                         T_NODE extends Node.OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE>>
                extends ToArrayTask<T, T_NODE, OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE>> {
            // 成员变量，表示目标数组
            private final T_ARR array;

            // 构造函数，初始化节点、数组和偏移量
            private OfPrimitive(T_NODE node, T_ARR array, int offset) {
                super(node, offset);
                this.array = array;
            }

            // 构造函数，用于子任务的创建，继承自父任务的节点、数组和偏移量
            private OfPrimitive(OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE> parent, T_NODE node, int offset) {
                super(parent, node, offset);
                this.array = parent.array;
            }

            @Override
            OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE> makeChild(int childIndex, int offset) {
                return new OfPrimitive<>(this, node.getChild(childIndex), offset);
            }

            @Override
            void copyNodeToArray() {
                node.copyInto(array, offset);
            }
        }

        @SuppressWarnings("serial")
        private static final class OfInt
                extends OfPrimitive<Integer, IntConsumer, int[], Spliterator.OfInt, Node.OfInt> {
            // 构造函数，初始化节点、数组和偏移量
            private OfInt(Node.OfInt node, int[] array, int offset) {
                super(node, array, offset);
            }
        }

        @SuppressWarnings("serial")
        private static final class OfLong
                extends OfPrimitive<Long, LongConsumer, long[], Spliterator.OfLong, Node.OfLong> {
            // 构造函数，初始化节点、数组和偏移量
            private OfLong(Node.OfLong node, long[] array, int offset) {
                super(node, array, offset);
            }
        }

        @SuppressWarnings("serial")
        private static final class OfDouble
                extends OfPrimitive<Double, DoubleConsumer, double[], Spliterator.OfDouble, Node.OfDouble> {
            // 构造函数，初始化节点、数组和偏移量
            private OfDouble(Node.OfDouble node, double[] array, int offset) {
                super(node, array, offset);
            }
        }
    }

    @SuppressWarnings("serial")
    private static class CollectorTask<P_IN, P_OUT, T_NODE extends Node<P_OUT>, T_BUILDER extends Node.Builder<P_OUT>>
            extends AbstractTask<P_IN, P_OUT, T_NODE, CollectorTask<P_IN, P_OUT, T_NODE, T_BUILDER>> {
        // 保护成员变量，表示管道助手
        protected final PipelineHelper<P_OUT> helper;
        // 保护成员变量，表示构建器工厂
        protected final LongFunction<T_BUILDER> builderFactory;
        // 保护成员变量，表示合并工厂
        protected final BinaryOperator<T_NODE> concFactory;

        // 构造函数，初始化管道助手、分拆器、构建器工厂和合并工厂
        CollectorTask(PipelineHelper<P_OUT> helper,
                      Spliterator<P_IN> spliterator,
                      LongFunction<T_BUILDER> builderFactory,
                      BinaryOperator<T_NODE> concFactory) {
            super(helper, spliterator);
            this.helper = helper;
            this.builderFactory = builderFactory;
            this.concFactory = concFactory;
        }

        // 构造函数，用于子任务的创建，继承自父任务的管道助手、分拆器、构建器工厂和合并工厂
        CollectorTask(CollectorTask<P_IN, P_OUT, T_NODE, T_BUILDER> parent,
                      Spliterator<P_IN> spliterator) {
            super(parent, spliterator);
            helper = parent.helper;
            builderFactory = parent.builderFactory;
            concFactory = parent.concFactory;
        }

        @Override
        protected CollectorTask<P_IN, P_OUT, T_NODE, T_BUILDER> makeChild(Spliterator<P_IN> spliterator) {
            return new CollectorTask<>(this, spliterator);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected T_NODE doLeaf() {
            // 创建构建器并根据分拆器的大小初始化
            T_BUILDER builder = builderFactory.apply(helper.exactOutputSizeIfKnown(spliterator));
            // 将分拆器中的数据复制到构建器中并构建节点
            return (T_NODE) helper.wrapAndCopyInto(builder, spliterator).build();
        }


                    @Override
        public void onCompletion(CountedCompleter<?> caller) {
            if (!isLeaf())
                setLocalResult(concFactory.apply(leftChild.getLocalResult(), rightChild.getLocalResult()));
            super.onCompletion(caller);
        }

        @SuppressWarnings("serial")
        private static final class OfRef<P_IN, P_OUT>
                extends CollectorTask<P_IN, P_OUT, Node<P_OUT>, Node.Builder<P_OUT>> {
            OfRef(PipelineHelper<P_OUT> helper,
                  IntFunction<P_OUT[]> generator,
                  Spliterator<P_IN> spliterator) {
                super(helper, spliterator, s -> builder(s, generator), ConcNode::new);
            }
        }

        @SuppressWarnings("serial")
        private static final class OfInt<P_IN>
                extends CollectorTask<P_IN, Integer, Node.OfInt, Node.Builder.OfInt> {
            OfInt(PipelineHelper<Integer> helper, Spliterator<P_IN> spliterator) {
                super(helper, spliterator, Nodes::intBuilder, ConcNode.OfInt::new);
            }
        }

        @SuppressWarnings("serial")
        private static final class OfLong<P_IN>
                extends CollectorTask<P_IN, Long, Node.OfLong, Node.Builder.OfLong> {
            OfLong(PipelineHelper<Long> helper, Spliterator<P_IN> spliterator) {
                super(helper, spliterator, Nodes::longBuilder, ConcNode.OfLong::new);
            }
        }

        @SuppressWarnings("serial")
        private static final class OfDouble<P_IN>
                extends CollectorTask<P_IN, Double, Node.OfDouble, Node.Builder.OfDouble> {
            OfDouble(PipelineHelper<Double> helper, Spliterator<P_IN> spliterator) {
                super(helper, spliterator, Nodes::doubleBuilder, ConcNode.OfDouble::new);
            }
        }
    }
}
