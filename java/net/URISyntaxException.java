/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.net;


/**
 * 表示字符串无法解析为URI引用时抛出的检查异常。
 *
 * @author Mark Reinhold
 * @see URI
 * @since 1.4
 */

public class URISyntaxException
    extends Exception
{
    private static final long serialVersionUID = 2137979680897488891L;

    private String input;
    private int index;

    /**
     * 从给定的输入字符串、原因和错误索引构造一个实例。
     *
     * @param  input   输入字符串
     * @param  reason  解释为什么输入无法解析的字符串
     * @param  index   解析错误发生的位置的索引，
     *                 或者如果索引未知则为 {@code -1}
     *
     * @throws  NullPointerException
     *          如果输入或原因字符串为 {@code null}
     *
     * @throws  IllegalArgumentException
     *          如果错误索引小于 {@code -1}
     */
    public URISyntaxException(String input, String reason, int index) {
        super(reason);
        if ((input == null) || (reason == null))
            throw new NullPointerException();
        if (index < -1)
            throw new IllegalArgumentException();
        this.input = input;
        this.index = index;
    }

    /**
     * 从给定的输入字符串和原因构造一个实例。生成的对象将具有 {@code -1} 的错误索引。
     *
     * @param  input   输入字符串
     * @param  reason  解释为什么输入无法解析的字符串
     *
     * @throws  NullPointerException
     *          如果输入或原因字符串为 {@code null}
     */
    public URISyntaxException(String input, String reason) {
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
     * 返回解释为什么输入字符串无法解析的字符串。
     *
     * @return  原因字符串
     */
    public String getReason() {
        return super.getMessage();
    }

    /**
     * 返回输入字符串中解析错误发生位置的索引，如果该位置未知则返回 {@code -1}。
     *
     * @return  错误索引
     */
    public int getIndex() {
        return index;
    }

    /**
     * 返回描述解析错误的字符串。生成的字符串由原因字符串后跟冒号字符
     * ({@code ':'})、空格和输入字符串组成。如果错误索引已定义，则在原因字符串后和冒号字符前插入
     * 字符串 {@code " at index "} 后跟索引的十进制表示。
     *
     * @return  描述解析错误的字符串
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
