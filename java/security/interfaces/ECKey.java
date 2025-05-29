/*
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
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
package java.security.interfaces;

import java.security.spec.ECParameterSpec;

/**
 * 椭圆曲线（EC）密钥的接口。
 *
 * @author Valerie Peng
 *
 * @since 1.5
 */
public interface ECKey {
    /**
     * 返回与此密钥关联的域参数。域参数
     * 要么在密钥生成时显式指定，要么隐式
     * 创建。
     * @return 关联的域参数。
     */
    ECParameterSpec getParams();
}
