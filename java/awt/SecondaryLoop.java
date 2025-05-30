/*
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
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
 * 一个辅助接口，用于运行嵌套的事件循环。
 * <p>
 * 实现此接口的对象是通过 {@link EventQueue#createSecondaryLoop} 方法创建的。该接口
 * 提供了两个方法，{@link #enter} 和 {@link #exit}，
 * 可以用于启动和停止事件循环。
 * <p>
 * 当调用 {@link #enter} 方法时，当前线程将被阻塞，直到通过
 * {@link #exit} 方法终止循环。此外，事件分发线程上会启动一个新的事件循环，
 * 该线程可能是也可能不是当前线程。可以在任何线程上调用其 {@link #exit} 方法来终止循环。
 * 终止循环后，可以重新使用 {@code SecondaryLoop} 对象来运行新的嵌套事件循环。
 * <p>
 * 应用此接口的典型用例是 AWT 和 Swing 模态对话框。当在事件分发线程上显示模态对话框时，
 * 它会进入一个新的二级循环。稍后，当对话框被隐藏或销毁时，它会退出循环，线程继续执行。
 * <p>
 * 以下示例说明了二级循环的简单用法：
 *
 * <pre>
 *   SecondaryLoop loop;
 *
 *   JButton jButton = new JButton("Button");
 *   jButton.addActionListener(new ActionListener() {
 *       {@code @Override}
 *       public void actionPerformed(ActionEvent e) {
 *           Toolkit tk = Toolkit.getDefaultToolkit();
 *           EventQueue eq = tk.getSystemEventQueue();
 *           loop = eq.createSecondaryLoop();
 *
 *           // 启动一个新线程来执行任务
 *           Thread worker = new WorkerThread();
 *           worker.start();
 *
 *           // 进入循环以阻塞当前事件处理程序，但保持 UI 响应
 *           if (!loop.enter()) {
 *               // 报告错误
 *           }
 *       }
 *   });
 *
 *   class WorkerThread extends Thread {
 *       {@code @Override}
 *       public void run() {
 *           // 执行计算
 *           doSomethingUseful();
 *
 *           // 退出循环
 *           loop.exit();
 *       }
 *   }
 * </pre>
 *
 * @see Dialog#show
 * @see EventQueue#createSecondaryLoop
 * @see Toolkit#getSystemEventQueue
 *
 * @author Anton Tarasov, Artem Ananiev
 *
 * @since 1.7
 */
public interface SecondaryLoop {

    /**
     * 阻塞当前线程的执行，并在事件分发线程上进入一个新的二级事件循环。
     * <p>
     * 该方法可以由任何线程调用，包括事件分发线程。该线程将被阻塞，直到调用 {@link
     * #exit} 方法或循环终止。无论哪种情况，事件分发线程上都会创建一个新的二级循环来分发事件。
     * <p>
     * 该方法每次只能启动一个新事件循环。如果此对象已经启动了一个二级事件循环并且当前仍在运行，
     * 则此方法返回 {@code false}，表示未能启动新的事件循环。否则，该方法将阻塞调用线程，稍后在
     * 新事件循环终止时返回 {@code true}。此时，可以再次使用此对象来启动另一个新的事件循环。
     *
     * @return 如果此调用启动了二级循环，则在二级循环终止后返回 {@code true}，
     *         否则返回 {@code false}
     */
    public boolean enter();

    /**
     * 解除由 {@link #enter} 方法阻塞的线程的阻塞，并退出二级循环。
     * <p>
     * 该方法恢复调用 {@link #enter} 方法的线程，并退出在调用 {@link #enter} 方法时创建的二级循环。
     * <p>
     * 注意，如果在该循环运行时启动了任何其他二级循环，则被阻塞的线程不会恢复执行，直到嵌套循环终止。
     * <p>
     * 如果此二级循环尚未通过 {@link #enter} 方法启动，或者此二级循环已经通过 {@link #exit} 方法终止，
     * 则此方法返回 {@code false}，否则返回 {@code true}。
     *
     * @return 如果此循环之前已启动且尚未通过 {@link #exit} 方法终止，则返回 {@code true}，
     *         否则返回 {@code false}
     */
    public boolean exit();

}
