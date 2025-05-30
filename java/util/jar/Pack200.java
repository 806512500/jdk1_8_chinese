
/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package java.util.jar;

import java.util.SortedMap;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.IOException;
import java.beans.PropertyChangeListener;




/**
 * 将 JAR 文件转换为 Pack200 格式的打包流或从打包流转换回 JAR 文件。
 * 请参阅网络传输格式 JSR 200 规范：
 * <a href=http://jcp.org/aboutJava/communityprocess/review/jsr200/index.html>http://jcp.org/aboutJava/communityprocess/review/jsr200/index.html</a>
 * <p>
 * 通常，打包引擎由应用程序开发人员用于在网站上部署或托管 JAR 文件。
 * 解包引擎由部署应用程序用于将字节流转换回 JAR 格式。
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
 *    // 选择更好的压缩编码，但需要更多时间
 *    p.put(Packer.EFFORT, "7");  // 默认值为 "5"
 *    // 使用尽可能大的存档段（压缩率提高超过 10%）。
 *    p.put(Packer.SEGMENT_LIMIT, "-1");
 *    // 重新排序文件以获得更好的压缩。
 *    p.put(Packer.KEEP_FILE_ORDER, Packer.FALSE);
 *    // 将修改时间统一为一个值。
 *    p.put(Packer.MODIFICATION_TIME, Packer.LATEST);
 *    // 忽略所有 JAR 压缩请求，
 *    // 传输一个请求以使用“存储”模式。
 *    p.put(Packer.DEFLATE_HINT, Packer.FALSE);
 *    // 删除调试属性
 *    p.put(Packer.CODE_ATTRIBUTE_PFX+"LineNumberTable", Packer.STRIP);
 *    // 如果属性未识别，则抛出错误
 *    p.put(Packer.UNKNOWN_ATTRIBUTE, Packer.ERROR);
 *    // 传递一个未压缩的类文件：
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
 * 部署应用程序可以使用 "Accept-Encoding=pack200-gzip"。这表示客户端应用程序希望获取一个使用 Pack200 编码并进一步使用 gzip 压缩的文件版本。请参阅
 * <a href="{@docRoot}/../technotes/guides/deployment/deployment-guide/pack200.html">Java 部署指南</a> 以获取更多详细信息和技巧。
 * <p>
 * 除非另有说明，否则将 <tt>null</tt> 作为参数传递给此类的构造函数或方法将导致抛出 {@link NullPointerException}。
 *
 * @author John Rose
 * @author Kumar Srinivasan
 * @since 1.5
 */
public abstract class Pack200 {
    private Pack200() {} // 防止实例化

    // Pack200 类的静态方法。
    /**
     * 获取实现 Packer 接口的新实例。
     * <ul>
     * <li><p>如果定义了系统属性 <tt>java.util.jar.Pack200.Packer</tt>，
     * 则其值被视为具体实现类的完全限定名，该类必须实现 Packer。
     * 该类将被加载并实例化。如果此过程失败，则会抛出未指定的错误。</p></li>
     *
     * <li><p>如果未通过系统属性指定实现，则实例化系统默认实现类，
     * 并返回结果。</p></li>
     * </ul>
     *
     * <p>注意：如果多个线程同时使用返回的对象，则不能保证其正确运行。
     * 多线程应用程序应分配多个打包引擎，或者使用锁序列化一个引擎的使用。
     *
     * @return 一个新分配的 Packer 引擎。
     */
    public synchronized static Packer newPacker() {
        return (Packer) newInstance(PACK_PROVIDER);
    }


    /**
     * 获取实现 Unpacker 接口的新实例。
     * <ul>
     * <li><p>如果定义了系统属性 <tt>java.util.jar.Pack200.Unpacker</tt>，
     * 则其值被视为具体实现类的完全限定名，该类必须实现 Unpacker。
     * 该类将被加载并实例化。如果此过程失败，则会抛出未指定的错误。</p></li>
     *
     * <li><p>如果未通过系统属性指定实现，则实例化系统默认实现类，
     * 并返回结果。</p></li>
     * </ul>
     *
     * <p>注意：如果多个线程同时使用返回的对象，则不能保证其正确运行。
     * 多线程应用程序应分配多个解包引擎，或者使用锁序列化一个引擎的使用。
     *
     * @return 一个新分配的 Unpacker 引擎。
     */

