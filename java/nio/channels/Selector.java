
/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.channels;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;


/**
 * 一个 {@link SelectableChannel} 对象的多路复用器。
 *
 * <p> 可以通过调用此类的 {@link #open open} 方法来创建一个选择器，该方法将使用系统的默认 {@link
 * java.nio.channels.spi.SelectorProvider 选择器提供者} 来创建一个新的选择器。也可以通过调用自定义选择器提供者的
 * {@link java.nio.channels.spi.SelectorProvider#openSelector openSelector}
 * 方法来创建一个选择器。选择器保持打开状态，直到通过其 {@link #close close} 方法关闭。
 *
 * <a name="ks"></a>
 *
 * <p> 一个可选择通道在选择器上的注册由一个 {@link SelectionKey} 对象表示。选择器维护三个选择键集：
 *
 * <ul>
 *
 *   <li><p> <i>键集</i> 包含表示此选择器当前通道注册的键。该集由 {@link #keys() keys} 方法返回。 </p></li>
 *
 *   <li><p> <i>已选择键集</i> 是那些在先前的选择操作中检测到其通道至少对键的兴趣集中标识的操作之一准备好的键的集合。
 *   该集由 {@link #selectedKeys() selectedKeys} 方法返回。已选择键集始终是键集的子集。 </p></li>
 *
 *   <li><p> <i>已取消键集</i> 是那些已被取消但其通道尚未注销的键的集合。该集不可直接访问。已取消键集始终是键集的子集。 </p></li>
 *
 * </ul>
 *
 * <p> 在新创建的选择器中，所有三个集都是空的。
 *
 * <p> 当通过通道的 {@link SelectableChannel#register(Selector,int)
 * register} 方法注册通道时，键将作为副作用被添加到选择器的键集中。已取消的键在选择操作期间从键集中移除。键集本身不可直接修改。
 *
 * <p> 当通过关闭其通道或调用其 {@link SelectionKey#cancel
 * cancel} 方法取消键时，键将被添加到其选择器的已取消键集中。取消键将导致其通道在下一个选择操作中注销，此时键将从选择器的所有键集中移除。
 *
 * <a name="sks"></a><p> 选择操作将键添加到已选择键集中。可以通过调用集的 {@link java.util.Set#remove(java.lang.Object) remove}
 * 方法或通过调用从集获取的 {@link java.util.Iterator iterator} 的 {@link java.util.Iterator#remove() remove} 方法直接从已选择键集中移除键。
 * 除了这些方法外，键不会以任何其他方式从已选择键集中移除；特别是，它们不会作为选择操作的副作用被移除。不能直接将键添加到已选择键集中。 </p>
 *
 *
 * <a name="selop"></a>
 * <h2>选择</h2>
 *
 * <p> 在每次选择操作期间，键可能会被添加到和从选择器的已选择键集中移除，也可能会从其键集和已取消键集中移除。选择由 {@link #select()}，{@link
 * #select(long)} 和 {@link #selectNow()} 方法执行，涉及三个步骤：
 * </p>
 *
 * <ol>
 *
 *   <li><p> 已取消键集中的每个键将从其所属的每个键集中移除，并注销其通道。这一步将使已取消键集为空。 </p></li>
 *
 *   <li><p> 查询底层操作系统以更新每个剩余通道对其键的兴趣集中标识的操作的准备情况，从选择操作开始的那一刻起。对于至少对其键的兴趣集中标识的一个操作准备好的通道，执行以下两个操作之一： </p>
 *
 *   <ol>
 *
 *     <li><p> 如果通道的键不在已选择键集中，则将其添加到该集中，并修改其准备操作集以标识通道现在报告准备好的操作。之前记录在准备集中的任何准备信息将被丢弃。 </p></li>
 *
 *     <li><p> 否则，通道的键已经在已选择键集中，因此修改其准备操作集以标识通道报告准备好的任何新操作。之前记录在准备集中的任何准备信息将被保留；换句话说，底层系统返回的准备集将与键的当前准备集进行按位或操作。 </p></li>
 *
 *   </ol>
 *
 *   如果在这一开始步骤中键集中的所有键的兴趣集都为空，则既不会更新已选择键集，也不会更新任何键的准备操作集。
 *
 *   <li><p> 如果在步骤 (2) 进行过程中有键被添加到已取消键集中，则将按照步骤 (1) 的方式处理这些键。 </p></li>
 *
 * </ol>
 *
 * <p> 选择操作是否阻塞以等待一个或多个通道变得准备好，以及如果阻塞，阻塞多长时间，是这三个选择方法之间唯一的本质区别。 </p>
 *
 *
 * <h2>并发</h2>
 *
 * <p> 选择器本身可以安全地由多个并发线程使用；然而，它们的键集不是。
 *
 * <p> 选择操作在选择器本身、键集和已选择键集上同步，顺序如上。在上述步骤 (1) 和 (3) 中，它们还在已取消键集上同步。
 *
 * <p> 在选择操作进行过程中对选择器的键的兴趣集所做的更改不会影响该操作；它们将被下一个选择操作看到。
 *
 * <p> 可以随时取消键和关闭通道。因此，选择器的键集中存在一个键并不意味着该键有效或其通道已打开。如果存在其他线程可能取消键或关闭通道的可能性，应用程序代码应小心同步并检查这些条件。
 *
 * <p> 被阻塞在 {@link #select()} 或 {@link
 * #select(long)} 方法中的线程可以通过以下三种方式之一被其他线程中断：
 *
 * <ul>
 *
 *   <li><p> 通过调用选择器的 {@link #wakeup wakeup} 方法，
 *   </p></li>
 *
 *   <li><p> 通过调用选择器的 {@link #close close} 方法，或
 *   </p></li>
 *
 *   <li><p> 通过调用被阻塞线程的 {@link
 *   java.lang.Thread#interrupt() interrupt} 方法，在这种情况下，其中断状态将被设置，并调用选择器的 {@link #wakeup wakeup}
 *   方法。 </p></li>
 *
 * </ul>
 *
 * <p> {@link #close close} 方法在选择器和所有三个键集上同步，顺序与选择操作相同。
 *
 * <a name="ksc"></a>
 *
 * <p> 选择器的键集和已选择键集通常不是安全的，不能由多个并发线程使用。如果这样的线程可能直接修改这些集之一，则应通过同步集本身来控制访问。这些集的 {@link
 * java.util.Set#iterator() iterator} 方法返回的迭代器是 <i>快速失败的：</i> 如果在创建迭代器后以任何方式修改集（除了调用迭代器自己的 {@link java.util.Iterator#remove() remove} 方法），将抛出 {@link java.util.ConcurrentModificationException}。 </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 *
 * @see SelectableChannel
 * @see SelectionKey
 */


