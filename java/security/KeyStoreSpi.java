
/*
 * 版权所有 (c) 1998, 2013, Oracle 和/或其附属公司。保留所有权利。
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

import java.io.*;
import java.util.*;

import java.security.KeyStore.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import javax.crypto.SecretKey;

import javax.security.auth.callback.*;

/**
 * 本类定义了 {@code KeyStore} 类的 <i>服务提供者接口</i> (<b>SPI</b>)。
 * 本类中的所有抽象方法都必须由每个希望为特定的密钥库类型提供实现的加密服务提供者实现。
 *
 * @author Jan Luehe
 *
 *
 * @see KeyStore
 *
 * @since 1.2
 */

public abstract class KeyStoreSpi {

    /**
     * 使用给定的密码恢复与给定别名关联的密钥。该密钥必须通过调用 {@code setKeyEntry}，
     * 或通过调用 {@code setEntry} 并带有 {@code PrivateKeyEntry} 或 {@code SecretKeyEntry} 来与别名关联。
     *
     * @param alias 别名名称
     * @param password 用于恢复密钥的密码
     *
     * @return 请求的密钥，如果给定的别名不存在或不标识与密钥相关的条目，则返回 null。
     *
     * @exception NoSuchAlgorithmException 如果无法找到恢复密钥的算法
     * @exception UnrecoverableKeyException 如果无法恢复密钥（例如，给定的密码错误）。
     */
    public abstract Key engineGetKey(String alias, char[] password)
        throws NoSuchAlgorithmException, UnrecoverableKeyException;

    /**
     * 返回与给定别名关联的证书链。该证书链必须通过调用 {@code setKeyEntry}，
     * 或通过调用 {@code setEntry} 并带有 {@code PrivateKeyEntry} 来与别名关联。
     *
     * @param alias 别名名称
     *
     * @return 证书链（按用户证书在前，根证书机构在后的顺序排列），如果给定的别名不存在或不包含证书链，则返回 null。
     */
    public abstract Certificate[] engineGetCertificateChain(String alias);

    /**
     * 返回与给定别名关联的证书。
     *
     * <p> 如果给定的别名标识通过调用 {@code setCertificateEntry} 创建的条目，
     * 或通过调用 {@code setEntry} 并带有 {@code TrustedCertificateEntry} 创建的条目，
     * 则返回该条目中包含的受信任证书。
     *
     * <p> 如果给定的别名标识通过调用 {@code setKeyEntry} 创建的条目，
     * 或通过调用 {@code setEntry} 并带有 {@code PrivateKeyEntry} 创建的条目，
     * 则返回该条目中的证书链的第一个元素（如果存在证书链）。
     *
     * @param alias 别名名称
     *
     * @return 证书，如果给定的别名不存在或不包含证书，则返回 null。
     */
    public abstract Certificate engineGetCertificate(String alias);

    /**
     * 返回由给定别名标识的条目的创建日期。
     *
     * @param alias 别名名称
     *
     * @return 该条目的创建日期，如果给定的别名不存在，则返回 null。
     */
    public abstract Date engineGetCreationDate(String alias);

    /**
     * 将给定的密钥分配给给定的别名，并使用给定的密码保护它。
     *
     * <p>如果给定的密钥类型为 {@code java.security.PrivateKey}，
     * 则必须附带一个证书链来证明相应的公钥。
     *
     * <p>如果给定的别名已存在，与之关联的密钥库信息将被给定的密钥（和可能的证书链）覆盖。
     *
     * @param alias 别名名称
     * @param key 要与别名关联的密钥
     * @param password 用于保护密钥的密码
     * @param chain 相应公钥的证书链（仅在给定的密钥类型为 {@code java.security.PrivateKey} 时需要）。
     *
     * @exception KeyStoreException 如果无法保护给定的密钥，或此操作因其他原因失败。
     */
    public abstract void engineSetKeyEntry(String alias, Key key,
                                           char[] password,
                                           Certificate[] chain)
        throws KeyStoreException;

    /**
     * 将给定的已受保护的密钥分配给给定的别名。
     *
     * <p>如果受保护的密钥类型为 {@code java.security.PrivateKey}，
     * 则必须附带一个证书链来证明相应的公钥。
     *
     * <p>如果给定的别名已存在，与之关联的密钥库信息将被给定的密钥（和可能的证书链）覆盖。
     *
     * @param alias 别名名称
     * @param key 要与别名关联的密钥（受保护格式）
     * @param chain 相应公钥的证书链（仅在受保护的密钥类型为 {@code java.security.PrivateKey} 时有用）。
     *
     * @exception KeyStoreException 如果此操作失败。
     */
    public abstract void engineSetKeyEntry(String alias, byte[] key,
                                           Certificate[] chain)
        throws KeyStoreException;

