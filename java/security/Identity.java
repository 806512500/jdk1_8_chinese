
/*
 * Copyright (c) 1996, 2015, Oracle and/or its affiliates. All rights reserved.
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

package java.security;

import java.io.Serializable;
import java.util.*;

/**
 * <p>此类表示身份：现实世界中的对象，如人、公司或组织，其身份可以通过公钥进行验证。身份也可以是更抽象（或具体）的构造，例如守护线程或智能卡。
 *
 * <p>所有 Identity 对象都有一个名称和一个公钥。名称是不可变的。身份也可以是限定的。也就是说，如果指定了身份具有特定的范围，则该身份的名称和公钥在该范围内是唯一的。
 *
 * <p>身份还有一组证书（所有证书都认证其自身的公钥）。这些证书中指定的主体名称不必相同，只需公钥相同即可。
 *
 * <p>身份可以被子类化，以包含邮政地址、电子邮件地址、电话号码、人脸和徽标图像等信息。
 *
 * @see IdentityScope
 * @see Signer
 * @see Principal
 *
 * @author Benjamin Renaud
 * @deprecated 此类不再使用。其功能已被 {@code java.security.KeyStore}、
 * {@code java.security.cert} 包和 {@code java.security.Principal} 所取代。
 */
@Deprecated
public abstract class Identity implements Principal, Serializable {

    /** 为了互操作性，使用 JDK 1.1.x 的 serialVersionUID */
    private static final long serialVersionUID = 3609922007826600659L;

    /**
     * 此身份的名称。
     *
     * @serial
     */
    private String name;

    /**
     * 此身份的公钥。
     *
     * @serial
     */
    private PublicKey publicKey;

    /**
     * 关于此身份的通用描述信息。
     *
     * @serial
     */
    String info = "没有更多可用信息。";

    /**
     * 此身份的范围。
     *
     * @serial
     */
    IdentityScope scope;

    /**
     * 此身份的证书。
     *
     * @serial
     */
    Vector<Certificate> certificates;

    /**
     * 仅用于序列化的构造函数。
     */
    protected Identity() {
        this("正在恢复...");
    }

    /**
     * 使用指定的名称和范围构造一个身份。
     *
     * @param name 身份的名称。
     * @param scope 身份的范围。
     *
     * @exception KeyManagementException 如果在该范围内已经存在同名的身份。
     */
    public Identity(String name, IdentityScope scope) throws
    KeyManagementException {
        this(name);
        if (scope != null) {
            scope.addIdentity(this);
        }
        this.scope = scope;
    }

    /**
     * 使用指定的名称和无范围构造一个身份。
     *
     * @param name 身份的名称。
     */
    public Identity(String name) {
        this.name = name;
    }

    /**
     * 返回此身份的名称。
     *
     * @return 此身份的名称。
     */
    public final String getName() {
        return name;
    }

    /**
     * 返回此身份的范围。
     *
     * @return 此身份的范围。
     */
    public final IdentityScope getScope() {
        return scope;
    }

    /**
     * 返回此身份的公钥。
     *
     * @return 此身份的公钥。
     *
     * @see #setPublicKey
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * 设置此身份的公钥。此操作会移除旧的公钥和此身份的所有证书。
     *
     * <p>首先，如果存在安全管理器，其 {@code checkSecurityAccess}
     * 方法将被调用，参数为 {@code "setIdentityPublicKey"}
     * 以检查是否允许设置公钥。
     *
     * @param key 此身份的公钥。
     *
     * @exception KeyManagementException 如果在该范围内的另一个身份具有相同的公钥，或发生其他异常。
     *
     * @exception  SecurityException  如果存在安全管理器且其
     * {@code checkSecurityAccess} 方法不允许设置公钥。
     *
     * @see #getPublicKey
     * @see SecurityManager#checkSecurityAccess
     */
    /* 如果已经设置，是否应该抛出异常？ */
    public void setPublicKey(PublicKey key) throws KeyManagementException {

        check("setIdentityPublicKey");
        this.publicKey = key;
        certificates = new Vector<Certificate>();
    }