public abstract class Selector implements Closeable {

    /**
     * 初始化此类的新实例。
     */
    protected Selector() { }

    /**
     * 打开一个选择器。
     *
     * <p> 通过调用系统默认的 {@link
     * java.nio.channels.spi.SelectorProvider} 对象的 {@link
     * java.nio.channels.spi.SelectorProvider#openSelector openSelector} 方法来创建新的选择器。 </p>
     *
     * @return  一个新的选择器
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public static Selector open() throws IOException {
        return SelectorProvider.provider().openSelector();
    }

    /**
     * 判断此选择器是否打开。
     *
     * @return <tt>true</tt> 如果且仅当此选择器打开
     */
    public abstract boolean isOpen();

    /**
     * 返回创建此通道的提供者。
     *
     * @return  创建此通道的提供者
     */
    public abstract SelectorProvider provider();

    /**
     * 返回此选择器的键集。
     *
     * <p> 键集不可直接修改。键只有在被取消并且其通道被注销后才会被移除。任何尝试修改键集的行为都会导致 {@link
     * UnsupportedOperationException} 被抛出。
     *
     * <p> 键集 <a href="#ksc">不是线程安全的</a>。 </p>
     *
     * @return  此选择器的键集
     *
     * @throws  ClosedSelectorException
     *          如果此选择器已关闭
     */
    public abstract Set<SelectionKey> keys();

