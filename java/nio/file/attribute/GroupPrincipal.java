/*
 * Copyright (c) 2007, 2009, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.nio.file.attribute;

/**
 * 一个表示<em>组身份</em>的 {@code UserPrincipal}，用于确定文件系统中对象的访问权限。组的准确定义是实现特定的，但通常，它表示为了管理目的而创建的身份，以确定组成员的访问权限。实体是否可以成为多个组的成员，以及组是否可以嵌套，这些都是实现特定的，因此未在此指定。
 *
 * @since 1.7
 *
 * @see UserPrincipalLookupService#lookupPrincipalByGroupName
 */

public interface GroupPrincipal extends UserPrincipal { }
