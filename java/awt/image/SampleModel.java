
/*
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
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

/**
 *  此抽象类定义了一个用于提取图像中像素样本的接口。所有图像数据都表示为像素的集合。
 *  每个像素由多个样本组成。样本是图像中一个波段的数据，而波段由图像中特定类型的所有样本组成。
 *  例如，一个像素可能包含三个样本，分别表示其红色、绿色和蓝色分量。该像素所在的图像包含三个波段。
 *  一个波段包含图像中所有像素的红色样本。第二个波段包含所有绿色样本，最后一个波段包含所有蓝色样本。
 *  像素可以存储在不同的格式中。例如，特定波段的所有样本可以连续存储，或者单个像素的所有样本可以连续存储。
 *  <p>
 *  SampleModel 的子类指定它们可以表示的样本类型（例如，无符号 8 位字节、有符号 16 位短整型等）
 *  并可能指定样本在内存中的组织方式。在 Java 2D API 中，内置的图像处理操作符可能不支持所有可能的样本类型，
 *  但通常可以处理 16 位或更少的无符号整数样本。某些操作符支持更广泛的样本类型。
 *  <p>
 *  像素的集合由 Raster 表示，它由 DataBuffer 和 SampleModel 组成。SampleModel 允许访问 DataBuffer 中的样本，
 *  并可能提供程序员可以直接操作 DataBuffer 中样本和像素的低级信息。
 *  <p>
 *  通常，此类是处理图像的后备方法。更高效的代码将 SampleModel 铸造成适当的子类，并提取所需的信息以直接操作 DataBuffer 中的像素。
 *
 *  @see java.awt.image.DataBuffer
 *  @see java.awt.image.Raster
 *  @see java.awt.image.ComponentSampleModel
 *  @see java.awt.image.PixelInterleavedSampleModel
 *  @see java.awt.image.BandedSampleModel
 *  @see java.awt.image.MultiPixelPackedSampleModel
 *  @see java.awt.image.SinglePixelPackedSampleModel
 */

public abstract class SampleModel
{

    /** 此 SampleModel 描述的图像数据区域的宽度（以像素为单位）。 */
    protected int width;

    /** 此 SampleModel 描述的图像数据区域的高度（以像素为单位）。 */
    protected int height;

    /** 此 SampleModel 描述的图像数据的波段数。 */
    protected int numBands;

    /** 存储像素数据的 DataBuffer 的数据类型。
     *  @see java.awt.image.DataBuffer
     */
    protected int dataType;

    static private native void initIDs();
    static {
        ColorModel.loadLibraries();
        initIDs();
    }

    /**
     * 使用指定的参数构造 SampleModel。
     * @param dataType  存储像素数据的 DataBuffer 的数据类型。
     * @param w         图像数据区域的宽度（以像素为单位）。
     * @param h         图像数据区域的高度（以像素为单位）。
     * @param numBands  图像数据的波段数。
     * @throws IllegalArgumentException 如果 <code>w</code> 或 <code>h</code>
     *         不大于 0
     * @throws IllegalArgumentException 如果 <code>w</code>
     *         和 <code>h</code> 的乘积大于
     *         <code>Integer.MAX_VALUE</code>
     * @throws IllegalArgumentException 如果 <code>dataType</code> 不是
     *         支持的数据类型之一
     */
    public SampleModel(int dataType, int w, int h, int numBands)
    {
        long size = (long)w * h;
        if (w <= 0 || h <= 0) {
            throw new IllegalArgumentException("宽度 ("+w+") 和高度 ("+
                                               h+") 必须大于 0");
        }
        if (size >= Integer.MAX_VALUE) {
            throw new IllegalArgumentException("尺寸 (宽度="+w+
                                               " 高度="+h+") 太大");
        }

        if (dataType < DataBuffer.TYPE_BYTE ||
            (dataType > DataBuffer.TYPE_DOUBLE &&
             dataType != DataBuffer.TYPE_UNDEFINED))
        {
            throw new IllegalArgumentException("不支持的数据类型: "+
                                               dataType);
        }

        if (numBands <= 0) {
            throw new IllegalArgumentException("波段数必须大于 0");
        }

        this.dataType = dataType;
        this.width = w;
        this.height = h;
        this.numBands = numBands;
    }

    /** 返回宽度（以像素为单位）。
     *  @return 此 <code>SampleModel</code> 描述的图像数据区域的宽度（以像素为单位）。
     */
    final public int getWidth() {
         return width;
    }

    /** 返回高度（以像素为单位）。
     *  @return 此 <code>SampleModel</code> 描述的图像数据区域的高度（以像素为单位）。
     */
    final public int getHeight() {
         return height;
    }

    /** 返回图像数据的总波段数。
     *  @return 此 <code>SampleModel</code> 描述的图像数据的波段数。
     */
    final public int getNumBands() {
         return numBands;
    }

