
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

package java.lang.instrument;

import  java.io.File;
import  java.io.IOException;
import  java.util.jar.JarFile;

/*
 * 版权所有 2003 Wily Technology, Inc.
 */

/**
 * 该类提供用于 instrument Java
 * 编程语言代码所需的服务。
 * 代码 instrument 是为了收集数据，这些数据将被工具使用而添加到方法中的字节码。
 * 由于这些更改是纯粹的添加，这些工具不会修改
 * 应用程序状态或行为。
 * 这样的良性工具示例包括监控代理、分析器、覆盖率分析器和事件记录器。
 *
 * <P>
 * 获取 <code>Instrumentation</code> 接口实例有两种方式：
 *
 * <ol>
 *   <li><p> 当 JVM 以指示代理类的方式启动时。在这种情况下，<code>Instrumentation</code> 实例
 *     会传递给代理类的 <code>premain</code> 方法。
 *     </p></li>
 *   <li><p> 当 JVM 提供了一种在启动后启动代理的机制时。在这种情况下，<code>Instrumentation</code>
 *     实例会传递给代理代码的 <code>agentmain</code> 方法。 </p> </li>
 * </ol>
 * <p>
 * 这些机制在
 * {@linkplain java.lang.instrument 包规范} 中描述。
 * <p>
 * 一旦代理获取了 <code>Instrumentation</code> 实例，
 * 代理可以在任何时候调用该实例上的方法。
 *
 * @since   1.5
 */
public interface Instrumentation {
    /**
     * 注册提供的转换器。所有未来的类定义
     * 都将被转换器看到，除了任何已注册转换器依赖的类定义。
     * 当类被加载时，当它们被
     * {@linkplain #redefineClasses 重新定义} 时，如果 <code>canRetransform</code> 为 true，
     * 当它们被 {@linkplain #retransformClasses 重新转换} 时，都会调用转换器。
     * 有关转换调用顺序，请参阅 {@link java.lang.instrument.ClassFileTransformer#transform
     * ClassFileTransformer.transform}。
     * 如果转换器在执行过程中抛出异常，JVM 仍然会按顺序调用其他已注册的
     * 转换器。同一个转换器可以添加多次，
     * 但强烈不建议这样做——通过创建转换器类的新实例来避免这种情况。
     * <P>
     * 该方法旨在用于 instrument，如
     * {@linkplain Instrumentation 类规范} 中所述。
     *
     * @param transformer          要注册的转换器
     * @param canRetransform       此转换器的转换是否可以重新转换
     * @throws java.lang.NullPointerException 如果传递的转换器为 <code>null</code>
     * @throws java.lang.UnsupportedOperationException 如果 <code>canRetransform</code>
     * 是 true 且当前 JVM 配置不允许重新转换（{@link #isRetransformClassesSupported} 为 false）
     * @since 1.6
     */
    void
    addTransformer(ClassFileTransformer transformer, boolean canRetransform);

    /**
     * 注册提供的转换器。
     * <P>
     * 与 <code>addTransformer(transformer, false)</code> 相同。
     *
     * @param transformer          要注册的转换器
     * @throws java.lang.NullPointerException 如果传递的转换器为 <code>null</code>
     * @see    #addTransformer(ClassFileTransformer,boolean)
     */
    void
    addTransformer(ClassFileTransformer transformer);

    /**
     * 注销提供的转换器。未来的类定义将
     * 不再显示给转换器。移除最近添加的匹配转换器实例。由于
     * 类加载的多线程性质，注销后转换器仍可能收到调用。
     * 转换器应编写得具有防御性，以应对这种情况。
     *
     * @param transformer          要注销的转换器
     * @return  如果找到了并移除了转换器，则返回 true，否则返回 false
     * @throws java.lang.NullPointerException 如果传递的转换器为 <code>null</code>
     */
    boolean
    removeTransformer(ClassFileTransformer transformer);

