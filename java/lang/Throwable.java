/*
 * 版权所有 (c) 1994, 2019, Oracle 和/或其关联公司。保留所有权利。
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

import java.io.*;
import java.util.*;

/**
 * {@code Throwable} 类是 Java 语言中所有错误和异常的超类。只有此类的实例（或其子类之一）
 * 才会被 Java 虚拟机抛出，或者可以通过 Java 的 {@code throw} 语句抛出。同样，只有此类或其子类
 * 可以作为 {@code catch} 子句中的参数类型。
 *
 * 为了在编译时检查异常，{@code Throwable} 及其任何子类，如果不是 {@link RuntimeException} 或
 * {@link Error} 的子类，则被视为受检查异常。
 *
 * <p>两个子类的实例，{@link java.lang.Error} 和 {@link java.lang.Exception}，通常用于指示
 * 发生了异常情况。通常，这些实例是在异常情况的上下文中新创建的，以便包含相关信息（如堆栈跟踪数据）。
 *
 * <p>一个 throwable 包含其创建时线程的执行堆栈快照。它还可以包含一个提供有关错误更多信息的消息字符串。
 * 随着时间的推移，一个 throwable 可以 {@linkplain Throwable#addSuppressed 抑制} 其他 throwable 的传播。
 * 最后，throwable 还可以包含一个 <i>原因</i>：导致此 throwable 被构造的另一个 throwable。
 * 记录这种因果信息被称为 <i>链式异常</i> 机制，因为原因本身也可以有原因，依此类推，形成一个由
 * 一个异常导致另一个异常的“链”。
 *
 * <p>一个 throwable 可能有原因的一个原因是抛出它的类构建在一个较低层次的抽象之上，而上层操作由于
 * 下层的失败而失败。让下层抛出的 throwable 向外传播通常是不好的设计，因为它通常与上层提供的抽象无关。
 * 此外，如果下层的异常是受检查异常，这样做会将上层的 API 与其实现细节绑定在一起。
 * 抛出“包装异常”（即包含原因的异常）允许上层向其调用者传达失败的细节，而不会导致上述任何缺点。
 * 它保留了更改上层实现的灵活性，而无需更改其 API（特别是其方法抛出的异常集）。
 *
 * <p>一个 throwable 可能有原因的第二个原因是抛出它的方法必须符合一个不允许直接抛出原因的通用接口。
 * 例如，假设一个持久化集合符合 {@link java.util.Collection Collection} 接口，并且其持久化
 * 是在 {@code java.io} 之上实现的。假设 {@code add} 方法的内部可能抛出
 * {@link java.io.IOException IOException}。实现可以通过将 {@code IOException} 包装在
 * 适当的非受检查异常中，在符合 {@code Collection} 接口的同时向其调用者传达
 * {@code IOException} 的细节。（持久化集合的规范应指明它能够抛出此类异常。）
 *
 * <p>可以通过两种方式将原因与 throwable 关联：通过接受原因作为参数的构造函数，或者通过
 * {@link #initCause(Throwable)} 方法。希望允许与其关联原因的新 throwable 类应提供接受原因的
 * 构造函数，并（可能间接）委托给接受原因的 {@code Throwable} 构造函数之一。
 *
 * 因为 {@code initCause} 方法是公有的，它允许将原因与任何 throwable 关联，即使是
 * 在异常链机制添加到 {@code Throwable} 之前实现的“遗留 throwable”。
 *
 * <p>按照惯例，类 {@code Throwable} 及其子类有两个构造函数，一个不接受参数，另一个接受一个
 * {@code String} 参数，可用于生成详细消息。此外，那些可能与其关联原因的子类应再有两个构造函数，
 * 一个接受一个 {@code Throwable}（原因），另一个接受一个 {@code String}（详细消息）和一个
 * {@code Throwable}（原因）。
 *
 * @作者 未署名
 * @作者 Josh Bloch（在 1.4 版本中添加了异常链和对堆栈跟踪的程序化访问）
 * @jls 11.2 编译时异常检查
 * @since JDK1.0
 */
public class Throwable implements Serializable {
    /** 为互操作性使用 JDK 1.0.2 的 serialVersionUID */
    private static final long serialVersionUID = -3042686055658047285L;

    /**
     * 本地代码在此槽中保存堆栈回溯的一些指示。
     */
    private transient Object backtrace;

    /**
     * 关于 Throwable 的具体细节。例如，对于 {@code FileNotFoundException}，
     * 它包含无法找到的文件的名称。
     *
     * @serial
     */
    private String detailMessage;

    /**
     * 延迟初始化仅用于序列化的哨兵对象的持有类。
     */
    private static class SentinelHolder {
        /**
         * 将堆栈跟踪 {@linkplain #setStackTrace(StackTraceElement[]) 设置} 为包含此哨兵值的
         * 单元素数组表示后续尝试设置堆栈跟踪将被忽略。哨兵值等于调用以下内容的结果：<br>
         * {@code new StackTraceElement("", "", null, Integer.MIN_VALUE)}
         */
        public static final StackTraceElement STACK_TRACE_ELEMENT_SENTINEL =
            new StackTraceElement("", "", null, Integer.MIN_VALUE);