    public static Unpacker newUnpacker() {
        return (Unpacker) newInstance(UNPACK_PROVIDER);
    }

    // 接口
    /**
     * 打包引擎对输入的 JAR 文件应用各种转换，使打包流通过 gzip 或 zip 等压缩器高度可压缩。
     * 可以通过 {@link #newPacker} 获取引擎的实例。

     * 通过使用 JSR 200 规范中描述的多种技术，实现了高度的压缩。
     * 一些技术包括排序、重新排序和常量池的共定位。
     * <p>
     * 引擎初始化为以下属性描述的初始状态。
     * 可以通过获取引擎属性（使用 {@link #properties}）并存储修改后的属性到映射中来操作初始状态。
     * 资源文件将不作任何更改地传递。
     * 类文件的字节不会完全相同，因为解包器可以自由地更改类文件的次要特征，如常量池顺序。
     * 但是，类文件在语义上是相同的，如
     * <cite>The Java&trade; Virtual Machine Specification</cite> 中所述。
     * <p>
     * 默认情况下，打包器不会改变 JAR 元素的顺序。
     * 同样，每个 JAR 元素的修改时间和压缩提示也会保持不变。
     * （任何其他 ZIP 存档信息，如提供 Unix 文件权限的额外属性，将丢失。）
     * <p>
     * 注意，打包和解包 JAR 通常会改变 JAR 中类文件的字节内容。
     * 这意味着打包和解包通常会使依赖于 JAR 元素字节图像的数字签名失效。
     * 为了同时签名和打包 JAR，您必须首先打包和解包 JAR 以“规范化”它，然后在解包的 JAR 元素上计算签名，
     * 最后重新打包已签名的 JAR。
     * 两个打包步骤应使用完全相同的选项，并且可能还需要将段限制设置为 "-1"，以防止类文件大小略有变化时段边界的意外变化。
     * <p>
     * （这是为什么它有效的原因：打包器对任何类文件结构的任何重新排序都是幂等的，因此第二次打包不会改变第一次打包产生的顺序。
     * 此外，根据 JSR 200 规范，解包器保证为任何给定的存档元素传输顺序生成特定的字节图像。）
     * <p>
     * 为了保持向后兼容性，打包文件的版本将根据输入 JAR 文件中的类文件进行设置。
     * 换句话说，如果类文件是最新版本，则打包文件版本将是最新版本；反之，如果类文件版本是最旧版本，则打包文件版本也将是最旧版本。
     * 对于中间版本的类文件，将使用相应的打包文件版本。
     * 例如：
     *    如果输入 JAR 文件仅包含 1.5（或更早）的类文件，则生成 1.5 兼容的打包文件。对于没有类文件的存档也是如此。
     *    如果输入 JAR 文件包含 1.6 类文件，则打包文件版本将设置为 1.6。
     * <p>
     * 注意：除非另有说明，否则将 <tt>null</tt> 作为参数传递给此类的构造函数或方法将导致抛出 {@link NullPointerException}。
     * <p>
     * @since 1.5
     */
    public interface Packer {
        /**
         * 此属性是一个数字，表示每个存档段的估计目标大小 N（以字节为单位）。
         * 如果单个输入文件需要超过 N 字节，则将为其分配自己的存档段。
         * <p>
         * 作为一种特殊情况，值 -1 将生成一个包含所有输入文件的单个大段，而值 0 将为每个类生成一个段。
         * 较大的存档段会导致较少的碎片和更好的压缩，但处理它们需要更多的内存。
         * <p>
         * 每个段的大小通过计算段中每个要传输的输入文件的大小以及其名称和其他传输属性的大小来估计。
         * <p>
         * 默认值为 -1，这意味着打包器将始终创建单个段输出文件。在生成极大数据文件的情况下，强烈建议用户使用分段或将输入文件拆分为较小的 JAR 文件。
         * <p>
         * 一个 10Mb 的 JAR 文件在不使用此限制的情况下打包通常会小 10%，但打包器可能需要更大的 Java 堆（大约是段限制的十倍）。
         */
        String SEGMENT_LIMIT    = "pack.segment.limit";

