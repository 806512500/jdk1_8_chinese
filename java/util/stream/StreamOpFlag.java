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

import java.util.EnumMap;
import java.util.Map;
import java.util.Spliterator;

/**
 * 与流和操作的特性相对应的标志。标志被流框架用于控制、专门化或优化计算。
 *
 * <p>
 * 流标志可用于描述与流相关的几种不同实体的特性：流源、中间操作和终端操作。并非所有流标志对所有实体都有意义；下表总结了哪些标志在什么上下文中是有意义的：
 *
 * <div>
 * <table>
 *   <caption>类型特性</caption>
 *   <thead class="tableSubHeadingColor">
 *     <tr>
 *       <th colspan="2">&nbsp;</th>
 *       <th>{@code DISTINCT}</th>
 *       <th>{@code SORTED}</th>
 *       <th>{@code ORDERED}</th>
 *       <th>{@code SIZED}</th>
 *       <th>{@code SHORT_CIRCUIT}</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *      <tr>
 *        <th colspan="2" class="tableSubHeadingColor">流源</th>
 *        <td>Y</td>
 *        <td>Y</td>
 *        <td>Y</td>
 *        <td>Y</td>
 *        <td>N</td>
 *      </tr>
 *      <tr>
 *        <th colspan="2" class="tableSubHeadingColor">中间操作</th>
 *        <td>PCI</td>
 *        <td>PCI</td>
 *        <td>PCI</td>
 *        <td>PC</td>
 *        <td>PI</td>
 *      </tr>
 *      <tr>
 *        <th colspan="2" class="tableSubHeadingColor">终端操作</th>
 *        <td>N</td>
 *        <td>N</td>
 *        <td>PC</td>
 *        <td>N</td>
 *        <td>PI</td>
 *      </tr>
 *   </tbody>
 *   <tfoot>
 *       <tr>
 *         <th class="tableSubHeadingColor" colspan="2">图例</th>
 *         <th colspan="6" rowspan="7">&nbsp;</th>
 *       </tr>
 *       <tr>
 *         <th class="tableSubHeadingColor">标志</th>
 *         <th class="tableSubHeadingColor">含义</th>
 *         <th colspan="6"></th>
 *       </tr>
 *       <tr><td>Y</td><td>允许</td></tr>
 *       <tr><td>N</td><td>无效</td></tr>
 *       <tr><td>P</td><td>保留</td></tr>
 *       <tr><td>C</td><td>清除</td></tr>
 *       <tr><td>I</td><td>注入</td></tr>
 *   </tfoot>
 * </table>
 * </div>
 *
 * <p>在上表中，“PCI”表示“可能保留、清除或注入”；“PC”表示“可能保留或清除”，“PI”表示“可能保留或注入”，“N”表示“无效”。
 *
 * <p>流标志由联合位集表示，因此单个字可以描述给定流实体的所有特性，并且例如，流源的标志可以与该流上的后续操作的标志高效地结合。
 *
 * <p>位掩码 {@link #STREAM_MASK}、{@link #OP_MASK} 和 {@link #TERMINAL_OP_MASK} 可以与流标志位集进行 AND 操作，以生成仅包含该实体类型的有效标志的掩码。
 *
 * <p>当描述流源时，只需描述该流具有哪些特性；当描述流操作时，需要描述该操作是否保留、注入或清除该特性。因此，每个标志使用两位，以便不仅表示特性的存在，还表示操作如何修改该特性。标志位通常以两种常见形式组合成一个 {@code int} 位集。 <em>流标志</em> 是通过 OR 运算符将 {@link #set()} 的枚举特性值（或更常见的，将前缀为 {@code IS_} 的相应静态命名常量）组合而成的联合位集。 <em>操作标志</em> 是通过 OR 运算符将 {@link #set()} 或 {@link #clear()} 的枚举特性值（分别用于注入或清除相应的标志）组合而成的联合位集，或更常见的，将前缀为 {@code IS_} 或 {@code NOT_} 的相应命名常量组合而成。未标记为 {@code IS_} 或 {@code NOT_} 的标志隐式地被视为保留。在组合位集时必须小心，以确保正确地应用了正确的组合操作。
 *
 * <p>
 * 除了 {@link #SHORT_CIRCUIT} 之外，流特性可以从等效的 {@link java.util.Spliterator} 特性派生：{@link java.util.Spliterator#DISTINCT}、{@link java.util.Spliterator#SORTED}、{@link java.util.Spliterator#ORDERED} 和 {@link java.util.Spliterator#SIZED}。可以使用方法 {@link #fromCharacteristics(java.util.Spliterator)} 将 spliterator 特性位集转换为流标志，并使用 {@link #toCharacteristics(int)} 转换回。 （位集 {@link #SPLITERATOR_CHARACTERISTICS_MASK} 用于与位集进行 AND 操作，以生成可以转换为流标志的有效 spliterator 特性位集。）
 *
 * <p>
 * 流的源封装了一个 spliterator。当该源 spliterator 的特性转换为流标志时，将是该流的流标志的适当子集。
 * 例如：
 * <pre> {@code
 *     Spliterator s = ...;
 *     Stream stream = Streams.stream(s);
 *     flagsFromSplitr = fromCharacteristics(s.characteristics());
 *     assert(flagsFromSplitr & stream.getStreamFlags() == flagsFromSplitr);
 * }</pre>
 *
 * <p>
 * 中间操作是在输入流上执行以创建新的输出流，可能保留、清除或注入流或操作特性。类似地，终端操作是在输入流上执行以生成输出结果，可能保留、清除或注入流或操作特性。保留意味着如果输入具有该特性，则输出也具有该特性。清除意味着无论输入如何，输出都不具有该特性。注入意味着无论输入如何，输出都具有该特性。如果未清除或注入某个特性，则隐式地保留该特性。
 *
 * <p>
 * 管道由封装 spliterator 的流源、一个或多个中间操作以及最终产生结果的终端操作组成。在管道的每个阶段，可以使用 {@link #combineOpFlags(int, int)} 计算组合的流和操作标志。这些标志确保在每个阶段保留保留、清除和注入信息。
 *
 * 管道源阶段的组合流和操作标志计算如下：
 * <pre> {@code
 *     int flagsForSourceStage = combineOpFlags(sourceFlags, INITIAL_OPS_VALUE);
 * }</pre>
 *
 * 管道中每个后续中间操作阶段的组合流和操作标志计算如下：
 * <pre> {@code
 *     int flagsForThisStage = combineOpFlags(flagsForPreviousStage, thisOpFlags);
 * }</pre>
 *
 * 最后，将管道最后一个中间操作的标志与终端操作的操作标志结合，以生成从管道输出的标志。
 *
 * <p>这些标志可用于应用优化。例如，如果 {@code SIZED.isKnown(flags)} 返回 true，则流大小在整个管道中保持不变，此信息可用于预分配数据结构，并与 {@link java.util.Spliterator#SUBSIZED} 结合使用，可以用于执行共享数组中的并发就地更新。
 *
 * 有关具体细节，请参阅 {@link AbstractPipeline} 构造函数。
 *
 * @since 1.8
 */
