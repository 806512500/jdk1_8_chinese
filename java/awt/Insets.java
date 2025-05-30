/*
 * Copyright (c) 1995, 2011, Oracle and/or its affiliates. All rights reserved.
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
 * 一个 <code>Insets</code> 对象是容器边框的表示。它指定了容器在每个边缘必须留出的空间。这个空间可以是边框、空白空间或标题。
 *
 * @author      Arthur van Hoff
 * @author      Sami Shaio
 * @see         java.awt.LayoutManager
 * @see         java.awt.Container
 * @since       JDK1.0
 */
public class Insets implements Cloneable, java.io.Serializable {

    /**
     * 从顶部的内边距。
     * 此值加到矩形的顶部以得出新的顶部位置。
     *
     * @serial
     * @see #clone()
     */
    public int top;

    /**
     * 从左侧的内边距。
     * 此值加到矩形的左侧以得出新的左侧位置。
     *
     * @serial
     * @see #clone()
     */
    public int left;

    /**
     * 从底部的内边距。
     * 此值从矩形的底部减去以得出新的底部位置。
     *
     * @serial
     * @see #clone()
     */
    public int bottom;

    /**
     * 从右侧的内边距。
     * 此值从矩形的右侧减去以得出新的右侧位置。
     *
     * @serial
     * @see #clone()
     */
    public int right;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = -2272572637695466749L;

    static {
        /* 确保加载必要的本机库 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    /**
     * 创建并初始化一个新的 <code>Insets</code> 对象，指定顶部、左侧、底部和右侧的内边距。
     * @param       top   从顶部的内边距。
     * @param       left   从左侧的内边距。
     * @param       bottom   从底部的内边距。
     * @param       right   从右侧的内边距。
     */
    public Insets(int top, int left, int bottom, int right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    /**
     * 将顶部、左侧、底部和右侧设置为指定的值
     *
     * @param       top   从顶部的内边距。
     * @param       left   从左侧的内边距。
     * @param       bottom   从底部的内边距。
     * @param       right   从右侧的内边距。
     * @since 1.5
     */
    public void set(int top, int left, int bottom, int right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    /**
     * 检查两个内边距对象是否相等。两个 <code>Insets</code> 实例相等，如果它们的四个整数值 <code>top</code>、<code>left</code>、
     * <code>bottom</code> 和 <code>right</code> 都相等。
     * @return      如果两个内边距相等，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @since       JDK1.1
     */
    public boolean equals(Object obj) {
        if (obj instanceof Insets) {
            Insets insets = (Insets)obj;
            return ((top == insets.top) && (left == insets.left) &&
                    (bottom == insets.bottom) && (right == insets.right));
        }
        return false;
    }

    /**
     * 返回此内边距的哈希码。
     *
     * @return    此内边距的哈希码。
     */
    public int hashCode() {
        int sum1 = left + bottom;
        int sum2 = right + top;
        int val1 = sum1 * (sum1 + 1)/2 + left;
        int val2 = sum2 * (sum2 + 1)/2 + top;
        int sum3 = val1 + val2;
        return sum3 * (sum3 + 1)/2 + val2;
    }

    /**
     * 返回此 <code>Insets</code> 对象的字符串表示。此方法仅用于调试目的，返回字符串的内容和格式可能因实现而异。返回的字符串可能为空，但不能为 <code>null</code>。
     *
     * @return  此 <code>Insets</code> 对象的字符串表示。
     */
    public String toString() {
        return getClass().getName() + "[top="  + top + ",left=" + left + ",bottom=" + bottom + ",right=" + right + "]";
    }

    /**
     * 创建此对象的副本。
     * @return     此 <code>Insets</code> 对象的副本。
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // 这不应该发生，因为我们实现了 Cloneable
            throw new InternalError(e);
        }
    }
    /**
     * 初始化 JNI 字段和方法 ID
     */
    private static native void initIDs();

}