    /**
     * 返回当前 JVM 配置是否支持类的重新转换。
     * 重新转换已加载类的能力是 JVM 的可选功能。
     * 仅当代理 JAR 文件中的
     * <code>Can-Retransform-Classes</code> 清单属性设置为
     * <code>true</code>（如 {@linkplain java.lang.instrument 包规范} 中所述）且 JVM 支持此功能时，
     * 才支持重新转换。
     * 在单个 JVM 实例的单次实例化过程中，多次调用此方法将始终返回相同的结果。
     * @return  如果当前 JVM 配置支持类的重新转换，则返回 true，否则返回 false。
     * @see #retransformClasses
     * @since 1.6
     */
    boolean
    isRetransformClassesSupported();

    /**
     * 重新转换提供的类集。
     *
     * <P>
     * 该函数便于已经加载的类的 instrument。
     * 当类最初加载或重新定义时，
     * 可以使用 {@link java.lang.instrument.ClassFileTransformer ClassFileTransformer} 对初始类文件字节进行转换。
     * 该函数重新运行转换过程
     * （无论之前是否发生过转换）。
     * 重新转换遵循以下步骤：
     *  <ul>
     *    <li>从初始类文件字节开始
     *    </li>
     *    <li>对于每个使用 <code>canRetransform</code>
     *      为 false 添加的转换器，上次类加载或重新定义时
     *      {@link java.lang.instrument.ClassFileTransformer#transform transform}
     *      返回的字节将被重用作为转换的输出；请注意，这相当于
     *      重新应用之前的转换，不作任何更改；
     *      除了
     *      {@link java.lang.instrument.ClassFileTransformer#transform transform}
     *      不会被调用
     *    </li>
     *    <li>对于每个使用 <code>canRetransform</code>
     *      为 true 添加的转换器，
     *      {@link java.lang.instrument.ClassFileTransformer#transform transform}
     *      方法将被调用
     *    </li>
     *    <li>转换后的类文件字节将作为类的新定义安装
     *    </li>
     *  </ul>
     * <P>
     *
     * 转换的顺序在
     * {@link java.lang.instrument.ClassFileTransformer#transform transform} 方法中描述。
     * 这个相同的顺序也用于重新转换不可转换的转换的自动重新应用。
     * <P>
     *
     * 初始类文件字节表示传递给
     * {@link java.lang.ClassLoader#defineClass ClassLoader.defineClass} 或
     * {@link #redefineClasses redefineClasses}
     * 的字节（在任何转换之前），但可能不完全匹配。
     * 常量池可能不具有相同的布局或内容。
     * 常量池可能有更多的或更少的条目。
     * 常量池条目可能顺序不同；但是，
     * 方法字节码中的常量池索引将对应。
     * 一些属性可能不存在。
     * 在顺序无意义的情况下，例如方法的顺序，
     * 顺序可能不会保留。
     *
     * <P>
     * 该方法操作一个集合，以便同时对多个类进行相互依赖的更改
     * （类 A 的重新转换可能需要类 B 的重新转换）。
     *
     * <P>
     * 如果重新转换的方法有活动的栈帧，这些活动帧将继续
     * 运行原始方法的字节码。
     * 重新转换的方法将在新的调用中使用。
     *
     * <P>
     * 该方法不会导致任何初始化，除了根据常规 JVM 语义会发生的情况。
     * 换句话说，重新定义类不会导致其初始化器运行。
     * 静态变量的值将保持在调用前的状态。
     *
     * <P>
     * 重新转换类的实例不受影响。
     *
     * <P>
     * 重新转换可以更改方法体、常量池和属性。
     * 重新转换不得添加、删除或重命名字段或方法，更改方法的签名，或更改继承。
     * 这些限制可能会在未来的版本中解除。
     * 类文件字节不会在转换应用后进行检查、验证和安装，如果结果字节有误，此方法将抛出异常。
     *
     * <P>
     * 如果此方法抛出异常，没有类被重新转换。
     * <P>
     * 该方法旨在用于 instrument，如
     * {@linkplain Instrumentation 类规范} 中所述。
     *
     * @param classes 要重新转换的类数组；
     *                允许使用零长度数组，在这种情况下，此方法不执行任何操作
     * @throws java.lang.instrument.UnmodifiableClassException 如果指定的类无法修改
     * ({@link #isModifiableClass} 将返回 <code>false</code>)
     * @throws java.lang.UnsupportedOperationException 如果当前 JVM 配置不允许
     * 重新转换（{@link #isRetransformClassesSupported} 为 false）或尝试的重新转换
     * 尝试进行了不支持的更改
     * @throws java.lang.ClassFormatError 如果数据不包含有效的类
     * @throws java.lang.NoClassDefFoundError 如果类文件中的名称与类的名称不匹配
     * @throws java.lang.UnsupportedClassVersionError 如果类文件版本号不受支持
     * @throws java.lang.ClassCircularityError 如果新类包含循环
     * @throws java.lang.LinkageError 如果发生链接错误
     * @throws java.lang.NullPointerException 如果提供的类数组或其任何组件
     *                                        为 <code>null</code>。
     *
     * @see #isRetransformClassesSupported
     * @see #addTransformer
     * @see java.lang.instrument.ClassFileTransformer
     * @since 1.6
     */
    void
    retransformClasses(Class<?>... classes) throws UnmodifiableClassException;


