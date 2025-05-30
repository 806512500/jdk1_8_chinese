
/*
 * Copyright (c) 1994, 2019, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

import java.io.*;
import java.util.*;

/**
 * {@code Throwable} 类是 Java 语言中所有错误和异常的超类。只有这个类（或其子类）的实例才能被 Java 虚拟机抛出或通过 Java {@code throw} 语句抛出。同样，只有这个类或其子类可以作为 {@code catch} 子句的参数类型。
 *
 * 对于编译时异常检查，{@code Throwable} 和任何不是 {@link RuntimeException} 或 {@link Error} 子类的 {@code Throwable} 子类都被视为检查型异常。
 *
 * <p>通常，两个子类的实例，{@link java.lang.Error} 和 {@link java.lang.Exception}，用于表示发生了异常情况。通常，这些实例在异常情况的上下文中新创建，以便包含相关信息（如堆栈跟踪数据）。
 *
 * <p>可抛出对象包含其创建时线程的执行堆栈快照。它还可以包含一条消息，提供有关错误的更多信息。随时间推移，可抛出对象可以 {@linkplain Throwable#addSuppressed 抑制} 其他可抛出对象的传播。最后，可抛出对象还可以包含一个 <i>原因</i>：另一个导致此可抛出对象被构造的可抛出对象。记录这种因果信息称为 <i>链式异常</i> 机制，因为原因本身可以有原因，依此类推，形成一个“链”。
 *
 * <p>可抛出对象可能有原因的一个原因是，抛出它的类建立在较低层次的抽象之上，而上层的操作因较低层次的失败而失败。让较低层次抛出的可抛出对象向外传播是不好的设计，因为这通常与上层提供的抽象无关。此外，这样做会将上层的 API 与其实现的细节绑定在一起，假设较低层次的异常是检查型异常。抛出一个“包装异常”（即包含原因的异常）允许上层向其调用者传达失败的详细信息，而不会产生这些缺点。它保留了在不改变 API（特别是其方法抛出的异常集）的情况下更改上层实现的灵活性。
 *
 * <p>可抛出对象可能有原因的另一个原因是，抛出它的方法必须符合一个通用接口，该接口不允许该方法直接抛出原因。例如，假设一个持久化集合符合 {@link java.util.Collection Collection} 接口，其持久化实现基于 {@code java.io}。假设 {@code add} 方法的内部可以抛出一个 {@link java.io.IOException IOException}。实现可以通过将 {@code IOException} 包装在一个适当的未检查异常中，向其调用者传达 {@code IOException} 的详细信息，同时符合 {@code Collection} 接口。（持久化集合的规范应指出它可以抛出此类异常。）
 *
 * <p>原因可以通过两种方式与可抛出对象关联：通过一个接受原因作为参数的构造函数，或通过 {@link #initCause(Throwable)} 方法。新的可抛出类如果希望允许与原因关联，应提供接受原因的构造函数，并委托（可能是间接地）给接受原因的 {@code Throwable} 构造函数之一。
 *
 * 由于 {@code initCause} 方法是公共的，因此它允许将原因与任何可抛出对象关联，即使该可抛出对象的实现早于添加到 {@code Throwable} 的异常链式机制。
 *
 * <p>按照惯例，类 {@code Throwable} 及其子类有两个构造函数，一个不接受任何参数，一个接受一个 {@code String} 参数，可以用于生成详细消息。此外，那些可能有原因关联的子类应再提供两个构造函数，一个接受一个 {@code Throwable}（原因），一个接受一个 {@code String}（详细消息）和一个 {@code Throwable}（原因）。
 *
 * @author  未署名
 * @author  Josh Bloch（在 1.4 版本中添加了异常链式机制和对堆栈跟踪的程序访问）
 * @jls 11.2 编译时异常检查
 * @since JDK1.0
 */
public class Throwable implements Serializable {
    /** 为了互操作性，使用 JDK 1.0.2 的 serialVersionUID */
    private static final long serialVersionUID = -3042686055658047285L;

    /**
     * 本槽位由本地代码保存一些堆栈回溯的指示。
     */
    private transient Object backtrace;

    /**
     * 关于可抛出对象的具体细节。例如，对于 {@code FileNotFoundException}，这包含无法找到的文件名。
     *
     * @serial
     */
    private String detailMessage;

