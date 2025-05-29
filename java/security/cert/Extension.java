/*
 * 版权所有 (c) 2007, 2013, Oracle 和/或其附属公司。保留所有权利。
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * 此接口表示一个 X.509 扩展。
 *
 * <p>
 * 扩展提供了一种将附加属性与用户或公钥关联以及管理认证层次结构的手段。扩展格式还允许社区定义私有扩展以携带特定于这些社区的信息。
 *
 * <p>
 * 每个扩展包含一个对象标识符、一个关键性设置（指示它是关键扩展还是非关键扩展），以及一个 ASN.1 DER 编码的值。其 ASN.1 定义为：
 *
 * <pre>
 *
 *     Extension ::= SEQUENCE {
 *         extnId        OBJECT IDENTIFIER,
 *         critical      BOOLEAN DEFAULT FALSE,
 *         extnValue     OCTET STRING
 *                 -- 包含一个 DER 编码的值
 *                 -- 该值的类型已注册用于
 *                 -- extnId 对象标识符值
 *     }
 *
 * </pre>
 *
 * <p>
 * 此接口旨在提供对单个扩展的访问，与 {@link java.security.cert.X509Extension} 不同，后者更适合访问一组扩展。
 *
 * @since 1.7
 */
public interface Extension {

    /**
     * 获取扩展的对象标识符。
     *
     * @return 作为字符串的对象标识符
     */
    String getId();

    /**
     * 获取扩展的关键性设置。
     *
     * @return 如果这是关键扩展，则返回 true。
     */
    boolean isCritical();

    /**
     * 获取扩展的 DER 编码值。注意，这是编码为 OCTET STRING 的字节。它不包括 OCTET
     * STRING 标签和长度。
     *
     * @return 扩展值的副本，如果没有扩展值，则返回 {@code null}。
     */
    byte[] getValue();

    /**
     * 生成扩展的 DER 编码并将其写入输出流。
     *
     * @param out 输出流
     * @exception IOException 在编码或输出错误时抛出。
     * @exception NullPointerException 如果 {@code out} 为 {@code null} 时抛出。
     */
    void encode(OutputStream out) throws IOException;
}