                /**
     * 返回当前JVM配置是否支持类的重新定义。
     * 重新定义已加载类的能力是JVM的可选功能。
     * 仅当代理JAR文件中的<code>Can-Redefine-Classes</code>清单属性设置为
     * <code>true</code>（如{@linkplain java.lang.instrument 包规范}中所述）且JVM支持此功能时，
     * 才会支持重新定义。
     * 在单个JVM实例的单次实例化期间，对本方法的多次调用将始终返回相同的结果。
     * @return  如果当前JVM配置支持类的重新定义，则返回true，否则返回false。
     * @see #redefineClasses
     */
    boolean
    isRedefineClassesSupported();

    /**
     * 使用提供的类文件重新定义提供的类集。
     *
     * <P>
     * 本方法用于在不引用现有类文件字节的情况下替换类的定义，就像在修复并继续调试时从源代码重新编译一样。
     * 如果现有类文件字节需要转换（例如在字节码仪器中）
     * 应使用{@link #retransformClasses retransformClasses}。
     *
     * <P>
     * 本方法作用于一个集合，以便同时对多个类进行相互依赖的更改
     * （类A的重新定义可能需要类B的重新定义）。
     *
     * <P>
     * 如果重新定义的方法有活动的堆栈帧，这些活动的帧将继续
     * 运行原始方法的字节码。
     * 重新定义的方法将在新的调用中使用。
     *
     * <P>
     * 本方法不会引起任何初始化，除了按照常规JVM语义会发生的情况外。换句话说，重新定义类
     * 不会导致其初始化器的运行。静态变量的值将保持在调用前的状态。
     *
     * <P>
     * 重新定义类的实例不受影响。
     *
     * <P>
     * 重新定义可以更改方法体、常量池和属性。
     * 重新定义不得添加、删除或重命名字段或方法，更改方法的签名，或更改继承。这些限制可能会
     * 在未来的版本中解除。类文件字节不会在转换应用后进行检查、验证和安装，
     * 如果结果字节有误，本方法将抛出异常。
     *
     * <P>
     * 如果本方法抛出异常，则没有类被重新定义。
     * <P>
     * 本方法旨在用于仪器化，如
     * {@linkplain Instrumentation 类规范}中所述。
     *
     * @param definitions 要重新定义的类及其相应定义的数组；
     *                    允许零长度数组，在这种情况下，本方法不执行任何操作
     * @throws java.lang.instrument.UnmodifiableClassException 如果指定的类无法修改
     * ({@link #isModifiableClass} 将返回 <code>false</code>)
     * @throws java.lang.UnsupportedOperationException 如果当前JVM配置不允许重新定义
     * ({@link #isRedefineClassesSupported} 为 false) 或尝试的重新定义尝试进行了不支持的更改
     * @throws java.lang.ClassFormatError 如果数据不包含有效的类
     * @throws java.lang.NoClassDefFoundError 如果类文件中的名称与类的名称不匹配
     * @throws java.lang.UnsupportedClassVersionError 如果类文件版本号不受支持
     * @throws java.lang.ClassCircularityError 如果新类包含循环引用
     * @throws java.lang.LinkageError 如果发生链接错误
     * @throws java.lang.NullPointerException 如果提供的定义数组或其任何组件为 <code>null</code>
     * @throws java.lang.ClassNotFoundException 永远不会抛出（仅出于兼容性原因存在）
     *
     * @see #isRedefineClassesSupported
     * @see #addTransformer
     * @see java.lang.instrument.ClassFileTransformer
     */
    void
    redefineClasses(ClassDefinition... definitions)
        throws  ClassNotFoundException, UnmodifiableClassException;


