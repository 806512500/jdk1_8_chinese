/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.util.*;

/**
 * 该类是一个简单的密钥对（公钥和私钥）持有者。它不强制执行任何安全性，初始化后应被视为PrivateKey。
 *
 * @see PublicKey
 * @see PrivateKey
 *
 * @author Benjamin Renaud
 */

public final class KeyPair implements java.io.Serializable {

    private static final long serialVersionUID = -7565189502268009837L;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    /**
     * 从给定的公钥和私钥构造一个密钥对。
     *
     * <p>请注意，此构造函数仅存储生成的密钥对中的公钥和私钥组件的引用。这是安全的，
     * 因为 {@code Key} 对象是不可变的。
     *
     * @param publicKey 公钥。
     *
     * @param privateKey 私钥。
     */
    public KeyPair(PublicKey publicKey, PrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    /**
     * 返回此密钥对的公钥组件的引用。
     *
     * @return 公钥的引用。
     */
    public PublicKey getPublic() {
        return publicKey;
    }

     /**
     * 返回此密钥对的私钥组件的引用。
     *
     * @return 私钥的引用。
     */
   public PrivateKey getPrivate() {
        return privateKey;
    }
}