        /**
         * 在序列化形式中用于指示不可变堆栈跟踪的哨兵值。
         */
        public static final StackTraceElement[] STACK_TRACE_SENTINEL =
            new StackTraceElement[] {STACK_TRACE_ELEMENT_SENTINEL};
    }

    /**
     * 共享的空堆栈值。
     */
    private static final StackTraceElement[] UNASSIGNED_STACK = new StackTraceElement[0];

    /*
     * 为了允许 Throwable 对象不可变并被 JVM 安全重用，例如 OutOfMemoryErrors，
     * 响应用户操作的可写字段（原因、堆栈跟踪和抑制异常）遵循以下协议：
     *
     * 1) 字段初始化为非空哨兵值，表示逻辑上尚未设置该值。
     *
     * 2) 向字段写入 null 表示禁止进一步写入。
     *
     * 3) 哨兵值可以被替换为另一个非空值。
     *
     * 例如，HotSpot JVM 的实现预分配了 OutOfMemoryError 对象，以在该情况下提供更好的
     * 诊断能力。这些对象在不调用该类的构造函数的情况下创建，并且相关字段初始化为 null。
     * 为了支持此功能，添加到 Throwable 的任何需要初始化为非空值的新字段都需要协调的 JVM 更改。
     */

    /**
     * 导致此 throwable 被抛出的 throwable，或者如果此 throwable 不是由另一个 throwable 导致，
     * 或原因 throwable 未知，则为 null。如果此字段等于此 throwable 本身，
     * 则表示此 throwable 的原因尚未初始化。
     *
     * @serial
     * @since 1.4
     */
    private Throwable cause = this;

    /**
     * 由 {@link #getStackTrace()} 返回的堆栈跟踪。
     *
     * 该字段初始化为零长度数组。此字段的 {@code null} 值表示后续对
     * {@link #setStackTrace(StackTraceElement[])} 和 {@link #fillInStackTrace()}
     * 的调用将是无操作。
     *
     * @serial
     * @since 1.4
     */
    private StackTraceElement[] stackTrace = UNASSIGNED_STACK;

    // 设置此静态字段引入了对几个 java.util 类的可接受初始化依赖。
    private static final List<Throwable> SUPPRESSED_SENTINEL =
        Collections.unmodifiableList(new ArrayList<Throwable>(0));

    /**
     * 由 {@link #getSuppressed()} 返回的抑制异常列表。该列表初始化为零元素的不可修改哨兵列表。
     * 当读取序列化的 Throwable 时，如果 {@code suppressedExceptions} 字段指向零元素列表，
     * 该字段将重置为哨兵值。
     *
     * @serial
     * @since 1.7
     */
    private List<Throwable> suppressedExceptions = SUPPRESSED_SENTINEL;

    /** 尝试抑制 null 异常的消息。 */
    private static final String NULL_CAUSE_MESSAGE = "无法抑制 null 异常。";

    /** 尝试抑制自身的消息。 */
    private static final String SELF_SUPPRESSION_MESSAGE = "不允许自我抑制";

    /** 用于标记原因异常堆栈跟踪的标题 */
    private static final String CAUSE_CAPTION = "原因：";

    /** 用于标记抑制异常堆栈跟踪的标题 */
    private static final String SUPPRESSED_CAPTION = "已抑制：";

    /**
     * 构造一个新的 throwable，其详细消息为 {@code null}。原因未初始化，
     * 随后可以通过调用 {@link #initCause} 初始化。
     *
     * <p>调用 {@link #fillInStackTrace()} 方法来初始化新创建的 throwable 中的堆栈跟踪数据。
     */
    public Throwable() {
        fillInStackTrace();
    }

    /**
     * 构造一个新的 throwable，带有指定的详细消息。原因未初始化，
     * 随后可以通过调用 {@link #initCause} 初始化。
     *
     * <p>调用 {@link #fillInStackTrace()} 方法来初始化新创建的 throwable 中的堆栈跟踪数据。
     *
     * @param message 详细消息。详细消息被保存以供稍后通过 {@link #getMessage()} 方法检索。
     */
    public Throwable(String message) {
        fillInStackTrace();
        detailMessage = message;
    }

    /**
     * 构造一个新的 throwable，带有指定的详细消息和原因。
     * <p>请注意，与 {@code cause} 关联的详细消息<i>不会</i>自动包含在此 throwable 的详细消息中。
     *
     * <p>调用 {@link #fillInStackTrace()} 方法来初始化新创建的 throwable 中的堆栈跟踪数据。
     *
     * @param message 详细消息（保存以供稍后通过 {@link #getMessage()} 方法检索）。
     * @param cause 原因（保存以供稍后通过 {@link #getCause()} 方法检索）。
     *              （允许 {@code null} 值，表示原因不存在或未知。）
     * @since 1.4
     */
    public Throwable(String message, Throwable cause) {
        fillInStackTrace();
        detailMessage = message;
        this.cause = cause;
    }

