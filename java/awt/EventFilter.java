/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
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

interface EventFilter {

    /**
     * 枚举类型，表示 <code>acceptEvent(AWTEvent ev)</code> 方法的可能返回值。
     * @see EventDispatchThread#pumpEventsForFilter
     */
    static enum FilterAction {
        /**
         * ACCEPT 表示此过滤器不过滤事件，允许其他活动的过滤器继续处理该事件。如果所有活动的过滤器都接受该事件，则由 <code>EventDispatchThread</code> 分发该事件。
         * @see EventDispatchThread#pumpEventsForFilter
         */
        ACCEPT,
        /**
         * REJECT 表示此过滤器过滤事件。不再查询其他过滤器，且事件不会由 <code>EventDispatchThread</code> 分发。
         * @see EventDispatchThread#pumpEventsForFilter
         */
        REJECT,
        /**
         * ACCEPT_IMMEDIATELY 表示此过滤器不过滤事件，不再查询其他过滤器，并立即处理该事件，由 <code>EventDispatchThread</code> 分发。
         * 不建议使用 ACCEPT_IMMEDIATELY，因为可能还有未查询的活动过滤器不接受此事件。它主要用于模态过滤器。
         * @see EventDispatchThread#pumpEventsForFilter
         * @see ModalEventFilter
         */
        ACCEPT_IMMEDIATELY
    };

    FilterAction acceptEvent(AWTEvent ev);
}