    /**
     * 持有类，用于延迟初始化仅用于序列化的哨兵对象。
     */
    private static class SentinelHolder {
        /**
         * {@linkplain #setStackTrace(StackTraceElement[]) 设置堆栈跟踪} 为包含此哨兵值的单元素数组，表示未来尝试设置堆栈跟踪将被忽略。哨兵等于调用以下内容的结果：<br>
         * {@code new StackTraceElement("", "", null, Integer.MIN_VALUE)}
         */
        public static final StackTraceElement STACK_TRACE_ELEMENT_SENTINEL =
            new StackTraceElement("", "", null, Integer.MIN_VALUE);

        /**
         * 用于序列化形式中的哨兵值，表示不可变的堆栈跟踪。
         */
        public static final StackTraceElement[] STACK_TRACE_SENTINEL =
            new StackTraceElement[] {STACK_TRACE_ELEMENT_SENTINEL};
    }

    /**
     * 用于空堆栈的共享值。
     */
    private static final StackTraceElement[] UNASSIGNED_STACK = new StackTraceElement[0];

    /*
     * 为了允许可抛出对象被 JVM 安全地重用，例如 OutOfMemoryErrors，可抛出对象中可由用户操作的字段（原因、堆栈跟踪和抑制异常）遵循以下协议：
     *
     * 1) 字段被初始化为非空的哨兵值，表示该值逻辑上尚未设置。
     *
     * 2) 向字段写入 null 表示禁止进一步写入。
     *
     * 3) 哨兵值可以用另一个非空值替换。
     *
     * 例如，HotSpot JVM 的实现预先分配了 OutOfMemoryError 对象，以提高该情况的诊断能力。这些对象是在不调用该类的构造函数的情况下创建的，相关字段被初始化为 null。为了支持这种能力，任何需要初始化为非空值的新字段都需要协调的 JVM 变更。
     */

    /**
     * 导致此可抛出对象被抛出的可抛出对象，或者如果此可抛出对象不是由另一个可抛出对象引起的，或者原因未知，则为 null。如果此字段等于此可抛出对象本身，表示此可抛出对象的原因尚未初始化。
     *
     * @serial
     * @since 1.4
     */
    private Throwable cause = this;

    /**
     * 由 {@link #getStackTrace()} 返回的堆栈跟踪。
     *
     * 该字段被初始化为零长度数组。该字段的值为 {@code null} 表示后续对 {@link #setStackTrace(StackTraceElement[])} 和 {@link #fillInStackTrace()} 的调用将被忽略。
     *
     * @serial
     * @since 1.4
     */
    private StackTraceElement[] stackTrace = UNASSIGNED_STACK;

    // 设置此静态字段会引入对少数 java.util 类的可接受的初始化依赖。
    private static final List<Throwable> SUPPRESSED_SENTINEL =
        Collections.unmodifiableList(new ArrayList<Throwable>(0));

    /**
     * 由 {@link #getSuppressed()} 返回的抑制异常列表。该列表被初始化为零元素的不可修改哨兵列表。当序列化的可抛出对象被读取时，如果 {@code suppressedExceptions} 字段指向零元素列表，该字段将被重置为哨兵值。
     *
     * @serial
     * @since 1.7
     */
    private List<Throwable> suppressedExceptions = SUPPRESSED_SENTINEL;

    /** 尝试抑制 null 异常时的消息。 */
    private static final String NULL_CAUSE_MESSAGE = "Cannot suppress a null exception.";

    /** 尝试抑制自身时的消息。 */
    private static final String SELF_SUPPRESSION_MESSAGE = "Self-suppression not permitted";

    /** 用于标记因果异常堆栈跟踪的标题 */
    private static final String CAUSE_CAPTION = "Caused by: ";

    /** 用于标记抑制异常堆栈跟踪的标题 */
    private static final String SUPPRESSED_CAPTION = "Suppressed: ";

    /**
     * 构造一个新的可抛出对象，其详细消息为 {@code null}。原因未初始化，可以通过调用 {@link #initCause} 进行初始化。
     *
     * <p>调用 {@link #fillInStackTrace()} 方法初始化新创建的可抛出对象中的堆栈跟踪数据。
     */
    public Throwable() {
        fillInStackTrace();
    }

