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

package java.awt;

import java.lang.annotation.Native;

/**
 * <code>Transparency</code> 接口定义了实现类的通用透明模式。
 */
public interface Transparency {

    /**
     * 表示图像数据保证完全不透明，意味着所有像素的 alpha 值为 1.0。
     */
    @Native public final static int OPAQUE            = 1;

    /**
     * 表示图像数据保证要么完全不透明，alpha 值为 1.0，要么完全透明，alpha 值为 0.0。
     */
    @Native public final static int BITMASK = 2;

    /**
     * 表示图像数据包含或可能包含任意的 alpha 值，范围从 0.0 到 1.0。
     */
    @Native public final static int TRANSLUCENT        = 3;

    /**
     * 返回此 <code>Transparency</code> 的类型。
     * @return 此 <code>Transparency</code> 的字段类型，可以是 OPAQUE、BITMASK 或 TRANSLUCENT。
     */
    public int getTransparency();
}
