/*
 * 版权所有 (c) 2008, 2011, Oracle 和/或其附属公司。保留所有权利。
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

package java.lang.invoke;

/**
 * 这是一个占位符类。某些 HotSpot 实现需要看到它。
 */
final class InvokeDynamic {
    private InvokeDynamic() { throw new InternalError(); }  // 不要实例化
}
