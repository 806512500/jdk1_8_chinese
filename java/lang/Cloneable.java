/*
 * Copyright (c) 1995, 2004, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

/**
 * 一个类实现 <code>Cloneable</code> 接口以向 {@link java.lang.Object#clone()} 方法表明
 * 该方法可以对该类的实例进行字段到字段的复制。
 * <p>
 * 对于不实现 <code>Cloneable</code> 接口的实例调用 Object 的 clone 方法会导致抛出
 * <code>CloneNotSupportedException</code> 异常。
 * <p>
 * 按照惯例，实现此接口的类应覆盖 <tt>Object.clone</tt>（该方法是受保护的）并提供一个公共方法。
 * 有关覆盖此方法的详细信息，请参阅 {@link java.lang.Object#clone()}。
 * <p>
 * 请注意，此接口不包含 <tt>clone</tt> 方法。
 * 因此，仅凭实现此接口并不能保证可以克隆一个对象。即使通过反射调用 clone 方法，也没有保证它会成功。
 *
 * @author  未署名
 * @see     java.lang.CloneNotSupportedException
 * @see     java.lang.Object#clone()
 * @since   JDK1.0
 */
public interface Cloneable {
}
