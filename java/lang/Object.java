
/*
 * 版权所有 (c) 1994, 2012, Oracle 和/或其附属公司。保留所有权利。
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

package java.lang;

/**
 * 类 {@code Object} 是类层次结构的根。
 * 每个类都有 {@code Object} 作为超类。所有对象，
 * 包括数组，都实现了这个类的方法。
 *
 * @author 未署名
 * @see java.lang.Class
 * @since JDK1.0
 */
public class Object {

    private static native void registerNatives();
    static {
        registerNatives();
    }

    /**
     * 返回此 {@code Object} 的运行时类。返回的
     * {@code Class} 对象是被 {@code
     * static synchronized} 方法锁定的对象。
     *
     * <p><b>实际结果类型是 {@code Class<? extends |X|>}
     * 其中 {@code |X|} 是调用 {@code getClass} 的表达式的静态类型的擦除。</b> 例如，此代码片段不需要强制转换：</p>
     *
     * <p>
     * {@code Number n = 0;                             }<br>
     * {@code Class<? extends Number> c = n.getClass(); }
     * </p>
     *
     * @return 代表此对象的运行时类的 {@code Class} 对象。
     * @jls 15.8.2 类字面量
     */
    public final native Class<?> getClass();

    /**
     * 返回对象的哈希码值。此方法是为了哈希表（如由
     * {@link java.util.HashMap} 提供的）的利益而支持的。
     * <p>
     * {@code hashCode} 的一般契约是：
     * <ul>
     * <li>在 Java 应用程序的执行过程中，每当多次调用同一对象的
     *     {@code hashCode} 方法时，只要用于 {@code equals} 比较的对象信息未被修改，
     *     {@code hashCode} 方法必须一致地返回相同的整数。此整数不必在应用程序的一次执行与另一次执行之间保持一致。
     * <li>如果两个对象根据 {@code equals(Object)}
     *     方法是相等的，那么对这两个对象调用 {@code hashCode} 方法必须产生相同的整数结果。
     * <li>如果两个对象根据 {@link java.lang.Object#equals(java.lang.Object)}
     *     方法是不相等的，那么对这两个对象调用 {@code hashCode} 方法不必产生不同的整数结果。然而，程序员应该意识到，为不相等的对象生成不同的整数结果可能会提高哈希表的性能。
     * </ul>
     * <p>
     * 尽可能合理地，由类 {@code Object} 定义的 {@code hashCode} 方法确实为不同的对象返回不同的整数。（这通常通过将对象的内部地址转换为整数来实现，但这种实现技术不是 Java&trade; 编程语言所要求的。）
     *
     * @return 该对象的哈希码值。
     * @see java.lang.Object#equals(java.lang.Object)
     * @see java.lang.System#identityHashCode
     */
    public native int hashCode();

    /**
     * 指示某些其他对象是否与该对象“相等”。
     * <p>
     * {@code equals} 方法在非空对象引用上实现等价关系：
     * <ul>
     * <li>它是<i>自反的</i>：对于任何非空引用值
     *     {@code x}，{@code x.equals(x)} 应返回
     *     {@code true}。
     * <li>它是<i>对称的</i>：对于任何非空引用值
     *     {@code x} 和 {@code y}，{@code x.equals(y)}
     *     应返回 {@code true} 当且仅当
     *     {@code y.equals(x)} 返回 {@code true}。
     * <li>它是<i>传递的</i>：对于任何非空引用值
     *     {@code x}，{@code y}，和 {@code z}，如果
     *     {@code x.equals(y)} 返回 {@code true} 且
     *     {@code y.equals(z)} 返回 {@code true}，那么
     *     {@code x.equals(z)} 应返回 {@code true}。
     * <li>它是<i>一致的</i>：对于任何非空引用值
     *     {@code x} 和 {@code y}，多次调用
     *     {@code x.equals(y)} 一致地返回 {@code true}
     *     或一致地返回 {@code false}，前提是用于 {@code equals} 比较的对象信息未被修改。
     * <li>对于任何非空引用值 {@code x}，
     *     {@code x.equals(null)} 应返回 {@code false}。
     * </ul>
     * <p>
     * 类 {@code Object} 的 {@code equals} 方法实现对对象最区分的可能等价关系；
     * 即，对于任何非空引用值 {@code x} 和
     * {@code y}，此方法返回 {@code true} 当且仅当
     * {@code x} 和 {@code y} 引用同一个对象
     * ({@code x == y} 的值为 {@code true})。
     * <p>
     * 通常情况下，每当重写此方法时，都需要重写 {@code hashCode}
     * 方法，以维护 {@code hashCode} 方法的一般契约，该契约规定相等的对象必须具有相等的哈希码。
     *
     * @param   obj   要比较的引用对象。
     * @return  如果此对象与 obj 参数相同，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     #hashCode()
     * @see     java.util.HashMap
     */
    public boolean equals(Object obj) {
        return (this == obj);
    }

