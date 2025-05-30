
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

import java.awt.peer.FileDialogPeer;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.File;
import sun.awt.AWTAccessor;

/**
 * <code>FileDialog</code> 类显示一个对话框窗口，用户可以通过该窗口选择文件。
 * <p>
 * 由于它是一个模态对话框，当应用程序调用其 <code>show</code> 方法显示对话框时，
 * 它会阻塞应用程序的其余部分，直到用户选择了一个文件。
 *
 * @see Window#show
 *
 * @author      Sami Shaio
 * @author      Arthur van Hoff
 * @since       JDK1.0
 */
public class FileDialog extends Dialog {

    /**
     * 此常量值表示文件对话框的目的是从文件中读取。
     */
    public static final int LOAD = 0;

    /**
     * 此常量值表示文件对话框的目的是向文件中写入。
     */
    public static final int SAVE = 1;

    /*
     * 文件对话框有两种模式：LOAD 和 SAVE。
     * 此整数将表示其中一种模式。
     * 如果未指定模式，则默认为 LOAD。
     *
     * @serial
     * @see getMode()
     * @see setMode()
     * @see java.awt.FileDialog#LOAD
     * @see java.awt.FileDialog#SAVE
     */
    int mode;

    /*
     * 指定在文件对话框中显示的目录的字符串。此变量可能为 <code>null</code>。
     *
     * @serial
     * @see getDirectory()
     * @see setDirectory()
     */
    String dir;

    /*
     * 指定文件对话框中文件名文本字段的初始值的字符串。此变量可能为 <code>null</code>。
     *
     * @serial
     * @see getFile()
     * @see setFile()
     */
    String file;

    /**
     * 包含用户选择的所有文件的 File 实例。
     *
     * @serial
     * @see #getFiles
     * @since 1.7
     */
    private File[] files;

    /**
     * 表示文件对话框是否允许多文件选择。
     *
     * @serial
     * @see #setMultipleMode
     * @see #isMultipleMode
     * @since 1.7
     */
    private boolean multipleMode = false;

    /*
     * 用作文件对话框的文件名过滤器。
     * 文件对话框将仅显示此过滤器接受的文件名。
     * 此变量可能为 <code>null</code>。
     *
     * @serial
     * @see #getFilenameFilter()
     * @see #setFilenameFilter()
     * @see FileNameFilter
     */
    FilenameFilter filter;

    private static final String base = "filedlg";
    private static int nameCounter = 0;

    /*
     * JDK 1.1 serialVersionUID
     */
     private static final long serialVersionUID = 5035145889651310422L;