    /**
     * 构造一个新的可抛出对象，具有指定的详细消息。原因未初始化，可以通过调用 {@link #initCause} 进行初始化。
     *
     * <p>调用 {@link #fillInStackTrace()} 方法初始化新创建的可抛出对象中的堆栈跟踪数据。
     *
     * @param   message   详细消息。详细消息将保存以供 {@link #getMessage()} 方法稍后检索。
     */
    public Throwable(String message) {
        fillInStackTrace();
        detailMessage = message;
    }

    /**
     * 构造一个新的可抛出对象，具有指定的详细消息和原因。注意，与 {@code cause} 关联的详细消息 <i>不会</i> 自动包含在此可抛出对象的详细消息中。
     *
     * <p>调用 {@link #fillInStackTrace()} 方法初始化新创建的可抛出对象中的堆栈跟踪数据。
     *
     * @param  message 详细消息（将保存以供 {@link #getMessage()} 方法稍后检索）。
     * @param  cause 原因（将保存以供 {@link #getCause()} 方法稍后检索）。允许 {@code null} 值，表示原因不存在或未知。
     * @since  1.4
     */
    public Throwable(String message, Throwable cause) {
        fillInStackTrace();
        detailMessage = message;
        this.cause = cause;
    }

    /**
     * 构造一个新的可抛出对象，具有指定的原因和详细消息 {@code (cause==null ? null : cause.toString())}（通常包含 {@code cause} 的类和详细消息）。此构造函数适用于那些基本上是其他可抛出对象包装器的可抛出对象（例如，{@link java.security.PrivilegedActionException}）。
     *
     * <p>调用 {@link #fillInStackTrace()} 方法初始化新创建的可抛出对象中的堆栈跟踪数据。
     *
     * @param  cause 原因（将保存以供 {@link #getCause()} 方法稍后检索）。允许 {@code null} 值，表示原因不存在或未知。
     * @since  1.4
     */
    public Throwable(Throwable cause) {
        fillInStackTrace();
        detailMessage = (cause==null ? null : cause.toString());
        this.cause = cause;
    }

    /**
     * 构造一个新的可抛出对象，具有指定的详细消息、原因、抑制启用或禁用以及可写堆栈跟踪启用或禁用。如果抑制被禁用，{@link #getSuppressed} 对此对象将返回零长度数组，对 {@link #addSuppressed} 的调用将不会将异常添加到抑制列表中。如果可写堆栈跟踪为 false，此构造函数将不会调用 {@link #fillInStackTrace()}，{@code null} 将被写入 {@code stackTrace} 字段，后续对 {@code fillInStackTrace} 和 {@link #setStackTrace(StackTraceElement[])} 的调用将不会设置堆栈跟踪。如果可写堆栈跟踪为 false，{@link #getStackTrace} 将返回零长度数组。
     *
     * <p>注意，{@code Throwable} 的其他构造函数将抑制视为启用，堆栈跟踪视为可写。{@code Throwable} 的子类应记录任何抑制被禁用的条件，并记录堆栈跟踪不可写的情况。只有在特殊情况下才应禁用抑制，例如虚拟机在低内存情况下重用异常对象。在给定异常对象被反复捕获和抛出的情况下，例如用于实现两个子系统之间的控制流，也是使用不可变可抛出对象的适当情况。
     *
     * @param  message 详细消息。
     * @param cause 原因。允许 {@code null} 值，表示原因不存在或未知。
     * @param enableSuppression 是否启用或禁用抑制
     * @param writableStackTrace 是否应使堆栈跟踪可写
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
     * 返回此可抛出对象的详细消息字符串。
     *
     * @return  此 {@code Throwable} 实例的详细消息字符串（可能为 {@code null}）。
     */
    public String getMessage() {
        return detailMessage;
    }

    /**
     * 创建此可抛出对象的本地化描述。
     * 子类可以重写此方法以生成特定于区域设置的消息。 对于不重写此方法的子类，
     * 默认实现返回与 {@code getMessage()} 相同的结果。
     *
     * @return  此可抛出对象的本地化描述。
     * @since   JDK1.1
     */
    public String getLocalizedMessage() {
        return getMessage();
    }