enum StreamOpFlag {

                /*
     * 每个特征在位集中占用2位，以容纳保留、清除和设置/注入信息。
     *
     * 这适用于流标志、中间/终端操作标志以及组合的流和操作标志。即使前者每个特征只需要1位信息，
     * 在组合标志时，对齐设置和注入位更为高效。
     *
     * 特征属于某些类型，参见Type枚举。类型的位掩码按照以下表格构建：
     *
     *                        DISTINCT  SORTED  ORDERED  SIZED  SHORT_CIRCUIT
     *          SPLITERATOR      01       01       01      01        00
     *               STREAM      01       01       01      01        00
     *                   OP      11       11       11      10        01
     *          TERMINAL_OP      00       00       10      00        01
     * UPSTREAM_TERMINAL_OP      00       00       10      00        00
     *
     * 01 = 设置/注入
     * 10 = 清除
     * 11 = 保留
     *
     * 非零值的列构建使用简单的构建器进行。
     */


    // 以下标志对应于Spliterator的特征
    // 值必须相等。
    //

    /**
     * 特征值表示，对于流中的每对遇到的元素 {@code x, y}，{@code !x.equals(y)}。
     * <p>
     * 流可以具有此值，或者中间操作可以保留、清除或注入此值。
     */
    // 0, 0x00000001
    // 匹配 Spliterator.DISTINCT
    DISTINCT(0,
             set(Type.SPLITERATOR).set(Type.STREAM).setAndClear(Type.OP)),