        /**
         * 如果此属性设置为 {@link #TRUE}，则打包器将按源存档中的原始顺序传输所有元素。
         * <p>
         * 如果设置为 {@link #FALSE}，则打包器可以重新排序元素，并且可以删除 JAR 目录条目，这些条目对 Java 应用程序没有有用的信息。
         * （通常这可以实现更好的压缩。）
         * <p>
         * 默认值为 {@link #TRUE}，这保留了输入信息，但可能导致传输的存档比必要时更大。
         */
        String KEEP_FILE_ORDER = "pack.keep.file.order";


        /**
         * 如果此属性设置为单个十进制数字，打包器将使用指示的压缩存档的努力程度。
         * 级别 1 可能会产生较大的大小和更快的压缩速度，而级别 9 将花费更长时间但可能产生更好的压缩。
         * <p>
         * 特殊值 0 指示打包器直接复制原始 JAR 文件，不进行压缩。JSR 200 标准要求任何解包器理解这种特殊情况作为整个存档的直接传递。
         * <p>
         * 默认值为 5，投入适度的时间以产生合理的压缩。
         */
        String EFFORT           = "pack.effort";

        /**
         * 如果此属性设置为 {@link #TRUE} 或 {@link #FALSE}，打包器将在输出存档中设置相应的压缩提示，并且不会传输存档元素的单独压缩提示。
         * <p>
         * 如果此属性设置为特殊字符串 {@link #KEEP}，打包器将尝试确定输入存档中每个可用元素的独立压缩提示，并单独传输此提示。
         * <p>
         * 默认值为 {@link #KEEP}，这保留了输入信息，但可能导致传输的存档比必要时更大。
         * <p>
         * 由解包器实现根据提示采取行动，以适当压缩解包后的 JAR 的元素。
         * <p>
         * ZIP 或 JAR 元素的压缩提示表示该元素是被压缩还是直接存储。
         */
        String DEFLATE_HINT     = "pack.deflate.hint";

        /**
         * 如果此属性设置为特殊字符串 {@link #LATEST}，打包器将尝试确定原始存档中所有可用条目的最新修改时间，或每个段中所有可用条目的最新修改时间。
         * 这个单一值将作为段的一部分传输，并应用于每个段中的所有条目，{@link #SEGMENT_LIMIT}。
         * <p>
         * 这可以略微减少存档的传输大小，但代价是将所有安装文件设置为单个日期。
         * <p>
         * 如果此属性设置为特殊字符串 {@link #KEEP}，打包器将为每个输入元素传输单独的修改时间。
         * <p>
         * 默认值为 {@link #KEEP}，这保留了输入信息，但可能导致传输的存档比必要时更大。
         * <p>
         * 由解包器实现采取行动，以适当设置其输出文件中每个元素的修改时间。
         * @see #SEGMENT_LIMIT
         */
        String MODIFICATION_TIME        = "pack.modification.time";


