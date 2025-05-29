
/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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
package java.util.jar;

import java.util.SortedMap;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.IOException;
import java.beans.PropertyChangeListener;




/**
 * 将 JAR 文件转换为 Pack200 格式的打包流，或从打包流转换回 JAR 文件。
 * 请参阅网络传输格式 JSR 200 规范：
 * <a href=http://jcp.org/aboutJava/communityprocess/review/jsr200/index.html>http://jcp.org/aboutJava/communityprocess/review/jsr200/index.html</a>
 * <p>
 * 通常，打包引擎由应用程序开发人员用于在网站上部署或托管 JAR 文件。
 * 解包引擎由部署应用程序使用，将字节流转换回 JAR 格式。
 * <p>
 * 以下是一个使用打包器和解包器的示例：
 * <pre>{@code
 *    import java.util.jar.Pack200;
 *    import java.util.jar.Pack200.*;
 *    ...
 *    // 创建 Packer 对象
 *    Packer packer = Pack200.newPacker();
 *
 *    // 通过设置所需的属性来初始化状态
 *    Map p = packer.properties();
 *    // 选择更好的压缩编码，花费更多时间
 *    p.put(Packer.EFFORT, "7");  // 默认值为 "5"
 *    // 使用尽可能大的存档段（压缩率提高 >10%）。
 *    p.put(Packer.SEGMENT_LIMIT, "-1");
 *    // 重新排序文件以获得更好的压缩。
 *    p.put(Packer.KEEP_FILE_ORDER, Packer.FALSE);
 *    // 将修改时间统一为一个值。
 *    p.put(Packer.MODIFICATION_TIME, Packer.LATEST);
 *    // 忽略所有 JAR 解压请求，
 *    // 传输一个使用“存储”模式的单个请求。
 *    p.put(Packer.DEFLATE_HINT, Packer.FALSE);
 *    // 删除调试属性
 *    p.put(Packer.CODE_ATTRIBUTE_PFX+"LineNumberTable", Packer.STRIP);
 *    // 如果属性未识别，则抛出错误
 *    p.put(Packer.UNKNOWN_ATTRIBUTE, Packer.ERROR);
 *    // 保持一个类文件不压缩：
 *    p.put(Packer.PASS_FILE_PFX+0, "mutants/Rogue.class");
 *    try {
 *        JarFile jarFile = new JarFile("/tmp/testref.jar");
 *        FileOutputStream fos = new FileOutputStream("/tmp/test.pack");
 *        // 调用打包器
 *        packer.pack(jarFile, fos);
 *        jarFile.close();
 *        fos.close();
 *
 *        File f = new File("/tmp/test.pack");
 *        FileOutputStream fostream = new FileOutputStream("/tmp/test.jar");
 *        JarOutputStream jostream = new JarOutputStream(fostream);
 *        Unpacker unpacker = Pack200.newUnpacker();
 *        // 调用解包器
 *        unpacker.unpack(f, jostream);
 *        // 必须显式关闭输出。
 *        jostream.close();
 *    } catch (IOException ioe) {
 *        ioe.printStackTrace();
 *    }
 * }</pre>
 * <p>
 * 使用 gzip 压缩的 Pack200 文件可以托管在 HTTP/1.1 网络服务器上。
 * 部署应用程序可以使用 "Accept-Encoding=pack200-gzip"。这
 * 表示服务器客户端应用程序希望获取一个使用 Pack200 编码并进一步使用 gzip 压缩的文件版本。请
 * 参阅 <a href="{@docRoot}/../technotes/guides/deployment/deployment-guide/pack200.html">Java 部署指南</a> 了解更多信息和
 * 技巧。
 * <p>
 * 除非另有说明，否则将 <tt>null</tt> 参数传递给此类的构造函数或方法将导致抛出 {@link NullPointerException}。
 *
 * @author John Rose
 * @author Kumar Srinivasan
 * @since 1.5
 */
public abstract class Pack200 {
    private Pack200() {} // 防止实例化

