
/*
 * Copyright (c) 1997, 2017, Oracle and/or its affiliates. All rights reserved.
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


import java.net.URL;
import java.net.SocketPermission;
import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.*;
import sun.misc.IOUtils;

/**
 *
 * <p>此类扩展了代码库的概念，不仅包括位置（URL），还包括用于验证从该位置来源的已签名代码的证书链。
 *
 * @author Li Gong
 * @author Roland Schemers
 */

public class CodeSource implements java.io.Serializable {

    private static final long serialVersionUID = 4977541819976013951L;

    /**
     * 代码位置。
     *
     * @serial
     */
    private URL location;

    /*
     * 代码签名者。
     */
    private transient CodeSigner[] signers = null;

    /*
     * 代码签名者。证书链被连接。
     */
    private transient java.security.cert.Certificate certs[] = null;

    // 用于匹配位置的缓存 SocketPermission
    private transient SocketPermission sp;

    // 用于生成证书路径
    private transient CertificateFactory factory = null;

    /**
     * 构造 CodeSource 并将其与指定的位置和证书集关联。
     *
     * @param url 位置（URL）。
     *
     * @param certs 证书。它可以为 null。数组的内容会被复制以防止后续修改。
     */
    public CodeSource(URL url, java.security.cert.Certificate certs[]) {
        this.location = url;

        // 复制提供的证书
        if (certs != null) {
            this.certs = certs.clone();
        }
    }

    /**
     * 构造 CodeSource 并将其与指定的位置和代码签名者集关联。
     *
     * @param url 位置（URL）。
     * @param signers 代码签名者。它可以为 null。数组的内容会被复制以防止后续修改。
     *
     * @since 1.5
     */
    public CodeSource(URL url, CodeSigner[] signers) {
        this.location = url;

        // 复制提供的签名者
        if (signers != null) {
            this.signers = signers.clone();
        }
    }

    /**
     * 返回此对象的哈希码值。
     *
     * @return 此对象的哈希码值。
     */
    @Override
    public int hashCode() {
        if (location != null)
            return location.hashCode();
        else
            return 0;
    }

    /**
     * 测试指定对象与该对象是否相等。两个 CodeSource 对象被视为相等，如果它们的位置值相同且签名者证书链值相同。证书链的顺序不必相同。
     *
     * @param obj 要测试与该对象是否相等的对象。
     *
     * @return 如果对象相等返回 true，否则返回 false。
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        // 对象类型必须相同
        if (!(obj instanceof CodeSource))
            return false;

        CodeSource cs = (CodeSource) obj;

        // URL 必须匹配
        if (location == null) {
            // 如果位置为 null，则 cs.location 也必须为 null
            if (cs.location != null) return false;
        } else {
            // 如果位置不为 null，则它必须等于 cs.location
            if (!location.equals(cs.location)) return false;
        }

        // 证书必须匹配
        return matchCerts(cs, true);
    }

    /**
     * 返回与此 CodeSource 关联的位置。
     *
     * @return 位置（URL）。
     */
    public final URL getLocation() {
        /* 由于 URL 实际上是不可变的，返回其本身不会造成安全问题 */
        return this.location;
    }

    /**
     * 返回与此 CodeSource 关联的证书。
     * <p>
     * 如果此 CodeSource 对象是使用
     * {@link #CodeSource(URL url, CodeSigner[] signers)}
     * 构造函数创建的，则其证书链被提取并用于创建一个 Certificate 对象数组。每个签名者的证书后面是其支持的证书链（可能为空）。
     * 每个签名者的证书及其支持的证书链按自底向上的顺序排列（即，签名者的证书在前，（根）证书机构在后）。
     *
     * @return 证书数组的副本，如果没有证书则返回 null。
     */
    public final java.security.cert.Certificate[] getCertificates() {
        if (certs != null) {
            return certs.clone();

        } else if (signers != null) {
            // 将代码签名者转换为证书
            ArrayList<java.security.cert.Certificate> certChains =
                        new ArrayList<>();
            for (int i = 0; i < signers.length; i++) {
                certChains.addAll(
                    signers[i].getSignerCertPath().getCertificates());
            }
            certs = certChains.toArray(
                        new java.security.cert.Certificate[certChains.size()]);
            return certs.clone();

        } else {
            return null;
        }
    }

