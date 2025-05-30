
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

/* ****************************************************************
 ******************************************************************
 ******************************************************************
 *** COPYRIGHT (c) Eastman Kodak Company, 1997
 *** As  an unpublished  work pursuant to Title 17 of the United
 *** States Code.  All rights reserved.
 ******************************************************************
 ******************************************************************
 ******************************************************************/

package java.awt.image;
import java.awt.Rectangle;
import java.awt.Point;

/**
 * 该类扩展了 Raster 以提供像素写入功能。
 * 有关 Raster 如何存储像素的描述，请参阅 Raster 类的注释。
 *
 * <p> 该类的构造函数是受保护的。要实例化 WritableRaster，请使用 Raster 类中的 createWritableRaster 工厂方法。
 */
public class WritableRaster extends Raster {

    /**
     * 通过给定的 SampleModel 构造 WritableRaster。WritableRaster 的左上角是 origin，其大小与 SampleModel 相同。
     * 会自动创建一个足够大的 DataBuffer 来描述 WritableRaster。
     * @param sampleModel     指定布局的 SampleModel。
     * @param origin          指定原点的 Point。
     * @throws RasterFormatException 如果计算 <code>origin.x + sampleModel.getWidth()</code> 或
     *          <code>origin.y + sampleModel.getHeight()</code> 导致整数溢出
     */
    protected WritableRaster(SampleModel sampleModel,
                             Point origin) {
        this(sampleModel,
             sampleModel.createDataBuffer(),
             new Rectangle(origin.x,
                           origin.y,
                           sampleModel.getWidth(),
                           sampleModel.getHeight()),
             origin,
             null);
    }

    /**
     * 通过给定的 SampleModel 和 DataBuffer 构造 WritableRaster。WritableRaster 的左上角是 origin，其大小与 SampleModel 相同。
     * DataBuffer 未初始化且必须与 SampleModel 兼容。
     * @param sampleModel     指定布局的 SampleModel。
     * @param dataBuffer      包含图像数据的 DataBuffer。
     * @param origin          指定原点的 Point。
     * @throws RasterFormatException 如果计算 <code>origin.x + sampleModel.getWidth()</code> 或
     *          <code>origin.y + sampleModel.getHeight()</code> 导致整数溢出
     */
    protected WritableRaster(SampleModel sampleModel,
                             DataBuffer dataBuffer,
                             Point origin) {
        this(sampleModel,
             dataBuffer,
             new Rectangle(origin.x,
                           origin.y,
                           sampleModel.getWidth(),
                           sampleModel.getHeight()),
             origin,
             null);
    }

    /**
     * 通过给定的 SampleModel、DataBuffer 和父级构造 WritableRaster。aRegion 指定新 Raster 的边界矩形。
     * 当转换到基 Raster 的坐标系中时，aRegion 必须包含在基 Raster 中。
     * (基 Raster 是没有父级的 Raster 的祖先。) sampleModelTranslate 指定新 Raster 的 sampleModelTranslateX 和
     * sampleModelTranslateY 值。
     *
     * 注意，此构造函数通常应由其他构造函数或创建方法调用，不应直接使用。
     * @param sampleModel     指定布局的 SampleModel。
     * @param dataBuffer      包含图像数据的 DataBuffer。
     * @param aRegion         指定图像区域的 Rectangle。
     * @param sampleModelTranslate  指定从 SampleModel 到 Raster 坐标的转换的 Point。
     * @param parent          此 Raster 的父级（如果有）。
     * @throws RasterFormatException 如果 <code>aRegion</code> 的宽度或高度小于或等于零，或计算 <code>aRegion.x + aRegion.width</code> 或
     *         <code>aRegion.y + aRegion.height</code> 导致整数溢出
     */
    protected WritableRaster(SampleModel sampleModel,
                             DataBuffer dataBuffer,
                             Rectangle aRegion,
                             Point sampleModelTranslate,
                             WritableRaster parent){
        super(sampleModel,dataBuffer,aRegion,sampleModelTranslate,parent);
    }