    /**
     * 创建并返回此对象的副本。“副本”的精确含义可能取决于对象的类。一般意图是，对于任何对象 {@code x}，表达式：
     * <blockquote>
     * <pre>
     * x.clone() != x</pre></blockquote>
     * 将为真，且表达式：
     * <blockquote>
     * <pre>
     * x.clone().getClass() == x.getClass()</pre></blockquote>
     * 将为 {@code true}，但这不是绝对要求。通常情况下：
     * <blockquote>
     * <pre>
     * x.clone().equals(x)</pre></blockquote>
     * 将为 {@code true}，但这不是绝对要求。
     * <p>
     * 按照惯例，返回的对象应通过调用
     * {@code super.clone} 获得。如果一个类及其所有超类（除了
     * {@code Object}）都遵守此惯例，则将满足
     * {@code x.clone().getClass() == x.getClass()}。
     * <p>
     * 按照惯例，此方法返回的对象应独立于该对象（即被克隆的对象）。为了实现这种独立性，可能需要修改由
     * {@code super.clone} 返回的对象的一个或多个字段，然后再返回它。通常，这意味着复制构成被克隆对象内部“深层结构”的任何可变对象，并用这些对象的引用替换这些对象的引用。如果一个类只包含基本字段或对不可变对象的引用，那么通常情况下，由
     * {@code super.clone} 返回的对象中的任何字段都不需要修改。
     * <p>
     * 类 {@code Object} 的 {@code clone} 方法执行特定的克隆操作。首先，如果此对象的类未实现
     * {@code Cloneable} 接口，则抛出
     * {@code CloneNotSupportedException}。注意，所有数组都被视为实现
     * {@code Cloneable} 接口，且数组类型 {@code T[]} 的
     * {@code clone} 方法的返回类型为 {@code T[]}，其中 T 是任何引用或基本类型。
     * 否则，此方法创建此对象类的新实例，并用与此对象对应的字段的完全相同内容初始化所有字段，就像通过赋值一样；字段的内容本身不会被克隆。因此，此方法执行的是此对象的“浅复制”，而不是“深复制”操作。
     * <p>
     * 类 {@code Object} 本身不实现
     * {@code Cloneable} 接口，因此对类为
     * {@code Object} 的对象调用 {@code clone} 方法将在运行时抛出异常。
     *
     * @return     该实例的克隆。
     * @throws  CloneNotSupportedException  如果对象的类不支持
     *               {@code Cloneable} 接口。重写 {@code clone} 方法的子类
     *               也可以抛出此异常，以指示实例不能被克隆。
     * @see java.lang.Cloneable
     */
    protected native Object clone() throws CloneNotSupportedException;

