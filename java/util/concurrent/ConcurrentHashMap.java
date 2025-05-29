
/*
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

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

import java.io.ObjectStreamField;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

/**
 * 一个支持完全并发检索和高预期并发更新的哈希表。此类遵循与 {@link java.util.Hashtable} 相同的功能规范，
 * 并包括与 {@code Hashtable} 的每个方法相对应的方法版本。然而，尽管所有操作都是线程安全的，检索操作
 * <em>不</em>涉及锁定，并且没有任何支持锁定整个表以防止所有访问的方式。此类在依赖其线程安全性但不依赖其
 * 同步细节的程序中与 {@code Hashtable} 完全互操作。
 *
 * <p>检索操作（包括 {@code get}）通常不会阻塞，因此可能与更新操作（包括 {@code put}
 * 和 {@code remove}）重叠。检索操作反映了在其开始时最近<em>完成</em>的更新操作的结果。（更正式地说，给定键的
 * 更新操作与任何（非空）检索操作报告更新值之间存在<em>先发生</em>关系。）对于诸如 {@code putAll} 和
 * {@code clear} 之类的聚合操作，同时进行的检索可能仅反映插入或删除的部分条目。同样，迭代器、分段迭代器和枚举
 * 返回的元素反映了哈希表在迭代器/枚举创建时或之后的某个时间点的状态。它们<em>不</em>抛出
 * {@link java.util.ConcurrentModificationException ConcurrentModificationException}。
 * 但是，迭代器设计为一次只能由一个线程使用。请注意，包括 {@code size}、{@code isEmpty} 和
 * {@code containsValue} 在内的聚合状态方法的结果通常只有在映射不被其他线程并发更新时才有用。
 * 否则，这些方法的结果反映了瞬态状态，可能足以用于监控或估算，但不适合用于程序控制。
 *
 * <p>当冲突过多（即，具有不同哈希码但模数表大小后落入同一槽位的键）时，表会动态扩展，预期平均效果是保持每个映射大约两个桶
 * （对应于 0.75 的负载因子阈值以进行调整大小）。随着映射的添加和删除，这个平均值可能会有很大变化，但总体上，这保持了哈希表
 * 常见的时间/空间权衡。然而，调整此或任何其他类型的哈希表大小可能是一个相对较慢的操作。如果可能，最好在可选的
 * {@code initialCapacity} 构造函数参数中提供大小估计。额外的可选 {@code loadFactor} 构造函数参数通过指定
 * 用于计算给定元素数量所需空间的表密度，提供了进一步自定义初始表容量的手段。此外，为了与此类的早期版本兼容，构造函数可以
 * 可选地指定一个预期的 {@code concurrencyLevel} 作为内部大小调整的额外提示。请注意，使用许多具有完全相同的
 * {@code hashCode()} 的键是减慢任何哈希表性能的可靠方法。为了减轻影响，当键是 {@link Comparable} 时，
 * 该类可能会使用键之间的比较顺序来帮助打破平局。
 *
 * <p>可以创建（使用 {@link #newKeySet()} 或 {@link #newKeySet(int)}）或查看（使用
 * {@link #keySet(Object)} 当仅对键感兴趣且映射值（可能是暂时的）不使用或都具有相同的映射值时）一个
 * {@link Set} 投影的 ConcurrentHashMap。
 *
 * <p>ConcurrentHashMap 可以用作可扩展的频率映射（一种直方图或多重集）通过使用 {@link
 * java.util.concurrent.atomic.LongAdder} 值并通过 {@link #computeIfAbsent computeIfAbsent} 初始化。
 * 例如，要向 {@code ConcurrentHashMap<String,LongAdder> freqs} 中添加一个计数，可以使用
 * {@code freqs.computeIfAbsent(k -> new LongAdder()).increment();}
 *
 * <p>此类及其视图和迭代器实现了 {@link Map} 和 {@link Iterator} 接口中所有<em>可选</em>的方法。
 *
 * <p>与 {@link Hashtable} 类似但与 {@link HashMap} 不同，此类<em>不</em>允许使用 {@code null} 作为键或值。
 *
 * <p>ConcurrentHashMap 支持一系列顺序和并行的批量操作，这些操作与大多数 {@link Stream} 方法不同，设计为可以安全地
 * 且通常合理地应用于即使被其他线程并发更新的映射；例如，当计算共享注册表中值的快照摘要时。有三种操作，每种操作有四种形式，
 * 接受具有键、值、条目和（键，值）参数和/或返回值的函数。由于 ConcurrentHashMap 中的元素没有特定的顺序，
 * 并且可能在不同的并行执行中以不同的顺序处理，因此提供的函数的正确性不应依赖于任何顺序，或任何可能在计算过程中暂时变化的其他对象或值；
 * 除了 forEach 操作外，理想情况下应是无副作用的。对 {@link java.util.Map.Entry} 对象的批量操作不支持方法
 * {@code setValue}。
 *
 * <ul>
 * <li> forEach: 对每个元素执行给定的操作。一个变体形式在执行操作之前对每个元素应用给定的转换。</li>
 *
 * <li> search: 返回对每个元素应用给定函数后找到的第一个非空结果；找到结果后跳过进一步的搜索。</li>
 *
 * <li> reduce: 累加每个元素。提供的归约函数不应依赖于顺序（更正式地说，它应该是关联且可交换的）。有五种变体：
 *
 * <ul>
 *
 * <li> 纯归约。没有 (key, value) 函数参数的形式，因为没有相应的返回类型。</li>
 *
 * <li> 映射归约，累加对每个元素应用给定函数的结果。</li>
 *
 * <li> 归约到标量 double、long 和 int，使用给定的基础值。</li>
 *
 * </ul>
 * </li>
 * </ul>
 *
 * <p>这些批量操作接受一个 {@code parallelismThreshold} 参数。如果当前映射大小估计小于给定阈值，则方法按顺序进行。
 * 使用值 {@code Long.MAX_VALUE} 可抑制所有并行性。使用值 {@code 1} 可通过将任务划分为足够多的子任务来实现最大并行性，
 * 以充分利用用于所有并行计算的 {@link ForkJoinPool#commonPool()}。通常，您会首先选择这些极端值之一，然后测量使用
 * 中间值的性能，这些值在开销与吞吐量之间进行权衡。
 *
 * <p>批量操作的并发属性源自 ConcurrentHashMap：从 {@code get(key)} 和相关访问方法返回的任何非空结果
 * 与关联的插入或更新之间存在先发生关系。任何批量操作的结果反映了这些每个元素关系的组合（但不一定是相对于整个映射的原子操作，
 * 除非以某种方式知道映射处于静止状态）。相反，因为映射中的键和值从不为 null，所以 null 作为当前没有结果的可靠原子指示器。
 * 为了保持这一属性，null 作为所有非标量归约操作的隐式基础。对于 double、long 和 int 版本，基础值应该是与任何其他值组合时
 * 返回该其他值的值（更正式地说，它应该是归约的单位元素）。大多数常见的归约操作具有这些属性；例如，计算总和时基础值为 0，
 * 计算最小值时基础值为 MAX_VALUE。
 *
 * <p>作为参数提供的搜索和转换函数应类似地返回 null 以指示没有结果（在这种情况下不会使用）。对于映射归约，这也允许转换
 * 作为过滤器，如果元素不应组合，则返回 null（或在原始特化的情况下，返回单位基础）。您可以通过根据“null 表示当前没有结果”
 * 的规则自行组合它们，然后再用于搜索或归约操作，来创建复合转换和过滤。
 *
 * <p>接受和/或返回 Entry 参数的方法维护键值关联。例如，当查找最大值的键时，这些方法可能很有用。请注意，可以使用
 * {@code new AbstractMap.SimpleEntry(k,v)} 提供“普通”Entry 参数。
 *
 * <p>批量操作可能会突然完成，抛出在应用提供的函数时遇到的异常。处理此类异常时，请记住其他并发执行的函数也可能抛出异常，
 * 或者如果第一个异常没有发生，它们也会抛出异常。
 *
 * <p>与顺序形式相比，平行形式的速度提升是常见的，但不是保证的。对于小映射上的简短函数，如果并行化计算的基础工作比计算本身更昂贵，
 * 平行形式可能会比顺序形式执行得更慢。同样，如果所有处理器都在执行不相关的任务，平行化可能不会带来太多实际的并行性。
 *
 * <p>所有任务方法的所有参数必须是非空的。
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @since 1.5
 * @author Doug Lea
 * @param <K> 此映射维护的键的类型
 * @param <V> 映射值的类型
 */
