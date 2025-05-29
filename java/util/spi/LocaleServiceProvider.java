
/*
 * 版权所有 (c) 2005, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.util.spi;

import java.util.Locale;

/**
 * <p>
 * 这是所有区域敏感服务提供者接口（SPIs）的超类。
 * <p>
 * 区域敏感服务提供者接口是与 <code>java.text</code> 和 <code>java.util</code> 包中的区域敏感类相对应的接口。
 * 这些接口使得可以构建区域敏感对象并检索这些包的本地化名称。<code>java.text</code> 和 <code>java.util</code> 包中的
 * 区域敏感工厂方法和名称检索方法使用提供者接口的实现来提供超出 Java 运行时环境本身支持的区域集的支持。
 *
 * <h3>区域敏感服务提供者实现的打包</h3>
 * 这些区域敏感服务的实现使用 <a href="../../../../technotes/guides/extensions/index.html">Java 扩展机制</a>
 * 作为已安装的扩展进行打包。提供者通过在资源目录 META-INF/services 中使用完全限定的提供者接口类名作为文件名的
 * 提供者配置文件来标识自己。该文件应包含每个提供者类名的完全限定名，每行一个。行由换行符 ('\n')、回车符 ('\r')
 * 或回车符后立即跟换行符终止。每个名称周围的空格和制表符以及空白行将被忽略。注释字符是 '#' ('\u0023')；在每一行中，
 * 所有跟随第一个注释字符的字符都将被忽略。文件必须使用 UTF-8 编码。
 * <p>
 * 如果某个具体的提供者类在多个配置文件中被命名，或者在同一配置文件中被命名多次，那么重复项将被忽略。命名特定提供者的
 * 配置文件不必与提供者本身位于同一个 jar 文件或其他分发单元中。提供者必须从最初查询以定位配置文件的类加载器中可访问；
 * 这不一定是加载文件的类加载器。
 * <p>
 * 例如，<code>java.text.spi.DateFormatProvider</code> 类的实现应采用包含以下文件的 jar 文件形式：
 * <pre>
 * META-INF/services/java.text.spi.DateFormatProvider
 * </pre>
 * 文件 <code>java.text.spi.DateFormatProvider</code> 应包含如下行：
 * <pre>
 * <code>com.foo.DateFormatProviderImpl</code>
 * </pre>
 * 这是实现 <code>DateFormatProvider</code> 的类的完全限定类名。
 * <h4>区域敏感服务的调用</h4>
 * <p>
 * <code>java.text</code> 和 <code>java.util</code> 包中的区域敏感工厂方法和名称检索方法在需要时调用服务提供者方法以支持请求的区域。
 * 这些方法首先检查 Java 运行时环境本身是否支持请求的区域，如果可用则使用其支持。否则，它们调用已安装的提供者
 * <code>{@link #isSupportedLocale(Locale) isSupportedLocale}</code> 方法以查找支持请求区域的提供者。如果找到这样的提供者，
 * 则调用其其他方法以获取请求的对象或名称。在检查区域是否受支持时，默认情况下忽略 <a href="../Locale.html#def_extensions">
 * 区域的扩展</a>。（如果还应检查区域的扩展，则必须重写 <code>isSupportedLocale</code> 方法。）如果 Java 运行时环境本身
 * 或已安装的提供者都不支持请求的区域，这些方法将遍历候选区域列表，并对每个候选区域重复可用性检查，直到找到匹配项。
 * 创建候选区域列表所使用的算法与 <code>ResourceBundle</code> 默认使用的算法相同（参见
 * <code>{@link java.util.ResourceBundle.Control#getCandidateLocales getCandidateLocales}</code> 了解详细信息）。
 * 即使从候选列表中解析出区域，返回请求对象或名称的方法也将使用包括 <code>Locale</code> 扩展在内的原始请求区域调用。
 * Java 运行时环境必须支持所有区域敏感服务的根区域，以确保此过程终止。
 * <p>
 * 名称提供者（而不是其他对象的提供者）即使对于它们声称支持的区域（通过将其包含在 <code>getAvailableLocales</code> 的返回值中）
 * 也允许对某些名称请求返回 null。同样，Java 运行时环境本身可能不支持它支持的所有区域的所有名称。这是因为请求名称的对象集
 * 可能很大且随时间变化，因此不总是可行的完全覆盖它们。如果 Java 运行时环境或提供者返回 null 而不是名称，查找将按照上述方式
 * 进行，就像该区域不受支持一样。
 * <p>
 * 从 JDK8 开始，可以通过使用 "java.locale.providers" 系统属性来配置区域敏感服务的搜索顺序。此系统属性声明了用户
 * 查找区域敏感服务的首选顺序，以逗号分隔。它仅在 Java 运行时启动时读取，因此稍后调用 System.setProperty() 不会影响顺序。
 * <p>
 * 例如，如果在属性中指定了以下内容：
 * <pre>
 * java.locale.providers=SPI,JRE
 * </pre>
 * 其中 "SPI" 表示已安装的 SPI 提供者实现的区域敏感服务，"JRE" 表示 Java 运行时环境中的区域敏感服务，将首先查找 SPI 提供者中的
 * 区域敏感服务。
 * <p>
 * 还有两个其他可能的区域敏感服务提供者，即 "CLDR"，它是基于 Unicode 联盟的 <a href="http://cldr.unicode.org/">CLDR 项目</a> 的提供者，
 * 以及 "HOST"，它是反映底层操作系统中用户的自定义设置的提供者。这两个提供者可能不可用，具体取决于 Java 运行时环境的实现。
 * 指定 "JRE,SPI" 与默认行为相同，与之前的版本兼容。
 *
 * @since        1.6
 */
