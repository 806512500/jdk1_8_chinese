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
package java.rmi.dgc;

/**
 * 一个租约包含一个唯一的虚拟机标识符和一个租约期限。Lease 对象用于请求和授予远程对象引用的租约。
 */
public final class Lease implements java.io.Serializable {

    /**
     * @serial 与此租约关联的虚拟机 ID。
     * @see #getVMID
     */
    private VMID vmid;

    /**
     * @serial 此租约的期限。
     * @see #getValue
     */
    private long value;
    /** 表示与 JDK 1.1.x 版本类的兼容性 */
    private static final long serialVersionUID = -5713411624328831948L;

    /**
     * 构造一个具有特定 VMID 和租约期限的租约。vmid 可能为 null。
     * @param id 与此租约关联的 VMID
     * @param duration 租约期限
     */
    public Lease(VMID id, long duration)
    {
        vmid = id;
        value = duration;
    }

    /**
     * 返回与此租约关联的客户端 VMID。
     * @return 客户端 VMID
     */
    public VMID getVMID()
    {
        return vmid;
    }

    /**
     * 返回租约期限。
     * @return 租约期限
     */
    public long getValue()
    {
        return value;
    }
}
