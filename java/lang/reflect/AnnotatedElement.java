
/*
 * 版权所有 (c) 2003, 2013, Oracle 和/或其附属公司。保留所有权利。
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

import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationFormatError;
import java.lang.annotation.Repeatable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import sun.reflect.annotation.AnnotationSupport;
import sun.reflect.annotation.AnnotationType;

/**
 * 表示当前在本 VM 中运行的程序的注解元素。此接口允许注解被反射性地读取。此接口中所有方法返回的注解都是不可变且可序列化的。此接口中方法返回的数组可以被调用者修改，而不会影响返回给其他调用者的数组。
 *
 * <p>{@link #getAnnotationsByType(Class)} 和 {@link
 * #getDeclaredAnnotationsByType(Class)} 方法支持元素上存在多个相同类型的注解。如果传递给这两个方法的参数是一个可重复的注解类型（JLS 9.6），那么这些方法将“穿透”容器注解（JLS 9.7），如果存在的话，返回容器中的注解。容器注解可能在编译时生成，以包装多个参数类型的注解。
 *
 * <p>在整个接口中使用了“直接存在”、“间接存在”、“存在”和“关联”等术语，以精确描述方法返回的注解：
 *
 * <ul>
 *
 * <li> 如果元素 <i>E</i> 有一个 {@code
 * RuntimeVisibleAnnotations} 或 {@code
 * RuntimeVisibleParameterAnnotations} 或 {@code
 * RuntimeVisibleTypeAnnotations} 属性，并且该属性包含 <i>A</i>，则注解 <i>A</i> 在元素 <i>E</i> 上“直接存在”。
 *
 * <li> 如果元素 <i>E</i> 有一个 {@code RuntimeVisibleAnnotations} 或
 * {@code RuntimeVisibleParameterAnnotations} 或 {@code RuntimeVisibleTypeAnnotations}
 * 属性，并且 <i>A</i> 的类型是可重复的，并且该属性包含一个值元素包含 <i>A</i> 且类型为 <i>A</i> 的类型容器的注解，则注解 <i>A</i> 在元素 <i>E</i> 上“间接存在”。
 *
 * <li> 注解 <i>A</i> 在元素 <i>E</i> 上“存在”，如果：
 *
 * <ul>
 *
 * <li><i>A</i> 直接存在于 <i>E</i> 上；或者
 *
 * <li> 没有 <i>A</i> 的类型的注解直接存在于
 * <i>E</i> 上，并且 <i>E</i> 是一个类，并且 <i>A</i> 的类型是可继承的，并且 <i>A</i> 存在于 <i>E</i> 的超类上。
 *
 * </ul>
 *
 * <li> 注解 <i>A</i> 与元素 <i>E</i>“关联”，如果：
 *
 * <ul>
 *
 * <li><i>A</i> 直接或间接存在于 <i>E</i> 上；或者
 *
 * <li> 没有 <i>A</i> 的类型的注解直接或间接
 * 存在于 <i>E</i> 上，并且 <i>E</i> 是一个类，并且 <i>A</i> 的类型是可继承的，并且 <i>A</i> 与 <i>E</i> 的超类关联。
 *
 * </ul>
 *
 * </ul>
 *
 * <p>下表总结了此接口中不同方法检测的注解存在类型。
 *
 * <table border>
 * <caption>不同 AnnotatedElement 方法检测的存在类型概览</caption>
 * <tr><th colspan=2></th><th colspan=4>存在类型</th>
 * <tr><th colspan=2>方法</th><th>直接存在</th><th>间接存在</th><th>存在</th><th>关联</th>
 * <tr><td align=right>{@code T}</td><td>{@link #getAnnotation(Class) getAnnotation(Class&lt;T&gt;)}
 * <td></td><td></td><td>X</td><td></td>
 * </tr>
 * <tr><td align=right>{@code Annotation[]}</td><td>{@link #getAnnotations getAnnotations()}
 * <td></td><td></td><td>X</td><td></td>
 * </tr>
 * <tr><td align=right>{@code T[]}</td><td>{@link #getAnnotationsByType(Class) getAnnotationsByType(Class&lt;T&gt;)}
 * <td></td><td></td><td></td><td>X</td>
 * </tr>
 * <tr><td align=right>{@code T}</td><td>{@link #getDeclaredAnnotation(Class) getDeclaredAnnotation(Class&lt;T&gt;)}
 * <td>X</td><td></td><td></td><td></td>
 * </tr>
 * <tr><td align=right>{@code Annotation[]}</td><td>{@link #getDeclaredAnnotations getDeclaredAnnotations()}
 * <td>X</td><td></td><td></td><td></td>
 * </tr>
 * <tr><td align=right>{@code T[]}</td><td>{@link #getDeclaredAnnotationsByType(Class) getDeclaredAnnotationsByType(Class&lt;T&gt;)}
 * <td>X</td><td>X</td><td></td><td></td>
 * </tr>
 * </table>
 *
 * <p>对于 {@code get[Declared]AnnotationsByType( Class <
 * T >)} 的调用，元素 <i>E</i> 上直接或间接存在的注解的顺序计算为，如果 <i>E</i> 上间接存在的注解在其容器注解的位置直接存在于 <i>E</i> 上，按照它们在容器注解的值元素中出现的顺序。
 *
 * <p>如果注解类型 <i>T</i> 最初不是可重复的，后来被修改为可重复的，有几项兼容性问题需要注意。
 *
 * <i>T</i> 的容器注解类型为 <i>TC</i>。
 *
 * <ul>
 *
 * <li> 将 <i>T</i> 修改为可重复的与 <i>T</i> 的现有使用和 <i>TC</i> 的现有使用在源代码和二进制级别上是兼容的。
 *
 * 即，对于源代码兼容性，类型为 <i>T</i> 或 <i>TC</i> 的注解的源代码仍然可以编译。对于二进制兼容性，类型为 <i>T</i> 或 <i>TC</i> 的注解的类文件（或类型 <i>T</i> 或 <i>TC</i> 的其他使用）如果在修改前可以链接到 <i>T</i> 的早期版本，那么它们也可以链接到修改后的版本。
 *
 * （在 <i>T</i> 被修改为正式可重复之前，注解类型 <i>TC</i> 可能非正式地充当容器注解类型。或者，当 <i>T</i> 被修改为可重复时，可以引入 <i>TC</i> 作为新类型。）
 *
 * <li> 如果注解类型 <i>TC</i> 存在于元素上，并且 <i>T</i> 被修改为可重复的，<i>TC</i> 作为其容器注解类型，则：
 *
 * <ul>
 *
 * <li> 对 <i>T</i> 的修改与 {@code get[Declared]Annotation(Class<T>)}（使用 <i>T</i> 或 <i>TC</i> 作为参数调用）和 {@code
 * get[Declared]Annotations()} 方法的行为兼容，因为这些方法的结果不会因为 <i>TC</i> 成为 <i>T</i> 的容器注解类型而改变。
 *
 * <li> 对 <i>T</i> 的修改会改变 {@code
 * get[Declared]AnnotationsByType(Class<T>)} 方法的结果，因为这些方法现在会将类型为 <i>TC</i> 的注解识别为 <i>T</i> 的容器注解，并“穿透”它以暴露类型为 <i>T</i> 的注解。
 *
 * </ul>
 *
 * <li> 如果类型为 <i>T</i> 的注解存在于元素上，并且 <i>T</i> 被修改为可重复的，并且更多类型为 <i>T</i> 的注解被添加到元素上：
 *
 * <ul>
 *
 * <li> 添加类型为 <i>T</i> 的注解在源代码和二进制级别上都是兼容的。
 *
 * <li> 添加类型为 <i>T</i> 的注解会改变 {@code get[Declared]Annotation(Class<T>)} 方法和 {@code
 * get[Declared]Annotations()} 方法的结果，因为这些方法现在只会看到元素上的容器注解，而不会看到类型为 <i>T</i> 的注解。
 *
 * <li> 添加类型为 <i>T</i> 的注解会改变 {@code get[Declared]AnnotationsByType(Class<T>)}
 * 方法的结果，因为它们的结果将暴露额外的类型为 <i>T</i> 的注解，而之前它们只暴露了一个类型为 <i>T</i> 的注解。
 *
 * </ul>
 *
 * </ul>
 *
 * <p>如果此接口中方法返回的注解（直接或间接）包含一个 {@link Class}-valued 成员，该成员引用的类在本 VM 中不可访问，尝试通过返回的注解上调用相关的 Class 返回方法读取该类将导致 {@link TypeNotPresentException}。
 *
 * <p>同样，尝试读取枚举值成员将导致 {@link EnumConstantNotPresentException}，如果注解中的枚举常量在枚举类型中不再存在。
 *
 * <p>如果注解类型 <i>T</i> 被（元）注解为带有 {@code @Repeatable} 注解，其值元素指示类型 <i>TC</i>，但 <i>TC</i> 没有声明一个返回类型为 <i>T</i>{@code []} 的 {@code value()} 方法，则将抛出类型为 {@link java.lang.annotation.AnnotationFormatError} 的异常。
 *
 * <p>最后，尝试读取定义已不兼容演化的成员将导致 {@link
 * java.lang.annotation.AnnotationTypeMismatchException} 或 {@link
 * java.lang.annotation.IncompleteAnnotationException}。
 *
 * @see java.lang.EnumConstantNotPresentException
 * @see java.lang.TypeNotPresentException
 * @see AnnotationFormatError
 * @see java.lang.annotation.AnnotationTypeMismatchException
 * @see java.lang.annotation.IncompleteAnnotationException
 * @since 1.5
 * @author Josh Bloch
 */
