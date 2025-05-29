/*
 * 版权所有 (c) 1997, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.util.jar;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.security.CodeSigner;
import java.security.cert.Certificate;

/**
 * 该类用于表示 JAR 文件条目。
 */
public
class JarEntry extends ZipEntry {
    Attributes attr;
    Certificate[] certs;
    CodeSigner[] signers;

    /**
     * 为指定的 JAR 文件条目名称创建一个新的 <code>JarEntry</code>。
     *
     * @param name JAR 文件条目名称
     * @exception NullPointerException 如果条目名称为 <code>null</code>
     * @exception IllegalArgumentException 如果条目名称长度超过 0xFFFF 字节。
     */
    public JarEntry(String name) {
        super(name);
    }

    /**
     * 从指定的 <code>ZipEntry</code> 对象的字段创建一个新的 <code>JarEntry</code>。
     * @param ze 用于创建 <code>JarEntry</code> 的 <code>ZipEntry</code> 对象
     */
    public JarEntry(ZipEntry ze) {
        super(ze);
    }

    /**
     * 从指定的 <code>JarEntry</code> 对象的字段创建一个新的 <code>JarEntry</code>。
     *
     * @param je 要复制的 <code>JarEntry</code>
     */
    public JarEntry(JarEntry je) {
        this((ZipEntry)je);
        this.attr = je.attr;
        this.certs = je.certs;
        this.signers = je.signers;
    }

    /**
     * 返回此条目的 <code>Manifest</code> <code>Attributes</code>，如果不存在则返回 <code>null</code>。
     *
     * @return 此条目的 <code>Manifest</code> <code>Attributes</code>，如果不存在则返回 <code>null</code>
     * @throws IOException  如果发生 I/O 错误
     */
    public Attributes getAttributes() throws IOException {
        return attr;
    }

    /**
     * 返回此条目的 <code>Certificate</code> 对象，如果不存在则返回 <code>null</code>。此方法只能在
     * <code>JarEntry</code> 已通过从条目输入流读取直到流的末尾来完全验证后调用。否则，此方法将返回 <code>null</code>。
     *
     * <p>返回的证书数组包括用于验证此条目的所有签名者证书。每个签名者证书后面跟着其支持的证书链（可能为空）。
     * 每个签名者证书及其支持的证书链按自下而上的顺序排列（即，签名者证书在前，（根）证书机构在后）。
     *
     * @return 此条目的 <code>Certificate</code> 对象，如果不存在则返回 <code>null</code>。
     */
    public Certificate[] getCertificates() {
        return certs == null ? null : certs.clone();
    }

    /**
     * 返回此条目的 <code>CodeSigner</code> 对象，如果不存在则返回 <code>null</code>。此方法只能在
     * <code>JarEntry</code> 已通过从条目输入流读取直到流的末尾来完全验证后调用。否则，此方法将返回 <code>null</code>。
     *
     * <p>返回的数组包括所有已签署此条目的代码签名者。
     *
     * @return 此条目的 <code>CodeSigner</code> 对象，如果不存在则返回 <code>null</code>。
     *
     * @since 1.5
     */
    public CodeSigner[] getCodeSigners() {
        return signers == null ? null : signers.clone();
    }
}