    /** 返回此 WritableRaster 的父级 WritableRaster（如果有），否则返回 null。
     * @return 此 <code>WritableRaster</code> 的父级，或 <code>null</code>。
     */
    public WritableRaster getWritableParent() {
        return (WritableRaster)parent;
    }

    /**
     * 创建一个与当前 WritableRaster 大小、SampleModel 和 DataBuffer 相同但位置不同的 WritableRaster。
     * 新的 WritableRaster 将引用当前 WritableRaster，可以通过其 getParent() 和 getWritableParent() 方法访问。
     *
     * @param childMinX 新 Raster 的左上角的 X 坐标。
     * @param childMinY 新 Raster 的左上角的 Y 坐标。
     * @return 一个与当前 <code>WritableRaster</code> 相同但位置不同的 <code>WritableRaster</code>。
     * @throws RasterFormatException 如果计算 <code>childMinX + this.getWidth()</code> 或
     *         <code>childMinY + this.getHeight()</code> 导致整数溢出
     */
    public WritableRaster createWritableTranslatedChild(int childMinX,
                                                        int childMinY) {
        return createWritableChild(minX,minY,width,height,
                                   childMinX,childMinY,null);
    }

    /**
     * 返回一个与当前 WritableRaster 共享全部或部分 DataBuffer 的新 WritableRaster。
     * 新的 WritableRaster 将引用当前 WritableRaster，可以通过其 getParent() 和 getWritableParent() 方法访问。
     *
     * <p> parentX、parentY、width 和 height 参数在当前 WritableRaster 的坐标空间中形成一个矩形，表示要共享的像素区域。
     * 如果此矩形不在当前 WritableRaster 的边界内，将抛出错误。
     *
     * <p> 新的 WritableRaster 可以被翻译到与当前 WritableRaster 不同的平面坐标系中。
     * childMinX 和 childMinY 参数给出新 Raster 的左上角的新 (x, y) 坐标；新 Raster 中的坐标 (childMinX, childMinY)
     * 将映射到当前 Raster 中的坐标 (parentX, parentY)。
     *
     * <p> 新的 WritableRaster 可以定义为包含当前 WritableRaster 的一个子集的波段，可能重新排序，通过 bandList 参数实现。
     * 如果 bandList 为 null，则认为包含当前 WritableRaster 的所有波段并保持其当前顺序。
     *
     * <p> 要创建一个包含当前 WritableRaster 的子区域但共享其坐标系和波段的新 WritableRaster，
     * 应调用此方法，使 childMinX 等于 parentX，childMinY 等于 parentY，bandList 等于 null。
     *
     * @param parentX    在当前 WritableRaster 坐标中的左上角的 X 坐标。
     * @param parentY    在当前 WritableRaster 坐标中的左上角的 Y 坐标。
     * @param w          从 (parentX, parentY) 开始的区域的宽度。
     * @param h          从 (parentX, parentY) 开始的区域的高度。
     * @param childMinX  返回的 WritableRaster 的左上角的 X 坐标。
     * @param childMinY  返回的 WritableRaster 的左上角的 Y 坐标。
     * @param bandList   波段索引数组，或 null 以使用所有波段。
     * @return 一个共享当前 <code>WritableRaster</code> 的全部或部分 <code>DataBuffer</code> 的 <code>WritableRaster</code>。
     * @exception RasterFormatException 如果子区域在 Raster 边界之外。
     * @throws RasterFormatException 如果 <code>w</code> 或 <code>h</code>
     *         小于或等于零，或计算 <code>parentX + w</code>、<code>parentY + h</code>、
     *         <code>childMinX + w</code> 或 <code>childMinY + h</code> 导致整数溢出
     */
    public WritableRaster createWritableChild(int parentX, int parentY,
                                              int w, int h,
                                              int childMinX, int childMinY,
                                              int bandList[]) {
        if (parentX < this.minX) {
            throw new RasterFormatException("parentX 超出 Raster 范围");
        }
        if (parentY < this.minY) {
            throw new RasterFormatException("parentY 超出 Raster 范围");
        }
        if ((parentX+w < parentX) || (parentX+w > this.width + this.minX)) {
            throw new RasterFormatException("(parentX + width) 超出 Raster 范围");
        }
        if ((parentY+h < parentY) || (parentY+h > this.height + this.minY)) {
            throw new RasterFormatException("(parentY + height) 超出 Raster 范围");
        }

        SampleModel sm;
        // 注意：子 Raster 的 SampleModel 应具有与父 Raster 相同的宽度和高度，因为它们表示像素数据的物理布局。
        // 子 Raster 的宽度和高度表示像素数据的“虚拟”视图，因此可能与 SampleModel 的宽度和高度不同。
        if (bandList != null) {
            sm = sampleModel.createSubsetSampleModel(bandList);
        }
        else {
            sm = sampleModel;
        }

        int deltaX = childMinX - parentX;
        int deltaY = childMinY - parentY;

        return new WritableRaster(sm,
                                  getDataBuffer(),
                                  new Rectangle(childMinX,childMinY,
                                                w, h),
                                  new Point(sampleModelTranslateX+deltaX,
                                            sampleModelTranslateY+deltaY),
                                  this);
    }

