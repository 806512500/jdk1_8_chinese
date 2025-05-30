
/*
 * Copyright (c) 1999, 2011, Oracle and/or its affiliates. All rights reserved.
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

package java.awt;

/**
 * 一组控制打印作业的属性。
 * <p>
 * 该类的实例控制每个使用该实例的打印作业的副本数量、默认选择、目标、打印对话框、文件和打印机名称、页码范围、多文档处理（包括装订）以及多页布局（例如双面打印）。属性名称尽可能符合互联网打印协议 (IPP) 1.1。属性值尽可能部分符合。
 * <p>
 * 要使用接受内部类类型的 方法，传递内部类的常量字段之一的引用。客户端代码不能创建内部类类型的 新实例，因为这些类中没有一个具有公共构造函数。例如，要将打印对话框类型设置为跨平台的纯 Java 打印对话框，使用以下代码：
 * <pre>
 * import java.awt.JobAttributes;
 *
 * public class PureJavaPrintDialogExample {
 *     public void setPureJavaPrintDialog(JobAttributes jobAttributes) {
 *         jobAttributes.setDialog(JobAttributes.DialogType.COMMON);
 *     }
 * }
 * </pre>
 * <p>
 * 每个支持 <i>attributeName</i>-default 值的 IPP 属性都有一个对应的 <code>set<i>attributeName</i>ToDefault</code> 方法。不提供默认值字段。
 *
 * @author      David Mendenhall
 * @since 1.3
 */
public final class JobAttributes implements Cloneable {
    /**
     * 可能的默认选择状态的类型安全枚举。
     * @since 1.3
     */
    public static final class DefaultSelectionType extends AttributeValue {
        private static final int I_ALL = 0;
        private static final int I_RANGE = 1;
        private static final int I_SELECTION = 2;

        private static final String NAMES[] = {
            "all", "range", "selection"
        };

        /**
         * 用于指定应打印作业的所有页面的 <code>DefaultSelectionType</code> 实例。
         */
        public static final DefaultSelectionType ALL =
           new DefaultSelectionType(I_ALL);
        /**
         * 用于指定应打印作业的某个页面范围的 <code>DefaultSelectionType</code> 实例。
         */
        public static final DefaultSelectionType RANGE =
           new DefaultSelectionType(I_RANGE);
        /**
         * 用于指定应打印当前选择的 <code>DefaultSelectionType</code> 实例。
         */
        public static final DefaultSelectionType SELECTION =
           new DefaultSelectionType(I_SELECTION);

        private DefaultSelectionType(int type) {
            super(type, NAMES);
        }
    }

    /**
     * 可能的作业目标的类型安全枚举。
     * @since 1.3
     */
    public static final class DestinationType extends AttributeValue {
        private static final int I_FILE = 0;
        private static final int I_PRINTER = 1;

        private static final String NAMES[] = {
            "file", "printer"
        };

        /**
         * 用于指定打印到文件的 <code>DestinationType</code> 实例。
         */
        public static final DestinationType FILE =
            new DestinationType(I_FILE);
        /**
         * 用于指定打印到打印机的 <code>DestinationType</code> 实例。
         */
        public static final DestinationType PRINTER =
            new DestinationType(I_PRINTER);

        private DestinationType(int type) {
            super(type, NAMES);
        }
    }

    /**
     * 可能的用户显示对话框的类型安全枚举。
     * @since 1.3
     */
    public static final class DialogType extends AttributeValue {
        private static final int I_COMMON = 0;
        private static final int I_NATIVE = 1;
        private static final int I_NONE = 2;

        private static final String NAMES[] = {
            "common", "native", "none"
        };

        /**
         * 用于指定跨平台的纯 Java 打印对话框的 <code>DialogType</code> 实例。
         */
        public static final DialogType COMMON = new DialogType(I_COMMON);
        /**
         * 用于指定平台的本机打印对话框的 <code>DialogType</code> 实例。
         */
        public static final DialogType NATIVE = new DialogType(I_NATIVE);
        /**
         * 用于指定不显示打印对话框的 <code>DialogType</code> 实例。
         */
        public static final DialogType NONE = new DialogType(I_NONE);

        private DialogType(int type) {
            super(type, NAMES);
        }
    }

