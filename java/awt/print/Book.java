/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Vector;

/**
 * <code>Book</code> 类提供了一个文档的表示，其中页面可以有不同的页面格式和页面绘制器。此
 * 类使用 {@link Pageable} 接口与 {@link PrinterJob} 进行交互。
 * @see Pageable
 * @see PrinterJob
*/

public class Book implements Pageable {

 /* Class Constants */

 /* Class Variables */

 /* Instance Variables */

    /**
     * 组成此书的页面集合。
     */
    private Vector mPages;

 /* Instance Methods */

    /**
     * 创建一个新的空 <code>Book</code>。
     */
    public Book() {
        mPages = new Vector();
    }

    /**
     * 返回此 <code>Book</code> 中的页面数。
     * @return 此 <code>Book</code> 包含的页面数。
     */
    public int getNumberOfPages(){
        return mPages.size();
    }

    /**
     * 返回由 <code>pageIndex</code> 指定的页面的 {@link PageFormat}。
     * @param pageIndex 请求其 <code>PageFormat</code> 的页面的零基索引
     * @return 描述页面大小和方向的 <code>PageFormat</code>。
     * @throws IndexOutOfBoundsException 如果 <code>Pageable</code> 不包含请求的页面
     */
    public PageFormat getPageFormat(int pageIndex)
        throws IndexOutOfBoundsException
    {
        return getPage(pageIndex).getPageFormat();
    }

    /**
     * 返回负责渲染由 <code>pageIndex</code> 指定的页面的 {@link Printable} 实例。
     * @param pageIndex 请求其 <code>Printable</code> 的页面的零基索引
     * @return 渲染页面的 <code>Printable</code>。
     * @throws IndexOutOfBoundsException 如果 <code>Pageable</code> 不包含请求的页面
     */
    public Printable getPrintable(int pageIndex)
        throws IndexOutOfBoundsException
    {
        return getPage(pageIndex).getPrintable();
    }

    /**
     * 为指定的页面编号设置 <code>PageFormat</code> 和 <code>Painter</code>。
     * @param pageIndex 要更改其绘制器和格式的页面的零基索引
     * @param painter   渲染页面的 <code>Printable</code> 实例
     * @param page      页面的大小和方向
     * @throws IndexOutOfBoundsException 如果指定的页面不在此 <code>Book</code> 中
     * @throws NullPointerException 如果 <code>painter</code> 或 <code>page</code> 参数为 <code>null</code>
     */
    public void setPage(int pageIndex, Printable painter, PageFormat page)
        throws IndexOutOfBoundsException
    {
        if (painter == null) {
            throw new NullPointerException("painter is null");
        }

        if (page == null) {
            throw new NullPointerException("page is null");
        }

        mPages.setElementAt(new BookPage(painter, page), pageIndex);
    }

    /**
     * 在此 <code>Book</code> 的末尾追加一个页面。
     * @param painter   渲染页面的 <code>Printable</code> 实例
     * @param page      页面的大小和方向
     * @throws NullPointerException
     *          如果 <code>painter</code> 或 <code>page</code> 参数为 <code>null</code>
     */
    public void append(Printable painter, PageFormat page) {
        mPages.addElement(new BookPage(painter, page));
    }

    /**
     * 在此 <code>Book</code> 的末尾追加 <code>numPages</code> 个页面。每个页面都与
     * <code>page</code> 关联。
     * @param painter   渲染页面的 <code>Printable</code> 实例
     * @param page      页面的大小和方向
     * @param numPages  要添加到此 <code>Book</code> 的页面数。
     * @throws NullPointerException
     *          如果 <code>painter</code> 或 <code>page</code> 参数为 <code>null</code>
     */
    public void append(Printable painter, PageFormat page, int numPages) {
        BookPage bookPage = new BookPage(painter, page);
        int pageIndex = mPages.size();
        int newSize = pageIndex + numPages;

        mPages.setSize(newSize);
        for(int i = pageIndex; i < newSize; i++){
            mPages.setElementAt(bookPage, i);
        }
    }

    /**
     * 返回由 'pageIndex' 指定的页面的 BookPage。
     */
    private BookPage getPage(int pageIndex)
        throws ArrayIndexOutOfBoundsException
    {
        return (BookPage) mPages.elementAt(pageIndex);
    }

    /**
     * BookPage 内部类通过 PageFormat-Printable 对描述书中的一个页面。
     */
    private class BookPage {
        /**
         * 页面的大小和方向。
         */
        private PageFormat mFormat;

        /**
         * 绘制页面的实例。
         */
        private Printable mPainter;

        /**
         * 新实例，其中 'format' 描述页面的大小和方向，'painter' 是绘制页面图形的实例。
         * @throws  NullPointerException
         *          如果 <code>painter</code> 或 <code>format</code> 参数为 <code>null</code>
         */
        BookPage(Printable painter, PageFormat format) {

            if (painter == null || format == null) {
                throw new NullPointerException();
            }

            mFormat = format;
            mPainter = painter;
        }

        /**
         * 返回绘制页面的实例。
         */
        Printable getPrintable() {
            return mPainter;
        }

        /**
         * 返回页面的格式。
         */
        PageFormat getPageFormat() {
            return mFormat;
        }
    }
}
