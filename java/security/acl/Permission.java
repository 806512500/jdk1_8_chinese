/*
 * Copyright (c) 1996, Oracle and/or its affiliates. All rights reserved.
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

package java.security.acl;


/**
 * 此接口表示一个权限，例如用于授予对资源的特定类型访问。
 *
 * @author Satish Dharmaraj
 */
public interface Permission {

    /**
     * 如果传递的对象与此接口中表示的权限匹配，则返回 true。
     *
     * @param another 要比较的 Permission 对象。
     *
     * @return 如果 Permission 对象相等，则返回 true，否则返回 false
     */
    public boolean equals(Object another);

    /**
     * 打印此权限的字符串表示形式。
     *
     * @return 权限的字符串表示形式。
     */
    public String toString();

}