    /**
     * 可能的多副本处理状态的类型安全枚举。
     * 用于控制单个文档的多个副本的装订方式。
     * @since 1.3
     */
    public static final class MultipleDocumentHandlingType extends
                                                               AttributeValue {
        private static final int I_SEPARATE_DOCUMENTS_COLLATED_COPIES = 0;
        private static final int I_SEPARATE_DOCUMENTS_UNCOLLATED_COPIES = 1;

        private static final String NAMES[] = {
            "separate-documents-collated-copies",
            "separate-documents-uncollated-copies"
        };

        /**
         * 用于指定作业应分为单独的装订副本的 <code>MultipleDocumentHandlingType</code> 实例。
         */
        public static final MultipleDocumentHandlingType
            SEPARATE_DOCUMENTS_COLLATED_COPIES =
                new MultipleDocumentHandlingType(
                    I_SEPARATE_DOCUMENTS_COLLATED_COPIES);
        /**
         * 用于指定作业应分为单独的非装订副本的 <code>MultipleDocumentHandlingType</code> 实例。
         */
        public static final MultipleDocumentHandlingType
            SEPARATE_DOCUMENTS_UNCOLLATED_COPIES =
                new MultipleDocumentHandlingType(
                    I_SEPARATE_DOCUMENTS_UNCOLLATED_COPIES);

        private MultipleDocumentHandlingType(int type) {
            super(type, NAMES);
        }
    }

    /**
     * 可能的多页布局的类型安全枚举。这些布局符合 IPP 1.1。
     * @since 1.3
     */
    public static final class SidesType extends AttributeValue {
        private static final int I_ONE_SIDED = 0;
        private static final int I_TWO_SIDED_LONG_EDGE = 1;
        private static final int I_TWO_SIDED_SHORT_EDGE = 2;

        private static final String NAMES[] = {
            "one-sided", "two-sided-long-edge", "two-sided-short-edge"
        };

        /**
         * 用于指定连续的作业页面应打印在同一侧的连续介质页上的 <code>SidesType</code> 实例。
         */
        public static final SidesType ONE_SIDED = new SidesType(I_ONE_SIDED);
        /**
         * 用于指定连续的作业页面应打印在连续介质页的正面和背面，使得每对页面在介质上的方向对于读者来说是正确的，如同沿长边装订一样。
         */
        public static final SidesType TWO_SIDED_LONG_EDGE =
            new SidesType(I_TWO_SIDED_LONG_EDGE);
        /**
         * 用于指定连续的作业页面应打印在连续介质页的正面和背面，使得每对页面在介质上的方向对于读者来说是正确的，如同沿短边装订一样。
         */
        public static final SidesType TWO_SIDED_SHORT_EDGE =
            new SidesType(I_TWO_SIDED_SHORT_EDGE);

        private SidesType(int type) {
            super(type, NAMES);
        }
    }

    private int copies;
    private DefaultSelectionType defaultSelection;
    private DestinationType destination;
    private DialogType dialog;
    private String fileName;
    private int fromPage;
    private int maxPage;
    private int minPage;
    private MultipleDocumentHandlingType multipleDocumentHandling;
    private int[][] pageRanges;
    private int prFirst;
    private int prLast;
    private String printer;
    private SidesType sides;
    private int toPage;

    /**
     * 构造一个具有所有属性默认值的 <code>JobAttributes</code> 实例。对话框默认为
     * <code>DialogType.NATIVE</code>。最小页码默认为
     * <code>1</code>。最大页码默认为 <code>Integer.MAX_VALUE</code>。
     * 目标默认为 <code>DestinationType.PRINTER</code>。
     * 选择默认为 <code>DefaultSelectionType.ALL</code>。
     * 副本数量默认为 <code>1</code>。多文档处理默认为
     * <code>MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES</code>。
     * 页边距默认为 <code>SidesType.ONE_SIDED</code>。文件名默认为
     * <code>null</code>。
     */
    public JobAttributes() {
        setCopiesToDefault();
        setDefaultSelection(DefaultSelectionType.ALL);
        setDestination(DestinationType.PRINTER);
        setDialog(DialogType.NATIVE);
        setMaxPage(Integer.MAX_VALUE);
        setMinPage(1);
        setMultipleDocumentHandlingToDefault();
        setSidesToDefault();
    }

    /**
     * 构造一个 <code>JobAttributes</code> 实例，该实例是提供的 <code>JobAttributes</code> 的副本。
     *
     * @param   obj 要复制的 <code>JobAttributes</code>
     */
    public JobAttributes(JobAttributes obj) {
        set(obj);
    }