                    /**
     * 返回对象的字符串表示形式。通常，{@code toString} 方法返回一个
     * “文本表示”此对象的字符串。结果应该是一个简洁但信息量大的表示形式，易于人们阅读。
     * 建议所有子类都重写此方法。
     * <p>
     * 对于 {@code Object} 类的 {@code toString} 方法，返回的字符串由该对象所属类的名称、
     * 字符 `{@code @}' 和该对象哈希码的无符号十六进制表示组成。换句话说，此方法返回一个等于以下值的字符串：
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return  对象的字符串表示形式。
     */
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }

    /**
     * 唤醒正在等待此对象监视器的单个线程。如果有线程正在等待此对象，其中一个线程将被唤醒。选择是任意的，并由实现决定。
     * 线程通过调用其中一个 {@code wait} 方法来等待对象的监视器。
     * <p>
     * 被唤醒的线程在当前线程释放此对象的锁之前无法继续执行。被唤醒的线程将与其他可能正在竞争同步此对象的线程以通常的方式竞争；
     * 例如，被唤醒的线程在成为下一个锁定此对象的线程方面没有可靠的特权或劣势。
     * <p>
     * 此方法只能由拥有此对象监视器的线程调用。线程可以通过以下三种方式之一成为对象监视器的所有者：
     * <ul>
     * <li>通过执行该对象的同步实例方法。
     * <li>通过执行同步于该对象的 {@code synchronized} 语句的主体。
     * <li>对于类型为 {@code Class} 的对象，通过执行该类的同步静态方法。
     * </ul>
     * <p>
     * 一次只能有一个线程拥有对象的监视器。
     *
     * @throws  IllegalMonitorStateException  如果当前线程不是此对象监视器的所有者。
     * @see        java.lang.Object#notifyAll()
     * @see        java.lang.Object#wait()
     */
    public final native void notify();

    /**
     * 唤醒正在等待此对象监视器的所有线程。线程通过调用其中一个 {@code wait} 方法来等待对象的监视器。
     * <p>
     * 被唤醒的线程在当前线程释放此对象的锁之前无法继续执行。被唤醒的线程将与其他可能正在竞争同步此对象的线程以通常的方式竞争；
     * 例如，被唤醒的线程在成为下一个锁定此对象的线程方面没有可靠的特权或劣势。
     * <p>
     * 此方法只能由拥有此对象监视器的线程调用。有关线程成为监视器所有者的描述，请参见 {@code notify} 方法。
     *
     * @throws  IllegalMonitorStateException  如果当前线程不是此对象监视器的所有者。
     * @see        java.lang.Object#notify()
     * @see        java.lang.Object#wait()
     */
    public final native void notifyAll();

    /**
     * 使当前线程等待，直到另一个线程调用此对象的 {@link java.lang.Object#notify()} 方法或
     * {@link java.lang.Object#notifyAll()} 方法，或者指定的时间已过。
     * <p>
     * 当前线程必须拥有此对象的监视器。
     * <p>
     * 此方法使当前线程（假设为 <var>T</var>）将自身置于此对象的等待集中，然后放弃对此对象的所有同步声明。
     * 线程 <var>T</var> 被禁用以进行线程调度，并处于休眠状态，直到以下四种情况之一发生：
     * <ul>
     * <li>其他某个线程调用此对象的 {@code notify} 方法，并且线程 <var>T</var> 被任意选中为要唤醒的线程。
     * <li>其他某个线程调用此对象的 {@code notifyAll} 方法。
     * <li>其他某个线程 {@linkplain Thread#interrupt() 中断} 线程 <var>T</var>。
     * <li>指定的实际时间已经过去，或多或少。但是，如果 {@code timeout} 为零，则不考虑实际时间，线程只是等待直到被通知。
     * </ul>
     * 线程 <var>T</var> 然后从该对象的等待集中移除，并重新启用以进行线程调度。然后它以通常的方式与其他线程竞争以同步该对象；
     * 一旦它获得了对象的控制权，它对该对象的所有同步声明将恢复到调用 {@code wait} 方法时的状态。
     * 线程 <var>T</var> 然后从 {@code wait} 方法调用返回。因此，从 {@code wait} 方法返回时，对象和线程 <var>T</var> 的同步状态
     * 与调用 {@code wait} 方法时完全相同。
     * <p>
     * 线程也可以在没有被通知、中断或超时的情况下醒来，这称为 <i>虚假唤醒</i>。虽然这种情况在实践中很少发生，但应用程序必须对此进行防护，
     * 通过测试应该导致线程唤醒的条件，并在条件不满足时继续等待。换句话说，等待应该始终在循环中进行，如下所示：
     * <pre>
     *     synchronized (obj) {
     *         while (&lt;条件不成立&gt;)
     *             obj.wait(timeout);
     *         ... // 执行与条件相关的操作
     *     }
     * </pre>
     * （关于此主题的更多信息，请参见 Doug Lea 的 "Concurrent Programming in Java (Second Edition)"（Addison-Wesley，2000）第 3.2.3 节，
     * 或 Joshua Bloch 的 "Effective Java Programming Language Guide"（Addison-Wesley，2001）第 50 项。
     *
     * <p>如果当前线程在等待之前或等待期间被任何线程 {@linkplain java.lang.Thread#interrupt() 中断}，则抛出
     * {@code InterruptedException}。此异常在恢复此对象的锁状态后抛出。
     * <p>
     * 请注意，当 {@code wait} 方法将当前线程放入此对象的等待集时，仅解锁此对象；当前线程可能同步的任何其他对象在等待期间保持锁定状态。
     * <p>
     * 此方法只能由拥有此对象监视器的线程调用。有关线程成为监视器所有者的描述，请参见 {@code notify} 方法。
     *
     * @param      timeout   最大等待时间（以毫秒为单位）。
     * @throws  IllegalArgumentException      如果 timeout 的值为负。
     * @throws  IllegalMonitorStateException  如果当前线程不是对象监视器的所有者。
     * @throws  InterruptedException 如果任何线程在当前线程等待通知之前或期间中断了当前线程。当前线程的 <i>中断状态</i> 在抛出此异常时被清除。
     * @see        java.lang.Object#notify()
     * @see        java.lang.Object#notifyAll()
     */
    public final native void wait(long timeout) throws InterruptedException;

                    /**
     * 使当前线程等待，直到其他线程调用此对象的
     * {@link java.lang.Object#notify()} 方法或
     * {@link java.lang.Object#notifyAll()} 方法，或者
     * 其他线程中断当前线程，或者一定的时间已经过去。
     * <p>
     * 此方法类似于只有一个参数的 {@code wait} 方法，但允许更精细地控制在放弃之前等待通知的时间。实际时间，
     * 以纳秒为单位，由以下给出：
     * <blockquote>
     * <pre>
     * 1000000*timeout+nanos</pre></blockquote>
     * <p>
     * 在所有其他方面，此方法与只有一个参数的 {@link #wait(long)} 方法执行相同的操作。特别是，
     * {@code wait(0, 0)} 与 {@code wait(0)} 意义相同。
     * <p>
     * 当前线程必须拥有此对象的监视器。线程释放此监视器的所有权并等待以下两种情况之一发生：
     * <ul>
     * <li>其他线程通过调用 {@code notify} 方法或 {@code notifyAll} 方法通知等待此对象监视器的线程
     *     唤醒。
     * <li>由 {@code timeout} 毫秒加上 {@code nanos} 纳秒参数指定的超时时间已过。
     * </ul>
     * <p>
     * 线程然后等待直到它可以重新获得监视器的所有权并恢复执行。
     * <p>
     * 与单参数版本一样，可能会发生中断和虚假唤醒，此方法应始终在循环中使用：
     * <pre>
     *     synchronized (obj) {
     *         while (&lt;条件不成立&gt;)
     *             obj.wait(timeout, nanos);
     *         ... // 执行与条件相关的操作
     *     }
     * </pre>
     * 此方法只能由拥有此对象监视器的线程调用。有关线程如何成为监视器所有者的描述，请参见 {@code notify} 方法。
     *
     * @param      timeout   最大等待时间，以毫秒为单位。
     * @param      nanos      额外的时间，以纳秒为单位，范围为 0-999999。
     * @throws  IllegalArgumentException      如果超时值为负数或纳秒值不在 0-999999 范围内。
     * @throws  IllegalMonitorStateException  如果当前线程不是此对象监视器的所有者。
     * @throws  InterruptedException 如果任何线程在当前线程等待通知之前或期间中断了当前线程。当抛出此异常时，当前线程的<i>中断状态</i>将被清除。
     */
    public final void wait(long timeout, int nanos) throws InterruptedException {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout value is negative");
        }

        if (nanos < 0 || nanos > 999999) {
            throw new IllegalArgumentException(
                                "nanosecond timeout value out of range");
        }

        if (nanos > 0) {
            timeout++;
        }

        wait(timeout);
    }

    /**
     * 使当前线程等待，直到其他线程调用此对象的
     * {@link java.lang.Object#notify()} 方法或
     * {@link java.lang.Object#notifyAll()} 方法。
     * 换句话说，此方法的行为与仅执行 {@code wait(0)} 调用完全相同。
     * <p>
     * 当前线程必须拥有此对象的监视器。线程释放此监视器的所有权并等待另一个线程
     * 通过调用 {@code notify} 方法或 {@code notifyAll} 方法通知等待此对象监视器的线程唤醒。
     * 线程然后等待直到它可以重新获得监视器的所有权并恢复执行。
     * <p>
     * 与单参数版本一样，可能会发生中断和虚假唤醒，此方法应始终在循环中使用：
     * <pre>
     *     synchronized (obj) {
     *         while (&lt;条件不成立&gt;)
     *             obj.wait();
     *         ... // 执行与条件相关的操作
     *     }
     * </pre>
     * 此方法只能由拥有此对象监视器的线程调用。有关线程如何成为监视器所有者的描述，请参见 {@code notify} 方法。
     *
     * @throws  IllegalMonitorStateException  如果当前线程不是对象监视器的所有者。
     * @throws  InterruptedException 如果任何线程在当前线程等待通知之前或期间中断了当前线程。当抛出此异常时，当前线程的<i>中断状态</i>将被清除。
     * @see        java.lang.Object#notify()
     * @see        java.lang.Object#notifyAll()
     */
    public final void wait() throws InterruptedException {
        wait(0);
    }

    /**
     * 当垃圾回收器确定没有更多对对象的引用时，由垃圾回收器在对象上调用。
     * 子类覆盖 {@code finalize} 方法以释放系统资源或执行其他清理。
     * <p>
     * {@code finalize} 的一般约定是在 Java&trade; 虚拟机确定没有
     * 任何未死线程可以通过任何方式访问此对象时调用，除非是由于
     * 其他对象或类的最终化操作的结果，这些对象或类已准备好被最终化。{@code finalize} 方法可以采取任何行动，包括
     * 使此对象再次对其他线程可用；但是，{@code finalize} 的通常目的是在对象被不可逆地丢弃之前执行清理操作。例如，表示输入/输出连接的对象的 finalize 方法
     * 可能会执行显式的 I/O 事务来断开连接，然后对象被永久丢弃。
     * <p>
     * 类 {@code Object} 的 {@code finalize} 方法不执行任何特殊操作；它只是正常返回。{@code Object} 的子类
     * 可以覆盖此定义。
     * <p>
     * Java 编程语言不保证哪个线程将调用任何给定对象的 {@code finalize} 方法。但是，
     * 保证调用 finalize 的线程在调用 finalize 时不会持有任何用户可见的同步锁。如果 finalize 方法抛出未捕获的异常，
     * 则该异常将被忽略，该对象的最终化将终止。
     * <p>
     * 在 {@code finalize} 方法被调用后，直到 Java 虚拟机再次确定没有更多对对象的引用时，不会采取任何进一步的行动，包括可能的
     * 其他对象或类的最终化操作，这些对象或类已准备好被最终化，此时对象可以被丢弃。
     * <p>
     * 对于任何给定的对象，Java 虚拟机最多只调用一次 {@code finalize} 方法。
     * <p>
     * 由 {@code finalize} 方法抛出的任何异常都会导致此对象的最终化终止，但除此之外会被忽略。
     *
     * @throws Throwable 由此方法引发的 {@code Exception}
     * @see java.lang.ref.WeakReference
     * @see java.lang.ref.PhantomReference
     * @jls 12.6 类实例的最终化
     */
    protected void finalize() throws Throwable { }
}