    /**
     * 构造一个新的 throwable，带有指定的原因和详细消息
     * {@code (cause==null ? null : cause.toString())}（通常包含 {@code cause} 的类和详细消息）。
     * 此构造函数对于仅作为其他 throwable 包装器的 throwable 非常有用（例如，
     * {@link java.security.PrivilegedActionException}）。
     *
     * <p>调用 {@link #fillInStackTrace()} 方法来初始化新创建的 throwable 中的堆栈跟踪数据。
     *
     * @param cause 原因（保存以供稍后通过 {@link #getCause()} 方法检索）。
     *              （允许 {@code null} 值，表示原因不存在或未知。）
     * @since 1.4
     */
    public Throwable(Throwable cause) {
        fillInStackTrace();
        detailMessage = (cause==null ? null : cause.toString());
        this.cause = cause;
    }

    /**
     * 构造一个新的 throwable，带有指定的详细消息、原因、{@linkplain #addSuppressed 抑制}启用或禁用，
     * 以及堆栈跟踪可写或不可写。如果禁用抑制，{@link #getSuppressed} 对于此对象将返回零长度数组，
     * 并且对 {@link #addSuppressed} 的调用（否则会将异常附加到抑制列表）将无效。
     * 如果堆栈跟踪不可写，此构造函数不会调用 {@link #fillInStackTrace()}，
     * 将向 {@code stackTrace} 字段写入 {@code null}，并且后续对 {@code fillInStackTrace}
     * 和 {@link #setStackTrace(StackTraceElement[])} 的调用不会设置堆栈跟踪。
     * 如果堆栈跟踪不可写，{@link #getStackTrace} 将返回零长度数组。
     *
     * <p>请注意，{@code Throwable} 的其他构造函数将抑制视为启用，堆栈跟踪视为可写。
     * {@code Throwable} 的子类应记录任何禁用抑制的情况以及堆栈跟踪不可写的条件。
     * 仅在存在特殊要求的情况下（例如虚拟机在低内存情况下重用异常对象）才应禁用抑制。
     * 在给定异常对象被反复捕获和重新抛出的情况下（例如在两个子系统之间实现控制流），
     * 使用不可变 throwable 对象也是适当的。
     *
     * @param message 详细消息。
     * @param cause 原因。（允许 {@code null} 值，表示原因不存在或未知。）
     * @param enableSuppression 是否启用或禁用抑制
     * @param writableStackTrace 堆栈跟踪是否应为可写
     *
     * @see OutOfMemoryError
     * @see NullPointerException
     * @see ArithmeticException
     * @since 1.7
     */
    protected Throwable(String message, Throwable cause,
                        boolean enableSuppression,
                        boolean writableStackTrace) {
        if (writableStackTrace) {
            fillInStackTrace();
        } else {
            stackTrace = null;
        }
        detailMessage = message;
        this.cause = cause;
        if (!enableSuppression)
            suppressedExceptions = null;
    }

    /**
     * 返回此 throwable 的详细消息字符串。
     *
     * @return 此 {@code Throwable} 实例的详细消息字符串（可能为 {@code null}）。
     */
    public String getMessage() {
        return detailMessage;
    }

    /**
     * 创建此 throwable 的本地化描述。子类可以重写此方法以生成特定于区域设置的消息。
     * 对于未重写此方法的子类，默认实现返回与 {@code getMessage()} 相同的结果。
     *
     * @return 此 throwable 的本地化描述。
     * @since JDK1.1
     */
    public String getLocalizedMessage() {
        return getMessage();
    }

    /**
     * 返回此 throwable 的原因，或者如果原因不存在或未知，则返回 {@code null}。
     * （原因是指导致此 throwable 被抛出的 throwable。）
     *
     * <p>此实现返回通过需要 {@code Throwable} 的构造函数之一提供的原因，
     * 或通过 {@link #initCause(Throwable)} 方法在创建后设置的原因。
     * 虽然通常不需要重写此方法，但子类可以重写它以返回通过其他方式设置的原因。
     * 这适用于在异常链机制添加到 {@code Throwable} 之前实现的“遗留链式 throwable”。
     * 请注意，不需要重写任何 {@code PrintStackTrace} 方法，
     * 所有这些方法都会调用 {@code getCause} 方法来确定 throwable 的原因。
     *
     * @return 此 throwable 的原因，或者如果原因不存在或未知，则返回 {@code null}。
     * @since 1.4
     */
    public synchronized Throwable getCause() {
        return (cause==this ? null : cause);
    }

