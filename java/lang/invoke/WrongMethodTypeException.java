/*
 * Copyright (c) 2008, 2012, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.invoke;

/**
 * 当代码尝试通过错误的方法类型调用方法句柄时抛出此异常。与常规 Java 方法调用的字节码表示一样，方法句柄调用在调用点处与特定的类型描述符强类型关联。
 * <p>
 * 当两个方法句柄组合时，如果系统检测到它们的类型无法正确匹配，也可能抛出此异常。这相当于在方法句柄构建时对类型不匹配进行早期评估，
 * 而不是在调用不匹配的方法句柄时进行评估。
 *
 * @author John Rose, JSR 292 EG
 * @since 1.7
 */
public class WrongMethodTypeException extends RuntimeException {
    private static final long serialVersionUID = 292L;

    /**
     * 构造一个没有详细消息的 {@code WrongMethodTypeException}。
     */
    public WrongMethodTypeException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 {@code WrongMethodTypeException}。
     *
     * @param s 详细消息。
     */
    public WrongMethodTypeException(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和原因构造一个 {@code WrongMethodTypeException}。
     *
     * @param s 详细消息。
     * @param cause 异常的原因，或 null。
     */
    //FIXME: 在 MR1 中将其公开
    /*non-public*/ WrongMethodTypeException(String s, Throwable cause) {
        super(s, cause);
    }

    /**
     * 使用指定的原因构造一个 {@code WrongMethodTypeException}。
     *
     * @param cause 异常的原因，或 null。
     */
    //FIXME: 在 MR1 中将其公开
    /*non-public*/ WrongMethodTypeException(Throwable cause) {
        super(cause);
    }
}
