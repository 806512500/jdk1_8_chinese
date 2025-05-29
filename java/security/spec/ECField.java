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
package java.security.spec;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * 此接口表示一个椭圆曲线（EC）有限域。
 * 所有专门的EC域必须实现此接口。
 *
 * @see ECFieldFp
 * @see ECFieldF2m
 *
 * @author Valerie Peng
 *
 * @since 1.5
 */
public interface ECField {
    /**
     * 返回字段大小（以位为单位）。注意：对于素数有限域ECFieldFp，返回素数p的位数。
     * 对于特征2有限域ECFieldF2m，返回m。
     * @return 字段大小（以位为单位）。
     */
    int getFieldSize();
}