                    /**
         * 表示文件应逐字节传递，不进行压缩。可以通过指定附加属性并附加不同的字符串来指定多个文件，
         * 以形成具有共同前缀的属性族。
         * <p>
         * 除了将系统文件分隔符替换为 JAR 文件分隔符 '/' 之外，没有路径名转换。
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
         * 表示在遇到包含未知属性的类文件时应采取的行动。可能的值是字符串 {@link #ERROR}、
         * {@link #STRIP} 和 {@link #PASS}。
         * <p>
         * 字符串 {@link #ERROR} 表示整个打包操作将失败，并抛出类型为 <code>IOException</code> 的异常。
         * 字符串
         * {@link #STRIP} 表示该属性将被删除。
         * 字符串
         * {@link #PASS} 表示整个类文件将被传递（就像它是资源文件一样）而不进行压缩，并显示适当的警告。
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
         * 与类属性名称连接时，表示该属性的格式，
         * 使用 JSR 200 规范中指定的布局语言。
         * <p>
         * 例如，此选项的效果是内置的：
         * <code>pack.class.attribute.SourceFile=RUH</code>。
         * <p>
         * 特殊字符串 {@link #ERROR}、{@link #STRIP} 和 {@link #PASS} 也是允许的，
         * 其含义与 {@link #UNKNOWN_ATTRIBUTE} 相同。
         * 这为用户提供了请求特定属性被拒绝、删除或逐位传递（不进行类压缩）的方式。
         * <p>
         * 以下代码可用于支持 JCOV 属性：
         * <pre><code>
         *     Map p = packer.properties();
         *     p.put(CODE_ATTRIBUTE_PFX+"CoverageTable",       "NH[PHHII]");
         *     p.put(CODE_ATTRIBUTE_PFX+"CharacterRangeTable", "NH[PHPOHIIH]");
         *     p.put(CLASS_ATTRIBUTE_PFX+"SourceID",           "RUH");
         *     p.put(CLASS_ATTRIBUTE_PFX+"CompilationID",      "RUH");
         * </code></pre>
         * <p>
         * 以下代码可用于删除调试属性：
         * <pre><code>
         *     Map p = packer.properties();
         *     p.put(CODE_ATTRIBUTE_PFX+"LineNumberTable",    STRIP);
         *     p.put(CODE_ATTRIBUTE_PFX+"LocalVariableTable", STRIP);
         *     p.put(CLASS_ATTRIBUTE_PFX+"SourceFile",        STRIP);
         * </code></pre>
         */
        String CLASS_ATTRIBUTE_PFX      = "pack.class.attribute.";

        /**
         * 与字段属性名称连接时，表示该属性的格式。
         * 例如，此选项的效果是内置的：
         * <code>pack.field.attribute.Deprecated=</code>。
         * 特殊字符串 {@link #ERROR}、{@link #STRIP} 和
         * {@link #PASS} 也是允许的。
         * @see #CLASS_ATTRIBUTE_PFX
         */
        String FIELD_ATTRIBUTE_PFX      = "pack.field.attribute.";

        /**
         * 与方法属性名称连接时，表示该属性的格式。
         * 例如，此选项的效果是内置的：
         * <code>pack.method.attribute.Exceptions=NH[RCH]</code>。
         * 特殊字符串 {@link #ERROR}、{@link #STRIP} 和 {@link #PASS}
         * 也是允许的。
         * @see #CLASS_ATTRIBUTE_PFX
         */
        String METHOD_ATTRIBUTE_PFX     = "pack.method.attribute.";

        /**
         * 与代码属性名称连接时，表示该属性的格式。
         * 例如，此选项的效果是内置的：
         * <code>pack.code.attribute.LocalVariableTable=NH[PHOHRUHRSHH]</code>。
         * 特殊字符串 {@link #ERROR}、{@link #STRIP} 和 {@link #PASS}
         * 也是允许的。
         * @see #CLASS_ATTRIBUTE_PFX
         */
        String CODE_ATTRIBUTE_PFX       = "pack.code.attribute.";

        /**
         * 解包器的进度百分比，由解包器定期更新。
         * 0 - 100 的值是正常的，-1 表示停滞。
         * 可以通过轮询此属性的值来监控进度。
         * <p>
         * 最少，解包器必须在打包操作开始时将进度设置为 0，
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
         * 此集是一个“实时视图”，因此更改其内容会立即影响打包器引擎，
         * 而引擎的更改（例如进度指示）会立即在映射中可见。
         *
         * <p>属性映射可能包含预定义的实现特定和默认属性。鼓励用户
         * 读取信息并充分了解其含义，然后再修改现有属性。
         * <p>
         * 实现特定属性以前缀为 <tt>com.</tt> 或类似前缀的包名开头。
         * 所有以 <tt>pack.</tt> 和 <tt>unpack.</tt> 开头的属性名称
         * 都保留供此 API 使用。
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
         * 将 JarFile 转换为 Pack200 存档。
         * <p>
         * 关闭其输入但不关闭其输出。（Pack200 存档是可追加的。）
         * @param in 一个 JarFile
         * @param out 一个 OutputStream
         * @exception IOException 如果遇到错误。
         */
        void pack(JarFile in, OutputStream out) throws IOException ;

