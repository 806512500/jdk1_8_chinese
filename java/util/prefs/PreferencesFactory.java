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

package java.util.prefs;
import java.util.*;

/**
 * 一个生成 Preferences 对象的工厂对象。新的 {@link Preferences} 实现的提供者应提供相应的
 * <tt>PreferencesFactory</tt> 实现，以便新的 <tt>Preferences</tt> 实现可以替代平台特定的默认实现。
 *
 * <p><strong>此类仅适用于 <tt>Preferences</tt> 实现者。正常使用 <tt>Preferences</tt> 设施的用户无需查阅此文档。</strong>
 *
 * @author  Josh Bloch
 * @see     Preferences
 * @since   1.4
 */
public interface PreferencesFactory {
    /**
     * 返回系统根偏好节点。多次调用此方法将返回相同的对象引用。
     * @return 系统根偏好节点
     */
    Preferences systemRoot();

    /**
     * 返回与调用用户相对应的用户根偏好节点。在服务器中，返回值通常取决于某些隐式客户端上下文。
     * @return 与调用用户相对应的用户根偏好节点
     */
    Preferences userRoot();
}
