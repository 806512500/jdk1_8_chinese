/*
 * Copyright (c) 2001, 2007, Oracle and/or its affiliates. All rights reserved.
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
 * 当 {@link CharsetDecoder#decodeLoop decodeLoop} 方法的 {@link CharsetDecoder} 或
 * {@link CharsetEncoder#encodeLoop encodeLoop} 方法的 {@link CharsetEncoder} 抛出意外异常时，
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