public interface AnnotatedElement {
    /**
     * 如果指定类型的注解“存在”于该元素上，则返回 true，否则返回 false。此方法主要用于方便访问标记注解。
     *
     * <p>此方法返回的布尔值等价于：
     * {@code getAnnotation(annotationClass) != null}
     *
     * <p>默认方法的主体被指定为上述代码。
     *
     * @param annotationClass 与注解类型对应的 Class 对象
     * @return 如果指定注解类型的注解存在于该元素上，则返回 true，否则返回 false
     * @throws NullPointerException 如果给定的注解类为 null
     * @since 1.5
     */
    default boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

               /**
     * 返回此元素的指定类型的注解，如果存在这样的注解，则返回，否则返回 null。
     *
     * @param <T> 要查询和返回的注解类型
     * @param annotationClass 与注解类型对应的 Class 对象
     * @return 如果此元素上存在指定的注解类型，则返回此元素的注解，否则返回 null
     * @throws NullPointerException 如果给定的注解类为 null
     * @since 1.5
     */
    <T extends Annotation> T getAnnotation(Class<T> annotationClass);

    /**
     * 返回此元素上存在的注解。
     *
     * 如果此元素上没有注解存在，返回值是一个长度为 0 的数组。
     *
     * 调用此方法的调用者可以自由修改返回的数组；它不会影响返回给其他调用者的数组。
     *
     * @return 此元素上的注解
     * @since 1.5
     */
    Annotation[] getAnnotations();

