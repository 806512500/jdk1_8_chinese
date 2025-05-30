/*
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;

/**
 * <code>GraphicsConfigTemplate</code> 类用于获取有效的
 * {@link GraphicsConfiguration}。用户实例化这些对象之一，并设置所有非默认属性。然后在
 * {@link GraphicsDevice} 类中调用 {@link GraphicsDevice#getBestConfiguration} 方法，
 * 传入此 <code>GraphicsConfigTemplate</code>。返回一个有效的
 * <code>GraphicsConfiguration</code>，满足或超过 <code>GraphicsConfigTemplate</code> 中请求的条件。
 * @see GraphicsDevice
 * @see GraphicsConfiguration
 *
 * @since       1.2
 */
public abstract class GraphicsConfigTemplate implements Serializable {
    /*
     * serialVersionUID
     */
    private static final long serialVersionUID = -8061369279557787079L;

    /**
     * 该类是抽象类，因此只有子类可以实例化。
     */
    public GraphicsConfigTemplate() {
    }

    /**
     * 用于“枚举”（整数）类型。表示此功能是
     * <code>GraphicsConfiguration</code> 对象所必需的。如果此功能不可用，则不选择
     * <code>GraphicsConfiguration</code> 对象。
     */
    public static final int REQUIRED    = 1;

    /**
     * 用于“枚举”（整数）类型。表示此功能是
     * <code>GraphicsConfiguration</code> 对象所希望的。具有此功能的选择优于不包含此功能的选择，尽管两者都可以视为有效的匹配。
     */
    public static final int PREFERRED   = 2;

    /**
     * 用于“枚举”（整数）类型。表示此功能对于选择
     * <code>GraphicsConfiguration</code> 对象不是必需的。不包含此功能的选择优于包含此功能的选择，因为此功能未使用。
     */
    public static final int UNNECESSARY = 3;

    /**
     * 返回符合 <code>GraphicsConfigTemplate</code> 中定义的标准的最佳配置。
     * @param gc 要从中选择的 <code>GraphicsConfiguration</code> 对象数组。
     * @return 一个 <code>GraphicsConfiguration</code> 对象，表示最佳配置。
     * @see GraphicsConfiguration
     */
    public abstract GraphicsConfiguration
      getBestConfiguration(GraphicsConfiguration[] gc);

    /**
     * 返回一个 <code>boolean</code> 值，指示指定的 <code>GraphicsConfiguration</code> 是否可以
     * 用于创建支持所指示功能的绘图表面。
     * @param gc 要测试的 <code>GraphicsConfiguration</code> 对象。
     * @return 如果此 <code>GraphicsConfiguration</code> 对象可以用于创建支持所指示功能的表面，则返回 <code>true</code>；
     * 如果 <code>GraphicsConfiguration</code> 不能用于创建 Java(tm) API 可用的绘图表面，则返回 <code>false</code>。
     */
    public abstract boolean
      isGraphicsConfigSupported(GraphicsConfiguration gc);

}