    /**
     * 将给定的证书分配给给定的别名。
     *
     * <p> 如果给定的别名标识通过调用 {@code setCertificateEntry} 创建的现有条目，
     * 或通过调用 {@code setEntry} 并带有 {@code TrustedCertificateEntry} 创建的现有条目，
     * 则现有条目中的受信任证书将被给定的证书覆盖。
     *
     * @param alias 别名名称
     * @param cert 证书
     *
     * @exception KeyStoreException 如果给定的别名已存在且不标识包含受信任证书的条目，
     * 或此操作因其他原因失败。
     */
    public abstract void engineSetCertificateEntry(String alias,
                                                   Certificate cert)
        throws KeyStoreException;

                /**
     * 从这个密钥库中删除由给定别名标识的条目。
     *
     * @param alias 别名名称
     *
     * @exception KeyStoreException 如果条目无法移除。
     */
    public abstract void engineDeleteEntry(String alias)
        throws KeyStoreException;

    /**
     * 列出这个密钥库中的所有别名名称。
     *
     * @return 别名名称的枚举
     */
    public abstract Enumeration<String> engineAliases();

    /**
     * 检查给定的别名是否存在于这个密钥库中。
     *
     * @param alias 别名名称
     *
     * @return 如果别名存在则返回 true，否则返回 false
     */
    public abstract boolean engineContainsAlias(String alias);

    /**
     * 检索这个密钥库中的条目数量。
     *
     * @return 这个密钥库中的条目数量
     */
    public abstract int engineSize();

    /**
     * 如果由给定别名标识的条目是通过调用 {@code setKeyEntry} 创建的，
     * 或者是通过调用 {@code setEntry} 并使用 {@code PrivateKeyEntry} 或 {@code SecretKeyEntry} 创建的，
     * 则返回 true。
     *
     * @param alias 要检查的密钥库条目的别名
     *
     * @return 如果由给定别名标识的条目是与密钥相关的，则返回 true，否则返回 false。
     */
    public abstract boolean engineIsKeyEntry(String alias);

    /**
     * 如果由给定别名标识的条目是通过调用 {@code setCertificateEntry} 创建的，
     * 或者是通过调用 {@code setEntry} 并使用 {@code TrustedCertificateEntry} 创建的，
     * 则返回 true。
     *
     * @param alias 要检查的密钥库条目的别名
     *
     * @return 如果由给定别名标识的条目包含受信任的证书，则返回 true，否则返回 false。
     */
    public abstract boolean engineIsCertificateEntry(String alias);

    /**
     * 返回与给定证书匹配的第一个密钥库条目的（别名）名称。
     *
     * <p>此方法尝试将给定的证书与每个密钥库条目匹配。如果正在考虑的条目是通过调用 {@code setCertificateEntry} 创建的，
     * 或者是通过调用 {@code setEntry} 并使用 {@code TrustedCertificateEntry} 创建的，
     * 则将给定的证书与该条目的证书进行比较。
     *
     * <p>如果正在考虑的条目是通过调用 {@code setKeyEntry} 创建的，
     * 或者是通过调用 {@code setEntry} 并使用 {@code PrivateKeyEntry} 创建的，
     * 则将给定的证书与该条目的证书链中的第一个元素进行比较。
     *
     * @param cert 要匹配的证书。
     *
     * @return 与给定证书匹配的第一个条目的别名名称，如果此密钥库中不存在这样的条目，则返回 null。
     */
    public abstract String engineGetCertificateAlias(Certificate cert);

    /**
     * 将此密钥库存储到给定的输出流中，并使用给定的密码保护其完整性。
     *
     * @param stream 将此密钥库写入的输出流。
     * @param password 用于生成密钥库完整性检查的密码
     *
     * @exception IOException 如果数据存在 I/O 问题
     * @exception NoSuchAlgorithmException 如果找不到适当的完整性检查算法
     * @exception CertificateException 如果密钥库数据中包含的任何证书无法存储
     */
    public abstract void engineStore(OutputStream stream, char[] password)
        throws IOException, NoSuchAlgorithmException, CertificateException;

    /**
     * 使用给定的 {@code KeyStore.LoadStoreParmeter} 存储此密钥库。
     *
     * @param param 指定如何存储密钥库的 {@code KeyStore.LoadStoreParmeter}，
     *          可能为 {@code null}
     *
     * @exception IllegalArgumentException 如果给定的 {@code KeyStore.LoadStoreParmeter} 输入未被识别
     * @exception IOException 如果数据存在 I/O 问题
     * @exception NoSuchAlgorithmException 如果找不到适当的完整性检查算法
     * @exception CertificateException 如果密钥库数据中包含的任何证书无法存储
     *
     * @since 1.5
     */
    public void engineStore(KeyStore.LoadStoreParameter param)
                throws IOException, NoSuchAlgorithmException,
                CertificateException {
        throw new UnsupportedOperationException();
    }

