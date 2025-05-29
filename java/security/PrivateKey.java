/*
 * 版权所有 (c) 1996, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.security;

/**
 * 私钥。
 * 该接口的目的是将所有私钥接口分组（并提供类型安全）。
 * <p>
 * 注意：专门的私钥接口扩展了此接口。
 * 例如，参见 {@link java.security.interfaces} 中的 {@code DSAPrivateKey} 接口。
 * <p>
 * 实现应覆盖来自 {@link javax.security.auth.Destroyable} 接口的默认 {@code destroy} 和
 * {@code isDestroyed} 方法，以启用敏感密钥信息的销毁、清除，或在信息不可变的情况下，解除引用。
 * 最后，由于 {@code PrivateKey} 是 {@code Serializable}，实现还应覆盖
 * {@link java.io.ObjectOutputStream#writeObject(java.lang.Object)}
 * 以防止已销毁的密钥被序列化。
 *
 * @see Key
 * @see PublicKey
 * @see Certificate
 * @see Signature#initVerify
 * @see java.security.interfaces.DSAPrivateKey
 * @see java.security.interfaces.RSAPrivateKey
 * @see java.security.interfaces.RSAPrivateCrtKey
 *
 * @author Benjamin Renaud
 * @author Josh Bloch
 */

public interface PrivateKey extends Key, javax.security.auth.Destroyable {

    // 声明 serialVersionUID 以与 JDK1.1 兼容
    /**
     * 设置类指纹以指示与类的先前版本的序列化兼容性。
     */
    static final long serialVersionUID = 6034044314589513430L;
}
