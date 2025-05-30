/*
 * Copyright (c) 1997, 2012, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.datatransfer;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;


/**
 * 一个多用途互联网邮件扩展（MIME）类型，如 RFC 2045 和 2046 中定义的。
 *
 * 这 *不是* - 重复 *不是* - 一个公共类！DataFlavor 是公共接口，而这是一个 ***私有***（即 *不是* 公共的）辅助类！
 */
class MimeType implements Externalizable, Cloneable {

    /*
     * 序列化支持
     */

    static final long serialVersionUID = -6568722458793895906L;

    /**
     * 用于外部化的构造函数；此构造函数不应直接由应用程序调用，因为结果将是一个未初始化的、不可变的 <code>MimeType</code> 对象。
     */
    public MimeType() {
    }

    /**
     * 从 <code>String</code> 构建 <code>MimeType</code>。
     *
     * @param rawdata 用于初始化 <code>MimeType</code> 的文本
     * @throws NullPointerException 如果 <code>rawdata</code> 为 null
     */
    public MimeType(String rawdata) throws MimeTypeParseException {
        parse(rawdata);
    }

    /**
     * 使用给定的主类型和子类型构建 <code>MimeType</code>，但参数列表为空。
     *
     * @param primary 此 <code>MimeType</code> 的主类型
     * @param sub 此 <code>MimeType</code> 的子类型
     * @throws NullPointerException 如果 <code>primary</code> 或 <code>sub</code> 为 null
     */
    public MimeType(String primary, String sub) throws MimeTypeParseException {
        this(primary, sub, new MimeTypeParameterList());
    }

    /**
     * 使用预定义的且有效（或为空）的参数列表构建 <code>MimeType</code>。
     *
     * @param primary 此 <code>MimeType</code> 的主类型
     * @param sub 此 <code>MimeType</code> 的子类型
     * @param mtpl 请求的参数列表
     * @throws NullPointerException 如果 <code>primary</code>、<code>sub</code> 或 <code>mtpl</code> 为 null
     */
    public MimeType(String primary, String sub, MimeTypeParameterList mtpl) throws
MimeTypeParseException {
        // 检查主类型是否有效
        if(isValidToken(primary)) {
            primaryType = primary.toLowerCase(Locale.ENGLISH);
        } else {
            throw new MimeTypeParseException("主类型无效。");
        }

        // 检查子类型是否有效
        if(isValidToken(sub)) {
            subType = sub.toLowerCase(Locale.ENGLISH);
        } else {
            throw new MimeTypeParseException("子类型无效。");
        }

        parameters = (MimeTypeParameterList)mtpl.clone();
    }

    public int hashCode() {

        // 我们将所有字符串的哈希码相加。这样，字符串的顺序无关紧要
        int code = 0;
        code += primaryType.hashCode();
        code += subType.hashCode();
        code += parameters.hashCode();
        return code;
    } // hashCode()

    /**
     * 如果主类型、子类型和参数都相等，则 <code>MimeType</code> 相等。不考虑默认值。
     * @param thatObject 要评估为 <code>MimeType</code> 的对象
     * @return 如果 <code>thatObject</code> 是 <code>MimeType</code>，则返回 <code>true</code>；否则返回 <code>false</code>
     */
    public boolean equals(Object thatObject) {
        if (!(thatObject instanceof MimeType)) {
            return false;
        }
        MimeType that = (MimeType)thatObject;
        boolean isIt =
            ((this.primaryType.equals(that.primaryType)) &&
             (this.subType.equals(that.subType)) &&
             (this.parameters.equals(that.parameters)));
        return isIt;
    } // equals()

    /**
     * 从字符串中解析 MIME 类型的例程。
     *
     * @throws NullPointerException 如果 <code>rawdata</code> 为 null
     */
    private void parse(String rawdata) throws MimeTypeParseException {
        int slashIndex = rawdata.indexOf('/');
        int semIndex = rawdata.indexOf(';');
        if((slashIndex < 0) && (semIndex < 0)) {
            // 两个字符都不存在，因此将其视为错误
            throw new MimeTypeParseException("无法找到子类型。");
        } else if((slashIndex < 0) && (semIndex >= 0)) {
            // 存在 ';'（因此存在参数列表），但没有 '/' 表示存在子类型
            throw new MimeTypeParseException("无法找到子类型。");
        } else if((slashIndex >= 0) && (semIndex < 0)) {
            // 存在主类型和子类型，但没有参数列表
            primaryType = rawdata.substring(0,slashIndex).
                trim().toLowerCase(Locale.ENGLISH);
            subType = rawdata.substring(slashIndex + 1).
                trim().toLowerCase(Locale.ENGLISH);
            parameters = new MimeTypeParameterList();
        } else if (slashIndex < semIndex) {
            // 三个项目按正确顺序存在
            primaryType = rawdata.substring(0, slashIndex).
                trim().toLowerCase(Locale.ENGLISH);
            subType = rawdata.substring(slashIndex + 1,
                semIndex).trim().toLowerCase(Locale.ENGLISH);
            parameters = new
MimeTypeParameterList(rawdata.substring(semIndex));
        } else {
            // ';' 在 '/' 之前出现，表示存在主类型和参数列表，但没有子类型
            throw new MimeTypeParseException("无法找到子类型。");
        }

        // 现在验证主类型和子类型

        // 检查主类型是否有效
        if(!isValidToken(primaryType)) {
            throw new MimeTypeParseException("主类型无效。");
        }

        // 检查子类型是否有效
        if(!isValidToken(subType)) {
            throw new MimeTypeParseException("子类型无效。");
        }
    }

