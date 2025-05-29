
/*
 * 版权所有 (c) 2003, 2011, Oracle 和/或其附属公司。保留所有权利。
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

package java.lang.instrument;

import  java.security.ProtectionDomain;

/*
 * 版权所有 2003 Wily Technology, Inc.
 */

/**
 * 代理提供此接口的实现以转换类文件。
 * 转换发生在类由 JVM 定义之前。
 * <P>
 * 注意，术语 <i>类文件</i> 是指在
 * <cite>Java&trade; 虚拟机规范</cite> 第 3.1 节中定义的，
 * 以表示类文件格式的字节序列，无论它们是否存储在文件中。
 *
 * @see     java.lang.instrument.Instrumentation
 * @see     java.lang.instrument.Instrumentation#addTransformer
 * @see     java.lang.instrument.Instrumentation#removeTransformer
 * @since   1.5
 */

public interface ClassFileTransformer {
    /**
     * 此方法的实现可以转换提供的类文件并返回一个新的替换类文件。
     *
     * <P>
     * 变换器有两种类型，由
     * {@link java.lang.instrument.Instrumentation#addTransformer(ClassFileTransformer,boolean)} 的
     * <code>canRetransform</code> 参数确定：
     *  <ul>
     *    <li><i>可重变换</i> 的变换器，添加时 <code>canRetransform</code> 为 true
     *    </li>
     *    <li><i>不可重变换</i> 的变换器，添加时 <code>canRetransform</code> 为 false 或使用
     *        {@link java.lang.instrument.Instrumentation#addTransformer(ClassFileTransformer)}
     *    </li>
     *  </ul>
     *
     * <P>
     * 一旦变换器通过
     * {@link java.lang.instrument.Instrumentation#addTransformer(ClassFileTransformer,boolean)
     * addTransformer} 注册，
     * 变换器将被调用于每个新的类定义和每个类重新定义。
     * 可重变换的变换器还将被调用于每个类的重变换。
     * 新类定义的请求是通过
     * {@link java.lang.ClassLoader#defineClass ClassLoader.defineClass}
     * 或其原生等效方法进行的。
     * 类重新定义的请求是通过
     * {@link java.lang.instrument.Instrumentation#redefineClasses Instrumentation.redefineClasses}
     * 或其原生等效方法进行的。
     * 类重变换的请求是通过
     * {@link java.lang.instrument.Instrumentation#retransformClasses Instrumentation.retransformClasses}
     * 或其原生等效方法进行的。
     * 变换器在请求处理期间被调用，但在类文件字节被验证或应用之前。
     * 当有多个变换器时，变换通过链接 <code>transform</code> 调用来组合。
     * 也就是说，一个 <code>transform</code> 调用返回的字节数组成为下一个调用的输入
     * （通过 <code>classfileBuffer</code> 参数）。
     *
     * <P>
     * 变换按以下顺序应用：
     *  <ul>
     *    <li>不可重变换的变换器
     *    </li>
     *    <li>不可重变换的原生变换器
     *    </li>
     *    <li>可重变换的变换器
     *    </li>
     *    <li>可重变换的原生变换器
     *    </li>
     *  </ul>
     *
     * <P>
     * 对于重变换，不可重变换的变换器不会被调用，而是重用前一次变换的结果。
     * 在所有其他情况下，将调用此方法。
     * 在每个组中，变换器按注册顺序调用。
     * 原生变换器由 Java 虚拟机工具接口中的 <code>ClassFileLoadHook</code> 事件提供。
     *
     * <P>
     * 第一个变换器的输入（通过 <code>classfileBuffer</code> 参数）是：
     *  <ul>
     *    <li>对于新类定义，
     *        传递给 <code>ClassLoader.defineClass</code> 的字节
     *    </li>
     *    <li>对于类重新定义，
     *        <code>definitions.getDefinitionClassFile()</code>，其中
     *        <code>definitions</code> 是
     *        {@link java.lang.instrument.Instrumentation#redefineClasses
     *         Instrumentation.redefineClasses} 的参数
     *    </li>
     *    <li>对于类重变换，
     *         传递给新类定义的字节或，如果已重新定义，
     *         最后一次重新定义的字节，所有由不可重变换的变换器自动重新应用且未更改的变换；
     *         详细信息请参见
     *         {@link java.lang.instrument.Instrumentation#retransformClasses
     *          Instrumentation.retransformClasses}
     *    </li>
     *  </ul>
     *
     * <P>
     * 如果实现方法确定不需要任何变换，
     * 它应返回 <code>null</code>。
     * 否则，它应创建一个新的 <code>byte[]</code> 数组，
     * 将输入 <code>classfileBuffer</code> 复制到其中，
     * 并进行所有所需的变换，然后返回新数组。
     * 输入的 <code>classfileBuffer</code> 不得被修改。
     *
     * <P>
     * 在重变换和重新定义的情况下，
     * 变换器必须支持重新定义语义：
     * 如果变换器在初始定义时更改了类，而该类后来
     * 被重变换或重新定义，变换器必须确保第二次输出的类文件是第一次输出类文件的合法重新定义。
     *
     * <P>
     * 如果变换器抛出异常（未捕获），
     * 后续的变换器仍将被调用，加载、重新定义
     * 或重变换仍将尝试。
     * 因此，抛出异常与返回 <code>null</code> 具有相同的效果。
     * 为了防止在变换器代码中生成未检查异常时出现意外行为，
     * 变换器可以捕获 <code>Throwable</code>。
     * 如果变换器认为 <code>classFileBuffer</code> 不表示格式良好的类文件，
     * 它应抛出 <code>IllegalClassFormatException</code>；
     * 虽然这与返回 null 有相同的效果，但它有助于记录或调试格式损坏。
     *
     * @param loader                要转换的类的定义加载器，
     *                              如果是引导加载器，则可能为 <code>null</code>
     * @param className             类的名称，采用
     *                              <i>Java 虚拟机规范</i> 中定义的完全限定类和接口名称的内部形式。
     *                              例如，<code>"java/util/List"</code>。
     * @param classBeingRedefined   如果这是由重新定义或重变换触发的，
     *                              正在重新定义或重变换的类；
     *                              如果这是类加载，则为 <code>null</code>
     * @param protectionDomain      正在定义或重新定义的类的保护域
     * @param classfileBuffer       以类文件格式的输入字节数组 - 不得被修改
     *
     * @throws IllegalClassFormatException 如果输入不表示格式良好的类文件
     * @return  格式良好的类文件缓冲区（变换的结果），
     *          或 <code>null</code> 如果未执行变换。
     * @see Instrumentation#redefineClasses
     */
    byte[]
    transform(  ClassLoader         loader,
                String              className,
                Class<?>            classBeingRedefined,
                ProtectionDomain    protectionDomain,
                byte[]              classfileBuffer)
        throws IllegalClassFormatException;
}