    /**
     * 为该身份指定一个通用信息字符串。
     *
     * <p>首先，如果存在安全管理器，其 {@code checkSecurityAccess}
     * 方法将被调用，参数为 {@code "setIdentityInfo"}
     * 以检查是否允许指定信息字符串。
     *
     * @param info 信息字符串。
     *
     * @exception  SecurityException  如果存在安全管理器且其
     * {@code checkSecurityAccess} 方法不允许设置信息字符串。
     *
     * @see #getInfo
     * @see SecurityManager#checkSecurityAccess
     */
    public void setInfo(String info) {
        check("setIdentityInfo");
        this.info = info;
    }

    /**
     * 返回之前为此身份指定的通用信息。
     *
     * @return 关于此身份的通用信息。
     *
     * @see #setInfo
     */
    public String getInfo() {
        return info;
    }

    /**
     * 为此身份添加一个证书。如果该身份已有公钥，则证书中的公钥必须相同；如果该身份没有公钥，则该身份的公钥将被设置为证书中指定的公钥。
     *
     * <p>首先，如果存在安全管理器，其 {@code checkSecurityAccess}
     * 方法将被调用，参数为 {@code "addIdentityCertificate"}
     * 以检查是否允许添加证书。
     *
     * @param certificate 要添加的证书。
     *
     * @exception KeyManagementException 如果证书无效，或证书中添加的公钥与该身份的公钥冲突，或发生其他异常。
     *
     * @exception  SecurityException  如果存在安全管理器且其
     * {@code checkSecurityAccess} 方法不允许添加证书。
     *
     * @see SecurityManager#checkSecurityAccess
     */
    public void addCertificate(Certificate certificate)
    throws KeyManagementException {

        check("addIdentityCertificate");

        if (certificates == null) {
            certificates = new Vector<Certificate>();
        }
        if (publicKey != null) {
            if (!keyEquals(publicKey, certificate.getPublicKey())) {
                throw new KeyManagementException(
                    "公钥与证书公钥不同");
            }
        } else {
            publicKey = certificate.getPublicKey();
        }
        certificates.addElement(certificate);
    }

    private boolean keyEquals(PublicKey aKey, PublicKey anotherKey) {
        String aKeyFormat = aKey.getFormat();
        String anotherKeyFormat = anotherKey.getFormat();
        if ((aKeyFormat == null) ^ (anotherKeyFormat == null))
            return false;
        if (aKeyFormat != null && anotherKeyFormat != null)
            if (!aKeyFormat.equalsIgnoreCase(anotherKeyFormat))
                return false;
        return java.util.Arrays.equals(aKey.getEncoded(),
                                     anotherKey.getEncoded());
    }


    /**
     * 从此身份中移除一个证书。
     *
     * <p>首先，如果存在安全管理器，其 {@code checkSecurityAccess}
     * 方法将被调用，参数为 {@code "removeIdentityCertificate"}
     * 以检查是否允许移除证书。
     *
     * @param certificate 要移除的证书。
     *
     * @exception KeyManagementException 如果证书缺失，或发生其他异常。
     *
     * @exception  SecurityException  如果存在安全管理器且其
     * {@code checkSecurityAccess} 方法不允许移除证书。
     *
     * @see SecurityManager#checkSecurityAccess
     */
    public void removeCertificate(Certificate certificate)
    throws KeyManagementException {
        check("removeIdentityCertificate");
        if (certificates != null) {
            certificates.removeElement(certificate);
        }
    }

    /**
     * 返回此身份的所有证书的副本。
     *
     * @return 此身份的所有证书的副本。
     */
    public Certificate[] certificates() {
        if (certificates == null) {
            return new Certificate[0];
        }
        int len = certificates.size();
        Certificate[] certs = new Certificate[len];
        certificates.copyInto(certs);
        return certs;
    }

