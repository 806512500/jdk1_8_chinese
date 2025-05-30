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
import java.lang.String;

/**
 * <code>TextHitInfo</code> 类表示文本模型中的一个字符位置，以及一个 <b>偏置</b>，或“侧”，即字符的一边。偏置可以是 <EM>前导</EM>（对于从左到右的字符，是左边），或 <EM>尾随</EM>（对于从左到右的字符，是右边）。<code>TextHitInfo</code> 的实例用于指定文本中的光标和插入位置。
 * <p>
 * 例如，考虑文本 "abc"。TextHitInfo.trailing(1) 对应于文本中 'b' 的右边。
 * <p>
 * <code>TextHitInfo</code> 主要由 {@link TextLayout} 及其客户端使用。这些客户端查询 <code>TextHitInfo</code> 实例以获取插入偏移量，即新文本插入到文本模型中的位置。插入偏移量等于 <code>TextHitInfo</code> 中的字符位置，如果偏置是前导的，则插入偏移量等于字符位置；如果偏置是尾随的，则插入偏移量等于字符位置后一个字符。TextHitInfo.trailing(1) 的插入偏移量为 2。
 * <p>
 * 有时，构造一个与现有 <code>TextHitInfo</code> 具有相同插入偏移量但位于插入偏移量另一侧字符的 <code>TextHitInfo</code> 是很方便的。<code>getOtherHit</code> 方法构造一个与现有 <code>TextHitInfo</code> 具有相同插入偏移量的新 <code>TextHitInfo</code>，但位于插入偏移量另一侧的字符上。对 trailing(1) 调用 <code>getOtherHit</code> 会返回 leading(2)。通常，<code>getOtherHit</code> 对 trailing(n) 返回 leading(n+1)，对 leading(n) 返回 trailing(n-1)。
 * <p>
 * <strong>示例</strong>：<p>
 * 将图形点转换为文本模型中的插入点
 * <blockquote><pre>
 * TextLayout layout = ...;
 * Point2D.Float hitPoint = ...;
 * TextHitInfo hitInfo = layout.hitTestChar(hitPoint.x, hitPoint.y);
 * int insPoint = hitInfo.getInsertionIndex();
 * // insPoint 相对于 layout；可能需要调整以用于文本模型
 * </pre></blockquote>
 *
 * @see TextLayout
 */

public final class TextHitInfo {
    private int charIndex;
    private boolean isLeadingEdge;

    /**
     * 构造一个新的 <code>TextHitInfo</code>。
     * @param charIndex 被击中的字符的索引
     * @param isLeadingEdge 如果击中的是字符的前导边，则为 <code>true</code>
     */
    private TextHitInfo(int charIndex, boolean isLeadingEdge) {
        this.charIndex = charIndex;
        this.isLeadingEdge = isLeadingEdge;
    }

    /**
     * 返回被击中的字符的索引。
     * @return 被击中的字符的索引。
     */
    public int getCharIndex() {
        return charIndex;
    }

    /**
     * 如果击中的是字符的前导边，则返回 <code>true</code>。
     * @return 如果击中的是字符的前导边，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    public boolean isLeadingEdge() {
        return isLeadingEdge;
    }

    /**
     * 返回插入索引。如果击中的是字符的前导边，则插入索引等于字符索引；如果击中的是字符的尾随边，则插入索引等于字符索引加一。
     * @return 插入索引。
     */
    public int getInsertionIndex() {
        return isLeadingEdge ? charIndex : charIndex + 1;
    }

    /**
     * 返回哈希码。
     * @return 此 <code>TextHitInfo</code> 的哈希码，即此 <code>TextHitInfo</code> 的 <code>charIndex</code>。
     */
    public int hashCode() {
        return charIndex;
    }