    /**
     * 特征值表示遇到的顺序遵循可比较元素的自然排序顺序。
     * <p>
     * 流可以具有此值，或者中间操作可以保留、清除或注入此值。
     * <p>
     * 注意：{@link java.util.Spliterator#SORTED} 特征可以定义一个关联的非空比较器的排序顺序。
     * 通过添加属性来增强标志状态，以便将这些属性传递给操作，需要对单一用例进行一些破坏性更改。
     * 此外，比较比较器的等价性超出身份比较可能是不可靠的。因此，对于定义的非自然排序顺序，
     * {@code SORTED} 特征不会内部映射到 {@code SORTED} 标志。
     */
    // 1, 0x00000004
    // 匹配 Spliterator.SORTED
    SORTED(1,
           set(Type.SPLITERATOR).set(Type.STREAM).setAndClear(Type.OP)),

    /**
     * 特征值表示流元素定义了遇到顺序。
     * <p>
     * 流可以具有此值，中间操作可以保留、清除或注入此值，或者终端操作可以保留或清除此值。
     */
    // 2, 0x00000010
    // 匹配 Spliterator.ORDERED
    ORDERED(2,
            set(Type.SPLITERATOR).set(Type.STREAM).setAndClear(Type.OP).clear(Type.TERMINAL_OP)
                    .clear(Type.UPSTREAM_TERMINAL_OP)),

    /**
     * 特征值表示流的大小是已知的有限大小，等于管道中第一个流的源Spliterator输入的已知有限大小。
     * <p>
     * 流可以具有此值，或者中间操作可以保留或清除此值。
     */
    // 3, 0x00000040
    // 匹配 Spliterator.SIZED
    SIZED(3,
          set(Type.SPLITERATOR).set(Type.STREAM).clear(Type.OP)),

    // 以下Spliterator特征当前未使用，但在位集中故意保留了一个空隙，以便在需要时无需修改其他标志值即可添加相应的流标志。
    //
    // 4, 0x00000100 NONNULL(4, ...
    // 5, 0x00000400 IMMUTABLE(5, ...
    // 6, 0x00001000 CONCURRENT(6, ...
    // 7, 0x00004000 SUBSIZED(7, ...

    // 以下4个标志当前未定义，可用于任何进一步的Spliterator特征。
    //
    //  8, 0x00010000
    //  9, 0x00040000
    // 10, 0x00100000
    // 11, 0x00400000

    // 以下标志特定于流和操作
    //

    /**
     * 特征值表示操作可能短路流。
     * <p>
     * 中间操作可以保留或注入此值，或者终端操作可以保留或注入此值。
     */
    // 12, 0x01000000
    SHORT_CIRCUIT(12,
                  set(Type.OP).set(Type.TERMINAL_OP));

    // 以下2个标志当前未定义，可用于任何进一步的流标志，如果/当需要时。
    //
    // 13, 0x04000000
    // 14, 0x10000000
    // 15, 0x40000000

    /**
     * 标志的类型
     */
    enum Type {
        /**
         * 标志与Spliterator特征相关联。
         */
        SPLITERATOR,

