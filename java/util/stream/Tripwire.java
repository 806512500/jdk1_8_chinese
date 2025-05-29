/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
package java.util.stream;

import java.security.AccessController;
import java.security.PrivilegedAction;

import sun.util.logging.PlatformLogger;

/**
 * 用于检测在 {@code java.util.stream} 类中无意使用的装箱操作的工具类。检测是否开启或关闭取决于系统属性
 * {@code org.openjdk.java.util.stream.tripwire} 是否被 {@link Boolean#getBoolean(String)} 认为是 {@code true}。
 * 通常情况下，对于生产使用，应将其关闭。
 *
 * @apiNote
 * 典型的使用方式是在装箱代码中执行以下操作：
 * <pre>{@code
 *     if (Tripwire.ENABLED)
 *         Tripwire.trip(getClass(), "{0} calling Sink.OfInt.accept(Integer)");
 * }</pre>
 *
 * @since 1.8
 */
final class Tripwire {
    private static final String TRIPWIRE_PROPERTY = "org.openjdk.java.util.stream.tripwire";

    /** 是否应启用调试检查？ */
    static final boolean ENABLED = AccessController.doPrivileged(
            (PrivilegedAction<Boolean>) () -> Boolean.getBoolean(TRIPWIRE_PROPERTY));

    private Tripwire() { }

    /**
     * 使用 {@code PlatformLogger.getLogger(className)} 生成日志警告，使用提供的消息。{@code trippingClass} 的类名将作为消息的第一个参数。
     *
     * @param trippingClass 生成消息的类名
     * @param msg 由 {@link PlatformLogger} 期望的消息格式字符串
     */
    static void trip(Class<?> trippingClass, String msg) {
        PlatformLogger.getLogger(trippingClass.getName()).warning(msg, trippingClass.getName());
    }
}