    // Pack200 类的静态方法。
    /**
     * 获取实现 Packer 的类的新实例。
     * <ul>
     * <li><p>如果定义了系统属性 <tt>java.util.jar.Pack200.Packer</tt>，
     * 则其值被视为具体实现类的完全限定名，该类必须实现 Packer。
     * 该类将被加载并实例化。如果此过程失败，则抛出未指定的错误。</p></li>
     *
     * <li><p>如果未使用系统属性指定实现，则实例化系统默认实现类，
     * 并返回结果。</p></li>
     * </ul>
     *
     * <p>注意：如果多个线程同时使用返回的对象，则不能保证其正确运行。
     * 多线程应用程序应分配多个打包引擎，或使用锁序列化一个引擎的使用。
     *
     * @return  新分配的 Packer 引擎。
     */
    public synchronized static Packer newPacker() {
        return (Packer) newInstance(PACK_PROVIDER);
    }


    /**
     * 获取实现 Unpacker 的类的新实例。
     * <ul>
     * <li><p>如果定义了系统属性 <tt>java.util.jar.Pack200.Unpacker</tt>，
     * 则其值被视为具体实现类的完全限定名，该类必须实现 Unpacker。
     * 该类将被加载并实例化。如果此过程失败，则抛出未指定的错误。</p></li>
     *
     * <li><p>如果未使用系统属性指定实现，则实例化系统默认实现类，
     * 并返回结果。</p></li>
     * </ul>
     *
     * <p>注意：如果多个线程同时使用返回的对象，则不能保证其正确运行。
     * 多线程应用程序应分配多个解包引擎，或使用锁序列化一个引擎的使用。
     *
     * @return  新分配的 Unpacker 引擎。
     */

    public static Unpacker newUnpacker() {
        return (Unpacker) newInstance(UNPACK_PROVIDER);
    }

    // 接口
    /**
     * 打包引擎对输入的 JAR 文件应用各种转换，
     * 使打包流通过 gzip 或 zip 等压缩器高度可压缩。可以使用 {@link #newPacker} 获取引擎实例。

                 * 高度压缩是通过使用 JSR 200 规范中描述的多种技术实现的。
     * 其中一些技术包括排序、重新排序和常量池的共定位。
     * <p>
     * 打包引擎初始化为以下属性描述的初始状态。
     * 可以通过获取引擎属性（使用 {@link #properties}）并存储
     * 修改后的属性到映射上来操作初始状态。
     * 资源文件将不作任何更改地传递。
     * 类文件将不包含相同的字节，因为解包器
     * 可以自由地更改类文件的次要特征，如常量池顺序。
     * 然而，类文件将在语义上相同，
     * 如《Java&trade; 虚拟机规范》中所指定的。
     * <p>
     * 默认情况下，打包器不会改变 JAR 元素的顺序。
     * 同样，每个 JAR 元素的修改时间和压缩提示也将保持不变。
     * （任何其他 ZIP 存档信息，如提供 Unix 文件权限的额外属性
     * 都会丢失。）
     * <p>
     * 请注意，打包和解包 JAR 通常会改变 JAR 中类文件的字节内容。这意味着打包
     * 和解包通常会使依赖于 JAR 元素字节图像的任何数字签名失效。为了同时对 JAR 进行签名
     * 和打包，您必须首先打包和解包 JAR 以“规范化”它，然后在解包的 JAR 元素上计算签名，
     * 最后重新打包已签名的 JAR。
     * 两个打包步骤都应
     * 使用完全相同的选项，段限制也可能需要设置为“-1”，以防止随着类文件大小的轻微变化
     * 段边界发生意外变化。
     * <p>
     * （这里解释了为什么这样做有效：打包器对任何类文件结构进行的任何重新排序
     * 都是幂等的，因此第二次打包不会改变第一次打包产生的顺序。
     * 此外，根据 JSR 200 规范，解包器保证为任何给定的存档元素传输顺序
     * 生成特定的字节图像。）
     * <p>
     * 为了保持向后兼容性，打包文件的版本设置为适应输入 JAR 文件中包含的类文件。换句话说，
     * 如果类文件是最新版本，打包文件版本也将是最新版本；反之，如果类文件版本也是最旧版本，
     * 打包文件版本也将是最旧版本。对于中间版本的类文件，将使用相应的打包文件版本。
     * 例如：
     *    如果输入的 JAR 文件仅包含 1.5（或更早）的类文件，则会生成 1.5 兼容的打包文件。对于没有类文件的存档也是如此。
     *    如果输入的 JAR 文件包含 1.6 版本的类文件，则打包文件版本将设置为 1.6。
     * <p>
     * 注意：除非另有说明，否则将 <tt>null</tt> 参数传递给此类的构造函数或方法将导致抛出 {@link NullPointerException}。
     * <p>
     * @since 1.5
     */
    public interface Packer {
        /**
         * 此属性是一个数字，表示每个存档段的估计目标大小 N
         * （以字节为单位）。如果单个输入文件需要超过 N 字节，
         * 它将被分配自己的存档段。
         * <p>
         * 作为一个特殊情况，值 -1 将生成一个包含所有输入文件的单个大段，而值 0 将
         * 为每个类生成一个段。
         * 较大的存档段会导致较少的碎片和更好的压缩，但处理它们需要更多的内存。
         * <p>
         * 每个段的大小是通过计算要传输到段中的每个输入文件的大小，以及其名称和
         * 其他传输属性的大小来估算的。
         * <p>
         * 默认值为 -1，这意味着打包器将始终创建单个段输出文件。在生成极大型输出文件的情况下，
         * 强烈建议用户使用分段或将输入文件分解为较小的 JAR。
         * <p>
         * 一个 10Mb 的 JAR 在没有此限制的情况下打包通常会
         * 压缩约 10% 更小，但打包器可能需要更大的 Java 堆（大约是段限制的十倍）。
         */
        String SEGMENT_LIMIT    = "pack.segment.limit";