        /**
         * 将 JarInputStream 转换为 Pack200 存档。
         * <p>
         * 关闭其输入但不关闭其输出。（Pack200 存档是可追加的。）
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
         * 为属性映射注册 PropertyChange 事件的监听器。
         * 这通常用于应用程序更新进度条。
         *
         * <p>此方法的默认实现不执行任何操作且没有副作用。</p>
         *
         * <p><b>警告：</b>此方法从所有不包含
         * {@code java.beans} 包的 Java SE 子集配置文件的接口声明中省略。</p>

         * @see #properties
         * @see #PROGRESS
         * @param listener  当属性更改时要调用的对象。
         * @deprecated 对 {@code PropertyChangeListener} 的依赖
         *             为 Java 平台的未来模块化创建了重大障碍。此方法将在未来的版本中删除。
         *             需要监控打包器进度的应用程序可以轮询 {@link #PROGRESS PROGRESS}
         *             属性的值。
         */
        @Deprecated
        default void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        /**
         * 移除通过 {@link #addPropertyChangeListener} 添加的 PropertyChange 事件的监听器。
         *
         * <p>此方法的默认实现不执行任何操作且没有副作用。</p>
         *
         * <p><b>警告：</b>此方法从所有不包含
         * {@code java.beans} 包的 Java SE 子集配置文件的接口声明中省略。</p>
         *
         * @see #addPropertyChangeListener
         * @param listener  要移除的 PropertyChange 监听器。
         * @deprecated 对 {@code PropertyChangeListener} 的依赖
         *             为 Java 平台的未来模块化创建了重大障碍。此方法将在未来的版本中删除。
         */
        @Deprecated
        default void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }

    /**
     * 解包器引擎将打包的流转换为 JAR 文件。
     * 可以通过 {@link #newUnpacker} 获取引擎的实例。
     * <p>
     * 由此引擎生成的每个 JAR 文件都将包含字符串
     * "<tt>PACK200</tt>" 作为 zip 文件注释。
     * 这允许部署者检测 JAR 存档是否已打包和解包。
     * <p>
     * 注意：除非另有说明，否则将 <tt>null</tt> 参数传递给此类的构造函数或方法
     * 将导致抛出 {@link NullPointerException}。
     * <p>
     * 此版本的解包器与所有先前版本兼容。
     * @since 1.5
     */
    public interface Unpacker {

        /** 字符串 "keep"，某些属性的可能值。
         * @see #DEFLATE_HINT
         */
        String KEEP  = "keep";

        /** 字符串 "true"，某些属性的可能值。
         * @see #DEFLATE_HINT
         */
        String TRUE = "true";

        /** 字符串 "false"，某些属性的可能值。
         * @see #DEFLATE_HINT
         */
        String FALSE = "false";

        /**
         * 属性表示解包器应
         * 忽略所有传输的 DEFLATE_HINT 值，
         * 用给定的值 {@link #TRUE} 或 {@link #FALSE} 替换它们。
         * 默认值是特殊字符串 {@link #KEEP}，
         * 表示解包器应保留所有传输的压缩提示。
         */
        String DEFLATE_HINT      = "unpack.deflate.hint";



        /**
         * 解包器的进度百分比，由解包器定期更新。
         * 0 - 100 的值是正常的，-1 表示停滞。
         * 可以通过轮询此属性的值来监控进度。
         * <p>
         * 最少，解包器必须在打包操作开始时将进度设置为 0，
         * 在结束时设置为 100。
         */
        String PROGRESS         = "unpack.progress";