    /**
     * 返回与此 CodeSource 关联的代码签名者。
     * <p>
     * 如果此 CodeSource 对象是使用
     * {@link #CodeSource(URL url, java.security.cert.Certificate[] certs)}
     * 构造函数创建的，则其证书链被提取并用于创建一个 CodeSigner 对象数组。注意，只有 X.509 证书会被检查，所有其他类型的证书将被忽略。
     *
     * @return 代码签名者数组的副本，如果没有代码签名者则返回 null。
     *
     * @since 1.5
     */
    public final CodeSigner[] getCodeSigners() {
        if (signers != null) {
            return signers.clone();

        } else if (certs != null) {
            // 将证书转换为代码签名者
            signers = convertCertArrayToSignerArray(certs);
            return signers.clone();

        } else {
            return null;
        }
    }

    /**
     * 如果此 CodeSource 对象“隐含”指定的 CodeSource，则返回 true。
     * <p>
     * 更具体地说，此方法执行以下检查。如果任何检查失败，则返回 false。如果所有检查都成功，则返回 true。
     * <ul>
     * <li> <i>codesource</i> 不能为空。
     * <li> 如果此对象的证书不为空，则此对象的所有证书必须存在于 <i>codesource</i> 的证书中。
     * <li> 如果此对象的位置（getLocation()）不为空，则对以下内容进行检查：
     *   <ul>
     *     <li>  <i>codesource</i> 的位置不能为空。
     *
     *     <li>  如果此对象的位置
     *           等于 <i>codesource</i> 的位置，则返回 true。
     *
     *     <li>  此对象的协议（getLocation().getProtocol()）必须等于 <i>codesource</i> 的协议，忽略大小写。
     *
     *     <li>  如果此对象的主机（getLocation().getHost()）不为空，
     *           则使用此对象的主机构造的 SocketPermission
     *           必须隐含使用 <i>codesource</i> 的主机构造的 SocketPermission。
     *
     *     <li>  如果此对象的端口（getLocation().getPort()）不等于 -1（即指定了端口），则它必须等于
     *           <i>codesource</i> 的端口或默认端口
     *           （codesource.getLocation().getDefaultPort()）。
     *
     *     <li>  如果此对象的文件（getLocation().getFile()）不等于
     *           <i>codesource</i> 的文件，则进行以下检查：
     *           如果此对象的文件以 "/-" 结尾，
     *           则 <i>codesource</i> 的文件必须以此对象的文件（不包括尾随的 "-"）开头。
     *           如果此对象的文件以 "/*" 结尾，
     *           则 <i>codesource</i> 的文件必须以此对象的文件开头且不能有进一步的 "/" 分隔符。
     *           如果此对象的文件不以 "/" 结尾，
     *           则 <i>codesource</i> 的文件必须与此对象的文件加上一个 "/" 匹配。
     *
     *     <li>  如果此对象的引用（getLocation().getRef()）不为空，它必须等于 <i>codesource</i> 的引用。
     *
     *   </ul>
     * </ul>
     * <p>
     * 例如，具有以下位置和空证书的 codesource 对象都隐含
     * 位置为 "http://java.sun.com/classes/foo.jar" 且证书为空的 codesource：
     * <pre>
     *     http:
     *     http://*.sun.com/classes/*
     *     http://java.sun.com/classes/-
     *     http://java.sun.com/classes/foo.jar
     * </pre>
     *
     * 注意，如果此 CodeSource 有空位置和空证书链，则它隐含所有其他 CodeSource。
     *
     * @param codesource 要比较的 CodeSource。
     *
     * @return 如果指定的 codesource 被此 codesource 隐含，则返回 true，否则返回 false。
     */

    public boolean implies(CodeSource codesource)
    {
        if (codesource == null)
            return false;

        return matchCerts(codesource, false) && matchLocation(codesource);
    }

    /**
     * 如果此 CodeSource 中的所有证书也存在于 <i>that</i> 中，则返回 true。
     *
     * @param that 要检查的 CodeSource。
     * @param strict 如果为 true，则执行严格的相等匹配。否则执行子集匹配。
     */
    private boolean matchCerts(CodeSource that, boolean strict)
    {
        boolean match;

        // 匹配任何键
        if (certs == null && signers == null) {
            if (strict) {
                return (that.certs == null && that.signers == null);
            } else {
                return true;
            }
        // 两者都有签名者
        } else if (signers != null && that.signers != null) {
            if (strict && signers.length != that.signers.length) {
                return false;
            }
            for (int i = 0; i < signers.length; i++) {
                match = false;
                for (int j = 0; j < that.signers.length; j++) {
                    if (signers[i].equals(that.signers[j])) {
                        match = true;
                        break;
                    }
                }
                if (!match) return false;
            }
            return true;

        // 两者都有证书
        } else if (certs != null && that.certs != null) {
            if (strict && certs.length != that.certs.length) {
                return false;
            }
            for (int i = 0; i < certs.length; i++) {
                match = false;
                for (int j = 0; j < that.certs.length; j++) {
                    if (certs[i].equals(that.certs[j])) {
                        match = true;
                        break;
                    }
                }
                if (!match) return false;
            }
            return true;
        }

        return false;
    }


