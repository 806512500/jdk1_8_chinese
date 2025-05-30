
/*
 * Copyright (c) 2000, 2015, Oracle and/or its affiliates. All rights reserved.
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
package java.beans;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

/**
 * <code>XMLEncoder</code> 类是 <code>ObjectOutputStream</code> 的补充替代品，可以用于生成 <em>JavaBean</em> 的文本表示，
 * 类似于 <code>ObjectOutputStream</code> 用于创建 <code>Serializable</code> 对象的二进制表示。例如，以下代码片段可以用于创建
 * 提供的 <em>JavaBean</em> 及其所有属性的文本表示：
 * <pre>
 *       XMLEncoder e = new XMLEncoder(
 *                          new BufferedOutputStream(
 *                              new FileOutputStream("Test.xml")));
 *       e.writeObject(new JButton("Hello, world"));
 *       e.close();
 * </pre>
 * 尽管它们的 API 类似，<code>XMLEncoder</code> 类专门设计用于存档 <em>JavaBean</em> 图的文本表示。像 Java 源文件一样，
 * 以这种方式编写的文档对所涉及类的实现变化具有天然的免疫力。<code>ObjectOutputStream</code> 仍推荐用于进程间通信和通用序列化。
 * <p>
 * <code>XMLEncoder</code> 类为 <em>JavaBean</em> 提供了一个默认的表示形式，其中它们被表示为符合 XML 1.0 规范和
 * Unicode/ISO 10646 字符集的 UTF-8 字符编码的 XML 文档。由 <code>XMLEncoder</code> 类生成的 XML 文档具有以下特点：
 * <ul>
 * <li>
 * <em>可移植且版本弹性</em>：它们不依赖于任何类的私有实现，因此，像 Java 源文件一样，它们可以在具有不同版本的某些类的环境中交换，
 * 也可以在不同供应商的虚拟机之间交换。
 * <li>
 * <em>结构紧凑</em>：<code>XMLEncoder</code> 类内部使用一种 <em>冗余消除</em> 算法，因此 Bean 属性的默认值不会写入流中。
 * <li>
 * <em>容错</em>：文件中的非结构错误，无论是由文件损坏还是存档中类的 API 变化引起的，都保持局部化，
 * 使得读取器可以报告错误并继续加载文档中未受错误影响的部分。
 * </ul>
 * <p>
 * 以下是一个包含 <em>swing</em> 工具包中一些用户界面组件的 XML 存档示例：
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;java version="1.0" class="java.beans.XMLDecoder"&gt;
 * &lt;object class="javax.swing.JFrame"&gt;
 *   &lt;void property="name"&gt;
 *     &lt;string&gt;frame1&lt;/string&gt;
 *   &lt;/void&gt;
 *   &lt;void property="bounds"&gt;
 *     &lt;object class="java.awt.Rectangle"&gt;
 *       &lt;int&gt;0&lt;/int&gt;
 *       &lt;int&gt;0&lt;/int&gt;
 *       &lt;int&gt;200&lt;/int&gt;
 *       &lt;int&gt;200&lt;/int&gt;
 *     &lt;/object&gt;
 *   &lt;/void&gt;
 *   &lt;void property="contentPane"&gt;
 *     &lt;void method="add"&gt;
 *       &lt;object class="javax.swing.JButton"&gt;
 *         &lt;void property="label"&gt;
 *           &lt;string&gt;Hello&lt;/string&gt;
 *         &lt;/void&gt;
 *       &lt;/object&gt;
 *     &lt;/void&gt;
 *   &lt;/void&gt;
 *   &lt;void property="visible"&gt;
 *     &lt;boolean&gt;true&lt;/boolean&gt;
 *   &lt;/void&gt;
 * &lt;/object&gt;
 * &lt;/java&gt;
 * </pre>
 * XML 语法使用以下约定：
 * <ul>
 * <li>
 * 每个元素表示一个方法调用。
 * <li>
 * "object" 标签表示一个 <em>表达式</em>，其值将用作封闭元素的参数。
 * <li>
 * "void" 标签表示一个 <em>语句</em>，该语句将被执行，但其结果不会用作封闭方法的参数。
 * <li>
 * 包含元素的元素使用这些元素作为参数，除非它们具有 "void" 标签。
 * <li>
 * 方法的名称由 "method" 属性表示。
 * <li>
 * XML 的标准 "id" 和 "idref" 属性用于引用先前的表达式，以处理对象图中的循环引用。
 * <li>
 * "class" 属性用于显式指定静态方法或构造函数的目标；其值是类的完全限定名称。
 * <li>
 * 具有 "void" 标签的元素在未由 "class" 属性定义目标时使用外部上下文作为目标。
 * <li>
 * Java 的 String 类被特别处理，写为 &lt;string&gt;Hello, world&lt;/string&gt;，其中字符串的字符使用 UTF-8 字符编码转换为字节。
 * </ul>
 * <p>
 * 虽然所有对象图都可以仅使用这三个标签编写，但以下定义包含在内，以便更简洁地表达常见的数据结构：
 * <p>
 * <ul>
 * <li>
 * 默认方法名称为 "new"。
 * <li>
 * Java 类的引用写为 &lt;class&gt;javax.swing.JButton&lt;/class&gt;。
 * <li>
 * Java 基本类型的包装类的实例使用基本类型的名称作为标签。例如，<code>Integer</code> 类的实例可以写为：
 * &lt;int&gt;123&lt;/int&gt;。注意，<code>XMLEncoder</code> 类使用 Java 的反射包，其中 Java 的基本类型和其关联的 "包装类" 之间的转换由内部处理。
 * <code>XMLEncoder</code> 类本身的 API 仅处理 <code>Object</code>。
 * <li>
 * 在表示名称以 "get" 开头的无参数方法的元素中，"method" 属性被 "property" 属性替换，其值由移除 "get" 前缀并小写化结果得到。
 * <li>
 * 在表示名称以 "set" 开头的单参数方法的元素中，"method" 属性被 "property" 属性替换，其值由移除 "set" 前缀并小写化结果得到。
 * <li>
 * 在表示名称为 "get" 且接受一个整数参数的方法的元素中，"method" 属性被 "index" 属性替换，其值为第一个参数的值。
 * <li>
 * 在表示名称为 "set" 且接受两个参数的方法的元素中，第一个参数为整数，"method" 属性被 "index" 属性替换，其值为第一个参数的值。
 * <li>
 * 数组的引用使用 "array" 标签写入。"class" 和 "length" 属性分别指定数组的子类型和长度。
 * </ul>
 *
 *<p>
 * 更多信息，您还可以查看
 * <a
 href="http://java.sun.com/products/jfc/tsc/articles/persistence4">使用 XMLEncoder</a>，
 * 该文章发表在 <em>The Swing Connection</em> 上。
 * @see XMLDecoder
 * @see java.io.ObjectOutputStream
 *
 * @since 1.4
 *
 * @author Philip Milne
 */