    /**
     * 从给定的输入流加载密钥库。
     *
     * <p>可以提供密码来解锁密钥库（例如，密钥库位于硬件令牌设备上），
     * 或检查密钥库数据的完整性。如果未提供密码进行完整性检查，
     * 则不执行完整性检查。
     *
     * @param stream 从其中加载密钥库的输入流，
     * 或 {@code null}
     * @param password 用于检查密钥库完整性的密码，用于解锁密钥库的密码，
     * 或 {@code null}
     *
     * @exception IOException 如果密钥库数据存在 I/O 或格式问题，如果需要密码但未提供，
     * 或提供的密码不正确。如果错误是由于错误的密码引起的，{@code IOException} 的
     * {@link Throwable#getCause 原因} 应该是 {@code UnrecoverableKeyException}
     * @exception NoSuchAlgorithmException 如果用于检查密钥库完整性的算法找不到
     * @exception CertificateException 如果密钥库中的任何证书无法加载
     */
    public abstract void engineLoad(InputStream stream, char[] password)
        throws IOException, NoSuchAlgorithmException, CertificateException;

    /**
     * 使用给定的 {@code KeyStore.LoadStoreParameter} 加载密钥库。
     *
     * <p>注意，如果此 KeyStore 已经被加载，它将被重新初始化并再次从给定的参数加载。
     *
     * @param param 指定如何加载密钥库的 {@code KeyStore.LoadStoreParameter}，
     *          可能为 {@code null}
     *
     * @exception IllegalArgumentException 如果给定的 {@code KeyStore.LoadStoreParameter} 输入未被识别
     * @exception IOException 如果密钥库数据存在 I/O 或格式问题。如果错误是由于错误的
     *         {@code ProtectionParameter}（例如，错误的密码）引起的，{@code IOException} 的
     *         {@link Throwable#getCause 原因} 应该是 {@code UnrecoverableKeyException}
     * @exception NoSuchAlgorithmException 如果用于检查密钥库完整性的算法找不到
     * @exception CertificateException 如果密钥库中的任何证书无法加载
     *
     * @since 1.5
     */
    public void engineLoad(KeyStore.LoadStoreParameter param)
                throws IOException, NoSuchAlgorithmException,
                CertificateException {


                    if (param == null) {
            engineLoad((InputStream)null, (char[])null);
            return;
        }

        if (param instanceof KeyStore.SimpleLoadStoreParameter) {
            ProtectionParameter protection = param.getProtectionParameter();
            char[] password;
            if (protection instanceof PasswordProtection) {
                password = ((PasswordProtection)protection).getPassword();
            } else if (protection instanceof CallbackHandlerProtection) {
                CallbackHandler handler =
                    ((CallbackHandlerProtection)protection).getCallbackHandler();
                PasswordCallback callback =
                    new PasswordCallback("Password: ", false);
                try {
                    handler.handle(new Callback[] {callback});
                } catch (UnsupportedCallbackException e) {
                    throw new NoSuchAlgorithmException
                        ("Could not obtain password", e);
                }
                password = callback.getPassword();
                callback.clearPassword();
                if (password == null) {
                    throw new NoSuchAlgorithmException
                        ("No password provided");
                }
            } else {
                throw new NoSuchAlgorithmException("ProtectionParameter must"
                    + " be PasswordProtection or CallbackHandlerProtection");
            }
            engineLoad(null, password);
            return;
        }

        throw new UnsupportedOperationException();
    }

    /**
     * 获取指定别名的 {@code KeyStore.Entry}，使用指定的保护参数。
     *
     * @param alias 获取此别名的 {@code KeyStore.Entry}
     * @param protParam 用于保护 {@code Entry} 的 {@code ProtectionParameter}，
     *          可能为 {@code null}
     *
     * @return 指定别名的 {@code KeyStore.Entry}，
     *          如果没有这样的条目，则返回 {@code null}
     *
     * @exception KeyStoreException 如果操作失败
     * @exception NoSuchAlgorithmException 如果无法找到恢复条目的算法
     * @exception UnrecoverableEntryException 如果指定的
     *          {@code protParam} 不足或无效
     * @exception UnrecoverableKeyException 如果条目是
     *          {@code PrivateKeyEntry} 或 {@code SecretKeyEntry}
     *          并且指定的 {@code protParam} 不包含恢复密钥所需的信息（例如，错误的密码）
     *
     * @since 1.5
     */
    public KeyStore.Entry engineGetEntry(String alias,
                        KeyStore.ProtectionParameter protParam)
                throws KeyStoreException, NoSuchAlgorithmException,
                UnrecoverableEntryException {

        if (!engineContainsAlias(alias)) {
            return null;
        }

        if (protParam == null) {
            if (engineIsCertificateEntry(alias)) {
                return new KeyStore.TrustedCertificateEntry
                                (engineGetCertificate(alias));
            } else {
                throw new UnrecoverableKeyException
                        ("requested entry requires a password");
            }
        }

        if (protParam instanceof KeyStore.PasswordProtection) {
            if (engineIsCertificateEntry(alias)) {
                throw new UnsupportedOperationException
                    ("trusted certificate entries are not password-protected");
            } else if (engineIsKeyEntry(alias)) {
                KeyStore.PasswordProtection pp =
                        (KeyStore.PasswordProtection)protParam;
                char[] password = pp.getPassword();

                Key key = engineGetKey(alias, password);
                if (key instanceof PrivateKey) {
                    Certificate[] chain = engineGetCertificateChain(alias);
                    return new KeyStore.PrivateKeyEntry((PrivateKey)key, chain);
                } else if (key instanceof SecretKey) {
                    return new KeyStore.SecretKeyEntry((SecretKey)key);
                }
            }
        }

        throw new UnsupportedOperationException();
    }

