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

import java.awt.Graphics;


/**
 * <code>Printable</code> 接口由当前页面绘制方法实现，这些方法由打印系统调用以渲染页面。在构建
 * {@link Pageable} 时，使用 {@link PageFormat} 实例和实现此接口的实例的对来描述每一页。实现
 * <code>Printable</code> 的实例用于打印页面的图形。
 * <p>
 * 可以将 <code>Printable(..)</code> 设置到 <code>PrinterJob</code> 上。当客户端随后通过调用
 * <code>PrinterJob.print(..)</code> 启动打印时，控制权将交给打印系统，直到所有页面都被打印完毕。
 * 它通过调用 <code>Printable.print(..)</code> 直到文档中的所有页面都被打印出来。
 * 使用 <code>Printable</code> 接口时，打印系统承诺在打印系统请求时随时呈现页面的内容。
 * <p>
 * <code>Printable.print(..)</code> 的参数包括一个描述页面可打印区域的 <code>PageFormat</code>，
 * 用于计算将适合页面的内容，以及页面索引，指定请求页面的零基打印流索引。
 * <p>
 * 为了确保正确的打印行为，应观察以下几点：
 * <ul>
 * <li> 打印系统可能会多次请求相同的页面索引。每次请求时，提供的 PageFormat 参数将相同。
 *
 * <li> 打印系统将以单调递增的方式调用 <code>Printable.print(..)</code>，尽管如上所述，
 * <code>Printable</code> 应该预期到可能会多次调用某个页面索引，并且当客户端或用户通过打印对话框指定页面范围时，
 * 可能会跳过某些页面索引。
 *
 * <li> 如果请求了多个装订副本的文档，并且打印机本身不支持此功能，则文档可能会被多次成像。打印将从最低的打印流页面索引开始每个副本。
 *
 * <li> 除了为多个装订副本重新成像整个文档外，递增的页面索引顺序意味着当请求页面 N 时，如果客户端需要计算页面断点位置，
 * 它可以安全地丢弃与页面 &lt; N 相关的任何状态，并使当前状态为页面 N。"状态"通常只是文档中对应页面起始位置的计算值。
 *
 * <li> 当被打印系统调用时，<code>Printable</code> 必须检查并遵守提供的 PageFormat 参数以及页面索引。
 * 要绘制的页面的格式由提供的 PageFormat 指定。因此，页面的大小、方向和可打印区域已经确定，渲染必须在该可打印区域内进行。
 * 这是正确打印行为的关键，并意味着客户端有责任跟踪指定页面上的内容。
 *
 * <li> 当 <code>Printable</code> 从客户端提供的 <code>Pageable</code> 中获取时，客户端可以为每个页面索引提供不同的 PageFormat。
 * 计算页面断点时必须考虑这一点。
 * </ul>
 * <p>
 * @see java.awt.print.Pageable
 * @see java.awt.print.PageFormat
 * @see java.awt.print.PrinterJob
 */
public interface Printable {

    /**
     * 从 {@link #print(Graphics, PageFormat, int)} 返回，表示请求的页面已渲染。
     */
    int PAGE_EXISTS = 0;

    /**
     * 从 <code>print</code> 返回，表示 <code>pageIndex</code> 太大，请求的页面不存在。
     */
    int NO_SUCH_PAGE = 1;

    /**
     * 将指定索引的页面打印到指定的 {@link Graphics} 上下文中，以指定的格式。<code>PrinterJob</code>
     * 调用 <code>Printable</code> 接口请求将页面渲染到由 <code>graphics</code> 指定的上下文中。
     * 要绘制的页面的格式由 <code>pageFormat</code> 指定。请求的页面的零基索引由 <code>pageIndex</code> 指定。
     * 如果请求的页面不存在，则此方法返回 NO_SUCH_PAGE；否则返回 PAGE_EXISTS。
     * <code>Graphics</code> 类或子类实现了 {@link PrinterGraphics} 接口以提供额外的信息。
     * 如果 <code>Printable</code> 对象终止打印作业，则抛出 {@link PrinterException}。
     * @param graphics 要绘制页面的上下文
     * @param pageFormat 要绘制页面的大小和方向
     * @param pageIndex 要绘制页面的零基索引
     * @return 如果页面成功渲染则返回 PAGE_EXISTS，如果 <code>pageIndex</code> 指定了不存在的页面，则返回 NO_SUCH_PAGE。
     * @exception java.awt.print.PrinterException
     *         当打印作业终止时抛出。
     */
    int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
                 throws PrinterException;

}
