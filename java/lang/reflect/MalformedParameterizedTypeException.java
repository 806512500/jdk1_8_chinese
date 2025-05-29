/*
 * Copyright (c) 2003, 2008, Oracle and/or its affiliates. All rights reserved.
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


package java.lang.reflect;

/**
 * 当反射方法需要实例化一个语义上不正确的参数化类型时抛出此异常。
 * 例如，如果参数化类型的类型参数数量不正确。
 *
 * @since 1.5
 */
public class MalformedParameterizedTypeException extends RuntimeException {
    private static final long serialVersionUID = -5696557788586220964L;
}
