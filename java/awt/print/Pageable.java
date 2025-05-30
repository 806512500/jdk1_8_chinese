/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.print;

import java.lang.annotation.Native;

/**
 * <code>Pageable</code> 实现表示一组要打印的页面。 <code>Pageable</code> 对象返回该组中的总页数以及指定页面的
 * {@link PageFormat} 和 {@link Printable}。
 * @see java.awt.print.PageFormat
 * @see java.awt.print.Printable
 */
public interface Pageable {

    /**
     * 如果 <code>Pageable</code> 实现不知道其组中的页数，则从
     * {@link #getNumberOfPages() getNumberOfPages}
     * 方法返回此常量。
     */
    @Native int UNKNOWN_NUMBER_OF_PAGES = -1;

    /**
     * 返回组中的页数。
     * 为了启用高级打印功能，
     * 建议 <code>Pageable</code>
     * 实现返回实际的页数
     * 而不是
     * UNKNOWN_NUMBER_OF_PAGES 常量。
     * @return 此 <code>Pageable</code> 中的页数。
     */
    int getNumberOfPages();

    /**
     * 返回由 <code>pageIndex</code> 指定的页面的 <code>PageFormat</code>。
     * @param pageIndex 请求的页面的零基索引
     * @return 描述大小和方向的 <code>PageFormat</code>。
     * @throws IndexOutOfBoundsException 如果
     *          <code>Pageable</code> 不包含请求的页面。
     */
    PageFormat getPageFormat(int pageIndex)
        throws IndexOutOfBoundsException;

    /**
     * 返回负责渲染由 <code>pageIndex</code> 指定的页面的 <code>Printable</code> 实例。
     * @param pageIndex 请求的页面的零基索引
     * @return 渲染页面的 <code>Printable</code>。
     * @throws IndexOutOfBoundsException 如果
     *            <code>Pageable</code> 不包含请求的页面。
     */
    Printable getPrintable(int pageIndex)
        throws IndexOutOfBoundsException;
}