    /**
     * 如果指定的 <code>Object</code> 是一个 <code>TextHitInfo</code> 并且等于此 <code>TextHitInfo</code>，则返回 <code>true</code>。
     * @param obj 要测试的 <code>Object</code>
     * @return 如果指定的 <code>Object</code> 等于此 <code>TextHitInfo</code>，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    public boolean equals(Object obj) {
        return (obj instanceof TextHitInfo) && equals((TextHitInfo)obj);
    }

    /**
     * 如果指定的 <code>TextHitInfo</code> 具有与此 <code>TextHitInfo</code> 相同的 <code>charIndex</code> 和 <code>isLeadingEdge</code>，则返回 <code>true</code>。这与具有相同的插入偏移量不同。
     * @param hitInfo 指定的 <code>TextHitInfo</code>
     * @return 如果指定的 <code>TextHitInfo</code> 具有与此 <code>TextHitInfo</code> 相同的 <code>charIndex</code> 和 <code>isLeadingEdge</code>，则返回 <code>true</code>。
     */
    public boolean equals(TextHitInfo hitInfo) {
        return hitInfo != null && charIndex == hitInfo.charIndex &&
            isLeadingEdge == hitInfo.isLeadingEdge;
    }

    /**
     * 返回一个表示此 <code>TextHitInfo</code> 的 <code>String</code>，仅用于调试。
     * @return 一个表示此 <code>TextHitInfo</code> 的 <code>String</code>。
     */
    public String toString() {
        return "TextHitInfo[" + charIndex + (isLeadingEdge ? "L" : "T")+"]";
    }

    /**
     * 在指定的 <code>charIndex</code> 处的字符的前导边上创建一个 <code>TextHitInfo</code>。
     * @param charIndex 被击中的字符的索引
     * @return 在指定的 <code>charIndex</code> 处的字符的前导边上创建的 <code>TextHitInfo</code>。
     */
    public static TextHitInfo leading(int charIndex) {
        return new TextHitInfo(charIndex, true);
    }

    /**
     * 在指定的 <code>charIndex</code> 处的字符的尾随边上创建一个 <code>TextHitInfo</code>。
     * @param charIndex 被击中的字符的索引
     * @return 在指定的 <code>charIndex</code> 处的字符的尾随边上创建的 <code>TextHitInfo</code>。
     */
    public static TextHitInfo trailing(int charIndex) {
        return new TextHitInfo(charIndex, false);
    }

    /**
     * 在指定的偏移量处创建一个 <code>TextHitInfo</code>，与偏移量前的字符相关联。
     * @param offset 与偏移量前的字符相关联的偏移量
     * @return 在指定的偏移量处创建的 <code>TextHitInfo</code>。
     */
    public static TextHitInfo beforeOffset(int offset) {
        return new TextHitInfo(offset-1, false);
    }

    /**
     * 在指定的偏移量处创建一个 <code>TextHitInfo</code>，与偏移量后的字符相关联。
     * @param offset 与偏移量后的字符相关联的偏移量
     * @return 在指定的偏移量处创建的 <code>TextHitInfo</code>。
     */
    public static TextHitInfo afterOffset(int offset) {
        return new TextHitInfo(offset, true);
    }

    /**
     * 在插入点的另一侧创建一个 <code>TextHitInfo</code>。此 <code>TextHitInfo</code> 保持不变。
     * @return 在插入点的另一侧创建的 <code>TextHitInfo</code>。
     */
    public TextHitInfo getOtherHit() {
        if (isLeadingEdge) {
            return trailing(charIndex - 1);
        } else {
            return leading(charIndex + 1);
        }
    }

    /**
     * 创建一个 <code>TextHitInfo</code>，其字符索引相对于此 <code>TextHitInfo</code> 的 <code>charIndex</code> 偏移 <code>delta</code>。此 <code>TextHitInfo</code> 保持不变。
     * @param delta 要偏移此 <code>charIndex</code> 的值
     * @return 一个 <code>TextHitInfo</code>，其 <code>charIndex</code> 相对于此 <code>TextHitInfo</code> 的 <code>charIndex</code> 偏移 <code>delta</code>。
     */
    public TextHitInfo getOffsetHit(int delta) {
        return new TextHitInfo(charIndex + delta, isLeadingEdge);
    }
}