    /** 返回通过 getDataElements 和 setDataElements 方法传输像素所需的数据元素数量。
     *  当通过这些方法传输像素时，它们可能以打包或未打包的格式传输，具体取决于 SampleModel 的实现。
     *  使用这些方法，像素作为 getNumDataElements() 个元素的原始类型数组传输，该原始类型由 getTransferType() 给出。
     *  传输类型可能与存储数据类型不同。
     *  @return 数据元素的数量。
     *  @see #getDataElements(int, int, Object, DataBuffer)
     *  @see #getDataElements(int, int, int, int, Object, DataBuffer)
     *  @see #setDataElements(int, int, Object, DataBuffer)
     *  @see #setDataElements(int, int, int, int, Object, DataBuffer)
     *  @see #getTransferType
     */
    public abstract int getNumDataElements();

    /** 返回存储像素数据的 DataBuffer 的数据类型。
     *  @return 数据类型。
     */
    final public int getDataType() {
        return dataType;
    }

    /** 返回通过 getDataElements 和 setDataElements 方法传输像素时使用的传输类型。
     *  当通过这些方法传输像素时，它们可能以打包或未打包的格式传输，具体取决于 SampleModel 的实现。
     *  使用这些方法，像素作为 getNumDataElements() 个元素的原始类型数组传输，该原始类型由 getTransferType() 给出。
     *  传输类型可能与存储数据类型不同。传输类型将是 DataBuffer 中定义的类型之一。
     *  @return 传输类型。
     *  @see #getDataElements(int, int, Object, DataBuffer)
     *  @see #getDataElements(int, int, int, int, Object, DataBuffer)
     *  @see #setDataElements(int, int, Object, DataBuffer)
     *  @see #setDataElements(int, int, int, int, Object, DataBuffer)
     *  @see #getNumDataElements
     *  @see java.awt.image.DataBuffer
     */
    public int getTransferType() {
        return dataType;
    }

    /**
     * 返回指定像素的样本，每个样本对应数组中的一个元素。
     * 如果坐标超出范围，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素位置的 X 坐标
     * @param y         像素位置的 Y 坐标
     * @param iArray    如果非空，则在此数组中返回样本
     * @param data      包含图像数据的 DataBuffer
     * @return 指定像素的样本。
     * @see #setPixel(int, int, int[], DataBuffer)
     *
     * @throws NullPointerException 如果 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标超出范围，或者 iArray 太小无法容纳输出。
     */
    public int[] getPixel(int x, int y, int iArray[], DataBuffer data) {

        int pixels[];

        if (iArray != null)
            pixels = iArray;
        else
            pixels = new int[numBands];

        for (int i=0; i<numBands; i++) {
            pixels[i] = getSample(x, y, i, data);
        }

        return pixels;
    }

    /**
     * 以原始类型数组的形式返回单个像素的数据，类型为 TransferType。
     * 对于 Java 2D API 支持的图像数据，这将是 DataBuffer.TYPE_BYTE、DataBuffer.TYPE_USHORT、
     * DataBuffer.TYPE_INT、DataBuffer.TYPE_SHORT、DataBuffer.TYPE_FLOAT 或 DataBuffer.TYPE_DOUBLE 之一。
     * 数据可能以打包格式返回，从而提高数据传输的效率。通常，obj 应该传递为 null，以便自动创建 Object 并且是正确的原始数据类型。
     * <p>
     * 以下代码说明了如何将数据从 DataBuffer <code>db1</code>（其存储布局由 SampleModel <code>sm1</code> 描述）
     * 传输到 DataBuffer <code>db2</code>（其存储布局由 SampleModel <code>sm2</code> 描述）。
     * 该传输通常比使用 getPixel/setPixel 更高效。
     * <pre>
     *       SampleModel sm1, sm2;
     *       DataBuffer db1, db2;
     *       sm2.setDataElements(x, y, sm1.getDataElements(x, y, null, db1), db2);
     * </pre>
     * 使用 getDataElements/setDataElements 在两个 DataBuffer/SampleModel 对之间传输数据是合法的，前提是 SampleModel 具有相同的波段数，
     * 对应的波段具有相同的每样本位数，并且传输类型相同。
     * <p>
     * 如果 obj 非空，它应该是一个类型为 TransferType 的原始数组。否则，将抛出 ClassCastException。
     * 如果坐标超出范围，或者 obj 非空且不足以容纳像素数据，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素位置的 X 坐标。
     * @param y         像素位置的 Y 坐标。
     * @param obj       如果非空，则在此原始数组中返回像素数据。
     * @param data      包含图像数据的 DataBuffer。
     * @return 指定像素的数据元素。
     * @see #getNumDataElements
     * @see #getTransferType
     * @see java.awt.image.DataBuffer
     * @see #setDataElements(int, int, Object, DataBuffer)
     *
     * @throws NullPointerException 如果 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标超出范围，或者 obj 太小无法容纳输出。
     */
    public abstract Object getDataElements(int x, int y,
                                           Object obj, DataBuffer data);