public abstract class LocaleServiceProvider {

                /**
     * 唯一的构造函数。 （通常由子类构造函数隐式调用。）
     */
    protected LocaleServiceProvider() {
    }

    /**
     * 返回此区域设置服务提供者可以提供本地化对象或名称的所有区域设置的数组。此信息用于组成
     * 区域设置依赖服务（如 {@code DateFormat.getAvailableLocales()}）的 {@code getAvailableLocales()} 值。
     *
     * <p>此方法返回的数组不应包含两个或多个仅在扩展名上有所不同的 {@code Locale} 对象。
     *
     * @return 一个数组，包含此区域设置服务提供者可以提供本地化对象或名称的所有区域设置。
     */
    public abstract Locale[] getAvailableLocales();

    /**
     * 如果给定的 {@code locale} 被此区域设置服务提供者支持，则返回 {@code true}。给定的 {@code locale} 可能包含
     * <a href="../Locale.html#def_extensions">扩展名</a>，这些扩展名应在支持确定中予以考虑。
     *
     * <p>默认实现如果给定的 {@code locale} 与 {@link #getAvailableLocales()} 返回的可用 {@code Locale} 之一相等，
     * 则返回 {@code true}，同时忽略给定的 {@code locale} 和可用区域设置中的任何扩展名。具体的区域设置服务
     * 提供者实现如果对 {@code Locale} 扩展名敏感，应覆盖此方法。例如，{@code DecimalFormatSymbolsProvider} 实现
     * 需要检查给定的 {@code locale} 中的扩展名，以查看是否指定了任何数字系统并可以支持。然而，{@code CollatorProvider}
     * 实现可能不受任何特定数字系统的影响，在这种情况下，应忽略数字系统的扩展名。
     *
     * @param locale 要测试的 {@code Locale}
     * @return 如果给定的 {@code locale} 被此提供者支持，则返回 {@code true}；否则返回 {@code false}。
     * @throws NullPointerException
     *         如果给定的 {@code locale} 为 {@code null}
     * @see Locale#hasExtensions()
     * @see Locale#stripExtensions()
     * @since 1.8
     */
    public boolean isSupportedLocale(Locale locale) {
        locale = locale.stripExtensions(); // 如果 locale == null，则抛出 NPE
        for (Locale available : getAvailableLocales()) {
            if (locale.equals(available.stripExtensions())) {
                return true;
            }
        }
        return false;
    }
}
