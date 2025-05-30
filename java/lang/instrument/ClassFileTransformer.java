/*
 * Copyright (c) 2003, 2011, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.instrument;

import  java.security.ProtectionDomain;

/*
 * Copyright 2003 Wily Technology, Inc.
 */

/**
 * 代理提供此接口的实现以转换类文件。
 * 转换在类由JVM定义之前发生。
 * <P>
 * 注意，术语<i>类文件</i>是按照《Java&trade;虚拟机规范》第3.1节的定义使用的，
 * 指的是以类文件格式存在的字节序列，无论它们是否存储在文件中。
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
     * {@link java.lang.instrument.Instrumentation#addTransformer(ClassFileTransformer,boolean)}
     * 的<code>canRetransform</code>参数确定：
     *  <ul>
     *    <li><i>可重变换</i>的变换器，添加时<code>canRetransform</code>为true
     *    </li>
     *    <li><i>不可重变换</i>的变换器，添加时<code>canRetransform</code>为false，或使用
     *        {@link java.lang.instrument.Instrumentation#addTransformer(ClassFileTransformer)}
     *        添加
     *    </li>
     *  </ul>
     *
     * <P>
     * 一旦变换器通过
     * {@link java.lang.instrument.Instrumentation#addTransformer(ClassFileTransformer,boolean)
     * addTransformer}注册，
     * 每当有新的类定义或类重新定义时，都会调用该变换器。
     * 可重变换的变换器在每次类重变换时也会被调用。
     * 新的类定义请求是通过
     * {@link java.lang.ClassLoader#defineClass ClassLoader.defineClass}
     * 或其本地等效方法发起的。
     * 类重新定义请求是通过
     * {@link java.lang.instrument.Instrumentation#redefineClasses Instrumentation.redefineClasses}
     * 或其本地等效方法发起的。
     * 类重变换请求是通过
     * {@link java.lang.instrument.Instrumentation#retransformClasses Instrumentation.retransformClasses}
     * 或其本地等效方法发起的。
     * 在处理请求期间，类文件字节被验证或应用之前，会调用变换器。
     * 当有多个变换器时，通过将<code>transform</code>调用链接起来来组合变换。
     * 也就是说，一个<code>transform</code>调用返回的字节数组成为下一个调用的输入
     * （通过<code>classfileBuffer</code>参数）。
     *
     * <P>
     * 变换按以下顺序应用：
     *  <ul>
     *    <li>不可重变换的变换器
     *    </li>
     *    <li>不可重变换的本地变换器
     *    </li>
     *    <li>可重变换的变换器
     *    </li>
     *    <li>可重变换的本地变换器
     *    </li>
     *  </ul>
     *
     * <P>
     * 对于重变换，不会调用不可重变换的变换器，而是重用前一次变换的结果。
     * 在所有其他情况下，都会调用此方法。
     * 在每个组中，变换器按注册顺序调用。
     * 本地变换器由Java虚拟机工具接口中的<code>ClassFileLoadHook</code>事件提供。
     *
     * <P>
     * 第一个变换器的输入（通过<code>classfileBuffer</code>参数）是：
     *  <ul>
     *    <li>对于新的类定义，
     *        传递给<code>ClassLoader.defineClass</code>的字节
     *    </li>
     *    <li>对于类重新定义，
     *        <code>definitions.getDefinitionClassFile()</code>，其中
     *        <code>definitions</code>是
     *        {@link java.lang.instrument.Instrumentation#redefineClasses
     *         Instrumentation.redefineClasses}的参数
     *    </li>
     *    <li>对于类重变换，
     *         传递给新类定义的字节，或如果已重新定义，则最后一次重新定义的字节，
     *         自动重新应用所有由不可重变换的变换器所做的变换，并且不改变；
     *         详细信息请参见
     *         {@link java.lang.instrument.Instrumentation#retransformClasses
     *          Instrumentation.retransformClasses}
     *    </li>
     *  </ul>
     *
     * <P>
     * 如果实现方法确定不需要任何变换，应返回<code>null</code>。
     * 否则，应创建一个新的<code>byte[]</code>数组，
     * 将输入<code>classfileBuffer</code>复制到其中，
     * 并进行所有所需的变换，然后返回新数组。
     * 输入<code>classfileBuffer</code>不得被修改。
     *
     * <P>
     * 在重变换和重新定义的情况下，
     * 变换器必须支持重新定义语义：
     * 如果变换器在初始定义时更改了类，而该类后来被重变换或重新定义，
     * 变换器必须确保输出的第二个类文件是第一个输出类文件的合法重新定义。
     *
     * <P>
     * 如果变换器抛出异常（未捕获），
     * 后续的变换器仍会被调用，并且仍会尝试加载、重新定义或重变换。
     * 因此，抛出异常的效果与返回<code>null</code>相同。
     * 为了防止在变换器代码中生成未检查异常时出现意外行为，变换器可以捕获<code>Throwable</code>。
     * 如果变换器认为<code>classFileBuffer</code>不代表格式良好的类文件，应抛出
     * <code>IllegalClassFormatException</code>；
     * 虽然这与返回null的效果相同，但它有助于记录或调试格式损坏。
     *
     * @param loader                要转换的类的定义加载器，
     *                              如果是引导加载器，则可能为<code>null</code>
     * @param className             类的名称，采用《Java虚拟机规范》中定义的内部形式的完全限定类和接口名称。
     *                              例如，<code>"java/util/List"</code>。
     * @param classBeingRedefined   如果是由重新定义或重变换触发的，
     *                              正在重新定义或重变换的类；
     *                              如果是类加载，则为<code>null</code>
     * @param protectionDomain      被定义或重新定义的类的保护域
     * @param classfileBuffer       以类文件格式的输入字节数组 - 不得修改
     *
     * @throws IllegalClassFormatException 如果输入不代表格式良好的类文件
     * @return  格式良好的类文件缓冲区（变换的结果），
     *          或<code>null</code>，如果未执行变换。
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
