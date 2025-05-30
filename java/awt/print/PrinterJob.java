
/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.print;

import java.awt.AWTError;
import java.awt.HeadlessException;
import java.util.Enumeration;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.StreamPrintServiceFactory;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;

import sun.security.action.GetPropertyAction;

/**
 * <code>PrinterJob</code> 类是控制打印的主要类。应用程序调用该类中的方法来设置作业，可选地调用打印对话框与用户交互，然后打印作业的页面。
 */
public abstract class PrinterJob {

 /* Public Class Methods */

    /**
     * 创建并返回一个最初与默认打印机关联的 <code>PrinterJob</code>。
     * 如果系统上没有可用的打印机，此方法仍会返回一个 <code>PrinterJob</code>，但 <code>getPrintService()</code>
     * 将返回 <code>null</code>，并且使用此 <code>PrinterJob</code> 调用 {@link #print() print} 可能会引发异常。
     * 在创建 <code>PrinterJob</code> 之前需要确定是否有合适的打印机的应用程序应确保
     * 从 {@link #lookupPrintServices() lookupPrintServices} 返回的数组不为空。
     * @return 一个新的 <code>PrinterJob</code>。
     *
     * @throws  SecurityException 如果存在安全经理，且其
     *          {@link java.lang.SecurityManager#checkPrintJobAccess}
     *          方法不允许此线程创建打印作业请求
     */
    public static PrinterJob getPrinterJob() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPrintJobAccess();
        }
        return (PrinterJob) java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
            public Object run() {
                String nm = System.getProperty("java.awt.printerjob", null);
                try {
                    return (PrinterJob)Class.forName(nm).newInstance();
                } catch (ClassNotFoundException e) {
                    throw new AWTError("PrinterJob not found: " + nm);
                } catch (InstantiationException e) {
                 throw new AWTError("Could not instantiate PrinterJob: " + nm);
                } catch (IllegalAccessException e) {
                    throw new AWTError("Could not access PrinterJob: " + nm);
                }
            }
        });
    }

    /**
     * 一个方便的方法，用于查找 2D 打印服务。
     * 从此方法返回的服务可以安装在支持打印服务的 <code>PrinterJob</code> 上。
     * 调用此方法等同于调用
     * {@link javax.print.PrintServiceLookup#lookupPrintServices(
     * DocFlavor, AttributeSet)
     * PrintServiceLookup.lookupPrintServices()}
     * 并指定一个 Pageable DocFlavor。
     * @return 一个可能为空的 2D 打印服务数组。
     * @since     1.4
     */
    public static PrintService[] lookupPrintServices() {
        return PrintServiceLookup.
            lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);
    }


    /**
     * 一个方便的方法，用于查找可以生成 2D 图形的流打印服务的工厂。
     * 示例用法：
     * <pre>{@code
     * FileOutputStream outstream;
     * StreamPrintService psPrinter;
     * String psMimeType = "application/postscript";
     * PrinterJob pj = PrinterJob.getPrinterJob();
     *
     * StreamPrintServiceFactory[] factories =
     *     PrinterJob.lookupStreamPrintServices(psMimeType);
     * if (factories.length > 0) {
     *     try {
     *         outstream = new File("out.ps");
     *         psPrinter =  factories[0].getPrintService(outstream);
     *         // psPrinter 现在可以设置为 PrinterJob 的服务
     *         pj.setPrintService(psPrinter)
     *     } catch (Exception e) {
     *         e.printStackTrace();
     *     }
     * }
     * }</pre>
     * 从此方法返回的服务可以安装在支持打印服务的 <code>PrinterJob</code> 实例上。
     * 调用此方法等同于调用
     * {@link javax.print.StreamPrintServiceFactory#lookupStreamPrintServiceFactories(DocFlavor, String)
     * StreamPrintServiceFactory.lookupStreamPrintServiceFactories()
     * } 并指定一个 Pageable DocFlavor。
     *
     * @param mimeType 所需的输出格式，或 null 表示任何格式。
     * @return 一个可能为空的 2D 流打印服务工厂数组。
     * @since     1.4
     */
    public static StreamPrintServiceFactory[]
        lookupStreamPrintServices(String mimeType) {
        return StreamPrintServiceFactory.lookupStreamPrintServiceFactories(
                                       DocFlavor.SERVICE_FORMATTED.PAGEABLE,
                                       mimeType);
    }


 /* Public Methods */

    /**
     * 应使用静态 {@link #getPrinterJob() getPrinterJob} 方法创建 <code>PrinterJob</code> 对象。
     */
    public PrinterJob() {
    }

    /**
     * 返回此打印作业的服务（打印机）。
     * 不支持打印服务的此类实现可能返回 null。如果没有可用的打印机，也会返回 null。
     * @return 此打印作业的服务。
     * @see #setPrintService(PrintService)
     * @see #getPrinterJob()
     * @since     1.4
     */
    public PrintService getPrintService() {
        return null;
    }

    /**
     * 将此 PrinterJob 与新的 PrintService 关联。
     * 支持指定 Print Service 的子类会重写此方法。
     *
     * 如果指定的服务不能支持必要的 <code>Pageable</code> 和
     * <code>Printable</code> 接口以支持 2D 打印，则抛出 <code>PrinterException</code>。
     * @param service 支持 2D 打印的打印服务
     * @exception PrinterException 如果指定的服务不支持 2D 打印，或者此 PrinterJob 类不支持
     * 设置 2D 打印服务，或者指定的服务以其他方式不是有效的打印服务。
     * @see #getPrintService
     * @since     1.4
     */
    public void setPrintService(PrintService service)
        throws PrinterException {
            throw new PrinterException(
                         "Setting a service is not supported on this class");
    }

    /**
     * 调用 <code>painter</code> 渲染页面。此 <code>PrinterJob</code> 要打印的文档中的页面
     * 由 {@link Printable} 对象 <code>painter</code> 渲染。每个页面的 {@link PageFormat} 是默认页面格式。
     * @param painter 渲染文档每一页的 <code>Printable</code>。
     */
    public abstract void setPrintable(Printable painter);

    /**
     * 调用 <code>painter</code> 以指定的 <code>format</code> 渲染页面。此 <code>PrinterJob</code> 要打印的文档中的页面
     * 由 <code>Printable</code> 对象 <code>painter</code> 渲染。每个页面的 <code>PageFormat</code> 是 <code>format</code>。
     * @param painter 被调用来渲染文档每一页的 <code>Printable</code>
     * @param format 每一页的大小和方向
     */
    public abstract void setPrintable(Printable painter, PageFormat format);

    /**
     * 查询 <code>document</code> 以获取其中包含的每一页的页数、<code>PageFormat</code> 和 <code>Printable</code>。
     * @param document 要打印的页面。不能为 <code>null</code>。
     * @exception NullPointerException 传入的 <code>Pageable</code> 为 <code>null</code>。
     * @see PageFormat
     * @see Printable
     */
    public abstract void setPageable(Pageable document)
        throws NullPointerException;

    /**
     * 显示一个对话框，供用户更改打印作业的属性。
     * 如果选择了本机打印服务，此方法将显示本机对话框，用户选择的打印机将仅限于这些本机打印服务。
     * 要显示适用于所有服务（包括本机服务）的跨平台打印对话框，请使用
     * <code>printDialog(PrintRequestAttributeSet)</code>。
     * <p>
     * 可以使用 PrintService 的 PrinterJob 实现将更新此 PrinterJob 的 PrintService 以反映用户选择的新服务。
     * @return 如果用户未取消对话框，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true。
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public abstract boolean printDialog() throws HeadlessException;

    /**
     * 一个方便的方法，显示一个跨平台的打印对话框，用于所有能够使用 <code>Pageable</code> 接口打印 2D 图形的服务。
     * 初始显示对话框时，选定的打印机将反映当前附加到此打印作业的打印服务。
     * 如果用户更改了打印服务，除非用户取消了对话框，否则 PrinterJob 将更新以反映这一点。
     * 除了选定的打印机外，PrinterJob 状态不会更新以反映用户的更改。
     * 为了使选择影响打印作业，必须在调用
     * <code>print(PrintRequestAttributeSet)</code> 方法时指定属性。如果使用 Pageable 接口，客户端必须创建一个基于用户选择的 PageFormat。
     * 如果用户取消了对话框，属性将不会反映用户所做的任何更改。
     * @param attributes 输入时是应用程序提供的属性，输出时内容更新以反映用户的选择。此参数不能为空。
     * @return 如果用户未取消对话框，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true。
     * @exception NullPointerException 如果 <code>attributes</code> 参数为空。
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @since     1.4
     *
     */
    public boolean printDialog(PrintRequestAttributeSet attributes)
        throws HeadlessException {

        if (attributes == null) {
            throw new NullPointerException("attributes");
        }
        return printDialog();
    }

    /**
     * 显示一个对话框，允许修改 <code>PageFormat</code> 实例。
     * <code>page</code> 参数用于初始化页面设置对话框中的控件。
     * 如果用户取消了对话框，则此方法返回未修改的原始 <code>page</code> 对象。
     * 如果用户确认了对话框，则此方法返回一个包含用户指示更改的新 <code>PageFormat</code> 对象。
     * 无论哪种情况，原始 <code>page</code> 对象都不会被修改。
     * @param page 提供给用户修改的默认 <code>PageFormat</code>
     * @return 如果对话框被取消，则返回原始 <code>page</code> 对象；如果对话框被确认，则返回一个包含用户指示格式的新 <code>PageFormat</code> 对象。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true。
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @since     1.2
     */
    public abstract PageFormat pageDialog(PageFormat page)
        throws HeadlessException;

    /**
     * 一个方便的方法，显示一个跨平台的页面设置对话框。
     * 可用的选择将反映当前设置在此 PrinterJob 上的打印服务。
     * <p>
     * 输入时，属性参数将反映客户端在用户对话框中的初始选择。未指定的属性将使用服务的默认值显示。返回时，它将反映用户的选择。选择可能会由实现更新，以与当前选定的打印服务支持的值保持一致。
     * <p>
     * 返回值将是一个与 PrintRequestAttributeSet 中的选择等效的 PageFormat。
     * 如果用户取消了对话框，属性将不会反映用户所做的任何更改，返回值将为 null。
     * @param attributes 输入时是应用程序提供的属性，输出时内容更新以反映用户的选择。此参数不能为空。
     * @return 如果用户未取消对话框，则返回一个页面格式；否则返回 <code>null</code>。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true。
     * @exception NullPointerException 如果 <code>attributes</code> 参数为空。
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @since     1.4
     *
     */
    public PageFormat pageDialog(PrintRequestAttributeSet attributes)
        throws HeadlessException {


                    if (attributes == null) {
            throw new NullPointerException("attributes");
        }
        return pageDialog(defaultPage());
    }

    /**
     * 克隆 <code>PageFormat</code> 参数并修改克隆以描述默认的页面大小和方向。
     * @param page 要克隆和修改的 <code>PageFormat</code>
     * @return 克隆的 <code>page</code>，修改后描述默认的 <code>PageFormat</code>。
     */
    public abstract PageFormat defaultPage(PageFormat page);

    /**
     * 创建一个新的 <code>PageFormat</code> 实例并将其设置为默认大小和方向。
     * @return 设置为默认大小和方向的 <code>PageFormat</code>。
     */
    public PageFormat defaultPage() {
        return defaultPage(new PageFormat());
    }

    /**
     * 计算一个 <code>PageFormat</code>，其值与当前 <code>PrintService</code>（即 <code>getPrintService()</code> 返回的值）支持的值以及 <code>attributes</code> 中包含的媒体、可打印区域和方向一致。
     * <p>
     * 调用此方法不会更新作业。
     * 对于从 <code>printDialog(PrintRequestAttributeSet attributes)</code> 获得一组属性的客户端，此方法很有用，需要一个 PageFormat 来打印 Pageable 对象。
     * @param attributes 一组打印属性，例如从调用 printDialog 获得。如果 <code>attributes</code> 为 null，则返回默认的 PageFormat。
     * @return 一个 <code>PageFormat</code>，其设置符合当前服务和指定属性。
     * @since 1.6
     */
    public PageFormat getPageFormat(PrintRequestAttributeSet attributes) {

        PrintService service = getPrintService();
        PageFormat pf = defaultPage();

        if (service == null || attributes == null) {
            return pf;
        }

        Media media = (Media)attributes.get(Media.class);
        MediaPrintableArea mpa =
            (MediaPrintableArea)attributes.get(MediaPrintableArea.class);
        OrientationRequested orientReq =
           (OrientationRequested)attributes.get(OrientationRequested.class);

        if (media == null && mpa == null && orientReq == null) {
           return pf;
        }
        Paper paper = pf.getPaper();

        /* 如果有媒体但没有媒体可打印区域，我们可以尝试检索 mpa 的默认值并使用它。
         */
        if (mpa == null && media != null &&
            service.isAttributeCategorySupported(MediaPrintableArea.class)) {
            Object mpaVals =
                service.getSupportedAttributeValues(MediaPrintableArea.class,
                                                    null, attributes);
            if (mpaVals instanceof MediaPrintableArea[] &&
                ((MediaPrintableArea[])mpaVals).length > 0) {
                mpa = ((MediaPrintableArea[])mpaVals)[0];
            }
        }

        if (media != null &&
            service.isAttributeValueSupported(media, null, attributes)) {
            if (media instanceof MediaSizeName) {
                MediaSizeName msn = (MediaSizeName)media;
                MediaSize msz = MediaSize.getMediaSizeForName(msn);
                if (msz != null) {
                    double inch = 72.0;
                    double paperWid = msz.getX(MediaSize.INCH) * inch;
                    double paperHgt = msz.getY(MediaSize.INCH) * inch;
                    paper.setSize(paperWid, paperHgt);
                    if (mpa == null) {
                        paper.setImageableArea(inch, inch,
                                               paperWid-2*inch,
                                               paperHgt-2*inch);
                    }
                }
            }
        }

        if (mpa != null &&
            service.isAttributeValueSupported(mpa, null, attributes)) {
            float [] printableArea =
                mpa.getPrintableArea(MediaPrintableArea.INCH);
            for (int i=0; i < printableArea.length; i++) {
                printableArea[i] = printableArea[i]*72.0f;
            }
            paper.setImageableArea(printableArea[0], printableArea[1],
                                   printableArea[2], printableArea[3]);
        }

        if (orientReq != null &&
            service.isAttributeValueSupported(orientReq, null, attributes)) {
            int orient;
            if (orientReq.equals(OrientationRequested.REVERSE_LANDSCAPE)) {
                orient = PageFormat.REVERSE_LANDSCAPE;
            } else if (orientReq.equals(OrientationRequested.LANDSCAPE)) {
                orient = PageFormat.LANDSCAPE;
            } else {
                orient = PageFormat.PORTRAIT;
            }
            pf.setOrientation(orient);
        }

        pf.setPaper(paper);
        pf = validatePage(pf);
        return pf;
    }

    /**
     * 返回 <code>page</code> 的克隆，并调整其设置以与当前 <code>PrinterJob</code> 的打印机兼容。例如，返回的 <code>PageFormat</code> 可能会调整其可打印区域以适应当前打印机使用的纸张的物理区域。
     * @param page 要克隆并调整设置以与当前打印机兼容的 <code>PageFormat</code>
     * @return 从 <code>page</code> 克隆并调整设置以符合此 <code>PrinterJob</code> 的 <code>PageFormat</code>。
     */
    public abstract PageFormat validatePage(PageFormat page);

    /**
     * 打印一组页面。
     * @exception PrinterException 打印系统中的错误导致作业被中止。
     * @see Book
     * @see Pageable
     * @see Printable
     */
    public abstract void print() throws PrinterException;

   /**
     * 使用属性集中的设置打印一组页面。默认实现忽略属性集。
     * <p>
     * 注意，某些属性可能直接通过等效的方法调用设置在 PrinterJob 上，例如：副本数：<code>setCopies(int)</code>，作业名称：<code>setJobName(String)</code> 以及通过 <code>PageFormat</code> 对象指定的媒体大小和方向。
     * <p>
     * 如果在此属性集中指定了支持的属性值，则它将优先于此 print() 操作的 API 设置。
     * 对于 PageFormat 的行为如下：
     * 如果客户端使用 Printable 接口，则检查此方法的 <code>attributes</code> 参数以指定媒体（按大小）、方向和可打印区域的属性，并使用这些属性构造一个新的 PageFormat，然后传递给 Printable 对象的 print() 方法。
     * 有关确保通过 PrinterJob 最佳打印的 Printable 的行为要求，请参见 {@link Printable}。
     * 对于 Pageable 接口的客户端，PageFormat 始终由该接口按页面提供。
     * <p>
     * 这些行为允许应用程序直接将 <code>printDialog(PrintRequestAttributeSet attributes</code> 返回的用户设置传递给此 print() 方法。
     * <p>
     *
     * @param attributes 作业的一组属性
     * @exception PrinterException 打印系统中的错误导致作业被中止。
     * @see Book
     * @see Pageable
     * @see Printable
     * @since 1.4
     */
    public void print(PrintRequestAttributeSet attributes)
        throws PrinterException {
        print();
    }

    /**
     * 设置要打印的副本数。
     * @param copies 要打印的副本数
     * @see #getCopies
     */
    public abstract void setCopies(int copies);

    /**
     * 获取要打印的副本数。
     * @return 要打印的副本数。
     * @see #setCopies
     */
    public abstract int getCopies();

    /**
     * 获取打印用户的名称。
     * @return 打印用户的名称
     */
    public abstract String getUserName();

    /**
     * 设置要打印的文档的名称。
     * 文档名称不能为 <code>null</code>。
     * @param jobName 要打印的文档的名称
     * @see #getJobName
     */
    public abstract void setJobName(String jobName);

    /**
     * 获取要打印的文档的名称。
     * @return 要打印的文档的名称。
     * @see #setJobName
     */
    public abstract String getJobName();

    /**
     * 取消正在进行的打印作业。如果已调用 {@link #print() print} 但尚未返回，则此方法信号表明应在下一个机会取消作业。如果没有正在进行的打印作业，则此调用不执行任何操作。
     */
    public abstract void cancel();

    /**
     * 如果打印作业正在进行但将在下一个机会被取消，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @return 如果正在进行的作业将被取消，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    public abstract boolean isCancelled();

}
