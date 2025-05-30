/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

import java.util.Objects;

/**
 * 一个堆栈跟踪中的元素，由 {@link
 * Throwable#getStackTrace()} 返回。每个元素代表一个单独的堆栈帧。
 * 除了堆栈顶部的帧外，所有堆栈帧都代表一个方法调用。堆栈顶部的帧代表生成堆栈跟踪的执行点。
 * 通常，这是与堆栈跟踪对应的可抛出对象的创建点。
 *
 * @since  1.4
 * @author Josh Bloch
 */
public final class StackTraceElement implements java.io.Serializable {
    // 通常由 VM 初始化（公共构造函数在 1.5 中添加）
    private String declaringClass;
    private String methodName;
    private String fileName;
    private int    lineNumber;

    /**
     * 创建一个表示指定执行点的堆栈跟踪元素。
     *
     * @param declaringClass 包含堆栈跟踪元素表示的执行点的类的完全限定名
     * @param methodName 包含堆栈跟踪元素表示的执行点的方法名
     * @param fileName 包含堆栈跟踪元素表示的执行点的文件名，如果此信息不可用，则为 {@code null}
     * @param lineNumber 包含堆栈跟踪元素表示的执行点的源代码行号，如果此信息不可用，则为负数。值 -2 表示包含执行点的方法是本地方法
     * @throws NullPointerException 如果 {@code declaringClass} 或
     *         {@code methodName} 为 null
     * @since 1.5
     */
    public StackTraceElement(String declaringClass, String methodName,
                             String fileName, int lineNumber) {
        this.declaringClass = Objects.requireNonNull(declaringClass, "Declaring class is null");
        this.methodName     = Objects.requireNonNull(methodName, "Method name is null");
        this.fileName       = fileName;
        this.lineNumber     = lineNumber;
    }

    /**
     * 返回包含此堆栈跟踪元素表示的执行点的源文件名。通常，这对应于相关 {@code class}
     * 文件的 {@code SourceFile} 属性（参见《Java 虚拟机规范》第 4.7.7 节）。在某些系统中，名称可能指代源代码单元，而不是文件，例如源代码库中的条目。
     *
     * @return 包含此堆栈跟踪元素表示的执行点的文件名，如果此信息不可用，则为 {@code null}。
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 返回包含此堆栈跟踪元素表示的执行点的源代码行号。通常，这是从相关 {@code class}
     * 文件的 {@code LineNumberTable} 属性派生的（参见《Java 虚拟机规范》第 4.7.8 节）。
     *
     * @return 包含此堆栈跟踪元素表示的执行点的源代码行号，如果此信息不可用，则为负数。
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * 返回包含此堆栈跟踪元素表示的执行点的类的完全限定名。
     *
     * @return 包含此堆栈跟踪元素表示的执行点的 {@code Class} 的完全限定名。
     */
    public String getClassName() {
        return declaringClass;
    }

    /**
     * 返回包含此堆栈跟踪元素表示的执行点的方法名。如果执行点包含在实例或类初始化器中，此方法将返回适当的方法名，如 {@code <init>} 或
     * {@code <clinit>}（参见《Java 虚拟机规范》第 3.9 节）。
     *
     * @return 包含此堆栈跟踪元素表示的执行点的方法名。
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * 如果包含此堆栈跟踪元素表示的执行点的方法是本地方法，则返回 true。
     *
     * @return 如果包含此堆栈跟踪元素表示的执行点的方法是本地方法，则返回 {@code true}。
     */
    public boolean isNativeMethod() {
        return lineNumber == -2;
    }

    /**
     * 返回此堆栈跟踪元素的字符串表示形式。此字符串的格式取决于实现，但以下示例可以视为典型：
     * <ul>
     * <li>
     *   {@code "MyClass.mash(MyClass.java:9)"} - 这里，{@code "MyClass"}
     *   是包含此堆栈跟踪元素表示的执行点的类的完全限定名，
     *   {@code "mash"} 是包含执行点的方法名，
     *   {@code "MyClass.java"} 是包含执行点的源文件，
     *   {@code "9"} 是包含执行点的源代码行号。
     * <li>
     *   {@code "MyClass.mash(MyClass.java)"} - 与上述相同，但行号不可用。
     * <li>
     *   {@code "MyClass.mash(Unknown Source)"} - 与上述相同，但文件名和行号均不可用。
     * <li>
     *   {@code "MyClass.mash(Native Method)"} - 与上述相同，但文件名和行号均不可用，且包含执行点的方法已知是本地方法。
     * </ul>
     * @see    Throwable#printStackTrace()
     */
    public String toString() {
        return getClassName() + "." + methodName +
            (isNativeMethod() ? "(Native Method)" :
             (fileName != null && lineNumber >= 0 ?
              "(" + fileName + ":" + lineNumber + ")" :
              (fileName != null ?  "("+fileName+")" : "(Unknown Source)")));
    }

    /**
     * 如果指定的对象是另一个表示与该实例相同的执行点的
     * {@code StackTraceElement} 实例，则返回 true。两个堆栈跟踪元素 {@code a} 和
     * {@code b} 相等当且仅当：
     * <pre>{@code
     *     equals(a.getFileName(), b.getFileName()) &&
     *     a.getLineNumber() == b.getLineNumber()) &&
     *     equals(a.getClassName(), b.getClassName()) &&
     *     equals(a.getMethodName(), b.getMethodName())
     * }</pre>
     * 其中 {@code equals} 具有 {@link
     * java.util.Objects#equals(Object, Object) Objects.equals} 的语义。
     *
     * @param  obj 要与此堆栈跟踪元素进行比较的对象。
     * @return 如果指定的对象是另一个表示与该实例相同的执行点的
     *         {@code StackTraceElement} 实例，则返回 true。
     */
    public boolean equals(Object obj) {
        if (obj==this)
            return true;
        if (!(obj instanceof StackTraceElement))
            return false;
        StackTraceElement e = (StackTraceElement)obj;
        return e.declaringClass.equals(declaringClass) &&
            e.lineNumber == lineNumber &&
            Objects.equals(methodName, e.methodName) &&
            Objects.equals(fileName, e.fileName);
    }

    /**
     * 返回此堆栈跟踪元素的哈希码值。
     */
    public int hashCode() {
        int result = 31*declaringClass.hashCode() + methodName.hashCode();
        result = 31*result + Objects.hashCode(fileName);
        result = 31*result + lineNumber;
        return result;
    }

    private static final long serialVersionUID = 6992337162326171013L;
}