    /**
     * 以原始类型数组的形式返回指定矩形区域像素的像素数据，类型为 TransferType。
     * 对于 Java 2D API 支持的图像数据，这将是 DataBuffer.TYPE_BYTE、DataBuffer.TYPE_USHORT、
     * DataBuffer.TYPE_INT、DataBuffer.TYPE_SHORT、DataBuffer.TYPE_FLOAT 或 DataBuffer.TYPE_DOUBLE 之一。
     * 数据可能以打包格式返回，从而提高数据传输的效率。通常，obj 应该传递为 null，以便自动创建 Object 并且是正确的原始数据类型。
     * <p>
     * 以下代码说明了如何将数据从 DataBuffer <code>db1</code>（其存储布局由 SampleModel <code>sm1</code> 描述）
     * 传输到 DataBuffer <code>db2</code>（其存储布局由 SampleModel <code>sm2</code> 描述）。
     * 该传输通常比使用 getPixels/setPixels 更高效。
     * <pre>
     *       SampleModel sm1, sm2;
     *       DataBuffer db1, db2;
     *       sm2.setDataElements(x, y, w, h, sm1.getDataElements(x, y, w,
     *                           h, null, db1), db2);
     * </pre>
     * 使用 getDataElements/setDataElements 在两个 DataBuffer/SampleModel 对之间传输数据是合法的，前提是 SampleModel 具有相同的波段数，
     * 对应的波段具有相同的每样本位数，并且传输类型相同。
     * <p>
     * 如果 obj 非空，它应该是一个类型为 TransferType 的原始数组。否则，将抛出 ClassCastException。
     * 如果坐标超出范围，或者 obj 非空且不足以容纳像素数据，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素矩形的最小 X 坐标。
     * @param y         像素矩形的最小 Y 坐标。
     * @param w         像素矩形的宽度。
     * @param h         像素矩形的高度。
     * @param obj       如果非空，则在此原始数组中返回像素数据。
     * @param data      包含图像数据的 DataBuffer。
     * @return 指定区域像素的数据元素。
     * @see #getNumDataElements
     * @see #getTransferType
     * @see #setDataElements(int, int, int, int, Object, DataBuffer)
     * @see java.awt.image.DataBuffer
     *
     * @throws NullPointerException 如果 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标超出范围，或者 obj 太小无法容纳输出。
     */
    public Object getDataElements(int x, int y, int w, int h,
                                  Object obj, DataBuffer data) {


                    int type = getTransferType();
        int numDataElems = getNumDataElements();
        int cnt = 0;
        Object o = null;

        int x1 = x + w;
        int y1 = y + h;

        if (x < 0 || x >= width || w > width || x1 < 0 || x1 > width ||
            y < 0 || y >= height || h > height || y1 < 0 || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("无效的坐标。");
        }

        switch(type) {

        case DataBuffer.TYPE_BYTE:

            byte[] btemp;
            byte[] bdata;

            if (obj == null)
                bdata = new byte[numDataElems*w*h];
            else
                bdata = (byte[])obj;

            for (int i=y; i<y1; i++) {
                for (int j=x; j<x1; j++) {
                    o = getDataElements(j, i, o, data);
                    btemp = (byte[])o;
                    for (int k=0; k<numDataElems; k++) {
                        bdata[cnt++] = btemp[k];
                    }
                }
            }
            obj = (Object)bdata;
            break;

        case DataBuffer.TYPE_USHORT:
        case DataBuffer.TYPE_SHORT:

            short[] sdata;
            short[] stemp;

            if (obj == null)
                sdata = new short[numDataElems*w*h];
            else
                sdata = (short[])obj;

            for (int i=y; i<y1; i++) {
                for (int j=x; j<x1; j++) {
                    o = getDataElements(j, i, o, data);
                    stemp = (short[])o;
                    for (int k=0; k<numDataElems; k++) {
                        sdata[cnt++] = stemp[k];
                    }
                }
            }

            obj = (Object)sdata;
            break;

        case DataBuffer.TYPE_INT:

            int[] idata;
            int[] itemp;

            if (obj == null)
                idata = new int[numDataElems*w*h];
            else
                idata = (int[])obj;

            for (int i=y; i<y1; i++) {
                for (int j=x; j<x1; j++) {
                    o = getDataElements(j, i, o, data);
                    itemp = (int[])o;
                    for (int k=0; k<numDataElems; k++) {
                        idata[cnt++] = itemp[k];
                    }
                }
            }

            obj = (Object)idata;
            break;

        case DataBuffer.TYPE_FLOAT:

            float[] fdata;
            float[] ftemp;

            if (obj == null)
                fdata = new float[numDataElems*w*h];
            else
                fdata = (float[])obj;

            for (int i=y; i<y1; i++) {
                for (int j=x; j<x1; j++) {
                    o = getDataElements(j, i, o, data);
                    ftemp = (float[])o;
                    for (int k=0; k<numDataElems; k++) {
                        fdata[cnt++] = ftemp[k];
                    }
                }
            }

            obj = (Object)fdata;
            break;

        case DataBuffer.TYPE_DOUBLE:

            double[] ddata;
            double[] dtemp;

            if (obj == null)
                ddata = new double[numDataElems*w*h];
            else
                ddata = (double[])obj;

            for (int i=y; i<y1; i++) {
                for (int j=x; j<x1; j++) {
                    o = getDataElements(j, i, o, data);
                    dtemp = (double[])o;
                    for (int k=0; k<numDataElems; k++) {
                        ddata[cnt++] = dtemp[k];
                    }
                }
            }

            obj = (Object)ddata;
            break;
        }

        return obj;
    }

