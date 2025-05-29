/*
 * Copyright (c) 2007, 2009, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.file;

/**
 * 当路径字符串无法转换为 {@link Path} 时抛出的未检查异常，因为路径字符串包含无效字符，或者由于其他文件系统特定的原因而无效。
 */

public class InvalidPathException
    extends IllegalArgumentException
{
    static final long serialVersionUID = 4355821422286746137L;

    private String input;
    private int index;

    /**
     * 从给定的输入字符串、原因和错误索引构造一个实例。
     *
     * @param  input   输入字符串
     * @param  reason  解释输入被拒绝的原因的字符串
     * @param  index   错误发生的位置的索引，
     *                 或 <tt>-1</tt> 如果索引未知
     *
     * @throws  NullPointerException
     *          如果输入或原因字符串为 <tt>null</tt>
     *
     * @throws  IllegalArgumentException
     *          如果错误索引小于 <tt>-1</tt>
     */
    public InvalidPathException(String input, String reason, int index) {
        super(reason);
        if ((input == null) || (reason == null))
            throw new NullPointerException();
        if (index < -1)
            throw new IllegalArgumentException();
        this.input = input;
        this.index = index;
    }

    /**
     * 从给定的输入字符串和原因构造一个实例。结果对象的错误索引为 <tt>-1</tt>。
     *
     * @param  input   输入字符串
     * @param  reason  解释输入被拒绝的原因的字符串
     *
     * @throws  NullPointerException
     *          如果输入或原因字符串为 <tt>null</tt>
     */
    public InvalidPathException(String input, String reason) {
        this(input, reason, -1);
    }

    /**
     * 返回输入字符串。
     *
     * @return  输入字符串
     */
    public String getInput() {
        return input;
    }

    /**
     * 返回解释输入字符串被拒绝的原因的字符串。
     *
     * @return  原因字符串
     */
    public String getReason() {
        return super.getMessage();
    }

    /**
     * 返回输入字符串中错误发生位置的索引，如果该位置未知则返回 <tt>-1</tt>。
     *
     * @return  错误索引
     */
    public int getIndex() {
        return index;
    }

    /**
     * 返回描述错误的字符串。结果字符串由原因字符串、冒号字符
     * (<tt>':'</tt>)、空格和输入字符串组成。如果错误索引已定义，则字符串 <tt>" at index "</tt> 后跟索引（十进制）将插入到原因字符串和冒号字符之间。
     *
     * @return  描述错误的字符串
     */
    public String getMessage() {
        StringBuffer sb = new StringBuffer();
        sb.append(getReason());
        if (index > -1) {
            sb.append(" at index ");
            sb.append(index);
        }
        sb.append(": ");
        sb.append(input);
        return sb.toString();
    }
}
