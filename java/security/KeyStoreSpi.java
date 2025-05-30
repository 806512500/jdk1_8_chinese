
/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;
import java.util.*;

import java.security.KeyStore.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import javax.crypto.SecretKey;

import javax.security.auth.callback.*;

/**
 * 该类定义了 {@code KeyStore} 类的 <i>服务提供者接口</i> (<b>SPI</b>)。
 * 所有该类中的抽象方法都必须由每个希望为特定密钥库类型提供实现的加密服务提供商实现。
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
     * 返回与给定别名关联的密钥，使用给定的密码恢复该密钥。该密钥必须通过调用 {@code setKeyEntry}，
     * 或通过调用 {@code setEntry} 并传递 {@code PrivateKeyEntry} 或 {@code SecretKeyEntry} 来与别名关联。
     *
     * @param alias 别名
     * @param password 用于恢复密钥的密码
     *
     * @return 请求的密钥，如果给定的别名不存在或不标识密钥相关条目，则返回 null。
     *
     * @exception NoSuchAlgorithmException 如果找不到恢复密钥的算法
     * @exception UnrecoverableKeyException 如果无法恢复密钥（例如，给定的密码错误）。
     */
    public abstract Key engineGetKey(String alias, char[] password)
        throws NoSuchAlgorithmException, UnrecoverableKeyException;

    /**
     * 返回与给定别名关联的证书链。该证书链必须通过调用 {@code setKeyEntry}，
     * 或通过调用 {@code setEntry} 并传递 {@code PrivateKeyEntry} 来与别名关联。
     *
     * @param alias 别名
     *
     * @return 证书链（按顺序排列，用户的证书在前，根证书机构在后），如果给定的别名不存在或不包含证书链，则返回 null。
     */
    public abstract Certificate[] engineGetCertificateChain(String alias);

    /**
     * 返回与给定别名关联的证书。
     *
     * <p> 如果给定的别名标识通过调用 {@code setCertificateEntry} 创建的条目，
     * 或通过调用 {@code setEntry} 并传递 {@code TrustedCertificateEntry} 创建的条目，
     * 则返回该条目中包含的受信任证书。
     *
     * <p> 如果给定的别名标识通过调用 {@code setKeyEntry} 创建的条目，
     * 或通过调用 {@code setEntry} 并传递 {@code PrivateKeyEntry} 创建的条目，
     * 则返回该条目中的证书链的第一个元素（如果存在证书链）。
     *
     * @param alias 别名
     *
     * @return 证书，如果给定的别名不存在或不包含证书，则返回 null。
     */
    public abstract Certificate engineGetCertificate(String alias);

    /**
     * 返回由给定别名标识的条目的创建日期。
     *
     * @param alias 别名
     *
     * @return 该条目的创建日期，如果给定的别名不存在，则返回 null。
     */
    public abstract Date engineGetCreationDate(String alias);

    /**
     * 将给定的密钥分配给给定的别名，并使用给定的密码保护该密钥。
     *
     * <p> 如果给定的密钥类型为 {@code java.security.PrivateKey}，
     * 则必须附带一个证书链来证明相应的公钥。
     *
     * <p> 如果给定的别名已存在，与该别名关联的密钥库信息将被给定的密钥（和可能的证书链）覆盖。
     *
     * @param alias 别名
     * @param key 要与别名关联的密钥
     * @param password 用于保护密钥的密码
     * @param chain 对应公钥的证书链（仅在给定的密钥类型为 {@code java.security.PrivateKey} 时需要）。
     *
     * @exception KeyStoreException 如果无法保护给定的密钥，或此操作因其他原因失败。
     */
    public abstract void engineSetKeyEntry(String alias, Key key,
                                           char[] password,
                                           Certificate[] chain)
        throws KeyStoreException;

    /**
     * 将给定的已保护密钥分配给给定的别名。
     *
     * <p> 如果已保护的密钥类型为 {@code java.security.PrivateKey}，
     * 则必须附带一个证书链来证明相应的公钥。
     *
     * <p> 如果给定的别名已存在，与该别名关联的密钥库信息将被给定的密钥（和可能的证书链）覆盖。
     *
     * @param alias 别名
     * @param key 要与别名关联的密钥（已保护格式）
     * @param chain 对应公钥的证书链（仅在已保护的密钥类型为 {@code java.security.PrivateKey} 时有用）。
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
     * 或通过调用 {@code setEntry} 并传递 {@code TrustedCertificateEntry} 创建的现有条目，
     * 则该现有条目中的受信任证书将被给定的证书覆盖。
     *
     * @param alias 别名
     * @param cert 证书
     *
     * @exception KeyStoreException 如果给定的别名已存在且不标识包含受信任证书的条目，
     * 或此操作因其他原因失败。
     */
    public abstract void engineSetCertificateEntry(String alias,
                                                   Certificate cert)
        throws KeyStoreException;

    /**
     * 从该密钥库中删除由给定别名标识的条目。
     *
     * @param alias 别名
     *
     * @exception KeyStoreException 如果无法删除该条目。
     */
    public abstract void engineDeleteEntry(String alias)
        throws KeyStoreException;

    /**
     * 列出该密钥库中的所有别名。
     *
     * @return 别名的枚举
     */
    public abstract Enumeration<String> engineAliases();

    /**
     * 检查给定的别名是否存在于该密钥库中。
     *
     * @param alias 别名
     *
     * @return 如果别名存在，则返回 true，否则返回 false。
     */
    public abstract boolean engineContainsAlias(String alias);

    /**
     * 检索该密钥库中的条目数量。
     *
     * @return 该密钥库中的条目数量
     */
    public abstract int engineSize();

    /**
     * 如果由给定别名标识的条目是通过调用 {@code setKeyEntry} 创建的，
     * 或通过调用 {@code setEntry} 并传递 {@code PrivateKeyEntry} 或 {@code SecretKeyEntry} 创建的，
     * 则返回 true。
     *
     * @param alias 要检查的密钥库条目的别名
     *
     * @return 如果由给定别名标识的条目是密钥相关条目，则返回 true，否则返回 false。
     */
    public abstract boolean engineIsKeyEntry(String alias);

    /**
     * 如果由给定别名标识的条目是通过调用 {@code setCertificateEntry} 创建的，
     * 或通过调用 {@code setEntry} 并传递 {@code TrustedCertificateEntry} 创建的，
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
     * <p> 该方法尝试将给定的证书与每个密钥库条目进行匹配。如果正在考虑的条目是通过调用 {@code setCertificateEntry} 创建的，
     * 或通过调用 {@code setEntry} 并传递 {@code TrustedCertificateEntry} 创建的，
     * 则将给定的证书与该条目的证书进行比较。
     *
     * <p> 如果正在考虑的条目是通过调用 {@code setKeyEntry} 创建的，
     * 或通过调用 {@code setEntry} 并传递 {@code PrivateKeyEntry} 创建的，
     * 则将给定的证书与该条目的证书链的第一个元素进行比较。
     *
     * @param cert 要匹配的证书。
     *
     * @return 与给定证书匹配的第一个条目的别名名称，如果该密钥库中不存在这样的条目，则返回 null。
     */
    public abstract String engineGetCertificateAlias(Certificate cert);

    /**
     * 将该密钥库存储到给定的输出流中，并使用给定的密码保护其完整性。
     *
     * @param stream 要写入该密钥库的输出流。
     * @param password 用于生成密钥库完整性检查的密码
     *
     * @exception IOException 如果数据存在 I/O 问题
     * @exception NoSuchAlgorithmException 如果找不到适当的完整性检查算法
     * @exception CertificateException 如果密钥库数据中包含的任何证书无法存储
     */
    public abstract void engineStore(OutputStream stream, char[] password)
        throws IOException, NoSuchAlgorithmException, CertificateException;

    /**
     * 使用给定的 {@code KeyStore.LoadStoreParmeter} 存储该密钥库。
     *
     * @param param 指定如何存储密钥库的 {@code KeyStore.LoadStoreParmeter}，
     *          可能为 {@code null}
     *
     * @exception IllegalArgumentException 如果给定的 {@code KeyStore.LoadStoreParmeter}
     *          输入未被识别
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
     * 从给定的输入流加载该密钥库。
     *
     * <p> 可以提供密码来解锁密钥库（例如，密钥库位于硬件令牌设备上），
     * 或检查密钥库数据的完整性。如果未提供密码进行完整性检查，
     * 则不进行完整性检查。
     *
     * @param stream 要从中加载密钥库的输入流，或 {@code null}
     * @param password 用于检查密钥库完整性的密码，用于解锁密钥库的密码，或 {@code null}
     *
     * @exception IOException 如果密钥库数据存在 I/O 或格式问题，如果需要密码但未提供，
     * 或提供的密码错误。如果错误是由于错误的密码引起的，{@code IOException} 的
     * {@link Throwable#getCause 原因} 应该是 {@code UnrecoverableKeyException}
     * @exception NoSuchAlgorithmException 如果找不到用于检查密钥库完整性的算法
     * @exception CertificateException 如果密钥库中的任何证书无法加载
     */
    public abstract void engineLoad(InputStream stream, char[] password)
        throws IOException, NoSuchAlgorithmException, CertificateException;

    /**
     * 使用给定的 {@code KeyStore.LoadStoreParameter} 加载该密钥库。
     *
     * <p> 注意，如果该 KeyStore 已经加载，则将重新初始化并从给定的参数再次加载。
     *
     * @param param 指定如何加载密钥库的 {@code KeyStore.LoadStoreParameter}，
     *          可能为 {@code null}
     *
     * @exception IllegalArgumentException 如果给定的 {@code KeyStore.LoadStoreParameter}
     *          输入未被识别
     * @exception IOException 如果密钥库数据存在 I/O 或格式问题。如果错误是由于错误的
     *         {@code ProtectionParameter}（例如，错误的密码）引起的，{@code IOException} 的
     *         {@link Throwable#getCause 原因} 应该是 {@code UnrecoverableKeyException}
     * @exception NoSuchAlgorithmException 如果找不到用于检查密钥库完整性的算法
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
     * 获取指定别名的 {@code KeyStore.Entry}，并使用指定的保护参数。
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
     *          {@code protParam} 不充分或无效
     * @exception UnrecoverableKeyException 如果条目是
     *          {@code PrivateKeyEntry} 或 {@code SecretKeyEntry}
     *          并且指定的 {@code protParam} 不包含恢复密钥所需的信息（例如，密码错误）
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
                        ("请求的条目需要密码");
            }
        }

        if (protParam instanceof KeyStore.PasswordProtection) {
            if (engineIsCertificateEntry(alias)) {
                throw new UnsupportedOperationException
                    ("受信任的证书条目不受密码保护");
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
     * 保存指定别名下的 {@code KeyStore.Entry}。
     * 使用指定的保护参数来保护 {@code Entry}。
     *
     * <p> 如果指定别名下已存在条目，则会被覆盖。
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
            throw new KeyStoreException("不支持的保护参数");
        }
        KeyStore.PasswordProtection pProtect = null;
        if (protParam != null) {
            pProtect = (KeyStore.PasswordProtection)protParam;
        }

        // 设置条目
        if (entry instanceof KeyStore.TrustedCertificateEntry) {
            if (protParam != null && pProtect.getPassword() != null) {
                // pre-1.5 风格的 setCertificateEntry 不允许密码
                throw new KeyStoreException
                    ("受信任的证书条目不受密码保护");
            } else {
                KeyStore.TrustedCertificateEntry tce =
                        (KeyStore.TrustedCertificateEntry)entry;
                engineSetCertificateEntry(alias, tce.getTrustedCertificate());
                return;
            }
        } else if (entry instanceof KeyStore.PrivateKeyEntry) {
            if (pProtect == null || pProtect.getPassword() == null) {
                // pre-1.5 风格的 setKeyEntry 需要密码
                throw new KeyStoreException
                    ("创建 PrivateKeyEntry 需要非空密码");
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
                // pre-1.5 风格的 setKeyEntry 需要密码
                throw new KeyStoreException
                    ("创建 SecretKeyEntry 需要非空密码");
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
                ("不支持的条目类型: " + entry.getClass().getName());
    }

    /**
     * 确定指定别名的 keystore {@code Entry} 是否是指定的
     * {@code entryClass} 的实例或子类。
     *
     * @param alias 别名
     * @param entryClass 条目类
     *
     * @return 如果指定别名的 keystore {@code Entry} 是指定的
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
