/*
 * Copyright (c) 2001, 2007, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.charset;


/**
 * 当 {@link CharsetDecoder#decodeLoop decodeLoop} 方法（属于 {@link CharsetDecoder}）或
 * {@link CharsetEncoder#encodeLoop encodeLoop} 方法（属于 {@link CharsetEncoder}）抛出意外异常时，
 * 抛出此错误。
 *
 * @since 1.4
 */

public class CoderMalfunctionError
    extends Error
{

    private static final long serialVersionUID = -1151412348057794301L;

    /**
     * 初始化此类的一个实例。
     *
     * @param  cause
     *         抛出的意外异常
     */
    public CoderMalfunctionError(Exception cause) {
        super(cause);
    }

}