public class ConcurrentHashMap<K,V> extends AbstractMap<K,V>
    implements ConcurrentMap<K,V>, Serializable {
    private static final long serialVersionUID = 7249069246763182397L;

                /*
     * 概述：
     *
     * 该哈希表的主要设计目标是在最小化更新竞争的同时保持并发可读性（通常是 get() 方法，但也包括迭代器和相关方法）。次要目标是保持空间消耗与 java.util.HashMap 相当或更好，并支持在许多线程上对空表进行高初始插入率。
     *
     * 该映射通常作为分箱（桶化）哈希表运行。每个键值映射都存储在 Node 中。大多数节点是基本 Node 类的实例，具有 hash、key、value 和 next 字段。然而，存在各种子类：TreeNodes 被排列在平衡树中，而不是列表中。TreeBins 持有 TreeNode 集合的根。ForwardingNodes 在调整大小时放置在 bin 的头部。ReservationNodes 用作在 computeIfAbsent 和相关方法中建立值时的占位符。类型 TreeBin、ForwardingNode 和 ReservationNode 不持有正常的用户键、值或哈希值，并且在搜索等过程中容易区分，因为它们具有负的哈希字段和空的键和值字段。（这些特殊节点要么不常见，要么是瞬时的，因此携带一些未使用的字段的影响是微不足道的。）
     *
     * 表在第一次插入时被延迟初始化为2的幂大小。表中的每个 bin 通常包含一个 Node 列表（大多数情况下，列表只有零个或一个 Node）。表访问需要易失性/原子读取、写入和 CAS 操作。由于没有其他方法可以在不添加进一步间接的情况下安排这一点，我们使用内联（sun.misc.Unsafe）操作。
     *
     * 我们使用 Node 哈希字段的最高（符号）位进行控制——由于地址约束，它无论如何都是可用的。具有负哈希字段的节点在映射方法中被特别处理或忽略。
     *
     * 在空 bin 中插入（通过 put 或其变体）第一个节点是通过仅将它 CAS 到 bin 来完成的。这是在大多数键/哈希分布下 put 操作最常见的案例。其他更新操作（插入、删除和替换）需要锁。我们不想浪费与每个 bin 关联的锁对象所需的空间，因此使用 bin 列表的第一个节点本身作为锁。这些锁的锁定支持依赖于内置的“synchronized”监视器。
     *
     * 仅使用列表的第一个节点作为锁是不够的：当一个节点被锁定时，任何更新都必须首先验证它在锁定后仍然是第一个节点，如果不是，则重试。因为新节点总是被追加到列表中，所以一旦一个节点成为 bin 的第一个节点，它将保持第一个位置，直到被删除或 bin 无效（在调整大小时）。
     *
     * 每个 bin 锁的主要缺点是其他受同一锁保护的 bin 列表中的更新操作可能会停滞，例如当用户 equals() 或映射函数花费很长时间时。然而，统计上，在随机哈希码下，这不是一个常见问题。理想情况下，bin 中节点的频率遵循参数约为0.5的泊松分布（http://en.wikipedia.org/wiki/Poisson_distribution），平均给定0.75的调整大小阈值，尽管由于调整大小的粒度，方差很大。忽略方差，列表大小 k 的预期出现次数为 (exp(-0.5) * pow(0.5, k) / 阶乘(k))。前几个值是：
     *
     * 0:    0.60653066
     * 1:    0.30326533
     * 2:    0.07581633
     * 3:    0.01263606
     * 4:    0.00157952
     * 5:    0.00015795
     * 6:    0.00001316
     * 7:    0.00000094
     * 8:    0.00000006
     * 更多：小于千万分之一
     *
     * 两个线程访问不同元素的锁竞争概率在随机哈希下大约为 1 / (8 * #elements)。
     *
     * 实际遇到的哈希码分布有时会显著偏离均匀随机。这包括 N > (1<<30) 的情况，因此某些键必须碰撞。同样，对于设计为具有相同哈希码或仅在被屏蔽的高位不同的键的愚蠢或敌对使用也是如此。因此，当 bin 中的节点数超过阈值时，我们使用一种次级策略。这些 TreeBins 使用平衡树来保持节点（一种专门形式的红黑树），将搜索时间限制为 O(log N)。每个 TreeBin 中的搜索步骤至少比常规列表慢两倍，但鉴于 N 不能超过 (1<<64)（在地址耗尽之前），这将搜索步骤、锁持有时间等限制在合理的常数范围内（最坏情况下每次操作检查大约100个节点），只要键是可比较的（这非常常见——String、Long 等）。TreeBin 节点（TreeNodes）还保持与常规节点相同的“next”遍历指针，因此可以在迭代器中以相同的方式遍历。
     *
     * 当占用率超过百分比阈值时（名义上为0.75，但见下文），表将被调整大小。任何注意到过满 bin 的线程都可以在初始线程分配并设置替换数组后帮助调整大小。然而，这些其他线程不会停滞，而是可以继续插入等操作。使用 TreeBins 使我们在调整大小时免受最坏情况的影响。调整大小通过将 bin 从表转移到下一个表来完成。然而，线程在这样做之前会声明一小块索引（通过字段 transferIndex），以减少竞争。字段 sizeCtl 中的代戳确保调整大小不会重叠。由于我们使用2的幂扩展，每个 bin 的元素必须保持在相同的索引或移动一个2的幂偏移。我们通过捕捉旧节点可以重用的情况（因为它们的 next 字段不会改变）来消除不必要的节点创建。当表加倍时，平均只有大约六分之一的节点需要克隆。它们替换的节点将在不再被任何可能正在并发遍历表的读取线程引用时被垃圾回收。转移时，旧表 bin 仅包含一个特殊的转发节点（哈希字段为“MOVED”），其键为下一个表。在遇到转发节点时，访问和更新操作将重新启动，使用新表。
     *
     * 每个 bin 转移需要其 bin 锁，这在调整大小时可能会导致等待锁的停滞。然而，因为其他线程可以加入并帮助调整大小而不是竞争锁，随着调整大小的进行，平均总等待时间会缩短。转移操作还必须确保旧表和新表中所有可访问的 bin 在任何遍历中都是可用的。这在部分通过从最后一个 bin（table.length - 1）向上到第一个 bin 来安排。在看到转发节点时，遍历（参见类 Traverser）会安排移动到新表而不重新访问节点。为了确保即使在无序移动时也不会跳过任何中间节点，当遍历遇到转发节点时会创建一个堆栈（参见类 TableStack），以在稍后处理当前表时保持其位置。虽然这些保存/恢复机制的需求相对较少，但一旦遇到一个转发节点，通常会遇到更多。因此，Traversers 使用简单的缓存方案来避免创建如此多的新 TableStack 节点。（感谢 Peter Levart 建议在此使用堆栈。）
     *
     * 遍历方案也适用于 bin 范围的部分遍历（通过备用的 Traverser 构造函数）以支持分区聚合操作。此外，只读操作如果被转发到空表，则会放弃，这提供了对关闭式清除的支持，但目前尚未实现。
     *
     * 延迟表初始化最小化了首次使用前的占用空间，并且还避免了在第一次操作是 putAll、带映射参数的构造函数或反序列化时进行调整大小。这些情况试图覆盖初始容量设置，但在竞争情况下会无害地失败。
     *
     * 元素计数使用 LongAdder 的一个特化版本来维护。我们需要使用特化版本而不是直接使用 LongAdder 以便访问导致创建多个 CounterCells 的隐式竞争检测。计数机制避免了更新时的竞争，但在并发访问时如果读取过于频繁可能会遇到缓存冲突。为了避免读取过于频繁，只有在向已经包含两个或更多节点的 bin 添加时才会尝试在竞争下调整大小。在均匀的哈希分布下，这在阈值时发生的概率约为13%，这意味着只有大约八分之一的 put 操作会检查阈值（调整大小后，更少的操作会这样做）。
     *
     * TreeBins 使用一种特殊的比较形式进行搜索和相关操作（这是我们不能使用现有集合如 TreeMaps 的主要原因）。TreeBins 包含可比较元素，但也可能包含其他元素，以及可比较但不一定可比较为相同 T 的元素，因此我们不能在它们之间调用 compareTo。为了处理这一点，树主要按哈希值排序，然后在适用时按 Comparable.compareTo 顺序排序。在节点查找时，如果元素不可比较或比较结果为0，则在哈希值相同的情况下可能需要搜索左右子节点。（这对应于如果所有元素都是不可比较的且具有相同的哈希值时需要进行的完整列表搜索。）在插入时，为了在重新平衡期间保持总顺序（或尽可能接近），我们使用类和 identityHashCodes 作为决胜器。红黑平衡代码是从 pre-jdk-collections（http://gee.cs.oswego.edu/dl/classes/collections/RBCell.java）更新的，该代码又基于 Cormen, Leiserson, and Rivest 的“Introduction to Algorithms”（CLR）。
     *
     * TreeBins 还需要一个额外的锁定机制。虽然在更新期间列表遍历总是可能的，但树遍历不是，主要是因为树旋转可能会改变根节点和/或其链接。TreeBins 包含一个简单的读写锁机制，寄生在主 bin 同步策略上：与插入或删除相关的结构调整已经 bin 锁定（因此不会与其他写入者冲突），但必须等待正在进行的读取者完成。由于只能有一个这样的等待者，我们使用一个简单的方案，使用一个“等待者”字段来阻止写入者。然而，读取者永远不需要阻塞。如果根锁被持有，它们将沿着慢速遍历路径（通过 next 指针）前进，直到锁可用或列表耗尽，以先到者为准。这些情况不是很快，但最大化了总体预期吞吐量。
     *
     * 为了与该类的先前版本保持 API 和序列化兼容性，引入了几种奇异性。主要是：我们保留但未使用的构造参数引用 concurrencyLevel。我们接受一个 loadFactor 构造参数，但仅将其应用于初始表容量（这是我们可以保证遵守它的唯一时间）。我们还声明了一个在序列化时仅以最小形式实例化的未使用的“Segment”类。
     *
     * 此外，仅为了与该类的先前版本兼容，它扩展了 AbstractMap，即使所有方法都被重写，因此它只是无用的包袱。
     *
     * 该文件的组织是为了在阅读时使事情稍微容易一些：首先是主要的静态声明和实用程序，然后是字段，然后是主要的公共方法（将多个公共方法组合成内部方法的情况除外），然后是大小方法、树、遍历器和批量操作。
     */

                /* ---------------- Constants -------------- */

    /**
     * 可能的最大表容量。此值必须为 1<<30，以保持在 Java 数组分配和索引范围之内，适用于 2 的幂次的表大小，并且由于 32 位哈希字段的最高两位用于控制目的，所以需要此值。
     */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * 此表的默认初始表容量。必须是 2 的幂次（即至少为 1），并且最多为 MAXIMUM_CAPACITY。
     */
    private static final int DEFAULT_CAPACITY = 16;

    /**
     * 可能的最大（非 2 的幂次）数组大小。
     * 由 toArray 和相关方法需要。
     */
    static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * 此表的默认并发级别。未使用，但为了与此类的早期版本兼容而定义。
     */
    private static final int DEFAULT_CONCURRENCY_LEVEL = 16;

    /**
     * 此表的加载因子。构造函数中对此值的覆盖仅影响初始表容量。实际的浮点值通常不使用——使用诸如 {@code n - (n >>> 2)} 之类的表达式来计算相关的调整大小阈值更为简单。
     */
    private static final float LOAD_FACTOR = 0.75f;

    /**
     * 使用树而不是列表的 bin 计数阈值。当向具有至少这么多节点的 bin 添加元素时，bin 会转换为树。此值必须大于 2，并且为了与树移除时转换回普通 bin 的假设相匹配，至少应为 8。
     */
    static final int TREEIFY_THRESHOLD = 8;

    /**
     * 在调整大小操作期间取消树化的（拆分）bin 的 bin 计数阈值。应小于 TREEIFY_THRESHOLD，并且最多为 6，以与删除时的收缩检测相匹配。
     */
    static final int UNTREEIFY_THRESHOLD = 6;

    /**
     * bins 可能被树化的最小表容量。（否则，如果 bin 中的节点过多，表将被调整大小。）此值应至少为 4 * TREEIFY_THRESHOLD，以避免调整大小和树化阈值之间的冲突。
     */
    static final int MIN_TREEIFY_CAPACITY = 64;

    /**
     * 每个转移步骤的最小重新分配数量。范围被细分以允许多个调整大小的线程。此值作为下限，以避免调整大小器遇到过多的内存争用。该值应至少为 DEFAULT_CAPACITY。
     */
    private static final int MIN_TRANSFER_STRIDE = 16;

    /**
     * sizeCtl 中用于生成戳记的位数。
     * 对于 32 位数组，必须至少为 6。
     */
    private static int RESIZE_STAMP_BITS = 16;

    /**
     * 可以帮助调整大小的最大线程数。
     * 必须适合 32 - RESIZE_STAMP_BITS 位。
     */
    private static final int MAX_RESIZERS = (1 << (32 - RESIZE_STAMP_BITS)) - 1;

    /**
     * 在 sizeCtl 中记录大小戳记的位移。
     */
    private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;

    /*
     * Node 哈希字段的编码。参见上文的解释。
     */
    static final int MOVED     = -1; // 转发节点的哈希
    static final int TREEBIN   = -2; // 树根的哈希
    static final int RESERVED  = -3; // 暂时保留的哈希
    static final int HASH_BITS = 0x7fffffff; // 普通节点哈希的可用位

    /** CPU 数量，用于限制某些大小 */
    static final int NCPU = Runtime.getRuntime().availableProcessors();

    /** 为了序列化兼容性。 */
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("segments", Segment[].class),
        new ObjectStreamField("segmentMask", Integer.TYPE),
        new ObjectStreamField("segmentShift", Integer.TYPE)
    };

    /* ---------------- Nodes -------------- */

    /**
     * 键值条目。此类从未作为用户可修改的 Map.Entry（即支持 setValue；参见下面的 MapEntry）导出，但可用于只读遍历，用于批量任务。具有负哈希字段的 Node 子类是特殊的，包含 null 键和值（但永远不会导出）。否则，键和值永远不会为 null。
     */
    static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;
        final K key;
        volatile V val;
        volatile Node<K,V> next;

        Node(int hash, K key, V val, Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.val = val;
            this.next = next;
        }

        public final K getKey()       { return key; }
        public final V getValue()     { return val; }
        public final int hashCode()   { return key.hashCode() ^ val.hashCode(); }
        public final String toString(){ return key + "=" + val; }
        public final V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        public final boolean equals(Object o) {
            Object k, v, u; Map.Entry<?,?> e;
            return ((o instanceof Map.Entry) &&
                    (k = (e = (Map.Entry<?,?>)o).getKey()) != null &&
                    (v = e.getValue()) != null &&
                    (k == key || k.equals(key)) &&
                    (v == (u = val) || v.equals(u)));
        }

        /**
         * 为 map.get() 提供虚拟支持；在子类中被覆盖。
         */
        Node<K,V> find(int h, Object k) {
            Node<K,V> e = this;
            if (k != null) {
                do {
                    K ek;
                    if (e.hash == h &&
                        ((ek = e.key) == k || (ek != null && k.equals(ek))))
                        return e;
                } while ((e = e.next) != null);
            }
            return null;
        }
    }

    /* ---------------- Static utilities -------------- */

                /**
     * 将哈希的高位扩展（异或）到低位，并且强制最高位为0。因为表使用了2的幂次掩码，所以只在当前掩码以上的位不同的哈希集
     * 总是会发生冲突。（已知的例子包括在小表中持有连续整数的Float键集。）所以我们应用一个变换，将高位的影响向下扩展。
     * 在速度、效用和位扩展质量之间存在权衡。因为许多常见的哈希集已经分布得相当合理（因此不需要扩展），并且因为我们使用树来处理
     * 二进制中的大量冲突集，我们只是以最便宜的方式异或一些移位的位，以减少系统性的损失，同时也将由于表的边界限制而从未在索引计算中
     * 使用的最高位的影响结合进来。
     */
    static final int spread(int h) {
        return (h ^ (h >>> 16)) & HASH_BITS;
    }

    /**
     * 返回给定期望容量的2的幂次表大小。
     * 参见《Hacker's Delight》第3.2节
     */
    private static final int tableSizeFor(int c) {
        int n = c - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /**
     * 如果x的形式为 "class C implements Comparable<C>"，则返回x的Class，否则返回null。
     */
    static Class<?> comparableClassFor(Object x) {
        if (x instanceof Comparable) {
            Class<?> c; Type[] ts, as; Type t; ParameterizedType p;
            if ((c = x.getClass()) == String.class) // 绕过检查
                return c;
            if ((ts = c.getGenericInterfaces()) != null) {
                for (int i = 0; i < ts.length; ++i) {
                    if (((t = ts[i]) instanceof ParameterizedType) &&
                        ((p = (ParameterizedType)t).getRawType() ==
                         Comparable.class) &&
                        (as = p.getActualTypeArguments()) != null &&
                        as.length == 1 && as[0] == c) // 类型参数是c
                        return c;
                }
            }
        }
        return null;
    }

    /**
     * 如果x匹配kc（k的筛选后的Comparable类），则返回k.compareTo(x)，否则返回0。
     */
    @SuppressWarnings({"rawtypes","unchecked"}) // 用于转换为Comparable
    static int compareComparables(Class<?> kc, Object k, Object x) {
        return (x == null || x.getClass() != kc ? 0 :
                ((Comparable)k).compareTo(x));
    }

    /* ---------------- 表元素访问 -------------- */

    /*
     * 使用易失性访问方法用于表元素以及在调整大小时正在使用的下一个表的元素。所有对tab参数的使用都必须由调用者进行空检查。
     * 所有调用者还偏执地预先检查tab的长度是否不为零（或等效检查），从而确保任何形式为哈希值与（长度 - 1）按位与的索引参数
     * 是有效的。注意，为了正确处理用户的任意并发错误，这些检查必须在局部变量上操作，这解释了下面一些看起来很奇怪的内联赋值。
     * 注意，调用setTabAt总是发生在锁定区域内，因此原则上只需要释放顺序，而不是完全的易失性语义，但目前编码为易失性写入以保持保守。
     */

    @SuppressWarnings("unchecked")
    static final <K,V> Node<K,V> tabAt(Node<K,V>[] tab, int i) {
        return (Node<K,V>)U.getObjectVolatile(tab, ((long)i << ASHIFT) + ABASE);
    }

    static final <K,V> boolean casTabAt(Node<K,V>[] tab, int i,
                                        Node<K,V> c, Node<K,V> v) {
        return U.compareAndSwapObject(tab, ((long)i << ASHIFT) + ABASE, c, v);
    }

    static final <K,V> void setTabAt(Node<K,V>[] tab, int i, Node<K,V> v) {
        U.putObjectVolatile(tab, ((long)i << ASHIFT) + ABASE, v);
    }

    /* ---------------- 字段 -------------- */

    /**
     * 二进制数组。在第一次插入时延迟初始化。大小总是2的幂次。直接由迭代器访问。
     */
    transient volatile Node<K,V>[] table;

    /**
     * 下一个要使用的表；仅在调整大小时非空。
     */
    private transient volatile Node<K,V>[] nextTable;

    /**
     * 基础计数值，主要用于没有竞争时，但在表初始化竞争中也作为回退。通过CAS更新。
     */
    private transient volatile long baseCount;

    /**
     * 表初始化和调整大小控制。当为负数时，表正在初始化或调整大小：-1表示初始化，否则表示-(1 + 活动调整大小线程的数量)。
     * 否则，当表为空时，保存创建时使用的初始表大小，或0表示默认值。初始化后，保存表的下一个元素计数值，以触发调整大小。
     */
    private transient volatile int sizeCtl;

    /**
     * 调整大小时要拆分的下一个表索引（加一）。
     */
    private transient volatile int transferIndex;

    /**
     * 调整大小和/或创建CounterCells时使用的自旋锁（通过CAS锁定）。
     */
    private transient volatile int cellsBusy;

    /**
     * 计数单元表。当非空时，大小是2的幂次。
     */
    private transient volatile CounterCell[] counterCells;

    // 视图
    private transient KeySetView<K,V> keySet;
    private transient ValuesView<K,V> values;
    private transient EntrySetView<K,V> entrySet;


    /* ---------------- 公共操作 -------------- */

    /**
     * 创建一个新的、空的映射，使用默认的初始表大小（16）。
     */
    public ConcurrentHashMap() {
    }

    /**
     * 创建一个新的、空的映射，使用能够容纳指定数量元素的初始表大小，而不需要动态调整大小。
     *
     * @param initialCapacity 实现内部调整大小以容纳这么多元素。
     * @throws IllegalArgumentException 如果元素的初始容量为负数
     */
    public ConcurrentHashMap(int initialCapacity) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException();
        int cap = ((initialCapacity >= (MAXIMUM_CAPACITY >>> 1)) ?
                   MAXIMUM_CAPACITY :
                   tableSizeFor(initialCapacity + (initialCapacity >>> 1) + 1));
        this.sizeCtl = cap;
    }


                /**
     * 创建一个具有与给定映射相同映射的新映射。
     *
     * @param m 映射
     */
    public ConcurrentHashMap(Map<? extends K, ? extends V> m) {
        this.sizeCtl = DEFAULT_CAPACITY;
        putAll(m);
    }

    /**
     * 创建一个新的、空的映射，其初始表大小基于给定的元素数量（{@code initialCapacity}）和
     * 表的密度（{@code loadFactor}）。
     *
     * @param initialCapacity 初始容量。实现执行内部大小调整以容纳这么多元素，
     * 给定指定的负载因子。
     * @param loadFactor 负载因子（表密度），用于确定初始表大小
     * @throws IllegalArgumentException 如果元素的初始容量为负数或负载因子为非正数
     *
     * @since 1.6
     */
    public ConcurrentHashMap(int initialCapacity, float loadFactor) {
        this(initialCapacity, loadFactor, 1);
    }

    /**
     * 创建一个新的、空的映射，其初始表大小基于给定的元素数量（{@code initialCapacity}）、表
     * 密度（{@code loadFactor}）和并发更新线程数（{@code concurrencyLevel}）。
     *
     * @param initialCapacity 初始容量。实现执行内部大小调整以容纳这么多元素，
     * 给定指定的负载因子。
     * @param loadFactor 负载因子（表密度），用于确定初始表大小
     * @param concurrencyLevel 估计的并发更新线程数。实现可以使用此值作为大小提示。
     * @throws IllegalArgumentException 如果初始容量为负数或负载因子或并发级别为非正数
     */
    public ConcurrentHashMap(int initialCapacity,
                             float loadFactor, int concurrencyLevel) {
        if (!(loadFactor > 0.0f) || initialCapacity < 0 || concurrencyLevel <= 0)
            throw new IllegalArgumentException();
        if (initialCapacity < concurrencyLevel)   // 使用至少与估计线程数一样多的桶
            initialCapacity = concurrencyLevel;   // 作为估计的线程数
        long size = (long)(1.0 + (long)initialCapacity / loadFactor);
        int cap = (size >= (long)MAXIMUM_CAPACITY) ?
            MAXIMUM_CAPACITY : tableSizeFor((int)size);
        this.sizeCtl = cap;
    }

    // 原始（自JDK1.2起）Map方法

    /**
     * {@inheritDoc}
     */
    public int size() {
        long n = sumCount();
        return ((n < 0L) ? 0 :
                (n > (long)Integer.MAX_VALUE) ? Integer.MAX_VALUE :
                (int)n);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return sumCount() <= 0L; // 忽略瞬时负值
    }

    /**
     * 返回指定键所映射的值，如果此映射不包含该键的映射，则返回 {@code null}。
     *
     * <p>更正式地说，如果此映射包含从键 {@code k} 到值 {@code v} 的映射，使得 {@code key.equals(k)}，
     * 则此方法返回 {@code v}；否则返回 {@code null}。 （最多只能有一个这样的映射。）
     *
     * @throws NullPointerException 如果指定的键为 null
     */
    public V get(Object key) {
        Node<K,V>[] tab; Node<K,V> e, p; int n, eh; K ek;
        int h = spread(key.hashCode());
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (e = tabAt(tab, (n - 1) & h)) != null) {
            if ((eh = e.hash) == h) {
                if ((ek = e.key) == key || (ek != null && key.equals(ek)))
                    return e.val;
            }
            else if (eh < 0)
                return (p = e.find(h, key)) != null ? p.val : null;
            while ((e = e.next) != null) {
                if (e.hash == h &&
                    ((ek = e.key) == key || (ek != null && key.equals(ek))))
                    return e.val;
            }
        }
        return null;
    }

    /**
     * 测试指定的对象是否是此表中的键。
     *
     * @param  key 可能的键
     * @return 如果且仅当指定的对象是此表中的键，根据 {@code equals} 方法确定，则返回 {@code true}；否则返回 {@code false}
     * @throws NullPointerException 如果指定的键为 null
     */
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    /**
     * 如果此映射将一个或多个键映射到指定值，则返回 {@code true}。注意：此方法可能需要遍历整个映射，
     * 比 {@code containsKey} 方法慢得多。
     *
     * @param value 要测试其在此映射中是否存在
     * @return 如果此映射将一个或多个键映射到指定值，则返回 {@code true}
     * @throws NullPointerException 如果指定的值为 null
     */
    public boolean containsValue(Object value) {
        if (value == null)
            throw new NullPointerException();
        Node<K,V>[] t;
        if ((t = table) != null) {
            Traverser<K,V> it = new Traverser<K,V>(t, t.length, 0, t.length);
            for (Node<K,V> p; (p = it.advance()) != null; ) {
                V v;
                if ((v = p.val) == value || (v != null && value.equals(v)))
                    return true;
            }
        }
        return false;
    }

    /**
     * 在此表中将指定的键映射到指定的值。键和值都不能为 null。
     *
     * <p>可以通过调用带有与原始键相等的键的 {@code get} 方法来检索值。
     *
     * @param key 要与指定值关联的键
     * @param value 要与指定键关联的值
     * @return 与 {@code key} 关联的前一个值，如果 {@code key} 没有映射，则返回 {@code null}
     * @throws NullPointerException 如果指定的键或值为 null
     */
    public V put(K key, V value) {
        return putVal(key, value, false);
    }


                /** 实现 put 和 putIfAbsent 方法 */
    final V putVal(K key, V value, boolean onlyIfAbsent) {
        if (key == null || value == null) throw new NullPointerException();
        int hash = spread(key.hashCode());
        int binCount = 0;
        for (Node<K,V>[] tab = table;;) {
            Node<K,V> f; int n, i, fh;
            if (tab == null || (n = tab.length) == 0)
                tab = initTable();
            else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
                if (casTabAt(tab, i, null,
                             new Node<K,V>(hash, key, value, null)))
                    break;                   // 在空桶中添加时无需锁定
            }
            else if ((fh = f.hash) == MOVED)
                tab = helpTransfer(tab, f);
            else {
                V oldVal = null;
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        if (fh >= 0) {
                            binCount = 1;
                            for (Node<K,V> e = f;; ++binCount) {
                                K ek;
                                if (e.hash == hash &&
                                    ((ek = e.key) == key ||
                                     (ek != null && key.equals(ek)))) {
                                    oldVal = e.val;
                                    if (!onlyIfAbsent)
                                        e.val = value;
                                    break;
                                }
                                Node<K,V> pred = e;
                                if ((e = e.next) == null) {
                                    pred.next = new Node<K,V>(hash, key,
                                                              value, null);
                                    break;
                                }
                            }
                        }
                        else if (f instanceof TreeBin) {
                            Node<K,V> p;
                            binCount = 2;
                            if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                           value)) != null) {
                                oldVal = p.val;
                                if (!onlyIfAbsent)
                                    p.val = value;
                            }
                        }
                    }
                }
                if (binCount != 0) {
                    if (binCount >= TREEIFY_THRESHOLD)
                        treeifyBin(tab, i);
                    if (oldVal != null)
                        return oldVal;
                    break;
                }
            }
        }
        addCount(1L, binCount);
        return null;
    }

    /**
     * 将指定映射中的所有映射复制到此映射中。
     * 这些映射将替换此映射中当前在指定映射中的任何键的任何映射。
     *
     * @param m 要存储在此映射中的映射
     */
    public void putAll(Map<? extends K, ? extends V> m) {
        tryPresize(m.size());
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
            putVal(e.getKey(), e.getValue(), false);
    }

    /**
     * 从此映射中移除键（及其对应的值）。如果键不在映射中，则此方法不执行任何操作。
     *
     * @param  key 需要移除的键
     * @return 之前与 {@code key} 关联的值，或如果 {@code key} 没有映射则为 {@code null}
     * @throws NullPointerException 如果指定的键为 null
     */
    public V remove(Object key) {
        return replaceNode(key, null, null);
    }

    /**
     * 实现四个公共 remove/replace 方法：
     * 如果 cv 不为 null，则在匹配 cv 的条件下将节点值替换为 v。如果结果值为 null，则删除。
     */
    final V replaceNode(Object key, V value, Object cv) {
        int hash = spread(key.hashCode());
        for (Node<K,V>[] tab = table;;) {
            Node<K,V> f; int n, i, fh;
            if (tab == null || (n = tab.length) == 0 ||
                (f = tabAt(tab, i = (n - 1) & hash)) == null)
                break;
            else if ((fh = f.hash) == MOVED)
                tab = helpTransfer(tab, f);
            else {
                V oldVal = null;
                boolean validated = false;
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        if (fh >= 0) {
                            validated = true;
                            for (Node<K,V> e = f, pred = null;;) {
                                K ek;
                                if (e.hash == hash &&
                                    ((ek = e.key) == key ||
                                     (ek != null && key.equals(ek)))) {
                                    V ev = e.val;
                                    if (cv == null || cv == ev ||
                                        (ev != null && cv.equals(ev))) {
                                        oldVal = ev;
                                        if (value != null)
                                            e.val = value;
                                        else if (pred != null)
                                            pred.next = e.next;
                                        else
                                            setTabAt(tab, i, e.next);
                                    }
                                    break;
                                }
                                pred = e;
                                if ((e = e.next) == null)
                                    break;
                            }
                        }
                        else if (f instanceof TreeBin) {
                            validated = true;
                            TreeBin<K,V> t = (TreeBin<K,V>)f;
                            TreeNode<K,V> r, p;
                            if ((r = t.root) != null &&
                                (p = r.findTreeNode(hash, key, null)) != null) {
                                V pv = p.val;
                                if (cv == null || cv == pv ||
                                    (pv != null && cv.equals(pv))) {
                                    oldVal = pv;
                                    if (value != null)
                                        p.val = value;
                                    else if (t.removeTreeNode(p))
                                        setTabAt(tab, i, untreeify(t.first));
                                }
                            }
                        }
                    }
                }
                if (validated) {
                    if (oldVal != null) {
                        if (value == null)
                            addCount(-1L, -1);
                        return oldVal;
                    }
                    break;
                }
            }
        }
        return null;
    }


                /**
     * 从这个映射中移除所有的映射。
     */
    public void clear() {
        long delta = 0L; // 负数的删除数量
        int i = 0;
        Node<K,V>[] tab = table;
        while (tab != null && i < tab.length) {
            int fh;
            Node<K,V> f = tabAt(tab, i);
            if (f == null)
                ++i;
            else if ((fh = f.hash) == MOVED) {
                tab = helpTransfer(tab, f);
                i = 0; // 重新开始
            }
            else {
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        Node<K,V> p = (fh >= 0 ? f :
                                       (f instanceof TreeBin) ?
                                       ((TreeBin<K,V>)f).first : null);
                        while (p != null) {
                            --delta;
                            p = p.next;
                        }
                        setTabAt(tab, i++, null);
                    }
                }
            }
        }
        if (delta != 0L)
            addCount(delta, -1);
    }

    /**
     * 返回一个包含此映射中所有键的 {@link Set} 视图。
     * 该集合由映射支持，因此映射的更改会反映在集合中，反之亦然。集合支持元素移除，这将从映射中移除相应的映射，
     * 通过 {@code Iterator.remove}、{@code Set.remove}、{@code removeAll}、{@code retainAll} 和 {@code clear} 操作。
     * 它不支持 {@code add} 或 {@code addAll} 操作。
     *
     * <p>视图的迭代器和拆分器是
     * <a href="package-summary.html#Weakly"><i>弱一致的</i></a>。
     *
     * <p>视图的 {@code spliterator} 报告 {@link Spliterator#CONCURRENT}、
     * {@link Spliterator#DISTINCT} 和 {@link Spliterator#NONNULL}。
     *
     * @return 键的集合视图
     */
    public KeySetView<K,V> keySet() {
        KeySetView<K,V> ks;
        return (ks = keySet) != null ? ks : (keySet = new KeySetView<K,V>(this, null));
    }

    /**
     * 返回一个包含此映射中所有值的 {@link Collection} 视图。
     * 该集合由映射支持，因此映射的更改会反映在集合中，反之亦然。集合支持元素移除，这将从映射中移除相应的映射，
     * 通过 {@code Iterator.remove}、{@code Collection.remove}、{@code removeAll}、
     * {@code retainAll} 和 {@code clear} 操作。它不支持 {@code add} 或 {@code addAll} 操作。
     *
     * <p>视图的迭代器和拆分器是
     * <a href="package-summary.html#Weakly"><i>弱一致的</i></a>。
     *
     * <p>视图的 {@code spliterator} 报告 {@link Spliterator#CONCURRENT} 和 {@link Spliterator#NONNULL}。
     *
     * @return 值的集合视图
     */
    public Collection<V> values() {
        ValuesView<K,V> vs;
        return (vs = values) != null ? vs : (values = new ValuesView<K,V>(this));
    }

    /**
     * 返回一个包含此映射中所有映射的 {@link Set} 视图。
     * 该集合由映射支持，因此映射的更改会反映在集合中，反之亦然。集合支持元素移除，这将从映射中移除相应的映射，
     * 通过 {@code Iterator.remove}、{@code Set.remove}、{@code removeAll}、
     * {@code retainAll} 和 {@code clear} 操作。
     *
     * <p>视图的迭代器和拆分器是
     * <a href="package-summary.html#Weakly"><i>弱一致的</i></a>。
     *
     * <p>视图的 {@code spliterator} 报告 {@link Spliterator#CONCURRENT}、
     * {@link Spliterator#DISTINCT} 和 {@link Spliterator#NONNULL}。
     *
     * @return 映射的集合视图
     */
    public Set<Map.Entry<K,V>> entrySet() {
        EntrySetView<K,V> es;
        return (es = entrySet) != null ? es : (entrySet = new EntrySetView<K,V>(this));
    }

    /**
     * 返回此 {@link Map} 的哈希码值，即映射中每个键值对的
     * {@code key.hashCode() ^ value.hashCode()} 的总和。
     *
     * @return 此映射的哈希码值
     */
    public int hashCode() {
        int h = 0;
        Node<K,V>[] t;
        if ((t = table) != null) {
            Traverser<K,V> it = new Traverser<K,V>(t, t.length, 0, t.length);
            for (Node<K,V> p; (p = it.advance()) != null; )
                h += p.key.hashCode() ^ p.val.hashCode();
        }
        return h;
    }

    /**
     * 返回此映射的字符串表示形式。字符串表示形式由括号 ("{@code {}}") 包围的一系列键值映射组成（没有特定顺序）。
     * 相邻的映射由字符 {@code ", "}（逗号和空格）分隔。每个键值映射呈现为键后跟等号 ("{@code =}") 再跟关联的值。
     *
     * @return 此映射的字符串表示形式
     */
    public String toString() {
        Node<K,V>[] t;
        int f = (t = table) == null ? 0 : t.length;
        Traverser<K,V> it = new Traverser<K,V>(t, f, 0, f);
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        Node<K,V> p;
        if ((p = it.advance()) != null) {
            for (;;) {
                K k = p.key;
                V v = p.val;
                sb.append(k == this ? "(this Map)" : k);
                sb.append('=');
                sb.append(v == this ? "(this Map)" : v);
                if ((p = it.advance()) == null)
                    break;
                sb.append(',').append(' ');
            }
        }
        return sb.append('}').toString();
    }

    /**
     * 比较指定对象与此映射是否相等。
     * 如果给定对象是一个具有与此映射相同映射的映射，则返回 {@code true}。如果在执行此方法期间任一映射被并发修改，此操作可能会返回误导性结果。
     *
     * @param o 要与此映射比较相等性的对象
     * @return 如果指定对象等于此映射，则返回 {@code true}
     */
    public boolean equals(Object o) {
        if (o != this) {
            if (!(o instanceof Map))
                return false;
            Map<?,?> m = (Map<?,?>) o;
            Node<K,V>[] t;
            int f = (t = table) == null ? 0 : t.length;
            Traverser<K,V> it = new Traverser<K,V>(t, f, 0, f);
            for (Node<K,V> p; (p = it.advance()) != null; ) {
                V val = p.val;
                Object v = m.get(p.key);
                if (v == null || (v != val && !v.equals(val)))
                    return false;
            }
            for (Map.Entry<?,?> e : m.entrySet()) {
                Object mk, mv, v;
                if ((mk = e.getKey()) == null ||
                    (mv = e.getValue()) == null ||
                    (v = get(mk)) == null ||
                    (mv != v && !mv.equals(v)))
                    return false;
            }
        }
        return true;
    }


                /**
     * 简化版本的辅助类，用于之前的版本，
     * 声明的目的是为了序列化兼容性
     */
    static class Segment<K,V> extends ReentrantLock implements Serializable {
        private static final long serialVersionUID = 2249069246763182397L;
        final float loadFactor;
        Segment(float lf) { this.loadFactor = lf; }
    }

    /**
     * 将 {@code ConcurrentHashMap} 实例的状态保存到流中（即，序列化它）。
     * @param s 流
     * @throws java.io.IOException 如果发生 I/O 错误
     * @serialData
     * 每个键值映射的键（Object）和值（Object），最后是一个 null 对。
     * 键值映射的顺序没有特定要求。
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        // 为了序列化兼容性
        // 模拟此类早期版本中的段计算
        int sshift = 0;
        int ssize = 1;
        while (ssize < DEFAULT_CONCURRENCY_LEVEL) {
            ++sshift;
            ssize <<= 1;
        }
        int segmentShift = 32 - sshift;
        int segmentMask = ssize - 1;
        @SuppressWarnings("unchecked")
        Segment<K,V>[] segments = (Segment<K,V>[])
            new Segment<?,?>[DEFAULT_CONCURRENCY_LEVEL];
        for (int i = 0; i < segments.length; ++i)
            segments[i] = new Segment<K,V>(LOAD_FACTOR);
        s.putFields().put("segments", segments);
        s.putFields().put("segmentShift", segmentShift);
        s.putFields().put("segmentMask", segmentMask);
        s.writeFields();

        Node<K,V>[] t;
        if ((t = table) != null) {
            Traverser<K,V> it = new Traverser<K,V>(t, t.length, 0, t.length);
            for (Node<K,V> p; (p = it.advance()) != null; ) {
                s.writeObject(p.key);
                s.writeObject(p.val);
            }
        }
        s.writeObject(null);
        s.writeObject(null);
        segments = null; // 抛弃
    }

    /**
     * 从流中重建实例（即，反序列化它）。
     * @param s 流
     * @throws ClassNotFoundException 如果找不到序列化对象的类
     * @throws java.io.IOException 如果发生 I/O 错误
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        /*
         * 为了提高典型情况下的性能，我们在读取时创建节点，然后在知道大小后将其放入表中。
         * 然而，我们必须在这样做时验证唯一性并处理过度填充的 bin，这需要专门版本的 putVal 机制。
         */
        sizeCtl = -1; // 强制排除以进行表构建
        s.defaultReadObject();
        long size = 0L;
        Node<K,V> p = null;
        for (;;) {
            @SuppressWarnings("unchecked")
            K k = (K) s.readObject();
            @SuppressWarnings("unchecked")
            V v = (V) s.readObject();
            if (k != null && v != null) {
                p = new Node<K,V>(spread(k.hashCode()), k, v, p);
                ++size;
            }
            else
                break;
        }
        if (size == 0L)
            sizeCtl = 0;
        else {
            int n;
            if (size >= (long)(MAXIMUM_CAPACITY >>> 1))
                n = MAXIMUM_CAPACITY;
            else {
                int sz = (int)size;
                n = tableSizeFor(sz + (sz >>> 1) + 1);
            }
            @SuppressWarnings("unchecked")
            Node<K,V>[] tab = (Node<K,V>[])new Node<?,?>[n];
            int mask = n - 1;
            long added = 0L;
            while (p != null) {
                boolean insertAtFront;
                Node<K,V> next = p.next, first;
                int h = p.hash, j = h & mask;
                if ((first = tabAt(tab, j)) == null)
                    insertAtFront = true;
                else {
                    K k = p.key;
                    if (first.hash < 0) {
                        TreeBin<K,V> t = (TreeBin<K,V>)first;
                        if (t.putTreeVal(h, k, p.val) == null)
                            ++added;
                        insertAtFront = false;
                    }
                    else {
                        int binCount = 0;
                        insertAtFront = true;
                        Node<K,V> q; K qk;
                        for (q = first; q != null; q = q.next) {
                            if (q.hash == h &&
                                ((qk = q.key) == k ||
                                 (qk != null && k.equals(qk)))) {
                                insertAtFront = false;
                                break;
                            }
                            ++binCount;
                        }
                        if (insertAtFront && binCount >= TREEIFY_THRESHOLD) {
                            insertAtFront = false;
                            ++added;
                            p.next = first;
                            TreeNode<K,V> hd = null, tl = null;
                            for (q = p; q != null; q = q.next) {
                                TreeNode<K,V> t = new TreeNode<K,V>
                                    (q.hash, q.key, q.val, null, null);
                                if ((t.prev = tl) == null)
                                    hd = t;
                                else
                                    tl.next = t;
                                tl = t;
                            }
                            setTabAt(tab, j, new TreeBin<K,V>(hd));
                        }
                    }
                }
                if (insertAtFront) {
                    ++added;
                    p.next = first;
                    setTabAt(tab, j, p);
                }
                p = next;
            }
            table = tab;
            sizeCtl = n - (n >>> 2);
            baseCount = added;
        }
    }

                // ConcurrentMap 方法

    /**
     * {@inheritDoc}
     *
     * @return 与指定键关联的先前值，如果键没有映射，则返回 {@code null}
     * @throws NullPointerException 如果指定的键或值为 null
     */
    public V putIfAbsent(K key, V value) {
        return putVal(key, value, true);
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException 如果指定的键为 null
     */
    public boolean remove(Object key, Object value) {
        if (key == null)
            throw new NullPointerException();
        return value != null && replaceNode(key, null, value) != null;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException 如果任何参数为 null
     */
    public boolean replace(K key, V oldValue, V newValue) {
        if (key == null || oldValue == null || newValue == null)
            throw new NullPointerException();
        return replaceNode(key, newValue, oldValue) != null;
    }

    /**
     * {@inheritDoc}
     *
     * @return 与指定键关联的先前值，如果键没有映射，则返回 {@code null}
     * @throws NullPointerException 如果指定的键或值为 null
     */
    public V replace(K key, V value) {
        if (key == null || value == null)
            throw new NullPointerException();
        return replaceNode(key, value, null);
    }

    // 覆盖 JDK8+ Map 扩展方法的默认实现

    /**
     * 返回指定键映射的值，如果此映射中没有该键的映射，则返回给定的默认值。
     *
     * @param key 要返回其关联值的键
     * @param defaultValue 如果此映射中没有给定键的映射，则返回的值
     * @return 如果存在映射，则返回该键的映射；否则返回默认值
     * @throws NullPointerException 如果指定的键为 null
     */
    public V getOrDefault(Object key, V defaultValue) {
        V v;
        return (v = get(key)) == null ? defaultValue : v;
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        if (action == null) throw new NullPointerException();
        Node<K,V>[] t;
        if ((t = table) != null) {
            Traverser<K,V> it = new Traverser<K,V>(t, t.length, 0, t.length);
            for (Node<K,V> p; (p = it.advance()) != null; ) {
                action.accept(p.key, p.val);
            }
        }
    }

    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        if (function == null) throw new NullPointerException();
        Node<K,V>[] t;
        if ((t = table) != null) {
            Traverser<K,V> it = new Traverser<K,V>(t, t.length, 0, t.length);
            for (Node<K,V> p; (p = it.advance()) != null; ) {
                V oldValue = p.val;
                for (K key = p.key;;) {
                    V newValue = function.apply(key, oldValue);
                    if (newValue == null)
                        throw new NullPointerException();
                    if (replaceNode(key, newValue, oldValue) != null ||
                        (oldValue = get(key)) == null)
                        break;
                }
            }
        }
    }

    /**
     * 如果指定的键尚未与值关联，则尝试使用给定的映射函数计算其值，并将其输入此映射，除非计算结果为 {@code null}。整个方法调用是原子性的，因此每个键最多应用一次函数。其他线程对本映射的某些更新操作可能在计算进行时被阻塞，因此计算应简短且简单，且不得尝试更新此映射的其他映射。
     *
     * @param key 要与指定值关联的键
     * @param mappingFunction 用于计算值的函数
     * @return 与指定键关联的当前（现有或计算出的）值，如果计算出的值为 null，则返回 null
     * @throws NullPointerException 如果指定的键或 mappingFunction 为 null
     * @throws IllegalStateException 如果计算检测到尝试递归更新此映射，而该更新将永远不会完成
     * @throws RuntimeException 或 Error 如果 mappingFunction 抛出，此时映射将不被建立
     */
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        if (key == null || mappingFunction == null)
            throw new NullPointerException();
        int h = spread(key.hashCode());
        V val = null;
        int binCount = 0;
        for (Node<K,V>[] tab = table;;) {
            Node<K,V> f; int n, i, fh;
            if (tab == null || (n = tab.length) == 0)
                tab = initTable();
            else if ((f = tabAt(tab, i = (n - 1) & h)) == null) {
                Node<K,V> r = new ReservationNode<K,V>();
                synchronized (r) {
                    if (casTabAt(tab, i, null, r)) {
                        binCount = 1;
                        Node<K,V> node = null;
                        try {
                            if ((val = mappingFunction.apply(key)) != null)
                                node = new Node<K,V>(h, key, val, null);
                        } finally {
                            setTabAt(tab, i, node);
                        }
                    }
                }
                if (binCount != 0)
                    break;
            }
            else if ((fh = f.hash) == MOVED)
                tab = helpTransfer(tab, f);
            else {
                boolean added = false;
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        if (fh >= 0) {
                            binCount = 1;
                            for (Node<K,V> e = f;; ++binCount) {
                                K ek; V ev;
                                if (e.hash == h &&
                                    ((ek = e.key) == key ||
                                     (ek != null && key.equals(ek)))) {
                                    val = e.val;
                                    break;
                                }
                                Node<K,V> pred = e;
                                if ((e = e.next) == null) {
                                    if ((val = mappingFunction.apply(key)) != null) {
                                        added = true;
                                        pred.next = new Node<K,V>(h, key, val, null);
                                    }
                                    break;
                                }
                            }
                        }
                        else if (f instanceof TreeBin) {
                            binCount = 2;
                            TreeBin<K,V> t = (TreeBin<K,V>)f;
                            TreeNode<K,V> r, p;
                            if ((r = t.root) != null &&
                                (p = r.findTreeNode(h, key, null)) != null)
                                val = p.val;
                            else if ((val = mappingFunction.apply(key)) != null) {
                                added = true;
                                t.putTreeVal(h, key, val);
                            }
                        }
                    }
                }
                if (binCount != 0) {
                    if (binCount >= TREEIFY_THRESHOLD)
                        treeifyBin(tab, i);
                    if (!added)
                        return val;
                    break;
                }
            }
        }
        if (val != null)
            addCount(1L, binCount);
        return val;
    }


                /**
     * 如果指定键的值存在，则尝试根据键及其当前映射值计算新的映射。整个方法调用是原子性的。
     * 在计算过程中，其他线程对本映射表的某些更新操作可能会被阻塞，因此计算应该简短且简单，且不能尝试更新此映射表的其他映射。
     *
     * @param key 可能与值关联的键
     * @param remappingFunction 计算值的函数
     * @return 与指定键关联的新值，如果没有则返回null
     * @throws NullPointerException 如果指定的键或remappingFunction为null
     * @throws IllegalStateException 如果计算检测到尝试对本映射表进行递归更新，否则永远不会完成
     * @throws RuntimeException 或 Error 如果remappingFunction抛出异常，则映射保持不变
     */
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (key == null || remappingFunction == null)
            throw new NullPointerException();
        int h = spread(key.hashCode());
        V val = null;
        int delta = 0;
        int binCount = 0;
        for (Node<K,V>[] tab = table;;) {
            Node<K,V> f; int n, i, fh;
            if (tab == null || (n = tab.length) == 0)
                tab = initTable();
            else if ((f = tabAt(tab, i = (n - 1) & h)) == null)
                break;
            else if ((fh = f.hash) == MOVED)
                tab = helpTransfer(tab, f);
            else {
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        if (fh >= 0) {
                            binCount = 1;
                            for (Node<K,V> e = f, pred = null;; ++binCount) {
                                K ek;
                                if (e.hash == h &&
                                    ((ek = e.key) == key ||
                                     (ek != null && key.equals(ek)))) {
                                    val = remappingFunction.apply(key, e.val);
                                    if (val != null)
                                        e.val = val;
                                    else {
                                        delta = -1;
                                        Node<K,V> en = e.next;
                                        if (pred != null)
                                            pred.next = en;
                                        else
                                            setTabAt(tab, i, en);
                                    }
                                    break;
                                }
                                pred = e;
                                if ((e = e.next) == null)
                                    break;
                            }
                        }
                        else if (f instanceof TreeBin) {
                            binCount = 2;
                            TreeBin<K,V> t = (TreeBin<K,V>)f;
                            TreeNode<K,V> r, p;
                            if ((r = t.root) != null &&
                                (p = r.findTreeNode(h, key, null)) != null) {
                                val = remappingFunction.apply(key, p.val);
                                if (val != null)
                                    p.val = val;
                                else {
                                    delta = -1;
                                    if (t.removeTreeNode(p))
                                        setTabAt(tab, i, untreeify(t.first));
                                }
                            }
                        }
                    }
                }
                if (binCount != 0)
                    break;
            }
        }
        if (delta != 0)
            addCount((long)delta, binCount);
        return val;
    }

    /**
     * 尝试为指定键及其当前映射值（如果当前没有映射，则为{@code null}）计算映射。整个方法调用是原子性的。
     * 在计算过程中，其他线程对本映射表的某些更新操作可能会被阻塞，因此计算应该简短且简单，且不能尝试更新此映射表的其他映射。
     *
     * @param key 要与指定值关联的键
     * @param remappingFunction 计算值的函数
     * @return 与指定键关联的新值，如果没有则返回null
     * @throws NullPointerException 如果指定的键或remappingFunction为null
     * @throws IllegalStateException 如果计算检测到尝试对本映射表进行递归更新，否则永远不会完成
     * @throws RuntimeException 或 Error 如果remappingFunction抛出异常，则映射保持不变
     */
    public V compute(K key,
                     BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (key == null || remappingFunction == null)
            throw new NullPointerException();
        int h = spread(key.hashCode());
        V val = null;
        int delta = 0;
        int binCount = 0;
        for (Node<K,V>[] tab = table;;) {
            Node<K,V> f; int n, i, fh;
            if (tab == null || (n = tab.length) == 0)
                tab = initTable();
            else if ((f = tabAt(tab, i = (n - 1) & h)) == null) {
                Node<K,V> r = new ReservationNode<K,V>();
                synchronized (r) {
                    if (casTabAt(tab, i, null, r)) {
                        binCount = 1;
                        Node<K,V> node = null;
                        try {
                            if ((val = remappingFunction.apply(key, null)) != null) {
                                delta = 1;
                                node = new Node<K,V>(h, key, val, null);
                            }
                        } finally {
                            setTabAt(tab, i, node);
                        }
                    }
                }
                if (binCount != 0)
                    break;
            }
            else if ((fh = f.hash) == MOVED)
                tab = helpTransfer(tab, f);
            else {
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        if (fh >= 0) {
                            binCount = 1;
                            for (Node<K,V> e = f, pred = null;; ++binCount) {
                                K ek;
                                if (e.hash == h &&
                                    ((ek = e.key) == key ||
                                     (ek != null && key.equals(ek)))) {
                                    val = remappingFunction.apply(key, e.val);
                                    if (val != null)
                                        e.val = val;
                                    else {
                                        delta = -1;
                                        Node<K,V> en = e.next;
                                        if (pred != null)
                                            pred.next = en;
                                        else
                                            setTabAt(tab, i, en);
                                    }
                                    break;
                                }
                                pred = e;
                                if ((e = e.next) == null) {
                                    val = remappingFunction.apply(key, null);
                                    if (val != null) {
                                        delta = 1;
                                        pred.next =
                                            new Node<K,V>(h, key, val, null);
                                    }
                                    break;
                                }
                            }
                        }
                        else if (f instanceof TreeBin) {
                            binCount = 1;
                            TreeBin<K,V> t = (TreeBin<K,V>)f;
                            TreeNode<K,V> r, p;
                            if ((r = t.root) != null)
                                p = r.findTreeNode(h, key, null);
                            else
                                p = null;
                            V pv = (p == null) ? null : p.val;
                            val = remappingFunction.apply(key, pv);
                            if (val != null) {
                                if (p != null)
                                    p.val = val;
                                else {
                                    delta = 1;
                                    t.putTreeVal(h, key, val);
                                }
                            }
                            else if (p != null) {
                                delta = -1;
                                if (t.removeTreeNode(p))
                                    setTabAt(tab, i, untreeify(t.first));
                            }
                        }
                    }
                }
                if (binCount != 0) {
                    if (binCount >= TREEIFY_THRESHOLD)
                        treeifyBin(tab, i);
                    break;
                }
            }
        }
        if (delta != 0)
            addCount((long)delta, binCount);
        return val;
    }


    /**
     * 如果指定的键尚未与任何（非空）值关联，则将其与给定值关联。
     * 否则，使用给定的重新映射函数的结果替换该值，如果结果为 {@code null} 则移除该键值对。
     * 整个方法调用是原子性的。在计算过程中，其他线程对本映射的某些更新操作可能会被阻塞，
     * 因此计算应简短且简单，并且不得尝试更新此映射的任何其他映射。
     *
     * @param key 与指定值关联的键
     * @param value 如果键不存在时使用的值
     * @param remappingFunction 用于重新计算值的函数，如果键已存在
     * @return 与指定键关联的新值，如果没有则返回 null
     * @throws NullPointerException 如果指定的键或 remappingFunction 为 null
     * @throws RuntimeException 或 Error 如果 remappingFunction 抛出异常，
     *         在这种情况下，映射保持不变
     */
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        if (key == null || value == null || remappingFunction == null)
            throw new NullPointerException();
        int h = spread(key.hashCode());
        V val = null;
        int delta = 0;
        int binCount = 0;
        for (Node<K,V>[] tab = table;;) {
            Node<K,V> f; int n, i, fh;
            if (tab == null || (n = tab.length) == 0)
                tab = initTable();
            else if ((f = tabAt(tab, i = (n - 1) & h)) == null) {
                if (casTabAt(tab, i, null, new Node<K,V>(h, key, value, null))) {
                    delta = 1;
                    val = value;
                    break;
                }
            }
            else if ((fh = f.hash) == MOVED)
                tab = helpTransfer(tab, f);
            else {
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        if (fh >= 0) {
                            binCount = 1;
                            for (Node<K,V> e = f, pred = null;; ++binCount) {
                                K ek;
                                if (e.hash == h &&
                                    ((ek = e.key) == key ||
                                     (ek != null && key.equals(ek)))) {
                                    val = remappingFunction.apply(e.val, value);
                                    if (val != null)
                                        e.val = val;
                                    else {
                                        delta = -1;
                                        Node<K,V> en = e.next;
                                        if (pred != null)
                                            pred.next = en;
                                        else
                                            setTabAt(tab, i, en);
                                    }
                                    break;
                                }
                                pred = e;
                                if ((e = e.next) == null) {
                                    delta = 1;
                                    val = value;
                                    pred.next =
                                        new Node<K,V>(h, key, val, null);
                                    break;
                                }
                            }
                        }
                        else if (f instanceof TreeBin) {
                            binCount = 2;
                            TreeBin<K,V> t = (TreeBin<K,V>)f;
                            TreeNode<K,V> r = t.root;
                            TreeNode<K,V> p = (r == null) ? null :
                                r.findTreeNode(h, key, null);
                            val = (p == null) ? value :
                                remappingFunction.apply(p.val, value);
                            if (val != null) {
                                if (p != null)
                                    p.val = val;
                                else {
                                    delta = 1;
                                    t.putTreeVal(h, key, val);
                                }
                            }
                            else if (p != null) {
                                delta = -1;
                                if (t.removeTreeNode(p))
                                    setTabAt(tab, i, untreeify(t.first));
                            }
                        }
                    }
                }
                if (binCount != 0) {
                    if (binCount >= TREEIFY_THRESHOLD)
                        treeifyBin(tab, i);
                    break;
                }
            }
        }
        if (delta != 0)
            addCount((long)delta, binCount);
        return val;
    }

    // Hashtable 遗留方法

    /**
     * 用于测试此表中是否有一些键映射到指定的值。此方法在功能上与
     * {@link #containsValue(Object)} 完全相同，仅为了确保与
     * 类 {@link java.util.Hashtable} 的完全兼容性而存在，后者在引入
     * Java Collections 框架之前支持此方法。
     *
     * @param  value 要搜索的值
     * @return 如果且仅当此表中存在某个键映射到 {@code value} 参数，根据
     *         {@code equals} 方法确定，则返回 {@code true}；
     *         否则返回 {@code false}
     * @throws NullPointerException 如果指定的值为 null
     */
    public boolean contains(Object value) {
        return containsValue(value);
    }

    /**
     * 返回此表中键的枚举。
     *
     * @return 此表中键的枚举
     * @see #keySet()
     */
    public Enumeration<K> keys() {
        Node<K,V>[] t;
        int f = (t = table) == null ? 0 : t.length;
        return new KeyIterator<K,V>(t, f, 0, f, this);
    }


                /**
     * 返回此表中的值的枚举。
     *
     * @return 此表中的值的枚举
     * @see #values()
     */
    public Enumeration<V> elements() {
        Node<K,V>[] t;
        int f = (t = table) == null ? 0 : t.length;
        return new ValueIterator<K,V>(t, f, 0, f, this);
    }

    // 仅限 ConcurrentHashMap 的方法

    /**
     * 返回映射的数量。此方法应替代 {@link #size} 使用，因为 ConcurrentHashMap 可能包含比 int 更多的映射。
     * 返回的值是一个估计值；如果存在并发插入或删除，实际计数可能会有所不同。
     *
     * @return 映射的数量
     * @since 1.8
     */
    public long mappingCount() {
        long n = sumCount();
        return (n < 0L) ? 0L : n; // 忽略瞬时负值
    }

    /**
     * 创建一个由 ConcurrentHashMap 支持的新 {@link Set}，从给定类型到 {@code Boolean.TRUE}。
     *
     * @param <K> 返回的集合的元素类型
     * @return 新的集合
     * @since 1.8
     */
    public static <K> KeySetView<K,Boolean> newKeySet() {
        return new KeySetView<K,Boolean>
            (new ConcurrentHashMap<K,Boolean>(), Boolean.TRUE);
    }

    /**
     * 创建一个由 ConcurrentHashMap 支持的新 {@link Set}，从给定类型到 {@code Boolean.TRUE}。
     *
     * @param initialCapacity 实现执行内部调整大小以容纳此数量的元素。
     * @param <K> 返回的集合的元素类型
     * @return 新的集合
     * @throws IllegalArgumentException 如果元素的初始容量为负
     * @since 1.8
     */
    public static <K> KeySetView<K,Boolean> newKeySet(int initialCapacity) {
        return new KeySetView<K,Boolean>
            (new ConcurrentHashMap<K,Boolean>(initialCapacity), Boolean.TRUE);
    }

    /**
     * 返回此映射中键的 {@link Set} 视图，使用给定的通用映射值进行任何添加（即 {@link
     * Collection#add} 和 {@link Collection#addAll(Collection)}）。
     * 当然，只有在可以接受从该视图添加的所有元素使用相同的值时，才适合使用此方法。
     *
     * @param mappedValue 用于任何添加的映射值
     * @return 集合视图
     * @throws NullPointerException 如果 mappedValue 为 null
     */
    public KeySetView<K,V> keySet(V mappedValue) {
        if (mappedValue == null)
            throw new NullPointerException();
        return new KeySetView<K,V>(this, mappedValue);
    }

    /* ---------------- 特殊节点 -------------- */

    /**
     * 在转移操作期间插入到 bin 头部的节点。
     */
    static final class ForwardingNode<K,V> extends Node<K,V> {
        final Node<K,V>[] nextTable;
        ForwardingNode(Node<K,V>[] tab) {
            super(MOVED, null, null, null);
            this.nextTable = tab;
        }

        Node<K,V> find(int h, Object k) {
            // 循环以避免在转发节点上进行任意深度的递归
            outer: for (Node<K,V>[] tab = nextTable;;) {
                Node<K,V> e; int n;
                if (k == null || tab == null || (n = tab.length) == 0 ||
                    (e = tabAt(tab, (n - 1) & h)) == null)
                    return null;
                for (;;) {
                    int eh; K ek;
                    if ((eh = e.hash) == h &&
                        ((ek = e.key) == k || (ek != null && k.equals(ek))))
                        return e;
                    if (eh < 0) {
                        if (e instanceof ForwardingNode) {
                            tab = ((ForwardingNode<K,V>)e).nextTable;
                            continue outer;
                        }
                        else
                            return e.find(h, k);
                    }
                    if ((e = e.next) == null)
                        return null;
                }
            }
        }
    }

    /**
     * 用于 computeIfAbsent 和 compute 的占位节点。
     */
    static final class ReservationNode<K,V> extends Node<K,V> {
        ReservationNode() {
            super(RESERVED, null, null, null);
        }

        Node<K,V> find(int h, Object k) {
            return null;
        }
    }

    /* ---------------- 表初始化和调整大小 -------------- */

    /**
     * 返回大小为 n 的表调整大小的戳位。必须在左移 RESIZE_STAMP_SHIFT 时为负。
     */
    static final int resizeStamp(int n) {
        return Integer.numberOfLeadingZeros(n) | (1 << (RESIZE_STAMP_BITS - 1));
    }

    /**
     * 使用 sizeCtl 中记录的大小初始化表。
     */
    private final Node<K,V>[] initTable() {
        Node<K,V>[] tab; int sc;
        while ((tab = table) == null || tab.length == 0) {
            if ((sc = sizeCtl) < 0)
                Thread.yield(); // 输掉了初始化竞赛；只是自旋
            else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
                try {
                    if ((tab = table) == null || tab.length == 0) {
                        int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
                        @SuppressWarnings("unchecked")
                        Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                        table = tab = nt;
                        sc = n - (n >>> 2);
                    }
                } finally {
                    sizeCtl = sc;
                }
                break;
            }
        }
        return tab;
    }

    /**
     * 增加计数，如果表太小且尚未调整大小，则启动转移。如果已经在调整大小，则在有工作可做时帮助执行转移。
     * 转移后重新检查占用情况，以查看是否已经需要再次调整大小，因为调整大小可能落后于添加。
     *
     * @param x 要添加的计数
     * @param check 如果 <0，不检查调整大小，如果 <= 1 仅在无竞争时检查
     */
    private final void addCount(long x, int check) {
        CounterCell[] as; long b, s;
        if ((as = counterCells) != null ||
            !U.compareAndSwapLong(this, BASECOUNT, b = baseCount, s = b + x)) {
            CounterCell a; long v; int m;
            boolean uncontended = true;
            if (as == null || (m = as.length - 1) < 0 ||
                (a = as[ThreadLocalRandom.getProbe() & m]) == null ||
                !(uncontended =
                  U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))) {
                fullAddCount(x, uncontended);
                return;
            }
            if (check <= 1)
                return;
            s = sumCount();
        }
        if (check >= 0) {
            Node<K,V>[] tab, nt; int n, sc;
            while (s >= (long)(sc = sizeCtl) && (tab = table) != null &&
                   (n = tab.length) < MAXIMUM_CAPACITY) {
                int rs = resizeStamp(n);
                if (sc < 0) {
                    if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                        sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||
                        transferIndex <= 0)
                        break;
                    if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
                        transfer(tab, nt);
                }
                else if (U.compareAndSwapInt(this, SIZECTL, sc,
                                             (rs << RESIZE_STAMP_SHIFT) + 2))
                    transfer(tab, null);
                s = sumCount();
            }
        }
    }


                /**
     * 帮助转移，如果正在进行扩容。
     */
    final Node<K,V>[] helpTransfer(Node<K,V>[] tab, Node<K,V> f) {
        Node<K,V>[] nextTab; int sc;
        if (tab != null && (f instanceof ForwardingNode) &&
            (nextTab = ((ForwardingNode<K,V>)f).nextTable) != null) {
            int rs = resizeStamp(tab.length);
            while (nextTab == nextTable && table == tab &&
                   (sc = sizeCtl) < 0) {
                if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                    sc == rs + MAX_RESIZERS || transferIndex <= 0)
                    break;
                if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1)) {
                    transfer(tab, nextTab);
                    break;
                }
            }
            return nextTab;
        }
        return table;
    }

    /**
     * 尝试预设表的大小以容纳给定数量的元素。
     *
     * @param size 元素数量（不需要完全准确）
     */
    private final void tryPresize(int size) {
        int c = (size >= (MAXIMUM_CAPACITY >>> 1)) ? MAXIMUM_CAPACITY :
            tableSizeFor(size + (size >>> 1) + 1);
        int sc;
        while ((sc = sizeCtl) >= 0) {
            Node<K,V>[] tab = table; int n;
            if (tab == null || (n = tab.length) == 0) {
                n = (sc > c) ? sc : c;
                if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
                    try {
                        if (table == tab) {
                            @SuppressWarnings("unchecked")
                            Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                            table = nt;
                            sc = n - (n >>> 2);
                        }
                    } finally {
                        sizeCtl = sc;
                    }
                }
            }
            else if (c <= sc || n >= MAXIMUM_CAPACITY)
                break;
            else if (tab == table) {
                int rs = resizeStamp(n);
                if (sc < 0) {
                    Node<K,V>[] nt;
                    if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                        sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||
                        transferIndex <= 0)
                        break;
                    if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
                        transfer(tab, nt);
                }
                else if (U.compareAndSwapInt(this, SIZECTL, sc,
                                             (rs << RESIZE_STAMP_SHIFT) + 2))
                    transfer(tab, null);
            }
        }
    }

    /**
     * 将每个桶中的节点移动和/或复制到新表中。参见上方的解释。
     */
    private final void transfer(Node<K,V>[] tab, Node<K,V>[] nextTab) {
        int n = tab.length, stride;
        if ((stride = (NCPU > 1) ? (n >>> 3) / NCPU : n) < MIN_TRANSFER_STRIDE)
            stride = MIN_TRANSFER_STRIDE; // 划分范围
        if (nextTab == null) {            // 初始化
            try {
                @SuppressWarnings("unchecked")
                Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n << 1];
                nextTab = nt;
            } catch (Throwable ex) {      // 尝试处理 OOME
                sizeCtl = Integer.MAX_VALUE;
                return;
            }
            nextTable = nextTab;
            transferIndex = n;
        }
        int nextn = nextTab.length;
        ForwardingNode<K,V> fwd = new ForwardingNode<K,V>(nextTab);
        boolean advance = true;
        boolean finishing = false; // 确保在提交 nextTab 之前完成扫描
        for (int i = 0, bound = 0;;) {
            Node<K,V> f; int fh;
            while (advance) {
                int nextIndex, nextBound;
                if (--i >= bound || finishing)
                    advance = false;
                else if ((nextIndex = transferIndex) <= 0) {
                    i = -1;
                    advance = false;
                }
                else if (U.compareAndSwapInt
                         (this, TRANSFERINDEX, nextIndex,
                          nextBound = (nextIndex > stride ?
                                       nextIndex - stride : 0))) {
                    bound = nextBound;
                    i = nextIndex - 1;
                    advance = false;
                }
            }
            if (i < 0 || i >= n || i + n >= nextn) {
                int sc;
                if (finishing) {
                    nextTable = null;
                    table = nextTab;
                    sizeCtl = (n << 1) - (n >>> 1);
                    return;
                }
                if (U.compareAndSwapInt(this, SIZECTL, sc = sizeCtl, sc - 1)) {
                    if ((sc - 2) != resizeStamp(n) << RESIZE_STAMP_SHIFT)
                        return;
                    finishing = advance = true;
                    i = n; // 在提交前重新检查
                }
            }
            else if ((f = tabAt(tab, i)) == null)
                advance = casTabAt(tab, i, null, fwd);
            else if ((fh = f.hash) == MOVED)
                advance = true; // 已经处理过
            else {
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        Node<K,V> ln, hn;
                        if (fh >= 0) {
                            int runBit = fh & n;
                            Node<K,V> lastRun = f;
                            for (Node<K,V> p = f.next; p != null; p = p.next) {
                                int b = p.hash & n;
                                if (b != runBit) {
                                    runBit = b;
                                    lastRun = p;
                                }
                            }
                            if (runBit == 0) {
                                ln = lastRun;
                                hn = null;
                            }
                            else {
                                hn = lastRun;
                                ln = null;
                            }
                            for (Node<K,V> p = f; p != lastRun; p = p.next) {
                                int ph = p.hash; K pk = p.key; V pv = p.val;
                                if ((ph & n) == 0)
                                    ln = new Node<K,V>(ph, pk, pv, ln);
                                else
                                    hn = new Node<K,V>(ph, pk, pv, hn);
                            }
                            setTabAt(nextTab, i, ln);
                            setTabAt(nextTab, i + n, hn);
                            setTabAt(tab, i, fwd);
                            advance = true;
                        }
                        else if (f instanceof TreeBin) {
                            TreeBin<K,V> t = (TreeBin<K,V>)f;
                            TreeNode<K,V> lo = null, loTail = null;
                            TreeNode<K,V> hi = null, hiTail = null;
                            int lc = 0, hc = 0;
                            for (Node<K,V> e = t.first; e != null; e = e.next) {
                                int h = e.hash;
                                TreeNode<K,V> p = new TreeNode<K,V>
                                    (h, e.key, e.val, null, null);
                                if ((h & n) == 0) {
                                    if ((p.prev = loTail) == null)
                                        lo = p;
                                    else
                                        loTail.next = p;
                                    loTail = p;
                                    ++lc;
                                }
                                else {
                                    if ((p.prev = hiTail) == null)
                                        hi = p;
                                    else
                                        hiTail.next = p;
                                    hiTail = p;
                                    ++hc;
                                }
                            }
                            ln = (lc <= UNTREEIFY_THRESHOLD) ? untreeify(lo) :
                                (hc != 0) ? new TreeBin<K,V>(lo) : t;
                            hn = (hc <= UNTREEIFY_THRESHOLD) ? untreeify(hi) :
                                (lc != 0) ? new TreeBin<K,V>(hi) : t;
                            setTabAt(nextTab, i, ln);
                            setTabAt(nextTab, i + n, hn);
                            setTabAt(tab, i, fwd);
                            advance = true;
                        }
                    }
                }
            }
        }
    }

                /* ---------------- Counter support -------------- */

    /**
     * 一个用于分发计数的填充单元。改编自 LongAdder 和 Striped64。请参阅它们的内部文档以获取解释。
     */
    @sun.misc.Contended static final class CounterCell {
        volatile long value;
        CounterCell(long x) { value = x; }
    }

    final long sumCount() {
        CounterCell[] as = counterCells; CounterCell a;
        long sum = baseCount;
        if (as != null) {
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null)
                    sum += a.value;
            }
        }
        return sum;
    }

    // 请参阅 LongAdder 版本以获取解释
    private final void fullAddCount(long x, boolean wasUncontended) {
        int h;
        if ((h = ThreadLocalRandom.getProbe()) == 0) {
            ThreadLocalRandom.localInit();      // 强制初始化
            h = ThreadLocalRandom.getProbe();
            wasUncontended = true;
        }
        boolean collide = false;                // 如果最后一个槽非空，则为真
        for (;;) {
            CounterCell[] as; CounterCell a; int n; long v;
            if ((as = counterCells) != null && (n = as.length) > 0) {
                if ((a = as[(n - 1) & h]) == null) {
                    if (cellsBusy == 0) {            // 尝试附加新单元
                        CounterCell r = new CounterCell(x); // 乐观创建
                        if (cellsBusy == 0 &&
                            U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
                            boolean created = false;
                            try {               // 在锁下重新检查
                                CounterCell[] rs; int m, j;
                                if ((rs = counterCells) != null &&
                                    (m = rs.length) > 0 &&
                                    rs[j = (m - 1) & h] == null) {
                                    rs[j] = r;
                                    created = true;
                                }
                            } finally {
                                cellsBusy = 0;
                            }
                            if (created)
                                break;
                            continue;           // 槽现在非空
                        }
                    }
                    collide = false;
                }
                else if (!wasUncontended)       // CAS 已知失败
                    wasUncontended = true;      // 重新哈希后继续
                else if (U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))
                    break;
                else if (counterCells != as || n >= NCPU)
                    collide = false;            // 达到最大大小或陈旧
                else if (!collide)
                    collide = true;
                else if (cellsBusy == 0 &&
                         U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
                    try {
                        if (counterCells == as) {// 除非陈旧，否则扩展表
                            CounterCell[] rs = new CounterCell[n << 1];
                            for (int i = 0; i < n; ++i)
                                rs[i] = as[i];
                            counterCells = rs;
                        }
                    } finally {
                        cellsBusy = 0;
                    }
                    collide = false;
                    continue;                   // 使用扩展表重试
                }
                h = ThreadLocalRandom.advanceProbe(h);
            }
            else if (cellsBusy == 0 && counterCells == as &&
                     U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
                boolean init = false;
                try {                           // 初始化表
                    if (counterCells == as) {
                        CounterCell[] rs = new CounterCell[2];
                        rs[h & 1] = new CounterCell(x);
                        counterCells = rs;
                        init = true;
                    }
                } finally {
                    cellsBusy = 0;
                }
                if (init)
                    break;
            }
            else if (U.compareAndSwapLong(this, BASECOUNT, v = baseCount, v + x))
                break;                          // 转而使用基础
        }
    }

    /* ---------------- Conversion from/to TreeBins -------------- */

    /**
     * 除非表太小，在这种情况下会重新调整大小，否则用树节点替换给定索引处的所有链接节点。
     */
    private final void treeifyBin(Node<K,V>[] tab, int index) {
        Node<K,V> b; int n, sc;
        if (tab != null) {
            if ((n = tab.length) < MIN_TREEIFY_CAPACITY)
                tryPresize(n << 1);
            else if ((b = tabAt(tab, index)) != null && b.hash >= 0) {
                synchronized (b) {
                    if (tabAt(tab, index) == b) {
                        TreeNode<K,V> hd = null, tl = null;
                        for (Node<K,V> e = b; e != null; e = e.next) {
                            TreeNode<K,V> p =
                                new TreeNode<K,V>(e.hash, e.key, e.val,
                                                  null, null);
                            if ((p.prev = tl) == null)
                                hd = p;
                            else
                                tl.next = p;
                            tl = p;
                        }
                        setTabAt(tab, index, new TreeBin<K,V>(hd));
                    }
                }
            }
        }
    }

    /**
     * 返回一个非树节点列表，替换给定列表中的那些。
     */
    static <K,V> Node<K,V> untreeify(Node<K,V> b) {
        Node<K,V> hd = null, tl = null;
        for (Node<K,V> q = b; q != null; q = q.next) {
            Node<K,V> p = new Node<K,V>(q.hash, q.key, q.val, null);
            if (tl == null)
                hd = p;
            else
                tl.next = p;
            tl = p;
        }
        return hd;
    }


                /* ---------------- TreeNodes -------------- */

    /**
     * 用于 TreeBins 的节点
     */
    static final class TreeNode<K,V> extends Node<K,V> {
        TreeNode<K,V> parent;  // 红黑树链接
        TreeNode<K,V> left;
        TreeNode<K,V> right;
        TreeNode<K,V> prev;    // 删除时需要解除下一个节点的链接
        boolean red;

        TreeNode(int hash, K key, V val, Node<K,V> next,
                 TreeNode<K,V> parent) {
            super(hash, key, val, next);
            this.parent = parent;
        }

        Node<K,V> find(int h, Object k) {
            return findTreeNode(h, k, null);
        }

        /**
         * 从给定的根节点开始，返回给定键的 TreeNode（如果未找到则返回 null）
         */
        final TreeNode<K,V> findTreeNode(int h, Object k, Class<?> kc) {
            if (k != null) {
                TreeNode<K,V> p = this;
                do  {
                    int ph, dir; K pk; TreeNode<K,V> q;
                    TreeNode<K,V> pl = p.left, pr = p.right;
                    if ((ph = p.hash) > h)
                        p = pl;
                    else if (ph < h)
                        p = pr;
                    else if ((pk = p.key) == k || (pk != null && k.equals(pk)))
                        return p;
                    else if (pl == null)
                        p = pr;
                    else if (pr == null)
                        p = pl;
                    else if ((kc != null ||
                              (kc = comparableClassFor(k)) != null) &&
                             (dir = compareComparables(kc, k, pk)) != 0)
                        p = (dir < 0) ? pl : pr;
                    else if ((q = pr.findTreeNode(h, k, kc)) != null)
                        return q;
                    else
                        p = pl;
                } while (p != null);
            }
            return null;
        }
    }

    /* ---------------- TreeBins -------------- */

    /**
     * 用于 bin 头部的 TreeNodes。TreeBins 不持有用户键或值，而是指向 TreeNode 列表和根节点。
     * 它们还维护一个寄生读写锁，迫使写入者（持有 bin 锁）等待读取者（不持有锁）完成后再进行树结构调整操作。
     */
    static final class TreeBin<K,V> extends Node<K,V> {
        TreeNode<K,V> root;
        volatile TreeNode<K,V> first;
        volatile Thread waiter;
        volatile int lockState;
        // lockState 的值
        static final int WRITER = 1; // 持有写锁时设置
        static final int WAITER = 2; // 等待写锁时设置
        static final int READER = 4; // 设置读锁的增量值

        /**
         * 当 hashCode 相等且不可比较时，用于插入排序的打破平局工具。
         * 我们不需要一个全序，只需要一个在重新平衡时保持等价的规则。
         * 进一步的打破平局简化了测试。
         */
        static int tieBreakOrder(Object a, Object b) {
            int d;
            if (a == null || b == null ||
                (d = a.getClass().getName().
                 compareTo(b.getClass().getName())) == 0)
                d = (System.identityHashCode(a) <= System.identityHashCode(b) ?
                     -1 : 1);
            return d;
        }

        /**
         * 使用初始节点集 b 创建 bin。
         */
        TreeBin(TreeNode<K,V> b) {
            super(TREEBIN, null, null, null);
            this.first = b;
            TreeNode<K,V> r = null;
            for (TreeNode<K,V> x = b, next; x != null; x = next) {
                next = (TreeNode<K,V>)x.next;
                x.left = x.right = null;
                if (r == null) {
                    x.parent = null;
                    x.red = false;
                    r = x;
                }
                else {
                    K k = x.key;
                    int h = x.hash;
                    Class<?> kc = null;
                    for (TreeNode<K,V> p = r;;) {
                        int dir, ph;
                        K pk = p.key;
                        if ((ph = p.hash) > h)
                            dir = -1;
                        else if (ph < h)
                            dir = 1;
                        else if ((kc == null &&
                                  (kc = comparableClassFor(k)) == null) ||
                                 (dir = compareComparables(kc, k, pk)) == 0)
                            dir = tieBreakOrder(k, pk);
                            TreeNode<K,V> xp = p;
                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
                            x.parent = xp;
                            if (dir <= 0)
                                xp.left = x;
                            else
                                xp.right = x;
                            r = balanceInsertion(r, x);
                            break;
                        }
                    }
                }
            }
            this.root = r;
            assert checkInvariants(root);
        }

        /**
         * 获取用于树结构调整的写锁。
         */
        private final void lockRoot() {
            if (!U.compareAndSwapInt(this, LOCKSTATE, 0, WRITER))
                contendedLock(); // 转移到单独的方法
        }

        /**
         * 释放用于树结构调整的写锁。
         */
        private final void unlockRoot() {
            lockState = 0;
        }

        /**
         * 可能会阻塞以等待根锁。
         */
        private final void contendedLock() {
            boolean waiting = false;
            for (int s;;) {
                if (((s = lockState) & ~WAITER) == 0) {
                    if (U.compareAndSwapInt(this, LOCKSTATE, s, WRITER)) {
                        if (waiting)
                            waiter = null;
                        return;
                    }
                }
                else if ((s & WAITER) == 0) {
                    if (U.compareAndSwapInt(this, LOCKSTATE, s, s | WAITER)) {
                        waiting = true;
                        waiter = Thread.currentThread();
                    }
                }
                else if (waiting)
                    LockSupport.park(this);
            }
        }


                    /**
         * 返回匹配的节点，如果没有则返回 null。尝试从根节点开始使用树比较进行搜索，
         * 但在锁不可用时继续线性搜索。
         */
        final Node<K,V> find(int h, Object k) {
            if (k != null) {
                for (Node<K,V> e = first; e != null; ) {
                    int s; K ek;
                    if (((s = lockState) & (WAITER|WRITER)) != 0) {
                        if (e.hash == h &&
                            ((ek = e.key) == k || (ek != null && k.equals(ek))))
                            return e;
                        e = e.next;
                    }
                    else if (U.compareAndSwapInt(this, LOCKSTATE, s,
                                                 s + READER)) {
                        TreeNode<K,V> r, p;
                        try {
                            p = ((r = root) == null ? null :
                                 r.findTreeNode(h, k, null));
                        } finally {
                            Thread w;
                            if (U.getAndAddInt(this, LOCKSTATE, -READER) ==
                                (READER|WAITER) && (w = waiter) != null)
                                LockSupport.unpark(w);
                        }
                        return p;
                    }
                }
            }
            return null;
        }

        /**
         * 查找或添加一个节点。
         * @return 如果添加成功则返回 null
         */
        final TreeNode<K,V> putTreeVal(int h, K k, V v) {
            Class<?> kc = null;
            boolean searched = false;
            for (TreeNode<K,V> p = root;;) {
                int dir, ph; K pk;
                if (p == null) {
                    first = root = new TreeNode<K,V>(h, k, v, null, null);
                    break;
                }
                else if ((ph = p.hash) > h)
                    dir = -1;
                else if (ph < h)
                    dir = 1;
                else if ((pk = p.key) == k || (pk != null && k.equals(pk)))
                    return p;
                else if ((kc == null &&
                          (kc = comparableClassFor(k)) == null) ||
                         (dir = compareComparables(kc, k, pk)) == 0) {
                    if (!searched) {
                        TreeNode<K,V> q, ch;
                        searched = true;
                        if (((ch = p.left) != null &&
                             (q = ch.findTreeNode(h, k, kc)) != null) ||
                            ((ch = p.right) != null &&
                             (q = ch.findTreeNode(h, k, kc)) != null))
                            return q;
                    }
                    dir = tieBreakOrder(k, pk);
                }

                TreeNode<K,V> xp = p;
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                    TreeNode<K,V> x, f = first;
                    first = x = new TreeNode<K,V>(h, k, v, f, xp);
                    if (f != null)
                        f.prev = x;
                    if (dir <= 0)
                        xp.left = x;
                    else
                        xp.right = x;
                    if (!xp.red)
                        x.red = true;
                    else {
                        lockRoot();
                        try {
                            root = balanceInsertion(root, x);
                        } finally {
                            unlockRoot();
                        }
                    }
                    break;
                }
            }
            assert checkInvariants(root);
            return null;
        }

        /**
         * 移除给定的节点，该节点在调用此方法前必须存在。这比典型的红黑树删除代码更复杂，
         * 因为我们不能交换内部节点的内容与通过“next”指针独立访问的叶节点的后继节点。
         * 所以我们交换树的链接。
         *
         * @return 如果现在太小，应该被取消树化，则返回 true
         */
        final boolean removeTreeNode(TreeNode<K,V> p) {
            TreeNode<K,V> next = (TreeNode<K,V>)p.next;
            TreeNode<K,V> pred = p.prev;  // 解除遍历指针链接
            TreeNode<K,V> r, rl;
            if (pred == null)
                first = next;
            else
                pred.next = next;
            if (next != null)
                next.prev = pred;
            if (first == null) {
                root = null;
                return true;
            }
            if ((r = root) == null || r.right == null || // 太小
                (rl = r.left) == null || rl.left == null)
                return true;
            lockRoot();
            try {
                TreeNode<K,V> replacement;
                TreeNode<K,V> pl = p.left;
                TreeNode<K,V> pr = p.right;
                if (pl != null && pr != null) {
                    TreeNode<K,V> s = pr, sl;
                    while ((sl = s.left) != null) // 查找后继节点
                        s = sl;
                    boolean c = s.red; s.red = p.red; p.red = c; // 交换颜色
                    TreeNode<K,V> sr = s.right;
                    TreeNode<K,V> pp = p.parent;
                    if (s == pr) { // p 是 s 的直接父节点
                        p.parent = s;
                        s.right = p;
                    }
                    else {
                        TreeNode<K,V> sp = s.parent;
                        if ((p.parent = sp) != null) {
                            if (s == sp.left)
                                sp.left = p;
                            else
                                sp.right = p;
                        }
                        if ((s.right = pr) != null)
                            pr.parent = s;
                    }
                    p.left = null;
                    if ((p.right = sr) != null)
                        sr.parent = p;
                    if ((s.left = pl) != null)
                        pl.parent = s;
                    if ((s.parent = pp) == null)
                        r = s;
                    else if (p == pp.left)
                        pp.left = s;
                    else
                        pp.right = s;
                    if (sr != null)
                        replacement = sr;
                    else
                        replacement = p;
                }
                else if (pl != null)
                    replacement = pl;
                else if (pr != null)
                    replacement = pr;
                else
                    replacement = p;
                if (replacement != p) {
                    TreeNode<K,V> pp = replacement.parent = p.parent;
                    if (pp == null)
                        r = replacement;
                    else if (p == pp.left)
                        pp.left = replacement;
                    else
                        pp.right = replacement;
                    p.left = p.right = p.parent = null;
                }


                            root = (p.red) ? r : balanceDeletion(r, replacement);

                if (p == replacement) {  // detach pointers
                    TreeNode<K,V> pp;
                    if ((pp = p.parent) != null) {
                        if (p == pp.left)
                            pp.left = null;
                        else if (p == pp.right)
                            pp.right = null;
                        p.parent = null;
                    }
                }
            } finally {
                unlockRoot();
            }
            assert checkInvariants(root);
            return false;
        }

        /* ------------------------------------------------------------ */
        // Red-black tree methods, all adapted from CLR

        static <K,V> TreeNode<K,V> rotateLeft(TreeNode<K,V> root,
                                              TreeNode<K,V> p) {
            TreeNode<K,V> r, pp, rl;
            if (p != null && (r = p.right) != null) {
                if ((rl = p.right = r.left) != null)
                    rl.parent = p;
                if ((pp = r.parent = p.parent) == null)
                    (root = r).red = false;
                else if (pp.left == p)
                    pp.left = r;
                else
                    pp.right = r;
                r.left = p;
                p.parent = r;
            }
            return root;
        }

        static <K,V> TreeNode<K,V> rotateRight(TreeNode<K,V> root,
                                               TreeNode<K,V> p) {
            TreeNode<K,V> l, pp, lr;
            if (p != null && (l = p.left) != null) {
                if ((lr = p.left = l.right) != null)
                    lr.parent = p;
                if ((pp = l.parent = p.parent) == null)
                    (root = l).red = false;
                else if (pp.right == p)
                    pp.right = l;
                else
                    pp.left = l;
                l.right = p;
                p.parent = l;
            }
            return root;
        }

        static <K,V> TreeNode<K,V> balanceInsertion(TreeNode<K,V> root,
                                                    TreeNode<K,V> x) {
            x.red = true;
            for (TreeNode<K,V> xp, xpp, xppl, xppr;;) {
                if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                else if (!xp.red || (xpp = xp.parent) == null)
                    return root;
                if (xp == (xppl = xpp.left)) {
                    if ((xppr = xpp.right) != null && xppr.red) {
                        xppr.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    }
                    else {
                        if (x == xp.right) {
                            root = rotateLeft(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateRight(root, xpp);
                            }
                        }
                    }
                }
                else {
                    if (xppl != null && xppl.red) {
                        xppl.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    }
                    else {
                        if (x == xp.left) {
                            root = rotateRight(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateLeft(root, xpp);
                            }
                        }
                    }
                }
            }
        }

        static <K,V> TreeNode<K,V> balanceDeletion(TreeNode<K,V> root,
                                                   TreeNode<K,V> x) {
            for (TreeNode<K,V> xp, xpl, xpr;;)  {
                if (x == null || x == root)
                    return root;
                else if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                else if (x.red) {
                    x.red = false;
                    return root;
                }
                else if ((xpl = xp.left) == x) {
                    if ((xpr = xp.right) != null && xpr.red) {
                        xpr.red = false;
                        xp.red = true;
                        root = rotateLeft(root, xp);
                        xpr = (xp = x.parent) == null ? null : xp.right;
                    }
                    if (xpr == null)
                        x = xp;
                    else {
                        TreeNode<K,V> sl = xpr.left, sr = xpr.right;
                        if ((sr == null || !sr.red) &&
                            (sl == null || !sl.red)) {
                            xpr.red = true;
                            x = xp;
                        }
                        else {
                            if (sr == null || !sr.red) {
                                if (sl != null)
                                    sl.red = false;
                                xpr.red = true;
                                root = rotateRight(root, xpr);
                                xpr = (xp = x.parent) == null ?
                                    null : xp.right;
                            }
                            if (xpr != null) {
                                xpr.red = (xp == null) ? false : xp.red;
                                if ((sr = xpr.right) != null)
                                    sr.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateLeft(root, xp);
                            }
                            x = root;
                        }
                    }
                }
                else { // symmetric
                    if (xpl != null && xpl.red) {
                        xpl.red = false;
                        xp.red = true;
                        root = rotateRight(root, xp);
                        xpl = (xp = x.parent) == null ? null : xp.left;
                    }
                    if (xpl == null)
                        x = xp;
                    else {
                        TreeNode<K,V> sl = xpl.left, sr = xpl.right;
                        if ((sl == null || !sl.red) &&
                            (sr == null || !sr.red)) {
                            xpl.red = true;
                            x = xp;
                        }
                        else {
                            if (sl == null || !sl.red) {
                                if (sr != null)
                                    sr.red = false;
                                xpl.red = true;
                                root = rotateLeft(root, xpl);
                                xpl = (xp = x.parent) == null ?
                                    null : xp.left;
                            }
                            if (xpl != null) {
                                xpl.red = (xp == null) ? false : xp.red;
                                if ((sl = xpl.left) != null)
                                    sl.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateRight(root, xp);
                            }
                            x = root;
                        }
                    }
                }
            }
        }


                    /**
         * 递归不变性检查
         */
        static <K,V> boolean checkInvariants(TreeNode<K,V> t) {
            TreeNode<K,V> tp = t.parent, tl = t.left, tr = t.right,
                tb = t.prev, tn = (TreeNode<K,V>)t.next;
            if (tb != null && tb.next != t)
                return false;
            if (tn != null && tn.prev != t)
                return false;
            if (tp != null && t != tp.left && t != tp.right)
                return false;
            if (tl != null && (tl.parent != t || tl.hash > t.hash))
                return false;
            if (tr != null && (tr.parent != t || tr.hash < t.hash))
                return false;
            if (t.red && tl != null && tl.red && tr != null && tr.red)
                return false;
            if (tl != null && !checkInvariants(tl))
                return false;
            if (tr != null && !checkInvariants(tr))
                return false;
            return true;
        }

        private static final sun.misc.Unsafe U;
        private static final long LOCKSTATE;
        static {
            try {
                U = sun.misc.Unsafe.getUnsafe();
                Class<?> k = TreeBin.class;
                LOCKSTATE = U.objectFieldOffset
                    (k.getDeclaredField("lockState"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    /* ----------------表遍历 -------------- */

    /**
     * 记录表、其长度和当前遍历索引，以便遍历器在继续当前表之前处理转发表的区域。
     */
    static final class TableStack<K,V> {
        int length;
        int index;
        Node<K,V>[] tab;
        TableStack<K,V> next;
    }

    /**
     * 封装了诸如 containsValue 方法的遍历；也作为其他迭代器和分割器的基础类。
     *
     * 方法 advance 会依次访问在迭代器构造时可达的所有仍然有效的节点。它可能会错过在访问某个桶之后添加到该桶的节点，这在一致性保证方面是可以接受的。为了在可能的重调整大小期间维护这一属性，需要大量的簿记状态，这些状态在易失性访问中难以优化。即便如此，遍历仍然保持合理的吞吐量。
     *
     * 通常，迭代按桶进行，遍历列表。但是，如果表已重调整大小，则所有未来的步骤必须遍历当前索引处的桶以及 (index + baseSize) 处的桶；对于进一步的重调整大小也是如此。为了应对用户在多线程中共享迭代器的潜在问题，如果表读取的边界检查失败，迭代将终止。
     */
    static class Traverser<K,V> {
        Node<K,V>[] tab;        // 当前表；如果重调整大小则更新
        Node<K,V> next;         // 下一个要使用的条目
        TableStack<K,V> stack, spare; // 在 ForwardingNodes 上保存/恢复
        int index;              // 下一个要使用的桶的索引
        int baseIndex;          // 初始表的当前索引
        int baseLimit;          // 初始表的索引边界
        final int baseSize;     // 初始表大小

        Traverser(Node<K,V>[] tab, int size, int index, int limit) {
            this.tab = tab;
            this.baseSize = size;
            this.baseIndex = this.index = index;
            this.baseLimit = limit;
            this.next = null;
        }

        /**
         * 如果可能，前进并返回下一个有效的节点，如果没有则返回 null。
         */
        final Node<K,V> advance() {
            Node<K,V> e;
            if ((e = next) != null)
                e = e.next;
            for (;;) {
                Node<K,V>[] t; int i, n;  // 必须在检查中使用局部变量
                if (e != null)
                    return next = e;
                if (baseIndex >= baseLimit || (t = tab) == null ||
                    (n = t.length) <= (i = index) || i < 0)
                    return next = null;
                if ((e = tabAt(t, i)) != null && e.hash < 0) {
                    if (e instanceof ForwardingNode) {
                        tab = ((ForwardingNode<K,V>)e).nextTable;
                        e = null;
                        pushState(t, i, n);
                        continue;
                    }
                    else if (e instanceof TreeBin)
                        e = ((TreeBin<K,V>)e).first;
                    else
                        e = null;
                }
                if (stack != null)
                    recoverState(n);
                else if ((index = i + baseSize) >= n)
                    index = ++baseIndex; // 如果存在，访问上层槽
            }
        }

        /**
         * 在遇到转发节点时保存遍历状态。
         */
        private void pushState(Node<K,V>[] t, int i, int n) {
            TableStack<K,V> s = spare;  // 尽可能重用
            if (s != null)
                spare = s.next;
            else
                s = new TableStack<K,V>();
            s.tab = t;
            s.length = n;
            s.index = i;
            s.next = stack;
            stack = s;
        }

        /**
         * 可能弹出遍历状态。
         *
         * @param n 当前表的长度
         */
        private void recoverState(int n) {
            TableStack<K,V> s; int len;
            while ((s = stack) != null && (index += (len = s.length)) >= n) {
                n = len;
                index = s.index;
                tab = s.tab;
                s.tab = null;
                TableStack<K,V> next = s.next;
                s.next = spare; // 保存以备重用
                stack = next;
                spare = s;
            }
            if (s == null && (index += baseSize) >= n)
                index = ++baseIndex;
        }
    }

    /**
     * 键、值和条目迭代器的基础类。为支持 iterator.remove 添加字段。
     */
    static class BaseIterator<K,V> extends Traverser<K,V> {
        final ConcurrentHashMap<K,V> map;
        Node<K,V> lastReturned;
        BaseIterator(Node<K,V>[] tab, int size, int index, int limit,
                    ConcurrentHashMap<K,V> map) {
            super(tab, size, index, limit);
            this.map = map;
            advance();
        }


                    public final boolean hasNext() { return next != null; }
        public final boolean hasMoreElements() { return next != null; }

        public final void remove() {
            Node<K,V> p;
            if ((p = lastReturned) == null)
                throw new IllegalStateException();
            lastReturned = null;
            map.replaceNode(p.key, null, null);
        }
    }

    static final class KeyIterator<K,V> extends BaseIterator<K,V>
        implements Iterator<K>, Enumeration<K> {
        KeyIterator(Node<K,V>[] tab, int index, int size, int limit,
                    ConcurrentHashMap<K,V> map) {
            super(tab, index, size, limit, map);
        }

        public final K next() {
            Node<K,V> p;
            if ((p = next) == null)
                throw new NoSuchElementException();
            K k = p.key;
            lastReturned = p;
            advance();
            return k;
        }

        public final K nextElement() { return next(); }
    }

    static final class ValueIterator<K,V> extends BaseIterator<K,V>
        implements Iterator<V>, Enumeration<V> {
        ValueIterator(Node<K,V>[] tab, int index, int size, int limit,
                      ConcurrentHashMap<K,V> map) {
            super(tab, index, size, limit, map);
        }

        public final V next() {
            Node<K,V> p;
            if ((p = next) == null)
                throw new NoSuchElementException();
            V v = p.val;
            lastReturned = p;
            advance();
            return v;
        }

        public final V nextElement() { return next(); }
    }

    static final class EntryIterator<K,V> extends BaseIterator<K,V>
        implements Iterator<Map.Entry<K,V>> {
        EntryIterator(Node<K,V>[] tab, int index, int size, int limit,
                      ConcurrentHashMap<K,V> map) {
            super(tab, index, size, limit, map);
        }

        public final Map.Entry<K,V> next() {
            Node<K,V> p;
            if ((p = next) == null)
                throw new NoSuchElementException();
            K k = p.key;
            V v = p.val;
            lastReturned = p;
            advance();
            return new MapEntry<K,V>(k, v, map);
        }
    }

    /**
     * 导出的 Entry 用于 EntryIterator
     */
    static final class MapEntry<K,V> implements Map.Entry<K,V> {
        final K key; // 非空
        V val;       // 非空
        final ConcurrentHashMap<K,V> map;
        MapEntry(K key, V val, ConcurrentHashMap<K,V> map) {
            this.key = key;
            this.val = val;
            this.map = map;
        }
        public K getKey()        { return key; }
        public V getValue()      { return val; }
        public int hashCode()    { return key.hashCode() ^ val.hashCode(); }
        public String toString() { return key + "=" + val; }

        public boolean equals(Object o) {
            Object k, v; Map.Entry<?,?> e;
            return ((o instanceof Map.Entry) &&
                    (k = (e = (Map.Entry<?,?>)o).getKey()) != null &&
                    (v = e.getValue()) != null &&
                    (k == key || k.equals(key)) &&
                    (v == val || v.equals(val)));
        }

        /**
         * 设置我们条目的值并写入到映射中。返回的值在这里是有些任意的。由于我们不一定跟踪异步更改，最近的“先前”值可能与我们返回的不同（或者甚至可能已被移除，在这种情况下，put 将重新建立）。我们不能保证更多。
         */
        public V setValue(V value) {
            if (value == null) throw new NullPointerException();
            V v = val;
            val = value;
            map.put(key, value);
            return v;
        }
    }

    static final class KeySpliterator<K,V> extends Traverser<K,V>
        implements Spliterator<K> {
        long est;               // 大小估计
        KeySpliterator(Node<K,V>[] tab, int size, int index, int limit,
                       long est) {
            super(tab, size, index, limit);
            this.est = est;
        }

        public Spliterator<K> trySplit() {
            int i, f, h;
            return (h = ((i = baseIndex) + (f = baseLimit)) >>> 1) <= i ? null :
                new KeySpliterator<K,V>(tab, baseSize, baseLimit = h,
                                        f, est >>>= 1);
        }

        public void forEachRemaining(Consumer<? super K> action) {
            if (action == null) throw new NullPointerException();
            for (Node<K,V> p; (p = advance()) != null;)
                action.accept(p.key);
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            if (action == null) throw new NullPointerException();
            Node<K,V> p;
            if ((p = advance()) == null)
                return false;
            action.accept(p.key);
            return true;
        }

        public long estimateSize() { return est; }

        public int characteristics() {
            return Spliterator.DISTINCT | Spliterator.CONCURRENT |
                Spliterator.NONNULL;
        }
    }

    static final class ValueSpliterator<K,V> extends Traverser<K,V>
        implements Spliterator<V> {
        long est;               // 大小估计
        ValueSpliterator(Node<K,V>[] tab, int size, int index, int limit,
                         long est) {
            super(tab, size, index, limit);
            this.est = est;
        }

        public Spliterator<V> trySplit() {
            int i, f, h;
            return (h = ((i = baseIndex) + (f = baseLimit)) >>> 1) <= i ? null :
                new ValueSpliterator<K,V>(tab, baseSize, baseLimit = h,
                                          f, est >>>= 1);
        }

        public void forEachRemaining(Consumer<? super V> action) {
            if (action == null) throw new NullPointerException();
            for (Node<K,V> p; (p = advance()) != null;)
                action.accept(p.val);
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            if (action == null) throw new NullPointerException();
            Node<K,V> p;
            if ((p = advance()) == null)
                return false;
            action.accept(p.val);
            return true;
        }


                    public long estimateSize() { return est; }

        public int characteristics() {
            return Spliterator.CONCURRENT | Spliterator.NONNULL;
        }
    }

    static final class EntrySpliterator<K,V> extends Traverser<K,V>
        implements Spliterator<Map.Entry<K,V>> {
        final ConcurrentHashMap<K,V> map; // 用于导出 MapEntry
        long est;               // 大小估计
        EntrySpliterator(Node<K,V>[] tab, int size, int index, int limit,
                         long est, ConcurrentHashMap<K,V> map) {
            super(tab, size, index, limit);
            this.map = map;
            this.est = est;
        }

        public Spliterator<Map.Entry<K,V>> trySplit() {
            int i, f, h;
            return (h = ((i = baseIndex) + (f = baseLimit)) >>> 1) <= i ? null :
                new EntrySpliterator<K,V>(tab, baseSize, baseLimit = h,
                                          f, est >>>= 1, map);
        }

        public void forEachRemaining(Consumer<? super Map.Entry<K,V>> action) {
            if (action == null) throw new NullPointerException();
            for (Node<K,V> p; (p = advance()) != null; )
                action.accept(new MapEntry<K,V>(p.key, p.val, map));
        }

        public boolean tryAdvance(Consumer<? super Map.Entry<K,V>> action) {
            if (action == null) throw new NullPointerException();
            Node<K,V> p;
            if ((p = advance()) == null)
                return false;
            action.accept(new MapEntry<K,V>(p.key, p.val, map));
            return true;
        }

        public long estimateSize() { return est; }

        public int characteristics() {
            return Spliterator.DISTINCT | Spliterator.CONCURRENT |
                Spliterator.NONNULL;
        }
    }

    // 并行批量操作

    /**
     * 计算批量任务的初始批次值。返回的值大约是将任务分成两个部分之前需要执行叶操作的次数（减一）的2的幂。这个值比深度更快地计算，并且在分割时更方便使用，因为它无论如何都会被除以二。
     */
    final int batchFor(long b) {
        long n;
        if (b == Long.MAX_VALUE || (n = sumCount()) <= 1L || n < b)
            return 0;
        int sp = ForkJoinPool.getCommonPoolParallelism() << 2; // 松弛度为4
        return (b <= 0L || (n /= b) >= sp) ? sp : (int)n;
    }

    /**
     * 对每个 (key, value) 执行给定的操作。
     *
     * @param parallelismThreshold 执行此操作所需的并行度阈值（估计的元素数量）
     * @param action 操作
     * @since 1.8
     */
    public void forEach(long parallelismThreshold,
                        BiConsumer<? super K,? super V> action) {
        if (action == null) throw new NullPointerException();
        new ForEachMappingTask<K,V>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             action).invoke();
    }

    /**
     * 对每个 (key, value) 的非空转换执行给定的操作。
     *
     * @param parallelismThreshold 执行此操作所需的并行度阈值（估计的元素数量）
     * @param transformer 返回元素转换的函数，如果没有转换则返回 null
     * @param action 操作
     * @param <U> 转换器的返回类型
     * @since 1.8
     */
    public <U> void forEach(long parallelismThreshold,
                            BiFunction<? super K, ? super V, ? extends U> transformer,
                            Consumer<? super U> action) {
        if (transformer == null || action == null)
            throw new NullPointerException();
        new ForEachTransformedMappingTask<K,V,U>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             transformer, action).invoke();
    }

    /**
     * 返回应用给定搜索函数在每个 (key, value) 上的结果，如果没有任何结果则返回 null。成功时，进一步的元素处理将被抑制，并且忽略其他并行调用搜索函数的结果。
     *
     * @param parallelismThreshold 执行此操作所需的并行度阈值（估计的元素数量）
     * @param searchFunction 成功时返回非空结果的函数，否则返回 null
     * @param <U> 搜索函数的返回类型
     * @return 应用给定搜索函数在每个 (key, value) 上的结果，如果没有任何结果则返回 null
     * @since 1.8
     */
    public <U> U search(long parallelismThreshold,
                        BiFunction<? super K, ? super V, ? extends U> searchFunction) {
        if (searchFunction == null) throw new NullPointerException();
        return new SearchMappingsTask<K,V,U>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             searchFunction, new AtomicReference<U>()).invoke();
    }

    /**
     * 返回使用给定的归约器组合值的所有 (key, value) 对的给定转换的累积结果，如果没有则返回 null。
     *
     * @param parallelismThreshold 执行此操作所需的并行度阈值（估计的元素数量）
     * @param transformer 返回元素转换的函数，如果没有转换则返回 null
     * @param reducer 交换和关联的组合函数
     * @param <U> 转换器的返回类型
     * @return 所有 (key, value) 对的给定转换的累积结果
     * @since 1.8
     */
    public <U> U reduce(long parallelismThreshold,
                        BiFunction<? super K, ? super V, ? extends U> transformer,
                        BiFunction<? super U, ? super U, ? extends U> reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceMappingsTask<K,V,U>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             null, transformer, reducer).invoke();
    }

                /**
     * 返回对所有 (key, value) 对进行给定转换并使用给定的归约器组合值的结果，
     * 以及将给定的基准值作为身份值。
     *
     * @param parallelismThreshold 用于并行执行此操作所需的元素数量（估计值）
     * @param transformer 返回元素转换结果的函数
     * @param basis 归约的身份值（初始默认值）
     * @param reducer 一个可交换的关联组合函数
     * @return 对所有 (key, value) 对进行给定转换的结果
     * @since 1.8
     */
    public double reduceToDouble(long parallelismThreshold,
                                 ToDoubleBiFunction<? super K, ? super V> transformer,
                                 double basis,
                                 DoubleBinaryOperator reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceMappingsToDoubleTask<K,V>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             null, transformer, basis, reducer).invoke();
    }

    /**
     * 返回对所有 (key, value) 对进行给定转换并使用给定的归约器组合值的结果，
     * 以及将给定的基准值作为身份值。
     *
     * @param parallelismThreshold 用于并行执行此操作所需的元素数量（估计值）
     * @param transformer 返回元素转换结果的函数
     * @param basis 归约的身份值（初始默认值）
     * @param reducer 一个可交换的关联组合函数
     * @return 对所有 (key, value) 对进行给定转换的结果
     * @since 1.8
     */
    public long reduceToLong(long parallelismThreshold,
                             ToLongBiFunction<? super K, ? super V> transformer,
                             long basis,
                             LongBinaryOperator reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceMappingsToLongTask<K,V>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             null, transformer, basis, reducer).invoke();
    }

    /**
     * 返回对所有 (key, value) 对进行给定转换并使用给定的归约器组合值的结果，
     * 以及将给定的基准值作为身份值。
     *
     * @param parallelismThreshold 用于并行执行此操作所需的元素数量（估计值）
     * @param transformer 返回元素转换结果的函数
     * @param basis 归约的身份值（初始默认值）
     * @param reducer 一个可交换的关联组合函数
     * @return 对所有 (key, value) 对进行给定转换的结果
     * @since 1.8
     */
    public int reduceToInt(long parallelismThreshold,
                           ToIntBiFunction<? super K, ? super V> transformer,
                           int basis,
                           IntBinaryOperator reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceMappingsToIntTask<K,V>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             null, transformer, basis, reducer).invoke();
    }

    /**
     * 对每个键执行给定的操作。
     *
     * @param parallelismThreshold 用于并行执行此操作所需的元素数量（估计值）
     * @param action 操作
     * @since 1.8
     */
    public void forEachKey(long parallelismThreshold,
                           Consumer<? super K> action) {
        if (action == null) throw new NullPointerException();
        new ForEachKeyTask<K,V>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             action).invoke();
    }

    /**
     * 对每个键的每个非空转换执行给定的操作。
     *
     * @param parallelismThreshold 用于并行执行此操作所需的元素数量（估计值）
     * @param transformer 返回元素转换结果的函数，如果没有转换则返回 null（在这种情况下不应用操作）
     * @param action 操作
     * @param <U> 转换器的返回类型
     * @since 1.8
     */
    public <U> void forEachKey(long parallelismThreshold,
                               Function<? super K, ? extends U> transformer,
                               Consumer<? super U> action) {
        if (transformer == null || action == null)
            throw new NullPointerException();
        new ForEachTransformedKeyTask<K,V,U>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             transformer, action).invoke();
    }

    /**
     * 返回对每个键应用给定搜索函数的结果，如果没有任何结果则返回 null。一旦成功，
     * 进一步的元素处理将被抑制，任何其他并行调用搜索函数的结果将被忽略。
     *
     * @param parallelismThreshold 用于并行执行此操作所需的元素数量（估计值）
     * @param searchFunction 成功时返回非空结果的函数，否则返回 null
     * @param <U> 搜索函数的返回类型
     * @return 对每个键应用给定搜索函数的结果，如果没有任何结果则返回 null
     * @since 1.8
     */
    public <U> U searchKeys(long parallelismThreshold,
                            Function<? super K, ? extends U> searchFunction) {
        if (searchFunction == null) throw new NullPointerException();
        return new SearchKeysTask<K,V,U>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             searchFunction, new AtomicReference<U>()).invoke();
    }

                /**
     * 返回使用给定的reducer组合值的所有键的累积结果，如果没有则返回null。
     *
     * @param parallelismThreshold 该操作并行执行所需的（估计）元素数量
     * @param reducer 一个可交换的关联组合函数
     * @return 使用给定的reducer组合值的所有键的累积结果，如果没有则返回null
     * @since 1.8
     */
    public K reduceKeys(long parallelismThreshold,
                        BiFunction<? super K, ? super K, ? extends K> reducer) {
        if (reducer == null) throw new NullPointerException();
        return new ReduceKeysTask<K,V>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             null, reducer).invoke();
    }

    /**
     * 返回使用给定的reducer组合值的所有键的给定转换的累积结果，如果没有则返回null。
     *
     * @param parallelismThreshold 该操作并行执行所需的（估计）元素数量
     * @param transformer 返回元素转换的函数，如果没有转换则返回null
     * @param reducer 一个可交换的关联组合函数
     * @param <U> 转换器的返回类型
     * @return 使用给定的reducer组合值的所有键的给定转换的累积结果
     * @since 1.8
     */
    public <U> U reduceKeys(long parallelismThreshold,
                            Function<? super K, ? extends U> transformer,
         BiFunction<? super U, ? super U, ? extends U> reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceKeysTask<K,V,U>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             null, transformer, reducer).invoke();
    }

    /**
     * 返回使用给定的reducer组合值的所有键的给定转换的累积结果，并使用给定的basis作为身份值。
     *
     * @param parallelismThreshold 该操作并行执行所需的（估计）元素数量
     * @param transformer 返回元素转换的函数
     * @param basis 减少的身份（初始默认值）
     * @param reducer 一个可交换的关联组合函数
     * @return 使用给定的reducer组合值的所有键的给定转换的累积结果
     * @since 1.8
     */
    public double reduceKeysToDouble(long parallelismThreshold,
                                     ToDoubleFunction<? super K> transformer,
                                     double basis,
                                     DoubleBinaryOperator reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceKeysToDoubleTask<K,V>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             null, transformer, basis, reducer).invoke();
    }

    /**
     * 返回使用给定的reducer组合值的所有键的给定转换的累积结果，并使用给定的basis作为身份值。
     *
     * @param parallelismThreshold 该操作并行执行所需的（估计）元素数量
     * @param transformer 返回元素转换的函数
     * @param basis 减少的身份（初始默认值）
     * @param reducer 一个可交换的关联组合函数
     * @return 使用给定的reducer组合值的所有键的给定转换的累积结果
     * @since 1.8
     */
    public long reduceKeysToLong(long parallelismThreshold,
                                 ToLongFunction<? super K> transformer,
                                 long basis,
                                 LongBinaryOperator reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceKeysToLongTask<K,V>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             null, transformer, basis, reducer).invoke();
    }

    /**
     * 返回使用给定的reducer组合值的所有键的给定转换的累积结果，并使用给定的basis作为身份值。
     *
     * @param parallelismThreshold 该操作并行执行所需的（估计）元素数量
     * @param transformer 返回元素转换的函数
     * @param basis 减少的身份（初始默认值）
     * @param reducer 一个可交换的关联组合函数
     * @return 使用给定的reducer组合值的所有键的给定转换的累积结果
     * @since 1.8
     */
    public int reduceKeysToInt(long parallelismThreshold,
                               ToIntFunction<? super K> transformer,
                               int basis,
                               IntBinaryOperator reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceKeysToIntTask<K,V>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             null, transformer, basis, reducer).invoke();
    }

    /**
     * 对每个值执行给定的操作。
     *
     * @param parallelismThreshold 该操作并行执行所需的（估计）元素数量
     * @param action 操作
     * @since 1.8
     */
    public void forEachValue(long parallelismThreshold,
                             Consumer<? super V> action) {
        if (action == null)
            throw new NullPointerException();
        new ForEachValueTask<K,V>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             action).invoke();
    }

    /**
     * 对每个值的每个非空转换执行给定的操作。
     *
     * @param parallelismThreshold 该操作并行执行所需的（估计）元素数量
     * @param transformer 返回元素转换的函数，如果没有转换则返回null
     * @param action 操作
     * @param <U> 转换器的返回类型
     * @since 1.8
     */
    public <U> void forEachValue(long parallelismThreshold,
                                 Function<? super V, ? extends U> transformer,
                                 Consumer<? super U> action) {
        if (transformer == null || action == null)
            throw new NullPointerException();
        new ForEachTransformedValueTask<K,V,U>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             transformer, action).invoke();
    }

                /**
     * 返回应用给定搜索函数到每个值的结果，如果没有任何结果，则返回 null。成功时，
     * 进一步的元素处理将被抑制，其他并行调用搜索函数的结果将被忽略。
     *
     * @param parallelismThreshold 该操作并行执行所需的（估计）元素数量
     * @param searchFunction 成功时返回非 null 结果的函数，否则返回 null
     * @param <U> 搜索函数的返回类型
     * @return 应用给定搜索函数到每个值的非 null 结果，如果没有则返回 null
     * @since 1.8
     */
    public <U> U searchValues(long parallelismThreshold,
                              Function<? super V, ? extends U> searchFunction) {
        if (searchFunction == null) throw new NullPointerException();
        return new SearchValuesTask<K,V,U>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             searchFunction, new AtomicReference<U>()).invoke();
    }

    /**
     * 返回使用给定的归约函数组合所有值的结果，如果没有则返回 null。
     *
     * @param parallelismThreshold 该操作并行执行所需的（估计）元素数量
     * @param reducer 一个交换和结合的组合函数
     * @return 累积所有值的结果
     * @since 1.8
     */
    public V reduceValues(long parallelismThreshold,
                          BiFunction<? super V, ? super V, ? extends V> reducer) {
        if (reducer == null) throw new NullPointerException();
        return new ReduceValuesTask<K,V>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             null, reducer).invoke();
    }

    /**
     * 返回使用给定的归约函数组合所有值的给定转换的结果，如果没有则返回 null。
     *
     * @param parallelismThreshold 该操作并行执行所需的（估计）元素数量
     * @param transformer 返回元素转换的函数，如果没有转换则返回 null（在这种情况下不会被组合）
     * @param reducer 一个交换和结合的组合函数
     * @param <U> 转换器的返回类型
     * @return 累积所有值的给定转换的结果
     * @since 1.8
     */
    public <U> U reduceValues(long parallelismThreshold,
                              Function<? super V, ? extends U> transformer,
                              BiFunction<? super U, ? super U, ? extends U> reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceValuesTask<K,V,U>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             null, transformer, reducer).invoke();
    }

    /**
     * 返回使用给定的归约函数组合所有值的给定转换的结果，以及给定的基础值作为身份值。
     *
     * @param parallelismThreshold 该操作并行执行所需的（估计）元素数量
     * @param transformer 返回元素转换的函数
     * @param basis 归约的身份（初始默认值）
     * @param reducer 一个交换和结合的组合函数
     * @return 累积所有值的给定转换的结果
     * @since 1.8
     */
    public double reduceValuesToDouble(long parallelismThreshold,
                                       ToDoubleFunction<? super V> transformer,
                                       double basis,
                                       DoubleBinaryOperator reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceValuesToDoubleTask<K,V>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             null, transformer, basis, reducer).invoke();
    }

    /**
     * 返回使用给定的归约函数组合所有值的给定转换的结果，以及给定的基础值作为身份值。
     *
     * @param parallelismThreshold 该操作并行执行所需的（估计）元素数量
     * @param transformer 返回元素转换的函数
     * @param basis 归约的身份（初始默认值）
     * @param reducer 一个交换和结合的组合函数
     * @return 累积所有值的给定转换的结果
     * @since 1.8
     */
    public long reduceValuesToLong(long parallelismThreshold,
                                   ToLongFunction<? super V> transformer,
                                   long basis,
                                   LongBinaryOperator reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceValuesToLongTask<K,V>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             null, transformer, basis, reducer).invoke();
    }

    /**
     * 返回使用给定的归约函数组合所有值的给定转换的结果，以及给定的基础值作为身份值。
     *
     * @param parallelismThreshold 该操作并行执行所需的（估计）元素数量
     * @param transformer 返回元素转换的函数
     * @param basis 归约的身份（初始默认值）
     * @param reducer 一个交换和结合的组合函数
     * @return 累积所有值的给定转换的结果
     * @since 1.8
     */
    public int reduceValuesToInt(long parallelismThreshold,
                                 ToIntFunction<? super V> transformer,
                                 int basis,
                                 IntBinaryOperator reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceValuesToIntTask<K,V>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             null, transformer, basis, reducer).invoke();
    }

                /**
     * 对每个条目执行给定的操作。
     *
     * @param parallelismThreshold 用于并行执行此操作所需的（估计）元素数量
     * @param action 操作
     * @since 1.8
     */
    public void forEachEntry(long parallelismThreshold,
                             Consumer<? super Map.Entry<K,V>> action) {
        if (action == null) throw new NullPointerException();
        new ForEachEntryTask<K,V>(null, batchFor(parallelismThreshold), 0, 0, table,
                                  action).invoke();
    }

    /**
     * 对每个条目的非空转换执行给定的操作。
     *
     * @param parallelismThreshold 用于并行执行此操作所需的（估计）元素数量
     * @param transformer 返回元素转换的函数，如果没有转换则返回null
     * @param action 操作
     * @param <U> 转换器的返回类型
     * @since 1.8
     */
    public <U> void forEachEntry(long parallelismThreshold,
                                 Function<Map.Entry<K,V>, ? extends U> transformer,
                                 Consumer<? super U> action) {
        if (transformer == null || action == null)
            throw new NullPointerException();
        new ForEachTransformedEntryTask<K,V,U>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             transformer, action).invoke();
    }

    /**
     * 返回应用给定搜索函数到每个条目时返回的非空结果，如果没有则返回null。成功时，
     * 进一步的元素处理将被抑制，并且其他并行调用搜索函数的结果将被忽略。
     *
     * @param parallelismThreshold 用于并行执行此操作所需的（估计）元素数量
     * @param searchFunction 成功时返回非空结果的函数，否则返回null
     * @param <U> 搜索函数的返回类型
     * @return 应用给定搜索函数到每个条目时返回的非空结果，如果没有则返回null
     * @since 1.8
     */
    public <U> U searchEntries(long parallelismThreshold,
                               Function<Map.Entry<K,V>, ? extends U> searchFunction) {
        if (searchFunction == null) throw new NullPointerException();
        return new SearchEntriesTask<K,V,U>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             searchFunction, new AtomicReference<U>()).invoke();
    }

    /**
     * 返回使用给定的归约器组合值累积所有条目的结果，如果没有则返回null。
     *
     * @param parallelismThreshold 用于并行执行此操作所需的（估计）元素数量
     * @param reducer 一个可交换的关联组合函数
     * @return 累积所有条目的结果
     * @since 1.8
     */
    public Map.Entry<K,V> reduceEntries(long parallelismThreshold,
                                        BiFunction<Map.Entry<K,V>, Map.Entry<K,V>, ? extends Map.Entry<K,V>> reducer) {
        if (reducer == null) throw new NullPointerException();
        return new ReduceEntriesTask<K,V>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             null, reducer).invoke();
    }

    /**
     * 返回使用给定的归约器组合值累积所有条目的给定转换的结果，如果没有则返回null。
     *
     * @param parallelismThreshold 用于并行执行此操作所需的（估计）元素数量
     * @param transformer 返回元素转换的函数，如果没有转换则返回null
     * @param reducer 一个可交换的关联组合函数
     * @param <U> 转换器的返回类型
     * @return 累积所有条目的给定转换的结果
     * @since 1.8
     */
    public <U> U reduceEntries(long parallelismThreshold,
                               Function<Map.Entry<K,V>, ? extends U> transformer,
                               BiFunction<? super U, ? super U, ? extends U> reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceEntriesTask<K,V,U>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             null, transformer, reducer).invoke();
    }

    /**
     * 返回使用给定的归约器组合值累积所有条目的给定转换的结果，使用给定的基础值作为身份值。
     *
     * @param parallelismThreshold 用于并行执行此操作所需的（估计）元素数量
     * @param transformer 返回元素转换的函数
     * @param basis 归约的身份（初始默认值）
     * @param reducer 一个可交换的关联组合函数
     * @return 累积所有条目的给定转换的结果
     * @since 1.8
     */
    public double reduceEntriesToDouble(long parallelismThreshold,
                                        ToDoubleFunction<Map.Entry<K,V>> transformer,
                                        double basis,
                                        DoubleBinaryOperator reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceEntriesToDoubleTask<K,V>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             null, transformer, basis, reducer).invoke();
    }

    /**
     * 返回使用给定的归约器组合值累积所有条目的给定转换的结果，使用给定的基础值作为身份值。
     *
     * @param parallelismThreshold 用于并行执行此操作所需的（估计）元素数量
     * @param transformer 返回元素转换的函数
     * @param basis 归约的身份（初始默认值）
     * @param reducer 一个可交换的关联组合函数
     * @return 累积所有条目的给定转换的结果
     * @since 1.8
     */
    public long reduceEntriesToLong(long parallelismThreshold,
                                    ToLongFunction<Map.Entry<K,V>> transformer,
                                    long basis,
                                    LongBinaryOperator reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceEntriesToLongTask<K,V>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             null, transformer, basis, reducer).invoke();
    }

                /**
     * 返回对所有条目进行给定转换并使用给定的归约器组合值的结果，
     * 以及给定的基础值作为身份值。
     *
     * @param parallelismThreshold 该操作并行执行所需的（估计）元素数量
     * @param transformer 返回元素转换的函数
     * @param basis 归约的身份（初始默认值）
     * @param reducer 交换结合组合函数
     * @return 对所有条目进行给定转换的结果
     * @since 1.8
     */
    public int reduceEntriesToInt(long parallelismThreshold,
                                  ToIntFunction<Map.Entry<K,V>> transformer,
                                  int basis,
                                  IntBinaryOperator reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceEntriesToIntTask<K,V>
            (null, batchFor(parallelismThreshold), 0, 0, table,
             null, transformer, basis, reducer).invoke();
    }


    /* ----------------视图 -------------- */

    /**
     * 视图的基类。
     */
    abstract static class CollectionView<K,V,E>
        implements Collection<E>, java.io.Serializable {
        private static final long serialVersionUID = 7249069246763182397L;
        final ConcurrentHashMap<K,V> map;
        CollectionView(ConcurrentHashMap<K,V> map)  { this.map = map; }

        /**
         * 返回支持此视图的映射。
         *
         * @return 支持此视图的映射
         */
        public ConcurrentHashMap<K,V> getMap() { return map; }

        /**
         * 通过从支持此视图的映射中删除所有映射来删除此视图中的所有元素。
         */
        public final void clear()      { map.clear(); }
        public final int size()        { return map.size(); }
        public final boolean isEmpty() { return map.isEmpty(); }

        // 以下实现依赖于具体类提供这些抽象方法
        /**
         * 返回此集合中元素的迭代器。
         *
         * <p>返回的迭代器是
         * <a href="package-summary.html#Weakly"><i>弱一致</i></a>。
         *
         * @return 此集合中元素的迭代器
         */
        public abstract Iterator<E> iterator();
        public abstract boolean contains(Object o);
        public abstract boolean remove(Object o);

        private static final String oomeMsg = "所需数组大小过大";

        public final Object[] toArray() {
            long sz = map.mappingCount();
            if (sz > MAX_ARRAY_SIZE)
                throw new OutOfMemoryError(oomeMsg);
            int n = (int)sz;
            Object[] r = new Object[n];
            int i = 0;
            for (E e : this) {
                if (i == n) {
                    if (n >= MAX_ARRAY_SIZE)
                        throw new OutOfMemoryError(oomeMsg);
                    if (n >= MAX_ARRAY_SIZE - (MAX_ARRAY_SIZE >>> 1) - 1)
                        n = MAX_ARRAY_SIZE;
                    else
                        n += (n >>> 1) + 1;
                    r = Arrays.copyOf(r, n);
                }
                r[i++] = e;
            }
            return (i == n) ? r : Arrays.copyOf(r, i);
        }

        @SuppressWarnings("unchecked")
        public final <T> T[] toArray(T[] a) {
            long sz = map.mappingCount();
            if (sz > MAX_ARRAY_SIZE)
                throw new OutOfMemoryError(oomeMsg);
            int m = (int)sz;
            T[] r = (a.length >= m) ? a :
                (T[])java.lang.reflect.Array
                .newInstance(a.getClass().getComponentType(), m);
            int n = r.length;
            int i = 0;
            for (E e : this) {
                if (i == n) {
                    if (n >= MAX_ARRAY_SIZE)
                        throw new OutOfMemoryError(oomeMsg);
                    if (n >= MAX_ARRAY_SIZE - (MAX_ARRAY_SIZE >>> 1) - 1)
                        n = MAX_ARRAY_SIZE;
                    else
                        n += (n >>> 1) + 1;
                    r = Arrays.copyOf(r, n);
                }
                r[i++] = (T)e;
            }
            if (a == r && i < n) {
                r[i] = null; // null-终止
                return r;
            }
            return (i == n) ? r : Arrays.copyOf(r, i);
        }

        /**
         * 返回此集合的字符串表示形式。
         * 字符串表示形式由集合元素的字符串表示形式组成，顺序为迭代器返回的顺序，
         * 用方括号（{@code "[]"}）括起来。相邻元素之间用逗号和空格（{@code ", "}）分隔。
         * 元素转换为字符串的方式与 {@link String#valueOf(Object)} 相同。
         *
         * @return 此集合的字符串表示形式
         */
        public final String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            Iterator<E> it = iterator();
            if (it.hasNext()) {
                for (;;) {
                    Object e = it.next();
                    sb.append(e == this ? "(this Collection)" : e);
                    if (!it.hasNext())
                        break;
                    sb.append(',').append(' ');
                }
            }
            return sb.append(']').toString();
        }

        public final boolean containsAll(Collection<?> c) {
            if (c != this) {
                for (Object e : c) {
                    if (e == null || !contains(e))
                        return false;
                }
            }
            return true;
        }

        public final boolean removeAll(Collection<?> c) {
            if (c == null) throw new NullPointerException();
            boolean modified = false;
            for (Iterator<E> it = iterator(); it.hasNext();) {
                if (c.contains(it.next())) {
                    it.remove();
                    modified = true;
                }
            }
            return modified;
        }


                    public final boolean retainAll(Collection<?> c) {
            if (c == null) throw new NullPointerException();
            boolean modified = false;
            for (Iterator<E> it = iterator(); it.hasNext();) {
                if (!c.contains(it.next())) {
                    it.remove();
                    modified = true;
                }
            }
            return modified;
        }

    }

    /**
     * 一个 ConcurrentHashMap 的视图，作为键的 {@link Set}，其中添加操作可以通过映射到一个共同的值来选择性地启用。
     * 该类不能直接实例化。
     * 参见 {@link #keySet() keySet()}，
     * {@link #keySet(Object) keySet(V)}，
     * {@link #newKeySet() newKeySet()}，
     * {@link #newKeySet(int) newKeySet(int)}。
     *
     * @since 1.8
     */
    public static class KeySetView<K,V> extends CollectionView<K,V,K>
        implements Set<K>, java.io.Serializable {
        private static final long serialVersionUID = 7249069246763182397L;
        private final V value;
        KeySetView(ConcurrentHashMap<K,V> map, V value) {  // 非公开
            super(map);
            this.value = value;
        }

        /**
         * 返回添加操作的默认映射值，
         * 或者如果添加操作不受支持则返回 {@code null}。
         *
         * @return 添加操作的默认映射值，或者如果不受支持则返回 {@code null}
         */
        public V getMappedValue() { return value; }

        /**
         * {@inheritDoc}
         * @throws NullPointerException 如果指定的键为 null
         */
        public boolean contains(Object o) { return map.containsKey(o); }

        /**
         * 通过从支持的映射中移除键（及其对应的值）来从该映射视图中移除键。
         * 如果键不在映射中，此方法不执行任何操作。
         *
         * @param  o 要从支持的映射中移除的键
         * @return 如果支持的映射包含指定的键，则返回 {@code true}
         * @throws NullPointerException 如果指定的键为 null
         */
        public boolean remove(Object o) { return map.remove(o) != null; }

        /**
         * @return 支持的映射的键的迭代器
         */
        public Iterator<K> iterator() {
            Node<K,V>[] t;
            ConcurrentHashMap<K,V> m = map;
            int f = (t = m.table) == null ? 0 : t.length;
            return new KeyIterator<K,V>(t, f, 0, f, m);
        }

        /**
         * 通过在支持的映射中将键映射到默认映射值（如果已定义）来将指定的键添加到此集合视图中。
         *
         * @param e 要添加的键
         * @return 如果此集合因调用而改变，则返回 {@code true}
         * @throws NullPointerException 如果指定的键为 null
         * @throws UnsupportedOperationException 如果未提供添加操作的默认映射值
         */
        public boolean add(K e) {
            V v;
            if ((v = value) == null)
                throw new UnsupportedOperationException();
            return map.putVal(e, v, true) == null;
        }

        /**
         * 将指定集合中的所有元素添加到此集合中，就像对每个元素调用 {@link #add} 一样。
         *
         * @param c 要插入到此集合中的元素
         * @return 如果此集合因调用而改变，则返回 {@code true}
         * @throws NullPointerException 如果集合或其任何元素为 {@code null}
         * @throws UnsupportedOperationException 如果未提供添加操作的默认映射值
         */
        public boolean addAll(Collection<? extends K> c) {
            boolean added = false;
            V v;
            if ((v = value) == null)
                throw new UnsupportedOperationException();
            for (K e : c) {
                if (map.putVal(e, v, true) == null)
                    added = true;
            }
            return added;
        }

        public int hashCode() {
            int h = 0;
            for (K e : this)
                h += e.hashCode();
            return h;
        }

        public boolean equals(Object o) {
            Set<?> c;
            return ((o instanceof Set) &&
                    ((c = (Set<?>)o) == this ||
                     (containsAll(c) && c.containsAll(this))));
        }

        public Spliterator<K> spliterator() {
            Node<K,V>[] t;
            ConcurrentHashMap<K,V> m = map;
            long n = m.sumCount();
            int f = (t = m.table) == null ? 0 : t.length;
            return new KeySpliterator<K,V>(t, f, 0, f, n < 0L ? 0L : n);
        }

        public void forEach(Consumer<? super K> action) {
            if (action == null) throw new NullPointerException();
            Node<K,V>[] t;
            if ((t = map.table) != null) {
                Traverser<K,V> it = new Traverser<K,V>(t, t.length, 0, t.length);
                for (Node<K,V> p; (p = it.advance()) != null; )
                    action.accept(p.key);
            }
        }
    }

    /**
     * 一个 ConcurrentHashMap 的视图，作为值的 {@link Collection}，其中添加操作被禁用。
     * 该类不能直接实例化。参见 {@link #values()}。
     */
    static final class ValuesView<K,V> extends CollectionView<K,V,V>
        implements Collection<V>, java.io.Serializable {
        private static final long serialVersionUID = 2249069246763182397L;
        ValuesView(ConcurrentHashMap<K,V> map) { super(map); }
        public final boolean contains(Object o) {
            return map.containsValue(o);
        }

        public final boolean remove(Object o) {
            if (o != null) {
                for (Iterator<V> it = iterator(); it.hasNext();) {
                    if (o.equals(it.next())) {
                        it.remove();
                        return true;
                    }
                }
            }
            return false;
        }

        public final Iterator<V> iterator() {
            ConcurrentHashMap<K,V> m = map;
            Node<K,V>[] t;
            int f = (t = m.table) == null ? 0 : t.length;
            return new ValueIterator<K,V>(t, f, 0, f, m);
        }


                    public final boolean add(V e) {
            throw new UnsupportedOperationException();
        }
        public final boolean addAll(Collection<? extends V> c) {
            throw new UnsupportedOperationException();
        }

        public Spliterator<V> spliterator() {
            Node<K,V>[] t;
            ConcurrentHashMap<K,V> m = map;
            long n = m.sumCount();
            int f = (t = m.table) == null ? 0 : t.length;
            return new ValueSpliterator<K,V>(t, f, 0, f, n < 0L ? 0L : n);
        }

        public void forEach(Consumer<? super V> action) {
            if (action == null) throw new NullPointerException();
            Node<K,V>[] t;
            if ((t = map.table) != null) {
                Traverser<K,V> it = new Traverser<K,V>(t, t.length, 0, t.length);
                for (Node<K,V> p; (p = it.advance()) != null; )
                    action.accept(p.val);
            }
        }
    }

    /**
     * A view of a ConcurrentHashMap as a {@link Set} of (key, value)
     * entries.  This class cannot be directly instantiated. See
     * {@link #entrySet()}.
     */
    static final class EntrySetView<K,V> extends CollectionView<K,V,Map.Entry<K,V>>
        implements Set<Map.Entry<K,V>>, java.io.Serializable {
        private static final long serialVersionUID = 2249069246763182397L;
        EntrySetView(ConcurrentHashMap<K,V> map) { super(map); }

        public boolean contains(Object o) {
            Object k, v, r; Map.Entry<?,?> e;
            return ((o instanceof Map.Entry) &&
                    (k = (e = (Map.Entry<?,?>)o).getKey()) != null &&
                    (r = map.get(k)) != null &&
                    (v = e.getValue()) != null &&
                    (v == r || v.equals(r)));
        }

        public boolean remove(Object o) {
            Object k, v; Map.Entry<?,?> e;
            return ((o instanceof Map.Entry) &&
                    (k = (e = (Map.Entry<?,?>)o).getKey()) != null &&
                    (v = e.getValue()) != null &&
                    map.remove(k, v));
        }

        /**
         * @return an iterator over the entries of the backing map
         */
        public Iterator<Map.Entry<K,V>> iterator() {
            ConcurrentHashMap<K,V> m = map;
            Node<K,V>[] t;
            int f = (t = m.table) == null ? 0 : t.length;
            return new EntryIterator<K,V>(t, f, 0, f, m);
        }

        public boolean add(Entry<K,V> e) {
            return map.putVal(e.getKey(), e.getValue(), false) == null;
        }

        public boolean addAll(Collection<? extends Entry<K,V>> c) {
            boolean added = false;
            for (Entry<K,V> e : c) {
                if (add(e))
                    added = true;
            }
            return added;
        }

        public final int hashCode() {
            int h = 0;
            Node<K,V>[] t;
            if ((t = map.table) != null) {
                Traverser<K,V> it = new Traverser<K,V>(t, t.length, 0, t.length);
                for (Node<K,V> p; (p = it.advance()) != null; ) {
                    h += p.hashCode();
                }
            }
            return h;
        }

        public final boolean equals(Object o) {
            Set<?> c;
            return ((o instanceof Set) &&
                    ((c = (Set<?>)o) == this ||
                     (containsAll(c) && c.containsAll(this))));
        }

        public Spliterator<Map.Entry<K,V>> spliterator() {
            Node<K,V>[] t;
            ConcurrentHashMap<K,V> m = map;
            long n = m.sumCount();
            int f = (t = m.table) == null ? 0 : t.length;
            return new EntrySpliterator<K,V>(t, f, 0, f, n < 0L ? 0L : n, m);
        }

        public void forEach(Consumer<? super Map.Entry<K,V>> action) {
            if (action == null) throw new NullPointerException();
            Node<K,V>[] t;
            if ((t = map.table) != null) {
                Traverser<K,V> it = new Traverser<K,V>(t, t.length, 0, t.length);
                for (Node<K,V> p; (p = it.advance()) != null; )
                    action.accept(new MapEntry<K,V>(p.key, p.val, map));
            }
        }

    }

    // -------------------------------------------------------

    /**
     * Base class for bulk tasks. Repeats some fields and code from
     * class Traverser, because we need to subclass CountedCompleter.
     */
    @SuppressWarnings("serial")
    abstract static class BulkTask<K,V,R> extends CountedCompleter<R> {
        Node<K,V>[] tab;        // same as Traverser
        Node<K,V> next;
        TableStack<K,V> stack, spare;
        int index;
        int baseIndex;
        int baseLimit;
        final int baseSize;
        int batch;              // split control

        BulkTask(BulkTask<K,V,?> par, int b, int i, int f, Node<K,V>[] t) {
            super(par);
            this.batch = b;
            this.index = this.baseIndex = i;
            if ((this.tab = t) == null)
                this.baseSize = this.baseLimit = 0;
            else if (par == null)
                this.baseSize = this.baseLimit = t.length;
            else {
                this.baseLimit = f;
                this.baseSize = par.baseSize;
            }
        }

        /**
         * Same as Traverser version
         */
        final Node<K,V> advance() {
            Node<K,V> e;
            if ((e = next) != null)
                e = e.next;
            for (;;) {
                Node<K,V>[] t; int i, n;
                if (e != null)
                    return next = e;
                if (baseIndex >= baseLimit || (t = tab) == null ||
                    (n = t.length) <= (i = index) || i < 0)
                    return next = null;
                if ((e = tabAt(t, i)) != null && e.hash < 0) {
                    if (e instanceof ForwardingNode) {
                        tab = ((ForwardingNode<K,V>)e).nextTable;
                        e = null;
                        pushState(t, i, n);
                        continue;
                    }
                    else if (e instanceof TreeBin)
                        e = ((TreeBin<K,V>)e).first;
                    else
                        e = null;
                }
                if (stack != null)
                    recoverState(n);
                else if ((index = i + baseSize) >= n)
                    index = ++baseIndex;
            }
        }


                    private void pushState(Node<K,V>[] t, int i, int n) {
            TableStack<K,V> s = spare;
            if (s != null)
                spare = s.next;
            else
                s = new TableStack<K,V>();
            s.tab = t;
            s.length = n;
            s.index = i;
            s.next = stack;
            stack = s;
        }

        private void recoverState(int n) {
            TableStack<K,V> s; int len;
            while ((s = stack) != null && (index += (len = s.length)) >= n) {
                n = len;
                index = s.index;
                tab = s.tab;
                s.tab = null;
                TableStack<K,V> next = s.next;
                s.next = spare; // save for reuse
                stack = next;
                spare = s;
            }
            if (s == null && (index += baseSize) >= n)
                index = ++baseIndex;
        }
    }

    /*
     * 任务类。以一种常规但丑陋的格式/风格编写，以简化检查每个变体在正确的方式上与其他变体的不同。存在空检查是因为编译器无法判断我们已经对任务参数进行了空检查，所以我们强制执行最简单的提升绕过，以帮助避免复杂的陷阱。
     */
    @SuppressWarnings("serial")
    static final class ForEachKeyTask<K,V>
        extends BulkTask<K,V,Void> {
        final Consumer<? super K> action;
        ForEachKeyTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             Consumer<? super K> action) {
            super(p, b, i, f, t);
            this.action = action;
        }
        public final void compute() {
            final Consumer<? super K> action;
            if ((action = this.action) != null) {
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    new ForEachKeyTask<K,V>
                        (this, batch >>>= 1, baseLimit = h, f, tab,
                         action).fork();
                }
                for (Node<K,V> p; (p = advance()) != null;)
                    action.accept(p.key);
                propagateCompletion();
            }
        }
    }

    @SuppressWarnings("serial")
    static final class ForEachValueTask<K,V>
        extends BulkTask<K,V,Void> {
        final Consumer<? super V> action;
        ForEachValueTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             Consumer<? super V> action) {
            super(p, b, i, f, t);
            this.action = action;
        }
        public final void compute() {
            final Consumer<? super V> action;
            if ((action = this.action) != null) {
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    new ForEachValueTask<K,V>
                        (this, batch >>>= 1, baseLimit = h, f, tab,
                         action).fork();
                }
                for (Node<K,V> p; (p = advance()) != null;)
                    action.accept(p.val);
                propagateCompletion();
            }
        }
    }

    @SuppressWarnings("serial")
    static final class ForEachEntryTask<K,V>
        extends BulkTask<K,V,Void> {
        final Consumer<? super Entry<K,V>> action;
        ForEachEntryTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             Consumer<? super Entry<K,V>> action) {
            super(p, b, i, f, t);
            this.action = action;
        }
        public final void compute() {
            final Consumer<? super Entry<K,V>> action;
            if ((action = this.action) != null) {
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    new ForEachEntryTask<K,V>
                        (this, batch >>>= 1, baseLimit = h, f, tab,
                         action).fork();
                }
                for (Node<K,V> p; (p = advance()) != null; )
                    action.accept(p);
                propagateCompletion();
            }
        }
    }

    @SuppressWarnings("serial")
    static final class ForEachMappingTask<K,V>
        extends BulkTask<K,V,Void> {
        final BiConsumer<? super K, ? super V> action;
        ForEachMappingTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             BiConsumer<? super K,? super V> action) {
            super(p, b, i, f, t);
            this.action = action;
        }
        public final void compute() {
            final BiConsumer<? super K, ? super V> action;
            if ((action = this.action) != null) {
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    new ForEachMappingTask<K,V>
                        (this, batch >>>= 1, baseLimit = h, f, tab,
                         action).fork();
                }
                for (Node<K,V> p; (p = advance()) != null; )
                    action.accept(p.key, p.val);
                propagateCompletion();
            }
        }
    }

    @SuppressWarnings("serial")
    static final class ForEachTransformedKeyTask<K,V,U>
        extends BulkTask<K,V,Void> {
        final Function<? super K, ? extends U> transformer;
        final Consumer<? super U> action;
        ForEachTransformedKeyTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             Function<? super K, ? extends U> transformer, Consumer<? super U> action) {
            super(p, b, i, f, t);
            this.transformer = transformer; this.action = action;
        }
        public final void compute() {
            final Function<? super K, ? extends U> transformer;
            final Consumer<? super U> action;
            if ((transformer = this.transformer) != null &&
                (action = this.action) != null) {
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    new ForEachTransformedKeyTask<K,V,U>
                        (this, batch >>>= 1, baseLimit = h, f, tab,
                         transformer, action).fork();
                }
                for (Node<K,V> p; (p = advance()) != null; ) {
                    U u;
                    if ((u = transformer.apply(p.key)) != null)
                        action.accept(u);
                }
                propagateCompletion();
            }
        }
    }


                @SuppressWarnings("serial")
    static final class ForEachTransformedValueTask<K,V,U>
        extends BulkTask<K,V,Void> {
        // 转换器，用于将值转换为另一种类型
        final Function<? super V, ? extends U> transformer;
        // 消费者，用于处理转换后的值
        final Consumer<? super U> action;
        // 构造函数，初始化任务
        ForEachTransformedValueTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             Function<? super V, ? extends U> transformer, Consumer<? super U> action) {
            super(p, b, i, f, t);
            this.transformer = transformer; this.action = action;
        }
        // 执行任务的方法
        public final void compute() {
            final Function<? super V, ? extends U> transformer;
            final Consumer<? super U> action;
            // 检查转换器和消费者是否不为空
            if ((transformer = this.transformer) != null &&
                (action = this.action) != null) {
                // 分割任务
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    new ForEachTransformedValueTask<K,V,U>
                        (this, batch >>>= 1, baseLimit = h, f, tab,
                         transformer, action).fork();
                }
                // 处理当前任务的节点
                for (Node<K,V> p; (p = advance()) != null; ) {
                    U u;
                    if ((u = transformer.apply(p.val)) != null)
                        action.accept(u);
                }
                // 传播完成状态
                propagateCompletion();
            }
        }
    }

    @SuppressWarnings("serial")
    static final class ForEachTransformedEntryTask<K,V,U>
        extends BulkTask<K,V,Void> {
        // 转换器，用于将条目转换为另一种类型
        final Function<Map.Entry<K,V>, ? extends U> transformer;
        // 消费者，用于处理转换后的值
        final Consumer<? super U> action;
        // 构造函数，初始化任务
        ForEachTransformedEntryTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             Function<Map.Entry<K,V>, ? extends U> transformer, Consumer<? super U> action) {
            super(p, b, i, f, t);
            this.transformer = transformer; this.action = action;
        }
        // 执行任务的方法
        public final void compute() {
            final Function<Map.Entry<K,V>, ? extends U> transformer;
            final Consumer<? super U> action;
            // 检查转换器和消费者是否不为空
            if ((transformer = this.transformer) != null &&
                (action = this.action) != null) {
                // 分割任务
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    new ForEachTransformedEntryTask<K,V,U>
                        (this, batch >>>= 1, baseLimit = h, f, tab,
                         transformer, action).fork();
                }
                // 处理当前任务的节点
                for (Node<K,V> p; (p = advance()) != null; ) {
                    U u;
                    if ((u = transformer.apply(p)) != null)
                        action.accept(u);
                }
                // 传播完成状态
                propagateCompletion();
            }
        }
    }

    @SuppressWarnings("serial")
    static final class ForEachTransformedMappingTask<K,V,U>
        extends BulkTask<K,V,Void> {
        // 转换器，用于将键值对转换为另一种类型
        final BiFunction<? super K, ? super V, ? extends U> transformer;
        // 消费者，用于处理转换后的值
        final Consumer<? super U> action;
        // 构造函数，初始化任务
        ForEachTransformedMappingTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             BiFunction<? super K, ? super V, ? extends U> transformer,
             Consumer<? super U> action) {
            super(p, b, i, f, t);
            this.transformer = transformer; this.action = action;
        }
        // 执行任务的方法
        public final void compute() {
            final BiFunction<? super K, ? super V, ? extends U> transformer;
            final Consumer<? super U> action;
            // 检查转换器和消费者是否不为空
            if ((transformer = this.transformer) != null &&
                (action = this.action) != null) {
                // 分割任务
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    new ForEachTransformedMappingTask<K,V,U>
                        (this, batch >>>= 1, baseLimit = h, f, tab,
                         transformer, action).fork();
                }
                // 处理当前任务的节点
                for (Node<K,V> p; (p = advance()) != null; ) {
                    U u;
                    if ((u = transformer.apply(p.key, p.val)) != null)
                        action.accept(u);
                }
                // 传播完成状态
                propagateCompletion();
            }
        }
    }

    @SuppressWarnings("serial")
    static final class SearchKeysTask<K,V,U>
        extends BulkTask<K,V,U> {
        // 搜索函数，用于从键中查找值
        final Function<? super K, ? extends U> searchFunction;
        // 结果引用，用于存储搜索结果
        final AtomicReference<U> result;
        // 构造函数，初始化任务
        SearchKeysTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             Function<? super K, ? extends U> searchFunction,
             AtomicReference<U> result) {
            super(p, b, i, f, t);
            this.searchFunction = searchFunction; this.result = result;
        }
        // 获取原始结果
        public final U getRawResult() { return result.get(); }
        // 执行任务的方法
        public final void compute() {
            final Function<? super K, ? extends U> searchFunction;
            final AtomicReference<U> result;
            // 检查搜索函数和结果引用是否不为空
            if ((searchFunction = this.searchFunction) != null &&
                (result = this.result) != null) {
                // 分割任务
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    if (result.get() != null)
                        return;
                    addToPendingCount(1);
                    new SearchKeysTask<K,V,U>
                        (this, batch >>>= 1, baseLimit = h, f, tab,
                         searchFunction, result).fork();
                }
                // 处理当前任务的节点
                while (result.get() == null) {
                    U u;
                    Node<K,V> p;
                    if ((p = advance()) == null) {
                        propagateCompletion();
                        break;
                    }
                    if ((u = searchFunction.apply(p.key)) != null) {
                        if (result.compareAndSet(null, u))
                            quietlyCompleteRoot();
                        break;
                    }
                }
            }
        }
    }


                @SuppressWarnings("serial")
    static final class SearchValuesTask<K,V,U>
        extends BulkTask<K,V,U> {
        final Function<? super V, ? extends U> searchFunction;
        final AtomicReference<U> result;
        SearchValuesTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             Function<? super V, ? extends U> searchFunction,
             AtomicReference<U> result) {
            super(p, b, i, f, t);
            this.searchFunction = searchFunction; this.result = result;
        }
        public final U getRawResult() { return result.get(); }
        public final void compute() {
            final Function<? super V, ? extends U> searchFunction;
            final AtomicReference<U> result;
            if ((searchFunction = this.searchFunction) != null &&
                (result = this.result) != null) {
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    if (result.get() != null)
                        return;
                    addToPendingCount(1);
                    new SearchValuesTask<K,V,U>
                        (this, batch >>>= 1, baseLimit = h, f, tab,
                         searchFunction, result).fork();
                }
                while (result.get() == null) {
                    U u;
                    Node<K,V> p;
                    if ((p = advance()) == null) {
                        propagateCompletion();
                        break;
                    }
                    if ((u = searchFunction.apply(p.val)) != null) {
                        if (result.compareAndSet(null, u))
                            quietlyCompleteRoot();
                        break;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
    static final class SearchEntriesTask<K,V,U>
        extends BulkTask<K,V,U> {
        final Function<Entry<K,V>, ? extends U> searchFunction;
        final AtomicReference<U> result;
        SearchEntriesTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             Function<Entry<K,V>, ? extends U> searchFunction,
             AtomicReference<U> result) {
            super(p, b, i, f, t);
            this.searchFunction = searchFunction; this.result = result;
        }
        public final U getRawResult() { return result.get(); }
        public final void compute() {
            final Function<Entry<K,V>, ? extends U> searchFunction;
            final AtomicReference<U> result;
            if ((searchFunction = this.searchFunction) != null &&
                (result = this.result) != null) {
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    if (result.get() != null)
                        return;
                    addToPendingCount(1);
                    new SearchEntriesTask<K,V,U>
                        (this, batch >>>= 1, baseLimit = h, f, tab,
                         searchFunction, result).fork();
                }
                while (result.get() == null) {
                    U u;
                    Node<K,V> p;
                    if ((p = advance()) == null) {
                        propagateCompletion();
                        break;
                    }
                    if ((u = searchFunction.apply(p)) != null) {
                        if (result.compareAndSet(null, u))
                            quietlyCompleteRoot();
                        return;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
    static final class SearchMappingsTask<K,V,U>
        extends BulkTask<K,V,U> {
        final BiFunction<? super K, ? super V, ? extends U> searchFunction;
        final AtomicReference<U> result;
        SearchMappingsTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             BiFunction<? super K, ? super V, ? extends U> searchFunction,
             AtomicReference<U> result) {
            super(p, b, i, f, t);
            this.searchFunction = searchFunction; this.result = result;
        }
        public final U getRawResult() { return result.get(); }
        public final void compute() {
            final BiFunction<? super K, ? super V, ? extends U> searchFunction;
            final AtomicReference<U> result;
            if ((searchFunction = this.searchFunction) != null &&
                (result = this.result) != null) {
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    if (result.get() != null)
                        return;
                    addToPendingCount(1);
                    new SearchMappingsTask<K,V,U>
                        (this, batch >>>= 1, baseLimit = h, f, tab,
                         searchFunction, result).fork();
                }
                while (result.get() == null) {
                    U u;
                    Node<K,V> p;
                    if ((p = advance()) == null) {
                        propagateCompletion();
                        break;
                    }
                    if ((u = searchFunction.apply(p.key, p.val)) != null) {
                        if (result.compareAndSet(null, u))
                            quietlyCompleteRoot();
                        break;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
    static final class ReduceKeysTask<K,V>
        extends BulkTask<K,V,K> {
        final BiFunction<? super K, ? super K, ? extends K> reducer;
        K result;
        ReduceKeysTask<K,V> rights, nextRight;
        ReduceKeysTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             ReduceKeysTask<K,V> nextRight,
             BiFunction<? super K, ? super K, ? extends K> reducer) {
            super(p, b, i, f, t); this.nextRight = nextRight;
            this.reducer = reducer;
        }
        public final K getRawResult() { return result; }
        public final void compute() {
            final BiFunction<? super K, ? super K, ? extends K> reducer;
            if ((reducer = this.reducer) != null) {
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    (rights = new ReduceKeysTask<K,V>
                     (this, batch >>>= 1, baseLimit = h, f, tab,
                      rights, reducer)).fork();
                }
                K r = null;
                for (Node<K,V> p; (p = advance()) != null; ) {
                    K u = p.key;
                    r = (r == null) ? u : u == null ? r : reducer.apply(r, u);
                }
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
                    ReduceKeysTask<K,V>
                        t = (ReduceKeysTask<K,V>)c,
                        s = t.rights;
                    while (s != null) {
                        K tr, sr;
                        if ((sr = s.result) != null)
                            t.result = (((tr = t.result) == null) ? sr :
                                        reducer.apply(tr, sr));
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }


                @SuppressWarnings("serial")
    static final class ReduceValuesTask<K,V>
        extends BulkTask<K,V,V> {
        final BiFunction<? super V, ? super V, ? extends V> reducer;
        V result;
        ReduceValuesTask<K,V> rights, nextRight;
        ReduceValuesTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             ReduceValuesTask<K,V> nextRight,
             BiFunction<? super V, ? super V, ? extends V> reducer) {
            super(p, b, i, f, t); this.nextRight = nextRight;
            this.reducer = reducer;
        }
        public final V getRawResult() { return result; }
        public final void compute() {
            final BiFunction<? super V, ? super V, ? extends V> reducer;
            if ((reducer = this.reducer) != null) {
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    (rights = new ReduceValuesTask<K,V>
                     (this, batch >>>= 1, baseLimit = h, f, tab,
                      rights, reducer)).fork();
                }
                V r = null;
                for (Node<K,V> p; (p = advance()) != null; ) {
                    V v = p.val;
                    r = (r == null) ? v : reducer.apply(r, v);
                }
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
                    ReduceValuesTask<K,V>
                        t = (ReduceValuesTask<K,V>)c,
                        s = t.rights;
                    while (s != null) {
                        V tr, sr;
                        if ((sr = s.result) != null)
                            t.result = (((tr = t.result) == null) ? sr :
                                        reducer.apply(tr, sr));
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
    static final class ReduceEntriesTask<K,V>
        extends BulkTask<K,V,Map.Entry<K,V>> {
        final BiFunction<Map.Entry<K,V>, Map.Entry<K,V>, ? extends Map.Entry<K,V>> reducer;
        Map.Entry<K,V> result;
        ReduceEntriesTask<K,V> rights, nextRight;
        ReduceEntriesTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             ReduceEntriesTask<K,V> nextRight,
             BiFunction<Entry<K,V>, Map.Entry<K,V>, ? extends Map.Entry<K,V>> reducer) {
            super(p, b, i, f, t); this.nextRight = nextRight;
            this.reducer = reducer;
        }
        public final Map.Entry<K,V> getRawResult() { return result; }
        public final void compute() {
            final BiFunction<Map.Entry<K,V>, Map.Entry<K,V>, ? extends Map.Entry<K,V>> reducer;
            if ((reducer = this.reducer) != null) {
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    (rights = new ReduceEntriesTask<K,V>
                     (this, batch >>>= 1, baseLimit = h, f, tab,
                      rights, reducer)).fork();
                }
                Map.Entry<K,V> r = null;
                for (Node<K,V> p; (p = advance()) != null; )
                    r = (r == null) ? p : reducer.apply(r, p);
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
                    ReduceEntriesTask<K,V>
                        t = (ReduceEntriesTask<K,V>)c,
                        s = t.rights;
                    while (s != null) {
                        Map.Entry<K,V> tr, sr;
                        if ((sr = s.result) != null)
                            t.result = (((tr = t.result) == null) ? sr :
                                        reducer.apply(tr, sr));
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
    static final class MapReduceKeysTask<K,V,U>
        extends BulkTask<K,V,U> {
        final Function<? super K, ? extends U> transformer;
        final BiFunction<? super U, ? super U, ? extends U> reducer;
        U result;
        MapReduceKeysTask<K,V,U> rights, nextRight;
        MapReduceKeysTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             MapReduceKeysTask<K,V,U> nextRight,
             Function<? super K, ? extends U> transformer,
             BiFunction<? super U, ? super U, ? extends U> reducer) {
            super(p, b, i, f, t); this.nextRight = nextRight;
            this.transformer = transformer;
            this.reducer = reducer;
        }
        public final U getRawResult() { return result; }
        public final void compute() {
            final Function<? super K, ? extends U> transformer;
            final BiFunction<? super U, ? super U, ? extends U> reducer;
            if ((transformer = this.transformer) != null &&
                (reducer = this.reducer) != null) {
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    (rights = new MapReduceKeysTask<K,V,U>
                     (this, batch >>>= 1, baseLimit = h, f, tab,
                      rights, transformer, reducer)).fork();
                }
                U r = null;
                for (Node<K,V> p; (p = advance()) != null; ) {
                    U u;
                    if ((u = transformer.apply(p.key)) != null)
                        r = (r == null) ? u : reducer.apply(r, u);
                }
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
                    MapReduceKeysTask<K,V,U>
                        t = (MapReduceKeysTask<K,V,U>)c,
                        s = t.rights;
                    while (s != null) {
                        U tr, sr;
                        if ((sr = s.result) != null)
                            t.result = (((tr = t.result) == null) ? sr :
                                        reducer.apply(tr, sr));
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }


                @SuppressWarnings("serial")
    static final class MapReduceValuesTask<K,V,U>
        extends BulkTask<K,V,U> {
        final Function<? super V, ? extends U> transformer;
        final BiFunction<? super U, ? super U, ? extends U> reducer;
        U result;
        MapReduceValuesTask<K,V,U> rights, nextRight;
        MapReduceValuesTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             MapReduceValuesTask<K,V,U> nextRight,
             Function<? super V, ? extends U> transformer,
             BiFunction<? super U, ? super U, ? extends U> reducer) {
            super(p, b, i, f, t); this.nextRight = nextRight;
            this.transformer = transformer;
            this.reducer = reducer;
        }
        public final U getRawResult() { return result; }
        public final void compute() {
            final Function<? super V, ? extends U> transformer;
            final BiFunction<? super U, ? super U, ? extends U> reducer;
            if ((transformer = this.transformer) != null &&
                (reducer = this.reducer) != null) {
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    (rights = new MapReduceValuesTask<K,V,U>
                     (this, batch >>>= 1, baseLimit = h, f, tab,
                      rights, transformer, reducer)).fork();
                }
                U r = null;
                for (Node<K,V> p; (p = advance()) != null; ) {
                    U u;
                    if ((u = transformer.apply(p.val)) != null)
                        r = (r == null) ? u : reducer.apply(r, u);
                }
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
                    MapReduceValuesTask<K,V,U>
                        t = (MapReduceValuesTask<K,V,U>)c,
                        s = t.rights;
                    while (s != null) {
                        U tr, sr;
                        if ((sr = s.result) != null)
                            t.result = (((tr = t.result) == null) ? sr :
                                        reducer.apply(tr, sr));
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
    static final class MapReduceEntriesTask<K,V,U>
        extends BulkTask<K,V,U> {
        final Function<Map.Entry<K,V>, ? extends U> transformer;
        final BiFunction<? super U, ? super U, ? extends U> reducer;
        U result;
        MapReduceEntriesTask<K,V,U> rights, nextRight;
        MapReduceEntriesTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             MapReduceEntriesTask<K,V,U> nextRight,
             Function<Map.Entry<K,V>, ? extends U> transformer,
             BiFunction<? super U, ? super U, ? extends U> reducer) {
            super(p, b, i, f, t); this.nextRight = nextRight;
            this.transformer = transformer;
            this.reducer = reducer;
        }
        public final U getRawResult() { return result; }
        public final void compute() {
            final Function<Map.Entry<K,V>, ? extends U> transformer;
            final BiFunction<? super U, ? super U, ? extends U> reducer;
            if ((transformer = this.transformer) != null &&
                (reducer = this.reducer) != null) {
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    (rights = new MapReduceEntriesTask<K,V,U>
                     (this, batch >>>= 1, baseLimit = h, f, tab,
                      rights, transformer, reducer)).fork();
                }
                U r = null;
                for (Node<K,V> p; (p = advance()) != null; ) {
                    U u;
                    if ((u = transformer.apply(p)) != null)
                        r = (r == null) ? u : reducer.apply(r, u);
                }
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
                    MapReduceEntriesTask<K,V,U>
                        t = (MapReduceEntriesTask<K,V,U>)c,
                        s = t.rights;
                    while (s != null) {
                        U tr, sr;
                        if ((sr = s.result) != null)
                            t.result = (((tr = t.result) == null) ? sr :
                                        reducer.apply(tr, sr));
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
    static final class MapReduceMappingsTask<K,V,U>
        extends BulkTask<K,V,U> {
        final BiFunction<? super K, ? super V, ? extends U> transformer;
        final BiFunction<? super U, ? super U, ? extends U> reducer;
        U result;
        MapReduceMappingsTask<K,V,U> rights, nextRight;
        MapReduceMappingsTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             MapReduceMappingsTask<K,V,U> nextRight,
             BiFunction<? super K, ? super V, ? extends U> transformer,
             BiFunction<? super U, ? super U, ? extends U> reducer) {
            super(p, b, i, f, t); this.nextRight = nextRight;
            this.transformer = transformer;
            this.reducer = reducer;
        }
        public final U getRawResult() { return result; }
        public final void compute() {
            final BiFunction<? super K, ? super V, ? extends U> transformer;
            final BiFunction<? super U, ? super U, ? extends U> reducer;
            if ((transformer = this.transformer) != null &&
                (reducer = this.reducer) != null) {
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    (rights = new MapReduceMappingsTask<K,V,U>
                     (this, batch >>>= 1, baseLimit = h, f, tab,
                      rights, transformer, reducer)).fork();
                }
                U r = null;
                for (Node<K,V> p; (p = advance()) != null; ) {
                    U u;
                    if ((u = transformer.apply(p.key, p.val)) != null)
                        r = (r == null) ? u : reducer.apply(r, u);
                }
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
                    MapReduceMappingsTask<K,V,U>
                        t = (MapReduceMappingsTask<K,V,U>)c,
                        s = t.rights;
                    while (s != null) {
                        U tr, sr;
                        if ((sr = s.result) != null)
                            t.result = (((tr = t.result) == null) ? sr :
                                        reducer.apply(tr, sr));
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }


                @SuppressWarnings("serial")
    static final class MapReduceKeysToDoubleTask<K,V>
        extends BulkTask<K,V,Double> {
        // 转换器，用于将键转换为double值
        final ToDoubleFunction<? super K> transformer;
        // 用于合并结果的二元操作
        final DoubleBinaryOperator reducer;
        // 初始值
        final double basis;
        // 任务的计算结果
        double result;
        // 右侧子任务
        MapReduceKeysToDoubleTask<K,V> rights, nextRight;
        // 构造函数
        MapReduceKeysToDoubleTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             MapReduceKeysToDoubleTask<K,V> nextRight,
             ToDoubleFunction<? super K> transformer,
             double basis,
             DoubleBinaryOperator reducer) {
            super(p, b, i, f, t); this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis; this.reducer = reducer;
        }
        // 获取计算结果
        public final Double getRawResult() { return result; }
        // 执行任务
        public final void compute() {
            final ToDoubleFunction<? super K> transformer;
            final DoubleBinaryOperator reducer;
            if ((transformer = this.transformer) != null &&
                (reducer = this.reducer) != null) {
                double r = this.basis;
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    (rights = new MapReduceKeysToDoubleTask<K,V>
                     (this, batch >>>= 1, baseLimit = h, f, tab,
                      rights, transformer, r, reducer)).fork();
                }
                for (Node<K,V> p; (p = advance()) != null; )
                    r = reducer.applyAsDouble(r, transformer.applyAsDouble(p.key));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
                    MapReduceKeysToDoubleTask<K,V>
                        t = (MapReduceKeysToDoubleTask<K,V>)c,
                        s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsDouble(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
    static final class MapReduceValuesToDoubleTask<K,V>
        extends BulkTask<K,V,Double> {
        // 转换器，用于将值转换为double值
        final ToDoubleFunction<? super V> transformer;
        // 用于合并结果的二元操作
        final DoubleBinaryOperator reducer;
        // 初始值
        final double basis;
        // 任务的计算结果
        double result;
        // 右侧子任务
        MapReduceValuesToDoubleTask<K,V> rights, nextRight;
        // 构造函数
        MapReduceValuesToDoubleTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             MapReduceValuesToDoubleTask<K,V> nextRight,
             ToDoubleFunction<? super V> transformer,
             double basis,
             DoubleBinaryOperator reducer) {
            super(p, b, i, f, t); this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis; this.reducer = reducer;
        }
        // 获取计算结果
        public final Double getRawResult() { return result; }
        // 执行任务
        public final void compute() {
            final ToDoubleFunction<? super V> transformer;
            final DoubleBinaryOperator reducer;
            if ((transformer = this.transformer) != null &&
                (reducer = this.reducer) != null) {
                double r = this.basis;
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    (rights = new MapReduceValuesToDoubleTask<K,V>
                     (this, batch >>>= 1, baseLimit = h, f, tab,
                      rights, transformer, r, reducer)).fork();
                }
                for (Node<K,V> p; (p = advance()) != null; )
                    r = reducer.applyAsDouble(r, transformer.applyAsDouble(p.val));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
                    MapReduceValuesToDoubleTask<K,V>
                        t = (MapReduceValuesToDoubleTask<K,V>)c,
                        s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsDouble(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
    static final class MapReduceEntriesToDoubleTask<K,V>
        extends BulkTask<K,V,Double> {
        // 转换器，用于将条目转换为double值
        final ToDoubleFunction<Map.Entry<K,V>> transformer;
        // 用于合并结果的二元操作
        final DoubleBinaryOperator reducer;
        // 初始值
        final double basis;
        // 任务的计算结果
        double result;
        // 右侧子任务
        MapReduceEntriesToDoubleTask<K,V> rights, nextRight;
        // 构造函数
        MapReduceEntriesToDoubleTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             MapReduceEntriesToDoubleTask<K,V> nextRight,
             ToDoubleFunction<Map.Entry<K,V>> transformer,
             double basis,
             DoubleBinaryOperator reducer) {
            super(p, b, i, f, t); this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis; this.reducer = reducer;
        }
        // 获取计算结果
        public final Double getRawResult() { return result; }
        // 执行任务
        public final void compute() {
            final ToDoubleFunction<Map.Entry<K,V>> transformer;
            final DoubleBinaryOperator reducer;
            if ((transformer = this.transformer) != null &&
                (reducer = this.reducer) != null) {
                double r = this.basis;
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    (rights = new MapReduceEntriesToDoubleTask<K,V>
                     (this, batch >>>= 1, baseLimit = h, f, tab,
                      rights, transformer, r, reducer)).fork();
                }
                for (Node<K,V> p; (p = advance()) != null; )
                    r = reducer.applyAsDouble(r, transformer.applyAsDouble(p));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
                    MapReduceEntriesToDoubleTask<K,V>
                        t = (MapReduceEntriesToDoubleTask<K,V>)c,
                        s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsDouble(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }


                @SuppressWarnings("serial")
    static final class MapReduceMappingsToDoubleTask<K,V>
        extends BulkTask<K,V,Double> {
        // 转换器，用于将键值对转换为 double 值
        final ToDoubleBiFunction<? super K, ? super V> transformer;
        // 用于合并两个 double 值的二元操作符
        final DoubleBinaryOperator reducer;
        // 初始值
        final double basis;
        // 结果
        double result;
        // 右子任务链
        MapReduceMappingsToDoubleTask<K,V> rights, nextRight;
        // 构造函数
        MapReduceMappingsToDoubleTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             MapReduceMappingsToDoubleTask<K,V> nextRight,
             ToDoubleBiFunction<? super K, ? super V> transformer,
             double basis,
             DoubleBinaryOperator reducer) {
            super(p, b, i, f, t); this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis; this.reducer = reducer;
        }
        // 获取原始结果
        public final Double getRawResult() { return result; }
        // 计算方法
        public final void compute() {
            final ToDoubleBiFunction<? super K, ? super V> transformer;
            final DoubleBinaryOperator reducer;
            if ((transformer = this.transformer) != null &&
                (reducer = this.reducer) != null) {
                double r = this.basis;
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    (rights = new MapReduceMappingsToDoubleTask<K,V>
                     (this, batch >>>= 1, baseLimit = h, f, tab,
                      rights, transformer, r, reducer)).fork();
                }
                for (Node<K,V> p; (p = advance()) != null; )
                    r = reducer.applyAsDouble(r, transformer.applyAsDouble(p.key, p.val));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
                    MapReduceMappingsToDoubleTask<K,V>
                        t = (MapReduceMappingsToDoubleTask<K,V>)c,
                        s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsDouble(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
    static final class MapReduceKeysToLongTask<K,V>
        extends BulkTask<K,V,Long> {
        // 转换器，用于将键转换为 long 值
        final ToLongFunction<? super K> transformer;
        // 用于合并两个 long 值的二元操作符
        final LongBinaryOperator reducer;
        // 初始值
        final long basis;
        // 结果
        long result;
        // 右子任务链
        MapReduceKeysToLongTask<K,V> rights, nextRight;
        // 构造函数
        MapReduceKeysToLongTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             MapReduceKeysToLongTask<K,V> nextRight,
             ToLongFunction<? super K> transformer,
             long basis,
             LongBinaryOperator reducer) {
            super(p, b, i, f, t); this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis; this.reducer = reducer;
        }
        // 获取原始结果
        public final Long getRawResult() { return result; }
        // 计算方法
        public final void compute() {
            final ToLongFunction<? super K> transformer;
            final LongBinaryOperator reducer;
            if ((transformer = this.transformer) != null &&
                (reducer = this.reducer) != null) {
                long r = this.basis;
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    (rights = new MapReduceKeysToLongTask<K,V>
                     (this, batch >>>= 1, baseLimit = h, f, tab,
                      rights, transformer, r, reducer)).fork();
                }
                for (Node<K,V> p; (p = advance()) != null; )
                    r = reducer.applyAsLong(r, transformer.applyAsLong(p.key));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
                    MapReduceKeysToLongTask<K,V>
                        t = (MapReduceKeysToLongTask<K,V>)c,
                        s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsLong(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
    static final class MapReduceValuesToLongTask<K,V>
        extends BulkTask<K,V,Long> {
        // 转换器，用于将值转换为 long 值
        final ToLongFunction<? super V> transformer;
        // 用于合并两个 long 值的二元操作符
        final LongBinaryOperator reducer;
        // 初始值
        final long basis;
        // 结果
        long result;
        // 右子任务链
        MapReduceValuesToLongTask<K,V> rights, nextRight;
        // 构造函数
        MapReduceValuesToLongTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             MapReduceValuesToLongTask<K,V> nextRight,
             ToLongFunction<? super V> transformer,
             long basis,
             LongBinaryOperator reducer) {
            super(p, b, i, f, t); this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis; this.reducer = reducer;
        }
        // 获取原始结果
        public final Long getRawResult() { return result; }
        // 计算方法
        public final void compute() {
            final ToLongFunction<? super V> transformer;
            final LongBinaryOperator reducer;
            if ((transformer = this.transformer) != null &&
                (reducer = this.reducer) != null) {
                long r = this.basis;
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    (rights = new MapReduceValuesToLongTask<K,V>
                     (this, batch >>>= 1, baseLimit = h, f, tab,
                      rights, transformer, r, reducer)).fork();
                }
                for (Node<K,V> p; (p = advance()) != null; )
                    r = reducer.applyAsLong(r, transformer.applyAsLong(p.val));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
                    MapReduceValuesToLongTask<K,V>
                        t = (MapReduceValuesToLongTask<K,V>)c,
                        s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsLong(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }


                @SuppressWarnings("serial")
    static final class MapReduceEntriesToLongTask<K,V>
        extends BulkTask<K,V,Long> {
        final ToLongFunction<Map.Entry<K,V>> transformer; // 转换器，将 Map.Entry<K,V> 转换为 long
        final LongBinaryOperator reducer; // 用于合并结果的二元操作符
        final long basis; // 基础值，用于初始化结果
        long result; // 最终结果
        MapReduceEntriesToLongTask<K,V> rights, nextRight; // 右侧任务和下一个右侧任务

        MapReduceEntriesToLongTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             MapReduceEntriesToLongTask<K,V> nextRight,
             ToLongFunction<Map.Entry<K,V>> transformer,
             long basis,
             LongBinaryOperator reducer) {
            super(p, b, i, f, t); this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis; this.reducer = reducer;
        }

        public final Long getRawResult() { return result; } // 获取最终结果

        public final void compute() {
            final ToLongFunction<Map.Entry<K,V>> transformer;
            final LongBinaryOperator reducer;
            if ((transformer = this.transformer) != null &&
                (reducer = this.reducer) != null) {
                long r = this.basis;
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    (rights = new MapReduceEntriesToLongTask<K,V>
                     (this, batch >>>= 1, baseLimit = h, f, tab,
                      rights, transformer, r, reducer)).fork();
                }
                for (Node<K,V> p; (p = advance()) != null; )
                    r = reducer.applyAsLong(r, transformer.applyAsLong(p));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
                    MapReduceEntriesToLongTask<K,V>
                        t = (MapReduceEntriesToLongTask<K,V>)c,
                        s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsLong(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
    static final class MapReduceMappingsToLongTask<K,V>
        extends BulkTask<K,V,Long> {
        final ToLongBiFunction<? super K, ? super V> transformer; // 转换器，将 (K, V) 转换为 long
        final LongBinaryOperator reducer; // 用于合并结果的二元操作符
        final long basis; // 基础值，用于初始化结果
        long result; // 最终结果
        MapReduceMappingsToLongTask<K,V> rights, nextRight; // 右侧任务和下一个右侧任务

        MapReduceMappingsToLongTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             MapReduceMappingsToLongTask<K,V> nextRight,
             ToLongBiFunction<? super K, ? super V> transformer,
             long basis,
             LongBinaryOperator reducer) {
            super(p, b, i, f, t); this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis; this.reducer = reducer;
        }

        public final Long getRawResult() { return result; } // 获取最终结果

        public final void compute() {
            final ToLongBiFunction<? super K, ? super V> transformer;
            final LongBinaryOperator reducer;
            if ((transformer = this.transformer) != null &&
                (reducer = this.reducer) != null) {
                long r = this.basis;
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    (rights = new MapReduceMappingsToLongTask<K,V>
                     (this, batch >>>= 1, baseLimit = h, f, tab,
                      rights, transformer, r, reducer)).fork();
                }
                for (Node<K,V> p; (p = advance()) != null; )
                    r = reducer.applyAsLong(r, transformer.applyAsLong(p.key, p.val));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
                    MapReduceMappingsToLongTask<K,V>
                        t = (MapReduceMappingsToLongTask<K,V>)c,
                        s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsLong(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
    static final class MapReduceKeysToIntTask<K,V>
        extends BulkTask<K,V,Integer> {
        final ToIntFunction<? super K> transformer; // 转换器，将 K 转换为 int
        final IntBinaryOperator reducer; // 用于合并结果的二元操作符
        final int basis; // 基础值，用于初始化结果
        int result; // 最终结果
        MapReduceKeysToIntTask<K,V> rights, nextRight; // 右侧任务和下一个右侧任务

        MapReduceKeysToIntTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             MapReduceKeysToIntTask<K,V> nextRight,
             ToIntFunction<? super K> transformer,
             int basis,
             IntBinaryOperator reducer) {
            super(p, b, i, f, t); this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis; this.reducer = reducer;
        }

        public final Integer getRawResult() { return result; } // 获取最终结果

        public final void compute() {
            final ToIntFunction<? super K> transformer;
            final IntBinaryOperator reducer;
            if ((transformer = this.transformer) != null &&
                (reducer = this.reducer) != null) {
                int r = this.basis;
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    (rights = new MapReduceKeysToIntTask<K,V>
                     (this, batch >>>= 1, baseLimit = h, f, tab,
                      rights, transformer, r, reducer)).fork();
                }
                for (Node<K,V> p; (p = advance()) != null; )
                    r = reducer.applyAsInt(r, transformer.applyAsInt(p.key));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
                    MapReduceKeysToIntTask<K,V>
                        t = (MapReduceKeysToIntTask<K,V>)c,
                        s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsInt(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }


                @SuppressWarnings("serial")
    static final class MapReduceValuesToIntTask<K,V>
        extends BulkTask<K,V,Integer> {
        final ToIntFunction<? super V> transformer; // 转换函数，将V转换为int
        final IntBinaryOperator reducer; // 用于合并结果的二元操作符
        final int basis; // 初始值
        int result; // 任务的结果
        MapReduceValuesToIntTask<K,V> rights, nextRight; // 右侧子任务和下一个右侧子任务

        MapReduceValuesToIntTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             MapReduceValuesToIntTask<K,V> nextRight,
             ToIntFunction<? super V> transformer,
             int basis,
             IntBinaryOperator reducer) {
            super(p, b, i, f, t); this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis; this.reducer = reducer;
        }
        public final Integer getRawResult() { return result; } // 获取任务的原始结果
        public final void compute() {
            final ToIntFunction<? super V> transformer;
            final IntBinaryOperator reducer;
            if ((transformer = this.transformer) != null &&
                (reducer = this.reducer) != null) {
                int r = this.basis;
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    (rights = new MapReduceValuesToIntTask<K,V>
                     (this, batch >>>= 1, baseLimit = h, f, tab,
                      rights, transformer, r, reducer)).fork();
                }
                for (Node<K,V> p; (p = advance()) != null; )
                    r = reducer.applyAsInt(r, transformer.applyAsInt(p.val));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
                    MapReduceValuesToIntTask<K,V>
                        t = (MapReduceValuesToIntTask<K,V>)c,
                        s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsInt(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
    static final class MapReduceEntriesToIntTask<K,V>
        extends BulkTask<K,V,Integer> {
        final ToIntFunction<Map.Entry<K,V>> transformer; // 转换函数，将Map.Entry<K,V>转换为int
        final IntBinaryOperator reducer; // 用于合并结果的二元操作符
        final int basis; // 初始值
        int result; // 任务的结果
        MapReduceEntriesToIntTask<K,V> rights, nextRight; // 右侧子任务和下一个右侧子任务

        MapReduceEntriesToIntTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             MapReduceEntriesToIntTask<K,V> nextRight,
             ToIntFunction<Map.Entry<K,V>> transformer,
             int basis,
             IntBinaryOperator reducer) {
            super(p, b, i, f, t); this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis; this.reducer = reducer;
        }
        public final Integer getRawResult() { return result; } // 获取任务的原始结果
        public final void compute() {
            final ToIntFunction<Map.Entry<K,V>> transformer;
            final IntBinaryOperator reducer;
            if ((transformer = this.transformer) != null &&
                (reducer = this.reducer) != null) {
                int r = this.basis;
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    (rights = new MapReduceEntriesToIntTask<K,V>
                     (this, batch >>>= 1, baseLimit = h, f, tab,
                      rights, transformer, r, reducer)).fork();
                }
                for (Node<K,V> p; (p = advance()) != null; )
                    r = reducer.applyAsInt(r, transformer.applyAsInt(p));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
                    MapReduceEntriesToIntTask<K,V>
                        t = (MapReduceEntriesToIntTask<K,V>)c,
                        s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsInt(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
    static final class MapReduceMappingsToIntTask<K,V>
        extends BulkTask<K,V,Integer> {
        final ToIntBiFunction<? super K, ? super V> transformer; // 转换函数，将(K, V)对转换为int
        final IntBinaryOperator reducer; // 用于合并结果的二元操作符
        final int basis; // 初始值
        int result; // 任务的结果
        MapReduceMappingsToIntTask<K,V> rights, nextRight; // 右侧子任务和下一个右侧子任务

        MapReduceMappingsToIntTask
            (BulkTask<K,V,?> p, int b, int i, int f, Node<K,V>[] t,
             MapReduceMappingsToIntTask<K,V> nextRight,
             ToIntBiFunction<? super K, ? super V> transformer,
             int basis,
             IntBinaryOperator reducer) {
            super(p, b, i, f, t); this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis; this.reducer = reducer;
        }
        public final Integer getRawResult() { return result; } // 获取任务的原始结果
        public final void compute() {
            final ToIntBiFunction<? super K, ? super V> transformer;
            final IntBinaryOperator reducer;
            if ((transformer = this.transformer) != null &&
                (reducer = this.reducer) != null) {
                int r = this.basis;
                for (int i = baseIndex, f, h; batch > 0 &&
                         (h = ((f = baseLimit) + i) >>> 1) > i;) {
                    addToPendingCount(1);
                    (rights = new MapReduceMappingsToIntTask<K,V>
                     (this, batch >>>= 1, baseLimit = h, f, tab,
                      rights, transformer, r, reducer)).fork();
                }
                for (Node<K,V> p; (p = advance()) != null; )
                    r = reducer.applyAsInt(r, transformer.applyAsInt(p.key, p.val));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
                    MapReduceMappingsToIntTask<K,V>
                        t = (MapReduceMappingsToIntTask<K,V>)c,
                        s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsInt(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

                // 不安全机制
    private static final sun.misc.Unsafe U;
    private static final long SIZECTL;
    private static final long TRANSFERINDEX;
    private static final long BASECOUNT;
    private static final long CELLSBUSY;
    private static final long CELLVALUE;
    private static final long ABASE;
    private static final int ASHIFT;

    static {
        try {
            U = sun.misc.Unsafe.getUnsafe();
            Class<?> k = ConcurrentHashMap.class;
            SIZECTL = U.objectFieldOffset
                (k.getDeclaredField("sizeCtl"));
            TRANSFERINDEX = U.objectFieldOffset
                (k.getDeclaredField("transferIndex"));
            BASECOUNT = U.objectFieldOffset
                (k.getDeclaredField("baseCount"));
            CELLSBUSY = U.objectFieldOffset
                (k.getDeclaredField("cellsBusy"));
            Class<?> ck = CounterCell.class;
            CELLVALUE = U.objectFieldOffset
                (ck.getDeclaredField("value"));
            Class<?> ak = Node[].class;
            ABASE = U.arrayBaseOffset(ak);
            int scale = U.arrayIndexScale(ak);
            if ((scale & (scale - 1)) != 0)
                throw new Error("数据类型缩放不是2的幂");
            ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