    /**
     * 将此 throwable 的<i>原因</i>初始化为指定值。（原因是指导致此 throwable 被抛出的 throwable。）
     *
     * <p>此方法最多可以调用一次。它通常在构造函数中调用，或者在创建 throwable 后立即调用。
     * 如果此 throwable 是通过 {@link #Throwable(Throwable)} 或
     * {@link #Throwable(String,Throwable)} 创建的，则即使调用一次此方法也不行。
     *
     * <p>在没有其他支持设置原因的遗留 throwable 类型上使用此方法的一个示例是：
     *
     * <pre>
     * try {
     *     lowLevelOp();
     * } catch (LowLevelException le) {
     *     throw (HighLevelException)
     *           new HighLevelException().initCause(le); // 遗留构造函数
     * }
     * </pre>
     *
     * @param cause 原因（保存以供稍后通过 {@link #getCause()} 方法检索）。
     *              （允许 {@code null} 值，表示原因不存在或未知。）
     * @return 对该 {@code Throwable} 实例的引用。
     * @throws IllegalArgumentException 如果 {@code cause} 是此 throwable。（一个 throwable 不能是它自己的原因。）
     * @throws IllegalStateException 如果此 throwable 是通过 {@link #Throwable(Throwable)} 或
     *         {@link #Throwable(String,Throwable)} 创建的，或者此方法已在此 throwable 上调用过。
     * @since 1.4
     */
    public synchronized Throwable initCause(Throwable cause) {
        if (this.cause != this)
            throw new IllegalStateException("无法用 " +
                                            Objects.toString(cause, "null") + " 覆盖原因", this);
        if (cause == this)
            throw new IllegalArgumentException("不允许自我原因", this);
        this.cause = cause;
        return this;
    }

    /**
     * 返回此 throwable 的简短描述。结果是以下内容的连接：
     * <ul>
     * <li> 此对象的类的 {@linkplain Class#getName() 名称}
     * <li> ": "（冒号和空格）
     * <li> 调用此对象的 {@link #getLocalizedMessage} 方法的结果
     * </ul>
     * 如果 {@code getLocalizedMessage} 返回 {@code null}，则仅返回类名。
     *
     * @return 此 throwable 的字符串表示形式。
     */
    public String toString() {
        String s = getClass().getName();
        String message = getLocalizedMessage();
        return (message != null) ? (s + ": " + message) : s;
    }

    /**
     * 将此 throwable 及其回溯打印到标准错误流。此方法在错误输出流（即字段 {@code System.err} 的值）
     * 上为此 {@code Throwable} 对象打印堆栈跟踪。输出的第一行包含此对象的 {@link #toString()} 方法的结果。
     * 其余行表示之前由 {@link #fillInStackTrace()} 方法记录的数据。此信息的格式取决于实现，
     * 但以下示例可视为典型：
     * <blockquote><pre>
     * java.lang.NullPointerException
     *         at MyClass.mash(MyClass.java:9)
     *         at MyClass.crunch(MyClass.java:6)
     *         at MyClass.main(MyClass.java:3)
     * </pre></blockquote>
     * 此示例是通过运行以下程序生成的：
     * <pre>
     * class MyClass {
     *     public static void main(String[] args) {
     *         crunch(null);
     *     }
     *     static void crunch(int[] a) {
     *         mash(a);
     *     }
     *     static void mash(int[] b) {
     *         System.out.println(b[0]);
     *     }
     * }
     * </pre>
     * 对于具有已初始化、非空原因的 throwable，其回溯通常应包括原因的回溯。
     * 此信息的格式取决于实现，但以下示例可视为典型：
     * <pre>
     * HighLevelException: MidLevelException: LowLevelException
     *         at Junk.a(Junk.java:13)
     *         at Junk.main(Junk.java:4)
     * 原因：MidLevelException: LowLevelException
     *         at Junk.c(Junk.java:23)
     *         at Junk.b(Junk.java:17)
     *         at Junk.a(Junk.java:11)
     *         ... 1 more
     * 原因：LowLevelException
     *         at Junk.e(Junk.java:30)
     *         at Junk.d(Junk.java:27)
     *         at Junk.c(Junk.java:21)
     *         ... 3 more
     * </pre>
     * 请注意包含 {@code "..."} 字符的行。这些行表示此异常的堆栈跟踪的其余部分与由该异常导致的
     * 异常（“包含”异常）的堆栈跟踪底部指定数量的帧匹配。这种简写可以在包装异常从捕获“原因异常”的
     * 同一方法抛出的常见情况下大大减少输出长度。以上示例是通过运行以下程序生成的：
     * <pre>
     * public class Junk {
     *     public static void main(String args[]) {
     *         try {
     *             a();
     *         } catch(HighLevelException e) {
     *             e.printStackTrace();
     *         }
     *     }
     *     static void a() throws HighLevelException {
     *         try {
     *             b();
     *         } catch(MidLevelException e) {
     *             throw new HighLevelException(e);
     *         }
     *     }
     *     static void b() throws MidLevelException {
     *         c();
     *     }
     *     static void c() throws MidLevelException {
     *         try {
     *             d();
     *         } catch(LowLevelException e) {
     *             throw new MidLevelException(e);
     *         }
     *     }
     *     static void d() throws LowLevelException {
     *        e();
     *     }
     *     static void e() throws LowLevelException {
     *         throw new LowLevelException();
     *     }
     * }
     *
     * class HighLevelException extends Exception {
     *     HighLevelException(Throwable cause) { super(cause); }
     * }
     *
     * class MidLevelException extends Exception {
     *     MidLevelException(Throwable cause)  { super(cause); }
     * }
     *
     * class LowLevelException extends Exception {
     * }
     * </pre>
     * 从版本 7 开始，平台支持<i>抑制异常</i>的概念（结合 {@code try}-with-resources 语句）。
     * 为了传递异常而抑制的任何异常将在堆栈跟踪下方打印出来。此信息的格式取决于实现，
     * 但以下示例可视为典型：
     *
     * <pre>
     * Exception in thread "main" java.lang.Exception: Something happened
     *  at Foo.bar(Foo.java:10)
     *  at Foo.main(Foo.java:5)
     *  已抑制：Resource$CloseFailException: Resource ID = 0
     *          at Resource.close(Resource.java:26)
     *          at Foo.bar(Foo.java:9)
     *          ... 1 more
     * </pre>
     * 请注意，抑制异常上也使用 "... n more" 符号，就像在原因上一样。
     * 与原因不同，抑制异常缩进超过其“包含异常”。
     *
     * <p>一个异常可以同时具有原因和一个或多个抑制异常：
     * <pre>
     * Exception in thread "main" java.lang.Exception: Main block
     *  at Foo3.main(Foo3.java:7)
     *  已抑制：Resource$CloseFailException: Resource ID = 2
     *          at Resource.close(Resource.java:26)
     *          at Foo3.main(Foo3.java:5)
     *  已抑制：Resource$CloseFailException: Resource ID = 1
     *          at Resource.close(Resource.java:26)
     *          at Foo3.main(Foo3.java:5)
     * 原因：java.lang.Exception: I did it
     *  at Foo3.main(Foo3.java:8)
     * </pre>
     * 同样，抑制异常也可以有原因：
     * <pre>
     * Exception in thread "main" java.lang.Exception: Main block
     *  at Foo4.main(Foo4.java:6)
     *  已抑制：Resource2$CloseFailException: Resource ID = 1
     *          at Resource2.close(Resource2.java:20)
     *          at Foo4.main(Foo4.java:5)
     * 原因：java.lang.Exception: Rats, you caught me
     *          at Resource2$CloseFailException.<init>(Resource2.java:45)
     *          ... 2 more
     * </pre>
     */
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * 将此 throwable 及其回溯打印到指定的打印流。
     *
     * @param s 用于输出的 {@code PrintStream}
     */
    public void printStackTrace(PrintStream s) {
        printStackTrace(new WrappedPrintStream(s));
    }

