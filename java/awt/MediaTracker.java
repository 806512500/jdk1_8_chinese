
/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Component;
import java.awt.Image;
import java.awt.image.ImageObserver;
import sun.awt.image.MultiResolutionToolkitImage;

/**
 * <code>MediaTracker</code> 类是一个用于跟踪多个媒体对象状态的工具类。媒体对象可以包括音频片段和图像，但目前仅支持图像。
 * <p>
 * 使用媒体跟踪器时，创建一个 <code>MediaTracker</code> 实例，并为每个要跟踪的图像调用其 <code>addImage</code> 方法。
 * 此外，每个图像可以分配一个唯一的标识符。此标识符控制图像的加载优先级，也可以用于标识可以独立等待的独特图像子集。
 * 具有较低 ID 的图像优先于具有较高 ID 的图像加载。
 *
 * <p>
 *
 * 跟踪动画图像可能并不总是有用的，因为动画图像的加载和绘制具有多部分性质，但它是支持的。
 * <code>MediaTracker</code> 将动画图像视为完全加载，当第一帧完全加载时。
 * 在这一点上，<code>MediaTracker</code> 会通知任何等待者图像已完全加载。
 * 如果在第一帧加载完成时没有 <code>ImageObserver</code> 观察图像，图像可能会为了节省资源而刷新自身
 * （参见 {@link Image#flush()}）。
 *
 * <p>
 * 以下是一个使用 <code>MediaTracker</code> 的示例：
 * <p>
 * <hr><blockquote><pre>{@code
 * import java.applet.Applet;
 * import java.awt.Color;
 * import java.awt.Image;
 * import java.awt.Graphics;
 * import java.awt.MediaTracker;
 *
 * public class ImageBlaster extends Applet implements Runnable {
 *      MediaTracker tracker;
 *      Image bg;
 *      Image anim[] = new Image[5];
 *      int index;
 *      Thread animator;
 *
 *      // 获取背景图像（id == 0）
 *      // 和动画帧（id == 1）
 *      // 并将它们添加到 MediaTracker
 *      public void init() {
 *          tracker = new MediaTracker(this);
 *          bg = getImage(getDocumentBase(),
 *                  "images/background.gif");
 *          tracker.addImage(bg, 0);
 *          for (int i = 0; i < 5; i++) {
 *              anim[i] = getImage(getDocumentBase(),
 *                      "images/anim"+i+".gif");
 *              tracker.addImage(anim[i], 1);
 *          }
 *      }
 *
 *      // 启动动画线程。
 *      public void start() {
 *          animator = new Thread(this);
 *          animator.start();
 *      }
 *
 *      // 停止动画线程。
 *      public void stop() {
 *          animator = null;
 *      }
 *
 *      // 运行动画线程。
 *      // 首先等待背景图像完全加载
 *      // 并绘制。然后等待所有动画
 *      // 帧加载完成。最后，循环并
 *      // 增加动画帧索引。
 *      public void run() {
 *          try {
 *              tracker.waitForID(0);
 *              tracker.waitForID(1);
 *          } catch (InterruptedException e) {
 *              return;
 *          }
 *          Thread me = Thread.currentThread();
 *          while (animator == me) {
 *              try {
 *                  Thread.sleep(100);
 *              } catch (InterruptedException e) {
 *                  break;
 *              }
 *              synchronized (this) {
 *                  index++;
 *                  if (index >= anim.length) {
 *                      index = 0;
 *                  }
 *              }
 *              repaint();
 *          }
 *      }
 *
 *      // 背景图像填充框架，因此在重绘时
 *      // 不需要清除 applet。
 *      // 只需调用 paint 方法。
 *      public void update(Graphics g) {
 *          paint(g);
 *      }
 *
 *      // 如果加载图像时有任何错误
 *      // 绘制一个大的红色矩形。否则始终绘制
 *      // 背景，以便在加载时逐步显示。
 *      // 最后，只有在所有帧（id == 1）加载完成时
 *      // 才绘制当前动画帧，以避免部分动画。
 *      public void paint(Graphics g) {
 *          if ((tracker.statusAll(false) & MediaTracker.ERRORED) != 0) {
 *              g.setColor(Color.red);
 *              g.fillRect(0, 0, size().width, size().height);
 *              return;
 *          }
 *          g.drawImage(bg, 0, 0, this);
 *          if (tracker.statusID(1, false) == MediaTracker.COMPLETE) {
 *              g.drawImage(anim[index], 10, 10, this);
 *          }
 *      }
 * }
 * } </pre></blockquote><hr>
 *
 * @author      Jim Graham
 * @since       JDK1.0
 */
