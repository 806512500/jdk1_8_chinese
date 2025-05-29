
/*
 * 版权所有 (c) 2007, 2017, Oracle 和/或其附属公司。保留所有权利。
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

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.x500.X500Principal;

import sun.misc.IOUtils;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.InvalidityDateExtension;

/**
 * 一个异常，表示 X.509 证书已被撤销。{@code CertificateRevokedException} 包含有关已撤销证书的附加信息，
 * 例如证书被撤销的日期和撤销原因。
 *
 * @author Sean Mullan
 * @since 1.7
 * @see CertPathValidatorException
 */
public class CertificateRevokedException extends CertificateException {

    private static final long serialVersionUID = 7839996631571608627L;

    /**
     * @serial 证书被撤销的日期
     */
    private Date revocationDate;
    /**
     * @serial 撤销原因
     */
    private final CRLReason reason;
    /**
     * @serial 代表签署证书撤销状态信息的权威机构名称的 {@code X500Principal}
     */
    private final X500Principal authority;

    private transient Map<String, Extension> extensions;

    /**
     * 使用指定的撤销日期、原因代码、权威机构名称和扩展映射构造一个 {@code CertificateRevokedException}。
     *
     * @param revocationDate 证书被撤销的日期。日期被复制以防止后续修改。
     * @param reason 撤销原因
     * @param extensions X.509 扩展的映射。每个键是一个 OID 字符串，映射到相应的扩展。映射被复制以防止后续修改。
     * @param authority 代表签署证书撤销状态信息的权威机构名称的 {@code X500Principal}
     * @throws NullPointerException 如果 {@code revocationDate}、
     *    {@code reason}、{@code authority} 或
     *    {@code extensions} 为 {@code null}
     */
    public CertificateRevokedException(Date revocationDate, CRLReason reason,
        X500Principal authority, Map<String, Extension> extensions) {
        if (revocationDate == null || reason == null || authority == null ||
            extensions == null) {
            throw new NullPointerException();
        }
        this.revocationDate = new Date(revocationDate.getTime());
        this.reason = reason;
        this.authority = authority;
        // 确保映射中只包含正确的类型
        this.extensions = Collections.checkedMap(new HashMap<>(),
                                                 String.class, Extension.class);
        this.extensions.putAll(extensions);
    }

    /**
     * 返回证书被撤销的日期。每次调用该方法时都会返回一个新的副本，以防止后续修改。
     *
     * @return 撤销日期
     */
    public Date getRevocationDate() {
        return (Date) revocationDate.clone();
    }

    /**
     * 返回证书被撤销的原因。
     *
     * @return 撤销原因
     */
    public CRLReason getRevocationReason() {
        return reason;
    }

    /**
     * 返回签署证书撤销状态信息的权威机构的名称。
     *
     * @return 代表签署证书撤销状态信息的权威机构名称的 {@code X500Principal}
     */
    public X500Principal getAuthorityName() {
        return authority;
    }

    /**
     * 返回此 {@code CertificateRevokedException} 的无效日期扩展中指定的无效日期。
     * 无效日期是指已知或怀疑私钥被泄露或证书以其他方式变得无效的日期。此实现调用 {@code getExtensions()}
     * 并检查返回的映射中是否有无效日期扩展 OID ("2.5.29.24") 的条目。如果找到，则返回扩展中的无效日期；
     * 否则返回 null。每次调用该方法时都会返回一个新的 Date 对象，以防止后续修改。
     *
     * @return 无效日期，或如果未指定则为 {@code null}
     */
    public Date getInvalidityDate() {
        Extension ext = getExtensions().get("2.5.29.24");
        if (ext == null) {
            return null;
        } else {
            try {
                Date invalidity = InvalidityDateExtension.toImpl(ext).get("DATE");
                return new Date(invalidity.getTime());
            } catch (IOException ioe) {
                return null;
            }
        }
    }

    /**
     * 返回包含有关已撤销证书的附加信息（如无效日期扩展）的 X.509 扩展映射。每个键是一个 OID 字符串，映射到相应的扩展。
     *
     * @return 不可修改的 X.509 扩展映射，或如果没有扩展则为空映射
     */
    public Map<String, Extension> getExtensions() {
        return Collections.unmodifiableMap(extensions);
    }

    @Override
    public String getMessage() {
        return "证书已被撤销，原因: "
               + reason + ", 撤销日期: " + revocationDate
               + ", 权威机构: " + authority + ", 扩展 OID: "
               + extensions.keySet();
    }

    /**
     * 序列化此 {@code CertificateRevokedException} 实例。
     *
     * @serialData 扩展映射的大小（int），然后是映射中的所有扩展，顺序不限。对于每个扩展，
     * 发出以下数据：OID 字符串（Object），关键性标志（boolean），编码扩展值字节数组的长度（int），
     * 以及编码的扩展值字节。
     */
    private void writeObject(ObjectOutputStream oos) throws IOException {
        // 写出非瞬态字段
        // (revocationDate, reason, authority)
        oos.defaultWriteObject();


                    // 写出扩展映射的大小（映射的数量）
        oos.writeInt(extensions.size());

        // 对于映射中的每个扩展，以下内容将按顺序发出：
        // OID字符串（对象），关键性标志（布尔值），编码扩展值字节数组的长度（整数），以及编码
        // 扩展值字节数组。扩展本身没有特定的顺序。
        for (Map.Entry<String, Extension> entry : extensions.entrySet()) {
            Extension ext = entry.getValue();
            oos.writeObject(ext.getId());
            oos.writeBoolean(ext.isCritical());
            byte[] extVal = ext.getValue();
            oos.writeInt(extVal.length);
            oos.write(extVal);
        }
    }

    /**
     * 反序列化 {@code CertificateRevokedException} 实例。
     */
    private void readObject(ObjectInputStream ois)
        throws IOException, ClassNotFoundException {
        // 读取非瞬态字段
        // （撤销日期，原因，授权机构）
        ois.defaultReadObject();

        // 防御性地复制撤销日期
        revocationDate = new Date(revocationDate.getTime());

        // 读取扩展映射的大小（映射的数量）并创建扩展映射
        int size = ois.readInt();
        if (size == 0) {
            extensions = Collections.emptyMap();
        } else if (size < 0) {
            throw new IOException("size cannot be negative");
        } else {
            extensions = new HashMap<>(size > 20 ? 20 : size);
        }

        // 读取扩展并将映射放入扩展映射中
        for (int i = 0; i < size; i++) {
            String oid = (String) ois.readObject();
            boolean critical = ois.readBoolean();
            byte[] extVal = IOUtils.readExactlyNBytes(ois, ois.readInt());
            Extension ext = sun.security.x509.Extension.newExtension
                (new ObjectIdentifier(oid), critical, extVal);
            extensions.put(oid, ext);
        }
    }
}