    /**
     * 确定类是否可以通过
     * {@linkplain #retransformClasses 重转换}
     * 或 {@linkplain #redefineClasses 重新定义} 进行修改。
     * 如果类可修改，则本方法返回 <code>true</code>。
     * 如果类不可修改，则本方法返回 <code>false</code>。
     * <P>
     * 要对类进行重转换，{@link #isRetransformClassesSupported} 也必须为 true。
     * 但 <code>isRetransformClassesSupported()</code> 的值不会影响本函数的返回值。
     * 要对类进行重新定义，{@link #isRedefineClassesSupported} 也必须为 true。
     * 但 <code>isRedefineClassesSupported()</code> 的值不会影响本函数的返回值。
     * <P>
     * 原始类（例如，<code>java.lang.Integer.TYPE</code>）
     * 和数组类永远不可修改。
     *
     * @param theClass 要检查是否可修改的类
     * @return 参数类是否可修改
     * @throws java.lang.NullPointerException 如果指定的类为 <code>null</code>。
     *
     * @see #retransformClasses
     * @see #isRetransformClassesSupported
     * @see #redefineClasses
     * @see #isRedefineClassesSupported
     * @since 1.6
     */
    boolean
    isModifiableClass(Class<?> theClass);

    /**
     * 返回当前由JVM加载的所有类的数组。
     *
     * @return 包含JVM加载的所有类的数组，如果没有则返回零长度数组
     */
    @SuppressWarnings("rawtypes")
    Class[]
    getAllLoadedClasses();

    /**
     * 返回由<code>loader</code>初始化的所有类的数组。
     * 如果提供的加载器为<code>null</code>，则返回由引导类加载器初始化的类。
     *
     * @param loader          将返回其初始化类列表的加载器
     * @return 包含由加载器初始化的所有类的数组，
     *          如果没有则返回零长度数组
     */
    @SuppressWarnings("rawtypes")
    Class[]
    getInitiatedClasses(ClassLoader loader);

                /**
     * 返回指定对象所消耗的存储量的实现特定的近似值。结果可能包括对象的部分或全部开销，
     * 因此在实现内进行比较是有用的，但在不同实现之间进行比较则不然。
     *
     * 该估计值可能在单次JVM调用期间发生变化。
     *
     * @param objectToSize     要测量大小的对象
     * @return 指定对象所消耗的存储量的实现特定的近似值
     * @throws java.lang.NullPointerException 如果提供的对象为 <code>null</code>。
     */
    long
    getObjectSize(Object objectToSize);


    /**
     * 指定一个包含要由引导类加载器定义的工具类的JAR文件。
     *
     * <p> 当虚拟机的内置类加载器，即“引导类加载器”，未能找到一个类时，JAR文件中的条目也将被搜索。
     *
     * <p> 可以多次使用此方法以按调用此方法的顺序添加多个JAR文件进行搜索。
     *
     * <p> 代理应确保JAR文件不包含除由引导类加载器定义用于工具的类或资源之外的任何内容。
     * 如果不遵守此警告，可能会导致难以诊断的意外行为。例如，假设有一个加载器L，L的委托父加载器是引导类加载器。
     * 此外，类C中的一个方法（由L定义）引用了一个非公共访问器类C$1。如果JAR文件包含类C$1，则委托给引导类加载器将导致C$1由引导类加载器定义。在这个例子中，将抛出一个<code>IllegalAccessError</code>，可能导致应用程序失败。避免这类问题的一种方法是为工具类使用唯一的包名。
     *
     * <p>
     * <cite>Java&trade;虚拟机规范</cite>
     * 规定，Java虚拟机先前未能解析的符号引用的后续解析尝试总是以与初始解析尝试时抛出的相同错误失败。因此，如果JAR文件包含一个条目，该条目对应于Java虚拟机已尝试解析但未成功的类，则对该引用的后续解析尝试将以与初始尝试相同的错误失败。
     *
     * @param   jarfile
     *          当引导类加载器未能找到一个类时要搜索的JAR文件。
     *
     * @throws  NullPointerException
     *          如果<code>jarfile</code>为<code>null</code>。
     *
     * @see     #appendToSystemClassLoaderSearch
     * @see     java.lang.ClassLoader
     * @see     java.util.jar.JarFile
     *
     * @since 1.6
     */
    void
    appendToBootstrapClassLoaderSearch(JarFile jarfile);