public class MediaTracker implements java.io.Serializable {

    /**
     * 一个将由媒体跟踪器跟踪的 <code>Component</code>，图像最终将在此组件上绘制。
     *
     * @serial
     * @see #MediaTracker(Component)
     */
    Component target;
    /**
     * 由 <code>MediaTracker</code> 跟踪的 <code>Images</code> 列表的头部。
     *
     * @serial
     * @see #addImage(Image, int)
     * @see #removeImage(Image)
     */
    MediaEntry head;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = -483174189758638095L;

    /**
     * 创建一个媒体跟踪器，用于跟踪给定组件的图像。
     * @param     comp 图像最终将在此组件上绘制
     */
    public MediaTracker(Component comp) {
        target = comp;
    }

    /**
     * 将图像添加到此媒体跟踪器正在跟踪的图像列表中。图像将最终以默认（未缩放）大小渲染。
     * @param     image   要跟踪的图像
     * @param     id      用于跟踪此图像的标识符
     */
    public void addImage(Image image, int id) {
        addImage(image, id, -1, -1);
    }

    /**
     * 将缩放图像添加到此媒体跟踪器正在跟踪的图像列表中。图像将最终以指定的宽度和高度渲染。
     *
     * @param     image   要跟踪的图像
     * @param     id   用于跟踪此图像的标识符
     * @param     w    图像渲染的宽度
     * @param     h    图像渲染的高度
     */
    public synchronized void addImage(Image image, int id, int w, int h) {
        addImageImpl(image, id, w, h);
        Image rvImage = getResolutionVariant(image);
        if (rvImage != null) {
            addImageImpl(rvImage, id,
                    w == -1 ? -1 : 2 * w,
                    h == -1 ? -1 : 2 * h);
        }
    }

    private void addImageImpl(Image image, int id, int w, int h) {
        head = MediaEntry.insert(head,
                                 new ImageMediaEntry(this, image, id, w, h));
    }
    /**
     * 标志，表示媒体当前正在加载。
     * @see         java.awt.MediaTracker#statusAll
     * @see         java.awt.MediaTracker#statusID
     */
    public static final int LOADING = 1;

    /**
     * 标志，表示媒体下载被中止。
     * @see         java.awt.MediaTracker#statusAll
     * @see         java.awt.MediaTracker#statusID
     */
    public static final int ABORTED = 2;

    /**
     * 标志，表示媒体下载遇到错误。
     * @see         java.awt.MediaTracker#statusAll
     * @see         java.awt.MediaTracker#statusID
     */
    public static final int ERRORED = 4;

    /**
     * 标志，表示媒体下载成功完成。
     * @see         java.awt.MediaTracker#statusAll
     * @see         java.awt.MediaTracker#statusID
     */
    public static final int COMPLETE = 8;

    static final int DONE = (ABORTED | ERRORED | COMPLETE);

    /**
     * 检查此媒体跟踪器正在跟踪的所有图像是否已加载完成。
     * <p>
     * 如果图像尚未加载，此方法不会启动加载。
     * <p>
     * 如果在加载或缩放图像时遇到错误，则该图像被视为已加载完成。使用
     * <code>isErrorAny</code> 或 <code>isErrorID</code> 方法检查错误。
     * @return      <code>true</code> 如果所有图像已加载完成、被中止或遇到错误；否则返回 <code>false</code>
     * @see         java.awt.MediaTracker#checkAll(boolean)
     * @see         java.awt.MediaTracker#checkID
     * @see         java.awt.MediaTracker#isErrorAny
     * @see         java.awt.MediaTracker#isErrorID
     */
    public boolean checkAll() {
        return checkAll(false, true);
    }