        /**
         * 获取此引擎的属性集。此集是
         * 一个“实时视图”，因此更改其
         * 内容会立即影响打包器引擎，
         * 而引擎的更改（例如进度指示）会立即在映射中可见。
         *
         * <p>属性映射可能包含预定义的实现
         * 特定和默认属性。鼓励用户
         * 读取信息并充分了解其含义，然后再修改现有属性。
         * <p>
         * 实现特定属性以前缀为
         * 与实现者关联的包名开头，以 <tt>com.</tt> 或类似前缀开始。
         * 所有以 <tt>pack.</tt> 和 <tt>unpack.</tt> 开头的属性名称
         * 都保留供此 API 使用。
         * <p>
         * 未知属性可能会被忽略或以未指定的错误拒绝，无效条目可能会导致
         * 抛出未指定的错误。
         *
         * @return 选项键字符串到选项值的排序关联。
         */
        SortedMap<String,String> properties();


                    /**
         * 读取一个 Pack200 归档，并将编码的 JAR 写入
         * 一个 JarOutputStream。
         * 输入流的全部内容将被读取。
         * 为了提高效率，可以将 Pack200 归档读取到文件中，并传递 File 对象，
         * 使用下面描述的替代方法。
         * <p>
         * 关闭其输入但不关闭其输出。 （输出可以累积更多元素。）
         * @param in 一个 InputStream。
         * @param out 一个 JarOutputStream。
         * @exception IOException 如果遇到错误。
         */
        void unpack(InputStream in, JarOutputStream out) throws IOException;

        /**
         * 读取一个 Pack200 归档，并将编码的 JAR 写入
         * 一个 JarOutputStream。
         * <p>
         * 不关闭其输出。 （输出可以累积更多元素。）
         * @param in 一个 File。
         * @param out 一个 JarOutputStream。
         * @exception IOException 如果遇到错误。
         */
        void unpack(File in, JarOutputStream out) throws IOException;

        /**
         * 为属性映射中的属性更改事件注册一个监听器。
         * 这通常用于应用程序更新进度条。
         *
         * <p> 该方法的默认实现不执行任何操作且没有副作用。</p>
         *
         * <p><b>警告：</b> 该方法在所有不包含
         * {@code java.beans} 包的 Java SE 子集配置文件中被省略。</p>
         *
         * @see #properties
         * @see #PROGRESS
         * @param listener  当属性更改时被调用的对象。
         * @deprecated 对 {@code PropertyChangeListener} 的依赖
         *             为 Java 平台的未来模块化创建了重大障碍。此方法将在未来的版本中移除。
         *             需要监控解包器进度的应用程序可以轮询 {@link #PROGRESS
         *             PROGRESS} 属性的值。
         */
        @Deprecated
        default void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        /**
         * 移除通过 {@link #addPropertyChangeListener} 添加的属性更改事件监听器。
         *
         * <p> 该方法的默认实现不执行任何操作且没有副作用。</p>
         *
         * <p><b>警告：</b> 该方法在所有不包含
         * {@code java.beans} 包的 Java SE 子集配置文件中被省略。</p>
         *
         * @see #addPropertyChangeListener
         * @param listener  要移除的属性更改监听器。
         * @deprecated 对 {@code PropertyChangeListener} 的依赖
         *             为 Java 平台的未来模块化创建了重大障碍。此方法将在未来的版本中移除。
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
            // 我们有一个类。现在实例化它。
            return impl.newInstance();
        } catch (ClassNotFoundException e) {
            throw new Error("未找到类: " + implName +
                                ":\n检查属性文件中的 " + prop +
                                " 属性。", e);
        } catch (InstantiationException e) {
            throw new Error("无法实例化: " + implName +
                                ":\n检查属性文件中的 " + prop +
                                " 属性。", e);
        } catch (IllegalAccessException e) {
            throw new Error("无法访问类: " + implName +
                                ":\n检查属性文件中的 " + prop +
                                " 属性。", e);
        }
    }

}
