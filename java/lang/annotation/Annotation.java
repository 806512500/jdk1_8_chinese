/*
 * 版权所有 (c) 2003, 2013, Oracle 及/或其附属公司。保留所有权利。
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

package java.lang.annotation;

/**
 * 所有注解类型扩展的公共接口。请注意，手动扩展此接口的接口<em>不</em>定义注解类型。还请注意，此接口本身不定义注解类型。
 *
 * 更多关于注解类型的信息可以在《Java&trade; 语言规范》的第 9.6 节中找到。
 *
 * {@link java.lang.reflect.AnnotatedElement} 接口讨论了将注解类型从不可重复变为可重复时的兼容性问题。
 *
 * @author  Josh Bloch
 * @since   1.5
 */
public interface Annotation {
    /**
     * 如果指定的对象表示一个逻辑上等价于此注解的注解，则返回 true。换句话说，如果指定的对象是与此实例相同的注解类型的实例，并且其所有成员都等于此注解的相应成员（如下定义），则返回 true：
     * <ul>
     *    <li>两个对应的基本类型成员的值为 <tt>x</tt> 和 <tt>y</tt>，如果 <tt>x == y</tt>，则认为相等，除非它们的类型是 <tt>float</tt> 或 <tt>double</tt>。
     *
     *    <li>两个对应 <tt>float</tt> 成员的值为 <tt>x</tt> 和 <tt>y</tt>，如果 <tt>Float.valueOf(x).equals(Float.valueOf(y))</tt>，则认为相等。
     *    （与 <tt>==</tt> 操作符不同，NaN 被认为等于自身，而 <tt>0.0f</tt> 不等于 <tt>-0.0f</tt>。）
     *
     *    <li>两个对应 <tt>double</tt> 成员的值为 <tt>x</tt> 和 <tt>y</tt>，如果 <tt>Double.valueOf(x).equals(Double.valueOf(y))</tt>，则认为相等。
     *    （与 <tt>==</tt> 操作符不同，NaN 被认为等于自身，而 <tt>0.0</tt> 不等于 <tt>-0.0</tt>。）
     *
     *    <li>两个对应 <tt>String</tt>、<tt>Class</tt>、枚举或注解类型成员的值为 <tt>x</tt> 和 <tt>y</tt>，如果 <tt>x.equals(y)</tt>，则认为相等。（请注意，对于注解类型成员，此定义是递归的。）
     *
     *    <li>两个对应数组类型成员 <tt>x</tt> 和 <tt>y</tt>，如果 <tt>Arrays.equals(x, y)</tt>，则认为相等，适用于 {@link java.util.Arrays#equals} 的适当重载。
     * </ul>
     *
     * @return 如果指定的对象表示一个逻辑上等价于此注解的注解，则返回 true，否则返回 false
     */
    boolean equals(Object obj);

    /**
     * 返回此注解的哈希码，如下定义：
     *
     * <p>注解的哈希码是其成员（包括具有默认值的成员）的哈希码之和，如下定义：
     *
     * 注解成员的哈希码是（127 乘以成员名的哈希码，由 {@link String#hashCode()} 计算）XOR 成员值的哈希码，如下定义：
     *
     * <p>成员值的哈希码取决于其类型：
     * <ul>
     * <li>原始值 <tt><i>v</i></tt> 的哈希码等于
     *     <tt><i>WrapperType</i>.valueOf(<i>v</i>).hashCode()</tt>，其中
     *     <tt><i>WrapperType</i></tt> 是与 <tt><i>v</i></tt> 的原始类型对应的包装类型（{@link Byte}，
     *     {@link Character}，{@link Double}，{@link Float}，{@link Integer}，
     *     {@link Long}，{@link Short}，或 {@link Boolean}）。
     *
     * <li>字符串、枚举、类或注解成员值 <tt><i>v</i></tt> 的哈希码通过调用
     *     <tt><i>v</i>.hashCode()</tt> 计算。（在注解成员值的情况下，这是一个递归定义。）
     *
     * <li>数组成员值的哈希码通过调用
     *     适当的 {@link java.util.Arrays#hashCode(long[]) Arrays.hashCode}
     *     重载计算。（每个原始类型有一个重载，对象引用类型有一个重载。）
     * </ul>
     *
     * @return 此注解的哈希码
     */
    int hashCode();

    /**
     * 返回此注解的字符串表示形式。表示形式的细节取决于实现，但以下可以视为典型：
     * <pre>
     *   &#064;com.acme.util.Name(first=Alfred, middle=E., last=Neuman)
     * </pre>
     *
     * @return 此注解的字符串表示形式
     */
    String toString();

    /**
     * 返回此注解的注解类型。
     * @return 此注解的注解类型
     */
    Class<? extends Annotation> annotationType();
}
