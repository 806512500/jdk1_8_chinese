/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.event.KeyEvent;


/**
 * KeyEventDispatcher 与当前的 KeyboardFocusManager 协作，负责所有 KeyEvent 的目标定位和分发。
 * 注册到当前 KeyboardFocusManager 的 KeyEventDispatcher 会在事件分发到目标之前接收到这些 KeyEvent，
 * 允许每个 KeyEventDispatcher 重新定位事件、消耗事件、自行分发事件或进行其他更改。
 * <p>
 * 注意，KeyboardFocusManager 本身实现了 KeyEventDispatcher。默认情况下，当前的 KeyboardFocusManager
 * 将作为所有未由注册的 KeyEventDispatcher 分发的 KeyEvent 的接收者。当前的 KeyboardFocusManager
 * 不能完全取消注册为 KeyEventDispatcher。但是，如果 KeyEventDispatcher 报告它已经分发了 KeyEvent，
 * 无论它是否真的这样做，KeyboardFocusManager 将不会对 KeyEvent 采取进一步的行动。虽然客户端代码
 * 可以将当前的 KeyboardFocusManager 作为 KeyEventDispatcher 注册一次或多次，但这通常是不必要的，
 * 也不推荐这样做。
 *
 * @author David Mendenhall
 *
 * @see KeyboardFocusManager#addKeyEventDispatcher
 * @see KeyboardFocusManager#removeKeyEventDispatcher
 * @since 1.4
 */
@FunctionalInterface
public interface KeyEventDispatcher {

    /**
     * 当前 KeyboardFocusManager 调用此方法，请求此 KeyEventDispatcher 代表其分发指定的事件。
     * 此 KeyEventDispatcher 可以重新定位事件、消耗事件、自行分发事件或进行其他更改。此功能通常用于
     * 将 KeyEvent 分发到非焦点所有者的组件。例如，在无障碍环境中导航非焦点窗口的子组件时，这可能非常有用。
     * 注意，如果 KeyEventDispatcher 自行分发 KeyEvent，必须使用 <code>redispatchEvent</code>
     * 以防止当前 KeyboardFocusManager 递归地请求此 KeyEventDispatcher 再次分发事件。
     * <p>
     * 如果此方法的实现返回 <code>false</code>，则 KeyEvent 将传递给链中的下一个 KeyEventDispatcher，
     * 最后是当前的 KeyboardFocusManager。如果实现返回 <code>true</code>，则假定 KeyEvent 已经被分发
     * （尽管这不一定如此），当前的 KeyboardFocusManager 将不会对 KeyEvent 采取进一步的行动。在这种情况下，
     * <code>KeyboardFocusManager.dispatchEvent</code> 也应该返回 <code>true</code>。如果实现消耗了 KeyEvent
     * 但返回 <code>false</code>，则消耗的事件仍将传递给链中的下一个 KeyEventDispatcher。开发人员在
     * 将事件分发到目标之前，检查 KeyEvent 是否已被消耗是很重要的。默认情况下，当前的 KeyboardFocusManager
     * 不会分发已消耗的 KeyEvent。
     *
     * @param e 要分发的 KeyEvent
     * @return <code>true</code> 如果 KeyboardFocusManager 不应再对 KeyEvent 采取进一步的行动；
     *         否则返回 <code>false</code>
     * @see KeyboardFocusManager#redispatchEvent
     */
    boolean dispatchKeyEvent(KeyEvent e);
}
