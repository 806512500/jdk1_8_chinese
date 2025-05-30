/*
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 一个加密原语的枚举。
 *
 * @since 1.7
 */
public enum CryptoPrimitive {
    /**
     * 哈希函数
     */
    MESSAGE_DIGEST,

    /**
     * 加密随机数生成器
     */
    SECURE_RANDOM,

    /**
     * 对称原语：分组密码
     */
    BLOCK_CIPHER,

    /**
     * 对称原语：流密码
     */
    STREAM_CIPHER,

    /**
     * 对称原语：消息认证码
     */
    MAC,

    /**
     * 对称原语：密钥包装
     */
    KEY_WRAP,

    /**
     * 非对称原语：公钥加密
     */
    PUBLIC_KEY_ENCRYPTION,

    /**
     * 非对称原语：签名方案
     */
    SIGNATURE,

    /**
     * 非对称原语：密钥封装机制
     */
    KEY_ENCAPSULATION,

    /**
     * 非对称原语：密钥协商和密钥分发
     */
    KEY_AGREEMENT
}
