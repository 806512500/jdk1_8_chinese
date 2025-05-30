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
 * 一个类型安全的编码错误处理枚举。
 *
 * <p> 该类的实例用于指定如何处理字符集 <a
 * href="CharsetDecoder.html#cae">解码器</a> 和 <a
 * href="CharsetEncoder.html#cae">编码器</a> 中的错误输入和无法映射的字符错误。 </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 专家组
 * @since 1.4
 */

public class CodingErrorAction {

    private String name;

    private CodingErrorAction(String name) {
        this.name = name;
    }

    /**
     * 表示编码错误将通过丢弃错误输入并继续编码操作来处理。
     */
    public static final CodingErrorAction IGNORE
        = new CodingErrorAction("IGNORE");

    /**
     * 表示编码错误将通过丢弃错误输入，将编码器的替换值追加到输出缓冲区，并继续编码操作来处理。
     */
    public static final CodingErrorAction REPLACE
        = new CodingErrorAction("REPLACE");

    /**
     * 表示编码错误将通过返回一个 {@link CoderResult} 对象或抛出一个 {@link
     * CharacterCodingException} 来报告，具体取决于实现编码过程的方法。
     */
    public static final CodingErrorAction REPORT
        = new CodingErrorAction("REPORT");

    /**
     * 返回描述此操作的字符串。
     *
     * @return  描述性字符串
     */
    public String toString() {
        return name;
    }

}