    /**
     * 构造一个具有指定值的 <code>JobAttributes</code> 实例。
     *
     * @param   copies 大于 0 的整数
     * @param   defaultSelection <code>DefaultSelectionType.ALL</code>、
     *          <code>DefaultSelectionType.RANGE</code> 或
     *          <code>DefaultSelectionType.SELECTION</code>
     * @param   destination <code>DesintationType.FILE</code> 或
     *          <code>DesintationType.PRINTER</code>
     * @param   dialog <code>DialogType.COMMON</code>、
     *          <code>DialogType.NATIVE</code> 或
     *          <code>DialogType.NONE</code>
     * @param   fileName 可能为 <code>null</code> 的文件名
     * @param   maxPage 大于零且大于或等于 <i>minPage</i> 的整数
     * @param   minPage 大于零且小于或等于 <i>maxPage</i> 的整数
     * @param   multipleDocumentHandling
     *     <code>MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_COLLATED_COPIES</code> 或
     *     <code>MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES</code>
     * @param   pageRanges 一个包含两个元素的整数数组的数组；一个数组被解释为包括和介于指定页面之间的所有页面的范围；范围必须按升序排列且不得重叠；指定的页码不得小于 <i>minPage</i> 且不得大于 <i>maxPage</i>；
     *          例如：
     *          <pre>
     *          (new int[][] { new int[] { 1, 3 }, new int[] { 5, 5 },
     *                         new int[] { 15, 19 } }),
     *          </pre>
     *          指定页面 1、2、3、5、15、16、17、18 和 19。注意
     *          (<code>new int[][] { new int[] { 1, 1 }, new int[] { 1, 2 } }</code>)，
     *          是无效的页面范围集，因为两个范围重叠
     * @param   printer 可能为 <code>null</code> 的打印机名称
     * @param   sides <code>SidesType.ONE_SIDED</code>、
     *          <code>SidesType.TWO_SIDED_LONG_EDGE</code> 或
     *          <code>SidesType.TWO_SIDED_SHORT_EDGE</code>
     * @throws  IllegalArgumentException 如果上述条件中的一个或多个被违反
     */
    public JobAttributes(int copies, DefaultSelectionType defaultSelection,
                         DestinationType destination, DialogType dialog,
                         String fileName, int maxPage, int minPage,
                         MultipleDocumentHandlingType multipleDocumentHandling,
                         int[][] pageRanges, String printer, SidesType sides) {
        setCopies(copies);
        setDefaultSelection(defaultSelection);
        setDestination(destination);
        setDialog(dialog);
        setFileName(fileName);
        setMaxPage(maxPage);
        setMinPage(minPage);
        setMultipleDocumentHandling(multipleDocumentHandling);
        setPageRanges(pageRanges);
        setPrinter(printer);
        setSides(sides);
    }

    /**
     * 创建并返回此 <code>JobAttributes</code> 的副本。
     *
     * @return  新创建的副本；可以安全地将此对象转换为 <code>JobAttributes</code>
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // 由于我们实现了 Cloneable，这不应该发生
            throw new InternalError(e);
        }
    }

    /**
     * 将此 <code>JobAttributes</code> 的所有属性设置为与 obj 的属性相同的值。
     *
     * @param   obj 要复制的 <code>JobAttributes</code>
     */
    public void set(JobAttributes obj) {
        copies = obj.copies;
        defaultSelection = obj.defaultSelection;
        destination = obj.destination;
        dialog = obj.dialog;
        fileName = obj.fileName;
        fromPage = obj.fromPage;
        maxPage = obj.maxPage;
        minPage = obj.minPage;
        multipleDocumentHandling = obj.multipleDocumentHandling;
        // 没问题，因为我们从不修改 pageRanges 的内容
        pageRanges = obj.pageRanges;
        prFirst = obj.prFirst;
        prLast = obj.prLast;
        printer = obj.printer;
        sides = obj.sides;
        toPage = obj.toPage;
    }


                /**
     * 返回应用程序应为使用这些属性的作业渲染的副本数量。此属性将更新为用户选择的值。
     *
     * @return  大于 0 的整数。
     */
    public int getCopies() {
        return copies;
    }

