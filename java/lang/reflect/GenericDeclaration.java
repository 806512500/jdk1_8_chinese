/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.reflect;

/**
 * 所有声明类型变量的实体的公共接口。
 *
 * @since 1.5
 */
public interface GenericDeclaration extends AnnotatedElement {
    /**
     * 返回一个 {@code TypeVariable} 对象数组，这些对象表示由该 {@code GenericDeclaration}
     * 对象表示的泛型声明所声明的类型变量，顺序与声明顺序相同。如果底层泛型声明没有声明类型变量，则返回长度为 0 的数组。
     *
     * @return 一个 {@code TypeVariable} 对象数组，表示由该泛型声明声明的类型变量
     * @throws GenericSignatureFormatError 如果该泛型声明的泛型签名不符合
     *     《Java&trade; 虚拟机规范》中指定的格式
     */
    public TypeVariable<?>[] getTypeParameters();
}
