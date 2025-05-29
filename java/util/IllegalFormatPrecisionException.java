/*
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 当精度是一个负值且不为 <tt>-1</tt>，转换不支持精度，或值不被支持时抛出的未检查异常。
 *
 * @since 1.5
 */
public class IllegalFormatPrecisionException extends IllegalFormatException {

    private static final long serialVersionUID = 18711008L;

    private int p;

    /**
     * 使用指定的精度构造此类的一个实例。
     *
     * @param  p
     *         精度
     */
    public IllegalFormatPrecisionException(int p) {
        this.p = p;
    }

    /**
     * 返回精度
     *
     * @return  精度
     */
    public int getPrecision() {
        return p;
    }

    public String getMessage() {
        return Integer.toString(p);
    }
}