    /**
     * 从类型为 TransferType 的基本数组中设置单个像素的数据。对于 Java 2D(tm) API 支持的图像数据，这将是 DataBuffer.TYPE_BYTE、
     * DataBuffer.TYPE_USHORT、DataBuffer.TYPE_INT、DataBuffer.TYPE_SHORT、DataBuffer.TYPE_FLOAT 或 DataBuffer.TYPE_DOUBLE 之一。
     * 数组中的数据可能是打包格式，从而提高数据传输的效率。
     * 如果坐标不在范围内，或 inData 不足以容纳像素数据，可能会抛出 ArrayIndexOutOfBoundsException。
     * 但是，显式边界检查不是必需的。
     * 如果输入对象不为 null 且引用的不是 TransferType 类型的数组，将抛出 ClassCastException。
     * @see java.awt.image.SampleModel#setDataElements(int, int, Object, DataBuffer)
     * @param x        像素位置的 X 坐标。
     * @param y        像素位置的 Y 坐标。
     * @param inData   一个对象引用，指向由 getTransferType() 定义的类型且长度为 getNumDataElements()
     *                 的数组，包含要放置在 x,y 的像素数据。
     *
     * @throws ArrayIndexOutOfBoundsException 如果坐标不在范围内，或 inData 太小而无法容纳输入。
     */
    public void setDataElements(int x, int y, Object inData) {
        sampleModel.setDataElements(x-sampleModelTranslateX,
                                    y-sampleModelTranslateY,
                                    inData, dataBuffer);
    }

    /**
     * 从输入 Raster 设置矩形像素的数据。输入 Raster 必须与当前 WritableRaster 兼容，即它们必须具有相同数量的波段，
     * 对应波段必须具有相同数量的每样本位数，TransferTypes 和 NumDataElements 必须相同，getDataElements/setDataElements
     * 使用的打包方式必须相同。
     * 如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。
     * 但是，显式边界检查不是必需的。
     * @param x        像素位置的 X 坐标。
     * @param y        像素位置的 Y 坐标。
     * @param inRaster 包含要放置在 x,y 的数据的 Raster。
     *
     * @throws NullPointerException 如果 inRaster 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标不在范围内。
     */
    public void setDataElements(int x, int y, Raster inRaster) {
        int dstOffX = x+inRaster.getMinX();
        int dstOffY = y+inRaster.getMinY();
        int width  = inRaster.getWidth();
        int height = inRaster.getHeight();
        if ((dstOffX < this.minX) || (dstOffY < this.minY) ||
            (dstOffX + width > this.minX + this.width) ||
            (dstOffY + height > this.minY + this.height)) {
            throw new ArrayIndexOutOfBoundsException
                ("坐标超出范围！");
        }


                    int srcOffX = inRaster.getMinX();
        int srcOffY = inRaster.getMinY();
        Object tdata = null;

        for (int startY=0; startY < height; startY++) {
            tdata = inRaster.getDataElements(srcOffX, srcOffY+startY,
                                             width, 1, tdata);
            setDataElements(dstOffX, dstOffY+startY,
                            width, 1, tdata);
        }
    }

