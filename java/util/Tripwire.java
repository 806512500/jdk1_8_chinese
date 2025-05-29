/*
 * 版权所有 (c) 2012, 2013, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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
package java.util;

import sun.util.logging.PlatformLogger;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * 用于检测 {@code java.util} 类中无意使用的装箱的工具类。检测是否开启基于系统属性
 * {@code org.openjdk.java.util.stream.tripwire} 是否根据 {@link Boolean#getBoolean(String)} 被认为是 {@code true}。
 * 通常情况下，对于生产使用应关闭此功能。
 *
 * @apiNote
 * 典型用法是装箱代码执行如下操作：
 * <pre>{@code
 *     if (Tripwire.ENABLED)
 *         Tripwire.trip(getClass(), "{0} 调用 PrimitiveIterator.OfInt.nextInt()");
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
     * 使用 {@code PlatformLogger.getLogger(className)} 生成日志警告，
     * 使用提供的消息。{@code trippingClass} 的类名将作为消息的第一个参数。
     *
     * @param trippingClass 生成消息的类名
     * @param msg 由 {@link PlatformLogger} 期望的消息格式字符串
     */
    static void trip(Class<?> trippingClass, String msg) {
        PlatformLogger.getLogger(trippingClass.getName()).warning(msg, trippingClass.getName());
    }
}