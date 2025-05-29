/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security;

/**
 * 该类定义了 {@code SecureRandom} 类的 <i>服务提供者接口</i> (<b>SPI</b>)。
 * 本类中的所有抽象方法都必须由希望提供加密强度伪随机数生成器实现的每个服务提供者实现。
 *
 *
 * @see SecureRandom
 * @since 1.2
 */

public abstract class SecureRandomSpi implements java.io.Serializable {

    private static final long serialVersionUID = -2991854161009191830L;

    /**
     * 重新播种此随机对象。给定的种子补充而不是替换现有的种子。因此，重复调用保证不会减少随机性。
     *
     * @param seed 种子。
     */
    protected abstract void engineSetSeed(byte[] seed);

    /**
     * 生成用户指定数量的随机字节。
     *
     * <p> 如果之前没有调用 {@code engineSetSeed}，则第一次调用此方法会强制此 SecureRandom 实现进行自播种。如果之前调用了 {@code engineSetSeed}，则不会发生自播种。
     *
     * @param bytes 要填充随机字节的数组。
     */
    protected abstract void engineNextBytes(byte[] bytes);

    /**
     * 返回指定数量的种子字节。此调用可用于播种其他随机数生成器。
     *
     * @param numBytes 要生成的种子字节数。
     *
     * @return 种子字节。
     */
     protected abstract byte[] engineGenerateSeed(int numBytes);
}
