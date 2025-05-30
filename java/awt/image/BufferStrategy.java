/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.image;

import java.awt.BufferCapabilities;
import java.awt.Graphics;
import java.awt.Image;

/**
 * <code>BufferStrategy</code> 类表示在特定的 <code>Canvas</code> 或 <code>Window</code> 上组织复杂内存的机制。硬件和软件限制决定了特定缓冲策略是否可以实现以及如何实现。这些限制可以通过创建 <code>Canvas</code> 或 <code>Window</code> 时使用的 <code>GraphicsConfiguration</code> 的功能来检测。
 * <p>
 * 值得注意的是，术语 <i>缓冲区</i> 和 <i>表面</i> 是同义词：一块连续的内存，无论是在视频设备内存中还是在系统内存中。
 * <p>
 * 有几种复杂的缓冲策略类型，包括顺序环形缓冲和复制缓冲。
 * 顺序环形缓冲（即双缓冲或三缓冲）是最常见的；应用程序绘制到一个单一的 <i>后缓冲区</i>，然后将内容一次性移动到前面（显示）缓冲区，通过复制数据或移动视频指针来实现。
 * 移动视频指针交换缓冲区，使第一个绘制的缓冲区成为 <i>前缓冲区</i>，即当前在设备上显示的内容；这称为 <i>页面翻转</i>。
 * <p>
 * 或者，后缓冲区的内容可以被复制，或 <i>复制</i> 到一个链中，而不是移动视频指针。
 * <pre>{@code
 * 双缓冲：
 *
 *                    ***********         ***********
 *                    *         * ------> *         *
 * [显示] <---- * 前缓冲 *   显示  * 后缓冲 * <---- 渲染
 *                    *         * <------ *         *
 *                    ***********         ***********
 *
 * 三缓冲：
 *
 * [显示] ***********         ***********        ***********
 *       *         * --------+---------+------> *         *
 *    <---- * 前缓冲 *   显示  * 中间缓冲 *        * 后缓冲 * <---- 渲染
 *          *         * <------ *         * <----- *         *
 *          ***********         ***********        ***********
 *
 * }</pre>
 * <p>
 * 以下是一个如何创建和使用缓冲策略的示例：
 * <pre><code>
 *
 * // 检查 GraphicsConfiguration 的功能
 * ...
 *
 * // 创建组件
 * Window w = new Window(gc);
 *
 * // 显示窗口
 * w.setVisible(true);
 *
 * // 创建一个通用的双缓冲策略
 * w.createBufferStrategy(2);
 * BufferStrategy strategy = w.getBufferStrategy();
 *
 * // 主循环
 * while (!done) {
 *     // 准备渲染下一帧
 *     // ...
 *
 *     // 渲染单帧
 *     do {
 *         // 以下循环确保在底层表面被重新创建时，绘图缓冲区的内容是一致的
 *         do {
 *             // 每次循环都获取一个新的图形上下文，以确保策略的有效性
 *             Graphics graphics = strategy.getDrawGraphics();
 *
 *             // 渲染到图形
 *             // ...
 *
 *             // 释放图形
 *             graphics.dispose();
 *
 *             // 如果绘图缓冲区的内容被恢复，则重复渲染
 *         } while (strategy.contentsRestored());
 *
 *         // 显示缓冲区
 *         strategy.show();
 *
 *         // 如果绘图缓冲区丢失，则重复渲染
 *     } while (strategy.contentsLost());
 * }
 *
 * // 释放窗口
 * w.setVisible(false);
 * w.dispose();
 * </code></pre>
 *
 * @see java.awt.Window
 * @see java.awt.Canvas
 * @see java.awt.GraphicsConfiguration
 * @see VolatileImage
 * @author Michael Martak
 * @since 1.4
 */
public abstract class BufferStrategy {

    /**
     * 返回此 <code>BufferStrategy</code> 的 <code>BufferCapabilities</code>。
     *
     * @return 此策略的缓冲功能
     */
    public abstract BufferCapabilities getCapabilities();

    /**
     * 为绘图缓冲区创建一个图形上下文。出于性能原因，此方法可能未同步；多个线程使用此方法应由应用程序级别处理。必须由应用程序处理获取的图形对象的释放。
     *
     * @return 绘图缓冲区的图形上下文
     */
    public abstract Graphics getDrawGraphics();

    /**
     * 返回自上次调用 <code>getDrawGraphics</code> 以来绘图缓冲区是否丢失。由于缓冲策略中的缓冲区通常是 <code>VolatileImage</code> 类型，因此它们可能会丢失。有关丢失缓冲区的讨论，请参见 <code>VolatileImage</code>。
     *
     * @return 自上次调用 <code>getDrawGraphics</code> 以来绘图缓冲区是否丢失
     * @see java.awt.image.VolatileImage
     */
    public abstract boolean contentsLost();

    /**
     * 返回自上次调用 <code>getDrawGraphics</code> 以来绘图缓冲区是否从丢失状态恢复并重新初始化为默认背景色（白色）。由于缓冲策略中的缓冲区通常是 <code>VolatileImage</code> 类型，因此它们可能会丢失。如果自上次调用 <code>getDrawGraphics</code> 以来表面已从丢失状态恢复，则可能需要重新绘制。
     * 有关丢失缓冲区的讨论，请参见 <code>VolatileImage</code>。
     *
     * @return 自上次调用 <code>getDrawGraphics</code> 以来绘图缓冲区是否恢复
     * @see java.awt.image.VolatileImage
     */
    public abstract boolean contentsRestored();

    /**
     * 通过复制内存（复制）或更改显示指针（翻转）使下一个可用缓冲区可见。
     */
    public abstract void show();

    /**
     * 释放此 <code>BufferStrategy</code> 当前消耗的系统资源，并从关联的组件中移除。调用此方法后，<code>getBufferStrategy</code> 将返回 null。尝试在释放后使用 <code>BufferStrategy</code> 将导致未定义的行为。
     *
     * @see java.awt.Window#createBufferStrategy
     * @see java.awt.Canvas#createBufferStrategy
     * @see java.awt.Window#getBufferStrategy
     * @see java.awt.Canvas#getBufferStrategy
     * @since 1.6
     */
    public void dispose() {
    }
}
