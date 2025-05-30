/*
 * Copyright (c) 1996, 1998, Oracle and/or its affiliates. All rights reserved.
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

package java.beans;

/**
 * 在某些情况下，bean 可能在没有 GUI 的服务器上运行。此接口可用于查询 bean
 * 以确定它是否绝对需要 GUI，并告知 bean 是否有可用的 GUI。
 * <p>
 * 此接口适用于高级开发人员，对于普通的简单 bean 并不需要。
 * 为了避免混淆最终用户，我们避免使用 getXXX setXXX 设计模式来命名这些方法。
 */

public interface Visibility {

    /**
     * 确定此 bean 是否需要 GUI。
     *
     * @return 如果 bean 为了完成其工作绝对需要 GUI，则返回 true。
     */
    boolean needsGui();

    /**
     * 此方法指示 bean 不要使用 GUI。
     */
    void dontUseGui();

    /**
     * 此方法指示 bean 可以使用 GUI。
     */
    void okToUseGui();

    /**
     * 确定此 bean 是否正在避免使用 GUI。
     *
     * @return 如果 bean 当前正在避免使用 GUI，则返回 true。
     *   例如，由于调用了 dontUseGui()。
     */
    boolean avoidingGui();

}