    /**
     * 指定应用程序应为使用这些属性的作业渲染的副本数量。不指定此属性等同于指定 <code>1</code>。
     *
     * @param   copies 大于 0 的整数
     * @throws  IllegalArgumentException 如果 <code>copies</code> 小于或等于 0
     */
    public void setCopies(int copies) {
        if (copies <= 0) {
            throw new IllegalArgumentException("Invalid value for attribute "+
                                               "copies");
        }
        this.copies = copies;
    }

    /**
     * 将应用程序应为使用这些属性的作业渲染的副本数量设置为默认值。默认副本数量为 1。
     */
    public void setCopiesToDefault() {
        setCopies(1);
    }

    /**
     * 指定应用程序应为使用这些属性的作业打印所有页面、<code>getPageRanges</code> 返回的范围指定的页面，还是当前选择的页面。此属性将更新为用户选择的值。
     *
     * @return  DefaultSelectionType.ALL, DefaultSelectionType.RANGE, 或 DefaultSelectionType.SELECTION
     */
    public DefaultSelectionType getDefaultSelection() {
        return defaultSelection;
    }

    /**
     * 指定应用程序应为使用这些属性的作业打印所有页面、<code>getPageRanges</code> 返回的范围指定的页面，还是当前选择的页面。不指定此属性等同于指定 DefaultSelectionType.ALL。
     *
     * @param   defaultSelection DefaultSelectionType.ALL, DefaultSelectionType.RANGE, 或 DefaultSelectionType.SELECTION。
     * @throws  IllegalArgumentException 如果 defaultSelection 为 <code>null</code>
     */
    public void setDefaultSelection(DefaultSelectionType defaultSelection) {
        if (defaultSelection == null) {
            throw new IllegalArgumentException("Invalid value for attribute "+
                                               "defaultSelection");
        }
        this.defaultSelection = defaultSelection;
    }

    /**
     * 指定输出是否为打印机或文件，用于使用这些属性的作业。此属性将更新为用户选择的值。
     *
     * @return  DesintationType.FILE 或 DesintationType.PRINTER
     */
    public DestinationType getDestination() {
        return destination;
    }

    /**
     * 指定输出是否为打印机或文件，用于使用这些属性的作业。不指定此属性等同于指定 DesintationType.PRINTER。
     *
     * @param   destination DesintationType.FILE 或 DesintationType.PRINTER。
     * @throws  IllegalArgumentException 如果 destination 为 null。
     */
    public void setDestination(DestinationType destination) {
        if (destination == null) {
            throw new IllegalArgumentException("Invalid value for attribute "+
                                               "destination");
        }
        this.destination = destination;
    }

    /**
     * 返回是否为使用这些属性的作业显示打印对话框以修改打印设置，以及应显示哪种类型的打印对话框。DialogType.COMMON 表示跨平台的纯 Java 打印对话框。DialogType.NATIVE 表示平台的本地打印对话框。如果平台不支持本地打印对话框，则显示纯 Java 打印对话框。DialogType.NONE 表示不显示打印对话框（即后台打印）。此属性不能被实现或目标打印机修改或受其限制。
     *
     * @return  <code>DialogType.COMMON</code>, <code>DialogType.NATIVE</code>, 或 <code>DialogType.NONE</code>
     */
    public DialogType getDialog() {
        return dialog;
    }

    /**
     * 指定是否为使用这些属性的作业显示打印对话框以修改打印设置，以及应显示哪种类型的打印对话框。DialogType.COMMON 表示跨平台的纯 Java 打印对话框。DialogType.NATIVE 表示平台的本地打印对话框。如果平台不支持本地打印对话框，则显示纯 Java 打印对话框。DialogType.NONE 表示不显示打印对话框（即后台打印）。不指定此属性等同于指定 DialogType.NATIVE。
     *
     * @param   dialog DialogType.COMMON, DialogType.NATIVE, 或 DialogType.NONE。
     * @throws  IllegalArgumentException 如果 dialog 为 null。
     */
    public void setDialog(DialogType dialog) {
        if (dialog == null) {
            throw new IllegalArgumentException("Invalid value for attribute "+
                                               "dialog");
        }
        this.dialog = dialog;
    }

