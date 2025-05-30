/*
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security.cert;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * 此接口表示一个 X.509 扩展。
 *
 * <p>
 * 扩展提供了一种将附加属性与用户或公钥关联的方法，并用于管理认证层次结构。扩展格式还允许社区定义私有扩展以携带这些社区特有的信息。
 *
 * <p>
 * 每个扩展包含一个对象标识符、一个表示其是否为关键扩展的设置，以及一个 ASN.1 DER 编码的值。其 ASN.1 定义为：
 *
 * <pre>
 *
 *     Extension ::= SEQUENCE {
 *         extnId        OBJECT IDENTIFIER,
 *         critical      BOOLEAN DEFAULT FALSE,
 *         extnValue     OCTET STRING
 *                 -- 包含为与 extnId 对象标识符值注册的类型编码的值
 *     }
 *
 * </pre>
 *
 * <p>
 * 此接口旨在提供对单个扩展的访问，而 {@link java.security.cert.X509Extension} 更适合用于访问一组扩展。
 *
 * @since 1.7
 */
public interface Extension {

    /**
     * 获取扩展的对象标识符。
     *
     * @return 对象标识符作为字符串
     */
    String getId();

    /**
     * 获取扩展的关键性设置。
     *
     * @return 如果这是一个关键扩展，则返回 true。
     */
    boolean isCritical();

    /**
     * 获取扩展的 DER 编码值。注意，这是编码为 OCTET STRING 的字节。它不包括 OCTET STRING 标签和长度。
     *
     * @return 扩展值的副本，如果没有扩展值，则返回 {@code null}。
     */
    byte[] getValue();

    /**
     * 生成扩展的 DER 编码并将其写入输出流。
     *
     * @param out 输出流
     * @exception IOException 在编码或输出时发生错误。
     * @exception NullPointerException 如果 {@code out} 为 {@code null}。
     */
    void encode(OutputStream out) throws IOException;
}