    /**
     * 获取此对象的主类型。
     */
    public String getPrimaryType() {
        return primaryType;
    }

    /**
     * 获取此对象的子类型。
     */
    public String getSubType() {
        return subType;
    }

    /**
     * 获取此对象的参数列表的副本。
     */
    public MimeTypeParameterList getParameters() {
        return (MimeTypeParameterList)parameters.clone();
    }

    /**
     * 获取与给定名称关联的值，如果没有当前关联，则返回 null。
     */
    public String getParameter(String name) {
        return parameters.get(name);
    }

    /**
     * 设置与给定名称关联的值，替换任何先前的关联。
     *
     * @throw IllegalArgumentException 如果参数或值非法
     */
    public void setParameter(String name, String value) {
        parameters.set(name, value);
    }

    /**
     * 删除与给定名称关联的任何值。
     *
     * @throw IllegalArgumentExcpetion 如果参数不可删除
     */
    public void removeParameter(String name) {
        parameters.remove(name);
    }

    /**
     * 返回此对象的字符串表示形式。
     */
    public String toString() {
        return getBaseType() + parameters.toString();
    }

    /**
     * 返回此对象的字符串表示形式，不包括参数列表。
     */
    public String getBaseType() {
        return primaryType + "/" + subType;
    }

    /**
     * 如果此对象的主类型和子类型与指定的 <code>type</code> 相同，则返回 <code>true</code>；否则返回 <code>false</code>。
     *
     * @param type 要与 <code>this</code> 的类型进行比较的类型
     * @return 如果此对象的主类型和子类型与指定的 <code>type</code> 相同，则返回 <code>true</code>；否则返回 <code>false</code>
     */
    public boolean match(MimeType type) {
        if (type == null)
            return false;
        return primaryType.equals(type.getPrimaryType())
                    && (subType.equals("*")
                            || type.getSubType().equals("*")
                            || (subType.equals(type.getSubType())));
    }

    /**
     * 如果此对象的主类型和子类型与 <code>rawdata</code> 中描述的内容类型相同，则返回 <code>true</code>；否则返回 <code>false</code>。
     *
     * @param rawdata 要检查的原始数据
     * @return 如果此对象的主类型和子类型与 <code>rawdata</code> 中描述的内容类型相同，则返回 <code>true</code>；否则返回 <code>false</code>；如果 <code>rawdata</code> 为 <code>null</code>，则返回 <code>false</code>
     */
    public boolean match(String rawdata) throws MimeTypeParseException {
        if (rawdata == null)
            return false;
        return match(new MimeType(rawdata));
    }

    /**
     * 对象实现 writeExternal 方法以通过调用 DataOutput 的方法保存其原始值或调用 ObjectOutput 的 writeObject 方法来保存对象、字符串和数组。
     * @exception IOException 包括可能发生的任何 I/O 异常
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        String s = toString(); // 仅包含 ASCII 字符
        // ASCII 字符和 UTF 字符串中的字节之间存在一对一的对应关系
        if (s.length() <= 65535) { // 65535 是 UTF 字符串的最大长度
            out.writeUTF(s);
        } else {
            out.writeByte(0);
            out.writeByte(0);
            out.writeInt(s.length());
            out.write(s.getBytes());
        }
    }

    /**
     * 对象实现 readExternal 方法以通过调用 DataInput 的方法恢复其内容，对于原始类型调用 readObject 方法恢复对象、字符串和数组。
     * readExternal 方法必须按与 writeExternal 写入相同的顺序和类型读取值。
     * @exception ClassNotFoundException 如果无法找到要恢复的对象的类。
     */
    public void readExternal(ObjectInput in) throws IOException,
ClassNotFoundException {
        String s = in.readUTF();
        if (s == null || s.length() == 0) { // 长 MIME 类型
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len = in.readInt();
            while (len-- > 0) {
                baos.write(in.readByte());
            }
            s = baos.toString();
        }
        try {
            parse(s);
        } catch(MimeTypeParseException e) {
            throw new IOException(e.toString());
        }
    }

    /**
     * 返回此对象的克隆。
     * @return 此对象的克隆
     */

    public Object clone() {
        MimeType newObj = null;
        try {
            newObj = (MimeType)super.clone();
        } catch (CloneNotSupportedException cannotHappen) {
        }
        newObj.parameters = (MimeTypeParameterList)parameters.clone();
        return newObj;
    }

    private String    primaryType;
    private String    subType;
    private MimeTypeParameterList parameters;

    // 以下是与解析相关的吓人内容

    /**
     * 确定给定字符是否属于合法的 token。
     */
    private static boolean isTokenChar(char c) {
        return ((c > 040) && (c < 0177)) && (TSPECIALS.indexOf(c) < 0);
    }

    /**
     * 确定给定字符串是否为合法的 token。
     *
     * @throws NullPointerException 如果 <code>s</code> 为 null
     */
    private boolean isValidToken(String s) {
        int len = s.length();
        if(len > 0) {
            for (int i = 0; i < len; ++i) {
                char c = s.charAt(i);
                if (!isTokenChar(c)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 包含所有特殊字符的字符串。
     */

    private static final String TSPECIALS = "()<>@,;:\\\"/[]?=";

} // class MimeType