    /**
     * 指定用于使用这些属性的作业的输出文件的文件名。此属性将更新为用户选择的值。
     *
     * @return  可能为 <code>null</code> 的文件名
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 指定用于使用这些属性的作业的输出文件的文件名。默认值取决于平台和实现。
     *
     * @param   fileName 可能为 null 的文件名。
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * 返回为使用这些属性的作业要打印的第一页（如果要打印页面范围）。此属性将更新为用户选择的值。应用程序在输出时应忽略此属性，除非 <code>getDefaultSelection</code> 方法的返回值为 DefaultSelectionType.RANGE。如果可能，应用程序应优先考虑 <code>getPageRanges</code> 方法的返回值。
     *
     * @return  大于零且小于或等于 <i>toPage</i>，且大于或等于 <i>minPage</i> 且小于或等于 <i>maxPage</i> 的整数。
     */
    public int getFromPage() {
        if (fromPage != 0) {
            return fromPage;
        } else if (toPage != 0) {
            return getMinPage();
        } else if (pageRanges != null) {
            return prFirst;
        } else {
            return getMinPage();
        }
    }

    /**
     * 指定为使用这些属性的作业要打印的第一页（如果要打印页面范围）。如果不指定此属性，则使用 pageRanges 属性的值。如果指定了 pageRanges 和 fromPage 或 toPage 中的一个或两个，则 pageRanges 优先。不指定 pageRanges、fromPage 或 toPage 等同于调用
     * setPageRanges(new int[][] { new int[] { <i>minPage</i> } });
     *
     * @param   fromPage 大于零且小于或等于 <i>toPage</i>，且大于或等于 <i>minPage</i> 且小于或等于 <i>maxPage</i> 的整数。
     * @throws  IllegalArgumentException 如果上述条件之一被违反。
     */
    public void setFromPage(int fromPage) {
        if (fromPage <= 0 ||
            (toPage != 0 && fromPage > toPage) ||
            fromPage < minPage ||
            fromPage > maxPage) {
            throw new IllegalArgumentException("Invalid value for attribute "+
                                               "fromPage");
        }
        this.fromPage = fromPage;
    }

    /**
     * 指定用户可以为使用这些属性的作业指定的最后一页的最大值。此属性不能被实现或目标打印机修改或受其限制。
     *
     * @return  大于零且大于或等于 <i>minPage</i> 的整数。
     */
    public int getMaxPage() {
        return maxPage;
    }

    /**
     * 指定用户可以为使用这些属性的作业指定的最后一页的最大值。不指定此属性等同于指定 <code>Integer.MAX_VALUE</code>。
     *
     * @param   maxPage 大于零且大于或等于 <i>minPage</i> 的整数
     * @throws  IllegalArgumentException 如果上述条件之一被违反
     */
    public void setMaxPage(int maxPage) {
        if (maxPage <= 0 || maxPage < minPage) {
            throw new IllegalArgumentException("Invalid value for attribute "+
                                               "maxPage");
        }
        this.maxPage = maxPage;
    }

    /**
     * 指定用户可以为使用这些属性的作业指定的第一页的最小值。此属性不能被实现或目标打印机修改或受其限制。
     *
     * @return  大于零且小于或等于 <i>maxPage</i> 的整数。
     */
    public int getMinPage() {
        return minPage;
    }

    /**
     * 指定用户可以为使用这些属性的作业指定的第一页的最小值。不指定此属性等同于指定 <code>1</code>。
     *
     * @param   minPage 大于零且小于或等于 <i>maxPage</i> 的整数。
     * @throws  IllegalArgumentException 如果上述条件之一被违反。
     */
    public void setMinPage(int minPage) {
        if (minPage <= 0 || minPage > maxPage) {
            throw new IllegalArgumentException("Invalid value for attribute "+
                                               "minPage");
        }
        this.minPage = minPage;
    }

    /**
     * 指定为使用这些属性的作业处理多个副本的方式，包括装订。此属性将更新为用户选择的值。
     *
     * @return
     *     MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_COLLATED_COPIES 或
     *     MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES。
     */
    public MultipleDocumentHandlingType getMultipleDocumentHandling() {
        return multipleDocumentHandling;
    }

    /**
     * 指定为使用这些属性的作业处理多个副本的方式，包括装订。不指定此属性等同于指定
     * MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES。
     *
     * @param   multipleDocumentHandling
     *     MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_COLLATED_COPIES 或
     *     MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES。
     * @throws  IllegalArgumentException 如果 multipleDocumentHandling 为 null。
     */
    public void setMultipleDocumentHandling(MultipleDocumentHandlingType
                                            multipleDocumentHandling) {
        if (multipleDocumentHandling == null) {
            throw new IllegalArgumentException("Invalid value for attribute "+
                                               "multipleDocumentHandling");
        }
        this.multipleDocumentHandling = multipleDocumentHandling;
    }

