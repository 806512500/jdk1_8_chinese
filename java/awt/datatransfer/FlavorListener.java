/*
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
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
package java.awt.datatransfer;

import java.util.EventListener;


/**
 * 定义一个监听 {@link FlavorEvent} 的对象。
 *
 * @author Alexander Gerasimov
 * @since 1.5
 */
public interface FlavorListener extends EventListener {
    /**
     * 当监听器的目标 {@link Clipboard} 的可用 {@link DataFlavor} 发生变化时调用。
     * <p>
     * 有些通知可能是多余的 —— 它们并不是由剪贴板上可用的 DataFlavor 集合的变化引起的。
     * 例如，如果剪贴板子系统认为系统剪贴板的内容已更改，但因访问剪贴板时出现某些异常情况而无法确定其 DataFlavor 是否已更改，
     * 则会发送通知以确保不会遗漏重要的通知。通常，这些多余的
     * 通知应该是偶尔发生的。
     *
     * @param e 一个 <code>FlavorEvent</code> 对象
     */
    void flavorsChanged(FlavorEvent e);
}