    /**
     * 检查此媒体跟踪器正在跟踪的所有图像是否已加载完成。
     * <p>
     * 如果 <code>load</code> 标志的值为 <code>true</code>，则此方法将启动尚未加载的图像的加载。
     * <p>
     * 如果在加载或缩放图像时遇到错误，则该图像被视为已加载完成。使用
     * <code>isErrorAny</code> 和 <code>isErrorID</code> 方法检查错误。
     * @param       load   如果为 <code>true</code>，则启动尚未加载的图像的加载
     * @return      <code>true</code> 如果所有图像已加载完成、被中止或遇到错误；否则返回 <code>false</code>
     * @see         java.awt.MediaTracker#checkID
     * @see         java.awt.MediaTracker#checkAll()
     * @see         java.awt.MediaTracker#isErrorAny()
     * @see         java.awt.MediaTracker#isErrorID(int)
     */
    public boolean checkAll(boolean load) {
        return checkAll(load, true);
    }

    private synchronized boolean checkAll(boolean load, boolean verify) {
        MediaEntry cur = head;
        boolean done = true;
        while (cur != null) {
            if ((cur.getStatus(load, verify) & DONE) == 0) {
                done = false;
            }
            cur = cur.next;
        }
        return done;
    }

    /**
     * 检查所有图像的错误状态。
     * @return   <code>true</code> 如果此媒体跟踪器跟踪的任何图像在加载时遇到错误；否则返回 <code>false</code>
     * @see      java.awt.MediaTracker#isErrorID
     * @see      java.awt.MediaTracker#getErrorsAny
     */
    public synchronized boolean isErrorAny() {
        MediaEntry cur = head;
        while (cur != null) {
            if ((cur.getStatus(false, true) & ERRORED) != 0) {
                return true;
            }
            cur = cur.next;
        }
        return false;
    }

    /**
     * 返回遇到错误的所有媒体的列表。
     * @return       一个包含此媒体跟踪器跟踪的遇到错误的媒体对象的数组，如果没有错误则返回 <code>null</code>
     * @see          java.awt.MediaTracker#isErrorAny
     * @see          java.awt.MediaTracker#getErrorsID
     */
    public synchronized Object[] getErrorsAny() {
        MediaEntry cur = head;
        int numerrors = 0;
        while (cur != null) {
            if ((cur.getStatus(false, true) & ERRORED) != 0) {
                numerrors++;
            }
            cur = cur.next;
        }
        if (numerrors == 0) {
            return null;
        }
        Object errors[] = new Object[numerrors];
        cur = head;
        numerrors = 0;
        while (cur != null) {
            if ((cur.getStatus(false, false) & ERRORED) != 0) {
                errors[numerrors++] = cur.getMedia();
            }
            cur = cur.next;
        }
        return errors;
    }

    /**
     * 开始加载此媒体跟踪器跟踪的所有图像。此方法将等待所有图像加载完成。
     * <p>
     * 如果在加载或缩放图像时遇到错误，则该图像被视为已加载完成。使用
     * <code>isErrorAny</code> 或 <code>isErrorID</code> 方法检查错误。
     * @see         java.awt.MediaTracker#waitForID(int)
     * @see         java.awt.MediaTracker#waitForAll(long)
     * @see         java.awt.MediaTracker#isErrorAny
     * @see         java.awt.MediaTracker#isErrorID
     * @exception   InterruptedException  如果任何线程中断了此线程
     */
    public void waitForAll() throws InterruptedException {
        waitForAll(0);
    }

    /**
     * 开始加载此媒体跟踪器跟踪的所有图像。此方法将等待所有图像加载完成，或者等待指定的毫秒数。
     * <p>
     * 如果在加载或缩放图像时遇到错误，则该图像被视为已加载完成。使用
     * <code>isErrorAny</code> 或 <code>isErrorID</code> 方法检查错误。
     * @param       ms       等待加载完成的毫秒数
     * @return      <code>true</code> 如果所有图像成功加载；否则返回 <code>false</code>
     * @see         java.awt.MediaTracker#waitForID(int)
     * @see         java.awt.MediaTracker#waitForAll(long)
     * @see         java.awt.MediaTracker#isErrorAny
     * @see         java.awt.MediaTracker#isErrorID
     * @exception   InterruptedException  如果任何线程中断了此线程。
     */
    public synchronized boolean waitForAll(long ms)
        throws InterruptedException
    {
        long end = System.currentTimeMillis() + ms;
        boolean first = true;
        while (true) {
            int status = statusAll(first, first);
            if ((status & LOADING) == 0) {
                return (status == COMPLETE);
            }
            first = false;
            long timeout;
            if (ms == 0) {
                timeout = 0;
            } else {
                timeout = end - System.currentTimeMillis();
                if (timeout <= 0) {
                    return false;
                }
            }
            wait(timeout);
        }
    }


