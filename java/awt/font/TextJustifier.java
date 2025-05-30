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
 *
 */

package java.awt.font;

/*
 * 每个字形的每一边都有一个信息
 * 分别为扩展和收缩情况提供信息
 * !!! 这实际上不需要是一个单独的类。如果我们保持它
 * 单独，TextLayout 的新对齐代码可能也属于这里。
 */

class TextJustifier {
    private GlyphJustificationInfo[] info;
    private int start;
    private int limit;

    static boolean DEBUG = false;

    /**
     * 使用与每个字形对应的信息数组初始化对齐器。Start 和 limit 指示要检查的数组范围。
     */
    TextJustifier(GlyphJustificationInfo[] info, int start, int limit) {
        this.info = info;
        this.start = start;
        this.limit = limit;

        if (DEBUG) {
            System.out.println("start: " + start + ", limit: " + limit);
            for (int i = start; i < limit; i++) {
                GlyphJustificationInfo gji = info[i];
                System.out.println("w: " + gji.weight + ", gp: " +
                                   gji.growPriority + ", gll: " +
                                   gji.growLeftLimit + ", grl: " +
                                   gji.growRightLimit);
            }
        }
    }

    public static final int MAX_PRIORITY = 3;

    /**
     * 返回一个长度为原始信息数组两倍的 delta 数组，
     * 表示每个字形的每一边应扩展或收缩的量。
     *
     * Delta 应为正数以扩展行，负数以压缩行。
     */
    public float[] justify(float delta) {
        float[] deltas = new float[info.length * 2];

        boolean grow = delta > 0;

        if (DEBUG)
            System.out.println("delta: " + delta);

        // 按优先级递减的顺序遍历字形，直到 justifyDelta 为零或优先级用完。
        int fallbackPriority = -1;
        for (int p = 0; delta != 0; p++) {
            /*
             * 特殊情况 'fallback' 迭代，设置标志并重新检查
             * 最高优先级
             */
            boolean lastPass = p > MAX_PRIORITY;
            if (lastPass)
                p = fallbackPriority;

            // 遍历字形，首先收集权重和限制
            float weight = 0;
            float gslimit = 0;
            float absorbweight = 0;
            for (int i = start; i < limit; i++) {
                GlyphJustificationInfo gi = info[i];
                if ((grow ? gi.growPriority : gi.shrinkPriority) == p) {
                    if (fallbackPriority == -1) {
                        fallbackPriority = p;
                    }

                    if (i != start) { // 忽略第一个字符的左边
                        weight += gi.weight;
                        if (grow) {
                            gslimit += gi.growLeftLimit;
                            if (gi.growAbsorb) {
                                absorbweight += gi.weight;
                            }
                        } else {
                            gslimit += gi.shrinkLeftLimit;
                            if (gi.shrinkAbsorb) {
                                absorbweight += gi.weight;
                            }
                        }
                    }

                    if (i + 1 != limit) { // 忽略最后一个字符的右边
                        weight += gi.weight;
                        if (grow) {
                            gslimit += gi.growRightLimit;
                            if (gi.growAbsorb) {
                                absorbweight += gi.weight;
                            }
                        } else {
                            gslimit += gi.shrinkRightLimit;
                            if (gi.shrinkAbsorb) {
                                absorbweight += gi.weight;
                            }
                        }
                    }
                }
            }

            // 是否达到限制？
            if (!grow) {
                gslimit = -gslimit; // 负 delta 为负
            }
            boolean hitLimit = (weight == 0) || (!lastPass && ((delta < 0) == (delta < gslimit)));
            boolean absorbing = hitLimit && absorbweight > 0;

            // 提前将 delta 除以权重
            float weightedDelta = delta / weight; // 如果权重为 0，则不使用

            float weightedAbsorb = 0;
            if (hitLimit && absorbweight > 0) {
                weightedAbsorb = (delta - gslimit) / absorbweight;
            }

            if (DEBUG) {
                System.out.println("pass: " + p +
                    ", d: " + delta +
                    ", l: " + gslimit +
                    ", w: " + weight +
                    ", aw: " + absorbweight +
                    ", wd: " + weightedDelta +
                    ", wa: " + weightedAbsorb +
                    ", hit: " + (hitLimit ? "y" : "n"));
            }

            // 现在根据权重与总权重的比例分配
            int n = start * 2;
            for (int i = start; i < limit; i++) {
                GlyphJustificationInfo gi = info[i];
                if ((grow ? gi.growPriority : gi.shrinkPriority) == p) {
                    if (i != start) { // 忽略左边
                        float d;
                        if (hitLimit) {
                            // 包含符号
                            d = grow ? gi.growLeftLimit : -gi.shrinkLeftLimit;
                            if (absorbing) {
                                // 符号已包含
                                d += gi.weight * weightedAbsorb;
                            }
                        } else {
                            // 符号已包含
                            d = gi.weight * weightedDelta;
                        }

                        deltas[n] += d;
                    }
                    n++;

                    if (i + 1 != limit) { // 忽略右边
                        float d;
                        if (hitLimit) {
                            d = grow ? gi.growRightLimit : -gi.shrinkRightLimit;
                            if (absorbing) {
                                d += gi.weight * weightedAbsorb;
                            }
                        } else {
                            d = gi.weight * weightedDelta;
                        }

                        deltas[n] += d;
                    }
                    n++;
                } else {
                    n += 2;
                }
            }

            if (!lastPass && hitLimit && !absorbing) {
                delta -= gslimit;
            } else {
                delta = 0; // 停止迭代
            }
        }

        if (DEBUG) {
            float total = 0;
            for (int i = 0; i < deltas.length; i++) {
                total += deltas[i];
                System.out.print(deltas[i] + ", ");
                if (i % 20 == 9) {
                    System.out.println();
                }
            }
            System.out.println("\ntotal: " + total);
            System.out.println();
        }

        return deltas;
    }
}
