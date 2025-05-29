/*
 * Copyright (c) 2003, 2012, Oracle and/or its affiliates. All rights reserved.
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

package java.util;

import java.io.NotSerializableException;
import java.io.IOException;

/**
 * 抛出以表示操作无法完成，因为输入不符合 {@link Properties} 规范中规定的属性集合的适当 XML 文档类型。<p>
 *
 * 注意，尽管 InvalidPropertiesFormatException 继承了 Exception 的 Serializable 接口，但它不打算被序列化。适当的序列化方法会抛出 NotSerializableException。
 *
 * @see     Properties
 * @since   1.5
 * @serial exclude
 */

public class InvalidPropertiesFormatException extends IOException {

    private static final long serialVersionUID = 7763056076009360219L;

    /**
     * 使用指定的原因构造一个 InvalidPropertiesFormatException。
     *
     * @param  cause 原因（通过 {@link Throwable#getCause()} 方法稍后检索）。
     */
    public InvalidPropertiesFormatException(Throwable cause) {
        super(cause==null ? null : cause.toString());
        this.initCause(cause);
    }

   /**
    * 使用指定的详细消息构造一个 InvalidPropertiesFormatException。
    *
    * @param   message   详细消息。详细消息稍后通过 {@link Throwable#getMessage()} 方法检索。
    */
    public InvalidPropertiesFormatException(String message) {
        super(message);
    }

    /**
     * 抛出 NotSerializableException，因为 InvalidPropertiesFormatException 对象不打算被序列化。
     */
    private void writeObject(java.io.ObjectOutputStream out)
        throws NotSerializableException
    {
        throw new NotSerializableException("Not serializable.");
    }

    /**
     * 抛出 NotSerializableException，因为 InvalidPropertiesFormatException 对象不打算被序列化。
     */
    private void readObject(java.io.ObjectInputStream in)
        throws NotSerializableException
    {
        throw new NotSerializableException("Not serializable.");
    }

}