    /**
     * 返回此可抛出对象的原因或如果原因不存在或未知则返回 {@code null}。
     * （原因是导致此可抛出对象被抛出的可抛出对象。）
     *
     * <p>此实现返回通过需要 {@code Throwable} 的构造函数之一提供的原因，或在创建后使用
     * {@link #initCause(Throwable)} 方法设置的原因。 虽然通常不需要重写此方法，但子类可以重写
     * 它以返回通过其他方式设置的原因。 这适用于早于向 {@code Throwable} 添加链式异常的“遗留链式可抛出对象”。
     * 请注意，不需要重写任何 {@code PrintStackTrace} 方法，所有这些方法都会调用 {@code getCause} 方法来确定
     * 可抛出对象的原因。
     *
     * @return  此可抛出对象的原因或如果原因不存在或未知则返回 {@code null}。
     * @since 1.4
     */
    public synchronized Throwable getCause() {
        return (cause==this ? null : cause);
    }

    /**
     * 将此可抛出对象的 <i>原因</i> 初始化为指定的值。
     * （原因是导致此可抛出对象被抛出的可抛出对象。）
     *
     * <p>此方法最多只能调用一次。 通常在构造函数内或在创建可抛出对象后立即调用。
     * 如果此可抛出对象是使用 {@link #Throwable(Throwable)} 或
     * {@link #Throwable(String,Throwable)} 创建的，则此方法不能调用。
     *
     * <p>一个在没有其他支持设置原因的遗留可抛出类型上使用此方法的示例是：
     *
     * <pre>
     * try {
     *     lowLevelOp();
     * } catch (LowLevelException le) {
     *     throw (HighLevelException)
     *           new HighLevelException().initCause(le); // Legacy constructor
     * }
     * </pre>
     *
     * @param  cause 原因（稍后通过 {@link #getCause()} 方法检索）。 （允许 {@code null} 值，表示原因不存在或未知。）
     * @return  对此 {@code Throwable} 实例的引用。
     * @throws IllegalArgumentException 如果 {@code cause} 是此可抛出对象。 （可抛出对象不能是自己的原因。）
     * @throws IllegalStateException 如果此可抛出对象是使用 {@link #Throwable(Throwable)} 或
     *         {@link #Throwable(String,Throwable)} 创建的，或者此方法已经在此可抛出对象上调用过。
     * @since  1.4
     */
    public synchronized Throwable initCause(Throwable cause) {
        if (this.cause != this)
            throw new IllegalStateException("Can't overwrite cause with " +
                                            Objects.toString(cause, "a null"), this);
        if (cause == this)
            throw new IllegalArgumentException("Self-causation not permitted", this);
        this.cause = cause;
        return this;
    }

    /**
     * 返回此可抛出对象的简短描述。
     * 结果是以下内容的连接：
     * <ul>
     * <li> 此对象的类的 {@linkplain Class#getName() 名称}
     * <li> ": "（一个冒号和一个空格）
     * <li> 调用此对象的 {@link #getLocalizedMessage} 方法的结果
     * </ul>
     * 如果 {@code getLocalizedMessage} 返回 {@code null}，则仅返回类名。
     *
     * @return 此可抛出对象的字符串表示形式。
     */
    public String toString() {
        String s = getClass().getName();
        String message = getLocalizedMessage();
        return (message != null) ? (s + ": " + message) : s;
    }

