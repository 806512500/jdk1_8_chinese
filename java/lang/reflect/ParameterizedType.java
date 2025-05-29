/*
 * 版权所有 (c) 2003, 2004, Oracle 和/或其附属公司。保留所有权利。
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
 * ParameterizedType 表示一个参数化类型，例如 Collection&lt;String&gt;。
 *
 * <p>当反射方法需要时，参数化类型第一次被创建，具体规定见本包。当创建参数化类型 p 时，p 实例化的泛型类型声明将被解析，并且 p 的所有类型参数将递归创建。有关类型变量的创建过程的详细信息，请参见 {@link java.lang.reflect.TypeVariable
 * TypeVariable}。重复创建参数化类型没有效果。
 *
 * <p>实现此接口的类的实例必须实现一个 equals() 方法，该方法将任何两个共享相同泛型类型声明且具有相等类型参数的实例视为相等。
 *
 * @since 1.5
 */
public interface ParameterizedType extends Type {
    /**
     * 返回一个 {@code Type} 对象数组，表示此类型的实际类型参数。
     *
     * <p>请注意，在某些情况下，返回的数组可能是空的。这可能发生在该类型表示嵌套在参数化类型内的非参数化类型时。
     *
     * @return 一个 {@code Type} 对象数组，表示此类型的实际类型参数
     * @throws TypeNotPresentException 如果任何实际类型参数引用了不存在的类型声明
     * @throws MalformedParameterizedTypeException 如果任何实际类型参数引用了由于任何原因无法实例化的参数化类型
     * @since 1.5
     */
    Type[] getActualTypeArguments();

    /**
     * 返回表示声明此类型的类或接口的 {@code Type} 对象。
     *
     * @return 表示声明此类型的类或接口的 {@code Type} 对象
     * @since 1.5
     */
    Type getRawType();

    /**
     * 返回一个 {@code Type} 对象，表示此类型所属的类型。例如，如果此类型是 {@code O<T>.I<S>}，则返回 {@code O<T>} 的表示。
     *
     * <p>如果此类型是顶级类型，则返回 {@code null}。
     *
     * @return 一个 {@code Type} 对象，表示此类型所属的类型。如果此类型是顶级类型，则返回 {@code null}
     * @throws TypeNotPresentException 如果所有者类型引用了不存在的类型声明
     * @throws MalformedParameterizedTypeException 如果所有者类型引用了由于任何原因无法实例化的参数化类型
     * @since 1.5
     */
    Type getOwnerType();
}
