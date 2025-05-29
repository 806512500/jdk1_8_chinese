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
 * 当格式说明符中提供重复标志时抛出的未检查异常。
 *
 * <p> 除非另有说明，否则将 <tt>null</tt> 参数传递给此类中的任何方法或构造函数将导致抛出 {@link
 * NullPointerException}。
 *
 * @since 1.5
 */
public class DuplicateFormatFlagsException extends IllegalFormatException {

    private static final long serialVersionUID = 18890531L;

    private String flags;

    /**
     * 使用指定的标志构造此类的实例。
     *
     * @param  f
     *         包含重复标志的格式标志集。
     */
    public DuplicateFormatFlagsException(String f) {
        if (f == null)
            throw new NullPointerException();
        this.flags = f;
    }

    /**
     * 返回包含重复标志的标志集。
     *
     * @return  标志
     */
    public String getFlags() {
        return flags;
    }

    public String getMessage() {
        return String.format("Flags = '%s'", flags);
    }
}