    private void printStackTrace(PrintStreamOrWriter s) {
        // 通过使用具有标识相等语义的 Set 来防止恶意重写 Throwable.equals。
        Set<Throwable> dejaVu =
            Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
        dejaVu.add(this);

        synchronized (s.lock()) {
            // 打印我们的堆栈跟踪
            s.println(this);
            StackTraceElement[] trace = getOurStackTrace();
            for (StackTraceElement traceElement : trace)
                s.println("\tat " + traceElement);

            // 打印抑制异常（如果有）
            for (Throwable se : getSuppressed())
                se.printEnclosedStackTrace(s, trace, SUPPRESSED_CAPTION, "\t", dejaVu);

            // 打印原因（如果有）
            Throwable ourCause = getCause();
            if (ourCause != null)
                ourCause.printEnclosedStackTrace(s, trace, CAUSE_CAPTION, "", dejaVu);
        }
    }

    /**
     * 将我们的堆栈跟踪作为指定堆栈跟踪的封闭异常打印。
     */
    private void printEnclosedStackTrace(PrintStreamOrWriter s,
                                         StackTraceElement[] enclosingTrace,
                                         String caption,
                                         String prefix,
                                         Set<Throwable> dejaVu) {
        assert Thread.holdsLock(s.lock());
        if (dejaVu.contains(this)) {
            s.println("\t[循环引用：" + this + "]");
        } else {
            dejaVu.add(this);
            // 计算此跟踪与封闭跟踪的共同帧数
            StackTraceElement[] trace = getOurStackTrace();
            int m = trace.length - 1;
            int n = enclosingTrace.length - 1;
            while (m >= 0 && n >=0 && trace[m].equals(enclosingTrace[n])) {
                m--; n--;
            }
            int framesInCommon = trace.length - 1 - m;

            // 打印我们的堆栈跟踪
            s.println(prefix + caption + this);
            for (int i = 0; i <= m; i++)
                s.println(prefix + "\tat " + trace[i]);
            if (framesInCommon != 0)
                s.println(prefix + "\t... " + framesInCommon + " more");

            // 打印抑制异常（如果有）
            for (Throwable se : getSuppressed())
                se.printEnclosedStackTrace(s, trace, SUPPRESSED_CAPTION,
                                           prefix +"\t", dejaVu);

            // 打印原因（如果有）
            Throwable ourCause = getCause();
            if (ourCause != null)
                ourCause.printEnclosedStackTrace(s, trace, CAUSE_CAPTION, prefix, dejaVu);
        }
    }