    /**
     * 如果两个 CodeSource 有“相同”的位置，则返回 true。
     *
     * @param that 要比较的 CodeSource
     */
    private boolean matchLocation(CodeSource that) {
        if (location == null)
            return true;

        if ((that == null) || (that.location == null))
            return false;

        if (location.equals(that.location))
            return true;

        if (!location.getProtocol().equalsIgnoreCase(that.location.getProtocol()))
            return false;

        int thisPort = location.getPort();
        if (thisPort != -1) {
            int thatPort = that.location.getPort();
            int port = thatPort != -1 ? thatPort
                                      : that.location.getDefaultPort();
            if (thisPort != port)
                return false;
        }

        if (location.getFile().endsWith("/-")) {
            // 匹配目录及其（递归地）包含的所有文件和子目录。
            // 例如，"/a/b/-" 意味着以 "/a/b/" 开头的任何内容
            String thisPath = location.getFile().substring(0,
                                            location.getFile().length()-1);
            if (!that.location.getFile().startsWith(thisPath))
                return false;
        } else if (location.getFile().endsWith("/*")) {
            // 匹配目录及其包含的所有文件。
            // 例如，"/a/b/*" 意味着以 "/a/b/" 开头但没有进一步斜杠的内容
            int last = that.location.getFile().lastIndexOf('/');
            if (last == -1)
                return false;
            String thisPath = location.getFile().substring(0,
                                            location.getFile().length()-1);
            String thatPath = that.location.getFile().substring(0, last+1);
            if (!thatPath.equals(thisPath))
                return false;
        } else {
            // 仅精确匹配。
            // 例如，"/a/b" 和 "/a/b/" 都隐含 "/a/b/"
            if ((!that.location.getFile().equals(location.getFile()))
                && (!that.location.getFile().equals(location.getFile()+"/"))) {
                return false;
            }
        }

        if (location.getRef() != null
            && !location.getRef().equals(that.location.getRef())) {
            return false;
        }

        String thisHost = location.getHost();
        String thatHost = that.location.getHost();
        if (thisHost != null) {
            if (("".equals(thisHost) || "localhost".equals(thisHost)) &&
                ("".equals(thatHost) || "localhost".equals(thatHost))) {
                // ok
            } else if (!thisHost.equals(thatHost)) {
                if (thatHost == null) {
                    return false;
                }
                if (this.sp == null) {
                    this.sp = new SocketPermission(thisHost, "resolve");
                }
                if (that.sp == null) {
                    that.sp = new SocketPermission(thatHost, "resolve");
                }
                if (!this.sp.implies(that.sp)) {
                    return false;
                }
            }
        }
        // 一切都匹配
        return true;
    }


                /**
     * 返回描述此 CodeSource 的字符串，包括其 URL 和证书。
     *
     * @return 有关此 CodeSource 的信息。
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(this.location);

        if (this.certs != null && this.certs.length > 0) {
            for (int i = 0; i < this.certs.length; i++) {
                sb.append( " " + this.certs[i]);
            }

        } else if (this.signers != null && this.signers.length > 0) {
            for (int i = 0; i < this.signers.length; i++) {
                sb.append( " " + this.signers[i]);
            }
        } else {
            sb.append(" <no signer certificates>");
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * 将此对象写入流（即序列化它）。
     *
     * @serialData 初始的 {@code URL} 后跟一个表示要跟随的证书数量的
     * {@code int}（值为“零”表示此对象没有关联的证书）。
     * 每个证书从一个表示证书类型的 {@code String} 开始，后跟一个
     * 指定证书编码长度的 {@code int}，然后是证书编码本身，以字节数组的形式写入。
     * 最后，如果有代码签名者，则将代码签名者数组序列化并写入。
     */
    private void writeObject(java.io.ObjectOutputStream oos)
        throws IOException
    {
        oos.defaultWriteObject(); // location

        // 序列化证书数组
        if (certs == null || certs.length == 0) {
            oos.writeInt(0);
        } else {
            // 写出证书的总数
            oos.writeInt(certs.length);
            // 写出每个证书，包括其类型
            for (int i = 0; i < certs.length; i++) {
                java.security.cert.Certificate cert = certs[i];
                try {
                    oos.writeUTF(cert.getType());
                    byte[] encoded = cert.getEncoded();
                    oos.writeInt(encoded.length);
                    oos.write(encoded);
                } catch (CertificateEncodingException cee) {
                    throw new IOException(cee.getMessage());
                }
            }
        }

        // 序列化代码签名者数组（如果有）
        if (signers != null && signers.length > 0) {
            oos.writeObject(signers);
        }
    }

