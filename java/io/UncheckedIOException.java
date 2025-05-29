/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
package java.io;

import java.util.Objects;

/**
 * 将一个 {@link IOException} 包装成一个未检查的异常。
 *
 * @since   1.8
 */
public class UncheckedIOException extends RuntimeException {
    private static final long serialVersionUID = -8134305061645241065L;

    /**
     * 构造此类的一个实例。
     *
     * @param   message
     *          详细消息，可以为 null
     * @param   cause
     *          {@code IOException}
     *
     * @throws  NullPointerException
     *          如果原因（cause）为 {@code null}
     */
    public UncheckedIOException(String message, IOException cause) {
        super(message, Objects.requireNonNull(cause));
    }

    /**
     * 构造此类的一个实例。
     *
     * @param   cause
     *          {@code IOException}
     *
     * @throws  NullPointerException
     *          如果原因（cause）为 {@code null}
     */
    public UncheckedIOException(IOException cause) {
        super(Objects.requireNonNull(cause));
    }

    /**
     * 返回此异常的原因。
     *
     * @return  作为此异常原因的 {@code IOException}。
     */
    @Override
    public IOException getCause() {
        return (IOException) super.getCause();
    }

    /**
     * 从流中读取对象时调用。
     *
     * @throws  InvalidObjectException
     *          如果对象无效或原因（cause）不是
     *          一个 {@code IOException}
     */
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException
    {
        s.defaultReadObject();
        Throwable cause = super.getCause();
        if (!(cause instanceof IOException))
            throw new InvalidObjectException("Cause must be an IOException");
    }
}