    /**
     * 将此 throwable 及其回溯打印到指定的打印写入器。
     *
     * @param s 用于输出的 {@code PrintWriter}
     * @since JDK1.1
     */
    public void printStackTrace(PrintWriter s) {
        printStackTrace(new WrappedPrintWriter(s));
    }

    /**
     * 为 PrintStream 和 PrintWriter 提供包装类，以启用单一的 printStackTrace 实现。
     */
    private abstract static class PrintStreamOrWriter {
        /** 返回使用此 StreamOrWriter 时要锁定的对象 */
        abstract Object lock();

        /** 在此 StreamOrWriter 上将指定字符串打印为一行 */
        abstract void println(Object o);
    }

    private static class WrappedPrintStream extends PrintStreamOrWriter {
        private final PrintStream printStream;

        WrappedPrintStream(PrintStream printStream) {
            this.printStream = printStream;
        }

        Object lock() {
            return printStream;
        }

        void println(Object o) {
            printStream.println(o);
        }
    }

    private static class WrappedPrintWriter extends PrintStreamOrWriter {
        private final PrintWriter printWriter;

        WrappedPrintWriter(PrintWriter printWriter) {
            this.printWriter = printWriter;
        }

        Object lock() {
            return printWriter;
        }

        void println(Object o) {
            printWriter.println(o);
        }
    }

    /**
     * 填充执行堆栈跟踪。此方法在此 {@code Throwable} 对象中记录当前线程的堆栈帧的当前状态信息。
     *
     * <p>如果此 {@code Throwable} 的堆栈跟踪 {@linkplain
     * Throwable#Throwable(String, Throwable, boolean, boolean) 不可写}，调用此方法无效。
     *
     * @return 对该 {@code Throwable} 实例的引用。
     * @see java.lang.Throwable#printStackTrace()
     */
    public synchronized Throwable fillInStackTrace() {
        if (stackTrace != null ||
            backtrace != null /* 协议状态异常 */ ) {
            fillInStackTrace(0);
            stackTrace = UNASSIGNED_STACK;
        }
        return this;
    }

    private native Throwable fillInStackTrace(int dummy);

    /**
     * 提供对 {@link #printStackTrace()} 打印的堆栈跟踪信息的程序化访问。
     * 返回一个堆栈跟踪元素数组，每个元素表示一个堆栈帧。
     * 数组的第零个元素（假设数组长度非零）表示堆栈顶部，即序列中的最后一次方法调用。
     * 通常，这是创建和抛出此 throwable 的点。
     * 数组的最后一个元素（假设数组长度非零）表示堆栈底部，即序列中的第一次方法调用。
     *
     * <p>在某些情况下，某些虚拟机可能会从堆栈跟踪中省略一个或多个堆栈帧。
     * 在极端情况下，允许没有关于此 throwable 的堆栈跟踪信息的虚拟机从此方法返回零长度数组。
     * 一般来说，此方法返回的数组将为 {@code printStackTrace} 打印的每个帧包含一个元素。
     * 对返回数组的写入不会影响对此方法的未来调用。
     *
     * @return 表示此 throwable 的堆栈跟踪的堆栈跟踪元素数组。
     * @since 1.4
     */
    public StackTraceElement[] getStackTrace() {
        return getOurStackTrace().clone();
    }

    private synchronized StackTraceElement[] getOurStackTrace() {
        // 如果这是对此方法的第一次调用，则使用回溯信息初始化堆栈跟踪字段
        if (stackTrace == UNASSIGNED_STACK ||
            (stackTrace == null && backtrace != null) /* 协议状态异常 */) {
            int depth = getStackTraceDepth();
            stackTrace = new StackTraceElement[depth];
            for (int i=0; i < depth; i++)
                stackTrace[i] = getStackTraceElement(i);
        } else if (stackTrace == null) {
            return UNASSIGNED_STACK;
        }
        return stackTrace;
    }

