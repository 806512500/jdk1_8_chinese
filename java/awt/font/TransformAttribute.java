/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.geom.AffineTransform;
import java.io.Serializable;
import java.io.ObjectStreamException;

/**
 * <code>TransformAttribute</code> 类提供了一个不可变的转换包装器，以便安全地用作属性。
 */
public final class TransformAttribute implements Serializable {

    /**
     * 此 <code>TransformAttribute</code> 的 <code>AffineTransform</code>，如果 <code>AffineTransform</code> 是单位转换，则为 <code>null</code>。
     */
    private AffineTransform transform;

    /**
     * 包装指定的转换。转换被克隆并保留克隆的引用。原始转换保持不变。
     * 如果传递的参数为 null，则此构造函数的行为如同它是单位转换。在这种情况下，最好使用 {@link #IDENTITY}。
     * @param transform 要包装的指定 {@link AffineTransform}，或 null。
     */
    public TransformAttribute(AffineTransform transform) {
        if (transform != null && !transform.isIdentity()) {
            this.transform = new AffineTransform(transform);
        }
    }

    /**
     * 返回包装的转换的副本。
     * @return 一个 <code>AffineTransform</code>，它是此 <code>TransformAttribute</code> 的包装转换的副本。
     */
    public AffineTransform getTransform() {
        AffineTransform at = transform;
        return (at == null) ? new AffineTransform() : new AffineTransform(at);
    }

    /**
     * 如果包装的转换是单位转换，则返回 <code>true</code>。
     * @return 如果包装的转换是单位转换，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @since 1.4
     */
    public boolean isIdentity() {
        return transform == null;
    }

    /**
     * 表示单位转换的 <code>TransformAttribute</code>。
     * @since 1.6
     */
    public static final TransformAttribute IDENTITY = new TransformAttribute(null);

    private void writeObject(java.io.ObjectOutputStream s)
      throws java.lang.ClassNotFoundException,
             java.io.IOException
    {
        // 叹息 -- 1.3 期望转换从不为 null，所以我们需要始终写入一个
        if (this.transform == null) {
            this.transform = new AffineTransform();
        }
        s.defaultWriteObject();
    }

    /*
     * @since 1.6
     */
    private Object readResolve() throws ObjectStreamException {
        if (transform == null || transform.isIdentity()) {
            return IDENTITY;
        }
        return this;
    }

    // 为序列化向后兼容添加（4348425）
    static final long serialVersionUID = 3356247357827709530L;

    /**
     * @since 1.6
     */
    public int hashCode() {
        return transform == null ? 0 : transform.hashCode();
    }

    /**
     * 如果 rhs 是一个 <code>TransformAttribute</code>，其转换等于此 <code>TransformAttribute</code> 的转换，则返回 <code>true</code>。
     * @param rhs 要比较的对象
     * @return 如果参数是一个 <code>TransformAttribute</code>，其转换等于此 <code>TransformAttribute</code> 的转换，则返回 <code>true</code>。
     * @since 1.6
     */
    public boolean equals(Object rhs) {
        if (rhs != null) {
            try {
                TransformAttribute that = (TransformAttribute)rhs;
                if (transform == null) {
                    return that.transform == null;
                }
                return transform.equals(that.transform);
            }
            catch (ClassCastException e) {
            }
        }
        return false;
    }
}
