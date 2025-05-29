/*
 * 版权所有 (c) 1998, 2006, Oracle 和/或其附属公司。保留所有权利。
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

/**
 * 提供了对 Java 编程语言设计至关重要的类。最重要的类是 {@code
 * Object}，它是类层次结构的根，以及 {@code
 * Class}，其实例在运行时代表类。
 *
 * <p>通常需要将原始类型值表示为对象。包装类 {@code Boolean}，
 * {@code Character}，{@code Integer}，{@code Long}，{@code Float}，
 * 和 {@code Double} 服务于这个目的。例如，类型为 {@code
 * Double} 的对象包含一个类型为 double 的字段，以这种方式表示该值，使得对它的引用可以存储在引用类型的变量中。这些类还提供了许多用于在原始值之间转换的方法，以及支持诸如 equals 和 hashCode 等标准方法。 {@code Void} 类是一个不可实例化的类，它持有一个表示 void 类型的 {@code Class} 对象的引用。
 *
 * <p>{@code Math} 类提供了常用的数学函数，如正弦、余弦和平方根。类 {@code
 * String}，{@code StringBuffer}，和 {@code StringBuilder} 类似地提供了常用的字符字符串操作。
 *
 * <p>类 {@code ClassLoader}，{@code Process}，{@code
 * ProcessBuilder}，{@code Runtime}，{@code SecurityManager}，和
 * {@code System} 提供了管理类的动态加载、外部进程的创建、主机环境查询（如一天中的时间）以及安全策略的强制执行等“系统操作”。
 *
 * <p>{@code Throwable} 类包括可能由 {@code throw} 语句抛出的对象。{@code Throwable}
 * 的子类表示错误和异常。
 *
 * <a name="charenc"></a>
 * <h3>字符编码</h3>
 *
 * {@link java.nio.charset.Charset
 * java.nio.charset.Charset} 类的规范描述了字符编码的命名约定以及每个 Java
 * 平台实现必须支持的标准编码集。
 *
 * @since JDK1.0
 */
package java.lang;
