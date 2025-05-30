/*
 * Copyright (c) 2001, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.charset;


/**
 * 当输入字符（或字节）序列有效但无法映射到输出字节（或字符）序列时抛出的检查异常。
 *
 * @since 1.4
 */

public class UnmappableCharacterException
    extends CharacterCodingException
{

    private static final long serialVersionUID = -7026962371537706123L;

    private int inputLength;

    /**
     * 使用给定的长度构造一个 {@code UnmappableCharacterException}。
     * @param inputLength 输入的长度
     */
    public UnmappableCharacterException(int inputLength) {
        this.inputLength = inputLength;
    }

    /**
     * 返回输入的长度。
     * @return 输入的长度
     */
    public int getInputLength() {
        return inputLength;
    }

    /**
     * 返回消息。
     * @return 消息
     */
    public String getMessage() {
        return "输入长度 = " + inputLength;
    }

}
