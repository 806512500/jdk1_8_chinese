/*
 * 版权所有 (c) 2000, 2013，Oracle 和/或其附属公司。保留所有权利。
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

package java.security.cert;

import java.security.GeneralSecurityException;

/**
 * 一个异常，指示在使用 {@code CertPathBuilder} 构建认证路径时遇到的各种问题。
 * <p>
 * {@code CertPathBuilderException} 提供了包装异常的支持。{@link #getCause getCause} 方法返回导致此异常被抛出的可抛出对象，如果有的话。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 除非另有说明，否则本类中定义的方法不是线程安全的。需要并发访问单个对象的多个线程应相互同步并提供必要的锁定。每个操作独立对象的多个线程不需要同步。
 *
 * @see CertPathBuilder
 *
 * @since       1.4
 * @author      Sean Mullan
 */
public class CertPathBuilderException extends GeneralSecurityException {

    private static final long serialVersionUID = 5316471420178794402L;

    /**
     * 创建一个带有 {@code null} 作为其详细消息的 {@code CertPathBuilderException}。
     */
    public CertPathBuilderException() {
        super();
    }

    /**
     * 使用给定的详细消息创建一个 {@code CertPathBuilderException}。详细消息是一个 {@code String}，用于更详细地描述此特定异常。
     *
     * @param msg 详细消息
     */
    public CertPathBuilderException(String msg) {
        super(msg);
    }

    /**
     * 创建一个包装指定可抛出对象的 {@code CertPathBuilderException}。这允许将任何异常转换为 {@code CertPathBuilderException}，同时保留有关包装异常的信息，这些信息对于调试可能有用。详细消息设置为 ({@code cause==null ? null : cause.toString()})
     * （通常包含 cause 的类和详细消息）。
     *
     * @param cause 由 {@link #getCause getCause()} 方法稍后检索的原因。（允许 {@code null} 值，表示原因不存在或未知。）
     */
    public CertPathBuilderException(Throwable cause) {
        super(cause);
    }

    /**
     * 使用指定的详细消息和原因创建一个 {@code CertPathBuilderException}。
     *
     * @param msg 详细消息
     * @param  cause 由 {@link #getCause getCause()} 方法稍后检索的原因。（允许 {@code null} 值，表示原因不存在或未知。）
     */
    public CertPathBuilderException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
