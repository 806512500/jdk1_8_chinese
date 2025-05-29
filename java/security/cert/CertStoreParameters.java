/*
 * 版权所有 (c) 2000, 2013, Oracle 和/或其附属公司。保留所有权利。
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

/**
 * {@code CertStore} 参数的规范。
 * <p>
 * 该接口的目的是将（并为）所有 {@code CertStore} 参数规范提供类型安全。所有
 * {@code CertStore} 参数规范都必须实现此接口。
 * <p>
 * 通常，一个 {@code CertStoreParameters} 对象作为参数传递给
 * {@link CertStore#getInstance CertStore.getInstance} 方法之一。
 * {@code getInstance} 方法返回一个用于检索 {@code Certificate} 和 {@code CRL} 的
 * {@code CertStore}。返回的 {@code CertStore} 会使用指定的参数进行初始化。不同类型的
 * {@code CertStore} 可能需要的参数类型不同。
 *
 * @see CertStore#getInstance
 *
 * @since       1.4
 * @author      Steve Hanna
 */
public interface CertStoreParameters extends Cloneable {

    /**
     * 复制此 {@code CertStoreParameters}。
     * <p>
     * “复制”的确切含义可能取决于 {@code CertStoreParameters} 对象的类。典型的实现
     * 执行此对象的“深复制”，但这不是绝对要求。某些实现可能对对象的某些或所有字段执行“浅复制”。
     * <p>
     * 请注意，{@code CertStore.getInstance} 方法会复制指定的 {@code CertStoreParameters}。
     * 深复制实现的 {@code clone} 更安全、更健壮，因为它防止调用者通过修改其初始化参数的内容来破坏共享的
     * {@code CertStore}。然而，浅复制实现的 {@code clone} 更适合需要引用
     * {@code CertStoreParameters} 中包含的参数的应用程序。例如，浅复制克隆允许应用程序立即释放特定
     * {@code CertStore} 初始化参数的资源，而不是等待垃圾回收机制。这应该非常小心地进行，因为
     * {@code CertStore} 可能仍然被其他线程使用。
     * <p>
     * 每个子类都应说明此方法的精确行为，以便用户和开发人员知道预期的行为。
     *
     * @return 此 {@code CertStoreParameters} 的副本
     */
    Object clone();
}
