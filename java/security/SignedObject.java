/*
 * Copyright (c) 1997, 2017, Oracle and/or its affiliates. All rights reserved.
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

/**
 * <p> SignedObject 是一个用于创建真实运行时对象的类，这些对象的完整性不能被篡改而不被检测到。
 *
 * <p> 更具体地说，SignedObject 包含另一个可序列化的对象，即（待签名的）对象及其签名。
 *
 * <p> 签名对象是原始对象的“深度副本”（以序列化形式）。一旦副本创建，对原始对象的进一步操作不会影响副本。
 *
 * <p> 底层签名算法由传递给构造函数和 {@code verify} 方法的 Signature 对象指定。一个典型的签名用法如下：
 *
 * <pre>{@code
 * Signature signingEngine = Signature.getInstance(algorithm,
 *                                                 provider);
 * SignedObject so = new SignedObject(myobject, signingKey,
 *                                    signingEngine);
 * }</pre>
 *
 * <p> 一个典型的验证用法如下（已收到 SignedObject {@code so}）：
 *
 * <pre>{@code
 * Signature verificationEngine =
 *     Signature.getInstance(algorithm, provider);
 * if (so.verify(publickey, verificationEngine))
 *     try {
 *         Object myobj = so.getObject();
 *     } catch (java.lang.ClassNotFoundException e) {};
 * }</pre>
 *
 * <p> 有几个要点值得注意。首先，无需初始化签名或验证引擎，因为它们将在构造函数和 {@code verify} 方法中重新初始化。其次，要使验证成功，指定的公钥必须是用于生成 SignedObject 的私钥对应的公钥。
 *
 * <p> 更重要的是，为了灵活性，构造函数和 {@code verify} 方法允许使用自定义的签名引擎，这些引擎可以实现未正式安装为加密提供程序一部分的签名算法。然而，编写验证器代码的程序员必须意识到正在使用哪个 {@code Signature} 引擎，因为其自身的 {@code verify} 方法实现将被调用来验证签名。换句话说，恶意的 {@code Signature} 可能会选择在验证时始终返回 true，以试图绕过安全检查。
 *
 * <p> 签名算法可以是 NIST 标准 DSA，使用 DSA 和 SHA-256。算法的指定方式与签名的指定方式相同。例如，可以将使用 SHA-256 消息摘要算法的 DSA 算法指定为 "SHA256withDSA"。对于 RSA，签名算法可以指定为 "SHA256withRSA"。必须指定算法名称，因为没有默认值。
 *
 * <p> 加密包提供程序的名称也由构造函数和 {@code verify} 方法的 Signature 参数指定。如果未指定提供程序，则使用默认提供程序。每个安装都可以配置为使用特定的提供程序作为默认值。
 *
 * <p> SignedObject 的潜在应用包括：
 * <ul>
 * <li> 它可以用于任何 Java 运行时内部，作为不可伪造的授权令牌——可以传递而无需担心令牌会被恶意修改而不被检测到。
 * <li> 它可以用于签名和序列化数据/对象以存储在 Java 运行时之外（例如，将关键访问控制数据存储在磁盘上）。
 * <li> 嵌套的 SignedObjects 可以用于构建逻辑签名序列，类似于授权和委托链。
 * </ul>
 *
 * @see Signature
 *
 * @author Li Gong
 */

public final class SignedObject implements Serializable {

    private static final long serialVersionUID = 720502720485447167L;

    /*
     * 原始内容以序列化格式“深度复制”并存储在字节数组中。签名字段也以字节数组的形式存在。
     */

    private byte[] content;
    private byte[] signature;
    private String thealgorithm;

    /**
     * 从任何可序列化的对象构造 SignedObject。
     * 使用指定的签名引擎和签名密钥对给定对象进行签名。
     *
     * @param object 要签名的对象。
     * @param signingKey 用于签名的私钥。
     * @param signingEngine 签名签名引擎。
     *
     * @exception IOException 如果在序列化过程中发生错误
     * @exception InvalidKeyException 如果密钥无效。
     * @exception SignatureException 如果签名失败。
     */
    public SignedObject(Serializable object, PrivateKey signingKey,
                        Signature signingEngine)
        throws IOException, InvalidKeyException, SignatureException {
            // 创建从 a 到 b 的流管道
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            ObjectOutput a = new ObjectOutputStream(b);

            // 将对象内容写入并刷新到字节数组
            a.writeObject(object);
            a.flush();
            a.close();
            this.content = b.toByteArray();
            b.close();

            // 现在对封装的对象进行签名
            this.sign(signingKey, signingEngine);
    }

    /**
     * 检索封装的对象。
     * 在返回之前对封装的对象进行反序列化。
     *
     * @return 封装的对象。
     *
     * @exception IOException 如果在反序列化过程中发生错误
     * @exception ClassNotFoundException 如果在反序列化过程中发生错误
     */
    public Object getObject()
        throws IOException, ClassNotFoundException
    {
        // 创建从 b 到 a 的流管道
        ByteArrayInputStream b = new ByteArrayInputStream(this.content);
        ObjectInput a = new ObjectInputStream(b);
        Object obj = a.readObject();
        b.close();
        a.close();
        return obj;
    }

    /**
     * 以字节数组的形式检索签名对象的签名。
     *
     * @return 签名。每次调用此方法时返回一个新的数组。
     */
    public byte[] getSignature() {
        return this.signature.clone();
    }

    /**
     * 检索签名算法的名称。
     *
     * @return 签名算法名称。
     */
    public String getAlgorithm() {
        return this.thealgorithm;
    }

    /**
     * 验证此 SignedObject 中的签名是否是存储在其中的对象的有效签名，使用指定的验证密钥和验证引擎。
     *
     * @param verificationKey 用于验证的公钥。
     * @param verificationEngine 签名验证引擎。
     *
     * @exception SignatureException 如果签名验证失败。
     * @exception InvalidKeyException 如果验证密钥无效。
     *
     * @return {@code true} 如果签名有效，{@code false} 否则
     */
    public boolean verify(PublicKey verificationKey,
                          Signature verificationEngine)
         throws InvalidKeyException, SignatureException {
             verificationEngine.initVerify(verificationKey);
             verificationEngine.update(this.content.clone());
             return verificationEngine.verify(this.signature.clone());
    }

    /*
     * 使用指定的签名密钥和签名引擎对封装的对象进行签名。
     *
     * @param signingKey 用于签名的私钥。
     * @param signingEngine 签名签名引擎。
     *
     * @exception InvalidKeyException 如果密钥无效。
     * @exception SignatureException 如果签名失败。
     */
    private void sign(PrivateKey signingKey, Signature signingEngine)
        throws InvalidKeyException, SignatureException {
            // 初始化签名引擎
            signingEngine.initSign(signingKey);
            signingEngine.update(this.content.clone());
            this.signature = signingEngine.sign().clone();
            this.thealgorithm = signingEngine.getAlgorithm();
    }

    /**
     * readObject 用于从流中恢复 SignedObject 的状态。
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
            java.io.ObjectInputStream.GetField fields = s.readFields();
            content = ((byte[])fields.get("content", null)).clone();
            signature = ((byte[])fields.get("signature", null)).clone();
            thealgorithm = (String)fields.get("thealgorithm", null);
    }
}