        /**
         * 标志与流标志相关联。
         */
        STREAM,

        /**
         * 标志与中间操作标志相关联。
         */
        OP,

        /**
         * 标志与终端操作标志相关联。
         */
        TERMINAL_OP,

        /**
         * 标志与终端操作标志相关联，这些标志在最后一个有状态操作边界上游传播。
         */
        UPSTREAM_TERMINAL_OP
    }

    /**
     * 设置/注入标志的位模式。
     */
    private static final int SET_BITS = 0b01;

    /**
     * 清除标志的位模式。
     */
    private static final int CLEAR_BITS = 0b10;

    /**
     * 保留标志的位模式。
     */
    private static final int PRESERVE_BITS = 0b11;

    private static MaskBuilder set(Type t) {
        return new MaskBuilder(new EnumMap<>(Type.class)).set(t);
    }

    private static class MaskBuilder {
        final Map<Type, Integer> map;


                    MaskBuilder(Map<Type, Integer> map) {
            this.map = map;
        }

        MaskBuilder mask(Type t, Integer i) {
            map.put(t, i);
            return this;
        }

        MaskBuilder set(Type t) {
            return mask(t, SET_BITS);
        }

        MaskBuilder clear(Type t) {
            return mask(t, CLEAR_BITS);
        }

        MaskBuilder setAndClear(Type t) {
            return mask(t, PRESERVE_BITS);
        }

        Map<Type, Integer> build() {
            for (Type t : Type.values()) {
                map.putIfAbsent(t, 0b00);
            }
            return map;
        }
    }

    /**
     * 该标志的掩码表，用于确定标志是否对应于某种标志类型以及用于创建掩码常量。
     */
    private final Map<Type, Integer> maskTable;

    /**
     * 位掩码中的位位置。
     */
    private final int bitPosition;

    /**
     * 位位置处的2位设置偏移。
     */
    private final int set;

    /**
     * 位位置处的2位清除偏移。
     */
    private final int clear;

    /**
     * 位位置处的2位保留偏移。
     */
    private final int preserve;

    private StreamOpFlag(int position, MaskBuilder maskBuilder) {
        this.maskTable = maskBuilder.build();
        // 每个标志两位
        position *= 2;
        this.bitPosition = position;
        this.set = SET_BITS << position;
        this.clear = CLEAR_BITS << position;
        this.preserve = PRESERVE_BITS << position;
    }

    /**
     * 获取与此特性设置相关的位图。
     *
     * @return 设置此特性的位图
     */
    int set() {
        return set;
    }

    /**
     * 获取与此特性清除相关的位图。
     *
     * @return 清除此特性的位图
     */
    int clear() {
        return clear;
    }

    /**
     * 确定此标志是否为基于流的标志。
     *
     * @return 如果是基于流的标志，则返回true，否则返回false。
     */
    boolean isStreamFlag() {
        return maskTable.get(Type.STREAM) > 0;
    }

    /**
     * 检查此标志是否设置在流标志、操作标志或组合流和操作标志上。
     *
     * @param flags 流标志、操作标志或组合流和操作标志
     * @return 如果此标志已知，则返回true，否则返回false。
     */
    boolean isKnown(int flags) {
        return (flags & preserve) == set;
    }

    /**
     * 检查此标志是否在操作标志或组合流和操作标志上被清除。
     *
     * @param flags 操作标志或组合流和操作标志。
     * @return 如果此标志被保留，则返回true，否则返回false。
     */
    boolean isCleared(int flags) {
        return (flags & preserve) == clear;
    }

    /**
     * 检查此标志是否在组合流和操作标志上被保留。
     *
     * @param flags 组合流和操作标志。
     * @return 如果此标志被保留，则返回true，否则返回false。
     */
    boolean isPreserved(int flags) {
        return (flags & preserve) == preserve;
    }

