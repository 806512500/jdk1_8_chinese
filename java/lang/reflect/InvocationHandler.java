/*
 * 版权所有 (c) 1999, 2006, Oracle 和/或其附属公司。保留所有权利。
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

package java.lang.reflect;

/**
 * {@code InvocationHandler} 是由代理实例的 <i>调用处理器</i> 实现的接口。
 *
 * <p>每个代理实例都有一个关联的调用处理器。当代理实例上的方法被调用时，方法调用会被编码并分派给其调用处理器的 {@code invoke} 方法。
 *
 * @author      Peter Jones
 * @see         Proxy
 * @since       1.3
 */
public interface InvocationHandler {

    /**
     * 处理代理实例上的方法调用并返回结果。当关联的调用处理器的代理实例上的方法被调用时，此方法将被调用。
     *
     * @param   proxy 被调用方法的代理实例。
     *
     * @param   method 与代理实例上被调用的接口方法相对应的 {@code Method} 实例。{@code Method} 对象的声明类将是该方法声明所在的接口，这可能是代理接口继承该方法的超接口。
     *
     * @param   args 包含传递给代理实例上的方法调用的参数值的对象数组，如果没有参数，则为 {@code null}。原始类型参数被包装在适当的原始类型包装类的实例中，例如
     * {@code java.lang.Integer} 或 {@code java.lang.Boolean}。
     *
     * @return  从代理实例上的方法调用返回的值。如果接口方法的声明返回类型是原始类型，则此方法返回的值必须是相应的原始类型包装类的实例；否则，它必须是可以分配给声明返回类型的类型。如果此方法返回的值为
     * {@code null} 且接口方法的返回类型是原始类型，则代理实例上的方法调用将抛出 {@code NullPointerException}。如果此方法返回的值与上述接口方法的声明返回类型不兼容，
     * 代理实例上的方法调用将抛出 {@code ClassCastException}。
     *
     * @throws  Throwable 从代理实例上的方法调用抛出的异常。异常的类型必须可以分配给接口方法的 {@code throws} 子句中声明的任何异常类型，或者可以分配给未检查的异常类型
     * {@code java.lang.RuntimeException} 或 {@code java.lang.Error}。如果此方法抛出的检查异常不能分配给接口方法的 {@code throws} 子句中声明的任何异常类型，
     * 则代理实例上的方法调用将抛出包含此方法抛出的异常的 {@link UndeclaredThrowableException}。
     *
     * @see     UndeclaredThrowableException
     */
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable;
}
