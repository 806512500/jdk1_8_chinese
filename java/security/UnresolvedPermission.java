
/*
 * Copyright (c) 1997, 2022, Oracle and/or its affiliates. All rights reserved.
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

import sun.misc.IOUtils;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Hashtable;
import java.lang.reflect.*;
import java.security.cert.*;
import java.util.List;

/**
 * UnresolvedPermission 类用于保存在初始化 Policy 时“未解决”的权限。
 * 未解决的权限是指在 Policy 初始化时其实际的 Permission 类尚未存在的权限（见下文）。
 *
 * <p>Java 运行时的策略（指定从各种主体获取代码的权限）由 Policy 对象表示。
 * 每当 Policy 被初始化或刷新时，都会为所有由 Policy 允许的权限创建适当类的 Permission 对象。
 *
 * <p>许多由策略配置引用的权限类类型是本地存在的（即，可以在 CLASSPATH 上找到的）。
 * 这样的权限对象可以在 Policy 初始化期间实例化。例如，总是可以实例化 java.io.FilePermission，
 * 因为 FilePermission 类可以在 CLASSPATH 上找到。
 *
 * <p>其他权限类可能在 Policy 初始化时还不存在。例如，引用的权限类可能在稍后加载的 JAR 文件中。
 * 对于每个这样的类，都会实例化一个 UnresolvedPermission。因此，UnresolvedPermission 实质上是一个“占位符”，
 * 包含有关权限的信息。
 *
 * <p>稍后，当代码调用 AccessController.checkPermission 时，如果权限类型之前未解决但其类已加载，
 * 则会“解决”之前未解决的权限。也就是说，对于每个这样的 UnresolvedPermission，都会根据 UnresolvedPermission 中的信息
 * 实例化一个适当类类型的新对象。
 *
 * <p>为了实例化新类，UnresolvedPermission 假设该类提供了一个零参数、一个参数和/或两个参数的构造函数。
 * 零参数构造函数用于实例化没有名称和没有操作的权限。
 * 一个参数的构造函数假定接受一个 {@code String} 名称作为输入，两个参数的构造函数假定接受一个
 * {@code String} 名称和 {@code String} 操作作为输入。UnresolvedPermission 可能会调用一个
 * 带有 {@code null} 名称和/或操作的构造函数。如果适当的权限构造函数不可用，
 * 则忽略 UnresolvedPermission，相关的权限将不会授予执行代码。
 *
 * <p>新创建的权限对象将替换 UnresolvedPermission，后者将被移除。
 *
 * <p>注意，{@code UnresolvedPermission} 的 {@code getName} 方法返回
 * 未解决的底层权限的 {@code type}（类名）。
 *
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.PermissionCollection
 * @see java.security.Policy
 *
 *
 * @author Roland Schemers
 */

