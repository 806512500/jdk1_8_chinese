
/*
 * 版权所有 (c) 1997, 2017, Oracle 和/或其附属公司。保留所有权利。
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
 * UnresolvedPermission 类用于保存在 Policy 初始化时“未解决”的权限。
 * 未解决的权限是指在 Policy 初始化时其实际的 Permission 类尚不存在的权限（参见下文）。
 *
 * <p>Java 运行时的策略（指定从各种主体获取的代码可用的权限）
 * 由 Policy 对象表示。
 * 每当 Policy 被初始化或刷新时，都会为所有由 Policy 允许的权限
 * 创建适当类的 Permission 对象。
 *
 * <p>策略配置中引用的许多权限类类型是本地存在的（即，可以在 CLASSPATH 上找到的）。
 * 这样的权限对象可以在 Policy 初始化期间实例化。例如，总是可以实例化一个 java.io.FilePermission，
 * 因为 FilePermission 类可以在 CLASSPATH 上找到。
 *
 * <p>其他权限类可能在 Policy 初始化时还不存在。例如，引用的权限类可能在稍后加载的 JAR 文件中。
 * 对于每个这样的类，都会实例化一个 UnresolvedPermission。
 * 因此，UnresolvedPermission 实质上是一个“占位符”，包含有关权限的信息。
 *
 * <p>稍后，当代码调用 AccessController.checkPermission
 * 检查一个之前未解决的权限类型，但其类已经加载的权限时，
 * 之前未解决的权限类型会被“解决”。也就是说，
 * 对于每个这样的 UnresolvedPermission，都会根据 UnresolvedPermission 中的信息
 * 实例化一个适当类类型的新对象。
 *
 * <p>为了实例化新类，UnresolvedPermission 假设该类提供了无参、一参和/或二参的构造函数。
 * 无参构造函数用于实例化没有名称和没有动作的权限。
 * 假设一参构造函数接受一个 {@code String} 名称作为输入，二参构造函数接受一个
 * {@code String} 名称和 {@code String} 动作作为输入。UnresolvedPermission 可能会调用
 * 一个带有 {@code null} 名称和/或动作的构造函数。
 * 如果没有合适的权限构造函数可用，UnresolvedPermission 将被忽略，相关的权限
 * 将不会授予执行代码。
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
     * 当此未解决的权限被解决时将创建的权限类的类名。
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
     * 权限的动作。
     *
     * @serial
     */
    private String actions;

    private transient java.security.cert.Certificate certs[];

    /**
     * 创建一个新的 UnresolvedPermission，包含稍后实际创建指定类的权限所需的信息，
     * 当权限被解决时。
     *
     * @param type 当此未解决的权限被解决时将创建的权限类的类名。
     * @param name 权限的名称。
     * @param actions 权限的动作。
     * @param certs 权限类被签名的证书。这是一个证书链的列表，每个链由一个
     * 签名证书及其可选的支持证书链组成。每个链按自底向上的顺序排列（即，签名证书在前，
     * （根）证书机构在后）。签名证书从数组中复制。对数组的后续更改不会影响此 UnsolvedPermission。
     */
    public UnresolvedPermission(String type,
                                String name,
                                String actions,
                                java.security.cert.Certificate certs[])
    {
        super(type);

        if (type == null)
                throw new NullPointerException("type can't be null");

        this.type = type;
        this.name = name;
        this.actions = actions;
        if (certs != null) {
            // 从证书列表中提取签名证书。
            for (int i=0; i<certs.length; i++) {
                if (!(certs[i] instanceof X509Certificate)) {
                    // 没有签名证书的概念，所以我们存储整个证书数组
                    this.certs = certs.clone();
                    break;
                }
            }

            if (this.certs == null) {
                // 遍历证书列表，查看所有证书是否都是签名证书。
                int i = 0;
                int count = 0;
                while (i < certs.length) {
                    count++;
                    while (((i+1) < certs.length) &&
                           ((X509Certificate)certs[i]).getIssuerDN().equals(
                               ((X509Certificate)certs[i+1]).getSubjectDN())) {
                        i++;
                    }
                    i++;
                }
                if (count == certs.length) {
                    // 所有证书都是签名证书，所以我们存储整个数组
                    this.certs = certs.clone();
                }


                            if (this.certs == null) {
                    // 提取签名者证书
                    ArrayList<java.security.cert.Certificate> signerCerts =
                        new ArrayList<>();
                    i = 0;
                    while (i < certs.length) {
                        signerCerts.add(certs[i]);
                        while (((i+1) < certs.length) &&
                            ((X509Certificate)certs[i]).getIssuerDN().equals(
                              ((X509Certificate)certs[i+1]).getSubjectDN())) {
                            i++;
                        }
                        i++;
                    }
                    this.certs =
                        new java.security.cert.Certificate[signerCerts.size()];
                    signerCerts.toArray(this.certs);
                }
            }
        }
    }


    private static final Class[] PARAMS0 = { };
    private static final Class[] PARAMS1 = { String.class };
    private static final Class[] PARAMS2 = { String.class, String.class };

    /**
     * 尝试使用传递的权限的类加载器解析此权限。
     */
    Permission resolve(Permission p, java.security.cert.Certificate certs[]) {
        if (this.certs != null) {
            // 如果 p 未签名，我们没有匹配
            if (certs == null) {
                return null;
            }

            // this.certs 中的所有证书必须存在于 certs 中
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
                debug.println("NoSuchMethodException:\n  无法找到 " +
                        "适当的构造函数 " + type);
                nsme.printStackTrace();
            }
            return null;
        } catch (Exception e) {
            if (debug != null ) {
                debug.println("无法实例化 " + name);
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * 该方法对于未解析的权限始终返回 false。
     * 即，UnresolvedPermission 从不被认为隐含另一个权限。
     *
     * @param p 要检查的权限。
     *
     * @return false。
     */
    public boolean implies(Permission p) {
        return false;
    }

    /**
     * 检查两个 UnresolvedPermission 对象是否相等。
     * 检查 <i>obj</i> 是否为 UnresolvedPermission，并且具有与该对象相同的类型（类）名称、权限名称、操作和证书。
     *
     * <p> 为了确定证书的相等性，此方法仅比较实际的签名者证书。此方法不考虑支持的证书链。
     *
     * @param obj 要测试与该对象相等的对象。
     *
     * @return 如果 obj 是 UnresolvedPermission，并且具有与该对象相同的类型（类）名称、权限名称、操作和证书，则返回 true。
     */
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


                    int i, j;
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
     * 返回此对象的哈希码值。
     *
     * @return 此对象的哈希码值。
     */

    public int hashCode() {
        int hash = type.hashCode();
        if (name != null)
            hash ^= name.hashCode();
        if (actions != null)
            hash ^= actions.hashCode();
        return hash;
    }

    /**
     * 返回动作的规范字符串表示形式，目前是空字符串 ""，因为 UnresolvedPermission 没有动作。
     * 即，当此 UnresolvedPermission 被解析时创建的权限可能有非空的动作，但 UnresolvedPermission
     * 本身从不被认为有任何动作。
     *
     * @return 空字符串 ""。
     */
    public String getActions()
    {
        return "";
    }

    /**
     * 获取未解析的底层权限的类型（类名）。
     *
     * @return 未解析的底层权限的类型（类名）。
     *
     * @since 1.5
     */
    public String getUnresolvedType() {
        return type;
    }

    /**
     * 获取未解析的底层权限的目标名称。
     *
     * @return 未解析的底层权限的目标名称，或 {@code null}，如果没有目标名称。
     *
     * @since 1.5
     */
    public String getUnresolvedName() {
        return name;
    }

    /**
     * 获取未解析的底层权限的动作。
     *
     * @return 未解析的底层权限的动作，或 {@code null}，如果没有动作。
     *
     * @since 1.5
     */
    public String getUnresolvedActions() {
        return actions;
    }

    /**
     * 获取未解析的底层权限的签名证书（不包括支持链）。
     *
     * @return 未解析的底层权限的签名证书，或 null，如果没有签名证书。每次调用此方法时返回一个新数组。
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
     * @return 有关此 UnresolvedPermission 的信息。
     */
    public String toString() {
        return "(unresolved " + type + " " + name + " " + actions + ")";
    }

    /**
     * 返回一个用于存储 UnresolvedPermission 对象的新 PermissionCollection 对象。
     * <p>
     * @return 一个适合存储 UnresolvedPermissions 的新 PermissionCollection 对象。
     */

    public PermissionCollection newPermissionCollection() {
        return new UnresolvedPermissionCollection();
    }

    /**
     * 将此对象写入流（即序列化它）。
     *
     * @serialData 一个初始的 {@code String} 表示 {@code type}，后跟一个表示 {@code name} 的 {@code String}，
     * 后跟一个表示 {@code actions} 的 {@code String}，后跟一个表示证书数量的 {@code int}（值为“零”表示此对象没有关联的证书）。
     * 每个证书从表示证书类型的 {@code String} 开始，后跟一个指定证书编码长度的 {@code int}，
     * 后跟证书编码本身，以字节数组的形式写入。
     */
    private void writeObject(java.io.ObjectOutputStream oos)
        throws IOException
    {
        oos.defaultWriteObject();

        if (certs == null || certs.length == 0) {
            oos.writeInt(0);
        } else {
            // 写出证书总数
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

        // 处理流中可能存在的新样式证书
        int size = ois.readInt();
        if (size > 0) {
            // 我们知道有 3 种不同的证书类型：X.509、PGP、SDSI，这些类型可能同时存在于流中
            cfs = new Hashtable<>(3);
            certList = new ArrayList<>(size > 20 ? 20 : size);
        } else if (size < 0) {
            throw new IOException("size cannot be negative");
        }


                    for (int i=0; i<size; i++) {
            // 读取证书类型，并实例化该类型的证书工厂（如果可能，重用现有的工厂）
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
                        ("未找到 " + certType + " 的证书工厂");
                }
                // 存储证书工厂以便后续重用
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
