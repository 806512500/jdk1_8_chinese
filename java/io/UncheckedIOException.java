/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
package java.io;

import java.util.Objects;

/**
 * 包装一个 {@link IOException} 为一个未检查的异常。
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
     *          如果原因为空
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
     *          如果原因为空
     */
    public UncheckedIOException(IOException cause) {
        super(Objects.requireNonNull(cause));
    }

    /**
     * 返回此异常的原因。
     *
     * @return  该异常的原因，即 {@code IOException}。
     */
    @Override
    public IOException getCause() {
        return (IOException) super.getCause();
    }

    /**
     * 从流中读取对象时调用。
     *
     * @throws  InvalidObjectException
     *          如果对象无效或其原因不是 {@code IOException}
     */
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException
    {
        s.defaultReadObject();
        Throwable cause = super.getCause();
        if (!(cause instanceof IOException))
            throw new InvalidObjectException("原因必须是 IOException");
    }
}
