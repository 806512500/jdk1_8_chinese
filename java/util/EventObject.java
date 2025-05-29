/*
 * Copyright (c) 1996, 2003, Oracle and/or its affiliates. All rights reserved.
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

package java.util;

/**
 * <p>
 * 所有事件状态对象的根类。
 * <p>
 * 所有事件都使用一个引用，即“源”，逻辑上认为该对象是事件最初发生的地方。
 *
 * @since JDK1.1
 */

public class EventObject implements java.io.Serializable {

    private static final long serialVersionUID = 5516075349620653480L;

    /**
     * 事件最初发生的地方的对象。
     */
    protected transient Object  source;

    /**
     * 构造一个原型事件。
     *
     * @param    source    事件最初发生的地方的对象。
     * @exception  IllegalArgumentException  如果源为 null。
     */
    public EventObject(Object source) {
        if (source == null)
            throw new IllegalArgumentException("null source");

        this.source = source;
    }

    /**
     * 获取事件最初发生的地方的对象。
     *
     * @return   事件最初发生的地方的对象。
     */
    public Object getSource() {
        return source;
    }

    /**
     * 返回此 EventObject 的字符串表示形式。
     *
     * @return  此 EventObject 的字符串表示形式。
     */
    public String toString() {
        return getClass().getName() + "[source=" + source + "]";
    }
}