    /**
     * 返回此选择器的已选择键集。
     *
     * <p> 键可以从已选择键集中移除，但不能直接添加。任何尝试向键集添加对象的行为都会导致 {@link UnsupportedOperationException} 被抛出。
     *
     * <p> 已选择键集 <a href="#ksc">不是线程安全的</a>。 </p>
     *
     * @return  此选择器的已选择键集
     *
     * @throws  ClosedSelectorException
     *          如果此选择器已关闭
     */
    public abstract Set<SelectionKey> selectedKeys();

    /**
     * 选择一组其对应通道已准备好进行 I/O 操作的键。
     *
     * <p> 此方法执行非阻塞的 <a href="#selop">选择操作</a>。如果自上次选择操作以来没有通道变得可选择，则此方法立即返回零。
     *
     * <p> 调用此方法会清除任何先前调用 {@link #wakeup wakeup} 方法的效果。 </p>
     *
     * @return  选择操作更新的键的数量，可能为零
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @throws  ClosedSelectorException
     *          如果此选择器已关闭
     */
    public abstract int selectNow() throws IOException;

    /**
     * 选择一组其对应通道已准备好进行 I/O 操作的键。
     *
     * <p> 此方法执行阻塞的 <a href="#selop">选择操作</a>。它仅在至少一个通道被选择、此选择器的 {@link #wakeup wakeup} 方法被调用、当前线程被中断或给定的超时时间到期时返回，以最先发生的情况为准。
     *
     * <p> 此方法不提供实时保证：它像调用 {@link Object#wait(long)} 方法一样调度超时。 </p>
     *
     * @param  timeout  如果为正数，则阻塞最多 <tt>timeout</tt> 毫秒，等待通道变得可选择；如果为零，则无限期阻塞；不得为负数
     *
     * @return  选择操作更新的键的数量，可能为零
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @throws  ClosedSelectorException
     *          如果此选择器已关闭
     *
     * @throws  IllegalArgumentException
     *          如果超时参数的值为负数
     */
    public abstract int select(long timeout)
        throws IOException;

    /**
     * 选择一组其对应通道已准备好进行 I/O 操作的键。
     *
     * <p> 此方法执行阻塞的 <a href="#selop">选择操作</a>。它仅在至少一个通道被选择、此选择器的 {@link #wakeup wakeup} 方法被调用或当前线程被中断时返回，以最先发生的情况为准。 </p>
     *
     * @return  选择操作更新的键的数量，可能为零
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @throws  ClosedSelectorException
     *          如果此选择器已关闭
     */
    public abstract int select() throws IOException;

    /**
     * 使尚未返回的第一个选择操作立即返回。
     *
     * <p> 如果另一个线程当前正在调用 {@link #select()} 或 {@link #select(long)} 方法，则该调用将立即返回。如果当前没有选择操作正在进行，则下一次调用这些方法将立即返回，除非在此期间调用了 {@link #selectNow()} 方法。无论如何，该调用的返回值可能不为零。随后对 {@link #select()} 或 {@link
     * #select(long)} 方法的调用将像往常一样阻塞，除非在此期间再次调用此方法。
     *
     * <p> 在两次连续的选择操作之间多次调用此方法的效果与仅调用一次相同。 </p>
     *
     * @return  此选择器
     */
    public abstract Selector wakeup();

    /**
     * 关闭此选择器。
     *
     * <p> 如果某个线程当前正在调用此选择器的某个选择方法，则该线程将被中断，如同调用了选择器的 {@link
     * #wakeup wakeup} 方法。
     *
     * <p> 与此选择器关联的任何未取消的键将被无效，其通道将被注销，并且与此选择器关联的任何其他资源将被释放。
     *
     * <p> 如果此选择器已关闭，则调用此方法没有效果。
     *
     * <p> 选择器关闭后，任何尝试使用它的行为（除了调用此方法或 {@link #wakeup wakeup} 方法）都将导致 {@link ClosedSelectorException} 被抛出。 </p>
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public abstract void close() throws IOException;

}
