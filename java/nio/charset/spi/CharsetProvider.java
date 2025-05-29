/*
 * 版权所有 (c) 2000, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.nio.charset.spi;

import java.nio.charset.Charset;
import java.util.Iterator;


/**
 * 字符集服务提供者类。
 *
 * <p> 字符集提供者是此类的具体子类，具有无参数构造函数和一些关联的字符集实现类。字符集提供者可以作为扩展安装在 Java 平台的实例中，即放置在通常的扩展目录中的 jar 文件。提供者也可以通过将其添加到 applet 或应用程序类路径中或通过其他平台特定的方式提供。字符集提供者通过当前线程的 {@link java.lang.Thread#getContextClassLoader() 上下文类加载器} 查找。
 *
 * <p> 字符集提供者通过资源目录 <tt>META-INF/services</tt> 中名为 <tt>java.nio.charset.spi.CharsetProvider</tt> 的提供者配置文件来标识自己。文件应包含每个具体字符集提供者类的全限定名称列表，每行一个。行由换行符 (<tt>'\n'</tt>)、回车符 (<tt>'\r'</tt>) 或紧跟回车符的换行符终止。每个名称周围的空格和制表符以及空白行将被忽略。注释字符是 <tt>'#'</tt> (<tt>'&#92;u0023'</tt>)；在每一行中，第一个注释字符之后的所有字符都将被忽略。文件必须使用 UTF-8 编码。
 *
 * <p> 如果特定的具体字符集提供者类在多个配置文件中被命名，或者在同一配置文件中被命名多次，则重复项将被忽略。命名特定提供者的配置文件不必与提供者本身位于同一个 jar 文件或其他分发单元中。提供者必须从最初查询以定位配置文件的类加载器中可访问；这不一定是加载文件的类加载器。 </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 专家小组
 * @since 1.4
 *
 * @see java.nio.charset.Charset
 */

public abstract class CharsetProvider {

    /**
     * 初始化一个新的字符集提供者。
     *
     * @throws  SecurityException
     *          如果安装了安全经理并且它拒绝
     *          {@link RuntimePermission}<tt>("charsetProvider")</tt>
     */
    protected CharsetProvider() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(new RuntimePermission("charsetProvider"));
    }

    /**
     * 创建一个迭代器，用于迭代此提供者支持的字符集。此方法用于实现 {@link
     * java.nio.charset.Charset#availableCharsets Charset.availableCharsets}
     * 方法。
     *
     * @return  新的迭代器
     */
    public abstract Iterator<Charset> charsets();

    /**
     * 检索给定字符集名称的字符集。
     *
     * @param  charsetName
     *         请求的字符集的名称；可以是规范名称或别名
     *
     * @return  命名字符集的字符集对象，
     *          或 <tt>null</tt> 如果此提供者不支持命名字符集
     */
    public abstract Charset charsetForName(String charsetName);

}