    /**
     * 从一个基本类型的数组中设置指定 DataBuffer 中单个像素的数据。对于 Java 2D API 支持的图像数据，这将是 DataBuffer.TYPE_BYTE、
     * DataBuffer.TYPE_USHORT、DataBuffer.TYPE_INT、DataBuffer.TYPE_SHORT、DataBuffer.TYPE_FLOAT 或 DataBuffer.TYPE_DOUBLE 之一。
     * 数组中的数据可能是打包格式，从而提高数据传输的效率。
     * <p>
     * 以下代码说明了如何将数据从 DataBuffer <code>db1</code>（其存储布局由 SampleModel <code>sm1</code> 描述）传输到
     * DataBuffer <code>db2</code>（其存储布局由 SampleModel <code>sm2</code> 描述）。这种传输通常比使用 getPixel/setPixel 更高效。
     * <pre>
     *       SampleModel sm1, sm2;
     *       DataBuffer db1, db2;
     *       sm2.setDataElements(x, y, sm1.getDataElements(x, y, null, db1),
     *                           db2);
     * </pre>
     * 使用 getDataElements/setDataElements 在两个 DataBuffer/SampleModel 对之间传输数据是合法的，前提是 SampleModels 具有相同数量的波段，
     * 对应的波段具有相同数量的每样本位数，并且 TransferTypes 相同。
     * <p>
     * obj 必须是一个基本类型的数组，类型为 TransferType。否则，将抛出 ClassCastException。如果坐标不在范围内，或者 obj 不足以容纳像素数据，
     * 可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素位置的 X 坐标。
     * @param y         像素位置的 Y 坐标。
     * @param obj       包含像素数据的基本类型数组。
     * @param data      包含图像数据的 DataBuffer。
     * @see #getNumDataElements
     * @see #getTransferType
     * @see #getDataElements(int, int, Object, DataBuffer)
     * @see java.awt.image.DataBuffer
     *
     * @throws NullPointerException 如果 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标不在范围内，或者 obj 太小而无法容纳输入。
     */
    public abstract void setDataElements(int x, int y,
                                         Object obj, DataBuffer data);

    /**
     * 从一个基本类型的数组中设置指定 DataBuffer 中矩形区域像素的数据。对于 Java 2D API 支持的图像数据，这将是 DataBuffer.TYPE_BYTE、
     * DataBuffer.TYPE_USHORT、DataBuffer.TYPE_INT、DataBuffer.TYPE_SHORT、DataBuffer.TYPE_FLOAT 或 DataBuffer.TYPE_DOUBLE 之一。
     * 数组中的数据可能是打包格式，从而提高数据传输的效率。
     * <p>
     * 以下代码说明了如何将数据从 DataBuffer <code>db1</code>（其存储布局由 SampleModel <code>sm1</code> 描述）传输到
     * DataBuffer <code>db2</code>（其存储布局由 SampleModel <code>sm2</code> 描述）。这种传输通常比使用 getPixels/setPixels 更高效。
     * <pre>
     *       SampleModel sm1, sm2;
     *       DataBuffer db1, db2;
     *       sm2.setDataElements(x, y, w, h, sm1.getDataElements(x, y, w, h,
     *                           null, db1), db2);
     * </pre>
     * 使用 getDataElements/setDataElements 在两个 DataBuffer/SampleModel 对之间传输数据是合法的，前提是 SampleModels 具有相同数量的波段，
     * 对应的波段具有相同数量的每样本位数，并且 TransferTypes 相同。
     * <p>
     * obj 必须是一个基本类型的数组，类型为 TransferType。否则，将抛出 ClassCastException。如果坐标不在范围内，或者 obj 不足以容纳像素数据，
     * 可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素矩形的最小 X 坐标。
     * @param y         像素矩形的最小 Y 坐标。
     * @param w         像素矩形的宽度。
     * @param h         像素矩形的高度。
     * @param obj       包含像素数据的基本类型数组。
     * @param data      包含图像数据的 DataBuffer。
     * @see #getNumDataElements
     * @see #getTransferType
     * @see #getDataElements(int, int, int, int, Object, DataBuffer)
     * @see java.awt.image.DataBuffer
     *
     * @throws NullPointerException 如果 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标不在范围内，或者 obj 太小而无法容纳输入。
     */
    public void setDataElements(int x, int y, int w, int h,
                                Object obj, DataBuffer data) {

        int cnt = 0;
        Object o = null;
        int type = getTransferType();
        int numDataElems = getNumDataElements();

        int x1 = x + w;
        int y1 = y + h;

        if (x < 0 || x >= width || w > width || x1 < 0 || x1 > width ||
            y < 0 || y >= height || h > height || y1 < 0 || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("无效的坐标。");
        }

        switch(type) {

        case DataBuffer.TYPE_BYTE:

            byte[] barray = (byte[])obj;
            byte[] btemp = new byte[numDataElems];

            for (int i=y; i<y1; i++) {
                for (int j=x; j<x1; j++) {
                    for (int k=0; k<numDataElems; k++) {
                        btemp[k] = barray[cnt++];
                    }

                    setDataElements(j, i, btemp, data);
                }
            }
            break;

        case DataBuffer.TYPE_USHORT:
        case DataBuffer.TYPE_SHORT:

            short[] sarray = (short[])obj;
            short[] stemp = new short[numDataElems];

            for (int i=y; i<y1; i++) {
                for (int j=x; j<x1; j++) {
                    for (int k=0; k<numDataElems; k++) {
                        stemp[k] = sarray[cnt++];
                    }

                    setDataElements(j, i, stemp, data);
                }
            }
            break;

        case DataBuffer.TYPE_INT:

            int[] iArray = (int[])obj;
            int[] itemp = new int[numDataElems];

            for (int i=y; i<y1; i++) {
                for (int j=x; j<x1; j++) {
                    for (int k=0; k<numDataElems; k++) {
                        itemp[k] = iArray[cnt++];
                    }

                    setDataElements(j, i, itemp, data);
                }
            }
            break;

        case DataBuffer.TYPE_FLOAT:

            float[] fArray = (float[])obj;
            float[] ftemp = new float[numDataElems];

            for (int i=y; i<y1; i++) {
                for (int j=x; j<x1; j++) {
                    for (int k=0; k<numDataElems; k++) {
                        ftemp[k] = fArray[cnt++];
                    }

                    setDataElements(j, i, ftemp, data);
                }
            }
            break;

        case DataBuffer.TYPE_DOUBLE:

            double[] dArray = (double[])obj;
            double[] dtemp = new double[numDataElems];

            for (int i=y; i<y1; i++) {
                for (int j=x; j<x1; j++) {
                    for (int k=0; k<numDataElems; k++) {
                        dtemp[k] = dArray[cnt++];
                    }

                    setDataElements(j, i, dtemp, data);
                }
            }
            break;
        }

    }

