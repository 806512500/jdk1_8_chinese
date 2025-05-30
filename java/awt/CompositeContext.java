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

import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * <code>CompositeContext</code> 接口定义了合成操作的封装和优化环境。
 * <code>CompositeContext</code> 对象维护合成操作的状态。在多线程环境中，可以同时存在多个上下文对象，对应于单个 {@link Composite} 对象。
 * @see Composite
 */

public interface CompositeContext {
    /**
     * 释放为上下文分配的资源。
     */
    public void dispose();

    /**
     * 将两个源 {@link Raster} 对象合成，并将结果存储在目标
     * {@link WritableRaster} 中。注意，目标可以是第一个或第二个源对象。注意 <code>dstIn</code> 和
     * <code>dstOut</code> 必须与传递给
     * {@link Composite#createContext(java.awt.image.ColorModel, java.awt.image.ColorModel, java.awt.RenderingHints) createContext}
     * 方法的 <code>dstColorModel</code> 兼容。
     * @param src 合成操作的第一个源
     * @param dstIn 合成操作的第二个源
     * @param dstOut 存储操作结果的 <code>WritableRaster</code>
     * @see Composite
     */
    public void compose(Raster src,
                        Raster dstIn,
                        WritableRaster dstOut);


}