        /**
         * 如果此属性设置为 {@link #TRUE}，打包器将按源存档中的原始顺序传输所有元素。
         * <p>
         * 如果设置为 {@link #FALSE}，打包器可以重新排序元素，
         * 并且还可以删除对 Java 应用程序没有用处的 JAR 目录条目。
         * （通常这可以实现更好的压缩。）
         * <p>
         * 默认值为 {@link #TRUE}，这保留了输入信息，
         * 但可能导致传输的存档比必要时更大。
         */
        String KEEP_FILE_ORDER = "pack.keep.file.order";


        /**
         * 如果此属性设置为单个十进制数字，打包器将在压缩存档时使用指示的压缩级别。
         * 级别 1 可能会产生稍大的大小和更快的压缩速度，
         * 而级别 9 将花费更长时间但可能产生更好的压缩效果。
         * <p>
         * 特殊值 0 指示打包器直接复制原始 JAR 文件，不进行压缩。JSR 200
         * 标准要求任何解包器理解这种特殊情况作为整个存档的直接传递。
         * <p>
         * 默认值为 5，投入适度的时间以
         * 产生合理的压缩效果。
         */
        String EFFORT           = "pack.effort";


                    /**
         * 如果将此属性设置为 {@link #TRUE} 或 {@link #FALSE}，打包器
         * 将在输出存档中相应地设置压缩提示，并且
         * 不会传输存档元素的个别压缩提示。
         * <p>
         * 如果将此属性设置为特殊字符串 {@link #KEEP}，打包器
         * 将尝试为输入存档的每个可用元素确定一个独立的压缩提示，并分别传输此提示。
         * <p>
         * 默认值为 {@link #KEEP}，这保留了输入信息，
         * 但可能导致传输的存档比必要时更大。
         * <p>
         * 由解包器实现
         * 根据提示采取行动，以适当地压缩解包后的 JAR 的元素。
         * <p>
         * ZIP 或 JAR 元素的压缩提示表示
         * 该元素是被压缩还是直接存储。
         */
        String DEFLATE_HINT     = "pack.deflate.hint";

        /**
         * 如果将此属性设置为特殊字符串 {@link #LATEST}，
         * 打包器将尝试确定原始存档中所有可用条目的最新修改时间，
         * 或每个段中所有可用条目的最新修改时间。
         * 这个单一值将作为段的一部分传输，并应用于每个段中的所有条目，{@link #SEGMENT_LIMIT}。
         * <p>
         * 这可以略微减少传输的存档大小，
         * 但会将所有安装文件的日期设置为单个日期。
         * <p>
         * 如果将此属性设置为特殊字符串 {@link #KEEP}，
         * 打包器将为每个输入元素传输一个单独的修改时间。
         * <p>
         * 默认值为 {@link #KEEP}，这保留了输入信息，
         * 但可能导致传输的存档比必要时更大。
         * <p>
         * 由解包器实现采取行动，以适当地
         * 设置其输出文件中每个元素的修改时间。
         * @see #SEGMENT_LIMIT
         */
        String MODIFICATION_TIME        = "pack.modification.time";