    /**
     * 为一个矩形像素区域设置数据，数据类型为 TransferType。对于 Java 2D API 支持的图像数据，这将是 DataBuffer.TYPE_BYTE、
     * DataBuffer.TYPE_USHORT、DataBuffer.TYPE_INT、DataBuffer.TYPE_SHORT、DataBuffer.TYPE_FLOAT 或 DataBuffer.TYPE_DOUBLE 之一。
     * 数组中的数据可能是压缩格式，从而提高数据传输的效率。
     * 如果坐标不在范围内，或者 inData 不足以容纳像素数据，可能会抛出 ArrayIndexOutOfBoundsException。
     * 但是，显式边界检查不是必需的。
     * 如果输入对象不为 null 且引用的不是 TransferType 类型的数组，将抛出 ClassCastException。
     * @see java.awt.image.SampleModel#setDataElements(int, int, int, int, Object, DataBuffer)
     * @param x        上左角像素位置的 X 坐标。
     * @param y        上左角像素位置的 Y 坐标。
     * @param w        像素矩形的宽度。
     * @param h        像素矩形的高度。
     * @param inData   一个对象引用，指向由 getTransferType() 定义的数组，长度为 w*h*getNumDataElements()，
     *                 包含从 x,y 到 x+w-1, y+h-1 的像素数据。
     *
     * @throws NullPointerException 如果 inData 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标不在范围内，或者 inData 太小而无法容纳输入。
     */
    public void setDataElements(int x, int y, int w, int h, Object inData) {
        sampleModel.setDataElements(x-sampleModelTranslateX,
                                    y-sampleModelTranslateY,
                                    w,h,inData,dataBuffer);
    }

    /**
     * 从 Raster srcRaster 复制像素到此 WritableRaster。srcRaster 中的每个像素都会复制到此光栅中的相同 x,y 地址，除非该地址超出此光栅的范围。
     * srcRaster 必须具有与此 WritableRaster 相同的波段数。复制是一个简单的源样本到相应目标样本的复制。
     * <p>
     * 如果源和目标光栅的所有样本都是整数类型且小于或等于 32 位，则调用此方法等同于对两个光栅中所有有效的 <code>x,y</code> 地址执行以下代码。
     * <pre>{@code
     *       Raster srcRaster;
     *       WritableRaster dstRaster;
     *       for (int b = 0; b < srcRaster.getNumBands(); b++) {
     *           dstRaster.setSample(x, y, b, srcRaster.getSample(x, y, b));
     *       }
     * }</pre>
     * 因此，当将整数类型源复制到整数类型目标时，如果源样本大小大于目标样本大小，源样本的高位将被截断。如果源样本大小小于目标样本大小，目标的高位将被零扩展或符号扩展，具体取决于 srcRaster 的 SampleModel 是否将样本视为有符号或无符号量。
     * <p>
     * 当将浮点或双精度源复制到整数类型目标时，每个源样本将被转换为目标类型。当将整数类型源复制到浮点或双精度目标时，源首先转换为 32 位整数（如果必要），使用上述整数类型的规则，然后将整数转换为浮点或双精度。
     * <p>
     * @param srcRaster  要从中复制像素的 Raster。
     *
     * @throws NullPointerException 如果 srcRaster 为 null。
     */
    public void setRect(Raster srcRaster) {
        setRect(0,0,srcRaster);
    }