    /**
     * 返回指定像素的样本值，以 float 数组的形式返回。如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素位置的 X 坐标。
     * @param y         像素位置的 Y 坐标。
     * @param fArray    如果非空，则返回样本值到此数组。
     * @param data      包含图像数据的 DataBuffer。
     * @return 指定像素的样本值。
     * @see #setPixel(int, int, float[], DataBuffer)
     *
     * @throws NullPointerException 如果 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标不在范围内，或者 fArray 太小而无法容纳输出。
     */
    public float[] getPixel(int x, int y, float fArray[],
                            DataBuffer data) {

        float pixels[];

        if (fArray != null)
            pixels = fArray;
        else
            pixels = new float[numBands];

        for (int i=0; i<numBands; i++)
            pixels[i] = getSampleFloat(x, y, i, data);

        return pixels;
    }

    /**
     * 返回指定像素的样本值，以 double 数组的形式返回。如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素位置的 X 坐标。
     * @param y         像素位置的 Y 坐标。
     * @param dArray    如果非空，则返回样本值到此数组。
     * @param data      包含图像数据的 DataBuffer。
     * @return 指定像素的样本值。
     * @see #setPixel(int, int, double[], DataBuffer)
     *
     * @throws NullPointerException 如果 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标不在范围内，或者 dArray 太小而无法容纳输出。
     */
    public double[] getPixel(int x, int y, double dArray[],
                             DataBuffer data) {

        double pixels[];

        if(dArray != null)
            pixels = dArray;
        else
            pixels = new double[numBands];

        for (int i=0; i<numBands; i++)
            pixels[i] = getSampleDouble(x, y, i, data);

        return pixels;
    }

    /**
     * 返回指定矩形区域像素的所有样本值，以 int 数组的形式返回，每个数组元素一个样本值。如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         左上角像素位置的 X 坐标。
     * @param y         左上角像素位置的 Y 坐标。
     * @param w         像素矩形的宽度。
     * @param h         像素矩形的高度。
     * @param iArray    如果非空，则返回样本值到此数组。
     * @param data      包含图像数据的 DataBuffer。
     * @return 指定区域像素的所有样本值。
     * @see #setPixels(int, int, int, int, int[], DataBuffer)
     *
     * @throws NullPointerException 如果 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标不在范围内，或者 iArray 太小而无法容纳输出。
     */
    public int[] getPixels(int x, int y, int w, int h,
                           int iArray[], DataBuffer data) {

        int pixels[];
        int Offset=0;
        int x1 = x + w;
        int y1 = y + h;

        if (x < 0 || x >= width || w > width || x1 < 0 || x1 > width ||
            y < 0 || y >= height || h > height || y1 < 0 || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("无效的坐标。");
        }

        if (iArray != null)
            pixels = iArray;
        else
            pixels = new int[numBands * w * h];

        for (int i=y; i<y1; i++) {
            for (int j=x; j<x1; j++) {
                for(int k=0; k<numBands; k++) {
                    pixels[Offset++] = getSample(j, i, k, data);
                }
            }
        }

        return pixels;
    }

    /**
     * 返回指定矩形区域像素的所有样本值，以 float 数组的形式返回，每个数组元素一个样本值。如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         左上角像素位置的 X 坐标。
     * @param y         左上角像素位置的 Y 坐标。
     * @param w         像素矩形的宽度。
     * @param h         像素矩形的高度。
     * @param fArray    如果非空，则返回样本值到此数组。
     * @param data      包含图像数据的 DataBuffer。
     * @return 指定区域像素的所有样本值。
     * @see #setPixels(int, int, int, int, float[], DataBuffer)
     *
     * @throws NullPointerException 如果 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标不在范围内，或者 fArray 太小而无法容纳输出。
     */
    public float[] getPixels(int x, int y, int w, int h,
                             float fArray[], DataBuffer data) {


        if (x < 0 || x >= width || w > width || x1 < 0 || x1 > width ||
            y < 0 || y >= height || h > height || y1 < 0 || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }

        for (int i=y; i<y1; i++) {
            for (int j=x; j<x1; j++) {
                for (int k=0; k<numBands; k++) {
                    setSample(j, i, k, fArray[Offset++], data);
                }
            }
        }
    }