    /**
     * 指定一个包含要由系统类加载器定义的工具类的JAR文件。
     *
     * 当系统类加载器（参见
     * {@link java.lang.ClassLoader#getSystemClassLoader getSystemClassLoader()}
     * ）未能找到一个类时，JAR文件中的条目也将被搜索。
     *
     * <p> 可以多次使用此方法以按调用此方法的顺序添加多个JAR文件进行搜索。
     *
     * <p> 代理应确保JAR文件不包含除由系统类加载器定义用于工具的类或资源之外的任何内容。
     * 如果不遵守此警告，可能会导致难以诊断的意外行为（参见
     * {@link #appendToBootstrapClassLoaderSearch
     * appendToBootstrapClassLoaderSearch}）。
     *
     * <p> 如果系统类加载器实现了名为<code>appendToClassPathForInstrumentation</code>的方法，该方法接受一个类型为<code>java.lang.String</code>的单个参数，则支持添加JAR文件进行搜索。该方法不需要具有<code>public</code>访问权限。JAR文件的名称通过调用<code>jarfile</code>上的{@link java.util.zip.ZipFile#getName
     * getName()}方法获得，并作为参数提供给<code>appendToClassPathForInstrumentation</code>方法。
     *
     * <p>
     * <cite>Java&trade;虚拟机规范</cite>
     * 规定，Java虚拟机先前未能解析的符号引用的后续解析尝试总是以与初始解析尝试时抛出的相同错误失败。因此，如果JAR文件包含一个条目，该条目对应于Java虚拟机已尝试解析但未成功的类，则对该引用的后续解析尝试将以与初始尝试相同的错误失败。
     *
     * <p> 此方法不会更改<code>java.class.path</code>
     * {@link java.lang.System#getProperties system property}的值。
     *
     * @param   jarfile
     *          当系统类加载器未能找到一个类时要搜索的JAR文件。
     *
     * @throws  UnsupportedOperationException
     *          如果系统类加载器不支持添加JAR文件进行搜索。
     *
     * @throws  NullPointerException
     *          如果<code>jarfile</code>为<code>null</code>。
     *
     * @see     #appendToBootstrapClassLoaderSearch
     * @see     java.lang.ClassLoader#getSystemClassLoader
     * @see     java.util.jar.JarFile
     * @since 1.6
     */
    void
    appendToSystemClassLoaderSearch(JarFile jarfile);

                /**
     * 返回当前JVM配置是否支持
     * {@linkplain #setNativeMethodPrefix(ClassFileTransformer,String)
     * 设置本地方法前缀}。
     * 设置本地方法前缀的能力是JVM的一个可选功能。
     * 只有当代理JAR文件中的<code>Can-Set-Native-Method-Prefix</code>清单属性设置为
     * <code>true</code>（如{@linkplain java.lang.instrument 包规范}所述）并且JVM支持
     * 此功能时，设置本地方法前缀才会被支持。
     * 在单个JVM实例的整个生命周期中，多次调用此方法将始终返回相同的结果。
     * @return  如果当前JVM配置支持设置本地方法前缀，则返回true，否则返回false。
     * @see #setNativeMethodPrefix
     * @since 1.6
     */
    boolean
    isNativeMethodPrefixSupported();

