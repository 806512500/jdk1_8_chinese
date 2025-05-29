/*
 * 版权所有 (c) 2000, 2006，Oracle 及/或其附属公司。保留所有权利。
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

package java.lang;

/**
 * 一个包含断言状态指令的集合（例如“在包 p 中启用断言”或“在类 c 中禁用断言”）。此类用于 JVM 与
 * <tt>java</tt> 命令行标志 <tt>-enableassertions</tt> (<tt>-ea</tt>) 和 <tt>-disableassertions</tt> (<tt>-da</tt>)
 * 暗含的断言状态指令进行通信。
 *
 * @since  1.4
 * @author Josh Bloch
 */
class AssertionStatusDirectives {
    /**
     * 需要启用或禁用断言的类。此数组中的字符串是完全限定的类名（例如，“com.xyz.foo.Bar”）。
     */
    String[] classes;

    /**
     * 与 <tt>classes</tt> 平行的数组，指示每个类是否应启用或禁用断言。对于 <tt>classEnabled[i]</tt> 的值为 <tt>true</tt>
     * 表示 <tt>classes[i]</tt> 命名的类应启用断言；值为 <tt>false</tt> 表示应禁用断言。
     * 此数组必须与 <tt>classes</tt> 具有相同数量的元素。
     *
     * <p>对于同一类的冲突指令，给定类的最后一个指令获胜。换句话说，如果字符串 <tt>s</tt> 在 <tt>classes</tt> 数组中出现多次
     * 且 <tt>i</tt> 是满足 <tt>classes[i].equals(s)</tt> 的最高整数，则 <tt>classEnabled[i]</tt>
     * 指示是否应在类 <tt>s</tt> 中启用断言。
     */
    boolean[] classEnabled;

    /**
     * 需要启用或禁用断言的包树。此数组中的字符串是完整或部分包名（例如，“com.xyz”或“com.xyz.foo”）。
     */
    String[] packages;

    /**
     * 与 <tt>packages</tt> 平行的数组，指示每个包树是否应启用或禁用断言。对于 <tt>packageEnabled[i]</tt> 的值为 <tt>true</tt>
     * 表示 <tt>packages[i]</tt> 命名的包树应启用断言；值为 <tt>false</tt> 表示应禁用断言。
     * 此数组必须与 <tt>packages</tt> 具有相同数量的元素。
     *
     * 对于同一包树的冲突指令，给定包树的最后一个指令获胜。换句话说，如果字符串 <tt>s</tt> 在 <tt>packages</tt> 数组中出现多次
     * 且 <tt>i</tt> 是满足 <tt>packages[i].equals(s)</tt> 的最高整数，则 <tt>packageEnabled[i]</tt>
     * 指示是否应在包树 <tt>s</tt> 中启用断言。
     */
    boolean[] packageEnabled;

    /**
     * 是否默认启用非系统类中的断言。
     */
    boolean deflt;
}