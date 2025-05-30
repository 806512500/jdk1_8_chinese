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

/**
 * 一个私钥。
 * 此接口的目的是对所有私钥接口进行分组（并提供类型安全）。
 * <p>
 * 注意：专门的私钥接口扩展了此接口。
 * 例如，参见 {@link java.security.interfaces} 中的 {@code DSAPrivateKey} 接口。
 * <p>
 * 实现应覆盖来自 {@link javax.security.auth.Destroyable} 接口的默认 {@code destroy} 和
 * {@code isDestroyed} 方法，以使敏感的密钥信息能够被销毁、清除，或者在信息不可变的情况下，解除引用。
 * 最后，由于 {@code PrivateKey} 是 {@code Serializable}，实现还应覆盖
 * {@link java.io.ObjectOutputStream#writeObject(java.lang.Object)}
 * 以防止已被销毁的密钥被序列化。
 *
 * @see Key
 * @see PublicKey
 * @see Certificate
 * @see Signature#initVerify
 * @see java.security.interfaces.DSAPrivateKey
 * @see java.security.interfaces.RSAPrivateKey
 * @see java.security.interfaces.RSAPrivateCrtKey
 *
 * @author Benjamin Renaud
 * @author Josh Bloch
 */

public interface PrivateKey extends Key, javax.security.auth.Destroyable {

    // 声明 serialVersionUID 以与 JDK1.1 兼容
    /**
     * 设置类指纹以表示与类的先前版本的序列化兼容性。
     */
    static final long serialVersionUID = 6034044314589513430L;
}