    /**
     * 返回与此元素关联的注解。
     *
     * 如果没有注解与此元素关联，返回值是一个长度为 0 的数组。
     *
     * 本方法与 {@link #getAnnotation(Class)} 的区别在于，本方法会检测其参数是否为 <em>可重复注解类型</em> (JLS 9.6)，如果是，则尝试通过“查看”容器注解来查找一个或多个该类型的注解。
     *
     * 调用此方法的调用者可以自由修改返回的数组；它不会影响返回给其他调用者的数组。
     *
     * @implSpec 默认实现首先调用 {@link #getDeclaredAnnotationsByType(Class)}，传递 {@code annotationClass} 作为参数。如果返回的数组长度大于零，则返回该数组。如果返回的数组长度为零，并且此 {@code AnnotatedElement} 是一个类且参数类型是可继承的注解类型，且此 {@code AnnotatedElement} 的超类不为 null，则返回结果是调用 {@link #getAnnotationsByType(Class)} 时传递 {@code annotationClass} 作为参数的结果。否则，返回一个长度为零的数组。
     *
     * @param <T> 要查询和返回的注解类型
     * @param annotationClass 与注解类型对应的 Class 对象
     * @return 如果与此元素关联，则返回此元素的所有指定注解类型，否则返回一个长度为零的数组
     * @throws NullPointerException 如果给定的注解类为 null
     * @since 1.8
     */
    default <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
         /*
          * 关联的定义：直接或间接存在 OR
          * 既不直接也不间接存在，并且元素是一个类，注解类型是可继承的，且注解类型与元素的超类关联。
          */
         T[] result = getDeclaredAnnotationsByType(annotationClass);