                /**
     * 计算并返回此媒体跟踪器跟踪的所有媒体的状态的按位包含<b>或</b>。
     * <p>
     * 由<code>MediaTracker</code>类定义的可能标志是<code>LOADING</code>，
     * <code>ABORTED</code>，<code>ERRORED</code>和<code>COMPLETE</code>。
     * 尚未开始加载的图像的状态为零。
     * <p>
     * 如果<code>load</code>的值为<code>true</code>，则此方法开始加载任何尚未加载的图像。
     *
     * @param        load   如果<code>true</code>，开始加载任何尚未加载的图像
     * @return       所有被跟踪媒体的状态的按位包含<b>或</b>
     * @see          java.awt.MediaTracker#statusID(int, boolean)
     * @see          java.awt.MediaTracker#LOADING
     * @see          java.awt.MediaTracker#ABORTED
     * @see          java.awt.MediaTracker#ERRORED
     * @see          java.awt.MediaTracker#COMPLETE
     */
    public int statusAll(boolean load) {
        return statusAll(load, true);
    }

    private synchronized int statusAll(boolean load, boolean verify) {
        MediaEntry cur = head;
        int status = 0;
        while (cur != null) {
            status = status | cur.getStatus(load, verify);
            cur = cur.next;
        }
        return status;
    }

    /**
     * 检查由此媒体跟踪器跟踪并标记为指定标识符的所有图像是否已完成加载。
     * <p>
     * 如果图像尚未加载，此方法不会开始加载图像。
     * <p>
     * 如果在加载或缩放图像时发生错误，则认为该图像已加载完成。使用
     * <code>isErrorAny</code>或<code>isErrorID</code>方法检查错误。
     * @param       id   要检查的图像的标识符
     * @return      <code>true</code>如果所有图像已加载完成、已中止或遇到错误；<code>false</code>否则
     * @see         java.awt.MediaTracker#checkID(int, boolean)
     * @see         java.awt.MediaTracker#checkAll()
     * @see         java.awt.MediaTracker#isErrorAny()
     * @see         java.awt.MediaTracker#isErrorID(int)
     */
    public boolean checkID(int id) {
        return checkID(id, false, true);
    }

    /**
     * 检查由此媒体跟踪器跟踪并标记为指定标识符的所有图像是否已完成加载。
     * <p>
     * 如果<code>load</code>标志的值为<code>true</code>，则此方法开始加载任何尚未加载的图像。
     * <p>
     * 如果在加载或缩放图像时发生错误，则认为该图像已加载完成。使用
     * <code>isErrorAny</code>或<code>isErrorID</code>方法检查错误。
     * @param       id       要检查的图像的标识符
     * @param       load     如果<code>true</code>，开始加载任何尚未加载的图像
     * @return      <code>true</code>如果所有图像已加载完成、已中止或遇到错误；<code>false</code>否则
     * @see         java.awt.MediaTracker#checkID(int, boolean)
     * @see         java.awt.MediaTracker#checkAll()
     * @see         java.awt.MediaTracker#isErrorAny()
     * @see         java.awt.MediaTracker#isErrorID(int)
     */
    public boolean checkID(int id, boolean load) {
        return checkID(id, load, true);
    }

    private synchronized boolean checkID(int id, boolean load, boolean verify)
    {
        MediaEntry cur = head;
        boolean done = true;
        while (cur != null) {
            if (cur.getID() == id
                && (cur.getStatus(load, verify) & DONE) == 0)
            {
                done = false;
            }
            cur = cur.next;
        }
        return done;
    }