    /**
     * 从 Raster srcRaster 复制像素到此 WritableRaster。对于 srcRaster 中的每个 (x, y) 地址，相应的像素将复制到此 WritableRaster 中的 (x+dx, y+dy) 地址，
     * 除非 (x+dx, y+dy) 超出此光栅的范围。srcRaster 必须具有与此 WritableRaster 相同的波段数。复制是一个简单的源样本到相应目标样本的复制。详细信息请参见
     * {@link WritableRaster#setRect(Raster)}。
     *
     * @param dx        从源空间到目标空间的 X 轴平移因子。
     * @param dy        从源空间到目标空间的 Y 轴平移因子。
     * @param srcRaster 要从中复制像素的 Raster。
     *
     * @throws NullPointerException 如果 srcRaster 为 null。
     */
    public void setRect(int dx, int dy, Raster srcRaster) {
        int width  = srcRaster.getWidth();
        int height = srcRaster.getHeight();
        int srcOffX = srcRaster.getMinX();
        int srcOffY = srcRaster.getMinY();
        int dstOffX = dx+srcOffX;
        int dstOffY = dy+srcOffY;

        // 裁剪到此光栅
        if (dstOffX < this.minX) {
            int skipX = this.minX - dstOffX;
            width -= skipX;
            srcOffX += skipX;
            dstOffX = this.minX;
        }
        if (dstOffY < this.minY) {
            int skipY = this.minY - dstOffY;
            height -= skipY;
            srcOffY += skipY;
            dstOffY = this.minY;
        }
        if (dstOffX+width > this.minX+this.width) {
            width = this.minX + this.width - dstOffX;
        }
        if (dstOffY+height > this.minY+this.height) {
            height = this.minY + this.height - dstOffY;
        }

        if (width <= 0 || height <= 0) {
            return;
        }

        switch (srcRaster.getSampleModel().getDataType()) {
        case DataBuffer.TYPE_BYTE:
        case DataBuffer.TYPE_SHORT:
        case DataBuffer.TYPE_USHORT:
        case DataBuffer.TYPE_INT:
            int[] iData = null;
            for (int startY=0; startY < height; startY++) {
                // 每次获取一行
                iData =
                    srcRaster.getPixels(srcOffX, srcOffY+startY, width, 1,
                                        iData);
                setPixels(dstOffX, dstOffY+startY, width, 1, iData);
            }
            break;

        case DataBuffer.TYPE_FLOAT:
            float[] fData = null;
            for (int startY=0; startY < height; startY++) {
                fData =
                    srcRaster.getPixels(srcOffX, srcOffY+startY, width, 1,
                                        fData);
                setPixels(dstOffX, dstOffY+startY, width, 1, fData);
            }
            break;

        case DataBuffer.TYPE_DOUBLE:
            double[] dData = null;
            for (int startY=0; startY < height; startY++) {
                // 每次获取一行
                dData =
                    srcRaster.getPixels(srcOffX, srcOffY+startY, width, 1,
                                        dData);
                setPixels(dstOffX, dstOffY+startY, width, 1, dData);
            }
            break;
        }
    }

    /**
     * 使用 int 数组中的样本设置 DataBuffer 中的一个像素。如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。
     * 但是，显式边界检查不是必需的。
     * @param x      像素位置的 X 坐标。
     * @param y      像素位置的 Y 坐标。
     * @param iArray 输入样本的 int 数组。
     *
     * @throws NullPointerException 如果 iArray 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标不在范围内，或者 iArray 太小而无法容纳输入。
     */
    public void setPixel(int x, int y, int iArray[]) {
        sampleModel.setPixel(x-sampleModelTranslateX,y-sampleModelTranslateY,
                             iArray,dataBuffer);
    }

    /**
     * 使用 float 数组中的样本设置 DataBuffer 中的一个像素。如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。
     * 但是，显式边界检查不是必需的。
     * @param x      像素位置的 X 坐标。
     * @param y      像素位置的 Y 坐标。
     * @param fArray 输入样本的 float 数组。
     *
     * @throws NullPointerException 如果 fArray 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标不在范围内，或者 fArray 太小而无法容纳输入。
     */
    public void setPixel(int x, int y, float fArray[]) {
        sampleModel.setPixel(x-sampleModelTranslateX,y-sampleModelTranslateY,
                             fArray,dataBuffer);
    }