public final class UnresolvedPermission extends Permission
implements java.io.Serializable
{

    private static final long serialVersionUID = -4821973115467008846L;

    private static final sun.security.util.Debug debug =
        sun.security.util.Debug.getInstance
        ("policy,access", "UnresolvedPermission");

    /**
     * 当此未解决的权限被解决时，将要创建的 Permission 类的类名。
     *
     * @serial
     */
    private String type;

    /**
     * 权限名称。
     *
     * @serial
     */
    private String name;

    /**
     * 权限的操作。
     *
     * @serial
     */
    private String actions;

    private transient java.security.cert.Certificate certs[];

    /**
     * 创建一个新的 UnresolvedPermission，包含稍后实际创建指定类的 Permission 所需的权限信息，
     * 当权限被解决时。
     *
     * @param type 当此未解决的权限被解决时，将要创建的 Permission 类的类名。
     * @param name 权限的名称。
     * @param actions 权限的操作。
     * @param certs 权限类的签名证书。这是一个证书链的列表，每个链由一个签名者证书及其支持的证书链组成。
     * 每个链按自底向上的顺序排列（即，签名者证书在前，（根）证书机构在后）。签名者证书从数组中复制。
     * 对数组的后续更改不会影响此 UnresolvedPermission。
     */
    public UnresolvedPermission(String type,
                                String name,
                                String actions,
                                java.security.cert.Certificate certs[])
    {
        super(type);

        if (type == null)
                throw new NullPointerException("type can't be null");

        // 执行防御性复制并重新分配 certs，如果我们有一个非 null 引用
        if (certs != null) {
            certs = certs.clone();
        }

        this.type = type;
        this.name = name;
        this.actions = actions;

        if (certs != null) {
            // 从证书列表中提取签名者证书。
            for (int i = 0; i < certs.length; i++) {
                if (!(certs[i] instanceof X509Certificate)) {
                    // 没有签名者证书的概念，所以我们存储整个证书数组。不需要进一步处理。
                    this.certs = certs;
                    return;
                }
            }

            // 遍历证书列表，查看所有证书是否都是签名者证书。
            int i = 0;
            int count = 0;
            while (i < certs.length) {
                count++;
                while (((i + 1) < certs.length) &&
                       ((X509Certificate)certs[i]).getIssuerDN().equals(
                           ((X509Certificate)certs[i + 1]).getSubjectDN())) {
                    i++;
                }
                i++;
            }
            if (count == certs.length) {
                // 所有证书都是签名者证书，所以我们存储整个数组。不需要进一步处理。
                this.certs = certs;
                return;
            }

            // 提取签名者证书
            ArrayList<java.security.cert.Certificate> signerCerts =
                new ArrayList<>();
            i = 0;
            while (i < certs.length) {
                signerCerts.add(certs[i]);
                while (((i + 1) < certs.length) &&
                    ((X509Certificate)certs[i]).getIssuerDN().equals(
                      ((X509Certificate)certs[i + 1]).getSubjectDN())) {
                    i++;
                }
                i++;
            }
            this.certs =
                new java.security.cert.Certificate[signerCerts.size()];
            signerCerts.toArray(this.certs);
        }
    }


    private static final Class[] PARAMS0 = { };
    private static final Class[] PARAMS1 = { String.class };
    private static final Class[] PARAMS2 = { String.class, String.class };

    /**
     * 尝试使用传递的权限的类加载器来解决此权限。
     */
    Permission resolve(Permission p, java.security.cert.Certificate certs[]) {
        if (this.certs != null) {
            // 如果 p 没有签名，我们没有匹配
            if (certs == null) {
                return null;
            }

            // this.certs 中的所有证书都必须在 certs 中存在
            boolean match;
            for (int i = 0; i < this.certs.length; i++) {
                match = false;
                for (int j = 0; j < certs.length; j++) {
                    if (this.certs[i].equals(certs[j])) {
                        match = true;
                        break;
                    }
                }
                if (!match) return null;
            }
        }
        try {
            Class<?> pc = p.getClass();

            if (name == null && actions == null) {
                try {
                    Constructor<?> c = pc.getConstructor(PARAMS0);
                    return (Permission)c.newInstance(new Object[] {});
                } catch (NoSuchMethodException ne) {
                    try {
                        Constructor<?> c = pc.getConstructor(PARAMS1);
                        return (Permission) c.newInstance(
                              new Object[] { name});
                    } catch (NoSuchMethodException ne1) {
                        Constructor<?> c = pc.getConstructor(PARAMS2);
                        return (Permission) c.newInstance(
                              new Object[] { name, actions });
                    }
                }
            } else {
                if (name != null && actions == null) {
                    try {
                        Constructor<?> c = pc.getConstructor(PARAMS1);
                        return (Permission) c.newInstance(
                              new Object[] { name});
                    } catch (NoSuchMethodException ne) {
                        Constructor<?> c = pc.getConstructor(PARAMS2);
                        return (Permission) c.newInstance(
                              new Object[] { name, actions });
                    }
                } else {
                    Constructor<?> c = pc.getConstructor(PARAMS2);
                    return (Permission) c.newInstance(
                          new Object[] { name, actions });
                }
            }
        } catch (NoSuchMethodException nsme) {
            if (debug != null ) {
                debug.println("NoSuchMethodException:\n  could not find " +
                        "proper constructor for " + type);
                nsme.printStackTrace();
            }
            return null;
        } catch (Exception e) {
            if (debug != null ) {
                debug.println("unable to instantiate " + name);
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * 未解决的权限总是返回 false。也就是说，UnresolvedPermission 从不被认为
     * 暗示另一个权限。
     *
     * @param p 要检查的权限。
     *
     * @return false。
     */
    @Override
    public boolean implies(Permission p) {
        return false;
    }

    /**
     * 检查两个 UnresolvedPermission 对象是否相等。
     * 检查 <i>obj</i> 是否是 UnresolvedPermission，并且具有与该对象相同的类型（类）名称、权限名称、操作和证书。
     *
     * <p>为了确定证书相等性，此方法仅比较实际的签名者证书。支持的证书链
     * 不被此方法考虑。
     *
     * @param obj 要测试与该对象相等的对象。
     *
     * @return 如果 obj 是 UnresolvedPermission，并且具有与该对象相同的
     * 类型（类）名称、权限名称、操作和证书，则返回 true。
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (! (obj instanceof UnresolvedPermission))
            return false;
        UnresolvedPermission that = (UnresolvedPermission) obj;

        // 检查类型
        if (!this.type.equals(that.type)) {
            return false;
        }

        // 检查名称
        if (this.name == null) {
            if (that.name != null) {
                return false;
            }
        } else if (!this.name.equals(that.name)) {
            return false;
        }

        // 检查操作
        if (this.actions == null) {
            if (that.actions != null) {
                return false;
            }
        } else {
            if (!this.actions.equals(that.actions)) {
                return false;
            }
        }

        // 检查证书
        if ((this.certs == null && that.certs != null) ||
            (this.certs != null && that.certs == null) ||
            (this.certs != null && that.certs != null &&
                this.certs.length != that.certs.length)) {
            return false;
        }

        int i,j;
        boolean match;

        for (i = 0; this.certs != null && i < this.certs.length; i++) {
            match = false;
            for (j = 0; j < that.certs.length; j++) {
                if (this.certs[i].equals(that.certs[j])) {
                    match = true;
                    break;
                }
            }
            if (!match) return false;
        }

        for (i = 0; that.certs != null && i < that.certs.length; i++) {
            match = false;
            for (j = 0; j < this.certs.length; j++) {
                if (that.certs[i].equals(this.certs[j])) {
                    match = true;
                    break;
                }
            }
            if (!match) return false;
        }
        return true;
    }

    /**
     * 返回该对象的哈希码值。
     *
     * @return 该对象的哈希码值。
     */
    @Override
    public int hashCode() {
        int hash = type.hashCode();
        if (name != null)
            hash ^= name.hashCode();
        if (actions != null)
            hash ^= actions.hashCode();
        return hash;
    }

    /**
     * 返回操作的规范字符串表示形式，当前为空字符串 ""，因为 UnresolvedPermission 没有操作。
     * 即，当此 UnresolvedPermission 被解决时，将要创建的权限的操作可能是非 null，
     * 但 UnresolvedPermission 本身从不被认为有任何操作。
     *
     * @return 空字符串 ""。
     */
    @Override
    public String getActions()
    {
        return "";
    }


                /**
     * 获取尚未解析的基础权限的类型（类名）。
     *
     * @return 尚未解析的基础权限的类型（类名）。
     *
     * @since 1.5
     */
    public String getUnresolvedType() {
        return type;
    }

    /**
     * 获取尚未解析的基础权限的目标名称。
     *
     * @return 尚未解析的基础权限的目标名称，或 {@code null}，
     *          如果没有目标名称。
     *
     * @since 1.5
     */
    public String getUnresolvedName() {
        return name;
    }

    /**
     * 获取尚未解析的基础权限的动作。
     *
     * @return 尚未解析的基础权限的动作，或 {@code null}
     *          如果没有动作。
     *
     * @since 1.5
     */
    public String getUnresolvedActions() {
        return actions;
    }

    /**
     * 获取尚未解析的基础权限的签名证书（不包括任何支持链）。
     *
     * @return 尚未解析的基础权限的签名证书，或 null，如果没有任何签名证书。
     * 每次调用此方法时返回一个新的数组。
     *
     * @since 1.5
     */
    public java.security.cert.Certificate[] getUnresolvedCerts() {
        return (certs == null) ? null : certs.clone();
    }

    /**
     * 返回描述此 UnresolvedPermission 的字符串。约定是指定类名、权限名称和动作，格式如下：
     * '(unresolved "ClassName" "name" "actions")'。
     *
     * @return 关于此 UnresolvedPermission 的信息。
     */
    @Override
    public String toString() {
        return "(unresolved " + type + " " + name + " " + actions + ")";
    }

    /**
     * 返回一个用于存储 UnresolvedPermission 对象的新 PermissionCollection 对象。
     * <p>
     * @return 一个适合存储 UnresolvedPermissions 的新 PermissionCollection 对象。
     */
    @Override
    public PermissionCollection newPermissionCollection() {
        return new UnresolvedPermissionCollection();
    }

    /**
     * 将此对象写入流（即序列化它）。
     *
     * @serialData 一个初始的 {@code String} 表示 {@code type}，后面跟着一个 {@code String} 表示
     * {@code name}，后面跟着一个 {@code String} 表示 {@code actions}，后面跟着一个 {@code int} 表示
     * 证书的数量（值为“零”表示此对象没有关联的证书）。
     * 每个证书从一个 {@code String} 开始，表示证书类型，后面跟着一个 {@code int} 指定证书编码的长度，
     * 后面跟着证书编码本身，以字节数组的形式写入。
     */
    private void writeObject(java.io.ObjectOutputStream oos)
        throws IOException
    {
        oos.defaultWriteObject();

        if (certs==null || certs.length==0) {
            oos.writeInt(0);
        } else {
            // 写出证书的总数
            oos.writeInt(certs.length);
            // 写出每个证书，包括其类型
            for (int i=0; i < certs.length; i++) {
                java.security.cert.Certificate cert = certs[i];
                try {
                    oos.writeUTF(cert.getType());
                    byte[] encoded = cert.getEncoded();
                    ois.writeInt(encoded.length);
                    ois.write(encoded);
                } catch (CertificateEncodingException cee) {
                    throw new IOException(cee.getMessage());
                }
            }
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
        List<Certificate> certList = null;

        ois.defaultReadObject();

        if (type == null)
                throw new NullPointerException("type can't be null");

        // 处理流中的任何新式证书（如果存在）
        int size = ois.readInt();
        if (size > 0) {
            // 我们知道有 3 种不同的证书类型：X.509, PGP, SDSI，它们可能同时存在于流中
            cfs = new Hashtable<>(3);
            certList = new ArrayList<>(size > 20 ? 20 : size);
        } else if (size < 0) {
            throw new IOException("size cannot be negative");
        }

        for (int i=0; i<size; i++) {
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
                        ("Certificate factory for "+certType+" not found");
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
    }
}