    /**
     * 将为使用这些属性的作业处理多个副本的方式（包括装订）设置为默认值。默认处理方式为
     * MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES。
     */
    public void setMultipleDocumentHandlingToDefault() {
        setMultipleDocumentHandling(
            MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES);
    }

    /**
     * 指定为使用这些属性的作业要打印的页面范围（如果要打印页面范围）。所有范围数字都是包含的。此属性将更新为用户选择的值。应用程序在输出时应忽略此属性，除非 <code>getDefaultSelection</code> 方法的返回值为
     * DefaultSelectionType.RANGE。
     *
     * @return  一个包含 2 个元素的整数数组的数组。一个数组被解释为包括和介于指定页面之间的所有页面的范围。范围必须按升序排列且不得重叠。指定的页码不能小于 <i>minPage</i> 且不能大于 <i>maxPage</i>。
     *          例如：
     *          (new int[][] { new int[] { 1, 3 }, new int[] { 5, 5 },
     *                         new int[] { 15, 19 } }),
     *          指定第 1、2、3、5、15、16、17、18 和 19 页。
     */
    public int[][] getPageRanges() {
        if (pageRanges != null) {
            // 返回副本，因为否则客户端代码可以通过修改返回的数组来规避 setPageRanges 中的检查。
            int[][] copy = new int[pageRanges.length][2];
            for (int i = 0; i < pageRanges.length; i++) {
                copy[i][0] = pageRanges[i][0];
                copy[i][1] = pageRanges[i][1];
            }
            return copy;
        } else if (fromPage != 0 || toPage != 0) {
            int fromPage = getFromPage();
            int toPage = getToPage();
            return new int[][] { new int[] { fromPage, toPage } };
        } else {
            int minPage = getMinPage();
            return new int[][] { new int[] { minPage, minPage } };
        }
    }

    /**
     * 指定为使用这些属性的作业要打印的页面范围（如果要打印页面范围）。所有范围数字都是包含的。如果不指定此属性，则使用 fromPage 和 toPages 属性的值。如果指定了 pageRanges 和 fromPage 或 toPage 中的一个或两个，则 pageRanges 优先。不指定 pageRanges、fromPage 或 toPage 等同于调用
     * setPageRanges(new int[][] { new int[] { <i>minPage</i>,
     *                                                 <i>minPage</i> } });
     *
     * @param   pageRanges 一个包含 2 个元素的整数数组的数组。一个数组被解释为包括和介于指定页面之间的所有页面的范围。范围必须按升序排列且不得重叠。指定的页码不能小于 <i>minPage</i> 且不能大于 <i>maxPage</i>。
     *          例如：
     *          (new int[][] { new int[] { 1, 3 }, new int[] { 5, 5 },
     *                         new int[] { 15, 19 } }),
     *          指定第 1、2、3、5、15、16、17、18 和 19 页。注意：
     *          (new int[][] { new int[] { 1, 1 }, new int[] { 1, 2 } }),
     *          是一个无效的页面范围集，因为两个范围重叠。
     * @throws  IllegalArgumentException 如果上述条件之一被违反。
     */
    public void setPageRanges(int[][] pageRanges) {
        String xcp = "Invalid value for attribute pageRanges";
        int first = 0;
        int last = 0;


                    if (pageRanges == null) {
            throw new IllegalArgumentException(xcp);
        }

        for (int i = 0; i < pageRanges.length; i++) {
            if (pageRanges[i] == null ||
                pageRanges[i].length != 2 ||
                pageRanges[i][0] <= last ||
                pageRanges[i][1] < pageRanges[i][0]) {
                    throw new IllegalArgumentException(xcp);
            }
            last = pageRanges[i][1];
            if (first == 0) {
                first = pageRanges[i][0];
            }
        }

        if (first < minPage || last > maxPage) {
            throw new IllegalArgumentException(xcp);
        }

        // 存储一个副本，因为否则客户端代码可以通过持有数组的引用并在调用 setPageRanges 后修改它来绕过上述检查。
        int[][] copy = new int[pageRanges.length][2];
        for (int i = 0; i < pageRanges.length; i++) {
            copy[i][0] = pageRanges[i][0];
            copy[i][1] = pageRanges[i][1];
        }
        this.pageRanges = copy;
        this.prFirst = first;
        this.prLast = last;
    }