    /**
     * 将此可抛出对象及其调用堆栈打印到标准错误流。此方法将此
     * {@code Throwable} 对象的调用堆栈打印到标准错误流，该流是
     * {@code System.err} 字段的值。第一行输出包含此对象的 {@link #toString()} 方法的结果。
     * 剩余的行表示之前由 {@link #fillInStackTrace()} 方法记录的数据。此信息的格式取决于实现，但以下
     * 示例可以视为典型：
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
     * 具有已初始化且非空原因的可抛出对象的调用堆栈通常应包括原因的调用堆栈。此信息的格式取决于实现，但以下
     * 示例可以视为典型：
     * <pre>
     * HighLevelException: MidLevelException: LowLevelException
     *         at Junk.a(Junk.java:13)
     *         at Junk.main(Junk.java:4)
     * Caused by: MidLevelException: LowLevelException
     *         at Junk.c(Junk.java:23)
     *         at Junk.b(Junk.java:17)
     *         at Junk.a(Junk.java:11)
     *         ... 1 more
     * Caused by: LowLevelException
     *         at Junk.e(Junk.java:30)
     *         at Junk.d(Junk.java:27)
     *         at Junk.c(Junk.java:21)
     *         ... 3 more
     * </pre>
     * 注意包含字符 {@code "..."} 的行。这些行表示此异常的调用堆栈的其余部分与导致此异常的异常（“包含异常”）的
     * 调用堆栈底部的指定数量的帧匹配。这种简写可以大大减少输出的长度，尤其是在包装异常从捕获“原因异常”的同一方法中抛出的常见情况下。
     * 上述示例是通过运行以下程序生成的：
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
     * 从 7 版本开始，平台支持“抑制异常”（与 {@code try}-with-resources 语句一起使用）。为了抛出异常而抑制的任何异常
     * 都会打印在调用堆栈下方。此信息的格式取决于实现，但以下示例可以视为典型：
     *
     * <pre>
     * Exception in thread "main" java.lang.Exception: Something happened
     *  at Foo.bar(Foo.java:10)
     *  at Foo.main(Foo.java:5)
     *  Suppressed: Resource$CloseFailException: Resource ID = 0
     *          at Resource.close(Resource.java:26)
     *          at Foo.bar(Foo.java:9)
     *          ... 1 more
     * </pre>
     * 注意，抑制异常使用“... n more”表示法，就像原因一样。与原因不同，抑制异常的缩进超出其“包含异常”。
     *
     * <p>异常可以同时具有原因和一个或多个抑制异常：
     * <pre>
     * Exception in thread "main" java.lang.Exception: Main block
     *  at Foo3.main(Foo3.java:7)
     *  Suppressed: Resource$CloseFailException: Resource ID = 2
     *          at Resource.close(Resource.java:26)
     *          at Foo3.main(Foo3.java:5)
     *  Suppressed: Resource$CloseFailException: Resource ID = 1
     *          at Resource.close(Resource.java:26)
     *          at Foo3.main(Foo3.java:5)
     * Caused by: java.lang.Exception: I did it
     *  at Foo3.main(Foo3.java:8)
     * </pre>
     * 同样，抑制异常可以有原因：
     * <pre>
     * Exception in thread "main" java.lang.Exception: Main block
     *  at Foo4.main(Foo4.java:6)
     *  Suppressed: Resource2$CloseFailException: Resource ID = 1
     *          at Resource2.close(Resource2.java:20)
     *          at Foo4.main(Foo4.java:5)
     *  Caused by: java.lang.Exception: Rats, you caught me
     *          at Resource2$CloseFailException.&lt;init&gt;(Resource2.java:45)
     *          ... 2 more
     * </pre>
     */
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * 将此可抛出对象及其调用堆栈打印到指定的打印流。
     *
     * @param s 用于输出的 {@code PrintStream}
     */
    public void printStackTrace(PrintStream s) {
        printStackTrace(new WrappedPrintStream(s));
    }

    private void printStackTrace(PrintStreamOrWriter s) {
        // 防止恶意重写 Throwable.equals 通过使用具有身份相等语义的 Set。
        Set<Throwable> dejaVu =
            Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
        dejaVu.add(this);

        synchronized (s.lock()) {
            // 打印我们的调用堆栈
            s.println(this);
            StackTraceElement[] trace = getOurStackTrace();
            for (StackTraceElement traceElement : trace)
                s.println("\tat " + traceElement);

            // 打印抑制异常，如果有
            for (Throwable se : getSuppressed())
                se.printEnclosedStackTrace(s, trace, SUPPRESSED_CAPTION, "\t", dejaVu);

            // 打印原因，如果有
            Throwable ourCause = getCause();
            if (ourCause != null)
                ourCause.printEnclosedStackTrace(s, trace, CAUSE_CAPTION, "", dejaVu);
        }
    }

