/*
 * 版权所有 (c) 2000, 2007, Oracle 和/或其附属公司。保留所有权利。
 *
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
 *
 */

// -- 本文件由机器生成：请勿编辑！ -- //

package java.nio.charset;


/**
 * 当请求的字符集不受支持时抛出的未检查异常。
 *
 * @since 1.4
 */

public class UnsupportedCharsetException
    extends IllegalArgumentException
{

    private static final long serialVersionUID = 1490765524727386367L;

    private String charsetName;

    /**
     * 构造此类的一个实例。
     *
     * @param  charsetName
     *         不支持的字符集的名称
     */
    public UnsupportedCharsetException(String charsetName) {
        super(String.valueOf(charsetName));
	this.charsetName = charsetName;
    }

    /**
     * 获取不支持的字符集的名称。
     *
     * @return  不支持的字符集的名称
     */
    public String getCharsetName() {
        return charsetName;
    }

}