public class XMLEncoder extends Encoder implements AutoCloseable {

    private final CharsetEncoder encoder;
    private final String charset;
    private final boolean declaration;

    private OutputStreamWriter out;
    private Object owner;
    private int indentation = 0;
    private boolean internal = false;
    private Map<Object, ValueData> valueToExpression;
    private Map<Object, List<Statement>> targetToStatementList;
    private boolean preambleWritten = false;
    private NameGenerator nameGenerator;

    private class ValueData {
        public int refs = 0;
        public boolean marked = false; // 标记 -> refs > 0 除非引用是目标。
        public String name = null;
        public Expression exp = null;
    }

    /**
     * 创建一个新的 XML 编码器，用于使用 XML 编码将 <em>JavaBeans</em> 写入流 <code>out</code>。
     *
     * @param out  将写入对象的 XML 表示的流
     *
     * @throws  IllegalArgumentException
     *          如果 <code>out</code> 为 <code>null</code>
     *
     * @see XMLDecoder#XMLDecoder(InputStream)
     */
    public XMLEncoder(OutputStream out) {
        this(out, "UTF-8", true, 0);
    }

    /**
     * 创建一个新的 XML 编码器，用于使用给定的 <code>charset</code> 从给定的 <code>indentation</code> 开始将 <em>JavaBeans</em> 写入流 <code>out</code>。
     *
     * @param out          将写入对象的 XML 表示的流
     * @param charset      请求的字符集的名称；可以是规范名称或别名
     * @param declaration  是否生成 XML 声明；当将内容嵌入到另一个 XML 文档中时，设置为 <code>false</code>
     * @param indentation  整个 XML 文档的缩进空格数
     *
     * @throws  IllegalArgumentException
     *          如果 <code>out</code> 或 <code>charset</code> 为 <code>null</code>，或 <code>indentation</code> 小于 0
     *
     * @throws  IllegalCharsetNameException
     *          如果 <code>charset</code> 名称不合法
     *
     * @throws  UnsupportedCharsetException
     *          如果此 Java 虚拟机实例不支持命名的字符集
     *
     * @throws  UnsupportedOperationException
     *          如果加载的字符集不支持编码
     *
     * @see Charset#forName(String)
     *
     * @since 1.7
     */
    public XMLEncoder(OutputStream out, String charset, boolean declaration, int indentation) {
        if (out == null) {
            throw new IllegalArgumentException("输出流不能为 null");
        }
        if (indentation < 0) {
            throw new IllegalArgumentException("缩进必须 >= 0");
        }
        Charset cs = Charset.forName(charset);
        this.encoder = cs.newEncoder();
        this.charset = charset;
        this.declaration = declaration;
        this.indentation = indentation;
        this.out = new OutputStreamWriter(out, cs.newEncoder());
        valueToExpression = new IdentityHashMap<>();
        targetToStatementList = new IdentityHashMap<>();
        nameGenerator = new NameGenerator();
    }

