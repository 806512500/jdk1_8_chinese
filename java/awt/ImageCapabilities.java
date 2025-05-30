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
 * 图像的能力和属性。
 * @author Michael Martak
 * @since 1.4
 */
public class ImageCapabilities implements Cloneable {

    private boolean accelerated = false;

    /**
     * 创建一个新的对象来指定图像能力。
     * @param accelerated 是否需要加速图像
     */
    public ImageCapabilities(boolean accelerated) {
        this.accelerated = accelerated;
    }

    /**
     * 如果此 <code>ImageCapabilities</code> 封装的对象可以或已经被加速，则返回 <code>true</code>。
     * @return 图像是否可以或已经被加速。有多种平台特定的方法可以加速图像，包括
     * pixmaps, VRAM, AGP。这是通用的加速方法（相对于驻留在系统内存中）。
     */
    public boolean isAccelerated() {
        return accelerated;
    }

    /**
     * 如果此 <code>ImageCapabilities</code> 描述的 <code>VolatileImage</code>
     * 可能会丢失其表面，则返回 <code>true</code>。
     * @return 挥发性图像是否可能因操作系统的任意决定而丢失其表面。
     */
    public boolean isTrueVolatile() {
        return false;
    }

    /**
     * @return 此 ImageCapabilities 对象的副本。
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // 由于我们实现了 Cloneable，这种情况不应该发生
            throw new InternalError(e);
        }
    }

}
