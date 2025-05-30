/*
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
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
 * 一个表示 X.509 证书被撤销的异常。{@code CertificateRevokedException} 包含关于被撤销证书的附加信息，
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
     * @serial 表示签署证书撤销状态信息的权威机构名称的 {@code X500Principal}
     */
    private final X500Principal authority;

    private transient Map<String, Extension> extensions;

    /**
     * 构造一个带有指定撤销日期、原因代码、权威机构名称和扩展映射的 {@code CertificateRevokedException}。
     *
     * @param revocationDate 证书被撤销的日期。日期会被复制以防止后续修改。
     * @param reason 撤销原因
     * @param extensions X.509 扩展的映射。每个键是一个 OID 字符串，映射到相应的扩展。映射会被复制以防止后续修改。
     * @param authority 表示签署证书撤销状态信息的权威机构名称的 {@code X500Principal}
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
        // 确保映射只包含正确的类型
        this.extensions = Collections.checkedMap(new HashMap<>(),
                                                 String.class, Extension.class);
        this.extensions.putAll(extensions);
    }

    /**
     * 返回证书被撤销的日期。每次调用该方法时都会返回一个新的副本以防止后续修改。
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
     * 返回签署证书撤销状态信息的权威机构名称。
     *
     * @return 表示签署证书撤销状态信息的权威机构名称的 {@code X500Principal}
     */
    public X500Principal getAuthorityName() {
        return authority;
    }

    /**
     * 返回此 {@code CertificateRevokedException} 的无效日期，该日期在证书的无效日期扩展中指定。
     * 无效日期是已知或怀疑私钥被泄露或证书以其他方式失效的日期。此实现调用 {@code getExtensions()}
     * 并检查返回的映射中是否有无效日期扩展 OID ("2.5.29.24") 的条目。如果找到，则返回无效日期；
     * 否则返回 null。每次调用该方法时都会返回一个新的 Date 对象以防止后续修改。
     *
     * @return 无效日期，或如果未指定则返回 {@code null}
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
     * 返回包含关于被撤销证书的附加信息的 X.509 扩展映射，例如无效日期扩展。每个键是一个 OID 字符串，
     * 映射到相应的扩展。
     *
     * @return 不可修改的 X.509 扩展映射，或如果没有扩展则返回空映射
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
     * @serialData 扩展映射的大小（int），后跟映射中的所有扩展，顺序不定。对于映射中的每个扩展，
     * 发出以下数据：OID 字符串（Object），关键性标志（boolean），编码扩展值字节数组的长度（int），
     * 以及编码扩展值字节数组。
     */
    private void writeObject(ObjectOutputStream oos) throws IOException {
        // 写出非瞬态字段
        // (revocationDate, reason, authority)
        oos.defaultWriteObject();

        // 写出扩展映射的大小（映射中的条目数）
        oos.writeInt(extensions.size());

        // 对于映射中的每个扩展，以下数据按顺序发出：
        // OID 字符串（Object），关键性标志（boolean），编码扩展值字节数组的长度（int），
        // 以及编码扩展值字节数组。扩展本身按无特定顺序发出。
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
        // (revocationDate, reason, authority)
        ois.defaultReadObject();

        // 防御性地复制撤销日期
        revocationDate = new Date(revocationDate.getTime());

        // 读取扩展映射的大小（映射中的条目数）并创建扩展映射
        int size = ois.readInt();
        if (size == 0) {
            extensions = Collections.emptyMap();
        } else if (size < 0) {
            throw new IOException("大小不能为负");
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
