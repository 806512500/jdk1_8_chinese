/*
 * Copyright (c) 1996, 1999, Oracle and/or its affiliates. All rights reserved.
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

package java.awt;

/**
 * <code>Stroke</code> 接口允许 {@link Graphics2D} 对象获取一个 {@link Shape}，该 {@link Shape} 是指定 <code>Shape</code> 的装饰轮廓或风格化的轮廓表示。
 * 用标记笔描绘 <code>Shape</code> 的轮廓类似于用适当大小和形状的笔描边。
 * 笔放置墨水的区域就是轮廓 <code>Shape</code> 包围的区域。
 * <p>
 * 使用 <code>Stroke</code> 对象返回的轮廓 <code>Shape</code> 的 <code>Graphics2D</code> 接口方法包括 <code>draw</code> 和其他基于该方法实现的方法，如
 * <code>drawLine</code>、<code>drawRect</code>、
 * <code>drawRoundRect</code>、<code>drawOval</code>、
 * <code>drawArc</code>、<code>drawPolyline</code>、
 * 和 <code>drawPolygon</code>。
 * <p>
 * 实现 <code>Stroke</code> 的类的对象必须是只读的，因为 <code>Graphics2D</code> 在将这些对象设置为属性时不会克隆这些对象，或者在 <code>Graphics2D</code> 对象本身被克隆时也不会克隆这些对象。
 * 如果在 <code>Graphics2D</code> 上下文中设置 <code>Stroke</code> 对象后对其进行修改，则后续渲染的行为将无法定义。
 * @see BasicStroke
 * @see Graphics2D#setStroke
 */
public interface Stroke {
    /**
     * 返回一个轮廓 <code>Shape</code>，该轮廓 <code>Shape</code> 包围了根据实现 <code>Stroke</code> 接口的对象定义的规则进行描边时应绘制的区域。
     * @param p 一个要描边的 <code>Shape</code>
     * @return 描边的轮廓 <code>Shape</code>。
     */
    Shape createStrokedShape (Shape p);
}