    /**
     * 设置所有样本，用于从包含每个数组元素一个样本的双精度数组中设置矩形像素。
     * 如果坐标超出范围，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         矩形左上角像素的 X 坐标。
     * @param y         矩形左上角像素的 Y 坐标。
     * @param w         像素矩形的宽度。
     * @param h         像素矩形的高度。
     * @param dArray    包含输入样本的双精度数组。
     * @param data      包含图像数据的 DataBuffer。
     * @see #getPixels(int, int, int, int, double[], DataBuffer)
     *
     * @throws NullPointerException 如果 dArray 或 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标超出范围，或者 dArray 太小无法容纳输入。
     */
    public void setPixels(int x, int y, int w, int h,
                          double dArray[], DataBuffer data) {
        int Offset=0;
        int x1 = x + w;
        int y1 = y + h;

        if (x < 0 || x >= width || w > width || x1 < 0 || x1 > width ||
            y < 0 || y >= height || h > height || y1 < 0 || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }

        for (int i=y; i<y1; i++) {
            for (int j=x; j<x1; j++) {
                for (int k=0; k<numBands; k++) {
                    setSample(j, i, k, dArray[Offset++], data);
                }
            }
        }
    }

    /**
     * 设置指定像素位置 (x, y) 的指定波段的样本值。
     * 如果坐标超出范围，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素位置的 X 坐标。
     * @param y         像素位置的 Y 坐标。
     * @param b         要设置的波段。
     * @param sample    要设置的样本值。
     * @param data      包含图像数据的 DataBuffer。
     * @see #getSample(int, int, int, DataBuffer)
     *
     * @throws NullPointerException 如果 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标或波段索引超出范围。
     */
    public abstract void setSample(int x, int y, int b, int sample, DataBuffer data);

    /**
     * 设置指定像素位置 (x, y) 的指定波段的样本值。
     * 如果坐标超出范围，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素位置的 X 坐标。
     * @param y         像素位置的 Y 坐标。
     * @param b         要设置的波段。
     * @param sample    要设置的样本值。
     * @param data      包含图像数据的 DataBuffer。
     * @see #getSampleFloat(int, int, int, DataBuffer)
     *
     * @throws NullPointerException 如果 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标或波段索引超出范围。
     */
    public void setSample(int x, int y, int b, float sample, DataBuffer data) {
        setSample(x, y, b, (int) sample, data);
    }

    /**
     * 设置指定像素位置 (x, y) 的指定波段的样本值。
     * 如果坐标超出范围，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素位置的 X 坐标。
     * @param y         像素位置的 Y 坐标。
     * @param b         要设置的波段。
     * @param sample    要设置的样本值。
     * @param data      包含图像数据的 DataBuffer。
     * @see #getSampleDouble(int, int, int, DataBuffer)
     *
     * @throws NullPointerException 如果 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标或波段索引超出范围。
     */
    public void setSample(int x, int y, int b, double sample, DataBuffer data) {
        setSample(x, y, b, (int) sample, data);
    }

    /**
     * 设置指定矩形区域的指定波段的所有样本值。
     * 如果坐标超出范围，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         矩形左上角像素的 X 坐标。
     * @param y         矩形左上角像素的 Y 坐标。
     * @param w         像素矩形的宽度。
     * @param h         像素矩形的高度。
     * @param b         要设置的波段。
     * @param iArray    包含输入样本的整型数组。
     * @param data      包含图像数据的 DataBuffer。
     * @see #getSamples(int, int, int, int, int, int[], DataBuffer)
     *
     * @throws NullPointerException 如果 iArray 或 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标或波段索引超出范围，或者 iArray 太小无法容纳输入。
     */
    public void setSamples(int x, int y, int w, int h, int b,
                           int iArray[], DataBuffer data) {
        int Offset=0;
        int x1 = x + w;
        int y1 = y + h;

        if (x < 0 || x1 < x || x1 > width ||
            y < 0 || y1 < y || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }

        for (int i=y; i<y1; i++) {
            for (int j=x; j<x1; j++) {
                setSample(j, i, b, iArray[Offset++], data);
            }
        }
    }

