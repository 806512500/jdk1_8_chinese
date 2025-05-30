/*
 * Copyright (c) 1997, 1999, Oracle and/or its affiliates. All rights reserved.
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

/*
 * (C) Copyright Taligent, Inc. 1996 - 1997, All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998, All Rights Reserved
 *
 * The original version of this source code and documentation is
 * copyrighted and owned by Taligent, Inc., a wholly-owned subsidiary
 * of IBM. These materials are provided under terms of a License
 * Agreement between Taligent and Sun. This technology is protected
 * by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 */

package java.awt.font;

/**
 * <code>GlyphJustificationInfo</code> 类表示关于字形对齐属性的信息。字形是一个或多个字符的视觉表示。许多不同的字形可以用来表示单个字符或字符组合。由 <code>GlyphJustificationInfo</code> 表示的四个对齐属性是权重、优先级、吸收和限制。
 * <p>
 * 权重是字形在线条中的总体“权重”。通常它与字体大小成正比。权重较大的字形会被分配相应较大的空间变化量。
 * <p>
 * 优先级确定在哪个对齐阶段使用此字形。所有相同优先级的字形都会在下一个优先级的字形之前进行检查。如果所有空间变化都可以分配给这些字形而不超过它们的限制，则不会检查下一个优先级的字形。有四个优先级：kashida、空格、字符间和无。KASHIDA 是首先检查的优先级。NONE 是最后检查的优先级。
 * <p>
 * 吸收确定字形是否吸收所有空间变化。在给定的优先级内，某些字形可能会吸收所有空间变化。如果存在这些字形，则不会检查后续优先级的字形。
 * <p>
 * 限制确定字形可以变化的最大或最小量。字形的左右两侧可以有不同的限制。
 * <p>
 * 每个 <code>GlyphJustificationInfo</code> 表示两组度量，分别是 <i>扩展</i> 和 <i>收缩</i>。扩展度量用于当一行上的字形需要分散以适应更大的宽度时。收缩度量用于当字形需要靠拢以适应更小的宽度时。
 */

public final class GlyphJustificationInfo {

    /**
     * 构造关于字形对齐属性的信息。
     * @param weight 分配空间时此字形的权重。必须是非负数。
     * @param growAbsorb 如果为 <code>true</code>，则此字形在扩展时吸收此优先级和更低优先级的所有额外空间。
     * @param growPriority 此字形扩展时的优先级。
     * @param growLeftLimit 此字形左侧可以扩展的最大量。必须是非负数。
     * @param growRightLimit 此字形右侧可以扩展的最大量。必须是非负数。
     * @param shrinkAbsorb 如果为 <code>true</code>，则此字形在收缩时吸收此优先级和更低优先级的所有剩余收缩。
     * @param shrinkPriority 此字形收缩时的优先级。
     * @param shrinkLeftLimit 此字形左侧可以收缩的最大量。必须是非负数。
     * @param shrinkRightLimit 此字形右侧可以收缩的最大量。必须是非负数。
     */
     public GlyphJustificationInfo(float weight,
                                  boolean growAbsorb,
                                  int growPriority,
                                  float growLeftLimit,
                                  float growRightLimit,
                                  boolean shrinkAbsorb,
                                  int shrinkPriority,
                                  float shrinkLeftLimit,
                                  float shrinkRightLimit)
    {
        if (weight < 0) {
            throw new IllegalArgumentException("weight is negative");
        }

        if (!priorityIsValid(growPriority)) {
            throw new IllegalArgumentException("Invalid grow priority");
        }
        if (growLeftLimit < 0) {
            throw new IllegalArgumentException("growLeftLimit is negative");
        }
        if (growRightLimit < 0) {
            throw new IllegalArgumentException("growRightLimit is negative");
        }

        if (!priorityIsValid(shrinkPriority)) {
            throw new IllegalArgumentException("Invalid shrink priority");
        }
        if (shrinkLeftLimit < 0) {
            throw new IllegalArgumentException("shrinkLeftLimit is negative");
        }
        if (shrinkRightLimit < 0) {
            throw new IllegalArgumentException("shrinkRightLimit is negative");
        }

        this.weight = weight;
        this.growAbsorb = growAbsorb;
        this.growPriority = growPriority;
        this.growLeftLimit = growLeftLimit;
        this.growRightLimit = growRightLimit;
        this.shrinkAbsorb = shrinkAbsorb;
        this.shrinkPriority = shrinkPriority;
        this.shrinkLeftLimit = shrinkLeftLimit;
        this.shrinkRightLimit = shrinkRightLimit;
    }

    private static boolean priorityIsValid(int priority) {

        return priority >= PRIORITY_KASHIDA && priority <= PRIORITY_NONE;
    }

    /** 最高的对齐优先级。 */
    public static final int PRIORITY_KASHIDA = 0;

    /** 第二高的对齐优先级。 */
    public static final int PRIORITY_WHITESPACE = 1;

    /** 第二低的对齐优先级。 */
    public static final int PRIORITY_INTERCHAR = 2;

    /** 最低的对齐优先级。 */
    public static final int PRIORITY_NONE = 3;

    /**
     * 此字形的权重。
     */
    public final float weight;

    /**
     * 此字形在扩展时的优先级。
     */
    public final int growPriority;

    /**
     * 如果为 <code>true</code>，则此字形在扩展时吸收此优先级和更低优先级的所有额外空间。
     */
    public final boolean growAbsorb;

    /**
     * 此字形左侧可以扩展的最大量。
     */
    public final float growLeftLimit;

    /**
     * 此字形右侧可以扩展的最大量。
     */
    public final float growRightLimit;

    /**
     * 此字形在收缩时的优先级。
     */
    public final int shrinkPriority;

    /**
     * 如果为 <code>true</code>，则此字形在收缩时吸收此优先级和更低优先级的所有剩余收缩。
     */
    public final boolean shrinkAbsorb;

    /**
     * 此字形左侧可以收缩的最大量（正数）。
     */
    public final float shrinkLeftLimit;

    /**
     * 此字形右侧可以收缩的最大量（正数）。
     */
    public final float shrinkRightLimit;
}
