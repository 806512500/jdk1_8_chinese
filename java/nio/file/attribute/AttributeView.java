/*
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 提供文件系统中对象关联的非不透明值的只读或可更新<em>视图</em>的对象。此接口由特定的属性视图扩展或实现，这些视图定义了视图支持的属性。特定的属性视图通常会定义类型安全的方法来读取或更新它支持的属性。
 *
 * @since 1.7
 */

public interface AttributeView {
    /**
     * 返回属性视图的名称。
     *
     * @return 属性视图的名称
     */
    String name();
}
