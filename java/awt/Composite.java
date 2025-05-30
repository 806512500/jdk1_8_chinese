/*
 * Copyright (c) 1997, 1998, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.image.ColorModel;

/**
 * <code>Composite</code> 接口，连同
 * {@link CompositeContext}，定义了将绘制原语与底层图形区域组合的方法。
 * 在 <code>Composite</code> 被设置在
 * {@link Graphics2D} 上下文中后，它会根据预定义的规则，将正在渲染的形状、文本或图像
 * 与已经渲染的颜色组合在一起。实现此接口的类提供了规则和创建特定操作上下文的方法。
 * <code>CompositeContext</code> 是由 <code>Graphics2D</code>
 * 在操作开始前创建的用于组合操作的环境。<code>CompositeContext</code>
 * 包含组合操作所需的所有私有信息和资源。当 <code>CompositeContext</code>
 * 不再需要时，<code>Graphics2D</code> 对象会将其释放以回收为操作分配的资源。
 * <p>
 * 实现 <code>Composite</code> 接口的类的实例必须是不可变的，因为当这些对象作为属性
 * 通过 <code>setComposite</code> 方法设置或 <code>Graphics2D</code>
 * 对象被克隆时，<code>Graphics2D</code> 不会克隆这些对象。这是为了避免由于
 * 在 <code>Graphics2D</code> 上下文中设置后修改 <code>Composite</code> 对象
 * 而导致的 <code>Graphics2D</code> 未定义的渲染行为。
 * <p>
 * 由于此接口必须向可能的任意代码公开目标设备或图像上的像素内容，因此当直接在屏幕设备上渲染时，
 * 使用实现此接口的自定义对象受 <code>readDisplayPixels</code>
 * {@link AWTPermission} 的管理。权限检查将在这样的自定义对象被传递给从
 * {@link Component} 获取的 <code>Graphics2D</code> 的 <code>setComposite</code>
 * 方法时发生。
 * @see AlphaComposite
 * @see CompositeContext
 * @see Graphics2D#setComposite
 */
public interface Composite {

    /**
     * 创建一个包含用于执行组合操作状态的上下文。在多线程环境中，
     * 单个 <code>Composite</code> 对象可以同时存在多个上下文。
     * @param srcColorModel  源的 {@link ColorModel}
     * @param dstColorModel  目标的 <code>ColorModel</code>
     * @param hints 上下文对象用于选择渲染替代方案的提示
     * @return 用于执行组合操作的 <code>CompositeContext</code> 对象。
     */
    public CompositeContext createContext(ColorModel srcColorModel,
                                          ColorModel dstColorModel,
                                          RenderingHints hints);

}
