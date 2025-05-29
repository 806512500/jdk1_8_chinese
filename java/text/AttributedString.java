
/*
 * 版权所有 (c) 1997, 2020, Oracle 和/或其附属公司。保留所有权利。
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

package java.text;

import java.util.*;
import java.text.AttributedCharacterIterator.Attribute;

/**
 * AttributedString 保存文本和相关的属性信息。在某些情况下，它可以作为文本读取器通过 AttributedCharacterIterator
 * 接口访问带属性的文本的实际数据存储。
 *
 * <p>
 * 属性是一个键/值对，由键标识。给定字符上的两个属性不能具有相同的键。
 *
 * <p>属性的值是不可变的，或者客户端或存储不应对其进行修改。它们始终通过引用传递，而不是克隆。
 *
 * @see AttributedCharacterIterator
 * @see Annotation
 * @since 1.2
 */

public class AttributedString {

    // 由于没有 int 的向量，我们不得不使用数组。
    // 我们以 10 个元素的块分配它们，因此不必一直分配。
    private static final int ARRAY_SIZE_INCREMENT = 10;

    // 保存文本的字段
    String text;

    // 保存运行属性信息的字段
    // 运行属性按运行组织
    int runArraySize;               // 当前数组的大小
    int runCount;                   // 实际运行数，<= runArraySize
    int runStarts[];                // 每个运行的起始索引
    Vector<Attribute> runAttributes[];         // 每个运行的属性键向量
    Vector<Object> runAttributeValues[];    // 每个运行的属性值并行向量

    /**
     * 使用给定的 AttributedCharacterIterators 构造 AttributedString 实例。
     *
     * @param iterators 用于构造 AttributedString 的 AttributedCharacterIterators。
     * @throws NullPointerException 如果 iterators 为 null
     */
    AttributedString(AttributedCharacterIterator[] iterators) {
        if (iterators == null) {
            throw new NullPointerException("Iterators must not be null");
        }
        if (iterators.length == 0) {
            text = "";
        }
        else {
            // 构建字符串内容
            StringBuffer buffer = new StringBuffer();
            for (int counter = 0; counter < iterators.length; counter++) {
                appendContents(buffer, iterators[counter]);
            }

            text = buffer.toString();

            if (!text.isEmpty()) {
                // 确定运行，当属性不同时创建新的运行。
                int offset = 0;
                Map<Attribute,Object> last = null;

                for (int counter = 0; counter < iterators.length; counter++) {
                    AttributedCharacterIterator iterator = iterators[counter];
                    int start = iterator.getBeginIndex();
                    int end = iterator.getEndIndex();
                    int index = start;

                    while (index < end) {
                        iterator.setIndex(index);

                        Map<Attribute,Object> attrs = iterator.getAttributes();

                        if (mapsDiffer(last, attrs)) {
                            setAttributes(attrs, index - start + offset);
                        }
                        last = attrs;
                        index = iterator.getRunLimit();
                    }
                    offset += (end - start);
                }
            }
        }
    }

    /**
     * 使用给定的文本构造 AttributedString 实例。
     * @param text 此带属性字符串的文本。
     * @exception NullPointerException 如果 <code>text</code> 为 null。
     */
    public AttributedString(String text) {
        if (text == null) {
            throw new NullPointerException();
        }
        this.text = text;
    }