    static {
        /* 确保加载必要的本机库 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    static {
        AWTAccessor.setFileDialogAccessor(
            new AWTAccessor.FileDialogAccessor() {
                public void setFiles(FileDialog fileDialog, File files[]) {
                    fileDialog.setFiles(files);
                }
                public void setFile(FileDialog fileDialog, String file) {
                    fileDialog.file = ("".equals(file)) ? null : file;
                }
                public void setDirectory(FileDialog fileDialog, String directory) {
                    fileDialog.dir = ("".equals(directory)) ? null : directory;
                }
                public boolean isMultipleMode(FileDialog fileDialog) {
                    synchronized (fileDialog.getObjectLock()) {
                        return fileDialog.multipleMode;
                    }
                }
            });
    }

    /**
     * 初始化可以从 C 访问的字段的 JNI 字段和方法 ID。
     */
    private static native void initIDs();

    /**
     * 创建一个用于加载文件的文件对话框。文件对话框的标题最初为空。这是
     * <code>FileDialog(parent, "", LOAD)</code> 的便捷方法。
     *
     * @param parent 对话框的所有者
     * @since JDK1.1
     */
    public FileDialog(Frame parent) {
        this(parent, "", LOAD);
    }

    /**
     * 创建一个具有指定标题的文件对话框，用于加载文件。显示的文件是当前目录中的文件。
     * 这是 <code>FileDialog(parent, title, LOAD)</code> 的便捷方法。
     *
     * @param     parent   对话框的所有者
     * @param     title    对话框的标题
     */
    public FileDialog(Frame parent, String title) {
        this(parent, title, LOAD);
    }

    /**
     * 创建一个具有指定标题的文件对话框，用于加载或保存文件。
     * <p>
     * 如果 <code>mode</code> 的值为 <code>LOAD</code>，则文件对话框用于读取文件，显示的文件是当前目录中的文件。
     * 如果 <code>mode</code> 的值为 <code>SAVE</code>，则文件对话框用于写入文件。
     *
     * @param     parent   对话框的所有者
     * @param     title   对话框的标题
     * @param     mode   对话框的模式；可以是
     *          <code>FileDialog.LOAD</code> 或 <code>FileDialog.SAVE</code>
     * @exception  IllegalArgumentException 如果提供了非法的文件对话框模式
     * @see       java.awt.FileDialog#LOAD
     * @see       java.awt.FileDialog#SAVE
     */
    public FileDialog(Frame parent, String title, int mode) {
        super(parent, title, true);
        this.setMode(mode);
        setLayout(null);
    }

    /**
     * 创建一个用于加载文件的文件对话框。文件对话框的标题最初为空。这是
     * <code>FileDialog(parent, "", LOAD)</code> 的便捷方法。
     *
     * @param     parent   对话框的所有者
     * @exception java.lang.IllegalArgumentException 如果 <code>parent</code> 的
     *            <code>GraphicsConfiguration</code>
     *            不来自屏幕设备；
     * @exception java.lang.IllegalArgumentException 如果 <code>parent</code>
     *            为 <code>null</code>；当 <code>GraphicsEnvironment.isHeadless</code>
     *            返回 <code>true</code> 时，此异常总是被抛出
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @since 1.5
     */
    public FileDialog(Dialog parent) {
        this(parent, "", LOAD);
    }

    /**
     * 创建一个具有指定标题的文件对话框，用于加载文件。显示的文件是当前目录中的文件。
     * 这是 <code>FileDialog(parent, title, LOAD)</code> 的便捷方法。
     *
     * @param     parent   对话框的所有者
     * @param     title    对话框的标题；<code>null</code> 值将被接受而不会抛出
     *                     <code>NullPointerException</code>
     * @exception java.lang.IllegalArgumentException 如果 <code>parent</code> 的
     *            <code>GraphicsConfiguration</code>
     *            不来自屏幕设备；
     * @exception java.lang.IllegalArgumentException 如果 <code>parent</code>
     *            为 <code>null</code>；当 <code>GraphicsEnvironment.isHeadless</code>
     *            返回 <code>true</code> 时，此异常总是被抛出
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @since     1.5
     */
    public FileDialog(Dialog parent, String title) {
        this(parent, title, LOAD);
    }

    /**
     * 创建一个具有指定标题的文件对话框，用于加载或保存文件。
     * <p>
     * 如果 <code>mode</code> 的值为 <code>LOAD</code>，则文件对话框用于读取文件，显示的文件是当前目录中的文件。
     * 如果 <code>mode</code> 的值为 <code>SAVE</code>，则文件对话框用于写入文件。
     *
     * @param     parent   对话框的所有者
     * @param     title    对话框的标题；<code>null</code> 值将被接受而不会抛出
     *                     <code>NullPointerException</code>
     * @param     mode     对话框的模式；可以是
     *                     <code>FileDialog.LOAD</code> 或 <code>FileDialog.SAVE</code>
     * @exception java.lang.IllegalArgumentException 如果提供了非法的
     *            文件对话框模式；
     * @exception java.lang.IllegalArgumentException 如果 <code>parent</code> 的
     *            <code>GraphicsConfiguration</code>
     *            不来自屏幕设备；
     * @exception java.lang.IllegalArgumentException 如果 <code>parent</code>
     *            为 <code>null</code>；当 <code>GraphicsEnvironment.isHeadless</code>
     *            返回 <code>true</code> 时，此异常总是被抛出
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @see       java.awt.FileDialog#LOAD
     * @see       java.awt.FileDialog#SAVE
     * @since     1.5
     */
    public FileDialog(Dialog parent, String title, int mode) {
        super(parent, title, true);
        this.setMode(mode);
        setLayout(null);
    }

    /**
     * 为该组件构造一个名称。当名称为 <code>null</code> 时由 <code>getName()</code> 调用。
     */
    String constructComponentName() {
        synchronized (FileDialog.class) {
            return base + nameCounter++;
        }
    }

    /**
     * 创建文件对话框的对等体。对等体允许我们在不改变文件对话框功能的情况下改变其外观。
     */
    public void addNotify() {
        synchronized(getTreeLock()) {
            if (parent != null && parent.getPeer() == null) {
                parent.addNotify();
            }
            if (peer == null)
                peer = getToolkit().createFileDialog(this);
            super.addNotify();
        }
    }

    /**
     * 指示此文件对话框是用于从文件加载还是用于保存到文件。
     *
     * @return   此文件对话框的模式，可以是
     *               <code>FileDialog.LOAD</code> 或
     *               <code>FileDialog.SAVE</code>
     * @see      java.awt.FileDialog#LOAD
     * @see      java.awt.FileDialog#SAVE
     * @see      java.awt.FileDialog#setMode
     */
    public int getMode() {
        return mode;
    }

    /**
     * 设置文件对话框的模式。如果 <code>mode</code> 不是合法值，将抛出异常且 <code>mode</code>
     * 不会被设置。
     *
     * @param      mode  此文件对话框的模式，可以是
     *                 <code>FileDialog.LOAD</code> 或
     *                 <code>FileDialog.SAVE</code>
     * @see        java.awt.FileDialog#LOAD
     * @see        java.awt.FileDialog#SAVE
     * @see        java.awt.FileDialog#getMode
     * @exception  IllegalArgumentException 如果提供了非法的文件对话框模式
     * @since      JDK1.1
     */
    public void setMode(int mode) {
        switch (mode) {
          case LOAD:
          case SAVE:
            this.mode = mode;
            break;
          default:
            throw new IllegalArgumentException("非法的文件对话框模式");
        }
    }

    /**
     * 获取此文件对话框的目录。
     *
     * @return  此 <code>FileDialog</code> 的目录（可能为 <code>null</code> 或无效）
     * @see       java.awt.FileDialog#setDirectory
     */
    public String getDirectory() {
        return dir;
    }

    /**
     * 将此文件对话框的目录设置为指定的目录。指定 <code>null</code> 或无效目录表示实现定义的默认值。
     * 但是，此默认值不会在用户选择文件之前实现，直到这一点，<code>getDirectory()</code>
     * 将返回传递给此方法的值。
     * <p>
     * 指定 "" 作为目录与指定 <code>null</code> 作为目录完全等效。
     *
     * @param     dir   指定的目录
     * @see       java.awt.FileDialog#getDirectory
     */
    public void setDirectory(String dir) {
        this.dir = (dir != null && dir.equals("")) ? null : dir;
        FileDialogPeer peer = (FileDialogPeer)this.peer;
        if (peer != null) {
            peer.setDirectory(this.dir);
        }
    }

    /**
     * 获取此文件对话框中选择的文件。如果用户选择了 <code>CANCEL</code>，返回的文件为 <code>null</code>。
     *
     * @return    此文件对话框中当前选择的文件，如果没有选择文件则为 <code>null</code>
     * @see       java.awt.FileDialog#setFile
     */
    public String getFile() {
        return file;
    }

    /**
     * 返回用户选择的文件。
     * <p>
     * 如果用户取消了文件对话框，则该方法返回一个空数组。
     *
     * @return    用户选择的文件或一个空数组（如果用户取消了文件对话框）。
     * @see       #setFile(String)
     * @see       #getFile
     * @since 1.7
     */
    public File[] getFiles() {
        synchronized (getObjectLock()) {
            if (files != null) {
                return files.clone();
            } else {
                return new File[0];
            }
        }
    }

    /**
     * 存储用户选择的所有文件的名称。
     *
     * 注意，此方法是私有的，旨在通过 AWTAccessor API 由对等体使用。
     *
     * @param files     包含用户选择的所有文件的短名称的数组。
     *
     * @see #getFiles
     * @since 1.7
     */
    private void setFiles(File files[]) {
        synchronized (getObjectLock()) {
            this.files = files;
        }
    }


                /**
     * 为文件对话窗口设置选定的文件。
     * 此文件在文件对话窗口首次显示之前设置，将成为默认文件。
     * <p>
     * 当对话框显示时，指定的文件将被选中。选择的类型取决于文件是否存在、对话框类型和原生平台。例如，文件可以在文件列表中被高亮显示，或者文件名编辑框可以被填充文件名。
     * <p>
     * 该方法接受完整的文件路径，或者与 {@code setDirectory} 方法一起使用时接受带有扩展名的文件名。
     * <p>
     * 指定 "" 作为文件与指定 {@code null} 作为文件完全等效。
     *
     * @param    file   被设置的文件
     * @see      #getFile
     * @see      #getFiles
     */
    public void setFile(String file) {
        this.file = (file != null && file.equals("")) ? null : file;
        FileDialogPeer peer = (FileDialogPeer)this.peer;
        if (peer != null) {
            peer.setFile(this.file);
        }
    }

    /**
     * 启用或禁用文件对话框的多文件选择。
     *
     * @param enable    如果 {@code true}，则启用多文件选择；{@code false} - 禁用。
     * @see #isMultipleMode
     * @since 1.7
     */
    public void setMultipleMode(boolean enable) {
        synchronized (getObjectLock()) {
            this.multipleMode = enable;
        }
    }

    /**
     * 返回文件对话框是否允许多文件选择。
     *
     * @return          如果文件对话框允许多文件选择，则返回 {@code true}；否则返回 {@code false}。
     * @see #setMultipleMode
     * @since 1.7
     */
    public boolean isMultipleMode() {
        synchronized (getObjectLock()) {
            return multipleMode;
        }
    }

    /**
     * 确定文件对话框的文件名过滤器。文件名过滤器允许用户指定在文件对话框窗口中显示的文件。文件名过滤器在 Sun 的 Microsoft Windows 参考实现中不起作用。
     *
     * @return    文件对话框的文件名过滤器
     * @see       java.io.FilenameFilter
     * @see       java.awt.FileDialog#setFilenameFilter
     */
    public FilenameFilter getFilenameFilter() {
        return filter;
    }

    /**
     * 为文件对话窗口设置指定的文件名过滤器。
     * 文件名过滤器在 Sun 的 Microsoft Windows 参考实现中不起作用。
     *
     * @param   filter   指定的过滤器
     * @see     java.io.FilenameFilter
     * @see     java.awt.FileDialog#getFilenameFilter
     */
    public synchronized void setFilenameFilter(FilenameFilter filter) {
        this.filter = filter;
        FileDialogPeer peer = (FileDialogPeer)this.peer;
        if (peer != null) {
            peer.setFilenameFilter(filter);
        }
    }

    /**
     * 读取 <code>ObjectInputStream</code> 并通过将 <code>dir</code> 或 <code>file</code>
     * 等于空字符串转换为 <code>null</code> 来执行向后兼容性检查。
     *
     * @param s the <code>ObjectInputStream</code> to read
     */
    private void readObject(ObjectInputStream s)
        throws ClassNotFoundException, IOException
    {
        s.defaultReadObject();

        // 1.1 兼容性：在 1.1 中 "" 不转换为 null
        if (dir != null && dir.equals("")) {
            dir = null;
        }
        if (file != null && file.equals("")) {
            file = null;
        }
    }

    /**
     * 返回表示此 <code>FileDialog</code> 窗口状态的字符串。此方法仅用于调试目的，返回的字符串的内容和格式可能因实现而异。返回的字符串可以为空，但不能为 <code>null</code>。
     *
     * @return  此文件对话窗口的参数字符串
     */
    protected String paramString() {
        String str = super.paramString();
        str += ",dir= " + dir;
        str += ",file= " + file;
        return str + ((mode == LOAD) ? ",load" : ",save");
    }

    boolean postsOldMouseEvents() {
        return false;
    }
}