    /**
     * 为指定的调用堆栈打印我们的调用堆栈作为嵌套异常。
     */
    private void printEnclosedStackTrace(PrintStreamOrWriter s,
                                         StackTraceElement[] enclosingTrace,
                                         String caption,
                                         String prefix,
                                         Set<Throwable> dejaVu) {
        assert Thread.holdsLock(s.lock());
        if (dejaVu.contains(this)) {
            s.println(prefix + caption + "[CIRCULAR REFERENCE: " + this + "]");
        } else {
            dejaVu.add(this);
            // 计算此调用堆栈与包含调用堆栈之间相同的帧数
            StackTraceElement[] trace = getOurStackTrace();
            int m = trace.length - 1;
            int n = enclosingTrace.length - 1;
            while (m >= 0 && n >=0 && trace[m].equals(enclosingTrace[n])) {
                m--; n--;
            }
            int framesInCommon = trace.length - 1 - m;

            // 打印我们的调用堆栈
            s.println(prefix + caption + this);
            for (int i = 0; i <= m; i++)
                s.println(prefix + "\tat " + trace[i]);
            if (framesInCommon != 0)
                s.println(prefix + "\t... " + framesInCommon + " more");

            // 打印抑制异常，如果有
            for (Throwable se : getSuppressed())
                se.printEnclosedStackTrace(s, trace, SUPPRESSED_CAPTION,
                                           prefix +"\t", dejaVu);

            // 打印原因，如果有
            Throwable ourCause = getCause();
            if (ourCause != null)
                ourCause.printEnclosedStackTrace(s, trace, CAUSE_CAPTION, prefix, dejaVu);
        }
    }

    /**
     * 将此可抛出对象及其调用堆栈打印到指定的打印写入器。
     *
     * @param s 用于输出的 {@code PrintWriter}
     * @since   JDK1.1
     */
    public void printStackTrace(PrintWriter s) {
        printStackTrace(new WrappedPrintWriter(s));
    }

    /**
     * 打印流和打印写入器的包装类，以实现 printStackTrace 的单一实现。
     */
    private abstract static class PrintStreamOrWriter {
        /** 返回使用此 StreamOrWriter 时要锁定的对象 */
        abstract Object lock();