    /**
     * 设置将由 {@link #getStackTrace()} 返回并由 {@link #printStackTrace()}
     * 及其相关方法打印的堆栈跟踪元素。
     *
     * 此方法专为 RPC 框架和其他高级系统设计，允许客户端覆盖在构造 throwable 时
     * 由 {@link #fillInStackTrace()} 生成的默认堆栈跟踪，或在从序列化流读取 throwable 时反序列化。
     *
     * <p>如果此 {@code Throwable} 的堆栈跟踪 {@linkplain
     * Throwable#Throwable(String, Throwable, boolean, boolean) 不可写}，
     * 调用此方法除了验证其参数外无效。
     *
     * @param stackTrace 要与此 {@code Throwable} 关联的堆栈跟踪元素。
     *                   此调用会复制指定的数组；方法调用返回后对指定数组的更改
     *                   不会影响此 {@code Throwable} 的堆栈跟踪。
     *
     * @throws NullPointerException 如果 {@code stackTrace} 为 {@code null}，
     *         或如果 {@code stackTrace} 的任何元素为 {@code null}
     *
     * @since 1.4
     */
    public void setStackTrace(StackTraceElement[] stackTrace) {
        // 验证参数
        StackTraceElement[] defensiveCopy = stackTrace.clone();
        for (int i = 0; i < defensiveCopy.length; i++) {
            if (defensiveCopy[i] == null)
                throw new NullPointerException("stackTrace[" + i + "]");
        }

        synchronized (this) {
            if (this.stackTrace == null && // 不可变堆栈
                backtrace == null) // 测试协议状态异常
                return;
            this.stackTrace = defensiveCopy;
        }
    }

    /**
     * 返回堆栈跟踪中的元素数量（如果堆栈跟踪不可用，则返回 0）。
     *
     * 为 SharedSecrets 使用的包保护。
     */
    native int getStackTraceDepth();

    /**
     * 返回堆栈跟踪的指定元素。
     *
     * 为 SharedSecrets 使用的包保护。
     *
     * @param index 要返回的元素索引。
     * @throws IndexOutOfBoundsException 如果 {@code index < 0 ||
     *         index >= getStackTraceDepth() }
     */
    native StackTraceElement getStackTraceElement(int index);

    /**
     * 从流中读取 {@code Throwable}，对字段强制执行格式良好的约束。
     * {@code suppressedExceptions} 列表中不允许有 null 条目和自指针。
     * 堆栈跟踪元素不允许有 null 条目。序列化形式中的 null 堆栈跟踪会导致零长度堆栈元素数组。
     * 单元素堆栈跟踪，其条目等于 {@code new StackTraceElement("",
     * "", null, Integer.MIN_VALUE)}，会导致 {@code stackTrace} 字段为 {@code null}。
     *
     * 请注意，{@code cause} 字段可以持有的值没有任何约束；{@code null} 和 {@code this}
     * 都是该字段的有效值。
     */
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        s.defaultReadObject();     // 读取所有字段

        // 将抑制异常和堆栈跟踪元素字段设置为标记值，直到验证来自序列化流的内容。
        List<Throwable> candidateSuppressedExceptions = suppressedExceptions;
        suppressedExceptions = SUPPRESSED_SENTINEL;

        StackTraceElement[] candidateStackTrace = stackTrace;
        stackTrace = UNASSIGNED_STACK.clone();

        if (candidateSuppressedExceptions != null) {
            int suppressedSize = validateSuppressedExceptionsList(candidateSuppressedExceptions);
            if (suppressedSize > 0) { // 将有效 Throwables 复制到新列表
                List<Throwable> suppList  = new ArrayList<>(Math.min(100, suppressedSize));

                for (Throwable t : candidateSuppressedExceptions) {
                    // 在流损坏或恶意的情况下强制执行抑制异常的约束。
                    if (t == null)
                        throw new NullPointerException(NULL_CAUSE_MESSAGE);
                    if (t == this)
                        throw new IllegalArgumentException(SELF_SUPPRESSION_MESSAGE);
                    suppList.add(t);
                }
                // 如果有任何无效的抑制异常，隐式使用之前分配的哨兵值。
                suppressedExceptions = suppList;
            }
        } else {
            suppressedExceptions = null;
        }

