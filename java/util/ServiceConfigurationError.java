/*
 * 版权所有 (c) 2005, 2006, Oracle 和/或其附属公司。保留所有权利。
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


/**
 * 当加载服务提供者时出现问题时抛出的错误。
 *
 * <p> 在以下情况下将抛出此错误：
 *
 * <ul>
 *
 *   <li> 提供者配置文件的格式违反了<a
 *   href="ServiceLoader.html#format">规范</a>； </li>
 *
 *   <li> 读取提供者配置文件时发生 {@link java.io.IOException IOException}； </li>
 *
 *   <li> 提供者配置文件中命名的具体提供者类找不到； </li>
 *
 *   <li> 具体提供者类不是服务类的子类； </li>
 *
 *   <li> 无法实例化具体提供者类；或
 *
 *   <li> 发生其他类型的错误。 </li>
 *
 * </ul>
 *
 *
 * @author Mark Reinhold
 * @since 1.6
 */

public class ServiceConfigurationError
    extends Error
{

    private static final long serialVersionUID = 74132770414881L;

    /**
     * 使用指定的消息构造一个新的实例。
     *
     * @param  msg  消息，或 <tt>null</tt> 如果没有消息
     *
     */
    public ServiceConfigurationError(String msg) {
        super(msg);
    }

    /**
     * 使用指定的消息和原因构造一个新的实例。
     *
     * @param  msg  消息，或 <tt>null</tt> 如果没有消息
     *
     * @param  cause  原因，或 <tt>null</tt> 如果原因不存在或未知
     */
    public ServiceConfigurationError(String msg, Throwable cause) {
        super(msg, cause);
    }

}