    /**
     * 返回使用这些属性的作业的目标打印机。此属性被更新为用户选择的值。
     *
     * @return  可能为 null 的打印机名称。
     */
    public String getPrinter() {
        return printer;
    }

    /**
     * 指定使用这些属性的作业的目标打印机。默认值取决于平台和实现。
     *
     * @param   printer 可能为 null 的打印机名称。
     */
    public void setPrinter(String printer) {
        this.printer = printer;
    }

    /**
     * 返回如何将连续的页面施加到打印介质的两侧。SidesType.ONE_SIDED 将每个连续的页面施加到连续的介质表的同一侧。这种施加有时称为<i>单面</i>。
     * SidesType.TWO_SIDED_LONG_EDGE 将每个连续的页面对施加到连续的介质表的前后两侧，使得介质上的每个页面对的方向对于读者来说是正确的，就像在长边装订一样。这种施加有时称为<i>双面</i>。
     * SidesType.TWO_SIDED_SHORT_EDGE 将每个连续的页面对施加到连续的介质表的前后两侧，使得介质上的每个页面对的方向对于读者来说是正确的，就像在短边装订一样。这种施加有时称为<i>翻转</i>。此属性被更新为用户选择的值。
     *
     * @return  SidesType.ONE_SIDED, SidesType.TWO_SIDED_LONG_EDGE, 或 SidesType.TWO_SIDED_SHORT_EDGE。
     */
    public SidesType getSides() {
        return sides;
    }

    /**
     * 指定如何将连续的页面施加到打印介质的两侧。SidesType.ONE_SIDED 将每个连续的页面施加到连续的介质表的同一侧。这种施加有时称为<i>单面</i>。
     * SidesType.TWO_SIDED_LONG_EDGE 将每个连续的页面对施加到连续的介质表的前后两侧，使得介质上的每个页面对的方向对于读者来说是正确的，就像在长边装订一样。这种施加有时称为<i>双面</i>。
     * SidesType.TWO_SIDED_SHORT_EDGE 将每个连续的页面对施加到连续的介质表的前后两侧，使得介质上的每个页面对的方向对于读者来说是正确的，就像在短边装订一样。这种施加有时称为<i>翻转</i>。不指定此属性等同于指定 SidesType.ONE_SIDED。
     *
     * @param   sides SidesType.ONE_SIDED, SidesType.TWO_SIDED_LONG_EDGE, 或 SidesType.TWO_SIDED_SHORT_EDGE。
     * @throws  IllegalArgumentException 如果 sides 为 null。
     */
    public void setSides(SidesType sides) {
        if (sides == null) {
            throw new IllegalArgumentException("属性 sides 的无效值");
        }
        this.sides = sides;
    }

    /**
     * 将如何将连续的页面施加到打印介质的两侧设置为默认值。默认施加为 SidesType.ONE_SIDED。
     */
    public void setSidesToDefault() {
        setSides(SidesType.ONE_SIDED);
    }

    /**
     * 返回使用这些属性的作业要打印的最后一页（包含），如果要打印一个页面范围。此属性被更新为用户选择的值。应用程序在输出时应忽略此属性，除非 <code>
     * getDefaultSelection</code> 方法的返回值为 DefaultSelectionType.RANGE。如果可能，应用程序应优先考虑 <code>getPageRanges</code> 方法的返回值而不是此方法的返回值。
     *
     * @return  大于零且大于或等于 <i>toPage</i> 且大于或等于 <i>minPage</i> 且小于或等于 <i>maxPage</i> 的整数。
     */
    public int getToPage() {
        if (toPage != 0) {
            return toPage;
        } else if (fromPage != 0) {
            return fromPage;
        } else if (pageRanges != null) {
            return prLast;
        } else {
            return getMinPage();
        }
    }