    /**
     * 测试指定对象与该身份是否相等。首先测试这两个实体是否实际引用同一个对象，如果是，则返回 true。接下来，检查这两个实体是否具有相同的名称和相同的范围。如果它们相同，则方法返回 true。否则，调用
     * {@link #identityEquals(Identity) identityEquals}，子类应覆盖该方法。
     *
     * @param identity 要测试与该身份相等的对象。
     *
     * @return 如果对象被认为相等，则返回 true，否则返回 false。
     *
     * @see #identityEquals
     */
    public final boolean equals(Object identity) {

        if (identity == this) {
            return true;
        }

        if (identity instanceof Identity) {
            Identity i = (Identity)identity;
            if (this.fullName().equals(i.fullName())) {
                return true;
            } else {
                return identityEquals(i);
            }
        }
        return false;
    }

    /**
     * 测试指定身份与该身份是否相等。子类应覆盖此方法以测试相等性。默认行为是如果名称和公钥相同，则返回 true。
     *
     * @param identity 要测试与该身份相等的身份。
     *
     * @return 如果身份被认为相等，则返回 true，否则返回 false。
     *
     * @see #equals
     */
    protected boolean identityEquals(Identity identity) {
        if (!name.equalsIgnoreCase(identity.name))
            return false;

        if ((publicKey == null) ^ (identity.publicKey == null))
            return false;

        if (publicKey != null && identity.publicKey != null)
            if (!publicKey.equals(identity.publicKey))
                return false;

        return true;

    }

    /**
     * 返回身份的可解析名称：identityName.scopeName
     */
    String fullName() {
        String parsable = name;
        if (scope != null) {
            parsable += "." + scope.getName();
        }
        return parsable;
    }

    /**
     * 返回描述此身份的简短字符串，包括其名称和范围（如果有）。
     *
     * <p>首先，如果存在安全管理器，其 {@code checkSecurityAccess}
     * 方法将被调用，参数为 {@code "printIdentity"}
     * 以检查是否允许返回字符串。
     *
     * @return 有关此身份的信息，例如其名称和范围名称（如果有）。
     *
     * @exception  SecurityException  如果存在安全管理器且其
     * {@code checkSecurityAccess} 方法不允许返回描述此身份的字符串。
     *
     * @see SecurityManager#checkSecurityAccess
     */
    public String toString() {
        check("printIdentity");
        String printable = name;
        if (scope != null) {
            printable += "[" + scope.getName() + "]";
        }
        return printable;
    }

    /**
     * 返回此身份的字符串表示形式，可选地提供比 {@code toString} 方法无参数时更多的详细信息。
     *
     * <p>首先，如果存在安全管理器，其 {@code checkSecurityAccess}
     * 方法将被调用，参数为 {@code "printIdentity"}
     * 以检查是否允许返回字符串。
     *
     * @param detailed 是否提供详细信息。
     *
     * @return 有关此身份的信息。如果 {@code detailed}
     * 为 true，则此方法返回比 {@code toString} 方法无参数时更多的信息。
     *
     * @exception  SecurityException  如果存在安全管理器且其
     * {@code checkSecurityAccess} 方法不允许返回描述此身份的字符串。
     *
     * @see #toString
     * @see SecurityManager#checkSecurityAccess
     */
    public String toString(boolean detailed) {
        String out = toString();
        if (detailed) {
            out += "\n";
            out += printKeys();
            out += "\n" + printCertificates();
            if (info != null) {
                out += "\n\t" + info;
            } else {
                out += "\n\tno additional information available.";
            }
        }
        return out;
    }

    String printKeys() {
        String key = "";
        if (publicKey != null) {
            key = "\tpublic key initialized";
        } else {
            key = "\tno public key";
        }
        return key;
    }

    String printCertificates() {
        String out = "";
        if (certificates == null) {
            return "\tno certificates";
        } else {
            out += "\tcertificates: \n";

            int i = 1;
            for (Certificate cert : certificates) {
                out += "\tcertificate " + i++ +
                    "\tfor  : " + cert.getPrincipal() + "\n";
                out += "\t\t\tfrom : " +
                    cert.getGuarantor() + "\n";
            }
        }
        return out;
    }


                /**
     * 返回此身份的哈希码。
     *
     * @return 此身份的哈希码。
     */
    public int hashCode() {
        return name.hashCode();
    }

    private static void check(String directive) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSecurityAccess(directive);
        }
    }
}