    /**
     * 确定此标志是否可以为标志类型设置。
     *
     * @param t 标志类型。
     * @return 如果此标志可以为标志类型设置，则返回true，否则返回false。
     */
    boolean canSet(Type t) {
        return (maskTable.get(t) & SET_BITS) > 0;
    }

    /**
     * 分割器特性的位掩码
     */
    static final int SPLITERATOR_CHARACTERISTICS_MASK = createMask(Type.SPLITERATOR);

    /**
     * 源流标志的位掩码。
     */
    static final int STREAM_MASK = createMask(Type.STREAM);

    /**
     * 中间操作标志的位掩码。
     */
    static final int OP_MASK = createMask(Type.OP);

    /**
     * 终端操作标志的位掩码。
     */
    static final int TERMINAL_OP_MASK = createMask(Type.TERMINAL_OP);

    /**
     * 上游终端操作标志的位掩码。
     */
    static final int UPSTREAM_TERMINAL_OP_MASK = createMask(Type.UPSTREAM_TERMINAL_OP);

    private static int createMask(Type t) {
        int mask = 0;
        for (StreamOpFlag flag : StreamOpFlag.values()) {
            mask |= flag.maskTable.get(t) << flag.bitPosition;
        }
        return mask;
    }

    /**
     * 完整的标志掩码。
     */
    private static final int FLAG_MASK = createFlagMask();

    private static int createFlagMask() {
        int mask = 0;
        for (StreamOpFlag flag : StreamOpFlag.values()) {
            mask |= flag.preserve;
        }
        return mask;
    }

    /**
     * 设置的流标志的标志掩码。
     */
    private static final int FLAG_MASK_IS = STREAM_MASK;

    /**
     * 清除的流标志的标志掩码。
     */
    private static final int FLAG_MASK_NOT = STREAM_MASK << 1;

    /**
     * 与管道中第一个流的流标志组合的初始值。
     */
    static final int INITIAL_OPS_VALUE = FLAG_MASK_IS | FLAG_MASK_NOT;

    /**
     * 设置或注入 {@link #DISTINCT} 的位值。
     */
    static final int IS_DISTINCT = DISTINCT.set;

    /**
     * 清除 {@link #DISTINCT} 的位值。
     */
    static final int NOT_DISTINCT = DISTINCT.clear;

    /**
     * 设置或注入 {@link #SORTED} 的位值。
     */
    static final int IS_SORTED = SORTED.set;

    /**
     * 清除 {@link #SORTED} 的位值。
     */
    static final int NOT_SORTED = SORTED.clear;

    /**
     * 设置或注入 {@link #ORDERED} 的位值。
     */
    static final int IS_ORDERED = ORDERED.set;

    /**
     * 清除 {@link #ORDERED} 的位值。
     */
    static final int NOT_ORDERED = ORDERED.clear;

    /**
     * 设置 {@link #SIZED} 的位值。
     */
    static final int IS_SIZED = SIZED.set;

    /**
     * 清除 {@link #SIZED} 的位值。
     */
    static final int NOT_SIZED = SIZED.clear;


                /**
     * 要注入的位值 {@link #SHORT_CIRCUIT}。
     */
    static final int IS_SHORT_CIRCUIT = SHORT_CIRCUIT.set;

    private static int getMask(int flags) {
        return (flags == 0)
               ? FLAG_MASK
               : ~(flags | ((FLAG_MASK_IS & flags) << 1) | ((FLAG_MASK_NOT & flags) >> 1));
    }