         if (result.length == 0 && // 既不直接也不间接存在
             this instanceof Class && // 元素是一个类
             AnnotationType.getInstance(annotationClass).isInherited()) { // 可继承
             Class<?> superClass = ((Class<?>) this).getSuperclass();
             if (superClass != null) {
                 // 确定注解是否与超类关联
                 result = superClass.getAnnotationsByType(annotationClass);
             }
         }

         return result;
     }

    /**
     * 如果此元素上直接存在指定类型的注解，则返回该注解，否则返回 null。
     *
     * 本方法忽略继承的注解。（如果此元素上没有直接存在的注解，则返回 null。）
     *
     * @implSpec 默认实现首先执行空检查，然后遍历 {@link #getDeclaredAnnotations} 的结果，返回第一个注解类型与参数类型匹配的注解。
     *
     * @param <T> 要查询和返回的注解类型
     * @param annotationClass 与注解类型对应的 Class 对象
     * @return 如果此元素上直接存在指定的注解类型，则返回此元素的注解，否则返回 null
     * @throws NullPointerException 如果给定的注解类为 null
     * @since 1.8
     */
    default <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
         Objects.requireNonNull(annotationClass);
         // 遍历所有直接存在的注解，寻找匹配的注解
         for (Annotation annotation : getDeclaredAnnotations()) {
             if (annotationClass.equals(annotation.annotationType())) {
                 // 运行时动态转换比仅编译时转换更健壮。
                 return annotationClass.cast(annotation);
             }
         }
         return null;
     }

    /**
     * 返回此元素的指定类型的注解，如果这些注解是 <em>直接存在</em> 或 <em>间接存在</em>。本方法忽略继承的注解。
     *
     * 如果没有指定的注解直接或间接存在于此元素上，返回值是一个长度为 0 的数组。
     *
     * 本方法与 {@link #getDeclaredAnnotation(Class)} 的区别在于，本方法会检测其参数是否为 <em>可重复注解类型</em> (JLS 9.6)，如果是，则尝试通过“查看”容器注解来查找一个或多个该类型的注解。
     *
     * 调用此方法的调用者可以自由修改返回的数组；它不会影响返回给其他调用者的数组。
     *
     * @implSpec 默认实现可能调用 {@link #getDeclaredAnnotation(Class)} 一次或多次，以查找直接存在的注解，并且如果注解类型是可重复的，以查找容器注解。如果发现注解类型 {@code annotationClass} 的注解既直接存在又间接存在，则调用 {@link #getDeclaredAnnotations()} 以确定返回数组中元素的顺序。
     *
     * <p>或者，默认实现可能调用 {@link #getDeclaredAnnotations()} 一次，并检查返回的数组中直接和间接存在的注解。调用 {@link #getDeclaredAnnotations()} 的结果假定与调用 {@link #getDeclaredAnnotation(Class)} 的结果一致。
     *
     * @param <T> 要查询和返回的注解类型
     * @param annotationClass 与注解类型对应的 Class 对象
     * @return 如果此元素上直接或间接存在指定的注解类型，则返回此元素的所有注解，否则返回一个长度为零的数组
     * @throws NullPointerException 如果给定的注解类为 null
     * @since 1.8
     */
    default <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        Objects.requireNonNull(annotationClass);
        return AnnotationSupport.
            getDirectlyAndIndirectlyPresent(Arrays.stream(getDeclaredAnnotations()).
                                            collect(Collectors.toMap(Annotation::annotationType,
                                                                     Function.identity(),
                                                                     ((first,second) -> first),
                                                                     LinkedHashMap::new)),
                                            annotationClass);
    }

                /**
     * 返回直接存在于此元素上的注解。
     * 此方法忽略继承的注解。
     *
     * 如果此元素上没有直接存在的注解，
     * 返回值是一个长度为0的数组。
     *
     * 该方法的调用者可以修改返回的数组；它不会影响返回给其他调用者的数组。
     *
     * @return 直接存在于此元素上的注解
     * @since 1.5
     */
    Annotation[] getDeclaredAnnotations();
}