        /**
         * 表示文件应逐字节传递，不进行压缩。通过指定
         * 具有不同字符串后缀的附加属性，可以指定多个文件，
         * 以形成具有共同前缀的属性族。
         * <p>
         * 除了将系统文件分隔符替换为 JAR 文件
         * 分隔符 '/' 外，没有路径名转换。
         * <p>
         * 结果文件名必须与 JAR 文件中的出现完全匹配。
         * <p>
         * 如果属性值是目录名，则该目录下的所有文件也将被传递。
         * <p>
         * 示例：
         * <pre>{@code
         *     Map p = packer.properties();
         *     p.put(PASS_FILE_PFX+0, "mutants/Rogue.class");
         *     p.put(PASS_FILE_PFX+1, "mutants/Wolverine.class");
         *     p.put(PASS_FILE_PFX+2, "mutants/Storm.class");
         *     # 传递整个目录层次结构中的所有文件：
         *     p.put(PASS_FILE_PFX+3, "police/");
         * }</pre>
         */
        String PASS_FILE_PFX            = "pack.pass.file.";

        /// 属性控制。

        /**
         * 表示在遇到包含未知属性的类文件时应采取的行动。可能的值是字符串 {@link #ERROR}，
         * {@link #STRIP} 和 {@link #PASS}。
         * <p>
         * 字符串 {@link #ERROR} 表示整个打包操作
         * 将失败，并抛出类型为 <code>IOException</code> 的异常。
         * 字符串
         * {@link #STRIP} 表示该属性将被删除。
         * 字符串
         * {@link #PASS} 表示整个类文件将被传递（就像它是资源文件一样），
         * 不进行压缩，并带有适当的警告。
         * 这是此属性的默认值。
         * <p>
         * 示例：
         * <pre>{@code
         *     Map p = pack200.getProperties();
         *     p.put(UNKNOWN_ATTRIBUTE, ERROR);
         *     p.put(UNKNOWN_ATTRIBUTE, STRIP);
         *     p.put(UNKNOWN_ATTRIBUTE, PASS);
         * }</pre>
         */
        String UNKNOWN_ATTRIBUTE        = "pack.unknown.attribute";

        /**
         * 与类属性名称连接时，
         * 表示该属性的格式，
         * 使用 JSR 200 规范中指定的布局语言。
         * <p>
         * 例如，此选项的效果是内置的：
         * <code>pack.class.attribute.SourceFile=RUH</code>。
         * <p>
         * 特殊字符串 {@link #ERROR}，{@link #STRIP} 和 {@link #PASS} 也
         * 允许使用，含义与 {@link #UNKNOWN_ATTRIBUTE} 相同。
         * 这为用户提供了一种请求特定属性被
         * 拒绝、删除或逐位传递（不进行类压缩）的方法。
         * <p>
         * 可能会使用如下代码来支持 JCOV 的属性：
         * <pre><code>
         *     Map p = packer.properties();
         *     p.put(CODE_ATTRIBUTE_PFX+"CoverageTable",       "NH[PHHII]");
         *     p.put(CODE_ATTRIBUTE_PFX+"CharacterRangeTable", "NH[PHPOHIIH]");
         *     p.put(CLASS_ATTRIBUTE_PFX+"SourceID",           "RUH");
         *     p.put(CLASS_ATTRIBUTE_PFX+"CompilationID",      "RUH");
         * </code></pre>
         * <p>
         * 可能会使用如下代码来删除调试属性：
         * <pre><code>
         *     Map p = packer.properties();
         *     p.put(CODE_ATTRIBUTE_PFX+"LineNumberTable",    STRIP);
         *     p.put(CODE_ATTRIBUTE_PFX+"LocalVariableTable", STRIP);
         *     p.put(CLASS_ATTRIBUTE_PFX+"SourceFile",        STRIP);
         * </code></pre>
         */
        String CLASS_ATTRIBUTE_PFX      = "pack.class.attribute.";


                    /**
         * 当与字段属性名称连接时，
         * 表示该属性的格式。
         * 例如，此选项的效果是内置的：
         * <code>pack.field.attribute.Deprecated=</code>。
         * 也允许使用特殊字符串 {@link #ERROR}、{@link #STRIP} 和 {@link #PASS}。
         * @see #CLASS_ATTRIBUTE_PFX
         */
        String FIELD_ATTRIBUTE_PFX      = "pack.field.attribute.";

