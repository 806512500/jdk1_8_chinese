/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.InvalidObjectException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.GeneralSecurityException;

/**
 * 表示在验证证书路径时遇到的各种问题的异常。
 * <p>
 * {@code CertPathValidatorException} 提供了对异常的包装支持。{@link #getCause getCause} 方法返回导致此异常被抛出的可抛出对象，如果有。
 * <p>
 * {@code CertPathValidatorException} 还可能包含在抛出异常时正在验证的证书路径、导致异常被抛出的证书在证书路径中的索引，以及导致失败的原因。使用 {@link #getCertPath getCertPath}、{@link #getIndex getIndex} 和 {@link #getReason getReason} 方法检索此信息。
 *
 * <p>
 * <b>并发访问</b>
 * <p>
 * 除非另有说明，否则本类中定义的方法不是线程安全的。多个线程需要并发访问单个对象时，应进行同步并提供必要的锁定。每个线程操作不同对象时，无需同步。
 *
 * @see CertPathValidator
 *
 * @since       1.4
 * @author      Yassir Elley
 */
public class CertPathValidatorException extends GeneralSecurityException {

    private static final long serialVersionUID = -3083180014971893139L;

    /**
     * @serial 导致异常被抛出的证书在证书路径中的索引
     */
    private int index = -1;

    /**
     * @serial 抛出异常时正在验证的证书路径
     */
    private CertPath certPath;

    /**
     * @serial 验证失败的原因
     */
    private Reason reason = BasicReason.UNSPECIFIED;

    /**
     * 创建一个没有详细消息的 {@code CertPathValidatorException}。
     */
    public CertPathValidatorException() {
        this(null, null);
    }

    /**
     * 使用给定的详细消息创建一个 {@code CertPathValidatorException}。详细消息是一个描述此特定异常的 {@code String}。
     *
     * @param msg 详细消息
     */
    public CertPathValidatorException(String msg) {
        this(msg, null);
    }

    /**
     * 创建一个包装指定可抛出对象的 {@code CertPathValidatorException}。这允许将任何异常转换为 {@code CertPathValidatorException}，同时保留有关包装异常的信息，这可能对调试有用。详细消息设置为 ({@code cause==null ? null : cause.toString()})
     * （通常包含 cause 的类和详细消息）。
     *
     * @param cause 通过 {@link #getCause getCause()} 方法稍后检索的原因（允许 {@code null} 值，表示原因不存在或未知）。
     */
    public CertPathValidatorException(Throwable cause) {
        this((cause == null ? null : cause.toString()), cause);
    }

    /**
     * 使用指定的详细消息和原因创建一个 {@code CertPathValidatorException}。
     *
     * @param msg 详细消息
     * @param cause 通过 {@link #getCause getCause()} 方法稍后检索的原因（允许 {@code null} 值，表示原因不存在或未知）。
     */
    public CertPathValidatorException(String msg, Throwable cause) {
        this(msg, cause, null, -1);
    }

    /**
     * 使用指定的详细消息、原因、证书路径和索引创建一个 {@code CertPathValidatorException}。
     *
     * @param msg 详细消息（或 {@code null} 如果没有）
     * @param cause 原因（或 {@code null} 如果没有）
     * @param certPath 遇到错误时正在验证的证书路径
     * @param index 证书在证书路径中的索引（或 -1 如果不适用）。注意，证书路径中的证书列表是基于零的。
     * @throws IndexOutOfBoundsException 如果索引超出范围 {@code (index < -1 || (certPath != null && index >= certPath.getCertificates().size()) }
     * @throws IllegalArgumentException 如果 {@code certPath} 是 {@code null} 且 {@code index} 不是 -1
     */
    public CertPathValidatorException(String msg, Throwable cause,
            CertPath certPath, int index) {
        this(msg, cause, certPath, index, BasicReason.UNSPECIFIED);
    }

    /**
     * 使用指定的详细消息、原因、证书路径、索引和原因创建一个 {@code CertPathValidatorException}。
     *
     * @param msg 详细消息（或 {@code null} 如果没有）
     * @param cause 原因（或 {@code null} 如果没有）
     * @param certPath 遇到错误时正在验证的证书路径
     * @param index 证书在证书路径中的索引（或 -1 如果不适用）。注意，证书路径中的证书列表是基于零的。
     * @param reason 验证失败的原因
     * @throws IndexOutOfBoundsException 如果索引超出范围 {@code (index < -1 || (certPath != null && index >= certPath.getCertificates().size()) }
     * @throws IllegalArgumentException 如果 {@code certPath} 是 {@code null} 且 {@code index} 不是 -1
     * @throws NullPointerException 如果 {@code reason} 是 {@code null}
     *
     * @since 1.7
     */
    public CertPathValidatorException(String msg, Throwable cause,
            CertPath certPath, int index, Reason reason) {
        super(msg, cause);
        if (certPath == null && index != -1) {
            throw new IllegalArgumentException();
        }
        if (index < -1 ||
            (certPath != null && index >= certPath.getCertificates().size())) {
            throw new IndexOutOfBoundsException();
        }
        if (reason == null) {
            throw new NullPointerException("reason can't be null");
        }
        this.certPath = certPath;
        this.index = index;
        this.reason = reason;
    }

    /**
     * 返回抛出异常时正在验证的证书路径。
     *
     * @return 抛出异常时正在验证的 {@code CertPath}（或 {@code null} 如果未指定）
     */
    public CertPath getCertPath() {
        return this.certPath;
    }

    /**
     * 返回导致异常被抛出的证书在证书路径中的索引。注意，证书路径中的证书列表是基于零的。如果没有设置索引，返回 -1。
     *
     * @return 已设置的索引，或如果没有设置则返回 -1
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * 返回验证失败的原因。原因与 {@link #getIndex} 返回的证书索引相关联。
     *
     * @return 验证失败的原因，或如果未指定原因则返回 {@code BasicReason.UNSPECIFIED}
     *
     * @since 1.7
     */
    public Reason getReason() {
        return this.reason;
    }

    private void readObject(ObjectInputStream stream)
        throws ClassNotFoundException, IOException {
        stream.defaultReadObject();
        if (reason == null) {
            reason = BasicReason.UNSPECIFIED;
        }
        if (certPath == null && index != -1) {
            throw new InvalidObjectException("certpath is null and index != -1");
        }
        if (index < -1 ||
            (certPath != null && index >= certPath.getCertificates().size())) {
            throw new InvalidObjectException("index out of range");
        }
    }

    /**
     * 验证算法失败的原因。
     *
     * @since 1.7
     */
    public static interface Reason extends java.io.Serializable { }


    /**
     * BasicReason 枚举了任何类型证书路径可能无效的潜在原因。
     *
     * @since 1.7
     */
    public static enum BasicReason implements Reason {
        /**
         * 未指定原因。
         */
        UNSPECIFIED,

        /**
         * 证书已过期。
         */
        EXPIRED,

        /**
         * 证书尚未生效。
         */
        NOT_YET_VALID,

        /**
         * 证书已被撤销。
         */
        REVOKED,

        /**
         * 无法确定证书的撤销状态。
         */
        UNDETERMINED_REVOCATION_STATUS,

        /**
         * 签名无效。
         */
        INVALID_SIGNATURE,

        /**
         * 公钥或签名算法已被限制。
         */
        ALGORITHM_CONSTRAINED
    }
}