    /**
     * 将此编码器的所有者设置为 <code>owner</code>。
     *
     * @param owner 此编码器的所有者。
     *
     * @see #getOwner
     */
    public void setOwner(Object owner) {
        this.owner = owner;
        writeExpression(new Expression(this, "getOwner", new Object[0]));
    }

    /**
     * 获取此编码器的所有者。
     *
     * @return 此编码器的所有者。
     *
     * @see #setOwner
     */
    public Object getOwner() {
        return owner;
    }

    /**
     * 将指定对象的 XML 表示写入输出。
     *
     * @param o 要写入流的对象。
     *
     * @see XMLDecoder#readObject
     */
    public void writeObject(Object o) {
        if (internal) {
            super.writeObject(o);
        }
        else {
            writeStatement(new Statement(this, "writeObject", new Object[]{o}));
        }
    }

    private List<Statement> statementList(Object target) {
        List<Statement> list = targetToStatementList.get(target);
        if (list == null) {
            list = new ArrayList<>();
            targetToStatementList.put(target, list);
        }
        return list;
    }


    private void mark(Object o, boolean isArgument) {
        if (o == null || o == this) {
            return;
        }
        ValueData d = getValueData(o);
        Expression exp = d.exp;
        // 不标记字面字符串。其他字符串，例如来自资源包的字符串，仍然需要标记。
        if (o.getClass() == String.class && exp == null) {
            return;
        }

        // 增加所有参数的引用计数
        if (isArgument) {
            d.refs++;
        }
        if (d.marked) {
            return;
        }
        d.marked = true;
        Object target = exp.getTarget();
        mark(exp);
        if (!(target instanceof Class)) {
            statementList(target).add(exp);
            // 待解决：为什么需要在这里增加引用计数？
            d.refs++;
        }
    }