    /**
     * 从流中恢复此对象（即反序列化它）。
     */
    private void readObject(java.io.ObjectInputStream ois)
        throws IOException, ClassNotFoundException
    {
        CertificateFactory cf;
        Hashtable<String, CertificateFactory> cfs = null;
        List<java.security.cert.Certificate> certList = null;

        ois.defaultReadObject(); // location

        // 处理流中的新样式证书（如果有）
        int size = ois.readInt();
        if (size > 0) {
            // 我们知道有 3 种不同的证书类型：X.509, PGP, SDSI，它们可以同时存在于流中
            cfs = new Hashtable<String, CertificateFactory>(3);
            certList = new ArrayList<>(size > 20 ? 20 : size);
        } else if (size < 0) {
            throw new IOException("size cannot be negative");
        }

        for (int i = 0; i < size; i++) {
            // 读取证书类型，并实例化该类型的证书工厂（如果可能，重用现有工厂）
            String certType = ois.readUTF();
            if (cfs.containsKey(certType)) {
                // 重用证书工厂
                cf = cfs.get(certType);
            } else {
                // 创建新的证书工厂
                try {
                    cf = CertificateFactory.getInstance(certType);
                } catch (CertificateException ce) {
                    throw new ClassNotFoundException
                        ("Certificate factory for " + certType + " not found");
                }
                // 存储证书工厂以便稍后重用
                cfs.put(certType, cf);
            }
            // 解析证书
            byte[] encoded = IOUtils.readExactlyNBytes(ois, ois.readInt());
            ByteArrayInputStream bais = new ByteArrayInputStream(encoded);
            try {
                certList.add(cf.generateCertificate(bais));
            } catch (CertificateException ce) {
                throw new IOException(ce.getMessage());
            }
            bais.close();
        }

        if (certList != null) {
            this.certs = certList.toArray(
                    new java.security.cert.Certificate[size]);
        }
        // 反序列化代码签名者数组（如果有）
        try {
            this.signers = ((CodeSigner[])ois.readObject()).clone();
        } catch (IOException ioe) {
            // 没有签名者
        }
    }

    /*
     * 将证书数组转换为代码签名者数组。
     * 证书数组是证书链的连接，每个链的初始证书是实体证书。
     *
     * @return 代码签名者数组或如果没有生成则返回 null。
     */
    private CodeSigner[] convertCertArrayToSignerArray(
        java.security.cert.Certificate[] certs) {

        if (certs == null) {
            return null;
        }

        try {
            // 初始化证书工厂
            if (factory == null) {
                factory = CertificateFactory.getInstance("X.509");
            }

            // 遍历所有证书
            int i = 0;
            List<CodeSigner> signers = new ArrayList<>();
            while (i < certs.length) {
                List<java.security.cert.Certificate> certChain =
                        new ArrayList<>();
                certChain.add(certs[i++]); // 第一个证书是实体证书
                int j = i;

                // 提取证书链
                // （当证书不是实体证书时循环）
                while (j < certs.length &&
                    certs[j] instanceof X509Certificate &&
                    ((X509Certificate)certs[j]).getBasicConstraints() != -1) {
                    certChain.add(certs[j]);
                    j++;
                }
                i = j;
                CertPath certPath = factory.generateCertPath(certChain);
                signers.add(new CodeSigner(certPath, null));
            }

            if (signers.isEmpty()) {
                return null;
            } else {
                return signers.toArray(new CodeSigner[signers.size()]);
            }

        } catch (CertificateException e) {
            return null; //TODO - 可能最好在这里抛出异常
        }
    }
}