    /**
     * 此方法通过允许在名称上应用前缀重试来修改
     * 本地方法解析的失败处理。
     * 当与
     * {@link java.lang.instrument.ClassFileTransformer ClassFileTransformer}一起使用时，
     * 它使得本地方法可以被
     * 仪器化。
     * <p>
     * 由于本地方法不能直接被仪器化
     * （它们没有字节码），因此必须用
     * 一个可以被仪器化的非本地方法来包装它们。
     * 例如，如果我们有：
     * <pre>
     *   native boolean foo(int x);</pre>
     * <p>
     * 我们可以转换类文件（在类的初始定义时使用
     * ClassFileTransformer）使这变成：
     * <pre>
     *   boolean foo(int x) {
     *     <i>... 记录进入foo ...</i>
     *     return wrapped_foo(x);
     *   }
     *
     *   native boolean wrapped_foo(int x);</pre>
     * <p>
     * 其中<code>foo</code>成为实际本地
     * 方法的包装器，附加了前缀"wrapped_"。注意
     * "wrapped_"不是一个好的前缀选择，因为它
     * 可能会形成现有方法的名称
     * 因此像"$$$MyAgentWrapped$$$_"这样的前缀会更好，但会使这些示例更难阅读。
     * <p>
     * 包装器将允许收集本地
     * 方法调用的数据，但现在的问题是如何将
     * 包装方法与本地实现链接起来。
     * 即，方法<code>wrapped_foo</code>需要被
     * 解析为<code>foo</code>的本地实现，
     * 可能是：
     * <pre>
     *   Java_somePackage_someClass_foo(JNIEnv* env, jint x)</pre>
     * <p>
     * 此函数允许指定前缀并进行
     * 正确的解析。
     * 具体来说，当标准解析失败时，将
     * 重新尝试解析，考虑前缀。
     * 解析有两种方式，使用JNI函数<code>RegisterNatives</code>的显式
     * 解析和正常的自动解析。对于
     * <code>RegisterNatives</code>，JVM将尝试此
     * 关联：
     * <pre>{@code
     *   method(foo) -> nativeImplementation(foo)
     * }</pre>
     * <p>
     * 当这失败时，解析将使用
     * 指定的前缀附加到方法名称上重试，
     * 得到正确的解析：
     * <pre>{@code
     *   method(wrapped_foo) -> nativeImplementation(foo)
     * }</pre>
     * <p>
     * 对于自动解析，JVM将尝试：
     * <pre>{@code
     *   method(wrapped_foo) -> nativeImplementation(wrapped_foo)
     * }</pre>
     * <p>
     * 当这失败时，解析将使用
     * 从实现名称中删除指定的前缀重试，
     * 得到正确的解析：
     * <pre>{@code
     *   method(wrapped_foo) -> nativeImplementation(foo)
     * }</pre>
     * <p>
     * 请注意，由于前缀仅在标准
     * 解析失败时使用，因此本地方法可以被选择性地包装。
     * <p>
     * 由于每个<code>ClassFileTransformer</code>
     * 都可以对字节码进行自己的转换，因此可以应用
     * 多层包装。因此，每个
     * 转换器需要自己的前缀。由于转换
     * 是按顺序应用的，前缀（如果应用）也将
     * 按相同的顺序应用
     * （参见{@link #addTransformer(ClassFileTransformer,boolean) addTransformer}）。
     * 因此，如果有三个转换器应用了
     * 包装，<code>foo</code>可能会变成
     * <code>$trans3_$trans2_$trans1_foo</code>。但如果
     * 第二个转换器没有对
     * <code>foo</code>应用包装，它将只是
     * <code>$trans3_$trans1_foo</code>。为了能够
     * 高效地确定前缀序列，
     * 只有当非本地包装器存在时才应用中间前缀。因此，在最后一个示例中，即使
     * <code>$trans1_foo</code>不是一个本地方法，<code>$trans1_</code>前缀也会被应用，因为
     * <code>$trans1_foo</code>存在。
     *
     * @param   transformer
     *          使用此前缀进行包装的ClassFileTransformer。
     * @param   prefix
     *          当重试失败的本地方法解析时，应用于包装的本地方法的前缀。如果前缀
     *          为<code>null</code>或空字符串，则
     *          对于此转换器，失败的本地方法解析不会重试。
     * @throws java.lang.NullPointerException 如果传递的转换器为<code>null</code>。
     * @throws java.lang.UnsupportedOperationException 如果当前JVM配置不允许设置本地方法前缀
     *           （{@link #isNativeMethodPrefixSupported}为false）。
     * @throws java.lang.IllegalArgumentException 如果转换器未注册
     *           （参见{@link #addTransformer(ClassFileTransformer,boolean) addTransformer}）。
     *
     * @since 1.6
     */
    void
    setNativeMethodPrefix(ClassFileTransformer transformer, String prefix);
}