        /**
         * 当与方法属性名称连接时，
         * 表示该属性的格式。
         * 例如，此选项的效果是内置的：
         * <code>pack.method.attribute.Exceptions=NH[RCH]</code>。
         * 也允许使用特殊字符串 {@link #ERROR}、{@link #STRIP} 和 {@link #PASS}。
         * @see #CLASS_ATTRIBUTE_PFX
         */
        String METHOD_ATTRIBUTE_PFX     = "pack.method.attribute.";

        /**
         * 当与代码属性名称连接时，
         * 表示该属性的格式。
         * 例如，此选项的效果是内置的：
         * <code>pack.code.attribute.LocalVariableTable=NH[PHOHRUHRSHH]</code>。
         * 也允许使用特殊字符串 {@link #ERROR}、{@link #STRIP} 和 {@link #PASS}。
         * @see #CLASS_ATTRIBUTE_PFX
         */
        String CODE_ATTRIBUTE_PFX       = "pack.code.attribute.";

        /**
         * 解包器的进度百分比，由解包器定期更新。
         * 0 - 100 的值是正常的，-1 表示停滞。
         * 可以通过轮询此属性的值来监控进度。
         * <p>
         * 至少，解包器必须在打包操作开始时将进度设置为 0，
         * 在结束时设置为 100。
         */
        String PROGRESS                 = "pack.progress";

        /** 字符串 "keep"，某些属性的可能值。
         * @see #DEFLATE_HINT
         * @see #MODIFICATION_TIME
         */
        String KEEP  = "keep";

        /** 字符串 "pass"，某些属性的可能值。
         * @see #UNKNOWN_ATTRIBUTE
         * @see #CLASS_ATTRIBUTE_PFX
         * @see #FIELD_ATTRIBUTE_PFX
         * @see #METHOD_ATTRIBUTE_PFX
         * @see #CODE_ATTRIBUTE_PFX
         */
        String PASS  = "pass";

        /** 字符串 "strip"，某些属性的可能值。
         * @see #UNKNOWN_ATTRIBUTE
         * @see #CLASS_ATTRIBUTE_PFX
         * @see #FIELD_ATTRIBUTE_PFX
         * @see #METHOD_ATTRIBUTE_PFX
         * @see #CODE_ATTRIBUTE_PFX
         */
        String STRIP = "strip";

        /** 字符串 "error"，某些属性的可能值。
         * @see #UNKNOWN_ATTRIBUTE
         * @see #CLASS_ATTRIBUTE_PFX
         * @see #FIELD_ATTRIBUTE_PFX
         * @see #METHOD_ATTRIBUTE_PFX
         * @see #CODE_ATTRIBUTE_PFX
         */
        String ERROR = "error";

        /** 字符串 "true"，某些属性的可能值。
         * @see #KEEP_FILE_ORDER
         * @see #DEFLATE_HINT
         */
        String TRUE = "true";

        /** 字符串 "false"，某些属性的可能值。
         * @see #KEEP_FILE_ORDER
         * @see #DEFLATE_HINT
         */
        String FALSE = "false";

        /** 字符串 "latest"，某些属性的可能值。
         * @see #MODIFICATION_TIME
         */
        String LATEST = "latest";

        /**
         * 获取此引擎的属性集。
         * 该集是一个“实时视图”，因此更改其内容会立即影响打包器引擎，
         * 引擎的更改（如进度指示）会立即在映射中可见。
         *
         * <p>属性映射可能包含预定义的实现特定和默认属性。鼓励用户
         * 了解信息并充分理解修改现有属性的影响。
         * <p>
         * 实现特定的属性以前缀为 <tt>com.</tt> 或类似前缀的包名开头。
         * 所有以 <tt>pack.</tt> 和 <tt>unpack.</tt> 开头的属性名称
         * 都保留用于此 API。
         * <p>
         * 未知属性可能会被忽略或以未指定的错误拒绝，无效条目可能会导致
         * 抛出未指定的错误。
         *
         * <p>
         * 返回的映射实现了所有可选的 {@link SortedMap} 操作
         * @return 属性键字符串到属性值的排序关联。
         */
        SortedMap<String,String> properties();

        /**
         * 接受一个 JarFile 并将其转换为 Pack200 归档文件。
         * <p>
         * 关闭输入但不关闭输出。（Pack200 归档文件是可追加的。）
         * @param in 一个 JarFile
         * @param out 一个 OutputStream
         * @exception IOException 如果遇到错误。
         */
        void pack(JarFile in, OutputStream out) throws IOException ;

