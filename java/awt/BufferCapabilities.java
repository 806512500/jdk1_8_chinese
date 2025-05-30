/*
 * Copyright (c) 2000, 2011, Oracle and/or its affiliates. All rights reserved.
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
 * 缓冲区的功能和属性。
 *
 * @see java.awt.image.BufferStrategy#getCapabilities()
 * @see GraphicsConfiguration#getBufferCapabilities
 * @author Michael Martak
 * @since 1.4
 */
public class BufferCapabilities implements Cloneable {

    private ImageCapabilities frontCaps;
    private ImageCapabilities backCaps;
    private FlipContents flipContents;

    /**
     * 创建一个新的对象来指定缓冲区功能。
     * @param frontCaps 前缓冲区的功能；不能为 <code>null</code>
     * @param backCaps 后缓冲区和中间缓冲区的功能；不能为 <code>null</code>
     * @param flipContents 页翻转后后缓冲区的内容，如果未使用页翻转（意味着使用位块传输）则为 <code>null</code>
     * @exception IllegalArgumentException 如果 frontCaps 或 backCaps 为 <code>null</code>
     */
    public BufferCapabilities(ImageCapabilities frontCaps,
        ImageCapabilities backCaps, FlipContents flipContents) {
        if (frontCaps == null || backCaps == null) {
            throw new IllegalArgumentException(
                "指定的图像功能不能为空");
        }
        this.frontCaps = frontCaps;
        this.backCaps = backCaps;
        this.flipContents = flipContents;
    }

    /**
     * @return 前（显示）缓冲区的图像功能
     */
    public ImageCapabilities getFrontBufferCapabilities() {
        return frontCaps;
    }

    /**
     * @return 所有后缓冲区的图像功能（中间缓冲区也被视为后缓冲区）
     */
    public ImageCapabilities getBackBufferCapabilities() {
        return backCaps;
    }

    /**
     * @return 缓冲区策略是否使用页翻转；使用页翻转的缓冲区集可以在前缓冲区和一个或多个后缓冲区之间通过切换视频指针（或内部复制内存）来交换内容。不使用页翻转的缓冲区集使用位块传输将内容从一个缓冲区复制到另一个缓冲区；在这种情况下，<code>getFlipContents</code> 返回 <code>null</code>
     */
    public boolean isPageFlipping() {
        return (getFlipContents() != null);
    }

    /**
     * @return 页翻转后后缓冲区的内容。当 <code>isPageFlipping</code> 返回 <code>false</code> 时，这意味着位块传输，返回值为 <code>null</code>。它可以是 <code>FlipContents.UNDEFINED</code>（默认假设值）、<code>FlipContents.BACKGROUND</code>、<code>FlipContents.PRIOR</code> 或 <code>FlipContents.COPIED</code>。
     * @see #isPageFlipping
     * @see FlipContents#UNDEFINED
     * @see FlipContents#BACKGROUND
     * @see FlipContents#PRIOR
     * @see FlipContents#COPIED
     */
    public FlipContents getFlipContents() {
        return flipContents;
    }

    /**
     * @return 页翻转是否仅在全屏模式下可用。如果为 <code>true</code>，则需要全屏独占模式才能进行页翻转。
     * @see #isPageFlipping
     * @see GraphicsDevice#setFullScreenWindow
     */
    public boolean isFullScreenRequired() {
        return false;
    }

    /**
     * @return 是否可以使用两个以上的缓冲区（一个或多个中间缓冲区以及前缓冲区和后缓冲区）进行页翻转。
     * @see #isPageFlipping
     */
    public boolean isMultiBufferAvailable() {
        return false;
    }

    /**
     * @return 此 BufferCapabilities 对象的副本。
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // 由于我们实现了 Cloneable，这种情况不应该发生
            throw new InternalError(e);
        }
    }

    // 内部类 FlipContents
    /**
     * 页翻转后可能的后缓冲区内容的类型安全枚举。
     * @since 1.4
     */
    public static final class FlipContents extends AttributeValue {

        private static int I_UNDEFINED = 0;
        private static int I_BACKGROUND = 1;
        private static int I_PRIOR = 2;
        private static int I_COPIED = 3;

        private static final String NAMES[] =
            { "undefined", "background", "prior", "copied" };

        /**
         * 当翻转内容为 <code>UNDEFINED</code> 时，翻转后后缓冲区的内容是未定义的。
         * @see #isPageFlipping
         * @see #getFlipContents
         * @see #BACKGROUND
         * @see #PRIOR
         * @see #COPIED
         */
        public static final FlipContents UNDEFINED =
            new FlipContents(I_UNDEFINED);

        /**
         * 当翻转内容为 <code>BACKGROUND</code> 时，翻转后后缓冲区的内容被清除为背景色。
         * @see #isPageFlipping
         * @see #getFlipContents
         * @see #UNDEFINED
         * @see #PRIOR
         * @see #COPIED
         */
        public static final FlipContents BACKGROUND =
            new FlipContents(I_BACKGROUND);

        /**
         * 当翻转内容为 <code>PRIOR</code> 时，翻转后后缓冲区的内容是前缓冲区的先前内容（真正的页翻转）。
         * @see #isPageFlipping
         * @see #getFlipContents
         * @see #UNDEFINED
         * @see #BACKGROUND
         * @see #COPIED
         */
        public static final FlipContents PRIOR =
            new FlipContents(I_PRIOR);

        /**
         * 当翻转内容为 <code>COPIED</code> 时，翻转后后缓冲区的内容被复制到前缓冲区。
         * @see #isPageFlipping
         * @see #getFlipContents
         * @see #UNDEFINED
         * @see #BACKGROUND
         * @see #PRIOR
         */
        public static final FlipContents COPIED =
            new FlipContents(I_COPIED);

        private FlipContents(int type) {
            super(type, NAMES);
        }

    } // 内部类 FlipContents

}