        /*
         * 对于零长度堆栈跟踪，使用 UNASSIGNED_STACK 的克隆而不是 UNASSIGNED_STACK 本身，
         * 以允许在 getOurStackTrace 中与 UNASSIGNED_STACK 进行标识比较。
         * stackTrace 中 UNASSIGNED_STACK 的标识向 getOurStackTrace 方法指示
         * 需要从 backtrace 中的信息构造 stackTrace。
         */
        if (candidateStackTrace != null) {
            // 使用 candidateStackTrace 的克隆以确保检查的一致性。
            candidateStackTrace = candidateStackTrace.clone();
            if (candidateStackTrace.length >= 1) {
                if (candidateStackTrace.length == 1 &&
                        // 检查不可变堆栈跟踪的标记
                        SentinelHolder.STACK_TRACE_ELEMENT_SENTINEL.equals(candidateStackTrace[0])) {
                    stackTrace = null;
                } else { // 验证堆栈跟踪元素非空。
                    for (StackTraceElement ste : candidateStackTrace) {
                        if (ste == null)
                            throw new NullPointerException("序列化流中的 null StackTraceElement。");
                    }
                    stackTrace = candidateStackTrace;
                }
            }
        }
        // 序列化形式中的 null stackTrace 字段可能源于较旧 JDK 版本中未包含该字段的异常序列化；
        // 将此类异常视为具有空堆栈跟踪，保持 stackTrace 分配为 UNASSIGNED_STACK 的克隆。
    }

    private int validateSuppressedExceptionsList(List<Throwable> deserSuppressedExceptions)
        throws IOException {

        boolean isBootstrapClassLoader;
        try {
            ClassLoader cl = deserSuppressedExceptions.getClass().getClassLoader();
            isBootstrapClassLoader = (cl == null);
        } catch (SecurityException exc) {
            isBootstrapClassLoader = false;
        }

        if (!isBootstrapClassLoader) {
            throw new StreamCorruptedException("列表实现类未由 Bootstrap 类加载器加载。");
        } else {
            int size = deserSuppressedExceptions.size();
            if (size < 0) {
                throw new StreamCorruptedException("报告了负列表大小。");
            }
            return size;
        }
    }

    /**
     * 将 {@code Throwable} 对象写入流。
     *
     * 序列化形式中的 {@code null} 堆栈跟踪字段表示为单元素数组，
     * 其元素等于 {@code new StackTraceElement("", "", null, Integer.MIN_VALUE)}。
     */
    private synchronized void writeObject(ObjectOutputStream s)
        throws IOException {
        // 确保 stackTrace 字段根据需要初始化为非空值。从 JDK 7 开始，
        // null 堆栈跟踪字段是指示堆栈不应设置的合法值。
        getOurStackTrace();

        StackTraceElement[] oldStackTrace = stackTrace;
        try {
            if (stackTrace == null)
                stackTrace = SentinelHolder.STACK_TRACE_SENTINEL;
            s.defaultWriteObject();
        } finally {
            stackTrace = oldStackTrace;
        }
    }

    /**
     * 将指定的异常追加到为了传递此异常而抑制的异常列表中。此方法是线程安全的，
     * 通常由 {@code try}-with-resources 语句自动且隐式调用。
     *
     * <p>除非通过 {@linkplain #Throwable(String, Throwable, boolean, boolean) 构造函数} 禁用，
     * 否则启用抑制行为。当禁用抑制时，此方法除了验证其参数外无效。
     *
     * <p>请注意，当一个异常 {@link #initCause(Throwable) 导致} 另一个异常时，
     * 通常会先捕获第一个异常，然后抛出第二个异常作为响应。换句话说，两个异常之间存在因果关系。
     *
     * 相比之下，在兄弟代码块中可能抛出两个独立的异常，特别是在 {@code try}-with-resources
     * 语句的 {@code try} 块和编译器生成的关闭资源的 {@code finally} 块中。
     *
     * 在这些情况下，只能传播一个抛出的异常。在 {@code try}-with-resources 语句中，
     * 当存在两个这样的异常时，来自 {@code try} 块的异常会被传播，
     * 而来自 {@code finally} 块的异常会被添加到由 {@code try} 块异常抑制的异常列表中。
     * 随着异常在栈上展开，它可以累积多个抑制异常。
     *
     * <p>一个异常可能同时具有抑制异常且由另一个异常引起。
     * 一个异常是否具有原因在其创建时在语义上是已知的，
     而一个异常是否会抑制其他异常通常仅在抛出异常后才确定。
     *
     * <p>请注意，程序员编写的代码也可以在存在多个兄弟异常且只有一个可以传播的情况下利用此方法。
     *
     * @param exception 要添加到抑制异常列表的异常
     * @throws IllegalArgumentException 如果 {@code exception} 是此 throwable；
     一个 throwable 不能抑制自己。
     * @throws NullPointerException 如果 {@code exception} 是 {@code null}
     * @since 1.7
     */
    public final synchronized void addSuppressed(Throwable exception) {
        if (exception == this)
            throw new IllegalArgumentException(SELF_SUPPRESSION_MESSAGE, exception);

        if (exception == null)
            throw new NullPointerException(NULL_CAUSE_MESSAGE);

        if (suppressedExceptions == null) // 不记录抑制异常
            return;

        if (suppressedExceptions == SUPPRESSED_SENTINEL)
            suppressedExceptions = new ArrayList<>(1);

        suppressedExceptions.add(exception);
    }

    private static final Throwable[] EMPTY_THROWABLE_ARRAY = new Throwable[0];

    /**
     * 返回包含所有为了传递此异常而抑制的异常的数组，通常由 {@code try}-with-resources 语句引起。
     *
     * 如果没有异常被抑制或 {@code #抑制} 通过
     * {@linkplain #Throwable(String, Throwable, boolean, boolean) 禁用}，则返回空数组。
     * 此方法是线程安全的。对返回的数组的写入不会影响对此方法的后续调用。
     *
     * @return 包含所有为了传递此异常而抑制的异常的数组。
     * @since 1.7
     */
    public final synchronized Throwable[] getSuppressed() {
        if (suppressedExceptions == SUPPRESSED_SENTINEL ||
            suppressedExceptions == null)
            return EMPTY_THROWABLE_ARRAY;
        else
            return suppressedExceptions.toArray(EMPTY_THROWABLE_ARRAY);
    }
}