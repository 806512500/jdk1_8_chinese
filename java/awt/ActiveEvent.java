/*
 * Copyright (c) 1997, 2002, Oracle and/or its affiliates. All rights reserved.
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
 * 一个接口，用于知道如何分发自己的事件。
 * 通过实现此接口，事件可以被放置在事件队列上，当事件被分发时，将调用其 <code>dispatch()</code> 方法，
 * 使用 <code>EventDispatchThread</code>。
 * <p>
 * 这是一种非常有用的机制，可以避免死锁。如果
 * 一个线程正在执行临界区（即，它已进入
 * 一个或多个监视器），调用其他同步代码可能会
 * 导致死锁。为了避免潜在的死锁，可以创建一个
 * <code>ActiveEvent</code> 以稍后运行第二段代码。如果存在监视器争用，
 * 第二个线程将简单地阻塞，直到第一个线程
 * 完成其工作并退出其监视器。
 * <p>
 * 由于安全原因，通常希望使用 <code>ActiveEvent</code>
 * 以避免从关键线程调用不可信代码。例如，
 * 对等实现可以使用此功能以避免从系统线程调用用户代码。这样做可以避免
 * 潜在的死锁和拒绝服务攻击。
 *
 * @author  Timothy Prinzing
 * @since   1.2
 */
public interface ActiveEvent {

    /**
     * 将事件分发到其目标，事件源的监听器，
     * 或执行此事件应该执行的操作。
     */
    public void dispatch();
}