    /**
     * 检查由此媒体跟踪器跟踪的具有指定标识符的所有图像的错误状态。
     * @param        id   要检查的图像的标识符
     * @return       <code>true</code>如果具有指定标识符的任何图像在加载过程中遇到错误；<code>false</code>否则
     * @see          java.awt.MediaTracker#isErrorAny
     * @see          java.awt.MediaTracker#getErrorsID
     */
    public synchronized boolean isErrorID(int id) {
        MediaEntry cur = head;
        while (cur != null) {
            if (cur.getID() == id
                && (cur.getStatus(false, true) & ERRORED) != 0)
            {
                return true;
            }
            cur = cur.next;
        }
        return false;
    }

    /**
     * 返回具有指定ID且遇到错误的媒体列表。
     * @param       id   要检查的图像的标识符
     * @return      一个包含由此媒体跟踪器跟踪的具有指定标识符且遇到错误的媒体对象数组，
     *                       如果没有错误则返回<code>null</code>
     * @see         java.awt.MediaTracker#isErrorID
     * @see         java.awt.MediaTracker#isErrorAny
     * @see         java.awt.MediaTracker#getErrorsAny
     */
    public synchronized Object[] getErrorsID(int id) {
        MediaEntry cur = head;
        int numerrors = 0;
        while (cur != null) {
            if (cur.getID() == id
                && (cur.getStatus(false, true) & ERRORED) != 0)
            {
                numerrors++;
            }
            cur = cur.next;
        }
        if (numerrors == 0) {
            return null;
        }
        Object errors[] = new Object[numerrors];
        cur = head;
        numerrors = 0;
        while (cur != null) {
            if (cur.getID() == id
                && (cur.getStatus(false, false) & ERRORED) != 0)
            {
                errors[numerrors++] = cur.getMedia();
            }
            cur = cur.next;
        }
        return errors;
    }

    /**
     * 开始加载由此媒体跟踪器跟踪的具有指定标识符的所有图像。此方法等待所有具有指定标识符的图像加载完成。
     * <p>
     * 如果在加载或缩放图像时发生错误，则认为该图像已加载完成。使用
     * <code>isErrorAny</code>和<code>isErrorID</code>方法检查错误。
     * @param         id   要检查的图像的标识符
     * @see           java.awt.MediaTracker#waitForAll
     * @see           java.awt.MediaTracker#isErrorAny()
     * @see           java.awt.MediaTracker#isErrorID(int)
     * @exception     InterruptedException  如果任何线程中断了此线程。
     */
    public void waitForID(int id) throws InterruptedException {
        waitForID(id, 0);
    }

    /**
     * 开始加载由此媒体跟踪器跟踪的具有指定标识符的所有图像。此方法等待所有具有指定标识符的图像加载完成，
     * 或直到由<code>ms</code>参数指定的毫秒数时间过去。
     * <p>
     * 如果在加载或缩放图像时发生错误，则认为该图像已加载完成。使用
     * <code>statusID</code>、<code>isErrorID</code>和<code>isErrorAny</code>方法检查错误。
     * @param         id   要检查的图像的标识符
     * @param         ms   等待加载完成的时间，以毫秒为单位
     * @see           java.awt.MediaTracker#waitForAll
     * @see           java.awt.MediaTracker#waitForID(int)
     * @see           java.awt.MediaTracker#statusID
     * @see           java.awt.MediaTracker#isErrorAny()
     * @see           java.awt.MediaTracker#isErrorID(int)
     * @exception     InterruptedException  如果任何线程中断了此线程。
     */
    public synchronized boolean waitForID(int id, long ms)
        throws InterruptedException
    {
        long end = System.currentTimeMillis() + ms;
        boolean first = true;
        while (true) {
            int status = statusID(id, first, first);
            if ((status & LOADING) == 0) {
                return (status == COMPLETE);
            }
            first = false;
            long timeout;
            if (ms == 0) {
                timeout = 0;
            } else {
                timeout = end - System.currentTimeMillis();
                if (timeout <= 0) {
                    return false;
                }
            }
            wait(timeout);
        }
    }

