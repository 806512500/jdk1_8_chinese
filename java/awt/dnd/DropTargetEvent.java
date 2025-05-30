/*
 * Copyright (c) 1997, 2008, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.dnd;

import java.util.EventObject;
import java.awt.dnd.DropTargetContext;

/**
 * <code>DropTargetEvent</code> 是 <code>DropTargetDragEvent</code>
 * 和 <code>DropTargetDropEvent</code> 的基类。
 * 它封装了拖放操作的当前状态，特别是当前的
 * <code>DropTargetContext</code>。
 *
 * @since 1.2
 *
 */

public class DropTargetEvent extends java.util.EventObject {

    private static final long serialVersionUID = 2821229066521922993L;

    /**
     * 使用指定的 <code>DropTargetContext</code> 构造一个 <code>DropTargetEvent</code> 对象。
     * <P>
     * @param dtc <code>DropTargetContext</code>
     * @throws NullPointerException 如果 {@code dtc} 等于 {@code null}。
     * @see #getSource()
     * @see #getDropTargetContext()
     */

    public DropTargetEvent(DropTargetContext dtc) {
        super(dtc.getDropTarget());

        context  = dtc;
    }

    /**
     * 返回与此 <code>DropTargetEvent</code> 关联的 <code>DropTargetContext</code>。
     * <P>
     * @return <code>DropTargetContext</code>
     */

    public DropTargetContext getDropTargetContext() {
        return context;
    }

    /**
     * 与此 <code>DropTargetEvent</code> 关联的 <code>DropTargetContext</code>。
     *
     * @serial
     */
    protected DropTargetContext   context;
}
