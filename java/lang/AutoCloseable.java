/*
 * 版权所有 (c) 2009, 2013, Oracle 及/或其附属公司。保留所有权利。
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
 * 一个可能持有资源（如文件或套接字句柄）的对象，直到它被关闭。当退出一个 {@code
 * try}-with-resources 块时，如果该对象在资源规范头中声明，那么 {@code AutoCloseable}
 * 对象的 {@link #close()} 方法将自动被调用。这种构造确保资源的及时释放，避免因资源耗尽而引起的异常和错误。
 *
 * @apiNote
 * <p>基类可以实现 AutoCloseable，即使不是所有子类或实例都持有可释放的资源。对于必须完全通用的代码，或者当已知 {@code AutoCloseable}
 * 实例需要释放资源时，建议使用 {@code try}-with-resources 构造。然而，当使用如 {@link java.util.stream.Stream}
 * 这样的设施时，它既支持基于 I/O 的形式，也支持非基于 I/O 的形式，使用非基于 I/O 的形式时，通常不需要使用 {@code try}-with-resources 块。
 *
 * @author Josh Bloch
 * @since 1.7
 */
public interface AutoCloseable {
    /**
     * 关闭此资源，释放任何底层资源。此方法在由 {@code try}-with-resources 语句管理的对象上自动调用。
     *
     * <p>虽然此接口方法声明抛出 {@code Exception}，但实现者强烈建议声明 {@code close} 方法的具体实现抛出更具体的异常，或者如果关闭操作不会失败，则根本不抛出异常。
     *
     * <p>关闭操作可能失败的情况需要实现者仔细注意。强烈建议在抛出异常之前释放底层资源并内部 <em>标记</em> 资源为已关闭。由于 {@code
     * close} 方法不太可能被调用多次，因此这确保了资源能够及时释放。此外，这减少了当资源被另一个资源包装或包装其他资源时可能出现的问题。
     *
     * <p><em>实现此接口的实现者还强烈建议不要让 {@code close} 方法抛出 {@link
     * InterruptedException}。</em>
     *
     * 该异常与线程的中断状态交互，如果 {@code InterruptedException} 被 {@linkplain Throwable#addSuppressed
     * 抑制}，则可能会导致运行时行为异常。
     *
     * 更广泛地说，如果异常被抑制会导致问题，那么 {@code AutoCloseable.close} 方法就不应该抛出该异常。
     *
     * <p>请注意，与 {@link java.io.Closeable#close close}
     * 方法不同，此 {@code close} 方法 <em>不要求</em> 是幂等的。换句话说，多次调用此 {@code close} 方法可能会有一些可见的副作用，而 {@code Closeable.close}
     * 要求多次调用时没有效果。
     *
     * 然而，强烈建议实现此接口的实现者使其 {@code close} 方法幂等。
     *
     * @throws Exception 如果此资源无法关闭
     */
    void close() throws Exception;
}