    /**
     * 设置指定矩形区域的指定波段的所有样本值。
     * 如果坐标超出范围，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         矩形左上角像素的 X 坐标。
     * @param y         矩形左上角像素的 Y 坐标。
     * @param w         像素矩形的宽度。
     * @param h         像素矩形的高度。
     * @param b         要设置的波段。
     * @param fArray    包含输入样本的浮点型数组。
     * @param data      包含图像数据的 DataBuffer。
     * @see #getSamples(int, int, int, int, int, float[], DataBuffer)
     *
     * @throws NullPointerException 如果 fArray 或 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标或波段索引超出范围，或者 fArray 太小无法容纳输入。
     */
    public void setSamples(int x, int y, int w, int h, int b,
                           float fArray[], DataBuffer data) {
        int Offset=0;
        int x1 = x + w;
        int y1 = y + h;

        if (x < 0 || x1 < x || x1 > width ||
            y < 0 || y1 < y || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }

        for (int i=y; i<y1; i++) {
            for (int j=x; j<x1; j++) {
                setSample(j, i, b, fArray[Offset++], data);
            }
        }
    }

    /**
     * 设置指定矩形区域的指定波段的所有样本值。
     * 如果坐标超出范围，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         矩形左上角像素的 X 坐标。
     * @param y         矩形左上角像素的 Y 坐标。
     * @param w         像素矩形的宽度。
     * @param h         像素矩形的高度。
     * @param b         要设置的波段。
     * @param dArray    包含输入样本的双精度数组。
     * @param data      包含图像数据的 DataBuffer。
     * @see #getSamples(int, int, int, int, int, double[], DataBuffer)
     *
     * @throws NullPointerException 如果 dArray 或 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标或波段索引超出范围，或者 dArray 太小无法容纳输入。
     */
    public void setSamples(int x, int y, int w, int h, int b,
                           double dArray[], DataBuffer data) {
        int Offset=0;
        int x1 = x + w;
        int y1 = y + h;

        if (x < 0 || x1 < x || x1 > width ||
            y < 0 || y1 < y || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("Invalid coordinates.");
        }

        for (int i=y; i<y1; i++) {
            for (int j=x; j<x1; j++) {
                setSample(j, i, b, dArray[Offset++], data);
            }
        }
    }


                    if (x < 0 || x >= width || w > width || x1 < 0 || x1 > width||
            y < 0 || y >= height || h > height || y1 < 0 || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("无效的坐标。");
        }