    /**
     * 计算并返回由此媒体跟踪器跟踪的具有指定标识符的所有媒体的状态的按位包含<b>或</b>。
     * <p>
     * 由<code>MediaTracker</code>类定义的可能标志是<code>LOADING</code>，
     * <code>ABORTED</code>，<code>ERRORED</code>和<code>COMPLETE</code>。
     * 尚未开始加载的图像的状态为零。
     * <p>
     * 如果<code>load</code>的值为<code>true</code>，则此方法开始加载任何尚未加载的图像。
     * @param        id   要检查的图像的标识符
     * @param        load   如果<code>true</code>，开始加载任何尚未加载的图像
     * @return       具有指定标识符的所有被跟踪媒体的状态的按位包含<b>或</b>
     * @see          java.awt.MediaTracker#statusAll(boolean)
     * @see          java.awt.MediaTracker#LOADING
     * @see          java.awt.MediaTracker#ABORTED
     * @see          java.awt.MediaTracker#ERRORED
     * @see          java.awt.MediaTracker#COMPLETE
     */
    public int statusID(int id, boolean load) {
        return statusID(id, load, true);
    }

    private synchronized int statusID(int id, boolean load, boolean verify) {
        MediaEntry cur = head;
        int status = 0;
        while (cur != null) {
            if (cur.getID() == id) {
                status = status | cur.getStatus(load, verify);
            }
            cur = cur.next;
        }
        return status;
    }

    /**
     * 从此媒体跟踪器中移除指定的图像。
     * 无论比例或ID如何，指定图像的所有实例都将被移除。
     * @param   image     要移除的图像
     * @see     java.awt.MediaTracker#removeImage(java.awt.Image, int)
     * @see     java.awt.MediaTracker#removeImage(java.awt.Image, int, int, int)
     * @since   JDK1.1
     */
    public synchronized void removeImage(Image image) {
        removeImageImpl(image);
        Image rvImage = getResolutionVariant(image);
        if (rvImage != null) {
            removeImageImpl(rvImage);
        }
        notifyAll();    // 如果剩余的图像已完成，则通知。
    }

    private void removeImageImpl(Image image) {
        MediaEntry cur = head;
        MediaEntry prev = null;
        while (cur != null) {
            MediaEntry next = cur.next;
            if (cur.getMedia() == image) {
                if (prev == null) {
                    head = next;
                } else {
                    prev.next = next;
                }
                cur.cancel();
            } else {
                prev = cur;
            }
            cur = next;
        }
    }

    /**
     * 从此媒体跟踪器的指定跟踪ID中移除指定的图像。
     * 无论比例如何，指定ID下跟踪的所有<code>Image</code>实例都将被移除。
     * @param      image 要移除的图像
     * @param      id 要从中移除图像的跟踪ID
     * @see        java.awt.MediaTracker#removeImage(java.awt.Image)
     * @see        java.awt.MediaTracker#removeImage(java.awt.Image, int, int, int)
     * @since      JDK1.1
     */
    public synchronized void removeImage(Image image, int id) {
        removeImageImpl(image, id);
        Image rvImage = getResolutionVariant(image);
        if (rvImage != null) {
            removeImageImpl(rvImage, id);
        }
        notifyAll();    // 如果剩余的图像已完成，则通知。
    }

    private void removeImageImpl(Image image, int id) {
        MediaEntry cur = head;
        MediaEntry prev = null;
        while (cur != null) {
            MediaEntry next = cur.next;
            if (cur.getID() == id && cur.getMedia() == image) {
                if (prev == null) {
                    head = next;
                } else {
                    prev.next = next;
                }
                cur.cancel();
            } else {
                prev = cur;
            }
            cur = next;
        }
    }

    /**
     * 从此媒体跟踪器中移除具有指定宽度、高度和ID的指定图像。
     * 只移除指定的实例（包括任何重复项）。
     * @param   image 要移除的图像
     * @param   id 要从中移除图像的跟踪ID
     * @param   width 要移除的宽度（-1表示未缩放）
     * @param   height 要移除的高度（-1表示未缩放）
     * @see     java.awt.MediaTracker#removeImage(java.awt.Image)
     * @see     java.awt.MediaTracker#removeImage(java.awt.Image, int)
     * @since   JDK1.1
     */
    public synchronized void removeImage(Image image, int id,
                                         int width, int height) {
        removeImageImpl(image, id, width, height);
        Image rvImage = getResolutionVariant(image);
        if (rvImage != null) {
            removeImageImpl(rvImage, id,
                    width == -1 ? -1 : 2 * width,
                    height == -1 ? -1 : 2 * height);
        }
        notifyAll();    // 如果剩余的图像已完成，则通知。
    }