        /** 在此 StreamOrWriter 上将指定的字符串作为一行打印 */
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
     * 填充执行调用堆栈。此方法在此
     * {@code Throwable} 对象中记录当前线程的调用帧的当前状态。
     *
     * <p>如果此 {@code Throwable} 的调用堆栈 {@linkplain
     * Throwable#Throwable(String, Throwable, boolean, boolean) 不可写}，调用此方法没有效果。
     *
     * @return  对此 {@code Throwable} 实例的引用。
     * @see     java.lang.Throwable#printStackTrace()
     */
    public synchronized Throwable fillInStackTrace() {
        if (stackTrace != null ||
            backtrace != null /* Out of protocol state */ ) {
            fillInStackTrace(0);
            stackTrace = UNASSIGNED_STACK;
        }
        return this;
    }


                private native Throwable fillInStackTrace(int dummy);

    /**
     * 提供对 {@link #printStackTrace()} 打印的堆栈跟踪信息的程序化访问。返回一个堆栈跟踪元素数组，
     * 每个元素代表一个堆栈帧。数组的零元素（假设数组的长度非零）代表堆栈的顶部，即序列中的最后一个方法调用。
     * 通常，这是创建并抛出此异常的点。数组的最后一个元素（假设数组的长度非零）代表堆栈的底部，即序列中的第一个方法调用。
     *
     * <p>在某些情况下，某些虚拟机可能会省略一个或多个堆栈帧。在极端情况下，没有关于此异常的堆栈跟踪信息的虚拟机
     * 被允许从该方法返回一个零长度的数组。通常，此方法返回的数组将包含与 {@code printStackTrace} 打印的帧数量相同的元素。
     * 对返回的数组的写入不会影响此方法的未来调用。
     *
     * @return 一个堆栈跟踪元素数组，表示与此异常相关的堆栈跟踪。
     * @since  1.4
     */
    public StackTraceElement[] getStackTrace() {
        return getOurStackTrace().clone();
    }

    private synchronized StackTraceElement[] getOurStackTrace() {
        // 如果这是调用此方法的第一次，则使用来自 backtrace 的信息初始化堆栈跟踪字段
        if (stackTrace == UNASSIGNED_STACK ||
            (stackTrace == null && backtrace != null) /* 协议外状态 */) {
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
     * 设置将由 {@link #getStackTrace()} 返回并由 {@link #printStackTrace()} 和相关方法打印的堆栈跟踪元素。
     *
     * 该方法旨在供 RPC 框架和其他高级系统使用，允许客户端覆盖默认的堆栈跟踪，该堆栈跟踪要么在构造异常时由 {@link #fillInStackTrace()}
     * 生成，要么在从序列化流中读取异常时反序列化。
     *
     * <p>如果此 {@code Throwable} 的堆栈跟踪 {@linkplain
     * Throwable#Throwable(String, Throwable, boolean, boolean) 不可写}，调用此方法除了验证其参数外不会产生任何其他效果。
     *
     * @param   stackTrace 要与此 {@code Throwable} 关联的堆栈跟踪元素。指定的数组在此调用中被复制；方法调用返回后对指定数组的更改
     *         不会影响此 {@code Throwable} 的堆栈跟踪。
     *
     * @throws NullPointerException 如果 {@code stackTrace} 为
     *         {@code null} 或者 {@code stackTrace} 的任何元素为 {@code null}
     *
     * @since  1.4
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
                backtrace == null) // 测试协议外状态
                return;
            this.stackTrace = defensiveCopy;
        }
    }

    /**
     * 返回堆栈跟踪中的元素数量（如果堆栈跟踪不可用，则返回 0）。
     *
     * 包级保护，供 SharedSecrets 使用。
     */
    native int getStackTraceDepth();

    /**
     * 返回堆栈跟踪的指定元素。
     *
     * 包级保护，供 SharedSecrets 使用。
     *
     * @param index 要返回的元素的索引。
     * @throws IndexOutOfBoundsException 如果 {@code index < 0 ||
     *         index >= getStackTraceDepth() }
     */
    native StackTraceElement getStackTraceElement(int index);

    /**
     * 从流中读取一个 {@code Throwable}，强制执行字段的格式良好性约束。不允许 {@code
     * suppressedExceptions} 列表中的空条目和自指针。不允许堆栈跟踪元素的空条目。序列化形式中的空堆栈跟踪
     * 导致零长度的堆栈元素数组。单元素堆栈跟踪，其条目等于 {@code new StackTraceElement("",
     * "", null, Integer.MIN_VALUE)}，导致 {@code null} 的 {@code
     * stackTrace} 字段。
     *
     * 注意，对 {@code cause} 字段的值没有约束；{@code null} 和 {@code this} 都是该字段的有效值。
     */
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        s.defaultReadObject();     // 读取所有字段

        // 设置抑制异常和堆栈跟踪元素字段
        // 直到从序列化流中验证内容为止，将它们设置为标记值。
        List<Throwable> candidateSuppressedExceptions = suppressedExceptions;
        suppressedExceptions = SUPPRESSED_SENTINEL;

        StackTraceElement[] candidateStackTrace = stackTrace;
        stackTrace = UNASSIGNED_STACK.clone();

        if (candidateSuppressedExceptions != null) {
            int suppressedSize = validateSuppressedExceptionsList(candidateSuppressedExceptions);
            if (suppressedSize > 0) { // 将有效的 Throwable 复制到新列表中
                List<Throwable> suppList  = new ArrayList<Throwable>(Math.min(100, suppressedSize));

                for (Throwable t : candidateSuppressedExceptions) {
                    // 强制执行抑制异常的约束，以防流损坏或恶意。
                    if (t == null)
                        throw new NullPointerException(NULL_CAUSE_MESSAGE);
                    if (t == this)
                        throw new IllegalArgumentException(SELF_SUPPRESSION_MESSAGE);
                    suppList.add(t);
                }
                // 如果有任何无效的抑制异常，
                // 隐式使用之前分配的哨兵值。
                suppressedExceptions = suppList;
            }
        } else {
            suppressedExceptions = null;
        }

        /*
         * 对于零长度的堆栈跟踪，使用 UNASSIGNED_STACK 的克隆而不是 UNASSIGNED_STACK 本身，以
         * 允许在 getOurStackTrace 中使用 UNASSIGNED_STACK 进行身份比较。UNASSIGNED_STACK 在
         * stackTrace 中的身份表示 getOurStackTrace 方法需要从 backtrace 中构建 stackTrace。
         */
        if (candidateStackTrace != null) {
            // 从 candidateStackTrace 的克隆中工作，以确保检查的一致性。
            candidateStackTrace = candidateStackTrace.clone();
            if (candidateStackTrace.length >= 1) {
                if (candidateStackTrace.length == 1 &&
                        // 检查不可变堆栈跟踪的标记
                        SentinelHolder.STACK_TRACE_ELEMENT_SENTINEL.equals(candidateStackTrace[0])) {
                    stackTrace = null;
                } else { // 验证堆栈跟踪元素非空。
                    for (StackTraceElement ste : candidateStackTrace) {
                        if (ste == null)
                            throw new NullPointerException("null StackTraceElement in serial stream.");
                    }
                    stackTrace = candidateStackTrace;
                }
            }
        }
        // 序列化形式中的空 stackTrace 字段可能来自没有该字段的旧 JDK 版本的异常；
        // 将这样的异常视为具有空堆栈跟踪，通过将 stackTrace 分配给 UNASSIGNED_STACK 的克隆来处理。
    }

    private int validateSuppressedExceptionsList(List<Throwable> deserSuppressedExceptions)
        throws IOException {
        if (Object.class.getClassLoader() != deserSuppressedExceptions.getClass().getClassLoader()) {
            throw new StreamCorruptedException("列表实现不在引导类路径上。");
        } else {
            int size = deserSuppressedExceptions.size();
            if (size < 0) {
                throw new StreamCorruptedException("报告了负的列表大小。");
            }
            return size;
        }
    }

    /**
     * 将一个 {@code Throwable} 对象写入流。
     *
     * 序列化形式中的空堆栈跟踪字段表示为一个元素的数组，其元素等于 {@code
     * new StackTraceElement("", "", null, Integer.MIN_VALUE)}。
     */
    private synchronized void writeObject(ObjectOutputStream s)
        throws IOException {
        // 确保 stackTrace 字段被初始化为非空值（如果适用）。从 JDK 7 开始，空的堆栈
        // 跟踪字段是表示不应设置堆栈跟踪的有效值。
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
     * 将指定的异常添加到被此异常抑制的异常列表中。此方法是线程安全的，通常由 {@code try}-with-resources 语句
     * （自动且隐式地）调用。
     *
     * <p>除非通过构造函数禁用，否则抑制行为是启用的。当禁用抑制时，此方法除了验证其参数外不执行任何操作。
     *
     * <p>当一个异常 {@linkplain
     * #initCause(Throwable) 引起} 另一个异常时，通常会捕获第一个异常，然后抛出第二个异常作为响应。换句话说，
     * 这两个异常之间存在因果关系。
     *
     * 相反，存在两种独立的异常可以在兄弟代码块中抛出的情况，特别是 {@code try}-with-resources 语句的 {@code try} 块
     * 和编译器生成的关闭资源的 {@code finally} 块中。
     *
     * 在这些情况下，只能传播一个异常。在 {@code try}-with-resources 语句中，当有两个这样的异常时，
     * 从 {@code try} 块中抛出的异常被传播，而从 {@code finally} 块中抛出的异常被添加到由 {@code try} 块抛出的异常
     * 抑制的异常列表中。随着异常解开堆栈，它可以累积多个被抑制的异常。
     *
     * <p>一个异常可能有被抑制的异常，同时也可以由另一个异常引起。一个异常是否有原因是在其创建时语义上已知的，
     * 而一个异常是否会抑制其他异常通常只有在异常被抛出后才能确定。
     *
     * <p>程序员编写的代码也可以在有多个兄弟异常且只能传播一个的情况下调用此方法。
     *
     * @param exception 要添加到被抑制异常列表中的异常
     * @throws IllegalArgumentException 如果 {@code exception} 是此异常；异常不能抑制自身。
     * @throws NullPointerException 如果 {@code exception} 为 {@code null}
     * @since 1.7
     */
    public final synchronized void addSuppressed(Throwable exception) {
        if (exception == this)
            throw new IllegalArgumentException(SELF_SUPPRESSION_MESSAGE, exception);

        if (exception == null)
            throw new NullPointerException(NULL_CAUSE_MESSAGE);

        if (suppressedExceptions == null) // 不记录被抑制的异常
            return;

        if (suppressedExceptions == SUPPRESSED_SENTINEL)
            suppressedExceptions = new ArrayList<>(1);

        suppressedExceptions.add(exception);
    }

    private static final Throwable[] EMPTY_THROWABLE_ARRAY = new Throwable[0];

    /**
     * 返回一个包含所有被抑制的异常（通常由 {@code try}-with-resources 语句）的数组，以传递此异常。
     *
     * 如果没有异常被抑制或 {@linkplain
     * #Throwable(String, Throwable, boolean, boolean) 抑制被禁用}，则返回一个空数组。此方法是线程安全的。
     * 对返回数组的写入不会影响此方法的未来调用。
     *
     * @return 一个包含所有被抑制以传递此异常的异常的数组。
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