    /**
     * 使用 double 数组中的样本设置 DataBuffer 中的一个像素。如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。
     * 但是，显式边界检查不是必需的。
     * @param x      像素位置的 X 坐标。
     * @param y      像素位置的 Y 坐标。
     * @param dArray 输入样本的 double 数组。
     *
     * @throws NullPointerException 如果 dArray 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标不在范围内，或者 dArray 太小而无法容纳输入。
     */
    public void setPixel(int x, int y, double dArray[]) {
        sampleModel.setPixel(x-sampleModelTranslateX,y-sampleModelTranslateY,
                             dArray,dataBuffer);
    }

    /**
     * 从包含每个数组元素一个样本的 int 数组中设置一个矩形像素区域的所有样本。如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。
     * 但是，显式边界检查不是必需的。
     * @param x        上左角像素位置的 X 坐标。
     * @param y        上左角像素位置的 Y 坐标。
     * @param w        像素矩形的宽度。
     * @param h        像素矩形的高度。
     * @param iArray   输入 int 像素数组。
     *
     * @throws NullPointerException 如果 iArray 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标不在范围内，或者 iArray 太小而无法容纳输入。
     */
    public void setPixels(int x, int y, int w, int h, int iArray[]) {
        sampleModel.setPixels(x-sampleModelTranslateX,y-sampleModelTranslateY,
                              w,h,iArray,dataBuffer);
    }

    /**
     * 从包含每个数组元素一个样本的 float 数组中设置一个矩形像素区域的所有样本。如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。
     * 但是，显式边界检查不是必需的。
     * @param x        上左角像素位置的 X 坐标。
     * @param y        上左角像素位置的 Y 坐标。
     * @param w        像素矩形的宽度。
     * @param h        像素矩形的高度。
     * @param fArray   输入 float 像素数组。
     *
     * @throws NullPointerException 如果 fArray 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标不在范围内，或者 fArray 太小而无法容纳输入。
     */
    public void setPixels(int x, int y, int w, int h, float fArray[]) {
        sampleModel.setPixels(x-sampleModelTranslateX,y-sampleModelTranslateY,
                              w,h,fArray,dataBuffer);
    }

    /**
     * 从包含每个数组元素一个样本的 double 数组中设置一个矩形像素区域的所有样本。如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。
     * 但是，显式边界检查不是必需的。
     * @param x        上左角像素位置的 X 坐标。
     * @param y        上左角像素位置的 Y 坐标。
     * @param w        像素矩形的宽度。
     * @param h        像素矩形的高度。
     * @param dArray   输入 double 像素数组。
     *
     * @throws NullPointerException 如果 dArray 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标不在范围内，或者 dArray 太小而无法容纳输入。
     */
    public void setPixels(int x, int y, int w, int h, double dArray[]) {
        sampleModel.setPixels(x-sampleModelTranslateX,y-sampleModelTranslateY,
                              w,h,dArray,dataBuffer);
    }

    /**
     * 使用 int 作为输入，设置位于 (x,y) 的像素在指定波段中的样本。如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。
     * 但是，显式边界检查不是必需的。
     * @param x        像素位置的 X 坐标。
     * @param y        像素位置的 Y 坐标。
     * @param b        要设置的波段。
     * @param s        输入样本。
     *
     * @throws ArrayIndexOutOfBoundsException 如果坐标或波段索引不在范围内。
     */
    public void setSample(int x, int y, int b, int s) {
        sampleModel.setSample(x-sampleModelTranslateX,
                              y-sampleModelTranslateY, b, s,
                              dataBuffer);
    }

