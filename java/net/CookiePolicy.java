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

package java.net;

/**
 * CookiePolicy 实现决定应接受哪些 cookie 以及应拒绝哪些 cookie。提供了三种预定义的策略实现，即 ACCEPT_ALL、ACCEPT_NONE 和 ACCEPT_ORIGINAL_SERVER。
 *
 * <p>详情请参阅 RFC 2965 第 3.3 节和第 7 节。
 *
 * @author Edward Wang
 * @since 1.6
 */
public interface CookiePolicy {
    /**
     * 一种预定义的策略，接受所有 cookie。
     */
    public static final CookiePolicy ACCEPT_ALL = new CookiePolicy(){
        public boolean shouldAccept(URI uri, HttpCookie cookie) {
            return true;
        }
    };

    /**
     * 一种预定义的策略，不接受任何 cookie。
     */
    public static final CookiePolicy ACCEPT_NONE = new CookiePolicy(){
        public boolean shouldAccept(URI uri, HttpCookie cookie) {
            return false;
        }
    };

    /**
     * 一种预定义的策略，仅接受来自原始服务器的 cookie。
     */
    public static final CookiePolicy ACCEPT_ORIGINAL_SERVER  = new CookiePolicy(){
        public boolean shouldAccept(URI uri, HttpCookie cookie) {
            if (uri == null || cookie == null)
                return false;
            return HttpCookie.domainMatches(cookie.getDomain(), uri.getHost());
        }
    };


    /**
     * 将被调用以确定是否应接受此 cookie。
     *
     * @param uri       用于咨询接受策略的 URI
     * @param cookie    问题中的 HttpCookie 对象
     * @return          如果应接受此 cookie，则返回 {@code true}；否则，返回 {@code false}
     */
    public boolean shouldAccept(URI uri, HttpCookie cookie);
}