        /**
         * 接受一个 JarInputStream 并将其转换为 Pack200 归档文件。
         * <p>
         * 关闭输入但不关闭输出。（Pack200 归档文件是可追加的。）
         * <p>
         * JAR 清单文件及其包含目录的修改时间和压缩提示属性不可用。
         *
         * @see #MODIFICATION_TIME
         * @see #DEFLATE_HINT
         * @param in 一个 JarInputStream
         * @param out 一个 OutputStream
         * @exception IOException 如果遇到错误。
         */
        void pack(JarInputStream in, OutputStream out) throws IOException ;

        /**
         * 为属性映射上的属性更改事件注册监听器。
         * 这通常用于应用程序更新进度条。
         *
         * <p>此方法的默认实现不执行任何操作且没有副作用。</p>
         *
         * <p><b>警告：</b>此方法从所有不包含
         * {@code java.beans} 包的 Java SE 子集配置文件的接口声明中省略。</p>

                     * @see #properties
         * @see #PROGRESS
         * @param listener  An object to be invoked when a property is changed.
         * @deprecated The dependency on {@code PropertyChangeListener} creates
         *             a significant impediment to future modularization of the
         *             Java platform. This method will be removed in a future
         *             release.
         *             Applications that need to monitor progress of the packer
         *             can poll the value of the {@link #PROGRESS PROGRESS}
         *             property instead.
         */
        @Deprecated
        default void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        /**
         * Remove a listener for PropertyChange events, added by
         * the {@link #addPropertyChangeListener}.
         *
         * <p> The default implementation of this method does nothing and has
         * no side-effects.</p>
         *
         * <p><b>WARNING:</b> This method is omitted from the interface
         * declaration in all subset Profiles of Java SE that do not include
         * the {@code java.beans} package. </p>
         *
         * @see #addPropertyChangeListener
         * @param listener  The PropertyChange listener to be removed.
         * @deprecated The dependency on {@code PropertyChangeListener} creates
         *             a significant impediment to future modularization of the
         *             Java platform. This method will be removed in a future
         *             release.
         */
        @Deprecated
        default void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }

    /**
     * The unpacker engine converts the packed stream to a JAR file.
     * An instance of the engine can be obtained
     * using {@link #newUnpacker}.
     * <p>
     * Every JAR file produced by this engine will include the string
     * "<tt>PACK200</tt>" as a zip file comment.
     * This allows a deployer to detect if a JAR archive was packed and unpacked.
     * <p>
     * Note: Unless otherwise noted, passing a <tt>null</tt> argument to a
     * constructor or method in this class will cause a {@link NullPointerException}
     * to be thrown.
     * <p>
     * This version of the unpacker is compatible with all previous versions.
     * @since 1.5
     */
    public interface Unpacker {

        /** The string "keep", a possible value for certain properties.
         * @see #DEFLATE_HINT
         */
        String KEEP  = "keep";

        /** The string "true", a possible value for certain properties.
         * @see #DEFLATE_HINT
         */
        String TRUE = "true";

        /** The string "false", a possible value for certain properties.
         * @see #DEFLATE_HINT
         */
        String FALSE = "false";

        /**
         * Property indicating that the unpacker should
         * ignore all transmitted values for DEFLATE_HINT,
         * replacing them by the given value, {@link #TRUE} or {@link #FALSE}.
         * The default value is the special string {@link #KEEP},
         * which asks the unpacker to preserve all transmitted
         * deflation hints.
         */
        String DEFLATE_HINT      = "unpack.deflate.hint";



        /**
         * The unpacker's progress as a percentage, as periodically
         * updated by the unpacker.
         * Values of 0 - 100 are normal, and -1 indicates a stall.
         * Progress can be monitored by polling the value of this
         * property.
         * <p>
         * At a minimum, the unpacker must set progress to 0
         * at the beginning of a packing operation, and to 100
         * at the end.
         */
        String PROGRESS         = "unpack.progress";

        /**
         * Get the set of this engine's properties. This set is
         * a "live view", so that changing its
         * contents immediately affects the Packer engine, and
         * changes from the engine (such as progress indications)
         * are immediately visible in the map.
         *
         * <p>The property map may contain pre-defined implementation
         * specific and default properties.  Users are encouraged to
         * read the information and fully understand the implications,
         * before modifying pre-existing properties.
         * <p>
         * Implementation specific properties are prefixed with a
         * package name associated with the implementor, beginning
         * with <tt>com.</tt> or a similar prefix.
         * All property names beginning with <tt>pack.</tt> and
         * <tt>unpack.</tt> are reserved for use by this API.
         * <p>
         * Unknown properties may be ignored or rejected with an
         * unspecified error, and invalid entries may cause an
         * unspecified error to be thrown.
         *
         * @return A sorted association of option key strings to option values.
         */
        SortedMap<String,String> properties();