    /**
     * 保存一个 {@code KeyStore.Entry} 到指定的别名下。
     * 指定的保护参数用于保护 {@code Entry}。
     *
     * <p> 如果指定的别名已经存在条目，则会被覆盖。
     *
     * @param alias 保存 {@code KeyStore.Entry} 的别名
     * @param entry 要保存的 {@code Entry}
     * @param protParam 用于保护 {@code Entry} 的 {@code ProtectionParameter}，
     *          可能为 {@code null}
     *
     * @exception KeyStoreException 如果此操作失败
     *
     * @since 1.5
     */
    public void engineSetEntry(String alias, KeyStore.Entry entry,
                        KeyStore.ProtectionParameter protParam)
                throws KeyStoreException {

        // 获取密码
        if (protParam != null &&
            !(protParam instanceof KeyStore.PasswordProtection)) {
            throw new KeyStoreException("unsupported protection parameter");
        }
        KeyStore.PasswordProtection pProtect = null;
        if (protParam != null) {
            pProtect = (KeyStore.PasswordProtection)protParam;
        }

        // 设置条目
        if (entry instanceof KeyStore.TrustedCertificateEntry) {
            if (protParam != null && pProtect.getPassword() != null) {
                // pre-1.5 style setCertificateEntry did not allow password
                throw new KeyStoreException
                    ("trusted certificate entries are not password-protected");
            } else {
                KeyStore.TrustedCertificateEntry tce =
                        (KeyStore.TrustedCertificateEntry)entry;
                engineSetCertificateEntry(alias, tce.getTrustedCertificate());
                return;
            }
        } else if (entry instanceof KeyStore.PrivateKeyEntry) {
            if (pProtect == null || pProtect.getPassword() == null) {
                // pre-1.5 style setKeyEntry required password
                throw new KeyStoreException
                    ("non-null password required to create PrivateKeyEntry");
            } else {
                engineSetKeyEntry
                    (alias,
                    ((KeyStore.PrivateKeyEntry)entry).getPrivateKey(),
                    pProtect.getPassword(),
                    ((KeyStore.PrivateKeyEntry)entry).getCertificateChain());
                return;
            }
        } else if (entry instanceof KeyStore.SecretKeyEntry) {
            if (pProtect == null || pProtect.getPassword() == null) {
                // pre-1.5 style setKeyEntry required password
                throw new KeyStoreException
                    ("non-null password required to create SecretKeyEntry");
            } else {
                engineSetKeyEntry
                    (alias,
                    ((KeyStore.SecretKeyEntry)entry).getSecretKey(),
                    pProtect.getPassword(),
                    (Certificate[])null);
                return;
            }
        }


                    throw new KeyStoreException
                ("unsupported entry type: " + entry.getClass().getName());
    }

    /**
     * 确定指定的 {@code alias} 的密钥库 {@code Entry} 是否是指定的
     * {@code entryClass} 的实例或子类。
     *
     * @param alias 别名名称
     * @param entryClass 入口类
     *
     * @return 如果指定的 {@code alias} 的密钥库 {@code Entry} 是指定的
     *          {@code entryClass} 的实例或子类，则返回 true，否则返回 false
     *
     * @since 1.5
     */
    public boolean
        engineEntryInstanceOf(String alias,
                              Class<? extends KeyStore.Entry> entryClass)
    {
        if (entryClass == KeyStore.TrustedCertificateEntry.class) {
            return engineIsCertificateEntry(alias);
        }
        if (entryClass == KeyStore.PrivateKeyEntry.class) {
            return engineIsKeyEntry(alias) &&
                        engineGetCertificate(alias) != null;
        }
        if (entryClass == KeyStore.SecretKeyEntry.class) {
            return engineIsKeyEntry(alias) &&
                        engineGetCertificate(alias) == null;
        }
        return false;
    }
}
