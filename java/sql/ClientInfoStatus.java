/*
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
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

package java.sql;

import java.util.*;

/**
 * 枚举类型，表示通过调用 <code>Connection.setClientInfo</code> 设置属性失败的原因
 * @since 1.6
 */

public enum ClientInfoStatus {

    /**
     * 客户端信息属性由于某些未知原因无法设置
     * @since 1.6
     */
    REASON_UNKNOWN,

    /**
     * 指定的客户端信息属性名称不是已识别的属性名称。
     * @since 1.6
     */
    REASON_UNKNOWN_PROPERTY,

    /**
     * 指定的客户端信息属性值无效。
     * @since 1.6
     */
    REASON_VALUE_INVALID,

    /**
     * 指定的客户端信息属性值太大。
     * @since 1.6
     */
    REASON_VALUE_TRUNCATED
}