    /**
     * 将流或操作标志与之前组合的流和操作标志结合，以生成更新的组合流和操作标志。
     * <p>
     * 在流标志上设置或在操作标志上注入的标志，
     * 以及在组合流和操作标志上注入的标志，
     * 将在更新的组合流和操作标志上注入。
     *
     * <p>
     * 在流标志上设置或在操作标志上注入的标志，
     * 以及在组合流和操作标志上清除的标志，
     * 将在更新的组合流和操作标志上清除。
     *
     * <p>
     * 在流标志上设置或在操作标志上注入的标志，
     * 以及在组合流和操作标志上保留的标志，
     * 将在更新的组合流和操作标志上注入。
     *
     * <p>
     * 在流标志上未设置或在操作标志上清除/保留的标志，
     * 以及在组合流和操作标志上注入的标志，
     * 将在更新的组合流和操作标志上注入。
     *
     * <p>
     * 在流标志上未设置或在操作标志上清除/保留的标志，
     * 以及在组合流和操作标志上清除的标志，
     * 将在更新的组合流和操作标志上清除。
     *
     * <p>
     * 在流标志上未设置的标志，
     * 以及在组合流和操作标志上保留的标志，
     * 将在更新的组合流和操作标志上保留。
     *
     * <p>
     * 在操作标志上清除的标志，
     * 以及在组合流和操作标志上保留的标志，
     * 将在更新的组合流和操作标志上清除。
     *
     * <p>
     * 在操作标志上保留的标志，
     * 以及在组合流和操作标志上保留的标志，
     * 将在更新的组合流和操作标志上保留。
     *
     * @param newStreamOrOpFlags 流或操作标志。
     * @param prevCombOpFlags 之前组合的流和操作标志。
     *        必须使用 {#link INITIAL_OPS_VALUE} 作为种子值。
     * @return 更新的组合流和操作标志。
     */
    static int combineOpFlags(int newStreamOrOpFlags, int prevCombOpFlags) {
        // 0x01 或 0x10 的十六进制位转换为 0x11
        // 0x00 的十六进制位保持不变
        // 然后所有位取反
        // 然后结果与操作标志进行逻辑或操作。
        return (prevCombOpFlags & StreamOpFlag.getMask(newStreamOrOpFlags)) | newStreamOrOpFlags;
    }

    /**
     * 将组合的流和操作标志转换为流标志。
     *
     * <p>在组合的流和操作标志上注入的每个标志都将在流标志上设置。
     *
     * @param combOpFlags 组合的流和操作标志。
     * @return 流标志。
     */
    static int toStreamFlags(int combOpFlags) {
        // 通过取反十六进制位 0x11 变为 0x00，0x01 变为 0x10
        // 左移 1 位以恢复设置的标志，并屏蔽掉除设置的标志以外的所有内容
        return ((~combOpFlags) >> 1) & FLAG_MASK_IS & combOpFlags;
    }

    /**
     * 将流标志转换为拆分器特征位集。
     *
     * @param streamFlags 流标志。
     * @return 拆分器特征位集。
     */
    static int toCharacteristics(int streamFlags) {
        return streamFlags & SPLITERATOR_CHARACTERISTICS_MASK;
    }

    /**
     * 将拆分器特征位集转换为流标志。
     *
     * @implSpec
     * 如果拆分器自然为 {@code SORTED}（关联的 {@code Comparator} 为 {@code null}），则特征转换为 {@link #SORTED} 标志，否则不转换该特征。
     *
     * @param spliterator 从中获取特征位集的拆分器。
     * @return 流标志。
     */
    static int fromCharacteristics(Spliterator<?> spliterator) {
        int characteristics = spliterator.characteristics();
        if ((characteristics & Spliterator.SORTED) != 0 && spliterator.getComparator() != null) {
            // 如果特征不对应于自然排序顺序，则不传播 SORTED 特征
            return characteristics & SPLITERATOR_CHARACTERISTICS_MASK & ~Spliterator.SORTED;
        }
        else {
            return characteristics & SPLITERATOR_CHARACTERISTICS_MASK;
        }
    }

    /**
     * 将拆分器特征位集转换为流标志。
     *
     * @param characteristics 拆分器特征位集。
     * @return 流标志。
     */
    static int fromCharacteristics(int characteristics) {
        return characteristics & SPLITERATOR_CHARACTERISTICS_MASK;
    }
}
