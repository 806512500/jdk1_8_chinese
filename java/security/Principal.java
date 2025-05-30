/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security;

import javax.security.auth.Subject;

/**
 * 该接口表示主体的抽象概念，可以用于表示任何实体，如个人、公司和登录ID。
 *
 * @see java.security.cert.X509Certificate
 *
 * @author Li Gong
 */
public interface Principal {

    /**
     * 将此主体与指定的对象进行比较。如果传递的对象与此接口实现所表示的主体匹配，则返回true。
     *
     * @param another 要比较的主体。
     *
     * @return 如果传递的主体与此主体相同，则返回true，否则返回false。
     */
    public boolean equals(Object another);

    /**
     * 返回此主体的字符串表示形式。
     *
     * @return 此主体的字符串表示形式。
     */
    public String toString();

    /**
     * 返回此主体的哈希码。
     *
     * @return 此主体的哈希码。
     */
    public int hashCode();

    /**
     * 返回此主体的名称。
     *
     * @return 此主体的名称。
     */
    public String getName();

    /**
     * 如果指定的主体由此主体隐含，则返回true。
     *
     * <p>此方法的默认实现返回true，如果 {@code subject} 非空且包含至少一个与此主体相等的主体。
     *
     * <p>子类可以根据需要覆盖此方法，以提供不同的实现。
     *
     * @param subject {@code Subject}
     * @return 如果 {@code subject} 非空且由此主体隐含，则返回true，否则返回false。
     * @since 1.8
     */
    public default boolean implies(Subject subject) {
        if (subject == null)
            return false;
        return subject.getPrincipals().contains(this);
    }
}