    /**
     * 使用 float 作为输入，设置位于 (x,y) 的像素在指定波段中的样本。如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。
     * 但是，显式边界检查不是必需的。
     * @param x        像素位置的 X 坐标。
     * @param y        像素位置的 Y 坐标。
     * @param b        要设置的波段。
     * @param s        输入样本。
     *
     * @throws ArrayIndexOutOfBoundsException 如果坐标或波段索引不在范围内。
     */
    public void setSample(int x, int y, int b, float s){
        sampleModel.setSample(x-sampleModelTranslateX,y-sampleModelTranslateY,
                              b,s,dataBuffer);
    }

    /**
     * 使用 double 作为输入，设置位于 (x,y) 的像素在指定波段中的样本。如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。
     * 但是，显式边界检查不是必需的。
     * @param x        像素位置的 X 坐标。
     * @param y        像素位置的 Y 坐标。
     * @param b        要设置的波段。
     * @param s        输入样本。
     *
     * @throws ArrayIndexOutOfBoundsException 如果坐标或波段索引不在范围内。
     */
    public void setSample(int x, int y, int b, double s){
        sampleModel.setSample(x-sampleModelTranslateX,y-sampleModelTranslateY,
                                    b,s,dataBuffer);
    }


                /**
     * 设置指定像素矩形中指定波段的样本，从包含每个数组元素一个样本的 int 数组中设置。
     * 如果坐标不在边界内，可能会抛出 ArrayIndexOutOfBoundsException。
     * 但是，显式边界检查不是必需的。
     * @param x        左上像素位置的 X 坐标。
     * @param y        左上像素位置的 Y 坐标。
     * @param w        像素矩形的宽度。
     * @param h        像素矩形的高度。
     * @param b        要设置的波段。
     * @param iArray   输入的 int 样本数组。
     *
     * @throws NullPointerException 如果 iArray 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标或波段索引不在边界内，或者 iArray 太小而无法容纳输入。
     */
    public void setSamples(int x, int y, int w, int h, int b,
                           int iArray[]) {
        sampleModel.setSamples(x-sampleModelTranslateX,y-sampleModelTranslateY,
                               w,h,b,iArray,dataBuffer);
    }

    /**
     * 设置指定像素矩形中指定波段的样本，从包含每个数组元素一个样本的 float 数组中设置。
     * 如果坐标不在边界内，可能会抛出 ArrayIndexOutOfBoundsException。
     * 但是，显式边界检查不是必需的。
     * @param x        左上像素位置的 X 坐标。
     * @param y        左上像素位置的 Y 坐标。
     * @param w        像素矩形的宽度。
     * @param h        像素矩形的高度。
     * @param b        要设置的波段。
     * @param fArray   输入的 float 样本数组。
     *
     * @throws NullPointerException 如果 fArray 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标或波段索引不在边界内，或者 fArray 太小而无法容纳输入。
     */
    public void setSamples(int x, int y, int w, int h, int b,
                           float fArray[]) {
        sampleModel.setSamples(x-sampleModelTranslateX,y-sampleModelTranslateY,
                               w,h,b,fArray,dataBuffer);
    }

    /**
     * 设置指定像素矩形中指定波段的样本，从包含每个数组元素一个样本的 double 数组中设置。
     * 如果坐标不在边界内，可能会抛出 ArrayIndexOutOfBoundsException。
     * 但是，显式边界检查不是必需的。
     * @param x        左上像素位置的 X 坐标。
     * @param y        左上像素位置的 Y 坐标。
     * @param w        像素矩形的宽度。
     * @param h        像素矩形的高度。
     * @param b        要设置的波段。
     * @param dArray   输入的 double 样本数组。
     *
     * @throws NullPointerException 如果 dArray 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标或波段索引不在边界内，或者 dArray 太小而无法容纳输入。
     */
    public void setSamples(int x, int y, int w, int h, int b,
                           double dArray[]) {
        sampleModel.setSamples(x-sampleModelTranslateX,y-sampleModelTranslateY,
                              w,h,b,dArray,dataBuffer);
    }

}