    /**
     * 指定使用这些属性的作业要打印的最后一页（包含），如果要打印一个页面范围。
     * 如果未指定此属性，则使用 pageRanges 属性的值。如果指定了 pageRanges 和 fromPage 或 toPage 中的一个或两个，则 pageRanges 优先。不指定 pageRanges、fromPage 或 toPage 等同于调用
     * setPageRanges(new int[][] { new int[] { <i>minPage</i> } });
     *
     * @param   toPage 大于零且大于或等于 <i>fromPage</i> 且大于或等于 <i>minPage</i> 且小于或等于 <i>maxPage</i> 的整数。
     * @throws  IllegalArgumentException 如果违反了上述条件之一。
     */
    public void setToPage(int toPage) {
        if (toPage <= 0 ||
            (fromPage != 0 && toPage < fromPage) ||
            toPage < minPage ||
            toPage > maxPage) {
            throw new IllegalArgumentException("属性 toPage 的无效值");
        }
        this.toPage = toPage;
    }

    /**
     * 确定两个 JobAttributes 是否相等。
     * <p>
     * 两个 JobAttributes 相等当且仅当它们的每个属性都相等。枚举类型的属性相等当且仅当字段引用同一个唯一的枚举对象。页面范围集相等当且仅当集的长度相等，每个范围枚举相同的页面，并且范围的顺序相同。
     *
     * @param   obj 要检查其相等性的对象。
     * @return  obj 是否根据上述标准等于此 JobAttribute。
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof JobAttributes)) {
            return false;
        }
        JobAttributes rhs = (JobAttributes)obj;

        if (fileName == null) {
            if (rhs.fileName != null) {
                return false;
            }
        } else {
            if (!fileName.equals(rhs.fileName)) {
                return false;
            }
        }

        if (pageRanges == null) {
            if (rhs.pageRanges != null) {
                return false;
            }
        } else {
            if (rhs.pageRanges == null ||
                    pageRanges.length != rhs.pageRanges.length) {
                return false;
            }
            for (int i = 0; i < pageRanges.length; i++) {
                if (pageRanges[i][0] != rhs.pageRanges[i][0] ||
                    pageRanges[i][1] != rhs.pageRanges[i][1]) {
                    return false;
                }
            }
        }

        if (printer == null) {
            if (rhs.printer != null) {
                return false;
            }
        } else {
            if (!printer.equals(rhs.printer)) {
                return false;
            }
        }

        return (copies == rhs.copies &&
                defaultSelection == rhs.defaultSelection &&
                destination == rhs.destination &&
                dialog == rhs.dialog &&
                fromPage == rhs.fromPage &&
                maxPage == rhs.maxPage &&
                minPage == rhs.minPage &&
                multipleDocumentHandling == rhs.multipleDocumentHandling &&
                prFirst == rhs.prFirst &&
                prLast == rhs.prLast &&
                sides == rhs.sides &&
                toPage == rhs.toPage);
    }

    /**
     * 返回此 JobAttributes 的哈希码值。
     *
     * @return  哈希码。
     */
    public int hashCode() {
        int rest = ((copies + fromPage + maxPage + minPage + prFirst + prLast +
                     toPage) * 31) << 21;
        if (pageRanges != null) {
            int sum = 0;
            for (int i = 0; i < pageRanges.length; i++) {
                sum += pageRanges[i][0] + pageRanges[i][1];
            }
            rest ^= (sum * 31) << 11;
        }
        if (fileName != null) {
            rest ^= fileName.hashCode();
        }
        if (printer != null) {
            rest ^= printer.hashCode();
        }
        return (defaultSelection.hashCode() << 6 ^
                destination.hashCode() << 5 ^
                dialog.hashCode() << 3 ^
                multipleDocumentHandling.hashCode() << 2 ^
                sides.hashCode() ^
                rest);
    }

    /**
     * 返回此 JobAttributes 的字符串表示形式。
     *
     * @return  字符串表示形式。
     */
    public String toString() {
        int[][] pageRanges = getPageRanges();
        String prStr = "[";
        boolean first = true;
        for (int i = 0; i < pageRanges.length; i++) {
            if (first) {
                first = false;
            } else {
                prStr += ",";
            }
            prStr += pageRanges[i][0] + ":" + pageRanges[i][1];
        }
        prStr += "]";

        return "copies=" + getCopies() + ",defaultSelection=" +
            getDefaultSelection() + ",destination=" + getDestination() +
            ",dialog=" + getDialog() + ",fileName=" + getFileName() +
            ",fromPage=" + getFromPage() + ",maxPage=" + getMaxPage() +
            ",minPage=" + getMinPage() + ",multiple-document-handling=" +
            getMultipleDocumentHandling() + ",page-ranges=" + prStr +
            ",printer=" + getPrinter() + ",sides=" + getSides() + ",toPage=" +
            getToPage();
    }
}
