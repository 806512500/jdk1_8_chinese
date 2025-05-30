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
package java.awt.font;

import java.awt.Font;

/**
 * <code>MultipleMaster</code> 接口表示 Type 1 多重主字体。
 * 特定的 {@link Font} 对象可以实现此接口。
 */
public interface MultipleMaster {

  /**
   * 返回多重主设计控制的数量。
   * 设计轴包括宽度、重量和光学缩放等。
   * @return 多重主设计控制的数量
   */
  public  int getNumDesignAxes();

  /**
   * 返回每个轴的设计限制的数组，形式为 [from&rarr;to]。
   * 例如，重量的设计限制可以是从 0.1 到 1.0。返回的值顺序与
   * <code>getDesignAxisNames</code> 返回的顺序相同。
   * @return 每个轴的设计限制的数组。
   */
  public  float[]  getDesignAxisRanges();

  /**
   * 返回每个轴的默认设计值的数组。例如，
   * 重量的默认值可以是 1.6。返回的值顺序与 <code>getDesignAxisNames</code> 返回的顺序相同。
   * @return 每个轴的默认设计值的数组。
   */
  public  float[]  getDesignAxisDefaults();

  /**
   * 返回每个设计轴的名称。这也决定了每个轴的值返回的顺序。
   * @return 包含每个设计轴名称的数组。
   */
  public  String[] getDesignAxisNames();

  /**
   * 根据指定数组中的设计轴值创建多重主字体的新实例。数组的大小必须与
   * <code>getNumDesignAxes</code> 返回的值对应，数组元素的值必须在
   * <code>getDesignAxesLimits</code> 指定的范围内。如果发生错误，
   * 返回 <code>null</code>。
   * @param axes 包含轴值的数组
   * @return 一个 {@link Font} 对象，它是 <code>MultipleMaster</code> 的实例，并基于
   * <code>axes</code> 提供的设计轴值。
   */
  public Font deriveMMFont(float[] axes);

  /**
   * 根据详细的度量信息创建多重主字体的新实例。如果发生错误，返回 <code>null</code>。
   * @param glyphWidths 一个浮点数数组，表示字体空间中每个字形的期望宽度
   * @param avgStemWidth 字体空间中整个字体的平均笔画宽度
   * @param typicalCapHeight 典型大写字符的高度
   * @param typicalXHeight 典型小写字符的高度
   * @param italicAngle 斜体倾斜的角度，以度为单位，逆时针方向从垂直方向计算
   * @return 一个 <code>Font</code> 对象，它是 <code>MultipleMaster</code> 的实例，并基于
   * 指定的度量信息。
   */
  public Font deriveMMFont(
                                   float[] glyphWidths,
                                   float avgStemWidth,
                                   float typicalCapHeight,
                                   float typicalXHeight,
                                   float italicAngle);


}
