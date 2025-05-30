/*
 * Copyright (c) 2001, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * KeyEventPostProcessor 与当前的 KeyboardFocusManager 协作，以最终解决所有未被消费的 KeyEvent。注册到当前 KeyboardFocusManager 的 KeyEventPostProcessors 将在 KeyEvent 被分发并处理后接收这些事件。那些因为应用程序中没有组件拥有焦点而本应被丢弃的 KeyEvent 也将被转发到注册的 KeyEventPostProcessors。这将允许应用程序实现需要全局 KeyEvent 后处理的功能，例如菜单快捷键。
 * <p>
 * 注意，KeyboardFocusManager 本身实现了 KeyEventPostProcessor。默认情况下，当前的 KeyboardFocusManager 将是链中的最后一个 KeyEventPostProcessor。当前的 KeyboardFocusManager 不能完全取消注册为 KeyEventPostProcessor。然而，如果一个 KeyEventPostProcessor 报告说不应再对 KeyEvent 进行进一步的后处理，AWT 将认为事件已被完全处理，并且不会对事件采取任何进一步的行动。（虽然客户端代码可以将当前的 KeyboardFocusManager 注册为 KeyEventPostProcessor 一次或多次，但这通常是不必要的，也不推荐这样做。）
 *
 * @author David Mendenhall
 *
 * @see KeyboardFocusManager#addKeyEventPostProcessor
 * @see KeyboardFocusManager#removeKeyEventPostProcessor
 * @since 1.4
 */
@FunctionalInterface
public interface KeyEventPostProcessor {

    /**
     * 当前的 KeyboardFocusManager 调用此方法，请求此 KeyEventPostProcessor 执行 KeyEvent 最终解决所需的所有必要的后处理。调用此方法时，通常 KeyEvent 已经被分发并由其目标处理。然而，如果应用程序中没有组件当前拥有焦点，那么 KeyEvent 将不会被分发到任何组件。通常，KeyEvent 的后处理将用于实现需要全局 KeyEvent 后处理的功能，例如菜单快捷键。注意，如果一个 KeyEventPostProcessor 希望分发 KeyEvent，它必须使用 <code>redispatchEvent</code> 以防止 AWT 递归地请求此 KeyEventPostProcessor 再次对事件进行后处理。
     * <p>
     * 如果此方法的实现返回 <code>false</code>，则 KeyEvent 将传递给链中的下一个 KeyEventPostProcessor，最终传递给当前的 KeyboardFocusManager。如果实现返回 <code>true</code>，则认为 KeyEvent 已被完全处理（尽管情况不一定如此），AWT 将不会对 KeyEvent 采取任何进一步的行动。如果实现消费了 KeyEvent 但返回 <code>false</code>，消费的事件仍将传递给链中的下一个 KeyEventPostProcessor。开发人员在对 KeyEvent 进行任何后处理之前，检查 KeyEvent 是否已被消费是很重要的。默认情况下，当前的 KeyboardFocusManager 在响应已消费的 KeyEvent 时不会执行任何后处理。
     *
     * @param e 要后处理的 KeyEvent
     * @return <code>true</code> 如果 AWT 不应对 KeyEvent 采取任何进一步的行动；否则返回 <code>false</code>
     * @see KeyboardFocusManager#redispatchEvent
     */
    boolean postProcessKeyEvent(KeyEvent e);
}