    private void removeImageImpl(Image image, int id, int width, int height) {
        MediaEntry cur = head;
        MediaEntry prev = null;
        while (cur != null) {
            MediaEntry next = cur.next;
            if (cur.getID() == id && cur instanceof ImageMediaEntry
                && ((ImageMediaEntry) cur).matches(image, width, height))
            {
                if (prev == null) {
                    head = next;
                } else {
                    prev.next = next;
                }
                cur.cancel();
            } else {
                prev = cur;
            }
            cur = next;
        }
    }

    synchronized void setDone() {
        notifyAll();
    }


                private static Image getResolutionVariant(Image image) {
        if (image instanceof MultiResolutionToolkitImage) {
            return ((MultiResolutionToolkitImage) image).getResolutionVariant();
        }
        return null;
    }
}

abstract class MediaEntry {
    MediaTracker tracker;
    int ID;
    MediaEntry next;

    int status;
    boolean cancelled;

    MediaEntry(MediaTracker mt, int id) {
        tracker = mt;
        ID = id;
    }

    abstract Object getMedia();

    static MediaEntry insert(MediaEntry head, MediaEntry me) {
        MediaEntry cur = head;
        MediaEntry prev = null;
        while (cur != null) {
            if (cur.ID > me.ID) {
                break;
            }
            prev = cur;
            cur = cur.next;
        }
        me.next = cur;
        if (prev == null) {
            head = me;
        } else {
            prev.next = me;
        }
        return head;
    }

    int getID() {
        return ID;
    }

    abstract void startLoad();

    void cancel() {
        cancelled = true;
    }

    static final int LOADING = MediaTracker.LOADING;
    static final int ABORTED = MediaTracker.ABORTED;
    static final int ERRORED = MediaTracker.ERRORED;
    static final int COMPLETE = MediaTracker.COMPLETE;

    static final int LOADSTARTED = (LOADING | ERRORED | COMPLETE);
    static final int DONE = (ABORTED | ERRORED | COMPLETE);

    synchronized int getStatus(boolean doLoad, boolean doVerify) {
        if (doLoad && ((status & LOADSTARTED) == 0)) {
            status = (status & ~ABORTED) | LOADING;
            startLoad();
        }
        return status;
    }

    void setStatus(int flag) {
        synchronized (this) {
            status = flag;
        }
        tracker.setDone();
    }
}

class ImageMediaEntry extends MediaEntry implements ImageObserver,
java.io.Serializable {
    Image image;
    int width;
    int height;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = 4739377000350280650L;

    ImageMediaEntry(MediaTracker mt, Image img, int c, int w, int h) {
        super(mt, c);
        image = img;
        width = w;
        height = h;
    }

    boolean matches(Image img, int w, int h) {
        return (image == img && width == w && height == h);
    }

    Object getMedia() {
        return image;
    }

    synchronized int getStatus(boolean doLoad, boolean doVerify) {
        if (doVerify) {
            int flags = tracker.target.checkImage(image, width, height, null);
            int s = parseflags(flags);
            if (s == 0) {
                if ((status & (ERRORED | COMPLETE)) != 0) {
                    setStatus(ABORTED);
                }
            } else if (s != status) {
                setStatus(s);
            }
        }
        return super.getStatus(doLoad, doVerify);
    }

    void startLoad() {
        if (tracker.target.prepareImage(image, width, height, this)) {
            setStatus(COMPLETE);
        }
    }

    int parseflags(int infoflags) {
        if ((infoflags & ERROR) != 0) {
            return ERRORED;
        } else if ((infoflags & ABORT) != 0) {
            return ABORTED;
        } else if ((infoflags & (ALLBITS | FRAMEBITS)) != 0) {
            return COMPLETE;
        }
        return 0;
    }

    public boolean imageUpdate(Image img, int infoflags,
                               int x, int y, int w, int h) {
        if (cancelled) {
            return false;
        }
        int s = parseflags(infoflags);
        if (s != 0 && s != status) {
            setStatus(s);
        }
        return ((status & LOADING) != 0);
    }
}