        /**
         * Read a Pack200 archive, and write the encoded JAR to
         * a JarOutputStream.
         * The entire contents of the input stream will be read.
         * It may be more efficient to read the Pack200 archive
         * to a file and pass the File object, using the alternate
         * method described below.
         * <p>
         * Closes its input but not its output.  (The output can accumulate more elements.)
         * @param in an InputStream.
         * @param out a JarOutputStream.
         * @exception IOException if an error is encountered.
         */
        void unpack(InputStream in, JarOutputStream out) throws IOException;

        /**
         * Read a Pack200 archive, and write the encoded JAR to
         * a JarOutputStream.
         * <p>
         * Does not close its output.  (The output can accumulate more elements.)
         * @param in a File.
         * @param out a JarOutputStream.
         * @exception IOException if an error is encountered.
         */
        void unpack(File in, JarOutputStream out) throws IOException;

        /**
         * Registers a listener for PropertyChange events on the properties map.
         * This is typically used by applications to update a progress bar.
         *
         * <p> The default implementation of this method does nothing and has
         * no side-effects.</p>
         *
         * <p><b>WARNING:</b> This method is omitted from the interface
         * declaration in all subset Profiles of Java SE that do not include
         * the {@code java.beans} package. </p>
         *
         * @see #properties
         * @see #PROGRESS
         * @param listener  An object to be invoked when a property is changed.
         * @deprecated The dependency on {@code PropertyChangeListener} creates
         *             a significant impediment to future modularization of the
         *             Java platform. This method will be removed in a future
         *             release.
         *             Applications that need to monitor progress of the
         *             unpacker can poll the value of the {@link #PROGRESS
         *             PROGRESS} property instead.
         */
        @Deprecated
        default void addPropertyChangeListener(PropertyChangeListener listener) {
        }


                    /**
         * 移除通过 {@link #addPropertyChangeListener} 添加的属性更改事件监听器。
         *
         * <p> 该方法的默认实现不执行任何操作，也没有副作用。</p>
         *
         * <p><b>警告：</b> 该方法在不包含 {@code java.beans} 包的 Java SE 子集配置文件的接口声明中被省略。</p>
         *
         * @see #addPropertyChangeListener
         * @param listener  要移除的属性更改监听器。
         * @deprecated 对 {@code PropertyChangeListener} 的依赖对 Java 平台的未来模块化造成了重大障碍。此方法将在未来的版本中移除。
         */
        @Deprecated
        default void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }

    // 私有内容....

    private static final String PACK_PROVIDER = "java.util.jar.Pack200.Packer";
    private static final String UNPACK_PROVIDER = "java.util.jar.Pack200.Unpacker";

    private static Class<?> packerImpl;
    private static Class<?> unpackerImpl;

    private synchronized static Object newInstance(String prop) {
        String implName = "(unknown)";
        try {
            Class<?> impl = (PACK_PROVIDER.equals(prop))? packerImpl: unpackerImpl;
            if (impl == null) {
                // 第一次时，我们必须决定使用哪个类。
                implName = java.security.AccessController.doPrivileged(
                    new sun.security.action.GetPropertyAction(prop,""));
                if (implName != null && !implName.equals(""))
                    impl = Class.forName(implName);
                else if (PACK_PROVIDER.equals(prop))
                    impl = com.sun.java.util.jar.pack.PackerImpl.class;
                else
                    impl = com.sun.java.util.jar.pack.UnpackerImpl.class;
            }
            // 我们有了一个类。现在实例化它。
            return impl.newInstance();
        } catch (ClassNotFoundException e) {
            throw new Error("未找到类: " + implName +
                                ":\n请检查属性文件中的 " + prop + " 属性。", e);
        } catch (InstantiationException e) {
            throw new Error("无法实例化: " + implName +
                                ":\n请检查属性文件中的 " + prop + " 属性。", e);
        } catch (IllegalAccessException e) {
            throw new Error("无法访问类: " + implName +
                                ":\n请检查属性文件中的 " + prop + " 属性。", e);
        }
    }

}
