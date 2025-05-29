/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

/**
 * 该类用于表示可以数字签名数据的身份。
 *
 * <p>管理签名者的私钥是一个重要且敏感的问题，应由子类根据其预期用途适当处理。
 *
 * @see Identity
 *
 * @author Benjamin Renaud
 *
 * @deprecated 该类不再使用。其功能已被 {@code java.security.KeyStore}、
 * {@code java.security.cert} 包和 {@code java.security.Principal} 所取代。
 */
@Deprecated
public abstract class Signer extends Identity {

    private static final long serialVersionUID = -1763464102261361480L;

    /**
     * 签名者的私钥。
     *
     * @serial
     */
    private PrivateKey privateKey;

    /**
     * 创建一个签名者。此构造函数仅用于序列化。
     */
    protected Signer() {
        super();
    }


    /**
     * 使用指定的身份名称创建签名者。
     *
     * @param name 身份名称。
     */
    public Signer(String name) {
        super(name);
    }

    /**
     * 使用指定的身份名称和范围创建签名者。
     *
     * @param name 身份名称。
     *
     * @param scope 身份的范围。
     *
     * @exception KeyManagementException 如果在范围内已经存在同名的身份。
     */
    public Signer(String name, IdentityScope scope)
    throws KeyManagementException {
        super(name, scope);
    }

    /**
     * 返回此签名者的私钥。
     *
     * <p>首先，如果有安全管理者，其 {@code checkSecurityAccess}
     * 方法将被调用，参数为 {@code "getSignerPrivateKey"}
     * 以确定是否可以返回私钥。
     *
     * @return 此签名者的私钥，如果私钥尚未设置则返回 null。
     *
     * @exception  SecurityException  如果存在安全管理者且其
     * {@code checkSecurityAccess} 方法不允许返回私钥。
     *
     * @see SecurityManager#checkSecurityAccess
     */
    public PrivateKey getPrivateKey() {
        check("getSignerPrivateKey");
        return privateKey;
    }

   /**
     * 为该签名者设置密钥对（公钥和私钥）。
     *
     * <p>首先，如果有安全管理者，其 {@code checkSecurityAccess}
     * 方法将被调用，参数为 {@code "setSignerKeyPair"}
     * 以确定是否可以设置密钥对。
     *
     * @param pair 已初始化的密钥对。
     *
     * @exception InvalidParameterException 如果密钥对未正确初始化。
     * @exception KeyException 如果由于其他原因无法设置密钥对。
     * @exception  SecurityException  如果存在安全管理者且其
     * {@code checkSecurityAccess} 方法不允许设置密钥对。
     *
     * @see SecurityManager#checkSecurityAccess
     */
    public final void setKeyPair(KeyPair pair)
    throws InvalidParameterException, KeyException {
        check("setSignerKeyPair");
        final PublicKey pub = pair.getPublic();
        PrivateKey priv = pair.getPrivate();

        if (pub == null || priv == null) {
            throw new InvalidParameterException();
        }
        try {
            AccessController.doPrivileged(
                new PrivilegedExceptionAction<Void>() {
                public Void run() throws KeyManagementException {
                    setPublicKey(pub);
                    return null;
                }
            });
        } catch (PrivilegedActionException pae) {
            throw (KeyManagementException) pae.getException();
        }
        privateKey = priv;
    }

    String printKeys() {
        String keys = "";
        PublicKey publicKey = getPublicKey();
        if (publicKey != null && privateKey != null) {
            keys = "\tpublic and private keys initialized";

        } else {
            keys = "\tno keys";
        }
        return keys;
    }

    /**
     * 返回关于签名者的信息字符串。
     *
     * @return 关于签名者的信息字符串。
     */
    public String toString() {
        return "[Signer]" + super.toString();
    }

    private static void check(String directive) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSecurityAccess(directive);
        }
    }

}