    /**
     * 使用给定的文本和属性构造 AttributedString 实例。
     * @param text 此带属性字符串的文本。
     * @param attributes 适用于整个字符串的属性。
     * @exception NullPointerException 如果 <code>text</code> 或
     *            <code>attributes</code> 为 null。
     * @exception IllegalArgumentException 如果文本长度为 0
     * 且属性参数不是空 Map（属性不能应用于 0 长度的范围）。
     */
    public AttributedString(String text,
                            Map<? extends Attribute, ?> attributes)
    {
        if (text == null || attributes == null) {
            throw new NullPointerException();
        }
        this.text = text;

        if (text.isEmpty()) {
            if (attributes.isEmpty())
                return;
            throw new IllegalArgumentException("Can't add attribute to 0-length text");
        }

        int attributeCount = attributes.size();
        if (attributeCount > 0) {
            createRunAttributeDataVectors();
            Vector<Attribute> newRunAttributes = new Vector<>(attributeCount);
            Vector<Object> newRunAttributeValues = new Vector<>(attributeCount);
            runAttributes[0] = newRunAttributes;
            runAttributeValues[0] = newRunAttributeValues;

            Iterator<? extends Map.Entry<? extends Attribute, ?>> iterator = attributes.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<? extends Attribute, ?> entry = iterator.next();
                newRunAttributes.addElement(entry.getKey());
                newRunAttributeValues.addElement(entry.getValue());
            }
        }
    }

    /**
     * 使用由 AttributedCharacterIterator 表示的带属性文本构造 AttributedString 实例。
     * @param text 此带属性字符串的文本。
     * @exception NullPointerException 如果 <code>text</code> 为 null。
     */
    public AttributedString(AttributedCharacterIterator text) {
        // 如果性能至关重要，此构造函数应在此处实现，而不是调用子范围的构造函数。我们可以在循环中避免一些范围检查。
        this(text, text.getBeginIndex(), text.getEndIndex(), null);
    }


                /**
     * 构造一个 AttributedString 实例，表示由 AttributedCharacterIterator 表示的给定带属性文本的子范围。如果给定范围产生空文本，所有属性将被丢弃。注意，任何由 Annotation 对象包装的属性在原始属性范围的子范围内都会被丢弃。
     *
     * @param text 该带属性字符串的文本。
     * @param beginIndex 范围中第一个字符的索引。
     * @param endIndex 范围中最后一个字符之后的字符的索引。
     * @exception NullPointerException 如果 <code>text</code> 为 null。
     * @exception IllegalArgumentException 如果由 beginIndex 和 endIndex 给出的子范围超出文本范围。
     * @see java.text.Annotation
     */
    public AttributedString(AttributedCharacterIterator text,
                            int beginIndex,
                            int endIndex) {
        this(text, beginIndex, endIndex, null);
    }

    /**
     * 构造一个 AttributedString 实例，表示由 AttributedCharacterIterator 表示的给定带属性文本的子范围。只有与给定属性匹配的属性才会被合并到实例中。如果给定范围产生空文本，所有属性将被丢弃。注意，任何由 Annotation 对象包装的属性在原始属性范围的子范围内都会被丢弃。
     *
     * @param text 该带属性字符串的文本。
     * @param beginIndex 范围中第一个字符的索引。
     * @param endIndex 范围中最后一个字符之后的字符的索引。
     * @param attributes 指定要从文本中提取的属性。如果指定为 null，则使用所有可用属性。
     * @exception NullPointerException 如果 <code>text</code> 为 null。
     * @exception IllegalArgumentException 如果由 beginIndex 和 endIndex 给出的子范围超出文本范围。
     * @see java.text.Annotation
     */
    public AttributedString(AttributedCharacterIterator text,
                            int beginIndex,
                            int endIndex,
                            Attribute[] attributes) {
        if (text == null) {
            throw new NullPointerException();
        }

        // 验证给定的子范围
        int textBeginIndex = text.getBeginIndex();
        int textEndIndex = text.getEndIndex();
        if (beginIndex < textBeginIndex || endIndex > textEndIndex || beginIndex > endIndex)
            throw new IllegalArgumentException("无效的子字符串范围");

        // 复制给定的字符串
        StringBuffer textBuffer = new StringBuffer();
        text.setIndex(beginIndex);
        for (char c = text.current(); text.getIndex() < endIndex; c = text.next())
            textBuffer.append(c);
        this.text = textBuffer.toString();

        if (beginIndex == endIndex)
            return;

        // 选择要处理的属性键
        HashSet<Attribute> keys = new HashSet<>();
        if (attributes == null) {
            keys.addAll(text.getAllAttributeKeys());
        } else {
            for (int i = 0; i < attributes.length; i++)
                keys.add(attributes[i]);
            keys.retainAll(text.getAllAttributeKeys());
        }
        if (keys.isEmpty())
            return;

        // 获取并设置每个属性名称的属性运行。需要从文本的顶部扫描，以便我们可以丢弃任何不再应用于子文本段的 Annotation。
        Iterator<Attribute> itr = keys.iterator();
        while (itr.hasNext()) {
            Attribute attributeKey = itr.next();
            text.setIndex(textBeginIndex);
            while (text.getIndex() < endIndex) {
                int start = text.getRunStart(attributeKey);
                int limit = text.getRunLimit(attributeKey);
                Object value = text.getAttribute(attributeKey);

                if (value != null) {
                    if (value instanceof Annotation) {
                        if (start >= beginIndex && limit <= endIndex) {
                            addAttribute(attributeKey, value, start - beginIndex, limit - beginIndex);
                        } else {
                            if (limit > endIndex)
                                break;
                        }
                    } else {
                        // 如果运行超出给定（子集）范围，我们不需要进一步处理。
                        if (start >= endIndex)
                            break;
                        if (limit > beginIndex) {
                            // 属性应用于任何子范围
                            if (start < beginIndex)
                                start = beginIndex;
                            if (limit > endIndex)
                                limit = endIndex;
                            if (start != limit) {
                                addAttribute(attributeKey, value, start - beginIndex, limit - beginIndex);
                            }
                        }
                    }
                }
                text.setIndex(limit);
            }
        }
    }

    /**
     * 将属性添加到整个字符串。
     * @param attribute 属性键
     * @param value 属性的值；可以为 null
     * @exception NullPointerException 如果 <code>attribute</code> 为 null。
     * @exception IllegalArgumentException 如果 AttributedString 的长度为 0（属性不能应用于 0 长度的范围）。
     */
    public void addAttribute(Attribute attribute, Object value) {

        if (attribute == null) {
            throw new NullPointerException();
        }

        int len = length();
        if (len == 0) {
            throw new IllegalArgumentException("不能将属性添加到 0 长度的文本");
        }

        addAttributeImpl(attribute, value, 0, len);
    }

                /**
     * 向字符串的子范围添加属性。
     * @param attribute 属性键
     * @param value 属性的值。可以为 null。
     * @param beginIndex 范围中第一个字符的索引。
     * @param endIndex 范围中最后一个字符之后的字符的索引。
     * @exception NullPointerException 如果 <code>attribute</code> 为 null。
     * @exception IllegalArgumentException 如果 beginIndex 小于 0，endIndex 大于字符串长度，或者 beginIndex 和 endIndex 不能定义字符串的非空子范围。
     */
    public void addAttribute(Attribute attribute, Object value,
            int beginIndex, int endIndex) {

        if (attribute == null) {
            throw new NullPointerException();
        }

        if (beginIndex < 0 || endIndex > length() || beginIndex >= endIndex) {
            throw new IllegalArgumentException("无效的子字符串范围");
        }

        addAttributeImpl(attribute, value, beginIndex, endIndex);
    }

    /**
     * 向字符串的子范围添加一组属性。
     * @param attributes 要添加到字符串的属性。
     * @param beginIndex 范围中第一个字符的索引。
     * @param endIndex 范围中最后一个字符之后的字符的索引。
     * @exception NullPointerException 如果 <code>attributes</code> 为 null。
     * @exception IllegalArgumentException 如果 beginIndex 小于 0，endIndex 大于字符串长度，或者 beginIndex 和 endIndex 不能定义字符串的非空子范围，并且 attributes 参数不是空 Map。
     */
    public void addAttributes(Map<? extends Attribute, ?> attributes,
                              int beginIndex, int endIndex)
    {
        if (attributes == null) {
            throw new NullPointerException();
        }

        if (beginIndex < 0 || endIndex > length() || beginIndex > endIndex) {
            throw new IllegalArgumentException("无效的子字符串范围");
        }
        if (beginIndex == endIndex) {
            if (attributes.isEmpty())
                return;
            throw new IllegalArgumentException("不能向长度为0的文本添加属性");
        }

        // 确保我们有运行属性数据向量
        if (runCount == 0) {
            createRunAttributeDataVectors();
        }

        // 必要时拆分运行
        int beginRunIndex = ensureRunBreak(beginIndex);
        int endRunIndex = ensureRunBreak(endIndex);

        Iterator<? extends Map.Entry<? extends Attribute, ?>> iterator =
            attributes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<? extends Attribute, ?> entry = iterator.next();
            addAttributeRunData(entry.getKey(), entry.getValue(), beginRunIndex, endRunIndex);
        }
    }

    private synchronized void addAttributeImpl(Attribute attribute, Object value,
            int beginIndex, int endIndex) {

        // 确保我们有运行属性数据向量
        if (runCount == 0) {
            createRunAttributeDataVectors();
        }

        // 必要时拆分运行
        int beginRunIndex = ensureRunBreak(beginIndex);
        int endRunIndex = ensureRunBreak(endIndex);

        addAttributeRunData(attribute, value, beginRunIndex, endRunIndex);
    }

    private final void createRunAttributeDataVectors() {
        // 使用临时变量，以确保在发生异常时保持一致性
        int newRunStarts[] = new int[ARRAY_SIZE_INCREMENT];

        @SuppressWarnings("unchecked")
        Vector<Attribute> newRunAttributes[] = (Vector<Attribute>[]) new Vector<?>[ARRAY_SIZE_INCREMENT];

        @SuppressWarnings("unchecked")
        Vector<Object> newRunAttributeValues[] = (Vector<Object>[]) new Vector<?>[ARRAY_SIZE_INCREMENT];

        runStarts = newRunStarts;
        runAttributes = newRunAttributes;
        runAttributeValues = newRunAttributeValues;
        runArraySize = ARRAY_SIZE_INCREMENT;
        runCount = 1; // 假设初始运行从索引 0 开始
    }

    // 确保在偏移处有运行中断，返回运行的索引
    private final int ensureRunBreak(int offset) {
        return ensureRunBreak(offset, true);
    }

    /**
     * 确保在偏移处有运行中断，返回运行的索引。如果这导致拆分运行，可能发生两件事：
     * <ul>
     * <li>如果 copyAttrs 为 true，现有运行中的属性将被放置在新创建的两个运行中。
     * <li>如果 copyAttrs 为 false，现有运行中的属性不会被复制到断点右侧（>= 偏移）的运行中，但会存在于断点左侧（< 偏移）的运行中。
     * </ul>
     */
    private final int ensureRunBreak(int offset, boolean copyAttrs) {
        if (offset == length()) {
            return runCount;
        }

        // 搜索此偏移应所在的运行索引
        int runIndex = 0;
        while (runIndex < runCount && runStarts[runIndex] < offset) {
            runIndex++;
        }

        // 如果偏移已经在运行开始处，我们完成了
        if (runIndex < runCount && runStarts[runIndex] == offset) {
            return runIndex;
        }

        // 我们必须拆分一个运行
        // 首先，确保我们的数组中有足够的空间
        if (runCount == runArraySize) {
            int newArraySize = runArraySize + ARRAY_SIZE_INCREMENT;
            int newRunStarts[] = new int[newArraySize];

            @SuppressWarnings("unchecked")
            Vector<Attribute> newRunAttributes[] = (Vector<Attribute>[]) new Vector<?>[newArraySize];

            @SuppressWarnings("unchecked")
            Vector<Object> newRunAttributeValues[] = (Vector<Object>[]) new Vector<?>[newArraySize];

            for (int i = 0; i < runArraySize; i++) {
                newRunStarts[i] = runStarts[i];
                newRunAttributes[i] = runAttributes[i];
                newRunAttributeValues[i] = runAttributeValues[i];
            }
            runStarts = newRunStarts;
            runAttributes = newRunAttributes;
            runAttributeValues = newRunAttributeValues;
            runArraySize = newArraySize;
        }


                    // 复制旧运行中包含的新运行的属性信息
        // 使用临时变量以确保在发生异常时保持一致性
        Vector<Attribute> newRunAttributes = null;
        Vector<Object> newRunAttributeValues = null;

        if (copyAttrs) {
            Vector<Attribute> oldRunAttributes = runAttributes[runIndex - 1];
            Vector<Object> oldRunAttributeValues = runAttributeValues[runIndex - 1];
            if (oldRunAttributes != null) {
                newRunAttributes = new Vector<>(oldRunAttributes);
            }
            if (oldRunAttributeValues != null) {
                newRunAttributeValues =  new Vector<>(oldRunAttributeValues);
            }
        }

        // 现在实际分割运行
        runCount++;
        for (int i = runCount - 1; i > runIndex; i--) {
            runStarts[i] = runStarts[i - 1];
            runAttributes[i] = runAttributes[i - 1];
            runAttributeValues[i] = runAttributeValues[i - 1];
        }
        runStarts[runIndex] = offset;
        runAttributes[runIndex] = newRunAttributes;
        runAttributeValues[runIndex] = newRunAttributeValues;

        return runIndex;
    }

    // 将属性/值添加到所有 beginRunIndex <= runIndex < endRunIndex 的运行中
    private void addAttributeRunData(Attribute attribute, Object value,
            int beginRunIndex, int endRunIndex) {

        for (int i = beginRunIndex; i < endRunIndex; i++) {
            int keyValueIndex = -1; // 在向量中的键和值的索引；假设我们还没有条目
            if (runAttributes[i] == null) {
                Vector<Attribute> newRunAttributes = new Vector<>();
                Vector<Object> newRunAttributeValues = new Vector<>();
                runAttributes[i] = newRunAttributes;
                runAttributeValues[i] = newRunAttributeValues;
            } else {
                // 检查是否已经有条目
                keyValueIndex = runAttributes[i].indexOf(attribute);
            }

            if (keyValueIndex == -1) {
                // 创建新条目
                int oldSize = runAttributes[i].size();
                runAttributes[i].addElement(attribute);
                try {
                    runAttributeValues[i].addElement(value);
                }
                catch (Exception e) {
                    runAttributes[i].setSize(oldSize);
                    runAttributeValues[i].setSize(oldSize);
                }
            } else {
                // 更新现有条目
                runAttributeValues[i].set(keyValueIndex, value);
            }
        }
    }

    /**
     * 创建一个 AttributedCharacterIterator 实例，提供对此字符串全部内容的访问。
     *
     * @return 提供对文本及其属性访问的迭代器。
     */
    public AttributedCharacterIterator getIterator() {
        return getIterator(null, 0, length());
    }

    /**
     * 创建一个 AttributedCharacterIterator 实例，提供对此字符串部分内容的访问。
     * 实现者可能拥有的未在属性中列出的信息不需要通过迭代器访问。
     * 如果列表为 null，则应提供所有可用的属性信息。
     *
     * @param attributes 客户端感兴趣的属性列表
     * @return 提供对整个文本及其选定属性访问的迭代器
     */
    public AttributedCharacterIterator getIterator(Attribute[] attributes) {
        return getIterator(attributes, 0, length());
    }

    /**
     * 创建一个 AttributedCharacterIterator 实例，提供对此字符串部分内容的访问。
     * 实现者可能拥有的未在属性中列出的信息不需要通过迭代器访问。
     * 如果列表为 null，则应提供所有可用的属性信息。
     *
     * @param attributes 客户端感兴趣的属性列表
     * @param beginIndex 第一个字符的索引
     * @param endIndex 最后一个字符之后的字符索引
     * @return 提供对文本及其属性访问的迭代器
     * @exception IllegalArgumentException 如果 beginIndex 小于 0，endIndex 大于字符串长度，或 beginIndex 大于 endIndex。
     */
    public AttributedCharacterIterator getIterator(Attribute[] attributes, int beginIndex, int endIndex) {
        return new AttributedStringIterator(attributes, beginIndex, endIndex);
    }

    // 所有（除了 length 之外的）读取操作都是私有的，
    // 因为 AttributedString 实例是通过迭代器访问的。

    // length 是包私有的，以便 CharacterIteratorFieldDelegate 可以
    // 在不创建 AttributedCharacterIterator 的情况下访问它。
    int length() {
        return text.length();
    }

    private char charAt(int index) {
        return text.charAt(index);
    }

    private synchronized Object getAttribute(Attribute attribute, int runIndex) {
        Vector<Attribute> currentRunAttributes = runAttributes[runIndex];
        Vector<Object> currentRunAttributeValues = runAttributeValues[runIndex];
        if (currentRunAttributes == null) {
            return null;
        }
        int attributeIndex = currentRunAttributes.indexOf(attribute);
        if (attributeIndex != -1) {
            return currentRunAttributeValues.elementAt(attributeIndex);
        }
        else {
            return null;
        }
    }

    // 获取属性值，但如果注解的范围超出 beginIndex..endIndex 范围，则不返回注解
    private Object getAttributeCheckRange(Attribute attribute, int runIndex, int beginIndex, int endIndex) {
        Object value = getAttribute(attribute, runIndex);
        if (value instanceof Annotation) {
            // 需要检查注解的范围是否超出迭代器的范围
            if (beginIndex > 0) {
                int currIndex = runIndex;
                int runStart = runStarts[currIndex];
                while (runStart >= beginIndex &&
                        valuesMatch(value, getAttribute(attribute, currIndex - 1))) {
                    currIndex--;
                    runStart = runStarts[currIndex];
                }
                if (runStart < beginIndex) {
                    // 注解的范围在迭代器范围之前开始
                    return null;
                }
            }
            int textLength = length();
            if (endIndex < textLength) {
                int currIndex = runIndex;
                int runLimit = (currIndex < runCount - 1) ? runStarts[currIndex + 1] : textLength;
                while (runLimit <= endIndex &&
                        valuesMatch(value, getAttribute(attribute, currIndex + 1))) {
                    currIndex++;
                    runLimit = (currIndex < runCount - 1) ? runStarts[currIndex + 1] : textLength;
                }
                if (runLimit > endIndex) {
                    // 注解的范围在迭代器范围之后结束
                    return null;
                }
            }
            // 注解的范围是迭代器范围的子范围，
            // 因此可以返回值
        }
        return value;
    }

                // 返回指定索引的运行中所有指定属性的值是否相等
    private boolean attributeValuesMatch(Set<? extends Attribute> attributes, int runIndex1, int runIndex2) {
        Iterator<? extends Attribute> iterator = attributes.iterator();
        while (iterator.hasNext()) {
            Attribute key = iterator.next();
           if (!valuesMatch(getAttribute(key, runIndex1), getAttribute(key, runIndex2))) {
                return false;
            }
        }
        return true;
    }

    // 返回两个对象是否都为 null 或者相等
    private final static boolean valuesMatch(Object value1, Object value2) {
        if (value1 == null) {
            return value2 == null;
        } else {
            return value1.equals(value2);
        }
    }

    /**
     * 将 CharacterIterator 迭代器的内容追加到 StringBuffer buf 中。
     */
    private final void appendContents(StringBuffer buf,
                                      CharacterIterator iterator) {
        int index = iterator.getBeginIndex();
        int end = iterator.getEndIndex();

        while (index < end) {
            iterator.setIndex(index++);
            buf.append(iterator.current());
        }
    }

    /**
     * 将属性范围从偏移量到下一个运行中断（通常是文本的末尾）设置为 attrs 中指定的属性。
     * 仅应在构造函数中调用！
     */
    private void setAttributes(Map<Attribute, Object> attrs, int offset) {
        if (runCount == 0) {
            createRunAttributeDataVectors();
        }

        int index = ensureRunBreak(offset, false);
        int size;

        if (attrs != null && (size = attrs.size()) > 0) {
            Vector<Attribute> runAttrs = new Vector<>(size);
            Vector<Object> runValues = new Vector<>(size);
            Iterator<Map.Entry<Attribute, Object>> iterator = attrs.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<Attribute, Object> entry = iterator.next();

                runAttrs.add(entry.getKey());
                runValues.add(entry.getValue());
            }
            runAttributes[index] = runAttrs;
            runAttributeValues[index] = runValues;
        }
    }

    /**
     * 如果 last 和 attrs 中指定的属性不同，则返回 true。
     */
    private static <K,V> boolean mapsDiffer(Map<K, V> last, Map<K, V> attrs) {
        if (last == null) {
            return (attrs != null && attrs.size() > 0);
        }
        return (!last.equals(attrs));
    }


    // 与此字符串类关联的迭代器类

    final private class AttributedStringIterator implements AttributedCharacterIterator {

        // 关于同步的说明：
        // 我们不对迭代器进行同步，假设迭代器只在一个线程中使用。
        // 但是，我们确实会对 AttributedString 进行同步访问，因为它更可能在多个线程之间共享。

        // 我们的迭代的开始和结束索引
        private int beginIndex;
        private int endIndex;

        // 客户端感兴趣的属性
        private Attribute[] relevantAttributes;

        // 我们迭代的当前索引
        // 不变量：beginIndex <= currentIndex <= endIndex
        private int currentIndex;

        // 包含 currentIndex 的运行信息
        private int currentRunIndex;
        private int currentRunStart;
        private int currentRunLimit;

        // 构造函数
        AttributedStringIterator(Attribute[] attributes, int beginIndex, int endIndex) {

            if (beginIndex < 0 || beginIndex > endIndex || endIndex > length()) {
                throw new IllegalArgumentException("Invalid substring range");
            }

            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
            this.currentIndex = beginIndex;
            updateRunInfo();
            if (attributes != null) {
                relevantAttributes = attributes.clone();
            }
        }

        // Object 方法。请参阅该类的文档。

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AttributedStringIterator)) {
                return false;
            }

            AttributedStringIterator that = (AttributedStringIterator) obj;

            if (AttributedString.this != that.getString())
                return false;
            if (currentIndex != that.currentIndex || beginIndex != that.beginIndex || endIndex != that.endIndex)
                return false;
            return true;
        }

        public int hashCode() {
            return text.hashCode() ^ currentIndex ^ beginIndex ^ endIndex;
        }

        public Object clone() {
            try {
                AttributedStringIterator other = (AttributedStringIterator) super.clone();
                return other;
            }
            catch (CloneNotSupportedException e) {
                throw new InternalError(e);
            }
        }

        // CharacterIterator 方法。请参阅该接口的文档。

        public char first() {
            return internalSetIndex(beginIndex);
        }

        public char last() {
            if (endIndex == beginIndex) {
                return internalSetIndex(endIndex);
            } else {
                return internalSetIndex(endIndex - 1);
            }
        }

        public char current() {
            if (currentIndex == endIndex) {
                return DONE;
            } else {
                return charAt(currentIndex);
            }
        }

        public char next() {
            if (currentIndex < endIndex) {
                return internalSetIndex(currentIndex + 1);
            }
            else {
                return DONE;
            }
        }

        public char previous() {
            if (currentIndex > beginIndex) {
                return internalSetIndex(currentIndex - 1);
            }
            else {
                return DONE;
            }
        }


                    public char setIndex(int position) {
            if (position < beginIndex || position > endIndex)
                throw new IllegalArgumentException("无效的索引");
            return internalSetIndex(position);
        }

        public int getBeginIndex() {
            return beginIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }

        public int getIndex() {
            return currentIndex;
        }

        // AttributedCharacterIterator 方法。参见该接口的文档。

        public int getRunStart() {
            return currentRunStart;
        }

        public int getRunStart(Attribute attribute) {
            if (currentRunStart == beginIndex || currentRunIndex == -1) {
                return currentRunStart;
            } else {
                Object value = getAttribute(attribute);
                int runStart = currentRunStart;
                int runIndex = currentRunIndex;
                while (runStart > beginIndex &&
                        valuesMatch(value, AttributedString.this.getAttribute(attribute, runIndex - 1))) {
                    runIndex--;
                    runStart = runStarts[runIndex];
                }
                if (runStart < beginIndex) {
                    runStart = beginIndex;
                }
                return runStart;
            }
        }

        public int getRunStart(Set<? extends Attribute> attributes) {
            if (currentRunStart == beginIndex || currentRunIndex == -1) {
                return currentRunStart;
            } else {
                int runStart = currentRunStart;
                int runIndex = currentRunIndex;
                while (runStart > beginIndex &&
                        AttributedString.this.attributeValuesMatch(attributes, currentRunIndex, runIndex - 1)) {
                    runIndex--;
                    runStart = runStarts[runIndex];
                }
                if (runStart < beginIndex) {
                    runStart = beginIndex;
                }
                return runStart;
            }
        }

        public int getRunLimit() {
            return currentRunLimit;
        }

        public int getRunLimit(Attribute attribute) {
            if (currentRunLimit == endIndex || currentRunIndex == -1) {
                return currentRunLimit;
            } else {
                Object value = getAttribute(attribute);
                int runLimit = currentRunLimit;
                int runIndex = currentRunIndex;
                while (runLimit < endIndex &&
                        valuesMatch(value, AttributedString.this.getAttribute(attribute, runIndex + 1))) {
                    runIndex++;
                    runLimit = runIndex < runCount - 1 ? runStarts[runIndex + 1] : endIndex;
                }
                if (runLimit > endIndex) {
                    runLimit = endIndex;
                }
                return runLimit;
            }
        }

        public int getRunLimit(Set<? extends Attribute> attributes) {
            if (currentRunLimit == endIndex || currentRunIndex == -1) {
                return currentRunLimit;
            } else {
                int runLimit = currentRunLimit;
                int runIndex = currentRunIndex;
                while (runLimit < endIndex &&
                        AttributedString.this.attributeValuesMatch(attributes, currentRunIndex, runIndex + 1)) {
                    runIndex++;
                    runLimit = runIndex < runCount - 1 ? runStarts[runIndex + 1] : endIndex;
                }
                if (runLimit > endIndex) {
                    runLimit = endIndex;
                }
                return runLimit;
            }
        }

        public Map<Attribute,Object> getAttributes() {
            if (runAttributes == null || currentRunIndex == -1 || runAttributes[currentRunIndex] == null) {
                // ??? 最好能返回 null，但当前规范不允许
                // 返回 Hashtable 可以避免 AttributeMap 处理空的情况
                return new Hashtable<>();
            }
            return new AttributeMap(currentRunIndex, beginIndex, endIndex);
        }

        public Set<Attribute> getAllAttributeKeys() {
            // ??? 这应该筛选出与客户端无关的属性键
            if (runAttributes == null) {
                // ??? 最好能返回 null，但当前规范不允许
                // 返回 HashSet 可以避免处理空的情况
                return new HashSet<>();
            }
            synchronized (AttributedString.this) {
                // ??? 应该尝试只创建一次，然后在必要时更新，
                // 并给调用者只读视图
                Set<Attribute> keys = new HashSet<>();
                int i = 0;
                while (i < runCount) {
                    if (runStarts[i] < endIndex && (i == runCount - 1 || runStarts[i + 1] > beginIndex)) {
                        Vector<Attribute> currentRunAttributes = runAttributes[i];
                        if (currentRunAttributes != null) {
                            int j = currentRunAttributes.size();
                            while (j-- > 0) {
                                keys.add(currentRunAttributes.get(j));
                            }
                        }
                    }
                    i++;
                }
                return keys;
            }
        }

        public Object getAttribute(Attribute attribute) {
            int runIndex = currentRunIndex;
            if (runIndex < 0) {
                return null;
            }
            return AttributedString.this.getAttributeCheckRange(attribute, runIndex, beginIndex, endIndex);
        }

        // 内部使用的方法

        private AttributedString getString() {
            return AttributedString.this;
        }

        // 设置当前索引，必要时更新当前运行的信息，
        // 返回当前索引处的字符
        private char internalSetIndex(int position) {
            currentIndex = position;
            if (position < currentRunStart || position >= currentRunLimit) {
                updateRunInfo();
            }
            if (currentIndex == endIndex) {
                return DONE;
            } else {
                return charAt(position);
            }
        }


                    // 更新当前运行的信息
        private void updateRunInfo() {
            if (currentIndex == endIndex) {
                currentRunStart = currentRunLimit = endIndex;
                currentRunIndex = -1;
            } else {
                synchronized (AttributedString.this) {
                    int runIndex = -1;
                    while (runIndex < runCount - 1 && runStarts[runIndex + 1] <= currentIndex)
                        runIndex++;
                    currentRunIndex = runIndex;
                    if (runIndex >= 0) {
                        currentRunStart = runStarts[runIndex];
                        if (currentRunStart < beginIndex)
                            currentRunStart = beginIndex;
                    }
                    else {
                        currentRunStart = beginIndex;
                    }
                    if (runIndex < runCount - 1) {
                        currentRunLimit = runStarts[runIndex + 1];
                        if (currentRunLimit > endIndex)
                            currentRunLimit = endIndex;
                    }
                    else {
                        currentRunLimit = endIndex;
                    }
                }
            }
        }

    }

    // 与该字符串类关联的映射类，提供对一个运行属性的访问

    final private class AttributeMap extends AbstractMap<Attribute,Object> {

        int runIndex;
        int beginIndex;
        int endIndex;

        AttributeMap(int runIndex, int beginIndex, int endIndex) {
            this.runIndex = runIndex;
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
        }

        public Set<Map.Entry<Attribute, Object>> entrySet() {
            HashSet<Map.Entry<Attribute, Object>> set = new HashSet<>();
            synchronized (AttributedString.this) {
                int size = runAttributes[runIndex].size();
                for (int i = 0; i < size; i++) {
                    Attribute key = runAttributes[runIndex].get(i);
                    Object value = runAttributeValues[runIndex].get(i);
                    if (value instanceof Annotation) {
                        value = AttributedString.this.getAttributeCheckRange(key,
                                                             runIndex, beginIndex, endIndex);
                        if (value == null) {
                            continue;
                        }
                    }

                    Map.Entry<Attribute, Object> entry = new AttributeEntry(key, value);
                    set.add(entry);
                }
            }
            return set;
        }

        public Object get(Object key) {
            return AttributedString.this.getAttributeCheckRange((Attribute) key, runIndex, beginIndex, endIndex);
        }
    }
}

class AttributeEntry implements Map.Entry<Attribute,Object> {

    private Attribute key;
    private Object value;

    AttributeEntry(Attribute key, Object value) {
        this.key = key;
        this.value = value;
    }

    public boolean equals(Object o) {
        if (!(o instanceof AttributeEntry)) {
            return false;
        }
        AttributeEntry other = (AttributeEntry) o;
        return other.key.equals(key) &&
            (value == null ? other.value == null : other.value.equals(value));
    }

    public Attribute getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public Object setValue(Object newValue) {
        throw new UnsupportedOperationException();
    }

    public int hashCode() {
        return key.hashCode() ^ (value==null ? 0 : value.hashCode());
    }

    public String toString() {
        return key.toString()+"="+value.toString();
    }
}
