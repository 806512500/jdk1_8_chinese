
/*
 * 版权所有 (c) 1996, 2013, Oracle 和/或其附属公司。保留所有权利。
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

/*
 * 版权所有 (C) 1996, 1997 - Taligent, Inc. 保留所有权利
 * 版权所有 (C) 1996 - 1998 - IBM Corp. 保留所有权利
 *
 * 本源代码和文档的原始版本由 Taligent, Inc. 版权所有并拥有，Taligent, Inc. 是 IBM 的全资子公司。这些材料是根据 Taligent 和 Sun 之间的许可协议提供的。这项技术受多项美国和国际专利保护。
 *
 * 本通知和对 Taligent 的归属不得移除。Taligent 是 Taligent, Inc. 的注册商标。
 *
 */

package java.text;

/**
 * <code>StringCharacterIterator</code> 为 <code>String</code> 实现了
 * <code>CharacterIterator</code> 协议。 <code>StringCharacterIterator</code> 类遍历整个 <code>String</code>。
 *
 * @see CharacterIterator
 */

public final class StringCharacterIterator implements CharacterIterator
{
    private String text;
    private int begin;
    private int end;
    // 不变量：begin <= pos <= end
    private int pos;

    /**
     * 构造一个初始索引为 0 的迭代器。
     *
     * @param text 要迭代的 {@code String}
     */
    public StringCharacterIterator(String text)
    {
        this(text, 0);
    }

    /**
     * 构造一个具有指定初始索引的迭代器。
     *
     * @param  text   要迭代的字符串
     * @param  pos    迭代器的初始位置
     */
    public StringCharacterIterator(String text, int pos)
    {
        this(text, 0, text.length(), pos);
    }

    /**
     * 构造一个在给定字符串的给定范围内迭代的迭代器，并将索引设置在指定位置。
     *
     * @param  text   要迭代的字符串
     * @param  begin  第一个字符的索引
     * @param  end    最后一个字符之后的字符索引
     * @param  pos    迭代器的初始位置
     */
    public StringCharacterIterator(String text, int begin, int end, int pos) {
        if (text == null)
            throw new NullPointerException();
        this.text = text;

        if (begin < 0 || begin > end || end > text.length())
            throw new IllegalArgumentException("无效的子字符串范围");

        if (pos < begin || pos > end)
            throw new IllegalArgumentException("无效的位置");

        this.begin = begin;
        this.end = end;
        this.pos = pos;
    }

    /**
     * 重置此迭代器以指向新字符串。此包可见方法由其他 java.text 类使用，以避免每次调用 setText 方法时都分配新的 StringCharacterIterator 对象。
     *
     * @param  text   要迭代的字符串
     * @since 1.2
     */
    public void setText(String text) {
        if (text == null)
            throw new NullPointerException();
        this.text = text;
        this.begin = 0;
        this.end = text.length();
        this.pos = 0;
    }

    /**
     * 为 String 实现 CharacterIterator.first()。
     * @see CharacterIterator#first
     */
    public char first()
    {
        pos = begin;
        return current();
    }

    /**
     * 为 String 实现 CharacterIterator.last()。
     * @see CharacterIterator#last
     */
    public char last()
    {
        if (end != begin) {
            pos = end - 1;
        } else {
            pos = end;
        }
        return current();
     }

    /**
     * 为 String 实现 CharacterIterator.setIndex()。
     * @see CharacterIterator#setIndex
     */
    public char setIndex(int p)
    {
        if (p < begin || p > end)
            throw new IllegalArgumentException("无效的索引");
        pos = p;
        return current();
    }

    /**
     * 为 String 实现 CharacterIterator.current()。
     * @see CharacterIterator#current
     */
    public char current()
    {
        if (pos >= begin && pos < end) {
            return text.charAt(pos);
        }
        else {
            return DONE;
        }
    }

    /**
     * 为 String 实现 CharacterIterator.next()。
     * @see CharacterIterator#next
     */
    public char next()
    {
        if (pos < end - 1) {
            pos++;
            return text.charAt(pos);
        }
        else {
            pos = end;
            return DONE;
        }
    }

    /**
     * 为 String 实现 CharacterIterator.previous()。
     * @see CharacterIterator#previous
     */
    public char previous()
    {
        if (pos > begin) {
            pos--;
            return text.charAt(pos);
        }
        else {
            return DONE;
        }
    }

    /**
     * 为 String 实现 CharacterIterator.getBeginIndex()。
     * @see CharacterIterator#getBeginIndex
     */
    public int getBeginIndex()
    {
        return begin;
    }

    /**
     * 为 String 实现 CharacterIterator.getEndIndex()。
     * @see CharacterIterator#getEndIndex
     */
    public int getEndIndex()
    {
        return end;
    }

    /**
     * 为 String 实现 CharacterIterator.getIndex()。
     * @see CharacterIterator#getIndex
     */
    public int getIndex()
    {
        return pos;
    }

    /**
     * 比较两个 StringCharacterIterator 对象的相等性。
     * @param obj 要比较的 StringCharacterIterator 对象。
     * @return 如果给定的 obj 与此 StringCharacterIterator 对象相同，则返回 true；否则返回 false。
     */
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!(obj instanceof StringCharacterIterator))
            return false;

        StringCharacterIterator that = (StringCharacterIterator) obj;

        if (hashCode() != that.hashCode())
            return false;
        if (!text.equals(that.text))
            return false;
        if (pos != that.pos || begin != that.begin || end != that.end)
            return false;
        return true;
    }
}

                /**
     * 计算此迭代器的哈希码。
     * @return 哈希码
     */
    public int hashCode()
    {
        return text.hashCode() ^ pos ^ begin ^ end;
    }

    /**
     * 创建此迭代器的副本。
     * @return 此迭代器的副本
     */
    public Object clone()
    {
        try {
            StringCharacterIterator other
            = (StringCharacterIterator) super.clone();
            return other;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

}
