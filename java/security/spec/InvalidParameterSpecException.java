/*
 * 版权所有 (c) 1997, 2003, Oracle 和/或其附属公司。保留所有权利。
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

package java.security.spec;

import java.security.GeneralSecurityException;

/**
 * 这是无效参数规范的异常。
 *
 * @author Jan Luehe
 *
 *
 * @see java.security.AlgorithmParameters
 * @see AlgorithmParameterSpec
 * @see DSAParameterSpec
 *
 * @since 1.2
 */

public class InvalidParameterSpecException extends GeneralSecurityException {

    private static final long serialVersionUID = -970468769593399342L;

    /**
     * 构造一个没有详细消息的 InvalidParameterSpecException。详细消息是一个描述此特定
     * 异常的字符串。
     */
    public InvalidParameterSpecException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 InvalidParameterSpecException。详细消息是一个描述此特定
     * 异常的字符串。
     *
     * @param msg 详细消息。
     */
    public InvalidParameterSpecException(String msg) {
        super(msg);
    }
}
