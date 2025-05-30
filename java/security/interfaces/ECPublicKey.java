/*
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
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
package java.security.interfaces;

import java.security.PublicKey;
import java.security.spec.ECPoint;

/**
 * 椭圆曲线（EC）公钥的接口。
 *
 * @author Valerie Peng
 *
 *
 * @see PublicKey
 * @see ECKey
 * @see java.security.spec.ECPoint
 *
 * @since 1.5
 */
public interface ECPublicKey extends PublicKey, ECKey {

   /**
    * 用于指示序列化兼容性的类指纹。
    */
    static final long serialVersionUID = -3314988629879632826L;

    /**
     * 返回公钥点 W。
     * @return 公钥点 W。
     */
    ECPoint getW();
}