    private void mark(Statement stm) {
        Object[] args = stm.getArguments();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            mark(arg, true);
        }
        mark(stm.getTarget(), stm instanceof Expression);
    }


    /**
     * 记录 Statement，以便在流刷新时编码器生成实际输出。
     * <P>
     * 此方法仅应在初始化持久性代理的上下文中调用。
     *
     * @param oldStm 将写入流的语句。
     * @see java.beans.PersistenceDelegate#initialize
     */
    public void writeStatement(Statement oldStm) {
        // System.out.println("XMLEncoder::writeStatement: " + oldStm);
        boolean internal = this.internal;
        this.internal = true;
        try {
            super.writeStatement(oldStm);
            /*
               注意我们必须首先进行标记，因为我们可能需要此上下文中先前值的结果
               用于此语句。
               测试用例是：
                   os.setOwner(this);
                   os.writeObject(this);
            */
            mark(oldStm);
            Object target = oldStm.getTarget();
            if (target instanceof Field) {
                String method = oldStm.getMethodName();
                Object[] args = oldStm.getArguments();
                if ((method == null) || (args == null)) {
                }
                else if (method.equals("get") && (args.length == 1)) {
                    target = args[0];
                }
                else if (method.equals("set") && (args.length == 2)) {
                    target = args[0];
                }
            }
            statementList(target).add(oldStm);
        }
        catch (Exception e) {
            getExceptionListener().exceptionThrown(new Exception("XMLEncoder: discarding statement " + oldStm, e));
        }
        this.internal = internal;
    }


    /**
     * 记录表达式，以便编码器在流刷新时生成实际输出。
     * <P>
     * 该方法仅应在初始化持久性代理或设置编码器以从资源包读取的上下文中调用。
     * <P>
     * 有关使用资源包与 XMLEncoder 的更多信息，请参阅
     * http://java.sun.com/products/jfc/tsc/articles/persistence4/#i18n
     *
     * @param oldExp 将被写入流的表达式。
     * @see java.beans.PersistenceDelegate#initialize
     */
    public void writeExpression(Expression oldExp) {
        boolean internal = this.internal;
        this.internal = true;
        Object oldValue = getValue(oldExp);
        if (get(oldValue) == null || (oldValue instanceof String && !internal)) {
            getValueData(oldValue).exp = oldExp;
            super.writeExpression(oldExp);
        }
        this.internal = internal;
    }

    /**
     * 如果尚未写入，则写入与 XML 编码相关的前言，并写出自上次调用 <code>flush</code>
     * 以来写入流的所有值。刷新后，清除所有对写入此流的值的内部引用。
     */
    public void flush() {
        if (!preambleWritten) { // 不要在构造函数中执行此操作 - 它会抛出 ... 暂停。
            if (this.declaration) {
                writeln("<?xml version=" + quote("1.0") +
                            " encoding=" + quote(this.charset) + "?>");
            }
            writeln("<java version=" + quote(System.getProperty("java.version")) +
                           " class=" + quote(XMLDecoder.class.getName()) + ">");
            preambleWritten = true;
        }
        indentation++;
        List<Statement> statements = statementList(this);
        while (!statements.isEmpty()) {
            Statement s = statements.remove(0);
            if ("writeObject".equals(s.getMethodName())) {
                outputValue(s.getArguments()[0], this, true);
            }
            else {
                outputStatement(s, this, false);
            }
        }
        indentation--;

        Statement statement = getMissedStatement();
        while (statement != null) {
            outputStatement(statement, this, false);
            statement = getMissedStatement();
        }

        try {
            out.flush();
        }
        catch (IOException e) {
            getExceptionListener().exceptionThrown(e);
        }
        clear();
    }

    void clear() {
        super.clear();
        nameGenerator.clear();
        valueToExpression.clear();
        targetToStatementList.clear();
    }

    Statement getMissedStatement() {
        for (List<Statement> statements : this.targetToStatementList.values()) {
            for (int i = 0; i < statements.size(); i++) {
                if (Statement.class == statements.get(i).getClass()) {
                    return statements.remove(i);
                }
            }
        }
        return null;
    }


    /**
     * 调用 <code>flush</code>，写入关闭的后言，然后关闭与此流关联的输出流。
     */
    public void close() {
        flush();
        writeln("</java>");
        try {
            out.close();
        }
        catch (IOException e) {
            getExceptionListener().exceptionThrown(e);
        }
    }

    private String quote(String s) {
        return "\"" + s + "\"";
    }

    private ValueData getValueData(Object o) {
        ValueData d = valueToExpression.get(o);
        if (d == null) {
            d = new ValueData();
            valueToExpression.put(o, d);
        }
        return d;
    }

    /**
     * 如果参数（Unicode 代码点）在 XML 文档中有效，则返回 <code>true</code>。
     * Unicode 字符适合 Unicode 代码点的低十六位，成对的 Unicode <em>代理字符</em>可以组合
     * 以在仅包含 Unicode 的文档中编码 Unicode 代码点。
     * （Java 编程语言中的 <code>char</code> 数据类型表示 Unicode 字符，包括未配对的代理字符。）
     * <par>
     * [2] Char ::= #x0009 | #x000A | #x000D
     *            | [#x0020-#xD7FF]
     *            | [#xE000-#xFFFD]
     *            | [#x10000-#x10ffff]
     * </par>
     *
     * @param code 被测试的 32 位 Unicode 代码点
     * @return 如果 Unicode 代码点有效，则返回 <code>true</code>，否则返回 <code>false</code>
     */
    private static boolean isValidCharCode(int code) {
        return (0x0020 <= code && code <= 0xD7FF)
            || (0x000A == code)
            || (0x0009 == code)
            || (0x000D == code)
            || (0xE000 <= code && code <= 0xFFFD)
            || (0x10000 <= code && code <= 0x10ffff);
    }

    private void writeln(String exp) {
        try {
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < indentation; i++) {
                sb.append(' ');
            }
            sb.append(exp);
            sb.append('\n');
            this.out.write(sb.toString());
        }
        catch (IOException e) {
            getExceptionListener().exceptionThrown(e);
        }
    }

    private void outputValue(Object value, Object outer, boolean isArgument) {
        if (value == null) {
            writeln("<null/>");
            return;
        }

        if (value instanceof Class) {
            writeln("<class>" + ((Class)value).getName() + "</class>");
            return;
        }

        ValueData d = getValueData(value);
        if (d.exp != null) {
            Object target = d.exp.getTarget();
            String methodName = d.exp.getMethodName();

            if (target == null || methodName == null) {
                throw new NullPointerException((target == null ? "target" :
                                                "methodName") + " should not be null");
            }

            if (isArgument && target instanceof Field && methodName.equals("get")) {
                Field f = (Field) target;
                if (Modifier.isStatic(f.getModifiers())) {
                    writeln("<object class=" + quote(f.getDeclaringClass().getName()) +
                            " field=" + quote(f.getName()) + "/>");
                    return;
                }
            }

            Class<?> primitiveType = primitiveTypeFor(value.getClass());
            if (primitiveType != null && target == value.getClass() &&
                methodName.equals("new")) {
                String primitiveTypeName = primitiveType.getName();
                // 确保字符类型正确引用。
                if (primitiveType == Character.TYPE) {
                    char code = ((Character) value).charValue();
                    if (!isValidCharCode(code)) {
                        writeln(createString(code));
                        return;
                    }
                    value = quoteCharCode(code);
                    if (value == null) {
                        value = Character.valueOf(code);
                    }
                }
                writeln("<" + primitiveTypeName + ">" + value + "</" +
                        primitiveTypeName + ">");
                return;
            }

        } else if (value instanceof String) {
            writeln(createString((String) value));
            return;
        }

        if (d.name != null) {
            if (isArgument) {
                writeln("<object idref=" + quote(d.name) + "/>");
            }
            else {
                outputXML("void", " idref=" + quote(d.name), value);
            }
        }
        else if (d.exp != null) {
            outputStatement(d.exp, outer, isArgument);
        }
    }

    private static String quoteCharCode(int code) {
        switch(code) {
          case '&':  return "&amp;";
          case '<':  return "&lt;";
          case '>':  return "&gt;";
          case '"':  return "&quot;";
          case '\'': return "&apos;";
          case '\r': return "&#13;";
          default:   return null;
        }
    }

    private static String createString(int code) {
        return "<char code=\"#" + Integer.toString(code, 16) + "\"/>";
    }

    private String createString(String string) {
        StringBuilder sb = new StringBuilder();
        sb.append("<string>");
        int index = 0;
        while (index < string.length()) {
            int point = string.codePointAt(index);
            int count = Character.charCount(point);

            if (isValidCharCode(point) && this.encoder.canEncode(string.substring(index, index + count))) {
                String value = quoteCharCode(point);
                if (value != null) {
                    sb.append(value);
                } else {
                    sb.appendCodePoint(point);
                }
                index += count;
            } else {
                sb.append(createString(string.charAt(index)));
                index++;
            }
        }
        sb.append("</string>");
        return sb.toString();
    }

    private void outputStatement(Statement exp, Object outer, boolean isArgument) {
        Object target = exp.getTarget();
        String methodName = exp.getMethodName();

        if (target == null || methodName == null) {
            throw new NullPointerException((target == null ? "target" :
                                            "methodName") + " should not be null");
        }

        Object[] args = exp.getArguments();
        boolean expression = exp.getClass() == Expression.class;
        Object value = (expression) ? getValue((Expression)exp) : null;

        String tag = (expression && isArgument) ? "object" : "void";
        String attributes = "";
        ValueData d = getValueData(value);

        // 特殊情况处理目标。
        if (target == outer) {
        }
        else if (target == Array.class && methodName.equals("newInstance")) {
            tag = "array";
            attributes = attributes + " class=" + quote(((Class)args[0]).getName());
            attributes = attributes + " length=" + quote(args[1].toString());
            args = new Object[]{};
        }
        else if (target.getClass() == Class.class) {
            attributes = attributes + " class=" + quote(((Class)target).getName());
        }
        else {
            d.refs = 2;
            if (d.name == null) {
                getValueData(target).refs++;
                List<Statement> statements = statementList(target);
                if (!statements.contains(exp)) {
                    statements.add(exp);
                }
                outputValue(target, outer, false);
            }
            if (expression) {
                outputValue(value, outer, isArgument);
            }
            return;
        }
        if (expression && (d.refs > 1)) {
            String instanceName = nameGenerator.instanceName(value);
            d.name = instanceName;
            attributes = attributes + " id=" + quote(instanceName);
        }

        // 特殊情况处理方法。
        if ((!expression && methodName.equals("set") && args.length == 2 &&
             args[0] instanceof Integer) ||
             (expression && methodName.equals("get") && args.length == 1 &&
              args[0] instanceof Integer)) {
            attributes = attributes + " index=" + quote(args[0].toString());
            args = (args.length == 1) ? new Object[]{} : new Object[]{args[1]};
        }
        else if ((!expression && methodName.startsWith("set") && args.length == 1) ||
                 (expression && methodName.startsWith("get") && args.length == 0)) {
            if (3 < methodName.length()) {
                attributes = attributes + " property=" +
                    quote(Introspector.decapitalize(methodName.substring(3)));
            }
        }
        else if (!methodName.equals("new") && !methodName.equals("newInstance")) {
            attributes = attributes + " method=" + quote(methodName);
        }
        outputXML(tag, attributes, value, args);
    }

    private void outputXML(String tag, String attributes, Object value, Object... args) {
        List<Statement> statements = statementList(value);
        // 当没有正文时，使用 XML 的简短形式。
        if (args.length == 0 && statements.size() == 0) {
            writeln("<" + tag + attributes + "/>");
            return;
        }

        writeln("<" + tag + attributes + ">");
        indentation++;

        for(int i = 0; i < args.length; i++) {
            outputValue(args[i], null, true);
        }

        while (!statements.isEmpty()) {
            Statement s = statements.remove(0);
            outputStatement(s, value, false);
        }

        indentation--;
        writeln("</" + tag + ">");
    }

    @SuppressWarnings("rawtypes")
    static Class primitiveTypeFor(Class wrapper) {
        if (wrapper == Boolean.class) return Boolean.TYPE;
        if (wrapper == Byte.class) return Byte.TYPE;
        if (wrapper == Character.class) return Character.TYPE;
        if (wrapper == Short.class) return Short.TYPE;
        if (wrapper == Integer.class) return Integer.TYPE;
        if (wrapper == Long.class) return Long.TYPE;
        if (wrapper == Float.class) return Float.TYPE;
        if (wrapper == Double.class) return Double.TYPE;
        if (wrapper == Void.class) return Void.TYPE;
        return null;
    }
}