        for (int i=y; i<y1; i++) {
            for (int j=x; j<x1; j++) {
                for(int k=0; k<numBands; k++) {
                    setSample(j, i, k, fArray[Offset++], data);
                }
            }
        }
    }

    /**
     * 从包含每个数组元素一个样本的 double 数组中设置像素矩形的所有样本。
     * 如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素左上角的 X 坐标。
     * @param y         像素左上角的 Y 坐标。
     * @param w         像素矩形的宽度。
     * @param h         像素矩形的高度。
     * @param dArray    输入样本的 double 数组。
     * @param data      包含图像数据的 DataBuffer。
     * @see #getPixels(int, int, int, int, double[], DataBuffer)
     *
     * @throws NullPointerException 如果 dArray 或 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标不在范围内，或者 dArray 太小无法容纳输入。
     */
    public void setPixels(int x, int y, int w, int h,
                          double dArray[], DataBuffer data) {
        int Offset=0;
        int x1 = x + w;
        int y1 = y + h;

        if (x < 0 || x >= width || w > width || x1 < 0 || x1 > width ||
            y < 0 || y >= height || h > height || y1 < 0 || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("无效的坐标。");
        }

        for (int i=y; i<y1; i++) {
            for (int j=x; j<x1; j++) {
                for (int k=0; k<numBands; k++) {
                    setSample(j, i, k, dArray[Offset++], data);
                }
            }
        }
    }

    /**
     * 使用 int 输入在 DataBuffer 中 (x,y) 位置的指定带中设置样本。
     * 如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素位置的 X 坐标。
     * @param y         像素位置的 Y 坐标。
     * @param b         要设置的带。
     * @param s         输入样本为 int。
     * @param data      包含图像数据的 DataBuffer。
     * @see #getSample(int, int, int,  DataBuffer)
     *
     * @throws NullPointerException 如果 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标或带索引不在范围内。
     */
    public abstract void setSample(int x, int y, int b,
                                   int s,
                                   DataBuffer data);

    /**
     * 使用 float 输入在 DataBuffer 中 (x,y) 位置的指定带中设置样本。
     * 此方法的默认实现将输入的 float 样本转换为 int，然后使用该 int 值调用
     * <code>setSample(int, int, int, DataBuffer)</code> 方法。
     * 如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素位置的 X 坐标。
     * @param y         像素位置的 Y 坐标。
     * @param b         要设置的带。
     * @param s         输入样本为 float。
     * @param data      包含图像数据的 DataBuffer。
     * @see #getSample(int, int, int, DataBuffer)
     *
     * @throws NullPointerException 如果 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标或带索引不在范围内。
     */
    public void setSample(int x, int y, int b,
                          float s ,
                          DataBuffer data) {
        int sample = (int)s;

        setSample(x, y, b, sample, data);
    }

    /**
     * 使用 double 输入在 DataBuffer 中 (x,y) 位置的指定带中设置样本。
     * 此方法的默认实现将输入的 double 样本转换为 int，然后使用该 int 值调用
     * <code>setSample(int, int, int, DataBuffer)</code> 方法。
     * 如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素位置的 X 坐标。
     * @param y         像素位置的 Y 坐标。
     * @param b         要设置的带。
     * @param s         输入样本为 double。
     * @param data      包含图像数据的 DataBuffer。
     * @see #getSample(int, int, int, DataBuffer)
     *
     * @throws NullPointerException 如果 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标或带索引不在范围内。
     */
    public void setSample(int x, int y, int b,
                          double s,
                          DataBuffer data) {
        int sample = (int)s;

        setSample(x, y, b, sample, data);
    }

    /**
     * 从包含每个数组元素一个样本的 int 数组中设置指定矩形的像素在指定带中的样本。
     * 如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素左上角的 X 坐标。
     * @param y         像素左上角的 Y 坐标。
     * @param w         像素矩形的宽度。
     * @param h         像素矩形的高度。
     * @param b         要设置的带。
     * @param iArray    输入样本的 int 数组。
     * @param data      包含图像数据的 DataBuffer。
     * @see #getSamples(int, int, int, int, int, int[], DataBuffer)
     *
     * @throws NullPointerException 如果 iArray 或 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标或带索引不在范围内，或者 iArray 太小无法容纳输入。
     */
    public void setSamples(int x, int y, int w, int h, int b,
                           int iArray[], DataBuffer data) {

        int Offset=0;
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= width || w > width || x1 < 0 || x1 > width ||
            y < 0 || y >= height || h > height || y1 < 0 || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("无效的坐标。");
        }

        for (int i=y; i<y1; i++) {
            for (int j=x; j<x1; j++) {
                setSample(j, i, b, iArray[Offset++], data);
            }
        }
    }

    /**
     * 从包含每个数组元素一个样本的 float 数组中设置指定矩形的像素在指定带中的样本。
     * 如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素左上角的 X 坐标。
     * @param y         像素左上角的 Y 坐标。
     * @param w         像素矩形的宽度。
     * @param h         像素矩形的高度。
     * @param b         要设置的带。
     * @param fArray    输入样本的 float 数组。
     * @param data      包含图像数据的 DataBuffer。
     * @see #getSamples(int, int, int, int, int, float[], DataBuffer)
     *
     * @throws NullPointerException 如果 fArray 或 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标或带索引不在范围内，或者 fArray 太小无法容纳输入。
     */
    public void setSamples(int x, int y, int w, int h, int b,
                           float fArray[], DataBuffer data) {
        int Offset=0;
        int x1 = x + w;
        int y1 = y + h;

        if (x < 0 || x >= width || w > width || x1 < 0 || x1 > width ||
            y < 0 || y >= height || h > height || y1 < 0 || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("无效的坐标。");
        }

        for (int i=y; i<y1; i++) {
            for (int j=x; j<x1; j++) {
                setSample(j, i, b, fArray[Offset++], data);
            }
        }
    }

    /**
     * 从包含每个数组元素一个样本的 double 数组中设置指定矩形的像素在指定带中的样本。
     * 如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素左上角的 X 坐标。
     * @param y         像素左上角的 Y 坐标。
     * @param w         像素矩形的宽度。
     * @param h         像素矩形的高度。
     * @param b         要设置的带。
     * @param dArray    输入样本的 double 数组。
     * @param data      包含图像数据的 DataBuffer。
     * @see #getSamples(int, int, int, int, int, double[], DataBuffer)
     *
     * @throws NullPointerException 如果 dArray 或 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标或带索引不在范围内，或者 dArray 太小无法容纳输入。
     */
    public void setSamples(int x, int y, int w, int h, int b,
                           double dArray[], DataBuffer data) {
        int Offset=0;
        int x1 = x + w;
        int y1 = y + h;


        if (x < 0 || x >= width || w > width || x1 < 0 || x1 > width ||
            y < 0 || y >= height || h > height || y1 < 0 || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException("无效的坐标。");
        }

        for (int i=y; i<y1; i++) {
            for (int j=x; j<x1; j++) {
                setSample(j, i, b, dArray[Offset++], data);
            }
        }
    }

    /**
     * 创建一个描述此 SampleModel 格式的数据，但具有不同宽度和高度的 SampleModel。
     * @param w 图像数据的宽度。
     * @param h 图像数据的高度。
     * @return 一个描述与该 SampleModel 相同的图像数据，但具有不同大小的 <code>SampleModel</code>。
     */
    public abstract SampleModel createCompatibleSampleModel(int w, int h);

    /**
     * 创建一个具有此 SampleModel 的子集带的新 SampleModel。
     * @param bands 此 <code>SampleModel</code> 的子集带。
     * @return 一个具有此 <code>SampleModel</code> 的子集带的 <code>SampleModel</code>。
     */
    public abstract SampleModel createSubsetSampleModel(int bands[]);

    /**
     * 创建一个与此 SampleModel 对应的 DataBuffer。
     * DataBuffer 的宽度和高度将与此 SampleModel 匹配。
     * @return 一个与此 <code>SampleModel</code> 对应的 <code>DataBuffer</code>。
     */
    public abstract DataBuffer createDataBuffer();

    /** 返回所有带的样本大小（以位为单位）。
     *  @return 所有带的样本大小。
     */
    public abstract int[] getSampleSize();

    /** 返回指定带的样本大小（以位为单位）。
     *  @param band 指定的带。
     *  @return 指定带的样本大小。
     */
    public abstract int getSampleSize(int band);

}
